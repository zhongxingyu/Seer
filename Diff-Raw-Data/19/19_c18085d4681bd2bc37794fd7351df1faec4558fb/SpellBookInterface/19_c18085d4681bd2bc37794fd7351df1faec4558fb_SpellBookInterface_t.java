 package arcane_arcade_menus;
 
 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 import utopia_gameobjects.DrawnObject;
import utopia_graphic.SpriteDrawerObject;
 import utopia_graphic.ParagraphDrawer;
 import utopia_listeners.AdvancedMouseListener.MouseButton;
 import utopia_listeners.RoomListener;
 import utopia_resourcebanks.MultiMediaHolder;
 import utopia_utility.DepthConstants;
 import utopia_worlds.Area;
 import utopia_worlds.Room;
 import arcane_arcade_main.GameSettings;
 import arcane_arcade_spells.Spell;
 import arcane_arcade_status.Element;
 import arcane_arcade_status.ElementIndicator;
 import arcane_arcade_status.ElementIndicatorListener;
 
 /**
  * SpellBookInterface includes the guide system in the spellBook menu. In other 
  * words it shows the possible elements, allows the user to choose any spell 
  * by combining them together and provides information about that spell.
  * 
  * @author Mikko Hilpinen
  * @since 30.4.2014
  */
 public class SpellBookInterface implements ElementIndicatorListener, RoomListener
 {	
 	// ATTRIBUTES	------------------------------------------------------
 	
 	private ArrayList<ElementIndicator> indicators;
 	private ArrayList<SpriteDrawerObject> buffIndicators;
 	private ElementIndicator lastChosenIndicator;
 	private SpriteDrawerObject firstChosenMarker, secondChosenMarker;
 	private SpellInformationDrawer informationDrawer;
 	private boolean dead;
 	private Area area;
 	
 	
 	// CONSTRUCTOR	------------------------------------------------------
 	
 	/**
 	 * Creates a new SpellBookInterface
 	 * @param area The area where the interface will be created
 	 */
 	public SpellBookInterface(Area area)
 	{
 		// Initializes attributes
 		this.lastChosenIndicator = null;
 		this.indicators = new ArrayList<ElementIndicator>();
 		this.informationDrawer = new SpellInformationDrawer(area);
 		this.buffIndicators = new ArrayList<SpriteDrawerObject>();
 		this.dead = false;
 		this.area = area;
 		
 		createChosenMarker(area, false);
 		createChosenMarker(area, true);
 		
 		// Creates the Indicators
 		ElementIndicator.createIndicators(area, this);
 		
 		// Adds the interface into the area
 		area.addRoomListener(this);
 	}
 	
 	
 	// IMPLEMENTED METHODS	----------------------------------------------
 
 	@Override
 	public void onElementIndicatorPressed(MouseButton button,
 			ElementIndicator source)
 	{
 		// Moves the chosenMarkers
 		this.firstChosenMarker.setPosition(source.getPosition());
 		
 		if (this.lastChosenIndicator != null)
 		{	
 			this.secondChosenMarker.setPosition(this.lastChosenIndicator.getPosition());
 			
 			// Also updates the shown data
 			this.informationDrawer.setElements(this.lastChosenIndicator.getElement(), 
 					source.getElement());
 			
 			// And the buffs / debuffs
 			createBuffIndicators(this.lastChosenIndicator.getElement(), source.getElement());
 		}
 		
 		// Updates the last chosen indicator
 		this.lastChosenIndicator = source;
 	}
 
 	@Override
 	public void onElementIndicatorCreated(ElementIndicator newIndicator)
 	{
 		// Adds the new indicator to the list
 		this.indicators.add(newIndicator);
 	}
 	
 	@Override
 	public boolean isDead()
 	{
 		return this.dead;
 	}
 
 	@Override
 	public void kill()
 	{
 		// Kills the objects in the interface
 		killInterfaceElements();
 		this.dead = true;
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
 	
 	
 	// OTHER METHODS	--------------------------------------------------
 	
 	private void createChosenMarker(Area area, boolean isSecondary)
 	{
 		SpriteDrawerObject marker = new SpriteDrawerObject(area, 
 				DepthConstants.FOREGROUND, null, 
 				MultiMediaHolder.getSpriteBank("menu").getSprite("chosen2"));
 		
 		marker.setPosition(-400, -400);
 		
 		if (isSecondary)
 		{
 			marker.setScale(-1, -1);
 			this.secondChosenMarker = marker;
 		}
 		else
 			this.firstChosenMarker = marker;
 	}
 	
 	private void createBuffIndicators(Element element1, Element element2)
 	{
 		// First destroys the previous indicators
 		killBuffIndicators();
 		
 		Spell spell = element1.getSpell(element2);
 		
 		for (ElementIndicator indicator : this.indicators)
 		{
 			createBuffIndicator(spell, indicator);
 		}
 	}
 	
 	private void createBuffIndicator(Spell currentSpell, 
 			ElementIndicator elementIndicator)
 	{
 		// Checks what kind of buff is needed or if one isn't needed at all
 		double forceModifier = Element.getForceModifier(
 				currentSpell.getFirstEffectElement(), 
 				currentSpell.getSecondEffectElement(), 
 				elementIndicator.getElement().getCausedStatus(), 100);
 		
 		if (forceModifier > 0.9 && forceModifier < 1.1)
 			return;
 		
 		// Creates the buffIndicator
 		SpriteDrawerObject buffIndicator = new SpriteDrawerObject(this.area, 
 				DepthConstants.FOREGROUND, null, 
 				MultiMediaHolder.getSpriteBank("hud").getSprite("buffarrow"));
 		buffIndicator.getSpriteDrawer().setImageSpeed(0);
 		buffIndicator.goToRelativePosition(new Point2D.Double(50, 50), 
 				elementIndicator);
 		
 		// Sets the correct buff
 		if (forceModifier < 0.9)
 			buffIndicator.getSpriteDrawer().setImageIndex(1);
 		else
 			buffIndicator.getSpriteDrawer().setImageIndex(0);
 		
 		// Remembers the new indicator
 		this.buffIndicators.add(buffIndicator);
 	}
 	
 	private void killInterfaceElements()
 	{
 		// Kills the indicators
 		for (int i = 0; i < this.indicators.size(); i++)
 		{
 			this.indicators.get(i).kill();
 		}
 		this.indicators.clear();
 		
 		// Kills the markers
 		this.firstChosenMarker.kill();
 		this.secondChosenMarker.kill();
 		
 		// Kills the information drawer
 		this.informationDrawer.kill();
 		
 		// Also kills the buffindicators
 		killBuffIndicators();
 	}
 	
 	private void killBuffIndicators()
 	{
 		// Kills all the remaining buffindicators
 		for (int i = 0; i < this.buffIndicators.size(); i++)
 		{
 			this.buffIndicators.get(i).kill();
 		}
 		this.buffIndicators.clear();
 	}
 	
 	
 	// SUBCLASSES	------------------------------------------------------
 	
 	private class SpellInformationDrawer extends DrawnObject
 	{
 		// ATTRIBUTES	-------------------------------------------------
 		
 		private ParagraphDrawer infoDrawer;
 		private String spellName;
 		
 		
 		// CONSTRUCTOR	-------------------------------------------------
 		
 		public SpellInformationDrawer(Area area)
 		{
 			super(GameSettings.SCREENWIDTH / 2, GameSettings.SCREENHEIGHT / 2, 
 					DepthConstants.HUD, area);
 			
 			// TODO: Use a smaller font here
 			// Initializes attributes
 			this.spellName = "Help:";
 			this.infoDrawer = new ParagraphDrawer("Click two elements to for spell information.", 
 					GameSettings.BASICFONT, GameSettings.WHITETEXTCOLOR, 500, this);
 		}
 		
 		
 		// IMPLEMENTED METHODS	-----------------------------------------
 
 		@Override
 		public int getOriginX()
 		{
 			return 0;
 		}
 
 		@Override
 		public int getOriginY()
 		{
 			return 0;
 		}
 
 		@Override
 		public void drawSelfBasic(Graphics2D g2d)
 		{
 			// Draws the information
 			if (this.infoDrawer != null)
 				this.infoDrawer.drawText(g2d, -250, -75);
 			
 			g2d.setFont(GameSettings.BASICFONT);
 			g2d.setColor(GameSettings.WHITETEXTCOLOR);
 			
 			// Draws the spell name
 			g2d.drawString(this.spellName, -100, -100);
 		}
 		
 		
 		// OTHER METHODS	--------------------------------------------
 		
 		public void setElements(Element element1, Element element2)
 		{
 			Spell spell = element1.getSpell(element2);
 			this.spellName = spell.getName();
 			this.infoDrawer.setText(spell.getDescription());
 		}
 	}
 }
