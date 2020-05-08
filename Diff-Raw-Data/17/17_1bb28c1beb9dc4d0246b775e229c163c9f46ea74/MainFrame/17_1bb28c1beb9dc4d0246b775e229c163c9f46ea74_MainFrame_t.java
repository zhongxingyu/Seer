 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.io.File;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 public class MainFrame extends JFrame implements ActionListener
 {
 	private JButton sendButton;
 
 	private JLabel receiveStatus, localIP;
 
 	public MainFrame()
 	{
 		setLayout( new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ) );
 
 		// ======================
 		// Add the title JPanel.
 		// ======================
 
 		JPanel titlePanel = new JPanel();
 		{
 			JLabel titleLabel = new JLabel( "P2P File Transfer!" );
 			titleLabel.setForeground( Color.LIGHT_GRAY );
 			titleLabel.setFont( new Font( "Sans serif", Font.BOLD, 28 ) );
 			titlePanel.add( Box.createRigidArea( new Dimension( 1, 30 ) ) );
 			titlePanel.add( titleLabel );
 			titlePanel.setBackground( Color.gray );
 		}
 		add( titlePanel );
 
 		// ======================
 		// Add the receive label.
 		// ======================
 		sendButton = new JButton( "Send a file..." );
 		sendButton.addActionListener( this );
 
 		JPanel receivePanel = new JPanel();
 		{
 			// receivePanel.setLayout( new BoxLayout( receivePanel, BoxLayout.Y_AXIS ) );
			receivePanel.add( Box.createHorizontalGlue() );
			receivePanel.add( Box.createHorizontalStrut( 10 ) );
 			receiveStatus = new JLabel( "Server starting..." );
 			receiveStatus.setFont( receiveStatus.getFont().deriveFont( Font.BOLD ) );
 			receivePanel.add( receiveStatus );
 
 			localIP = new JLabel( "" );
 			receivePanel.add( localIP );
 
 			// receivePanel.add( new JButton( "Copy" ) );
			receivePanel.add( Box.createHorizontalStrut( 5 ) );
			receivePanel.setPreferredSize( new Dimension( 450, 75 ) );
 			receivePanel.add( sendButton );
			receivePanel.add( Box.createHorizontalStrut( 10 ) );
			receivePanel.add( Box.createHorizontalGlue() );
 		}
		add( Box.createVerticalStrut( 10 ) );
 		add( receivePanel );
 
 		// ================
 		// Add the footer.
 		// ================
 
 		JPanel footerPanel = new JPanel();
 		{
 			JLabel footerLabel = new JLabel( "Version 1.0 / created by Phillip Cohen" );
 			footerLabel.setForeground( Color.gray );
 			footerPanel.add( footerLabel );
 		}
 		add( footerPanel );
 
 		// Other attributes...
 		updateLabels();
 		setTitle( "P2P File Transfer!" );
 		setDefaultCloseOperation( DISPOSE_ON_CLOSE );
		setMinimumSize( new Dimension( 350, 175 ));
		pack();
 		setResizable( false );
 		setVisible( true );
 
 		addWindowListener( new java.awt.event.WindowAdapter()
 		{
 			@Override
 			public void windowClosing( WindowEvent winEvt )
 			{
 				// Perhaps ask user if they want to save any unsaved files first.
 				System.exit( 0 );
 			}
 		} );
 	}
 
 	public void updateLabels()
 	{
 		if ( Main.getListener() != null )
 		{
 			localIP.setVisible( Main.getListener().isEnabled() );
 			if ( !Main.getListener().isEnabled() )
 				receiveStatus.setText( "Server starting..." );
 			else
 			{
 				receiveStatus.setText( "Ready to receive files!" );
 				localIP.setText( "Your IP is: " + Main.getListener().getServerIP() );
 			}
 		}
 		invalidate();
 	}
 
 	@Override
 	public void actionPerformed( ActionEvent arg0 )
 	{
 		// Create a file chooser
 		final JFileChooser fc = new JFileChooser();
 
 		// In response to a button click:
 		int returnVal = fc.showOpenDialog( this );
 
 		if ( returnVal == JFileChooser.APPROVE_OPTION )
 		{
 			File file = fc.getSelectedFile();
 			if ( fc.getSelectedFile().length() > Long.MAX_VALUE )
 			{
 				JOptionPane.showMessageDialog( this, "The file you selected is too big; the max file size that can be transferred is " + Util.formatFileSize( Long.MAX_VALUE ) + "." );
 				return;
 			}
 
 			// Read the connection address.
 			String input = JOptionPane.showInputDialog( "Enter the address and port you'd like to send this file to (with port).", "127.0.0.1:" + Listener.DEFAULT_PORT );
 			try
 			{
 				if ( ( input == null ) || ( input.length() < 1 ) )
 					return;
 				if ( input.split( ":" ).length == 2 )
 				{
 					// Create the main frame, server, and controller.
 					int port = Integer.parseInt( input.split( ":" )[1] );
 					new OutgoingTransfer( file, input.split( ":" )[0], port );
 				}
 			}
 			catch ( NumberFormatException ex )
 			{
 				JOptionPane.showMessageDialog( null, "Error parsing that port.", "Input error", JOptionPane.ERROR_MESSAGE );
 			}
 
 		}
 	}
 }
