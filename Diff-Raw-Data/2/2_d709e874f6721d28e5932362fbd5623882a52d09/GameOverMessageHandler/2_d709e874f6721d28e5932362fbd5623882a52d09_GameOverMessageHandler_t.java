 package foop.java.snake.common.message.handler;
 
 import java.net.SocketAddress;
 import java.util.Observable;
 
 import foop.java.snake.common.message.GameOverMessage;
 import foop.java.snake.common.message.InputMessage;
 import foop.java.snake.common.message.MessageInterface;
 import foop.java.snake.common.message.exception.NoMessageHandlerFoundException;
 
 public class GameOverMessageHandler extends Observable implements
 		MessageHandlerInterface {
 
 	@Override
 	public void handle(MessageInterface message, SocketAddress address)
 			throws NoMessageHandlerFoundException {
 
		if (message.getType() != GameOverMessage.TYPE) {
 			throw new NoMessageHandlerFoundException("This is not a GameOver-Message.");
 		}
 
 		this.printInput((GameOverMessage) message);
 
 		// Implementation of the observer-pattern
 		setChanged();
 		notifyObservers((GameOverMessage) message);
 
 	}
 	private void printInput(GameOverMessage m) {
 		System.out.println("GameOverMessageHandler: Got GameOver-Message.");
 		System.out.println("Message: " + m.getMessage());
 	}
 }
 
 
 
