 package eu.uberdust.formatter;
 
import eu.uberdust.caching.Cachable;
 import eu.uberdust.formatter.exception.NotImplementedException;
 import eu.wisebed.wisedb.model.*;
 import org.apache.log4j.Logger;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Implements the {@link Formatter} Interface and converts Wisedb Objects to Raw Text.
  *
  * @author amaxilat
  */
 public class TextFormatter implements Formatter {
     /**
      * LOGGER.
      */
     private static final Logger LOGGER = Logger.getLogger(TextFormatter.class);
     /**
      * Singleton Instance.
      */
     private static TextFormatter instance = new TextFormatter();
     /**
      * Base Url to use with url links.
      */
     private static String baseUrl = "";
 
     /**
      * Returns a {@link TextFormatter} instance.
      *
      * @return the {@link TextFormatter} instance.
      */
     public static TextFormatter getInstance() {
         return instance;
     }
 
     @Override
     public void setBaseUrl(String baseUrl) {
         this.baseUrl = baseUrl;
     }
 
     @Override
     public final String formatTestbed(final Testbed testbed) throws NotImplementedException {
         LOGGER.info("formatTestbed");
         throw new NotImplementedException();
     }
 
     @Override
     public String formatNode(Node node) throws NotImplementedException {
         throw new NotImplementedException();
     }
 
     @Override
     public String formatLink(Link link) throws NotImplementedException {
         throw new NotImplementedException();
     }
 
     @Override
     public final String formatTestbeds(final List<Testbed> testbeds, final Map<String, Long> nodesCount,
                                        final Map<String, Long> linksCount) throws NotImplementedException {
         LOGGER.info("formatTestbeds");
         throw new NotImplementedException();
     }
 
     @Override
     public final String formatLinks(final List<Link> links) throws NotImplementedException {
         LOGGER.info("formatLinks");
 
         final StringBuilder output = new StringBuilder();
 
         for (final Link link : links) {
             output.append("[").append(link.getSource().getName()).append(",");
             output.append(link.getTarget().getName()).append("]");
             output.append(NEW_LINE);
         }
 
         return output.toString();
     }
 
     @Override
     public String formatLinkCapabilities(List<LinkCapability> linkCapabilities) throws NotImplementedException {
         throw new NotImplementedException();
     }
 
     @Override
     public String formatCapability(final Testbed testbed, final Capability capability) throws NotImplementedException {
         throw new NotImplementedException();
     }
 
     @Override
     public String formatCapabilities(final Testbed testbed, final List<Capability> capabilities)
             throws NotImplementedException {
         LOGGER.info("formatCapabilities");
 
         final StringBuilder output = new StringBuilder();
 
         for (final Capability capability : capabilities) {
             output.append(capability.getName());
             output.append(NEW_LINE);
         }
 
         return output.toString();
     }
 
     @Override
     public final String formatLastReadings(final List<LastNodeReading> lastNodeReadings,
                                            final List<LastLinkReading> lastLinkReadings)
             throws NotImplementedException {
         LOGGER.info("formatLastReadings");
 
         final StringBuilder output = new StringBuilder();
         if (lastNodeReadings != null) {
             for (final LastNodeReading lnr : lastNodeReadings) {
 
                 if (lnr.getTimestamp() != null) {
                     output.append(lnr.getNodeCapability().getNode().getName()).append(TAB);
                     output.append(lnr.getTimestamp().getTime()).append(TAB);
                     if (lnr.getReading() != null) {
                         output.append(lnr.getReading().toString()).append(TAB);
                     } else {
                         output.append(NULL).append(TAB);
                     }
                     if (lnr.getStringReading() != null) {
                         output.append(lnr.getStringReading()).append(TAB);
                     } else {
                         output.append(NULL).append(TAB);
                     }
                     output.append(NEW_LINE);
                 }
             }
         }
         if (lastLinkReadings != null) {
             for (final LastLinkReading llr : lastLinkReadings) {
                 output.append("[").append(llr.getLinkCapability().getLink().getSource().getName()).append(",");
 
                 if (llr.getTimestamp() != null) {
                     output.append(llr.getLinkCapability().getLink().getTarget().getName()).append("]").append(TAB);
                     output.append(llr.getTimestamp().getTime()).append(TAB);
                     if (llr.getReading() != null) {
                         output.append(llr.getReading().toString()).append(TAB);
                     } else {
                         output.append(NULL).append(TAB);
                     }
                     if (llr.getStringReading() != null) {
                         output.append(llr.getStringReading()).append(TAB);
                     } else {
                         output.append(NULL).append(TAB);
                     }
                     output.append(NEW_LINE);
                 }
             }
         }
         return output.toString();
     }
 
     @Override
     public final String describeNode(final Node node, final String requestURL, final String requestURI,
                                      final String nodeDescription, final Position nodePos)
             throws NotImplementedException {
         throw new NotImplementedException();
     }
 
     @Override
     public final String describeTestbed(final Testbed testbed, final String requestURL, final String requestURI,
                                         final List<Node> nodes, final Map<Node, String> descriptionMap,
                                         final Map<Node, List<NodeCapability>> capabilityMap,
                                         final Map<Node, Position> originMap) throws NotImplementedException {
         throw new NotImplementedException();
     }
 
     @Override
     public String showTestbed(Testbed testbed, List<Node> nodes, List<Link> links, List<Capability> capabilities) throws NotImplementedException {
         throw new NotImplementedException();
     }
 
     @Override
     public String describeNodeCapabilities(List<NodeCapability> capabilities) throws NotImplementedException {
         throw new NotImplementedException();
     }
 
     @Override
     public final String formatNodes(final List<Node> nodes) throws NotImplementedException {
         LOGGER.info("formatNodes");
 
         final StringBuilder output = new StringBuilder();
 
         for (final Node node : nodes) {
             output.append(node.getName());
             output.append(NEW_LINE);
         }
 
         return output.toString();
     }
 
     @Override
     public final String formatNodeReadings(final List<NodeReading> nodeReadings) throws NotImplementedException {
         LOGGER.info("formatNodeReadings");
 
         final StringBuilder output = new StringBuilder();
 
         for (final NodeReading nr : nodeReadings) {
 
             output.append(nr.getTimestamp().getTime()).append(TAB);
             if (nr.getReading() != null) {
                 output.append(nr.getReading().toString()).append(TAB);
             }
             if (nr.getStringReading() != null) {
                 output.append(nr.getStringReading()).append(TAB);
             }
             output.append(NEW_LINE);
         }
 
         return output.toString();
     }
 
     @Override
    @Cachable
     public String formatLastNodeReadings(List<NodeCapability> nodeCapabilities) throws NotImplementedException {
         LOGGER.info("formatLastNodeReadings");
         final StringBuilder output = new StringBuilder();
         for (NodeCapability capability : nodeCapabilities) {
             if (capability.getLastNodeReading().getTimestamp() != null) {
                 output.append(capability.getNode().getName()).append(TAB);
                 output.append(capability.getCapability().getName()).append(TAB);
 
                 output.append(capability.getLastNodeReading().getTimestamp().getTime()).append(TAB);
                 if (capability.getLastNodeReading().getReading() != null) {
                     output.append(capability.getLastNodeReading().getReading()).append(TAB);
                 }
                 if (capability.getLastNodeReading().getStringReading() != null) {
                     output.append(capability.getLastNodeReading().getStringReading()).append(TAB);
                 }
                 output.append(NEW_LINE);
             }
         }
         return output.toString();
     }
 
     @Override
     public final String formatNodeReading(final LastNodeReading nodeReading) throws NotImplementedException {
         LOGGER.info("formatNodeReading");
 
         final StringBuilder output = new StringBuilder();
 
         output.append(nodeReading.getTimestamp().getTime()).append(TAB);
         if (nodeReading.getReading() != null) {
             output.append(nodeReading.getReading().toString()).append(TAB);
         }
         if (nodeReading.getStringReading() != null) {
             output.append(nodeReading.getStringReading()).append(TAB);
 
             output.append(NEW_LINE);
         }
 
         return output.toString();
     }
 
     @Override
     public final String formatUniqueLastNodeReadings(final List<NodeCapability> nodeCapabilities)
             throws NotImplementedException {
         LOGGER.info("formatUniqueLastNodeReadings");
 
         final StringBuilder output = new StringBuilder();
 
         final Map<String, Integer> uniqueRooms = new HashMap<String, Integer>();
 
         for (final NodeCapability nodeCapability : nodeCapabilities) {
             uniqueRooms.put(nodeCapability.getLastNodeReading().getStringReading(), 1);
         }
 
         for (final String room : uniqueRooms.keySet()) {
             output.append(room).append(NEW_LINE);
         }
 
         return output.toString();
     }
 }
