 package thegui;
 import java.awt.*;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.Vector;
 
 /**
  *
  * @author Aki Stankoski and Dennis Huynh
  */
 
 public class TheGUI{
  	//declare the panels and buttons to be accessed from multiple methods  
 	JFrame frame         =      new JFrame("UCSB Campus Map");//main frame
 	JPanel thePanel      =      new JPanel();//Back homescreen panel
 	JPanel newPanel      =      new JPanel();//New panel when button is clicked
 	JPanel leftPanel     =      new JPanel();//Left homescreen panel
 	JPanel rightPanel    =      new JPanel();//Right homescreen panel
 	JPanel bottomPanel   =      new JPanel();//Bottom subpanels
 	JPanel topPanel      =      new JPanel();//Top subpanels
       JPanel infoPanel     =      new JPanel();//info panel on subpanels
       JTextField searchBar =      new JTextField(20);//Seach bar 
 
 	static JButton T387     =      new JButton("387");
 	static JButton T429     =      new JButton("429");
 	static JButton BRDA     =      new JButton("BRDA");
 	static JButton BSIF     =      new JButton("BSIF");
 	static JButton GIRV     =      new JButton("GIRV");
 	static JButton HSSB     =      new JButton("HSSB");
 	static JButton HFH      =      new JButton("HFH");
 	static JButton KERR     =      new JButton("KERR");
 	static JButton LLCH     =      new JButton("LLCH");
 	static JButton PHELP    =      new JButton("PHELP");
	static JButton NORTH    =       new JButton("NORTH");
	static JButton SOUTH    = 	new JButton("SOUTH");
	static JButton CAMPBELL = 	new JButton("CAMPBELL");
 	static JButton ENGRSCI  =	 new JButton("ENGRSCI");
 	static JButton ENGR2 	=	 new JButton("ENGR2");
 	static JButton LIBRARY 	=	 new JButton ("LIBRARY");
 	JButton cancel          =      new JButton("Cancel");//cancel button for subscreens
 
       JLabel searchLabel   =      new JLabel("Search: ");
 	JLabel T387Label     =      new JLabel("387 - Trailer 387");
 	JLabel T429Label     =      new JLabel("429 - Trailer 429");
 	JLabel BRDALabel     =      new JLabel("BRDA - Broida Hall");
 	JLabel BSIFLabel     =      new JLabel("BSIF - Biological Sciences Instruction Facility");
 	JLabel GIRVLabel     =      new JLabel("GIRV - Girvetz Hall");
 	JLabel HSSBLabel     =      new JLabel("HSSB - Humanities and Social Sciences Building");
 	JLabel HFHLabel      =      new JLabel("HFH - Harold Frank Hall");
 	JLabel KERRLabel     =      new JLabel("KERR - Kerr Hall");
 	JLabel LLCHLabel     =      new JLabel("LLCH - Lotte-Lehmann Concert Hall");
 	JLabel PHELPLabel    =      new JLabel("PHELP - Phelps Hall");
	JLabel NORTHLabel    =     new JLabel("NORTH - North Hall");
	JLabel SOUTHLabel	= 		new JLabel("SOUTH - South Hall");
	JLabel CAMPBELLLabel =  new JLabel("CAMPBELL - Campbell Hall");
	JLabel ENGRSCILabel = 	new JLabel("ENGRSCI - Engineering Science Building");
	JLabel ENGR2Label 	= 		new JLabel("ENGR2 - Engineering II Building");
	JLabel LIBRARYLabel = 	new JLabel("LIBRARY - Davidson Library");
 
       //Arrays of building names and abbreviations and buttons
       static ArrayList<String> bldgNames = new ArrayList<String>();
       static ArrayList<String> bldgAbbrs = new ArrayList<String>();
       static ArrayList<JButton> buttons = new ArrayList<JButton>();
       
       static {
             bldgNames.add("Trailer 387");
             bldgAbbrs.add("387");
             bldgNames.add("Trailer 429");
             bldgAbbrs.add("429");
             bldgNames.add("Broida Hall");
             bldgAbbrs.add("BRDA");
             bldgNames.add("Biological Sciences Instruction Facility");
             bldgAbbrs.add("BSIF");
             bldgNames.add("Davidson Library");
             bldgAbbrs.add("LIBRARY");
             bldgNames.add("Engineering Science Building");
             bldgAbbrs.add("ENGRSCI");
             bldgNames.add("Engineering II Building");
             bldgAbbrs.add("ENGR2");
             bldgNames.add("Girvetz Hall");
             bldgAbbrs.add("GIRV");
             bldgNames.add("Harold Frank Hall");
             bldgAbbrs.add("HFH");
             bldgNames.add("Humanities and Social Sciences Building");
             bldgAbbrs.add("HSSB");
             bldgNames.add("Kerr Hall");
             bldgAbbrs.add("KERR");
             bldgNames.add("Lotte-Lehmann Concert Hall");
             bldgAbbrs.add("LLCH");
             bldgNames.add("North Hall");
             bldgAbbrs.add("NORTH");
             bldgNames.add("Phelps Hall");
             bldgAbbrs.add("PHELP");
 		bldgNames.add("South Hall");
 		bldgAbbrs.add("SOUTH");
 
 
             buttons.add(T387);
             buttons.add(T429);
             buttons.add(BRDA);
             buttons.add(BSIF);
             buttons.add(ENGRSCI);
             buttons.add(ENGR2);
             buttons.add(GIRV);
             buttons.add(HFH);
             buttons.add(HSSB);
             buttons.add(KERR);
            buttons.add(NORTH);
            buttons.add(LIBRARY);
             buttons.add(LLCH);
             buttons.add(PHELP);
 		buttons.add(SOUTH);
 
       }
 
         //building information
         String T387Info = 
             "1 Start by making your way towards the University Center.\n"
             +"\n"        
             +"2 Once you get to the University Center make a left.\n"                           
             +"\n" 
             +"3 Coninue walking straight until you get to the bike path. "
             +"Cross the bike path and cross the parking lot.\n"
             +"\n" 
             +"4 Trailer 387 will be the building parallel to the parking"
             + "lot on the right side of the parking lot.";
         
         String T429Info = 
             "1 Start by making your way towards the University Center.\n"
             +"\n"
             +"2 Once you get to the University Center make a left.\n"
             +"\n"   
             +"3 Coninue walking straight until you get to the bike path. "
             +"Cross the bike path and cross the parking lot.\n"
             +"\n"
             +"4 Trailer 429 will be the building in the middle of three "
             + "similar looking buildings. It is an older brown building.\n";
 
         String BRDAInfo = 
             "1 Start out by walking away from the University Center towards "
             +"the Women's Center. Cross the bikepath and get on the sidewalk.\n"
             +"\n"
             +"2 When you get to the sidewalk, turn right and keep going until "
             +"you have the library in front of you and a red metal sculpture on your left.\n"
             +"\n"
             +"3 Keep going until you pass the Arbor and Woodstock's and then take "
             +"the slight right sidewalk. This will take you to a long lawn.\n"
             +"\n"
             +"4 Stay on the left side of the lawn and keep going until you pass a bike parking"
             +" lot and are facing a big square planter. Broida Hall will be on your right.";
         
         String BSIFInfo =
             "1 Start out by walking towards the University Center. "
             +"When you get to the University Center, turn left.\n"
             +"\n"
             +"2 Keep going straight until you get to the parking lot and then turn left.\n"
             +"\n"
             +"3 Make your way onto the sidewalk next to UCEN Road.\n"
             +"\n"
             +"4 Continue on UCEN Road. Pass Santa Rosa Residence Hall on your right, the Psychology "
             +"Building on your left, Parking Lot 7 on your left, and the Life Sciences Building on left.\n"
             +"\n"
             +"5 Turn left after the blue and grey Life Sciences Building, and the Biological Sciences "
             + "Instructional Facility will be on the left.";
         
         String GIRVInfo =
             "1 Start out by walking away from the University Center towards the Women's Center. "
             +"Cross the bikepath and get on the sidewalk.\n"
             +"\n"
             +"2 When you get to the sidewalk, turn right and continue on the sidewalk until you see the building on your left. "
             +"You can also turn left at the red metal sculpture and there is another entrance on that side of the building.\n"; 
         
         String HSSBInfo =  
             "1 Start out by walking away from the University Center towards the Women's Center. "
             +"Cross the bikepath and get on the sidewalk.\n"
             +"\n"
             +"2 When you get to the sidewalk, turn left and continue on the sidewalk for about 100 feet. It will be on your right.";
             
 	String HFHInfo =
             "1 Start out by walking away from the University Center and towards the Women's Center. Find the sidewalk and turn left.\n"
             +"\n"
             +"2 Continue until you get to the library and walk up the steps and through the walkway next to the library.\n"
             +"\n"
             +"3 When you leave this walkway keep going straight until you cross the bike path and find the parking lot.\n"
             +"\n"
             +"4 Now, turn left and follow the bikepath that will be on your left. When the bike path turns right, follow it. "
             +"Broida Hall will be on your left and Webb Hall will be on your right.\n"
             +"\n"
             +"5 Harold Frank Hall will be straight ahead. You will cross the bike path again and be right in front of it.";
        
         String KERRInfo =
             "1 Start out by walking away from the University Center towards the Women's Center. "
             +"Cross the bikepath and get on the sidewalk.\n"
             +"\n"
             +"2 When you get to the sidewalk, turn right and continue on the sidewalk until you see the "
             +"library ahead of you and a red metal sculpture on your left.\n"
             +"\n"
             +"3 Turn left at the red metal sculpture. Continue past the tables and past the Arbor and Woodstocks,"
             +" which will be on your left.\n"
             +"\n"
             +"4 Kerr Hall will be on the left just past Woodstocks. It is a two-story, cement-colored building"
             +" with red accents. If you go too far, you will pass through an enclosed walkway - this is North Hall.";
         
         String LLCHInfo =
             "1 Start by making your way towards the University Center.\n"
             +"\n"
             +"2 Once you get to the University Center make a left.\n"
             +"\n"
             +"3 Continue walking straight until you pass the ATM's on your right and "
             +"see a sidewalk leading to steps on your left.\n"
             +"\n"
             +"4 Take these steps. You will be in the music building. The concert hall is "
             +"through the glass double doors on your left.\n";
                 
         String PHELPInfo =
             "1 Start out by walking away from the University Center towards the Women's Center. "
             +"Cross the bikepath and get on the sidewalk.\n"
             +"\n"
             +"2 When you get to the sidewalk, turn right and continue on the sidewalk until "
             +"you are facing the library and have a red metal sculpture on your left.\n"
             +"\n"
             +"3 At the sculpture turn left. You should pass Girvetz Hall and the Arbor.\n"
             +"\n"
             +"4 As you pass the Arbor, you will want to take a sidewalk that turns slightly right. This sidewalk "
             +"should take you diagonally across the lawn, and you should end up with Buchanan Hall on your left.\n"
             +"\n"
             +"5 Phelps Hall is hidden behind Buchannan Hall. Walk just past Buchannan and turn left. "
             +"You will see a building labeled as Gevirtz Graduate School of Education. This building is actually Phelps Hall. "
             +"Walk through the walkway into the courtyard.";
                 
 		String NORTHInfo = 
 			"1 Start out by walking north towards South Hall. "
 			+"Cross the bikepath at the bike loop and continue walking past South Hall.\n"
 			+"\n"
 			+"2 After you have passed South Hall, continue walking north beside the bike path.\n"
 			+"\n"
 			+"3 When you reach the bike lot near the bus loop, turn right. "
 			+"The building across the small grass field is North Hall.\n";
 		
 		String SOUTHInfo = 
 			"1 Head east alongside the bike path until you reach the bike loop.\n"
 			+"\n"
 			+"2 Turn left and head north across the bike path. "
 			+"The bike loop should be on your right.\n"
 			+"\n"
 			+"3 Continue walking alongside the bike path until it turns right.\n"
 			+"\n"
 			+"4 You will see a bike lot to your right. The building right next to this bike lot is South Hall.\n";
 			
 		String ENGRSCIInfo =
 			"Stub";
 		
 		String ENGR2Info = 
 			"Stub";
 		
 		String LIBRARYInfo =
 			"1 Head east alongside the bike path until you reach the bike loop.\n"
 			+"\n"
 			+"2 Continue east across the bike path and follow the bike path until you reach another bike loop.\n"
 			+"\n"
 			+"3 Head north across the bike path. The bike loop should be on your right.\n"
 			+"\n"
 			+"4 Continue walking north into the open courtyard. The building to your right is Davidson Library. "
 			+"The main entrance is near the center of the side facing you.\n";
 
         //function to set up the homescreen
 	public void setUpHomeScreen() throws IOException{
             guiRemoveAll();
             frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             thePanel.setLayout(new BoxLayout(thePanel, BoxLayout.X_AXIS));
 			thePanel.setBackground(Color.WHITE);
             thePanel.setSize(600,400);
             topPanel.setBackground(Color.WHITE);
             topPanel.setSize(600,50);
             leftPanel.setBackground(Color.WHITE);
             leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
             leftPanel.setSize(300,200);
             rightPanel.setBackground(Color.WHITE);
             rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
             rightPanel.setSize(300,200);
      	 
             //setting size and adding actionlister
             searchBar.addActionListener(new SearchBarActionListener());
 	      //DocumentListener searchBarUpdate;
             searchBar.getDocument().addDocumentListener(new SearchBarDocumentListener());
             T387.setPreferredSize(new Dimension(100,100));
             T387.addActionListener(new T387Listener());
             T429.setPreferredSize(new Dimension(100,100));
             T429.addActionListener(new T429Listener());
             BRDA.setPreferredSize(new Dimension(100,100));
             BRDA.addActionListener(new BRDAListener());
             BSIF.setPreferredSize(new Dimension(100,100));
             BSIF.addActionListener(new BSIFListener());
             GIRV.setPreferredSize(new Dimension(100,100));
             GIRV.addActionListener(new GIRVListener());
             HSSB.setPreferredSize(new Dimension(100,100));
             HSSB.addActionListener(new HSSBListener());
             HFH.setPreferredSize(new Dimension(100,100));
             HFH.addActionListener(new HFHListener());
             LLCH.setPreferredSize(new Dimension(100,100));
             LLCH.addActionListener(new LLCHListener());
             PHELP.setPreferredSize(new Dimension(100,100));
             PHELP.addActionListener(new PHELPListener());
             KERR.setPreferredSize(new Dimension(100,100));
             KERR.addActionListener(new KERRListener());
 		NORTH.setPreferredSize(new Dimension(100,100));
 		NORTH.addActionListener(new NORTHListener());
 		SOUTH.setPreferredSize(new Dimension(100,100));
 		SOUTH.addActionListener(new SOUTHListener());
 		ENGRSCI.setPreferredSize(new Dimension(100,100));
 		ENGRSCI.addActionListener(new ENGRSCIListener());
 		ENGR2.setPreferredSize(new Dimension(100,100));
 		ENGR2.addActionListener(new ENGR2Listener());
 		LIBRARY.setPreferredSize(new Dimension(100,100));
 		LIBRARY.addActionListener(new LIBRARYListener());
 
             //adding panels and setting dimensions
             topPanel.add(searchLabel);
             topPanel.add(searchBar);
             leftPanel.add(T387);
             leftPanel.add(Box.createRigidArea(new Dimension(200,50)));
             leftPanel.add(T429);
             leftPanel.add(Box.createRigidArea(new Dimension(200,50)));
             leftPanel.add(BRDA);
             leftPanel.add(Box.createRigidArea(new Dimension(200,50)));
             leftPanel.add(BSIF);
             leftPanel.add(Box.createRigidArea(new Dimension(200,50)));
             leftPanel.add(GIRV);
 		leftPanel.add(Box.createRigidArea(new Dimension(200,50)));
 		leftPanel.add(NORTH);
 		leftPanel.add(Box.createRigidArea(new Dimension(200,50)));
 		leftPanel.add(ENGRSCI);
 		leftPanel.add(Box.createRigidArea(new Dimension(200,50)));
 		leftPanel.add(LIBRARY);
             rightPanel.add(HSSB);
             rightPanel.add(Box.createRigidArea(new Dimension(200,50)));
             rightPanel.add(HFH);
             rightPanel.add(Box.createRigidArea(new Dimension(200,50)));
             rightPanel.add(KERR);
             rightPanel.add(Box.createRigidArea(new Dimension(200,50)));
             rightPanel.add(LLCH);
             rightPanel.add(Box.createRigidArea(new Dimension(200,50)));
             rightPanel.add(PHELP);
 		rightPanel.add(Box.createRigidArea(new Dimension(200,50)));
             rightPanel.add(SOUTH);
 		rightPanel.add(Box.createRigidArea(new Dimension(200,50)));
             rightPanel.add(ENGR2);
 		rightPanel.add(Box.createRigidArea(new Dimension(200,75)));
             thePanel.add(leftPanel);
             thePanel.add(rightPanel);
             frame.getContentPane().add(BorderLayout.CENTER, thePanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.setSize(420, 600);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
             searchBar.requestFocus(); //put cursor in search bar
 	}//end setUpHomeScreen
 
  	//function to clear the main frame
 	public void guiRemoveAll() {
     	////this wipes the frame clean, use before switching panels
             leftPanel.removeAll();
             rightPanel.removeAll();
             bottomPanel.removeAll();
             topPanel.removeAll();
             infoPanel.removeAll();
             thePanel.removeAll();
             newPanel.removeAll();
             searchBar.setText("");
             frame.getContentPane().removeAll();
             frame.getContentPane().remove(thePanel);
             frame.getContentPane().remove(newPanel);
  	      frame.validate();
             frame.repaint();
 	}//end guiRemoveAll
 
       public void search() throws IOException {
             String query = searchBar.getText();
             if (bldgAbbrs.contains(query) || bldgNames.contains(query)) {
                   int i = bldgNames.indexOf(query);
                   int j = bldgAbbrs.indexOf(query);
                   int k;
                   k = i >= 0 ? i : j;
                   buttons.get(k).doClick();
                   return;
             }
       }//end search
 
       public void autoComplete() throws IOException {
             String query = searchBar.getText();
             Vector<String> suggestions = new Vector<String>();
             for(String abbr: bldgAbbrs) {
                   if(abbr.startsWith(query)){
                         suggestions.add(abbr);
                   }
             }
             for(String name: bldgNames) {
                   if(name.startsWith(query)) {
                         suggestions.add(name);
                   }
             }
 
             //Test loop for suggestions vector
             for(int j = 0; j < suggestions.size(); j++) {
                   System.out.println(suggestions.get(j));
             }
             System.out.println("");
       }//end autoComplete
 
 	public void T387() throws IOException{
             guiRemoveAll();
             newPanel.setBackground(Color.WHITE);
             newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
             newPanel.setSize(800,625);
             JTextArea T387TA = new JTextArea(T387Info);
             T387TA.setEditable(false);
             T387TA.setLineWrap(true);
             T387TA.setWrapStyleWord(true);
             JScrollPane T387Scroll = new JScrollPane(T387TA);
             T387Scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
             T387TA.setPreferredSize(new Dimension(200,600));
             infoPanel.add(T387Scroll);
             bottomPanel.add(cancel);
             newPanel.add(bottomPanel);
             topPanel.add(T387Label);
             cancel.addActionListener(new CancelActionListener());
             java.net.URL T387_URL = getClass().getResource("/387.jpg");
             JLabel T387label = new JLabel(new ImageIcon(T387_URL));
 
 
             //String Path = "387.jpg";
             //File File = new File(Path);
             //BufferedImage Image = ImageIO.read(File);
             //JLabel label = new JLabel(new ImageIcon(Image));
             newPanel.add(T387label);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}//end T387
 
 	public void T429() throws IOException{
             guiRemoveAll();
             newPanel.setBackground(Color.WHITE);
             newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
             newPanel.setSize(800,625);
             JTextArea T429TA = new JTextArea(T429Info);
             T429TA.setEditable(false);
             T429TA.setLineWrap(true);
             T429TA.setWrapStyleWord(true);
             JScrollPane T429Scroll = new JScrollPane(T429TA);
             T429Scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
             T429TA.setPreferredSize(new Dimension(200,600));
             infoPanel.add(T429Scroll);
             bottomPanel.add(cancel);
             newPanel.add(bottomPanel);
             topPanel.add(T429Label);
             cancel.addActionListener(new CancelActionListener());
             java.net.URL T429_URL = getClass().getResource("/429.jpg");
             JLabel T429label = new JLabel(new ImageIcon(T429_URL));
 
             //String Path = "429.jpg";
             //File File = new File(Path);
             //BufferedImage Image = ImageIO.read(File);
             //JLabel label = new JLabel(new ImageIcon(Image));
             newPanel.add(T429label);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}//end T429
     
     	public void BRDA() throws IOException{
             guiRemoveAll();
             newPanel.setBackground(Color.WHITE);
             newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
             newPanel.setSize(800,625);
             JTextArea BRDATA = new JTextArea(BRDAInfo);
             BRDATA.setEditable(false);
             BRDATA.setLineWrap(true);
             BRDATA.setWrapStyleWord(true);
             JScrollPane BRDAScroll = new JScrollPane(BRDATA);
             BRDAScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
             BRDATA.setPreferredSize(new Dimension(200,600));
             infoPanel.add(BRDAScroll);
             bottomPanel.add(cancel);
             newPanel.add(bottomPanel);
             topPanel.add(BRDALabel);
             cancel.addActionListener(new CancelActionListener());
             java.net.URL BRDA_URL = getClass().getResource("/BRDA.jpg");
             JLabel BRDAlabel = new JLabel(new ImageIcon(BRDA_URL));
             //String BRDAPath = "BRDA.jpg";
             //File BRDAFile = new File(BRDAPath);
             //BufferedImage BRDAImage = ImageIO.read(BRDAFile);
             //JLabel BRDAlabel = new JLabel(new ImageIcon(BRDAImage));
             newPanel.add(BRDAlabel);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}//end BRDA
        
 	public void BSIF() throws IOException{
             guiRemoveAll();
             newPanel.setBackground(Color.WHITE);
             newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
             newPanel.setSize(800,625);
             JTextArea BSIFTA = new JTextArea(BSIFInfo);
             BSIFTA.setEditable(false);
             BSIFTA.setLineWrap(true);
             BSIFTA.setWrapStyleWord(true);
             JScrollPane BSIFScroll = new JScrollPane(BSIFTA);
             BSIFScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
             BSIFTA.setPreferredSize(new Dimension(200,600));
             infoPanel.add(BSIFScroll);
             bottomPanel.add(cancel);
             newPanel.add(bottomPanel);
             topPanel.add(BSIFLabel);
             cancel.addActionListener(new CancelActionListener());
             java.net.URL BSIF_URL = getClass().getResource("/BSIF.jpg");
             JLabel BSIFlabel = new JLabel(new ImageIcon(BSIF_URL));
 	    //String BSIFPath = "BSIF.jpg";
             //File BSIFFile = new File(BSIFPath);
             //BufferedImage BSIFImage = ImageIO.read(BSIFFile);
             //JLabel BSIFlabel = new JLabel(new ImageIcon(BSIFImage));
             newPanel.add(BSIFlabel);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}//end BSIF
        
 	public void GIRV() throws IOException{
             guiRemoveAll();
             newPanel.setBackground(Color.WHITE);
             newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
             newPanel.setSize(800,625);
             JTextArea GIRVTA = new JTextArea(GIRVInfo);
             GIRVTA.setEditable(false);
             GIRVTA.setLineWrap(true);
             GIRVTA.setWrapStyleWord(true);
             JScrollPane GIRVScroll = new JScrollPane(GIRVTA);
             GIRVScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
             GIRVTA.setPreferredSize(new Dimension(200,600));
             infoPanel.add(GIRVScroll);
             bottomPanel.add(cancel);
             newPanel.add(bottomPanel);
             topPanel.add(GIRVLabel);
             cancel.addActionListener(new CancelActionListener());
             java.net.URL GIRV_URL = getClass().getResource("/GIRV.jpg");
             JLabel GIRVlabel = new JLabel(new ImageIcon(GIRV_URL));
             //String GIRVPath = "GIRV.jpg";
             //File GIRVFile = new File(GIRVPath);
             //BufferedImage GIRVImage = ImageIO.read(GIRVFile);
             //JLabel GIRVlabel = new JLabel(new ImageIcon(GIRVImage));
             newPanel.add(GIRVlabel);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}//end GIRV
        
 	public void HFH() throws IOException{
             guiRemoveAll();
             newPanel.setBackground(Color.WHITE);
             newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
             newPanel.setSize(800,625);
             JTextArea HFHTA = new JTextArea(HFHInfo);
             HFHTA.setEditable(false);
             HFHTA.setLineWrap(true);
             HFHTA.setWrapStyleWord(true);
             JScrollPane HFHScroll = new JScrollPane(HFHTA);
             HFHScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
             HFHTA.setPreferredSize(new Dimension(200,600));
             infoPanel.add(HFHScroll);
             bottomPanel.add(cancel);
             newPanel.add(bottomPanel);
             topPanel.add(HFHLabel);
             cancel.addActionListener(new CancelActionListener());
             java.net.URL HFH_URL = getClass().getResource("/HFH.jpg");
             JLabel HFHlabel = new JLabel(new ImageIcon(HFH_URL));
 
             //String HFHPath = "HFH.jpg";
             //File HFHFile = new File(HFHPath);
             //BufferedImage HFHImage = ImageIO.read(HFHFile);
             //JLabel HFHlabel = new JLabel(new ImageIcon(HFHImage));
             newPanel.add(HFHlabel);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}//end HFH
         
 	public void HSSB() throws IOException{
             guiRemoveAll();
             newPanel.setBackground(Color.WHITE);
             newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
             newPanel.setSize(800,625);
             JTextArea HSSBTA = new JTextArea(HSSBInfo);
             HSSBTA.setEditable(false);
             HSSBTA.setLineWrap(true);
             HSSBTA.setWrapStyleWord(true);
             JScrollPane HSSBScroll = new JScrollPane(HSSBTA);
             HSSBScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
             HSSBTA.setPreferredSize(new Dimension(200,600));
             infoPanel.add(HSSBScroll);
             bottomPanel.add(cancel);
             newPanel.add(bottomPanel);
             topPanel.add(HSSBLabel);
             cancel.addActionListener(new CancelActionListener());
             java.net.URL HSSB_URL = getClass().getResource("/HSSB.jpg");
             JLabel HSSBlabel = new JLabel(new ImageIcon(HSSB_URL));
 
             //String HFHPath = "HSSB.jpg";
             //File HFHFile = new File(HFHPath);
             //BufferedImage HFHImage = ImageIO.read(HFHFile);
             //JLabel HFHlabel = new JLabel(new ImageIcon(HFHImage));
             newPanel.add(HSSBlabel);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}//end HSSB
     
 	public void KERR() throws IOException{
             guiRemoveAll();
             newPanel.setBackground(Color.WHITE);
             newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
             newPanel.setSize(800,625);
             JTextArea KERRTA = new JTextArea(KERRInfo);
             KERRTA.setEditable(false);
             KERRTA.setLineWrap(true);
             KERRTA.setWrapStyleWord(true);
             JScrollPane KERRScroll = new JScrollPane(KERRTA);
             KERRScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
             KERRTA.setPreferredSize(new Dimension(200,600));
             infoPanel.add(KERRScroll);
             bottomPanel.add(cancel);
             newPanel.add(bottomPanel);
             topPanel.add(KERRLabel);
             cancel.addActionListener(new CancelActionListener());
             java.net.URL KERR_URL = getClass().getResource("/KERR.jpg");
             JLabel KERRlabel = new JLabel(new ImageIcon(KERR_URL));
 
             //String KERRPath = "KERR.jpg";
             //File KERRFile = new File(KERRPath);
             //BufferedImage KERRImage = ImageIO.read(KERRFile);
             //JLabel KERRlabel = new JLabel(new ImageIcon(KERRImage));
             newPanel.add(KERRlabel);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}//end KERR
     
 	public void LLCH() throws IOException{
             guiRemoveAll();
             newPanel.setBackground(Color.WHITE);
             newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
             newPanel.setSize(800,625);
             JTextArea LLCHTA = new JTextArea(LLCHInfo);
             LLCHTA.setEditable(false);
             LLCHTA.setLineWrap(true);
             LLCHTA.setWrapStyleWord(true);
             JScrollPane LLCHScroll = new JScrollPane(LLCHTA);
             LLCHScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
             LLCHTA.setPreferredSize(new Dimension(200,600));
             infoPanel.add(LLCHScroll);
             bottomPanel.add(cancel);
             newPanel.add(bottomPanel);
             topPanel.add(LLCHLabel);
             cancel.addActionListener(new CancelActionListener());
 	    java.net.URL LLCH_URL = getClass().getResource("/LLCH.jpg");
             //String LLCHPath = "LLCH.jpg";
             //File LLCHFile = new File(LLCHPath);
             //BufferedImage LLCHImage = ImageIO.read(LLCHFile);
             JLabel LLCHlabel = new JLabel(new ImageIcon(LLCH_URL));
             newPanel.add(LLCHlabel);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}//end LLCH
        
 	public void PHELP() throws IOException{
             guiRemoveAll();
             newPanel.setBackground(Color.WHITE);
             newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
             newPanel.setSize(800,625);
             JTextArea PHELPTA = new JTextArea(PHELPInfo);
             PHELPTA.setEditable(false);
             PHELPTA.setLineWrap(true);
             PHELPTA.setWrapStyleWord(true);
             JScrollPane PHELPScroll = new JScrollPane(PHELPTA);
             PHELPScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
             PHELPTA.setPreferredSize(new Dimension(200,600));
             infoPanel.add(PHELPScroll);
             bottomPanel.add(cancel);
             newPanel.add(bottomPanel);
             topPanel.add(PHELPLabel);
             cancel.addActionListener(new CancelActionListener());
             java.net.URL PHELP_URL = getClass().getResource("/PHELP.jpg");
             JLabel PHELPlabel = new JLabel(new ImageIcon(PHELP_URL));
 
             //String PHELPPath = "PHELP.jpg";
             //File PHELPFile = new File(PHELPPath);
             //BufferedImage PHELPImage = ImageIO.read(PHELPFile);
             //JLabel PHELPlabel = new JLabel(new ImageIcon(PHELPImage));
             newPanel.add(PHELPlabel);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}//end PHELP
     
 	public void NORTH() throws IOException {
 			guiRemoveAll();
 			newPanel.setBackground(Color.WHITE);
 			newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
 			newPanel.setSize(800,625);
 			JTextArea NORTHTA = new JTextArea(NORTHInfo);
 			NORTHTA.setEditable(false);
 			NORTHTA.setLineWrap(true);
 			NORTHTA.setWrapStyleWord(true);
 			JScrollPane NORTHScroll = new JScrollPane(NORTHTA);
 			NORTHScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 			NORTHTA.setPreferredSize(new Dimension(200,600));
 			infoPanel.add(NORTHScroll);
 			bottomPanel.add(cancel);
 			newPanel.add(bottomPanel);
 			topPanel.add(NORTHLabel);
 			cancel.addActionListener(new CancelActionListener());
 			java.net.URL NORTH_URL = getClass().getResource("/NORTH.jpg");
 			JLabel NORTHlabel = new JLabel(new ImageIcon(NORTH_URL));
 			
 			newPanel.add(NORTHlabel);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}
 	
 	public void SOUTH() throws IOException {
 			guiRemoveAll();
 			newPanel.setBackground(Color.WHITE);
 			newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
 			newPanel.setSize(800,625);
 			JTextArea SOUTHTA = new JTextArea(SOUTHInfo);
 			SOUTHTA.setEditable(false);
 			SOUTHTA.setLineWrap(true);
 			SOUTHTA.setWrapStyleWord(true);
 			JScrollPane SOUTHScroll = new JScrollPane(SOUTHTA);
 			SOUTHScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 			SOUTHTA.setPreferredSize(new Dimension(200,600));
 			infoPanel.add(SOUTHScroll);
 			bottomPanel.add(cancel);
 			newPanel.add(bottomPanel);
 			topPanel.add(SOUTHLabel);
 			cancel.addActionListener(new CancelActionListener());
 			java.net.URL SOUTH_URL = getClass().getResource("/SOUTH.jpg");
 			JLabel SOUTHlabel = new JLabel(new ImageIcon(SOUTH_URL));
 			
 			newPanel.add(SOUTHlabel);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}
 	
 	public void ENGRSCI() throws IOException {
 			guiRemoveAll();
 			newPanel.setBackground(Color.WHITE);
 			newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
 			newPanel.setSize(800,625);
 			JTextArea ENGRSCITA = new JTextArea(ENGRSCIInfo);
 			ENGRSCITA.setEditable(false);
 			ENGRSCITA.setLineWrap(true);
 			ENGRSCITA.setWrapStyleWord(true);
 			JScrollPane ENGRSCIScroll = new JScrollPane(ENGRSCITA);
 			ENGRSCIScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 			ENGRSCITA.setPreferredSize(new Dimension(200,600));
 			infoPanel.add(ENGRSCIScroll);
 			bottomPanel.add(cancel);
 			newPanel.add(bottomPanel);
 			topPanel.add(ENGRSCILabel);
 			cancel.addActionListener(new CancelActionListener());
 			java.net.URL ENGRSCI_URL = getClass().getResource("/ENGRSCI.jpg");
 			JLabel ENGRSCIlabel = new JLabel(new ImageIcon(ENGRSCI_URL));
 			
 			newPanel.add(ENGRSCIlabel);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}
 	
 	public void ENGR2() throws IOException {
 			guiRemoveAll();
 			newPanel.setBackground(Color.WHITE);
 			newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
 			newPanel.setSize(800,625);
 			JTextArea ENGR2TA = new JTextArea(ENGR2Info);
 			ENGR2TA.setEditable(false);
 			ENGR2TA.setLineWrap(true);
 			ENGR2TA.setWrapStyleWord(true);
 			JScrollPane ENGR2Scroll = new JScrollPane(ENGR2TA);
 			ENGR2Scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 			ENGR2TA.setPreferredSize(new Dimension(200,600));
 			infoPanel.add(ENGR2Scroll);
 			bottomPanel.add(cancel);
 			newPanel.add(bottomPanel);
 			topPanel.add(ENGR2Label);
 			cancel.addActionListener(new CancelActionListener());
 			java.net.URL ENGR2_URL = getClass().getResource("/ENGR2.jpg");
 			JLabel ENGR2label = new JLabel(new ImageIcon(ENGR2_URL));
 			
 			newPanel.add(ENGR2label);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}
 	
 	public void LIBRARY() throws IOException {
 			guiRemoveAll();
 			newPanel.setBackground(Color.WHITE);
 			newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
 			newPanel.setSize(800,625);
 			JTextArea LIBRARYTA = new JTextArea(LIBRARYInfo);
 			LIBRARYTA.setEditable(false);
 			LIBRARYTA.setLineWrap(true);
 			LIBRARYTA.setWrapStyleWord(true);
 			JScrollPane LIBRARYScroll = new JScrollPane(LIBRARYTA);
 			LIBRARYScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 			LIBRARYTA.setPreferredSize(new Dimension(200,600));
 			infoPanel.add(LIBRARYScroll);
 			bottomPanel.add(cancel);
 			newPanel.add(bottomPanel);
 			topPanel.add(LIBRARYLabel);
 			cancel.addActionListener(new CancelActionListener());
 			java.net.URL LIBRARY_URL = getClass().getResource("/LIBRARY.jpg");
 			JLabel LIBRARYlabel = new JLabel(new ImageIcon(LIBRARY_URL));
 			
 			newPanel.add(LIBRARYlabel);
             frame.getContentPane().add(BorderLayout.EAST, infoPanel);
             frame.getContentPane().add(BorderLayout.NORTH, topPanel);
             frame.getContentPane().add(BorderLayout.CENTER,newPanel);
             frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
             frame.setSize(1000,625);
             frame.setBackground(Color.WHITE);
             frame.setVisible(true);
 	}
 	
       //action listener class for the search bar
       //this is called when a user presses enter
       class SearchBarActionListener implements ActionListener{
             public void actionPerformed(ActionEvent event){
             try {search();}
                 catch (IOException ex) {
                     Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
                       }
             }
       }//end SearchBarActionListener
 
       //document listener for the search bar
       //this is called for every change made to the JTextField 'searchBar'
       class SearchBarDocumentListener implements DocumentListener{
 	  public void changedUpdate(DocumentEvent e){
 	      /* The search bar doesn't need to know about attribute updates
 		 other than text insertion and deletion
 	      try {autoComplete();}
 	      catch (IOException ex) {
 		  Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
 	      }
 	      */
 	  }
 	  //called when there is an insertion to the document
 	  public void insertUpdate(DocumentEvent e){
 	      try {autoComplete();}
 	      catch (IOException ex) {
 		  Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
 	      }
 	  }
 	  //called when there is a deletion in the document
 	  public void removeUpdate(DocumentEvent e){
 	      try {autoComplete();}
 	      catch (IOException ex) {
 		  Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
 	      }
 	  }
       }//end SearchBarDocumentActionListener
 
 	//action listener class for the cancel button
 	class CancelActionListener implements ActionListener{
             public void actionPerformed(ActionEvent event){
         	try {setUpHomeScreen();}
                 catch (IOException ex) {
                     Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
             }
 	}//end CancelActionListener
 
 	class T387Listener implements ActionListener{
             public void actionPerformed(ActionEvent event){
         	try {T387();}
                 catch (IOException ex) {
                     Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
             }
 	}//end T387Listener
 
 	class T429Listener implements ActionListener{
             public void actionPerformed(ActionEvent event){
         	try {T429();} 
                 catch (IOException ex) {
                     Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
             }
 	}//end T429Listener
 
 	class BRDAListener implements ActionListener{
             public void actionPerformed(ActionEvent event){
         	try {BRDA();}
                 catch (IOException ex) {
                     Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
             }
 	}//end BRDAListener
 
 	class BSIFListener implements ActionListener{
             public void actionPerformed(ActionEvent event){
         	try {BSIF();}
                 catch (IOException ex) {
                     Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
             }
 	}//end BSIFListener
 
 	class GIRVListener implements ActionListener{
             public void actionPerformed(ActionEvent event){
         	try {GIRV();} 
                 catch (IOException ex) {
                     Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
             }
 	}//end GIRVListener
 
 	class HFHListener implements ActionListener{
             public void actionPerformed(ActionEvent event){
         	try {HFH();}
                 catch (IOException ex) {
                     Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
             }
 	}//end HFHListener
 
 	class HSSBListener implements ActionListener{
             public void actionPerformed(ActionEvent event){
         	try {HSSB();}
                 catch (IOException ex) {
                     Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
             }
 	}//end HSSBListener
 
 	class KERRListener implements ActionListener{
             public void actionPerformed(ActionEvent event){
         	try {KERR();}
                 catch (IOException ex) {
                     Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
             }
 	}//end KERRListener
 
 	class LLCHListener implements ActionListener{
             public void actionPerformed(ActionEvent event){
         	try {LLCH();} 
                 catch (IOException ex) {
                     Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
             }
 	}//end LLCHListener
 
 	class PHELPListener implements ActionListener{
             public void actionPerformed(ActionEvent event){
         	try {PHELP();}
                 catch (IOException ex) {
                     Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
             }
 	}//end PHELPListener
 	
 	class NORTHListener implements ActionListener {
 			public void actionPerformed(ActionEvent event) {
 				try {NORTH();}
 					catch (IOException ex) {
 						Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
 						}
 			}
 	}//end NORTHListener
 	
 	class SOUTHListener implements ActionListener {
 			public void actionPerformed(ActionEvent event) {
 				try {SOUTH();}
 					catch (IOException ex) {
 						Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
 						}
 			}
 	}//end SOUTHListener
 	
 	class ENGRSCIListener implements ActionListener {
 			public void actionPerformed(ActionEvent event) {
 				try {ENGRSCI();}
 					catch (IOException ex) {
 						Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
 						}
 			}
 	}//end ENGRSCIListener
 	
 	class ENGR2Listener implements ActionListener {
 			public void actionPerformed(ActionEvent event) {
 				try {ENGR2();}
 					catch (IOException ex) {
 						Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
 						}
 			}
 	}//end ENGR2Listener
 	
 	class LIBRARYListener implements ActionListener {
 			public void actionPerformed(ActionEvent event) {
 				try {LIBRARY();}
 					catch (IOException ex) {
 						Logger.getLogger(TheGUI.class.getName()).log(Level.SEVERE, null, ex);
 						}
 			}
 	}//end LIBRARYListener
         
 } //end class
 
