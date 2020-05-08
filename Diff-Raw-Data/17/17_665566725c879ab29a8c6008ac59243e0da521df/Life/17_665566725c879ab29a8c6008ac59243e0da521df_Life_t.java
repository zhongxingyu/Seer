 package com.njd.life;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 public class Life extends JPanel {
 
 	private Timer		timer	= new Timer();
 	private Grid		grid	= new Grid();
 	private boolean		play	= false;
 	protected boolean	mouseDown;
 
 	public Life() {
 		setPreferredSize(new Dimension(800, 600));
 
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				mouseDown = true;
				int col = (int) ((e.getX()-getWidth()/2)/grid.getScaleX()) / grid.getTileWidth();
				int row = (int) ((e.getY()-getHeight()/2)/grid.getScaleY()) / grid.getTileHeight();
 				if (!grid.contains(col, row)) {
 					grid.add(col, row);
 				} else {
 					grid.remove(col, row);
 				}
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				mouseDown = false;
 			}
 		});
 
 		addMouseMotionListener(new MouseMotionAdapter() {
 			@Override
 			public void mouseDragged(MouseEvent e) {
				int col = (int) ((e.getX()-getWidth()/2)/grid.getScaleX()) / grid.getTileWidth();
				int row = (int) ((e.getY()-getHeight()/2)/grid.getScaleY()) / grid.getTileHeight();
 				if (!grid.contains(col, row)) {
 					grid.add(col, row);
 				} else {
 					grid.remove(col, row);
 				}
 			}
 		});
 
 		addMouseWheelListener(new MouseWheelListener() {
 			@Override
 			public void mouseWheelMoved(MouseWheelEvent e) {
 				double amount = e.getPreciseWheelRotation() * .05;
 				grid.changeScale(amount, amount);
 			}
 		});
 
 		addKeyListener(new KeyAdapter() {

 			@Override
 			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_UP) {
 					play = false;
 					grid.step();
 				}
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
 					play = !play;
 					// grid.togglePause();
 				} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
 					play = true;
 				} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 					play = false;
 				} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE
 						|| e.getKeyCode() == KeyEvent.VK_DELETE) {
 					play = false;
 					grid.clear();
 				} else if (e.getKeyCode() == KeyEvent.VK_R) {
 					play = false;
 					grid.clear();
 					Random r = new Random(System.currentTimeMillis());
 					int total = (int) (0.85 * r.nextInt((getWidth() / grid
 							.getTileWidth())
 							* (getHeight() / grid.getTileHeight())));
 					for (int i = 0; i < total; ++i) {
 						grid.add(
 								r.nextInt(getWidth() / grid.getTileWidth())
 										- (getWidth() / 2 / grid.getTileWidth()),
 								r.nextInt(getHeight() / grid.getTileHeight())
 										- (getHeight() / 2 / grid
 												.getTileHeight()));
 					}
 				}
 			}
 		});
 
 		timer.schedule(new TimerTask() {
 			public void run() {
 				if (play && !grid.isPaused()) {
 					grid.step();
 				}
 				Life.this.repaint();
 			}
 		}, 0, 10);
 	}
 
 	@Override
 	public void paintComponent(Graphics init) {
 		// System.out.println("Repainting");
 		Graphics2D g = (Graphics2D) init.create();
 		g.setColor(Color.white);
 		g.clearRect(0, 0, getWidth(), getHeight());
 		Graphics2D gCentered = (Graphics2D) g.create();
 		gCentered.translate(getWidth() / 2, getHeight() / 2);
 		grid.render(gCentered, getWidth(), getHeight());
 		gCentered.dispose();
 		g.setColor(Color.black);
 		g.drawString(
 				String.format(
 						"Click or drag to create/destroy life, [DEL] or [BACKSP] to clear, [UP] to step, [RIGHT] to play, [R] for random fill. status=%s",
 						(play) ? "Running" : "Paused"), 10, getHeight() - 20);
 		g.dispose();
 	}
 
 	public static void main(String... args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				final Life life = new Life();
 				JFrame frame = new JFrame("Life");
 				frame.setLayout(new BorderLayout());
 				frame.add(life);
 				frame.pack();
 				frame.setLocationRelativeTo(null);
 				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 				frame.setVisible(true);
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						life.requestFocus();
 					}
 				});
 			}
 		});
 	}
 }
