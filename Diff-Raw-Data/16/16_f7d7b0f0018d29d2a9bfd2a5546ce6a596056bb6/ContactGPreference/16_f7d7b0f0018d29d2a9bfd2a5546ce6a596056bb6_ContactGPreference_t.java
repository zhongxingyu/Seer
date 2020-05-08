 package com.baosight.spark.plugin;
 
 import com.baosight.spark.plugin.gui.ContactGPreferencePanel;
import org.jivesoftware.spark.PluginManager;
import org.jivesoftware.spark.plugin.Plugin;
 import org.jivesoftware.spark.preference.Preference;
 import org.jivesoftware.spark.util.log.Log;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: SaintKnight
  * Date: 13-9-6
  * Time: 下午3:44
  * Creator:Huang Jie
  * To change this template use File | Settings | File Templates.
  */
 public class ContactGPreference implements Preference {
     private final static String ContactGPreferenceName = "组织构架联系人";
     private ContactGProperties _props;
     private ContactGPreferencePanel _prefPanel;
 
     public ContactGPreference() {
         _props = ContactGProperties.getInstance();
         try {
             if (EventQueue.isDispatchThread()) {
                 _prefPanel = new ContactGPreferencePanel();
             } else {
                 EventQueue.invokeAndWait(new Runnable() {
                     public void run() {
                         _prefPanel = new ContactGPreferencePanel();
                     }
                 });
             }
         } catch (Exception e) {
             Log.error(e);
         }
     }
 
     public String getTitle() {
         return ContactGPreferenceName;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public Icon getIcon() {
         ClassLoader cl = getClass().getClassLoader();
         return new ImageIcon(cl.getResource("orgcontacts-logo.png"));
     }
 
     public String getTooltip() {
         return ContactGPreferenceName;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public String getListName() {
         return ContactGPreferenceName;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public String getNamespace() {
         return ContactGPreferenceName;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public JComponent getGUI() {
         return _prefPanel;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void load() {
         _prefPanel.setContactsUrl(_props.getRemoteContacts());
         _prefPanel.setGroupUrl(_props.getRemoteGroup());
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
    PluginManager pluginManager;

     public void commit() {
         _props.setRemoteContacts(_prefPanel.getContactsUrl());
         _props.setRemoteGroup(_prefPanel.getGroupUrl());
         _props.save();

        //刷新重载plugin
        pluginManager = PluginManager.getInstance();
        Plugin plugin = pluginManager.getPlugin(ContactGList.class);
        plugin.shutdown();
        pluginManager.removePlugin(plugin);
        plugin = new ContactGList();
        plugin.initialize();
        pluginManager.registerPlugin(plugin);

 //        SparkManager.getWorkspace().loadPlugins();
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public boolean isDataValid() {
         return true;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public String getErrorMessage() {
         return "FAAAAAAAAAAAQ";  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public Object getData() {
         return _props;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void shutdown() {
         //do Nothing
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
