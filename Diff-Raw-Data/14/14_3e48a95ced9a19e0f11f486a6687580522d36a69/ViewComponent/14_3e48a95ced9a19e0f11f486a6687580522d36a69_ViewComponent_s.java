 package uk.ac.brighton.vmg.cviz.ui;
 
 /**
  * The ViewComponent of the ConceptViz plugin.
  * 
  * Copyright (c) 2013 The ConceptViz authors (see the file AUTHORS).
  * See the file LICENSE for copying permission.
  */
 import icircles.concreteDiagram.ConcreteDiagram;
 import icircles.concreteDiagram.DiagramCreator;
 import icircles.gui.CirclesPanel;
 import icircles.input.AbstractDiagram;
 import icircles.input.Spider;
 import icircles.input.Zone;
 import icircles.util.CannotDrawException;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
 import org.apache.log4j.Logger;
 import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
 import org.semanticweb.owlapi.model.OWLClass;
 
 import uk.ac.brighton.vmg.cviz.syntax.AbstractDiagramBuilder;
 
 public class ViewComponent extends AbstractOWLClassViewComponent {
 	private static final long serialVersionUID = -4515710047558710080L;
 	public static final int CVIZ_VERSION_MAJOR = 0;
 	public static final int CVIZ_VERSION_MINOR = 1;
 	public static final String CVIZ_VERSION_STATUS = "alpha";
 	private JPanel cdPanel;
 	private JComboBox<String> depthPicker;
 	private boolean showInd = false;
 	private OWLClass theSelectedClass;
 	private Thread buildRunner;
 
 	private int DIAG_SIZE;
 	private final double DIAG_SCALE = 0.9;
 	private int hierarchyDepth = 2;
 	private AbstractDiagramBuilder builder;
 
 	private static final Logger log = Logger.getLogger(ViewComponent.class);
 	private static final int IC_VERSION = 1;
 
 	/**
 	 * Not used.
 	 */
 	@Override
 	public void disposeView() {
 		//
 	}
 
 	/**
 	 * The Protege API callback when the view is first loaded. Sets up the GUI
 	 * but doesn't start the process of drawing anything.
 	 */
 	@Override
 	public void initialiseClassView() throws Exception {
 		getView().setSyncronizing(true);
 		setLayout(new BorderLayout(6, 6));
 
 		JPanel topPanel = new JPanel();
 		JLabel depthLabel = new JLabel("Depth:");
 		String[] depths = { "1", "2", "3", "4", "5" };
 		depthPicker = new JComboBox<String>(depths);
 		depthPicker.setSelectedIndex(1);
 		depthPicker.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				hierarchyDepth = Integer.parseInt((String) depthPicker
 						.getSelectedItem());
 				if (theSelectedClass != null)
 					updateView(theSelectedClass);
 			}
 		});
 		topPanel.add(depthLabel);
 		topPanel.add(depthPicker);
 		JCheckBox showIndCB = new JCheckBox("Show individuals:");
 		showIndCB.setSelected(showInd);
 		showIndCB.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				showInd = (e.getStateChange() == ItemEvent.SELECTED);
 				log.info("drawing with spiders: " + showInd);
 				if (theSelectedClass != null)
 					updateView(theSelectedClass);
 			}
 		});
 		topPanel.add(showIndCB);
 
 		add(topPanel, BorderLayout.NORTH);
 		cdPanel = new JPanel();
 		cdPanel.setBackground(Color.WHITE);
 		add(cdPanel, BorderLayout.CENTER);
 
 		log.debug("CD View Component initialized");
 	}
 
 	/**
 	 * Callback when user selects a class in a hierarchy viewer pane. Constructs
 	 * the diagram with the selectedClass as its top-level element using an
 	 * AbstractDiagramBuilder.
 	 */
 	@Override
 	protected OWLClass updateView(OWLClass selectedClass) {
 		// DIAG_SIZE = Math.max(getHeight(), getWidth()) - 100;
 		theSelectedClass = selectedClass;
 		DIAG_SIZE = getHeight() - 50;
 
 		if (selectedClass != null) {
 			displayInfProgress();
 			if (builder != null)
 				builder.notifyStop();
 			builder = new AbstractDiagramBuilder(this, selectedClass,
 					getOWLModelManager(), hierarchyDepth, showInd);
 			buildRunner = new Thread(builder);
 			buildRunner.start();
 		}
 		return selectedClass;
 	}
 
 	public void diagramReady(String[] cs, Zone[] zs, Zone[] szs, Spider[] sps) {
 		Spider[] theSps = (showInd) ? sps : new Spider[] {};
 		debug("Curves", cs);
 		debug("Zones", zs);
 		debug("Shaded zones", szs);
 		debug("Individuals", sps);
 		drawCD(cs, zs, szs, theSps);
 	}
 
 	/**
 	 * Pass the generated abstract description to iCircles and display the
 	 * result in a JPanel.
 	 * 
 	 * @param c
 	 *            the abstract curves
 	 * @param z
 	 *            the abstract zones
 	 * @param sz
 	 *            the abstract shaded zones
 	 */
 	private void drawCD(final String[] c, final Zone[] z, final Zone[] sz,
 			final Spider[] sps) {
 		new Thread(new Runnable() {
 			public void run() {
 				AbstractDiagram ad = new AbstractDiagram(IC_VERSION, c, z, sz, sps);
 				DiagramCreator dc = new DiagramCreator(ad.toAbstractDescription());
 				try {
 					final ConcreteDiagram cd = dc.createDiagram(DIAG_SIZE);
 					SwingUtilities.invokeLater(new Runnable() {
 						public void run() {
 							displayDiagram(cd);
 						}
 					});
 				} catch (final CannotDrawException e) {
 					SwingUtilities.invokeLater(new Runnable() {
 						public void run() {
							displayMessage(e.getMessage());
 						}
 					});
 				}
 			}
 		}).start();
 	}
 
 	/**
 	 * Callback for the runnable that creates the concrete diagram by calling
 	 * iCircles.
 	 * 
 	 * @param cd
 	 */
 	private void displayDiagram(ConcreteDiagram cd) {
 		Font font = new Font("Helvetica", Font.BOLD | Font.ITALIC, 16);
 		String failureMessage = null;
 		cd.setFont(font);
 		CirclesPanel cp = new CirclesPanel("", failureMessage, cd, true);// do
 																			// use
 																			// colours
 		cp.setScaleFactor(DIAG_SCALE);
 		cdPanel.removeAll();
 		cdPanel.add(cp);
 		cdPanel.validate();
 	}
 
 	/**
 	 * Display an error message or other warning to the user in the main panel.
 	 * 
 	 * @param message
 	 *            the message to display
 	 */
 	public void displayMessage(String message) {
 		JTextField tf = new JTextField(message);
 		cdPanel.removeAll();
 		cdPanel.setBackground(Color.WHITE);
 		cdPanel.add(tf, BorderLayout.CENTER);
 		cdPanel.revalidate();
 		cdPanel.repaint();
 	}
 
 	private void displayInfProgress() {
 		cdPanel.removeAll();
 		JTextField tf = new JTextField("Building diagram...");
 		JProgressBar pBar = new JProgressBar();
 		pBar.setIndeterminate(true);
 		cdPanel.setBackground(Color.WHITE);
 		cdPanel.add(tf, BorderLayout.NORTH);
 		cdPanel.add(pBar, BorderLayout.CENTER);
 
 		cdPanel.revalidate();
 		cdPanel.repaint();
 	}
 
 	private <T> void debug(String name, Object[] xs) {
 		log.info("::::::::::   " + name + "   ::::::::::");
 		for (Object x : xs)
 			log.info(x);
 	}
 
 }
