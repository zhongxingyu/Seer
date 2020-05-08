 package edu.rutgers.MOST.presentation;
 
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableColumn;
 import javax.swing.text.JTextComponent;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.xml.stream.XMLStreamException;
 
 import org.apache.log4j.Logger;
 import org.jdesktop.swingx.JXTable;
 import org.jdesktop.swingx.decorator.ColorHighlighter;
 import org.jdesktop.swingx.decorator.ComponentAdapter;
 import org.jdesktop.swingx.decorator.HighlightPredicate;
 import org.sbml.jsbml.SBMLDocument;
 import org.sbml.jsbml.SBMLReader;
 
 import edu.rutgers.MOST.config.ConfigConstants;
 import edu.rutgers.MOST.config.LocalConfig;
 import edu.rutgers.MOST.data.FBAModel;
 import edu.rutgers.MOST.data.GDBBModel;
 import edu.rutgers.MOST.data.JSBMLWriter;
 import edu.rutgers.MOST.data.MetaboliteFactory;
 import edu.rutgers.MOST.data.MetaboliteUndoItem;
 import edu.rutgers.MOST.data.ModelReactionEquation;
 import edu.rutgers.MOST.data.ObjectCloner;
 import edu.rutgers.MOST.data.ReactionFactory;
 import edu.rutgers.MOST.data.ReactionUndoItem;
 import edu.rutgers.MOST.data.SBMLModelReader;
 import edu.rutgers.MOST.data.SBMLProduct;
 import edu.rutgers.MOST.data.SBMLReactant;
 import edu.rutgers.MOST.data.SBMLReactionEquation;
 import edu.rutgers.MOST.data.SettingsFactory;
 import edu.rutgers.MOST.data.Solution;
 import edu.rutgers.MOST.data.TextMetabolitesModelReader;
 import edu.rutgers.MOST.data.TextMetabolitesWriter;
 import edu.rutgers.MOST.data.TextReactionsModelReader;
 import edu.rutgers.MOST.data.TextReactionsWriter;
 import edu.rutgers.MOST.data.UndoConstants;
 import edu.rutgers.MOST.logic.ReactionParser;
 import edu.rutgers.MOST.optimization.FBA.FBA;
 import edu.rutgers.MOST.optimization.GDBB.GDBB;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.text.DecimalFormat;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import layout.TableLayout;
 
 public class GraphicalInterface extends JFrame {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	//log4j
 	static Logger log = Logger.getLogger(GraphicalInterface.class);
 
 	public static JToolBar toolbar = new JToolBar("Toolbar", JToolBar.HORIZONTAL);
 	public static JButton copybutton = new JButton(new ImageIcon(GraphicalInterfaceConstants.COPY_ICON_IMAGE_PATH));
 	public static JButton pastebutton = new JButton(new ImageIcon(GraphicalInterfaceConstants.PASTE_ICON_IMAGE_PATH));
 	public static JButton findbutton = new JButton(new ImageIcon(GraphicalInterfaceConstants.FIND_ICON_IMAGE_PATH));
 	public static OptionComponent undoSplitButton = new OptionComponent("image", "");
 	public static OptionComponent redoSplitButton = new OptionComponent("image", "");	
 	public static JLabel undoLabel = new JLabel(new ImageIcon(GraphicalInterfaceConstants.UNDO_ICON_IMAGE_PATH));
 	public static JLabel redoLabel = new JLabel(new ImageIcon(GraphicalInterfaceConstants.REDO_ICON_IMAGE_PATH));
 	public static JLabel undoGrayedLabel = new JLabel(new ImageIcon(GraphicalInterfaceConstants.UNDO_GRAYED_ICON_IMAGE_PATH));
 	public static JLabel redoGrayedLabel = new JLabel(new ImageIcon(GraphicalInterfaceConstants.REDO_GRAYED_ICON_IMAGE_PATH));
 
 	// Excel calls this a formula bar
 	public static JTextField formulaBar = new JTextField();
 
 	public static JXTable reactionsTable = new JXTable(){  
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public boolean isCellEditable(int row, int column){	    	   
 			if (!isRoot) {	
 				return false;					
 			}
 			return true;  
 		}
 
 		public Component prepareEditor(
 				TableCellEditor editor, int row, int column)
 		{
 			Component c = super.prepareEditor(editor, row, column);
 
 			if (c instanceof JTextComponent)
 			{
 				((JTextField)c).selectAll();
 			}
 
 			return c;
 		}		
 	}; 
 
 	public static JXTable metabolitesTable = new JXTable(){  
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public boolean isCellEditable(int row, int column){	    	   
 			if (!isRoot) {	
 				return false;					
 			}
 			return true;  
 		}
 
 		public Component prepareEditor(
 				TableCellEditor editor, int row, int column)
 		{
 			Component c = super.prepareEditor(editor, row, column);
 
 			if (c instanceof JTextComponent)
 			{
 				((JTextField)c).selectAll();
 			}
 
 			return c;
 		}		
 	}; 
 
 	protected GraphicalInterface gi;
 
 	public static DefaultListModel<String> listModel = new DefaultListModel<String>();	
 
 	public static JTextArea outputTextArea = new JTextArea();
 	public static JScrollPane outputPane = new JScrollPane(outputTextArea);
 
 	public static JLabel statusBar = new JLabel();
 
 	//set tabs south (bottom) = 3
 	public static JTabbedPane tabbedPane = new JTabbedPane(3); 
 
 	//Methods of saving current directory
 	public static SettingsFactory curSettings;
 
 	protected GDBBTask gdbbTask;
 
 	private Task task;	
 	public final ProgressBar progressBar = new ProgressBar();	
 	javax.swing.Timer timer = new javax.swing.Timer(100, new TimeListener());
 
 	/*****************************************************************************/
 	// boolean values
 	/*****************************************************************************/
 
 	public boolean showIdColumn;
 
 	public static boolean showPrompt;
 	// selection values
 	public static boolean includeRxnColumnNames;
 	public static boolean includeMtbColumnNames;
 	// load values
 	public static boolean isCSVFile;
 	public static boolean validFile;
 	// highlighting
 	public static boolean highlightUnusedMetabolites;	
 	public static boolean highlightParticipatingRxns;
 	// listener values
 	public static boolean selectedCellChanged;
 	public static boolean formulaBarFocusGained;
 	public static boolean tabChanged;
 	// find-replace values 
 	public static boolean findMode;
 	public static boolean findButtonReactionsClicked;
 	public static boolean findButtonMetabolitesClicked;
 	public static boolean matchCase;
 	public static boolean wrapAround;
 	public static boolean searchSelectedArea;
 	public static boolean searchBackwards;
 	public static boolean reactionUpdateValid;
 	public static boolean metaboliteUpdateValid;
 	public static boolean replaceAllMode;
 	public static boolean reactionsFindAll;
 	public static boolean metabolitesFindAll;
 	public static boolean throwNotFoundError;
 	public static boolean changeReactionFindSelection;
 	public static boolean changeMetaboliteFindSelection;
 	// paste
 	public static boolean validPaste;                 // used for error message when pasting non-valid values
 	public static boolean showDuplicatePrompt;
 	public static boolean duplicateMetabOK;
 	// other
 	public static boolean showErrorMessage;
 	public static boolean saveOptFile;
 	public static boolean addReacColumn;              // used to scroll added column to visible
 	public static boolean addMetabColumn;             // used to scroll added column to visible
 	public static boolean duplicatePromptShown;		  // ensures "Duplicate Metabolite" prompt displayed once per event
 	public static boolean renameMetabolite;           // if Rename menu action determines used, is set to true to for OK button action          
 	public static boolean reactionsTableEditable;	  
 	public static boolean modelCollectionOKButtonClicked;
 	public static boolean reactionCancelLoad;
 	public static boolean isRoot;
 	public static boolean hasGurobiPath;
 	public static boolean setGurobiPathClicked;
 	public static boolean gurobiPathSelected;
 	public static boolean gurobiPathErrorShown;
 	public static boolean openGurobiPathFilechooser;
 	public static boolean gurobiFileChooserShown;
 	public static boolean gurobiPathFound;
 	public static boolean openFileChooser;
 	public static boolean showMetaboliteRenameInterface;
 	public boolean addMetabolite;
 	// close
 	public static boolean exit;
 
 	/*****************************************************************************/
 	// end boolean values
 	/*****************************************************************************/ 
 
 	/*****************************************************************************/
 	// components
 	/*****************************************************************************/		
 
 	public final CSVLoadInterface csvLoadInterface = new CSVLoadInterface();
 
 	private static FindReplaceDialog findReplaceDialog;
 
 	public void setFindReplaceDialog(FindReplaceDialog findReplaceDialog) {
 		GraphicalInterface.findReplaceDialog = findReplaceDialog;
 	}
 
 	public static FindReplaceDialog getFindReplaceDialog() {
 		return findReplaceDialog;
 	}
 
 	private static GDBBDialog textInput;
 
 	public static GDBBDialog getTextInput() {
 		return textInput;
 	}
 
 	public void setTextInput(GDBBDialog textInput) {
 		GraphicalInterface.textInput = textInput;
 	}
 
 	private static GurobiPathInterface gurobiPathInterface;
 
 	public static GurobiPathInterface getGurobiPathInterface() {
 		return gurobiPathInterface;
 	}
 
 	public static void setGurobiPathInterface(
 			GurobiPathInterface gurobiPathInterface) {
 		GraphicalInterface.gurobiPathInterface = gurobiPathInterface;
 	}
 
 	private static MetaboliteColAddRenameInterface metaboliteColAddRenameInterface;   
 
 	public void setMetaboliteColAddRenameInterface(MetaboliteColAddRenameInterface metaboliteColAddRenameInterface) {
 		GraphicalInterface.metaboliteColAddRenameInterface = metaboliteColAddRenameInterface;
 	}
 
 	public static MetaboliteColAddRenameInterface getMetaboliteColAddRenameInterface() {
 		return metaboliteColAddRenameInterface;
 	}
 
 	private static MetaboliteColumnNameInterface metaboliteColumnNameInterface;
 
 	public static MetaboliteColumnNameInterface getMetaboliteColumnNameInterface() {
 		return metaboliteColumnNameInterface;
 	}
 
 	public static void setMetaboliteColumnNameInterface(
 			MetaboliteColumnNameInterface metaboliteColumnNameInterface) {
 		GraphicalInterface.metaboliteColumnNameInterface = metaboliteColumnNameInterface;
 	}
 
 	private static MetaboliteRenameInterface metaboliteRenameInterface;
 
 	public void setMetaboliteRenameInterface(MetaboliteRenameInterface metaboliteRenameInterface) {
 		GraphicalInterface.metaboliteRenameInterface = metaboliteRenameInterface;
 	}
 
 	public static MetaboliteRenameInterface getMetaboliteRenameInterface() {
 		return metaboliteRenameInterface;
 	}
 
 	private static ModelCollectionTable modelCollectionTable;
 
 	public static ModelCollectionTable getModelCollectionTable() {
 		return modelCollectionTable;
 	}
 
 	public static void setModelCollectionTable(ModelCollectionTable modelCollectionTable) {
 		GraphicalInterface.modelCollectionTable = modelCollectionTable;
 	}
 
 	private static OutputPopout popout;
 
 	public void setPopout(OutputPopout popout) {
 		GraphicalInterface.popout = popout;
 	}
 
 	public static OutputPopout getPopout() {
 		return popout;
 	}
 
 	private static ReactionColAddRenameInterface reactionColAddRenameInterface;
 
 	public void setReactionColAddRenameInterface(ReactionColAddRenameInterface reactionColAddRenameInterface) {
 		GraphicalInterface.reactionColAddRenameInterface = reactionColAddRenameInterface;
 	}
 
 	public static ReactionColAddRenameInterface getReactionColAddRenameInterface() {
 		return reactionColAddRenameInterface;
 	}
 
 	private static ReactionColumnNameInterface reactionColumnNameInterface;
 
 	public static ReactionColumnNameInterface getReactionColumnNameInterface() {
 		return reactionColumnNameInterface;
 	}
 
 	public static void setReactionColumnNameInterface(
 			ReactionColumnNameInterface reactionColumnNameInterface) {
 		GraphicalInterface.reactionColumnNameInterface = reactionColumnNameInterface;
 	}
 
 	private static ReactionEditor reactionEditor;
 
 	public void setReactionEditor(ReactionEditor reactionEditor) {
 		GraphicalInterface.reactionEditor = reactionEditor;
 	}
 
 	public static ReactionEditor getReactionEditor() {
 		return reactionEditor;
 	}	
 
 	private DynamicTreePanel treePanel;
 
 	/*****************************************************************************/
 	// end components
 	/*****************************************************************************/
 
 	/*****************************************************************************/
 	// find replace
 	/*****************************************************************************/
 
 	private static ArrayList<ArrayList<Integer>> metabolitesFindLocationsList;
 
 	public static ArrayList<ArrayList<Integer>> getMetabolitesFindLocationsList() {
 		return metabolitesFindLocationsList;
 	}
 
 	public static void setMetabolitesFindLocationsList(
 			ArrayList<ArrayList<Integer>> metabolitesFindLocationsList) {
 		GraphicalInterface.metabolitesFindLocationsList = metabolitesFindLocationsList;
 	}
 
 	private static ArrayList<Integer> metabolitesReplaceLocation;
 
 	public static ArrayList<Integer> getMetabolitesReplaceLocation() {
 		return metabolitesReplaceLocation;
 	}
 
 	public static void setMetabolitesReplaceLocation(ArrayList<Integer> metabolitesReplaceLocation) {
 		GraphicalInterface.metabolitesReplaceLocation = metabolitesReplaceLocation;
 	}
 
 	private static ArrayList<ArrayList<Integer>> reactionsFindLocationsList;
 
 	public static ArrayList<ArrayList<Integer>> getReactionsFindLocationsList() {
 		return reactionsFindLocationsList;
 	}
 
 	public static void setReactionsFindLocationsList(
 			ArrayList<ArrayList<Integer>> reactionsFindLocationsList) {
 		GraphicalInterface.reactionsFindLocationsList = reactionsFindLocationsList;
 	}
 
 	private static ArrayList<Integer> reactionsReplaceLocation;
 
 	public static ArrayList<Integer> getReactionsReplaceLocation() {
 		return reactionsReplaceLocation;
 	}
 
 	public static void setReactionsReplaceLocation(ArrayList<Integer> reactionsReplaceLocation) {
 		GraphicalInterface.reactionsReplaceLocation = reactionsReplaceLocation;
 	}
 
 	private static String replaceAllError;
 
 	public void setReplaceAllError(String replaceAllError) {
 		GraphicalInterface.replaceAllError = replaceAllError;
 	}
 
 	public static String getReplaceAllError() {
 		return replaceAllError;
 	}
 
 	/*****************************************************************************/
 	// end find replace
 	/*****************************************************************************/
 
 	/*****************************************************************************/
 	// menu items
 	/*****************************************************************************/
 
 	public final JMenuItem loadExistingItem = new JMenuItem(GraphicalInterfaceConstants.LOAD_FROM_MODEL_COLLECTION_TABLE_TITLE);
 	public final JMenuItem saveSBMLItem = new JMenuItem("Save As SBML");
 	public final JMenuItem saveCSVMetabolitesItem = new JMenuItem("Save As CSV Metabolites");
 	public final JMenuItem saveCSVReactionsItem = new JMenuItem("Save As CSV Reactions");
 	public final JMenuItem saveSQLiteItem = new JMenuItem("Save As SQLite");
 	public final JMenuItem clearItem = new JMenuItem("Clear Tables");
 	public final static JMenuItem fbaItem = new JMenuItem("FBA");
 	public final static JMenuItem gdbbItem = new JMenuItem("GDBB");
 	public final JCheckBoxMenuItem highlightUnusedMetabolitesItem = new JCheckBoxMenuItem("Highlight Unused Metabolites");
 	public final JMenuItem deleteUnusedItem = new JMenuItem("Delete All Unused Metabolites");
 	public final JMenuItem findSuspiciousItem = new JMenuItem("Find Suspicious Metabolites");
 	public final JMenuItem undoItem = new JMenuItem("Undo");
 	public final JMenuItem redoItem = new JMenuItem("Redo");
 	public final JMenuItem findReplaceItem = new JMenuItem("Find/Replace");	
 	public final JMenuItem addReacRowItem = new JMenuItem("Add Row to Reactions Table");
 	public final JMenuItem addMetabRowItem = new JMenuItem("Add Row to Metabolites Table");
 	public final JMenuItem addReacColumnItem = new JMenuItem("Add Column to Reactions Table");
 	public final JMenuItem addMetabColumnItem = new JMenuItem("Add Column to Metabolites Table"); 
 	public final JMenuItem deleteReactionRowMenuItem = new JMenuItem("Delete Row(s)");
 	public final JMenuItem deleteMetaboliteRowMenuItem = new JMenuItem("Delete Row(s)");
 	public final JMenuItem editorMenu = new JMenuItem("Launch Reaction Editor");
 
 	public final JMenuItem formulaBarCutItem = new JMenuItem("Cut");
 	public final JMenuItem formulaBarCopyItem = new JMenuItem("Copy");
 	public final JMenuItem formulaBarPasteItem = new JMenuItem("Paste");
 	public final JMenuItem formulaBarDeleteItem = new JMenuItem("Delete");
 	public final JMenuItem formulaBarSelectAllItem = new JMenuItem("Select All");
 
 	public final JMenuItem outputCopyItem = new JMenuItem("Copy");
 	public final JMenuItem outputSelectAllItem = new JMenuItem("Select All");
 
 	public final JMenuItem unhighlightParticipatingReactionsMenu = new JMenuItem("Un-Highlight Participating Reactions");
 	public final JMenuItem unhighlightMenu = new JMenuItem("Un-Highlight Participating Reactions");
 
 	/*****************************************************************************/
 	// end menu items
 	/*****************************************************************************/	
 
 	/*****************************************************************************/
 	// misc
 	/*****************************************************************************/
 
 	private static ArrayList<ModelReactionEquation> copiedReactions;
 
 	public static ArrayList<ModelReactionEquation> getCopiedReactions() {
 		return copiedReactions;
 	}
 
 	public static void setCopiedReactions(
 			ArrayList<ModelReactionEquation> copiedReactions) {
 		GraphicalInterface.copiedReactions = copiedReactions;
 	}
 
 	private static int currentMetabolitesRow;
 
 	public void setCurrentMetabolitesRow(int currentMetabolitesRow){
 		GraphicalInterface.currentMetabolitesRow = currentMetabolitesRow;
 	}
 
 	public static int getCurrentMetabolitesRow() {
 		return currentMetabolitesRow;
 	}
 
 	private static int currentMetabolitesColumn;
 
 	public void setCurrentMetabolitesColumn(int currentMetabolitesColumn){
 		GraphicalInterface.currentMetabolitesColumn = currentMetabolitesColumn;
 	}
 
 	public static int getCurrentMetabolitesColumn() {
 		return currentMetabolitesColumn;
 	}
 
 	private static int currentReactionsRow;
 
 	public void setCurrentReactionsRow(int currentReactionsRow){
 		GraphicalInterface.currentReactionsRow = currentReactionsRow;
 	}
 
 	public static int getCurrentReactionsRow() {
 		return currentReactionsRow;
 	}
 
 	private static int currentReactionsColumn;
 
 	public void setCurrentReactionsColumn(int currentReactionsColumn){
 		GraphicalInterface.currentReactionsColumn = currentReactionsColumn;
 	}
 
 	public static int getCurrentReactionsColumn() {
 		return currentReactionsColumn;
 	}
 
 	private static String fileType;
 	
 	public static String getFileType() {
 		return fileType;
 	}
 
 	public static void setFileType(String fileType) {
 		GraphicalInterface.fileType = fileType;
 	}
 
 	private static String gurobiPath;
 
 	public static String getGurobiPath() {
 		return gurobiPath;
 	}
 
 	public static void setGurobiPath(String gurobiPath) {
 		GraphicalInterface.gurobiPath = gurobiPath;
 	}
 
 	private static ArrayList<Image> icons;
 
 	public void setIconsList(ArrayList<Image> icons) {
 		GraphicalInterface.icons = icons;
 	}
 
 	public static ArrayList<Image> getIconsList() {
 		return icons;
 	}    
 
 	private static String loadErrorMessage;
 
 	public void setLoadErrorMessage(String loadErrorMessage) {
 		GraphicalInterface.loadErrorMessage = loadErrorMessage;
 	}
 
 	public static String getLoadErrorMessage() {
 		return loadErrorMessage;
 	}
 
 	private static String oldReaction;
 
 	public void setOldReaction(String oldReaction) {
 		GraphicalInterface.oldReaction = oldReaction;
 	}
 
 	public static String getOldReaction() {
 		return oldReaction;
 	}
 
 	private static String participatingMetabolite;
 
 	public void setParticipatingMetabolite(String participatingMetabolite) {
 		GraphicalInterface.participatingMetabolite = participatingMetabolite;
 	}
 
 	public static String getParticipatingMetabolite() {
 		return participatingMetabolite;
 	}
 
 	private static String pasteError;
 
 	public void setPasteError(String pasteError) {
 		GraphicalInterface.pasteError = pasteError;
 	}
 
 	public static String getPasteError() {
 		return pasteError;
 	}
 
 	private static int selectionMode;
 
 	public void setSelectionMode(int selectionMode){
 		GraphicalInterface.selectionMode = selectionMode;
 	}
 
 	public static int getSelectionMode() {
 		return selectionMode;
 	}
 
 	private static File SBMLFile;
 
 	public void setSBMLFile(File SBMLFile) {
 		GraphicalInterface.SBMLFile = SBMLFile;
 	}
 
 	public static File getSBMLFile() {
 		return SBMLFile;
 	}
 
 	private static String tableCellNewValue;
 
 	public void setTableCellNewValue(String tableCellNewValue) {
 		GraphicalInterface.tableCellNewValue = tableCellNewValue;
 	}
 
 	public static String getTableCellNewValue() {
 		return tableCellNewValue;
 	} 
 
 	private static String tableCellOldValue;
 
 	public void setTableCellOldValue(String tableCellOldValue) {
 		GraphicalInterface.tableCellOldValue = tableCellOldValue;
 	}
 
 	public static String getTableCellOldValue() {
 		return tableCellOldValue;
 	}    
 
 	/*****************************************************************************/
 	// end misc
 	/*****************************************************************************/
 
 	/*****************************************************************************/
 	// sorting
 	/*****************************************************************************/
 
 	private static int metabolitesSortColumnIndex;
 
 	public void setMetabolitesSortColumnIndex(int metabolitesSortColumnIndex){
 		GraphicalInterface.metabolitesSortColumnIndex = metabolitesSortColumnIndex;
 	}
 
 	public static int getMetabolitesSortColumnIndex() {
 		return metabolitesSortColumnIndex;
 	}
 
 	private static SortOrder metabolitesSortOrder;
 
 	public void setMetabolitesSortOrder(SortOrder metabolitesSortOrder){
 		GraphicalInterface.metabolitesSortOrder = metabolitesSortOrder;
 	}
 
 	public static SortOrder getMetabolitesSortOrder() {
 		return metabolitesSortOrder;
 	}
 
 	private static int reactionsSortColumnIndex;
 
 	public void setReactionsSortColumnIndex(int reactionsSortColumnIndex){
 		GraphicalInterface.reactionsSortColumnIndex = reactionsSortColumnIndex;
 	}
 
 	public static int getReactionsSortColumnIndex() {
 		return reactionsSortColumnIndex;
 	}
 
 	private static SortOrder reactionsSortOrder;
 
 	public void setReactionsSortOrder(SortOrder reactionsSortOrder){
 		GraphicalInterface.reactionsSortOrder = reactionsSortOrder;
 	}
 
 	public static SortOrder getReactionsSortOrder() {
 		return reactionsSortOrder;
 	}
 
 	/*****************************************************************************/
 	// end sorting
 	/*****************************************************************************/
 
 	// used in undo/redo when adding menu items actions, allows action to be
 	// enabled or disabled, based on visibility
 	public static final String ENABLE = "ENABLE";
 	public static final String DISABLE = "DISABLE";
 
 	public Integer undoCount;
 	public Integer redoCount;
 
 	// used for highlighting selected area in find mode
 	public Integer selectedReactionsRowStartIndex;
 	public Integer selectedReactionsRowEndIndex;
 	public Integer selectedReactionsColumnStartIndex;
 	public Integer selectedReactionsColumnEndIndex;
 	public Integer selectedMetabolitesRowStartIndex;
 	public Integer selectedMetabolitesRowEndIndex;
 	public Integer selectedMetabolitesColumnStartIndex;
 	public Integer selectedMetabolitesColumnEndIndex;
 
 	public GraphicalInterface() {
 		// make this true only when troubleshooting, false for actual use
 		showIdColumn = true;
 		//showIdColumn = false;
 
 		gi = this;
 
 		isRoot = true;
 		gurobiPathSelected = false;
 		gurobiPathErrorShown = false;
 
 		// Tree Panel
 		treePanel = new DynamicTreePanel(new DynamicTree() {
 			private static final long serialVersionUID = 1L;
 
 			public void valueChanged(TreeSelectionEvent e) {
 				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
 						tree.getLastSelectedPathComponent();
 
 				if (node == null) return;
 
 				Solution nodeInfo = (Solution)node.getUserObject();
 				if (node.isLeaf()) {
 					String solutionName = nodeInfo.getSolutionName();
 					if (solutionName.equals(LocalConfig.getInstance().getModelName())) {
 						enableMenuItems();
 						clearOutputPane();
 						if (getPopout() != null) {
 							getPopout().clear();
 						}	
 						setUpReactionsTable(LocalConfig.getInstance().getReactionsTableModelMap().get(solutionName));
 						setUpMetabolitesTable(LocalConfig.getInstance().getMetabolitesTableModelMap().get(solutionName));
 						setTitle(GraphicalInterfaceConstants.TITLE + " - " + solutionName);
 						isRoot = true;
 					} else {
 						if (node.getUserObject().toString() != null) {
 							setUpReactionsTable(LocalConfig.getInstance().getReactionsTableModelMap().get(solutionName));
 							setUpMetabolitesTable(LocalConfig.getInstance().getMetabolitesTableModelMap().get(solutionName));
 							if (solutionName.endsWith(node.getUserObject().toString())) {
 								loadOutputPane(solutionName + ".log");
 								if (getPopout() != null) {
 									getPopout().load(solutionName + ".log");
 								}										
 							}
 							disableMenuItems();
 							setTitle(GraphicalInterfaceConstants.TITLE + " - " + solutionName);
 							isRoot = false;					
 						}
 					}					
 				}
 			}
 		});
 
 		// this code must be before any components if the components
 		// are going to have an image icon
 		final ArrayList<Image> icons = new ArrayList<Image>(); 
 		icons.add(new ImageIcon("etc/most16.jpg").getImage()); 
 		icons.add(new ImageIcon("etc/most32.jpg").getImage());
 		setIconsList(icons);
 
 		textInput = new GDBBDialog(gi);
 		textInput.setModal(true);
 		textInput.setIconImages(icons);
 		textInput.setTitle("GDBB");
 		textInput.setSize(300, 300);
 		textInput.setResizable(false);
 		textInput.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		textInput.setLocationRelativeTo(null);
 		textInput.setAlwaysOnTop(true);
 		setTextInput(textInput);
 
 		LocalConfig.getInstance().setProgress(0);
 		progressBar.pack();
 		progressBar.setIconImages(icons);
 		progressBar.setSize(GraphicalInterfaceConstants.PROGRESS_BAR_WIDTH, GraphicalInterfaceConstants.PROGRESS_BAR_HEIGHT);		
 		progressBar.setResizable(false);
 		progressBar.setTitle("Loading...");
 		progressBar.progress.setIndeterminate(true);
 		progressBar.setLocationRelativeTo(null);
 		progressBar.setVisible(false);
 
 		csvLoadInterface.setIconImages(icons);					
 		csvLoadInterface.setSize(600, 200);
 		csvLoadInterface.setResizable(false);
 		csvLoadInterface.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		csvLoadInterface.setLocationRelativeTo(null);		
 		csvLoadInterface.setVisible(false);	
 		csvLoadInterface.setModal(true);
 		csvLoadInterface.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent evt) {
 				csvLoadInterface.setVisible(false);	        	
 			}
 		});			
 		csvLoadInterface.okButton.addActionListener(okButtonCSVLoadActionListener);
 		csvLoadInterface.cancelButton.addActionListener(cancelButtonCSVLoadActionListener);
 
 		setTitle(GraphicalInterfaceConstants.TITLE);
 		LocalConfig.getInstance().setModelName(GraphicalInterfaceConstants.DEFAULT_MODEL_NAME);
 
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent evt) {
 				SaveChangesPrompt();
 				if (exit) {
 					// Exit the application
 					System.exit(0);	
 				}	            	        	
 			}
 		});
 
 		setBooleanDefaults();
 		setSortDefault();
 		setUpCellSelectionMode();
 		setFileType(GraphicalInterfaceConstants.DEFAULT_FILE_TYPE);
 		// TODO: need to account for adding metabolites when creating model in blank gui
 		LocalConfig.getInstance().setMaxMetabolite(0);
 		LocalConfig.getInstance().setMaxMetaboliteId(GraphicalInterfaceConstants.BLANK_METABOLITE_ROW_COUNT);
 		LocalConfig.getInstance().setMaxReactionId(GraphicalInterfaceConstants.BLANK_REACTION_ROW_COUNT);
 		LocalConfig.getInstance().setReactionsLocationsListCount(0);
 		LocalConfig.getInstance().setMetabolitesLocationsListCount(0);
 
 		outputTextArea.setEditable(false);
 
 		listModel.addElement(GraphicalInterfaceConstants.DEFAULT_MODEL_NAME);
 		DynamicTreePanel.treePanel.addObject(new Solution(GraphicalInterfaceConstants.DEFAULT_MODEL_NAME, GraphicalInterfaceConstants.DEFAULT_MODEL_NAME));
 
 		// lists populated in file load
 		ArrayList<Integer> blankMetabIds = new ArrayList<Integer>();
 		LocalConfig.getInstance().setBlankMetabIds(blankMetabIds);	
 
 		Map<String, Object> metaboliteNameIdMap = new HashMap<String, Object>();
 		LocalConfig.getInstance().setMetaboliteNameIdMap(metaboliteNameIdMap);
 		Map<Object, String> metaboliteIdNameMap = new HashMap<Object, String>();
 		LocalConfig.getInstance().setMetaboliteIdNameMap(metaboliteIdNameMap);
 		Map<String, Object> metaboliteUsedMap = new HashMap<String, Object>();
 		LocalConfig.getInstance().setMetaboliteUsedMap(metaboliteUsedMap);			
 		ArrayList<Integer> suspiciousMetabolites = new ArrayList<Integer>();
 		LocalConfig.getInstance().setSuspiciousMetabolites(suspiciousMetabolites);
 		ArrayList<Integer> unusedList = new ArrayList<Integer>();
 		LocalConfig.getInstance().setUnusedList(unusedList);
 
 		// lists used in find and replace
 		ArrayList<ArrayList<Integer>> metabolitesFindLocationsList = new ArrayList<ArrayList<Integer>>();
 		setMetabolitesFindLocationsList(metabolitesFindLocationsList);
 		ArrayList<ArrayList<Integer>> reactionsFindLocationsList = new ArrayList<ArrayList<Integer>>();
 		setReactionsFindLocationsList(reactionsFindLocationsList);
 		ArrayList<Integer> reactionsReplaceLocation = new ArrayList<Integer>();
 		setReactionsReplaceLocation(reactionsReplaceLocation);
 		ArrayList<Integer> metabolitesReplaceLocation = new ArrayList<Integer>();
 		setMetabolitesReplaceLocation(metabolitesReplaceLocation);
 		ArrayList<String> findEntryList = new ArrayList<String>();
 		LocalConfig.getInstance().setFindEntryList(findEntryList);
 		ArrayList<String> replaceEntryList = new ArrayList<String>();
 		LocalConfig.getInstance().setReplaceEntryList(replaceEntryList);
 
 		selectedReactionsRowStartIndex = 0;
 		selectedReactionsRowEndIndex = 0;
 		selectedReactionsColumnStartIndex = 1;
 		selectedReactionsColumnEndIndex = 1;
 		selectedMetabolitesRowStartIndex = 0;
 		selectedMetabolitesRowEndIndex = 0;
 		selectedMetabolitesColumnStartIndex = 1;
 		selectedMetabolitesColumnEndIndex = 1;
 
 		// miscellaneous lists and maps
 		ArrayList<Integer> participatingReactions = new ArrayList<Integer>();
 		LocalConfig.getInstance().setParticipatingReactions(participatingReactions);
 		ArrayList<String> optimizationFilesList = new ArrayList<String>();
 		LocalConfig.getInstance().setOptimizationFilesList(optimizationFilesList);
 		Map<String, Object> metabDisplayCollectionMap = new HashMap<String, Object>();
 		LocalConfig.getInstance().setMetabDisplayCollectionMap(metabDisplayCollectionMap);
 
 		// meta column lists
 		ArrayList<String> reactionsMetaColumnNames = new ArrayList<String>();
 		LocalConfig.getInstance().setReactionsMetaColumnNames(reactionsMetaColumnNames);
 		ArrayList<String> metabolitesMetaColumnNames = new ArrayList<String>();
 		LocalConfig.getInstance().setMetabolitesMetaColumnNames(metabolitesMetaColumnNames);
 
 		Map<Object, ModelReactionEquation> reactionEquationMap = new HashMap<Object, ModelReactionEquation>();
 		LocalConfig.getInstance().setReactionEquationMap(reactionEquationMap);
 		// map used to store equations pasted, so if paste invalid, not added to above map
 		Map<Object, ModelReactionEquation> reactionPasteEquationMap = new HashMap<Object, ModelReactionEquation>();
 		LocalConfig.getInstance().setReactionPasteEquationMap(reactionPasteEquationMap);
 		Map<String, Object> reactionsIdRowMap = new HashMap<String, Object>();
 		LocalConfig.getInstance().setReactionsIdRowMap(reactionsIdRowMap);
 		ArrayList<ModelReactionEquation> copiedReactions = new ArrayList<ModelReactionEquation>();
 		setCopiedReactions(copiedReactions);
 
 		// table model maps
 		Map<String, DefaultTableModel> metabolitesTableModelMap = new HashMap<String, DefaultTableModel>();
 		LocalConfig.getInstance().setMetabolitesTableModelMap(metabolitesTableModelMap);
 		Map<String, DefaultTableModel> reactionsTableModelMap = new HashMap<String, DefaultTableModel>();
 		LocalConfig.getInstance().setReactionsTableModelMap(reactionsTableModelMap);
 
 		// undo/redo
 		Map<Object, Object> undoItemMap = new HashMap<Object, Object>();
 		LocalConfig.getInstance().setUndoItemMap(undoItemMap);
 		Map<Object, Object> redoItemMap = new HashMap<Object, Object>();
 		LocalConfig.getInstance().setRedoItemMap(redoItemMap);
 		Map<String, DefaultTableModel> metabolitesUndoTableModelMap = new HashMap<String, DefaultTableModel>();
 		LocalConfig.getInstance().setMetabolitesUndoTableModelMap(metabolitesUndoTableModelMap);
 		Map<String, DefaultTableModel> reactionsUndoTableModelMap = new HashMap<String, DefaultTableModel>();
 		LocalConfig.getInstance().setReactionsUndoTableModelMap(reactionsUndoTableModelMap);
 
 		undoCount = 1;
 		redoCount = 1;
 		LocalConfig.getInstance().setNumReactionTablesCopied(0);
 		LocalConfig.getInstance().setNumMetabolitesTableCopied(0);
 		LocalConfig.getInstance().setUndoMenuIndex(0);
 
 		// undo/redo sort order
 		ArrayList<Integer> reactionsSortColumns = new ArrayList<Integer>();		
 		ArrayList<SortOrder> reactionsSortOrderList = new ArrayList<SortOrder>();		
 		ArrayList<Integer> metabolitesSortColumns = new ArrayList<Integer>();		
 		ArrayList<SortOrder> metabolitesSortOrderList = new ArrayList<SortOrder>();
 		reactionsSortColumns.add(0);
 		LocalConfig.getInstance().setReactionsSortColumns(reactionsSortColumns);
 		reactionsSortOrderList.add(SortOrder.ASCENDING);
 		LocalConfig.getInstance().setReactionsSortOrderList(reactionsSortOrderList);
 		metabolitesSortColumns.add(0);
 		LocalConfig.getInstance().setMetabolitesSortColumns(metabolitesSortColumns);
 		metabolitesSortOrderList.add(SortOrder.ASCENDING);
 		LocalConfig.getInstance().setMetabolitesSortOrderList(metabolitesSortOrderList);
 
 		ArrayList<Integer> reactionsRedoSortColumns = new ArrayList<Integer>();		
 		ArrayList<SortOrder> reactionsRedoSortOrderList = new ArrayList<SortOrder>();		
 		ArrayList<Integer> metabolitesRedoSortColumns = new ArrayList<Integer>();		
 		ArrayList<SortOrder> metabolitesRedoSortOrderList = new ArrayList<SortOrder>();
 		LocalConfig.getInstance().setReactionsRedoSortColumns(reactionsRedoSortColumns);
 		LocalConfig.getInstance().setReactionsRedoSortOrderList(reactionsRedoSortOrderList);
 		LocalConfig.getInstance().setMetabolitesRedoSortColumns(metabolitesRedoSortColumns);
 		LocalConfig.getInstance().setMetabolitesRedoSortOrderList(metabolitesRedoSortOrderList);
 
 		ArrayList<Integer> addedMetabolites = new ArrayList<Integer>();
 		LocalConfig.getInstance().setAddedMetabolites(addedMetabolites);
 
 		DynamicTreePanel.treePanel.saveAsCSVItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) { 
 				saveOptFile = true;	
 				saveReactionsTextFileChooser();
 			}
 		});
 
 		DynamicTreePanel.treePanel.saveAsSBMLItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) { 
 				// Add action here when save SBML works	
 			}
 		});
 
 		DynamicTreePanel.treePanel.deleteItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) {
 				deleteItemFromDynamicTree();		
 			}
 		});
 
 		DynamicTreePanel.treePanel.clearItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) {
 				deleteAllItemsFromDynamicTree();			
 			}
 		});
 
 		/**************************************************************************/
 		// set up output popout
 		/**************************************************************************/		
 
 		final JPopupMenu outputPopupMenu = new JPopupMenu(); 
 		outputPopupMenu.add(outputCopyItem);
 		outputCopyItem.setEnabled(false);
 		outputCopyItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) { 	
 				setClipboardContents(outputTextArea.getSelectedText());							
 			}
 		});
 		outputPopupMenu.add(outputSelectAllItem);
 		outputSelectAllItem.setEnabled(false);
 		outputSelectAllItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) { 
 				outputTextArea.selectAll();							
 			}
 		});
 		outputPopupMenu.addSeparator();
 		JMenuItem popOutItem = new JMenuItem("Pop Out");
 		outputPopupMenu.add(popOutItem);
 		popOutItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) { 	
 				OutputPopout popout = new OutputPopout();
 				popout.setIconImages(icons);
 				setPopout(popout);
 				/*
 				if (getOptimizePath() != null) {
 					popout.load(getOptimizePath() + ".log");
 				}
 				 */							
 			}
 		});
 
 		outputTextArea.addMouseListener(new MouseAdapter() {
 
 			public void mousePressed(MouseEvent e)  {check(e);}
 			public void mouseReleased(MouseEvent e) {check(e);}
 
 			public void check(MouseEvent e) {
 				if (e.isPopupTrigger()) { //if the event shows the menu
 					outputPopupMenu.show(outputTextArea, e.getX(), e.getY()); 
 				}
 			}
 		});	
 
 		outputTextArea.getDocument().addDocumentListener(new DocumentListener() {
 			public void changedUpdate(DocumentEvent e) {
 				fieldChangeAction();
 			}
 			public void removeUpdate(DocumentEvent e) {
 				fieldChangeAction();
 			}
 			public void insertUpdate(DocumentEvent e) {
 				fieldChangeAction();
 			}
 			public void fieldChangeAction() {
 				if (outputTextArea.getText().length() > 0) {
 					outputCopyItem.setEnabled(true);
 					outputSelectAllItem.setEnabled(true);
 				} else {
 					outputCopyItem.setEnabled(false);
 					outputSelectAllItem.setEnabled(false);
 				}
 			}
 		});
 
 		/**************************************************************************/
 		// end set up output popout
 		/**************************************************************************/		
 
 		/**************************************************************************/
 		// create menu bar
 		/**************************************************************************/
 
 		JMenuBar menuBar = new JMenuBar();
 
 		setJMenuBar(menuBar);
 
 		JMenu modelMenu = new JMenu("Model");
 		modelMenu.setMnemonic(KeyEvent.VK_M);
 
 		JMenuItem loadSBMLItem = new JMenuItem("Load SBML");
 		modelMenu.add(loadSBMLItem);
 		loadSBMLItem.setMnemonic(KeyEvent.VK_L);
 		loadSBMLItem.addActionListener(new LoadSBMLAction());
 
 		JMenuItem loadCSVItem = new JMenuItem("Load CSV");
 		modelMenu.add(loadCSVItem);
		// does not work
		//loadCSVItem.setMnemonic(KeyEvent.VK_C);
 		loadCSVItem.addActionListener(new LoadCSVAction());
 
 		modelMenu.add(loadExistingItem);
		//loadExistingItem.setMnemonic(KeyEvent.VK_Q);
 		loadExistingItem.addActionListener(new LoadExistingItemAction());
 
 		modelMenu.addSeparator();
 
 		modelMenu.add(saveSBMLItem);
		saveSBMLItem.setMnemonic(KeyEvent.VK_S);
 		saveSBMLItem.addActionListener(new SaveSBMLItemAction());
 
 		modelMenu.add(saveCSVMetabolitesItem);
 		saveCSVMetabolitesItem.setMnemonic(KeyEvent.VK_O);
 		saveCSVMetabolitesItem.addActionListener(new SaveCSVMetabolitesItemAction());
 
 		modelMenu.add(saveCSVReactionsItem);
 		saveCSVReactionsItem.setMnemonic(KeyEvent.VK_N);
 		saveCSVReactionsItem.addActionListener(new SaveCSVReactionsItemAction());
 
 		modelMenu.addSeparator();
 
 		modelMenu.add(clearItem);
 		clearItem.setMnemonic(KeyEvent.VK_C);
 		clearItem.addActionListener(new ClearAction());
 
 		modelMenu.addSeparator();
 
 		JMenuItem exitItem = new JMenuItem("Exit");
 		modelMenu.add(exitItem);
 		exitItem.setMnemonic(KeyEvent.VK_X);
 		exitItem.addActionListener(new ExitAction());
 
 		menuBar.add(modelMenu);
 
 		//Analysis menu
 		JMenu analysisMenu = new JMenu("Analysis");
 		analysisMenu.setMnemonic(KeyEvent.VK_A);
 
 		analysisMenu.add(fbaItem);
 		if (!hasGurobiPath) {
 			fbaItem.setEnabled(false);
 		}
 		fbaItem.setMnemonic(KeyEvent.VK_F);
 
 		fbaItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) {
 				Utilities u = new Utilities();
 
 				highlightUnusedMetabolites = false;
 				highlightUnusedMetabolitesItem.setState(false);
 
 				String dateTimeStamp = u.createDateTimeStamp();
 				String optimizeName = GraphicalInterfaceConstants.OPTIMIZATION_PREFIX
 						+ LocalConfig.getInstance().getModelName() + dateTimeStamp;
 
 				// copy models, run optimization on these model
 				DefaultTableModel metabolitesOptModel = copyMetabolitesTableModel((DefaultTableModel) metabolitesTable.getModel());
 				DefaultTableModel reactionsOptModel = copyReactionsTableModel((DefaultTableModel) reactionsTable.getModel());				
 				LocalConfig.getInstance().getReactionsTableModelMap().put(optimizeName, reactionsOptModel);
 				LocalConfig.getInstance().getMetabolitesTableModelMap().put(optimizeName, metabolitesOptModel);
 				setUpReactionsTable(LocalConfig.getInstance().getReactionsTableModelMap().get(optimizeName));
 				setUpMetabolitesTable(LocalConfig.getInstance().getMetabolitesTableModelMap().get(optimizeName));
 				LocalConfig.getInstance().getOptimizationFilesList().add(optimizeName);
 
 				// Begin optimization
 
 				FBAModel model = new FBAModel();
 				log.debug("create an optimize");
 				FBA fba = new FBA();
 				fba.setFBAModel(model);
 				log.debug("about to optimize");
 				ArrayList<Double> soln = fba.run();
 				log.debug("optimization complete");
 				//End optimization
 
 				ReactionFactory rFactory = new ReactionFactory("SBML");
 				rFactory.setFluxes(soln);
 
 				if (LocalConfig.getInstance().hasValidGurobiKey) {
 					Writer writer = null;
 					try {
 						StringBuffer outputText = new StringBuffer();
 						outputText.append("FBA\n");
 						outputText.append(LocalConfig.getInstance().getModelName() + "\n");
 						outputText.append(model.getNumMetabolites() + " metabolites, " + model.getNumReactions() + " reactions\n");
 						outputText.append("Maximum objective: "	+ fba.getMaxObj() + "\n");
 
 						File file = new File(optimizeName + ".log");
 						writer = new BufferedWriter(new FileWriter(file));
 						writer.write(outputText.toString());
 
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					} finally {
 						try {
 							if (writer != null) {
 								writer.close();
 							}
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 					loadOutputPane(optimizeName + ".log");
 					if (getPopout() != null) {
 						getPopout().load(optimizeName + ".log");
 					}
 					setTitle(GraphicalInterfaceConstants.TITLE + " - " + optimizeName);
 					listModel.addElement(optimizeName);				
 					DynamicTreePanel.treePanel.addObject(new Solution(optimizeName, optimizeName));
 					DynamicTreePanel.treePanel.setNodeSelected(GraphicalInterface.listModel.getSize() - 1);
 				} else {
 					DynamicTreePanel.treePanel.setNodeSelected(0);
 				}
 				LocalConfig.getInstance().hasValidGurobiKey = true;
 
 			}
 		});
 
 		menuBar.add(analysisMenu);
 
 		analysisMenu.add(gdbbItem);
 		if (!hasGurobiPath) {
 			gdbbItem.setEnabled(false);
 		}
 		gdbbItem.setMnemonic(KeyEvent.VK_G);
 
 		//		TODO Optimization using GDBB
 		//	Action Listener for GDBB optimization
 		gdbbItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) {
 				Utilities u = new Utilities();
 				//load original db into tables
 				//reloadTables(getDatabaseName());
 				highlightUnusedMetabolites = false;
 				highlightUnusedMetabolitesItem.setState(false);
 
 				String dateTimeStamp = u.createDateTimeStamp();
 
 				textInput.setObjectiveColumnNames();
 
 				textInput.setVisible(true);
 			}
 		});
 
 		//Edit menu
 		JMenu editMenu = new JMenu("Edit");
 		editMenu.setMnemonic(KeyEvent.VK_E);
 
 		editMenu.add(highlightUnusedMetabolitesItem);
 		highlightUnusedMetabolitesItem.setMnemonic(KeyEvent.VK_H);
 
 		highlightUnusedMetabolitesItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				if (LocalConfig.getInstance().getUnusedList().size() > 0) {
 					highlightUnusedMetabolitesItem.setEnabled(true);
 				}
 				tabbedPane.setSelectedIndex(1);
 				boolean state = highlightUnusedMetabolitesItem.getState();
 				if (state == true) {
 					highlightUnusedMetabolites = true;
 				} else {
 					highlightUnusedMetabolites = false;
 				}
 				metabolitesTable.repaint();
 			}
 		});
 
 		editMenu.add(deleteUnusedItem);
 		deleteUnusedItem.setMnemonic(KeyEvent.VK_D);		
 
 		deleteUnusedItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {				
 				tabbedPane.setSelectedIndex(1);
 				// copy model so old model can be restored if paste not valid
 				DefaultTableModel oldMetabolitesModel = copyMetabolitesTableModel((DefaultTableModel) metabolitesTable.getModel());	
 				copyMetabolitesTableModels(oldMetabolitesModel); 
 				MetaboliteUndoItem undoItem = createMetaboliteUndoItem("", "", metabolitesTable.getSelectedRow(), metabolitesTable.getSelectedColumn(), 1, UndoConstants.DELETE_UNUSED, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);
 				undoItem.setTableCopyIndex(LocalConfig.getInstance().getNumMetabolitesTableCopied());
 				setUndoOldCollections(undoItem);
 				Map<String, Object> usedMap = LocalConfig.getInstance().getMetaboliteUsedMap();
 				Map<String, Object> idMap = new HashMap<String, Object>();
 
 				try {
 					idMap = (Map<String, Object>) (ObjectCloner.deepCopy(LocalConfig.getInstance().getMetaboliteNameIdMap()));
 					System.out.println("idmap" + idMap);
 					ArrayList<String> usedList = new ArrayList<String>(usedMap.keySet());
 					ArrayList<String> idList = new ArrayList<String>(idMap.keySet());
 					ArrayList<Integer> unusedList = new ArrayList<Integer>();
 					// removes unused metabolites from idMap and populates list of
 					// unused metabolite id's for deletion from table
 					for (int i = 0; i < idList.size(); i++) {						
 						if (!usedList.contains(idList.get(i))) {
 							int id = (Integer) idMap.get(idList.get(i));
 							idMap.remove(idList.get(i));
 							unusedList.add(id); 
 						}
 					}
 					LocalConfig.getInstance().setUnusedList(unusedList);
 					System.out.println(unusedList);
 					for (int i = 0; i < unusedList.size(); i++) {
 						deleteMetabolitesRowById(unusedList.get(i));
 					}	
 					for (int j = 0; j < LocalConfig.getInstance().getBlankMetabIds().size(); j++) {
 						deleteMetabolitesRowById(LocalConfig.getInstance().getBlankMetabIds().get(j));
 					}
 					highlightUnusedMetabolites = false;
 					highlightUnusedMetabolitesItem.setState(false);
 					formulaBar.setText("");
 					LocalConfig.getInstance().getUnusedList().clear();
 					LocalConfig.getInstance().setMetaboliteNameIdMap(idMap);
 					DefaultTableModel newMetabolitesModel = copyMetabolitesTableModel((DefaultTableModel) metabolitesTable.getModel());			
 					copyMetabolitesTableModels(newMetabolitesModel); 
 					setUndoNewCollections(undoItem);
 					setUpMetabolitesUndo(undoItem);	
 				} catch (Exception e3) {
 					// TODO Auto-generated catch block
 					e3.printStackTrace();
 				}			
 			}
 		});   
 
 		editMenu.add(findSuspiciousItem);
 		findSuspiciousItem.setMnemonic(KeyEvent.VK_S);
 		findSuspiciousItem.setEnabled(false);
 
 		findSuspiciousItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) {
 				tabbedPane.setSelectedIndex(1);
 				if (LocalConfig.getInstance().getSuspiciousMetabolites().size() > 0) {
 					int firstId = LocalConfig.getInstance().getSuspiciousMetabolites().get(0);
 					for (int r = 0; r < metabolitesTable.getRowCount(); r++) {
 						int viewRow = metabolitesTable.convertRowIndexToModel(r);
 						Integer cellValue = Integer.valueOf((String) metabolitesTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.METABOLITE_ID_COLUMN));
 						if (cellValue == firstId) {
 							setTableCellFocused(r, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN, metabolitesTable);
 						}	
 					}
 				}
 			}    	     
 		});
 
 		editMenu.addSeparator(); 
 
 		editMenu.add(undoItem);
 		undoItem.setMnemonic(KeyEvent.VK_U);
 		undoItem.setAccelerator(KeyStroke.getKeyStroke(
 				KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
 		undoItem.setEnabled(false);
 
 		undoItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) {				
 				undoButtonAction();
 			}    	     
 		});
 
 		editMenu.add(redoItem);
 		redoItem.setMnemonic(KeyEvent.VK_E);
 		redoItem.setAccelerator(KeyStroke.getKeyStroke(
 				KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
 		redoItem.setEnabled(false);
 
 		redoItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) {
 				redoButtonAction();			
 			}    	     
 		});
 
 		editMenu.add(findReplaceItem);
 		findReplaceItem.setMnemonic(KeyEvent.VK_F);
 		findReplaceItem.setAccelerator(KeyStroke.getKeyStroke(
 				KeyEvent.VK_F, ActionEvent.CTRL_MASK));
 		findReplaceItem.setEnabled(true);
 
 		findReplaceItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) {
 				showFindReplace();
 				findReplaceItem.setEnabled(false);
 				findbutton.setEnabled(false);
 			}    	     
 		});
 
 		editMenu.addSeparator();
 
 		editMenu.add(addReacRowItem);
 		addReacRowItem.setMnemonic(KeyEvent.VK_R);
 
 		ActionListener addReacRowActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				tabbedPane.setSelectedIndex(0);
 				int id = LocalConfig.getInstance().getMaxReactionId();
 				DefaultTableModel model = (DefaultTableModel) reactionsTable.getModel();	
 				model.addRow(createReactionsRow(id));
 				setUpReactionsTable(model);
 				ReactionUndoItem undoItem = createReactionUndoItem("", "", reactionsTable.getSelectedRow(), reactionsTable.getSelectedColumn(), id, UndoConstants.ADD_ROW, UndoConstants.REACTION_UNDO_ITEM_TYPE);		
 				//set focus to id cell in new row in order to set row visible
 				int maxRow = reactionsTable.getModel().getRowCount();
 				int viewRow = reactionsTable.convertRowIndexToView(maxRow - 1);
 				undoItem.setRow(maxRow - 1);
 				setTableCellFocused(viewRow, 1, reactionsTable);
 				setUpReactionsUndo(undoItem);
 				LocalConfig.getInstance().setMaxReactionId(id + 1);
 			}
 		};  
 
 		addReacRowItem.addActionListener(addReacRowActionListener);
 
 		editMenu.add(addMetabRowItem); 
 		addMetabRowItem.setMnemonic(KeyEvent.VK_M);
 
 		addMetabRowItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				tabbedPane.setSelectedIndex(1);
 				int id = LocalConfig.getInstance().getMaxMetaboliteId();
 				DefaultTableModel model = (DefaultTableModel) metabolitesTable.getModel();	
 				model.addRow(createMetabolitesRow(id));
 				setUpMetabolitesTable(model);
 				MetaboliteUndoItem undoItem = createMetaboliteUndoItem("", "", metabolitesTable.getSelectedRow(), metabolitesTable.getSelectedColumn(), id, UndoConstants.ADD_ROW, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);
 				setUndoOldCollections(undoItem);				
 				int maxRow = metabolitesTable.getModel().getRowCount();
 				int viewRow = metabolitesTable.convertRowIndexToView(maxRow - 1);
 				undoItem.setRow(maxRow - 1);
 				setTableCellFocused(viewRow, 1, metabolitesTable);
 				setUndoNewCollections(undoItem);
 				setUpMetabolitesUndo(undoItem);
 				LocalConfig.getInstance().setMaxMetaboliteId(id + 1);
 			}
 		});
 
 		editMenu.addSeparator();
 
 		editMenu.add(addReacColumnItem);
 		addReacColumnItem.setMnemonic(KeyEvent.VK_C);
 
 		addReacColumnItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				setCurrentReactionsRow(reactionsTable.getSelectedRow());
 				setCurrentReactionsColumn(reactionsTable.getSelectedColumn());
 				tabbedPane.setSelectedIndex(0);
 				LocalConfig.getInstance().addColumnInterfaceVisible = true;
 				addReacColumnItem.setEnabled(false);
 				addMetabColumnItem.setEnabled(false);
 				ReactionColAddRenameInterface reactionColAddRenameInterface = new ReactionColAddRenameInterface();
 				setReactionColAddRenameInterface(reactionColAddRenameInterface);
 				reactionColAddRenameInterface.setTitle(GraphicalInterfaceConstants.COLUMN_ADD_INTERFACE_TITLE);
 				reactionColAddRenameInterface.setIconImages(icons);
 				reactionColAddRenameInterface.setSize(350, 160);
 				reactionColAddRenameInterface.setResizable(false);
 				reactionColAddRenameInterface.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 				reactionColAddRenameInterface.setAlwaysOnTop(true);
 				reactionColAddRenameInterface.setModal(true);
 				reactionColAddRenameInterface.setLocationRelativeTo(null);
 				reactionColAddRenameInterface.addWindowListener(new WindowAdapter() {
 					public void windowClosing(WindowEvent evt) {
 						addReactionColumnCloseAction();
 					}
 				});					
 				reactionColAddRenameInterface.setVisible(true);	
 			}
 		});
 
 		ActionListener addColOKButtonActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent prodActionEvent) {
 				// allows table to scroll to make added column visible
 				addReacColumn = true;
 				if (getReactionColAddRenameInterface().isColumnDuplicate()) {
 					reactionColAddRenameInterface.setAlwaysOnTop(false);
 					reactionColAddRenameInterface.setModal(false);
 					JOptionPane.showMessageDialog(null,                
 							"Column Name Already Exists.",                
 							"Duplicate ColumnName",                                
 							JOptionPane.ERROR_MESSAGE);
 					reactionColAddRenameInterface.setAlwaysOnTop(true);
 					reactionColAddRenameInterface.setModal(true);
 				} else {
 					// copy old model for undo/redo
 					DefaultTableModel oldReactionsModel = copyReactionsTableModel((DefaultTableModel) reactionsTable.getModel());			
 					copyReactionsTableModels(oldReactionsModel);
 					ReactionUndoItem undoItem = createReactionUndoItem("", "", getCurrentReactionsRow(), getCurrentReactionsColumn(), 1, UndoConstants.ADD_COLUMN, UndoConstants.REACTION_UNDO_ITEM_TYPE);
 					undoItem.setTableCopyIndex(LocalConfig.getInstance().getNumReactionTablesCopied());
 					undoItem.setAddedColumnIndex(LocalConfig.getInstance().getReactionsMetaColumnNames().size() + GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES.length);
 					ArrayList<String> oldMetaCol = new ArrayList<String>();
 					ArrayList<String> newMetaCol = new ArrayList<String>();
 					for (int i = 0; i < LocalConfig.getInstance().getReactionsMetaColumnNames().size(); i++) {
 						oldMetaCol.add(LocalConfig.getInstance().getReactionsMetaColumnNames().get(i));
 					}
 					undoItem.setOldMetaColumnNames(oldMetaCol);
 					getReactionColAddRenameInterface().addColumn();
 					getReactionColAddRenameInterface().textField.setText("");
 					getReactionColAddRenameInterface().setVisible(false);
 					getReactionColAddRenameInterface().dispose();
 					setUpReactionsTable(LocalConfig.getInstance().getReactionsTableModelMap().get(LocalConfig.getInstance().getModelName()));
 					DefaultTableModel newReactionsModel = copyReactionsTableModel((DefaultTableModel) reactionsTable.getModel());			
 					copyReactionsTableModels(newReactionsModel);
 					for (int i = 0; i < LocalConfig.getInstance().getReactionsMetaColumnNames().size(); i++) {
 						newMetaCol.add(LocalConfig.getInstance().getReactionsMetaColumnNames().get(i));
 					}
 					undoItem.setNewMetaColumnNames(newMetaCol);
 					setUpReactionsUndo(undoItem);
 					addReacColumn = false;
 					addReactionColumnCloseAction();
 				}							
 			}
 		};
 
 		ActionListener addColCancelButtonActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent prodActionEvent) {
 				addReactionColumnCloseAction();
 			}
 		};
 
 		reactionColAddRenameInterface.okButton.addActionListener(addColOKButtonActionListener);
 		reactionColAddRenameInterface.cancelButton.addActionListener(addColCancelButtonActionListener);
 
 		editMenu.add(addMetabColumnItem);
 		addMetabColumnItem.setMnemonic(KeyEvent.VK_O);
 
 		addMetabColumnItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				setCurrentMetabolitesRow(metabolitesTable.getSelectedRow());
 				setCurrentMetabolitesColumn(metabolitesTable.getSelectedColumn());
 				tabbedPane.setSelectedIndex(1);
 				LocalConfig.getInstance().addColumnInterfaceVisible = true;
 				addReacColumnItem.setEnabled(false);
 				addMetabColumnItem.setEnabled(false);
 				MetaboliteColAddRenameInterface metaboliteColAddRenameInterface = new MetaboliteColAddRenameInterface();
 				setMetaboliteColAddRenameInterface(metaboliteColAddRenameInterface);
 				metaboliteColAddRenameInterface.setTitle(GraphicalInterfaceConstants.COLUMN_ADD_INTERFACE_TITLE);
 				metaboliteColAddRenameInterface.setIconImages(icons);
 				metaboliteColAddRenameInterface.setSize(350, 160);
 				metaboliteColAddRenameInterface.setResizable(false);
 				metaboliteColAddRenameInterface.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 				metaboliteColAddRenameInterface.setAlwaysOnTop(true);
 				metaboliteColAddRenameInterface.setModal(true);
 				metaboliteColAddRenameInterface.setLocationRelativeTo(null);
 				metaboliteColAddRenameInterface.addWindowListener(new WindowAdapter() {
 					public void windowClosing(WindowEvent evt) {
 						addMetaboliteColumnCloseAction();
 					}
 				});
 				metaboliteColAddRenameInterface.setVisible(true);						
 			}
 		});
 
 		ActionListener addMetabColOKButtonActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent prodActionEvent) {				
 				// allows table to scroll to make added column visible
 				addMetabColumn = true;
 				if (getMetaboliteColAddRenameInterface().isColumnDuplicate()) {
 					metaboliteColAddRenameInterface.setAlwaysOnTop(false);
 					metaboliteColAddRenameInterface.setModal(false);
 					JOptionPane.showMessageDialog(null,                
 							"Column Name Already Exists.",                
 							"Duplicate ColumnName",                                
 							JOptionPane.ERROR_MESSAGE);
 					metaboliteColAddRenameInterface.setAlwaysOnTop(true);
 					metaboliteColAddRenameInterface.setModal(true);
 				} else {
 					// copy old model for undo/redo
 					DefaultTableModel oldMetabolitesModel = copyMetabolitesTableModel((DefaultTableModel) metabolitesTable.getModel());			
 					copyMetabolitesTableModels(oldMetabolitesModel);
 					MetaboliteUndoItem undoItem = createMetaboliteUndoItem("", "", getCurrentMetabolitesRow(), getCurrentMetabolitesColumn(), 1, UndoConstants.ADD_COLUMN, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);		
 					undoItem.setTableCopyIndex(LocalConfig.getInstance().getNumMetabolitesTableCopied());
 					undoItem.setAddedColumnIndex(LocalConfig.getInstance().getMetabolitesMetaColumnNames().size() + GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES.length);
 					setUndoOldCollections(undoItem);
 					ArrayList<String> oldMetaCol = new ArrayList<String>();
 					ArrayList<String> newMetaCol = new ArrayList<String>();
 					for (int i = 0; i < LocalConfig.getInstance().getMetabolitesMetaColumnNames().size(); i++) {
 						oldMetaCol.add(LocalConfig.getInstance().getMetabolitesMetaColumnNames().get(i));
 					}
 					undoItem.setOldMetaColumnNames(oldMetaCol);
 					getMetaboliteColAddRenameInterface().addColumn();
 					getMetaboliteColAddRenameInterface().textField.setText("");
 					getMetaboliteColAddRenameInterface().setVisible(false);
 					getMetaboliteColAddRenameInterface().dispose();
 					setUpMetabolitesTable(LocalConfig.getInstance().getMetabolitesTableModelMap().get(LocalConfig.getInstance().getModelName()));					
 					DefaultTableModel newMetabolitesModel = copyMetabolitesTableModel((DefaultTableModel) metabolitesTable.getModel());			
 					copyMetabolitesTableModels(newMetabolitesModel);
 					for (int i = 0; i < LocalConfig.getInstance().getMetabolitesMetaColumnNames().size(); i++) {
 						newMetaCol.add(LocalConfig.getInstance().getMetabolitesMetaColumnNames().get(i));
 					}
 					undoItem.setNewMetaColumnNames(newMetaCol);
 					setUndoNewCollections(undoItem);
 					setUpMetabolitesUndo(undoItem);					
 					addMetabColumn = false;
 					addMetaboliteColumnCloseAction();
 				}								
 			}
 		};
 
 		ActionListener addMetabColCancelButtonActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent prodActionEvent) {
 				addMetaboliteColumnCloseAction();
 			}
 		};
 
 		metaboliteColAddRenameInterface.okButton.addActionListener(addMetabColOKButtonActionListener);
 		metaboliteColAddRenameInterface.cancelButton.addActionListener(addMetabColCancelButtonActionListener);
 
 		menuBar.add(editMenu);
 
 		JMenu optionsMenu = new JMenu("Options");
 		optionsMenu.setMnemonic(KeyEvent.VK_O);
 
 		JMenuItem setGurobiPath = new JMenuItem(GraphicalInterfaceConstants.GUROBI_JAR_PATH_OPTIONS_MENU_ITEM);
 		optionsMenu.add(setGurobiPath);
 		setGurobiPath.setMnemonic(KeyEvent.VK_G);
 		setGurobiPath.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) {
				//getGurobiPathInterface().setVisible(true);
				loadGurobiPathInterface();
 			}    	     
 		});
 
 		menuBar.add(optionsMenu);
 
 		JMenu helpMenu = new JMenu("Help");
 		helpMenu.setMnemonic(KeyEvent.VK_H);
 
 		JMenuItem viewHelpTopics = new JMenuItem("View Help Topics");
 		helpMenu.add(viewHelpTopics);
 		viewHelpTopics.setMnemonic(KeyEvent.VK_H);
 
 		viewHelpTopics.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) {
 				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
 				if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {
 					//System.err.println( "Desktop doesn't support the browse action (fatal)" );
 					//System.exit( 1 );
 					JOptionPane.showMessageDialog(null,                
 							"Default Browser Error. Default Browser May Not Be Set On This System.",                
 							"Default Browser Error",                                
 							JOptionPane.ERROR_MESSAGE); 
 				}
 
 				try{ 
 					String url = GraphicalInterfaceConstants.HELP_TOPICS_URL;  
 					java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));  
 				}  
 				catch (java.io.IOException e) {  
 					JOptionPane.showMessageDialog(null,                
 							GraphicalInterfaceConstants.HELP_URL_NOT_FOUND_MESSAGE,                
 							GraphicalInterfaceConstants.HELP_URL_NOT_FOUND_TITLE,                                
 							JOptionPane.ERROR_MESSAGE);   
 				}
 			}    	     
 		});
 
 		JMenuItem aboutBox = new JMenuItem("About MOST");
 		helpMenu.add(aboutBox);
 		aboutBox.setMnemonic(KeyEvent.VK_A);
 
 		aboutBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) {
 				JOptionPane.showMessageDialog(null,                
 						GraphicalInterfaceConstants.ABOUT_BOX_TEXT,                
 						GraphicalInterfaceConstants.ABOUT_BOX_TITLE,                                
 						JOptionPane.INFORMATION_MESSAGE);
 			}    	     
 		});
 
 		menuBar.add(helpMenu);
 
 		/**************************************************************************/
 		// end menu bar
 		/**************************************************************************/
 
 		/**************************************************************************/
 		//set up toolbar
 		/**************************************************************************/			
 
 		toolbar.add(copybutton);
 		setUpToolbarButton(copybutton);
 		copybutton.setToolTipText("Copy");
 		copybutton.addActionListener(copyButtonActionListener);
 		toolbar.add(pastebutton);
 		setUpToolbarButton(pastebutton);
 		pastebutton.setToolTipText("Paste");
 		pastebutton.addActionListener(pasteButtonActionListener);
 
 		toolbar.addSeparator();
 
 		addImage(undoSplitButton, undoLabel);
 		addImage(undoSplitButton, undoGrayedLabel);
 		undoSplitButton.setToolTipText("Can't Undo (Ctrl+Z)");	
 		undoSplitButton.addMouseListener(undoButtonMouseListener);
 		disableOptionComponent(undoSplitButton, undoLabel, undoGrayedLabel);
 		toolbar.add(undoSplitButton);
 
 		addImage(redoSplitButton, redoLabel);
 		addImage(redoSplitButton, redoGrayedLabel);
 		redoSplitButton.setToolTipText("Can't Redo (Ctrl+Y)");	
 		redoSplitButton.addMouseListener(redoButtonMouseListener);
 		disableOptionComponent(redoSplitButton, redoLabel, redoGrayedLabel);
 		toolbar.add(redoSplitButton);		
 
 		toolbar.add(findbutton);
 		setUpToolbarButton(findbutton);
 		findbutton.setToolTipText("Find/Replace");
 		findbutton.addActionListener(findButtonActionListener);
 
 		/**************************************************************************/
 		//end set up toolbar
 		/**************************************************************************/	
 
 		/************************************************************************/
 		// set up tables
 		/************************************************************************/
 
 		// register actions
 		ActionListener reactionsCopyActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				reactionsCopy();
 			}
 		};
 
 		ActionListener reactionsPasteActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				if (isRoot) {
 					reactionsPaste();
 				}
 			}
 		};
 
 		ActionListener reactionsClearActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				if (isRoot) {
 					reactionsClear();
 				}				
 			}
 		};
 
 		ActionListener reactionsFindActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {	
 				if (!findMode) {
 					showFindReplace();
 				}
 				findReplaceItem.setEnabled(false);
 				findbutton.setEnabled(false);
 			}
 		};
 
 		ActionListener reactionsUndoActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				if (undoItem.isEnabled()) {
 					undoButtonAction();
 				}				
 			}
 		};
 
 		ActionListener reactionsRedoActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				if (redoItem.isEnabled()) {
 					redoButtonAction();
 				}				
 			}
 		};
 
 		findReplaceDialog.findButton.addActionListener(findReactionsButtonActionListener);
 		findReplaceDialog.findAllButton.addActionListener(findAllReactionsButtonActionListener);
 		findReplaceDialog.replaceButton.addActionListener(replaceReactionsButtonActionListener);
 		findReplaceDialog.replaceAllButton.addActionListener(replaceAllReactionsButtonActionListener);
 		//findReplaceDialog.replaceFindButton.addActionListener(replaceFindReactionsButtonActionListener);
 		findReplaceDialog.doneButton.addActionListener(findDoneButtonActionListener);
 		findReplaceDialog.caseCheckBox.addActionListener(matchCaseActionListener);
 		findReplaceDialog.wrapCheckBox.addActionListener(wrapAroundActionListener);
 		findReplaceDialog.selectedAreaCheckBox.addActionListener(selectedAreaActionListener);
 		findReplaceDialog.backwardsCheckBox.addActionListener(searchBackwardsActionListener);
 
 		KeyStroke reacCopy = KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK,false);       
 		KeyStroke reacPaste = KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK,false); 		
 		KeyStroke reacClear = KeyStroke.getKeyStroke(KeyEvent.VK_E,ActionEvent.CTRL_MASK,false); 
 		KeyStroke reacFind = KeyStroke.getKeyStroke(KeyEvent.VK_F,ActionEvent.CTRL_MASK,false); 
 		KeyStroke reacUndo = KeyStroke.getKeyStroke(KeyEvent.VK_Z,ActionEvent.CTRL_MASK,false); 
 		KeyStroke reacRedo = KeyStroke.getKeyStroke(KeyEvent.VK_Y,ActionEvent.CTRL_MASK,false); 
 
 		DefaultTableModel blankReacModel = createBlankReactionsTableModel();
 		setUpReactionsTable(blankReacModel);
 		LocalConfig.getInstance().getReactionsTableModelMap().put(GraphicalInterfaceConstants.DEFAULT_MODEL_NAME, blankReacModel);
 		TableCellListener tcl = new TableCellListener(reactionsTable, reacAction);
 		ReactionsPopupListener reactionsPopupListener = new ReactionsPopupListener();
 		reactionsTable.addMouseListener(reactionsPopupListener);
 		reactionsTable.setRowHeight(20);
 		reactionsTable.registerKeyboardAction(reactionsCopyActionListener,reacCopy,JComponent.WHEN_FOCUSED); 
 		reactionsTable.registerKeyboardAction(reactionsPasteActionListener,reacPaste,JComponent.WHEN_FOCUSED); 		
 		reactionsTable.registerKeyboardAction(reactionsClearActionListener,reacClear,JComponent.WHEN_FOCUSED); 
 		reactionsTable.registerKeyboardAction(reactionsFindActionListener,reacFind,JComponent.WHEN_IN_FOCUSED_WINDOW); 
 		reactionsTable.registerKeyboardAction(reactionsFindActionListener,reacFind,JComponent.WHEN_FOCUSED); 
 		reactionsTable.registerKeyboardAction(reactionsUndoActionListener,reacUndo,JComponent.WHEN_IN_FOCUSED_WINDOW); 
 		reactionsTable.registerKeyboardAction(reactionsUndoActionListener,reacUndo,JComponent.WHEN_FOCUSED); 
 		reactionsTable.registerKeyboardAction(reactionsRedoActionListener,reacRedo,JComponent.WHEN_IN_FOCUSED_WINDOW); 
 		reactionsTable.registerKeyboardAction(reactionsRedoActionListener,reacRedo,JComponent.WHEN_FOCUSED); 
 
 		ActionListener metabolitesCopyActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				metabolitesCopy();
 			}
 		};
 
 		ActionListener metabolitesPasteActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				if (isRoot) {
 					metabolitesPaste();
 				}
 			}
 		};
 
 		ActionListener metabolitesClearActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				if (isRoot) {
 					metabolitesClear();
 				}				
 			}
 		};
 
 		ActionListener metabolitesFindActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				if (!findMode) {
 					showFindReplace();
 				}				
 				findReplaceItem.setEnabled(false);
 				findbutton.setEnabled(false);
 			}
 		};
 
 		ActionListener metabolitesUndoActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				if (undoItem.isEnabled()) {
 					undoButtonAction();
 				}
 			}
 		};
 
 		ActionListener metabolitesRedoActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				if (redoItem.isEnabled()) {
 					redoButtonAction();
 				}
 			}
 		};
 
 		findReplaceDialog.findButton.addActionListener(findMetabolitesButtonActionListener);
 		findReplaceDialog.findAllButton.addActionListener(findAllMetabolitesButtonActionListener);
 		findReplaceDialog.replaceButton.addActionListener(replaceMetabolitesButtonActionListener);
 		findReplaceDialog.replaceAllButton.addActionListener(replaceAllMetabolitesButtonActionListener);
 		//findReplaceDialog.replaceFindButton.addActionListener(replaceFindMetabolitesButtonActionListener);
 		findReplaceDialog.doneButton.addActionListener(findDoneButtonActionListener);
 		findReplaceDialog.caseCheckBox.addActionListener(matchCaseActionListener);
 		findReplaceDialog.wrapCheckBox.addActionListener(wrapAroundActionListener);
 		findReplaceDialog.selectedAreaCheckBox.addActionListener(selectedAreaActionListener);
 		findReplaceDialog.backwardsCheckBox.addActionListener(searchBackwardsActionListener);
 
 		KeyStroke metabCopy = KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK,false);       
 		KeyStroke metabPaste = KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK,false);
 		KeyStroke metabClear = KeyStroke.getKeyStroke(KeyEvent.VK_E,ActionEvent.CTRL_MASK,false);
 		KeyStroke metabFind = KeyStroke.getKeyStroke(KeyEvent.VK_F,ActionEvent.CTRL_MASK,false);
 		KeyStroke metabUndo = KeyStroke.getKeyStroke(KeyEvent.VK_Z,ActionEvent.CTRL_MASK,false);
 		KeyStroke metabRedo = KeyStroke.getKeyStroke(KeyEvent.VK_Y,ActionEvent.CTRL_MASK,false);
 
 		DefaultTableModel blankMetabModel = createBlankMetabolitesTableModel();
 		setUpMetabolitesTable(blankMetabModel);
 		LocalConfig.getInstance().getMetabolitesTableModelMap().put(GraphicalInterfaceConstants.DEFAULT_MODEL_NAME, blankMetabModel);
 		TableCellListener mtcl = new TableCellListener(metabolitesTable, metabAction);
 		MetabolitesPopupListener metabolitesPopupListener = new MetabolitesPopupListener();
 		metabolitesTable.addMouseListener(metabolitesPopupListener);
 		metabolitesTable.setRowHeight(20);
 		metabolitesTable.registerKeyboardAction(metabolitesCopyActionListener,metabCopy,JComponent.WHEN_FOCUSED); 
 		metabolitesTable.registerKeyboardAction(metabolitesPasteActionListener,metabPaste,JComponent.WHEN_FOCUSED); 
 		metabolitesTable.registerKeyboardAction(metabolitesClearActionListener,metabClear,JComponent.WHEN_FOCUSED);
 		metabolitesTable.registerKeyboardAction(metabolitesFindActionListener,metabFind,JComponent.WHEN_IN_FOCUSED_WINDOW);
 		metabolitesTable.registerKeyboardAction(metabolitesFindActionListener,metabFind,JComponent.WHEN_FOCUSED);
 		metabolitesTable.registerKeyboardAction(metabolitesUndoActionListener,metabUndo,JComponent.WHEN_IN_FOCUSED_WINDOW);
 		metabolitesTable.registerKeyboardAction(metabolitesUndoActionListener,metabUndo,JComponent.WHEN_FOCUSED);
 		metabolitesTable.registerKeyboardAction(metabolitesRedoActionListener,metabRedo,JComponent.WHEN_IN_FOCUSED_WINDOW);
 		metabolitesTable.registerKeyboardAction(metabolitesRedoActionListener,metabRedo,JComponent.WHEN_FOCUSED);
 
 		//setTableCellFocused(0, 1, metabolitesTable);
 		//setTableCellFocused(0, 1, reactionsTable);
 		formulaBar.setText((String) reactionsTable.getModel().getValueAt(0, 1));     			
 
 		DynamicTreePanel.treePanel.setNodeSelected(0);
 
 		/************************************************************************/
 		// end set up tables
 		/************************************************************************/
 
 		/************************************************************************/
 		// set up other components of gui
 		/************************************************************************/ 	
 
 		formulaBar.getDocument().addDocumentListener(new DocumentListener() {
 			public void changedUpdate(DocumentEvent e) {
 				setCellText();
 				fieldChangeAction();
 			}
 			public void removeUpdate(DocumentEvent e) {
 				setCellText();
 				fieldChangeAction();
 			}
 			public void insertUpdate(DocumentEvent e) {
 				setCellText();
 				fieldChangeAction();
 			}
 
 			public void setCellText() {
 				if (tabbedPane.getSelectedIndex() == 0 && reactionsTable.getSelectedRow() > -1 && reactionsTable.getSelectedColumn() > -1) {							
 					int viewRow = reactionsTable.convertRowIndexToModel(reactionsTable.getSelectedRow());					
 					if (formulaBarFocusGained) {
 						reactionsTable.getModel().setValueAt(formulaBar.getText(), viewRow, reactionsTable.getSelectedColumn());    							
 					}				
 				} else if (tabbedPane.getSelectedIndex() == 1 && metabolitesTable.getSelectedRow() > -1 && metabolitesTable.getSelectedColumn() > -1) {		
 					int viewRow = metabolitesTable.convertRowIndexToModel(metabolitesTable.getSelectedRow());
 					if (formulaBarFocusGained) {
 						try {
 							metabolitesTable.getModel().setValueAt(formulaBar.getText(), viewRow, metabolitesTable.getSelectedColumn());    							
 						} catch (Throwable t) {
 
 						}					
 					}
 				} 
 			}
 			public void fieldChangeAction() {
 				if (formulaBar.getText().length() > 0) {
 					formulaBarCutItem.setEnabled(true);
 					formulaBarCopyItem.setEnabled(true);
 					formulaBarDeleteItem.setEnabled(true);
 					formulaBarSelectAllItem.setEnabled(true);
 				} else {
 					formulaBarCutItem.setEnabled(false);
 					formulaBarCopyItem.setEnabled(false);
 					formulaBarDeleteItem.setEnabled(false);
 					formulaBarSelectAllItem.setEnabled(false);
 				}
 			}
 		});
 
 		formulaBar.addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				int key = e.getKeyCode();
 				if (key == KeyEvent.VK_ENTER) {  
 					// prevents editing of invisible id column for find events
 					if (tabbedPane.getSelectedIndex() == 0 && reactionsTable.getSelectedRow() > -1 && reactionsTable.getSelectedColumn() > 0) {	
 						try {
 							updateReactionsCell();
 						} catch (Throwable t) {
 
 						}						
 					} else if (tabbedPane.getSelectedIndex() == 1 && metabolitesTable.getSelectedRow() > -1 && metabolitesTable.getSelectedColumn() > 0) {
 						try {
 							updateMetabolitesCell();
 						} catch (Throwable t) {
 
 						}						
 					} 
 				}
 			}
 		}
 				);
 
 		formulaBar.addFocusListener(new FocusListener() {
 
 			@Override
 			public void focusGained(FocusEvent e) {	
 				Component c = e.getComponent();
 				if (c instanceof JTextField) {
 					((JTextField)c).selectAll();
 				}
 				if (tabbedPane.getSelectedIndex() == 0 && reactionsTable.getSelectedRow() > - 1) {
 					int viewRow = reactionsTable.convertRowIndexToModel(reactionsTable.getSelectedRow());
 					if (reactionsTable.getModel().getValueAt(viewRow, reactionsTable.getSelectedColumn()) != null) {
 						setTableCellOldValue((String) reactionsTable.getModel().getValueAt(viewRow, reactionsTable.getSelectedColumn()));    			
 					} else {
 						setTableCellOldValue("");
 					}
 					setCurrentReactionsRow(reactionsTable.getSelectedRow());
 					setCurrentReactionsColumn(reactionsTable.getSelectedColumn());
 				} else if (tabbedPane.getSelectedIndex() == 1 && metabolitesTable.getSelectedRow() > - 1) {
 					int viewRow = metabolitesTable.convertRowIndexToModel(metabolitesTable.getSelectedRow());
 					if (metabolitesTable.getModel().getValueAt(viewRow, metabolitesTable.getSelectedColumn()) != null) {
 						setTableCellOldValue((String) metabolitesTable.getModel().getValueAt(viewRow, metabolitesTable.getSelectedColumn()));    			
 					} else {
 						setTableCellOldValue("");
 					}
 					setCurrentMetabolitesRow(metabolitesTable.getSelectedRow());
 					setCurrentMetabolitesColumn(metabolitesTable.getSelectedColumn());
 				}
 
 				formulaBarFocusGained = true;				
 			}
 
 			@Override
 			public void focusLost(FocusEvent arg0) {
 				formulaBarFocusGained = false;
 				selectedCellChanged = true;	
 				if (getCurrentReactionsRow() > -1 && getCurrentReactionsColumn() > 0) {
 					if (tabbedPane.getSelectedIndex() == 0) {
 						int viewRow = reactionsTable.convertRowIndexToModel(getCurrentReactionsRow());
 						String newValue = "";                        
 						if (reactionsTable.getModel().getValueAt(viewRow, getCurrentReactionsColumn()) != null) {
 							newValue = (String) reactionsTable.getModel().getValueAt(viewRow, getCurrentReactionsColumn());
 							//System.out.println(newValue);
 						} 						
 						//ReactionUndoItem undoItem = createReactionUndoItem(getTableCellOldValue(), newValue, getCurrentRow(), getCurrentColumn(), viewRow + 1, UndoConstants.TYPING, UndoConstants.REACTION_UNDO_ITEM_TYPE);
 						updateReactionsCellIfValid(getTableCellOldValue(), newValue, viewRow, getCurrentReactionsColumn());
 						if (reactionUpdateValid) {
 							/*
 							if (undoItem.getColumn() == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 								if (reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) != null) {
 									undoItem.setNewValue(reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN).toString());
 								}				
 								if (reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN) != null) {
 									undoItem.setEquationNames(reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN).toString());
 								} else {
 									undoItem.setEquationNames("");
 								}				
 							}
 							setUpReactionsUndo(undoItem);
 							 */
 						}
 					} 
 				} else if (getCurrentMetabolitesRow() > -1 && getCurrentMetabolitesColumn() > 0) {
 					if (tabbedPane.getSelectedIndex() == 1) {
 						int viewRow = metabolitesTable.convertRowIndexToModel(getCurrentMetabolitesRow());
 						String newValue = "";                        
 						if (metabolitesTable.getModel().getValueAt(viewRow, getCurrentMetabolitesColumn()) != null) {
 							newValue = (String) metabolitesTable.getModel().getValueAt(viewRow, getCurrentMetabolitesColumn());
 							//System.out.println(newValue);
 						}
 						//MetaboliteUndoItem undoItem = createMetaboliteUndoItem(getTableCellOldValue(), newValue, getCurrentRow(), getCurrentColumn(), viewRow + 1, UndoConstants.TYPING, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);
 						//setUndoOldCollections(undoItem);
 						updateMetabolitesCellIfValid(getTableCellOldValue(), newValue, viewRow, getCurrentMetabolitesColumn());	
 						if (metaboliteUpdateValid) {
 							//setUpMetabolitesUndo(undoItem);
 						}
 					}
 				} 
 			}
 		});
 
 		final JPopupMenu formulaBarPopupMenu = new JPopupMenu(); 
 		formulaBarPopupMenu.add(formulaBarCutItem);
 		formulaBarPopupMenu.add(formulaBarCopyItem);
 		formulaBarPopupMenu.add(formulaBarPasteItem);
 		formulaBarPopupMenu.add(formulaBarDeleteItem);
 		formulaBarPopupMenu.addSeparator();
 		formulaBarPopupMenu.add(formulaBarSelectAllItem);
 		formulaBarCutItem.setEnabled(false);
 		formulaBarCopyItem.setEnabled(false);
 		formulaBarDeleteItem.setEnabled(false);
 		formulaBarSelectAllItem.setEnabled(false);
 		if (reactionsTable.getSelectedColumn() == GraphicalInterfaceConstants.REVERSIBLE_COLUMN || reactionsTable.getSelectedColumn() == GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN) {
 			formulaBarPasteItem.setEnabled(false);
 		} else {
 			formulaBarPasteItem.setEnabled(true);
 		}
 		formulaBarCutItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) { 
 				setClipboardContents(formulaBar.getSelectedText());
 				String selection = formulaBar.getSelectedText();	             
 				if(selection==null){
 					return;
 				}
 				formulaBar.replaceSelection("");				
 			}
 		});
 		formulaBarCopyItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) { 	
 				setClipboardContents(formulaBar.getSelectedText());
 			}
 		});
 		formulaBarPasteItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) { 	
 				try{
 					String clip_string = getClipboardContents(GraphicalInterface.this);
 					formulaBar.replaceSelection(clip_string);
 
 				}catch(Exception excpt){
 
 				}
 			}
 		});
 		formulaBarDeleteItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) { 	
 				formulaBar.setText("");
 			}
 		});
 		formulaBarSelectAllItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) { 	
 				formulaBar.selectAll();
 			}
 		});
 
 		formulaBar.addMouseListener(new MouseAdapter() {
 
 			public void mousePressed(MouseEvent e)  {check(e);}
 			public void mouseReleased(MouseEvent e) {check(e);}
 
 			public void check(MouseEvent e) {
 				if (e.isPopupTrigger()) { //if the event shows the menu
 					formulaBarPopupMenu.show(formulaBar, e.getX(), e.getY()); 
 				}
 			}
 		}); 	  
 
 		JScrollPane scrollPaneReac = new JScrollPane(reactionsTable);
 		LineNumberTableRowHeader tableLineNumber = new LineNumberTableRowHeader(scrollPaneReac, reactionsTable);
 		tableLineNumber.setBackground(new Color(240, 240, 240));
 		scrollPaneReac.setRowHeaderView(tableLineNumber);
 		JLabel rowLabel = new JLabel(GraphicalInterfaceConstants.ROW_HEADER_TITLE);
 		rowLabel.setFont(rowLabel.getFont().deriveFont(Font.PLAIN));		
 		scrollPaneReac.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowLabel);
 		tabbedPane.addTab(GraphicalInterfaceConstants.DEFAULT_REACTION_TABLE_TAB_NAME, scrollPaneReac);
 		tabbedPane.setMnemonicAt(0, KeyEvent.VK_R);
 
 		JScrollPane scrollPaneMetab = new JScrollPane(metabolitesTable);
 		LineNumberTableRowHeader tableMetabLineNumber = new LineNumberTableRowHeader(scrollPaneMetab, metabolitesTable);
 		tableMetabLineNumber.setBackground(new Color(240, 240, 240));
 		scrollPaneMetab.setRowHeaderView(tableMetabLineNumber);		
 		JLabel metabRowLabel = new JLabel(GraphicalInterfaceConstants.ROW_HEADER_TITLE);
 		metabRowLabel.setFont(rowLabel.getFont().deriveFont(Font.PLAIN));		
 		scrollPaneMetab.setCorner(JScrollPane.UPPER_LEFT_CORNER, metabRowLabel);
 		tabbedPane.addTab(GraphicalInterfaceConstants.DEFAULT_METABOLITE_TABLE_TAB_NAME, scrollPaneMetab);
 		tabbedPane.setMnemonicAt(1, KeyEvent.VK_B);  
 
 		tabbedPane.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				GraphicalInterface.this.requestFocus();
 				tabChanged = true;
 				int tabIndex = tabbedPane.getSelectedIndex();
 				String reactionRow = Integer.toString((reactionsTable.getSelectedRow() + 1));
 				String metaboliteRow = Integer.toString((metabolitesTable.getSelectedRow() + 1));
 				if (tabIndex == 0 && reactionsTable.getSelectedRow() > - 1) {
 					selectedCellChanged = true;
 					if (LocalConfig.getInstance().getSuspiciousMetabolites().size() > 0) {
 						setLoadErrorMessage("Model contains suspicious metabolites.");
 						statusBar.setText("Row " + reactionRow + "                   " + getLoadErrorMessage());
 					} else {
 						statusBar.setText("Row " + reactionRow);
 					}
 					// prevents invisible id column from setting id in formulaBar for find events
 					if (reactionsTable.getSelectedRow() > -1 && reactionsTable.getSelectedColumn() > 0) {
 						int viewRow = reactionsTable.convertRowIndexToModel(reactionsTable.getSelectedRow());
 						formulaBar.setText((String) reactionsTable.getModel().getValueAt(viewRow, reactionsTable.getSelectedColumn()));
 						setTableCellOldValue(formulaBar.getText());
 					} 
 					enableOrDisableReactionsItems();
 				} else if (tabIndex == 1 && metabolitesTable.getSelectedRow() > - 1) {
 					selectedCellChanged = true;
 					if (LocalConfig.getInstance().getSuspiciousMetabolites().size() > 0) {
 						setLoadErrorMessage("Model contains suspicious metabolites.");
 						statusBar.setText("Row " + metaboliteRow + "                   " + getLoadErrorMessage());
 					} else {
 						statusBar.setText("Row " + metaboliteRow);
 					}					
 					if (metabolitesTable.getSelectedRow() > -1 && metabolitesTable.getSelectedColumn() > 0) {
 						int viewRow = metabolitesTable.convertRowIndexToModel(metabolitesTable.getSelectedRow());
 						formulaBar.setText((String) metabolitesTable.getModel().getValueAt(viewRow, metabolitesTable.getSelectedColumn())); 
 						setTableCellOldValue(formulaBar.getText());
 					}
 					enableOrDisableMetabolitesItems();
 				} else {
 					if (LocalConfig.getInstance().getSuspiciousMetabolites().size() > 0) {
 						setLoadErrorMessage("Model contains suspicious metabolites.");
 						statusBar.setText("Row 1" + "                   " + getLoadErrorMessage());
 					} else {
 						statusBar.setText("Row 1");
 					}
 					formulaBar.setText("");
 				}
 			}
 		});
 
 		statusBar.setFont(statusBar.getFont().deriveFont(Font.PLAIN));
 
 		/************************************************************************/
 		//end of set up other components of gui
 		/************************************************************************/
 
 		/************************************************************************/
 		//set frame layout - using TableLayout http://www.clearthought.info/sun/products/jfc/tsc/articles/tablelayout/Simple.html
 		/************************************************************************/		
 
 		double border = 10;
 		double size[][] =
 			{{border, TableLayout.FILL, 20, 0.20, border},  //Columns
 				{border, 0.06, 10, 0.04, 10, TableLayout.FILL, 10, 0.15, 5, 0.02, border}}; // Rows
 
 		setLayout (new TableLayout(size)); 
 
 		add (toolbar, "0, 1, 4, 1");
 		add (formulaBar, "1, 3, 1, 1");
 		add (tabbedPane, "1, 5, 1, 1"); // Left
 		//add (rightPane, "3, 3, 1, 7"); // Right
 		add (treePanel, "3, 3, 1, 7"); // Right
 		add (outputPane, "1, 7, 1, 1"); // Bottom
 		add (statusBar, "1, 9, 3, 1");
 
 		setBackground(Color.lightGray);
 	}
 	/********************************************************************************/
 	//end constructor and layout
 	/********************************************************************************/
 
 	/*******************************************************************************/
 	//begin formulaBar methods and actions
 	/*******************************************************************************/ 
 
 	public void updateReactionsCell() {
 		if (formulaBar.getText() != null) {
 			LocalConfig.getInstance().reactionsTableChanged = true;
 		}						
 		int viewRow = reactionsTable.convertRowIndexToModel(reactionsTable.getSelectedRow());
 		String newValue = formulaBar.getText();
 		ReactionUndoItem undoItem = createReactionUndoItem(getTableCellOldValue(), newValue, reactionsTable.getSelectedRow(), reactionsTable.getSelectedColumn(), viewRow + 1, UndoConstants.TYPING, UndoConstants.REACTION_UNDO_ITEM_TYPE);
 		updateReactionsCellIfValid(getTableCellOldValue(), newValue, viewRow, reactionsTable.getSelectedColumn());
 		if (reactionUpdateValid) {
 			if (undoItem.getColumn() == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 				if (reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) != null) {
 					undoItem.setNewValue(reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN).toString());
 				}				
 				if (reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN) != null) {
 					undoItem.setEquationNames(reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN).toString());
 				} else {
 					undoItem.setEquationNames("");
 				}				
 			}
 			setUpReactionsUndo(undoItem);	
 		} 
 	}
 
 	public void updateMetabolitesCell() {
 		if (formulaBar.getText() != null) {
 			LocalConfig.getInstance().metabolitesTableChanged = true;
 		}						
 		int viewRow = metabolitesTable.convertRowIndexToModel(metabolitesTable.getSelectedRow());		
 		String newValue = formulaBar.getText();
 		// these variables are needed since after the table is reloaded, there is
 		// no selected cell
 		int row = metabolitesTable.getSelectedRow();
 		int col = metabolitesTable.getSelectedColumn();
 		MetaboliteUndoItem undoItem = createMetaboliteUndoItem(getTableCellOldValue(), newValue, row, col, viewRow + 1, UndoConstants.TYPING, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);
 		setUndoOldCollections(undoItem);
 		updateMetabolitesCellIfValid(getTableCellOldValue(), newValue, viewRow, metabolitesTable.getSelectedColumn());	
 		if (metaboliteUpdateValid) {
 			setUndoNewCollections(undoItem);
 			setUpMetabolitesUndo(undoItem);
 		} 	
 	}
 
 	/*******************************************************************************/
 	//end formulaBar methods and actions
 	/*******************************************************************************/ 
 
 	/*******************************************************************************/
 	// begin Model menu methods and actions
 	/*******************************************************************************/ 
 
 	/*******************************************************************************/
 	// load methods and actions
 	/*******************************************************************************/ 
 
 	class LoadSBMLAction implements ActionListener {
 		public void actionPerformed(ActionEvent ae) {
 			SaveChangesPrompt();
 			if (openFileChooser) {
 				JTextArea output = null;
 				JFileChooser fileChooser = new JFileChooser();
 				fileChooser.setDialogTitle("Load SBML File"); 
 				fileChooser.setFileFilter(new SBMLFileFilter());
 				fileChooser.setFileFilter(new XMLFileFilter());
 				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);			
 				//TODO: test the possibility of a global FileChooser
 
 				String lastSBML_path = curSettings.get("LastSBML");
 				if (lastSBML_path == null) {
 					lastSBML_path = ".";
 				}
 				fileChooser.setCurrentDirectory(new File(lastSBML_path));
 
 				//SBMLDocument doc = new SBMLDocument();
 				//SBMLReader reader = new SBMLReader();  
 				//... Open a file dialog.
 				int retval = fileChooser.showOpenDialog(output);
 				if (retval == JFileChooser.APPROVE_OPTION) {
 					loadSetUp();
 					//... The user selected a file, get it, use it.
 					File file = fileChooser.getSelectedFile();
 					String rawPathName = file.getAbsolutePath();
 					curSettings.add("LastSBML", rawPathName);
 
 					String rawFilename = file.getName();				
 					if (!rawFilename.endsWith(".xml") && !rawFilename.endsWith(".sbml")) {
 						JOptionPane.showMessageDialog(null,                
 								"Not a Valid SBML File.",                
 								"Invalid SBML File",                                
 								JOptionPane.ERROR_MESSAGE);
 						validFile = false;
 					} else {
 						listModel.clear();						
 						DynamicTreePanel.treePanel.clear();
 						setFileType("sbml");
 						String filename;
 						if (rawFilename.endsWith(".xml")) {
 							filename = rawFilename.substring(0, rawFilename.length() - 4);
 						} else {
 							filename = rawFilename.substring(0, rawFilename.length() - 5);
 						}
 						setSBMLFile(file);
 						LocalConfig.getInstance().setModelName(filename);
 						LocalConfig.getInstance().setProgress(0);
 						progressBar.setVisible(true);
 
 						timer.start();
 
 						task = new Task();
 						task.execute();
 					}
 				}
 			}			
 		}
 	}
 
 	class LoadCSVAction implements ActionListener {
 		public void actionPerformed(ActionEvent ae) {
 			SaveChangesPrompt();
 			if (openFileChooser) {
 				//setExtension(".csv");	
 				csvLoadInterface.textMetabField.setText("");
 				csvLoadInterface.textReacField.setText("");
 				LocalConfig.getInstance().setMetabolitesCSVFile(null);
 				LocalConfig.getInstance().hasMetabolitesFile = false;
 				LocalConfig.getInstance().hasReactionsFile = false;
 				csvLoadInterface.okButton.setEnabled(false);
 				csvLoadInterface.setVisible(true);
 			}				
 		}
 	}
 
 	ActionListener okButtonCSVLoadActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {	
 			csvLoadInterface.setVisible(false);
 			csvLoadInterface.dispose();	
 			loadSetUp();
 			//isCSVFile = true;
 			loadCSV();
 		}
 	}; 
 
 	ActionListener cancelButtonCSVLoadActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {	
 			csvLoadInterface.setVisible(false);
 		}
 	};
 
 	public void loadCSV() {	
 		//DynamicTreePanel.treePanel.clear();
 		//listModel.clear();
 		LocalConfig.getInstance().setMetabolitesNextRowCorrection(0);
 
 		if (LocalConfig.getInstance().hasMetabolitesFile && LocalConfig.getInstance().getMetabolitesCSVFile() != null) {
 			TextMetabolitesModelReader reader = new TextMetabolitesModelReader();
 			ArrayList<String> columnNamesFromFile = reader.columnNamesFromFile(LocalConfig.getInstance().getMetabolitesCSVFile(), 0);
 			MetaboliteColumnNameInterface columnNameInterface = new MetaboliteColumnNameInterface(columnNamesFromFile);
 			setMetaboliteColumnNameInterface(columnNameInterface);
 			getMetaboliteColumnNameInterface().setIconImages(icons);					
 			getMetaboliteColumnNameInterface().setSize(600, 330);
 			getMetaboliteColumnNameInterface().setResizable(false);
 			getMetaboliteColumnNameInterface().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 			getMetaboliteColumnNameInterface().setLocationRelativeTo(null);											
 			getMetaboliteColumnNameInterface().addWindowListener(new WindowAdapter() {
 				public void windowClosing(WindowEvent evt) {
 					metaboliteColumnNameCloseAction();	        	
 				}
 			});
 			getMetaboliteColumnNameInterface().okButton.addActionListener(okButtonCSVMetabLoadActionListener);
 			getMetaboliteColumnNameInterface().cancelButton.addActionListener(cancelButtonCSVMetabLoadActionListener);
 			getMetaboliteColumnNameInterface().setModal(true);
 			getMetaboliteColumnNameInterface().setVisible(true);
 		} else {
 			loadReactionColumnNameInterface();
 		} 		
 	}
 
 	public void metaboliteColumnNameCloseAction() {
 		getMetaboliteColumnNameInterface().setVisible(false);
 		getMetaboliteColumnNameInterface().dispose();
 		/*
 		DynamicTreePanel.treePanel.clear();
 		listModel.clear();
 		DefaultTableModel blankMetabModel = createBlankMetabolitesTableModel();
 		setUpMetabolitesTable(blankMetabModel);
 		LocalConfig.getInstance().getMetabolitesTableModelMap().put(LocalConfig.getInstance().getModelName(), blankMetabModel);
 		 */
 		if (LocalConfig.getInstance().hasReactionsFile) {
 			loadReactionColumnNameInterface();
 		} else {
 			//LocalConfig.getInstance().setModelName(GraphicalInterfaceConstants.DEFAULT_MODEL_NAME);
 			//clearTables();
 		}
 	}
 
 	ActionListener okButtonCSVMetabLoadActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			getMetaboliteColumnNameInterface().getColumnIndices();
 			getMetaboliteColumnNameInterface().setVisible(false);
 			getMetaboliteColumnNameInterface().dispose();
 
 			DynamicTreePanel.treePanel.clear();
 			listModel.clear();
 			setFileType("csv");
 
 			TextMetabolitesModelReader reader = new TextMetabolitesModelReader();
 			reader.load(LocalConfig.getInstance().getMetabolitesCSVFile());	
 			setUpMetabolitesTable(reader.getMetabolitesTableModel());
 			LocalConfig.getInstance().getMetabolitesTableModelMap().put(LocalConfig.getInstance().getModelName(), reader.getMetabolitesTableModel());
 			//System.out.println(LocalConfig.getInstance().getMetabolitesTableModelMap());
 			if (LocalConfig.getInstance().hasReactionsFile) {
 				loadReactionColumnNameInterface();
 			} else {
 				DefaultTableModel blankReacModel = createBlankReactionsTableModel();
 				setUpReactionsTable(blankReacModel);
 				LocalConfig.getInstance().getReactionsTableModelMap().put(LocalConfig.getInstance().getModelName(), blankReacModel);
 				setUpTables();
 			}			
 		}
 	};
 
 	ActionListener cancelButtonCSVMetabLoadActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			metaboliteColumnNameCloseAction();
 		}
 	};
 
 	public void loadReactionColumnNameInterface() {
 		/*
 		if (!LocalConfig.getInstance().hasMetabolitesFile) {
 			DefaultTableModel blankMetabModel = createBlankMetabolitesTableModel();
 			setUpMetabolitesTable(blankMetabModel);
 			LocalConfig.getInstance().getMetabolitesTableModelMap().put(LocalConfig.getInstance().getModelName(), blankMetabModel);
 		}
 		 */
 		LocalConfig.getInstance().setReactionsNextRowCorrection(0);
 
 		TextReactionsModelReader reader = new TextReactionsModelReader();			    
 		ArrayList<String> columnNamesFromFile = reader.columnNamesFromFile(LocalConfig.getInstance().getReactionsCSVFile(), 0);	
 		ReactionColumnNameInterface columnNameInterface = new ReactionColumnNameInterface(columnNamesFromFile);
 		setReactionColumnNameInterface(columnNameInterface);
 		getReactionColumnNameInterface().setIconImages(icons);					
 		getReactionColumnNameInterface().setSize(600, 650);
 		getReactionColumnNameInterface().setResizable(false);
 		getReactionColumnNameInterface().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		getReactionColumnNameInterface().setLocationRelativeTo(null);											
 		getReactionColumnNameInterface().addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent evt) {
 				getReactionColumnNameInterface().setVisible(false);
 				getReactionColumnNameInterface().dispose();
 				if (LocalConfig.getInstance().hasMetabolitesFile) {
 					csvReactionCancelLoadAction();
 				} else {
 					clearTables();
 				}        	
 			}
 		});
 		getReactionColumnNameInterface().okButton.addActionListener(okButtonCSVReacLoadActionListener);
 		getReactionColumnNameInterface().cancelButton.addActionListener(cancelButtonCSVReacLoadActionListener);
 		getReactionColumnNameInterface().setModal(true);
 		getReactionColumnNameInterface().setVisible(true);	
 	}
 
 	ActionListener okButtonCSVReacLoadActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			getReactionColumnNameInterface().getColumnIndices();
 			getReactionColumnNameInterface().setVisible(false);
 			getReactionColumnNameInterface().dispose();
 
 			DynamicTreePanel.treePanel.clear();
 			listModel.clear();
 			setFileType("csv");
 
 			TextReactionsModelReader reader = new TextReactionsModelReader();
 			reader.load(LocalConfig.getInstance().getReactionsCSVFile());	
 			setUpReactionsTable(reader.getReactionsTableModel());
 			LocalConfig.getInstance().getReactionsTableModelMap().put(LocalConfig.getInstance().getModelName(), reader.getReactionsTableModel());
 			// sets updated model if any metabolites added in reactions load
 			setUpMetabolitesTable(reader.getMetabolitesTableModel());
 			LocalConfig.getInstance().getMetabolitesTableModelMap().put(LocalConfig.getInstance().getModelName(), reader.getMetabolitesTableModel());
 			setUpTables();
 		}
 	};
 
 	ActionListener cancelButtonCSVReacLoadActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			csvReactionCancelLoadAction();
 		}
 	};
 
 	public void csvReactionCancelLoadAction() {
 		reactionCancelLoad = true;
 	}
 
 	class LoadExistingItemAction implements ActionListener {
 		public void actionPerformed(ActionEvent ae) {
 			SaveChangesPrompt();
 			if (openFileChooser) {
 				File f = new File("ModelCollection.csv");
 				ModelCollectionTable mcTable = new ModelCollectionTable(f);
 				mcTable.setIconImages(icons);
 				mcTable.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 				mcTable.setAlwaysOnTop(true);				
 				mcTable.setLocationRelativeTo(null);
 				mcTable.setVisible(true);
 				setModelCollectionTable(mcTable);
 				ModelCollectionTable.okButton.addActionListener(modelCollectionOKButtonActionListener);
 				ModelCollectionTable.cancelButton.addActionListener(modelCollectionCancelButtonActionListener);
 				loadExistingItem.setEnabled(false);
 				mcTable.addWindowListener(new WindowAdapter() {
 					public void windowClosing(WindowEvent evt) {
 						loadExistingItem.setEnabled(true);
 						getModelCollectionTable().setVisible(false);
 						getModelCollectionTable().dispose();
 					}
 				});	
 			}			
 		}
 	}
 
 	ActionListener modelCollectionOKButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {			
 			if (!modelCollectionOKButtonClicked) {
 				if (openFileChooser) {
 					loadSetUp();
 					listModel.clear();
 					DynamicTreePanel.treePanel.clear();
 					setFileType("sbml");
 					String path = getModelCollectionTable().getPath();
 					File file = new File(path);
 					setSBMLFile(file);
 					LocalConfig.getInstance().setModelName(getModelCollectionTable().getFileName());
 					LocalConfig.getInstance().setProgress(0);
 					progressBar.setVisible(true);
 
 					timer.start();
 
 					task = new Task();
 					task.execute();
 
 					loadExistingItem.setEnabled(true);
 
 					modelCollectionOKButtonClicked = true;					
 				}				
 			}			
 		}
 	};
 
 	ActionListener modelCollectionCancelButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {						
 			loadExistingItem.setEnabled(true);						
 		}
 	};
 
 	/*******************************************************************************/
 	//end load methods and actions
 	/*******************************************************************************/
 
 	/*******************************************************************************/
 	//save methods and actions
 	/*******************************************************************************/
 
 	class SaveSBMLItemAction implements ActionListener {
 		public void actionPerformed(ActionEvent ae) {
 			try {
 				JSBMLWriter jWrite = new JSBMLWriter();
 
 				jWrite.formConnect(LocalConfig.getInstance());
 
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	class SaveCSVMetabolitesItemAction implements ActionListener {
 		public void actionPerformed(ActionEvent ae) {
 			saveMetabolitesTextFileChooser();
 		}
 	}
 
 	public void saveMetabolitesTextFile(String path, String filename) {
 		TextMetabolitesWriter writer = new TextMetabolitesWriter();
 		writer.write(path);				    		
 		setTitle(GraphicalInterfaceConstants.TITLE + " - " + filename);
 		//list holds dateTime stamps from items in old tree
 		ArrayList<String> suffixList = new ArrayList<String>();
 		for (int i = 1; i < listModel.size(); i++) {
 			//length of dateTime stamp is 14
 			String suffix = listModel.get(i).substring(listModel.get(i).length() - 14);
 			suffixList.add(suffix);
 		}
 		listModel.clear();
 		listModel.addElement(filename);
 		/*
 		DatabaseCopier copier = new DatabaseCopier();
 		//copies files and assigns new names based on name of saved file to
 		//refresh tree with new names
 		for (int i = 0; i < suffixList.size(); i++) {
 			listModel.addElement(GraphicalInterfaceConstants.OPTIMIZATION_PREFIX + filename + suffixList.get(i));
 			DynamicTreePanel.treePanel.addObject(new Solution(GraphicalInterfaceConstants.OPTIMIZATION_PREFIX + filename + suffixList.get(i)));
 			copier.copyDatabase(GraphicalInterfaceConstants.OPTIMIZATION_PREFIX + oldName + suffixList.get(i), GraphicalInterfaceConstants.OPTIMIZATION_PREFIX + filename + suffixList.get(i));
 			copier.copyLogFile(GraphicalInterfaceConstants.OPTIMIZATION_PREFIX + oldName + suffixList.get(i), GraphicalInterfaceConstants.OPTIMIZATION_PREFIX + filename + suffixList.get(i));
 		}
 		 */
 
 		clearOutputPane();
 	}
 
 	public void saveMetabolitesTextFileChooser() {
 		JTextArea output = null;
 		JFileChooser fileChooser = new JFileChooser(new File(LocalConfig.getInstance().getModelName()));
 		fileChooser.setDialogTitle("Save CSV Metabolites File");
 		fileChooser.setFileFilter(new CSVFileFilter());
 		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 
 		String lastCSV_path = curSettings.get("LastCSV");
 		if (lastCSV_path == null) {
 			lastCSV_path = ".";	
 		}
 		fileChooser.setCurrentDirectory(new File(lastCSV_path));
 
 		boolean done = false;
 		while (!done) {
 			//... Open a file dialog.
 			int retval = fileChooser.showSaveDialog(output);
 			if (retval == JFileChooser.CANCEL_OPTION) {
 				done = true;
 				exit = false;
 			}
 			if (retval == JFileChooser.APPROVE_OPTION) {
 				//... The user selected a file, get it, use it.
 				String rawPathName = fileChooser.getSelectedFile().getAbsolutePath();
 				//curSettings.add("LastCSV", rawPathName);
 
 				LocalConfig.getInstance().hasMetabolitesFile = true;
 
 				//checks if filename endswith .csv else renames file to end with .csv
 				String path = fileChooser.getSelectedFile().getPath();
 				String filename = fileChooser.getSelectedFile().getName();
 				if (!path.endsWith(".csv")) {
 					path = path + ".csv";
 				}
 
 				File file = new File(path);
 				if (path == null) {
 					done = true;
 				} else {        	    	  
 					if (file.exists()) {
 						int confirmDialog = JOptionPane.showConfirmDialog(fileChooser, "Replace existing file?");
 						if (confirmDialog == JOptionPane.YES_OPTION) {
 							done = true;
 
 							saveMetabolitesTextFile(path, filename);
 
 						} else if (confirmDialog == JOptionPane.NO_OPTION) {        		    	  
 							done = false;
 						} else {
 							done = true;
 						}       		    	  
 					} else {
 						done = true;
 
 						saveMetabolitesTextFile(path, filename);
 					}
 				}			                  	  
 			}
 		}
 	}
 
 	class SaveCSVReactionsItemAction implements ActionListener {
 		public void actionPerformed(ActionEvent ae) {
 			// Message box displayed if model contains invalid reactions
 			if (LocalConfig.getInstance().getInvalidReactions().size() > 0) {
 				Object[] options = {"    Yes    ", "    No    ",};
 				int choice = JOptionPane.showOptionDialog(null, 
 						GraphicalInterfaceConstants.INVALID_REACTIONS_ERROR_MESSAGE, 
 						GraphicalInterfaceConstants.INVALID_REACTIONS_ERROR_TITLE, 
 						JOptionPane.YES_NO_OPTION, 
 						JOptionPane.QUESTION_MESSAGE, 
 						null, options, options[0]);
 				if (choice == JOptionPane.YES_OPTION) {
 					saveReactionsTextFileChooser(); 
 				}
 				if (choice == JOptionPane.NO_OPTION) {
 
 				}							
 			} else {
 				saveReactionsTextFileChooser(); 
 			}
 		}
 	}
 
 	public void saveReactionsTextFile(String path, String filename) {
 		//String oldName = getDatabaseName();
 		TextReactionsWriter writer = new TextReactionsWriter();
 		writer.write(path);				    
 		setTitle(GraphicalInterfaceConstants.TITLE + " - " + filename);	
 		if (!saveOptFile) {
 			//list holds dateTime stamps from items in old tree
 			ArrayList<String> suffixList = new ArrayList<String>();
 			for (int i = 1; i < listModel.size(); i++) {
 				//length of dateTime stamp is 14
 				String suffix = listModel.get(i).substring(listModel.get(i).length() - 14);
 				suffixList.add(suffix);
 			}
 			listModel.clear();
 			listModel.addElement(filename);
 			/*
 			DatabaseCopier copier = new DatabaseCopier();
 			for (int i = 0; i < suffixList.size(); i++) {
 				listModel.addElement(GraphicalInterfaceConstants.OPTIMIZATION_PREFIX + filename + suffixList.get(i));
 				copier.copyDatabase(GraphicalInterfaceConstants.OPTIMIZATION_PREFIX + oldName + suffixList.get(i), GraphicalInterfaceConstants.OPTIMIZATION_PREFIX + filename + suffixList.get(i));
 				copier.copyLogFile(GraphicalInterfaceConstants.OPTIMIZATION_PREFIX + oldName + suffixList.get(i), GraphicalInterfaceConstants.OPTIMIZATION_PREFIX + filename + suffixList.get(i));
 			}
 
 			DynamicTreePanel.treePanel.addObject(new Solution(GraphicalInterface.listModel.get(GraphicalInterface.listModel.getSize() - 1)));
 			 */
 			clearOutputPane();
 		}	
 		saveOptFile = false;
 	}
 
 	public void saveReactionsTextFileChooser() {
 		JTextArea output = null;
 		JFileChooser fileChooser = new JFileChooser(new File(LocalConfig.getInstance().getModelName()));
 		fileChooser.setDialogTitle("Save CSV Reactions File");
 		fileChooser.setFileFilter(new CSVFileFilter());
 		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 
 		String lastCSV_path = curSettings.get("LastCSV");
 		if (lastCSV_path == null) {
 			lastCSV_path = ".";	
 		}
 		fileChooser.setCurrentDirectory(new File(lastCSV_path));
 
 		boolean done = false;
 		while (!done) {
 			//... Open a file dialog.
 			File file = null;
 			if (saveOptFile) {
 				file = new File(DynamicTreePanel.treePanel.getTree().getLastSelectedPathComponent().toString());
 				fileChooser.setSelectedFile(file);
 			}
 			int retval = fileChooser.showSaveDialog(output);
 			if (retval == JFileChooser.CANCEL_OPTION) {
 				done = true;
 				exit = false;
 			}
 			if (retval == JFileChooser.APPROVE_OPTION) {            	  
 				//... The user selected a file, get it, use it.
 				String rawPathName = fileChooser.getSelectedFile().getAbsolutePath();
 				//curSettings.add("LastCSV", rawPathName);
 
 				LocalConfig.getInstance().hasReactionsFile = true;
 
 				String path = "";
 				String filename = "";
 
 				//checks if filename endswith .csv else renames file to end with .csv
 				path = fileChooser.getSelectedFile().getPath();
 				filename = fileChooser.getSelectedFile().getName();
 
 				if (!path.endsWith(".csv")) {
 					path = path + ".csv";
 				}
 
 				file = new File(path);
 
 				if (path == null) {
 					done = true;
 				} else {        	    	  
 					if (file.exists()) {
 						int confirmDialog = JOptionPane.showConfirmDialog(fileChooser, "Replace existing file?");
 						if (confirmDialog == JOptionPane.YES_OPTION) {
 							done = true;
 
 							saveReactionsTextFile(path, filename);
 
 						} else if (confirmDialog == JOptionPane.NO_OPTION) {        		    	  
 							done = false;
 						} else {
 							done = true;
 						}       		    	  
 					} else {
 						done = true;
 
 						saveReactionsTextFile(path, filename);
 					}
 				}			                  	  
 			}
 		}
 	}
 
 	class ClearAction implements ActionListener {
 		public void actionPerformed(ActionEvent cae) {
 			LocalConfig.getInstance().setModelName(GraphicalInterfaceConstants.DEFAULT_MODEL_NAME);
 			clearTables();
 		}
 	}
 
 	public void clearTables() {
 		SaveChangesPrompt();
 		loadSetUp();
 		listModel.clear();
 		DynamicTreePanel.treePanel.clear();
 		DefaultTableModel blankReacModel = createBlankReactionsTableModel();
 		setUpReactionsTable(blankReacModel);
 		LocalConfig.getInstance().getReactionsTableModelMap().put(GraphicalInterfaceConstants.DEFAULT_MODEL_NAME, blankReacModel);
 		DefaultTableModel blankMetabModel = createBlankMetabolitesTableModel();
 		setUpMetabolitesTable(blankMetabModel);
 		LocalConfig.getInstance().getMetabolitesTableModelMap().put(GraphicalInterfaceConstants.DEFAULT_MODEL_NAME, blankMetabModel);
 		//LocalConfig.getInstance().setModelName(GraphicalInterfaceConstants.DEFAULT_MODEL_NAME);
 		setUpTables();
 	}
 
 	class SBMLFileFilter extends javax.swing.filechooser.FileFilter {
 		public boolean accept(File f) {
 			return f.isDirectory() || f.getName().toLowerCase().endsWith(".sbml");
 		}
 
 		public String getDescription() {
 			return ".sbml files";
 		}
 	}
 
 	class XMLFileFilter extends javax.swing.filechooser.FileFilter {
 		public boolean accept(File f) {
 			return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
 		}
 
 		public String getDescription() {
 			return ".xml files";
 		}
 	}
 
 	class SQLiteFileFilter extends javax.swing.filechooser.FileFilter {
 		public boolean accept(File f) {
 			return f.isDirectory() || f.getName().toLowerCase().endsWith(".db");
 		}
 
 		public String getDescription() {
 			return "SQLite .db files";
 		}
 	}
 
 	class CSVFileFilter extends javax.swing.filechooser.FileFilter {
 		public boolean accept(File f) {
 			return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
 		}
 
 		public String getDescription() {
 			return ".csv files";
 		}
 	}
 
 	class JarFileFilter extends javax.swing.filechooser.FileFilter {
 		public boolean accept(File f) {
 			return f.isDirectory() || f.getName().toLowerCase().endsWith(".jar");
 		}
 
 		public String getDescription() {
 			return ".jar files";
 		}
 	}
 
 	class ExitAction implements ActionListener {
 		public void actionPerformed(ActionEvent cae) {
 			SaveChangesPrompt();
 			if (exit) {
 				// Exit the application
 				System.exit(0);	
 			}			
 		}
 	}
 
 	public void SaveChangesPrompt() {
 		Utilities util = new Utilities();
 		boolean saveChanges = true;
 		openFileChooser = true;
 		if (LocalConfig.getInstance().metabolitesTableChanged || LocalConfig.getInstance().reactionsTableChanged || LocalConfig.getInstance().getOptimizationFilesList().size() > 0) {
 			Object[] options = {"  Yes  ", "   No   ", "Cancel"};
 			String message = "";
 			String suffix = " Save changes?";
 			if (LocalConfig.getInstance().getOptimizationFilesList().size() > 0) {
 				message = "Optimizations have not been saved. ";
 			}
 			if (LocalConfig.getInstance().metabolitesTableChanged && LocalConfig.getInstance().reactionsTableChanged) {
 				message += "Reactions table and Metabolites table changed." + suffix;
 			} else if (LocalConfig.getInstance().reactionsTableChanged) {
 				message += "Reactions table changed." + suffix;
 			} else if (LocalConfig.getInstance().metabolitesTableChanged) {
 				message += "Metabolites table changed." + suffix;
 			} else {
 				message += suffix;
 			}
 			int choice = JOptionPane.showOptionDialog(null, 
 					message, 
 					"Save Changes?",  
 					JOptionPane.YES_NO_CANCEL_OPTION, 
 					JOptionPane.QUESTION_MESSAGE, 
 					null, options, options[0]);
 			//options[0] sets "Yes" as default button
 			// interpret the user's choice	  
 			if (choice == JOptionPane.YES_OPTION)
 			{
 				if (LocalConfig.getInstance().metabolitesTableChanged) {
 					saveMetabolitesTextFileChooser();
 					LocalConfig.getInstance().metabolitesTableChanged = false;
 				}
 				if (LocalConfig.getInstance().reactionsTableChanged) {
 					saveReactionsTextFileChooser();
 					LocalConfig.getInstance().reactionsTableChanged = false;
 				}				
 				if (LocalConfig.getInstance().getOptimizationFilesList().size() > 0) {				
 					for (int i = 0; i < LocalConfig.getInstance().getOptimizationFilesList().size(); i++) {
 						saveOptFile = true;
 						if (getFileType().equals("csv")) {
 							saveReactionsTextFileChooser();
 						} else if (getFileType().equals("sbml")) {
 							try {
 								JSBMLWriter jWrite = new JSBMLWriter();
 								jWrite.setOptFilePath(DynamicTreePanel.treePanel.getTree().getLastSelectedPathComponent().toString());
 								jWrite.formConnect(LocalConfig.getInstance());
 								
 							} catch (Exception e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						} 
 					}
 				}
 				deleteAllOptimizationFiles();
 				LocalConfig.getInstance().getOptimizationFilesList().clear();
 				exit = true;
 				//System.exit(0);
 			}
 			if (choice == JOptionPane.NO_OPTION)
 			{
 				//TODO: if "_orig" db exists rename to db w/out "_orig", delete db w/out "_orig"
 				// or delete db
 				deleteAllOptimizationFiles();
 				exit = true;
 				//System.exit(0);
 				saveChanges = false;
 			}
 			if (choice == JOptionPane.CANCEL_OPTION) {
 				exit = false;
 				openFileChooser = false;
 			}
 		}		
 	}	
 
 	/*******************************************************************************/
 	//end Model menu methods and actions
 	/*******************************************************************************/
 
 	/*******************************************************************************/
 	// table actions
 	/*******************************************************************************/
 
 	//based on code from http://tips4java.wordpress.com/2009/06/07/table-cell-listener/
 	Action reacAction = new AbstractAction()
 	{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent ae)
 		{		  	
 			TableCellListener tcl = (TableCellListener)ae.getSource();
 
 			if (tcl.getOldValue() != tcl.getNewValue()) {
 				int id = Integer.parseInt((String) (reactionsTable.getModel().getValueAt(tcl.getRow(), 0)));
 				ReactionUndoItem undoItem = createReactionUndoItem(tcl.getOldValue(), tcl.getNewValue(), reactionsTable.getSelectedRow(), tcl.getColumn(), id, UndoConstants.TYPING, UndoConstants.REACTION_UNDO_ITEM_TYPE);
 				LocalConfig.getInstance().reactionsTableChanged = true;
 				updateReactionsCellIfValid(tcl.getOldValue(), tcl.getNewValue(), tcl.getRow(), tcl.getColumn());
 				if (reactionUpdateValid) {
 					if (undoItem.getColumn() == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 						if (reactionsTable.getModel().getValueAt(tcl.getRow(), GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) != null) {
 							undoItem.setNewValue(reactionsTable.getModel().getValueAt(tcl.getRow(), GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN).toString());
 						}						
 						if (reactionsTable.getModel().getValueAt(tcl.getRow(), GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN) != null) {
 							undoItem.setEquationNames(reactionsTable.getModel().getValueAt(tcl.getRow(), GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN).toString());
 						} else {
 							undoItem.setEquationNames("");
 						}						
 					}
 					setUpReactionsUndo(undoItem);
 				}
 			}
 		}
 	};
 
 	Action metabAction = new AbstractAction()
 	{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent e)
 		{   	  
 			TableCellListener mtcl = (TableCellListener)e.getSource();
 
 			if (mtcl.getOldValue() != mtcl.getNewValue()) {				
 				LocalConfig.getInstance().metabolitesTableChanged = true;
 				int id = Integer.parseInt((String) (metabolitesTable.getModel().getValueAt(mtcl.getRow(), 0)));
 				MetaboliteUndoItem undoItem = createMetaboliteUndoItem(mtcl.getOldValue(), mtcl.getNewValue(), metabolitesTable.getSelectedRow(), mtcl.getColumn(), id, UndoConstants.TYPING, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);
 				setUndoOldCollections(undoItem);
 				updateMetabolitesCellIfValid(mtcl.getOldValue(), mtcl.getNewValue(), mtcl.getRow(), mtcl.getColumn());
 				if (metaboliteUpdateValid) {
 					if (renameMetabolite) {
 						undoItem.setUndoType(UndoConstants.RENAME_METABOLITE);
 					}
 					setUndoNewCollections(undoItem);
 					setUpMetabolitesUndo(undoItem);
 				}
 			}			
 		}
 	};
 
 	// updates reactions table with new value is valid, else reverts to old value
 	public void updateReactionsCellIfValid(String oldValue, String newValue, int rowIndex, int colIndex) {		
 		reactionUpdateValid = true;
 		EntryValidator validator = new EntryValidator();
 		//LocalConfig.getInstance().editMode = true;
 		int id = Integer.parseInt((String) (reactionsTable.getModel().getValueAt(rowIndex, 0)));		
 		if (colIndex == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 			ReactionParser parser = new ReactionParser();
 			if (!parser.isValid(newValue.trim())) {
 				setFindReplaceAlwaysOnTop(false);
 				JOptionPane.showMessageDialog(null,                
 						GraphicalInterfaceConstants.INVALID_REACTIONS_ENTRY_ERROR_MESSAGE, 
 						GraphicalInterfaceConstants.INVALID_REACTIONS_ENTRY_ERROR_TITLE,                               
 						JOptionPane.ERROR_MESSAGE);
 				setFindReplaceAlwaysOnTop(true);
 				LocalConfig.getInstance().getInvalidReactions().add(newValue.trim());
 			} else {
 				//  if reaction is changed unhighlight unused metabolites since
 				//  used status may change, same with participating reactions
 				highlightUnusedMetabolites = false;
 				highlightUnusedMetabolitesItem.setState(false);
 				// if reaction is reversible, no need to check lower bound
 				if (newValue.contains("<") || (newValue.contains("=") && !newValue.contains(">"))) {					
 					parser.reactionList(newValue.trim());
 					updateReactionEquation(newValue, id, rowIndex, parser.getEquation());
 					// check if lower bound is >= 0 if reversible = false
 				} else if (newValue.contains("-->") || newValue.contains("->") || newValue.contains("=>")) {
 					parser.reactionList(newValue.trim());
 					// if lower bound < 0, display option dialog		
 					if (Double.valueOf((String) reactionsTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.LOWER_BOUND_COLUMN)) < 0)  {
 						if (!replaceAllMode) {
 							setFindReplaceAlwaysOnTop(false);
 							Object[] options = {"    Yes    ", "    No    ",};
 							int choice = JOptionPane.showOptionDialog(null, 
 									GraphicalInterfaceConstants.LOWER_BOUND_ERROR_MESSAGE, 
 									GraphicalInterfaceConstants.LOWER_BOUND_ERROR_TITLE, 
 									JOptionPane.YES_NO_OPTION, 
 									JOptionPane.QUESTION_MESSAGE, 
 									null, options, options[0]);
 							// set lower bound to 0 and set new equation
 							if (choice == JOptionPane.YES_OPTION) {
 								reactionsTable.getModel().setValueAt("0.0", rowIndex, GraphicalInterfaceConstants.LOWER_BOUND_COLUMN);
 								reactionsTable.getModel().setValueAt(GraphicalInterfaceConstants.BOOLEAN_VALUES[0], rowIndex, GraphicalInterfaceConstants.REVERSIBLE_COLUMN);
 								updateReactionEquation(newValue, id, rowIndex, parser.getEquation());
 							}
 							// set old equation
 							if (choice == JOptionPane.NO_OPTION) {
 								reactionsTable.getModel().setValueAt(oldValue, rowIndex, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN);
 							}
 							setFindReplaceAlwaysOnTop(true);
 							// if in replace all mode, just set lower bound to 0 and set new equation
 						} else {
 							reactionsTable.getModel().setValueAt("0.0", rowIndex, GraphicalInterfaceConstants.LOWER_BOUND_COLUMN);
 							reactionsTable.getModel().setValueAt(GraphicalInterfaceConstants.BOOLEAN_VALUES[0], rowIndex, GraphicalInterfaceConstants.REVERSIBLE_COLUMN);
 							updateReactionEquation(newValue, id, rowIndex, parser.getEquation());
 						}
 					} else {
 						// lower bound >= 0, set new equation
 						updateReactionEquation(newValue, id, rowIndex, parser.getEquation());
 					}
 				} 
 				if (LocalConfig.getInstance().getSuspiciousMetabolites().size() > 0) {
 					setLoadErrorMessage("Model contains suspicious metabolites.");
 					statusBar.setText("Row 1" + "                   " + getLoadErrorMessage());
 				}
 			}
 		} else if (colIndex == GraphicalInterfaceConstants.KO_COLUMN) {
 			if (validator.validTrueEntry(newValue)) {
 				reactionsTable.getModel().setValueAt(GraphicalInterfaceConstants.BOOLEAN_VALUES[1], rowIndex, GraphicalInterfaceConstants.KO_COLUMN);
 			} else if (validator.validFalseEntry(newValue)) {
 				reactionsTable.getModel().setValueAt(GraphicalInterfaceConstants.BOOLEAN_VALUES[0], rowIndex, GraphicalInterfaceConstants.KO_COLUMN);
 			} else if (newValue != null) {				
 				if (!replaceAllMode) {
 					setFindReplaceAlwaysOnTop(false);
 					JOptionPane.showMessageDialog(null,                
 							GraphicalInterfaceConstants.BOOLEAN_VALUE_ERROR_MESSAGE, 
 							GraphicalInterfaceConstants.BOOLEAN_VALUE_ERROR_TITLE,                               
 							JOptionPane.ERROR_MESSAGE);
 					formulaBar.setText(getTableCellOldValue());
 					setFindReplaceAlwaysOnTop(true);
 				}				
 				reactionUpdateValid = false;
 				reactionsTable.getModel().setValueAt(oldValue, rowIndex, GraphicalInterfaceConstants.KO_COLUMN);
 			}
 		} else if (colIndex == GraphicalInterfaceConstants.REVERSIBLE_COLUMN) {
 			if (!replaceAllMode) {
 				setFindReplaceAlwaysOnTop(false);
 				JOptionPane.showMessageDialog(null, 
 						GraphicalInterfaceConstants.REVERSIBLE_ERROR_MESSAGE,                
 						GraphicalInterfaceConstants.REVERSIBLE_ERROR_TITLE, 					                               
 						JOptionPane.ERROR_MESSAGE);
 				formulaBar.setText(getTableCellOldValue());
 				setFindReplaceAlwaysOnTop(true);
 			}			
 			reactionUpdateValid = false;
 			reactionsTable.getModel().setValueAt(oldValue, rowIndex, GraphicalInterfaceConstants.REVERSIBLE_COLUMN);
 		} else if (colIndex == GraphicalInterfaceConstants.FLUX_VALUE_COLUMN || 
 				colIndex == GraphicalInterfaceConstants.LOWER_BOUND_COLUMN || 
 				colIndex == GraphicalInterfaceConstants.UPPER_BOUND_COLUMN || 
 				colIndex == GraphicalInterfaceConstants.BIOLOGICAL_OBJECTIVE_COLUMN || 
 				colIndex == GraphicalInterfaceConstants.SYNTHETIC_OBJECTIVE_COLUMN) {
 			if (!validator.isNumber(newValue)) {
 				if (!replaceAllMode) {
 					setFindReplaceAlwaysOnTop(false);
 					JOptionPane.showMessageDialog(null,                
 							GraphicalInterfaceConstants.NUMERIC_VALUE_ERROR_TITLE,                
 							GraphicalInterfaceConstants.NUMERIC_VALUE_ERROR_MESSAGE,                               
 							JOptionPane.ERROR_MESSAGE);
 					formulaBar.setText(getTableCellOldValue());
 					setFindReplaceAlwaysOnTop(true);
 				}	
 				reactionUpdateValid = false;
 				reactionsTable.getModel().setValueAt(oldValue, rowIndex, colIndex);				
 			} else {
 				if (colIndex == GraphicalInterfaceConstants.LOWER_BOUND_COLUMN) { 
 					Double lowerBound = Double.valueOf(newValue);
 					Double upperBound = Double.valueOf((String) (reactionsTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.UPPER_BOUND_COLUMN)));
 					String reversible = reactionsTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.REVERSIBLE_COLUMN).toString();
 					if (!validator.lowerBoundReversibleValid(lowerBound, upperBound, reversible)) {
 						setFindReplaceAlwaysOnTop(false);
 						Object[] options = {"    Yes    ", "    No    ",};
 						int choice = JOptionPane.showOptionDialog(null, 
 								GraphicalInterfaceConstants.LOWER_BOUND_ERROR_MESSAGE, 
 								GraphicalInterfaceConstants.LOWER_BOUND_ERROR_TITLE, 
 								JOptionPane.YES_NO_OPTION, 
 								JOptionPane.QUESTION_MESSAGE, 
 								null, options, options[0]);
 						if (choice == JOptionPane.YES_OPTION) {
 							reactionsTable.getModel().setValueAt("0.0", rowIndex, GraphicalInterfaceConstants.LOWER_BOUND_COLUMN);
 						}
 						if (choice == JOptionPane.NO_OPTION) {
 							reactionUpdateValid = false;
 							reactionsTable.getModel().setValueAt(oldValue, rowIndex, GraphicalInterfaceConstants.LOWER_BOUND_COLUMN);
 						}
 						setFindReplaceAlwaysOnTop(true);
 					} else if (lowerBound > upperBound) {
 						setFindReplaceAlwaysOnTop(false);
 						Object[] options = {"    Yes    ", "    No    ",};
 						int choice = JOptionPane.showOptionDialog(null, 
 								GraphicalInterfaceConstants.LOWER_BOUND_ERROR_MESSAGE2, 
 								GraphicalInterfaceConstants.LOWER_BOUND_ERROR_TITLE, 
 								JOptionPane.YES_NO_OPTION, 
 								JOptionPane.QUESTION_MESSAGE, 
 								null, options, options[0]);
 						if (choice == JOptionPane.YES_OPTION) {
 							reactionsTable.getModel().setValueAt("0.0", rowIndex, GraphicalInterfaceConstants.LOWER_BOUND_COLUMN);
 						}
 						if (choice == JOptionPane.NO_OPTION) {
 							reactionUpdateValid = false;
 							reactionsTable.getModel().setValueAt(oldValue, rowIndex, GraphicalInterfaceConstants.LOWER_BOUND_COLUMN);
 						}
 						setFindReplaceAlwaysOnTop(true);
 					} else {
 						reactionsTable.getModel().setValueAt(newValue, rowIndex, GraphicalInterfaceConstants.LOWER_BOUND_COLUMN);
 					}
 				}
 				if (colIndex == GraphicalInterfaceConstants.UPPER_BOUND_COLUMN) { 
 					Double lowerBound = Double.valueOf((String) (reactionsTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.LOWER_BOUND_COLUMN)));
 					Double upperBound = Double.valueOf(newValue);
 					if (upperBound < lowerBound) {
 						setFindReplaceAlwaysOnTop(false);
 						Object[] options = {"    Yes    ", "    No    ",};
 						int choice = JOptionPane.showOptionDialog(null, 
 								GraphicalInterfaceConstants.UPPER_BOUND_ERROR_MESSAGE, 
 								GraphicalInterfaceConstants.UPPER_BOUND_ERROR_TITLE, 
 								JOptionPane.YES_NO_OPTION, 
 								JOptionPane.QUESTION_MESSAGE, 
 								null, options, options[0]);
 						if (choice == JOptionPane.YES_OPTION) {
 							reactionsTable.getModel().setValueAt(GraphicalInterfaceConstants.UPPER_BOUND_DEFAULT_STRING, rowIndex, GraphicalInterfaceConstants.UPPER_BOUND_COLUMN);
 						}
 						if (choice == JOptionPane.NO_OPTION) {
 							reactionUpdateValid = false;
 							reactionsTable.getModel().setValueAt(oldValue, rowIndex, GraphicalInterfaceConstants.UPPER_BOUND_COLUMN);
 						}	
 						setFindReplaceAlwaysOnTop(true);
 					} else {
 						reactionsTable.getModel().setValueAt(newValue, rowIndex, GraphicalInterfaceConstants.UPPER_BOUND_COLUMN);
 					}
 				} 
 				if (colIndex == GraphicalInterfaceConstants.FLUX_VALUE_COLUMN || 
 						colIndex == GraphicalInterfaceConstants.BIOLOGICAL_OBJECTIVE_COLUMN ||
 						colIndex == GraphicalInterfaceConstants.SYNTHETIC_OBJECTIVE_COLUMN) {
 					reactionsTable.getModel().setValueAt(newValue, rowIndex, colIndex);
 				}				
 			}
 		} else {
 			// action for remaining columns
 			reactionsTable.getModel().setValueAt(newValue, rowIndex, colIndex);
 		}
 	}
 	
 	public void updateReactionEquation(String newValue, int id, int rowIndex, SBMLReactionEquation equation) {
 		SBMLReactionEquation equn = new SBMLReactionEquation();	
 		ArrayList<SBMLReactant> reactants = new ArrayList<SBMLReactant>();
 		ArrayList<SBMLProduct> products = new ArrayList<SBMLProduct>();
 		ReactionParser parser = new ReactionParser();
 		reactionsTable.getModel().setValueAt(newValue, rowIndex, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN);
 		LocalConfig.getInstance().getReactionEquationMap().put(id, equation);	
 		for (int i = 0; i < equation.getReactants().size(); i++){
 			maybeAddSpecies(equation.getReactants().get(i).getMetaboliteAbbreviation(), equation, "reactant", i);
 			if (addMetabolite) {
 				equation.getReactants().get(i).setReactionId(id);
 				Integer metabId = (Integer) LocalConfig.getInstance().getMetaboliteNameIdMap().get(equation.getReactants().get(i).getMetaboliteAbbreviation());				
 				equation.getReactants().get(i).setMetaboliteId(metabId);
 				if (LocalConfig.getInstance().getMetaboliteIdNameMap().containsKey(metabId)) {
 					equation.getReactants().get(i).setMetaboliteName(LocalConfig.getInstance().getMetaboliteIdNameMap().get(metabId));
 				}				
 				reactants.add(equation.getReactants().get(i));
 				if (parser.isSuspicious(equation.getReactants().get(i).getMetaboliteAbbreviation())) {
 					if (!LocalConfig.getInstance().getSuspiciousMetabolites().contains(metabId)) {
 						LocalConfig.getInstance().getSuspiciousMetabolites().add(metabId);
 					}							
 				}
 			}	
 		}	
 		for (int i = 0; i < equation.getProducts().size(); i++){
 			maybeAddSpecies(equation.getProducts().get(i).getMetaboliteAbbreviation(), equation, "product", i);
 			if (addMetabolite) {
 				equation.getProducts().get(i).setReactionId(id);
 				Integer metabId = (Integer) LocalConfig.getInstance().getMetaboliteNameIdMap().get(equation.getProducts().get(i).getMetaboliteAbbreviation());				
 				equation.getProducts().get(i).setMetaboliteId(metabId);
 				if (LocalConfig.getInstance().getMetaboliteIdNameMap().containsKey(metabId)) {
 					equation.getProducts().get(i).setMetaboliteName(LocalConfig.getInstance().getMetaboliteIdNameMap().get(metabId));
 				}				
 				products.add(equation.getProducts().get(i));
 				if (parser.isSuspicious(equation.getProducts().get(i).getMetaboliteAbbreviation())) {
 					if (!LocalConfig.getInstance().getSuspiciousMetabolites().contains(metabId)) {
 						LocalConfig.getInstance().getSuspiciousMetabolites().add(metabId);
 					}							
 				}
 			}	
 		}
 		equn.setReactants(reactants);
 		equn.setProducts(products);
 		equn.setReversible(equation.getReversible());
 		equn.setReversibleArrow(equation.getReversibleArrow());
 		equn.setIrreversibleArrow(equation.getIrreversibleArrow());
 		equn.writeReactionEquation();
 		LocalConfig.getInstance().getReactionEquationMap().put(id, equn);
 		if (LocalConfig.getInstance().noButtonClicked) {			
 			reactionsTable.getModel().setValueAt(equn.equationAbbreviations, rowIndex, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN);
 		}
 		reactionsTable.getModel().setValueAt(equn.equationNames, rowIndex, GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN);
 		reactionsTable.getModel().setValueAt(equn.getReversible(), rowIndex, GraphicalInterfaceConstants.REVERSIBLE_COLUMN);
 		GraphicalInterface.showPrompt = true;
 	}
 	
 	public void maybeAddSpecies(String species, SBMLReactionEquation equation, String type, int index) {
 		addMetabolite = true;
 		int maxMetab = LocalConfig.getInstance().getMaxMetabolite();
 		int maxMetabId = LocalConfig.getInstance().getMaxMetaboliteId();
 		boolean newMetabolite = false;
 		if (!(LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey(species.trim()))) {
 			newMetabolite = true;
 			if (GraphicalInterface.showPrompt && !(GraphicalInterface.replaceAllMode && LocalConfig.getInstance().yesToAllButtonClicked)) {
 				Object[] options = {"Yes",
 						"Yes to All",
 				"No"};
 				LocalConfig.getInstance().addReactantPromptShown = true;
 				int choice = JOptionPane.showOptionDialog(null, 
 						"The metabolite " + species + " does not exist. Do you wish to add it?", 
 						"Add Metabolite?", 
 						JOptionPane.YES_NO_CANCEL_OPTION, 
 						JOptionPane.QUESTION_MESSAGE, 
 						null, options, options[0]);
 				//options[0] sets "Yes" as default button
 
 				// interpret the user's choice	  
 				if (choice == JOptionPane.YES_OPTION)
 				{
 					LocalConfig.getInstance().addMetaboliteOption = true;
 					LocalConfig.getInstance().getMetaboliteNameIdMap().put(species, maxMetab);
 					//LocalConfig.getInstance().getAddedMetabolites().add((maxMetabId));
 					addNewMetabolite(maxMetab, species);
 					maxMetab += 1;
 					LocalConfig.getInstance().setMaxMetabolite(maxMetab);
 					maxMetabId += 1;
 					LocalConfig.getInstance().setMaxMetaboliteId(maxMetabId);
 				}
 				//No option actually corresponds to "Yes to All" button
 				if (choice == JOptionPane.NO_OPTION)
 				{
 					LocalConfig.getInstance().addMetaboliteOption = true;
 					GraphicalInterface.showPrompt = false;
 					LocalConfig.getInstance().getMetaboliteNameIdMap().put(species, maxMetab);
 					//LocalConfig.getInstance().getAddedMetabolites().add((maxMetabId));
 					addNewMetabolite(maxMetab, species);
 					maxMetab += 1;
 					LocalConfig.getInstance().setMaxMetabolite(maxMetab);
 					maxMetabId += 1;
 					LocalConfig.getInstance().setMaxMetaboliteId(maxMetabId);
 					LocalConfig.getInstance().yesToAllButtonClicked = true;
 				}
 				//Cancel option actually corresponds to "No" button
 				if (choice == JOptionPane.CANCEL_OPTION) {
 					addMetabolite = false;
 					LocalConfig.getInstance().addMetaboliteOption = false;
 					LocalConfig.getInstance().noButtonClicked = true;
 					// TODO; determine if necessary since these are removed elsewhere
 					if (type == "reactant") {
 						equation.getReactants().remove(index);
 					} else if (type == "product") {
 						equation.getProducts().remove(index);
 					} 					
 				}	
 			} else {
 				LocalConfig.getInstance().getMetaboliteNameIdMap().put(species, maxMetab);
 				//LocalConfig.getInstance().getAddedMetabolites().add((maxMetabId));
 				addNewMetabolite(maxMetab, species);
 				maxMetab += 1;
 				LocalConfig.getInstance().setMaxMetabolite(maxMetab);
 				maxMetabId += 1;
 				LocalConfig.getInstance().setMaxMetaboliteId(maxMetabId);
 			}
 		}
 		if (!newMetabolite || LocalConfig.getInstance().addMetaboliteOption) {
 			if (LocalConfig.getInstance().getMetaboliteUsedMap().containsKey(species)) {
 				int usedCount = (Integer) LocalConfig.getInstance().getMetaboliteUsedMap().get(species);
 				LocalConfig.getInstance().getMetaboliteUsedMap().put(species, new Integer(usedCount + 1));									
 			} else {
 				LocalConfig.getInstance().getMetaboliteUsedMap().put(species, new Integer(1));
 			}	
 		}
 	}
 	
 	public void addNewMetabolite(int maxMetab, String species) {
 		DefaultTableModel model = (DefaultTableModel) metabolitesTable.getModel();		
 		if (maxMetab < LocalConfig.getInstance().getMaxMetaboliteId()) {
 		
 		} else {
 			model.addRow(createMetabolitesRow(maxMetab));
 		}
 		model.setValueAt(species, maxMetab, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN);
 		if (LocalConfig.getInstance().getMetaboliteUsedMap().containsKey(species)) {
 			int usedCount = (Integer) LocalConfig.getInstance().getMetaboliteUsedMap().get(species);
 			LocalConfig.getInstance().getMetaboliteUsedMap().put(species, new Integer(usedCount + 1));
 		} else {
 			LocalConfig.getInstance().getMetaboliteUsedMap().put(species, new Integer(1));
 		}
 		setUpMetabolitesTable(model);
 	}
 
 	// updates metabolites table with new value is valid, else reverts to old value
 	public void updateMetabolitesCellIfValid(String oldValue, String newValue, int rowIndex, int colIndex) {
 		metaboliteUpdateValid = true;
 		EntryValidator validator = new EntryValidator();
 		int id = Integer.parseInt((String) (metabolitesTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.METABOLITE_ID_COLUMN)));
 		String metabAbbrev = (String) (metabolitesTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN));
 		String metabName = (String) (metabolitesTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN));
 		if (colIndex == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) {
 			// enables or disables unused metabolites menu items depending on if there are unused items present
 			createUnusedMetabolitesList();			
 			if (LocalConfig.getInstance().getUnusedList().size() > 0) {
 				highlightUnusedMetabolitesItem.setEnabled(true);
 				deleteUnusedItem.setEnabled(true);
 			} else {
 				highlightUnusedMetabolitesItem.setEnabled(false);
 				deleteUnusedItem.setEnabled(false);
 			}
 			if (oldValue == null || oldValue.trim().length() == 0 && 
 					newValue != null || newValue.trim().length() > 0) {
 				if (id >= LocalConfig.getInstance().getMaxMetabolite()) {
 					int max = id + 1;
 					LocalConfig.getInstance().setMaxMetabolite(max);
 				}				
 			}
 			if (LocalConfig.getInstance().getMetaboliteUsedMap().containsKey(oldValue) 
 					&& !LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey(newValue)) {
 				if (newValue != null && newValue.toString().trim().length() > 0) {					
 					if (!newValue.equals(oldValue)) {
 						showMetaboliteRenameInterface = false;
 						showRenameMessage(oldValue);
 						if (renameMetabolite) {					
 							rewriteReactions(id, oldValue, metabName, newValue, colIndex);
 							updateMetaboliteMaps(id, oldValue, metabName, newValue, colIndex);
 						} else {
 							metaboliteUpdateValid = false;
 							metabolitesTable.getModel().setValueAt(oldValue, rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN);
 						}
 					}					
 				} else {
 					setFindReplaceAlwaysOnTop(false);
 					JOptionPane.showMessageDialog(null,                
 							GraphicalInterfaceConstants.METABOLITE_RENAME_ERROR_MESSAGE,                
 							GraphicalInterfaceConstants.METABOLITE_RENAME_ERROR_TITLE,                                
 							JOptionPane.ERROR_MESSAGE);
 					formulaBar.setText(getTableCellOldValue());
 					setFindReplaceAlwaysOnTop(true);
 					metaboliteUpdateValid = false;
 					metabolitesTable.getModel().setValueAt(oldValue, rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN);
 				}
 				// entry is duplicate
 			} else if (LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey(newValue)) {
 				setFindReplaceAlwaysOnTop(false);
 //				if (LocalConfig.getInstance().getMetaboliteUsedMap().containsKey(oldValue)) {
 //					JOptionPane.showMessageDialog(null,                
 //							"Duplicate Metabolite.",                
 //							"Duplicate Metabolite",                                
 //							JOptionPane.ERROR_MESSAGE);
 //					metaboliteUpdateValid = false;
 //					metabolitesTable.getModel().setValueAt(oldValue, rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN);
 //				} else {
 					Object[] options = {"    Yes    ", "    No    ",};
 					int choice = JOptionPane.showOptionDialog(null, 
 							GraphicalInterfaceConstants.DUPLICATE_METABOLITE_MESSAGE + newValue + duplicateSuffix(newValue) + " ?", 
 							GraphicalInterfaceConstants.DUPLICATE_METABOLITE_TITLE, 
 							JOptionPane.YES_NO_OPTION, 
 							JOptionPane.QUESTION_MESSAGE, 
 							null, options, options[0]);
 					if (choice == JOptionPane.YES_OPTION) {
 						newValue = newValue + duplicateSuffix(newValue);
 						metabolitesTable.getModel().setValueAt(newValue, rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN);
 						LocalConfig.getInstance().getMetaboliteNameIdMap().put(newValue, id);
 						LocalConfig.getInstance().getMetaboliteNameIdMap().remove(oldValue);
 					}
 					if (choice == JOptionPane.NO_OPTION) {
 						metaboliteUpdateValid = false;
 						metabolitesTable.getModel().setValueAt(oldValue, rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN);
 					}
 				//}				
 				setFindReplaceAlwaysOnTop(true);
 			} else {
 				//if a blank is entered remove key/value of old value
 				if (newValue == null || newValue.length() == 0 || newValue.trim().equals("")) {
 					LocalConfig.getInstance().getMetaboliteNameIdMap().remove(oldValue);
 					// non-duplicate entry
 				} else {
 					if (newValue.trim() != null && newValue.trim().length() > 0) {
 						LocalConfig.getInstance().getMetaboliteNameIdMap().remove(oldValue);
 						LocalConfig.getInstance().getMetaboliteNameIdMap().put(newValue, Integer.parseInt((String) (metabolitesTable.getModel().getValueAt(rowIndex, 0))));
 					}					
 				}
 				metabolitesTable.getModel().setValueAt(newValue, rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN);
 			}
 			LocalConfig.getInstance().getMetaboliteNameIdMap().remove(oldValue);
 			// update id name map
 			if (!LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey((String) metabolitesTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN))) {
 				if (metabolitesTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) != null 
 						&& ((String) metabolitesTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN)).trim().length() > 0) {
 					LocalConfig.getInstance().getMetaboliteNameIdMap().put((String) metabolitesTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN), new Integer(id));
 				}				
 			}	
 		} else if (colIndex == GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN) {
 			if (LocalConfig.getInstance().getMetaboliteUsedMap().containsKey(metabAbbrev)) {
 				if (newValue != null && newValue.toString().trim().length() > 0) {
 					if (!newValue.equals(oldValue)) {
 						showMetaboliteRenameInterface = false;
 						showRenameMessage(oldValue);
 						if (renameMetabolite) {					
 							rewriteReactions(id, metabAbbrev, oldValue, newValue, colIndex);
 							updateMetaboliteMaps(id, oldValue, metabName, newValue, colIndex);
 						} else {
 							metaboliteUpdateValid = false;
 							metabolitesTable.getModel().setValueAt(oldValue, rowIndex, GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN);
 						}
 					}					
 				} else {
 					setFindReplaceAlwaysOnTop(false);
 					JOptionPane.showMessageDialog(null,                
 							GraphicalInterfaceConstants.METABOLITE_RENAME_ERROR_MESSAGE,                
 							GraphicalInterfaceConstants.METABOLITE_RENAME_ERROR_TITLE,                                
 							JOptionPane.ERROR_MESSAGE);
 					formulaBar.setText(getTableCellOldValue());
 					setFindReplaceAlwaysOnTop(true);
 					metaboliteUpdateValid = false;
 					metabolitesTable.getModel().setValueAt(oldValue, rowIndex, GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN);
 				}				
 			}
 			// if not a number error message displayed
 		} else if (colIndex == GraphicalInterfaceConstants.CHARGE_COLUMN) {
 			if (!validator.isNumber(newValue)) {
 				if (!replaceAllMode) {
 					setFindReplaceAlwaysOnTop(false);
 					JOptionPane.showMessageDialog(null,                
 							GraphicalInterfaceConstants.NUMERIC_VALUE_ERROR_TITLE,                
 							GraphicalInterfaceConstants.NUMERIC_VALUE_ERROR_MESSAGE,                               
 							JOptionPane.ERROR_MESSAGE);
 					formulaBar.setText(getTableCellOldValue());
 					setFindReplaceAlwaysOnTop(true);
 				}				
 				metaboliteUpdateValid = false;
 				metabolitesTable.getModel().setValueAt(oldValue, rowIndex, colIndex);
 			} else {
 				metabolitesTable.getModel().setValueAt(newValue, rowIndex, colIndex);
 			}
 		} else if (colIndex == GraphicalInterfaceConstants.BOUNDARY_COLUMN) {
 			if (validator.validTrueEntry(newValue)) {
 				metabolitesTable.getModel().setValueAt(GraphicalInterfaceConstants.BOOLEAN_VALUES[1], rowIndex, GraphicalInterfaceConstants.BOUNDARY_COLUMN);
 			} else if (validator.validFalseEntry(newValue)) {
 				metabolitesTable.getModel().setValueAt(GraphicalInterfaceConstants.BOOLEAN_VALUES[0], rowIndex, GraphicalInterfaceConstants.BOUNDARY_COLUMN);
 			} else if (newValue != null) {				
 				if (!replaceAllMode) {
 					setFindReplaceAlwaysOnTop(false);
 					JOptionPane.showMessageDialog(null,                
 							GraphicalInterfaceConstants.BOOLEAN_VALUE_ERROR_MESSAGE,                
 							GraphicalInterfaceConstants.BOOLEAN_VALUE_ERROR_TITLE,                                
 							JOptionPane.ERROR_MESSAGE);
 					formulaBar.setText(getTableCellOldValue());
 					setFindReplaceAlwaysOnTop(true);
 				}	
 				metaboliteUpdateValid = false;
 				metabolitesTable.getModel().setValueAt(oldValue, rowIndex, GraphicalInterfaceConstants.BOUNDARY_COLUMN);
 			}
 		} else {
 			// action for remaining columns
 			metabolitesTable.getModel().setValueAt(newValue, rowIndex, colIndex);
 		}
 		updateMetaboliteCollections(rowIndex, Integer.parseInt((String) (metabolitesTable.getModel().getValueAt(rowIndex, 0)))); 
 		showMetaboliteRenameInterface = true;
 	}
 
 	public void updateMetaboliteCollections(int rowIndex, int metaboliteId) {
 		// prevents invisible id column from setting id in formulaBar for find events
 		if (metabolitesTable.getSelectedRow() > 0) {
 			int viewRow = metabolitesTable.convertRowIndexToView(metabolitesTable.getSelectedRow());
 			formulaBar.setText((String) metabolitesTable.getModel().getValueAt(viewRow, metabolitesTable.getSelectedColumn()));
 		}
 
 		// update id name map
 		if (!LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey((String) metabolitesTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN))) {
 			if (metabolitesTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) != null 
 					&& ((String) metabolitesTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN)).trim().length() > 0) {
 				LocalConfig.getInstance().getMetaboliteNameIdMap().put((String) metabolitesTable.getModel().getValueAt(rowIndex, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN), new Integer(metaboliteId));
 			}				
 		}
 		// enables or disables unused metabolites menu items depending on if there are unused items present
 		createUnusedMetabolitesList();			
 		if (LocalConfig.getInstance().getUnusedList().size() > 0) {
 			highlightUnusedMetabolitesItem.setEnabled(true);
 			deleteUnusedItem.setEnabled(true);
 		} else {
 			highlightUnusedMetabolitesItem.setEnabled(false);
 			deleteUnusedItem.setEnabled(false);
 		}
 	}
 
 	public static void updateReactionsCellById(String value, int id, int col) {
 		Map<String, Object> reactionsIdRowMap = new HashMap<String, Object>();
 		for (int i = 0; i < reactionsTable.getRowCount(); i++) {
 			reactionsIdRowMap.put((String) reactionsTable.getModel().getValueAt(i, GraphicalInterfaceConstants.REACTIONS_ID_COLUMN), i);
 		}
 		String row = (reactionsIdRowMap.get(Integer.toString(id))).toString();
 		int rowNum = Integer.valueOf(row);
 		reactionsTable.getModel().setValueAt(value, rowNum, col);
 	}
 
 	public void deleteMetabolitesRowById(int id) {
 		Map<String, Object> metabolitesIdRowMap = new HashMap<String, Object>();
 		for (int i = 0; i < metabolitesTable.getRowCount(); i++) {
 			metabolitesIdRowMap.put((String) metabolitesTable.getModel().getValueAt(i, GraphicalInterfaceConstants.REACTIONS_ID_COLUMN), i);
 		}
 		System.out.println(metabolitesIdRowMap);
 		System.out.println(id);
 		String row = (metabolitesIdRowMap.get(Integer.toString(id))).toString();
 		int rowNum = Integer.valueOf(row);
 		DefaultTableModel model = (DefaultTableModel) metabolitesTable.getModel();
 		model.removeRow(rowNum);
 		setUpMetabolitesTable(model);
 	}
 
 	/*****************************************************************************/
 	//end table actions
 	/*****************************************************************************/
 
 	/******************************************************************************/
 	//reload tables methods
 	/******************************************************************************/
 
 	//sets parameters to initial values on load
 	public void loadSetUp() {
 		if (getFindReplaceDialog() != null) {
 			getFindReplaceDialog().dispose();
 		}
 		findReplaceItem.setEnabled(true);
 		findbutton.setEnabled(true);
 		clearOutputPane();
 		if (getPopout() != null) {
 			popout.dispose();
 		}
 		setBooleanDefaults();
 		clearConfigLists();
 		undoSplitButton.setToolTipText("Can't Undo (Ctrl+Z)");
 		redoSplitButton.setToolTipText("Can't Redo (Ctrl+Y)");
 		disableOptionComponent(undoSplitButton, undoLabel, undoGrayedLabel);
 		disableOptionComponent(redoSplitButton, undoLabel, undoGrayedLabel);
 		undoCount = 1;
 		redoCount = 1;
 		LocalConfig.getInstance().setNumReactionTablesCopied(0);
 		LocalConfig.getInstance().setNumMetabolitesTableCopied(0);
 		showPrompt = true;
 		//LocalConfig.getInstance().hasMetabolitesFile = false;
 		highlightUnusedMetabolites = false;
 		highlightUnusedMetabolitesItem.setState(false);
 		setReactionsSortColumnIndex(0);
 		setMetabolitesSortColumnIndex(0);
 		LocalConfig.getInstance().setReactionsLocationsListCount(0);
 		LocalConfig.getInstance().setMetabolitesLocationsListCount(0);		
 		// default selection mode cells only
 		setUpCellSelectionMode();
 	}
 
 	public void setUpMetabolitesTable(DefaultTableModel model) {
 		metabolitesTable.setModel(model);
 		setMetabolitesTableLayout();
 
 		// enables or disables menu items depending on if there are unused items present
 		createUnusedMetabolitesList();
 		if (LocalConfig.getInstance().getUnusedList().size() > 0) {
 			highlightUnusedMetabolitesItem.setEnabled(true);
 			deleteUnusedItem.setEnabled(true);
 		} else {
 			highlightUnusedMetabolitesItem.setEnabled(false);
 			deleteUnusedItem.setEnabled(false);
 		}
 		if (LocalConfig.getInstance().getSuspiciousMetabolites().size() > 0) {
 			setLoadErrorMessage("Model contains suspicious metabolites.");
 			// selected row default at row 1 (index 0)
 			statusBar.setText("Row 1" + "                   " + getLoadErrorMessage());
 			findSuspiciousItem.setEnabled(true);
 		} else {
 			statusBar.setText("Row 1");
 			findSuspiciousItem.setEnabled(false);
 		}		
 
 		if (getMetabolitesSortColumnIndex() >= 0) {
 			metabolitesTable.setSortOrder(getMetabolitesSortColumnIndex(), getMetabolitesSortOrder());
 		} else {
 			setMetabolitesSortColumnIndex(0);
 			setMetabolitesSortOrder(SortOrder.ASCENDING);
 		}			
 		statusBar.setText("Row 1");	   
 	}
 
 	public void setUpReactionsTable(DefaultTableModel model) {
 		reactionsTable.setModel(model);
 		setReactionsTableLayout();
 
 		if (getReactionsSortColumnIndex() >= 0) {
 			reactionsTable.setSortOrder(getReactionsSortColumnIndex(), getReactionsSortOrder());
 		} else {
 			setReactionsSortColumnIndex(0);
 			setReactionsSortOrder(SortOrder.ASCENDING);
 		}	
 		// selected row default at row 1 (index 0)
 		if (LocalConfig.getInstance().getSuspiciousMetabolites().size() > 0) {
 			setLoadErrorMessage("Model contains suspicious metabolites.");
 			// selected row default at row 1 (index 0)
 			statusBar.setText("Row 1" + "                   " + getLoadErrorMessage());
 		} else {
 			statusBar.setText("Row 1");
 		}				   
 	}
 
 	public void setUpTables() {
 		setTitle(GraphicalInterfaceConstants.TITLE + " - " + LocalConfig.getInstance().getModelName());		
 		listModel.addElement(LocalConfig.getInstance().getModelName());
 		DynamicTreePanel.treePanel.addObject(new Solution(LocalConfig.getInstance().getModelName(), LocalConfig.getInstance().getModelName()));
 		DynamicTreePanel.treePanel.setNodeSelected(0);
 		enableMenuItems();
 		setSortDefault();
 		setUpCellSelectionMode();
 		// selected row default at row 1 (index 0)
 		if (LocalConfig.getInstance().getSuspiciousMetabolites().size() > 0) {
 			setLoadErrorMessage("Model contains suspicious metabolites.");
 			// selected row default at row 1 (index 0)
 			statusBar.setText("Row 1" + "                   " + getLoadErrorMessage());
 		} else {
 			statusBar.setText("Row 1");
 		}	
 	}
 
 	public void setBooleanDefaults() {
 		modelCollectionOKButtonClicked = false;
 		// listener values
 		selectedCellChanged = false;
 		formulaBarFocusGained = false;
 		tabChanged = false;
 		// find-replace values
 		findMode = false;
 		findButtonReactionsClicked = false;
 		findButtonMetabolitesClicked = false;
 		matchCase = false;
 		wrapAround = false;
 		searchSelectedArea = false;
 		searchBackwards = false;
 		reactionUpdateValid = true;
 		metaboliteUpdateValid = true;
 		replaceAllMode = false;
 		reactionsFindAll = false;
 		metabolitesFindAll = false;
 		changeReactionFindSelection = true;
 		changeMetaboliteFindSelection = true;
 		// paste
 		validPaste = true;
 		showDuplicatePrompt = true;
 		duplicateMetabOK = true;
 		// other
 		showErrorMessage = true;
 		saveOptFile = false;
 		addReacColumn = false;
 		addMetabColumn = false;
 		showPrompt = true;
 		duplicatePromptShown = false;
 		reactionsTableEditable = true;
 		renameMetabolite = false;
 		showMetaboliteRenameInterface = true;
 		exit = true;
 		modelCollectionOKButtonClicked = false;
 		reactionCancelLoad = false;
 		isRoot = true;
 		LocalConfig.getInstance().hasValidGurobiKey = true;
 		openFileChooser = true;
 		LocalConfig.getInstance().reactionsTableChanged = false;
 		LocalConfig.getInstance().metabolitesTableChanged = false;
 		LocalConfig.getInstance().includesReactions = true;
 	}
 
 	public void clearConfigLists() {
 		LocalConfig.getInstance().getMetabolitesTableModelMap().clear();
 		LocalConfig.getInstance().getReactionsTableModelMap().clear();
 		LocalConfig.getInstance().getInvalidReactions().clear();
 		LocalConfig.getInstance().getBlankMetabIds().clear();
 		LocalConfig.getInstance().getMetaboliteNameIdMap().clear();
 		LocalConfig.getInstance().getMetaboliteUsedMap().clear();
 		LocalConfig.getInstance().getSuspiciousMetabolites().clear();
 		LocalConfig.getInstance().getUnusedList().clear();
 		LocalConfig.getInstance().getOptimizationFilesList().clear();
 		//LocalConfig.getInstance().getAddedMetabolites().clear();
 		LocalConfig.getInstance().getReactionEquationMap().clear();
 		LocalConfig.getInstance().getUndoItemMap().clear();
 		LocalConfig.getInstance().getUndoItemMap().clear();
 
 		// clear and reinitialize sort lists
 		LocalConfig.getInstance().getReactionsSortColumns().clear();
 		LocalConfig.getInstance().getReactionsSortColumns().add(0);
 		LocalConfig.getInstance().getReactionsSortOrderList().clear();
 		LocalConfig.getInstance().getReactionsSortOrderList().add(SortOrder.ASCENDING);
 		LocalConfig.getInstance().getMetabolitesSortColumns().clear();
 		LocalConfig.getInstance().getMetabolitesSortColumns().add(0);
 		LocalConfig.getInstance().getMetabolitesSortOrderList().clear();
 		LocalConfig.getInstance().getMetabolitesSortOrderList().add(SortOrder.ASCENDING);
 	}
 
 	public DefaultTableModel copyMetabolitesTableModel(DefaultTableModel model) {
 		DefaultTableModel metabolitesModel = new DefaultTableModel();
 		for (int m = 0; m < GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES.length; m++) {
 			metabolitesModel.addColumn(GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES[m]);
 		}
 		for (int n = 0; n < LocalConfig.getInstance().getMetabolitesMetaColumnNames().size(); n++) {
 			metabolitesModel.addColumn(LocalConfig.getInstance().getMetabolitesMetaColumnNames().get(n));
 		}
 		for (int p = 0; p < metabolitesTable.getModel().getRowCount(); p++) {
 			Vector <String> metabRow = new Vector<String>();
 			for (int q = 0; q < metabolitesTable.getModel().getColumnCount(); q++) {
 				metabRow.add((String) metabolitesTable.getModel().getValueAt(p, q));
 			}
 			metabolitesModel.addRow(metabRow);
 		}
 		return metabolitesModel;
 	}
 
 	public DefaultTableModel copyReactionsTableModel(DefaultTableModel model) {
 		DefaultTableModel reactionsModel = new DefaultTableModel();
 		for (int r = 0; r < GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES.length; r++) {
 			reactionsModel.addColumn(GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[r]);
 		}	
 		for (int s = 0; s < LocalConfig.getInstance().getReactionsMetaColumnNames().size(); s++) {
 			reactionsModel.addColumn(LocalConfig.getInstance().getReactionsMetaColumnNames().get(s));
 		}
 		for (int t = 0; t < reactionsTable.getModel().getRowCount(); t++) {
 			Vector <String> reacRow = new Vector<String>();
 			for (int v = 0; v < reactionsTable.getModel().getColumnCount(); v++) {
 				reacRow.add((String) reactionsTable.getModel().getValueAt(t, v));
 			}
 			reactionsModel.addRow(reacRow);
 		}
 		return reactionsModel;
 	}
 
 	public DefaultTableModel createBlankMetabolitesTableModel() {
 		DefaultTableModel blankMetabModel = new DefaultTableModel();
 		for (int m = 0; m < GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES.length; m++) {
 			blankMetabModel.addColumn(GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES[m]);
 		}
 		for (int j = 0; j < GraphicalInterfaceConstants.BLANK_METABOLITE_ROW_COUNT; j++) {
 			blankMetabModel.addRow(createMetabolitesRow(j));
 		}
 		return blankMetabModel;
 	}
 
 	public DefaultTableModel createBlankReactionsTableModel() {
 		DefaultTableModel blankReacModel = new DefaultTableModel();		
 		for (int r = 0; r < GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES.length; r++) {
 			blankReacModel.addColumn(GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[r]);
 		} 
 		for (int i = 0; i < GraphicalInterfaceConstants.BLANK_REACTION_ROW_COUNT; i++) {
 			blankReacModel.addRow(createReactionsRow(i));
 		}
 		return blankReacModel;
 	}
 
 	public DefaultTableModel deletedColumnReactionsTableModel(int index) {
 		DefaultTableModel reacModel = (DefaultTableModel) reactionsTable.getModel();
 		DefaultTableModel model = new DefaultTableModel();	
 		ArrayList<String> newMetaColumnNames = new ArrayList<String>();
 		for (int r = 0; r < GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES.length; r++) {
 			model.addColumn(GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[r]);
 		} 
 		for (int m = 0; m < LocalConfig.getInstance().getReactionsMetaColumnNames().size(); m++) {
 			if (m != index - GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES.length) {
 				model.addColumn(LocalConfig.getInstance().getReactionsMetaColumnNames().get(m));
 				newMetaColumnNames.add(LocalConfig.getInstance().getReactionsMetaColumnNames().get(m));
 			}
 		}
 		LocalConfig.getInstance().setReactionsMetaColumnNames(newMetaColumnNames);
 		for (int i = 0; i < reacModel.getRowCount(); i++) {
 			Vector <String> reacRow = new Vector<String>();
 			for (int j = 0; j < reacModel.getColumnCount(); j++) {
 				if (j != index) {	
 					if (reacModel.getValueAt(i, j) != null) {
 						reacRow.add((String) reacModel.getValueAt(i, j));
 					} else {
 						reacRow.add("");
 					}					
 				}				
 			}
 			model.addRow(reacRow);
 		}
 		return model;
 	}
 
 	public DefaultTableModel deletedColumnMetabolitesTableModel(int index) {
 		DefaultTableModel metabModel = (DefaultTableModel) metabolitesTable.getModel();
 		DefaultTableModel model = new DefaultTableModel();	
 		ArrayList<String> newMetaColumnNames = new ArrayList<String>();
 		for (int r = 0; r < GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES.length; r++) {
 			model.addColumn(GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES[r]);
 		} 
 		for (int m = 0; m < LocalConfig.getInstance().getMetabolitesMetaColumnNames().size(); m++) {
 			if (m != index - GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES.length) {
 				model.addColumn(LocalConfig.getInstance().getMetabolitesMetaColumnNames().get(m));
 				newMetaColumnNames.add(LocalConfig.getInstance().getMetabolitesMetaColumnNames().get(m));
 			}
 		}
 		LocalConfig.getInstance().setMetabolitesMetaColumnNames(newMetaColumnNames);
 		for (int i = 0; i < metabModel.getRowCount(); i++) {
 			Vector <String> metabRow = new Vector<String>();
 			for (int j = 0; j < metabModel.getColumnCount(); j++) {
 				if (j != index) {	
 					if (metabModel.getValueAt(i, j) != null) {
 						metabRow.add((String) metabModel.getValueAt(i, j));
 					} else {
 						metabRow.add("");
 					}					
 				}				
 			}
 			model.addRow(metabRow);
 		}
 		return model;
 	}
 
 	/******************************************************************************/
 	//end reload tables methods
 	/******************************************************************************/
 
 	/*******************************************************************************/
 	//table layouts
 	/*******************************************************************************/
 
 	// start reactions table layout
 	public void positionColumn(JXTable table,int col_Index) {
 		table.moveColumn(table.getColumnCount()-1, col_Index);
 	}
 
 	//from http://www.roseindia.net/java/example/java/swing/ChangeColumnName.shtml  
 	public void ChangeName(JXTable table, int col_index, String col_name){
 		table.getColumnModel().getColumn(col_index).setHeaderValue(col_name);
 	}
 
 	//used in numerical columns so they are sorted by value and not as strings
 	Comparator numberComparator = new Comparator() {
 		public int compare(Object o1, Object o2) {
 			Double d1 = Double.valueOf(o1 == null ? "0" : (String)o1);
 			Double d2 = d1;
 			try {
 				d2 = Double.valueOf(o2 == null ? "0" : (String)o2);
 			} catch (Throwable t) {
 
 			}			
 			return d1.compareTo(d2);
 		}
 	};
 
 	private class ReactionsRowListener implements ListSelectionListener {
 		public void valueChanged(ListSelectionEvent event) {
 			if (LocalConfig.getInstance().findReplaceFocusLost) {
 				findButtonReactionsClicked = false;
 				throwNotFoundError = false;
 				if (getFindReplaceDialog() != null && !LocalConfig.getInstance().addReactantPromptShown) {
 					getFindReplaceDialog().replaceButton.setEnabled(false);
 					getFindReplaceDialog().replaceAllButton.setEnabled(false);
 					//getFindReplaceDialog().replaceFindButton.setEnabled(false);
 				}
 			}
 			String reactionRow = Integer.toString((reactionsTable.getSelectedRow() + 1));
 			if (LocalConfig.getInstance().getSuspiciousMetabolites().size() > 0) {
 				setLoadErrorMessage("Model contains suspicious metabolites.");
 				statusBar.setText("Row " + reactionRow + "                   " + getLoadErrorMessage());
 			} else {
 				statusBar.setText("Row " + reactionRow);
 			}
 			if (reactionsTable.getRowCount() > 0 && reactionsTable.getSelectedRow() > -1 && tabbedPane.getSelectedIndex() == 0) {
 				enableOrDisableReactionsItems();
 				// if any cell selected any existing find all highlighting is unhighlighted
 				reactionsFindAll = false;
 				metabolitesFindAll = false;	
 				reactionsTable.repaint();
 				metabolitesTable.repaint();  
 				selectedCellChanged = true;
 				changeReactionFindSelection = true;
 				int viewRow = reactionsTable.convertRowIndexToModel(reactionsTable.getSelectedRow());
 				// prevents invisible id column from setting id in formulaBar for find events
 				if (reactionsTable.getSelectedColumn() > 0) {
 					try {
 						formulaBar.setText((String) reactionsTable.getModel().getValueAt(viewRow, reactionsTable.getSelectedColumn()));
 					} catch (Throwable t) {
 
 					} 
 				}    			  						
 			} else {
 				// to catch strange error "Attempt to mutate in notification" 
 				try {
 					formulaBar.setText("");
 				} catch (Throwable t) {
 
 				}
 
 			}
 			if (event.getValueIsAdjusting()) {
 				return;
 			}
 		}
 	}
 
 	private class ReactionsColumnListener implements ListSelectionListener {
 		public void valueChanged(ListSelectionEvent event) {
 			if (LocalConfig.getInstance().findReplaceFocusLost) {
 				findButtonReactionsClicked = false;
 				throwNotFoundError = false;
 				if (getFindReplaceDialog() != null && !LocalConfig.getInstance().addReactantPromptShown) {
 					getFindReplaceDialog().replaceButton.setEnabled(false);
 					getFindReplaceDialog().replaceAllButton.setEnabled(false);
 					//getFindReplaceDialog().replaceFindButton.setEnabled(false);
 				}				
 			}
 			if (reactionsTable.getSelectedRow() > -1 && reactionsTable.getSelectedColumn() > -1 && tabbedPane.getSelectedIndex() == 0) {
 				String reactionRow = Integer.toString((reactionsTable.getSelectedRow() + 1));
 				if (LocalConfig.getInstance().getSuspiciousMetabolites().size() > 0) {
 					setLoadErrorMessage("Model contains suspicious metabolites.");
 					statusBar.setText("Row " + reactionRow + "                   " + getLoadErrorMessage());
 				} else {
 					statusBar.setText("Row " + reactionRow);
 				}
 				enableOrDisableReactionsItems();
 				// if any cell selected any existing find all highlighting is unhighlighted
 				reactionsFindAll = false;
 				metabolitesFindAll = false;	
 				reactionsTable.repaint();
 				metabolitesTable.repaint();
 				selectedCellChanged = true;	
 				changeReactionFindSelection = true;
 				int viewRow = reactionsTable.convertRowIndexToModel(reactionsTable.getSelectedRow());
 				// prevents invisible id column from setting id in formulaBar for find events
 				if (reactionsTable.getSelectedColumn() > 0) {
 					try {
 						formulaBar.setText((String) reactionsTable.getModel().getValueAt(viewRow, reactionsTable.getSelectedColumn()));				
 					} catch (Throwable t) {
 
 					}					
 				}				
 			} else {
 				formulaBar.setText("");
 			} 
 			if (event.getValueIsAdjusting()) {
 				return;
 			}
 		}
 	}
 
 	// disables items if cell is non-editable
 	public void enableOrDisableReactionsItems() {
 		if (reactionsTable.getSelectedColumn() == GraphicalInterfaceConstants.REVERSIBLE_COLUMN || reactionsTable.getSelectedColumn() == GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN) {
 			formulaBar.setEditable(false);
 			formulaBar.setBackground(Color.WHITE);
 			formulaBarPasteItem.setEnabled(false);
 			pastebutton.setEnabled(false);
 		}  else {
 			if (isRoot) {
 				formulaBar.setEditable(true);
 				formulaBarPasteItem.setEnabled(true);
 				pastebutton.setEnabled(true);
 			}					
 		}
 	}
 
 	// saves sort column and order so it is preserved when table is refreshed
 	// after editing and updating database
 	class ReactionsColumnHeaderListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent evt) {
 			int r = reactionsTable.getSortedColumnIndex();
 			// bug: mouse listener sets multiple undo items for one event at times
 			// this fix works
 			if (r != getReactionsSortColumnIndex() || reactionsTable.getSortOrder(reactionsTable.getSortedColumnIndex()) != getReactionsSortOrder()) {
 				  ReactionUndoItem undoItem = createReactionUndoItem("", "", reactionsTable.getSelectedRow(), reactionsTable.getSelectedColumn(), 1, UndoConstants.SORT, UndoConstants.REACTION_UNDO_ITEM_TYPE);
 				  LocalConfig.getInstance().getReactionsSortColumns().add(r);
 				  LocalConfig.getInstance().getReactionsSortOrderList().add(reactionsTable.getSortOrder(reactionsTable.getSortedColumnIndex()));
 				  undoItem.setOldSortColumnIndex(LocalConfig.getInstance().getReactionsSortColumns().get(LocalConfig.getInstance().getReactionsSortColumns().size() - 2));
 				  undoItem.setNewSortColumnIndex(LocalConfig.getInstance().getReactionsSortColumns().get(LocalConfig.getInstance().getReactionsSortColumns().size() - 1));
 				  undoItem.setOldSortOrder(LocalConfig.getInstance().getReactionsSortOrderList().get(LocalConfig.getInstance().getReactionsSortOrderList().size() - 2));
 				  undoItem.setNewSortOrder(LocalConfig.getInstance().getReactionsSortOrderList().get(LocalConfig.getInstance().getReactionsSortOrderList().size() - 1));
 				  setUpReactionsUndo(undoItem);
 			}
 			setReactionsSortColumnIndex(r);
 			setReactionsSortOrder(reactionsTable.getSortOrder(reactionsTable.getSortedColumnIndex()));
 		}
 	}
 
 	HighlightPredicate reactionsSelectedAreaPredicate = new HighlightPredicate() {
 		public boolean isHighlighted(Component renderer ,ComponentAdapter adapter) {
 			if (findMode) {
 				if (searchSelectedArea) {
 					if (findButtonReactionsClicked) {
 						if (adapter.row >= selectedReactionsRowStartIndex && adapter.row < selectedReactionsRowEndIndex && adapter.column >= selectedReactionsColumnStartIndex && adapter.column < selectedReactionsColumnEndIndex) {
 							return true;
 						}
 					}					
 				}
 			}
 
 			return false;
 		}
 	};
 
 	ColorHighlighter reactionsSelectedArea = new ColorHighlighter(reactionsSelectedAreaPredicate, GraphicalInterfaceConstants.SELECTED_AREA_COLOR, null);
 
 	HighlightPredicate reactionFindAllPredicate = new HighlightPredicate() {
 		public boolean isHighlighted(Component renderer ,ComponentAdapter adapter) {
 			if (findMode) {
 				if (reactionsFindAll) {
 					for (int i = 0; i < getReactionsFindLocationsList().size(); i++) {
 						if (adapter.row == getReactionsFindLocationsList().get(i).get(0) && adapter.column == getReactionsFindLocationsList().get(i).get(1)) {
 							return true;
 						}
 					}
 				} else {
 					if (getReactionsReplaceLocation().size() > 0 && !changeReactionFindSelection) {
 						if (adapter.row == getReactionsReplaceLocation().get(0) && adapter.column == getReactionsReplaceLocation().get(1)) {
 							return true;
 						}
 					}				
 				}
 			}
 
 			return false;
 		}
 	};
 
 	ColorHighlighter reactionFindAll = new ColorHighlighter(reactionFindAllPredicate, GraphicalInterfaceConstants.FIND_ALL_COLOR, null);
 
 	HighlightPredicate participatingPredicate = new HighlightPredicate() {
 		public boolean isHighlighted(Component renderer ,ComponentAdapter adapter) {
 			int viewRow = reactionsTable.convertRowIndexToModel(adapter.row);
 			int id = Integer.valueOf(reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTIONS_ID_COLUMN).toString());					
 			if (highlightParticipatingRxns == true && LocalConfig.getInstance().getParticipatingReactions().contains(id)) {									
 				return true;
 			}						
 			return false;
 		}
 	};
 
 	ColorHighlighter participating = new ColorHighlighter(participatingPredicate, Color.GREEN, null);
 
 	HighlightPredicate invalidReactionPredicate = new HighlightPredicate() {
 		public boolean isHighlighted(Component renderer ,ComponentAdapter adapter) {			
 			if (adapter.getValue() != null && LocalConfig.getInstance().getInvalidReactions().contains(adapter.getValue().toString())) {			
 				return true;
 			}				
 			return false;
 		}
 	};
 
 	ColorHighlighter invalidReaction = new ColorHighlighter(invalidReactionPredicate, Color.RED, null);
 
 	public void setReactionsTableLayout() {
 		reactionsTable.getSelectionModel().addListSelectionListener(new ReactionsRowListener());
 		reactionsTable.getColumnModel().getSelectionModel().
 		addListSelectionListener(new ReactionsColumnListener());
 
 		reactionsTable.setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);
 		reactionsTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 
 		// Comparator allows numerical columns to be sorted by numeric value and
 		// not like strings
 		reactionsTable.getColumnExt(GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.REACTIONS_ID_COLUMN]).setComparator(numberComparator);
 		reactionsTable.getColumnExt(GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.FLUX_VALUE_COLUMN]).setComparator(numberComparator);
 		reactionsTable.getColumnExt(GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.LOWER_BOUND_COLUMN]).setComparator(numberComparator);
 		reactionsTable.getColumnExt(GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.UPPER_BOUND_COLUMN]).setComparator(numberComparator);
 		reactionsTable.getColumnExt(GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.BIOLOGICAL_OBJECTIVE_COLUMN]).setComparator(numberComparator);
 		reactionsTable.getColumnExt(GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.SYNTHETIC_OBJECTIVE_COLUMN]).setComparator(numberComparator);
 
 		reactionsTable.addHighlighter(participating);
 		reactionsTable.addHighlighter(invalidReaction);
 		reactionsTable.addHighlighter(reactionsSelectedArea);
 		reactionsTable.addHighlighter(reactionFindAll);	
 
 		// these columns have names that are too long to fit in cell and need tooltips
 		// also KO has Knockout as tooltip
 		ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
 		for (int c = 0; c < reactionsTable.getColumnCount(); c++) {
 			TableColumn col = reactionsTable.getColumnModel().getColumn(c);
 			if (c == GraphicalInterfaceConstants.BIOLOGICAL_OBJECTIVE_COLUMN) {
 				tips.setToolTip(col, GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.BIOLOGICAL_OBJECTIVE_COLUMN]);
 			}
 			if (c == GraphicalInterfaceConstants.SYNTHETIC_OBJECTIVE_COLUMN) {
 				tips.setToolTip(col, GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.SYNTHETIC_OBJECTIVE_COLUMN]);
 			}
 			if (c == GraphicalInterfaceConstants.REVERSIBLE_COLUMN) {
 				tips.setToolTip(col, GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.REVERSIBLE_COLUMN]);
 			}
 			if (c == GraphicalInterfaceConstants.KO_COLUMN) {
 				tips.setToolTip(col, GraphicalInterfaceConstants.KNOCKOUT_TOOLTIP);
 			}
 			if (c == GraphicalInterfaceConstants.GENE_ASSOCIATION_COLUMN) {
 				tips.setToolTip(col, GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.GENE_ASSOCIATION_COLUMN]);
 			}
 		}
 		reactionsTable.getTableHeader().addMouseMotionListener(tips);
 		reactionsTable.getTableHeader().addMouseListener(new ReactionsColumnHeaderListener());
 		reactionsTable.getTableHeader().addMouseListener(new ReactionsHeaderPopupListener());	
 
 		//from http://www.java2s.com/Tutorial/Java/0240__Swing/thelastcolumnismovedtothefirstposition.htm
 		// columns cannot be rearranged by dragging
 		reactionsTable.getTableHeader().setReorderingAllowed(false);  
 
 		int r = reactionsTable.getModel().getColumnCount();
 		for (int i = 0; i < r; i++) {	    	
 			//set background of id column to grey
 			ColorTableCellRenderer reacGreyRenderer = new ColorTableCellRenderer();
 			ReactionsTableCellRenderer reacRenderer = new ReactionsTableCellRenderer();
 
 			TableColumn column = reactionsTable.getColumnModel().getColumn(i);
 			column.setPreferredWidth(GraphicalInterfaceConstants.REACTION_META_DEFAULT_WIDTH); 
 			if (i==GraphicalInterfaceConstants.REACTIONS_ID_COLUMN) {
 				if (showIdColumn) {
 					column.setPreferredWidth(GraphicalInterfaceConstants.REACTIONS_ID_WIDTH); 
 				} else {
 					//sets column not visible
 					column.setMaxWidth(0);
 					column.setMinWidth(0); 
 					column.setWidth(0); 
 					column.setPreferredWidth(0);
 				}		
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.REACTIONS_ID_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.REACTIONS_ID_COLUMN]); 
 				column.setCellRenderer(reacGreyRenderer);
 				//sets color of id column to grey
 				reacGreyRenderer.setHorizontalAlignment(JLabel.CENTER);	
 			} else {
 				column.setCellRenderer(reacRenderer);
 			}
 			if (i==GraphicalInterfaceConstants.KO_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.KO_WIDTH); 
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.KO_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.KO_COLUMN]); 
 			}
 			if (i==GraphicalInterfaceConstants.FLUX_VALUE_COLUMN) {
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.FLUX_VALUE_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.FLUX_VALUE_COLUMN]); 
 			}
 			if (i==GraphicalInterfaceConstants.REACTION_ABBREVIATION_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.REACTION_ABBREVIATION_WIDTH);//2
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.REACTION_ABBREVIATION_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.REACTION_ABBREVIATION_COLUMN]); 
 			}
 			if (i==GraphicalInterfaceConstants.REACTION_NAME_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.REACTION_NAME_WIDTH);
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.REACTION_NAME_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.REACTION_NAME_COLUMN]);     
 			}
 			if (i==GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.REACTION_EQUN_ABBR_WIDTH);//3  
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN]); 
 			}
 			if (i==GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.REACTION_EQUN_NAMES_WIDTH);//4  
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN]); 
 			}
 			if (i==GraphicalInterfaceConstants.REVERSIBLE_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.REVERSIBLE_WIDTH);        //5
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.REVERSIBLE_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.REVERSIBLE_COLUMN]); 
 			}
 			if (i==GraphicalInterfaceConstants.LOWER_BOUND_COLUMN) {
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.LOWER_BOUND_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.LOWER_BOUND_COLUMN]);          
 			}
 			if (i==GraphicalInterfaceConstants.UPPER_BOUND_COLUMN) {
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.UPPER_BOUND_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.UPPER_BOUND_COLUMN]);          
 			} 
 			if (i==GraphicalInterfaceConstants.BIOLOGICAL_OBJECTIVE_COLUMN) {
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.BIOLOGICAL_OBJECTIVE_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.BIOLOGICAL_OBJECTIVE_COLUMN]); 
 			}
 			if (i==GraphicalInterfaceConstants.SYNTHETIC_OBJECTIVE_COLUMN) {
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.SYNTHETIC_OBJECTIVE_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.SYNTHETIC_OBJECTIVE_COLUMN]); 
 			}
 			//set alignment of columns with numerical values to right, and default width
 			if (i==GraphicalInterfaceConstants.BIOLOGICAL_OBJECTIVE_COLUMN || i==GraphicalInterfaceConstants.LOWER_BOUND_COLUMN
 					|| i==GraphicalInterfaceConstants.UPPER_BOUND_COLUMN || i==GraphicalInterfaceConstants.FLUX_VALUE_COLUMN || 
 					i==GraphicalInterfaceConstants.SYNTHETIC_OBJECTIVE_COLUMN) {	  
 				reacRenderer.setHorizontalAlignment(JLabel.RIGHT); 
 				column.setPreferredWidth(GraphicalInterfaceConstants.DEFAULT_WIDTH);
 			} 
 
 			if (i==GraphicalInterfaceConstants.GENE_ASSOCIATION_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.REACTION_META_DEFAULT_WIDTH);        
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.GENE_ASSOCIATION_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.GENE_ASSOCIATION_COLUMN]); 
 			}
 			if (i==GraphicalInterfaceConstants.PROTEIN_ASSOCIATION_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.REACTION_META_DEFAULT_WIDTH);        
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.PROTEIN_ASSOCIATION_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.PROTEIN_ASSOCIATION_COLUMN]); 
 			}
 			if (i==GraphicalInterfaceConstants.SUBSYSTEM_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.REACTION_META_DEFAULT_WIDTH);        
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.SUBSYSTEM_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.SUBSYSTEM_COLUMN]); 
 			}
 			if (i==GraphicalInterfaceConstants.PROTEIN_CLASS_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.REACTION_META_DEFAULT_WIDTH);        
 				ChangeName(reactionsTable, GraphicalInterfaceConstants.PROTEIN_CLASS_COLUMN, 
 						GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[GraphicalInterfaceConstants.PROTEIN_CLASS_COLUMN]); 
 			}
 			if (i > GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES.length - 1) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.REACTION_META_DEFAULT_WIDTH);        
 			}
 			// only scrolls all the way to the right when column added
 			if (addReacColumn) {
 				scrollToLocation(reactionsTable, 0, LocalConfig.getInstance().getReactionsMetaColumnNames().size() + GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES.length);
 			}
 		}
 	}
 
 	/********************************************************************************/
 	// end reactions table layout
 	/********************************************************************************/
 
 	/********************************************************************************/
 	// start metabolites table layout
 	/********************************************************************************/
 
 	private class MetabolitesRowListener implements ListSelectionListener {
 		public void valueChanged(ListSelectionEvent event) {
 			highlightParticipatingRxns = false;
 			if (LocalConfig.getInstance().findReplaceFocusLost) {
 				findButtonMetabolitesClicked = false;
 				throwNotFoundError = false;
 				if (getFindReplaceDialog() != null && !LocalConfig.getInstance().addReactantPromptShown) {
 					getFindReplaceDialog().replaceButton.setEnabled(false);
 					getFindReplaceDialog().replaceAllButton.setEnabled(false);
 					//getFindReplaceDialog().replaceFindButton.setEnabled(false);
 				}				
 			}
 			String metaboliteRow = Integer.toString((metabolitesTable.getSelectedRow() + 1));
 			try {
 				if (LocalConfig.getInstance().getSuspiciousMetabolites().size() > 0) {
 					setLoadErrorMessage("Model contains suspicious metabolites.");
 					statusBar.setText("Row " + metaboliteRow + "                   " + getLoadErrorMessage());
 				} else {
 					statusBar.setText("Row " + metaboliteRow);
 				}
 			} catch (Throwable t) {
 
 			}			
 			if (metabolitesTable.getRowCount() > 0 && metabolitesTable.getSelectedRow() > -1 && tabbedPane.getSelectedIndex() == 1) {
 				if (metabolitesTable.getSelectedRow() > -1) {
 					int viewRow = metabolitesTable.convertRowIndexToModel(metabolitesTable.getSelectedRow());
 					//String value = (String) metabolitesTable.getModel().getValueAt(viewRow, metabolitesTable.getSelectedColumn()); 
 					enableOrDisableMetabolitesItems();
 					// if any cell selected any existing find all highlighting is unhighlighted
 					reactionsFindAll = false;
 					metabolitesFindAll = false;	
 					reactionsTable.repaint();
 					metabolitesTable.repaint();			 
 					selectedCellChanged = true;
 					changeMetaboliteFindSelection = true;
 					// prevents invisible id column from setting id in formulaBar for find events
 					if (metabolitesTable.getSelectedColumn() > 0) {
 						formulaBar.setText((String) metabolitesTable.getModel().getValueAt(viewRow, metabolitesTable.getSelectedColumn()));	
 					}	    			
 				} else {
 					formulaBar.setText("");
 				}
 			}
 			if (event.getValueIsAdjusting()) {
 				return;
 			}
 		}
 	}
 
 	private class MetabolitesColumnListener implements ListSelectionListener {
 		public void valueChanged(ListSelectionEvent event) {
 			if (LocalConfig.getInstance().findReplaceFocusLost) {
 				findButtonMetabolitesClicked = false;
 				throwNotFoundError = false;
 				if (getFindReplaceDialog() != null && !LocalConfig.getInstance().addReactantPromptShown) {
 					getFindReplaceDialog().replaceButton.setEnabled(false);
 					getFindReplaceDialog().replaceAllButton.setEnabled(false);
 					//getFindReplaceDialog().replaceFindButton.setEnabled(false);
 				}				
 			}
 			if (metabolitesTable.getSelectedRow() > -1 && metabolitesTable.getSelectedColumn() > -1 && tabbedPane.getSelectedIndex() == 1) {				
 				int viewRow = metabolitesTable.convertRowIndexToModel(metabolitesTable.getSelectedRow());
 				//String value = (String) metabolitesTable.getModel().getValueAt(viewRow, metabolitesTable.getSelectedColumn()); 
 				enableOrDisableMetabolitesItems();
 				// if any cell selected any existing find all highlighting is unhighlighted
 				reactionsFindAll = false;
 				metabolitesFindAll = false;	
 				reactionsTable.repaint();
 				metabolitesTable.repaint();								 
 				selectedCellChanged = true;
 				changeMetaboliteFindSelection = true;
 				// prevents invisible id column from setting id in formulaBar for find events
 				if (metabolitesTable.getSelectedColumn() > 0) {
 					formulaBar.setText((String) metabolitesTable.getModel().getValueAt(viewRow, metabolitesTable.getSelectedColumn()));			
 				}				
 				if (event.getValueIsAdjusting()) {
 					return;
 				}
 			} else {
 				formulaBar.setText("");
 			}			
 		}
 	}
 
 	// disables items if cell is non-editable
 	public void enableOrDisableMetabolitesItems() {
 		if (metabolitesTable.getSelectedRow() > -1 && metabolitesTable.getSelectedColumn() > 0) {
 			int viewRow = metabolitesTable.convertRowIndexToModel(metabolitesTable.getSelectedRow());
 			String value = (String) metabolitesTable.getModel().getValueAt(viewRow, metabolitesTable.getSelectedColumn()); 
 			if ((metabolitesTable.getSelectedColumn() == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN 
 					||  metabolitesTable.getSelectedColumn() == GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN)) {
 				/*
 				if ((metabolitesTable.getSelectedColumn() == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN 
 					||  metabolitesTable.getSelectedColumn() == GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN)
 					&& metabolitesTable.getModel().getValueAt(viewRow, metabolitesTable.getSelectedColumn()) != null && LocalConfig.getInstance().getMetaboliteUsedMap().containsKey(value)) {
 				 */
 				formulaBar.setEditable(false);
 				formulaBar.setBackground(Color.WHITE);
 				formulaBarPasteItem.setEnabled(false);
 				pastebutton.setEnabled(false);
 			}  else {
 				if (isRoot) {
 					formulaBar.setEditable(true);
 					formulaBarPasteItem.setEnabled(true);
 					pastebutton.setEnabled(true);
 				}					
 			}
 		}		
 	}
 
 	class MetabolitesColumnHeaderListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent evt) {
 			int m = metabolitesTable.getSortedColumnIndex();
 			// bug: mouse listener sets multiple undo items
 			// this fix works
 			if (m != getMetabolitesSortColumnIndex() || metabolitesTable.getSortOrder(metabolitesTable.getSortedColumnIndex()) != getMetabolitesSortOrder()) {
 				  MetaboliteUndoItem undoItem = createMetaboliteUndoItem("", "", metabolitesTable.getSelectedRow(), metabolitesTable.getSelectedColumn(), 1, UndoConstants.SORT, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);
 				  setUndoOldCollections(undoItem);
 				  LocalConfig.getInstance().getMetabolitesSortColumns().add(m);
 				  LocalConfig.getInstance().getMetabolitesSortOrderList().add(metabolitesTable.getSortOrder(metabolitesTable.getSortedColumnIndex()));
 				  undoItem.setOldSortColumnIndex(LocalConfig.getInstance().getMetabolitesSortColumns().get(LocalConfig.getInstance().getMetabolitesSortColumns().size() - 2));
 				  undoItem.setNewSortColumnIndex(LocalConfig.getInstance().getMetabolitesSortColumns().get(LocalConfig.getInstance().getMetabolitesSortColumns().size() - 1));
 				  undoItem.setOldSortOrder(LocalConfig.getInstance().getMetabolitesSortOrderList().get(LocalConfig.getInstance().getMetabolitesSortOrderList().size() - 2));
 				  undoItem.setNewSortOrder(LocalConfig.getInstance().getMetabolitesSortOrderList().get(LocalConfig.getInstance().getMetabolitesSortOrderList().size() - 1));
 				  setUpMetabolitesUndo(undoItem);
 			}
 			setMetabolitesSortColumnIndex(m);
 			setMetabolitesSortOrder(metabolitesTable.getSortOrder(metabolitesTable.getSortedColumnIndex()));
 		}
 	}
 
 	HighlightPredicate suspiciousPredicate = new HighlightPredicate() {
 		public boolean isHighlighted(Component renderer ,ComponentAdapter adapter) {
 			int viewRow = metabolitesTable.convertRowIndexToModel(adapter.row);			
 			int id = Integer.valueOf(metabolitesTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.METABOLITE_ID_COLUMN).toString());					
 			if (metabolitesTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) != null) {
 				if (LocalConfig.getInstance().getSuspiciousMetabolites().contains(id)) {					
 					return true;
 				}
 			}						
 			return false;
 		}
 	};
 
 	ColorHighlighter suspicious = new ColorHighlighter(suspiciousPredicate, Color.RED, null);
 
 	HighlightPredicate unusedPredicate = new HighlightPredicate() {
 		public boolean isHighlighted(Component renderer ,ComponentAdapter adapter) {
 			int viewRow = metabolitesTable.convertRowIndexToModel(adapter.row);
 			if (metabolitesTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) != null) {
 				if (highlightUnusedMetabolites == true && !(LocalConfig.getInstance().getMetaboliteUsedMap().containsKey(metabolitesTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN).toString()))) {					
 					return true;
 				}
 			}						
 			return false;
 		}
 	};
 
 	ColorHighlighter unused = new ColorHighlighter(unusedPredicate, Color.YELLOW, null);
 
 	HighlightPredicate metabolitesSelectedAreaPredicate = new HighlightPredicate() {
 		public boolean isHighlighted(Component renderer ,ComponentAdapter adapter) {
 			if (findMode) {
 				if (searchSelectedArea) {
 					if (findButtonMetabolitesClicked) {
 						if (adapter.row >= selectedMetabolitesRowStartIndex && adapter.row < selectedMetabolitesRowEndIndex && adapter.column >= selectedMetabolitesColumnStartIndex && adapter.column < selectedMetabolitesColumnEndIndex) {
 							return true;
 						}
 					}					
 				}
 			}
 
 			return false;
 		}
 	};
 
 	ColorHighlighter metabolitesSelectedArea = new ColorHighlighter(metabolitesSelectedAreaPredicate, GraphicalInterfaceConstants.SELECTED_AREA_COLOR, null);
 
 	HighlightPredicate metaboliteFindAllPredicate = new HighlightPredicate() {
 		public boolean isHighlighted(Component renderer ,ComponentAdapter adapter) {
 			if (findMode) {
 				if (metabolitesFindAll) {
 					for (int i = 0; i < getMetabolitesFindLocationsList().size(); i++) {
 						if (adapter.row == getMetabolitesFindLocationsList().get(i).get(0) && adapter.column == getMetabolitesFindLocationsList().get(i).get(1)) {
 							return true;
 						}
 					}
 				} else {
 					if (getMetabolitesReplaceLocation().size() > 0 && !changeMetaboliteFindSelection) {
 						if (adapter.row == getMetabolitesReplaceLocation().get(0) && adapter.column == getMetabolitesReplaceLocation().get(1)) {
 							return true;
 						}
 					}				
 				}
 			}								
 			return false;
 		}
 	};
 
 	ColorHighlighter metaboliteFindAll = new ColorHighlighter(metaboliteFindAllPredicate, GraphicalInterfaceConstants.FIND_ALL_COLOR, null);
 
 	public void setMetabolitesTableLayout() {
 		metabolitesTable.getSelectionModel().addListSelectionListener(new MetabolitesRowListener());
 		metabolitesTable.getColumnModel().getSelectionModel().
 		addListSelectionListener(new MetabolitesColumnListener());
 
 		metabolitesTable.setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);
 		metabolitesTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 
 		metabolitesTable.getColumnExt(GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES[GraphicalInterfaceConstants.METABOLITE_ID_COLUMN]).setComparator(numberComparator);
 
 		metabolitesTable.addHighlighter(unused);
 		metabolitesTable.addHighlighter(suspicious);
 		metabolitesTable.addHighlighter(metabolitesSelectedArea);
 		metabolitesTable.addHighlighter(metaboliteFindAll);
 
 		ColumnHeaderToolTips tips = new ColumnHeaderToolTips();		
 
 		for (int c = 0; c < metabolitesTable.getColumnCount(); c++) {
 			TableColumn col = metabolitesTable.getColumnModel().getColumn(c);	
 			if (c == GraphicalInterfaceConstants.CHARGE_COLUMN) {
 				tips.setToolTip(col, GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES[GraphicalInterfaceConstants.CHARGE_COLUMN]);     
 			}
 			if (c == GraphicalInterfaceConstants.BOUNDARY_COLUMN) {
 				tips.setToolTip(col, GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES[GraphicalInterfaceConstants.BOUNDARY_COLUMN]);     
 			}
 		}
 		metabolitesTable.getTableHeader().addMouseMotionListener(tips);	
 		metabolitesTable.getTableHeader().addMouseListener(new MetabolitesColumnHeaderListener());
 		metabolitesTable.getTableHeader().addMouseListener(new MetabolitesHeaderPopupListener());
 
 		// from http://www.java2s.com/Tutorial/Java/0240__Swing/thelastcolumnismovedtothefirstposition.htm
 		// columns cannot be rearranged by dragging 
 		metabolitesTable.getTableHeader().setReorderingAllowed(false);  
 
 		int m = metabolitesTable.getModel().getColumnCount();
 		for (int w = 0; w < m; w++) {
 			ColorTableCellRenderer metabGreyRenderer = new ColorTableCellRenderer();
 			MetabolitesTableCellRenderer metabRenderer = new MetabolitesTableCellRenderer();
 
 			TableColumn column = metabolitesTable.getColumnModel().getColumn(w);
 			column.setPreferredWidth(GraphicalInterfaceConstants.METABOLITE_META_DEFAULT_WIDTH); 
 			if (w==GraphicalInterfaceConstants.METABOLITE_ID_COLUMN) {
 				if (showIdColumn) {
 					column.setPreferredWidth(GraphicalInterfaceConstants.METABOLITE_ID_WIDTH);
 				} else {
 					//sets column not visible
 					column.setMaxWidth(0);
 					column.setMinWidth(0); 
 					column.setWidth(0); 
 					column.setPreferredWidth(0);
 				}		
 				ChangeName(metabolitesTable, GraphicalInterfaceConstants.METABOLITE_ID_COLUMN, 
 						GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES[GraphicalInterfaceConstants.METABOLITE_ID_COLUMN]);     
 				column.setCellRenderer(metabGreyRenderer);
 				metabGreyRenderer.setHorizontalAlignment(JLabel.CENTER);
 			} else {	    		  
 				column.setCellRenderer(metabRenderer); 	    		  
 			}
 			if (w==GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_WIDTH);
 				ChangeName(metabolitesTable, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN, 
 						GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES[GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN]);     
 			}
 			if (w==GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.METABOLITE_NAME_WIDTH);
 				ChangeName(metabolitesTable, GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN, 
 						GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES[GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN]);     
 			} 
 			if (w==GraphicalInterfaceConstants.CHARGE_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.CHARGE_WIDTH);
 				ChangeName(metabolitesTable, GraphicalInterfaceConstants.CHARGE_COLUMN, 
 						GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES[GraphicalInterfaceConstants.CHARGE_COLUMN]);     
 				metabRenderer.setHorizontalAlignment(JLabel.RIGHT);
 			}
 			if (w==GraphicalInterfaceConstants.COMPARTMENT_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.COMPARTMENT_WIDTH);
 				ChangeName(metabolitesTable, GraphicalInterfaceConstants.COMPARTMENT_COLUMN, 
 						GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES[GraphicalInterfaceConstants.COMPARTMENT_COLUMN]);     
 			}	    	  	    	  
 			if (w==GraphicalInterfaceConstants.BOUNDARY_COLUMN) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.BOUNDARY_WIDTH);
 				ChangeName(metabolitesTable, GraphicalInterfaceConstants.BOUNDARY_COLUMN, 
 						GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES[GraphicalInterfaceConstants.BOUNDARY_COLUMN]);     
 			}
 			if (w > GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES.length - 1) {
 				column.setPreferredWidth(GraphicalInterfaceConstants.METABOLITE_META_DEFAULT_WIDTH);
 			}
 			// only scrolls all the way to the right when column added
 			if (addMetabColumn) {
 				scrollToLocation(metabolitesTable, 0, LocalConfig.getInstance().getMetabolitesMetaColumnNames().size() + GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES.length);
 			}
 		}	  
 	}
 
 	public Vector<String> createReactionsRow(int id)
 	{
 		Vector<String> row = new Vector<String>();
 		row.addElement(Integer.toString(id));
 		// start at 1 since id is already added
 		for (int i = 1; i < GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES.length; i++) {		  
 			if (i==GraphicalInterfaceConstants.KO_COLUMN) {
 				row.addElement(GraphicalInterfaceConstants.KO_DEFAULT);
 			} else if (i==GraphicalInterfaceConstants.FLUX_VALUE_COLUMN) {
 				row.addElement(GraphicalInterfaceConstants.FLUX_VALUE_DEFAULT_STRING);
 			} else if (i==GraphicalInterfaceConstants.REVERSIBLE_COLUMN) {
 				row.addElement(GraphicalInterfaceConstants.REVERSIBLE_DEFAULT);
 			} else if (i==GraphicalInterfaceConstants.LOWER_BOUND_COLUMN) {
 				row.addElement(GraphicalInterfaceConstants.LOWER_BOUND_DEFAULT_REVERSIBLE_STRING);
 			} else if (i==GraphicalInterfaceConstants.UPPER_BOUND_COLUMN) {
 				row.addElement(GraphicalInterfaceConstants.UPPER_BOUND_DEFAULT_STRING);
 			} else if (i==GraphicalInterfaceConstants.BIOLOGICAL_OBJECTIVE_COLUMN) {
 				row.addElement(GraphicalInterfaceConstants.BIOLOGICAL_OBJECTIVE_DEFAULT_STRING);
 			} else if (i==GraphicalInterfaceConstants.SYNTHETIC_OBJECTIVE_COLUMN) {
 				row.addElement(GraphicalInterfaceConstants.SYNTHETIC_OBJECTIVE_DEFAULT_STRING);
 			} else {
 				row.addElement("");
 			}		  
 		}
 		return row;
 	}
 
 	public Vector<String> createMetabolitesRow(int id)
 	{
 		Vector<String> row = new Vector<String>();
 		row.addElement(Integer.toString(id));
 		for (int i = 1; i < GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES.length; i++) {
 			if (i==GraphicalInterfaceConstants.BOUNDARY_COLUMN) {
 				row.addElement(GraphicalInterfaceConstants.BOUNDARY_DEFAULT);
 			} else {
 				row.addElement("");
 			}	
 		}
 		return row;
 	}
 
 	/************************************************************************************/
 	//end table layouts
 	/************************************************************************************/
 
 	/******************************************************************************/
 	//set table properties
 	/******************************************************************************/
 
 	public void setTableCellFocused(int row, int col, JXTable table) {
 		table.changeSelection(row, col, false, false);
 		table.requestFocus();
 	}
 
 	public void setUpColumnSelectionMode() {
 		setSelectionMode(1);
 		reactionsTable.setColumnSelectionAllowed(true);
 		reactionsTable.setRowSelectionAllowed(false); 
 		metabolitesTable.setColumnSelectionAllowed(true);
 		metabolitesTable.setRowSelectionAllowed(false);
 	}
 
 	public void setUpRowSelectionMode() {
 		setSelectionMode(2);
 		reactionsTable.setColumnSelectionAllowed(false);
 		reactionsTable.setRowSelectionAllowed(true); 
 		metabolitesTable.setColumnSelectionAllowed(false);
 		metabolitesTable.setRowSelectionAllowed(true);
 	}
 
 	public void setUpCellSelectionMode() {
 		setSelectionMode(0);
 		reactionsTable.setColumnSelectionAllowed(false);
 		reactionsTable.setRowSelectionAllowed(false); 
 		reactionsTable.setCellSelectionEnabled(true);
 		metabolitesTable.setColumnSelectionAllowed(false);
 		metabolitesTable.setRowSelectionAllowed(false); 
 		metabolitesTable.setCellSelectionEnabled(true);
 	}
 
 	// sets sorted by db id and ascending
 	public void setSortDefault() {
 		setReactionsSortColumnIndex(0);
 		setMetabolitesSortColumnIndex(0);
 		setReactionsSortOrder(SortOrder.ASCENDING);
 		setMetabolitesSortOrder(SortOrder.ASCENDING);
 	}
 
 	/*******************************************************************************/
 	//end table methods and actions
 	/*******************************************************************************/
 
 	/************************************************************************************/
 	//table header context menus
 	/************************************************************************************/
 
 	public class ReactionsHeaderPopupListener extends MouseAdapter {
 
 		public void maybeShowPopup(MouseEvent e) {
 			if (e.isPopupTrigger() && reactionsTable.isEnabled()) {
 				Point p = new Point(e.getX(), e.getY());
 				int col = reactionsTable.columnAtPoint(p);
 				JPopupMenu reactionsHeaderContextMenu = createReactionsHeaderContextMenu(col);
 				// ... and show it
 				if (reactionsHeaderContextMenu != null
 						&& reactionsHeaderContextMenu.getComponentCount() > 0) {
 					reactionsHeaderContextMenu.show(reactionsTable, p.x, p.y);
 				}
 			}
 		}
 
 		public void mousePressed(MouseEvent e) {
 			maybeShowPopup(e);
 		}
 
 		public void mouseReleased(MouseEvent e) {
 			maybeShowPopup(e);
 		}
 	}
 
 	private JPopupMenu createReactionsHeaderContextMenu(final int columnIndex) {
 		JPopupMenu reactionsHeaderContextMenu = new JPopupMenu();
 
 		JMenuItem deleteColumnMenu = new JMenuItem("Delete Column");
 		if (columnIndex > GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES.length - 1) {
 			deleteColumnMenu.setEnabled(true);
 		} else {
 			deleteColumnMenu.setEnabled(false);
 		}
 		deleteColumnMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				// copy old model for undo/redo
 				DefaultTableModel oldReactionsModel = copyReactionsTableModel((DefaultTableModel) reactionsTable.getModel());			
 				copyReactionsTableModels(oldReactionsModel);
 				ReactionUndoItem undoItem = createReactionUndoItem("", "", reactionsTable.getSelectedRow(), reactionsTable.getSelectedColumn(), 1, UndoConstants.DELETE_COLUMN, UndoConstants.REACTION_UNDO_ITEM_TYPE);
 				undoItem.setTableCopyIndex(LocalConfig.getInstance().getNumReactionTablesCopied());								
 				undoItem.setDeletedColumnIndex(columnIndex);
 				ArrayList<String> oldMetaCol = new ArrayList<String>();
 				ArrayList<String> newMetaCol = new ArrayList<String>();
 				for (int i = 0; i < LocalConfig.getInstance().getReactionsMetaColumnNames().size(); i++) {
 					oldMetaCol.add(LocalConfig.getInstance().getReactionsMetaColumnNames().get(i));
 				}
 				undoItem.setOldMetaColumnNames(oldMetaCol);
 				DefaultTableModel model = deletedColumnReactionsTableModel(columnIndex);
 				reactionsTable.setModel(model);
 				setUpReactionsTable(model);
 				copyReactionsTableModels(model);
 				for (int i = 0; i < LocalConfig.getInstance().getReactionsMetaColumnNames().size(); i++) {
 					newMetaCol.add(LocalConfig.getInstance().getReactionsMetaColumnNames().get(i));
 				}
 				undoItem.setNewMetaColumnNames(newMetaCol);
 				setUpReactionsUndo(undoItem);
 			}
 		});
 		reactionsHeaderContextMenu.add(deleteColumnMenu);	
 
 		return reactionsHeaderContextMenu;
 
 	}
 
 	public class MetabolitesHeaderPopupListener extends MouseAdapter {
 
 		public void maybeShowPopup(MouseEvent e) {
 			if (e.isPopupTrigger() && metabolitesTable.isEnabled()) {
 				Point p = new Point(e.getX(), e.getY());
 				int col = metabolitesTable.columnAtPoint(p);
 				JPopupMenu metabolitesHeaderContextMenu = createMetabolitesHeaderContextMenu(col);
 				// ... and show it
 				if (metabolitesHeaderContextMenu != null
 						&& metabolitesHeaderContextMenu.getComponentCount() > 0) {
 					metabolitesHeaderContextMenu.show(metabolitesTable, p.x, p.y);
 				}
 			}
 		}
 
 		public void mousePressed(MouseEvent e) {
 			maybeShowPopup(e);
 		}
 
 		public void mouseReleased(MouseEvent e) {
 			maybeShowPopup(e);
 		}
 	}
 
 	private JPopupMenu createMetabolitesHeaderContextMenu(final int columnIndex) {
 		JPopupMenu metabolitesHeaderContextMenu = new JPopupMenu();
 
 		JMenuItem deleteColumnMenu = new JMenuItem("Delete Column");
 		//core columns cannot be deleted - abbreviation and boundary
 		if (columnIndex > GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES.length - 1) {
 			deleteColumnMenu.setEnabled(true);
 		} else {
 			deleteColumnMenu.setEnabled(false);
 		}
 		deleteColumnMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				// copy old model for undo/redo
 				DefaultTableModel oldMetabolitesModel = copyMetabolitesTableModel((DefaultTableModel) metabolitesTable.getModel());			
 				copyMetabolitesTableModels(oldMetabolitesModel);
 				MetaboliteUndoItem undoItem = createMetaboliteUndoItem("", "", metabolitesTable.getSelectedRow(), metabolitesTable.getSelectedColumn(), 1, UndoConstants.DELETE_COLUMN, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);
 				setUndoOldCollections(undoItem);
 				undoItem.setTableCopyIndex(LocalConfig.getInstance().getNumMetabolitesTableCopied());								
 				undoItem.setDeletedColumnIndex(columnIndex);
 				ArrayList<String> oldMetaCol = new ArrayList<String>();
 				ArrayList<String> newMetaCol = new ArrayList<String>();
 				for (int i = 0; i < LocalConfig.getInstance().getMetabolitesMetaColumnNames().size(); i++) {
 					oldMetaCol.add(LocalConfig.getInstance().getMetabolitesMetaColumnNames().get(i));
 				}
 				undoItem.setOldMetaColumnNames(oldMetaCol);
 				DefaultTableModel model = deletedColumnMetabolitesTableModel(columnIndex);
 				metabolitesTable.setModel(model);
 				setUpMetabolitesTable(model);
 				copyMetabolitesTableModels(model);
 				for (int i = 0; i < LocalConfig.getInstance().getMetabolitesMetaColumnNames().size(); i++) {
 					newMetaCol.add(LocalConfig.getInstance().getMetabolitesMetaColumnNames().get(i));
 				}
 				undoItem.setNewMetaColumnNames(newMetaCol);
 				setUndoNewCollections(undoItem);				
 				setUpMetabolitesUndo(undoItem);
 			}
 		});
 		metabolitesHeaderContextMenu.add(deleteColumnMenu);	
 
 		return metabolitesHeaderContextMenu;
 
 	}
 
 	/************************************************************************************/
 	//end header context menus
 	/************************************************************************************/	
 
 	/*******************************************************************************/
 	//Reactions Table context menus
 	/*******************************************************************************/	
 
 	// from http://docs.oracle.com/javase/tutorial/uiswing/components/menu.html
 	public class ReactionsPopupListener extends MouseAdapter {
 
 		public void maybeShowPopup(MouseEvent e) {
 			if (e.isPopupTrigger() && reactionsTable.isEnabled()) {
 				Point p = new Point(e.getX(), e.getY());
 				int col = reactionsTable.columnAtPoint(p);
 				int row = reactionsTable.rowAtPoint(p);
 				setCurrentReactionsRow(row);
 
 				// translate table index to model index
 				//int mcol = reactionsTable.getColumn(reactionsTable.getColumnName(col)).getModelIndex();
 
 				if (row >= 0 && row < reactionsTable.getRowCount()) {
 					cancelCellEditing();            
 					// create reaction equation column popup menu
 					if (col == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 						JPopupMenu reactionsContextMenu = createReactionEquationContextMenu(row, col);
 						if (reactionsContextMenu != null
 								&& reactionsContextMenu.getComponentCount() > 0) {
 							reactionsContextMenu.show(reactionsTable, p.x, p.y);
 						}
 					} else {
 						// create popup for remaining columns
 						JPopupMenu contextMenu = createReactionsContextMenu(row, col);
 						// ... and show it
 						if (contextMenu != null
 								&& contextMenu.getComponentCount() > 0) {
 							contextMenu.show(reactionsTable, p.x, p.y);
 						}
 					}
 				}
 			}
 		}
 
 		public void mousePressed(MouseEvent e) {
 			maybeShowPopup(e);
 		}
 
 		public void mouseReleased(MouseEvent e) {
 			maybeShowPopup(e);
 		}
 	}
 
 	private void cancelCellEditing() {
 		CellEditor ce = reactionsTable.getCellEditor();
 		if (ce != null) {
 			ce.cancelCellEditing();
 		}
 	}
 
 	/*******************************************************************************/
 	//begin reaction equation context menu
 	/*******************************************************************************/	
 
 	private JPopupMenu createReactionEquationContextMenu(final int rowIndex,
 			final int columnIndex) {
 		JPopupMenu contextMenu = createReactionsContextMenu(rowIndex, columnIndex);
 		contextMenu.addSeparator();
 		if (!reactionsTableEditable || LocalConfig.getInstance().reactionEditorVisible) {
 			editorMenu.setEnabled(false);
 		}
 		editorMenu.addActionListener(new ActionListener() {
 			@SuppressWarnings("deprecation")
 			public void actionPerformed(ActionEvent e) {
 				if (!LocalConfig.getInstance().reactionEditorVisible) {
 					setCurrentReactionsRow(rowIndex);
 					ReactionEditor reactionEditor = new ReactionEditor();
 					setReactionEditor(reactionEditor);
 					reactionEditor.setIconImages(icons);
 					reactionEditor.setSize(800, 520);
 					reactionEditor.setResizable(false);
 					reactionEditor.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 					//reactionEditor.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 					reactionEditor.setLocationRelativeTo(null);
 					reactionEditor.setVisible(true);
 					reactionEditor.okButton.addActionListener(okButtonActionListener);
 					reactionEditor.cancelButton.addActionListener(cancelButtonActionListener);
 					reactionEditor.addWindowListener(new WindowAdapter() {
 						public void windowClosing(WindowEvent evt) {
 							getReactionEditor().setVisible(false);
 							getReactionEditor().dispose();
 							reactionEditorCloseAction();
 						}
 					});
 					LocalConfig.getInstance().reactionEditorVisible = true;
 				}				
 			}
 		});
 		contextMenu.add(editorMenu);
 
 		return contextMenu;
 	}
 
 	//listens for ok button event in ReactionEditor
 	ActionListener okButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			boolean okToClose = true;
 			int viewRow = reactionsTable.convertRowIndexToModel(reactionsTable.getSelectedRow());
 			int id = (Integer.valueOf((String) reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTIONS_ID_COLUMN)));
 			reactionEditor.setReactionEquation(reactionEditor.reactionArea.getText());
 			//ReactionUndoItem undoItem = createReactionUndoItem(reactionEditor.getOldReaction(), reactionEditor.getReactionEquation(), reactionsTable.getSelectedRow(), GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN, id, UndoConstants.TYPING, UndoConstants.REACTION_UNDO_ITEM_TYPE);
 			if (reactionEditor.getReactionEquation().contains("<") || (reactionEditor.getReactionEquation().contains("=") && !reactionEditor.getReactionEquation().contains(">"))) {
 				reactionsTable.getModel().setValueAt(reactionEditor.getReactionEquation(), viewRow, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN);
 				reactionsTable.getModel().setValueAt("true", viewRow, GraphicalInterfaceConstants.REVERSIBLE_COLUMN);
 			} else if (reactionEditor.getReactionEquation().contains("-->") || reactionEditor.getReactionEquation().contains("->") || reactionEditor.getReactionEquation().contains("=>")) {
 				if (Double.valueOf((String) reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.LOWER_BOUND_COLUMN)) < 0)  {
 					JOptionPane.showMessageDialog(null,                
 							GraphicalInterfaceConstants.IRREVERSIBLE_REACTION_ERROR_MESSAGE,                
 							GraphicalInterfaceConstants.IRREVERSIBLE_REACTION_ERROR_TITLE,                               
 							JOptionPane.ERROR_MESSAGE);
 					okToClose = false;
 				} else {
 					reactionsTable.getModel().setValueAt(reactionEditor.getReactionEquation(), viewRow, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN);
 					reactionsTable.getModel().setValueAt("false", viewRow, GraphicalInterfaceConstants.REVERSIBLE_COLUMN);		    		
 				}				
 			}
 			/*
 		    updateReactionsDatabaseRow(viewRow, Integer.parseInt((String) (reactionsTable.getModel().getValueAt(viewRow, 0))), "SBML", LocalConfig.getInstance().getLoadedDatabase());
 
 			ReactionsUpdater updater = new ReactionsUpdater();
 			updater.updateReactionEquations(id, reactionEditor.getOldReaction(), reactionEditor.getReactionEquation(), LocalConfig.getInstance().getLoadedDatabase());			
 			if (LocalConfig.getInstance().noButtonClicked) {
 				reactionsTable.getModel().setValueAt(updater.reactionEqunAbbr, viewRow, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN);
 				updateReactionsDatabaseRow(viewRow, Integer.parseInt((String) (reactionsTable.getModel().getValueAt(viewRow, 0))), "SBML", LocalConfig.getInstance().getLoadedDatabase());				
 			}
 			 */
 
 			// TODO: ReactionEditor should store arrow string so that reaction created by editor has same arrow as previous
 			// so undo item not created when reaction not changed but arrow changed and for uniformity
 			/*
 		    if (reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) != null && reactionEditor.getOldReaction() != null) {
 				if (!reactionEditor.getOldReaction().equals((String) reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN))) {
 					if (reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) != null) {
 						undoItem.setNewValue((String) reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN));
 					} 
 					if (reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN) != null) {
 						undoItem.setEquationNames((String) reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN));
 					} else {
 						undoItem.setEquationNames("");
 					}
 					setUpReactionsUndo(undoItem);
 				}
 			} 
 			 */
 			/*
 			if (highlightParticipatingRxns) {
 				scrollFirstParticipatingRxnToView();
 			}
 			 */
 			if (LocalConfig.getInstance().getSuspiciousMetabolites().size() > 0) {
 				setLoadErrorMessage("Model contains suspicious metabolites.");
 				statusBar.setText("Row 1" + "                   " + getLoadErrorMessage());
 			}
 			// update reaction equation - metabolite names
 			//reactionsTable.getModel().setValueAt(updater.reactionEqunNames, viewRow, GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN);
 			if (okToClose) {
 				reactionEditor.setVisible(false);
 				reactionEditor.dispose();
 			}
 			okToClose = true;
 			LocalConfig.getInstance().reactionEditorVisible = false;
 			editorMenu.setEnabled(true);
 		}
 	}; 
 
 	ActionListener cancelButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			reactionEditorCloseAction();
 		}
 	}; 
 
 	public void reactionEditorCloseAction() {
 		LocalConfig.getInstance().reactionEditorVisible = false;
 		editorMenu.setEnabled(true);
 	}
 
 	/*******************************************************************************/
 	//end reaction equation context menu
 	/*******************************************************************************/	
 
 	private JPopupMenu createReactionsContextMenu(final int rowIndex,
 			final int columnIndex) {
 		JPopupMenu contextMenu = new JPopupMenu();
 
 		JMenu selectAllMenu = new JMenu("Select All");
 
 		final JRadioButtonMenuItem inclColNamesItem = new JRadioButtonMenuItem(
 				"Include Column Names");
 		final JRadioButtonMenuItem selectCellsOnly = new JRadioButtonMenuItem(
 				"Selected Cells Only");
 
 		ButtonGroup bg = new ButtonGroup();
 		bg.add(inclColNamesItem);
 		bg.add(selectCellsOnly);
 		inclColNamesItem.setSelected(includeRxnColumnNames);
 		selectCellsOnly.setSelected(!includeRxnColumnNames);
 
 		inclColNamesItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (inclColNamesItem.isSelected()) {
 					includeRxnColumnNames = true;
 				} else {
 					includeRxnColumnNames = false;
 				}				
 				reactionsTable.selectAll();			
 				selectReactionsRows();
 			}
 		});
 		selectCellsOnly.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (selectCellsOnly.isSelected()) {
 					includeRxnColumnNames = false;
 				} else {
 					includeRxnColumnNames = true;
 				}				
 				reactionsTable.selectAll();			
 				selectReactionsRows();
 			}
 		});
 
 		selectAllMenu.add(inclColNamesItem);
 		selectAllMenu.add(selectCellsOnly);
 
 		contextMenu.add(selectAllMenu);
 
 		contextMenu.addSeparator();
 
 		JMenuItem copyMenu = new JMenuItem("Copy");
 		copyMenu.setAccelerator(KeyStroke.getKeyStroke(
 				KeyEvent.VK_C, ActionEvent.CTRL_MASK));
 		copyMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				reactionsCopy();
 			}
 		});
 		contextMenu.add(copyMenu);
 
 		JMenuItem pasteMenu = new JMenuItem("Paste");
 		pasteMenu.setAccelerator(KeyStroke.getKeyStroke(
 				KeyEvent.VK_V, ActionEvent.CTRL_MASK));
 		if (isClipboardContainingText(this)
 				&& reactionsTable.getModel().isCellEditable(rowIndex, columnIndex)
 				&& isRoot) {
 			pasteMenu.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					reactionsPaste();										
 				}
 			});
 		} else {
 			pasteMenu.setEnabled(false);
 		}
 		contextMenu.add(pasteMenu);
 
 		JMenuItem clearMenu = new JMenuItem("Clear Contents");
 		if (isRoot) {
 			clearMenu.setEnabled(true);
 		} else {
 			clearMenu.setEnabled(false);
 		}
 		clearMenu.setAccelerator(KeyStroke.getKeyStroke(
 				KeyEvent.VK_E, ActionEvent.CTRL_MASK));
 		clearMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				reactionsClear();
 			}
 		});
 		contextMenu.add(clearMenu);
 
 		contextMenu.addSeparator();
 
 		if (isRoot) {
 			deleteReactionRowMenuItem.setEnabled(true);
 		} else {
 			deleteReactionRowMenuItem.setEnabled(false);
 		}
 		deleteReactionRowMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				if (reactionsTable.getModel().getRowCount() > 0 && reactionsTable.getSelectedRow() != -1 )
 				{
 					// copy old model for undo/redo
 					DefaultTableModel oldReactionsModel = copyReactionsTableModel((DefaultTableModel) reactionsTable.getModel());			
 					copyReactionsTableModels(oldReactionsModel);
 					DefaultTableModel model = (DefaultTableModel) reactionsTable.getModel();
 					int startRow=reactionsTable.getSelectedRows()[0]; 
 					int rowIndexEnd = reactionsTable.getSelectionModel().getMaxSelectionIndex();
 					ArrayList<Integer> deleteIds = new ArrayList<Integer>();
 					for (int r = rowIndexEnd; r >= startRow; r--) {
 						int viewRow = reactionsTable.convertRowIndexToModel(r);
 						int id = (Integer.valueOf((String) reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTIONS_ID_COLUMN)));
 						deleteIds.add(id);	
 						model.removeRow(viewRow);
 						LocalConfig.getInstance().getReactionEquationMap().remove(viewRow);
 					}
 					//System.out.println(deleteIds);
 					ReactionUndoItem undoItem = createReactionUndoItem("", "", startRow, reactionsTable.getSelectedColumn(), deleteIds.get(0), UndoConstants.DELETE_ROW, UndoConstants.REACTION_UNDO_ITEM_TYPE);
 					undoItem.setTableCopyIndex(LocalConfig.getInstance().getNumReactionTablesCopied());
 					copyReactionsTableModels(model); 
 					setUpReactionsUndo(undoItem);
 				}
 				formulaBar.setText("");
 				//ReactionFactory aFactory = new ReactionFactory("SBML");
 				//aFactory.getAllReactions();
 				//System.out.println("eq map " + LocalConfig.getInstance().getReactionEquationMap());
 			}
 		});
 		contextMenu.add(deleteReactionRowMenuItem);	
 
 		contextMenu.addSeparator();
 
 		if (highlightParticipatingRxns) {
 			unhighlightMenu.setEnabled(true);
 		} else {
 			unhighlightMenu.setEnabled(false);
 		}
 		unhighlightMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				highlightParticipatingRxns = false;
 				reactionsTable.repaint();
 			}
 		});
 
 		contextMenu.add(unhighlightMenu);	
 
 		return contextMenu;
 	}
 
 	/*******************************************************************************/
 	//end Reactions Table context menus
 	/*******************************************************************************/
 
 	/**************************************************************************/
 	//reactionsTable context menu methods
 	/**************************************************************************/
 
 	public void selectReactionsRows() {
 		setClipboardContents("");
 
 		StringBuffer sbf=new StringBuffer();
 		int numrows = reactionsTable.getSelectedRowCount(); 
 		int[] rowsselected=reactionsTable.getSelectedRows();  
 		reactionsTable.changeSelection(rowsselected[0], 1, false, false);
 		reactionsTable.changeSelection(rowsselected[numrows - 1], reactionsTable.getColumnCount(), false, true);
 		reactionsTable.scrollColumnToVisible(1);		
 		if (includeRxnColumnNames == true) {
 			//add column names to clipboard
 			for (int c = 1; c < GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES.length; c++) {
 				sbf.append(GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES[c]);
 				if (c < GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES.length - 1) {
 					sbf.append("\t"); 
 				}				
 			}
 			for (int r = 0; r < LocalConfig.getInstance().getReactionsMetaColumnNames().size(); r++) {
 				sbf.append("\t");
 				sbf.append(LocalConfig.getInstance().getReactionsMetaColumnNames().get(r));					
 			}
 			sbf.append("\n");
 		}
 
 		for (int i = 0; i < numrows; i++) {
 			//starts at 1 to avoid reading hidden db id column
 			for (int j = 1; j < reactionsTable.getColumnCount(); j++) 
 			{ 
 				if (reactionsTable.getValueAt(rowsselected[i], j) != null) {
 					sbf.append(reactionsTable.getValueAt(rowsselected[i], j));
 				} else {
 					sbf.append(" ");
 				}
 				if (j < reactionsTable.getColumnCount()-1) sbf.append("\t");				 
 			} 
 			sbf.append("\n"); 
 		}  
 		setClipboardContents(sbf.toString());
 		//System.out.println(sbf.toString());
 	}
 
 	public void reactionsCopy() {
 		int numCols=reactionsTable.getSelectedColumnCount(); 
 		int numRows=reactionsTable.getSelectedRowCount(); 
 		int[] rowsSelected=reactionsTable.getSelectedRows(); 
 		int[] colsSelected=reactionsTable.getSelectedColumns(); 
 		if (numRows!=rowsSelected[rowsSelected.length-1]-rowsSelected[0]+1 || numRows!=rowsSelected.length || 
 				numCols!=colsSelected[colsSelected.length-1]-colsSelected[0]+1 || numCols!=colsSelected.length) {
 
 			JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
 			return; 
 		} 
 
 		StringBuffer excelStr=new StringBuffer(); 
 		ArrayList<ModelReactionEquation> copiedReactionList = new ArrayList<ModelReactionEquation>();
 		for (int i=0; i<numRows; i++) { 
 			for (int j=0; j<numCols; j++) { 
 				if (colsSelected[j] == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 					int viewRow = reactionsTable.convertRowIndexToModel(rowsSelected[i]);
 					//System.out.println(reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTIONS_ID_COLUMN));
 					int id = Integer.valueOf((String) reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTIONS_ID_COLUMN));
 					copiedReactionList.add(LocalConfig.getInstance().getReactionEquationMap().get(id));					
 				}
 				try {
 					excelStr.append(escape(reactionsTable.getValueAt(rowsSelected[i], colsSelected[j]))); 
 				} catch (Throwable t) {
 
 				}						
 				if (j<numCols-1) {
 					//System.out.println("t");
 					excelStr.append("\t"); 
 				} 
 			} 
 			//System.out.println("n");
 			excelStr.append("\n"); 
 		}
 		setCopiedReactions(copiedReactionList);
 		//System.out.println(getCopiedReactions());
 
 		StringSelection sel  = new StringSelection(excelStr.toString()); 
 		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
 	}
 
 	public void reactionsPaste() {
 		// columns can not be repeatedly pasted (at least for now)
 		if (reactionsTable.getSelectedColumns().length > 1 && reactionsTable.getSelectedColumns().length != numberOfClipboardColumns()) {
 			JOptionPane.showMessageDialog(null,                
 					GraphicalInterfaceConstants.PASTE_AREA_ERROR,                
 					"Paste Error",                                
 					JOptionPane.ERROR_MESSAGE);
 		} else {
 			int startRow=reactionsTable.getSelectedRows()[0]; 
 			int startCol=reactionsTable.getSelectedColumns()[0];
 			String pasteString = ""; 
 			try { 
 				pasteString = (String)(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this).getTransferData(DataFlavor.stringFlavor)); 
 			} catch (Exception e1) { 
 				JOptionPane.showMessageDialog(null, "Invalid Paste Type", "Invalid Paste Type", JOptionPane.ERROR_MESSAGE);
 				return; 
 			}
 			// copy model so old model can be restored if paste not valid
 			DefaultTableModel oldReactionsModel = copyReactionsTableModel((DefaultTableModel) reactionsTable.getModel());			
 			copyReactionsTableModels(oldReactionsModel);
 			ReactionUndoItem undoItem = createReactionUndoItem("", "", startRow, startCol, 1, UndoConstants.PASTE, UndoConstants.REACTION_UNDO_ITEM_TYPE);
 			undoItem.setTableCopyIndex(LocalConfig.getInstance().getNumReactionTablesCopied());
 			// if selected rows for paste > number of clipboard rows, need to paste
 			// clipboard rows repeatedly
 			if (numberOfClipboardRows() > 0 && reactionsTable.getSelectedRows().length > numberOfClipboardRows()) {
 				int quotient = reactionsTable.getSelectedRows().length/numberOfClipboardRows();
 				int remainder = reactionsTable.getSelectedRows().length%numberOfClipboardRows();
 				for (int q = 0; q < quotient; q++) {
 					String[] lines = pasteString.split("\n");
 					for (int i=0 ; i<numberOfClipboardRows(); i++) { 
 						if (i < lines.length) {
 							String[] cells = lines[i].split("\t");
 							// fixes bug where if last cell in row is blank, will not
 							// paste blank value over cell value if not blank
 							if (q < quotient) {
 								for (int j=0 ; j < numberOfClipboardColumns(); j++) {									
 									if (j < cells.length) {
 										updateReactionsCellIfPasteValid(cells[j], startRow+i, startCol+j);
 										if (startCol+j == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 											if (validPaste) {
 												updateReactionPasteEquationMap(startRow+i, i);
 											}						
 										}
 									} else {
 										updateReactionsCellIfPasteValid("", startRow+i, startCol+j);
 									} 				
 								}
 							}									
 						} else {
 							if (q < quotient) {
 								for (int j=0 ; j < numberOfClipboardColumns(); j++) {
 									updateReactionsCellIfPasteValid("", startRow+i, startCol+j);
 									if (startCol+j == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 										if (validPaste) {
 											updateReactionPasteEquationMap(startRow+i, i);
 										}						
 									}
 								}
 							}									
 						}							 
 					}
 					startRow += numberOfClipboardRows();
 					//System.out.println("start" + startRow);
 				}
 				for (int m = 0; m < remainder; m++) {
 					//System.out.println("rem start" + startRow);
 					String[] lines = pasteString.split("\n");
 					if (m < lines.length) {
 						String[] cells = lines[m].split("\t"); 
 						for (int j=0 ; j < numberOfClipboardColumns(); j++) {
 							if (j < cells.length) {
 								if (reactionsTable.getRowCount()>startRow+m && reactionsTable.getColumnCount()>startCol+j) { 
 									updateReactionsCellIfPasteValid(cells[j], startRow+m, startCol+j); 
 									if (startCol+j == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 										if (validPaste) {
 											updateReactionPasteEquationMap(startRow+m, m);
 										}						
 									}
 								} 
 							} else {
 								if (reactionsTable.getRowCount()>startRow+m && reactionsTable.getColumnCount()>startCol+j) { 
 									updateReactionsCellIfPasteValid("", startRow+m, startCol+j);
 								} 										
 							} 
 						} 
 					} else {
 						for (int j=0 ; j < numberOfClipboardColumns(); j++) { 
 							updateReactionsCellIfPasteValid("", startRow+m, startCol+j);
 							if (startCol+j == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 								if (validPaste) {
 									updateReactionPasteEquationMap(startRow+m, m);
 								}						
 							}
 						}
 					}
 				}
 				// if selected rows for paste <= number of clipboard rows 	
 			} else {
 				String[] lines = pasteString.split("\n");
 				if (startRow + lines.length > reactionsTable.getRowCount()) {
 					System.out.println("Out of range error");
 				} else {
 					for (int i=0 ; i<numberOfClipboardRows(); i++) { 
 						if (i < lines.length) {
 							String[] cells = lines[i].split("\t"); 
 							if (startCol + cells.length > reactionsTable.getColumnCount()) {
 								System.out.println("Out of range error");
 							} else {
 								for (int j=0 ; j < numberOfClipboardColumns(); j++) {
 									if (j < cells.length) {
 										updateReactionsCellIfPasteValid(cells[j], startRow+i, startCol+j);
 										if (startCol+j == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 											if (validPaste) {
 												updateReactionPasteEquationMap(startRow+i, i);
 											}						
 										}
 									} else {
 										updateReactionsCellIfPasteValid("", startRow+i, startCol+j);
 									} 
 								}
 							}
 						} else {
 							for (int j=0 ; j < numberOfClipboardColumns(); j++) { 
 								updateReactionsCellIfPasteValid("", startRow+i, startCol+j);
 								if (startCol+j == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 									if (validPaste) {
 										updateReactionPasteEquationMap(startRow+i, i);
 									}						
 								}
 							}
 						}
 					} 
 				}
 			}
 			// if paste not valid, set old model
 			if (!validPaste) {
 				JOptionPane.showMessageDialog(null,                
 						getPasteError(),                
 						"Paste Error",                                
 						JOptionPane.ERROR_MESSAGE);
 				setUpReactionsTable(oldReactionsModel);
 				LocalConfig.getInstance().getReactionsTableModelMap().put(LocalConfig.getInstance().getModelName(), oldReactionsModel);
 				deleteReactionsPasteUndoItem();
 				//System.out.println(LocalConfig.getInstance().getReactionEquationMap());
 				validPaste = true;
 			} else {
 				DefaultTableModel newReactionsModel = copyReactionsTableModel((DefaultTableModel) reactionsTable.getModel());			
 				copyReactionsTableModels(newReactionsModel); 
 				setUpReactionsUndo(undoItem);
 				// add pasted reactions to equation map
 				ArrayList<Object> keys = new ArrayList<Object>(LocalConfig.getInstance().getReactionPasteEquationMap().keySet());
 				for (int r = 0; r < keys.size(); r++) {
 					LocalConfig.getInstance().getReactionEquationMap().put(keys.get(r), LocalConfig.getInstance().getReactionPasteEquationMap().get(keys.get(r)));
 				}
 				//System.out.println(LocalConfig.getInstance().getReactionEquationMap());
 			}
 		}				
 	}
 
 	// used for invalid paste, invalid clear, and invalid replace all
 	public void deleteReactionsPasteUndoItem() {
 		int numCopied = LocalConfig.getInstance().getNumReactionTablesCopied();	
 		if (LocalConfig.getInstance().getReactionsUndoTableModelMap().containsKey(Integer.toString(numCopied + 1))) {
 			LocalConfig.getInstance().getReactionsUndoTableModelMap().remove(Integer.toString(numCopied + 1));
 		}
 		numCopied -= 1;
 		if (LocalConfig.getInstance().getReactionsUndoTableModelMap().containsKey(Integer.toString(numCopied + 1))) {
 			LocalConfig.getInstance().getReactionsUndoTableModelMap().remove(Integer.toString(numCopied + 1));
 		}
 		LocalConfig.getInstance().setNumReactionTablesCopied(numCopied);
 		//System.out.println(LocalConfig.getInstance().getReactionsUndoTableModelMap());
 	}
 
 	public void updateReactionsCellIfPasteValid(String value, int row, int col) {
 		if (isReactionsEntryValid(row, col, value)) {
 			reactionsTable.setValueAt(value, row, col);	
 			formulaBar.setText("");
 		} else {
 			validPaste = false;
 		}
 	}
 
 	public boolean isReactionsEntryValid(int row, int columnIndex, String value) {
 		System.out.println("c" + columnIndex);
 		EntryValidator validator = new EntryValidator();
 		int viewRow = reactionsTable.convertRowIndexToModel(row);
 		int id = Integer.valueOf((String) reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTIONS_ID_COLUMN));
 		if (columnIndex == GraphicalInterfaceConstants.FLUX_VALUE_COLUMN || 
 				columnIndex == GraphicalInterfaceConstants.LOWER_BOUND_COLUMN ||
 				columnIndex == GraphicalInterfaceConstants.UPPER_BOUND_COLUMN ||
 				columnIndex == GraphicalInterfaceConstants.BIOLOGICAL_OBJECTIVE_COLUMN ||
 				columnIndex == GraphicalInterfaceConstants.SYNTHETIC_OBJECTIVE_COLUMN) {
 			if (validator.isNumber(value)) {
 				if (columnIndex == GraphicalInterfaceConstants.LOWER_BOUND_COLUMN && getSelectionMode() != 2) {
 					Double lowerBound = Double.valueOf(value);
 					Double upperBound = Double.valueOf((String) (reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.UPPER_BOUND_COLUMN)));
 					if (reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REVERSIBLE_COLUMN).toString().compareTo("false") == 0 && lowerBound < 0) {					
 						setPasteError(GraphicalInterfaceConstants.IRREVERSIBLE_REACTION_ERROR_MESSAGE);
 						setReplaceAllError(GraphicalInterfaceConstants.IRREVERSIBLE_REACTION_ERROR_MESSAGE);
 						return false;					
 					} else if (lowerBound > upperBound) {
 						setPasteError(GraphicalInterfaceConstants.LOWER_BOUND_PASTE_ERROR);
 						setReplaceAllError(GraphicalInterfaceConstants.LOWER_BOUND_REPLACE_ALL_ERROR);
 						return false;						
 					}
 				} else if (columnIndex == GraphicalInterfaceConstants.UPPER_BOUND_COLUMN && getSelectionMode() != 2) {				
 					Double lowerBound = Double.valueOf((String) (reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.LOWER_BOUND_COLUMN)));
 					Double upperBound = Double.valueOf(value);
 					if (upperBound < lowerBound) {
 						setPasteError(GraphicalInterfaceConstants.UPPER_BOUND_PASTE_ERROR);
 						setReplaceAllError(GraphicalInterfaceConstants.UPPER_BOUND_REPLACE_ALL_ERROR);
 						return false;						
 					}
 				}
 			} else {
 				setPasteError("Number format exception");
 				setReplaceAllError("Number format exception");
 				validPaste = false;
 			}
 		} else if (columnIndex == GraphicalInterfaceConstants.KO_COLUMN) {
 			if (value.compareTo("true") == 0 || value.compareTo("false") == 0) {
 				return true;
 			} else {
 				setPasteError(GraphicalInterfaceConstants.INVALID_PASTE_BOOLEAN_VALUE);
 				setReplaceAllError(GraphicalInterfaceConstants.INVALID_REPLACE_ALL_BOOLEAN_VALUE);
 				validPaste = false;
 				return false;
 			}
 		} else if (columnIndex == GraphicalInterfaceConstants.REVERSIBLE_COLUMN) {
 			// TODO: get equation object after setting value and update to this value
 			/*
 			System.out.println(id);			
 			if (((SBMLReactionEquation) LocalConfig.getInstance().getReactionEquationMap().get(id)).getReversible() != null) {
 				String reversible = ((SBMLReactionEquation) LocalConfig.getInstance().getReactionEquationMap().get(id)).getReversible();
 				if (value.equals(reversible)) {
 					return true;
 				} else {
 					setPasteError(GraphicalInterfaceConstants.INVALID_PASTE_BOOLEAN_VALUE);
 					setReplaceAllError(GraphicalInterfaceConstants.INVALID_REPLACE_ALL_BOOLEAN_VALUE);
 					validPaste = false;
 					return false;
 				}
 			}
 			 */
 			if (value.compareTo("true") == 0 || value.compareTo("false") == 0) {
 				return true;
 			} else {
 				setPasteError(GraphicalInterfaceConstants.INVALID_PASTE_BOOLEAN_VALUE);
 				setReplaceAllError(GraphicalInterfaceConstants.INVALID_REPLACE_ALL_BOOLEAN_VALUE);
 				validPaste = false;
 				return false;
 			}
 		} else if (columnIndex == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 			System.out.println("r");
 			System.out.println(LocalConfig.getInstance().getInvalidReactions());
 			if (value != null && value.trim().length() > 0) {
 				ReactionParser parser = new ReactionParser();
 				if (!parser.isValid(value)) {
 					System.out.println(parser.isValid(value));
 					setPasteError("Invalid Reaction Format");
 					setReplaceAllError("Invalid Reaction Format");
 					validPaste = false;
 					return false;
 				} else {
 					return true;
 				}				
 			} else {
 				setPasteError("Invalid Reaction Format");
 				setReplaceAllError("Invalid Reaction Format");
 				validPaste = false;
 				return false;
 			}
 		}
 		return true;
 
 	}
 
 	public void updateReactionPasteEquationMap(int row, int index) {
 		int viewRow = reactionsTable.convertRowIndexToModel(row);
 		int id = Integer.valueOf((String) reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTIONS_ID_COLUMN));
 		//System.out.println(id);
 		Map<Object, ModelReactionEquation> reactionPasteEquationMap = LocalConfig.getInstance().getReactionPasteEquationMap();
 		for (int i = 0; i < ((SBMLReactionEquation) getCopiedReactions().get(index)).getReactants().size(); i++) {
 			((SBMLReactionEquation) getCopiedReactions().get(index)).getReactants().get(i).setReactionId(id);
 		}
 		for (int j = 0; j < ((SBMLReactionEquation) getCopiedReactions().get(index)).getProducts().size(); j++) {
 			((SBMLReactionEquation) getCopiedReactions().get(index)).getProducts().get(j).setReactionId(id);
 		}
 		reactionPasteEquationMap.put(id, getCopiedReactions().get(index));
 		LocalConfig.getInstance().setReactionPasteEquationMap(reactionPasteEquationMap);	
 	}
 
 	public void reactionsClear() {
 		int startRow=(reactionsTable.getSelectedRows())[0]; 
 		int startCol=(reactionsTable.getSelectedColumns())[0];
 		// copy model for undo 
 		DefaultTableModel oldReactionsModel = copyReactionsTableModel((DefaultTableModel) reactionsTable.getModel());	
 		copyReactionsTableModels(oldReactionsModel);
 		ReactionUndoItem undoItem = createReactionUndoItem("", "", startRow, startCol, 1, UndoConstants.CLEAR_CONTENTS, UndoConstants.REACTION_UNDO_ITEM_TYPE);
 		undoItem.setTableCopyIndex(LocalConfig.getInstance().getNumReactionTablesCopied());		
 		boolean valid = true;
 		// check if columns that require values will be cleared
 		for(int j=0; j < reactionsTable.getSelectedColumns().length ;j++) { 
 			if (startCol + j == GraphicalInterfaceConstants.KO_COLUMN || startCol + j == GraphicalInterfaceConstants.FLUX_VALUE_COLUMN || 
 					startCol + j == GraphicalInterfaceConstants.LOWER_BOUND_COLUMN || startCol + j == GraphicalInterfaceConstants.UPPER_BOUND_COLUMN
 					|| startCol + j == GraphicalInterfaceConstants.BIOLOGICAL_OBJECTIVE_COLUMN || startCol + j == GraphicalInterfaceConstants.REVERSIBLE_COLUMN ||
 					startCol + j == GraphicalInterfaceConstants.SYNTHETIC_OBJECTIVE_COLUMN || startCol + j == GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN) {
 				valid = false;
 			}
 		}
 		if (valid) {
 			for(int i=0; i < reactionsTable.getSelectedRows().length ;i++) { 
 				for(int j=0; j < reactionsTable.getSelectedColumns().length ;j++) {
 					int viewRow = reactionsTable.convertRowIndexToModel(startRow + i);
 					// TODO: use to remove reaction equations from map
 					int id = (Integer.valueOf((String) reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTIONS_ID_COLUMN)));
 					//System.out.println(id);
 					reactionsTable.setValueAt(" ", startRow + i, startCol + j);	
 					if (startCol + j == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 						LocalConfig.getInstance().getReactionEquationMap().remove(id);
 						reactionsTable.setValueAt(" ", startRow + i, GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN);	
 						//System.out.println(LocalConfig.getInstance().getReactionEquationMap());
 					}							
 				} 
 			}
 			DefaultTableModel newReactionsModel = copyReactionsTableModel((DefaultTableModel) reactionsTable.getModel());			
 			copyReactionsTableModels(newReactionsModel);  
 			setUpReactionsUndo(undoItem);
 		} else {
 			JOptionPane.showMessageDialog(null,                
 					GraphicalInterfaceConstants.CLEAR_ERROR_MESSAGE,                
 					"Clear Error",                                
 					JOptionPane.ERROR_MESSAGE);
 			deleteReactionsPasteUndoItem();
 		}
 	}
 	
 	/**************************************************************************/
 	//end reactionsTable context menu methods
 	/**************************************************************************/
 
 	/****************************************************************************/
 	// Metabolites Table context menus
 	/****************************************************************************/
 
 	// from http://docs.oracle.com/javase/tutorial/uiswing/components/menu.html
 	public class MetabolitesPopupListener extends MouseAdapter {
 
 		public void maybeShowPopup(MouseEvent e) {
 			if (e.isPopupTrigger() && metabolitesTable.isEnabled()) {
 				Point p = new Point(e.getX(), e.getY());
 				int col = metabolitesTable.columnAtPoint(p);
 				int row = metabolitesTable.rowAtPoint(p);
 
 				// translate table index to model index
 				//int mcol = metabolitesTable.getColumn(metabolitesTable.getColumnName(col)).getModelIndex();
 
 				if (row >= 0 && row < metabolitesTable.getRowCount()) {
 					cancelCellEditing();            
 					// create popup menu for abbreviation column
 					if (col == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN ||
 							col == GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN) {
 						JPopupMenu abbrevContextMenu = createMetaboliteAbbreviationContextMenu(row, col);
 						// ... and show it
 						if (abbrevContextMenu != null
 								&& abbrevContextMenu.getComponentCount() > 0) {
 							abbrevContextMenu.show(metabolitesTable, p.x, p.y);
 						}
 						// create popup menu for remaining columns	
 					} else {
 						JPopupMenu contextMenu = createMetabolitesContextMenu(row, col);
 						// ... and show it
 						if (contextMenu != null
 								&& contextMenu.getComponentCount() > 0) {
 							contextMenu.show(metabolitesTable, p.x, p.y);
 						}	            	
 					}
 				}
 			}
 		}
 
 		public void mousePressed(MouseEvent e) {
 			maybeShowPopup(e);
 		}
 
 		public void mouseReleased(MouseEvent e) {
 			maybeShowPopup(e);
 		}
 	}
 
 	/****************************************************************************/
 	// begin abbreviation column context menu
 	/****************************************************************************/
 
 	public void showRenameMessage(String toBeRenamed) {
 		Object[] options = {"    Yes    ", "    No    ",};
 		int choice = JOptionPane.showOptionDialog(null, 
 				GraphicalInterfaceConstants.PARTICIPATING_METAB_RENAME_MESSAGE_PREFIX + 
 				toBeRenamed + GraphicalInterfaceConstants.PARTICIPATING_METAB_RENAME_MESSAGE_SUFFIX, 
 				GraphicalInterfaceConstants.PARTICIPATING_METAB_RENAME_TITLE, 
 				JOptionPane.YES_NO_OPTION, 
 				JOptionPane.QUESTION_MESSAGE, 
 				null, options, options[0]);
 		if (choice == JOptionPane.YES_OPTION) {
 			renameMetabolite = true;
 			if (showMetaboliteRenameInterface) {
 				showMetaboliteRenameInterface();
 			}			
 		}
 		if (choice == JOptionPane.NO_OPTION) {
 
 		}
 	}
 
 	public void showMetaboliteRenameInterface() {
 		MetaboliteRenameInterface metaboliteRenameInterface = new MetaboliteRenameInterface();
 		setMetaboliteRenameInterface(metaboliteRenameInterface);
 		metaboliteRenameInterface.setTitle(GraphicalInterfaceConstants.RENAME_METABOLITE_INTERFACE_TITLE);
 		metaboliteRenameInterface.setIconImages(icons);
 		metaboliteRenameInterface.setSize(350, 160);
 		metaboliteRenameInterface.setResizable(false);
 		metaboliteRenameInterface.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		metaboliteRenameInterface.setLocationRelativeTo(null);
 		metaboliteRenameInterface.setVisible(true);
 	}
 
 	public void updateMetaboliteMaps(int id, String metabAbbrev, String metabName, String newName, int columnIndex) {
 		if (columnIndex == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) {
 			System.out.println("bef" + LocalConfig.getInstance().getMetaboliteNameIdMap());
 			System.out.println("bef" + LocalConfig.getInstance().getMetaboliteUsedMap());
 			Object idValue = LocalConfig.getInstance().getMetaboliteNameIdMap().get(metabAbbrev);
 			LocalConfig.getInstance().getMetaboliteNameIdMap().remove(metabAbbrev);	
 			LocalConfig.getInstance().getMetaboliteNameIdMap().put(newName, idValue);
 
 			if (LocalConfig.getInstance().getMetaboliteUsedMap().containsKey(metabAbbrev)) {
 				Object value = LocalConfig.getInstance().getMetaboliteUsedMap().get(metabAbbrev);
 				LocalConfig.getInstance().getMetaboliteUsedMap().remove(metabAbbrev);
 				LocalConfig.getInstance().getMetaboliteUsedMap().put(newName, value);
 			}			
 			System.out.println("aft" + LocalConfig.getInstance().getMetaboliteNameIdMap());
 			System.out.println("aft" + LocalConfig.getInstance().getMetaboliteUsedMap());
 		}
 	}
 
 	public void rewriteReactions(int id, String metabAbbrev, String metabName, String newName, int columnIndex) {
 		MetaboliteFactory aFactory = new MetaboliteFactory("SBML");
 		ArrayList<Integer> participatingReactions = aFactory.participatingReactions(metabAbbrev);
 		//System.out.println("p" + participatingReactions);
 		//undoItem.setReactionIdList(participatingReactions);				
 		// rewrite reactions 
 		for (int i = 0; i < participatingReactions.size(); i++) {
 			SBMLReactionEquation equn = (SBMLReactionEquation) LocalConfig.getInstance().getReactionEquationMap().get(participatingReactions.get(i));
 			if (columnIndex == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) {
 				for (int j = 0; j < equn.reactants.size(); j++) {
 					if (equn.reactants.get(j).getMetaboliteAbbreviation().equals(metabAbbrev)) {
 						equn.reactants.get(j).setMetaboliteAbbreviation(newName);
 					}
 				}
 				for (int j = 0; j < equn.products.size(); j++) {
 					if (equn.products.get(j).getMetaboliteAbbreviation().equals(metabAbbrev)) {
 						equn.products.get(j).setMetaboliteAbbreviation(newName);
 					}
 				}
 				equn.writeReactionEquation();
 				updateReactionsCellById(equn.equationAbbreviations, participatingReactions.get(i), GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN);
 			} else if (columnIndex == GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN) {
 				for (int j = 0; j < equn.reactants.size(); j++) {
 					if (equn.reactants.get(j).getMetaboliteName().equals(metabName)) {
 						equn.reactants.get(j).setMetaboliteName(newName);
 					}
 				}
 				for (int j = 0; j < equn.products.size(); j++) {
 					if (equn.products.get(j).getMetaboliteName().equals(metabName)) {
 						equn.products.get(j).setMetaboliteName(newName);
 					}
 				}
 				equn.writeReactionEquation();
 				updateReactionsCellById(equn.equationNames, participatingReactions.get(i), GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN);
 			}								
 		}
 	}
 
 	private JPopupMenu createMetaboliteAbbreviationContextMenu(final int rowIndex,
 			final int columnIndex) {
 		JPopupMenu contextMenu = createMetabolitesContextMenu(rowIndex, columnIndex);
 		contextMenu.addSeparator();
 
 		final int viewRow = metabolitesTable.convertRowIndexToModel(rowIndex);
 		final int id = Integer.valueOf((String) metabolitesTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.METABOLITE_ID_COLUMN));		
 		final String metabAbbrev = (String) metabolitesTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN);
 		System.out.println(metabAbbrev);
 		System.out.println(LocalConfig.getInstance().getMetaboliteUsedMap());		
 		final String metabName = (String) metabolitesTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN);
 
 		JMenuItem renameMenu = new JMenuItem("Rename");
 		if (isRoot) {
 			renameMenu.setEnabled(true);
 		} else {
 			renameMenu.setEnabled(false);
 		}
 		if (columnIndex == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) {
 			if (metabAbbrev == null || metabAbbrev.length() == 0) {
 				renameMenu.setEnabled(false);
 			}
 		} else if (columnIndex == GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN) {
 			if (metabName == null || metabName.length() == 0) {
 				renameMenu.setEnabled(false);
 			}
 		}
 		contextMenu.add(renameMenu);
 
 		renameMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				duplicatePromptShown = false;
 				if (LocalConfig.getInstance().getMetaboliteUsedMap().containsKey(metabAbbrev)) {
 					String toBeRenamed = "";
 					if (columnIndex == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) {
 						toBeRenamed = metabAbbrev;
 					} else if (columnIndex == GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN) {
 						toBeRenamed = metabName;
 					}
 					showRenameMessage(toBeRenamed);
 					// not necessary to use the interface to rename an unused or duplicate
 					// metabolite but for consistency, when rename item clicked, interface
 					// is displayed and functional. another option would be to disable the
 					// menu item if these conditions are true but that may be confusing
 				} else {
 					showMetaboliteRenameInterface();
 				}
 			}
 		});
 
 		ActionListener metabRenameOKButtonActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent prodActionEvent) {
 				String newName = "";
 				metaboliteRenameInterface.setNewName(metaboliteRenameInterface.textField.getText());
 				if (metaboliteRenameInterface.getNewName() != null && metaboliteRenameInterface.getNewName().length() > 0) {
 					newName = metaboliteRenameInterface.getNewName();
 					// check if duplicate metabolite
 					if (LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey(newName) && 
 							columnIndex == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) {							
 						if (!duplicatePromptShown) {
 							JOptionPane.showMessageDialog(null,                
 									"Duplicate Metabolite.",                
 									"Duplicate Metabolite",                                
 									JOptionPane.ERROR_MESSAGE);
 						}
 						duplicatePromptShown = true;
 					} else {
 						Object idValue = LocalConfig.getInstance().getMetaboliteNameIdMap().get(metabAbbrev);
 						MetaboliteUndoItem undoItem = null;
 						if (columnIndex == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) {
 							undoItem = createMetaboliteUndoItem(metabAbbrev, newName, metabolitesTable.getSelectedRow(), 
 									GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN, (Integer) idValue, UndoConstants.RENAME_METABOLITE, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);
 						} else if (columnIndex == GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN) {
 							undoItem = createMetaboliteUndoItem(metabName, newName, metabolitesTable.getSelectedRow(), 
 									GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN, (Integer) idValue, UndoConstants.RENAME_METABOLITE, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);
 						}
 						// this only prevents a null pointer exception, lists updated in undo item
 						setUndoOldCollections(undoItem);
 						if (renameMetabolite) {
 							rewriteReactions(id, metabAbbrev, metabName, newName, columnIndex);
 							updateMetaboliteMaps(id, metabAbbrev, metabName, newName, columnIndex);
 							if (columnIndex == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) {
 								metabolitesTable.getModel().setValueAt(newName, viewRow, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN);
 							} else if (columnIndex == GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN) {
 								metabolitesTable.getModel().setValueAt(newName, viewRow, GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN);
 							}
 						}
 						metaboliteRenameInterface.textField.setText("");
 						metaboliteRenameInterface.setVisible(false);
 						metaboliteRenameInterface.dispose();
 						if (undoItem != null) {
 							setUndoNewCollections(undoItem);
 							setUpMetabolitesUndo(undoItem);
 						}	
 					}						
 				}
 			}
 		};
 		
 		metaboliteRenameInterface.okButton.addActionListener(metabRenameOKButtonActionListener);
 
 		contextMenu.addSeparator();
 
 		final JMenuItem participatingReactionsMenu = new JMenuItem("Highlight Participating Reactions");
 		if (columnIndex == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) {
 			if (metabAbbrev == null || !LocalConfig.getMetaboliteUsedMap().containsKey(metabAbbrev) || highlightParticipatingRxns) {
 				participatingReactionsMenu.setEnabled(false);
 			}
 		} else if (columnIndex == GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN) {
 			if (metabName == null || !LocalConfig.getMetaboliteUsedMap().containsKey(metabAbbrev) || highlightParticipatingRxns) {
 				participatingReactionsMenu.setEnabled(false);
 			}
 		}		
 		participatingReactionsMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				highlightParticipatingRxns = true;
 				unhighlightParticipatingReactionsMenu.setEnabled(true);
 				MetaboliteFactory aFactory = new MetaboliteFactory("SBML");	
 				setParticipatingMetabolite(metabAbbrev);
 				LocalConfig.getInstance().setParticipatingReactions(aFactory.participatingReactions(metabAbbrev));
 				tabbedPane.setSelectedIndex(0);
 				ArrayList<Integer> participatingReactions = aFactory.participatingReactions(metabAbbrev);
 				// sort to get minimum
 				Collections.sort(participatingReactions);
 				// scroll first participating reaction into view
 				if (participatingReactions.size() > 0) {
 					int firstId = participatingReactions.get(0);
 					for (int r = 0; r < reactionsTable.getRowCount(); r++) {
 						int viewRow = reactionsTable.convertRowIndexToModel(r);
 						Integer cellValue = Integer.valueOf((String) reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTIONS_ID_COLUMN));
 						if (cellValue == firstId) {
 							if (columnIndex == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) {
 								setTableCellFocused(r, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN, reactionsTable);
 							} else if (columnIndex == GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN) {
 								setTableCellFocused(r, GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN, reactionsTable);
 							}				
 						}	
 					}
 				}				
 			}
 		});
 		contextMenu.add(participatingReactionsMenu);
 
 		if (metabAbbrev == null || !LocalConfig.getMetaboliteUsedMap().containsKey(metabAbbrev) || !highlightParticipatingRxns) {
 			unhighlightParticipatingReactionsMenu.setEnabled(false);
 		}
 		unhighlightParticipatingReactionsMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				highlightParticipatingRxns = false;
 			}
 		});
 		contextMenu.add(unhighlightParticipatingReactionsMenu);
 
 		return contextMenu;
 	}
 
 	/****************************************************************************/
 	// end abbreviation column context menu
 	/****************************************************************************/
 
 	/****************************************************************************/
 	// begin context menu for remaining columns
 	/****************************************************************************/
 
 	private JPopupMenu createMetabolitesContextMenu(final int rowIndex,
 			final int columnIndex) {
 		JPopupMenu contextMenu = new JPopupMenu();
 
 		JMenu selectAllMenu = new JMenu("Select All");
 
 		final JRadioButtonMenuItem inclColNamesItem = new JRadioButtonMenuItem(
 				"Include Column Names");
 		final JRadioButtonMenuItem selectCellsOnly = new JRadioButtonMenuItem(
 				"Selected Cells Only");
 
 		ButtonGroup bg = new ButtonGroup();
 		bg.add(inclColNamesItem);
 		bg.add(selectCellsOnly);
 		inclColNamesItem.setSelected(includeMtbColumnNames);
 		selectCellsOnly.setSelected(!includeMtbColumnNames);
 
 		inclColNamesItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (inclColNamesItem.isSelected()) {
 					includeMtbColumnNames = true;
 				} else {
 					includeMtbColumnNames = false;
 				}				
 				metabolitesTable.selectAll();			
 				selectMetabolitesRows();
 			}
 		});
 		selectCellsOnly.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (selectCellsOnly.isSelected()) {
 					includeMtbColumnNames = false;
 				} else {
 					includeMtbColumnNames = true;
 				}				
 				metabolitesTable.selectAll();			
 				selectMetabolitesRows();
 			}
 		});        
 		selectAllMenu.add(inclColNamesItem);
 		selectAllMenu.add(selectCellsOnly);
 
 		contextMenu.add(selectAllMenu);
 
 		contextMenu.addSeparator();
 
 		JMenuItem copyMenu = new JMenuItem("Copy");
 		copyMenu.setAccelerator(KeyStroke.getKeyStroke(
 				KeyEvent.VK_C, ActionEvent.CTRL_MASK));
 		copyMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				metabolitesCopy();
 			}
 		});
 		contextMenu.add(copyMenu);
 
 		JMenuItem pasteMenu = new JMenuItem("Paste");
 		pasteMenu.setAccelerator(KeyStroke.getKeyStroke(
 				KeyEvent.VK_V, ActionEvent.CTRL_MASK));
 		if (isClipboardContainingText(this)
 				&& metabolitesTable.getModel().isCellEditable(rowIndex, columnIndex)
 				&& isRoot) {
 			pasteMenu.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					metabolitesPaste();
 				}
 			});
 		} else {
 			pasteMenu.setEnabled(false);
 		}
 		contextMenu.add(pasteMenu);
 
 		JMenuItem clearMenu = new JMenuItem("Clear Contents");
 		if (isRoot) {
 			clearMenu.setEnabled(true);
 		} else {
 			clearMenu.setEnabled(false);
 		}
 		clearMenu.setAccelerator(KeyStroke.getKeyStroke(
 				KeyEvent.VK_E, ActionEvent.CTRL_MASK));
 		clearMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				metabolitesClear();
 			}
 		});
 		contextMenu.add(clearMenu);
 
 		contextMenu.addSeparator();
 
 		if (isRoot) {
 			deleteMetaboliteRowMenuItem.setEnabled(true);
 		} else {
 			deleteMetaboliteRowMenuItem.setEnabled(false);
 		}
 		if (metabolitesTable.getSelectedRow() > -1) {
 			int viewRow = metabolitesTable.convertRowIndexToModel(metabolitesTable.getSelectedRow());
 			int id = Integer.valueOf((String) metabolitesTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.METABOLITE_ID_COLUMN));			
 			String metabAbbrev = (String) metabolitesTable.getModel().getValueAt(viewRow, 1);
 			if (LocalConfig.getInstance().getMetaboliteUsedMap().containsKey(metabAbbrev)) {
 				deleteMetaboliteRowMenuItem.setEnabled(false);
 			} else {
 				if (isRoot) {
 					deleteMetaboliteRowMenuItem.setEnabled(true);
 					deleteMetaboliteRowMenuItem.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent ae) {
 							boolean participant = false;
 							boolean errorShown = false;
 							if (metabolitesTable.getModel().getRowCount() > 0 && metabolitesTable.getSelectedRow() != -1 )
 							{
 								// copy old model for undo/redo
 								DefaultTableModel oldMetabolitesModel = copyMetabolitesTableModel((DefaultTableModel) metabolitesTable.getModel());	
 								copyMetabolitesTableModels(oldMetabolitesModel); 
 								DefaultTableModel model = (DefaultTableModel) metabolitesTable.getModel();
 								int startRow=metabolitesTable.getSelectedRows()[0]; 
 								int rowIndexEnd = metabolitesTable.getSelectionModel().getMaxSelectionIndex();
 								int firstViewRow = metabolitesTable.convertRowIndexToModel(startRow);
 								int firstId = (Integer.valueOf((String) metabolitesTable.getModel().getValueAt(firstViewRow, GraphicalInterfaceConstants.METABOLITE_ID_COLUMN)));
 								MetaboliteUndoItem undoItem = createMetaboliteUndoItem("", "", startRow, 1, firstId, UndoConstants.DELETE_ROW, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);
 								setUndoOldCollections(undoItem);
 								for (int r = rowIndexEnd; r >= startRow; r--) {
 									int viewRow = metabolitesTable.convertRowIndexToModel(r);
 									int id = (Integer.valueOf((String) metabolitesTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.METABOLITE_ID_COLUMN)));
 									String key = (String) metabolitesTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN);
 									// TODO: need to check if id is used	
 									if (LocalConfig.getInstance().getMetaboliteUsedMap().containsKey(key)) {
 										participant = true;
 									} else {
 										model.removeRow(viewRow);
 										if (LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey(key)) {
 											LocalConfig.getInstance().getMetaboliteNameIdMap().remove(key);
 										}
 										if (LocalConfig.getInstance().getBlankMetabIds().contains(id)) {
 											LocalConfig.getInstance().getBlankMetabIds().remove(LocalConfig.getInstance().getBlankMetabIds().indexOf(id));
 										}
 										//System.out.println("blk" + LocalConfig.getInstance().getBlankMetabIds());
 										//System.out.println("del" + LocalConfig.getInstance().getMetaboliteNameIdMap());
 									}									
 								}
 								undoItem.setTableCopyIndex(LocalConfig.getInstance().getNumMetabolitesTableCopied());
 								copyMetabolitesTableModels(model); 
 								setUndoNewCollections(undoItem);
 								setUpMetabolitesUndo(undoItem);
 							}
 							formulaBar.setText("");
 							if (participant && !errorShown) {
 								JOptionPane.showMessageDialog(null,                
 										GraphicalInterfaceConstants.PARTICIPATING_METAB_ERROR_MESSAGE,
 										GraphicalInterfaceConstants.PARTICIPATING_METAB_ERROR_TITLE,                                
 										JOptionPane.ERROR_MESSAGE);
 								errorShown = true;
 							}							
 						}
 					});
 				} else {
 					deleteMetaboliteRowMenuItem.setEnabled(false);
 				}
 			}
 		}
 
 		contextMenu.add(deleteMetaboliteRowMenuItem);	
 
 		return contextMenu;
 	}
 
 	/****************************************************************************/
 	// end Metabolites Table context menus
 	/****************************************************************************/
 
 	/**************************************************************************/
 	//metabolitesTable context menu methods
 	/**************************************************************************/
 
 	public void selectMetabolitesRows() {
 		setClipboardContents("");
 
 		StringBuffer sbf=new StringBuffer();
 		int numrows = metabolitesTable.getSelectedRowCount(); 
 		int[] rowsselected=metabolitesTable.getSelectedRows();  
 
 		metabolitesTable.changeSelection(rowsselected[0], 1, false, false);
 		metabolitesTable.changeSelection(rowsselected[numrows - 1], metabolitesTable.getColumnCount(), false, true);
 		metabolitesTable.scrollColumnToVisible(1);
 
 		if (includeMtbColumnNames == true) {
 			//add column names to clipboard
 			for (int c = 1; c < GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES.length; c++) {
 				sbf.append(GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES[c]);
 				if (c < GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES.length - 1) {
 					sbf.append("\t"); 
 				}			
 			}
 			for (int r = 0; r < LocalConfig.getInstance().getMetabolitesMetaColumnNames().size(); r++) {
 				sbf.append("\t");
 				sbf.append(LocalConfig.getInstance().getMetabolitesMetaColumnNames().get(r));					
 			}
 			sbf.append("\n");
 		}
 
 		for (int i = 0; i < numrows; i++) {
 			//starts at 1 to avoid reading hidden db id column 
 			for (int j = 1; j < metabolitesTable.getColumnCount(); j++) 
 			{ 
 				if (metabolitesTable.getValueAt(rowsselected[i], j) != null) {
 					sbf.append(metabolitesTable.getValueAt(rowsselected[i], j));
 				} else {
 					sbf.append(" ");
 				}
 				if (j < metabolitesTable.getColumnCount()-1) sbf.append("\t"); 
 			} 
 			sbf.append("\n"); 
 		}  
 		setClipboardContents(sbf.toString());
 		//System.out.println(sbf.toString());
 	}
 
 	public void metabolitesCopy() {
 		int numCols=metabolitesTable.getSelectedColumnCount(); 
 		int numRows=metabolitesTable.getSelectedRowCount(); 
 		int[] rowsSelected=metabolitesTable.getSelectedRows(); 
 		int[] colsSelected=metabolitesTable.getSelectedColumns(); 
 		if (numRows!=rowsSelected[rowsSelected.length-1]-rowsSelected[0]+1 || numRows!=rowsSelected.length || 
 				numCols!=colsSelected[colsSelected.length-1]-colsSelected[0]+1 || numCols!=colsSelected.length) {
 
 			JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
 			return; 
 		} 
 
 		StringBuffer excelStr=new StringBuffer(); 
 		for (int i=0; i<numRows; i++) { 
 			for (int j=0; j<numCols; j++) { 
 				try {
 					excelStr.append(escape(metabolitesTable.getValueAt(rowsSelected[i], colsSelected[j]))); 
 				} catch (Throwable t) {
 
 				}						
 				if (j<numCols-1) {
 					//System.out.println("t");
 					excelStr.append("\t"); 
 				} 
 			} 
 			//System.out.println("n");
 			excelStr.append("\n"); 
 		} 
 
 		StringSelection sel  = new StringSelection(excelStr.toString()); 
 		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
 	}
 
 	public void metabolitesPaste() {
 		// columns can not be repeatedly pasted (at least for now)
 		if (metabolitesTable.getSelectedColumns().length > 1 && metabolitesTable.getSelectedColumns().length != numberOfClipboardColumns()) {
 			JOptionPane.showMessageDialog(null,                
 					GraphicalInterfaceConstants.PASTE_AREA_ERROR,                
 					"Paste Error",                                
 					JOptionPane.ERROR_MESSAGE);
 		} else {
 			int startRow=metabolitesTable.getSelectedRows()[0]; 
 			int startCol=metabolitesTable.getSelectedColumns()[0];
 			String pasteString = ""; 
 			try { 
 				pasteString = (String)(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this).getTransferData(DataFlavor.stringFlavor)); 
 			} catch (Exception e1) { 
 				JOptionPane.showMessageDialog(null, "Invalid Paste Type", "Invalid Paste Type", JOptionPane.ERROR_MESSAGE);
 				return; 
 			} 
 			// copy model so old model can be restored if paste not valid
 			DefaultTableModel oldMetabolitesModel = copyMetabolitesTableModel((DefaultTableModel) metabolitesTable.getModel());	
 			copyMetabolitesTableModels(oldMetabolitesModel); 
 			MetaboliteUndoItem undoItem = createMetaboliteUndoItem("", "", startRow, startCol, 1, UndoConstants.PASTE, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);
 			undoItem.setTableCopyIndex(LocalConfig.getInstance().getNumMetabolitesTableCopied());
 			setUndoOldCollections(undoItem);
 			// if selected rows for paste > number of clipboard rows, need to paste
 			// clipboard rows repeatedly
 			if (numberOfClipboardRows() > 0 && metabolitesTable.getSelectedRows().length > numberOfClipboardRows()) {
 				int quotient = metabolitesTable.getSelectedRows().length/numberOfClipboardRows();
 				int remainder = metabolitesTable.getSelectedRows().length%numberOfClipboardRows();
 				for (int q = 0; q < quotient; q++) {
 					String[] lines = pasteString.split("\n");
 					for (int i=0 ; i<numberOfClipboardRows(); i++) { 
 						if (i < lines.length) {
 							String[] cells = lines[i].split("\t");
 							// fixes bug where if last cell in row is blank, will not
 							// paste blank value over cell value if not blank
 							if (q < quotient) {
 								for (int j=0 ; j < numberOfClipboardColumns(); j++) { 
 									if (j < cells.length) {
 										updateMetabolitesCellIfPasteValid(cells[j], startRow+i, startCol+j);
 									} else {
 										updateMetabolitesCellIfPasteValid("", startRow+i, startCol+j);
 									} 
 								}
 							}									
 						} else {
 							if (q < quotient) {
 								for (int j=0 ; j < numberOfClipboardColumns(); j++) { 
 									updateMetabolitesCellIfPasteValid("", startRow+i, startCol+j);
 								}
 							}									
 						}							 
 					}
 					startRow += numberOfClipboardRows();
 				}
 				for (int m = 0; m < remainder; m++) {
 					String[] lines = pasteString.split("\n");
 					if (m < lines.length) {
 						String[] cells = lines[m].split("\t"); 
 						for (int j=0 ; j < numberOfClipboardColumns(); j++) { 
 							if (j < cells.length) {
 								if (metabolitesTable.getRowCount()>startRow+m && metabolitesTable.getColumnCount()>startCol+j) { 
 									updateMetabolitesCellIfPasteValid(cells[j], startRow+m, startCol+j); 
 								} 
 							} else {
 								if (metabolitesTable.getRowCount()>startRow+m && metabolitesTable.getColumnCount()>startCol+j) { 
 									updateMetabolitesCellIfPasteValid("", startRow+m, startCol+j);
 								} 										
 							} 
 						} 
 					} else {
 						for (int j=0 ; j < numberOfClipboardColumns(); j++) { 
 							updateMetabolitesCellIfPasteValid("", startRow+m, startCol+j);
 						}
 					}
 				}
 				// if selected rows for paste <= number of clipboard rows 	
 			} else {
 				String[] lines = pasteString.split("\n");
 				if (startRow + lines.length > metabolitesTable.getRowCount()) {
 					System.out.println("Out of range error");
 				} else {
 					for (int i=0 ; i<numberOfClipboardRows(); i++) { 
 						if (i < lines.length) {
 							String[] cells = lines[i].split("\t"); 
 							if (startCol + cells.length > metabolitesTable.getColumnCount()) {
 								System.out.println("Out of range error");
 							} else {
 								for (int j=0 ; j < numberOfClipboardColumns(); j++) { 
 									if (j < cells.length) {
 										updateMetabolitesCellIfPasteValid(cells[j], startRow+i, startCol+j);
 									} else {
 										updateMetabolitesCellIfPasteValid("", startRow+i, startCol+j);
 									} 
 								}
 							}
 						} else {
 							for (int j=0 ; j < numberOfClipboardColumns(); j++) { 
 								updateMetabolitesCellIfPasteValid("", startRow+i, startCol+j);
 							}
 						}
 					} 
 				}
 			}
 			// if paste not valid, set old model
 			if (!validPaste) {
 				JOptionPane.showMessageDialog(null,                
 						getPasteError(),                
 						"Paste Error",                                
 						JOptionPane.ERROR_MESSAGE);
 				setUpMetabolitesTable(oldMetabolitesModel);
 				LocalConfig.getInstance().getMetabolitesTableModelMap().put(LocalConfig.getInstance().getModelName(), oldMetabolitesModel);
 				deleteReactionsPasteUndoItem();
 				validPaste = true;
 			} else {
 				DefaultTableModel newMetabolitesModel = copyMetabolitesTableModel((DefaultTableModel) metabolitesTable.getModel());			
 				copyMetabolitesTableModels(newMetabolitesModel); 
 				setUndoNewCollections(undoItem);
 				setUpMetabolitesUndo(undoItem);				
 			}
 		}		
 	}
 	
 	// used for invalid paste, invalid clear, and invalid replace all
 	public void deleteMetabolitesPasteUndoItem() {
 		int numCopied = LocalConfig.getInstance().getNumMetabolitesTableCopied();	
 		if (LocalConfig.getInstance().getMetabolitesUndoTableModelMap().containsKey(Integer.toString(numCopied + 1))) {
 			LocalConfig.getInstance().getMetabolitesUndoTableModelMap().remove(Integer.toString(numCopied + 1));
 		}
 		numCopied -= 1;
 		if (LocalConfig.getInstance().getMetabolitesUndoTableModelMap().containsKey(Integer.toString(numCopied + 1))) {
 			LocalConfig.getInstance().getMetabolitesUndoTableModelMap().remove(Integer.toString(numCopied + 1));
 		}
 		LocalConfig.getInstance().setNumMetabolitesTableCopied(numCopied);
 		//System.out.println(LocalConfig.getInstance().getMetabolitesUndoTableModelMap());
 	}
 
 	public void updateMetabolitesCellIfPasteValid(String value, int row, int col) {
 		int id = Integer.valueOf((String) metabolitesTable.getModel().getValueAt(row, GraphicalInterfaceConstants.METABOLITE_ID_COLUMN));		
 		String metabAbbrev = (String) metabolitesTable.getModel().getValueAt(row, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN);
 		if (col == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) {
 			if (LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey(value)) {
 				if (showDuplicatePrompt) {
 					Object[] options = {"    Yes    ", "    No    ",};
 					int choice = JOptionPane.showOptionDialog(null, 
 							GraphicalInterfaceConstants.DUPLICATE_METABOLITE_PASTE_MESSAGE, 
 							GraphicalInterfaceConstants.DUPLICATE_METABOLITE_TITLE, 
 							JOptionPane.YES_NO_OPTION, 
 							JOptionPane.QUESTION_MESSAGE, 
 							null, options, options[0]);
 					if (choice == JOptionPane.YES_OPTION) {	
 						value = value + duplicateSuffix(value);
 						metabolitesTable.setValueAt(value, row, col);
 						if (LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey(metabAbbrev)) {
 							LocalConfig.getInstance().getMetaboliteNameIdMap().remove(metabAbbrev);
 						}
 						LocalConfig.getInstance().getMetaboliteNameIdMap().put(value, id);
 						System.out.println(LocalConfig.getInstance().getMetaboliteNameIdMap());
 						showDuplicatePrompt = false;
 					}
 					if (choice == JOptionPane.NO_OPTION) {
 						showDuplicatePrompt = false;
 						duplicateMetabOK = false;
 						//validPaste = false;
 					}
 				} else {
 					value = value + duplicateSuffix(value);
 					metabolitesTable.setValueAt(value, row, col);
 					if (LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey(metabAbbrev)) {
 						LocalConfig.getInstance().getMetaboliteNameIdMap().remove(metabAbbrev);
 					}
 					LocalConfig.getInstance().getMetaboliteNameIdMap().put(value, id);
 					System.out.println(LocalConfig.getInstance().getMetaboliteNameIdMap());
 				}
 			} else {
 				metabolitesTable.setValueAt(value, row, col);
 				LocalConfig.getInstance().getMetaboliteNameIdMap().put(value, id);
 			}
 		} else if (isMetabolitesEntryValid(col, value)) {
 			metabolitesTable.setValueAt(value, row, col);
 			formulaBar.setText("");
 		} else {
 			validPaste = false;
 		}	
 	}
 
 	public String duplicateSuffix(String value) {
 		String duplicateSuffix = GraphicalInterfaceConstants.DUPLICATE_SUFFIX;
 		if (LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey(value + duplicateSuffix)) {
 			int duplicateCount = Integer.valueOf(duplicateSuffix.substring(1, duplicateSuffix.length() - 1));
 			while (LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey(value + duplicateSuffix.replace("1", Integer.toString(duplicateCount + 1)))) {
 				duplicateCount += 1;
 			}
 			duplicateSuffix = duplicateSuffix.replace("1", Integer.toString(duplicateCount + 1));
 		}
 		return duplicateSuffix;
 	}
 	
 	public boolean isMetabolitesEntryValid(int columnIndex, String value) {
 		if (columnIndex == GraphicalInterfaceConstants.CHARGE_COLUMN) {
 			if (value != null && value.trim().length() > 0) {
 				EntryValidator validator = new EntryValidator();
 				if (!validator.isNumber(value)) {
 					setPasteError("Number format exception");
 			    	setReplaceAllError("Number format exception");
 			        return false;
 				}	           
 			}			
 		} else if (columnIndex == GraphicalInterfaceConstants.BOUNDARY_COLUMN) {
 			if (value.compareTo("true") == 0 || value.compareTo("false") == 0) {
 				return true;
 			} else {
 				setPasteError(GraphicalInterfaceConstants.INVALID_PASTE_BOOLEAN_VALUE);
 				setReplaceAllError(GraphicalInterfaceConstants.INVALID_REPLACE_ALL_BOOLEAN_VALUE);
 				return false;
 			}
 		} 	
 		return true;
 		 
 	}
 	
 	public void metabolitesClear() {
 		int startRow=(metabolitesTable.getSelectedRows())[0]; 
 		int startCol=(metabolitesTable.getSelectedColumns())[0];
 		// copy model for undo
 		DefaultTableModel oldMetabolitesModel = copyMetabolitesTableModel((DefaultTableModel) metabolitesTable.getModel());	
 		copyMetabolitesTableModels(oldMetabolitesModel); 
 		MetaboliteUndoItem undoItem = createMetaboliteUndoItem("", "", startRow, startCol, 1, UndoConstants.CLEAR_CONTENTS, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);
 		undoItem.setTableCopyIndex(LocalConfig.getInstance().getNumMetabolitesTableCopied());
 		setUndoOldCollections(undoItem);		
 		boolean valid = true;
 		//ArrayList<Integer> rowList = new ArrayList<Integer>();
 		//ArrayList<Integer> metabIdList = new ArrayList<Integer>();
 		//TODO: Clear must throw an error if user attempts to clear
 		//a used metabolite, but should be able to clear an unused
 		//metabolite - see delete, also should not be able to clear boundary		
 		if (startCol == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN || 
 				startCol == GraphicalInterfaceConstants.METABOLITE_NAME_COLUMN) {
 			JOptionPane.showMessageDialog(null,                
 					"Cannot clear Metab abbreviation or name column, some may be used.",                
 					"Clear Error",                                
 					JOptionPane.ERROR_MESSAGE);
 			valid = false;
 		} else {			
 			for (int r = 0; r < metabolitesTable.getSelectedColumns().length; r++) {
 				if (startCol + r == GraphicalInterfaceConstants.BOUNDARY_COLUMN) {
 					valid = false;
 				} 			
 			}
 			if (valid) {
 				/*
 				for (int r = 0; r < metabolitesTable.getSelectedRows().length; r++) {
 					int row = metabolitesTable.convertRowIndexToModel(startRow + r);
 					rowList.add(row);
 					int metabId = Integer.valueOf((String) metabolitesTable.getModel().getValueAt(row, 0));
 					metabIdList.add(metabId);
 				}
 				*/
 				for(int i=0; i < metabolitesTable.getSelectedRows().length ;i++) { 
 					for(int j=0; j < metabolitesTable.getSelectedColumns().length ;j++) { 					
 						//int viewRow = metabolitesTable.convertRowIndexToView(rowList.get(i));
 						metabolitesTable.setValueAt(" ", startRow + i, startCol + j);
 					} 
 				}
 				DefaultTableModel newMetabolitesModel = copyMetabolitesTableModel((DefaultTableModel) metabolitesTable.getModel());			
 				copyMetabolitesTableModels(newMetabolitesModel); 
 				setUndoNewCollections(undoItem);
 				setUpMetabolitesUndo(undoItem);				
 			} else {
 				JOptionPane.showMessageDialog(null,                
 						GraphicalInterfaceConstants.CLEAR_ERROR_MESSAGE,                
 						"Clear Error",                                
 						JOptionPane.ERROR_MESSAGE);
 				deleteMetabolitesPasteUndoItem();
 			}			
 		} 
 	}
 
 	/**************************************************************************/
 	//end metabolitesTable context menu methods
 	/**************************************************************************/
 
 	/*******************************************************************************/
 	//output pane methods
 	/*******************************************************************************/
 
 	//based on http://www.java2s.com/Code/Java/File-Input-Output/Textfileviewer.htm
 	public static void loadOutputPane(String path) {
 		File file;
 		FileReader in = null;
 
 		try {
 			file = new File(path); 
 			in = new FileReader(file); 
 			char[] buffer = new char[4096]; // Read 4K characters at a time
 			int len; 
 			outputTextArea.setText(""); 
 			while ((len = in.read(buffer)) != -1) { // Read a batch of chars
 				String s = new String(buffer, 0, len); 
 				outputTextArea.append(s); 
 			}
 			outputTextArea.setCaretPosition(0); 
 		}
 
 		catch (IOException e) {
 
 		}
 
 		finally {
 			try {
 				if (in != null)
 					in.close();
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	public void clearOutputPane() {
 		outputTextArea.setText(""); 
 	}
 
 	/*******************************************************************************/
 	//end output pane methods
 	/*******************************************************************************/
 
 	/***********************************************************************************/
 	//clipboard
 	/***********************************************************************************/
 
 	private static String getClipboardContents(Object requestor) {
 		Transferable t = Toolkit.getDefaultToolkit()
 				.getSystemClipboard().getContents(requestor);
 		if (t != null) {
 			DataFlavor df = DataFlavor.stringFlavor;
 			if (df != null) {
 				try {
 					Reader r = df.getReaderForText(t);
 					char[] charBuf = new char[512];
 					StringBuffer buf = new StringBuffer();
 					int n;
 					while ((n = r.read(charBuf, 0, charBuf.length)) > 0) {
 						buf.append(charBuf, 0, n);
 					}
 					r.close();
 					return (buf.toString());
 				} catch (IOException ex) {
 					ex.printStackTrace();
 				} catch (UnsupportedFlavorException ex) {
 					ex.printStackTrace();
 				}
 			}
 		}
 		return null;
 	}
 
 	private static boolean isClipboardContainingText(Object requestor) {
 		Transferable t = Toolkit.getDefaultToolkit()
 				.getSystemClipboard().getContents(requestor);
 		return t != null
 				&& (t.isDataFlavorSupported(DataFlavor.stringFlavor) || t
 						.isDataFlavorSupported(DataFlavor.plainTextFlavor));
 	}
 
 	private static void setClipboardContents(String s) {
 		StringSelection selection = new StringSelection(s);
 		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
 				selection, selection);
 	}
 
 	private String escape(Object cell) { 
 		return cell.toString().replace("\n", " ").replace("\t", " "); 
 	} 
 
 	private int numberOfClipboardRows() {
 		// fixes the bug where paste will not paste blank values
 		// over cells with non-blank values, also gets 
 		// number of rows copied from external application
 		int numRows = 0;
 		if (isClipboardContainingText(this)) {
 			String pasteString = ""; 
 			try { 
 				pasteString = (String)(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this).getTransferData(DataFlavor.stringFlavor)); 
 			} catch (Exception e1) { 
 
 			} 
 			for (int i=0 ; i<pasteString.length(); i++) { 
 				if( pasteString.charAt(i) == '\n' ) {
 					numRows += 1;
 				}
 			}
 			/*
 			String[] lines = pasteString.split("\n"); 
 			for (int i=0 ; i<lines.length; i++) { 
 				numRows += 1;
 			} 
 			 */					
 		}
 		return numRows;		
 	}
 
 	private int numberOfClipboardColumns() {
 		int numColumns = 0;
 		if (isClipboardContainingText(this)) {
 			String pasteString = ""; 
 			try { 
 				pasteString = (String)(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this).getTransferData(DataFlavor.stringFlavor)); 
 			} catch (Exception e1) { 
 
 			} 
 			String[] lines = pasteString.split("\n");
 			// fixes bug where if last column copied is blank, would not
 			// paste over cell with value
 			try {
 				for (int i=0 ; i<lines[0].length(); i++) { 
 					if( lines[0].charAt(i) == '\t' ) {
 						numColumns += 1;
 					}
 				}
 			} catch (Throwable t) {
 
 			}			
 		}
 		numColumns += 1;
 		return numColumns;		
 	}
 
 	/*****************************************************************************/
 	//end clipboard
 	/******************************************************************************/
 
 	/******************************************************************************/
 	// undo/redo
 	/******************************************************************************/
 
 	// adds image to OptionComponent
 	public void addImage(OptionComponent comp, JLabel label) {
 		label.setBorder(BorderFactory.createEmptyBorder(0,0,0,20));
 		label.setAlignmentX(LEFT_ALIGNMENT);
 		comp.add(label); 
 	}
 
 	public void enableOptionComponent(OptionComponent comp, JLabel label, JLabel grayedLabel) {
 		comp.setEnabled(true);
 		label.setVisible(true);
 		grayedLabel.setVisible(false);
 	}
 
 	public void disableOptionComponent(OptionComponent comp, JLabel label, JLabel grayedLabel) {
 		comp.setEnabled(false);
 		comp.buttonClicked = false;
 		label.setVisible(false);
 		grayedLabel.setVisible(true);
 	}
 
 	public ReactionUndoItem createReactionUndoItem(String oldValue, String newValue, 
     		int row, int column, int id, String undoType, String undoItemType) {
     	if (oldValue == null) {
     		oldValue = "";
     	}
     	if (newValue == null) {
     		newValue = "";
     	}
     	ReactionUndoItem undoItem = new ReactionUndoItem();
     	undoItem.setOldValue(oldValue);
     	undoItem.setNewValue(newValue);
     	undoItem.setRow(row);
     	undoItem.setColumn(column);
     	if (column > -1) {
     		undoItem.setColumnName(undoItem.displayReactionsColumnNameFromIndex(column, LocalConfig.getInstance().getReactionsMetaColumnNames()));
     	}    	
     	undoItem.setId(id);
     	undoItem.setUndoType(undoType);
     	undoItem.setUndoItemType(undoItemType);
 		return undoItem;
     	
     }
     
     public MetaboliteUndoItem createMetaboliteUndoItem(String oldValue, String newValue, 
     		int row, int column, int id, String undoType, String undoItemType) {
     	if (oldValue == null) {
     		oldValue = "";
     	}
     	if (newValue == null) {
     		newValue = "";
     	}
     	MetaboliteUndoItem undoItem = new MetaboliteUndoItem();
     	undoItem.setOldValue(oldValue);
     	undoItem.setNewValue(newValue);
     	undoItem.setRow(row);
     	undoItem.setColumn(column);
     	if (column > -1) {
     		undoItem.setColumnName(undoItem.displayMetabolitesColumnNameFromIndex(column, LocalConfig.getInstance().getMetabolitesMetaColumnNames()));
     	} 
     	undoItem.setId(id);
     	undoItem.setUndoType(undoType);
     	undoItem.setUndoItemType(undoItemType);
     	// make deep copies of lists and maps to be restored if undo is executed		
 		ArrayList<Integer> oldBlankMetabIds = null;
 		ArrayList<Integer> oldDuplicateIds = null;
 		Map<String, Object> oldMetaboliteNameIdMap = null;
 		Map<String, Object> oldMetaboliteUsedMap = null;
 		ArrayList<Integer> oldSuspiciousMetabolites = null;		
 		ArrayList<Integer> oldUnusedList = null;
 		
 		try {
 			oldBlankMetabIds = (ArrayList<Integer>)(ObjectCloner.deepCopy(LocalConfig.getInstance().getBlankMetabIds()));
 			oldMetaboliteNameIdMap = (Map<String, Object>) (ObjectCloner.deepCopy(LocalConfig.getInstance().getMetaboliteNameIdMap()));
 			oldMetaboliteUsedMap = (Map<String, Object>) (ObjectCloner.deepCopy(LocalConfig.getInstance().getMetaboliteUsedMap()));
 			oldSuspiciousMetabolites = (ArrayList<Integer>)(ObjectCloner.deepCopy(LocalConfig.getInstance().getSuspiciousMetabolites()));
 			oldUnusedList = (ArrayList<Integer>)(ObjectCloner.deepCopy(LocalConfig.getInstance().getUnusedList()));
 			undoItem.setOldBlankMetabIds(oldBlankMetabIds);
 			undoItem.setOldMetaboliteNameIdMap(oldMetaboliteNameIdMap);
 			undoItem.setOldMetaboliteUsedMap(oldMetaboliteUsedMap);
 			undoItem.setOldSuspiciousMetabolites(oldSuspiciousMetabolites);
 			undoItem.setOldUnusedList(oldUnusedList);
 		} catch (Exception e3) {
 			// TODO Auto-generated catch block
 			e3.printStackTrace();
 		}
 		return undoItem;
     	
     }
 
 	public void setUndoOldCollections(MetaboliteUndoItem undoItem) {
 		// make deep copies of lists and maps to be restored if undo is executed		
 		ArrayList<Integer> oldBlankMetabIds = new ArrayList<Integer>();
 		ArrayList<Integer> oldDuplicateIds = new ArrayList<Integer>();
 		Map<String, Object> oldMetaboliteNameIdMap = new HashMap<String, Object>();
 		Map<String, Object> oldMetaboliteUsedMap = new HashMap<String, Object>();
 		ArrayList<Integer> oldSuspiciousMetabolites = new ArrayList<Integer>();		
 		ArrayList<Integer> oldUnusedList = new ArrayList<Integer>();
 
 		try {
 			if (LocalConfig.getInstance().getBlankMetabIds() != null) {
 				oldBlankMetabIds = (ArrayList<Integer>)(ObjectCloner.deepCopy(LocalConfig.getInstance().getBlankMetabIds()));
 			}
 			if (LocalConfig.getInstance().getMetaboliteNameIdMap() != null) {
 				oldMetaboliteNameIdMap = (Map<String, Object>) (ObjectCloner.deepCopy(LocalConfig.getInstance().getMetaboliteNameIdMap()));
 			}
 			if (LocalConfig.getInstance().getMetaboliteUsedMap() != null) {
 				oldMetaboliteUsedMap = (Map<String, Object>) (ObjectCloner.deepCopy(LocalConfig.getInstance().getMetaboliteUsedMap()));
 			}
 			if (LocalConfig.getInstance().getSuspiciousMetabolites() != null) {
 				oldSuspiciousMetabolites = (ArrayList<Integer>)(ObjectCloner.deepCopy(LocalConfig.getInstance().getSuspiciousMetabolites()));
 			}
 			if (LocalConfig.getInstance().getUnusedList() != null) {
 				oldUnusedList = (ArrayList<Integer>)(ObjectCloner.deepCopy(LocalConfig.getInstance().getUnusedList()));
 			}
 			undoItem.setOldBlankMetabIds(oldBlankMetabIds);
 			undoItem.setOldMetaboliteNameIdMap(oldMetaboliteNameIdMap);
 			undoItem.setOldMetaboliteUsedMap(oldMetaboliteUsedMap);
 			undoItem.setOldSuspiciousMetabolites(oldSuspiciousMetabolites);
 			undoItem.setOldUnusedList(oldUnusedList);
 		} catch (Exception e3) {
 			// TODO Auto-generated catch block
 			e3.printStackTrace();
 		}
 	}
 
 	public void setUndoNewCollections(MetaboliteUndoItem undoItem) {
 		// make deep copies of lists and maps to be restored if redo is executed		
 		ArrayList<Integer> newBlankMetabIds = new ArrayList<Integer>();
 		ArrayList<Integer> newDuplicateIds = new ArrayList<Integer>();
 		Map<String, Object> newMetaboliteNameIdMap = new HashMap<String, Object>();
 		Map<String, Object> newMetaboliteUsedMap = new HashMap<String, Object>();
 		ArrayList<Integer> newSuspiciousMetabolites = new ArrayList<Integer>();	
 		ArrayList<Integer> newUnusedList = new ArrayList<Integer>();
 
 		try {
 			if (LocalConfig.getInstance().getBlankMetabIds() != null) {
 				newBlankMetabIds = (ArrayList<Integer>)(ObjectCloner.deepCopy(LocalConfig.getInstance().getBlankMetabIds()));
 			}
 			if (LocalConfig.getInstance().getMetaboliteNameIdMap() != null) {
 				newMetaboliteNameIdMap = (Map<String, Object>) (ObjectCloner.deepCopy(LocalConfig.getInstance().getMetaboliteNameIdMap()));
 			}
 			if (LocalConfig.getInstance().getMetaboliteUsedMap() != null) {
 				newMetaboliteUsedMap = (Map<String, Object>) (ObjectCloner.deepCopy(LocalConfig.getInstance().getMetaboliteUsedMap()));
 			}
 			if (LocalConfig.getInstance().getSuspiciousMetabolites() != null) {
 				newSuspiciousMetabolites = (ArrayList<Integer>)(ObjectCloner.deepCopy(LocalConfig.getInstance().getSuspiciousMetabolites()));
 			}
 			if (LocalConfig.getInstance().getUnusedList() != null) {
 				newUnusedList = (ArrayList<Integer>)(ObjectCloner.deepCopy(LocalConfig.getInstance().getUnusedList()));
 			}
 
 			undoItem.setNewBlankMetabIds(newBlankMetabIds);
 			undoItem.setNewMetaboliteNameIdMap(newMetaboliteNameIdMap);
 			undoItem.setNewMetaboliteUsedMap(newMetaboliteUsedMap);
 			undoItem.setNewSuspiciousMetabolites(newSuspiciousMetabolites);
 			undoItem.setNewUnusedList(newUnusedList);
 		} catch (Exception e3) {
 			// TODO Auto-generated catch block
 			e3.printStackTrace();
 		}
 	}
 
 	public void updateUndoButton() {
 		JScrollPopupMenu popupMenu = undoSplitButton.getPopupMenu();
 		addMenuItems(LocalConfig.getInstance().getUndoItemMap(), "undo");
 		if (undoCount > 0) {
 			enableOptionComponent(undoSplitButton, undoLabel, undoGrayedLabel);
 			undoItem.setEnabled(true);
 			Class cls = LocalConfig.getInstance().getUndoItemMap().get(LocalConfig.getInstance().getUndoItemMap().size()).getClass();
 			if ((cls.getName().equals("edu.rutgers.MOST.data.ReactionUndoItem"))) {
 				undoSplitButton.setToolTipText("Undo " + ((ReactionUndoItem) LocalConfig.getInstance().getUndoItemMap().get(LocalConfig.getInstance().getUndoItemMap().size())).createUndoDescription() + " (Ctrl+Z)");
 			} else if ((cls.getName().equals("edu.rutgers.MOST.data.MetaboliteUndoItem"))) {
 				undoSplitButton.setToolTipText("Undo " + ((MetaboliteUndoItem) LocalConfig.getInstance().getUndoItemMap().get(LocalConfig.getInstance().getUndoItemMap().size())).createUndoDescription() + " (Ctrl+Z)");
 			}			
 		} else {
 			disableOptionComponent(undoSplitButton, undoLabel, undoGrayedLabel);
 			undoItem.setEnabled(false);
 			undoSplitButton.setToolTipText("Can't Undo (Ctrl+Z)");
 		}		
 		undoCount += 1;	
 	}
 
 	public void updateRedoButton() {
 		JScrollPopupMenu popupMenu = redoSplitButton.getPopupMenu();
 		addMenuItems(LocalConfig.getInstance().getRedoItemMap(), "redo");
 		if (LocalConfig.getInstance().getRedoItemMap().size() > 0) {
 			enableOptionComponent(redoSplitButton, redoLabel, redoGrayedLabel);
 			redoItem.setEnabled(true);
 			Class cls = LocalConfig.getInstance().getRedoItemMap().get(LocalConfig.getInstance().getRedoItemMap().size()).getClass();
 			if ((cls.getName().equals("edu.rutgers.MOST.data.ReactionUndoItem"))) {
 				redoSplitButton.setToolTipText("Redo " + ((ReactionUndoItem) LocalConfig.getInstance().getRedoItemMap().get(LocalConfig.getInstance().getRedoItemMap().size())).createUndoDescription() + " (Ctrl+Y)");
 			} else if ((cls.getName().equals("edu.rutgers.MOST.data.MetaboliteUndoItem"))) {
 				redoSplitButton.setToolTipText("Redo " + ((MetaboliteUndoItem) LocalConfig.getInstance().getRedoItemMap().get(LocalConfig.getInstance().getRedoItemMap().size())).createUndoDescription() + " (Ctrl+Y)");
 			}
 		} else {
 			disableOptionComponent(redoSplitButton, redoLabel, redoGrayedLabel);
 			redoItem.setEnabled(false);
 			redoSplitButton.setToolTipText("Can't Redo (Ctrl+Y)");
 		}			
 	}
 
 	public void addMenuItems(final Map<Object, Object> undoMap, final String type) {
 		final JScrollPopupMenu popupMenu = new JScrollPopupMenu();
 		popupMenu.addMouseListener(new MouseListener() {
 			public void mouseClicked(MouseEvent e) {
 				popupMenu.setVisible(false);
 			}
 			public void mouseEntered(MouseEvent e) {
 				LocalConfig.getInstance().setUndoMenuIndex(undoMap.size() + 1);
 				popupMenu.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, GraphicalInterfaceConstants.UNDO_BORDER_HEIGHT, 0), "Cancel", TitledBorder.LEFT, TitledBorder.BOTTOM));
 			}
 			public void mouseExited(MouseEvent e) {
 			}
 			public void mousePressed(MouseEvent arg0) {
 			}
 			public void mouseReleased(MouseEvent arg0) {				
 			}
 		});
 		if (type.equals("undo")) {
 			undoSplitButton.setPopupMenu(popupMenu);
 		} else if (type.equals("redo")) {
 			redoSplitButton.setPopupMenu(popupMenu);
 		}
 		for (int i = undoMap.size() - 1; i > -1; i--) {
 			String item = "";
 			Class cls = undoMap.get(i + 1).getClass();
 			if ((cls.getName().equals("edu.rutgers.MOST.data.ReactionUndoItem"))) {
 				item = ((ReactionUndoItem) undoMap.get(i + 1)).createUndoDescription();
 			} else if ((cls.getName().equals("edu.rutgers.MOST.data.MetaboliteUndoItem"))) {
 				item = ((MetaboliteUndoItem) undoMap.get(i + 1)).createUndoDescription();
 			}
 			final JMenuItem menuItem = new JMenuItem(item);
 			menuItem.setName(Integer.toString(i + 1));
 			menuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					boolean scroll = true;
 					if (e.getActionCommand().equals(ENABLE)) {
 						int scrollRow = 0;
 						int scrollCol = 1;
 						for (int i = undoMap.size(); i >= Integer.valueOf(menuItem.getName()); i--) {
 							Class cls = undoMap.get(i).getClass();
 							if ((cls.getName().equals("edu.rutgers.MOST.data.ReactionUndoItem"))) {
 								int row = ((ReactionUndoItem) undoMap.get(i)).getRow();
 								if (row > -1) {
 									scrollRow = row;
 								}
 								int col = ((ReactionUndoItem) undoMap.get(i)).getColumn();
 								if (col > -1) {
 									scrollCol = col;
 								}
 								if (type.equals("undo")) {
 									if (((ReactionUndoItem) undoMap.get(i)).getUndoType().equals(UndoConstants.DELETE_COLUMN)) {
 										scrollCol = ((ReactionUndoItem) undoMap.get(i)).getDeletedColumnIndex();
 									}
 									reactionUndoAction(i);
 									DefaultTableModel model = (DefaultTableModel) reactionsTable.getModel();
 									setUpReactionsTable(model);
 									undoCount -= 1;	       					
 								} else if (type.equals("redo")) { 
 									if (((ReactionUndoItem) undoMap.get(i)).getUndoType().equals(UndoConstants.ADD_ROW)) {
 										//scrollRow = redoAddRowScrollRow(reactionsTable);
 										redoAddReactionRow();
 										scroll = false;
 									}
 									if (((ReactionUndoItem) undoMap.get(i)).getUndoType().equals(UndoConstants.ADD_COLUMN)) {
 										scrollCol = redoAddColumnScrollColumn("reactions");
 										addReacColumn = true;
 									}
 									reactionRedoAction(i);
 									DefaultTableModel model = (DefaultTableModel) reactionsTable.getModel();
 									setUpReactionsTable(model);
 								}  
 								updateUndoButton();
 								updateRedoButton();
 								addReacColumn = false;
 								if (i == Integer.valueOf(menuItem.getName())) {
 									DefaultTableModel model = (DefaultTableModel) reactionsTable.getModel();
 									setUpReactionsTable(model);
 									tabbedPane.setSelectedIndex(0);
 									if (scroll) {
 										scrollToLocation(reactionsTable, scrollRow, scrollCol);
 									}									
 								}											
 							} else if ((cls.getName().equals("edu.rutgers.MOST.data.MetaboliteUndoItem"))) {
 								int row = ((MetaboliteUndoItem) undoMap.get(i)).getRow();
 								if (row > -1) {
 									scrollRow = row;
 								}
 								int col = ((MetaboliteUndoItem) undoMap.get(i)).getColumn();
 								if (col > -1) {
 									scrollCol = col;
 								}
 								if (type.equals("undo")) {	
 									if (((MetaboliteUndoItem) undoMap.get(i)).getUndoType().equals(UndoConstants.DELETE_COLUMN)) {
 										scrollCol = ((MetaboliteUndoItem) undoMap.get(i)).getDeletedColumnIndex();
 									}
 									metaboliteUndoAction(i);
 									DefaultTableModel model = (DefaultTableModel) metabolitesTable.getModel();
 									setUpMetabolitesTable(model);
 									undoCount -= 1;	       					
 								} else if (type.equals("redo")) { 
 									if (((MetaboliteUndoItem) undoMap.get(i)).getUndoType().equals(UndoConstants.ADD_ROW)) {
 										//scrollRow = redoAddRowScrollRow(metabolitesTable);
 										redoAddMetaboliteRow();
 										scroll = false;
 									}
 									if (((MetaboliteUndoItem) undoMap.get(i)).getUndoType().equals(UndoConstants.ADD_COLUMN)) {
 										scrollCol = redoAddColumnScrollColumn("metabolites");
 										addMetabColumn = true;
 									}
 									metaboliteRedoAction(i);
 									DefaultTableModel model = (DefaultTableModel) metabolitesTable.getModel();
 									setUpMetabolitesTable(model);
 								} 
 								updateUndoButton();
 								updateRedoButton();
 								addMetabColumn = false;
 								if (i == Integer.valueOf(menuItem.getName())) {
 									DefaultTableModel model = (DefaultTableModel) metabolitesTable.getModel();
 									setUpMetabolitesTable(model);
 									tabbedPane.setSelectedIndex(1);
 									if (scroll) {
 										scrollToLocation(metabolitesTable, scrollRow, scrollCol);					
 									}									
 								}							
 							}
 						}
 						LocalConfig.getInstance().setUndoMenuIndex(undoMap.size() + 1);
 					} else if (e.getActionCommand().equals(DISABLE)) {
 						popupMenu.setVisible(false);
 					}					
 				}
 			});
 			menuItem.addMouseListener(new MouseListener() {
 				boolean increase;
 				public void mouseClicked(MouseEvent e) {
 				}
 				public void mouseEntered(MouseEvent e) {
 
 				}
 				public void mouseExited(MouseEvent e) {
 
 				}
 				public void mousePressed(MouseEvent arg0) {
 				}
 				public void mouseReleased(MouseEvent arg0) {				
 				}
 			});
 			menuItem.addChangeListener(new ChangeListener() {
 				public void stateChanged(ChangeEvent e) {
 					String lastmenuItem = "";
 					int numberOfActions = undoMap.size() - Integer.valueOf(menuItem.getName()) + 1; 
 					if (type.equals("undo")) {
 						lastmenuItem = "Undo " + Integer.toString(numberOfActions) + " Item(s)";
 					} else if (type.equals("redo")) {
 						lastmenuItem = "Redo " + Integer.toString(numberOfActions) + " Item(s)";
 					}
 					if (isMenuItemVisible(undoMap, popupMenu, menuItem)) {
 						menuItem.setActionCommand(ENABLE);
 						popupMenu.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, GraphicalInterfaceConstants.UNDO_BORDER_HEIGHT, 0), lastmenuItem, TitledBorder.LEFT, TitledBorder.BOTTOM));
 					} else {
 						menuItem.setActionCommand(DISABLE);
 						popupMenu.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, GraphicalInterfaceConstants.UNDO_BORDER_HEIGHT, 0), "Cancel", TitledBorder.LEFT, TitledBorder.BOTTOM));
 					}
 				}
 			});
 			popupMenu.getScrollBar().addAdjustmentListener(new AdjustmentListener() {
 				@Override
 				public void adjustmentValueChanged(AdjustmentEvent e) {
 					if(e.getValueIsAdjusting()) {
 						enableVisibleMenuItems(undoMap, popupMenu, menuItem);
 					}
 					if(popupMenu.getScrollBar().getValueIsAdjusting()) {
 						enableVisibleMenuItems(undoMap, popupMenu, menuItem);
 					}
 					if(e.getAdjustmentType() == AdjustmentEvent.TRACK) { 
 						enableVisibleMenuItems(undoMap, popupMenu, menuItem);
 					}
 				}
 			});	
 			menuItem.addMouseMotionListener(mml);
 			popupMenu.add(menuItem);
 		}
 	}
 
 	public void redoAddReactionRow() {
 		int id = LocalConfig.getInstance().getMaxReactionId();
 		DefaultTableModel model = (DefaultTableModel) reactionsTable.getModel();	
 		model.addRow(createReactionsRow(id));
 		setUpReactionsTable(model);
 		//set focus to id cell in new row in order to set row visible
 		int maxRow = reactionsTable.getModel().getRowCount();
 		int viewRow = reactionsTable.convertRowIndexToView(maxRow - 1);
 		setTableCellFocused(viewRow, 1, reactionsTable);
 		LocalConfig.getInstance().setMaxReactionId(id + 1);
 	}
 
 	public void redoAddMetaboliteRow() {
 		int id = LocalConfig.getInstance().getMaxMetaboliteId();
 		DefaultTableModel model = (DefaultTableModel) metabolitesTable.getModel();	
 		model.addRow(createMetabolitesRow(id));
 		setUpMetabolitesTable(model);
 		//set focus to id cell in new row in order to set row visible
 		int maxRow = metabolitesTable.getModel().getRowCount();
 		int viewRow = metabolitesTable.convertRowIndexToView(maxRow - 1);
 		setTableCellFocused(viewRow, 1, metabolitesTable);
 		LocalConfig.getInstance().setMaxMetaboliteId(id + 1);
 	}
 
 	public boolean isMenuItemVisible(Map<Object, Object> undoMap, JScrollPopupMenu popupMenu, JMenuItem menuItem) {
 		int visibilityCorrection = 0;		
 		double top = popupMenu.getScrollBar().getValue()/menuItem.getPreferredSize().getHeight();
 		double fraction = top - Math.floor(top);
 		if (fraction > GraphicalInterfaceConstants.UNDO_VISIBILITY_FRACTION) {
 			visibilityCorrection = 1;
 		} else {
 			visibilityCorrection = 0;
 		}
 		int topIndex = (int) (popupMenu.getScrollBar().getValue()/menuItem.getPreferredSize().getHeight());
 		int topItem = undoMap.size() - topIndex;
 		int bottomItem = topItem - (GraphicalInterfaceConstants.UNDO_MAX_VISIBLE_ROWS - 1) - visibilityCorrection;
 		if (Integer.valueOf(menuItem.getName()) > topItem || Integer.valueOf(menuItem.getName()) < bottomItem) { 
 			return false;
 		}
 		return true;
 
 	}
 
 	public void enableVisibleMenuItems(Map<Object, Object> undoMap, JScrollPopupMenu popupMenu, JMenuItem menuItem) {
 		if (isMenuItemVisible(undoMap, popupMenu, menuItem)) {
 			menuItem.setActionCommand(ENABLE);
 		} else {
 			menuItem.setActionCommand(DISABLE);
 			popupMenu.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, GraphicalInterfaceConstants.UNDO_BORDER_HEIGHT, 0), "Cancel", TitledBorder.LEFT, TitledBorder.BOTTOM));
 		}
 	}
 
 	private MouseMotionAdapter mml = new MouseMotionAdapter() {
 		public void mouseMoved( MouseEvent e) {
 			//JMenuItem menuItem = (JMenuItem) e.getSource();
 			//LocalConfig.getInstance().setUndoMenuIndex(Integer.valueOf(menuItem.getName()));
 		}
 	};
 
 	private MouseListener undoButtonMouseListener = new MouseAdapter() { 
 		public void mousePressed(MouseEvent e) {
 			if (undoSplitButton.buttonClicked) {
 				undoButtonAction();
 			}            
 		}
 	};
 
 	public void undoButtonAction() {
 		Class cls = LocalConfig.getInstance().getUndoItemMap().get(LocalConfig.getInstance().getUndoItemMap().size()).getClass();
 		if ((cls.getName().equals("edu.rutgers.MOST.data.ReactionUndoItem"))) {
 			reactionUndoButtonAction();		
 		} else if ((cls.getName().equals("edu.rutgers.MOST.data.MetaboliteUndoItem"))) {
 			metaboliteUndoButtonAction();
 		}				
 		// used for highlighting menu items, necessary here?
 		LocalConfig.getInstance().setUndoMenuIndex(0);  
 	}
 
 	public void reactionUndoButtonAction() {
 		boolean scroll = false;
 		int row = ((ReactionUndoItem) LocalConfig.getInstance().getUndoItemMap().get(LocalConfig.getInstance().getUndoItemMap().size())).getRow();
 		int col = ((ReactionUndoItem) LocalConfig.getInstance().getUndoItemMap().get(LocalConfig.getInstance().getUndoItemMap().size())).getColumn();	    				
 		if (((ReactionUndoItem) LocalConfig.getInstance().getUndoItemMap().get(LocalConfig.getInstance().getUndoItemMap().size())).getUndoType().equals(UndoConstants.DELETE_COLUMN)) {
 			col = ((ReactionUndoItem) LocalConfig.getInstance().getUndoItemMap().get(LocalConfig.getInstance().getUndoItemMap().size())).getDeletedColumnIndex();
 			scroll = true;
 		}
 		reactionUndoAction(LocalConfig.getInstance().getUndoItemMap().size());
 		DefaultTableModel model = (DefaultTableModel) reactionsTable.getModel();
 		setUpReactionsTable(model);
 		undoCount -= 1;				
 		updateUndoButton();
 		updateRedoButton(); 
 		tabbedPane.setSelectedIndex(0);
 		if (row > -1 && col > -1) {
 			scrollToLocation(reactionsTable, row, col);
 		}
 		if (scroll) {
 			if (row < 0) {
 				row = 0;
 			}
 			scrollToLocation(reactionsTable, row, col);
 		}
 	}
 
 	public void metaboliteUndoButtonAction() {
 		boolean scroll = false;
 		int row = ((MetaboliteUndoItem) LocalConfig.getInstance().getUndoItemMap().get(LocalConfig.getInstance().getUndoItemMap().size())).getRow();
 		int col = ((MetaboliteUndoItem) LocalConfig.getInstance().getUndoItemMap().get(LocalConfig.getInstance().getUndoItemMap().size())).getColumn();	    				
 		if (((MetaboliteUndoItem) LocalConfig.getInstance().getUndoItemMap().get(LocalConfig.getInstance().getUndoItemMap().size())).getUndoType().equals(UndoConstants.DELETE_COLUMN)) {
 			col = ((MetaboliteUndoItem) LocalConfig.getInstance().getUndoItemMap().get(LocalConfig.getInstance().getUndoItemMap().size())).getDeletedColumnIndex();
 			scroll = true;
 		}
 		metaboliteUndoAction(LocalConfig.getInstance().getUndoItemMap().size());
 		undoCount -= 1;
 		updateUndoButton();
 		updateRedoButton();
 		tabbedPane.setSelectedIndex(1);
 		if (row > -1 && col > -1) {
 			scrollToLocation(metabolitesTable, row, col);
 		}
 		if (scroll) {
 			if (row < 0) {
 				row = 0;
 			}
 			scrollToLocation(metabolitesTable, row, col);
 		}
 	}
 
 	public void metaboliteUndoAction(int index) {
     	((MetaboliteUndoItem) LocalConfig.getInstance().getUndoItemMap().get(index)).undo(); 
     	DefaultTableModel model = (DefaultTableModel) metabolitesTable.getModel();
     	setUpMetabolitesTable(model);
     	LocalConfig.getInstance().getRedoItemMap().put(redoCount, LocalConfig.getInstance().getUndoItemMap().get(index));
     	redoCount += 1;		
     	if (((MetaboliteUndoItem) LocalConfig.getInstance().getUndoItemMap().get(index)).getUndoType().equals(UndoConstants.SORT)) {
     		setMetabolitesSortColumnIndex(((MetaboliteUndoItem) LocalConfig.getInstance().getUndoItemMap().get(index)).getOldSortColumnIndex());
     		setMetabolitesSortOrder(((MetaboliteUndoItem) LocalConfig.getInstance().getUndoItemMap().get(index)).getOldSortOrder());
     		LocalConfig.getInstance().getMetabolitesRedoSortColumns().add(getMetabolitesSortColumnIndex());
     		LocalConfig.getInstance().getMetabolitesRedoSortOrderList().add(getMetabolitesSortOrder());
     		LocalConfig.getInstance().getMetabolitesSortColumns().remove(LocalConfig.getInstance().getMetabolitesSortColumns().size() - 1);
     		LocalConfig.getInstance().getMetabolitesSortOrderList().remove(LocalConfig.getInstance().getMetabolitesSortOrderList().size() - 1);
     		//DefaultTableModel model = (DefaultTableModel) metabolitesTable.getModel();
     		setUpMetabolitesTable(model);
     	}
     	LocalConfig.getInstance().getUndoItemMap().remove(index);
     	undoCount -= 1;
     	highlightUnusedMetabolites = false;
 		highlightUnusedMetabolitesItem.setState(false);
     }
 
     public void reactionUndoAction(int index) {
     	((ReactionUndoItem) LocalConfig.getInstance().getUndoItemMap().get(index)).undo(); 
     	DefaultTableModel model = (DefaultTableModel) reactionsTable.getModel();
     	setUpReactionsTable(model);
     	LocalConfig.getInstance().getRedoItemMap().put(redoCount, LocalConfig.getInstance().getUndoItemMap().get(index));    	
     	redoCount += 1;		
     	if (((ReactionUndoItem) LocalConfig.getInstance().getUndoItemMap().get(index)).getUndoType().equals(UndoConstants.SORT)) {
     		setReactionsSortColumnIndex(((ReactionUndoItem) LocalConfig.getInstance().getUndoItemMap().get(index)).getOldSortColumnIndex());
     		setReactionsSortOrder(((ReactionUndoItem) LocalConfig.getInstance().getUndoItemMap().get(index)).getOldSortOrder());
     		LocalConfig.getInstance().getReactionsRedoSortColumns().add(getReactionsSortColumnIndex());
     		LocalConfig.getInstance().getReactionsRedoSortOrderList().add(getReactionsSortOrder());
     		LocalConfig.getInstance().getReactionsSortColumns().remove(LocalConfig.getInstance().getReactionsSortColumns().size() - 1);
     		LocalConfig.getInstance().getReactionsSortOrderList().remove(LocalConfig.getInstance().getReactionsSortOrderList().size() - 1);
     		//DefaultTableModel model = (DefaultTableModel) reactionsTable.getModel();
     		setUpReactionsTable(model);
     	} 
     	LocalConfig.getInstance().getUndoItemMap().remove(index);
     	undoCount -= 1;		
     }
 
 	private MouseListener redoButtonMouseListener = new MouseAdapter() { 
 		public void mousePressed(MouseEvent e) {
 			if (redoSplitButton.buttonClicked) {
 				redoButtonAction();
 			}            
 		}
 	};
 
 	public void redoButtonAction() {
 		Class cls = LocalConfig.getInstance().getRedoItemMap().get(LocalConfig.getInstance().getRedoItemMap().size()).getClass();
 		if ((cls.getName().equals("edu.rutgers.MOST.data.ReactionUndoItem"))) {
 			reactionRedoButtonAction();
 		} else if ((cls.getName().equals("edu.rutgers.MOST.data.MetaboliteUndoItem"))) {
 			metaboliteRedoButtonAction();
 		}
 		// used for highlighting menu items, necessary here?
 		LocalConfig.getInstance().setUndoMenuIndex(0); 
 	}
 
 	public void reactionRedoButtonAction() {
 		boolean scroll = false;
 		int row = ((ReactionUndoItem) LocalConfig.getInstance().getRedoItemMap().get(LocalConfig.getInstance().getRedoItemMap().size())).getRow();
 		int col = ((ReactionUndoItem) LocalConfig.getInstance().getRedoItemMap().get(LocalConfig.getInstance().getRedoItemMap().size())).getColumn();	    				
 		if (((ReactionUndoItem) LocalConfig.getInstance().getRedoItemMap().get(LocalConfig.getInstance().getRedoItemMap().size())).getUndoType().equals(UndoConstants.ADD_ROW)) {
 			//row = redoAddRowScrollRow(reactionsTable);
 			redoAddReactionRow();
 		}
 		if (((ReactionUndoItem) LocalConfig.getInstance().getRedoItemMap().get(LocalConfig.getInstance().getRedoItemMap().size())).getUndoType().equals(UndoConstants.ADD_COLUMN)) {
 			col = redoAddColumnScrollColumn("reactions");
 			addReacColumn = true;
 			scroll = true;
 		}
 		reactionRedoAction(LocalConfig.getInstance().getRedoItemMap().size());
 		updateUndoButton();
 		updateRedoButton();  
 		DefaultTableModel model = (DefaultTableModel) reactionsTable.getModel();
 		setUpReactionsTable(model);
 		tabbedPane.setSelectedIndex(0);
 		if (row > -1 && col > -1) {
 			scrollToLocation(reactionsTable, row, col);  					
 		}
 		if (scroll) {
 			if (row < 0) {
 				row = 0;
 			}
 			scrollToLocation(reactionsTable, row, col);
 		}
 		addReacColumn = false;
 	}
 
 	public void metaboliteRedoButtonAction() {
 		boolean scroll = true;
 		int row = ((MetaboliteUndoItem) LocalConfig.getInstance().getRedoItemMap().get(LocalConfig.getInstance().getRedoItemMap().size())).getRow();
 		int col = ((MetaboliteUndoItem) LocalConfig.getInstance().getRedoItemMap().get(LocalConfig.getInstance().getRedoItemMap().size())).getColumn();	    	
 		if (((MetaboliteUndoItem) LocalConfig.getInstance().getRedoItemMap().get(LocalConfig.getInstance().getRedoItemMap().size())).getUndoType().equals(UndoConstants.ADD_ROW)) {
 			//row = redoAddRowScrollRow(metabolitesTable);
 			redoAddMetaboliteRow();
 		}
 		if (((MetaboliteUndoItem) LocalConfig.getInstance().getRedoItemMap().get(LocalConfig.getInstance().getRedoItemMap().size())).getUndoType().equals(UndoConstants.ADD_COLUMN)) {
 			col = redoAddColumnScrollColumn("metabolites");
 			addMetabColumn = true;
 			scroll = true;
 		}
 		metaboliteRedoAction(LocalConfig.getInstance().getRedoItemMap().size());
 		updateUndoButton();
 		updateRedoButton();
 		DefaultTableModel model = (DefaultTableModel) metabolitesTable.getModel();
 		setUpMetabolitesTable(model);
 		tabbedPane.setSelectedIndex(1);
 		if (row > -1 && col > -1) {
 			scrollToLocation(metabolitesTable, row, col);				
 		}
 		if (scroll) {
 			if (row < 0) {
 				row = 0;
 			}
 			scrollToLocation(metabolitesTable, row, col);
 		}
 		addMetabColumn = false;
 	}
 
 	public void metaboliteRedoAction(int index) {    	
     	((MetaboliteUndoItem) LocalConfig.getInstance().getRedoItemMap().get(index)).redo();     	
     	LocalConfig.getInstance().getUndoItemMap().put(undoCount, LocalConfig.getInstance().getRedoItemMap().get(index));
     	//undoCount += 1;
     	if (((MetaboliteUndoItem) LocalConfig.getInstance().getRedoItemMap().get(index)).getUndoType().equals(UndoConstants.SORT)) {
     		setMetabolitesSortColumnIndex(((MetaboliteUndoItem) LocalConfig.getInstance().getRedoItemMap().get(index)).getNewSortColumnIndex());
     		setMetabolitesSortOrder(((MetaboliteUndoItem) LocalConfig.getInstance().getRedoItemMap().get(index)).getNewSortOrder());
     		LocalConfig.getInstance().getMetabolitesSortColumns().add(getMetabolitesSortColumnIndex());
     		LocalConfig.getInstance().getMetabolitesSortOrderList().add(getMetabolitesSortOrder());
     		LocalConfig.getInstance().getMetabolitesRedoSortColumns().remove(LocalConfig.getInstance().getMetabolitesRedoSortColumns().size() - 1);
     		LocalConfig.getInstance().getMetabolitesRedoSortOrderList().remove(LocalConfig.getInstance().getMetabolitesRedoSortOrderList().size() - 1);
     		DefaultTableModel model = (DefaultTableModel) metabolitesTable.getModel();
     		setUpMetabolitesTable(model);
     	}
     	LocalConfig.getInstance().getRedoItemMap().remove(index);
     	redoCount -= 1; 
     	highlightUnusedMetabolites = false;
 		highlightUnusedMetabolitesItem.setState(false);
     }
     
     public void reactionRedoAction(int index) {    	
     	((ReactionUndoItem) LocalConfig.getInstance().getRedoItemMap().get(index)).redo();  
     	LocalConfig.getInstance().getUndoItemMap().put(undoCount, LocalConfig.getInstance().getRedoItemMap().get(index));    	
     	//undoCount += 1;
     	if (((ReactionUndoItem) LocalConfig.getInstance().getRedoItemMap().get(index)).getUndoType().equals(UndoConstants.SORT)) {
     		setReactionsSortColumnIndex(((ReactionUndoItem) LocalConfig.getInstance().getRedoItemMap().get(index)).getNewSortColumnIndex());
     		setReactionsSortOrder(((ReactionUndoItem) LocalConfig.getInstance().getRedoItemMap().get(index)).getNewSortOrder());
     		LocalConfig.getInstance().getReactionsSortColumns().add(getReactionsSortColumnIndex());
     		LocalConfig.getInstance().getReactionsSortOrderList().add(getReactionsSortOrder());
     		LocalConfig.getInstance().getReactionsRedoSortColumns().remove(LocalConfig.getInstance().getReactionsRedoSortColumns().size() - 1);
     		LocalConfig.getInstance().getReactionsRedoSortOrderList().remove(LocalConfig.getInstance().getReactionsRedoSortOrderList().size() - 1);
     		DefaultTableModel model = (DefaultTableModel) reactionsTable.getModel();
     		setUpReactionsTable(model);
     	}
     	LocalConfig.getInstance().getRedoItemMap().remove(index);
     	redoCount -= 1;
     }
     
 	public void setUpMetabolitesUndo(MetaboliteUndoItem undoItem) {
 		setUndoNewCollections(undoItem);
 		LocalConfig.getInstance().getUndoItemMap().put(undoCount, undoItem);				
 		updateUndoButton();
 		clearRedoButton();
 	}
 
 	public void setUpReactionsUndo(ReactionUndoItem undoItem) {
         ArrayList<Integer> addedMetabList = new ArrayList<Integer>();		
 		try {
 			if (LocalConfig.getInstance().getBlankMetabIds() != null) {
 				addedMetabList = (ArrayList<Integer>)(ObjectCloner.deepCopy(LocalConfig.getInstance().getAddedMetabolites()));
 			}			
 			undoItem.setAddedMetabolites(addedMetabList);
 	    	LocalConfig.getInstance().getUndoItemMap().put(undoCount, undoItem);				
 			updateUndoButton();
 			clearRedoButton();
 		} catch (Exception e3) {
 			// TODO Auto-generated catch block
 			e3.printStackTrace();
 		}
     }
     
     public static String tableCopySuffix(int count) {
     	return new DecimalFormat("000").format(count);
     }
     
     public void copyReactionsTableModels(DefaultTableModel model) {
     	int numCopied = LocalConfig.getInstance().getNumReactionTablesCopied();
 		LocalConfig.getInstance().setNumReactionTablesCopied(numCopied + 1);
 		LocalConfig.getInstance().getReactionsUndoTableModelMap().put(Integer.toString(LocalConfig.getInstance().getNumReactionTablesCopied()), model);
 		System.out.println(LocalConfig.getInstance().getReactionsUndoTableModelMap());
     }
     
     public void copyMetabolitesTableModels(DefaultTableModel model) {
     	int numCopied = LocalConfig.getInstance().getNumMetabolitesTableCopied();
 		LocalConfig.getInstance().setNumMetabolitesTableCopied(numCopied + 1);
 		LocalConfig.getInstance().getMetabolitesUndoTableModelMap().put(Integer.toString(LocalConfig.getInstance().getNumMetabolitesTableCopied()), model);
 		System.out.println(LocalConfig.getInstance().getMetabolitesUndoTableModelMap());
     }    
     
     // any changes in table clears redo button
     public void clearRedoButton() {
     	disableOptionComponent(redoSplitButton, redoLabel, redoGrayedLabel);
     	redoItem.setEnabled(false);
     	redoSplitButton.setToolTipText("Can't Redo (Ctrl+Y)");
     	redoCount = 1;
     	LocalConfig.getInstance().getRedoItemMap().clear();
     	LocalConfig.getInstance().getReactionsRedoSortColumns().clear();
 		LocalConfig.getInstance().getReactionsRedoSortOrderList().clear();
 		LocalConfig.getInstance().getMetabolitesRedoSortColumns().clear();
 		LocalConfig.getInstance().getMetabolitesRedoSortOrderList().clear();
     }
     
     public void scrollToLocation(JXTable table, int row, int column) {
     	try {
     		table.scrollRectToVisible(table.getCellRect(row, column, false));
     		setTableCellFocused(row, column, table);
     		int viewRow = table.convertRowIndexToModel(row);
     		formulaBar.setText((String) table.getModel().getValueAt(viewRow, column));
     	} catch (Throwable t) {
     		
     	}    	
     }
     
     public int redoAddRowScrollRow(JXTable table) {
     	int maxRow = table.getModel().getRowCount();
 		int row = table.convertRowIndexToView(maxRow - 1);
 		row += 1;
 		
 		return row;
     }
     
     public int redoAddColumnScrollColumn(String tableName) {
     	int col = 0;
     	if (tableName.equals("reactions")) {
     		col = LocalConfig.getInstance().getReactionsMetaColumnNames().size() + GraphicalInterfaceConstants.REACTIONS_COLUMN_NAMES.length;
     	} else if (tableName.equals("metabolites")) {
     		col = LocalConfig.getInstance().getMetabolitesMetaColumnNames().size() + GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES.length;
     	}
     			
 		return col;
     }
 
 
 	/******************************************************************************/
 	// end undo/redo
 	/******************************************************************************/
 
 	/********************************************************************************/
 	// toolbar methods
 	/********************************************************************************/
 
 	public void setUpToolbarButton(final JButton button) {
 		// based on http://www.java2s.com/Tutorial/Java/0240__Swing/Buttonsusedintoolbars.htm
 		if (!System.getProperty("java.version").startsWith("1.3")) {
 			button.setOpaque(false);
 			button.setBackground(new java.awt.Color(0, 0, 0, 0));
 		}
 		button.setBorderPainted(false);
 		button.setMargin(new Insets(2, 2, 2, 2));
 		button.addMouseListener(new MouseListener() {
 			public void mouseClicked(MouseEvent e) {
 			}
 			public void mouseEntered(MouseEvent e) {
 				if (button.isEnabled()) {
 					button.setBorderPainted(true);
 				}				
 			}
 			public void mouseExited(MouseEvent e) {
 				button.setBorderPainted(false);
 			}
 			public void mousePressed(MouseEvent arg0) {
 			}
 			public void mouseReleased(MouseEvent arg0) {				
 			}
 		});
 	}
 
 	ActionListener copyButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			if (tabbedPane.getSelectedIndex() == 0) {
 				reactionsCopy();
 			} else if (tabbedPane.getSelectedIndex() == 1) {
 				metabolitesCopy();
 			}
 		}
 	};
 
 	ActionListener pasteButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			if (tabbedPane.getSelectedIndex() == 0) {
 				reactionsPaste();
 			} else if (tabbedPane.getSelectedIndex() == 1) {
 				metabolitesPaste();
 			}
 		}
 	};
 
 	ActionListener findButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {	
 			if (!findMode) {
 				showFindReplace();
 			}
 			findReplaceItem.setEnabled(false);
 			findbutton.setEnabled(false);
 		}
 	};
 
 	/********************************************************************************/
 	// end toolbar methods
 	/********************************************************************************/
 
 	/*******************************************************************************/
 	// find/replace methods
 	/******************************************************************************/
 
 	public void showFindReplace() {
 		LocalConfig.getInstance().setReactionsLocationsListCount(0);
 		LocalConfig.getInstance().setMetabolitesLocationsListCount(0);
 		LocalConfig.getInstance().findFieldChanged = false;
 		FindReplaceDialog findReplace = new FindReplaceDialog();
 		setFindReplaceDialog(findReplace);
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 
 		int x = (screenSize.width - findReplace.getSize().width)/2;
 		int y = (screenSize.height - findReplace.getSize().height)/2;
 
 		findReplace.setIconImages(icons);
 		findReplace.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		findReplace.setAlwaysOnTop(true);        
 		//findReplace.setLocation(x + 420, y);
 		// Find/Replace positioned at far right on screen so it does not obscure scroll bar
 		findReplace.setLocation((screenSize.width - findReplace.getSize().width) - 10, y);
 		findReplace.setVisible(true);
 		findReplace.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent evt) {
 				findReplaceCloseAction();
 				getFindReplaceDialog().setVisible(false);
 				getFindReplaceDialog().dispose();
 			}
 		});
 		findButtonReactionsClicked = false;
 		findButtonMetabolitesClicked = false;
 		// ensure states of boolean values match states of findReplace frame
 		searchBackwards = false;
 		matchCase = false;
 		wrapAround = false;
 		searchSelectedArea = false;
 		findMode = true;
 	}	
 
 	// start reactions find replace
 
 	public int reactionsFindStartIndex() {
 		// always start at 0 when in column or row selection mode
 		int startIndex = 0;
 		// start from selected cell
 		if (!searchSelectedArea) {
 			if (reactionsTable.getSelectedRow() > -1 && reactionsTable.getSelectedColumn() > -1) {
 				int row = reactionsTable.getSelectedRow();
 				int col = reactionsTable.getSelectedColumn();
 				startIndex = findStartIndex(row, col, getReactionsFindLocationsList());
 			}				
 		} else {
 			if (searchBackwards) {
 				startIndex = getReactionsFindLocationsList().size() - 1;
 			} 		
 		}
 		return startIndex;
 
 	}
 
 	public int findStartIndex(int row, int col, ArrayList<ArrayList<Integer>> locationList) {
 		int startIndex = 0;
 		boolean sameCell = false;
 		boolean sameRow = false;
 		boolean rowGreater = false;			
 		for (int i = 0; i < locationList.size(); i++) {					
 			if (locationList.get(i).get(0) == row) { 
 				if (locationList.get(i).get(1) == col) {
 					sameCell = true;
 				}
 				if (locationList.get(i).get(1) > col) {	
 					if (!sameRow) {
 						startIndex = i;
 					}
 					sameRow = true;
 				}
 			} else if (!sameRow && locationList.get(i).get(0) > row) { 
 				if (!rowGreater) {
 					startIndex = i;
 				}
 				rowGreater = true;
 			} 
 		}			
 		if (rowGreater || sameRow) {
 		//if (rowGreater) {
 			// if string not found after selected cell
 		} else {
 			if (wrapAround) {
 				startIndex = 0;
 			} else {
 				startIndex = -1;
 			}				
 		}
 		// if search backwards, index will be 1 before than selected cell not 1 after
 		if (searchBackwards) {
 			if (sameCell) {
 				startIndex -= 2;
 			} else {
 				startIndex -= 1;
 			}				
 		}
 		if (startIndex == -1 && wrapAround) {
 			startIndex = locationList.size() - 1;
 		}		
 		return startIndex;
 
 	}
 
 	ActionListener findReactionsButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			if (tabbedPane.getSelectedIndex() == 0) {
 				reactionsFindAction();
 			}
 			findButtonReactionsClicked = true;
 		}
 	};
 
 	// this has been removed since the keyboard shortcut does not work due
 	// to button action causes Find/Replace dialog to lose focus
 	/*
 	ActionListener replaceFindReactionsButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			if (tabbedPane.getSelectedIndex() == 0) {
 				reactionsFindAll = false;
 				reactionsReplace();				
 				reactionsFindNext();
 			}
 			// button works but even with this line of code, Find/Replace
 			// does not regain focus after updating table.
 			getFindReplaceDialog().requestFocus();
 		}
 	};
 	 */
 
 	public void reactionsFindAction() {
 		reactionsFindAll = false;
 		reactionsTable.repaint();
 		ArrayList<ArrayList<Integer>> locationList = reactionsLocationsList();
 		setReactionsFindLocationsList(locationList);
 		// uses window listener for focus event and row column change to reset button
 		// to not clicked if user changes selected cell.
 		// for first click of find button, find starts from first cell after (or before when backwards)
 		// that contains string, after this first click, find just iterates through list				
 		if (!findButtonReactionsClicked) {
 			if (reactionsFindStartIndex() > -1) {
 				LocalConfig.getInstance().setReactionsLocationsListCount(reactionsFindStartIndex());
 				reactionsFindNext();
 			} else {
 				notFoundAction();	
 				if (wrapAround) {
 					if (searchBackwards) {
 						LocalConfig.getInstance().setReactionsLocationsListCount(getReactionsFindLocationsList().size() - 1);
 					} else {
 						LocalConfig.getInstance().setReactionsLocationsListCount(0);
 					}
 				}
 			}					
 		} else {
 			reactionsFindNext();
 		}
 	}
 
 	public void notFoundAction() {
 		getFindReplaceDialog().setAlwaysOnTop(false);
 		Object[] options = {"    Yes    ", "    No    ",};
 		int choice = JOptionPane.showOptionDialog(null, 
 				"MOST has not found the item you are searching for.\nDo you want to start over from the beginning?", 
 				"Item Not Found", 
 				JOptionPane.YES_NO_OPTION, 
 				JOptionPane.QUESTION_MESSAGE, 
 				null, options, options[0]);
 		if (choice == JOptionPane.YES_OPTION) {
 			wrapAround = true; 
 			getFindReplaceDialog().wrapCheckBox.setSelected(true);
 			if (searchBackwards) {
 				LocalConfig.getInstance().setReactionsLocationsListCount(getReactionsFindLocationsList().size() - 1);
 			} else {
 				LocalConfig.getInstance().setReactionsLocationsListCount(0);
 			}
 		}
 		if (choice == JOptionPane.NO_OPTION) {
 
 		}
 		getFindReplaceDialog().setAlwaysOnTop(true);
 	}
 
 	public void reactionsFindNext() {
 		reactionsTable.repaint();
 		ArrayList<ArrayList<Integer>> locationList = reactionsLocationsList();
 		if (locationList.size() == 0) {
 			notFoundAction();
 			LocalConfig.getInstance().setReactionsLocationsListCount(0);
 		} else {
 			try {
 				// set focus to an invisible cell to give appearance that find cell is 
 				// only cell selected
 				reactionsTable.changeSelection(locationList.get(LocalConfig.getInstance().getReactionsLocationsListCount()).get(0), 0, false, false);
 				changeReactionFindSelection = false;
 				setReactionsReplaceLocation(locationList.get(LocalConfig.getInstance().getReactionsLocationsListCount()));			
 				reactionsTable.requestFocus();
 				reactionsTable.scrollRectToVisible(reactionsTable.getCellRect(locationList.get(LocalConfig.getInstance().getReactionsLocationsListCount()).get(0), locationList.get(LocalConfig.getInstance().getReactionsLocationsListCount()).get(1), false));
 				int viewRow = reactionsTable.convertRowIndexToModel(reactionsTable.getSelectedRow());
 				formulaBar.setText((String) reactionsTable.getModel().getValueAt(viewRow, locationList.get(LocalConfig.getInstance().getReactionsLocationsListCount()).get(1)));
 				getFindReplaceDialog().requestFocus();
 				// if not at end of list increment, else start over
 				int count = LocalConfig.getInstance().getReactionsLocationsListCount();
 				if (!searchBackwards) {
 					if (LocalConfig.getInstance().getReactionsLocationsListCount() < (getReactionsFindLocationsList().size() - 1)) {
 						count += 1;
 						LocalConfig.getInstance().setReactionsLocationsListCount(count);
 					} else {
 						if (wrapAround) {							
 							count = 0;
 							LocalConfig.getInstance().setReactionsLocationsListCount(count);							
 						} else {							
 							if (throwNotFoundError) {															
 								notFoundAction();
 								throwNotFoundError = false;
 							}
 							throwNotFoundError = true;
 						}
 					}
 				} else {
 					if (LocalConfig.getInstance().getReactionsLocationsListCount() > 0) {
 						count -= 1;
 						LocalConfig.getInstance().setReactionsLocationsListCount(count);
 					} else {
 						if (wrapAround) {							
 							count = locationList.size() - 1;
 							LocalConfig.getInstance().setReactionsLocationsListCount(count);							
 						} else {							
 							if (throwNotFoundError) {															
 								notFoundAction();
 								throwNotFoundError = false;
 							}
 							throwNotFoundError = true;
 						}
 					}
 				}						
 			} catch (Throwable t){
 				LocalConfig.getInstance().setReactionsLocationsListCount(LocalConfig.getInstance().getReactionsLocationsListCount());
 				if (locationList.size() > 0) {
 					try {
 						//reactionsTable.changeSelection(locationList.get(LocalConfig.getInstance().getReactionsLocationsListCount()).get(0), locationList.get(LocalConfig.getInstance().getReactionsLocationsListCount()).get(1), false, false);
 						//reactionsTable.requestFocus();
 					} catch (Throwable t1){
 
 					}					
 				}				
 			}										
 		}
 		getFindReplaceDialog().requestFocus();
 	}
 
 	ActionListener findAllReactionsButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {			
 			if (tabbedPane.getSelectedIndex() == 0) {
 				ArrayList<ArrayList<Integer>> locationList = reactionsLocationsList();
 				setReactionsFindLocationsList(locationList);
 				if (locationList.size() == 0) {
 					notFoundAction();				
 				} else {
 					// set focus to first found item
 					reactionsTable.changeSelection(locationList.get(0).get(0), locationList.get(0).get(1), false, false);
 					reactionsTable.requestFocus();
 					// enables highlighter
 					reactionsFindAll = true;
 					getFindReplaceDialog().requestFocus();
 				}			
 			}			
 		}
 	};
 
 	public ArrayList<ArrayList<Integer>> reactionsLocationsList() {		
 		int rowStartIndex = 0;
 		int rowEndIndex = reactionsTable.getRowCount();
 		// start with 1 to avoid including hidden id column
 		int colStartIndex = 1;
 		int colEndIndex = reactionsTable.getColumnCount();
 		if (searchSelectedArea) {
 			if (!findButtonReactionsClicked) {
 				int numcols=reactionsTable.getSelectedColumnCount(); 
 				int numrows=reactionsTable.getSelectedRowCount(); 
 				int[] rowsselected=reactionsTable.getSelectedRows(); 
 				int[] colsselected=reactionsTable.getSelectedColumns();
 				if (numrows > 0 && getSelectionMode() != 1) {
 					rowStartIndex = rowsselected[0];
 					rowEndIndex = rowsselected[0] + numrows;
 					selectedReactionsRowStartIndex = rowsselected[0];
 					selectedReactionsRowEndIndex = rowsselected[0] + numrows;
 				}			
 				if (numcols > 0 && getSelectionMode() != 2) {
 					colStartIndex = colsselected[0];
 					colEndIndex = colsselected[0] + numcols;
 					selectedReactionsColumnStartIndex = colsselected[0];
 					selectedReactionsColumnEndIndex = colsselected[0] + numcols;
 				}	
 			}					
 		} else {
 			selectedReactionsRowStartIndex = 0;
 			selectedReactionsRowEndIndex = reactionsTable.getRowCount();
 			// start with 1 to avoid including hidden id column
 			selectedReactionsColumnStartIndex = 1;
 			selectedReactionsColumnEndIndex = reactionsTable.getColumnCount();
 		}
 		ArrayList<ArrayList<Integer>> reactionsLocationsList = new ArrayList<ArrayList<Integer>>();
 		for (int r = selectedReactionsRowStartIndex; r < selectedReactionsRowEndIndex; r++) {					
 			for (int c = selectedReactionsColumnStartIndex; c < selectedReactionsColumnEndIndex; c++) {					
 				int viewRow = reactionsTable.convertRowIndexToModel(r);
 				if (reactionsTable.getModel().getValueAt(viewRow, c) != null) {
 					String cellValue = (String) reactionsTable.getModel().getValueAt(viewRow, c);
 					String findValue = findReplaceDialog.getFindText();
 					if (!matchCase) {
 						cellValue = cellValue.toLowerCase();
 						findValue = findValue.toLowerCase();						
 					}
 					if (cellValue.contains(findValue)) {
 						ArrayList<Integer> rowColumnList = new ArrayList<Integer>();
 						rowColumnList.add(r);
 						rowColumnList.add(c);
 						reactionsLocationsList.add(rowColumnList);
 					}					
 				}
 			}
 		}
 		return reactionsLocationsList;
 
 	}
 
 	ActionListener replaceReactionsButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {	
 			if (tabbedPane.getSelectedIndex() == 0) {
 				reactionsReplace();				
 			}
 		}
 	};
 
 	public void reactionsReplace() {
 		int viewRow = reactionsTable.convertRowIndexToModel(getReactionsReplaceLocation().get(0));
 		String oldValue = (String) reactionsTable.getModel().getValueAt(viewRow, getReactionsReplaceLocation().get(1));		
 		String newValue = replaceValue(oldValue, replaceLocation(oldValue));
 		ReactionUndoItem undoItem = createReactionUndoItem(oldValue, newValue, reactionsTable.getSelectedRow(), getReactionsReplaceLocation().get(1), viewRow + 1, UndoConstants.REPLACE, UndoConstants.REACTION_UNDO_ITEM_TYPE);
 		ArrayList<ArrayList<Integer>> locationList = reactionsLocationsList();
 		if (replaceLocation(oldValue) > -1) {
 			reactionsTable.getModel().setValueAt(newValue, viewRow, getReactionsReplaceLocation().get(1));
 			updateReactionsCellIfValid(oldValue, newValue, viewRow, getReactionsReplaceLocation().get(1));
 			if (reactionUpdateValid) {
 				formulaBar.setText(newValue);
 				/*
 				if (reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) != null) {
 					undoItem.setNewValue(reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN).toString());
 				}	
 				*/			
 				if (reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN) != null) {
 					undoItem.setEquationNames(reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.REACTION_EQUN_NAMES_COLUMN).toString());
 				} else {
 					undoItem.setEquationNames("");
 				}				
 				setUpReactionsUndo(undoItem);
 			} else {
 				formulaBar.setText(oldValue);
 			}
 			setReactionsFindLocationsList(locationList);
 			int count = LocalConfig.getInstance().getReactionsLocationsListCount();
 			if (!searchBackwards) {
 				if (LocalConfig.getInstance().getReactionsLocationsListCount() <= (getReactionsFindLocationsList().size() - 1)) {
 					// if value changed in cell, when list recreated, will need to move back
 					// since there will be one less value found. also if cell contains multiple
 					// instances of find string, need to keep counter from advancing.
 					if (reactionUpdateValid || oldValue.contains(findReplaceDialog.getFindText())) {
 						count -= 1;
 						LocalConfig.getInstance().setReactionsLocationsListCount(count);
 					} 
 				} else {
 					count = 0;
 					LocalConfig.getInstance().setReactionsLocationsListCount(count);
 				}
 			} else {
 				if (LocalConfig.getInstance().getReactionsLocationsListCount() > 0) {
 					if (reactionUpdateValid || oldValue.contains(findReplaceDialog.getFindText())) {
 						// seems to be working without adjusting counter(?)
 						//count += 1;
 						//LocalConfig.getInstance().setReactionsLocationsListCount(count);
 					}
 				} else {
 					if (locationList.size() > 1) {
 						count = locationList.size() - 1;
 						LocalConfig.getInstance().setReactionsLocationsListCount(count);
 					} else {
 						LocalConfig.getInstance().setReactionsLocationsListCount(0);
 					}
 				}
 			}
 
 		} else {
 			//TODO: Display an error message here in the unlikely event that there is an error
 			//System.out.println("String not found");
 		}
 		getFindReplaceDialog().requestFocus();
 	}
 
 	ActionListener replaceAllReactionsButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			if (tabbedPane.getSelectedIndex() == 0) {
 				// copy model so old model can be restored if replace all not valid
 				DefaultTableModel oldReactionsModel = copyReactionsTableModel((DefaultTableModel) reactionsTable.getModel());	
 				copyReactionsTableModels(oldReactionsModel);
 				ReactionUndoItem undoItem = createReactionUndoItem("", "", getReactionsFindLocationsList().get(0).get(0), getReactionsFindLocationsList().get(0).get(1), 1, UndoConstants.REPLACE_ALL, UndoConstants.REACTION_UNDO_ITEM_TYPE);
 				undoItem.setTableCopyIndex(LocalConfig.getInstance().getNumReactionTablesCopied());
 				replaceAllMode = true;
 				showErrorMessage = true;
 				Map<Object, ModelReactionEquation> reactionOldEquationMap = LocalConfig.getInstance().getReactionPasteEquationMap();
 				Map<Object, ModelReactionEquation> reactionNewEquationMap = LocalConfig.getInstance().getReactionPasteEquationMap();
 				for (int i = 0; i < getReactionsFindLocationsList().size(); i++) {
 					int viewRow = reactionsTable.convertRowIndexToModel(getReactionsFindLocationsList().get(i).get(0));
 					String oldValue = (String) reactionsTable.getModel().getValueAt(viewRow, getReactionsFindLocationsList().get(i).get(1));					
 					String replaceAllValue = "";
 					if (matchCase) {
 						replaceAllValue = oldValue.replaceAll(findReplaceDialog.getFindText(), findReplaceDialog.getReplaceText());
 					} else {
 						try {
 							replaceAllValue = oldValue.replaceAll("(?i)" + findReplaceDialog.getFindText(), findReplaceDialog.getReplaceText());
 						} catch (Throwable t){
 							// catches regex error when () or [] are not in pairs
 							validPaste = false;
 						}						
 					}
 					if (getReactionsFindLocationsList().get(i).get(1) == GraphicalInterfaceConstants.REACTION_EQUN_ABBR_COLUMN) {
 						int id = Integer.valueOf((String) reactionsTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.METABOLITE_ID_COLUMN));								
 						//System.out.println("id" + id);
 						reactionOldEquationMap.put(id, LocalConfig.getInstance().getReactionEquationMap().get(id));
 						//update newEquationMap when parser rewritten
 					}
 					if (isReactionsEntryValid(viewRow, getReactionsFindLocationsList().get(i).get(1), replaceAllValue)) {	
 						reactionsTable.setValueAt(replaceAllValue, viewRow, getReactionsFindLocationsList().get(i).get(1));	
 					} else {
 						validPaste = false;
 					}		
 				}
 				if (validPaste) {
 					DefaultTableModel newReactionsModel = copyReactionsTableModel((DefaultTableModel) reactionsTable.getModel());			
 					copyReactionsTableModels(newReactionsModel); 
 					setUpReactionsUndo(undoItem);
 					// add pasted reactions to equation map
 					/*
 					 * modify this code to update for new reactions when parser done
 					ArrayList<Object> keys = new ArrayList<Object>(LocalConfig.getInstance().getReactionPasteEquationMap().keySet());
 					for (int r = 0; r < keys.size(); r++) {
 						LocalConfig.getInstance().getReactionEquationMap().put(keys.get(r), LocalConfig.getInstance().getReactionPasteEquationMap().get(keys.get(r)));
 					}
 					System.out.println(LocalConfig.getInstance().getReactionEquationMap());
 					*/
 				} else {
 					if (showErrorMessage = true) {
 						setFindReplaceAlwaysOnTop(false);
 						JOptionPane.showMessageDialog(null,                
 								getReplaceAllError(),                
 								GraphicalInterfaceConstants.REPLACE_ALL_ERROR_TITLE,                                
 								JOptionPane.ERROR_MESSAGE);
 						setFindReplaceAlwaysOnTop(true);
 						//reactionsFindAll = false;
 						//reactionsTable.repaint();
 					}
 					// restore old model if invalid replace
 					setUpReactionsTable(oldReactionsModel);
 					LocalConfig.getInstance().getReactionsTableModelMap().put(LocalConfig.getInstance().getModelName(), oldReactionsModel);
 					// add pasted reactions to equation map
 					ArrayList<Object> keys = new ArrayList<Object>(reactionOldEquationMap.keySet());
 					for (int r = 0; r < keys.size(); r++) {
 						LocalConfig.getInstance().getReactionEquationMap().put(keys.get(r), reactionOldEquationMap.get(keys.get(r)));
 					}
 					//System.out.println(LocalConfig.getInstance().getReactionEquationMap());
 					deleteReactionsPasteUndoItem();
 					getFindReplaceDialog().replaceAllButton.setEnabled(true);
 					validPaste = true;
 				}
 			}
 
 			// these two lines remove highlighting from find all
 			ArrayList<ArrayList<Integer>> locationList = reactionsLocationsList();
 			setReactionsFindLocationsList(locationList);
 
 			// reset boolean values to default
 			//LocalConfig.getInstance().yesToAllButtonClicked = false;
 			replaceAllMode = false;
 			getFindReplaceDialog().requestFocus();
 		}
 	};
 
 	/***********************************************************************************/
 	// start metabolites find replace
 	/***********************************************************************************/
 
 	public int metabolitesFindStartIndex() {
 		// always start at 0 when in column or row selection mode
 		int startIndex = 0;
 		// start from selected cell
 		if (!searchSelectedArea) {
 			if (metabolitesTable.getSelectedRow() > -1 && metabolitesTable.getSelectedColumn() > -1) {
 				int row = metabolitesTable.getSelectedRow();
 				int col = metabolitesTable.getSelectedColumn();
 				startIndex = findStartIndex(row, col, getMetabolitesFindLocationsList());
 			}				
 		} else {
 			if (searchBackwards) {
 				startIndex = getMetabolitesFindLocationsList().size() - 1;
 			} 		
 		}
 		return startIndex;
 
 	}
 
 	ActionListener findMetabolitesButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			if (tabbedPane.getSelectedIndex() == 1) {
 				metabolitesFindAction();
 			}			
 			findButtonMetabolitesClicked = true;				
 		}
 	};
 
 	// temporarily removed see replaceFindReactionsButtonActionListener above
 	/*
 	ActionListener replaceFindMetabolitesButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			if (tabbedPane.getSelectedIndex() == 1) {
 				metabolitesFindAll = false;
 				metabolitesReplace();				
 				metabolitesFindNext();
 			}
 			// button works but even with this line of code, Find/Replace
 			// does not regain focus after updating table.
 			getFindReplaceDialog().requestFocus();
 		}
 	};
 	 */
 
 	public void metabolitesFindAction() {
 		metabolitesFindAll = false;
 		metabolitesTable.repaint();
 		ArrayList<ArrayList<Integer>> locationList = metabolitesLocationsList();
 		setMetabolitesFindLocationsList(locationList);
 		// uses window listener for focus event and row column change to reset button
 		// to not clicked if user changes selected cell.
 		// for first click of find button, find starts from first cell after (or before when backwards)
 		// that contains string, after this first click, find just iterates through list				
 		if (!findButtonMetabolitesClicked) {
 			if (metabolitesFindStartIndex() > -1) {
 				LocalConfig.getInstance().setMetabolitesLocationsListCount(metabolitesFindStartIndex());
 				metabolitesFindNext();
 			} else {
 				notFoundAction();
 				if (wrapAround) {
 					if (searchBackwards) {
 						LocalConfig.getInstance().setMetabolitesLocationsListCount(getMetabolitesFindLocationsList().size() - 1);
 					} else {
 						LocalConfig.getInstance().setMetabolitesLocationsListCount(0);
 					}
 				}
 			}					
 		} else {
 			metabolitesFindNext();
 		}
 	}
 
 	public void metabolitesFindNext() {
 		metabolitesTable.repaint();
 		ArrayList<ArrayList<Integer>> locationList = metabolitesLocationsList();
 		if (locationList.size() == 0) {
 			notFoundAction();
 			LocalConfig.getInstance().setMetabolitesLocationsListCount(0);
 		} else {
 			try {
 				// set focus to an invisible cell to give appearance that find cell is 
 				// only cell selected
 				metabolitesTable.changeSelection(locationList.get(LocalConfig.getInstance().getMetabolitesLocationsListCount()).get(0), 0, false, false);
 				changeMetaboliteFindSelection = false;
 				setMetabolitesReplaceLocation(locationList.get(LocalConfig.getInstance().getMetabolitesLocationsListCount()));
 				metabolitesTable.requestFocus();
 				metabolitesTable.scrollRectToVisible(metabolitesTable.getCellRect(locationList.get(LocalConfig.getInstance().getMetabolitesLocationsListCount()).get(0), locationList.get(LocalConfig.getInstance().getMetabolitesLocationsListCount()).get(1), false));
 				int viewRow = metabolitesTable.convertRowIndexToModel(metabolitesTable.getSelectedRow());
 				formulaBar.setText((String) metabolitesTable.getModel().getValueAt(viewRow, locationList.get(LocalConfig.getInstance().getMetabolitesLocationsListCount()).get(1)));
 				getFindReplaceDialog().requestFocus();
 				// if not at end of list increment, else start over
 				int count = LocalConfig.getInstance().getMetabolitesLocationsListCount();
 				if (!searchBackwards) {
 					if (LocalConfig.getInstance().getMetabolitesLocationsListCount() < (getMetabolitesFindLocationsList().size() - 1)) {
 						count += 1;
 						LocalConfig.getInstance().setMetabolitesLocationsListCount(count);
 					} else {
 						if (wrapAround) {							
 							count = 0;
 							LocalConfig.getInstance().setMetabolitesLocationsListCount(count);							
 						} else {							
 							if (throwNotFoundError) {															
 								notFoundAction();
 								throwNotFoundError = false;
 							}
 							throwNotFoundError = true;
 						}
 					}
 				} else {
 					if (LocalConfig.getInstance().getMetabolitesLocationsListCount() > 0) {
 						count -= 1;
 						LocalConfig.getInstance().setMetabolitesLocationsListCount(count);
 					} else {
 						if (wrapAround) {							
 							count = getMetabolitesFindLocationsList().size() - 1;
 							LocalConfig.getInstance().setMetabolitesLocationsListCount(count);							
 						} else {							
 							if (throwNotFoundError) {															
 								notFoundAction();
 								throwNotFoundError = false;
 							}
 							throwNotFoundError = true;
 						}
 					}
 				}						
 			} catch (Throwable t){
 				LocalConfig.getInstance().setMetabolitesLocationsListCount(LocalConfig.getInstance().getMetabolitesLocationsListCount());
 				try {
 					//metabolitesTable.changeSelection(locationList.get(LocalConfig.getInstance().getMetabolitesLocationsListCount()).get(0), locationList.get(LocalConfig.getInstance().getMetabolitesLocationsListCount()).get(1), false, false);
 					//metabolitesTable.requestFocus();
 				} catch (Throwable t1){
 
 				}				
 			}										
 		}			
 	}
 
 	ActionListener findAllMetabolitesButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {	
 			if (tabbedPane.getSelectedIndex() == 1) {
 				ArrayList<ArrayList<Integer>> locationList = metabolitesLocationsList();
 				setMetabolitesFindLocationsList(locationList);
 				if (locationList.size() == 0) {
 					notFoundAction();				
 				} else {
 					// set focus to first found item
 					metabolitesTable.changeSelection(locationList.get(0).get(0), locationList.get(0).get(1), false, false);
 					metabolitesTable.requestFocus();
 					// enables highlighter
 					metabolitesFindAll = true;	
 					getFindReplaceDialog().requestFocus();
 				}			
 			}		
 		}
 	};
 
 	public ArrayList<ArrayList<Integer>> metabolitesLocationsList() {		
 		int rowStartIndex = 0;
 		int rowEndIndex = metabolitesTable.getRowCount();
 		// start with 1 to avoid including hidden id column
 		int colStartIndex = 1;
 		int colEndIndex = metabolitesTable.getColumnCount();
 		if (searchSelectedArea) {
 			if (!findButtonMetabolitesClicked) {
 				int numcols=metabolitesTable.getSelectedColumnCount(); 
 				int numrows=metabolitesTable.getSelectedRowCount(); 
 				int[] rowsselected=metabolitesTable.getSelectedRows(); 
 				int[] colsselected=metabolitesTable.getSelectedColumns();
 				if (numrows > 0 && getSelectionMode() != 1) {
 					rowStartIndex = rowsselected[0];
 					rowEndIndex = rowsselected[0] + numrows;
 					selectedMetabolitesRowStartIndex = rowsselected[0];
 					selectedMetabolitesRowEndIndex = rowsselected[0] + numrows;
 				}			
 				if (numcols > 0 && getSelectionMode() != 2) {
 					colStartIndex = colsselected[0];
 					colEndIndex = colsselected[0] + numcols;
 					selectedMetabolitesColumnStartIndex = colsselected[0];
 					selectedMetabolitesColumnEndIndex = colsselected[0] + numcols;
 				}	
 			}					
 		} else {
 			selectedMetabolitesRowStartIndex = 0;
 			selectedMetabolitesRowEndIndex = metabolitesTable.getRowCount();
 			// start with 1 to avoid including hidden id column
 			selectedMetabolitesColumnStartIndex = 1;
 			selectedMetabolitesColumnEndIndex = metabolitesTable.getColumnCount();
 		}
 		ArrayList<ArrayList<Integer>> metabolitesLocationsList = new ArrayList<ArrayList<Integer>>();
 		for (int r = selectedMetabolitesRowStartIndex; r < selectedMetabolitesRowEndIndex; r++) {					
 			for (int c = selectedMetabolitesColumnStartIndex; c < selectedMetabolitesColumnEndIndex; c++) {					
 				int viewRow = metabolitesTable.convertRowIndexToModel(r);
 				if (metabolitesTable.getModel().getValueAt(viewRow, c) != null) {
 					String cellValue = (String) metabolitesTable.getModel().getValueAt(viewRow, c);
 					String findValue = findReplaceDialog.getFindText();
 					if (!matchCase) {
 						cellValue = cellValue.toLowerCase();
 						findValue = findValue.toLowerCase();						
 					}
 					if (cellValue.contains(findValue)) {
 						ArrayList<Integer> rowColumnList = new ArrayList<Integer>();
 						rowColumnList.add(r);
 						rowColumnList.add(c);
 						metabolitesLocationsList.add(rowColumnList);
 					}					
 				}
 			}
 		}
 		return metabolitesLocationsList;
 
 	}
 
 	ActionListener replaceMetabolitesButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			if (tabbedPane.getSelectedIndex() == 1) {
 				metabolitesReplace();
 			}
 		}
 	};
 
 	public void metabolitesReplace() {
 		int viewRow = metabolitesTable.convertRowIndexToModel(getMetabolitesReplaceLocation().get(0));
 		String oldValue = (String) metabolitesTable.getModel().getValueAt(viewRow, getMetabolitesReplaceLocation().get(1));
 		String newValue = replaceValue(oldValue, replaceLocation(oldValue));
 		MetaboliteUndoItem undoItem = createMetaboliteUndoItem(oldValue, newValue, metabolitesTable.getSelectedRow(), getMetabolitesReplaceLocation().get(1), viewRow + 1, UndoConstants.REPLACE, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);
 		setUndoOldCollections(undoItem);
 		if (replaceLocation(oldValue) > -1) {
 			//String newValue = replaceValue(oldValue, replaceLocation(oldValue));
 			metabolitesTable.getModel().setValueAt(newValue, viewRow, getMetabolitesReplaceLocation().get(1));
 			updateMetabolitesCellIfValid(oldValue, newValue, viewRow, getMetabolitesReplaceLocation().get(1));
 			if (metaboliteUpdateValid) {
 				formulaBar.setText(newValue);
 				setUndoNewCollections(undoItem);
 				setUpMetabolitesUndo(undoItem);
 			} else {
 				formulaBar.setText(oldValue);
 			}
 			setMetabolitesFindLocationsList(metabolitesLocationsList());
 			int count = LocalConfig.getInstance().getMetabolitesLocationsListCount();
 			if (!searchBackwards) {
 				if (LocalConfig.getInstance().getMetabolitesLocationsListCount() < (getMetabolitesFindLocationsList().size() - 1)) {
 					// if value changed in cell, when list recreated, will need to move back
 					// since there will be one less value found. also if cell contains multiple
 					// instances of find string, need to keep counter from advancing.
 					if (metaboliteUpdateValid || oldValue.contains(findReplaceDialog.getFindText())) {
 						count -= 1;
 						LocalConfig.getInstance().setMetabolitesLocationsListCount(count);
 					} 
 				} else {
 					count = 0;
 					LocalConfig.getInstance().setMetabolitesLocationsListCount(count);
 				}
 			} else {
 				if (LocalConfig.getInstance().getMetabolitesLocationsListCount() > 0) {
 					if (metaboliteUpdateValid || oldValue.contains(findReplaceDialog.getFindText())) {
 						// seems to be working without adjusting counter(?)
 						//count += 1;
 						//LocalConfig.getInstance().setMetabolitesLocationsListCount(count);
 					}
 				} else {
 					if (metabolitesLocationsList().size() > 1) {
 						count = metabolitesLocationsList().size() - 1;
 						LocalConfig.getInstance().setMetabolitesLocationsListCount(count);
 					} else {
 						LocalConfig.getInstance().setMetabolitesLocationsListCount(0);
 					}
 				}
 			}
 
 		} else {
 			//TODO: Display an error message here in the unlikely event that there is an error
 			//System.out.println("String not found");
 		}
 		getFindReplaceDialog().requestFocus();
 	}
 
 	ActionListener replaceAllMetabolitesButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			if (tabbedPane.getSelectedIndex() == 1) {
 				// copy model so old model can be restored if replace not valid
 				DefaultTableModel oldMetabolitesModel = copyMetabolitesTableModel((DefaultTableModel) metabolitesTable.getModel());
 				copyMetabolitesTableModels(oldMetabolitesModel); 
 				MetaboliteUndoItem undoItem = createMetaboliteUndoItem("", "", getMetabolitesFindLocationsList().get(0).get(0), getMetabolitesFindLocationsList().get(0).get(1), 1, UndoConstants.REPLACE_ALL, UndoConstants.METABOLITE_UNDO_ITEM_TYPE);
 				undoItem.setTableCopyIndex(LocalConfig.getInstance().getNumMetabolitesTableCopied());
 				setUndoOldCollections(undoItem);				
 				replaceAllMode = true;
 				showErrorMessage = true;
 				ArrayList<Integer> rowList = new ArrayList<Integer>();
 				ArrayList<Integer> metabIdList = new ArrayList<Integer>();
 				for (int i = 0; i < getMetabolitesFindLocationsList().size(); i++) {
 					int viewRow = metabolitesTable.convertRowIndexToModel(getMetabolitesFindLocationsList().get(i).get(0));
 					int id = Integer.valueOf((String) metabolitesTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.METABOLITE_ID_COLUMN));		
 					rowList.add(viewRow);
 					int metabId = Integer.valueOf((String) metabolitesTable.getModel().getValueAt(viewRow, 0));
 					metabIdList.add(metabId);
 				}
 				for (int i = 0; i < getMetabolitesFindLocationsList().size(); i++) {
 					int viewRow = metabolitesTable.convertRowIndexToModel(getMetabolitesFindLocationsList().get(i).get(0));
 					String metabAbbrev = (String) metabolitesTable.getModel().getValueAt(viewRow, GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN);
 					String oldValue = (String) metabolitesTable.getModel().getValueAt(viewRow, getMetabolitesFindLocationsList().get(i).get(1));
 					String replaceAllValue = "";
 					if (matchCase) {
 						replaceAllValue = oldValue.replaceAll(findReplaceDialog.getFindText(), findReplaceDialog.getReplaceText());
 					} else {
 						try {
 							replaceAllValue = oldValue.replaceAll("(?i)" + findReplaceDialog.getFindText(), findReplaceDialog.getReplaceText());
 						} catch (Throwable t){
 							// catches regex error when () or [] are not in pairs
 							validPaste = false;
 						}						
 					}
 					if (getMetabolitesFindLocationsList().get(i).get(1) == GraphicalInterfaceConstants.METABOLITE_ABBREVIATION_COLUMN) {
 						if (LocalConfig.getInstance().getMetaboliteUsedMap().containsKey(oldValue)) {
 							setReplaceAllError(GraphicalInterfaceConstants.REPLACE_ALL_PARTICIPATING_ERROR_MESSAGE);
 							//updater.updateMetaboliteRows(rowList, metabIdList, LocalConfig.getInstance().getLoadedDatabase());
 							metabolitesTable.getModel().setValueAt(oldValue, viewRow, getMetabolitesFindLocationsList().get(i).get(1));
 							validPaste = false;
 						} else {
 						    metabolitesTable.getModel().setValueAt(replaceAllValue, viewRow, getMetabolitesFindLocationsList().get(i).get(1));							
 						}
 					} else if (getMetabolitesFindLocationsList().get(i).get(1) == GraphicalInterfaceConstants.CHARGE_COLUMN) {
 						replaceAllValue = oldValue.replaceAll(findReplaceDialog.getFindText(), findReplaceDialog.getReplaceText());
 						if (replaceAllValue != null && replaceAllValue.trim().length() > 0) {
 							validPaste = true;
 							EntryValidator validator = new EntryValidator();
 							if (!validator.isNumber(replaceAllValue)) {
 								setReplaceAllError("Number format exception");
 						    	validPaste = false;
 						    	metabolitesTable.getModel().setValueAt(oldValue, viewRow, getMetabolitesFindLocationsList().get(i).get(1));	
 							}
 						} else {
 							metabolitesTable.getModel().setValueAt("", viewRow, getMetabolitesFindLocationsList().get(i).get(1));
 						}
 						if (validPaste) {
 							metabolitesTable.getModel().setValueAt(replaceAllValue, viewRow, getMetabolitesFindLocationsList().get(i).get(1));
 						}						
 					} else {
 						if (isMetabolitesEntryValid(getMetabolitesFindLocationsList().get(i).get(1), replaceAllValue)) {
 							metabolitesTable.setValueAt(replaceAllValue, viewRow, getMetabolitesFindLocationsList().get(i).get(1));			
 						} else {
 							validPaste = false;
 						}	
 					}			
 				}
 				if (validPaste) {						
 					DefaultTableModel newMetabolitesModel = copyMetabolitesTableModel((DefaultTableModel) metabolitesTable.getModel());			
 					copyMetabolitesTableModels(newMetabolitesModel); 
 					setUndoNewCollections(undoItem);
 					setUpMetabolitesUndo(undoItem);					
 				} else {
 					if (showErrorMessage = true) {
 						setFindReplaceAlwaysOnTop(false);
 						JOptionPane.showMessageDialog(null,                
 								getReplaceAllError(),                
 								GraphicalInterfaceConstants.REPLACE_ALL_ERROR_TITLE,                                
 								JOptionPane.ERROR_MESSAGE);
 						setFindReplaceAlwaysOnTop(true);
 					}
 					setUpMetabolitesTable(oldMetabolitesModel);
 					LocalConfig.getInstance().getMetabolitesTableModelMap().put(LocalConfig.getInstance().getModelName(), oldMetabolitesModel);
 					deleteMetabolitesPasteUndoItem();
 					getFindReplaceDialog().replaceAllButton.setEnabled(true);
 					validPaste = true;
 				}				
 			}
 
 			// these two lines remove highlighting from find all
 			ArrayList<ArrayList<Integer>> locationList = metabolitesLocationsList();
 			setMetabolitesFindLocationsList(locationList);
 
 			// reset boolean values to default
 			LocalConfig.getInstance().yesToAllButtonClicked = false;
 			replaceAllMode = false;
 			getFindReplaceDialog().requestFocus();
 		}
 	};
 
 	public Integer replaceLocation(String oldValue) {		
 		// start index of find text in cell value
 		int replaceLocation = 0;
 		if (matchCase) {
 			replaceLocation = oldValue.indexOf(findReplaceDialog.getFindText());
 		} else {
 			replaceLocation = oldValue.toLowerCase().indexOf(findReplaceDialog.getFindText().toLowerCase());
 		}
 
 		return replaceLocation;
 
 	}
 
 	public String replaceValue(String oldValue, int replaceLocation) {
 		String replaceValue = "";
 		int endIndex = replaceLocation + findReplaceDialog.getFindText().length();
 		String replaceEnd = "";
 		if (endIndex != oldValue.length()) {
 			replaceEnd = oldValue.substring(endIndex);
 		}
 		try {
 			replaceValue = oldValue.substring(0, replaceLocation) + findReplaceDialog.getReplaceText() + replaceEnd;
 		} catch (Throwable t) {
 			getFindReplaceDialog().replaceButton.setEnabled(false);
 			//getFindReplaceDialog().replaceFindButton.setEnabled(false);
 			return "";
 		}
 
 		return replaceValue;
 	}
 
 	ActionListener matchCaseActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent actionEvent) {
 			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
 			matchCase = abstractButton.getModel().isSelected();
 		}
 	};
 
 	ActionListener wrapAroundActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent actionEvent) {
 			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
 			wrapAround = abstractButton.getModel().isSelected();
 		}
 	};
 
 	ActionListener selectedAreaActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent actionEvent) {
 			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
 			searchSelectedArea = abstractButton.getModel().isSelected();
 		}
 	};
 
 	ActionListener searchBackwardsActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent actionEvent) {
 			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
 			searchBackwards = abstractButton.getModel().isSelected();
 			if (tabbedPane.getSelectedIndex() == 0) {
 				int count = LocalConfig.getInstance().getReactionsLocationsListCount();
 				if (!searchBackwards && count > 0 && count < getReactionsFindLocationsList().size() - 1) {
 					count += 1;
 					LocalConfig.getInstance().setReactionsLocationsListCount(count);
 				} else if (searchBackwards && count > 0 && count < getReactionsFindLocationsList().size() - 1) {
 					count -= 1;
 					LocalConfig.getInstance().setReactionsLocationsListCount(count);
 				}
 			} else if (tabbedPane.getSelectedIndex() == 1) {
 				int count = LocalConfig.getInstance().getMetabolitesLocationsListCount();
 				if (!searchBackwards && count > 0 && count < getMetabolitesFindLocationsList().size() - 1) {
 					count += 1;
 					LocalConfig.getInstance().setMetabolitesLocationsListCount(count);
 				} else if (searchBackwards && count > 0 && count < getMetabolitesFindLocationsList().size() - 1) {
 					count -= 1;
 					LocalConfig.getInstance().setMetabolitesLocationsListCount(count);
 				}
 			}
 			// only change cell if selected cell has been changed
 			if (!findButtonReactionsClicked) {
 				if (tabbedPane.getSelectedIndex() == 0) {
 					if (searchBackwards) {	
 						ArrayList<ArrayList<Integer>> locationList = reactionsLocationsList();
 						setReactionsFindLocationsList(locationList);
 						if (!findButtonReactionsClicked && getReactionsFindLocationsList().size() > 1) {
 							LocalConfig.getInstance().setReactionsLocationsListCount(getReactionsFindLocationsList().size() - 1);
 						}
 					} else {
 						LocalConfig.getInstance().setReactionsLocationsListCount(0);
 					}
 				} else if (tabbedPane.getSelectedIndex() == 1) {
 					if (searchBackwards) {	
 						ArrayList<ArrayList<Integer>> locationList = metabolitesLocationsList();
 						setMetabolitesFindLocationsList(locationList);
 						if (getMetabolitesFindLocationsList().size() > 1) {
 							LocalConfig.getInstance().setMetabolitesLocationsListCount(getMetabolitesFindLocationsList().size() - 1);
 						}
 					} else {
 						LocalConfig.getInstance().setMetabolitesLocationsListCount(0);
 					}
 				}
 			} 
 		}
 	};
 
 	ActionListener findDoneButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {	
 			findReplaceCloseAction();
 		}
 	};
 
 	public void findReplaceCloseAction() {
 		findMode = false;
 		reactionsFindAll = false;
 		metabolitesFindAll = false;	
 		reactionsTable.repaint();
 		metabolitesTable.repaint();	
 		findReplaceItem.setEnabled(true);
 		findbutton.setEnabled(true);
 	}
 
 	// This avoids conflicts with error messages being on top of component
 	public void setFindReplaceAlwaysOnTop(boolean state) {
 		if (getFindReplaceDialog() != null) {
 			getFindReplaceDialog().setAlwaysOnTop(state);
 		}
 	}
 	/*******************************************************************************/
 	// end reactions find replace
 
 	/*******************************************************************************/
 	// end find/replace methods
 	/******************************************************************************/
 
 	public void deleteAllOptimizationFiles() {
 		Utilities u = new Utilities();
 		//TODO: if "_orig" db exists rename to db w/out "_orig", delete db w/out "_orig"
 		// or delete db
 		if (LocalConfig.getInstance().getOptimizationFilesList().size() > 0) {
 			for (int i = 0; i < LocalConfig.getInstance().getOptimizationFilesList().size(); i++) {
 				// TODO: determine where and how to display these messages, and actually delete these files
 				//System.out.println(LocalConfig.getInstance().getOptimizationFilesList().get(i) + ".db will be deleted.");
 				File f = new File(LocalConfig.getInstance().getOptimizationFilesList().get(i) + ".log");
 				if (f.exists()) {
 					u.delete(LocalConfig.getInstance().getOptimizationFilesList().get(i) + ".log");
 				}						
 			}					
 		}				
 		LocalConfig.getInstance().getOptimizationFilesList().clear(); 
 	}
 
 	public void createUnusedMetabolitesList() {
 		Map<String, Object> idMap = LocalConfig.getInstance().getMetaboliteNameIdMap();
 
 		ArrayList<String> usedList = new ArrayList<String>(LocalConfig.getInstance().getMetaboliteUsedMap().keySet());
 		ArrayList<String> idList = new ArrayList<String>(LocalConfig.getInstance().getMetaboliteNameIdMap().keySet());
 		ArrayList<Integer> unusedList = new ArrayList<Integer>();
 		// removes unused metabolites from idMap and populates list of
 		// unused metabolite id's for deletion from table
 		for (int i = 0; i < idList.size(); i++) {						
 			if (!usedList.contains(idList.get(i))) {
 				int id = (Integer) idMap.get(idList.get(i));
 				unusedList.add(id); 
 			}
 		}
 		LocalConfig.getInstance().setUnusedList(unusedList);
 	}
 
 	public void deleteItemFromDynamicTree() {
 		Utilities u = new Utilities();
 		Object[] options = {"    Yes    ", "    No    ",};
 		int choice = JOptionPane.showOptionDialog(null, 
 				GraphicalInterfaceConstants.DELETE_ASSOCIATED_FILES, 
 				GraphicalInterfaceConstants.DELETE_ASSOCIATED_FILES_TITLE, 
 				JOptionPane.YES_NO_OPTION, 
 				JOptionPane.QUESTION_MESSAGE, 
 				null, options, options[0]);
 		if (choice == JOptionPane.YES_OPTION) {//here
 			/*
 			System.out.println("database name: "
 					+ LocalConfig.getInstance()
 							.getOptimizationFilesList()
 							.get(DynamicTree.getRow() - 1) + ".db");
 			 */
 			u.delete(LocalConfig.getInstance().getOptimizationFilesList().get(DynamicTree.getRow() - 1) + ".db");
 			File f = new File(LocalConfig.getInstance().getOptimizationFilesList().get(DynamicTree.getRow() - 1) + ".log");
 			if (f.exists()) {
 				u.delete(LocalConfig.getInstance().getOptimizationFilesList().get(DynamicTree.getRow() - 1) + ".log");
 			}
 			// TODO: Determine why MIP Files do not usually delete. (???)
 			File f1 = new File(LocalConfig.getInstance().getOptimizationFilesList().get(DynamicTree.getRow() - 1).substring(4) + "_MIP.log");						
 			if (f1.exists()) {
 				u.delete(LocalConfig.getInstance().getOptimizationFilesList().get(DynamicTree.getRow() - 1).substring(4) + "_MIP.log");						
 			}
 		}
 		if (choice == JOptionPane.NO_OPTION) {
 
 		}
 		// TODO; reload tables
 		setTitle(GraphicalInterfaceConstants.TITLE + " - " + LocalConfig.getInstance().getModelName());
 		clearOutputPane();
 	}
 
 	public void deleteAllItemsFromDynamicTree() {
 		Object[] options = {"    Yes    ", "    No    ",};
 		int choice = JOptionPane.showOptionDialog(null, 
 				GraphicalInterfaceConstants.DELETE_ASSOCIATED_FILES, 
 				GraphicalInterfaceConstants.DELETE_ASSOCIATED_FILES_TITLE, 
 				JOptionPane.YES_NO_OPTION, 
 				JOptionPane.QUESTION_MESSAGE, 
 				null, options, options[0]);
 		if (choice == JOptionPane.YES_OPTION) {				
 			deleteAllOptimizationFiles();
 		}
 		if (choice == JOptionPane.NO_OPTION) {
 
 		}
 		// TODO: reload tables here	
 	}
 
 	// enables menu items when main file is selected in analysis pane
 	public void enableMenuItems() {
 		saveSBMLItem.setEnabled(true);
 		saveCSVMetabolitesItem.setEnabled(true);
 		saveCSVReactionsItem.setEnabled(true);
 		saveSQLiteItem.setEnabled(true);
 		clearItem.setEnabled(true);
 		if (hasGurobiPath) {
 			fbaItem.setEnabled(true);
 			gdbbItem.setEnabled(true);
 		}		
 		addReacRowItem.setEnabled(true);
 		addMetabRowItem.setEnabled(true);
 		addReacColumnItem.setEnabled(true);
 		addMetabColumnItem.setEnabled(true);
 		reactionsTableEditable = true;
 		saveOptFile = false;
 		formulaBar.setEditable(true);		
 		editorMenu.setEnabled(true);
 		pastebutton.setEnabled(true);
 		if (undoCount > 1) {
 			enableOptionComponent(undoSplitButton, undoLabel, undoGrayedLabel);
 			undoItem.setEnabled(true);
 		}
 		if (LocalConfig.getInstance().getRedoItemMap().size() > 0) {
 			enableOptionComponent(redoSplitButton, redoLabel, redoGrayedLabel);
 			redoItem.setEnabled(true);
 		}
 	}
 
 	// disables menu items when optimization is selected in analysis pane
 	public void disableMenuItems() {
 		saveSBMLItem.setEnabled(false);
 		saveCSVMetabolitesItem.setEnabled(false);
 		saveCSVReactionsItem.setEnabled(false);
 		clearItem.setEnabled(false);
 		saveSQLiteItem.setEnabled(false);
 		fbaItem.setEnabled(false);
 		gdbbItem.setEnabled(false);
 		addReacRowItem.setEnabled(false);
 		addMetabRowItem.setEnabled(false);
 		addReacColumnItem.setEnabled(false);
 		addMetabColumnItem.setEnabled(false);
 		reactionsTableEditable = false;
 		tabbedPane.setSelectedIndex(0);
 		formulaBar.setEditable(false);
 		formulaBar.setBackground(Color.WHITE);
 		editorMenu.setEnabled(false);
 		getFindReplaceDialog().replaceButton.setEnabled(false);
 		getFindReplaceDialog().replaceAllButton.setEnabled(false);
 		//getFindReplaceDialog().replaceFindButton.setEnabled(false);
 		pastebutton.setEnabled(false);
 		disableOptionComponent(undoSplitButton, undoLabel, undoGrayedLabel);
 		disableOptionComponent(redoSplitButton, redoLabel, redoGrayedLabel);
 		undoItem.setEnabled(false);
 		redoItem.setEnabled(false);
 	}
 
 	public void addReactionColumnCloseAction() {
 		LocalConfig.getInstance().addColumnInterfaceVisible = false;
     	addReacColumnItem.setEnabled(true);
     	addMetabColumnItem.setEnabled(true);
     	getReactionColAddRenameInterface().textField.setText("");
     	getReactionColAddRenameInterface().setVisible(false);
     	getReactionColAddRenameInterface().dispose();
 	}
 
 	public void addMetaboliteColumnCloseAction() {
 		LocalConfig.getInstance().addColumnInterfaceVisible = false;
     	addReacColumnItem.setEnabled(true);
     	addMetabColumnItem.setEnabled(true);
     	getMetaboliteColAddRenameInterface().textField.setText("");
     	getMetaboliteColAddRenameInterface().setVisible(false);
     	getMetaboliteColAddRenameInterface().dispose();
 	}
 
 	/*******************************************************************************/
 	//progressBar methods
 	/*******************************************************************************/
 
 	class Task extends SwingWorker<Void, Void> {
 
 		@Override
 		public void done() {
 
 		}
 
 		@Override
 		protected Void doInBackground() throws Exception {
 			int progress = 0;
 			SBMLDocument doc = new SBMLDocument();
 			SBMLReader reader = new SBMLReader();
 			try {		  
 				doc = reader.readSBML(getSBMLFile());
 				SBMLModelReader modelReader = new SBMLModelReader(doc);
 				modelReader.load();
 			} catch (FileNotFoundException e) {				
 				JOptionPane.showMessageDialog(null,                
 						"File does not exist.",                
 						"File does not exist.",                                
 						JOptionPane.ERROR_MESSAGE);					
 				//e.printStackTrace();					
 				progress = 100;
 				progressBar.setVisible(false);
 				modelCollectionOKButtonClicked = false;
 			} catch (XMLStreamException e) {
 				JOptionPane.showMessageDialog(null,                
 						"This File is not a Valid SBML File.",                
 						"Invalid SBML File.",                                
 						JOptionPane.ERROR_MESSAGE);
 				// TODO Auto-generated catch block
 				//e.printStackTrace();
 				progress = 100;
 				progressBar.setVisible(false);
 				modelCollectionOKButtonClicked = false;
 			}	
 			while (progress < 100) {
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException ignore) {
 				}			
 			}				
 			timer.stop();
 			return null;
 		}
 	}
 
 	class GDBBTask extends SwingWorker<Void, Solution> {
 		private GDBB gdbb;
 		private GDBBModel model;
 		private ReactionFactory rFactory;
 		private Vector<String> uniqueGeneAssociations;
 		private int knockoutOffset;
 		private Writer writer;
 		private StringBuffer outputText;
 		//		private MemoryDatabaseCopier copier;
 		private ArrayList<Double> soln;
 		private String dateTimeStamp;
 		private String optimizePath;
 
 		// Since synthetic objective column exists, no need for dropbox. But methods not changed in case,
 		// sometime in future it is desired to choose columns, just uncomment second line, and comment first
 		// line, and uncomment code commented out in TextInputDemo, also uncomment textInput.setObjectiveColumnNames...
 		// in GDBB action
 		GDBBTask() {
 			model = new GDBBModel(textInput.getReactionNameDBColumnMapping().get((String)textInput.getColumnList().getSelectedItem()));
 		}
 
 		@Override
 		protected Void doInBackground() {
 			/*
         	copier = new DatabaseCopier();
         	String oldOptimizaePath = getDatabaseName();
 			oldOptimizaePath = getOptimizePath();	// to copy from memory database
 			 */
 
 			rFactory = new ReactionFactory("SBML");
 			uniqueGeneAssociations = rFactory.getUniqueGeneAssociations();
 
 			outputText = new StringBuffer();
 
 			//			log.debug("create an optimize");
 			gdbb = new GDBB();
 			GDBB.intermediateSolution.clear();
 
 			gdbb.setGDBBModel(model);
 			gdbb.start();
 
 			knockoutOffset = 4*model.getNumReactions() + model.getNumMetabolites();
 
 			soln = new ArrayList<Double>();
 			Format formatter;
 			formatter = new SimpleDateFormat("_yyMMdd_HHmmss");
 			Solution solution;
 
 			while (gdbb.isAlive() || GDBB.intermediateSolution.size() > 0) {
 				if (GDBB.intermediateSolution.size() > 0) {
 					dateTimeStamp = formatter.format(new Date());
 					optimizePath = GraphicalInterfaceConstants.GDBB_PREFIX + LocalConfig.getInstance().getModelName() + dateTimeStamp;
 					/*
 					copier.copyDatabase(getDatabaseName(), optimizePath);
 					listModel.addElement(GraphicalInterfaceConstants.GDBB_PREFIX
 							+ (getDatabaseName().substring(getDatabaseName().lastIndexOf("\\") + 1) + dateTimeStamp));				
 					LocalConfig.getInstance().getOptimizationFilesList().add(optimizePath);
 //					setOptimizePath(optimizePath);
 					 */
 
 					// need to lock if process is busy
 					solution = GDBB.intermediateSolution.poll();
 					solution.setDatabaseName(optimizePath);
 					publish(solution);
 				}
 			}
 			return null;
 		}
 
 		public GDBB getGdbb() {
 			return gdbb;
 		}
 
 		public GDBBModel getModel() {
 			return model;
 		}
 
 		public void setModel(GDBBModel model) {
 			this.model = model;
 		}
 
 		@Override
 		protected void process(List<Solution> solutions) {
 			Solution solution = solutions.get(solutions.size() - 1);
 			double[] x = solution.getKnockoutVector();
 			double objectiveValue = solution.getObjectiveValue();
 
 			String kString = "";
 			soln.clear();
 			for (int j = 0; j < x.length; j++) {
 				soln.add(x[j]);
 				if ((j >= knockoutOffset) && (x[j] >= 0.5)) {	// compiler optimizes: boolean short circuiting
 					kString += "\n\t" + uniqueGeneAssociations.elementAt(j - knockoutOffset);
 				}
 			}
 
 			rFactory = new ReactionFactory("SBML");
 			rFactory.setFluxes(new ArrayList<Double>(soln.subList(0, model.getNumReactions())));
 			rFactory.setKnockouts(soln.subList(knockoutOffset, soln.size()));
 
 			DynamicTreePanel.treePanel.addObject((DefaultMutableTreeNode)DynamicTreePanel.treePanel.getRootNode().getChildAt(DynamicTreePanel.treePanel.getRootNode().getChildCount() - 1), solution, true);
 			GraphicalInterface.outputTextArea.append("\n\n" + model.getNumMetabolites() + " metabolites, " + model.getNumReactions() + " reactions, " + model.getNumGeneAssociations() + " unique gene associations\n" + "Maximum synthetic objective: " + objectiveValue + "\nKnockouts:" + kString);
 			DynamicTreePanel.treePanel.setNodeSelected(DynamicTreePanel.treePanel.getRootNode().getChildCount() - 1);
 		}
 
 		@Override
 		protected void done() {
 			//        	System.out.println("GDBB is done!");
 			soln = gdbb.getSolution();
 
 			log.debug("optimization complete");
 
 			textInput.enableStart();
 			textInput.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
 			writer = null;
 			if (optimizePath.contains(ConfigConstants.DEFAULT_DATABASE_NAME)) {
 				setrFactory(new ReactionFactory("SBML"));
 				getrFactory().setFluxes(new ArrayList<Double>(soln.subList(0, model.getNumReactions())));
 				getrFactory().setKnockouts(soln.subList(knockoutOffset, soln.size()));
 
 				System.out.println("optimizePath = " + optimizePath);
 				writer = null;
 				try {
 					//					outputText.append(getDatabaseName() + "\n");
 					outputText.append(model.getNumMetabolites() + " metabolites, " + model.getNumReactions() + " reactions, " + model.getNumGeneAssociations() + " unique gene associations\n");
 					outputText.append("Maximum synthetic objective: "	+ gdbb.getMaxObj() + "\n");
 					outputText.append("knockouts: \n");
 
 					File file = new File(optimizePath + ".log");
 					writer = new BufferedWriter(new FileWriter(file));
 					writer.write(outputText.toString());			
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					try {
 						if (writer != null) {
 							writer.close();
 						}
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 				//loadOutputPane(getOptimizePath() + ".log");
 			}
 			//			loadOutputPane(getOptimizePath() + ".log");
 			if (getPopout() != null) {
 				//getPopout().load(getOptimizePath() + ".log");
 			}				
 
 			//reloadTables(getOptimizePath());
 
 			DynamicTreePanel.treePanel.getTree().setSelectionPath(DynamicTreePanel.treePanel.getTree().getPathForRow(DynamicTreePanel.treePanel.getTree().getRowCount() - 1));
 		}
 
 		public ReactionFactory getrFactory() {
 			return rFactory;
 		}
 
 		public void setrFactory(ReactionFactory rFactory) {
 			this.rFactory = rFactory;
 		}
 	}
 
 	class TimeListener implements ActionListener {
 		public void actionPerformed(ActionEvent ae) {
 			if (LocalConfig.getInstance().getProgress() > 0) {
 				progressBar.progress.setIndeterminate(false);
 			}			
 			progressBar.progress.setValue(LocalConfig.getInstance().getProgress());
 			progressBar.progress.repaint();
 			if (LocalConfig.getInstance().getProgress() == 100) {
 				setUpReactionsTable(SBMLModelReader.getReactionsTableModel());
 				LocalConfig.getInstance().getReactionsTableModelMap().put(LocalConfig.getInstance().getModelName(), SBMLModelReader.getReactionsTableModel());
 				setUpMetabolitesTable(SBMLModelReader.getMetabolitesTableModel());
 				LocalConfig.getInstance().getMetabolitesTableModelMap().put(LocalConfig.getInstance().getModelName(), SBMLModelReader.getMetabolitesTableModel());	
 				setUpTables();
 				modelCollectionOKButtonClicked = false;
 				DynamicTreePanel.treePanel.clear();
 				DynamicTreePanel.treePanel.addObject(new Solution(GraphicalInterface.listModel.get(GraphicalInterface.listModel.getSize() - 1), GraphicalInterface.listModel.get(GraphicalInterface.listModel.getSize() - 1)));
 				DynamicTreePanel.treePanel.setNodeSelected(GraphicalInterface.listModel.getSize() - 1);
 				progressBar.setVisible(false);		
 				timer.stop();
 				// This appears redundant, but is the only way to not have an extra progress bar on screen
 				progressBar.setVisible(false);
 				progressBar.progress.setIndeterminate(true);
 
 			}
 		}
 	}
 
 	/*******************************************************************************/
 	//end progressBar methods
 	/*******************************************************************************/
 
 	/******************************************************************************/
 	// Gurobi path methods
 	/******************************************************************************/
 
 	public static void loadGurobiPathInterface() {
 		GurobiPathInterface gpi = new GurobiPathInterface();
 		setGurobiPathInterface(gpi);
 		gpi.setIconImages(icons);					
 		gpi.setSize(600, 150);
 		gpi.setResizable(false);
 		gpi.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		gpi.setLocationRelativeTo(null);		
 		gpi.setAlwaysOnTop(true);	
 		gpi.setModal(true);
 		gpi.textField.setText(findGurobiPath());
 		if (gurobiPathFound) {
 			gpi.topLabel.setText(GraphicalInterfaceConstants.GUROBI_JAR_PATH_FOUND_LABEL);
 			setGurobiPath(findGurobiPath());
 		} else {
 			gpi.topLabel.setText(GraphicalInterfaceConstants.GUROBI_JAR_PATH_NOT_FOUND_LABEL);
 		}
 		gpi.fileButton.addActionListener(fileButtonActionListener);
 		gpi.okButton.addActionListener(gpiOKActionListener);
 		gpi.cancelButton.addActionListener(gpiCancelActionListener);
 		gpi.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent evt) {
 				gpiCloseAction();	        	
 			}
 		});	
 		gpi.setVisible(true);
 		gurobiPathErrorShown = false;
 		openGurobiPathFilechooser = true;
 		gurobiFileChooserShown = false; 
 	}
 
 	static ActionListener fileButtonActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			getGurobiPathInterface().setAlwaysOnTop(false);
 			JTextArea output = null;
 			JFileChooser fileChooser = new JFileChooser(); 
 			fileChooser.setDialogTitle(GraphicalInterfaceConstants.GUROBI_JAR_PATH_FILE_CHOOSER_TITLE);
 			// no matter what is tried this file filter will not work (?????)
 			//			fileChooser.setFileFilter(new SBMLFileFilter());
 			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);							
 			fileChooser.setCurrentDirectory(new File("C:\\"));
 			if (openGurobiPathFilechooser && !gurobiFileChooserShown) {
 				//... Open a file dialog.
 				int retval = fileChooser.showOpenDialog(output);
 				if (retval == JFileChooser.APPROVE_OPTION) {
 					//... The user selected a file, get it, use it.          	
 					File file = fileChooser.getSelectedFile();
 					String rawPathName = file.getAbsolutePath();
 					getGurobiPathInterface().textField.setText(rawPathName);
 					if (!rawPathName.endsWith(".jar")) {
 						getGurobiPathInterface().setAlwaysOnTop(false);
 						JOptionPane.showMessageDialog(null,                
 								"Not a Valid Jar File.",                
 								"Invalid Jar File",                                
 								JOptionPane.ERROR_MESSAGE);
 						getGurobiPathInterface().setAlwaysOnTop(true);
 					} else {
 						setGurobiPath(rawPathName);
 						gurobiPathSelected = true;
 					}					
 					gurobiFileChooserShown = true;
 				}
 			}
 			openGurobiPathFilechooser = true;
 			gurobiFileChooserShown = false;
 			getGurobiPathInterface().setAlwaysOnTop(true);
 		}
 	};
 
 	static ActionListener gpiOKActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			String lastGurobi_path = curSettings.get("LastGurobi");
 			if (lastGurobi_path == null) {
 				lastGurobi_path = ".";
 			}
 			setGurobiPath(getGurobiPathInterface().textField.getText());
 			String path = getGurobiPath();
 			System.out.println(path);
 			curSettings.add("LastGurobi", path);
 			hasGurobiPath = true;
 			gurobiPathSelected = false;
 			fbaItem.setEnabled(true);
 			gdbbItem.setEnabled(true);
 			getGurobiPathInterface().setVisible(false);
 			getGurobiPathInterface().dispose();
 		}
 	};
 
 	static ActionListener gpiCancelActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			gpiCloseAction();
 		}
 	};
 
 	public static void gpiCloseAction() {
 		getGurobiPathInterface().setAlwaysOnTop(false);
 		String lastGurobi_path = curSettings.get("LastGurobi");
 		if (!gurobiPathErrorShown && lastGurobi_path == null) {
 			JOptionPane.showMessageDialog(null,                
 					GraphicalInterfaceConstants.NO_GUROBI_JAR_PATH_ERROR,                
 					GraphicalInterfaceConstants.NO_GUROBI_JAR_PATH_ERROR_TITLE,                                
 					JOptionPane.ERROR_MESSAGE);	
 		}
 		gurobiPathErrorShown = true;
 		hasGurobiPath = false;	
 		getGurobiPathInterface().setVisible(false);
 		getGurobiPathInterface().dispose();
 	}
 
 	public static String findGurobiPath() {
 		String gurobiPath = "";
 		gurobiPathFound = false;
 
 		String variable = System.getenv("GUROBI_HOME");  
 		if (variable != null) {
 			gurobiPath = variable;
 			gurobiPathFound = true;
 		}
 
 		if (gurobiPathFound) {
 			gurobiPath = gurobiPath + "\\lib\\gurobi.jar";
			//File f = new File(gurobiPath);
 			// uncomment to test Gurobi not found
			File f = new File("none");
 			if (f.exists()) {
 				gurobiPathFound = true;
 			} else {
 				gurobiPathFound = false;
 			}
 		}
 		return gurobiPath;
 	}
 
 	/******************************************************************************/
 	// end Gurobi path methods
 	/******************************************************************************/
 
 	public static void main(String[] args) throws Exception {
 		curSettings = new SettingsFactory();
 
 		//based on code from http://stackoverflow.com/questions/6403821/how-to-add-an-image-to-a-jframe-title-bar
 		final ArrayList<Image> icons = new ArrayList<Image>(); 
 		icons.add(new ImageIcon("etc/most16.jpg").getImage()); 
 		icons.add(new ImageIcon("etc/most32.jpg").getImage());
 
 		gurobiPathFound = false;
 		hasGurobiPath = true;
 		String lastGurobi_path = curSettings.get("LastGurobi");
 		if (lastGurobi_path == null) {
 			openGurobiPathFilechooser = true;
 			loadGurobiPathInterface();
 		} else {
 			setGurobiPath(lastGurobi_path);
 		}
 
 		GraphicalInterface frame = new GraphicalInterface();
 		frame.setIconImages(icons);
 		frame.setSize(1000, 600);
 		frame.setMinimumSize(new Dimension(800, 600));
 		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);		
 	}
 }
 
 
