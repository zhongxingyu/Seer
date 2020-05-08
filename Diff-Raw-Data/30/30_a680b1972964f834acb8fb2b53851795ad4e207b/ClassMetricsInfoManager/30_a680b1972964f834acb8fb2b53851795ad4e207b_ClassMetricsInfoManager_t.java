 package jp.ac.osaka_u.ist.sel.metricstool.main.data.metric;
 
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.DefaultMessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageSource;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * NXgNXǗNXD
  * 
  * @author y-higo
  * 
  */
 public final class ClassMetricsInfoManager implements Iterable<ClassMetricsInfo>, MessageSource {
 
     /**
      * ̃NX̃CX^XԂDVOgp^[pĂD
      * 
      * @return ̃NX̃CX^X
      */
     public static ClassMetricsInfoManager getInstance() {
         return SINGLETON;
     }
 
     /**
      * gNXꗗ̃Ce[^ԂD
      * 
      * @return gNX̃Ce[^
      */
     public Iterator<ClassMetricsInfo> iterator() {
         MetricsToolSecurityManager.getInstance().checkAccess();
         Collection<ClassMetricsInfo> unmodifiableClassMetricsInfoCollection = Collections
                 .unmodifiableCollection(this.classMetricsInfos.values());
         return unmodifiableClassMetricsInfoCollection.iterator();
     }
 
     /**
      * Ŏw肳ꂽNX̃gNXԂD Ŏw肳ꂽNX̃gNX񂪑݂Ȃꍇ́C null ԂD
      * 
      * @param classInfo قgNX̃NX
      * @return gNX
      */
     public ClassMetricsInfo get(final ClassInfo classInfo) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == classInfo) {
             throw new NullPointerException();
         }
 
         return this.classMetricsInfos.get(classInfo);
     }
 
     /**
      * gNXo^
      * 
      * @param classInfo gNXvΏۂ̃NXIuWFNg
      * @param plugin gNX̃vOC
      * @param value gNXl
      * @throws MetricAlreadyRegisteredException o^悤ƂĂ郁gNXɓo^Ă
      */
     public void putMetric(final ClassInfo classInfo, final AbstractPlugin plugin, final int value)
             throws MetricAlreadyRegisteredException {
         this.putMetric(classInfo, plugin, (float) value);
     }
 
     /**
      * gNXo^
      * 
      * @param classInfo gNXvΏۂ̃NXIuWFNg
      * @param plugin gNX̃vOC
      * @param value gNXl
      * @throws MetricAlreadyRegisteredException o^悤ƂĂ郁gNXɓo^Ă
      */
     public void putMetric(final ClassInfo classInfo, final AbstractPlugin plugin, final float value)
             throws MetricAlreadyRegisteredException {
 
         ClassMetricsInfo classMetricsInfo = this.classMetricsInfos.get(classInfo);
 
         // ΏۃNX classMetricsInfo ꍇ́Cnew  Map ɓo^
         if (null == classMetricsInfo) {
             classMetricsInfo = new ClassMetricsInfo(classInfo);
             this.classMetricsInfos.put(classInfo, classMetricsInfo);
         }
 
         classMetricsInfo.putMetric(plugin, value);
     }
 
     /**
      * NXgNXɓo^RꂪȂ`FbN
      * 
      * @throws MetricNotRegisteredException o^RꂪꍇɃX[
      */
     public void checkMetrics() throws MetricNotRegisteredException {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
 
         for (ClassInfo classInfo : ClassInfoManager.getInstance().getTargetClassInfos()) {
 
             ClassMetricsInfo classMetricsInfo = this.get(classInfo);
             if (null == classMetricsInfo) {
                String message = "Class \"" + classInfo.getFullQualifiedtName(".")
                         + "\" metrics are not registered!";
                 MessagePrinter printer = new DefaultMessagePrinter(this,
                         MessagePrinter.MESSAGE_TYPE.ERROR);
                 printer.println(message);
                 throw new MetricNotRegisteredException(message);
             }
             classMetricsInfo.checkMetrics();
         }
     }
 
     /**
      * bZ[WMҖԂ
      * 
      * @return bZ[WM
      */
     public String getMessageSourceName() {
         return this.getClass().getName();
     }
 
     /**
      * NXgNX}l[W̃IuWFNg𐶐D VOgp^[pĂ邽߁Cprivate ĂD
      */
     private ClassMetricsInfoManager() {
         this.classMetricsInfos = Collections
                 .synchronizedSortedMap(new TreeMap<ClassInfo, ClassMetricsInfo>());
     }
 
     /**
      * ̃NX̃IuWFNgǗĂ萔DVOgp^[pĂD
      */
     private static final ClassMetricsInfoManager SINGLETON = new ClassMetricsInfoManager();
 
     /**
      * NXgNX̃}bvۑ邽߂̕ϐ
      */
     private final SortedMap<ClassInfo, ClassMetricsInfo> classMetricsInfos;
 }
