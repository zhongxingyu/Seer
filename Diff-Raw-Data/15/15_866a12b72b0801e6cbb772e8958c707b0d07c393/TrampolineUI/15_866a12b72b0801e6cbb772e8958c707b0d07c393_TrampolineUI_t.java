 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package trampoline;
 import javax.swing.*;
 import java.io.*;
 import java.util.*;
 import java.awt.event.*;
 import java.awt.*;
 import java.awt.Image.*;
 import java.awt.geom.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import javax.xml.parsers.*;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.*;
 import javax.xml.transform.stream.*;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.data.category.DefaultCategoryDataset;
 import org.w3c.dom.*;
 
 /**
  *
  * @author Andreas
  */
 public class TrampolineUI extends javax.swing.JFrame {
     
     public ArrayList<PortController> portsAvaliable_;
     public ArrayList<String> portStrings_;
     public ArrayList<Integer> noOfTof_;
     public TofInterface currentInterface_;
     public DBConnect db_;
     
     static SplashScreen mySplash_;
     static Graphics2D splashGraphics;               // graphics context for overlay of the splash image
     static Rectangle2D.Double splashTextArea;       // area where we draw the text
     static Rectangle2D.Double splashProgressArea;   // area where we draw the progress bar
     static Font font;                               // used to draw our text
 	
     javax.swing.Timer jumpTimer;
     javax.swing.Timer pageRefreshTimer_;
     javax.swing.Timer errorTimer_;
     private int refresh;
     private int nextJumpToFill;
     private static int REFRESH_TIME = 30; // Time to keep refreshing for after GO event
     private double[] chartValues;
     private String[] chartNames;
     private Chart chartObject_;
     private Chart chartObjectStats_;
     private JLabel[] labelArray_;
     private JLabel[] beamStatusGreenArray_;
     private JLabel[] beamStatusRedArray_;
     private int currentRoutineId_;      // ID of routine displayed on screen currently;
     private Map<String,ImageIcon> locationImagesSmall_; //Array of location images for labels
     private Map<String,ImageIcon> locationImagesLarge_; //Array of location images for labels
     private String currentLocation_; // String of location currently displayed
     
     private boolean adminAccessGranted_;
     private String adminPassword_;
     private Dimension screenResolution_; //Current screen resolution when program loaded
     private MessageHandler messageHandler_;  // Instance of the project Error Handler.
     private int errorPersist_;          // Length of time to show error for.
     private BounceCounter bounceCounter_;
     
     
    ActionListener pageRefresh = new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
             if(currentInterface_ != null){
                 boolean beamStatus[] = currentInterface_.getBeamStatus();
 
                 for(int i=0;i<8;i++){
                     if(beamStatus[0]==true){
                         beamStatusRedArray_[2*i].setVisible(false);
                         beamStatusRedArray_[2*i+1].setVisible(false);
                         beamStatusGreenArray_[2*i].setVisible(true);
                         beamStatusGreenArray_[2*i+1].setVisible(true);
                     }else{
                         beamStatusRedArray_[2*i].setVisible(true);
                         beamStatusRedArray_[2*i+1].setVisible(true);
                         beamStatusGreenArray_[2*i].setVisible(false);
                         beamStatusGreenArray_[2*i+1].setVisible(false);                        
                     }
                 }
                 
                 String location = currentInterface_.getLastLocation();
                 
                 if(location.equals("")){
                     location = "none";
                 }
                 if(!(location.equals(currentLocation_))){
                     lblTrampoline.setIcon(locationImagesLarge_.get(location));
                 }
             }
         }
     };
    
    ActionListener errorAction = new ActionListener(){
        public void actionPerformed(ActionEvent evt){
             if(messageHandler_.isError()){
                 lblError.setText(messageHandler_.getCurrentErrorShort() + " (Click for more info...)");
                 lblError.setForeground(messageHandler_.getColour());
                 lblError.setCursor(new Cursor(java.awt.Cursor.HAND_CURSOR));
                 if (errorPersist_!=0){
                     if (errorPersist_ ==1){
                         lblError.setText("");
                         messageHandler_.clearError();
                         lblError.setCursor(new Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                     }
                     errorPersist_ --;
                 } else {
                     errorPersist_ = 10000;
                 }
             }
        }
    };
     
     ActionListener jumpAction = new ActionListener(){
         public void actionPerformed(ActionEvent evt){        
             if(refresh>0){
                 if(currentInterface_.getRoutine().getJumps().length >= nextJumpToFill){
                     Jump thisJump = currentInterface_.getRoutine().getJumps()[nextJumpToFill-1];
                     
                     labelArray_[(nextJumpToFill-1)*5].setVisible(true);
                     labelArray_[(nextJumpToFill-1)*5+1].setText(thisJump.getTof()+"");
                     labelArray_[(nextJumpToFill-1)*5+2].setText(thisJump.getTon()+"");
                     labelArray_[(nextJumpToFill-1)*5+3].setText(thisJump.getTotal()+"");
                     labelArray_[(nextJumpToFill-1)*5+4].setIcon(locationImagesSmall_.get(thisJump.getLocation()));
                     
                     // UPDATE BAR GRAPH
                     pnlGraph.removeAll();
                     chartObject_.addValue(thisJump.getTof());
                     JFreeChart jChart = chartObject_.createChart();
                     ChartPanel CP = new ChartPanel(jChart);
                     pnlGraph.add(CP);
                     pnlGraph.validate();
                     
                     nextJumpToFill++;
                     refresh--;
 
                     if(refresh==0){
                         double lowestToF = thisJump.getTof();
                         
                         for(Jump aJump : currentInterface_.getRoutine().getJumps()){
                             if(aJump.getTof()<lowestToF){
                                 lowestToF = aJump.getTof();
                             }
                         }
                         
                         for(int i=0;i<currentInterface_.getRoutine().getNumberOfJumps();i++){
                             if(currentInterface_.getRoutine().getJumps()[i].getTof()==lowestToF){
                                 labelArray_[i*5].setForeground(new java.awt.Color(255, 0, 0));
                                 labelArray_[i*5+1].setForeground(new java.awt.Color(255, 0, 0));
                                 labelArray_[i*5+2].setForeground(new java.awt.Color(255, 0, 0));
                                 labelArray_[i*5+3].setForeground(new java.awt.Color(255, 0, 0));
                             }
                         }
                         
                         btnSaveComments.setVisible(true);
                         btnClearComments.setVisible(true);
                         sclComments.setVisible(true);
                         txtComments.setVisible(true);
                         lblComments.setVisible(true);
                         btnClearData.setVisible(true);
                         jumpTimer.stop();
                         currentRoutineId_ = currentInterface_.getRoutineId();
                     }
                 }
             }
         }
     };
     
     /**
      * Creates new form TrampolineUI
      */
         
     public TrampolineUI() {
         this.messageHandler_ = new MessageHandler();
         
         initComponents();
         this.splashText("Connecting to Database.");
         this.splashProgress(10);
         initDatabase();
         this.splashText("Setting up GUI.");
         this.splashProgress(20);
         initGeneralUI();
         this.splashProgress(30);
         initToFUI();
         this.splashProgress(35);
         initStatisticsUI();
         this.splashProgress(45);
         initImportExportUI();
         this.splashProgress(50);
         initClubManagementUI();
         this.splashText("Setting up Hardware.");
         this.splashProgress(75);
         initHardware();
         this.setVisible(true);
         this.bounceCounter_ = new BounceCounter();
         this.bounceCounter_.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         if (mySplash_.isVisible())   // check if we really had a spash screen
             mySplash_.close();   // we're done with it
     }
     
     private void initHardware(){
         this.splashText("Finding ToF Devices on system.");
         PortController thisPort = new PortController(this.messageHandler_);
         this.portsAvaliable_ = new ArrayList<PortController>();
         this.portStrings_ = thisPort.getPorts();
         this.noOfTof_ = thisPort.getNoTof();
         
         if(portStrings_.size()==0){
             drpDeviceName.addItem("<<No ToF Connected>>");
         }
         
         this.splashText("Connecting to available ToF Devices.");
         this.splashProgress(33);
         for (int i=0; i<portStrings_.size();i++) {
             String s = this.portStrings_.get(i);
             thisPort = new PortController(this.messageHandler_, s);
             this.portsAvaliable_.add(thisPort);
             for(int j=1;j<=this.noOfTof_.get(i);j++){
                 drpDeviceName.addItem(s+" Device "+j);
             }
         }
         
         String currentlySelected = drpDeviceName.getSelectedItem().toString();
         if (currentlySelected.equals("<<No ToF Connected>>")){
             this.currentInterface_ = null;
         }else{
             this.currentInterface_ = this.stringToTof(currentlySelected);
         }
         
         this.splashText("Getting beam status from ToF.");
         this.splashProgress(66);
         pageRefreshTimer_ = new javax.swing.Timer(1, pageRefresh);
         pageRefreshTimer_.start();
         jumpTimer = new javax.swing.Timer(1000, jumpAction);
         errorPersist_ = 0;
         errorTimer_ = new javax.swing.Timer(1, errorAction);
         errorTimer_.start();
     }
     
     private void initToFUI(){
         int screenHeight = this.getMaximizedBounds().height;
         int screenWidth = this.getMaximizedBounds().width;
         int heightTags;  
         Font centralTitleFont;
         Font centralNumberFont;
         
         //Resolution specific layout
         if(screenWidth<1200){
             //Code for screens less than 1200 width (once taken off taskbar) assuming size of 1024 x 768
             pnlStats.setVisible(false);
             pnlGraph.setVisible(false);
 
             GroupLayout pnlToFLayout = (GroupLayout)pnlToF.getLayout(); 
             pnlToFLayout.setHorizontalGroup(
                 pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                 .addGroup(pnlToFLayout.createSequentialGroup()
                     .addContainerGap(5,5)
                     .addGroup(pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                         .addComponent(layBeamStatus, 380, 380, 380)
                         .addComponent(pnlStart, 380, 380, 380))
                     .addGap(15)
                     .addComponent(pnlData, screenWidth - 420, screenWidth - 420, screenWidth - 420)
                     .addContainerGap(5,5))
             );
             
             pnlToFLayout.setVerticalGroup(
                 pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                 .addGroup(pnlToFLayout.createSequentialGroup()
                     .addContainerGap(5,5)
                     .addGroup(pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                         .addGroup(pnlToFLayout.createSequentialGroup()
                             .addComponent(pnlStart,screenHeight-365,screenHeight-365,screenHeight-365)
                             .addGap(15)
                             .addComponent(layBeamStatus, 240, 240, 240))
                         .addComponent(pnlData,screenHeight-110,screenHeight-110,screenHeight-110))
                      .addContainerGap(5,5)
             ));
             
             GroupLayout pnlDataLayout = (GroupLayout)pnlData.getLayout();
             pnlDataLayout.setHorizontalGroup(
                 pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                 .addGroup(pnlDataLayout.createSequentialGroup()
                     .addContainerGap(5,5)
                     .addGroup(pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                         .addGroup(pnlDataLayout.createSequentialGroup()
                             .addGap((screenWidth - 470)/5,(screenWidth - 470)/5,(screenWidth - 470)/5)
                             .addComponent(lblTof,(screenWidth - 470)/5,(screenWidth - 470)/5,(screenWidth - 470)/5)
                             .addComponent(lblTon,(screenWidth - 470)/5,(screenWidth - 470)/5,(screenWidth - 470)/5)
                             .addComponent(lblTotal,(screenWidth - 470)/5,(screenWidth - 470)/5,(screenWidth - 470)/5)
                             .addComponent(lblLocation,(screenWidth - 470)/5,(screenWidth - 470)/5,(screenWidth - 470)/5))
                         .addComponent(pnlDataTable,screenWidth - 470,screenWidth - 470,screenWidth - 470))
                     .addContainerGap(5,5))
                 .addComponent(lblComments)
                 .addComponent(sclComments, screenWidth - 470, screenWidth - 470,screenWidth - 470)
                 .addGroup(pnlDataLayout.createSequentialGroup()
                     .addComponent(btnClearComments,(screenWidth - 470)/2,(screenWidth - 470)/2,(screenWidth - 470)/2)
                     .addComponent(btnSaveComments,(screenWidth - 470)/2,(screenWidth - 470)/2,(screenWidth - 470)/2)) 
             );
 
             pnlDataLayout.setVerticalGroup(
                 pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                 .addGroup(pnlDataLayout.createSequentialGroup()
                     .addContainerGap(5,5)
                     .addGroup(pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                         .addComponent(lblTof,25,25,25)
                         .addComponent(lblTon,25,25,25)
                         .addComponent(lblTotal,25,25,25)
                         .addComponent(lblLocation,25,25,25))
                     .addGap(5,5,5)
                     .addComponent(pnlDataTable, screenHeight-345,screenHeight-345,screenHeight-345)
                     .addGap(5,5,5)
                     .addComponent(lblComments,25,25,25)
                     .addComponent(sclComments,100,100,100)
                     .addGroup(pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                         .addComponent(btnClearComments, 25,25,25)
                         .addGap(5,5,5)
                         .addComponent(btnSaveComments,25,25,25))
                     .addContainerGap(5,5))
             );
             
             heightTags = 75;
             
             //Setup image array
             locationImagesSmall_ = new HashMap<String,ImageIcon>(35);
             locationImagesLarge_ = new HashMap<String,ImageIcon>(35);
             char[] letters = {'A','B','C','D','E','F','G'};
             char[] numbers = {'0','1','2','3','4'};
             int count = 0;
             for(char letter : letters){
                 for(char number: numbers){
                     ImageIcon icon = new ImageIcon(getClass().getResource("/trampoline/images/"+letter+number+".png"));
                     Image img = icon.getImage();
                     java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(60, 35, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                     Graphics g = bi.createGraphics();
                     g.drawImage(img, 0,0, 60, 35, null);
                     locationImagesSmall_.put(""+letter+number, new ImageIcon(bi));
                     locationImagesLarge_.put(""+letter+number, icon);
                     count++;
                 }
             }
 
             ImageIcon icon = new ImageIcon(getClass().getResource("/trampoline/images/eurotramp.png"));
             Image img = icon.getImage();
             java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(60, 35, java.awt.image.BufferedImage.TYPE_INT_ARGB);
             Graphics g = bi.createGraphics();
             g.drawImage(img, 0,0, 60, 35, null);
             locationImagesSmall_.put("none", new ImageIcon(bi));
             locationImagesLarge_.put("none", icon);
             
             centralTitleFont = getFont("centralTitleFontSmall");
             centralNumberFont = getFont("centralNumbersFontSmall");
         }else{
             //Code for screens 1280 X 1024 or larger
             pnlStats.setVisible(true);
             pnlGraph.setVisible(true);
             System.out.println("ujdsnbkjfndkgjndkfjn");
             GroupLayout pnlToFLayout = (GroupLayout)pnlToF.getLayout();  
             pnlToFLayout.setHorizontalGroup(
                 pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                 .addGroup(pnlToFLayout.createSequentialGroup()
                     .addContainerGap(5,5)
                     .addGroup(pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                         .addComponent(layBeamStatus, 380, 380, 380)
                         .addComponent(pnlStart, 380, 380, 380))
                     .addGap(15)
                     .addComponent(pnlData, screenWidth - 800, screenWidth - 800, screenWidth - 800)
                     .addGap(15)
                     .addGroup(pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(pnlStats, 380, 380, 380)
                         .addComponent(pnlGraph, 380, 380, 380))
                     .addContainerGap(5,5))
             );
             
             pnlToFLayout.setVerticalGroup(
                 pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                 .addGroup(pnlToFLayout.createSequentialGroup()
                     .addContainerGap(5,5)
                     .addGroup(pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                         .addGroup(pnlToFLayout.createSequentialGroup()
                             .addComponent(pnlStart,screenHeight-365,screenHeight-365,screenHeight-365)
                             .addGap(15)
                             .addComponent(layBeamStatus, 240, 240, 240))
                         .addComponent(pnlData,screenHeight-110,screenHeight-110,screenHeight-110)
                         .addGroup(pnlToFLayout.createSequentialGroup()
                                 .addComponent(pnlStats,(screenHeight-125)/2,(screenHeight-125)/2,(screenHeight-125)/2)
                                 .addGap(15)
                                 .addComponent(pnlGraph,(screenHeight-125)/2,(screenHeight-125)/2,(screenHeight-125)/2)))
                     .addContainerGap(5,5)
             ));
             
            GroupLayout pnlDataLayout = (GroupLayout)pnlData.getLayout();
             pnlDataLayout.setHorizontalGroup(
                 pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                 .addGroup(pnlDataLayout.createSequentialGroup()
                     .addContainerGap(5,5)
                     .addGroup(pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                         .addGroup(pnlDataLayout.createSequentialGroup()
                             .addGap((screenWidth - 830)/5,(screenWidth - 830)/5,(screenWidth - 830)/5)
                             .addComponent(lblTof,(screenWidth - 830)/5,(screenWidth - 830)/5,(screenWidth - 830)/5)
                             .addComponent(lblTon,(screenWidth - 830)/5,(screenWidth - 830)/5,(screenWidth - 830)/5)
                             .addComponent(lblTotal,(screenWidth - 830)/5,(screenWidth - 830)/5,(screenWidth - 830)/5)
                             .addComponent(lblLocation,(screenWidth - 830)/5,(screenWidth - 830)/5,(screenWidth - 830)/5))
                         .addComponent(pnlDataTable,screenWidth - 830,screenWidth - 830,screenWidth - 830))
                     .addContainerGap(5,5))
                 .addComponent(lblComments)
                 .addComponent(sclComments, screenWidth - 830, screenWidth - 830,screenWidth - 830)
                 .addGroup(pnlDataLayout.createSequentialGroup()
                     .addComponent(btnClearComments,(screenWidth - 830)/2,(screenWidth - 830)/2,(screenWidth - 830)/2)
                     .addComponent(btnSaveComments,(screenWidth - 830)/2,(screenWidth - 830)/2,(screenWidth - 830)/2)) 
             );
 
             pnlDataLayout.setVerticalGroup(
                 pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                 .addGroup(pnlDataLayout.createSequentialGroup()
                     .addContainerGap(5,5)
                     .addGroup(pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                         .addComponent(lblTof,25,25,25)
                         .addComponent(lblTon,25,25,25)
                         .addComponent(lblTotal,25,25,25)
                         .addComponent(lblLocation,25,25,25))
                     .addGap(5,5,5)
                     .addComponent(pnlDataTable, screenHeight-345,screenHeight-345,screenHeight-345)
                     .addGap(5,5,5)
                     .addComponent(lblComments,25,25,25)
                     .addComponent(sclComments,100,100,100)
                     .addGroup(pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                         .addComponent(btnClearComments, 25,25,25)
                         .addGap(5,5,5)
                         .addComponent(btnSaveComments,25,25,25))
                     .addContainerGap(5,5))
             );
             
             //Setup image array
             locationImagesSmall_ = new HashMap<String,ImageIcon>(35);
             locationImagesLarge_ = new HashMap<String,ImageIcon>(35);
             char[] letters = {'A','B','C','D','E','F','G'};
             char[] numbers = {'0','1','2','3','4'};
             int count = 0;
             for(char letter : letters){
                 for(char number: numbers){
                     ImageIcon icon = new ImageIcon(getClass().getResource("/trampoline/images/"+letter+number+".png"));
                     Image img = icon.getImage();
                     java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(60, 35, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                     Graphics g = bi.createGraphics();
                     g.drawImage(img, 0,0, 60, 35, null);
                     locationImagesSmall_.put(""+letter+number, new ImageIcon(bi));
                     locationImagesLarge_.put(""+letter+number, icon);
                     count++;
                 }
             }
             
             ImageIcon icon = new ImageIcon(getClass().getResource("/trampoline/images/eurotramp.png"));
             Image img = icon.getImage();
             java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(60, 35, java.awt.image.BufferedImage.TYPE_INT_ARGB);
             Graphics g = bi.createGraphics();
             g.drawImage(img, 0,0, 60, 35, null);
             locationImagesSmall_.put("none", new ImageIcon(bi));
             locationImagesLarge_.put("none", icon);
                                         
             centralTitleFont = getFont("centralTitleFontLarge");
             centralNumberFont = getFont("centralNumbersFontLarge");
         }
         
         GroupLayout pnlStatsLayout = (GroupLayout)pnlStats.getLayout();
         pnlStatsLayout.setHorizontalGroup(
              pnlStatsLayout.createSequentialGroup()
              .addContainerGap(5,5)
              .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                     .addGroup(pnlStatsLayout.createSequentialGroup()
                         .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                             .addComponent(lblAvToFTxt,110,110,110)
                             .addComponent(lblAvToNTxt,110,110,110)
                             .addComponent(lblAvTotalTxt,110,110,110)
                             .addComponent(lblHighestToFTxt,110,110,110))
                         .addGap(5,5,5)
                         .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                             .addComponent(lblAvToFNo,50,50,50)
                             .addComponent(lblAvToNNo,50,50,50)
                             .addComponent(lblAvTotalNo,50,50,50)
                             .addComponent(lblHighestToFNo,50,50,50))
                         .addGap(20,20,20)
                         .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                             .addComponent(lblOvToFTxt,100,100,100)
                             .addComponent(lblOvToNTxt,100,100,100)
                             .addComponent(lblOvTotalTxt,100,100,100)
                             .addComponent(lblLowestToFTxt,100,100,100))
                         .addGap(5,5,5)
                         .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                             .addComponent(lblOvToFNo,50,50,50)
                             .addComponent(lblOvToNNo,50,50,50)
                             .addComponent(lblOvTotalNo,50,50,50)
                             .addComponent(lblLowestToFNo,50,50,50)))
                    .addGroup(pnlStatsLayout.createSequentialGroup()
                         .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                             .addComponent(lblFurthestTxt,235,235,235)
                             .addComponent(lblAvPositionTxt,235,235,235)
                             .addComponent(lblLargestPositionTxt,235,235,235)
                             .addComponent(lblSmallestPositionTxt,235,235,235))
                         .addGap(5,5,5)
                         .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                             .addComponent(lblFurthestNo,100,100,100)
                             .addComponent(lblAvPositionNo,100,100,100)
                             .addComponent(lblLargestPositionNo,100,100,100)
                             .addComponent(lblSmallestPositionNo,100,100,100))))
              .addContainerGap(5,5)
         );
         pnlStatsLayout.setVerticalGroup(
             pnlStatsLayout.createSequentialGroup()
             .addContainerGap(5,5)
             .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                 .addComponent(lblAvToFTxt,25,25,25)
                 .addComponent(lblAvToFNo,25,25,25)
                 .addComponent(lblOvToFTxt,25,25,25)
                 .addComponent(lblOvToFNo,25,25,25))
              .addGap(5,5,5)
             .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                 .addComponent(lblAvToNTxt,25,25,25)
                 .addComponent(lblAvToNNo,25,25,25)
                 .addComponent(lblOvToNTxt,25,25,25)
                 .addComponent(lblOvToNNo,25,25,25))
             .addGap(5,5,5)
             .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                 .addComponent(lblAvTotalTxt,25,25,25)
                 .addComponent(lblAvTotalNo,25,25,25)
                 .addComponent(lblOvTotalTxt,25,25,25)
                 .addComponent(lblOvTotalNo,25,25,25))
             .addGap((((screenHeight-125)/2)-285)/4,(((screenHeight-125)/2)-285)/4,(((screenHeight-125)/2)-285)/4)
             .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                 .addComponent(lblHighestToFTxt,25,25,25)
                 .addComponent(lblHighestToFNo,25,25,25)
                 .addComponent(lblLowestToFTxt,25,25,25)
                 .addComponent(lblLowestToFNo,25,25,25))
             .addGap((((screenHeight-125)/2)-285)/4,(((screenHeight-125)/2)-285)/4,(((screenHeight-125)/2)-285)/4)
             .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                 .addComponent(lblFurthestTxt,25,25,25)
                 .addComponent(lblFurthestNo,25,25,25))
             .addGap(5,5,5)
             .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                 .addComponent(lblAvPositionTxt,25,25,25)
                 .addComponent(lblAvPositionNo,25,25,25))
             .addGap(5,5,5)
             .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                 .addComponent(lblSmallestPositionTxt,25,25,25)
                 .addComponent(lblSmallestPositionNo,25,25,25))
             .addGap(5,5,5)
             .addGroup(pnlStatsLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                 .addComponent(lblLargestPositionTxt,25,25,25)
                 .addComponent(lblLargestPositionNo,25,25,25))
             .addGap((((screenHeight-125)/2)-285)/2,(((screenHeight-125)/2)-285)/2,(((screenHeight-125)/2)-285)/2)
             .addContainerGap(5,5)
         );
         
         lblAvToFTxt.setFont(getFont("statsPanelFont"));
         lblAvToNTxt.setFont(getFont("statsPanelFont"));
         lblAvTotalTxt.setFont(getFont("statsPanelFont"));
         lblOvToFTxt.setFont(getFont("statsPanelFont"));
         lblOvToNTxt.setFont(getFont("statsPanelFont"));
         lblOvTotalTxt.setFont(getFont("statsPanelFont"));
         lblAvToFNo.setFont(getFont("statsPanelFont"));
         lblAvToFNo.setForeground(new java.awt.Color(0, 0, 255));
         lblAvToNNo.setFont(getFont("statsPanelFont"));
         lblAvToNNo.setForeground(new java.awt.Color(0, 0, 255));
         lblAvTotalNo.setFont(getFont("statsPanelFont"));
         lblAvTotalNo.setForeground(new java.awt.Color(0,0, 255));
         lblOvToFNo.setFont(getFont("statsPanelFont"));
         lblOvToFNo.setForeground(new java.awt.Color(0, 0, 255));
         lblOvToNNo.setFont(getFont("statsPanelFont"));
         lblOvToNNo.setForeground(new java.awt.Color(0, 0, 255));
         lblOvTotalNo.setFont(getFont("statsPanelFont"));
         lblOvTotalNo.setForeground(new java.awt.Color(0, 0, 255));
         
         lblHighestToFTxt.setFont(getFont("statsPanelFont"));
         lblHighestToFNo.setFont(getFont("statsPanelFont"));
         lblHighestToFNo.setForeground(new java.awt.Color(0, 0, 255));
         lblLowestToFTxt.setFont(getFont("statsPanelFont"));
         lblLowestToFNo.setFont(getFont("statsPanelFont"));
         lblLowestToFNo.setForeground(new java.awt.Color(255, 0, 0));
         
         lblFurthestTxt.setFont(getFont("statsPanelFont"));
         lblFurthestNo.setFont(getFont("statsPanelFont"));
         lblFurthestNo.setForeground(new java.awt.Color(0, 0, 255));
         lblAvPositionTxt.setFont(getFont("statsPanelFont"));
         lblAvPositionNo.setFont(getFont("statsPanelFont"));
         lblAvPositionNo.setForeground(new java.awt.Color(0, 0, 255));
         lblSmallestPositionTxt.setFont(getFont("statsPanelFont"));
         lblSmallestPositionNo.setFont(getFont("statsPanelFont"));
         lblSmallestPositionNo.setForeground(new java.awt.Color(0, 0, 255));
         lblLargestPositionTxt.setFont(getFont("statsPanelFont"));
         lblLargestPositionNo.setFont(getFont("statsPanelFont"));
         lblLargestPositionNo.setForeground(new java.awt.Color(255, 0, 0));
 
         heightTags = screenHeight - 665;
         GroupLayout pnlStartLayout = (GroupLayout)pnlStart.getLayout();  
         pnlStartLayout.setHorizontalGroup(
             pnlStartLayout.createSequentialGroup()
             .addGroup(pnlStartLayout.createSequentialGroup()
                 .addContainerGap(5,5)
                 .addGroup(pnlStartLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                     .addComponent(lblSelectTof,340, 340, 340)
                     .addComponent(drpDeviceName,340, 340, 340)
                     .addComponent(lblSelectGymnast,340, 340, 340)
                     .addComponent(drpSelectGymnast,340, 340, 340)
                     .addComponent(lblNumberOfBounces,340, 340, 350)
                     .addComponent(txtNumberOfBounces,50,50,50)
                     .addGroup(pnlStartLayout.createSequentialGroup()
                         .addComponent(lblTags,115, 115, 115)
                         .addGap(5,5,5)
                         .addComponent(lblAddNewTag,220,220,220))
                     .addComponent(sclTags,340, 340, 340)
                     .addGroup(pnlStartLayout.createSequentialGroup()
                         .addComponent(btnCollectData,150,150,150)
                         .addComponent(btnClearData, 150,150,150))))
                 .addContainerGap(5,5)
         );
         pnlStartLayout.setVerticalGroup(
             pnlStartLayout.createSequentialGroup()
             .addGroup(pnlStartLayout.createSequentialGroup()
                 .addComponent(lblSelectTof,25,25,25)
                 .addGap(5,5,5)
                 .addComponent(drpDeviceName,25,25,25)
                 .addGap(5,5,5)
                 .addComponent(lblSelectGymnast,25,25,25)
                 .addGap(5,5,5)
                 .addComponent(drpSelectGymnast,25,25,25)
                 .addGap(5,5,5)
                 .addComponent(lblNumberOfBounces,25,25,25)
                 .addGap(5,5,5)
                 .addComponent(txtNumberOfBounces,25,25,25)
                 .addGap(5,5,5)
                 .addGroup(pnlStartLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                     .addComponent(lblTags,25,25,25)
                     .addComponent(lblAddNewTag,25,25,25))
                 .addGap(5,5,5)
                 .addComponent(sclTags, heightTags, heightTags, heightTags)
                 .addGap(5,5,5)
                 .addGroup(pnlStartLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                     .addComponent(btnCollectData, 25, 25, 25)
                     .addComponent(btnClearData, 25, 25, 25))
                 .addContainerGap(5,5))
         );
         
         lblAddNewTag.setForeground(new java.awt.Color(0,0,255));
         lblAddNewTag.setCursor(new Cursor(java.awt.Cursor.HAND_CURSOR));
         
         //Make the labels that we require for the centre panel. 
         labelArray_ = new JLabel[50];
  
         lblTof.setFont(centralTitleFont);
         lblTof.setText("ToF");
         
         lblTon.setFont(centralTitleFont);
         lblTon.setText("ToN");
 
         lblTotal.setFont(centralTitleFont);
         lblTotal.setText("Total");
         
         lblLocation.setFont(centralTitleFont);
         lblLocation.setText("Location:");
         
         
         pnlDataTable.setLayout(new GridLayout(10,5));
         
         //Create the labels for the Numbers themselves. 
         for (int i = 0; i < 10; i++) {
             labelArray_[i*5]   = new JLabel("labNumber"+i);
             labelArray_[i*5+1] = new JLabel("labTOF"+i);
             labelArray_[i*5+2] = new JLabel("labTON"+i);
             labelArray_[i*5+3] = new JLabel("labTotal"+i);
             labelArray_[i*5+4] = new JLabel("labLocation"+i);
         }
         
         //Then place the labels just created into the GridLayout. 
         for (int i = 0; i< 10; i++) {
             pnlDataTable.add(labelArray_[i*5]);
             labelArray_[i*5].setText("Jump "+(i+1)+":");
             labelArray_[i*5].setFont(centralTitleFont);
             //labelArray_[i*4].setVisible(false);
             
             pnlDataTable.add(labelArray_[i*5+1]);
             labelArray_[i*5+1].setText("00.000");
             labelArray_[i*5+1].setFont(centralNumberFont);
             
             pnlDataTable.add(labelArray_[i*5+2]);
             labelArray_[i*5+2].setText("00.000");
             labelArray_[i*5+2].setFont(centralNumberFont);
             
             pnlDataTable.add(labelArray_[i*5+3]);
             labelArray_[i*5+3].setText("00.000");
             labelArray_[i*5+3].setFont(centralNumberFont);
             
             pnlDataTable.add(labelArray_[i*5+4]);
             labelArray_[i*5+4].setText("");
         }
  
         labelArray_[4].setIcon(locationImagesSmall_.get("D2"));
         labelArray_[9].setIcon(locationImagesSmall_.get("E2"));
         labelArray_[14].setIcon(locationImagesSmall_.get("F1"));
         labelArray_[19].setIcon(locationImagesSmall_.get("G4"));
         labelArray_[24].setIcon(locationImagesSmall_.get("A3"));
         labelArray_[29].setIcon(locationImagesSmall_.get("B4"));
         labelArray_[34].setIcon(locationImagesSmall_.get("C1"));
         labelArray_[39].setIcon(locationImagesSmall_.get("E4"));
         labelArray_[44].setIcon(locationImagesSmall_.get("D2"));
         labelArray_[49].setIcon(locationImagesSmall_.get("none"));
                 
         pnlDataTable.repaint();
         
         // Setup Beam Status images
         lblTrampoline.setBounds(7, 20, 366, 217);
         lblTrampoline.setIcon(locationImagesLarge_.get("none"));
         currentLocation_ = "none";
         
         this.beamStatusRedArray_ = new JLabel[16];
         this.beamStatusGreenArray_ = new JLabel[16];
         
         for(int i = 0; i < 16; i++){
             beamStatusRedArray_[i] = new JLabel("");
             beamStatusGreenArray_[i] = new JLabel("");
             beamStatusRedArray_[i].setIcon(new ImageIcon(getClass().getResource("/trampoline/images/redBeam.png")));
             beamStatusGreenArray_[i].setIcon(new ImageIcon(getClass().getResource("/trampoline/images/greenBeam.png")));
             layBeamStatus.add(beamStatusRedArray_[i], JLayeredPane.POPUP_LAYER);
             layBeamStatus.add(beamStatusGreenArray_[i], JLayeredPane.POPUP_LAYER);
             beamStatusGreenArray_[i].setVisible(false);
         }
         
         beamStatusRedArray_[0].setBounds(29, 160, 20, 20);
         beamStatusGreenArray_[0].setBounds(29, 160, 20, 20);
         beamStatusRedArray_[1].setBounds(325, 160, 20, 20);
         beamStatusGreenArray_[1].setBounds(325, 160, 20, 20);
         beamStatusRedArray_[2].setBounds(29, 120, 20, 20);
         beamStatusGreenArray_[2].setBounds(29, 120, 20, 20);
         beamStatusRedArray_[3].setBounds(325, 120, 20, 20);
         beamStatusGreenArray_[3].setBounds(325, 120, 20, 20);
         beamStatusRedArray_[4].setBounds(29, 80, 20, 20);
         beamStatusGreenArray_[4].setBounds(29, 80, 20, 20);
         beamStatusRedArray_[5].setBounds(325, 80, 20, 20);
         beamStatusGreenArray_[5].setBounds(325, 80, 20, 20);
         beamStatusRedArray_[6].setBounds(62, 40, 20, 20);
         beamStatusGreenArray_[6].setBounds(62, 40, 20, 20);
         beamStatusRedArray_[7].setBounds(62, 200, 20, 20);
         beamStatusGreenArray_[7].setBounds(62, 200, 20, 20);
         beamStatusRedArray_[8].setBounds(117, 40, 20, 20);
         beamStatusGreenArray_[8].setBounds(117, 40, 20, 20);
         beamStatusRedArray_[9].setBounds(117, 200, 20, 20);
         beamStatusGreenArray_[9].setBounds(117, 200, 20, 20);
         beamStatusRedArray_[10].setBounds(180, 40, 20, 20);
         beamStatusGreenArray_[10].setBounds(180, 40, 20, 20);
         beamStatusRedArray_[11].setBounds(180, 200, 20, 20);
         beamStatusGreenArray_[11].setBounds(180, 200, 20, 20);
         beamStatusRedArray_[12].setBounds(242, 40, 20, 20);
         beamStatusGreenArray_[12].setBounds(242, 40, 20, 20);
         beamStatusRedArray_[13].setBounds(242, 200, 20, 20);
         beamStatusGreenArray_[13].setBounds(242, 200, 20, 20);
         beamStatusRedArray_[14].setBounds(297, 40, 20, 20);
         beamStatusGreenArray_[14].setBounds(297, 40, 20, 20);
         beamStatusRedArray_[15].setBounds(297, 200, 20, 20);
         beamStatusGreenArray_[15].setBounds(297, 200, 20, 20);
 		
         //Create a dummy chart to add to essentially reserve the space on the relevant panels.        
         double[] values = new double[10];
         String[] names = new String[10];
         for (int i = 0; i < 10; i++) {
             values[i] = 0;
             names[i]  = "Bounce "+i;
         }
         //Create the chart objects with dummy data.
         chartObject_ = new Chart("Default Generated Statistics Chart", values, names, "Jump Number", "Height");
         pnlGraph.setLayout(new java.awt.BorderLayout());
 		JFreeChart jChart = chartObject_.createChart();
 		ChartPanel CP = new ChartPanel(jChart);
         pnlGraph.add(CP);
         
         //Initially give values to avoid NullPointerExceptions
         chartValues = new double[10];
         chartNames  = new String[10];
         for (int i = 0; i < 10; i++) {
             chartValues[i] = 0;
             chartNames[i]  = "Bounce "+i;
         }
         
         DefaultListModel lstTagsModel = new DefaultListModel();
         lstTags.setModel(lstTagsModel);
         lblNumberOfBounces.setVisible(false);
         txtNumberOfBounces.setVisible(false);
         lblTags.setVisible(false);
         lblAddNewTag.setVisible(false);
         sclTags.setVisible(false);
         lstTags.setVisible(false);
         btnCollectData.setVisible(false);
         btnClearData.setVisible(false);
         drpSelectGymnast.setVisible(false);
         lblSelectGymnast.setVisible(false);
     }
     
     private void initStatisticsUI(){
 
         
         //Create the chart objects with dummy data.
         //chartObjectStats_ = new Chart(values, names, "title ststs");
         //Set the two panels to have the appropriate layouts and add the charts. 
         pnlStatisticsSmall.setLayout(new java.awt.BorderLayout());
         //pnlStatisticsSmall.add(chartObjectStats_, BorderLayout.CENTER);
     }
     
     private void initImportExportUI(){
         
     }
     
     private void initClubManagementUI(){
         int screenHeight = this.getMaximizedBounds().height;
         int screenWidth = this.getMaximizedBounds().width;
         
         GroupLayout pnlClubManagementLayout = (GroupLayout)pnlClubManagement.getLayout();  
         pnlClubManagementLayout.setHorizontalGroup(
             pnlClubManagementLayout.createSequentialGroup()
             .addContainerGap(5,5)    
             .addGroup(pnlClubManagementLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                 .addGroup(pnlClubManagementLayout.createSequentialGroup()
                     .addComponent(pnlGymnast,(screenWidth-20)/2,(screenWidth-20)/2,(screenWidth-20)/2)
                     .addComponent(pnlClub,(screenWidth-20)/2,(screenWidth-20)/2,(screenWidth-20)/2))
                 .addGroup(pnlClubManagementLayout.createSequentialGroup()
                     .addComponent(pnlGymnastDetails,(screenWidth-20)/2,(screenWidth-20)/2,(screenWidth-20)/2)
                     .addComponent(pnlClubDetails, (screenWidth-20)/2,(screenWidth-20)/2,(screenWidth-20)/2))
                 .addComponent(pnlRoutines,screenWidth-20,screenWidth-20,screenWidth-20)
                 .addComponent(pnlAdmin,screenWidth-20,screenWidth-20,screenWidth-20))
             .addContainerGap(5,5)  
         );
 
         pnlClubManagementLayout.setVerticalGroup(
             pnlClubManagementLayout.createSequentialGroup()
             .addContainerGap(5,5)
             .addGroup(pnlClubManagementLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                 .addComponent(pnlGymnast, 85, 85, 85)
                 .addComponent(pnlClub, 85, 85, 85))
             .addGroup(pnlClubManagementLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                 .addComponent(pnlGymnastDetails,200,200,200)
                 .addComponent(pnlClubDetails,200,200,200))
             .addComponent(pnlRoutines, screenHeight - 470, screenHeight - 470, screenHeight - 470)
             .addComponent(pnlAdmin,85,85,85)
             .addContainerGap(5,5)
         );
 
         GroupLayout pnlGymnastLayout = (GroupLayout)pnlGymnast.getLayout();
         pnlGymnastLayout.setHorizontalGroup(
             pnlGymnastLayout.createSequentialGroup()
             .addContainerGap(5,5)
             .addComponent(lblGymnast,60,60,60)
             .addGap(5,5,5)
             .addComponent(drpGymnastName,((screenWidth-20)/2)-235,((screenWidth-20)/2)-235,((screenWidth-20)/2)-235)
             .addGap(20,20,20)    
             .addComponent(btnAddGymnast,115,115,115)   
             .addContainerGap(5,5)
         );
 
         pnlGymnastLayout.setVerticalGroup(
             pnlGymnastLayout.createSequentialGroup()
             .addContainerGap(5,5)
             .addGroup(pnlGymnastLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                 .addComponent(lblGymnast,25,25,25)
                 .addComponent(drpGymnastName,25,25,25)
                 .addComponent(btnAddGymnast,25,25,25))
             .addContainerGap(5,5)
         );
         
         GroupLayout pnlClubLayout = (GroupLayout)pnlClub.getLayout();
         pnlClubLayout.setHorizontalGroup(
             pnlClubLayout.createSequentialGroup()
             .addContainerGap(5,5)
             .addComponent(lblClub,40,40,40)
             .addGap(5,5,5)
             .addComponent(drpClubName,((screenWidth-20)/2)-215,((screenWidth-20)/2)-215,((screenWidth-20)/2)-215)
             .addGap(20,20,20)    
             .addComponent(btnAddClub,115,115,115)   
             .addContainerGap(5,5)
         );
 
         pnlClubLayout.setVerticalGroup(
             pnlClubLayout.createSequentialGroup()
             .addContainerGap(5,5)
             .addGroup(pnlClubLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                 .addComponent(lblClub,25,25,25)
                 .addComponent(drpClubName,25,25,25)
                 .addComponent(btnAddClub,25,25,25))
             .addContainerGap(5,5)
         );
 
         GroupLayout pnlGymnastDetailsLayout = (GroupLayout)pnlGymnastDetails.getLayout();
         pnlGymnastDetailsLayout.setHorizontalGroup(
             pnlGymnastDetailsLayout.createSequentialGroup()
             .addContainerGap(5,5)
             .addGroup(pnlGymnastDetailsLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                 .addComponent(lblGymnastName,100,100,100)
                 .addComponent(lblDob,100,100,100)
                 .addComponent(lblCategory,100,100,100)
                 .addComponent(lblClubName,100,100,100)
                 .addComponent(btnAddModifyUser,100,100,100))
             .addGap(5,5,5)
             .addGroup(pnlGymnastDetailsLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                 .addComponent(txtName,((screenWidth-20)/2)-140,((screenWidth-20)/2)-140,((screenWidth-20)/2)-140)
                 .addGroup(pnlGymnastDetailsLayout.createSequentialGroup()
                     .addComponent(drpDate,50,50,50)
                     .addGap(5,5,5)
                     .addComponent(drpMonth,50,50,50)
                     .addGap(5,5,5)
                     .addComponent(drpYear,75,75,75)
                     .addGap(5,5,5))
                 .addComponent(drpCategory,100,100,100)
                 .addComponent(drpClubs,100,100,100)
                 .addComponent(btnDeleteUser,100,100,100))
             .addContainerGap(5,5)
         );
 
         pnlGymnastDetailsLayout.setVerticalGroup(
             pnlGymnastDetailsLayout.createSequentialGroup()
             .addContainerGap(5,5)
             .addGroup(pnlGymnastDetailsLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                 .addComponent(lblGymnastName,25,25,25)
                 .addComponent(txtName,25,25,25))
             .addGap(5,5,5)
             .addGroup(pnlGymnastDetailsLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                 .addComponent(lblDob,25,25,25)
                 .addComponent(drpDate,25,25,25)
                 .addComponent(drpMonth,25,25,25)
                 .addComponent(drpYear,25,25,25))
             .addGap(5,5,5)
             .addGroup(pnlGymnastDetailsLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                 .addComponent(lblCategory,25,25,25)
                 .addComponent(drpCategory,25,25,25))
             .addGap(5,5,5)
             .addGroup(pnlGymnastDetailsLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                 .addComponent(lblClubName,25,25,25)
                 .addComponent(drpClubs,25,25,25))  
             .addGap(5,5,5)   
             .addGroup(pnlGymnastDetailsLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                 .addComponent(btnAddModifyUser,25,25,25)
                 .addComponent(btnDeleteUser,25,25,25))
             .addContainerGap(5,5)
         );
 
         GroupLayout pnlAdminLayout = (GroupLayout)pnlAdmin.getLayout();
         pnlAdminLayout.setHorizontalGroup(
             pnlAdminLayout.createSequentialGroup()
             .addContainerGap(5,5)
             .addComponent(lblNewPassword,90,90,90)
             .addGap(5,5,5)
             .addComponent(txtPassword1,150,150,150)
             .addGap(15,15,15)
             .addComponent(lblNewPassword2,100,100,100)
             .addGap(5,5,5)
             .addComponent(txtPassword2,150,150,150)
             .addGap(5,5,5)
             .addComponent(btnNewPassword,120,120,120)
             .addGap(screenWidth - 900,screenWidth - 900,screenWidth - 900)
             .addComponent(btnResetAll,120,120,120)
             .addGap(5,5,5)
             .addComponent(btnLogout,80,80,80)
             .addContainerGap(5,5)   
         );
 
         pnlAdminLayout.setVerticalGroup(
             pnlAdminLayout.createSequentialGroup()
             .addContainerGap(5,5)
             .addGroup(pnlAdminLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                 .addComponent(lblNewPassword,25,25,25)
                 .addComponent(txtPassword1,25,25,25)
                 .addComponent(lblNewPassword2,25,25,25)
                 .addComponent(txtPassword2,25,25,25)
                 .addComponent(btnNewPassword,25,25,25)
                 .addComponent(btnResetAll,25,25,25)
                 .addComponent(btnLogout,25,25,25))
             .addContainerGap(5,5)
         );
    
          //Set the numbers for the date of birth entries on Club Management. 
         for (int k = 1; k <= 31; k++) {
             drpDate.addItem(k);
         }
         
         //And month on Club Management.
         String[] monthName = {"January", "February","March", "April", "May", "June", "July","August", "September", "October", "November","December"};
         for (int l = 1; l <= 12; l++) {
             drpMonth.addItem(l);
         }
         
         //Finally the year on Club Management. 
         DateFormat dateFormat = new SimpleDateFormat("yyyy");
         Date date = new Date();
         int yearStart = Integer.parseInt(dateFormat.format(date));
         for (int l = 1900; l <= yearStart; l++) {
             drpYear.addItem(l);
         }
         
         //Category on Club Management. 
         updateCategoryDropDown();
          
         updateGymnastDropDown();
         gymnastPanelVisible(false);               
         //Make club management tab invisible
         setAdminAccess(false);
     }
     
     private void gymnastPanelVisible(boolean state){
         lblGymnastName.setVisible(state);
         txtName.setVisible(state);
         lblCategory.setVisible(state);
         drpCategory.setVisible(state);
         lblDob.setVisible(state);
         drpDate.setVisible(state);
         drpMonth.setVisible(state);
         drpYear.setVisible(state);
         lblClubName.setVisible(state);
         drpClubs.setVisible(state);
         btnAddModifyUser.setVisible(state);
         btnDeleteUser.setVisible(state);
         btnAddModifyUser.setEnabled(state);
         btnDeleteUser.setEnabled(state);
     }
             
     private void initGeneralUI() { 
         Toolkit toolkit = Toolkit.getDefaultToolkit();
         this.screenResolution_ = toolkit.getScreenSize();
         
         
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setTitle("Chronos");
         tabPane.setBackground(new Color(255, 255, 255));
         tabPane.setBackground(new Color(255, 255, 255));
         
         this.setSize(screenResolution_);
         this.setPreferredSize(screenResolution_);
         tabPane.setSize(screenResolution_);
         tabPane.setPreferredSize(screenResolution_);
         layMainLayer.setPreferredSize(screenResolution_);
         layMainLayer.setSize(screenResolution_);
         pnlToF.setSize(screenResolution_);
         pnlToF.setPreferredSize(screenResolution_);
         pnlStatistics.setSize(screenResolution_);
 	pnlStatistics.setPreferredSize(screenResolution_);
         pnlImportExport.setSize(screenResolution_);
 	pnlImportExport.setPreferredSize(screenResolution_);
         pnlClubManagement.setSize(screenResolution_);
 	pnlClubManagement.setPreferredSize(screenResolution_);
         
         
         GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
         this.setMaximizedBounds(env.getMaximumWindowBounds());
         this.setExtendedState(this.getExtendedState()|JFrame.MAXIMIZED_BOTH); 
         
         //Set size of list of tags
         int noItems = lstTags.getModel().getSize()*35;
         lstTags.setPreferredSize(new Dimension(128,noItems));
         lstTags.setSize(new Dimension (128,noItems));
         
         lblError.setText("");
         lblError.setCursor(new Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         lblError.setFont(getFont("errorFont"));
         
         currentLocation_ = "";
     }
 
     private void initDatabase(){
         db_ = new DBConnect(this.messageHandler_);
         adminAccessGranted_ = false;
         currentRoutineId_ = 0;
         
         // Change to retrieve from database
         adminPassword_ = "21232F297A57A5A743894A0E4A801FC3";
         
     }
     
     public Font getFont(String s){
         Font returnFont = new Font("Verdana", Font.PLAIN, 1);
         
         if(s.equals("labelFont")){
             returnFont = new Font("Verdana", Font.PLAIN, 12);
         }
         
         if(s.equals("centralTitleFontLarge")){
             returnFont = new Font("Verdana", Font.BOLD, 14);
         }
         
         if(s.equals("centralTitleFontSmall")){
             returnFont = new Font("Verdana", Font.BOLD, 12);
         }
         
         if(s.equals("centralNumbersFontLarge")){
             returnFont = new Font("Verdana", Font.PLAIN, 14);
         }
         
         if(s.equals("centralNumbersFontSmall")){
             returnFont = new Font("Verdana", Font.PLAIN, 12);
         }
         
         if(s.equals("dropdownFont")){
             returnFont = new Font("Verdana", Font.PLAIN, 12);
         }
         
         if(s.equals("scrollboxFont")){
             returnFont = new Font("Verdana", Font.PLAIN, 12);
         }
         
         if(s.equals("buttonFont")){
             returnFont = new Font("Verdana", Font.PLAIN, 12);
         }
         
         if(s.equals("borderFont")){
                 returnFont = new Font("Verdana", Font.BOLD, 14);
         }
         
         if(s.equals("errorFont")){
                 returnFont = new Font("Verdana", Font.BOLD, 16);
         }
         
         if(s.equals("statsPanelFont")){
             returnFont = new Font("Verdana",Font.PLAIN, 14);
         }
         return returnFont;
     }
     
     public TofInterface stringToTof(String s) {
         String split[] = s.split(" ");
         
         if(split.length == 3){
             int id = this.portStrings_.indexOf(split[0]);
             return this.portsAvaliable_.get(id).getInterface(Integer.parseInt(split[2])-1);
         }else{
             int id = this.portStrings_.indexOf(split[0]);
             return this.portsAvaliable_.get(id).getInterface(0);
         }
     }
     
     public void callOnClose() {
         for (PortController thisPort : this.portsAvaliable_) {
             thisPort.close();
         }
         pageRefreshTimer_.stop();
         errorTimer_.stop();
     }
     
     //This function updates the mini chart on each bounce. 
     public void updateChart(double[] values, String[] names, String title, JPanel pan) {     
         /*
         chartObject_.updateInfo(values, names, title);
 
         //jPanel4.setLayout(new java.awt.BorderLayout());
         pan.removeAll();
         pan.validate();
         pan.repaint();
         pan.add(chartObject_, BorderLayout.CENTER);
         pan.validate();
         pan.repaint();
         * 
         */
     }
     
     public void updateChart(double[] values, String[] names, String title) {
         updateChart(values, names, "Bounce Height", pnlGraph);
     }
     
     public void updateChart(double[] values, String[] names) {
         updateChart(values, names, "Bounce Height");
     }
     
     public void updateCategoryDropDown() {
         JComboBox[] boxesToUpdate = {drpCategory};
         Category[] categoryList = db_.getAllCategories();
         
         for (JComboBox jcb:boxesToUpdate) {
             jcb.removeAllItems();
             
             //jcb.addItem(new ComboItem(0, "<< Please Select Gymnast >>"));
 
             //Gymnast List on Statistics
             for (Category c:categoryList) {
                 jcb.addItem(new ComboItem(c.getID(), c.getName()));
             }
         }
     }
     
     public void updateGymnastDropDown() {
         JComboBox[] boxesToUpdate = {drpStatsGymnast, drpSelectGymnast, drpGymnastName};
         Gymnast[] gymnastList = db_.getAllGymnasts();
         
         for (JComboBox jcb:boxesToUpdate) {
             jcb.removeAllItems();
             
             jcb.addItem(new ComboItem(0, "<< Please Select Gymnast >>"));
 
             //Gymnast List on Statisticsz
             for (Gymnast g:gymnastList) {
                 jcb.addItem(new ComboItem(g.getID(), g.getName()));
             }
         }
     }
     
     public void updateClubDropDown() {
         JComboBox[] boxesToUpdate = {drpClubName, drpClubs};
         Club[] clubList = db_.getAllClubs();
         
         for(JComboBox jcb:boxesToUpdate){
             jcb.removeAllItems();
             jcb.addItem(new ComboItem(0,"<< Please Select Club >>"));
             
             for(Club c:clubList){
                 jcb.addItem(new ComboItem(c.getId(),c.getShortName()));
             }
         }
     }
     
     public void updateJumpTime(String jumpNum, Jump j) {
         //This needs to be written to take account of the new JLabels for the times. 
     }
     
     public void updateJumpTime(int jumpNum, Jump j) {
         updateJumpTime(String.valueOf(jumpNum), j);
     }
     
     //Given a routine, this function will update the main Statistics panel with a graph as well
     //as textual information (the exact nature of which is to be decided).
     public void updateStatistics(Routine r) {
         double[] times  = r.getStatsTimes();
         double[] values = new double[r.getNumberOfJumps()];
         String[] names  = new String[r.getNumberOfJumps()];
         for (int i = 0; i < r.getNumberOfJumps(); i++) {
             values[i] = times[i]*1000;
             names[i]  = "Jump "+i;
             System.out.println(i+ " - "+values[i]+ "=== "+names[i]);
         }
         /*
         chartObjectStats_.updateInfo(values, names, "tsdfsndf");
 
         //jPanel4.setLayout(new java.awt.BorderLayout());
         pnlStatisticsSmall.removeAll();
         pnlStatisticsSmall.validate();
         pnlStatisticsSmall.repaint();
         pnlStatisticsSmall.add(chartObjectStats_, BorderLayout.CENTER);
         pnlStatisticsSmall.validate();
         pnlStatisticsSmall.repaint();
         * 
         */
     }
     
     public void updateStatistics(Jump[] jumpList) {
         //Routine r = new Routine(jumpList);
         //updateStatistics(r);
     }
 
     public void setAdminAccess(boolean state){
         this.adminAccessGranted_ = state;
         pnlGymnast.setVisible(state);
         pnlGymnastDetails.setVisible(state);
         pnlClub.setVisible(state);
         pnlClubDetails.setVisible(state);
         pnlRoutines.setVisible(state);
         pnlAdmin.setVisible(state);
     }
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         layMainLayer = new javax.swing.JLayeredPane();
         tabPane = new javax.swing.JTabbedPane();
         pnlToF = new javax.swing.JPanel();
         pnlStart = new javax.swing.JPanel();
         btnCollectData = new javax.swing.JButton();
         txtNumberOfBounces = new javax.swing.JTextField();
         lblNumberOfBounces = new javax.swing.JLabel();
         drpDeviceName = new javax.swing.JComboBox();
         lblSelectTof = new javax.swing.JLabel();
         lblSelectGymnast = new javax.swing.JLabel();
         drpSelectGymnast = new javax.swing.JComboBox();
         sclTags = new javax.swing.JScrollPane();
         lstTags = new javax.swing.JList();
         lblTags = new javax.swing.JLabel();
         btnClearData = new javax.swing.JButton();
         lblAddNewTag = new javax.swing.JLabel();
         pnlData = new javax.swing.JPanel();
         btnSaveComments = new javax.swing.JButton();
         pnlDataTable = new javax.swing.JPanel();
         lblComments = new javax.swing.JLabel();
         btnClearComments = new javax.swing.JButton();
         sclComments = new javax.swing.JScrollPane();
         txtComments = new javax.swing.JTextArea();
         lblTof = new javax.swing.JLabel();
         lblTon = new javax.swing.JLabel();
         lblTotal = new javax.swing.JLabel();
         lblLocation = new javax.swing.JLabel();
         pnlStats = new javax.swing.JPanel();
         lblAvToFTxt = new javax.swing.JLabel();
         lblAvToNTxt = new javax.swing.JLabel();
         lblAvTotalTxt = new javax.swing.JLabel();
         lblOvTotalTxt = new javax.swing.JLabel();
         lblOvToNTxt = new javax.swing.JLabel();
         lblOvToFTxt = new javax.swing.JLabel();
         lblLowestToFTxt = new javax.swing.JLabel();
         lblHighestToFTxt = new javax.swing.JLabel();
         lblFurthestTxt = new javax.swing.JLabel();
         lblAvPositionTxt = new javax.swing.JLabel();
         lblLargestPositionTxt = new javax.swing.JLabel();
         lblAvToFNo = new javax.swing.JLabel();
         lblAvToNNo = new javax.swing.JLabel();
         lblAvTotalNo = new javax.swing.JLabel();
         lblHighestToFNo = new javax.swing.JLabel();
         lblOvToFNo = new javax.swing.JLabel();
         lblOvToNNo = new javax.swing.JLabel();
         lblOvTotalNo = new javax.swing.JLabel();
         lblLowestToFNo = new javax.swing.JLabel();
         lblFurthestNo = new javax.swing.JLabel();
         lblAvPositionNo = new javax.swing.JLabel();
         lblLargestPositionNo = new javax.swing.JLabel();
         lblSmallestPositionTxt = new javax.swing.JLabel();
         lblSmallestPositionNo = new javax.swing.JLabel();
         pnlGraph = new javax.swing.JPanel();
         layBeamStatus = new javax.swing.JLayeredPane();
         lblTrampoline = new javax.swing.JLabel();
         pnlStatistics = new javax.swing.JPanel();
         pnlStatisticsSmall = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         btnUpdateImages = new javax.swing.JButton();
         drpStatsGymnast = new javax.swing.JComboBox();
         lblStatsGymnast = new javax.swing.JLabel();
         btnStatisticsUpdate = new javax.swing.JButton();
         lblStatsRoutine = new javax.swing.JLabel();
         drpStatsRoutine = new javax.swing.JComboBox();
         pnlImportExport = new javax.swing.JPanel();
         pnlImport = new javax.swing.JPanel();
         pnlExport = new javax.swing.JPanel();
         rdoExportCsv = new javax.swing.JRadioButton();
         btnExportUser = new javax.swing.JButton();
         rdoExportText = new javax.swing.JRadioButton();
         rdoExportExcel = new javax.swing.JRadioButton();
         pnlClubManagement = new javax.swing.JPanel();
         pnlGymnast = new javax.swing.JPanel();
         lblGymnast = new javax.swing.JLabel();
         drpGymnastName = new javax.swing.JComboBox();
         btnAddGymnast = new javax.swing.JButton();
         pnlAdmin = new javax.swing.JPanel();
         lblNewPassword = new javax.swing.JLabel();
         lblNewPassword2 = new javax.swing.JLabel();
         txtPassword2 = new javax.swing.JTextField();
         txtPassword1 = new javax.swing.JTextField();
         btnResetAll = new javax.swing.JButton();
         btnNewPassword = new javax.swing.JButton();
         btnLogout = new javax.swing.JButton();
         pnlGymnastDetails = new javax.swing.JPanel();
         lblGymnastName = new javax.swing.JLabel();
         txtName = new javax.swing.JTextField();
         lblDob = new javax.swing.JLabel();
         drpDate = new javax.swing.JComboBox();
         drpMonth = new javax.swing.JComboBox();
         drpYear = new javax.swing.JComboBox();
         lblCategory = new javax.swing.JLabel();
         drpCategory = new javax.swing.JComboBox();
         btnAddModifyUser = new javax.swing.JButton();
         btnDeleteUser = new javax.swing.JButton();
         lblClubName = new javax.swing.JLabel();
         drpClubs = new javax.swing.JComboBox();
         pnlRoutines = new javax.swing.JPanel();
         jScrollPane3 = new javax.swing.JScrollPane();
         pnlClub = new javax.swing.JPanel();
         lblClub = new javax.swing.JLabel();
         drpClubName = new javax.swing.JComboBox();
         btnAddClub = new javax.swing.JButton();
         pnlClubDetails = new javax.swing.JPanel();
         lblError = new javax.swing.JLabel();
         menBar = new javax.swing.JMenuBar();
         jMenu3 = new javax.swing.JMenu();
         jMenuItem1 = new javax.swing.JMenuItem();
         jMenu4 = new javax.swing.JMenu();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Chronos");
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowOpened(java.awt.event.WindowEvent evt) {
                 TrampolineUIWindowOpened(evt);
             }
         });
 
         layMainLayer.setPreferredSize(new java.awt.Dimension(1280, 1024));
 
         tabPane.setBackground(new java.awt.Color(255, 255, 255));
         tabPane.setPreferredSize(new java.awt.Dimension(1280, 1024));
         tabPane.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 tabPaneFocusGained(evt);
             }
         });
 
         pnlToF.setPreferredSize(new java.awt.Dimension(1280, 1024));
 
         pnlStart.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Start Bouncing", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 14))); // NOI18N
 
         btnCollectData.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
         btnCollectData.setText("Collect Data");
         btnCollectData.setName("");
         btnCollectData.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCollectDataActionPerformed(evt);
             }
         });
 
         txtNumberOfBounces.setText("10");
         txtNumberOfBounces.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 txtNumberOfBouncesActionPerformed(evt);
             }
         });
 
         lblNumberOfBounces.setLabelFor(txtNumberOfBounces);
         lblNumberOfBounces.setText("Number of Jumps To Collect:");
 
         drpDeviceName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         drpDeviceName.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 drpDeviceNameActionPerformed(evt);
             }
         });
 
         lblSelectTof.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
         lblSelectTof.setText("Select a ToF Device:");
 
         lblSelectGymnast.setText("Select a Gymnast:");
 
         drpSelectGymnast.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 drpSelectGymnastActionPerformed(evt);
             }
         });
 
         lstTags.setModel(new javax.swing.AbstractListModel() {
             String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8" };
             public int getSize() { return strings.length; }
             public Object getElementAt(int i) { return strings[i]; }
         });
         lstTags.setMaximumSize(new java.awt.Dimension(1280, 1024));
         lstTags.setMinimumSize(new java.awt.Dimension(35, 128));
         lstTags.setPreferredSize(new java.awt.Dimension(40, 128));
         sclTags.setViewportView(lstTags);
 
         lblTags.setText("Select Tags for Pass");
 
         btnClearData.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
         btnClearData.setText("Clear Data");
         btnClearData.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnClearDataActionPerformed(evt);
             }
         });
 
         lblAddNewTag.setForeground(new java.awt.Color(0, 0, 255));
         lblAddNewTag.setText("(or click here to add a new tag):");
         lblAddNewTag.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lblAddNewTagMouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout pnlStartLayout = new javax.swing.GroupLayout(pnlStart);
         pnlStart.setLayout(pnlStartLayout);
         pnlStartLayout.setHorizontalGroup(
             pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlStartLayout.createSequentialGroup()
                 .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlStartLayout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(drpSelectGymnast, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(sclTags, javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(drpDeviceName, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                     .addGroup(pnlStartLayout.createSequentialGroup()
                         .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(pnlStartLayout.createSequentialGroup()
                                 .addGap(10, 10, 10)
                                 .addComponent(lblTags)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(lblAddNewTag, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(pnlStartLayout.createSequentialGroup()
                                 .addGap(10, 10, 10)
                                 .addComponent(lblNumberOfBounces))
                             .addGroup(pnlStartLayout.createSequentialGroup()
                                 .addGap(37, 37, 37)
                                 .addComponent(btnCollectData)
                                 .addGap(38, 38, 38)
                                 .addComponent(btnClearData, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(pnlStartLayout.createSequentialGroup()
                                 .addContainerGap()
                                 .addComponent(txtNumberOfBounces, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(pnlStartLayout.createSequentialGroup()
                                 .addContainerGap()
                                 .addComponent(lblSelectGymnast))
                             .addGroup(pnlStartLayout.createSequentialGroup()
                                 .addContainerGap()
                                 .addComponent(lblSelectTof, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGap(0, 31, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         pnlStartLayout.setVerticalGroup(
             pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlStartLayout.createSequentialGroup()
                 .addGap(14, 14, 14)
                 .addComponent(lblSelectTof)
                 .addGap(7, 7, 7)
                 .addComponent(drpDeviceName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(lblSelectGymnast)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(drpSelectGymnast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(lblNumberOfBounces)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtNumberOfBounces, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblTags)
                     .addComponent(lblAddNewTag))
                 .addGap(11, 11, 11)
                 .addComponent(sclTags, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(btnCollectData, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnClearData, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(13, Short.MAX_VALUE))
         );
 
         pnlData.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N
 
         btnSaveComments.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
         btnSaveComments.setText("Save Comments");
         btnSaveComments.setName("");
         btnSaveComments.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSaveCommentsActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout pnlDataTableLayout = new javax.swing.GroupLayout(pnlDataTable);
         pnlDataTable.setLayout(pnlDataTableLayout);
         pnlDataTableLayout.setHorizontalGroup(
             pnlDataTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 252, Short.MAX_VALUE)
         );
         pnlDataTableLayout.setVerticalGroup(
             pnlDataTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 191, Short.MAX_VALUE)
         );
 
         lblComments.setText("Comments:");
 
         btnClearComments.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
         btnClearComments.setText("Clear Comments");
         btnClearComments.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnClearCommentsActionPerformed(evt);
             }
         });
 
         sclComments.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
         txtComments.setColumns(20);
         txtComments.setLineWrap(true);
         txtComments.setRows(5);
         sclComments.setViewportView(txtComments);
 
         javax.swing.GroupLayout pnlDataLayout = new javax.swing.GroupLayout(pnlData);
         pnlData.setLayout(pnlDataLayout);
         pnlDataLayout.setHorizontalGroup(
             pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlDataLayout.createSequentialGroup()
                 .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlDataLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(pnlDataTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(pnlDataLayout.createSequentialGroup()
                         .addGap(85, 85, 85)
                         .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(btnSaveComments)
                             .addComponent(btnClearComments))))
                 .addContainerGap(131, Short.MAX_VALUE))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDataLayout.createSequentialGroup()
                 .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlDataLayout.createSequentialGroup()
                         .addGap(70, 70, 70)
                         .addComponent(lblComments)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addGroup(pnlDataLayout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(lblTof)
                             .addComponent(sclComments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(53, 53, 53)))
                 .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lblLocation)
                     .addComponent(lblTotal)
                     .addComponent(lblTon))
                 .addGap(25, 25, 25))
         );
         pnlDataLayout.setVerticalGroup(
             pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlDataLayout.createSequentialGroup()
                 .addComponent(pnlDataTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlDataLayout.createSequentialGroup()
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(lblComments)
                             .addComponent(lblTon)))
                     .addGroup(pnlDataLayout.createSequentialGroup()
                         .addGap(21, 21, 21)
                         .addComponent(lblTof)))
                 .addGap(18, 18, 18)
                 .addComponent(lblTotal)
                 .addGap(32, 32, 32)
                 .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlDataLayout.createSequentialGroup()
                         .addComponent(sclComments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(101, 101, 101)
                         .addComponent(btnClearComments)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(btnSaveComments)
                         .addGap(176, 176, 176))
                     .addGroup(pnlDataLayout.createSequentialGroup()
                         .addComponent(lblLocation)
                         .addContainerGap())))
         );
 
         pnlStats.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Event Stats", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N
 
         lblAvToFTxt.setText("Average ToF:");
 
         lblAvToNTxt.setText("Average ToN:");
 
         lblAvTotalTxt.setText("Average Total:");
 
         lblOvTotalTxt.setText("Overall Total:");
 
         lblOvToNTxt.setText("Overall ToN:");
 
         lblOvToFTxt.setText("Overall ToF:");
 
         lblLowestToFTxt.setText("Lowest ToF:");
 
         lblHighestToFTxt.setText("Highest ToF:");
 
         lblFurthestTxt.setText("Jump furthest from Cross:");
 
         lblAvPositionTxt.setText("Average position deduction:");
 
         lblLargestPositionTxt.setText("Largest position deduction:");
 
         lblAvToFNo.setForeground(new java.awt.Color(0, 51, 255));
         lblAvToFNo.setText("00.000");
 
         lblAvToNNo.setText("00.000");
 
         lblAvTotalNo.setText("00.000");
 
         lblHighestToFNo.setForeground(new java.awt.Color(0, 153, 0));
         lblHighestToFNo.setText("00.000");
 
         lblOvToFNo.setText("00.000");
 
         lblOvToNNo.setText("00.000");
 
         lblOvTotalNo.setText("00.000");
 
         lblLowestToFNo.setForeground(new java.awt.Color(255, 0, 0));
         lblLowestToFNo.setText("00.000");
 
         lblFurthestNo.setText("Jump 4");
 
         lblAvPositionNo.setText("5");
 
         lblLargestPositionNo.setText("5");
 
         lblSmallestPositionTxt.setText("Smallest position deduction:");
 
         lblSmallestPositionNo.setText("5");
 
         javax.swing.GroupLayout pnlStatsLayout = new javax.swing.GroupLayout(pnlStats);
         pnlStats.setLayout(pnlStatsLayout);
         pnlStatsLayout.setHorizontalGroup(
             pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlStatsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlStatsLayout.createSequentialGroup()
                         .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addComponent(lblAvToNTxt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(lblAvTotalTxt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                             .addComponent(lblAvToFTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(lblHighestToFTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblAvToFNo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblAvToNNo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblAvTotalNo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblHighestToFNo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addComponent(lblOvToNTxt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(lblOvToFTxt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(lblOvTotalTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblOvToFNo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblOvToNNo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblOvTotalNo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(30, 30, 30))
                     .addGroup(pnlStatsLayout.createSequentialGroup()
                         .addComponent(lblFurthestTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(lblFurthestNo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap())
                     .addGroup(pnlStatsLayout.createSequentialGroup()
                         .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(pnlStatsLayout.createSequentialGroup()
                                 .addComponent(lblAvPositionTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(lblAvPositionNo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(pnlStatsLayout.createSequentialGroup()
                                 .addComponent(lblLowestToFTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(lblLowestToFNo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(pnlStatsLayout.createSequentialGroup()
                                 .addComponent(lblLargestPositionTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(lblLargestPositionNo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(pnlStatsLayout.createSequentialGroup()
                                 .addComponent(lblSmallestPositionTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(lblSmallestPositionNo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGap(0, 0, Short.MAX_VALUE))))
         );
         pnlStatsLayout.setVerticalGroup(
             pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlStatsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlStatsLayout.createSequentialGroup()
                         .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(lblOvToFTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblOvToFNo))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(lblOvToNTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblOvToNNo))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(lblOvTotalTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblOvTotalNo)))
                     .addGroup(pnlStatsLayout.createSequentialGroup()
                         .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(lblAvToFTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblAvToFNo))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(lblAvToNTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblAvToNNo))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(lblAvTotalTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblAvTotalNo))))
                 .addGap(26, 26, 26)
                 .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblHighestToFTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lblHighestToFNo))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblLowestToFTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lblLowestToFNo))
                 .addGap(18, 18, 18)
                 .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblFurthestTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lblFurthestNo))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblAvPositionTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lblAvPositionNo))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblLargestPositionTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lblLargestPositionNo))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblSmallestPositionNo)
                     .addComponent(lblSmallestPositionTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(4, 4, 4))
         );
 
         pnlGraph.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Graph", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N
         pnlGraph.setPreferredSize(new java.awt.Dimension(500, 500));
 
         javax.swing.GroupLayout pnlGraphLayout = new javax.swing.GroupLayout(pnlGraph);
         pnlGraph.setLayout(pnlGraphLayout);
         pnlGraphLayout.setHorizontalGroup(
             pnlGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 433, Short.MAX_VALUE)
         );
         pnlGraphLayout.setVerticalGroup(
             pnlGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 281, Short.MAX_VALUE)
         );
 
         layBeamStatus.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Beam Status", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N
 
         lblTrampoline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/trampoline/images/eurotramp.png"))); // NOI18N
         lblTrampoline.setBounds(10, 40, 360, 217);
         layBeamStatus.add(lblTrampoline, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         javax.swing.GroupLayout pnlToFLayout = new javax.swing.GroupLayout(pnlToF);
         pnlToF.setLayout(pnlToFLayout);
         pnlToFLayout.setHorizontalGroup(
             pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlToFLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(layBeamStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                     .addComponent(pnlStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGap(18, 18, 18)
                 .addComponent(pnlData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(pnlStats, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(pnlGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE))
                 .addContainerGap())
         );
         pnlToFLayout.setVerticalGroup(
             pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlToFLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(pnlToFLayout.createSequentialGroup()
                         .addComponent(pnlStats, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(pnlGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE))
                     .addGroup(pnlToFLayout.createSequentialGroup()
                         .addComponent(pnlStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(layBeamStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(pnlData, javax.swing.GroupLayout.PREFERRED_SIZE, 632, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(974, Short.MAX_VALUE))
         );
 
         tabPane.addTab("Time of Flight", pnlToF);
 
         pnlStatisticsSmall.setPreferredSize(new java.awt.Dimension(495, 495));
 
         javax.swing.GroupLayout pnlStatisticsSmallLayout = new javax.swing.GroupLayout(pnlStatisticsSmall);
         pnlStatisticsSmall.setLayout(pnlStatisticsSmallLayout);
         pnlStatisticsSmallLayout.setHorizontalGroup(
             pnlStatisticsSmallLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 495, Short.MAX_VALUE)
         );
         pnlStatisticsSmallLayout.setVerticalGroup(
             pnlStatisticsSmallLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
 
         btnUpdateImages.setText("Update Images on Display for Bounce Counter");
         btnUpdateImages.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnUpdateImagesActionPerformed(evt);
             }
         });
 
         drpStatsGymnast.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 drpStatsGymnastActionPerformed(evt);
             }
         });
 
         lblStatsGymnast.setText("Select Gymnast:");
 
         btnStatisticsUpdate.setText("Update Lists Below");
         btnStatisticsUpdate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnStatisticsUpdateActionPerformed(evt);
             }
         });
 
         lblStatsRoutine.setText("Select Routine:");
 
         drpStatsRoutine.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<< Select Gymnast First >>" }));
         drpStatsRoutine.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 drpStatsRoutineActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(btnUpdateImages)
                         .addGap(117, 117, 117)
                         .addComponent(btnStatisticsUpdate))
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblStatsGymnast)
                             .addComponent(lblStatsRoutine))
                         .addGap(18, 18, 18)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(drpStatsRoutine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(drpStatsGymnast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                 .addContainerGap(249, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(btnUpdateImages)
                         .addGap(53, 53, 53)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(drpStatsGymnast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblStatsGymnast)))
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGap(30, 30, 30)
                         .addComponent(btnStatisticsUpdate)))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblStatsRoutine)
                     .addComponent(drpStatsRoutine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(1455, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout pnlStatisticsLayout = new javax.swing.GroupLayout(pnlStatistics);
         pnlStatistics.setLayout(pnlStatisticsLayout);
         pnlStatisticsLayout.setHorizontalGroup(
             pnlStatisticsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlStatisticsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(pnlStatisticsSmall, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         pnlStatisticsLayout.setVerticalGroup(
             pnlStatisticsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlStatisticsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlStatisticsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(pnlStatisticsSmall, javax.swing.GroupLayout.DEFAULT_SIZE, 1600, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         tabPane.addTab("Statistics", pnlStatistics);
 
         pnlImport.setBorder(javax.swing.BorderFactory.createTitledBorder("Import"));
 
         javax.swing.GroupLayout pnlImportLayout = new javax.swing.GroupLayout(pnlImport);
         pnlImport.setLayout(pnlImportLayout);
         pnlImportLayout.setHorizontalGroup(
             pnlImportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 492, Short.MAX_VALUE)
         );
         pnlImportLayout.setVerticalGroup(
             pnlImportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
 
         pnlExport.setBorder(javax.swing.BorderFactory.createTitledBorder("Export"));
 
         rdoExportCsv.setText("CSV");
 
         btnExportUser.setText("Export User");
 
         rdoExportText.setText("Text");
 
         rdoExportExcel.setText("Excel");
 
         javax.swing.GroupLayout pnlExportLayout = new javax.swing.GroupLayout(pnlExport);
         pnlExport.setLayout(pnlExportLayout);
         pnlExportLayout.setHorizontalGroup(
             pnlExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlExportLayout.createSequentialGroup()
                 .addGap(19, 19, 19)
                 .addGroup(pnlExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(btnExportUser)
                     .addComponent(rdoExportCsv)
                     .addComponent(rdoExportText)
                     .addComponent(rdoExportExcel))
                 .addContainerGap(625, Short.MAX_VALUE))
         );
         pnlExportLayout.setVerticalGroup(
             pnlExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlExportLayout.createSequentialGroup()
                 .addGap(15, 15, 15)
                 .addComponent(btnExportUser)
                 .addGap(18, 18, 18)
                 .addComponent(rdoExportCsv)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(rdoExportText)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(rdoExportExcel)
                 .addContainerGap(1445, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout pnlImportExportLayout = new javax.swing.GroupLayout(pnlImportExport);
         pnlImportExport.setLayout(pnlImportExportLayout);
         pnlImportExportLayout.setHorizontalGroup(
             pnlImportExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlImportExportLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(pnlImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(pnlExport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         pnlImportExportLayout.setVerticalGroup(
             pnlImportExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlImportExportLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlImportExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(pnlExport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(pnlImport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         tabPane.addTab("Import/Export Data", pnlImportExport);
 
         pnlClubManagement.addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentShown(java.awt.event.ComponentEvent evt) {
                 pnlClubManagementComponentShown(evt);
             }
         });
         pnlClubManagement.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 pnlClubManagementFocusGained(evt);
             }
         });
 
         pnlGymnast.setBorder(javax.swing.BorderFactory.createTitledBorder("Gymnast"));
 
         lblGymnast.setText("Gymnast:");
 
         drpGymnastName.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 drpGymnastNameActionPerformed(evt);
             }
         });
 
         btnAddGymnast.setText("Add New User");
         btnAddGymnast.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddGymnastActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout pnlGymnastLayout = new javax.swing.GroupLayout(pnlGymnast);
         pnlGymnast.setLayout(pnlGymnastLayout);
         pnlGymnastLayout.setHorizontalGroup(
             pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlGymnastLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblGymnast)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(drpGymnastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(42, 42, 42)
                 .addComponent(btnAddGymnast, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(480, Short.MAX_VALUE))
         );
         pnlGymnastLayout.setVerticalGroup(
             pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlGymnastLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblGymnast)
                     .addComponent(drpGymnastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnAddGymnast))
                 .addContainerGap(65, Short.MAX_VALUE))
         );
 
         pnlAdmin.setBorder(javax.swing.BorderFactory.createTitledBorder("Administration"));
 
         lblNewPassword.setText("New Password:");
 
         lblNewPassword2.setText("Retype Password:");
 
         btnResetAll.setText("Reset All Data");
         btnResetAll.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnResetAllActionPerformed(evt);
             }
         });
 
         btnNewPassword.setText("New Password");
 
         btnLogout.setText("Logout");
         btnLogout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnLogoutActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout pnlAdminLayout = new javax.swing.GroupLayout(pnlAdmin);
         pnlAdmin.setLayout(pnlAdminLayout);
         pnlAdminLayout.setHorizontalGroup(
             pnlAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlAdminLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblNewPassword)
                 .addGap(32, 32, 32)
                 .addComponent(txtPassword1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(69, 69, 69)
                 .addComponent(lblNewPassword2)
                 .addGap(37, 37, 37)
                 .addComponent(txtPassword2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(btnNewPassword)
                 .addGap(99, 99, 99)
                 .addComponent(btnResetAll)
                 .addGap(52, 52, 52)
                 .addComponent(btnLogout)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         pnlAdminLayout.setVerticalGroup(
             pnlAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(lblNewPassword)
                 .addComponent(txtPassword1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(btnResetAll)
                 .addComponent(btnNewPassword)
                 .addComponent(txtPassword2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(lblNewPassword2)
                 .addComponent(btnLogout))
         );
 
         pnlGymnastDetails.setBorder(javax.swing.BorderFactory.createTitledBorder("Gymnast Details"));
 
         lblGymnastName.setText("Name:");
 
         lblDob.setText("Date of Birth:");
 
         lblCategory.setText("Category:");
 
         btnAddModifyUser.setText("Add / Modify User");
         btnAddModifyUser.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddModifyUserActionPerformed(evt);
             }
         });
 
         btnDeleteUser.setText("Delete User");
         btnDeleteUser.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDeleteUserActionPerformed(evt);
             }
         });
 
         lblClubName.setText("Club:");
 
         drpClubs.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
 
         javax.swing.GroupLayout pnlGymnastDetailsLayout = new javax.swing.GroupLayout(pnlGymnastDetails);
         pnlGymnastDetails.setLayout(pnlGymnastDetailsLayout);
         pnlGymnastDetailsLayout.setHorizontalGroup(
             pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlGymnastDetailsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lblGymnastName)
                     .addComponent(lblDob)
                     .addComponent(lblClubName))
                 .addGap(36, 36, 36)
                 .addGroup(pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlGymnastDetailsLayout.createSequentialGroup()
                         .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(101, 101, 101)
                         .addComponent(lblCategory)
                         .addGap(18, 18, 18)
                         .addComponent(drpCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(pnlGymnastDetailsLayout.createSequentialGroup()
                         .addGroup(pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addComponent(drpClubs, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addGroup(pnlGymnastDetailsLayout.createSequentialGroup()
                                 .addComponent(drpDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(drpMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(drpYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGroup(pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(pnlGymnastDetailsLayout.createSequentialGroup()
                                 .addGap(444, 444, 444)
                                 .addComponent(btnAddModifyUser))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlGymnastDetailsLayout.createSequentialGroup()
                                 .addGap(474, 474, 474)
                                 .addComponent(btnDeleteUser)))))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         pnlGymnastDetailsLayout.setVerticalGroup(
             pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlGymnastDetailsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblGymnastName)
                     .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lblCategory)
                     .addComponent(drpCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblDob)
                     .addComponent(drpDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(drpMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(drpYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnAddModifyUser))
                 .addGap(18, 18, 18)
                 .addGroup(pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(btnDeleteUser)
                     .addGroup(pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(lblClubName)
                         .addComponent(drpClubs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pnlRoutines.setBorder(javax.swing.BorderFactory.createTitledBorder("Routines"));
 
         javax.swing.GroupLayout pnlRoutinesLayout = new javax.swing.GroupLayout(pnlRoutines);
         pnlRoutines.setLayout(pnlRoutinesLayout);
         pnlRoutinesLayout.setHorizontalGroup(
             pnlRoutinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlRoutinesLayout.createSequentialGroup()
                 .addGap(124, 124, 124)
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         pnlRoutinesLayout.setVerticalGroup(
             pnlRoutinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlRoutinesLayout.createSequentialGroup()
                 .addGap(82, 82, 82)
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pnlClub.setBorder(javax.swing.BorderFactory.createTitledBorder("Club"));
 
         lblClub.setText("Club:");
 
         drpClubName.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 drpClubNameActionPerformed(evt);
             }
         });
 
         btnAddClub.setText("Add New Club");
         btnAddClub.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddClubActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout pnlClubLayout = new javax.swing.GroupLayout(pnlClub);
         pnlClub.setLayout(pnlClubLayout);
         pnlClubLayout.setHorizontalGroup(
             pnlClubLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlClubLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblClub)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(drpClubName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(42, 42, 42)
                 .addComponent(btnAddClub, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(480, Short.MAX_VALUE))
         );
         pnlClubLayout.setVerticalGroup(
             pnlClubLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlClubLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlClubLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblClub)
                     .addComponent(drpClubName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnAddClub))
                 .addContainerGap(65, Short.MAX_VALUE))
         );
 
         pnlClubDetails.setBorder(javax.swing.BorderFactory.createTitledBorder("Club Details"));
 
         javax.swing.GroupLayout pnlClubDetailsLayout = new javax.swing.GroupLayout(pnlClubDetails);
         pnlClubDetails.setLayout(pnlClubDetailsLayout);
         pnlClubDetailsLayout.setHorizontalGroup(
             pnlClubDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 100, Short.MAX_VALUE)
         );
         pnlClubDetailsLayout.setVerticalGroup(
             pnlClubDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 100, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout pnlClubManagementLayout = new javax.swing.GroupLayout(pnlClubManagement);
         pnlClubManagement.setLayout(pnlClubManagementLayout);
         pnlClubManagementLayout.setHorizontalGroup(
             pnlClubManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlClubManagementLayout.createSequentialGroup()
                 .addGroup(pnlClubManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(pnlGymnastDetails, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(pnlClubManagementLayout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(pnlClubManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(pnlRoutines, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(pnlAdmin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                     .addGroup(pnlClubManagementLayout.createSequentialGroup()
                         .addGroup(pnlClubManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(pnlClubManagementLayout.createSequentialGroup()
                                 .addComponent(pnlGymnast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(27, 27, 27)
                                 .addComponent(pnlClub, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(pnlClubManagementLayout.createSequentialGroup()
                                 .addGap(438, 438, 438)
                                 .addComponent(pnlClubDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         pnlClubManagementLayout.setVerticalGroup(
             pnlClubManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlClubManagementLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlClubManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(pnlGymnast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(pnlClubManagementLayout.createSequentialGroup()
                         .addGap(16, 16, 16)
                         .addComponent(pnlClub, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addGap(24, 24, 24)
                 .addComponent(pnlClubDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(86, 86, 86)
                 .addComponent(pnlGymnastDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(pnlRoutines, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(pnlAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(696, Short.MAX_VALUE))
         );
 
         tabPane.addTab("Club Management", pnlClubManagement);
 
         tabPane.setBounds(0, 10, 1280, 1650);
         layMainLayer.add(tabPane, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         lblError.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
         lblError.setForeground(new java.awt.Color(255, 0, 0));
         lblError.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lblErrorMouseClicked(evt);
             }
         });
         lblError.setBounds(460, 10, 420, 20);
         layMainLayer.add(lblError, javax.swing.JLayeredPane.POPUP_LAYER);
 
         jMenu3.setText("File");
 
         jMenuItem1.setText("jMenuItem1");
         jMenu3.add(jMenuItem1);
 
         menBar.add(jMenu3);
 
         jMenu4.setText("Edit");
         menBar.add(jMenu4);
 
         setJMenuBar(menBar);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(layMainLayer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(layMainLayer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnSaveCommentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveCommentsActionPerformed
         // CURRENTLY THE ROUTINE ID OF DISPLAYED ROUTINE IS IN THE VARIABLE currentRoutineId_
         db_.addComments(currentRoutineId_,txtComments.getText());
     }//GEN-LAST:event_btnSaveCommentsActionPerformed
 
     private void btnCollectDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCollectDataActionPerformed
         this.btnClearDataActionPerformed(evt);
         if(this.currentInterface_!=null){
             this.currentInterface_.collectBounces(Integer.parseInt(txtNumberOfBounces.getText()), this.db_, 1);
             refresh = REFRESH_TIME;
             nextJumpToFill = 1;
             btnSaveComments.setVisible(false);
             btnClearComments.setVisible(false);
             sclComments.setVisible(false);
             txtComments.setVisible(false);
             lblComments.setVisible(false);
             jumpTimer.start();
         }
     }//GEN-LAST:event_btnCollectDataActionPerformed
 
     private void txtNumberOfBouncesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNumberOfBouncesActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_txtNumberOfBouncesActionPerformed
 
     private void drpDeviceNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drpDeviceNameActionPerformed
         if(!(drpDeviceName.getSelectedItem().toString().equals("<<No ToF Connected>>"))){
             this.currentInterface_ = this.stringToTof(drpDeviceName.getSelectedItem().toString());
             drpSelectGymnast.setVisible(true);
             lblSelectGymnast.setVisible(true);
         }       
     }//GEN-LAST:event_drpDeviceNameActionPerformed
 
     private void btnAddModifyUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddModifyUserActionPerformed
         if (drpGymnastName.getSelectedIndex() == 0) {
             if (txtName.getText() == "") {
                 messageHandler_.setError(14);
             } else {
                 messageHandler_.clearError();
                 //Then we need to add the gymnast. Start by entering the information into the databse. 
                db_.addGymnast(txtName.getText(), Integer.parseInt(drpDate.getSelectedItem().toString()), Integer.parseInt(drpMonth.getSelectedItem().toString()), Integer.parseInt(drpYear.getSelectedItem().toString()), db_.getCategoryID(drpCategory.getSelectedItem().toString())+"");
                 if(!(messageHandler_.isError())){
                     //Add a success message.
                     messageHandler_.setError(12);
 
                     //Then clear all the items. 
                     txtName.setText("");
                     drpDate.setSelectedIndex(0);
                     drpMonth.setSelectedIndex(0);
                     drpYear.setSelectedIndex(0);
                     drpCategory.setSelectedIndex(0);
                     drpGymnastName.setSelectedIndex(0);
                     drpClubs.setSelectedIndex(0);
                     gymnastPanelVisible(false);
 
                     //Re-update the drop-down.
                     updateGymnastDropDown();
                     updateCategoryDropDown();
                 }
             }
         } else {
             //Then we need to edit the gymnast. 
             ComboItem gymnastItem = (ComboItem) drpGymnastName.getSelectedItem();
             messageHandler_.clearError();
             //Note that we have to take 1 off the date and month because of offsets. 
             
            db_.editGymnast(gymnastItem.getNumericID(), txtName.getText(), Integer.parseInt(drpDate.getSelectedItem().toString())-1, Integer.parseInt(drpMonth.getSelectedItem().toString())-1, Integer.parseInt(drpYear.getSelectedItem().toString()), db_.getCategoryID(drpCategory.getSelectedItem().toString())+"", 1);
             if(!(messageHandler_.isError())){
                 messageHandler_.setError(13);
             
                 //Then clear all the items. 
                 txtName.setText("");
                 drpDate.setSelectedIndex(0);
                 drpMonth.setSelectedIndex(0);
                 drpYear.setSelectedIndex(0);
                 drpCategory.setSelectedIndex(0);
                 drpGymnastName.setSelectedIndex(0);
                 drpClubs.setSelectedIndex(0);
                 gymnastPanelVisible(false);
 
                 //Re-update the drop-down.
                 updateGymnastDropDown();
             }
         }
     }//GEN-LAST:event_btnAddModifyUserActionPerformed
 
     private void btnUpdateImagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateImagesActionPerformed
        bounceCounter_.loadImages();
     }//GEN-LAST:event_btnUpdateImagesActionPerformed
 
     private void tabPaneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tabPaneFocusGained
         // TODO add your handling code here:
     }//GEN-LAST:event_tabPaneFocusGained
 
     private void btnClearCommentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearCommentsActionPerformed
         txtComments.setText("");
 		
 		Random random = new Random();
 		int i;
 		double j;
 		
 		//THIS IS CODE FOR ANDREAS TO TEST STUFF
 		pnlGraph.removeAll();
 		i = random.nextInt(10);
 		j = 6+Math.random()*2;
 		chartObject_.setTitle("title+"+i+" num m= "+chartObject_.getNumberOfItems());
 		chartObject_.updateValue(j, i);
 		JFreeChart jChart = chartObject_.createChart();
 		ChartPanel CP = new ChartPanel(jChart);
                 pnlGraph.add(CP);
 		pnlGraph.validate();
     }//GEN-LAST:event_btnClearCommentsActionPerformed
 
     private void btnClearDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearDataActionPerformed
         for(int i=0;i<10;i++){
             labelArray_[i*5].setVisible(false);
             labelArray_[i*5].setForeground(new java.awt.Color(0,0,0));
             labelArray_[i*5+1].setText("");
             labelArray_[i*5+1].setForeground(new java.awt.Color(0,0,0));
             labelArray_[i*5+2].setText("");
             labelArray_[i*5+2].setForeground(new java.awt.Color(0,0,0));
             labelArray_[i*5+3].setText("");
             labelArray_[i*5+3].setForeground(new java.awt.Color(0,0,0));
             labelArray_[i*5+4].setIcon(null);
         }
         lblAvToFNo.setText("");
         lblAvToNNo.setText("");
         lblAvTotalNo.setText("");
         lblOvToFNo.setText("");
         lblOvToNNo.setText("");
         lblOvTotalNo.setText("");
         lblHighestToFNo.setText("");
         lblLowestToFNo.setText("");
         lblFurthestNo.setText("");
         lblAvPositionNo.setText("");
         lblLargestPositionNo.setText("");
         lblSmallestPositionNo.setText("");
         
         lblComments.setVisible(false);
         sclComments.setVisible(false);
         txtComments.setVisible(false);
         btnClearComments.setVisible(false);
         btnSaveComments.setVisible(false);
         btnClearData.setVisible(false);
     }//GEN-LAST:event_btnClearDataActionPerformed
 
     private void btnStatisticsUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatisticsUpdateActionPerformed
         ComboItem currentItem = (ComboItem)drpStatsRoutine.getSelectedItem();
 		
 		if (currentItem != null) {
 			int routineID = currentItem.getNumericID();
 			chartObjectStats_ = new Chart(db_.getRoutine(routineID));
 			pnlStatisticsSmall.setLayout(new java.awt.BorderLayout());
 			JFreeChart jChart = chartObjectStats_.createChart();
 			ChartPanel CP = new ChartPanel(jChart);
 			pnlStatisticsSmall.removeAll();
 			pnlStatisticsSmall.add(CP);
 			pnlStatisticsSmall.validate();
 		}
     }//GEN-LAST:event_btnStatisticsUpdateActionPerformed
 
     private void pnlClubManagementFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pnlClubManagementFocusGained
         // TODO add your handling code here:
     }//GEN-LAST:event_pnlClubManagementFocusGained
 
     private void pnlClubManagementComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pnlClubManagementComponentShown
         // TODO add your handling code here:
         if(this.adminAccessGranted_){
             setAdminAccess(true);
         }else{
             PasswordPrompt passwordPopup = new PasswordPrompt(this, true, adminPassword_);
             passwordPopup.setVisible(true);
             //switch(passwordPopup.getReturnStatus()){
             switch(1){
                 case 0:
                     //BAD PASSWORD
                     setAdminAccess(false);
                     this.messageHandler_.setError(1);
                     break;
                 case 1:
                     setAdminAccess(true);
                     this.messageHandler_.clearError();
                     break;
             }
         }
     }//GEN-LAST:event_pnlClubManagementComponentShown
 
     private void TrampolineUIWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_TrampolineUIWindowOpened
         // TODO add your handling code here:
     }//GEN-LAST:event_TrampolineUIWindowOpened
 
     private void lblErrorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblErrorMouseClicked
         if(messageHandler_.isError()){
             JOptionPane.showMessageDialog(this,this.messageHandler_.getCurrentErrorLong(),this.messageHandler_.getCurrentErrorShort(), JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_lblErrorMouseClicked
 
     private void btnDeleteUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteUserActionPerformed
         ComboItem c = (ComboItem) drpGymnastName.getSelectedItem();
         int confirmInt = (int) JOptionPane.showConfirmDialog(pnlStatistics, "Are you sure you want to Delete '"+c+"'?", "Delete User", 0, 0);
         
         if (confirmInt == 0) {
             messageHandler_.setError(15);
             db_.deleteGymnast(c.getNumericID());
         }
         
         updateGymnastDropDown();
     }//GEN-LAST:event_btnDeleteUserActionPerformed
 
     private void drpGymnastNameActionPerformed(java.awt.event.ActionEvent evt) {                                               
         ComboItem c = (ComboItem) drpGymnastName.getSelectedItem();
         
         if (c != null) {
             if (c.getNumericID() == 0) {
                 btnAddGymnastActionPerformed(evt);
             } else {
                 Gymnast g = db_.getGymnast(c.getNumericID());
                 txtName.setText(g.getName());
 
                 drpDate.setSelectedIndex(g.getDobDay());
                 drpMonth.setSelectedIndex(g.getDobMonth());
                 drpYear.setSelectedIndex(g.getDobYear()-1900);
                 drpCategory.setSelectedIndex(g.getCategory());
                 btnAddModifyUser.setText("Modify User");
                 gymnastPanelVisible(true);
             }
         }
     }                                           
 
     private void drpStatsGymnastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drpStatsGymnastActionPerformed
         //Get the selected gymnast. 
         ComboItem c = (ComboItem) drpStatsGymnast.getSelectedItem();
         
         if (c != null && c.getNumericID() != 0) {
             Gymnast g = db_.getGymnast(c.getNumericID());
 			
 			System.out.println("Selected gymnastID "+c.getNumericID()+" in selStatsGymnastActionPerformed in UI.java");
 
             //Update the routine drop-down. 
             Routine[] routineList = db_.getRoutinesForGymnast(g.getID());
             drpStatsRoutine.removeAllItems();
             for (Routine r:routineList) {
                 drpStatsRoutine.addItem(new ComboItem(r.getID(), "ID: "+r.getID()));
             }
         }
     }//GEN-LAST:event_drpStatsGymnastActionPerformed
 
     private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
         setAdminAccess(false);
         this.messageHandler_.setError(4);
     }//GEN-LAST:event_btnLogoutActionPerformed
 
     private void btnResetAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetAllActionPerformed
         
         //Password currently hardcoded to "MasterReset" -> to be retrived from database later
         ResetAllConfirm resetDialog = new ResetAllConfirm(this, true, "D642FBD7CACF27F7568A6ED701AA44D8");
         resetDialog.setVisible(true);
         switch(resetDialog.getReturnStatus()){
             case 0:
                 // Bad password
                 messageHandler_.setError(16);
                 break;
             case 1:
                 //Correct password
                 Object[] options = {"Yes","No"};
                 int finalConfirm = JOptionPane.showOptionDialog(this,"Continuing will erase ALL DATA stored\n"
                     + "in the database. Do you wish to continue?\n","Final Confirm of Master Reset" ,
                     JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
 
                 if(finalConfirm == JOptionPane.YES_OPTION){
                     //Confirmed to delete all the database data
                     messageHandler_.setError(18);
                 }else{
                     messageHandler_.setError(17);
                 }
                 break;
                 //Display popup for confirmation
         }   
     }//GEN-LAST:event_btnResetAllActionPerformed
 
     private void drpSelectGymnastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drpSelectGymnastActionPerformed
         // TODO add your handling code here:
         ComboItem currentItem = (ComboItem)drpSelectGymnast.getSelectedItem();
         
         if (currentItem != null) {
             if(!(currentItem.getName().equals("<< Please Select Gymnast >>"))){
                 Map<Integer, String> tagMapToF = db_.getTags(Integer.parseInt(currentItem.getID()));
 
                 DefaultListModel lstTagsModel = new DefaultListModel();
                 for(int tagId : tagMapToF.keySet()){
                     ComboItem newTag = new ComboItem(tagId,tagMapToF.get(tagId));
                     lstTagsModel.addElement(newTag);
                 }
 
                 lstTags.setModel(lstTagsModel);
 
                 String firstItem = drpSelectGymnast.getItemAt(0).toString();
                 if(firstItem.equals("<< Please Select Gymnast >>")){
                     drpSelectGymnast.removeItemAt(0);
                 }
 
                 lblNumberOfBounces.setVisible(true);
                 txtNumberOfBounces.setVisible(true);
                 lblTags.setVisible(true);
                 lblAddNewTag.setVisible(true);
                 sclTags.setVisible(true);
                 lstTags.setVisible(true);
                 btnCollectData.setVisible(true);
             }
         }
     }//GEN-LAST:event_drpSelectGymnastActionPerformed
 
     private void lblAddNewTagMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAddNewTagMouseClicked
         // TODO add your handling code here:
         String newTag = (String)JOptionPane.showInputDialog(this,"Please enter the name for the new Tag:","Add New Tag",
                                                             JOptionPane.QUESTION_MESSAGE,null,null,"");
         if(newTag.equals("")){
             messageHandler_.setError(11);
         }else{
             ComboItem currentGymnast = (ComboItem)drpSelectGymnast.getSelectedItem();
             Map<Integer, String> tagMapToF = db_.getTags(Integer.parseInt(currentGymnast.getID()));
             
             boolean differentName = true;
             for(String tags : tagMapToF.values()){
                 if(tags.equals(newTag)){
                     differentName = false;
                 }
             }
             
             if(differentName){
                 db_.addTag(Integer.parseInt(currentGymnast.getID()),newTag);
                 this.drpSelectGymnastActionPerformed(null);
             }else{
                 messageHandler_.setError(11);
             }
         }
     }//GEN-LAST:event_lblAddNewTagMouseClicked
 
 	private void drpStatsRoutineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drpStatsRoutineActionPerformed
 		//DONT USE THIS TO UPDATE IT USE A BUTTON INSTEAD
 	}//GEN-LAST:event_drpStatsRoutineActionPerformed
 
     private void btnAddGymnastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddGymnastActionPerformed
         btnAddModifyUser.setText("Add User");
         drpGymnastName.setSelectedIndex(0);
         txtName.setText("");
         drpDate.setSelectedIndex(0);
         drpMonth.setSelectedIndex(0);
         drpYear.setSelectedIndex(0);
         drpCategory.setSelectedIndex(0);
         drpClubs.setSelectedIndex(0);
         gymnastPanelVisible(true);
     }//GEN-LAST:event_btnAddGymnastActionPerformed
 
     private void drpClubNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drpClubNameActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_drpClubNameActionPerformed
 
     private void btnAddClubActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddClubActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_btnAddClubActionPerformed
    
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /*
          * Set the Nimbus look and feel
          */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /*
          * If Nimbus (introduced in Java SE 6) is not available, stay with the
          * default look and feel. For details see
          * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(TrampolineUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(TrampolineUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(TrampolineUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(TrampolineUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
         /*
          * Create and display the form
          */    
         JFrame.setDefaultLookAndFeelDecorated(true);
         splashInit();           // initialize splash overlay drawing parameters
                
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 new TrampolineUI().setVisible(true);
             }
         });
     }
     
     private static void splashInit(){
         // the splash screen object is created by the JVM, if it is displaying a splash image
         
         mySplash_ = SplashScreen.getSplashScreen();
         // if there are any problems displaying the splash image
         // the call to getSplashScreen will returned null
 
         if (mySplash_ != null)
         {
             // get the size of the image now being displayed
             Dimension ssDim = mySplash_.getSize();
             int height = ssDim.height;
             int width = ssDim.width;
             
             // stake out some area for our status information
             splashTextArea = new Rectangle2D.Double(15., height*0.898, width * .45, 20);
             
             splashProgressArea = new Rectangle2D.Double(width * .55, height*.92, width*.4, 12);
 
             // create the Graphics environment for drawing status info
             splashGraphics = mySplash_.createGraphics();
             font = new Font("Dialog", Font.PLAIN, 14);
             splashGraphics.setFont(font);
 
             // initialize the status info
             splashText("Starting...");
             splashProgress(0);
         }
     }
     
     public static void splashText(String str)
     {
         if (mySplash_ != null && mySplash_.isVisible())
         {   // important to check here so no other methods need to know if there
             // really is a Splash being displayed
 
             // erase the last status text
             splashGraphics.setPaint(Color.WHITE);
             splashGraphics.fill(splashTextArea);
 
             // draw the text
             splashGraphics.setPaint(Color.BLUE);
             splashGraphics.drawString(str, (int)(splashTextArea.getX() + 10),(int)(splashTextArea.getY() + 15));
 
             // make sure it's displayed
             mySplash_.update();
         }
     }
     
      public static void splashProgress(int pct)
     {
         if (mySplash_ != null && mySplash_.isVisible())
         {
 
             // Note: 3 colors are used here to demonstrate steps
             // erase the old one
             splashGraphics.setPaint(Color.WHITE);
             splashGraphics.fill(splashProgressArea);
 
             // draw an outline
             splashGraphics.setPaint(Color.BLACK);
             splashGraphics.draw(splashProgressArea);
 
             // Calculate the width corresponding to the correct percentage
             int x = (int) splashProgressArea.getMinX();
             int y = (int) splashProgressArea.getMinY();
             int wid = (int) splashProgressArea.getWidth();
             int hgt = (int) splashProgressArea.getHeight();
 
             int doneWidth = Math.round(pct*wid/100.f);
             doneWidth = Math.max(0, Math.min(doneWidth, wid-1));  // limit 0-width
 
             // fill the done part one pixel smaller than the outline
             splashGraphics.setPaint(Color.RED);
             splashGraphics.fillRect(x, y+1, doneWidth, hgt-1);
 
             // make sure it's displayed
             mySplash_.update();
         }
     }
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnAddClub;
     private javax.swing.JButton btnAddGymnast;
     private javax.swing.JButton btnAddModifyUser;
     private javax.swing.JButton btnClearComments;
     private javax.swing.JButton btnClearData;
     private javax.swing.JButton btnCollectData;
     private javax.swing.JButton btnDeleteUser;
     private javax.swing.JButton btnExportUser;
     private javax.swing.JButton btnLogout;
     private javax.swing.JButton btnNewPassword;
     private javax.swing.JButton btnResetAll;
     private javax.swing.JButton btnSaveComments;
     private javax.swing.JButton btnStatisticsUpdate;
     private javax.swing.JButton btnUpdateImages;
     private javax.swing.JComboBox drpCategory;
     private javax.swing.JComboBox drpClubName;
     private javax.swing.JComboBox drpClubs;
     private javax.swing.JComboBox drpDate;
     private javax.swing.JComboBox drpDeviceName;
     private javax.swing.JComboBox drpGymnastName;
     private javax.swing.JComboBox drpMonth;
     private javax.swing.JComboBox drpSelectGymnast;
     private javax.swing.JComboBox drpStatsGymnast;
     private javax.swing.JComboBox drpStatsRoutine;
     private javax.swing.JComboBox drpYear;
     private javax.swing.JMenu jMenu3;
     private javax.swing.JMenu jMenu4;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JLayeredPane layBeamStatus;
     private javax.swing.JLayeredPane layMainLayer;
     private javax.swing.JLabel lblAddNewTag;
     private javax.swing.JLabel lblAvPositionNo;
     private javax.swing.JLabel lblAvPositionTxt;
     private javax.swing.JLabel lblAvToFNo;
     private javax.swing.JLabel lblAvToFTxt;
     private javax.swing.JLabel lblAvToNNo;
     private javax.swing.JLabel lblAvToNTxt;
     private javax.swing.JLabel lblAvTotalNo;
     private javax.swing.JLabel lblAvTotalTxt;
     private javax.swing.JLabel lblCategory;
     private javax.swing.JLabel lblClub;
     private javax.swing.JLabel lblClubName;
     private javax.swing.JLabel lblComments;
     private javax.swing.JLabel lblDob;
     private javax.swing.JLabel lblError;
     private javax.swing.JLabel lblFurthestNo;
     private javax.swing.JLabel lblFurthestTxt;
     private javax.swing.JLabel lblGymnast;
     private javax.swing.JLabel lblGymnastName;
     private javax.swing.JLabel lblHighestToFNo;
     private javax.swing.JLabel lblHighestToFTxt;
     private javax.swing.JLabel lblLargestPositionNo;
     private javax.swing.JLabel lblLargestPositionTxt;
     private javax.swing.JLabel lblLocation;
     private javax.swing.JLabel lblLowestToFNo;
     private javax.swing.JLabel lblLowestToFTxt;
     private javax.swing.JLabel lblNewPassword;
     private javax.swing.JLabel lblNewPassword2;
     private javax.swing.JLabel lblNumberOfBounces;
     private javax.swing.JLabel lblOvToFNo;
     private javax.swing.JLabel lblOvToFTxt;
     private javax.swing.JLabel lblOvToNNo;
     private javax.swing.JLabel lblOvToNTxt;
     private javax.swing.JLabel lblOvTotalNo;
     private javax.swing.JLabel lblOvTotalTxt;
     private javax.swing.JLabel lblSelectGymnast;
     private javax.swing.JLabel lblSelectTof;
     private javax.swing.JLabel lblSmallestPositionNo;
     private javax.swing.JLabel lblSmallestPositionTxt;
     private javax.swing.JLabel lblStatsGymnast;
     private javax.swing.JLabel lblStatsRoutine;
     private javax.swing.JLabel lblTags;
     private javax.swing.JLabel lblTof;
     private javax.swing.JLabel lblTon;
     private javax.swing.JLabel lblTotal;
     private javax.swing.JLabel lblTrampoline;
     private javax.swing.JList lstTags;
     private javax.swing.JMenuBar menBar;
     private javax.swing.JPanel pnlAdmin;
     private javax.swing.JPanel pnlClub;
     private javax.swing.JPanel pnlClubDetails;
     private javax.swing.JPanel pnlClubManagement;
     private javax.swing.JPanel pnlData;
     private javax.swing.JPanel pnlDataTable;
     private javax.swing.JPanel pnlExport;
     private javax.swing.JPanel pnlGraph;
     private javax.swing.JPanel pnlGymnast;
     private javax.swing.JPanel pnlGymnastDetails;
     private javax.swing.JPanel pnlImport;
     private javax.swing.JPanel pnlImportExport;
     private javax.swing.JPanel pnlRoutines;
     private javax.swing.JPanel pnlStart;
     private javax.swing.JPanel pnlStatistics;
     private javax.swing.JPanel pnlStatisticsSmall;
     private javax.swing.JPanel pnlStats;
     private javax.swing.JPanel pnlToF;
     private javax.swing.JRadioButton rdoExportCsv;
     private javax.swing.JRadioButton rdoExportExcel;
     private javax.swing.JRadioButton rdoExportText;
     private javax.swing.JScrollPane sclComments;
     private javax.swing.JScrollPane sclTags;
     private javax.swing.JTabbedPane tabPane;
     private javax.swing.JTextArea txtComments;
     private javax.swing.JTextField txtName;
     private javax.swing.JTextField txtNumberOfBounces;
     private javax.swing.JTextField txtPassword1;
     private javax.swing.JTextField txtPassword2;
     // End of variables declaration//GEN-END:variables
 }
