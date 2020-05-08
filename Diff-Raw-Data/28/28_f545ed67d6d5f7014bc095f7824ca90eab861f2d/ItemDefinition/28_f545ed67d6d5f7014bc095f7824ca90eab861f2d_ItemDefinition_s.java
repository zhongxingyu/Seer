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
 package com.amee.domain.data;
 
 import com.amee.base.utils.XMLUtils;
import com.amee.domain.*;
 import com.amee.domain.algorithm.Algorithm;
 import com.amee.domain.environment.Environment;
 import com.amee.domain.sheet.Choice;
 import com.amee.platform.science.InternalValue;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowire;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.annotation.Resource;
import javax.persistence.*;
import java.util.*;
 
 @Entity
 @Table(name = "ITEM_DEFINITION")
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 @Configurable(autowire = Autowire.BY_TYPE)
 public class ItemDefinition extends AMEEEnvironmentEntity {
 
     public final static int NAME_MIN_SIZE = 3;
     public final static int NAME_MAX_SIZE = 255;
     public final static int DRILL_DOWN_MIN_SIZE = 0;
     public final static int DRILL_DOWN_MAX_SIZE = 255;
     public final static int USAGES_MIN_SIZE = 0;
     public final static int USAGES_MAX_SIZE = Metadata.VALUE_MAX_SIZE;
 
     @Transient
     @Resource
     private ILocaleService localeService;
 
     @Column(name = "NAME", length = NAME_MAX_SIZE, nullable = false)
     private String name = "";
 
     @Column(name = "DRILL_DOWN", length = DRILL_DOWN_MAX_SIZE, nullable = true)
     private String drillDown = "";
 
     @OneToMany(mappedBy = "itemDefinition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
     @OrderBy("name")
     private Set<Algorithm> algorithms = new HashSet<Algorithm>();
 
     @OneToMany(mappedBy = "itemDefinition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
     @OrderBy("name")
     private Set<ItemValueDefinition> itemValueDefinitions = new HashSet<ItemValueDefinition>();
 
     @OneToMany(mappedBy = "itemDefinition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
     private Set<ReturnValueDefinition> returnValueDefinitions = new HashSet<ReturnValueDefinition>();
 
     /**
      * A locally cached List of 'usages'.
      */
     @Transient
     private List<String> usagesList;
 
     public ItemDefinition() {
         super();
     }
 
     public ItemDefinition(Environment environment) {
         super(environment);
     }
 
     public ItemDefinition(Environment environment, String name) {
         this(environment);
         setName(name);
     }
 
     public void add(Algorithm algorithm) {
         algorithms.add(algorithm);
     }
 
     public void add(ItemValueDefinition itemValueDefinition) {
         itemValueDefinitions.add(itemValueDefinition);
     }
 
     public void add(ReturnValueDefinition returnValueDefinition) {
         returnValueDefinitions.add(returnValueDefinition);
     }
 
     public boolean hasDrillDownAvailable() {
         return getDrillDown().length() > 0;
     }
 
     public List<Choice> getDrillDownChoices() {
         return Choice.parseChoices(getDrillDown());
     }
 
     /**
      * Check if an {@link ItemValueDefinition} is included in the list of DrillDowns for this ItemDefinition.
      *
      * @param itemValueDefinition - the {@link ItemValueDefinition}
      * @return true if the name is in the DrillDown, otherwise false
      */
     public boolean isDrillDownValue(ItemValueDefinition itemValueDefinition) {
         for (Choice choice : getDrillDownChoices()) {
             if (choice.getName().equalsIgnoreCase(itemValueDefinition.getPath())) {
                 return true;
             }
         }
         return false;
     }
 
     public ItemValueDefinition getItemValueDefinition(String path) {
         for (ItemValueDefinition itemValueDefinition : getItemValueDefinitions()) {
             if (itemValueDefinition.getPath().equalsIgnoreCase(path)) {
                 return itemValueDefinition;
             }
         }
         return null;
     }
 
     public JSONObject getJSONObject() throws JSONException {
         return getJSONObject(true);
     }
 
     public JSONObject getJSONObject(boolean detailed) throws JSONException {
         JSONObject obj = new JSONObject();
         obj.put("uid", getUid());
         obj.put("name", getName());
         obj.put("drillDown", getDrillDown());
         if (detailed) {
             obj.put("created", getCreated());
             obj.put("modified", getModified());
             obj.put("environment", getEnvironment().getIdentityJSONObject());
         }
         return obj;
     }
 
     public JSONObject getIdentityJSONObject() throws JSONException {
         return XMLUtils.getIdentityJSONObject(this);
     }
 
     public Element getElement(Document document) {
         return getElement(document, true);
     }
 
     public Element getElement(Document document, boolean detailed) {
         Element element = document.createElement("ItemDefinition");
         element.setAttribute("uid", getUid());
         element.appendChild(XMLUtils.getElement(document, "Name", getName()));
         element.appendChild(XMLUtils.getElement(document, "DrillDown", getDrillDown()));
         if (detailed) {
             element.setAttribute("created", getCreated().toString());
             element.setAttribute("modified", getModified().toString());
             element.appendChild(getEnvironment().getIdentityElement(document));
         }
         return element;
     }
 
     public Element getIdentityElement(Document document) {
         return XMLUtils.getIdentityElement(document, this);
     }
 
     public String getName() {
         return localeService.getLocaleNameValue(this, name);
     }
 
     public void setName(String name) {
         if (name == null) {
             name = "";
         }
         this.name = name;
     }
 
     public String getDrillDown() {
         return drillDown;
     }
 
     public void setDrillDown(String drillDown) {
         if (drillDown == null) {
             drillDown = "";
         }
         this.drillDown = drillDown;
     }
 
     public Set<Algorithm> getAlgorithms() {
         return getActiveAlgorithms();
     }
 
     public Set<Algorithm> getActiveAlgorithms() {
         Set<Algorithm> activeAlgorithms = new HashSet<Algorithm>();
         for (Algorithm algorithm : algorithms) {
             if (!algorithm.isTrash()) {
                 activeAlgorithms.add(algorithm);
             }
         }
         return Collections.unmodifiableSet(activeAlgorithms);
     }
 
     public Set<ItemValueDefinition> getItemValueDefinitions() {
         return getActiveItemValueDefinitions();
     }
 
     public Set<ItemValueDefinition> getActiveItemValueDefinitions() {
         Set<ItemValueDefinition> activeItemValueDefinitions = new HashSet<ItemValueDefinition>();
         for (ItemValueDefinition itemValueDefinition : itemValueDefinitions) {
             if (!itemValueDefinition.isTrash()) {
                 activeItemValueDefinitions.add(itemValueDefinition);
             }
         }
         return Collections.unmodifiableSet(activeItemValueDefinitions);
     }
 
     public Set<ReturnValueDefinition> getReturnValueDefinitions() {
         return getActiveReturnValueDefinitions();
     }
 
     public Set<ReturnValueDefinition> getActiveReturnValueDefinitions() {
         Set<ReturnValueDefinition> activeReturnValueDefinitions = new HashSet<ReturnValueDefinition>();
         for (ReturnValueDefinition returnValueDefinition : returnValueDefinitions) {
             if (!returnValueDefinition.isTrash()) {
                 activeReturnValueDefinitions.add(returnValueDefinition);
             }
         }
         return Collections.unmodifiableSet(activeReturnValueDefinitions);
     }
 
     public ObjectType getObjectType() {
         return ObjectType.ID;
     }
 
     /**
      * Get the algorithm corresponding to the supplied name.
      *
      * @param name - the name of the Algorithm to retreive
      * @return the Algorithm corresponding to the supplied name
      */
     public Algorithm getAlgorithm(String name) {
         for (Algorithm algorithm : getAlgorithms()) {
             if (algorithm.getName().equalsIgnoreCase(name)) {
                 return algorithm;
             }
         }
         return null;
     }
 
     public void appendInternalValues(Map<ItemValueDefinition, InternalValue> values, APIVersion version) {
         for (ItemValueDefinition ivd : getItemValueDefinitions()) {
             if (ivd.isUsableValue() && ivd.isValidInAPIVersion(version)) {
                 values.put(ivd, new InternalValue(ivd));
             }
         }
     }
 
     /**
      * Returns the usages property as a String.
      *
      * @return String representation of the usages property
      */
     public String getUsagesString() {
         return getMetadataValue("usages");
     }
 
     /**
      * Set the usages property.
      *
      * @param usages value to set
      */
     public void setUsages(String usages) {
         getOrCreateMetadata("usages").setValue(usages);
         usagesList = null;
         onModify();
     }
 
     /**
      * Returns the usages property as a List. Modifications to the returned list are not
      * persisted and are discouraged.
      *
      * @return a List of usages
      */
     public List<String> getUsages() {
         if (usagesList == null) {
             usagesList = new ArrayList<String>();
             for (String usage : getUsagesString().split(",")) {
                 if (!usage.trim().isEmpty()) {
                     usagesList.add(usage.trim());
                 }
             }
         }
         return usagesList;
     }
 
     public List<ItemValueUsage> getItemValueUsages() {
         List<ItemValueUsage> itemValueUsages = new ArrayList<ItemValueUsage>();
         for (String usage : getUsages()) {
             itemValueUsages.add(new ItemValueUsage(usage));
         }
         return itemValueUsages;
     }
 
     public Set<ItemValueUsage> getAllItemValueUsages() {
         Set<ItemValueUsage> allItemValueUsages = new HashSet<ItemValueUsage>();
         for (ItemValueDefinition itemValueDefinition : getActiveItemValueDefinitions()) {
             allItemValueUsages.addAll(itemValueDefinition.getItemValueUsages());
         }
         return allItemValueUsages;
     }
 
     /**
      * Updates the usages property with a CSV representation of the usages argument.
      *
      * @param usages List to update the usages property with
      */
     public void setUsages(List<String> usages) {
         if ((usages == null) || usages.isEmpty()) {
             setUsages("");
         } else {
             StringBuilder sb = new StringBuilder();
             for (String usage : usages) {
                 if (!usage.trim().isEmpty()) {
                     sb.append(usage);
                     sb.append(",");
                 }
             }
             sb.deleteCharAt(sb.lastIndexOf(","));
             setUsages(sb.toString());
         }
     }
 }
