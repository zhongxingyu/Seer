 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 // Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
 // Jad home page: http://www.kpdus.com/jad.html
 // Decompiler options: packimports(3)
 // Source File Name:   WFAkukPlugin.java
 package de.cismet.cids.navigator.plugins;
 
 import Sirius.navigator.plugin.context.PluginContext;
 import Sirius.navigator.plugin.interfaces.*;
 import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 
 import Sirius.server.middleware.types.MetaObject;
 
 import org.apache.log4j.Logger;
 
 import java.io.*;
 
 import java.util.*;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public class WFAkukPlugin implements PluginSupport {
 
     //~ Instance fields --------------------------------------------------------
 
     private final Logger log = Logger.getLogger(getClass());
     private HashMap pluginMethods;
     private ShowInAkuk showInAkukMethod;
     private PluginContext pluginContext;
     private String akukClassKey;
     private String exchangeFile;
     private String exchangeDirectory;
     private String triggerExe;
     private String home;
     private String fs;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new WFAkukPlugin object.
      *
      * @param  pluginContext  DOCUMENT ME!
      */
     public WFAkukPlugin(final PluginContext pluginContext) {
         pluginMethods = new HashMap();
         showInAkukMethod = new ShowInAkuk();
         home = "";
         fs = "";
         pluginMethods.put(showInAkukMethod.getId(), showInAkukMethod);
         this.pluginContext = pluginContext;
         akukClassKey = pluginContext.getEnvironment().getParameter("akukClassKey");
         exchangeFile = pluginContext.getEnvironment().getParameter("exchangeFile");
         exchangeDirectory = pluginContext.getEnvironment().getParameter("exchangeDirectory");
         triggerExe = pluginContext.getEnvironment().getParameter("exchangeTriggerExe");
         home = System.getProperty("user.home");
         fs = System.getProperty("file.separator");
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public PluginUI getUI(final String id) {
         return null;
     }
 
     @Override
     public PluginMethod getMethod(final String id) {
         return (PluginMethod)pluginMethods.get(id);
     }
 
     @Override
     public void setVisible(final boolean flag) {
     }
 
     @Override
     public void setActive(final boolean flag) {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getId() {
         return "wfakukplugin";
     }
 
     @Override
     public Iterator getUIs() {
         final LinkedList ll = new LinkedList();
         return ll.iterator();
     }
 
     @Override
     public PluginProperties getProperties() {
         return null;
     }
 
     @Override
     public Iterator getMethods() {
         return pluginMethods.values().iterator();
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class ShowInAkuk implements PluginMethod {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new ShowInAkuk object.
          */
         private ShowInAkuk() {
             super();
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void invoke() throws Exception {
             log.fatal("Show in AKUK");
             final Collection selectedNodes = pluginContext.getMetadata().getSelectedNodes();
             final Iterator it = selectedNodes.iterator();
            final Vector mos = new Vector();
             try {
                 while (it.hasNext()) {
                     if (log.isDebugEnabled()) {
                         log.debug("selected node");
                     }
                     final DefaultMetaTreeNode node = (DefaultMetaTreeNode)it.next();
                     if (node instanceof ObjectTreeNode) {
                         final MetaObject mo = ((ObjectTreeNode)node).getMetaObject();
                         if (mo.getClassKey().equalsIgnoreCase(akukClassKey)) {
                             if (log.isDebugEnabled()) {
                                 log.debug("Metaobject hinzugefuegt");
                             }
                             mos.add(mo);
                         } else {
                             if (log.isDebugEnabled()) {
                                 log.debug((new StringBuilder()).append("falscher ClassKey:").append(mo.getClassKey())
                                             .append("=?=").append(akukClassKey).toString());
                             }
                         }
                     } else {
                         log.warn((new StringBuilder()).append("Node ist kein ObjectTreeNode, sondern:").append(
                                 node.getClass()).toString());
                     }
                 }
                 if (mos.size() > 0) {
                     String outString = "";
                     for (final Iterator i$ = mos.iterator(); i$.hasNext();) {
                         final MetaObject mo = (MetaObject)i$.next();
                         outString = (new StringBuilder()).append(outString).append(mo.getID()).append("\r\n")
                                     .toString();
                     }
                     if (log.isDebugEnabled()) {
                         log.debug((new StringBuilder()).append("outstring").append(outString).toString());
                     }
                     try {
                         (new File((new StringBuilder()).append(home).append(fs).append(exchangeDirectory).toString()))
                                 .mkdirs();
                         final BufferedWriter out = new BufferedWriter(new FileWriter(
                                     (new StringBuilder()).append(home).append(fs).append(exchangeDirectory).append(
                                         fs).append(exchangeFile).toString()));
                         out.write(outString);
                         out.close();
                     } catch (IOException e) {
                         log.error("Fehler beim Schreiben des WF-AKUK Exchange Files", e);
                     }
                     try {
                        final Runtime rt = Runtime.getRuntime();
                        rt.exec((new StringBuilder()).append(home).append(fs).append(exchangeDirectory).append(fs)
                                    .append(triggerExe).toString());
                     } catch (Throwable t) {
                         log.error("Fehler beim Aufruf der WF-AKUK triggerExe", t);
                     }
                 }
             } catch (Throwable t) {
                 log.fatal("Unerwarteter Fehler im WF-AKUK-Plugin", t);
             }
         }
 
         @Override
         public String getId() {
             return getClass().getName();
         }
     }
 }
