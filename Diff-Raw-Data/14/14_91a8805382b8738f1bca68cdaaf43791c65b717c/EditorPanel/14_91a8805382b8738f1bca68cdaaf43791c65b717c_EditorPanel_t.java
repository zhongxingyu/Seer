 package com.bugfullabs.mapeditor;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.AffineTransform;
 
 import javax.swing.JPanel;
 
 
 @SuppressWarnings("serial")
 public class EditorPanel extends JPanel implements MouseListener {
 	
 	private Graphics2D g2d;
 	private boolean drawing = false;
 	private TexturePack tx;
 	private static int item_id;
 	private static PlayerEntity player;
 	
 	public Level level;
 	
 	EditorPanel(String name, TexturePack texture, int x, int y, int width, int height){
 		super();
 		
 		level = new Level(1, 1, "texturepack_02", width, height);
 		player = new PlayerEntity(-1, -1, 0, 1);
 		level.setPlayer(player);
 		
 		addMouseListener(this);
 
 		this.tx = texture;
 		
 		this.setName(name);
 		this.setBounds(x, y, width, height);
 		this.setBackground(Color.DARK_GRAY);
 	}
 	
 	@Override
 	public void paintComponent(Graphics g){
 		
 		g2d = (Graphics2D) g;
 		g2d.clearRect(0, 0, this.getWidth(), this.getHeight());
 		g2d.drawImage(tx.getTextureRegion(16), null, 0, 0);
 
 		drawGrid(32, 32);
 		
		if (player.getRow() >= 0 && player.getColumn() >= 0)
 			g2d.drawImage(tx.getTextureRegion(0), null, player.getColumn()*32, player.getRow()*32);
 
 		if (drawing)
 			if(level.getWidth() > 0){
 				
 				int j = 0;
 				int k = 0;
 				
 				for(int i = 0; i < level.getWidth(); i++){
 	
 					if(level.getItem(k, j) != 0 && level.getItem(k, j) != 2) {
 						g2d.drawImage(tx.getTextureRegion(level.getItem(k, j)), null, k*32, j*32);
 					}
 					else if(level.getItem(k, j) == 2) {
 						AffineTransform xform = new AffineTransform();
 						xform.translate(k*32, j*32);
 						xform.rotate(player.getDir()*(Math.PI/2), tx.getTextureRegion(level.getItem(k, j)).getWidth()/2, tx.getTextureRegion(level.getItem(k, j)).getHeight()/2);
 						g2d.drawImage(tx.getTextureRegion(level.getItem(k, j)), xform, null);
 						System.out.println("finish placed, rotation: " + (player.getDir()*(Math.PI/2)));
 					}
 					
 					k++;
 			
 					if(k >= this.getWidth()/32){
 						k = 0;
 						j++;
 					}
 			
 				}
 	
 			}
 
 		
 	}
 
 	
 	private void drawGrid(int OffX, int OffY){
 	
 		Rectangle rect = this.getBounds();
 		
 		g2d.setColor(Color.gray);
 		
 		for(int i = 0; i < (int)rect.getWidth(); i = i + OffX){
 			g2d.drawLine(i, 0, i, (int) rect.getHeight());
 		}
 		
 		for(int i = 0; i < (int)rect.getHeight(); i = i + OffY){
 			g2d.drawLine(0, i, (int) rect.getWidth(), i);
 		}
 		
 		
 	}
 
 	public static void selectItem(int id) {
 		item_id = id;
 	}
 	
 	public void repaintIt() {
 		repaint();
 	}
 
 	public static void setPlayerDir(int dir) {
 		player.setDir(dir);
 	}
 
 	public static int getPlayerDir() {
 		return player.getDir();
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
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
 	public void mousePressed(MouseEvent e) {
 		
 		drawing = true;
 
 		if (level.getItem(e.getX()/32, e.getY()/32) == 0 && ItemsPanel.playerSelected()) {
 			player.setColumn(e.getX()/32);
 			player.setRow(e.getY()/32);
 		}
 		else if (level.getItem(e.getX()/32, e.getY()/32) == 2 && ItemsPanel.finishSelected()) {
 			if (EditorPanel.getPlayerDir() < 3)
 				EditorPanel.setPlayerDir(EditorPanel.getPlayerDir() + 1);
 			else
 				EditorPanel.setPlayerDir(0);
 			BugfullabsMapEditor.mEditor.mEditorPanel.repaintIt();
 		}
 		
 		System.out.println("position_editor: " + e.getX() + ", " + e.getY() + ", item id: " + item_id);
 		level.setItem(item_id, e.getX()/32, e.getY()/32);
 		repaint();
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		drawing = false;
 		
 	}
 	
 }
