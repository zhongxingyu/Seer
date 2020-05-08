 package gui;
 
 /**
  * Created with IntelliJ IDEA.
  * User: i.k
  * Date: 13.06.13
  * Time: 22:48
  */
 
 import graph.Connection;
 import graph.Graph;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 public class GraphPanel extends JPanel {
     private List<Layer> layers;
     private Graph graph;
     private List<Node> nodes;
 
     private final Color standardColor = Color.BLACK;
     private final Color cycleColor = Color.GREEN;
     private final Color errorColor = Color.RED;
 
     public boolean equals(Object o) {
         return true;
     }
 
     public GraphPanel() {
         this.layers = new ArrayList<Layer>();
         this.nodes = new ArrayList<Node>();
         this.addMouseListener(new MouseAdapter() {
             boolean dragging = false;
             Node draggedNode;
 
             @Override
             public void mouseClicked(MouseEvent e) {
                 for (Node n : nodes)
                     if (n.getView().getBounds().contains(e.getPoint())) {
                         setCursor(new Cursor(Cursor.MOVE_CURSOR));
                         dragging = true;
                         draggedNode = n;
                         draggedNode.setState(State.DRAGGED);
                         return;
                     }
 
             }
 
             @Override
             public void mouseReleased(MouseEvent e) {
                 for (Layer l : layers)
                     if (l.contains(e.getPoint())) {
                         draggedNode.getLayer().getNodes().remove(draggedNode);
                         draggedNode.setLayer(l);
                         draggedNode.setState(State.NORMAL);
                         l.getNodes().add(draggedNode);
                         dragging = false;
                         setCursor(Cursor.getDefaultCursor());
                         repaint();
                         return;
                     }
             }
 
             @Override
             public void mouseDragged(MouseEvent e) {
                 super.mouseDragged(e);    //To change body of overridden methods use File | Settings | File Templates.
             }
         });
     }
 
     public List<Node> getNodes() {
         return nodes;
     }
 
     public void setGraph(Graph graph) {
         this.graph = graph;
         for (int i = 0; i <= graph.getLayers(); i++)
             this.getLayers().add(new Layer(this, i));
 
         Graph.Node vx;
         Layer cl;
 
         //Gets all the nodes from Grpah graph and distributes them to layers as intended in graphical model.
         for (int i = 0; i < graph.getVertexes().length; i++) {
             vx = graph.getVertexes()[i];
             Node n = new Node(this.getLayers().get(vx.getLayer()), i);
             this.nodes.add(n);
             this.getLayers().get(vx.getLayer()).getNodes().add(n);
         }
         //Initialises node attributes, that can be in--sed only after all the nodes in current layer are set
         for (Layer l : this.getLayers())
             for (Node n : l.getNodes()) {
                 n.setNodeInLayerID();
                 n.setPosition();
             }
         for (Layer l : this.getLayers())
             for (Node n : l.getNodes()) {
                 n.setIncomingArcsByIndexes(graph.getVertexes()[n.getNodeID()].getUsedBy());
                 n.setOutgoingArcsByIndexes(graph.getVertexes()[n.getNodeID()].getUsing());
             }
     }
 
     public void analyzeGraph() {
         if (graph == null) return;
 
         // analyzing conflict bindings
         List<Connection> connections = graph.findAllConflictBindings();
         for (Connection c : connections) {
             Arc arc = findArc(c);
             if (arc != null) arc.setState(State.INCORRECT);
         }
 
         // analyzing self loops
         List<Integer> nodeIndexes = graph.findAllSelfLoops();
         List<Node> nodes = getNodes();
         for (int index : nodeIndexes) {
             nodes.get(index).setState(State.INCORRECT);
         }
     }
 
     private Arc findArc(Connection connection) {
         List<Node> nodes = getNodes();
         for (Node node : nodes) {
             if (node.getNodeID() == connection.getFrom()) {
                 List<Arc> outgoingArcs = node.getOutgoingArcs();
                 for (Arc arc : outgoingArcs) {
                     if (arc.getTarget().getNodeID() == connection.getTo()) {
                         return arc;
                     }
                 }
             }
             else if (node.getNodeID() == connection.getTo()) {
                 List<Arc> incomingArcs = node.getIncomingArcs();
                 for (Arc arc : incomingArcs) {
                     if (arc.getTarget().getNodeID() == connection.getFrom()) {
                         return arc;
                     }
                 }
             }
         }
         return null;
     }
 
     public void analyzeLoops() {
         if (graph == null)
             return;
 
         List<Set<Connection>> loops = graph.findAllLoops();
         for (Set<Connection> loop : loops) {
             for (Connection connection : loop) {
                 Arc arc = findArc(connection);
                 if (arc == null) {
                     continue;
                 }
                 arc.setState(State.IN_CYCLE);
                 arc.getOrigin().setState(State.IN_CYCLE);
                 arc.getTarget().setState(State.IN_CYCLE);
             }
         }
     }
 
     public void clearAllStates() {
         List<Node> nodes = getNodes();
         for (Node node : nodes) {
             node.setState(State.NORMAL);
             for (Arc arc : node.getIncomingArcs()) {
                 arc.setState(State.NORMAL);
             }
             for (Arc arc : node.getOutgoingArcs()) {
                 arc.setState(State.NORMAL);
             }
         }
     }
 
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D) g;
 
         for (Layer l : this.getLayers()) {
             //Drawing a layer
             g.setColor(Color.darkGray);
             g.drawRect((int) l.getX(), (int) l.getY(), (int) l.getWidth(), (int) l.getHeight());
             g2.drawString("Layer " + l.getLayerID(), (float) l.getX() + 5, (float) l.getY() + 15);
 
             for (Node n : l.getNodes()) {
                 Color nodeColor = n.getState() == State.NORMAL
                         ? standardColor
                         : n.getState() == State.IN_CYCLE
                                 ? cycleColor
                                 : n.getState() == State.INCORRECT
                                         ? errorColor
                                         : Color.darkGray;
                 g2.setColor(nodeColor);
                 g2.draw(n.getView());
                 g2.drawString(Integer.toString(n.getNodeID()), (int) n.getCenterOf().getX() - 3,
                               (int) n.getCenterOf().getY() + 5);
                 for (Arc a : n.getOutgoingArcs()) {
                     g2.setColor(a.getState() == State.NORMAL
                                         ? standardColor
                                         : a.getState() == State.IN_CYCLE
                                                 ? cycleColor
                                                 : errorColor);
                     g2.draw(a);
                     g2.fillPolygon(a.getEnd());
                 }
             }
         }
     }
 
     public List<Layer> getLayers() {
         return layers;
     }
 
     public void setLayers(List<Layer> layers) {
         this.layers = layers;
     }
 
 }
