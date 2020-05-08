 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.form.data;
 
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionMapping;
 import org.geotools.data.DataStoreFactorySpi;
 import org.geotools.data.DataStoreFactorySpi.Param;
 import org.vfny.geoserver.config.DataConfig;
 import org.vfny.geoserver.config.DataStoreConfig;
 import org.vfny.geoserver.global.GeoserverDataDirectory;
 import org.vfny.geoserver.global.UserContainer;
 import org.vfny.geoserver.util.DataStoreUtils;
 import org.vfny.geoserver.util.Requests;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 
 /**
  * Represents the information required for editing a DataStore.
  *
  * <p>
  * The parameters required by a DataStore are dynamically generated from the
  * DataStoreFactorySPI. Most use of DataStoreFactorySPI has been hidden behind
  * the DataStoreUtil class.
  * </p>
  *
  * @author Richard Gould, Refractions Research
  */
 public class DataDataStoresEditorForm extends ActionForm {
     /** Help text for Params if available */
     private ArrayList paramHelp;
 
     /**
      * Used to identify the DataStore being edited. Maybe we should grab this
      * from session?
      */
     private String dataStoreId;
 
     /** Enabled status of DataStore */
     private boolean enabled;
 
     /* NamespaceID used for DataStore content */
     private String namespaceId;
 
     /* Description of DataStore (abstract?) */
     private String description;
 
     // These are not stored in a single map so we can access them
     // easily from JSP page
     //
 
     /** String representation of connection parameter keys */
     private List paramKeys;
 
     /** String representation of connection parameter values */
     private List paramValues;
 
     /** String representation of connection paramter types */
     private List paramTypes;
 
     /** String representation of paramters which are required */
     private List paramRequired;
 
     //
     // More hacky attempts to transfer information into the JSP smoothly
     //
 
     /** Available NamespaceIds */
     private SortedSet namespaces;
 
     /**
      * Because of the way that STRUTS works, if the user does not check the
      * enabled box, or unchecks it, setEnabled() is never called, thus we must
      * monitor setEnabled() to see if it doesn't get called. This must be
      * accessible, as ActionForms need to know about it -- there is no way we
      * can tell whether we are about to be passed to an ActionForm or not.
      * Probably a better way to do this, but I can't think of one. -rgould
      */
     private boolean enabledChecked = false;
 
     public void reset(ActionMapping mapping, HttpServletRequest request) {
         super.reset(mapping, request);
 
         enabledChecked = false;
 
         ServletContext context = getServlet().getServletContext();
         DataConfig config = (DataConfig) context.getAttribute(DataConfig.CONFIG_KEY);
 
         namespaces = new TreeSet(config.getNameSpaces().keySet());
 
         DataStoreConfig dsConfig = Requests.getUserContainer(request).getDataStoreConfig();
 
         if (dsConfig == null) {
             // something is horribly wrong no DataStoreID selected!
             // The JSP needs to not include us if there is no
             // selected DataStore
             //
             throw new RuntimeException("selectedDataStoreId required in Session");
         }
 
         dataStoreId = dsConfig.getId();
         description = dsConfig.getAbstract();
         enabled = dsConfig.isEnabled();
         namespaceId = dsConfig.getNameSpaceId();
 
         if (namespaceId.equals("")) {
             namespaceId = config.getDefaultNameSpace().getPrefix();
         }
 
         //Retrieve connection params
         DataStoreFactorySpi factory = dsConfig.getFactory();
         Param[] params = factory.getParametersInfo();
 
         paramKeys = new ArrayList(params.length);
         paramValues = new ArrayList(params.length);
         paramTypes = new ArrayList(params.length);
         paramHelp = new ArrayList(params.length);
         paramRequired = new ArrayList(params.length);
 
         for (int i = 0; i < params.length; i++) {
             Param param = params[i];
             String key = param.key;
 
             if ("namespace".equals(key)) {
                 // skip namespace as it is *magic* and
                 // appears to be an entry used in all datastores?
                 //
                 continue;
             }
 
             Object value = dsConfig.getConnectionParams().get(key);
             String text;
 
             if (value == null) {
                 text = null;
             } else if (value instanceof String) {
                 text = (String) value;
             } else {
                 text = param.text(value);
             }
 
             paramKeys.add(key);
             paramValues.add((text != null) ? text : "");
             paramTypes.add(param.type.getName());
             paramHelp.add(param.description + (param.required ? "" : " (optional)"));
             paramRequired.add(Boolean.valueOf(param.required).toString());
         }
     }
 
     public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
         ActionErrors errors = new ActionErrors();
 
         // Selected DataStoreConfig is in session
         //
         UserContainer user = Requests.getUserContainer(request);
         DataStoreConfig dsConfig = user.getDataStoreConfig();
 
         //
         // dsConfig is the only way to get a factory
         DataStoreFactorySpi factory = dsConfig.getFactory();
         Param[] info = factory.getParametersInfo();
 
         Map connectionParams = new HashMap();
 
         // Convert Params into the kind of Map we actually need
         //
         for (int i = 0; i < paramKeys.size(); i++) {
             String key = (String) getParamKey(i);
 
             Param param = DataStoreUtils.find(info, key);
 
             if (param == null) {
                 errors.add("paramValue[" + i + "]",
                     new ActionError("error.dataStoreEditor.param.missing", key,
                         factory.getDescription()));
 
                 continue;
             }
 
             //special case check for url
             if (URL.class.equals(param.type)) {
                 String value = getParamValue(i);
 
                 if ((value != null) && !"".equals(value)) {
                     URL url = null;
 
                     try {
                         // if this does not throw an exception then cool
                         url = new URL(value);
                     } catch (MalformedURLException e) {
                         //check for special case of file
                         try {
                             if (GeoserverDataDirectory.findDataFile(value).exists()) {
                                 new URL("file://" + value);
                                 setParamValues(i, "file://" + value);
                             }
                         } catch (MalformedURLException e1) {
                             //let this paramter die later
                         }
                     }
                    
                    if(url.getProtocol() == null || url.getProtocol() == "file") {
	                    //do a check to see if the shapefile url is valid, report 
	                    // an error if it does not 
	                    File file = GeoserverDataDirectory.findDataFile(value);
	                    FormUtils.checkFileExistsAndCanRead(file, errors);
                    } 
                 }
             }
 
             Object value;
 
             try {
                 value = param.lookUp(getParams());
 
                 if (value instanceof String) {
                     value = param.parse((String) value);
                 }
             } catch (IOException erp) {
                 errors.add("paramValue[" + i + "]",
                     new ActionError("error.dataStoreEditor.param.parse", key, param.type, erp));
 
                 continue;
             } catch (Throwable t) { //thrown by param.parse()
                 errors.add("paramValue[" + i + "]",
                     new ActionError("error.dataStoreEditor.param.parse", key, param.type, t));
 
                 continue;
             }
 
             if ((value == null) && param.required) {
                 errors.add("paramValue[" + i + "]",
                     new ActionError("error.dataStoreEditor.param.required", key));
 
                 continue;
             }
 
             if (value != null) {
                 connectionParams.put(key, value);
             }
         }
 
         // put magic namespace into the mix
         //
         //connectionParams.put("namespace", getNamespaceId());
         dump("form", connectionParams);
 
         // Factory will provide even more stringent checking
         //
         if (!factory.canProcess(connectionParams)) {
             errors.add("paramValue", new ActionError("error.datastoreEditor.validation"));
         }
 
         return errors;
     }
 
     /** Used to debug connection parameters */
     public void dump(String msg, Map params) {
         if (msg != null) {
             System.out.print(msg + " ");
         }
 
         System.out.print(" connection params { ");
 
         for (Iterator i = params.entrySet().iterator(); i.hasNext();) {
             Map.Entry entry = (Map.Entry) i.next();
             System.out.print(entry.getKey());
             System.out.print("=");
 
             if (entry.getValue() == null) {
                 System.out.print("null");
             } else if (entry.getValue() instanceof String) {
                 System.out.print("\"");
                 System.out.print(entry.getValue());
                 System.out.print("\"");
             } else {
                 System.out.print(entry.getValue());
             }
 
             if (i.hasNext()) {
                 System.out.print(", ");
             }
         }
 
         System.out.println("}");
     }
 
     public Map getParams() {
         Map map = new HashMap();
 
         for (int i = 0; i < paramKeys.size(); i++) {
             map.put(paramKeys.get(i), paramValues.get(i));
         }
 
         return map;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return
      */
     public List getParamKeys() {
         return paramKeys;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param index DOCUMENT ME!
      *
      * @return
      */
     public String getParamKey(int index) {
         return (String) paramKeys.get(index);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param index DOCUMENT ME!
      *
      * @return
      */
     public String getParamValue(int index) {
         return (String) paramValues.get(index);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param index
      * @param value DOCUMENT ME!
      */
     public void setParamValues(int index, String value) {
         paramValues.set(index, value);
     }
 
     /**
      * getDataStoreId purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getDataStoreId() {
         return dataStoreId;
     }
 
     /**
      * getDescription purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * isEnabled purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public boolean isEnabled() {
         return enabled;
     }
 
     /**
      * getNamespaces purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public SortedSet getNamespaces() {
         return namespaces;
     }
 
     /**
      * getParamValues purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public List getParamValues() {
         return paramValues;
     }
 
     /**
      * setDescription purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setDescription(String string) {
         description = string;
     }
 
     /**
      * setEnabled purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param b
      */
     public void setEnabled(boolean b) {
         setEnabledChecked(true);
         enabled = b;
     }
 
     /**
      * setParamKeys purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param list
      */
     public void setParamKeys(List list) {
         paramKeys = list;
     }
 
     /**
      * setParamValues purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param list
      */
     public void setParamValues(List list) {
         paramValues = list;
     }
 
     /**
      * getNamespaceId purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getNamespaceId() {
         return namespaceId;
     }
 
     /**
      * setNamespaceId purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setNamespaceId(String string) {
         namespaceId = string;
     }
 
     /**
      * enabledChecked property
      *
      * @return DOCUMENT ME!
      */
     public boolean isEnabledChecked() {
         return enabledChecked;
     }
 
     /**
      * enabledChecked property
      *
      * @param b DOCUMENT ME!
      */
     public void setEnabledChecked(boolean b) {
         enabledChecked = b;
     }
 
     /**
      * Index property paramHelp
      *
      * @return DOCUMENT ME!
      */
     public String[] getParamHelp() {
         return (String[]) paramHelp.toArray(new String[paramHelp.size()]);
     }
 
     /**
      * Index property paramHelp
      *
      * @param index DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getParamHelp(int index) {
         return (String) paramHelp.get(index);
     }
 
     /**
      * @return list containing the name of the class of each paramter.
      */
     public List getParamTypes() {
         return paramTypes;
     }
 
     /**
      * @param index paramter index.
      *
      * @return The string represention of the class of the paramter at the
      * specified index.
      */
     public String getParamType(int index) {
         return (String) paramTypes.get(index);
     }
 
     /**
      * @return list containing java.lang.Boolean values representing which
      * paramters are required.
      */
     public List getParamRequired() {
         return paramRequired;
     }
 
     /**
      * @param index paramter index.
      * @return True if the paramter is required, otherwise false.
      */
     public String getParamRequired(int index) {
         return (String) paramRequired.get(index);
     }
 }
