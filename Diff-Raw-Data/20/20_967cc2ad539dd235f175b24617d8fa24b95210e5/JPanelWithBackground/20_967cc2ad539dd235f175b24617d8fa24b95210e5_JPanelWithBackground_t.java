 package MusicCrawler;
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.geom.Area;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 
class JPanelWithBackground extends JPanel
 { 
 
 	private static final long serialVersionUID = 1L;	
 	private BufferedImage image;
 	int IMG_WIDTH,IMG_HEIGHT,Type;
 
     JPanelWithBackground(String path,int width,int height) 
     {
        try 
        {                
           image = ImageIO.read(getClass().getClassLoader().getResource(path));
           Type=image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
           IMG_WIDTH=width;IMG_HEIGHT=height;
           image=resizeImage(image,Type);
        } catch (IOException ex) 
        {
             // handle exception...
        }
     }
     JPanelWithBackground() 
     {
        try 
        {                
           image = ImageIO.read(getClass().getClassLoader().getResource("res/background.jpg"));
           IMG_WIDTH=900;IMG_HEIGHT=700;
        } catch (IOException ex) 
        {
             // handle exception...
        }
     }
 
     @Override
     protected void paintComponent(Graphics g) 
     {
         super.paintComponent(g);
         g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters            
     }
     
     private BufferedImage resizeImage(BufferedImage originalImage, int type)
     {
     	BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
     	Graphics2D g = resizedImage.createGraphics();
     	g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
     	g.dispose();
     	return resizedImage;
     }
 }
 
 class TransparentButton extends JButton 
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public TransparentButton(String text) 
 	{ 
 	    super(text);
 	    setOpaque(false); 
 	} 
 	    
 	@Override
 	public void paint(Graphics g)
 	{ 
 	    Graphics2D g2 = (Graphics2D) g.create(); 
 	    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); 
 	    super.paint(g2); 
 	    g2.dispose(); 
 	} 
 }
 
 class ShapeButton extends JButton 
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	ShapeButton(int shape,String label)
 	{
 		super(label);
 		Dimension size = getPreferredSize();
 		size.width = size.height = Math.max(size.width, size.height);
 		setPreferredSize(size);
 		setContentAreaFilled(false);
 	}
 	ShapeButton(String label)
 	{
 		super(label);
 		Dimension size = getPreferredSize();
 		size.width = size.height = Math.max(size.width, size.height);
 		setPreferredSize(size);
 		setContentAreaFilled(false);	
 	}
 	public static Shape makeStarDesign(int arms, Point center, double r_out, double r_in) {
 	     double angle = Math.PI / arms;
 	     GeneralPath path = new GeneralPath();
 	     for (int i = 0; i < 2 * arms; i++) {
 	          double r = (i & 1) == 0 ? r_out : r_in;
 	          Point2D.Double p = new Point2D.Double(center.x + Math.cos(i * angle) * r, center.y + Math.sin(i * angle) * r);
 	          if (i == 0) path.moveTo(p.getX(), p.getY());
 	          else path.lineTo(p.getX(), p.getY());
 	     }
 	     path.closePath();
 	     return path;
 	}
 	@Override
 	protected void paintComponent(Graphics g) {
 	     if (getModel().isArmed()) {
 	          g.setColor(Color.lightGray);
 	     } else {
 	          g.setColor(getBackground());
 	     }
 	     Graphics2D graphics2d = (Graphics2D) g;
 	     graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 	     graphics2d.fill(makeStarDesign(5, new Point(50,50), 50, 30));
 	     super.paintComponent(g);
 	}
 	@Override
 	protected void paintBorder(Graphics g) {
 	     g.setColor(getForeground());
 	     Graphics2D graphics2d = (Graphics2D) g;
 	     graphics2d.draw(makeStarDesign(5, new Point(50,50), 50, 30));
 	}
 	Shape shape;
 	@Override
 	public boolean contains(int x, int y) 
 	{
 	     if (shape == null || !shape.getBounds().equals(getBounds())) {
 	          shape = new Area(makeStarDesign(5, new Point(50,50), 50, 30));
 	     }
 	     return shape.contains(x, y);
 	}
    
 }
