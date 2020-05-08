 package org.geworkbench.engine.config.rules;
 
 import org.geworkbench.engine.ccm.ComponentConfigurationManager;
 import org.geworkbench.engine.config.*;
 import org.geworkbench.engine.config.events.AppEventListener;
 import org.geworkbench.engine.config.events.EventSource;
 import org.geworkbench.engine.management.ComponentRegistry;
 import org.geworkbench.util.Debug;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 /**
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: First Genetic Trust, Inc.</p>
  * @author First Genetic Trust, Inc.
  * @version 1.0
  */
 
 /**
  * Describes the object that is pushed on the <code>UILauncher</code> stack
  * when processing the pattern "geaw-config/plugin". It will create the
  * <code>PluginDescriptor</code> for the plugin and register it with the
  * <code>PluginRegistry</code>. It will also handle all other registration
  * tasks associated with this plugin, including:
  * <UI>
  * <LI> register the event listeners (if any) associated with the plugin
  * with the appropriate event sources.</LI>
  * <LI> associate any <code>ActionListener</code>s declared by the plugin
  * with the requested menu items.
  * </UI>
  */
 public class PluginObjectCCM extends PluginObject{
 
 	private static final String FILE_DEL = System.getProperty("file.separator");
 
     // ---------------------------------------------------------------------------
     // --------------- Constructors
     // ---------------------------------------------------------------------------
     public PluginObjectCCM() {
     	super();
     	
     }
 
     // ---------------------------------------------------------------------------
     // --------------- Methods
     // ---------------------------------------------------------------------------
 
     public static ComponentMetadata processComponentDescriptor(String resourceName, Class type, String fileName) throws IOException, JDOMException, NotMenuListenerException, MalformedMenuItemException, NotVisualPluginException {
     	
     	String componentsDir = System
 				.getProperty(UILauncher.COMPONENTS_DIR_PROPERTY);
 		if (componentsDir == null) {
 			componentsDir = UILauncher.DEFAULT_COMPONENTS_DIR;
 		}
 		componentsDir += FILE_DEL + resourceName + FILE_DEL ;
     	
         // Look for descriptor file
         ComponentMetadata metadata = new ComponentMetadata(type, resourceName);
         String fileFullName = componentsDir + fileName + ComponentConfigurationManager.CCM_EXTENSION;
         
         SAXBuilder builder = new SAXBuilder();
 
         InputStream in = new FileInputStream(new File(fileFullName));
 
         if (in != null) {
             Document doc = builder.build(in);
             Element root = doc.getRootElement();
             
             if (root.getName().equals("geaw-config")) {
             	root = root.getChild("component-descriptor");
             	
             	if (root == null){
             	       ComponentMetadata defaultData = new ComponentMetadata(type, resourceName);
             	        defaultData.setName(type.getSimpleName());
             	        return defaultData;
             	}
             	
 	            if (root.getName().equals("component-descriptor")) {
 	                root = root.getChild("component");
 	                if (root != null) {
 	                    // Check for optional attributes
 	                    String iconName = root.getAttributeValue("icon");
 	                    if (iconName != null) {
 	                        URL url = type.getResource(iconName);
 	                        if (url != null) {
 	                            ImageIcon icon = new ImageIcon(url);
 	                            if (icon != null) {
 	                                metadata.setIcon(icon);
 	                            }
 	                        } else {
 	                            System.out.println("Icon for component '" + type + "' not found: " + iconName);
 	                        }
 	                    }
 	                    String commonName = root.getAttributeValue("name");
 	                    if (commonName != null) {
 	                        metadata.setName(commonName);
 	                    }
 	                    String version = root.getAttributeValue("version");
 	                    if (version != null) {
 	                        metadata.setVersion(version);
 	                    }
 	                    String description = root.getText().trim();
 	                    if ((description != null) && (description.length() > 0)) {
 	                        metadata.setDescription(description);
 	                    }
 	                    java.util.List<Element> elements = root.getChildren();
 	                    for (int i = 0; i < elements.size(); i++) {
 	                        Element element = elements.get(i);
 	                        if (element.getName().equals("menu-item")) {
 	                            metadata.addMenuInfo(
 	                                    element.getAttributeValue("path"),
 	                                    element.getAttributeValue("mode"),
 	                                    element.getAttributeValue("var"),
 	                                    element.getAttributeValue("icon"),
 	                                    element.getAttributeValue("accelerator")
 	                            );
 	                        } else if (element.getName().equals("online-help")) {
 	                            metadata.setHelpSet(element.getAttributeValue("helpSet"));
 	                        }
 	                    }
 	                    if (metadata.getName() == null) {
 	                        metadata.setName(type.getSimpleName());
 	                    }
 	                    return metadata;
 	                }
 	            }
 	        }
         }
         ComponentMetadata defaultData = new ComponentMetadata(type, resourceName);
         defaultData.setName(type.getSimpleName());
         return defaultData;
     }
 
     /**
      * Creates a new plugin in and adds it to the <code>PluginRegistry</code>.
      * The plugin is instantiated by a call to its default constructor.
      *
      * @param id           Id to be used for the new plugin.
      * @param name         Name to be used for the new plugin.
      * @param className    Class name for the new plugin.
      * @param resourceName Resource from which to load the plugin.
      */
     public void createPlugin(String id, String name, String className, String resourceName) {
    	PluginRegistry.setNameMap(className, name);
         compDes = new PluginDescriptor(className, id, name, resourceName, loadOrderTracker);
         loadOrderTracker++;
         Debug.debug("PluginObject::createPlugIn --> Creating id = " + id + " name = " + name + " className = " + className + " resourceName = " + resourceName);
         PluginRegistry.addPlugin(compDes);
         // Digest component descriptor
         try {
             ComponentMetadata metadata = processComponentDescriptor(resourceName, compDes.getPluginClass(), id);
             compDes.setComponentMetadata(metadata);
         } catch (Exception e) {
             System.out.println("Problem parsing component descriptor for component: " + compDes.getPluginClass() + ".");
         }
     }
 
     /**
      * Invoked by the Digester. Adds the plugin (which must implement the
      * <code>VisualPlugin</code> interface) to the top-level GUI window, at the
      * desiganted GUI area.
      *
      * @param guiAreaName The area where to add the visual component.
      */
     public void addGUIComponent(String guiAreaName) {
         // Make sure that the plugin has a visual representation
         if (!compDes.isVisualPlugin()) {
             System.err.println("PluginObject::addGUIComponent - Attempt to add as " + "GUI component the plugin with id = " + compDes.getID() + ", which does not implement " + "interface VisualPlugin.\n");
             return;
         }
         compDes.setVisualLocation(guiAreaName);
         GUIFramework guiFramework = GeawConfigObjectCCM.getGuiWindow();
         VisualPlugin visualPlugin = (VisualPlugin) compDes.getPlugin();
         Component component = visualPlugin.getComponent();
         guiFramework.addToContainer(guiAreaName, ((VisualPlugin) compDes.getPlugin()).getComponent(), compDes.getLabel(), compDes.getPluginClass());
         PluginRegistry.addVisualAreaInfo(guiAreaName, (VisualPlugin) compDes.getPlugin());
     }
 }
 
