 package org.wyona.yanel.impl.resources.jellyadapterofcmdv3;
 
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.transform.Transformer;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.jelly.JellyContext;
 import org.wyona.yanel.core.Resource;
 import org.wyona.yanel.core.api.attributes.CreatableV3;
 import org.wyona.yanel.core.api.attributes.DeletableV1;
 import org.wyona.yanel.core.api.attributes.ModifiableV3;
 import org.wyona.yanel.core.api.attributes.creatable.ResourceInput;
 import org.wyona.yanel.core.api.attributes.creatable.ResourceInputItem;
 import org.wyona.yanel.core.api.attributes.creatable.ResourceInputItemCollection;
 import org.wyona.yanel.core.attributes.viewable.View;
 import org.wyona.yanel.core.util.ResourceAttributeHelper;
 import org.wyona.yanel.impl.jelly.FileItem;
 import org.wyona.yanel.servlet.communication.HttpRequest;
 
 import org.apache.log4j.Logger;
 
 /**
  * Adapts a resource which has CreatableV3 implemented.
  * <p>
  * The templates in addition get the following parameters:
  * <ul>
  * <li>resourceInput - description of the inputs that creatable resource needs
  * <li>url.before.conversation - the VIEW can redirect here when the conversation is cancelled
  * <li>resource.input.collection.action - Decides on what kind of action is taken on collection items. Can be 'add' (default) or 'remove'. 
  * </ul>
  * 
  * This is quite a generic class. Subclasses may want to pass other parameters to the templates.
  * */
 public class JellyAdapterForCUDResource extends JellyConversationAdapter {
 
     private static Logger log = Logger.getLogger(JellyAdapterForCUDResource.class);
 
     // Collection items are special. Values can be removed or added/set to specific index
     public static final String REMOVE_ACTION = "remove";
     public static final String PARAM_RESOURCE_INPUT_COLLECTION_ACTION = "resource.input.collection.action";
     public static final String PARAM_RESOURCE_INPUT_COLLECTION_INDEX = "resource.input.collection.index";
     
     // The three parameters must be specified together!
     public static final String PARAM_RESOURCE_INPUT_COLLECTION_OLD_INDEX = "resource.input.collection.old.index";
     public static final String PARAM_RESOURCE_INPUT_COLLECTION_NEW_INDEX = "resource.input.collection.new.index";
     public static final String PARAM_TARGET_RESOURCE_INPUT = "target.resource.input";
     
     public JellyAdapterForCUDResource() {
     }
 
     /**
      * @see
      */
     public View getView(String viewId)throws Exception{
         try{
             ViewDescriptorUsingTemplate viewDescriptor = (ViewDescriptorUsingTemplate)getViewDescriptor(normalize(viewId));
             if(viewDescriptor == null){
                 throw new IllegalArgumentException("The view descriptor was not found for the given id: '" + viewId + "'");
             }
             log.debug("View ID: " + viewDescriptor.getId());
             
             if (CANCEL_VIEW_ID.equals(viewDescriptor.getId())) {
                 log.warn("Cancel ...");
                 doCancel();
                 destroyConversation();
                 return super.getView(CANCEL_VIEW_ID);
             }
             
             // Deal with the input
             init();
             ConversationState cs = getConversationState();
             
             boolean incomingInputIsValid = true;
             if (cs == null) {
                 throw new IllegalStateException("Conversation state is not available");
             }
 			String oldIndex = getParameterAsString(PARAM_RESOURCE_INPUT_COLLECTION_OLD_INDEX);
 			String newIndex = getParameterAsString(PARAM_RESOURCE_INPUT_COLLECTION_NEW_INDEX);
 			String [] targetInputs = getParameterAsStringValues(PARAM_TARGET_RESOURCE_INPUT);
 			
 			if(oldIndex != null && newIndex != null){
 			    try {
 			        int o = Integer.parseInt(oldIndex);
 			        int n = Integer.parseInt(newIndex);
 			        
 			        ResourceInput ri = (ResourceInput)cs.getModel();
 			        
 			        // Moving input values
 			        for (String target : targetInputs) {
 			            ResourceInputItem item = ri.getItem(target);
 			            if (item instanceof ResourceInputItemCollection) {
 			                ResourceInputItemCollection itemAsCollection = (ResourceInputItemCollection) item;
 			                itemAsCollection.moveValue(o, n);
 			            }
 			        }
 			    } catch (NumberFormatException e) {
 			        log.warn("oldIndex="+oldIndex+", newIndex="+newIndex+": Illegal indexes passed. Operation skiped");
 			    }
 			}else{
 			    // Fill incoming values and validate. This will also 
 			    incomingInputIsValid = fillIncomingInput(cs);
 			}
             
             String nextViewId = viewDescriptor.getId();
             
             
             if(!incomingInputIsValid){
                 if(!viewDescriptor.isFragment()){
                     // When the screen was not valid go to the previous screen
                     nextViewId = cs.getPreviousScreen();
                 }else{
                 }
                 // Keep the same screen for the fragment
                 cs.setCurrentScreen(cs.getPreviousScreen());
             }else {
                 if(!viewDescriptor.isFragment()){
                     // Now the current screen is the next view
                     String previous = cs.getPreviousScreen();
                     cs.setCurrentScreen(nextViewId);
                     if (DONE_VIEW_ID.equals(nextViewId)) {
                         // NOTE: assumes that DONE_VIEW_ID is not a fragment
                         try {
                             commit();
                             destroyConversation();
                         } catch (IllegalArgumentException e) {
                             log.warn("Cannot commit", e);
                             //thrown when the ResourceInput is not valid, then go back
                             nextViewId = previous; 
                             cs.setCurrentScreen(previous);
                         }
                     }
                 }else{
                     // Keep the previous screen when the input is a fragment
                     cs.setCurrentScreen(cs.getPreviousScreen());
                 }
             }
             return super.getView(nextViewId);
         } catch(Throwable e){
             // Cancel the conversation when something goes wrong
             destroyConversation();
             
             throw new Exception("Due to an exception the request has been canceled. Exception message: " + e.getMessage(), e);
         }
     }
 
     /**
      * Try to set the resource input values according to what came in HTTP
      * request.
      * <p>
      * NOTE[very important]: this method relies on the fact that HTTP request 
      * has parameters even for checkboxes and select(multiple), which are normally not submitted
      * when nothing is selected or checked. The "nothing is selected" means that the 
      * parameter is passed but the value is <code>null</code>. 
      * This must be ensured in order to use the adapter correctly, e.g. the simple tag library is 
      * rendering an additional HIDDEN field for this purpose.
      * @return true when all the incoming input items are valid
      */
     private boolean fillIncomingInput(ConversationState cs) throws Exception {
         ResourceInput ri = (ResourceInput) cs.getModel();
         String[] itemNames = ri.getItemNames();
         
         Set incomingParameterNames = new HashSet(getParameters().keySet());
         // Add incoming names of the inputs that are files
         if (getEnvironment().getRequest() instanceof HttpRequest) {
             HttpRequest httpRequest = (HttpRequest) request;
             if (httpRequest.isMultipartRequest()) {
                 Enumeration params = httpRequest.getFileNames();
                 while (params.hasMoreElements()) {
                     incomingParameterNames.add(params.nextElement());
                 }
             }
         }
         //Now we have all incoming parameter names
         boolean inputValid = true;
         for (int i = 0; i < itemNames.length; i++) {
             String action = getParameterAsString(PARAM_RESOURCE_INPUT_COLLECTION_ACTION);
             String index = getParameterAsString(PARAM_RESOURCE_INPUT_COLLECTION_INDEX);
             
             if(!incomingParameterNames.contains(itemNames[i])){
                 // We need to fill only the parameters that were passed
                 continue;
             }
             
             // Go further to addValue()/removeValue()/setValue()
             
             Object paramValue = getParameterAsFileItem(itemNames[i]);
             if(paramValue == null){
                 paramValue = getParameter(itemNames[i]);
             }
             
             if (REMOVE_ACTION.equals(action)){
                 if (ri.getItem(itemNames[i]).getType() == ResourceInputItem.INPUT_TYPE_COLLECTION) {
                     ResourceInputItemCollection collectionItem = (ResourceInputItemCollection) ri.getItem(itemNames[i]);
                     
                     // Remove the value (maybe at the specified index)
                     boolean removed = false;
                     if(paramValue != null){
                         removed = collectionItem.removeValue(paramValue);
                     }
                     if(!removed && index != null){
                         try{
                             int valueIndex = Integer.parseInt(index);
                             collectionItem.removeValue(valueIndex);
                         }catch(NumberFormatException e){
                             log.warn(itemNames[i]+"="+paramValue+": Could not remove the parameter at the index "+index);
                         }
                     }
                 }else{
                     ri.getItem(itemNames[i]).setValue(null);
                 }
             }else{
                 if(ri.getItem(itemNames[i]).getType() == ResourceInputItem.INPUT_TYPE_FILE_UPLOAD){
                     FileItem fi = (FileItem)paramValue;
                     if(fi == null || !fi.hasData()){
                         // This will cause the old data (maybe null) to be left and validated
                         inputValid = ri.getItem(itemNames[i]).validate() && inputValid;
                         continue;
                     }
                 }
                 
                 if (ri.getItem(itemNames[i]).getType() == ResourceInputItem.INPUT_TYPE_COLLECTION) {
                     ResourceInputItemCollection collectionItem = (ResourceInputItemCollection) ri.getItem(itemNames[i]);
                     if(index != null){
                         try{
                             int valueIndex = Integer.parseInt(index);
                             collectionItem.addValue(valueIndex, paramValue);
                         }catch(NumberFormatException e){
                             log.warn(itemNames[i]+"="+paramValue+": Could not add the parameter at the index "+index+", appending");
                             collectionItem.addValue(paramValue);
                         }
                     }else{
                         collectionItem.addValue(paramValue);
                     }
                 } else {
                     ri.getItem(itemNames[i]).setValue(paramValue);
                 }
             }
             inputValid = ri.getItem(itemNames[i]).validate() && inputValid;
         }
         
         return inputValid;
     }
     
     /**
      * @return <code>null</code> when the parameter is not a file upload, otherwise a constructed file item.
      * */
     private FileItem getParameterAsFileItem(String parameterName)throws Exception{
         FileItem fileItem = null;
         if (getEnvironment().getRequest() instanceof HttpRequest) {
             HttpRequest httpRequest = (HttpRequest) request;
             if (httpRequest.isMultipartRequest()) {
                 Enumeration fileParameterNames = httpRequest.getFileNames();
                 while (fileParameterNames.hasMoreElements()) {
                     // create new file item
                     String name = (String) fileParameterNames.nextElement();
                     if (name.equals(parameterName)) {
                         String contentType = httpRequest.getContentType(name);
                         InputStream is = httpRequest.getInputStream(name);
                         fileItem = new FileItem(IOUtils.toByteArray(is), contentType);
                         fileItem.setFileName(httpRequest.getFilesystemName(name));
                         break;
                     }
                 }
             }
         }
         return fileItem;
     }
 
     /**
      *
      */
     protected void init() throws Exception {
         ConversationState cs = getConversationState();
 
         // try to identify the usecase. It is either in the request or in the conversation
         Usecase usecase = getUsecase();
         if (usecase == null) {
             log.warn("No usecase (neither 'create' nor 'update' nor 'delete') has been specified!");
             //throw new UnsupportedOperationException("No usecase (neither 'create' nor 'update' nor 'delete') has been specified!");
         }
         List<Usecase> supportedUsecases = Arrays.asList(Usecase.values());
         if (!supportedUsecases.contains(usecase)) {
             if (cs == null || !supportedUsecases.contains(cs.getUsecase())){
                 throw new UnsupportedOperationException("The following usecase is not supported by this adapter implementation: " + usecase);
             }
             usecase = cs.getUsecase();
         }
         
         String resourcePath = getAdaptedResourcePathFromConversationState();
 
         // TODO: For creation one doesn't need necessarily a resource path, but a resource type definition would be sufficient, whereas for update/modify and delete one needs a resource path
         if(resourcePath == null) {
             log.warn("TODO: Try getting adapted resource from resource configuration ...");
             String adaptedResourceName = getResourceConfigProperty("adapted-resource-name");
             String adaptedResourceNamespace = getResourceConfigProperty("adapted-resource-namespace");
             if (adaptedResourceName != null && adaptedResourceNamespace != null) {
                 log.warn("TODO: Implement initialization of resource from resource type definition: " + adaptedResourceName + ", " + adaptedResourceNamespace);
             }
             throw new IllegalStateException("The adapted resource path must be specified in the request or should be available in the conversation state");
         }
         
         Object model = null;
         if (cs == null || cs.getModel() == null) { // This is check is needed because the model is not serializable
             // Instantiate model, depending on the usecase
             if (Usecase.create.equals(usecase)) {
                 CreatableV3 cv3 = getAdaptedResourceAsCreatableV3(resourcePath);
                 model = cv3.getResourceInputForCreation();
             } else if (Usecase.modify.equals(usecase)) {
                 ModifiableV3 mv3 = getAdaptedResourceAsModifiableV3(resourcePath);
                 model = mv3.getResourceInputForModification();
             } else if (Usecase.remove.equals(usecase)) {
                 DeletableV1 dv1 = getAdaptedResourceAsDeletableV1(resourcePath);
                 model = dv1.getResourceInputForDeletion();
             } else {
                log.warn("Could not identify usecase");
             }
         } else {
             model = cs.getModel();
         }
 
         initConversation(model, usecase, resourcePath);
     }
 
     /**
     * Validates and calls the create() or modify() or delete() method
      */
     public void commit() throws Exception {
         java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSZ");
         if( log.isDebugEnabled() )
             log.debug("Start commit " + dateFormat.format(new java.util.Date()));
         ConversationState cs = getConversationState();
         String path = cs.getResourcePath();
         Usecase usecase = cs.getUsecase();
         
         if (Usecase.create.equals(usecase)) {
             CreatableV3 cv3 = getAdaptedResourceAsCreatableV3(path);
             log.warn("CreatableV3 resource is using the path '" + path + "' of the adapted (aka 'template') resource, hence you might want to overwrite it with Resource.setPath(String) within the create(ResourceInput) method!");
             ResourceInput input = (ResourceInput) cs.getModel();
             if(input.validate()){
                 if(log.isDebugEnabled()) {
                     log.debug("Start create " + dateFormat.format(new java.util.Date()));
                 }
                 cv3.create(input);
 /* DEBUG
                 if (input.getItem("id") != null) {
                     log.warn("DEBUG: ID: " + (String) input.getItem("id").getValue());
                 }
 */
                 initConversation(input, usecase, path); // TODO: Is this necessary, or is this done by some superclass?
                 if(log.isDebugEnabled()) {
                     log.debug("End create " + dateFormat.format(new java.util.Date()));
                 }
             } else {
                 throw new IllegalArgumentException("The input for the adapted resource is not valid");
             }
         } else if (Usecase.modify.equals(usecase)) {
             ModifiableV3 mv3 = getAdaptedResourceAsModifiableV3(path);
             ResourceInput input = (ResourceInput) cs.getModel();
             if(input.validate()){
                 mv3.modify(input);
            }else{
                 throw new IllegalArgumentException("The input for the adapted resource is not valid");
             }
         } else if (Usecase.remove.equals(usecase)) {
             DeletableV1 dv1 = getAdaptedResourceAsDeletableV1(path);
             ResourceInput input = (ResourceInput) cs.getModel();
             if(input.validate()){
                 dv1.delete(input);
             }else{
                 throw new IllegalArgumentException("The input for the adapted resource is not valid");
             }
         }
         if( log.isDebugEnabled() )
             log.debug("End commit " + dateFormat.format(new java.util.Date()));
     }
 
     /**
      * If the conversation/continuation has been canceled, then react accordingly
      */
     private void doCancel() throws Exception {
         String adaptedResourcePath = getAdaptedResourcePathFromConversationState();
         log.debug("Adapted resource: " +  adaptedResourcePath);
         Resource adaptedResource = getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), adaptedResourcePath);
         if (ResourceAttributeHelper.hasAttributeImplemented(adaptedResource, "Viewable", "2") && ResourceAttributeHelper.hasAttributeImplemented(adaptedResource, "Versionable", "2")) {
             if (((org.wyona.yanel.core.api.attributes.ViewableV2) adaptedResource).exists()) {
                ((org.wyona.yanel.core.api.attributes.VersionableV2) adaptedResource).cancelCheckout();
             } else {
                 log.warn("Resource '" + adaptedResourcePath + "' does not exist!");
             }
         } else {
             log.warn("Resource '" + adaptedResourcePath + "' is not ViewableV2/VersionableV2 and hence checkout cannot be canceled!");
         }
     }
 
     /**
      * Get adapted resource path. It is either in the request or in the conversation.
      */
     private String getAdaptedResourcePathFromConversationState() {
         String adaptedResourcePath = getAdaptedResourcePath();
         ConversationState cs = getConversationState();
         if(adaptedResourcePath == null && cs != null) {
             adaptedResourcePath = cs.getResourcePath();
         }
         return adaptedResourcePath;
     }
 
     /**
      * Check if resource has the interface CreatableV3 implemented
      */
     private CreatableV3 getAdaptedResourceAsCreatableV3(String path) throws Exception {
         Resource resource = getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), path);
         if (!ResourceAttributeHelper.hasAttributeImplemented(resource, "Creatable", "3")) {
             throw new Exception("The adapted resource (" + resource.getResourceTypeUniversalName() + ", " + path + ") is not of the type '" + CreatableV3.class.getName() + "'");
         }
         return (CreatableV3) resource;
     }
     
     private ModifiableV3 getAdaptedResourceAsModifiableV3(String path) throws Exception {
         Resource resource = getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), path);
         if (!ResourceAttributeHelper.hasAttributeImplemented(resource, "Modifiable", "3")) {
             throw new Exception("The adapted resource is not of the type " + ModifiableV3.class.getName());
         }
 		return (ModifiableV3) resource;
     }
     
     private DeletableV1 getAdaptedResourceAsDeletableV1(String path) throws Exception {
         Resource resource = getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), path);
         if (!ResourceAttributeHelper.hasAttributeImplemented(resource, "Deletable", "1")) {
             throw new Exception("The adapted resource is not of the type " + DeletableV1.class.getName());
         }
 		return (DeletableV1) resource;
     }
 
     /**
      * @see
      */
     @Override
     protected void passParameters(Transformer transformer) throws Exception {
         super.passParameters(transformer);
         ConversationState cs = getConversationState();
         //log.debug("Conversation state: " + cs);
         if (cs != null) {
             transformer.setParameter("resourceInput", cs.getModel());
             String refererUrl = cs.getRefererUrl();
             if (refererUrl != null) {
                 transformer.setParameter("url.before.conversation", refererUrl);
             } else {
                 log.warn("No referer URL, hence cancel/redirect will not work. Maybe link has been copied/pasted from email");
             }
         } else {
             log.warn("The conversation was not initialized");
         }
     }
 
     /**
      * @see
      */
     @Override
     protected void passParameters(JellyContext jellyContext) throws Exception {
         super.passParameters(jellyContext);
         ConversationState cs = getConversationState();
         //log.debug("Conversation state: " + cs);
         if (cs != null) {
             jellyContext.setVariable("resourceInput", cs.getModel());
             String refererUrl = cs.getRefererUrl();
             if (refererUrl != null) {
                 jellyContext.setVariable("url.before.conversation", cs.getRefererUrl());
             } else {
                 log.warn("No referer URL, hence cancel/redirect will not work. Maybe link has been copied/pasted from email");
             }
         } else {
             log.warn("The conversation was not initialized");
         }
     }
 }
