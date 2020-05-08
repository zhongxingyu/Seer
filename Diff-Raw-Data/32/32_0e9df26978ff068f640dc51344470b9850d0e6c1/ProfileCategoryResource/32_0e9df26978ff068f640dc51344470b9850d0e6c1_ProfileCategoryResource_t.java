 /**
  * This file is part of AMEE.
  *
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 package gc.carbon.profile;
 
 import gc.carbon.ResourceBuilder;
 import gc.carbon.ResourceBuilderFactory;
 import gc.carbon.domain.ObjectType;
 import gc.carbon.domain.profile.Profile;
 import gc.carbon.domain.profile.ProfileItem;
 import gc.carbon.profile.acceptor.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.Context;
 import org.restlet.data.*;
 import org.restlet.resource.Representation;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @Component("profileCategoryResource")
 @Scope("prototype")
 public class ProfileCategoryResource extends BaseProfileResource {
 
     private final Log log = LogFactory.getLog(getClass());
 
     private List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
     private ResourceBuilder builder;
     private Map<MediaType, ProfileAcceptor> acceptors;
     private boolean recurse = false;
 
     @Override
     public void init(Context context, Request request, Response response) {
         super.init(context, request, response);
         profileBrowser.setDataCategoryUid(request.getAttributes().get("categoryUid").toString());
         setPage(request);
         setAcceptors();
         setBuilderStrategy();
         recurse = request.getResourceRef().getQueryAsForm().getFirstValue("recurse", "false").equals("true");
     }
 
     private void setBuilderStrategy() {
         builder = ResourceBuilderFactory.createProfileCategoryBuilder(this);
     }
 
     private void setAcceptors() {
         acceptors = new HashMap<MediaType, ProfileAcceptor>();
         acceptors.put(MediaType.APPLICATION_XML, new ProfileCategoryXMLAcceptor(this));
         acceptors.put(MediaType.APPLICATION_JSON, new ProfileCategoryJSONAcceptor(this));
         acceptors.put(MediaType.APPLICATION_WWW_FORM, new ProfileCategoryFormAcceptor(this));
         acceptors.put(MediaType.APPLICATION_ATOM_XML, new ProfileCategoryAtomAcceptor(this));
     }
 
     @Override
     public boolean isValid() {
         return super.isValid() &&
                 (profileBrowser.getDataCategory() != null) &&
                 (pathItem.getObjectType().equals(ObjectType.DC));
     }
 
     @Override
     public String getTemplatePath() {
         return ProfileConstants.VIEW_PROFILE_CATEGORY;
     }
 
     @Override
     public Map<String, Object> getTemplateValues() {
         Map<String, Object> templateValues = builder.getTemplateValues();
         templateValues.putAll(super.getTemplateValues());
         return templateValues;
     }
 
     @Override
     public org.apache.abdera.model.Element getAtomElement() {
         return builder.getAtomElement();
     }
 
     @Override
     public JSONObject getJSONObject() throws JSONException {
         JSONObject obj = builder.getJSONObject();
         obj.put("actions", getActions(profileBrowser.getProfileActions()));
         obj.put("profileItemActions", getActions(profileBrowser.getProfileItemActions()));
         return obj;
     }
 
     @Override
     public Element getElement(Document document) {
         return builder.getElement(document);
     }
 
     @Override
     public void handleGet() {
         log.debug("handleGet()");
         if (profileBrowser.getProfileCategoryActions().isAllowView()) {
             if (!validateParameters()) {
                 return;
             }
             Form form = getRequest().getResourceRef().getQueryAsForm();
             if (getApiVersion().isVersionOne()) {
                 profileBrowser.setProfileDate(form.getFirstValue("profileDate"));
             } else {
                 String startDate = form.getFirstValue("startDate");
                 if (startDate != null) {
                     profileBrowser.setStartDate(form.getFirstValue("startDate"));
                 }
                 profileBrowser.setEndDate(form.getFirstValue("endDate"));
                 profileBrowser.setDuration(form.getFirstValue("duration"));
                profileBrowser.setSelectBy(form.getFirstValue("selectby"));
                 profileBrowser.setMode(form.getFirstValue("mode"));
                 profileBrowser.setAmountReturnUnit(form.getFirstValue("returnUnit"), form.getFirstValue("returnPerUnit"));
             }
 
             super.handleGet();
         } else {
             notAuthorized();
         }
     }
 
     @Override
     public boolean allowPost() {
         return true;
     }
 
     @Override
     public boolean allowPut() {
         return true;
     }
 
     @Override
     public void acceptRepresentation(Representation entity) {
         log.debug("acceptRepresentation()");
         acceptOrStore(entity);
     }
 
     @Override
     public void storeRepresentation(Representation entity) {
         log.debug("storeRepresentation()");
         acceptOrStore(entity);
     }
 
     protected void acceptOrStore(Representation entity) {
         log.debug("acceptOrStore()");
         if (isAcceptOrStoreAuthorized()) {
             profileItems = doAcceptOrStore(entity);
             if (!profileItems.isEmpty()) {
                 // clear caches
                 profileService.clearCaches(profileBrowser);
                 if (isStandardWebBrowser()) {
                     success(profileBrowser.getFullPath());
                 } else {
                     // return a response for API calls
                     super.handleGet();
                 }
             }
         } else {
             notAuthorized();
         }
     }
 
     protected boolean isAcceptOrStoreAuthorized() {
         return (getRequest().getMethod().equals(Method.POST) && (profileBrowser.getProfileItemActions().isAllowCreate())) ||
                 (getRequest().getMethod().equals(Method.PUT) && (profileBrowser.getProfileItemActions().isAllowModify()));
     }
 
     public List<ProfileItem> doAcceptOrStore(Representation entity) {
        profileBrowser.setAmountReturnUnit(getForm().getFirstValue("returnUnit"), getForm().getFirstValue("returnPerUnit"));
         return getAcceptor(entity.getMediaType()).accept(entity);
     }
 
     public ProfileAcceptor getAcceptor(MediaType type) {
         if (MediaType.APPLICATION_JSON.includes(type)) {
             return acceptors.get(MediaType.APPLICATION_JSON);
         } else if (MediaType.APPLICATION_XML.includes(type)) {
             return acceptors.get(MediaType.APPLICATION_XML);
         } else if (MediaType.APPLICATION_ATOM_XML.includes(type)) {
             return acceptors.get(MediaType.APPLICATION_ATOM_XML);
         } else {
             return acceptors.get(MediaType.APPLICATION_WWW_FORM);
         }
     }
 
     @Override
     public boolean allowDelete() {
         // only allow delete for profile (a request to /profiles/{profileUid})
         return (pathItem.getPath().length() == 0);
     }
 
     @Override
     public void removeRepresentations() {
         log.debug("removeRepresentations()");
         if (profileBrowser.getProfileActions().isAllowDelete()) {
             Profile profile = profileBrowser.getProfile();
             profileService.clearCaches(profileBrowser);
             profileService.remove(profile);
             success("/profiles");
         } else {
             notAuthorized();
         }
     }
 
     public List<ProfileItem> getProfileItems() {
         return profileItems;
     }
 
     public ProfileItem getProfileItem() {
         return profileBrowser.getProfileItem();
     }
 
     public boolean isRecurse() {
         return recurse;
     }
 }
