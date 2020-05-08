 package org.dotplot.ui.views;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.dotplot.tokenizer.IFileList;
 import org.dotplot.ui.DotPlotPerspective;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DragSourceAdapter;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 
 /**
  * <code>DotPlotNavigator</code> allows navigating your file-system and to
  * choose files to plot.
  *
  * @author Sascha Hemminger & Roland Helmrich
  * @see ViewPart
  */
 public class DotPlotNavigator extends ViewPart implements ICheckStateListener
 {
    private boolean dirty;
 
    private CheckboxTreeViewer viewer;
    private Action refreshAction;
 
    public DotPlotNavigator()
    {
       this.dirty = false;
    }
 
    /**
     * called when a state has changed.
     *
     * @param event the event
     *
     * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
     */
    public void checkStateChanged(CheckStateChangedEvent event)
    {
       IWorkbenchWindow window = this.getSite().getWorkbenchWindow();
       DotPlotLister lister = (DotPlotLister) window.getActivePage().findView(DotPlotPerspective.DOTPLOTLIST);
       Object changed = event.getElement();
 
       try
       {
          window.getActivePage().showView(DotPlotPerspective.DOTPLOTLIST);
       }
       catch (PartInitException e)
       {
          e.printStackTrace();
       }
 
       if (event.getSource() == this.viewer)
       {
          if (this.viewer.getChecked(changed))
          {
             markChildren(changed, this.viewer);
          }
          else
          {
             demarkChildren(changed, this.viewer);
          }
          lister.setInputFiles(this.getSelection());
       }
 
       this.dirty = true;
    }
 
    /**
     * creates the control.
     *
     * @param parent the parent
     *
     * @see ViewPart#createPartControl
     */
    public void createPartControl(Composite parent)
    {
       viewer = new CheckboxTreeViewer(parent);
       viewer.setContentProvider(new DotPlotContentProvider(true));
       viewer.setLabelProvider(new DotPlotLabelProvider());
       viewer.addFilter(new DotPlotFilter());
       viewer.setInput(getFileSystemRoot());
       viewer.addCheckStateListener(this);
 
       // TODO window.getActivePage() throws NullPointerException so that under Eclipse 3.x the navigator won't start
 //      IWorkbenchWindow window = getSite().getWorkbenchWindow();
 //      final DotPlotLister lister = (DotPlotLister) window.getActivePage().findView(DotPlotPerspective.DOTPLOTLIST);
 //      initDnD(lister);
 
       viewer.getTree().forceFocus();
       this.createActions();
       this.createToolbar();
    }
 
    /**
     * Will return the root File for the file system.
     * On Windows, there are more than one root, in this case, a special File will
     * be returned that provides access to all Windows-"drives".
     *
     * @return the file system root(s)
     */
    private File getFileSystemRoot()
    {
       // default
       File rootFolder = new File("/");
 
       // On Windows systems, there are more than one root (drives C:, D:, ...)
       final File[] roots = File.listRoots();
       if (roots.length > 1)
       {
          // this special File will provide more than one root, needed under Windows
          rootFolder = new File("/")
          {
             /**
              * for being Serializable
              */
             private static final long serialVersionUID = -612186735884992554L;
 
             public File[] listFiles(FileFilter filter)
             {
                ArrayList v = new ArrayList();
                for (int i = 0; i < roots.length; i++)
                {
                   try
                   {
                      // try to open drive (it could be invalid)
                      String path = roots[i].getCanonicalPath();
 
                      // success, add to list
                      v.add(new File(path)
                      {
                         /**
                          * for being Serializable
                          */
                         private static final long serialVersionUID = -6160781320267803938L;
 
                         // overwrite getName() to let the name appear in the tree
                         public String getName()
                         {
                            return super.toString();
                         }
                      });
                   }
                   catch (IOException ioExc)
                   {
                      // TODO suppress "Drive not ready" message (only with SWT??)
 
                      // drive not ready, ignore
                      continue;
                   }
                }
 
                return (File[]) (v.toArray(new File[0]));
             }
 
             public boolean isDirectory()
             {
                return true;
             }
          };
       }
 
       return rootFolder;
    }
 
    private void initDnD(final DotPlotLister lister)
    {
       DragSource dragSource = new DragSource(this.viewer.getControl(), DND.DROP_COPY | DND.DROP_MOVE);
       DropTarget dropTarget = new DropTarget(this.viewer.getControl(), DND.DROP_COPY | DND.DROP_MOVE);
 
       dragSource.setTransfer(new Transfer[]{FileTransfer.getInstance()});
       dropTarget.setTransfer(new Transfer[]{FileTransfer.getInstance()});
 
       dragSource.addDragListener(new DragSourceAdapter()
       {
          public void dragSetData(DragSourceEvent event)
          {
             event.data = viewer.getSelection();
          }
       });
 
       dropTarget.addDropListener(new DropTargetAdapter()
       {
          //this event occurs when the user releases the mouse over the drop target
          public void drop(DropTargetEvent event)
          {
             DotPlotTable dotPlotTable = lister.getTable();
             Table table = dotPlotTable.getTable();
             String[] saFiles = (String[]) event.data;
             int index = 0;
 
             for (int i = 0; i < saFiles.length; ++i)
             {
                File f = new File(saFiles[i]);
 
                if (!f.exists())
                {
                   continue;
                }
 
                index = table.indexOf((TableItem) event.item);
                dotPlotTable.insertItem(f, index);
                table.update();
             }
          }
       });
    }
 
    /**
     * <code>demarkChildren</code> unchecks all children of a selected element.
     *
     * @param selection an element that could have children
     * @param viewer    the treeviewer you want to manipulate
     */
    private void demarkChildren(Object selection, CheckboxTreeViewer viewer)
    {
       viewer.setAutoExpandLevel(CheckboxTreeViewer.ALL_LEVELS);
       viewer.setSubtreeChecked(selection, false);
    }
 
    /**
     * <code>getFileList</code> creates a IFileList from the selected elements.
     *
     * @return an IFileList with files from the chosen viewer
     *
     * @see org.dotplot.tokenizer.IFileList
     */
    public IFileList getFileList()
    {
       DotPlotFileList actualList = new DotPlotFileList();
       Object[] selection = this.getSelection();
 
       for (int i = 0; i < selection.length; i++)
       {
          if (!((File) selection[i]).isDirectory())
          {
             actualList.add(selection[i]);
          }
       }
 
       return actualList;
    }
 
    /**
     * returns the internal viewer.
     *
     * @return the viewer
     */
    public CheckboxTreeViewer getViewer()
    {
       return viewer;
    }
 
    /**
     * <code>getSelection</code> gives all selected elements from the
     * navigator.
     *
     * @return a vector with the names of the checked elements
     */
    public Object[] getSelection()
    {
       return this.viewer.getCheckedElements();
    }
 
    /**
     * use <code>getSelectionAsString</code> to get a stringrepresentation of
     * the selection.
     *
     * @return stringrepresentations of the selected objects
     *
     * @see java.lang.Object#toString
     */
    public String[] getSelectionAsString()
    {
       Object[] checked = this.getSelection();
       String[] result = new String[checked.length];
 
       for (int i = 0; i < checked.length; ++i)
       {
          result[i] = checked[i].toString();
       }
 
       return result;
    }
 
    /**
     * gives information about the dirty bit.
     *
     * @return true if selection has changed
     */
    public boolean isDirty()
    {
       return dirty;
    }
 
    /**
     * use <code> isEmpty</code> if you want to get to know if any elements are
     * selected.
     *
     * @return true if nothing is selected
     */
    public boolean isEmpty()
    {
       return (viewer.getCheckedElements().length == 0);
    }
 
    /**
     * <code>markChildren</code> checks all children of a selected element.
     *
     * @param selection an element that could have children
     * @param viewer    the treeviewer you want to manipulate
     */
    private void markChildren(Object selection, CheckboxTreeViewer viewer)
    {
       viewer.setAutoExpandLevel(CheckboxTreeViewer.ALL_LEVELS);
       viewer.setSubtreeChecked(selection, true);
    }
 
    /**
     * empty implementation.
     *
     * @see ViewPart#setFocus
     */
    public void setFocus()
    {
    }
 
    /**
     * use this when selection has been plotted.
     */
    public void setNotDirty()
    {
       this.dirty = false;
    }
    
    /**
     * Create toolbar, must be called from createPartControl
     */
    private void createToolbar() {
            IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
            mgr.add(this.refreshAction);
           
    }
    
    /**
     * init the refresh action, must be called from createPartControl
     */
    public void createActions() {
 	  refreshAction = new Action("Refresh!") {
 		   public void run () {
			   viewer.refresh();
 		   }
 	   };
 	   
 	  //TODO Image for the refresh action
 	  //refreshAction.setImageDescriptor()
    }
    
 }
