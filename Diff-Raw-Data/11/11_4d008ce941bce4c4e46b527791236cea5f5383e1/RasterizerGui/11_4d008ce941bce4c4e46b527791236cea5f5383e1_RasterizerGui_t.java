 package de.krutisch.jan.rasterizer;
 import javax.swing.*;         
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import com.lowagie.text.PageSize;
 
 
 public class RasterizerGui implements ActionListener{
     /**
      * Create the GUI and show it.  For thread safety,
      * this method should be invoked from the
      * event-dispatching thread.
      */
 	
 	static final String[] pageFormatOptions= {"A4","A3","LETTER","LEGAL"};
 	static final String[] colorOptions= {"Schwarz/Weiss","Einfache Farbe"};
 	static final String[] cropmarkOptions= {"Keine","Smart","Alle"};
 	
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
 	
 	RasterizerGui() {
 		
 		window = new JFrame("RasterizerGui");
         window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         Component contents = createComponents();
         window.getContentPane().add(contents, BorderLayout.CENTER);
 
 		imageChooser = new JFileChooser();
 		pdfChooser = new JFileChooser();
 
         
         //Display the window.
         window.pack();
         window.setVisible(true);
         logger = new TextFieldLogger();
         logger.setLogLevel(EventLogger.VERBOSE);
 		logger.log(EventLogger.VERBOSE,"Setting up...");
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
                 BorderFactory.createTitledBorder("Dateien"),
                 BorderFactory.createEmptyBorder(5,5,5,5)));
         	JLabel imageLabel = new JLabel("Bild");
         	imageFileTextField = new JTextField();
         	imageLabel.setLabelFor(imageFileTextField);
         	filesPanel.add(imageLabel);
         	filesPanel.add(imageFileTextField);
         	imageFileButton = new JButton("<html>Ausw&auml;hlen...</html>");
         	imageFileButton.setActionCommand("IMAGEFILE");
         	imageFileButton.addActionListener(this);
         	filesPanel.add(imageFileButton);
         	
         	JLabel pdfLabel = new JLabel("Pdf");
         	pdfFileTextField = new JTextField();
         	pdfLabel.setLabelFor(pdfFileTextField);
         	filesPanel.add(pdfLabel);
         	filesPanel.add(pdfFileTextField);
         	pdfFileButton = new JButton("<html>Ausw&auml;hlen...</html>");
         	pdfFileButton.setActionCommand("PDFFILE");
         	pdfFileButton.addActionListener(this);
         	filesPanel.add(pdfFileButton);
         	
         	
         	
         pane.add(filesPanel);
         
         pagePanel = new JPanel(new GridLayout(0,2));
         pagePanel.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createTitledBorder("Seitenoptionen"),
                 BorderFactory.createEmptyBorder(5,5,5,5)));
         	JLabel pagesLabel = new JLabel("Horizontale Seitenanzahl");
         	pagesSpinner = new JSpinner();
         	pagesSpinner.setModel(new SpinnerNumberModel(3,1,100,1));
            	pagesLabel.setLabelFor(pagesSpinner);
         	pagePanel.add(pagesLabel);
         	pagePanel.add(pagesSpinner);
         	pageFormatComboBox = new JComboBox(pageFormatOptions);
         	JLabel pageFormatLabel = new JLabel("Seitenformat");
         	pageFormatLabel.setLabelFor(pageFormatComboBox);
         	pagePanel.add(pageFormatLabel);
         	pagePanel.add(pageFormatComboBox);
         	landscapeCheckBox = new JCheckBox("Querformat");
         	pagePanel.add(landscapeCheckBox);
         pane.add(pagePanel);
 
         stylePanel = new JPanel(new GridLayout(0,2));
         stylePanel.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createTitledBorder("Stiloptionen"),
                 BorderFactory.createEmptyBorder(5,5,5,5)));
         	dotSizeSpinner = new JSpinner();
         	dotSizeSpinner.setModel(new SpinnerNumberModel(10,4,30,1));
         	JLabel dotSizeLabel = new JLabel("Maximaler Punktdurchmesser");
         	dotSizeLabel.setLabelFor(dotSizeSpinner);
         	stylePanel.add(dotSizeLabel);
         	stylePanel.add(dotSizeSpinner);
         	colorComboBox = new JComboBox(colorOptions);
         	JLabel colorLabel = new JLabel("Farbmodus");
         	colorLabel.setLabelFor(colorComboBox);
         	stylePanel.add(colorLabel);
         	stylePanel.add(colorComboBox);
         	cropmarkComboBox = new JComboBox(cropmarkOptions);
         	JLabel cropmarkLabel = new JLabel("Schnittmarken");
         	cropmarkLabel.setLabelFor(cropmarkComboBox);
         	stylePanel.add(cropmarkLabel);
         	stylePanel.add(cropmarkComboBox);
 
         	
         pane.add(stylePanel);
 
         buttonPanel = new JPanel(new GridLayout(0,2));
         startButton = new JButton("Rasterize!");
         startButton.setMnemonic(KeyEvent.VK_I);
         startButton.setActionCommand("START");
         startButton.addActionListener(this);
        
         buttonPanel.add(startButton);
         JButton cancelButton = new JButton("Cancel");
         cancelButton.setActionCommand("CANCEL");
         cancelButton.addActionListener(this);
        
         cancelButton.setEnabled(false);
         buttonPanel.add(cancelButton);
         
         
         pane.add(buttonPanel);
         pageProgressBar = new JProgressBar();
         pageProgressBar.setString("0/0 Pages");
         pageProgressBar.setStringPainted(true);
         pageProgressBar.setValue(0);
         pageProgressBar.setEnabled(false);
         //pane.add(pageProgressBar);
         overallProgressBar = new JProgressBar();
         overallProgressBar.setStringPainted(true);
         overallProgressBar.setString("");
         overallProgressBar.setEnabled(false);
         pane.add(overallProgressBar);
         logTextArea = new JTextArea();
         JScrollPane scrollPane =  new JScrollPane(logTextArea,
                             JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                             JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
         logTextArea.setEditable(false);
         pane.add(scrollPane);
         
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
 		
 		String format = (String)pageFormatComboBox.getSelectedItem();
 		if (format.equals("A4")) rp.setPageSize(PageSize.A4);
 		if (format.equals("A3")) rp.setPageSize(PageSize.A3);
 		if (format.equals("LETTER")) rp.setPageSize(PageSize.LETTER);
 		if (format.equals("LEGAL")) rp.setPageSize(PageSize.LEGAL);
 		
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
 				JOptionPane.showMessageDialog(window,"Fehlende oder falsche Eingaben");
 				return;
 			}
 			setOptions();
 			switchGuiRunning();
 			thread = new RasterThread(ri,rp,logger);
 			thread.start();
 		}
 		
 		if (event.getActionCommand().equals("CANCEL")) {
 			if (JOptionPane.showConfirmDialog(window,"Aktion wirklich abbrechen ?","Abbruch",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.YES_OPTION) {
 			
 			if (thread!=null && thread.isAlive()) {
 				logger.log(EventLogger.TERSE,"Trying abortion...");
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
 	            imageFileTextField.setText(imageFile.toString());
 	        }
 		}
 		if (event.getActionCommand().equals("PDFFILE")) {
			int returnVal = pdfChooser.showSaveDialog(window);
 
 	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            pdfFile = pdfChooser.getSelectedFile();
 	            if (pdfFile.exists()) {
 	            	if (JOptionPane.showConfirmDialog(window,"Datei vorhanden.\n ?berschreiben?","Datei vorhanden",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.YES_OPTION) {
 	            		pdfFileTextField.setText(pdfFile.toString());
 	            	}
	            } else {
	            	pdfFileTextField.setText(pdfFile.toString());
 	            }
 	            
	        } else {
	        	logger.log("FR.option:" + returnVal);
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
         JFrame.setDefaultLookAndFeelDecorated(true);
         //Create and set up the window.
         RasterizerGui app = new RasterizerGui();
         
     }
 
     public static void main(String[] args) {
         //Schedule a job for the event-dispatching thread:
         //creating and showing this application's GUI.
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
