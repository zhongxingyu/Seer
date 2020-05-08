 package org.multiverse.stms.alpha.transactions.readonly;
 
 import org.multiverse.api.exceptions.SpeculativeConfigurationFailure;
 import org.multiverse.api.latches.Latch;
 import org.multiverse.stms.alpha.AlphaTranlocal;
 import org.multiverse.stms.alpha.AlphaTransactionalObject;
 
 /**
  * A tracking readonly transaction that is optimized for reading/tracking a single transactional object.
  *
  * @author Peter Veentjer.
  */
public final class MonoReadonlyAlphaTransaction extends AbstractReadonlyAlphaTransaction {
 
     private AlphaTranlocal attached;
 
     public MonoReadonlyAlphaTransaction(ReadonlyConfiguration config) {
         super(config);
         init();
     }
 
     @Override
     protected void doClear() {
         attached = null;
     }
 
     @Override
     protected AlphaTranlocal findAttached(AlphaTransactionalObject txObject) {
         if (attached == null) {
             return null;
         }
 
         if (txObject != attached.getTransactionalObject()) {
             return null;
         }
 
         return attached;
     }
 
     @Override
     protected void attach(AlphaTranlocal tranlocal) {
         if (attached != null) {
             config.speculativeConfiguration.signalSpeculativeSizeFailure(1);
             throw SpeculativeConfigurationFailure.create();
         }
 
         attached = tranlocal;
     }
 
     @Override
     protected boolean dodoRegisterRetryLatch(Latch latch, long wakeupVersion) {
         if (attached == null) {
             return false;
         }
 
         AlphaTransactionalObject txObject = attached.getTransactionalObject();
 
         switch (txObject.___registerRetryListener(latch, wakeupVersion)) {
             case registered:
             case opened:
                 return true;
             case noregistration:
                 return true;
             default:
                 throw new IllegalStateException();
 
         }
     }
 }
