 
 package kkckkc.jsourcepad.util.action;
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.Map;
 import java.util.Set;
 
 public class ActionContext {
     private static final Object KEY = "ActionContext";
 
     static final ActionContext EMPTY_CONTEXT = new ActionContext() {
         @Override
         public <T> void put(Key<T> key, T value) {
             throw new IllegalStateException("EMPTY_CONTEXT is immutable");
         }
     };
 
     private Map<Key<?>, Object> items = Maps.newHashMap();
     private ActionContext parent;
     private Set<Listener> listeners = Sets.newHashSet();
     private JComponent component;
 
     public JComponent getComponent() {
         return component;
     }
 
     public void setComponent(JComponent component) {
         this.component = component;
     }
 
     public static ActionContext get(Container container) {
         if (container == null) {
             return EMPTY_CONTEXT;
         }
         if (! (container instanceof JComponent)) {
             return EMPTY_CONTEXT;
         }
 
         ActionContext ac = (ActionContext) ((JComponent) container).getClientProperty(KEY);
         if (ac != null) return ac;
 
         if (container instanceof JPopupMenu) {
             return get((JComponent) ((JPopupMenu) container).getInvoker());
         } else {
             return get(container.getParent());
         }
     }
 
     public static void set(JComponent component, ActionContext ac) {
         component.putClientProperty(KEY, ac);
        ac.setComponent(component);
     }
 
 
     public ActionContext() {
     }
 
     public ActionContext(ActionContext parent) {
         this.parent = parent;
     }
 
     public <T> T get(Key<T> key) {
         T t = (T) items.get(key);
         if (t == null && parent != null) return parent.get(key);
         return t;
     }
 
     public <T> void put(Key<T> key, T value) {
         items.put(key, value);
     }
 
     public <T> void remove(Key<T> key) {
         items.put(key, null);
     }
 
     public void commit() {
         for (Listener listener : listeners) {
             listener.actionContextUpdated(this);
         }
     }
 
     public ActionContext subContext() {
         return new ActionContext(this);
     }
 
     public void addListener(Listener listener) {
         this.listeners.add(listener);
         if (parent != null) {
             parent.addListener(listener);
         }
     }
 
     public void removeListener(Listener listener) {
         this.listeners.remove(listener);
         if (parent != null) {
             parent.removeListener(listener);
         }
     }
 
     public void update() {
         commit();
     }
 
 
     public interface Listener {
         public <T> void actionContextUpdated(ActionContext actionContext);
     }
 
     public static class Key<T> {
         public Key() {
         }
 
         public int hashCode() {
             return System.identityHashCode(this);
         }
 
         public boolean equals(Object o) {
             return o == this;
         }
     }
 }
