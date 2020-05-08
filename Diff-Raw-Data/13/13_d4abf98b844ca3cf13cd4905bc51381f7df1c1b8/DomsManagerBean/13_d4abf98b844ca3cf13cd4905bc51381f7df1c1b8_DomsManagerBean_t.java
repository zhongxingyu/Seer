 package dk.statsbiblioteket.doms.gui;
 
 import dk.statsbiblioteket.doms.client.exceptions.ServerOperationFailed;
 import dk.statsbiblioteket.doms.client.exceptions.XMLParseException;
 import dk.statsbiblioteket.doms.client.methods.Parameter;
 import dk.statsbiblioteket.doms.client.objects.DigitalObject;
 import dk.statsbiblioteket.doms.client.objects.MissingObject;
 import dk.statsbiblioteket.doms.client.relations.ObjectRelation;
 import dk.statsbiblioteket.doms.client.sdo.SDOParsedXmlElement;
 import dk.statsbiblioteket.doms.client.utils.Constants;
 import dk.statsbiblioteket.doms.repository.RepositoryBean;
 import dk.statsbiblioteket.doms.util.Util;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.*;
 import org.jboss.seam.annotations.datamodel.DataModel;
 import org.jboss.seam.faces.FacesMessages;
 import org.jboss.seam.log.Log;
 import org.richfaces.event.UploadEvent;
 
 import javax.ejb.Remove;
 import javax.faces.application.FacesMessage;
 import javax.faces.model.SelectItem;
 import java.util.*;
 
 @Scope(ScopeType.SESSION)
 @Name("domsManager")
 @Synchronized(timeout=Long.MAX_VALUE)
 public class DomsManagerBean {
 
     @In(create = true)
     private RepositoryBean repository;
 
     private DigitalObject rootDataObject;
     private DigitalObject selectedDataObject;
 
     private String selectedChildRelation;
 
     private int keepAliveIntervalMillis = 0;
 
     private boolean displayOnlyRequired = false;
 
 
     List<SelectItem> deleteModes = new ArrayList<SelectItem>();
 
 
     @Logger
     private Log logger;
 
     public RepositoryBean getRepository()
     {
         return repository;
     }
 
     @Create
     public void setupDomsManager() {
         keepAliveIntervalMillis = Util.extractInt(Util.getInitParameter("keepAliveIntervalMillis"));
         if(keepAliveIntervalMillis <= 0) {
             keepAliveIntervalMillis = 600000;
         }
     }
 
 
     public DigitalObject getRootDataObject() {
         return rootDataObject;
     }
 
     public String reload() throws ServerOperationFailed {
 
         rootDataObject = repository.getDomsClient().getFactory().getDigitalObject(rootDataObject.getPid());
 
         if (rootDataObject instanceof MissingObject){
             rootDataObject = null;
         } else {
             Set<DigitalObject> children = rootDataObject.getChildObjects("GUI");
             for (DigitalObject child : children) {
                 if (child.getPid().equals(selectedDataObject.getPid())){
                     selectedDataObject = child;
                     break;
                 }
             }
         }
         return "/editRecord.xhtml";
     }
 
     public void setRootDataObject(String pid) throws ServerOperationFailed {
         if ((pid!=null) && (pid.length()>0)) {
             rootDataObject = repository.getDomsClient().getFactory().getDigitalObject(pid);
             if (rootDataObject instanceof MissingObject){
                 rootDataObject = null;
                 return;
             }
             selectedDataObject = rootDataObject;
         }
         else {
             if (pid==null) {
                 logger.warn("setRootDataObject was called with null");
             }
             else {
                 logger.warn("setRootDataObject was called with empty string");
             }
         }
     }
 
     public void save() throws ServerOperationFailed, XMLParseException {
         saveAll(rootDataObject);
         FacesMessages.instance().add(new FacesMessage("Record saved"));
 
     }
 
     private void saveAll(DigitalObject parentDataobject) throws ServerOperationFailed, XMLParseException {
         logger.info("Saving dataobject: " + parentDataobject.getPid());
         parentDataobject.save(Constants.VIEW_GUI);
 
     }
 
     @Remove
     @Destroy
     public void cleanup() {
 
     }
 
     public boolean displayThisField(boolean required){
         return displayOnlyRequired ? required : true;
     }
 
     public boolean isDisplayOnlyRequired() {
         return displayOnlyRequired;
     }
 
     public void toggleDisplayRequired(){
         displayOnlyRequired = !displayOnlyRequired;
     }
 
     public DigitalObject getSelectedDataObject() {
         return selectedDataObject;
     }
 
     public void setSelectedDataObject(DigitalObject selectedDataObject) {
         this.selectedDataObject = selectedDataObject;
     }
 
     public void createElement(SDOParsedXmlElement dataobject) {
         dataobject.create();
     }
 
     public void deleteElement(SDOParsedXmlElement dataobject) {
         dataobject.delete();
     }
 
     public String getSelectedChildRelation() {
         return selectedChildRelation;
     }
 
     public void setSelectedChildRelation(String selectedChildRelation) {
         this.selectedChildRelation = selectedChildRelation;
     }
 
     public void createChildRelation() {
 
     }
 
     private String uploadFilePath = "";
 
 
     public String getUploadFilePath() {
         return uploadFilePath;
     }
 
     public void setUploadFilePath(String uploadFilePath) {
         this.uploadFilePath = uploadFilePath;
     }
 
     public void selectUploadPath(){
 
     }
 
    public String publish() throws ServerOperationFailed, XMLParseException {
         try {
             save();
             setState(Constants.FedoraState.Active);
         } catch(Exception e) {
             setState(Constants.FedoraState.Inactive);
            Util.addFacesMessage("Publish failed.");
             System.out.println(e.getMessage());
             //TODO beuatify this error message
             String message = e.getMessage();
             message = message.replaceAll("</problem>","</problem>\n");
            Util.addFacesMessage("\n"+message);
            return "/editRecord.xhml";
         }
         FacesMessages.instance().add(new FacesMessage("Record published successfully"));
         selectedDataObject = rootDataObject;
        return "/editRecord.xhml";
     }
 
     /**
      * Determines if the open record has any elements that are in active mode.
      * @return
      */
     public boolean isActive() throws ServerOperationFailed {
         if(rootDataObject == null) {
             return true;
         }
         if(rootDataObject.getState().equals(Constants.FedoraState.Active)){
             return true;
         }
         return childActive(rootDataObject);
     }
 
     /**
      * Determines if the open record has any elements that are in active mode.
      * @return
      */
     public boolean isActiveSelected() throws ServerOperationFailed {
         if (selectedDataObject == null){
             return false;
         }
         if(selectedDataObject.getState().equals(Constants.FedoraState.Active)){
             return true;
         }
         return false;
     }
 
     public String getSelectedDataObjectStateString() throws ServerOperationFailed {
         if (selectedDataObject == null){
             return null;
         }
         switch (selectedDataObject.getState()){
             case Deleted: return "Deleted";
             case Inactive: return "Editable";
             case Active: return "Published";
         }
         return null;
 
     }
 
 
     private boolean childActive(DigitalObject root) throws ServerOperationFailed {
         for(DigitalObject d : root.getChildObjects("GUI")) {
             if(d.getState().equals(Constants.FedoraState.Active)){
                 return true;
             }
             childActive(d);//TODO broken recursiion
         }
         return false;
     }
 
     private boolean fileUploaded = false;
     public void uploadListener(UploadEvent event) {
         fileUploaded = true;
     }
 
     public boolean isFileUploaded() {
         return fileUploaded;
     }
 
     public void setFileUploaded(boolean fileUploaded) {
         this.fileUploaded = fileUploaded;
     }
 
     public void setEditState() throws ServerOperationFailed, XMLParseException {
         setState(Constants.FedoraState.Inactive);
         selectedDataObject = rootDataObject;
         FacesMessages.instance().add(new FacesMessage("Record is now in edit mode"));
     }
 
     public void setActiveState() throws ServerOperationFailed, XMLParseException {
         setState(Constants.FedoraState.Active);
         FacesMessages.instance().add(new FacesMessage("Record is now in active mode"));
     }
 
 
 
     private void setState(Constants.FedoraState s) throws ServerOperationFailed, XMLParseException {
         rootDataObject.setState(s,"GUI");
         rootDataObject.save("GUI");
     }
 
 
 
     public int getKeepAliveInterval() {
         return keepAliveIntervalMillis;
     }
 
     public String getStateImagePathLarge(Constants.FedoraState state) {
         String img = "/img/error.gif";
         if(state != null) {
             if(state.equals(Constants.FedoraState.Active)) {
                 img = "/img/24_published_01.jpg";
             }
             if(state.equals(Constants.FedoraState.Inactive)) {
                 img = "/img/24_unpublished_01.jpg";
             }
         }
         return img;
     }
 
     public String getStateImagePathMedium(Constants.FedoraState state) {
         String img = "/img/error.gif";
         if(state != null) {
             if(state.equals(Constants.FedoraState.Active)) {
                 img = "/img/16_published_01.jpg";
             }
             if(state.equals(Constants.FedoraState.Inactive)) {
                 img = "/img/16_unpublished_01.jpg";
             }
         }
         return img;
     }
 
 
     public boolean isRootSelected() {
         if(rootDataObject != null && selectedDataObject != null) {
             return rootDataObject.getPid().equals(selectedDataObject.getPid());
         }
         return false;
     }
 
 }
