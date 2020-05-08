 package org.geworkbench.engine.ccm;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.ItemSelectable;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.font.TextAttribute;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.RowFilter;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableRowSorter;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.builtin.projects.ProjectPanel;
 import org.geworkbench.engine.config.rules.GeawConfigObject;
 import org.geworkbench.engine.management.ComponentRegistry;
 import org.geworkbench.engine.preferences.GlobalPreferences;
 import org.geworkbench.util.BrowserLauncher;
 import org.geworkbench.util.Util;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * This is a shadow class for ComponentConfigurationManagerWindow to support the component upgrade feature.
  * TODO This class lacks adequate test and has fell behind since it was introduced
  * considering the entire system has evolved in many ways;
  * it also has much duplicate code. It should eventually be merged with ComponentConfigurationManagerWindow,
  * or re-implemented, or dropped.
  * 
  * @author tg2321
  * @version $Id$
  */
 public class ComponentConfigurationManagerWindow2 {
 
 	private static Log log = LogFactory.getLog(ComponentConfigurationManagerWindow2.class);
 	
 	private CCMTableModel2 ccmTableModel;
 //	protected ComponentConfigurationManager2 manager = null;	
 	ComponentConfigurationManager2 manager = null;
 	
 	private JFrame frame;
 	private JPanel topPanel;
 	private JLabel displayLabel;
 	private JComboBox displayComboBox;
 	private JLabel showByTypeLabel;
 	private JComboBox showByTypeComboBox;
 	private JLabel keywordSearchLabel;
 	private JTextField keywordSearchField;
 	private JSplitPane splitPane;
 	private JScrollPane scrollPaneForTable;
 	private JTable table;
 	private JScrollPane scrollPaneForTextPane;
 	private JTextPane textPane;
 	private JPanel bottompanel;
 	
 	private JButton viewLicenseButton = new JButton("View License");
 	private JButton applyButton = new JButton("Apply");
 	private JButton resetButton = new JButton("Reset");
 	private JButton closeButton = new JButton("Close");
 	private JButton componentUpdateButton = new JButton("Search for updates");
 	
 	/* TODO remove 999 logic during refactoring */
 	private static final int initialRow = 999;
 	private static final int initialColumn = 999;
 	private static int launchedRow = initialRow;
 	private static int launchedColumn = initialColumn;
 	private static int installedRow = initialRow;
 	
 	private final static String DISPLAY_FILTER_ALL = "All";
 	private final static String DISPLAY_ONLY_LOADED = "Only loaded";
 	private final static String DISPLAY_ONLY_UNLOADED = "Only unloaded";
 	
 	private ArrayList<Boolean> originalChoices = null;
 
 	private static ComponentConfigurationManagerWindow2 ccmWindow = null;
 	/**
 	 * Constructor
 	 * Provides a call-back to the {@link ComponentConfigurationManagerMenu}.
 	 * 
 	 * @param ComponentConfigurationManagerMenu
 	 */
 	private ComponentConfigurationManagerWindow2() {
 
 		manager = new ComponentConfigurationManager2();
 
 		//TODO
 		// for coexistence of CCM and CCM2
 		// REMOVE WHEN MERGED. 
 				ComponentConfigurationManager originalManager = ComponentConfigurationManager.getInstance(); 
 				manager.cwbFiles = originalManager.cwbFile;
 				manager.componentConfigurationManager.files = originalManager.files;
 		//		
 		
 		
 		initComponents();
 		
 	}
 
 	
 	/**
 	 * Load method
 	 */
 	public static void load(){
 		if(ccmWindow == null){
 			ccmWindow = new ComponentConfigurationManagerWindow2();
 		}
 		ccmWindow.frame.setExtendedState(Frame.NORMAL);
 		ccmWindow.frame.setVisible(true);
 	}
 
 	
 	/**
 	 * Load method
 	 */
 	public static void load(ComponentConfigurationManagerMenu menu){
 		if(ccmWindow == null){
 			ccmWindow = new ComponentConfigurationManagerWindow2();
 		}
 		ccmWindow.frame.setExtendedState(Frame.NORMAL);
 		ccmWindow.frame.setVisible(true);
 	}
 	
 	private TableRowSorter<CCMTableModel2> sorter = null;
 
 	/**
 	 * Set up the GUI
 	 * 
 	 * @param void
 	 * @return void
 	 */
 	private void initComponents() {
 		frame = new JFrame("geWorkbench - Component Configuration Manager");
 		
 		topPanel = new JPanel();
 		displayLabel = new JLabel();
 		String[] displayChoices = { DISPLAY_FILTER_ALL, DISPLAY_ONLY_LOADED, DISPLAY_ONLY_UNLOADED };
 		displayComboBox = new JComboBox(displayChoices);
 		showByTypeLabel = new JLabel();
 		String[] showByTypeChoices = new String[PluginComponent.categoryList.size()+2];
 		showByTypeChoices[0] = SHOW_BY_TYPE_ALL;
 		int index = 1;
 		for(String s: PluginComponent.categoryList){
 			showByTypeChoices[index] =  s.substring(0, 1).toUpperCase()+s.substring(1).toLowerCase();
 			index++;
 		};
 		showByTypeChoices[index] = SHOW_BY_TYPE_OTHERS; 
		Arrays.sort(showByTypeChoices);
 		showByTypeComboBox = new JComboBox(showByTypeChoices);
		showByTypeComboBox.setMaximumRowCount(showByTypeChoices.length);
 		keywordSearchLabel = new JLabel("Keyword search:");
 		keywordSearchField = new JTextField("Enter Text");
 		splitPane = new JSplitPane();
 		scrollPaneForTextPane = new JScrollPane();
 		textPane = new JTextPane();
 		bottompanel = new JPanel();
 		CellConstraints cc = new CellConstraints();
 		
 	     frame.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				ccmWindow = null;
 			}
 		});
 	     
 	     viewLicenseButton.addActionListener(new ActionListener(){
 	    	public void actionPerformed(ActionEvent e) {
 				viewLicense_actionPerformed(e);
 	    	}
 	     } );
 
 	     
 		applyButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				applyCcmSelections_actionPerformed(e);
 			}
 		});
 		resetButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				resetCcmSelections_actionPerformed(e);
 			}
 
 		});
 		closeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				closeCcmSelections_actionPerformed(e);
 			}
 
 		});
 		componentUpdateButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				componentRemoteUpdate_actionPerformed(e);
 			}
 
 		});
 		
 		//======== frame ========
 		{
 			Container frameContentPane = frame.getContentPane();
 			frameContentPane.setLayout(new BorderLayout());
 
 			//======== outerPanel ========
 			{
 				
 				frameContentPane.addPropertyChangeListener(
 						new java.beans.PropertyChangeListener() {
 							public void propertyChange(
 									java.beans.PropertyChangeEvent e) {
 								if ("border".equals(e.getPropertyName()))
 									throw new RuntimeException();
 							}
 						});
 
 				//======== topPanel ========
 				{
 					FormLayout topPanelLayout = new FormLayout(
 							" 32dlu, default,  4dlu, default,  32dlu, default,  4dlu, default, 32dlu, default,  4dlu, 64dlu, 32dlu",
 							"center:25dlu"); 
 					topPanel.setLayout(topPanelLayout);
 					
 					//---- displayLabel ----
 					displayLabel.setText("Display:");
 					topPanel.add(displayLabel, cc.xy(2, 1));
 					//======== scrollPaneForTopList1 ========
 					{
 						//---- displayComboBox ----
 					    ActionListener actionListener = new ActionListener() {
 					        public void actionPerformed(ActionEvent actionEvent) {
 					          ItemSelectable is = (ItemSelectable)actionEvent.getSource();
 					          Object[] selections = is.getSelectedObjects();
 					          String selection = (String)selections[0];
 					          ccmTableModel.setLoadedFilterValue(selection);
 					          sorter.setRowFilter(combinedFilter);
 					          ccmTableModel.fireTableDataChanged();
 					        }
 					    };
 						
 						displayComboBox.addActionListener(actionListener);
 					}
 					topPanel.add(displayComboBox, cc.xy(4, 1));
 
 					//---- showByTypeLabel ----
 					showByTypeLabel.setText("Show by type:");
 					topPanel.add(showByTypeLabel, cc.xy(6, 1));
 					//======== scrollPaneForTopList2 ========
 					{
 						//---- showByTypeComboBox ----
 					    ActionListener actionListener2 = new ActionListener() {
 					        public void actionPerformed(ActionEvent actionEvent) {
 					          ItemSelectable is = (ItemSelectable)actionEvent.getSource();
 					          Object[] selections = is.getSelectedObjects();
 					          String selection = (String)selections[0];
 					          ccmTableModel.setTypeFilterValue(selection);
 					          sorter.setRowFilter(combinedFilter);
 					          ccmTableModel.fireTableDataChanged();
 					        }
 					    };
 
 					    showByTypeComboBox.addActionListener(actionListener2);
 					}
 					topPanel.add(showByTypeComboBox, cc.xy(8, 1));
 
 					//---- topLabel3 ----					
 					topPanel.add(keywordSearchLabel, cc.xy(10, 1));
 					
 					//======== scrollPaneForTopList3 ========
 					{
 						// ---- keywordSearchField ----
 						KeyListener actionListener3 = new KeyListener() {
 
 							public void keyPressed(KeyEvent e) {
 							}
 
 							public void keyReleased(KeyEvent e) {
 								String text = keywordSearchField.getText();
 								ccmTableModel.setKeywordFilterValue(text);
 								sorter.setRowFilter(combinedFilter);
 								ccmTableModel.fireTableDataChanged();
 							}
 
 							public void keyTyped(KeyEvent e) {
 							}
 						};
 
 						keywordSearchField.setText("Enter Text");
 						keywordSearchField.addKeyListener(actionListener3);
 					}
 					topPanel.add(keywordSearchField, cc.xy(12, 1));
 				} // Top Panel
 				frameContentPane.add(topPanel, BorderLayout.NORTH);
 
 				//======== splitPane ========
 				{
 					splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
 					splitPane.setResizeWeight(0.5);
 
 					//======== scrollPaneForTable ========
 					{
 						//---- table ----
 						ccmTableModel = new CCMTableModel2(manager.componentConfigurationManager);
 						setOriginalChoices();
 						table = new JTable(ccmTableModel);
 						sorter = new TableRowSorter<CCMTableModel2>(ccmTableModel);
 						table.setRowSorter(sorter);
 
 					    table.setDefaultRenderer(Object.class, new CellRenderer());
 					    table.setDefaultRenderer(CCMTableModel2.ImageLink.class, new ImageLinkRenderer());
 					    table.setDefaultRenderer(CCMTableModel2.HyperLink.class, new HyperLinkRenderer());
 					    table.setDefaultRenderer(CCMTableModel2.DownloadLink.class, new DownloadLinkRenderer());
 					    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 
 						ListSelectionModel cellSM = table.getSelectionModel();
 						cellSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 						cellSM.addListSelectionListener(new ListSelectionListener() {
 						    public void valueChanged(ListSelectionEvent e) {
 						    	boolean adjusting = e.getValueIsAdjusting();
 						    	if (adjusting){
 							    	return;
 						    	}
 						        int[] selectedRow = table.getSelectedRows();
 						        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
 						        if (lsm.isSelectionEmpty()) {
 									textPane.setText(" ");
 						        } else {
 						            String description = (String)ccmTableModel.getValueAt(table.convertRowIndexToModel(selectedRow[0]), CCMTableModel2.DESCRIPTION_INDEX);
 						        	textPane.setText(description);
 						        	
 						            if (textPane.getCaretPosition() > 1){
 						            	textPane.setCaretPosition(1);        	
 						            }
 						        }
 
 						        if(table.getSelectedRow()>=0) {
 						        	int modelColumn = table.convertColumnIndexToModel(table.getSelectedColumn());
 						        	if(modelColumn==CCMTableModel2.AVAILABLE_UPDATE_INDEX)
 								        installRemoteComponent();
 						        	else
 						        		launchBrowser();
 						        }
 							}
 						});
 						
 						TableColumn column = table.getColumnModel().getColumn(CCMTableModel2.SELECTION_INDEX);
 						column.setMaxWidth(50);
 						column = table.getColumnModel().getColumn(CCMTableModel2.VERSION_INDEX);
 						column.setMaxWidth(60);
 						column = table.getColumnModel().getColumn(CCMTableModel2.AVAILABLE_UPDATE_INDEX);
 						column.setMaxWidth(60);
 						column = table.getColumnModel().getColumn(CCMTableModel2.TUTORIAL_URL_INDEX_2);
 						column.setMaxWidth(70);
 						column = table.getColumnModel().getColumn(CCMTableModel2.TOOL_URL_INDEX_2);
 						column.setMaxWidth(70);
 						
 						scrollPaneForTable = new JScrollPane(table);
 					}
 					splitPane.setTopComponent(scrollPaneForTable);
 
 					//======== scrollPaneForTextPane ========
 					{
 						//---- textPane ----
 						textPane.setEditable(false);
 						scrollPaneForTextPane.setViewportView(textPane);
 					}
 					splitPane.setBottomComponent(scrollPaneForTextPane);
 				} //======== splitPane ========.
 				frameContentPane.add(splitPane, BorderLayout.CENTER);			
 
 				//======== bottompanel ========
 				{
 					bottompanel.setLayout(new FormLayout(   "20dlu,"            + 
 															"default,  4dlu, " + // view License
 															"default,100dlu, " + // Component Update
 															"default,  4dlu, " + // Apply
 															"default,  4dlu, " + // Reset
 															"default,  4dlu, " + // Cancel
 															"default "           // Close
 															,
 															"center:25dlu"));
 					
 					viewLicenseButton.setText("View License");
 					bottompanel.add(viewLicenseButton, cc.xy(2, 1));
 
 					//---- componentUpdateButton ----
 					bottompanel.add(componentUpdateButton, cc.xy(6, 1));
 					
 					//---- applyButton ----
 					applyButton.setText("Apply");
 					bottompanel.add(applyButton, cc.xy(8, 1));
 
 					//---- resetButton ----
 					resetButton.setText("Reset");
 					bottompanel.add(resetButton, cc.xy(10, 1));
 
 					//---- closeButton ----
 					closeButton.setText("Close");
 					bottompanel.add(closeButton, cc.xy(12, 1));
 					
 				} //======== bottompanel ========.
 				frameContentPane.add(bottompanel, BorderLayout.SOUTH);
 			} //======== outerPanel ========
 			frame.pack();
 			frame.setLocationRelativeTo(frame.getOwner());
 		} // ============ frame ============
 
 		topPanel.setVisible(true);
 		splitPane.setVisible(true);
 		scrollPaneForTable.setVisible(true);
 		table.setVisible(true);
 		scrollPaneForTextPane.setVisible(true);
 		textPane.setVisible(true);
 		bottompanel.setVisible(true);
 		sorter.setRowFilter(combinedFilter);
 		frame.setVisible(true);
 		splitPane.setDividerLocation(.7d);
 	}
 	
 	final private RowFilter<CCMTableModel2, Integer> hiddenFilter = new RowFilter<CCMTableModel2, Integer>() {
 		public boolean include(
 				Entry<? extends CCMTableModel2, ? extends Integer> entry) {
 
 			CCMTableModel2 model = (CCMTableModel2) entry.getModel();
 
 			Boolean hidden = (Boolean) model.getModelValueAt(entry
 					.getIdentifier(), CCMTableModel2.HIDDEN_INDEX);
 			if (hidden)
 				return false;
 			else
 				return true;
 		}
 	};
 
 	final private RowFilter<CCMTableModel2, Integer> loadFilter = new RowFilter<CCMTableModel2, Integer>() {
 		public boolean include(
 				Entry<? extends CCMTableModel2, ? extends Integer> entry) {
 
 			CCMTableModel2 model = (CCMTableModel2) entry.getModel();
 
 			String loadedFilterValue = model.getLoadedFilterValue();
 			if(loadedFilterValue==null || loadedFilterValue.equals(ComponentConfigurationManagerWindow2.DISPLAY_FILTER_ALL))
 				return true;
 			
 			boolean loaded = componentLoaded(entry.getIdentifier());
 			if (loaded && loadedFilterValue.equals(ComponentConfigurationManagerWindow2.DISPLAY_ONLY_LOADED)
 					||
 					!loaded && loadedFilterValue.equals(ComponentConfigurationManagerWindow2.DISPLAY_ONLY_UNLOADED))
 				return true;
 
 			return false;
 		}
 	};
 
 	private final static String SHOW_BY_TYPE_ALL = "All";
 	private final static String SHOW_BY_TYPE_OTHERS = "Others";
 
 	/**
 	 * type filter: analysis or visualization
 	 */
 	final private RowFilter<CCMTableModel2, Integer> typeFilter = new RowFilter<CCMTableModel2, Integer>() {
 		@Override
 		public boolean include(
 				Entry<? extends CCMTableModel2, ? extends Integer> entry) {
 
 			CCMTableModel2 model = (CCMTableModel2) entry.getModel();
 			String typeFilterValue = model.getTypeFilterValue();
 			if (typeFilterValue == null
 					|| typeFilterValue
 							.equals(SHOW_BY_TYPE_ALL))
 				return true;
 
 			String[] category = (String[]) model.getModelValueAt(entry
 					.getIdentifier(), CCMTableModel.CATEGORY_INDEX);
 			if (Arrays.asList( category ).contains(typeFilterValue.toLowerCase()) )
 				return true;
 			
 			if ( category.length==0
 					&& typeFilterValue
 							.equals(SHOW_BY_TYPE_OTHERS))
 				return true;
 
 			return false;
 		}
 	};
 
 	/**
 	 * type filter: analysis or visualization
 	 */
 	final private RowFilter<CCMTableModel2, Integer> keywordSearchFilter = new RowFilter<CCMTableModel2, Integer>() {
 		public boolean include(
 				Entry<? extends CCMTableModel2, ? extends Integer> entry) {
 
 			CCMTableModel2 model = (CCMTableModel2) entry.getModel();
 			String keywordFilterValue = model.getKeywordFilterValue();
 			if (keywordFilterValue == null
 					|| keywordFilterValue.equals("") ||
 					keywordFilterValue.equals("text"))
 				return true;
 
 			keywordFilterValue = keywordFilterValue.toLowerCase().trim();
 			
 			for(int j=CCMTableModel2.FIRST_STRING_COLUMN; j<CCMTableModel2.AUTHOR_INDEX; j++ ) {
 				String fieldValue = ((String) model.getModelValueAt(entry
 						.getIdentifier(), j)).toLowerCase();
 				if(fieldValue.contains(keywordFilterValue))
 					return true;
 			}
 
 			return false;
 		}
 	};
 
 	final private List<RowFilter<CCMTableModel2, Integer>> filters = new ArrayList<RowFilter<CCMTableModel2, Integer>>();
 	{
 		filters.add(hiddenFilter);
 		filters.add(loadFilter);
 		filters.add(typeFilter);
 		filters.add(keywordSearchFilter);
 	}
 	final private RowFilter<CCMTableModel2, Integer> combinedFilter = RowFilter
 			.andFilter(filters);
 
 	/*
 	 * launchBrowser for URLs in CCM GUI 
 	 */
 	private void launchBrowser(){
         int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
         int modeColumn = table.convertColumnIndexToModel(table.getSelectedColumn());
         
    		if (launchedRow == modelRow && launchedColumn == modeColumn){
     			return;
    		}
    		
    		launchedRow = modelRow;
    		launchedColumn = modeColumn;
     		
    		String url = null;
    		Object obj = ccmTableModel.getValueAt(modelRow, modeColumn);
    		if(obj==null) return;
    			
    		if(obj instanceof CCMTableModel2.HyperLink)
    			url = ((CCMTableModel.HyperLink)obj).url;
    		else if(obj instanceof CCMTableModel.ImageLink)
    			url = ((CCMTableModel.ImageLink)obj).url;
    		else
 			return;
    		
    		if(url==null)return;
 
 		try {
 			BrowserLauncher.openURL(url);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	/**
 	 * Display a dialog box with a components license in it.
 	 * 
 	 * @param ActionEvent
 	 * @return void
 	 */
 	private void viewLicense_actionPerformed(ActionEvent e) {
 
         int[] selectedRow = table.getSelectedRows();
 
         String license = "Select a component in order to view its license.";
         String componentName = null;
         if (   selectedRow != null && selectedRow.length > 0 && selectedRow[0] >= 0) {
 
     		int modelRow = table.convertRowIndexToModel( selectedRow[0] );
     		license = (String) ccmTableModel.getModelValueAt(modelRow, CCMTableModel2.LICENSE_INDEX);
     		componentName = (String) ccmTableModel.getModelValueAt(modelRow, CCMTableModel2.NAME_INDEX);
         }
         
         JDialog licenseDialog = new JDialog();
         final JEditorPane jEditorPane = new JEditorPane("text/html", "");
         jEditorPane.getDocument().putProperty("IgnoreCharsetDirective",Boolean.TRUE);
         jEditorPane.setText(license);
         if (jEditorPane.getCaretPosition() > 1){
             jEditorPane.setCaretPosition(1);        	
         }
 		JScrollPane scrollPane = new JScrollPane(jEditorPane);
 		licenseDialog.setTitle(componentName + " License");
 		licenseDialog.setContentPane(scrollPane);
 		licenseDialog.setSize(400,300);
 		licenseDialog.setLocationRelativeTo(frame);
 		licenseDialog.setVisible(true);
 	}
 	
 	/**
 	 * Persist users component selections 
 	 * Add newly selected components 
 	 * Remove newly unselected components Leave CCM Window open
 	 * 
 	 * @param ActionEvent
 	 * @return void
 	 */
 	private void applyCcmSelections_actionPerformed(ActionEvent e) {
 		Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
 		frame.setCursor(hourglassCursor);
 		
 		for (int i = 0; i < ccmTableModel.getModelRowCount(); i++) {
 
 			boolean choice = ((Boolean) ccmTableModel.getModelValueAt(i,
 					CCMTableModel2.SELECTION_INDEX)).booleanValue();
 
 			boolean originalChoice = this.originalChoices.get(i).booleanValue();
 			/* No change in selection */
 			if (choice == originalChoice) {
 				continue;
 			}
 
 			String resource = ccmTableModel.getResourceFolder(i);
 			File file = ccmTableModel.getFile(i);
 			String filename = file.getName();
 
 			String propFileName = null;
 			if (filename.endsWith(".cwb.xml")) {
 				propFileName = filename.replace(".cwb.xml", ".ccmproperties");
 			} else {
 				log.error("File name is "+filename+" when .cwb.xml file is expected");
 				continue;
 			}
 			String sChoice = (new Boolean(choice)).toString();
 			
 			ComponentConfigurationManager.writeProperty(resource, propFileName, "on-off", sChoice);
 			
 			if (choice) {
 				manager.componentConfigurationManager.loadComponent(file);
 				continue;
 			}
 
 			/* Remove Component */
 			manager.componentConfigurationManager.removeComponent(resource, file.getAbsolutePath());
 
 			ccmTableModel.fireTableDataChanged();
             if (textPane.getCaretPosition() > 1){
             	textPane.setCaretPosition(1);        	
             }
 		}
 		GeawConfigObject.recreateHelpSets();
 
 		ProjectPanel.getInstance().ccmUpdate();
 
 		setOriginalChoices();
 
 		Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
 		frame.setCursor(normalCursor);
 	}
 	
 	/**
 	 * Reset selections. Leave Window open
 	 * 
 	 * @param ActionEvent
 	 * @return void
 	 */
 	private void resetCcmSelections_actionPerformed(ActionEvent e) {
 		for (int i = 0; i < ccmTableModel.getModelRowCount(); i++) {
 			Boolean originalChoice = this.originalChoices.get(i);
 			ccmTableModel.setModelValueAt(originalChoice, i, CCMTableModel2.SELECTION_INDEX, CCMTableModel2.NO_VALIDATION);
 		}
 
 	}
 
 	/**
 	 * Reset selections Close CCM Window
 	 * 
 	 * @param ActionEvent
 	 * @return void
 	 */
 	private void closeCcmSelections_actionPerformed(ActionEvent e) {
 		frame.dispose();
 		ccmWindow = null;
 	}
 
 	
 	/*
 	 * Look for updated Components
 	 * 
 	 * @param ActionEvent
 	 * @return void
 	 */
 	private void componentRemoteUpdate_actionPerformed(ActionEvent e) {
 	
 		findAvailableUpdatesFromTomcat();
 	}
 	
 	
 	/**
 	 * Save the original selections for use with resetCcmSelections action
 	 * 
 	 * @param void
 	 * @return void
 	 */
 	private void setOriginalChoices() {
 		originalChoices = new ArrayList<Boolean>(ccmTableModel.getModelRowCount());
 		for (int i = 0; i < ccmTableModel.getModelRowCount(); i++) {
 			originalChoices.add(i, (Boolean) ccmTableModel.getModelValueAt(i, CCMTableModel2.SELECTION_INDEX));
 		}
 	}
 
 	/*
 	 * Look on Tomcat Server for available components
 	 * 
 	 * @param void
 	 * 
 	 * @return void
 	 */
 	private void findAvailableUpdatesFromTomcat() {
 
 		
 		installedRow = initialRow;
 //		String url = System.getProperty("remote_components_config_url");
 		//remote_components_config_url=http://califano11.cgc.cpmc.columbia.edu:8080/componentRepository/deploycomponents.txt
 
 		GlobalPreferences prefs = GlobalPreferences.getInstance();
 		String url = prefs.getRCM_URL().trim();
 		if (url == null || url == "") {
 			log.info("No Remote Component Manager URL configured.");
 			return;
 		}
 		url += "/deploycomponents.txt";
 		
 		HttpClient client = new HttpClient();
 		client.getHttpConnectionManager().getParams().setConnectionTimeout(3000); 
 		GetMethod method = new GetMethod(url);
 		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
 				new DefaultHttpMethodRetryHandler(3, false));
 		method.setFollowRedirects(true);
 
 		try {
 			int statusCode = client.executeMethod(method);
 			if (statusCode != HttpStatus.SC_OK) {
 				JOptionPane.showMessageDialog(null,
 								"No updates are available at this time.\nPlease try again later.",
 								"Remote Component Update",
 								JOptionPane.PLAIN_MESSAGE);
 				return;
 			}
 
 			String deploycomponents = method.getResponseBodyAsString();
 //			String[] rows = deploycomponents.split("\\r\\n");
 			String[] folderNameVersions = deploycomponents.split("\\r\\n");
 			
 			for (int i = 0; i < ccmTableModel.getModelRowCount(); i++) {
 				String localFolderName = ccmTableModel.getResourceFolder(i);
 				
 				String localVersion = ((String) ccmTableModel.getModelValueAt(
 						i, CCMTableModel2.VERSION_INDEX));
 				Double dLocalVersion = new Double(localVersion);
 
 				for (int j = 0; j < folderNameVersions.length; j++) {
 					String[] cols = folderNameVersions[j].split(",");
 					String remoteFolderName = cols[0];
 					String remoteVersion = cols[1];
 					Double dRemoteVersion = new Double(remoteVersion);
 
 					if (localFolderName.equalsIgnoreCase(remoteFolderName)) {
 						if (dRemoteVersion.compareTo(dLocalVersion) > 0) {
 							ccmTableModel.setModelValueAt(remoteVersion, i,
 									CCMTableModel2.AVAILABLE_UPDATE_INDEX,
 									CCMTableModel2.NO_VALIDATION);
 						}
 					}
 				}
 			}
 		} catch (HttpException e) {
 			JOptionPane
 					.showMessageDialog(
 							null,
 							"No updates are available at this time.\nPlease try again later.",
 							"Remote Component Update",
 							JOptionPane.PLAIN_MESSAGE);
 			//e.printStackTrace();
 			return;
 		} catch (IOException e) {
 			JOptionPane
 					.showMessageDialog(
 							null,
 							e.getMessage()+".\n("+e.getClass().getName()+")\nPlease try again later.",
 							"No Update Available",
 							JOptionPane.PLAIN_MESSAGE);
 			//e.printStackTrace();
 			return;
 		} catch (Exception e) { // IllegalArgumentException
 			JOptionPane
 			.showMessageDialog(
 					null,
 					e.getMessage()+".",
 					"No Update Available",
 					JOptionPane.PLAIN_MESSAGE);
 		}
 	}
 
 	/*
 	 * Copy GEAR file to users temp directory to be installed after geWorkbench
 	 * restart.
 	 * 
 	 * @param void
 	 * 
 	 * @return void
 	 */
 	private void installRemoteComponent() { 
 		int modelColumn = table.convertColumnIndexToModel(table.getSelectedColumn());
     	if(modelColumn!=CCMTableModel2.AVAILABLE_UPDATE_INDEX)return;
 
 		int[] selectedRows = table.getSelectedRows();
 		
 		if (selectedRows != null && selectedRows.length > 0
 				&& selectedRows[0] >= 0 ) {
 
 			int modelRow = table.convertRowIndexToModel(selectedRows[0]);
 			
 			if (installedRow == modelRow) {
 				installedRow = initialRow;
 				return;
 			}
 			installedRow = modelRow;
 
 			CCMTableModel2.DownloadLink availableUpdateLink = (CCMTableModel2.DownloadLink)(ccmTableModel
 					.getValueAt(modelRow, CCMTableModel2.AVAILABLE_UPDATE_INDEX));
 
 			String availableUpdate = availableUpdateLink.text;
 			availableUpdate = availableUpdate.replace("<html><font><u>", "");
 			availableUpdate = availableUpdate.replace("</u><br></font>", "");
 			
 			if (availableUpdate == null || availableUpdate.trim().equals("")) {
 				return;
 			}
 
 			String componentFolder = ccmTableModel.getResourceFolder(modelRow);
 			
 			/* Get a list of component names from the give folder */
 			String componentNames = "";
 			int count = 0;
 			for (int i = 0; i < ccmTableModel.getModelRowCount(); i++) {
 				String folder = ccmTableModel.getResourceFolder(i);
 				
 				if (!componentFolder.equalsIgnoreCase(folder)) {
 					continue;
 				}
 
 				if (count++ > 5) {
 					componentNames += "etc...";
 					break;
 				}
 				componentNames += ((String) ccmTableModel.getModelValueAt(i,
 						CCMTableModel2.NAME_INDEX)) + "\n";
 			}
 
 			if (!(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null,
 					"Would you like to update the following components:\n\n"
 							+ componentNames + "\n", "Remote Component Update",
 					JOptionPane.YES_NO_OPTION))) {
 				return;
 			}
 			
 			String componentFolderName = ccmTableModel.getResourceFolder(modelRow);
 			
 			
 			if (!manager.downloadGEARFromServer(componentFolderName)) {
 				JOptionPane.showMessageDialog( null,
 								componentNames + " did not download properly. Please try again later.",
 								"Remote Component Update",
 								JOptionPane.ERROR_MESSAGE);
 				installedRow = initialRow;
 				return;
 			}
 
 			/* turn off all related components */
 			for (int i = 0; i < ccmTableModel.getModelRowCount(); i++) {
 				String modelFolder = ccmTableModel.getResourceFolder(i);
 				
 				if (!componentFolder.equalsIgnoreCase(modelFolder)) {
 					continue;
 				}
 
 				ccmTableModel.setModelValueAt(false, i, CCMTableModel2.SELECTION_INDEX, CCMTableModel2.NO_VALIDATION);
 				
 				File file = ccmTableModel.getFile(i);
 				/* Turn off component */
 				manager.componentConfigurationManager.removeComponent(componentFolderName, file.getAbsolutePath());
 				originalChoices.set(i, false);
 			}
 			
 			/* Install GEAR file */
 			manager.installGEAR(componentFolderName, availableUpdate);
 			
 			/* Set new folder names for updated components */
 			for (int i = 0; i < ccmTableModel.getModelRowCount(); i++) {
 				String modelFolder = ccmTableModel.getResourceFolder(i);
 				
 				if (!componentFolder.equalsIgnoreCase(modelFolder)) {
 					continue;
 				}
 
 				ccmTableModel.setResourceFolder(i, componentFolderName + "." + availableUpdate);
 				ccmTableModel.setModelValueAt(availableUpdate, i, CCMTableModel2.VERSION_INDEX,
 						CCMTableModel2.NO_VALIDATION);
 				ccmTableModel.setModelValueAt("", i, CCMTableModel2.AVAILABLE_UPDATE_INDEX,
 						CCMTableModel2.NO_VALIDATION);
 				
 				/* Put updated cwb file into model from cwb list*/
 				File file = ccmTableModel.getFile(i);
 				String cwbFileName = file.getName();
 				File newCwbFile = manager.getCwbFile(cwbFileName);
 				ccmTableModel.setFile(i, newCwbFile);
 				String[]files = manager.componentConfigurationManager.files;
 				for (int j=0; j<files.length; j++){
 					if (files[j].equals(componentFolderName)){
 						files[j] = componentFolderName + "." + availableUpdate;
 						break;
 					}
 				}
 
 				/* Turn on component */
 				ccmTableModel.setModelValueAt(true, i, CCMTableModel2.SELECTION_INDEX, CCMTableModel2.NO_VALIDATION);
 				originalChoices.set(i, true);
 			}
 		}
 	}
 
 	
 	
 	
 	private boolean componentLoaded(int modelRow){
 		boolean loaded = false;
 		
 		String pluginClazzName = (String)ccmTableModel.getModelValueAt(modelRow, CCMTableModel2.CLASS_INDEX);
 		
 		ComponentRegistry componentRegistry = ComponentRegistry.getRegistry();
 		List<Object> components = componentRegistry.getComponentsList();
 		for (Object proxiedComponent : components) {
 			// FIXME use a "deproxy" (see cglib or HibernateProxy)
 			Class<?> clazz = proxiedComponent.getClass();
 			String proxiedClazzName = clazz.getName();
 			String[] temp = StringUtils.split(proxiedClazzName, "$$");
 			String clazzName = temp[0];
 
 			if (StringUtils.equals(pluginClazzName, clazzName)) {
 				loaded = true;
 			}
 		}
 		return loaded;			
 	}
 
 	/**
 	 * This render makes the cmm-selected row darker.
 	 * @author zji
 	 *
 	 */
 	static private class CellRenderer extends DefaultTableCellRenderer {
 		private static final long serialVersionUID = 4878020589478015309L;
 
 		@Override
 		public Component getTableCellRendererComponent(JTable table,
 				Object value, boolean isSelected, boolean hasFocus, int row,
 				int column) {
 			
 			Component c = super.getTableCellRendererComponent(table, value,
 					isSelected, hasFocus, row, column);
 			return c;
 		}
 	}
 
 	static private class ImageLinkRenderer extends CellRenderer {
 		private static final long serialVersionUID = 8730940505472251871L;
 
 		private static ImageIcon colored = Util
 				.createImageIcon("/org/geworkbench/engine/visualPlugin.png");
 		private static ImageIcon grayed = Util
 				.createImageIcon("/org/geworkbench/engine/visualPluginGrey.png");
 
 		@Override
 		public Component getTableCellRendererComponent(JTable table,
 				Object value, boolean isSelected, boolean hasFocus, int row,
 				int column) {
 			Component c = super.getTableCellRendererComponent(table, value,
 					isSelected, hasFocus, row, column);
 			if(!(c instanceof JLabel)) { // this is for safe guard. should not happen
 				return c;
 			}
 			JLabel label = (JLabel)c;
 			label.setText(null);
 			label.setToolTipText(((CCMTableModel2.ImageLink) value).url);
 			
 			CCMTableModel2.LinkIcon linkIcon = ((CCMTableModel2.ImageLink) value).image;
 			if(linkIcon==CCMTableModel2.LinkIcon.COLORED)
 				label.setIcon(colored);
 			else if (linkIcon==CCMTableModel2.LinkIcon.GRAYED)
 				label.setIcon(grayed);
 			
 			return label;
 		}
 	}
 
 	static private class HyperLinkRenderer extends CellRenderer {
 		private static final long serialVersionUID = -1378393715835011075L;
 
 		@Override
 		public Component getTableCellRendererComponent(JTable table,
 				Object value, boolean isSelected, boolean hasFocus, int row,
 				int column) {
 
 			Component c = super.getTableCellRendererComponent(table,
 					((CCMTableModel2.HyperLink) value).text, isSelected,
 					hasFocus, row, column);
 
 			if (!isSelected)
 				c.setForeground(Color.blue);
 
 			Font font = c.getFont();
 			Map<TextAttribute, Object> attributes = new Hashtable<TextAttribute, Object>();
 			attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
 			c.setFont(font.deriveFont(attributes));
 			
 			setToolTipText(((CCMTableModel2.HyperLink) value).url);
 			return c;
 		}
 	}
 
 	static private class DownloadLinkRenderer extends CellRenderer {
 		private static final long serialVersionUID = -1378393715835011075L;
 
 		@Override
 		public Component getTableCellRendererComponent(JTable table,
 				Object value, boolean isSelected, boolean hasFocus, int row,
 				int column) {
 
 			Component c = super.getTableCellRendererComponent(table,
 					((CCMTableModel2.DownloadLink) value).text, isSelected,
 					hasFocus, row, column);
 
 			setToolTipText(((CCMTableModel2.DownloadLink) value).url);
 			return c;
 		}
 	}
 
 }
