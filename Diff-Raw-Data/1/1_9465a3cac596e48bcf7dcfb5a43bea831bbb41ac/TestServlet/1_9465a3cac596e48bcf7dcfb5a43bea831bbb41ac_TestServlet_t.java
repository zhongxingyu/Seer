 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.Scanner;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.neumont.learningChess.api.ChessGameState;
 import edu.neumont.learningChess.api.MoveHistory;
 import edu.neumont.learningChess.engine.GameStateInfo;
 import edu.neumont.learningChess.engine.LearningEngine;
 import edu.neumont.learningChess.json.Jsonizer;
 import edu.neumont.learningChess.model.Move;
 
 /**
  * Servlet implementation class Test
  */
 public class TestServlet extends HttpServlet {
 
 	private static final String LEARNING_ENGINE = "LearningEngine";
 	private static final long serialVersionUID = 1L;
 
 	private enum Paths {
 		analyzehistory, getmove, /*
 								 * postdatatofacebook, getusestats,
 								 * gettopscores,
 								 */ping, getGameStateInfo, noValue;
 
 		public static Paths toPath(String path) {
 			try {
 				return valueOf(path);
 			} catch (Exception e) {
 				return noValue;
 			}
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String realPath = getServletContext().getRealPath("index.jsp");
 		Scanner fileScanner = new Scanner(new FileInputStream(new File(realPath)));
 		StringBuilder stringBuilder = new StringBuilder();
 		while(fileScanner.hasNextLine()) {
 			stringBuilder.append(fileScanner.nextLine()+"\r\n");
 		}
 		response.getWriter().println(stringBuilder.toString());
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// need to get the url our of the request
 		StringBuffer requestURLBuffer = request.getRequestURL();
 		String requestURL = requestURLBuffer.toString();
 		String urlString = requestURL.substring(requestURL.lastIndexOf("/")+1);
 		ServletContext context = request.getSession().getServletContext();
 		String responseString = null;
 		context.log("Method:" + urlString);
		context.log("Web-Inf Path:" + context.getRealPath("/WEB-INF/"));
 		try {
 			// switch on the url
 			switch (Paths.toPath(urlString)) {
 				case getmove :
 					responseString = getMoveFromLearningCenter(context, request);
 					break;
 
 				case analyzehistory :
 					analyzeGameHistory(context, request);
 					responseString = "";
 					break;
 					
 				case getGameStateInfo:
 					responseString = getGameStateInfo(context, request);
 					break;
 
 				case ping :
 					responseString = "you have pinged the server's servlet";
 					break;
 
 				case noValue :
 					responseString = "Unrecognized Action";
 					break;
 
 				default :
 					responseString = "Unrecognized Action";
 					break;
 			}
 		} catch (Exception e) {
 			ByteArrayOutputStream bos = new ByteArrayOutputStream();
 			PrintStream printStream = new PrintStream(bos);
 			e.printStackTrace(printStream);
 			context.log(bos.toString());
 		}
 
 		context.log("ResponseString: " + responseString);
 		context.log("Server Activity: Finished Method");
 
 		PrintWriter writer = response.getWriter();
 		writer.println(responseString);
 		writer.flush();
 
 	}
 
 	private String getGameStateInfo(ServletContext context, HttpServletRequest request) throws IOException {
 		String jsonString = getPostBody(request.getReader());
 		ChessGameState chessGameState = Jsonizer.dejsonize(jsonString, ChessGameState.class);
 		LearningEngine learningEngine = (LearningEngine) context.getAttribute(LEARNING_ENGINE);
 		
 		GameStateInfo gameStateInfo = learningEngine.getGameStateInfo(chessGameState);
 		
 		return Jsonizer.jsonize(gameStateInfo);
 	}
 
 	private void analyzeGameHistory(ServletContext context, HttpServletRequest request) throws IOException {
 		String jsonString = getPostBody(request.getReader());
 		MoveHistory gameStateHistory = Jsonizer.dejsonize(jsonString, MoveHistory.class);
 		
 		LearningEngine learningEngine = (LearningEngine) context.getAttribute(LEARNING_ENGINE);
 		learningEngine.analyzeGameHistory(gameStateHistory);
 	}
 
 	private String getMoveFromLearningCenter(ServletContext context, HttpServletRequest request) throws IOException {
 		String jsonString = getPostBody(request.getReader());
 
 		context.log("getmove request: " + jsonString);
 		ChessGameState gameState = Jsonizer.dejsonize(jsonString, ChessGameState.class);
 		context.log("state created");
 		context.log("got game state");
 		LearningEngine learningEngine = (LearningEngine) context.getAttribute(LEARNING_ENGINE);
 		context.log("got engine");
 		Move gameMove = learningEngine.getMove(gameState);
 		context.log("got move");
 
 		return Jsonizer.jsonize(gameMove);
 	}
 
 	private String getPostBody(BufferedReader reader) throws IOException {
 
 		StringBuilder builder = new StringBuilder();
 		for (;;) {
 			String line = reader.readLine();
 			if (line == null)
 				break;
 			builder.append(line);
 		}
 		return builder.toString();
 	}
 
 }
