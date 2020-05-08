 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 public class InventoryPanel extends JPanel implements MouseListener, Serializable {
 
 	private Game myGame;
 	Item selected;
 	SidePanel sidePanel;
 	boolean combineItem;
 
 	public InventoryPanel(Game g, SidePanel side) {
 		this.addMouseListener(this);
 		sidePanel = side;
 		this.myGame = g;
 		Dimension size = new Dimension(100, 400);
 		combineItem = true;
 		combineItem = false;
 		setPreferredSize(size);
 
 	
 	}
 
 	public void checkCombineItem() {
 		if (((returnSelected().getName().equals("dynamite")) | 
 				(returnSelected().getName().equals("string"))) && 
 				(myGame.getCurrentPlayer().getInventory().size() == 5)) {
			System.out.println("combine true");
 		combineItem = true;
 		}
 		else {
 			combineItem = false;
 		}
 	}
 	
 	@Override
 	public void paintComponent(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setColor(Color.WHITE);
 		g2.fillRect(0, 0, 100, 400);
 		ArrayList<Item> inv = myGame.getCurrentPlayer().getInventory();
 		for (int i = 0; i < inv.size(); i++) {
 			g2.drawImage(inv.get(i).getItemImage().getImage(), inv.get(i)
 					.getRegion().getX(), inv.get(i).getRegion().getY(), null);
 		}
 		g2.setColor(Color.RED);
 		g2.setStroke(new BasicStroke(5));
 		if (selected != null) {
 			g.drawRect(selected.getRegion().getX(),
 					selected.getRegion().getY(), selected.getRegion()
 							.getWidth(), selected.getRegion().getHeight());
 		}
 	
 		}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		if (myGame.getCurrentPlayer().getInventory().size() > 0) {
 		checkCombineClick(arg0.getPoint());
 		checkImageClick(arg0.getPoint());
 		this.repaint();
 		sidePanel.updateText();
 		}
 		
 
 	}
 
 	public Item returnSelected() {
 		return this.selected;
 	}
 
 	public void setSelected(Item i) {
 		this.selected = i;
 	}
 	public void checkCombineClick(Point p) {
 		if (combineItem) {
 			if (selected.getName().equals("dynamite")) {
 				if (myGame.getCurrentPlayer().getInventory().get(3).getRegion().isInsideRegion((int) p.getX(),(int)p.getY())) {
 					myGame.getCurrentPlayer().removeItem(myGame.getCurrentPlayer().getInventory().get(3));
 					myGame.getCurrentPlayer().removeItem(myGame.getCurrentPlayer().getInventory().get(3));
 					myGame.getCurrentPlayer().addItem(myGame.getDynamiteItem()); 
 					this.setSelected(myGame.getDynamiteItem());
 
 					sidePanel.updateText();
 					checkCombineItem();
 					this.repaint();
 				
 				}
 			} else if (selected.getName().equals("string")) {
 				if (myGame.getCurrentPlayer().getInventory().get(4).getRegion().isInsideRegion((int) p.getX(),(int)p.getY())) {
 					myGame.getCurrentPlayer().removeItem(myGame.getCurrentPlayer().getInventory().get(3));
 					myGame.getCurrentPlayer().removeItem(myGame.getCurrentPlayer().getInventory().get(3));
 					myGame.getCurrentPlayer().addItem(myGame.getDynamiteItem());
 					this.setSelected(myGame.getDynamiteItem());
 
 					sidePanel.updateText();
 					checkCombineItem();
 					this.repaint();
 				}
 			}
 			
 		}
 	}
 	public void checkImageClick(Point p) {
 		for (int i = 0; i < myGame.getCurrentPlayer().getInventory().size(); i++) {
 			if (myGame.getCurrentPlayer().getInventory().get(i).getRegion()
 					.isInsideRegion((int)p.getX(),(int) p.getY())) {
 				setSelected(myGame.getCurrentPlayer().getInventory().get(i));
 			}
 		}
 		checkCombineItem();
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
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
