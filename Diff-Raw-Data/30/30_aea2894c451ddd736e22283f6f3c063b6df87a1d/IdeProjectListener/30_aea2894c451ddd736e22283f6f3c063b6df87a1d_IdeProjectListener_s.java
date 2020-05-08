 package com.gratex.perconik.activity.ide.listeners;
 
 import static com.gratex.perconik.activity.ide.IdeActivityServices.performWatcherServiceOperation;
 import static com.gratex.perconik.activity.ide.IdeDataTransferObjects.setApplicationData;
 import static com.gratex.perconik.activity.ide.IdeDataTransferObjects.setEventData;
 import static com.gratex.perconik.activity.ide.IdeDataTransferObjects.setProjectData;
 import static com.gratex.perconik.activity.ide.listeners.Utilities.currentTime;
 import static sk.stuba.fiit.perconik.eclipse.core.resources.ResourceDeltaFlag.OPEN;
 import static sk.stuba.fiit.perconik.eclipse.core.resources.ResourceDeltaKind.ADDED;
 import static sk.stuba.fiit.perconik.eclipse.core.resources.ResourceEventType.POST_CHANGE;
 import static sk.stuba.fiit.perconik.eclipse.core.resources.ResourceEventType.PRE_CLOSE;
 import static sk.stuba.fiit.perconik.eclipse.core.resources.ResourceEventType.PRE_DELETE;
 import static sk.stuba.fiit.perconik.eclipse.core.resources.ResourceEventType.PRE_REFRESH;
 import static sk.stuba.fiit.perconik.eclipse.core.resources.ResourceType.PROJECT;
 import java.util.Set;
 import javax.annotation.concurrent.GuardedBy;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPart;
 import sk.stuba.fiit.perconik.core.listeners.ResourceListener;
 import sk.stuba.fiit.perconik.core.listeners.SelectionListener;
 import sk.stuba.fiit.perconik.eclipse.core.resources.AbstractResourceDeltaVisitor;
 import sk.stuba.fiit.perconik.eclipse.core.resources.Projects;
 import sk.stuba.fiit.perconik.eclipse.core.resources.ResourceDeltaFlag;
 import sk.stuba.fiit.perconik.eclipse.core.resources.ResourceDeltaKind;
 import sk.stuba.fiit.perconik.eclipse.core.resources.ResourceEventType;
 import sk.stuba.fiit.perconik.eclipse.core.resources.ResourceType;
 import com.google.common.collect.ImmutableSet;
 import com.gratex.perconik.activity.ide.IdeActivityServices.WatcherServiceOperation;
 import com.gratex.perconik.services.IVsActivityWatcherService;
 import com.gratex.perconik.services.uaca.vs.IdeProjectOperationDto;
 import com.gratex.perconik.services.uaca.vs.IdeProjectOperationTypeEnum;
 
 /**
  * A listener of {@code IdeProjectOperation} events. This listener creates
  * {@link IdeProjectOperationDto} data transfer objects and passes them to
  * the <i>Activity Watcher Service</i> to be transferred into the
  * <i>User Activity Client Application</i> for further processing.
  * 
 * <p> TODO document how DTOs are build and what data they contain
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 public final class IdeProjectListener extends IdeListener implements ResourceListener, SelectionListener
 {
 	// TODO rename not implemented
 	// TODO switch to --> explorer/a editor/b/file explorer/b --> generates switch-to(a,b,a,b)
 	
 	private final Object lock = new Object();
 	
 	@GuardedBy("lock")
 	private IProject project;
 	
 	public IdeProjectListener()
 	{
 	}
 
 	private final boolean updateProject(final IProject project)
 	{
 		if (project != null)
 		{
 			synchronized (this.lock)
 			{
 				if (!project.equals(this.project))
 				{
 					this.project = project;
 					
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	static final void send(final IdeProjectOperationDto data)
 	{
 		performWatcherServiceOperation(new WatcherServiceOperation()
 		{
 			public final void perform(final IVsActivityWatcherService service)
 			{
 				service.notifyIdeProjectOperation(data);
 			}
 		});
 	}
 	
 	static final IdeProjectOperationDto build(final long time, final IProject project, final IdeProjectOperationTypeEnum type)
 	{
 		final IdeProjectOperationDto data = new IdeProjectOperationDto();
 
 		data.setOperationType(type);
 		
 		setProjectData(data, project);
 		setApplicationData(data);
 		setEventData(data, time);
 		
 		if (Debug.enabled()) Debug.message().appendln("project: " + project.getFullPath() + " operation: " + type).appendTo(console);
 		
 		return data;
 	}
 	
 	private static final class ResourceDeltaVisitor extends AbstractResourceDeltaVisitor
 	{
 		private final long time;
 		
 		private final ResourceEventType type;
 		
 		ResourceDeltaVisitor(final long time, final ResourceEventType type)
 		{
 			assert time >= 0;
 			assert type != null;
 			
 			this.time = time;
 			this.type = type;
 		}
 
 		@Override
 		protected final boolean resolveDelta(final IResourceDelta delta, final IResource resource)
 		{
 //			// TODO rm
 //			if (IdeApplication.getInstance().isDebug()) { console.put("resource: "+ resource);
 //			console.put("  type: "+ this.type);console.put("  kind: "+ ResourceDeltaKind.valueOf(delta.getKind()).toString());
 //			console.print("  flags: "+ResourceDeltaFlag.setOf(delta.getFlags()).toString()); }
 
 			assert delta != null && resource != null;
 			
 			if (ResourceType.valueOf(resource.getType()) != PROJECT)
 			{
 				return true;
 			}
 			
 			if (this.type == POST_CHANGE)
 			{
 				IProject project = (IProject) resource;
 				
 				ResourceDeltaKind      kind  = ResourceDeltaKind.valueOf(delta.getKind());
 				Set<ResourceDeltaFlag> flags = ResourceDeltaFlag.setOf(delta.getFlags());
 				
 				if (kind == ADDED) send(build(this.time, project, IdeProjectOperationTypeEnum.ADD));
 				if (flags.contains(OPEN) && project.isOpen()) send(build(this.time, project, IdeProjectOperationTypeEnum.OPEN));
 				
 				return false;
 			}
 			
 			return this.resolveResource(resource);
 		}
 
 		@Override
 		protected final boolean resolveResource(final IResource resource)
 		{
 			assert ResourceType.valueOf(resource.getType()) == PROJECT;
 			
 			switch (this.type)
 			{
 				case PRE_CLOSE:
 					send(build(this.time, (IProject) resource, IdeProjectOperationTypeEnum.CLOSE));
 					break;
 
 				case PRE_DELETE:
 					send(build(this.time, (IProject) resource, IdeProjectOperationTypeEnum.REMOVE));
 					break;
 
 				case PRE_REFRESH:
 					send(build(this.time, (IProject) resource, IdeProjectOperationTypeEnum.REFRESH));
 					break;
 
 				default:
 					break;
 			}
 			
 			return false;
 		}
 	}
 	
 	static final void process(final long time, final IResourceChangeEvent event)
 	{
 		ResourceEventType type  = ResourceEventType.valueOf(event.getType());
 		IResourceDelta    delta = event.getDelta();
 	
 		new ResourceDeltaVisitor(time, type).visitOrHandle(delta, event);
 	}
 
 	final void process(final long time, final IWorkbenchPart part, final ISelection selection)
 	{
 		IProject project = part instanceof IEditorPart ? Projects.fromEditor((IEditorPart) part) : null;
 		
 		if (project == null && selection instanceof IStructuredSelection)
 		{
 			project = Projects.fromSelection((IStructuredSelection) selection);
 		}
 		
 		if (project == null)
 		{
 			project = Projects.fromPage(part.getSite().getPage());
 		}
 		
 		if (this.updateProject(project))
 		{
 			send(build(time, project, IdeProjectOperationTypeEnum.SWITCH_TO));
 		}
 	}
 
 	public final void resourceChanged(final IResourceChangeEvent event)
 	{
 		final long time = currentTime();
 		
 		execute(new Runnable()
 		{
 			public final void run()
 			{
 				process(time, event);
 			}
 		});
 	}
 
 	public final void selectionChanged(final IWorkbenchPart part, final ISelection selection)
 	{
 		final long time = currentTime();
 
 		executeSafely(new Runnable()
 		{
 			public final void run()
 			{
 				process(time, part, selection);
 			}
 		});
 	}
 
 	public final Set<ResourceEventType> getEventTypes()
 	{
 		return ImmutableSet.of(PRE_CLOSE, PRE_DELETE, PRE_REFRESH, POST_CHANGE);
 	}
 }
