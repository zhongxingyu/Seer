 package org.pillarone.riskanalytics.domain.pc.output;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.pillarone.riskanalytics.core.components.ComposedComponent;
 import org.pillarone.riskanalytics.core.components.DynamicComposedComponent;
 import org.pillarone.riskanalytics.core.components.IComponentMarker;
 import org.pillarone.riskanalytics.core.output.*;
 import org.pillarone.riskanalytics.core.packets.Packet;
 import org.pillarone.riskanalytics.core.packets.PacketList;
 import org.pillarone.riskanalytics.core.simulation.engine.MappingCache;
 import org.pillarone.riskanalytics.domain.pc.constants.ClaimType;
 import org.pillarone.riskanalytics.domain.pc.filter.SegmentFilter;
 import org.pillarone.riskanalytics.domain.utils.marker.IPerilMarker;
 import org.pillarone.riskanalytics.domain.utils.marker.ISegmentMarker;
 import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.IReinsuranceContractMarker;
 import org.pillarone.riskanalytics.domain.pc.claims.Claim;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfo;
 import org.pillarone.riskanalytics.domain.utils.MarkerKeyPair;
 
 import java.util.*;
 
 /**
  * @author stefan.kunz (at) intuitive-collaboration (dot) com
  */
 public class AggregateDrillDownCollectingModeStrategy implements ICollectingModeStrategy {
 
     protected static Log LOG = LogFactory.getLog(AggregateDrillDownCollectingModeStrategy.class);
 
     static final String IDENTIFIER = "AGGREGATED_DRILL_DOWN";
     private static final String PERILS = "claimsGenerators";
     private static final String CONTRACTS = "reinsuranceContracts";
     private static final String LOB = "linesOfBusiness";
     private static final String RESOURCE_BUNDLE = "org.pillarone.riskanalytics.domain.pc.output.AggregateDrillDownCollectingModeStrategyResources";
     private static final String PATH_SEPARATOR = ":";
     private String displayName;
 
     private PacketCollector packetCollector;
 
     // the following variables are used for caching purposes
     private SimulationRun simulationRun;
     private String componentPath;
     private Map<IComponentMarker, PathMapping> markerPaths;
     private Map<MarkerKeyPair, PathMapping> markerComposedPaths;
     private MappingCache mappingCache;
     private int iteration = 0;
     private int period = 0;
 
     private void initSimulation() {
         if (simulationRun != null) return;
         simulationRun = packetCollector.getSimulationScope().getSimulation().getSimulationRun();
         componentPath = getComponentPath();
         markerPaths = new HashMap<IComponentMarker, PathMapping>();
         markerComposedPaths = new HashMap<MarkerKeyPair, PathMapping>();
         mappingCache = packetCollector.getSimulationScope().getMappingCache();
     }
 
     public List<SingleValueResultPOJO> collect(PacketList packets) {
         initSimulation();
         iteration = packetCollector.getSimulationScope().getIterationScope().getCurrentIteration();
         period = packetCollector.getSimulationScope().getIterationScope().getPeriodScope().getCurrentPeriod();
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
             String notImplemented = ResourceBundle.getBundle(RESOURCE_BUNDLE).getString("AggregateDrillDownCollectingModeStrategy.notImplemented");
            throw new NotImplementedException(notImplemented);
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
     private List<SingleValueResultPOJO> createSingleValueResults(Map<PathMapping, Packet> packets) throws IllegalAccessException {
         List<SingleValueResultPOJO> singleValueResults = new ArrayList<SingleValueResultPOJO>(packets.size());
         boolean firstPath = true;
         for (Map.Entry<PathMapping, Packet> packetEntry : packets.entrySet()) {
             PathMapping path = packetEntry.getKey();
             Packet packet = packetEntry.getValue();
             for (Map.Entry<String, Number> field : packet.getValuesToSave().entrySet()) {
                 String fieldName = field.getKey();
                 Double value = (Double) field.getValue();
                 if (value == Double.NaN || value == Double.NEGATIVE_INFINITY || value == Double.POSITIVE_INFINITY) {
                     if (LOG.isErrorEnabled()) {
                         StringBuilder message = new StringBuilder();
                         message.append(value).append(" collected at ").append(packetCollector.getPath());
                         message.append(" (period ").append(period).append(") in iteration ");
                         message.append(iteration).append(" - ignoring.");
                         LOG.error(message);
                     }
                     continue;
                 }
                 SingleValueResultPOJO result = new SingleValueResultPOJO();
                 result.setSimulationRun(simulationRun);
                 result.setIteration(iteration);
                 result.setPeriod(period);
                 result.setPath(path);
                 if (firstPath) {    // todo(sku): might be completely removed
                     result.setCollector(mappingCache.lookupCollector("AGGREGATED"));
                 }
                 else {
                     result.setCollector(mappingCache.lookupCollector(IDENTIFIER));
                 }
                 result.setField(mappingCache.lookupField(fieldName));
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
     private Map<PathMapping, Packet> aggregateClaims(List<Claim> claims) {
         // has to be a LinkedHashMap to make sure the shortest path is the first in the map and gets AGGREGATED as collecting mode
         Map<PathMapping, Packet> resultMap = new LinkedHashMap<PathMapping, Packet>(claims.size());
         if (claims == null || claims.size() == 0) {
             return resultMap;
         }
 
         for (Claim claim : claims) {
             String originPath = packetCollector.getSimulationScope().getStructureInformation().getPath(claim);
             PathMapping path = mappingCache.lookupPath(originPath);
             addToMap(claim, path, resultMap);
 
             PathMapping perilPath = getPathMapping(claim, claim.getPeril(), PERILS);
             PathMapping lobPath = null;
             if (!(claim.sender instanceof ISegmentMarker)) {
                 lobPath = getPathMapping(claim, claim.getLineOfBusiness(), LOB);
             }
             PathMapping contractPath = null;
             if (!(claim.sender instanceof IReinsuranceContractMarker)) {
                 contractPath = getPathMapping(claim, claim.getReinsuranceContract(), CONTRACTS);
             }
             if (claim.sender instanceof ISegmentMarker) {
                 addToMap(claim, perilPath, resultMap);
                 addToMap(claim, contractPath, resultMap);
             }
             if (claim.sender instanceof IReinsuranceContractMarker) {
                 addToMap(claim, lobPath, resultMap);
                 addToMap(claim, perilPath, resultMap);
             }
             if (claim.sender instanceof SegmentFilter) {
                 addToMap(claim, perilPath, resultMap);
                 addToMap(claim, lobPath, resultMap);
                 addToMap(claim, contractPath, resultMap);
                 if (perilPath != null && lobPath != null) {
                     PathMapping lobPerilPath = getPathMapping(claim, claim.getLineOfBusiness(), LOB, claim.getPeril(), PERILS);
                     addToMap(claim, lobPerilPath, resultMap);
                 }
                 if (perilPath != null && contractPath != null) {
                     PathMapping contractPerilPath = getPathMapping(claim, claim.getReinsuranceContract(), CONTRACTS,
                             claim.getPeril(), PERILS);
                     addToMap(claim, contractPerilPath, resultMap);
                 }
                 if (lobPath != null && contractPath != null) {
                     PathMapping lobContractPath = getPathMapping(claim, claim.getLineOfBusiness(), LOB,
                             claim.getReinsuranceContract(), CONTRACTS);
                     addToMap(claim, lobContractPath, resultMap);
                 }
                 if (perilPath != null && lobPath != null && contractPath != null) {
                     PathMapping lobContractPerilPath = getPathMapping(claim, claim.getLineOfBusiness(), LOB,
                             claim.getReinsuranceContract(), CONTRACTS, claim.getPeril(), PERILS);
                     addToMap(claim, lobContractPerilPath, resultMap);
                 }
             }
         }
         return resultMap;
     }
 
     private PathMapping getPathMapping(Packet packet, IComponentMarker marker, String pathExtensionPrefix) {
         PathMapping path = markerPaths.get(marker);
         if (marker != null && path == null) {
             String pathExtension = pathExtensionPrefix + PATH_SEPARATOR + marker.getName();
             String pathExtended = getExtendedPath(packet, pathExtension);
             path = mappingCache.lookupPath(pathExtended);
             markerPaths.put(marker, path);
         }
         return path;
     }
 
     private PathMapping getPathMapping(Packet packet,
                                        IComponentMarker firstMarker, String firstPathExtensionPrefix,
                                        IComponentMarker secondMarker, String secondPathExtensionPrefix) {
         MarkerKeyPair pair = new MarkerKeyPair(firstMarker, secondMarker);
         PathMapping path = markerComposedPaths.get(pair);
         if (firstMarker != null && path == null) {
             String pathExtension = firstPathExtensionPrefix + PATH_SEPARATOR + firstMarker.getName()
                     + PATH_SEPARATOR + secondPathExtensionPrefix + PATH_SEPARATOR + secondMarker.getName();
             String pathExtended = getExtendedPath(packet, pathExtension);
             path = mappingCache.lookupPath(pathExtended);
             markerComposedPaths.put(pair, path);
         }
         return path;
     }
 
     private PathMapping getPathMapping(Packet packet,
                                        IComponentMarker firstMarker, String firstPathExtensionPrefix,
                                        IComponentMarker secondMarker, String secondPathExtensionPrefix,
                                        IComponentMarker thirdMarker, String thirdPathExtensionPrefix) {
         MarkerKeyPair pair = new MarkerKeyPair(firstMarker, secondMarker, thirdMarker);
         PathMapping path = markerComposedPaths.get(pair);
         if (firstMarker != null && path == null) {
             String pathExtension = firstPathExtensionPrefix + PATH_SEPARATOR + firstMarker.getName()
                     + PATH_SEPARATOR + secondPathExtensionPrefix + PATH_SEPARATOR + secondMarker.getName()
                     + PATH_SEPARATOR + thirdPathExtensionPrefix + PATH_SEPARATOR + thirdMarker.getName();
             String pathExtended = getExtendedPath(packet, pathExtension);
             path = mappingCache.lookupPath(pathExtended);
             markerComposedPaths.put(pair, path);
         }
         return path;
     }
 
 
     /**
      * @param underwritingInfos
      * @return a map with paths as key
      */
     private Map<PathMapping, Packet> aggregateUnderwritingInfo(List<UnderwritingInfo> underwritingInfos) {
         Map<PathMapping, Packet> resultMap = new HashMap<PathMapping, Packet>(underwritingInfos.size());
         if (underwritingInfos == null || underwritingInfos.size() == 0) {
             return resultMap;
         }
 
         for (UnderwritingInfo underwritingInfo : underwritingInfos) {
             String originPath = packetCollector.getSimulationScope().getStructureInformation().getPath(underwritingInfo);
             PathMapping path = mappingCache.lookupPath(originPath);
             addToMap(underwritingInfo, path, resultMap);
 
             PathMapping lobPath = null;
             if (!(underwritingInfo.sender instanceof ISegmentMarker)) {
                 lobPath = getPathMapping(underwritingInfo, underwritingInfo.getLineOfBusiness(), LOB);
             }
             PathMapping contractPath = null;
             if (!(underwritingInfo.sender instanceof IReinsuranceContractMarker)) {
                 contractPath = getPathMapping(underwritingInfo, underwritingInfo.getReinsuranceContract(), CONTRACTS);
             }
             if (underwritingInfo.sender instanceof ISegmentMarker) {
                 addToMap(underwritingInfo, contractPath, resultMap);
             }
             if (underwritingInfo.sender instanceof IReinsuranceContractMarker) {
                 addToMap(underwritingInfo, lobPath, resultMap);
             }
             if (underwritingInfo.sender instanceof SegmentFilter) {
                 addToMap(underwritingInfo, contractPath, resultMap);
                 addToMap(underwritingInfo, lobPath, resultMap);
                 if (lobPath != null && contractPath != null) {
                     PathMapping lobContractPath = getPathMapping(underwritingInfo,
                             underwritingInfo.getLineOfBusiness(), LOB,
                             underwritingInfo.getReinsuranceContract(), CONTRACTS);
                     addToMap(underwritingInfo, lobContractPath, resultMap);
                 }
             }
         }
         return resultMap;
     }
 
     private String getComponentPath() {
         int separatorPositionBeforeChannel = packetCollector.getPath().lastIndexOf(":");
         return packetCollector.getPath().substring(0, separatorPositionBeforeChannel);
     }
 
     private void addToMap(Claim claim, PathMapping path, Map<PathMapping, Packet> resultMap) {
         if (path == null) return;
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
 
     private void addToMap(UnderwritingInfo underwritingInfo, PathMapping path, Map<PathMapping, Packet> resultMap) {
         if (path == null) return;
         if (resultMap.containsKey(path)) {
             UnderwritingInfo aggregateUnderwritingInfo = (UnderwritingInfo) resultMap.get(path);
             aggregateUnderwritingInfo.plus(underwritingInfo);
             resultMap.put(path, aggregateUnderwritingInfo);
         } else {
             UnderwritingInfo clonedUnderwritingInfo = (UnderwritingInfo) underwritingInfo.copy();
             resultMap.put(path, clonedUnderwritingInfo);
         }
     }
 
     private String getExtendedPath(Packet packet, String pathExtension) {
         if (pathExtension == null) return null;
         StringBuilder composedPath = new StringBuilder(componentPath);
         composedPath.append(PATH_SEPARATOR);
         composedPath.append(pathExtension);
         composedPath.append(PATH_SEPARATOR);
         composedPath.append(packet.senderChannelName);
         return composedPath.toString();
     }
 
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
