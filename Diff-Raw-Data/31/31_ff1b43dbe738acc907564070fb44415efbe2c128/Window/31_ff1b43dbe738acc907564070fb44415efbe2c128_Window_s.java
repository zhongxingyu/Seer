 package window;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 
 import compositor.Compositor;
 
 import application.Application;
 
 public class Window {
 	
	public static final Integer marginTop=20, margin=4, iconSize=10, defaultSizeX=100, defaultSizeY=100, 
 						sizeButton = marginTop-2*margin;
 	
 	private Integer posiX, posiY, width, height;
 	private Integer oldPosiX, oldPosiY, oldWidth, oldHeight;
 	private Integer posiXClose, posiXMaximize, posiXIconify;
 	private Boolean maximised=false;
 	private BufferedImage image;
 	
 	private Application app;
 	
 	public Window(Application parApp, int x, int y, int w, int h) {
 		
 		posiX = x; posiY = y;
 		width=w; height=h;
 		calculatePosiXButtons();
 		
 		app = parApp;
 		image = new BufferedImage(width-2*margin, height-marginTop-margin, BufferedImage.TYPE_INT_ARGB);
 		app.setPadre(this);
 		maj();
 	}
 	
 	public Window(Application parApp, int x, int y) {
 		this(parApp, x, y, defaultSizeX, defaultSizeY);
 	}
 	
 	@Override
 	protected void finalize() throws Throwable {
 		app = null;
 		super.finalize();
 	}
 	
 	public Rectangle draw(Graphics2D context) {
 		
 		context.setColor(Color.black);
 		context.fillRect(posiX, posiY, width, marginTop); // dessus
 		context.fillRect(posiX, posiY, margin, height); // coté gauche
 		context.fillRect(posiX, posiY+height-margin, width, margin); // dessous
 		context.fillRect(posiX+width-margin, posiY, margin, height); // coté droit
 		
 		
 		//croix
 		context.setColor(Color.white);
 		context.drawRect(posiXClose, posiY+margin, sizeButton, sizeButton);
 		context.drawLine(posiXClose, posiY+margin, posiX+width-margin, posiY+marginTop-margin);
 		context.drawLine(posiXClose, posiY+marginTop-margin, posiX+width-margin, posiY+margin);
 		
 		//maximisation
 		context.drawRect(posiXMaximize, posiY+margin, sizeButton, sizeButton);
 		context.drawRect(posiXMaximize+margin/2, posiY+margin+margin/2, 
 				marginTop-3*margin, marginTop-3*margin-margin/2);
 		
 		// minimalisation
 		context.drawRect(posiXIconify, posiY+margin, sizeButton, sizeButton);
 		context.fillRect(posiXIconify+margin/2, posiY+marginTop-2*margin, marginTop-3*margin, margin/2);
 		
 		context.clipRect(posiX+margin, posiY+marginTop, 
 				width-2*margin, height-marginTop-margin);
 
 		context.drawImage(image, null, posiX+margin, posiY+marginTop);
 		
 		return new Rectangle(posiX, posiY, width, height);
 	}
 
 	public Rectangle drawIcon(Graphics2D context, Integer x, Integer y) {
 		context.setColor(Color.black);
		context.drawLine(x, y, x+iconSize, y);
		context.drawLine(x, y, x, y+iconSize);
		context.drawLine(x+iconSize, y+iconSize, x+iconSize, y);
		context.drawLine(x, y+iconSize, x+iconSize, y+iconSize);
 		
 		context.drawImage(image.getScaledInstance(iconSize-2, iconSize-2, Image.SCALE_DEFAULT), x+1, y+1, null);
 		
 		return new Rectangle(x, y, iconSize, iconSize);
 	}
 	
 	public void maj() {
 		app.draw(image.createGraphics(), width-2*margin, height-marginTop-margin);
 		Compositor.getInstance().repaint(posiX, posiY, width, height);
 	}
 	
 	public void save() {
 		oldPosiX=posiX; 
 		oldPosiY=posiY; 
 		oldWidth=width; 
 		oldHeight=height;
 	}
 	
 	public void restore() {
 		posiX=oldPosiX; 
 		posiY=oldPosiY;
 		width=oldWidth;
 		height=oldHeight;
 		calculatePosiXButtons();
 		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 	}
 	
 	private void calculatePosiXButtons() {
 		posiXClose = posiX+width-marginTop+margin;
 		posiXMaximize = posiX+width-2*(marginTop-margin);
 		posiXIconify = posiX+width-3*(marginTop-margin);
 	}
 	
 	public void translate(Integer x, Integer y) {
 		posiX+=x;
 		posiY+=y;
 		calculatePosiXButtons();
 	}
 	
 	//fonctions de gestion des evenements
 	public void mousePressed(MouseEvent e) {
 		if(Compositor.collision(e.getX(), e.getY(), margin, marginTop, width-2*margin, height-margin-marginTop)) {
 			e.translatePoint(-margin, -marginTop);
 			app.mousePressed(e);
 		}
 	}
 	public void mouseReleased(MouseEvent e) {
 		if(Compositor.collision(e.getX(), e.getY(), margin, marginTop, width-2*margin, height-margin-marginTop)) {
 			e.translatePoint(-margin, -marginTop);
 			app.mouseReleased(e);
 		}
 	}
 	public void mouseMoved(MouseEvent e) {
 		if(Compositor.collision(e.getX(), e.getY(), margin, marginTop, width-2*margin, height-margin-marginTop)) {
 			e.translatePoint(-margin, -marginTop);
 			app.mouseMoved(e);
 		}
 	}
 	public void mouseDragged(MouseEvent e) {
 		if(Compositor.collision(e.getX(), e.getY(), margin, marginTop, width-2*margin, height-margin-marginTop)) {
 			e.translatePoint(-margin, -marginTop);
 			app.mouseDragged(e);
 		}
 	}
 	public void mouseClicked(MouseEvent e) {
 		if(Compositor.collision(e.getX(), e.getY(), margin, marginTop, width-2*margin, height-margin-marginTop)) {
 			e.translatePoint(-margin, -marginTop);
 			app.mouseClicked(e);
 		}
 	}
 	public void mouseWheel(MouseEvent e) {
 		
 	}
 	public void mouseEntered(MouseEvent e) {
 		
 	}
 	public void mouseExited(MouseEvent e) {
 		
 	}
 	public void keyPressed(KeyEvent e) { app.keyPressed(e); }
 	public void keyReleased(KeyEvent e) { app.keyReleased(e); }
 	public void keyTyped(KeyEvent e) { app.keyTyped(e); }
 	
 	//getters / setters ------------------------------------------------------------------------
 	public Integer getPosiXClose() {
 		return posiXClose;
 	}
 	public Integer getPosiXMaximize() {
 		return posiXMaximize;
 	}
 	public Integer getPosiXIconify() {
 		return posiXIconify;
 	}
 	public Integer getPosiX() {
 		return posiX;
 	}
 	public void setPosiX(Integer posiX) {
 		this.posiX = posiX;
 		calculatePosiXButtons();
 	}
 	public Integer getPosiY() {
 		return posiY;
 	}
 	public void setPosiY(Integer posiY) {
 		this.posiY = posiY;
 	}
 	public Integer getWidth() {
 		return width;
 	}
 	public void setWidth(Integer parWidth) {
 		if(parWidth>3*marginTop) {
 			width=parWidth;
 			calculatePosiXButtons();
 			image = new BufferedImage(width-2*margin, height-marginTop-margin, BufferedImage.TYPE_INT_ARGB);
 			this.maj();
 		}
 	}
 	public Integer getHeight() {
 		return height;
 	}
 	public void setHeight(Integer parHeight) {
 		if(parHeight>=2*marginTop) {
 			height=parHeight;
 			image = new BufferedImage(width-2*margin, height-marginTop-margin, BufferedImage.TYPE_INT_ARGB);
 			this.maj();
 		}
 	}
 	public Boolean isMaximised() {
 		return maximised;
 	}
 	public void setMaximised(Boolean maximised) {
 		this.maximised = maximised;
 	}
 }
