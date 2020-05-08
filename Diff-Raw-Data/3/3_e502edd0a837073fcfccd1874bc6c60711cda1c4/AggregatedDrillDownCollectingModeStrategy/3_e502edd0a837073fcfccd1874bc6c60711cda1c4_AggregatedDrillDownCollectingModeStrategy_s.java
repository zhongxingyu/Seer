 package org.pillarone.riskanalytics.domain.pc.output;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.pillarone.riskanalytics.core.output.ICollectingModeStrategy;
 import org.pillarone.riskanalytics.core.output.PacketCollector;
 import org.pillarone.riskanalytics.core.output.SingleValueResult;
 import org.pillarone.riskanalytics.core.output.batch.AbstractBulkInsert;
 import org.pillarone.riskanalytics.core.packets.PacketList;
 import org.pillarone.riskanalytics.domain.pc.constants.ClaimType;
 import org.pillarone.riskanalytics.domain.pc.lob.LobMarker;
 import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.IReinsuranceContractMarker;
 import org.pillarone.riskanalytics.domain.pc.claims.Claim;
 
 import java.util.*;
 
 /**
  * @author stefan.kunz (at) intuitive-collaboration (dot) com
  */
 public class AggregatedDrillDownCollectingModeStrategy implements ICollectingModeStrategy {
 
     protected static Log LOG = LogFactory.getLog(AggregatedDrillDownCollectingModeStrategy.class);
 
     static final String IDENTIFIER = "AGGREGATED_DRILL_DOWN";
     private static final String RESOURCE_BUNDLE = "org.pillarone.riskanalytics.domain.pc.application.applicationResources";
     private static final String PATH_SEPARATOR = ":";
     private String displayName;
 
     private PacketCollector packetCollector;
 
     public List<SingleValueResult> collect(PacketList packets) { //throws IllegalAccessException {
         if (packets.get(0) instanceof Claim) {
             try {
                 return createSingleValueResults(aggregateClaims(packets));
             }
             catch (IllegalAccessException ex) {
 //                todo(sku): remove
             }
         }
         else {
            throw new NotImplementedException("The aggregate drill down mode is only available for claims");
         }
         return null;
     }
 
     /**
      * Create a SingleValueResult object for each packetValue.
      * Information about current simulation is gathered from the scopes.
      * The key of the value map is the path.
      */
     private List createSingleValueResults(Map<String, Claim> packets) throws IllegalAccessException {
         List<SingleValueResult> singleValueResults = new ArrayList<SingleValueResult>(packets.size());
         for (Map.Entry<String, Claim> packet : packets.entrySet()) {
             String path = packet.getKey();
             Claim claim = packet.getValue();
             for (Map.Entry<String, Number> field : claim.getValuesToSave().entrySet()) {
                 String fieldName = field.getKey();
                 Double value = (Double) field.getValue();
                 if (value == Double.NaN || value == Double.NEGATIVE_INFINITY || value == Double.POSITIVE_INFINITY) {
                     int currentPeriod = packetCollector.getSimulationScope().getIterationScope().getPeriodScope().getCurrentPeriod();
                     int currentIteration = packetCollector.getSimulationScope().getIterationScope().getCurrentIteration();
                     if (LOG.isErrorEnabled()) {
                         StringBuilder message = new StringBuilder();
                         message.append(value).append(" collected at ").append(packetCollector.getPath());
                         message.append(" (period ").append(currentPeriod).append(") in iteration ");
                         message.append(currentIteration).append(" - ignoring.");
                         LOG.error(message);
                     }
                     continue;
                 }
                 SingleValueResult result = new SingleValueResult();
                 // correct syntax for master branch (0.6)
                 result.setSimulationRun(packetCollector.getSimulationScope().getSimulation().getSimulationRun());
                 // correct syntax for 0.5.x branch
 //                result.setSimulationRun(packetCollector.getSimulationScope().getSimulationRun());
                 result.setIteration(packetCollector.getSimulationScope().getIterationScope().getCurrentIteration());
                 result.setPeriod(packetCollector.getSimulationScope().getIterationScope().getPeriodScope().getCurrentPeriod());
                 result.setPath(packetCollector.getSimulationScope().getMappingCache().lookupPath(path));
                 result.setCollector(packetCollector.getSimulationScope().getMappingCache().lookupCollector(AbstractBulkInsert.DEFAULT_COLLECTOR_NAME));
                 result.setField(packetCollector.getSimulationScope().getMappingCache().lookupField(fieldName));
                 result.setValueIndex(0);
                 result.setValue(value);
                 singleValueResults.add(result);
             }
         }
         return singleValueResults;
     }
 
     /**
      * @param claims
      * @return a map with paths as key
      */
     private Map<String, Claim> aggregateClaims(List<Claim> claims) {
         Map<String, Claim> resultMap = new HashMap<String, Claim>(claims.size());
         if (claims == null || claims.size() == 0) {
             return resultMap;
         }
 
         for (Claim claim : claims) {
             String originPath = packetCollector.getSimulationScope().getStructureInformation().getPath(claim);
 //            System.out.println("++P++" + originPath);
             addToMap(claim, originPath, resultMap);
             if (claim.sender instanceof LobMarker && claim.getPeril() != null) {
                 String perilPathExtension = claim.getPeril().getName();
 //                System.out.println(" L " +perilPathExtension + "(LobMarker)");
                 addToMap(claim, getComponentPath(), perilPathExtension, resultMap);
             }
             if (claim.sender instanceof LobMarker && claim.getReinsuranceContract() != null) {
                 String contractPathExtension = claim.getReinsuranceContract().getName();
 //                System.out.println(" L " +contractPathExtension + "(LobMarker)");
                 addToMap(claim, getComponentPath(), contractPathExtension, resultMap);
             }
             if (claim.sender instanceof IReinsuranceContractMarker && claim.getLineOfBusiness() != null) {
                 String lobPathExtension = claim.getLineOfBusiness().getName();
 //                System.out.println(" R " +lobPathExtension + "(IReinsuranceContractMarker)");
                 addToMap(claim, getComponentPath(), lobPathExtension, resultMap);
             }
             if (claim.sender instanceof IReinsuranceContractMarker && claim.getPeril() != null) {
                 String perilPathExtension = claim.getPeril().getName();
 //                System.out.println(" R " +perilPathExtension + "(IReinsuranceContractMarker)");
                 addToMap(claim, getComponentPath(), perilPathExtension, resultMap);
             }
         }
         return resultMap;
     }
 
     private String getComponentPath() {
         int separatorPositionBeforeChannel = packetCollector.getPath().lastIndexOf(":");
         return packetCollector.getPath().substring(0, separatorPositionBeforeChannel);
     }
 
     private void addToMap(Claim claim, String path, Map<String, Claim> resultMap) {
         if (resultMap.containsKey(path)) {
             Claim aggregateClaim = resultMap.get(path);
             aggregateClaim.plus(claim);
             resultMap.put(path, aggregateClaim);
         } else {
             Claim clonedClaim = claim.copy();
             clonedClaim.setClaimType(ClaimType.AGGREGATED);
             resultMap.put(path, clonedClaim);
         }
     }
 
     // todo(sku): cache extended paths
     private void addToMap(Claim claim, String path, String pathExtension, Map<String, Claim> resultMap) {
         StringBuilder composedPath = new StringBuilder(path);
         composedPath.append(PATH_SEPARATOR);
         composedPath.append(pathExtension);
         composedPath.append(PATH_SEPARATOR);
         composedPath.append(claim.senderChannelName);
         addToMap(claim, composedPath.toString(), resultMap);
     }
 
 //    PathMapping getPathMapping(String extendedPath) {
 //        PathMapping extendedPathMapping = packetCollector.getSimulationScope().getMappingCache().lookupPath(extendedPath);
 //        if (extendedPathMapping == null) {
 //            // todo(sku): discuss with msp
 //            extendedPathMapping = new PathMapping(extendedPath);
 //            assert extendedPathMapping.save();
 //        }
 //        return extendedPathMapping;
 ////        return null;
 //    }
 
     public String getDisplayName(Locale locale) {
         if (displayName == null) {
             displayName = ResourceBundle.getBundle(RESOURCE_BUNDLE, locale).getString("ICollectingModeStrategy."+IDENTIFIER);
         }
         return displayName;
     }
 
     public String getIdentifier() {
         return IDENTIFIER;
     }
 
     public PacketCollector getPacketCollector() {
         return packetCollector;
     }
 
     public void setPacketCollector(PacketCollector packetCollector) {
         this.packetCollector = packetCollector;
     }
 }
