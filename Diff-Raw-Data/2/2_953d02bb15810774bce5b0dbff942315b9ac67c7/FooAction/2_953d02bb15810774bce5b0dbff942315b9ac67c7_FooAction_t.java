 package cytoscape.actions;
 
 import cytoscape.Cytoscape;
 import cytoscape.util.CytoscapeAction;
 import cytoscape.view.CyNetworkView;
 import giny.view.NodeView;
 
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.util.Iterator;
 import javax.swing.JDialog;
 import javax.swing.JSlider;
 import javax.swing.WindowConstants;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 public class FooAction extends CytoscapeAction
 {
 
  public FooAction() { super("Foo Bug"); setPreferredMenu("Layout"); }
 
   public void actionPerformed(ActionEvent e) {
     CyNetworkView graphView = Cytoscape.getCurrentNetworkView();
     final NodeView[] nodes = new NodeView[graphView.getNodeViewCount()];
     Iterator nodeIter = graphView.getNodeViewsIterator();
     int nodeInx = 0;
     while (nodeIter.hasNext()) {
       nodes[nodeInx++] = (NodeView) nodeIter.next(); }
     Frame cyFrame = Cytoscape.getDesktop();
     JDialog dialog = new JDialog(cyFrame, "Foo", true);
     dialog.setResizable(false);
     dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
     JSlider slider = new JSlider(0, 100, 0);
     slider.setBorder(new EmptyBorder(15, 15, 15, 15));
     slider.addChangeListener(new ChangeListener() {
         private boolean b = false;
         public void stateChanged(ChangeEvent e) {
           boolean forward = b;
           b = !b;
           int addThis = (forward ? 1 : -1);
           for (int i = 0; i < nodes.length; i++) {
 //             nodes[i].setXPosition(nodes[i].getXPosition() + addThis);
 //             nodes[i].setYPosition(nodes[i].getYPosition() + addThis); } } } );
             nodes[i].setOffset(nodes[i].getXPosition() + addThis,
                                nodes[i].getYPosition() + addThis); } } } );
     dialog.getContentPane().add(slider);
     dialog.pack();
     dialog.move((cyFrame.size().width - dialog.size().width) / 2 +
                 cyFrame.location().x,
                 (cyFrame.size().height - dialog.size().height) / 5 +
                 cyFrame.location().y);
     dialog.show(); // This blocks until dialog is disposed of.
   }
 
 }
