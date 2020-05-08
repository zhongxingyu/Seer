 
 package app;
 
 import gfx.*;
 
 import java.awt.*;
 
 import java.awt.event.*;
 import java.awt.image.*;
 import java.util.Hashtable;
 import java.util.LinkedList;
 
 public abstract class AppShell implements Surface {
 	
 	private MyCanvas canvas;
 	private int[] pixels;
 	private Mouse mouse;
 	private Keyboard keyboard;
 	
 	/** 
 	 * Instantiate a basic application shell
 	 */
 	public AppShell(String title, final int W, final int H) {
 		pixels=new int[W*H];
 		mouse=new Mouse();
 		keyboard=new Keyboard();
 		
 		ColorModel m=new DirectColorModel(32, 0xff0000, 0xff00, 0xff);
 		DataBuffer databuffer=new DataBufferInt(pixels, pixels.length);
 		WritableRaster wr=Raster.createWritableRaster(m.createCompatibleSampleModel(W,H), databuffer, null);
 		BufferedImage img=new BufferedImage(m,wr,false,new Hashtable());
 		Frame f=new Frame(title);
 		f.addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent e) { System.exit(0);}});
 		canvas=new MyCanvas(img);
 		f.add(canvas);
 		canvas.setSize(W,H);
 		canvas.addKeyListener(keyboard);
 		canvas.addMouseListener(mouse);
 		f.setResizable(false);
 		f.setVisible(true);
 		Insets insets=f.getInsets();
 		
 		f.setSize(W+insets.left+insets.right, H+insets.top+insets.bottom);
 	}
 	
 	public final void hidemouse(boolean hidden) {
 		Cursor c;
 		if (hidden) {
 			Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, new int[256], 0, 16));
 			c=Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
 		}
 		else {
 			c=Cursor.getDefaultCursor();
 		}
 		canvas.setCursor(c);
 	}
 	
 	public final void run() {
 		while (true) {
 			// Simply tick at 20Hz for now
 			try {	Thread.sleep(20); 		}
 			catch (InterruptedException e) {}
 			// Handle input from the keyboard
 			synchronized(keyboard) {
 				while (keyboard.keypresses.size()>0) {
 					Keyboard.Keypress k=keyboard.keypresses.removeFirst();
 					keypress(k.code,k.ch);
 				}
 				processKeyState(keyboard.keyheld);
 			}
 			// Handle input from the mouse
 			synchronized(mouse) {
 				while (mouse.clicks.size()>0) {
 					Mouse.Click c=mouse.clicks.removeFirst();
 					mouseclick(c.x,c.y,c.b);
 				}
 				Point p=canvas.getMousePosition();
 				if (p!=null) processMouseState((int)p.getX(),(int)p.getY());
 			}
 			
 			update();
 			gfx.Graphics.setSurface(this);
 			render();
 			canvas.repaint();
 		}
     }
 
 	/** We are a graphics surface, after all */
 	public final int[] getPixels() {	return pixels;	}
 	public final int getWidth() {    return canvas.getWidth();	}
 	public final int getHeight() {   return canvas.getHeight();  	}
 
 	/** Handle incoming events */
 	public abstract void mouseclick(int x, int y, int b);
 	public abstract void keypress(int code, char ch);
 	/** Handle general state */
 	public abstract void processKeyState(boolean[] held);
 	public abstract void processMouseState(int x, int y);
 	/** Core two methods */
 	public abstract void update();
 	public abstract void render();
 	
 	static class MyCanvas extends Canvas {
 		public Image src;
 		MyCanvas(Image _src) {
 			src=_src;
 		}
		public void paint(java.awt.Graphics g) {
 			g.drawImage(src,0,0,this);
 		}
 	}
 	
 	static class Keyboard implements KeyListener {
 		public boolean[] keyheld;
 		public LinkedList<Keypress> keypresses;
 		private static class Keypress {
 			public final int code; 
 			public final char ch;
 			public Keypress(int _code, char _ch) {code=_code; ch=_ch;}
 		}
 		public Keyboard() {
 			keyheld=new boolean[KeyEvent.KEY_LAST];
 			keypresses=new LinkedList<Keypress>();
 		}
         public synchronized void keyPressed(KeyEvent e) {
        		keyheld[e.getKeyCode()]=true;
        		keypresses.addLast(new Keypress(e.getKeyCode(),e.getKeyChar()));
         }
         public synchronized void keyReleased(KeyEvent e) {
        		keyheld[e.getKeyCode()]=false;
         }
         public synchronized void keyTyped(KeyEvent e) { }
 	}
 	
 	/** Small class which encapsulates the mouse state. Use for synchronisation */
 	static class Mouse implements MouseListener {
 		public LinkedList<Click> clicks;
 		private static class Click { 
 			public final int x, y, b; 
 			public Click(int _x,int _y,int _b){x=_x;y=_y;b=_b;} 
 		}
 		public Mouse() { clicks=new LinkedList<Click>(); }
 		public synchronized void mouseEntered(MouseEvent e) { }
 		public synchronized void mouseExited(MouseEvent e)  { }
 		public synchronized void mouseReleased(MouseEvent e){ }
         public synchronized void mouseClicked(MouseEvent e) { }
         public synchronized void mousePressed(MouseEvent e) 	{ 
         	clicks.addLast(new Click(e.getX(),e.getY(),e.getButton()));
         }
 	}
 }
 
