 package screen;
 
 import items.Bullet;
 import items.Inventory;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.*;
 
 import main.ProjectMedusa;
 import map.Map;
 
 @SuppressWarnings("serial")
 public class ScreenPanel extends JPanel implements KeyListener {
 
 	private Map map;
 	private Camera camera;
 	private Cursor cursor;
 	private int mouseX;
 	private int mouseY;
 	private Graphics2D bufferGraphics;
 	private Hud hud;
 	private long lastTime;
 
 	public ScreenPanel() {
 		setFocusable(true);
 		requestFocusInWindow();
 		MListener mlist = new MListener();
 		addMouseListener(mlist);
 		addMouseMotionListener(mlist);
 		this.addKeyListener(this);
 		init();
 	}
 
 	public void init() {
 		map = new Map("Levels/testLevel.txt");
 		camera = new Camera(map.getChar().getX()
 				+ (map.getChar().CHAR_WIDTH / 2), map.getChar().getY()
 				+ (map.getChar().CHAR_HEIGHT / 2));
 		cursor = new Cursor(ProjectMedusa.SCREEN_WIDTH / 2,
 				ProjectMedusa.SCREEN_HEIGHT / 2);
 		hud = new Hud(map.getChar().getHealth(), map.getChar().getHunger(), map
 				.getChar().getStamina());
 	}
 
 	public void paintComponent(Graphics g) {
 
 		requestFocusInWindow();
 		Graphics2D g2d = (Graphics2D) g;
 		Image offscreen = createImage(getWidth(), getHeight());
 		bufferGraphics = (Graphics2D) offscreen.getGraphics();
 		bufferGraphics.setColor(Color.white);
 		bufferGraphics.fillRect(0, 0, getWidth(), getHeight());
 
 		mainLoop(bufferGraphics);
 		g2d.drawImage(offscreen, 0, 0, this);
 
 	}
 
 	public void mainLoop(Graphics2D g) {
 		update();
		if(map.getChar().getInv().isOpen()){
 		    g.translate(-camera.getX()/2, -camera.getY());
             map.draw(g);
             g.translate(camera.getX()/2, camera.getY());
         }else{
             g.translate(-camera.getX(), -camera.getY());
             map.draw(g);
             g.translate(camera.getX(), camera.getY());
         }
 
 		cursor.draw(g);
 		
 		hud.draw(g);
 		if(map.getChar().getInv().isOpen()){
 		    map.getChar().getInv().draw(g);
 		}
 
 	}
 
 	public void update() {
 		map.update();
 		if(map.getChar().getInv().isOpen())
 		    camera.setCamera((map.getChar().getX() + (map.getChar().CHAR_WIDTH / 2))*2, map.getChar().getY() + (map.getChar().CHAR_HEIGHT));
 		else
 		    camera.setCamera(map.getChar().getX() + (map.getChar().CHAR_WIDTH / 2), map.getChar().getY() + (map.getChar().CHAR_HEIGHT));
 		cursor.setCur(mouseX, mouseY);
 		hud.setStats(map.getChar().getHealth(), map.getChar().getHunger(), map
 				.getChar().getStamina());
 	}
 
 	@Override
 	public void keyPressed(KeyEvent k) {
 		if (k.getKeyCode() == KeyEvent.VK_SHIFT)
 			if (map.getChar().getStamina() > 0) {
 				map.getChar().setRunning(true);
 			}
 		if (k.getKeyCode() == KeyEvent.VK_A)
 			map.getChar().setMovingLeft(true);
 		if (k.getKeyCode() == KeyEvent.VK_W)
 			map.getChar().setMovingUp(true);
 		if (k.getKeyCode() == KeyEvent.VK_D)
 			map.getChar().setMovingRight(true);
 		if (k.getKeyCode() == KeyEvent.VK_S)
 			map.getChar().setMovingDown(true);
 
 		if (k.getKeyCode() == KeyEvent.VK_U) {
 			map.getChar().damageHealth(1);
 		}
 		if (k.getKeyCode() == KeyEvent.VK_I)
 			map.getChar().damageHealth(-1);
 
 		if (k.getKeyCode() == KeyEvent.VK_ESCAPE)
 			System.exit(0);
 
 		if (k.getKeyCode() == KeyEvent.VK_E) {
 			map.getChar().getInv().setOpen(!map.getChar().getInv().isOpen());
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent k) {
 		if (k.getKeyCode() == KeyEvent.VK_SHIFT)
 			map.getChar().setRunning(false);
 		if (k.getKeyCode() == KeyEvent.VK_A)
 			map.getChar().setMovingLeft(false);
 		if (k.getKeyCode() == KeyEvent.VK_W)
 			map.getChar().setMovingUp(false);
 		if (k.getKeyCode() == KeyEvent.VK_D)
 			map.getChar().setMovingRight(false);
 		if (k.getKeyCode() == KeyEvent.VK_S)
 			map.getChar().setMovingDown(false);
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 
 	}
 
 	private class MListener implements MouseListener, MouseMotionListener {
 
 		@Override
 		public void mouseDragged(MouseEvent m) {
 			mouseX = m.getX();
 			mouseY = m.getY();
 
 		}
 
 		@Override
 		public void mouseMoved(MouseEvent m) {
 			mouseX = m.getX();
 			mouseY = m.getY();
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent arg0) {
 
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mouseExited(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mousePressed(MouseEvent arg0) {
 			if (arg0.getButton() == 1) {
 				// future fireWeapon method map.fireWeapon();
 				System.out.println("mouse clicked");
 				int x = (arg0.getX() + camera.getX());
 				int y = arg0.getY() + camera.getY();
 				System.out.println("New Bullet created at ("
 						+ map.getChar().getX() + ", " + map.getChar().getY()
 						+ ") heading to (" + x + ", " + y + ")");
				
				if(map.getChar().getInv().isOpen())
				    map.addBullet(new Bullet(map.getChar().getX(), map.getChar().getY(), arg0.getX() + camera.getX()/2, arg0.getY() + camera.getY()));
				else
                    map.addBullet(new Bullet(map.getChar().getX(), map.getChar().getY(), arg0.getX() + camera.getX(), arg0.getY() + camera.getY()));
 			}
 			if (arg0.getButton() == 3) {
 				//Pick up item if within range
 			}
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 	}
 
 }
