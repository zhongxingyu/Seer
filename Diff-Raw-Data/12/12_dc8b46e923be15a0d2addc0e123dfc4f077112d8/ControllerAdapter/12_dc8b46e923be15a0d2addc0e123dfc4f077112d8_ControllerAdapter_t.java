 package org.wyona.yanel.impl.resources;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.wyona.yanel.core.Constants;
 import org.wyona.yanel.core.Resource;
 import org.wyona.yanel.core.api.attributes.ViewableV2;
 import org.wyona.yanel.core.attributes.viewable.ViewDescriptor;
 
 abstract class ControllerAdapter extends Resource implements ResourceAdapter, ViewableV2 {
     private static Logger log = Logger.getLogger(ControllerAdapter.class);
     public static String YANEL_CONTINUATION_ID = "yanel.continuation.id";
     
     /**
      * Resource config property for internationalization
      * */
     //TODO: what is that???
     private static final String I18N_CATALOG_PROPERTY = "i18n-catalogue";
 
     protected final Set<String> supportedViewIds = new HashSet<String>();
     
     private String adaptedResourcePath = null;
     
     private Usecase usecase = null;
     
     /**
      * Constants.Request.DEFAULT_VIEW_ID is registered
      */
     public ControllerAdapter() {
         supportedViewIds.add(Constants.Request.DEFAULT_VIEW_ID);
     }
     
     /**
      * Checks if the path was set, otherwise
      * looks into available parameters and returns the value of
      * PARAM_ADAPTED_RESOURCE_PATH
      * @return null when the parameter is not found
      * */
     public final String getAdaptedResourcePath() {
         if(adaptedResourcePath == null){
             adaptedResourcePath = getParameterAsString(PARAM_ADAPTED_RESOURCE_PATH); 
         }
         return adaptedResourcePath;
     }
     
     public void setAdaptedResourcePath(String adaptedResourcePath) {
         this.adaptedResourcePath = adaptedResourcePath;
     }
     
     /**
      * Checks if the usecase was set, otherwise looks into available parameters
      * @return null when usecase can't be determined
      * */
     public final Usecase getUsecase() {
         if(usecase == null){
             String u = getParameterAsString(Constants.Request.YANEL_RESOURCE_USECASE);
             if (u == null) {
                 try {
                     u = getResourceConfigProperty("usecase");
                 } catch (Exception e) {
                     log.error(e, e);
                 }
                 if (u == null) {
                     log.error("No usecase (neither 'create' nor 'update/modify' nor 'remove/delete') has been specified (neither within conversation nor query string nor resource config)!");
                     return null;
                 }
             }
             usecase = Usecase.caseInsensitiveValueOf(u);
         }
         return usecase;
     }
     
     public void setUsecase(Usecase usecase) {
         this.usecase = usecase;
     }
     
     /**
      * Get view descriptor
      * @param viewId For example 'video'
      */
     public ViewDescriptor getViewDescriptor(String viewId) {
         ViewDescriptor[] viewDescriptors = getViewDescriptors();
         for (int i = 0; i < viewDescriptors.length; i++) {
             if (viewDescriptors[i].getId().equalsIgnoreCase(viewId)) {
                 return viewDescriptors[i];
             }
         }
         return null;
     }
     
     /**
      * Checks for validity of the viewId. For instance,
      * "CANCEL" is the same as "cancel". So this method normalizes
      * the view id.
      * <p>
      * When the client is doing some logic for some view, it first should normalize it with this method
      * 
      * @return the normalized viewId (lowercase), when the viewId is <code>null</code> it returns the default view id
      * */
     protected final String normalize(String viewId) {
         if(viewId == null || "".equals(viewId.trim())){
             return Constants.Request.DEFAULT_VIEW_ID;
         }
         for (Iterator i = supportedViewIds.iterator(); i.hasNext();) {
             String id = (String) i.next();
             if(id.equalsIgnoreCase(viewId)){
                 return id;
             }
         }
         return viewId.toLowerCase();
     }
 
     /**
      * Gets the names of the i18n message catalogues used for the i18n transformation.
      * Looks for an rc config property named 'i18n-catalogue'. Defaults to 'global'.
      * @return i18n catalogue name
      */
     protected String[] getI18NCatalogueNames() throws Exception {
         String[] catalogueNames = getResourceConfigProperties(I18N_CATALOG_PROPERTY);
         if (catalogueNames == null || catalogueNames.length == 0) {
             catalogueNames = new String[1];
             catalogueNames[0] = "global";
         }
         return catalogueNames;
     }
     
     public long getSize() throws Exception {
         return -1;
     }
     
     public boolean exists() throws Exception {
         return true;
     }
 
     /**
      * Returns the current continuation associated with the request which is associated with this resource, or if the request does not have a continuation, creates one.
      */
     public Continuation getContinuation() {
         if (getRequest().getParameter(YANEL_CONTINUATION_ID) != null) {
             String continuationId = getRequest().getParameter(YANEL_CONTINUATION_ID);
             log.warn("Return existing continuation: " + continuationId);
             return new Continuation(continuationId);
             //return new ContinuationSessionImpl(continuationId, getSession(true));
             //return new ContinuationYarepRepoImpl(continuationId, continuationRepo);
        } else {
            Continuation continuation = new Continuation();
            //Continuation continuation = new ContinuationSessionImpl(getSession(true));
            //Continuation continuation = new ContinuationYarepRepoImpl(continuationRepo);
            log.warn("New continuation created: " + continuation.getId());
            return continuation;
         }
     }
 }
