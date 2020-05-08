 package gui;
 /* This program is licensed under the terms of the GPL V3 or newer*/
 /* Written by Johannes Putzke*/
 /* eMail: die_eule@gmx.net*/ 
 
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.net.URL;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.table.DefaultTableModel;
 
 
 
 import control.Control_PlayList;
 import control.Control_Stream;
 import control.SRSOutput;
 
 public class Import_Streams extends JDialog {
 	private static final long serialVersionUID = 1L;
 	private ResourceBundle trans = ResourceBundle.getBundle("translations.StreamRipStar");
 	
 	private JPanel mainPanel = new JPanel();
 	
 	private Object[] importHeader = {"import","Stream Name", "Address","Website"};
 	private String[][] allData = {};
 
 	private DefaultTableModel importModel = new DefaultTableModel(allData,importHeader) {
 		private static final long serialVersionUID = 1L;
 		public Class<?> getColumnClass(int columnIndex) {
 			if (columnIndex == 0)
 				return Boolean.class;
 			return String.class;}};
 	
 	private Gui_JTTable importTable = new Gui_JTTable(importModel);
 	private JScrollPane importPane  = new JScrollPane(importTable);
 	
 	private ImageIcon abortIcon = new ImageIcon((URL)getClass().getResource("/Icons/abort_small.png"));
 	private ImageIcon browseIcon = new ImageIcon((URL)getClass().getResource("/Icons/open_small.png"));
 	private ImageIcon importIcon = new ImageIcon((URL)getClass().getResource("/Icons/import_small.png"));
 	private ImageIcon loadIcon = new ImageIcon((URL)getClass().getResource("/Icons/load_small.png"));
 	
 	private JButton browseButton = new JButton(browseIcon);
 	private JButton abortButton = new JButton("Abort",abortIcon);
 	private JButton importButton = new JButton("Import",importIcon);
 	private JButton loadButton = new JButton("Load",loadIcon);
 	private JButton selectAllButton = new JButton("Select All");
 	private JButton unselectAllButton = new JButton("Unselect All");
 	private JLabel destLabel = new JLabel("Source :");
 	
 	private JTextField importPathField = new JTextField("myImport.pls");
 	
 	private JFileChooser dirChooser = new JFileChooser();
 	
 	private String[][] importableStreams = null;
 	private Gui_StreamRipStar mainGui = null;
 	private Control_Stream controlStreams = null;
 	
 	public Import_Streams(Gui_StreamRipStar gui, Control_Stream controlStreams)	{
 		super(gui,"importiere Stream");
 		this.mainGui = gui;
 		this.controlStreams = controlStreams;
 		this.setModalityType(ModalityType.APPLICATION_MODAL);
 		setLanguage();
 		init();
 
 	}
 	
 	//Open importframe and load the file in url
 	public Import_Streams(Gui_StreamRipStar gui, Control_Stream controlStreams ,String url)	{
 		super(gui,"importiere Stream");
 		this.mainGui = gui;
 		this.controlStreams = controlStreams;
 		this.setModalityType(ModalityType.APPLICATION_MODAL);
 		setLanguage();
 		importPathField.setText(url);
 		load();
 		init();
 	}
 	
 	//set up all graphic components 
 	private void init()	{
 		//set it to an appropriate width 
 		importTable.getColumn(importHeader[0]).setMaxWidth(50);
 		//dont allow moving columns
 		importTable.getTableHeader().setReorderingAllowed(false);
 		//set layout; add jpanel to mainwindow
 		mainPanel.setLayout( new GridBagLayout() );
 		add(mainPanel);
 		
 		//set Constrains defaults
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		c.insets = new Insets( 5, 5, 5, 5);
 	
 		//Line, where get PlayList file from
 		c.gridy = 0;
 		c.gridx = 0;
 		mainPanel.add(destLabel,c);
 		c.insets = new Insets( 5, 5, 5, 5);
 		c.gridx = 1;
 		c.weightx = 1.0;
 		c.gridwidth = 3;
 		mainPanel.add(importPathField,c);
 		c.insets = new Insets( 5, 0, 5, 5 );
 		c.gridx = 4;
 		c.weightx = 0.0;
 		c.gridwidth = 1;
 		mainPanel.add(browseButton,c);
 		c.insets = new Insets( 5, 5, 5, 5);
 		c.gridx = 5;
 		mainPanel.add(loadButton,c);
 		
 		
 		//Add the JTable that contains all possible importable streams
 		c.weightx = 1.0;
 		c.weighty= 1.0;
 		c.gridy = 1;
 		c.gridx = 0;
 		c.gridwidth=6;
 		mainPanel.add(importPane,c);
 		
 		// Line with buttons
 		c.gridwidth=1;
 		c.insets = new Insets( 5, 5, 5, 5);
 		c.weighty= 0.0;
 		c.weightx = 0.0;
 		c.gridy = 2;
 		c.gridx = 0;
 		mainPanel.add(importButton,c);
 		c.gridx = 1;
 		c.anchor = GridBagConstraints.CENTER;
 		mainPanel.add(selectAllButton,c);
 		c.gridx = 2;
 		mainPanel.add(unselectAllButton,c);
 		c.gridx = 4;
 		c.weightx = 0.0;
 		c.gridwidth=2;
 		mainPanel.add(abortButton,c);
 
 	//set up Listeners
 		browseButton.addActionListener( new BrowseListener() );
 		abortButton.addActionListener( new AbortListener() );
 		loadButton.addActionListener( new LoadListener());
 		dirChooser.setFileFilter(new PlayListFilter());
 		importButton.addActionListener( new ImportListener());
 		selectAllButton.addActionListener(new SelectAllListener());
 		unselectAllButton.addActionListener(new UnselectAllListener());
 		
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		
 		 //set size of window
     	pack();
     	Dimension frameDim = getSize();
     	
         //get resolution
         Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
         
         //calculates the app. values
         int x = (screenDim.width - frameDim.width)/2;
         int y = (screenDim.height - frameDim.height)/2;
         
         //set location
         
         setLocation(x, y);
 
         //escape for exit
         KeyStroke escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
         //register all Strokes
         getRootPane().registerKeyboardAction(new AbortListener(), escStroke,
                 JComponent.WHEN_IN_FOCUSED_WINDOW);
         setVisible(true);
 	}
 	
 	private void setLanguage() {
 		try	{
     		setTitle(trans.getString("importStream"));
     		abortButton.setText(trans.getString("abortButton"));
     		importButton.setText(trans.getString("import"));
     		loadButton.setText(trans.getString("load"));
     		selectAllButton.setText(trans.getString("selectAll"));
     		unselectAllButton.setText(trans.getString("unselectAll"));
     		destLabel.setText(trans.getString("source"));
     		importPathField.setText(trans.getString("myPlayList"));
     		
     		//Update tableHeaders
     		importHeader[0] = trans.getString("import");
     		importHeader[1] = trans.getString("streamname");
     		importHeader[2] = trans.getString("address");
     		importHeader[3] = trans.getString("website");
     		importModel.setColumnIdentifiers(importHeader);
     		
     	} catch ( MissingResourceException e ) { 
 		      SRSOutput.getInstance().logE( e.getMessage() ); 
 	    }
 	}
 	
 	private void fillListTable() {
 		if(importableStreams != null) {
 			for(int i=0 ; i < importableStreams.length ;i++) {
 				Object[] tmp = {new Boolean(false)
 						,importableStreams[i][1]
 				        ,importableStreams[i][0]
 				        ,importableStreams[i][2]};
 				importModel.addRow(tmp);
 			}
 		}
 	}
 	
 	private Import_Streams getMe() {
 		return this;
 	}
 	
 	private void removeAllFromTable() {
 		int rowCount = importModel.getRowCount();
 		for(int i=rowCount; i>0;i--)
 			importModel.removeRow(i-1);
 	}
 	
 	private void load() {
 		String pathPlayList = importPathField.getText().toLowerCase().trim();
 		
 		if(pathPlayList.endsWith(".pls")||pathPlayList.endsWith(".m3u")) {
 			//clear old Streams
 			removeAllFromTable();
 			
 			//get filtered Streams
 			importableStreams = new Control_PlayList().anlyseFile(importPathField.getText());
 			
 			//set active
 			fillListTable();
 			selectAll();
 		}
 	}
 	
 	public void selectAll() {
 		for(int i=0;i< importTable.getRowCount();i++) {
 			importTable.setValueAt(true, i, 0);
 		}	
 	}
 	
 	public void unselectAll() {
 		for(int i=0;i< importTable.getRowCount();i++) {
 			importTable.setValueAt(false, i, 0);
 		}
 	}
 	
 	//All Listeners
 	
 	class SelectAllListener  implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			selectAll();
 		}
 	}
 	
 	class UnselectAllListener  implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			unselectAll();
 		}
 	}
 	
 	
 	class LoadListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			load();
 		}
 	}
 	
 	
 	class AbortListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			dispose();
 		}
 	}
 	
 	class BrowseListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {	
 			dirChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 			
 			int i = dirChooser.showOpenDialog(getMe());
 			if(i == JFileChooser.APPROVE_OPTION) {
 				importPathField.setText(dirChooser.getSelectedFile().toString());
 			}
 			load();
 			selectAll();
 		}
 	}
 	
 	class PlayListFilter extends FileFilter {
 	    public boolean accept(File f) {
 	        return f.isDirectory() || f.getName().toLowerCase().endsWith(".pls")
 	        			|| f.getName().toLowerCase().endsWith(".m3u");
 	    }
 	    
 	    public String getDescription() {
 	        return ".pls .m3u";
 	    }
 	}
 	
 	class ImportListener  implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			int importStreamCount = importModel.getRowCount();
 			Vector<Integer> toImportLines = new Vector<Integer>(0,1);
 			Boolean errorAcc = false;
 			Boolean ignoreAll = false;
 			
 			//gets lines, where the JCheckBox is selected
 			for(int i=0; i< importStreamCount; i++) {
 				//the an name field is empty
 				if(!errorAcc && importModel.getValueAt(i, 1).toString().trim().equals("")) {
 					JOptionPane.showMessageDialog(getMe(), 
 							trans.getString("Import.everyName"));
 					errorAcc = true;
 				}
 				
 				else if(!errorAcc && (Boolean)importModel.getValueAt(i, 0)) {
 					if(mainGui.getControlStream().existURL(
 							importModel.getValueAt(i, 2).toString().trim()))
 					{
 						if(!ignoreAll) {
 							//show a Dialog about what to do
 							Object[] options = { trans.getString("import.Options0")
 									,trans.getString("import.Options1") , trans.getString("import.Options2")};
 							int choose = JOptionPane.showOptionDialog(getMe(),
 									trans.getString("import.URLExist")
 									+importModel.getValueAt(i, 2).toString().trim()+
 									trans.getString("import.WhatDo"),
 									trans.getString("import.URLExistHeader"),
 									JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
 						            null, options, options[0]);
 							//if 0 : abort
 							if(choose == 0) {
 								errorAcc = true;
 							}
 							//if 1 : ignore
 							else if(choose == 1) {
 								//simply do nothing here
 							}
 							//if 2 : ignore all
 							else if(choose == 2) {
 								ignoreAll =true;
 							}
 						}
 					}
 					else {
 						toImportLines.add(i);
 					}
 				}
 			}
 			
 			if(!errorAcc) {
 				//now can add stream
 				//line by line
 				for(int i=0; i< toImportLines.capacity(); i++) {
					String[] stream = new String[3];
 					//Name
 					stream[1] = importModel.getValueAt(toImportLines.get(i), 1).toString();
 					//Address
 					stream[0] = importModel.getValueAt(toImportLines.get(i), 2).toString();
 					//Website
 					stream[2] = importModel.getValueAt(toImportLines.get(i), 3).toString();
 					
 					//Add all to files
 					Gui_StreamOptions addStream = new Gui_StreamOptions(null,mainGui,true,false,false);
 					addStream.setBasics(stream);
 					addStream.save();
 				}
 				JOptionPane.showMessageDialog(getMe(),toImportLines.capacity()+" "+trans.getString("addedStreams"));
 				controlStreams.saveStreamVector();
 				dispose();
 			}
 			
 		}
 	}
 }
