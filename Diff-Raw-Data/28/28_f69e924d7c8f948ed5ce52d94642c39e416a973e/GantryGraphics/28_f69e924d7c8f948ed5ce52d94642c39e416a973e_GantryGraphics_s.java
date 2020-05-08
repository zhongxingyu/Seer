 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JPanel;
 
 /**
  * this class calls FactoryPainter to draw everything in the Kit Assembly
  * Manager
  */
 
 public class GantryGraphics extends JPanel {
 	public static final int UPDATE_RATE = 50;
 	
 	/** variable of FactoryPainter that draws graphic stuffs */
 	private FactoryPainter painter;
 	/**
 	 * @param args
 	 */
 	/** Initialization */
 	public GantryGraphics() {
 		//add( new JLabel("GRAPHICS"));
 		this.setPreferredSize(FactoryPainter.getAreaSize(FactoryPainter.FactoryArea.GANTRY_MANAGER));
		painter = new FactoryPainter( new FactoryStateMsg() );
 	}
 	
 	/** set the factory state to keep this client synchronized with the server */
 	public void setFactoryState(FactoryStateMsg factoryState)
 	{
 		painter.setFactoryState(factoryState);
 	}
 	
 	/** update with updateMsg */
 	public void update(FactoryUpdateMsg updateMsg)
 	{
 		painter.update(updateMsg);
 	}
 	/** call drawFactoryArea() in painter to draw */
 	public void paint(Graphics gfx)
 	{
 		Graphics2D g = (Graphics2D)gfx;
 		
 		BufferedImage factoryImg = painter.drawFactoryArea(FactoryPainter.FactoryArea.GANTRY_MANAGER);
 		g.drawImage(factoryImg, 0, 0, null);
 	}
 }
