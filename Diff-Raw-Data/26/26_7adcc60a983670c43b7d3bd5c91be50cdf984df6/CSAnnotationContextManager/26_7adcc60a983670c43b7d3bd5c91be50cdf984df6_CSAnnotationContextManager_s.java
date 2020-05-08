 package org.geworkbench.bison.annotation;
 
 import java.io.Serializable;
 import java.util.WeakHashMap;
 
 import org.apache.commons.collections15.set.ListOrderedSet;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 import org.geworkbench.bison.datastructure.properties.DSNamed;
 
 /**
  * @author John Watkinson
  * @version $Id$
  */
 public class CSAnnotationContextManager implements DSAnnotationContextManager {
 
     private static final CSAnnotationContextManager instance = new CSAnnotationContextManager();
 
     public static CSAnnotationContextManager getInstance() {
         return instance;
     }
 
     public static final String DEFAULT_CONTEXT_NAME = "Default";
 
     private WeakHashMap<DSItemList<? extends DSNamed>, ListOrderedSet<DSAnnotationContext<?>>> contextMap;
     private WeakHashMap<DSItemList<? extends DSNamed>, String> currentContextMap;
 
     private CSAnnotationContextManager() {
         contextMap = new WeakHashMap<DSItemList<? extends DSNamed>, ListOrderedSet<DSAnnotationContext<? extends DSNamed>>>();
         currentContextMap = new WeakHashMap<DSItemList<? extends DSNamed>, String>();
     }
 
     @SuppressWarnings("unchecked")
 	public <T extends DSNamed> DSAnnotationContext<T>[] getAllContexts(DSItemList<T> itemList) {
         ListOrderedSet<DSAnnotationContext<? extends DSNamed>> contexts = contextMap.get(itemList);
         if (contexts == null) {
             return new DSAnnotationContext[0];
         } else {
             return contexts.toArray(new DSAnnotationContext[0]);
         }
     }
 
     @SuppressWarnings("unchecked")
 	public <T extends DSNamed> DSAnnotationContext<T> getContext(DSItemList<T> itemList, String name) {
         ListOrderedSet<DSAnnotationContext<? extends DSNamed>> contexts = contextMap.get(itemList);
         if (contexts != null) {
             for (DSAnnotationContext<? extends DSNamed> context : contexts) {
                 if (name.equals(context.getName())) {
                     return (DSAnnotationContext<T>) context;
                 }
             }
         }
         // Create context
         return createContext(itemList, name);
     }
 
     public <T extends DSNamed> boolean hasContext(DSItemList<T> itemList, String name) {
         ListOrderedSet<DSAnnotationContext<? extends DSNamed>> contexts = contextMap.get(itemList);
         if (contexts != null) {
             for (DSAnnotationContext<? extends DSNamed> context : contexts) {
                 if (name.equals(context.getName())) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public <T extends DSNamed> DSAnnotationContext<T> createContext(DSItemList<T> itemList, String name) {
         CSAnnotationContext<T> context = new CSAnnotationContext<T>(name, itemList);
         ListOrderedSet<DSAnnotationContext<? extends DSNamed>> contexts = contextMap.get(itemList);
         if (contexts == null) {
             contexts = new ListOrderedSet<DSAnnotationContext<? extends DSNamed>>();
             contextMap.put(itemList, contexts);
         }
         contexts.add(context);
         return context;
     }
 
     public <T extends DSNamed> int getNumberOfContexts(DSItemList<T> itemList) {
         ListOrderedSet<DSAnnotationContext<? extends DSNamed>> contexts = contextMap.get(itemList);
         if (contexts == null) {
             return 0;
         } else {
             return contexts.size();
         }
     }
 
     @SuppressWarnings("unchecked")
 	public <T extends DSNamed> DSAnnotationContext<T> getContext(DSItemList<T> itemList, int index) {
         ListOrderedSet<DSAnnotationContext<? extends DSNamed>> contexts = contextMap.get(itemList);
         if (contexts == null) {
             throw new ArrayIndexOutOfBoundsException("Attempt to index an empty context.");
         } else {
             return (DSAnnotationContext<T>) contexts.get(index);
         }
     }
 
     public <T extends DSNamed> boolean removeContext(DSItemList<T> itemList, String name) {
         ListOrderedSet<DSAnnotationContext<? extends DSNamed>> contexts = contextMap.get(itemList);
         if (contexts == null) {
             return false;
         } else {
             DSAnnotationContext<T> context = getContext(itemList, name);
             if (context == null) {
                 return false;
             } else {
                 return contexts.remove(context);
             }
         }
     }
 
     public <T extends DSNamed> boolean renameContext(DSItemList<T> itemList, String oldName, String newName) {
         DSAnnotationContext<T> context = getContext(itemList, oldName);
         if (hasContext(itemList, newName)) {
             throw new IllegalArgumentException("Context with name '" + newName + "' already exists.");
         }
         if (context == null) {
             return false;
         } else {
             context.setName(newName);
             return true;
         }
     }
 
     @SuppressWarnings("unchecked")
 	public <T extends DSNamed> DSAnnotationContext<T> getCurrentContext(DSItemList<T> itemList) {
         ListOrderedSet<DSAnnotationContext<? extends DSNamed>> contexts = contextMap.get(itemList);
         if (contexts == null) {
             contexts = new ListOrderedSet<DSAnnotationContext<? extends DSNamed>>();
             contextMap.put(itemList, contexts);
         }
         if (contexts.isEmpty()) {
             // Create a default context
             CSAnnotationContext<T> context = new CSAnnotationContext<T>(DEFAULT_CONTEXT_NAME, itemList);
             contexts.add(context);
         }
         String currentContext = currentContextMap.get(itemList);
         DSAnnotationContext<T> context = null;
         if (currentContext != null) {
             context = getContext(itemList, currentContext);
         }
         if (context == null) {
             context = (DSAnnotationContext<T>) contexts.get(0);
             currentContextMap.put(itemList, context.getName());
         }
         return context;
     }
 
     public <T extends DSNamed> void setCurrentContext(DSItemList<T> itemList, DSAnnotationContext<T> context) {
         currentContextMap.put(itemList, context.getName());
     }
 
    public <T extends DSNamed> void copyContexts(DSItemList<T> from, DSItemList<T> to) {
        DSAnnotationContext<T>[] contexts = getAllContexts(from);
        ListOrderedSet<DSAnnotationContext<?>> contextSet = contextMap.get(to);
        if (contextSet == null) {
            contextSet = new ListOrderedSet<DSAnnotationContext<? extends DSNamed>>();
            contextMap.put(to, contextSet);
        }
        for (int i = 0; i < contexts.length; i++) {
            DSAnnotationContext<T> context = contexts[i];
            contextSet.add(context.clone());
        }
    }

     public static class SerializableContexts implements Serializable {
         /**
 		 * 
 		 */
 		private static final long serialVersionUID = -8930177139134699811L;
 		private ListOrderedSet<DSAnnotationContext<? extends DSNamed>> contexts;
         private String current;
 
         private SerializableContexts(ListOrderedSet<DSAnnotationContext<? extends DSNamed>> contexts, String current) {
             this.contexts = contexts;
             this.current = current;
         }
     }
 
     public <T extends DSNamed> SerializableContexts getContextsForSerialization(DSItemList<T> itemList) {
         return new SerializableContexts(contextMap.get(itemList), currentContextMap.get(itemList));
     }
 
     public <T extends DSNamed> void setContextsFromSerializedObject(DSItemList<T> itemList, SerializableContexts contexts) {
         contextMap.put(itemList, contexts.contexts);
         currentContextMap.put(itemList, contexts.current);
     }
 }
