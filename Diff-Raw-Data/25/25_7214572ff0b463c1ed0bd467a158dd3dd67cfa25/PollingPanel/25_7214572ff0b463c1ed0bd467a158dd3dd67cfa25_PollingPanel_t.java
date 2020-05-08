 package mo.umac.umsu.UMSUDemocrat.PollingPlace;
 
 import javax.swing.JPanel;
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 import javax.swing.JTextArea;
 import javax.swing.JScrollPane;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 //import java.awt.Dimension;
import javax.swing.JOptionPane;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import javax.swing.BorderFactory; 
 
 import java.sql.*;
 
 
 public class PollingPanel extends JPanel implements ActionListener
 {
 	
 	//http://stackoverflow.com/questions/5632065/what-does-the-declare-a-static-final-serialversionuid-warning-mean-and-how-to
    private static final long serialVersionUID = 5L;
     public static JFrame frame;
     protected JTextField searchField;
     protected JButton searchBtn;
     protected JLabel bottomBar;
     protected JTextArea infoArea;
 
     public PollingPanel( )
     {
 
	Connection conn = connectSQLiteJDBC( );
	if( conn != null ) {
 	    // DO WHAT?
 	}
 	setLayout( new GridBagLayout( ) );
 	
 	JLabel searchLabel = new JLabel( "Search:" );
 
 	searchField = new JTextField( );
 
 	searchBtn = new JButton( "Search" );
 	searchBtn.addActionListener( this );
 	searchBtn.setActionCommand( "search" );
 
 	GridBagConstraints c = new GridBagConstraints( );
 	c.ipadx = 8;
 	c.ipady = 8;
 
 	c.anchor = GridBagConstraints.PAGE_START;
 	c.fill = GridBagConstraints.BOTH;
 	c.weightx = 0;
 	c.weighty = 0.00;
 	c.gridx = 0;
 	c.gridy = 0;
 	add( searchLabel, c );
 
 	c.fill = GridBagConstraints.HORIZONTAL;
 	c.weightx = 1;
 	c.gridx = 1;
 	c.ipadx = 2;
 	add( searchField, c );
 
 	c.fill = GridBagConstraints.NONE;
 	c.weightx = 0;
 	c.gridx = 2;
 	add( searchBtn, c );
 
 	infoArea = new JTextArea( "Eligible: No\nReason: Already voted." );
 	JScrollPane scrollPane = new JScrollPane( infoArea );
 	infoArea.setEditable( false );
 	c.fill = GridBagConstraints.BOTH;
 	c.weighty = 1;
 	c.gridwidth = 3;
 	c.gridx = 0;
 	c.gridy = 1;
 	scrollPane.setBorder( BorderFactory.createTitledBorder( "Eligibility" ) );
 	add( scrollPane, c );
 	
 	bottomBar = new JLabel( "Voted students: 532   |   Voted fraction: 532/8000 = 6.65%" );
 	c.gridx = 0;
 	c.gridy = 2;
 	c.gridwidth = 3;
 	c.gridheight = 1;
 	c.weighty = 0.00;
 	c.anchor = GridBagConstraints.PAGE_END;
 	c.fill = GridBagConstraints.HORIZONTAL;
 	add( bottomBar, c );
     }
 
     private Connection connectSQLiteJDBC( )
     {
 	Connection c = null;
 
 	try {
 	    Class.forName("org.sqlite.JDBC");
 	    c = DriverManager.getConnection("jdbc:sqlite:test.db");
	} catch ( Exception e ) {
	    JOptionPane.showMessageDialog( this, "Unable to connect to the database.\n"
					   + "Please contact your tech support." );
 	}
 
 	return null;
     }
 
     public void actionPerformed( ActionEvent e )
     {
         String c = e.getActionCommand();
         if( c.equals( "search" ) ) {
 	    // TODO: Implement this.
 	    return;
         }
     }
 
 
     public static JFrame startPollingFrame( )
     {
 	if( frame != null ) {
 	    frame.setVisible( true );
 	    return frame;
 	}
 
 	frame = new JFrame( );
 	frame.setContentPane( new PollingPanel( ) );
 	
 	// TODO: We should ask him again before actually quiting. 
 	frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 	frame.setTitle( "UMSUDemocrat: Polling Place Assistant" );
 	frame.setSize( 500, 250 );
 	frame.setVisible( true );
 
 	return frame;
     }
 }
 
