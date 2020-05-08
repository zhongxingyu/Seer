 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.samples.showcase.example.ace.selectmenu;
 
 import org.icefaces.samples.showcase.metadata.annotation.*;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
 import java.io.Serializable;
 
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 import java.util.*;
 
 @ComponentExample(
         title = "example.ace.selectmenu.title",
         description = "example.ace.selectmenu.description",
         example = "/resources/examples/ace/selectmenu/selectMenuOverview.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="selectMenuOverview.xhtml",
                     resource = "/resources/examples/ace/selectmenu/selectMenuOverview.xhtml"),
             // Java Source
             @ExampleResource(type = ResourceType.java,
                     title="SelectMenuBean.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/showcase"+
                     "/example/ace/selectmenu/SelectMenuBean.java")
         }
 )
 @Menu(
             title = "menu.ace.selectmenu.subMenu.title",
             menuLinks = {
                 @MenuLink(title = "menu.ace.selectmenu.subMenu.main", isDefault = true, exampleBeanName = SelectMenuBean.BEAN_NAME),
 				@MenuLink(title = "menu.ace.selectmenu.subMenu.facet", exampleBeanName = SelectMenuFacetBean.BEAN_NAME),
 				@MenuLink(title = "menu.ace.selectmenu.subMenu.label", exampleBeanName = SelectMenuLabelBean.BEAN_NAME),
 				@MenuLink(title = "menu.ace.selectmenu.subMenu.indicator", exampleBeanName = SelectMenuIndicatorBean.BEAN_NAME),
 				@MenuLink(title = "menu.ace.selectmenu.subMenu.reqStyle", exampleBeanName = SelectMenuReqStyleBean.BEAN_NAME)
             }
 )
 @ManagedBean(name= SelectMenuBean.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class SelectMenuBean extends ComponentExampleImpl< SelectMenuBean > implements Serializable {
 
     public static final String BEAN_NAME = "selectMenuBean";
 	
 	private List<SelectItem> britishColumbiaCities;
 	private List<SelectItem> albertaCities;
 	private List<SelectItem> saskatchewanCities;
 	private List<SelectItem> manitobaCities;
 	private List<SelectItem> ontarioCities;
 	private List<SelectItem> quebecCities;
 	private List<SelectItem> newfoundlandCities;
 	private List<SelectItem> princeEdwardIslandCities;
 	private List<SelectItem> newBrunswickCities;
 	private List<SelectItem> novaScotiaCities;
 	private List<SelectItem> yukonCities;
 	private List<SelectItem> northwestTerritoriesCities;
 	private List<SelectItem> nunavutCities;
 	private Map<String, List<SelectItem>> provinceCitiesMap;
     
     public SelectMenuBean() {
         super(SelectMenuBean.class);
 		
 		britishColumbiaCities = new ArrayList<SelectItem>();
 		britishColumbiaCities.add(new SelectItem("Kelowna"));
 		britishColumbiaCities.add(new SelectItem("Tofino"));
 		britishColumbiaCities.add(new SelectItem("Vancouver"));
 		britishColumbiaCities.add(new SelectItem("Victoria"));
 		britishColumbiaCities.add(new SelectItem("Whistler"));
 		albertaCities = new ArrayList<SelectItem>();
 		albertaCities.add(new SelectItem("Banff"));
 		albertaCities.add(new SelectItem("Calgary"));
 		albertaCities.add(new SelectItem("Edmonton"));
 		albertaCities.add(new SelectItem("Jasper"));
 		saskatchewanCities = new ArrayList<SelectItem>();
 		saskatchewanCities.add(new SelectItem("Regina"));
 		saskatchewanCities.add(new SelectItem("Saskatoon"));
 		manitobaCities = new ArrayList<SelectItem>();
 		manitobaCities.add(new SelectItem("Winnipeg"));
 		manitobaCities.add(new SelectItem("Churchill"));
 		ontarioCities = new ArrayList<SelectItem>();
 		ontarioCities.add(new SelectItem("Niagara Falls"));
 		ontarioCities.add(new SelectItem("Niagara-on-the-Lake"));
 		ontarioCities.add(new SelectItem("London"));
 		ontarioCities.add(new SelectItem("Ottawa"));
 		ontarioCities.add(new SelectItem("St. Catharines"));
 		ontarioCities.add(new SelectItem("Stratford"));
 		ontarioCities.add(new SelectItem("Toronto"));
 		quebecCities = new ArrayList<SelectItem>();
 		quebecCities.add(new SelectItem("Montreal"));
 		quebecCities.add(new SelectItem("Quebec City"));
 		quebecCities.add(new SelectItem("Sherbrooke"));
 		quebecCities.add(new SelectItem("Trois Rivires"));
 		newfoundlandCities = new ArrayList<SelectItem>();
 		newfoundlandCities.add(new SelectItem("Corner Brook"));
 		newfoundlandCities.add(new SelectItem("St. John's"));
 		princeEdwardIslandCities = new ArrayList<SelectItem>();
 		princeEdwardIslandCities.add(new SelectItem("Cavendish"));
 		princeEdwardIslandCities.add(new SelectItem("Charlottetown"));
 		newBrunswickCities = new ArrayList<SelectItem>();
 		newBrunswickCities.add(new SelectItem("Fredericton"));
 		newBrunswickCities.add(new SelectItem("Saint John"));
 		novaScotiaCities = new ArrayList<SelectItem>();
 		novaScotiaCities.add(new SelectItem("Cape Breton"));
 		novaScotiaCities.add(new SelectItem("Halifax"));
 		yukonCities = new ArrayList<SelectItem>();
 		yukonCities.add(new SelectItem("Whitehorse"));
 		northwestTerritoriesCities = new ArrayList<SelectItem>();
 		northwestTerritoriesCities.add(new SelectItem("Yellowknife"));
 		nunavutCities = new ArrayList<SelectItem>();
 		nunavutCities.add(new SelectItem("Iqaluit"));
 		
 		provinceCitiesMap = new HashMap<String, List<SelectItem>>();
 		provinceCitiesMap.put("British Columbia", britishColumbiaCities);
 		provinceCitiesMap.put("Alberta", albertaCities);
 		provinceCitiesMap.put("Saskatchewan", saskatchewanCities);
 		provinceCitiesMap.put("Manitoba", manitobaCities);
 		provinceCitiesMap.put("Ontario", ontarioCities);
 		provinceCitiesMap.put("Quebec", quebecCities);
 		provinceCitiesMap.put("Newfoundland and Labrador", newfoundlandCities);
 		provinceCitiesMap.put("Prince Edward Island", princeEdwardIslandCities);
 		provinceCitiesMap.put("New Brunswick", newBrunswickCities);
 		provinceCitiesMap.put("Nova Scotia", novaScotiaCities);
 		provinceCitiesMap.put("Yukon", yukonCities);
 		provinceCitiesMap.put("Northwest Territories", northwestTerritoriesCities);
 		provinceCitiesMap.put("Nunavut", nunavutCities);
     }
 
     @PostConstruct
     public void initMetaData() {
         super.initMetaData();
     }
 
 	private String province = "";
     public String getProvince() { return province; }
     public void setProvince(String province) { this.province = province; }
 	
 	private String city = "";
     public String getCity() { return city; }
     public void setCity(String city) { this.city = city; }
 	
 	private List<SelectItem> cities = null;
     public List<SelectItem> getCities() { return cities; }
     public void setCities(List<SelectItem> cities) { this.cities = cities; }
 	
 	private boolean displayCities = false;
     public boolean getDisplayCities() { return displayCities; }
     public void setDisplayCities(boolean displayCities) { this.displayCities = displayCities; }
 	
 	public void provinceChange(ValueChangeEvent event) {
 		Object value = event.getNewValue();
 		if (value != null) {
 			List<SelectItem> cities = provinceCitiesMap.get(value.toString());
 			if (cities != null) {
 				setCities(cities);
 				setDisplayCities(true);
 				setCity(null);
 				return;
 			}
 		}
 		setDisplayCities(false);
 	}
 	
 	public void clearValues() {
 		this.province = "";
 		this.city = "";
 	}
 }
