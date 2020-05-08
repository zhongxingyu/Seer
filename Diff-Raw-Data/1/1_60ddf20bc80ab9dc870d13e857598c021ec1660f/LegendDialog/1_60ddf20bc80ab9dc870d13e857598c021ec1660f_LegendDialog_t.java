 /*
  * LegendDialog.java
  */
 package cytoscape.visual.ui;
 
 import cytoscape.visual.EdgeAppearanceCalculator;
 import cytoscape.visual.NodeAppearanceCalculator;
 import cytoscape.visual.VisualPropertyType;
 import cytoscape.visual.VisualStyle;
 
 import cytoscape.visual.calculators.Calculator;
 
 import cytoscape.visual.mappings.ObjectMapping;
 import cytoscape.visual.mappings.PassThroughMapping;
 
 import org.freehep.util.export.ExportDialog;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.border.TitledBorder;
 
 
 /**
  *
  */
 public class LegendDialog extends JDialog {
 	private VisualStyle visualStyle;
 	private JPanel jPanel1;
 	private JButton jButton1;
 	private JButton jButton2;
 	private JScrollPane jScrollPane1;
 	private Component parent;
 
 	/**
 	 * Creates a new LegendDialog object.
 	 *
 	 * @param parent  DOCUMENT ME!
 	 * @param vs  DOCUMENT ME!
 	 */
 	public LegendDialog(Dialog parent, VisualStyle vs) {
 		super(parent, true);
 		visualStyle = vs;
 		this.parent = parent;
 		initComponents();
 	}
 
 	/**
 	 * Creates a new LegendDialog object.
 	 *
 	 * @param parent  DOCUMENT ME!
 	 * @param vs  DOCUMENT ME!
 	 */
 	public LegendDialog(JFrame parent, VisualStyle vs) {
 		super(parent, true);
 		visualStyle = vs;
 		this.parent = parent;
 		initComponents();
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param visualStyle DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public static JPanel generateLegendPanel(VisualStyle visualStyle) {
 		final JPanel legend = new JPanel();
 
 		final NodeAppearanceCalculator nac = visualStyle.getNodeAppearanceCalculator();
 		final List<Calculator> nodeCalcs = nac.getCalculators();
 		final EdgeAppearanceCalculator eac = visualStyle.getEdgeAppearanceCalculator();
 		final List<Calculator> edgeCalcs = eac.getCalculators();
 
 		ObjectMapping om;
 
 		/*
 		 * Set layout
 		 */
 		legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
 		legend.setBackground(Color.white);
 
 		legend.setBorder(new TitledBorder(new LineBorder(Color.DARK_GRAY, 2),
 		                                  "Visual Legend for " + visualStyle.getName(),
 		                                  TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.CENTER,
 		                                  new Font("SansSerif", Font.BOLD, 16), Color.DARK_GRAY));
 
 		for (Calculator calc : nodeCalcs) {
 			// AAARGH
 			if (nac.getNodeSizeLocked()) {
 				if (calc.getVisualPropertyType() == VisualPropertyType.NODE_WIDTH)
 					continue;
 				else if (calc.getVisualPropertyType() == VisualPropertyType.NODE_HEIGHT)
 					continue;
 			} else {
 				if (calc.getVisualPropertyType() == VisualPropertyType.NODE_SIZE)
 					continue;
 			}
 
 			om = calc.getMapping(0);
 
 			JPanel mleg = om.getLegend(calc.getVisualPropertyType());
 
 			// Add passthrough mappings to the top since they don't
 			// display anything besides the title.
 			if (om instanceof PassThroughMapping)
 				legend.add(mleg, 0);
 			else
 				legend.add(mleg);
 
 			// Set padding
 			mleg.setBorder(new EmptyBorder(15, 30, 15, 30));
 		}
 
 		int top = legend.getComponentCount();
 
 		for (Calculator calc : edgeCalcs) {
 			om = calc.getMapping(0);
 
 			JPanel mleg = om.getLegend(calc.getVisualPropertyType());
 
 			// Add passthrough mappings to the top since they don't
 			// display anything besides the title.
 			if (om instanceof PassThroughMapping)
 				legend.add(mleg, 0);
 			else
 				legend.add(mleg);
 
 			//			 Set padding
 			mleg.setBorder(new EmptyBorder(15, 30, 15, 30));
 		}
 
 		return legend;
 	}
 
 	private void initComponents() {
 		this.setTitle("Visual Legend for " + visualStyle.getName());
 
 		jPanel1 = generateLegendPanel(visualStyle);
 
 		jScrollPane1 = new JScrollPane();
 		jScrollPane1.setViewportView(jPanel1);
 
 		jButton1 = new JButton();
 		jButton1.setText("Export");
 		jButton1.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					export();
 				}
 			});
 
 		jButton2 = new JButton();
 		jButton2.setText("Cancel");
 		jButton2.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					dispose();
 				}
 			});
 
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.add(jButton1);
 		buttonPanel.add(jButton2);
 
 		JPanel containerPanel = new JPanel();
 		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
 		containerPanel.add(jScrollPane1);
 		containerPanel.add(buttonPanel);
 
 		setContentPane(containerPanel);
 		setPreferredSize(new Dimension(650, 500));
 		pack();
 		repaint();
 	}
 
 	private void export() {
 		ExportDialog export = new ExportDialog();
 		export.showExportDialog(parent, "Export legend as ...", jPanel1, "export");
		dispose();
 	}
 }
