 package main;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 import data.Shape;
 import data.ShapeData;
 import data.Vertex;
 
 public class ImagePanel extends JPanel implements MouseListener
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	/** The image to display in this panel */
 	BufferedImage image;
 	/** The maximum size of the panel */
 	Dimension size = new Dimension(800,600);
 
 	ShapeData shapeData;
 
 	public ImagePanel()
 	{
 		addMouseListener(this);
 		this.setVisible(true);
 		this.setPreferredSize(size);
 
 	}
 
 	public ImagePanel(String imageName, ShapeData shapeData) throws IOException
 	{
 		this();
 		setImage(imageName);
 		this.shapeData = shapeData;
 	}
 	
 	public void newImage(String imageName, ShapeData shapeData) throws IOException
 	{
 		setImage(imageName);
 		this.shapeData = shapeData;
 		this.repaint();
 	}
 	
 	@Override
 	public void paint(Graphics g) 
 	{
		super.paint(g);
 
 		displayImage(g, false);
 		ArrayList<Shape> shapes = shapeData.getShapes();
 		for (Shape shape : shapes) 
 		{ 
 			drawShape(g, shape);
 		}
 	}
 
 	/**
 	 * Display the loaded image
 	 */
 	private void displayImage(Graphics g, boolean screenshot)
 	{
 		if (!screenshot) {
 			g = this.getGraphics();
 		}
 
 		if (image != null) 
 		{
 			g.drawImage(
 					image, 0, 0, null);
 		}
 	}
 
 	/**
 	 * Attempts to load the image using size constraints. Will scale if 
 	 * image is too large.
 	 * @param path
 	 * @throws IOException
 	 */
 	public void setImage(String path) throws IOException
 	{
 
 		image = ImageIO.read(new File(path));
 		int width = (int) size.getWidth();
 		int height = (int) size.getHeight();
 		if (image.getWidth() > width || image.getHeight() > height) 
 		{
 			int newWidth = image.getWidth() > width ? 
 					width : 
 						(image.getWidth() * height)/image.getHeight();
 			int newHeight = image.getHeight() > height ? 
 					height : 
 						(image.getHeight() * width)/image.getWidth();
 
 			System.out.println("SCALING TO " + newWidth + "x" + newHeight );
 
 			Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
 			image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
 			image.getGraphics().drawImage(scaledImage, 0, 0, this);
 		}
 	}
 
 	/**
 	 * Draws all the vertices in the shape and draws lines between 
 	 * each adjacent vertex
 	 * @param shape
 	 */
 	private void drawShape(Graphics g, Shape shape) 
 	{
 		g.setColor(shape.getColor());
 		ArrayList<Vertex> vertices = shape.getVertices();
 		int size = vertices.size();
 
 		for (int i = 0; i < size; i++) 
 		{
 			Vertex current = vertices.get(i);
 			if (i != 0) 
 			{
 				Vertex previous = vertices.get(i - 1);
 				drawLine(current, previous, shape.getColor());
 			}
 			drawVertex(current, shape.getColor());
 		}
 	}
 
 	/**
 	 * Draws a line from the first vertex to the next
 	 * @param v1
 	 * @param v2
 	 */
 	public void drawLine(Vertex v1, Vertex v2, Color color) 
 	{
 
 		Graphics2D g = (Graphics2D) this.getGraphics();
 		g.setColor(color);
 		g.drawLine(v2.getX(), v2.getY(), v1.getX(), v1.getY());
 	}
 
 	/**
 	 * Draws a circle around the given vertex of the given color
 	 * @param v
 	 * @param color
 	 */
 	private void drawVertex(Vertex v, Color color) 
 	{
 		Graphics2D g = (Graphics2D) this.getGraphics();
 		g.setColor(color);
 		g.fillOval(v.getX() - v.getRadius(), v.getY() - v.getRadius(), v.getRadius()*2, v.getRadius()*2);
 	}
 
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) 
 	{
 		ArrayList<Shape> shapes = shapeData.getShapes();
 		Vertex vertex = new Vertex(arg0.getX(), arg0.getY());
 		if (shapes.size() == 0)
 		{
 			// TODO: Check necessary?
 		}
 		else if (arg0.getX() < image.getWidth() - vertex.getRadius()  && arg0.getY() < image.getWidth() - vertex.getRadius()) 
 		{
 			Shape lastShape = shapes.get(shapes.size() - 1);
 			drawVertex(vertex, lastShape.getColor());
 			if (lastShape.size() != 0) 
 			{
 				drawLine(vertex, lastShape.get(lastShape.size()-1), lastShape.getColor());
 			}
 			lastShape.add(vertex);
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) 
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) 
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) 
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) 
 	{
 		// TODO Auto-generated method stub
 	}
 
 	public BufferedImage getScreenshot() {
 		BufferedImage image = new BufferedImage(
 				this.getWidth(),
 				this.getHeight(),
 				BufferedImage.TYPE_INT_RGB
 				);
 		// call the Component's paint method, using
 		// the Graphics object of the image.
 		this.displayImage(image.getGraphics(), true);
 
 		return image;
 	}
 
 }
