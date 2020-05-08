 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.plugin;
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.plugin.context.*;
 import Sirius.navigator.plugin.interfaces.*;
 import Sirius.navigator.plugin.ui.*;
 import Sirius.navigator.resource.*;
 import Sirius.navigator.ui.embedded.*;
 
 import Sirius.server.newuser.permission.PermissionHolder;
 
 import org.apache.commons.digester.*;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import org.xml.sax.*;
 
 import java.io.*;
 
 import java.lang.reflect.*;
 
 import java.net.*;
 
 import java.util.*;
 
 import javax.swing.*;
 
 /**
  * blah.
  *
  * @author   Pascal
  * @version  1.0 02/15/2003
  */
 public final class PluginFactory {
 
     //~ Static fields/initializers ---------------------------------------------
 
     /** singleton shared instance. */
     // private static PluginFactory factory = null;
 
     private static final ResourceManager resource = ResourceManager.getManager();
 
     //~ Instance fields --------------------------------------------------------
 
     private final Logger logger;
 
     private final PreloadPluginRuleSet preloadRuleSet;
     private final LoadPluginRuleSet loadRuleSet;
 
     private final String schemaLocation;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates the singleton shared instance of PluginFactory.
      */
     protected PluginFactory() {
         logger = Logger.getLogger(this.getClass());
         // log = new Log4jFactory().getInstance("navigator.plugin.factory.digester");
         final Logger digesterLogger = Logger.getLogger(PluginFactory.LoadPluginRuleSet.class);
         digesterLogger.setLevel(Level.WARN);
 
         preloadRuleSet = new PreloadPluginRuleSet();
         loadRuleSet = new LoadPluginRuleSet();
 
         schemaLocation = resource.pathToIURIString(PropertyManager.getManager().getPluginPath() + "plugin.xsd"); // NOI18N
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Return the singleton PluginFactory instance.
      *
      * @param   descriptor  DOCUMENT ME!
      * @param   validating  DOCUMENT ME!
      *
      * @throws  IOException            DOCUMENT ME!
      * @throws  SAXException           DOCUMENT ME!
      * @throws  URISyntaxException     DOCUMENT ME!
      * @throws  FileNotFoundException  DOCUMENT ME!
      */
     /*public final static PluginFactory getFactory()
      * { if(factory == null) {     factory = new PluginFactory(); }  return factory;}*/
 
     protected void preloadPlugin(final PluginDescriptor descriptor, final boolean validating) throws IOException,
         SAXException,
         URISyntaxException,
         FileNotFoundException {
         // InputStream inputStream = this.getXMLDescriptorInputStream(descriptor.getUrl());
         final InputStream inputStream = this.getXMLDescriptorInputStream(descriptor.getPluginPath());
         final Digester digester = new Digester();
 
         if (validating) {
             if (logger.isInfoEnabled()) {
                 logger.info("plugin xml schema validation turned off to improve performance"); // NOI18N
             }
             // if(logger.isDebugEnabled())logger.debug("enabling xml schema validation: '" + this.getSchemaLocation() +
             // "'"); digester.setSchema(this.getSchemaLocation()); digester.setValidating(validating);
         }
 
         digester.push(descriptor);
         digester.addRuleSet(preloadRuleSet);
 
         digester.parse(inputStream);
         inputStream.close();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   descriptor  DOCUMENT ME!
      *
      * @throws  IOException                DOCUMENT ME!
      * @throws  SAXException               DOCUMENT ME!
      * @throws  URISyntaxException         DOCUMENT ME!
      * @throws  FileNotFoundException      DOCUMENT ME!
      * @throws  InvocationTargetException  DOCUMENT ME!
      */
     protected void loadPlugin(final PluginDescriptor descriptor) throws IOException,
         SAXException,
         URISyntaxException,
         FileNotFoundException,
         InvocationTargetException {
         // InputStream inputStream = this.getXMLDescriptorInputStream(descriptor.getUrl());
         final InputStream inputStream = this.getXMLDescriptorInputStream(descriptor.getPluginPath());
         final Digester digester = new Digester();
 
//        digester.setLogger(log);
         digester.push(new PluginFactory.FactoryCore(descriptor));
         digester.addRuleSet(loadRuleSet);
 
         digester.parse(inputStream);
         inputStream.close();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getSchemaLocation() {
         return this.schemaLocation;
     }
 
     // helper methods ----------------------------------------------------------
 
     /**
      * Return the plugin descriptor XML configuration file (plugin.xml).
      *
      * @param   pluginUrl  DOCUMENT ME!
      *
      * @return  the input stream, that reads the XML file
      *
      * @throws  URISyntaxException     DOCUMENT ME!
      * @throws  FileNotFoundException  DOCUMENT ME!
      * @throws  IOException            DOCUMENT ME!
      */
     private InputStream getXMLDescriptorInputStream(final URL pluginUrl) throws URISyntaxException,
         FileNotFoundException,
         IOException {
         if (logger.isDebugEnabled()) {
             logger.debug("loading plugin XML descriptor '" + pluginUrl.toString() + "/"
                         + PluginDescriptor.XML_DESCRIPTOR + "'"); // NOI18N
         }
 
         final File file = new File(new URI(pluginUrl.toString() + "/" + PluginDescriptor.XML_DESCRIPTOR)); // NOI18N
         return new BufferedInputStream(new FileInputStream(file), 8192);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   pluginPath  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  FileNotFoundException  DOCUMENT ME!
      * @throws  MalformedURLException  DOCUMENT ME!
      * @throws  IOException            DOCUMENT ME!
      */
     private InputStream getXMLDescriptorInputStream(final String pluginPath) throws FileNotFoundException,
         MalformedURLException,
         IOException {
         final String pluginDescriptorPath = pluginPath + PluginDescriptor.XML_DESCRIPTOR;
         if (logger.isDebugEnabled()) {
             logger.debug("loading plugin XML descriptor from remote URL '" + pluginDescriptorPath + "'"); // NOI18N
         }
 
         return new BufferedInputStream(resource.getResourceAsStream(pluginDescriptorPath), 16384);
 
         /*if(pluginPath.indexOf("http://") == 0 || pluginPath.indexOf("https://") == 0)
          * { if(logger.isDebugEnabled())logger.debug("loading plugin XML descriptor from remote URL '" +
          * pluginDescriptorPath + "'"); URL pluginURL = new URL(pluginPath); return new
          * BufferedInputStream(pluginURL.openStream()); } else { if(logger.isDebugEnabled())logger.debug("loading plugin
          * XML descriptor from local filesystem '" + pluginDescriptorPath + "'"); File file = new
          * File(pluginDescriptorPath); return new BufferedInputStream(new FileInputStream(file));}*/
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * Digester RuleSets -------------------------------------------------------.
      *
      * @version  $Revision$, $Date$
      */
     private class PreloadPluginRuleSet extends RuleSetBase {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void addRuleInstances(final Digester digester) {
             // <plugin> ........................................................
             // set PluginDescriptor Bean properties (<plugin> attributes -> properties)
             digester.addSetProperties("plugin"); // NOI18N
 
             // <metainfo> ......................................................
             // create PluginMetaInfo object
             digester.addObjectCreate("plugin/metainfo", "Sirius.navigator.plugin.PluginMetaInfo"); // NOI18N
             // add PluginMetaInfo to PluginDescriptor
             digester.addSetNext("plugin/metainfo", "setMetaInfo", "Sirius.navigator.plugin.PluginMetaInfo"); // NOI18N
             // set PluginMetaInfo Bean properties (<plugin> attributes -> properties)
             // digester.addSetProperties("plugin/metainfo/properties");
             digester.addSetProperties("plugin/metainfo"); // NOI18N
             // set PluginMetaInfo description (<description></description> -> description)
             digester.addCallMethod("plugin/metainfo/description", "setDescription", 0); // NOI18N
 
             // <rights> ........................................................
             // set the usernames
             digester.addCallMethod("plugin/deployment/rights/users/name", "addUser", 0); // NOI18N
             // set the usergroupnames
             digester.addCallMethod("plugin/deployment/rights/usergroups/name", "addUsergroup", 0); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class LoadPluginRuleSet extends RuleSetBase {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void addRuleInstances(final Digester digester) {
             // <properties> section ............................................
             // capabilities
             digester.addCallMethod(
                 "plugin/properties/capabilities",
                 "setCapabilities",
                 5,
                 new String[] {
                     "java.lang.Boolean",
                     "java.lang.Boolean",
                     "java.lang.Boolean",
                     "java.lang.Boolean",
                     "java.lang.Boolean"
                 });                                                                        // NOI18N
             digester.addCallParam("plugin/properties/capabilities/progressobservable", 0); // NOI18N
             digester.addCallParam("plugin/properties/capabilities/propertyobservable", 1); // NOI18N
             digester.addCallParam("plugin/properties/capabilities/internationalized", 2);  // NOI18N
             digester.addCallParam("plugin/properties/capabilities/unloadable", 3);         // NOI18N
             digester.addCallParam("plugin/properties/capabilities/deactivateable", 4);     // NOI18N
 
             // internationalization
             digester.addCallMethod("plugin/properties/internationalization/defaultlocale", "setDefaultLocale", 0); // NOI18N
             // add PluginLocale object
             digester.addCallMethod("plugin/properties/internationalization/locales/locale", "addLocale", 4); // NOI18N
             digester.addCallParam("plugin/properties/internationalization/locales/locale/name", 0);          // NOI18N
             digester.addCallParam("plugin/properties/internationalization/locales/locale/language", 1);      // NOI18N
             digester.addCallParam("plugin/properties/internationalization/locales/locale/country", 2);       // NOI18N
             digester.addCallParam("plugin/properties/internationalization/locales/locale/resourcefile", 3);  // NOI18N
 
             // <deployment> section ............................................
             // set plugin class name
             digester.addCallMethod("plugin/deployment/pluginclass", "setClassName", 0); // NOI18N
 
             // add libraries (jar files)
             digester.addCallMethod("plugin/deployment/libraries/jar", "addLibrary", 0); // NOI18N
 
             // add the plugin parameters
             digester.addCallMethod("plugin/deployment/params/param", "addParameter", 2); // NOI18N
             digester.addCallParam("plugin/deployment/params/param/name", 0);             // NOI18N
             digester.addCallParam("plugin/deployment/params/param/value", 1);            // NOI18N
 
             // add the attribute id <-> attribute name mappings
             digester.addCallMethod("plugin/deployment/mappings/attribute", "addAttributeMapping", 2); // NOI18N
             digester.addCallParam("plugin/deployment/mappings/attribute/name", 0);                    // NOI18N
             digester.addCallParam("plugin/deployment/mappings/attribute/id", 1);                      // NOI18N
 
             // it's time to create the plugin
             digester.addCallMethod("plugin/deployment", "createPluginInstance"); // NOI18N
 
             // .................................................................
             // add the method
             digester.addCallMethod(
                 "plugin/methods/method",
                 "addMethod",
                 5,
                 new String[] {
                     "java.lang.String",
                     "java.lang.String",
                     "java.lang.String",
                     "java.lang.Boolean",
                     "java.lang.Long"
                 });                                                          // NOI18N
             digester.addCallParam("plugin/methods/method/id", 0);            // NOI18N
             digester.addCallParam("plugin/methods/method/name", 1);          // NOI18N
             digester.addCallParam("plugin/methods/method/description", 2);   // NOI18N
             digester.addCallParam("plugin/methods/method/multithreaded", 3); // NOI18N
             digester.addCallParam("plugin/methods/method/availability", 4);  // NOI18N
             // register methods digester.addCallMethod("plugin/methods", "registerMethods");
             // ..................................................................
 
             // register methods digester.addCallMethod("plugin/deployment/events", "registerMethods"); register event
             // listeners @deprecated! use PluginContext.getMetadata.addXXXListener()
             // digester.addCallMethod("plugin/deployment/events/selection/nodes", "registerNodeSelectionEvent", 0);
             // digester.addCallMethod("plugin/deployment/events/selection/attributes",
             // "registerAttributeSelectionEvent", 0);
 
             // <ui><widget><component> section ---------------------------------
 
             // create PluginComponentProxy
             digester.addObjectCreate(
                 "plugin/ui/widgets/widget/component",
                 "Sirius.navigator.plugin.PluginUIDescriptor");                                       // NOI18N
             digester.addSetNext(
                 "plugin/ui/widgets/widget/component",
                 "addUIDescriptor",
                 "Sirius.navigator.plugin.PluginUIDescriptor");                                       // NOI18N
             digester.addCallMethod("plugin/ui/widgets/widget/component/id", "setId", 0);             // NOI18N
             digester.addCallMethod("plugin/ui/widgets/widget/component/name", "setName", 0);         // NOI18N
             digester.addCallMethod("plugin/ui/widgets/widget/component/tooltip", "setToolTip", 0);   // NOI18N
             digester.addCallMethod("plugin/ui/widgets/widget/component/icon", "setIconName", 0);     // NOI18N
             digester.addCallMethod("plugin/ui/widgets/widget/component/position", "setPosition", 0); // NOI18N
             digester.addCallMethod(
                 "plugin/ui/widgets/widget/component/preferredindex",
                 "setPreferredIndex",
                 0,
                 new String[] { "java.lang.Integer" });                                               // NOI18N
             digester.addCallMethod(
                 "plugin/ui/widgets/widget/component/componentevents",
                 "setPluginComponentEventsEnabled",
                 0,
                 new String[] { "java.lang.Boolean" });                                               // NOI18N
             // <ui><widget><container> section ---------------------------------
             // no container
             digester.addCallMethod("plugin/ui/widgets/widget/container/none", "addAsComponent"); // NOI18N
             // panel
             digester.addCallMethod("plugin/ui/widgets/widget/container/panel/layout", "addAsPanel", 0); // NOI18N
             // scrollpane
             digester.addCallMethod("plugin/ui/widgets/widget/container/scrollpane", "addAsScrollPane"); // NOI18N
             // floatingframe
             digester.addObjectCreate(
                 "plugin/ui/widgets/widget/container/floatingframe",
                 "Sirius.navigator.plugin.ui.PluginFloatingFrameConfigurator"); // NOI18N
             digester.addSetNext(
                 "plugin/ui/widgets/widget/container/floatingframe",
                 "addAsFloatingFrame",
                 "Sirius.navigator.plugin.ui.PluginFloatingFrameConfigurator"); // NOI18N
             digester.addCallMethod(
                 "plugin/ui/widgets/widget/container/floatingframe/menubaravailable",
                 "setMenuBarAvailable",
                 0,
                 new String[] { "java.lang.Boolean" });                         // NOI18N
             digester.addCallMethod(
                 "plugin/ui/widgets/widget/container/floatingframe/swapmenubar",
                 "setSwapMenuBar",
                 0,
                 new String[] { "java.lang.Boolean" });                         // NOI18N
             digester.addCallMethod(
                 "plugin/ui/widgets/widget/container/floatingframe/disablemenubar",
                 "setDisableMenuBar",
                 0,
                 new String[] { "java.lang.Boolean" });                         // NOI18N
             digester.addCallMethod(
                 "plugin/ui/widgets/widget/container/floatingframe/toolbaravailable",
                 "setToolBarAvailable",
                 0,
                 new String[] { "java.lang.Boolean" });                         // NOI18N
             digester.addCallMethod(
                 "plugin/ui/widgets/widget/container/floatingframe/swaptoolbar",
                 "setSwapToolBar",
                 0,
                 new String[] { "java.lang.Boolean" });                         // NOI18N
             digester.addCallMethod(
                 "plugin/ui/widgets/widget/container/floatingframe/disabletoolbar",
                 "setDisableToolBar",
                 0,
                 new String[] { "java.lang.Boolean" });                         // NOI18N
             digester.addCallMethod(
                 "plugin/ui/widgets/widget/container/floatingframe/floatingevents",
                 "setFloatingEventsEnabled",
                 0,
                 new String[] { "java.lang.Boolean" });                         // NOI18N
             // <ui><actions><toolbar> section ----------------------------------
             digester.addObjectCreate(
                 "plugin/ui/actions/toolbar/properties",
                 "Sirius.navigator.plugin.PluginActionDescriptor");                             // NOI18N
             digester.addSetNext(
                 "plugin/ui/actions/toolbar/properties",
                 "createPluginToolBar",
                 "Sirius.navigator.plugin.PluginActionDescriptor");                             // NOI18N
             digester.addCallMethod("plugin/ui/actions/toolbar/properties/name", "setName", 0); // NOI18N
             digester.addCallMethod(
                 "plugin/ui/actions/toolbar/properties/floatable",
                 "setFloatable",
                 0,
                 new String[] { "java.lang.Boolean" });                                         // NOI18N
 
             digester.addObjectCreate(
                 "plugin/ui/actions/toolbar/buttons/button",
                 "Sirius.navigator.plugin.PluginActionDescriptor");                                         // NOI18N
             digester.addSetNext(
                 "plugin/ui/actions/toolbar/buttons/button",
                 "addToolBarButton",
                 "Sirius.navigator.plugin.PluginActionDescriptor");                                         // NOI18N
             digester.addCallMethod("plugin/ui/actions/toolbar/buttons/button/name", "setName", 0);         // NOI18N
             digester.addCallMethod("plugin/ui/actions/toolbar/buttons/button/mnemonic", "setMnemonic", 0); // NOI18N
             digester.addCallMethod("plugin/ui/actions/toolbar/buttons/button/tooltip", "setTooltip", 0);   // NOI18N
             digester.addCallMethod("plugin/ui/actions/toolbar/buttons/button/icon", "setIconName", 0);     // NOI18N
             digester.addCallMethod("plugin/ui/actions/toolbar/buttons/button/method", "setMethodId", 0);   // NOI18N
 
             // <ui><actions><menubar> section ----------------------------------
             digester.addObjectCreate(
                 "plugin/ui/actions/menu/properties",
                 "Sirius.navigator.plugin.PluginActionDescriptor");                                  // NOI18N
             digester.addSetNext(
                 "plugin/ui/actions/menu/properties",
                 "createPluginMenu",
                 "Sirius.navigator.plugin.PluginActionDescriptor");                                  // NOI18N
             digester.addCallMethod("plugin/ui/actions/menu/properties/name", "setName", 0);         // NOI18N
             digester.addCallMethod("plugin/ui/actions/menu/properties/mnemonic", "setMnemonic", 0); // NOI18N
             digester.addCallMethod("plugin/ui/actions/menu/properties/icon", "setIconName", 0);     // NOI18N
 
             digester.addObjectCreate(
                 "plugin/ui/actions/menu/items/item",
                 "Sirius.navigator.plugin.PluginActionDescriptor");                                        // NOI18N
             digester.addSetNext(
                 "plugin/ui/actions/menu/items/item",
                 "addMenuItem",
                 "Sirius.navigator.plugin.PluginActionDescriptor");                                        // NOI18N
             digester.addCallMethod("plugin/ui/actions/menu/items/item/name", "setName", 0);               // NOI18N
             digester.addCallMethod("plugin/ui/actions/menu/items/item/mnemonic", "setMnemonic", 0);       // NOI18N
             digester.addCallMethod("plugin/ui/actions/menu/items/item/tooltip", "setTooltip", 0);         // NOI18N
             digester.addCallMethod("plugin/ui/actions/menu/items/item/icon", "setIconName", 0);           // NOI18N
             digester.addCallMethod("plugin/ui/actions/menu/items/item/method", "setMethodId", 0);         // NOI18N
             digester.addCallMethod("plugin/ui/actions/menu/items/item/accelerator", "setAccelerator", 0); // NOI18N
             digester.addCallMethod(
                 "plugin/ui/actions/menu/items/item/separator",
                 "setSeparator",
                 0,
                 new String[] { "java.lang.Boolean" });                                                    // NOI18N
 
             // <ui><actions><popup> section ----------------------------------
             digester.addObjectCreate(
                 "plugin/ui/actions/popupmenu/properties",
                 "Sirius.navigator.plugin.PluginActionDescriptor");                                       // NOI18N
             digester.addSetNext(
                 "plugin/ui/actions/popupmenu/properties",
                 "createPluginPopupMenu",
                 "Sirius.navigator.plugin.PluginActionDescriptor");                                       // NOI18N
             digester.addCallMethod("plugin/ui/actions/popupmenu/properties/name", "setName", 0);         // NOI18N
             digester.addCallMethod("plugin/ui/actions/popupmenu/properties/mnemonic", "setMnemonic", 0); // NOI18N
             digester.addCallMethod("plugin/ui/actions/popupmenu/properties/icon", "setIconName", 0);     // NOI18N
 
             digester.addObjectCreate(
                 "plugin/ui/actions/popupmenu/items/item",
                 "Sirius.navigator.plugin.PluginActionDescriptor");                                             // NOI18N
             digester.addSetNext(
                 "plugin/ui/actions/popupmenu/items/item",
                 "addPopupMenuItem",
                 "Sirius.navigator.plugin.PluginActionDescriptor");                                             // NOI18N
             digester.addCallMethod("plugin/ui/actions/popupmenu/items/item/name", "setName", 0);               // NOI18N
             digester.addCallMethod("plugin/ui/actions/popupmenu/items/item/mnemonic", "setMnemonic", 0);       // NOI18N
             digester.addCallMethod("plugin/ui/actions/popupmenu/items/item/tooltip", "setTooltip", 0);         // NOI18N
             digester.addCallMethod("plugin/ui/actions/popupmenu/items/item/icon", "setIconName", 0);           // NOI18N
             digester.addCallMethod("plugin/ui/actions/popupmenu/items/item/method", "setMethodId", 0);         // NOI18N
             digester.addCallMethod("plugin/ui/actions/popupmenu/items/item/accelerator", "setAccelerator", 0); // NOI18N
             digester.addCallMethod(
                 "plugin/ui/actions/popupmenu/items/item/separator",
                 "setSeparator",
                 0,
                 new String[] { "java.lang.Boolean" });                                                         // NOI18N
 
             // @deprecated
             // digester.addCallMethod("plugin/deployment/methods/method/id", "setId", 0);
             // digester.addCallMethod("plugin/deployment/methods/method/name", "setName", 0);
             // digester.addCallMethod("plugin/deployment/methods/method/description", "setDescription", 0);
             /*digester.addObjectCreate("plugin/deployment/methods/method",
              * "Sirius.navigator.pluginsupport.PluginMethodDescriptor");
              * digester.addSetNext("plugin/deployment/methods/method","addMethodDescriptor",
              * "Sirius.navigator.pluginsupport.PluginMethodDescriptor");
              * digester.addBeanPropertySetter("plugin/deployment/methods/method/id");
              * digester.addBeanPropertySetter("plugin/deployment/methods/method/name");digester.addBeanPropertySetter("plugin/deployment/methods/method/description");*/
 
             // digester.addCallMethod("plugin/ui/widgets/widget/component/id", "setId");
             // digester.addCallMethod("plugin/ui/widgets/widget/component/name", "setName");
 
         }
 
         // @deprecated
         /*
          * private class PluginCreationFactory extends AbstractObjectCreationFactory { public Object
          * createObject(Attributes attributes) throws java.lang.Exception {     String classname =
          * attributes.getValue("pluginclass");     PluginFactory.this.logger.info("creating plugin instance '" +
          * classname + "'");      Class plugin = digester.getClassLoader().loadClass(classname);     return
          * plugin.newInstance(); } }
          */
     }
 
     /**
      * helper factory ----------------------------------------------------------.
      *
      * @version  $Revision$, $Date$
      */
     private class FactoryCore {
 
         //~ Instance fields ----------------------------------------------------
 
         /** a list of libraries, this plugin needs. */
         private ArrayList libraries = new ArrayList();
 
         private HashMap mappingTable = new HashMap();
 
         private HashMap paramTable = new HashMap();
 
         private HashMap methodDescriptors = null;
 
         private HashMap uiDescriptors = null;
 
         private HashMap pluginLocales = null;
 
         // private ArrayList actionDescriptors = new ArrayList();
 
         private PluginDescriptor descriptor = null;
 
         private String className = null;
 
         private PluginUIDescriptor uiDescriptor = null;
 
         private PluginMenu pluginMenu = null;
         private PluginMenu pluginPopupMenu = null;
         private EmbeddedToolBar pluginToolBar = null;
         private String defaultLocale = null;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new FactoryCore object.
          *
          * @param  descriptor  DOCUMENT ME!
          */
         private FactoryCore(final PluginDescriptor descriptor) {
             if (logger.isInfoEnabled()) {
                 logger.info("new Plugin Factory Core instance created"); // NOI18N
             }
             this.descriptor = descriptor;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         private PluginDescriptor getDescriptor() {
             return this.descriptor;
         }
 
         /**
          * <properties> set apabilities ========================================.
          *
          * @param  progressObservable  DOCUMENT ME!
          * @param  propertyObservable  DOCUMENT ME!
          * @param  internationalized   DOCUMENT ME!
          * @param  unloadable          DOCUMENT ME!
          * @param  deactivateable      DOCUMENT ME!
          */
         public void setCapabilities(final Boolean progressObservable,
                 final Boolean propertyObservable,
                 final Boolean internationalized,
                 final Boolean unloadable,
                 final Boolean deactivateable) {
             if (logger.isDebugEnabled()) {
                 logger.debug("setting plugin capabilities"); // NOI18N
             }
             this.descriptor.setProgressObservable(progressObservable.booleanValue());
             this.descriptor.setPropertyObservable(propertyObservable.booleanValue());
             this.descriptor.setInternationalized(internationalized.booleanValue());
             this.descriptor.setUnloadable(unloadable.booleanValue());
             this.descriptor.setDeactivateable(deactivateable.booleanValue());
         }
 
         /**
          * <properties> add locale .............................................
          *
          * @param  defaultLocale  DOCUMENT ME!
          */
         public void setDefaultLocale(final String defaultLocale) {
             this.defaultLocale = defaultLocale;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  name          DOCUMENT ME!
          * @param  language      DOCUMENT ME!
          * @param  country       DOCUMENT ME!
          * @param  resourceFile  DOCUMENT ME!
          */
         public void addLocale(final String name,
                 final String language,
                 final String country,
                 final String resourceFile) {
             if (logger.isDebugEnabled()) {
                 logger.debug("adding new plugin locale '" + name + "'"); // NOI18N
             }
             if (this.pluginLocales == null) {
                 this.pluginLocales = new HashMap();
             }
 
             final PluginLocale locale = new PluginLocale(name, language, country, resourceFile);
             this.pluginLocales.put(locale.getName(), locale);
         }
 
         /**
          * <deployment> section part 1 =========================================.
          *
          * @param  className  DOCUMENT ME!
          */
         public void setClassName(final String className) {
             if (logger.isDebugEnabled()) {
                 logger.debug("setting plugin class name " + className + "'"); // NOI18N
             }
             this.className = className;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  jar  DOCUMENT ME!
          */
         public void addLibrary(final String jar) {
             if (logger.isDebugEnabled()) {
                 logger.debug("adding library " + jar + "'"); // NOI18N
             }
             libraries.add(jar);
         }
 
         /**
          * Maps meta attribute names to attribute ids.
          *
          * <p>The mapping is defined in the plugin descriptor (plugin.xml).</p>
          *
          * @param  attributeName  the name of the meta attribute, e.g. 'Koordinate'
          * @param  attributeId    the id of the meta attribute, e.g. XYZ123
          */
         public void addAttributeMapping(final String attributeName, final String attributeId) {
             if (logger.isDebugEnabled()) {
                 logger.debug("adding attribute mapping '" + attributeName + "' = '" + attributeId + "'");        // NOI18N
             }
             if (mappingTable.containsKey(attributeName)) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("attribute '" + attributeName + "' already in map, adding id to String array"); // NOI18N
                 }
 
                 // String[] currentIds = (String[])mappingTable.get(attributeName);
                 // String[] newIds = new String[currentIds.length + 1];
 
                 // System.arraycopy(currentIds, 0, newIds, 0, currentIds.length);
                 // newIds[currentIds.length] = attributeId;
 
                 final Set idSet = (Set)mappingTable.get(attributeName);
                 idSet.add(attributeId);
 
                 // logger.fatal("size: " + ((Set)mappingTable.get(attributeName)).size());
 
             } else {
                 // mappingTable.put(attributeName, new String[]{attributeId});
 
                 final HashSet idSet = new HashSet();
                 idSet.add(attributeId);
                 mappingTable.put(attributeName, idSet);
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  paramName   DOCUMENT ME!
          * @param  paramValue  DOCUMENT ME!
          */
         public void addParameter(final String paramName, final String paramValue) {
             if (logger.isDebugEnabled()) {
                 logger.debug("adding parameter " + paramName + "' = '" + paramValue + "'"); // NOI18N
             }
             paramTable.put(paramName, paramValue);
         }
 
         /**
          * create the plugin instance ==========================================.
          *
          * @throws  MalformedURLException      DOCUMENT ME!
          * @throws  ClassNotFoundException     DOCUMENT ME!
          * @throws  NoSuchMethodException      DOCUMENT ME!
          * @throws  InstantiationException     DOCUMENT ME!
          * @throws  IllegalAccessException     DOCUMENT ME!
          * @throws  InvocationTargetException  DOCUMENT ME!
          * @throws  InterruptedException       DOCUMENT ME!
          */
         public void createPluginInstance() throws MalformedURLException,
             ClassNotFoundException,
             NoSuchMethodException,
             InstantiationException,
             IllegalAccessException,
             InvocationTargetException,
             InterruptedException {
             if (logger.isInfoEnabled()) {
                 logger.info("creating new plugin '" + className + "' instance"); // NOI18N
             }
 
             PluginProgressObserver progressObserver = null;
             if (descriptor.isProgressObservable()) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("setting plugin progress observer");       // NOI18N
                 }
                 progressObserver = new PluginProgressObserver(this.descriptor.getName());
                 PropertyManager.getManager().getSharedProgressObserver().setSubProgressObserver(progressObserver);
             } else if (logger.isDebugEnabled()) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("plugin " + descriptor.getName() + "' is not progressobservable ("
                                 + descriptor.isProgressObservable() + ")"); // NOI18N
                 }
             }
 
             final PluginContext context = new PluginContext(
                     this.descriptor,
                     progressObserver,
                     this.paramTable,
                     this.mappingTable,
                     this.pluginLocales,
                     this.defaultLocale);
             descriptor.setContext(context);
             if (descriptor.isInternationalized() && !context.getI18n().isInternationalized()) {
                 logger.warn("internationalization broken, check plugin descriptor &  resource files"); // NOI18N
                 descriptor.setInternationalized(false);
             }
 
             // load jar files
             final URL[] urls = new URL[libraries.size() + 1];
             final String jarBase = resource.pathToIURIString(descriptor.getPluginPath() + "lib/"); // NOI18N
 
             // /res implements classpath ...
             urls[0] = new URL(resource.pathToIURIString(descriptor.getPluginPath() + "res/")); // NOI18N
 
             for (int i = 0; i < libraries.size(); i++) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("loading plugin library: '" + jarBase + libraries.get(i).toString() + "'"); // NOI18N
                 }
                 urls[i + 1] = new URL(jarBase + libraries.get(i).toString());
             }
 
             if (logger.isDebugEnabled()) {
                 logger.debug("the current classloader is '" + this.getClass().getClassLoader().getClass().getName()
                             + "'"); // NOI18N
             }
             // URLClassLoader classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
             Class pluginClass = null;
             if (urls.length > 0) {                                                     // vorher 1 geht aber trotzdem
                                                                                        // weil der URLClassLOader den
                                                                                        // normalen als Fallback hat
                                                                                        // ///HELL
                 final PluginClassLoader classLoader = new PluginClassLoader(urls, this.getClass().getClassLoader());
                 if (logger.isDebugEnabled()) {
                     logger.debug("the current url parent classloader is '"
                                 + classLoader.getParent().getClass().getName() + "'"); // NOI18N
                 }
 
                 final URL[] jarURLs = classLoader.getURLs();
                 for (int i = 0; i < jarURLs.length; i++) {
                     if (logger.isDebugEnabled()) {
                         logger.debug("loadign jar file at '" + jarURLs[i] + "'"); // NOI18N
                     }
                 }
 
                 // create plugin instance
                 if (logger.isInfoEnabled()) {
                     logger.info("creating plugin instance of class '" + className + "'"); // NOI18N
                 }
                 pluginClass = classLoader.loadClass(className);
             } else {
                 pluginClass = this.getClass().getClassLoader().loadClass(className);      // HELL \u00C4nderung quick &
                                                                                           // Dirty
             }
 
             // ...........................................
             final Constructor pluginConstructor = pluginClass.getConstructor(
                     new Class[] { Sirius.navigator.plugin.context.PluginContext.class });
             final Object pluginObject = pluginConstructor.newInstance(new Object[] { descriptor.getContext() });
             // just to be shure:
             if (descriptor.isProgressObservable()) {
                 progressObserver.setFinished(true);
             }
             // ..................................................................
             if (logger.isInfoEnabled()) {
                 logger.info("plugin instance created!"); // NOI18N
             }
             descriptor.setPlugin((PluginSupport)pluginObject);
         }
 
         /**
          * <deployment> section part 2 =========================================.
          *
          * @param  id             DOCUMENT ME!
          * @param  name           DOCUMENT ME!
          * @param  description    DOCUMENT ME!
          * @param  multithreaded  DOCUMENT ME!
          * @param  availability   DOCUMENT ME!
          */
         public void addMethod(final String id,
                 final String name,
                 final String description,
                 final Boolean multithreaded,
                 final Long availability) {
             if (logger.isDebugEnabled()) {
                 logger.debug("addingr new plugin method: id='" + id + "', name='" + name + "'"); // NOI18N
             }
             final Object object = descriptor.getPlugin().getMethod(id);
 
             if ((object != null) && (object instanceof PluginMethod)) {
                 if (methodDescriptors == null) {
                     methodDescriptors = new HashMap();
                     descriptor.setMethodDescriptors(methodDescriptors);
                 }
 
                 final PluginMethodDescriptor methodDescriptor = new PluginMethodDescriptor(
                         id,
                         name,
                         description,
                         multithreaded.booleanValue(),
                         availability.longValue(),
                         (PluginMethod)object);
                 methodDescriptors.put(methodDescriptor.getId(), methodDescriptor);
             } else {
                 logger.error("plugin method '" + id + "' could not be found: '" + object + "'"); // NOI18N
             }
         }
 
         /**
          * public void registerMethods() { if(logger.isDebugEnabled())logger.debug("registering plugin methods");
          * descriptor.setMethodDescriptors(methodDescriptors); } <ui> section part 1
          * =========================================.
          *
          * @param  uiDescriptor  DOCUMENT ME!
          */
         public void addUIDescriptor(final PluginUIDescriptor uiDescriptor) {
             if (logger.isDebugEnabled()) {
                 logger.debug("adding plugin ui descriptor " + uiDescriptor.getName() + "'"); // NOI18N
             }
             if (uiDescriptors == null) {
                 uiDescriptors = new HashMap();
                 descriptor.setUIDescriptors(uiDescriptors);
             }
 
             this.uiDescriptor = uiDescriptor;
             uiDescriptors.put(this.uiDescriptor.getId(), this.uiDescriptor);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         private PluginUI getPluginUI() {
             if (this.uiDescriptor != null) {
                 final PluginUI pluginUI = descriptor.getPlugin().getUI(uiDescriptor.getId());
 
                 if (pluginUI != null) {
                     if (uiDescriptor.getIconName() != null) {
                         uiDescriptor.setIcon(this.getImageIcon(uiDescriptor.getIconName()));
                     }
 
                     return pluginUI;
                 } else {
                     logger.error("plugin ui '" + uiDescriptor.getId() + "' could not be found"); // NOI18N
                     uiDescriptors.remove(uiDescriptor.getId());
                     return null;
                 }
             } else {
                 logger.fatal("synchronization error: plugin ui descriptor generation failed");   // NOI18N
                 return null;
             }
         }
 
         /**
          * DOCUMENT ME!
          */
         public void addAsComponent() {
             final PluginUI pluginUI = this.getPluginUI();
 
             if (pluginUI != null) {
                 uiDescriptor.addAsComponent(pluginUI);
                 uiDescriptor = null;
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  layout  DOCUMENT ME!
          */
         public void addAsPanel(final String layout) {
             final PluginUI pluginUI = this.getPluginUI();
 
             if (pluginUI != null) {
                 uiDescriptor.addAsPanel(pluginUI, layout);
                 uiDescriptor = null;
             }
         }
 
         /**
          * DOCUMENT ME!
          */
         public void addAsScrollPane() {
             final PluginUI pluginUI = this.getPluginUI();
 
             if (pluginUI != null) {
                 uiDescriptor.addAsScrollPane(pluginUI);
                 uiDescriptor = null;
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  configurator  DOCUMENT ME!
          */
         public void addAsFloatingFrame(final PluginFloatingFrameConfigurator configurator) {
             final PluginUI pluginUI = this.getPluginUI();
             configurator.setAdvancedLayout(PropertyManager.getManager().isAdvancedLayout());
 
             if (pluginUI != null) {
                 if (pluginUI instanceof FloatingPluginUI) {
                     uiDescriptor.addAsFloatingFrame((FloatingPluginUI)pluginUI, configurator);
                 } else {
                     logger.error("wrong plugin ui type'" + pluginUI.getClass().getName()
                                 + "',  'Sirius.navigator.plugin.interfaces.FloatingPluginUI' expected"); // NOI18N
                     uiDescriptors.remove(uiDescriptor.getId());
                 }
             }
         }
 
         /**
          * <ui> section part 2 <actions> =======================================.
          *
          * @param  actionDescriptor  DOCUMENT ME!
          */
         public void createPluginToolBar(final PluginActionDescriptor actionDescriptor) {
             if (logger.isDebugEnabled()) {
                 logger.debug("createPluginToolBar, floatable: '" + actionDescriptor.isFloatable() + "'"); // NOI18N
             }
             pluginToolBar = new EmbeddedToolBar(descriptor.getId());
             pluginToolBar.setName(actionDescriptor.getName());
             pluginToolBar.setRollover(PropertyManager.getManager().isAdvancedLayout());
             pluginToolBar.setFloatable(actionDescriptor.isFloatable());
             descriptor.setPluginToolBar(pluginToolBar);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  actionDescriptor  DOCUMENT ME!
          */
         public void addToolBarButton(final PluginActionDescriptor actionDescriptor) {
             if (descriptor.isPluginMethodAvailable(actionDescriptor.getMethodId())) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("creating new plugin toolbar button: '" + actionDescriptor.getName() + "'"); // NOI18N
                 }
                 final PluginToolBarButton button = new PluginToolBarButton(descriptor.getMethodDescriptor(
                             actionDescriptor.getMethodId()).getMethod());
 
                 this.setItemProperties(button, actionDescriptor);
 
                 pluginToolBar.addButton(button);
             } else {
                 logger.error("plugin toolbar button '" + actionDescriptor.getName()
                             + "' refers to an unknown plugin method: '" + actionDescriptor.getMethodId() + "'"); // NOI18N
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  actionDescriptor  DOCUMENT ME!
          */
         public void createPluginMenu(final PluginActionDescriptor actionDescriptor) {
             if (logger.isDebugEnabled()) {
                 logger.debug("createPluginMenu"); // NOI18N
             }
             pluginMenu = new PluginMenu(descriptor.getId(), actionDescriptor.getName());
             // pluginMenu.setText(actionDescriptor.getName());
             pluginMenu.setMnemonic(actionDescriptor.getMnemonic());
             pluginMenu.setIcon(this.getImageIcon(actionDescriptor.getIconName()));
             descriptor.setPluginMenu(pluginMenu);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  actionDescriptor  DOCUMENT ME!
          */
         public void addMenuItem(final PluginActionDescriptor actionDescriptor) {
             if (descriptor.isPluginMethodAvailable(actionDescriptor.getMethodId())) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("creating new plugin menu item: '" + actionDescriptor.getName() + "'"); // NOI18N
                 }
                 final PluginMethodDescriptor methodDescriptor = descriptor.getMethodDescriptor(
                         actionDescriptor.getMethodId());
                 Sirius.server.localserver.method.Method method = null;
                 final String methodKey = actionDescriptor.getMethodId() + '@' + this.descriptor.getId(); // NOI18N
 
                 try {
                     method = SessionManager.getProxy().getMethod(methodKey);
 
                     if (method != null) {
                         if (method.getPermissions().hasPermission(
                                         SessionManager.getSession().getUser().getUserGroup().getKey(),
                                         PermissionHolder.READPERMISSION)) {
                             final PluginMenuItem menuItem = new PluginMenuItem(methodDescriptor.getMethod(), method);
 
                             this.setItemProperties(menuItem, actionDescriptor);
                             final KeyStroke accelerator = actionDescriptor.getAccelerator();
                             if (accelerator != null) {
                                 menuItem.setAccelerator(accelerator);
                             }
 
                             pluginMenu.addItem(menuItem);
                             if (actionDescriptor.isSeparator()) {
                                 pluginMenu.addSeparator();
                             }
                         } else {
                             if (logger.isDebugEnabled()) {
                                 logger.warn("no permission to show method '" + method.getKey() + "'"); // NOI18N
                             }
                         }
                     } else {
                         logger.error("method '" + methodKey + "' is not registered, ignoring method"); // NOI18N
                     }
                 } catch (Throwable t) {
                     logger.warn("could not retrieve method '" + methodKey + "'");                      // NOI18N
                 }
             } else {
                 logger.error("plugin menu item '" + actionDescriptor.getName()
                             + "' refers to an unknown plugin method: '" + actionDescriptor.getMethodId() + "'"); // NOI18N
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  actionDescriptor  DOCUMENT ME!
          */
         public void createPluginPopupMenu(final PluginActionDescriptor actionDescriptor) {
             if (logger.isDebugEnabled()) {
                 logger.debug("createPluginPopupMenu"); // NOI18N
             }
             pluginPopupMenu = new PluginMenu(descriptor.getId(), actionDescriptor.getName());
             // pluginPopupMenu.setText(actionDescriptor.getName());
             pluginPopupMenu.setMnemonic(actionDescriptor.getMnemonic());
             pluginPopupMenu.setIcon(this.getImageIcon(actionDescriptor.getIconName()));
             descriptor.setPluginPopupMenu(pluginPopupMenu);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  actionDescriptor  DOCUMENT ME!
          */
         public void addPopupMenuItem(final PluginActionDescriptor actionDescriptor) {
             if (descriptor.isPluginMethodAvailable(actionDescriptor.getMethodId())) {
                 if (pluginPopupMenu == null) {
                     pluginPopupMenu = new PluginMenu(descriptor.getId(), descriptor.getMetaInfo().getName());
                     descriptor.setPluginPopupMenu(pluginPopupMenu);
                 }
 
                 if (logger.isDebugEnabled()) {
                     logger.debug("creating new plugin menu item: '" + actionDescriptor.getName() + "'"); // NOI18N
                 }
                 // PluginMenuItem menuItem = new
                 // PluginMenuItem(descriptor.getMethodDescriptor(actionDescriptor.getMethodId()).getMethod());
                 final PluginMethodDescriptor methodDescriptor = descriptor.getMethodDescriptor(
                         actionDescriptor.getMethodId());
                 Sirius.server.localserver.method.Method method = null;
                 final String methodKey = actionDescriptor.getMethodId() + '@' + this.descriptor.getId(); // NOI18N
 
                 try {
                     method = SessionManager.getProxy().getMethod(methodKey);
                     if (method != null) {
                         if (method.getPermissions().hasPermission(
                                         SessionManager.getSession().getUser().getUserGroup().getKey(),
                                         PermissionHolder.READPERMISSION)) {
                             final PluginMenuItem menuItem = new PluginMenuItem(methodDescriptor.getMethod(), method);
 
                             this.setItemProperties(menuItem, actionDescriptor);
 
                             final KeyStroke accelerator = actionDescriptor.getAccelerator();
                             if (accelerator != null) {
                                 menuItem.setAccelerator(accelerator);
                             }
 
                             // pluginPopupMenu.setAvailability(actionDescriptor.getAvailability());
 
                             pluginPopupMenu.addItem(menuItem);
                             if (actionDescriptor.isSeparator()) {
                                 pluginPopupMenu.addSeparator();
                             }
                         } else {
                             logger.warn("no permission to show method '" + method.getKey() + "'");    // NOI18N
                         }
                     } else {
                         logger.error("method '" + methodKey + "' is not available, ignoring method"); // NOI18N
                     }
                 } catch (Throwable t) {
                     logger.warn("could not retrieve method '" + methodKey + "'");                     // NOI18N
                 }
             } else {
                 logger.error("plugin menu item '" + actionDescriptor.getName()
                             + "' refers to an unknown plugin method: '" + actionDescriptor.getMethodId() + "'"); // NOI18N
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  item              DOCUMENT ME!
          * @param  actionDescriptor  DOCUMENT ME!
          */
         private void setItemProperties(final AbstractButton item, final PluginActionDescriptor actionDescriptor) {
             item.setText(actionDescriptor.getName());
             item.setMnemonic(actionDescriptor.getMnemonic());
             item.setToolTipText(actionDescriptor.getTooltip());
             item.setIcon(this.getImageIcon(actionDescriptor.getIconName()));
             /*Icon icon = descriptor.getPlugin().getImageIcon(actionDescriptor.getIconId());
              * if(icon != null) { item.setIcon(icon);}*/
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   icon  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         private ImageIcon getImageIcon(final String icon) {
             String iconURL = null;
             if ((icon.indexOf("/") == 0) || (icon.indexOf("\\") == 0))                             // NOI18N
             {
                 iconURL = descriptor.getContext().getEnvironment().getDocumentBase() + icon;
             } else {
                 iconURL = descriptor.getContext().getEnvironment().getDocumentBase() + "/" + icon; // NOI18N
             }
 
             return descriptor.getContext().getResource().getImageIcon(iconURL);
         }
     }
 
     // test --------------------------------------------------------------------
 
     /*public static void main(String args[])
      * { try {     //BufferedInputStream inputStream = new BufferedInputStream(new
      * FileInputStream("D:\\work\\web\\Sirius\\Navigator\\plugins\\plugin.xml"));      String name = "example";
      * PluginFactory factory = PluginFactory.getFactory();     //factory.logger.setLevel(Level.DEBUG); PluginDescriptor
      * descriptor = new PluginDescriptor(new URL("file:///D:/work/web/Sirius/Navigator/plugins"), name);
      * factory.logger.info("preloading plugin '" +  name + "'");     factory.preloadPlugin(descriptor);
      * factory.logger.info("loading plugin '" +  name + "'");     factory.loadPlugin(descriptor);
      * System.out.println(descriptor.getName());     System.out.println(descriptor.getMetaInfo().getDisplayname()); }
      * catch(Exception exp) {     exp.printStackTrace(); }}*/
 }
