 package ch.bfh.monopoly.net;
 
 import org.apache.mina.core.service.IoHandler;
 import org.apache.mina.core.session.IdleStatus;
 import org.apache.mina.core.session.IoSession;
 
 import ch.bfh.monopoly.common.GameClient;
 
 /**
  * This class is used to handle the operations of the client over the network
  * 
  * @author snake
  * 
  */
 public class ClientHandler implements IoHandler {
 
 	private boolean gameCanBegin = false;
 	private GameClient gameClient;
 	private String localPlayerName;
 	private int rollOrderValue;
 
 	public ClientHandler(GameClient gc, String localPlayerName,
 			int rollOrderValue) {
 		this.gameClient = gc;
 		this.localPlayerName = localPlayerName;
 		this.rollOrderValue = rollOrderValue;
 	}
 
 	@Override
 	public void exceptionCaught(IoSession arg0, Throwable arg1)
 	throws Exception {
 	}
 
 	/**
 	 * Check wehter the game can begin or not
 	 * 
 	 * @return a boolean , true if the game can begin, false otherwise
 	 */
 	public boolean gameCanBegin() {
 		return gameCanBegin;
 	}
 
 	@Override
 	public void messageReceived(IoSession arg0, Object arg1) throws Exception {
 		NetMessage n = (NetMessage) arg1;
 
 		switch (n.getMessageType()) {
 		case GAME_START:
 			// create the board for this client by passing the locale the users
 			// and the name
 			// of the locaplayer
 			gameClient.createBoard(n.getLocale(), n.getUserNameList(),
 					this.localPlayerName);
 
 			// TODO only for test
 			System.out.println("Client list:" + n.getUserNameList().size());
 			System.out.println("User size: " + n.getUserNameList());
 
 			gameCanBegin = true;
 			break;
 		case CHAT_MSG:
 			gameClient.displayChat(n.getString1());
 			break;
 		case DICE_ROLL:
 			// TODO dice roll function
 			System.out.println("ROLL RECEIVED");
 			int rollValue = n.getInt();
 			System.out
 			.println("THIS IS THE ROLL VALUE RECEIVED IN netMessage.DICE_ROLL: "
 					+ rollValue);
 			gameClient.advancePlayerNSpacesInDirection(rollValue, n.getDir(),
 					false);
 			break;
 		case KICK_REQUEST:
 			System.err.println("clientHandler: KICK_REQUEST: netMessage: string1" + n.getString1() + " string2 "+ n.getString2());
 			gameClient.receiveKickRequest(n.getString1(), n.getString2());
 			break;
 		case KICK_ANSWER:
 			gameClient.receiveKickVote(n.getString1(), n.getTradeAnswer());
 			break;	
 		case KICK_PLAYER:
			gameClient.kickThePlayer(n.getString1());
 			break;
 		case BUY_HOUSE:
 			gameClient.buyHouse(n.getInt(), false);
 			break;
 		case BUY_HOUSEROW:
 			gameClient.buyHouseRow(n.getInt(), false);
 			break;
 		case BUY_HOTEL:
 			gameClient.buyHotel(n.getInt(), false);
 			break;
 		case BUY_HOTELROW:
 			gameClient.buyHotelRow(n.getInt(), false);
 			break;
 		case BUY_PROPERTY:
 			System.out.println("RECEIVED MESSAGE: " + n.getMessageCode());
 			gameClient.buyCurrentPropertyForPlayer(n.getString1(), false);
 			break;
 		case SELL_HOUSE:
 			gameClient.sellHouse(n.getInt(), false);
 			break;
 		case SELL_HOUSEROW:
 			gameClient.sellHouseRow(n.getInt(), false);
 			break;
 		case SELL_HOTEL:
 			gameClient.sellHotel(n.getInt(), false);
 			break;
 		case SELL_HOTELROW:
 			gameClient.sellHotelRow(n.getInt(), false);
 			break;
 		case SELL_PROPERTY:
 			gameClient.transferProperty(n.getFromName(), n.getToName(),
 					n.getInt(), n.getPrice(), false);
 			break;
 
 		case SELL_CARD:
 			gameClient.transferProperty(n.getFromName(), n.getToName(),
 					n.getInt(), n.getPrice(), false);
 			break;
 			
 		case TRANSFER_JAILCARD:
 			System.out.println("clientHandler: received TransferJailcard: from " + n.getFromName() +" to " + n.getToName());
 			gameClient.transferJailCards(n.getFromName(), n.getToName(), n.getInt(), 0, false);
 			break;
 		case TRANSFER_MONEY:
 			System.out.println("clientHandler: received TransferMoney: from " + n.getFromName() +" to " + n.getToName());
 			gameClient.transferMoney(n.getFromName(), n.getToName(), n.getInt(), false);
 			break;
 		case TRANSFER_PROPERTY:
 			System.out.println("clientHandler: received TransferProperty: from " + n.getFromName() +" to " + n.getToName());
 			gameClient.transferProperty(n.getFromName(), n.getToName(), n.getInt(),0, false);
 			break;
 			
 		case TOGGLE_MORTGAGE:
 			gameClient.toggleMortgageStatus(n.getInt(), false);
 			break;
 
 		case PAY_RENT:
 			gameClient.payRent(false);
 			break;
 		case TURN_TOKEN:
 			String username = n.getString1();
 			gameClient.updateTurnTokens(username);
 			break;
 		case START_TURN_PANEL:
 			// what can we put here that other clients see the start window?
 			break;
 		case PAY_FEE:
 			gameClient.payFee(n.getInt(), false);
 			break;
 		case PAY_UTILITY_FEE:
 			gameClient.payUtilityFee(n.getInt(), false);
 			break;
 		case FREE_PARKING:
 			gameClient.freeParking(false);
 			break;
 		case BIRTHDAY_EVENT:
 			gameClient.birthdayEvent(n.getInt(), false);
 			break;
 		case WIN_JAIL_CARD:
 			gameClient.winJailCard(false);
 			break;
 		case UPDATE_CHANCE_ORDER:
 			gameClient.updateChanceDrawOrder(n.getDrawOrder(), false);
 			break;
 		case UPDATE_COMMCHEST_ORDER:
 			gameClient.updateCommChestDrawOrder(n.getDrawOrder(), false);
 			break;
 		case GET_EVENT_WINDOW:
 			break;
 		case GO_TO_JAIL:
 			gameClient.goToJail(false);
 			break;
 		case GET_OUT_OF_JAIL_ROLL:
 			gameClient.getOutOfJailByRoll(false);
 			break;
 		case GET_OUT_OF_JAIL_PAY:
 			gameClient.getOutOfJailByPayment(false);
 			break;
 		case GET_OUT_OF_JAIL_USECARD:
 			gameClient.getOutOfJailByCard(false);
 			break;
 		case GET_OUT_OF_JAIL_FAILURE:
 			gameClient.getOutOfJailFailure(false);
 			break;
 		case TRADE_REQUEST:
 			System.out.println("clientHandler: TRADE REQUEST ARRIVED");
 			gameClient.receiveTradeRequest(n.getTie());
 			break;
 		case TRADE_ANSWER: 
 			System.out.println("ClientHandler: trade answer received: "+n.getTradeAnswer());
 			gameClient.receiveTradeAnswer(n.getTradeAnswer());
 			break;
 		case PERFORM_TRADE:
 			gameClient.performTrade(n.getTie());
 			break;
 		case ACKNOWLEDGE:
 			// TODO probably to remove
 			break;
 		case QUIT_GAME:
 			System.out.println("The player quit the game: " + n.getString1());
 			gameClient.dividePlayerAssets(n.getString1());
 			break;
 		case GAME_WINNER:
 			System.out.println("THERE IS A WINNER AND IS: " + n.getString1());
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void messageSent(IoSession arg0, Object arg1) throws Exception {
 		NetMessage n = (NetMessage) arg1;
 		System.out.println("SENT A MESSAGE" + n.getMessageType());
 	}
 
 	@Override
 	public void sessionClosed(IoSession arg0) throws Exception {
 
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void sessionCreated(IoSession arg0) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void sessionIdle(IoSession arg0, IdleStatus arg1) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void sessionOpened(IoSession arg0) throws Exception {
 		// TODO Auto-generated method stub
 		System.out.println("session opened");
 
 		// as first, send the username and the roll value
 		NetMessage n = new NetMessage(this.localPlayerName,
 				this.rollOrderValue, Messages.SEND_USERNAME);
 		arg0.write(n);
 	}
 
 }
