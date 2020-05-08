 package jp.ac.osaka_u.ist.sel.metricstool.main.io;
 
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.MethodMetricsInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.MetricNotRegisteredException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin.PluginInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.METRIC_TYPE;
 
 
 /**
  * \bhgNXCSV@CɏoNX
  * 
  * @author y-higo
  * 
  */
 public final class CSVMethodMetricsWriter implements MethodMetricsWriter, CSVWriter, MessageSource {
 
     /**
      * CSVt@C^
      * 
      * @param fileName CSVt@C
      */
     public CSVMethodMetricsWriter(final String fileName) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == fileName) {
             throw new NullPointerException();
         }
 
         this.fileName = fileName;
     }
 
     /**
      * \bhgNXCSVt@Cɏo
      */
     public void write() {
 
         try {
 
             BufferedWriter writer = new BufferedWriter(new FileWriter(this.fileName));
 
             // gNXȂǂo
             writer.write(METHOD_NAME);
             for (AbstractPlugin plugin : PLUGIN_MANAGER.getPlugins()) {
                 PluginInfo pluginInfo = plugin.getPluginInfo();
                 if (METRIC_TYPE.METHOD_METRIC == pluginInfo.getMetricType()) {
                     String metricName = pluginInfo.getMetricName();
                     writer.write(SEPARATOR);
                     writer.write(metricName);
                 }
             }
 
             // gNXlo
             for (MethodMetricsInfo methodMetricsInfo : METHOD_METRICS_MANAGER) {
                 MethodInfo methodInfo = methodMetricsInfo.getMethodInfo();
 
                 String methodName = methodInfo.getName();
                 writer.write(methodName);
                 for (AbstractPlugin plugin : PLUGIN_MANAGER.getPlugins()) {
                     PluginInfo pluginInfo = plugin.getPluginInfo();
                     if (METRIC_TYPE.METHOD_METRIC == pluginInfo.getMetricType()) {
 
                         try {
                             writer.write(SEPARATOR);
                             Float value = methodMetricsInfo.getMetric(plugin);
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
      * MessagerPrinter p邽߂ɕKvȃ\bh
      * 
      * @see MessagePrinter
      * @see MessageSource
      * 
      * @return bZ[WMҖԂ
      */
     public String getMessageSourceName() {
         return this.getClass().toString();
     }
 
     /**
      * \bhgNXt@Cۑ邽߂̕ϐ
      */
     private final String fileName;
 }
