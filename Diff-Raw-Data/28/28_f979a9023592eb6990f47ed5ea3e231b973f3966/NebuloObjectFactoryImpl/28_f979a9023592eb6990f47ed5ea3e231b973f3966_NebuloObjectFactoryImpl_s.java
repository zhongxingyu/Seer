 package org.nebulostore.appcore.model;
 
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
 import org.nebulostore.appcore.addressing.AppKey;
 import org.nebulostore.appcore.addressing.NebuloAddress;
 import org.nebulostore.appcore.addressing.ObjectId;
 import org.nebulostore.appcore.exceptions.NebuloException;
 import org.nebulostore.crypto.CryptoUtils;
 
 /**
  * Factory that produces objects for NebuloStore users.
  * @author Bolek Kulbabinski
  */
 public class NebuloObjectFactoryImpl implements NebuloObjectFactory {
   private static final int TIMEOUT_SEC = 60;
 
   // Needed to inject dependencies to objects fetched from the network.
   protected Injector injector_;
 
   @Inject
   public void setInjector(Injector injector) {
     injector_ = injector;
   }
 
   public NebuloObject fetchExistingNebuloObject(NebuloAddress address) throws NebuloException {
     ObjectGetter getter = injector_.getInstance(ObjectGetter.class);
     getter.fetchObject(address, null);
     NebuloObject result = getter.awaitResult(TIMEOUT_SEC);
     injector_.injectMembers(result);
     return result;
   }
 
   public NebuloFile createNewNebuloFile() {
     // TODO(bolek): Here should come more sophisticated ID generation method to account for
     //   (probably) fixed replication groups with ID intervals. (ask Broker? what size?)
     return createNewNebuloFile(new ObjectId(CryptoUtils.getRandomId()));
   }
 
   public NebuloFile createNewNebuloFile(ObjectId objectId) {
     NebuloAddress address = new NebuloAddress(injector_.getInstance(AppKey.class), objectId);
     return createNewNebuloFile(address);
   }
 
   public NebuloFile createNewNebuloFile(NebuloAddress address) {
     NebuloFile file = new NebuloFile(address);
     injector_.injectMembers(file);
     return file;
   }
 
   public NebuloList createNewNebuloList() {
     ObjectId objectId = new ObjectId(CryptoUtils.getRandomId());
     return createNewNebuloList(objectId);
   }
 
   public NebuloList createNewNebuloList(ObjectId objectId) {
     NebuloAddress address = new NebuloAddress(injector_.getInstance(AppKey.class), objectId);
     return createNewNebuloList(address);
   }
 
   public NebuloList createNewNebuloList(NebuloAddress address) {
     NebuloList list = new NebuloList(address);
     injector_.injectMembers(list);
     return list;
   }
 }
