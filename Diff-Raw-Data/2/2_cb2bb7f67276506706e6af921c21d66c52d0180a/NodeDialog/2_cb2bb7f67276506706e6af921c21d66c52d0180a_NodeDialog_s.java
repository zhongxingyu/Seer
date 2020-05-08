 package inat.cytoscape;
 
 import giny.model.Node;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 
 /**
  * The node dialog contains the settings of a node.
  * 
  * @author Brend Wanders
  * 
  */
 public class NodeDialog extends JFrame {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1498730989498413815L;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param node the node to display for.
 	 */
 	@SuppressWarnings("unchecked")
 	public NodeDialog(final Node node) {
 		super("Reactant '" + node.getIdentifier() + "'");
 		CyAttributes networkAttributes = Cytoscape.getNetworkAttributes(),
 					 nodeAttributes = Cytoscape.getNodeAttributes();
 		Object res = nodeAttributes.getAttribute(node.getIdentifier(), "canonicalName");
 		if (res != null) {
 			this.setTitle("Reactant " + res.toString());
 		}
 		if (!nodeAttributes.hasAttribute(node.getIdentifier(), "initialConcentration")) {
 			nodeAttributes.setAttribute(node.getIdentifier(), "initialConcentration", 0);
 		}
 
 		this.setLayout(new BorderLayout(2, 2));
 
 		JPanel values = new JPanel(new GridLayout(2, 2, 2, 2));
 		
 		int levels;
 		if (nodeAttributes.hasAttribute(node.getIdentifier(), "levels")) {
 			levels = nodeAttributes.getIntegerAttribute(node.getIdentifier(), "levels");
 		} else if (networkAttributes.hasAttribute(Cytoscape.getCurrentNetwork().getIdentifier(), "levels")) {
 			levels = networkAttributes.getIntegerAttribute(Cytoscape.getCurrentNetwork().getIdentifier(), "levels");
 		} else {
 			levels = 15;
 		}

 		final JLabel totalLevelsLabel = new JLabel("Total activity levels: " + levels);
 		values.add(totalLevelsLabel);
 		final JSlider totalLevels = new JSlider(1, 100);
 		totalLevels.setValue(levels);
 		totalLevels.setMajorTickSpacing(20);
 		totalLevels.setMinorTickSpacing(10);
 		
 		totalLevels.setPaintLabels(true);
 		totalLevels.setPaintTicks(true);
 		if (totalLevels.getMaximum() == 100) {
 			Dictionary labelTable = totalLevels.getLabelTable();
 			labelTable.put(totalLevels.getMaximum(), new JLabel("" + totalLevels.getMaximum()));
 			totalLevels.setLabelTable(labelTable);
 		}
 		values.add(totalLevels);
 		
 		
 		final JSlider initialConcentration = new JSlider(0, levels);
 		initialConcentration.setValue(nodeAttributes.getIntegerAttribute(node.getIdentifier(), "initialConcentration"));
 		
 		final JLabel initialConcentrationLabel = new JLabel("Initial activity level: " + initialConcentration.getValue());
 		values.add(initialConcentrationLabel);
 
 
 		initialConcentration.setMajorTickSpacing(levels / 5);
 		initialConcentration.setMinorTickSpacing(levels / 10);
 		
 		initialConcentration.setPaintLabels(true);
 		initialConcentration.setPaintTicks(true);
 
 		values.add(initialConcentration);
 
 		this.add(values, BorderLayout.CENTER);
 
 
 		totalLevels.addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				totalLevelsLabel.setText("Total activity levels: " + totalLevels.getValue());
 				if (totalLevels.getValueIsAdjusting()) return;
 				double prevMax = initialConcentration.getMaximum(),
 					   currMax = totalLevels.getValue();
 				int currValue = (int)((initialConcentration.getValue()) / prevMax * currMax);
 				initialConcentration.setMaximum(totalLevels.getValue());
 				initialConcentration.setValue(currValue);
 				int space = (initialConcentration.getMaximum() - initialConcentration.getMinimum() + 1) / 5;
 				if (space < 1) space = 1;
 				initialConcentration.setMajorTickSpacing(space);
 				initialConcentration.setMinorTickSpacing(space / 2);
 				Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
 				for (int i=initialConcentration.getMinimum();i<=initialConcentration.getMaximum();i+=space) {
 					labelTable.put(i, new JLabel("" + i));
 				}
 				initialConcentration.setLabelTable(labelTable);
 				initialConcentrationLabel.setText("Initial activity level: " + initialConcentration.getValue());
 				initialConcentration.setValue(currValue);
 			}
 			
 		});
 		
 		
 		initialConcentration.addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				//if (initialConcentration.getValueIsAdjusting()) return;
 				initialConcentrationLabel.setText("Initial activity level: " + initialConcentration.getValue());
 			}
 			
 		});
 		
 		JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		controls.add(new JButton(new AbstractAction("Save") {
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = -6179643943409321939L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
 				nodeAttributes.setAttribute(node.getIdentifier(), "initialConcentration",
 						initialConcentration.getValue());
 				
 				nodeAttributes.setAttribute(node.getIdentifier(), "levels", totalLevels.getValue());
 
 				Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
 
 				NodeDialog.this.dispose();
 			}
 		}));
 
 		controls.add(new JButton(new AbstractAction("Cancel") {
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = -2038333013177775241L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// discard changes
 				NodeDialog.this.dispose();
 			}
 		}));
 
 		this.add(controls, BorderLayout.SOUTH);
 	}
 }
