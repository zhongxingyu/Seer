 /**
  * @file SearchWaldoClient.java
  */
 
 /**
  * @author Arefin
  *
  */
 
 import java.io.*;
 import java.net.*;
 import java.nio.channels.SocketChannel;
 import java.util.Random;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.beans.*;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.sun.xml.internal.ws.api.server.Container;
 
 public class SearchWaldoClient extends JPanel
 							   implements ActionListener,
 							   			  PropertyChangeListener {
 
 	//GUI
 	static private final String newline = "\n";
     JButton openButton, submitButton;
     JTextArea log;
     JFileChooser fc;
     JFrame dailogFrame;
     
     //TCP/IP
     static int[] port;	
 	static String ipAddress;
 	static int[] portsTried;
 	static String clusterConfig;
 	static String logMsg;
 	static int progressCounter;
 	
 	private Task task;
 	
 	class Task extends SwingWorker<Void, Void> {
         /*
          * Main task. Executed in background thread.
          */
         /* (non-Javadoc)
          * @see javax.swing.SwingWorker#doInBackground()
          */
         @Override
         public Void doInBackground() {
            
         	
         	int selectedPort;
     		// Read in the appropriate server info
     		extractServerInfo();
     		
     		//select the server in random
     		selectedPort = selectPort(0);
     		    		
     		setProgress(0);
     		//Communicate with the TCP server
     		//try all port if the comm fails or break when it's done
     		progressCounter = 0;
     		for (int i = 0; i < port.length; i++){
 	    		
 	    		try{	    			    		
 	    			
 	    			logMsg = "Connecting to the server " + ipAddress + " on port " + selectedPort;
 	    			setProgress(setProgressCounter());
 	    			//try to open a tcp/ip connection
 	    			InetAddress ia = InetAddress.getByName(ipAddress);
 	    			Socket csSocket = new Socket(ia, selectedPort);
 	    		    			
 	    		
 	    			if (csSocket.isConnected()){
 	    				logMsg = "Connected to the server.";
 	    				setProgress(setProgressCounter());
 	    				
 	    				logMsg = "Uploading file: " + fc.getSelectedFile().getPath() + ".";
 	    				setProgress(setProgressCounter());
 	    				
 	    				//start the time count
 	    				long start = System.currentTimeMillis();
 	    				
 	    				
 	    				File myFile = fc.getSelectedFile();	    				
 	    				
 	    				
 	    				//Send the file	    
 	    				FileTransfer.sendFile(csSocket, myFile);
 	    				
 	    				//csSocket.close();
 	    				
 	    				logMsg = "File upload successful.";
 	    				setProgress(setProgressCounter());
 	    				
 	    				//monitor progress
 	    				logMsg = "Task Completed 0%.";
 	    				setProgress(setProgressCounter());
 	    					    				 
 	    				//receive the file	  
 	    				String filePath = fc.getSelectedFile().getParentFile().getPath();
	    				String outFile = FileTransfer.receiveFile(csSocket, filePath);
 	    						
 	    						//fc.getSelectedFile().getParentFile() + "\\"  + fc.getSelectedFile().getName().substring(0, fc.getSelectedFile().getName().indexOf(".")) + "-highlight." +  fc.getSelectedFile().getName().substring(fc.getSelectedFile().getName().indexOf("."), fc.getSelectedFile().getName().length());
 	    				
 	    					    				
 	    			    long end = System.currentTimeMillis();
 	    			    
 	    			    logMsg = "Total time to process the request: " + (end-start) + ".";
 	    				setProgress(setProgressCounter());
 	    			    
 	    			    csSocket.close();
 	    			    
 	    			    //Display image
 	    			    DisplayImage.displayImgFrame(outFile);
 	    			  
 	    			}
 	    			else{
 	    				//read server response
 	
 	    			}
 	    				
 	    				    				    			
 	    			//get out of the loop
 	    			
 	    			i = port.length;
 	    			
 	    			
 	    		}
 	    		catch (IOException e) {
 	    			//e.printStackTrace();
 	    				    			
 	    			logMsg = "Unable to connect to the server " + ipAddress + " on port " + selectedPort;
 	    			setProgress(setProgressCounter());
 	    			
 	    			//go to the next port
 	    			selectedPort = selectPort(selectedPort);
 	    			
 	    		}        	
 	    		
 	    		
     		}
         	
             return null;
         }
  
         /*
          * Executed in event dispatching thread
          */
         @Override
         public void done() {
             Toolkit.getDefaultToolkit().beep();
             //startButton.setEnabled(true);
             setCursor(null); //turn off the wait cursor
             //taskOutput.append("Done!\n");
         }
     }
 	
 	
 	public int setProgressCounter(){
 		
 		try {
             Thread.sleep(1000);
         } catch (InterruptedException ignore) {}
 		
 		if (progressCounter >= 100){
 			progressCounter = 0;			
 		}
 		
 		return progressCounter++;
 	}
 	
 	
 	public SearchWaldoClient(){
 		//super(new GridLayout(3,1));  //3 rows, 1 column
 		super(new BorderLayout());
 		
         JLabel label1, label2;
         
         //Create labels
         label1 = new JLabel("The application will search waldo using distributed algorithm.", JLabel.CENTER);
         label2 = new JLabel("Please select an image and press submit to search waldo.", JLabel.CENTER);
         
         //Create tool tips, for the heck of it.
         label1.setToolTipText("The application will search waldo using distributed algorithm.");
         label2.setToolTipText("Please select an image and press submit to search waldo.");        
  
         //Add the labels.
         JPanel labelPanel = new JPanel(); //use FlowLayout        
         labelPanel.add(label1);
         labelPanel.add(label2);
  
        // add(label1);
        // add(label2);
         
         
         
         
         //Create the log first, because the action listeners
         //need to refer to it.
         log = new JTextArea(30,20);
         log.setMargin(new Insets(5,5,5,5));
         log.setEditable(false);
         JScrollPane logScrollPane = new JScrollPane(log);
         
         //Create a file chooser
         fc = new JFileChooser();
 		
         //Create the open button.  We use the image from the JLF
         //Graphics Repository (but we extracted it from the jar).
         openButton = new JButton("Open Image");
         openButton.addActionListener(this);
  
         //Create the save button.  We use the image from the JLF
         //Graphics Repository (but we extracted it from the jar).
         submitButton = new JButton("Submit Image");
         submitButton.addActionListener(this);
  
         //For layout purposes, put the buttons in a separate panel
         JPanel buttonPanel = new JPanel(); //use FlowLayout        
         buttonPanel.add(openButton);
         buttonPanel.add(submitButton);
  
         //Add the buttons and the log to this panel.
         add(labelPanel, BorderLayout.PAGE_START);
         add(buttonPanel, BorderLayout.CENTER);
         add(logScrollPane, BorderLayout.PAGE_END);
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		 
         //Handle open button action.
         if (e.getSource() == openButton) {
             int returnVal = fc.showOpenDialog(SearchWaldoClient.this);
  
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 File file = fc.getSelectedFile();
                 //This is where a real application would open the file.
                 log.append("Opening: " + file.getName() + "." + newline);
             } else {
                 log.append("Open command cancelled by user." + newline);
             }
             log.setCaretPosition(log.getDocument().getLength());
  
         //Handle submit button action.
         } else if (e.getSource() == submitButton) {
         	File file = fc.getSelectedFile();
         	if (file == null){
         		log.append("You need to select a file before trying to submit" + newline);
         	}else{
         		int returnVal = JOptionPane.showConfirmDialog(
                 		dailogFrame, "Would you to search waldo in " + file.getName() + "?",
     		                    "Confirm your selection",
     		                    JOptionPane.YES_NO_OPTION);
             	
                 
                 if (returnVal == JOptionPane.YES_OPTION) {
                     
                 	//we create new instances as needed.
                     task = new Task();
                     task.addPropertyChangeListener(this);
                     task.execute();
                 	//submitImage();
                 	//File file = fc.getSelectedFile();
                     
                     //
                     
                     //This is where a real application would save the file.
                     
                     
                     
                    
                 } else {
                     log.append("Submit command cancelled by user." + newline);
                 }
                 
         		
         	}
             
             
             
             log.setCaretPosition(log.getDocument().getLength());
         }
     }
 	
 	/**
      * Invoked when task's progress property changes.
      */
     public void propertyChange(PropertyChangeEvent evt) {
         if ("progress" == evt.getPropertyName()) {
             //String progress = (String) evt.getNewValue();
             //progressBar.setValue(progress);
             //taskOutput.append(String.format(
               //      "Completed %d%% of task.\n", task.getProgress()));
             
             log.append(logMsg + newline);
         }
     }
 		
 	/** Returns an ImageIcon, or null if the path was invalid. */
     protected static ImageIcon createImageIcon(String path,
                                                String description) {
         java.net.URL imgURL = SearchWaldoClient.class.getResource(path);
         if (imgURL != null) {
             return new ImageIcon(imgURL, description);
         } else {
             System.err.println("Couldn't find file: " + path);
             return null;
         }
     }
 
     /**
      * Create the GUI and show it.  For thread safety,
      * this method should be invoked from the
      * event dispatch thread.
      */
     private static void createAndShowGUI() {
     	
     	//Ask for window decorations provided by the look and feel.
     	JFrame.setDefaultLookAndFeelDecorated(true);
     			
         //Create and set up the window.
         JFrame frame = new JFrame("Wheres Waldo Search Application");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  
         //Add content to the window.
         frame.add(new SearchWaldoClient());
  
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
     
 
     public static void main(String a[]) {
     	
     	
 		// Verify that user has passed in a proper server configuration
 		if (a.length < 1) {
 			System.out.println("Server configuration id not specified. Please enter aaron, arefin, or grader as argument");
 			return;
 		}
 		clusterConfig = a[0];
 		if (!clusterConfig.equals("aaron") && !clusterConfig.equals("arefin") && !clusterConfig.equals("rahul")) {
 			System.out.println("Server configuration id not valid. Please enter aaron, arefin, or grader as argument");
 			return;
 		}
 		
         //Schedule a job for the event dispatch thread:
         //creating and showing this application's GUI.
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
         //Turn off metal's use of bold fonts
             UIManager.put("swing.boldMetal", Boolean.FALSE);
                  
         createAndShowGUI();
             }
         });
     }
     
     
     //select an image
   	//submit the image
   		//select one of the server randomly
   		//try to open tcp/ip connection
   		//if not successful go to the next server on the list until successful	
   		//send the image to the server	
   	//wait for the server to finish processing
   	//monitor progress while waiting 
   	//show progress in the progress bar
   	//when done
   	//receive image from the server
   	//receive message from the server
 
     
     //will select a port at random, excludePort will have zero when it gets called for the first time
   	public static int selectPort(int excludePort){
   		
   		//we'll randomly pick one of the server	that was not selected previously
   		int selectedPort = excludePort;
   		
   		
   		//to avoid getting stuck in the while forever
   		if (selectedPort == 0){
   			selectedPort = port[new Random().nextInt(port.length)];	
   			portsTried = new int[]{selectedPort};
   		}
   		else{
   			
   			if (portsTried.length == port.length){
   				//clear portstried by only keeing the last port
   				//selectedPort = port[new Random().nextInt(port.length)];
   				portsTried = new int[]{portsTried[portsTried.length -1]};
   			}
   				
   			int[] randomPort = new int[port.length - portsTried.length];
   			
   			int index = 0;
   			dontAdd:
   			for (int i = 0; i < port.length; i++){
   				for (int k = 0; k < portsTried.length; k++){
   					
   					if (port[i] == portsTried[k]){
   						continue dontAdd;
   					}													
   				}
   				
   				//add
   				randomPort[index] = port[i];
   				index++;
   			}
   			
   			
   			//now select the randomport
   			selectedPort = randomPort[new Random().nextInt(randomPort.length)];
   			
   			//add selected port to portstried
   			
   			int[] cpyPort = portsTried;
   			
   			portsTried = new int[cpyPort.length + 1];
   			
   			for (int k = 0; k < cpyPort.length; k++){
   				portsTried[k] = cpyPort[k];
   			}
   			
   			portsTried[portsTried.length - 1] = selectedPort;
   				
   				
   			
   		}
   		
   		
   		
   		
   		return selectedPort;
   	}
   	
     
     public static void extractServerInfo() {
 		try	{
 			File serverXml = new File("servers.xml");
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc = dBuilder.parse(serverXml);
 	
 			// Recommended for parsing
 			doc.getDocumentElement().normalize();
 	
 			// Retrieving the config info
 			NodeList clusterInfo = doc.getElementsByTagName("cluster");
 			Element e = (Element) clusterInfo.item(0);
 			
 			for ( int i = 0 ; !e.getAttribute("id").equals(clusterConfig) ; i++) {
 				e = (Element) clusterInfo.item(i);
 			}
 			
 			// Capturing info
 			ipAddress = e.getElementsByTagName("ipAddress").item(0).getTextContent();			
 			
 			NodeList serverInfo = e.getElementsByTagName("server");  
 			port = new int[serverInfo.getLength()];
 			
 			for (int i = 0; i < port.length; i++){
 			
 				port[i] = Integer.parseInt(((Element) serverInfo.item(i)).getElementsByTagName("port").item(0).getTextContent());
 			}
 			
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
     
        
     
     
     
 	
 	
 	
 	
 	
 	
 	
 	
 }
