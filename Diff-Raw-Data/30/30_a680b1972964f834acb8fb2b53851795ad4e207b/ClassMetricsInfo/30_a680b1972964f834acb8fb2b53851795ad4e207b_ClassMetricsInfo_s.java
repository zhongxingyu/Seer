 package jp.ac.osaka_u.ist.sel.metricstool.main.data.metric;
 
 
 import java.util.Collections;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.DefaultMessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageSource;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.MetricTypeAndNamePluginComparator;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.PluginManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin.PluginInfo;
 
 
 /**
  * NXgNXo^邽߂̃f[^NX
  * 
  * @author y-higo
  * 
  */
 public final class ClassMetricsInfo implements MessageSource {
 
     /**
      * ȂRXgN^D
      */
     public ClassMetricsInfo(final ClassInfo classInfo) {
 
         if (null == classInfo) {
             throw new NullPointerException();
         }
 
         this.classInfo = classInfo;
         this.classMetrics = Collections.synchronizedSortedMap(new TreeMap<AbstractPlugin, Float>(
                 new MetricTypeAndNamePluginComparator()));
     }
 
     /**
      * ̃gNX̃NXԂ
      * 
      * @return ̃gNX̃NX
      */
     public ClassInfo getClassInfo() {
         return this.classInfo;
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
 
         Float value = this.classMetrics.get(key);
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
                 ClassInfo classInfo = this.getClassInfo();
                String className = classInfo.getFullQualifiedtName();
                 String message = "Metric \"" + metricName + "\" of " + className
                         + " is not registered!";
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
         if (this.classMetrics.containsKey(key)) {
             throw new MetricAlreadyRegisteredException();
         }
 
         this.classMetrics.put(key, value);
     }
 
     /**
      * gNX̃NXۑ邽߂̕ϐ
      */
     private final ClassInfo classInfo;
 
     /**
      * NXgNXۑ邽߂̕ϐ
      */
     private final SortedMap<AbstractPlugin, Float> classMetrics;
 }
