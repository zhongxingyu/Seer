 package jp.ac.osaka_u.ist.sel.metricstool.main.data.metric;
 
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.DefaultMessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageSource;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * \bhgNXǗNXD
  * 
  * @author y-higo
  * 
  */
 public final class MethodMetricsInfoManager implements Iterable<MethodMetricsInfo>, MessageSource {
 
     /**
      * ̃NX̃CX^XԂDVOgp^[pĂD
      * 
      * @return ̃NX̃CX^X
      */
     public static MethodMetricsInfoManager getInstance() {
         return SINGLETON;
     }
 
     /**
      * gNXꗗ̃Ce[^ԂD
      * 
      * @return gNX̃Ce[^
      */
     public Iterator<MethodMetricsInfo> iterator() {
         MetricsToolSecurityManager.getInstance().checkAccess();
         Collection<MethodMetricsInfo> unmodifiableMethodMetricsInfoCollection = Collections
                 .unmodifiableCollection(this.methodMetricsInfos.values());
         return unmodifiableMethodMetricsInfoCollection.iterator();
     }
 
     /**
      * Ŏw肳ꂽ\bh̃gNXԂD Ŏw肳ꂽ\bh̃gNX񂪑݂Ȃꍇ́C null ԂD
      * 
      * @param methodInfo قgNX̃\bh
      * @return gNX
      */
     public MethodMetricsInfo get(final MethodInfo methodInfo) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == methodInfo) {
             throw new NullPointerException();
         }
 
         return this.methodMetricsInfos.get(methodInfo);
     }
 
     /**
      * gNXo^
      * 
      * @param methodInfo gNXvΏۂ̃\bhIuWFNg
      * @param plugin gNX̃vOC
      * @param value gNXl
      * @throws MetricAlreadyRegisteredException o^悤ƂĂ郁gNXɓo^Ă
      */
     public void putMetric(final MethodInfo methodInfo, final AbstractPlugin plugin, final int value)
             throws MetricAlreadyRegisteredException {
         putMetric(methodInfo, plugin, (float) value);
     }
 
     /**
      * gNXo^
      * 
      * @param methodInfo gNXvΏۂ̃\bhIuWFNg
      * @param plugin gNX̃vOC
      * @param value gNXl
      * @throws MetricAlreadyRegisteredException o^悤ƂĂ郁gNXɓo^Ă
      */
     public void putMetric(final MethodInfo methodInfo, final AbstractPlugin plugin,
             final float value) throws MetricAlreadyRegisteredException {
 
         MethodMetricsInfo methodMetricsInfo = this.methodMetricsInfos.get(methodInfo);
 
         // Ώۃ\bh methodMetricsInfo ꍇ́Cnew  Map ɓo^
         if (null == methodMetricsInfo) {
             methodMetricsInfo = new MethodMetricsInfo(methodInfo);
             this.methodMetricsInfos.put(methodInfo, methodMetricsInfo);
         }
 
         methodMetricsInfo.putMetric(plugin, value);
     }
 
     /**
      * \bhgNXɓo^RꂪȂ`FbN
      * 
      * @throws MetricNotRegisteredException o^RꂪꍇɃX[
      */
     public void checkMetrics() throws MetricNotRegisteredException {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
 
         for (MethodInfo methodInfo : MethodInfoManager.getInstance().getTargetMethodInfos()) {
 
             MethodMetricsInfo methodMetricsInfo = this.get(methodInfo);
             if (null == methodMetricsInfo) {
                 String methodName = methodInfo.getMethodName();
                 ClassInfo ownerClassInfo = methodInfo.getOwnerClass();
                String ownerClassName = ownerClassInfo.getFullQualifiedtName(".");
                 String message = "Metrics of " + ownerClassName + "::" + methodName
                         + " are not registered!";
                 MessagePrinter printer = new DefaultMessagePrinter(this,
                         MessagePrinter.MESSAGE_TYPE.ERROR);
                 printer.println(message);
                 throw new MetricNotRegisteredException(message);
             }
             methodMetricsInfo.checkMetrics();
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
      * \bhgNX}l[W̃IuWFNg𐶐D VOgp^[pĂ邽߁Cprivate ĂD
      * 
      */
     private MethodMetricsInfoManager() {
         this.methodMetricsInfos = Collections
                 .synchronizedSortedMap(new TreeMap<MethodInfo, MethodMetricsInfo>());
     }
 
     /**
      * ̃NX̃IuWFNgǗĂ萔DVOgp^[pĂD
      */
     private static final MethodMetricsInfoManager SINGLETON = new MethodMetricsInfoManager();
 
     /**
      * \bhgNX̃}bvۑ邽߂̕ϐ
      */
     private final SortedMap<MethodInfo, MethodMetricsInfo> methodMetricsInfos;
 }
