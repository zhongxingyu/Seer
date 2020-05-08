 package it.wargame.gamestates;
 
 import it.wargame.Wargame;
import it.wargame.ai.AiInterface;
 import it.wargame.creatures.Creature;
 import it.wargame.events.AttackEvent;
 import it.wargame.events.MoveEvent;
 import it.wargame.events.NextTurnEvent;
 import it.wargame.map.GameMap;
 import it.wargame.ui.Button;
 import it.wargame.ui.NextTurnImageButton;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.google.common.eventbus.Subscribe;
 
 public class GameWorld extends BasicGameState {
 
 	private GameMap map;
 
 	private int mx;
 
 	private int my;
 
 	private int sx;
 
 	private int sy;
 
 	private Creature selectedCreature;
 
 	private Button nextTurn;
 
 	private Image moveImage;
 
 	private Image moveGrayImage;
 
 	private Image swordImage;
 
 	private Image swordGrayImage;
 
 	@Override
 	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
 		map = new GameMap(12).init().placeBlock(2, 2).placeBlock(2, 3).placeBlock(3, 2).placeBlock(3, 3);
 		map.placeUnits(this);
 		Wargame.eventBus.register(this);
 		loadImages();
 	}
 	private void loadImages() throws SlickException {
 		nextTurn = new NextTurnImageButton(320, 384,new Image("res/nextButton.png"));		
 		moveImage = new Image("res/move.png");
 		moveGrayImage = new Image("res/move-gray.png");
 
 		swordImage = new Image("res/sword.png");
 		swordGrayImage = new Image("res/sword-gray.png");
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame state, Graphics g) throws SlickException {
 		map.render(container, state, g);
 		drawSelector(g);
 		drawSelected(g);
 		map.renderCreatures(container, state, g);
 		if (selectedCreature != null) {
 			drawCreatureInfo(g, selectedCreature);
 		}
 		drawNextTurn(g);
 	}
 
 	private void drawNextTurn(Graphics g) {
 		nextTurn.render(g);
 	}
 
 	private void drawSelected(Graphics g) {
 		if (selectedCreature!=null && isValid(sx / 32, sy / 32)) {
 			g.setColor(Color.red);
 			g.setLineWidth(4);
 			g.drawRect(sx, sy, 32, 32);
 			g.resetLineWidth();
 			g.setColor(Color.white);
 		}
 	}
 
 	private void drawSelector(Graphics g) {
 		if (selectedCreature != null && isValid(mx / 32, my / 32)) {
 			g.setColor(Color.blue);
 			g.setLineWidth(4);
 			g.drawRect(mx, my, 32, 32);
 			g.resetLineWidth();
 			g.setColor(Color.white);
 		}
 	}
 
 	private void drawCreatureInfo(Graphics g, Creature c) {
 		int x = 5;
 		int y = 388;
 		g.drawString(c.getType(), x, y);
 		g.drawString("HP " + c.getHp() + " MV " + c.getMovement() + " DMG " + c.getDamage(), x, y + 16);
 		g.drawString(c.getGroup(), x, y + 32);
 
 		if (!c.isMoved()) {
 			g.drawImage(moveImage, 200, y+8);
 		} else {
 			g.drawImage(moveGrayImage, 200, y+8);
 		}
 		if (!c.isAttacked()) {
 			g.drawImage(swordImage, 256, y+8);
 		} else {
 			g.drawImage(swordGrayImage, 256, y+8);
 		}
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame state, int arg2) throws SlickException {
 		nextTurn.update(gc, state, arg2);
 		map.removeDeadCreatures(gc, state, arg2);
 	}
 
 	@Override
 	public int getID() {
 		return 0;
 	}
 
 	@Override
 	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
 		if (oldx != newx || oldy != newy) {
 			mx = newx / 32 * 32;
 			my = newy / 32 * 32;
 		} 
 	}
 
 	@Override
 	public void mouseClicked(int button, int x, int y, int clickCount) {
 		// first select
 		if (button == Input.MOUSE_LEFT_BUTTON) {
 			sx = x / 32 * 32;
 			sy = y / 32 * 32;
 			selectedCreature = map.isCreature(x / 32, y / 32);
 			if (selectedCreature!=null){
 				if (selectedCreature.isGroupPlayer()){
 					map.drawMoveable(selectedCreature);
 				}
 			}
 		}
 
 		// move and attack commands
 		if (selectedCreature != null && button == Input.MOUSE_RIGHT_BUTTON) {
 			int tx = x / 32;
 			int ty = y / 32;
 			Creature target = map.isCreature(tx, ty);
 			if (target == null) {
 				if (isValid(tx, ty)) {
 					Wargame.eventBus.post(new MoveEvent(selectedCreature, tx, ty, Creature.GROUP_PLAYER));
 				}
 			} else if (map.isTargetable(tx, ty, Creature.GROUP_PLAYER)) {
 				Wargame.eventBus.post(new AttackEvent(selectedCreature, target));
 			}
 			sx = 1000;
 			sy = 1000;
 		}
 	}
 
 	public boolean isValid(int tx, int ty) {
 		if (tx < 0 || tx > map.getSize() - 1 || ty < 0 || ty > map.getSize() - 1) {
 			return false;
 		}
 		return true;
 	}
 
 
 
 	@Subscribe
 	public void handleNextTurn(NextTurnEvent e) {
 		map.resetMoveAttackCreatures();
 		selectedCreature = null;
 	}
 	public int getSize() {
 		return map.getSize();
 	}
 	
 	public GameMap getMap() {
 		return map;
 	}
 }
