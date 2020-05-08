 /*
  * Created by JFormDesigner on Mon Apr 21 12:50:34 EDT 2008
  */
 
 package Provider.GoogleMapsStatic.TestUI;
 
 import Provider.GoogleMapsStatic.*;
 import Task.*;
 import Task.Manager.*;
 import Task.ProgressMonitor.*;
 import Task.Support.CoreSupport.*;
 import Task.Support.GUISupport.*;
 import com.jgoodies.forms.factories.*;
 import info.clearthought.layout.*;
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.methods.*;
 
 import javax.imageio.*;
 import javax.swing.*;
 import javax.swing.border.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.beans.*;
 import java.text.*;
 import java.util.concurrent.*;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.StringTokenizer;
 
 
 /** @author nazmul idris */
 public class SampleApp extends JFrame {
 //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 // data members
 //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 /** reference to task */
 private SimpleTask _task;
 /** this might be null. holds the image to display in a popup */
 private BufferedImage _img;
 /** this might be null. holds the text in case image doesn't display */
 private String _respStr;
 private HashMap <String,String> cityDetails = new HashMap<String,String>();
 
 //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 // main method...
 //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 
 public static void main(String[] args) {
   Utils.createInEDT(SampleApp.class);
 }
 
 //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 // constructor
 //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 
 private void doInit() {
   GUIUtils.setAppIcon(this, "burn.png");
   GUIUtils.centerOnScreen(this);
   setVisible(true);
 
   int W = 28, H = W;
   boolean blur = false;
   float alpha = .7f;
 
   try {
     btnGetMap.setIcon(ImageUtils.loadScaledBufferedIcon("ok1.png", W, H, blur, alpha));
     btnQuit.setIcon(ImageUtils.loadScaledBufferedIcon("charging.png", W, H, blur, alpha));
   }
   catch (Exception e) {
     System.out.println(e);
   }
 
   _setupTask();
 }
 
 /** create a test task and wire it up with a task handler that dumps output to the textarea */
 @SuppressWarnings("unchecked")
 private void _setupTask() {
 
   TaskExecutorIF<ByteBuffer> functor = new TaskExecutorAdapter<ByteBuffer>() {
     public ByteBuffer doInBackground(Future<ByteBuffer> swingWorker,
                                      SwingUIHookAdapter hook) throws Exception
     {
 
       _initHook(hook);
 
       // set the license key
       MapLookup.setLicenseKey(ttfLicense.getText());
       // get the uri for the static map
       String uri = MapLookup.getMap(Double.parseDouble(ttfLat.getText()),
                                     Double.parseDouble(ttfLon.getText()),
                                     Integer.parseInt(ttfSizeW.getText()),
                                     Integer.parseInt(ttfSizeH.getText()),
                                     Integer.parseInt(ttfZoom.getText())
       );
       sout("Google Maps URI=" + uri);
 
       // get the map from Google
       GetMethod get = new GetMethod(uri);
       new HttpClient().executeMethod(get);
 
       ByteBuffer data = HttpUtils.getMonitoredResponse(hook, get);
 
       try {
         _img = ImageUtils.toCompatibleImage(ImageIO.read(data.getInputStream()));
         sout("converted downloaded data to image...");
       }
       catch (Exception e) {
         _img = null;
         sout("The URI is not an image. Data is downloaded, can't display it as an image.");
         _respStr = new String(data.getBytes());
       }
 
       return data;
     }
 
     @Override public String getName() {
       return _task.getName();
     }
   };
 
   _task = new SimpleTask(
       new TaskManager(),
       functor,
       "HTTP GET Task",
       "Download an image from a URL",
       AutoShutdownSignals.Daemon
   );
 
   _task.addStatusListener(new PropertyChangeListener() {
     public void propertyChange(PropertyChangeEvent evt) {
       sout(":: task status change - " + ProgressMonitorUtils.parseStatusMessageFrom(evt));
       lblProgressStatus.setText(ProgressMonitorUtils.parseStatusMessageFrom(evt));
     }
   });
 
   _task.setTaskHandler(new
       SimpleTaskHandler<ByteBuffer>() {
         @Override public void beforeStart(AbstractTask task) {
           sout(":: taskHandler - beforeStart");
         }
         @Override public void started(AbstractTask task) {
           sout(":: taskHandler - started ");
         }
         /** {@link SampleApp#_initHook} adds the task status listener, which is removed here */
         @Override public void stopped(long time, AbstractTask task) {
           sout(":: taskHandler [" + task.getName() + "]- stopped");
           sout(":: time = " + time / 1000f + "sec");
           task.getUIHook().clearAllStatusListeners();
         }
         @Override public void interrupted(Throwable e, AbstractTask task) {
           sout(":: taskHandler [" + task.getName() + "]- interrupted - " + e.toString());
         }
         @Override public void ok(ByteBuffer value, long time, AbstractTask task) {
           sout(":: taskHandler [" + task.getName() + "]- ok - size=" + (value == null
               ? "null"
               : value.toString()));
           if (_img != null) {
             _displayImgInFrame();
           }
           else _displayRespStrInFrame();
 
         }
         @Override public void error(Throwable e, long time, AbstractTask task) {
           sout(":: taskHandler [" + task.getName() + "]- error - " + e.toString());
         }
         @Override public void cancelled(long time, AbstractTask task) {
           sout(" :: taskHandler [" + task.getName() + "]- cancelled");
         }
       }
   );
 }
 
 private SwingUIHookAdapter _initHook(SwingUIHookAdapter hook) {
   hook.enableRecieveStatusNotification(checkboxRecvStatus.isSelected());
   hook.enableSendStatusNotification(checkboxSendStatus.isSelected());
 
   hook.setProgressMessage(ttfProgressMsg.getText());
 
   PropertyChangeListener listener = new PropertyChangeListener() {
     public void propertyChange(PropertyChangeEvent evt) {
       SwingUIHookAdapter.PropertyList type = ProgressMonitorUtils.parseTypeFrom(evt);
       int progress = ProgressMonitorUtils.parsePercentFrom(evt);
       String msg = ProgressMonitorUtils.parseMessageFrom(evt);
 
       progressBar.setValue(progress);
       progressBar.setString(type.toString());
 
       sout(msg);
     }
   };
 
   hook.addRecieveStatusListener(listener);
   hook.addSendStatusListener(listener);
   hook.addUnderlyingIOStreamInterruptedOrClosed(new PropertyChangeListener() {
     public void propertyChange(PropertyChangeEvent evt) {
       sout(evt.getPropertyName() + " fired!!!");
     }
   });
 
   return hook;
 }
 
 private void _displayImgInFrame() {
 
   final JFrame frame = new JFrame("Google Static Map");
   GUIUtils.setAppIcon(frame, "71.png");
   frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
   JLabel imgLbl = new JLabel(new ImageIcon(_img));
   imgLbl.setToolTipText(MessageFormat.format("<html>Image downloaded from URI<br>size: w={0}, h={1}</html>",
                                              _img.getWidth(), _img.getHeight()));
   imgLbl.addMouseListener(new MouseListener() {
     public void mouseClicked(MouseEvent e) {}
     public void mousePressed(MouseEvent e) { frame.dispose();}
     public void mouseReleased(MouseEvent e) { }
     public void mouseEntered(MouseEvent e) { }
     public void mouseExited(MouseEvent e) { }
   });
 
   frame.setContentPane(imgLbl);
   frame.pack();
 
   GUIUtils.centerOnScreen(frame);
   frame.setVisible(true);
 }
 
 private void _displayRespStrInFrame() {
 
   final JFrame frame = new JFrame("Google Static Map - Error");
   GUIUtils.setAppIcon(frame, "69.png");
   frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
   JTextArea response = new JTextArea(_respStr, 25, 80);
   response.addMouseListener(new MouseListener() {
     public void mouseClicked(MouseEvent e) {}
     public void mousePressed(MouseEvent e) { frame.dispose();}
     public void mouseReleased(MouseEvent e) { }
     public void mouseEntered(MouseEvent e) { }
     public void mouseExited(MouseEvent e) { }
   });
 
   frame.setContentPane(new JScrollPane(response));
   frame.pack();
 
   GUIUtils.centerOnScreen(frame);
   frame.setVisible(true);
 }
 
 /** simply dump status info to the textarea */
 private void sout(final String s) {
   Runnable soutRunner = new Runnable() {
     public void run() {
       if (ttaStatus.getText().equals("")) {
         ttaStatus.setText(s);
       }
       else {
         ttaStatus.setText(ttaStatus.getText() + "\n" + s);
       }
     }
   };
 
   if (ThreadUtils.isInEDT()) {
     soutRunner.run();
   }
   else {
     SwingUtilities.invokeLater(soutRunner);
   }
 }
 
 private void startTaskAction() {
   try {
     _task.execute();
   }
   catch (TaskException e) {
     sout(e.getMessage());
   }
 }
 
 
 public SampleApp() {
   initComponents();
   doInit();
 }
 
 private void quitProgram() {
   _task.shutdown();
   System.exit(0);
 }
 
 public String[] getLines(String filename) throws IOException {
     
     FileReader fileReader = new FileReader(filename);
     
     BufferedReader bufferedReader = new BufferedReader(fileReader);
     List<String> lines = new ArrayList<String>();
     String line = null;
     while ((line = bufferedReader.readLine()) != null) {
         lines.add(line);
     }
     bufferedReader.close();
     return lines.toArray(new String[lines.size()]);
 }
 
 private void initComponents() {
   // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
   // Generated using JFormDesigner non-commercial license
   dialogPane = new JPanel();
   contentPanel = new JPanel();
   panel1 = new JPanel();
   label2 = new JLabel();
   ttfSizeW = new JTextField();
   label4 = new JLabel();
   ttfLat = new JTextField();
   btnGetMap = new JButton();
   label3 = new JLabel();
   ttfSizeH = new JTextField();
   label5 = new JLabel();
   ttfLon = new JTextField();
   btnQuit = new JButton();
   label1 = new JLabel();
   ttfLicense = new JTextField();
   label6 = new JLabel();
   ttfZoom = new JTextField();
   btnZoomIn = new JButton();
   btnZoomOut = new JButton();
   btnLatDecr = new JButton();
   btnLatIncr = new JButton();
   btnLongDecr = new JButton();
   btnLongIncr = new JButton();
   cityCombo = new JComboBox();
   savedPositionList = new JComboBox();
   btnSavePosition = new JButton();
  txtPositionName = new JTextField(20);
   btnSaveDone = new JButton();
   btnSaveDone = new JButton();
   btnSavePosition = new JButton();
   panelList = new JPanel();
   scrollPane1 = new JScrollPane();
   ttaStatus = new JTextArea();
   panel2 = new JPanel();
   panel3 = new JPanel();
   checkboxRecvStatus = new JCheckBox();
   checkboxSendStatus = new JCheckBox();
   ttfProgressMsg = new JTextField();
   progressBar = new JProgressBar();
   lblProgressStatus = new JLabel();
 
   try
   {
 	  savedPositionList = new JComboBox(getLines("savedpositions.txt"));
   }
   catch (IOException ex)
   {
 	  
   }
   
   //---- errorWindow ----
   errorWindow = new JDialog(this, "Error!", true);
   errorWindow.setSize(new Dimension(150, 70));;
   //Show the dialog in the middle of the screen
   errorWindow.setLocationRelativeTo(null);
   
   //--- popupWindow ----
   popupWindow =  new JDialog(this,"Enter Name For Position",true);
  popupWindow.setSize(new Dimension(400, 100));
   //Show the dialog in the middle of the screen
   popupWindow.setLocationRelativeTo(null);
   
   JPanel tempPanel = new JPanel();
  
   tempPanel.setLayout( new FlowLayout( FlowLayout.LEFT, 5, 20 ) );
   tempPanel.add(new JLabel("Position Name : "));
   tempPanel.add(txtPositionName);
   tempPanel.add(btnSaveDone);
   
   popupWindow.add(tempPanel);
   
   //======== this ========
   setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
   setTitle("Google Static Maps");
   setIconImage(null);
   Container contentPane = getContentPane();
   contentPane.setLayout(new BorderLayout());
 
   //======== dialogPane ========
   {
   	dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
   	dialogPane.setOpaque(false);
   	dialogPane.setLayout(new BorderLayout());
 
   	//======== contentPanel ========
   	{
   		contentPanel.setOpaque(false);
   		contentPanel.setLayout(new TableLayout(new double[][] {
   			{TableLayout.FILL},
   			{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}}));
   		((TableLayout)contentPanel.getLayout()).setHGap(7);
   		((TableLayout)contentPanel.getLayout()).setVGap(7);
 
   		//======== panel1 ========
   		{
   			panel1.setOpaque(false);
   			panel1.setBorder(new CompoundBorder(
   				new TitledBorder("Configure the inputs to Google Static Maps"),
   				Borders.DLU2_BORDER));
   			panel1.setLayout(new TableLayout(new double[][] {
   				{0.17, 0.17, 0.17, 0.17, 0.05, 0.05, 0.2, TableLayout.FILL},
   				{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
   			((TableLayout)panel1.getLayout()).setHGap(7);
   			((TableLayout)panel1.getLayout()).setVGap(7);
 
   			//---- label2 ----
   			label2.setText("Size Width");
   			label2.setHorizontalAlignment(SwingConstants.RIGHT);
   			panel1.add(label2, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- ttfSizeW ----
   			ttfSizeW.setText("512");
   			panel1.add(ttfSizeW, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- label4 ----
   			label4.setText("Latitude");
   			label4.setHorizontalAlignment(SwingConstants.RIGHT);
   			panel1.add(label4, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- ttfLat ----
   			ttfLat.setText("38.931099");
   			panel1.add(ttfLat, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- btnGetMap ----
   			btnGetMap.setText("Get Map");
   			btnGetMap.setHorizontalAlignment(SwingConstants.LEFT);
   			btnGetMap.setMnemonic('G');
   			btnGetMap.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   					startTaskAction();
   				}
   			});
   			panel1.add(btnGetMap, new TableLayoutConstraints(6, 0, 6, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- label3 ----
   			label3.setText("Size Height");
   			label3.setHorizontalAlignment(SwingConstants.RIGHT);
   			panel1.add(label3, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- ttfSizeH ----
   			ttfSizeH.setText("512");
   			panel1.add(ttfSizeH, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- label5 ----
   			label5.setText("Longitude");
   			label5.setHorizontalAlignment(SwingConstants.RIGHT);
   			panel1.add(label5, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- ttfLon ----
   			ttfLon.setText("-77.3489");
   			panel1.add(ttfLon, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- btnQuit ----
   			btnQuit.setText("Quit");
   			btnQuit.setMnemonic('Q');
   			btnQuit.setHorizontalAlignment(SwingConstants.LEFT);
   			btnQuit.setHorizontalTextPosition(SwingConstants.RIGHT);
   			btnQuit.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   					quitProgram();
   				}
   			});
   			panel1.add(btnQuit, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- label1 ----
   			label1.setText("License Key");
   			label1.setHorizontalAlignment(SwingConstants.RIGHT);
   			panel1.add(label1, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- ttfLicense ----
   			ttfLicense.setToolTipText("Enter your own URI for a file to download in the background");
   			panel1.add(ttfLicense, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- label6 ----
   			label6.setText("Zoom");
   			label6.setHorizontalAlignment(SwingConstants.RIGHT);
   			panel1.add(label6, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- ttfZoom ----
   			ttfZoom.setText("14");
   			panel1.add(ttfZoom, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
   			ttfZoom.setEditable(false);
   			
   			//---- btnZoomOut ----
   			btnZoomOut.setText("-");
   			btnZoomOut.addActionListener(new ActionListener(){
   				public void actionPerformed(ActionEvent e){
   					int num = Integer.parseInt(ttfZoom.getText());
   					if(num != 0){
   						num--;
   					}
   					ttfZoom.setText(Integer.toString(num));
   				}
   			});
   			panel1.add(btnZoomOut, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
   			
   			//---- btnZoomIn ----
   			btnZoomIn.setText("+");
   			btnZoomIn.addActionListener(new ActionListener(){
   				public void actionPerformed(ActionEvent e){
   					int num = Integer.parseInt(ttfZoom.getText());
   					if(num != 19){
   						num++;
   					}
   					ttfZoom.setText(Integer.toString(num));
   				}
   			});
   			panel1.add(btnZoomIn, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
   			
   			//---- btnLatDecr ----
   		    btnLatDecr.setText("-");
   		    btnLatDecr.addActionListener(new ActionListener() {
 			    public void actionPerformed(ActionEvent e)
 			    {
 				    BigDecimal temp = new BigDecimal( new Double(ttfLat.getText()));
 				    if(temp.doubleValue() >= -88.9999999999999)
 				    {
 					    temp = new BigDecimal(temp.doubleValue() - 1);
 					    temp = temp.setScale(6, RoundingMode.HALF_UP);
 					    ttfLat.setText(Double.toString(temp.doubleValue()));
 				    }
 			  	 }
 		    });
   		    panel1.add(btnLatDecr, new TableLayoutConstraints(4, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
   		    
   		    //---- btnLatIncr ----
   		    btnLatIncr.setText("+");
   		    btnLatIncr.addActionListener(new ActionListener() {
 			    public void actionPerformed(ActionEvent e)
 			    {
 				    BigDecimal temp = new BigDecimal( new Double(ttfLat.getText()));
 				    if(temp.doubleValue() <= 89)
 				    {
 					    temp = new BigDecimal(temp.doubleValue() + 1);
 					    temp = temp.setScale(6, RoundingMode.HALF_UP);
 					    ttfLat.setText(Double.toString(temp.doubleValue()));
 				    }
 			    }
 		    });
   		    panel1.add(btnLatIncr, new TableLayoutConstraints(5, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
   		    
   		    //---- btnLongDecr ----
   		    btnLongDecr.setText("-");;
   		    btnLongDecr.addActionListener(new ActionListener() {
 			    public void actionPerformed(ActionEvent e)
 			    {
 				    BigDecimal temp = new BigDecimal( new Double(ttfLon.getText()));
 				    if(temp.doubleValue() >= -179)
 				    {
 					    temp = new BigDecimal(temp.doubleValue() - 1);
 					    temp = temp.setScale(6, RoundingMode.HALF_UP);
 					    ttfLon.setText(Double.toString(temp.doubleValue()));
 				    }
 			    }
 		    });	  
   		    panel1.add(btnLongDecr, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
   		    
   		    //---- btnLongIncr ----
   		    btnLongIncr.setText("+");
   		    btnLongIncr.addActionListener(new ActionListener() {
 			    public void actionPerformed(ActionEvent e)
 			    {
 				    BigDecimal temp = new BigDecimal( new Double(ttfLon.getText()));
 				    if(temp.doubleValue() <= 179)
 				    {
 				 	    temp = new BigDecimal(temp.doubleValue() + 1);
 					    temp = temp.setScale(6, RoundingMode.HALF_UP);
 					    ttfLon.setText(Double.toString(temp.doubleValue()));
 				    }
 			    }
 		    });
   		    panel1.add(btnLongIncr, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));  	
   		    
   		    //---- btnSavePosition ----
   		    btnSavePosition.setText("Save Position");            
   		    btnSavePosition.addActionListener(new ActionListener() {
   			    public void actionPerformed( ActionEvent e )
   			    {
   				    // set popup window visibility
   				    if (!popupWindow.isVisible()) {
   					    // show the popup if not visible
   					    popupWindow.setVisible(true);
   					    popupWindow.requestFocus();
   				    } 
   				    else {
   					    popupWindow.setVisible(false);
   				    }
   			    }
   		    });
   		    panel1.add(btnSavePosition, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));  
   		}
   		contentPanel.add(panel1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   		//======== List of cities and bookmarks =======
   		{
   			panelList.setOpaque(false);
   			panelList.setBorder(new CompoundBorder(
   				new TitledBorder("Bookmarks and Cities"),
   				Borders.DLU2_BORDER));
   			panelList.setLayout(new TableLayout(new double[][] {
   				{0.5, 0.5, TableLayout.FILL},
   				{TableLayout.PREFERRED, TableLayout.PREFERRED}}));
   			((TableLayout)panelList.getLayout()).setHGap(2);
   			((TableLayout)panelList.getLayout()).setVGap(2);
   			
   		    //---- btnSaveDone ----
   		    btnSaveDone.setText("Done");
   		    btnSaveDone.addActionListener(new ActionListener() {
   			    public void actionPerformed(ActionEvent e)
   			    {
   				    String positionName = txtPositionName.getText();
   				    if(positionName.length() == 0)
   				    {
   					    JPanel pn = new JPanel();
   					    pn.add(new JLabel("Enter position name!!"));
   					    errorWindow.add(pn);
   					    errorWindow.setVisible(true);
   				    }
   				    else
   				    {
   					    try
   					    {
   						    String [] latlng = {ttfLat.getText(),ttfLon.getText()};
   						    FileWriter fstream = new FileWriter("savedpositions.txt",true);
   						    BufferedWriter out = new BufferedWriter(fstream);
  						    out.write(positionName + ": " + latlng[0] + ", " + latlng[1] + "\n");
  						    savedPositionList.addItem(new String(positionName + ": " + latlng[0] + ", " + latlng[1]));
   						    //Close the output stream
   						    out.close();
  						    txtPositionName.setText("");
   						    popupWindow.setVisible(false);
   					    }
   					    catch(Exception ex)
   					    {
   						   
   					    }
   				    }
   			    }
   		    });
 	          
   		    //---- savedPositionList ----
   		    savedPositionList.addItemListener( new ItemListener() {
   			    public void itemStateChanged(ItemEvent e) // triggered by a selection
   			    {
   				    if (e.getStateChange() == ItemEvent.SELECTED && savedPositionList.getSelectedIndex() != 0 ) // index 0: not a city name
   				    {
   					    String item = (String)savedPositionList.getSelectedItem();
  					    item = item.substring(item.indexOf(": ") + 2);
   					    String [] latlng = item.split(", ");
   					    ttfLat.setText(latlng[0]);
   					    ttfLon.setText(latlng[1]);
   				    }
   			    }
   		    });
   		    savedPositionList.insertItemAt( "Select Geo Position", 0 ); // not a city name
   		    savedPositionList.setSelectedIndex(0);
   		    panelList.add(savedPositionList, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));  
   		    
   		    //---- cityCombo ----
   		    String [] cityList = {"Select City..", "Tirana", "Andorra la Vella", "Vienna", "Minsk", "Bruxelles ", "Sarajevo ", "Sofia", "Zagreb", "Praha", "Kobenhavn", "Tallinn", "Helsinki", "Paris", "Berlin", "Athinai", "Budapest", "Reykjavik", "Dublin", "Rome", "Riga", "Vaduz", "Vilnius", "Luxembourg", "Skopje", "Valletta", "Chisinau", "Monaco", "Podgorica", "Amsterdam", "Oslo", "Warsaw", "Lisbon", "Bucharest", "Moscow", "San Marino", "Belgrade", "Bratislava", "Ljubljana",
   				"Madrid", "Stockholm", "Bern", "Kiev", "London", "Torshavn (on Streymoy)", "Gibraltar", "Saint Peter Port", "Douglas", "Saint Helier", "Prishtine", "Longyearbyen", "Kabul", "Yerevan", "Baku", "Manama", "Dhaka", "Thimphu", "Bandar Seri Begawan", "Phnom Penh", "Beijing", "Nicosia", "T'bilisi", "New Delhi", "Jakarta (on Java)", "Teheran", "Baghdad", "Jerusalem", "Tokyo",
   				"Amman", "Astana", "Kuwait", "Bishkek", "VVientiane", "Beirut", "Kuala Lumpur", "Male (on Male)", "Ulan Bator", "Pyinmana", "Kathmandu", "P'yongyang", "Muscat", "Islamabad", "Manila", "Ad Doha",
   				"Riyadh", "Singapore", "Seoul", "Colombo", "Damascus", "Dushanbe", "Bangkok", "Dili", "Ankara", "Ashgabat", "Abu Dhabi", "TTashkent", "Hanoi", "Sanaa", "Taipei", "Saint John's (on Antigua)", "Buenos Aires", "Nassau (on New Providence)", "Bridgetown", "Belmopan", "Sucre", "Brasilia", "Ottawa", "Santiago", "Bogota", "San Jose", "Havana", "Roseau", "Santo Domingo", "Quito", "San Salvador", "Saint George's", "Guatemala", "Georgetown", "Port-au-Prince",
   				"Tegucigalpa", "Kingston", "Ciudad de Mexico", "Managua", "Panama", "Asuncion", "Lima", "Basseterre (on St. Kitts)", "Castries", "Kingstown (on St. Vincent)", "Paramaribo", "Port of Spain (on Trinidad)",
   				"Washington", "Montevideo", "Caracas", "The Valley", "Oranjestad", "Hamilton (on Main Island)", "Road Town (on Tortola)", "George Town (on Grand Cayman)", "Stanley (on East Falkland)", "Cayenne", "Nuuk", "Basse-Terre", "Fort-de-France", "Plymouth", "Willemstad (on Curacao)", "San Juan", "Saint-Pierre (on St. Pierre)", "Cockburn Town (on Grand Turk)", "Charlotte Amalie (on St. Thomas)", "Algiers", "Luanda", "Porto-Novo", "Gaborone", "Ouagadougou", "Bujumbura",
   				"Yaounde", "Praia (on Sao Tiago)", "Bangui", "N'djamena", "Moroni (on Njazidja)", "Brazzaville", "Kinshasa", "Yamoussoukro", "Djibouti", "Cairo", "Malabo (on Bioko)", "Asmara", "Addis Abeba", "Libreville", "Banjul", "Accra", "Conakry", "Bissau", "Nairobi", "Maseru", "Monrovia", "Tripoli", "Antananarivo", "Lilongwe", "Bamako", "Nouakchott", "Port Louis", "Rabat", "Maputo", "Windhoek", "Niamey", "Abuja", "Kigali", "Sao Tome (on Sao Tome)", "Dakar", "Victoria (on Mahe)", "Freetown", "Mogadishu", "Pretoria", "Khartoum", "Juba", "Mbabane", "Dodoma", "Lome", "Tunis", "Kampala", "Lusaka", "Harare", "Mamoudzou",
   				"Saint Denis", "Jamestown", "El Aaiun",
   				"Canberra", "Suva (on Viti Levu)", "Bairiki (on Tarawa)", "Dalap-Uliga-Darrit (on Majuro)", "Palikir (on Pohnpei)", "Yaren", "Wellington", "Melekeok (on Babelthuap)", "Port Moresby", "Apia (on Upolu)", "Honiara (on Guadalcanal)", "Nuku'alofa (on Tongatapu)", "Vaiaku (on Funafuti)", "Port Vila (on Efate)", "Pago Pago (on Tutuila)", "Flying Fish Cove", "West Island", "Avarua (on Rarotonga)", "Papeete (on Tahiti)", "Hagatna", "Noumea (on Grande Terre)", "Alofi", "Kingston", "Garapan (on Saipan)", "Adamstown (on Pitcairn)", "Mata-Utu (on Wallis)", "Montgomery", "Juneau", "Phoenix", "Little Rock", "Sacramento", "Denver", "Hartford", "Dover", "Tallahassee", "Atlanta", "Honolulu (on Oahu)", "Boise", "Springfield",
   				"Indianapolis", "Des Moines", "Topeka", "Frankfort", "Baton Rouge", "Augusta", "Annapolis", "Boston", "Lansing", "Saint Paul", "Jackson", "Jefferson City", "Helena", "Lincoln", "Carson City", "Concord", "Trenton", "Santa Fe", "Albany", "Raleigh", "Bismarck", "Columbus", "Oklahoma City", "Salem", "Harrisburg", "Providence", "Columbia", "Pierre", "Nashville", "Austin", "Salt Lake City", "Montpelier", "Richmond", "Olympia", "Charleston", "Madison", "Cheyenne"
 
 
   				};
   			java.util.Arrays.sort(cityList);
 
   			cityDetails.put("Select City..", "0:37.0");
   			cityDetails.put("Tirana","41.3317:19.8172");
   			cityDetails.put("Andorra la Vella","42.5075:1.5218");
   			cityDetails.put("Vienna","48.2092:16.3728");
   			cityDetails.put("Minsk","53.9678:27.5766");
   			cityDetails.put("Brussels","50.8371:4.3676");
   			cityDetails.put("Sarajevo","43.8608:18.4214");
   			cityDetails.put("Sofia","42.7105:23.3238");
   			cityDetails.put("Zagreb","45.8150:15.9785");
   			cityDetails.put("Prague","50.0878:14.4205");
   			cityDetails.put("Copenhagen","55.6763:12.5681");
   			cityDetails.put("Tallinn","59.4389:24.7545");
   			cityDetails.put("Helsinki","60.1699:24.9384");
   			cityDetails.put("Paris","48.8567:2.3510");
   			cityDetails.put("Berlin","52.5235:13.4115");
   			cityDetails.put("Athens","37.9792:23.7166");
   			cityDetails.put("Budapest","47.4984:19.0408");
   			cityDetails.put("Reykjavik","64.1353:-21.8952");
   			cityDetails.put("Dublin","53.3441:-6.2675");
   			cityDetails.put("Rome","41.8955:12.4823");
   			cityDetails.put("Riga","56.9465:24.1049");
   			cityDetails.put("Vaduz","47.1411:9.5215");
   			cityDetails.put("Vilnius","54.6896:25.2799");
   			cityDetails.put("Luxembourg","49.6100:6.1296");
   			cityDetails.put("Skopje","42.0024:21.4361");
   			cityDetails.put("Valletta","35.9042:14.5189");
   			cityDetails.put("Chisinau","47.0167:28.8497");
   			cityDetails.put("Monaco","43.7325:7.4189");
   			cityDetails.put("Podgorica","42.4602:19.2595");
   			cityDetails.put("Amsterdam","52.3738:4.8910");
   			cityDetails.put("Oslo","59.9138:10.7387");
   			cityDetails.put("Warsaw","52.2297:21.0122");
   			cityDetails.put("Lisbon","38.7072:-9.1355");
   			cityDetails.put("Bucharest","44.4479:26.0979");
   			cityDetails.put("Moscow","55.7558:37.6176");
   			cityDetails.put("San Marino","43.9424:12.4578");
   			cityDetails.put("Belgrade","44.8048:20.4781");
   			cityDetails.put("Bratislava","48.2116:17.1547");
   			cityDetails.put("Ljubljana","46.0514:14.5060");
   			cityDetails.put("Madrid","40.4167:-3.7033");
   			cityDetails.put("Stockholm","59.3328:18.0645");
   			cityDetails.put("Bern","46.9480:7.4481");
   			cityDetails.put("Kiev","50.4422:30.5367");
   			cityDetails.put("London","51.5002:-0.1262");
   			cityDetails.put("Torshavn ","62.0177:-6.7719");
   			cityDetails.put("Gibraltar","36.1377:-5.3453");
   			cityDetails.put("Saint Peter Port","49.4660:-2.5522");
   			cityDetails.put("Douglas","54.1670:-4.4821");
   			cityDetails.put("Saint Helier","49.1919:-2.1071");
   			cityDetails.put("Prishtine","42.6740:21.1788");
   			cityDetails.put("Longyearbyen","78.2186:15.6488");
   			cityDetails.put("Kabul","34.5155:69.1952");
   			cityDetails.put("Yerevan","40.1596:44.5090");
   			cityDetails.put("Baku","40.3834:49.8932");
   			cityDetails.put("Manama","26.1921:50.5354");
   			cityDetails.put("Dhaka","23.7106:90.3978");
   			cityDetails.put("Thimphu","27.4405:89.6730");
   			cityDetails.put("Bandar Seri Begawan","4.9431:114.9425");
   			cityDetails.put("Phnom Penh","11.5434:104.8984");
   			cityDetails.put("Beijing","39.9056:116.3958");
   			cityDetails.put("Nicosia","35.1676:33.3736");
   			cityDetails.put("T'bilisi","41.7010:44.7930");
   			cityDetails.put("New Delhi","28.6353:77.2250");
   			cityDetails.put("Jakarta","-6.1862:106.8063");
   			cityDetails.put("Teheran","35.7061:51.4358");
   			cityDetails.put("Baghdad","33.3157:44.3922");
   			cityDetails.put("Jerusalem","31.7857:35.2007");
   			cityDetails.put("Tokyo","35.6785:139.6823");
   			cityDetails.put("Amman","31.9394:35.9349");
   			cityDetails.put("Astana","51.1796:71.4475");
   			cityDetails.put("Kuwait","29.3721:47.9824");
   			cityDetails.put("Bishkek","42.8679:74.5984");
   			cityDetails.put("Vientiane","17.9689:102.6137");
   			cityDetails.put("Beirut","33.8872:35.5134");
   			cityDetails.put("Kuala Lumpur","3.1502:101.7077");
   			cityDetails.put("Male","4.1742:73.5109");
   			cityDetails.put("Ulan Bator","47.9138:106.9220");
   			cityDetails.put("Pyinmana","19.7378:96.2083");
   			cityDetails.put("Kathmandu","27.7058:85.3157");
   			cityDetails.put("P'yongyang","39.0187:125.7468");
   			cityDetails.put("Muscat","23.6086:58.5922");
   			cityDetails.put("Islamabad","33.6751:73.0946");
   			cityDetails.put("Manila","14.5790:120.9726");
   			cityDetails.put("Ad Dawhah","25.2948:51.5082");
   			cityDetails.put("Riyadh","24.6748:46.6977");
   			cityDetails.put("Singapore","1.2894:103.8500");
   			cityDetails.put("Seoul","37.5139:126.9828");
   			cityDetails.put("Colombo","6.9155:79.8572");
   			cityDetails.put("Damascus","33.5158:36.2939");
   			cityDetails.put("Dushanbe","38.5737:68.7738");
   			cityDetails.put("Bangkok","13.7573:100.5020");
   			cityDetails.put("Dili","-8.5662:125.5880");
   			cityDetails.put("Ankara","39.9439:32.8560");
   			cityDetails.put("Ashgabat","37.9509:58.3794");
   			cityDetails.put("Abu Dhabi","24.4764:54.3705");
   			cityDetails.put("Tashkent","41.3193:69.2481");
   			cityDetails.put("Ha Noi","21.0341:105.8372");
   			cityDetails.put("Sanaa","15.3556:44.2081");
   			cityDetails.put("Taipei","25.0338:121.5645");
   			cityDetails.put("Saint John's (on Antigua)","17.1175:-61.8456");
   			cityDetails.put("Buenos Aires","-34.6118:-58.4173");
   			cityDetails.put("Nassau (on New Providence)","25.0661:-77.3390");
   			cityDetails.put("Bridgetown","13.0935:-59.6105");
   			cityDetails.put("Belmopan","17.2534:-88.7713");
   			cityDetails.put("Sucre","-19.0421:-65.2559");
   			cityDetails.put("Brasilia","-15.7801:-47.9292");
   			cityDetails.put("Ottawa","45.4235:-75.6979");
   			cityDetails.put("Santiago","-33.4691:-70.6420");
   			cityDetails.put("Bogota","4.6473:-74.0962");
   			cityDetails.put("San Jose","9.9402:-84.1002");
   			cityDetails.put("Havana","23.1333:-82.3667");
   			cityDetails.put("Roseau","15.2976:-61.3900");
   			cityDetails.put("Santo Domingo","18.4790:-69.8908");
   			cityDetails.put("Quito","-0.2295:-78.5243");
   			cityDetails.put("San Salvador","13.7034:-89.2073");
   			cityDetails.put("Saint George's","12.0540:-61.7486");
   			cityDetails.put("Guatemala","14.6248:-90.5328");
   			cityDetails.put("Georgetown","6.8046:-58.1548");
   			cityDetails.put("Port-au-Prince","18.5392:-72.3288");
   			cityDetails.put("Tegucigalpa","14.0821:-87.2063");
   			cityDetails.put("Kingston","17.9927:-76.7920");
   			cityDetails.put("Ciudad de Mexico","19.4271:-99.1276");
   			cityDetails.put("Managua","12.1475:-86.2734");
   			cityDetails.put("Panama","8.9943:-79.5188");
   			cityDetails.put("Asuncion","-25.3005:-57.6362");
   			cityDetails.put("Lima","-12.0931:-77.0465");
   			cityDetails.put("Basseterre (on St. Kitts)","17.2968:-62.7138");
   			cityDetails.put("Castries","13.9972:-60.0018");
   			cityDetails.put("Kingstown (on St. Vincent)","13.2035:-61.2653");
   			cityDetails.put("Paramaribo","5.8232:-55.1679");
   			cityDetails.put("Port of Spain (on Trinidad)","10.6596:-61.4789");
   			cityDetails.put("Washington","38.8921:-77.0241");
   			cityDetails.put("Montevideo","-34.8941:-56.0675");
   			cityDetails.put("Caracas","10.4961:-66.8983");
   			cityDetails.put("The Valley","18.2249:-63.0669");
   			cityDetails.put("Oranjestad","12.5246:-70.0265");
   			cityDetails.put("Hamilton (on Main Island)","32.2930:-64.7820");
   			cityDetails.put("Road Town (on Tortola)","18.4328:-64.6235");
   			cityDetails.put("George Town (on Grand Cayman)","19.3022:-81.3857");
   			cityDetails.put("Stanley (on East Falkland)","-51.7010:-57.8492");
   			cityDetails.put("Cayenne","4.9346:-52.3303");
   			cityDetails.put("Nuuk","64.1836:-51.7214");
   			cityDetails.put("Basse-Terre","15.9985:-61.7220");
   			cityDetails.put("Fort-de-France","14.5997:-61.0760");
   			cityDetails.put("Plymouth","16.6802:-62.2014");
   			cityDetails.put("Willemstad (on Curacao)","12.1034:-68.9335");
   			cityDetails.put("San Juan","18.4500:-66.0667");
   			cityDetails.put("Saint-Pierre (on St. Pierre)","46.7878:-56.1968");
   			cityDetails.put("Cockburn Town (on Grand Turk)","21.4608:-71.1363");
   			cityDetails.put("Charlotte Amalie (on St. Thomas)","18.3405:-64.9326");
   			cityDetails.put("Algiers","36.7755:3.0597");
   			cityDetails.put("Luanda","-8.8159:13.2306");
   			cityDetails.put("Porto-Novo","6.4779:2.6323");
   			cityDetails.put("Gaborone","-24.6570:25.9089");
   			cityDetails.put("Ouagadougou","12.3569:-1.5352");
   			cityDetails.put("Bujumbura","-3.3818:29.3622");
   			cityDetails.put("Yaounde","3.8612:11.5217");
   			cityDetails.put("Praia (on Sao Tiago)","14.9195:-23.5153");
   			cityDetails.put("Bangui","4.3621:18.5873");
   			cityDetails.put("N'djamena","12.1121:15.0355");
   			cityDetails.put("Moroni (on Njazidja)","-11.7004:43.2412");
   			cityDetails.put("Brazzaville","-4.2767:15.2662");
   			cityDetails.put("Kinshasa","-4.3369:15.3271");
   			cityDetails.put("Yamoussoukro","6.8067:-5.2728");
   			cityDetails.put("Djibouti","11.5806:43.1425");
   			cityDetails.put("Cairo","30.0571:31.2272");
   			cityDetails.put("Malabo (on Bioko)","3.7523:8.7741");
   			cityDetails.put("Asmara","15.3315:38.9183");
   			cityDetails.put("Addis Abeba","9.0084:38.7575");
   			cityDetails.put("Libreville","0.3858:9.4496");
   			cityDetails.put("Banjul","13.4399:-16.6775");
   			cityDetails.put("Accra","5.5401:-0.2074");
   			cityDetails.put("Conakry","9.5370:-13.6785");
   			cityDetails.put("Bissau","11.8598:-15.5875");
   			cityDetails.put("Nairobi","-1.2762:36.7965");
   			cityDetails.put("Maseru","-29.2976:27.4854");
   			cityDetails.put("Monrovia","6.3106:-10.8047");
   			cityDetails.put("Tripoli","32.8830:13.1897");
   			cityDetails.put("Antananarivo","-18.9201:47.5237");
   			cityDetails.put("Lilongwe","-13.9899:33.7703");
   			cityDetails.put("Bamako","12.6530:-7.9864");
   			cityDetails.put("Nouakchott","18.0669:-15.9900");
   			cityDetails.put("Port Louis","-20.1654:57.4896");
   			cityDetails.put("Rabat","33.9905:-6.8704");
   			cityDetails.put("Maputo","-25.9686:32.5804");
   			cityDetails.put("Windhoek","-22.5749:17.0805");
   			cityDetails.put("Niamey","13.5164:2.1157");
   			cityDetails.put("Abuja","9.0580:7.4891");
   			cityDetails.put("Kigali","-1.9441:30.0619");
   			cityDetails.put("Sao Tome (on Sao Tome)","0.3360:6.7311");
   			cityDetails.put("Dakar","14.6953:-17.4439");
   			cityDetails.put("Victoria (on Mahe)","-4.6167:55.4500");
   			cityDetails.put("Freetown","8.4697:-13.2659");
   			cityDetails.put("Mogadishu","2.0411:45.3426");
   			cityDetails.put("Pretoria","-25.7463:28.1876");
   			cityDetails.put("Khartoum","15.5501:32.5322");
   			cityDetails.put("Juba","4.8496:31.6046");
   			cityDetails.put("Mbabane","-26.3186:31.1410");
   			cityDetails.put("Dodoma","-6.1670:35.7497");
   			cityDetails.put("Lome","6.1228:1.2255");
   			cityDetails.put("Tunis","36.8117:10.1761");
   			cityDetails.put("Kampala","0.3133:32.5714");
   			cityDetails.put("Lusaka","-15.4145:28.2809");
   			cityDetails.put("Harare","-17.8227:31.0496");
   			cityDetails.put("Mamoudzou","-12.7806:45.2278");
   			cityDetails.put("Saint Denis","-20.8732:55.4603");
   			cityDetails.put("Jamestown","-15.9244:-5.7181");
   			cityDetails.put("El Aaiun","27.1536:-13.2033");
   			cityDetails.put("Canberra","-35.2820:149.1286");
   			cityDetails.put("Suva (on Viti Levu)","-18.1416:178.4419");
   			cityDetails.put("Bairiki (on Tarawa)","1.3282:172.9784");
   			cityDetails.put("Dalap-Uliga-Darrit (on Majuro)","7.1167:171.3667");
   			cityDetails.put("Palikir (on Pohnpei)","6.9177:158.1854");
   			cityDetails.put("Yaren","-0.5434:166.9196");
   			cityDetails.put("Wellington","-41.2865:174.7762");
   			cityDetails.put("Melekeok (on Babelthuap)","7.5007:134.6241");
   			cityDetails.put("Port Moresby","-9.4656:147.1969");
   			cityDetails.put("Apia (on Upolu)","-13.8314:-171.7518");
   			cityDetails.put("Honiara (on Guadalcanal)","-9.4333:159.9500");
   			cityDetails.put("Nuku'alofa (on Tongatapu)","-21.1360:-175.2164");
   			cityDetails.put("Vaiaku (on Funafuti)","-8.5210:179.1983");
   			cityDetails.put("Port Vila (on Efate)","-17.7404:168.3210");
   			cityDetails.put("Pago Pago (on Tutuila)","-14.2793:-170.7009");
   			cityDetails.put("Flying Fish Cove","-10.4286:105.6807");
   			cityDetails.put("West Island","-12.1869:96.8283");
   			cityDetails.put("Avarua (on Rarotonga)","-21.2039:-159.7658");
   			cityDetails.put("Papeete (on Tahiti)","-17.5350:-149.5696");
   			cityDetails.put("Hagatna","13.4667:144.7470");
   			cityDetails.put("Noumea (on Grande Terre)","-22.2758:166.4581");
   			cityDetails.put("Alofi","-19.0565:-169.9237");
   			cityDetails.put("Kingston","-29.0545:167.9666");
   			cityDetails.put("Garapan (on Saipan)","15.2069:145.7197");
   			cityDetails.put("Adamstown (on Pitcairn)","-25.0662:-130.1027");
   			cityDetails.put("Mata-Utu (on Wallis)","-13.2784:-176.1430");
   			cityDetails.put("Montgomery","32.3754:-86.2996");
   			cityDetails.put("Juneau","58.3637:-134.5721");
   			cityDetails.put("Phoenix","33.4483:-112.0738");
   			cityDetails.put("Little Rock","34.7244:-92.2789");
   			cityDetails.put("Sacramento","38.5737:-121.4871");
   			cityDetails.put("Denver","39.7551:-104.9881");
   			cityDetails.put("Hartford","41.7665:-72.6732");
   			cityDetails.put("Dover","39.1615:-75.5136");
   			cityDetails.put("Tallahassee","30.4382:-84.2806");
   			cityDetails.put("Atlanta","33.7545:-84.3897");
   			cityDetails.put("Honolulu (on Oahu)","21.2920:-157.8219");
   			cityDetails.put("Boise","43.6021:-116.2125");
   			cityDetails.put("Springfield","39.8018:-89.6533");
   			cityDetails.put("Indianapolis","39.7670:-86.1563");
   			cityDetails.put("Des Moines","41.5888:-93.6203");
   			cityDetails.put("Topeka","39.0474:-95.6815");
   			cityDetails.put("Frankfort","38.1894:-84.8715");
   			cityDetails.put("Baton Rouge","30.4493:-91.1882");
   			cityDetails.put("Augusta","44.3294:-69.7323");
   			cityDetails.put("Annapolis","38.9693:-76.5197");
   			cityDetails.put("Boston","42.3589:-71.0568");
   			cityDetails.put("Lansing","42.7336:-84.5466");
   			cityDetails.put("Saint Paul","44.9446:-93.1027");
   			cityDetails.put("Jackson","32.3122:-90.1780");
   			cityDetails.put("Jefferson City","38.5698:-92.1941");
   			cityDetails.put("Helena","46.5911:-112.0205");
   			cityDetails.put("Lincoln","40.8136:-96.7026");
   			cityDetails.put("Carson City","39.1501:-119.7519");
   			cityDetails.put("Concord","43.2314:-71.5597");
   			cityDetails.put("Trenton","40.2202:-74.7642");
   			cityDetails.put("Santa Fe","35.6816:-105.9381");
   			cityDetails.put("Albany","42.6517:-73.7551");
   			cityDetails.put("Raleigh","35.7797:-78.6434");
   			cityDetails.put("Bismarck","46.8084:-100.7694");
   			cityDetails.put("Columbus","39.9622:-83.0007");
   			cityDetails.put("Oklahoma City","35.4931:-97.4591");
   			cityDetails.put("Salem","44.9370:-123.0272");
   			cityDetails.put("Harrisburg","40.2740:-76.8849");
   			cityDetails.put("Providence","41.8270:-71.4087");
   			cityDetails.put("Columbia","34.0007:-81.0353");
   			cityDetails.put("Pierre","44.3776:-100.3177");
   			cityDetails.put("Nashville","36.1589:-86.7821");
   			cityDetails.put("Austin","30.2687:-97.7452");
   			cityDetails.put("Salt Lake City","40.7716:-111.8882");
   			cityDetails.put("Montpelier","44.2627:-72.5716");
   			cityDetails.put("Richmond","37.5408:-77.4339");
   			cityDetails.put("Olympia","47.0449:-122.9016");
   			cityDetails.put("Charleston","38.3533:-81.6354");
   			cityDetails.put("Madison","43.0632:-89.4007");
   			cityDetails.put("Cheyenne","41.1389:-104.8165");
 			cityCombo = new JComboBox(cityList);
 
   			cityCombo.addActionListener(new ActionListener(){
   				public void actionPerformed(ActionEvent e){
   					String details = cityDetails.get(cityCombo.getSelectedItem());
   					StringTokenizer st = new StringTokenizer(details, ":");
   					ttfLat.setText(st.nextToken());
   					ttfLon.setText(st.nextToken());
   				}
   			});
   			panelList.add(cityCombo, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));  
   		}
   		contentPanel.add(panelList, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
   		
   		//======== scrollPane1 ========
   		{
   			scrollPane1.setBorder(new TitledBorder("System.out - displays all status and progress messages, etc."));
   			scrollPane1.setOpaque(false);
 
   			//---- ttaStatus ----
   			ttaStatus.setBorder(Borders.createEmptyBorder("1dlu, 1dlu, 1dlu, 1dlu"));
   			ttaStatus.setToolTipText("<html>Task progress updates (messages) are displayed here,<br>along with any other output generated by the Task.<html>");
   			scrollPane1.setViewportView(ttaStatus);
   		}
   		contentPanel.add(scrollPane1, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   		//======== panel2 ========
   		{
   			panel2.setOpaque(false);
   			panel2.setBorder(new CompoundBorder(
   				new TitledBorder("Status - control progress reporting"),
   				Borders.DLU2_BORDER));
   			panel2.setLayout(new TableLayout(new double[][] {
   				{0.45, TableLayout.FILL, 0.45},
   				{TableLayout.PREFERRED, TableLayout.PREFERRED}}));
   			((TableLayout)panel2.getLayout()).setHGap(5);
   			((TableLayout)panel2.getLayout()).setVGap(5);
 
   			//======== panel3 ========
   			{
   				panel3.setOpaque(false);
   				panel3.setLayout(new GridLayout(1, 2));
 
   				//---- checkboxRecvStatus ----
   				checkboxRecvStatus.setText("Enable \"Recieve\"");
   				checkboxRecvStatus.setOpaque(false);
   				checkboxRecvStatus.setToolTipText("Task will fire \"send\" status updates");
   				checkboxRecvStatus.setSelected(true);
   				panel3.add(checkboxRecvStatus);
 
   				//---- checkboxSendStatus ----
   				checkboxSendStatus.setText("Enable \"Send\"");
   				checkboxSendStatus.setOpaque(false);
   				checkboxSendStatus.setToolTipText("Task will fire \"recieve\" status updates");
   				panel3.add(checkboxSendStatus);
   			}
   			panel2.add(panel3, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- ttfProgressMsg ----
   			ttfProgressMsg.setText("Loading map from Google Static Maps");
   			ttfProgressMsg.setToolTipText("Set the task progress message here");
   			panel2.add(ttfProgressMsg, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- progressBar ----
   			progressBar.setStringPainted(true);
   			progressBar.setString("progress %");
   			progressBar.setToolTipText("% progress is displayed here");
   			panel2.add(progressBar, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- lblProgressStatus ----
   			lblProgressStatus.setText("task status listener");
   			lblProgressStatus.setHorizontalTextPosition(SwingConstants.LEFT);
   			lblProgressStatus.setHorizontalAlignment(SwingConstants.LEFT);
   			lblProgressStatus.setToolTipText("Task status messages are displayed here when the task runs");
   			panel2.add(lblProgressStatus, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
   		}
   		contentPanel.add(panel2, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
   	}
   	dialogPane.add(contentPanel, BorderLayout.CENTER);
   }
   contentPane.add(dialogPane, BorderLayout.CENTER);
   setSize(725, 575);
   setLocationRelativeTo(null);
   // JFormDesigner - End of component initialization  //GEN-END:initComponents
 }
 
 // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
 // Generated using JFormDesigner non-commercial license
 private JPanel dialogPane;
 private JPanel contentPanel;
 private JPanel panel1;
 private JLabel label2;
 private JTextField ttfSizeW;
 private JLabel label4;
 private JTextField ttfLat;
 private JButton btnGetMap;
 private JLabel label3;
 private JTextField ttfSizeH;
 private JLabel label5;
 private JTextField ttfLon;
 private JButton btnQuit;
 private JLabel label1;
 private JTextField ttfLicense;
 private JLabel label6;
 private JTextField ttfZoom;
 private JButton btnZoomOut;
 private JButton btnZoomIn;
 private JButton btnLatDecr;
 private JButton btnLatIncr;
 private JButton btnLongDecr;
 private JButton btnLongIncr;
 private JComboBox savedPositionList;
 private JButton btnSavePosition;
 private JDialog popupWindow;
 private JDialog errorWindow;
 private JPanel panelList;
 private JComboBox cityCombo;
 private JTextField txtPositionName;
 private JButton btnSaveDone;
 private JScrollPane scrollPane1;
 private JTextArea ttaStatus;
 private JPanel panel2;
 private JPanel panel3;
 private JCheckBox checkboxRecvStatus;
 private JCheckBox checkboxSendStatus;
 private JTextField ttfProgressMsg;
 private JProgressBar progressBar;
 private JLabel lblProgressStatus;
 // JFormDesigner - End of variables declaration  //GEN-END:variables
 }
