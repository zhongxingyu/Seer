 package org.pillarone.riskanalytics.core.output;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.joda.time.DateTime;
 import org.pillarone.riskanalytics.core.packets.Packet;
 import org.pillarone.riskanalytics.core.simulation.SimulationException;
 import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 abstract public class AbstractCollectingModeStrategy implements ICollectingModeStrategy {
 
     protected PacketCollector packetCollector;
 
     private Log LOG = LogFactory.getLog(AbstractCollectingModeStrategy.class);
 
     /**
      *
      * @param packet        Period information in following packets is ignored. If no period information is found the
      *                      current period of the packetCollector is used.
      * @param valueMap      field, value map
      * @param valueIndex    Used when aggregating single packets
      * @param crashSimOnError
      * @return
      */
     protected List<SingleValueResultPOJO> createSingleValueResults(Packet packet, Map<String, Number> valueMap, int valueIndex, boolean crashSimOnError) {
         PeriodScope periodScope = packetCollector.getSimulationScope().getIterationScope().getPeriodScope();
         return createSingleValueResults(valueMap, valueIndex, getPeriod(packet, periodScope), getDate(packet, periodScope), crashSimOnError);
     }
 
     /**
      * @param packet
      * @param periodScope
      * @return period property of the packet if set or the current period
      */
     private int getPeriod(Packet packet, PeriodScope periodScope) {
         int period = periodScope.getCurrentPeriod();
         if (packet != null && packet.period != null) {
             period = packet.period;
         }
         return period;
     }
 
     /**
      *
      * @param packet
      * @param periodScope
      * @return date property of packet if SingleValueCollectingModeStrategy used or beginning of current period
      */
     private DateTime getDate(Packet packet, PeriodScope periodScope) {
         DateTime date = null;
         if (packetCollector.getMode() instanceof SingleValueCollectingModeStrategy
                 && packet != null && packet.getDate() != null) {
             date = packet.getDate();
         }
         else if (periodScope.getPeriodCounter() != null) {
             date = periodScope.getCurrentPeriodStartDate();
         }
         return date;
     }
 
     /**
      * Create a SingleValueResult object for each packetValue.
      * Information about current simulation is gathered from the scopes.
      * The key of the value map is the field name.
      * If a value is infinite or NaN a log statement is created and the packet ignored.
      */
     private List<SingleValueResultPOJO> createSingleValueResults(Map<String, Number> valueMap, int valueIndex, int period, DateTime date, boolean crashSimOnError) {
         List<SingleValueResultPOJO> results = new ArrayList(valueMap.size());
         int iteration = packetCollector.getSimulationScope().getIterationScope().getCurrentIteration();
         PathMapping path = packetCollector.getSimulationScope().getMappingCache().lookupPath(packetCollector.getPath());
         for (Map.Entry<String, Number> entry : valueMap.entrySet()) {
             String name = entry.getKey();
             Double value = entry.getValue().doubleValue();
             SingleValueResultPOJO result = new SingleValueResultPOJO();
             if (checkInvalidValues(name, value, period, iteration, crashSimOnError)) continue;
            result.setSimulationRun(packetCollector.getSimulationScope().getSimulation().getSimulationRun());
             result.setIteration(iteration);
             result.setPeriod(period);
             result.setPath(path);
             result.setCollector(packetCollector.getSimulationScope().getMappingCache().lookupCollector(packetCollector.getMode().getIdentifier()));
             result.setField(packetCollector.getSimulationScope().getMappingCache().lookupField(name));
             result.setValueIndex(valueIndex);
             result.setValue(value);
             result.setDate(date);
             results.add(result);
         }
         return results;
     }
 
     public boolean checkInvalidValues(String name, Double value, int period, int iteration, boolean crashSimulationOnError) {
         if (value.isInfinite() || value.isNaN()) {
             StringBuilder message = new StringBuilder();
             message.append(value).append(" collected at ").append(packetCollector.getPath()).append(":").append(name);
             message.append(" Period : ").append(period).append( " in Iteration : " ).append(iteration);
             if (LOG.isErrorEnabled()) {
                 LOG.info(message.toString());
             }
             if(crashSimulationOnError) {
                 throw new SimulationException(message.toString() + " : insanity detected; killing simulation.");
             }
             return true;
         }
         return false;
     }
 
     public PacketCollector getPacketCollector() {
         return packetCollector;
     }
 
     public void setPacketCollector(PacketCollector packetCollector) {
         this.packetCollector = packetCollector;
     }
 }
