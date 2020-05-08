 package eu.uberdust.rest.controller.rdf;
 
 import com.sun.syndication.io.FeedException;
 import eu.uberdust.caching.Loggable;
 import eu.uberdust.rest.controller.UberdustSpringController;
 import eu.uberdust.rest.exception.InvalidTestbedIdException;
 import eu.uberdust.rest.exception.NodeNotFoundException;
 import eu.uberdust.rest.exception.TestbedNotFoundException;
 import eu.wisebed.wisedb.controller.CapabilityController;
 import eu.wisebed.wisedb.controller.NodeController;
 import eu.wisebed.wisedb.controller.NodeReadingController;
 import eu.wisebed.wisedb.controller.TestbedController;
 import eu.wisebed.wisedb.model.Capability;
 import eu.wisebed.wisedb.model.Testbed;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.IOException;
 
 /**
  * Controller class that returns the position of a node in GeoRSS format.
  */
 @Controller
 @RequestMapping("/testbed/{testbedId}/capability/{capabilityName}/rdf")
 
 public final class CapabilityRdfDescriptionController extends UberdustSpringController {
 
     /**
      * Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(CapabilityRdfDescriptionController.class);
 
     /**
      * Tested persistence manager.
      */
     private transient TestbedController testbedManager;
     /**
      * Node persistence manager.
      */
     private transient NodeController nodeManager;
     /**
      * NodeReading persistence manager.
      */
     private transient NodeReadingController nodeReadingManager;
     /**
      * Capability persistence manager.
      */
     private transient CapabilityController capabilityManager;
 
     /**
      * Sets testbed persistence manager.
      *
      * @param testbedManager testbed persistence manager.
      */
     @Autowired
     public void setTestbedManager(final TestbedController testbedManager) {
         this.testbedManager = testbedManager;
     }
 
     /**
      * Sets node persistence manager.
      *
      * @param nodeManager node persistence manager.
      */
     @Autowired
     public void setNodeManager(final NodeController nodeManager) {
         this.nodeManager = nodeManager;
     }
 
     /**
      * Sets NodeReading persistence manager.
      *
      * @param nodeReadingManager NodeReading persistence manager.
      */
     @Autowired
     public void setNodeReadingManager(final NodeReadingController nodeReadingManager) {
         this.nodeReadingManager = nodeReadingManager;
     }
 
     /**
      * Sets capability persistence manager.
      *
      * @param capabilityManager capability persistence manager.
      */
     @Autowired
     public void setCapabilityManager(final CapabilityController capabilityManager) {
         this.capabilityManager = capabilityManager;
     }
 
     /**
      * Handle request and return the appropriate response.
      *
      * @return http servlet response.
      * @throws java.io.IOException an IOException exception.
      * @throws com.sun.syndication.io.FeedException
      *                             a FeedException exception.
      * @throws eu.uberdust.rest.exception.NodeNotFoundException
      *                             NodeNotFoundException exception.
      * @throws eu.uberdust.rest.exception.TestbedNotFoundException
      *                             TestbedNotFoundException exception.
      * @throws eu.uberdust.rest.exception.InvalidTestbedIdException
      *                             InvalidTestbedIdException exception.
      */
     @Loggable
     @SuppressWarnings("unchecked")
     @RequestMapping(method = RequestMethod.GET, value = "/{rdfEncoding}")
     public ResponseEntity<String> handleDefault(@PathVariable("testbedId") int testbedId, @PathVariable("capabilityName") String capabilityName, HttpServletRequest request)
             throws IOException, FeedException, NodeNotFoundException, TestbedNotFoundException,
             InvalidTestbedIdException {
         return handle(testbedId, capabilityName, "rdf+xml", request);
     }
 
 
     /**
      * Handle request and return the appropriate response.
      *
      * @return http servlet response.
      * @throws java.io.IOException an IOException exception.
      * @throws com.sun.syndication.io.FeedException
      *                             a FeedException exception.
      * @throws eu.uberdust.rest.exception.NodeNotFoundException
      *                             NodeNotFoundException exception.
      * @throws eu.uberdust.rest.exception.TestbedNotFoundException
      *                             TestbedNotFoundException exception.
      * @throws eu.uberdust.rest.exception.InvalidTestbedIdException
      *                             InvalidTestbedIdException exception.
      */
     @Loggable
     @SuppressWarnings("unchecked")
     @RequestMapping(method = RequestMethod.GET, value = "/{rdfEncoding}")
     public ResponseEntity<String> handle(@PathVariable("testbedId") int testbedId, @PathVariable("capabilityName") String capabilityName, @PathVariable("rdfEncoding") String rdfEncoding, HttpServletRequest request)
             throws IOException, FeedException, NodeNotFoundException, TestbedNotFoundException,
             InvalidTestbedIdException {
         final long start = System.currentTimeMillis();
         initialize(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
 
         // look up testbed
         final Testbed testbed = testbedManager.getByID(testbedId);
         if (testbed == null) {
             // if no testbed is found throw exception
             throw new TestbedNotFoundException("Cannot find testbed [" + testbedId + "].");
         }
 
         final Capability capability = capabilityManager.getByID(capabilityName);
 //        final Capability capabilityRoom = capabilityManager.getByID("room");
 //        List<NodeReading> roomReading = nodeReadingManager.listNodeReadings(node, capabilityRoom, 1);
 //        readings.add(1, roomReading.get(0));
 
         // current host base URL
 
         StringBuilder rdfDescription = new StringBuilder("");
 
         rdfDescription.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                 .append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n")
                 .append("  xmlns:ns0=\"http://www.w3.org/2000/01/rdf-schema#\"\n")
                 .append("  xmlns:ns1=\"http://purl.oclc.org/NET/ssnx/ssn#\"\n")
                 .append("  xmlns:ns2=\"http://spitfire-project.eu/cc/spitfireCC_n3.owl#\"\n")
                 .append("  xmlns:ns3=\"http://www.loa-cnr.it/ontologies/DUL.owl#\"\n")
                .append("  xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n")
                 .append("  xmlns:ns4=\"http://purl.org/dc/terms/\">\n").append("\n");
         rdfDescription.append("<rdf:Description rdf:about=\"" + request.getRequestURL() + "\">\n")
                 .append("\t<owl:sameAs rdf:resource=\"" + capability.getSemanticUrl() + "\"/>\n")
                 .append("</rdf:Description>\n");
 
 
         rdfDescription.append("\n")
                 .append("</rdf:RDF>");
 
         HttpHeaders responseHeaders = new HttpHeaders();
         responseHeaders.add("Content-Type", "application/rdf+xml; charset=UTF-8");
         return new ResponseEntity<String>(rdfDescription.toString(), responseHeaders, HttpStatus.OK);
     }
 }
