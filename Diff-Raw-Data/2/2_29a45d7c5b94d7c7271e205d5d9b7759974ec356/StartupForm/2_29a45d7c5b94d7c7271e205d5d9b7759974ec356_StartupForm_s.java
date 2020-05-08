 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 /**
  * The startup form with buttons to host/connect.
  * @author Phillip Cohen
  */
 public class StartupForm extends JFrame implements ActionListener
 {
 	// The buttons that start a session.
 	private final JButton hostButton, joinButton;
 
 	public StartupForm()
 	{
 		setLayout( new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ) );
 
 		// ======================
 		// Add the title JPanel.
 		// ======================
 
 		JPanel titlePanel = new JPanel();
 		{
 			JLabel titleLabel = new JLabel( "Keck Lab Banner!" );
 			titleLabel.setForeground( Color.LIGHT_GRAY );
 			titleLabel.setFont( new Font( "Sans serif", Font.BOLD, 28 ) );
 			titlePanel.add( Box.createRigidArea( new Dimension( 1, 30 ) ) );
 			titlePanel.add( titleLabel );
 			titlePanel.setBackground( Color.gray );
 		}
 		add( titlePanel );
 
 		// ==============================
 		// Add the host/connect buttons.
 		// ==============================
 
 		hostButton = new JButton( "Start a new session" );
 		hostButton.addActionListener( this );
 		joinButton = new JButton( "Join existing session" );
 		joinButton.addActionListener( this );
 
 		add( Box.createRigidArea( new Dimension( 1, 25 ) ) );
 		add( hostButton );
 		add( Box.createRigidArea( new Dimension( 1, 5 ) ) );
 		add( joinButton );
 		add( Box.createRigidArea( new Dimension( 1, 35 ) ) );
 
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
 		setTitle( "Keck Lab Banner!" );
 		setDefaultCloseOperation( DISPOSE_ON_CLOSE );
 		setSize( 400, 230 );
 		setResizable( false );
 		setVisible( true );
 	}
 
 	/**
 	 * A button was clicked...
 	 */
 	@Override
 	public void actionPerformed( ActionEvent e )
 	{
 		if ( e.getSource() == hostButton )
 		{
 			// Start the server.
 			try
 			{
 				BannerController controller = new BannerController();
 				int port = Integer.parseInt( JOptionPane.showInputDialog( "Enter the port to host on.", Protocol.DEFAULT_PORT ) );
 				setVisible( false );
 				new BannerFrame( controller, new Server( controller, port ) );
 				dispose();
 			}
 			catch ( NumberFormatException ex )
 			{
 				JOptionPane.showMessageDialog( null, "That's not a number!", "Input error", JOptionPane.ERROR_MESSAGE );
 			}
 		}
 		else if ( e.getSource() == joinButton )
 		{
 			// Start the client.
 			BannerController controller = new BannerController();
 			String input = JOptionPane.showInputDialog( "Enter the server's address and port (e.g. 127.0.0.1:" + Protocol.DEFAULT_PORT + ")." );
 			try
 			{
 				if ( input == null || input.length() < 1 )
 					return;
				if ( input.split( ":" ).length == 1 )
 				{
 					int port = Integer.parseInt( input.split( ":" )[1] );
 					new BannerFrame( controller, new Client( controller, input.split( ":" )[0], port ) );
 					dispose();
 				}
 				else
 					JOptionPane.showMessageDialog( null, "Error parsing that IP address.", "Input error", JOptionPane.ERROR_MESSAGE );
 			}
 			catch ( NumberFormatException ex )
 			{
 				JOptionPane.showMessageDialog( null, "Error parsing that port.", "Input error", JOptionPane.ERROR_MESSAGE );
 			}
 		}
 	}
 
 	/**
 	 * Program entry point.
 	 * @param args Command-line args.
 	 */
 	public static void main( String[] args )
 	{
 		new StartupForm().setVisible( true );
 	}
 }
