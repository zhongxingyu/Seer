 package org.github.craftfortress2;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World;
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
 			Server svr = player.getServer();
 			World dustbowl = svr.getWorld("Dustbowl");
 			Location blue = new Location(dustbowl, 1, 1, 1);
 			Location red = new Location(dustbowl, 2, 2, 2);
 			if(CFCommandExecutor.getTeam(players[0]) == "blue"){
 				players[0].teleport(blue);
 			}else{
 				players[0].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[1]) == "blue"){
 				players[1].teleport(blue);
 			}else{
 				players[1].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[2]) == "blue"){
 				players[2].teleport(blue);
 			}else{
 				players[2].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[3]) == "blue"){
 				players[3].teleport(blue);
 			}else{
 				players[3].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[4]) == "blue"){
 				players[4].teleport(blue);
 			}else{
 				players[4].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[5]) == "blue"){
 				players[5].teleport(blue);
 			}else{
 				players[5].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[6]) == "blue"){
 				players[6].teleport(blue);
 			}else{
 				players[6].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[7]) == "blue"){
 				players[7].teleport(blue);
 			}else{
 				players[7].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[8]) == "blue"){
 				players[8].teleport(blue);
 			}else{
 				players[8].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[9]) == "blue"){
 				players[9].teleport(blue);
 			}else{
 				players[9].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[10]) == "blue"){
 				players[10].teleport(blue);
 			}else{
 				players[10].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[11]) == "blue"){
 				players[11].teleport(blue);
 			}else{
 				players[11].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[12]) == "blue"){
 				players[12].teleport(blue);
 			}else{
 				players[12].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[13]) == "blue"){
 				players[13].teleport(blue);
 			}else{
 				players[13].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[14]) == "blue"){
 				players[14].teleport(blue);
 			}else{
 				players[14].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[15]) == "blue"){
 				players[15].teleport(blue);
 			}else{
 				players[15].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[16]) == "blue"){
 				players[16].teleport(blue);
 			}else{
 				players[16].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[17]) == "blue"){
 				players[17].teleport(blue);
 			}else{
 				players[17].teleport(red);
 			}
			if(CFCommandExecutor.getTeam(players[18]) == "blue"){
 				players[18].teleport(blue);
 			}else{
 				players[18].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[19]) == "blue"){
 				players[19].teleport(blue);
 			}else{
 				players[19].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[20]) == "blue"){
 				players[20].teleport(blue);
 			}else{
 				players[20].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[21]) == "blue"){
 				players[21].teleport(blue);
 			}else{
 				players[21].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[22]) == "blue"){
 				players[22].teleport(blue);
 			}else{
 				players[22].teleport(red);
 			}
 			if(CFCommandExecutor.getTeam(players[23]) == "blue"){
 				players[23].teleport(blue);
 			}else{
 				players[23].teleport(red);
 			}
 		}
 }
