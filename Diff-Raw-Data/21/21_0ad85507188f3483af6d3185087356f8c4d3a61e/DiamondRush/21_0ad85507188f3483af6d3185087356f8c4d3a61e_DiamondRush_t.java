 package fr.aumgn.diamondrush;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginManager;
 
 import fr.aumgn.bukkitutils.command.exception.CommandError;
 import fr.aumgn.diamondrush.config.DRConfig;
 import fr.aumgn.diamondrush.event.game.DRGameStartEvent;
 import fr.aumgn.diamondrush.event.game.DRGameStopEvent;
 import fr.aumgn.diamondrush.event.game.DRGameWinEvent;
 import fr.aumgn.diamondrush.event.players.DRPlayerJoinEvent;
 import fr.aumgn.diamondrush.event.players.DRPlayerQuitEvent;
 import fr.aumgn.diamondrush.event.spectators.DRSpectatorJoinEvent;
 import fr.aumgn.diamondrush.event.spectators.DRSpectatorQuitEvent;
 import fr.aumgn.diamondrush.event.team.DRTeamLooseEvent;
 import fr.aumgn.diamondrush.event.team.DRTeamSpawnSetEvent;
 import fr.aumgn.diamondrush.event.team.DRTotemBreakEvent;
 import fr.aumgn.diamondrush.event.team.DRTotemSetEvent;
 import fr.aumgn.diamondrush.game.Game;
 import fr.aumgn.diamondrush.game.Team;
 import fr.aumgn.diamondrush.listeners.GameListener;
 import fr.aumgn.diamondrush.listeners.RegionsListener;
 import fr.aumgn.diamondrush.listeners.SpectatorsListener;
 import fr.aumgn.diamondrush.listeners.SpectatorsVisibilityListener;
 import fr.aumgn.diamondrush.listeners.StatisticsListener;
 import fr.aumgn.diamondrush.region.TeamSpawn;
 import fr.aumgn.diamondrush.region.Totem;
 import fr.aumgn.diamondrush.stage.JoinStage;
 import fr.aumgn.diamondrush.stage.PauseStage;
 import fr.aumgn.diamondrush.stage.Stage;
 import fr.aumgn.diamondrush.stage.StartStage;
 import fr.aumgn.diamondrush.views.GameView;
 
 public final class DiamondRush {
 
     private DiamondRushPlugin plugin;
     private DRConfig config;
     private Game game;
     private Stage stage;
     private Listener[] listeners;
 
     public DiamondRush(DiamondRushPlugin plugin) {
         this.plugin = plugin;
         reloadConfig();
         this.game = null;
         this.stage = null;
         this.listeners = new Listener[5];
     }
 
     public DiamondRushPlugin getPlugin() {
         return plugin;
     }
 
     public DRConfig getConfig() {
         return config;
     }
 
     public boolean isRunning() {
         return game != null;
     }
 
     public Game getGame() {
         return game;
     }
 
     public Stage getStage() {
         return stage;
     }
 
     public void initGame(Game game, JoinStage stage) {
         this.game = game;
         PluginManager pm = Bukkit.getPluginManager();
         listeners[0] = new GameListener(this);
         listeners[1] = new RegionsListener(this);
         listeners[2] = new SpectatorsListener(this);
         listeners[3] = new SpectatorsVisibilityListener(this);
         listeners[4] = new StatisticsListener(this);
         for (Listener listener : listeners) {
             pm.registerEvents(listener, plugin);
         }
         nextStage(stage);
     }
 
     public void reloadConfig() {
         this.config = plugin.loadDRConfig();
     }
 
     private void unregisterStageListeners() {
         for (Listener listener : stage.getListeners()) {
             HandlerList.unregisterAll(listener);
         }
     }
 
     private void registerStageListeners() {
         for (Listener listener : stage.getListeners()) {
             Bukkit.getPluginManager().registerEvents(listener, plugin);
         }
     }
 
     public void nextStage(Stage newStage) {
         if (newStage == null) {
             throw new IllegalArgumentException("New stage cannot be null");
         }
         if (stage != null) {
             unregisterStageListeners();
             Bukkit.getScheduler().cancelTasks(plugin);
             stage.stop();
         }
         stage = newStage;
         registerStageListeners();
         stage.start();
     }
 
     public boolean isPaused() {
         return (stage instanceof PauseStage);
     }
 
     public void pause() {
         if (stage == null) {
             throw new UnsupportedOperationException();
         }
         unregisterStageListeners();
         stage.pause();
         stage = new PauseStage(this, stage);
         stage.start();
         registerStageListeners();
     }
 
     public void resume() {
         if (stage == null) {
             throw new UnsupportedOperationException();
         }
         unregisterStageListeners();
         stage.stop();
         stage = ((PauseStage) stage).getOldStage();
         registerStageListeners();
         stage.resume();
     }
 
     private void forceStop() {
         for (Listener listener : stage.getListeners()) {
             HandlerList.unregisterAll(listener);
         }
         Bukkit.getScheduler().cancelTasks(plugin);
 
         for (Listener listener : listeners) {
             HandlerList.unregisterAll(listener);
         }
         game = null;
     }
 
     public void playerJoin(Team team, Player player) {
         if (!game.contains(player)) {
             DRPlayerJoinEvent event = new DRPlayerJoinEvent(game, team, player);
             Util.callEvent(event);
             if (!event.isCancelled()) {
                 game.addPlayer(player, event.getTeam());
                 Util.broadcast(player.getDisplayName() + ChatColor.YELLOW +
                         " a rejoint l'équipe " + event.getTeam().getDisplayName());
             }
         } else {
             player.sendMessage(ChatColor.RED + 
                     "Vous êtes déjà dans la partie.");
         }
     }
 
     public void playerQuit(Player player) {
         if (game.contains(player)) {
             DRPlayerQuitEvent event = new DRPlayerQuitEvent(game, player); 
             Util.callEvent(event);
             if (!event.isCancelled()) {
                 game.removePlayer(player);
                 Util.broadcast(player.getDisplayName() + ChatColor.YELLOW +
                         " a quitté la partie.");
             }
         } else {
             player.sendMessage(ChatColor.RED + 
                     "Vous n'êtes pas dans la partie.");
         }
     }
 
     public void spectatorJoin(Player spectator) {
         DRSpectatorJoinEvent event = new DRSpectatorJoinEvent(game, spectator);
         if (!game.getSpectators().contains(spectator)) {
             Util.callEvent(event);
             if (!event.isCancelled()) {
                 Player player = event.getSpectator();
                 game.sendMessage(player.getDisplayName() + ChatColor.YELLOW +
                         " est maintenant spectateur.");
                 game.getSpectators().add(player);
                 player.sendMessage(ChatColor.GREEN + "Vous êtes maintenant spectateur.");
             }
         } else {
             event.setCancelled(true);
             spectator.sendMessage(ChatColor.RED + "Vous êtes déjà spectateur.");
         }
     }
 
     public void spectatorQuit(Player spectator) {
         DRSpectatorQuitEvent event = new DRSpectatorQuitEvent(game, spectator);
         if (game.getSpectators().contains(spectator)) {
             Util.callEvent(event);
             game.getSpectators().remove(spectator);
             spectator.sendMessage(ChatColor.GREEN + "Vous n'êtes plus spectateur.");
             game.sendMessage(spectator.getDisplayName() + ChatColor.YELLOW +
                     " n'est plus spectateur.");
         } else {
             spectator.sendMessage(spectator.getDisplayName() + "n'est pas spectateur.");
         }
     }
 
     public void startGame() {
         if (!(stage instanceof JoinStage)) {
             throw new CommandError("Cette commande ne peut être utilisée que durant la phase de join.");
         }
 
         if (stage.hasNextStageScheduled()) {
             throw new CommandError(
                     "La partie est déjà sur le point de démarrer.");
         }
 
         DRGameStartEvent event = new DRGameStartEvent(game);
         Util.callEvent(event);
         if (!event.isCancelled()) {
             game.sendMessage(ChatColor.GREEN + "La partie va commencer.");
             nextStage(new StartStage(this));
         }
     }
 
     public void gameWin(Team team) {
         DRGameWinEvent event = new DRGameWinEvent(game, team);
         Util.callEvent(event);
         Team winningTeam = game.getTeams().get(0);
         String msg = ChatColor.GREEN +
                 "L'équipe " + winningTeam.getDisplayName() +
                 ChatColor.GREEN + " a gagné la partie.";
         Util.broadcast(msg);
         GameView view = new GameView(game, false, false);
         for (String message : view) {
             Util.broadcast(message);
         }
         forceStop();
     }
 
     public void gameStop() {
         DRGameStopEvent event = new DRGameStopEvent(game);
         Util.callEvent(event);
         game.sendMessage(ChatColor.RED + "La partie a été arretée.");
         forceStop();
     }
 
     public boolean totemBreak(Team team, Totem totem, Player player) {
         DRTotemBreakEvent event = new DRTotemBreakEvent(game, team, totem, player);
         Util.callEvent(event);
         if (!event.isCancelled()) {
             team.decreaseLives();
             if (team.getLives() == 0) {
                 Team responsible = game.getTeam(player);
                 teamLoose(team, responsible, totem);
             } else {
                 game.sendMessage(ChatColor.YELLOW + "L'équipe " + team.getDisplayName() + 
                         ChatColor.YELLOW + " a perdu une vie. " + team.getLives() + " restantes.");
             }
         }
         return event.isCancelled();
     }
 
     public void teamLoose(Team team, Team responsible, Totem totem) {
         DRTeamLooseEvent event = new DRTeamLooseEvent(game, team, responsible, totem);
         Util.callEvent(event);
         game.removeTeam(team);
         if (game.getTeams().size() == 1) {
             gameWin(team);
         } else {
             String msg = ChatColor.RED +"L'équipe " + team.getDisplayName() 
                     + ChatColor.RED + " a perdu la partie.";
             game.sendMessage(msg);
             team.sendMessage(msg);
            for (Player player : team.getPlayers()) {
                spectatorJoin(player);
            }
         }
     }
 
     public void totemSet(Team team, Totem totem) {
         DRTotemSetEvent event = new DRTotemSetEvent(game, team, totem);
         Util.callEvent(event);
         team.setTotem(totem);
         totem.create(game.getWorld(), team.getColor(), team.getLives());
     }
 
     public void teamSpawnSet(Team team, TeamSpawn spawn) {
         DRTeamSpawnSetEvent event = new DRTeamSpawnSetEvent(game, team, spawn); 
         Util.callEvent(event);
         team.setSpawn(spawn);
         team.getSpawn().create(game.getWorld(), team.getColor());
     }
 }
