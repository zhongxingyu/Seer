 package scespet.core;
 
 import gsa.esg.mekon.core.EventGraphObject;
 import gsa.esg.mekon.core.Function;
 
 import java.util.*;
 
 /**
  * This is a very simplistic, inefficient implementation of a state propagation graph.
  * This is one implementation of the types.Env listener handling API, I have a significantly better implementation elsewhere.
  * However, it is enough to drive the unit tests and development of Scespet, even though in any real production system you'd want something
  * considerably better.
  *
  * Many concepts are not implemented in this class, or are only implemented to a bare minimum of correctness
  * the topology of the graph propagation is correct (but can't handle cycles)
  * 'wakeups' are poorly implemented.
  * the contract between graph structure modification and when events use that new structure is not completely robust.
  *
  *
  */
 public class SlowGraphWalk {
 
     public class Node {
         private int lastFired = -1;
         private Set<Node> in = new HashSet<Node>(2);
         private Set<Node> out = new HashSet<Node>(2);
         private EventGraphObject graphObject;
         private int order = -1;
 
         private Node(EventGraphObject graphObject) {
             this.graphObject = graphObject;
            if (graphObject == null) {
                throw new IllegalArgumentException("Wat?");
            }
         }
 
         public EventGraphObject getGraphObject() {
             return graphObject;
         }
 
         public Collection<Function> getListeners() {
             ArrayList<Function> list = new ArrayList<Function>();
             for (Node node : out) {
                 list.add((Function) node.getGraphObject());
             }
             return list;
         }
 
         public void addIn(Node sourceNode) {
             in.add(sourceNode);
         }
 
         public void removeIn(Node sourceNode) {
             in.remove(sourceNode);
         }
 
         public void addOut(Node targetNode) {
             out.add(targetNode);
         }
 
         public void removeOut(Node targetNode) {
             out.remove(targetNode);
         }
 
         public void trigger() {
             if (in.size() > 1) {
                 // it is a rendezvous, punt it
                 if (!joinNodes.contains(this)) {
                     joinNodes.add(this);
                 }
             } else {
                 // do it now.
                 execute();
             }
         }
 
         private void execute() {
             boolean propagate = true;
             if (graphObject instanceof Function) {
                 propagate = ((Function) graphObject).calculate();
             }
             if (propagate) {
                 if (lastFired == cycleCount) {
                     throw new AssertionError("cycle: "+cycleCount+" This simple graph walk is trying to double-fire a node: "+graphObject);
                 } else {
                     lastFired = cycleCount;
                 }
                 for (Node node : out) {
                     node.trigger();
                 }
             }
         }
     }
 
     private PriorityQueue<Node> joinNodes = new PriorityQueue<Node>(10, new Comparator<Node>() {
         @Override
         public int compare(Node o1, Node o2) {
             return o1.order - o2.order;
         }
     });
     private IdentityHashMap<EventGraphObject, Node> nodes = new IdentityHashMap<EventGraphObject, Node>();
     private int cycleCount = -1;
     private boolean isFiring = false;
     private boolean isChanging = false;
     // graph changes should occur between propagations to avoid very complex behaviour semantics
     private Deque<Runnable> deferredChanges = new ArrayDeque<Runnable>();
     private List<EventGraphObject> deferredFires = new ArrayList<>();
 
     public Collection<Node> getAllNodes() {
         Set<Node> allNodes = new HashSet<Node>();
         for (Node node : nodes.values()) {
             addToNodeSet(allNodes, node);
         }
         return allNodes;
     }
 
     private void addToNodeSet(Set<Node> allNodes, Node node) {
         if (allNodes.add(node)) {
             for (Node inNode : node.in) {
                 addToNodeSet(allNodes, inNode);
             }
             for (Node outNode : node.out) {
                 addToNodeSet(allNodes, outNode);
             }
         }
     }
 
     public void addTrigger(final EventGraphObject source, final Function target) {
         if (isFiring) {
             deferredChanges.add(new Runnable() {
                 public void run() {
                     addTrigger(source, target);
                 }
             });
         } else {
             Node sourceNode = getNode(source);
             Node targetNode = getNode(target);
             sourceNode.addOut(targetNode);
             targetNode.addIn(sourceNode);
             propagateOrder(targetNode, sourceNode.order);
         }
     }
 
     public void removeTrigger(final EventGraphObject source, final Function target) {
         if (isFiring) {
             deferredChanges.add(new Runnable() {
                 public void run() {
                     removeTrigger(source, target);
                 }
             });
         } else {
             Node sourceNode = getNode(source);
             Node targetNode = getNode(target);
             sourceNode.removeOut(targetNode);
             targetNode.removeIn(sourceNode);
         }
     }
 
     public boolean hasChanged(EventGraphObject obj) {
         Node node = getNode(obj);
         return hasChanged(node);
     }
 
     private boolean hasChanged(Node node) {
         return node.lastFired == cycleCount && cycleCount >= 0;
     }
 
     public Iterable<EventGraphObject> getTriggers(EventGraphObject obj) {
         Node node = getNode(obj);
         // should I bring in Guava for functional filtered iterators, or just convert this to scala?
         ArrayList<EventGraphObject> changed = new ArrayList<>(node.in.size());
         for (Node in : node.in) {
             if (hasChanged(in)) {
                 changed.add(in.getGraphObject());
             }
         }
         return changed;
     }
 
     private void propagateOrder(Node targetNode, int greaterThan) {
         if (targetNode.order <= greaterThan) {
             int newGreater = greaterThan + 1;
             targetNode.order = newGreater;
             for (Node child : targetNode.out) {
                 propagateOrder(child, newGreater);
             }
         }
     }
 
     private Node getNode(EventGraphObject source) {
         Node sourceNode = nodes.get(source);
         if (sourceNode == null) {
             sourceNode = new Node(source);
             nodes.put(source, sourceNode);
         }
         return sourceNode;
     }
 
     public void fire(EventGraphObject graphObject) {
         doFire(graphObject);
 
         // now process graph wiring
         applyChanges();
     }
 
     private void doFire(EventGraphObject graphObject) {
         isFiring = true;
         cycleCount++;
         Node node = getNode(graphObject);
         joinNodes.add(node);
         while (!joinNodes.isEmpty()) {
             Node next = joinNodes.remove();
             next.execute();
         }
         isFiring = false;
     }
 
     public void applyChanges() {
         if (!isChanging) {
             isChanging = true;
             while (!deferredChanges.isEmpty() || !deferredFires.isEmpty()) {
                 for (Runnable deferredChange : deferredChanges) {
                     deferredChange.run();
                 }
                 deferredChanges.clear();
                 for (EventGraphObject deferredFire : deferredFires) {
                     doFire(deferredFire);
                 }
                 deferredFires.clear();
             }
             isChanging = false;
         } else {
             throw new UnsupportedOperationException("Don't think I need to apply changes in nested fire");
         }
     }
 
     public void wakeup(EventGraphObject graphObject) {
         Node node = getNode(graphObject);
         if (!joinNodes.contains(node)) {
             joinNodes.add(node);
         }
     }
 
     public void fireAfterChangingListeners(EventGraphObject graphObject) {
         deferredFires.add(graphObject);
     }
 }
