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
 package com.amee.restlet.profile;
 
 import com.amee.core.ObjectType;
 import com.amee.domain.data.CO2AmountUnit;
 import com.amee.domain.profile.ProfileItem;
 import com.amee.restlet.profile.acceptor.ProfileCategoryAtomAcceptor;
 import com.amee.restlet.profile.acceptor.ProfileCategoryFormAcceptor;
 import com.amee.restlet.profile.acceptor.ProfileCategoryJSONAcceptor;
 import com.amee.restlet.profile.acceptor.ProfileCategoryXMLAcceptor;
 import com.amee.restlet.profile.builder.IProfileCategoryResourceBuilder;
 import com.amee.restlet.profile.builder.ProfileCategoryResourceBuilderFactory;
 import com.amee.service.profile.ProfileConstants;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.Context;
 import org.restlet.data.*;
 import org.restlet.resource.Representation;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 @Component("profileCategoryResource")
 @Scope("prototype")
 public class ProfileCategoryResource extends BaseProfileResource {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private ProfileCategoryFormAcceptor formAcceptor;
 
     @Autowired
     private ProfileCategoryAtomAcceptor atomAcceptor;
 
     @Autowired
     private ProfileCategoryXMLAcceptor xmlAcceptor;
 
     @Autowired
     private ProfileCategoryJSONAcceptor jsonAcceptor;
 
     @Autowired
     private ProfileCategoryResourceBuilderFactory builderFactory;
 
     private List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
     private IProfileCategoryResourceBuilder builder;
     private boolean recurse = false;
 
     @Override
     public void init(Context context, Request request, Response response) {
         super.init(context, request, response);
         setDataCategory(request.getAttributes().get("categoryUid").toString());
         setPage(request);
         setBuilderStrategy();
         recurse = request.getResourceRef().getQueryAsForm().getFirstValue("recurse", "false").equals("true");
         setAvailable(isValid());
     }
 
     private void setBuilderStrategy() {
         builder = builderFactory.createProfileCategoryResourceBuilder(this);
     }
 
     @Override
     public boolean isValid() {
         return super.isValid() && (getDataCategory() != null) && (pathItem.getObjectType().equals(ObjectType.DC));
     }
 
     @Override
     public String getTemplatePath() {
         return getAPIVersion() + "/" + ProfileConstants.VIEW_PROFILE_CATEGORY;
     }
 
     @Override
     public Map<String, Object> getTemplateValues() {
         Map<String, Object> templateValues = builder.getTemplateValues(this);
         templateValues.putAll(super.getTemplateValues());
         return templateValues;
     }
 
     @Override
     public org.apache.abdera.model.Element getAtomElement() {
         return builder.getAtomElement(this);
     }
 
     @Override
     public JSONObject getJSONObject() throws JSONException {
         JSONObject obj = builder.getJSONObject(this);
         obj.put("actions", getActions(profileBrowser.getProfileActions()));
         obj.put("profileItemActions", getActions(profileBrowser.getProfileItemActions()));
         return obj;
     }
 
     @Override
     public Element getElement(Document document) {
         return builder.getElement(this, document);
     }
 
     @Override
     public void handleGet() {
         log.debug("handleGet()");
         if (profileBrowser.getProfileCategoryActions().isAllowView()) {
             if (!validateParameters()) {
                 return;
             }
             Form form = getRequest().getResourceRef().getQueryAsForm();
             if (getAPIVersion().isVersionOne()) {
                 profileBrowser.setProfileDate(form.getFirstValue("profileDate"));
             } else {
                 profileBrowser.setStartDate(form.getFirstValue("startDate"));
                 profileBrowser.setEndDate(form.getFirstValue("endDate"));
                 profileBrowser.setDuration(form.getFirstValue("duration"));
                 profileBrowser.setSelectBy(form.getFirstValue("selectby"));
                 profileBrowser.setMode(form.getFirstValue("mode"));
                 String unit = form.getFirstValue("returnUnit");
                 String perUnit = form.getFirstValue("returnPerUnit");
                 profileBrowser.setCO2AmountUnit(new CO2AmountUnit(unit, perUnit));
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
                 profileService.clearCaches(getProfile());
                 if (isPost()) {
                     successfulPost(getFullPath(), getProfileItems().get(0).getUid());
                 } else {
                     successfulPut(getFullPath());
                 }            }
         } else {
             notAuthorized();
         }
     }
 
     protected boolean isAcceptOrStoreAuthorized() {
         return (getRequest().getMethod().equals(Method.POST) && (profileBrowser.getProfileItemActions().isAllowCreate())) ||
                 (getRequest().getMethod().equals(Method.PUT) && (profileBrowser.getProfileItemActions().isAllowModify()));
     }
 
     public List<ProfileItem> doAcceptOrStore(Representation entity) {
 
         // Accept the representation according to the MediaType
         MediaType type = entity.getMediaType();
         if (MediaType.APPLICATION_JSON.includes(type)) {
             return jsonAcceptor.accept(this, entity);
         } else if (MediaType.APPLICATION_XML.includes(type)) {
             return xmlAcceptor.accept(this, entity);
         } else if (MediaType.APPLICATION_ATOM_XML.includes(type)) {
             return atomAcceptor.accept(this, entity);
         } else {
             return formAcceptor.accept(this, getForm());
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
             profileService.clearCaches(getProfile());
             profileService.remove(getProfile());
             successfulDelete("/profiles");
         } else {
             notAuthorized();
         }
     }
 
     public List<ProfileItem> getProfileItems() {
         return profileItems;
     }
 
     public boolean isRecurse() {
         return recurse;
     }
 }
