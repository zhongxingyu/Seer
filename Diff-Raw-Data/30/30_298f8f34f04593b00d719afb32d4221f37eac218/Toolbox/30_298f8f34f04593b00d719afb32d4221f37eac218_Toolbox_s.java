 package main;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.EmptyBorder;
 
 import data.Shape;
 import fileio.FileIOPanel;
 
 public class Toolbox extends JPanel 
 {
 
 	private static final long serialVersionUID = 1L;
 	JButton btnNewObject = new JButton("New Object");
 	JButton btnSelectObject = new JButton("Move Object");
 	ShapeList shapeList = null;
 	/**
 	 * Create the panel.
 	 */
 	public Toolbox(ActionListener changeColor) 
 	{		
 		this.setBorder(new EmptyBorder(5,5,5,5));
 		this.setPreferredSize(new Dimension(200,600));
 
 		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 
 		shapeList = new ShapeList(God.shapeData);
 		JScrollPane scrollPane = new JScrollPane(shapeList);
 
 		scrollPane.setPreferredSize(new Dimension(200, 300));
 
 		btnNewObject = new JButton("New Object");
 		btnNewObject.addActionListener(new ActionListener() 
 		{
 			@Override
 			public void actionPerformed(ActionEvent e) 
 			{
 				God.moveMode = false;
 				if (God.shapeData.getShapes().size() != 0) {
					God.shapeData.endShape(God.shapeData.getIndex());
 					Shape lastShape = God.shapeData.endShape(God.shapeData.getIndex());
 
 					if (lastShape != null) {
 						// screenshot
 						BufferedImage screenshot = God.imagePanel.getScreenshot();
 
 						Rectangle r = lastShape.getBoundingBox();
 						lastShape.setThumbnail(screenshot.getSubimage(r.x,r.y,r.width,r.height));
 						God.vertexPanel.drawLine(lastShape.get(lastShape.size() - 2), lastShape.get(0), lastShape.getColor());
 						God.mainWindow.repaint();
 					}
 				}
 				God.shapeData.addShape(new Shape());
 			}
 		});
 		add(btnNewObject);
 
 		btnSelectObject = new JButton("Move Object");		
 		btnSelectObject.addActionListener(new ActionListener() 
 		{
 			@Override
 			public void actionPerformed(ActionEvent e) 
 			{
 				God.moveMode = true;
 			}
 		});
 		add(btnSelectObject);
 
 		ColorPalette colourPalette = new ColorPalette(changeColor);
 		add(colourPalette);
 		add(scrollPane);
 
 		FileIOPanel fileIO = new FileIOPanel();
 		add(fileIO);
 		God.fileIOPanel = fileIO;
 	}
 
 }
