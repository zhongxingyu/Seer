 /**
  * @(#)GamersClub.java
  *
  * GamersClub Main application
  *
  * @author Leon Blakey/Lord.Quackstar
  * @version 1.00 2009/12/5
  */
 import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FilterOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.LookAndFeel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import javax.swing.border.Border;
 
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 import org.json.me.JSONException;
 import org.json.me.JSONObject;
 
 public class GamersClub extends JFrame implements ActionListener {
     JPanel contentPane;
     public static JPanel bodyPanel;
     ComponentTitledBorder bodyBorder;
     JTextPane errorLog;
     PrintStream oldOut,oldErr;
     JScrollPane errorScroll;
     StyledDocument errorDoc;
     
     /***Static Class Refrences***/
     public static MainMenu MainMenu = new MainMenu();
     public static GameBrowser GameBrowser = new GameBrowser();
     public static PeopleBrowser PeopleBrowser = new PeopleBrowser();
     public static AddGame AddGame = new AddGame();
     public static CopyGame CopyGame = new CopyGame();
     public static PasteGame PasteGame = new PasteGame();
     public static Profile Profile = new Profile();
     
     /***Static User Info***/
     public static String username, uid, realName, gamersTag, grade, bestAt, favGame, avatar, desc, userid;
     public static Boolean admin;
    public static String avatarDir = "4242f83ae1ed41dd9e3b4d75037d7079";
     
 	public GamersClub () {
 		/***Pre init, make error logs so future commands are happy***/
 		errorLog = new JTextPane();
 		errorLog.setEditable(false);
 		errorLog.setAlignmentX(Component.CENTER_ALIGNMENT);
 		errorDoc = errorLog.getStyledDocument();
 		errorScroll = new JScrollPane(errorLog);
 		Globs.setSize(errorScroll,125,0);
 		errorScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		errorScroll.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		errorScroll.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
 		
       	/***Init***/
       	oldOut = System.out;
       	oldErr = System.err;
 		System.setOut(new PrintStream(new FilteredStream(new ByteArrayOutputStream(),false)));
 		System.setErr(new PrintStream(new FilteredStream(new ByteArrayOutputStream(),true)));
       	System.out.println("Initializing");
       	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Will exit when close button is pressed
      	setTitle("Gamers Club Distrobution Service");
        	setMinimumSize(new Dimension(1000,700));
        	try {
       		UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
       		//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
    		} 
    		catch (Exception e) {
    			e.printStackTrace();
    		}
 
        	
        	/***Check if user exists***/
        	System.out.println("System user account: "+System.getProperty("user.name")+", checking with database");
 		String response = Globs.webTalk("mode=userExists&user="+System.getProperty("user.name"),null,null);
 		JSONObject dbInfo = null;
 		System.out.println("Response: "+response);
 		try{
 			dbInfo = new JSONObject(response);
 			uid = (String)dbInfo.get("counter");
 			username = (String)dbInfo.get("username");
 			admin = (Integer.parseInt((String)dbInfo.get("admin")) == 1) ? true : false;
 			avatar = (dbInfo.get("avatar") == JSONObject.NULL) ? "NULL" : (String)dbInfo.get("avatar");
 			realName = (String)dbInfo.get("name");
 			gamersTag = (String)dbInfo.get("gamersTag");
 			grade = (String)dbInfo.get("gradeNum");
 			bestAt = (String)dbInfo.get("bestAt");
 			favGame = (String)dbInfo.get("favGames");
 			avatar = (String)dbInfo.get("avatar");
 			desc = (String)dbInfo.get("desc");
 
 			System.out.println("User: "+username+" | Real Name: "+realName+" | UID: "+uid+" | Is Admin: "+admin);
 		}
 		catch(JSONException e) {
 			//must of false or garbage
 			if(response.equals("false")) {
 				JOptionPane.showMessageDialog(null,"You are not in the Gamers Club.");
 				System.exit(0);
 			}
 			else if(response.equals("disabled")) {
 				JOptionPane.showMessageDialog(null,"Account is disabled");
 				System.exit(0);
 			}
 			else {
 				JOptionPane.showMessageDialog(null,"<HTML>ERROR: Initial check, Either garbage for input or website dosen't exist!<br>"+e.getMessage()+"</HTML>");
 				System.exit(0);
 			}
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
         
       	/***Get Basic Gui configured***/
       	contentPane = new JPanel(); //el massive panel that holds everything
       	//contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
       	contentPane.setLayout(new BorderLayout());
       	contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
       	
       	
       	/***Make Banner***/
       	JLabel bannerLabel = new JLabel("Gamers Club Distribution Service",null,JLabel.CENTER);
 		bannerLabel.setFont(new Font("Serif", Font.PLAIN, 36)); //make it big
 		bannerLabel.setMinimumSize(new Dimension(600,100));
 		//bannerLabel.setBorder(BorderFactory.createEmptyBorder(-10,0,-10,0)); //Black line border
 		bannerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
 		contentPane.add(bannerLabel,BorderLayout.NORTH);
 		
 		/***Make Main Body Panel***/
 		bodyPanel = new JPanel(new CardLayout());
 		bodyPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 		JButton homeButton = new JButton("Main Menu");
 		homeButton.setFocusPainted(false);
 		
 		bodyBorder = new ComponentTitledBorder(homeButton, bodyPanel, BorderFactory.createEtchedBorder()); 
 		homeButton.addActionListener(new ActionListener(){ 
 		            public void actionPerformed(ActionEvent e){ 
 		                Globs.switchBody("MainMenu");
 		            }
 		});
 
 		bodyPanel.setBorder(bodyBorder);
 		//Start adding panels
 		bodyPanel.add(MainMenu.generate(),"MainMenu");
 		bodyPanel.add(GameBrowser.generate(),"GameBrowser");
 		bodyPanel.add(PeopleBrowser.generate(),"PeopleBrowser");
 		bodyPanel.add(AddGame.generate(),"AddGame");
 		bodyPanel.add(CopyGame.generate(),"CopyGame");
 		bodyPanel.add(PasteGame.generate(),"PasteGame");
 		bodyPanel.add(Profile.generate(),"Profile");
 		CardLayout cl = (CardLayout)(bodyPanel.getLayout());
 	    cl.show(bodyPanel, "MainMenu");
 		bodyPanel.setMinimumSize(new Dimension(600,500));
 		contentPane.add(bodyPanel,BorderLayout.CENTER);
 		
 		/***Error Log***/
 		contentPane.add(errorScroll,BorderLayout.SOUTH);
 		
 		/***Finish Up***/
 		add(contentPane); //add to JFrame
 		SwingUtilities.updateComponentTreeUI(contentPane); //tell all children to updateUI to current one
 		setVisible(true); //make JFrame visible
 	}
 	
 	
 	
 	/* Description: All the actionListeners for Main Menu
 	 * Used by: Hierchy Buttons */
 	public void actionPerformed(ActionEvent e) {
     	JComboBox cb = (JComboBox)e.getSource();
         String reqPanel = (String)cb.getSelectedItem();
         Globs.switchBody(reqPanel);
     }
     
     /***Output Wrapper, Redirects all ouput to log at bottom***/
     class FilteredStream extends FilterOutputStream {
     	AttributeSet className, text;
     	boolean error;
     	
         public FilteredStream(OutputStream aStream,boolean error) {
             super(aStream);
             this.error = error;
             
           	Style style = errorLog.addStyle("Class", null); 
           	StyleConstants.setForeground(style, Color.RED );
 
           	StyleConstants.setForeground(style, Color.BLUE ); 
           	        	
           	style = errorLog.addStyle("Normal", null); 
           	
           	style = errorLog.addStyle("Error", null); 
           	StyleConstants.setForeground(style, Color.red); 	
        	}
 
         public void write(byte b[], int off, int len) throws IOException {
             //get string version
             String aString = new String(b , off , len).trim();
             
             //don't print empty strings
             if(aString.length()==0)
             	return;
             //get calling class name
         	StackTraceElement[] elem = Thread.currentThread().getStackTrace();
         	String callingClass = elem[10].getClassName();
         	
         	//Capture real class from error message
         	if(callingClass.equals("java.lang.Throwable"))
         		callingClass = elem[12].getClassName();
         	
         	Style style = null;
         	if(error) style = errorDoc.getStyle("Error");
         	else style = errorDoc.getStyle("Class");
         	
             try {
         		if(errorDoc.getLength()!=0)
         			errorDoc.insertString(errorDoc.getLength(),"\n",errorDoc.getStyle("Normal"));
         		errorDoc.insertString(errorDoc.getLength(),callingClass+": ",style);
         		errorDoc.insertString(errorDoc.getLength(),aString,errorDoc.getStyle("Normal"));
         	} 
         	catch (BadLocationException ble) {
             	oldErr.println("Error");
         	}
         	
         	if(error) 
         		oldErr.println(aString); //so runtime errors can be caught
         	else
         		oldOut.println(aString); //so runtime errors can be caught
         	
         	errorLog.repaint();
 			errorLog.setCaretPosition(errorDoc.getLength()); 
         }
     }
 
     
     public static void main(String[] args) {
 		new GamersClub(); //simply start gamersclub
     }
 }
