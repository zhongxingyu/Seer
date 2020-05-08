 package org.stringtree.http;
 
 public class NameValue {
 
    protected String name;
    protected String value;
 
     public NameValue(String name, String value) {
         this.name = name;
         this.value = value;
     }
 
     public String getName() {
         return name;
     }
 
     public String getValue() {
         return value;
     }
     
     public String toString() {
     	return name + ": " + value;
     }
 }
