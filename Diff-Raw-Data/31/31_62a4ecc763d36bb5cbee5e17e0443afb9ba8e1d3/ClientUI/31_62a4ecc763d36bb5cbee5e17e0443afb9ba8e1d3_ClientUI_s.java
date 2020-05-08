 package com.apps4you.client;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.border.EmptyBorder;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import com.apps4you.shared.Warrior;
 
 
 
 public class ClientUI extends JFrame implements ActionListener
 {
 
    private static final long serialVersionUID = -8606655646939582216L;
    private JPanel contentPane;
    private final JButton connectButton = new JButton("Server");
    private final JButton selectDataFileButton = new JButton("Select File");
    private final JButton opponentButton = new JButton("Opponent");
    private final JButton newCombatantButton = new JButton("New");
    private final JButton closeButton = new JButton("Close");
    private JLabel welcomeLabel = new JLabel("Welcome to the Apps4You Client.");
    private JLabel moderatorCommentsLabel = new JLabel("Moderator Comments:");
    private JTextArea moderatorCommentsArea = new JTextArea();
    
    private Client client;
    private File file;
    private Warrior w;
    private String hostLocation;
    
    
    public ClientUI()
    {
 	   //lookAndFeelSetup(); //-- Wasn't happy with the Nimbus look
 	   
 	   setupFrame();
 	   
    
    }
    
    protected void connectToServer()
    {
 	   this.client = new Client(this.hostLocation);
 	   this.client.runClient();   
    }
    
    
    private void setupFrame()
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	   setBounds(100, 100, 450, 300);
 	   contentPane = new JPanel();
 	   contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 	   contentPane.setLayout(new BorderLayout(0, 0));
 	   setContentPane(contentPane);
 	   
 	   this.getContentPane().add(connectButton, BorderLayout.SOUTH);
 	   this.getContentPane().add(selectDataFileButton, BorderLayout.SOUTH);
 	   this.getContentPane().add(opponentButton, BorderLayout.SOUTH);
 	   this.getContentPane().add(newCombatantButton, BorderLayout.SOUTH);   
 	   this.getContentPane().add(closeButton, BorderLayout.SOUTH);
        this.getContentPane().add(welcomeLabel,BorderLayout.PAGE_START);
        moderatorCommentsArea.setMinimumSize(new Dimension(200, 300));
        moderatorCommentsArea.setText("Moderator Comments:");
        this.getContentPane().add(new JScrollPane(moderatorCommentsArea), BorderLayout.EAST);
 	   this.getContentPane().add(new JLabel(""),BorderLayout.SOUTH);
 	   
 	   this.setTitle("Apps4You - Client");
 	   this.selectDataFileButton.setSize(100, 40);
 	   this.closeButton.setSize(100, 40);
 	   this.opponentButton.setSize(100, 40);
 	   this.newCombatantButton.setSize(100, 40);
 	   this.selectDataFileButton.addActionListener(this);
 	   this.closeButton.addActionListener(this);
 	   this.opponentButton.addActionListener(this);
 	   this.connectButton.addActionListener(this);	
 	   this.newCombatantButton.addActionListener(this);
 	   this.connectButton.setBounds(80,40,100,30);	
 	   this.selectDataFileButton.setBounds(80, 100, 100, 30);
 	   this.opponentButton.setBounds(80, 160, 100, 30);
 	   this.newCombatantButton.setBounds(80, 220, 100, 30);
 	   this.closeButton.setBounds(80, 280, 100, 30);		   
 	   this.moderatorCommentsArea.setBounds(0, 200, 200, 30);	      
        	   
 	   this.opponentButton.setEnabled(false);
 	   
 	   this.setSize( 500, 400 ); // set size of window
 	   this.setVisible( true ); // show window
 	   this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		   
 	   
    }
    
 
    public void actionPerformed (ActionEvent e)
    {
 	 //Handle Select button action.
 	    if (e.getSource() == selectDataFileButton) 
 	    {	    	
 	        final JFileChooser fc = new JFileChooser();	
 	        final FileNameExtensionFilter fileFilter= new FileNameExtensionFilter("Warrior data file", "wdat");
 	        fc.setFileFilter(fileFilter);
 	        int returnVal = fc.showOpenDialog(this);
 
 	        if (returnVal == JFileChooser.APPROVE_OPTION) 
 	        {
 	        	file = fc.getSelectedFile();
 	            System.out.println("File " + file.getName() + " was selected.");
 	            w = Utils.readFileToCreateCombatant(file);
 	            connectToServer();
 	            this.selectDataFileButton.setEnabled(false);
 	            this.opponentButton.setEnabled(true);
 	        } else
 	        {
 	        	System.out.println("Open command cancelled by user./n");
 	        }	        
 	   }
 	    else if (e.getSource() == opponentButton)
 	    {	    	
 	    	//Get a list of the possible opponents from the server and then 
 	    	Object[] possibilities = {"Zerg", "Woody", "Ham", "Buzz"};
 	    	String s = (String)JOptionPane.showInputDialog(
 	    	                    this,
 	    	                    "Please select your opponent:",
 	    	                    "Opponent Selection Dialog",
 	    	                    JOptionPane.QUESTION_MESSAGE,
 	    	                    null,
 	    	                    possibilities,
 	    	                    null);	    	
 	    	System.out.println("\n Opponent Selection was chosen - with an opponent of: " + s ); 
 
 	    	JOptionPane.showMessageDialog(this,
 	    		    "The Opponent button is not implemented, yet.",
 	    		    "Warning",
 	    		    JOptionPane.WARNING_MESSAGE);
 	    	
 	    	//TODO Update who was selected
 	    }
 
 	    else if (e.getSource() == connectButton)
 	    {
 
 	    	String s = (String)JOptionPane.showInputDialog(
 	    	                    this,
 	    	                    "Please enter the prefered server that you would like to connect to:",
 	    	                    "Prefered Server Dialog",
 	    	                    JOptionPane.QUESTION_MESSAGE,
 	    	                    null,
 	    	                    null,
 	    	                    "localhost");
 
 	    	//If a string was returned, say so.
 	    	if ((s != null) && (s.length() > 0)&& (s.length()<=15)) 
 	    	{
 	    		System.out.println("\n Prefered Server location was chosen - with a host of: " + s ); 
 		    	this.hostLocation = s;	    	
 		     	 
 	    	}
 	    	else
 	    	{
 	    		System.out.println("\n Prefered server location is not valid with: " + s + " as a value.");	 
 	    	}
    	
 	    }
 	    else if (e.getSource() == newCombatantButton)
 	    {
 	    	CreateWarrior dialog = new CreateWarrior();
 			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 			dialog.setVisible(true);
 			Warrior nw = new Warrior(dialog.getNewName(), 
 										dialog.getOrigin(), 
 										dialog.getDescription());
			/*
 			File file = new File(dialog.getNewName()+".wdat");
 			Utils.saveWarriorToFile(nw, file);
			*/
 	    }
 	    else if (e.getSource() == closeButton)
 	    {
 	    	if ((client != null)&&(w !=null))
 	    	{
 	    	  w.setHealth(w.getHealth()-10);
 	    	  Utils.saveWarriorToFile(w, file);
 	    	}
 	    	dispose();
 	    	System.exit(0);
 	    }
    }   
 }
