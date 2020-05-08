 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.apress.projsf.weblets.faces;
 
 import com.apress.projsf.weblets.misc.WebletsJSPBean;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 /**
 * Pseudo map for accessing the weblet url format
  * from legacy jsf applications via elwelbetResolver
 * <h:outputFormat value="#{webletUrl['weblet://demo/test.js']}" />
  * 
  * @author Werner Punz
  */
 public class JSFElWebletURL implements Map {
 
     public int size() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean isEmpty() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean containsKey(Object key) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean containsValue(Object value) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public Object get(Object key) {
        if(!(key instanceof String))
             throw new UnsupportedOperationException("only String keys are allowed");
        return WebletsJSPBean.getResourceUrl((String)key);      
     }
 
     public Object put(Object key, Object value) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public Object remove(Object key) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void putAll(Map m) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void clear() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public Set keySet() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public Collection values() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public Set entrySet() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
 }
