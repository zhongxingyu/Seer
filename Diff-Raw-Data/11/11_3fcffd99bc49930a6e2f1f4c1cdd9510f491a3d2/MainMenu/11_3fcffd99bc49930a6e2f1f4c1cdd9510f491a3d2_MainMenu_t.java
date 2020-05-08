 package maze;
 
 import java.awt.*;
 
 import javax.swing.*;
 import javax.swing.border.*;
 import java.awt.event.*;
 
 public class MainMenu extends JFrame {
 
 	private JPanel contentPane;
 	private static JRadioButton rdbtnLow = new JRadioButton("Low");
 	private static JRadioButton rdbtnMedium = new JRadioButton("Medium");
 	private static JRadioButton rdbtnHigh = new JRadioButton("High");
 	private final MainMenu mainFrame;		//Added 5/10 AA
 	public static int getBoardSize(){
 		int size = 10;
 		if(rdbtnLow.isSelected())
 			return size = 5;
 		else if(rdbtnMedium.isSelected())
 			return size = 10;
 		else if(rdbtnHigh.isSelected())
 			return size = 20;
 		else
 			return size;
 	}
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainMenu frame = new MainMenu();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public MainMenu() {
 		mainFrame = this;
 		setTitle("Adam & Jason's Recursive Maze Traversal Application");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 500, 400);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(new BorderLayout(0, 0));
 
 		JPanel optionPanel = new JPanel();
 		contentPane.add(optionPanel, BorderLayout.SOUTH);
 		optionPanel.setLayout(new BorderLayout(0, 0));
 
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setBorder(new TitledBorder(null, "Select maze option", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		optionPanel.add(buttonPanel, BorderLayout.CENTER);
 		JButton btnBuildMaze = new JButton("Build A Maze");
 		buttonPanel.add(btnBuildMaze);
 
 		JButton btnRamdomMaze = new JButton("Random Maze");
 		buttonPanel.add(btnRamdomMaze);
 
 		ButtonGroup group = new ButtonGroup();
 		group.add(rdbtnLow);
 		group.add(rdbtnMedium);
 		group.add(rdbtnHigh);
 
 		JPanel radioPanel = new JPanel();
 		radioPanel.setBorder(new TitledBorder(null, "Select level of complexity", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		optionPanel.add(radioPanel, BorderLayout.WEST);
 		radioPanel.add(rdbtnLow);
 		radioPanel.add(rdbtnMedium);
 		radioPanel.add(rdbtnHigh);
 		rdbtnLow.setSelected(true);
 		btnRamdomMaze.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				RandomMaze randMaze = new RandomMaze();
 				randMaze.setParent(mainFrame);		//Added 5/10 AA
 				randMaze.setVisible(true);
 				setEnabled(false);
 			}
 		});
 		btnBuildMaze.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Maze maze = new Maze();
 				maze.setParent(mainFrame);			//Added 5/10 AA
 				maze.setVisible(true);
 				setEnabled(false);
 			}
 		});
 		JPanel titlePanel = new JPanel();
 		contentPane.add(titlePanel, BorderLayout.NORTH);
 
 		JLabel lblTitle = new JLabel("Recursive Maze Traversal Application");
 		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 24));
 		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
 		titlePanel.add(lblTitle);
 
 		JLabel imagePanel = new JLabel(new ImageIcon("BG.jpg"));	//Added 5/10 AA
 		contentPane.add(imagePanel, BorderLayout.CENTER);
 
 		Canvas imageCanvas = new Canvas();
 		imagePanel.add(imageCanvas);
 	}
 
 
 
}
