 package eu.uberdust.rest.controller.html.node;
 
 import eu.uberdust.caching.Cachable;
 import eu.uberdust.caching.Loggable;
 import eu.uberdust.rest.exception.InvalidTestbedIdException;
 import eu.uberdust.rest.exception.NodeNotFoundException;
 import eu.uberdust.rest.exception.TestbedNotFoundException;
 import eu.wisebed.wisedb.controller.NodeCapabilityController;
 import eu.wisebed.wisedb.controller.NodeController;
 import eu.wisebed.wisedb.controller.TestbedController;
 import eu.wisebed.wisedb.model.Node;
 import eu.wisebed.wisedb.model.NodeCapability;
 import eu.wisebed.wisedb.model.Position;
 import eu.wisebed.wisedb.model.Testbed;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Controller class that returns the a web page for a node.
  */
 @Controller
 @RequestMapping("/testbed/{testbedId}/node")
 public final class NodeViewController {
     /**
      * Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(NodeViewController.class);
 
     /**
      * Testbed persistence manager.
      */
     private transient TestbedController testbedManager;
 
     /**
      * Node persistence manager.
      */
     private transient NodeController nodeManager;
 
     private transient NodeCapabilityController nodeCapabilityManager;
 
     @Autowired
     public void setNodeCapabilityManager(final NodeCapabilityController nodeCapabilityManager) {
         this.nodeCapabilityManager = nodeCapabilityManager;
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
      * Sets testbed persistence manager.
      *
      * @param testbedManager testbed persistence manager.
      */
     @Autowired
     public void setTestbedManager(final TestbedController testbedManager) {
         this.testbedManager = testbedManager;
     }
 
     /**
      * Handle req and return the appropriate response.
      *
      * @return http servlet response.
      * @throws InvalidTestbedIdException InvalidTestbedIdException exception.
      * @throws TestbedNotFoundException  TestbedNotFoundException exception.
      * @throws NodeNotFoundException     NodeNotFoundException exception.
      */
     @Loggable
    @RequestMapping(value = "/{nodeName}", method = RequestMethod.GET)
     public ModelAndView getNode(@PathVariable("testbedId") int testbedId, @PathVariable("nodeName") String nodeName) throws TestbedNotFoundException, NodeNotFoundException {
 
         final long start = System.currentTimeMillis();
 
 
         final Testbed testbed = testbedManager.getByID(testbedId);
         if (testbed == null) {
             // if no testbed is found throw exception
             throw new TestbedNotFoundException("Cannot find testbed [" + testbedId + "].");
         }
 
         // look up node
         final Node node = nodeManager.getByName(nodeName);
         if (node == null) {
             // if no testbed is found throw exception
             throw new NodeNotFoundException("Cannot find node [" + nodeName + "].");
         }
 
         List<NodeCapability> nodeCapabilities = nodeCapabilityManager.list(node);
 
 
         Position nodePosition = nodeManager.getAbsolutePosition(node);
         String nodeType = getNodeType(node);
 
 
         // Prepare data to pass to jsp
         final Map<String, Object> refData = new HashMap<String, Object>();
 
         // else put thisNode instance in refData and return index view
         refData.put("testbed", testbed);
         refData.put("setup", testbed.getSetup());
         refData.put("node", node);
         refData.put("nodePosition", nodePosition);
         refData.put("nodeType", nodeType);
         refData.put("nodeCapabilities", nodeCapabilities);
 
         refData.put("time", String.valueOf((System.currentTimeMillis() - start)));
         return new ModelAndView("node/show.html", refData);
     }
 
     @Loggable
     @RequestMapping(value = "{nodeName}/", method = RequestMethod.PUT)
     @Transactional
     @ResponseBody
     public String putNode(@PathVariable("testbedId") int testbedId, @PathVariable("nodeName") String nodeName, HttpServletResponse response)
             throws InvalidTestbedIdException, TestbedNotFoundException, NodeNotFoundException, IOException {
         final long start = System.currentTimeMillis();
 
         final Testbed testbed = testbedManager.getByID(testbedId);
         if (testbed == null) {
             // if no testbed is found throw exception
             throw new TestbedNotFoundException("Cannot find testbed [" + testbedId + "].");
         }
 
         if (nodeManager.getByName(nodeName) != null) {
             response.setStatus(200);
             response.setContentType("text/plain");
             response.setCharacterEncoding("UTF-8");
             return "Node Already Exists";
         }
 
         final Node newNode = new Node();
         newNode.setName(nodeName);
         newNode.setSetup(testbed.getSetup());
 
         nodeManager.add(newNode);
         // write on the HTTP response
         response.setStatus(200);
         response.setContentType("text/plain");
         response.setCharacterEncoding("UTF-8");
 
         return "Node added to the system";
     }
 
     /**
      * Handle Request and return the appropriate response.
      *
      * @return response http servlet response.
      * @throws InvalidTestbedIdException an InvalidTestbedIdException exception.
      * @throws TestbedNotFoundException  an TestbedNotFoundException exception.
      */
     @Loggable
     @RequestMapping(method = RequestMethod.GET)
     public ModelAndView listNodes(@PathVariable("testbedId") int testbedId) throws TestbedNotFoundException {
 
 
         final long start = System.currentTimeMillis();
 
         final Testbed testbed = testbedManager.getByID(testbedId);
         if (testbed == null) {
             // if no testbed is found throw exception
             throw new TestbedNotFoundException("Cannot find testbed [" + testbedId + "].");
         }
 
         // get testbed's nodes
         final List<Node> nodes = new ArrayList<Node>();
         for (Node node : nodeManager.list(testbed.getSetup())) {
             if (!node.getName().contains(":virtual:")) {
                 nodes.add(node);
             }
         }
 
         // Prepare data to pass to jsp
         final Map<String, Object> refData = new HashMap<String, Object>();
 
         // else put thisNode instance in refData and return index view
         refData.put("testbed", testbed);
 
         refData.put("nodes", nodes);
 
         refData.put("time", String.valueOf((System.currentTimeMillis() - start)));
         return new ModelAndView("node/list.html", refData);
 
     }
 
     @Cachable
     String getNodeType(Node node) {
         String nodeType = "default";
         NodeCapability cap = nodeCapabilityManager.getByID(node, "nodeType");
         if (cap != null) {
             nodeType = cap.getLastNodeReading().getStringReading();
         }
         return nodeType;
     }
 
 }
