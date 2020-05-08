 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.awt.geom.*;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 
 @SuppressWarnings("serial")
 public class Gfx extends JFrame{
 	Graphics2D g2;
 	BufferedImage backgroundImage;
 	//public List<TerritoryGraphics> territoryGraphicsList;
 	public Gfx()
 	{
 		setSize(1080,700);
 		backgroundImage=getBackgroundImage();
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setTitle("Risk!");
 		setVisible(true);
 		
 		addMouseListener(RiskAI.clickHandler);
 	}
 
 	/**
 	 * @param args
 	 */
 	
 	private BufferedImage getBackgroundImage()
 	{
 		BufferedImage img = null;
 		try{
 			img = ImageIO.read(new File("BlankRiskMap.jpg"));
 		}catch (IOException e){}
 		return img;
 	}
 
 	public void paint(Graphics g)
 	{
 		g2 = (Graphics2D) g;
 		g2.drawImage(backgroundImage, 10, 40, this);
 		for(Territory i : RiskAI.territoryData)
 		{
 			drawTerritoryGraphics(i.getTerritoryGraphic());
 		}
 		
 	}
 	/**
 	 * Draws a TerritoryGraphics.
 	 * More precisely, draws an oval in the territory's owner's color, then puts
 	 * the number of armies in text on top of it.
 	 */
 	public void drawTerritoryGraphics(TerritoryGraphics icon)
 	{
 		g2.setColor(icon.getColor());
 		//drawCircle(CenterXCoordinate,CenterYCoordinate, side length, side length
 		//g2.fill(new Ellipse2D.Double(icon.xCoord-icon.SIDE_LENGTH/2, icon.yCoord-icon.SIDE_LENGTH/2, icon.SIDE_LENGTH, icon.SIDE_LENGTH));
 		g2.fill(icon.icon);
 		g2.setColor(Color.BLACK);
 		String str = String.valueOf(icon.parent.getUnitCount());
 		FontMetrics metrics = g2.getFontMetrics(new Font("Arial", Font.PLAIN, 10));
 		int stringWidth = metrics.stringWidth(str);
 		int stringHeight = metrics.getHeight();
		g2.drawString(str, icon.xCoord - stringWidth/2, icon.yCoord);
 	}
 }
