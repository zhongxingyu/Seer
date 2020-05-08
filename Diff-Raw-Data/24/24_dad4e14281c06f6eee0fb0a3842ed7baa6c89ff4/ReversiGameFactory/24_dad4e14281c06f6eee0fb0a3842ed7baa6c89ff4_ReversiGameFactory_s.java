 package reversi.server;
 
 import java.util.List;
 
import base.models.Player;
 import reversi.game.ReversiGame;
 import reversi.game.controller.ReversiControllerSet;
 import reversi.game.controller.ReversiPlayerController;
 import reversi.models.ReversiBoard;
 import reversi.models.ReversiPlayer;
import reversi.server.display.ReversiAsciiDisplayController;
 import reversi.server.models.ReversiSettings;
 import reversi.server.models.ReversiSettings.GameMode;
 
 public class ReversiGameFactory {
 
 	public ReversiGame createNewGame(ReversiSettings settings) {
 
 		ReversiControllerSet controllerSet = ReversiControllerSet.controllerSetWithSettings(settings);
 		ReversiPlayerController playersController = controllerSet.getPlayerController();
 		this.finalizePlayers(playersController, settings);
 
 		ReversiGame newGame = new ReversiGame(controllerSet);
 		return newGame;
 	}
 
 	private void finalizePlayers(ReversiPlayerController playersController, ReversiSettings settings) {
 
 		List<ReversiPlayer> players = playersController.getAllPlayers();
 		ReversiPlayer host = players.get(0);
 		host.setControlledByAi(false);
 
 		GameMode mode = settings.getGameMode();
 
 		switch (mode) {
 		case AIAILocal: {
 
 			ReversiPlayer aiPlayer = new ReversiPlayer("AI-1", ReversiBoard.whiteReversiPiece);
 			ReversiPlayer aiSecondaryPlayer = new ReversiPlayer("AI-2", ReversiBoard.blackReversiPiece);
 
 			players.add(aiPlayer);
 			players.add(aiSecondaryPlayer);
 			host.setPlaying(false);

 		}
 			break;
 		case AIAIRemote: {
 
 			ReversiPlayer aiPlayer = new ReversiPlayer("AI-1", host.getAsciiDisplayPiece());
 			players.add(aiPlayer);
 			host.setPlaying(false);
 
 		}
 			break;
 		case HumanAi: {
 
 			ReversiPlayer aiPlayer = new ReversiPlayer("AI-1", "?");
 			players.add(aiPlayer);
 			host.setPlaying(true);
 
 		}
 			break;
 		}
 	}
 
 }
