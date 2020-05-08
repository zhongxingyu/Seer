 package edu.pdx.svl.coDoc.views.link;
 
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.PlatformUI;
 
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuCreator;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.dnd.TransferData;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.*;
 import org.eclipse.ui.part.DrillDownAdapter;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.views.navigator.ResourceNavigator;
 import org.eclipse.ui.actions.RetargetAction;
 import org.eclipse.ui.navigator.resources.ProjectExplorer;
 
 
 import edu.pdx.svl.coDoc.cdc.preferences.PreferenceValues;
 import edu.pdx.svl.coDoc.cdc.datacenter.BaseEntry;
 import edu.pdx.svl.coDoc.cdc.datacenter.CDCCachedFile;
 import edu.pdx.svl.coDoc.cdc.datacenter.CDCDataCenter;
 import edu.pdx.svl.coDoc.cdc.datacenter.CDCModel;
 import edu.pdx.svl.coDoc.cdc.datacenter.CategoryEntry;
 import edu.pdx.svl.coDoc.cdc.datacenter.CodeSelection;
 import edu.pdx.svl.coDoc.cdc.datacenter.EntryNode;
 import edu.pdx.svl.coDoc.cdc.datacenter.LinkEntry;
 import edu.pdx.svl.coDoc.cdc.datacenter.MapSelectionFilter;
 import edu.pdx.svl.coDoc.cdc.datacenter.MapSelectionSort;
 import edu.pdx.svl.coDoc.cdc.datacenter.SpecSelection;
 import edu.pdx.svl.coDoc.cdc.editor.CDCEditor;
 import edu.pdx.svl.coDoc.cdc.editor.EntryEditor;
 import edu.pdx.svl.coDoc.cdc.editor.IReferenceExplorer;
 
 public class ReferenceExplorerView extends ViewPart implements ISelectionListener, Listener, IPartListener2, IResourceChangeListener, IReferenceExplorer {
 	public static final String ID = "edu.pdx.svl.coDoc.views.link.ReferenceExplorerView";
 	Composite parent;
 	private String projectname = null;
 	private final int NUM_HORIZONTAL_ELEMENTS = 5; // max num elements in a row
 	Text searchText;
 	String searchTextStr;
 	Combo combo;
 	Button checkButton;
 	private static TableViewer tableViewer = null;
 	private static TreeViewer treeViewer = null;
 
 	private DrillDownAdapter drillDownAdapter;
 	private Action action1;
 	private Action showCategoryAction;
 	private boolean showCategory = true;
 	private Action showUUIDAction;
 	private boolean showUUID = false;
 	private Action showTimeAction;
 	private boolean showTime = true;
 	private Action showOSAction;
 	private boolean showOS = false;
 	private Action showAuthorAction;
 	private boolean showAuthor = false;
 	private Action checkColumnAction;
 	
 	private MapSelectionFilter filter = new MapSelectionFilter();
 	private MapSelectionSort sorter = new MapSelectionSort();
 
 	public void sniff() {
 		System.out.println("ReferenceExplorerView.sniff()");
 		projectname = null;
 		IWorkbenchPart part = CDCEditor.getActivePart();
 		if(part instanceof IEditorPart) {
 			EntryEditor editorPart = (EntryEditor) CDCEditor.getActiveEntryEditor();
 			if (editorPart != null) {
 				projectname = editorPart.getProjectName();
 			}
 		} else if(part instanceof IViewPart) {
 			IViewPart view = CDCEditor.findVisibleView(IPageLayout.ID_PROJECT_EXPLORER);
 			if(view==null) {
 				view = CDCEditor.findVisibleView(IPageLayout.ID_RES_NAV);
 			}
 			if(view!=null) {
 				ISelectionProvider selProv = view.getViewSite().getSelectionProvider();
 				ISelection selection = selProv.getSelection();
 				if(selection != null) {
 				    if(selection instanceof IStructuredSelection) {
 				        for(Iterator it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
 				            Object element = it.next();
 			            	if(element instanceof IResource) {
 			            		IResource res = (IResource) element;
 				            	if(CDCEditor.isCDCProject(res.getProject().getName())) {
 					            	projectname = res.getProject().getName();
 				            	}
 				            }
 				        }
 				    }
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		this.parent = parent;
 		
 		//NH - I split the setup into several parts...
 		createSearchBarAndButtons(parent);
 		//createTableViewer(parent);
 		createTreeViewer(parent);
 		
 		makeActions();
 		//drillDownAdapter = new DrillDownAdapter(viewer);
 		contributeToActionBars();
 
 		sniff();
 		
 		getSite().getWorkbenchWindow().getPartService().addPartListener(this);
 		getSite().getPage().addSelectionListener(this);
 		selectionChanged(getSite().getPart(), getSite().getPage().getSelection());
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
 		
 		filter.setSelector(MapSelectionFilter.ALLDATA);
 		filter.setSearchStr("");
 		refresh();
 	}
 
 	private void contributeToActionBars() {
 		IActionBars bars = getViewSite().getActionBars();
 		fillLocalPullDown(bars.getMenuManager());
 		fillLocalToolBar(bars.getToolBarManager());
 	}
 
 	private void fillLocalPullDown(IMenuManager manager) {
 		manager.add(action1);
 		manager.add(new Separator());
 		manager.add(showCategoryAction);
 		manager.add(checkColumnAction);
 	}
 	
 	private void fillLocalToolBar(IToolBarManager manager) {
 		manager.add(action1);
 		manager.add(showCategoryAction);
 		manager.add(checkColumnAction);
 		manager.add(new Separator());
 		//drillDownAdapter.addNavigationActions(manager);
 	}
 
 	private final class chkColMenuCreator implements IMenuCreator {
 		private IAction[] actions;
 		public chkColMenuCreator(IAction[] actions) {
 			this.actions = actions;
 		}
 		@Override
 		public Menu getMenu(Control parent) {
 			Menu dropDownMenu = new Menu(parent);
 			for(IAction action : actions) {
 				ActionContributionItem contributionItem = new ActionContributionItem(action);
 				contributionItem.fill(dropDownMenu, -1);				
 			}
 			return dropDownMenu;
 		}
 		@Override
 		public Menu getMenu(Menu parent) {
 			Menu dropDownMenu = new Menu(parent);
 			for(IAction action : actions) {
 				ActionContributionItem contributionItem = new ActionContributionItem(action);
 				contributionItem.fill(dropDownMenu, -1);				
 			}
 			return dropDownMenu;
 		}
 		@Override
 		public void dispose() {
 		}
 	}
 	private void makeActions() {
 		action1 = new Action() {
 			public void run() {
 				ISelection selection = treeViewer.getSelection();
 				if(selection instanceof IStructuredSelection) {
 					IStructuredSelection sel = (IStructuredSelection) selection;
 					Object[] objs = sel.toArray();
 					treeViewer.setSelection(new StructuredSelection(objs[0]));
 				}
 			}
 		};
 		action1.setText("Action 1");
 		action1.setToolTipText("Action 1 tooltip");
 		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
 		
 		showCategoryAction = new Action("showCategory", IAction.AS_CHECK_BOX) {
 			public void run() {
 				showCategory = isChecked();
 				refresh();
 			}
 		};
 		showCategoryAction.setText("showCategory");
 		showCategoryAction.setChecked(true);
 		showCategoryAction.setToolTipText("showCategory");
 		showCategoryAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEF_VIEW));
 		
 		showUUIDAction = new Action("showUUIDAction", IAction.AS_CHECK_BOX) {
 			public void run() {
 			}
 		};
 		showUUIDAction.setText("showUUIDAction");
 		showUUIDAction.setToolTipText("showUUIDAction");
 		showTimeAction = new Action("showTimeAction", IAction.AS_CHECK_BOX) {
 			public void run() {
 				showUUID = isChecked();
 				if(showUUID) {
 					treeViewer.getTree().getColumn(1).setWidth(75);					
 				} else {
 					treeViewer.getTree().getColumn(1).setWidth(0);					
 				}
 			}
 		};
 		showTimeAction.setText("showTimeAction");
 		showTimeAction.setToolTipText("showTimeAction");
 		showOSAction = new Action("showOSAction", IAction.AS_CHECK_BOX) {
 			public void run() {
 				this.isChecked();
 				//showCategory = ;
 			}
 		};
 		showOSAction.setText("showOSAction");
 		showOSAction.setToolTipText("showOSAction");
 		showAuthorAction = new Action("showAuthorAction", IAction.AS_CHECK_BOX) {
 			public void run() {
 				this.isChecked();
 				showMessage("");
 			}
 		};
 		showAuthorAction.setText("showAuthorAction");
 		showAuthorAction.setToolTipText("showAuthorAction");
 		
 		checkColumnAction = new Action("checkColumn", IAction.AS_DROP_DOWN_MENU) {
 			public void run() {
 			}
 		};
 		checkColumnAction.setText("checkColumn");
 		checkColumnAction.setToolTipText("checkColumn");
 		checkColumnAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
 		checkColumnAction.setMenuCreator(new chkColMenuCreator(new IAction[]{showUUIDAction,showTimeAction,showOSAction,showAuthorAction}));
 	}
 	private void showMessage(String message) 
 	{
 		MessageDialog.openInformation(null, "Property View", message);
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	@Override
 	public void setFocus() {
 		if(tableViewer != null) {
 			tableViewer.getControl().setFocus();			
 		}
 		if(treeViewer != null) {
 			treeViewer.getControl().setFocus();
 		}
 	}
 	
 	public String getProjectName() {
 		return projectname;
 	}
 	
 	public EntryEditor getActiveEntryEditor() {
 		return (EntryEditor) CDCEditor.getActiveEntryEditor();
 	}
 
 	private void createSearchBarAndButtons(Composite parent) {
 		GridLayout layout = new GridLayout(NUM_HORIZONTAL_ELEMENTS, false);
 		parent.setLayout(layout);
 
 		// setup search bar
 		createSearchBar(parent);
 		
 		// setup buttons
 		createSearchTypeComboBox(parent);
 		// createShowCategoryCheckBox(parent);
 	}
 
 	private void createSearchBar(Composite parent) {
 		Label searchLabel = new Label(parent, SWT.NONE);
 		searchLabel.setText("Search: ");
 		searchText = new Text(parent, SWT.BORDER | SWT.SEARCH);
 		searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
 		searchText.addKeyListener(new KeyListener() {
 			@Override
 			public void keyReleased(KeyEvent e) {
 				//the enter/return key has the key code of 13
 				if (e.keyCode == 13 || e.keyCode == 16777296) {
 				}
 				searchTextStr = searchText.getText();
 				filter.setSearchStr(searchTextStr);
 				refresh();
 			}
 			@Override
 			public void keyPressed(KeyEvent e) {
 			}
 		});
 	}
 	
 	private void createSearchTypeComboBox(Composite parent) {
 		combo = new Combo (parent, SWT.READ_ONLY);
 		combo.setItems (new String [] {"All Data", "Code File", "Code Text", "Spec File", "PDF Page", "Spec Text", "Comments"});
 		combo.setToolTipText("Search Categories.");
 		combo.select(0);
 		combo.addSelectionListener( new SelectionListener() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				filter.setSelector(combo.getSelectionIndex());
 				refresh();
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 	}
 	
 	private void createShowCategoryCheckBox(Composite parent) {
 		Button showCategoryButton = new Button(parent, SWT.CHECK);
 		showCategoryButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				showCategory = ((Button) e.widget).getSelection();
 				refresh();
 			}
 		});
 		showCategoryButton.setText("showCategory");
 		showCategoryButton.setSelection(true);
 		showCategoryButton.setToolTipText("When checked, the categories of the links will show.");
 	}
 	
 	public void refresh() {
 		String cdcfilename = CDCEditor.projname2cdcName(projectname);
 		if(tableViewer != null) {
 			//tableViewer.setInput(CDCDataCenter.getInstance().getLinkTable(projectname, filter));
 			CDCDataCenter.getInstance().getLinkTable(cdcfilename, filter);
 			tableViewer.setInput(CDCDataCenter.getInstance().sortLinkTable(cdcfilename, sorter));
 			tableViewer.refresh();
 		}
 		if(treeViewer != null) {
 			if(showCategory) {
 				//treeViewer.setInput(CDCDataCenter.getInstance().getLinkTree(projectname, filter));				
 				CDCDataCenter.getInstance().getLinkTree(cdcfilename, filter);				
 				treeViewer.setInput(CDCDataCenter.getInstance().sortLinkTree(cdcfilename, sorter));				
 			} else {
 				//treeViewer.setInput(CDCDataCenter.getInstance().getLinkTable(projectname, filter));				
 				CDCDataCenter.getInstance().getLinkTable(cdcfilename, filter);
 				treeViewer.setInput(CDCDataCenter.getInstance().sortLinkTable(cdcfilename, sorter));
 			}
 			treeViewer.refresh();
 			treeViewer.expandToLevel(4);
 		}
 	}
 	public void refresh(EntryNode invisibleroot) {
 		String cdcfilename = CDCEditor.projname2cdcName(projectname);
 		if(tableViewer != null) {
 			tableViewer.setInput(invisibleroot);
 			tableViewer.refresh();
 		}
 		if(treeViewer != null) {
 			if(showCategory) {
 				treeViewer.setInput(invisibleroot);				
 			} else {
 				treeViewer.setInput(invisibleroot);
 			}
 			treeViewer.refresh();
 			treeViewer.expandToLevel(4);
 		}
 	}
 
 	public void reselect(EntryNode node) {
 		if(tableViewer != null) {
 			tableViewer.setSelection(new StructuredSelection(node),true);
 		}
 		if(treeViewer != null) {
 			treeViewer.setSelection(new StructuredSelection(node),true);
 			CategoryEntry entry = (node.getData() instanceof CategoryEntry) ? (CategoryEntry) node.getData() : (CategoryEntry) node.getParent().getData();
 			EntryEditor ed = (EntryEditor) CDCEditor.getOpenedEntryEditorTop(projectname);
 			if(ed != null) {
 				ed.setCurCategoryId(entry.uuid);
 			}							
 		}
 	}
 	public void reselect(String uuid) {
 		String cdcfilename = CDCEditor.projname2cdcName(projectname);
 		EntryNode node = CDCDataCenter.getInstance().getEntryNode(cdcfilename, uuid);
 		reselect(node);
 		selectEntryNodeInEditor(node);
 	}
 	private void selectEntryNodeInEditor(EntryNode node) {
 		if(node.getData() instanceof CategoryEntry) {
 			//highlightSelection(null);							
 			CategoryEntry entry = (CategoryEntry) node.getData();
 			EntryEditor ed = getActiveEntryEditor();
 			if(ed != null) {
 				ed.setCurCategoryId(entry.uuid);
 			}							
 		} else {
 			LinkEntry mp = (LinkEntry) node.getData();
 			highlightSelection(mp);							
 			CategoryEntry entry = (CategoryEntry) node.getParent().getData();
 			EntryEditor ed = getActiveEntryEditor();
 			if(ed != null) {
 				ed.setCurCategoryId(entry.uuid);
 			}							
 		}
 	}
 	
 	private void highlightSelection(LinkEntry mp) {
 		EntryEditor editor = (EntryEditor) CDCEditor.getOpenedEntryEditorTop(projectname);
 		if(editor == null) return;
 		if(mp != null) {
 			if(editor.getProjectName().equals(projectname)) {
 				String codeFilename1 = mp.codefilename;
 				IPath codepath2 = editor.getCodeFilepath();
 				String codefilename2 = (codepath2.isAbsolute()?"project://":"project:///")+codepath2;
 				if(codeFilename1.equals(codefilename2)) {
 					editor.selectTextInTextEditor(mp.codeselpath);
 				} else {
 					editor.selectTextInTextEditor((CodeSelection)null);				
 				}
 				String specFilename1 = mp.specfilename;
 				IPath specpath2 = editor.getSpecFilepath();
 				String specfilename2 = (specpath2.isAbsolute()?"project://":"project:///")+specpath2;
 				if(specFilename1.equals(specfilename2)) {
 					editor.selectTextInAcrobat(mp.specselpath);								
 				} else {
 					editor.selectTextInAcrobat(null);								
 				}
 			}			
 		} else {
 			editor.selectTextInTextEditor((CodeSelection)null);
 			editor.selectTextInAcrobat(null);
 		}
 	}
 
 	private void createTableViewer(Composite parent) {
 		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
 				| SWT.FULL_SELECTION | SWT.BORDER);
 
 		Table table = tableViewer.getTable();
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		TableLayout tLayout = new TableLayout();
 		table.setLayout(tLayout);
 		
 		tLayout.addColumnData(new ColumnWeightData(24));
 		new TableColumn(table,SWT.NONE).setText("Source file");
 		tLayout.addColumnData(new ColumnWeightData(60));
 		new TableColumn(table,SWT.NONE).setText("Code");
 		tLayout.addColumnData(new ColumnWeightData(24));
 		new TableColumn(table,SWT.NONE).setText("PDF file");
 		tLayout.addColumnData(new ColumnWeightData(10));
 		new TableColumn(table,SWT.NONE).setText("Page");
 		tLayout.addColumnData(new ColumnWeightData(64));
 		new TableColumn(table,SWT.NONE).setText("Specification Text");
 		tLayout.addColumnData(new ColumnWeightData(52));
 		new TableColumn(table,SWT.NONE).setText("Comments");
 
 		tableViewer.setContentProvider(new TableContentProvider());
 		tableViewer.setLabelProvider(new TableLabelProvider());
 
 		/*table.addListener (SWT.MeasureItem, new Listener () {
 			public void handleEvent (Event event) {
 				event.height = 10;
 			}
 		});*/
 		table.addListener (SWT.Selection, new Listener () {
 			public void handleEvent (Event event) {
 				ISelection selection = tableViewer.getSelection();
 				if (selection != null && selection instanceof IStructuredSelection) {
 					IStructuredSelection sel = (IStructuredSelection) selection;
 					for (Iterator<EntryNode> iterator = sel.iterator(); iterator.hasNext();) {
 						EntryNode node = iterator.next();
 						LinkEntry mp = (LinkEntry) node.getData();
 						highlightSelection(mp);
 					}
 				}					
 			}
 		});
 		table.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				ISelection selection = tableViewer.getSelection();
 				if (selection != null && selection instanceof IStructuredSelection) {
 					IStructuredSelection sel = (IStructuredSelection) selection;
 					for (Iterator<EntryNode> iterator = sel.iterator(); iterator.hasNext();) {
 						EntryNode node = iterator.next();
 						LinkEntry mp = (LinkEntry) node.getData();
 						String codeFilename = mp.codefilename;
 						String specFilename = mp.specfilename;
 						IPath codepath = new Path(codeFilename);
 						codepath = codepath.removeFirstSegments(1); // remove "project:"
 						IPath specpath = new Path(specFilename);
 						specpath = specpath.removeFirstSegments(1); // remove "project:"
 					    CDCEditor.open(projectname, codepath, specpath);
 						highlightSelection(mp);
 					}
 				}
 				setFocus();
 			}
 		});
 
 		// Layout the viewer
 		GridData gridData = new GridData();
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 6;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		tableViewer.getControl().setLayoutData(gridData);
 	}
 
 	private void createTreeViewer(Composite parent) {
 		treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
 				| /* NH */SWT.FULL_SELECTION | SWT.BORDER);
 
 		treeViewer.setAutoExpandLevel(4);
 		Tree tree = treeViewer.getTree();
 		tree.setHeaderVisible(true);
 		tree.setLinesVisible(false);
 
 		TreeViewerColumn viewerColumn = createTreeViewerColumn(
 				"Type", 100);
 		viewerColumn.setLabelProvider(new TreeLabelProvider());
 
 		if(showUUID) {
 			viewerColumn = createTreeViewerColumn("id", 75);			
 		} else {
 			viewerColumn = createTreeViewerColumn("id", 0);						
 		}
 		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				if (element instanceof EntryNode) {
 					BaseEntry entry = (BaseEntry) ((EntryNode) element).getData();
 					return entry.uuid;
 				} else {
 					return "";
 				}
 			}
 		});
 
 		if(showTime) {
 			viewerColumn = createTreeViewerColumn("time", 120);			
 		} else {
 			viewerColumn = createTreeViewerColumn("time", 0);						
 		}
 		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				if (element instanceof EntryNode) {
 					BaseEntry entry = (BaseEntry) ((EntryNode) element).getData();
 					return entry.time;
 				} else {
 					return "";
 				}
 			}
 		});
 
 		if(showOS) {
 			viewerColumn = createTreeViewerColumn("os", 50);
 		} else {
 			viewerColumn = createTreeViewerColumn("os", 0);
 		}
 		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				if (element instanceof EntryNode) {
 					BaseEntry entry = (BaseEntry) ((EntryNode) element).getData();
 					return entry.os;
 				} else {
 					return "";
 				}
 			}
 		});
 
 		if(showAuthor) {
 			viewerColumn = createTreeViewerColumn("author", 50);			
 		} else {
 			viewerColumn = createTreeViewerColumn("author", 0);						
 		}
 		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				if (element instanceof EntryNode) {
 					BaseEntry entry = (BaseEntry) ((EntryNode) element).getData();
 					return entry.creater;
 				} else {
 					return "";
 				}
 			}
 		});
 
 		viewerColumn = createTreeViewerColumn("Code file", 80);
 		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				if (element instanceof EntryNode) {
 					BaseEntry entry = (BaseEntry) ((EntryNode) element).getData();
 					if(entry instanceof LinkEntry) {
 						String temp = ((LinkEntry) entry).codefilename;
 						return temp.substring(temp.lastIndexOf('/')+1);
 					}
 				}
 				return "";
 			}
 		});
 
 		viewerColumn = createTreeViewerColumn("Code text", 250);
 		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				if (element instanceof EntryNode) {
 					BaseEntry entry = (BaseEntry) ((EntryNode) element).getData();
 					if(entry instanceof LinkEntry) {
						return ((LinkEntry) entry).codeselpath.getSyntaxCodeText().replace('\n', ' ');						
 					}
 				}
 				return "";
 			}
 		});
 		
 		viewerColumn = createTreeViewerColumn("PDF File", 100);
 		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				if (element instanceof EntryNode) {
 					BaseEntry entry = (BaseEntry) ((EntryNode) element).getData();
 					if(entry instanceof LinkEntry) {
 						String temp = ((LinkEntry) entry).specfilename;
 						return temp.substring(temp.lastIndexOf('/')+1);
 					}
 				}
 				return "";
 			}
 		});
 
 		viewerColumn = createTreeViewerColumn("Page", 30);
 		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				if (element instanceof EntryNode) {
 					BaseEntry entry = (BaseEntry) ((EntryNode) element).getData();
 					if(entry instanceof LinkEntry) {
 						return ((LinkEntry) entry).specselpath.getPage(new String());
 					}
 				}
 				return "";
 			}
 		});
 
 		viewerColumn = createTreeViewerColumn("Spec Text", 250);
 		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				if (element instanceof EntryNode) {
 					BaseEntry entry = (BaseEntry) ((EntryNode) element).getData();
 					if(entry instanceof LinkEntry) {
 						return ((LinkEntry) entry).specselpath.getPDFText(new String()).replace('\n', ' ');						
 					}
 				}
 				return "";
 			}
 		});
 
 		viewerColumn = createTreeViewerColumn("Comments", 200);
 		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				if (element instanceof EntryNode) {
 					BaseEntry entry = (BaseEntry) ((EntryNode) element).getData();
 					if(entry instanceof LinkEntry) {
 						return ((LinkEntry) entry).comment;						
 					}
 				}
 				return "";
 			}
 		});
 		viewerColumn.setEditingSupport(new EditComment(treeViewer));
 		// end NH
 
 		TreeContentProvider contentProvider = new TreeContentProvider();
 		treeViewer.setContentProvider(contentProvider);
 		
 //		TableContentProvider contentProvider = new TableContentProvider();
 //		treeViewer.setContentProvider(contentProvider);
 
 		tree.addListener (SWT.Selection, new Listener () {
 			public void handleEvent (Event event) {
 				ISelection selection = treeViewer.getSelection();
 				if (selection != null && selection instanceof IStructuredSelection) {
 					IStructuredSelection sel = (IStructuredSelection) selection;
 					for (Iterator<EntryNode> iterator = sel.iterator(); iterator.hasNext();) {
 						EntryNode node = iterator.next();
 						selectEntryNodeInEditor(node);
 					}
 				}					
 			}
 		});
 		tree.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				ISelection selection = treeViewer.getSelection();
 				if (selection != null && selection instanceof IStructuredSelection) {
 					IStructuredSelection sel = (IStructuredSelection) selection;
 					for (Iterator<EntryNode> iterator = sel.iterator(); iterator.hasNext();) {
 						EntryNode node = iterator.next();
 						if(node.getData() instanceof LinkEntry) {
 							LinkEntry mp = (LinkEntry) node.getData();
 							String codeFilename = mp.codefilename;
 							String specFilename = mp.specfilename;
 							IPath codepath = new Path(codeFilename);
 							codepath = codepath.removeFirstSegments(1); // remove "project:"
 							IPath specpath = new Path(specFilename);
 							specpath = specpath.removeFirstSegments(1); // remove "project:"
 						    CDCEditor.open(projectname, codepath, specpath);
 						}
 						selectEntryNodeInEditor(node);
 					}
 				}
 				setFocus();
 			}
 		});
 		
 		int operations = DND.DROP_MOVE | DND.DROP_COPY;
 		Transfer[] transferTypes = new Transfer[]{TextTransfer.getInstance()};
 		treeViewer.addDragSupport(operations, transferTypes, new DragSourceListener() {
 			String uuids = null;
 			@Override
 			public void dragStart(DragSourceEvent event) {
 				System.out.println("dragStart");
 				// event.doit = false;
 				ISelection selection = treeViewer.getSelection();
 				if(selection!=null && selection instanceof IStructuredSelection) {
 					IStructuredSelection sel = (IStructuredSelection) selection;
 					Object[] objs = sel.toArray();
 					EntryNode node = (EntryNode) objs[0];
 					uuids = ((BaseEntry) node.getData()).uuid;
 					for(int i=1; i<objs.length; i++) {
 						node = (EntryNode) objs[i];
 						uuids += "/";
 						uuids += ((BaseEntry) node.getData()).uuid;
 					}
 				}
 				System.out.println(uuids);
 				String cdcfilename = CDCEditor.projname2cdcName(projectname);
 				refresh(CDCDataCenter.getInstance().getCategoryTree(cdcfilename));
 			}			
 			@Override
 			public void dragSetData(DragSourceEvent event) {
 				System.out.println("dragSetData");
 				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
 					event.data = uuids; 
 				}
 			}
 			@Override
 			public void dragFinished(DragSourceEvent event) {
 				System.out.println("dragFinished");
 				if(event.detail == DND.DROP_MOVE) {
 					// delete source
 				}
 			}
 		});
 		treeViewer.addDropSupport(operations, transferTypes, new ViewerDropAdapter(treeViewer) {
 			private CategoryEntry category = null;
 			@Override
 			public boolean validateDrop(Object target, int operation, TransferData transferType) {
 				System.out.println("validateDrop");
 				if(target instanceof EntryNode) {
 					EntryNode node = (EntryNode) target;
 					if(node.getData() instanceof CategoryEntry) {
 						return true;
 					}
 				}
 				category = null;
 				return true;
 			}
 			@Override
 			public void drop(DropTargetEvent event) {
 				System.out.println("drop");
 				EntryNode node = null;
 				int location = determineLocation(event);
 				EntryNode target = (EntryNode) determineTarget(event);
 				if(target instanceof EntryNode) {
 					node = (EntryNode) target;
 					if(node.getData() instanceof CategoryEntry) {
 						switch (location){
 						case 3 :
 							// Dropped on the target
 							category = (CategoryEntry) node.getData();
 							break;
 						case 1 :
 							// Dropped before the target
 						case 2 :
 							// Dropped after the target
 						case 4 :
 							// Dropped into nothing
 							break;
 						}
 					}
 				}
 				if(category == null) {
 					MessageDialog.openError(null, "Invalid destination", "Drag the item onto a category please!");					
 				}
 				super.drop(event);
 			}
 			@Override
 			public boolean performDrop(Object data) {
 				if(!(data instanceof String)) {
 					try {
 						throw new Exception("Not the expected type (String)");
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				System.out.println("performDrop: "+data);
 				Vector<String> uuids = new Vector<String>();
 				String tempstr = (String) data;
 				while(tempstr.lastIndexOf('/') != -1) {
 					int index = tempstr.lastIndexOf('/');
 					String uuid = tempstr.substring(index+1);
 					uuids.add(0, uuid);
 					tempstr = tempstr.substring(0,index);
 				}
 				uuids.add(0, tempstr);
 				if(category != null) {
 					String cdcfilename = CDCEditor.projname2cdcName(projectname);
 					for(int i=0; i<uuids.size(); i++) {
 						if(CDCDataCenter.getInstance().parentOf(cdcfilename, category.uuid, uuids.get(i))) {
 							MessageDialog.openError(null, "Invalid destination", "Dragging an ancestor to a decendent!");
 							return true;
 						}
 					}
 					for(int i=0; i<uuids.size(); i++) {
 						CDCDataCenter.getInstance().moveEntry(cdcfilename, uuids.get(i), category.uuid);
 					}
 				}
 				refresh();
 				reselect(uuids.get(uuids.size()-1));
 				return true;
 			}
 		});
 
 		// Layout the viewer
 		GridData gridData = new GridData();
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalSpan = NUM_HORIZONTAL_ELEMENTS;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		treeViewer.getControl().setLayoutData(gridData);
 	}
 
 	// NH
 	private TreeViewerColumn createTreeViewerColumn(String title, int bound) {
 		final TreeViewerColumn viewerColumn = new TreeViewerColumn(treeViewer,
 				SWT.NONE);
 		final TreeColumn column = viewerColumn.getColumn();
 		column.setText(title);
 		column.setWidth(bound);
 		column.setResizable(true);
 		column.setMoveable(true);
 		column.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				sorter.setNeedsort();
 				if(column.getText().equals("Code file")) {
 					sorter.setKey(MapSelectionSort.CODEFILE);
 					sorter.toggleCodefilename();
 				} else if(column.getText().equals("Code text")) {
 					sorter.setKey(MapSelectionSort.CODETEXT);
 					sorter.toggleCodefiletext();
 				} else if(column.getText().equals("PDF File")) {
 					sorter.setKey(MapSelectionSort.SPECFILE);
 					sorter.toggleSpecfilename();
 				} else if(column.getText().equals("Page")) {
 					sorter.setKey(MapSelectionSort.PDFPAGE);
 					sorter.toggleSpecfilepage();
 				} else if(column.getText().equals("Spec Text")) {
 					sorter.setKey(MapSelectionSort.SPECTEXT);
 					sorter.toggleSpecfiletext();
 				} else if(column.getText().equals("Comments")) {
 					sorter.setKey(MapSelectionSort.COMMENT);
 					sorter.toggleComments();
 				}
 				refresh();
 			}
 		});
 		return viewerColumn;
 	}
 
 	public void handleEvent(Event event) {
 		MessageDialog.openError(null, "Alert",  "handleEvent(Event event)");
 	}
 	
 	public ISelection getSelection() {
 		if(tableViewer != null) {
 			return tableViewer.getSelection();			
 		}
 		if(treeViewer != null) {
 			return treeViewer.getSelection();			
 		}
 		return null;
 	}
 	
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		System.out.println("<<<<<-------------------");
 		System.out.println("ReferenceExplorerView.selectionChanged: " + part.getClass().getName());
 
 		if (selection == null) return;
 		System.out.println("ReferenceExplorerView.selectionChanged: " + selection.getClass().getName());
 		
 		IEditorReference[] ef = part.getSite().getPage().getEditorReferences();
 		for(int i=0; i<ef.length; i++) {
 			if(ef[i].getEditor(false) != null)
 				System.out.println("ReferenceExplorerView.selectionChanged: editors: " + ef[i].getEditor(false).getClass().getName());
 		}
 		if(part.getSite().getPage().getActiveEditor() != null)
 			System.out.println("ReferenceExplorerView.selectionChanged: active editor: " + part.getSite().getPage().getActiveEditor().getClass().getName());
 		System.out.println("------------------->>>>>>>>");
 		
 		if(part instanceof ProjectExplorer || part instanceof ResourceNavigator) {
 		    if(selection instanceof IStructuredSelection) {
 		        for(Iterator it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
 		            Object element = it.next();
 		            /* if (element instanceof IFile) {
 		                IFile file = (IFile) element;
 	                	projectname = file.getProject().getName();
 		            } else if(element instanceof IFolder) {
 		            	IFolder folder = (IFolder) element;
 		            	projectname = folder.getProject().getName();
 		            } else if(element instanceof IProject) {
 		            	IProject project = (IProject) element;
 		            	projectname = project.getName();
 		            } */
 	            	if(element instanceof IResource) {
 	            		IResource res = (IResource) element;
 	            		IProject proj = res.getProject();
 		            	if(proj.isOpen() && CDCEditor.isCDCProject(proj.getName())) {
 		            		if(projectname != res.getProject().getName()) {
 				            	projectname = res.getProject().getName();
 				        	    refresh();		            			
 		            		}
 		            	} else {
 		            		projectname = null;
 			        	    refresh();
 		            	}
 		            } else {
 		            	projectname = null;
 		        	    refresh();
 		            }
 		        }
 		    }
 		}/* else if(part instanceof IEditorPart) {
 			EntryEditor editorPart = (EntryEditor) CDCEditor.getActiveEntryEditor();
 			if (editorPart != null) {
 				projectname = editorPart.getProjectName();
 			} else {
 				projectname = null;
 			}
 		}
 		if(projectname != null) {
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 			String cdcfilename = CDCEditor.projname2cdcName(projectname);
 			String codeFilename = CDCDataCenter.getInstance().getLastOpenedCodeFilename(cdcfilename);
 			String specFilename = CDCDataCenter.getInstance().getLastOpenedSpecFilename(cdcfilename);
 			if(codeFilename!=null && specFilename!=null) {
 				IPath codepath = new Path(codeFilename);
 				codepath = codepath.removeFirstSegments(1); // remove "project:"
 				IPath specpath = new Path(specFilename);
 				specpath = specpath.removeFirstSegments(1); // remove "project:"
 					    CDCEditor.open(projectname, codepath, specpath);				
 					}
 			}
 				});
 		}*/
 	
 //		IEditorReference[] editors = part.getSite().getPage().getEditorReferences();
 //		editors[0].getName();
 //		
 //		IViewReference[] views = part.getSite().getPage().getViewReferences();
 //		views[0].getTitle();
 		
 //		IViewReference outlineView = views[6];
 //		IViewPart ov = outlineView.getView(true);
 //		ContentOutline contentOutline = (ContentOutline)ov;
 	}
 
 	@Override
 	public void partActivated(IWorkbenchPartReference partRef) {
 		System.out.println("partActivated!");
 		System.out.println(partRef.getPart(false).getClass().getName());
 		//sniff();
 		//refresh();
 	}
 
 	@Override
 	public void partBroughtToTop(IWorkbenchPartReference partRef) {
 		System.out.println("partBroughtToTop!");
 		System.out.println(partRef.getPart(false).getClass().getName());
 		//sniff();
 		//refresh();
 	}
 
 	@Override
 	public void partClosed(IWorkbenchPartReference partRef) {
 		System.out.println("partClosed!");
 		System.out.println(partRef.getPart(false).getClass().getName());
 		//sniff();
 		//refresh();
 	}
 
 	@Override
 	public void partDeactivated(IWorkbenchPartReference partRef) {
 		System.out.println("partDeactivated!");
 		System.out.println(partRef.getPart(false).getClass().getName());
 		//sniff();
 		//refresh();
 	}
 
 	@Override
 	public void partHidden(IWorkbenchPartReference partRef) {
 		System.out.println("partHidden!");
 		System.out.println(partRef.getPart(false).getClass().getName());
 		//sniff();
 		//refresh();
 	}
 
 	@Override
 	public void partInputChanged(IWorkbenchPartReference partRef) {
 		System.out.println("partInputChanged!");
 		System.out.println(partRef.getPart(false).getClass().getName());
 		//sniff();
 		//refresh();
 	}
 
 	@Override
 	public void partOpened(IWorkbenchPartReference partRef) {
 		System.out.println("partOpened!");
 		System.out.println(partRef.getPart(false).getClass().getName());
 		//sniff();
 		/*try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}*/
 		//refresh();
 	}
 
 	@Override
 	public void partVisible(IWorkbenchPartReference partRef) {
 		System.out.println("partVisible!");
 		System.out.println(partRef.getPart(false).getClass().getName());
 		//sniff();
 		//refresh();
 	}
 	
 	@Override
 	public void resourceChanged(IResourceChangeEvent event) {
 		System.out.println("Resource Changed!");
 		
 		//some possible constants that a flag or kind could be...
 //		int added = IResourceDelta.ADDED;
 //		int changed = IResourceDelta.CHANGED;
 //		int copiedFrom = IResourceDelta.COPIED_FROM;
 //		int movedFrom = IResourceDelta.MOVED_FROM;
 //		int movedTo = IResourceDelta.MOVED_TO;
 //		int localChanged = IResourceDelta.LOCAL_CHANGED;
 //		int noChange = IResourceDelta.NO_CHANGE;
 //		int open = IResourceDelta.OPEN;
 //		int removed = IResourceDelta.REMOVED;
 //		int removedPhantom = IResourceDelta.REMOVED_PHANTOM;
 //		int replaced = IResourceDelta.REPLACED;
 		
 		IResourceDelta workspaceIrd = event.getDelta(); //workspace
 		if (workspaceIrd == null) return;
 		
 		IResourceDelta[] projectsChanged = workspaceIrd.getAffectedChildren();
 		for (IResourceDelta projectIrd : projectsChanged) {
 			int projectIrdKind = projectIrd.getKind(); //project
 			int projectIrdFlag = projectIrd.getFlags();
 			
 			//project is renamed or moved
 			if (projectIrdKind == IResourceDelta.REMOVED && projectIrdFlag == IResourceDelta.MOVED_TO) {
 				IPath projectFullPath = projectIrd.getFullPath();
 				IPath projectMovedToPath = projectIrd.getMovedToPath();
 				break;
 			}
 			//project is removed
 			else if (projectIrdKind == IResourceDelta.REMOVED && projectIrdFlag == IResourceDelta.NO_CHANGE) {
 				IPath projectFullPath = projectIrd.getFullPath();
 				//references.resourceDeltaRemoveProject(projectFullPath);
 				break;
 			}
 			//new project imported, we need to scan to see if there are saved references
 			else if (projectIrdKind == IResourceDelta.ADDED && projectIrdFlag == IResourceDelta.OPEN) {
 				//((EntryEditor) CDCEditor.getActiveEntryEditor()).setDocument(SimpleXML.read());
 				break;
 			}
 			
 			IResourceDelta[] filesChanged = projectIrd.getAffectedChildren();
 			for (IResourceDelta fileIrd : filesChanged) {
 				int fileIrdKind = fileIrd.getKind(); //file
 				int fileIrdFlag = fileIrd.getFlags();
 				
 				//old file renamed or moved
 				if (fileIrdKind == IResourceDelta.REMOVED && fileIrdFlag == IResourceDelta.MOVED_TO) {
 					IPath fileIrdFullPath = fileIrd.getFullPath();
 					IPath fileIrdMovedToPath = fileIrd.getMovedToPath();
 				}
 				//file removed
 				else if (fileIrdKind == IResourceDelta.REMOVED && fileIrdFlag == IResourceDelta.NO_CHANGE) {
 					IPath fileIrdFullPath = fileIrd.getFullPath();
 					//references.resourceDeltaRemoveFile(fileIrdFullPath);
 				}
 			}
 		}
 		//refresh();
 	}
 }
