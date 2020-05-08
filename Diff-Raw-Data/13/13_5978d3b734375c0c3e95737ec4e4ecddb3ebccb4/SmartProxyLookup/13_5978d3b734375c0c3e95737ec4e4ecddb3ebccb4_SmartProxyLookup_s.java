 package com.lemoulinstudio.gfa.nb.util;
 
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 import org.openide.util.Lookup;
 import org.openide.util.LookupEvent;
 import org.openide.util.LookupListener;
 
 /**
  *
  * @author vincent
  */
 public class SmartProxyLookup extends Lookup {
 
   private static class ProxyResult<T> extends Result<T> implements LookupListener {
 
     private Template<T> template;
     private Set<LookupListener> listenerSet;
     private Result<T> delegateResult;
 
     public ProxyResult(Template<T> template, Lookup delegateLookup) {
       this.template = template;
       this.delegateResult = delegateLookup.lookup(template);
     }
 
     public void setDelegateLookup(Lookup delegateLookup) {
       setDelegateResult(delegateLookup.lookup(template));
     }
 
     private void setDelegateResult(Result<T> delegateResult) {
       Collection<? extends T> contentBefore = this.delegateResult.allInstances();
       Collection<? extends T> contentAfter = delegateResult.allInstances();
 
       this.delegateResult.removeLookupListener(this);
       this.delegateResult = delegateResult;
       this.delegateResult.addLookupListener(this);
 
       if (!contentAfter.containsAll(contentBefore) ||
           !contentBefore.containsAll(contentAfter))
         fireResultChanged();
     }
 
     public void resultChanged(LookupEvent ev) {
       fireResultChanged();
     }
 
     private void fireResultChanged() {
       LookupEvent event = new LookupEvent(this);
       for (LookupListener listener : listenerSet)
         listener.resultChanged(event);
     }
 
     @Override
     public void addLookupListener(LookupListener listener) {
       if (listenerSet == null)
         listenerSet = new HashSet<LookupListener>();
       
       listenerSet.add(listener);
     }
 
     @Override
     public void removeLookupListener(LookupListener listener) {
       if (listenerSet != null)
         listenerSet.remove(listener);
     }
 
     @Override
     public Collection<? extends T> allInstances() {
       return delegateResult.allInstances();
     }
 
     @Override
     public Set<Class<? extends T>> allClasses() {
       return delegateResult.allClasses();
     }
 
     @Override
     public Collection<? extends Item<T>> allItems() {
       return delegateResult.allItems();
     }
 
   }
 
   private Lookup delegateLookup;
   private Map<Template<?>, Reference<ProxyResult<?>>> templateToProxyResult;
 
   public SmartProxyLookup() {
     delegateLookup = Lookup.EMPTY;
     templateToProxyResult = new WeakHashMap<Template<?>, Reference<ProxyResult<?>>>();
   }
 
   public Lookup getDelegateLookup() {
     return delegateLookup;
   }
 
   public void setDelegateLookup(Lookup delegateLookup) {
     this.delegateLookup = delegateLookup;
 
     for (Reference<ProxyResult<?>> proxyResultRef : templateToProxyResult.values()) {
       ProxyResult<?> proxyResult = proxyResultRef.get();
       if (proxyResult != null)
         proxyResult.setDelegateLookup(delegateLookup);
     }
   }
 
   @Override
   public <T> T lookup(Class<T> clazz) {
     return delegateLookup.lookup(clazz);
   }
 
   @Override
   public <T> Result<T> lookup(Template<T> template) {
    ProxyResult<T> proxyResult = new ProxyResult<T>(template, delegateLookup);
    templateToProxyResult.put(template, new WeakReference<ProxyResult<?>>(proxyResult));
     return proxyResult;
   }
 
 }
