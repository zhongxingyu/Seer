 package org.multiverse.stms.alpha;
 
 import org.multiverse.api.Stm;
 import org.multiverse.api.TransactionFactory;
 import org.multiverse.api.TransactionFactoryBuilder;
 import org.multiverse.stms.alpha.transactions.AlphaTransaction;
 import org.multiverse.stms.alpha.transactions.SpeculativeConfiguration;
 import org.multiverse.stms.alpha.transactions.readonly.*;
 import org.multiverse.stms.alpha.transactions.update.ArrayUpdateAlphaTransaction;
 import org.multiverse.stms.alpha.transactions.update.MapUpdateAlphaTransaction;
 import org.multiverse.stms.alpha.transactions.update.MonoUpdateAlphaTransaction;
 import org.multiverse.stms.alpha.transactions.update.UpdateAlphaTransactionConfiguration;
 import org.multiverse.utils.backoff.BackoffPolicy;
 import org.multiverse.utils.clock.PrimitiveClock;
 import org.multiverse.utils.commitlock.CommitLockPolicy;
 
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Logger;
 
 import static java.lang.String.format;
 import static org.multiverse.stms.alpha.transactions.SpeculativeConfiguration.createSpeculativeConfiguration;
 
 /**
  * Default {@link Stm} implementation that provides the most complete set of features. Like retry/orelse.
  * <p/>
  * It can be configured through the {@link AlphaStmConfig}.
  *
  * @author Peter Veentjer.
  */
 public final class AlphaStm implements Stm<AlphaStm.AlphaTransactionFactoryBuilder> {
 
     private final static Logger logger = Logger.getLogger(AlphaStm.class.getName());
 
     private final PrimitiveClock clock;
 
     private final CommitLockPolicy commitLockPolicy;
 
     private final BackoffPolicy backoffPolicy;
 
     private final AlphaTransactionFactoryBuilder transactionBuilder;
 
     private final int maxRetryCount;
 
     private final int maxArraySize;
 
     private final boolean speculativeConfigEnabled;
 
     private final boolean optimizeConflictDetectionEnabled;
 
     private final boolean dirtyCheckEnabled;
 
     public static AlphaStm createFast() {
         return new AlphaStm(AlphaStmConfig.createFastConfig());
     }
 
     public static AlphaStm createDebug() {
         return new AlphaStm(AlphaStmConfig.createDebugConfig());
     }
 
     /**
      * Creates a new AlphaStm with the AlphaStmConfig.createFast as configuration.
      */
     public AlphaStm() {
         this(AlphaStmConfig.createFastConfig());
     }
 
     /**
      * Creates a new AlphaStm with the provided configuration.
      *
      * @param config the provided config.
      * @throws NullPointerException  if config is null.
      * @throws IllegalStateException if the provided config is invalid.
      */
     public AlphaStm(AlphaStmConfig config) {
         if (config == null) {
             throw new NullPointerException();
         }
 
         config.ensureValid();
 
         this.speculativeConfigEnabled = config.speculativeConfigurationEnabled;
         this.optimizeConflictDetectionEnabled = config.optimizedConflictDetectionEnabled;
         this.dirtyCheckEnabled = config.dirtyCheckEnabled;
         this.clock = config.clock;
         this.maxArraySize = config.maxFixedUpdateSize;
         this.commitLockPolicy = config.commitLockPolicy;
         this.backoffPolicy = config.backoffPolicy;
         this.maxRetryCount = config.maxRetryCount;
         this.transactionBuilder = new AlphaTransactionFactoryBuilder();
 
         logger.info("Created a new AlphaStm instance");
     }
 
     @Override
     public AlphaTransactionFactoryBuilder getTransactionFactoryBuilder() {
         return transactionBuilder;
     }
 
     /**
      * Returns the current WriteSetLockPolicy. Returned value will never be null.
      *
      * @return the current WriteSetLockPolicy.
      */
     public CommitLockPolicy getAtomicObjectLockPolicy() {
         return commitLockPolicy;
     }
 
 
     /**
      * Returns the current BackoffPolicy. Returned value will never be null.
      *
      * @return
      */
     public BackoffPolicy getBackoffPolicy() {
         return backoffPolicy;
     }
 
     @Override
     public long getVersion() {
         return clock.getVersion();
     }
 
     public PrimitiveClock getClock() {
         return clock;
     }
 
     public class AlphaTransactionFactoryBuilder
             implements TransactionFactoryBuilder<AlphaTransaction, AlphaTransactionFactoryBuilder> {
 
         private final int maxRetryCount;
         private final boolean readonly;
         private final String familyName;
         private final boolean automaticReadTracking;
         private final boolean allowWriteSkewProblem;
         private final CommitLockPolicy commitLockPolicy;
         private final BackoffPolicy backoffPolicy;
         private final SpeculativeConfiguration speculativeConfig;
         private final boolean interruptible;
         private final boolean dirtyCheck;
 
         public AlphaTransactionFactoryBuilder() {
             this(false, //readonly
                     false,//automaticReadTracking
                     null,//familyname
                     AlphaStm.this.maxRetryCount,//maxRetryCount
                     true,//allowWriteSkewProblem
                     AlphaStm.this.commitLockPolicy,
                     AlphaStm.this.backoffPolicy,
                     createSpeculativeConfiguration(speculativeConfigEnabled, maxArraySize),
                     false,//interruptible
                     AlphaStm.this.dirtyCheckEnabled);
         }
 
         public AlphaTransactionFactoryBuilder(
                 boolean readonly, boolean automaticReadTracking, String familyName,
                 int maxRetryCount, boolean allowWriteSkewProblem,
                 CommitLockPolicy commitLockPolicy, BackoffPolicy backoffPolicy,
                 SpeculativeConfiguration speculativeConfig, boolean interruptible,
                 boolean dirtyCheck) {
             this.readonly = readonly;
             this.familyName = familyName;
             this.maxRetryCount = maxRetryCount;
             this.automaticReadTracking = automaticReadTracking;
             this.allowWriteSkewProblem = allowWriteSkewProblem;
             this.commitLockPolicy = commitLockPolicy;
             this.backoffPolicy = backoffPolicy;
             this.speculativeConfig = speculativeConfig;
             this.interruptible = interruptible;
             this.dirtyCheck = dirtyCheck;
         }
 
         @Override
         public AlphaTransactionFactoryBuilder setTimeout(long timeout, TimeUnit unit) {
             //todo: this needs to be done.            
             return this;
         }
 
         @Override
         public AlphaTransactionFactoryBuilder setFamilyName(String familyName) {
             return new AlphaTransactionFactoryBuilder(
                     readonly, automaticReadTracking, familyName, maxRetryCount, allowWriteSkewProblem,
                     commitLockPolicy, backoffPolicy, speculativeConfig, interruptible, dirtyCheck);
         }
 
         @Override
         public AlphaTransactionFactoryBuilder setMaxRetryCount(int retryCount) {
             if (retryCount < 0) {
                 throw new IllegalArgumentException(format("retryCount can't be smaller than 0, found %s", retryCount));
             }
 
             return new AlphaTransactionFactoryBuilder(
                     readonly, automaticReadTracking, familyName, retryCount, allowWriteSkewProblem,
                     commitLockPolicy, backoffPolicy, speculativeConfig, interruptible, dirtyCheck);
         }
 
         @Override
         public AlphaTransactionFactoryBuilder setReadonly(boolean readonly) {
             SpeculativeConfiguration newSpeculativeConfig = speculativeConfig.withSpeculativeReadonlyDisabled();
 
             return new AlphaTransactionFactoryBuilder(
                     readonly, automaticReadTracking, familyName, maxRetryCount,
                     allowWriteSkewProblem, commitLockPolicy, backoffPolicy,
                     newSpeculativeConfig, interruptible, dirtyCheck);
         }
 
         public AlphaTransactionFactoryBuilder setAutomaticReadTracking(boolean automaticReadTracking) {
             SpeculativeConfiguration newSpeculativeConfig = speculativeConfig.withSpeculativeNonAutomaticReadTrackingDisabled();
 
             return new AlphaTransactionFactoryBuilder(
                     readonly, automaticReadTracking, familyName, maxRetryCount,
                     allowWriteSkewProblem, commitLockPolicy, backoffPolicy,
                     newSpeculativeConfig, interruptible, dirtyCheck);
         }
 
         @Override
         public AlphaTransactionFactoryBuilder setInterruptible(boolean interruptible) {
             return new AlphaTransactionFactoryBuilder(
                     readonly, automaticReadTracking, familyName, maxRetryCount,
                     allowWriteSkewProblem, commitLockPolicy, backoffPolicy,
                     speculativeConfig, interruptible, dirtyCheck);
         }
 
         public AlphaTransactionFactoryBuilder setCommitLockPolicy(CommitLockPolicy commitLockPolicy) {
             if (commitLockPolicy == null) {
                 throw new NullPointerException();
             }
 
             return new AlphaTransactionFactoryBuilder(
                     readonly, automaticReadTracking, familyName, maxRetryCount,
                     allowWriteSkewProblem, commitLockPolicy, backoffPolicy,
                     speculativeConfig, interruptible, dirtyCheck);
         }
 
         @Override
         public AlphaTransactionFactoryBuilder setSpeculativeConfigurationEnabled(boolean enabled) {
             SpeculativeConfiguration newSpeculativeConfig = createSpeculativeConfiguration(enabled, maxArraySize);
 
             return new AlphaTransactionFactoryBuilder(
                     readonly, automaticReadTracking, familyName, maxRetryCount,
                     allowWriteSkewProblem, commitLockPolicy, backoffPolicy,
                     newSpeculativeConfig,
                     interruptible, dirtyCheck);
         }
 
         @Override
         public AlphaTransactionFactoryBuilder setAllowWriteSkewProblem(boolean allowWriteSkew) {
             SpeculativeConfiguration newSpeculativeConfig = speculativeConfig.withSpeculativeNonAutomaticReadTrackingDisabled();
 
             return new AlphaTransactionFactoryBuilder(
                     readonly, automaticReadTracking, familyName, maxRetryCount,
                     allowWriteSkew, commitLockPolicy, backoffPolicy, newSpeculativeConfig,
                     interruptible, dirtyCheck);
         }
 
         @Override
         public AlphaTransactionFactoryBuilder setBackoffPolicy(BackoffPolicy backoffPolicy) {
             if (backoffPolicy == null) {
                 throw new NullPointerException();
             }
 
             return new AlphaTransactionFactoryBuilder(
                     readonly, automaticReadTracking, familyName, maxRetryCount,
                     allowWriteSkewProblem, commitLockPolicy, backoffPolicy,
                     speculativeConfig, interruptible, dirtyCheck);
         }
 
         @Override
         public TransactionFactory<AlphaTransaction> build() {
             if (speculativeConfig.isEnabled()) {
                 return createSpeculativeTxFactory();
             } else if (readonly) {
                 return createNonSpeculativeReadonlyTxFactory();
             } else {
                 return createNonSpeculativeUpdateTxFactory();
             }
         }
 
         private TransactionFactory<AlphaTransaction> createSpeculativeTxFactory() {
             final ReadonlyAlphaTransactionConfiguration ro_nort =
                     new ReadonlyAlphaTransactionConfiguration(
                             clock, backoffPolicy, familyName, speculativeConfig,
                            maxRetryCount, false, false);
             final ReadonlyAlphaTransactionConfiguration ro_rt =
                     new ReadonlyAlphaTransactionConfiguration(
                             clock, backoffPolicy, familyName, speculativeConfig,
                             maxRetryCount, interruptible, true);
             final UpdateAlphaTransactionConfiguration up_rt =
                     new UpdateAlphaTransactionConfiguration(
                             clock, backoffPolicy, commitLockPolicy, familyName,
                             speculativeConfig, maxRetryCount, interruptible,
                             true, allowWriteSkewProblem,
                             optimizeConflictDetectionEnabled, true);
             final UpdateAlphaTransactionConfiguration up_nort =
                     new UpdateAlphaTransactionConfiguration(
                             clock, backoffPolicy, commitLockPolicy, familyName,
                             speculativeConfig, maxRetryCount, interruptible,
                             false, true,
                             optimizeConflictDetectionEnabled, true);
 
             return new TransactionFactory<AlphaTransaction>() {
                 @Override
                 public AlphaTransaction start() {
                     //System.out.println(familyName);
                     //System.out.println(automaticReadTracking);
 
                     boolean finalReadonly;
                     if (speculativeConfig.isSpeculativeReadonlyEnabled()) {
                         finalReadonly = speculativeConfig.isReadonly();
                     } else {
                         finalReadonly = readonly;
                     }
 
                     //System.out.println("readonly: "+finalReadonly);
 
                     boolean finalAutomaticReadTracking;
                     if (speculativeConfig.isSpeculativeNonAutomaticReadTrackingEnabled()) {
                         finalAutomaticReadTracking = speculativeConfig.isAutomaticReadTracking();
                     } else {
                         finalAutomaticReadTracking = automaticReadTracking;
                     }
                     //System.out.println("finalAutomaticReadTracking: "+finalAutomaticReadTracking);
 
                     boolean speculativeSizeEnabled = speculativeConfig.isSpeculativeSizeEnabled();
 
                     //System.out.println("speculativeSizeEnabled: "+speculativeSizeEnabled);
 
                     if (finalReadonly) {
                         if (finalAutomaticReadTracking) {
                             if (speculativeSizeEnabled) {
                                 int size = speculativeConfig.getOptimalSize();
 
                                 if (size <= 1) {
                                     return new MonoReadonlyAlphaTransaction(ro_rt);
                                 } else if (size < maxArraySize) {
                                     return new ArrayReadonlyAlphaTransaction(ro_rt, size);
                                 } else {
                                     return new MapReadonlyAlphaTransaction(ro_rt);
                                 }
                             } else {
                                 return new MapReadonlyAlphaTransaction(ro_rt);
                             }
                         } else {
                             return new NonTrackingReadonlyAlphaTransaction(ro_nort);
                         }
                     } else {
 
                         UpdateAlphaTransactionConfiguration config;
                         if (finalAutomaticReadTracking) {
                             config = up_rt;
                         } else {
                             config = up_nort;
                         }
 
                         if (speculativeSizeEnabled) {
                             int size = speculativeConfig.getOptimalSize();
 
                             if (size <= 1) {
                                 return new MonoUpdateAlphaTransaction(config);
                             } else if (size <= maxArraySize) {
                                 return new ArrayUpdateAlphaTransaction(config, size);
                             } else {
                                 return new MapUpdateAlphaTransaction(config);
                             }
                         } else {
                             return new MapUpdateAlphaTransaction(config);
                         }
                     }
                 }
             };
         }
 
         private TransactionFactory<AlphaTransaction> createNonSpeculativeReadonlyTxFactory() {
             ReadonlyAlphaTransactionConfiguration config =
                     new ReadonlyAlphaTransactionConfiguration(
                             clock, backoffPolicy, familyName, speculativeConfig,
                             maxRetryCount, interruptible, automaticReadTracking);
 
             if (automaticReadTracking) {
                 return new MapReadonlyAlphaTransaction.Factory(config);
             } else {
                 return new NonTrackingReadonlyAlphaTransaction.Factory(config);
             }
         }
 
         private TransactionFactory<AlphaTransaction> createNonSpeculativeUpdateTxFactory() {
             if (!automaticReadTracking && !allowWriteSkewProblem) {
                 String msg = format("Can't create transactionfactory for transaction family '%s' because an update "
                         + "transaction without automaticReadTracking and without allowWriteSkewProblem is "
                         + "not possible", familyName
                 );
 
                 throw new IllegalStateException(msg);
             }
 
             UpdateAlphaTransactionConfiguration config =
                     new UpdateAlphaTransactionConfiguration(
                             clock, backoffPolicy, commitLockPolicy, familyName,
                             speculativeConfig, maxRetryCount, interruptible,
                             automaticReadTracking, allowWriteSkewProblem,
                             optimizeConflictDetectionEnabled, true);
 
             return new MapUpdateAlphaTransaction.Factory(config);
         }
     }
 }
