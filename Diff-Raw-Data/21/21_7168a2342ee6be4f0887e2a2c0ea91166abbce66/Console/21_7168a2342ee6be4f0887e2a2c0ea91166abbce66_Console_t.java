 package vibe;
 
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 
 import javax.swing.JTextPane;
 
 import earfuck.IoManager;
 import earfuck.Parser;
 
 public class Console extends JTextPane implements IoManager{
 	private ArrayList<String> mText;
 	private String inputText;
 	private boolean inputMode = false;
 	private Parser mParser;
 	
 	private final String lineStartIndicator = "> ";
 	private final String inputIndicator = ":>";
 	
 	public Console() {
 		super();
 		mText = new ArrayList<String>();
 		inputText = "";
 		mParser = null;
 		updateText();
 		setEditable(false);
 	}
 	
 	@Override
 	protected void processKeyEvent(KeyEvent event) {
 		if (inputMode) {
 			if (event.getID()==KeyEvent.KEY_PRESSED) {
 				//Handle deleting chars with backspace
 				if (event.getKeyCode()==KeyEvent.VK_BACK_SPACE) {
 					if (inputText.length()>0) {
 						inputText = inputText.substring(0,inputText.length()-1);
 					}
 				}
 				if (event.getKeyCode()==KeyEvent.VK_ENTER) {
 					if (mParser!=null) {
 						try {
 							mParser.giveInput(Integer.parseInt(inputText));
 							mParser = null;
 							mText.add(inputText);
 							inputMode = false;
 							setEditable(false);
							
							//This is a hack to lose focus
							setFocusable(false);
							setFocusable(true);
 						}
 						catch (NumberFormatException e) {
 							addLine("Invalid: "+inputText);
 						}
 					}
 					inputText = "";
 				}
 			}
 			if (event.getID()==KeyEvent.KEY_TYPED) {
 				
 				if (event.getKeyChar() != KeyEvent.CHAR_UNDEFINED &&
 						event.getKeyChar() != KeyEvent.VK_BACK_SPACE &&
 						event.getKeyChar() != KeyEvent.VK_ENTER) {
 					inputText += event.getKeyChar();
 				}
 			}
 		}
 
 		updateText();
 	}
 	
 	private void updateText() {
 		StringBuilder builder = new StringBuilder();
 		for (String line : mText) {
 			builder.append(lineStartIndicator+line+"\n");
 		}
 		
 		builder.append(inputIndicator+inputText+"\n");
 
 		String text = builder.toString();
 		setText(text.replaceFirst("\\n$", ""));
 		this.setCaretPosition(getText().length());
 	}
 	
 	public void addLine(String line) {
 		mText.add(line);
 		updateText();
 	}
 	
 	public void takeInput() {
 		inputMode = true;
 		updateText();
 		requestFocusInWindow(false);
 		setEditable(true);
 	}
 
 	@Override
 	public void requestInput(Parser parser) {
 		mParser = parser;
 		takeInput();
 	}
 
 	@Override
 	public void output(int value) {
 		addLine(String.valueOf(value));
 	}
 }
