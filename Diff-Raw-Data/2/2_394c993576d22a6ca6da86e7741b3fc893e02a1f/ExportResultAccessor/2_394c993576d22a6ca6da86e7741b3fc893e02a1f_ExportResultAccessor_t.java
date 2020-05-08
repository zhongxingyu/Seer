 package org.pillarone.riskanalytics.core.dataaccess;
 
 import org.joda.time.DateTime;
 import org.pillarone.riskanalytics.core.RiskAnalyticsResultAccessException;
 import org.pillarone.riskanalytics.core.output.SimulationRun;
 import org.pillarone.riskanalytics.core.output.SingleValueResultPOJO;
 import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * author simon.parten @ art-allianz . com
  */
 public class ExportResultAccessor {
 
 
     /**
      * This method is a fairly close clone of @link(org.pillarone.riskanalytics.core.dataaccess.ResultAccessor#getSingleValueResults(java.lang.String, java.lang.String, java.lang.String, org.pillarone.riskanalytics.core.output.SimulationRun))
      *
      * The differences are that this is written in java, it should thus be faster. It should handle errors more gracefully, and it
      * is far more strongly typed.
      *
      * @param collector used to lookup the field in question
      * @param path path in question
      * @param field field in question
      * @param run the simulation run
      * @return returns a list of @link{org.pillarone.riskanalytics.core.output.SingleValueResultPOJO}. Note that these objects have null
      * fields set for their collector, simulationRun, path and fields member variables. The reason is that these should certainly be know by the calling function.
      * Furthermore, they are grails domain objects, and grails will do funky stuff on their creation. We therefore return nuill values.
      * It is important the calling function handles this appropriately.
      */
     public static List<SingleValueResultPOJO> getSingleValueResultsForExport(String collector, String path, String field, SimulationRun run ) {
         List<SingleValueResultPOJO> result = new ArrayList<SingleValueResultPOJO>();
         long pathId = ResultAccessor.getPathId(path);
         long fieldId = ResultAccessor.getFieldId(field);
         long collectorId = ResultAccessor.getCollectorId(collector);
 
         for (int i = 0; i < run.getPeriodCount(); i++) {
             File f = new File(GridHelper.getResultPathLocation(ResultAccessor.getRunIDFromSimulation(run) , pathId, fieldId, collectorId, i));
             IterationFileAccessor ifa;
             try {
                 ifa = new IterationFileAccessor(f);
             } catch (Exception e) {
                 throw new RiskAnalyticsResultAccessException("Failed to find file : " + f.toString(), e);
             }
             try {
                 while (ifa.fetchNext()) {
                     int iteration = ifa.getIteration();
                     List<DateTimeValuePair> values = ifa.getSingleValues();
 
                     for (DateTimeValuePair val : values) {
                         SingleValueResultPOJO resultWithNullFieldsCollectorsSimRunAndPath = new SingleValueResultPOJO();
                         resultWithNullFieldsCollectorsSimRunAndPath.setDate(new DateTime(val.getDateTime()));
                         resultWithNullFieldsCollectorsSimRunAndPath.setValue(val.getaDouble());
                         resultWithNullFieldsCollectorsSimRunAndPath.setIteration(iteration);
                        resultWithNullFieldsCollectorsSimRunAndPath.setPeriod(i);
                         result.add(resultWithNullFieldsCollectorsSimRunAndPath);
                     }
                 }
             } catch (Exception e) {
                 throw new RiskAnalyticsResultAccessException("Failed to get iteration : " + ifa.getIteration(), e);
             }
         }
         return result;
     }
 
 }
