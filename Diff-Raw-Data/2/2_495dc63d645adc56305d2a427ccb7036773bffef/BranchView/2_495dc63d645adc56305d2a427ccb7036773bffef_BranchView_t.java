 package GAIL.src.view;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 import GAIL.src.view.AnchorSet.Anchor;
 import GAIL.src.model.Branch;
 
 public class BranchView extends EdgeView {
 
 	Branch branch;
 	int x, y, size = 18;
 	BufferedImage xOrange;
 	
 	public BranchView(){
 		try{
			xOrange= ImageIO.read(new File("src/GAIL/image/xorange.png"));
 		}catch(Exception e){}
 	}
 	
 	public boolean isClicked(int mouseX, int mouseY) {
 		if (mouseX > x - size / 2 && mouseX < x + size / 2 && mouseY > y - size / 2 && mouseY < y + size / 2) {
 			return true;
 		}
 		return false;
 	}
 
 	public void setBranch(Branch branch) {
 		this.branch = branch;
 	}
 
 	// draw this branch
 	public void draw(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g2.setStroke(new BasicStroke(2));
 		g2.setColor(Color.orange);
 		if (sourceAnchor != null && targetAnchor != null) {
 			g2.drawLine(sourceAnchor.getLocation().x, sourceAnchor.getLocation().y,
 					targetAnchor.getLocation().x, targetAnchor.getLocation().y);
 		}
 		x = sourceAnchor.getLocation().x / 2 + targetAnchor.getLocation().x / 2;
 		y = sourceAnchor.getLocation().y / 2 + targetAnchor.getLocation().y / 2;
 		g.drawImage(xOrange, x - xOrange.getWidth() / 2, y - xOrange.getHeight() / 2, null);	}
 
 	Anchor sourceAnchor;
 	Anchor targetAnchor;
 
 	public void addAnchorActivePoint(Anchor sourceAnchor, Anchor targetAnchor) {
 		this.sourceAnchor = sourceAnchor;
 		this.targetAnchor = targetAnchor;
 	}
 
 	@Override
 	ArrayList<Point> getConnectingPoints() {
 		return null;
 	}
 
 }
