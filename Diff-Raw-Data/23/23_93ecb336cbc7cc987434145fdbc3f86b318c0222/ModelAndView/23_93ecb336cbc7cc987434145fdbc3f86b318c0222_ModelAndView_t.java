 package com.expressmvc;
 
 import java.util.Collections;
 import java.util.Map;
 
 import static com.google.common.collect.Maps.newHashMap;
 
 public class ModelAndView {
     private String viewName;
     private Map<String, Object> viewIngredients = newHashMap();
 
    public ModelAndView() {
    }

     public ModelAndView(String viewName) {
         this.viewName = viewName;
     }
 
     public void addViewIngredient(String name, Object ingredient) {
         viewIngredients.put(name, ingredient);
     }
 
     public final Map<String, Object> getViewIngredients() {
         return Collections.unmodifiableMap(viewIngredients);
     }
 
     public String getViewName() {
         return viewName;
     }
 
     public void setViewName(String viewName) {
         this.viewName = viewName;
     }
 }
