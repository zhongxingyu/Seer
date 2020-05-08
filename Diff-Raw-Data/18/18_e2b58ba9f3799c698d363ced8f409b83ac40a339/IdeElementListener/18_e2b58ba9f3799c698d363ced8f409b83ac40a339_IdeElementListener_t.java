 package com.gratex.perconik.activity.listeners;
 
 import static com.gratex.perconik.activity.DataTransferObjects.setApplicationData;
 import static com.gratex.perconik.activity.DataTransferObjects.setEventData;
 import static com.gratex.perconik.activity.DataTransferObjects.setProjectData;
 import static sk.stuba.fiit.perconik.eclipse.jdt.core.JavaElementEventType.POST_CHANGE;
 import static sk.stuba.fiit.perconik.eclipse.jdt.core.JavaElementEventType.POST_RECONCILE;
 import java.net.URI;
 import java.util.Set;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 import org.eclipse.jdt.core.ElementChangedEvent;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import sk.stuba.fiit.perconik.core.annotations.Experimental;
 import sk.stuba.fiit.perconik.core.annotations.Unsupported;
 import sk.stuba.fiit.perconik.core.java.JavaElements;
 import sk.stuba.fiit.perconik.core.java.dom.AstTransformers;
 import sk.stuba.fiit.perconik.core.java.dom.difference.AstDifference;
 import sk.stuba.fiit.perconik.core.java.dom.difference.AstNodeDelta;
 import sk.stuba.fiit.perconik.core.listeners.JavaElementListener;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.JavaElementDeltaFlag;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.JavaElementEventType;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.dom.AstNodeType;
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.collect.ImmutableSet;
 import com.gratex.perconik.activity.ActivityServices;
 import com.gratex.perconik.activity.Application;
 import com.gratex.perconik.activity.ActivityServices.WatcherServiceOperation;
 import com.gratex.perconik.services.activity.IVsActivityWatcherService;
 import com.gratex.perconik.services.activity.IdeCodeElementEventDto;
 import com.gratex.perconik.services.activity.IdeCodeElementEventTypeEnum;
 
 /**
  * A listener of {@code IdeCodeElement} events. This listener creates
  * {@link IdeCodeElementEventDto} data transfer objects and passes them to
  * the <i>Activity Watcher Service</i> to be transferred into the
  * <i>User Activity Client Application</i> for further processing.
  * 
  * <p> TODO document how DTOs are build and what data they contain
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 @Experimental
 @Unsupported
 public final class IdeElementListener extends IdeListener implements JavaElementListener
 {
 	private final Executor executor = Executors.newSingleThreadExecutor();
 	
 	private final Cache<URI, CompilationUnit> cache;
 	
 	final CompilationUnitDifferencer differencer;
 	
 	public IdeElementListener()
 	{
 		this.cache = CacheBuilder.newBuilder().weakValues().recordStats().build();
 		
 		this.differencer = new CompilationUnitDifferencer();
 	}
 	
 	static final void process(final IJavaElement element, final ASTNode node, final IdeCodeElementEventTypeEnum type)
 	{
 		final IdeCodeElementEventDto data = new IdeCodeElementEventDto();
 
 		data.setEventType(type);
 		
 		data.setElementType(AstNodeType.valueOf(node).toString());
 		data.setElementFullName(AstTransformers.namePathExtractor().transform(node).toString());
 		
 		// TODO
 		if (Application.getInstance().isDebug()){
 		System.out.println("-- ELEMENT --");
 		System.out.println(data.getElementType());
 		System.out.println(data.getElementFullName());
 		}
 		
 		setProjectData(data, element.getJavaProject().getProject());
 		setApplicationData(data);
 		setEventData(data);
 		
 		ActivityServices.performWatcherServiceOperation(new WatcherServiceOperation()
 		{
 			public final void perform(final IVsActivityWatcherService service)
 			{
 				service.notifyIdeCodeElementEventAsync(data);
 			}
 		});
 	}
 	
 	final CompilationUnit cache(final URI uri, final CompilationUnit revised)
 	{
 		CompilationUnit original = this.cache.getIfPresent(uri);
 
 		if (revised != null)
 		{
 			this.cache.put(uri, revised);
 		}
 		
 		return original;
 	}
 
 	public final void elementChanged(final ElementChangedEvent event)
 	{
 		final IJavaElementDelta delta = event.getDelta();
 		
 		if (!JavaElementDeltaFlag.setOf(delta.getFlags()).contains(JavaElementDeltaFlag.AST_AFFECTED))
 		{
 			return;
 		}
 		
 		final Runnable command = new Runnable()
 		{
 			public final void run()
 			{
 				IJavaElement element = delta.getElement();
 				
 				URI uri = JavaElements.resource(element).getLocationURI();
 				
 				CompilationUnit revised  = delta.getCompilationUnitAST();
 				CompilationUnit original = cache(uri, revised);
 				
 				if (revised != null && original != null)
 				{
					AstDifference<ASTNode> difference = IdeElementListener.this.differencer.difference(original, revised);
 
					for (AstNodeDelta<ASTNode> delta: difference)
 					{
 						process(element, delta.getOriginalNode(), null);
 					}
 				}
 			}
 		};
 		
 		this.executor.execute(command);
 	}
 
 	public final Set<JavaElementEventType> getEventTypes()
 	{
 		return ImmutableSet.of(POST_CHANGE, POST_RECONCILE);
 	}
 }
