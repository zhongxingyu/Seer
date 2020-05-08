 package com.atlassian.sal.api.upgrade;
 
 import java.util.Collection;
 
 import com.atlassian.sal.api.message.Message;
 
 /**
  * A task that needs to be executed to upgrade the existing data
  *
  * @since 2.0
  */
 public interface PluginUpgradeTask
 {
     /**
      * @return The new build number that this upgrade will upgrade to
      *         Build number is specified in atlassian-plugin.xml inside <plugin-info> element. eg: <code>&lt;param name="build"&gt;1&lt;/param&gt;</code>
      */
     public int getBuildNumber();
 
     /**
      * @return A short (<50 chars) description of the upgrade action
      */
     public String getShortDescription();
 
 
     /**
     * Perform the upgrade task. An exception should be thrown if the upgrade fails and cannot be recovered from. A
     * collection of error or warning messages should be returned if there a problems with the upgrade that are not
     * severe enough to halt the execution of the entire upgrade process. An exception should be thrown if the upgrade
     * process cannot continue.
      *
     * @return a collection of warnings about the upgrade
     * @throws Exception if the upgrade fails
      */
     public Collection<Message> doUpgrade() throws Exception;
 
     /**
      * @return key of the plugin that this upgrade task applies to.
      *         Find the key as an attribute of top level element in atlassian-plugin.xml
      */
     public String getPluginKey();
 }
 
