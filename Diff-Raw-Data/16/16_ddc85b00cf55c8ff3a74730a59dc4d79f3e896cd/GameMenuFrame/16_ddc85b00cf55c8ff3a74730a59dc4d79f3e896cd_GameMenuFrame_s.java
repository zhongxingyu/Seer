 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 
 public class GameMenuFrame extends JFrame implements ActionListener
 {
 	private GamePanel gp = new GamePanel();
 	
 	private JButton fireButton;
 	private JButton octopusButton;
 	private JButton flagmanButton;
 	private JButton turtleButton;
 	private JButton oilButton;
 	private JButton cementButton;
 	private JButton helmetButton;
 	private JButton chefButton;
 	
 	public GameMenuFrame()
 	{
 		super( "Game and Watch Collection" );
 		
 		setLayout( null );
 		
 		fireButton = new JButton();
		fireButton.setIcon( new ImageIcon( "GamePics/fireIcon.png" ) );
 		fireButton.setBounds( 50, 120, 325, 100 );
 		fireButton.addActionListener( this );
 		
 		octopusButton = new JButton();
 		//octopusButton.setIcon( new ImageIcon( "" ) );
 		octopusButton.setBounds( 50, 240, 325, 100 );
 		octopusButton.addActionListener( this );
 		
 		flagmanButton = new JButton();
 		flagmanButton.setIcon( new ImageIcon( "GamePics/flagmanIcon.png" ) );
 		flagmanButton.setBounds( 50, 360, 325, 100 );
 		flagmanButton.addActionListener( this );
 		
 		turtleButton = new JButton();
 		//turtleButton.setIcon( new ImageIcon( "" ) );
 		turtleButton.setBounds( 50, 480, 325, 100 );
 		turtleButton.addActionListener( this );
 		
 		oilButton = new JButton();
 		oilButton.setIcon( new ImageIcon( "GamePics/oilIcon.png" ) );
 		oilButton.setBounds( 425, 120, 325, 100 );
 		oilButton.addActionListener( this );
 		
 		cementButton = new JButton();
 		//cementButton.setIcon( new ImageIcon( "" ) );
 		cementButton.setBounds( 425, 240, 325, 100 );
 		cementButton.addActionListener( this );
 		
 		helmetButton = new JButton();
 		helmetButton.setIcon( new ImageIcon( "GamePics/helmetIcon.png" ) );
 		helmetButton.setBounds( 425, 360, 325, 100 );
 		helmetButton.addActionListener( this );
 		
 		chefButton = new JButton();
 		//chefButton.setIcon( new ImageIcon( "" ) );
 		chefButton.setBounds( 425, 480, 325, 100 );
 		chefButton.addActionListener( this );
 		
 		add( fireButton );
 		add( octopusButton );
 		add( flagmanButton );
 		add( turtleButton );
 		add( oilButton );
 		add( cementButton );
 		add( helmetButton );
 		add( chefButton );
 	}
 
 	@Override
 	public void actionPerformed( ActionEvent event )
 	{
		
 	}
 }
