 ${copyright}
 
 package ${package};
 
 import java.io.Serializable;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Factory;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.faces.FacesMessages;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.NuxeoPrincipal;
 import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
 import org.nuxeo.ecm.platform.ui.web.invalidations.AutomaticDocumentBasedInvalidation;
 import org.nuxeo.ecm.platform.ui.web.invalidations.DocumentContextBoundActionBean;
 import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
 
 /**
  * Code skeleton for a Bean that is controlling
  * a new Tab like action (a xhtml fragment)
  *
  * This can be used :
  *  - to display additional informations about a document
  *  - to provide a screen that allow to update the document
  *
  * The bean is by default Conversation scoped
  * so that :
  *  - if you have expensive computation it will be run only when needed
  *  - you can manage view/update/view cycle
  *
  *  All state should be managed as member variables of the beans.
  *  The default invalidation policy is to reset the state when
  *  the currentDocument changes.
  *
  */
@Name("${bean}")
 @Scope(ScopeType.CONVERSATION)
 @AutomaticDocumentBasedInvalidation
 public class ${class} extends DocumentContextBoundActionBean implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     private static final Log log = LogFactory.getLog(${class}.class);
 
     @In(create = true, required = false)
     protected transient CoreSession documentManager;
 
     @In(create = true)
     protected NavigationContext navigationContext;
 
     @In(create = true, required = false)
     protected transient FacesMessages facesMessages;
 
     @In(create = true)
     protected transient ResourcesAccessor resourcesAccessor;
 
     @In(create = true, required = false)
     protected NuxeoPrincipal currentNuxeoPrincipal;
 
     // there 3 fields are here for demonstration purpose only
     protected String title;
     protected String[] contributors = new String[0];
     protected int updateCount = 0;
 
     // This method is automatically called
     //  - before your bean will be called by JSF or any other bean
     //  - only if the currentDocument has changed
     //
     // To get the currentDocument, you can directly use
     // the inherited getCurrentDocument method
     @Override
     protected void resetBeanCache(DocumentModel currentDoc) {
         try {
             title=getCurrentDocument().getTitle();
             contributors = (String[]) getCurrentDocument().getPropertyValue("dc:contributors");
             updateCount=0;
         } catch (ClientException e) {
             log.error("Cannot fetch document properties", e);
         }
     }
 
     // Sample property that is exposed via setter/getter
     public String getTitle() throws Exception {
         return title;
     }
 
     public void setTitle(String value) throws Exception {
         title=value;
     }
 
     // Gives access to the updateCounter that is maintained by the bean
     @Factory(value="${bean?uncap_first}UpdateCount", scope=ScopeType.EVENT)
     public int getUpdateCount()  {
         return updateCount;
     }
 
     // Sample property that is exposed via a Factory
     // Main advantage is that the method will never be
     // called more than once per page
     @Factory(value="${bean?uncap_first}Contributors", scope=ScopeType.EVENT)
     public String[] getContributors() throws Exception {
         return contributors;
     }
 
     // Sample method that is bound to form submit
     public String save() {
         // commit the change in document
         try {
             DocumentModel doc = getCurrentDocument();
             doc.setPropertyValue("dc:title", title);
             documentManager.saveDocument(doc);
             documentManager.save();
         } catch (ClientException e) {
             log.error("Cannot save title", e);
             return null;
         }
         updateCount+=1;
         // if you need to change the current document and let Nuxeo
         // select the correct view
         // you can use navigationContext and return the view
         //
         // return navigationContext.navigateToDocument(doc);
 
         // If you want to explicitly go to a given view
         // just return the outcome string associated to the view
         //
         // return "someView";
 
         // stay on the same view
         return null;
     }
 
     // this method will be called by the action system to determine if the
     // action should be available
     //
     // the return value can depend on the context,
     // you can use the navigationContext to get the currentDocument,
     // currentWorkspace ...
     // you can cache the value in a member variable as long as the Bean stays
     // Event scoped
     //
     // if you don't need this, you should remove the filter in the associated
     // action contribution
     public boolean accept() {
         return true;
     }
 }
