 package menu;
 
 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glColor3f;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glTexCoord2f;
 import static org.lwjgl.opengl.GL11.glVertex2f;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.newdawn.slick.opengl.Texture;
 
 import sound.Sound;
 import breakout.GameState;
 import engine.Font;
 import engine.Game;
 
 public abstract class MainMenu implements IMenu {
 	private enum SELECTION {
 		CONTINUE, START, OPTIONS, CREDITS, EXIT
 	}
 
 	private Texture cursorTexture;
 	private Font font;
 	private SELECTION currentSelection;
 	private boolean isKeyPressed;
 	private float highlightColor;
 	private float highlightColorDelta;
 
 	public MainMenu(Font font, Texture cursorTexture) {
 		this.font = font;
 		this.cursorTexture = cursorTexture;
 		this.currentSelection = SELECTION.START;
 		this.isKeyPressed = true;
 		this.highlightColor = 1.0f;
 		this.highlightColorDelta = -1.0f;
 	}
 
 	public abstract void showOptions();
 
 	public abstract void showCredits();
 
 	@Override
 	public void render(int delta) {
 		renderCursor();
 		if (GameState.paused) {
 			highlightSelection(SELECTION.CONTINUE);
 			font.drawText("CONTINUE", Display.getWidth() / 2.0f,
 					getSelectionY(SELECTION.CONTINUE));
 		}
 		highlightSelection(SELECTION.START);
 		font.drawText("START", Display.getWidth() / 2.0f,
 				getSelectionY(SELECTION.START));
 		highlightSelection(SELECTION.OPTIONS);
 		font.drawText("OPTIONS", Display.getWidth() / 2.0f,
 				getSelectionY(SELECTION.OPTIONS));
 		highlightSelection(SELECTION.CREDITS);
 		font.drawText("CREDITS", Display.getWidth() / 2.0f,
 				getSelectionY(SELECTION.CREDITS));
 		highlightSelection(SELECTION.EXIT);
 		font.drawText("EXIT", Display.getWidth() / 2.0f,
 				getSelectionY(SELECTION.EXIT));
 	}
 
 	private void highlightSelection(SELECTION selection) {
 		if (currentSelection.equals(selection))
 			glColor3f(1.0f, highlightColor, 0.0f);
 		else
 			glColor3f(1.0f, 1.0f, 1.0f);
 	}
 
 	private void renderCursor() {
 		float width = Display.getWidth() / 24;
 		float height = Display.getHeight() / 18;
 		float x = Display.getWidth() * 0.5f - 1.2f * width;
 		float y = getSelectionY(currentSelection);
 		cursorTexture.bind();
 		glColor3f(1.0f, 1.0f, 1.0f);
 		glBegin(GL_QUADS);
 		glTexCoord2f(0.0f, cursorTexture.getHeight());
 		glVertex2f(x, y);
 		glTexCoord2f(cursorTexture.getWidth(), cursorTexture.getHeight());
 		glVertex2f(x + width, y);
 		glTexCoord2f(cursorTexture.getWidth(), 0.0f);
 		glVertex2f(x + width, y + height);
 		glTexCoord2f(0.0f, 0.0f);
 		glVertex2f(x, y + height);
 		glEnd();
 	}
 
 	private float getSelectionY(SELECTION selection) {
 		float y = Display.getHeight() * 0.5f;
 		switch (selection) {
 		case CONTINUE:
 			y += Display.getHeight() / 16.0f;
 			break;
 		case OPTIONS:
 			y -= Display.getHeight() / 16.0f;
 			break;
 		case CREDITS:
 			y -= Display.getHeight() / 8.0f;
 			break;
 		case EXIT:
 			y -= Display.getHeight() / 5.2f;
 			break;
 		default:
 			break;
 		}
 		return y;
 	}
 
 	@Override
 	public void update(int delta) {
 		updateHighlightColor(delta);
 
 		handleKeyboardInput();
 
 		handleMouseInput();
 
 		handleSelectedAction();
 	}
 
 	private void handleKeyboardInput() {
 		boolean downPressed = Keyboard.isKeyDown(Keyboard.KEY_DOWN);
 		boolean upPressed = Keyboard.isKeyDown(Keyboard.KEY_UP);
 		boolean returnPressed = Keyboard.isKeyDown(Keyboard.KEY_RETURN);
 		if (!isKeyPressed) {
 			if (downPressed || upPressed) {
 				currentSelection = getNextSelection(currentSelection, upPressed);
 				isKeyPressed = true;
 				Sound.get().playCursor();
 			}
 		} else if (!downPressed && !upPressed && !returnPressed) {
 			isKeyPressed = false;
 		}
 	}
 
 	private void handleMouseInput() {
 		if (Mouse.getDX() != 0 || Mouse.getDY() != 0) {
 			for (SELECTION selection : SELECTION.values()) {
 				if (!currentSelection.equals(selection)
 						&& isMouseOnSelection(selection)
 						&& (!selection.equals(SELECTION.CONTINUE) || GameState.paused)) {
 					Sound.get().playCursor();
 					currentSelection = selection;
 					break;
 				}
 			}
 		}
 	}
 
 	private boolean isMouseOnSelection(SELECTION selection) {
 		float x = Mouse.getX();
 		float y = Mouse.getY();
 		return x > Display.getWidth() / 2.0f
 				&& x < Display.getWidth() / 2.0f + 7 * font.getCharacterWidth()
 				&& y > getSelectionY(selection)
 				&& y < getSelectionY(selection) + font.getCharacterHeight();
 	}
 
 	private void handleSelectedAction() {
 		if (!isKeyPressed
 				&& (Keyboard.isKeyDown(Keyboard.KEY_RETURN) || Mouse
 						.isButtonDown(0)
 						&& isMouseOnSelection(currentSelection))) {
 			isKeyPressed = true;
 			Sound.get().playAccept();
 			switch (currentSelection) {
 			case CONTINUE:
 				Game.get().setCurrentState(GameState.name);
 				break;
 			case START:
 				Game.get().reinit(GameState.name);
 				Game.get().setCurrentState(GameState.name);
 				break;
 			case OPTIONS:
 				showOptions();
 				break;
 			case CREDITS:
 				showCredits();
 				break;
 			case EXIT:
 				Game.get().requestShutdown();
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 	private SELECTION getNextSelection(SELECTION selection, boolean up) {
 		switch (selection) {
		case CONTINUE:
			return up ? SELECTION.EXIT : SELECTION.START;
 		case START:
			return up ? GameState.paused ? SELECTION.CONTINUE : SELECTION.EXIT
					: SELECTION.OPTIONS;
 		case OPTIONS:
 			return up ? SELECTION.START : SELECTION.CREDITS;
 		case CREDITS:
 			return up ? SELECTION.OPTIONS : SELECTION.EXIT;
 		case EXIT:
			return up ? SELECTION.CREDITS
					: GameState.paused ? SELECTION.CONTINUE : SELECTION.START;
 		default:
 			return SELECTION.START;
 		}
 	}
 
 	private void updateHighlightColor(float delta) {
 		float newColor = highlightColor + highlightColorDelta * delta / 200;
 		if (newColor < 0.0f || newColor > 1.0f) {
 			highlightColorDelta = -highlightColorDelta;
 		} else {
 			highlightColor = newColor;
 		}
 	}
 
 }
