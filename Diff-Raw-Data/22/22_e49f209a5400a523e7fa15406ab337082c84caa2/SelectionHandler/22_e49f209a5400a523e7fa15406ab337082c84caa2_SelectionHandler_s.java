 package sk.stuba.fiit.perconik.core.resources;
 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchWindow;
 import sk.stuba.fiit.perconik.core.listeners.SelectionListener;
 import sk.stuba.fiit.perconik.core.listeners.WindowListener;
 import sk.stuba.fiit.perconik.eclipse.ui.Workbenches;
 
 enum SelectionHandler implements Handler<SelectionListener>
 {
 	INSTANCE;
 
 	static final class InternalSelectionHook extends InternalHook<IWorkbenchWindow, SelectionListener> implements WindowListener
 	{
 		InternalSelectionHook(final SelectionListener listener)
 		{
			super(null, listener);
 		}
 
 		@Override
 		final void addInternal(final IWorkbenchWindow window)
 		{
 			window.getSelectionService().addSelectionListener(this.listener);
 		}
 
 		@Override
 		final void removeInternal(final IWorkbenchWindow window)
 		{
 			window.getSelectionService().removeSelectionListener(this.listener);
 		}
 		
 		@Override
 		final void preRegisterInternal()
 		{
 			final Runnable addListener = new Runnable()
 			{
 				@Override
 				public final void run()
 				{
					for (IWorkbenchWindow window: Workbenches.waitForWorkbench().getWorkbenchWindows())
					{
						add(window);
					}
 				}
 			};
 		
 			Display.getDefault().asyncExec(addListener);
 		}
 		
 		public final void windowOpened(final IWorkbenchWindow window)
 		{
 			this.add(window);
 		}
 
 		public final void windowClosed(final IWorkbenchWindow window)
 		{
 			this.remove(window);
 		}
 
 		public final void windowActivated(final IWorkbenchWindow window)
 		{
 		}
 
 		public final void windowDeactivated(final IWorkbenchWindow window)
 		{
 		}
 	}
 	
 	private final HookSupport<InternalSelectionHook, SelectionListener> support = new HookSupport<>();
 	
 	public final void add(final SelectionListener listener)
 	{
 		this.support.hook(DefaultResources.window, listener);
 	}
 
 	public final void remove(final SelectionListener listener)
 	{
 		this.support.unhook(DefaultResources.window, listener);
 	}
 }
