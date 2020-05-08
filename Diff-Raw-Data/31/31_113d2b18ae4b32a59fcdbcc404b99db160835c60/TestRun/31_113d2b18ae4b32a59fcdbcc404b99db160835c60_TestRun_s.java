 package apps;
 
 import java.io.IOException;
 
 import player.GamePlayer;
 import player.gamer.statemachine.eggplant.EggplantPrimaryGamer;
import player.gamer.statemachine.reflex.legal.LegalGamer;
 
 public class TestRun {
 	private static final int DEFAULT_GAME_PORT = 9147;
 	public static void main(String[] args) throws IOException {
 		GamePlayer player = new GamePlayer(DEFAULT_GAME_PORT, new EggplantPrimaryGamer());
		GamePlayer legal = new GamePlayer(DEFAULT_GAME_PORT+1, new LegalGamer());
 		player.start();
 		legal.start();
 		System.out.println("Started Players!");
 	}
 
 }
