 package com.zutubi.pulse.core.commands.xbuild;
 
 import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
 import com.zutubi.pulse.core.engine.api.Addable;
 import com.zutubi.pulse.core.tove.config.annotations.BrowseScmFileAction;
 import com.zutubi.tove.annotations.Form;
 import com.zutubi.tove.annotations.SymbolicName;
 import com.zutubi.util.StringUtils;

 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 @SymbolicName("XBuildCommandConfiguration")
@Form(fieldOrder = {"name", "workingDir", "buildFile", "targets", "configuration", "postProcessors", "exe", "inputFile", "outputFile", "force"})
 public class XBuildCommandConfiguration extends NamedArgumentCommandConfiguration
 {
     private static final String EXECUTABLE_PROPERTY = "xbuild.bin";
     private static final String DEFAULT_EXECUTABLE = "xbuild";
     private static final String FLAG_TARGET = "/target:";
     private static final String FLAG_PROPERTY = "/property:";
     private static final String PROPERTY_SEPARATOR = "=";
     private static final String CONFIGURATION_PROPERTY = "Configuration";
 
     @BrowseScmFileAction(baseDirField = "workingDir")
    private String buildFile = "";
    private String targets = "";
    private String configuration = "Debug";
 
     @Addable("build-property")
     private Map<String, BuildPropertyConfiguration> buildProperties = new LinkedHashMap();
 
     public XBuildCommandConfiguration()
     {
         super(XBuildCommand.class, EXECUTABLE_PROPERTY, DEFAULT_EXECUTABLE);
     }
 
     protected List<NamedArgumentCommandConfiguration.NamedArgument> getNamedArguments()
     {
         List result = new LinkedList();
         if (StringUtils.stringSet(this.buildFile))
         {
             result.add(new NamedArgumentCommandConfiguration.NamedArgument("build file", this.buildFile));
         }
         if (StringUtils.stringSet(this.targets))
         {
             List flaggedTargets = new LinkedList();
             for (String target : this.targets.split("\\s+"))
             {
                 flaggedTargets.add(FLAG_TARGET + target);
             }
 
             result.add(new NamedArgumentCommandConfiguration.NamedArgument("targets", targets, flaggedTargets));
         }
         if (StringUtils.stringSet(this.configuration))
         {
             result.add(new NamedArgumentCommandConfiguration.NamedArgument("configuration", this.configuration, Arrays.asList(new String[] {FLAG_PROPERTY + CONFIGURATION_PROPERTY + PROPERTY_SEPARATOR + this.configuration})));
         }
         
         return result;
     }
 
     @Override
     public List<String> getCombinedArguments()
     {
         List<String> result = new LinkedList<String>(super.getCombinedArguments());
 
         for (BuildPropertyConfiguration property : this.buildProperties.values())
         {
             result.add(FLAG_PROPERTY + property.getName() + PROPERTY_SEPARATOR + property.getValue());
         }
 
         return result;
     }
 
     public String getBuildFile()
     {
         return this.buildFile;
     }
 
     public void setBuildFile(String buildFile)
     {
         this.buildFile = buildFile;
     }
 
     public String getTargets()
     {
         return this.targets;
     }
 
     public void setTargets(String targets)
     {
         this.targets = targets;
     }
 
     public String getConfiguration()
     {
         return this.configuration;
     }
 
     public void setConfiguration(String configuration)
     {
         this.configuration = configuration;
     }
 
     public Map<String, BuildPropertyConfiguration> getBuildProperties()
     {
         return this.buildProperties;
     }
 
     public void setBuildProperties(Map<String, BuildPropertyConfiguration> buildProperties)
     {
         this.buildProperties = buildProperties;
     }
 
     public void addBuildProperty(BuildPropertyConfiguration property)
     {
         this.buildProperties.put(property.getName(), property);
     }
 }
