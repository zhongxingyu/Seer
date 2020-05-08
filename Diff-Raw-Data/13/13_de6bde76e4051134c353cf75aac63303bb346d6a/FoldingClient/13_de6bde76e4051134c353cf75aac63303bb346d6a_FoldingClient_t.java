 package edu.vub.at.commlib;
 
 import java.io.IOException;
 
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 
 import edu.vub.at.nfcpoker.Card;
 import edu.vub.at.nfcpoker.PokerGameState;
 import edu.vub.at.nfcpoker.comm.Message.ClientAction;
 import edu.vub.at.nfcpoker.comm.Message.ClientActionType;
 import edu.vub.at.nfcpoker.comm.Message.FutureMessage;
 import edu.vub.at.nfcpoker.comm.Message.ReceiveHoleCardsMessage;
 import edu.vub.at.nfcpoker.comm.Message.ReceivePublicCards;
 import edu.vub.at.nfcpoker.comm.Message.RequestClientActionFutureMessage;
 import edu.vub.at.nfcpoker.comm.Message.SetClientParameterMessage;
 import edu.vub.at.nfcpoker.comm.Message.SetIDMessage;
 import edu.vub.at.nfcpoker.settings.Settings;
 
 public class FoldingClient {
 
 	public static Connection serverConnection;
 	public static int clientId;
 	
 	public static void main(String[] args) {
 		try {
 			String ipAddress = "127.0.0.1";
 			if (args.length > 0) ipAddress = args[0];
 			CommLibConnectionInfo.connect(ipAddress, CommLib.SERVER_PORT, new Listener() {
 				@Override
 				public void connected(Connection c) {
 					super.connected(c);
 					serverConnection = c;
 				}
 				
 				@Override
 				public void received(Connection c, Object m) {
 					super.received(c, m);
 					
 					System.out.println("Received message " + m.toString());
 
 					if (m instanceof SetIDMessage) {
 						final SetIDMessage sidm = (SetIDMessage) m;
 						clientId = sidm.id;
						SetClientParameterMessage pm = new SetClientParameterMessage(clientId, false, Settings.nickname, Settings.avatar, 2000);
 						serverConnection.sendTCP(pm);
 					}
 					
 					if (m instanceof PokerGameState) {
 						PokerGameState newGameState = (PokerGameState) m;
 						switch (newGameState) {
 			            	case STOPPED:
 			            		System.out.println("Game state changed to STOPPED");
 			            		break;
 			            	case WAITING_FOR_PLAYERS:
 			            		System.out.println("Game state changed to WAITING_FOR_PLAYERS");
 			            		break;
 			            	case PREFLOP:
 			            		System.out.println("Game state changed to PREFLOP");
 			            		break;
 			            	case FLOP:
 			            		System.out.println("Game state changed to FLOP");
 			            		break;
 			            	case TURN:
 			            		System.out.println("Game state changed to TURN");
 			            		break;
 			            	case RIVER:
 			            		System.out.println("Game state changed to RIVER");
 			            		break;
 			            	case END_OF_ROUND:
 			            		System.out.println("Game state changed to END_OF_ROUND");
 			            		break;
 						}
 					}
 					
 					if (m instanceof ReceivePublicCards) {
 						ReceivePublicCards newPublicCards = (ReceivePublicCards) m;
 						System.out.print("Received public cards: ");
 						Card[] cards = newPublicCards.cards;
 						for (int i = 0; i < cards.length; i++) {
 							System.out.print(cards[i].toString() + ", ");
 						}
 						System.out.println();
 					}
 					
 					if (m instanceof ReceiveHoleCardsMessage) {
 						ReceiveHoleCardsMessage newHoleCards = (ReceiveHoleCardsMessage) m;
 						System.out.print("Received hand cards: " + newHoleCards.toString());
 					}
 					
 					if (m instanceof RequestClientActionFutureMessage) {
 						RequestClientActionFutureMessage rcafm = (RequestClientActionFutureMessage) m;
 						c.sendTCP(new FutureMessage(rcafm.futureId, new ClientAction(ClientActionType.Fold)));
 					}
 						
 				}
 			});
 			while (true) {
 				Thread.sleep(10000);
 			}
 		} catch (IOException e) {
 			System.err.println("Could not discover server: ");
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}		
 	}
 }
