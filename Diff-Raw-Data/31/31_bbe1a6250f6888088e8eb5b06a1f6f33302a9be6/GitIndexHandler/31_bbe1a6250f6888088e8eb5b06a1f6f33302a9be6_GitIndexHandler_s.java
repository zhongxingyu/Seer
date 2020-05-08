 package sk.stuba.fiit.perconik.core.resources;
 
 import sk.stuba.fiit.perconik.core.listeners.GitIndexListener;
 
 enum GitIndexHandler implements Handler<GitIndexListener>
 {
 	INSTANCE;
 	
	private final GitHandlerSupport<GitIndexListener> support = new GitHandlerSupport<>(GitIndexListener.class);
 	
 	public final void register(final GitIndexListener listener)
 	{
 		this.support.register(listener);
 	}
 
 	public final void unregister(final GitIndexListener listener)
 	{
 		this.support.unregister(listener);
 	}
 }
