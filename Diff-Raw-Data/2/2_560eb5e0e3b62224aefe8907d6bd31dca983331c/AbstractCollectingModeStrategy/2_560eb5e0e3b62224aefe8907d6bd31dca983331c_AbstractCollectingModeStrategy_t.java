 package org.pillarone.riskanalytics.core.output;
 
 import org.pillarone.riskanalytics.core.output.batch.AbstractBulkInsert;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 abstract public class AbstractCollectingModeStrategy implements ICollectingModeStrategy {
 
     protected PacketCollector packetCollector;
 
     /**
      * Create a SingleValueResult object for each packetValue.
      * Information about current simulation is gathered from the scopes.
      * The key of the value map is the field name.
      */
     protected List<SingleValueResult> createSingleValueResults(Map<String, Number> valueMap, int valueIndex) {
         List<SingleValueResult> results = new ArrayList(valueMap.size());
         for (Map.Entry<String, Number> entry : valueMap.entrySet()) {
             String name = entry.getKey();
             Double value = entry.getValue().doubleValue();
             SingleValueResult result = new SingleValueResult();
            result.setSimulationRun(packetCollector.getSimulationScope().getSimulation().getSimulationRun());
             result.setIteration(packetCollector.getSimulationScope().getIterationScope().getCurrentIteration());
             result.setPeriod(packetCollector.getSimulationScope().getIterationScope().getPeriodScope().getCurrentPeriod());
             result.setPath(packetCollector.getSimulationScope().getMappingCache().lookupPath(packetCollector.getPath()));
             result.setCollector(packetCollector.getSimulationScope().getMappingCache().lookupCollector(AbstractBulkInsert.DEFAULT_COLLECTOR_NAME));
             result.setField(packetCollector.getSimulationScope().getMappingCache().lookupField(name));
             result.setValueIndex(valueIndex);
             result.setValue(value);
             results.add(result);
         }
         return results;
     }
 
     public PacketCollector getPacketCollector() {
         return packetCollector;
     }
 
     public void setPacketCollector(PacketCollector packetCollector) {
         this.packetCollector = packetCollector;
     }
 }
