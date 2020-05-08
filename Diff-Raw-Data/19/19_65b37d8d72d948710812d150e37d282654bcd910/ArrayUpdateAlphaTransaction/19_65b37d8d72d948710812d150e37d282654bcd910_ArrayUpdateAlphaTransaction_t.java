 package org.multiverse.stms.alpha.transactions.update;
 
 import org.multiverse.api.Listeners;
 import org.multiverse.api.exceptions.PanicError;
 import org.multiverse.api.exceptions.SpeculativeConfigurationFailure;
 import org.multiverse.api.exceptions.UncommittedReadConflict;
 import org.multiverse.api.latches.Latch;
 import org.multiverse.stms.alpha.AlphaTranlocal;
 import org.multiverse.stms.alpha.AlphaTransactionalObject;
 import org.multiverse.stms.alpha.UncommittedFilter;
 import org.multiverse.stms.alpha.transactions.SpeculativeConfiguration;
 
 import static java.lang.System.arraycopy;
 
 /**
  * A {@link AbstractUpdateAlphaTransaction} where the tranlocals are stored in an array. An array
  * is faster for very small collection than a {@link MapUpdateAlphaTransaction}.
  *
  * @author Peter Veentjer
  */
public final class ArrayUpdateAlphaTransaction extends AbstractUpdateAlphaTransaction {
 
     private AlphaTranlocal[] attachedArray;
 
     private int firstFreeIndex;
 
     public ArrayUpdateAlphaTransaction(UpdateConfiguration config, int size) {
         super(config);
         attachedArray = new AlphaTranlocal[size];
         init();
     }
 
     @Override
     protected void dodoClear() {
         firstFreeIndex = 0;
         for (int k = 0; k < attachedArray.length; k++) {
             attachedArray[k] = null;
         }
     }
 
     @Override
     protected boolean isDirty() {
         boolean isDirty = false;
 
         for (int k = 0; k < firstFreeIndex; k++) {
             AlphaTranlocal attached = attachedArray[k];
             if (isDirty(attached)) {
                 isDirty = true;
             }
         }
 
         return isDirty;
     }
 
     @Override
     public AlphaTranlocal doOpenForCommutingWrite(AlphaTransactionalObject txObject) {
         updateTransactionStatus = updateTransactionStatus.upgradeToOpenForWrite();
 
 
         int indexOf = indexOf(txObject);
 
         AlphaTranlocal opened;
         if (indexOf == -1) {
             opened = txObject.___openForCommutingOperation();
             attach(opened);
         } else {
             opened = attachedArray[indexOf];
 
             if (opened.isCommitted()) {
                 //it is loaded before but it is a readonly
                 //make an updatable clone of the tranlocal already is committed and use that
                 //from now on.
                 opened = opened.openForWrite();
                 attachedArray[indexOf] = opened;
             }
         }
 
         return opened;
     }
 
     @Override
     protected AlphaTranlocal doOpenForWrite(AlphaTransactionalObject txObject) {
         updateTransactionStatus = updateTransactionStatus.upgradeToOpenForWrite();
 
 
         int indexOf = indexOf(txObject);
 
         if (indexOf == -1) {
             //it isnt opened before.
             AlphaTranlocal committed = txObject.___load(getReadVersion());
 
             AlphaTranlocal opened;
             if (committed == null) {
                 throw new UncommittedReadConflict();
             }
 
             opened = committed.openForWrite();
             attach(opened);
             return opened;
         } else {
             AlphaTranlocal attached = attachedArray[indexOf];
 
             if (attached.isCommitted()) {
                 //it is loaded before but it is a readonly
                 //make an updatable clone of the tranlocal already is committed and use that
                 //from now on.
                 attached = attached.openForWrite();
                 attachedArray[indexOf] = attached;
             } else if (attached.isCommuting()) {
                 AlphaTranlocal origin = txObject.___load(getReadVersion());
                 if (origin == null) {
                     throw new UncommittedReadConflict();
                 }
 
                 attached.fixatePremature(this, origin);
             }
             return attached;
         }
     }
 
