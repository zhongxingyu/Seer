 import java.awt.*;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JTextPane;
 
 import java.awt.Dimension;
 
 import javax.swing.JLabel;
 import javax.swing.JEditorPane;
 import javax.swing.border.LineBorder;
 import javax.swing.FocusManager;
 
 import java.awt.Color;
 import java.awt.event.*;
 
 import javax.swing.JButton;
 
 import java.awt.Font;
 import java.awt.TextArea;
 import java.awt.ScrollPane;
 import java.util.*;
 import java.io.*;
 
 
 @SuppressWarnings("serial")
 public class MainWin extends JFrame implements ActionListener
 {
 	private final int WINWIDTH = 1000;
 	private final int WINHEIGHT = 600;
 	private final int LABELSIZE = 45;
 	private final int BORDER = 25;
 	private final int TOP = 0;
 	private final int EDIT_AREA_HEIGHT = 25;
 	private final int OUTPUT_AREA_WIDTH = WINWIDTH/2 - LABELSIZE/2;
 	private final int OUTPUT_AREA_HEIGHT = WINHEIGHT - EDIT_AREA_HEIGHT - BORDER;
 	private final int MAP_AREA_WIDTH = WINWIDTH/2 - LABELSIZE/2;
 	private final int MAP_AREA_HEIGHT = WINHEIGHT - BORDER;
 	private final String PATH_TO_FILES = "src/files/";
 	private final Font MAP_FONT = new Font("Consolas", 0, 12);
 	
 	private StringBuilder rightSideText = new StringBuilder();
 		
 	private JPanel contentPane;
 	private JTextPane mapArea;
 	private JTextField inputArea;
 	private JLabel lblNewLabel;
 	private JLabel label;
 	private JTextPane outputArea;
 	private JScrollPane scrollPane;
 	private JLabel label_1;
 	private Scanner emptyMapInfile;
 	
 	TwoDMap theMap = new TwoDMap(68, 38, "testRoom");
 	
 	/**
 	 * Launch the application.
 	 
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try
 				{
 					MainWin frame = new MainWin();
 					frame.setVisible(true);
 				}
 				catch (Exception e) 
 				{
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	*/
 	/**
 	 * Create the frame.
 	 */
 	public MainWin() 
 	{
 		
 		setWindow();
 		createInputArea();
 		createMapArea();
 		setImages();
 		setOutputArea();
 	}
 	
 	private String getMap() {
 	    
         theMap.add(16, 8, new TileActor('M', new TileTestMonster()));
         theMap.add(18, 10, new TileActor('M', new TileTestMonster()));
         
         int i;
         
         for(i = 6; i < 34; i++)
             theMap.add(i, 5, new TileActor('=', new TileActorWall()));
         for(i = 6; i < 19; i++)
             theMap.add(5, i, new TileActor('║', new TileActorWall()));
         for(i = 6; i < 19; i++)
             theMap.add(34, i, new TileActor('║', new TileActorWall()));
         for(i = 6; i < 17; i++)
             theMap.add(i, 19, new TileActor('═', new TileActorWall()));
         for(i = 23; i < 34; i++)
             theMap.add(i, 19, new TileActor('═', new TileActorWall()));
         
         theMap.add(5,  5,   new TileActor('╔', new TileActorWall()));
         theMap.add(5,  19,  new TileActor('╚', new TileActorWall()));
         theMap.add(34, 5,   new TileActor('╗', new TileActorWall()));
         theMap.add(34, 19,  new TileActor('╝', new TileActorWall()));
         theMap.add(17, 19,  new TileActor('O', new TileActorWall()));
         theMap.add(22, 19,  new TileActor('O', new TileActorWall()));
 	    
 	    return theMap.toString();
 	    
 	}
 	
 	private Scanner getInputFile(String filename){
 		
 		Scanner scan = null;
 		try{  scan = new Scanner(new FileReader(PATH_TO_FILES + filename));  }
 		
 		catch(FileNotFoundException fnf)
 		{
 			System.err.println(fnf);
 			System.exit(0);
 		}
 		return scan;
 		
 	}
 	private void setWindow(){
 		
 		Toolkit tk = Toolkit.getDefaultToolkit();
 		Dimension dim = tk.getScreenSize();
 
 		int x_pos = dim.width/2 - WINWIDTH/2;
 		int y_pos = dim.height/2 - WINHEIGHT/2;
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(x_pos, y_pos, WINWIDTH, WINHEIGHT);
 		setSize(WINWIDTH, WINHEIGHT);
 		setResizable(false);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		contentPane.requestFocus();
 		
 	}
 	private void createMapArea(){
 		mapArea = new JTextPane();
 		mapArea.setFont(MAP_FONT);
 		mapArea.setText(getMap());
 		mapArea.setBackground(Color.LIGHT_GRAY);
 		mapArea.setEditable(false);
 		mapArea.setBounds(LABELSIZE, TOP,MAP_AREA_WIDTH, MAP_AREA_HEIGHT);
 		contentPane.add(mapArea);
 		mapArea.setBorder(new LineBorder(Color.black));
 	}
 	private void createInputArea(){
 		//editable area to enter commands
 		inputArea = new JTextField();
 		inputArea.setText("Enter Commands Here");
 		inputArea.setBounds(LABELSIZE + MAP_AREA_WIDTH, WINHEIGHT- EDIT_AREA_HEIGHT - BORDER, OUTPUT_AREA_WIDTH, EDIT_AREA_HEIGHT);
 		contentPane.add(inputArea);
 		
 
 	}
 	public void clearInput(){
 		inputArea.setText("");
 	}
 	private void setImages(){
 		// useful images off to the right
 		lblNewLabel = new JLabel("<html>Images of...something</html>");
 		lblNewLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
 		lblNewLabel.setBounds(0, TOP, LABELSIZE, LABELSIZE);
 		contentPane.add(lblNewLabel);
 		
 		label = new JLabel("Image of something");
 		label.setBorder(new LineBorder(new Color(0, 0, 0)));
 		label.setBounds(0, 45, LABELSIZE, LABELSIZE);
 		contentPane.add(label);
 		
 		label_1 = new JLabel("Image of something");
 		label_1.setBorder(new LineBorder(new Color(0, 0, 0)));
 		label_1.setBounds(0, 90, LABELSIZE, LABELSIZE);
 		contentPane.add(label_1);
 
 	}
 	private void setOutputArea(){
 		
 		//uneditable TextPane to read output
 		outputArea = new JTextPane();
 		
 		outputArea.setText("");
 		outputArea.setBackground(Color.LIGHT_GRAY);
 		outputArea.setEditable(false);
 		outputArea.setBounds(LABELSIZE + MAP_AREA_WIDTH, TOP, OUTPUT_AREA_WIDTH, OUTPUT_AREA_HEIGHT);
		contentPane.add(outputArea);
 	}
 	private String defaultOutputText(){
 		Scanner defaultFile = getInputFile("defaultOutput.txt");
 		String ret = "";
 		while(defaultFile.hasNext()){
 			ret += defaultFile.nextLine();
 		}
 		defaultFile.close();
 		return ret;
 	}
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void setOutput(String text){
		rightSideText.append(text);
 		outputArea.setText(rightSideText.toString());
 	}
 	
 	public void setOutput(PrintStream input){
 		outputArea.setText("");
 	}
 	public JTextField getInputArea(){
 		return inputArea;
 	}
 	public void postOutput(String str){
 		String currentText = outputArea.getText();
 		outputArea.setText(currentText + "\n\n"+  str );
 	}
 }
