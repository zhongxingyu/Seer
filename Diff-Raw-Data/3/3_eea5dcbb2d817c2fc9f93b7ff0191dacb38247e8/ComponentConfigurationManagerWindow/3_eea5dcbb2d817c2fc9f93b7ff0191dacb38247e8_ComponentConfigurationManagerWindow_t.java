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
 import java.util.HashMap;
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
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableRowSorter;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.builtin.projects.ProjectPanel;
 import org.geworkbench.engine.config.rules.GeawConfigObject;
 import org.geworkbench.engine.management.ComponentRegistry;
 import org.geworkbench.events.ComponentConfigurationManagerUpdateEvent;
 import org.geworkbench.util.BrowserLauncher;
 import org.geworkbench.util.Util;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * This is the main menu for the Component Configuration Manager.
  * 
  * @author tg2321
  * @version $Id$
  */
 public class ComponentConfigurationManagerWindow {
 
 	private static Log log = LogFactory.getLog(ComponentConfigurationManagerWindow.class);
 	
 	private CCMTableModel ccmTableModel;
 	protected ComponentConfigurationManager manager = null;	
 	
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
 
 	private static int launchedRow = -1;
 	private static int launchedColumn = -1;
 	
 	private final static String DISPLAY_FILTER_ALL = "All";
 	private final static String DISPLAY_ONLY_LOADED = "Only loaded";
 	private final static String DISPLAY_ONLY_UNLOADED = "Only unloaded";
 	
 	private final static String SHOW_BY_TYPE_ALL = "All";
 	private final static String SHOW_BY_TYPE_OTHERS = "Others";
 	
 	private ArrayList<Boolean> originalChoices = null;
 
 	private static ComponentConfigurationManagerWindow ccmWindow = null;
 	/**
 	 * Constructor
 	 * Provides a call-back to the {@link ComponentConfigurationManagerMenu}.
 	 * 
 	 * @param ComponentConfigurationManagerMenu
 	 */
 	private ComponentConfigurationManagerWindow() {
 
 		manager = ComponentConfigurationManager.getInstance();
 		initComponents();
 	}
 
 	/**
 	 * Load method
 	 */
 	public static void load(ComponentConfigurationManagerMenu menu){
 		if(ccmWindow == null){
 			ccmWindow = new ComponentConfigurationManagerWindow();
 		}
 		ccmWindow.frame.setExtendedState(Frame.NORMAL);
 		ccmWindow.frame.setVisible(true);
 	}
 	
 	private TableRowSorter<CCMTableModel> sorter = null;
 	
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
 		String[] showByTypeChoices = new String[PluginComponent.categoryMap.size()+2];
 		showByTypeChoices[0] = SHOW_BY_TYPE_ALL;
 		int index = 1;
 		for(String s: PluginComponent.categoryMap.keySet()){
 			showByTypeChoices[index] =  s.substring(0, 1).toUpperCase()+s.substring(1);
 			index++;
 		};
 		showByTypeChoices[index] = SHOW_BY_TYPE_OTHERS; 
 		showByTypeComboBox = new JComboBox(showByTypeChoices);
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
 						ccmTableModel = new CCMTableModel(manager);
 						setOriginalChoices();
 						table = new JTable(ccmTableModel);
 						sorter = new TableRowSorter<CCMTableModel>(ccmTableModel);
 						table.setRowSorter(sorter);
 
 					    table.setDefaultRenderer(Object.class, new CellRenderer());
 					    table.setDefaultRenderer(CCMTableModel.ImageLink.class, new ImageLinkRenderer());
 					    table.setDefaultRenderer(CCMTableModel.HyperLink.class, new HyperLinkRenderer());
 					    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 
 						ListSelectionModel cellSM = table.getSelectionModel();
 						cellSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 						cellSM.addListSelectionListener(new ListSelectionListener() {
 						    public void valueChanged(ListSelectionEvent e) {
 						    	boolean adjusting = e.getValueIsAdjusting();
 						    	if (adjusting){
 							    	return;
 						    	}
 						        int selectedRow = table.getSelectedRow();
 						        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
 						        if (lsm.isSelectionEmpty()) {
 									textPane.setText(" ");
 						        } else {
 						            String description = (String)ccmTableModel.getValueAt(table.convertRowIndexToModel(selectedRow), CCMTableModel.DESCRIPTION_INDEX);
 						        	textPane.setText(description);
 						        	
 						            if (textPane.getCaretPosition() > 1){
 						            	textPane.setCaretPosition(1);        	
 						            }
 						        }
 
						        if(table.getSelectedRow()>=0)
						        	launchBrowser();
 							}
 						});
 						
 						TableColumn column = table.getColumnModel().getColumn(CCMTableModel.SELECTION_INDEX);
 						column.setMaxWidth(50);
 						column = table.getColumnModel().getColumn(CCMTableModel.VERSION_INDEX);
 						column.setMaxWidth(60);
 						column = table.getColumnModel().getColumn(CCMTableModel.TUTORIAL_URL_INDEX);
 						column.setMaxWidth(70);
 						column = table.getColumnModel().getColumn(CCMTableModel.TOOL_URL_INDEX);
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
 															"default,200dlu, " + // Apply
 															"default,  4dlu, " + // Reset
 															"default,  4dlu, " + // Cancel
 															"default "           // Close
 															,
 															"center:25dlu"));
 					
 					viewLicenseButton.setText("View License");
 					bottompanel.add(viewLicenseButton, cc.xy(2, 1));
 
 					//---- applyButton ----
 					applyButton.setText("Apply");
 					bottompanel.add(applyButton, cc.xy(6, 1));
 
 					//---- resetButton ----
 					resetButton.setText("Reset");
 					bottompanel.add(resetButton, cc.xy(8, 1));
 
 					//---- closeButton ----
 					closeButton.setText("Close");
 					bottompanel.add(closeButton, cc.xy(10, 1));
 					
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
 	
 	final private RowFilter<CCMTableModel, Integer> hiddenFilter = new RowFilter<CCMTableModel, Integer>() {
 		@Override
 		public boolean include(
 				Entry<? extends CCMTableModel, ? extends Integer> entry) {
 
 			CCMTableModel model = (CCMTableModel) entry.getModel();
 
 			Boolean hidden = (Boolean) model.getModelValueAt(entry
 					.getIdentifier(), CCMTableModel.HIDDEN_INDEX);
 			if (hidden)
 				return false;
 			else
 				return true;
 		}
 	};
 
 	final private RowFilter<CCMTableModel, Integer> loadFilter = new RowFilter<CCMTableModel, Integer>() {
 		@Override
 		public boolean include(
 				Entry<? extends CCMTableModel, ? extends Integer> entry) {
 
 			CCMTableModel model = (CCMTableModel) entry.getModel();
 
 			String loadedFilterValue = model.getLoadedFilterValue();
 			if(loadedFilterValue==null || loadedFilterValue.equals(ComponentConfigurationManagerWindow.DISPLAY_FILTER_ALL))
 				return true;
 			
 			boolean loaded = componentLoaded(entry.getIdentifier());
 			if (loaded && loadedFilterValue.equals(ComponentConfigurationManagerWindow.DISPLAY_ONLY_LOADED)
 					||
 					!loaded && loadedFilterValue.equals(ComponentConfigurationManagerWindow.DISPLAY_ONLY_UNLOADED))
 				return true;
 
 			return false;
 		}
 	};
 
 	/**
 	 * type filter: analysis or visualization
 	 */
 	final private RowFilter<CCMTableModel, Integer> typeFilter = new RowFilter<CCMTableModel, Integer>() {
 		@Override
 		public boolean include(
 				Entry<? extends CCMTableModel, ? extends Integer> entry) {
 
 			CCMTableModel model = (CCMTableModel) entry.getModel();
 			String typeFilterValue = model.getTypeFilterValue();
 			if (typeFilterValue == null
 					|| typeFilterValue
 							.equals(ComponentConfigurationManagerWindow.SHOW_BY_TYPE_ALL))
 				return true;
 
 			PluginComponent.Category category = (PluginComponent.Category) model.getModelValueAt(entry
 					.getIdentifier(), CCMTableModel.CATEGORY_INDEX);
 			if (category == PluginComponent.categoryMap.get(typeFilterValue.toLowerCase()))
 				return true;
 			
 			if (category == null
 					&& typeFilterValue
 							.equals(ComponentConfigurationManagerWindow.SHOW_BY_TYPE_OTHERS))
 				return true;
 
 			return false;
 		}
 	};
 
 	/**
 	 * type filter: analysis or visualization
 	 */
 	final private RowFilter<CCMTableModel, Integer> keywordSearchFilter = new RowFilter<CCMTableModel, Integer>() {
 		@Override
 		public boolean include(
 				Entry<? extends CCMTableModel, ? extends Integer> entry) {
 
 			CCMTableModel model = (CCMTableModel) entry.getModel();
 			String keywordFilterValue = model.getKeywordFilterValue();
 			if (keywordFilterValue == null
 					|| keywordFilterValue.equals("") ||
 					keywordFilterValue.equals("text"))
 				return true;
 
 			keywordFilterValue = keywordFilterValue.toLowerCase().trim();
 			
 			for(int j=CCMTableModel.FIRST_STRING_COLUMN; j<CCMTableModel.AUTHOR_INDEX; j++ ) {
 				String fieldValue = ((String) model.getModelValueAt(entry
 						.getIdentifier(), j)).toLowerCase();
 				if(fieldValue.contains(keywordFilterValue))
 					return true;
 			}
 
 			return false;
 		}
 	};
 
 	final private List<RowFilter<CCMTableModel, Integer>> filters = new ArrayList<RowFilter<CCMTableModel, Integer>>();
 	{
 		filters.add(hiddenFilter);
 		filters.add(loadFilter);
 		filters.add(typeFilter);
 		filters.add(keywordSearchFilter);
 	}
 	final private RowFilter<CCMTableModel, Integer> combinedFilter = RowFilter
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
    		Object obj = ccmTableModel.getModelValueAt(modelRow, modeColumn);
    		if(obj==null) return;
    			
    		if(obj instanceof CCMTableModel.HyperLink)
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
     		license = (String) ccmTableModel.getModelValueAt(modelRow, CCMTableModel.LICENSE_INDEX);
     		componentName = (String) ccmTableModel.getModelValueAt(modelRow, CCMTableModel.NAME_INDEX);
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
 	@SuppressWarnings("unchecked")
 	private void applyCcmSelections_actionPerformed(ActionEvent e) {
 		Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
 		frame.setCursor(hourglassCursor);
 		
 		for (int i = 0; i < ccmTableModel.getModelRowCount(); i++) {
 
 			boolean choice = ((Boolean) ccmTableModel.getModelValueAt(i,
 					CCMTableModel.SELECTION_INDEX)).booleanValue();
 
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
 				propFileName = filename
 						.replace(".cwb.xml", ".ccmproperties");
 			} else {
 				log.error("File name is "+filename+" when .cwb.xml file is expected");
 				continue;
 			}
 			String sChoice = (new Boolean(choice)).toString();
 			
 			ComponentConfigurationManager.writeProperty(resource, propFileName, "on-off", sChoice);
 			
 			if (choice) {
 				manager.loadComponent(file);
 				continue;
 			}
 
 			/* Remove Component */
 			manager.removeComponent(resource, file.getAbsolutePath());
 
 			ccmTableModel.fireTableDataChanged();
             if (textPane.getCaretPosition() > 1){
             	textPane.setCaretPosition(1);        	
             }
 		}
 		GeawConfigObject.recreateHelpSets();
 
 		ComponentRegistry componentRegistry = ComponentRegistry.getRegistry();
 		HashMap<Class, List<Class>> acceptors = componentRegistry
 				.getAcceptorsHashMap();
 		
 		ComponentConfigurationManagerUpdateEvent ccmEvent = new ComponentConfigurationManagerUpdateEvent(
 				acceptors);
 		
 		// TODO this has no need to use publish/receive mechanism and should be refactored.
 		ProjectPanel.getInstance().receive(ccmEvent, null);
 
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
 			ccmTableModel.selectRow(originalChoice, i, CCMTableModel.NO_VALIDATION);
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
 
 	/**
 	 * Save the original selections for use with resetCcmSelections action
 	 * 
 	 * @param void
 	 * @return void
 	 */
 	private void setOriginalChoices() {
 		originalChoices = new ArrayList<Boolean>(ccmTableModel.getModelRowCount());
 		for (int i = 0; i < ccmTableModel.getModelRowCount(); i++) {
 			originalChoices.add(i, (Boolean) ccmTableModel.getModelValueAt(i, CCMTableModel.SELECTION_INDEX));
 		}
 	}
 
 	private boolean componentLoaded(int modelRow){
 		boolean loaded = false;
 		
 		String pluginClazzName = (String)ccmTableModel.getModelValueAt(modelRow, CCMTableModel.CLASS_INDEX);
 		
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
 			
 			int modelRow = table.convertRowIndexToModel(row);
 			Boolean selected = (Boolean) (table.getModel().getValueAt(modelRow, CCMTableModel.SELECTION_INDEX));
 
 			Component defaultComponent = defaultRenderer
 					.getTableCellRendererComponent(table, value, isSelected,
 							hasFocus, row, column);
 			Component c = super.getTableCellRendererComponent(table, value,
 					isSelected, hasFocus, row, column);
 //			if (selected) {
 //				c.setBackground(defaultComponent.getBackground().darker());
 //			} else {
 //				c.setBackground(defaultComponent.getBackground());
 //			}
 			return c;
 		}
 	    private static TableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
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
 			label.setToolTipText(((CCMTableModel.ImageLink) value).url);
 			
 			CCMTableModel.LinkIcon linkIcon = ((CCMTableModel.ImageLink) value).image;
 			if(linkIcon==CCMTableModel.LinkIcon.COLORED)
 				label.setIcon(colored);
 			else if (linkIcon==CCMTableModel.LinkIcon.GRAYED)
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
 					((CCMTableModel.HyperLink) value).text, isSelected,
 					hasFocus, row, column);
 
 			if (!isSelected)
 				c.setForeground(Color.blue);
 
 			Font font = c.getFont();
 			Map<TextAttribute, Object> attributes = new Hashtable<TextAttribute, Object>();
 			attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
 			c.setFont(font.deriveFont(attributes));
 			
 			setToolTipText(((CCMTableModel.HyperLink) value).url);
 			return c;
 		}
 	}
 
 }
