 package javachallenge.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import javachallenge.common.Action;
 import javachallenge.common.ClientMessage;
 import javachallenge.common.InitMessage;
 import javachallenge.common.Point;
 import javachallenge.common.ServerMessage;
 import javachallenge.graphics.GraphicClient;
 import javachallenge.graphics.GraphicClient.OutOfMapException;
 import javachallenge.graphics.util.Position;
 
 public class Main {
 	private static int PORT = 5555;
 	private static int CYCLE_TIME = 500;
 	private GraphicClient graphicClient;
 	
 	public void run() throws IOException, InterruptedException, OutOfMapException {
 		ServerSocket ss = new ServerSocket(PORT);
 		ServerMap sampleMap = ServerMap.load("map/m1.txt");
 		
 		Position[] tmpFlagPositions = new Position[sampleMap.getFlagLocations().size()];
 		for(int i = 0 ; i < sampleMap.getFlagLocations().size() ; i++) {
 			Point flag = sampleMap.getFlagLocations().get(i);
 			tmpFlagPositions[i] = new Position(flag.x, flag.y);
 		}
 		
 		graphicClient = new GraphicClient(sampleMap);
 		Engine engine = new Engine(sampleMap, graphicClient);
 		
 		ArrayList<TeamConnection> connections = new ArrayList<TeamConnection>();
 		
 		for (int i = 0; i < sampleMap.getTeamCount(); i++) {
 			System.out.println("Waiting for team " + i);
 			Socket socket = ss.accept();
 			connections.add(new TeamConnection(engine.getTeam(i), socket));
 		}
 
 		ArrayList<InitMessage> initialMessage = engine.getInitialMessage();
 		for (TeamConnection c: connections) {
 			c.sendInitialMessage(initialMessage.get(c.getTeam().getId()));
 		}
 		
 		engine.beginStep();
 		engine.teamStep(new ArrayList<Action>());
 		engine.endStep();
 		
 		
 		while (!engine.gameIsOver()) {
			System.out.println("Cylce filan " + engine.getCycle());
 			
 			ArrayList<ServerMessage> stepMessage = engine.getStepMessage();
 			for (int i = 0; i < sampleMap.getTeamCount(); i++) {
 				connections.get(i).sendStepMessage(stepMessage.get(i));
 				connections.get(i).clearClientMessage();
 			}
 			
 			Thread.sleep(CYCLE_TIME);
 
 			ArrayList<Action> allActions = new ArrayList<Action>();
 			for (int i = 0; i < sampleMap.getTeamCount(); i++) {
 				ClientMessage msg = connections.get(i).getClientMessage();
 				if (msg == null) {
 					System.out.println("Team " + i + " message miss");
 				} else {
 					allActions.addAll(msg.getActions(i));
 				}
 			}
 			
 			engine.beginStep();
 			engine.teamStep(allActions);
 			engine.endStep();
 		}
 		
 		ss.close();
 	}
 	
 	public static void main(String[] args) throws IOException, InterruptedException, OutOfMapException {
 		new Main().run();
 	}
 }
