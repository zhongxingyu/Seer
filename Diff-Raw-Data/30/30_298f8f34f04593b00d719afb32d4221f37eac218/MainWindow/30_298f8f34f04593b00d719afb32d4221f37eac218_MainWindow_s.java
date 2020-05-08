 package main;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import data.Shape;
 import data.ShapeData;
 
 
 public class MainWindow extends JFrame
 {
 
 	private static final long serialVersionUID = 1L;
 	JPanel mainPanel;
 	ImagePanel imagePanel;
 	Toolbox toolbox;
 	Dimension minimumSize = new Dimension(1000,600);
 	String imageName = "res/kirby.jpg";
 	JFileChooser fc;
 
 	/**
 	 * Create the panel.
 	 */
 	public MainWindow() 
 	{
 
 		this.addWindowListener(new WindowAdapter() 
 		{
 			public void windowClosing(WindowEvent event) 
 			{
 				System.exit(0);
 			}
 		});
 
 		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		manager.addKeyEventDispatcher(new MyDispatcher());
 
 		this.setMinimumSize(minimumSize);
 
 
 
 		mainPanel = new JPanel();
 
 		this.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
 		this.setContentPane(mainPanel);
 
 		God.mainWindow = this;
 		God.shapeData = new ShapeData();
 
 		try 
 		{
 			imagePanel = new ImagePanel(imageName);
 		} 
 		catch (IOException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		imagePanel.setOpaque(true); //content panes must be opaque
 
 		toolbox = new Toolbox( 
 				// changeColor
 				new ActionListener(){
 					@Override
 					public void actionPerformed(ActionEvent arg0) {
 						God.shapeData.setColor(ColorEnum.getColor(arg0.getActionCommand()));
 						repaint();
 					}});
 
 		try
 		{
 			imagePanel.setImage(imageName);
 		} 
 		catch (IOException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		God.imagePanel = imagePanel;
 		God.toolBox = toolbox;
 
 		mainPanel.setLayout(new BorderLayout());
 		mainPanel.add(toolbox,BorderLayout.EAST);
 		VertexPanel vPanel = new VertexPanel();
 		God.vertexPanel = vPanel;
 		MyLayeredPane layeredPanel = new MyLayeredPane(new BorderLayout());
 		layeredPanel.imagePanel = imagePanel;
 		layeredPanel.vertexPanel = vPanel;
 		layeredPanel.add(imagePanel,BorderLayout.CENTER);
 		layeredPanel.add(vPanel,BorderLayout.CENTER);
 		God.layeredPanel = layeredPanel;
 		mainPanel.add(layeredPanel,BorderLayout.CENTER);
 		layeredPanel.addMouseListener(new MouseListener(){
 
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				God.vertexPanel.mouseClicked(arg0);		
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				God.vertexPanel.mouseEntered(arg0);		
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				God.vertexPanel.mouseExited(arg0);		
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				God.vertexPanel.mousePressed(arg0);		
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				God.vertexPanel.mouseReleased(arg0);		
 			}});
 		
 		layeredPanel.addMouseMotionListener(new MouseMotionListener(){
 
 			@Override
 			public void mouseDragged(MouseEvent arg0) {
 				God.vertexPanel.mouseDragged(arg0);
 			}
 
 			@Override
 			public void mouseMoved(MouseEvent arg0) {
 				God.vertexPanel.mouseMoved(arg0);		
 			}
 		});
 		this.pack();
 		this.setVisible(true);
 	}
 
 	@Override
 	public void paint(Graphics g) 
 	{
 		super.paint(g);
 		if (imagePanel == null) {
 			System.out.println("SOIDJSIODJ");
 		}
 //				God.imagePanel.paintComponent(g); //update image panel
 //				God.vertexPanel.paintComponent(g);
 //		God.layeredPanel.paint(God.layeredPanel.getGraphics());
 	}
 
 	/**
 	 * Runs the program
 	 * @param argv path to an image
 	 */
 	public static void main(String argv[]) {
 		try 
 		{
 			//creates a window and display the image
 			new MainWindow();
 		} 
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private class MyDispatcher implements KeyEventDispatcher 
 	{
 		@Override
 		public boolean dispatchKeyEvent(KeyEvent e) 
 		{
 			if (e.getID() == KeyEvent.KEY_TYPED) 
 			{
 				if (e.getKeyChar() == KeyEvent.VK_ENTER ) 
 				{
 					Shape lastShape = God.shapeData.endShape(God.shapeData.getIndex());
 
 					if (lastShape != null) {
 						// screenshot
 						BufferedImage screenshot = imagePanel.getScreenshot();
 
 						Rectangle r = lastShape.getBoundingBox();
 						lastShape.setThumbnail(screenshot.getSubimage(r.x,r.y,r.width,r.height));
 						God.vertexPanel.drawLine(lastShape.get(lastShape.size() - 2), lastShape.get(0), lastShape.getColor());
 						God.shapeData.addShape(new Shape());	
 					}
 				}
 			}
 			return false;
 		}
 	}

 	public void requestLabel() {
 		String s = (String) JOptionPane.showInputDialog("Please enter label");
 		ArrayList<Shape> shapes = God.shapeData.getShapes();
 		if (s != null) {
 			shapes.get(shapes.size()-1).setLabel(s);
 		}
 	}
 }
