 package org.github.craftfortress2;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
public class CFStart extends CFCommandExecutor {
	public CFStart(CraftFortress2 cf2) {
		super(cf2);
	}
	static Player[] players = (Player[]) CFCommandExecutor.players.toArray();
 		public static void startGame(){
 			Player player = null;
 			Server server = player.getServer();
 			server.broadcastMessage("Craft Fortress 2 is starting!");
			CFCommandExecutor.getTeam(players[0]);
 		}
 }
