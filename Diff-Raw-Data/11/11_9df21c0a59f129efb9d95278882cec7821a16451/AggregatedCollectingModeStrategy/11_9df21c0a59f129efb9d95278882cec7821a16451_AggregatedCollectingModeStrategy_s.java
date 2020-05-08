 package org.pillarone.riskanalytics.core.output;
 
 import org.pillarone.riskanalytics.core.output.aggregation.IPacketAggregator;
 import org.pillarone.riskanalytics.core.output.aggregation.PacketAggregatorRegistry;
 import org.pillarone.riskanalytics.core.packets.Packet;
 import org.pillarone.riskanalytics.core.packets.PacketList;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 public class AggregatedCollectingModeStrategy extends AbstractCollectingModeStrategy {
 
     static final String IDENTIFIER = "AGGREGATED";
 
     private static final String RESOURCE_BUNDLE = "org.pillarone.riskanalytics.core.output.applicationResources";
     private String displayName;
 
 
     public List<SingleValueResultPOJO> collect(PacketList packets) throws IllegalAccessException {
         IPacketAggregator<Packet> sumAggregator = PacketAggregatorRegistry.getAggregator(packets.get(0).getClass());
         Packet aggregatedPacket = sumAggregator.aggregate(packets);
         return createSingleValueResults((Packet) packets.get(0), aggregatedPacket.getValuesToSave(), 0);
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
 
 }
