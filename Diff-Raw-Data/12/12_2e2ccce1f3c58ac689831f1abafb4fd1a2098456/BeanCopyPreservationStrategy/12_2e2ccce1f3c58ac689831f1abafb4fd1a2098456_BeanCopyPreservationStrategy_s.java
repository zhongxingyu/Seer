 /*
  * Created on Nov 16, 2005
  */
 package uk.org.ponder.rsf.preservation;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.springframework.beans.factory.BeanNameAware;
 
 import uk.org.ponder.beanutil.BeanLocator;
 import uk.org.ponder.beanutil.BeanModelAlterer;
 import uk.org.ponder.beanutil.IterableBeanLocator;
 import uk.org.ponder.beanutil.PathUtil;
 import uk.org.ponder.beanutil.WriteableBeanLocator;
 import uk.org.ponder.errorutil.ThreadErrorState;
 import uk.org.ponder.messageutil.TargettedMessageList;
 import uk.org.ponder.rsf.state.ExpiredFlowException;
 import uk.org.ponder.rsf.state.TokenStateHolder;
 import uk.org.ponder.stringutil.StringList;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * A preservation strategy that works by simple copying of bean object 
  * references. These should probably be "Serializable" if their destination is
  * a Session.
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 // * If the target beans are serializable, then so will be the entry sent to the
 // * tokenstateholder. If so this state may be passed to a
 // * ClientFormTokenStateHolder or other similar - for Hibernate-style
 // * non-serializable beans, they must be sent to an InMemoryTSH. But on the other
 // * hand, these beans will need a Session.lock() called on them in any case, so
 // * need to derive from the BCPS for a HibernateBCPS.
 public class BeanCopyPreservationStrategy implements TSHPreservationStrategy,
     BeanNameAware, GenericBeanCopyPreservationStrategy {
   private StringList beannames;
   private TokenStateHolder holder;
   private String basekey = "";
   private BeanModelAlterer alterer;
   private boolean expected = true;
   private boolean classLoaderInternal = true;
   
   private Set ourLoaders;
   
   public void setClassLoaderInternal(boolean classLoaderInternal) {
     this.classLoaderInternal = classLoaderInternal;
   }
 
   public BeanCopyPreservationStrategy() {
     ourLoaders = new HashSet();
     ClassLoader ours = getClass().getClassLoader();
     while (ours != null) {
       ourLoaders.add(ours);
       ours = ours.getParent();
     }
   }
 
   public void setPreservingBeans(StringList beannames) {
     this.beannames = beannames;
   }
 
   public void setTokenStateHolder(TokenStateHolder holder) {
     this.holder = holder;
   }
 
   public TokenStateHolder getTokenStateHolder() {
     return holder;
   }
   
   public void setBeanModelAlterer(BeanModelAlterer alterer) {
     this.alterer = alterer;
   }
   
   public void setStorageExpected(boolean expected) {
     this.expected = expected;
   }
   
   public void setBaseKey(String basekey) {
     this.basekey = basekey;
   }
 
   public int preserveImmediate(BeanLocator source, StringList beannames,
       String tokenid) {
     HashMap beans = new HashMap();
     for (int i = 0; i < beannames.size(); ++i) {
       String beanname = beannames.stringAt(i);
       Object bean = alterer.getBeanValue(beanname, source, null);
       // This branch generally useful for entity bean locators
       if (bean instanceof IterableBeanLocator) {
         IterableBeanLocator iterablebean = (IterableBeanLocator) bean;
         for (Iterator beanit = iterablebean.iterator(); beanit.hasNext(); ) {
           String subbeanname = (String) beanit.next();
           Object subbean = iterablebean.locateBean(subbeanname);
           String fullpath = PathUtil.buildPath(beanname, subbeanname);
           beans.put(fullpath, subbean);
         }
       }
       if (bean != null) {
         beans.put(beanname, bean);
         Logger.log.info("BeanCopy preserved to path " + beanname + ": " + bean);   
       }
     }
     String token = basekey + tokenid;
     holder.putTokenState(token, beans);
     Logger.log.info("BeanCopy saved " + beans.size() + " beans to token " + token);
     return beans.size();  
   }
   
   public int preserve(BeanLocator source, String tokenid) {
     return preserveImmediate(source, beannames, tokenid);
   }
 
   public int restore(WriteableBeanLocator target, String tokenid) {
     String token = basekey + tokenid;
     Logger.log.info("BeanCopy looking for state token " + token);
     Map beans = (Map) holder.getTokenState(token);
     if (beans == null) {
       if (expected) {
         throw UniversalRuntimeException.accumulate(new ExpiredFlowException(),
           "Client requested restoration of expired flow state with ID "
               + tokenid);
       }
       return 0;
     }
     else {
      for (Iterator keyit = beans.keySet().iterator(); keyit.hasNext();) {
        String beanname = (String) keyit.next();
        Object bean = beans.get(beanname);
         ClassLoader beanloader = bean.getClass().getClassLoader();
         if (classLoaderInternal && !ourLoaders.contains(beanloader)) {
           Logger.log.warn("Bean " + bean +" with name " + beanname + 
               " from unrecognized ClassLoader " + beanloader.getClass().getName() +"@" + System.identityHashCode(beanloader) + " was destroyed from preservation");
          beans.remove(beanname);
         }
         else {
           TargettedMessageList messages = ThreadErrorState.getErrorState().messages;
 //          Logger.log.info("Restoring bean " + bean + " to path " + beanname);
           alterer.setBeanValue(beanname, target, bean, messages, false);
         }
       }
       return beans.size();
     }
   }
 
   public void clear(String tokenid) {
     String token = basekey + tokenid;
     Logger.log.info("Cleared token state for " + token);
     holder.clearTokenState(token);
   }
 
   public void setBeanName(String name) {
     this.basekey = name;
   }
 
 
 
 }
