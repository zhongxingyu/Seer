 package arcane_arcade_menus;
 
 import java.awt.Graphics2D;
 import java.awt.Point;
 
 import worlds.Room;
 
 import listeners.AdvancedMouseListener;
 import listeners.RoomListener;
 import drawnobjects.DrawnObject;
 import arcane_arcade_main.GameSettings;
 import arcane_arcade_worlds.GamePhase;
 import arcane_arcade_worlds.Navigator;
 import graphic.MaskChecker;
 import graphic.SpriteDrawer;
 import handlers.DrawableHandler;
 import handlers.MouseListenerHandler;
 import helpAndEnums.DepthConstants;
 
 /**
  * 
  * @author Unto Solala
  * 			Created 3.9.2013
  *
  */
 public class MainMenuMenuCreator {
 	
 	//CONSTRUCTOR---------------------------------------------------
 	/**
 	 * 
 	 * @param drawer
 	 * @param mousehandler
 	 * @param room
 	 * @param navigator
 	 */
 	public MainMenuMenuCreator(DrawableHandler drawer, 
 			MouseListenerHandler mousehandler, Room room, Navigator navigator)
 	{
 		//Let's create the four menuElements
 		new MainMenuElement(MainMenuElement.UP, drawer, mousehandler, room, navigator);
 		new MainMenuElement(MainMenuElement.RIGHT, drawer, mousehandler, room, navigator);
 		new MainMenuElement(MainMenuElement.DOWN, drawer, mousehandler, room, navigator);
 		new MainMenuElement(MainMenuElement.LEFT, drawer, mousehandler, room, navigator);
 		
 	}
 
 	
 	
 	/**MainMenuElements are the four main buttons in the MainMenu-screen. The 
 	 * buttons take the user to another phase within the game and a new screen.
 	 * 
 	 * @author Unto Solala
 	 * 			Created 3.9.2013
 	 *
 	 */
 	private class MainMenuElement extends DrawnObject implements AdvancedMouseListener, RoomListener{
 		
 		private static final int UP = 0;
 		private static final int RIGHT = 1;
 		private static final int DOWN = 2;
 		private static final int LEFT = 3;
 		//ATTRIBUTES-----------------------------------------------------
 		private int direction;
 		private MaskChecker maskchecker;
 		private SpriteDrawer spritedrawer;
 		private GamePhase gamephase;
 		private boolean active;
 		private Point startposition;
 		private Navigator navigator;
 		
 		/**Constructs one of the buttons in the MainMenu.
 		 * 
 		 * @param direction	Direction determines where the button is located.
 		 * There are four possible locations: UP, RIGHT, DOWN and LEFT.
 		 * @param drawer	The drawer that will draw the menu corner
 		 * @param mousehandler	The mouselistenerhandler that will inform the 
 		 * corner about mouse events
 		 * @param room	The room where the corner is created at
 		 * @param navigator	Navigator is needed for moving between the gamePhases
 		 */
 		public MainMenuElement(int direction, DrawableHandler drawer, 
 				MouseListenerHandler mousehandler, Room room, Navigator navigator) {
 			super(0, 0, DepthConstants.NORMAL, drawer);
 			//We need a couple of new variables for construction
 			String spriteName = new String();
 			int x=GameSettings.SCREENWIDTH/2;
 			int y=GameSettings.SCREENHEIGHT/2;
 			switch (this.direction) 
 			{
 				case UP: 
 				{
 					spriteName = "play";
 					// Let's move the MenuElement up a bit
 					y = y - 50;
 					this.gamephase = GamePhase.BATTLESETTINGMENU;
 					break;
 				}
 				case RIGHT: 
 				{
 					spriteName = "options";
 					// Let's move the MenuElement to the right a bit
 					x = x + 50;
 					this.gamephase = GamePhase.OPTIONSMENU;
 					break;
 				}
 				case DOWN: 
 				{
 					spriteName = "spellbook";
 					// Let's move the MenuElement down a bit
 					y = y + 50;
 					this.gamephase = GamePhase.SPELLBOOKMENU;
 					break;
 				}
 				case LEFT: 
 				{
 					spriteName = "tutorial";
 					// Let's move the MenuElement to the left a bit
 					x = x - 50;
 					this.gamephase = GamePhase.TUTORIALMENU;
 					break;
 				}
 			}
 			//Let's set the position for our MenuElement
 			this.setPosition(x, y);
 			this.startposition = new Point(x,y);
 			// Let's initialize rest of the attributes
 			this.spritedrawer = new SpriteDrawer(Navigator.getSpriteBank(
 					"menu").getSprite(spriteName), null);
 			this.spritedrawer.inactivate();
 			this.maskchecker = new MaskChecker(Navigator.getSpriteBank(
 					"menu").getSprite(spriteName+"mask"));
 			this.active = true;
 			this.navigator = navigator;
 			// Adds the object to the handlers
 			if (mousehandler != null)
 				mousehandler.addMouseListener(this);
 			if (room != null)
 				room.addOnject(this);
 		}
 
 		// IMPLEMENTENTED METHODS	------------------------------------------
 
 		@Override
 		public int getOriginX()
 		{
 			if (this.spritedrawer == null)
 				return 0;
 			return this.spritedrawer.getSprite().getOriginX();
 		}
 
 		@Override
 		public int getOriginY()
 		{
 			if (this.spritedrawer == null)
 				return 0;
 			return this.spritedrawer.getSprite().getOriginY();
 		}
 
 		@Override
 		public void drawSelfBasic(Graphics2D g2d)
 		{
 			// Draws the sprite
 			if (this.spritedrawer != null)
 				this.spritedrawer.drawSprite(g2d, 0, 0);
 		}
 
 		@Override
 		public boolean isActive()
 		{
 			return this.active;
 		}
 
 		@Override
 		public void activate()
 		{
 			this.active = true;
 		}
 
 		@Override
 		public void inactivate()
 		{
 			this.active = false;
 		}
 
 		@Override
 		public void onLeftDown(int mouseX, int mouseY)
 		{
 			// Does nothing
 		}
 
 		@Override
 		public void onRightDown(int mouseX, int mouseY)
 		{
 			// Does nothing
 		}
 
 		@Override
 		public void onLeftPressed(int mouseX, int mouseY)
 		{
 			//Starts the correct gamePhase
 			this.navigator.startPhase(this.gamephase);
 		}
 
 		@Override
 		public void onRightPressed(int mouseX, int mouseY)
 		{
 			// Does nothing
 		}
 
 		@Override
 		public void onLeftReleased(int mouseX, int mouseY)
 		{
 			// Does nothing
 		}
 
 		@Override
 		public void onRightReleased(int mouseX, int mouseY)
 		{
 			// Does nothing
 		}
 
 		@Override
 		public boolean listensPosition(int x, int y)
 		{
 			// If currently has no mask, doesn't listen to the mouse
 			if (this.maskchecker == null)
 				return false;
 			// Checks mask collision
 			Point relpoint = negateTransformations(x, y);
 			return this.maskchecker.maskContainsRelativePoint(relpoint, 0);
 		}
 
 		@Override
 		public boolean listensMouseEnterExit()
 		{
 			return true;
 		}
 
 		@Override
 		public void onMouseEnter(int mouseX, int mouseY)
 		{
 			//Moves the MenuElement slightly when the mouse enters
 			switch (this.direction) 
 			{
 				case UP: 
 				{
 					this.setPosition(this.startposition.getX(), this.startposition.getY()-50);
 					break;
 				}
 				case RIGHT: 
 				{
 					this.setPosition(this.startposition.getX()+50, this.startposition.getY());
 					break;
 				}
 				case DOWN: 
 				{
 					this.setPosition(this.startposition.getX(), this.startposition.getY()+50);
 					break;
 				}
 				case LEFT: 
 				{
 					this.setPosition(this.startposition.getX()-50, this.startposition.getY());
 					break;
 				}
 			}
 		}
 
 		@Override
 		public void onMouseOver(int mouseX, int mouseY)
 		{
 			// Does nothing
 		}
 
 		@Override
 		public void onMouseExit(int mouseX, int mouseY)
 		{
 			//Resets the position of the MenuElement
 			this.setPosition(this.startposition.getX(), this.startposition.getY());
 		}
 
 		@Override
 		public void onMouseMove(int mouseX, int mouseY)
 		{
 			// Does nothing
 		}
 
 		@Override
 		public void onRoomStart(Room room)
 		{
 			// Does nothing
 		}
 
 		@Override
 		public void onRoomEnd(Room room)
 		{
 			// Dies
 			kill();
 		}
 		
 		@Override
 		public void kill()
 		{
 			// Kills the spritedrawer and maskchecker
 			this.spritedrawer.kill();
 			this.spritedrawer = null;
 			this.maskchecker = null;
 			super.kill();
 		}
 	}
 }
