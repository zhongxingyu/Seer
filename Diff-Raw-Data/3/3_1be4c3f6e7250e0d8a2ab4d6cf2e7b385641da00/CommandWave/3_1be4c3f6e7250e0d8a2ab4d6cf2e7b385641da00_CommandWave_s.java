 package game.console.command;
 
import game.Game;nWaveMode;
 
 public class CommandWave extends Command {
 
 	public CommandWave(Game game) {
 		super(game);
 	}
 
 	@Override
 	public void executeCommand(String[] args) {
 		if (game.currentScreen instanceof ScreenWaveMode) {
 			if (args[1].contains("set")) {
 				int newwave = Integer.parseInt(args[2]);
 				((ScreenWaveMode) game.currentScreen).wave = newwave;
 				Game.log("Done");
 				return;
 			}
 			if (args[1].contains("notice")) {
 				int newvalue = Integer.parseInt(args[2]);
 				((ScreenWaveMode) game.currentScreen).noticetimer = newvalue;
 				Game.log("Done");
 				return;
 			}
 			
 			if (args[1].contains("path")) {
 				if (args[2].contains("reinit")) {
 					((ScreenWaveMode) game.currentScreen).map = null;
 					return;
 				}
 				return;
 			}
 			if (args[1].contains("deinit")) {
 				((ScreenWaveMode) game.currentScreen).deinit();
 				return;
 			}
 			
 			
 			Game.log("Invalid command");
 
 		}
 
 	}
 
 }
