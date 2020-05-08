 package foop.java.snake.common.message;
 
 /**
  * Message sent to the server upon keyboard input
  * @author Fabian Grünbichler
  *
  */
 public class InputMessage implements MessageInterface {
 
 	private static final long serialVersionUID = 1;
    public static final int TYPE = 1;
 
 	public enum Keycode {
 		UP, DOWN, LEFT, RIGHT
 	}
 	
 	private Keycode input;
 	private String playerName;
 	
 	public InputMessage(String playerName, Keycode input) {
 		this.input = input;
 		this.playerName = playerName;
 	}
 
 	public Keycode getInput() {
 		return input;
 	}
 
 	public String getPlayerName() {
 		return playerName;
 	}
 	
 	@Override
 	public int getType() {
 		return InputMessage.TYPE;
 	}
 
 }
