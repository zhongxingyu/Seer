 package uk.ac.shef.tiledpres.gui;
 
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.SwingUtilities;
 
 import uk.ac.shef.tiledpres.Slide;
 import edu.umd.cs.piccolo.PNode;
 import edu.umd.cs.piccolox.PFrame;
 
 public class SlideGUI extends PFrame
 {
 	List<Slide> slides;
 
 	LayoutManager layout = new GridLayout();
 
 	double maxX = 0;
 
 	double maxY = 0;
 
 	Map<Rectangle2D, Integer> titleToBounds = new HashMap<Rectangle2D, Integer>();
 
 	int currSlide = -1;
 
 	public SlideGUI(List<Slide> slides)
 	{
 		this.slides = slides;
 
 		setFullScreenMode(true);
 		SwingUtilities.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				setVisible(true);
 			}
 		});
 
 		setupDisplay();
 		showAllSlides();
 
 		this.addComponentListener(new ComponentAdapter()
 		{
 			public void componentResized(ComponentEvent e)
 			{
 				showAllSlides();
 			}
 		});
 
 		SlideGUIClickListener clickListener = new SlideGUIClickListener(this);
 		this.getCanvas().addInputEventListener(clickListener);
 		this.getCanvas().addKeyListener(new KeyAdapter()
 		{
 			public void keyPressed(KeyEvent e)
 			{
 				if (e.getKeyCode() == KeyEvent.VK_SPACE)
 					showAllSlides();
 				else if (e.getKeyCode() == KeyEvent.VK_LEFT)
 					prev();
 				else if (e.getModifiers() == KeyEvent.ALT_MASK
 						&& e.getKeyCode() == KeyEvent.VK_ENTER)
 					fullscreen();
				else if (e.getModifiers() != KeyEvent.ALT_MASK)
 					next();
 			}
 		});
 	}
 
 	public void fullscreen()
 	{
 		this.setFullScreenMode(!this.isFullScreenMode());
 	}
 
 	public void next()
 	{
 		currSlide++;
 		currSlide %= slides.size();
 
 		for (Rectangle2D bounds : titleToBounds.keySet())
 			if (titleToBounds.get(bounds) == currSlide)
 				this.getCanvas().getCamera().animateViewToCenterBounds(bounds,
 						true, 2000);
 	}
 
 	public void prev()
 	{
 		currSlide--;
 		for (Rectangle2D bounds : titleToBounds.keySet())
 			if (titleToBounds.get(bounds) == currSlide)
 				this.getCanvas().getCamera().animateViewToCenterBounds(bounds,
 						true, 2000);
 	}
 
 	public void selectSlide(double x, double y)
 	{
 		for (Rectangle2D bounds : titleToBounds.keySet())
 			if (bounds.contains(x, y))
 			{
 				this.getCanvas().getCamera().animateViewToCenterBounds(bounds,
 						true, 2000);
 				currSlide = titleToBounds.get(bounds);
 			}
 	}
 
 	public void setupDisplay()
 	{
 		Map<String, Point2D.Double> locMap = layout.layoutSlides(slides);
 		maxX = 0;
 		maxY = 0;
 
 		for (int i = 0; i < slides.size(); i++)
 		{
 			PNode node = slides.get(i).getNode();
 
 			this.getCanvas().getLayer().addChild(node);
 			Point2D.Double location = locMap.get(slides.get(i).getTitle());
 			node.animateToPositionScaleRotation(location.getX(), location
 					.getY(), 1.0, 0.0, 2000);
 
 			titleToBounds.put(new Rectangle2D.Double(location.getX(), location
 					.getY(), node.getWidth(), node.getHeight()), i);
 
 			maxX = Math.max(location.getX() + node.getWidth(), maxX);
 			maxY = Math.max(maxY, location.getY() + node.getHeight());
 		}
 
 	}
 
 	public void showAllSlides()
 	{
 		this.getCanvas().getCamera().animateViewToCenterBounds(
 				new Rectangle2D.Double(0, 0, maxX, maxY), true, 2000);
 	}
 }
