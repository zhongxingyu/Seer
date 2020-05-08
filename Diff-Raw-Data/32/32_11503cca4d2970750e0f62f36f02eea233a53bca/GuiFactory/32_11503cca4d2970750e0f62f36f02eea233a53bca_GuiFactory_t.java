 package dk.statsbiblioteket.doms.gui;
 
 
 import dk.statsbiblioteket.doms.client.datastreams.Datastream;
 import dk.statsbiblioteket.doms.client.datastreams.DatastreamDeclaration;
 import dk.statsbiblioteket.doms.client.datastreams.ExternalDatastream;
 
 import dk.statsbiblioteket.doms.client.exceptions.NotFoundException;
 import dk.statsbiblioteket.doms.client.exceptions.ServerOperationFailed;
 import dk.statsbiblioteket.doms.client.exceptions.XMLParseException;
 import dk.statsbiblioteket.doms.client.objects.ContentModelObject;
 import dk.statsbiblioteket.doms.client.objects.DigitalObject;
 import dk.statsbiblioteket.doms.client.objects.FileObject;
 import dk.statsbiblioteket.doms.client.relations.RelationDeclaration;
 import dk.statsbiblioteket.doms.client.sdo.SDOParsedXmlDocument;
 import dk.statsbiblioteket.doms.client.sdo.SDOParsedXmlElement;
 import dk.statsbiblioteket.doms.client.utils.Constants;
 import dk.statsbiblioteket.doms.gui.model.DigitalObjectTreeWrapper;
 import dk.statsbiblioteket.doms.util.StringUtils;
 import org.ajax4jsf.component.UIRepeat;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Logger;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.annotations.datamodel.DataModel;
 import org.jboss.seam.log.Log;
 import org.richfaces.component.UITree;
 import org.richfaces.component.html.HtmlFileUpload;
 import org.richfaces.event.NodeSelectedEvent;
 import org.richfaces.event.UploadEvent;
 import org.richfaces.model.UploadItem;
 
 import javax.faces.component.html.HtmlInputText;
 import javax.faces.context.FacesContext;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 @Name("guiFactory")
 @Scope(ScopeType.EVENT)
 public class GuiFactory {
 
 
     @Logger
     private Log logger;
 
 
     @In(create = true)
     private DomsManagerBean domsManager;
 
     private EditRecordBean editRecordBean;
 
     private EditRecordBean getEditRecordBean() {
         if (editRecordBean == null) {
             editRecordBean = new EditRecordBean();
         }
         return editRecordBean;
     }
 
     private int tabIndex = 0;
 
     public int getTabIndex() {
         return tabIndex++;
     }
 
     /*
       * srcRoots is the data source for rich:recursiveTreeNodesAdaptor in
       * editRecord.xhtml It contains the root DOMSXmlElement for each data stream
       * in the selected data object.
       */
     @DataModel
     private ArrayList<SDOParsedXmlElement> srcRoots = null;
 
     private void setupSrcRoots()
             throws ServerOperationFailed,
                    XMLParseException {
         srcRoots = new ArrayList<SDOParsedXmlElement>();
         DigitalObject dataobject = domsManager.getSelectedDataObject();
         for (Datastream datastream : dataobject.getDatastreams()) {
             boolean include = false;
 
             Set<DatastreamDeclaration> declarations = datastream.getDeclarations();
             for (DatastreamDeclaration declaration : declarations) {
                 if (declaration.getPresentation()== Constants.GuiRepresentation.editable){
                     include = true;
                 }
             }
             if (include){
                 SDOParsedXmlDocument parsed = datastream.getSDOParsedDocument();
                 if (parsed != null){
                     srcRoots.add(parsed.getRootSDOParsedXmlElement());
                 }
             }
 
         }
     }
 
     public List<SDOParsedXmlElement> getSourceRoots()
             throws ServerOperationFailed,
                    XMLParseException {
         if(domsManager.getSelectedDataObject() == null) {
             return null;
         }
         String currentPid = domsManager.getSelectedDataObject().getPid();
         if ((currentPid != getEditRecordBean().getRenderedPid())
             || (srcRoots == null)) {
             setupSrcRoots();
             getEditRecordBean().setRenderedPid(currentPid);
         }
 
         return srcRoots;
     }
 
     /** **Start Navigation tree******************* */
     /*
       * Navigation tree
       */
     @DataModel
     private ArrayList<DigitalObject> navigationDataObjects = null;
 
     public ArrayList<DigitalObject> getNavigationDataObjects()
     {
         if (navigationDataObjects == null) {
             navigationDataObjects = new ArrayList<DigitalObject>();
             navigationDataObjects.add(new DigitalObjectTreeWrapper(domsManager.getRootDataObject()));
         }
         return navigationDataObjects;
     }
 
     public String selectDataObject(DigitalObject dataobject) {
         domsManager.setSelectedDataObject(dataobject);
         return "/editRecord.xhtml";
     }
 
     public boolean detailsTreeExpanded(UITree t) {
         return true;
     }
 
     public boolean navigationTreeExpanded(UITree t) {
         return true;
     }
 
     public boolean navigationTreeSelected(UITree t) {
         DigitalObject element = (DigitalObject) t.getRowData();
         if(element == null || domsManager.getSelectedDataObject() == null) {
             return false;
         }
         return (element.getPid().equals(domsManager.getSelectedDataObject()
                                                 .getPid()));
     }
 
     /** **End Navigation tree******************************** */
 
     public boolean getHasPossibleChildRelations() throws ServerOperationFailed {
         DigitalObject dataobject = domsManager.getSelectedDataObject();
         if (dataobject!=null) {
             for (ContentModelObject contentModelObject : dataobject.getType()) {
                 for (RelationDeclaration relationDeclaration : contentModelObject.getRelationModel()
                         .getRelationDeclarations()) {
                     if (relationDeclaration.getViewAngles().contains("GUI")){
                         return true; //TODO figure out if all the relations have been filled, so there are no more allowed
                     }
                 }
             }
         }
         return false;
     }
 
 
 
     /** **Start Uploadable************************************* */
     /*
       * Handle UPLOADABLE DOMSDataStream
       */
     public boolean isUploadable() throws ServerOperationFailed, NotFoundException {
         DigitalObject dataobject = domsManager.getSelectedDataObject();
         if(dataobject != null) {
             if (dataobject instanceof DigitalObjectTreeWrapper) {
                 DigitalObjectTreeWrapper digitalObjectTreeWrapper = (DigitalObjectTreeWrapper) dataobject;
                 dataobject = digitalObjectTreeWrapper.getWrapped();
             }
             if (dataobject instanceof FileObject){
                 return true;
             }
         }
         return false;
     }
 
 
    public String getLocalContentPath() throws ServerOperationFailed {
         DigitalObject dataobject = domsManager.getSelectedDataObject();
        return "Not implemented yet";
/*
         if (dataobject != null) {

             Datastream ds = dataobject.getDatastream("GUI");
             if(ds != null) {

 //                if (ds.getTemppath() != null){
 //                    return ds.getTemppath();
 //                }
             }
         }
         return null;
*/
     }
 
 
    public boolean getHasContent() throws ServerOperationFailed {
         DigitalObject dataobject = domsManager.getSelectedDataObject();
         if (dataobject != null) {
            Datastream ds = null;
            try {
                ds = dataobject.getDatastream("CONTENTS");
            } catch (NotFoundException e) {
                return false;
            }
             if(ds != null) {
                 if (ds instanceof ExternalDatastream) {
                     ExternalDatastream externalDatastream = (ExternalDatastream) ds;
                     return externalDatastream.getUrl() != null;
                 }
 
             }
         }
         return false;
     }
 
 
     public String getContent() throws ServerOperationFailed, NotFoundException {
         DigitalObject dataobject = domsManager.getSelectedDataObject();
         if (dataobject != null) {
             Datastream ds = dataobject.getDatastream("CONTENTS");
             if(ds != null) {
                 if (ds instanceof ExternalDatastream) {
                     ExternalDatastream externalDatastream = (ExternalDatastream) ds;
 
                     String contents = externalDatastream.getUrl();
                     if (contents != null){
                         String href = "";
                         try {
                             URL url = new URL(contents);
                             href = url.toString();
                         } catch (MalformedURLException ex) {
                             href = "The url is not valid. "
                                    + contents;
                         }
                         return href;
                     }
                 }
             }
         }
 
         return "";
     }
 
     private HtmlFileUpload contentFileUpload;
 
     public void setContentFileUpload(HtmlFileUpload fileupload) {
         contentFileUpload = fileupload;
     }
 
     public HtmlFileUpload getContentFileUpload() {
         return contentFileUpload;
     }
 
     public String getContentFilenameFileUploadId() {
         return "'"
                + contentFileUpload.getClientId(FacesContext
                                                        .getCurrentInstance()) + "'";
     }
 
     private HtmlInputText contentFilenameInputText;
 
     public void setContentFilenameInputText(HtmlInputText inputtext) {
         contentFilenameInputText = inputtext;
     }
 
     public HtmlInputText getContentFilenameInputText() {
         return contentFilenameInputText;
     }
 
     public String getContentFilenameInputTextId() {
         return "'"
                + contentFilenameInputText.getClientId(FacesContext
                                                               .getCurrentInstance()) + "'";
     }
 
     /** **End uploadable *************************************** */
 
     /** **Start inline XML************************************** */
     /*
       * Handle readonly inline XML data streams
       */
 
     @DataModel
     private ArrayList<Datastream> readonlyInlineXmlDataStreams = null;
 
     private void setupReadonlyInlineXmlDataStreams() throws ServerOperationFailed, NotFoundException {
         readonlyInlineXmlDataStreams = new ArrayList<Datastream>();
         DigitalObject dataobject = domsManager.getSelectedDataObject();
         if(dataobject == null) {
             return;
         }
         //TODO fix with asger
         // readonlyInlineXmlDataStreams.add(dataobject.getDatastream("READONLY")); //getReadonlyDatastreams());
     }
 
     public ArrayList<Datastream> getReadOnlyInlineXmlDS() throws ServerOperationFailed, NotFoundException {
         if(domsManager.getSelectedDataObject() == null) {
             return null;
         }
         String currentPid = domsManager.getSelectedDataObject().getPid();
         if ((currentPid != getEditRecordBean()
                 .getInlineXmlReadOnlyRenderedPid())
             || (readonlyInlineXmlDataStreams == null)) {
             setupReadonlyInlineXmlDataStreams();
             getEditRecordBean().setInlineXmlReadOnlyRenderedPid(currentPid);
         }
 
         return readonlyInlineXmlDataStreams;
     }
 
     /*
       * Handle imported inline xml
       */
 
     @DataModel
     private ArrayList<Datastream> importedInlineXmlDataStreams = null;
     private UIRepeat importedInlineXmlRepeater;
 
     public void setImportedInlineXmlRepeater(UIRepeat repeater) {
         this.importedInlineXmlRepeater = repeater;
     }
 
     public UIRepeat getImportedInlineXmlRepeater() {
         return importedInlineXmlRepeater;
     }
 
     private void setupImportedInlineXmlDataStreams() throws ServerOperationFailed, NotFoundException {
         importedInlineXmlDataStreams = new ArrayList<Datastream>();
         DigitalObject dataobject = domsManager.getSelectedDataObject();
         if(dataobject == null) {
             return;
         }
         //TODO fix with Asger
         //importedInlineXmlDataStreams.add(dataobject.getDatastream("Importable")); //getImportableDatastreams());
     }
 
     public List<Datastream> getImportedInlineXmlDS() throws ServerOperationFailed, NotFoundException {
 
         if(domsManager.getSelectedDataObject() != null) {
             String currentPid = domsManager.getSelectedDataObject().getPid();
             if ((currentPid != getEditRecordBean()
                     .getInlineXmlImportedRenderedPid())
                 || (importedInlineXmlDataStreams == null)) {
                 setupImportedInlineXmlDataStreams();
                 getEditRecordBean().setInlineXmlImportedRenderedPid(currentPid);
             }
         }
         return importedInlineXmlDataStreams;
     }
 
     public void uploadListener(UploadEvent event) throws ServerOperationFailed, NotFoundException,
                                                          IOException {
         ExternalDatastream selectedDS = (ExternalDatastream) importedInlineXmlRepeater
                 .getRowData();
         UploadItem item = event.getUploadItem();
         if (item.isTempFile()) {
             File file = item.getFile();
             //TODO make work
             //selectedDS.setContents(file.getAbsolutePath());
 
 
             FileInputStream is = new FileInputStream(file);
 
             if (is != null) {
                 String filecontents = StringUtils.readStream(is);
 
                 //  selectedDS.setContents(filecontents);
                 quadrigavalue = filecontents;
                 logger.info("Setting quadrigavalue = "
                             + quadrigavalue);
                 setupImportedInlineXmlDataStreams();
 
             }
         }
         FacesContext.getCurrentInstance().renderResponse();
     }
 
     public String getQuadrigavalue() {
         return quadrigavalue;
     }
 
     private String quadrigavalue = null;
 
     /** **End inline XML*********************************** */
 
 
 
     public void keepAlive() {
         // This method is polled from the navigation site, to keep the session
         // alive when the user is editing
         logger.info("keepAlive");
     }
 
     public void navigationNodeSelected(NodeSelectedEvent e) {
 
     }
 
     public int getRowsFromText(String text){
         int rows = 0;
         if(text != null) {
             rows = text.split("\r\n|\r|\n").length;
         }
         if(rows > 2) {
             return rows;
         }
         return 2;
     }
 }
