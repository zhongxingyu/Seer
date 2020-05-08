 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Calendar;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 public class Notizbuch {
 	public static final int br = 1600, ho = 1000;
 	public static final String preDir = "";
 	
 	static MalPanel panel = new MalPanel();
 	static BufferedImage img, bg;
 	
 	static String autoName = null;
 	
 	static Graphics2D g;
 	
 	public static void main (String[] args) {
 		g = initBild();
 		
 		
 		JFrame f = new JFrame("neuenotiz");
 		f.setSize(br, ho);
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.add(panel);
 		
 		panel.addMouseMotionListener(new MouseMotionListener() {
 			private int x = -1, y = -1;
 			
 			public void mouseDragged(MouseEvent arg0) {
 				if (x > 0) {			
 					g.setColor(Color.BLACK);
 					g.drawLine(x, y, arg0.getX(), arg0.getY());
 				}
 				x = arg0.getX();
 				y = arg0.getY();
 				panel.repaint();
 			}
 
 			public void mouseMoved(MouseEvent arg0) {
 				x = arg0.getX();
 				y = arg0.getY();
 			}
 		});
 		
 		f.addKeyListener(new KeyListener() {
 			public void keyPressed(KeyEvent arg0) {}
 
 			public void keyReleased(KeyEvent ev) {
 				if (ev.getKeyChar() == 's') {
 					try {
 						String name = JOptionPane.showInputDialog("titel");
 						if (name != null)
 							javax.imageio.ImageIO.write(img, "png", new FileOutputStream(name+".png"));
 					}
 					catch (IOException e) {
 						e.printStackTrace();
 					}
 					
 					g = initBild();
 					panel.repaint();
 				}
 				
 				if (ev.getKeyChar() == 'a') {
 					try {
 						javax.imageio.ImageIO.write(img, "png", new FileOutputStream(autoName));
 					}
 					catch (IOException e) {
 						e.printStackTrace();
 					}
 					
 					g = initBild();
 					panel.repaint();
 				}
 
 			}
 
 			public void keyTyped(KeyEvent arg0) {}
 		});
 		
 
 		f.setVisible(true);
 		
 //		GraphicsDevice myDevice = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
 //		
 //		if (myDevice.isFullScreenSupported()) {
 //			f.setUndecorated(true);
 //			f.setSize(Toolkit.getDefaultToolkit().getScreenSize());
 //			myDevice.setFullScreenWindow(f);
 //
 //			f.setLocation(0, 0);
 //
 //		}
 //		else
 //			System.exit(0);
 
 		
 	}
 
 	private static Graphics2D initBild() {
 
 		ClassLoader cl = ClassLoader.getSystemClassLoader();
 		try {
			bg = ImageIO.read(cl.getResource("bg1600alpha.jpg"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		img = new BufferedImage(br, ho, BufferedImage.TYPE_INT_ARGB);
 		final Graphics2D g = (Graphics2D)(img.getGraphics());
 		g.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
 		
 		g.setColor(new Color(255, 255, 255, 255));
 		g.fillRect(0, 0, br, ho);
 		
 		/* Autospeichern-Name herausfinden */
 		Calendar rightNow = Calendar.getInstance();
 		String pre = "Notiz"+"-"+rightNow.get(Calendar.YEAR)+"-"+rightNow.get(Calendar.MONTH)+"-"+rightNow.get(Calendar.DAY_OF_MONTH)+"-";
 		
 		boolean search = true;
 		int i = 0;
 		while (search) {
 			
 			i++;
 			File act = new File(preDir+pre+i+".png");
 			search = act.exists();
 			
 			
 		}
 		
 		autoName = preDir+pre+i+".png";
 		return g;
 	}
 }
