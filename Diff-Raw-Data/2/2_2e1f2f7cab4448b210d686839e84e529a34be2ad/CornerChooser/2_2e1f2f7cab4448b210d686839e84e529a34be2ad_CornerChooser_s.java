 package edu.berkeley.gcweb.gui.gamescubeman.PuzzleUtils;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.HashMap;
 
 import javax.swing.JButton;
 import javax.swing.Timer;
 
 import edu.berkeley.gcweb.gui.gamescubeman.Cuboid.Cuboid;
 import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.Canvas3D;
 import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.Polygon3D;
 import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.RotationMatrix;
 
 public class CornerChooser extends RollingJPanel implements MouseListener, MouseMotionListener, ComponentListener, KeyListener, ActionListener {
 	private static final int PREFERRED_HEIGHT = 50;
 	private static final int STICKER_LENGTH = (int) (.3* PREFERRED_HEIGHT);
 	private HashMap<String, Color> colors;
 	private Canvas3D paintCanvas;
 	private AppletSettings settings;
 	private HashMap<GeneralPath, String> StickerColor;
 	private String selectedCorner = null;
 	private HashMap<String, Rectangle2D> colorRectangles;
 	private PuzzleCanvas puzzlecanvas;
 	private HashMap<String,String> cornermap;
 	private Cuboid cuboid;
 	private int flip;
 	private String lcach;
 	private HashMap<String, Integer> dupcheck;
 
 	
 	public CornerChooser(AppletSettings settings, HashMap<String, Color> colorScheme, Canvas3D paintCanvas, PuzzleCanvas puzzlecanvas){
 		this.paintCanvas = paintCanvas;
 		this.settings = settings;
 		this.puzzlecanvas = puzzlecanvas;
 		this.cuboid = (Cuboid) puzzlecanvas.getPuzzle();
 		cornermap = new HashMap<String,String>();
 		setLayout(new BorderLayout());
 		setPreferredSize(new Dimension(100, PREFERRED_HEIGHT));
 		setOpaque(true);
 		
 		colors = colorScheme;
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		addComponentListener(this);
 		setOpaque(true);
 		
 		StickerColor = new HashMap<GeneralPath, String>();
 		cornermap = new HashMap<String, String>();
 		
 		flip = 0;
 		lcach = "";
 	}
 
 	private void uniColor(){
 		for (PuzzleSticker[][] a : cuboid.cubeStickers)
 			for(PuzzleSticker[] b: a)
 				for (PuzzleSticker c: b){
 					c.setFace(null);
 				}
 		RotationMatrix rm = new RotationMatrix(1, 45);
 		rm = new RotationMatrix(0, -45).multiply(rm);
 		cuboid.setRotation(rm);
 		cuboid.fireStateChanged(null);
 		
 	}
 	
 
 	private GeneralPath drawSticker(Graphics2D g2d, float x, float y, double theta, Color c){
 		GeneralPath p = new GeneralPath();
 		p.moveTo(x,y);
 		double t =Math.sin((Math.PI)/3);
 		float a = (float) (STICKER_LENGTH*Math.sin(Math.PI/3*2));
 		float b = (float) (STICKER_LENGTH*Math.sin(Math.PI/3));
 		p.lineTo((float)(x-STICKER_LENGTH*Math.cos(Math.PI/6)),(float)(y-STICKER_LENGTH*Math.sin(Math.PI/6)));
 		p.lineTo((float)(x),(float)(y-2*STICKER_LENGTH*Math.sin(Math.PI/6)));
 		p.lineTo((float)(x+STICKER_LENGTH*Math.cos(Math.PI/6)),(float)(y-STICKER_LENGTH*Math.sin(Math.PI/6)));
 		p.closePath();
 		p.transform(AffineTransform.getRotateInstance(theta, x, y));
 		g2d.setColor(c);
 		g2d.fill(p);
 		g2d.setColor(Color.WHITE);
 		g2d.draw(p);
 		
 		return p;
 	}
 	private void drawCorner(Graphics2D g2d, float x, float y, String a, String b, String c){
 		GeneralPath p;
 		Color[] stickers = new Color[3];
 		String faces = a+","+b+","+c;
 		stickers[0]=colors.get(a);
 		stickers[1]=colors.get(b);
 		stickers[2]=colors.get(c);
 		p=drawSticker(g2d,x,y,0,stickers[0]);
 		StickerColor.put(p, faces);
 		p=drawSticker(g2d,x,y,Math.PI/3*2,stickers[1]);
 		StickerColor.put(p, faces);
 		p=drawSticker(g2d,x,y,Math.PI/3*4,stickers[2]);
 		StickerColor.put(p, faces);
 		
 		
 	}
 	private void paintkeyChar(String k, int x, int y, Graphics2D g2d){
 		g2d.setColor(Color.WHITE);
 		g2d.drawString(k, x-2, y);
 	}
 	
 	private void cornertableUpdate(String stickers, String idx){
 
 		cornermap.put(idx, stickers);
 		
 	}
 	protected void paintComponent(Graphics g) {
 		
 		
 		Graphics2D g2d = (Graphics2D) g;
 		if(isOpaque()) {
 			g2d.setColor(Color.BLACK);
 			g2d.fillRect(0, 0, getWidth(), getHeight());
 		}
 		double gap = (double) getWidth() / 15;
 		int x = 40;
 		
 		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"U","L","B");
 		paintkeyChar("A", x, PREFERRED_HEIGHT, g2d);
 		cornertableUpdate("U,L,B","a");
 		
 		x +=STICKER_LENGTH+gap;
 		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"U","F","L");
 		paintkeyChar("S", x, PREFERRED_HEIGHT, g2d);
 		cornertableUpdate("U,F,L","s");
 		
 		x +=STICKER_LENGTH+gap;
 		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"U","R","F");
 		paintkeyChar("D", x, PREFERRED_HEIGHT, g2d);
 		cornertableUpdate("U,R,F","d");
 		
 		
 		x +=STICKER_LENGTH+gap;
 		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"U","B","R");
 		paintkeyChar("F", x, PREFERRED_HEIGHT, g2d);
 		cornertableUpdate("U,B,R","f");
 		
 		x +=STICKER_LENGTH+gap;
		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"D","F","L");
 		paintkeyChar("J", x, PREFERRED_HEIGHT, g2d);
 		cornertableUpdate("D,B,L","j");
 		
 		
 		x +=STICKER_LENGTH+gap;
 		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"D","L","F");
 		paintkeyChar("K", x, PREFERRED_HEIGHT, g2d);
 		cornertableUpdate("D,L,F","k");
 		
 		x +=STICKER_LENGTH+gap;
 		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"D","F","R");
 		paintkeyChar("L", x, PREFERRED_HEIGHT, g2d);
 		cornertableUpdate("D,F,R","l");
 		
 		x +=STICKER_LENGTH+gap;
 		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"D","R","B");
 		paintkeyChar(";", x, PREFERRED_HEIGHT, g2d);
 		cornertableUpdate("D,R,B",";");
 		
 		
 		
 		paintkeyChar("SPACE", x+STICKER_LENGTH*2,PREFERRED_HEIGHT-25, g2d);
 		paintkeyChar("BACKSPACE", x+STICKER_LENGTH*2,PREFERRED_HEIGHT-15, g2d);
 		paintkeyChar("to iterate", x+STICKER_LENGTH*2,PREFERRED_HEIGHT, g2d);
 		colorRectangles = new HashMap<String, Rectangle2D>();
 		for(String face : colors.keySet()) {
 			colorRectangles.put(face, new Rectangle2D.Double());
 		}
 		colorRectangles.put("null", new Rectangle2D.Double());
 		
 	}
 
 	private GeneralPath getClickedGP(){
 		Point p = getMousePosition();
 		if(p == null) return null;
 		for(GeneralPath face : StickerColor.keySet())
 			if(face.contains(p)){
 				return face;
 			}
 		return null;
 	}
 	public String getClickedFace() {
 		GeneralPath g = getClickedGP();
 		if (g == null) return null;
 		return StickerColor.get(g);
 		/*
 		Point p = getMousePosition();
 		System.out.println(p);
 		if(p == null) return null;
 		for(GeneralPath face : StickerColor.keySet())
 			if(face.contains(p)){
 				System.out.println(StickerColor.get(face));
 				return StickerColor.get(face);
 			}
 		return null;*/
 	}
 	
 	private void pieceRotate(GeneralPath g){
 		if(StickerColor.containsKey(g)){
 			String[] swap = StickerColor.get(g).split(",");
 			StickerColor.put(g, swap[1]+","+swap[2]+","+swap[0]);
 		}	
 	}
 	private void refreshCursor() {
 		Cursor c = selectedCorner == null ? Cursor.getDefaultCursor() : createCursor(selectedCorner);
 		this.setCursor(c);
 		paintCanvas.setCursor(c);
 		repaint();
 	}
 	
 	private static final int CURSOR_SIZE = 32;
 	private Cursor createCursor(String c) {
 		String[] faces =getClickedFace().split(",");
 		BufferedImage buffer = new BufferedImage((int) (2*CURSOR_SIZE*Math.cos(Math.PI/6)), (int) (3*CURSOR_SIZE*Math.sin(Math.PI/6)), BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g2d = (Graphics2D) buffer.createGraphics();
 		
 		drawCorner(g2d, (float)(CURSOR_SIZE*Math.cos(Math.PI/6)), (float)(CURSOR_SIZE*Math.sin(Math.PI/6)), faces[0],faces[1], faces[2]);
 		
 		Toolkit tool = Toolkit.getDefaultToolkit();
 		return tool.createCustomCursor(buffer, new Point(0, 0), "bucket");
 	}
 
 	public void mouseClicked(MouseEvent e) {
 		String face = getClickedFace();
 		if(face != null)/*
 		String[] faces = getClickedFace().split(",");
 		Color[] face = new Color[3];
 		face[0]=colors.get(faces[0]);
 		face[1]=colors.get(faces[1]);
 		face[2]=colors.get(faces[2]);		
 		if(face != null) */{
 			System.out.println("face is "+face);
 			if (!face.equals(selectedCorner))
 				selectedCorner = face;
 			else{
 				pieceRotate(getClickedGP());
 				selectedCorner = getClickedFace();
 			}
 			refreshCursor();
 			System.out.println("Is corner changed?"+ getClickedFace());
 		}
 	}
 	
 	public String getSelectedFace() {
 		return selectedCorner;
 	}
 	
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void mouseDragged(MouseEvent e) {
 		repaint();
 	}
 	public void mouseMoved(MouseEvent e) {
 		repaint();
 	}
 	public void componentHidden(ComponentEvent e) {}
 	public void componentMoved(ComponentEvent e) {}
 	public void componentResized(ComponentEvent e) {}
 	public void componentShown(ComponentEvent e) {}
 	
 	private void setCorner(int currentCorner, String s, String corner) {
 		String[] cs = corner.split(",");
 		PuzzleSticker[] ps = cuboid.getCorner(currentCorner);
 		if (lcach.equals(s))
 			flip+=1;
 		else{
 			flip = 0;
 			lcach = s;
 		}
 		for (int i = 0; i < ps.length; i++) {
 			ps[i].setFace(cs[(i+flip)%3]);
 		}
 		Integer dupcorner = dupcheck.get(s);
 		if(dupcorner != null && dupcorner != currentCorner) {
 			PuzzleSticker[] ps2 = cuboid.getCorner(dupcorner);
 			for (int i = 0 ; i< ps2.length; i++)
 				ps2[i].setFace(null);
 			
 		}
 		for (String k:((HashMap<String, Integer>)dupcheck.clone()).keySet()){
 			if(dupcheck.get(k).equals(currentCorner)){
 				dupcheck.remove(k);
 			}
 		}
 		dupcheck.put(s, currentCorner);
 		cuboid.fireCanvasChange();
 	}
 	
 	public void keyPressed(KeyEvent e) {
 		String s = e.getKeyChar() + "";
 		if (cornermap.containsKey(s)) {
 			setCorner(currentCorner, s, cornermap.get(s));
 		} else if(e.getKeyCode() == KeyEvent.VK_SPACE){
 			currentCorner++;
 			lcach="";
 			if (currentCorner>7)
 				currentCorner-=8;
 		}
 			
 		else if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
 			currentCorner--;
 			lcach="";
 			if(currentCorner<0)
 				currentCorner+=8;
 		}
 		if (!cuboid.getState().equals("Invalid")){
 			setVisible(false);
 		}
 	}
 	public void keyReleased(KeyEvent e) {}
 	public void keyTyped(KeyEvent arg0) {}
 
 	private Timer glowTimer = new Timer(100, this);
 	private int currentCorner;
 	public void setVisible(boolean visible){
 		super.setVisible(visible);
 		if(visible) {
 			glowTimer.start();
 			currentCorner = 0;
 			uniColor();
 			dupcheck = new HashMap<String,Integer>();
 			setCorner(7, ";", "D,R,B");
 			cuboid.fireCanvasChange();
 			cuboid.setDisabled(true);
 			paintCanvas.addKeyListener(this);
 		} else {
 			currentCorner = -1;
 			actionPerformed(null);
 			glowTimer.stop();
 			cuboid.setRotation(cuboid.getPreferredViewAngle());
 			cuboid.setDisabled(false);
 			cuboid.fireStateChanged(null);
 			paintCanvas.removeKeyListener(this);
 		}
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		//assuming the event is fired by glowTimer
 		for(int i = 0; i < 8; i++) {
 			PuzzleSticker[] polys = cuboid.getCorner(i);
 			if(polys == null)
 				return;
 			for(PuzzleSticker poly : polys) {
 				poly.setBorderColor(i == currentCorner ? new Color((int) System.currentTimeMillis()) : Color.BLACK);
 				cuboid.fireCanvasChange();
 			}
 		}
 	}
 }
