 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.form.data;
 
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.config.ControllerConfig;
 import org.apache.struts.upload.CommonsMultipartRequestHandler;
 import org.apache.struts.upload.FormFile;
 import org.apache.struts.upload.MultipartRequestHandler;
 import org.geotools.coverage.grid.io.AbstractGridFormat;
 import org.opengis.coverage.grid.Format;
 import org.opengis.parameter.ParameterValue;
 import org.opengis.parameter.ParameterValueGroup;
 import org.vfny.geoserver.config.CoverageStoreConfig;
 import org.vfny.geoserver.config.DataConfig;
 import org.vfny.geoserver.global.GeoserverDataDirectory;
 import org.vfny.geoserver.global.UserContainer;
 import org.vfny.geoserver.util.CoverageStoreUtils;
 import org.vfny.geoserver.util.CoverageUtils;
 import org.vfny.geoserver.util.Requests;
 import java.io.File;
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
  * Represents the information required for editing a DataFormat.
  *
  * <p>
  * The parameters required by a DataFormat are dynamically generated from the
  * DataFormatFactorySPI. Most use of DataFormatFactorySPI has been hidden behind
  * the DataStoreUtil class.
  * </p>
  *
  * @author Richard Gould, Refractions Research
  * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
  *         modification)
  * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
  *         modification)
  */
 public final class CoverageStoresEditorForm extends ActionForm {
     /**
      *
      */
     private static final long serialVersionUID = 8469919940722502675L;
 
     /**
      * Help text for Params if available
      */
     private ArrayList paramHelp;
 
     /**
      * Used to identify the Format being edited. Maybe we should grab this from
      * session?
      */
     private String dataFormatId;
 
     /**
      * Enabled status of Format
      */
     private boolean enabled;
 
     /**
      *
      */
 
     /* NamespaceID used for DataStore content */
     private String namespaceId;
 
     /**
      *
      */
     private String type;
 
     /**
      *
      */
     private String url;
 
     /**
      *
      */
     private FormFile urlFile = null;
 
     /**
      *
      */
 
     /* Description of Format (abstract?) */
     private String description;
 
     // These are not stored in a single map so we can access them
     // easily from JSP page
     //
 
     /**
      * String representation of connection parameter keys
      */
     private List paramKeys;
 
     /**
      * String representation of connection parameter values
      */
     private List paramValues;
 
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
 
         // //
         //
         //
         //
         // //
         enabledChecked = false;
 
         ServletContext context = getServlet().getServletContext();
         DataConfig config = (DataConfig) context.getAttribute(DataConfig.CONFIG_KEY);
 
         namespaces = new TreeSet(config.getNameSpaces().keySet());
 
         // //
         //
         //
         //
         // //
         CoverageStoreConfig dfConfig = Requests.getUserContainer(request).getDataFormatConfig();
 
         if (dfConfig == null) {
             // something is horribly wrong no FormatID selected!
             // The JSP needs to not include us if there is no
             // selected Format
             //
             throw new RuntimeException("selectedDataFormatId required in Session");
         }
 
         // //
         //
         //
         //
         // //
         dataFormatId = dfConfig.getId();
         description = dfConfig.getAbstract();
         enabled = dfConfig.isEnabled();
         namespaceId = dfConfig.getNameSpaceId();
 
         if ("".equals(namespaceId) && (config.getDefaultNameSpace() != null)) {
             namespaceId = config.getDefaultNameSpace().getPrefix();
         }
 
         url = dfConfig.getUrl();
 
         // //
         //
         //
         //
         // //
         Format factory = dfConfig.getFactory();
         type = (((dfConfig.getType() != null) && (dfConfig.getType().length() > 0))
             ? dfConfig.getType() : factory.getName());
     }
 
     public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
         ActionErrors errors = new ActionErrors();
 
         // Selected CoverageStoreConfig is in session
         //
         UserContainer user = Requests.getUserContainer(request);
         CoverageStoreConfig dfConfig = user.getDataFormatConfig();
 
         //
         // dsConfig is the only way to get a factory
         Format factory = dfConfig.getFactory();
         ParameterValueGroup info = factory.getReadParameters();
 
         Map connectionParams = new HashMap();
 
         // Convert Params into the kind of Map we actually need
         //
         if (paramKeys != null) {
             final int length = paramKeys.size();
             String key;
             ParameterValue param;
             Boolean maxSize;
             String size;
             ControllerConfig cc;
             Object value;
             final String readGeometryKey = AbstractGridFormat.READ_GRIDGEOMETRY2D.getName()
                                                                                  .toString();
 
             for (int i = 0; i < length; i++) {
                 key = (String) getParamKey(i);
 
                 // //
                 //
                 // Ignore the parameters used for decimation at run time
                 //
                 // //
                 if (key.equalsIgnoreCase(readGeometryKey)) {
                     continue;
                 }
 
                 param = CoverageStoreUtils.find(info, key);
 
                 if (param == null) {
                     errors.add("paramValue[" + i + "]",
                         new ActionError("error.dataFormatEditor.param.missing", key,
                             factory.getDescription()));
 
                     continue;
                 }
 
                 maxSize = (Boolean) request.getAttribute(MultipartRequestHandler.ATTRIBUTE_MAX_LENGTH_EXCEEDED);
 
                 if ((maxSize != null) && (maxSize.booleanValue())) {
                     size = null;
                     cc = mapping.getModuleConfig().getControllerConfig();
 
                     if (cc == null) {
                         size = Long.toString(CommonsMultipartRequestHandler.DEFAULT_SIZE_MAX);
                     } else {
                         size = cc.getMaxFileSize(); // struts-config :
                                                     // <controller
                                                     // maxFileSize="nK" />
                     }
 
                     errors.add("styleID", new ActionError("error.file.maxLengthExceeded", size));
 
                     return errors;
                 }
 
                 value = CoverageUtils.getCvParamValue(key, param, paramValues, i);
 
                 if (value != null) {
                     connectionParams.put(key, value);
                 }
             }
         }
 
         //do a check to make sure the format accepts the url and report back 
         // an error if it does not
         if (factory instanceof AbstractGridFormat) {
             AbstractGridFormat aFormat = (AbstractGridFormat) factory;
 
            File file;
 
            // HACK!  ArcSDE rasters take a string (which is stuffed into the given file)
            if (-1 == factory.getClass().toString()
                                 .indexOf("org.geotools.arcsde.gce.ArcSDERasterFormat")) {
                file = GeoserverDataDirectory.findDataFile(url);
                FormUtils.checkFileExistsAndCanRead(file, errors);

                if (!errors.isEmpty()) {
                    return errors;
                }
            } else {
                file = new File(url);
             }
 
             if (!aFormat.accepts(file)) {
                 String key = "error.coverage.invalidUrlForFormat";
                 Object[] params = new Object[] { url, type };
 
                 errors.add("URL", new ActionMessage(key, params));
 
                 return errors;
             }
         }
 
         dump("form", connectionParams);
 
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
 
         if (paramKeys != null) {
             final int size = paramKeys.size();
 
             for (int i = 0; i < size; i++) {
                 map.put(paramKeys.get(i), paramValues.get(i));
             }
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
      * @param index
      *            DOCUMENT ME!
      *
      * @return
      */
     public String getParamKey(int index) {
         return (String) paramKeys.get(index);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param index
      *            DOCUMENT ME!
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
      * @param value
      *            DOCUMENT ME!
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
     public String getDataFormatId() {
         return dataFormatId;
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
      * @param b
      *            DOCUMENT ME!
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
      * @param index
      *            DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getParamHelp(int index) {
         return (String) paramHelp.get(index);
     }
 
     /**
      * @return Returns the type.
      */
     public String getType() {
         return type;
     }
 
     /**
      * @param type
      *            The type to set.
      */
     public void setType(String type) {
         this.type = type;
     }
 
     /**
      * @return Returns the url.
      */
     public String getUrl() {
         return url;
     }
 
     /**
      * @param url
      *            The url to set.
      */
     public void setUrl(String url) {
         this.url = url;
     }
 
     /**
      *
      */
     public FormFile getUrlFile() {
         return this.urlFile;
     }
 
     /**
      *
      */
     public void setUrlFile(FormFile filename) {
         this.urlFile = filename;
     }
 
     public String getNamespaceId() {
         return namespaceId;
     }
 
     public void setNamespaceId(String namespaceId) {
         this.namespaceId = namespaceId;
     }
 
     public SortedSet getNamespaces() {
         return namespaces;
     }
 }
