 package ClassAdminFrontEnd;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.event.*;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.text.BadLocationException;
 
 import org.jdesktop.swingx.JXPanel;
 import org.jdesktop.swingx.painter.CompoundPainter;
 import org.jdesktop.swingx.painter.GlossPainter;
 import org.jdesktop.swingx.painter.MattePainter;
 
 import ClassAdminBackEnd.FileHandler;
 import ClassAdminBackEnd.Global;
 import ClassAdminBackEnd.UnsupportedFileTypeException;
 
 public class Frame extends JFrame {
 
 	private JPanel contentPane;
 	private JXPanel navBar;
 	private ReflectionButton btnImport;
 	private ReflectionButton btnExport;
 	private FileHandler fileHandler;
 	private JFileChooser filechooser;
 	private JFrame frame = this;
 	private FrmTable table;
 	private JTabbedPane tabbedPane;
 	private File currentFilePath;
 	private int tabCount = -1;
 	private int navBarHeight;
 	private int navBarSpace;
 	
 	private String currentOs;
 	private static String MAC_OS = "MAC";
 	private static String WIN_OS = "WINDOWS";
 
 	public class TabButton extends JPanel {
 
 		private String text;
 		private JLabel label;
 		private JLabel button;
 		private TabButton tabbutton = this;
 
 		public TabButton(String _text) {
 			
 			//create label with file name for tab
 			text = _text;
 			label = new JLabel(text);
 			add(label);
 			
 			//create close button
 			button = new JLabel("x");
 			button.setBorder(new EmptyBorder(1,1,1,1));
 			add(button);
 			button.setForeground(Color.white);
 			
 			//set this panel with label and close button to transparent
 			this.setOpaque(false);
 			this.setBorder(null);
 			
 			//close tab action
 			button.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mousePressed(MouseEvent e) {
 					tabbedPane.remove(tabbedPane.indexOfTabComponent(tabbutton));
 					tabCount--;
 				}
 				
 				@Override
 				public void mouseEntered(MouseEvent e) {
 					button.setForeground(Color.darkGray);
 				}
 				
 				@Override
 				public void mouseExited(MouseEvent e) {
 					button.setForeground(Color.white);
 				}
 				
 			});
 		}
 
 		
 		
 		
 	}
 
 	public static void main(String[] args) {
 
 		try {
 			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
 					.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					javax.swing.UIManager.setLookAndFeel(info.getClassName());
 					UIManager.put("nimbusBase", new Color(0x7A7A7A));
 					UIManager.put("nimbusSelectionBackground", new Color(
 							0x171717));
 					UIManager.put("Menu.background", new Color(0x2B2B2B));
 					UIManager.put("background", new Color(0x171717));
 					UIManager
 							.put("DesktopIcon.background", new Color(0x171717));
 					UIManager.put("nimbusLightBackground", new Color(0xE3E3E3));
 
 					break;
 				}
 			}
 		} catch (ClassNotFoundException ex) {
 			java.util.logging.Logger.getLogger(Frame.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		} catch (InstantiationException ex) {
 			java.util.logging.Logger.getLogger(Frame.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		} catch (IllegalAccessException ex) {
 			java.util.logging.Logger.getLogger(Frame.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
 			java.util.logging.Logger.getLogger(Frame.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Frame frame = new Frame();
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
 	public Frame() {
 
 		//determine os
 		determineOS();
		if (currentOs == MAC_OS)
 		{
 			setupMac();
			
 		}
		else if ((currentOs == WIN_OS) || (currentOs ==  null))
 		{
 			setupWindows();
 		}
 		
 		// create file handler
 		fileHandler = FileHandler.get();
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 1131, 650);
 
 		// create top menu bar
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 
 		// create menu
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 
 		// create menu items
 		JMenuItem mntmNew = new JMenuItem("New");
 		mnFile.add(mntmNew);
 
 		// create gradient background panel
 		contentPane = new GradientPanel(new Color(0x878787),
 				new Color(0x242424), this.getWidth(), this.getHeight());
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 
 		// create bottom navigation bar
 		navBar = new JXPanel();
 		navBar.setBounds(0, contentPane.getHeight() - navBarHeight - navBarSpace, getWidth(), navBarHeight);
 	
 		setupPainters();
 		contentPane.setLayout(null);
 		contentPane.add(navBar);
 		navBar.setLayout(null);
 
 		// add navigation bar labels
 		ShadowLabel lblImport = new ShadowLabel("Import", 12);
		lblImport.setBounds(33, 64, 42, 17);
 		navBar.add(lblImport);
 
 		ShadowLabel lblExport = new ShadowLabel("Export", 12);
		lblExport.setBounds(121, 64, 42, 17);
 		navBar.add(lblExport);
 
 		ShadowLabel lblChart = new ShadowLabel("Charts", 12);
		lblChart.setBounds(205, 64, 42, 17);
 		navBar.add(lblChart);
 
 		ShadowLabel lblStudents = new ShadowLabel("Students", 12);
		lblStudents.setBounds(297, 64, 54, 17);
 		navBar.add(lblStudents);
 
 		ShadowLabel lblSave = new ShadowLabel("Save", 12);
		lblSave.setBounds(392, 64, 42, 17);
 		navBar.add(lblSave);
 
 		// create tabbedPane
 		tabbedPane = new JTabbedPane();
 		tabbedPane.setBounds(54, 50, 948, 400);
 		contentPane.add(tabbedPane);
 
 		// add navigation bar buttons
 		try {
 
 			btnImport = new ReflectionButton(ImageIO.read(getClass()
 					.getResource("Import.png")));
 			btnImport.setBounds(20, 11, 67, 73);
 			navBar.add(btnImport);
 
 			btnExport = new ReflectionButton(ImageIO.read(getClass()
 					.getResource("Export.png")));
 			btnExport.setBounds(105, 11, 67, 73);
 			navBar.add(btnExport);
 
 			JButton btnStudents;
 			btnStudents = new ReflectionButton(ImageIO.read(getClass()
 					.getResource("Students.png")));
 			btnStudents.setBounds(289, 11, 67, 73);
 			navBar.add(btnStudents);
 
 			JButton btnChart;
 			btnChart = new ReflectionButton(ImageIO.read(getClass()
 					.getResource("Chart.png")));
 			btnChart.setBounds(194, 11, 67, 73);
 			navBar.add(btnChart);
 
 			JButton btnSave;
 			btnSave = new ReflectionButton(ImageIO.read(getClass().getResource(
 					"Save.png")));
 			btnSave.setBounds(382, 13, 67, 73);
 			navBar.add(btnSave);
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// import button mouselistener
 		btnImport.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				try {
 					openFile();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (BadLocationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 
 		// export button mouselistener
 		btnExport.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				try {
 					saveFileAs();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 
 		// frame resize listener to put nav bar at bottom of frame on resize
 		this.addComponentListener(new ComponentListener() {
 
 			@Override
 			public void componentHidden(ComponentEvent arg0) {
 
 			}
 
 			@Override
 			public void componentMoved(ComponentEvent arg0) {
 
 			}
 
 			@Override
 			public void componentResized(ComponentEvent arg0) {
 				navBar.setBounds(0,
 						frame.getHeight() - navBar.getHeight()-navBarSpace,
 						frame.getWidth(), navBar.getHeight());
 
 			}
 
 			@Override
 			public void componentShown(ComponentEvent arg0) {
 
 			}
 		});
 
 	}
 
 	private void setupWindows() {
 		navBarHeight = 84;
 		navBarSpace = 60;
 	}
 
 	private void setupMac() {
 		navBarHeight = 84;
 		navBarSpace = 45;		
 	}
 
 	// bottom nav bar background painter
 	private void setupPainters() {
 		GlossPainter gloss = new GlossPainter();
 
 		MattePainter matte = new MattePainter(new Color(0x242424));
 		navBar.setBackgroundPainter(new CompoundPainter(matte, gloss));
 	}
 
 	public void openFile() throws IOException, BadLocationException {
 
 		File file;
 		// set the file extentions that may be chosen
 		FileFilter fileFilter = new FileNameExtensionFilter(
 				"Supported files types: pdat, csv", "pdat", "csv");
 
 		// Create a file chooser
 		filechooser = new JFileChooser();
 		if (currentFilePath != null) {
 			filechooser.setCurrentDirectory(currentFilePath);
 		}
 
 		// add the filter to the file chooser
 		filechooser.addChoosableFileFilter(fileFilter);
 
 		// shows the dialog, return value specifies file
 		int returnVal = filechooser.showOpenDialog(this);
 
 		// if the chosen file is valid
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			file = filechooser.getSelectedFile();
 			currentFilePath = filechooser.getSelectedFile();
 			try {
 
 				fileHandler.openFile(file.getAbsolutePath());
 				// create table on panel
 				table = new FrmTable(Global.getGlobal().getActiveProject()
 						.getHead().getHeaders(), Global.getGlobal()
 						.getActiveProject().getHead().getDataLinkedList());
 				
 				// put panel with table on a new tab
 				tabbedPane.addTab(file.getName(), table);
 				tabCount++;
 				tabbedPane.setTabComponentAt(tabCount,new TabButton(file.getName()));
 
 			} catch (UnsupportedFileTypeException e) {
 				JOptionPane.showMessageDialog(this, "File Error",
 						"Error retrieving file!", JOptionPane.ERROR_MESSAGE);
 				e.printStackTrace();
 			}
 		} else {
 
 		}
 	}
 
 	public void saveFileAs() throws IOException {
 
 		File file;
 		// set the file extentions that may be chosen
 		FileFilter filter = new FileNameExtensionFilter(
 				"Supported files types: pdat, csv", "pdat", "csv");
 
 		// Create a file chooser
 		filechooser = new JFileChooser();
 		if (currentFilePath != null) {
 			filechooser.setCurrentDirectory(currentFilePath);
 		}
 		currentFilePath = filechooser.getSelectedFile();
 
 		// add the filter to the file chooser
 		filechooser.addChoosableFileFilter(filter);
 
 		// shows the dialog, return value specifies file
 		int returnVal = filechooser.showSaveDialog(this);
 
 		// if the chosen file is valid
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			file = filechooser.getSelectedFile();
 
 			try {
 				FileHandler.get().saveFile(file.getAbsolutePath());
 			} catch (UnsupportedFileTypeException e) {
 				e.printStackTrace();
 			}
 		} else {
 
 		}
 	}
 	
 	public void determineOS() {
		currentOs = System.getProperty("os.name").toUpperCase();
 	    if(currentOs.contains("MAC")){
 	        currentOs = MAC_OS;
 	    }
 	    else if( currentOs.contains("WINDOWS") ){
 	        currentOs = WIN_OS;
 	    }
 	    else{
 	        currentOs = null;
 	    }
 	}
 }