     @Override
     protected void attach(AlphaTranlocal tranlocal) {
         if (___SANITY_CHECKS_ENABLED) {
             if (tranlocal.isUncommitted()) {
                 throw new PanicError();
             }
         }
 
         if (firstFreeIndex == attachedArray.length) {
             SpeculativeConfiguration speculativeConfig = config.speculativeConfiguration;
             speculativeConfig.signalSpeculativeSizeFailure(attachedArray.length);
 
             int newOptimalSize = speculativeConfig.getOptimalSize();
             if (newOptimalSize > speculativeConfig.getMaximumArraySize()) {
                 throw SpeculativeConfigurationFailure.create();
             }
 
             AlphaTranlocal[] newAttachedArray = new AlphaTranlocal[newOptimalSize];
             arraycopy(attachedArray, 0, newAttachedArray, 0, attachedArray.length);
             attachedArray = newAttachedArray;
         }
 
         attachedArray[firstFreeIndex] = tranlocal;
         firstFreeIndex++;
     }
 
     @Override
     protected AlphaTranlocal findAttached(AlphaTransactionalObject txObject) {
         for (int k = 0; k < firstFreeIndex; k++) {
             AlphaTranlocal attached = attachedArray[k];
 
             if (attached.getTransactionalObject() == txObject) {
                 if (k != 0) {
                     AlphaTranlocal old = attachedArray[0];
                     attachedArray[0] = attached;
                     attachedArray[k] = old;
                 }
                 return attached;
             }
         }
 
         return null;
     }
 
     private int indexOf(AlphaTransactionalObject txObject) {
         for (int k = 0; k < firstFreeIndex; k++) {
             if (attachedArray[k].getTransactionalObject() == txObject) {
                 return k;
             }
         }
 
         return -1;
     }
 
     @Override
     protected boolean hasWriteConflict() {
         for (int k = 0; k < firstFreeIndex; k++) {
             AlphaTranlocal attached = attachedArray[k];
             if (hasWriteConflict(attached)) {
                 return true;
             }
         }
 
         return false;
     }
 
     @Override
     protected boolean hasReadConflict() {
         for (int k = 0; k < firstFreeIndex; k++) {
             AlphaTranlocal attached = attachedArray[k];
             if (hasReadConflict(attached)) {
                 return true;
             }
         }
 
         return false;
     }
 
     @Override
     protected boolean dodoRegisterRetryLatch(Latch latch, long wakeupVersion) {
         boolean trackedReads = false;
 
         for (int k = 0; k < firstFreeIndex; k++) {
             AlphaTransactionalObject txObject = attachedArray[k].getTransactionalObject();
             switch (txObject.___registerRetryListener(latch, wakeupVersion)) {
                 case noregistration:
                     break;
                 case registered:
                     trackedReads = true;
                     break;
                 case opened:
                     return true;
                 default:
                     throw new IllegalStateException();
             }
         }
 
         return trackedReads;
     }
 
     @Override
     protected boolean tryWriteLocks() {
         return config.commitLockPolicy.tryAcquireAll(
                 attachedArray,
                 config.dirtyCheckEnabled ? UncommittedFilter.DIRTY_CHECK : UncommittedFilter.NO_DIRTY_CHECK,
                 this);
     }
 
     @Override
     protected void doReleaseWriteLocksForFailure() {
         for (int k = 0; k < firstFreeIndex; k++) {
             doReleaseWriteSetLocksForFailure(attachedArray[k]);
         }
     }
 
     @Override
     protected void doReleaseWriteLocksForSuccess(long writeVersion) {
         for (int k = 0; k < firstFreeIndex; k++) {
             doReleaseWriteLockForSuccess(attachedArray[k], writeVersion);
         }
     }
 
     @Override
     protected Listeners[] makeChangesPermanent(long writeVersion) {
         Listeners[] listenersArray = null;
 
         int listenersIndex = 0;
         for (int k = 0; k < firstFreeIndex; k++) {
             AlphaTranlocal attached = attachedArray[k];
 
             Listeners listeners = makePermanent(attached, writeVersion);
             if (listeners != null) {
                 if (listenersArray == null) {
                     listenersArray = new Listeners[firstFreeIndex - k];
                 }
 
                 listenersArray[listenersIndex] = listeners;
                 listenersIndex++;
             }
         }
 
         return listenersArray;
     }
 }
