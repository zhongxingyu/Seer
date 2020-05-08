 package jp.ac.osaka_u.ist.sel.metricstool.main.io;
 
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.ClassMetricsInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.MetricNotRegisteredException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin.PluginInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.METRIC_TYPE;
 
 
 /**
  * NXgNXCSV@CɏoNX
  * 
  * @author y-higo
  * 
  */
 public final class CSVClassMetricsWriter implements ClassMetricsWriter, CSVWriter, MessageSource {
 
     /**
      * CSVt@C^
      * 
      * @param fileName CSVt@C
      */
     public CSVClassMetricsWriter(final String fileName) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == fileName) {
             throw new NullPointerException();
         }
 
         this.fileName = fileName;
     }
 
     /**
      * NXgNXCSVt@Cɏo
      */
     public void write() {
 
         try {
 
             BufferedWriter writer = new BufferedWriter(new FileWriter(this.fileName));
 
             // gNXȂǂo
             writer.write(CLASS_NAME);
             for (AbstractPlugin plugin : PLUGIN_MANAGER.getPlugins()) {
                 PluginInfo pluginInfo = plugin.getPluginInfo();
                 if (METRIC_TYPE.CLASS_METRIC == pluginInfo.getMetricType()) {
                     String metricName = pluginInfo.getMetricName();
                     writer.write(SEPARATOR);
                     writer.write(metricName);
                 }
             }
 
             // gNXlo
             for (ClassMetricsInfo classMetricsInfo : CLASS_METRICS_MANAGER) {
                 ClassInfo classInfo = classMetricsInfo.getClassInfo();
 
                 String className = classInfo.getName();
                 writer.write(className);
                 for (AbstractPlugin plugin : PLUGIN_MANAGER.getPlugins()) {
                     PluginInfo pluginInfo = plugin.getPluginInfo();
                     if (METRIC_TYPE.CLASS_METRIC == pluginInfo.getMetricType()) {
 
                         try {
                             writer.write(SEPARATOR);
                             Float value = classMetricsInfo.getMetric(plugin);
                             writer.write(value.toString());
                         } catch (MetricNotRegisteredException e) {
                             writer.write(NO_METRIC);
                         }
                     }
                 }
                 writer.newLine();
             }
 
             writer.close();
 
         } catch (IOException e) {
 
             MessagePrinter printer = new DefaultMessagePrinter(this,
                     MessagePrinter.MESSAGE_TYPE.ERROR);
             printer.println("IO Error Happened on " + this.fileName);
         }
     }
 
     /**
      * MessagerPrinter p邽߂ɕKvȃ\bhDbZ[WMҖԂ
      * 
      * @see MessagePrinter
      * @see MessageSource
      * 
      * @return bZ[WMҖ
      */
     public String getMessageSourceName() {
         return this.getClass().toString();
     }
 
     /**
      * NXgNXt@Cۑ邽߂̕ϐ
      */
     private final String fileName;
 }
