 package renderer;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Graphics;
 import java.awt.MediaTracker;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.Point2D;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.SwingUtilities;
 
 import playable.TypeOfPlayer;
 import renderer.sprite.BaseSprite;
 import renderer.sprite.ProjectileSprite;
 import renderer.sprite.Sprite;
 import renderer.sprite.TowerSprite;
 import renderer.sprite.UnitSprite;
 import dispatcher.Dispatcher;
 import dispatcher.TypeOfTower;
 import engine.Base;
 import engine.Projectile;
 import engine.Unit;
 import engine.tower.Tower;
 
 
 /**
  * MapRenderer is the specific renderer which manage the displaying of the pure game elements, like background, bases and agents.
  * @author Jijidici
  *
  */
 public class MapRenderer implements MouseListener{
 	/**
 	 * background : the background layer. No event connected on it. 
 	 */
 	private JLabel background;
 	/**
 	 * effectsLayer : a transparent layer, above the background layer. We connect events on it.
 	 */
 	private JLabel effectsLayer;
 
 	private Container container;
 	private int width;
 	private int height;
 	private final ArrayList<Sprite> sprites;
 	/**
 	 * selectionStartingCorner and selectionEndingCorner are static variable, useful to select several bases.
 	 */
 	private static Point2D.Float selectionStartingCorner;
 	private static Point2D.Float selectionEndingCorner;
 	
 	private static boolean mouseDown = false;
 	
 	/**
 	 * Constructor which asking the frame container in which the game elements will be rendered. Il also ask the frame dimensions
 	 * @param c main parent container
 	 * @param width window width
 	 * @param height window height
 	 */
 	@SuppressWarnings("serial")
 	public MapRenderer(Container c, int width, int height){
 		super();
 		
 		this.background = new JLabel();
 		this.container = c;
 		this.width = width;
 		this.height = height;
 		this.sprites = new ArrayList<Sprite>(5);
 		
 		this.effectsLayer = new JLabel() {
 			@Override
 			public void paintComponent(Graphics g){
 				super.paintComponent(g);
 				
 				this.setVisible(false);
 				// draw the line between bases
 				if(BaseSprite.isThereAtLeastOneStartingElement()){
 					g.setColor(Color.WHITE);
 					Point mousePosition = MouseInfo.getPointerInfo().getLocation();
 					SwingUtilities.convertPointFromScreen(mousePosition, this);
 					for(Base startingBase:BaseSprite.getStartingBases()){
 						g.drawLine((int)startingBase.getCenter().getX(), (int)startingBase.getCenter().getY(), 
 							mousePosition.x, mousePosition.y);
 					}
 				}
 				//draw the selection rectangle 
 				if(MapRenderer.mouseDown == true){
 					Point mousePosition = MouseInfo.getPointerInfo().getLocation();
 					SwingUtilities.convertPointFromScreen(mousePosition, this);
 					g.drawRect(
 						Math.min(
 							(int)MapRenderer.selectionStartingCorner.x,
 							mousePosition.x 
 						),	
 						Math.min(	
 							(int)MapRenderer.selectionStartingCorner.y,
 							mousePosition.y 
 						), 
 						Math.abs(
 							mousePosition.x 
 							- (int)MapRenderer.selectionStartingCorner.x
 						), 
 						Math.abs(	
 							mousePosition.y
 							- (int)MapRenderer.selectionStartingCorner.y
 						)
 					);
 				}
 				
 				//draw the highlighted Tower range
 				if(TowerSprite.getHighlightedTower() != null){
 					if(TowerSprite.getHighlightedTower().getEngineTower().getLevel() > 0){
 						Tower hlEngineTower = TowerSprite.getHighlightedTower().getEngineTower();
 						g.setColor(Color.GRAY);
 						g.drawOval((int)(hlEngineTower.getCenter().x - hlEngineTower.getVision()),
 								   (int)(hlEngineTower.getCenter().y - hlEngineTower.getVision()), 
 								   hlEngineTower.getVision()*2,
 								   hlEngineTower.getVision()*2);
 					}
 				}
 				this.setVisible(true);
 			}
 		};
 		
 		MapRenderer.selectionStartingCorner = new Point2D.Float();
 		MapRenderer.selectionEndingCorner = new Point2D.Float();
 	}
 	
 	/**
 	 * Initialize the MapRenderer by building every Swing resource and loading every image.
 	 * @throws IOException
 	 */
 	public void init() throws IOException{
 		//Load the background image
 		ImageIcon bgImage = new ImageIcon("./tex/background/background.jpg");
 		if(bgImage.getImageLoadStatus() != MediaTracker.COMPLETE){
 			throw new IOException();
 		}		
 		this.background.setBounds(0, 0, this.width, this.height);
 		this.background.setIcon(bgImage);
 		this.container.add(this.background, Layer.BACKGROUND.id());
 		
 		this.effectsLayer.setBounds(0, 0, this.width, this.height);
 		this.container.add(this.effectsLayer, Layer.EFFECT.id());
 		
 		//Manage events
 		this.background.addMouseListener(this);
 	}
 	
 	/**
 	 * Add a Base to the Sprite collection in the renderer.
 	 * @param newBase : the base engine, corresponding to the next created sprite.
 	 */
 	public int addBaseSprite(Base newBase){
 		BaseSprite newSprite = new BaseSprite(newBase);
 		newSprite.setSize(newBase.getCapacity());
 		//set the image of the base
 		if(newBase.isANeutralBase())
 			newSprite.setImage(TypeOfPlayer.NEUTRAL.getImageOfBase());
 		else if(newBase.getOwner().getType() == TypeOfPlayer.IA_1)
 			newSprite.setImage(TypeOfPlayer.IA_1.getImageOfBase());
 		else if(newBase.getOwner().getType() == TypeOfPlayer.IA_2)
 			newSprite.setImage(TypeOfPlayer.IA_2.getImageOfBase());
 		else if(newBase.getOwner().getType() == TypeOfPlayer.PLAYER)
 			newSprite.setImage(TypeOfPlayer.PLAYER.getImageOfBase());
 		else
 			newSprite.setImage(TypeOfPlayer.NEUTRAL.getImageOfBase());
 		newSprite.setBounds(newBase.getXCoord(), newBase.getYCoord(), newBase.getCapacity(), newBase.getCapacity());
 		container.add(newSprite, Layer.BASE.id());
 		sprites.add(newSprite);
 		return newSprite.getId();
 	}
 	
 	/**
 	 * Add a unit to the Sprite collection in the renderer.
 	 * @param newUnit : the unit engine, corresponding to the next created sprite.
 	 */
 	public int addUnitSprite(Unit newUnit){
 		UnitSprite newSprite = new UnitSprite(newUnit);
 		newSprite.setSize((int) newUnit.getNbAgents());
 		//set the image of the unit
 		if(newUnit.getOwner().getType() == TypeOfPlayer.PLAYER)
 			newSprite.setImage(TypeOfPlayer.PLAYER.getImageOfUnit());
 		else if(newUnit.getOwner().getType() == TypeOfPlayer.IA_1)
 			newSprite.setImage(TypeOfPlayer.IA_1.getImageOfUnit());
 		else if(newUnit.getOwner().getType() == TypeOfPlayer.IA_2)
 			newSprite.setImage(TypeOfPlayer.IA_2.getImageOfUnit());
 		
 		newSprite.setBounds((int)(newUnit.getPosition().x - newSprite.getSpriteSize()/2), (int)(newUnit.getPosition().y - newSprite.getSpriteSize()/2), newSprite.getSpriteSize(), newSprite.getSpriteSize());
 
 		container.add(newSprite, Layer.UNIT.id());
 		sprites.add(newSprite);
 		return newSprite.getId();
 	}
 	
 	/**
 	 * Add a tower to the Sprite collection in the renderer.
 	 * @param newTower : the tower engine, corresponding to the next created sprite.
 	 */
 	public int addTowerSprite(Tower newTower){
 		TowerSprite newSprite = new TowerSprite(newTower);
 		//set the image of the tower
 		newSprite.setImage(TypeOfPlayer.NEUTRAL.getImageOfTowerLvl0());
 		newSprite.setBounds((int)newTower.getPosition().x, (int)newTower.getPosition().y, newSprite.getSpriteSize(), newSprite.getSpriteSize());
 		container.add(newSprite, Layer.TOWER.id());
 		newSprite.initSubSprite();
 		sprites.add(newSprite);
 		return newSprite.getId();
 	}
 	
 	/**
 	 * Add a unit to the Sprite collection in the renderer.
 	 * @param newUnit the unit engine, corresponding to the next created sprite.
 	 * @param type the type of tower which have created the projectile
 	 */
 	public int addProjectileSprite(Projectile newProjectile, TypeOfTower type){
 		ProjectileSprite newSprite = new ProjectileSprite(newProjectile);
 		//we need to choose the right sprite of projectile
 		newSprite.setImage(type.getProjectileSprite());
 		
 		newSprite.setBounds((int)(newProjectile.getPosition().x - newSprite.getSpriteSize()/2), (int)(newProjectile.getPosition().y - newSprite.getSpriteSize()/2), newSprite.getSpriteSize(), newSprite.getSpriteSize());
 
 		container.add(newSprite, Layer.UNIT.id());
 		sprites.add(newSprite);
 		return newSprite.getId();
 	}
 	
 	/**
 	 * Remove Sprite from the Sprite list
 	 * @param idSprite the id of the sprite to remove
 	 */
 	public void removeSprite(int idSprite){
 		for(Sprite s:this.sprites){
 			if(s.getId() == idSprite){
 				this.sprites.remove(s);
 				this.container.remove(s);
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Update the TowerSprite of a newly spacialized Tower in the Sprite Array
 	 * @param newTower The new specialized Tower
 	 * @param oldId ID of the old non-specialized Tower
 	 */
 	public void updateTowerSprite(Tower newTower, int oldID){
 		for(Sprite s:this.sprites){
 			if(s.getId() == oldID){
 				TowerSprite towerToChange = (TowerSprite) s;
 				towerToChange.setId(newTower.getId());
 				towerToChange.setEngineTower(newTower);
 				towerToChange.updateSubSprite(TowerSprite.getChosenTowerType());
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Hide the sprites
 	 */
 	public void hideSprites(){
 		this.container.removeAll();
 		this.container.add(this.background, Layer.BACKGROUND.id());
 		this.container.add(this.effectsLayer, Layer.EFFECT.id());
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		BaseSprite.resetStartingElements();
 		BaseSprite.resetEndingElement();
 		TowerSprite.resetTowerToBuild();
 		Dispatcher.getRenderer().hideRadialMenus();
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		mouseDown = true;
         //initThread();
 		if(MapRenderer.selectionStartingCorner==null){
 			MapRenderer.selectionStartingCorner=new Point2D.Float();
 		}
 		MapRenderer.selectionStartingCorner.x=(float)arg0.getX();
 		MapRenderer.selectionStartingCorner.y=(float)arg0.getY();
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		mouseDown = false;
 		if(selectionStartingCorner != null){
 			if(MapRenderer.selectionEndingCorner==null)
 				MapRenderer.selectionEndingCorner=new Point2D.Float();
 			
 			MapRenderer.selectionEndingCorner.x=arg0.getX();
 			MapRenderer.selectionEndingCorner.y=arg0.getY();
 			BaseSprite.setStartingBases(selectionStartingCorner, selectionEndingCorner);
 		}
 		
 	}
 	
 	// GETTERS & SETTERS
 
 	/**
 	 * Get the sprite by knowledge of his id.
 	 * @param id : the id of the sprite.
 	 */
 	public Sprite getSprite(int id) {
 		for(Sprite sprite : this.sprites) {
 			if(sprite.getId() == id)
 				return sprite;
 		}
 		return null;
 	}
 
 	/**
 	 * Get all the sprites.
 	 * @return ArrayList<Sprite>
 	 */
 	public ArrayList<Sprite> getSprites() {
 		return this.sprites;
 	}
 
 	/**
 	 * Get only the unitSprites.
 	 * @return ArrayList<UnitSprite>
 	 */
 	public ArrayList<UnitSprite> getUnitSprites() {
 		ArrayList<UnitSprite> res = new ArrayList<UnitSprite>();
 		for(Sprite sprite:this.sprites){
 			if(sprite.getClass() == UnitSprite.class){
 				res.add((UnitSprite) sprite);
 			}
 		}
 		return res;
 	}
 
 	/**
 	 * Get only the baseSprites.
 	 * @return ArrayList<UnitSprite>
 	 */
 	public ArrayList<BaseSprite> getBaseSprites() {
 		ArrayList<BaseSprite> res = new ArrayList<BaseSprite>();
 		for(Sprite sprite:this.sprites){
 			if(sprite.getClass() == BaseSprite.class){
 				res.add((BaseSprite) sprite);
 			}
 		}
 		return res;
 	}
 
 	/**
 	 * Get only the towerSprites.
 	 * @return ArrayList<UnitSprite>
 	 */
 	public ArrayList<TowerSprite> getTowerSprites() {
 		ArrayList<TowerSprite> res = new ArrayList<TowerSprite>();
 		for(Sprite sprite:this.sprites){
 			if(sprite.getClass() == TowerSprite.class){
 				res.add((TowerSprite) sprite);
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * @return the container
 	 */
 	public Container getContainer() {
 		return container;
 	}
 	
 	/**
 	 * Change the Icon of background JLabel 
 	 * @param img new background Icon
 	 */
 	public void setBackgroundImage(ImageIcon img){
 		this.background.setIcon(img);
 	}
 	
 	/**
 	 * Static function.
 	 * @return selectionStartingCorner
 	 */
 	public static Point2D.Float getSelectionStartingCorner() {
 		return selectionStartingCorner;
 	}
 	
 	public static void resetSelectionStartingCorner(){
 		selectionStartingCorner = null;
 	}
 
 	/**
 	 * Static function.
 	 * @return selectionEndingCorner
 	 */
 	public static Point2D.Float getSelectionEndingCorner() {
 		return selectionEndingCorner;
 	}
 	
 	public static void resetSelectionEndingCorner(){
 		selectionEndingCorner = null;
 	}
 }
