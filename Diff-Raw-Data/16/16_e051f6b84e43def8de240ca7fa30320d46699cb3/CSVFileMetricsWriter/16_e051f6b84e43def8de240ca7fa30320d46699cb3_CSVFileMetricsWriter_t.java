 package jp.ac.osaka_u.ist.sel.metricstool.main.io;
 
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.FileMetricsInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.MetricNotRegisteredException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FileInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin.PluginInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.METRIC_TYPE;
 
 
 /**
  * t@CgNXCSV@CɏoNX
  * 
  * @author y-higo
  * 
  */
 public final class CSVFileMetricsWriter implements FileMetricsWriter, CSVWriter, MessageSource {
 
     /**
      * CSVt@C^
      * 
      * @param fileName CSVt@C
      */
     public CSVFileMetricsWriter(final String fileName) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == fileName) {
             throw new NullPointerException();
         }
 
         this.fileName = fileName;
     }
 
     /**
      * t@CgNXCSVt@Cɏo
      */
     public void write() {
 
         try {
 
             BufferedWriter writer = new BufferedWriter(new FileWriter(this.fileName));
 
             // gNXȂǂo
             writer.write(FILE_NAME);
             for (AbstractPlugin plugin : PLUGIN_MANAGER.getPlugins()) {
                 PluginInfo pluginInfo = plugin.getPluginInfo();
                 if (METRIC_TYPE.FILE_METRIC == pluginInfo.getMetricType()) {
                     String metricName = pluginInfo.getMetricName();
                     writer.write(SEPARATOR);
                     writer.write(metricName);
                 }
             }
 
            writer.newLine();
            
             // gNXlo
             for (FileMetricsInfo fileMetricsInfo : FILE_METRICS_MANAGER) {
                 FileInfo fileInfo = fileMetricsInfo.getFileInfo();
 
                 String fileName = fileInfo.getName();
                 writer.write(fileName);
                 for (AbstractPlugin plugin : PLUGIN_MANAGER.getPlugins()) {
                     PluginInfo pluginInfo = plugin.getPluginInfo();
                     if (METRIC_TYPE.FILE_METRIC == pluginInfo.getMetricType()) {
 
                         try {
                             writer.write(SEPARATOR);
                             Float value = fileMetricsInfo.getMetric(plugin);
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
      * t@CgNXt@Cۑ邽߂̕ϐ
      */
     private final String fileName;
 }
