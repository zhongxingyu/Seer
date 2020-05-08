 package spritemaker;
 
 import java.awt.Color;
 import java.awt.FileDialog;
 import javax.swing.*;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 import javax.imageio.ImageIO;
 
 class SpriteMakerWindow extends JFrame
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private class GenerateStringListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			stringOutput.setText(drawingPanel.generateHexString());
 		}
 	}
 	
 	
 	//menu bars
     //menu variables
     JMenuBar mainMenuBar;
     
     JMenu propertiesMenu;
     JMenuItem pixelMappingMenu;
     JMenuItem sizeMenu;
     JMenuItem outputFormatOptions;
 	JMenuItem importImage;
     
 	private SpriteDrawingPanel drawingPanel;
 	private JButton getStringButton;
 	private JTextArea stringOutput;
 	private SpriteDrawingPanelSelector drawOptions;
 	
 	private final int updateRate = 20;
 
 	public SpriteMakerWindow()
 	{
 		//initialize components
 		getStringButton = new JButton("make string");
 		stringOutput = new JTextArea(10, 10);
 		drawingPanel = new SpriteDrawingPanel(12);
 		this.drawOptions = new SpriteDrawingPanelSelector(this.drawingPanel);
 		
 		//set up menubar
         pixelMappingMenu = new JMenuItem("Pixel Mapping");
         pixelMappingMenu.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				OrientationDialog mappingSelection = new OrientationDialog(SpriteMakerWindow.this, drawingPanel.getByteOrientation(), drawingPanel.getMSBPosition());
 				OrientationDiagram od = mappingSelection.showDialog();
 				drawingPanel.setByteOrientation(od.getByteOrientation());
 				drawingPanel.setMSBPosition(od.getMSBPosition());
 			}
 		});
         
         sizeMenu = new JMenuItem("Canvas Size");
         sizeMenu.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				int[] canvasSize = drawingPanel.getCanvasSize();
 				String restrictedDirection = "";
 				switch(drawingPanel.getByteOrientation())
 				{
 					case VERTICAL:
 					{
 						restrictedDirection = "height";
 						break;
 					}
 					
 					case HORIZONTAL:
 					{
 						restrictedDirection = "width";
 						break;
 					}
 				}
 				while(true)
 				{
 					try
 					{
 						SizeSelectionDialog select = new SizeSelectionDialog(canvasSize[0], canvasSize[1], drawingPanel.getByteOrientation(), SpriteMakerWindow.this);
 						drawingPanel.setCanvasSize(select.showDialog());
 					}
 					catch(BadCanvasSizeError oops)
 					{
 						JOptionPane.showMessageDialog(SpriteMakerWindow.this, restrictedDirection + " must be a multiple of 8!");
 						continue;
 					}
 					break;
 				}
 			}
 		});
         
         outputFormatOptions = new JMenuItem("Output Format");
         outputFormatOptions.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				String[] currentOutputFormatting = SpriteMakerWindow.this.drawingPanel.getOutputFormatting();
 				OutputFormatDialog outDialog = new OutputFormatDialog(currentOutputFormatting[0], currentOutputFormatting[1], SpriteMakerWindow.this);
 				currentOutputFormatting = outDialog.showDialog();
 				SpriteMakerWindow.this.drawingPanel.setPrefix(currentOutputFormatting[0]);
 				SpriteMakerWindow.this.drawingPanel.setPostfix(currentOutputFormatting[1]);
 			}
 		});
         
 		importImage = new JMenuItem("import image");
 		importImage.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				//use a FileDialog to get the file we want to write.
 				FileDialog fd = new FileDialog(SpriteMakerWindow.this, "select a file", FileDialog.LOAD);
 				//fd.setMultipleMode(false);
 
 				fd.setFilenameFilter(new FilenameFilter()
 				{
 					//accept all files.
 					public boolean accept(File dir, String name)
 					{
 						return true;
 					}
 				});
 				fd.setVisible(true);
 				String filepath = fd.getDirectory()+fd.getFile();
 				BufferedImage image = null;
 				try
 				{
 					image = ImageIO.read(new File(filepath));
 				}
 				catch(IOException oops)
 				{
 					JOptionPane.showMessageDialog(SpriteMakerWindow.this, "image could not be opened");
 					return;
 				}
				String s = (String)JOptionPane.showInputDialog(
									SpriteMakerWindow.this,
									"Enter a grayscale color threshold\nin hexidecimal.",
									"Select color threshold",
									JOptionPane.PLAIN_MESSAGE,
									null,
									null,
									"40");
				int colorThresh = Integer.parseInt(s, 16);
				SpriteMakerWindow.this.drawingPanel.importImage(image, new Color(colorThresh, colorThresh, colorThresh), false);
 			}
 		});
 		
         //next, set up menu items and add submenus
         propertiesMenu = new JMenu("Format Options");
         propertiesMenu.add(pixelMappingMenu);
         propertiesMenu.add(outputFormatOptions);
         propertiesMenu.add(sizeMenu);
 		propertiesMenu.add(importImage);
         
         //now, set up the main menu bar
         mainMenuBar = new JMenuBar();
         mainMenuBar.add(propertiesMenu);
         
         //set this menu bar as the frame's menu
         this.setJMenuBar(mainMenuBar);
 		
 		this.setLayout(new GridBagLayout());
 		
 		//layout code
 		GridBagConstraints c = new GridBagConstraints();
 		
 		//layout for paintbrush options
 		c.fill = GridBagConstraints.BOTH;
 		c.anchor = GridBagConstraints.CENTER;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 0;
 		c.weighty = 1;
 		this.add(drawOptions, c);
 		
 		//layout for drawing panel.
 		drawingPanel.setPostfix(", ");
 		drawingPanel.setPrefix("$");
 		c.gridx = 1;
 		c.gridheight = 2;
 		c.weightx = 2;
 		c.weighty = 2;
 		this.add(drawingPanel, c);
 		
 		//layout for button
 		c.gridheight = 1;
 		c.weighty = 0;
 		c.weightx = 0;
 		c.gridy = 1;
 		c.gridx = 0;
 		getStringButton.addActionListener(new GenerateStringListener());
 		this.add(getStringButton, c);
 		
 		//layout for text area
 		c.fill = GridBagConstraints.BOTH;
 		c.gridy = 2;
 		c.gridx = 0;
 		c.weighty = 0;
 		c.weightx = 0;
 		c.gridwidth = 2;
 		stringOutput.setEditable(false);
 		stringOutput.setLineWrap(true);
 		stringOutput.setWrapStyleWord(true);
 		this.add(stringOutput, c);
 		
 
 		
 		Timer graphicsTimer = new Timer();
 		graphicsTimer.scheduleAtFixedRate(new UpdateGraphics(), updateRate, updateRate);
 	}
 	
    	class UpdateGraphics extends TimerTask
 	{
 		public void run()
 		{
 			drawOptions.repaint();
 			drawingPanel.repaint();
 		}
 	}
 }
