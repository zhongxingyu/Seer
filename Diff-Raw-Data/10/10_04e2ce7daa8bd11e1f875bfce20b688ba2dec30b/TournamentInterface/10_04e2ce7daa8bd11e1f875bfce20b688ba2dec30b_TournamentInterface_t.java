 
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import players.BasePlayer;
 import players.ai.NoviceAI;
 import players.ai.RandomAI;
 import players.ai.minmax.MinMaxAI;
 import board.BoardState;
 import board.Move;
 import board.Piece;
 
 
 
 
 
 public class TournamentInterface {
 	private BasePlayer selectedAI;
 	private String playerNumber;
 	private int iWin = 0;
 	private int youWin = 0;
 	private int draws = 0;
 	private BoardState board = new BoardState();
 	//private Piece myPiece;
 	private BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
 	private Socket clientSocket;
 	private DataOutputStream outToServer; 
 	private BufferedReader inFromServer;
 	private OutputStream outputStream;
 	private String backUpString;
 	private static BufferedReader br;
 	private static BasePlayer player;
 	private boolean newRound = false;
 	Random r = new Random(System.currentTimeMillis());
 	
 	//testing new writer
 	PrintWriter printWriter;
 	BufferedWriter writer;
 	
 	
 	
 	
 
 	public TournamentInterface(BasePlayer player) {
 		setAI(player);
 		
 		try {
 			startTest();
 		} catch (UnknownHostException e) {
             System.err.println("Don't know about host: GameHost.");
             System.exit(1);
         } catch (IOException e) {
             System.err.println("Couldn't get I/O for "
                                + "the connection to: GameHost.");
             System.exit(1);
         }
 		//e1
 		//System.out.println("Constructor");	
 	}
 
 	
 	
 	
 	public void run() throws IOException{
 		//e2
 		
 		String innCom;
 		while ((innCom = inFromServer.readLine()) != null){
 			//System.out.println(innCom);
 			serverCom(innCom);
 		}
 	}
 	
 	public void startTest() throws IOException{
 		//e3
 		//System.out.println("init.");
 	  inFromUser = new BufferedReader( new InputStreamReader(System.in));
 	  clientSocket = new Socket("127.0.0.1", 4455);   //new Socket("127.0.0.1", 4455);   
 	  inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
 	  outToServer = new DataOutputStream(clientSocket.getOutputStream());   
 	  
 	  //testing new writer.
 	  this.outputStream = clientSocket.getOutputStream();
 	  this.printWriter = new PrintWriter(outputStream);
 	  this.writer = new BufferedWriter(printWriter);
 	  //inFromUser.readLine();   
 	  //outToServer.writeBytes("");   
 	  //inFromServer.readLine();   
 	  
 	  
 
 	}
 	
 	public void setAI(BasePlayer ai){
 		selectedAI = ai;
 	}
 	
 	private void setPlayerNumber(String s){
 		String[] tempString;
 		tempString = s.split(" ");
 		playerNumber = tempString[1];
 		System.out.println(playerNumber);
 	}
 	
 	
 	
 	/*
 	 * Player ['one'/'two']
 	 * BoardUpdate [piece] [row (0-indexed)] [column (0-indexed)]
 	 * Turn [piece chosen by opponent]
 	 * Move [placed piece at row (0-indexed)] [placed piece at column (0-indexed)] [piece chosen for opponent]
 	 * Round [round number, starts at 1] [startingPlayer ('one' or 'two')]
 	 * Winner ['one' or 'two']
 	 */
 	
 	private void serverCom(String inString) throws IOException{
 		//System.out.println("ServerCom Method:" + inString);
 		switch (inString.charAt(0)) {
 			case 'P':
 				setPlayerNumber(inString);
 				break;
 			case 'B':
 				updateBoard(inString);
 				break;
 			case 'T':
 				  backUpString = inString;
 				  if(newRound){
 					  String sMove = "Move 0 0 " +returnJustPiece() ;
 					  writeMessage(sMove);
 					  newRound = false;
 				  } else {
 					  String testy2 = returnMove(inString);
 					  //System.out.println("Sending move to server: " + testy2);
 					  //server doesnt reciece this one.
 					  //outToServer.writeBytes(testy2);    
 					  writeMessage(testy2);
 				  }
 				break;
 			case 'I':
 				writeMessage(returnMove(backUpString));
 				break;
 			case 'R':
 				String[] tempString;
 				tempString = inString.split(" ");
 				if(tempString[2].equals(playerNumber))
 					newRound = true;
 				
 				board = new BoardState();
 				
 				break;
 			case 'W':
 				updateWinners(inString);
 				System.out.println("Our success : " + Math.round((double) 100 *iWin) /(iWin+youWin+draws) + " %");
 				break;
 			case 'G':
 				//printresults or saveresults
 				//disconnect the socket connection and shut down. 
 				System.out.println("\n\nOur AI won "+iWin+" times");
 				System.out.println("Their AI won "+youWin+" times");
 				System.out.println("We drew "+draws+" times");
 				inFromUser.close();
 				clientSocket.close();
    			    outToServer.close();
 				inFromServer.close();
 				
 			default:
 				//some exceptions probably
 				break;
 		}
 	}
 	
 	public void writeMessage(String s) throws IOException{	
 		writer.write(s);
 		writer.newLine();
 		writer.flush();
 	}
 	
 	//this needs some work
 	private void updateWinners(String s){
 		String[] tempString;
 		tempString = s.split(" ");
 		if (tempString[1].equals(playerNumber)){
 			iWin++;
 		} else if (tempString[1].charAt(0)=='D' || tempString[1].charAt(0)=='d'){
 			draws++;
 		} else
 			youWin++;
 		
 		
 	}
 	
 	private void updateBoard(String boardUpdate){
 		//BoardUpdate [piece] [row (0-indexed)] [column (0-indexed)]
 		String[] tempString;
 		tempString = boardUpdate.split(" ");
 		//Change this if we remove BoardUpdate:
 		board.forceUsePiece(new Piece(tempString[1]), Integer.parseInt(tempString[3]), Integer.parseInt(tempString[2]));
 		
 	}
 	
 	private Move generateMove(String s){
 		Piece myPiece = board.getPiece(s);
 		board.forceRemovePiece(myPiece);
 		Move move = selectedAI.getNextMove(this.board.deepCopy(), myPiece);
 		
 		return move;
 	}
 	
 	private String returnJustPiece(){
 		ArrayList<Piece> remaining = board.getRemainingPieces();
 		Piece place = remaining.get(r.nextInt(remaining.size()));
 		return place.getName();
 	}
 	
 	//When a client has to do a turn, it receives:
 	//Turn [piece chosen by opponent]
 	private String returnMove(String inData){
 		String[] tempString;
 		tempString = inData.split(" ");
 		//ok until here.
 		String testy = moveToString(generateMove(tempString[1]));
 		//System.out.println(testy);
 		return testy;
 	}
 	
 	//Move [placed piece at row (0-indexed)] [placed piece at column (0-indexed)] [piece chosen for opponent]	
 	private String moveToString(Move move){
 		
		String sMove;
		
		if (move.getPieceToGiveOpponent()==null){
			sMove = "Move "+Integer.toString(move.getY())+" "+Integer.toString(move.getX())+
					" null";
		} else {
		sMove = "Move "+Integer.toString(move.getY())+" "+Integer.toString(move.getX())+
 				" "+ move.getPieceToGiveOpponent().getName();
		}
 		/*
 		sMove.concat("Move ");
 		sMove.concat(Integer.toString(move.getY()));
 		sMove.concat(" ");
 		sMove.concat(Integer.toString(move.getX()));
 		sMove.concat(" ");
 		sMove.concat(move.getPieceToGiveOpponent().toString());
 		*/
 		return sMove;
 	}
 	
 	private String getPlayer(){
 		return this.selectedAI.getName();
 	}
 	
 	public static BasePlayer findPlayer(String in){
 		
 
 		int temp = Integer.parseInt(in);
 		switch (temp){
 		case 1:
 			return new RandomAI(false);
 		case 2:
 			return new NoviceAI(false);
 		case 3:
 			System.out.println("Depth for search: 1-5");
 			System.out.println("Enter depth:");
 			String sIn;
 			try {	
 				sIn = br.readLine();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				sIn = "";
 			}
 			
 			//need exception handling
 			return new MinMaxAI(false,Integer.parseInt(sIn));
 			
 		default:
 			return new NoviceAI(false);
 		}
 	}
 
 	public static void main(String[] args)
     {
 		
 		br = new BufferedReader(new InputStreamReader(System.in));
 		
 		GM.printTournamentMenu();
 		String temp;
 		try {	
 			temp = br.readLine();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			temp = "3";
 		}
 		player = findPlayer(temp);
 		
 		
 		TournamentInterface t = new TournamentInterface(player);
 		System.out.println(t.getPlayer());
 		try {
 			t.run();
 		} catch (IOException e) {
 			System.exit(0);
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 	
 	
 	
 	
 }
