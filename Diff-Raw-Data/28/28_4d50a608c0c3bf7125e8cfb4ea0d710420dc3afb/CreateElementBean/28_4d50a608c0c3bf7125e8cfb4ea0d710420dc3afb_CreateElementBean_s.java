 package dk.statsbiblioteket.doms.gui;
 
 
 import dk.statsbiblioteket.doms.central.InvalidResourceException;
 import dk.statsbiblioteket.doms.central.MethodFailedException;
 import dk.statsbiblioteket.doms.client.exceptions.ServerOperationFailed;
 import dk.statsbiblioteket.doms.client.methods.Method;
 import dk.statsbiblioteket.doms.client.methods.Parameter;
 import dk.statsbiblioteket.doms.client.objects.CollectionObject;
 import dk.statsbiblioteket.doms.client.objects.ContentModelObject;
 import dk.statsbiblioteket.doms.client.objects.DigitalObject;
 import dk.statsbiblioteket.doms.client.objects.TemplateObject;
 import dk.statsbiblioteket.doms.client.relations.RelationDeclaration;
 import dk.statsbiblioteket.doms.client.relations.RelationModel;
 import dk.statsbiblioteket.doms.util.Util;
 import dk.statsbiblioteket.util.Pair;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.*;
 import org.jboss.seam.annotations.datamodel.DataModel;
 import org.jboss.seam.faces.FacesMessages;
 import org.jboss.seam.log.Log;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 import javax.xml.rpc.ServiceException;
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.*;
 
 @Name("createElementBean")
 @Scope(ScopeType.PAGE)
 public class CreateElementBean {
 
     @Logger
     private Log logger;
 
     @In(create = true)
     private DomsManagerBean domsManager;
 
     @In
     private OtherRelationsBean otherRelationsBean;
 
     @DataModel
     private List<SelectItem> childElements;
     private String selectedElement;
     private Map<String, Pair<String,TemplateObject>> elementsMap;
     
     public String getSelectedElement() {
         return selectedElement;
     }
 
     public void setSelectedElement(String selectedElement) throws RemoteException, ServiceException {
         this.selectedElement = selectedElement;
     }
 
     @Factory("childElements")
     public void childElementsFactory() {
         childElements = new ArrayList<SelectItem>();
         if (elementsMap == null){
             elementsMap = new Hashtable<String, Pair<String, TemplateObject>>();
         }
     }
 
     public void loadChildElements() throws RemoteException, ServiceException, ServerOperationFailed {
         resetPage();
         if(childElements == null) {
             childElements = new ArrayList<SelectItem>();
         }
         if(elementsMap == null) {
             elementsMap = new HashMap<String, Pair<String, TemplateObject>>();
         }
        
         dk.statsbiblioteket.doms.client.objects.DigitalObject dataobject = domsManager.getSelectedDataObject();
 
         Set<Pair<String,ContentModelObject>> contentModelObjects = new HashSet<Pair<String, ContentModelObject>>();
         
         for (ContentModelObject contentModelObject : dataobject.getType()) {
             RelationModel relModel = contentModelObject.getRelationModel();
             for (RelationDeclaration relationDeclaration : relModel.getRelationDeclarations()) {
                 if (!relationDeclaration.getViewAngles().contains("GUI")){
                     continue;
                 }
                 if (relationDeclaration.getMaxCardinality() > 0){
                     Set<ContentModelObject> contentmodels = relationDeclaration.getFirstLevelModels();
                     for (ContentModelObject contentmodel : contentmodels) {
                         contentModelObjects.add(new Pair<String, ContentModelObject>(relationDeclaration.getPredicate(),contentmodel));
                     }
                 }
             }
         }
         for (Pair<String, ContentModelObject> contentModelObject : contentModelObjects) {
 
 
             Set<TemplateObject> itemplates = contentModelObject.getRight().getTemplates();
             for (TemplateObject itemplate : itemplates) {
                 elementsMap.put(itemplate.getPid(), new Pair<String, TemplateObject>(contentModelObject.getLeft(),itemplate));
                 childElements.add(new SelectItem(itemplate.getPid(),itemplate.getTitle()));
             }
         }
         loadImportsList();
        
     }
 
 
     private Pair<String,TemplateObject> getSelectedElementObject() {
         if(elementsMap != null) {
             return elementsMap.get(selectedElement);
         }
         return null;
     }
 
 
     public String createChildDataObjectPressed() throws RemoteException,
             ServiceException,
             IOException,  ServerOperationFailed, MethodFailedException, InvalidResourceException {
 
         if(getSelectedElementObject() == null) {
             Util.addFacesMessage("Please select a template for the element to create.");
             return "";
         }
 
         logger.info("Creating databobject: "
                 + getSelectedElementObject().getRight().getTitle());
 
         DigitalObject newObject = getSelectedElementObject().getRight().clone();
 
         // Now add the relation.
         domsManager.getSelectedDataObject().addObjectRelation(getSelectedElementObject().getLeft(),newObject);
 
         FacesMessages.instance().add(new FacesMessage("Element created"));
 
         //Select the newly created child
         if(newObject != null) {
             domsManager.setSelectedDataObject(newObject);
         }
 
         //TODO should we save?
         resetPage();
         if(otherRelationsBean != null) {
             otherRelationsBean.clearPage();
         }
         return "/editRecord.xhtml";
     }
 
     private void resetPage() {
         childElements = null;
         elementsMap = null;
         selectedElement = null;
         selectedImport = null;
     }
 
 
     /** **Start Imports********************* */
     @DataModel
     private List<SelectItem> importsList;
     private String selectedImport;
     private Map<String, Method> methodMap;
 
     @Factory("importsList")
     public void importsListFactory() throws RemoteException, ServiceException, ServerOperationFailed {
         importsList = new ArrayList<SelectItem>();
         importsList.add(new SelectItem("", "Select import..."));
         methodMap = new HashMap<String, Method>();
     }
 
     public void loadImportsList() throws RemoteException, ServiceException, ServerOperationFailed {
         importsListFactory();
        List<ContentModelObject> cms = domsManager.getSelectedDataObject().getType();
        if(cms != null) {

            for(ContentModelObject cm : cms) {
                Set<Method> methods = cm.getMethods();
                for(Method method : methods) {
                    methodMap.put(method.getName(), method);
                    importsList.add(new SelectItem(method.getName(), method.getName()));
                }
            }
         }
     }
 
     public String getSelectedImport() {
         return selectedImport;
     }
 
     public void setSelectedImport(String selectedImport) {
         this.selectedImport = selectedImport;
         loadParameters();
     }
     /** **End Imports*********************** */
     
     
     /** **Start parameters*********************** */
     @DataModel
     private List<Parameter> importsParameters;
 
     @Factory("importsParameters")
     public void setupParameters() {
         importsParameters = new ArrayList<Parameter>();
     }
 
     public void loadParameters() {
         importsParameters = new ArrayList<Parameter>();
 
         Method method = methodMap.get(getSelectedImport());
         Set<Parameter> params = method.getParameters();
 
 
         for(Parameter param : params) {
             importsParameters.add(param);
         }
         Collections.sort(importsParameters, new Comparator<Parameter>() {
             @Override
             public int compare(Parameter o1, Parameter o2) {
                 return o1.getName().compareTo(o2.getName());
             }
         });
     }
 
     public boolean renderParameter(Parameter p) {
         if(p == null) {
             return false;
         }
         return true;
     }
     /** **End parameters *********************** */
 
     
     
 }
