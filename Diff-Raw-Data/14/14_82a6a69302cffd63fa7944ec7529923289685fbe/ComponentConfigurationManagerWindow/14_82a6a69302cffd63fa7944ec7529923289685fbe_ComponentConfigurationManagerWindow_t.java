 package org.geworkbench.engine.ccm;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Frame;
 import java.awt.ItemSelectable;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.AbstractButton;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
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
 import javax.swing.UIManager;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableColumnModel;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 
 import org.apache.commons.lang.StringUtils;
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
 * @version $Id: ComponentConfigurationManagerWindow.java,v 1.11 2009-08-04 14:56:06 tgarben Exp $
  */
 public class ComponentConfigurationManagerWindow {
 
 	private static final long serialVersionUID = 1L;
 
 	private ComponentConfigurationManagerMenu menu = null;
 	
 /*	private CCMTableModel ccmTableModel;  Use this Model after Java 1.6 conversion */ 
 	private CCMTableModelFilterView ccmTableModel;
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
 	private JButton viewDocumentationButton = new JButton("View Documentation");
 	private JButton viewExternalSiteButton = new JButton("View External Site");
 	private JButton applyButton = new JButton("Apply");
 	private JButton resetButton = new JButton("Reset");
 	private JButton closeButton = new JButton("Close");
 
 	private static int launchedRow = 999;
 	private static int launchedColumn = 999;
 	
 	public final static String DISPLAY_FILTER_ALL = "All";
 	public final static String DISPLAY_ONLY_LOADED = "Only loaded";
 	public final static String DISPLAY_ONLY_UNLOADED = "Only unloaded";
 	
 	public final static String SHOW_BY_TYPE_ALL = "All";
 	public final static String SHOW_BY_TYPE_PARSER = "Parsers";
 	public final static String SHOW_BY_TYPE_ANALYSIS = "Analysis plugins";
 	public final static String SHOW_BY_TYPE_VISUALIZER = "Visualizers";
 	
 	
 	private ArrayList<Boolean> originalChoices = null;
 
 	private static ComponentConfigurationManagerWindow ccmWindow = null;
 	/**
 	 * Constructor
 	 * Provides a call-back to the {@link ComponentConfigurationManagerMenu}.
 	 * 
 	 * @param ComponentConfigurationManagerMenu
 	 */
 	private ComponentConfigurationManagerWindow(
 			ComponentConfigurationManagerMenu menu) {
 
 		this.menu = menu;
 		manager = new ComponentConfigurationManager();
 		initComponents();
 	}
 
 	/**
 	 * Default Constructor
 	 */
 	private ComponentConfigurationManagerWindow() {
 	}
 	
 	
 	/**
 	 * Load method
 	 */
 	public static void load(ComponentConfigurationManagerMenu menu){
 		if(ccmWindow == null){
 			ccmWindow = new ComponentConfigurationManagerWindow(menu);
 		}
 		ccmWindow.frame.setExtendedState(Frame.NORMAL);
 		ccmWindow.frame.setVisible(true);
 	}
 	
 	
 	
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
 		String[] showByTypeChoices = { SHOW_BY_TYPE_ALL, SHOW_BY_TYPE_PARSER, SHOW_BY_TYPE_ANALYSIS, SHOW_BY_TYPE_VISUALIZER };
 		showByTypeComboBox = new JComboBox(showByTypeChoices);
 		keywordSearchLabel = new JLabel("Keyword search:");
 		keywordSearchField = new JTextField("Enter Text");
 		splitPane = new JSplitPane();
 		scrollPaneForTextPane = new JScrollPane();
 		textPane = new JTextPane();
 		bottompanel = new JPanel();
 		CellConstraints cc = new CellConstraints();
 		
 	     frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				ccmWindow = null;
 			}
 		});
 	     
 	     viewLicenseButton.addActionListener(new ActionListener(){
 	    	public void actionPerformed(ActionEvent e) {
 				viewLicense_actionPerformed(e);
 	    	}
 	     } );
 
 	     viewDocumentationButton.addActionListener(new ActionListener(){
 		    	public void actionPerformed(ActionEvent e) {
 					viewDocumentation_actionPerformed(e);
 		    	}
 		     } );
 
 	     viewExternalSiteButton.addActionListener(new ActionListener(){
 		    	public void actionPerformed(ActionEvent e) {
 					viewExternalSite_actionPerformed(e);
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
 					          ccmTableModel.updateView();
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
 					          ccmTableModel.updateView();
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
 								ccmTableModel.updateView();
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
 						ccmTableModel = new CCMTableModelFilterView(manager);
 						setOriginalChoices();
 						table = new JTable(ccmTableModel) {
 							private static final long serialVersionUID = 5118325076548663090L;
 
 							public Component prepareRenderer( TableCellRenderer renderer, int viewRow, int col) {
 
 								Component comp = super.prepareRenderer(renderer, viewRow, col);
 								
 								int modelRow =  ccmTableModel.getModelRow(viewRow);
 								boolean isPluginLoaded = ccmTableModel.componentLoaded(modelRow);
 								
 								if( isPluginLoaded ){
 									comp.setBackground( new Color(230,230,255));
 								}else{
 									comp.setBackground(Color.white);
 								}
 								
 								if (isCellSelected(viewRow, col)){
 									comp.setBackground( new Color(200,200,255));
 								}
 								
 								return comp;
 							}
 						};
 						
 						TableCellRenderer defaultRenderer;
 						defaultRenderer = table.getDefaultRenderer(JButton.class);
 					    table.setDefaultRenderer(JButton.class, new JTableButtonRenderer(defaultRenderer));
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
 						            String description = (String)ccmTableModel.getValueAt(selectedRow[0], CCMTableModel.DESCRIPTION_INDEX);
 						        	textPane.setText(description);
 						        	
 						            if (textPane.getCaretPosition() > 1){
 						            	textPane.setCaretPosition(1);        	
 						            }
 						        }
 
 						        launchBrowser();
 							}
 						});
 						
 						ListSelectionModel columnSM = table.getColumnModel().getSelectionModel();
 						columnSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 						columnSM.addListSelectionListener(new ListSelectionListener() {
 						    public void valueChanged(ListSelectionEvent e) {
 						    	boolean adjusting = e.getValueIsAdjusting();
 						    	if (adjusting){
 						    		return;
 						    	}
 
 						    	launchBrowser();
 						    }
 						});
 						
 						TableColumn column = null;
 						for (int i = 0; i < CCMTableModel.LAST_VISIBLE_COLUMN; i++) {
 							column = table.getColumnModel().getColumn(i);
 							column.setResizable(false);
 
 							switch (i) {
 							case CCMTableModel.SELECTION_INDEX:
 								column.setMaxWidth(100);
 								column.setMinWidth(100);
 								break;
 							case CCMTableModel.NAME_INDEX:
 								column.setPreferredWidth(300);
 								column.setResizable(true);
 								break;
 							case CCMTableModel.VERSION_INDEX:
 								column.setMaxWidth(50);
 								column.setMinWidth(50);
 								break;
 							case CCMTableModel.AUTHOR_INDEX:
 								column.setPreferredWidth(300);
 								column.setResizable(true);
 								break;
 							case CCMTableModel.AUTHOR_URL_INDEX:
 								break;
 							case CCMTableModel.TOOL_URL_INDEX:
 								break;
 							default:
 							}
 						}
 						
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
 					bottompanel.setLayout(new FormLayout(   "4dlu,"            + 
 															"default,  4dlu, " + // view License
 															"default,  4dlu, " + // View documentation
 															"default, 75dlu, " + // View external site
 															"default,  4dlu, " + // Accept
 															"default,  4dlu, " + // Apply
 															"default,  4dlu, " + // Reset
 															"default,  4dlu, " + // Cancel
 															"default "           // Close
 															,
 															"center:25dlu"));
 					
 					viewLicenseButton.setText("View License");
 					bottompanel.add(viewLicenseButton, cc.xy(2, 1));
 					viewDocumentationButton.setText("View Documentation");
 					bottompanel.add(viewDocumentationButton, cc.xy(4, 1));
 					viewExternalSiteButton.setText("View External Site");
 					bottompanel.add(viewExternalSiteButton, cc.xy(6, 1));
 					
 //					//---- acceptButton ----
 //					acceptButton.setText("Accept");
 //					bottompanel.add(acceptButton, cc.xy(10, 1));
 
 					//---- applyButton ----
 					applyButton.setText("Apply");
 					bottompanel.add(applyButton, cc.xy(10, 1));
 
 					//---- resetButton ----
 					resetButton.setText("Reset");
 					bottompanel.add(resetButton, cc.xy(12, 1));
 
 					//---- closeButton ----
 					closeButton.setText("Close");
 					bottompanel.add(closeButton, cc.xy(14, 1));
 					
 				} //======== bottompanel ========.
 				frameContentPane.add(bottompanel, BorderLayout.SOUTH);
 			} //======== outerPanel ========
 			frame.pack();
 			frame.setLocationRelativeTo(frame.getOwner());
 		} // ============ frame ============
 
 		 TableColumn tc = table.getColumnModel().getColumn(0);
 		 tc.setHeaderRenderer(new CheckBoxHeader(new CheckBoxHeaderListener()));  
 
 		topPanel.setVisible(true);
 		splitPane.setVisible(true);
 		scrollPaneForTable.setVisible(true);
 		table.setVisible(true);
 		scrollPaneForTextPane.setVisible(true);
 		textPane.setVisible(true);
 		bottompanel.setVisible(true);
 		ccmTableModel.updateView();
 		frame.setVisible(true);
 		splitPane.setDividerLocation(.7d);
 	}
 
 	/*
 	 * launchBrowser for URLs in CCM GUI 
 	 */
 	private void launchBrowser(){
         int[] selectedRow = table.getSelectedRows();
         int[] selectedColumn = table.getSelectedColumns();
         
         if (   selectedRow != null && selectedRow.length > 0 && selectedRow[0] >= 0 &&
 	       	   selectedColumn != null && selectedColumn.length > 0 && selectedColumn[0] >= 0 &&  
 	       	  (selectedColumn[0] == CCMTableModel.AUTHOR_URL_INDEX
 			|| selectedColumn[0] == CCMTableModel.TOOL_URL_INDEX)) {
 	        	
     		int modelRow = ccmTableModel.getModelRow(selectedRow[0]);
     		if (launchedRow == modelRow && launchedColumn == selectedColumn[0]){
     			return;
     		}
     		launchedRow = modelRow;
     		launchedColumn = selectedColumn[0];
     		
     		String url = "";
    			JButton button = (JButton) ccmTableModel.getModelValueAt(modelRow, selectedColumn[0]);
    			url = button.getToolTipText();
     		
 			try {
 				BrowserLauncher.openURL(url);
 			} catch (IOException e1) {
 				// TODO Auto-generated catch
 				// block
 				e1.printStackTrace();
 			}
 		}
 	}
 
 	
 	/*
 	 * launchBrowser for URLs in CCM GUI 
 	 */
 	private void viewExternalSite(){
         int[] selectedRow = table.getSelectedRows();
         
         if (   selectedRow != null && selectedRow.length > 0 && selectedRow[0] >= 0 ) {
 
         	
     		int modelRow = ccmTableModel.getModelRow(selectedRow[0]);
     		JButton jButton = (JButton) ccmTableModel.getModelValueAt(modelRow, CCMTableModel.TOOL_URL_INDEX);
     		String url = jButton.getToolTipText();
     		
 			try {
 				BrowserLauncher.openURL(url);
 			} catch (IOException e1) {
 				// TODO Auto-generated catch
 				// block
 				e1.printStackTrace();
 			}
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
 
     		int modelRow = ccmTableModel.getModelRow(selectedRow[0]);
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
 	 * 
 	 * @param e
 	 */
 	private void viewDocumentation_actionPerformed(ActionEvent e) {
 
         int[] selectedRow = table.getSelectedRows();
 
         String document = "Select a component in order to view its documents.";
         String componentName = null;
         if (   selectedRow != null && selectedRow.length > 0 && selectedRow[0] >= 0) {
 
     		int modelRow = ccmTableModel.getModelRow(selectedRow[0]);
     		document = (String) ccmTableModel.getModelValueAt(modelRow, CCMTableModel.DOCUMENTATION_INDEX);
     		componentName = (String) ccmTableModel.getModelValueAt(modelRow, CCMTableModel.NAME_INDEX);
         }
         
         JDialog documentDialog = new JDialog();
 		JTextPane documentTextPane = new JTextPane();
 		documentTextPane.setEditable(false);
 		documentTextPane.setText(document);
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setViewportView(documentTextPane);
 		documentDialog.setTitle(componentName + " Documents");
 		documentDialog.setContentPane(scrollPane);
 		documentDialog.setSize(400,400);
 		documentDialog.setLocationRelativeTo(frame);
 		documentDialog.setVisible(true);
 	}
 
 	
 	/**
 	 * 
 	 * @param e
 	 */
 	private void viewExternalSite_actionPerformed(ActionEvent e){
 		viewExternalSite();
 	}
 	
 	
 	
 //	/**
 //	 * Persist users component selections Add newly selected components Remove
 //	 * newly unselected components Close CCM Window
 //	 * 
 //	 * @param ActionEvent
 //	 * @return void
 //	 */
 //	private void acceptCcmSelections_actionPerformed(ActionEvent e) {
 //		applyCcmSelections_actionPerformed(e);
 //
 //		frame.dispose();
 //		ccmWindow = null;
 //	}
 
 	/**
 	 * Persist users component selections 
 	 * Add newly selected components 
 	 * Remove newly unselected components Leave CCM Window open
 	 * 
 	 * @param ActionEvent
 	 * @return void
 	 */
 	public void applyCcmSelections_actionPerformed(ActionEvent e) {
 
 		for (int i = 0; i < ccmTableModel.getModelRowCount(); i++) {
 
 			boolean choice = ((Boolean) ccmTableModel.getModelValueAt(i,
 					CCMTableModel.SELECTION_INDEX)).booleanValue();
 
 			boolean originalChoice = this.originalChoices.get(i).booleanValue();
 			/* No change in selection */
 			if (choice == originalChoice) {
 				continue;
 			}
 
 			String resource = ((String) ccmTableModel.getModelValueAt(i,
 					CCMTableModel.FOLDER_INDEX));
 
 			String ccmFileName = ((String) ccmTableModel.getModelValueAt(i,
 					CCMTableModel.CCM_FILE_NAME_INDEX));
 
 			String propFileName = ccmFileName.replace(".ccm.xml", ".ccmproperties");
 			String sChoice = (new Boolean(choice)).toString();
 			
 			ComponentConfigurationManager.writeProperty(resource, propFileName, "on-off", sChoice);
 			
 			if (choice) {
 				manager.loadComponent(resource, ccmFileName);
 				continue;
 			}
 
 			/* Remove Component */
 			manager.removeComponent(resource, ccmFileName);
 			ccmTableModel.fireTableDataChanged();
             if (textPane.getCaretPosition() > 1){
             	textPane.setCaretPosition(1);        	
             }
 		}
 
 		ComponentRegistry componentRegistry = ComponentRegistry.getRegistry();
 		HashMap<Class, List<Class>> acceptors = componentRegistry
 				.getAcceptorsHashMap();
 		
 		ComponentConfigurationManagerUpdateEvent ccmEvent = new ComponentConfigurationManagerUpdateEvent(
 				acceptors);
 		
 		publishComponentConfigurationManagerUpadateEvent(ccmEvent);
 
 		setOriginalChoices();
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
 			ccmTableModel.setModelValueAt(originalChoice, i, CCMTableModel.SELECTION_INDEX, CCMTableModel.NO_VALIDATION);
 		}
 
 	}
 
 	/*
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
 
 	/**
 	 * A call-back to the
 	 * {@link ComponentConfigurationManagerMenu#publishComponentConfigurationManagerUpadateEvent(ComponentConfigurationManagerUpdateEvent)}
 	 * 
 	 * @param ComponentConfigurationManagerUpdateEvent
 	 * @return ComponentConfigurationManagerUpdateEvent
 	 */
 	public ComponentConfigurationManagerUpdateEvent publishComponentConfigurationManagerUpadateEvent(
 			ComponentConfigurationManagerUpdateEvent event) {
 
 		return menu.publishComponentConfigurationManagerUpadateEvent(event);
 	}
 
 	/*
 	 * CCMTableModelFilterView
 	 */
 	class CCMTableModelFilterView extends CCMTableModel {
 		
 		private static final long serialVersionUID = -3149950381135283122L;
 
 		/* if true then display this row */
 		private Vector<Boolean> viewRows = new Vector<Boolean>();
 		
 		/*
 		 * Constructor
 		 */
 		public CCMTableModelFilterView(ComponentConfigurationManager manager) {
 			super(manager);
 		
 			for (int i=0; i<rows.size(); i++){
 				viewRows.add(new Boolean(true));
 			}
 		}
 
 		/**
 		 * 
 		 * @param viewRow
 		 * @return 
 		 */
 		private int getModelRow(int viewRow){
 			
 			int modelRow = -1;
 			int viewIndex = -1;
 			for (int i=0; i< viewRows.size(); i++){
 				if (viewRows.get(i).booleanValue()){
 					viewIndex++;
 				}
 				if (viewIndex==viewRow){
 					modelRow=i;
 					break;
 				}
 			}
 			
 			return modelRow;			
 		}
 
 		/**
 		 * @param void
 		 * @return 
 		 */
 		private void updateView() {
 
 			for (int i = 0; i < viewRows.size(); i++) {
 				if (loadedFilter(i) || showByTypeFilter(i)
 						|| keyWordSearchFilter(i) || hiddenFilter(i)) {
 					viewRows.set(i, Boolean.FALSE);
 				} else {
 					viewRows.set(i, Boolean.TRUE);
 				}
 			}
 		}
 		
 		/**
 		 * 
 		 * @param modelRow
 		 * @return
 		 */
 		private boolean componentLoaded(int modelRow){
 			boolean loaded = false;
 			
 			String pluginClazzName = (String)getModelValueAt(modelRow, CCMTableModel.CLASS_INDEX);
 			
 			ComponentRegistry componentRegistry = ComponentRegistry.getRegistry();
 			List<Object> components = componentRegistry.getComponentsList();
 			for (Object proxiedComponent : components) {
 				// FIXME use a "deproxy" (see cglib or HibernateProxy)
 				Class clazz = proxiedComponent.getClass();
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
 		 * 
 		 * @param modelRow
 		 * @return
 		 */
 		private boolean loadedFilter(int modelRow){
 			
 			String loadedFilterValue = getLoadedFilterValue();
 			if (loadedFilterValue == null || loadedFilterValue.equals(ComponentConfigurationManagerWindow.DISPLAY_FILTER_ALL)){
 				return false;
 			}
 			
 			boolean loaded = componentLoaded(modelRow);
 			if (	loaded && loadedFilterValue.equals(ComponentConfigurationManagerWindow.DISPLAY_ONLY_LOADED)
 				|| !loaded && loadedFilterValue.equals(ComponentConfigurationManagerWindow.DISPLAY_ONLY_UNLOADED)){
 				return false;
 			}
 			
 			return true;
 		}
 
 		/**
 		 * 
 		 * @param modelRow
 		 * @return
 		 */
 		private boolean hiddenFilter(int modelRow){
 			Boolean isHidden = (Boolean)getModelValueAt(modelRow, CCMTableModel.HIDDEN_INDEX);
 
 			if (isHidden){
 				return true;
 			}
 			
 			return false;
 		}
 		
 		/**
 		 * 
 		 * @param modelRow
 		 * @return
 		 */
 		private boolean showByTypeFilter(int modelRow){
 
 			String typeFilterValue = getTypeFilterValue();
 			if ( typeFilterValue == null || typeFilterValue.equals(ComponentConfigurationManagerWindow.SHOW_BY_TYPE_ALL)){
 				return false;
 			}
 			
 			Boolean isParser = (Boolean)getModelValueAt(modelRow, CCMTableModel.PARSER_INDEX);
 			Boolean isAnalysis = (Boolean)getModelValueAt(modelRow, CCMTableModel.ANALYSIS_INDEX);
 			Boolean isVisualizer = (Boolean)getModelValueAt(modelRow, CCMTableModel.VISUALIZER_INDEX);
 			
 			if (isParser && typeFilterValue.equals(ComponentConfigurationManagerWindow.SHOW_BY_TYPE_PARSER)){
 				return false;
 			}
 			
 			if (isAnalysis && typeFilterValue.equals(ComponentConfigurationManagerWindow.SHOW_BY_TYPE_ANALYSIS)){
 				return false;
 			}
 			
 			if (isVisualizer && typeFilterValue.equals(ComponentConfigurationManagerWindow.SHOW_BY_TYPE_VISUALIZER)){
 				return false;
 			}
 			
 			
 			return true;
 		}
 
 		/**
 		 * 
 		 * @param modelRow
 		 * @return
 		 */
 		private boolean keyWordSearchFilter(int modelRow){
 			String keywordFilterValue = getKeywordFilterValue();
 			
 			if (keywordFilterValue == null || keywordFilterValue.equals("") || keywordFilterValue.equals("text")){
 				return false;
 			}
 			
 			keywordFilterValue = keywordFilterValue.toLowerCase().trim(); 
 			
 			for (int j=FIRST_STRING_COLUMN; j<AUTHOR_INDEX; j++ ){
 				String fieldValue = ((String)getModelValueAt(modelRow, j)).toLowerCase();
 				
 				if (fieldValue.contains(keywordFilterValue)){
 					return false;
 				}
 			}
 			return true;
 		}
 
 		
 		/**
 		 * 
 		 */
 		public int getRowCount() {
 			
 			int viewCount = 0;
 			for (int i=0; i<viewRows.size();i++){
 				if (viewRows.get(i).booleanValue()){
 					viewCount++;
 				}
 			}
 
 			return viewCount;
 		}
 		
 		/**
 		 * 
 		 */
 		public Object getValueAt(int viewRow, int column) {
 			int modelRow = getModelRow(viewRow);
 			Object value = getModelValueAt(modelRow, column);
 
 			if (column == AUTHOR_URL_INDEX || column == TOOL_URL_INDEX){
 				return (JButton)value;
 			}
 			
 			return value;
 		}
 		
 		/**
 		 * 
 		 */
 		public void setValueAt(Object value, int viewRow, int column ) {
 			int modelRow = getModelRow(viewRow);
 			setModelValueAt(value, modelRow, column, true);	
 		}
 		
 		/**
 		 * 
 		 */
 		public void setValueAt(Object value, int viewRow, int column, boolean validation ) {
 			int modelRow = getModelRow(viewRow);
 			setModelValueAt(value, modelRow, column, validation);	
 		}
 	}
 	
 	/**
 	 * CCMTableModel
 	 * 
 	 */
 	class CCMTableModel extends AbstractTableModel {
 		
 		private static final long serialVersionUID = 1L;
 
 		public static final boolean NO_VALIDATION = false;
 		
 		public static final int SELECTION_INDEX = 0;
 		public static final int NAME_INDEX = 1;
 		public static final int VERSION_INDEX = 3;
 		public static final int AUTHOR_INDEX = 2;
 		public static final int AUTHOR_URL_INDEX = 4;
 		public static final int TOOL_URL_INDEX = 5;
 		public static final int CLASS_INDEX = 6;
 		public static final int DESCRIPTION_INDEX = 7;
 		public static final int FOLDER_INDEX = 8;
 		public static final int CCM_FILE_NAME_INDEX = 9;
 		public static final int LICENSE_INDEX = 10;
 		public static final int MUST_ACCEPT_INDEX = 11;
 		public static final int DOCUMENTATION_INDEX = 12;
 		public static final int REQUIRED_COMPONENT_INDEX = 13;
 		public static final int RELATED_COMPONENT_INDEX = 14;
 		public static final int PARSER_INDEX = 15;
 		public static final int ANALYSIS_INDEX = 16;
 		public static final int VISUALIZER_INDEX = 17;
 		public static final int LOAD_BY_DEFAULT_INDEX = 18;
 		public static final int HIDDEN_INDEX = 19;
 		public static final int LAST_VISIBLE_COLUMN = TOOL_URL_INDEX;
 		public static final int LAST_COLUMN = HIDDEN_INDEX;
 		public static final int FIRST_STRING_COLUMN = NAME_INDEX;
 		public static final int LAST_STRING_COLUMN = LICENSE_INDEX;
		private String[] columnNames = { "On/Off", "Name", "Author", "Version",
 				"Author URL", "Tool URL" };
 		protected Vector<TableRow> rows = new Vector<TableRow>();
 
 		ComponentConfigurationManager manager = null;
 
 		private String loadedFilterValue = null;
 		private String typeFilterValue = null;
 		private String keywordFilterValue = null;
 		
 		/*
 		 * Constructor
 		 */
 		public CCMTableModel(ComponentConfigurationManager manager) {
 			super();
 
 			this.manager = manager;
 			loadGuiModelFromCmmFiles();
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see javax.swing.table.TableModel#getColumnCount()
 		 */
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see javax.swing.table.TableModel#getRowCount()
 		 */
 		public int getRowCount() {
 			return getModelRowCount();
 		}
 
 		/**
 		 * 
 		 * @return
 		 */
 		final public int getModelRowCount(){
 			return rows.size();
 		}
 		
 		/*
 		 * (non-Javadoc)
 		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
 		 */
 		public String getColumnName(int col) {
 			return columnNames[col];
 		}
 
 		/**
 		 * (non-Javadoc)
 		 * 
 		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int) Without
 		 * this method, the check box column would default to a String
 		 */
 		@SuppressWarnings("unchecked")
 		public Class getColumnClass(int column) {
 
 			if (column == SELECTION_INDEX) {
 				return Boolean.class;
 			}
 
 			if(column == AUTHOR_URL_INDEX || column == TOOL_URL_INDEX){
 				return JButton.class;
 			}
 
 			return String.class;
 		}
 
 		/**
 		 * (non-Javadoc)
 		 * 
 		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
 		 */
 		public boolean isCellEditable(int row, int column) {
 			if (column == SELECTION_INDEX) {
 				return true;
 			}
 
 			return false;
 		}
 
 
 		/**
 		 * (non-Javadoc)
 		 * 
 		 * @see javax.swing.table.TableModel#getValueAt(int, int)
 		 */
 		public Object getValueAt(int row, int column) {
 			return getModelValueAt(row, column);
 		}
 
 		
 		/**
 		 * (non-Javadoc)
 		 * 
 		 * @see javax.swing.table.TableModel#getValueAt(int, int)
 		 */
 		final public Object getModelValueAt(int row, int column) {
 			TableRow record = (TableRow) rows.get(row);
 			switch (column) {
 			case SELECTION_INDEX:
 				return (Boolean) record.isSelected();
 			case NAME_INDEX:
 				return (String) record.getName();
 			case VERSION_INDEX:
 				return (String) record.getVersion();
 			case AUTHOR_INDEX:
 				return (String) record.getAuthor();
 			case AUTHOR_URL_INDEX:
 				return (JButton) record.getAuthorURL();
 			case TOOL_URL_INDEX:
 				return (JButton) record.getToolURL();
 			case CLASS_INDEX:
 				return (String) record.getClazz();
 			case DESCRIPTION_INDEX:
 				return (String) record.getDescription();
 			case FOLDER_INDEX:
 				return (String) record.getFolder();
 			case CCM_FILE_NAME_INDEX:
 				return (String) record.getFileName();
 			case LICENSE_INDEX:
 				return (String) record.getLicense();
 			case MUST_ACCEPT_INDEX:
 				return (Boolean) record.isMustAccept();
 			case DOCUMENTATION_INDEX:
 				return (String) record.getDocumentation();
 			case REQUIRED_COMPONENT_INDEX:
 				return (List<String>) record.getRequiredComponents();
 			case RELATED_COMPONENT_INDEX:
 				return (List<String>) record.getRelatedComponents();
 			case PARSER_INDEX:
 				return (Boolean)  record.isParser();
 			case ANALYSIS_INDEX:
 				return (Boolean) record.isAnalysis();
 			case VISUALIZER_INDEX:
 				return (Boolean)  record.isVisualizer();
 			case LOAD_BY_DEFAULT_INDEX:
 				return (Boolean)  record.isLoadByDefault();
 			case HIDDEN_INDEX:
 				return (Boolean)  record.isHidden();
 				
 			default:
 				return null;
 			}
 		}
 
 		/**
 		 * (non-Javadoc)
 		 * 
 		 * @see javax.swing.table.TableModel#getRowByClazz(String)
 		 */
 		final public int getModelRowByClazz(String clazz) {
 
 			for (int i = 0; i < rows.size(); i++) {
 				TableRow record = (TableRow) rows.get(i);
 				String rowClazz = record.getClazz();
 				if (clazz.equalsIgnoreCase(rowClazz)) {
 					return i;
 				}
 			}
 			return -1;
 		}
 
 		
 		
 		
 		
 		/**
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
 		 * int, int)
 		 */
 		public void setValueAt(Object value, int row, int column ) {
 			setModelValueAt(value, row, column, true);	
 		}
 		
 
 		/**
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
 		 * int, int, boolean)
 		 */
 		final public boolean setModelValueAt(Object value, int modelRow, int column, boolean validation) {
 			TableRow record = (TableRow) rows.get(modelRow);
 			switch (column) {
 			case SELECTION_INDEX:
 				record.setSelected((Boolean) value);
 
 				Boolean currentChoice = (Boolean) ccmTableModel.getModelValueAt(modelRow,
 						CCMTableModel.SELECTION_INDEX);
 
 				List<String> required = (List<String>) ccmTableModel
 						.getModelValueAt(modelRow, CCMTableModel.REQUIRED_COMPONENT_INDEX);
 				List<String> related = (List<String>) ccmTableModel.getModelValueAt(
 						modelRow, CCMTableModel.RELATED_COMPONENT_INDEX);
 				String folder = (String) ccmTableModel.getModelValueAt(
 						modelRow, CCMTableModel.FOLDER_INDEX);
 				String ccmFileName = (String) ccmTableModel.getModelValueAt(
 						modelRow, CCMTableModel.CCM_FILE_NAME_INDEX);
 
 				List<String> unselectedRequired = new ArrayList<String>();
 				for (int i=0; i< required.size(); i++){
 					String requiredClazz = required.get(i);
 					Integer requiredRow = getModelRowByClazz(requiredClazz); 
 					Boolean requiredSelected = (Boolean)getModelValueAt(requiredRow.intValue(), CCMTableModel.SELECTION_INDEX);
 					if(!requiredSelected){
 						unselectedRequired.add(requiredClazz);
 					}
 				}
 
 				if (currentChoice.booleanValue()) {
 					/* PLUGIN SELECTED */
 					String propFileName = ccmFileName.replace(".ccm.xml", ".ccmproperties");
 					String licenseAccepted = ComponentConfigurationManager.readProperty(folder, propFileName, "licenseAccepted");
 					Boolean boolRequired = record.isMustAccept();
 					boolean bRequired = boolRequired.booleanValue();
 					if ( (bRequired && (licenseAccepted == null) ) 
 						|| (bRequired && licenseAccepted != null && !licenseAccepted.equalsIgnoreCase("true")) ) {
 						String componentName = (String) ccmTableModel
 								.getModelValueAt(modelRow,
 										CCMTableModel.NAME_INDEX);
 
 						String title = "Terms Acceptance";
 							String message = "\nBy clicking \"I Accept\", you signify that you \nhave read and accept the terms of the license \nagreement for the \""
 								+ componentName + "\" application.\n\nTo view this license, exit out of this dialog and click \non the View License button below.\n\n";
 						Object[] options = { "I Accept", "No, thanks" };
 						int choice = JOptionPane.showOptionDialog(null,
 								message, title, JOptionPane.YES_NO_OPTION,
 								JOptionPane.PLAIN_MESSAGE, null, options,
 								options[0]);
 
 						if (choice != 0) {
 							ComponentConfigurationManager.writeProperty(folder, propFileName, "licenseAccepted", "false");
 							record.setSelected(new Boolean(false));
 							return false;
 						}
 						ComponentConfigurationManager.writeProperty(folder, propFileName, "licenseAccepted", "true");
 					}
 
 					if (!validation) {
 						ccmTableModel.fireTableDataChanged();
 						return true;
 					}
 
 					if (unselectedRequired.size() > 0 || related.size() > 0) {
 						ComponentConfigurationManagerLoadDialog ccmld = new ComponentConfigurationManagerLoadDialog(
 								frame, modelRow, unselectedRequired, related,
 								this);
 						ccmld.pack();
 						ccmld.setLocationRelativeTo(frame);
 						ccmld.setVisible(true);
 					}
 				} else {
 					/* PLUGIN UNSELECTED */
 					List<Integer> dependentPlugins = null;
 
 					String unselectedPluginClazz = "Plugin is missing a Class descriptor";
 					int unselectedRow = modelRow;
 					if (unselectedRow >= 0) {
 						unselectedPluginClazz = (String) ccmTableModel
 								.getModelValueAt(unselectedRow,
 										CCMTableModel.CLASS_INDEX);
 					}
 
 					dependentPlugins = new ArrayList<Integer>();
 					/*
 					 * Find Plugins that are dependent on this unselected
 					 * plugin.
 					 */
 					int rowCount = ccmTableModel.getModelRowCount();
 					for (int i = 0; i < rowCount; i++) {
 
 						/*
 						 * If the potentially dependent plugin is not selected,
 						 * don't bother to see if it is dependent
 						 */
 						Boolean selected = (Boolean) ccmTableModel.getModelValueAt(
 								i, CCMTableModel.SELECTION_INDEX);
 						if (!selected.booleanValue()) {
 							continue;
 						}
 
 						List<String> requiredList = (List<String>) ccmTableModel
 								.getModelValueAt(i, CCMTableModel.REQUIRED_COMPONENT_INDEX);
 						for (int j = 0; j < requiredList.size(); j++) {
 							String requiredClazz = requiredList.get(j);
 							if (unselectedPluginClazz
 									.equalsIgnoreCase(requiredClazz)) {
 								dependentPlugins.add(new Integer(i));
 								break;
 							}
 						}
 					}
 
 					if (!validation) {
 						ccmTableModel.fireTableDataChanged();
 						return true;
 					}
 					
 					/* If dependencies are found, then popup a dialog */
 					if (dependentPlugins.size() > 0) {
 						ComponentConfigurationManagerUnloadDialog ccmuld = new ComponentConfigurationManagerUnloadDialog(
 								frame, modelRow, dependentPlugins, this);
 						ccmuld.pack();
 						ccmuld.setLocationRelativeTo(frame);
 						ccmuld.setVisible(true);
 					}
 				}
 
 				break;
 			default:
 
 			}
 			fireTableCellUpdated(modelRow, column);
 			
 			return true;
 		}
 
 		// TODO choose weather to use loadGuiModelFromCmmFiles() or getPluginsFromCcmFile()
 		/**
 		 *  Load the GUI Model from.ccm.xml file
 		 */
 		private void loadGuiModelFromCmmFiles() {
 			String name = null;
 			String version = null;
 			String author = null;
 			String authorURL = null;
 			String toolURL = null;
 			String clazz = null;
 			String description = null;
 			String license = null;
 			String mustAccept = null;
 			String documentation = null;
 			List<String> requiredComponents = null;
 			List<String> relatedComponents = null;
 			String parser = null;
 			String analysis = null;
 			String visualizer = null;
 			String loadByDefault = null;
 			String hidden = null;
 			
 			ArrayList<String> allComponentFolders = this.manager.loadAllComponentFolders();
 			
 			
 			for (int index = 0; index < allComponentFolders.size(); index++) {
 				String folderName = allComponentFolders.get(index);
 				List<String> ccmFiles = this.manager.getFoldersToCcmFiles(folderName);
 
 				for (int j=0; j<ccmFiles.size(); j++){
 					String ccmFileName = ccmFiles.get(j);
 					
 					CcmComponent ccmComponent = manager.getPluginsFromCcmFile(folderName, ccmFileName );
 					
 					if (ccmComponent == null){
 						continue;
 					}
 					
 					List<Plugin> plugins = ccmComponent.getPlugins();
 					if (plugins == null) {
 						continue;
 					}
 	
 					Plugin plugin = (Plugin) plugins.get(0);
 					name = plugin.getName();
 	
 					version = ccmComponent.getVersion();
 					author = ccmComponent.getAuthor();
 					authorURL = ccmComponent.getAuthorURL();
 					toolURL = ccmComponent.getToolURL();
 					clazz = ccmComponent.getClazz();
 					description = ccmComponent.getDescription();
 					license = ccmComponent.getLicense();
 					mustAccept = ccmComponent.getMustAccept();
 					documentation = ccmComponent.getDocumentation();
 					requiredComponents = ccmComponent.getRequiredComponents();
 					relatedComponents = ccmComponent.getRelatedComponents();
 					parser = ccmComponent.getParser();
 					analysis = ccmComponent.getAnalysis();
 					visualizer = ccmComponent.getVisualizer();
 					loadByDefault = ccmComponent.getLoadByDefault();
 					hidden = ccmComponent.getHidden();
 
 					boolean bMustAccept = false;
 					boolean bParser = false;
 					boolean bAnalysis = false;
 					boolean bVisualizer = false;
 					boolean bLoadByDefault = false;
 					boolean bHidden = false;
 
 					if (mustAccept!=null && mustAccept.equalsIgnoreCase("true")){
 						bMustAccept = true;
 					}
 					if (parser!=null && parser.equalsIgnoreCase("true")){
 						bParser = true;
 					}
 					if (analysis!=null && analysis.equalsIgnoreCase("true")){
 						bAnalysis = true;
 					}
 					if (visualizer!=null && visualizer.equalsIgnoreCase("true")){
 						bVisualizer = true;
 					}
 					if (loadByDefault!=null && loadByDefault.equalsIgnoreCase("true")){
 						bLoadByDefault = true;
 					}
 					if (hidden!=null && hidden.equalsIgnoreCase("true")){
 						bHidden = true;
 					}
 					
 					TableRow tableRow = new TableRow(false, name, version,
 							author, authorURL, toolURL, clazz, description,
 							folderName, ccmFileName, license, bMustAccept, documentation, requiredComponents,
 							relatedComponents, bParser, bAnalysis, bVisualizer, bLoadByDefault, bHidden);
 
 					String propFileName = ccmFileName.replace(".ccm.xml", ".ccmproperties");
 	
 					String onOff = ComponentConfigurationManager.readProperty(folderName, propFileName, "on-off");
 
 					if (onOff != null && onOff.equals("true")){
 						tableRow.setSelected(true);
 					}
 					else{
 						tableRow.setSelected(false);
 					}
 						
 					rows.add(tableRow);
 				}// j
 			}// index
 			
 			sortRows(new TableNameComparator());
 			
 		}
 		
 		final private void sortRows(Comparator <TableRow> rowComparator){
 			 Collections.sort ( rows, rowComparator) ; 
 		}
 		
 		final public String getLoadedFilterValue() {
 			return loadedFilterValue;
 		}
 
 		final public void setLoadedFilterValue(String loadedFilterValue) {
 			this.loadedFilterValue = loadedFilterValue;
 		}
 
 		final public String getTypeFilterValue() {
 			return typeFilterValue;
 		}
 
 		final public void setTypeFilterValue(String typeFilterValue) {
 			this.typeFilterValue = typeFilterValue;
 		}
 
 		final public String getKeywordFilterValue() {
 			return keywordFilterValue;
 		}
 
 		final public void setKeywordFilterValue(String keywordFilterValue) {
 			this.keywordFilterValue = keywordFilterValue;
 		}
 		
 	}
 
 	/**
 	 * GUI row structure
 	 * 
 	 * @author tg2321
	 * @version $Id: ComponentConfigurationManagerWindow.java,v 1.11 2009-08-04 14:56:06 tgarben Exp $
 	 */
 	private class TableRow {
 		private boolean selected = false;
 		private String name = "";
 		private String version = "";
 		private String author = "";
 		private String authorURL = null;
 		private String toolURL = null;
 		private String clazz = "";
 		private String description = "";
 		private String folder = "";
 		private String fileName = "";
 		private String license = "";
 		private boolean mustAccept = false;
 		private String documentation = "";
 		private List<String> requiredComponents = null;
 		private List<String> relatedComponents = null;
 		private boolean parser = false;
 		private boolean analysis = false;
 		private boolean visualizer = false;
 		private boolean loadByDefault = false;
 		private boolean hidden = false;
 		
 		/**
 		 * Constructor
 		 * 
 		 * @param selected
 		 * @param name
 		 * @param version
 		 * @param author
 		 * @param authorURL
 		 * @param toolURL
 		 * @param clazz
 		 * @param description
 		 * @param folder
 		 * @param fileName
 		 * @param requiredComponents
 		 * @param relatedComponents
 		 */
 		public TableRow(boolean selected, String name, String version,
 				String author, String authorURL, String toolURL, String clazz,
 				String description, String folder, String fileName, String license, boolean mustAccept, String documentation,
 				List<String> requiredComponents, List<String> relatedComponents, boolean parser, boolean analysis, boolean visualizer, boolean loadByDefault, boolean hidden) {
 			super();
 			this.selected = selected;
 			this.name = name;
 			this.version = version;
 			this.author = author;
 			this.authorURL = authorURL;
 			this.toolURL = toolURL;
 			this.clazz = clazz;
 			this.description = description;
 			this.folder = folder;
 			this.fileName = fileName;
 			this.license = license;
 			this.mustAccept = mustAccept; 
 			this.documentation = documentation;
 			this.requiredComponents = requiredComponents;
 			this.relatedComponents = relatedComponents;
 			this.parser = parser;
 			this.analysis = analysis;
 			this.visualizer = visualizer;
 			this.loadByDefault = loadByDefault;
 			this.hidden = hidden;
 		}
 
 		public boolean isSelected() {
 			return selected;
 		}
 
 		public void setSelected(boolean selected) {
 			this.selected = selected;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		public String getVersion() {
 			return version;
 		}
 
 		public void setVersion(String version) {
 			this.version = version;
 		}
 
 		public String getAuthor() {
 			return author;
 		}
 
 		public void setAuthor(String author) {
 			this.author = author;
 		}
 
 		public JButton getAuthorURL() {
 			ImageIcon image = Util.createImageIcon("/org/geworkbench/engine/visualPlugin.png", this.authorURL);
 			
 			JButton button = new JButton(image);
 			button.setToolTipText(this.authorURL);
 			button.setBorderPainted(false);
 			return button;
 		}
 		
 		public void setAuthorURL(String authorURL) {
 			this.authorURL = authorURL;
 		}
 
 		public JButton getToolURL() {
 			ImageIcon image = Util.createImageIcon("/org/geworkbench/engine/visualPlugin.png", this.toolURL);
 			JButton button = new JButton(image);
 			button.setToolTipText(this.toolURL);
 			button.setBorderPainted(false);
 			return button;
 		}
 
 		public void setToolURL(String toolURL) {
 			this.toolURL = toolURL;
 		}
 
 		public String getClazz() {
 			return clazz;
 		}
 
 		public void setClazz(String clazz) {
 			this.clazz = clazz;
 		}
 
 		public String getFolder() {
 			return folder;
 		}
 
 		public void setDescription(String description) {
 			this.description = description;
 		}
 
 		public void setFolder(String folder) {
 			this.folder = folder;
 		}
 
 		public String getDescription() {
 			return description;
 		}
 
 		public String getFileName() {
 			return fileName;
 		}
 
 		public void setFileName(String fileName) {
 			this.fileName = fileName;
 		}
 
 		public String getLicense() {
 			return license;
 		}
 
 		public void setLicense(String license) {
 			this.license = license;
 		}
 
 		public boolean isMustAccept() {
 			return mustAccept;
 		}
 
 		public void setMustAccept(boolean mustAccept) {
 			this.mustAccept = mustAccept;
 		}
 		
 		public String getDocumentation() {
 			return documentation;
 		}
 
 		public void setDocumentation(String documentation) {
 			this.documentation = documentation;
 		}
 		
 		public List<String> getRequiredComponents() {
 			return requiredComponents;
 		}
 
 		public void setRequiredComponents(List<String> requiredComponents) {
 			this.requiredComponents = requiredComponents;
 		}
 
 		public List<String> getRelatedComponents() {
 			return relatedComponents;
 		}
 
 		public void setRelatedComponents(List<String> relatedComponents) {
 			this.relatedComponents = relatedComponents;
 		}
 
 		public boolean isParser() {
 			return parser;
 		}
 
 		public void setParser(boolean parser) {
 			this.parser = parser;
 		}
 
 		public boolean isAnalysis() {
 			return analysis;
 		}
 
 		public void setAnalysis(boolean analysis) {
 			this.analysis = analysis;
 		}
 
 		public boolean isVisualizer() {
 			return visualizer;
 		}
 
 		public void setVisualizer(boolean visualizer) {
 			this.visualizer = visualizer;
 		}
 
 		public boolean isLoadByDefault() {
 			return loadByDefault;
 		}
 
 		public void setLoadByDefault(boolean loadByDefault) {
 			this.loadByDefault = loadByDefault;
 		}
 
 		public boolean isHidden() {
 			return hidden;
 		}
 
 		public void setHidden(boolean hidden) {
 			this.hidden = hidden;
 		}
 		
 		
 	}
 
 	@SuppressWarnings("unchecked")
 	private class TableNameComparator implements Comparator{
 		public int compare(Object row1, Object row2){
 			String name1 = ( (TableRow) row1).getName().toLowerCase();
 			String name2 = ( (TableRow) row2).getName().toLowerCase();
 			return name1.compareTo(name2);
 		}
 	}
 	
 	/**
 	 * CheckBoxHeaderListener
 	 * 
 	 * @author tg2321
	 * @version $Id: ComponentConfigurationManagerWindow.java,v 1.11 2009-08-04 14:56:06 tgarben Exp $
 	 */
 	class CheckBoxHeaderListener implements ItemListener {
 		public void itemStateChanged(ItemEvent e) {
 			Object source = e.getSource();
 			if (source instanceof AbstractButton == false)
 				return;
 			boolean checked = e.getStateChange() == ItemEvent.SELECTED;
 			for (int x = 0, y = ccmTableModel.getRowCount(); x < y; x++) {
 
 				/* Don't unload LOAD_BY_DEFAULT_INDEX components */
 				if(!checked && ((Boolean)ccmTableModel.getModelValueAt(x, CCMTableModel.LOAD_BY_DEFAULT_INDEX)).booleanValue()){
 					continue;					
 				}
 				
 				ccmTableModel.setValueAt(new Boolean(checked), x, 0, true);
 			}
 		}
 	}
 	
 	/**
 	 * CheckBoxHeader
 	 * 
 	 * @author tg2321
	 * @version $Id: ComponentConfigurationManagerWindow.java,v 1.11 2009-08-04 14:56:06 tgarben Exp $
 	 */
 	class CheckBoxHeader extends JCheckBox implements TableCellRenderer,
 			MouseListener {
 		private static final long serialVersionUID = 9098648150367788628L;
 		protected CheckBoxHeader rendererComponent;
 		protected int column;
 		protected boolean mousePressed = false;
 
 		public CheckBoxHeader(ItemListener itemListener) {
 			rendererComponent = this;
 			rendererComponent.addItemListener(itemListener);
 		}
 
 		public Component getTableCellRendererComponent(JTable table,
 				Object value, boolean isSelected, boolean hasFocus, int row,
 				int column) {
 			if (table != null) {
 				JTableHeader header = table.getTableHeader();
 				if (header != null) {
 					rendererComponent.setHorizontalAlignment(CENTER);
 					rendererComponent.setForeground(header.getForeground());
 					rendererComponent.setBackground(header.getBackground());
 					rendererComponent.setFont(header.getFont());
 					header.addMouseListener(rendererComponent);
 				}
 			}
 			setColumn(column);
 			rendererComponent.setText("On/Off");
 			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
 			return rendererComponent;
 		}
 
 		protected void setColumn(int column) {
 			this.column = column;
 		}
 
 		public int getColumn() {
 			return column;
 		}
 
 		protected void handleClickEvent(MouseEvent e) {
 			if (mousePressed) {
 				mousePressed = false;
 				JTableHeader header = (JTableHeader) (e.getSource());
 				JTable tableView = header.getTable();
 				TableColumnModel columnModel = tableView.getColumnModel();
 				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
 				int column = tableView.convertColumnIndexToModel(viewColumn);
 
 				if (viewColumn == this.column && e.getClickCount() == 1
 						&& column != -1) {
 					doClick();
 				}
 			}
 		}
 
 		public void mouseClicked(MouseEvent e) {
 			handleClickEvent(e);
 			((JTableHeader) e.getSource()).repaint();
 		}
 
 		public void mousePressed(MouseEvent e) {
 			mousePressed = true;
 		}
 
 		public void mouseReleased(MouseEvent e) {
 		}
 
 		public void mouseEntered(MouseEvent e) {
 		}
 
 		public void mouseExited(MouseEvent e) {
 		}
 	} 
 	
 	class JTableButtonRenderer implements TableCellRenderer {
 		private TableCellRenderer __defaultRenderer;
 
 		public JTableButtonRenderer(TableCellRenderer renderer) {
 			__defaultRenderer = renderer;
 		}
 
 		public Component getTableCellRendererComponent(JTable table,
 				Object value, boolean isSelected, boolean hasFocus, int row,
 				int column) {
 			if (value instanceof Component){
 				
 				if (column == CCMTableModel.AUTHOR_URL_INDEX || column == CCMTableModel.TOOL_URL_INDEX){
 					DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
 				    TableColumn col = colModel.getColumn(column);
 					col.setMinWidth(75);
 					col.setMaxWidth(75);
 				}
 				
 				return (Component) value;
 			}
 
 			Component component = __defaultRenderer.getTableCellRendererComponent(table,
 				value, isSelected, hasFocus, row, column);
 			return component;
 		}
 	}
 	
 }
