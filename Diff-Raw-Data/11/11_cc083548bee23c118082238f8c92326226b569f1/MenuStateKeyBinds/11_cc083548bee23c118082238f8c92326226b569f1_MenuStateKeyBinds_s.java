 package se.chalmers.tda367.group15.game.states;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.KeyListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.TrueTypeFont;
 
 import se.chalmers.tda367.group15.game.menu.Button;
 import se.chalmers.tda367.group15.game.menu.TextButton;
 import se.chalmers.tda367.group15.game.settings.Constants;
 import se.chalmers.tda367.group15.game.settings.KeyBindings;
 import se.chalmers.tda367.group15.game.settings.KeyBindings.Key;
 
 /**
  * A game over state. Displays a high score and allows the user to either replay
  * or go back to menu.
  * 
  * @author Peter
  */
 public class MenuStateKeyBinds extends AbstractMenuBasedState {
 
 	/**
 	 * The true typed font used to draw text.
 	 */
 	private final TrueTypeFont textFont = new TrueTypeFont(
 			TextButton.DEFAULT_FONT, true);
 
 	/**
 	 * the upper left corner of button group
 	 */
 	private static final int MENUX = Constants.MENU_UPPER_X;
 	private static final int MENUY = Constants.MENU_UPPER_Y;
 
 	/**
 	 * The starting y-point for the list of buttons and texts
 	 */
 	private static final int LIST_TOP_Y = MENUY + 50;
 
 	/**
 	 * The starting x-point for the list of buttons and texts
 	 */
 	private static final int LIST_TOP_X = MENUX;
 
 	/**
 	 * Spacing in y between each button
 	 */
 	private static final int LIST_Y_SPACING = 50;
 
 	/**
 	 * Spacing in x between the text and the buttons
 	 */
 	private static final int LIST_X_SPACING = 220;
 
 	/**
 	 * A map mapping the buttons to the key they are binding
 	 */
 	private final Map<TextButton, Key> buttonToKeyBind = new HashMap<TextButton, Key>();
 
 	/**
 	 * The currently clicked/active keybind button.
 	 */
 	private TextButton activeBindButton;
 
 	/**
 	 * Boolean indicating if we are currently rebinding a key or not.
 	 */
 	private boolean isRebinding;
 
 	/**
 	 * Creates a new MenuStateKeyBinds.
 	 * 
 	 * @param id
 	 *            The int used to identify the state.
 	 */
 	public MenuStateKeyBinds(int id) {
 		super(id);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void init() {
 		try {
 			setBackground(new Image("res/menu/backgroundKeys.png"));
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 
 		try {
 			addReturnButton();
 			initKeyBindButtons();
 			addKeyBindButtons();
 
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 
 		container.getInput().addKeyListener(new keyBindingListener());
 
 	}
 
 	private void addKeyBindButtons() {
 		// Set the text and add each button mapped in the buttonToKeyBind map
 		for (Entry<TextButton, Key> entry : buttonToKeyBind.entrySet()) {
 			String keyName = Input.getKeyName(KeyBindings.getBinding(entry
 					.getValue()));
 			entry.getKey().setText(keyName);
 
 			// Add items so that they are drawn
 			addMenuItem(entry.getKey());
 		}
 	}
 
 	private void addReturnButton() throws SlickException {
 		Image backImage = new Image("res/menu/returnButton.png");
 		Image backImageMO = new Image("res/menu/returnButtonMO.png");
 
 		// Button for returning to main menu.
 		Button returnButton = new Button(container, backImage, backImageMO,
 				MENUX, MENUY) {
 			@Override
 			public void performAction() {
 				stopRebindKey();
 				game.enterState(Constants.GAME_STATE_MENU_OPTIONS);
 			}
 		};
 
 		addMenuItem(returnButton);
 	}
 
 	private void initKeyBindButtons() throws SlickException {
 
 		Image imageNormal = new Image(180, 40);
 		Image imageOver = new Image("res/menu/EmptyMO.png");
 
 		TextButton bindUp = new TextButton(new Button(container, imageNormal,
				imageOver, LIST_TOP_X + LIST_X_SPACING, LIST_TOP_Y
						+ LIST_Y_SPACING * 0), null) {
 			@Override
 			public void performAction() {
 				startRebindKey(this);
 			}
 		};
 		TextButton bindDown = new TextButton(new Button(container, imageNormal,
 				imageOver, LIST_TOP_X + LIST_X_SPACING, LIST_TOP_Y
						+ LIST_Y_SPACING * 1), null) {
 			@Override
 			public void performAction() {
 				startRebindKey(this);
 			}
 		};
 		TextButton bindLeft = new TextButton(new Button(container, imageNormal,
 				imageOver, LIST_TOP_X + LIST_X_SPACING, LIST_TOP_Y
 						+ LIST_Y_SPACING * 2), null) {
 			@Override
 			public void performAction() {
 				startRebindKey(this);
 			}
 		};
 		TextButton bindRight = new TextButton(new Button(container,
 				imageNormal, imageOver, LIST_TOP_X + LIST_X_SPACING, LIST_TOP_Y
 						+ LIST_Y_SPACING * 3), null) {
 			@Override
 			public void performAction() {
 				startRebindKey(this);
 			}
 		};
 		TextButton bindWeapon1 = new TextButton(new Button(container,
 				imageNormal, imageOver, LIST_TOP_X + LIST_X_SPACING, LIST_TOP_Y
 						+ LIST_Y_SPACING * 4), null) {
 			@Override
 			public void performAction() {
 				startRebindKey(this);
 			}
 		};
 		TextButton bindWeapon2 = new TextButton(new Button(container,
 				imageNormal, imageOver, LIST_TOP_X + LIST_X_SPACING, LIST_TOP_Y
 						+ LIST_Y_SPACING * 5), null) {
 			@Override
 			public void performAction() {
 				startRebindKey(this);
 			}
 		};
 		TextButton bindWeapon3 = new TextButton(new Button(container,
 				imageNormal, imageOver, LIST_TOP_X + LIST_X_SPACING, LIST_TOP_Y
 						+ LIST_Y_SPACING * 6), null) {
 			@Override
 			public void performAction() {
 				startRebindKey(this);
 			}
 		};
 
 		// Map bind buttons to the key they are binding
 		buttonToKeyBind.put(bindUp, Key.UP);
 		buttonToKeyBind.put(bindDown, Key.DOWN);
 		buttonToKeyBind.put(bindLeft, Key.LEFT);
 		buttonToKeyBind.put(bindRight, Key.RIGHT);
 		buttonToKeyBind.put(bindWeapon1, Key.WEAPON_1);
 		buttonToKeyBind.put(bindWeapon2, Key.WEAPON_2);
 		buttonToKeyBind.put(bindWeapon3, Key.WEAPON_3);
 	}
 
 	/**
 	 * Starts listening for keybindings for the Key associated with the clicked
 	 * button. If the button is clicked twice, the binding of the key will be
 	 * aborted.
 	 * 
 	 * @param button
 	 *            The button that was pressed.
 	 */
     void startRebindKey(TextButton button) {
 		if (!buttonToKeyBind.containsKey(button)) {
 			return;
 		}
 
 		if (activeBindButton == button) {
 			// If clicked twice.
 			stopRebindKey();
 		} else {
 			stopRebindKey();
 			button.setText("---");
 			activeBindButton = button;
 			isRebinding = true;
 		}
 	}
 
 	/**
 	 * Sets a Key as the bind for the key we were rebinding
 	 * 
 	 * @param keyCode
 	 *            The key code of the key to bind to.
 	 */
     void setRebindKey(int keyCode) {
 		Key key = buttonToKeyBind.get(activeBindButton);
 		int prevBind = KeyBindings.getBinding(key);
 		KeyBindings.setBinding(key, keyCode);
 		stopRebindKey();
 
 		// Check for duplicate binds to the same key. If one exist, set it's
 		// current bound to the previous bound that was changed (ie. prevBind).
 		for (Entry<TextButton, Key> entry : buttonToKeyBind.entrySet()) {
 			TextButton b = entry.getKey();
 			Key k = entry.getValue();
 			if (k != key && KeyBindings.getBinding(k) == keyCode) {
 				KeyBindings.setBinding(k, prevBind);
 				startRebindKey(b);
 				stopRebindKey();
 				break;
 			}
 		}
 
 	}
 
 	/**
 	 * Stops rebinding a key and resets the active button to default state.
 	 */
 	private void stopRebindKey() {
 		if (!isRebinding) {
 			return;
 		}
 
 		Key key = buttonToKeyBind.get(activeBindButton);
 		activeBindButton.setText(Input.getKeyName(KeyBindings.getBinding(key)));
 
 		activeBindButton = null;
 		isRebinding = false;
 	}
 
 	@Override
 	public void render(Graphics g) {
 		super.render(g);
		textFont.drawString(LIST_TOP_X, LIST_TOP_Y + LIST_Y_SPACING * 0, "Up: ");
		textFont.drawString(LIST_TOP_X, LIST_TOP_Y + LIST_Y_SPACING * 1,
 				"Down: ");
 		textFont.drawString(LIST_TOP_X, LIST_TOP_Y + LIST_Y_SPACING * 2,
 				"Left: ");
 		textFont.drawString(LIST_TOP_X, LIST_TOP_Y + LIST_Y_SPACING * 3,
 				"Right: ");
 		textFont.drawString(LIST_TOP_X, LIST_TOP_Y + LIST_Y_SPACING * 4,
 				"Weapon 1: ");
 		textFont.drawString(LIST_TOP_X, LIST_TOP_Y + LIST_Y_SPACING * 5,
 				"Weapon 2: ");
 		textFont.drawString(LIST_TOP_X, LIST_TOP_Y + LIST_Y_SPACING * 6,
 				"Weapon 3: ");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void initMenuItems() {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void escpAction() {
 		// escape cancels current binding
 		if (isRebinding) {
 			stopRebindKey();
 		} else {
 			game.enterState(Constants.GAME_STATE_MENU_OPTIONS);
 		}
 	}
 
 	private class keyBindingListener implements KeyListener {
 		@Override
 		public void keyPressed(int key, char c) {
 		}
 
 		@Override
 		public void inputEnded() {
 		}
 
 		@Override
 		public void inputStarted() {
 		}
 
 		@Override
 		public boolean isAcceptingInput() {
 			return isRebinding;
 		}
 
 		@Override
 		public void setInput(Input input) {
 		}
 
 		@Override
 		public void keyReleased(int keyCode, char c) {
 			if (keyCode != Input.KEY_ESCAPE) {
 				setRebindKey(keyCode);
 			}
 		}
 	}
 
 }
