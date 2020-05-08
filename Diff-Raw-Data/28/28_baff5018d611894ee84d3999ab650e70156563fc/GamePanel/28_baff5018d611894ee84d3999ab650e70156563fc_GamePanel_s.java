 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.*;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.*;
 
 
 
 public class GamePanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1552746400473185110L;
 
 	private Model d_model;
 	
 	private int d_xStart = -1;
 	private int d_yStart = -1;
 	//private Block d_edit;
 	
 	private float d_scale;
 	
 	public GamePanel() {
 		d_model = new Model();
		d_scale = 1;
 		setFocusable(true);
 		
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		addKeyListener(this);
 	}
 	
 	public Model model() {
 		return d_model;
 	}
 	
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		
 		Graphics2D g2d = (Graphics2D) g;
 		
		d_scale = getHeight() / 768.0f;
		float width = getHeight() * d_scale;
		float horOffset = width - 1024;
 		
		g2d.translate(horOffset, 0);
 		g2d.scale(d_scale, d_scale);
 		
 		g2d.setColor(Color.BLACK);
		g2d.fillRect((int)-horOffset, 0, (int) horOffset, 768);
 		
 		d_model.draw(g2d);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
		int eventX = (int) (e.getX() / d_scale);
 		int eventY = (int) (e.getY() / d_scale);
 		
 		
 		
 		if(e.isShiftDown()) {
 			d_model.removeBlock(eventX, eventY);
 			repaint();
 			return;
 		}
 		
 		d_xStart = (int) (d_model.tileSize() * Math.floor(eventX / (float) d_model.tileSize()));
 		d_yStart = (int) (d_model.tileSize() * Math.floor(eventY / (float) d_model.tileSize()));
 		
 		d_model.setTmpBlock(new Rectangle(d_xStart, d_yStart, 0, 0));
 		
 		//d_edit = new Block(d_xStart, d_yStart, 0, 0);
 		//d_model.addBlock(d_edit);
 		repaint();
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {	
 		if(d_xStart < 0)
 			return;		
 		
 		Rectangle block = d_model.tmpBlock();
 		
 		if(d_model.tmpBlock().height > 0 && d_model.tmpBlock().width > 0)
 			d_model.addBlock(block.x, block.y, block.width, block.height);
 		
 		d_model.setTmpBlock(null);
 		//d_edit = null;
 		d_xStart = -1;
 		d_yStart = -1;
 		
 		repaint();
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
		int eventX = (int) (e.getX() / d_scale);
 		int eventY = (int) (e.getY() / d_scale);
 		
 		if(d_xStart < 0)
 			return;
 		
 		int x = (int) (d_model.tileSize() * Math.floor(eventX / (float) d_model.tileSize()));
 		int y = (int) (d_model.tileSize() * Math.floor(eventY / (float) d_model.tileSize()));
 		
 		Rectangle block = d_model.tmpBlock();
 		block.x = d_xStart;
 		block.y = d_yStart;
 		block.height = y - d_yStart;
 		block.width = x - d_xStart;
 		
 		repaint();
 	    //d_edit.update(d_xStart, d_yStart, , );
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if(e.getKeyCode() == KeyEvent.VK_S)
 			d_model.save();
 		if(e.getKeyCode() == KeyEvent.VK_L)
 			d_model.setDrawPaths(true);
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		if(e.getKeyCode() == KeyEvent.VK_L)
 			d_model.setDrawPaths(false);
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 	}
 }
