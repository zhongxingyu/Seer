 package no.runsafe.inspectorgadget;
 
 import no.runsafe.framework.RunsafePlugin;
import no.runsafe.inspectorgadget.events.RightClick;
 
 public class Plugin extends RunsafePlugin
 {
 	@Override
 	protected void PluginSetup()
 	{
 		addComponent(RightClick.class);
 	}
 }
