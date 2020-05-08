 package com.atlassian.sal.core.upgrade;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
 
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.PluginAccessor;
 import com.atlassian.plugin.event.PluginEventListener;
 import com.atlassian.plugin.event.PluginEventManager;
 import com.atlassian.plugin.event.events.PluginEnabledEvent;
 import com.atlassian.sal.api.lifecycle.LifecycleAware;
 import com.atlassian.sal.api.message.Message;
 import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
 import com.atlassian.sal.api.transaction.TransactionCallback;
 import com.atlassian.sal.api.transaction.TransactionTemplate;
 import com.atlassian.sal.api.upgrade.PluginUpgradeManager;
 import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
 
 /**
  * Processes plugin upgrade operations.
  * <p>
  * Upgrades are triggered by the start lifecycle event, and plugin enabled.
  */
public class DefaultPluginUpgradeManager implements PluginUpgradeManager, LifecycleAware, InitializingBean, DisposableBean
 {
     private static final Logger log = Logger.getLogger(DefaultPluginUpgradeManager.class);
 
     private volatile boolean started = false;
 
     private final List<PluginUpgradeTask> upgradeTasks;
     private final TransactionTemplate transactionTemplate;
     private final PluginAccessor pluginAccessor;
     private final PluginSettingsFactory pluginSettingsFactory;
    private final PluginEventManager pluginEventManager;
 
     public DefaultPluginUpgradeManager(final List<PluginUpgradeTask> upgradeTasks, final TransactionTemplate transactionTemplate,
             final PluginAccessor pluginAccessor, final PluginSettingsFactory pluginSettingsFactory, final PluginEventManager pluginEventManager)
     {
         this.upgradeTasks = upgradeTasks;
         this.transactionTemplate = transactionTemplate;
         this.pluginAccessor = pluginAccessor;
         this.pluginSettingsFactory = pluginSettingsFactory;
        this.pluginEventManager = pluginEventManager;
     }
 
     /**
      * Notifies the plugin upgrade manager that a plugin update tasks has been registered.
      * 
      * This method does nothing but logging at the moment. Is is now deprecated since it could result in circular
      * dependency when trying to bind already exposed update tasks to plugin manager that is being created.
      * 
      * @deprecated
      * @param task the upgrade task that is being bound
      * @param props the set of properties that the upgrade task was registered with
      */
     @SuppressWarnings("unchecked")
     @Deprecated
     public void onBind(final PluginUpgradeTask task, final Map props)
     {
         // Doing lots here....
         log.debug("onbind task = [" + task.getPluginKey() + ", " + task.getBuildNumber() + "] ");
     }
 
     public void onStart()
     {
         log.debug("onStart");
         final List<Message> messages = upgrade();
 
         // TODO 1: should do something useful with the messages
         // TODO 2: we don't know what upgrade tasks these messages came from
         if (messages != null)
         {
             for(final Message msg : messages)
             {
                 log.error("Upgrade error: "+msg);
             }
         }
 
         started = true;
     }
 
     @PluginEventListener
     public void onPluginEnabled(PluginEnabledEvent event)
     {
         // Check if the Application is fully started:
         if (started)
         {
             // Run upgrades for this plugin that as been enabled AFTER the onStart event.
             final List<Message> messages = upgradeInTransaction(event.getPlugin());
             if (messages != null && messages.size() > 0)
             {
                 log.error("Error(s) encountered while upgrading plugin '" + event.getPlugin().getName() + "' on enable.");
                 for(final Message msg : messages)
                 {
                     log.error("Upgrade error: " + msg);
                 }
             }
         }
         // If onStart() has not occurred yet then ignore event - we need to wait until the App is started properly.
     }
 
     /**
      * @return map of all upgrade tasks (stored by pluginKey)
      */
     protected Map<String, List<PluginUpgradeTask>> getUpgradeTasks()
     {
         final Map<String, List<PluginUpgradeTask>> pluginUpgrades = new HashMap<String, List<PluginUpgradeTask>>();
 
         // Find all implementations of PluginUpgradeTask
         for (final PluginUpgradeTask upgradeTask : upgradeTasks)
         {
             List<PluginUpgradeTask> upgrades = pluginUpgrades.get(upgradeTask.getPluginKey());
             if (upgrades==null)
             {
                 upgrades=new ArrayList<PluginUpgradeTask>();
                 pluginUpgrades.put(upgradeTask.getPluginKey(), upgrades);
             }
             upgrades.add(upgradeTask);
         }
 
         return pluginUpgrades;
     }
 
 
     @SuppressWarnings("unchecked")
     public List<Message> upgrade()
     {
         //JRA-737: Need to ensure upgrades run in a transaction.  Just calling upgrade here may not provide this
         //as no this may be executed outside of a 'normal' context where a transaction is available.
         final List<Message> messages = (List<Message>) transactionTemplate.execute(new TransactionCallback()
         {
             public Object doInTransaction()
             {
                 return upgradeInternal();
             }
         });
         return messages;
     }
 
     public List<Message> upgradeInternal()
     {
         log.info("Running plugin upgrade tasks...");
 
         // 1. get all upgrade tasks for all plugins
         final Map<String, List<PluginUpgradeTask>> pluginUpgrades = getUpgradeTasks();
 
 
         final ArrayList<Message> messages = new ArrayList<Message>();
 
         // 2. for each plugin, sort tasks by build number and execute them
         for (final String pluginKey : pluginUpgrades.keySet())
         {
             final List<PluginUpgradeTask> upgrades = pluginUpgrades.get(pluginKey);
 
             final List<Message> upgradeMessages = upgradePlugin(pluginKey, upgrades);
             if (upgradeMessages != null)
             {
                 messages.addAll(upgradeMessages);
             }
         }
 
         return messages;
     }
 
     private List<Message> upgradeInTransaction(final Plugin plugin)
     {
         // Apparently we need to run in a transaction
         final List<Message> messages = (List<Message>) transactionTemplate.execute(new TransactionCallback()
         {
             public Object doInTransaction()
             {
                 return upgradeInternal(plugin);
             }
         });
         return messages;
     }
 
     public List<Message> upgradeInternal(Plugin plugin)
     {
         final Map<String, List<PluginUpgradeTask>> pluginUpgrades = getUpgradeTasks();
         final String pluginKey = plugin.getKey();
         final List<PluginUpgradeTask> upgrades = pluginUpgrades.get(pluginKey);
         if (upgrades == null)
         {
             // nothing to do
             return null;
         }
         return upgradePlugin(pluginKey, upgrades);
     }
 
     private List<Message> upgradePlugin(String pluginKey, List<PluginUpgradeTask> upgrades)
     {
         final Plugin plugin = pluginAccessor.getPlugin(pluginKey);
         if (plugin == null)
             throw new IllegalArgumentException("Invalid plugin key: " + pluginKey);
 
         final PluginUpgrader pluginUpgrader = new PluginUpgrader(plugin, pluginSettingsFactory.createGlobalSettings(), upgrades);
         final List<Message> upgradeMessages = pluginUpgrader.upgrade();
         return upgradeMessages;
     }
 
    public void afterPropertiesSet() throws Exception
    {
        pluginEventManager.register(this);
    }

    public void destroy() throws Exception
    {
        pluginEventManager.unregister(this);
    }
 }
