 package commsbook;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.net.URL;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.KeyStroke;
 import javax.swing.border.Border;
 import javax.swing.border.BevelBorder;
 
 
 import commsbook.model.Category;
 import commsbook.model.CategoryItem;
 import commsbook.model.Symbol;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.sun.speech.freetts.Voice;
 import com.sun.speech.freetts.VoiceManager;
 
 public class AppWindow {
 
 	private JFrame frame;
 	// I know JFileChooser looks crap, but we need folder selection.
 	// Kick Sun's Oracle 0wned arse if you want this to get better
 	// http://bugs.sun.com/view_bug.do?bug_id=6192906
 	// or find some other library. :'-(
 	JFileChooser libraryFolderChooser = new JFileChooser();
 	private JPanel panel_category;
 	private JPanel panel_sentence;
 	private JPanel panel_path;
 	
 	@Parameter(names={"-library", "-l"}, description="path to library to open")
 	private String libraryPathArg;
 	
 	@Parameter(names={"-test", "-t"}, description="test voice output")
 	private boolean testRun;
 	
 	private JCommander args;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(final String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					AppWindow window = new AppWindow(args);
 					if (window.frame != null){ // will be null if not showing the UI.
						// maximise the window - ref http://stackoverflow.com/a/5207711/10245
						window.frame.setExtendedState(window.frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
						//show
 						window.frame.setVisible(true);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 * @param args 
 	 */
 	public AppWindow(String[] args) {
 		this.args = new JCommander(this, args);
 		if (testRun){
 			speakSentence("Testing voice output. It's working!");
 			return;
 		}
 		initialize();
 		// set up the library selection dialog
 		libraryFolderChooser
 				.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		libraryFolderChooser.setDialogTitle("Select a library folder");
 		if (libraryPathArg != null){
 			File path = new File(libraryPathArg);
 			loadCategory(path);
 		}
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 450, 300);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		JMenuBar menuBar = new JMenuBar();
 		frame.setJMenuBar(menuBar);
 
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 		
 		JMenuItem mntmLoadLibrary = new JMenuItem("Load Library...");
 		mntmLoadLibrary.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				loadLibrary();
 			}
 		});
 		mntmLoadLibrary.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
 				InputEvent.CTRL_MASK));
 		mnFile.add(mntmLoadLibrary);
 
 		JMenuItem mntmQuit = new JMenuItem("Quit");
 		mntmQuit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				frame.dispose(); //close the app
 			}
 		});
 		mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
 				InputEvent.CTRL_MASK));
 		mnFile.add(mntmQuit);
 
 		JMenu mnHelp = new JMenu("Help");
 		menuBar.add(mnHelp);
 
 		JMenuItem mntmAbout = new JMenuItem("About");
 		mnHelp.add(mntmAbout);
 		frame.getContentPane().setLayout(new BorderLayout(0, 0));
 
 		JPanel statusPanel = new JPanel();
 		statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
 		frame.getContentPane().add(statusPanel, BorderLayout.SOUTH);
 		statusPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
 
 		JProgressBar progressBar = new JProgressBar();
 		statusPanel.add(progressBar);
 
 		JPanel panel_container = new JPanel();
 		frame.getContentPane().add(panel_container, BorderLayout.CENTER);
 		GridBagLayout gbl_panel_container = new GridBagLayout();
 		gbl_panel_container.columnWidths = new int[] { 0, 0, 0 };
 		gbl_panel_container.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
 		gbl_panel_container.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
 		gbl_panel_container.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0,
 				Double.MIN_VALUE };
 		panel_container.setLayout(gbl_panel_container);
 
 		JLabel lblNewLabel = new JLabel(
 				"Sentence:   (click item to remove)");
 		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
 		gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
 		gbc_lblNewLabel.gridwidth = 2;
 		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
 		gbc_lblNewLabel.gridx = 0;
 		gbc_lblNewLabel.gridy = 0;
 		panel_container.add(lblNewLabel, gbc_lblNewLabel);
 
 		panel_sentence = new JPanel();
 		FlowLayout flowLayout_1 = (FlowLayout) panel_sentence.getLayout();
 		flowLayout_1.setAlignment(FlowLayout.LEADING);
 		panel_sentence.setBackground(Color.WHITE);
 		panel_sentence.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		GridBagConstraints gbc_panel_sentence = new GridBagConstraints();
 		gbc_panel_sentence.insets = new Insets(0, 0, 5, 5);
 		gbc_panel_sentence.fill = GridBagConstraints.BOTH;
 		gbc_panel_sentence.gridx = 0;
 		gbc_panel_sentence.gridy = 1;
 		panel_container.add(panel_sentence, gbc_panel_sentence);
 
 		JButton btnNewButton = new JButton("Speak");
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Component[] components = panel_sentence.getComponents();
 				String sentence = "";
 				for (Component component : components){
 					sentence = sentence + ((JButton)component).getText() + " ";
 				}
 				speakSentence(sentence);
 			}
 		});
 		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
 		gbc_btnNewButton.fill = GridBagConstraints.BOTH;
 		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
 		gbc_btnNewButton.gridx = 1;
 		gbc_btnNewButton.gridy = 1;
 		panel_container.add(btnNewButton, gbc_btnNewButton);
 		
 		JLabel lblLib = new JLabel("Choose symbol / category:");
 		GridBagConstraints gbc_lblLib = new GridBagConstraints();
 		gbc_lblLib.fill = GridBagConstraints.HORIZONTAL;
 		gbc_lblLib.insets = new Insets(0, 0, 5, 5);
 		gbc_lblLib.gridx = 0;
 		gbc_lblLib.gridy = 2;
 		panel_container.add(lblLib, gbc_lblLib);
 		
 				panel_category = new JPanel();
 				FlowLayout flowLayout = (FlowLayout) panel_category.getLayout();
 				flowLayout.setAlignment(FlowLayout.LEADING);
 				panel_category.setBackground(Color.WHITE);
 				panel_category.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 				GridBagConstraints gbc_panel_library = new GridBagConstraints();
 				gbc_panel_library.insets = new Insets(0, 0, 5, 0);
 				gbc_panel_library.gridwidth = 2;
 				gbc_panel_library.fill = GridBagConstraints.BOTH;
 				gbc_panel_library.gridx = 0;
 				gbc_panel_library.gridy = 3;
 				panel_container.add(panel_category, gbc_panel_library);
 
 		JLabel lblNewLabel_1 = new JLabel(
 				"Choose previous category:");
 		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
 		gbc_lblNewLabel_1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_lblNewLabel_1.gridwidth = 2;
 		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
 		gbc_lblNewLabel_1.gridx = 0;
 		gbc_lblNewLabel_1.gridy = 4;
 		panel_container.add(lblNewLabel_1, gbc_lblNewLabel_1);
 		
 		panel_path = new JPanel();
 		panel_path.setBackground(Color.WHITE);
 		panel_path.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		FlowLayout flowLayout_2 = (FlowLayout) panel_path.getLayout();
 		flowLayout_2.setAlignment(FlowLayout.LEADING);
 		GridBagConstraints gbc_panel_path = new GridBagConstraints();
 		gbc_panel_path.gridwidth = 2;
 		gbc_panel_path.insets = new Insets(0, 0, 5, 0);
 		gbc_panel_path.fill = GridBagConstraints.BOTH;
 		gbc_panel_path.gridx = 0;
 		gbc_panel_path.gridy = 5;
 		panel_container.add(panel_path, gbc_panel_path);
 	}
 
 	protected void speakSentence(String sentence) {
 		// TODO Auto-generated method stub
 		Voice voice = VoiceManager.getInstance().getVoice("kevin16");
 		if (voice == null) {
 			System.err.println("Voice error. Couldn't load voice kevin16.");
 			System.exit(1);
 		}
 		voice.allocate();
 		voice.speak(sentence);
 		voice.deallocate();
 	}
 
 	public void loadCategory(File folder) {
 		// todo: background loading, and statusbar update
 		Category category = Category.load(folder);
 		addCategoryToPathPanel(category);
 		showCategory(category);
 	}
 
 	private void addCategoryToPathPanel(Category category) {
 		ImageIcon icon;
 		String name = category.getName();
 		String iconPath = category.getIconPath();
 		if (iconPath == null) {
 			URL resource = getClass().getResource(
 					"/commsbook/resources/folder.png");
 			icon = new ImageIcon(resource, name);
 		} else {
 			icon = new ImageIcon(iconPath, name);
 		}
 		JButton pathItemButton = new JButton(icon);
 		pathItemButton.setText(name);
 		pathItemButton.setBackground(Color.WHITE); // TODO: doesn't seem to
 												// work in ubuntu. hrmm.
 		pathItemButton.setVerticalTextPosition(JButton.BOTTOM);
 		pathItemButton.setHorizontalTextPosition(JButton.CENTER);
 		pathItemButton.setVerticalAlignment(JButton.BOTTOM);
 		pathItemButton.setHorizontalAlignment(JButton.CENTER);
 		pathItemButton.addActionListener(new PathPanelItemListener(category.getPath(), panel_path, pathItemButton, this));
 		panel_path.add(pathItemButton);
 		panel_path.revalidate();
 		panel_path.repaint();
 	}
 
 	private void showCategory(Category category) {
 		ImageIcon icon;
 		String name;
 		String iconPath;
 		panel_category.removeAll(); // clear the existing category
 		// display (or initial prompt)
 		// load the images into the visual library
 		for (CategoryItem item : category.getSymbols()) {
 			name = item.getName();
 			iconPath = item.getIconPath();
 			if (iconPath == null) {
 				URL resource = getClass().getResource(
 						"/commsbook/resources/folder.png");
 				icon = new ImageIcon(resource, name);
 			} else {
 				icon = new ImageIcon(iconPath, name);
 			}
 			JButton libraryItem = new JButton(icon);
 			libraryItem.setText(name);
 			libraryItem.setBackground(Color.WHITE); // TODO: doesn't seem to
 													// work in ubuntu. hrmm.
 			if (item.getIsCategory()){
 				// highlight categories visually as being different
 				libraryItem.setBorder(new BevelBorder(BevelBorder.RAISED));
 				libraryItem.setForeground(Color.BLUE);
 			}
 			libraryItem.setVerticalTextPosition(JButton.BOTTOM);
 			libraryItem.setHorizontalTextPosition(JButton.CENTER);
 			libraryItem.setVerticalAlignment(JButton.BOTTOM);
 			libraryItem.setHorizontalAlignment(JButton.CENTER);
 			libraryItem.addActionListener(new CategoryItemListener(item, this));
 			panel_category.add(libraryItem);
 		}
 		panel_category.revalidate();
 		panel_category.repaint();
 	}
 	
 	public void addToSentence(Symbol symbol){
 		ImageIcon icon = new ImageIcon(symbol.getIconPath(), symbol.getName());
 		JButton libraryItem = new JButton(icon);
 		libraryItem.setText(symbol.getName());
 		libraryItem.setBackground(Color.WHITE); // TODO: doesn't seem to
 												// work in ubuntu. hrmm.
 		libraryItem.setVerticalTextPosition(JButton.BOTTOM);
 		libraryItem.setHorizontalTextPosition(JButton.CENTER);
 		libraryItem.setVerticalAlignment(JButton.BOTTOM);
 		libraryItem.setHorizontalAlignment(JButton.CENTER);
 		libraryItem.addActionListener(new SentenceItemListener(panel_sentence, libraryItem));
 		panel_sentence.add(libraryItem);
 		panel_sentence.revalidate();
 		panel_sentence.repaint();
 	}
 
 	public void loadLibrary() {
 		if (libraryFolderChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
 			return;
 		}
 		File libraryFolder = libraryFolderChooser.getSelectedFile();
 		panel_path.removeAll();
 		panel_path.revalidate();
 		panel_path.repaint();
 		loadCategory(libraryFolder);
 	}
 }
 
 class CategoryItemListener implements ActionListener {
 	private final CategoryItem item;
 	private final AppWindow view;
 
 	CategoryItemListener(CategoryItem item, AppWindow view) {
 		this.item = item;
 		this.view = view;
 	}
 
 	public void actionPerformed(ActionEvent ae) {
 		if (item.getIsCategory()) {
 			// load another category
 			view.loadCategory(new File(((Category) item).getPath()));
 		} else {
 			view.addToSentence((Symbol)item);
 		}
 	}
 }
 
 class SentenceItemListener implements ActionListener {
 	private final JButton button;
 	private JPanel container;
 
 	SentenceItemListener(JPanel container, JButton button) {
 		this.container = container;
 		this.button = button;
 	}
 
 	public void actionPerformed(ActionEvent ae) {
 		// remove from sentence
 		container.remove(button);
 		container.revalidate();
 		container.repaint();
 	}
 }
 
 class PathPanelItemListener implements ActionListener {
 	private final String path;
 	private final AppWindow view;
 	private JPanel container;
 	private JButton button;
 
 	PathPanelItemListener(String path, JPanel container, JButton button, AppWindow view) {
 		this.path = path;
 		this.container = container;
 		this.button = button;
 		this.view = view;
 	}
 
 	public void actionPerformed(ActionEvent ae) {
 		//remove all subsequent buttons
 		boolean past = false;
 		for (Component component : container.getComponents()){
 			if (past) {
 				container.remove(component);
 			} else if (component==button){
 				container.remove(component); // clicked button re-added by load routine. 
 				past = true;
 			}
 		}
 		container.revalidate();
 		container.repaint();
 		//load the selected category
 		view.loadCategory(new File(path));
 	}
 }
 
