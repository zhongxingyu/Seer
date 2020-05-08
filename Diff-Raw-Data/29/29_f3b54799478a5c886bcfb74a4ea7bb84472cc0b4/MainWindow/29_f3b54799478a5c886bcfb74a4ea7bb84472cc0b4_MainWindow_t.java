 /**
  * 
  */
 package org.carnegiesciencecenter.buhl;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.RowSorter;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 /**
  * @author Anh Le
  *
  */
 public class MainWindow extends JFrame implements ActionListener {
 	JMenuBar menuBar;
 	JMenu fileMenu, scriptMenu, helpMenu;
 	JMenuItem openButtonSetMI, openSpiceMI, showInUseOnlyMI, copyUsedButtonsMI,
 		cutSeqMI, exploreMI, convertMI, saveMI, splitMI, 
 		helpMI, aboutMI; 
 	JTable scriptTable;
 	JPanel statusPanel;
 	JLabel buttonSetLabel, spiceLabel;
 	JTextArea spiceTextArea, dsTextArea;
 	static ScriptExplorer se;
 	
 	MainWindow(ScriptExplorer se_) {
 		super("Script Explorer - ver 0.1");
 		setBounds(100,100,800,400);
 	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		se = new ScriptExplorer();
 
 		menuBar = new JMenuBar();
 		
 		fileMenu = new JMenu("File");
 		openButtonSetMI = new JMenuItem("Open button set");
 		fileMenu.add(openButtonSetMI);
 		openSpiceMI = new JMenuItem("Open SPICE script");
 		fileMenu.add(openSpiceMI);
 		showInUseOnlyMI = new JMenuItem("Remove unused scripts");
 		fileMenu.add(showInUseOnlyMI);
 		copyUsedButtonsMI = new JMenuItem("Copy used scripts to a new set");
 		fileMenu.add(copyUsedButtonsMI);
 		menuBar.add(fileMenu);
 		
 		scriptMenu = new JMenu("Script");
 		convertMI = new JMenuItem("Convert SPICE to DS");
 		scriptMenu.add(convertMI);
 		saveMI = new JMenuItem("Save to button");
 		scriptMenu.add(saveMI);
 		cutSeqMI = new JMenuItem("Cut from time point");
 		scriptMenu.add(cutSeqMI);
 		splitMI = new JMenuItem("Split converted DS script");
 		scriptMenu.add(splitMI);
 		exploreMI = new JMenuItem("Explore");
 		scriptMenu.add(exploreMI);
 		menuBar.add(scriptMenu);
 		
 		helpMenu = new JMenu("Help");
 		helpMI = new JMenuItem("User's manual");
 		helpMenu.add(helpMI);
 		aboutMI = new JMenuItem("About");
 		helpMenu.add(aboutMI);
 		menuBar.add(helpMenu);
 		
 		this.setJMenuBar(menuBar);
 		
 		// create table and add to frame
 		scriptTable = new JTable(se.tableModel);
 		RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(se.tableModel);
 		sorter.toggleSortOrder(0);
 		scriptTable.setRowSorter(sorter);	
 		scriptTable.setFillsViewportHeight(true);
 		
 		JScrollPane tablePane = new JScrollPane(scriptTable);
 		//scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
 		//scriptTable.setPreferredSize(new Dimension(1000,50));
 		//scriptTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		scriptTable.getColumnModel().getColumn(0).setPreferredWidth(1);
 		scriptTable.getColumnModel().getColumn(1).setPreferredWidth(1);
 		scriptTable.getColumnModel().getColumn(2).setPreferredWidth(1);
 		scriptTable.getColumnModel().getColumn(3).setPreferredWidth(10);
 		scriptTable.getColumnModel().getColumn(4).setPreferredWidth(5);
 		scriptTable.getColumnModel().getColumn(5).setPreferredWidth(5);
 		scriptTable.getColumnModel().getColumn(6).setPreferredWidth(5);
 		scriptTable.getColumnModel().getColumn(7).setPreferredWidth(200);
 
 		
 		// create spice text area and add to panel
 		spiceTextArea = new JTextArea();
 		spiceTextArea.setAlignmentX(JTextArea.LEFT_ALIGNMENT);
 		spiceTextArea.setLineWrap(false);
 		spiceTextArea.setFont(new Font("Courier New", Font.PLAIN, 16));
 		JScrollPane spiceTextPane = new JScrollPane(spiceTextArea);
 
 		// create ds text area and add to panel
 		dsTextArea = new JTextArea();
 		dsTextArea.setAlignmentX(JTextArea.LEFT_ALIGNMENT);
 		dsTextArea.setLineWrap(false);
 		dsTextArea.setFont(new Font("Courier New", Font.PLAIN, 16));
 		JScrollPane dsTextPane = new JScrollPane(dsTextArea);
 
 		JPanel textPanel = new JPanel();
 		JSplitPane splitTextPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spiceTextPane, dsTextPane);
 		splitTextPane.setDividerLocation(700);
 		
 		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePane, splitTextPane);
 		splitPane.setOneTouchExpandable(true);
 		splitPane.setDividerLocation(200);
 		this.getContentPane().add(splitPane);
 		
 //		scriptTable.addMouseListener(
 //				new MouseAdapter() {
 //					public void mouseClicked(MouseEvent e) {
 //						if (e.getClickCount() == 2) {
 //							System.out.println("click" + e.getClickCount());
 //						}
 //					}
 //				} 
 //			);
 	   
 		// create status bar at the bottom
 		statusPanel = new JPanel();
 		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
 
 		this.getContentPane().add(statusPanel, BorderLayout.SOUTH);
 
 		statusPanel.setPreferredSize(new Dimension(this.getContentPane().getWidth(), 20));
 		statusPanel.setLayout(new GridLayout(1,2));
 		
 		// add Button Set Label to status bar
 		buttonSetLabel = new JLabel(" No button set opened.");
 		buttonSetLabel.setHorizontalAlignment(SwingConstants.LEFT);
 		buttonSetLabel.setBorder(new LineBorder(Color.blue, 1));
 		statusPanel.add(buttonSetLabel);
 
 		// add SPICE File Label to status bar
 		spiceLabel = new JLabel(" No SPICE script opened.");
 		spiceLabel.setHorizontalAlignment(SwingConstants.LEFT);
 		spiceLabel.setBorder(new LineBorder(Color.blue, 1));
 		statusPanel.add(spiceLabel);
 
 		openButtonSetMI.addActionListener(this);
 		openSpiceMI.addActionListener(this);
 		convertMI.addActionListener(this);
 		saveMI.addActionListener(this);
 		exploreMI.addActionListener(this);
 		showInUseOnlyMI.addActionListener(this);
 		copyUsedButtonsMI.addActionListener(this);
 		splitMI.addActionListener(this);
 		cutSeqMI.addActionListener(this);
 		
 		this.setExtendedState(this.getExtendedState()|JFrame.MAXIMIZED_BOTH);
 
 		setVisible(true);
 	}
 	
 	private File openButtonSet() {
 		JFileChooser fc = new JFileChooser();
 		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
 		fc.setFileFilter(new DSPathFilter());
 		int returnVal = fc.showOpenDialog(this);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			return fc.getSelectedFile();
 		}
 		return null;
 	}
 	
 	private File openSpiceFile() {
 		JFileChooser fc = new JFileChooser();
 		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
 		fc.setFileFilter(new SpiceFileFilter());
 		int returnVal = fc.showOpenDialog(this);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			return fc.getSelectedFile();
 		}
 		return null;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
         if (e.getSource() == openButtonSetMI) {
         	//se.buttonSetPath = openButtonSet();
         	//se.buttonSetPath = new File("/home/lion/Downloads/CSC/DAZZLE");
        	se.buttonSetPath = new File("/home/lion/Downloads/CSC/LEWIS");
        	//se.buttonSetPath = new File("/home/lion/Downloads/CSC/XMAS");
         	if (se.buttonSetPath != null) {
         		buttonSetLabel.setText(se.buttonSetPath.getName());
         		se.LoadDSFiles();
         		if (se.spiceFile != null)
         			se.updateUsage();
         	}
         } 
         else if (e.getSource() == openSpiceMI) {
         	//se.spiceFile = openSpiceFile();
         	//se.spiceFile = new File("/home/lion/Downloads/CSC/SHOW/2-DAZZLE.SHOW");
        	se.spiceFile = new File("/home/lion/Downloads/CSC/SHOW/3-LEWIS.SHOW");
        	//se.spiceFile = new File("/home/lion/Downloads/CSC/SHOW/4-XMAS.SHOW");
         	if (se.spiceFile != null) {
         		spiceLabel.setText(se.spiceFile.getName());
         		se.LoadSPICEfile();
         		//textArea.setText(se.spiceScript.text);
         		spiceTextArea.setText(se.spiceScript.reformat());
         		spiceTextArea.setCaretPosition(0);
         	}
         } 
         else if (e.getSource() == showInUseOnlyMI) {
         	se.removeUnused();
         }
         else if (e.getSource() == copyUsedButtonsMI) {
         	se.newSetPath = openButtonSet();
         	if (se.newSetPath != null) {
         		if (JOptionPane.showConfirmDialog(
         			    this, "Start a new page?", "Page location", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
         			se.copyToNewSet(true);
         		else
         			se.copyToNewSet(false);
         	}
         } 
         else if (e.getSource() == convertMI) {
 			String path = se.spiceFile.getAbsolutePath();
 			se.outputPath = path.substring(0,path.lastIndexOf(File.separator));
         	se.convert();
     		dsTextArea.setText(se.output);
     		dsTextArea.setCaretPosition(0);
         }
         else if (e.getSource() == saveMI) {
         	String response = JOptionPane.showInputDialog(null,
       			  "Please give destination button's number\n (CIRCUS:97,DAZZLE:121,LEWIS:145,XMAS:157)",
       			  "Button number",
       			  JOptionPane.QUESTION_MESSAGE);
 	      	if (response != null) {
 	        	se.buttonStartNum = Integer.parseInt(response);
 	    		se.saveToFile();
 	      	}
         }
         else if (e.getSource() == splitMI) {
         	String response = JOptionPane.showInputDialog(null,
         			  "Please give the first button's number\n (CIRCUS:109,DAZZLE:133,LEWIS:157)",
         			  "First button number",
         			  JOptionPane.QUESTION_MESSAGE);
         	if (response != null) {
 	        	se.buttonStartNum = Integer.parseInt(response);
 	        	se.splitFile();
         	}
         }
         else if (e.getSource() == cutSeqMI) {
         	String response = JOptionPane.showInputDialog(null,
       			  "Cut from time (mm:ss.ff)",
       			  "Cut time point",
       			  JOptionPane.QUESTION_MESSAGE);
         	if (response != null) {
         		se.output = se.generateOutput(se.cutSequence(Clock.timeValue("01:"+response), 99999999, se.allCmds));
         		dsTextArea.setText(se.output);
         	}
         }
         else if (e.getSource() == exploreMI) {
             	new TimelineWindow(se);
         }
     }
 
 	/**
 	 * @param Nothing
 	 */
 	public static void main(String[] args) {
 		se = new ScriptExplorer();
 		MainWindow mainWin = new MainWindow(se);
 		
 	}
 
 }
 
 class DSPathFilter extends FileFilter {
 
 	@Override
 	public boolean accept(File f) {
 		return (f.isDirectory());
 	}
 
 	@Override
 	public String getDescription() {
 		return "Button Set Directory";
 	}
 }
 
 class SpiceFileFilter extends FileFilter {
 
 	@Override
 	public boolean accept(File f) {
 		if (f.isDirectory())
 			return true;
 		return f.getName().toLowerCase().contains(".show");
 	}
 
 	@Override
 	public String getDescription() {
 		return "SPICE Theatre Automation Files";
 	}
 
 }
