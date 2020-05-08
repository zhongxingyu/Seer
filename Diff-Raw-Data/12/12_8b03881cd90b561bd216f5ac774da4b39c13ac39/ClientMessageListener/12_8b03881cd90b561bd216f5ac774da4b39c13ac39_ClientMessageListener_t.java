 package com.dyndns_home.maxwielsch.intelligent_agents.auction_agents_messaging.client;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.dyndns_home.maxwielsch.intelligent_agents.auction_agents_messaging.exceptions.InvalidJsonMessageException;
 import com.dyndns_home.maxwielsch.intelligent_agents.auction_agents_messaging.util.MessageType;
 
 /**
  * This class handles the incoming communication from another TCP end point. The
  * aim of this class is only to handle messages and not to control the
  * connection. That is to say the connection should be maintained outside this
  * class.
  * 
  * @author Max Wielsch
  * 
  */
 public class ClientMessageListener {
 
 	private char MESSAGE_DELIMITER = '%';
 
 	private BufferedInputStream in;
 	private ClientMessageHandler messageHandler;
 	private boolean listen;
 
 	/**
 	 * Expects a socket whose connection is alive.
 	 * 
 	 * @param input
 	 *            An input stream to listen to.
 	 * @param messageHandler
 	 *            An hander implementing the interface
 	 *            {@link ClientMessageHandler}. It is called when a message
 	 *            arrives at this end point.
 	 */
 	public ClientMessageListener(InputStream input,
 			ClientMessageHandler messageHandler) {
 
 		in = new BufferedInputStream(input);
 		this.messageHandler = messageHandler;
 		listen = true;
 	}
 
 	/**
 	 * Expects a socket whose connection is alive.
 	 * 
 	 * @param input
 	 *            An input stream to listen to.
 	 * @param messageHandler
 	 *            An hander implementing the interface
 	 *            {@link ClientMessageHandler}. It is called when a message
 	 *            arrives at this end point.
 	 * @param messageDelimiter
 	 *            a token that will be appended after all messages to send to
 	 *            signal the end of the message. As it has a special meaning and
 	 *            must be unique it mustn't be used in the message. The default
 	 *            delimiter is the char %.
 	 */
 	public ClientMessageListener(InputStream input,
 			ClientMessageHandler messageHandler, char messageDelimiter) {
 		this(input, messageHandler);
 		MESSAGE_DELIMITER = messageDelimiter;
 	}
 
 	/**
 	 * Begin listening to the auction managers messages.
 	 * 
 	 * @throws InvalidJsonMessageException
 	 *             Will be thrown when the communication partner sends an
 	 *             invalid JSON message. This doesn't necessarily mean that the
 	 *             message is not JSOn message. It only doesn't have the
 	 *             required key value pairs.
 	 */
 	public void startListenting() throws InvalidJsonMessageException {
 		while (listen) {
 			try {
 				listen();
 			} catch (IOException e) {
 				e.printStackTrace();
 				listen = false;
 			}
 		}
 	}
 
 	/**
 	 * Stop the MessageListener listening to the input stream.
 	 */
 	public void stopListening() {
 		listen = false;
 	}
 
 	/**
 	 * Listen to the inputStream and handle the the message. When the message
 	 * was read it is given to the message handler to decide how to deal with
 	 * this message.
 	 * 
 	 * @throws IOException
 	 *             Will be thrown when an error occurs with the socket
 	 *             connection.
 	 * @throws InvalidJsonMessageException
 	 *             Will be thrown when the communication partner sends an
 	 *             invalid JSON message. This doesn't necessarily mean that the
 	 *             message is not JSOn message. It only doesn't have the
 	 *             required key value pairs.
 	 */
 	private void listen() throws IOException, InvalidJsonMessageException {
 
 		String message = getNextIncomingMessage();
 
 		try {
 			JSONObject jsonObject = new JSONObject(message);
 
 			switch (MessageType.valueOf(jsonObject.getString("action"))) {
 			case NEW_ROUND:
 				handleNewRound(jsonObject);
 				break;
 			case END_ROUND:
 				handleEndRound(jsonObject);
 				break;
 			case ACCEPT_OFFER:
 				handleLastAcceptedOffer(jsonObject);
 				break;
 			case END_AUCTION:
 				handleAuctionEnd();
 				break;
 			default:
 				throw new JSONException(
 						"JSON message couldn't be handled due to bad format.");
 			}
 		} catch (JSONException e) {
 			throw new InvalidJsonMessageException(
 					"The Server send an invalid JSON message!");
 		}
 	}
 
 	/**
 	 * Call the message handler and tell it the message parameters.
 	 * 
 	 * @param jsonObject
 	 *            The message object that could be parsed out of the JSON
 	 *            message.
 	 * @throws JSONException
 	 *             Will be thrown when the communication partner sends an
 	 *             invalid JSON message. This doesn't necessarily mean that the
 	 *             message is not JSOn message. It only doesn't have the
 	 *             required key value pairs.
 	 */
 	private void handleNewRound(JSONObject jsonObject) throws JSONException {
 		int round = jsonObject.getInt("round");
 		int amount = jsonObject.getInt("amount");
 		double price = jsonObject.getDouble("price");
 		messageHandler.handleNewAuctionRound(round, amount, price);
 	}
 
 	/**
 	 * Call the message handler and tell it the message parameters.
 	 * 
 	 * @param jsonObject
 	 *            The message object that could be parsed out of the JSON
 	 *            message.
 	 * @throws JSONException
 	 *             Will be thrown when the communication partner sends an
 	 *             invalid JSON message. This doesn't necessarily mean that the
 	 *             message is not JSOn message. It only doesn't have the
 	 *             required key value pairs.
 	 */
 	private void handleEndRound(JSONObject jsonObject) throws JSONException {
 		String winner = jsonObject.getString("winner");
 		if (winner == null || winner.length() == 0) {
 			messageHandler.handleEndAuctionRound(null);
 		} else {
 			messageHandler.handleEndAuctionRound(winner);
 		}
 	}
 	
 	/**
 	 * Call the message handler and tell it the message parameters.
 	 * 
 	 * @param jsonObject
 	 *            The message object that could be parsed out of the JSON
 	 *            message.
 	 * @throws JSONException
 	 *             Will be thrown when the communication partner sends an
 	 *             invalid JSON message. This doesn't necessarily mean that the
 	 *             message is not JSOn message. It only doesn't have the
 	 *             required key value pairs.
 	 */
 	private void handleLastAcceptedOffer(JSONObject jsonObject) throws JSONException {
 		String participant = jsonObject.getString("participant");
 		double price = jsonObject.getDouble("price");
 		messageHandler.handleLastAcceptedOffer(participant, price);
 	}
 
 	/**
 	 * Call the message handler and tell it the message parameters.
 	 */
 	private void handleAuctionEnd() {
 		messageHandler.handleAuctionEnd();
 	}
 
 	/**
 	 * Read the next message that is transmitted via the inputStream. As long as
 	 * no message is there this method waits for it.
 	 * 
 	 * @return The message read from the socket connection.
 	 * @throws IOException
 	 *             Will be thrown when an error occurs with the socket
 	 *             connection.
 	 */
 	private String getNextIncomingMessage() throws IOException {
 
 		StringBuffer buffer = new StringBuffer();
		char character;
 
		while ((character = (char) in.read()) != MESSAGE_DELIMITER) {
			if(character == 65535) {
				//TODO find out why 65535
				listen = false;
				break;
			}
			buffer.append(((char) character));
 		}
 		return buffer.toString();
 	}
 
 }
