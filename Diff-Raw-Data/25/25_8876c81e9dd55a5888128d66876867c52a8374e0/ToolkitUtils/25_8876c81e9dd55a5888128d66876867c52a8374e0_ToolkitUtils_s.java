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
 package org.eclipse.jubula.toolkit.common.utils;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.jubula.toolkit.common.exception.ToolkitPluginException;
 import org.eclipse.jubula.toolkit.common.i18n.Messages;
 import org.eclipse.jubula.toolkit.common.xml.businessprocess.ComponentBuilder;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.ToolkitConstants;
 import org.eclipse.jubula.tools.xml.businessmodell.CompSystem;
 import org.eclipse.jubula.tools.xml.businessmodell.ToolkitPluginDescriptor;
 import org.eclipse.swt.widgets.Composite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author BREDEX GmbH
  * @created 24.04.2007
  */
 public class ToolkitUtils {
     /** The logger */
     private static final Logger LOG = 
         LoggerFactory.getLogger(ToolkitUtils.class);
     
     /** Map with description of toolkit level abstractness*/
     private static Map<String, Integer> abstractness = 
         new HashMap<String, Integer>();
     static {
         abstractness.put(ToolkitConstants.LEVEL_ABSTRACT, 0);
         abstractness.put(ToolkitConstants.LEVEL_CONCRETE, 1);
         abstractness.put(ToolkitConstants.LEVEL_TOOLKIT, 2);
     }
     
     /**
      * utility constructor
      */
     private ToolkitUtils() {
         // nothing here
     }
 
     /**
     * Returns an absolute (resolved) path of the given Plugin and the
     * given Plugin-relative path.
     * @param plugin the Plugin
     * @param pluginRelPath the relative path
     * @return an absolute (resolved) path or null if the path could 
     * not be resolved.
      */
     public static URL getURL(Plugin plugin, String pluginRelPath) {
         
         URL unresolvedUrl = plugin.getBundle().getEntry(pluginRelPath);
         URL fileURL = null;
         try {
            fileURL = FileLocator.toFileURL(unresolvedUrl);
         } catch (IOException e) {
             StringBuilder logMsg = new StringBuilder();
             logMsg.append(Messages.CouldNotResolvePath);
             logMsg.append(StringConstants.COLON);
             logMsg.append(StringConstants.SPACE);
             logMsg.append(pluginRelPath);
             logMsg.append(StringConstants.SPACE);
             logMsg.append(Messages.OfPlugin);
             logMsg.append(StringConstants.COLON);
             logMsg.append(StringConstants.SPACE);
             LOG.error(logMsg.toString(), e);
         }
         return fileURL;
     }
     
     /**
      * Checks if the given toolkit level1 is more concrete than the given
      * toolkit level2.
      * @param level1 a toolkit level.
      * @param level2 a toolkit level.
      * @return true if level1 is more concrete than level2, false otherwise.
      */
     public static boolean isToolkitMoreConcrete(String level1, String level2) {
         String lvl1 = level1;
         String lvl2 = level2;
         final String emptyLevel = StringConstants.EMPTY;
         if (emptyLevel.equals(lvl1)) {
             lvl1 = ToolkitConstants.LEVEL_ABSTRACT;
         }
         if (emptyLevel.equals(lvl2)) {
             lvl1 = ToolkitConstants.LEVEL_ABSTRACT;
         }
         final int abstractness1 = abstractness.get(lvl1);
         final int abstractness2 = abstractness.get(lvl2);
         return abstractness1 > abstractness2;   
     }
     
     /**
      * Gets the toolkit name of the given toolkitID.
      * @param toolkitId a Toolkit ID.
      * @return the toolkit name of the given toolkitID or the given ID if no
      * name was found.
      */
     @SuppressWarnings("unchecked")
     public static String getToolkitName(String toolkitId) {
         final List<ToolkitPluginDescriptor> toolkitPluginDescriptors = 
             ComponentBuilder.getInstance().getCompSystem()
                 .getAllToolkitPluginDescriptors();
         for (ToolkitPluginDescriptor desc : toolkitPluginDescriptors) {
             if (desc.getToolkitID().equalsIgnoreCase(toolkitId)) {
                 return desc.getName();
             }
         }
         return toolkitId;
     }
     
     /**
      * Checks if the given toolkit <code>toolkitId</code> somehow includes, or
      * inherits from, <code>includedToolkitId</code>.
      * @param toolkitId The toolkit to check.
      * @param includedToolkitId The toolkit that may be included.
      * @return <code>true</code> if the given <code>toolkitId</code> includes, 
      *         at some point in its hierarchy, <code>includedToolkitId</code>.
      */
     public static boolean doesToolkitInclude(String toolkitId, 
         String includedToolkitId) {
         
         CompSystem compSys = ComponentBuilder.getInstance().getCompSystem();
         
         ToolkitPluginDescriptor desc = 
             compSys.getToolkitPluginDescriptor(toolkitId);
         
         String includes = desc.getIncludes();
         
         if (includes != null && includes.equals(includedToolkitId)) {
             return true;
         }
         
         while (desc != null && includes != null 
             && !ToolkitConstants.EMPTY_EXTPOINT_ENTRY.equals(includes)) {
             
             desc = compSys.getToolkitPluginDescriptor(includes);
             if (desc != null) {
                 includes = desc.getIncludes();
                 if (includes != null && includes.equals(includedToolkitId)) {
                     return true;
                 }
             }
         }
         
         return false;
     }
     
     /**
      * Loads and creates an instance of the given Class-Name with the given 
      * constructor parameter with the given ClassLoader.
      *  
      * @param className the class name of the AutConfigComponent.
      * @param classLoader the ClassLoader to load the class with.
      * @param parent the parent of the AutConfigComponent.
      * @param style the SWT-Style
      * @param autConfig the aut configuration as a Map.
      * @param autName the name of the AUT that will be using this configuration.
      * @return an instance of the AutConfigComponent.
      */
     @SuppressWarnings("unchecked")
     public static Composite createAutConfigComponent(String className, 
         ClassLoader classLoader, Composite parent, int style, 
         Map<String, String> autConfig, String autName) 
         throws ToolkitPluginException {
         
         Composite autConfigDialog = null;
         StringBuilder logMsg = new StringBuilder();
         logMsg.append(Messages.FailedLoading);
         logMsg.append(StringConstants.SLASH);
         logMsg.append(Messages.InstantiatingClass);
         logMsg.append(StringConstants.COLON);
         logMsg.append(StringConstants.SPACE);
         try {
             Class autConfigComponentClass = classLoader.loadClass(className);
             Constructor<Composite> constructor = autConfigComponentClass
                 .getConstructor(new Class[]{
                     Composite.class, int.class, Map.class, String.class});
             autConfigDialog = constructor.newInstance(
                 new Object[]{parent, style, autConfig, autName});
         } catch (SecurityException e) {
             LOG.error(e.getLocalizedMessage(), e);
             throw new ToolkitPluginException(
                 logMsg.toString()
                 + String.valueOf(className), e);
         } catch (IllegalArgumentException e) {
             LOG.error(e.getLocalizedMessage(), e);
             throw new ToolkitPluginException(
                 logMsg.toString()
                 + String.valueOf(className), e);
         } catch (ClassNotFoundException e) {
             LOG.error(e.getLocalizedMessage(), e);
             throw new ToolkitPluginException(
                 logMsg.toString()
                 + String.valueOf(className), e);
         } catch (NoSuchMethodException e) {
             LOG.error(e.getLocalizedMessage(), e);
             throw new ToolkitPluginException(
                 logMsg.toString()
                 + String.valueOf(className), e);
         } catch (InstantiationException e) {
             LOG.error(e.getLocalizedMessage(), e);
             throw new ToolkitPluginException(
                 logMsg.toString()
                 + String.valueOf(className), e);
         } catch (IllegalAccessException e) {
             LOG.error(e.getLocalizedMessage(), e);
             throw new ToolkitPluginException(
                 logMsg.toString()
                 + String.valueOf(className), e);
         } catch (InvocationTargetException e) {
             LOG.error(e.getLocalizedMessage(), e);
             throw new ToolkitPluginException(
                 logMsg.toString()
                 + String.valueOf(className), e);
         }
         return autConfigDialog;
     }    
     
     
 }
