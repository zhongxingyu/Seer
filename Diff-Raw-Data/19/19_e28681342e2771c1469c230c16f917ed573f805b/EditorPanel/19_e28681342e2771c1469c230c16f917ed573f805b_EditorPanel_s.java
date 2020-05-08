 package com.github.spocot.mythra.mapEditor;
 
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JOptionPane;
 
 import com.github.spocot.mythra.*;
 
 public class EditorPanel extends AnimationPanel{
 
 	private Block currentBlock = new BlockGrass(0,0);
 
 	private Map map;
 
 	private Block[][] blocks;
 
 	//Constructor for a blank map
 	public EditorPanel() {
 		super(500, 500);
 		map = new Map();
 		blocks = map.getBlocks();
 	}
 
 	@Override
 	public void mouseDraggedEvents(MouseEvent e) {
 		mouseClickedEvents(e);
 	}
 
 	@Override
 	public void mouseMovedEvents(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void keyPressedEvents(KeyEvent e) {
 		int keyCode = e.getKeyCode();
 		switch(keyCode){
 		case KeyEvent.VK_S:saveMap();break;
 		case KeyEvent.VK_L:loadMap();break;
 		}
 	}
 
 	@Override
 	public void keyReleasedEvents(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void changeBlock(Block block){
 		currentBlock = block;
 	}
 
 	@Override
 	public void updateGame() {
 		map.update(blocks);
 		blocks = map.getBlocks();
 	}
 
 	@Override
 	public void drawGame(Graphics g) {
 		map.render(g);
 	}
 
 	@Override
 	public void mouseClickedEvents(MouseEvent e) {
 		int i = e.getX() / 10;
 		int j = e.getY() / 10;
 		blocks[i][j] = new Block(currentBlock.isCollideable(),currentBlock.getColor(), i * 10, j * 10);
 	}
 
 	@Override
 	public void mouseEnteredEvents(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExitedEvents(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressedEvents(MouseEvent e) {
 
 	}
 
 	@Override
 	public void mouseReleasedEvents(MouseEvent e) {
 
 	}
 
 	public void saveMap(){
 		MapLoader loader = new MapLoader(JOptionPane.showInputDialog("Enter the name to save under:"));
 		loader.writeMap(map);
 	}
 
 	public void loadMap(){
 		MapLoader loader = new MapLoader(JOptionPane.showInputDialog("Enter the name of the file to load:"));
 		map = loader.loadMap();
 	}
 
 	public void fillMap(){
 		map = null;
 		map = new Map(currentBlock);
 		blocks = map.getBlocks();
 		System.out.println("Map filled");
 	}
 }
