 package main;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
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
 	ShapeData shapeData = new ShapeData();
 
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
 		mainPanel.setBackground(Color.yellow);
 		this.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
 		this.setContentPane(mainPanel);
 
 		try 
 		{
 			imagePanel = new ImagePanel(imageName, shapeData);
 		} 
 		catch (IOException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		imagePanel.setOpaque(true); //content panes must be opaque
 
 		toolbox = new Toolbox(shapeData, new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				shapeData.setColor(ColorEnum.getColor(arg0.getActionCommand()));
				imagePanel.repaint();
 			}
 			
 		});
 		
 		try
 		{
 			imagePanel.setImage(imageName);
 		} 
 		catch (IOException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		mainPanel.add(imagePanel);
 		mainPanel.add(toolbox);
 
 		this.pack();
 		this.setVisible(true);
 
 	}
 
 	@Override
 	public void paint(Graphics g) 
 	{
 		super.paint(g);
 		imagePanel.paint(g); //update image panel
 	}
 
 	/**
 	 * Runs the program
 	 * @param argv path to an image
 	 */
 	public static void main(String argv[]) {
 		try 
 		{
 			//creates a window and display the image
 			MainWindow window = new MainWindow();
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
 				if (e.getKeyChar() == KeyEvent.VK_ENTER) 
 				{
 					Shape lastShape = shapeData.endShape(shapeData.getIndex());
 					
 					if (lastShape != null) {
 						imagePanel.drawLine(lastShape.get(lastShape.size() - 2), lastShape.get(0), lastShape.getColor());
 						shapeData.addShape(new Shape());	
 					}
 				}
 			}
 			return false;
 		}
 	}
 
 }
