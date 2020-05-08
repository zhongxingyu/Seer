 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 
 
 public class SceneView extends JDialog
 {
 	public SceneView()
 	{
 		this.addMouseListener(new MouseAdapter()
 		{
 			public void mousePressed(MouseEvent e)
 			{	
 				if(e.getButton() == MouseEvent.BUTTON1)
 				{
 					mouse1Pressed = true;
 										
 					mx2 = mx1 = e.getX();
 					my2 = my1 = e.getY();
 					
 					
 					Data.Box b = Data.getBoxAtPixel(mx1,my1);
 					if(b!=null)
 					{
 						selectedBox = b;
 						ScreenLayoutTool.instance.enableText(selectedBox.name);
 					}					
 					else
 					{
 						Data.Box box = new Data.Box();
 						selectedBox = box;
 						ScreenLayoutTool.instance.disableText();
 						
 						boxAdded = false;
 						createBoxMode = true;
 						
 						box.setFirstPixelPoint(mx1, my1);
 					}
 					
 																		
 					repaint();				
 				}
 				else if(e.getButton() == MouseEvent.BUTTON3)
 				{
 					//check if clicked in a box, if so delete it
 					Data.Box b = Data.getBoxAtPixel(e.getX(), e.getY());
 					if(b!=null)
 					{
 						if(selectedBox == b)
 						{
 							selectedBox = null;
 							ScreenLayoutTool.instance.disableText();
 						}
 						Data.removeBox(b);
 					}
 					
 					repaint();
 				}
 			}
 			
 			public void mouseReleased(MouseEvent e)
 			{
 				if(e.getButton() == MouseEvent.BUTTON1)
 				{
 					mouse1Pressed = false;
 					createBoxMode = false;
 					
 					repaint();
 				}
 			}
 		});
 		
 		this.addMouseMotionListener(new MouseMotionListener()
 		{
 			public void mouseMoved(MouseEvent e)
             {
             }
 			
 			public void mouseDragged(MouseEvent e)
 			{				
 				if(mouse1Pressed)
 				{									
 					mx1 = mx2;
 					my1 = my2;
 					mx2 = e.getX();
 					my2 = e.getY();				
 					
 
 					if(createBoxMode)
 					{
 						selectedBox.setSecondPixelPoint(mx2, my2);
 						
 						if(!boxAdded && selectedBox.isValid())
 						{
 							boxAdded = true;
 							Data.addBox(selectedBox);
 							ScreenLayoutTool.instance.enableText(selectedBox.name);
 						}
 					}
 					
 					repaint();
 				}
 			}
 		});		
 		
 		this.addWindowListener(new WindowAdapter()
 		{
 			public void windowClosing(WindowEvent e)
 			{
 				System.exit(0);
 			}
 		});
 		
 		backBuffer = new BufferedImage(400,300,1);
 		backBufferGraphics = backBuffer.createGraphics();
 		setLocation(new Point(200,0));
 		setSize(400,300);
		setUndecorated(true);
 		setVisible(true);
 		setResizable(false);
 		
 	}
 	
 	public void loadImage(String path)
 	{
 		img = FileIO.readImage(path);
 		
 		if(img!=null)
 		{
 			backBuffer = new BufferedImage(img.getWidth(),img.getHeight(),1);
 			backBufferGraphics = backBuffer.createGraphics();
 			
 			setSize(img.getWidth(),img.getHeight());
 			setVisible(true);
 		}		
 		
 		repaint();
 	}
 	
 	public void draw(Graphics g)
 	{
 		if(img!=null)
 			g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), this);
 		else
 		{			
 			g.setColor(new Color(0,0,0));
 			g.fillRect(0,0,getWidth(),getHeight());
 		}
 		Data.draw(g);
 	}
 	
 	public void paint(Graphics g)
 	{
 		draw(backBufferGraphics);
 		g.drawImage(backBuffer,0,0,getWidth(),getHeight(),this);		
 	}
 	
 	//these are for selecting and dragging the boxes:
 	private int mx1 = 0;
 	private int my1 = 0;
 	private int mx2 = 0;
 	private int my2 = 0;		
 	private boolean mouse1Pressed = false;	
 	
 	public BufferedImage img;
 	
 	BufferedImage backBuffer;
 	Graphics backBufferGraphics;
 	
 	public Data.Box selectedBox = null;
 	public boolean boxAdded = false;
 	public boolean createBoxMode = false;
 }
