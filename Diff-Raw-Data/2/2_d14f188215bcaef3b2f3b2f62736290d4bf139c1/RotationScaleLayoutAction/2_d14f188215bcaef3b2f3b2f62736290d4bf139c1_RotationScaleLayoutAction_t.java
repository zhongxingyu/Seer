 package cytoscape.actions;
 
 import cytoscape.Cytoscape;
 import cytoscape.foo.GraphConverter2;
 import cytoscape.graph.layout.algorithm.MutablePolyEdgeGraphLayout;
 import cytoscape.graph.layout.impl.RotationLayouter;
 import cytoscape.graph.layout.impl.ScaleLayouter;
 import cytoscape.util.CytoscapeAction;
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.util.Hashtable;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.WindowConstants;
 import javax.swing.border.EmptyBorder;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 public class RotationScaleLayoutAction extends CytoscapeAction
 {
 
   public RotationScaleLayoutAction()
   {
     super("Rotate/Scale Network");
     setPreferredMenu("Layout");
   }
 
   public void actionPerformed(ActionEvent e)
   {
     final boolean noNodesSelected =
       (Cytoscape.getCurrentNetworkView().getSelectedNodeIndices().length == 0);
     final MutablePolyEdgeGraphLayout[] nativeGraph =
       new MutablePolyEdgeGraphLayout[] {
         GraphConverter2.getGraphReference(16.0d, true, false) };
     final RotationLayouter[] rotation = new RotationLayouter[] {
       new RotationLayouter(nativeGraph[0]) };
     final ScaleLayouter[] scale = new ScaleLayouter[] {
       new ScaleLayouter(nativeGraph[0]) };
     Frame cyFrame = Cytoscape.getDesktop();
     JDialog dialog = new JDialog(cyFrame, "Rotate/Scale", true);
     dialog.setResizable(false);
     dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
     // Define the panel containing rotation widget.
     JPanel rotPanel = new JPanel(new BorderLayout());
     JLabel rotLabel = new JLabel("Degrees of Rotation:");
     rotLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
     rotPanel.add(rotLabel, BorderLayout.NORTH);
     final JSlider rotSlider = new JSlider(0, 360, 0);
     rotSlider.setBorder(new EmptyBorder(5, 5, 5, 5));
     rotSlider.setMajorTickSpacing(90);
     rotSlider.setMinorTickSpacing(15);
     rotSlider.setPaintTicks(true);
     rotSlider.setPaintLabels(true);
     rotSlider.addChangeListener(new ChangeListener() {
         private int prevValue = rotSlider.getValue();
         public void stateChanged(ChangeEvent e) {
           if (rotSlider.getValue() == prevValue) return;
           double radians = ((double) (rotSlider.getValue() - prevValue)) *
             2.0d * Math.PI / 360.0d;
           rotation[0].rotateGraph(radians);
           prevValue = rotSlider.getValue(); } });
     rotPanel.add(rotSlider, BorderLayout.CENTER);
 
     // Define the panel containing the scale widget.
     JPanel sclPanel = new JPanel(new BorderLayout());
     JLabel sclLabel = new JLabel("Scale Factor:");
     sclLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
     sclPanel.add(sclLabel, BorderLayout.NORTH);
     final JSlider sclSlider = new JSlider(JSlider.VERTICAL, -300, 300, 0) {
         public Dimension getPreferredSize() {
           Dimension dim = super.getPreferredSize();
           if (dim == null) return null;
           else return new Dimension(dim.width, Math.min(dim.height, 100)); } };
     sclSlider.setBorder(new EmptyBorder(5, 5, 5, 5));
     sclSlider.setMajorTickSpacing(100);
     Hashtable labels = new Hashtable();
     labels.put(new Integer(-300), new JLabel("1/8"));
     labels.put(new Integer(-200), new JLabel("1/4"));
     labels.put(new Integer(-100), new JLabel("1/2"));
     labels.put(new Integer(0), new JLabel("1"));
     labels.put(new Integer(100), new JLabel("2"));
     labels.put(new Integer(200), new JLabel("4"));
     labels.put(new Integer(300), new JLabel("8"));
     sclSlider.setLabelTable(labels);
     sclSlider.setPaintTicks(true);
     sclSlider.setPaintLabels(true);
     sclSlider.addChangeListener(new ChangeListener() {
         private int prevValue = sclSlider.getValue();
         public void stateChanged(ChangeEvent e) {
           if (prevValue == sclSlider.getValue()) return;
           double prevAbsoluteScaleFactor =
             Math.pow(2, ((double) prevValue) / 100.0d);
           double currentAbsoluteScaleFactor =
             Math.pow(2, ((double) sclSlider.getValue()) / 100.0d);
           double neededIncrementalScaleFactor =
             currentAbsoluteScaleFactor / prevAbsoluteScaleFactor;
           scale[0].scaleGraph(neededIncrementalScaleFactor);
           prevValue = sclSlider.getValue(); } });
     sclPanel.add(sclSlider, BorderLayout.CENTER);
 
     GridBagLayout bag = new GridBagLayout();
     JPanel westPanel = new JPanel(bag);
     final JCheckBox chx = new JCheckBox("Move Selected Nodes Only");
     chx.setBorder(new EmptyBorder(5, 5, 5, 5));
     if (noNodesSelected) chx.setEnabled(false);
     chx.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
          System.out.println("selected edges: " +
                             Cytoscape.getCurrentNetworkView().getSelectedEdgeIndices().length);
           nativeGraph[0] = GraphConverter2.getGraphReference
             (128.0d, true, chx.isSelected());
           rotation[0] = new RotationLayouter(nativeGraph[0]);
           scale[0] = new ScaleLayouter(nativeGraph[0]); } } );
     GridBagConstraints c = new GridBagConstraints();
     c.gridwidth = GridBagConstraints.REMAINDER;
     bag.setConstraints(chx, c);
     westPanel.add(chx);
     westPanel.add(rotPanel);
 
     JPanel mainPanel = new JPanel(new BorderLayout());
     mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
     mainPanel.add(westPanel, BorderLayout.CENTER);
     mainPanel.add(sclPanel, BorderLayout.EAST);
 
     dialog.getContentPane().add(mainPanel, BorderLayout.CENTER);
     dialog.pack();
     dialog.move((cyFrame.size().width - dialog.size().width) / 3 +
                 cyFrame.location().x,
                 (cyFrame.size().height - dialog.size().height) / 8 +
                 cyFrame.location().y);
     dialog.show(); // This blocks until dialog is disposed of.
   }
 
 }
