 package com.zutubi.pulse.core.commands.xbuild;
 
 import com.zutubi.tove.annotations.Form;
 import com.zutubi.tove.annotations.SymbolicName;
 import com.zutubi.tove.config.api.AbstractNamedConfiguration;
 import com.zutubi.validation.annotations.Required;
 
 @SymbolicName("XBuildCommandConfig.BuildPropertyConfig")
 @Form(fieldOrder = {"name", "value"})
 public class BuildPropertyConfiguration extends AbstractNamedConfiguration
 {
     @Required
    private String value;
 
     public BuildPropertyConfiguration()
     {
     }
 
     public BuildPropertyConfiguration(String name, String value)
     {
         setName(name);
         setValue(value);
     }
 
     public String getValue()
     {
         return this.value;
     }
 
     public void setValue(String value)
     {
         this.value = value;
     }
 }
