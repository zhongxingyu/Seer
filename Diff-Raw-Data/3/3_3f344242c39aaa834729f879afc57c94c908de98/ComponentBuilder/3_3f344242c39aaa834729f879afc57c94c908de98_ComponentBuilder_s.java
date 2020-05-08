 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.toolkit.common.xml.businessprocess;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.InvalidRegistryObjectException;
 import org.eclipse.jubula.toolkit.common.IToolKitProvider;
 import org.eclipse.jubula.toolkit.common.PluginStarter;
 import org.eclipse.jubula.toolkit.common.businessprocess.ToolkitSupportBP;
 import org.eclipse.jubula.toolkit.common.exception.ToolkitPluginException;
 import org.eclipse.jubula.toolkit.common.i18n.Messages;
 import org.eclipse.jubula.tools.constants.DebugConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.ToolkitConstants;
 import org.eclipse.jubula.tools.exception.GDConfigXmlException;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.jubula.tools.utils.generator.AbstractComponentBuilder;
 import org.eclipse.jubula.tools.xml.businessmodell.CompSystem;
 import org.eclipse.jubula.tools.xml.businessmodell.ToolkitPluginDescriptor;
 import org.eclipse.jubula.tools.xml.businessprocess.ConfigVersion;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class contains methods for reading the configuration file and for 
  * mapping the configuration file to java objects.
  * 
  * @author BREDEX GmbH
  * @created 25.07.2005
  * 
  */
 public class ComponentBuilder extends AbstractComponentBuilder {
     
     /** Singleton instance */
     private static ComponentBuilder instance;
     
     /** The logger. */
     private static Logger log = LoggerFactory.getLogger(ComponentBuilder.class);
     
     
     /**
      * Default constructor.
      */
     private ComponentBuilder() {
         super();
     }
     
     /**
      * Initializes the Component System.<br>
      * Reads in all ComponentConfiguration Files of all installed 
      * Toolkit-Plugins.
      */
     private void initCompSystem() throws ToolkitPluginException {
         IExtension[] plugins = PluginStarter.getDefault().findToolkitPlugins();
         for (IExtension extension : plugins) {
             IConfigurationElement[] elements = extension
                 .getConfigurationElements();
             for (IConfigurationElement element : elements) {
                 IToolKitProvider provider;
                 try {
                     provider = (IToolKitProvider) element
                          .createExecutableExtension(
                              ToolkitConstants.ATTR_ITOOLKITPROVIDER);
                     URL componentConfigurationURL = provider
                         .getComponentConfigurationFileURL();
                     InputStream inputStream = getInputStream(
                         componentConfigurationURL);
                     CompSystem compSystem = createCompSystem(inputStream);
                     ToolkitPluginDescriptor descr = 
                         createToolkitDescriptor(element, compSystem);
                     final ResourceBundle resourceBundle = provider
                         .getI18nResourceBundle();
                     if (resourceBundle == null) {
                         log.error(Messages.NoI18n + StringConstants.MINUS
                             + Messages.ResourceBundleAvailable 
                             + StringConstants.COLON + StringConstants.SPACE
                             + String.valueOf(descr.getName())); 
                     }
                     CompSystemI18n.addResourceBundle(resourceBundle);
                     setToolkitDescriptorToComponents(compSystem, descr);
                     // merge the CompSystem in the Main-CompSystem
                     addToolkitToCompSystem(compSystem);
                     ToolkitSupportBP.addToolkitProvider(descr, provider);
                 } catch (IOException fileNotFoundEx) {
                     final String msg = Messages.ComponenConfigurationNotFound
                         + StringConstants.EXCLAMATION_MARK;
                     log.error(msg, fileNotFoundEx);
                    throw new ToolkitPluginException(msg, fileNotFoundEx);
                 } catch (CoreException coreEx) {
                     final String msg = Messages.CouldNotCreateToolkitProvider
                         + StringConstants.EXCLAMATION_MARK;
                     log.error(msg, coreEx);
                    throw new ToolkitPluginException(msg, coreEx);
                 }
             }
         }
         postProcess();
     }
 
     /**
      * Creates a {@link ToolkitPluginDescriptor} which hold the attributes
      * of the Toolkit and adds the Descriptor to the given CompSystem.
      * @param element an IConfigurationElement
      * @param compSystem the CompSystem
      * @return {@link ToolkitPluginDescriptor}
      * Constants.ATTR_<attribute_name>
      * @throws ToolkitPluginException if an error occurs
      */
     private ToolkitPluginDescriptor createToolkitDescriptor(
         IConfigurationElement  element, CompSystem compSystem) 
         throws ToolkitPluginException {
         
         final String toolkitId = element.getAttribute(
             ToolkitConstants.ATTR_TOOLKITID);
         try {
             if (compSystem.getToolkitPluginDescriptor(toolkitId) == null) {
                 
                 final String name = element.getAttribute(
                     ToolkitConstants.ATTR_NAME);
                 final String level = element.getAttribute(
                     ToolkitConstants.ATTR_LEVEL);
                 final boolean isUserToolkit = Boolean.parseBoolean(element
                     .getAttribute(ToolkitConstants.ATTR_ISUSERTOOLKIT));
                 final String includes = String.valueOf(element.getAttribute(
                     ToolkitConstants.ATTR_INCLUDES));
                 final String depends = String.valueOf(element.getAttribute(
                     ToolkitConstants.ATTR_DEPENDS));
                 final int order = Integer.parseInt(element.getAttribute(
                     ToolkitConstants.ATTR_ORDER));
                 final ConfigVersion configVersion = compSystem
                     .getConfigVersion();
                 final int majorVersion = configVersion.getMajorVersion();
                 final int minorVersion = configVersion.getMinorVersion();
                 final ToolkitPluginDescriptor descr = 
                     new ToolkitPluginDescriptor(toolkitId, name, 
                         includes, depends, level, order, isUserToolkit, 
                         majorVersion, minorVersion);
                 compSystem.addToolkitPluginDescriptor(toolkitId, descr);
                 return descr;
             }
         } catch (NumberFormatException e) {
             log.error(DebugConstants.ERROR, e);
             throw new ToolkitPluginException(
                 Messages.ErrorWhileReadingAttributes + StringConstants.COLON
                 + StringConstants.SPACE
                 + String.valueOf(toolkitId), e);
         } catch (InvalidRegistryObjectException e) {
             log.error(DebugConstants.ERROR, e);
             throw new ToolkitPluginException(
                 Messages.ErrorWhileReadingAttributes + StringConstants.COLON
                 + StringConstants.SPACE
                 + String.valueOf(toolkitId), e);
         } 
         return null;
     }
     
     /**
      * @return the instance
      */
     public static ComponentBuilder getInstance() {
         if (instance == null) {
             instance = new ComponentBuilder();
         } 
         return instance;
     }
 
     /**
      * Returns a <code>CompSystem</code> with all components which can be
      * tested by Jubula. The configuration files (ComponentConfiguration.xml)
      * will be read from the installed Toolkit-Plugins.
      * @return the CompSystem a<code>CompSystem</code> object.
      */
     public CompSystem getCompSystem() {
         if (super.getCompSystem() == null) {
             try {
                 initCompSystem();
             } catch (RuntimeException e) {
                 log.error(e.getMessage());
                 throw new GDConfigXmlException(e.getMessage(), 
                     MessageIDs.E_GENERAL_COMPONENT_ERROR);
             } catch (ToolkitPluginException tke) {
                 throw new GDConfigXmlException(tke.getMessage(), 
                     MessageIDs.E_GENERAL_COMPONENT_ERROR);
             }
         }
         return super.getCompSystem();
     }
     
     /**
      * 
      * @return The IDs of the installed independent CompSystems without
      * abstract an concrete.
      */
     @SuppressWarnings("unchecked")
     public List<String> getLevelToolkitIds() {
         List<ToolkitPluginDescriptor> toolkitDescriptors = 
             super.getCompSystem().getIndependentToolkitPluginDescriptors(true);
 
         List<String> toolkitIds = new ArrayList<String>();
         
         for (ToolkitPluginDescriptor desc : toolkitDescriptors) {
             toolkitIds.add(desc.getToolkitID());
         }
         
         return toolkitIds;
     }
         
 }
