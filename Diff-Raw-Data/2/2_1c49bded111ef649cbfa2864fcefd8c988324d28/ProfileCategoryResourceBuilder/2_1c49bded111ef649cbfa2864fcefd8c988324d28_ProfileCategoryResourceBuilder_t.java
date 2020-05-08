 package gc.carbon.profile.builder.v2;
 
 import com.jellymold.utils.Pager;
 import com.jellymold.utils.domain.APIUtils;
 import gc.carbon.ResourceBuilder;
 import gc.carbon.domain.profile.ProfileItem;
 import gc.carbon.domain.profile.Profile;
 import gc.carbon.domain.profile.builder.v2.ProfileItemBuilder;
 import gc.carbon.domain.data.ItemDefinition;
 import gc.carbon.domain.data.DataCategory;
 import gc.carbon.domain.path.PathItem;
 import gc.carbon.profile.*;
 import org.apache.abdera.model.*;
 import org.apache.abdera.ext.history.FeedPagingHelper;
 import org.apache.abdera.ext.opensearch.OpenSearchExtensionFactory;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.math.BigDecimal;
 import java.util.*;
 
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
 
 public class ProfileCategoryResourceBuilder implements ResourceBuilder {
 
     private final Log log = LogFactory.getLog(getClass());
 
     private ProfileService profileService;
 
     ProfileCategoryResource resource;
 
     public ProfileCategoryResourceBuilder(ProfileCategoryResource resource) {
         this.resource = resource;
         this.profileService = resource.getProfileService();
     }
 
     public JSONObject getJSONObject() throws JSONException {
 
         JSONObject obj = new JSONObject();
 
         // add objects
         obj.put("path", resource.getFullPath());
 
         // add relevant Profile info depending on whether we are at root
         if (resource.hasParent()) {
             obj.put("profile", resource.getProfile().getIdentityJSONObject());
         } else {
             obj.put("profile", resource.getProfile().getJSONObject());
         }
 
         // add Data Category
         obj.put("dataCategory", resource.getDataCategory().getIdentityJSONObject());
 
         // add Data Categories via pathItem to children
         JSONArray dataCategories = new JSONArray();
         for (PathItem pi : resource.getChildrenByType("DC")) {
             dataCategories.put(pi.getJSONObject());
         }
         obj.put("profileCategories", dataCategories);
 
         // profile items
         List<ProfileItem> profileItems;
         Pager pager = null;
 
         if (resource.isGet()) {
             // get profile items
             profileItems = getProfileItems();
 
             // set-up pager
             pager = resource.getPager();
             profileItems = pageResults(profileItems, pager);
         } else {
             profileItems = resource.getProfileItems();
         }
 
         if (!profileItems.isEmpty()) {
             JSONArray jsonProfileItems = new JSONArray();
             obj.put("profileItems", jsonProfileItems);
             for (ProfileItem pi : profileItems) {
                setBuilder(pi);
                 jsonProfileItems.put(pi.getJSONObject(false));
             }
 
             // pager
             if (pager != null) {
                 obj.put("pager", pager.getJSONObject());
             }
 
             // add CO2 amount
             JSONObject totalAmount = new JSONObject();
             totalAmount.put("value", getTotalAmount(profileItems).toString());
             totalAmount.put("unit", resource.getProfileBrowser().getAmountUnit());
             obj.put("totalAmount", totalAmount);
 
         } else if (resource.isPost() || resource.isPut()) {
 
             if (!profileItems.isEmpty()) {
                 JSONArray profileItemsJSonn = new JSONArray();
                 obj.put("profileItems", profileItems);
                 for (ProfileItem pi : profileItems) {
                     setBuilder(pi);
                     profileItemsJSonn.put(pi.getJSONObject(false));
                 }
             }
 
         } else {
             obj.put("profileItems", new JSONObject());
             obj.put("pager", new JSONObject());
             obj.put("totalAmount", "0");
         }
 
         return obj;
     }
 
     public Element getElement(Document document) {
 
         // create element
         Element element = document.createElement("ProfileCategoryResource");
 
         element.appendChild(APIUtils.getElement(document, "Path", resource.getFullPath()));
 
         // add relevant Profile info depending on whether we are at root
         if (resource.hasParent()) {
             element.appendChild(resource.getProfile().getIdentityElement(document));
         } else {
             element.appendChild(resource.getProfile().getElement(document));
         }
 
         // add DataCategory
         element.appendChild(resource.getDataCategory().getIdentityElement(document));
 
         // add Data Categories via pathItem to children
         Element dataCategoriesElement = document.createElement("ProfileCategories");
         for (PathItem dc : resource.getChildrenByType("DC")) {
             dataCategoriesElement.appendChild(dc.getElement(document));
         }
         element.appendChild(dataCategoriesElement);
 
         // profile items
         List<ProfileItem> profileItems;
         Pager pager = null;
 
         if (resource.isGet()) {
             // get profile items
             profileItems = getProfileItems();
 
             // set-up pager
             pager = resource.getPager();
             profileItems = pageResults(profileItems, pager);
         } else {
             profileItems = resource.getProfileItems();
         }
 
         if (!profileItems.isEmpty()) {
 
             Element profileItemsElement = document.createElement("ProfileItems");
             element.appendChild(profileItemsElement);
             for (ProfileItem pi : profileItems) {
                 setBuilder(pi);
                 profileItemsElement.appendChild(pi.getElement(document, false));
             }
 
             // pager
             if (pager != null) {
                 element.appendChild(pager.getElement(document));
             }
 
             // add CO2 amount
             Element totalAmount = APIUtils.getElement(document,
                     "TotalAmount",
                     getTotalAmount(profileItems).toString());
             totalAmount.setAttribute("unit", resource.getProfileBrowser().getAmountUnit().toString());
             element.appendChild(totalAmount);
 
         }
         return element;
     }
 
     private List<ProfileItem> pageResults(List<ProfileItem> profileItems, Pager pager) {
         // set-up pager
         if (!(profileItems == null || profileItems.isEmpty())) {
             pager.setCurrentPage(resource.getPage());
             pager.setItems(profileItems.size());
             pager.goRequestedPage();
 
             // limit results
             profileItems = profileItems.subList((int) pager.getStart(), (int) pager.getTo());
 
             pager.setItemsFound(profileItems.size());
         }
         return profileItems;
     }
 
     private List<ProfileItem> getProfileItems() {
         ItemDefinition itemDefinition;
         List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
 
         // must have ItemDefinition
         itemDefinition = resource.getProfileBrowser().getDataCategory().getItemDefinition();
         if (itemDefinition != null) {
 
             ProfileService decoratedProfileServiceDAO = new OnlyActiveProfileService(profileService);
 
             if (resource.getProfileBrowser().isProRataRequest()) {
                 decoratedProfileServiceDAO = new ProRataProfileService(profileService);
             }
 
             if (resource.getProfileBrowser().isSelectByRequest()) {
                 decoratedProfileServiceDAO = new SelectByProfileService(decoratedProfileServiceDAO, resource.getProfileBrowser().getSelectBy());
             }
 
             profileItems = decoratedProfileServiceDAO.getProfileItems(resource.getProfileBrowser());
         }
         return profileItems;
     }
 
     private BigDecimal getTotalAmount(List<ProfileItem> profileItems) {
         BigDecimal totalAmount = ProfileItem.ZERO;
         BigDecimal amount;
 
         for (ProfileItem profileItem : profileItems) {
             try {
                 amount = profileItem.getAmount();
                 amount = amount.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
                 if (amount.precision() > ProfileItem.PRECISION) {
                     log.warn("getTotalAmount() - precision is too big: " + amount);
                     // TODO: do something?
                 }
             } catch (Exception e) {
                 // swallow
                 log.warn("getTotalAmount() - caught Exception: " + e);
                 amount = ProfileItem.ZERO;
             }
             totalAmount = totalAmount.add(amount);
         }
         return totalAmount;
     }
 
     private void setBuilder(ProfileItem pi) {
         if (resource.getProfileBrowser().returnAmountInExternalUnit()) {
             pi.setBuilder(new ProfileItemBuilder(pi, resource.getProfileBrowser().getAmountUnit()));
         } else {
             pi.setBuilder(new ProfileItemBuilder(pi));
         }
     }
 
     public Map<String, Object> getTemplateValues() {
 
         // profile items
         List<ProfileItem> profileItems;
         Pager pager;
 
         if (resource.isGet()) {
             // get profile items
             profileItems = getProfileItems();
 
             // set-up pager
             pager = resource.getPager();
             profileItems = pageResults(profileItems, pager);
         } else {
             profileItems = resource.getProfileItems();
         }
 
         // init builder to ensure units are represented correctly
         for (ProfileItem pi : profileItems) {
             ProfileItemBuilder builder = new ProfileItemBuilder(pi);
             pi.setConvertedAmount(builder.getAmount(pi));
         }
 
         Profile profile = resource.getProfile();
         DataCategory dataCategory = resource.getDataCategory();
         Map<String, Object> values = new HashMap<String, Object>();
         values.put("browser", resource.getProfileBrowser());
         values.put("profile", profile);
         values.put("dataCategory", dataCategory);
         values.put("node", dataCategory);
         values.put("profileItems", profileItems);
 
         if (!profileItems.isEmpty()) {
             values.put("totalAmount", getTotalAmount(profileItems));
         }
         return values;
     }
 
     public org.apache.abdera.model.Element getAtomElement() {
         if (resource.isGet()) {
             return getAtomElementForGet();
         } else {
             return getAtomElementForPost();
         }
     }
 
     private org.apache.abdera.model.Element getAtomElementForGet() {
 
         AtomFeed atomFeed = AtomFeed.getInstance();
 
         final Feed feed = atomFeed.newFeed();
         feed.setBaseUri(resource.getRequest().getAttributes().get("previousHierachicalPart").toString());
         feed.setTitle("Profile " + resource.getProfile().getDisplayName() + ", Category " + resource.getPathItem().getFullPath());
 
         atomFeed.newID(feed).setText("urn:dataCategory:" + resource.getDataCategory().getUid());
 
         atomFeed.addGenerator(feed, resource.getVersion());
 
         atomFeed.addLinks(feed, feed.getBaseUri().toString());
 
         Person author = atomFeed.newAuthor(feed);
         author.setName(resource.getProfile().getDisplayPath());
 
         Date lastModified = new Date(0);
 
         List<ProfileItem> profileItems = getProfileItems();
 
         atomFeed.addTotalAmount(feed, getTotalAmount(profileItems).toString(), resource.getProfileBrowser().getAmountUnit().toString());
 
         //TODO - Is this the correct way to use the pager?
         Pager pager = resource.getPager();
         if (profileItems.size() > pager.getItemsPerPage()) {
             profileItems = pageResults(profileItems, pager);
             FeedPagingHelper.setNext(feed, feed.getBaseUri() + "?page=" + pager.getNextPage());
             if (pager.getCurrentPage() != 1) {
                 FeedPagingHelper.setPrevious(feed, feed.getBaseUri() + "?page=" + pager.getPreviousPage());
             }
             FeedPagingHelper.setFirst(feed, feed.getBaseUri().toString());
             FeedPagingHelper.setLast(feed, feed.getBaseUri() + "?page=" + pager.getLastPage());
         }
 
         if (resource.getProfileBrowser().isQuery())  {
             feed.addExtension(OpenSearchExtensionFactory.ITEMS_PER_PAGE).setText("" + pager.getItemsPerPage());
             feed.addExtension(OpenSearchExtensionFactory.START_INDEX).setText("1");
             feed.addExtension(OpenSearchExtensionFactory.TOTAL_RESULTS).setText(""+ pager.getItems());
             org.apache.abdera.model.Element query = feed.addExtension(OpenSearchExtensionFactory.QUERY);
             query.setAttributeValue("role","request");
             query.setAttributeValue(AtomFeed.Q_NAME_START_DATE,resource.getProfileBrowser().getStartDate().toString());
             if (resource.getProfileBrowser().getEndDate() != null) {
                 query.setAttributeValue(AtomFeed.Q_NAME_END_DATE,resource.getProfileBrowser().getEndDate().toString());
             }
         }
 
         for(ProfileItem profileItem : profileItems) {
 
             Entry entry = feed.insertEntry();
 
             Text title = atomFeed.newTitle(entry);
             title.setText(profileItem.getDisplayName() + ", " + resource.getDataCategory().getDisplayName());
             Text subtitle = atomFeed.newSubtitle(entry);
             subtitle.setText(atomFeed.format(profileItem.getStartDate()) + ((profileItem.getEndDate() != null) ? " - " + atomFeed.format(profileItem.getEndDate()) : ""));
 
             atomFeed.addLinks(entry, profileItem.getUid());
 
             IRIElement eid = atomFeed.newID(entry);
             eid.setText("urn:item:" + profileItem.getUid());
 
             if (profileItem.getModified().after(lastModified))
                 lastModified = profileItem.getModified();
 
             entry.setPublished(profileItem.getCreated());
             entry.setUpdated(profileItem.getModified());
 
             atomFeed.addStartDate(entry, profileItem.getStartDate().toString());
 
             if (profileItem.getEndDate() != null) {
                 atomFeed.addEndDate(entry, profileItem.getEndDate().toString());
             }
 
             atomFeed.addAmount(entry, profileItem.getAmount().toString(), resource.getProfileBrowser().getAmountUnit().toString());
 
             HCalendar content = new HCalendar();
             content.addSummary(profileItem.getAmount() + " " + resource.getProfileBrowser().getAmountUnit().toString());
             content.addStartDate(profileItem.getStartDate());
 
             if (profileItem.getEndDate() != null) {
                 content.addEndDate(profileItem.getEndDate());
             }
 
             if (profileItem.getName() != null) {
                 atomFeed.addName(entry, profileItem.getName());
             }
 
             entry.setContentAsHtml(content.toString());
 
             Category cat = atomFeed.newItemCategory(entry);
             cat.setTerm(profileItem.getDataItem().getUid());
             cat.setLabel(profileItem.getDataItem().getItemDefinition().getName());
         }
 
         feed.setUpdated(lastModified);
 
         return feed;
     }
 
     private org.apache.abdera.model.Element getAtomElementForPost() {
         AtomFeed atomFeed = AtomFeed.getInstance();
 
         //TODO - Add batch support
         ProfileItem profileItem = resource.getProfileItems().get(0);
 
         Entry entry = atomFeed.newEntry();
         entry.setBaseUri(resource.getRequest().getAttributes().get("previousHierachicalPart").toString());
 
         Text title = atomFeed.newTitle(entry);
         title.setText(profileItem.getDisplayName() + ", " + resource.getDataCategory().getDisplayName());
         Text subtitle = atomFeed.newSubtitle(entry);
         subtitle.setText(atomFeed.format(profileItem.getStartDate()) + ((profileItem.getEndDate() != null) ? " - " + atomFeed.format(profileItem.getEndDate()) : ""));
 
         atomFeed.addLinks(entry, profileItem.getUid());
 
         IRIElement eid = atomFeed.newID(entry);
         eid.setText("urn:item:" + profileItem.getUid());
 
         entry.setPublished(profileItem.getCreated());
         entry.setUpdated(profileItem.getModified());
 
         HCalendar content = new HCalendar();
         content.addSummary(profileItem.getAmount() + " " + resource.getProfileBrowser().getAmountUnit().toString());
         content.addStartDate(profileItem.getStartDate());
         if (profileItem.getEndDate() != null) {
             content.addEndDate(profileItem.getEndDate());
         }
         entry.setContentAsHtml(content.toString());
 
         atomFeed.addStartDate(entry, profileItem.getStartDate().toString());
 
         if (profileItem.getEndDate() != null) {
             atomFeed.addEndDate(entry, profileItem.getEndDate().toString());
         }
 
         if (profileItem.getName() != null) {
             atomFeed.addName(entry, profileItem.getName());
         }
 
         atomFeed.addAmount(entry, profileItem.getAmount().toString(), resource.getProfileBrowser().getAmountUnit().toString());
 
         atomFeed.addItemValues(entry, profileItem.getItemValues());
 
         Category cat = atomFeed.newItemCategory(entry);
         cat.setTerm(profileItem.getDataItem().getUid());
         cat.setLabel(profileItem.getDataItem().getItemDefinition().getName());
 
         return entry;
     }
 }
 
