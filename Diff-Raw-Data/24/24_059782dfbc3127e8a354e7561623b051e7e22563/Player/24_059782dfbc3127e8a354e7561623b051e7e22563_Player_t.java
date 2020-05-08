 package rift.objs;
 
import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import rift.RObj;
 import rift.UI;
 
 public class Player extends RObj {
 
 	private int facing;
 	private JPanel plr;
 	private Image img;
 	private JLabel lbl;
 
 	public Player(int x, int y, int face, int height, int width) {
 		super(0, x, y, height, width, true, -1);
 		facing = face;
 		plr = new JPanel();
 		lbl = new JLabel();
 		plr.setOpaque(false);
 		lbl.setOpaque(false);
 		try {
 			if (facing == 1)
 				img = ImageIO.read(new File("pics/plrL.png"));
 			else
 				img = ImageIO.read(new File("pics/plrR.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		lbl.setIcon(new ImageIcon(img));
		plr.setLayout(new BorderLayout());
 		plr.add(lbl);
 		resize();
 	}
 
 	public int getFacing() {
 		return facing;
 	}
 
 	public void setFacing(int facing) {
 		this.facing = facing;
 		try {
 			if (facing == 1)
 				img = ImageIO.read(new File("pics/plrL.png"));
 			else
 				img = ImageIO.read(new File("pics/plrR.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		img = img.getScaledInstance(aWidth, aHeight, Image.SCALE_DEFAULT);
 		lbl.setIcon(new ImageIcon(img));
 	}
 
 	public JPanel getPanel() {
 		return plr;
 	}
 
 	public void resize() {
 		try {
 			if (facing == 1)
 				img = ImageIO.read(new File("pics/plrL.png"));
 			else
 				img = ImageIO.read(new File("pics/plrR.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		aX = UI.getScaledWidth(getX());
 		aY = UI.getScaledHeight(getY());
 		aHeight = UI.getScaledHeight(getHeight());
 		aWidth = UI.getScaledWidth(getWidth());
 		plr.setSize(aWidth, aHeight);
 		plr.setPreferredSize(new Dimension(aWidth, aHeight));
 		plr.setLocation(aX, aY);
 		plr.setMaximumSize(plr.getPreferredSize());
 		plr.setMinimumSize(plr.getPreferredSize());
 		if (aWidth == 0)
 			aWidth = 1;
 		if (aHeight == 0)
 			aHeight = 1;
 		img = img.getScaledInstance(aWidth, aHeight, Image.SCALE_DEFAULT);
 		lbl.setIcon(new ImageIcon(img));
 		plr.repaint();
 		System.out.println("(aX, aY) = (" + aX + ", " + aY + ")");
 	}
 
 	public boolean canIntersect() {
 		return true;
 	}
 
 	public boolean canFireThrough() {
 
 		return true;
 	}
 
 	public int constantAction(ArrayList<RObj> objs) {
 		return 0;
 	}
 
 	public int action(ArrayList<RObj> objs, int actionBy) {
 		return 0;
 	}
 
 }
