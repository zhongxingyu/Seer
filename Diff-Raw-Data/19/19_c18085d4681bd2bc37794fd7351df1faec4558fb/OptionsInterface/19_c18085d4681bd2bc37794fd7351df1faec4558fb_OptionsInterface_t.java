 package arcane_arcade_menus;
 
 import java.awt.Graphics2D;
 import java.awt.geom.Point2D.Double;
 
 import arcane_arcade_main.GameSettings;
 import arcane_arcade_main.Options;
 import utopia_graphic.Sprite;
 import utopia_utility.CollisionType;
 import utopia_utility.DepthConstants;
 import utopia_interfaceElements.AbstractButton;
 import utopia_interfaceElements.OptionMessageBox;
 import utopia_interfaceElements.SliderIntegerOptionBar;
 import utopia_interfaceElements.StringOptionBar;
 import utopia_resourcebanks.MultiMediaHolder;
 import utopia_worlds.Area;
 
 /**
  * OptionsIntercade handles the different option interface elements and 
  * the transactions between them. ie: creating elements and saving the settings.
  * 
  * @author Mikko Hilpinen
  * @since 20.4.2014
  */
 public class OptionsInterface
 {
 	// ATTRIBUTES	-----------------------------------------------------
 	
 	private StringOptionBar fullscreenBar;
	private SliderIntegerOptionBar wizardVolumeSlider, effectVolumeSlider, 
			maxPanSlider;
 	
 	
 	// CONSTRUCTOR	-----------------------------------------------------
 	
 	/**
 	 * Creates a new OptionsInterface to the given area
 	 * @param area The area where the interface elements will be placed
 	 */
 	public OptionsInterface(Area area)
 	{
 		int columnx = 200;
 		int columny = 100;
 		int ydifference = 50;
 		
 		// Initializes attributes
 		Sprite arrowSprite = MultiMediaHolder.getSpriteBank("menu").getSprite("arrow");
 		Sprite arrowMask = MultiMediaHolder.getSpriteBank("menu").getSprite("arrowmask");
 		String[] options = {"OFF", "ON"};
 		int fullscreenDefaultIndex = 0;
 		if (Options.fullscreenon)
 			fullscreenDefaultIndex = 1;
 		
 		this.fullscreenBar = new StringOptionBar(columnx, columny, options, 
 				fullscreenDefaultIndex, "'Fullscreen'", GameSettings.BASICFONT, 
 				GameSettings.WHITETEXTCOLOR, arrowSprite, arrowMask, area);
 		
 		Sprite sliderBackSprite = MultiMediaHolder.getSpriteBank("menu").getSprite("sliderback");
 		Sprite sliderHandleSprite = MultiMediaHolder.getSpriteBank("menu").getSprite("sliderhandle");
 		
 		//System.out.println("Wizard Voice: " + Options.voicevolumeadjustment);
 		//System.out.println("Effects: " + Options.soundvolumeadjustment);
 		
 		this.wizardVolumeSlider = new SliderIntegerOptionBar(columnx, 
 				columny + ydifference, Options.voicevolumeadjustment, -20, 20, 
 				"Voice volume adjustment", GameSettings.BASICFONT, 
 				GameSettings.WHITETEXTCOLOR, sliderBackSprite, sliderHandleSprite, 
 				area);
 		
 		this.effectVolumeSlider = new SliderIntegerOptionBar(columnx, 
 				columny + ydifference * 2, Options.soundvolumeadjustment, -20, 
 				20, "Sound effect volume adjustment", GameSettings.BASICFONT, 
 				GameSettings.WHITETEXTCOLOR, sliderBackSprite, sliderHandleSprite, 
 				area);
 		
		this.maxPanSlider = new SliderIntegerOptionBar(columnx, 
				columny + ydifference * 3, 50, 0, 100, "Maximum pan %", 
				GameSettings.BASICFONT, GameSettings.WHITETEXTCOLOR, 
				sliderBackSprite, sliderHandleSprite, area);
		
 		new ApplyChangesButton(GameSettings.SCREENWIDTH / 2, 
 				GameSettings.SCREENHEIGHT - 100, area);
 	}
 
 	// TODO: Add key rebind buttons and messageboxes
 	
 	
 	// SUBCLASSES	-----------------------------------------------------
 	
 	private abstract class OptionButton extends AbstractButton
 	{
 		// ATTRIBUTES	-------------------------------------------------
 		
 		private String description;
 		
 		
 		// CONSTRUCTOR	-------------------------------------------------
 		
 		public OptionButton(int x, int y, String description, Area area)
 		{
 			super(x, y, DepthConstants.HUD, 
 					MultiMediaHolder.getSpriteBank("menu").getSprite("button"), 
 					area);
 			
 			// Initializes attributes
 			this.description = description;
 			
 			// Changes collisions
 			setCollisionType(CollisionType.CIRCLE);
 		}
 		
 		
 		// ABSTRACT METHODS	--------------------------------------------
 		
 		protected abstract void onLeftClick();
 		
 		
 		// IMPLEMENTED METHODS	----------------------------------------
 
 		@Override
 		public void onMouseButtonEvent(MouseButton button,
 				MouseButtonEventType eventType, Double mousePosition,
 				double eventStepTime)
 		{
 			// Checks if left mouse button was pressed
 			if (button == MouseButton.LEFT && eventType == MouseButtonEventType.PRESSED)
 				onLeftClick();
 		}
 
 		@Override
 		public boolean listensMouseEnterExit()
 		{
 			return true;
 		}
 
 		@Override
 		public void onMousePositionEvent(MousePositionEventType eventType,
 				Double mousePosition, double eventStepTime)
 		{
 			if (eventType == MousePositionEventType.ENTER)
 				getSpriteDrawer().setImageIndex(1);
 			else if (eventType == MousePositionEventType.EXIT)
 				getSpriteDrawer().setImageIndex(0);
 		}
 		
 		@Override
 		public void drawSelfBasic(Graphics2D g2d)
 		{
 			super.drawSelfBasic(g2d);
 			
 			// Also draws the description
 			g2d.drawString(this.description, 100, 40);
 		}
 	}
 	
 	private class ApplyChangesButton extends OptionButton
 	{
 		// ATTRIBUTES	-------------------------------------------------
 		
 		private Area area;
 		
 		
 		// CONSTRUCTOR	-------------------------------------------------
 		
 		public ApplyChangesButton(int x, int y, Area area)
 		{
 			super(x, y, "Apply Changes", area);
 			
 			// Initializes attributes
 			this.area = area;
 		}
 		
 		
 		// IMPLEMENTED METHODS	-----------------------------------------
 
 		@Override
 		protected void onLeftClick()
 		{
 			// Adjusts the options and saves them
 			if (OptionsInterface.this.fullscreenBar.getValue() == 0)
 				Options.fullscreenon = false;
 			else
 				Options.fullscreenon = true;
 			
 			Options.voicevolumeadjustment = 
 					OptionsInterface.this.wizardVolumeSlider.getValue();
 			Options.soundvolumeadjustment = 
 					OptionsInterface.this.effectVolumeSlider.getValue();
			Options.maxenvironmentalpan = 
					OptionsInterface.this.maxPanSlider.getValue() / 100.0;
 			
 			Options.saveSettings();
 			
 			// Informs the user about the change
 			new OptionMessageBox(GameSettings.SCREENWIDTH / 2, 
 					GameSettings.SCREENHEIGHT / 2, DepthConstants.TOP, 32, 
 					"Changes successfully saved!", GameSettings.BASICFONT, 
 					GameSettings.WHITETEXTCOLOR, 
 					MultiMediaHolder.getSpriteBank("menu").getSprite("messageback"), 
 					OptionMessageBox.OKOPTIONS, 
 					MultiMediaHolder.getSpriteBank("menu").getSprite("button"), 
 					true, true, null, this.area);
 		}
 	}
 }
