 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ex4.viewer;
 
 import javax.swing.JFrame;
 import nav_msgs.OccupancyGrid;
 import org.ros.message.MessageFactory;
 import org.ros.message.MessageListener;
 import org.ros.namespace.GraphName;
 import org.ros.node.AbstractNodeMain;
 import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
 import org.ros.node.topic.Subscriber;
 
 /**
  *
  * @author robot
  */
 public class MapViewerNode extends AbstractNodeMain {
 
     private ConnectedNode node;
     private MessageFactory messageFactory;
     private Subscriber<OccupancyGrid> originalMapSub;
     private Subscriber<OccupancyGrid> heatMapSub;
     private OccupancyGrid originalMap;
     private JFrame frame;
     private MapViewerJPanel panel;
 
     @Override
     public GraphName getDefaultNodeName() {
         return GraphName.of("MapViewer");
     }
 
     @Override
     public void onStart(ConnectedNode connectedNode) {
         frame = new JFrame("Map viewer");
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
         originalMapSub = connectedNode.newSubscriber("map", OccupancyGrid._TYPE);
         originalMapSub.addMessageListener(new MessageListener<OccupancyGrid>() {
 
             @Override
             public void onNewMessage(OccupancyGrid t) {
                 if (originalMap == null) {
                     originalMap = t;
                     panel = new MapViewerJPanel(t.getInfo().getWidth(), t.getInfo().getHeight());
                     System.out.println("Map Width:" + t.getInfo().getWidth() + " map Height:" + t.getInfo().getHeight());
                     panel.setMap(t.getData().array());
 
                     frame.add(panel);
                     frame.pack();
                     frame.setSize(t.getInfo().getWidth() + 5, t.getInfo().getHeight() + 30);
                     frame.setVisible(true);
                     frame.setLocationRelativeTo(null);
                     panel.repaint();
                 }
             }
         });
 
         heatMapSub = connectedNode.newSubscriber("heat_map", OccupancyGrid._TYPE);
         heatMapSub.addMessageListener(new MessageListener<OccupancyGrid>() {
 
             @Override
             public void onNewMessage(OccupancyGrid t) {
                 if(panel != null){
                     panel.setHeatMap(t.getData().array());
                     panel.repaint();
                 }
             }
         });
     }
 }
