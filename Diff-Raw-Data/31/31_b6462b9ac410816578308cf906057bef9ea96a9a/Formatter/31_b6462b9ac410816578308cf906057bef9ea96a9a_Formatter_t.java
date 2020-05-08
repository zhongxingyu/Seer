 package eu.uberdust.formatter;
 
 import eu.uberdust.formatter.exception.NotImplementedException;
 import eu.wisebed.wisedb.model.*;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * An interface that contains all possible transformations of wisedb objects.
  *
  * @author amaxilat
  */
 public interface Formatter {
     /**
      * the TAB literal.
      */
     String TAB = "\t";
     /**
      * the NEW LINE literal.
      */
     String NEW_LINE = "\n";
     /**
      * the NULL literal.
      */
     String NULL = "null";
 
     void setBaseUrl(final String baseUrl);
 
     /**
      * Formats a given {@link Testbed} object to the target format.
      *
      * @param testbed the {@link Testbed} to format.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String formatTestbed(final Testbed testbed) throws NotImplementedException;
 
     /**
      * Formats a given {@link Node} object to the target format.
      *
      * @param node the {@link Node} to format.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String formatNode(final Node node) throws NotImplementedException;
 
     /**
      * Formats a given {@link Link} object to the target format.
      *
      * @param link the {@link Link} to format.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String formatLink(final Link link) throws NotImplementedException;
 
     /**
      * Formats a given {@link LastNodeReading} object to the target format.
      *
      * @param lastNodeReading a {@link LastNodeReading} object.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String formatNodeReading(final LastNodeReading lastNodeReading) throws NotImplementedException;
 
     /**
      * Formats a given List of {@link Testbed} objects to the target format.
      *
      * @param testbeds a List of {@link Testbed} objects.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String formatTestbeds(final List<Testbed> testbeds, final Map<String, Long> nodesCount,
                           final Map<String, Long> linksCount) throws NotImplementedException;
 
     /**
      * Formats a given List of {@link LinkCapability} objects to the target format.
      *
      * @param linkCapabilities a list of {@link LinkCapability} objects.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String formatLinkCapabilities(final List<LinkCapability> linkCapabilities) throws NotImplementedException;
 
     String formatCapability(final Testbed testbed, final Capability capability) throws NotImplementedException;
 
     /**
      * Formats a given List of {@link Capability} objects to the target format.
      *
      * @param testbed      the testbed the capabilities belong to,
      * @param capabilities a list of {@link Capability} objects.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String formatCapabilities(final Testbed testbed, final List<Capability> capabilities) throws NotImplementedException;
 
     /**
      * Formats a given List of {@link Node} objects to the target format.
      *
      * @param nodes a list of {@link Node} objects.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String formatNodes(final List<Node> nodes) throws NotImplementedException;
 
     /**
      * Formats a given List of {@link NodeReading} objects to the target format.
      *
      * @param nodeReadings a list of {@link NodeReading} objects.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
 
     String formatNodeReadings(final List<NodeReading> nodeReadings) throws NotImplementedException;
 
     /**
      * Formats a given List of {@link NodeReading} objects to the target format.
      *
      * @param nodeCapabilities a list of {@link Node} objects.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String formatLastNodeReadings(final List<NodeCapability> nodeCapabilities) throws NotImplementedException;
 
 
     /**
      * Formats a given List of {@link NodeCapability} objects to the target format.
      *
      * @param nodeCapabilities a List of {@link NodeCapability} objects.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String formatUniqueLastNodeReadings(final List<NodeCapability> nodeCapabilities) throws NotImplementedException;
 
     /**
      * Formats a given List of {@link Link} objects to the target format.
      *
      * @param links a List of {@link Link} objects.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String formatLinks(final List<Link> links) throws NotImplementedException;
 
     /**
      * Formats the given Lists of {@link LastNodeReading} and {@link LastLinkReading} objects to the target format.
      *
      * @param lastNodeReadings a List of {@link LastNodeReading} objects.
      * @param lastLinkReadings a List of {@link LastLinkReading} objects.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String formatLastReadings(final List<LastNodeReading> lastNodeReadings, final List<LastLinkReading> lastLinkReadings) throws NotImplementedException;
 
     /**
      * Returns a description of the {@link Node} object in the target format.
      *
      * @param node            the {@link Node} objectto describe.
      * @param requestURL      the original request url.
      * @param requestURI      the original request uri.
      * @param nodeDescription the Description String of the {@link Node} object.
      * @param nodePos         the position of the {@link Node} object.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String describeNode(final Node node, final String requestURL, final String requestURI,
                         final String nodeDescription, final Position nodePos) throws NotImplementedException;
 
     /**
      * Returns a desctiption of the {@link Testbed} object in the target format.
      *
      * @param testbed        the {@link Testbed} object object to describe.
      * @param requestURL     the original request url.
      * @param requestURI     the original request uri.
      * @param nodes          the {@link Node} object that belong to the {@link Testbed}.
      * @param descriptionMap a map with the Description String of the {@link Node} objects.
      * @param capabilityMap  a map with the {@link NodeCapability} objects of the {@link Node} objects.
      * @param positionMap      a map with the {@link Origin} objects of the {@link Node} objects.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String describeTestbed(final Testbed testbed, final String requestURL, final String requestURI,
                            final List<Node> nodes, final Map<Node, String> descriptionMap,
                            final Map<Node, List<NodeCapability>> capabilityMap,
                            final Map<Node, Position> positionMap) throws NotImplementedException;
 
     /**
      * @param testbed
      * @param nodes
      * @param links
      * @param capabilities
      * @return
      * @throws NotImplementedException
      */
     String showTestbed(final Testbed testbed, final List<Node> nodes, final List<Link> links,
                        final List<Capability> capabilities) throws NotImplementedException;
 
 
     /**
      * Returns a description of the {@link NodeCapability} objects in the target format.
      *
      * @param capabilities a list of {@link NodeCapability} objects.
      * @return a string containing the formatted object.
      * @throws NotImplementedException when not implemented the function informs the handler.
      */
     String describeNodeCapabilities(final List<NodeCapability> capabilities) throws NotImplementedException;
 
 
 }
