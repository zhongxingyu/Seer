 package eu.uberdust.rest.controller.html.reading;
 
 import eu.uberdust.caching.Loggable;
 import eu.uberdust.command.NodeCapabilityCommand;
 import eu.uberdust.formatter.HtmlFormatter;
 import eu.uberdust.rest.exception.*;
import eu.wisebed.wisedb.controller.*;
import eu.wisebed.wisedb.model.*;
 import org.apache.log4j.Logger;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractRestController;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
import java.util.*;
 
 /**
  * Controller class that returns an HTML page containing a list of the readings for a node/capability.
  */
 public final class ShowController extends AbstractRestController {
 
     /**
      * Node peristence manager.
      */
     private transient NodeController nodeManager;
 
     /**
      * Capability persistence manager.
      */
     private transient CapabilityController capabilityManager;
 
     /**
      * NodeReading persistence manager.
      */
     private transient NodeReadingController nodeReadingManager;
 
     /**
      * Testbed peristence manager.
      */
     private transient TestbedController testbedManager;
 
     /**
      * Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(ShowController.class);
 
     /**
      * Constructor.
      */
     public ShowController() {
         super();
 
         // Make sure to set which method this controller will support.
         this.setSupportedMethods(new String[]{METHOD_GET});
     }
 
     /**
      * Sets node persistence manager.
      *
      * @param nodeManager node persistence manager.
      */
     public void setNodeManager(final NodeController nodeManager) {
         this.nodeManager = nodeManager;
     }
 
     /**
      * Sets capability persistence manager.
      *
      * @param capabilityManager capability persistence manager.
      */
     public void setCapabilityManager(final CapabilityController capabilityManager) {
         this.capabilityManager = capabilityManager;
     }
 
     /**
      * Sets NodeReading persistence manager.
      *
      * @param nodeReadingManager NodeReading persistence manager.
      */
     public void setNodeReadingManager(final NodeReadingController nodeReadingManager) {
         this.nodeReadingManager = nodeReadingManager;
     }
 
     /**
      * Sets Testbed persistence manager.
      *
      * @param testbedManager Testbed persistence manager.
      */
     public void setTestbedManager(final TestbedController testbedManager) {
         this.testbedManager = testbedManager;
     }
 
     /**
      * Handle Request and return the appropriate response.
      *
      * @param req        http servlet req.
      * @param response   http servlet response.
      * @param commandObj command object.
      * @param errors     BindException exception.
      * @return response http servlet response.
      * @throws InvalidNodeIdException         invalid node id exception.
      * @throws InvalidCapabilityNameException invalid capability name exception.
      * @throws InvalidTestbedIdException      invalid testbed id exception.
      * @throws TestbedNotFoundException       testbed not found exception.
      * @throws NodeNotFoundException          node not found exception.
      * @throws CapabilityNotFoundException    capability not found exception.
      * @throws InvalidLimitException          invalid limit exception.
      */
     @Loggable
     protected ModelAndView handle(final HttpServletRequest req, final HttpServletResponse response,
                                   final Object commandObj, final BindException errors)
             throws CapabilityNotFoundException, NodeNotFoundException, TestbedNotFoundException,
             InvalidTestbedIdException, InvalidCapabilityNameException, InvalidNodeIdException, InvalidLimitException {
 
         HtmlFormatter.getInstance().setBaseUrl(req.getRequestURL().substring(0, req.getRequestURL().indexOf("/rest")));
 
         final long start = System.currentTimeMillis();
 
         // set commandNode object
         final NodeCapabilityCommand command = (NodeCapabilityCommand) commandObj;
 
         // check node id
         if (command.getNodeId() == null || command.getNodeId().isEmpty()) {
             throw new InvalidNodeIdException("Must provide node id");
         }
 
         // check capability name
         if (command.getCapabilityId() == null || command.getCapabilityId().isEmpty()) {
             throw new InvalidCapabilityNameException("Must provide capability name");
         }
 
         // a specific testbed is requested by testbed Id
         int testbedId;
         try {
             testbedId = Integer.parseInt(command.getTestbedId());
 
         } catch (NumberFormatException nfe) {
             throw new InvalidTestbedIdException("Testbed IDs have number format.", nfe);
         }
 
         // look up testbed
         final Testbed testbed = testbedManager.getByID(Integer.parseInt(command.getTestbedId()));
         if (testbed == null) {
             // if no testbed is found throw exception
             throw new TestbedNotFoundException("Cannot find testbed [" + testbedId + "].");
         }
 
         // retrieve node
         final Node node = nodeManager.getByName(command.getNodeId());
         if (node == null) {
             throw new NodeNotFoundException("Cannot find node [" + command.getNodeId() + "]");
         }
 
         // retrieve capability
         final Capability capability = capabilityManager.getByID(command.getCapabilityId());
         if (capability == null) {
             throw new CapabilityNotFoundException("Cannot find capability [" + command.getCapabilityId() + "]");
         }
 
         // retrieve readings based on node/capability
         final List<NodeReading> nodeReadings;
         if (command.getReadingsLimit() == null) {
             // no limit is provided
             nodeReadings = nodeReadingManager.listNodeReadings(node, capability);
         } else {
             int limit;
             try {
                 limit = Integer.parseInt(command.getReadingsLimit());
             } catch (NumberFormatException nfe) {
                 throw new InvalidLimitException("Limit must have have number format.", nfe);
             }
             nodeReadings = nodeReadingManager.listNodeReadings(node, capability, limit);
         }
 
         // Prepare data to pass to jsp
         final Map<String, Object> refData = new HashMap<String, Object>();
 
         // else put thisNode instance in refData and return index view
         refData.put("testbedId", command.getTestbedId());
         refData.put("readings", nodeReadings);
         refData.put("testbed", testbed);
 
         refData.put("time", String.valueOf((System.currentTimeMillis() - start)));
         // check type of view requested
         return new ModelAndView("nodecapability/readings.html", refData);
     }
 }
