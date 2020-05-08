 package asciiWorld.ui;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Scriptable;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.KeyListener;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.RoundedRectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 import asciiWorld.FontFactory;
 import asciiWorld.TextFactory;
 
 //Reference: https://developer.mozilla.org/en-US/docs/Rhino
 //Reference: https://developer.mozilla.org/en-US/docs/Rhino_documentation
 //Reference: https://developer.mozilla.org/en-US/docs/Rhino/Embedding_tutorial
 
 public class ScriptingWindow extends Border implements KeyListener {
 
 	private static final int CURSOR_BLINK_INTERVAL = 500;
 	private static final String CURSOR_TEXT = "_";
 	private static final Color COLOR_BORDER_TEXT = new Color(0.0f, 0.75f, 0.5f);
 	private static final Color COLOR_TEXT = Color.yellow;
 	private static final Color COLOR_BORDER_WINDOW_FILL = new Color(0.5f, 0.5f, 1.0f, 0.25f);
 	private static final int TEXT_SIZE = 12;
 	
 	private Border _textContainer;
 	
 	private Context _context;
 	private Scriptable _scope;
 	private String _text;
 	private UnicodeFont _textFont;
 	private int _cursorIndex;
 	private Boolean _controlIsPressed;
 	
 	/**
 	 * This is used to manage the cursor blink.
 	 */
 	private int _totalTime;
 	
 	public ScriptingWindow(GameContainer container, Rectangle bounds)
 			throws Exception {
 		super(new RoundedRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 8), COLOR_BORDER_WINDOW_FILL, true);
 		
 		createUI();
 		
 		initializeScriptingContext();
 		
 		container.getInput().enableKeyRepeat();
 		container.getInput().addKeyListener(this);
 
 		_textFont = FontFactory.get().getResource(TEXT_SIZE);
 
 		_text = "";
 		_cursorIndex = 0;
 		_totalTime = 0;
 		_controlIsPressed = false;
 	}
 	
 	public void executeScript() {
 		try {
 			// Evaluate the text as Javascript.
 			Object result = _context.evaluateString(_scope, _text, "<cmd>", 1, null);
 			
 			// Convert the result to a string and display it.
 			String scriptOutput = Context.toString(result);
 			
 			getRoot().showMessageBox(true, scriptOutput, "Script Output");
 		} catch (Exception e) {
 			getRoot().showMessageBox(true, e.getMessage(), "Runtime Error");
 		}
 	}
 
 	public void clearAllText() {
 		_text = "";
 		_cursorIndex = 0;
 	}
 
 	public MessageBox showHelp() {
 		try {
 			return getRoot().showMessageBox(true, TextFactory.get().getResource("scriptingInstructions"), "Help");
 		} catch (Exception e) {
 			System.err.println("Unable to show the scripting instructions.");
 			return null;
 		}
 	}
 	
 	public void close(GameContainer container) {
 		Context.exit();
 		container.getInput().disableKeyRepeat();
 		container.getInput().removeKeyListener(this);
 	}
 	
 	private void createUI() throws Exception {
 		final RoundedRectangle windowBounds = (RoundedRectangle)getBounds();
 		
 		Color textContainerFillColor = new Color(COLOR_BORDER_TEXT);
 		textContainerFillColor.a = 0.25f;
 		_textContainer = new Border(new Rectangle(windowBounds.getX() + 10, windowBounds.getY() + 10, windowBounds.getWidth() - 20, windowBounds.getHeight() - 42 - 20), textContainerFillColor, true);
 		_textContainer.setContent(new Border(COLOR_BORDER_TEXT, false));
 		
 		Color windowBorderColor = new Color(COLOR_BORDER_WINDOW_FILL);
 		windowBorderColor.a = 1.0f;
 		setContent(new Border(getBounds(), windowBorderColor, false) {{
 			setContent(new CanvasPanel() {{
 				addChild(_textContainer);
 				addChild(getButtons(windowBounds));
 			}});
 		}});
 	}
 	
 	private StackPanel getButtons(RoundedRectangle dialogBounds) throws Exception {
 		float myWidth = dialogBounds.getWidth();
 		
 		final Button executeButton = Button.createActionButton("Execute", new MethodBinding(this, "executeScript"));
 		final Button clearButton = Button.createActionButton("Clear", new MethodBinding(this, "clearAllText"));
 		final Button helpButton = Button.createActionButton("Help", new MethodBinding(this, "showHelp"));
 		
 		return new StackPanel(new Rectangle(dialogBounds.getMinX() + (dialogBounds.getWidth() - myWidth) / 2, dialogBounds.getMaxY() - 42 - 5, myWidth, 42)) {{
 			addChild(executeButton);
 			addChild(clearButton);
 			addChild(helpButton);
 		}};
 	}
 	
 	@Override
 	public void update(GameContainer container, int delta) {
 		_totalTime += delta;
 		super.update(container, delta);
 	}
 	
 	@Override
 	public void render(Graphics g) {
 		super.render(g);
 		
 		Boolean cursorWasRendered = false;
 		Vector2f tileSize = new Vector2f(_textFont.getWidth("*"), _textFont.getLineHeight());
 		int textMargin = 5;
 		Vector2f position = new Vector2f(_textContainer.getBounds().getMinX() + textMargin, _textContainer.getBounds().getMinY() + textMargin);
 		for (int index = 0; index < _text.length(); index++) {
 			if ((index == _cursorIndex) && (((_totalTime / CURSOR_BLINK_INTERVAL) % 2) == 1)) {
 				_textFont.drawString(position.x, position.y, CURSOR_TEXT, COLOR_TEXT);
 				cursorWasRendered = true;
 			}
 			
 			switch (_text.charAt(index)) {
 			case '\n':
 				position.y += tileSize.y;
 				position.x = _textContainer.getBounds().getMinX() + textMargin;
 				break;
 			case '\t':
 				float tabSize = 4 * tileSize.x;
 				position.x += (tabSize - ((position.x + tileSize.x) % tabSize));
 				break;
 			default:
 				_textFont.drawString(position.x, position.y, Character.toString(_text.charAt(index)), COLOR_TEXT);
 				position.x += tileSize.x;
 				break;
 			}
 			
 			if (position.x > _textContainer.getBounds().getMaxX() - tileSize.x - textMargin) {
 				position.x = _textContainer.getBounds().getMinX() + textMargin;
 				position.y += tileSize.y;
 			}
 			if (position.y > _textContainer.getBounds().getMaxY() - tileSize.y - textMargin) {
 				break;
 			}
 		}
 		
 		if (!cursorWasRendered) {
 			if (((_totalTime / CURSOR_BLINK_INTERVAL) % 2) == 1) {
 				if (position.y <= _textContainer.getBounds().getMaxY() - tileSize.y - textMargin) {
 					_textFont.drawString(position.x, position.y, CURSOR_TEXT, COLOR_TEXT);
 					cursorWasRendered = true;
 				}
 			}
 		}
 	}
 	
 	private void initializeScriptingContext() {
 		// Create and enter a Context.  The Context stores information about the execution environment of a script.
 		_context = Context.enter();
 		
 		// Initialize the standard objects (Object, Function, etc.).
 		// This must be done before scripts can be executed.
 		// Returns a scope object that we use in later calls.
 		_scope = _context.initStandardObjects();
 	}
 	
 	@Override
 	public void keyPressed(int key, char c) {
 		if ((' ' <= c) && (c <= '~')) {
 			_text = _text.substring(0, _cursorIndex).concat(Character.toString(c)).concat((_cursorIndex == _text.length()) ? "" : _text.substring(_cursorIndex, _text.length()));
 			_cursorIndex++;
 		} else {
 			switch (key) {
 			case Input.KEY_ENTER:
 				_text = _text.substring(0, _cursorIndex).concat("\n").concat((_cursorIndex == _text.length()) ? "" : _text.substring(_cursorIndex, _text.length()));
 				_cursorIndex++;
 				break;
 			case Input.KEY_TAB:
 				_text = _text.substring(0, _cursorIndex).concat("\t").concat((_cursorIndex == _text.length()) ? "" : _text.substring(_cursorIndex, _text.length()));
 				_cursorIndex++;
 				break;
 			case Input.KEY_HOME:
 				if (_controlIsPressed) {
 					_cursorIndex = 0;
 				} else {
 					moveCursorToBeginningOfLine();
 				}
 				break;
 			case Input.KEY_END:
 				if (_controlIsPressed) {
 					_cursorIndex = _text.length();
 				} else {
 					moveCursorToEndOfLine();
 				}
 				break;
 			case Input.KEY_BACK:
 				if (_cursorIndex > 0) {
 					_text = _text.substring(0, _cursorIndex - 1).concat((_cursorIndex == _text.length()) ? "" : _text.substring(_cursorIndex, _text.length()));
 					_cursorIndex--;
 				}
 				break;
 			case Input.KEY_DELETE:
 				if (_cursorIndex < _text.length()) {
 					_text = _text.substring(0, _cursorIndex).concat(((_cursorIndex + 1) == _text.length()) ? "" : _text.substring(_cursorIndex + 1, _text.length()));
 				}
 				break;
 			case Input.KEY_UP:
 				moveCursorToBeginningOfLine();
 				if (_cursorIndex > 0) {
 					_cursorIndex--;
 					moveCursorToBeginningOfLine();
 				}
 				break;
 			case Input.KEY_DOWN:
 				moveCursorToEndOfLine();
 				if (_cursorIndex < _text.length()) {
 					_cursorIndex++;
 					moveCursorToBeginningOfLine();
 				}
 				break;
 			case Input.KEY_LEFT:
 				_cursorIndex--;
 				if (_cursorIndex < 0) {
 					_cursorIndex = 0;
 				}
 				break;
 			case Input.KEY_RIGHT: 
 				_cursorIndex++;
 				if (_cursorIndex > _text.length()) {
 					_cursorIndex = _text.length();
 				}
 				break;
 			case Input.KEY_F11:
 				executeScript();
 				break;
			case Input.KEY_LCONTROL:
			case Input.KEY_RCONTROL:
				_controlIsPressed = true;
				break;
 			}
 		}
 	}
 	
 	private void moveCursorToBeginningOfLine() {
 		if (!cursorIsAtBeginningOfLine()) {
 			while (_cursorIndex > 0) {
 				_cursorIndex--;
 				if (_text.charAt(_cursorIndex) == '\n') {
 					_cursorIndex++;
 					break;
 				}
 			}
 		}
 	}
 	
 	private void moveCursorToEndOfLine() {
 		if (!cursorIsAtEndOfLine()) {
 			while (true) {
 				_cursorIndex++;
 				if ((_cursorIndex == _text.length()) || (_text.charAt(_cursorIndex) == '\n')) {
 					break;
 				}
 			}
 		}
 	}
 	
 	private Boolean cursorIsAtBeginningOfLine() {
 		return (_cursorIndex == 0) || (_text.charAt(_cursorIndex - 1) == '\n');
 	}
 	
 	private Boolean cursorIsAtEndOfLine() {
 		return (_cursorIndex == _text.length()) || (_text.charAt(_cursorIndex) == '\n');
 	}
 
 	@Override
 	public void setInput(Input input) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean isAcceptingInput() {
 		return true;
 	}
 
 	@Override
 	public void inputEnded() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void inputStarted() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyReleased(int key, char c) {
		if ((key == Input.KEY_LCONTROL) || (key == Input.KEY_RCONTROL)) {
			_controlIsPressed = false;
		}
 	}
 }
