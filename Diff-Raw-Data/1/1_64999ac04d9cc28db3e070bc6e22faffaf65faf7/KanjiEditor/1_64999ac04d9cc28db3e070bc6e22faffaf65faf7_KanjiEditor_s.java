 package kanjieditor;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import vocab.Kanji;
 
 
 /**
  * The kanji editor.
  */
 public class KanjiEditor {
 	
 	/** The kanji list. */
 	public ArrayList<Kanji> klist;
 	
 	/** The graphical list. */
 	public GraphicalList glist;
 	
 	/**
 	 * Instantiates a new kanji editor.
 	 */
 	public KanjiEditor(){
 		klist = new ArrayList<Kanji>();
 		JFrame frame = new JFrame("Kanji editor");
 		frame.setResizable(false);
 		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		//adds the menu bar
 		JMenuBar menu = new JMenuBar();
 		frame.setJMenuBar(menu);
 		JMenu fileMenu = new JMenu("File");
 		fileMenu.add(new NewMenuItem(this));
 		fileMenu.add(new OpenMenuItem(this));
 		fileMenu.add(new SaveMenuItem(klist));
 		menu.add(fileMenu);
 		
 		//adds the editable properties
 		JPanel proppanel = new JPanel();
 		JPanel sidepanel = new JPanel();
 		frame.add(sidepanel, BorderLayout.EAST);
 		JTextField reading = new JTextField(15);
 		JTextField translation = new JTextField();
 
 		//adds the scrolling vocabulary list' menu item.
 		JPanel listpanel = new JPanel();
 		frame.add(listpanel, BorderLayout.WEST);
 		glist = new GraphicalList(klist, reading, translation);
 		JScrollPane scrollPane = new JScrollPane(glist);
 		scrollPane.setPreferredSize(new Dimension(300,400));
 		listpanel.add(scrollPane);
 		
 		sidepanel.setLayout(new BoxLayout(sidepanel, BoxLayout.PAGE_AXIS));
 		proppanel.setLayout(new BoxLayout(proppanel, BoxLayout.PAGE_AXIS));
 		sidepanel.add(proppanel);
 		proppanel.add(new JLabel("Reading"));
 		proppanel.add(reading);
 		proppanel.add(new JLabel("Translation"));
 		proppanel.add(translation);
 		Dimension fbox = new Dimension(5,300);
 		proppanel.add(new Box.Filler(fbox, fbox, fbox));
 		JPanel butpanel = new JPanel();
 		JButton neww = new NewButton(glist);
 		butpanel.add(neww);
 		JButton delete = new DeleteButton(glist);
 		butpanel.add(delete);
		proppanel.add(butpanel);
 		sidepanel.add(butpanel);
 		
 		frame.pack();
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 
 	/**
 	 * Loads a new kanji list into the editor.
 	 *
 	 * @param newlist the new list
 	 */
 	public void load(ArrayList<Kanji> newlist) {
 		klist.clear();
 		klist.addAll(newlist);
 		glist.updateList();
 		glist.clearFields();
 	}
 	
 	
 }
