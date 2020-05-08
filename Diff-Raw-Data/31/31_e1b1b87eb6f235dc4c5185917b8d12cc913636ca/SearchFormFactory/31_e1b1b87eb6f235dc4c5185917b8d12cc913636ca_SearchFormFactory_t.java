 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * SearchFormFactory.java
  *
  * Created on 12. November 2003, 10:29
  */
 package Sirius.navigator.search.dynamic;
 
 import Sirius.navigator.plugin.*;
 import Sirius.navigator.resource.*;
 
 import org.apache.commons.digester.*;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import org.xml.sax.*;
 
 import java.io.*;
 
 import java.net.*;
 
 import java.util.*;
 
 /**
  * DOCUMENT ME!
  *
  * @author   pascal
  * @version  $Revision$, $Date$
  */
 public class SearchFormFactory {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final ResourceManager resource = ResourceManager.getManager();
 
     //~ Instance fields --------------------------------------------------------
 
     private final Logger logger;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new instance of SearchFormFactory.
      */
     protected SearchFormFactory() {
         this.logger = Logger.getLogger(this.getClass());
 
         final Logger digesterLogger = Logger.getLogger(SearchFormFactory.LoadSearchFormsRuleSet.class);
         digesterLogger.setLevel(Level.WARN);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   searchFormsPath        DOCUMENT ME!
      * @param   searchFormsDescriptor  DOCUMENT ME!
      * @param   searchContext          DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IOException            DOCUMENT ME!
      * @throws  SAXException           DOCUMENT ME!
      * @throws  URISyntaxException     DOCUMENT ME!
      * @throws  FileNotFoundException  DOCUMENT ME!
      */
     protected java.util.List createSearchForms(final String searchFormsPath,
             final String searchFormsDescriptor,
             final SearchContext searchContext) throws IOException,
         SAXException,
         URISyntaxException,
         FileNotFoundException {
         if (logger.isDebugEnabled()) {
             logger.debug("loading search forms descriptor '" + searchFormsDescriptor + "' in '" + searchFormsPath
                         + "'"); // NOI18N
         }
         final InputStream inputStream = this.getXMLDescriptorInputStream(searchFormsPath, searchFormsDescriptor);
 
         final LinkedList searchFormContainerList = new LinkedList();
         final Digester digester = new Digester();
 
         // digester.push(new SearchFormFactory.FactoryCore(searchFormManager));
         digester.push(new SearchFormFactory.FactoryCore(searchFormsPath, searchFormContainerList, searchContext));
         digester.addRuleSet(new LoadSearchFormsRuleSet());
 
         digester.parse(inputStream);
         inputStream.close();
 
         return searchFormContainerList;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   searchUrl              DOCUMENT ME!
      * @param   searchFormsDescriptor  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  URISyntaxException     DOCUMENT ME!
      * @throws  FileNotFoundException  DOCUMENT ME!
      * @throws  IOException            DOCUMENT ME!
      */
     private InputStream getXMLDescriptorInputStream(final URL searchUrl, final String searchFormsDescriptor)
             throws URISyntaxException, FileNotFoundException, IOException {
         if (logger.isDebugEnabled()) {
             logger.debug("loading search forms XML descriptor '" + searchUrl.toString() + "/" + searchFormsDescriptor
                         + "'"); // NOI18N
         }
 
         final File file = new File(new URI(searchUrl.toString() + "/" + searchFormsDescriptor)); // NOI18N
         return new BufferedInputStream(new FileInputStream(file), 4096);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   searchPath             DOCUMENT ME!
      * @param   searchFormsDescriptor  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  FileNotFoundException  DOCUMENT ME!
      * @throws  MalformedURLException  DOCUMENT ME!
      * @throws  IOException            DOCUMENT ME!
      */
     private InputStream getXMLDescriptorInputStream(final String searchPath, final String searchFormsDescriptor)
             throws FileNotFoundException, MalformedURLException, IOException {
         final String searchDescriptorPath = searchPath + "/" + searchFormsDescriptor;                           // NOI18N
         if (logger.isDebugEnabled()) {
             logger.debug("loading search forms XML descriptor from remote URL '" + searchDescriptorPath + "'"); // NOI18N
         }
 
         return new BufferedInputStream(resource.getResourceAsStream(searchDescriptorPath), 8192);
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * RuleSet =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=.
      *
      * @version  $Revision$, $Date$
      */
     private class LoadSearchFormsRuleSet extends RuleSetBase {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void addRuleInstances(final Digester digester) {
             // add libraries (jar files) & create classLoader ..................
             digester.addCallMethod("search/libraries/jar", "addLibrary", 0); // NOI18N
             digester.addCallMethod("search/libraries", "createClassLoader"); // NOI18N
 
             // add form properties  ............................................
             digester.addCallMethod("search/categories/category/forms/form/properties/property", "addProperty", 2); // NOI18N
             digester.addCallParam("search/categories/category/forms/form/properties/property/name", 0);            // NOI18N
             digester.addCallParam("search/categories/category/forms/form/properties/property/value", 1);           // NOI18N
 
             // add the query parameter name <-> form parameter name mapping ....
             digester.addCallMethod(
                 "search/categories/category/forms/form/mappings/queryParameter",
                 "addParameterMapping",
                 2);                                                                                              // NOI18N
             digester.addCallParam("search/categories/category/forms/form/mappings/queryParameter/queryName", 0); // NOI18N
             digester.addCallParam("search/categories/category/forms/form/mappings/queryParameter/formName", 1);  // NOI18N
 
             // form i182 .......................................................
             digester.addCallMethod("search/categories/category/forms/form/resourceBundle", "setFormResourceBundle", 0);
             // form visible ....................................................
             digester.addCallMethod(
                 "search/categories/category/forms/form/visible",
                 "setFormVisible",
                 0,
                 new String[] { "java.lang.Boolean" }); // NOI18N
 
             // create the search form  .........................................
             digester.addCallMethod("search/categories/category/forms/form", "createSearchForm", 5); // NOI18N
             digester.addCallParam("search/categories/category/forms/form/name", 0);                 // NOI18N
             digester.addCallParam("search/categories/category/forms/form/formClass", 1);            // NOI18N
             digester.addCallParam("search/categories/category/forms/form/formDataClass", 2);        // NOI18N
             digester.addCallParam("search/categories/category/forms/form/formId", 3);               // NOI18N
             digester.addCallParam("search/categories/category/forms/form/queryId", 4);              // NOI18N
             // digester.addCallParam("search/categories/category/forms/form/internationalized", 4);
 
             // category i182 ...................................................
             digester.addCallMethod("search/categories/category/resourceBundle", "setCategoryResourceBundle", 0); // NOI18N
             // category visible ................................................
             digester.addCallMethod(
                 "search/categories/category/visible",
                 "setContainerVisible",
                 0,
                 new String[] { "java.lang.Boolean" }); // NOI18N
 
             // create the search form  container ................................
             // digester.addCallMethod("search/categories/category", "createSearchFormContainer", 3, new String[]
             // {"java.lang.String", "java.lang.String", "java.lang.Boolean"});
             digester.addCallMethod("search/categories/category", "createSearchFormContainer", 2); // NOI18N
             digester.addCallParam("search/categories/category/name", 0);                          // NOI18N
             digester.addCallParam("search/categories/category/categoryClass", 1);                 // NOI18N
                                                                                                   // digester.addCallParam("search/categories/category/internationalized",
                                                                                                   // 2);
 
             // add all containers to the manager
             // digester.addCallMethod("search/categories", "initSearchFormManager");
         }
     }
     // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class FactoryCore {
 
         //~ Instance fields ----------------------------------------------------
 
         // private final SearchFormManager searchFormManager;
         private final String searchFormsPath;
         private java.util.List searchFormContainerList;
         private final SearchContext searchContext;
 
         private ClassLoader formClassLoader;
 
         private HashMap formPropertiesMap;
         private LinkedHashMap parameterMappingMap;
         private LinkedList librariesList;
         private LinkedList searchFormsList;
 
         private ResourceBundle categoryResourceBundle;
         private ResourceBundle formResourceBundle;
 
         private boolean formVisible = true;
         private boolean containerVisible = true;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new FactoryCore object.
          *
          * @param  searchFormsPath          DOCUMENT ME!
          * @param  searchFormContainerList  DOCUMENT ME!
          * @param  searchContext            DOCUMENT ME!
          */
         private FactoryCore(final String searchFormsPath,
                 final java.util.List searchFormContainerList,
                 final SearchContext searchContext) {
             // this.searchFormManager = searchFormManager;
             this.searchFormsPath = searchFormsPath;
             this.searchFormContainerList = searchFormContainerList;
 
             this.formPropertiesMap = new HashMap();
             this.parameterMappingMap = new LinkedHashMap();
             this.librariesList = new LinkedList();
             this.searchFormsList = new LinkedList();
             this.searchContext = searchContext;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * .....................................................................
          *
          * @param  jar  DOCUMENT ME!
          */
         public void addLibrary(final String jar) {
             if (logger.isDebugEnabled()) {
                 logger.debug("adding library " + jar + "'"); // NOI18N
             }
             librariesList.add(jar);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @throws  MalformedURLException  DOCUMENT ME!
          */
         public void createClassLoader() throws MalformedURLException {
             if (this.librariesList.size() > 0) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("loading libraries @ '" + this.searchFormsPath + "'"); // NOI18N
                 }
                 // load jar files
                 final URL[] urls = new URL[this.librariesList.size()];
                 // String jarBase =
                 // ResourceManager.getManager().pathToIURIString(PropertyManager.getManager().getSearchFormPath() +
                 // "lib/");
                 final String jarBase = resource.pathToIURIString(this.searchFormsPath + "/lib/"); // NOI18N
                 for (int i = 0; i < this.librariesList.size(); i++) {
                     if (logger.isDebugEnabled()) {
                         logger.debug("loading search form library: '" + jarBase + this.librariesList.get(i).toString()
                                     + "'");                                                       // NOI18N
                     }
                     urls[i] = new URL(jarBase + this.librariesList.get(i).toString());
                 }
 
                 this.formClassLoader = new PluginClassLoader(urls, this.getClass().getClassLoader());
             } else {
                 logger.warn("no form libraries loaded, using default classloader"); // NOI18N
                 this.formClassLoader = this.getClass().getClassLoader();
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  name   DOCUMENT ME!
          * @param  value  DOCUMENT ME!
          */
         public void addProperty(final String name, final String value) {
             if (logger.isDebugEnabled()) {
                 logger.debug("adding property: '" + name + "' = '" + value + "'"); // NOI18N
             }
             this.formPropertiesMap.put(name, value);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  queryName  DOCUMENT ME!
          * @param  formName   DOCUMENT ME!
          */
         public void addParameterMapping(final String queryName, final String formName) {
             if (logger.isDebugEnabled()) {
                 logger.debug("adding parameter mapping: '" + queryName + "' == '" + formName + "'"); // NOI18N
             }
             this.parameterMappingMap.put(queryName, formName);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  resourceBundleName  DOCUMENT ME!
          */
         public void setFormResourceBundle(final String resourceBundleName) {
             this.formResourceBundle = this.getResourceBundle(resourceBundleName);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  formVisible  DOCUMENT ME!
          */
         public void setFormVisible(final Boolean formVisible) {
             this.formVisible = formVisible.booleanValue();
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  containerVisible  DOCUMENT ME!
          */
         public void setContainerVisible(final Boolean containerVisible) {
             this.containerVisible = containerVisible.booleanValue();
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   name           DOCUMENT ME!
          * @param   formClass      DOCUMENT ME!
          * @param   formDataClass  DOCUMENT ME!
          * @param   formId         DOCUMENT ME!
          * @param   queryId        DOCUMENT ME!
          *
          * @throws  ClassNotFoundException  DOCUMENT ME!
          * @throws  InstantiationException  DOCUMENT ME!
          * @throws  IllegalAccessException  DOCUMENT ME!
          */
         public void createSearchForm(final String name,
                 final String formClass,
                 final String formDataClass,
                 final String formId,
                 final String queryId) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
             logger.info(name + ", " + formClass + ", " + formDataClass + formId + ", " + ", " + queryId); // + ", " + internationalized);//NOI18N
             if (logger.isDebugEnabled()) {
                 logger.debug("creating data bean instance of class '" + formDataClass + "'");             // NOI18N
             }
             final Class dataBeanClass = this.formClassLoader.loadClass(formDataClass);
 
             final FormDataBean dataBean = (FormDataBean)dataBeanClass.newInstance();
             dataBean.setFormId(formId);
             dataBean.setQueryId(queryId);
             dataBean.setParameterNamesMap(this.parameterMappingMap);
 
             // clear parameter map:
             this.parameterMappingMap.clear();
 
             if (logger.isDebugEnabled()) {
                 logger.debug("creating form instance of class '" + formClass + "'"); // NOI18N
             }
             final Class searchFormClass = this.formClassLoader.loadClass(formClass);
 
             final SearchForm searchForm = (SearchForm)searchFormClass.newInstance();
             searchForm.setName(name);
             searchForm.setFormId(formId);
             searchForm.setQueryId(queryId);
             searchForm.setDataBean(dataBean);
             if (this.formResourceBundle != null) {
                 // searchForm.setLocale(ResourceManager.getManager().getLocale());
                 searchForm.setResourceBundle(this.formResourceBundle);
 
                 // clear reference
                 this.formResourceBundle = null;
             } else if (logger.isDebugEnabled()) {
                 logger.warn("form '" + name + "' does not support i182"); // NOI18N
             }
             if (this.formPropertiesMap.size() > 0) {
                 searchForm.setFormProperties(formPropertiesMap);
                 // clear properties map
                 formPropertiesMap.clear();
             }
 
             searchForm.setVisible(this.formVisible);
             this.formVisible = true;
 
             searchForm.setSearchContext(this.searchContext);
 
             if (logger.isDebugEnabled()) {
                 logger.debug("new search form '" + searchForm.getName() + "' created"); // NOI18N
             }
             this.searchFormsList.add(searchForm);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  resourceBundleName  DOCUMENT ME!
          */
         public void setCategoryResourceBundle(final String resourceBundleName) {
             this.categoryResourceBundle = this.getResourceBundle(resourceBundleName);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   name           DOCUMENT ME!
          * @param   categoryClass  DOCUMENT ME!
          *
          * @throws  ClassNotFoundException  DOCUMENT ME!
          * @throws  InstantiationException  DOCUMENT ME!
          * @throws  IllegalAccessException  DOCUMENT ME!
          */
         public void createSearchFormContainer(final String name, final String categoryClass)
                 throws ClassNotFoundException, InstantiationException, IllegalAccessException {
             if (logger.isDebugEnabled()) {
                 logger.debug("creating form container instance of class '" + categoryClass + "'"); // NOI18N
             }
             final Class searchFormContainerClass = this.formClassLoader.loadClass(categoryClass);
 
             final SearchFormContainer searchFormContainer = (SearchFormContainer)searchFormContainerClass.newInstance();
             searchFormContainer.setName(name);
             if (this.categoryResourceBundle != null) {
                 // searchForm.setLocale(ResourceManager.getManager().getLocale());
                 searchFormContainer.setResourceBundle(this.categoryResourceBundle);
 
                 // clear reference
                 this.categoryResourceBundle = null;
             } else if (logger.isDebugEnabled()) {
                 logger.warn("form container '" + name + "' does not support i182"); // NOI18N
             }
 
             searchFormContainer.setVisible(this.containerVisible);
             this.containerVisible = true;
 
             try {
                 // logger.debug("initalizing searchFormContainer '" + searchFormContainer.getName() + "'");
                 searchFormContainer.intFormContainer();
             } catch (FormInitializationException fiexp) {
                 // TODO show message dialog
                 logger.fatal(fiexp.getMessage(), fiexp);
             }
 
             searchFormContainer.setSearchForms(this.searchFormsList);
 
             this.searchFormsList.clear();
 
             if (logger.isDebugEnabled()) {
                 logger.debug("new search form container '" + searchFormContainer.getName() + "' created"); // NOI18N
             }
             this.searchFormContainerList.add(searchFormContainer);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   resourceBundleName  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         private ResourceBundle getResourceBundle(final String resourceBundleName) {
             final Locale locale = Locale.getDefault();
             if (logger.isDebugEnabled()) {
                 logger.debug("loading resource bundle '" + resourceBundleName + "' for locale '" + locale + "'"); // NOI18N
             }
 
             try {
                 return ResourceBundle.getBundle(resourceBundleName, locale, this.formClassLoader);
             } catch (Throwable t) {
                 logger.error("could not load resource bundle '" + resourceBundleName + "' for locale '" + locale + "'",
                     t); // NOI18N
                 return null;
             }
         }
     }
 }
