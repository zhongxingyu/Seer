 package org.radargun.stages;
 
 import org.radargun.state.MasterState;
 
 import java.util.Map;
 
 /**
  * @author Diego Didona, didona@gsd.inesc-id.pt
  *         Date: 20/12/12
  */
 public class TpccCsvReportGenerationStage extends CsvReportGenerationStage {
 
    protected final String reportFileName(MasterState masterState) {
       Object o = masterState.get("results");
       @SuppressWarnings("unchecked")
       Map<String, Object> results = (Map<String, Object>) o;
       int slaves = masterState.getSlavesCountForCurrentStage();
       StringBuilder sb = new StringBuilder();
       int numThreads = toInt(results, "numThreads");
       int lambda = toInt(results, "Lambda");
       int thinkTime = toInt(results, "ThinkTime");
 
       sb.append(masterState.nameOfTheCurrentBenchmark());
       sb.append("_");
       sb.append(masterState.configNameOfTheCurrentBenchmark());
       sb.append("_");
       sb.append(numThreads);
       sb.append("T_");
       sb.append(slaves);
       sb.append(".csv");
       return sb.toString();
    }
 
    private int toInt(Map<String, Object> results, String stat) {
       Object o = results.get(stat);
      return o == null ? 0 : (Integer)o;
    }
 }
