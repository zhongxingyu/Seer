 package ralfstx.mylyn.bugview.internal;
 
 import java.util.Collection;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.mylyn.tasks.core.ITask;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.part.ViewPart;
 
 
 public class BugView extends ViewPart {
 
   static final int COL_ID = 0;
   static final int COL_TITLE = 1;
 
   private TableViewer viewer;
 
   @Override
   public void createPartControl( Composite parent ) {
     parent.setLayout( GridLayoutFactory.fillDefaults().create() );
     createTableViewer( parent );
     refreshViewer();
     makeActions();
   }
 
   @Override
   public void setFocus() {
     viewer.getControl().setFocus();
   }
 
   private void makeActions() {
     ImageDescriptor refreshImage = Activator.getImageDescriptor( "/icons/refresh.gif" );
     IAction refreshAction = new Action( "Refresh", refreshImage ) {
       @Override
       public void run() {
         refreshViewer();
       }
     };
     getViewSite().getActionBars().getToolBarManager().add( refreshAction );
   }
 
   private void createTableViewer( Composite parent ) {
     Table table = new Table( parent, SWT.VIRTUAL );
     table.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
     table.setLinesVisible( true );
     new TableColumn( table, SWT.LEFT ).setWidth( 80 );
     new TableColumn( table, SWT.LEFT ).setWidth( 400 );
     viewer = new TableViewer( table );
     viewer.setLabelProvider( new TaskLabelProvider() );
     viewer.setContentProvider( new ArrayContentProvider() );
   }
 
  private void refreshViewer() {
     Collection<ITask> tasks = MylynBridge.getAllTasks();
     viewer.setInput( tasks );
    updateStatusBar();
  }

  private void updateStatusBar() {
    String message = null;
    Object input = viewer.getInput();
    if( input instanceof Collection ) {
      Collection<?> collection = (Collection<?>)input;
      message = collection.size() + " bugs";
    }
    IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
    statusLineManager.setMessage( message );
   }
 
   static class TaskLabelProvider extends LabelProvider implements ITableLabelProvider {
 
     private static final ImageDescriptor TASK_ICON = Activator.getImageDescriptor( "/icons/task.gif" );
 
     public Image getColumnImage( Object element, int columnIndex ) {
       Image result = null;
       if( element instanceof ITask ) {
         if( columnIndex == BugView.COL_ID ) {
           result = TASK_ICON.createImage();
         }
       }
       return result;
     }
 
     public String getColumnText( Object element, int columnIndex ) {
       String result = null;
       if( element instanceof ITask ) {
         ITask task = (ITask)element;
         if( columnIndex == BugView.COL_ID ) {
           result = task.getTaskId();
         } else if( columnIndex == BugView.COL_TITLE ) {
           result = task.getSummary();
         }
       }
       return result;
     }
 
   }
 
 }
