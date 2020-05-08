 package racing;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 public class Racing extends JPanel implements ActionListener {
 
 	private static final long serialVersionUID = 1169331112688629681L;
 	private Car raceCar;
 	private int[] keys;
 	private Dimension screenSize;
 	private Timer timer;
 	private BufferedImage track;
 	private BufferedImage texture;
 	private BufferedImage checkpt;
 	private BufferedImage wall;
 
 	public Racing() {
 		init();
 	}
 
 	private void init() {
 		try {
 			track = ImageIO.read(new File("data/tracks/silverstone/track.png"));
 			texture = ImageIO.read(new File(
 					"data/tracks/silverstone/texture.png"));
 			checkpt = ImageIO.read(new File(
 					"data/tracks/silverstone/checkpt.png"));
 			wall = ImageIO.read(new File("data/tracks/silverstone/wall.png"));
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 
 		screenSize = new Dimension(1680, 1000);
 		setPreferredSize(screenSize);
 
 		raceCar = new Car();
 		raceCar.move(1370, 390);
 
 		registerKeyListener();
 
 		timer = new Timer(15, this);
 		timer.start();
 	}
 
 	private void registerKeyListener() {
 		keys = new int[256];
 		Arrays.fill(keys, 0); // 0 = key is up
 
 		final KeyboardFocusManager kfm = KeyboardFocusManager
 				.getCurrentKeyboardFocusManager();
 		kfm.addKeyEventDispatcher(new KeyEventDispatcher() {
 			@Override
 			public boolean dispatchKeyEvent(final KeyEvent e) {
 				switch (e.getID()) {
 				case KeyEvent.KEY_PRESSED:
 					keys[e.getKeyCode()] = 1;
 					break;
 				case KeyEvent.KEY_RELEASED:
 					keys[e.getKeyCode()] = 0;
 					break;
 				default:
 					break;
 				}
 				return false;
 			}
 		});
 
 	}
 
 	@Override
 	public void paintComponent(final Graphics g) {
 		super.paintComponent(g);
 		final Graphics2D g2d = (Graphics2D) g;
 		g2d.drawImage(wall, 0, 0, null);
 		g2d.drawImage(checkpt, 0, 0, null);
 		g2d.drawImage(track, 0, 0, null);
 		g2d.drawImage(texture, 0, 0, null);
 		raceCar.paintComponent(g2d);
 	}
 
 	private void moveCar() {
 		keyProcessing();
 		if (controlRaceCarPosition()) {
 			raceCar.move();
 		}
 	}
 
 	private boolean controlRaceCarPosition() {
 		/*
 		 * �berpr�fen Sie ob das Fahrzeug den sichtbaren Bereich des Bildschirm
 		 * nicht verl�sst
 		 */
 		final Dimension dim = getSize();
 		if (raceCar.getX() <= 0) {
 			raceCar.move(raceCar.getX() + 1, raceCar.getY());
 			raceCar.stop();
 			return false;
 		}
 		if (raceCar.getX() >= dim.width) {
 			raceCar.move(raceCar.getX() - 1, raceCar.getY());
 			raceCar.stop();
 			return false;
 		}
 		if (raceCar.getY() <= 0) {
 			raceCar.move(raceCar.getX(), raceCar.getY() + 1);
 			raceCar.stop();
 			return false;
 		}
 		if (raceCar.getY() >= dim.height) {
 			raceCar.move(raceCar.getX(), raceCar.getY() - 1);
 			raceCar.stop();
 			return false;
 		}
 
 		final int carMiddleX = raceCar.getX();
 		final int carMiddleY = raceCar.getY();
 		/*
 		 * Überprüfen ob das Fahrzeug einen Checkpoint passiert hat
 		 */
 
 		final int checkPointColor = checkpt.getRGB(carMiddleX, carMiddleY);
 		if (new Color(checkPointColor).equals(new Color(0xffff00))) {
 			// TODO Checkpoint verification
 		}
 
 		/*
 		 * �berpr�fen ob sich das Fahrzueg noch auf der Strecke befindet
 		 */
 
		int trackColor = track.getRGB(carMiddleX, carMiddleY);
 		if (trackColor == 0) {
 			if (raceCar.getSpeed() >= Car.SLOW_SPEED) {
 				raceCar.setSlowSpeed();
 			}
 			if (raceCar.getSpeed() < 0) {
 				raceCar.setMinSlowSpeed();
 			}
 		} else {
 			if (new Color(trackColor).equals(Color.black)) {
 				return true;
 			}
 			if (new Color(trackColor).equals(new Color(0xBDBDBD))) {
 				if (raceCar.getSpeed() >= Car.SLOW_SPEED) {
 					raceCar.slowdown();
 					return true;
 				}
 			}
 
 		}
		trackColor = wall.getRGB(carMiddleX, carMiddleY);
		if (new Color(trackColor).equals(new Color(0xff0000))) {
			raceCar.stop();
			trackColor = wall.getRGB(raceCar.getOldX(), raceCar.getOldY());
			raceCar.move(raceCar.getOldX(), raceCar.getOldY());
			return false;
		}
 		return true;
 	}
 
 	private void keyProcessing() {
 
 		if (keys[KeyEvent.VK_UP] == 1) {
 			raceCar.accelerate();
 		}
 		if (keys[KeyEvent.VK_DOWN] == 1) {
 			if (raceCar.getSpeed() > 0) {
 				raceCar.slowdown();
 			} else {
 				raceCar.deccelerate();
 			}
 		}
 		if (keys[KeyEvent.VK_RIGHT] == 1) {
 			raceCar.turnRight();
 		}
 		if (keys[KeyEvent.VK_LEFT] == 1) {
 			raceCar.turnLeft();
 		}
 
 		if (keys[KeyEvent.VK_UP] == 0 && keys[KeyEvent.VK_DOWN] == 0) {
 			if (raceCar.getSpeed() >= 0) {
 				raceCar.slowdown(0.4);
 			} else {
 				raceCar.slowdownBackward(0.4);
 			}
 		}
 	}
 
 	@Override
 	public void actionPerformed(final ActionEvent e) {
 		repaint();
 		moveCar();
 	}
 
 	public static void main(final String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				createAndShowGUI();
 			}
 		});
 	}
 
 	public static void createAndShowGUI() {
 		final JFrame f = new JFrame("Racing");
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.add(new Racing());
 		// f.setResizable(false);
 		f.pack();
 		f.setVisible(true);
 	}
 
 }
