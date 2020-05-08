 package kimononet.simulation;
 
 import java.awt.AWTEvent;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Line2D;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.awt.image.RescaleOp;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 import kimononet.geo.GeoLocation;
 import kimononet.geo.GeoVelocity;
 import kimononet.geo.GeoMap;
 import kimononet.peer.Peer;
 import kimononet.peer.PeerAgent;
 
 public class SimulationPanel extends JPanel {
 
 	private BufferedImage imageUAV;
 	private int mouseX = -1, mouseY = -1;
 	private GeoMap mapDim;
 	private Simulation simulation;
 
 	private void processDnD(int x, int y, boolean bMove) {
 		ArrayList<PeerAgent> peerAgents = simulation.getPeerAgents();
 		for (int i = 0; i < peerAgents.size(); i++) {
 			Peer peer = peerAgents.get(i).getPeer();
 			GeoLocation location = peer.getLocation();
 			Rectangle rect = new Rectangle();
 
 			if (location != null) {
 				rect.x = (int)(longitudeToX(location.getLongitude()) - (imageUAV.getWidth() / 2));
 				rect.y = (int)(latitudeToY(location.getLatitude()) - (imageUAV.getHeight() / 2));
 				rect.width = rect.x + imageUAV.getWidth();
 				rect.height = rect.y + imageUAV.getHeight();
 
 				if (rect.contains(x, y)) {
 					if (bMove)
						simulation.updateCurrentPeerAgent(new GeoLocation(xToLongitude(mouseX), yToLatitude(mouseY), peer.getLocation().getAccuracy()));
 					simulation.setCurrentPeerIndex(i);
 					simulation.refresh();
 					break;
 				}
 			}
 		}
 	}
 
 	public SimulationPanel(BufferedImage i, GeoMap m, Simulation s) {
 		imageUAV = i;
 		mapDim = m;
 		simulation = s;
 
 	    this.enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
 	}
 
 	public void processMouseMotionEvent(MouseEvent e) {
 		if (e.getID() == MouseEvent.MOUSE_DRAGGED || e.getID() == MouseEvent.MOUSE_MOVED) {
 			mouseX = e.getX();
 			mouseY = e.getY();
 			simulation.getFrame().repaint();
 		}
 
 		if (e.getID() == MouseEvent.MOUSE_DRAGGED)
 			processDnD(mouseX, mouseY, true);
 	}
 
 	public void processMouseEvent(MouseEvent e) {
 		if (e.getID() == MouseEvent.MOUSE_EXITED) {
 			mouseX = -1;
 			mouseY = -1;
 			simulation.getFrame().repaint();
 		}
 
 		if (e.getID() == MouseEvent.MOUSE_PRESSED)
 			processDnD(mouseX, mouseY, false);	// Don't move the UAV, just highlight it.
 
 		super.processMouseEvent(e);
 	}
 
 	private double longitudeToX(double longitude) {
 		double leftBound = mapDim.getUpperLeft().getLongitude();
 		double rightBound = mapDim.getLowerRight().getLongitude();
 		double range = rightBound - leftBound;
 		double panelWidth = (double)this.getBounds().width;
 		double scaler = panelWidth / range;
 		return ((longitude - leftBound) * scaler);
 	}
 
 	private double latitudeToY(double latitude) {
 		double topBound = mapDim.getUpperLeft().getLatitude();
 		double bottomBound = mapDim.getLowerRight().getLatitude();
 		double latitudeRange = topBound - bottomBound;
 		double panelHeight = (double)this.getBounds().height; 
 		double scaler = panelHeight / latitudeRange;
 		return (-(latitude - topBound) * scaler);
 	}
 
 	private double xToLongitude(double x) {
 		double leftBound = mapDim.getUpperLeft().getLongitude();
 		double rightBound = mapDim.getLowerRight().getLongitude();
 		double range = rightBound - leftBound;
 		double panelWidth = (double)this.getBounds().width;
 		double scaler = panelWidth / range;
 		return (x / scaler + leftBound);
 	}
 
 	private double yToLatitude(double y) {
 		double topBound = mapDim.getUpperLeft().getLatitude();
 		double bottomBound = mapDim.getLowerRight().getLatitude();
 		double latitudeRange = topBound - bottomBound;
 		double panelHeight = (double)this.getBounds().height; 
 		double scaler = panelHeight / latitudeRange;
 		return (-y / scaler + topBound);
 	}
 
 	public void paintComponent(Graphics g) {
 		final double GRID_INTERVAL = 0.001d; 
 
 		Graphics2D g2d = (Graphics2D)g;
 
 		g2d.setColor(Color.LIGHT_GRAY);
 
 		// Paint vertical gridlines.
 		for (double i = mapDim.getUpperLeft().getLongitude(); i <= mapDim.getLowerRight().getLongitude(); i += GRID_INTERVAL)
 			g2d.draw(new Line2D.Double(longitudeToX(i), 0, longitudeToX(i), this.getBounds().height));
 
 		// Paint horizontal gridlines.
 		for (double i = mapDim.getLowerRight().getLatitude(); i <= mapDim.getUpperLeft().getLatitude(); i += GRID_INTERVAL)
 			g2d.draw(new Line2D.Double(0, latitudeToY(i), this.getBounds().width, latitudeToY(i)));
 
 		// Paint UAVs.
 		ArrayList<PeerAgent> peerAgents = simulation.getPeerAgents();
 		for (int i = 0; i < peerAgents.size(); i++) {
 			Peer peer = peerAgents.get(i).getPeer();
 
 			GeoLocation location = peer.getLocation();
 
 			int peerX = 0;
 			int peerY = 0;
 
 			// These offsets are to make the center of the image as the origin.
 			int offsetX = imageUAV.getWidth() / 2;
 			int offsetY = imageUAV.getHeight() / 2;
 
 			if (location != null) {
 				peerX = (int)(longitudeToX(location.getLongitude())) - offsetX;
 				peerY = (int)(latitudeToY(location.getLatitude())) - offsetY;
 			}
 
 			AffineTransformOp atop = new AffineTransformOp(AffineTransform.getRotateInstance(peer.getVelocity().getBearing(), offsetX, offsetY), AffineTransformOp.TYPE_BILINEAR);
 			BufferedImage imageUAVRotated = atop.filter(imageUAV, null);
 			if (peer == simulation.getCurrentPeer()) {
 				RescaleOp rop = new RescaleOp(1.2f, 15, null);
 				rop.filter(imageUAVRotated, imageUAVRotated);
 			}
 			g2d.drawImage(imageUAVRotated, peerX, peerY, this);
 		}
 
 		// Paint tooltip.
 		if (mouseX >= 0 && mouseY >= 0) {
 			int tooltipX = mouseX + 10;
 			int tooltipY = mouseY + 20;
 			g2d.setColor(Color.LIGHT_GRAY);
 			g2d.fillRect(tooltipX, tooltipY, 175, 50);
 			g2d.setColor(Color.BLACK);
 			g2d.drawRect(tooltipX, tooltipY, 175, 50);
 			g2d.drawString("Longitude: " + Float.toString((float)xToLongitude(mouseX)) + "", tooltipX + 5, tooltipY + 20);
 			g2d.drawString("Latitude: " + Float.toString((float)yToLatitude(mouseY)) + "", tooltipX + 5, tooltipY + 40);
 		}
 
 	}
 }
