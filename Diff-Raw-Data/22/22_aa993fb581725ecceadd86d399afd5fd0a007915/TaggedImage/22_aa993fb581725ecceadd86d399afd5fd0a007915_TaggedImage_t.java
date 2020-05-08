 package hci.viewer;
 
 import hci.polygon.TaggedPolygon;
 import hci.polygon.PolygonManager;
 import hci.util.Point;
 
 import java.awt.*;
 import java.io.*;
 
 import javax.imageio.*;
 
 public class TaggedImage {
 	
 	private static final int POINT_SIZE = 10;
 	
 	private Image originalImage, scaledImage;
 	private PolygonManager polman;
 	
 	public TaggedImage(int w, int h, String file, PolygonManager polman) throws FileNotFoundException, IOException
 	{
 		originalImage = ImageIO.read(new File(file));
 		
 		setImageSize(w,h);
 		
 		// load the polgyons by image hash
 		polman.loadPolygons(originalImage.hashCode());
 		
 		this.polman = polman;
 	}
 	
 	public void setImageSize(int w, int h)
 	{
 		// scale image
 		if (originalImage.getWidth(null) > w || originalImage.getHeight(null) > h) {
 			int newWidth = originalImage.getWidth(null) > w ? w : (originalImage.getWidth(null) * w)/originalImage.getHeight(null);
 			int newHeight = originalImage.getHeight(null) > h ? h : (originalImage.getHeight(null) * h)/originalImage.getWidth(null);
 			scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
 		}
 		else
 		{
 			scaledImage = originalImage;
 		}
 		
 	}
 	
 	public void draw(Graphics g)
 	{
 		
 		// draw the image
 		g.drawImage(scaledImage, 0, 0, null);
 		
 		// draw the polygons
 		g.setColor(Color.GREEN);
 		
 		TaggedPolygon[] polygons = polman.getPolygons();
 		
 		for (int i = 0; i < polygons.length; i++)
 		{
 			drawPolygon(g,polygons[i]);
 		}
 		
 	}
 	
 	private void drawPolygon(Graphics g, TaggedPolygon polygon)
 	{
 		
 		if (polygon.isComplete() && !polygon.isEditing())
 		{
 			if (polygon.isHighlighted())
 			{
 				Graphics2D g2d = (Graphics2D)g;
 				Composite c = g2d.getComposite();
 				int type = AlphaComposite.SRC_OVER;
 				g2d.setComposite(AlphaComposite.getInstance(type, 0.4f));
 				g.fillPolygon(polygon);
 
 				g2d.setComposite(c);
 								
 				// Need to get the name of the polygon being moused over
 				
 				String polygonLabel = "";
 				
 				try{
 				polygonLabel = polman.getHighlighted().getNameOfPolygon();
 				}
 				catch (NullPointerException e){
 				
 					polygonLabel = "Please Tag Me";
 					
 				}
 				c = g2d.getComposite();
 				g2d.setComposite(AlphaComposite.getInstance(type, 1f));
 				
 				g.setColor(Color.white);
 				Rectangle boundingbox = polygon.getBounds();
 				int middleX = ((boundingbox.x + (boundingbox.width + boundingbox.x)) / 2);
 				int middleY = (boundingbox.y + (boundingbox.y + boundingbox.height)) / 2;
 				
 				// Multiply offset as a scaling factor to make the box and string label match
 			try {
 				g.fillRect(middleX - polygonLabel.length() * 4, middleY - 15, polygonLabel.length() * 10, 20);
 			}
 			catch (NullPointerException e){
 				
				g.fillRect(middleX - "Please Tag Me".length() * 4, middleY - 15, "Please Tag Me".length() * 10, 20);
 				
 			}
 				g2d.setComposite(c);
 				g.setColor(Color.BLACK);
 				Font currentFont = g.getFont();
 				Font labelFont = new Font("newFont", Font.BOLD, 15);
 				g.setFont(labelFont);
 				try{
 				g.drawString(polygonLabel, middleX - polygonLabel.length() * 3, middleY);
 				}
 				catch(NullPointerException e){
 					g.drawString("Please Tag Me", middleX - "Please Tag Me".length() * 3, middleY);
 				}
 				g.setColor(Color.GREEN);
 				g.setFont(currentFont);
 				//
 			}
 			else
 			{
 				g.drawPolygon(polygon);
 			}
 			return;
 		}
 		
 		Point[] points = polygon.getPoints();
 		
 		if (points.length == 0)
 			return;
 		
 		Point lastPoint = points[0];
 		
 		g.fillOval(points[0].getX()-(int)(POINT_SIZE / 2), points[0].getY()-(int)(POINT_SIZE / 2), POINT_SIZE, POINT_SIZE);
 		
 		for (int i = 1; i < points.length; i++)
 		{
 			g.drawLine(lastPoint.getX(), lastPoint.getY(), points[i].getX(), points[i].getY());
 			
 			g.fillOval(points[i].getX()-(int)(POINT_SIZE / 2), points[i].getY()-(int)(POINT_SIZE / 2), POINT_SIZE, POINT_SIZE);
 
 			lastPoint = points[i];
 			System.out.println("Drawing: " + points[i].toString());
 		}
 		
 		if (polygon.isEditing() && polygon.isComplete())
 		{
 			g.drawLine(lastPoint.getX(), lastPoint.getY(), points[0].getX(), points[0].getY());
 		}
 		
 		if (polygon.getSelectedPointIdx() != -1)
 		{
 			int i = polygon.getSelectedPointIdx();
 			g.setColor(Color.RED);
 			g.fillOval(points[i].getX()-(int)(POINT_SIZE / 2), points[i].getY()-(int)(POINT_SIZE / 2), POINT_SIZE, POINT_SIZE);
 			g.setColor(Color.GREEN);
 		}
 		
 	}
 	
 }
