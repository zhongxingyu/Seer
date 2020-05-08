 package no.runsafe.framework.hook;
 
 import org.picocontainer.DefaultPicoContainer;
 import org.picocontainer.Startable;
 
 public class HookEngine implements Startable
 {
 	public static final DefaultPicoContainer hookContainer = new DefaultPicoContainer();
 
 	public HookEngine()
 	{
 	}
 
 	public HookEngine(FrameworkHook[] hooks)
 	{
 		for (FrameworkHook hook : hooks)
 		{
			// Do this to avoid exceptions..
			hookContainer.removeComponent(hook);
 			hookContainer.addComponent(hook);
 		}
 	}
 
 	@Override
 	public void start()
 	{
 		// This class is startable just so it gets instantiated - constructor handles hooking..
 	}
 
 	@Override
 	public void stop()
 	{
 	}
 }
