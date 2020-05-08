 package sk.stuba.fiit.perconik.core.listeners;
 
 public final class Listeners
 {
 	private Listeners()
 	{
 		throw new AssertionError();
 	}
 
 	public static final void register(final FileBufferListener listener)
 	{
 		Resources.getFileBufferResource().register(listener);
 	}
 	
 	public static final void register(final LaunchListener listener)
 	{
 		Resources.getLaunchResource().register(listener);
 	}
 
 	public static final void register(final LaunchConfigurationListener listener)
 	{
 		Resources.getLaunchConfigurationResource().register(listener);
 	}
 	
 	public static final void register(final OperationHistoryListener listener)
 	{
 		Resources.getOperationHistoryResource().register(listener);
 	}
 	
 	public static final void register(final PartListener listener)
 	{
 		Resources.getPartResource().register(listener);
 	}
 	
 	public static final void register(final RefactoringExecutionListener listener)
 	{
 		Resources.getRefactoringExecutionResource().register(listener);
 	}
 	
 	public static final void register(final SelectionListener listener)
 	{
 		Resources.getSelectionResource().register(listener);
 	}
 	
 	public static final void unregister(final FileBufferListener listener)
 	{
 		Resources.getFileBufferResource().unregister(listener);
 	}
 
 	public static final void unregister(final LaunchListener listener)
 	{
 		Resources.getLaunchResource().unregister(listener);
 	}
 
 	public static final void unregister(final LaunchConfigurationListener listener)
 	{
 		Resources.getLaunchConfigurationResource().unregister(listener);
 	}
 	
 	public static final void unregister(final OperationHistoryListener listener)
 	{
 		Resources.getOperationHistoryResource().unregister(listener);
 	}
 
 	public static final void unregister(final PartListener listener)
 	{
 		Resources.getPartResource().unregister(listener);
 	}
 	
 	public static final void unregister(final RefactoringExecutionListener listener)
 	{
 		Resources.getRefactoringExecutionResource().unregister(listener);
 	}
 	
 	public static final void unregister(final SelectionListener listener)
 	{
 		Resources.getSelectionResource().unregister(listener);
 	}
 	
 	public static final void unregisterAll()
 	{
 		unregisterAll(Listener.class);
 	}
 	
	public static final void unregisterAll(final Class<? super Listener> type)
 	{
		for (Resource<?> resource: Resources.getResources())
 		{
 			resource.unregisterAll(type);
 		}
 	}
 }
