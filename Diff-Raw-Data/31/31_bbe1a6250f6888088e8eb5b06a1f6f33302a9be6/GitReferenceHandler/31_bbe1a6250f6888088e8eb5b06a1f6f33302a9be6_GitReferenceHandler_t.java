 package sk.stuba.fiit.perconik.core.resources;
 
import org.eclipse.jgit.events.RefsChangedListener;
 import sk.stuba.fiit.perconik.core.listeners.GitReferenceListener;
 
 enum GitReferenceHandler implements Handler<GitReferenceListener>
 {
 	INSTANCE;
 	
	private final GitHandlerSupport<RefsChangedListener> support = new GitHandlerSupport<>(RefsChangedListener.class);
 	
 	public final void register(final GitReferenceListener listener)
 	{
 		this.support.register(listener);
 	}
 
 	public final void unregister(final GitReferenceListener listener)
 	{
 		this.support.unregister(listener);
 	}
 }
