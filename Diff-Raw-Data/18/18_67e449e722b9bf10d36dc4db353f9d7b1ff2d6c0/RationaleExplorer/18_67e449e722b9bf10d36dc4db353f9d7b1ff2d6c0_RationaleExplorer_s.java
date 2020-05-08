 package edu.wpi.cs.jburge.SEURAT.views;
 
 import java.awt.Frame;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.*;
 import java.util.zip.ZipException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.part.*;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.action.*;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.dialogs.PopupDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.ui.*;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.swt.SWT;
 import org.eclipse.jdt.core.*;
 
 import edu.wpi.cs.jburge.SEURAT.SEURATPlugin;
 import edu.wpi.cs.jburge.SEURAT.actions.*;
 import edu.wpi.cs.jburge.SEURAT.editors.SelectCandidate;
 import edu.wpi.cs.jburge.SEURAT.editors.SelectItem;
 import edu.wpi.cs.jburge.SEURAT.editors.SelectRationaleElement;
 import edu.wpi.cs.jburge.SEURAT.editors.SelectRationaleElementForArgAndDec;
 import edu.wpi.cs.jburge.SEURAT.editors.SelectRationaleElement_Treeview;
 import edu.wpi.cs.jburge.SEURAT.editors.SelectType;
 import edu.wpi.cs.jburge.SEURAT.inference.*;
 //import edu.wpi.cs.jburge.SEURAT.queries.EditEntity;
 import edu.wpi.cs.jburge.SEURAT.queries.FindImportanceOverrides;
 import edu.wpi.cs.jburge.SEURAT.queries.FindStatusOverrides;
 import edu.wpi.cs.jburge.SEURAT.queries.FindCommonArguments;
 import edu.wpi.cs.jburge.SEURAT.queries.FindEntity;
 import edu.wpi.cs.jburge.SEURAT.queries.FindRequirements;
 import edu.wpi.cs.jburge.SEURAT.queries.HistoryDisplay;
 import edu.wpi.cs.jburge.SEURAT.queries.RequirementRelationshipDisplay;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.*;
 import edu.wpi.cs.jburge.SEURAT.reports.*;
 import edu.wpi.cs.jburge.SEURAT.tasks.RationaleTaskList;
 
 import org.eclipse.core.resources.IFile;
 import edu.wpi.cs.jburge.SEURAT.SEURATResourcePropertiesManager;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jface.action.Action;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.ChildrenIterator;
 
 import edu.wpi.cs.jburge.SEURAT.rationaleData.Argument;
 import SEURAT.editors.*;
 import SEURAT.preferences.PreferenceConstants;
 import SEURAT.xmlIO.RationaleEntry;
 import edu.wpi.cs.jburge.SEURAT.views.TreeParent;
 import edu.wpi.cs.jburge.SEURAT.decorators.*;
 
  /**
   * The Rationale Explorer is the primary display and access mechanism for the rationale.
   * It is essentially a tree view with different icons specifying the different types of 
   * rationale elements and their status.
   * <p>
   * The view uses a label provider to define how model
   * objects should be presented in the view. 
   * <p>
   */
 
 public class RationaleExplorer extends ViewPart implements ISelectionListener, IRationaleUpdateEventListener,
 	IPropertyChangeListener{
 	
 	/**
 	 * The view into the tree of rationale
 	 */
 	private TreeViewer viewer;
 	/**
 	 * Provides the icons that make up our tree
 	 */
 	protected RationaleLabelProvider labelProvider;
 	/**
 	 * ?
 	 */
 	private DrillDownAdapter drillDownAdapter;
 	
 	/**
 	 * the associate action is used to associate code with rationale
 	 */
 	private AssociateArtifactAction associate;
 	
 	/**
 	 * Menu item to restore associations - this is from the query menu
 	 */
 	private Action restoreAssociations;
 	/**
 	 * Find overrides option from the query menu - used to find status overrides
 	 */
 	private Action findOverrides;
 	/**
 	 * Query menu option to find importance overrides
 	 */
 	private Action findImportanceOverrides;
 	//	private Action updateDec;
 	/**
 	 * Menu item to edit an element
 	 */
 	private Action editElement;
 	/**
 	 * Menu item to add a new element
 	 */
 	private Action addElement;
 	/**
 	 * Menu item to add a new area of expertise
 	 */
 	private Action addExpertise;
 	/**
 	 * Menu item to delete an element
 	 */
 	private Action deleteElement;
 	/**
 	 * Menu item to find requirement relationships
 	 */
 	private Action findRelationships;
 	/**
 	 * Menu item to display an element's history
 	 */
 	private Action showHistory;
 	/**
 	 * Menu item to associate an ontology entry with a constraint
 	 */
 	private Action associateOntology;
 	/**
 	 * Menu item to associate a constraint with a decision or alternative
 	 */
 	private Action associateConstraint;
 	/**
 	 * Menu item to add a new alternative-constraint relationship
 	 */
 	private Action addAltConstRel;
 	/**
 	 * Menu item to change to a different database
 	 */
 	private Action changeDatabase;
 	/**
 	 * Menu item to create a new set of rationale
 	 */
 	private Action newRationale;
 	/**
 	 * Menu item to input rationale from an XML file
 	 */
 	private Action inputRationale;
 	/**
 	 * Menu item to import XFeature model as rationale
 	 */
 	private Action importXFeature;
 	/**
 	 * Menu item to export rationale as an XFeature application model
 	 */
 	private Action exportXFeature;
 	/**
 	 * Menu item to import TEI requirements and create arguments (not for delivery!)
 	 */
 	private Action importTEIRequirements;
 	/**
 	 * Menu item to look for entities of a particular type
 	 */
 	private Action findEntity;
 	/**
 	 * Menu item to find common arguments
 	 */
 	private Action findCommon;
 	/**
 	 * Menu item to find requirements with a specific status
 	 */
 	private Action findRequirement;
 	/**
 	 * Menu item to create a requirements traceability matrix report
 	 */
 	private Action requirementsTraceabilityMatrix;	
 	/**
 	 * Menu item to show the graphcal rationale
 	 */
 	private Action showGraphicalRationale;
 	/**
 	 * Menu item to generate a rationale subtree report
 	 */
 	private Action generateRatReportFromHere;
 	/**
 	 * Menu item to generate a complete rationale report
 	 */
 	private Action generateRatReport;
 	/**
 	 * Menu item to save the argument ontology to XML
 	 */
 	private Action exportOntology;
 	/**
 	 * Points to our display
 	 */
 	private Display ourDisplay;
 	
 //	protected TreeParent invisibleRoot;
 	
 	/**
 	 * The Java Element selected
 	 */
 	private IJavaElement navigatorSelection;
 	/**
 	 * The rationale object selected in the tree view (used in associations)
 	 */
 	private Object obj;
 	/**
 	 * The index for the first character of the artifact we're looking for.  This determines
 	 * where we will place the marker once we find it.
 	 */
 	private int cstart;
 	/**
 	 * The Java resource
 	 */
 	private IResource ourRes;
 	
 	/**
 	 * The name of the alternative involved in an association
 	 */
 	private String alternativeName;
 	
 	private Action showRequirementEditor;
 	private Action addRequirementEditor;
 	private Action addAlternativeEditor;
 	private Action showAlternativeEditor;
 	private Action addDecisionEditor;
 	private Action showDecisionEditor;
 	//private Action addAssumptionEditor;
 	//private Action showAssumptionEditor;
 	private Action addArgumentEditor;
 	private Action showArgumentEditor;
 	private Action addTradeoffEditor;
 	private Action showTradeoffEditor;
 	private Action addQuestionEditor;
 	private Action showQuestionEditor;
 	
 	/**
 	 * Menu item to move an element
 	 */
 	private Action moveElement;
 	
 	private Action moveArgElementUnderReq;
 	
 	private Action moveArgElementUnderAlt;
 	
 	private Action moveDecElementUnderDec;
 	
 	private Action moveDecElementUnderAlt;
 	
 	
 	/**
 	 * Menu item to adopt an element and its children
 	 */
 //	private Action changeElementType;
 	
 	
 	class NameSorter extends ViewerSorter {
 	}
 	
 	
 	
 	/**
 	 * The constructor.
 	 */
 	public RationaleExplorer() {
 		
 	}
 	
 	/**
 	 * This is a callback that will allow us
 	 * to create the viewer and initialize it.
 	 */
 	public void createPartControl(Composite parent) {
 		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		drillDownAdapter = new DrillDownAdapter(viewer);
 		viewer.setContentProvider(new RationaleViewContentProvider());
 		labelProvider = new RationaleLabelProvider();
 		viewer.setLabelProvider(labelProvider);
 		viewer.setSorter(null);
 		viewer.setInput(ResourcesPlugin.getWorkspace());
 		
 		//get our display information so we can use it later
 		ourDisplay = viewer.getControl().getDisplay();
 		
 		//initialize our update manager
 		UpdateManager mgr = UpdateManager.getHandle();
 		mgr.setTree(viewer);
 		makeActions();
 		hookContextMenu();
 		hookDoubleClickAction();
 		contributeToActionBars();
 		
 		viewer.setInput(((RationaleViewContentProvider) viewer.getContentProvider()).initialize());
 		viewer.expandToLevel(2);
 
 		// get associations
 		restoreAssociations.run();
 		
 		//add an action listener for the workbench window
 		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
 		
 		//add an action listener for this so we can get events
 		SEURATPlugin plugin = SEURATPlugin.getDefault();
 		plugin.addUpdateListener(this);
 		
 		// add this as a property change listener so we can be notified of preference changes
 		plugin.getPreferenceStore().addPropertyChangeListener(this);
 		
 		//get the initial selected value
 		selectionChanged(null, getViewSite().getWorkbenchWindow().getSelectionService().getSelection());
 	
 	}
 	
 	/**
 	 * Set up our context menu
 	 *
 	 */
 	private void hookContextMenu() {
 		MenuManager menuMgr = new MenuManager("#PopupMenu");
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager manager) {
 				RationaleExplorer.this.fillContextMenu(manager);
 			}
 		});
 		Menu menu = menuMgr.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		getSite().registerContextMenu(menuMgr, viewer);
 	}
 	
 	/**
 	 * Set up our action bars - toolbar, pull down
 	 *
 	 */
 	private void contributeToActionBars() {
 		IActionBars bars = getViewSite().getActionBars();
 		fillLocalPullDown(bars.getMenuManager());
 		fillLocalToolBar(bars.getToolBarManager());
 	}
 	
 	/**
 	 * This method is used to populate the pulldown menu on this view.
 	 * @param manager
 	 */
 	private void fillLocalPullDown(IMenuManager manager) {
 		//manager.add(findEntity);
 		//manager.add(findCommon);
 		//manager.add(findRequirement);
 		//manager.add(new Separator());
 		//manager.add(findOverrides);
 		//manager.add(findImportanceOverrides);
 		manager.add(changeDatabase);
 		manager.add(newRationale);
 		//restoreAssociations exists to re-set the rationale from a database
 		//this would not be used operationally and should be removed.
 		manager.add(restoreAssociations);
 		manager.add(inputRationale);
 		manager.add(new Separator());
 		manager.add(requirementsTraceabilityMatrix);
 		manager.add(showGraphicalRationale);
 		manager.add(new Separator());
 		manager.add(importXFeature);
 		manager.add(exportXFeature);
 		manager.add(importTEIRequirements);
 //		manager.add(testAction);
 	}
 	
 	/**
 	 * This method is used to populate the context menus for each of the different
 	 * types of rationale elements.
 	 * 
 	 * @param manager
 	 */
 	private void fillContextMenu(IMenuManager manager) {
 		ISelection selection = viewer.getSelection();
 		Object obj = ((IStructuredSelection)selection).getFirstElement();
 		
 //		Object curObj = ((IStructuredSelection)curSel).getFirstElement();
 		
 		if (obj instanceof TreeParent)
 		{
 			TreeParent ourElement = (TreeParent) obj;
 
 			if (ourElement.getType() == RationaleElementType.ALTERNATIVE)
 			{
 				manager.add(editElement);
 				manager.add(moveElement);
 				manager.add(deleteElement);
 				manager.add(associate);
 				manager.add(addQuestionEditor);
 				manager.add(addArgumentEditor);
 				manager.add(addDecisionEditor);
 				//manager.add(addQuestion);
 				//manager.add(addArgument);
 				//manager.add(addDecision);
 //				manager.add(addAltConstRel);
 				manager.add(showHistory);
 //				manager.add(changeElementType);
 				
 			}
 			else if (ourElement.getType() == RationaleElementType.REQUIREMENT)
 			{
 				manager.add(editElement);
 				manager.add(moveElement);
 				manager.add(deleteElement);
 //				manager.add(addQuestion);
 				manager.add(addArgumentEditor);
 				manager.add(addRequirementEditor);
 				//manager.add(addArgument);
 				manager.add(findRelationships);
 				manager.add(showHistory);
 		
 			}
 			else if (ourElement.getType() == RationaleElementType.CLAIM)
 			{
 				manager.add(editElement);
 //				manager.add(changeElementType);
 			}
 			else if (ourElement.getType() == RationaleElementType.DECISION)
 			{
 				Decision ourDec = (Decision) RationaleDB.getRationaleElement(ourElement.getName(), RationaleElementType.DECISION);
 				manager.add(editElement);
 				manager.add(moveDecElementUnderDec);
 				manager.add(moveDecElementUnderAlt);
 				manager.add(deleteElement);
 				//decisions can have alternatives or sub-decisions as children
 				if (ourDec.getAlts()) {
 					manager.add(addAlternativeEditor);
 					//manager.add(addAlternative);
 				}					
 				else {
 					manager.add(addDecisionEditor);
 					//manager.add(addDecision);
 				}
 				manager.add(addQuestionEditor);
 				//manager.add(addQuestion);
 				// manager.add(associateConstraint);
 				manager.add(showHistory);
 				
 			}
 			else if (ourElement.getType() == RationaleElementType.ASSUMPTION)
 			{
 				manager.add(editElement);
 				manager.add(moveElement);
 //				manager.add(changeElementType);
 			}
 			else if (ourElement.getType() == RationaleElementType.EXPERTISE)
 			{
 				manager.add(editElement);
 //				manager.add(changeElementType);
 			}
 			else if (ourElement.getType() == RationaleElementType.ARGUMENT)
 			{
 				manager.add(editElement);
 				manager.add(deleteElement);
 				manager.add(addArgumentEditor);
 				manager.add(moveArgElementUnderReq);
 				manager.add(moveArgElementUnderAlt);
 				
 //				manager.add(changeElementType);
 				//manager.add(addArgument);
 //				manager.add(addQuestion);
 			}
 			else if (ourElement.getType() == RationaleElementType.ONTENTRY)
 			{
 				if (ourElement.getName().compareTo("Argument-Ontology") == 0) {
 					manager.add(exportOntology);
 				}
 				manager.add(editElement);
 				manager.add(addElement);
 			}
 			else if (ourElement.getType() == RationaleElementType.DESIGNPRODUCTENTRY)
 			{
 				manager.add(editElement);
 				manager.add(addElement);
 			}
 			else if (ourElement.getType() == RationaleElementType.CONSTRAINT)
 			{
 				manager.add(editElement);
 				manager.add(addElement);
 				manager.add(associateOntology);
 			}
 			else if (ourElement.getType() == RationaleElementType.CONTINGENCY)
 			{
 				manager.add(editElement);
 				manager.add(addElement);
 			}
 			else if (ourElement.getType() == RationaleElementType.DESIGNER)
 			{
 				manager.add(editElement);
 				manager.add(addElement);
 				manager.add(addExpertise);
 			}	
 			else if (ourElement.getType() == RationaleElementType.EXPERTISE)
 			{
 				manager.add(editElement);
 			}
 			else if (ourElement.getType() == RationaleElementType.QUESTION)
 			{
 				manager.add(editElement);
 				manager.add(moveElement);
 				manager.add(deleteElement);
 				manager.add(showHistory);
 				
 //				manager.add(changeElementType);
 			}
 			else if (ourElement.getType() == RationaleElementType.ALTCONSTREL)
 			{
 				manager.add(editElement);
 			}
 			else if (ourElement.getType() == RationaleElementType.RATIONALE)
 			{
 				if (ourElement.getName().compareTo("Tradeoffs") == 0)
 				{
 					manager.add(addTradeoffEditor);
 					//manager.add(addElement);
 				}
 				else if (ourElement.getName().compareTo("Co-occurrences") == 0)
 				{
 					manager.add(addTradeoffEditor);
 					//manager.add(addElement);
 				}
 				else if (ourElement.getName().compareTo("Requirements") == 0)
 				{
 					manager.add(addRequirementEditor);
 					//manager.add(addElement);
 				}
 				else if (ourElement.getName().compareTo("Decisions") == 0)
 				{
 					manager.add(addDecisionEditor);
 					//manager.add(addElement);
 				}
 				else if (ourElement.getName().compareTo("Design-Contingencies") == 0)
 				{
 					manager.add(addElement);
 				}
 				else if (ourElement.getName().compareTo("Designer-Profiles") == 0)
 				{
 					manager.add(addElement);
 				}
 			}
 			else if (ourElement.getType() == RationaleElementType.TRADEOFF)
 			{
 				manager.add(editElement);
 				manager.add(deleteElement);
 			}
 			else if (ourElement.getType() == RationaleElementType.COOCCURRENCE)
 			{
 				manager.add(editElement);
 				manager.add(deleteElement);
 			}
 			
 		}
 		manager.add(new Separator());
 		manager.add(generateRatReportFromHere);
 		
 		// Other plug-ins can contribute there actions here
 		manager.add(new Separator("Additions"));
 	}
 	
 	private void fillLocalToolBar(IToolBarManager manager) {
 		drillDownAdapter.addNavigationActions(manager);
 	}
 
 	/**
 	 * This method sets up the actions invoked by chosing menu items
 	 *
 	 */
 	private void makeActions() {		
 		/* SEURAT ECLIPSE-STYLE EDITORS ACTION DEFINITIONS */
 
 		// Currently unused editors
 		
 		// Assumption editor removed for consistency reasons (pop-up boxes to select assumption, then
 		// a tab to edit it? would be confusing to users
 		/*addAssumptionEditor = new OpenRationaleEditorAction(AssumptionEditor.class, this, true);
 		addAssumptionEditor.setText("Create New Assumption (With Editor)");
 		addAssumptionEditor.setToolTipText("Create A New Assumption Using The Assumption Editor Window");
 		
 		showAssumptionEditor = new OpenRationaleEditorAction(AssumptionEditor.class, this, false);
 		showAssumptionEditor.setText("Assumption Editor Page");
 		showAssumptionEditor.setToolTipText("Edit An Assumption In An Editor Window");*/
 		
 		// Editors that are used
 		
 		addRequirementEditor = new OpenRationaleEditorAction(RequirementEditor.class, this, true);
 		addRequirementEditor.setText("New Requirement");
 		addRequirementEditor.setToolTipText("Add Requirement");
 		showRequirementEditor = new OpenRationaleEditorAction(RequirementEditor.class, this, false);
 		
 		addAlternativeEditor = new OpenRationaleEditorAction(AlternativeEditor.class, this, true, RationaleElementType.ALTERNATIVE);
 		addAlternativeEditor.setText("New Alternative");
 		addAlternativeEditor.setToolTipText("Add Alternative");
 		showAlternativeEditor = new OpenRationaleEditorAction(AlternativeEditor.class, this, false);
 		
 		addArgumentEditor = new OpenRationaleEditorAction(ArgumentEditor.class, this, true, RationaleElementType.ARGUMENT);
 		addArgumentEditor.setText("New Argument");
 		addArgumentEditor.setToolTipText("Add Argument");
 		showArgumentEditor = new OpenRationaleEditorAction(ArgumentEditor.class, this, false);
 		
 		addDecisionEditor = new OpenRationaleEditorAction(DecisionEditor.class, this, true, RationaleElementType.DECISION);
 		addDecisionEditor.setText("New Decision");
 		addDecisionEditor.setToolTipText("Add Decision");
 		showDecisionEditor = new OpenRationaleEditorAction(DecisionEditor.class, this, false);
 		
 		addTradeoffEditor = new OpenRationaleEditorAction(TradeoffEditor.class, this, true);
 		addTradeoffEditor.setText("New");
 		addTradeoffEditor.setToolTipText("Add Element");
 		showTradeoffEditor = new OpenRationaleEditorAction(TradeoffEditor.class, this, false);
 		
 		addQuestionEditor = new OpenRationaleEditorAction(QuestionEditor.class, this, true, RationaleElementType.QUESTION);
 		addQuestionEditor.setText("New Question");
 		addQuestionEditor.setToolTipText("Add Question");
 		showQuestionEditor = new OpenRationaleEditorAction(QuestionEditor.class, this, false);
 		
 		//Views that open as editors
 		
 		
 		
 		//
 		//associate action - used to associate rationale with a java element
 		//
 		associate = new AssociateArtifactAction(viewer);
 		restoreAssociations = new RestoreAssociations(viewer);
 		
 		//change database
 		changeDatabase = new Action () {
 			public void run() {
 				rebuildTree(false);
 			}
 		};
 		changeDatabase.setText("Change Rationale DB");
 		changeDatabase.setToolTipText("Changes the display to show the database set in the preferences.");
 		
 		//new Rationale
 		newRationale = new Action() {
 			public void run() {
 				Frame rf = new Frame();
 				String dbName;
 
 				InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),
 				"New Rationale Database", "Enter the new database name",
 				null, null);
 
 				if (dlg.open() == Window.OK) {
 				dbName = dlg.getValue();
 
 				RationaleDB.createNewDB(dbName);
 				rebuildTree(false);
 				}
 				
 			}
 		};
 		newRationale.setText("Create New Rationale DB");
 		newRationale.setToolTipText("Creates a new set of rationale");
 		
 		//
 		// input Rationale
 		//
 		inputRationale = new Action() {
 			public void run() {
 				RationaleEntry re;
 				try {
 					re = new RationaleEntry();
 					
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 			}
 		}; //end of the action definition		
 		inputRationale.setText("Input Rationale");
 		inputRationale.setToolTipText("Input Rationale from XML Files");
 		
 		//
 		// import XFeature information
 		importXFeature = new Action() {
 			public void run() {
 
 				FileDialog fd = new FileDialog(viewer.getControl().getShell(), SWT.OPEN);
 				fd.setText("Select");
 				fd.setFilterPath(".");
 				String[] filterExt = { "*.xfm", "*.*" };
 				fd.setFilterExtensions(filterExt);
 				String xfmFile=fd.open();
 
 				
 				//Now, get the name for our new database
 				String dbName;
 
 				InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),
 				"New Rationale Database", "Enter the new database name",
 				null, null);
 
 				if (dlg.open() == Window.OK) {
 				dbName = dlg.getValue();
 
 				RationaleDB.createNewDB(dbName);
 				
 				ImportXFeatureFile(xfmFile);
 				}
 
 			}
 		}; //end of the action definition		
 		importXFeature.setText("Import XFeature");
 		importXFeature.setToolTipText("Input Rationale from an XFeature file");
 		
 		//
 		// import XFeature information
 		exportXFeature = new Action() {
 			public void run() {
 
 				FileDialog fd = new FileDialog(viewer.getControl().getShell(), SWT.OPEN);
 				fd.setText("Select");
 				fd.setFilterPath(".");
 				String[] filterExt = { "*.xfm", "*.*" };
 				fd.setFilterExtensions(filterExt);
 				String xfmFile=fd.open();
 				
 				ExportXFeatureFile(xfmFile);
 
 			}
 		}; //end of the action definition		
 		exportXFeature.setText("Export XFeature");
 		exportXFeature.setToolTipText("Convert rationale to an XFeature application model");
 		
 		//
 		// import XFeature information
 		importTEIRequirements = new Action() {
 			public void run() {
 
 				FileDialog fd = new FileDialog(viewer.getControl().getShell(), SWT.OPEN);
 				fd.setText("Select");
 				fd.setFilterPath(".");
 				String[] filterExt = { "*.xml", "*.*" };
 				fd.setFilterExtensions(filterExt);
 				String xmlFile=fd.open();
 				
 				ImportTEIReqs(xmlFile);
 
 			}
 		}; //end of the action definition		
 		importTEIRequirements.setText("Export TEI Requirements");
 		importTEIRequirements.setToolTipText("Read in TEI Requirements and create matching arguments");
 		
 		//
 		// find Overrides
 		//
 		findOverrides = new Action() {
 			public void run() {
 				FindStatusOverrides ar = new FindStatusOverrides(viewer.getControl().getShell().getDisplay());
 				Vector<RationaleStatus> newStatus = ar.getUpdatedStatus();
 				if (newStatus != null)
 				{
 					//update tasks too
 					RationaleTaskList tlist = RationaleTaskList.getHandle();
 					tlist.addTasks(newStatus);
 					//need to update the rationale tree also
 					//need to update our tree item!
 					UpdateManager mgr = UpdateManager.getHandle();
 					Iterator statusI = newStatus.iterator();
 					//				RationaleDB db = RationaleDB.getHandle();
 					while (statusI.hasNext())
 					{
 						RationaleStatus stat = (RationaleStatus) statusI.next();
 						RationaleElement ourEle = RationaleDB.getRationaleElement(stat.getParent(), stat.getRationaleType());				
 						mgr.addUpdate(stat.getParent(), ourEle.getName(), stat.getRationaleType());					
 					}
 					mgr.makeTreeUpdates();
 				}
 			}
 		}; //end of the action definition		
 		findOverrides.setText("Find Status Overrides");
 		findOverrides.setToolTipText("Displays status items that were overridden");
 		
 		generateRatReportFromHere = new Action(){
 			public void run(){
 				Object selection = viewer.getSelection();
 				GenerateRationaleReport reportGen = new GenerateRationaleReport(viewer.getControl().getShell().getDisplay(), selection);
 			}
 		};
 		generateRatReportFromHere.setText("Generate Rationale Report From Here");
 		generateRatReportFromHere.setToolTipText("Generates a rationale report from this node down.");
 		
 		generateRatReport = new Action(){
 			public void run(){
 				Object selection = viewer.getSelection();
 				GenerateRationaleReport reportGen = new GenerateRationaleReport(viewer.getControl().getShell().getDisplay(), selection);
 			}
 		};
 		generateRatReport.setText("Generate Rationale Report");
 		generateRatReport.setToolTipText("Generates a complete rationale report");
 		
 		//Search for an entity of a particular type 
 		findEntity = new Action() {
 			public void run() {
 				@SuppressWarnings("unused") FindEntity entityF = new FindEntity(viewer.getControl().getShell().getDisplay());
 				
 			}
 		}; //end of the findEntity action definition		
 		findEntity.setText("Find Rationale Entity");
 		findEntity.setToolTipText("Finds an entity of a specific type");
 		
 		
 		//find Common arguments action
 		findCommon = new Action() {
 			public void run() {
 				@SuppressWarnings("unused") FindCommonArguments entityF = new FindCommonArguments(viewer.getControl().getShell().getDisplay());
 				
 			}
 		}; //end of the find Common definition
 		findCommon.setText("Find Common Arguments");
 		findCommon.setToolTipText("Finds common arguments");
 		
 		//finds requirements - can specify a desired status
 		findRequirement = new Action() {
 			public void run() {
 				@SuppressWarnings("unused") FindRequirements entityF = new FindRequirements(viewer.getControl().getShell().getDisplay());
 				
 			}
 		}; //end of the find requirement action definition	
 		findRequirement.setText("Find Requirements");
 		findRequirement.setToolTipText("Finds requirements by status");
 		
 		//Requirements traceability matrix report capability
 		requirementsTraceabilityMatrix = new Action() {
 				public void run() {
 					@SuppressWarnings("unused") TraceabilityMatrixDisplay entityF = 
 						new TraceabilityMatrixDisplay(viewer.getControl().getShell());
 				}
 		}; //end of action definition
 		requirementsTraceabilityMatrix.setText("Generate Traceability Matrix");
 		requirementsTraceabilityMatrix.setToolTipText("Generates a requirements traceability matrix");
 		
 		//Graphical Rationale
 		//showGraphicalRationale = new OpenRationaleEditorAction(GraphicalRationale.class, this, true);
 		showGraphicalRationale = new Action() {
 			public void run() {
 				@SuppressWarnings("unused") GraphicalRationalePop graphrat = 
 					new GraphicalRationalePop(viewer.getControl().getShell());
 			}
 		}; 
 		showGraphicalRationale.setText("Show Graphical Rationale");
 		showGraphicalRationale.setToolTipText("Shows the rationale tree graphically");
 		
 		//Show history is used to show a tabular display of the change history for an element
 		showHistory = new Action() {
 			public void run() {
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				if (obj instanceof TreeParent)
 				{
 					@SuppressWarnings("unused") HistoryDisplay entityF = new HistoryDisplay(viewer.getControl().getShell(), ((TreeParent) obj).getName(), ((TreeParent) obj).getType() );
 				}
 				
 			}
 		}; //end find History action definition	
 		showHistory.setText("Show History");
 		showHistory.setToolTipText("Show Status History");
 		
 		//
 		// find Importance overrides - looks for values other than default
 		//
 		findImportanceOverrides = new Action() {
 			public void run() {
 				@SuppressWarnings("unused") FindImportanceOverrides ar = new FindImportanceOverrides(viewer
 						.getControl().getShell().getDisplay());
 				
 			}
 		}; // end importance override action definition		
 		findImportanceOverrides.setText("Find Importance Overrides");
 		findImportanceOverrides
 		.setToolTipText("Displays items not using the default importance");
 		
 		//
 		//edit rationale element Action
 		//For the new editors, will get the rationale type and run the appropriate editor.
 		//
 		editElement = new Action() {
 			public void run() {
 				
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				if (obj instanceof TreeParent)
 				{
 					TreeParent ourElement = (TreeParent) obj;
 
 					if (ourElement.getType() == RationaleElementType.REQUIREMENT) {
 						showRequirementEditor.run();
 					} else if (ourElement.getType() == RationaleElementType.DECISION) {
 						showDecisionEditor.run();
 					} else if (ourElement.getType() == RationaleElementType.ALTERNATIVE) {
 						showAlternativeEditor.run();
 					} else if (ourElement.getType() == RationaleElementType.ARGUMENT) {
 						showArgumentEditor.run();
 					} else if (ourElement.getType() == RationaleElementType.QUESTION) {
 						showQuestionEditor.run();
 					} else if (ourElement.getType() == RationaleElementType.TRADEOFF) {
 						showTradeoffEditor.run();
 					} else if (ourElement.getType() == RationaleElementType.COOCCURRENCE) {
 						showTradeoffEditor.run();
 					} else {
 						try {
 							RationaleElement rElement = getElement((TreeParent) obj, false);
 							editElement((TreeParent) obj, rElement, ourDisplay);
 						} catch (NullPointerException npe) {
 							/* NPE is thrown when we double-click on an invalid element,
 							 such as the root of the tree.  In this case we don't want
 							 to do anything, no error pop-up, just leave it with no effect.
 							 Could change it to expand the tree if possible. */
 						}
 					}
 				}
 			}
 			
 		}; //end of the edit element action definition
 		editElement.setText("Edit");
 		editElement.setToolTipText("Edit Rationale");
 		
 		//
 		//add element Action
 		//
 		addElement = new Action() {
 			public void run() {
 				
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				if (obj instanceof TreeParent)
 				{
 					RationaleElement parentElement = getElement((TreeParent) obj, false);
 					RationaleElement rElement = getElement((TreeParent) obj, true);
 					createNewElement(parentElement, rElement, (TreeParent) obj);
 				}			
 			}			
 		}; //end add element action definition
 		addElement.setText("New");
 		addElement.setToolTipText("Add Rationale");
 		
 		//
 		//Move Element
 		//
 		moveElement= new Action() {
 			public void run() {
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				TreeParent parent;
 				TreeParent beingMovedEle;
 //				sel= SelectRationaleElement();
 				if (obj instanceof TreeParent)
 				{
 					RationaleElement rElement = getElement((TreeParent) obj, false);
 					parent = moveElement((TreeParent) obj, rElement, ourDisplay);
 //					refreshBranch(parent);
 //					beingMovedEle = ((RationaleViewContentProvider) viewer.getContentProvider()).findRationaleElement(((TreeParent)obj).getName());
 //					refreshBranch(beingMovedEle);
 				}
 				rebuildTree(false);
 				viewer.expandToLevel(4);
 			}			
 		}; //end add element action definition
 		moveElement.setText("Move");
 		moveElement.setToolTipText("Add Rationale");
 		
 		moveArgElementUnderReq= new Action() {
 			public void run() {
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				TreeParent parent;
 				TreeParent beingMovedEle;
 //				sel= SelectRationaleElement();
 				if (obj instanceof TreeParent)
 				{
 					RationaleElement rElement = getElement((TreeParent) obj, false);
 					parent = moveArgElementUnderReq((TreeParent) obj, rElement, ourDisplay);
 
 					
 				}
 				rebuildTree(false);
 				viewer.expandToLevel(4);
 			}
 		}; //end add element action definition
 		moveArgElementUnderReq.setText("Move Element Under Requirement");
 		moveArgElementUnderReq.setToolTipText("Add Rationale");
 		
 		moveArgElementUnderAlt= new Action() {
 			public void run() {
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				TreeParent parent;
 				TreeParent beingMovedEle;
 //				sel= SelectRationaleElement();
 				if (obj instanceof TreeParent)
 				{
 					RationaleElement rElement = getElement((TreeParent) obj, false);
 					parent = moveArgElementUnderAlt((TreeParent) obj, rElement, ourDisplay);
 //					refreshBranch(parent);
 					beingMovedEle = ((RationaleViewContentProvider) viewer.getContentProvider()).findRationaleElement(((TreeParent)obj).getName());
 					refreshBranch(beingMovedEle);
 				}
 				rebuildTree(false);
 				viewer.expandToLevel(4);
 			}			
 		}; //end add element action definition
 		moveArgElementUnderAlt.setText("Move Element Under Alternative");
 		moveArgElementUnderAlt.setToolTipText("Add Rationale");
 		
 		moveDecElementUnderDec= new Action() {
 			public void run() {
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				TreeParent parent;
 				TreeParent beingMovedEle;
 //				sel= SelectRationaleElement();
 				if (obj instanceof TreeParent)
 				{
 					RationaleElement rElement = getElement((TreeParent) obj, false);
 					parent = moveDecElementUnderDecision((TreeParent) obj, rElement, ourDisplay);
 //					refreshBranch(parent);
 //					beingMovedEle = ((RationaleViewContentProvider) viewer.getContentProvider()).findRationaleElement(((TreeParent)obj).getName());
 //					refreshBranch(beingMovedEle);
 				}
 				rebuildTree(false);
 				viewer.expandToLevel(4);
 			}			
 		}; 
 		moveDecElementUnderDec.setText("Move Element Under Decision");
 		moveDecElementUnderDec.setToolTipText("Add Rationale");
 		
 		moveDecElementUnderAlt= new Action() {
 			public void run() {
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				TreeParent parent;
 				TreeParent beingMovedEle;
 //				sel= SelectRationaleElement();
 				if (obj instanceof TreeParent)
 				{
 					RationaleElement rElement = getElement((TreeParent) obj, false);
 					parent = moveDecElementUnderAlt((TreeParent) obj, rElement, ourDisplay);
 //					refreshBranch(parent);
 //					beingMovedEle = ((RationaleViewContentProvider) viewer.getContentProvider()).findRationaleElement(((TreeParent)obj).getName());
 //					refreshBranch(beingMovedEle);
 				}
 				rebuildTree(false);
 				viewer.expandToLevel(4);
 			}			
 		}; 
 		moveDecElementUnderAlt.setText("Move Element Under Alternative");
 		moveDecElementUnderAlt.setToolTipText("Add Rationale");
 		
 		//
 		//Change Element Type
 		//
 /*		changeElementType= new Action() {
 			public void run() {
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				//We need to display a list of new parent elements and select which one we want
 				RationaleElement rElement = getElement((CandidateTreeParent) obj, false);
 				SelectType eleType = new SelectType(ourDisplay);
 				String type=eleType.getType();
 				String typeSelected=(((CandidateTreeParent) obj).getType()).toString();
 				
 			}			
 		}; //end add element action definition
 		changeElementType.setText("Change Element Type");
 		changeElementType.setToolTipText("Add Rationale");
 */		
 		//
 		//add expertise element Action
 		//
 		addExpertise = new Action() {
 			public void run() {
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				if (obj instanceof TreeParent)
 				{
 					RationaleElement parentElement = getElement((TreeParent) obj, false);
 					RationaleElement rElement = new AreaExp();
 					createNewElement(parentElement, rElement, (TreeParent) obj);
 				}
 			}
 		}; //end of the action definition
 		addExpertise.setText("New Area of Expertise");
 		addExpertise.setToolTipText("Add Area of Expertise");
 		
 		//
 		//add constraint association element Action
 		//
 		addAltConstRel = new Action() {
 			public void run() {
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				if (obj instanceof TreeParent)
 				{
 					RationaleElement parentElement = getElement((TreeParent) obj, false);
 					RationaleElement rElement = new AltConstRel();
 					createNewElement(parentElement, rElement, (TreeParent) obj);
 				}
 			}
 		}; //end constraint assoc. action def.
 		addAltConstRel.setText("Associate Constraint");
 		addAltConstRel.setToolTipText("Associate Constraint");
 		
 		
 		//add findRelationships Action
 		//
 		findRelationships = new Action() {
 			public void run() {
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				if (obj instanceof TreeParent)
 				{
 					@SuppressWarnings("unused") RequirementRelationshipDisplay findRel = new RequirementRelationshipDisplay(viewer.getControl().getShell(), ((TreeParent) obj).getName());
 				}
 			}
 		};
 		findRelationships.setText("Find Relationships");
 		findRelationships.setToolTipText("Find Related Alternatives");
 		
 		//
 		//delete rationale element Action
 		//
 		deleteElement = new Action() {
 			public void run() {
 				
 				RationaleDB db = RationaleDB.getHandle();
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				if (obj instanceof TreeParent)
 				{
 					RationaleElement rElement = getElement((TreeParent) obj, false);
 					boolean canceled = rElement.delete();
 					if (!canceled)
 					{
 						Vector<RationaleStatus> newStat = rElement.updateOnDelete();
 //						System.out.println("new stat ln (editor) = " + newStat.size());
 						Vector<RationaleStatus> curStatus = null; 
 						UpdateManager manager = UpdateManager.getHandle();
 						curStatus = manager.getInitialStatus();										
 						RationaleTaskList tlist = RationaleTaskList.getHandle();
 						if (newStat != null)
 						{
 							//						System.out.println("updated status");
 							//Before updating the task list, compare the status lists!
 							updateStatus(curStatus, newStat);
 							//update the database
 							db.addStatus(newStat);
 							//update tasks too
 							tlist.addTasks(newStat);
 						}
 						if (curStatus.size() > 0)
 						{
 							db.removeStatus(curStatus);
 							//update our task list as well
 //							System.out.println("removing a task");
 							tlist.removeTasks(curStatus);
 						}
 						
 						//before we update our tree, we need to make sure we 
 						//do any "structural" updates!
 						//			TreeParent newParent = removeElement((TreeParent) obj);
 						TreeParent objDeleted = (TreeParent) obj;
 						
 						//we need to remove ALL occurences of this element
 						RationaleTreeMap map = RationaleTreeMap.getHandle();
 						String key = map.makeKey(objDeleted.getName(), objDeleted.getType());
 						Vector objList = map.getKeys(key);
 						while (objList.size() > 0)
 						{
 							TreeParent nextDel = (TreeParent) objList.firstElement();
 							removeElement(nextDel);
 							objList = map.getKeys(key);
 						}
 						//now make our updates
 						Vector treeUpdates = manager.makeUpdates();
 						//update what is displayed in the tree
 						//how do I update if name changes?
 						//need to iterate through all the items
 						Iterator treeI = treeUpdates.iterator();
 						while (treeI.hasNext())
 						{
 							viewer.update((TreeParent) treeI.next(), null);
 						}
 						
 						//do now?
 						
 						
 					}
 				}
 			}
 			
 		};
 		deleteElement.setText("Delete");
 		deleteElement.setToolTipText("Delete Rationale");
 //		deleteElement.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
 //		getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));				
 		
 		//
 		//associate Ontology Action - associates an Argument Ontology
 		//entry with a constraint
 		//
 		associateOntology = new Action() {
 			public void run() {
 				
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				if (obj instanceof TreeParent)
 				{
 					RationaleElement parentElement = getElement((TreeParent) obj, false);
 					associateElement(parentElement, (TreeParent) obj);
 					
 				}
 				
 			}
 			
 		};
 		associateOntology.setText("Associate Ontology");
 		associateOntology.setToolTipText("Associate Ontology");
 		
 		//
 		//associate constraint Action - this associates a constraint with an
 		//alternative or decision
 		//
 		associateConstraint = new Action() {
 			public void run() {
 				
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				if (obj instanceof TreeParent)
 				{
 					RationaleElement parentElement = getElement((TreeParent) obj, false);
 					associateElement(parentElement, (TreeParent) obj);
 					
 				}
 				
 			}
 			
 		};
 		associateConstraint.setText("Associate Constraint");
 		associateConstraint.setToolTipText("Associate Constraint");
 		
 		// export argument ontology action- saves current argument ontology to XML
 		exportOntology = new Action() {
 			public void run() {
 				Shell shell = new Shell();
 				FileDialog path = new FileDialog(shell, SWT.SAVE);
 				String[] ext = {"*.xml"};
 				String[] name = {"XML (*.xml)"};
 				path.setFilterExtensions(ext);
 				path.setFilterNames(name);
 				// set default path to the static filename from RationaleDB
 				path.setFileName(RationaleDB.getOntName());
 				
 				shell.pack();
 				
 				// Open the path that the user selected
 				String filePath = path.open();
 				
 				RationaleDB db = RationaleDB.getHandle();
 				if (db.exportOntology(filePath)) {
 					showInformation("The argument ontology has been successfully saved to XML.");
 				} else {
 					showInformation("The argument ontology could not be saved to XML.  If you have manually" +
 							" modified your argument-ontology.xml file you may need to delete it and try again.");
 				}
 			}
 		};
 		
 		exportOntology.setText("Export Ontology to XML");
 		exportOntology.setToolTipText("Export the Argument-Ontology to XML");
 	}
 	
 	/**
 	 * hookDoubleClickAction is used to edit the rationale elements when you 
 	 * double-click them
 	 *
 	 */
 	private void hookDoubleClickAction() {
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 //				doubleClickAction.run();
 				
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				if (obj instanceof TreeParent)
 				{
 					try {
 						editElement.run();
 					} catch (NullPointerException npe) {
 						/* This means they clicked on an invalid node.
 						 * Don't display an error, just leave it with no effect */
 					}
 				}
 			}
 		});
 	}
 	
 	/**
 	 * shows a message to the user
 	 * @param message
 	 */
 	/*	private void showMessage(String message) {
 	 MessageDialog.openInformation(
 	 viewer.getControl().getShell(),
 	 "RationaleExplorer",
 	 message);
 	 } */
 	
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 	
 	public void dispose() {
 		getViewSite().getWorkbenchWindow().getSelectionService().
 		removeSelectionListener(this);
 		super.dispose();
 	}
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
 	 */
 	
 	/**
 	 * Sets up your selection when a different Java element is chosen
 	 */
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		if (selection instanceof IStructuredSelection) 
 		{
 			if (((IStructuredSelection) selection).getFirstElement() instanceof IJavaElement) 
 			{
 //				System.out.println("found Java Element");
 				navigatorSelection = (IJavaElement) ((IStructuredSelection) selection).getFirstElement();
 				associate.setSelection(navigatorSelection);
 			}
 			else
 			{
 //				System.out.println("not a java item");
 			}
 		}
 		
 	}
 	
 	/**
 	 * Sets up the new status of a Rationale Element after it (or one of its children) 
 	 * is edited. This is done by taking new status elements and adding them to the database
 	 * and taking old no longer true status values and removing them
 	 * @param curStatus - on entry, this vector contains the previous status values, on exit it contains those that no longer apply
 	 * @param newStatus - on entry, the complete status, on exit, only the new changes to add to the DB
 	 * @return returns a flag indicating if the status changed
 	 */
 	public boolean updateStatus(Vector<RationaleStatus> curStatus, Vector<RationaleStatus> newStatus)
 	{
 		boolean different = true;
 		Vector<RationaleStatus> removeCur = new Vector<RationaleStatus>();
 		Vector<RationaleStatus> removeNew = new Vector<RationaleStatus>();
 		
 		Iterator curS = curStatus.iterator();
 		//for each item in curStatus
 		while (curS.hasNext())
 		{
 			RationaleStatus curSt = (RationaleStatus) curS.next();
 			Iterator newS = newStatus.iterator();
 			while (newS.hasNext())
 			{
 				RationaleStatus newSt = (RationaleStatus) newS.next();
 				//if a matching item in newStatus is found,
 				if (curSt.equivalentTo(newSt))
 				{
 					//remove item from current status
 					removeCur.add(curSt);
 					//remove item from new status
 					removeNew.add(newSt);
 				}
 			}
 		}
 		
 		newStatus.removeAll(removeNew);
 		curStatus.removeAll(removeCur);
 		
 		//this will leave curStatus with a list of items that should
 		//be removed from the database and from the list
 		//this will leave newStatus with a list of items that should
 		//be added to the database and to the list
 		return different;
 	}
 	
 	/**
 	 * Update status
 	 */
 	private void updateStatus(RationaleElement rElement)
 	{
 		//we update the parent status, not the new element status
 		//this might not be correct...
 		Vector<RationaleStatus> newStat = null;
 		//try updating our own status - shouldn't that automatically get our parent?
 		newStat = rElement.updateStatus();
 		Vector<RationaleStatus> curStatus = null; 
 		UpdateManager manager = UpdateManager.getHandle();
 		curStatus = manager.getInitialStatus();										
 		RationaleTaskList tlist = RationaleTaskList.getHandle();
 		RationaleDB db = RationaleDB.getHandle();
 		if (newStat != null)
 		{
 //			System.out.println("updated status");
 			//Before updating the task list, compare the status lists!
 			updateStatus(curStatus, newStat);
 			//update the database
 			db.addStatus(newStat);
 			//update tasks too
 			tlist.addTasks(newStat);
 		}
 		if (curStatus.size() > 0)
 		{
 			db.removeStatus(curStatus);
 			//update our task list as well
 			tlist.removeTasks(curStatus);
 		}			
 	}
 
 	
 	/**
 	 * Updates the tree branch (to display new children, etc.)
 	 * @param parent - the top of the branch to refresh
 	 */
 	public void refreshBranch(TreeParent parent)
 	{
 		viewer.refresh(parent);
 		Iterator childrenI = parent.getIterator();
 		while (childrenI.hasNext())
 		{
 			refreshBranch((TreeParent) childrenI.next());
 		}
 	}
 	
 	/**
 	 * This method serves as a factory method that either creates a new element of the
 	 * type specified by the treeElement passed in to it or that uses the name of the treeElement
 	 * to get the corresponding RationaleElement from the database
 	 * @param treeElement - either the parent of the new element or the element itself
 	 * @param newElement - a flag indicating if we are creating a new RationaleElement
 	 * @return the RationaleElement created or read from the database
 	 */
 	public RationaleElement getElement(TreeObject treeElement, boolean newElement)
 	{
 		RationaleElement ourElement = null;
 		RationaleElementType type = treeElement.getType();
 		String name = treeElement.getName();
 		if (type == RationaleElementType.ASSUMPTION)
 		{
 			ourElement = new Assumption();
 		}
 		else if (type == RationaleElementType.ARGUMENT)
 		{
 			ourElement = new Argument();
 		}
 		else if (type == RationaleElementType.DECISION)
 		{
 			ourElement = new Decision();
 		}
 		else if (type == RationaleElementType.ALTERNATIVE)
 		{
 			ourElement = new Alternative();
 		}
 		else if (type == RationaleElementType.ONTENTRY)
 		{
 			ourElement = new OntEntry();
 		}
 		else if (type == RationaleElementType.DESIGNPRODUCTENTRY)
 		{
 			ourElement = new DesignProductEntry();
 		}
 		else if (type == RationaleElementType.CONSTRAINT)
 		{
 			ourElement = new Constraint();
 		}
 		else if (type == RationaleElementType.CONTINGENCY)
 		{
 			ourElement = new Contingency();
 		}
 		else if (type == RationaleElementType.DESIGNER)
 		{
 			ourElement = new Designer();
 		}
 		else if (type == RationaleElementType.REQUIREMENT)
 		{
 			ourElement = new Requirement();
 		}
 		else if (type == RationaleElementType.CLAIM)
 		{
 			ourElement = new Claim();
 		}
 		else if (type == RationaleElementType.EXPERTISE)
 		{
 			ourElement = new AreaExp();
 		}
 		else if (type == RationaleElementType.ALTCONSTREL)
 		{
 			ourElement = new AltConstRel();
 		}
 		else if (type == RationaleElementType.QUESTION)
 		{
 			ourElement = new Question();
 		}
 		else if (type == RationaleElementType.TRADEOFF)
 		{
 			ourElement = new Tradeoff(true);
 		}
 		else if (type == RationaleElementType.COOCCURRENCE)
 		{
 			ourElement = new Tradeoff(false);
 		}
 		else if (type == RationaleElementType.RATIONALE)
 		{
 			if (name.compareTo("Tradeoffs") == 0)
 			{
 //				System.out.println("found our tradeoff");
 				ourElement = new Tradeoff(true);
 			}
 			else if (name.compareTo("Co-occurrences") == 0)
 			{
 				ourElement = new Tradeoff(false);
 			}
 			else if (name.compareTo("Requirements") == 0)
 			{
 				ourElement = new Requirement();
 			}
 			else if (name.compareTo("Decisions") == 0)
 			{
 				ourElement = new Decision();
 			}
 			else if (name.compareTo("Design-Contingencies") == 0)
 			{
 				ourElement = new Contingency();
 			}
 			else if (name.compareTo("Designer-Profiles") == 0)
 			{
 				ourElement = new Designer();
 			}
 		}
 		if (!newElement && !name.equals("imaginary root"))
 		{
 			ourElement.fromDatabase(treeElement.getName());
 		}
 		return ourElement;
 	}
 	/*	I'm not sure why this is here - no one seems to call it.
 	 boolean updateAll(TreeParent treeElement)
 	 {
 	 boolean update = true;
 	 if ((treeElement.getType() == RationaleElementType.TRADEOFF) ||
 	 (treeElement.getType() == RationaleElementType.COOCCURRENCE))
 	 {
 	 update = false;
 	 }
 	 return update;
 	 }
 	 */	
 	/**
 	 * this creates a new element in the rationale tree
 	 * @param parentElement - this is the rationaleElement parent
 	 * @param rElement - this is our new (child) element
 	 * @param obj - this is the parent tree element
 	 */
 	void createNewElement(RationaleElement parentElement, RationaleElement rElement, TreeParent obj)
 	{
 		RationaleDB db = RationaleDB.getHandle();
 		boolean canceled = rElement.create(viewer.getControl().getShell().getDisplay(), parentElement);
 		if (!canceled)
 		{
 			//need to add the new element to the tree...
 //			TreeParent newChild = new TreeParent(rElement.getName(), rElement.getElementType());				
 //			((TreeParent) obj).addChild(newChild);
 			System.out.println("name in createNewElement = " + rElement.getName());
 			addElement((TreeParent) obj, rElement);
 			//we update the parent status, not the new element status
 			//this might not be correct...
 			Vector<RationaleStatus> newStat = null;
 			/*			if (parentElement.getID() > 0)
 			 {
 			 newStat = parentElement.updateStatus();
 			 }
 			 */
 			//try updating our own status - shouldn't that automatically get our parent?
 			newStat = rElement.updateStatus();
 			Vector<RationaleStatus> curStatus = null; 
 			UpdateManager manager = UpdateManager.getHandle();
 			curStatus = manager.getInitialStatus();										
 			RationaleTaskList tlist = RationaleTaskList.getHandle();
 			if (newStat != null)
 			{
 //				System.out.println("updated status");
 				//Before updating the task list, compare the status lists!
 				updateStatus(curStatus, newStat);
 				//update the database
 				db.addStatus(newStat);
 				//update tasks too
 				tlist.addTasks(newStat);
 			}
 			if (curStatus.size() > 0)
 			{
 				db.removeStatus(curStatus);
 				//update our task list as well
 				tlist.removeTasks(curStatus);
 			}
 			
 			//add our new element
 			refreshBranch((TreeParent) obj);
 			
 			Vector treeUpdates = manager.makeUpdates();
 			//update what is displayed in the tree
 			//how do I update if name changes?
 //			System.out.println(rElement.getName() + ": enabled = " + new Boolean(rElement.getEnabled()).toString());
 			//need to iterate through all the items
 			Iterator treeI = treeUpdates.iterator();
 			while (treeI.hasNext())
 			{
 				viewer.update((TreeParent) treeI.next(), null);
 			}
 		}
 		
 	}
 	
 	
 	/**
 	 * Update method for creating a new element using the new editors.  The function
 	 * is similar to the createNewElement method except that this is called from the
 	 * rationale editor itself, creating some minor differences.  This method
 	 * and the editUpdate method, and their "old editor" counterparts, should
 	 * probably be refactored.
 	 * 
 	 * @param p - the parent tree element
 	 * @param e - the new (child) rationale element
 	 * @return the TreeParent object representing the new element - This is done so that
 	 * the rationale editor class that calls this method can get the correct reference to
 	 * the new element and update itself accordingly- otherwise it will be editing the parent!
 	 */
 	public TreeParent createUpdate(TreeParent p, RationaleElement e) {
 		RationaleDB db = RationaleDB.getHandle();		
 
 		// Add The Element TO The Tree
 		TreeParent newEle = addElement(p, e);
 		
 
 		// Update Status Of Element
 		Vector<RationaleStatus> status = null;
 		
 		status = e.updateStatus();
 		
 		// Update Rationale Task List And Database With New Status
 		RationaleTaskList tlist = RationaleTaskList.getHandle();
 		Vector<RationaleStatus> oldStatus = null;
 		UpdateManager manager = UpdateManager.getHandle();
 		
 		oldStatus = manager.getInitialStatus();
 		
 		if( status != null ) {
 			updateStatus(oldStatus, status);
 			db.addStatus(status);
 			tlist.addTasks(status);			
 		}
 		if( oldStatus.size() > 0 ) {
 			db.removeStatus(oldStatus);
 			tlist.removeTasks(oldStatus);
 		}
 		
 		// Refresh Affected Branch Of Tree
 		refreshBranch(p);
 		
 		// Update Anything In Tree Affected By Insertion
 		Vector<TreeObject> treeUpdates = manager.makeUpdates();
 		Iterator<TreeObject> treeIterator = treeUpdates.iterator();
 		while( treeIterator.hasNext() )
 		{
 			getViewer().update((TreeParent)treeIterator.next(), null);
 		}
 		
 		RationaleTreeMap map = RationaleTreeMap.getHandle();		
 		Vector treeObjs = map.getKeys(map.makeKey(e.getName(), e.getElementType()));
 		//if there's more than one we don't care, just get the first
 		viewer.reveal(treeObjs.elementAt(0));
 		viewer.expandToLevel(treeObjs.elementAt(0), 4);
 		
 		return newEle;
 	}
 	
 	public TreeViewer getViewer() { return viewer; }
 	
 	/**
 	 * This is used to assocate different types of rationale elements
 	 * with each other. For example, a constraint has an ontology element associated. A 
 	 * constraint can be associated with a decision or alternative but the alternative 
 	 * association is handled differently (see action AltConstRel).
 	 * @param parentElement the rationale element (Decision or Constraint)
 	 * @param obj the tree element corresponding to the parent
 	 */
 	void associateElement(RationaleElement parentElement, TreeParent obj)
 	{
 		RationaleDB db = RationaleDB.getHandle();
 		RationaleElement newChild = parentElement.associateElement(viewer.getControl().getShell().getDisplay());
 		if (newChild != null)
 		{
 			addElement((TreeParent) obj, newChild);
 			//we update the parent status, not the new element status
 			//this might not be correct...
 			Vector<RationaleStatus> newStat = null;
 			/*			if (parentElement.getID() > 0)
 			 {
 			 newStat = parentElement.updateStatus();
 			 }
 			 */
 			//try updating our own status - shouldn't that automatically get our parent?
 			newStat = newChild.updateStatus();
 			Vector<RationaleStatus> curStatus = null; 
 			UpdateManager manager = UpdateManager.getHandle();
 			curStatus = manager.getInitialStatus();										
 			RationaleTaskList tlist = RationaleTaskList.getHandle();
 			if (newStat != null)
 			{
 //				System.out.println("updated status");
 				//Before updating the task list, compare the status lists!
 				updateStatus(curStatus, newStat);
 				//update the database
 				db.addStatus(newStat);
 				//update tasks too
 				tlist.addTasks(newStat);
 			}
 			if (curStatus.size() > 0)
 			{
 				db.removeStatus(curStatus);
 				//update our task list as well
 				tlist.removeTasks(curStatus);
 			}
 			
 			//add our new element
 			refreshBranch((TreeParent) obj);
 			
 			Vector treeUpdates = manager.makeUpdates();
 			//update what is displayed in the tree
 			//how do I update if name changes?
 //			System.out.println(rElement.getName() + ": enabled = " + new Boolean(rElement.getEnabled()).toString());
 			//need to iterate through all the items
 			Iterator treeI = treeUpdates.iterator();
 			while (treeI.hasNext())
 			{
 				viewer.update((TreeParent) treeI.next(), null);
 			}
 		}
 		
 	}
 	/**
 	 * Adding a new element to our rationale tree
 	 * @param parent - the parent element in the tree
 	 * @param element - the rationale element being added
 	 * @return the new treeParent that corresponds to the rationaleElement
 	 */
 	public TreeParent addElement(TreeParent parent, RationaleElement element)
 	{
 		
 		if (element instanceof Tradeoff)
 		{
 //			System.out.println("adding Tradeoff");
 			return ( (RationaleViewContentProvider) viewer.getContentProvider()).addTradeoff(parent, (Tradeoff) element);		
 		}
 		else if (element instanceof Argument)
 		{
 //			System.out.println("adding Argument");
 			//will need a special argument provider
 			return ( (RationaleViewContentProvider) viewer.getContentProvider()).addArgument(parent, (Argument) element);
 		}
 		else if (element instanceof Claim)
 		{
 			//will need a special claim provider
 			return ( (RationaleViewContentProvider) viewer.getContentProvider()).addClaim(parent, (Claim) element);
 		}
 		else if (element instanceof Requirement)
 		{
 			//need a special requirement provider because now we can have NFRs
 			return ( (RationaleViewContentProvider) viewer.getContentProvider()).addRequirement(parent, (Requirement) element);
 		}	
 		/*
 		 else if (element instanceof AltConstRel)
 		 {
 		 return ( (RationaleViewContentProvider) viewer.getContentProvider()).addAltConstRel(parent, (AltConstRel) element);
 		 
 		 } */
 		else
 		{
 			return ( (RationaleViewContentProvider) viewer.getContentProvider()).addNewElement(parent, element);
 		}
 	}
 	
 	/**
 	 * Refreshes the display of a tree element. This is called after an element is
 	 * edited (I believe adding an element has its own refresh). The only types that
 	 * change structure when edited are the Argument (it could have a different argument
 	 * type), Claim (you could select a different Ontology Entry) and cooccurrence and tradeoff elements. Other elements only get children added
 	 * by saying "New" under the parent. That doesn't require a refreshElement call to update
 	 * 
 	 * @param parent - the parent whose children need to be refreshed
 	 * @param element - the element corresponding to the parent (provides the type)
 	 * @return returns the tree parent - the one passed in is discarded and a new one created
 	 */
 	public TreeParent refreshElement (TreeParent parent, RationaleElement element)
 	{
 		TreeParent grandParent = parent.getParent();
 		TreeParent newParent;
 		//now, check to see if there is likely to be a structural change
 		if ((element.getElementType() == RationaleElementType.ARGUMENT) ||
 				(element.getElementType() == RationaleElementType.REQUIREMENT) ||
 				(element.getElementType() == RationaleElementType.COOCCURRENCE) ||
 				(element.getElementType() == RationaleElementType.CLAIM) ||
 				(element.getElementType() == RationaleElementType.TRADEOFF))
 		{
 			RationaleViewContentProvider content = ( (RationaleViewContentProvider) viewer.getContentProvider());
 			
 			//for simplicity, we will assume that yes, the structure changed
 			//so, we remove the old element from the tree
 			content.removeElement(parent);
 			//and then, we create a new element based on the RationaleElement just
 			//modified
 			newParent = addElement(grandParent, element);	
 			refreshBranch(grandParent);		
 			return newParent;	
 			
 		}
 		else
 		{
 			return parent;
 		}
 		
 	}
 	
 	/**
 	 * Removes an element from the tree. This is done when something is deleted but
 	 * also done if the underlying tree structure is changed (the old is removed, and a new
 	 * one is generated)
 	 * @param parent the element being removed
 	 * @return the parent of the element that was just removed
 	 */
 	public TreeParent removeElement (TreeParent parent)
 	{
 		TreeParent grandParent = parent.getParent();
 		//shouldn't we remove the item from the map too?
 		RationaleTreeMap map = RationaleTreeMap.getHandle();
 		String key = map.makeKey(parent.getName(), parent.getType());
 		map.removeItem(key, parent);
 		//remove the old element from the tree
 		( (RationaleViewContentProvider) viewer.getContentProvider()).removeElement(parent);
 		//re-draw this branch of the tree
 		refreshBranch(grandParent);		
 		return grandParent;
 		
 	}
 	
 	//
 	
 	/**
 	 * This is the editing code that is called in response to a menu item from
 	 * the tree OR on receipt of an event from the task list.
 	 * @param obj - the selected tree element being edited
 	 * @param rElement - the rationale Element being edited
 	 * @param theDisplay - the parent display
 	 */
 	private void editElement (TreeParent obj, RationaleElement rElement, Display theDisplay)
 	{
 //		boolean canceled = rElement.display();
 		boolean canceled = rElement.display(viewer.getControl().getShell().getDisplay());
 //		System.out.println("canceled? = " + canceled);
 		if (!canceled)
 		{
 			updateTreeElement(obj, rElement);
 		}
 		
 	}
 	
 	/**
 	 * Update method for editing an existing element.  This has the same function as the
 	 * editElement method, but is designed to be called by the new editors instead (there is
 	 * no display passed from the new editors).
 	 * 
 	 * @param p - the selected tree element being edited
 	 * @param e - the rationale element being edited
 	 */
 	public TreeParent editUpdate(TreeParent p, RationaleElement e) {
 		return updateTreeElement(p, e);
 	}
 	
 	/**
 	 * updateTreeElement - updates our tree element after it has been edited. 
 	 * This includes any name changes, any status changes, and adding any new rationale
 	 * tasks to the task list. Yes, this method probably does too much... The database is updated
 	 * with the status changes as well.
 	 * @param obj - the tree element that was just edited
 	 * @param rElement - the corresponding rationale element
 	 */
 	private TreeParent updateTreeElement (TreeParent obj, RationaleElement rElement)
 	{
 		RationaleDB db = RationaleDB.getHandle();
 		//need to check to see if the name has changed
 		if (obj.getName().compareTo(rElement.getName()) != 0)
 		{
 //			System.out.println("name has changed");
 			//need to save the old and new names and make the changes
 			updateName(obj.getName(), rElement.getName(), rElement.getElementType());
 		}
 		Vector<RationaleStatus> newStat = rElement.updateStatus();
 		//		System.out.println("new stat ln (editor) = " + newStat.size());
 		Vector<RationaleStatus> curStatus = null; 
 		UpdateManager manager = UpdateManager.getHandle();
 		curStatus = manager.getInitialStatus();										
 		RationaleTaskList tlist = RationaleTaskList.getHandle();
 		if (newStat != null)
 		{
 			System.out.println("updated status");
 			//Before updating the task list, compare the status lists!
 			updateStatus(curStatus, newStat);
 			
 		}
 		if (curStatus.size() > 0)
 		{
 			db.removeStatus(curStatus);
 			//update our task list as well
 //			System.out.println("removing a task");
 			tlist.removeTasks(curStatus);
 		}
 		//moved to after to see if this helps (???)
 		if (newStat != null)
 		{
 			//update the database
 			db.addStatus(newStat);
 			//update tasks too
 			tlist.addTasks(newStat);
 		}
 		
 		//before we update our tree, we need to make sure we 
 		//do any "structural" updates!
 		TreeParent newParent = refreshElement((TreeParent) obj, rElement);
 		//now make our updates
 		Vector treeUpdates = manager.makeUpdates();
 		//update what is displayed in the tree
 		//how do I update if name changes?
 //		System.out.println(rElement.getName() + ": enabled = " + new Boolean(rElement.getEnabled()).toString());
 		newParent.update(rElement.getName(), rElement.getEnabled());
 		//need to iterate through all the items
 		Iterator treeI = treeUpdates.iterator();
 		if (!treeI.hasNext())
 		{
 			//refresh ourself - needed for root elements with no status
 			viewer.refresh((TreeParent) obj);
 		}
 		while (treeI.hasNext())
 		{
 			
 //			viewer.update((TreeParent) treeI.next(), null);
 			viewer.refresh((TreeParent) treeI.next());
 		}
 		
 		return newParent;
 	}
 	
 	/**
 	 * this updates the RationaleTreeMap that uses the name of the rationale elements
 	 * to find all its occurrences in the tree
 	 * @param oldName - the previous name
 	 * @param newName - the new name
 	 * @param type - the rationale element type
 	 */
 	public void updateName(String oldName, String newName, RationaleElementType type)
 	{
 		RationaleTreeMap map = RationaleTreeMap.getHandle();
 		String oldkey = map.makeKey(oldName, type);
 		String newkey = map.makeKey(newName, type);
 		Vector treeElements = map.getKeys(oldkey);
 		//now, iterate through our tree objects and set their name
 		Enumeration updateT = treeElements.elements();
 		//	System.out.println("number of tree elements = " + new Integer(treeElements.size()).toString());
 		while (updateT.hasMoreElements())
 		{
 			TreeObject leaf = (TreeObject) updateT.nextElement();
 			leaf.setName(newName);
 			map.removeItem(oldkey, leaf);
 			map.addItem(newkey, leaf);
 		}
 		
 	}
 	
 	//we are now going to assume that the editing is done by the time
 	//we receive the event
 	/**
 	 * When editing is done from a querie or some other method that does not
 	 * have the treeParent for the edited item, the GUI where the edits were invoked from
 	 * will fire a RationaleUpdateEvent so that we will know that the tree may have
 	 * changed. That is handled here.
 	 * @param e - the rationale update event received. It will contain the element itself.
 	 */
 	public void updateRationaleTree(RationaleUpdateEvent e)
 	{
 		RationaleElement ele = e.getRationaleElement();
 		//get the tree parent corresponding to the element...
 		RationaleTreeMap map = RationaleTreeMap.getHandle();
 		Vector treeObjs = map.getKeys(map.makeKey(ele.getName(), ele.getElementType()));
 		//if there's more than one we don't care, just get the first
 		TreeParent ourObj = (TreeParent) treeObjs.elementAt(0); 
 		updateTreeElement(ourObj, ele);
 		
 	}
 	
 	/**
 	 * This method takes the tree and expands a node when requested by the user. The
 	 * request comes in with a RationaleUpdateEvent
 	 * @param e - the rationale update event
 	 */
 	public void showRationaleNode(RationaleUpdateEvent e)
 	{
 		RationaleElement ele = e.getRationaleElement();
 		//get the tree parent corresponding to the element...
 		RationaleTreeMap map = RationaleTreeMap.getHandle();
 		Vector treeObjs = map.getKeys(map.makeKey(ele.getName(), ele.getElementType()));
 		/* We used to just look at the top element
 		viewer.reveal(treeObjs.elementAt(0));
 		viewer.expandToLevel(treeObjs.elementAt(0), 4); */
 		//expand *all* the occurences!
 		Iterator<TreeObject> treeIterator = treeObjs.iterator();
 		while( treeIterator.hasNext() )
 		{
 			TreeObject treeEle = (TreeObject) treeIterator.next();
 			viewer.reveal(treeEle);
 			viewer.expandToLevel(treeEle, 4);
 		}
 		
 		
 	}
 	
 	/**
 	 * This method is used to associate the java file with alternative in the ratinale 
 	 * explore in the java direction
 	 */
 	public void associateAlternative (RationaleUpdateEvent e)
 	
 	{
 		//get the Java element selected in the Package Explorer from the RationaleUpdateEvent
 		navigatorSelection = e.getIJavaElement();
 		
 		if (navigatorSelection != null)
 		{
 			
 			ISelection selection = viewer.getSelection();
 			
 			obj = ((IStructuredSelection)selection).getFirstElement();
 			
 			//whether an alternative is selected?
 			if (obj !=null && ((TreeParent)obj).getType() == RationaleElementType.ALTERNATIVE )
 			{
 				alternativeName=((TreeParent)obj).getName();	
 			}
 			//if there is no alternative selected, provide a select items of alternative
 			else{
 				SelectItem selectItem = new SelectItem(ourDisplay, RationaleElementType.fromString("Alternative"));
 				alternativeName=selectItem.getNewItem().getName();
 			}
 			
 			String assQ = "Associate '" +
 			alternativeName + "' with " +
 			navigatorSelection.getElementName() + "?";				
 			
 			boolean selOk = showQuestion(assQ);
 			if (selOk)
 			{
 				cstart = 0;
 				
 				ourRes = null;
 				try {
 					
 					
 					if (navigatorSelection.getElementType() == IJavaElement.COMPILATION_UNIT)
 					{
 						ourRes = navigatorSelection.getCorrespondingResource(); 
 					}
 					else
 					{
 						ourRes = navigatorSelection.getUnderlyingResource();
 						if (ourRes != null)
 						{
 //							***								System.out.println("this one wasn't null?");
 						}
 						//find the enclosing class file
 						IJavaElement nextE = navigatorSelection.getParent();
 						while ((nextE != null) && (nextE.getElementType() != IJavaElement.COMPILATION_UNIT))
 						{
 //							***								System.out.println("Name = " + nextE.getElementName());
 //							***							System.out.println("Type = " + nextE.getElementType());
 							nextE = nextE.getParent();
 						}
 						try {
 //							***							System.out.println("getting our resource");
 							//						ourRes = nextE.getUnderlyingResource();
 							ourRes = nextE.getCorrespondingResource();
 							ourRes = nextE.getResource();
 						} catch (JavaModelException ex)
 						{
 							System.out.println("exception getting resource?");
 						}
 //						***							System.out.println("Final name = " + nextE.getElementName());
 //						***							System.out.println("Final type = " + nextE.getElementType());
 						if (ourRes == null)
 						{
 							//see if we can get the element from the working copy
 							IJavaElement original = nextE.getPrimaryElement();
 //							Get working copy has been deprecated
 //							IJavaElement original = ((ICompilationUnit) ((ICompilationUnit) nextE).getWorkingCopy()).getOriginalElement();
 							ourRes = original.getCorrespondingResource();
 							
 						}
 					}
 					//						ourRes = navigatorSelection.getUnderlyingResource();
 					if (ourRes == null)
 					{
 //						***							System.out.println("why would our resource be null?");
 					}
 //					***						System.out.println("FullPath = " + ourRes.getFullPath().toString());
 //					***						System.out.println("now checking file extension?");
 					if (ourRes.getFullPath().getFileExtension().compareTo("java") == 0)
 					{
 //						***							System.out.println("creating our file?");
 						IJavaElement myJavaElement = JavaCore.create((IFile) ourRes);						
 //						***						System.out.println("created an element?");
 						if (myJavaElement.getElementType() == IJavaElement.COMPILATION_UNIT)
 						{
 //							***						   System.out.println("Compilation Unit");
 							ICompilationUnit myCompilationUnit = (ICompilationUnit)
 							myJavaElement;
 							
 							IType[] myTypes = myCompilationUnit.getTypes();
 							boolean found = false;
 							int i = 0;
 							while ((!found) && i < myTypes.length)
 							{
 								//selected item was the class itself
 								if (navigatorSelection.getElementType() == IJavaElement.COMPILATION_UNIT)
 								{
 //									***						   	 	System.out.println("found the class");
 									if (myTypes[i].isClass())
 									{
 										found = true;
 										cstart = myTypes[i].getNameRange().getOffset();
 									}
 								}
 								else if (navigatorSelection.getElementType() == IJavaElement.FIELD)
 								{
 //									***						   	 	System.out.println("looking for types");
 									IField[] myFields = myTypes[i].getFields();
 									for (int j = 0; j< myFields.length; j++)
 									{
 										if (myFields[j].getElementName().compareTo(navigatorSelection.getElementName()) == 0)
 										{
 //											***									 	System.out.println("found a type");
 											found = true;
 											cstart = myFields[j].getNameRange().getOffset();
 										}
 									}
 									
 								}
 								else if (navigatorSelection.getElementType() == IJavaElement.METHOD)
 								{
 //									***						   	 	System.out.println("looking for a method");
 									IMethod[] myMethods = myTypes[i].getMethods();
 									for (int j = 0; j< myMethods.length; j++)
 									{
 										if (myMethods[j].getElementName().compareTo(navigatorSelection.getElementName()) == 0)
 										{
 //											***									 	System.out.println("found a method");
 											found = true;
 											cstart = myMethods[j].getNameRange().getOffset();
 										}
 									}
 								}
 								//don't forget to increment!
 								i++;
 							} //end while
 						}
 						else
 						{
 //							***						 	System.out.println("not a compilation unit?");
 							System.out.println(myJavaElement.getElementType());
 						}
 						//ok... now what type is our selected item? 
 						System.out.println("got the resource?");
 						if (ourRes == null)
 						{
 							System.out.println("null resource???");
 						}
 					}
 					else
 					{
 						System.out.println("not a java file?");
 					}
 //					from the newsgroup - in a runnable?						
 					ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable()
 							{
 						public void run(IProgressMonitor monitor) {
 							try {
 //								***						System.out.println("line number = " + new Integer(lineNumber).toString());
 								IMarker ratM = ourRes.createMarker("SEURAT.ratmarker");
 								String dbname = RationaleDB.getDbName();
 								String markD = "Alt: '" +
 								alternativeName + "'   Rationale DB: '" + dbname + "'";
 								ratM.setAttribute(IMarker.MESSAGE, markD);
 								ratM.setAttribute(IMarker.CHAR_START, cstart);
 								ratM.setAttribute(IMarker.CHAR_END, cstart+1);
 								ratM.setAttribute(IMarker.SEVERITY, 0);
 //								ratM.setAttribute(IMarker.LINE_NUMBER, lineNumber);
 								String artName = navigatorSelection.getElementName();
 								ratM.setAttribute("alternative", alternativeName);
 								SEURATResourcePropertiesManager.addPersistentProperty (ourRes,
 										"Rat", "true");
 								RationaleDB d = RationaleDB.getHandle();
 								d.associateAlternative(alternativeName,
 										navigatorSelection.getHandleIdentifier(),
 										ourRes.getName(),
 										artName,
 										markD);
 								
 							}
 							catch (CoreException e) {
 								e.printStackTrace();
 							}
 						}
 							}, null);
 //					***						System.out.println("adding persistent property");
 					
 					SEURATDecoratorManager.addSuccessResources (ourRes);
 //					***						System.out.println("added our property");  
 					// Refresh the label decorations... Change it to DemoDecoratorWithImageCaching if image caching should be used
 //					((TreeParent) obj).setStatus(RationaleErrorLevel.ERROR);
 					viewer.update((TreeParent) obj, null);
 					SEURATLightWeightDecorator.getRatDecorator().refresh();
 //					***						System.out.println("refresh");
 					
 				}
 				catch (Exception ex)
 				{
 					ex.printStackTrace();
 					System.out.println("an exception occured in AssociateArtifactAction");
 				}
 				
 			}
 			else
 			{
 				System.out.println("selection rejected");
 			}
 		}
 		
 		else
 		{
 			System.out.println("No java element selected...");
 		}
 		
 		
 	}
 	/**
 	 * This method is used to pop-up a question to ask user whether to go on with the job
 	 * It is used for the association.
 	 * @param message
 	 * @return return value from the dialog
 	 */
 	private boolean showQuestion(String message) {
 		return MessageDialog.openQuestion(
 				viewer.getControl().getShell(),
 				"RationaleExplorer",
 				message);
 	}
 	
 	/**
 	 * This method is used to pop-up a message to inform the user that an action has been completed.
 	 * It is used when saving the argument ontology but could easily be re-used elsewhere.
 	 * @param message
 	 * @return return value from the dialog
 	 */
 	private void showInformation(String message) {
 		MessageDialog.openInformation(
 			viewer.getControl().getShell(),
 			"RationaleExplorer",
 			message);
 	}
 	/**
 	 * This method is used to rebuild the tree from a different copy of the database
 	 * 
 	 */
 	public void rebuildTree(boolean newStatus)
 	{
 		RationaleDB.resetConnection();
 		viewer.getTree().removeAll();
 		viewer.getContentProvider().dispose();
 		viewer.setContentProvider(new RationaleViewContentProvider());
 		//this should re-fresh from the new database
 		viewer.setInput(((RationaleViewContentProvider) viewer.getContentProvider()).initialize());
 		viewer.expandToLevel(2);
 		RationaleTaskList tlist = RationaleTaskList.getHandle();
 		tlist.resetTable();
 		if (newStatus)
 		{
 			//We need to re-set the status value for each element in the tree
 			TreeParent root = (TreeParent) viewer.getInput();
 			resetStatus(root);
 		}
 		// Restore the associations on startup
 		restoreAssociations.run();
 	}
 	
 	/**
 	 * This method resets status when a tree is built by adding elements from XML
 	 * 
 	 */
 	public void resetStatus(TreeParent node)
 	{
 //		updateTreeElement (TreeParent obj, RationaleElement rElement)
 		ArrayList<TreeObject> children = node.getChildrenList();
 		Iterator childI = children.iterator();
 		try
 		{
 		while (childI.hasNext())
 		{
 
 			Object childO = childI.next();
 			if (childO instanceof TreeParent)
 			{
 				TreeParent treeNode = (TreeParent) childO;
 				if (	(treeNode.getType() == RationaleElementType.REQUIREMENT) ||
 						(treeNode.getType() == RationaleElementType.DECISION) ||
 						(treeNode.getType() == RationaleElementType.ALTERNATIVE) ||
 						(treeNode.getType() == RationaleElementType.ARGUMENT) || 
 						(treeNode.getType() == RationaleElementType.QUESTION))
 				{
 					RationaleElement ele = getElement(treeNode, false);
 					if (ele != null)
 					{
 					updateTreeElement(treeNode, ele);
 					resetStatus(treeNode);
 					}
 				}
 				else if (treeNode.getType() == RationaleElementType.RATIONALE)
 				{
 					resetStatus(treeNode);
 				}
 				else
 				{
 					System.out.println("other type = " + treeNode.getType());
 				}
 
 				
 			}
 
 		}
 		}
 		catch (Exception ex)
 		{
 			System.out.println("Strange exception in Reset status");
 		}
 	}
 	 
 	
 	/**
 	 * Simple method to open an associated database.  Most of the work is done for us
 	 * in the createNewDB method; the RationaleExplorer simply needs to get the
 	 * name, call createNewDB, and then make a call to run the changeDatabase action.
 	 * The last step allows the database to be loaded right away.
 	 * 
 	 * @param e - the rationale update event prompting this operation
 	 */
 	public void openDatabase(RationaleUpdateEvent e) {
 		String name;
 		if (e.getIJavaElement() == null) name = (e.getProject().getName()) + "_Rat";
 		else name = ((IJavaProject) e.getIJavaElement()).getElementName() + "_Rat";
 		RationaleDB.createNewDB(name);
 		changeDatabase.run();
 	}
 	
 	/**
 	 * When a candidate rationale element is added to the tree from the Candidate
 	 * Rationale Explorer, an event is triggered to send the new element to be added
 	 * to the tree. At this point, the element (and its children, if applicable) have
 	 * already been added to the database.
 	 */
 	public void addNewElement(RationaleUpdateEvent e)
 	{
 		RationaleElement ele = e.getRationaleElement();
 
 		//get the tree parent corresponding to the element...
 		RationaleTreeMap map = RationaleTreeMap.getHandle();
 		TreeParent ourNode = null;
 		TreeParent ourParent = null;
 		
 
 		if (ele.getElementType() == RationaleElementType.REQUIREMENT)
 		{
 			Requirement req = (Requirement) ele;
 			Vector treeObjs;
 			//We might be saving the requirement as a sub-requirement so we need to check
 			if (req.getParent() <= 0)
 			{
    			     treeObjs = map.getKeys(map.makeKey("Requirements", RationaleElementType.RATIONALE));
 			}
 			else
 			{
 				Requirement parentReq = new Requirement();
 				parentReq.fromDatabase(req.getParent());
 				treeObjs = map.getKeys(map.makeKey(parentReq.getName(), RationaleElementType.REQUIREMENT));
 			}
 			int pid = 0;
 			while (pid < treeObjs.size())
 			{
 				ourParent = (TreeParent) treeObjs.elementAt(pid);
 				ourNode = addElement(ourParent, ele);
 				pid++;
 			}
 			//Now, add our children
 			Iterator children = req.getArguments().iterator();
 			while (children.hasNext())
 			{
 				Argument arg = (Argument) children.next();
 				addNewElement(ourNode, arg);
 			}
 			
 		}
 		else if (ele.getElementType() == RationaleElementType.DECISION)
 		{
 			Decision dec = (Decision) ele;
 			Vector treeObjs = null;
 			//We might be saving the decision as a sub-decision so we need to check
 			if (dec.getParent() <= 0)
 			{
 			 treeObjs = map.getKeys(map.makeKey("Decisions", RationaleElementType.RATIONALE));
 			}
 			else
 			{
 				Decision parentDec = new Decision();
 				parentDec.fromDatabase(dec.getParent());
 				treeObjs = map.getKeys(map.makeKey(parentDec.getName(), RationaleElementType.DECISION));
 			}
 			int pid = 0;
 			while (pid < treeObjs.size())
 			{
 				ourParent = (TreeParent) treeObjs.elementAt(pid);
 				ourNode = addElement(ourParent, ele);
 				pid++;
 			}
 			//Now, add our children alternatives
 			Iterator children = dec.getAlternatives().iterator();
 			while (children.hasNext())
 			{
 				Alternative alt = (Alternative) children.next();
 				addNewElement(ourNode, alt);
 			}
 			//Add any questions
 			Iterator qchildren = dec.getQuestions().iterator();
 			while (qchildren.hasNext())
 			{
 				Question quest = (Question) qchildren.next();
 				addNewElement(ourNode, quest);
 			}
 		}
 		else if (ele.getElementType() == RationaleElementType.ALTERNATIVE)
 		{
 			Alternative alt = (Alternative) ele;
 			Vector treeObjs = null;
 			//We might be saving this alternative under a new decision
 							Decision parentDec = new Decision();
 				parentDec.fromDatabase(alt.getParent());
 				treeObjs = map.getKeys(map.makeKey(parentDec.getName(), RationaleElementType.DECISION));
 						int pid = 0;
 			while (pid < treeObjs.size())
 			{
 				ourParent = (TreeParent) treeObjs.elementAt(pid);
 				ourNode = addElement(ourParent, ele);
 				pid++;
 			}
 			//Now, add our children
 			Iterator children = alt.getAllArguments().iterator();
 			while (children.hasNext())
 			{
 				Argument arg = (Argument) children.next();
 				addNewElement(ourNode, arg);
 			}
 			//Add any questions
 			Iterator qchildren = alt.getQuestions().iterator();
 			while (qchildren.hasNext())
 			{
 				Question quest = (Question) children.next();
 				addNewElement(ourNode, quest);
 			}
 		}
 		else if (ele.getElementType() == RationaleElementType.ARGUMENT)
 		{
 			Argument arg = (Argument) ele;
 			Vector treeObjs = null;
 			if (arg.getPtype() == RationaleElementType.REQUIREMENT)
 			{
 				Requirement parentRec = new Requirement();
 				parentRec.fromDatabase(arg.getParent());
 				treeObjs = map.getKeys(map.makeKey(parentRec.getName(), RationaleElementType.REQUIREMENT));
 			}
 			else if (arg.getPtype() == RationaleElementType.ALTERNATIVE)
 			{
 				Alternative parentAlt = new Alternative();
 				parentAlt.fromDatabase(arg.getParent());
 				treeObjs = map.getKeys(map.makeKey(parentAlt.getName(), RationaleElementType.ALTERNATIVE));
 			}
 			else
 			{
 				return;
 			}
 			int pid = 0;
 			while (pid < treeObjs.size())
 			{
 				ourParent = (TreeParent) treeObjs.elementAt(pid);
 				ourNode = addElement(ourParent, ele);
 				pid++;
 			}
 		}
 		else if (ele.getElementType() == RationaleElementType.QUESTION)
 		{
 			Question quest = (Question) ele;
 			Vector treeObjs = null;
 			if (quest.getPtype() == RationaleElementType.REQUIREMENT)
 			{
 				Requirement parentRec = new Requirement();
 				parentRec.fromDatabase(quest.getParent());
 				treeObjs = map.getKeys(map.makeKey(parentRec.getName(), RationaleElementType.REQUIREMENT));
 			}
 			else if (quest.getPtype() == RationaleElementType.ALTERNATIVE)
 			{
 				Alternative parentAlt = new Alternative();
 				parentAlt.fromDatabase(quest.getParent());
 				treeObjs = map.getKeys(map.makeKey(parentAlt.getName(), RationaleElementType.ALTERNATIVE));
 			}
 			else if (quest.getPtype() == RationaleElementType.DECISION)
 			{
 				Decision parentDec = new Decision();
 				parentDec.fromDatabase(quest.getParent());
 				treeObjs = map.getKeys(map.makeKey(parentDec.getName(), RationaleElementType.DECISION));
 			}
 			else
 			{
 				return;
 			}
 			int pid = 0;
 			while (pid < treeObjs.size())
 			{
 				ourParent = (TreeParent) treeObjs.elementAt(pid);
 				ourNode = addElement(ourParent, ele);
 				pid++;
 			}
 		} //end check question
 		
 		else if (ele.getElementType() == RationaleElementType.ASSUMPTION)
 		{
 			//Get the actual assumption to get the actual assumption ID
 			Assumption ourAssump = new Assumption();
 			ourAssump.fromDatabase(ele.getName());
 			
 			Vector args = RationaleDB.getDependentAssumptionArguments(ourAssump.getID());
 			
 			Iterator argI = args.iterator();
 			while (argI.hasNext())
 			{
 				Argument arg = (Argument) argI.next();
 				Vector treeObjs;
 
 					treeObjs = map.getKeys(map.makeKey(arg.getName(), RationaleElementType.ARGUMENT));
 				int pid = 0;
 				while (pid < treeObjs.size())
 				{
 					ourParent = (TreeParent) treeObjs.elementAt(pid);
 					ourNode = addElement(ourParent, ele);
 					pid++;
 				}				
 			}
 			
 		}
 		
 		if (ourParent != null)
 		{
 		refreshBranch(ourParent);
 		}		
 	}
 	
 	private boolean isItsChild(TreeParent child,String name)
 	{
 		System.out.println(child.getName());
 		if ((child.getName()).compareTo(name)==0)
 		{
 			return true;
 		}
 		else
 		{
 			if ((child.getChildrenList()).isEmpty())
 				return false;
 			else
 			{
 				ArrayList<TreeObject> children=child.getChildrenList();
 				for(int i=0; i< children.size(); i++)
 				{
 					if(isItsChild((TreeParent)children.get(i),name)==true)
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * This is the code called when we move an element to a different part of the tree
 	 * @param obj - the selected tree element being moved
 	 * @param rElement - the rationale Element being moved
 	 * @param theDisplay - the parent display
 	 */
 	private TreeParent moveElement(TreeParent obj, RationaleElement rElement, Display theDisplay)
 	{
 		TreeParent newParentTreeParent;
 		TreeParent newChildTreeParent;
 
 //		System.out.println(obj.getType());
 //		SelectRationaleElement sel= new SelectRationaleElement(theDisplay, obj.getType());
 		SelectRationaleElement_Treeview sel= new SelectRationaleElement_Treeview(theDisplay, obj.getType());
 		RationaleElement parentRat = (RationaleElement) sel.getSelEle();
 //		sel.getNewItem();
 		
 		if(((String)parentRat.getName()).equals((String)rElement.getName()))
 		{
 			return null;
 		}
 		ArrayList<TreeObject> children=obj.getChildrenList();
 		
 		for(int i=0;i<children.size();i++)
 		{
 //			TreeParent abc=(TreeParent)children.get(i);
 			if (isItsChild((TreeParent)children.get(i),parentRat.getName())==true)
 			{
 				return null;
 			}
 		}
 
 		
 		//Move requirement under requirement
 		if(obj.getType()==RationaleElementType.REQUIREMENT)
 		{
 			Requirement req = new Requirement();
 			req.fromDatabase(obj.getName());
 			req.setParent(parentRat.getID());
 			req.toDatabase(parentRat.getID(),RationaleElementType.REQUIREMENT,false);
 		}
 		//Move alternative under decision
 		if(obj.getType()==RationaleElementType.ALTERNATIVE)
 		{
 			Alternative alt = new Alternative();
 			alt.fromDatabase(obj.getName());
 			alt.setParent(parentRat.getID());
 			alt.toDatabase(parentRat.getID(), RationaleElementType.DECISION,false);
 		}
 		//Move argument under alternative or requirement
 		if(obj.getType()==RationaleElementType.ARGUMENT)
 		{
 			Argument arg = new Argument();
 			arg.fromDatabase(obj.getName());
 			arg.setParent(parentRat.getID());
 			if(parentRat.getElementType()==RationaleElementType.REQUIREMENT)
 				arg.toDatabase(parentRat.getID(), RationaleElementType.REQUIREMENT,false);
 			if(parentRat.getElementType()==RationaleElementType.ALTERNATIVE)
 				arg.toDatabase(parentRat.getID(), RationaleElementType.ALTERNATIVE,false);
 		}
 		//Move decision under alternative:
 		if(obj.getType()==RationaleElementType.DECISION)
 		{
 			Decision dec = new Decision();
 			dec.fromDatabase(obj.getName());
 		    dec.setParent(parentRat.getID());
 		    dec.toDatabase(parentRat.getID(), RationaleElementType.ALTERNATIVE);
 		}
 		if(obj.getType()==RationaleElementType.ASSUMPTION)
 		{
 			/* for future use
 			Assumption assupt = new Assumption();
 			assupt.fromDatabase(obj.getName()));
 			assupt.set
 			*/
 		}
 		if(obj.getType()==RationaleElementType.CLAIM)
 		{
 			// for future use
 		}
 		if(obj.getType()==RationaleElementType.QUESTION)
 		{
 			// for future use
 		}
 	
 		//now, we need to find the new parent in our tree... 
 		newParentTreeParent = ((RationaleViewContentProvider) viewer.getContentProvider()).findRationaleElement(parentRat.getName());
 		
 		//delete from the old parent
 		TreeParent oldParentTreeParent = (TreeParent)obj.getParent();
 		//remove the old element from the tree
 		( (RationaleViewContentProvider) viewer.getContentProvider()).removeElement((TreeParent) obj);
 		//re-draw this branch of the tree
 		refreshBranch(oldParentTreeParent);		
 		//add to our new parent
 		newChildTreeParent = addElement(newParentTreeParent, (RationaleElement) rElement);
 		refreshBranch(newParentTreeParent);
 		newParentTreeParent = ((RationaleViewContentProvider) viewer.getContentProvider()).findRationaleElement(rElement.getName());
 		refreshBranch(newParentTreeParent);
 		return newParentTreeParent;
 	}
 	
 	/**
 	 * This is the code called when we move an Argument element under Requirement element
 	 * @param obj - the selected tree element being moved
 	 * @param rElement - the rationale Element being moved
 	 * @param theDisplay - the parent display
 	 */
 	private TreeParent moveArgElementUnderReq(TreeParent obj, RationaleElement rElement, Display theDisplay)
 	{
 		TreeParent newParentTreeParent;
 		TreeParent newChildTreeParent;
 
 //		System.out.println(obj.getType());
 		SelectRationaleElement_Treeview sel= new SelectRationaleElement_Treeview(theDisplay, RationaleElementType.ARGUMENT, RationaleElementType.REQUIREMENT);
 		RationaleElement parentRat = (RationaleElement) sel.getSelEle();
 		
 		if(((String)parentRat.getName()).equals((String)rElement.getName()))
 		{
 			return null;
 		}
 		ArrayList<TreeObject> children=obj.getChildrenList();
 		
 		for(int i=0;i<children.size();i++)
 		{
 //			TreeParent abc=(TreeParent)children.get(i);
 			if (isItsChild((TreeParent)children.get(i),parentRat.getName())==true)
 			{
 				return null;
 			}
 		}
 		
 		if(obj.getType()==RationaleElementType.ARGUMENT)
 		{
 			Argument arg = new Argument();
 			arg.fromDatabase(obj.getName());
 			arg.setParent(parentRat.getID());
 			arg.toDatabase(parentRat.getID(), RationaleElementType.REQUIREMENT,false);
 		}
 		//Move decision under alternative:
 		
 		//now, we need to find the new parent in our tree... 
 		newParentTreeParent = ((RationaleViewContentProvider) viewer.getContentProvider()).findRationaleElement(parentRat.getName());
 		
 		//delete from the old parent
 		TreeParent oldParentTreeParent = (TreeParent)obj.getParent();
 		//remove the old element from the tree
 		( (RationaleViewContentProvider) viewer.getContentProvider()).removeElement((TreeParent) obj);
 		//re-draw this branch of the tree
 		refreshBranch(oldParentTreeParent);
 		//add to our new parent
 		newChildTreeParent = addElement(newParentTreeParent, (RationaleElement) rElement);
 		refreshBranch(newParentTreeParent);
 		newParentTreeParent = ((RationaleViewContentProvider) viewer.getContentProvider()).findRationaleElement(rElement.getName());
 		refreshBranch(newParentTreeParent);
 		return newParentTreeParent;
 	}
 	
 	/**
 	 * This is the code called when we move an Argument element under Alternative element
 	 * @param obj - the selected tree element being moved
 	 * @param rElement - the rationale Element being moved
 	 * @param theDisplay - the parent display
 	 */
 	private TreeParent moveArgElementUnderAlt(TreeParent obj, RationaleElement rElement, Display theDisplay)
 	{
 		TreeParent newParentTreeParent;
 		TreeParent newChildTreeParent;
 
 //		System.out.println(obj.getType());
 		SelectRationaleElementForArgAndDec sel= new SelectRationaleElementForArgAndDec(theDisplay, RationaleElementType.ARGUMENT, RationaleElementType.ALTERNATIVE);
 		RationaleElement parentRat = (RationaleElement) sel.getNewItem();
 		if(((String)parentRat.getName()).equals((String)rElement.getName()))
 		{
 			return null;
 		}
 		ArrayList<TreeObject> children=obj.getChildrenList();
 		
 		for(int i=0;i<children.size();i++)
 		{
 //			TreeParent abc=(TreeParent)children.get(i);
 			if (isItsChild((TreeParent)children.get(i),parentRat.getName())==true)
 			{
 				return null;
 			}
 		}
 		if(obj.getType()==RationaleElementType.ARGUMENT)
 		{
 			Argument arg = new Argument();
 			arg.fromDatabase(obj.getName());
 /*			String childName=(arg.getAlternative()).getName();
 			boolean flag=childName.equals(parentRat.getName());
 			if(flag)
 			{
 //				System.out.println("It won't do stupid things now");
 				return null;
 			}*/
 			arg.setParent(parentRat.getID());
 			arg.toDatabase(parentRat.getID(), RationaleElementType.ALTERNATIVE,false);
 		}
 		//Move decision under alternative:
 		
 		//now, we need to find the new parent in our tree... 
 		newParentTreeParent = ((RationaleViewContentProvider) viewer.getContentProvider()).findRationaleElement(parentRat.getName());
 		
 		//delete from the old parent
 		TreeParent oldParentTreeParent = (TreeParent)obj.getParent();
 		//remove the old element from the tree
 		( (RationaleViewContentProvider) viewer.getContentProvider()).removeElement((TreeParent) obj);
 		//re-draw this branch of the tree
 		refreshBranch(oldParentTreeParent);
 		//add to our new parent
 		newChildTreeParent = addElement(newParentTreeParent, (RationaleElement) rElement);
 		refreshBranch(newParentTreeParent);
 		newParentTreeParent = ((RationaleViewContentProvider) viewer.getContentProvider()).findRationaleElement(rElement.getName());
 		refreshBranch(newParentTreeParent);
 		return newParentTreeParent;
 	}
 	
 	/**
 	 * This is the code called when we move a Decision element under Alternative Element
 	 * @param obj - the selected tree element being moved
 	 * @param rElement - the rationale Element being moved
 	 * @param theDisplay - the parent display
 	 */
 	private TreeParent moveDecElementUnderAlt(TreeParent obj, RationaleElement rElement, Display theDisplay)
 	{
 		TreeParent newParentTreeParent;
 		TreeParent newChildTreeParent;
 
 //		System.out.println(obj.getType());
 		SelectRationaleElementForArgAndDec sel= new SelectRationaleElementForArgAndDec(theDisplay, RationaleElementType.DECISION, RationaleElementType.ALTERNATIVE);
 		RationaleElement parentRat = (RationaleElement) sel.getNewItem();
 		if(((String)parentRat.getName()).equals((String)rElement.getName()))
 		{
 			return null;
 		}
 		ArrayList<TreeObject> children=obj.getChildrenList();
 		
 		for(int i=0;i<children.size();i++)
 		{
 //			TreeParent abc=(TreeParent)children.get(i);
 			if (isItsChild((TreeParent)children.get(i),parentRat.getName())==true)
 			{
 				return null;
 			}
 		}
 		if(obj.getType()==RationaleElementType.DECISION)
 		{
 			Decision dec = new Decision();
 			dec.fromDatabase(obj.getName());
 /*			String childName=(arg.getAlternative()).getName();
 			boolean flag=childName.equals(parentRat.getName());
 			if(flag)
 			{
 //				System.out.println("It won't do stupid things now");
 				return null;
 			}*/
 			dec.setParent(parentRat.getID());
 			dec.toDatabase(parentRat.getID(), RationaleElementType.ALTERNATIVE);
 		}
 		//Move decision under alternative:
 		
 		//now, we need to find the new parent in our tree... 
 		newParentTreeParent = ((RationaleViewContentProvider) viewer.getContentProvider()).findRationaleElement(parentRat.getName());
 		
 		//delete from the old parent
 		TreeParent oldParentTreeParent = (TreeParent)obj.getParent();
 		//remove the old element from the tree
 		( (RationaleViewContentProvider) viewer.getContentProvider()).removeElement((TreeParent) obj);
 		//re-draw this branch of the tree
 		refreshBranch(oldParentTreeParent);
 		//add to our new parent
 		newChildTreeParent = addElement(newParentTreeParent, (RationaleElement) rElement);
 		refreshBranch(newParentTreeParent);
 		newParentTreeParent = ((RationaleViewContentProvider) viewer.getContentProvider()).findRationaleElement(rElement.getName());
 		refreshBranch(newParentTreeParent);
 		return newParentTreeParent;
 	}
 	/**
 	 * This is the code called when we move a Decision element under Decision Element
 	 * @param obj - the selected tree element being moved
 	 * @param rElement - the rationale Element being moved
 	 * @param theDisplay - the parent display
 	 */
 	private TreeParent moveDecElementUnderDecision(TreeParent obj, RationaleElement rElement, Display theDisplay)
 	{
 		TreeParent newParentTreeParent;
 		TreeParent newChildTreeParent;
 
 //		System.out.println(obj.getType());
 		SelectRationaleElement_Treeview sel= new SelectRationaleElement_Treeview(theDisplay, RationaleElementType.DECISION, RationaleElementType.DECISION);
 		RationaleElement parentRat = (RationaleElement) sel.getSelEle();
 		if(((String)parentRat.getName()).equals((String)rElement.getName()))
 		{
 			return null;
 		}
 		
 		//To find out whether the parent selected is the element's child in a recursion way
 		ArrayList<TreeObject> children=obj.getChildrenList();
 		
 		for(int i=0;i<children.size();i++)
 		{
 //			TreeParent abc=(TreeParent)children.get(i);
 			if (isItsChild((TreeParent)children.get(i),parentRat.getName())==true)
 			{
 				return null;
 			}
 		}
 		if(obj.getType()==RationaleElementType.DECISION)
 		{
 			Decision dec = new Decision();
 			dec.fromDatabase(obj.getName());
 /*			String childName=(arg.getAlternative()).getName();
 			boolean flag=childName.equals(parentRat.getName());
 			if(flag)
 			{
 //				System.out.println("It won't do stupid things now");
 				return null;
 			}*/
 			dec.setParent(parentRat.getID());
 			dec.toDatabase(parentRat.getID(), RationaleElementType.DECISION);
 		}
 		//Move decision under alternative:
 		
 		//now, we need to find the new parent in our tree... 
 		newParentTreeParent = ((RationaleViewContentProvider) viewer.getContentProvider()).findRationaleElement(parentRat.getName());
 		
 		//delete from the old parent
 		TreeParent oldParentTreeParent = (TreeParent)obj.getParent();
 		//remove the old element from the tree
 		( (RationaleViewContentProvider) viewer.getContentProvider()).removeElement((TreeParent) obj);
 		//re-draw this branch of the tree
 		refreshBranch(oldParentTreeParent);
 		//add to our new parent
 		newChildTreeParent = addElement(newParentTreeParent, (RationaleElement) rElement);
 		refreshBranch(newParentTreeParent);
 		newParentTreeParent = ((RationaleViewContentProvider) viewer.getContentProvider()).findRationaleElement(rElement.getName());
 		refreshBranch(newParentTreeParent);
 		return newParentTreeParent;
 	}
 	/**
 	 * This is the recursive version of addNewElement that adds a rationale element
 	 */
 	public void addNewElement(TreeParent parent, RationaleElement ele)
 	{
 		//get the tree parent corresponding to the element...
 		RationaleTreeMap map = RationaleTreeMap.getHandle();
 		TreeParent ourNode = null;
 		//update status (will this work?)
 		updateStatus(ele);
 		
 		if (ele.getElementType() == RationaleElementType.ALTERNATIVE)
 		{
 			ourNode = addElement(parent, ele);
 			//Now, add our children
 			Alternative alt = (Alternative) ele;
 			Iterator children = alt.getAllArguments().iterator();
 			while (children.hasNext())
 			{
 				Argument arg = (Argument) children.next();
 				addNewElement(ourNode, arg);
 			}
 			//Add any questions
 			Iterator qchildren = alt.getQuestions().iterator();
 			while (qchildren.hasNext())
 			{
 				Question quest = (Question) qchildren.next();
 				addNewElement(ourNode, quest);
 			}
 		}
 		else if (ele.getElementType() == RationaleElementType.ARGUMENT)
 		{
 			ourNode = addElement(parent, ele);
 			
 			//check if we have an assumption
 			Argument arg = (Argument) ele;
 			if (arg.getAssumption() != null)
 			{
 				Assumption assump = arg.getAssumption();
 				addNewElement(ourNode, assump);
 			}
 		}
 		else if (ele.getElementType() == RationaleElementType.QUESTION)
 		{
 			ourNode = addElement(parent, ele);
 		}
 		else if (ele.getElementType() == RationaleElementType.ASSUMPTION)
 		{
 			ourNode = addElement(parent, ele);
 		}
 		updateTreeElement(ourNode, ele);
 		refreshBranch(parent);
 	}
 	
 	/**
 	 * Method to update the rationale status if a status override is removed. This
 	 * was previously done when looking for overrides
 	 * 
 	 * @param e - the rationale update event
 	 */
 	public void updateRationaleStatus(RationaleUpdateEvent e)
 	{
 		RationaleStatus rStat = e.getOurStatus();
 		Vector <RationaleStatus> newStatus = new Vector<RationaleStatus>();
 		newStatus.add(rStat);
 		//Add it back to the task list
 		RationaleTaskList tlist = RationaleTaskList.getHandle();
 		tlist.addTasks(newStatus);
 		//need to update the rationale tree also
 		//need to update our tree item!
 		UpdateManager mgr = UpdateManager.getHandle();
 		RationaleElement ourEle = RationaleDB.getRationaleElement(rStat.getParent(), rStat.getRationaleType());				
 		mgr.addUpdate(rStat.getParent(), ourEle.getName(), rStat.getRationaleType());					
 		mgr.makeTreeUpdates();
 	}
 
 	/**
 	 * Method that handles Rationale Explorer behavior when a change is made to
 	 * the SEURAT preference store.  This method causes the database to be automatically
 	 * reloaded for two types of preference changes:
 	 * -Change in database type (Derby or MySQL)
 	 * -Change in database name (either on the Derby or the MySQL page)
 	 */
 	public void propertyChange(PropertyChangeEvent event) {
 		if (event.getProperty().equals(PreferenceConstants.P_DATABASETYPE) ||
 				event.getProperty().equals(PreferenceConstants.P_DERBYNAME) ||
 				event.getProperty().equals(PreferenceConstants.P_DATABASE)) {
 			rebuildTree(false); // Automatically connect to the new database
 			//waitingToConnectRemote = false;  // was part of below code
 		}
 		
 		
 		
 		/* There are some significant problems if we want the database to auto-change when
 		the user changes between local and remote MySQL databases.  For example, we don't know
 		if they are changing the address, port, account info, etc. and have no way of knowing
 		from this method.  Given that, in practice, users will not be changing between local
 		and remote MySQL databases much if at all (they'll either be set up with a local one
 		or a remote multi-user one and only be using that) I decided to leave this code out. */
 		
 		/*else if (event.getProperty().equals(PreferenceConstants.P_MYSQLLOCATION)) {
 			if (event.getNewValue().equals(PreferenceConstants.MySQLLocationType.REMOTE)) {
 				// We need to make sure the location and port are defined, otherwise
 				// we won't be able to connect.  Due to the huge range of possible invalid values
 				// we just do a basic sanity check here.
 				if (!SEURATPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_MYSQLADDRESS).equals("") &&
 						!SEURATPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_MYSQLPORT).equals("")) {
 					rebuildTree();
 					waitingToConnectRemote = false;
 				} else {
 					waitingToConnectRemote = true;
 				}
 			} else {
 				rebuildTree(); // Local, so we can probably go ahead and connect
 				waitingToConnectRemote = false;
 			}
 		}
 		else if (event.getProperty().equals(PreferenceConstants.P_MYSQLPORT) && waitingToConnectRemote) {
 			// We'll try to connect now, but it may not work if they also had to change account info.
 			// It's hard to implement a working system for this, but it seems unlikely to happen in practice.
 			waitingToConnectRemote = false;
 			rebuildTree();
 		}*/
 	}
 	
 	private void ExportXFeatureFile(String outputFile)
 	{
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		Document xfeatureDoc;
 		try {
 			DocumentBuilder builder = factory.newDocumentBuilder();
 
 			StringBuffer sb = new StringBuffer();
 
 			try {
 				BufferedReader in = new BufferedReader(new FileReader(outputFile));
 				String str;
 				while ((str = in.readLine()) != null) {
 					sb.append(str);
 				}
 				in.close();
 			} catch (IOException e) {
 				System.err.println(e.toString());
 			}
 
 			xfeatureDoc = builder.parse(new InputSource(new StringReader(sb.toString())));
 			createXFeatureXML(xfeatureDoc);
 			
 			//Need to re-write the file 
 			TransformerFactory transfac = TransformerFactory.newInstance();
 			Transformer trans = transfac.newTransformer();
 			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
 			trans.setOutputProperty(OutputKeys.INDENT, "no");
 			//create a string from the xml tree
 			StringWriter sw = new StringWriter();
 			StreamResult result = new StreamResult(sw);
 			DOMSource source = new DOMSource(xfeatureDoc);
 			trans.transform(source, result);
 			String xmlString = sw.toString();
 			// write the file
 			File f=new File(outputFile);
 
 			FileWriter fw = new FileWriter(f);
 			fw.write(xmlString);
 			fw.close();
 
 		} catch (SAXException sce) {
 			System.err.println( sce.toString());
 			System.err.println ("SAX Exception parsing xfm file");
 			return;
 		} catch (IOException ioe) {
 			System.err.println (ioe.toString());
 			System.err.println ("I/O Exception parsing xfm file");
 			return;
 		} catch (ParserConfigurationException pce) {
 			System.err.println (pce.toString());
 			System.err.println ("Parser configuration exception with xfm file");
 			return;
 		} catch (TransformerException e) {
 			// TODO Auto-generated catch block
 			System.err.println("Error in transforming");
 			e.printStackTrace();
 		}
 
 	}
 	private void createXFeatureXML(Document xfeatureDoc) {
 
 		Element xfeatureTop = xfeatureDoc.getDocumentElement();
 		String nmSpace=xfeatureTop.getNamespaceURI();
 		//this should be the fm:FeatureModel element
 		String source = xfeatureTop.getAttribute("fm:value");
 		String name = xfeatureTop.getNodeName();
 		
 		Vector<XFeatureMapping> xfeatureroot = RationaleDB.getToplevelMappings();
 		Iterator xI = xfeatureroot.iterator();
 		XFeatureMapping xRoot = (XFeatureMapping) xI.next(); //hopefully just one!!!
 		
 		Element rootNode = xfeatureDoc.createElement("fm:" + xRoot.getNodeName());
 		rootNode.setAttribute("fm:value", xRoot.getNodeName());
 		xfeatureTop.appendChild(rootNode);
 		
 		//Retrieve the XFeature nodes
 		Vector<XFeatureMapping> xfeaturenodes = RationaleDB.getDependentMappings(xRoot.getId());
 		xI = xfeaturenodes.iterator();
 		while (xI.hasNext())
 		{
 			XFeatureMapping xNode = (XFeatureMapping) xI.next();
 			if (xNode.getNodeType().equals(XFeatureNodeType.SOLITARYFEATURENODE))
 			{
 				decodeSolitaryFeature(xfeatureDoc, rootNode, xNode);
 			}
 			else if (xNode.getNodeType().equals(XFeatureNodeType.GROUPNODE))
 			{
 				decodeGroupFeature(xfeatureDoc, rootNode, xNode);
 			}
 		}
 	}
 
 	private void decodeGroupFeature(Document xfeatureDoc, Element xfeatureTop, XFeatureMapping xNode) {
 		// Get our alternatives!
 		Vector<XFeatureMapping> xfeatureAlts = RationaleDB.getDependentMappings(xNode.getId());
 		Iterator xI = xfeatureAlts.iterator();
 		while (xI.hasNext())
 		{
 			XFeatureMapping xAlt = (XFeatureMapping) xI.next();
 			if (xAlt.getRationaleType().equals(RationaleElementType.ALTERNATIVE))
 			{
 				Alternative cardAlt = new Alternative();
 				cardAlt.fromDatabase(xAlt.getRationaleID());
 				//If the alternative is not selected we can ignore it
 				if (cardAlt.getStatus().equals(AlternativeStatus.ADOPTED))
 				{
 					//Is there a sub-decision on how many?
 					Vector<XFeatureMapping> xfeaturenodes = RationaleDB.getDependentMappings(xAlt.getId());
 					//If there aren't any sub-nodes, then this was a feature with a cardinality of one
 					if (xfeaturenodes.isEmpty())
 					{
 						Element testNode = xfeatureDoc.createElement("fm:" + xAlt.getNodeName());
 						testNode.setAttribute("fm:value", xAlt.getNodeName());
 						xfeatureTop.appendChild(testNode);					
 					}
 					//Otherwise, there are some sub-features we need to take care of
 					else
 					{
 						xI = xfeaturenodes.iterator();
 						boolean addedSelf = false;
 						while (xI.hasNext())
 						{
 							XFeatureMapping xDec = (XFeatureMapping) xI.next();
 							if (xDec.getNodeType().equals(XFeatureNodeType.SOLITARYFEATURENODE))
 							{
 								if (xDec.getNodeName().equals(xAlt.getNodeName()))
 								{
 									addedSelf = true;
 								}
 								decodeSolitaryFeature(xfeatureDoc, xfeatureTop, xDec);
 							}
 							else if (xDec.getNodeType().equals(XFeatureNodeType.GROUPNODE))
 								{
 								if (!addedSelf)
 								{
 								//First, add ourself (assume a cardinality of one)
 								Element testNode = xfeatureDoc.createElement("fm:" + xAlt.getNodeName());
 								testNode.setAttribute("fm:value", xAlt.getNodeName());
 								xfeatureTop.appendChild(testNode);
 									decodeGroupFeature(xfeatureDoc, testNode, xDec);
 								}
 								else
 								{
 									decodeGroupFeature(xfeatureDoc, xfeatureTop, xDec);
 								}
 								}
 						}
 					}	
 				}
 			}
 		}
 	}
 
 	private void decodeSolitaryFeature(Document xfeatureDoc, Element xfeatureTop,
 			XFeatureMapping xNode) {
 		// Get our alternatives!
 		Vector<XFeatureMapping> xfeatureAlts = RationaleDB.getDependentMappings(xNode.getId());
 		Iterator xI = xfeatureAlts.iterator();
 		while (xI.hasNext())
 		{
 			XFeatureMapping xAlt = (XFeatureMapping) xI.next();
 			if (xAlt.getRationaleType().equals(RationaleElementType.ALTERNATIVE))
 			{
 				Alternative cardAlt = new Alternative();
 				cardAlt.fromDatabase(xAlt.getRationaleID());
 				//If the alternative is not selected we can ignore it
 				if (cardAlt.getStatus().equals(AlternativeStatus.ADOPTED))
 				{
 					Element testNode = null;
 					//we have a selected feature!
 					if ((xAlt.getNodeType().equals(XFeatureNodeType.SOLITARYFEATURECARDINALITYONE)) ||
 						(xAlt.getNodeType().equals(XFeatureNodeType.GROUPEDFEATURECARDINALITYONE)))
 					{
 						testNode = xfeatureDoc.createElement("fm:" + xAlt.getNodeName());
 						testNode.setAttribute("fm:value", xAlt.getNodeName());
 						xfeatureTop.appendChild(testNode);
 					}
 					//more than one?
 					else if ((xAlt.getNodeType().equals(XFeatureNodeType.SOLITARYFEATURECARDINALITYMANY)) ||
 							(xAlt.getNodeType().equals(XFeatureNodeType.GROUPEDFEATURECARDINALITYMANY)))
 					{
 						testNode = xfeatureDoc.createElement("fm:" + xAlt.getNodeName()); 
 						testNode.setAttribute("fm:value", xAlt.getNodeName() + "One");
 						xfeatureTop.appendChild(testNode);
 						testNode = xfeatureDoc.createElement("fm:" + xAlt.getNodeName()); 
 						testNode.setAttribute("fm:value", xAlt.getNodeName() + "Mult");
 					}
 					if (testNode != null)
 					{
 						//Are there sub-decisions under the alternative (unlikely)?
 						Vector<XFeatureMapping> xfeaturenodes = RationaleDB.getDependentMappings(xAlt.getId());
 						Iterator xI2 = xfeaturenodes.iterator();
 						while (xI2.hasNext())
 						{
 							XFeatureMapping chNode = (XFeatureMapping) xI2.next();
 							if (chNode.getNodeType().equals(XFeatureNodeType.SOLITARYFEATURENODE))
 							{
 								decodeSolitaryFeature(xfeatureDoc, testNode, chNode);
 							}
 							else if (chNode.getNodeType().equals(XFeatureNodeType.GROUPNODE))
 							{
 								decodeGroupFeature(xfeatureDoc, testNode, chNode);
 							}
 						}
 					}
 
 				}
 			}
 			else {
 				if (xAlt.getNodeType().equals(XFeatureNodeType.SOLITARYFEATURENODE))
 				{
 					decodeSolitaryFeature(xfeatureDoc, xfeatureTop, xAlt);
 				}
 				else if (xAlt.getNodeType().equals(XFeatureNodeType.GROUPNODE))
 				{
 					decodeGroupFeature(xfeatureDoc, xfeatureTop, xAlt);
 				}
 			}
 		}
 		
 	}
 
 
 
 	private Element findChildElement(Element parentNode)
 	{
 		Element childNode = null;
 		Node nextChild = parentNode.getFirstChild();
 		while ((childNode == null) && (nextChild != null))
 		{
 			if (nextChild instanceof Element)
 			{
 				childNode = (Element) nextChild;
 			}
 			else
 			{
 				nextChild = nextChild.getNextSibling();
 			}
 		}
 		return childNode;
 	}
 	
 	private Element findSiblingElement(Element prevNode)
 	{
 		Element siblingNode = null;
 		Node nextSib = prevNode.getNextSibling();
 		while ((siblingNode == null) && (nextSib != null))
 		{
 			if (nextSib instanceof Element)
 			{
 				siblingNode = (Element) nextSib;
 			}
 			else
 			{
 				nextSib = nextSib.getNextSibling();
 			}
 		}
 		return siblingNode;
 	}
 	private  void readXFeatureXML(Document xfeatureDoc)
 	{
 
 		Element xfeatureTop = xfeatureDoc.getDocumentElement();
 		//this should be the fm:FeatureModel element
 		String source = xfeatureTop.getAttribute("fm:value");
 		String name = xfeatureTop.getNodeName();
 		//Now, get the root of the feature model tree
 		Element xfeatureRoot =  findChildElement(xfeatureTop);
 		String name2 = xfeatureRoot.getNodeName();
 		String source2 = xfeatureRoot.getAttribute("fm:value");
 		//Need to store the mapping into the database. In this case, there is no corresponding rationale element
 		XFeatureMapping xmap = new XFeatureMapping(0, RationaleElementType.NONE, 
 				XFeatureNodeType.ROOTFEATURENODE, source2, -1);
 		int xID = xmap.toDatabase();
 		
 		if (xfeatureRoot == null)
 		{
 			System.out.println("Null root?");
 			return;
 		}
 		Element xfeatureNext = findChildElement(xfeatureRoot);
 		while (xfeatureNext != null)
 		{
 			int id = interpretFeature(xfeatureNext, 0, RationaleElementType.DECISION, xID);
 			xfeatureNext = (Element) findSiblingElement(xfeatureNext);
 		}
 	}
 	
 	private Element findCardinalityNode(Element xfeatureNext)
 	{
 		Element childNode = findChildElement(xfeatureNext);
 		while ((childNode != null) && (! (childNode.getNodeName().contains("Cardinality"))))
 		{
 			childNode = (Element) findSiblingElement(childNode);
 		}
 		return childNode;
 	}
 	private int interpretFeature(Element xfeatureNext, int parentID, RationaleElementType ptype, int parent)
 	{
 		//Get a handle to our database
 		RationaleDB db = RationaleDB.getHandle();
 		int ourID = 0;
 		int cardMin = 1;
 		int cardMax = 1;
 		int altxID = 0;
 		String nodeType = xfeatureNext.getNodeName();
 		Element cardNode = null;
 		if (!nodeType.contains("Cardinality"))
 		{
 			cardNode = findCardinalityNode(xfeatureNext);
 		}
 	
 		if (cardNode != null)
 		{
 			try
 			{
 			cardMin = Integer.parseInt(cardNode.getAttribute("fm:cardMin"));
 			}
 			catch (Exception ex)
 			{
 				cardMin = 0;
 			}
 
 			try
 			{
 				cardMax = Integer.parseInt(cardNode.getAttribute("fm:cardMax"));
 			}
 			catch (Exception ex)
 			{
 				cardMax = 5;
 			}
 		}
 		if ((nodeType.equals("fm:SolitaryFeatureNode")) || (nodeType.equals("fm:SolitaryFeature")))
 		{
 			String decName = xfeatureNext.getAttribute("fm:value");
 			Decision dec;
 			dec = (Decision) getElement(new TreeObject("unused", RationaleElementType.DECISION), true);
 			dec.setName("How many " + decName);
 			dec.setDescription(decName + " needs to be selected");
 			dec.setStatus(DecisionStatus.UNRESOLVED);
 			dec.setPhase(Phase.DESIGN);
 			dec.setParent(parentID);
 			dec.setPtype(ptype);
 			dec.setType(DecisionType.SINGLECHOICE);
 			int decID;
 			decID = dec.toDatabase(parentID, ptype);
 			XFeatureMapping xmap = new XFeatureMapping(decID, RationaleElementType.DECISION, 
 					XFeatureNodeType.SOLITARYFEATURENODE, decName, parent);
 			int xID = xmap.toDatabase();
 			
 			if (cardMin == 0)
 			{
 				Alternative alt;
 				alt = new Alternative();
 				alt.setName("No " + decName);
 				alt.setDescription(alt.getName());
 				alt.setEnabled(true);
 				alt.setPtype(RationaleElementType.DECISION);
 				alt.setParent(decID);
 				int altID = alt.toDatabase(decID, RationaleElementType.DECISION);
 				xmap = new XFeatureMapping(altID, RationaleElementType.ALTERNATIVE, 
 						XFeatureNodeType.SOLITARYFEATURECARDINALITYZERO, decName, xID);
 				altxID = xmap.toDatabase();
 			}
 			if (cardMax > 1)
 			{
 				Alternative alt;
 				alt = new Alternative();
 				alt.setName("Multiple " + decName);
 				alt.setDescription(alt.getName());
 				alt.setEnabled(true);
 				alt.setPtype(RationaleElementType.DECISION);
 				alt.setParent(decID);
 				alt.toDatabase(decID, RationaleElementType.DECISION);
 				int altID = alt.toDatabase(decID, RationaleElementType.DECISION);
 				xmap = new XFeatureMapping(altID, RationaleElementType.ALTERNATIVE, 
 						XFeatureNodeType.SOLITARYFEATURECARDINALITYMANY, decName, xID);
 				altxID = xmap.toDatabase();
 			}
 			if ((cardMax == 1) || (cardMin == 1))
 			{
 				Alternative alt;
 				alt = new Alternative();
 				alt.setName("One " + decName);
 				alt.setDescription(alt.getName());
 				alt.setEnabled(true);
 				alt.setPtype(RationaleElementType.DECISION);
 				alt.setParent(decID);
 				if ((cardMax == 1) && (cardMin==1))
 				{
 					alt.setStatus(AlternativeStatus.ADOPTED);
 				}		
 				alt.toDatabase(decID, RationaleElementType.DECISION);
 				int altID = alt.toDatabase(decID, RationaleElementType.DECISION);
 				xmap = new XFeatureMapping(altID, RationaleElementType.ALTERNATIVE, 
 						XFeatureNodeType.SOLITARYFEATURECARDINALITYONE, decName, xID);
 				altxID = xmap.toDatabase();
 			}
 			Element nextFeatureNode =  findChildElement(xfeatureNext);
 			while (nextFeatureNode != null)
 			{
 				int tmp = interpretFeature(nextFeatureNode, decID, RationaleElementType.DECISION, altxID);
 				nextFeatureNode = (Element) findSiblingElement(nextFeatureNode);
 			}
 		}
 		else if ((nodeType.equals("fm:GroupNode")) || (nodeType.equals("fm:FeatureGroup")))
 		{
 			String decName = xfeatureNext.getAttribute("fm:value");
 			Decision dec;
 			dec = (Decision) getElement(new TreeObject("unused", RationaleElementType.DECISION), true);
 			dec.setName(decName);
 			dec.setDescription(decName + " needs to be selected");
 			dec.setStatus(DecisionStatus.UNRESOLVED);
 			dec.setPhase(Phase.DESIGN);
 			dec.setParent(parentID);
 			dec.setPtype(ptype);
 			if (cardMax > 1)
 			{
 				dec.setType(DecisionType.MULTIPLECHOICE);
 			}
 			else
 			{
 				dec.setType(DecisionType.SINGLECHOICE);
 			}
 			int decID;
 			decID = dec.toDatabase(parentID, ptype);
 			XFeatureMapping xmap = new XFeatureMapping(decID, RationaleElementType.DECISION, 
 					XFeatureNodeType.GROUPNODE, decName, parent);
 			int xID = xmap.toDatabase();
 			Element groupedFeatureNode =  findChildElement(xfeatureNext);
 			while (groupedFeatureNode != null)
 			{
 				int tmp = interpretFeature(groupedFeatureNode, decID, RationaleElementType.DECISION, xID);
 				groupedFeatureNode = (Element) findSiblingElement(groupedFeatureNode);
 			}
 			
 		}
 		else if ((nodeType.equals("fm:GroupedFeatureNode")) || (nodeType.equals("fm:GroupedFeature")))
 		{
 			String altName = xfeatureNext.getAttribute("fm:value");
 			Alternative alt;
 			int altID;
 			alt = new Alternative();
 			alt.setName(altName);
 			alt.setDescription(alt.getName());
 			alt.setEnabled(true);
 			alt.setPtype(ptype);
 			alt.setParent(parentID);
 			altID = alt.toDatabase(parentID, ptype);
 			XFeatureMapping xmap = new XFeatureMapping(altID, RationaleElementType.ALTERNATIVE, 
 					XFeatureNodeType.GROUPEDFEATURENODE, altName, parent);
 			int xID = xmap.toDatabase();
 			//If cardinality does not max out at 1, need to create a decision on how many
 			if (cardMax > 1)
 			{
 				Decision dec;
 				dec = (Decision) getElement(new TreeObject("unused", RationaleElementType.DECISION), true);
 				dec.setName("How many " + altName);
 				dec.setDescription(altName + " needs to be selected");
 				dec.setStatus(DecisionStatus.UNRESOLVED);
 				dec.setPhase(Phase.DESIGN);
 				dec.setParent(altID);
 				dec.setPtype(RationaleElementType.ALTERNATIVE);
 				dec.setType(DecisionType.SINGLECHOICE);
 				int decID;
 				decID = dec.toDatabase(altID, RationaleElementType.ALTERNATIVE);
 				XFeatureMapping xdmap = new XFeatureMapping(decID, RationaleElementType.DECISION, 
 						XFeatureNodeType.GROUPEDFEATUREDECISION, altName, xID);
 				int dxID = xdmap.toDatabase();
 				if (cardMax > 1)
 				{
 					alt = new Alternative();
 					alt.setName("Multiple " + altName);
 					alt.setDescription(alt.getName());
 					alt.setEnabled(true);
 					alt.setPtype(RationaleElementType.DECISION);
 					alt.setParent(decID);
 					altID = alt.toDatabase(decID, RationaleElementType.DECISION);
 					XFeatureMapping xamap = new XFeatureMapping(altID, RationaleElementType.ALTERNATIVE, 
 							XFeatureNodeType.GROUPEDFEATURECARDINALITYMANY, altName, dxID);
 					xamap.toDatabase();
 				}
 				if ((cardMax == 1) || (cardMin == 1))
 				{
 					alt = new Alternative();
 					alt.setName("One " + altName);
 					alt.setDescription(alt.getName());
 					alt.setEnabled(true);
 					alt.setPtype(RationaleElementType.DECISION);
 					alt.setParent(decID);
 					altID = alt.toDatabase(decID, RationaleElementType.DECISION);
 					XFeatureMapping xamap = new XFeatureMapping(altID, RationaleElementType.ALTERNATIVE, 
 							XFeatureNodeType.GROUPEDFEATURECARDINALITYONE, altName, dxID);
 					xamap.toDatabase();
 				}
 			}
 			Element nextFeatureNode =  findChildElement(xfeatureNext);
 			while (nextFeatureNode != null)
 			{
 				int tmp = interpretFeature(nextFeatureNode, altID, RationaleElementType.ALTERNATIVE, xID);
 				nextFeatureNode = (Element) findSiblingElement(nextFeatureNode);
 			}
 		}
 		
 
 		return ourID;
 	}
 	
 
 	/**
 	 * Read in the XML file and import data into the database
 	 */
 	private void ImportXFeatureFile(String inputFile)
 	{
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		Document xfeatureDoc;
 		try {
 			DocumentBuilder builder = factory.newDocumentBuilder();
 
 			StringBuffer sb = new StringBuffer();
 
 			try {
 				BufferedReader in = new BufferedReader(new FileReader(inputFile));
 				String str;
 				while ((str = in.readLine()) != null) {
 					sb.append(str);
 				}
 				in.close();
 			} catch (IOException e) {
 				System.err.println(e.toString());
 			}
 
 			xfeatureDoc = builder.parse(new InputSource(new StringReader(sb.toString())));
 			readXFeatureXML(xfeatureDoc);
 
 			//We need to re-build the tree. One way would have been to simply add each element
 			//to the tree as it is created (most efficient?) but we'll just re-build it
 			rebuildTree(true);
 
 		} catch (SAXException sce) {
 			System.err.println( sce.toString());
 			System.err.println ("SAX Exception parsing xfm file");
 			return;
 		} catch (IOException ioe) {
 			System.err.println (ioe.toString());
 			System.err.println ("I/O Exception parsing xfm file");
 			return;
 		} catch (ParserConfigurationException pce) {
 			System.err.println (pce.toString());
 			System.err.println ("Parser configuration exception with xfm file");
 			return;
 		}
 
 	}
 	/**
 	 * Read in the TEI XML File. Create requirements and create arguments. This is not
 	 * a permanent piece of functionality but is added as a convenience.
 	 */
 	private void ImportTEIReqs(String inputFile)
 	{
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		Document teiDoc;
 		try {
 			DocumentBuilder builder = factory.newDocumentBuilder();
 
 			StringBuffer sb = new StringBuffer();
 
 			try {
 				BufferedReader in = new BufferedReader(new FileReader(inputFile));
 				String str;
 				while ((str = in.readLine()) != null) {
 					sb.append(str);
 				}
 				in.close();
 			} catch (IOException e) {
 				System.err.println(e.toString());
 			}
 
 			teiDoc = builder.parse(new InputSource(new StringReader(sb.toString())));
 			readTEIXML(teiDoc);
 
 			//We need to re-build the tree. One way would have been to simply add each element
 			//to the tree as it is created (most efficient?) but we'll just re-build it
 			rebuildTree(true);
 
 		} catch (SAXException sce) {
 			System.err.println( sce.toString());
 			System.err.println ("SAX Exception parsing xfm file");
 			return;
 		} catch (IOException ioe) {
 			System.err.println (ioe.toString());
 			System.err.println ("I/O Exception parsing xfm file");
 			return;
 		} catch (ParserConfigurationException pce) {
 			System.err.println (pce.toString());
 			System.err.println ("Parser configuration exception with xfm file");
 			return;
 		}
 
 	}
 	
 	private void readTEIXML(Document teiDoc) {
 		Element xfeatureTop = teiDoc.getDocumentElement();
 		//this should be the element list. we need to keep going
 		Element rootEle = findChildElement(xfeatureTop);
 		//That should be the root. Now we need to find the correlation
 		while (!rootEle.getTagName().equals("correlation"))
 		{
 			rootEle = findSiblingElement(rootEle);
 		}
 		Element ele = findChildElement(rootEle);
 		while (ele != null)
 		{
 			decodeElement(ele);
 			ele = findSiblingElement(ele);
 		}
 
 	}
 
 	private void decodeElement(Element ele) {
 		Element nameEle = findChildElement(ele);
 		if (nameEle == null)
 			return;
 		Element reqEle = findSiblingElement(nameEle);
 		Element reasonEle = findSiblingElement(reqEle);
 		String name = nameEle.getTextContent();
 		String req = reqEle.getTextContent();
 		String desc = reasonEle.getTextContent();
 		
 		//First, create a requirement with the name and reason
 		Requirement teiReq = new Requirement();
 		teiReq.setName(req);
 		teiReq.setDescription(desc);
 //		teiReq.setDescription("");
 		teiReq.setType(ReqType.FR);
 		teiReq.setOntology(null);
 		teiReq.setImportance(Importance.ESSENTIAL);
 		teiReq.setEnabled(false);
 		int rID = teiReq.toDatabase(-1, RationaleElementType.RATIONALE);
 		//Now, where do we need to add arguments?
 		//Retrieve the XFeature nodes
 		Vector<XFeatureMapping> xfeaturenodes = RationaleDB.getDependentMappings(name);
 		Iterator xI = xfeaturenodes.iterator();
 		while (xI.hasNext())
 		{
 			XFeatureMapping xNode = (XFeatureMapping) xI.next();
 			//Is this an argument against?
 			if ((xNode.getNodeType() == XFeatureNodeType.GROUPEDFEATURECARDINALITYZERO) ||
 					(xNode.getNodeType() == XFeatureNodeType.SOLITARYFEATURECARDINALITYZERO))
 			{
 				Argument arg = new Argument();
 				arg.setName("Violates requirement " + req);
 				arg.setRequirement(teiReq);
 				arg.setType(ArgType.VIOLATES);
 				arg.setParent(xNode.getRationaleID());
 				arg.setPtype(RationaleElementType.ALTERNATIVE);
 				arg.setAmount(5);
 				arg.setEnabled(true);
 				arg.setImportance(Importance.DEFAULT);
 				arg.setPlausibility(Plausibility.CERTAIN);
 				arg.updateStatus();
 				arg.toDatabase(xNode.getRationaleID(), RationaleElementType.ALTERNATIVE);
 			}
 			else if ((xNode.getNodeType() == XFeatureNodeType.GROUPEDFEATURECARDINALITYONE))
 			{
 				Argument arg = new Argument();
 				arg.setName("Satisfies requirement " + req);
 				arg.setRequirement(teiReq);
 				arg.setType(ArgType.SATISFIES);
 				arg.setParent(xNode.getRationaleID());
 				arg.setPtype(RationaleElementType.ALTERNATIVE);
 				arg.setPlausibility(Plausibility.CERTAIN);
 				arg.setAmount(5);
 				arg.setEnabled(true);
 				arg.setImportance(Importance.DEFAULT);
 				arg.updateStatus();
 				arg.toDatabase(xNode.getRationaleID(), RationaleElementType.ALTERNATIVE);				
 			}
 			else if ((xNode.getNodeType() == XFeatureNodeType.GROUPEDFEATURECARDINALITYMANY))
 			{
 				Argument arg = new Argument();
 				arg.setName("Choosing more than one satisfies requirement " + req);
 				arg.setRequirement(teiReq);
 				arg.setType(ArgType.SATISFIES);
 				arg.setParent(xNode.getRationaleID());
 				arg.setPlausibility(Plausibility.CERTAIN);
 				arg.setPtype(RationaleElementType.ALTERNATIVE);
 				arg.setAmount(5);
 				arg.setEnabled(true);
 				arg.setImportance(Importance.DEFAULT);
 				arg.updateStatus();
 				arg.toDatabase(xNode.getRationaleID(), RationaleElementType.ALTERNATIVE);
 			}
 			else if ((xNode.getNodeType() == XFeatureNodeType.GROUPEDFEATURENODE))
 			{
 				Argument arg = new Argument();
 				arg.setName("Choosing one satisfies requirement " + req);
 				arg.setRequirement(teiReq);
 				arg.setType(ArgType.SATISFIES);
 				arg.setParent(xNode.getRationaleID());
 				arg.setPtype(RationaleElementType.ALTERNATIVE);
 				arg.setPlausibility(Plausibility.CERTAIN);
 				arg.setAmount(5);
 				arg.setEnabled(true);
 				arg.setImportance(Importance.DEFAULT);
 				arg.updateStatus();
 				arg.toDatabase(xNode.getRationaleID(), RationaleElementType.ALTERNATIVE);
 			}
 			else if ((xNode.getNodeType() == XFeatureNodeType.SOLITARYFEATURECARDINALITYONE))
 			{
 				Argument arg = new Argument();
 				arg.setName("One feature satisfies requirement " + req);
 				arg.setRequirement(teiReq);
 				arg.setType(ArgType.SATISFIES);
 				arg.setParent(xNode.getRationaleID());
 				arg.setPtype(RationaleElementType.ALTERNATIVE);
 				arg.setPlausibility(Plausibility.CERTAIN);
 				arg.setAmount(5);
 				arg.setEnabled(true);
 				arg.setImportance(Importance.DEFAULT);
 				arg.updateStatus();
 				arg.toDatabase(xNode.getRationaleID(), RationaleElementType.ALTERNATIVE);	
 			}
 			else if ((xNode.getNodeType() == XFeatureNodeType.SOLITARYFEATURECARDINALITYMANY))
 			{
 				Argument arg = new Argument();
 				arg.setName("Multiple features satisfies requirement " + req);
 				arg.setRequirement(teiReq);
 				arg.setType(ArgType.SATISFIES);
 				arg.setParent(xNode.getRationaleID());
 				arg.setPtype(RationaleElementType.ALTERNATIVE);
 				arg.setAmount(5);
 				arg.setPlausibility(Plausibility.CERTAIN);
 				arg.setEnabled(true);
 				arg.setImportance(Importance.DEFAULT);
 				arg.updateStatus();
 				arg.toDatabase(xNode.getRationaleID(), RationaleElementType.ALTERNATIVE);
 			}
 		}
 	}
 }
