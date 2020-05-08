 package com.atlassian.plugin.impl;
 
 /**
  * This class represents a Plugin that was not able to be loaded by the PluginManager.
  *
  * @see com.atlassian.plugin.manager.DefaultPluginManager
  */
 public class UnloadablePlugin extends StaticPlugin
 {
     private static final String UNKNOWN_KEY_PREFIX = "Unknown-";
     private String errorText;
     private boolean uninstallable = true;
     private boolean deletable = true;
 
     public UnloadablePlugin()
     {
         this(null);
     }
 
     /**
      * @param text The error text
      * @since 2.0.0
      */
    public UnloadablePlugin(String text)
     {
        this.errorText = text;
         setKey(UNKNOWN_KEY_PREFIX + System.identityHashCode(this));
     }
 
     public boolean isUninstallable()
     {
         return uninstallable;
     }
 
    public void setDeletable(boolean deletable)
     {
         this.deletable = deletable;
     }
 
     public boolean isDeleteable()
     {
         return deletable;
     }
 
    public void setUninstallable(boolean uninstallable)
     {
         this.uninstallable = uninstallable;
     }
 
     public boolean isEnabledByDefault()
     {
         return false;
     }
 
     public String getErrorText()
     {
         return errorText;
     }
 
    public void setErrorText(String errorText)
     {
         this.errorText = errorText;
     }
 

     public void close()
     {
 
     }
 }
