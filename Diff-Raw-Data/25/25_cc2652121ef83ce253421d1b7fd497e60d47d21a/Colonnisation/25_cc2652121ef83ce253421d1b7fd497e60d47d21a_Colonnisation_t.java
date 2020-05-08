 package fr.aumgn.dac2.game.colonnisation;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.localization.PluginMessages;
 import fr.aumgn.bukkitutils.playerref.PlayerRef;
 import fr.aumgn.bukkitutils.playerref.map.PlayersRefHashMap;
 import fr.aumgn.bukkitutils.playerref.map.PlayersRefMap;
 import fr.aumgn.bukkitutils.timer.Timer;
 import fr.aumgn.dac2.DAC;
 import fr.aumgn.dac2.arena.regions.Pool;
 import fr.aumgn.dac2.game.AbstractGame;
 import fr.aumgn.dac2.game.GameParty;
 import fr.aumgn.dac2.game.GameTimer;
 import fr.aumgn.dac2.game.start.GameStartData;
 import fr.aumgn.dac2.game.start.PlayerStartData;
 import fr.aumgn.dac2.shape.column.Column;
 import fr.aumgn.dac2.shape.column.ColumnPattern;
 import fr.aumgn.dac2.shape.column.GlassyPattern;
 
 public class Colonnisation extends AbstractGame {
 
     private final GameParty<ColonnPlayer> party;
     private final PlayersRefMap<ColonnPlayer> playersMap;
 
     private final Runnable turnTimedOut = new Runnable() {
         @Override
         public void run() {
             turnTimedOut();
         }
     };
 
     private boolean finished;
     private Timer timer;
     private int setupTurns;
 
     public Colonnisation(DAC dac, GameStartData data) {
         super(dac, data);
 
         Map<PlayerRef, ? extends PlayerStartData> playersData =
                 data.getPlayersData();
         List<ColonnPlayer> list =
                 new ArrayList<ColonnPlayer>(playersData.size());
         playersMap = new PlayersRefHashMap<ColonnPlayer>();
 
         for (Entry<PlayerRef, ? extends PlayerStartData> entry :
                 playersData.entrySet()) {
             PlayerRef playerId = entry.getKey();
             ColonnPlayer player = new ColonnPlayer(playerId,
                     playersData.get(playerId));
             list.add(player);
             playersMap.put(playerId, player);
         }
         party = new GameParty<ColonnPlayer>(this, ColonnPlayer.class, list);
 
         finished = false;
     }
 
     @Override
     public void start() {
         resetPoolOnStart();
 
         double size = arena.getPool().size2D();
         double setupColumns = size * dac.getConfig().getColonnisationRatio();
         setupTurns = (int) Math.ceil(setupColumns / party.size());
 
         send("colonnisation.start");
         send("colonnisation.playerslist");
         for (ColonnPlayer player : party.iterable()) {
            send("colonnisation.start.playerentry", player.getIndex() + 1,
                     player.getDisplayName());
         }
         send("colonnisation.setup.turns", setupTurns);
         nextTurn();
     }
 
     @Override
     public void onNewTurn() {
         setupTurns--;
         if (setupTurns == 0) {
             send("colonnisation.setup.finished");
             send("colonnisation.enjoy");
         }
     }
 
     private void nextTurn() {
         ColonnPlayer player = party.nextTurn();
 
         if (!player.isOnline()) {
             send("colonnisation.playerturn.notconnected",
                     player.getDisplayName());
             removePlayer(player);
             if (!finished) {
                 nextTurn();
             }
             return;
         }
 
         if (timer != null) {
             timer.cancel();
         }
         timer = new GameTimer(dac, this, turnTimedOut);
         timer.start();
 
         send("colonnisation.playerturn", player.getDisplayName());
         tpBeforeJump(player);
     }
 
     private void turnTimedOut() {
         ColonnPlayer player = party.getCurrent();
         removePlayer(player);
         if (!finished) {
             nextTurn();
         }
     }
 
     private void removePlayer(ColonnPlayer player) {
         party.removePlayer(player);
         playersMap.remove(player.playerId);
         spectators.add(player.playerId);
         if (party.size() < 2) {
             dac.getStages().stop(this);
         }
     }
 
     @Override
     public void stop(boolean force) {
         finished = true;
         resetPoolOnEnd();
         if (timer != null) {
             timer.cancel();
         }
 
         if (force) {
             send("colonnisation.stopped");
         } else {
             send("colonnisation.finished");
         }
 
         ColonnPlayer[] ranking = party.iterable().clone();
         Arrays.sort(ranking);
 
         int index = ranking.length - 1;
         send("colonnisation.winner", ranking[index].getDisplayName(),
                 ranking[index].getScore());
         index--;
         for (; index >= 0; index--) {
             send("colonnisation.ranking", ranking.length - index,
                     ranking[index].getDisplayName(), ranking[index].getScore());
         }
     }
 
     @Override
     public boolean contains(Player player) {
         return playersMap.containsKey(player);
     }
 
     @Override
     public void sendMessage(String message) {
         for (ColonnPlayer player : party.iterable()) {
             player.sendMessage(message);
         }
         sendSpectators(message);
     }
 
     @Override
     public boolean isPlayerTurn(Player player) {
         ColonnPlayer gamePlayer = playersMap.get(player);
         return gamePlayer != null && party.isTurn(gamePlayer);
     }
 
     @Override
     public void onJumpSuccess(Player player) {
         ColonnPlayer gamePlayer = playersMap.get(player);
         World world = arena.getWorld();
         Pool pool = arena.getPool();
 
         Column column = pool.getColumn(player);
         ColumnPattern pattern;
         if (setupTurns > 0) {
             pattern = dac.getConfig().getNeutralPattern();
             send("colonnisation.setup.success", gamePlayer.getDisplayName());
         } else {
             pattern = gamePlayer.getColumnPattern();
             boolean isADAC = column.isADAC(world);
             if (isADAC) {
                 gamePlayer.incrementMultiplier();
                 pattern = new GlassyPattern(pattern);
             }
 
             PoolVisitor visitor = new PoolVisitor(world, pool,
                     gamePlayer.getColor());
             int points = visitor.visit(column.getPos());
             points *= gamePlayer.getMultiplier();
             gamePlayer.addPoints(points);
 
             if (isADAC) {
                 send("colonnisation.multiplier.increment",
                         gamePlayer.getMultiplier());
             }
             send("colonnisation.jump.success", gamePlayer.getDisplayName(),
                     points, gamePlayer.getScore());
         }
 
         column.set(world, pattern);
         tpAfterJumpSuccess(gamePlayer, column);
         if (pool.isFilled(world)) {
             dac.getStages().stop(this);
         } else {
             nextTurn();
         }
     }
 
     @Override
     public void onJumpFail(Player player) {
         ColonnPlayer gamePlayer = playersMap.get(player);
 
         send("colonnisation.jump.fail", gamePlayer.getDisplayName());
         if (gamePlayer.getMultiplier() > 1) {
             gamePlayer.resetMultiplier();
             send("colonnisation.multiplier.reset");
         }
 
         tpAfterJumpFail(gamePlayer);
         nextTurn();
     }
 
     @Override
     public void onQuit(Player player) {
         ColonnPlayer gamePlayer = playersMap.get(player);
         removePlayer(gamePlayer);
         send("colonnisation.player.quit", gamePlayer.getDisplayName(),
                 gamePlayer.getScore());
         if (!finished) {
             nextTurn();
         }
     }
 
     @Override
     public void list(CommandSender sender) {
         PluginMessages messages = dac.getMessages();
 
         sender.sendMessage(messages.get("colonnisation.playerslist"));
         for (ColonnPlayer player : party.iterable()) {
             sender.sendMessage(messages.get("colonnisation.playerentry", 
                    player.getIndex(), player.getDisplayName(),
                     player.getScore()));
         }
     }
 }
