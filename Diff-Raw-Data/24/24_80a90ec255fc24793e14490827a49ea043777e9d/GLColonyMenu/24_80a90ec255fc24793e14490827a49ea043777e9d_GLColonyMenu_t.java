 package riskyspace.view.opengl.menu;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.media.opengl.GLAutoDrawable;
 
 import com.jogamp.opengl.util.awt.TextRenderer;
 
 import riskyspace.PlayerColors;
 import riskyspace.model.BuildAble;
 import riskyspace.model.Colony;
 import riskyspace.model.Player;
 import riskyspace.model.PlayerStats;
 import riskyspace.view.Action;
 import riskyspace.view.ViewResources;
 import riskyspace.view.opengl.Rectangle;
 import riskyspace.view.opengl.impl.GLButton;
 import riskyspace.view.opengl.impl.GLSprite;
 /**
  * 
  * @author Daniel Augurell
  * Menu showing information and options for a Colony
  */
 public class GLColonyMenu extends GLAbstractSideMenu{
 
 	/*
 	 * Strings to be printed on the menu
 	 */
 	
 	private Color ownerColor = null;
 	private Colony colony;
 	
 	private int margin;
 	
 	/*
 	 * Buttons
 	 */
 	private GLButton buildShipButton;
 	private GLButton buildingsButton;
 	
 	/*
 	 * Sub Menus
 	 */
 	private GLRecruitMenu recruitMenu;
 	private GLBuildingMenu buildingMenu;
 	
 	/*
 	 * Sprites
 	 */
 	private GLSprite colonyPicture = null;
 	private Map<Player, GLSprite> cities = new HashMap<Player, GLSprite>();
 	
 	/*
 	 * TextRenderer
 	 */
 	private TextRenderer nameRenderer = null;
 	private boolean initiated = false;
 	private int textX, textY;
 	
 	
 	public GLColonyMenu(int x, int y, int menuWidth, int menuHeight) {
 		super(x, y, menuWidth, menuHeight);
 		margin = menuHeight/20;
 		Action backAction = new Action() {
 			@Override public void performAction() {
 				setVisible(true);
 			}
 		};
 		recruitMenu = new GLRecruitMenu(x, y, menuWidth, menuHeight, backAction);
 		buildingMenu = new GLBuildingMenu(x, y, menuWidth, menuHeight, backAction);
 		int imageWidth = menuWidth - 2*margin;
 		int imageHeight = ((menuWidth - 2*margin)*3)/4;
 		setPicture(imageWidth, imageHeight);
 		setButtons();
 	}
 	
 	private void setButtons() {
 		buildShipButton = new GLButton(getX() + margin, 
 				getY() + getMenuHeight() - 2*(getMenuWidth() - 2*margin)/4 , 
 				getMenuWidth()-2*margin, (getMenuWidth() - 2*margin)/4);
 		buildShipButton.setTexture("menu/recruit", 128, 32);
 		buildShipButton.setAction(new Action(){
 			@Override
 			public void performAction() {
 				setVisible(false);
 				recruitMenu.setVisible(true);
 			}
 		});
 		buildingsButton = new GLButton(getX() + margin, 
 				getY() + getMenuHeight() - 3*(getMenuWidth() - margin)/4, 
 				getBounds().getWidth()-2*margin, (getBounds().getWidth() - 2*margin)/4);
 		buildingsButton.setTexture("menu/build", 128, 32);
 		buildingsButton.setAction(new Action(){
 			@Override
 			public void performAction() {
 				setVisible(false);
 				buildingMenu.setVisible(true);
 			}
 		});
 	}
 	
 	private void setPicture(int imageWidth, int imageHeight){
 		int sHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
 		Rectangle imageRenderRect = new Rectangle(getX() + margin, 
 				sHeight - (getY() + imageHeight + margin), 
 				imageWidth, imageHeight);
 
 		GLSprite sprite = new GLSprite("menu/city_red", 1280, 700);
 		sprite.setBounds(imageRenderRect);
 		cities.put(Player.RED, sprite);
 		sprite = new GLSprite("menu/city_blue", 900, 486);
 		sprite.setBounds(imageRenderRect);
 		cities.put(Player.BLUE, sprite);
 		sprite = new GLSprite("menu/city_yellow", 970, 594);
 		sprite.setBounds(imageRenderRect);
 		cities.put(Player.PINK, sprite);
 		sprite = new GLSprite("menu/city_green", 606, 389);
 		sprite.setBounds(imageRenderRect);
 		cities.put(Player.GREEN, sprite);
 	}
 	
 	public void setColony(Colony colony) {
 		this.colony = colony;
 		buildingMenu.setColony(colony);
 		recruitMenu.setColony(colony);
 		setMenuName(colony.getName());
 		ownerColor = PlayerColors.getColor(colony.getOwner());
 		colonyPicture = cities.get(colony.getOwner());
 	}
 
 	public Colony getColony() {
 		return colony;
 	}
 	
 	public void setStats(PlayerStats stats) {
 		recruitMenu.checkRecruitOptions(stats);
 		buildingMenu.setStats(stats);
 	}
 	
 	public void setQueues(Map<Colony, List<BuildAble>> colonyQueues) {
 		buildingMenu.setQueue(colonyQueues);
 	}
 	
 	@Override
 	public boolean mousePressed(Point p) {
 		if (recruitMenu.isVisible()) {
 			return recruitMenu.mousePressed(p);
 		} else if (buildingMenu.isVisible()) {
 			return buildingMenu.mousePressed(p);
 		} else if (isVisible()) {
 			if (buildShipButton.mousePressed(p)) {return true;}
 			if (buildingsButton.mousePressed(p)) {return true;}
 			if (this.contains(p)) {return true;}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean mouseReleased(Point p) {
 		if (recruitMenu.isVisible()) {
 			recruitMenu.mouseReleased(p);
 		} else if (buildingMenu.isVisible()) {
 			buildingMenu.mouseReleased(p);
 		} else if (isVisible()) {
 			if (buildShipButton.mouseReleased(p)) {return true;}
 			if (buildingsButton.mouseReleased(p)) {return true;}
 		}
 		return false;
 	}
 	
 	@Override
 	public void draw(GLAutoDrawable drawable, Rectangle objectRect, Rectangle targetArea, int zIndex) {
 		if(recruitMenu.isVisible()){
 			recruitMenu.draw(drawable, objectRect, targetArea, zIndex);
 		} else if(buildingMenu.isVisible()){
 			buildingMenu.draw(drawable, objectRect, targetArea, zIndex);
 		} else if(isVisible()){
 			super.draw(drawable, objectRect, targetArea, zIndex);
 			zIndex++;
 			colonyPicture.draw(drawable, colonyPicture.getBounds(), targetArea, zIndex);
 			buildingsButton.draw(drawable, null, targetArea, zIndex);
 			buildShipButton.draw(drawable, null, targetArea, zIndex);
 			drawMenuName(drawable);
 		} 
 	}
 	
 	private void drawMenuName(GLAutoDrawable drawable) {
 		if(!initiated){
 			nameRenderer = new TextRenderer(ViewResources.getFont().deriveFont((float)getMenuHeight()/20));
 			textX = getX() - ((int)nameRenderer.getBounds(getMenuName()).getWidth() / 2) + (getMenuWidth() / 2);
 			textY = getMenuHeight() - ((int)nameRenderer.getBounds(getMenuName()).getHeight() / 2) - (2*margin + colonyPicture.getBounds().getHeight());
			initiated = true;
 		}
 		nameRenderer.beginRendering(drawable.getWidth(), drawable.getHeight());
 		nameRenderer.setColor(ownerColor);
 		nameRenderer.draw(getMenuName(), textX, textY);
 		nameRenderer.setColor(1, 1, 1, 1);
 		nameRenderer.endRendering();
 	}
 
 	@Override
 	public void setVisible(boolean enabled) {
 		super.setVisible(enabled);
 		recruitMenu.setVisible(false);
 		buildingMenu.setVisible(false);
 	}
 	
 	@Override
 	public boolean isVisible() {
 		return super.isVisible() || recruitMenu.isVisible() || buildingMenu.isVisible();
 	}
 }
