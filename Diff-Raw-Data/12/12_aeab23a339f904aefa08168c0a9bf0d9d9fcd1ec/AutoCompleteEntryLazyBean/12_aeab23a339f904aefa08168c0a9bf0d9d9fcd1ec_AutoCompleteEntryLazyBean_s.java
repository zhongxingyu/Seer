 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
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
 
 package org.icefaces.samples.showcase.example.ace.autocompleteentry;
 
 import org.icefaces.samples.showcase.metadata.annotation.*;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
 import java.io.Serializable;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 import javax.faces.event.ValueChangeEvent;
 
 @ComponentExample(
         parent = AutoCompleteEntryBean.BEAN_NAME,
         title = "example.ace.autocompleteentry.lazy.title",
         description = "example.ace.autocompleteentry.lazy.description",
         example = "/resources/examples/ace/autocompleteentry/autoCompleteEntryLazy.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="autoCompleteEntryLazy.xhtml",
                     resource = "/resources/examples/ace/autocompleteentry/autoCompleteEntryComplex.xhtml"),
             // Java Source
             @ExampleResource(type = ResourceType.java,
                     title="AutoCompleteEntryLazyBean.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/showcase"+
                     "/example/ace/autocompleteentry/AutoCompleteEntryLazyBean.java")
         }
 )
 @ManagedBean(name= AutoCompleteEntryLazyBean.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class AutoCompleteEntryLazyBean extends ComponentExampleImpl<AutoCompleteEntryLazyBean> implements Serializable
 {
     public static final String BEAN_NAME = "autoCompleteEntryLazyBean";
 
     public AutoCompleteEntryLazyBean() 
     {
         super(AutoCompleteEntryLazyBean.class);
     }
 	
 	private String selectedText = null; // Text the user is typing in
 	public String getSelectedText() { return selectedText; }
 	public void setSelectedText(String selectedText) { this.selectedText = selectedText; }
 	
 	public City getSelectedCity() { 
 		if (selectedText != null) {
 			return AutoCompleteEntryData.getCitiesMap().get(selectedText);
 		}
 		
 		return null; 
 	}
 	
 	public void submitText(ActionEvent event) {
 		for (City city : cities) {
 			if (city.getName().equalsIgnoreCase(selectedText)) {
 				break;
 			}
 		}
 	}
 	
 	private List<City> cities = new ArrayList<City>();
 	
 	public List<City> getCities() {
 		return cities;
 	}
 	
 	public void valueChangeEventHandler(ValueChangeEvent event) {
 		cities.clear();
 		String filter = event.getNewValue() != null ? (String) event.getNewValue() : "";
 		for (City city : AutoCompleteEntryData.getCities()) {
			if (city.getName().toLowerCase().startsWith(filter) && city.getLatitude() < 23 && city.getLatitude() > -23) {
 				cities.add(city);
 			}
 		}
 	}
 }
