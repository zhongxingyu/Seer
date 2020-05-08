 package sk.stuba.fiit.perconik.core.resources;
 
 import sk.stuba.fiit.perconik.core.listeners.GitReferenceListener;
 
 enum GitReferenceHandler implements Handler<GitReferenceListener>
 {
 	INSTANCE;
 	
	private final GitHandlerSupport<GitReferenceListener> support = new GitHandlerSupport<>(GitReferenceListener.class);
 	
 	public final void register(final GitReferenceListener listener)
 	{
 		this.support.register(listener);
 	}
 
 	public final void unregister(final GitReferenceListener listener)
 	{
 		this.support.unregister(listener);
 	}
 }
