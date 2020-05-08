 package sk.stuba.fiit.perconik.core.resources;
 
 import sk.stuba.fiit.perconik.core.listeners.GitConfigurationListener;
 
 enum GitConfigurationHandler implements Handler<GitConfigurationListener>
 {
 	INSTANCE;
 	
	private final GitHandlerSupport<GitConfigurationListener> support = new GitHandlerSupport<>(GitConfigurationListener.class);
 	
 	public final void register(final GitConfigurationListener listener)
 	{
 		this.support.register(listener);
 	}
 
 	public final void unregister(final GitConfigurationListener listener)
 	{
 		this.support.unregister(listener);
 	}
 }
