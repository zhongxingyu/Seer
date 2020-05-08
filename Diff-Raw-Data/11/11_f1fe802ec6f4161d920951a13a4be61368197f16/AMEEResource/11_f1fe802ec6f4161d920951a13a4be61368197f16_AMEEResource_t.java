 package com.amee.restlet;
 
 import com.amee.domain.APIVersion;
 import com.amee.domain.PagerSetType;
 import com.amee.domain.auth.User;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.environment.Environment;
 import com.amee.domain.path.PathItem;
 import com.amee.restlet.profile.builder.v2.AtomFeed;
 import com.amee.restlet.utils.HeaderUtils;
 import com.amee.restlet.utils.MediaTypeUtils;
 import com.amee.service.ThreadBeanHolder;
 import com.amee.service.auth.ResourceActions;
 import com.amee.service.data.DataService;
 import com.amee.service.environment.EnvironmentService;
 import com.amee.service.profile.ProfileService;
 import org.apache.xerces.dom.DocumentImpl;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.Context;
 import org.restlet.data.*;
 import org.restlet.resource.*;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Map;
 import java.util.List;
 import java.util.Date;
 import java.util.Calendar;
 
 /**
  * This file is part of AMEE.
  * <p/>
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 public class AMEEResource extends BaseResource implements BeanFactoryAware {
 
     private Form form;
     private int page = 1;
     private PagerSetType pagerSetType = PagerSetType.ALL;
 
     protected BeanFactory beanFactory;
     protected Environment environment;
     protected PathItem pathItem;
     protected DataCategory dataCategory;
 
     @Autowired
     protected ProfileService profileService;
 
     @Autowired
     protected DataService dataService;
 
     @Autowired
     protected EnvironmentService environmentService;
 
     // TTL for all representations is (Now - ONE_DAY)
     private static final long ONE_DAY = 1000L * 60L * 60L * 24L;
 
     @Override
     public void init(Context context, Request request, Response response) {
         super.init(context, request, response);
 
         initVariants();
 
         //TODO - Use Spring to wire the Environment
         environment = EnvironmentService.getEnvironment();
 
         //TODO - This could be retrieved as a request attribute
         pathItem = (PathItem) ThreadBeanHolder.get("pathItem");
     }
 
     private void initVariants() {
         List<Variant> variants = super.getVariants();
         if (isStandardWebBrowser()) {
             variants.add(new Variant(MediaType.TEXT_HTML));
         } else {
             variants.add(new Variant(MediaType.APPLICATION_XML));
             variants.add(new Variant(MediaType.APPLICATION_JSON));
             if (getAPIVersion().isNotVersionOne()) {
                 super.getVariants().add(0, new Variant(MediaType.APPLICATION_ATOM_XML));
             }
         }
     }
 
     @Override
     public Representation represent(Variant variant) throws ResourceException {
         if (log.isDebugEnabled())
             log.debug("represent() - method: " + getRequest().getMethod() + ", parameters: " + getForm().getMatrixString());
 
         Representation representation;
         if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
             representation = getHtmlRepresentation();
         } else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
             representation = getJsonRepresentation();
         } else if (variant.getMediaType().equals(MediaType.APPLICATION_XML)) {
             representation = getDomRepresentation();
         } else if (variant.getMediaType().equals(MediaType.APPLICATION_ATOM_XML)) {
             representation = getAtomRepresentation();
         } else {
             representation = super.represent(variant);
         }
 
         if (representation != null) {
             representation.setCharacterSet(CharacterSet.UTF_8);
             representation.setExpirationDate(new Date(Calendar.getInstance().getTimeInMillis() - ONE_DAY));
             representation.setModificationDate(Calendar.getInstance().getTime());
         }
         return representation;
     }
 
     protected Representation getDomRepresentation() throws ResourceException {
 
         // flag to ensure we only do the fetching work once
         final ThreadLocal<Boolean> fetched = new ThreadLocal<Boolean>();
 
         Representation representation;
         try {
 
             representation = new DomRepresentation(MediaType.APPLICATION_XML) {
 
                 public Document getDocument() throws IOException {
                     if ((fetched.get() == null) || !fetched.get()) {
                         Document doc = new DocumentImpl();                                                                                  
                         try {
                             fetched.set(true);
                             Element element = doc.createElement("Resources");
                             if (getAPIVersion().isNotVersionOne()) {
                                element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://schemas.amee.cc/2.0");
                             }
                             element.appendChild(getElement(doc));
                             doc.appendChild(element);
                             setDocument(doc);
                         } catch (ResourceException ex) {
                             throw new IOException(ex);
                         }
                     }
                     return super.getDocument();
                 }
             };
 
         } catch (IOException e) {
             throw new ResourceException(e);
         }
         return representation;
     }
 
     // TODO: Needs to replace getJsonRepresentation in BaseResource or be merged.
     protected Representation getJsonRepresentation() throws ResourceException {
 
         // flag to ensure we only do the fetching work once
         final ThreadLocal<Boolean> fetched = new ThreadLocal<Boolean>();
 
         // No need to use JsonRepresentation directly as it doesn't add much beyond StringRepresentation
         return new StringRepresentation(null, MediaType.APPLICATION_JSON) {
 
             @Override
             public String getText() {
                 if ((fetched.get() == null) || !fetched.get()) {
                     try {
                         fetched.set(true);
                         JSONObject obj = getJSONObject();
                         if (!getAPIVersion().isVersionOne()) {
                             obj.put("apiVersion", getAPIVersion().toString());
                         }
                         setText(obj.toString());
                     } catch (JSONException e) {
                         // swallow
                         // TODO: replace this with a bespoke Exception implemention
                         log.error("Caught JSONException: " + e.getMessage());
                         throw new RuntimeException(e);
                     } catch (ResourceException e) {
                         // swallow
                         // TODO: replace this with a bespoke Exception implemention
                         log.error("Caught ResourceException: " + e.getMessage());
                         throw new RuntimeException(e);
                     }
                 }
                 return super.getText();
             }
         };
     }
 
     // TODO: Needs to replace getAtomRepresentation in BaseResource or be merged.
     protected Representation getAtomRepresentation() throws ResourceException {
         final org.apache.abdera.model.Element atomElement = getAtomElement();
         return new WriterRepresentation(MediaType.APPLICATION_ATOM_XML) {
             public void write(Writer writer) throws IOException {
                 AtomFeed.getInstance().getWriter().writeTo(atomElement, writer);
             }
         };
     }
 
     public int getItemsPerPage() {
         int itemsPerPage = EnvironmentService.getEnvironment().getItemsPerPage();
         String itemsPerPageStr = getRequest().getResourceRef().getQueryAsForm().getFirstValue("itemsPerPage");
         if (itemsPerPageStr == null) {
             itemsPerPageStr = HeaderUtils.getHeaderFirstValue("ItemsPerPage", getRequest());
         }
         if (itemsPerPageStr != null) {
             itemsPerPage = Integer.parseInt(itemsPerPageStr);
         }
         return itemsPerPage;
     }
 
     public PathItem getPathItem() {
         return pathItem;
     }
 
     protected void setDataCategory(String dataCategoryUid) {
         if (dataCategoryUid.isEmpty()) return;
         this.dataCategory = dataService.getDataCategory(dataCategoryUid);
     }
 
     public DataCategory getDataCategory() {
         return dataCategory;
     }
 
     public Environment getEnvironment() {
         return environment;
     }
 
     //TODO - Implementing here so that subclasses are not required to. Admin client templates will be phased out in time.
     public String getTemplatePath() {
         return null;
     }
 
     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
         this.beanFactory = beanFactory;
     }
 
     @Override
     public Map<String, Object> getTemplateValues() {
         Map<String, Object> values = super.getTemplateValues();
         values.put("apiVersions", environmentService.getAPIVersions());
         return values;
     }
 
     public APIVersion getAPIVersion() {
         User user = (User) ThreadBeanHolder.get("user");
         return user.getAPIVersion();
     }
 
     public JSONObject getActions(ResourceActions rActions) throws JSONException {
         JSONObject obj = new JSONObject();
         obj.put("allowList", rActions.isAllowList());
         obj.put("allowView", rActions.isAllowView());
         obj.put("allowCreate", rActions.isAllowCreate());
         obj.put("allowModify", rActions.isAllowModify());
         obj.put("allowDelete", rActions.isAllowDelete());
         return obj;
     }
 
     /**
      * Produce the appropriate response for a successful POST or PUT.
      *
      * @param uri - for POSTS this will be the URI of the parent resource; for PUTS this will be
      *  the URI of the updated resource.
      * @param uid - the uid of the created or modified resource
      */
     public void success(String uri, String uid) {
         // For web browsers, continue with the same logic for for V1.X API. This is only to support the AMEE
         // web interface.
         if (MediaTypeUtils.isStandardWebBrowser(getRequest())) {
             getResponse().setStatus(Status.REDIRECTION_FOUND);
             if (uri != null) {
                 getResponse().setLocationRef(uri);
             } else {
                 getResponse().setLocationRef(getRequest().getResourceRef().getBaseRef());
             }
         } else {
             // Generate a representation for the following scenarios:
             //  (i) backwards compatibility with API V1.X.
             //  (ii) if the client has specifically requested a representation (and thus following V1.X behaviour).
             if (getAPIVersion().isVersionOne() || shouldReturnRepresentation()) {
                 super.handleGet();
             } else if (isPost()) {
                 // For POSTs in API versions >V1.X set the Location and 201 Created headers.
                 getResponse().setLocationRef(uri + "/" + uid);
                 getResponse().setStatus(Status.SUCCESS_CREATED);
             } else {
                 // For PUTs in API versions >V1.X set the 200 Accepted header.
                 getResponse().setStatus(Status.SUCCESS_ACCEPTED);
             }
         }
     }
 
     // True if the client has explicitly requested a representation to be returned following a POST or PUT request
     //TODO
     private boolean shouldReturnRepresentation() {
         return true;
         //String representationRequested = getForm().getFirstValue("returnRepresentation");
        //return StringUtils.equals(representationRequested, "true");
     }
 }
