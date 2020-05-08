 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import edu.neumont.learningChess.api.ExtendedMove;
 import edu.neumont.learningChess.api.MoveHistory;
 import edu.neumont.learningChess.controller.GameController;
 import edu.neumont.learningChess.controller.ServerPlayer;
 import edu.neumont.learningChess.json.Jsonizer;
 import edu.neumont.learningChess.model.TextCommandProcessor;
 import edu.neumont.learningChess.model.TextCommandProcessorOutput;
 
 public class Main {
 
 	public static void main(String[] args) {
 		GameController.PlayerType white;
 		GameController.PlayerType black;
 		if ((args.length == 0) || (args[0].equalsIgnoreCase("white"))) {
 			white = GameController.PlayerType.Human;// human for check in
			black = GameController.PlayerType.Human;// human for check in
 		} else {
 			white = GameController.PlayerType.Human;
 			black = GameController.PlayerType.AI;
 		}
 		GameController game = new GameController(white, black);
 		game.play();
 		if (game.isCheckmate() || game.isStalemate())
 			tellTheServer(game.getGameHistory());
 		game.close();
 	}
 
 	private static void tellTheServer(Iterator<ExtendedMove> moveHistoryIterator) {
 		List<ExtendedMove> moves = new ArrayList<ExtendedMove>();
 		while (moveHistoryIterator.hasNext())
 			moves.add(moveHistoryIterator.next());
 
 		MoveHistory moveHistory = new MoveHistory(moves);
 		String endpoint;
 		if (ServerPlayer.IS_LOCAL) {
 			endpoint = "http://localhost:8080/LearningChessWebServer/analyzehistory";
 		} else {
 			endpoint = "http://chess.neumont.edu:8081/ChessGame/analyzehistory";
 		}
 
 		try {
 			URL url = new URL(endpoint);
 			URLConnection connection = url.openConnection();
 			connection.setDoOutput(true);
 			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
 			String jsonOut = Jsonizer.jsonize(moveHistory);
 			writer.write(jsonOut);
 			writer.flush();
 			int lengthFromClient = moveHistory.getMoves().size();
 			InputStreamReader in = new InputStreamReader(connection.getInputStream());
 			StringBuilder jsonStringBuilder = new StringBuilder();
 			int bytesRead;
 			while ((bytesRead = in.read()) > -1) {
 				if ((char) bytesRead != '\n' && (char) bytesRead != '\r')
 					jsonStringBuilder.append((char) bytesRead);
 			}
 			int lengthFromServer = 0;
 			try {
 				String jsonString = jsonStringBuilder.toString();
 				lengthFromServer = Integer.parseInt(jsonString);
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			}
 			if (lengthFromClient != lengthFromServer)
 				throw new RuntimeException("Lengths are different!");
 			else
 				System.out.println("Lengths are the same");
 
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public static void old_main(String[] args) {
 
 		TextCommandProcessorOutput output = new TextCommandProcessorOutput(System.out);
 		TextCommandProcessor processor = new TextCommandProcessor();
 		try {
 			processor.processCommands(System.in, output);
 		} catch (Throwable e) {
 			System.out.println(e.getMessage());
 		}
 	}
 }
