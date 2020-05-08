 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 class GamePanel extends JPanel implements MouseListener {
 
 	private Image img;
 	private Game myGame;
 	private InventoryPanel invPanel;
 	
 	public GamePanel(Game g, InventoryPanel inventoryPanel) {
 		this.addMouseListener(this);
 		this.invPanel = inventoryPanel;
 		this.myGame = g;
 		this.img = g.getCurrentView().getCurrentImage().getImage();
 		Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
 		setPreferredSize(size);
 		setLayout(new FlowLayout());
 
 	}
 
 	public void paintComponent(Graphics g) {
 		View v;
 		System.out.println(myGame.getLanguage());
		if (myGame.getLanguage() == "french") {
 			v = myGame.getCurrentView().getFrenchAlternate();
 		}
 		else {
 			v = myGame.getCurrentView();
 		}
 		g.drawImage(v.getCurrentImage().getImage(), 0, 0,
 				null);
 		ArrayList<Region> regionList = v.getRegions();
 		for (int i = 0; i < regionList.size(); i++) {
 			g.drawRect(regionList.get(i).getX(), regionList.get(i).getY(),
 					regionList.get(i).getWidth(), regionList.get(i).getHeight());
 		}
 	}
 
 	public void checkRegion(int x, int y) {
 		ArrayList<Region> regionList = myGame.getCurrentView().getRegions();
 		Region currentRegion;
 
 		for (int i = 0; i < regionList.size(); i++) {
 			currentRegion = regionList.get(i);
 			if (currentRegion.isInsideRegion(x, y)) {
 				if (regionList.get(i).hasItem()) {
 					if (!this.myGame.getCurrentPlayer().getInventory()
 							.contains(currentRegion.getItem())) {
 						myGame.changeView(myGame.getCurrentView().getViews()
 								.get(0));
 						this.myGame.getCurrentPlayer().addItem(
 								currentRegion.getItem());
 						this.invPanel.updateInventory();
 						this.repaint();
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		checkRegion(arg0.getX(), arg0.getY());
 		
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
