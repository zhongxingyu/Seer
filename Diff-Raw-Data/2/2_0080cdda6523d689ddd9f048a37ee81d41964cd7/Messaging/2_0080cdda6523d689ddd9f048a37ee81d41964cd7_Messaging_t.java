 package edu.cmu.ri.mrpl.comm;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.cmu.ri.mrpl.CommClient;
 import edu.cmu.ri.mrpl.CommClient.CommException;
 import edu.cmu.ri.mrpl.maze.MazeState;
 import edu.cmu.ri.mrpl.maze.MazeWorld.Direction;
 
 public class Messaging {
 	CommClient cc;
 	String friend;
 	
 	public Messaging (CommClient cc, String friend) {
 		this.cc = cc;
 		this.friend = friend;
 	}
 	
 	public static final String DELIM = ",";
 	
 	public enum Action {
 		REMOVE_GOLD, REMOVE_DROP, GO, ADD_WALL, REMOVE_WALL, TAKE_CHARGE
 	}
 	
 	public void sendAction (Action a, MazeState s) {
 		cc.send(friend, a.ordinal() + "");
 		System.out.printf("sending %s to %s \n", a.toString(), friend);
		if (a != Action.GO && a != Action.TAKE_CHARGE) {
 			sendMazeState(s);
 			System.out.println(s);
 		}
 	}
 	
 	public static String join (Object[] objs) {
 		StringBuilder sb = new StringBuilder();
 		for (Object o : objs) {
 			sb.append(o);
 			sb.append(DELIM);
 		}
 		return sb.toString();
 	}
 	
 	public static String serializeMazeState (MazeState state) {
 		return join(new Object[]{state.x(), state.y(), state.dir().ordinal()});
 	}
 	
 	public static MazeState parseMazeState (String message) {
 		String[] fields = message.split(DELIM);
 		
 		int x = Integer.parseInt(fields[0]);
 		int y = Integer.parseInt(fields[1]);
 		int dirOrd = Integer.parseInt(fields[2]);
 		Direction dir = Direction.values()[dirOrd];
 		
 		return new MazeState(x, y, dir);
 	}
 	
 	public void sendMazeState (MazeState state) {
 		String message = serializeMazeState(state);
 		cc.send(friend, message);
 	}
 	
 	public MazeState receiveMazeState () throws CommException {
 		String message = null;
 		message = singleString();
 		return parseMazeState(message);
 	}
 	
 	public static List<String> serializeWaypoints (List<MazeState> waypoints) {
 		List<String> messages = new ArrayList<String>(waypoints.size()+1);
 		messages.add(waypoints.size() + "");
 		for (MazeState s : waypoints) {
 			messages.add(serializeMazeState(s));
 		}
 		return messages;
 	}
 	
 	public static List<MazeState> parseWaypoints (List<String> messages) {
 		List<MazeState> waypoints = new ArrayList<MazeState>(messages.size()-1);
 		for (int i = 0; i < messages.size(); i++) {
 			waypoints.add(parseMazeState(messages.get(i)));
 		}
 		return waypoints;
 	}
 	
 	public void sendWaypoints (List<MazeState> waypoints) {
 		for (String message : serializeWaypoints(waypoints)) {
 			cc.send(friend, message);
 			System.out.println("sending to "+friend+" message: "+message);
 		}
 	}
 	
 	private String singleString()
 	{
 		String tmp = null;
 		do{
 			try {
 				tmp = cc.getIncomingMessage();
 			} catch (CommException e) {
 				e.printStackTrace();
 				break;
 			}
 		}while(tmp == null);
 		
 		return tmp;
 	}
 	
 	public List<MazeState> receiveWaypoints () throws NumberFormatException, CommException {
 		int size = Integer.parseInt(singleString());
 		System.out.println("received int size: "+size);
 		List<String> messages = new ArrayList<String>(size+1);
 		String tmp = null;
 		while (size-- > 0) {
 			tmp = singleString();
 			System.out.println("received message: "+tmp);
 			messages.add(tmp);
 		}
 		return parseWaypoints(messages);
 	}
 	
 	public static void main (String... args) {
 		List<MazeState> waypoints = new ArrayList<MazeState>(2);
 		waypoints.add(new MazeState(1,2,Direction.South));
 		waypoints.add(new MazeState(3,2,Direction.North));
 		
 		List<String> messages = serializeWaypoints(waypoints);		
 		List<MazeState> parsed = parseWaypoints(messages);
 		
 		for (MazeState s : parsed) {
 			System.out.println(s);
 		}
 	}
 }
