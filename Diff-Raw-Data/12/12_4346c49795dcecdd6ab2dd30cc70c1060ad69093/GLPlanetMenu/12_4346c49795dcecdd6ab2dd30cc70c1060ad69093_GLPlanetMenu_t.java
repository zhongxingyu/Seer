 package riskyspace.view.opengl.menu;
 
 import java.awt.Point;
 
 import javax.media.opengl.GLAutoDrawable;
 
 import riskyspace.model.Fleet;
 import riskyspace.model.Resource;
 import riskyspace.model.Territory;
 import riskyspace.services.Event;
 import riskyspace.services.EventBus;
 import riskyspace.view.Action;
 import riskyspace.view.opengl.Rectangle;
 import riskyspace.view.opengl.impl.GLButton;
 import riskyspace.view.opengl.impl.GLSprite;
 
 public class GLPlanetMenu extends GLAbstractSideMenu{
 	
 	private int margin = 30;
 	
 	private GLSprite planetPicture = null;
 	private GLSprite metalPlanetPicture = null;
 	private GLSprite gasPlanetPicture = null;
 	
 	private GLButton colonizeButton;
 	
 	public GLPlanetMenu(int x, int y, int menuWidth, int menuHeight) {
 		super(x, y, menuWidth, menuHeight);
 		
		Rectangle renderRect = new Rectangle(x + margin, menuHeight - ((menuWidth - 2*margin)*3)/4 - margin - 15, menuWidth - 2*margin, ((menuWidth - 2*margin)*3)/4);
 		
 		metalPlanetPicture = new GLSprite("menu/metal_planet", 1024, 768);
 		metalPlanetPicture.setBounds(renderRect);
 		gasPlanetPicture = new GLSprite("res/menu/gas_planet", 350, 350);
 		gasPlanetPicture.setBounds(renderRect);
 		
 		planetPicture = metalPlanetPicture;
 		
		colonizeButton = new GLButton(x + margin, y + menuHeight - 2*(menuWidth - 2*margin)/4, menuWidth-2*margin, (menuWidth - 2*margin)/4);
 		colonizeButton.setTexture("menu/colonizeButton", 180, 50);
 		colonizeButton.setAction(new Action(){
 			@Override
 			public void performAction() {
 				Event evt = new Event(Event.EventTag.COLONIZE_PLANET, null);
 				EventBus.CLIENT.publish(evt);
 			}
 		});
 	}
 	
 	public void setTerritory(Territory selection) {
 		planetPicture = selection.getPlanet().getType() == Resource.METAL? metalPlanetPicture : gasPlanetPicture;
 		colonizeButton.setEnabled(false);
 		for (Fleet fleet : selection.getFleets()) {
 			if (fleet.hasColonizer()) {
 				colonizeButton.setEnabled(true);
 				break;
 			}
 		}
 	}
 	
 	@Override
 	public boolean mousePressed(Point p) {
 		/*
 		 * Only handle mouse event if enabled
 		 */
 		if (isVisible()) {
 			if (colonizeButton.mousePressed(p)) {return true;}
 			if (this.contains(p)) {return true;}
 			else {
 				return false;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean mouseReleased(Point p) {
 		/*
 		 * Only handle mouse event if enabled
 		 */
 		if (isVisible()) {
 			if (colonizeButton.mouseReleased(p)) {return true;}
 			else {
 				return false;
 			}
 		}
 		return false;
 	}
 	@Override
 	public void draw(GLAutoDrawable drawable, Rectangle objectRect, Rectangle targetArea, int zIndex) {
 		if (isVisible()) {
 			super.draw(drawable, objectRect, targetArea, zIndex);
 			zIndex++;
 			planetPicture.draw(drawable, planetPicture.getBounds(), targetArea, zIndex);
 			colonizeButton.draw(drawable, colonizeButton.getBounds(), targetArea, zIndex);
 		}
 	}
 }
