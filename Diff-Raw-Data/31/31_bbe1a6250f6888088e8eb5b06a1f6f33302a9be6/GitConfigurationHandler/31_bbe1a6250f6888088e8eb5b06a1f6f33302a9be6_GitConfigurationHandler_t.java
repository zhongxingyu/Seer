 package sk.stuba.fiit.perconik.core.resources;
 
import org.eclipse.jgit.events.ConfigChangedListener;
 import sk.stuba.fiit.perconik.core.listeners.GitConfigurationListener;
 
 enum GitConfigurationHandler implements Handler<GitConfigurationListener>
 {
 	INSTANCE;
 	
	private final GitHandlerSupport<ConfigChangedListener> support = new GitHandlerSupport<>(ConfigChangedListener.class);
 	
 	public final void register(final GitConfigurationListener listener)
 	{
 		this.support.register(listener);
 	}
 
 	public final void unregister(final GitConfigurationListener listener)
 	{
 		this.support.unregister(listener);
 	}
 }
