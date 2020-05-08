 package my;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class Server implements Player {
 	private static final int WAITING = States.WAITING;
 	private static final int MOVING = States.MOVING;
 	private static final int SENDMOVE = States.SENDMOVE;
 	private static final int START = States.START;
 	private static final int GAMEOVER = States.GAMEOVER;
 	private int state = START;
 
 	int portNumber, descriptor;
 	PrintWriter out = null;
 	BufferedReader in = null;
 	Socket clientSocket = null;
 	ServerSocket serverSocket = null;
 	Parser parser = new Parser();
 	Serializer serializer = new Serializer();
 	Protocol protocol = new Protocol(parser, serializer);
 	String outputLine, playerName, oppName;
 	GameField gameField;
 	Cell lastMove;
 
 	public Server(int portNum, int descriptor, String playerName) {
 		this.portNumber = portNum;
 		this.descriptor = inverseDescriptor(descriptor);
 		this.playerName = playerName;
 
 		try {
 			serverSocket = new ServerSocket(portNumber);
 		} catch (IOException e) {
 			System.err.println("Could not listen on port: " + portNumber + ".");
 			System.exit(1);
 		}
 
 		try {
 			clientSocket = serverSocket.accept();
 		} catch (IOException e) {
 			System.err.println("Accept failed.");
 			System.exit(1);
 		}
 
 		try {
 			openInOut();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private int inverseDescriptor(int d) {
 		if (d == 0)
 			return 1;
 		else if (d == 1)
 			return 0;
 		else
 			return 3;
 	}
 
 	private void openInOut() throws IOException {
 		out = new PrintWriter(clientSocket.getOutputStream(), true);
 		in = new BufferedReader(new InputStreamReader(
 				clientSocket.getInputStream()));
 
 		sendStartData(); // send player name (this computer) and color of remote
 							// user.
 		getOppName(in); // get opponent name
 
 		/*
 		 * Server class is instance of Player class so in terms of game field
 		 * it's even player like "Human" or "Computer". Hence we can consider
 		 * Server like an imitation of Human class (actually remote player).
 		 * Because of this we just waiting for calling from game field.
 		 */
 
 		state = WAITING;
 	}
 
 	private void sendStartData() {
 		outputLine = serializer.serializeName(playerName);
 		out.println(outputLine);
 		outputLine = serializer.serializeColor(inverseDescriptor(descriptor));
 		out.println(outputLine);
 		protocol.setState(States.WAITforDATA);
 	}
 
 	private void getOppName(BufferedReader in) {
 		oppName = parser.parseName(in);
 		String outputLine = protocol.processOppName(oppName);
 		out.println(outputLine);
 	}
 
 	// This method called from the game field for the remote player move
 	@Override
 	public void makeMove(GameField gameField) {
 		this.gameField = gameField;
 		state = MOVING;
 		listenNewMove();
 	}
 
 	// this method returns remote player move to the game field.
 	private void makeMove(Cell move) {
 		int i = move.i_index;
 		int j = move.j_index;
 		gameField.makeMove(i, j);
 		if (gameField.movedSuccess) {
 			lastMove = gameField.allCells[i][j];
 			state = WAITING;
 		}		
 	}
 
 	private void listenNewMove() {
 		String inputLine = null;
 		inputLine = parser.parseNewLine(in);
 		if (inputLine.equalsIgnoreCase("Game over"))
 			processGameOver();
 		else {
 			Cell move = parser.parseMove(inputLine);
 			makeMove(move);
 		}
 	}
 
 	@Override
 	public int getState() {
 		return state;
 	}
 
 	@Override
 	public void stateChanged(GameField gameField, Cell cell) {
 		if (state == WAITING) {
 			if (cell != lastMove) {
 				outputLine = serializer.serializeMove(cell);
 				out.println(outputLine);
 				state = SENDMOVE;
 			}
 		}
 	}
 
 	// this method sends request to the opponent would he like start new game or
 	// not.
 	public void processGameOver() {
 		state = GAMEOVER;
 		outputLine = protocol.processGameOver();
 		out.println(outputLine);
 		handleGOanswer();
 	}
 
 	private void handleGOanswer() {
 		String answer = parser.parseNewLine(in);
 		System.out.println("From client: " + answer);
 		boolean newGame = protocol.processGOanswer(answer);
 		if (newGame)
 			newGame();
 		else
 			try {
 				System.out.println("Bye");
 				breakConnection();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	}
 
 	private void newGame() {
 		state = WAITING;
 		gameField.newRound();
 		outputLine = serializer.serializeString("Start");
 	}
 
 	private void breakConnection() throws IOException {
 		out.println("Bye.");
 		out.close();
 		in.close();
 		clientSocket.close();
 		serverSocket.close();
 	}
 
 	@Override
 	public void passAction() {
 		state = WAITING;
 	}
 
 }
