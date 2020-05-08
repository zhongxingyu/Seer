 package org.multiverse.stms.alpha.transactions;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import static java.lang.String.format;
 
 public final class SpeculativeConfiguration {
 
     public static SpeculativeConfiguration createSpeculativeConfiguration(boolean enabled, int maximumArraySize) {
         if (enabled) {
             return new SpeculativeConfiguration(true, true, true, maximumArraySize);
         } else {
             return new SpeculativeConfiguration(false, false, false, maximumArraySize);
         }
     }
 
     private final AtomicInteger size = new AtomicInteger();
     private final AtomicBoolean readonly = new AtomicBoolean(true);
     private final AtomicBoolean isReadTrackingEnabled = new AtomicBoolean(false);
 
     private final int maximumArraySize;
     private final boolean isSpeculativeReadonlyEnabled;
     private final boolean isSpeculativeNoReadTrackingEnabled;
     private final boolean isSpeculativeSizeEnabled;
 
     public SpeculativeConfiguration(
             boolean isSpeculativeOnReadonly,
             boolean isSpeculativeNonAutomaticReadTrackingEnabled,
             boolean isSpeculativeSizeEnabled,
             int maximumArraySize) {
 
         this.isSpeculativeReadonlyEnabled = isSpeculativeOnReadonly;
         this.isSpeculativeNoReadTrackingEnabled = isSpeculativeNonAutomaticReadTrackingEnabled;
         this.isSpeculativeSizeEnabled = isSpeculativeSizeEnabled;
         this.size.set(1);
         this.maximumArraySize = maximumArraySize;
     }
 
     public SpeculativeConfiguration(int maximumArraySize) {
         this(true, true, true, maximumArraySize);
     }
 
     public boolean isEnabled() {
         return isSpeculativeNoReadTrackingEnabled
                 || isSpeculativeReadonlyEnabled
                 || isSpeculativeSizeEnabled;
     }
 
     // ================ size ===========================
 
     public boolean isSpeculativeSizeEnabled() {
         return isSpeculativeSizeEnabled;
     }
 
     public void setOptimalSize(int newValue) {
         size.set(newValue);
     }
 
     public int getOptimalSize() {
         return size.get();
     }
 
     public void signalSpeculativeSizeFailure(int failedSize) {
         if (failedSize < 0) {
             throw new IllegalArgumentException();
         }
 
         int newOptimalSize;
         if (failedSize == 0) {
             newOptimalSize = 1;
         } else if (failedSize == 1) {
             newOptimalSize = 2;
         } else if (failedSize == 2) {
             newOptimalSize = 3;
         } else {
             newOptimalSize = failedSize + 2;
         }
 
         while (true) {
             int currentSize = this.size.get();
             if (currentSize >= newOptimalSize) {
                 return;
             }
 
             if (size.compareAndSet(currentSize, newOptimalSize)) {
                 return;
             }
         }
     }
 
     public int getMaximumArraySize() {
         return maximumArraySize;
     }
 
     // ================ readonly ===========================
 
     public void signalSpeculativeReadonlyFailure() {
         if (!isSpeculativeReadonlyEnabled) {
             throw new IllegalStateException();
         }
 
         readonly.set(false);
     }
 
     public boolean isSpeculativeReadonlyEnabled() {
         return isSpeculativeReadonlyEnabled;
     }
 
     public boolean isReadonly() {
         return readonly.get();
     }
 
     public SpeculativeConfiguration withSpeculativeReadonlyDisabled() {
         return new SpeculativeConfiguration(
                 false, isSpeculativeNoReadTrackingEnabled, isSpeculativeSizeEnabled, maximumArraySize);
     }
 
     // ================ automatic readtracking ===========================
 
     public boolean isSpeculativeNoReadTrackingEnabled() {
         return isSpeculativeNoReadTrackingEnabled;
     }
 
     public void signalSpeculativeReadTrackingDisabledFailure() {
         if (!isSpeculativeNoReadTrackingEnabled) {
             throw new IllegalStateException();
         }
 
         isReadTrackingEnabled.set(true);
     }
 
     public boolean isReadTrackingEnabled() {
         return isReadTrackingEnabled.get();
     }
 
     public SpeculativeConfiguration withSpeculativeNonAutomaticReadTrackingDisabled() {
         return new SpeculativeConfiguration(
                 isSpeculativeReadonlyEnabled, false, isSpeculativeSizeEnabled, maximumArraySize);
     }
 
     // ============= misc ===========================
 
     @Override
     public String toString() {
        return format("SpeculativeConfiguration(" +
                "size=%s, " +
                 "isSpeculativeReadonlyEnabled=%s, " +
                 "isSpeculativeNonAutomaticReadTrackingEnabled=%s, " +
                 "isSpeculativeSizeEnabled=%s" +
                 ")",
                 size.get(),
                 isSpeculativeReadonlyEnabled,
                 isSpeculativeNoReadTrackingEnabled,
                 isSpeculativeSizeEnabled);
     }
 }
