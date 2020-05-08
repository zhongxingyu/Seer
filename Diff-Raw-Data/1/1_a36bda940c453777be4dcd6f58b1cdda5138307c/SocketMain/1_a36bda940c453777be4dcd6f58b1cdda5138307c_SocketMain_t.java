 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Scanner;
 import java.util.Date;
 
 
 import java.io.*;
 import java.net.*;
 
 public class SocketMain{
 	
 	public static ServerSocket create(){
 		
 		for(int i = 1024; i < 2048; i++){
 			try{
 				return new ServerSocket(i);
 			}
 			catch(IOException e){
 				continue;
 			}
 		}
 		return null;
 	}
 	
 	
 	public static void main(String args[]){
 		
 		System.out.print("(1) Server or (2) Client?");
 		Scanner firstScan = new Scanner(System.in);
 		String response1 = firstScan.nextLine();
 		System.out.println("(1) Human or (2) Computer?");
 		String response2 = firstScan.nextLine();
 		
 		boolean human = false;
 		if(response2.equals("1")){
 			human = true;
 		}
 		else if(response2.equals("2")){
 			human = false;
 		}
 		else{
 			System.out.println("Error on human/computer selection");
 			System.exit(3);
 		}
 		
 		if(response1.equals("1")){
 			
 			Fanorona game;
 			Piece.Type clientPlayer;
 			int responseTime;
 			ServerSocket sock = create();
 			System.out.println("Listening on "+sock.getLocalPort());
 			
 //			clientThread cT = new clientThread("",sock.getLocalPort());
 //			cT.start();
 			
 			Socket client = null;
 			PrintWriter out = null;
 			BufferedReader in = null;
 			try{
 				client = sock.accept();
 				out = new PrintWriter(client.getOutputStream(), true);
 				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
 			}
 			catch(IOException e){
 				System.out.println("Accept failed");
 				System.exit(1);
 			}
 			
 			String inputLine, outputLine;
 			out.println("WELCOME");
 			int _rows = 5;
 			int _columns = 9;
 			char clientT = 'W';
 			out.println("INFO "+_columns+" "+_rows+" "+clientT+" "+"5000");
 			
 			game = new Fanorona(9,5);
 			Piece.Type serverPlayer;
 			if(clientT == 'B'){
 				clientPlayer = Piece.Type.BLACK;
 				serverPlayer = Piece.Type.WHITE;
 			}
 			else{
 				clientPlayer = Piece.Type.WHITE;
 				serverPlayer = Piece.Type.BLACK;
 			}
 	
 			String playerInput = "";
 			boolean cont = true;
 			try {
 				while((playerInput = in.readLine()) != null && cont){
 		//			System.out.print("Enter a command: ");
 					//System.out.println("From client: "+playerInput);
 						
 		
 		//			Scanner scan2 = new Scanner(System.in);
 					if(playerInput.equals("READY")){
 						out.println("BEGIN");
 						
 						if(serverPlayer == Piece.Type.WHITE && !human){
 							String move = game.getAIMove(serverPlayer);
 							game.move(move);
 							out.println(move);
 						}
 						else if(serverPlayer == Piece.Type.WHITE && human){
 							
 							System.out.print("Enter a move ");
 							playerInput = "";
 							Scanner scan2 = new Scanner(System.in);
 							playerInput = scan2.nextLine();
 							playerInput = game.convertToInternalMove(playerInput);
 							game.move(playerInput);
 							out.println(playerInput);
 							
 						}
 						
 						continue;
 					}
 					if(playerInput.equals("OK")){
 						//out.println("OK");
 						continue;
 					}
 	//			playerInput = scan2.nextLine();
 					System.out.println("input = " + playerInput);
 					int index = playerInput.indexOf(' ');
 					String command = "";
 					try{
 						command = playerInput.substring(0,index);
 					}
 					catch(Exception e){
 						command = playerInput;
 					}
 					
 					if(command.equals("INFO")){
 						String cmd = playerInput;
 						cmd = cmd.substring(index+1);
 						index = cmd.indexOf(' ');
 						int columns = Integer.parseInt(cmd.substring(0,index));
 						cmd = cmd.substring(index+1);
 						index = cmd.indexOf(' ');
 						int rows = Integer.parseInt(cmd.substring(0,index));
 						cmd = cmd.substring(index+1);
 						index = cmd.indexOf(' ');
 						char startType = cmd.charAt(index-1);
 						cmd = cmd.substring(index+1);
 						int timeRestriction = Integer.parseInt(cmd);
 						game = new Fanorona(columns,rows);
 						if(startType == 'W'){
 							clientPlayer = Piece.Type.WHITE;
 						}
 						else if(startType == 'B'){
 							clientPlayer = Piece.Type.BLACK;
 						}
 						else{
 							System.out.println("Type error");
 							System.exit(1);
 						}
 						responseTime = timeRestriction;
 						System.out.println("READY");
 					}
 					else if(command.equals("BEGIN")){
 						//Start game
 					}
 					else if(command.equals("A") || command.equals("W") || command.equals("S") || command.equals("P")){
 						out.println("OK");
 						
 						if(game.capturingMoveAvailable() && !game.isPossibleCapturingMove(playerInput)){
 							out.println("ILLEGAL");
 							out.println("LOSER");
							break;
 						}
 						game.move(playerInput);
 						game.prettyprint();
 						int val = game.checkEndGame();
 						if(val == 1){
 							//white win
 							if(clientPlayer == Piece.Type.WHITE){
 								out.println("WINNER");
 								break;
 							}
 							else{
 								out.println("LOSER");
 								break;
 							}
 						}
 						else if(val == -1){
 							//black win
 							if(clientPlayer == Piece.Type.WHITE){
 								out.println("LOSER");
 								break;
 							}
 							else{
 								out.println("WINNER");
 								break;
 							}
 						}
 						else if(val == 2){
 							//max turns
 							out.println("TIE");
 							break;
 						}
 						else{
 							if(!human){
 								String move = game.getAIMove(serverPlayer);
 								game.move(move);
 								out.println(move);
 							}
 							else{
 								System.out.print("Enter a move ");
 								playerInput = "";
 								Scanner scan2 = new Scanner(System.in);
 								playerInput = scan2.nextLine();
 								playerInput = game.convertToInternalMove(playerInput);
 								game.move(playerInput);
 								out.println(playerInput);
 							}
 						}
 						
 						val = game.checkEndGame();
 						if(val == 1){
 							//white win
 							if(clientPlayer == Piece.Type.WHITE){
 								out.println("WINNER");
 								break;
 							}
 							else{
 								out.println("LOSER");
 								break;
 							}
 						}
 						else if(val == -1){
 							//black win
 							if(clientPlayer == Piece.Type.WHITE){
 								out.println("LOSER");
 								break;
 							}
 							else{
 								out.println("WINNER");
 								break;
 							}
 						}
 						else if(val == 2){
 							//max turns
 							out.println("TIE");
 							break;
 						}
 						//Approach move
 					}
 					else{
 						out.println("ILLEGAL");
 						cont = false;
 					}
 				}
 			} catch (NumberFormatException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			try {
 				sock.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		else if(response1.equals("2")){
 			
 				
 			Fanorona game = null;
 			Piece.Type clientPlayer = null;
 			Piece.Type serverPlayer = null;
 			
 			int responseTime;
 		
 			Socket tSock = null;
 			BufferedReader in = null;
 			PrintWriter out = null;
 			
 			String host = "";
 			int port = 0;
 			try{
 				host = args[0];
 				port = Integer.parseInt(args[1]);
 			}
 			catch(Exception e){
 				System.out.println("Command line arguments error");
 				System.exit(2);
 			}
 			
 			try {
 				tSock = new Socket(host, port);
 				out = new PrintWriter(tSock.getOutputStream(), true);
 				in = new BufferedReader(new InputStreamReader(tSock.getInputStream()));
 			} catch (UnknownHostException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			System.out.println("client - got connection");
 			
 			String response = "";
 			try {
 				while((response = in.readLine()) != null){
 					System.out.println("from server: "+response+".");
 					if(response.equals("WELCOME")){
 						continue;
 					}
 					if(response.equals("OK")){
 						continue;
 					}
 					if(response.equals("ILLEGAL")){
 						break;
 					}
 					if(response.equals("LOSER")){
 						System.out.println("Lost");
 						System.exit(4);
 					}
 					if(response.equals("WINNER")){
 						System.out.println("won");
 						System.exit(5);
 					}
 					
 					int index = response.indexOf(' ');
 					String command = response;
 					if(index == -1){
 						command = response;
 					}
 					else{
 						command = response.substring(0,index);
 					}
 					System.out.println("Command: "+command);
 					
 					if(command.equals("INFO")){
 						String cmd = response;
 						cmd = cmd.substring(index+1);
 						index = cmd.indexOf(' ');
 						int columns = Integer.parseInt(cmd.substring(0,index));
 						cmd = cmd.substring(index+1);
 						index = cmd.indexOf(' ');
 						int rows = Integer.parseInt(cmd.substring(0,index));
 						cmd = cmd.substring(index+1);
 						index = cmd.indexOf(' ');
 						char startType = cmd.charAt(index-1);
 						cmd = cmd.substring(index+1);
 						int timeRestriction = Integer.parseInt(cmd);
 						game = new Fanorona(columns,rows);
 						if(startType == 'W'){
 							clientPlayer = Piece.Type.WHITE;
 						}
 						else if(startType == 'B'){
 							clientPlayer = Piece.Type.BLACK;
 						}
 						else{
 							System.out.println("Type error");
 							System.exit(1);
 						}
 						if(clientPlayer == Piece.Type.WHITE){
 							serverPlayer = Piece.Type.BLACK;
 						}
 						else{
 							serverPlayer = Piece.Type.WHITE;
 						}
 						responseTime = timeRestriction;
 						out.println("READY");
 					}
 					else if(command.equals("BEGIN")){
 						//Start game
 						System.out.println("In here");
 						if(clientPlayer == Piece.Type.WHITE && !human){
 							String move = game.getAIMove(clientPlayer);
 							game.move(move);
 							out.println(move);
 						}
 						else{
 							
 							System.out.print("Enter a move ");
 							String playerInput = "";
 							Scanner scan2 = new Scanner(System.in);
 							playerInput = scan2.nextLine();
 							playerInput = game.convertToInternalMove(playerInput);
 							game.move(playerInput);
 							out.println(playerInput);
 							
 						}
 						continue;
 					}
 					else if(command.equals("A")){
 						
 						
 						out.println("OK");
 						game.move(response);
 						System.out.println("check1");
 						game.prettyprint();
 //							System.out.print("Enter a move ");
 //							String playerInput = "";
 //							Scanner scan2 = new Scanner(System.in);
 //							playerInput = scan2.nextLine();
 //							try{
 //								playerInput = game.convertToInternalMove(playerInput);
 //							}
 //							catch(Exception e){
 //								
 //							}
 //							out.println(playerInput);
 						if(!human){
 							String move = game.getAIMove(clientPlayer);
 							game.move(move);
 							game.prettyprint();
 							out.println(move);
 						}
 						else{
 							System.out.print("Enter a move ");
 							String playerInput = "";
 							Scanner scan2 = new Scanner(System.in);
 							playerInput = scan2.nextLine();
 							playerInput = game.convertToInternalMove(playerInput);
 							game.move(playerInput);
 							out.println(playerInput);
 						}
 						
 						//Approach move
 					}
 					else if(command.equals("W")){
 						out.println("OK");
 						game.move(response);
 						game.prettyprint();
 						
 						if(!human){
 							String move = game.getAIMove(clientPlayer);
 							game.move(move);
 							game.prettyprint();
 							out.println(move);
 						}
 						else{
 							System.out.print("Enter a move ");
 							String playerInput = "";
 							Scanner scan2 = new Scanner(System.in);
 							playerInput = scan2.nextLine();
 							playerInput = game.convertToInternalMove(playerInput);
 							game.move(playerInput);
 							out.println(playerInput);
 						}
 						//withdrawal
 					}
 					else if(command.equals("P")){
 						out.println("OK");
 						game.move(response);
 						game.prettyprint();
 //							System.out.print("Enter a move ");
 //							String playerInput = "";
 //							Scanner scan2 = new Scanner(System.in);
 //							playerInput = scan2.nextLine();
 //							playerInput = game.convertToInternalMove(playerInput);
 //							game.move(playerInput);
 //							out.println(playerInput);
 						
 						if(!human){
 							String move = game.getAIMove(clientPlayer);
 							game.move(move);
 							game.prettyprint();
 							out.println(move);
 						}
 						else{
 							System.out.print("Enter a move ");
 							String playerInput = "";
 							Scanner scan2 = new Scanner(System.in);
 							playerInput = scan2.nextLine();
 							playerInput = game.convertToInternalMove(playerInput);
 							game.move(playerInput);
 							out.println(playerInput);
 						}
 						//piaka
 					}
 					else if(command.equals("S")){
 						out.println("OK");
 						game.move(response);
 						game.prettyprint();
 						
 						if(!human){
 							String move = game.getAIMove(clientPlayer);
 							game.move(move);
 							game.prettyprint();
 							out.println(move);
 						}
 						else{
 							System.out.print("Enter a move ");
 							String playerInput = "";
 							Scanner scan2 = new Scanner(System.in);
 							playerInput = scan2.nextLine();
 							playerInput = game.convertToInternalMove(playerInput);
 							game.move(playerInput);
 							out.println(playerInput);
 						}
 					}
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			out.close();
 			try {
 				in.close();
 				tSock.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	
 		}
 		else{
 			
 			System.out.println("Server/client selection error");
 			
 		}
 	}
 }
