 package asciiWorld.entities;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 
 import asciiWorld.Camera;
 import asciiWorld.CreateRectangle;
 import asciiWorld.Direction;
 import asciiWorld.math.Vector3f;
 import asciiWorld.ui.HUDView;
 import asciiWorld.ui.InventoryView;
 import asciiWorld.ui.RootVisualPanel;
 import asciiWorld.ui.WindowPanel;
 
 public class PlayerControlComponent extends InputAwareComponent {
 	
 	private static final int KEY_MOVE_NORTH = Input.KEY_W;
 	private static final int KEY_MOVE_SOUTH = Input.KEY_S;
 	private static final int KEY_MOVE_WEST = Input.KEY_A;
 	private static final int KEY_MOVE_EAST = Input.KEY_D;
 	private static final int KEY_TOUCH = Input.KEY_SPACE;
 	private static final int KEY_INVENTORY = Input.KEY_ESCAPE;
 	private static final int KEY_CONSOLE = Input.KEY_GRAVE;
 	
 	private WindowPanel _inventoryUI;
 	private Direction _movingDirection;
 	private Camera _camera;
 	private HotKeyManager _hotkeys;
 	private HUDView _hud;
 
 	public PlayerControlComponent(Entity owner, Camera camera) {
 		super(owner);
 		_inventoryUI = null;
 		_movingDirection = null;
 		_camera = camera;
 		_hotkeys = new HotKeyManager(getOwner());
 	}
 	
 	public HotKeyManager getHotKeyManager() {
 		return _hotkeys;
 	}
 	
 	public HUDView getHUD() {
 		return _hud;
 	}
 
 	public void setHUD(HUDView value) {
 		_hud = value;
 	}
 	
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int deltaTime) {
 		super.update(container, game, deltaTime);
 		
 		if (!isAcceptingInput()) {
 			_movingDirection = null;
 		}
 		
 		if (_movingDirection != null) {
 			getOwner().move(_movingDirection);
 		}
 		
 		handleMouseWheel();
 	}
 	
 	@Override
 	public void itemWasGained(Entity item) {
 		for (HotKeyInfo info : getHotKeyManager()) {
 			try {
 				if (info.getItem() == null) {
 					info.setItem(item);
 					break;
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.err.println("Unable to assign item to hotkey.");
 			}
 		}
 		if (getOwner().getActiveItem() == null) {
 			getOwner().setActiveItem(item);
 		}
 	}
 	
 	@Override
 	public void itemWasLost(Entity item) {
 		if (getOwner().getActiveItem() == null) {
 			for (HotKeyInfo info : getHotKeyManager()) {
 				try {
 					if (info.getItem() != null) {
 						getOwner().setActiveItem(info.getItem());
 						break;
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 					System.err.println("Unable to assign a new active item.");
 				}
 			}
 		}
 	}
 	
 	@Override
 	public boolean isAcceptingInput() {
 		if (isInventoryUIOpen()) {
 			return false;
 		}
 		return true;
 	}
 	
 	private Boolean isAcceptingMouseInput() {
 		try {
			return !RootVisualPanel.get().isModalWindowOpen() && (RootVisualPanel.get().findMouseHover() instanceof HUDView);
 		} catch (Exception e) {
 			return true;
 		}
 	}
 	
 	private boolean isConsoleOpen() {
 		if (getHUD() != null) {
 			return getHUD().isConsoleOpen();
 		}
 		return false;
 	}
 	
 	private void toggleScriptingConsole() {
 		if (getHUD() != null) {
 			if (!getHUD().isConsoleOpen()) {
 				getHUD().showConsole();
 			} else {
 				getHUD().hideConsole();
 			}
 		}
 	}
 	
 	@Override
 	public void keyPressed(int key, char c) {
 		if (key == KEY_CONSOLE) {
 			toggleScriptingConsole();
 		} else if (!isConsoleOpen()) {		
 			switch (key) {
 			case KEY_MOVE_NORTH:
 				_movingDirection = Direction.North;
 				break;
 			case KEY_MOVE_SOUTH:
 				_movingDirection = Direction.South;
 				break;
 			case KEY_MOVE_EAST:
 				_movingDirection = Direction.East;
 				break;
 			case KEY_MOVE_WEST:
 				_movingDirection = Direction.West;
 				break;
 			case KEY_TOUCH:
 				getOwner().touch();
 				break;
 			case KEY_INVENTORY:
 				openInventoryInterface();
 				break;
 			default:
 				for (HotKeyInfo info : getHotKeyManager()) {
 					if (info.getKeyboardKey() == key) {
 						try {
 							getOwner().setActiveItem(info.getItem());
 						} catch (Exception e) {
 							e.printStackTrace();
 							System.err.println("Unable to select the hot-key item.");
 						}
 						break;
 					}
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void keyReleased(int key, char c) {
 		switch (key) {
 		case KEY_MOVE_NORTH:
 		case KEY_MOVE_SOUTH:
 		case KEY_MOVE_EAST:
 		case KEY_MOVE_WEST:
 			_movingDirection = null;
 			break;
 		}
 	}
 	
 	@Override
 	public void mouseClicked(int button, int x, int y, int clickCount) {
 		if (isAcceptingMouseInput()) {
 			switch (button) {
 			case Input.MOUSE_LEFT_BUTTON:
 				try {
 					getOwner().useActiveItem(getChunkPointAtMousePosition(x, y));
 				} catch (Exception e) {
 					e.printStackTrace();
 					System.err.println("Unable to use the active item.");
 				}
 				break;
 			}
 		}
 	}
 	
 	private void handleMouseWheel() {
 		int mouseWheelValue = Mouse.getDWheel();
 		if (mouseWheelValue < 0) {
 			getHotKeyManager().activatePreviousItem();
 		} else if (mouseWheelValue > 0) {
 			getHotKeyManager().activateNextItem();
 		}
 	}
 	
 	private Vector3f getChunkPointAtMousePosition(int x, int y) {
 		return new Vector3f(_camera.screenPositionToChunkPoint(x, y), getOwner().getPosition().z);
 	}
 	
 	private Boolean isInventoryUIOpen() {
 		return (_inventoryUI != null) && !_inventoryUI.isClosed();
 	}
 	
 	private void openInventoryInterface() {
 		try {
 			if (isInventoryUIOpen() || RootVisualPanel.get().isModalWindowOpen()) {
 				return;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		try {
 			InventoryContainer inventory = getOwner().getInventory();
 			createInventoryWindow(inventory);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println("Unable to open the inventory interface.");
 		}
 	}
 	
 	private void createInventoryWindow(InventoryContainer inventory) throws Exception {
 		RootVisualPanel root = RootVisualPanel.get();
 		
 		Rectangle bounds = CreateRectangle
 				.from(root.getBounds())
 				.scale(4f / 5f)
 				.centerOn(root.getBounds())
 				.getRectangle();
 		
 		_inventoryUI = new WindowPanel(bounds, "Inventory");
 		_inventoryUI.setWindowContent(new InventoryView(inventory, getHotKeyManager()));
 		
 		root.addModalChild(_inventoryUI);
 	}
 }
