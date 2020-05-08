 package bot;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import util.Logger;
 
 class InputOutputUnit{
 	
 	private BufferedReader input;
 	private int mapHeight = -1;
 	private int mapWidth = -1;
 	private Socket mySocket;
 	private PrintWriter output;
 	
 	public InputOutputUnit(){
 		input = new BufferedReader(new InputStreamReader(System.in));
 		output = new PrintWriter(System.out);
 	}
 	
 	public InputOutputUnit(String hostname)
 			throws UnknownHostException, IOException{
 		this(hostname, Protocole.DEFAULT_PORT);
 	}
 	
 	public InputOutputUnit(String hostname, int portNo)
 			throws UnknownHostException, IOException{
 		mySocket = new Socket(hostname, portNo);
 		input = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
 		output = new PrintWriter(mySocket.getOutputStream());
 	}
 	
 	public void parseNextTurn(Bot b) throws IOException, UnknownPositionException{
 		try{
 			// Verify start of message
 			String line = input.readLine();
 			if (!line.equals(Protocole.START_TOKEN))
 				throw new RuntimeException("Invalid start Token. Expected \"" +
 										   Protocole.START_TOKEN + "\" received \"" +
 										   line + "\"");
 			Logger.println("Parsing informations");
 			while (!(line = input.readLine()).equals(Protocole.END_TOKEN)){
 				Logger.println(line);
 				// Multi-line message
 				Information i = null;
 				if (line.startsWith(Protocole.START_TOKEN))
 					i = parseMultiLineVar(line);
 				// Mono-line message
 				else
 					i = parseMonoLineVar(line);
 				if (i != null)
 					b.treatInformation(i);
 			}
 		}catch(IOException e){
 			output.close();
 			input.close();
 			mySocket.close();
			throw e;
 		}
 		Logger.println("Informations Parsed");
 	}
 	
 	private Information parseMultiLineVar(String line) throws IOException, UnknownPositionException{
 		Variable v = Variable.tokenToVariables(line.split(" ")[1]);
 		Object o = null;
 		switch (v){
 		case MAP :
 				o = parseMap();
 				break;
 		default ://TODO should throw Exception
 		}
 		return new Information(v,o);
 	}
 
 	private Map parseMap() throws IOException,UnknownPositionException{
 		Logger.println("Parsing map");
 		String line;
 		char[][] map = new char[mapHeight][mapWidth];
 		int lineNumber = 0;
 		while (!(line = input.readLine()).equals(Protocole.END_TOKEN +
 												 " " +
 												 Variable.MAP.getToken())){
 			Logger.println("Read : " + line);
 			for (int colNumber = 0; colNumber < mapWidth; colNumber++){
 				if (colNumber >= line.length())
 					map[lineNumber][colNumber] = ' ';//TODO notation en dur à éviter
 				else
 					map[lineNumber][colNumber] = line.charAt(colNumber);
 			}
 			lineNumber++;
 		}
 		Map m = new Map(map);
 		Logger.println("Map parsed : result");
 		Logger.println(m.toString());	
 		return m;
 	}
 
 	private Information parseMonoLineVar(String line){
 		String[] splitedLine = line.split(" ");
 		Variable v = Variable.tokenToVariables(splitedLine[0]);
 		Object o = null;
 		switch (v){
 		case MAP_HEIGHT: mapHeight = Integer.parseInt(splitedLine[1]);
 		case MAP_WIDTH: mapWidth = Integer.parseInt(splitedLine[1]);
 		case DUNGEON_LEVEL:
 			o = Integer.parseInt(splitedLine[1]);
 			break;
 		default :
 		}
 		if (o == null)
 			return null;
 		return new Information(v,o);
 	}
 	
 	public void broadcastMove(Direction d){
 		String action = Protocole.MOVE_TOKEN + ' ' + d.getValue();
 		Logger.println("ACTION : " + action);
 		output.println(action);
 		output.flush();
 	}
 	
 	public void broadcastSearch(){
 		output.println(Protocole.SEARCH_TOKEN);
 		Logger.println("ACTION : " + Protocole.SEARCH_TOKEN);
 		output.flush();
 	}
 	
 	public void broadcastOpeningDoor(Direction d){
 		String action = Protocole.OPEN_TOKEN + ' ' + d.getValue();
 		Logger.println("ACTION : " + action);
 		output.println(action);
 		output.flush();
 	}
 }
