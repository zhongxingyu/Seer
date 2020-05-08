 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  *
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.event.impl;
 
 import org.apache.commons.fileupload.DiskFileUpload;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUpload;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.provider.portletui.beans.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.*;
 
 /*
  * The <code>FormEventImpl</code> provides methods for creating/retrieving visual beans
  * from the <code>PortletRequest</code>
  */
 
 public abstract class BaseFormEventImpl {
 
     protected transient static PortletLog log = SportletLog.getInstance(BaseFormEventImpl.class);
 
     protected HttpServletRequest request;
     protected HttpServletResponse response;
     protected Map tagBeans = null;
     protected FileItem savedFileItem = null;
 
     public BaseFormEventImpl(HttpServletRequest request, HttpServletResponse response) {
         this.request = request;
         this.response = response;
     }
 
     /**
      * Returns the collection of visual tag beans contained by this form event
      *
      * @return the collection of visual tag beans
      */
     public Map getTagBeans() {
         return tagBeans;
     }
 
     /**
      * Return an existing <code>ActionLinkBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a ActionLinkBean
      */
     public ActionLinkBean getActionLinkBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (ActionLinkBean) tagBeans.get(beanKey);
         }
         ActionLinkBean al = new ActionLinkBean(request, beanId);
         tagBeans.put(beanKey, al);
         return al;
     }
 
     /**
      * Return an existing <code>ActionParamBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a ActionParamBean
      */
     public ActionParamBean getActionParamBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (ActionParamBean) tagBeans.get(beanKey);
         }
         ActionParamBean ap = new ActionParamBean(request, beanId);
         tagBeans.put(beanKey, ap);
         return ap;
     }
 
     /**
      * Return an existing <code>ActionSubmitBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a ActionSubmitBean
      */
     public ActionSubmitBean getActionSubmitBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (ActionSubmitBean) tagBeans.get(beanKey);
         }
         ActionSubmitBean as = new ActionSubmitBean(request, beanId);
         tagBeans.put(beanKey, as);
         return as;
     }
 
     /**
      * Return an existing <code>CheckBoxBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a CheckBoxBean
      */
     public CheckBoxBean getCheckBoxBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (CheckBoxBean) tagBeans.get(beanKey);
         }
         CheckBoxBean cb = new CheckBoxBean(request, beanId);
         tagBeans.put(beanKey, cb);
         return cb;
     }
 
     /**
      * Return an existing <code>RadioButtonBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a RadioButtonBean
      */
     public RadioButtonBean getRadioButtonBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (RadioButtonBean) tagBeans.get(beanKey);
         }
         RadioButtonBean rb = new RadioButtonBean(request, beanId);
         tagBeans.put(beanKey, rb);
         return rb;
     }
 
     /**
      * Return an existing <code>PanelBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a PanelBean
      */
     public PanelBean getPanelBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (PanelBean) tagBeans.get(beanKey);
         }
         PanelBean pb = new PanelBean(request, beanId);
         tagBeans.put(beanKey, pb);
         return pb;
     }
 
     /**
      * Return an existing <code>TextFieldBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a TextFieldBean
      */
     public TextFieldBean getTextFieldBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (TextFieldBean) tagBeans.get(beanKey);
         }
         TextFieldBean tf = new TextFieldBean(request, beanId);
         tagBeans.put(beanKey, tf);
         return tf;
     }
 
     /**
      * Return an existing <code>HiddenFieldBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a HiddenFieldBean
      */
     public HiddenFieldBean getHiddenFieldBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (HiddenFieldBean) tagBeans.get(beanKey);
         }
         HiddenFieldBean hf = new HiddenFieldBean(request, beanId);
         tagBeans.put(beanKey, hf);
         return hf;
     }
 
     /**
      * Return an existing <code>FileInputBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a FileInputBean
      */
     public FileInputBean getFileInputBean(String beanId) throws IOException {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (FileInputBean) tagBeans.get(beanKey);
         }
         FileInputBean fi = new FileInputBean(request, beanId);
         tagBeans.put(beanKey, fi);
         return fi;
     }
 
     /**
      * Return an existing <code>PasswordBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a PasswordBean
      */
     public PasswordBean getPasswordBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (PasswordBean) tagBeans.get(beanKey);
         }
         PasswordBean pb = new PasswordBean(request, beanId);
         tagBeans.put(beanKey, pb);
         return pb;
     }
 
     /**
      * Return an existing <code>TextAreaBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a TextAreaBean
      */
     public TextAreaBean getTextAreaBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (TextAreaBean) tagBeans.get(beanKey);
         }
         TextAreaBean ta = new TextAreaBean(request, beanId);
         tagBeans.put(beanKey, ta);
         return ta;
     }
 
     /**
      * Return an existing <code>FrameBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a FrameBean
      */
     public FrameBean getFrameBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (FrameBean) tagBeans.get(beanKey);
         }
         FrameBean fb = new FrameBean(request, beanId);
         //System.err.println("Creating new frame bean" + beanId + " bean key= " + beanKey);
         tagBeans.put(beanKey, fb);
         return fb;
     }
 
     /**
      * Return an existing <code>TextBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a TextBean
      */
     public TextBean getTextBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (TextBean) tagBeans.get(beanKey);
         }
         TextBean tb = new TextBean(request, beanId);
         tagBeans.put(beanKey, tb);
         return tb;
     }
 
     /**
      * Return an existing <code>ImageBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a ImageBean
      */
     public ImageBean getImageBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (ImageBean) tagBeans.get(beanKey);
         }
         ImageBean ib = new ImageBean(request, beanId);
         tagBeans.put(beanKey, ib);
         return ib;
 
     }
 
     /**
      * Return an existing <code>IncludeBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a IncludeBean
      */
     public IncludeBean getIncludeBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (IncludeBean) tagBeans.get(beanKey);
         }
        IncludeBean includeBean = new IncludeBean(request, beanId);
         tagBeans.put(beanKey, includeBean);
         return includeBean;
     }
 
     /**
      * Return an existing <code>TableBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a TableBean
      */
     public TableBean getTableBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (TableBean) tagBeans.get(beanKey);
         }
         TableBean tb = new TableBean(request, beanId);
         tagBeans.put(beanKey, tb);
         return tb;
     }
 
     /**
      * Return an existing <code>TableRowBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a TableRowBean
      */
     public TableRowBean getTableRowBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (TableRowBean) tagBeans.get(beanKey);
         }
         TableRowBean tr = new TableRowBean(request, beanId);
         tagBeans.put(beanKey, tr);
         return tr;
     }
 
     /**
      * Return an existing <code>TableCellBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a TableCellBean
      */
     public TableCellBean getTableCellBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (TableCellBean) tagBeans.get(beanKey);
         }
         TableCellBean tc = new TableCellBean(request, beanId);
         tagBeans.put(beanKey, tc);
         return tc;
     }
 
     /**
      * Return an existing <code>ListBoxBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a ListBoxBean
      */
     public ListBoxBean getListBoxBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (ListBoxBean) tagBeans.get(beanKey);
         }
         ListBoxBean lb = new ListBoxBean(request, beanId);
         tagBeans.put(beanKey, lb);
         return lb;
     }
 
     /**
      * Return an existing <code>ListBoxItemBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a ListBoxItemBean
      */
     public ListBoxItemBean getListBoxItemBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (ListBoxItemBean) tagBeans.get(beanKey);
         }
         ListBoxItemBean lb = new ListBoxItemBean(request, beanId);
         tagBeans.put(beanKey, lb);
         return lb;
     }
 
     /**
      * Return an existing <code>MessageBoxBean</code> or create a new one
      *
      * @param beanId the bean identifier
      * @return a IncludeBean
      */
     public MessageBoxBean getMessageBoxBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (MessageBoxBean)tagBeans.get(beanKey);
         }
         MessageBoxBean messageBoxBean = new MessageBoxBean(request, beanId);
         tagBeans.put(beanKey, messageBoxBean);
         return messageBoxBean;
     }
 
     /**
      * Prints the request parameters to stdout. Generally used for debugging
      */
     public void printRequestParameters() {
         System.out.println("\n\n show request params\n--------------------\n");
         Enumeration enum = request.getParameterNames();
         while (enum.hasMoreElements()) {
             String name = (String) enum.nextElement();
             System.out.println("name :" + name);
             String values[] = request.getParameterValues(name);
             if (values.length == 1) {
                 String pval = values[0];
                 if (pval.length() == 0) {
                     pval = "no value";
                 }
                 System.out.println(" value : " + pval);
             } else {
                 System.out.println(" value :");
                 for (int i = 0; i < values.length; i++) {
                     System.out.println("            - " + values[i]);
                 }
             }
         }
         System.out.println("--------------------\n");
     }
 
     /**
      * Prints the request attributes to stdout. Generally used for debugging
      */
     public void printRequestAttributes() {
         System.out.println("\n\n show request attributes\n--------------------\n");
         Enumeration enum = request.getAttributeNames();
         while (enum.hasMoreElements()) {
             String name = (String) enum.nextElement();
             System.out.println("name :" + name);
         }
         System.out.println("--------------------\n");
     }
 
     /**
      * Parses all request parameters for visual beans.
      * A visual bean parameter has the following encoding:
      * ui_<visual bean element>_<bean Id>_name
      * where <visual bean element> is a two letter encoding of the kind of
      * visual bean that it is.
      *
      * @param req the PortletRequest
      */
     protected void createTagBeans(HttpServletRequest req) {
         log.debug("in createTagBeans");
         if (tagBeans == null) tagBeans = new HashMap();
         Map paramsMap = new HashMap();
         // check for file upload
         paramsMap = parseFileUpload(req);
         Enumeration enum = request.getParameterNames();
         while (enum.hasMoreElements()) {
             String uiname = (String) enum.nextElement();
             String[] vals = request.getParameterValues(uiname);
             paramsMap.put(uiname, vals);
         }
 
         //Enumeration enum = request.getParameterNames();
 
         Iterator it = paramsMap.keySet().iterator();
 
         while (it.hasNext()) {
 
             String uiname = (String) it.next();
             String vb = "";
             String name = "";
             String beanId = "";
 
             if (!uiname.startsWith("ui")) continue;
             log.debug("found a tag bean: " + uiname);
 
             String vbname = uiname.substring(3);
 
             int idx = vbname.indexOf("_");
 
             if (idx > 0) {
                 vb = vbname.substring(0, idx);
                 //System.out.println("vb type :" + vb);
             }
 
             vbname = vbname.substring(idx + 1);
             idx = vbname.indexOf("_");
 
             if (idx > 0) {
                 beanId = vbname.substring(0, idx);
                 //System.out.println("beanId :" + beanId);
             }
 
             name = vbname.substring(idx + 1);
             System.out.println("name :" + name);
 
             String[] vals = (String[]) paramsMap.get(uiname); //request.getParameterValues(uiname);
             /*for (int i = 0; i < vals.length; i++) {
                 System.err.println("vals[" + i +"] = " + vals[i]);
             }*/
 
             String beanKey = getBeanKey(beanId);
             if (vb.equals(TextFieldBean.NAME)) {
                 log.debug("Creating a textfieldbean bean with id:" + beanId);
                 TextFieldBean bean = new TextFieldBean(req, beanId);
                 bean.setValue(vals[0]);
                 log.debug("setting new value" + vals[0]);
                 bean.setName(name);
                 //System.err.println("putting a bean: " + beanId + "into tagBeans with name: " + name);
                 tagBeans.put(beanKey, bean);
             } else if (vb.equals(FileInputBean.NAME)) {
                 this.printRequestAttributes();
                 log.debug("Creating a fileinput bean with id:" + beanId);
                 try {
                     FileInputBean bean = null;
                     if (savedFileItem != null) {
                         bean = new FileInputBean(req, beanId, savedFileItem);
                     } else {
                         bean = new FileInputBean(req, beanId);
                     }
                     bean.setName(name);
                     tagBeans.put(beanKey, bean);
                 } catch (IOException e) {
                     log.error("Unable to create file input bean: " + e);
                 }
                 //System.err.println("putting a bean: " + beanId + "into tagBeans with name: " + name);
 
             } else if (vb.equals(CheckBoxBean.NAME)) {
                 CheckBoxBean bean = (CheckBoxBean) tagBeans.get(beanKey);
                 if (bean == null) {
                     log.debug("Creating a checkbox bean with id:" + beanId);
                     bean = new CheckBoxBean(req, beanId);
                     bean.setValue(vals[0]);
                     for (int i = 0; i < vals.length; i++) {
                         String val = vals[i];
                         bean.addSelectedValue(val);
                     }
                     bean.setName(name);
                 } else {
                     /*is this called anytime ? */
                     log.debug("Using existing checkbox bean with id:" + beanId);
                     bean.addSelectedValue(vals[0]);
                 }
                 bean.setSelected(true);
 
                 //System.err.println("putting a bean: " + beanId + "into tagBeans with name: " + name);
                 tagBeans.put(beanKey, bean);
             } else if (vb.equals(ListBoxBean.NAME)) {
                 log.debug("Creating a listbox bean with id:" + beanId);
                 ListBoxBean bean = new ListBoxBean(req, beanId);
                 bean.setName(name);
                 for (int i = 0; i < vals.length; i++) {
                     ListBoxItemBean item = new ListBoxItemBean();
                     item.setName(vals[i]);
                     item.setValue(vals[i]);
                     item.setSelected(true);
                     log.debug("adding an item bean: " + vals[i]);
                     bean.addBean(item);
                 }
                 //System.err.println("putting a bean: " + beanId + "into tagBeans with name: " + name);
                 tagBeans.put(beanKey, bean);
             } else if (vb.equals(RadioButtonBean.NAME)) {
                 RadioButtonBean bean = (RadioButtonBean) tagBeans.get(beanKey);
                 if (bean == null) {
                     log.debug("Creating a new radiobutton bean with id:" + beanId);
                     bean = new RadioButtonBean(req, beanId);
                     bean.setValue(vals[0]);
                     bean.addSelectedValue(vals[0]);
                     bean.setName(name);
                 } else {
                     log.debug("Using existing radiobutton bean with id:" + beanId);
                     bean.addSelectedValue(vals[0]);
                 }
                 bean.setSelected(true);
                 //System.err.println("putting a bean: " + beanId + "into tagBeans with name: " + name);
                 tagBeans.put(beanKey, bean);
             } else if (vb.equals(PasswordBean.NAME)) {
                 log.debug("Creating a passwordbean bean with id:" + beanId);
                 PasswordBean bean = new PasswordBean(req, beanId);
                 bean.setValue(vals[0]);
                 bean.setName(name);
                 //System.err.println("putting a bean: " + beanId + "into tagBeans with name: " + name);
                 tagBeans.put(beanKey, bean);
             } else if (vb.equals(TextAreaBean.NAME)) {
                 log.debug("Creating a textareabean bean with id:" + beanId);
                 TextAreaBean bean = new TextAreaBean(req, beanId);
                 bean.setValue(vals[0]);
                 bean.setName(name);
                 //System.err.println("putting a bean: " + beanId + "into tagBeans with name: " + name);
                 tagBeans.put(beanKey, bean);
             } else if (vb.equals(HiddenFieldBean.NAME)) {
                 log.debug("Creating a hidden bean bean with id:" + beanId);
                 HiddenFieldBean bean = new HiddenFieldBean(req, beanId);
                 bean.setValue(vals[0]);
                 bean.setName(name);
                 //System.err.println("putting a bean: " + beanId + "into tagBeans with name: " + name);
                 tagBeans.put(beanKey, bean);
             } else {
                 log.error("unable to find suitable bean type for : " + uiname);
             }
 
             /*
             String values[] = request.getParameterValues(name);
             if (values.length == 1) {
                 String pval = values[0];
                 if (pval.length() == 0) {
                     pval = "no value";
                 }
                 System.out.println(" value : " + pval);
             } else {
                 System.out.println(" value :");
                 for (int i = 0; i < values.length; i++) {
                     System.out.println("            - " + values[i]);
                 }
             }
             */
 
         }
 
     }
 
 
     private Map parseFileUpload(HttpServletRequest req) {
         Map parameters = new Hashtable();
         if (FileUpload.isMultipartContent(req)) {
             //log.debug("Multipart!");
             DiskFileUpload upload = new DiskFileUpload();
             // Set upload parameters
             upload.setSizeMax(FileInputBean.MAX_UPLOAD_SIZE);
             upload.setRepositoryPath(FileInputBean.TEMP_DIR);
             List fileItems = null;
             try {
                 fileItems = upload.parseRequest(req);
 
             } catch (Exception e) {
                 log.error("Error Parsing multi Part form.Error in workaround!!!", e);
             }
 
             if (fileItems != null) {
                 for (int i = 0; i < fileItems.size(); i++) {
                     FileItem item = (FileItem) fileItems.get(i);
                     String[] tmpstr = new String[1];
                     if (item.isFormField()) {
                         tmpstr[0] = item.getString();
                     } else {
                         tmpstr[0] = "fileinput";
                         /** FileInput attribute is a FileItem*/
                         savedFileItem = item;
                     }
                     log.debug("Name: " + item.getFieldName() + " Value: " + tmpstr[0]);
                     parameters.put(item.getFieldName(), tmpstr);
                 }
 
             }
 
         }
         return parameters;
     }
 
     /**
      * Returns a bean key identifier using the component identifier
      *
      * @param beanId the bean identifier
      * @return the bean key identifier
      */
     protected String getBeanKey(String beanId) {
         String compId = (String) request.getAttribute(SportletProperties.COMPONENT_ID);
         return beanId + "_" + compId;
     }
 
     /**
      * Stores any created beans into the request
      */
     public void store() {
         Iterator it = tagBeans.values().iterator();
         while (it.hasNext()) {
             TagBean tagBean = (TagBean) it.next();
             //log.debug("storing bean id: " + tagBean.getBeanId());
             tagBean.store();
         }
         //printRequestAttributes();
 
     }
 
     /**
      * Logs all tag bean identifiers, primarily used for debugging
      */
     public void printTagBeans() {
         log.debug("in print tag beans:");
         Iterator it = tagBeans.values().iterator();
         while (it.hasNext()) {
             TagBean tagBean = (TagBean) it.next();
             log.debug("tag bean id: " + tagBean.getBeanId());
         }
     }
 
     public ActionMenuItemBean getActionMenuItemBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (ActionMenuItemBean) tagBeans.get(beanKey);
         }
         ActionMenuItemBean ami = new ActionMenuItemBean(request, beanId);
         tagBeans.put(beanKey, ami);
         return ami;
     }
 
     public DataGridBean getDataGridBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (DataGridBean) tagBeans.get(beanKey);
         }
         DataGridBean dgBean = new DataGridBean(request, beanId);
         tagBeans.put(beanKey, dgBean);
         return dgBean;
     }
 
     public ActionMenuBean getActionMenuBean(String beanId) {
         String beanKey = getBeanKey(beanId);
         if (tagBeans.containsKey(beanKey)) {
             return (ActionMenuBean) tagBeans.get(beanKey);
         }
         ActionMenuBean am = new ActionMenuBean(request, beanId);
         tagBeans.put(beanKey, am);
         return am;
     }
 
 }
