 package no.runsafe.inspectorgadget;
 
import no.runsafe.InspectorGadget.events.RightClick;
 import no.runsafe.framework.RunsafePlugin;
 
 public class Plugin extends RunsafePlugin
 {
 	@Override
 	protected void PluginSetup()
 	{
 		addComponent(RightClick.class);
 	}
 }
