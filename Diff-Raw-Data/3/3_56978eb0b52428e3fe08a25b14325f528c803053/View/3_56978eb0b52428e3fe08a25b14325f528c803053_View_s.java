 package codetrails.ui;
 
 import java.util.Arrays;
 
 import net.miginfocom.swt.MigLayout;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.internal.ui.util.CoreUtility;
 import org.eclipse.jdt.ui.IPackagesViewPart;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.util.OpenStrategy;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.model.IWorkbenchAdapter;
 import org.eclipse.ui.navigator.CommonNavigator;
 import org.eclipse.ui.navigator.CommonViewer;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.preferences.WorkingCopyManager;
 import org.eclipse.ui.statushandlers.StatusManager;
 import org.osgi.service.prefs.BackingStoreException;
 
 import codetrails.Activator;
 import codetrails.PluginImages;
 import codetrails.data.Trail;
 import codetrails.data.TrailEntry;
 import codetrails.model.ViewContentProvider;
 import codetrails.ui.actions.ModifyIndexAction;
 import codetrails.ui.actions.RenameTrailAction;
 
 @SuppressWarnings("restriction")
 public class View extends ViewPart implements ISelectionListener
 {
   private IProject lastProject = null;
   private TreeViewer viewer;
   private ViewContentProvider contentProvider;
   private Composite labelHolder;
   public enum Direction
   {
     UP,
     DOWN;
   }
 
   @Override
   public void createPartControl(Composite parent)
   {
     parent.setLayout(new FillLayout());
 
     if (isTaskTagsConfigured())
     {
       createUI(parent);
     }
     else
     {
       createWarning(parent);
     }
   }
 
   private void createUI(Composite parent)
   {
     // Startup content provider
     contentProvider = new ViewContentProvider();
 
     // Create content viewer
     Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
     viewer = new TreeViewer(tree);
     viewer.getTree().setLinesVisible(true);
     viewer.setContentProvider(contentProvider);
     viewer.setLabelProvider(contentProvider);
     viewer.setInput(ViewContentProvider.LOADING_INPUT);
 
     viewer.addDoubleClickListener(new IDoubleClickListener()
     {
       @Override
       public void doubleClick(DoubleClickEvent event)
       {
         ISelection selection = event.getSelection();
         if (selection instanceof ITreeSelection)
         {
           ITreeSelection treeSelection = (ITreeSelection) selection;
           Object firstElement = treeSelection.getFirstElement();
 
           if ((firstElement != null) && (firstElement instanceof TrailEntry))
           {
             // Selected a TrailEntry
             TrailEntry entry = (TrailEntry) firstElement;
             IMarker marker = entry.getMarker();
 
             if (marker != null && marker.getResource() instanceof IFile)
             {
               // Open the marker
               try
               {
                 IDE.openEditor(getSite().getPage(), marker, OpenStrategy.activateOnOpen());
               }
               catch (PartInitException e)
               {
                 StatusManager.getManager().handle(e, Activator.PLUGIN_ID);
               }
             }
           }
         }
       }
     });
 
     // Prepare context menu
     final IShellProvider shellProvider = getViewSite();
     viewer.addSelectionChangedListener(new ISelectionChangedListener()
     {
       @Override
       public void selectionChanged(SelectionChangedEvent event)
       {
         viewer.getControl().setMenu(null);
         if (event.getSelection() instanceof IStructuredSelection)
         {
           IStructuredSelection selection = (IStructuredSelection) event
               .getSelection();
           Object o = selection.getFirstElement();
 
           if (o instanceof Trail)
           {
             Trail trail = (Trail) o;
             MenuManager menuMgr = new MenuManager();
             Menu menu = menuMgr.createContextMenu(viewer.getControl());
             viewer.getControl().setMenu(menu);
             getSite().registerContextMenu(menuMgr, viewer);
 
             menuMgr.add(new RenameTrailAction(shellProvider, trail));
             contentProvider.acceptIndexChanges();
           }
           else if (o instanceof TrailEntry)
           {
             TrailEntry trailEntry = (TrailEntry) o;
             MenuManager menuMgr = new MenuManager();
             Menu menu = menuMgr.createContextMenu(viewer.getControl());
             viewer.getControl().setMenu(menu);
             getSite().registerContextMenu(menuMgr, viewer);
 
             if (trailEntry.index > 0)
             {
               menuMgr.add(new ModifyIndexAction(viewer, contentProvider, Direction.UP));
             }
             menuMgr.add(new ModifyIndexAction(viewer, contentProvider, Direction.DOWN));
           }
         }
       }
     });
 
     IActionBars actionBars = getViewSite().getActionBars();
     actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), new Action()
     {
       @Override
       public void run()
       {
         ISelection viewSelection = viewer.getSelection();
         if (viewSelection instanceof IStructuredSelection)
         {
           IStructuredSelection selection = (IStructuredSelection)viewSelection;
           Object o = selection.getFirstElement();
 
           if (o instanceof Trail)
           {
             Trail trail = (Trail) o;
             new RenameTrailAction(shellProvider, trail).run();
           }
         }
       }
     });
 
     final IToolBarManager toolBarManager = actionBars.getToolBarManager();
 
     toolBarManager.add(new Action()
     {
       {
         setChecked(false);
       }
 
       @Override
       public String getText()
       {
         if (isChecked())
           return "Hide Numbers";
         else
           return "Show Numbers";
       }
 
       @Override
       public ImageDescriptor getImageDescriptor()
       {
         return PluginImages.getImageDescriptor(PluginImages.SHOW_NUMBERS_ICON);
       }
 
       @Override
       public void run()
       {
         contentProvider.setShowNumbers(isChecked());
       }
     });
 
     toolBarManager.add(new Separator());
 
     toolBarManager.add(new Action()
     {
       @Override
       public String getText()
       {
         return "Expand All";
       }
 
       @Override
       public ImageDescriptor getImageDescriptor()
       {
         return PluginImages.getImageDescriptor(PluginImages.EXPAND_ALL_ICON);
       }
 
       @Override
       public void run()
       {
         viewer.expandAll();
       }
     });
     toolBarManager.add(new Action()
     {
       @Override
       public String getText()
       {
         return "Collapse All";
       }
 
       @Override
       public ImageDescriptor getImageDescriptor()
       {
         return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_COLLAPSEALL);
       }
 
       @Override
       public void run()
       {
         viewer.collapseAll();
       }
     });
 
     toolBarManager.add(new Separator());
 
     IAction refreshAction = new Action()
     {
       @Override
       public String getText()
       {
         return "Refresh Project";
       }
 
       @Override
       public ImageDescriptor getImageDescriptor()
       {
         return PluginImages.getImageDescriptor(PluginImages.REFRESH_ICON);
       }
 
       @Override
       public void run()
       {
         contentProvider.refresh();
       }
     };
     toolBarManager.add(refreshAction);
     actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
 
     viewer.getTree().addKeyListener(new KeyAdapter()
     {
       @Override
       public void keyPressed(KeyEvent e)
       {
         if ((e.stateMask & SWT.CTRL) != 0)
         {
           if (e.keyCode == SWT.ARROW_UP)
           {
             new ModifyIndexAction(viewer, contentProvider, Direction.UP).run();
           }
           else if (e.keyCode == SWT.ARROW_DOWN)
           {
             new ModifyIndexAction(viewer, contentProvider, Direction.DOWN).run();
           }
         }
       }
     });
 
     // Register to be notified about selections
     getSite().getWorkbenchWindow().getSelectionService()
         .addPostSelectionListener(this);
 
     // Setup title
     setContentDescription("Select a Project...");
     detectCurrentSelection();
 
     // Active page
     IWorkbenchPage page = getSite().getPage();
     page.addPartListener(new IPartListener2()
     {
       @Override
       public void partVisible(IWorkbenchPartReference partRef) {/* do nothing*/}
       @Override
       public void partOpened(IWorkbenchPartReference partRef) {/* do nothing*/}
       @Override
       public void partInputChanged(IWorkbenchPartReference partRef) {/* do nothing*/}
       @Override
       public void partHidden(IWorkbenchPartReference partRef) {/* do nothing*/}
       @Override
       public void partDeactivated(IWorkbenchPartReference partRef)
       {
         if ("codetrails.TrailsList".equals(partRef.getId()))
         {
           contentProvider.acceptIndexChanges();
         }
       }
       @Override
       public void partClosed(IWorkbenchPartReference partRef) {/* do nothing*/}
       @Override
       public void partBroughtToTop(IWorkbenchPartReference partRef) {/* do nothing*/}
       @Override
       public void partActivated(IWorkbenchPartReference partRef) {/* do nothing*/}
     });
   }
 
   private void createWarning(Composite parent)
   {
     labelHolder = new Composite(parent, SWT.NONE);
 
     MigLayout labelLayout = new MigLayout("fill", "[grow, align center]", "[grow][][][][grow]");
     labelHolder.setLayout(labelLayout);
 
     Label warningLabel1 = new Label(labelHolder, SWT.WRAP | SWT.CENTER);
     warningLabel1.setText("Warning: Code Trails requires a \"TRAIL\" task tag.");
     warningLabel1.setLayoutData("cell 0 1, wmin 0");
 
     Label warningLabel2 = new Label(labelHolder, SWT.WRAP | SWT.CENTER);
     warningLabel2.setText("Adding this task tag will require a workspace rebuild.");
     warningLabel2.setLayoutData("cell 0 2, wmin 0");
 
     Button enableButton = new Button(labelHolder, SWT.NONE);
     enableButton.setText("Enable Code Trails");
     enableButton.addSelectionListener(new SelectionAdapter()
     {
       @Override
       public void widgetSelected(SelectionEvent e)
       {
         enableTaskTagSettings();
 
         if (labelHolder != null)
         {
           Composite parent = labelHolder.getParent();
           labelHolder.dispose();
           labelHolder = null;
           createPartControl(parent);
         }
       }
     });
     enableButton.setLayoutData("cell 0 3");
   }
 
   private boolean isTaskTagsConfigured()
   {
     IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("/instance/org.eclipse.jdt.core");
 
     String tagsCfg = prefs.get(JavaCore.COMPILER_TASK_TAGS, "");
 
     String[] tags = tagsCfg.split(",");
     return Arrays.asList(tags).contains("TRAIL");
   }
 
   private void enableTaskTagSettings()
   {
     WorkingCopyManager wcManager = new WorkingCopyManager();
 
     IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("/instance/org.eclipse.jdt.core");
     IEclipsePreferences wcPref = wcManager.getWorkingCopy(prefs);
 
     boolean tagsCfgChanged = false;
     String tagsCfg = prefs.get(JavaCore.COMPILER_TASK_TAGS, "");
     String tagspriosCfg = prefs.get(JavaCore.COMPILER_TASK_PRIORITIES, "");
 
     String[] tags = tagsCfg.split(",");
     if (!Arrays.asList(tags).contains("TRAIL"))
     {
       tagsCfgChanged = true;
       if (tags.length == 0)
       {
         tagsCfg = "TRAIL";
         tagspriosCfg = JavaCore.COMPILER_TASK_PRIORITY_NORMAL;
       }
       else
       {
         tagsCfg += ",TRAIL";
         tagspriosCfg += "," + JavaCore.COMPILER_TASK_PRIORITY_NORMAL;
       }
     }
 
     if (tagsCfgChanged)
     {
       wcPref.put(JavaCore.COMPILER_TASK_TAGS, tagsCfg);
       wcPref.put(JavaCore.COMPILER_TASK_PRIORITIES, tagspriosCfg);
       try
       {
         wcPref.flush();
 
         // Call into JDT internals to schedule a full workspace build
         CoreUtility.getBuildJob(null).schedule();
       }
       catch (BackingStoreException e)
       {
         IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                     "Failed to update config", e);
         StatusManager.getManager().handle(status);
       }
     }
   }
 
   @Override
   public void setFocus()
   { /* Do nothing */
   }
 
   private void detectCurrentSelection()
   {
     IProject project = null;
 
     IWorkbenchWindow window = getSite().getWorkbenchWindow();
     IWorkbenchPage activePage = window.getActivePage();
 
     if (activePage != null)
     {
       IEditorPart part = activePage.getActiveEditor();
 
       if (part != null)
       {
         project = convertSelection(part);
       }
       else
       {
         IViewReference[] viewReferences = activePage.getViewReferences();
 
         for (IViewReference viewRef : viewReferences)
         {
           IViewPart view = viewRef.getView(false);
           ISelection selection = null;
 
           if (view instanceof IPackagesViewPart)
           {
             IPackagesViewPart viewPart = (IPackagesViewPart) view;
             TreeViewer treeViewer = viewPart.getTreeViewer();
             selection = treeViewer.getSelection();
           }
           else if (view instanceof CommonNavigator)
           {
             CommonNavigator navigator = (CommonNavigator) view;
             CommonViewer commonViewer = navigator.getCommonViewer();
             selection = commonViewer.getSelection();
           }
 
           if (selection instanceof IStructuredSelection)
           {
             IStructuredSelection structuredSelection = (IStructuredSelection) selection;
 
             project = convertSelection(structuredSelection);
 
             if (project != null)
               break;
           }
         }
       }
     }
 
     setProject(project);
   }
 
   @Override
   public void selectionChanged(IWorkbenchPart part, ISelection selection)
   {
     IProject project = convertSelection(part, selection);
     setProject(project);
   }
 
   private void setProject(IProject project)
   {
     if ((project != null) && (!project.equals(lastProject)))
     {
       // System.out.println("Selected project: " + project);
       this.lastProject = project;
       viewer.setInput(project);
       setContentDescription(project.getName());
     }
   }
 
   private IProject convertSelection(IWorkbenchPart part, ISelection selection)
   {
     IProject project = null;
     if (selection instanceof IStructuredSelection)
     {
       IStructuredSelection structuredSelection = (IStructuredSelection) selection;
       project = convertSelection(structuredSelection);
     }
     else if (selection instanceof ITextSelection)
     {
       if (part instanceof IEditorPart)
       {
         IEditorPart editorPart = (IEditorPart) part;
         IResource resource = (IResource)editorPart.getEditorInput().getAdapter(IResource.class);
         if (resource != null)
         {
           project = resource.getProject();
         }
       }
     }
 
     return project;
   }
 
   private IProject convertSelection(IEditorPart part)
   {
     IProject project = null;
     IResource resource = (IResource)part.getEditorInput().getAdapter(IResource.class);
     if (resource != null)
     {
       project = resource.getProject();
     }
     return project;
   }
 
   private IProject convertSelection(IStructuredSelection structuredSelection)
   {
     IProject project = null;
     Object element = structuredSelection.getFirstElement();
 
     if (element instanceof IResource)
     {
       project = ((IResource) element).getProject();
     }
     else if (element instanceof IJavaElement)
     {
       IJavaElement javaElement = (IJavaElement) element;
       project = javaElement.getJavaProject().getProject();
     }
     else if (element instanceof IAdaptable)
     {
       IAdaptable adaptable = (IAdaptable) element;
       IWorkbenchAdapter adapter = (IWorkbenchAdapter) adaptable.getAdapter(IWorkbenchAdapter.class);
       if (adapter != null)
       {
         Object parent = adapter.getParent(adaptable);
         if (parent instanceof IJavaProject)
         {
           IJavaProject javaProject = (IJavaProject) parent;
           project = javaProject.getProject();
         }
       }
     }
 
     return project;
   }
 
   @Override
   public void dispose()
   {
     // Stop listening for selection changes
     getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(this);
 
     // Dispose of all of our content
     if (contentProvider != null)
     {
       contentProvider.dispose();
     }
 
     if (labelHolder != null)
     {
       labelHolder.dispose();
     }
 
     super.dispose();
   }
 }
