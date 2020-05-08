 package asciiWorld.ui;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 import asciiWorld.CreateColor;
 import asciiWorld.CreateRectangle;
 import asciiWorld.FontFactory;
 
 public class WindowPanel extends Border {
 
 	private static final int CORNER_RADIUS = 8;
 	private static final Color COLOR_BORDER_WINDOW = new Color(0.5f, 0.5f, 1.0f);
 	private static final Color COLOR_TEXT_TITLE = Color.white;
 	private static final Color COLOR_CONTENT_BORDER = new Color(0.0f, 0.75f, 0.5f);
 	
 	private static final int BUTTON_WIDTH = 106;
 	private static final int BUTTON_HEIGHT = 42;
 	private static final int MARGIN = 5;
 
 	private UnicodeFont _font;
 	private Label _titleLabel;
 	private Border _contentBackground;
 	private StackPanel _buttonPanel;
 	
 	public WindowPanel(Rectangle bounds, String title) throws Exception {
 		super(CreateRectangle.from(bounds).setCornerRadius(CORNER_RADIUS).getRectangle(), CreateColor.from(COLOR_BORDER_WINDOW).changeAlphaTo(0.25f).getColor(), true);
 		
 		_font = FontFactory.get().getDefaultFont();
 		
 		int buttonHeight = BUTTON_HEIGHT;
 		
 		_contentBackground = new Border(new Rectangle(bounds.getMinX() + 10, bounds.getMinY() + 40, bounds.getWidth() - 20, bounds.getHeight() - 50 - buttonHeight), COLOR_CONTENT_BORDER, false);
 		
 		Color contentFillColor = CreateColor.from(COLOR_CONTENT_BORDER).changeAlphaTo(0.25f).getColor();
 		Border contentBorder = new Border(new Rectangle(bounds.getMinX() + 10, bounds.getMinY() + 40, bounds.getWidth() - 20, bounds.getHeight() - 50 - buttonHeight), contentFillColor, true);
 		contentBorder.setContent(_contentBackground);
 		
 		CanvasPanel windowCanvas = new CanvasPanel();
 		Vector2f titlePosition = new Vector2f(
 				bounds.getMinX() + (bounds.getWidth() - _font.getWidth(title)) / 2,
 				bounds.getMinY() + 10);
 		_titleLabel = new Label(titlePosition, _font, title, COLOR_TEXT_TITLE) {{
 			setTextWrappingMode(TextWrappingMode.NoWrap);
 		}};
 		windowCanvas.addChild(_titleLabel);
 		
 		_buttonPanel = getButtons();
 		windowCanvas.addChild(_buttonPanel);
 		windowCanvas.addChild(contentBorder);
 		
 		Border windowBorder = new Border(getBounds(), COLOR_BORDER_WINDOW, false);
 		windowBorder.setContent(windowCanvas);
 
 		setContent(windowBorder);
 	}
 	
 	public String getTitle() {
 		return _titleLabel.getText();
 	}
 	
 	public void setTitle(String value) {
 		Rectangle bounds = getBounds();
 		Vector2f titlePosition = new Vector2f(
 				bounds.getMinX() + (bounds.getWidth() - _font.getWidth(value)) / 2,
 				bounds.getMinY() + 10);
 		_titleLabel.setText(value);
 		_titleLabel.getBounds().setWidth(_font.getWidth(value));
 		_titleLabel.moveTo(titlePosition);
 	}
 	
 	public Boolean isClosed() {
 		return getParent() == null;
 	}
 	
 	/**
 	 * This sets what will be displayed within the inner content frame,
 	 * as opposed to setting the content of the outer border, which is
 	 * what calling setContent will do.
 	 * 
 	 * @param content
 	 * @throws Exception
 	 */
 	public void setWindowContent(FrameworkElement content) throws Exception {
 		_contentBackground.setContent(content);
 	}
 	
 	private StackPanel getButtons() throws Exception {
 		Rectangle dialogBounds = getBounds();
 		int numberOfButtons = 1;
 		int myWidth = BUTTON_WIDTH * numberOfButtons;
 		
 		StackPanel buttonPanel = new StackPanel(
 				new Rectangle(
 						dialogBounds.getMinX() + (dialogBounds.getWidth() - myWidth) / 2,
 						dialogBounds.getMaxY() - BUTTON_HEIGHT - MARGIN,
 						myWidth,
 						BUTTON_HEIGHT));
 		
 		buttonPanel.addChild(Button.createActionButton("Close", new MethodBinding(this, "closeWindow")));
 
 		return buttonPanel;
 	}
 	
 	public void addButton(Button button) {
 		Rectangle dialogBounds = getBounds();
 		float myWidth = dialogBounds.getWidth() - MARGIN * 2;
 		
 		try {
 			_buttonPanel.addChild(0, button);
			_buttonPanel.getBounds().setX(MARGIN);
 			_buttonPanel.getBounds().setWidth(myWidth);
 			_buttonPanel.resetBounds();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void closeWindow() {
 		try {
 			if (getRoot() instanceof RootVisualPanel) {
 				// Ensure that this window can only be closed once.
 				getRoot().modalWindowIsClosing();
 				if (getParent() != null) {
 					getParent().setParent(null); // close the modal panel
 					setParent(null); // close the inventory window
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println("Error while attempting to close the window.");
 		}
 	}
 	
 	boolean firstCall = true;
 	@Override
 	public void update(GameContainer container, int delta) {
 		super.update(container, delta);
 		
 		Input input = container.getInput();
 		if (firstCall) {
 			input.isKeyPressed(Input.KEY_ESCAPE); // clear the pressed state
 			firstCall = false;
 		} else {
 			if (input.isKeyPressed(Input.KEY_ESCAPE)) {
 				closeWindow();
 			}
 		}
 	}
 }
