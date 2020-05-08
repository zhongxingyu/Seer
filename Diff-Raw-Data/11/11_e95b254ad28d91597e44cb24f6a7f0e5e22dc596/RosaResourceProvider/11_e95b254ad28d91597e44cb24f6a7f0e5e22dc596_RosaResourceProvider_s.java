 package org.gatherdata.app.sling.resource.rosa.internal;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.sling.api.resource.NonExistingResource;
 import org.apache.sling.api.resource.Resource;
 import org.apache.sling.api.resource.ResourceProvider;
 import org.apache.sling.api.resource.ResourceResolver;
 import org.gatherdata.app.sling.resource.core.SyntheticMapResource;
 import org.gatherdata.archiver.core.model.GatherArchive;
 import org.gatherdata.archiver.core.spi.ArchiverService;
 import org.gatherdata.forms.core.model.FormTemplate;
 import org.gatherdata.forms.core.model.FormTemplateStyle;
 import org.gatherdata.forms.core.model.impl.MutableFormTemplate;
 import org.gatherdata.forms.core.spi.FormCatalogService;
 
 import com.google.inject.Inject;
 
 public class RosaResourceProvider implements ResourceProvider {
 
     private static Log log = LogFactory.getLog(RosaResourceProvider.class);
 
     public static final String PROVIDER_ROOT = "/gather/rosa/";
 
     public static final String[] RESOURCE_TYPES = new String[] { RosaFormResource.RESOURCE_TYPE };
 
     /**
      * URL pattern for individual form templates: PROVIDER_ROOT + /forms/[APPLICATION]/[FORM_NAME]
      * 
      */
     public static final Pattern FORM_URL_PATTERN = Pattern.compile("^" + PROVIDER_ROOT + "forms/([\\w-]*)/([\\w-&&[^/.]]*)$");
 
     /**
      * URL pattern for listing available forms: PROVIDER_ROOT + /forms
      * 
      */
     public static final Pattern FORM_LIST_URL_PATTERN = Pattern.compile("^" + PROVIDER_ROOT + "forms$");
 
     /**
      * URL pattern for submitting form instance data: PROVIDER_ROOT + /data
      */
     public static final Pattern POST_DATA_URL_PATTERN = Pattern.compile("^" + PROVIDER_ROOT + "data$");
     
     @Inject
     FormCatalogService formCatalog;
 
     SyntheticMapResource rosaListResource = null;
     SyntheticMapResource rosaDataResource = null;
 
     public Resource getResource(ResourceResolver resourceResolver, HttpServletRequest request, String path) {
         log.debug("getResource(*,*," + path + ")");
         return getResource(resourceResolver, path);
     }
 
     public Resource getResource(ResourceResolver resourceResolver, String path) {
         log.debug("getResource(*," + path + ")");
         Resource foundResource = null;
 
         Matcher formMatcher = FORM_URL_PATTERN.matcher(path);
         if (formMatcher.matches()) {
             String xformsApplication = formMatcher.group(1);
             String xformsType = formMatcher.group(2);
 
             log.debug("Possible form resource...");
             log.debug("application: " + xformsApplication);
             log.debug("type: " + xformsType);
 
             // URI = http://gatherdata.org/<xformsApplication>/<xformsType>
             URI formNamespace;
             try {
                 formNamespace = new URI("http://gatherdata.org/" + xformsApplication + "/" + xformsType);
                 foundResource = getFormResource(resourceResolver, path, formNamespace);
 
                 if (foundResource == null) {
                     foundResource = new RosaFormResource(resourceResolver, path, formNamespace);
                 }
             } catch (URISyntaxException e) {
                 log.error("Failed to create form namespace URI", e);
             }
         }
 
         if (foundResource == null) {
             Matcher listMatcher = FORM_LIST_URL_PATTERN.matcher(path);
             if (listMatcher.matches()) {
                 foundResource = getRosaFormListResource(resourceResolver, path);
             }
         }
         
         if (foundResource == null) {
             Matcher postDataMatcher = POST_DATA_URL_PATTERN.matcher(path);
             if (postDataMatcher.matches()) {
                 foundResource = getRosaDataResource(resourceResolver, path);
             }
         }
 
         log.debug("returning: " + foundResource);
         return foundResource;
     }
 
     private Resource getRosaFormListResource(ResourceResolver resourceResolver, String fullPath) {
        if (rosaListResource == null) {
             rosaListResource = new SyntheticMapResource(resourceResolver, fullPath, "rosa/list");
             rosaListResource.put("serviceClass", FormCatalogService.class.getName());
             rosaListResource.put("entityClass", FormTemplate.class.getName());
             rosaListResource.put("basePath", PROVIDER_ROOT);
        }
         return rosaListResource;
     }
 
     private Resource getRosaDataResource(ResourceResolver resourceResolver, String fullPath) {
        if (rosaDataResource == null) {
             rosaDataResource = new SyntheticMapResource(resourceResolver, fullPath, "rosa/data");
 
             rosaDataResource.put("producerClass", "org.gatherdata.camel.http.ServletProxyProducer");
             rosaDataResource.put("workflow", "xforms-workflow");
        }
         return rosaDataResource;
     }
 
     private Resource getFormResource(ResourceResolver resourceResolver, String fullPath, URI requestedUid) {
         Resource foundResource = null;
         if (requestedUid != null) {
             log.debug("looking for form template with uid: " + requestedUid.toASCIIString());
             FormTemplate requestedForm = formCatalog.get(requestedUid);
             if (requestedForm != null) {
                 if (requestedForm.getFormStyle().equals(FormTemplateStyle.ORBEON.toString())) {
                     requestedForm = transformToRosa(requestedForm);
                 }
                 foundResource = new RosaFormResource(resourceResolver, fullPath, requestedForm);
                 log.debug("found resource: " + foundResource);
             } else {
                 log.debug("form not found");
             }
         }
 
         return foundResource;
     }
 
     private FormTemplate transformToRosa(FormTemplate orbeonForm) {
         MutableFormTemplate rosaForm = new MutableFormTemplate();
         rosaForm.copy(orbeonForm);
         String rosaStyle = formCatalog.orbeonToRosa(orbeonForm.getFormTemplate());
         rosaForm.setFormTemplate(rosaStyle);
         rosaForm.setFormStyle(FormTemplateStyle.ROSA.toString());
         return rosaForm;
     }
 
     public Iterator<Resource> listChildren(Resource parent) {
         log.debug("listChildren(" + parent.getPath() + ")");
         return null;
     }
 
 }
