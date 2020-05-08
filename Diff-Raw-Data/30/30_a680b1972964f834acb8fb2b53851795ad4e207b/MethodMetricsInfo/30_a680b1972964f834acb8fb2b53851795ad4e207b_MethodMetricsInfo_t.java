 package jp.ac.osaka_u.ist.sel.metricstool.main.data.metric;
 
 
 import java.util.Collections;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.DefaultMessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageSource;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.MetricTypeAndNamePluginComparator;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.PluginManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin.PluginInfo;
 
 
 /**
  * \bhgNXo^邽߂̃f[^NX
  * 
  * @author y-higo
  * 
  */
 public final class MethodMetricsInfo implements MessageSource {
 
     /**
      * ȂRXgN^D
      */
     public MethodMetricsInfo(final MethodInfo methodInfo) {
 
         if (null == methodInfo) {
             throw new NullPointerException();
         }
 
         this.methodInfo = methodInfo;
         this.methodMetrics = Collections.synchronizedSortedMap(new TreeMap<AbstractPlugin, Float>(
                 new MetricTypeAndNamePluginComparator()));
     }
 
     /**
      * ̃gNX̃\bhԂ
      * 
      * @return ̃gNX̃\bh
      */
     public MethodInfo getMethodInfo() {
         return this.methodInfo;
     }
 
     /**
      * Ŏw肵vOCɂēo^ꂽgNX擾郁\bhD
      * 
      * @param key قgNXo^vOC
      * @return gNXl
      * @throws MetricNotRegisteredException gNXo^ĂȂƂɃX[
      */
     public float getMetric(final AbstractPlugin key) throws MetricNotRegisteredException {
 
         if (null == key) {
             throw new NullPointerException();
         }
 
         Float value = this.methodMetrics.get(key);
         if (null == value) {
             throw new MetricNotRegisteredException();
         }
 
         return value.floatValue();
     }
 
     /**
      * ŗ^ꂽvOCŌvꂽgNXlijo^D
      * 
      * @param key vvOCCX^XCMap ̃L[ƂėpD
      * @param value o^郁gNXl(int)
      * @throws MetricAlreadyRegisteredException o^悤ƂĂgNXɓo^ĂꍇɃX[
      */
     public void putMetric(final AbstractPlugin key, final int value)
             throws MetricAlreadyRegisteredException {
         this.putMetric(key, Float.valueOf(value));
     }
 
     /**
      * ŗ^ꂽvOCŌvꂽgNXlijo^D
      * 
      * @param key vvOCCX^XCMap ̃L[ƂėpD
      * @param value o^郁gNXl(float)
      * @throws MetricAlreadyRegisteredException o^悤ƂĂgNXɓo^ĂꍇɃX[
      */
     public void putMetric(final AbstractPlugin key, final float value)
             throws MetricAlreadyRegisteredException {
         this.putMetric(key, Float.valueOf(value));
     }
 
     /**
      * bZ[W̑MҖԂ
      * 
      * @return bZ[W̑MҖ
      */
     public String getMessageSourceName() {
         return this.getClass().getName();
     }
 
     /**
      * ̃gNXɕsȂ`FbN
      * 
      * @throws MetricNotRegisteredException
      */
     void checkMetrics() throws MetricNotRegisteredException {
         PluginManager pluginManager = PluginManager.getInstance();
         for (AbstractPlugin plugin : pluginManager.getPlugins()) {
             Float value = this.getMetric(plugin);
             if (null == value) {
                 PluginInfo pluginInfo = plugin.getPluginInfo();
                 String metricName = pluginInfo.getMetricName();
                 MethodInfo methodInfo = this.getMethodInfo();
                 String methodName = methodInfo.getMethodName();
                 ClassInfo ownerClassInfo = methodInfo.getOwnerClass();
                String ownerClassName = ownerClassInfo.getFullQualifiedtName(".");
                 String message = "Metric \"" + metricName + "\" of " + ownerClassName + "::"
                         + methodName + " is not registered!";
                 MessagePrinter printer = new DefaultMessagePrinter(this,
                         MessagePrinter.MESSAGE_TYPE.ERROR);
                 printer.println(message);
                 throw new MetricNotRegisteredException(message);
             }
         }
     }
 
     /**
      * ŗ^ꂽvOCŌvꂽgNXlijo^D
      * 
      * @param key vvOCCX^XCMap ̃L[ƂėpD
      * @param value o^郁gNXl
      * @throws MetricAlreadyRegisteredException o^悤ƂĂgNXɓo^ĂꍇɃX[
      */
     private void putMetric(final AbstractPlugin key, final Float value)
             throws MetricAlreadyRegisteredException {
 
         if ((null == key) || (null == value)) {
             throw new NullPointerException();
         }
         if (this.methodMetrics.containsKey(key)) {
             throw new MetricAlreadyRegisteredException();
         }
 
         this.methodMetrics.put(key, value);
     }
 
     /**
      * ̃gNX̃\bhۑ邽߂̕ϐ
      */
     private final MethodInfo methodInfo;
 
     /**
      * \bhgNXۑ邽߂̕ϐ
      */
     private final SortedMap<AbstractPlugin, Float> methodMetrics;
 }
