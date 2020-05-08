 package com.amee.domain.sheet;
 
 import com.amee.core.APIUtils;
 import com.amee.domain.APIObject;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Choice implements Serializable, Comparable, APIObject {
 
     private String name = "";
     private String value = "";
 
     private Choice() {
         super();
     }
 
     public Choice(String value) {
         this();
         this.name = value;
         this.value = value;
     }
 
     public Choice(String name, String value) {
         this();
         this.name = name;
         this.value = value;
     }
 
     public boolean equals(Object o) {
         Choice other = (Choice) o;
         return getName().equalsIgnoreCase(other.getName());
     }
 
     public int compareTo(Object o) {
         Choice other = (Choice) o;
         return getName().compareToIgnoreCase(other.getName());
     }
 
     public int hashCode() {
         return getName().toLowerCase().hashCode();
     }
 
     public String toString() {
         return getName();
     }
 
     public static Choice parseNameAndValue(String nameAndValue) {
         Choice choice = new Choice();
         if (nameAndValue != null) {
             String[] arr = nameAndValue.trim().split("=");
             if (arr.length > 1) {
                 choice = new Choice(arr[0],arr[1]);
             } else if (arr.length > 0) {
                 choice = new Choice(arr[0]);
             }
         }
         return choice;
     }
 
     public static List<Choice> parseChoices(String c) {
         List<Choice> choices = new ArrayList<Choice>();
         if ((c != null) && !c.isEmpty()) {
             String[] arr = c.split(",");
             for (String s : arr) {
                 choices.add(Choice.parseNameAndValue(s));
             }
         }
         return choices;
     }
 
     public JSONObject getJSONObject() throws JSONException {
         JSONObject obj = new JSONObject();
         obj.put("name", getName());
         obj.put("value", getValue());
         return obj;
     }
 
     public JSONObject getJSONObject(boolean detailed) throws JSONException {
         return getJSONObject();
     }
 
     public JSONObject getIdentityJSONObject() throws JSONException {
         return new JSONObject();
     }
 
     public Element getElement(Document document) {
         Element element = document.createElement("Choice");
         element.appendChild(APIUtils.getElement(document, "Name", getName()));
         element.appendChild(APIUtils.getElement(document, "Value", getValue()));
         return element;
     }
 
     public Element getElement(Document document, boolean detailed) {
         return getElement(document);
     }
 
     public Element getIdentityElement(Document document) {
         return document.createElement("Choice");
     }
 
     public String getName() {
         return name;
     }
 
     public String getValue() {
         return value;
     }
 }
