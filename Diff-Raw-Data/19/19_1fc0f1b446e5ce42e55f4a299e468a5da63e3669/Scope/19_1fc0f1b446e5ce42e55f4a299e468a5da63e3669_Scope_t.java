 package org.babblelang.engine.impl;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 public class Scope {
     private final Scope parent;
     private final Map<String, Object> locals = new HashMap<String, Object>();
 
     public Scope() {
         this.parent = null;
     }
 
     private Scope(Scope parent) {
         this.parent = parent;
         locals.put("..", parent);
     }
 
     public Scope enter(String name) {
         Scope newScope;
         if (name != null) {
             newScope = (Scope) locals.get(name);
             if (newScope == null) {
                 newScope = new Scope(this);
                 locals.put(name, newScope);
             }
         } else {
             newScope = new Scope(this);
         }
         return newScope;
     }
 
     public Scope closure(Set<String> closureKeys) {
        // No closure for root scope
        if (parent == null) {
            return enter(null);
        }

        Scope result = parent.enter(null);
         for (String key : closureKeys) {
             if (locals.containsKey(key)) {
                 result.define(key, locals.get(key));
             }
         }
         return result;
     }
 
     public Scope leave() {
         if (parent == null) {
             throw new IllegalStateException("Too many calls to leave()");
         }
         return parent;
     }
 
     public Object define(String key, Object value) {
         if (locals.containsKey(key)) {
             throw new IllegalArgumentException("Key already defined : " + key);
         }
         locals.put(key, value);
         return value;
     }
 
     public void defineAll(Map<String, Object> values) {
         for (Map.Entry<String, Object> entry : values.entrySet()) {
             define(entry.getKey(), entry.getValue());
         }
     }
 
     public boolean isLocal(String key) {
         return locals.containsKey(key);
     }
 
     public boolean isDeclared(String key) {
         return isDeclaredWithin(null, key);
     }
 
     /**
      * Returns whether the given key has been defined in this scope
      * or up to the given root.
      */
     public boolean isDeclaredWithin(Scope root, String key) {
         Scope current = this;
         while (current != null) {
             if (current.locals.containsKey(key)) {
                 return true;
             } else {
                 if (current == root) {
                     break;
                 }
                 current = current.parent;
             }
         }
         return false;
     }
 
     public Object assign(String key, Object value) {
         Scope current = this;
         while (current != null) {
             if (current.locals.containsKey(key)) {
                 current.locals.put(key, value);
                 return value;
             } else {
                 current = current.parent;
             }
         }
         throw new IllegalArgumentException("No such key : " + key);
     }
 
     public Object get(String key) {
         Scope current = this;
         while (current != null) {
             if (current.locals.containsKey(key)) {
                 return current.locals.get(key);
             } else {
                 current = current.parent;
             }
         }
         throw new IllegalArgumentException("No such key : " + key);
     }
 
 }
