 package visualizer;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Hashtable;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.WindowConstants;
 
 import net.coobird.thumbnailator.Thumbnails;
 
 @SuppressWarnings("unused")
 public class EdgeDetectFlow implements ActionListener {
 	String filename, nameExt;
 	int index;
 	File[] files;
 	JFrame edgeFrame;
 	JPanel textPanel, picturePanel, previewPanel;
 	JLabel edgeLabel, edgeLabel2, imageCont;
 	JButton progressButton, previewButton;
 	String edgeDetectValue;
 	int edgeLowThres;
 	int edgeHighThres;
 	JSlider edgeLowSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 10);
 	JSlider edgeHighSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 10);
 	BufferedImage origImage;
     Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
     Component thumbnailImg, thumbnailEdges;
     
 	public EdgeDetectFlow(String file, String ext, int i, File[] fileList) {
 		filename = file;
 		nameExt = ext;
 		index = i;
 		files = fileList;
 	}
 
 	public void determineEdgeDetect(String num) {
 		edgeFrame = new JFrame("Edge Detection for Image #" + Integer.toString(index + 1));
         edgeFrame.setSize(800, 550);
         edgeFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         
         // Get size of the screen
         Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
         
         // Determine the new location of the window
         int w = edgeFrame.getSize().width;
         int h = edgeFrame.getSize().height;
         int x = (dim.width-w)/2;
         int y = (dim.height-h)/2;
          
         // Move the window
         edgeFrame.setLocation(x, y);
         
         edgeFrame.setContentPane(createEdgeDetectPane(Integer.toString(index + 1)));
         edgeFrame.setAlwaysOnTop(true);
         edgeFrame.setVisible(true);       
 	}
 	
 	public JPanel createEdgeDetectPane(String num) {
 	   	JPanel entireGUI = new JPanel();
 	   	entireGUI.setLayout(null);
 	   	
 	   	textPanel = new JPanel();
 	   	textPanel.setLayout(null);
 	   	textPanel.setLocation(400, 0);
 	   	textPanel.setSize(400, 400);
 	   	entireGUI.add(textPanel);
 	   	
 	   	picturePanel = new JPanel();
 	   	picturePanel.setLayout(null);
 	   	picturePanel.setLocation(0, 0);
 	   	picturePanel.setSize(400, 250);
 	   	entireGUI.add(picturePanel);
 	   	
 	   	previewPanel = new JPanel();
 	   	previewPanel.setLayout(null);
 	   	previewPanel.setLocation(0, 250);
 	   	previewPanel.setSize(400, 550);
 	   	entireGUI.add(previewPanel);
 	   	
 	   	JPanel buttonPanel = new JPanel();
 	   	buttonPanel.setLayout(null);
 	   	buttonPanel.setLocation(400,400);
 	   	buttonPanel.setSize(400, 250);
 	   	entireGUI.add(buttonPanel);
 	   	
 	   	JLabel welcomeText = new JLabel("Set Edge Detection for Image #" + Integer.toString(index+1));
 	   	welcomeText.setLocation(0, 0);
 	   	welcomeText.setSize(400, 50);
 	   	welcomeText.setHorizontalAlignment(0);
 	   	textPanel.add(welcomeText);
 	   	
 	   	//Insert image at 200 px tall
 	   	try{
 	   		origImage = ImageIO.read(new File(filename));
 	   		
 	   	    // print the dimensions of the photo
 	    	int startW = origImage.getWidth();
 	    	int startH = origImage.getHeight();
 	    	float ratio = 1;
 
 	    	// Difference in image vs previewbox	    	
 	    	int diffH = (200 - startH)/200;
 	    	int diffW = (350 - startW)/350;
 	    	if (diffH < diffW) {
 	    		ratio = (float) (200.0/startH);
 	    	} else {
 	    		ratio = (float) (350.0/startW);
 	    	}
 	    	
 	    	int newW = Math.round(startW * ratio);
 	    	int newH = Math.round(startH * ratio);
 	    	
 	    	// attempted cropping that's currently not doing anything
 	    	// TODO set up cropping/resizing
 	    	BufferedImage thumb = Thumbnails.of(origImage).size(newW, newH).asBufferedImage();
 	    	File outputfile = new File("thumb" + nameExt + ".jpg");
 	    	ImageIO.write(thumb, "jpg", outputfile);
 	    	
 	    	// TODO FIGURE OUT THIS WEIRD BUG
 	    	// Since updating graphics on a JFrame already on the screen is very buggy, currently creating a dummy window and deleting it in order to 
 	    	// refresh the view
 	    	JFrame testFrame = new JFrame("temp");
 	    	testFrame.setSize(800, 525);
 	        testFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 	        thumbnailImg =  new LoadGenericImage(outputfile.getCanonicalPath());
     	    testFrame.add(thumbnailImg);
 	        testFrame.setVisible(true);
 	        testFrame.dispose();
 	        thumbnailImg.setLocation(50, 25);
 	        picturePanel.add(thumbnailImg);
 	    	
 	    } catch (IOException e) {
     		e.printStackTrace();
 	    }
 	   	
         edgeLabel = new javax.swing.JLabel();
         edgeLabel.setText("Edge Detect Low Threshold: ");
 	    	
 	    edgeLabel.setLocation(0, 75);
 	    edgeLabel.setSize(400, 50);
 	    edgeLabel.setHorizontalAlignment(0);
 	    textPanel.add(edgeLabel);
 
 	    //Turn on labels at major tick marks.
 	    edgeLowSlider.setMajorTickSpacing(20);
 	    edgeLowSlider.setMinorTickSpacing(2);
 	    edgeLowSlider.setPaintTicks(true);
 
 	    //Create the label table
 	    Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
 	    labelTable.put( new Integer( 1 ), new JLabel("More Edges") );
 	    labelTable.put( new Integer( 20 ), new JLabel("Less Edges") );
 	    edgeLowSlider.setLabelTable( labelTable );
 	    edgeLowSlider.setPaintLabels(true);
 	    
 	    edgeLowSlider.setPaintLabels(true);
 	    
 	    edgeLowSlider.setLocation(100, 125);
 	    edgeLowSlider.setSize(200, 100);
 	    textPanel.add(edgeLowSlider);
 	    
         edgeLabel2 = new javax.swing.JLabel();
         edgeLabel2.setText("Edge Detect High Threshold: ");
 	    	
 	    edgeLabel2.setLocation(0, 225);
 	    edgeLabel2.setSize(400, 50);
 	    edgeLabel2.setHorizontalAlignment(0);
 	    textPanel.add(edgeLabel2);
 
 	    //Turn on labels at major tick marks.
 	    edgeHighSlider.setMajorTickSpacing(20);
 	    edgeHighSlider.setMinorTickSpacing(2);
 	    edgeHighSlider.setPaintTicks(true);
 
 	    edgeHighSlider.setLabelTable( labelTable );
 	    edgeHighSlider.setPaintLabels(true);
 	    
 	    edgeHighSlider.setLocation(100, 275);
 	    edgeHighSlider.setSize(200, 100);
 	    textPanel.add(edgeHighSlider);
 	    
 	    previewButton = new JButton("Preview");
 	    previewButton.setLocation(70, 0);
 	    previewButton.setSize(100, 50);
 	    previewButton.addActionListener(this);
 	    buttonPanel.add(previewButton);
 	    
 	    progressButton = new JButton("Next");
 	    progressButton.setLocation(230, 0);
 	    progressButton.setSize(100, 50);
 	    progressButton.addActionListener(this);
 	    buttonPanel.add(progressButton);
 	    	
 	    //content panes must be opaque
 	    entireGUI.setOpaque(true);
 	    return entireGUI;
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == previewButton) {
 						
 			int edgeLowThres = edgeLowSlider.getValue();
 			int edgeHighThres = edgeHighSlider.getValue();
 			float lowThres = edgeLowThres * 1.0f;
 			float highThres = edgeHighThres * 1.0f;
 
 		    if (lowThres > highThres) {
 		    	JOptionPane.showMessageDialog(edgeFrame, "The low threshold needs to be lower than the high threshold!", "Try again", JOptionPane.ERROR_MESSAGE);
 			} else {
 		    	try {
 		    		LoadProcessing createEdgePreview = new LoadProcessing();
 		    		createEdgePreview.getEdges(filename, nameExt, index, files, lowThres, highThres);
 		    	    
 		    	    // TODO FIGURE OUT THIS WEIRD BUG
 			    	// Since updating graphics on a JFrame already on the screen is very buggy, currently creating a dummy window and deleting it in order to 
 			    	// refresh the view
 		    	    JFrame testFrame = new JFrame("temp2");
 			    	testFrame.setSize(800, 550);
 			        testFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 			        
 			        // Get size of the screen
 			        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 			        
 			        // Determine the new location of the window
 			        int w = testFrame.getSize().width;
 			        int h = testFrame.getSize().height;
 			        int x = (dim.width-w)/2;
 			        int y = (dim.height-h)/2;
 			         
 			        // Move the window
 			        testFrame.setLocation(x, y);
 			        
 			        thumbnailEdges = new LoadGenericImage("thumbedges" + nameExt + ".jpg");			        
 			        testFrame.add(thumbnailEdges);
 			        testFrame.setVisible(true);
 			        testFrame.dispose();
 
 			        thumbnailEdges.setLocation(50, 0);
 			        previewPanel.removeAll();
 			        previewPanel.add(thumbnailEdges);
			        previewPanel.updateUI();
 			        	    	    
 		    	} catch (IOException e1) {
 		    		 // TODO Auto-generated catch block
 		    		e1.printStackTrace();
 		    	}
 		    }	
 		}
 		
 		else if(e.getSource() == progressButton) {
 			int edgeLowThres = edgeLowSlider.getValue();
 			int edgeHighThres = edgeHighSlider.getValue();
 			float lowThres = edgeLowThres * 1.0f;
 			float highThres = edgeHighThres * 1.0f;
 		    if (lowThres > highThres) {
 		    	JOptionPane.showMessageDialog(edgeFrame, "The low threshold needs to be lower than the high threshold!", "Try again", JOptionPane.ERROR_MESSAGE);
 			} else if (lowThres == 0 | highThres == 0) {
 		    	JOptionPane.showMessageDialog(edgeFrame, "The thresholds must be greater than 0!", "Try again", JOptionPane.ERROR_MESSAGE);
 			} else {
 		    	edgeFrame.dispose();
 		    	try {
 		    		LoadProcessing createDots = new LoadProcessing();
 		    		createDots.loadProcessing(filename, nameExt, index, files, lowThres, highThres);	
 		    	} catch (IOException e1) {
 		    		 // TODO Auto-generated catch block
 		    		e1.printStackTrace();
 		    	}
 		    }
 		 } 
 	 }
 }
