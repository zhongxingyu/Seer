 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 import java.util.Enumeration;
 import org.jdom.*;
 import java.util.logging.Logger;
 
 /**
  * Denna klass implementerar olika sökalgoritmer för att hitta vägar
  * mellan två platser på en karta.
  *
  * @author "Anton Johansson" <anton.johansson@gmail.com>
  * @author "Victor Zamanian" <victor.zamanian@gmail.com>
  * @version 1.0
  */
 public class MySearcher extends MapSearcher {
    private Graph map;
    private dGraphWriter graphWriter;
    private boolean halt;
    private boolean running;
 
    /**
     * Skapar en ny MySearcher-instans.
     */
    public MySearcher () {
       super ();
    }
 
    public void setGraphics(dGraphWriter graphWriter) {
       Logger.global.info("@ => setGraphics");
       this.graphWriter = graphWriter;
       this.halt = true;
       this.running = true;
    }
 
    public void step() {
       // Releases Thread.sleep which makes the algorithm loop
       // continue.
       this.halt = false;
    }
 
    public void kill() {
       Logger.global.info("@ => kill()");
       this.running = false;
    }
 
    public boolean isRunning() {
       return this.running;
    }
 
    /**
     * Specificerar kartan utifrån en fil i XML-format. The xml-file should look like:
     * <code>
     * <?xml version='1.0' encoding='ISO-8859-1' ?>
     * <map>
     *    <node id="Teg" x="1720099" y="7072732">
     *       <road to="Rondellen" speed="50" />
     *       <road to="Tegsbron" speed="110" />
     *    </node>
     *    ...
     * </map>
     * </code>
     *
     * @param mapFile a File containing the xml representation of the
     * map.
     */
    public void setMap(File mapFile) {
       Document doc;
       List<Element> cityElements;
       try {
          doc = loadXmlMap(mapFile);
          cityElements = doc.getRootElement().getChildren();
          map = new Graph(cityElements.size(), 5);
 
          // Iterate through cityElements
          for (Element cityElement: cityElements) {
             map.insertNode(cityElement.getAttributeValue("id"),
                            Integer.parseInt(cityElement.getAttributeValue("x")),
                            Integer.parseInt(cityElement.getAttributeValue("y")));
          }
 
          // Iterate through cityElements once more to add roads
          for (Element cityElement: cityElements) {
             GraphNode cityNode = map.getNode(cityElement.getAttributeValue("id"));
 
             // Iterate through roadElements in this cityElement
             List<Element> roadElements = cityElement.getChildren();
             for (Element roadElement: roadElements) {
                GraphNode nodeAtEndOfRoad = map.getNode(roadElement.getAttributeValue("to"));
                Double distance = Math.hypot((cityNode.getX() - nodeAtEndOfRoad.getX()),
                                             (cityNode.getY() - nodeAtEndOfRoad.getY()));
 
                Road road =
                   new Road(Integer.parseInt(roadElement.getAttributeValue("speed")),
                            distance);
                map.insertEdge(cityNode.getName(),
                               nodeAtEndOfRoad.getName(),
                               road);
             }
          }
       }
       catch (IOException e) {
          System.err.println ("Could not read/find file.");
          System.exit(1);
       }
       catch (JDOMException e) {
          System.err.println (e.getMessage());
          System.exit(1);
       }
       catch (NumberFormatException e) {
          System.err.println ("Coordinates cannot be parsed. Check XML-file for errors.");
          System.exit(1);
       }
    }
 
    public Graph getMap() {
       return this.map;
    }
 
    /**
     * Utför sökning med Greedy Search.
     *
     * @param from Den plats sökningen börjar från.
     * @param to Den plats sökningen avslutas på.
     * @return En text-representation som innhåller sökvägen till
     * målet. Ex: nod1, nod2, nod3, mål
     */
    public String greedySearch (String from, String to) {
       String pathToGoal = "",
          expandedNodes = "";
       // 1. Set N to be a sorted list of initial nodes;
       PriorityQueue<GraphNode> queue =
          new PriorityQueue<GraphNode>(this.map.size());
       // Set fromNodes distance to 0.0
       map.getNode(from).setDistanceTraveled(0.0);
       queue.add(map.getNode(from));
       GraphNode n;
       while (!queue.isEmpty() && running) { // 2. If N is empty,
          // 3. set n to be the first node in N, and remove n from N;
          n = queue.poll();
 
         waitForClickDrawNode(n);

          // store expander
          expandedNodes += n.getName() + (n == map.getNode(to) ? "" : ", ");
          n.visit();
          // 4. If n is the goal node, exit and signal success;
          if (n == this.map.getNode(to)) {
             pathToGoal = n.getName(); // Goal
             n = n.getParent();
 
             while (n != map.getNode(from)) {
                pathToGoal = n.getName() + ", " + pathToGoal;
                n = n.getParent();
             }
             pathToGoal = n.getName() + ", " + pathToGoal; // Beginning
             Logger.global.info("\nAll expanded nodes:\n" + expandedNodes + "\n\n");
             return "Path to goal:\n" + pathToGoal + "\n";
          }
          // 5. Otherwise add the children of n to N,
          GraphNode neighbour;
          for (Enumeration<GraphNode> e = n.getNeighbours();
               e.hasMoreElements();) {
             neighbour = e.nextElement();
             neighbour.setDistanceToGoal(map.getNode(to));
             neighbour.setEvalFuncVal(neighbour.getDistanceToGoal());
             if (!neighbour.isVisited() && !queue.contains(neighbour)) {
                neighbour.setParent(n);
                queue.add(neighbour);
             }
             // and return to step 2. (end of loop)
          }
       }
       // 2. ... exit and signal failure
       return "Goal not found!\n";
    }
 
    /**
     * Utför sökning med A*.
     *
     * @param from Den plats sökningen börjar från.
     * @param to Den plats sökningen avslutas på.
     * @param fastest Om <code>true</code>, hitta snabbaste vägen,
     * annars den kortaste.
     * @return En text-representation som innhåller sökvägen till
     * målet. Ex: nod1, nod2, nod3, mål
     */
    public String aStar (String from, String to, boolean fastest) {
       String pathToGoal = "",
          expandedNodes = "";
       // 1. Set N to be a sorted list of initial nodes;
       PriorityQueue<GraphNode> queue =
          new PriorityQueue<GraphNode>(this.map.size());
       // Set fromNodes distance to 0.0
       map.getNode(from).setDistanceTraveled(0.0);
       queue.add(map.getNode(from));
       GraphNode n;
       while (!queue.isEmpty() && running) { // 2. If N is empty,
          // 3. set n to be the first node in N, and remove n from N;
          n = queue.poll();
          // store expander
          expandedNodes += n.getName() + (n == map.getNode(to) ? "" : ", ");
          n.visit();
 
         waitForClickDrawNode(n);

          // 4. If n is the goal node, exit and signal success;
          if (n == this.map.getNode(to)) {
             pathToGoal = n.getName(); // Goal
             n = n.getParent();
 
             while (n != map.getNode(from)) {
                pathToGoal = n.getName() + ", " + pathToGoal;
                n = n.getParent();
             }
             pathToGoal = n.getName() + ", " + pathToGoal; // Beginning
             Logger.global.info("\nAll expanded nodes:\n" + expandedNodes + "\n\n");
             return "Path to goal:\n" + pathToGoal + "\n";
          }
          // 5. Otherwise add the children of n to N,
          GraphNode neighbour;
          for (Enumeration<GraphNode> e = n.getNeighbours();
               e.hasMoreElements();) {
             neighbour = e.nextElement();
             if (fastest) {
                neighbour.setDistanceTraveled(n.getDistanceTraveled()
                                              + ((Road) n.getEdge(neighbour)).getTravelTime());
                neighbour.setDistanceToGoal(map.getNode(to));
                neighbour.setEvalFuncVal(neighbour.getDistanceTraveled()
                                         + neighbour.getDistanceToGoal() / (110 / 3.6));
             }
             else {
                neighbour.setDistanceTraveled(n.getDistanceTraveled()
                                              + ((Road) n.getEdge(neighbour)).getDistance());
                neighbour.setDistanceToGoal(map.getNode(to));
                neighbour.setEvalFuncVal(neighbour.getDistanceTraveled() +
                                         neighbour.getDistanceToGoal());
             }
 
             // sort the nodes in N according to the value on their
             // evaluation function
             //else if (!queue.contains(neighbour)) {//&& !neighbour.isVisited()) {
             if (!neighbour.isVisited()) {
                neighbour.setParent(n);
                queue.add(neighbour);
                // and return to step 2. (end of loop)
             }
          }
       }
       // 2. ... exit and signal failure
       return "Goal not found!\n";
    }
 
    /**
     * Utför bredden-förstsökning.
     *
     * @param from Den plats sökningen börjar från.
     * @param to Den plats sökningen avslutas på.
     * @return En text-representation som innhåller sökvägen till
     * målet. Ex: nod1, nod2, nod3, mål
     */
    public String breadthFirst (String from, String to) {
       Logger.global.info("@ breadthFirst => Entering");
       /*
        * Comments fetched from
        * http://en.wikipedia.org/wiki/Breadth-first_search
        * at Tue Oct 14 12:57:18 2008
        */
       LinkedList<GraphNode> queue = new LinkedList<GraphNode>();
       String expandedNodes = "";
       String pathToGoal = "";
       // 1. Put the root node on the queue.
       queue.add(map.getNode(from));
       while (!queue.isEmpty() && running) {
          // 2. Pull a node from the beginning of the queue and examine it.
          GraphNode n = queue.removeFirst();
          n.visit();
 
          expandedNodes += n.getName() + (n == map.getNode(to) ? "" : ", ");
 
          //   * If the searched element is found in this node, quit
          //   * the search and return a result.
          if (n.getName().equals(to)) {
            
            waitForClickDrawNode(n);
             
             pathToGoal = n.getName(); // Goal
             n = n.getParent();
 
             while (n != map.getNode(from)) {
                pathToGoal = n.getName() + ", " + pathToGoal;
                n = n.getParent();
             }
             pathToGoal = n.getName() + ", " + pathToGoal; // Beginning
             Logger.global.info("\nAll expanded nodes:\n" + expandedNodes + "\n\n");
             return "Path to goal:\n" + pathToGoal + "\n";
          }
          //   * Otherwise push all the (so-far-unexamined) successors
          //   * (the direct child nodes) of this node into the end of
          //   * the queue, if there are any.
          else {
             for (Enumeration e = n.getNeighbours(); e.hasMoreElements();) {
                GraphNode neighbour = (GraphNode) e.nextElement();
                if (!neighbour.isVisited() && !queue.contains(neighbour)) {
                   neighbour.setParent(n);
                   
                   drawParentArrows(neighbour, n);
                   
                   queue.add(neighbour);
                }
             }
          }
          
          waitForClickDrawNode(n);
          
       }
       Logger.global.info("@ breadthFirst => Leaving");
       return "Goal not found!\n";
    }
 
    /**
     * Utför djupet-förstsökning.
     *
     * @param from Den plats sökningen börjar från.
     * @param to Den plats sökningen avslutas på.
     * @return En text-representation som innhåller sökvägen till
     * målet. Ex: nod1, nod2, nod3, mål
     */
    public String depthFirst (String from, String to) {
       Logger.global.info("@ depthFirst => Entering depthFirst algorithm");
       LinkedList<GraphNode> stack = new LinkedList<GraphNode>();
       String expandedNodes = "";
       String pathToGoal = "";
       // 1. Push the root node onto the stack.
       stack.add(map.getNode(from));
       while (!stack.isEmpty() && running) {
          // 2. Pop a node from the stack
          GraphNode n = stack.removeLast();
          //   * Mark this node as visited
          n.visit();
 
          //   * If the searched element is found in this node, quit
          //   * the search and return a result.
          if (n.getName().equals(to)) {
             
             waitForClickDrawNode(n);
             
             expandedNodes += n.getName();
             pathToGoal = n.getName(); // Goal
             n = n.getParent();
 
             while (n != map.getNode(from)) {
                pathToGoal = n.getName() + ", " + pathToGoal;
                n = n.getParent();
             }
             pathToGoal = n.getName() + ", " + pathToGoal; // Beginning
             Logger.global.info("\nAll expanded nodes:\n" + expandedNodes + "\n\n");
             return "Path to goal:\n" + pathToGoal + "\n";
          }
          //   * Otherwise push all the unvisited connecting nodes of n
          else {
             for (Enumeration e = n.getNeighbours();
                  e.hasMoreElements();) {
                GraphNode neighbour = (GraphNode) e.nextElement();
                if (!neighbour.isVisited() && !stack.contains(neighbour)) {
                   neighbour.setParent(n);
                   stack.add(neighbour);
                   
                   drawParentArrows(neighbour, n);
                   
                }
             }
          }
          expandedNodes += n.getName() + ", ";
          Logger.global.info("Stack: \n " + stack);
          waitForClickDrawNode(n);
       }
       Logger.global.info("@ depthFirst => Leaving depthFirst algorithm");
       return "Goal not found!\n";
    }
 
    /**
     * Returnerar en text-representation av kartan satt i metoden
     * setMap().
     *
     * @return En text-representation av kartan satt i metoden
     * setMap().
     */
    public String toString() {
       return map.toString();
    }
 
    /**
     * FIX
     *
     * @param n FIX
     */
    private void waitForClickDrawNode(GraphNode n) {
       Logger.global.info("@ => waitForClickDrawNode");
       Logger.global.info("  -  Node is: " + n.getName());
       graphWriter.visitNode(n.getX(), n.getY());
       while (halt) {
          try {
             Thread.sleep(100);
          }
          catch (InterruptedException e) {
             e.printStackTrace();
          }
       }
       halt = true;
    }
 
    public void drawParentArrows(GraphNode child, GraphNode parent) {
       Logger.global.info("ChildNode is: " + child.getName() + ", ParentNode is: " + parent.getName());
       graphWriter.addParentArrow(child.getX(), child.getY(), parent.getX(), parent.getY());
    }
 
    /**
     * Reset the Graph from set variables.
     *
     */
    public void reset() {
       map.cleanGraph();
    }
 }
