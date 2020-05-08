 /*
  * 
  *   Rasterizer GUI Version
  *   $Id$
  * 
  *   This program is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation; either version 2 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program; if not, write to the Free Software
  *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * 
  */
 package de.krutisch.jan.rasterizer;
 import javax.swing.*;         
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 //import com.lowagie.text.PageSize;
 import java.util.*;
 import java.net.URL;
 
 
 public class RasterizerGui implements ActionListener{
     /**
      * Create the GUI and show it.  For thread safety,
      * this method should be invoked from the
      * event-dispatching thread.
      */
 	
 	static final String[] pageFormatOptions= {"A4","A3","LETTER","LEGAL"};
 	//static final String[] colorOptions= {"Schwarz/Weiss","Einfache Farbe"};
 	//static final String[] cropmarkOptions= {"Keine","Smart","Alle"};
 	
 	JTextField imageFileTextField,pdfFileTextField;
 	JButton	imageFileButton,pdfFileButton,startButton,cancelButton;
 	JSpinner pagesSpinner;
 	JComboBox pageFormatComboBox,colorComboBox,cropmarkComboBox;
 	JCheckBox landscapeCheckBox;
 	JSpinner dotSizeSpinner;
 	JTextArea logTextArea;
 	JPanel pagePanel,filesPanel,stylePanel,buttonPanel;
 	JProgressBar pageProgressBar,overallProgressBar;
 	JFrame window;
 	JFileChooser imageChooser;
 	JFileChooser pdfChooser;
 	
 	RasterizerImage ri;
 	RasterizerPdf rp;
 	TextFieldLogger logger;
 	
 	File imageFile;
 	File pdfFile;
 	RasterThread thread;
 	ResourceBundle guires;
 	PropertyResourceArrayBundle arrayRes;
 	
 	
 	RasterizerGui() {
 		// loading locale
 		guires = ResourceBundle.getBundle("de.krutisch.jan.rasterizer.locale.guiresource");
 		arrayRes = new PropertyResourceArrayBundle(guires);
 		
 		URL imageURL = RasterizerGui.class.getResource("images/rasterizer.gif");
 		window = new JFrame(guires.getString("windowTitle"));
         window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         ImageIcon icon = new ImageIcon(imageURL);
         window.setIconImage(icon.getImage());
         
         Component contents = createComponents();
         window.getContentPane().add(contents, BorderLayout.CENTER);
 
 		imageChooser = new JFileChooser();
 		imageChooser.addChoosableFileFilter(new ImageFileFilter());
 		imageChooser.setAccessory(new ImageFileAccessory(imageChooser));
 		pdfChooser = new JFileChooser();
 		pdfChooser.addChoosableFileFilter(new PdfFileFilter());
 
         
         //Display the window.
         window.pack();
         window.setVisible(true);
        
         logger = new TextFieldLogger();
         logger.setLogLevel(EventLogger.VERBOSE);
         
         URL pageSizeURL = RasterizerGui.class.getResource("defaultconfig/papersizes.xml");
 		PageFormatContainer pfc = new PageFormatContainer(logger);
 		pfc.parsePaperSizeXML(new File("papersizes.xml"));
 		pfc.parsePaperSizeXML(pageSizeURL);
 		
         pageFormatComboBox.setModel(new DefaultComboBoxModel(pfc.getVector()));
 		//logger.log(EventLogger.VERBOSE,"Setting up...");
 		ri = RasterizerImage.getInstance(logger);
 		rp = RasterizerPdf.getInstance(logger);
 		rp.setProgressBar(overallProgressBar);
 		
 	}
 	
 	public Component createComponents() {
        
         /*
          * An easy way to put space between a top-level container
          * and its contents is to put the contents in a JPanel
          * that has an "empty" border.
          */
 		
 		
 /*
 		PageFormat pf = new PageFormat("---","SEPARATOR",200,200);
 		pfc.add(pf);
 		pf = new PageFormat("A4","A4 ohne Border",595,842);
 		pfc.add(pf);
 		pf = new PageFormat("A3","A3 ohne Border",842,1190);
 		pfc.add(pf);
 		pf = new PageFormat("LEGAL","LEGAL ohne Border",612,1008);
 		pfc.add(pf);
 */		
         JPanel pane = new JPanel();
         pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
         pane.setBorder(BorderFactory.createEmptyBorder(
                                         5, //top
                                         5, //left
                                         5, //bottom
                                         5) //right
                                         );
         
         filesPanel = new JPanel(new GridLayout(0,3));
         	filesPanel.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createTitledBorder(guires.getString("files")),
                 BorderFactory.createEmptyBorder(5,5,5,5)));
         	JLabel imageLabel = new JLabel(guires.getString("image"));
         	imageFileTextField = new JTextField();
         	imageLabel.setLabelFor(imageFileTextField);
         	filesPanel.add(imageLabel);
         	filesPanel.add(imageFileTextField);
         	imageFileButton = new JButton(guires.getString("fileSelect"));
         	imageFileButton.setActionCommand("IMAGEFILE");
         	imageFileButton.addActionListener(this);
         	filesPanel.add(imageFileButton);
         	
         	JLabel pdfLabel = new JLabel(guires.getString("pdf"));
         	pdfFileTextField = new JTextField();
         	pdfLabel.setLabelFor(pdfFileTextField);
         	filesPanel.add(pdfLabel);
         	filesPanel.add(pdfFileTextField);
         	pdfFileButton = new JButton(guires.getString("fileSelect"));
         	pdfFileButton.setActionCommand("PDFFILE");
         	pdfFileButton.addActionListener(this);
         	filesPanel.add(pdfFileButton);
         	
         	
         	
         pane.add(filesPanel);
         
         pagePanel = new JPanel(new GridLayout(0,2));
         pagePanel.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createTitledBorder(guires.getString("pageOptions")),
                 BorderFactory.createEmptyBorder(5,5,5,5)));
         	JLabel pagesLabel = new JLabel(guires.getString("horizontalPages"));
         	pagesSpinner = new JSpinner();
         	pagesSpinner.setModel(new SpinnerNumberModel(3,1,100,1));
            	pagesLabel.setLabelFor(pagesSpinner);
         	pagePanel.add(pagesLabel);
         	pagePanel.add(pagesSpinner);
         	pageFormatComboBox = new JComboBox();
         	JLabel pageFormatLabel = new JLabel(guires.getString("pageFormat"));
         	pageFormatLabel.setLabelFor(pageFormatComboBox);
         	pagePanel.add(pageFormatLabel);
         	pagePanel.add(pageFormatComboBox);
         	landscapeCheckBox = new JCheckBox(guires.getString("landscapeFormat"));
         	pagePanel.add(landscapeCheckBox);
         pane.add(pagePanel);
 
         stylePanel = new JPanel(new GridLayout(0,2));
         stylePanel.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createTitledBorder(guires.getString("styleOptions")),
                 BorderFactory.createEmptyBorder(5,5,5,5)));
         	dotSizeSpinner = new JSpinner();
         	dotSizeSpinner.setModel(new SpinnerNumberModel(10,4,30,1));
         	JLabel dotSizeLabel = new JLabel(guires.getString("maxDotSize"));
         	dotSizeLabel.setLabelFor(dotSizeSpinner);
         	stylePanel.add(dotSizeLabel);
         	stylePanel.add(dotSizeSpinner);
         	colorComboBox = new JComboBox(arrayRes.getStringArray("colorModeOptions"));
         	JLabel colorLabel = new JLabel(guires.getString("colorMode"));
         	colorLabel.setLabelFor(colorComboBox);
         	stylePanel.add(colorLabel);
         	stylePanel.add(colorComboBox);
         	cropmarkComboBox = new JComboBox(arrayRes.getStringArray("cropmarkOptions"));
         	JLabel cropmarkLabel = new JLabel(guires.getString("cropmarks"));
         	cropmarkLabel.setLabelFor(cropmarkComboBox);
         	stylePanel.add(cropmarkLabel);
         	stylePanel.add(cropmarkComboBox);
 
         	
         pane.add(stylePanel);
 
         buttonPanel = new JPanel(new GridLayout(0,2));
         startButton = new JButton(guires.getString("startRasterize"));
         startButton.setMnemonic(KeyEvent.VK_I);
         startButton.setActionCommand("START");
         startButton.addActionListener(this);
        
         buttonPanel.add(startButton);
         JButton cancelButton = new JButton(guires.getString("cancel"));
         cancelButton.setActionCommand("CANCEL");
         cancelButton.addActionListener(this);
        
         cancelButton.setEnabled(false);
         buttonPanel.add(cancelButton);
         
         
         pane.add(buttonPanel);
         overallProgressBar = new JProgressBar();
         overallProgressBar.setStringPainted(true);
         overallProgressBar.setString("");
         overallProgressBar.setEnabled(false);
         pane.add(overallProgressBar);
         JLabel versionLabel = new JLabel("$Revision$".replace('$',' '));
         versionLabel.setAlignmentX(0.5f);
         pane.add(versionLabel);
 
         logTextArea = new JTextArea();
         JScrollPane scrollPane =  new JScrollPane(logTextArea,
                             JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                             JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
         logTextArea.setEditable(false);
         scrollPane.setMinimumSize(new Dimension(200,50));
         scrollPane.setPreferredSize(new Dimension(200,200));
         scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
 
         pane.add(scrollPane);
         pane.setMinimumSize(new Dimension(200,600));
         pane.setPreferredSize(new Dimension(400,600));
         pane.setMaximumSize(new Dimension(400,800));
         
         return pane;
     }
 
 	private void setOptions() {
 		ri.loadImageFromFile(imageFileTextField.getText());
 		if (colorComboBox.getSelectedIndex()==1) {
 			rp.setColorMode(RasterizerPdf.SIMPLECOLOR);
 		} else {
 			rp.setColorMode(RasterizerPdf.NOCOLOR);
 		}
 		SpinnerNumberModel nm = (SpinnerNumberModel)pagesSpinner.getModel();
 		rp.setHorizontalPages(nm.getNumber().intValue());
 
 		nm = (SpinnerNumberModel)dotSizeSpinner.getModel();
 		rp.setDotSize(nm.getNumber().floatValue());
 		if (landscapeCheckBox.getSelectedObjects()!=null) {
 			rp.setLandscape(true);
 		} else {
 			rp.setLandscape(false);
 		}
 		
 		rp.setOutputFile(pdfFileTextField.getText());
 		
 		PageFormat pf = (PageFormat)pageFormatComboBox.getSelectedItem();
 		rp.setPageSize(new com.lowagie.text.Rectangle(pf.getWidth(),pf.getHeight()));
 /*		String format = (String)pageFormatComboBox.getSelectedItem();
 		
 		if (format.equals("A4")) rp.setPageSize(PageSize.A4);
 		if (format.equals("A3")) rp.setPageSize(PageSize.A3);
 		if (format.equals("LETTER")) rp.setPageSize(PageSize.LETTER);
 		if (format.equals("LEGAL")) rp.setPageSize(PageSize.LEGAL);
 	*/	
 		
 		rp.setMargins(pf.getMarginLeft(),pf.getMarginRight(),pf.getMarginTop(),pf.getMarginBottom());
 		
 		switch (cropmarkComboBox.getSelectedIndex()) {
 			case 0:
 				rp.setCropmarks(RasterizerPdf.NOCROPMARKS);
 				break;
 			case 1:
 				rp.setCropmarks(RasterizerPdf.CROPMARKS);
 				break;
 			case 2:
 				rp.setCropmarks(RasterizerPdf.ALLCROPMARKS);
 				break;
 			default:
 				rp.setCropmarks(RasterizerPdf.NOCROPMARKS);
 				
 		}
 	}
 
 	private void switchGuiRunning() {
 		
 	}
 	private void switchGuiStopped() {
 		
 	}
 	
 	public void actionPerformed(ActionEvent event) {
 		
 		if (event.getActionCommand().equals("START")) {
 			if (!validateOptions()) { 
 				JOptionPane.showMessageDialog(window,guires.getString("missingParameters"));
 				return;
 			}
 			setOptions();
 			switchGuiRunning();
 			thread = new RasterThread(ri,rp,logger);
 			thread.start();
 		}
 		
 		if (event.getActionCommand().equals("CANCEL")) {
 			if (JOptionPane.showConfirmDialog(window,guires.getString("reallyCancel"),guires.getString("cancelTitle"),JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.YES_OPTION) {
 			
 			if (thread!=null && thread.isAlive()) {
 				logger.log(EventLogger.TERSE,"");
 				thread.interrupt();
 				thread = null;
 				switchGuiStopped();
 			}
 			}
 		}
 		
 		if (event.getActionCommand().equals("IMAGEFILE")) {
 			int returnVal = imageChooser.showOpenDialog(window);
 
 	        if (returnVal == JFileChooser.APPROVE_OPTION) {
 	            imageFile = imageChooser.getSelectedFile();
 	            imageFileTextField.setText(imageFile.getAbsolutePath());
 	        }
 		}
 		if (event.getActionCommand().equals("PDFFILE")) {
 			int returnVal = pdfChooser.showSaveDialog(window);
 
 	        if (returnVal == JFileChooser.APPROVE_OPTION) {
 	            pdfFile = pdfChooser.getSelectedFile();
 	            String pdfFilePath = pdfFile.getAbsolutePath();
 	            
 	            if (pdfFile.exists()) {
 	            	if (JOptionPane.showConfirmDialog(window,guires.getString("fileExistsOverwrite"),guires.getString("fileExistsTitle"),JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.NO_OPTION) {
 	            		pdfFilePath = "";
 	            	}
 	            }
 	            if (!pdfFilePath.toLowerCase().endsWith(".pdf")) {
 	            	pdfFilePath += ".pdf";
 	            }
 	            pdfFileTextField.setText(pdfFilePath);
 	            
 	        } 
 		}
 		
 		
 	}
 
 	public boolean validateOptions() {
 		logger.log(EventLogger.VERBOSE,"Validating Options:");
 		if (imageFileTextField.getText().length()==0) {
 			logger.log(EventLogger.VERBOSE,"empty filename (Image)");
 			return false;
 		}
 		if (pdfFileTextField.getText().length()==0) {
 			logger.log(EventLogger.VERBOSE,"empty filename (Pdf)");
 			return false;
 		}
 		
 		logger.log(EventLogger.VERBOSE,"Validating Options finished.");
 		return true;
 	}
 	
     private static void createAndShowGUI() {
         //Make sure we have nice window decorations.
         //JFrame.setDefaultLookAndFeelDecorated(true);
         //Create and set up the window.
         RasterizerGui app = new RasterizerGui();
         
     }
 
     public static void main(String[] args) {
         //Schedule a job for the event-dispatching thread:
         //creating and showing this application's GUI.
     	
     	try {
             UIManager.setLookAndFeel(
                 UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) { }
     	
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createAndShowGUI();
             }
         });
     }
     
     public class TextFieldLogger extends EventLogger {
     	public void log(String text) {
     		if (logTextArea !=null) {
     			logTextArea.append(text + "\n");
     		}
     	}
     }
 }
