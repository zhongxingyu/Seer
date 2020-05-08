 /*
  * Instruments.java
  *
  * Created on Apr 17, 2008, 8:12:15 PM
  */
 package org.dialogix.main;
 
 import com.sun.rave.faces.data.DefaultTableDataModel;
 import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
 import com.sun.webui.jsf.model.DefaultTableDataProvider;
 import java.awt.event.ActionEvent;
 import javax.faces.FacesException;
 import javax.faces.component.html.HtmlOutputText;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import org.dialogix.export.DataExporter;
 
 /**
  * <p>Fragment bean that corresponds to a similarly named JSP page
  * fragment.  This class contains component definitions (and initialization
  * code) for all components that you have defined on this fragment, as well as
  * lifecycle methods and event handlers where you may add behavior
  * to respond to incoming events.</p>
  *
  * @author George
  */
 public class Instruments extends AbstractFragmentBean {
     // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
 
     /**
      * <p>Automatically managed component initialization. <strong>WARNING:</strong>
      * This method is automatically generated, so any user-specified code inserted
      * here is subject to being replaced.</p>
      */
     private void _init() throws Exception {
     }
     private String instrumentVersionID = null;
 
     public String getInstrumentVersionID() {
         return instrumentVersionID;
     }
 
     public void setInstrumentVersionID(String instrumentVersionId) {
         this.instrumentVersionID = instrumentVersionId;
     }
     private DefaultTableDataProvider defaultTableDataProvider = new DefaultTableDataProvider();
 
     public DefaultTableDataProvider getDefaultTableDataProvider() {
         return defaultTableDataProvider;
     }
 
     public void setDefaultTableDataProvider(DefaultTableDataProvider dtdp) {
         this.defaultTableDataProvider = dtdp;
     }
     private DefaultTableDataModel dataTable1Model = new DefaultTableDataModel();
 
     public DefaultTableDataModel getDataTable1Model() {
         return dataTable1Model;
     }
 
     public void setDataTable1Model(DefaultTableDataModel dtdm) {
         this.dataTable1Model = dtdm;
     }
     private HtmlOutputText outputText4 = new HtmlOutputText();
 
     public HtmlOutputText getOutputText4() {
         return outputText4;
     }
 
     public void setOutputText4(HtmlOutputText hot) {
         this.outputText4 = hot;
     }
     // </editor-fold>
 
     public Instruments() {
     }
 
     /**
      * <p>Callback method that is called whenever a page containing
      * this page fragment is navigated to, either directly via a URL,
      * or indirectly via page navigation.  Override this method to acquire
      * resources that will be needed for event handlers and lifecycle methods.</p>
      * 
      * <p>The default implementation does nothing.</p>
      */
     @Override
     public void init() {
         // Perform initializations inherited from our superclass
         super.init();
         // Perform application initialization that must complete
         // *before* managed components are initialized
         // TODO - add your own initialiation code here
 
 
         // <editor-fold defaultstate="collapsed" desc="Visual-Web-managed Component Initialization">
         // Initialize automatically managed components
         // *Note* - this logic should NOT be modified
         try {
             _init();
         } catch (Exception e) {
             log("Page1 Initialization Failure", e);
             throw e instanceof FacesException ? (FacesException) e : new FacesException(e);
         }
 
     // </editor-fold>
     // Perform application initialization that must complete
     // *after* managed components are initialized
     // TODO - add your own initialization code here
     }
 
     /**
      * <p>Callback method that is called after rendering is completed for
      * this request, if <code>init()</code> was called.  Override this
      * method to release resources acquired in the <code>init()</code>
      * resources that will be needed for event handlers and lifecycle methods.</p>
      * 
      * <p>The default implementation does nothing.</p>
      */
     @Override
     public void destroy() {
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      *
      * @return reference to the scoped data bean
      */
     protected DataExporter getDataExporter() {
         return (org.dialogix.export.DataExporter) getBean("DataExporter");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      *
      * @return reference to the scoped data bean
      */
     protected ApplicationBean1 getApplicationBean1() {
         return (ApplicationBean1) getBean("ApplicationBean1");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      *
      * @return reference to the scoped data bean
      */
     protected SessionBean1 getSessionBean1() {
         return (SessionBean1) getBean("SessionBean1");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      *
      * @return reference to the scoped data bean
      */
     protected RequestBean1 getRequestBean1() {
         return (RequestBean1) getBean("RequestBean1");
     }
 
     public String runSurvey() {
         //target="_blank" href="servlet/Dialogix?schedule=${(fn:length(ivv.instrumentVersionFileName) > 0) ? ivv.instrumentVersionFileName : ivv.instrumentVersionID}&amp;DIRECTIVE=START"    
         
         // http://dialogix.org:8888/Dialogix/servlet/Dialogix?schedule=1&DIRECTIVE=START
         //FacesContext facesCtx = FacesContext.getCurrentInstance();
         //ExternalContext extCtx = facesCtx.getExternalContext();
         //ServletContext servletCtx = (ServletContext) extCts.getContext();
        String url = "http://localhost:8888/Dialogix/servlet/Dialogix?schedule=" + instrumentVersionID + "&DIRECTIVE=START";
         FacesContext context = FacesContext.getCurrentInstance();
         ExternalContext externalCtx = context.getExternalContext();
         try {
             externalCtx.redirect(url);
 
         } catch (Exception ex) {
 
             ex.printStackTrace();
         } finally {
             context.responseComplete();
         }
         return null;
     }
 }
