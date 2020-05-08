 package com.gratex.perconik.activity.listeners;
 
 import static com.gratex.perconik.activity.DataTransferObjects.setApplicationData;
 import static com.gratex.perconik.activity.DataTransferObjects.setEventData;
 import static com.gratex.perconik.activity.DataTransferObjects.setProjectData;
 import static sk.stuba.fiit.perconik.eclipse.core.resources.ResourceEventType.POST_CHANGE;
 import static sk.stuba.fiit.perconik.eclipse.core.resources.ResourceEventType.PRE_CLOSE;
 import static sk.stuba.fiit.perconik.eclipse.core.resources.ResourceEventType.PRE_DELETE;
 import static sk.stuba.fiit.perconik.eclipse.core.resources.ResourceEventType.PRE_REFRESH;
 import java.util.Set;
 import javax.annotation.concurrent.GuardedBy;
 import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPart;
import sk.stuba.fiit.perconik.core.java.JavaElements;
 import sk.stuba.fiit.perconik.core.listeners.SelectionListener;
 import sk.stuba.fiit.perconik.eclipse.core.resources.ResourceEventType;
 import sk.stuba.fiit.perconik.eclipse.ui.Editors;
 import com.google.common.collect.ImmutableSet;
 import com.gratex.perconik.activity.ActivityServices;
 import com.gratex.perconik.activity.ActivityServices.WatcherServiceOperation;
 import com.gratex.perconik.services.activity.IVsActivityWatcherService;
 import com.gratex.perconik.services.activity.IdeDocumentOperationDto;
 import com.gratex.perconik.services.activity.IdeDocumentOperationTypeEnum;
 
 /**
  * A listener of {@code IdeDocumentOperation} events. This listener creates
  * {@link IdeDocumentOperationDto} data transfer objects and passes them to
  * the <i>Activity Watcher Service</i> to be transferred into the
  * <i>User Activity Client Application</i> for further processing.
  * 
  * <p> TODO document how DTOs are build and what data they contain
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 public final class IdeDocumentListener extends IdeListener implements SelectionListener
 {
 	private final Object lock = new Object();
 	
 	@GuardedBy("lock")
 	private IFile file;
 	
 	public IdeDocumentListener()
 	{
 	}
 	
 	private final boolean updateFile(final IFile file)
 	{
 		if (file != null)
 		{
 			synchronized (this.lock)
 			{
 				if (!file.equals(this.file))
 				{
 					this.file = file;
 					
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	// TODO impl
 
 	static final void process(final IFile file, final IdeDocumentOperationTypeEnum type)
 	{
 		final IdeDocumentOperationDto data = new IdeDocumentOperationDto();
 
 		data.setOperationType(type);
 		
 		setProjectData(data, file.getProject());
 		setApplicationData(data);
 		setEventData(data);
 		
 		// TODO rm
 		System.out.println("DOCUMENT: " + file.getFullPath() + " operation: " + type);
 		
 		ActivityServices.performWatcherServiceOperation(new WatcherServiceOperation()
 		{
 			public final void perform(final IVsActivityWatcherService service)
 			{
 				service.notifyIdeDocumentOperation(data);
 			}
 		});
 	}
 
 	public final void selectionChanged(final IWorkbenchPart part, final ISelection selection)
 	{
 		IFile file = null;

 		if (selection instanceof StructuredSelection)
 		{
 			Object element = ((StructuredSelection) selection).getFirstElement();
 
 			if (element instanceof IFile)
 			{
 				file = (IFile) element;
 			}
			
			if (element instanceof IJavaElement)
			{
				IResource resource = JavaElements.resource((IJavaElement) element);

				if (resource instanceof IFile)
				{
					file = (IFile) resource;
				}
			}
 		}
 		
 		if (file == null && part instanceof IEditorPart)
 		{
 			file = Editors.getFile((IEditorPart) part);
 		}
 		
 		if (this.updateFile(file))
 		{
 			process(file, IdeDocumentOperationTypeEnum.SWITCH_TO);
 		}
 	}
 
 	public final Set<ResourceEventType> getEventTypes()
 	{
 		// TODO rm
 		//return ImmutableSet.of(ResourceEventType.POST_CHANGE);
 		//return EnumSet.allOf(ResourceEventType.class);
 		
 		return ImmutableSet.of(PRE_CLOSE, PRE_DELETE, PRE_REFRESH, POST_CHANGE);
 	}
 }
