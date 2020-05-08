 package lapsePlus.views;
 
 /*
 * SinkView.java,version 2.8, 2010
 */
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.text.Collator;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Vector;
 import lapsePlus.LapsePlugin;
 import lapsePlus.XMLConfig;
 import lapsePlus.XMLConfig.DerivationDescription;
 import lapsePlus.views.LapseView.SlicingFromSinkJob;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ClassInstanceCreation;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.internal.ui.JavaPluginImages;
 import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ColumnLayoutData;
 import org.eclipse.jface.viewers.ColumnPixelData;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IColorProvider;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.IFontProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTError;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 public class SinkView extends ViewPart {
 	
 	
     static final String MARKER_ID = "lapse.SQLMarker";
     private static final int MESSAGE_FIELD = 0;
     private static final int TYPE_FIELD = 1;
     private static final int CATEGORY_FIELD = 2;
     private static final int PROJECT_FIELD = 3;
     private static final int FILE_FIELD = 4;
     private static final int LINE_FIELD = 5;
     
     static TableViewer viewer;
     private Action runAction, 
         doubleClickAction, 
         doBackwardPropagationAction, 
         hideSafeAction;
     private Action 
         hideNoSourceAction, 
         statAction, 
         showSQLAction, 
         showXSSAction, 
         showPTAction,
         setSafeAction, 
         copyToClipboardAction,
         showHttpResponseAction,
         showCommandInjectionAction,
         showLDAPAction,
         showXPathAction,
         showRegexAction;
     private Clipboard fClipboard;
     private StatisticsManager statisticsManager = new StatisticsManager();
     
 	static class ViewContentProvider implements IStructuredContentProvider {
 		private StatisticsManager statisticsManager;
 
 		ViewContentProvider(final StatisticsManager statisticsManager) {
 			this.statisticsManager = statisticsManager;
 		}
 		
         Vector<SinkMatch> matches = new Vector<SinkMatch>();
 
         public void addMatch(SinkMatch match) {
             matches.add(match);
             statisticsManager.add(match);
             // TODO: handle markers
             /*
              * try { IMarker marker =
              * match.getResource().createMarker(MARKER_ID);
              * marker.setAttribute(IMarker.MESSAGE, match.getMessage());
              * if(match.getAST() != null){
              * marker.setAttribute(IMarker.CHAR_START,
              * match.getAST().getStartPosition());
              * marker.setAttribute(IMarker.CHAR_END, match.getAST().getLength() +
              * match.getAST().getStartPosition()); }
              * marker.setAttribute(IMarker.SEVERITY, match.isError() ?
              * IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING); } catch
              * (CoreException e) { e.printStackTrace(); }
              */
         }
         
         public StatisticsManager getStatisticsManager() {
         	return statisticsManager;
         }
 
         public void inputChanged(Viewer v, Object oldInput, Object newInput) {
         }
 
         public void dispose() {
         }
 
         public Object[] getElements(Object parent) {
             return matches.toArray();
         }
 
         public int getMatchCount() {
             return matches.size();
         }
 
         public void clearMatches() {
             matches.clear();
         }
     }
     class ViewLabelProvider extends LabelProvider
         implements
             ITableLabelProvider,
             IColorProvider,
             IFontProvider {
         Font fRegular, fBold, fItalic;
 
         ViewLabelProvider() {
             fRegular = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme()
                 .getFontRegistry().get(JFaceResources.DEFAULT_FONT);
             fBold = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry()
                 .getBold(JFaceResources.DIALOG_FONT);
             fItalic = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme()
                 .getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
         }
 
         public String getColumnText(Object obj, int index) {
             SinkMatch match = (SinkMatch) obj;
             String result = null;
             if (index == MESSAGE_FIELD) {
                 result = match.getMessage();
             } else if (index == TYPE_FIELD) {
                 result = match.getType();
             } else if (index == CATEGORY_FIELD) {
                 result = match.getCategory();
             } else if (index == PROJECT_FIELD) {
                 result = match.getProject().getName();
             } else if (index == FILE_FIELD) {
                 result = match.getFileName();
             } else if (index == LINE_FIELD) {
                 result = match.getLineNumber() != -1 ? "" + match.getLineNumber() : "";
             }
             if (result != null) {
                 return result;
             } else {
                 return ""; // "<unknown>";
             }
         }
 
         public Image getColumnImage(Object obj, int index) {
             SinkMatch match = (SinkMatch) obj;
             if (index == 0) {
                 return match.isError()
                     ? JavaPluginImages.get(JavaPluginImages.IMG_OBJS_ERROR)
                     : JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC);
             }
             return null;
         }
 
         public Color getForeground(Object element) {
             Display display = Display.getCurrent();
             SinkMatch match = (SinkMatch) element;
             if (match.isSource() && !match.isSafe()) {
                 return display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
             } else {
                 return display.getSystemColor(SWT.COLOR_GRAY);
             }
         }
 
         public Color getBackground(Object element) {
             return Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
         }
 
         public Font getFont(Object element) {
             SinkMatch match = (SinkMatch) element;
             if (match.isDone()) {
                 return fBold;
             }
             if (match.isSafe()) {
                 return fItalic;
             }
             return fRegular;
         }
     }
 
     /**
      * This is a callback that will allow us to create the viewer and initialize
      * it.
      */
     public void createPartControl(Composite parent) {
         fClipboard = new Clipboard(parent.getDisplay());
         // viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL |
         // SWT.V_SCROLL);
         viewer = new LocationViewer(parent);
         viewer.setContentProvider(new ViewContentProvider(statisticsManager));
         viewer.setLabelProvider(new ViewLabelProvider());
         viewer.setSorter(new ColumnBasedSorter(2));
         viewer.setInput(getViewSite());
         makeActions();
         hookContextMenu();
         viewer.addDoubleClickListener(new IDoubleClickListener() {
             public void doubleClick(DoubleClickEvent event) {
                 doubleClickAction.run();
             }
         });
         viewer.addSelectionChangedListener(new ISelectionChangedListener() {
             public void selectionChanged(SelectionChangedEvent event) {
                 IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                 if (sel != null) {
                     int size = sel.toArray().length;
                     if (size > 0) {
                         int done = 0, safe = 0;
                         Object[] selected = sel.toArray();
                         for (int i = 0; i < selected.length; i++) {
                             SinkMatch vm = (SinkMatch) selected[i];
                             if (vm.isDone()) done++;
                             if (vm.isSafe()) safe++;
                         }
                         IStatusLineManager slManager = getViewSite().getActionBars()
                             .getStatusLineManager();
                         slManager.setMessage("Selected " + size
                             + (size > 1 ? " entries." : " entry.") + done
                             + " elements are checked." + safe + " elements are safe.");
                     }
                 }
             }
         });
         contributeToActionBars();
     }
 
     private void hookContextMenu() {
         MenuManager menuMgr = new MenuManager("#PopupMenu");
         menuMgr.setRemoveAllWhenShown(true);
         menuMgr.addMenuListener(new IMenuListener() {
             public void menuAboutToShow(IMenuManager manager) {
                 SinkView.this.fillContextMenu(manager);
             }
         });
         Menu menu = menuMgr.createContextMenu(viewer.getControl());
         viewer.getControl().setMenu(menu);
         getSite().registerContextMenu(menuMgr, viewer);
     }
 
     private void contributeToActionBars() {
         IActionBars bars = getViewSite().getActionBars();
         // fillLocalPullDown(bars.getMenuManager());
         fillLocalToolBar(bars.getToolBarManager());
         LapseMultiActionGroup group = 
             new LapseCheckboxActionGroup(
                 new IAction[]{
                 hideSafeAction, hideNoSourceAction, showSQLAction, showXSSAction, showPTAction,showHttpResponseAction,showCommandInjectionAction,showLDAPAction,showXPathAction,showRegexAction},
                 new boolean[]{true, true, false, false, false,false,false,false,false});
         group.addActions(bars.getMenuManager());
     }
 
     private void fillContextMenu(IMenuManager manager) {
         
         SinkMatch match = (SinkMatch) ((IStructuredSelection) viewer.getSelection())
             .getFirstElement();
         if (match != null && match.isError()) {
             manager.add(doBackwardPropagationAction);
         }
         manager.add(runAction);
         manager.add(copyToClipboardAction);
         
         // Other plug-ins can contribute there actions here
         manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
     }
 
     private void fillLocalToolBar(IToolBarManager manager) {
         manager.add(runAction);
         manager.add(setSafeAction);
         manager.add(copyToClipboardAction);
         manager.add(new Separator());
         manager.add(statAction);
         // manager.add(hideSafeAction);
         // manager.add(hideNoSourceAction);
     }
 
     private void computeSinks() {
         (new ComputeSinksJob((ViewContentProvider)viewer.getContentProvider(), viewer)).schedule();
     }
 
     /*private Type findLocallyDeclaredType(String argName, CompilationUnit compilationUnit) {
         final Map<String, Type> var2type = new HashMap<String, Type>();
         ASTVisitor visitor = new ASTVisitor() {
             public boolean visit(VariableDeclarationStatement node) {
                 Type type = node.getType();
                 // node.getType()
                 for (Iterator iter = node.fragments().iterator(); iter.hasNext();) {
                     VariableDeclarationFragment frag = (VariableDeclarationFragment) iter.next();
                     SimpleName var = frag.getName();
                     // System.out.println("Storing " +
                     // var.getFullyQualifiedName() + " of type " + type);
                     var2type.put(var.getFullyQualifiedName(), type);
                 }
                 return false;
             }
         };
         compilationUnit.accept(visitor);
         // System.out.println("There are " + var2type.size() + " elements in the
         // map");
         return (Type) var2type.get(argName);
     }*/
 
 //    private Type findLocallyDeclaredType(SimpleName arg, CompilationUnit compilationUnit) {
 //        return findLocallyDeclaredType(arg.getFullyQualifiedName(), compilationUnit);
 //    }
 
     private void makeActions() {
         runAction = new Action() {
             public void run() {
                 computeSinks();
             }
         };
         
         runAction.setText("Find sinks");
         runAction.setToolTipText("Find sinks");
         runAction.setImageDescriptor(JavaPluginImages.DESC_OBJS_JSEARCH);
         setSafeAction = new Action() {
             public void run() {
                 Object[] matches = ((IStructuredSelection) viewer.getSelection()).toArray();
                 for (int i = 0; i < matches.length; i++) {
                     SinkMatch match = (SinkMatch) matches[i];
                     if (match != null) {
                         // toggle the safe status
                         match.setSafe(!match.getSafe());
                         viewer.refresh(match);
                     }
                 }
             }
         };
         
         setSafeAction.setText("Toggle safe status");
         setSafeAction.setToolTipText("Toggle safe status");
         // setSafeAction.setImageDescriptor(JavaPluginImages.DESC_DLCL_FILTER);
         JavaPluginImages.setLocalImageDescriptors(setSafeAction, "clear_co.gif");
         doBackwardPropagationAction = new Action() {
             public void run() {
                 LapseView lapseView = LapsePlugin.getDefault().getLapseView();
                 if (lapseView != null) {
                     SinkMatch match = ((SinkMatch) ((IStructuredSelection) viewer.getSelection()).getFirstElement());
                     ASTNode astNode = match.getAST();
                     SlicingFromSinkJob job = lapseView.getSinkSlicingJob();
                     if(job != null) {
                         ASTNode arg = null;
                         if (astNode instanceof MethodInvocation) {
                             arg = (ASTNode) ((MethodInvocation) astNode).arguments().get(0);
                         } else if(astNode instanceof ClassInstanceCreation)
                         {
                         	arg=(ASTNode)((ClassInstanceCreation) astNode).arguments().get(0);
                         } 
                         else
                         {
                             logError("Unrecognized " + astNode);
                             return;
                         }
                         job.setSink(arg);
                         job.setUnit(match.getUnit());
                         job.setResource(match.getResource());
                         
                         // switch to the provenance tracker view
                         lapseView.refresh();
                         lapseView.setFocus();
                         
                         // run the job					
                         job.schedule();
                     } else {
                         MessageDialog.openError(
                             viewer.getControl().getShell(), 
                             "Error occurred", 
                             "The provenance view is closed. Please open it and try again.");
                     }
                 } else {
                     MessageDialog.openError(
                         viewer.getControl().getShell(), 
                         "Error occurred", 
                         "The provenance view is closed. Please open it and try again.");
                 }
             }
         };
         
         doBackwardPropagationAction.setText("Perform backward propagation from this sink");
         doBackwardPropagationAction.setToolTipText("Perform backward propagation from this sink");
         doBackwardPropagationAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_VIEW_MENU); // TODO
         
         copyToClipboardAction = new CopyMatchViewAction(this, fClipboard);
         copyToClipboardAction.setText("Copy selection to clipboard");
         copyToClipboardAction.setToolTipText("Copy selection to clipboard");
         copyToClipboardAction.setImageDescriptor(JavaPluginImages.DESC_DLCL_COPY_QUALIFIED_NAME);
         doubleClickAction = new Action() {
             public void run() {
                 IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
                 SinkMatch match = (SinkMatch) sel.getFirstElement();
                 try {
                     // System.out.println("Double-clicked on " +
                     // match.getMember().getClass());
                     EditorUtility.openInEditor(match.getMember(), true);
                     if (match.getLineNumber() != -1) {
                         ITextEditor editor = (ITextEditor) EditorUtility.openInEditor(match.getMember());
                         editor.selectAndReveal(match.getAST().getStartPosition(), match.getAST().getLength());
                     }
                 } catch (PartInitException e) {
                     log(e.getMessage(), e);
                 } catch (Exception e) {
                     log(e.getMessage(), e);
                 }
             }
         };
         
         {
             hideSafeAction = new Action("Hide safe vulnerability sinks (const params)", IAction.AS_CHECK_BOX) {
                 boolean hasFilter = false;
                 ViewerFilter filter = new ViewerFilter() {
                     public boolean select(Viewer viewer, Object parentElement, Object element) {
                         SinkMatch match = (SinkMatch) element;
                         return match.isError();
                     }
                 };
 
                 public void run() {
                     if (!hasFilter) {
                         viewer.addFilter(filter);
                         hasFilter = true;
                     } else {
                         viewer.removeFilter(filter);
                         hasFilter = false;
                     }
                 }
             };
             hideSafeAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
             // //////////
             hideNoSourceAction = new Action("Hide vulnerability sinks without source", IAction.AS_CHECK_BOX) {
                 boolean hasFilter = false;
                 ViewerFilter filter = new ViewerFilter() {
                     public boolean select(Viewer viewer, Object parentElement, Object element) {
                         SinkMatch match = (SinkMatch) element;
                         return match.isSource();
                     }
                 };
 
                 public void run() {
                     if (!hasFilter) {
                         viewer.addFilter(filter);
                         hasFilter = true;
                     } else {
                         viewer.removeFilter(filter);
                         hasFilter = false;
                     }
                 }
             };
             hideSafeAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
         }
         {
             showSQLAction = new Action("Show SQL Injection vulnerabilities only", IAction.AS_CHECK_BOX) {
                 boolean hasFilter = false;
                 ViewerFilter filter = new ViewerFilter() {
                     public boolean select(Viewer viewer, Object parentElement, Object element) {
                         SinkMatch match = (SinkMatch) element;
                         return match.getCategory().equalsIgnoreCase("SQL Injection");
                     }
                 };
 
                 public void run() {
                     if (!hasFilter) {
                         viewer.addFilter(filter);
                         hasFilter = true;
                     } else {
                         viewer.removeFilter(filter);
                         hasFilter = false;
                     }
                 }
             };
             showSQLAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
         }
         {
             showXSSAction = new Action("Show Cross-site Scripting vulnerabilities only", IAction.AS_CHECK_BOX) {
                 boolean hasFilter = false;
                 ViewerFilter filter = new ViewerFilter() {
                     public boolean select(Viewer viewer, Object parentElement, Object element) {
                         SinkMatch match = (SinkMatch) element;
                         return match.getCategory().equalsIgnoreCase("Cross-site Scripting");
                     }
                 };
 
                 public void run() {
                     if (!hasFilter) {
                         viewer.addFilter(filter);
                         hasFilter = true;
                     } else {
                         viewer.removeFilter(filter);
                         hasFilter = false;
                     }
                 }
             };
             showXSSAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
         }
         {
             showPTAction = new Action("Show Path Traversal vulnerabilities only", IAction.AS_CHECK_BOX) {
                 boolean hasFilter = false;
                 ViewerFilter filter = new ViewerFilter() {
                     public boolean select(Viewer viewer, Object parentElement, Object element) {
                         SinkMatch match = (SinkMatch) element;
                         return match.getCategory().equalsIgnoreCase("Path Traversal");
                     }
                 };
 
                 public void run() {
                     if (!hasFilter) {
                         viewer.addFilter(filter);
                         hasFilter = true;
                     } else {
                         viewer.removeFilter(filter);
                         hasFilter = false;
                     }
                 }
             };
             showPTAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
         }
         
         {
             showHttpResponseAction = new Action("Show Http Response Splitting vulnerabilities only", IAction.AS_CHECK_BOX) {
                 boolean hasFilter = false;
                 ViewerFilter filter = new ViewerFilter() {
                     public boolean select(Viewer viewer, Object parentElement, Object element) {
                         SinkMatch match = (SinkMatch) element;
                         return match.getCategory().equalsIgnoreCase("HTTP Response Splitting");
                     }
                 };
 
                 public void run() {
                     if (!hasFilter) {
                         viewer.addFilter(filter);
                         hasFilter = true;
                     } else {
                         viewer.removeFilter(filter);
                         hasFilter = false;
                     }
                 }
             };
             showHttpResponseAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
         }
         
         {
             showCommandInjectionAction = new Action("Show Command Injection vulnerabilities only", IAction.AS_CHECK_BOX) {
                 boolean hasFilter = false;
                 ViewerFilter filter = new ViewerFilter() {
                     public boolean select(Viewer viewer, Object parentElement, Object element) {
                         SinkMatch match = (SinkMatch) element;
                         return match.getCategory().equalsIgnoreCase("Command Injection");
                     }
                 };
 
                 public void run() {
                     if (!hasFilter) {
                         viewer.addFilter(filter);
                         hasFilter = true;
                     } else {
                         viewer.removeFilter(filter);
                         hasFilter = false;
                     }
                 }
             };
             showCommandInjectionAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
         }
         
         {
             showLDAPAction = new Action("Show LDAP Injection vulnerabilities only", IAction.AS_CHECK_BOX) {
                 boolean hasFilter = false;
                 ViewerFilter filter = new ViewerFilter() {
                     public boolean select(Viewer viewer, Object parentElement, Object element) {
                         SinkMatch match = (SinkMatch) element;
                         return match.getCategory().equalsIgnoreCase("LDAP Injection");
                     }
                 };
 
                 public void run() {
                     if (!hasFilter) {
                         viewer.addFilter(filter);
                         hasFilter = true;
                     } else {
                         viewer.removeFilter(filter);
                         hasFilter = false;
                     }
                 }
             };
             showLDAPAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
         }
         
         {
             showXPathAction = new Action("Show XPath Injection vulnerabilities only", IAction.AS_CHECK_BOX) {
                 boolean hasFilter = false;
                 ViewerFilter filter = new ViewerFilter() {
                     public boolean select(Viewer viewer, Object parentElement, Object element) {
                         SinkMatch match = (SinkMatch) element;
                         return match.getCategory().equalsIgnoreCase("XPath Injection");
                     }
                 };
 
                 public void run() {
                     if (!hasFilter) {
                         viewer.addFilter(filter);
                         hasFilter = true;
                     } else {
                         viewer.removeFilter(filter);
                         hasFilter = false;
                     }
                 }
             };
             showXPathAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
         }
         
         {
             showRegexAction = new Action("Show Regex Injection vulnerabilities only", IAction.AS_CHECK_BOX) {
                 boolean hasFilter = false;
                 ViewerFilter filter = new ViewerFilter() {
                     public boolean select(Viewer viewer, Object parentElement, Object element) {
                        SinkMatch match = (SinkMatch) element;
                         return match.getCategory().equalsIgnoreCase("Regex Injection");
                     }
                 };
 
                 public void run() {
                     if (!hasFilter) {
                         viewer.addFilter(filter);
                         hasFilter = true;
                     } else {
                         viewer.removeFilter(filter);
                         hasFilter = false;
                     }
                 }
             };
             showRegexAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
         }
         
         
         {
             hideNoSourceAction = new Action("Hide vulnerability sinks without source code", IAction.AS_CHECK_BOX) {
                 boolean hasFilter = false;
                 ViewerFilter filter = new ViewerFilter() {
                     public boolean select(Viewer viewer, Object parentElement, Object element) {
                         SinkMatch match = (SinkMatch) element;
                         return match.isSource();
                     }
                 };
 
                 public void run() {
                     if (!hasFilter) {
                         viewer.addFilter(filter);
                         hasFilter = true;
                     } else {
                         viewer.removeFilter(filter);
                         hasFilter = false;
                     }
                 }
             };
             hideNoSourceAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
         }
         {
             final SinkView sinkView = this;
             statAction = new Action() {
                 public void run() {
                     SinkStatsDialog dialog = new SinkStatsDialog(viewer.getControl().getShell(),
                         sinkView);
                     // dialog.create();
                     dialog.open();
                     dialog.getReturnCode();
                 }
             };
             statAction.setText("Get sink statistics");
             statAction.setToolTipText("Get sink statistics");
             //JavaPluginImages.setLocalImageDescriptors(statAction, "statistics.gif");
             ImageDescriptor desc = JavaPluginImages.DESC_OBJS_LIBRARY;
             statAction.setImageDescriptor(desc);
         }
     }
 
 //    private void showMessage(String message) {
 //        MessageDialog.openInformation(viewer.getControl().getShell(), "List of matches", message);
 //    }
 
     /**
      * Passing the focus request to the viewer's control.
      */
     public void setFocus() {
         viewer.getControl().setFocus();
     }
     class LocationViewer extends TableViewer {
         private final String columnHeaders[] = 
             {
                 "Suspicious call", "Method", "Category",
                 "Project", "File", "Line"
             };
         private ColumnLayoutData columnLayouts[] = {
                 new ColumnPixelData(500),
                 new ColumnWeightData(50), new ColumnWeightData(50), new ColumnWeightData(50),
                 new ColumnWeightData(80), new ColumnWeightData(15)
             };
         //private CheckboxTableViewer checkable;
 
         LocationViewer(Composite parent) {
             super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
             
                     
             getTable().setLinesVisible(true);
             createColumns();
             // is there a way to remove this?..
 //            checkable = CheckboxTableViewer.newCheckList(parent,
 //                SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
 //            checkable.addCheckStateListener(new ICheckStateListener() {
 //                public void checkStateChanged(CheckStateChangedEvent event) {
 //                    ViewMatch match = ((ViewMatch) event.getElement());
 //                    match.setDone(!match.isDone());
 //                    // System.err.println("Refreshing");
 //                    refresh(match);
 //                }
 //            });
         }
 
         private void createColumns() {
             TableLayout layout = new TableLayout();
             getTable().setLayout(layout);
             getTable().setHeaderVisible(true);
             for (int i = 0; i < columnHeaders.length; i++) {
                 layout.addColumnData(columnLayouts[i]);
                 TableColumn tc = new TableColumn(getTable(), SWT.BORDER, i);
                 tc.setResizable(columnLayouts[i].resizable);
                 tc.setText(columnHeaders[i]);
                 tc.pack();
                 final int j = i;
                 tc.addSelectionListener(new SelectionAdapter() {
                     public void widgetSelected(SelectionEvent e) {
                         ViewerSorter oldSorter = viewer.getSorter();
                         if (oldSorter instanceof ColumnBasedSorter) {
                             ColumnBasedSorter sorter = (ColumnBasedSorter) oldSorter;
                             if (sorter.getColumn() == j) {
                                 sorter.toggle();
                                 viewer.refresh();
                                 // System.err.println("Resorting column " + j +
                                 // " in order "
                                 // + sorter.getOrientation());
                                 return;
                             }
                         }
                         viewer.setSorter(new ColumnBasedSorter(j));
                         logError("Sorting column " + j + " in order " + 1);
                         viewer.refresh();
                     }
                 });
             }
         }
 
         /**
          * Attaches a contextmenu listener to the tree
          */
         void initContextMenu(IMenuListener menuListener, String popupId, IWorkbenchPartSite viewSite) {
             MenuManager menuMgr = new MenuManager();
             menuMgr.setRemoveAllWhenShown(true);
             menuMgr.addMenuListener(menuListener);
             Menu menu = menuMgr.createContextMenu(getControl());
             getControl().setMenu(menu);
             viewSite.registerContextMenu(popupId, menuMgr, this);
         }
 
         void clearViewer() {
             setInput(""); //$NON-NLS-1$
         }
     }
 
     class CopyMatchViewAction extends Action {
         // private static final char INDENTATION = '\t'; //$NON-NLS-1$
         private SinkView fView;
         private final Clipboard fClipboard;
 
         public CopyMatchViewAction(SinkView view, Clipboard clipboard) {
             super("Copy matches to clipboard");
             Assert.isNotNull(clipboard);
             fView = view;
             fClipboard = clipboard;
         }
 
         public void run() {
             StringBuffer buf = new StringBuffer();
             addCalls(viewer.getTable().getSelection(), buf);
             TextTransfer plainTextTransfer = TextTransfer.getInstance();
             try {
                 fClipboard.setContents(new String[]{convertLineTerminators(buf.toString())},
                     new Transfer[]{plainTextTransfer});
             } catch (SWTError e) {
                 if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) throw e;
                 if (MessageDialog
                     .openQuestion(fView.getViewSite().getShell(),
                         ("CopyCallHierarchyAction.problem"),
                         ("CopyCallHierarchyAction.clipboard_busy"))) {
                     run();
                 }
             }
         }
 
         private void addCalls(TableItem[] items, StringBuffer buf) {
             for (int i = 0; i < items.length; i++) {
                 TableItem item = items[i];
                 SinkMatch match = (SinkMatch) item.getData();
                 buf.append(match.toLongString());
                 buf.append('\n');
             }
         }
 
         private String convertLineTerminators(String in) {
             StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter);
             StringReader stringReader = new StringReader(in);
             BufferedReader bufferedReader = new BufferedReader(stringReader);
             String line;
             try {
                 while ((line = bufferedReader.readLine()) != null) {
                     printWriter.println(line);
                 }
             } catch (IOException e) {
                 return in; // return the call hierarchy unfiltered
             }
             return stringWriter.toString();
         }
     }
     class ColumnBasedSorter extends ViewerSorter {
         private int columnNum;
         private int orientation = 1;
 
         ColumnBasedSorter(int columnNum, int orientation) {
             super(Collator.getInstance(Locale.getDefault()));
             this.columnNum = columnNum;
             this.orientation = orientation;
         }
 
         ColumnBasedSorter(int columnNum) {
             this(columnNum, 1);
         }
 
         public int category(Object element) {
             SinkMatch match = (SinkMatch) element;
             if (match.isSource()) {
                 return 1;
             } else {
                 return 0;
             }
         }
 
         public int compare(Viewer viewer, Object e1, Object e2) {
             SinkMatch match1 = (SinkMatch) e1;
             SinkMatch match2 = (SinkMatch) e2;
             int result = Integer.MAX_VALUE;
             String s1, s2;
             if (columnNum == MESSAGE_FIELD) {
                 s1 = match1.getMessage();
                 s2 = match2.getMessage();
             } else if (columnNum == TYPE_FIELD) {
                 s1 = match1.getType().toString();
                 s2 = match2.getType().toString();
             } else if (columnNum == CATEGORY_FIELD) {
                 s1 = match1.getCategory();
                 s2 = match2.getCategory();
             } else if (columnNum == PROJECT_FIELD) {
                 s1 = match1.getProject().getProject().getName();
                 s2 = match2.getProject().getProject().getName();
             } else if (columnNum == FILE_FIELD) {
                 s1 = match1.getFileName().toString();
                 s2 = match2.getFileName().toString();
             } else if (columnNum == LINE_FIELD) {
                 s1 = "" + match1.getLineNumber();
                 s2 = "" + match2.getLineNumber();
             } else {
                 logError("Unknown column: " + columnNum);
                 return 0;
             }
             result = orientation * s1.compareToIgnoreCase(s2);
          
             return result;
         }
 
         public void toggle() {
             orientation = orientation * -1;
         }
 
         public int getColumn() {
             return this.columnNum;
         }
 
         public int getOrientation() {
             return orientation;
         }
     }
 
     public StatisticsManager getStatisticsManager() {
         return statisticsManager;
     }
     
     private static void log(String message, Throwable e) {
         LapsePlugin.trace(LapsePlugin.SINK_DEBUG, "Sink view: " + message, e);
     }
     
     private static void log(String message) {
         log(message, null);
     }
     
     static void logError(String message) {
         log(message, new Throwable());
     }
     
     public static boolean isDerivationName(String identifier) {
     	
 		Collection<DerivationDescription> derivators = XMLConfig.readDerivators("derived.xml");
 		
 		for(Iterator<DerivationDescription> iter = derivators.iterator(); iter.hasNext(); ){
 			XMLConfig.DerivationDescription derivationDesc = (XMLConfig.DerivationDescription) iter.next();
 			int i=derivationDesc.getMethodName().lastIndexOf('.');
 			String sub=derivationDesc.getMethodName().substring(i+1);
 			if(sub.equals(identifier)){
 				return true;
 			}
 		}
 	
 		// none matched
 		return false;
 	}
 }
