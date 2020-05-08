 package fr.aumgn.tobenamed.stage;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.tobenamed.game.Game;
 import fr.aumgn.tobenamed.game.Team;
 import fr.aumgn.tobenamed.util.TBNUtil;
 import fr.aumgn.tobenamed.util.Vector;
 import fr.aumgn.tobenamed.util.Vector2D;
 
 public class TotemStage extends PositioningStage {
 
     public TotemStage(Game game) {
         super(game);
     }
 
     @Override
     public void start() {
         List<Team> teams = game.getTeams();
         Vector spawnPos = game.getSpawn().getMiddle(); 
         Iterator<Vector> positions = game.getSpawn().
                getDirections(teams.size()).iterator();
         for (Team team : teams) {
             Vector pos = positions.next();
             Vector2D dir = pos.subtract(spawnPos).to2D();
             initTeam(team, game.getWorld(), pos, dir);
         }
 
         super.start();
         game.getSpawn().create(game.getWorld());
         game.getWorld().setTime(0);
         game.sendMessage(ChatColor.GREEN + "La partie débute !");
         scheduleNextStage(600);
     }
 
     @Override
     protected Stage nextStage() {
         return new SpawnStage(game);
     }
 
     private void initTeam(Team team, World world, Vector pos, Vector2D dir) {
         Player foreman = TBNUtil.pickRandom(team.getPlayers());
         team.setForeman(foreman);
         team.sendMessage(ChatColor.GREEN + foreman.getDisplayName() + " est le chef d'equipe.");
 
         for (Player player : team.getPlayers()) {
             player.setGameMode(GameMode.SURVIVAL);
             player.setHealth(20);
             player.setFoodLevel(20);
             player.getInventory().clear();
         }
 
         foreman.teleport(pos.toLocation(world, dir));
     }
 
     @Override
     public void initPosition(Team team, Vector pos) {
         team.setTotem(pos, game.getWorld().getMaxHeight());
         team.getTotem().create(game.getWorld());
     }
 
     @Override
     public Material getMaterial() {
         return Material.OBSIDIAN;
     }
 }
