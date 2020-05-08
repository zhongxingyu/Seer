 package se.chalmers.kangaroo.view;
 
 import java.awt.Color;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.beans.PropertyChangeSupport;
 import java.util.HashMap;
 
 import javax.swing.ImageIcon;
 
 import se.chalmers.kangaroo.constants.Constants;
 import se.chalmers.kangaroo.model.GameModel;
 import se.chalmers.kangaroo.model.creatures.Creature;
 import se.chalmers.kangaroo.model.iobject.InteractiveObject;
 import se.chalmers.kangaroo.model.kangaroo.Item;
 import se.chalmers.kangaroo.model.utils.Position;
 
 /**
  * This is the normal view which renders the model.
  * 
  * @author alburgh
  * @modifiedby simonal
  * @modifiedby arvidk
  */
 public class GameView extends JPanelWithBackground {
 	private GameModel gm;
 	private HashMap<Creature, Animation> creatureAnimations;
 	private KangarooAnimation ka;
 	private boolean isRunning = false;
 	private PauseView pv;
 	private VictoryView vv;
 	private boolean newLevel = false;
 	private PropertyChangeSupport pcs;
 
 	/**
 	 * Create the view when the rendering shall begin.
 	 * 
 	 * @param imagepath
 	 *            , the path name to the background
 	 * @param gm
 	 *            , the gamemodel it shall render
 	 * @param cv
 	 *            , the changeview in order for the menu to work.
 	 */
 	public GameView(String imagepath, GameModel gm, ChangeView cv) {
 		super(imagepath);
 		this.setFocusable(true);
 		this.requestFocus();
 		this.gm = gm;
 		this.pcs = new PropertyChangeSupport(this);
 		this.requestFocus();
 		initAnimations();
 		ka = new KangarooAnimation(gm.getKangaroo(), 58, 64);
 		pv = new PauseView("resources/gfx/misc/pausebackground.png", cv, this);
 		pv.setVisible(isRunning);
 		pv.setOpaque(isRunning);
 		this.add(pv);
 	}
 
 	/**
 	 * Use this when you want to initialize the Animations.
 	 */
 	public void initAnimations() {
 		creatureAnimations = new HashMap<Creature, Animation>();
 		AnimationFactory af = new AnimationFactory();
 		for (int i = 0; i < gm.getGameMap().getCreatureSize(); i++) {
 			Creature c = gm.getGameMap().getCreatureAt(i);
 			creatureAnimations.put(c, af.getAnimation(c));
 		}
 	}
 
 	@Override
 	public void paintComponent(java.awt.Graphics g) {
 		super.paintComponent(g);
 		Position p = gm.getKangaroo().getPosition();
 
 		int drawFrom = getLeftX();
 		int fixPosition = p.getX() / Constants.TILE_SIZE < 16
 				|| drawFrom == gm.getGameMap().getTileWidth() - 33 ? 0 : p
 				.getX() % 32;
 
 		// paintIcon(null, g
 		/* Render the tiles */
 		for (int y = 0; y < gm.getGameMap().getTileHeight(); y++)
 			for (int x = drawFrom; x < drawFrom + 33; x++) {
 				Image img = Toolkit.getDefaultToolkit().getImage(
 						"resources/gfx/tiles/tileChart.png");
 				g.drawImage(
 						img,
 						(x - drawFrom) * 32 - fixPosition,
 						(y - 2) * 32,
 						((x - drawFrom) * 32 - fixPosition) + 32,
 						((y - 2) * 32) + 32,
 						(((gm.getGameMap().getTile(x, y).getId() - 1) % 20)) * 32,
 						((gm.getGameMap().getTile(x, y).getId() - 1) / 20) * 32,
 						(((gm.getGameMap().getTile(x, y).getId() - 1) % 20)) * 32 + 32,
 						((gm.getGameMap().getTile(x, y).getId() - 1) / 20) * 32 + 32,
 						null, null);
 			}
 		/* Render the items */
 		for (int i = 0; i < gm.getGameMap().amountOfItems(); i++) {
 			Item item = gm.getGameMap().getItem(i);
 			if (item.getPosition().getX() > drawFrom
 					&& item.getPosition().getX() < drawFrom + 32) {
 				Image img = Toolkit.getDefaultToolkit().getImage(
 						"resources/gfx/tiles/tileChart.png");
 				g.drawImage(
 						img,
 						(item.getPosition().getX() - drawFrom) * 32 - fixPosition,
 						(item.getPosition().getY() - 2) * 32,
 						((item.getPosition().getX() - drawFrom) * 32 - fixPosition) + 32,
 						((item.getPosition().getY() - 2) * 32) + 32,
 						(((item.getId() - 1) % 20)) * 32,
 						((item.getId() - 1) / 20) * 32,
 						(((item.getId() - 1) % 20)) * 32 + 32,
 						((item.getId() - 1) / 20) * 32 + 32,
 						null, null);
 			}
 		}
 		/* Render the interactive objects */
 		for (int i = 0; i < gm.getGameMap().getIObjectSize(); i++) {
 			InteractiveObject io = gm.getGameMap().getIObject(i);
			if (io.getPosition().getX() > drawFrom - 1 
 					&& io.getPosition().getX() < drawFrom + 32) {
 				Image img = Toolkit.getDefaultToolkit().getImage(
 						"resources/gfx/tiles/tileChart.png");
 				g.drawImage(
 						img,
 						(io.getPosition().getX() - drawFrom) * 32 - fixPosition,
 						(io.getPosition().getY() - 2) * 32,
 						((io.getPosition().getX() - drawFrom) * 32 - fixPosition) + 32,
 						((io.getPosition().getY() - 2) * 32) + 32,
 						(((io.getId() - 1) % 20)) * 32,
 						((io.getId() - 1) / 20) * 32,
 						(((io.getId() - 1) % 20)) * 32 + 32,
 						((io.getId() - 1) / 20) * 32 + 32,
 						null, null);
 			}
 		}
 		/* Render the creatures */
 		for (int i = 0; i < gm.getGameMap().getCreatureSize(); i++) {
 			Creature c = gm.getGameMap().getCreatureAt(i);
			if (c.getPosition().getX() > drawFrom * 31
 					&& c.getPosition().getX() < (drawFrom + 32) * 32) {
 				int xP = c.getPosition().getX();
 				int yP = c.getPosition().getY();
 				if (creatureAnimations.containsKey(c)) {
 					creatureAnimations.get(c).drawSprite(g,
 							xP - drawFrom * 32 - fixPosition, yP - 64);
 				}
 			}
 		}
 		/* Draw the kangaroo based on where you are */
 		if (drawFrom == 0
 				&& gm.getKangaroo().getPosition().getX() / Constants.TILE_SIZE != 16) {
 			ka.drawSprite(g, p.getX(), p.getY());
 		} else if (drawFrom == gm.getGameMap().getTileWidth() - 33) {
 			ka.drawSprite(g, p.getX() - drawFrom * 32 - fixPosition, p.getY());
 		} else {
 			ka.drawSprite(g, p.getX() - drawFrom * 32 - fixPosition, p.getY());
 		}
 		g.setColor(Color.WHITE);
 		g.drawString("" + gm.getTime(), Constants.POS_CLOCK.width+1, Constants.POS_CLOCK.height+1);
 		g.drawString("Deaths: " + gm.getDeathCount(), Constants.POS_DEATH.width+1, Constants.POS_DEATH.height+1);
 		g.setColor(Color.BLACK);
 		g.drawString("" + gm.getTime(), Constants.POS_CLOCK.width, Constants.POS_CLOCK.height);
 		g.drawString("Deaths: " + gm.getDeathCount(), Constants.POS_DEATH.width, Constants.POS_DEATH.height);
 	}
 
 	/* Private method for calculate the left side to render */
 	private int getLeftX() {
 		int kPos = gm.getKangaroo().getPosition().getX() / Constants.TILE_SIZE;
 		if (kPos < 16)
 			return 0;
 		if (gm.getGameMap().getTileWidth() - kPos < 17)
 			return gm.getGameMap().getTileWidth() - 33;
 		return kPos - 16;
 	}
 
 	public boolean isRunning() {
 		return isRunning;
 	}
 
 	/**
 	 * When the game pauses a menu will appear.
 	 */
 	public void togglePause() {
 		this.isRunning = !isRunning;
 		pv.setVisible(!isRunning);
 		pv.setOpaque(!isRunning);
 		pv.revalidate();
 		pv.repaint();
 		pv.validate();
 		this.revalidate();
 		this.repaint();
 		this.validate();
 	}
 
 	public void showVictoryView() {
 		vv = new VictoryView("resources/gfx/misc/victory_background.png",
 				gm.getDeathCount(), gm.getTime(), this, gm.getLevel());
 		vv.setVisible(true);
 		vv.setOpaque(true);
 		this.add(vv);
 		this.repaint();
 		this.revalidate();
 		this.validate();
 		this.revalidate();
 	}
 
 	public void removeVictoryView() {
 		this.remove(vv);
 		newLevel = false;
 	}
 
 	public void setNewLevel(boolean b) {
 		newLevel = b;
 	}
 
 	public boolean startNewLevel() {
 		return newLevel;
 	}
 
 	public void start() {
 		isRunning = true;
 		pcs.firePropertyChange("start", 0, 1);
 	}
 
 	public PropertyChangeSupport getObserver() {
 		return pcs;
 	}
 }
