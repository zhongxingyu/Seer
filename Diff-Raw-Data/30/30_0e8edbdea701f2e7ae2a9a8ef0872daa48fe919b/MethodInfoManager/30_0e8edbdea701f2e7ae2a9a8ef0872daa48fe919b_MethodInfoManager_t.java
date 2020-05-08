 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalConstructorInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * \bhǗNXD methodInfo vfƂĎD
  * 
  * @author higo
  * 
  */
 public final class MethodInfoManager {
 
     /**
      * \bhǗĂCX^XԂD VOgp^[ĂD
      * 
      * @return \bhǗĂCX^X
      */
     public static MethodInfoManager getInstance() {
         return SINGLETON;
     }
 
     /**
      * 
      * @param methodInfo ǉ郁\bh
      */
     public void add(final TargetMethodInfo methodInfo) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == methodInfo) {
             throw new NullPointerException();
         }
 
         this.targetMethodInfos.add(methodInfo);
     }
 
     /**
      * 
      * @param methodInfo ǉ郁\bh
      */
     public void add(final ExternalMethodInfo methodInfo) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == methodInfo) {
             throw new NullPointerException();
         }
 
         this.externalMethodInfos.add(methodInfo);
     }
 
     /**
      * 
      * @param constructorInfo ǉRXgN^
      */
     public void add(final TargetConstructorInfo constructorInfo) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == constructorInfo) {
             throw new NullPointerException();
         }
 
         this.targetConstructorInfos.add(constructorInfo);
     }
 
     /**
      * 
      * @param constructorInfo ǉRXgN^
      */
     public void add(final ExternalConstructorInfo constructorInfo) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == constructorInfo) {
             throw new NullPointerException();
         }
 
         this.externalConstructorInfos.add(constructorInfo);
     }
 
     /**
      * Ώۃ\bhSortedSetԂD
      * 
      * @return Ώۃ\bhSortedSet
      */
     public SortedSet<TargetMethodInfo> getTargetMethodInfos() {
         return Collections.unmodifiableSortedSet(this.targetMethodInfos);
     }
 
     /**
      * O\bhSortedSetԂD
      * 
      * @return O\bhSortedSet
      */
     public SortedSet<ExternalMethodInfo> getExternalMethodInfos() {
         return Collections.unmodifiableSortedSet(this.externalMethodInfos);
     }
 
     /**
      * ĂΏۃ\bȟԂ.
      * 
      * @return Ώۃ\bȟ
      */
     public int getTargetMethodCount() {
         return this.targetMethodInfos.size();
     }
 
     /**
      * ĂO\bȟԂ.
      * 
      * @return O\bȟ
      */
     public int getExternalMethodCount() {
         return this.externalMethodInfos.size();
     }
 
     /**
      * ΏۃRXgN^SortedSetԂD
      * 
      * @return ΏۃRXgN^SortedSet
      */
     public SortedSet<TargetConstructorInfo> getTargetConstructorInfos() {
         return Collections.unmodifiableSortedSet(this.targetConstructorInfos);
     }
 
     /**
      * ORXgN^SortedSetԂD
      * 
      * @return ORXgN^SortedSet
      */
     public SortedSet<ExternalConstructorInfo> getExternalConstructorInfos() {
         return Collections.unmodifiableSortedSet(this.externalConstructorInfos);
     }
 
     /**
      * ΏۃRXgN^̌Ԃ.
      * 
      * @return ΏۃRXgN^̌
      */
     public int getTargetConstructorCount() {
         return this.targetConstructorInfos.size();
     }
 
     /**
      * ORXgN^̌Ԃ.
      * 
      * @return ORXgN^̌
      */
     public int getExternalConstructorCount() {
         return this.externalConstructorInfos.size();
     }
 
     /**
      * 
      * RXgN^D VOgp^[ŎĂ邽߂ private ĂD
      */
     private MethodInfoManager() {
         this.targetMethodInfos = new TreeSet<TargetMethodInfo>();
         this.externalMethodInfos = new TreeSet<ExternalMethodInfo>();
         this.targetConstructorInfos = new TreeSet<TargetConstructorInfo>();
         this.externalConstructorInfos = new TreeSet<ExternalConstructorInfo>();
     }
    
     /**
      * 
      * VOgp^[邽߂̕ϐD
      */
     private static final MethodInfoManager SINGLETON = new MethodInfoManager();
 
     /**
      * 
      * Ώۃ\bhi[ϐD
      */
     private final SortedSet<TargetMethodInfo> targetMethodInfos;
 
     /**
      * 
      * O\bhi[ϐD
      */
     private final SortedSet<ExternalMethodInfo> externalMethodInfos;
 
     /**
      * ΏۃRXgN^i[ϐ
      */
     private final SortedSet<TargetConstructorInfo> targetConstructorInfos;
 
     /**
      * ORXgN^i[ϐ
      */
     private final SortedSet<ExternalConstructorInfo> externalConstructorInfos;
 }
