 package com.zutubi.pulse.core.commands.xbuild;
 
 import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;
 import java.util.Arrays;
 import java.util.List;
 
 public class XBuildCommand extends NamedArgumentCommand
 {
     public XBuildCommand(XBuildCommandConfiguration configuration)
     {
         super(configuration);
     }
 
     @Override
     protected List getDefaultPostProcessorTypes()
     {
         return Arrays.asList(new Class[] { XBuildPostProcessorConfiguration.class});
     }
 }
