 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 public class BughouseServer extends Thread {
 	private final int PLAYER_TOTAL = 4;
 	private ArrayList<Game> g = new ArrayList<Game>();
 	private final int port = 3355;
 	private ServerSocket server = null;
 	private ArrayList<ClientHandler> playerList = new ArrayList<ClientHandler>();
 
 	public static void main(String[] args) {
 		new BughouseServer().start();
 	}
 
 	public BughouseServer() {
 		g.add(new Game());
 		g.add(new Game());
 		//Setup partners and gameCount
 		for(int i = 0;i<2;i++) {
 			g.get(i).pW.partner = g.get((i+1)%2).pB;
 			g.get(i).pB.partner = g.get((i+1)%2).pW;
 			g.get(i).pW.gameCount++;
 			g.get(i).pB.gameCount++;
 		}
 
 		//setup serversocket
 		try {
 			server = new ServerSocket(port);
 			InetAddress ip = InetAddress.getLocalHost();
 			System.out.println("Server IP address: " + ip.getHostAddress());
 			System.out.println("Listening on port " + port);
 		}
 		catch(UnknownHostException e) {
 			e.printStackTrace();
 		}
 		catch(IOException ex) {
 			System.out.println("Error can't connect to port " + port);
 			System.exit(1);
 		}
 	}
 	@Override
 	public void run() {
 		//get clients, setup ClientHandlers
 		while(playerList.size() < PLAYER_TOTAL) {
 			try {
 				Socket s = server.accept();
 				ClientHandler ch = new ClientHandler(s);
 				playerList.add(ch);
 				System.out.println("Accepted client socket");
 			}
 			catch(IOException ex) {
 				System.out.println("Failed to accept client OR create I/O streams");
 				System.exit(1);
 			}
 		}
 
 		Collections.shuffle(playerList);
 		for(int i = 0;i<PLAYER_TOTAL;i++) {
 			ClientHandler ch = playerList.get(i);
 			Game clientGame = null;
 			clientGame = g.get(i/2);
 			if(i % 2 == 0) {
 				ch.send(clientGame.pW);
 			}
 			else {
 				ch.send(clientGame.pB);
 			}
 			ch.send(g.get(0));
 			ch.send(g.get(1));
 		}
 	}
 
 	class ClientHandler {
 		private Socket socket = null;
 		private ObjectOutputStream oos = null;
 		private InputHandler ih = null;
 
 		public ClientHandler(Socket s) {
 			this.socket = s;
 			try {
 				oos = new ObjectOutputStream(s.getOutputStream());
 				ih = new InputHandler(s);
 				ih.start();
 			}
 			catch(IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		class InputHandler extends Thread {
 			private ObjectInputStream ois = null;
 			public InputHandler(Socket s) {
 				try {
 					ois = new ObjectInputStream(s.getInputStream());
 				}
 				catch(IOException e) {
 					e.printStackTrace();
 				}
 			}
 			@Override
 			public void run() {
 				try {
 					while(true) {
 						Move move = (Move) ois.readObject();
 						int gameID = move.player.gameID;
 						boolean validMove = g.get(gameID).applyMove(move);//handles legal checks
 						int totalLegalMoves = 0;
 						boolean sendOtherBoard = false;//set in if(validMove), used before sending to clients
 						for(Piece p : g.get(gameID).turn.pieceList) {
 							totalLegalMoves += p.getLegalMoves(g.get(gameID), false).size();
 						}
 						for(Piece p : g.get(gameID).turn.heldPieces) {
 							totalLegalMoves += p.getLegalPlacement(g.get(gameID)).size();
 						}
 						if(totalLegalMoves == 0) {
 							g.get(0).setWinner(g.get(gameID).getOpponent());
 							g.get(1).setWinner(g.get(gameID).getOpponent());
 						}
 						
 						if(validMove) {
 							move = new Move(move, g.get(move.player.gameID));//dereference from the Game, necessary for the server
 							move.player.pickupQueue();
 							move.player.opponent.pickupQueue();
 							
 							sendOtherBoard = !move.player.opponent.deadPieces.isEmpty();
 							for(Piece p : move.player.opponent.deadPieces) {
								if(!(p instanceof Pawn) && p.originalType == Piece.PieceType.P) {
 									p = new Pawn(move.player.partner);
 								}
 								p.loc = null;
 								p.setOwner(move.player.partner);
 								
 								if(g.get((move.player.gameID+1)%2).turn == move.player.partner) {
 									move.player.partner.queuingPieces.add(p);//Adds piece to queuingPieces if it's the partner's turn 
 								}
 								else {
 									move.player.partner.heldPieces.add(p);//Adds piece to heldPieces if it's not partner's turn (helps partner's opponent see what to deal with)
 								}
 							}
 							move.player.opponent.deadPieces.clear();
 						}
 
 						for(ClientHandler ch : playerList) {
 							if(ch == null) {
 								playerList.remove(ch);
 							}
 							else {
 								ch.send(g.get(gameID));
 								if(sendOtherBoard) {
 									ch.send(g.get((gameID+1)%2));
 								}
 							}
 						}
 					}
 				}
 				catch(ClassNotFoundException e) {
 					e.printStackTrace();
 				}
 				catch(IOException e) {
 					e.printStackTrace();
 				}
 				finally {
 					if(ois != null) {
 						try {
 							ois.close();
 						}
 						catch(IOException e) {
 							e.printStackTrace();
 						}
 					}
 					if(socket != null) {
 						try {
 							socket.close();
 						}
 						catch(IOException e) {
 							e.printStackTrace();
 						}
 					}
 					playerList.remove(this);//TODO: announce to other players, perhaps allow another player to play
 				}
 			}
 		}
 
 		public boolean send(Game g) {
 			boolean returner = false;
 			try {
 				oos.writeObject(g);
 				oos.reset();//necessary to send new Game object, not just references
 				returner = true;
 			}
 			catch(IOException e) {
 				e.printStackTrace();
 				returner = false;
 			}
 			return returner;
 		}
 		public boolean send(Player p) {
 			boolean returner = false;
 			try {
 				oos.writeObject(p);
 				returner = true;
 			}
 			catch(IOException e) {
 				e.printStackTrace();
 				returner = false;
 			}
 			return returner;
 		}
 
 		public void close() {
 			if(oos != null) {
 				try {
 					oos.close();
 				}
 				catch(IOException e) {
 					e.printStackTrace();
 				}
 			}
 			if(socket != null) {
 				try {
 					socket.close();
 				}
 				catch(IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public void closeSockets() {
 		for(ClientHandler c : playerList) {
 			if(c != null) {
 				c.close();
 			}
 		}
 		if(server != null) {
 			try {
 				server.close();
 			}
 			catch(IOException e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println("Server Exited");
 		System.exit(0);
 	}
 }
