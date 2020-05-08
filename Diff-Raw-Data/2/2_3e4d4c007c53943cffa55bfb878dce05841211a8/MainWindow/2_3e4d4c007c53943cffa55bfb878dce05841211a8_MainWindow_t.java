 package views;
 
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 
 import views.components.ImagePanel;
 import views.components.NicePanel;
 
 import java.awt.Container;
 import java.awt.Color;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 
 public class MainWindow {
 
 	private JFrame frame;
 	private JTextField txtVulHierJe;
 	private Container ownPanel;
 	
 	/**
 	 * static variables
 	 */
 	private static final String TXT_PLACEH_NAAM_INPUT = "Vul hier je naam in";
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainWindow window = new MainWindow();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public MainWindow() {
 		initialize();
 	}
 	
 	public void reset()
 	{
 		frame.setContentPane(ownPanel);
 		((JPanel) ownPanel).updateUI();
 	}
 
 	public void openPanel(JPanel panel) {
 		frame.setContentPane(panel);
 		panel.updateUI();
 	}
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		ownPanel = new NicePanel();
 		frame.setContentPane(ownPanel);
 		//frame.getContentPane().setBackground(Color.BLACK);
 		frame.setBounds(100, 100, 1280, 720);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(new MigLayout("", "[300.00,grow,left][100.00,grow,fill][100.00,grow][300.00,grow]", "[grow,fill][150px:150px:150px,fill][][][][][][100.00,grow]"));
 		
 		//Panel panel = new Panel();
 		//panel.;
 		//frame.getContentPane().add(panel, "cell 1 0 4 1,grow");
 		
 		ImagePanel hoofdmenuPlaatje;
 		try {
 			//panel.setLayout(new MigLayout("", "[1.00,grow][176.00px][1.00,grow]", "[grow][135.00px][grow]"));
 			hoofdmenuPlaatje = new ImagePanel("images/PSWTitel.png");
 			hoofdmenuPlaatje.setSize(176, 204);
 			
 			//panel.add(hoofdmenuPlaatje, "cell 1 0 4 1,grow");
 			frame.getContentPane().add(hoofdmenuPlaatje, "cell 1 1 2 1,alignx left,aligny baseline");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		txtVulHierJe = new JTextField();
 		txtVulHierJe.setText(TXT_PLACEH_NAAM_INPUT);
 		frame.getContentPane().add(txtVulHierJe, "cell 1 2 2 1,growx");
 		txtVulHierJe.setColumns(10);
 
 		
 		//Als er met de mouse geklikt wordt zal de placeholder text verwijderd worden
 		txtVulHierJe.addMouseListener( new MouseAdapter() {
 			public void mouseClicked(MouseEvent arg0) {
 				if ( arg0.getButton() == 0x1 && txtVulHierJe.getText().equals(TXT_PLACEH_NAAM_INPUT) )
 					txtVulHierJe.setText("");
 			}
 		} );
 		//Placeholder wijzgen d.m.v. een keypress
 		txtVulHierJe.addKeyListener( new KeyAdapter() { 
 			public void keyPressed( KeyEvent e ) {
 				if ( txtVulHierJe.getText().equals(TXT_PLACEH_NAAM_INPUT) ) {
 						txtVulHierJe.setText("");
 				}
 			}
 		} );
 		
 		JButton btnNewButton = new JButton("Spelregels");
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				HelpScherm helpScherm = new HelpScherm();
 				openPanel(helpScherm);
 			}
 		});
 		frame.getContentPane().add(btnNewButton, "cell 1 3,growx");
 		
 		JButton btnNewButton_3 = new JButton("Start");
 		
 		txtVulHierJe.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 				if(arg0.getKeyCode() == KeyEvent.VK_ENTER)
 				{
 					startGame();
 				}
 			}
 		});
 		
 		btnNewButton_3.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				startGame();
 			}
 		});
 		frame.getContentPane().add(btnNewButton_3, "cell 2 3 1 3,grow");
 		
 		JButton btnNewButton_1 = new JButton("Beheer");
 		frame.getContentPane().add(btnNewButton_1, "cell 1 4,growx");
 		
 		JButton btnNewButton_2 = new JButton("Highscores");
 		final Highscore highscore = new Highscore(this);
 		btnNewButton_2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				openPanel(highscore);
 			}
 		});
 		frame.getContentPane().add(btnNewButton_2, "cell 1 5,growx");
 	}
 	private void startGame()
 	{
 		KiesOnderwerp kiesOnderwerp = new KiesOnderwerp(this);
 		String naamNo = txtVulHierJe.getText();
 		
		if (naamNo.equals(TXT_PLACEH_NAAM_INPUT) || naamNo.isEmpty())
 		{
 			txtVulHierJe.setBackground(Color.red);
 			txtVulHierJe.selectAll();
 		}
 		else
 		{
 			openPanel(kiesOnderwerp);
 		}
 	}
 }
