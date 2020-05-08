 package views;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 
 import model.FeuilleDeRoute;
 import model.Node;
 import model.ZoneGeo;
 
 import supervision.Controleur;
 import supervision.Dessin;
 
 public class ViewMain {
 	Controleur controleur;
 	Dessin dessin;
 	ViewZoneGeo zonegeo;
 	ViewFeuilleDeRoute feuilleDeRoute;
 	int border = 20;
 	java.awt.Image image;
 	BufferedImage img;
 
 	
 	public ViewMain(Dessin d) 
 	{
 		dessin = d;
 		zonegeo = null;
 		feuilleDeRoute = null;
 	}
 	
 	public void repaint() {
 		dessin.repaint();
 	}
 	
 	public void setZoneGeo(ZoneGeo zg,File path) {
 		zonegeo = new ViewZoneGeo(zg, this);
 		 try {
 			  img= ImageIO.read(new File(path.toString().substring(0, path.toString().length()-3)+"png"));
 			  image=  img.getScaledInstance(dessin.getWidth(),dessin.getHeight(),BufferedImage.SCALE_DEFAULT);
 		} catch (IOException e) {
 		}
 	}
 	
 	public void setFeuilleDeRoute(FeuilleDeRoute f) {
 		feuilleDeRoute = new ViewFeuilleDeRoute(f, this);
 	}
 	
 	public int xpix(int xplan) {
 		int canvasWidth = dessin.getWidth() - 2*border;
 		float xpourcent = (xplan - zonegeo.getXmin()) / (float)zonegeo.getWidth();
 		return (int) (border + xpourcent * canvasWidth);
 	}
 	
 	public int ypix(int yplan) {
 		int canvasHeight = dessin.getHeight() - 2*border;
 		float ypourcent = (yplan - zonegeo.getYmin()) / (float)zonegeo.getHeight();
 		return (int) (border + ypourcent * canvasHeight);
 	}
 	
 	public Object findAt(int x, int y, boolean onlyArcs) {
 		return zonegeo.findAt(x, y, onlyArcs);
 	}
 	
 	public void paint(Graphics g) {
 		dessin.setBackground(new Color(255, 255, 255));
 		if (zonegeo != null) {
 			if (img != null) {
 				image=  img.getScaledInstance(dessin.getWidth(),dessin.getHeight(),BufferedImage.SCALE_DEFAULT);
 				g.drawImage(image, 0, 0, null);
 			}
 			zonegeo.paint(g);
 		}
 		if (feuilleDeRoute != null) {
 			feuilleDeRoute.paint(g);
 		}
 	}
 
 	public void setControleur(Controleur ctrl) {
 		controleur = ctrl;
 	}
 	
 	public Controleur getControleur() {
 		return controleur;
 	}
 	
 	public ViewNode getNode(Node n) {
 		return zonegeo.getNode(n);
 	}
 
 	public void updateFeuilleDeRoute() {
 		feuilleDeRoute.update();
 	}
 	
 	public void updatePulseSleep(int sleepTime) {
		feuilleDeRoute.setPulseSleepTime(sleepTime);
 	}
 }
