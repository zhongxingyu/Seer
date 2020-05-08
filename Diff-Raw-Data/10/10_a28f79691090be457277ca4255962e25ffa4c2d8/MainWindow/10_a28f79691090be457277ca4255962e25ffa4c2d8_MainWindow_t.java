 package main;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import data.Shape;
 import data.ShapeData;
 import fileio.ImagePreview;
 import fileio.TypeFilter;
 import fileio.ViewFile;
 
 
 public class MainWindow extends JFrame
 {
 
 	private static final long serialVersionUID = 1L;
 	JPanel mainPanel;
 	ImagePanel imagePanel;
 	Toolbox toolbox;
 	Dimension minimumSize = new Dimension(1000,600);
 	String imageName = "res/kirby.jpg";
 	ShapeData shapeData = new ShapeData();
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
 
 		toolbox = new Toolbox(shapeData, 
 				// changeColor
 				new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				shapeData.setColor(ColorEnum.getColor(arg0.getActionCommand()));
 				repaint();
 			}},
 			
 			// NEW IMAGE
 			new ActionListener(){
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					if (fc == null) {
 						fc = new JFileChooser();
 
 						//Add a custom file filter
 						fc.addChoosableFileFilter(new TypeFilter(1));
 						fc.addChoosableFileFilter(new TypeFilter(0));
 						fc.setAcceptAllFileFilterUsed(true);
 
 						//Add custom icons for file types.
 						fc.setFileView(new ViewFile());
 
 						//Add the preview pane.
 						fc.setAccessory(new ImagePreview(fc));
 					}
 
 					//Show it.
 					int returnVal = fc.showDialog(MainWindow.this,
 							"Opening New Image");
 
 					//Process the results.
 					if (returnVal == JFileChooser.APPROVE_OPTION) {
 						String file = fc.getSelectedFile().getPath();
 						
 						try {
 							// TODO SAVE BEFORE NEW PROJECT
 							shapeData = new ShapeData();
 							imagePanel.newImage(file, shapeData);
 							toolbox.changeShapeData(shapeData);
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					//Reset the file chooser for the next time it's shown.
 					fc.setSelectedFile(null);
 				}},
 				
 				// SAVE SESSION
 				new ActionListener(){
 					@Override
 					public void actionPerformed(ActionEvent arg0) {
 						if (fc == null) {
 							fc = new JFileChooser();
 
 							//Add a custom file filter
 							fc.addChoosableFileFilter(new TypeFilter(1));
 							fc.addChoosableFileFilter(new TypeFilter(0));
 							fc.setAcceptAllFileFilterUsed(false);
 
 							//Add custom icons for file types.
 							fc.setFileView(new ViewFile());
 
 							//Add the preview pane.
 							fc.setAccessory(new ImagePreview(fc));
 						}
 
 						//Show it.
 						int returnVal = fc.showDialog(MainWindow.this,
 								"Saving Session");
 
 						//Process the results.
 						if (returnVal == JFileChooser.APPROVE_OPTION) {
 							// TODO FUNCTIONALITY
 							File file = fc.getSelectedFile();
 						} else {
 						}
 						//Reset the file chooser for the next time it's shown.
 						fc.setSelectedFile(null);
 					}},
 					
 					// LOAD SESSION
 					new ActionListener(){
 						@Override
 						public void actionPerformed(ActionEvent arg0) {
 							if (fc == null) {
 								fc = new JFileChooser();
 
 								//Add a custom file filter
 								fc.addChoosableFileFilter(new TypeFilter(1));
 								fc.addChoosableFileFilter(new TypeFilter(0));
 								fc.setAcceptAllFileFilterUsed(false);
 
 								//Add custom icons for file types.
 								fc.setFileView(new ViewFile());
 
 								//Add the preview pane.
 								fc.setAccessory(new ImagePreview(fc));
 							}
 
 							//Show it.
 							int returnVal = fc.showDialog(MainWindow.this,
 									"Loading Session");
 
 							//Process the results.
 							if (returnVal == JFileChooser.APPROVE_OPTION) {
 								// TODO FUNCTIONALITY
 								File file = fc.getSelectedFile();
 							} else {
 							}
 							//Reset the file chooser for the next time it's shown.
 							fc.setSelectedFile(null);
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
 						// screenshot
 						BufferedImage screenshot = imagePanel.getScreenshot();
 						
 						Rectangle r = lastShape.getBoundingBox();
 						lastShape.setThumbnail(screenshot.getSubimage(r.x,r.y,r.width,r.height));
 						imagePanel.drawLine(lastShape.get(lastShape.size() - 2), lastShape.get(0), lastShape.getColor());
 						shapeData.addShape(new Shape());	
 					}
 				}
 			}
 			return false;
 		}
 	}
 }
