 package mapeditor.panels;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 
 import com.liongrid.gameengine.tools.LVector2;
 import com.liongrid.gameengine.tools.LVector2Int;
 
 import mapeditor.CData;
 import mapeditor.LCollision;
 import mapeditor.LImage;
 import mapeditor.MapData;
 import mapeditor.MapManager;
 import mapeditor.MapObject;
 import mapeditor.MapObject.StaticObject;
 
 public class MapPanel extends JPanel {
 	private JCheckBox grid = new JCheckBox();
 	private int mapWidth;
 	private int mapHeight;
 	private int offsetY;
 	private int offsetX;
 	private int pendingAdditions = 0;
 	private boolean mapReady = false;
 
 	private int[][] mapIndices;
 	private JButton addStaticObject;
 	private JButton removeStaticObject;
 
 	public MapPanel() {
 		addStaticObject = new JButton("Add StaticO (" + pendingAdditions + ")");
 		removeStaticObject = new JButton("Remove StaticO");
 		
 		grid.setSelected(true);
 		
 		add(grid);
 		add(addStaticObject);
 		add(removeStaticObject);
 		
 		
 		grid.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				repaint();
 			}
 		});
 		
 		addStaticObject.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				pendingAdditions =+ 1;
 				updateButtons();
 			}
 		});
 		
 		removeStaticObject.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				MapManager.deleteSelectedStaticObject();
 				CData.updateMaps();
 			}
 		});
 		
 
 		addMouseListener(new MouseListener() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				MapPanel panel = (MapPanel) e.getSource();
 				float x = fromWindowX(e.getX(), e.getY());
 				float y = fromWindowY(e.getX(), e.getY());
 
 				if (x < 0) x = 0;
 				if (y < 0) y = 0;
 				if (x > MapData.arrayWidth) x = MapData.arrayWidth - 1;
 				if (y > MapData.arrayHeight) y = MapData.arrayHeight - 1;
 
 				if (CData.staticObjectMode == false) {
 					MapManager.insertBackgroundMapO((int) x, (int) y,
 							CData.curMapO);
 					System.out.println("pressed tile x = " + (int) x + " y = " + (int)y);
 				} else {
					System.out.println("x = " + x + " y = " + y);
 					if(pendingAdditions > 0){
 						insertStaticObject(e.getX(), e.getY());
 						System.out.println("Additions = " + pendingAdditions);
 						if(pendingAdditions > 0) pendingAdditions =- 1;
 						updateButtons();
 					}
 					else{
 						selectStaticObject(e.getX(), e.getY());
 					}
 				}
 				
 				panel.repaint();
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 			}
 		});
 
 		addMouseMotionListener(new MouseMotionListener() {
 
 			@Override
 			public void mouseDragged(MouseEvent e) {
 				float x = fromWindowX(e.getX(), e.getY());
 				float y = fromWindowY(e.getX(), e.getY());
 				if(CData.staticObjectMode){
 					StaticObject selected = CData.selectedStaticObject;
 					if(selected == null) return;
 					selected.setArrayPos(x, y);
 				}
 				else{
 					MapManager.insertBackgroundMapO((int)x, (int)y, CData.curMapO);
 				}
 				MapPanel panel = (MapPanel) e.getSource();
 				panel.repaint();
 			}
 
 			@Override
 			public void mouseMoved(MouseEvent arg0) {
 			}
 
 		});
 	}
 
 	@Override
 	public Dimension getPreferredSize() {
 		// Override this to make the scrolling appear as we want it
 
 		Dimension d = super.getPreferredSize();
 		d.height = mapHeight;
 		d.width = mapWidth;
 		return d;
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		if (!mapReady)
 			return;
 
 		Graphics2D g2d = (Graphics2D) g;
 
 		drawTiles(g2d);
 		drawStaticObjects(g2d);
 
 		if (grid.isSelected()) {
 			paintGrid(g2d);
 		}
 	}
 
 	private void drawStaticObjects(Graphics2D g2d) {
 		for (int i = 0; i < CData.staticObjects.size(); i++) {
 			StaticObject o = CData.staticObjects.get(i);
 			LVector2 pos = o.getArrayPos();
 			int x = toWindowX(pos.x, pos.y);
 			int y = toWindowY(pos.x, pos.y);
 			if(o.hasImage())
 				g2d.drawImage(o.getLImage().getImage(), x, (int) (y - o.getHeight()), null);
 			if(o == CData.selectedStaticObject){
 				g2d.setColor(new Color(1, 0, 0));
 				g2d.drawRect(x, (int) (y - o.getHeight()), o.getMapO().getWidth(), o.getMapO().getHeight());
 				g2d.setColor(new Color(0, 0, 0));
 			}
 		}
 	}
 
 	private void drawTiles(Graphics2D g2d) {
 		LVector2Int center;
 		for (int x = 0; x < MapData.arrayWidth; x++) {
 			for (int y = 0; y < MapData.arrayHeight; y++) {
 				MapObject mapO = CData.mapObjects
 						.get((int) CData.backgroundObjectsIDs[x][y]);
 				if (mapO == null || mapO.getLImage() == null
 						|| mapO.getLImage().getImage() == null)
 					continue;
 				center = mapO.getCenter();
 				int isoX = toWindowX(x, y) - center.x;
 				int isoY = toWindowY(x, y) + center.y;
 				LImage lImg = mapO.getLImage();
 				g2d.drawImage(lImg.getImage(), isoX, isoY - mapO.getHeight(),
 						null);
 			}
 		}
 	}
 
 	private void paintGrid(Graphics2D g2d) {
 		if(mapIndices == null) return;
 		for (int x = 0; x < mapIndices.length; x++) {
 			for (int y = mapIndices[x][0]; y <= mapIndices[x][1]; y++) {
 				drawSquare(g2d, x, y);
 			}
 		}
 		drawMapBounds(g2d);
 	}
 
 	private void drawMapBounds(Graphics2D g2d) {
 		g2d.drawRect(0 + offsetX, -mapHeight + offsetY, mapWidth, mapHeight);
 	}
 
 	private void drawSquare(Graphics2D g2d, int x, int y) {
 		int x1 = toWindowX(x, y);
 		int y1 = toWindowY(x, y);
 		int x2 = toWindowX(x + 1, y);
 		int y2 = toWindowY(x + 1, y);
 		g2d.drawLine(x1, y1, x2, y2);
 		x1 = toWindowX(x + 1, y + 1);
 		y1 = toWindowY(x + 1, y + 1);
 		g2d.drawLine(x2, y2, x1, y1);
 		x2 = toWindowX(x, y + 1);
 		y2 = toWindowY(x, y + 1);
 		g2d.drawLine(x1, y1, x2, y2);
 		x1 = toWindowX(x, y);
 		y1 = toWindowY(x, y);
 		g2d.drawLine(x2, y2, x1, y1);
 	}
 	
 	/**
 	 *  x and y coordinates in window coordinates
 	 * @param x
 	 * @param y
 	 */
 	private void selectStaticObject(int x, int y){
 		for (int i = 0; i < CData.staticObjects.size(); i++) {
 			StaticObject o = CData.staticObjects.get(i);
 			if(LCollision.collides(x + offsetX, offsetY - y, o)){
 				MapManager.selectStaticObject(o);
 				return;
 			}
 			else MapManager.selectStaticObject(null);
 		}
 	}
 	
 	private void insertStaticObject(int x, int y){
 		if(!CData.staticObjectMode || CData.curMapO == null) return;
 		float carthX = fromWindowX(x, y);
 		float carthY = fromWindowY(x, y);
 		MapManager.insertStaticObject(CData.curMapO.createStaticObject(carthX, carthY));
 	}
 	
 	private float fromWindowX(int x, int y) {
 		return MapData.fromIsoToCartX(x + offsetX, -y + offsetY);
 	}
 
 	private float fromWindowY(int x, int y) {
 		return MapData.fromIsoToCartY(x + offsetX, -y + offsetY);
 	}
 
 	private int toWindowX(float x, float y) {
 		return (int) (MapData.fromCartToIsoX(x, y) + offsetX);
 	}
 
 	private int toWindowY(float x, float y) {
 		return (int) (-MapData.fromCartToIsoY(x, y) + offsetY);
 	}
 
 	public void loadMap() {
 		updateButtons();
 		mapIndices = MapData.getMapIndices();
 		mapHeight = MapData.mapHeight;
 		mapWidth = MapData.mapWidth;
		offsetX = 30;
 		offsetY = mapHeight;
		offsetY += 50;
 		mapReady = true;
 		repaint();
 	}
 	
 	public void updateButtons(){
 		addStaticObject.setText("Add StaticO (" + pendingAdditions + ")");
 		addStaticObject.setEnabled(CData.staticObjectMode);
 		removeStaticObject.setEnabled(CData.staticObjectMode);
 	}
 
 }
