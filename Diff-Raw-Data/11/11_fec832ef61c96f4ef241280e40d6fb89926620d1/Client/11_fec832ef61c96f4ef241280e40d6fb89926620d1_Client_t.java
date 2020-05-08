 package com.apps4you.client;
 
 //Client portion of a stream-socket connection between client and server.
 import java.io.EOFException;
 import java.io.File;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.UUID;
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 
 import com.apps4you.shared.*;
 
 public class Client extends JFrame implements ActionListener
 {
 
    private static final long serialVersionUID = 7189340988809001708L;
    private JTextField enterField; // enters information from user
    private JTextArea displayArea; // display information to user
    private ObjectOutputStream output; // output stream to server
    private ObjectInputStream input; // input stream from server
    private String message = ""; // message from server
    private String chatServer; // host server for this application
    private Socket client; // socket to communicate with server
    
    //New UI Pieces
    private File selectedFile;
    private JFrame mainFrame = new JFrame("Apps4You - Client");
    private JButton selectDataFileButton = new JButton("Select File");
    private JButton opponentButton = new JButton("Opponent");
    private JButton closeButton = new JButton("Close");
    private JLabel welcomeLabel = new JLabel("Welcome to the Apps4You Client.");
    private JLabel moderatorCommentsLabel = new JLabel("Moderator Comments:");
    private JTextArea moderatorCommentsArea = new JTextArea();
    
    private Warrior combatant;
    
    // initialize chatServer and set up GUI
    public Client( String host )
    {
       super( "Client" );
       
       chatServer = host; // set server to which this client connects
 
       //lookAndFeelSetup();
       
       enterField = new JTextField(); // create enterField
       enterField.setEditable( false );
       enterField.addActionListener(
          new ActionListener() 
          {
             // send message to server
             public void actionPerformed( ActionEvent event )
             {
                sendData( event.getActionCommand() );
                enterField.setText( "" );
             } // end method actionPerformed
          } // end anonymous inner class
       ); // end call to addActionListener
 
       add( enterField, BorderLayout.NORTH );
 
       displayArea = new JTextArea(); // create displayArea
       add( new JScrollPane( displayArea ), BorderLayout.CENTER );
 
       setSize( 300, 150 ); // set size of window
       setVisible( true ); // show window
       
       setupMainFrame(); //Setup the Window and start gathering info form the user
           
       moderatorCommentsArea.append("Moderator Comments:\n");      
    } // end Client constructor
    
    // connect to server and process messages from server
    public void runClient() 
    {
       try // connect to server, get streams, process connection
       {
          connectToServer(); // create a Socket to make connection
          getStreams(); // get the input and output streams
          processConnection(); // process connection
          
          
       } // end try
       catch ( EOFException eofException ) 
       {
          displayMessage( "\nClient terminated connection" );
       } // end catch
       catch ( IOException ioException ) 
       {
          ioException.printStackTrace();
       } // end catch
       finally 
       {
          closeConnection(); // close connection
       } // end finally
    } // end method runClient
 
    // connect to server
    private void connectToServer() throws IOException
    {      
       displayMessage( "Attempting connection\n" );
 
       // create Socket to make connection to server
       client = new Socket( InetAddress.getByName( chatServer ), 12345 );
 
       // display connection information
       displayMessage( "Connected to: " + 
          client.getInetAddress().getHostName() );
    } // end method connectToServer
 
    // get streams to send and receive data
    private void getStreams() throws IOException
    {
       // set up output stream for objects
       output = new ObjectOutputStream( client.getOutputStream() );      
       output.flush(); // flush output buffer to send header information
 
       // set up input stream for objects
       input = new ObjectInputStream( client.getInputStream() );
 
       displayMessage( "\nGot I/O streams\n" );
    } // end method getStreams
 
    // process connection with server
    private void processConnection() throws IOException
    {
       // enable enterField so client user can send messages
       setTextFieldEditable( true );
 
       do // process messages sent from server
       { 
          try // read message and display it
          {
             message = ( String ) input.readObject(); // read new message
             displayMessage( "\n" + message ); // display message
          } // end try
          catch ( ClassNotFoundException classNotFoundException ) 
          {
             displayMessage( "\nUnknown object type received" );
          } // end catch
 
       } while ( !message.equals( "SERVER>>> TERMINATE" ) );
    } // end method processConnection
 
    // close streams and socket
    private void closeConnection() 
    {
       displayMessage( "\nClosing connection" );
       setTextFieldEditable( false ); // disable enterField
 
       try 
       {
          output.close(); // close output stream
          input.close(); // close input stream
          client.close(); // close socket
       } // end try
       catch ( IOException ioException ) 
       {
          ioException.printStackTrace();
       } // end catch
    } // end method closeConnection
 
    // send message to server
    private void sendData( String message )
    {
       try // send object to server
       {
          output.writeObject( "CLIENT>>> " + message );
          output.flush(); // flush data to output
          displayMessage( "\nCLIENT>>> " + message );
       } // end try
       catch ( IOException ioException )
       {
          displayArea.append( "\nError writing object" );
       } // end catch
    } // end method sendData
 
    // manipulates displayArea in the event-dispatch thread
    private void displayMessage( final String messageToDisplay )
    {
       SwingUtilities.invokeLater(
          new Runnable()
          {
             public void run() // updates displayArea
             {
                displayArea.append( messageToDisplay );
             } // end method run
          }  // end anonymous inner class
       ); // end call to SwingUtilities.invokeLater
    } // end method displayMessage
 
    // manipulates enterField in the event-dispatch thread
    private void setTextFieldEditable( final boolean editable )
    {
       SwingUtilities.invokeLater(
          new Runnable() 
          {
             public void run() // sets enterField's editability
             {
                enterField.setEditable( editable );
             } // end method run
          } // end anonymous inner class
       ); // end call to SwingUtilities.invokeLater
    } // end method setTextFieldEditable
 
  
    private Warrior readFileToCreateCombatant(File file)
    {
 	   //Setup reading of the serialized wdat file here
 	   
 	   
 	   //For now just going to construct a warrior as if it had been read in from the file
	   Warrior w = new Warrior(new UUID(4242L, 4240L), "Brainy Viking", 100, Origins.KRIKKIT, "Silly Viking you are supposed to be tough.");
 	   return w;
 	   
    }
 
    
    private void lookAndFeelSetup()
    {
 	   try {
 		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 		        if ("Nimbus".equals(info.getName())) {
 		        	UIManager.setLookAndFeel(info.getClassName());
 		        	System.out.println("\n Nimbus UI was selected.");
 		        	break;
 		            
 		        }
 		    }
 		} catch (Exception e) {
 		    // If Nimbus is not available, then use the system default.
 			System.out.println("\n Nimbus UI was not available \n");
 		}   
    }
    
    private void setupMainFrame()
    {
 	   this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	   this.mainFrame.getContentPane().add(selectDataFileButton, BorderLayout.SOUTH);
 	   this.mainFrame.getContentPane().add(opponentButton, BorderLayout.SOUTH);
 	   this.mainFrame.getContentPane().add(closeButton, BorderLayout.SOUTH);
        this.mainFrame.getContentPane().add(welcomeLabel,BorderLayout.PAGE_START);
        this.mainFrame.getContentPane().add(new JScrollPane(moderatorCommentsArea), BorderLayout.EAST);
 	   this.mainFrame.getContentPane().add(new JLabel(""),BorderLayout.AFTER_LAST_LINE);
 	   
 	   this.selectDataFileButton.setSize(100, 40);
 	   this.closeButton.setSize(100, 40);
 	   this.opponentButton.setSize(100, 40);
 	   this.selectDataFileButton.addActionListener(this);
 	   this.closeButton.addActionListener(this);
 	   this.opponentButton.addActionListener(this);
 	   this.selectDataFileButton.setBounds(80, 60, 100, 30);
 	   this.closeButton.setBounds(80, 180, 100, 30);
 	   this.opponentButton.setBounds(80, 120, 100, 30);
        	   
 	   this.mainFrame.setSize( 500, 300 ); // set size of window
 	   this.mainFrame.setVisible( true ); // show window
 	   
    }
    
 
    public void actionPerformed (ActionEvent e)
    {
 	 //Handle Select button action.
 	    if (e.getSource() == selectDataFileButton) 
 	    {	    	
 	        final JFileChooser fc = new JFileChooser();	    	
 	        int returnVal = fc.showOpenDialog(this);
 
 	        if (returnVal == JFileChooser.APPROVE_OPTION) 
 	        {
 	        	this.selectedFile = fc.getSelectedFile();
 	            displayArea.append("\n File " + this.selectedFile.getName() + " was selected.");
	            this.combatant= this.readFileToCreateCombatant(this.selectedFile);
 	            this.selectDataFileButton.setEnabled(false);
 	        } else
 	        {
 	            displayArea.append("Open command cancelled by user./n");
 	        }	        
 	   }
 	    else if (e.getSource() == opponentButton)
 	    {
 	    	displayArea.append("\n Oppents Selection was chosen - Not implemented Yet. \n");
 	    }
 	    else if (e.getSource() == closeButton)
 	    {
 	    	dispose();
 	    	System.exit(0);
 	    }
    }
    
       
 } // end class Client
