 package gc.carbon.profile.builder.v2;
 
 import com.jellymold.utils.domain.APIUtils;
 import gc.carbon.ResourceBuilder;
 import gc.carbon.domain.profile.builder.v2.ProfileItemBuilder;
 import gc.carbon.domain.profile.ProfileItem;
 import gc.carbon.domain.Unit;
 import gc.carbon.profile.ProfileItemResource;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.apache.abdera.model.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
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
 public class ProfileItemResourceBuilder implements ResourceBuilder {
 
     ProfileItemResource resource;
 
     public ProfileItemResourceBuilder(ProfileItemResource resource) {
         this.resource = resource;
     }
 
     public JSONObject getJSONObject() throws JSONException {
         JSONObject obj = new JSONObject();
         ProfileItem profileItem = resource.getProfileItem();
         setBuilder(profileItem);
         obj.put("profileItem", profileItem.getJSONObject(true));
         obj.put("path", resource.getFullPath());
         obj.put("profile", resource.getProfile().getIdentityJSONObject());
         return obj;
     }
 
 
     public Element getElement(Document document) {
         ProfileItem profileItem = resource.getProfileItem();
         setBuilder(profileItem);
         Element element = document.createElement("ProfileItemResource");
         element.appendChild(profileItem.getElement(document, true));
         element.appendChild(APIUtils.getElement(document, "Path", resource.getFullPath()));
         element.appendChild(resource.getProfile().getIdentityElement(document));
         return element;
     }
 
     public Map<String, Object> getTemplateValues() {
         ProfileItem profileItem = resource.getProfileItem();
         Map<String, Object> values = new HashMap<String, Object>();
         values.put("browser", resource.getProfileBrowser());
         values.put("profile", profileItem.getProfile());
         values.put("profileItem", profileItem);
         values.put("node", profileItem);
         return values;
     }
 

     public org.apache.abdera.model.Element getAtomElement() {
 
         AtomFeed atomFeed = AtomFeed.getInstance();
 
         ProfileItem profileItem = resource.getProfileItem();
 
         Unit returnUnit = resource.getProfileBrowser().getReturnUnit();
         String amount = profileItem.getAmount(returnUnit).toString();
 
         Entry entry = atomFeed.newEntry();
         entry.setBaseUri(resource.getRequest().getAttributes().get("previousHierachicalPart").toString());
 
         Text title = atomFeed.newTitle(entry);
         title.setText(profileItem.getDisplayName());
         Text subtitle = atomFeed.newSubtitle(entry);
         subtitle.setText(atomFeed.format(profileItem.getStartDate()) + ((profileItem.getEndDate() != null) ? " - " + atomFeed.format(profileItem.getEndDate()) : ""));
 
         atomFeed.addLinks(entry, "");
 
         IRIElement eid = atomFeed.newID(entry);
         eid.setText("urn:item:" + profileItem.getUid());
 
         entry.setPublished(profileItem.getStartDate());
         entry.setUpdated(profileItem.getStartDate());
 
         HCalendar content = new HCalendar();
 
         content.addSummary(amount + " " + returnUnit.toString());
 
         content.addStartDate(profileItem.getStartDate());
         if (profileItem.getEndDate() != null) {
             content.addEndDate(profileItem.getEndDate());
         }
         entry.setContentAsHtml(content.toString());
 
         atomFeed.addStartDate(entry, profileItem.getStartDate().toString());
         if (profileItem.getEndDate() != null) {
             atomFeed.addEndDate(entry, profileItem.getEndDate().toString());
         }
 
         atomFeed.addAmount(entry, amount.toString(), returnUnit.toString());
 
         atomFeed.addItemValuesWithLinks(entry, profileItem.getItemValues(), "");
 
         Category cat = atomFeed.newItemCategory(entry);
         cat.setTerm(profileItem.getDataItem().getUid());
         cat.setLabel(profileItem.getDataItem().getItemDefinition().getName());
 
         return entry;
 
     }
     
     private void setBuilder(ProfileItem pi) {
         if (resource.getProfileBrowser().returnInExternalUnit()) {
             pi.setBuilder(new ProfileItemBuilder(pi, resource.getProfileBrowser().getReturnUnit()));
         } else {
             pi.setBuilder(new ProfileItemBuilder(pi));
         }
     }
 }
