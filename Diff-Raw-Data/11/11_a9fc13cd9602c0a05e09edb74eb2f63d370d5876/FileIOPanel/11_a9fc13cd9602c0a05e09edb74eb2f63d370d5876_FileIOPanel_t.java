 package fileio;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import main.God;
 import main.ShapeList;
 import data.Shape;
 import data.ShapeData;
 import data.Vertex;
 
 public class FileIOPanel extends JPanel
 {
 
 	private static final long serialVersionUID = 1L;
 	JButton newButton, saveButton, loadButton, lolPaul;
 	JTextArea log;
 	JFileChooser fc;
 
 	public FileIOPanel()
 	{
 		this.setPreferredSize(new Dimension(200,200));
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
 		newButton = new JButton("New Image");
 		newButton.addActionListener(newImage());
 		add(newButton);
 
 		saveButton = new JButton("Save Annotations");	
 		saveButton.addActionListener(saveSession());
 		add(saveButton);
 
 		loadButton = new JButton("Load Annotations");
 		loadButton.addActionListener(loadSession());
 		add(loadButton);
 		
 
 		lolPaul = new JButton("lolPaul lolPaul");
 		lolPaul.addActionListener(new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 				
 			}
 			
 		});
 		add(lolPaul);
 	}
 
 	/***
 	 * ACTIONLISTENER DEFINITIONS FOR BUTTONS
 	 */
 
 	/***
 	 * Method definition for loading a new image into the system.
 	 * @return ActionListener for newButton button
 	 */
 	ActionListener newImage()
 	{
 		return new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0) 
 			{		
 				if(God.dirtyFlag)
 				{
 					JOptionPane.showMessageDialog(null, "You have not commited your current modifications! TODO");
 					return;
 				}
 				if (fc == null) 
 				{
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
 
 				// Show window dialog
 				int returnVal = fc.showDialog(FileIOPanel.this, "Opening New Image");
 
 				// Process the results
 				if (returnVal == JFileChooser.APPROVE_OPTION) 
 				{
 					String file = fc.getSelectedFile().getPath();
 
 					try 
 					{
 						// TODO SAVE BEFORE NEW PROJECT
 
 						/*** Reset properties for new image ***/
 						ShapeData shapeData = new ShapeData();
 						God.shapeData = shapeData;
 						God.imagePanel.newImage(file);
 						God.moveMode = 0;
 						// Empty thumbnail list
 						God.shapeList.emptyList();
 						God.dirtyFlag = false;
 						God.vertexPanel.repaint();
 					} 
 					catch (IOException e) 
 					{
 						e.printStackTrace();
 					}
 				}
 				//Reset the file chooser for the next time it's shown.
 				fc.setSelectedFile(null);
 			}};
 	}
 
 	/***
 	 *  Method definition for saving an image and polygon labels.
 	 * @return ActionListener for saveButton button
 	 */
 	ActionListener saveSession()
 	{
 		return new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				if (fc == null) 
 				{
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
 
 				int returnVal = fc.showDialog(FileIOPanel.this, "Save Annotations");
 
 				if (returnVal == JFileChooser.APPROVE_OPTION) 
 				{
 					FileWriter fw = null;
 					BufferedWriter bw = null;
 					try {
 						String filePath = fc.getSelectedFile().getPath();
 						System.out.println(filePath);
 						String[] fileArray = filePath.split("\\" + File.separator); // want to make sure we only take name, not extension
 						//						if (fileArray.length > 0) {
 						//							String[] fileExtension = fileArray[1].split(".");
 						//							if (fileExtension.length > 0) {
 						//								filePath
 						//							}
 						//						}
 						int pathLength = filePath.length() - 1;
 						for (int i = pathLength; i >= 0; i--) {
 							if (filePath.charAt(i) == File.separatorChar) {
 								break;
 							} 
 							if (filePath.charAt(i) == '.') {
 								filePath = filePath.substring(0, i);
 							}
 
 						}
 						File file = new File(filePath + ".csv");
 						if (!file.exists()) {
 							file.createNewFile();
 						}
 						fw = new FileWriter(file.getAbsoluteFile());
 						bw = new BufferedWriter(fw);
 						// Write the image dimension this label is for 
 						// Avoids drawing vertices in non image space (illegal!)
 						bw.write(String.valueOf(God.imageDimension[0]) + ',' + String.valueOf(God.imageDimension[1]));
 						bw.newLine();
 
 						// Write shapes and labels to file
 						for (Shape shape : God.shapeData.getShapes()) {
 							// Only write shapes that are complete (head == tail and at least 3 vertices)!
 							if(shape.size() > 2)
 								if(shape.getHead().equals(shape.getTail()))
 								{
 									{
 										bw.write(shape.getLabel());
 										bw.write(',' + String.valueOf(shape.getColor().getRed()));
 										bw.write(',' + String.valueOf(shape.getColor().getGreen()));
 										bw.write(',' + String.valueOf(shape.getColor().getBlue()));
 										for (Vertex v : shape.getVertices()) {
 											bw.write(',' + String.valueOf(v.getX()));
 											bw.write(',' + String.valueOf(v.getY()));
 										}
 										bw.newLine();
 									}
 								}
 						}
 						bw.close();
 						God.dirtyFlag = false;
 						System.out.println("Done");
 
 					} catch (IOException e) {
 						System.out.println("Error when writing to file");
 						e.printStackTrace();
 						System.out.println(e.getMessage());
 					} 
 				}
 				//Reset the file chooser for the next time it's shown.
 				fc.setSelectedFile(null);
 			}};
 	}
 
 	/***
 	 * Method definition for loading an image and polygon labels.
 	 * @return ActionListener for loadButton button
 	 */
 	ActionListener loadSession()
 	{
 		// LOAD SESSION
 		return new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0) 
 			{
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
 				int returnVal = fc.showDialog(FileIOPanel.this,
 						"Loading Annotations");
 
 				//Process the results.
 				if (returnVal == JFileChooser.APPROVE_OPTION) 
 				{
 					try {
 
 						// TODO FUNCTIONALITY
 						File file = fc.getSelectedFile();
 						FileInputStream fis = new FileInputStream(file);
 						DataInputStream in = new DataInputStream(fis);
 						BufferedReader br = new BufferedReader(new InputStreamReader(in));
 						ArrayList<String> shapeInfo = new ArrayList<String>();
 						String strLine;
 
 						/***
 						 * Checks if the annotation file was for an larger image than the current one on display
 						 * (draws vertices out of bounds of the current image)
 						 * If so, show error message to user
 						 */
 						if((strLine = br.readLine()) != null)
 						{
 							String[] infoArray = strLine.split(",");
 							int[] fileDimension = new int[2];
 							fileDimension[0] = Integer.parseInt(infoArray[0]);
 							fileDimension[1] = Integer.parseInt(infoArray[1]);
 							if (fileDimension[0] > God.imageDimension[0] || fileDimension[1] > God.imageDimension[1])
 							{
 								JOptionPane.showMessageDialog(null, "Annotation dimensions are too large for the current image!");		
 								return;
 							}
 						}
 
 						while ((strLine = br.readLine()) != null) {
 							shapeInfo.add(strLine);
 						}
 						//remove old shape data ????
 						for (Shape s : God.shapeData.getShapes()) {
 							ShapeList.removeShape(0);
 						}
 						ShapeData shapeData = new ShapeData();
 
 						// Loading shape function
 						for (String info : shapeInfo) {
 							if (!info.contains(",")) {
 								//PROBLEM, continue for now
 								continue;
 							}
 							String[] infoArray = info.split(",");
 							//create shape object from data
 							Shape shape = new Shape();
 							shape.setLabel(infoArray[0]);
 							Color color = new Color(Integer.parseInt(infoArray[1]),
 									Integer.parseInt(infoArray[2]),
 									Integer.parseInt(infoArray[3])
 									);
 							shape.setColor(color);
 							for (int i = 4; i < infoArray.length; i+=2) {
 								Vertex v = new Vertex(	Integer.parseInt(infoArray[i]),
 										Integer.parseInt(infoArray[i+1])
 										);
 								shape.add(v);
 							}
 							BufferedImage screenshot = God.imagePanel.getScreenshot();
 							Rectangle r = shape.getBoundingBox();
 							shape.setThumbnail(screenshot.getSubimage(r.x,r.y,r.width,r.height));
 							shapeData.addShape(shape);
							
 						}
						shapeData.addShape(new Shape());
 						God.shapeData = shapeData;
 						God.moveMode = 0;
 						God.dirtyFlag = false;
 						God.layeredPanel.paint(God.layeredPanel.getGraphics());
 					} 
 					catch(FileNotFoundException e) 
 					{
 						e.printStackTrace();
 					} 
 					catch(IOException e) 
 					{	
 						e.printStackTrace();
 					} 
 					catch(Exception e)
 					{
 						JOptionPane.showMessageDialog(null, "This file is not a valid annotation file!\n Please try another file.");
 					}
 				} 
 				else 
 				{
 				}
 				//Reset the file chooser for the next time it's shown.
 				fc.setSelectedFile(null);
 			}};
 	}
 }
