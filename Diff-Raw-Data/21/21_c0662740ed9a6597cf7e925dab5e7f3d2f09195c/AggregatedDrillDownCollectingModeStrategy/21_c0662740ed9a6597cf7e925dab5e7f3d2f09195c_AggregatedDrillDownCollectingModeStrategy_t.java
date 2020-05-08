 package org.pillarone.riskanalytics.domain.pc.output;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.pillarone.riskanalytics.core.output.*;
 import org.pillarone.riskanalytics.core.packets.Packet;
 import org.pillarone.riskanalytics.core.packets.PacketList;
 import org.pillarone.riskanalytics.domain.pc.constants.ClaimType;
 import org.pillarone.riskanalytics.domain.pc.lob.LobMarker;
 import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.IReinsuranceContractMarker;
 import org.pillarone.riskanalytics.domain.pc.claims.Claim;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfo;
 
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
 
     public List<SingleValueResultPOJO> collect(PacketList packets) {
         if (packets.get(0) instanceof Claim) {
             try {
                 return createSingleValueResults(aggregateClaims(packets));
             }
             catch (IllegalAccessException ex) {
 //                todo(sku): remove
             }
         } else if (packets.get(0) instanceof UnderwritingInfo) {
             try {
                 return createSingleValueResults(aggregateUnderwritingInfo(packets));
             }
             catch (IllegalAccessException ex) {
 //                  todo(sku): remove
             }
         } else {
             throw new NotImplementedException("The aggregate drill down mode is only available for claims and underwriting info.\n" +
                     "Please adjust the selected result template or select another one.");
         }
         return null;
     }
 
     /**
      * Create a SingleValueResult object for each packetValue.
      * Information about current simulation is gathered from the scopes.
      * The key of the value map is the path.
      *
      * @param packets
      * @return
      * @throws IllegalAccessException
      */
     private List<SingleValueResultPOJO> createSingleValueResults(Map<String, Packet> packets) throws IllegalAccessException {
         List<SingleValueResultPOJO> singleValueResults = new ArrayList<SingleValueResultPOJO>(packets.size());
        boolean firstPath = true;
         for (Map.Entry<String, Packet> packetEntry : packets.entrySet()) {
             String path = packetEntry.getKey();
             Packet packet = packetEntry.getValue();
             for (Map.Entry<String, Number> field : packet.getValuesToSave().entrySet()) {
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
                 SingleValueResultPOJO result = new SingleValueResultPOJO();
                 result.setSimulationRun(packetCollector.getSimulationScope().getSimulation().getSimulationRun());
                 result.setIteration(packetCollector.getSimulationScope().getIterationScope().getCurrentIteration());
                 result.setPeriod(packetCollector.getSimulationScope().getIterationScope().getPeriodScope().getCurrentPeriod());
                 result.setPath(packetCollector.getSimulationScope().getMappingCache().lookupPath(path));
                if (firstPath) {
                    result.setCollector(packetCollector.getSimulationScope().getMappingCache().lookupCollector("AGGREGATED"));
                }
                else {
                    result.setCollector(packetCollector.getSimulationScope().getMappingCache().lookupCollector(packetCollector.getMode().getIdentifier()));
                }
                 result.setField(packetCollector.getSimulationScope().getMappingCache().lookupField(fieldName));
                 result.setValueIndex(0);
                 result.setValue(value);
                 singleValueResults.add(result);
             }
            firstPath = false;
         }
         return singleValueResults;
     }
 
     /**
      * @param claims
      * @return a map with paths as key
      */
     private Map<String, Packet> aggregateClaims(List<Claim> claims) {
        // has to be a LinkedHashMap to make sure the shortest path is the first in the map and gets AGGREGATED as collecting mode
        Map<String, Packet> resultMap = new LinkedHashMap<String, Packet>(claims.size());
         if (claims == null || claims.size() == 0) {
             return resultMap;
         }
 
         for (Claim claim : claims) {
             String originPath = packetCollector.getSimulationScope().getStructureInformation().getPath(claim);
             addToMap(claim, originPath, resultMap);
             if (claim.sender instanceof LobMarker && claim.getPeril() != null) {
                 String perilPathExtension = claim.getPeril().getName();
                 addToMap(claim, getComponentPath(), perilPathExtension, resultMap);
             }
             if (claim.sender instanceof LobMarker && claim.getReinsuranceContract() != null) {
                 String contractPathExtension = claim.getReinsuranceContract().getName();
                 addToMap(claim, getComponentPath(), contractPathExtension, resultMap);
             }
             if (claim.sender instanceof IReinsuranceContractMarker && claim.getLineOfBusiness() != null) {
                 String lobPathExtension = claim.getLineOfBusiness().getName();
                 addToMap(claim, getComponentPath(), lobPathExtension, resultMap);
             }
             if (claim.sender instanceof IReinsuranceContractMarker && claim.getPeril() != null) {
                 String perilPathExtension = claim.getPeril().getName();
                 addToMap(claim, getComponentPath(), perilPathExtension, resultMap);
             }
         }
         return resultMap;
     }
 
     /**
      * @param underwritingInfos
      * @return a map with paths as key
      */
     private Map<String, Packet> aggregateUnderwritingInfo(List<UnderwritingInfo> underwritingInfos) {
         Map<String, Packet> resultMap = new HashMap<String, Packet>(underwritingInfos.size());
         if (underwritingInfos == null || underwritingInfos.size() == 0) {
             return resultMap;
         }
 
         for (UnderwritingInfo underwritingInfo : underwritingInfos) {
             String originPath = packetCollector.getSimulationScope().getStructureInformation().getPath(underwritingInfo);
             addToMap(underwritingInfo, originPath, resultMap);
             if (underwritingInfo.sender instanceof LobMarker && underwritingInfo.getReinsuranceContract() != null) {
                 String contractPathExtension = underwritingInfo.getReinsuranceContract().getName();
                 addToMap(underwritingInfo, getComponentPath(), contractPathExtension, resultMap);
             }
             if (underwritingInfo.sender instanceof IReinsuranceContractMarker && underwritingInfo.getLineOfBusiness() != null) {
                 String lobPathExtension = underwritingInfo.getLineOfBusiness().getName();
                 addToMap(underwritingInfo, getComponentPath(), lobPathExtension, resultMap);
             }
         }
         return resultMap;
     }
 
     private String getComponentPath() {
         int separatorPositionBeforeChannel = packetCollector.getPath().lastIndexOf(":");
         return packetCollector.getPath().substring(0, separatorPositionBeforeChannel);
     }
 
     private void addToMap(Claim claim, String path, Map<String, Packet> resultMap) {
         if (resultMap.containsKey(path)) {
             Claim aggregateClaim = (Claim) resultMap.get(path);
             aggregateClaim.plus(claim);
             resultMap.put(path, aggregateClaim);
         } else {
             Claim clonedClaim = claim.copy();
             clonedClaim.setClaimType(ClaimType.AGGREGATED);
             resultMap.put(path, clonedClaim);
         }
     }
 
     private void addToMap(UnderwritingInfo underwritingInfo, String path, Map<String, Packet> resultMap) {
         if (resultMap.containsKey(path)) {
             UnderwritingInfo aggregateUnderwritingInfo = (UnderwritingInfo) resultMap.get(path);
             aggregateUnderwritingInfo.plus(underwritingInfo);
             resultMap.put(path, aggregateUnderwritingInfo);
         } else {
             UnderwritingInfo clonedUnderwritingInfo = (UnderwritingInfo) underwritingInfo.copy();
             resultMap.put(path, clonedUnderwritingInfo);
         }
     }
 
     // todo(sku): cache extended paths
     private void addToMap(Claim claim, String path, String pathExtension, Map<String, Packet> resultMap) {
         StringBuilder composedPath = new StringBuilder(path);
         composedPath.append(PATH_SEPARATOR);
         composedPath.append(pathExtension);
         composedPath.append(PATH_SEPARATOR);
         composedPath.append(claim.senderChannelName);
         addToMap(claim, composedPath.toString(), resultMap);
     }
 
     // todo(sku): cache extended paths
     private void addToMap(UnderwritingInfo underwritingInfo, String path, String pathExtension, Map<String, Packet> resultMap) {
         StringBuilder composedPath = new StringBuilder(path);
         composedPath.append(PATH_SEPARATOR);
         composedPath.append(pathExtension);
         composedPath.append(PATH_SEPARATOR);
         composedPath.append(underwritingInfo.senderChannelName);
         addToMap(underwritingInfo, composedPath.toString(), resultMap);
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
             displayName = ResourceBundle.getBundle(RESOURCE_BUNDLE, locale).getString("ICollectingModeStrategy." + IDENTIFIER);
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
