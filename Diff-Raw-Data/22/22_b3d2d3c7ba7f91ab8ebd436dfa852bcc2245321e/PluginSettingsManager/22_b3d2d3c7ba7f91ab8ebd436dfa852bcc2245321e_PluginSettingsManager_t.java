 /*
  * Created by Dmitry Miroshnichenko 01-12-2012. Copyright Mail.Ru Group 2012.
  * All rights reserved.
  */
 package ru.mail.plugins.imageloader.settings;
 
 import ru.mail.plugins.imageloader.common.Consts;
 import com.atlassian.sal.api.pluginsettings.PluginSettings;
 import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
 
 /**
  * IMplementation of <code>IPluginSettingsManager</code>.
  */
 public class PluginSettingsManager
     implements IPluginSettingsManager
 {
     public static final String FORMAT_APPENDER_ISSUE_TYPE_IMAGE = "_issueimage";
    
     private final PluginSettingsFactory pluginSettingsFactory;
 
     public PluginSettingsManager(final PluginSettingsFactory pluginSettingsFactory)
     {
         this.pluginSettingsFactory = pluginSettingsFactory;
     }
 
     private String formatIssueImageKey(String projectId, String issueTypeName)
     {
         return projectId + issueTypeName + FORMAT_APPENDER_ISSUE_TYPE_IMAGE;
     }
 
     @Override
     public String getIssueTypeImage(String projectId, String issueTypeName)
     {
         return (String) getValue(formatIssueImageKey(projectId, issueTypeName));
     }
 
     @Override
     public Object getValue(final String key)
     {
         final PluginSettings settings = pluginSettingsFactory.createSettingsForKey(Consts.PLUGIN_KEY_MAILRU_IMAGE_LOADER);
         return settings.get(key);
     }
 
     public void setIssueTypeImage(String imageName, String projectId, String issueTypeName)
     {
         setValue(formatIssueImageKey(projectId, issueTypeName), imageName);
     }
 
     @Override
     public void setValue(final String key, final String value)
     {
         final PluginSettings settings = pluginSettingsFactory.createSettingsForKey(Consts.PLUGIN_KEY_MAILRU_IMAGE_LOADER);
         if (value == null)
         {
             settings.remove(key);
         }
         else
         {
             settings.put(key, value);
         }
     }
 }
