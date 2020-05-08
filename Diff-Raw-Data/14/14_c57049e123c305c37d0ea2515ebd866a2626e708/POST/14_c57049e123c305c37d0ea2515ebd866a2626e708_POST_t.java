 
 package Server.Method;
 
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.net.Socket;
 import java.util.StringTokenizer;
 
 import org.apache.log4j.Logger;
 
 import Server.Response;
 import Server.Method.POSTActions.Analyze;
 import Server.Method.POSTActions.GetMethods;
 import Server.Method.POSTActions.Save;
 
 
 
 
 
 
 public class POST
 {
 
 	private static Logger logger = Logger.getLogger(POST.class.getName());
 
 	private String currentVersion = "2.4";
 
 	private String action;
 
 	String extractionMethod = null;
 
 	private String incomingLine;
 
 	private String authorization;
 
 	private DataOutputStream outgoing;
 
 	private BufferedReader incoming;
 
 	private Socket connection = null;
 
 
 
 
 
 
 	public POST(DataOutputStream outgoing, BufferedReader incoming, Socket connection)
 	{
 
 		this.outgoing = outgoing;
 		this.incoming = incoming;
 		this.connection = connection;
 
 		logger.info("Client send POST request:");
 		readHeaders();
 		makeAction();
 	}
 
 
 
 
 
 
 	@SuppressWarnings("unused")
 	private void readHeaders() {
 
 		try {
 
 			String line = incoming.readLine();
 			String incomingLine = line;
 			while (line.length() > 0) {
 				if (line.contains("Authorization: ")) {
 					StringTokenizer tokenizer = new StringTokenizer(line);
 					String httpMethod = tokenizer.nextToken();
 					authorization = tokenizer.nextToken();
 					authorization = tokenizer.nextToken();
 					if (!authorization.equals(currentVersion)) {
 						logger.info(incomingLine);
 						new Response(outgoing).sendResponse(400, "Error: New version available - http://events.email.ui.sav.sk \r\n");
 						throw (new Exception("Plugin has old version."));
 					}
 				}
 
 				if (line.contains("ExtractionMethod: ")) {
 					StringTokenizer tokenizer = new StringTokenizer(line);
 					String httpMethod = tokenizer.nextToken();
 					extractionMethod = tokenizer.nextToken();
 				}
 
 				if (line.contains("Action: ")) {
 					StringTokenizer tokenizer = new StringTokenizer(line);
 					String httpMethod = tokenizer.nextToken();
 					action = tokenizer.nextToken();
 				}
 
 				line = incoming.readLine();
 				incomingLine = incomingLine.concat("\n" + line);
 			}
 		} catch (Exception e) {
			new Response(outgoing).sendResponse(404, e.toString());
 			logger.fatal(e);
			
 		}
 	}
 
 
 
 
 
 
 	private void makeAction() {
 
 		try {
 			if (action.contains("ANALYZE")) {
 				logger.info(incomingLine);
 
 				Analyze analyze = new Analyze(outgoing, incoming, connection);
 				analyze.prepareExtractionMethod(extractionMethod, authorization);
 				analyze.analyzeMessage();
 				analyze.saveDataToDatabase();
 				analyze.makeJsonObject();
 				analyze.sendJsonObject();
 			}
 
 			if (action.contains("SAVE")) {
 				logger.info(incomingLine);
 				Save save = new Save(outgoing, incoming);
 				save.saveToDatabase();
 			}
 
 			if (action.contains("GET_METHODS")) {
 				logger.info(incomingLine);
 				GetMethods methods = new GetMethods(outgoing, incoming);
 				methods.getMethods();
 				methods.makeJsonObject();
 				methods.sendJsonObject();
 			}
 
 		} catch (Exception e) {
			new Response(outgoing).sendResponse(404, e.toString());
 			logger.fatal(e);
			
 		}
 	}
 }
