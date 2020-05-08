 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 /**
  * Project #2
  * CS 2334, Section 011
  * 9/19/2013
  * <P>
  * This class represents a GUI which allows users to use publication systems.
  * </P>
  * @version 1.0
  */
 public class PublicationSystemGUI extends JFrame
 {
 	private PublicationSystem publicationSystem;
 	
 	private JPanel titlePanel;
 	private JPanel defaultUserPanel;
 	private JPanel modernUserPanel;
 	private JPanel publicationDisplayPanel;
 	
 	private JLabel title;
 	
 	private JLabel userPrompt;
 	private JTextField userInput;
 
 	private JComboBox sortDropdown;
 	private JTextField searchTextField;
 	private JComboBox searchDropdown;
 
 	private JButton defaultImportButton;
 	private JButton defaultSwitchViewButton;
 	private JButton modernImportButton;
 	private JButton modernSwitchViewButton;
 	
 	private JLabel displayLabel;
 
 	private DefaultControlListener controlListener;
 	private ModernControlListener modernControlListener;
 	private DefaultInputListener defaultInputListener;
 
 	private int height;
 	private int width;
 
 	/**
      * Creates a GUI designed to allow a user to quickly access and modify a PublicationSystem.
      * @param             width      			The width of the GUI
      * @param             height        		The height of the GUI
      *
      * <dt><b>Conditions:</b>
      * <dd>PRE -		width >= 520 and height >= 680
      * <dd>POST -		The GUI is ready to go
      */
 	public PublicationSystemGUI(int width, int height){
 		if(width < 520){
 			width = 520;
 		}
 		if(height < 680){
 			height = 680;
 		}
 		this.width = width;
 		this.height = height;
 		
 		publicationSystem = new PublicationSystem();
 		init();
 	}
 	
 	private void init(){
 		controlListener = new DefaultControlListener();
 		modernControlListener = new ModernControlListener();
 		defaultInputListener = new DefaultInputListener();
 		
 		createTitlePanel();
 		createDefaultUserPanel();
 		createModernUserPanel();
 		createPublicationDisplayPanel();
 		
 		FlowLayout flowy = new FlowLayout();
 		flowy.setAlignment(FlowLayout.LEADING);
 		setLayout(flowy);
 		add(titlePanel);
 		add(defaultUserPanel);
 		add(publicationDisplayPanel);
 		
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setSize(width, height);
 		setLocationRelativeTo(null);
 
 	}
 	
 	private void createTitlePanel(){
 		title = new JLabel("PUBLICATION SYSTEM");
 		title.setHorizontalAlignment(JLabel.CENTER);
 		title.setFont(new Font(Font.MONOSPACED, Font.TRUETYPE_FONT, 40));
 		JPanel titlePanel = new JPanel();
 		titlePanel.setPreferredSize(new Dimension(width, 50));
 		titlePanel.setLayout(new BorderLayout());
 		titlePanel.add(title, BorderLayout.CENTER);
 		this.titlePanel = titlePanel;
 	}
 
 	private void createDefaultUserPanel(){
 		JPanel userPanel = new JPanel();
 		JPanel inputPanel = new JPanel();
 		JPanel controlPanel = new JPanel();
 		Dimension componentDimension = new Dimension(160, 32);
 		
 		userPrompt = new JLabel("Enter command");
 		userPrompt.setHorizontalAlignment(JLabel.CENTER);
 		userInput = new JTextField();
 		userPrompt.setPreferredSize(componentDimension);
 		userInput.setPreferredSize(componentDimension);
 		userInput.addKeyListener(defaultInputListener);
 		inputPanel.setPreferredSize(new Dimension(componentDimension.width, componentDimension.height*2 + new FlowLayout().getVgap()*3));
 		inputPanel.add(userPrompt);
 		inputPanel.add(userInput);
 		
 		defaultImportButton = new JButton("Import Publication");
 		defaultImportButton.setPreferredSize(componentDimension);
 		defaultImportButton.addActionListener(controlListener);
 		defaultSwitchViewButton = new JButton("Switch View");
 		defaultSwitchViewButton.setPreferredSize(componentDimension);
 		defaultSwitchViewButton.addActionListener(controlListener);
 		controlPanel.setPreferredSize(new Dimension(componentDimension.width, componentDimension.height*2 + new FlowLayout().getVgap()*3));
 		controlPanel.add(defaultImportButton);
 		controlPanel.add(defaultSwitchViewButton);
 
 		FlowLayout flowy = new FlowLayout();
 		flowy.setHgap(40);
 		userPanel.setLayout(flowy);
 		userPanel.setPreferredSize(new Dimension(width - new FlowLayout().getHgap()*2, componentDimension.height*2 + new FlowLayout().getVgap()*6));
 		userPanel.add(inputPanel);
 		userPanel.add(controlPanel);
 
 		defaultUserPanel = userPanel;
 	}
 
 	private void createModernUserPanel(){
 		JPanel userPanel = new JPanel();
 		JPanel inputPanel = new JPanel();
 		JPanel controlPanel = new JPanel();
 		Dimension componentDimension = new Dimension(160, 32);
 		Dimension smallComponentDimension = new Dimension(80, 32);
 
 		String[] sortOptions = {"Paper Title", "Serial Title", "Author", "Bibliography", "Date", "Digital Identifier"};  //Sort panel stuff
 		JLabel sortLabel = new JLabel("Sort by ");
 		sortLabel.setPreferredSize(smallComponentDimension);
 		sortLabel.setHorizontalAlignment(JLabel.RIGHT);
 		sortDropdown = new JComboBox(sortOptions);
 		sortDropdown.setPreferredSize(smallComponentDimension);
 		sortDropdown.addActionListener(modernControlListener);
 		JPanel sortPanel = new JPanel();
 		sortPanel.setPreferredSize(new Dimension(smallComponentDimension.width * 2 + new FlowLayout().getHgap()*3, smallComponentDimension.height + new FlowLayout().getVgap()));
 		sortPanel.add(sortLabel);
 		sortPanel.add(sortDropdown);
 		JLabel searchLabel1 = new JLabel("Search for ");  //Search panel stuff
 		searchLabel1.setPreferredSize(smallComponentDimension);
 		searchLabel1.setHorizontalAlignment(JLabel.RIGHT);
 		JLabel searchLabel2 = new JLabel("Search by ");  //Search panel stuff
 		searchLabel2.setPreferredSize(smallComponentDimension);
 		searchLabel2.setHorizontalAlignment(JLabel.RIGHT);
 		String[] searchOptions = {"Title"};
 		searchDropdown = new JComboBox(searchOptions);
 		searchDropdown.setPreferredSize(smallComponentDimension);
 		searchDropdown.addActionListener(modernControlListener);
 		searchTextField = new JTextField();
 		searchTextField.setPreferredSize(smallComponentDimension);
 		searchTextField.addKeyListener(modernControlListener);
 		JPanel searchPanel1 = new JPanel();
 		JPanel searchPanel2 = new JPanel();
 		searchPanel1.add(searchLabel1);
 		searchPanel1.add(searchTextField);
 		searchPanel2.add(searchLabel2);
 		searchPanel2.add(searchDropdown);
 		searchPanel1.setPreferredSize(new Dimension(smallComponentDimension.width * 2 + new FlowLayout().getHgap()*3, smallComponentDimension.height + new FlowLayout().getVgap()));
 		searchPanel2.setPreferredSize(new Dimension(smallComponentDimension.width * 2 + new FlowLayout().getHgap()*3, smallComponentDimension.height + new FlowLayout().getVgap()));
 		JButton printButton = new JButton("Print to File"); //Print panel stuff
 		printButton.setPreferredSize(componentDimension);
 		JPanel printPanel = new JPanel();
 		printPanel.add(printButton);
 		printPanel.setPreferredSize(new Dimension(smallComponentDimension.width * 2 + new FlowLayout().getHgap()*3, smallComponentDimension.height + new FlowLayout().getVgap()));
 		inputPanel.setPreferredSize(new Dimension(smallComponentDimension.width * 2 + new FlowLayout().getHgap()*3, smallComponentDimension.height*4 + new FlowLayout().getVgap()*10));
 		inputPanel.add(sortPanel);
 		inputPanel.add(searchPanel1);
 		inputPanel.add(searchPanel2);
 		inputPanel.add(printPanel);
 
 		modernImportButton = new JButton("Import Publication");
 		modernImportButton.setPreferredSize(componentDimension);
 		modernImportButton.addActionListener(modernControlListener);
 		modernSwitchViewButton = new JButton("Switch View");
 		modernSwitchViewButton.setPreferredSize(componentDimension);
 		modernSwitchViewButton.addActionListener(modernControlListener);
 		controlPanel.setPreferredSize(new Dimension(componentDimension.width, componentDimension.height*2 + new FlowLayout().getVgap()*3));
 		controlPanel.add(modernImportButton);
 		controlPanel.add(modernSwitchViewButton);
 		
 		FlowLayout flowy = new FlowLayout();
 		flowy.setHgap(40);
 		userPanel.setLayout(flowy);
 		userPanel.setPreferredSize(new Dimension(width - new FlowLayout().getHgap()*2, smallComponentDimension.height*4 + new FlowLayout().getVgap()*12));
 		userPanel.add(inputPanel);
 		userPanel.add(controlPanel);
 		
 		modernUserPanel = userPanel;
 	}
 	
 	private void createPublicationDisplayPanel(){
 		displayLabel = new JLabel();
 		JScrollPane pane = new JScrollPane(displayLabel);
 		JPanel displayPanel = new JPanel();
 		displayPanel.setPreferredSize(new Dimension(width - new FlowLayout().getHgap()*2, 400));
 		displayPanel.setLayout(new BorderLayout());
 		displayPanel.add(pane, BorderLayout.CENTER);
 		publicationDisplayPanel = displayPanel;
 	}
 
 	/**
     * Makes the GUI visible to the user/
     */
 	public void open(){
 		setVisible(true);
 	}
 
 	/**
     * Makes the GUI invisible to the user/
     */
 	public void close(){
 		setVisible(false);
 	}
 	
 	private void switchView(){
 		if(getContentPane().getComponent(1) == defaultUserPanel){
 			remove(defaultUserPanel);
 			add(modernUserPanel, 1);
 		}
 		else if(getContentPane().getComponent(1) == modernUserPanel){
 			remove(modernUserPanel);
 			add(defaultUserPanel, 1);
 		}
 		validate();
 		getContentPane().repaint();
 	}
 	
 	private void importPublication(){
 		publicationSystem.importPublication();
 	}
 	
 	/**
 	 * Searches for text according to terms dictated by the search drop down component
 	 * @param text			The text to search for
 	 */
 	private void searchFor(String text){
 		switch(searchDropdown.getSelectedIndex()){
 		case 0:
 			performTask("PT");
 			break;
 		case 1:
 			performTask("ST");
 			break;
 		case 2:
 			performTask("AN");
 			break;
 		case 3:
 			performTask("BI");
 			break;
 		case 4:
 			performTask("CH");
 			break;
 		case 5:
 			performTask("DI");
 			break;
 		default:
 			break;
 		}
 	}
 	
 	private void performTask(String task){
 		if(task.equals("BI")){  //Bibliographic sort
 			publicationSystem.sortByBibliographicInfo();
 		}
 		else if(task.equals("AN")){  //Author sort
 			publicationSystem.sortByAuthor();
 		}
 		else if(task.equals("PT")){  //Paper title sort
 			publicationSystem.sortByPaperTitle();
 		}
 		else if(task.equals("ST")){  //Serial title sort
 			publicationSystem.sortBySerialTitle();
 		}
 		else if(task.equals("CH")){  //Chronological sort
 			publicationSystem.sortByDate();
 		}
 		else if(task.equals("R")){  //Random sort
 			publicationSystem.randomSort();
 		}
 		else if(task.equals("DI")){  //
 			publicationSystem.sortByDigitalIdentifier();
 		}
 		else if(task.equals("PF")){  //Print to file
 			String fileName = "";
 			try{
 				fileName = JOptionPane.showInputDialog("Enter new file name", "Print to File");
 				publicationSystem.printPublicationsToFile(fileName);
 			}
 			catch(Exception e){e.printStackTrace();}
 			return;
 		}
 		else if(task.equals("S")){  //Search
 			String searchQuery = "";
 			try{
 				searchQuery = JOptionPane.showInputDialog("Enter title to search", "Search by Title");
 			}
 			catch(Exception e){e.printStackTrace();}
 			Paper paperFound = publicationSystem.getPaperLinear(searchQuery);
 			if(paperFound == null){
 				JOptionPane.showMessageDialog(null, "Paper not found", "", JOptionPane.ERROR_MESSAGE);
 			}
 			else{
 				displayLabel.setText(paperFound.toString());
 				return;
 			}
 		}
 		//By default the method prints the publicationList to the screen
 		displayLabel.setText("<html>");
 		for(Paper p: publicationSystem.getPublicationList()){
 			displayLabel.setText(displayLabel.getText() + "<p>" + p.toString() + "</p>");
 		}
 		displayLabel.setText(displayLabel.getText() + "</html>");
 	}
 
 	private class DefaultControlListener implements ActionListener{
 		public void actionPerformed(ActionEvent event){
 			if(event.getSource().equals(defaultImportButton)){
 				importPublication();
 			}
 			else if(event.getSource().equals(defaultSwitchViewButton)){
 				switchView();
 			}
 		}
 	}
 
 	private class ModernControlListener implements ActionListener, KeyListener{
 		public void actionPerformed(ActionEvent event){
 			if(event.getSource() == modernImportButton){
 				importPublication();
 			}
 			else if(event.getSource() == modernSwitchViewButton){
 				switchView();
 			}
 		}
 		public void keyPressed(KeyEvent event){
 			searchFor(searchTextField.getText());
 		}
 		public void keyReleased(KeyEvent event){
 		}
 		public void keyTyped(KeyEvent event){
 		}
 	}
 
 	private class DefaultInputListener extends KeyAdapter{
 		public void keyPressed(KeyEvent event){
 			if(event.getSource() == userInput){
 				performTask(userInput.getText().toUpperCase());
 			}
 		}
 	}
 }
