 package it.wargame.gamestates;
 
 import it.wargame.Wargame;
 import it.wargame.ai.AiInterface;
 import it.wargame.ai.TargetAi;
 import it.wargame.creatures.Creature;
 import it.wargame.events.AttackEvent;
 import it.wargame.events.MoveEvent;
 import it.wargame.events.NextTurnEvent;
 import it.wargame.map.GameMap;
 import it.wargame.ui.Button;
 import it.wargame.ui.NextTurnButton;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
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
 
 	private ArrayList<Creature> creatures = new ArrayList<Creature>();
 
 	private int mx;
 
 	private int my;
 
 	private int sx;
 
 	private int sy;
 
 	private Creature selectedCreature;
 
 	private Button nextTurn;
 
 	@SuppressWarnings("unused")
 	private AiInterface ai;
 
 	private Image moveImage;
 
 	private Image moveGrayImage;
 
 	private Image swordImage;
 
 	private Image swordGrayImage;
 
 	@Override
 	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
 		map = new GameMap(6).init();
		Creature c = Creature.buildWarrior().setLocation(0, 2).setGroup(Creature.GROUP_PLAYER);
 		creatures.add(c);
		c = Creature.buildArcher().setLocation(0, 3).setGroup(Creature.GROUP_PLAYER);
 		creatures.add(c);
 
 		nextTurn = new NextTurnButton(4 * 64, 424).setText("Next turn");
 
		c = Creature.buildWarrior().setLocation(5, 2).setGroup(Creature.GROUP_AI);
 		creatures.add(c);
 		c = null;
		c = Creature.buildArcher().setLocation(5, 3).setGroup(Creature.GROUP_AI);
 		creatures.add(c);
 
 		ai = new TargetAi(creatures, this);
 		Wargame.eventBus.register(this);
 
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
 		for (Creature c : creatures) {
 			c.render(container, state, g);
 		}
 		if (selectedCreature != null) {
 			drawCreatureInfo(g, selectedCreature);
 		}
 		drawNextTurn(g);
 	}
 
 	private void drawNextTurn(Graphics g) {
 		nextTurn.render(g);
 	}
 
 	private void drawSelected(Graphics g) {
		if (selectedCreature!=null && isValid(sx / 64, sy / 64)) {
 			g.setColor(Color.red);
 			g.setLineWidth(4);
 			g.drawRect(sx, sy, 64, 64);
 			g.resetLineWidth();
 			g.setColor(Color.white);
 		}
 	}
 
 	private void drawSelector(Graphics g) {
		if (selectedCreature != null && isValid(mx / 64, my / 64)) {
 			g.setColor(Color.blue);
 			g.setLineWidth(4);
 			g.drawRect(mx, my, 64, 64);
 			g.resetLineWidth();
 			g.setColor(Color.white);
 		}
 	}
 
 	private void drawCreatureInfo(Graphics g, Creature c) {
 		int x = 32;
 		int y = 424;
 		g.drawString(c.getType(), x, y);
 		g.drawString("HP " + c.getHp() + " MV " + c.getMovement() + " DMG " + c.getDamage(), x, y + 16);
 		g.drawString(c.getGroup(), x, y + 32);
 
 		if (!c.isMoved()) {
 			g.drawImage(moveImage, 32, 388);
 		} else {
 			g.drawImage(moveGrayImage, 32, 388);
 		}
 		if (!c.isAttacked()) {
 			g.drawImage(swordImage, 76, 388);
 		} else {
 			g.drawImage(swordGrayImage, 76, 388);
 		}
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame state, int arg2) throws SlickException {
 		nextTurn.update(gc, state, arg2);
 		// check dead creatures
 		for (Iterator<Creature> iterator = creatures.iterator(); iterator.hasNext();) {
 			Creature t = iterator.next();
 			if (t.isDead()) {
 				iterator.remove();
 			}
 
 		}
 	}
 
 	@Override
 	public int getID() {
 		return 0;
 	}
 
 	@Override
 	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
 		if (oldx != newx || oldy != newy) {
 			mx = newx / 64 * 64;
 			my = newy / 64 * 64;
 		} else {
 		}
 	}
 
 	@Override
 	public void mouseClicked(int button, int x, int y, int clickCount) {
 		// first select
 		if (selectedCreature == null && button == Input.MOUSE_LEFT_BUTTON) {
 			sx = x / 64 * 64;
 			sy = y / 64 * 64;
 			selectedCreature = isCreature(x / 64, y / 64);
 			return;
 		}
 		if (selectedCreature != null && button == Input.MOUSE_LEFT_BUTTON) {
 			selectedCreature = null;
 		}
 
 		// move and attack commands
 		if (selectedCreature != null && button == Input.MOUSE_RIGHT_BUTTON) {
 			int tx = x / 64;
 			int ty = y / 64;
 			Creature target = isCreature(tx, ty);
 			if (target == null) {
 				if (isValid(tx, ty)) {
 					Wargame.eventBus.post(new MoveEvent(selectedCreature, tx, ty, Creature.GROUP_PLAYER));
 				}
 			} else if (isTargetable(tx, ty, Creature.GROUP_PLAYER)) {
 				Wargame.eventBus.post(new AttackEvent(selectedCreature, target));
 			}
 			sx = 1000;
 			sy = 1000;
 		}
 		// attack command
 		// if (selectedCreature != null && button == Input.MOUSE_LEFT_BUTTON) {
 		// int tx = x / 64;
 		// int ty = y / 64;
 		// if (isTargetable(tx, ty, Creature.GROUP_PLAYER)) {
 		// Wargame.eventBus.post(new AttackEvent(selectedCreature, tx, ty,
 		// Creature.GROUP_PLAYER));
 		// selectedCreature = null;
 		// sx = 1000;
 		// sy = 1000;
 		// } else {
 		// selectedCreature = null;
 		// }
 		//
 		// }
 	}
 	private boolean isTargetable(int tx, int ty, int targetGroup) {
 		for (Creature c : creatures) {
 			if (c.getOwner() != targetGroup) {
 				if (c.getX() == tx && c.getY() == ty) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	public boolean isValid(int tx, int ty) {
 		if (tx < 0 || tx > map.getSize() - 1 || ty < 0 || ty > map.getSize() - 1) {
 			return false;
 		}
 		return true;
 	}
 
 	public Creature isCreature(int x, int y) {
 		for (Creature c : creatures) {
 			if (c.isLocation(x, y)) {
 				return c;
 			}
 		}
 		return null;
 	}
 
 	@Subscribe
 	public void handleNextTurn(NextTurnEvent e) {
 		// reset moves&atacks
 		for (Creature c : creatures) {
 			c.setMoved(false);
 			c.setAttacked(false);
 		}
 		selectedCreature = null;
 	}
 	public int getSize() {
 		return map.getSize();
 	}
 }
