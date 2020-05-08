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
 import java.util.ArrayList;
 import java.util.List;
 
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
     }
 
     public void analyzeGraph() {
         if (graph == null)
             return;
 
         // analyzing conflict bindings
         List<Connection> connections = graph.findAllConflictBindings();
         for (Connection c : connections) {
             Arc arc = findArc(c);
             if (arc != null)
                 arc.setState(State.INCORRECT);
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
                 for (Arc arc : node.getOutgoingArcs()) {
                     if (arc.getTarget().getNodeID() == connection.getTo()) {
                         return arc;
                     }
                 }
             } else if (node.getNodeID() == connection.getTo()) {
                 for (Arc arc : node.getOutgoingArcs()) {
                     if (arc.getTarget().getNodeID() == connection.getFrom()) {
                         return arc;
                     }
                 }
             }
         }
         return null;
     }
 
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D) g;
 
         for (Layer l : this.getLayers()) {
             //Drawing a layer
             g.setColor(Color.darkGray);
             g.drawRect((int) l.getX(), (int) l.getY(), (int) l.getWidth(), (int) l.getHeight());
             g2.drawString("Layer " + l.getLayerID(), (float)l.getX() + 5, (float)l.getY()+ 15);
 
             for (Node n : l.getNodes()) {
                 n.setIncomingArcsByIndexes(graph.getVertexes()[n.getNodeID()].getUsedBy());
                 n.setOutgoingArcsByIndexes(graph.getVertexes()[n.getNodeID()].getUsing());
                 g.setColor(standardColor);
                 g.fillOval((int) n.getPosition().getX(), (int) n.getPosition().getY(), (int) n.getFigure().getWidth(),
                            (int) n.getFigure().getHeight());
                 for (Arc a : n.getOutgoingArcs()) {
                     g2.setColor(a.getState() == State.NORMAL
                                         ? standardColor
                                         : a.getState() == State.IN_CYCLE
                                                 ? cycleColor
                                                 : errorColor
                     );
                     g2.draw(a);
                     g2.fillPolygon(a.getEnd());
                    g.setColor(Color.white);
                    g2.drawString(Integer.toString(n.getNodeID()), (int)n.getPosition().getX() + 7, (int)n.getPosition().getY() + 15);
                 }
                /*for (Arc a : n.getIncomingArcs()) {
                    g2.draw(a);
                    g2.fillPolygon(a.getEnd());
                }*/
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
