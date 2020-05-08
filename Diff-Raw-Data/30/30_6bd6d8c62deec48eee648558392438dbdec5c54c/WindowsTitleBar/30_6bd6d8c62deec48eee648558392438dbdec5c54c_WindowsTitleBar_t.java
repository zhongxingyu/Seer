 package org.apache.pivot.wtk;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.LinearGradientPaint;
 import java.awt.MouseInfo;
 import java.awt.Paint;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.Toolkit;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.WindowEvent;
 import java.awt.geom.Arc2D;
 import java.awt.geom.Area;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 /**
  * Component which represents the styling of the top border of
  * a given frame. This {@link TitleBar} has the particular styling
  * for a BarChart custom style. This title bar also defines a
  * {@link TitleBarControl} which handles the minimize, maximize
  * and close functions.
  * 
  * @author David Ray
  *
  */
 public class WindowsTitleBar extends TitleBar {
 
 	private static final long serialVersionUID = 8331424255756031127L;
 	
 	Timer t = null;
 	final String s = "A Mikey Mike Production";
 	int strLoc = Integer.MIN_VALUE;
 	int offset = 0;
 	boolean animating = false;
 	Timer repaintTimer = null;
 	
 	int paintCountDown = 3;
 	
 	private DesktopFrame hostFrame;
 	
 	public WindowsTitleBar() {
 		addTitleBarControl(createTitleBarControl());
 		addSignature(createSignatureLogo());
 		getParentFrame();
 	}
 
 	@Override
 	protected void paintTitle(Graphics2D g2) {
 		g2.setFont(g2.getFont().deriveFont(12f));
 		FontMetrics fm = g2.getFontMetrics();
 		g2.setColor(Color.BLACK);
         Rectangle r = g2.getClipBounds();
        int textX = 30;
         int textY = ((r.height) / 2) - ((fm.getAscent() + fm.getDescent()) / 2) + fm.getAscent();
         g2.drawString(title, textX, textY);
     }
 	
 	@Override
 	protected void fillTitleBar(Graphics2D g2) {
 		Rectangle r = g2.getClipBounds();
 		if(r.x > 0) return;
 		
 		Paint oldPaint = g2.getPaint();
         Stroke oldStroke = g2.getStroke();
         
         g2.setColor(new Color(241,241,241));
         g2.setStroke(new BasicStroke(1));
         g2.drawLine(0, 0, getWidth(), 0);
         
         LinearGradientPaint lgp = new LinearGradientPaint(
             new Point2D.Double(0,0), new Point2D.Double(0, r.height),
             	new float[] {0.0f, 1.0f}, new Color[] { new Color(146, 173, 201), new Color(185, 209, 234) });
         g2.setPaint(lgp);
         g2.fillRect(0, 0, getWidth(), this.getHeight() - 1);
         g2.setPaint(oldPaint);
         
         g2.setPaint(oldPaint);
         g2.setColor(new Color(104, 104, 104));
         g2.setStroke(new BasicStroke(1));
         g2.drawLine(getX(), getHeight() - 1, getWidth(), getHeight() - 1);
         
         g2.setStroke(oldStroke);
         
         if(animating) {
         	g2.setClip(getBounds());
         	g2.setColor(Color.WHITE);
         	strLoc = - SwingUtilities.computeStringWidth(getFontMetrics(getFont()), s);
         	g2.drawString(s, strLoc + offset, 20);
         }
 	}
 	
 	private TitleBarControl createTitleBarControl() {
 		final WindowsTitleBarControl control = new WindowsTitleBarControl();
 		control.setPreferredSize(new Dimension(120,20));
 		control.setControlLocation(TitleBar.Location.RIGHT);
 		
 		repaintTimer = new Timer(250, new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent e) {
 				java.awt.Point p = MouseInfo.getPointerInfo().getLocation();
 				SwingUtilities.convertPointFromScreen(p, control);
 				if(!minus.contains(p) && !plus.contains(p) && !close.contains(p)) {
 					overPlus = false;
 					overMinus = false;
 					overClose = false;
 					control.repaint();
 				}
 			}
 		});
 		repaintTimer.setRepeats(false);
 		
 		return control;
 	}
 	
 	private SignatureLogo createSignatureLogo() {
 		SignatureLogo logo = new SignatureLogo();
 		logo.setPreferredSize(new Dimension(60,20));
 		return logo;
 	}
 	
 	@SuppressWarnings("serial")
 	public class SignatureLogo extends java.awt.Component implements MouseListener, MouseMotionListener {
 		private double rotationAngle = 0.0d;
 		private BufferedImage logo = createLogo(Color.WHITE, new Color(95, 95, 95));
 		
 		public SignatureLogo() {
 			addMouseListener(this);
 			addMouseMotionListener(this);
 		}
 		
 		@Override
 		public void mouseDragged(MouseEvent e) {}
 
 		@Override
 		public void mouseMoved(MouseEvent e) {}
 
 		@Override
 		public void mouseClicked(MouseEvent e) {}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {}
 
 		@Override
 		public void mouseExited(MouseEvent e) {}
 		
 		@Override
 		public void paint(Graphics g) {
 			super.paint(g);
 	        
 	        Graphics2D g2 = (Graphics2D)g;
 	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 	        
 	        int x = 5;
 	        int y = 6;
 	        int imgCenterX = x + this.logo.getWidth() / 2;
 	        int imgCenterY = y + this.logo.getHeight() / 2 - 2;
 	        g2.translate(imgCenterX, imgCenterY);
 	        double angle = -getRotation();
 	        g2.rotate(angle);
 	        g2.translate(-imgCenterX, -imgCenterY);
 	        g2.drawImage(this.logo, x, y, null); 
 	    }
 		
 		private double getRotation() {
 			return rotationAngle;
 		}
 		
 		private BufferedImage createLogo(Color bg, Color fg) {
 			BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
 			Graphics2D g2 = (Graphics2D)img.getGraphics();
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			
 			int lx = 0;
 			g2.setColor(bg);
 	        Arc2D lLogo = new Arc2D.Double(lx, 0, 16, 16, 90, 180, Arc2D.OPEN);
 	        g2.draw(lLogo);
 	        g2.drawLine(lx + 8, 0, lx + 8, 4);
 	        g2.drawLine(lx + 8, 4, lx + 4, 4);
 	        g2.drawLine(lx + 4, 4, lx + 4, 6);
 	        g2.drawLine(lx + 4, 6, lx + 8, 6);
 	        g2.drawLine(lx + 8, 6, lx + 8, 15);
 	                
 	        Arc2D rLogo = new Arc2D.Double(lx + 2, 0, 18, 17, 90, -180, Arc2D.OPEN);
 	        g2.fill(rLogo);
 	        g2.setColor(new Color(149, 178, 207));
 	        g2.fillRect(lx + 10, 10, 4, 2);
 	        
 	        g2.dispose();
 	        return img;
 		}
 		
 		public void mousePressed(MouseEvent m) {
 			//Use this to make clickable if menu exists
 //			if(getBounds().contains(m.getPoint())) {
 //				rotationAngle = Math.PI;
 //				repaint();
 //			}
 		}
 		
 		public void mouseReleased(MouseEvent m) {
 			rotationAngle = 0;
 			
 			if(m.getButton() == MouseEvent.BUTTON2 && (t == null || !t.isRunning())) {
 				t = new Timer(0, new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent e) {
 						if(strLoc + offset > 69) {
 							t.stop();
 							t = null;
 							offset = strLoc;
 							animating = false;
 							WindowsTitleBar.this.repaint();
 						}else{
 							offset += 3;
 							WindowsTitleBar.this.repaint();
 						}
 					}
 				});
 				animating = true;
 				t.setRepeats(true);
 				t.setDelay(50);
 				t.start();
 			}
 			repaint();
 		}
 	}
 	
 	java.awt.Point mp = new java.awt.Point();
 	Rectangle minus = new Rectangle();
 	Rectangle plus = new Rectangle();
 	Rectangle close = new Rectangle();
 	boolean overClose = false;
 	boolean overPlus = false;
 	boolean overMinus = false;
 	@SuppressWarnings("serial")
 	public class WindowsTitleBarControl extends TitleBarControl {
 		public WindowsTitleBarControl() {
 			addMouseListener(this);
 			addMouseMotionListener(this);
 		}
 		public void update(Graphics g) {
 			paint(g);
 		}
 		public void paint(Graphics g) {
 			Graphics2D g2 = (Graphics2D)g;
 			Paint oldPaint = g2.getPaint();
 			g2.setColor(Color.white);
 			Rectangle r = g.getClipBounds();
 			r.height -= 1;
 			r.width -= 1;
 			
 			Rectangle xBox = new Rectangle(r.width - 43, 6, 31, 16);
 			close = xBox;
 			
 			g2.setColor(new Color(253, 228, 223));
     		Rectangle innerBox = new Rectangle(r.width - 42, 7, 29, 14);
     		g2.draw(innerBox);//252, 200, 191
     		LinearGradientPaint topHalfX = new LinearGradientPaint(
 	            new Point2D.Double(r.width - 41, 8), new Point2D.Double(r.width - 41, 15),
 	            	new float[] {0.0f, 1.0f}, new Color[] { new Color(252, 200, 191), new Color(251, 168, 154) });
     		g2.setPaint(topHalfX);
     		g2.fill(new Rectangle(r.width - 41, 8, 28, 7));
     		Color topColor = new Color(180, 63, 44);
     		Color bottomColor = new Color(210, 126, 111);
     		if(overClose) {
     			topColor = new Color(215, 65, 22);
     			bottomColor = new Color(245, 237, 108);
     		}
     		LinearGradientPaint bottomHalfX = new LinearGradientPaint(
 	            new Point2D.Double(r.width - 41, 14), new Point2D.Double(r.width - 41, 21),
 	            	new float[] {0.0f, 1.0f}, new Color[] { topColor, bottomColor });
     		g2.setPaint(bottomHalfX);
     		g2.fill(new Rectangle(r.width - 41, 14, 28, 7));
     		g2.setPaint(oldPaint);
 			
 			int x = r.width - 98;
 			Area xfig = new Area(new Rectangle(x + 64, 10, 14, 8));
     		int[] xpoints = new int[]{ x + 64, x + 68, x + 64 };
     		int[] ypoints = new int[] { 10, 14, 18 };
     		Polygon p = new Polygon(xpoints, ypoints, 3);
     		Area leftTri = new Area(p);
     		xfig.subtract(leftTri);
     		xpoints = new int[]{ x + 68, x + 71, x + 74 };
     		ypoints = new int[] { 10, 13, 10 };
     		p = new Polygon(xpoints, ypoints, 3);
     		Area topTri = new Area(p);
     		xfig.subtract(topTri);
     		xpoints = new int[]{ x + 78, x + 74, x + 78 };
     		ypoints = new int[] { 10, 14, 18 };
     		p = new Polygon(xpoints, ypoints, 3);
     		Area rightTri = new Area(p);
     		xfig.subtract(rightTri);
     		xpoints = new int[]{ x + 68, x + 71, x + 74 };
     		ypoints = new int[] { 18, 15, 18 };
     		p = new Polygon(xpoints, ypoints, 3);
     		Area bottomTri = new Area(p);
     		xfig.subtract(bottomTri);
     		
     		g2.setColor(Color.WHITE);
     		g2.fill(xfig);
     		g2.setColor(Color.BLACK);
     		g2.draw(xfig);
     		
     		g2.draw(xBox);
     		
     		xBox = new Rectangle(r.width - 79, 6, 31, 16);
     		plus = xBox;
     		innerBox = new Rectangle(r.width - 78, 7, 29, 14);
     		g2.setColor(new Color(223, 232, 242));
     		g2.draw(innerBox);
     		topColor = new Color(195, 212, 231);
     		bottomColor = new Color(190, 211, 232);
     		if(overPlus) {
     			topColor = new Color(170, 213, 243);
     			bottomColor = new Color(129, 192, 234);
     		}
     		topHalfX = new LinearGradientPaint(
 	            new Point2D.Double(r.width - 77, 8), new Point2D.Double(r.width - 77, 15),
 	            	new float[] {0.0f, 1.0f}, new Color[] { topColor,  bottomColor});
     		g2.setPaint(topHalfX);
     		g2.fill(new Rectangle(r.width - 77, 8, 28, 7));
     		topColor = new Color(152, 177, 204);
     		bottomColor = new Color(183, 208, 233);
     		if(overPlus) {
     			topColor = new Color(45, 115, 163);
     			bottomColor = new Color(133, 239, 249);
     		}
     		bottomHalfX = new LinearGradientPaint(
 	            new Point2D.Double(r.width - 77, 14), new Point2D.Double(r.width - 77, 21),
 	            	new float[] {0.0f, 1.0f}, new Color[] { topColor, bottomColor });
     		g2.setPaint(bottomHalfX);
     		g2.fill(new Rectangle(r.width - 77, 14, 28, 7));
     		g2.setColor(Color.WHITE);
     		g2.setStroke(new BasicStroke(2));
     		g2.drawRect(r.width - 69, 11, 12, 6);
     		g2.setStroke(new BasicStroke(1));
     		g2.setColor(Color.DARK_GRAY);
     		g2.drawRect(r.width - 70, 10, 14, 8);
     		g2.drawRect(r.width - 67, 13, 8, 2);
     		g2.setColor(Color.BLACK);
     		g2.draw(xBox);
     		
     		xBox = new Rectangle(r.width - 115, 6, 31, 16);
     		minus = xBox;
     		innerBox = new Rectangle(r.width - 114, 7, 29, 14);
     		topColor = new Color(195, 212, 231);
     		bottomColor = new Color(190, 211, 232);
     		if(overMinus) {
     			topColor = new Color(170, 213, 243);
     			bottomColor = new Color(129, 192, 234);
     		}
     		topHalfX = new LinearGradientPaint(
 	            new Point2D.Double(r.width - 113, 8), new Point2D.Double(r.width - 113, 15),
 	            	new float[] {0.0f, 1.0f}, new Color[] { topColor, bottomColor });
     		g2.setPaint(topHalfX);
     		g2.fill(new Rectangle(r.width - 113, 8, 28, 7));
     		topColor = new Color(152, 177, 204);
     		bottomColor = new Color(183, 208, 233);
     		if(overMinus) {
     			topColor = new Color(45, 115, 163);
     			bottomColor = new Color(133, 239, 249);
     		}
     		bottomHalfX = new LinearGradientPaint(
 	            new Point2D.Double(r.width - 113, 14), new Point2D.Double(r.width - 113, 21),
 	            	new float[] {0.0f, 1.0f}, new Color[] { topColor, bottomColor });
     		g2.setPaint(bottomHalfX);
     		g2.fill(new Rectangle(r.width - 113, 14, 28, 7));
     		g2.setColor(new Color(223, 232, 242));
     		g2.draw(innerBox);
     		g2.setColor(Color.WHITE);
     		g2.setStroke(new BasicStroke(2));
     		g2.drawRect(r.width - 105, 13, 12, 2);
     		g2.setStroke(new BasicStroke(1));
     		g2.setColor(Color.DARK_GRAY);
     		g2.drawRect(r.width - 106, 12, 14, 4);
     		g2.setColor(Color.BLACK);
     		g2.draw(xBox);
     		
 		}
 		
 		@Override
 		public void mouseMoved(MouseEvent e) {
 			mp = e.getPoint();
 			if(plus.contains(e.getPoint())) {
 				overPlus = true;
 				overMinus = false;
 				overClose = false;
 			}else if(close.contains(e.getPoint())) {
 				overPlus = false;
 				overMinus = false;
 				overClose = true;
 			}else if(minus.contains(e.getPoint())) {
 				overPlus = false;
 				overMinus = true;
 				overClose = false;
 			}else{
 				overPlus = false;
 				overMinus = false;
 				overClose = false;
 			}
 			repaint();
 			
 			repaintTimer.restart();
 		}
 		
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			if(minus.contains(e.getPoint())) {
 				fireFrameWillBecomeIconized();
 				hostFrame.setExtendedState(JFrame.ICONIFIED);
 			}else if(plus.contains(e.getPoint())) {
 				if((hostFrame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
 					hostFrame.setExtendedState(JFrame.NORMAL);
 					hostFrame.setBordersVisible(true);
 					hostFrame.setBorderSize(5);
 					hostFrame.invalidate();
 					hostFrame.repaint();
 				}else{
 					hostFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
 					hostFrame.setBordersVisible(false);
 					hostFrame.setBorderSize(0);
 					hostFrame.invalidate();
 					hostFrame.repaint();
 				}
 			}else if(close.contains(e.getPoint())) {
 				WindowEvent wev = new WindowEvent(hostFrame, WindowEvent.WINDOW_CLOSING);
                 Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
 			}
 			
 			repaint();
 		}
 	}
 
 	private void getParentFrame() {
 		(new Thread() {
 			public void run() {
 				while(hostFrame == null) {
     				try{ Thread.sleep(100); }catch(Exception e) { e.printStackTrace(); }
     				SwingUtilities.invokeLater(new Runnable() {
     					public void run() {
     						hostFrame = (DesktopFrame)SwingUtilities.getAncestorOfClass(DesktopFrame.class, WindowsTitleBar.this);
     						if((hostFrame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) { 
     							hostFrame.setBorderSize(0);
     						}
     					}
     				});
 				}
 			}
 		}).start();
 	}
 
 }
