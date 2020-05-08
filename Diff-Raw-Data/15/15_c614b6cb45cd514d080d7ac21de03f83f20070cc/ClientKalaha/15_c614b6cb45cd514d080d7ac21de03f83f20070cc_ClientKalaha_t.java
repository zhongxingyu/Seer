 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 public class ClientKalaha
 {
 	private int player;
 	private String host;
 	private int port;
 	private boolean ai;
 	private AiNodeMaster bestBoard;
 	private int nodeDepth;
 	private int nodeCount;
 	private long millis;
 	private int maxCurrentDepth;
 	private boolean newDepth;
 
 		//sorts out commandline arguments and starts the application
 	public static void main(String args[]) throws Exception
 	{
 		boolean aiFlag = false;
 		if(args.length == 4)
 		{
 			if(args[2].toUpperCase().equals("AI") )
 				aiFlag = true;
 		}
 		else if(args.length != 2)
 		{
 			System.out.println("Syntax: ClientKalaha [host] [port] [ai] [depth]");
 			System.exit(0);
 		}
 		int myDepth = Integer.parseInt(args[3]);
 		ClientKalaha app = new ClientKalaha(args[0], Integer.parseInt(args[1]), aiFlag, myDepth);
 
 	}
 		//handles all the input/output.
 	public ClientKalaha(String _host, int _port, boolean _ai, int _depth) throws Exception
 	{
 		int next_player = 0;
 
 		nodeDepth = _depth;
 		port = _port;
 		host = _host;
 		ai = _ai;
 
 		String input;
 		String reply;
 		String output;
 		BufferedReader terminalInput = new BufferedReader(new InputStreamReader(System.in) );
 
 		Socket socket = new Socket(host, port);
 		PrintWriter outData = new PrintWriter(socket.getOutputStream(), true);
 		BufferedReader inData = new BufferedReader(new InputStreamReader(socket.getInputStream() ) );
 
 		outData.println("HELLO");
 		reply = inData.readLine();
 
 		String parse[] = reply.split(" ");
 		player = Integer.parseInt(parse[1]);
 
 		print("You are player "+player+".");
 
 		while(true)
 		{
 			outData.println("WINNER");
 			reply = inData.readLine();
 
 			if(reply.equals("1") || reply.equals("2") )
 			{
 				System.out.println("\n\nPlayer "+reply+" won!\n Press Enter to play again.");
 				terminalInput.readLine();
 				outData.println("NEW");
 				reply = inData.readLine();
 			}
 
 			outData.println("PLAYER");
 			reply = inData.readLine();
 			try
 			{
 				next_player = Integer.parseInt(reply);
 			}
 			catch(NumberFormatException e)
 			{
 				next_player = 1;
 			}
 
 			if(next_player == player)
 			{
 				outData.println("BOARD");
 				reply = inData.readLine();
 				System.out.println(parseBoardToPrint(reply) + "\n");
 				if(ai == false)
 				{
 					System.out.print("You are next! make a move.\n[1,2,3,4,5,6,N]  > ");
 					input = terminalInput.readLine();
 					//input = new String(""+IterativeDeepening2(host, port) );
 				}
 				else
 				{
 					input = new String(""+IterativeDeepening(host, port) );
 				}
 
 				if(input.toUpperCase().startsWith("N") )
 				{
 					output = new String("NEW");
 				}
 				else if(input.equals("1") || input.equals("2") || input.equals("3") || input.equals("4") || input.equals("5") || input.equals("6") )
 				{
 					output = new String("MOVE "+input+" "+player);
 				}
 				else
 				{
 					output = new String("BOARD");
 				}
 
 				outData.println(output);
 				reply = inData.readLine();
 
 				if(reply.startsWith("ERROR") )
 				{
 					System.out.println(reply);
 				}
 				else
 				{
 					System.out.println(parseBoardToPrint(reply) );
 				}
 			}
 			else
 			{
 				outData.println("BOARD");
 				reply = inData.readLine();
 				System.out.println(parseBoardToPrint(reply) + "\n");
 				if(player == 1)
 				{
 					System.out.println("\nWaiting for player 2, press enter to update.");
 				}
 				else
 				{
 					System.out.println("\nWaiting for player 1, press enter to update.");
 				}
 				terminalInput.readLine();
 			}
 		}
 	}
 
 		//returns a ranom number from 1 to 6.
 	public int getRandom()
 	{
 		return 1+ (int) (Math.random()*6);
 	}
 
 		//returns the string with a fixed length of 3 chars.
 	public String makeSpaces(String _in)
 	{
 		String _out = new String();
 
 		if(_in.length() == 2)
 		{
 			_out = " "+_in;
 		}
 		else if(_in.length() == 1)
 		{
 			_out = "  "+_in;
 		}
 		else
 		{
 			_out = _in;
 		}
 
 		return _out;
 	}
 
 		//returns a formatted string ready to print out, directly from a string returned by the server.
 	public String parseBoardToPrint(String _parse)
 	{
 		String board[] = _parse.split(";");
 		String out = new String("\n[2]");
 
 		for(int i=13; i>7; i--)
 		{
 			out += makeSpaces(board[i]);
 		}
 
 		out += "\n" + makeSpaces(board[0]) + "                  " + makeSpaces(board[7]) + "\n" + "[1]";
 
 		for(int i=1; i<7; i++)
 		{
 			out += makeSpaces(board[i]);
 		}
 		return out;
 	}
 		//prints stuff.
 	public void print(Object o)
 	{
 		System.out.println(""+o);
 	}
 
 	public int IterativeDeepening(String _host, int _port) throws Exception {
 		
 		AiNodeMaster tree;
 		String reply;
 		String temp[];
 		int x[];
 		int b = 0;
 		char output[];
 		char childBoard[];
 		boolean root = false;
 		
 		Socket socket = new Socket(host, port);
 		PrintWriter outData = new PrintWriter(socket.getOutputStream(), true);
 		BufferedReader inData = new BufferedReader(new InputStreamReader(socket.getInputStream() ) );
 		outData.println("BOARD");
 		reply = inData.readLine();
 		temp = reply.split(";");
 		x = new int[temp.length];
 		output = new char[temp.length];
 		
 		for (int i = 0; i < temp.length; i ++) {
 			x[i] = Integer.parseInt(temp[i]);
 			output[i] = (char)x[i];
 		}
 		tree = new AiNodeMaster(null, output, (short)0, (char)10);
 		tree.setBeta((short)32700);
 		tree.setAlpha((short)-32700);
 		maxCurrentDepth = 0;
 		newDepth = false;
 		millis = System.currentTimeMillis();
 		DLS2(tree, nodeDepth, output[14], output);
		print("Nodes: "+nodeCount);
		System.out.println((System.currentTimeMillis())-millis);
		nodeDepth = 9;
		DLS2(tree, nodeDepth, output[14], output);
 		while(bestBoard.getParent().getChoise() != 10){
 			bestBoard = bestBoard.getParent();
 		}
		print(nodeDepth);
 		System.out.println((System.currentTimeMillis())-millis);
 
 		return bestBoard.getChoise();
 
 		
 	}	
 
 	
 
 
 
 	public char[] intToCharConverter(int[] _board){
 		char output[];
 
 		output = new char[_board.length];
 		
 		for (int i = 0; i < _board.length; i ++) {
 			output[i] = (char)_board[i];
 		}
 
 		return output;
 	}
 
 	
 
 	
 	public void DLS2(AiNodeMaster node, int depth, int Max, char[] _board) {
 		char childBoard[];
 		short childNodeValue = (short)0;
 		int counter = 0;
 		boolean end = false;
 		
		if (depth > maxCurrentDepth) {				
 			for (int i = 1; i<7; i++) {
 				childBoard = node.move((short)i, (short)_board[14], _board.clone());
 				if (!Arrays.equals(childBoard, _board)) {
 					
 					if (Max == _board[14] && Max == 1) {
 						childNodeValue = (short)(node.getNodeValue() + ((childBoard[7] - childBoard[0])*depth));	
 						end = alphaBeta(node, true, childNodeValue, (short)32700);
 											
 					} else if (Max == _board[14] && Max == 2){
 						childNodeValue = (short)(node.getNodeValue() + ((childBoard[0] - childBoard[7])*depth));
 						end = alphaBeta(node, true, childNodeValue, (short)32700);
 					}
 					else if(Max != _board[14] && Max == 1){
 						childNodeValue = (short)(node.getNodeValue() - ((childBoard[0] - childBoard[7])*depth));
 						end = alphaBeta(node, false, (short)-32700, childNodeValue);
 					}
 					else if(Max != _board[14] && Max == 2){
 						childNodeValue = (short)(node.getNodeValue() - ((childBoard[7] - childBoard[0])*depth));
 						end = alphaBeta(node, false, (short)-32700, childNodeValue);
 					}
 					
 					if(!end){
						nodeCount++;
 						node.childNode.add(new AiNodeMaster(node, childBoard, childNodeValue, (char)i));
 						if (nodeDepth == depth && counter == 0) {
 							bestBoard = node.childNode.get(counter);
 						}
 						else if (bestBoard.getNodeValue() < childNodeValue) {
 							bestBoard = node.childNode.get(counter);
 						}
 						
 						counter++;
 						
 						if(!newDepth && (System.currentTimeMillis()-millis)>2500) {
 							newDepth = true;
 							maxCurrentDepth = 2;
 						}
 						DLS2(node.childNode.get(counter-1), depth-1, Max, childBoard);	
 					}		
 				}	
 			}			
 		}
 	}
 
 
 	public boolean alphaBeta(AiNodeMaster node, boolean MaxNode, short alpha, short beta){
 		
 		if (MaxNode) {
 			for (int i = 0; i < node.childNode.size(); i++) {
 				alpha = maxAlphaBeta(alpha, node.getAlpha());
 				if (alpha >= beta) {
 					return true;		
 				}
 			node.setAlpha(maxAlphaBeta(alpha, node.getAlpha()));	
 			}			
 		
 			if (node.childNode.size() != 0) {
 				node.setAlpha(alpha);
 			}
 		}
 		else {
 			for (int i = 0; i < node.childNode.size(); i++) {
 				beta = maxAlphaBeta(beta, node.getBeta());
 				if (beta <= alpha) {
 					return true;		
 				}
 				node.setBeta(maxAlphaBeta(beta, node.getBeta()));	
 			}
 			if (node.childNode.size() != 0) {
 				node.setBeta(beta);
 			}
 		}
 		
 		return false;
 	}
 
 	public short maxAlphaBeta(short m1, short m2){
 		if (m2 > m1) {
 			return m2;
 		}
 		else{
 			return m1;
 		}
 	}
 
 
 
 }
