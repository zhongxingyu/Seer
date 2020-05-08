 package sk.stuba.fiit.perconik.eclipse.ui;
 
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import sk.stuba.fiit.perconik.eclipse.core.runtime.PluginConsole;
 import sk.stuba.fiit.perconik.environment.Environment;
 import sk.stuba.fiit.perconik.environment.plugin.Activator;
 
 public final class Workbenches
 {
 	private Workbenches()
 	{
 		throw new AssertionError();
 	}
 	
 	public static final IWorkbench getWorkbench()
 	{
 		try
 		{
 			return PlatformUI.getWorkbench();
 		}
 		catch (IllegalStateException e)
 		{
 			if (Environment.debug)
 			{
 				PluginConsole.of(Activator.getDefault()).error("Workbench has not been created yet.", e);
 			}
 			
 			return null;
 		}
 	}
 
 	public static final IWorkbenchWindow getActiveWindow()
 	{
 		return getActiveWindow(getWorkbench());
 	}
 	
 	public static final IWorkbenchWindow getActiveWindow(final IWorkbench workbench)
 	{
 		if (workbench == null)
 		{
 			return null;
 		}
 		
 		return workbench.getActiveWorkbenchWindow();
 	}
 	
 	public static final IWorkbenchPage getActivePage()
 	{
 		return getActivePage(getActiveWindow());
 	}
 
 	public static final IWorkbenchPage getActivePage(final IWorkbenchWindow window)
 	{
 		if (window == null)
 		{
 			return null;
 		}
 		
 		return window.getActivePage();
 	}
 	
 	public static final IWorkbenchPart getActivePart()
 	{
 		return getActivePart(getActivePage());
 	}
 
 	public static final IWorkbenchPart getActivePart(final IWorkbenchPage page)
 	{
 		if (page == null)
 		{
 			return null;
 		}
 		
 		return page.getActivePart();
 	}
 
 	public static final IWorkbench waitForWorkbench()
 	{
 		IWorkbench workbench;
 		
 		while ((workbench = getWorkbench()) == null) {}
 		
 		return workbench;
 	}
 
 	public static final IWorkbenchWindow waitForActiveWindow()
 	{
		return waitForActiveWindow(waitForWorkbench());
 	}
 
 	public static final IWorkbenchWindow waitForActiveWindow(final IWorkbench workbench)
 	{
 		IWorkbenchWindow window;
 		
 		while ((window = getActiveWindow(workbench)) == null) {}
 		
 		return window;
 	}
 
 	public static final IWorkbenchPage waitForActivePage()
 	{
		return waitForActivePage(waitForActiveWindow());
 	}
 	
 	public static final IWorkbenchPage waitForActivePage(final IWorkbenchWindow window)
 	{
 		IWorkbenchPage page;
 		
 		while ((page = getActivePage(window)) == null) {}
 		
 		return page;
 	}
 	
 	public static final IWorkbenchPart waitForActivePart()
 	{
		return waitForActivePart(waitForActivePage());
 	}
 	
 	public static final IWorkbenchPart waitForActivePart(final IWorkbenchPage page)
 	{
 		IWorkbenchPart part;
 		
 		while ((part = getActivePart(page)) == null) {}
 		
 		return part;
 	}
 }
