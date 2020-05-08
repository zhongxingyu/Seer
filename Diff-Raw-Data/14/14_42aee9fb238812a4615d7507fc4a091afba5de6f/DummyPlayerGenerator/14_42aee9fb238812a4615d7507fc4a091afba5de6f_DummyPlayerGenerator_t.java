 package ch.creasystem.tennis.shared.player;
 
 import java.util.ArrayList;
 
 
 public class DummyPlayerGenerator {
 
 	public static ArrayList <Player> generatePlayers () {
 		ArrayList<Player> playerList = new ArrayList<Player>();
 		
 		
 		playerList.add(new Player("David","Gerber", "Kuba", "a@b.com", null));
 		playerList.add(new Player("Patrick", "Kocher", "Hakin", "a@b.com", null));
		playerList.add(new Player("Gaetan", "Bernier", "Amélie",  "a@b.com", null));
 		playerList.add(new Player("Juan","Lopez", "Aranxa",  "a@b.com", null));
 		return playerList;
 	}
 }
