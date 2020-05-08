 package recycleBuddy;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 
 import javax.swing.JPanel;
 
 public class RBWindow extends JFrame {
 	
 	// Constants for the window size and number of options.
 	public static final int WIDTH = 600;
 	public static final int HEIGHT = 400;
 	public static final int NUM_OPTIONS = 6;
 	
 	// References to the window's buttons.
 	private RBButton[] options;
 	private JButton[] sideOptions;
 	private JButton back;
 	private JButton home;
 	
 	// Reference to RecycleBuddy's model.
 	private RBModel model;
 	
 	// enum for different button types options.
 	enum ButtonTypes {
 		HOME,
 		BACK,
 		OPTION,
 		SIDE_OPTION
 	}
 
 	RBWindow() {
 		// Set the window's default size and behavior.
 		super();
 		setSize(WIDTH, HEIGHT);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		ImageIcon temp = new ImageIcon("RecycleBuddy.png");
 		this.setIconImage(temp.getImage());
		this.setTitle("Recycle Buddy");
 		
 		// Create RecycleBuddy's model.
 		model = new RBModel(NUM_OPTIONS, this);
 		
 		// Set the layout for the window.
 		setLayout(new BorderLayout());
 		
 		// Create panel for the side pane buttons.
 		JPanel pPan = new JPanel();
 		pPan.setLayout(new GridLayout(6,1));
 		add(pPan, BorderLayout.WEST);
 		
 		// Add buttons to the side pane.
 		sideOptions = new JButton[NUM_OPTIONS];
 		for(int i = 0; i < sideOptions.length; i++) {
 			sideOptions[i] = new JButton("test");
 			sideOptions[i].addActionListener(new SideOptionListener(model, i));
 			pPan.add(sideOptions[i]);
 		}
 		
 		// Create panel for the option buttons.
 		JPanel bPan = new JPanel();
 		bPan.setLayout(new GridLayout(2, 3));
 		add(bPan, BorderLayout.CENTER);
 		
 		// Add buttons to the options panel.
 		options = new RBButton[NUM_OPTIONS];
 		for(int i = 0; i < options.length; i++) {
 			options[i] = new RBButton("test", "test.png");
 			options[i].addActionListener(new OptionListener(model, i));
 			bPan.add(options[i]);
 		}
 		
 		// Create panel for the home and back buttons.
 		JPanel pan = new JPanel();
 		pan.setLayout(new FlowLayout());
 		add(pan, BorderLayout.SOUTH);
 		
 		// Add the home and back buttons.
 		back = new JButton("Back");
 		back.addActionListener(new BackListener(model));
 		pan.add(back);	
 		home = new JButton("Home");
 		home.addActionListener(new HomeListener(model));
 		pan.add(home);
 	}
 	
 	public void changeView(ButtonTypes option, int buttonNum) {
 		if(option == ButtonTypes.OPTION) {
 			for(int i = 0; i < options.length; i++) {
 				options[i].setText("test2");
 			}
 		}
 		else {
 			for(int i = 0; i < options.length; i++) {
 				options[i].setText("test");
 			}
 		}
 	}
 	
 	public void changeView(ButtonTypes option) {
 		changeView(option, 0);
 	}
 	
 	public void refreshOption(int button, String text, String image, 
 			boolean active) {
 		options[button].setText(text);
 		options[button].setImage(image);
 		options[button].setEnabled(active);
 	}
 	
 	public void refreshSideOption(int button, String text, boolean active) {
 		sideOptions[button].setText(text);
 		sideOptions[button].setEnabled(active);
 	}
 	
 }
