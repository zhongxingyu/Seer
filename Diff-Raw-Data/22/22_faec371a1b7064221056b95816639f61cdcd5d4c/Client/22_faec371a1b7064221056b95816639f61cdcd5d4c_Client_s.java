 package my;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 public class Client implements Player {
 	private static final int WAITING = States.WAITING;
 	private static final int MOVING = States.MOVING;
 	private static final int SENDMOVE = States.SENDMOVE;
 	private static final int START = States.START;
 	private static final int GAMEOVER = States.GAMEOVER;
 
 	int portNumber, descriptor;
 	String hostName, playerName, oppName, outputLine;
 
 	GameField gameField;
 	Cell lastMove;
 
 	Socket socket = null;
 	PrintWriter out = null;
 	BufferedReader in = null;
 	Parser parser = new Parser();
 	Serializer serializer = new Serializer();
 	private int state = START;
 	private boolean ready = false;
 
 	public Client(int pNumber, String hName, String pName) {
 		this.portNumber = pNumber;
 		this.hostName = hName;
 		this.playerName = pName;
 
 		runConnection();
 		getStartData(in);
 		sendPName();
 		if (parser.parseNewLine(in).contains("Start")) {
 			state = WAITING;
 			ready = true;
 		}
 	}
 
 	private void sendPName() {
 		outputLine = serializer.serializeName(playerName);
 		out.println(outputLine);
 	}
 
 	private void getStartData(BufferedReader in) {
 		oppName = parser.parseName(in);
 		descriptor = parser.parseColor(in);
 		System.out.println("Client descriptor: " + descriptor);
 	}
 
 	private void runConnection() {
 		try {
 			socket = new Socket(hostName, portNumber);
 			out = new PrintWriter(socket.getOutputStream(), true);
 			in = new BufferedReader(new InputStreamReader(
 					socket.getInputStream()));
 		} catch (UnknownHostException e) {
 			System.err.println("Don't know about host: " + hostName);
 			System.exit(1);
 		} catch (IOException e) {
 			System.err.println("Couldn't get I/O for the connection to: "
 					+ hostName);
 			System.exit(1);
 		}
 		System.out.println("Connection established");
 	}
 
 	// This method called from the game field for the remote player move
 	@Override
 	public void makeMove(GameField gameField) {
 		if (ready) {
 			this.gameField = gameField;
 			state = MOVING;
 			listenNewMove();
 		}
 	}
 
 	private void listenNewMove() {
 		String inputLine = null;
 		inputLine = parser.parseNewLine(in);
 		if (inputLine.equalsIgnoreCase("Game over"))
 			processGameOver();
 		else {
 			System.out.println("Move: " + inputLine);
 			Cell move = parser.parseMove(inputLine);
 			makeMove(move);
 		}
 	}
 
 	// this method returns remote player move to the game field.
 	private void makeMove(Cell move) {
 		int i = move.i_index;
 		int j = move.j_index;
 		gameField.makeMove(i, j);
 		System.out.println("flag " + gameField.movedSuccess);
 		if (gameField.movedSuccess) {
 			System.out.println("Client: moved success");
 			lastMove = gameField.allCells[i][j];
 			state = WAITING;
 		}
 	}
 
 	public void processGameOver() {
 		state = GAMEOVER;
 		ready = false;
 		try {
 			handleGOanswer();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void handleGOanswer() throws IOException {
 		String answer = parser.parseNewLine(in);
 		if (answer.contains(States.regameSuggest)) {
 			BufferedReader stdIn = new BufferedReader(new InputStreamReader(
 					System.in));
 			String fromUser = null;
 
 			System.out.println(answer);
 
 			fromUser = stdIn.readLine();
 
 			if (fromUser != null) {
 				stdIn.close();
 				if (fromUser.equalsIgnoreCase("y")) {
					newGame();
					out.println(serializer.serializeString(fromUser));
 				}
 
 				else {
 					out.println(serializer.serializeString("n"));
 					breakConnection();
 				}
 			}
 		}
 	}
 
 	private void breakConnection() throws IOException {
 		out.println("Bye.");
 		out.close();
 		in.close();
 		socket.close();
 	}
 
 	private void newGame() {
 		state = WAITING;
 		gameField.newRound();
 		waitForServer();
 	}
 
 	private void waitForServer() {
 		if (parser.parseNewLine(in).contains("Start")) {
 			state = WAITING;
 			ready = true;
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
 
 	public int getColor() {
 		return descriptor;
 	}
 
 	@Override
 	public void passAction() {
 		state = WAITING;
 	}
 
 }
