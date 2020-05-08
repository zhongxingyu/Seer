 /*
  * Node.java
  *
  * Created on January 10, 2010, 7:29 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.rameses.rcp.common;
 
 import java.rmi.server.UID;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author elmo
  */
 public class Node 
 {
     private Object item;    
     private String id = "NODE" + new UID();
     private String caption;
     private String tooltip;
     private String mnemonic;
     private String icon;    
     private boolean dynamic;
     private boolean leaf;
     private boolean loaded;
     
     private List<NodeListener> listeners = new ArrayList();
     private Map properties = new HashMap(); 
     private Node.Provider provider;
     private Node parent;
             
     public Node() {
         this(null, null, null); 
     }
     
     public Node(String id) {
         this(id, null, null);
     }
     
     public Node(String id, String caption) { 
         this(id, caption, null); 
     }
     
     public Node(String id, String caption, Object item) 
     {
         this.id = resolveId(id);
         this.caption = caption;
         this.item = item;
     }  
     
     public Node(Map props) {
         if (props == null || props.isEmpty()) 
             throw new NullPointerException("props parameter is required in the Node object");
             
         properties.putAll(props);
         this.item = props; 
         this.id = resolveId(properties.remove("id")); 
         this.caption = removeString(properties, "caption");
         this.mnemonic = removeString(properties, "mnemonic");
         this.tooltip = removeString(properties, "tooltip");
         this.icon = removeString(properties, "icon");
         this.dynamic = "true".equals(removeString(properties,"dynamic"));
         
         Object value = properties.get("folder");
        if (value != null && "false".equals(value.toString())) this.leaf = true;
         
         String sleaf = removeString(properties,"leaf");
         if (sleaf != null && "true".equals(sleaf)) this.leaf = true; 
     } 
     
     // <editor-fold defaultstate="collapsed" desc=" Getters/Setters ">
     
     public String getId() { return id; } 
     public void setId(String id) { 
         this.id = (id == null? "NODE"+new UID(): id); 
     }
     
     public String getCaption() {
         return (caption == null? id: caption); 
     }
     
     public void setCaption(String caption) {
         this.caption = caption;
     }
 
     public Object getItem() { return item; }    
     public void setItem(Object item) { 
         this.item = item; 
     } 
     
     public String getMnemonic() { return mnemonic; }
     public void setMnemonic(String mnemonic) {
         this.mnemonic = mnemonic;
     }    
         
     public String getTooltip() { return tooltip; }    
     public void setTooltip(String tooltip) {
         this.tooltip = tooltip;
     }
     
     public boolean isDynamic() { return dynamic; }    
     public void setDynamic(boolean dynamic) { 
         this.dynamic = dynamic;
     }
     
     public boolean isLeaf() { return leaf; } 
     public void setLeaf(boolean leaf) {
         this.leaf = leaf;
     }
         
     public String getIcon() { return icon; }    
     public void setIcon(String icon) { this.icon = icon; }
 
     public boolean isLoaded() { return loaded; }    
     public void setLoaded(boolean loaded) { 
         this.loaded = loaded; 
     }
     
     public Map getProperties() { return properties; }
     public void setProperties(Map properties) { 
         this.properties = properties; 
     } 
     
     public Node getParent() { return parent; } 
     public void setParent(Node parent) { this.parent = parent; }
     
     public Node.Provider getProvider() { return provider; } 
     public void setProvider(Node.Provider provider) {
         this.provider = provider; 
     }
     
     public String getPropertyString(String name) {
         Object o = getProperties().get(name); 
         return (o == null? null: o.toString()); 
     }
     
     // </editor-fold>    
             
     // <editor-fold defaultstate="collapsed" desc=" helper methods ">
     
     private String resolveId(Object id) {
         return (id == null? "NODE"+new UID(): id.toString()); 
     }
     
     private String getString(Map props, String name) {
         Object value = props.get(name);
         return (value == null? null: value.toString()); 
     }
     
     private String removeString(Map props, String name) {
         Object value = props.remove(name);
         return (value == null? null: value.toString()); 
     }
     
     private int removeInt(Map props, String name) {
         try {
             return Integer.parseInt(props.get(name).toString()); 
         } catch(Throwable t) {
             return -1;
         } 
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc=" events handling ">
     
     public void addListener(NodeListener listener) 
     {
         if (listener != null && !listeners.contains(listener)) 
             listeners.add(listener);
     }
     
     public void removeListener(NodeListener listener) 
     {
         if (listener != null) listeners.remove(listener);
     }
     
     public void reload() 
     {
         for (NodeListener nl: listeners) {
             nl.reload();
         }
     }    
     
     protected void finalize() throws Throwable {
         super.finalize();        
         properties.clear();
         properties = null;
         listeners.clear();
         listeners = null; 
         item = null; 
     } 
     
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc=" proxying Provider methods ">   
     
     public int getIndex() {
         Node.Provider provider = getProvider();
         return (provider == null? -1: provider.getIndex()); 
     }
     
     public boolean hasItems() {
         Node.Provider provider = getProvider();
         return (provider == null? false: provider.hasItems()); 
     }  
     
     public void reloadItems() {
         Node.Provider provider = getProvider();
         if (provider != null) provider.reloadItems();
     }    
     
     public List<Node> getItems() {
         Node.Provider provider = getProvider();
         return (provider == null? null: provider.getItems());
     }
     
     public void select() {
         Node.Provider provider = getProvider();
         if (provider != null) provider.select(); 
     }    
     
     public Object open() {
         Node.Provider provider = getProvider();
         return (provider == null? null: provider.open());
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc=" Provider interface for additional information ">
     
     public static interface Provider 
     {
         int getIndex();
         
         boolean hasItems();
         void reloadItems();        
         List<Node> getItems();
         
         void select();
         Object open();
     } 
     
     // </editor-fold>
 }
