 /**
  * 
  */
 package no.whg.mini;
 
 import java.awt.BorderLayout;
 
 
 import java.awt.Dimension;
 
 import javax.swing.*;
 
 /**
  * The Main class, connects all the "dots" together to become a full program, that
  * 	lets the users create a GUI and save that to file
  * @author Fredrik, Peer
  *
  */
 public class Main extends JFrame{
 	private CustomTableModel tableModel = new CustomTableModel();
 	ImageIcon[] images;
 	Integer[] imageIndex;
 	private JTable table = new JTable(tableModel);
 	private String[] imagePath = {"anchor_center_shifted.png", "anchor_center.png", "anchor_east_shifted.png", "anchor_east.png",
 			"anchor_north_shifted.png", "anchor_north.png", "anchor_northeast_shifted.png", "anchor_northeast.png", "anchor_northwest.png",
 			"anchor_south_shifted.png", "anchor_south.png", "anchor_southeast_shifted.png", "anchor_southeast.png", "anchor_southwest.png",
 			"anchor_west.png"};
 	private String[] objects = {"JLabel", "JButton", "JTextField", "JTextArea", 
 			"JCheckBox", "JList", "JComboBox", "JSpinnerList", "JSpinnerNumber"
 	};
 	
 	private JComboBox objectType = new JComboBox(objects);
 	private JComboBox anchorList;
 	private ComboBoxRenderer renderer;
 	
 	public Main() {	
 		
 		// override JFrame constructor
 		super(Messages.getString("Main.title")); //$NON-NLS-1$
 		// tell application to close on exit, instead of hide on exit
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		// set the default size
 		setSize(400, 400);
 		// set the initial position of window to center
 		setLocationRelativeTo(null);
 		// show the frame
 		setVisible(true);
 		
 		// set the look and feel of the program to resemble users OS, if possible
 		try {
 			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
 		} catch (Exception e) {
 			System.out.println(Messages.getString("Main.catchError") + e); //$NON-NLS-1$
 		}
 		
 		createImages();
 		
 		// add the menu bar at the top
 		setJMenuBar(new Menubar(tableModel));
 		
 		// add the toolbar below the menu bar
 		add(new Toolbar(tableModel), BorderLayout.NORTH);
		add(anchorList, BorderLayout.SOUTH);
 		
 		
 		tableModel.setTableFrame(this);
 		add(new JScrollPane(table), BorderLayout.CENTER);
 		table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(objectType));
 		
 		
 		// pack it all together
 		pack();
 	}
 	
 	public void createImages()
 	{
 		 images = new ImageIcon[imagePath.length];
 		 imageIndex = new Integer[imagePath.length];
 		 for(int i = 0; i < imagePath.length; i++)
 		 {
 			 imageIndex[i] = new Integer(i);
 			 images[i] = new ImageIcon("gbleditor_icons/"+imagePath[i]);
 		 }
 		 
 		 anchorList = new JComboBox(imageIndex);
 		 renderer = new ComboBoxRenderer();
 		 renderer.setImages(images);
 		 renderer.setPreferredSize(new Dimension(20,100));
 		 anchorList.setRenderer(renderer);
 		 anchorList.setMaximumRowCount(3);
 	};
 
 	/**
 	 * @param args The variables that are sent with the "java" command
 	 */
 	public static void main(String[] args) {
 		// set the locale
 		//Messages.setLocale(args[0], args[1]);
 		new Main();
 		
 	}
 
 }
