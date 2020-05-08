 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Insets;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.BorderFactory;
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 
 import org.jgraph.JGraph;
 import org.jgraph.graph.AttributeMap;
 import org.jgraph.graph.DefaultGraphCell;
 import org.jgraph.graph.GraphConstants;
 import org.jgrapht.demo.JGraphAdapterDemo;
 import org.jgrapht.ext.JGraphModelAdapter;
 
 // FIXME: Lots of side effects
 public class Main extends JApplet {
     private static final long serialVersionUID = 3256444702936019250L;
     private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);
     private static int sensorRange = 100;
 
     private JGraph jgraph = null;
     private Set<Sensor> sensors = new HashSet<Sensor>();
    private JGraphModelAdapter<Sensor, SensorEdge> jgraphAdapter = null;
     private MouseAdapter mouseAdapter = null;
 
     public static void main(String[] args) {
         JGraphAdapterDemo applet = new JGraphAdapterDemo();
         applet.init();
         applet.setBackground(ColorTheme.Black);
 
         initFrame(applet);
     }
 
     public void init() {
         setupButtons();
 
         initSensors();
         initMouseAdapter();
         initJGraphT();
         initJGraph();
 
         positionSensorsOnJGraph();
 
         resize(DEFAULT_SIZE);
     }
 
     private void initMouseAdapter() {
         mouseAdapter = new MouseAdapter() {
             public void mouseReleased(MouseEvent event) {
                 Object[] cells = jgraph.getSelectionCells().clone();
                 if (cells == null)
                     return;
 
                 for (Object cellObject : cells) {
                     DefaultGraphCell cell = (DefaultGraphCell) cellObject;
                     updateSensorPositionFromCell(cell);
                 }
 
                 recomputeJGraphT();
                 repaint();
             }
         };
     }
 
     public void paint(Graphics g) {
         super.paint(g);
 
         Graphics jgraphGraphics = jgraph.getGraphics();
 
         for (Sensor sensor : sensors) {
             SensorDrawingUtils.PaintSensorRange(jgraphGraphics, sensor);
         }
     }
 
     private void initSensors() {
         Sensor.SetRange(sensorRange);
         sensors = new HashSet<Sensor>();
         sensors.add(new Sensor(new Point2D.Double(130, 40)));
         sensors.add(new Sensor(new Point2D.Double(130, 100)));
         sensors.add(new Sensor(new Point2D.Double(190, 100)));
     }
 
     private void initJGraphT() {
        //ProximityGraph proximityGraph = (new GraphFactory()).makeProximityGraphFromSensors(sensors);
    	ProximityGraph proximityGraph = new ProximityGraph(sensors);
        //jgraphAdapter = (new JGraphConverter()).convertProximityGraphToJGraph(proximityGraph);
    	jgraphAdapter = (new JGraphConverter()).convertProximityGraphToJGraph(proximityGraph);
     }
 
     private void recomputeJGraphT() {
         this.remove(jgraph);
         initJGraphT();
         initJGraph();
         positionSensorsOnJGraph();
     }
 
     private void initJGraph() {
         jgraph = new JGraph(jgraphAdapter);
         jgraph.addMouseListener(mouseAdapter);
         adjustDisplaySettings(jgraph);
         this.getContentPane().add(jgraph);
     }
 
     private void updateSensorPositionFromCell(DefaultGraphCell cell) {
         Sensor sensor = (Sensor) jgraphAdapter.getValue(cell);
 
         AttributeMap cellAttributes = cell.getAttributes();
         Rectangle2D cellBounds = GraphConstants.getBounds(cellAttributes);
 
         double offset = SensorDrawingUtils.GetSensorScreenWidth()/2.0;
         sensor.setPosition(cellBounds.getCenterX()+offset, cellBounds.getCenterY()+offset);
     }
 
     private void positionSensorsOnJGraph() {
         for (Sensor sensor : sensors) {
             positionSensorOnJgraph(jgraphAdapter, sensor);
         }
     }
 
     @SuppressWarnings("unchecked")
    private void positionSensorOnJgraph(JGraphModelAdapter<Sensor, SensorEdge> jgraphAdapter, Sensor sensor) {
         DefaultGraphCell cell = jgraphAdapter.getVertexCell(sensor);
 
         AttributeMap cellAttributes = cell.getAttributes();
         GraphConstants.setBounds(cellAttributes, SensorDrawingUtils.GetBoundsFromSensor(sensor));
 
         AttributeMap cellAttr = new AttributeMap();
         cellAttr.put(cell, cellAttributes);
 
         jgraphAdapter.edit(cellAttr, null, null, null);
     }
 
     private void setupButtons() {
         Container c = getContentPane();
         c.setBackground(ColorTheme.Black);
         c.setLayout(new FlowLayout());
         c.add(makeButton(" Show Proximity Graph "));
         c.add(makeButton(" Show MST "));
     }
 
     private JButton makeButton(String label) {
         JButton button = new JButton(label);
         button.setBorder(BorderFactory.createLineBorder(ColorTheme.White, 1));
         button.setBackground(ColorTheme.Black);
         button.setForeground(ColorTheme.White);
         button.setMargin(new Insets(10, 100, 10, 10));
 
         return button;
     }
 
     private static void initFrame(JGraphAdapterDemo applet) {
         JFrame frame = new JFrame();
         frame.getContentPane().add(applet);
         frame.setTitle("JGraphT Adapter to JGraph Demo");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.pack();
         frame.setVisible(true);
         frame.setBackground(ColorTheme.Black);
     }
 
     private void adjustDisplaySettings(JGraph jgraph) {
         jgraph.setPreferredSize(DEFAULT_SIZE);
         jgraph.setBackground(ColorTheme.Black);
         jgraph.setDragEnabled(false);
         jgraph.setAntiAliased(true);
         jgraph.setDropEnabled(false);
         jgraph.setEditable(false);
         jgraph.setMoveBeyondGraphBounds(false);
 
     }
 }
