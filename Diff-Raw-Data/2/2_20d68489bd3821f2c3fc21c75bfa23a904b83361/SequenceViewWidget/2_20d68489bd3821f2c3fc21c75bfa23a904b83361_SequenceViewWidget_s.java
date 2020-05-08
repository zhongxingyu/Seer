 package org.geworkbench.util.sequences;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputMethodEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.geom.Rectangle2D;
 import java.beans.PropertyChangeEvent;
 import java.util.HashMap;
 import java.util.TreeSet;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 
 import org.geworkbench.bison.datastructure.biocollections.Collection;
 import org.geworkbench.bison.datastructure.biocollections.DSCollection;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
 import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
 import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
 import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
 import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
 import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
 import org.geworkbench.engine.management.Subscribe;
 import org.geworkbench.events.GeneSelectorEvent;
 import org.geworkbench.events.MicroarraySetViewEvent;
 import org.geworkbench.events.SequenceDiscoveryTableEvent;
 import org.geworkbench.util.Util;
 import org.geworkbench.util.patterns.CSMatchedSeqPattern;
 import org.geworkbench.util.patterns.PatternLocations;
 import org.geworkbench.util.patterns.PatternOperations;
 import org.geworkbench.util.patterns.PatternSequenceDisplayUtil;
 
 /**
  * Widget provides all GUI services for sequence panel displays.
  * 
  * Widget is controlled by its associated component, SequenceViewAppComponent
  * Copyright: Copyright (c) 2003
  * 
  * Company: Califano Lab
  * </p>
  * 
  * @author
  * @version $Id$
  */
 
 @SuppressWarnings("unchecked")
 public class SequenceViewWidget extends JPanel {
 	private static final long serialVersionUID = -6141589995966150788L;
 
 	public HashMap<CSSequence, PatternSequenceDisplayUtil> patternLocationsMatches;
 	public DSCollection<DSMatchedPattern<DSSequence, CSSeqRegistration>> selectedPatterns = new Collection<DSMatchedPattern<DSSequence, CSSeqRegistration>>();
 	public JToolBar jToolBar1 = new JToolBar();
 	public static final String NONBASIC = "NONBASIC";
 
 	protected SequenceViewWidgetPanel seqViewWPanel = new SequenceViewWidgetPanel();
 	protected CSSequenceSet activeSequenceDB = null;
 	protected boolean subsetMarkerOn = true;
 	protected DSPanel<? extends DSGeneMarker> activatedMarkers = null;
 
 	private int prevSeqId = -1;
 	private int prevSeqDx = 0;
 	private DSSequenceSet sequenceDB = new CSSequenceSet();
 	private DSSequenceSet orgSequenceDB = new CSSequenceSet();
 	private DSSequenceSet displaySequenceDB = new CSSequenceSet();
 
 	private BorderLayout borderLayout2 = new BorderLayout();
 
 	// Panels and Panes
 	private JDetailPanel sequencedetailPanel = new JDetailPanel();
 	private JPanel bottomPanel = new JPanel();
 	private JButton leftShiftButton = new JButton();
 	private JButton rightShiftButton = new JButton();
 	private JScrollPane seqScrollPane = new JScrollPane();
 
 	private JCheckBox showAllBtn = new JCheckBox();
 	private JCheckBox jAllSequenceCheckBox = new JCheckBox();
 	private JLabel jViewLabel = new JLabel();
 	private JComboBox jViewComboBox = new JComboBox();
 	private static final String LINEVIEW = "Line";
 	private static final String FULLVIEW = "Full Sequence";
 	private JTextField jSequenceSummaryTextField = new JTextField();
 	private boolean isLineView = true; // true is for LineView.
 	private boolean onlyShowPattern = false;
 	private static final String LEFT = "left";
 	private static final String RIGHT = "right";
 	private boolean goLeft = false;
 	private int xStartPoint = -1;
 	private static final int GAP = 40;
 
 	public SequenceViewWidget() {
 		try {
 			jbInit();
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	void jbInit() throws Exception {
 
 		this.setLayout(borderLayout2);
 		sequencedetailPanel.setBorder(BorderFactory.createEtchedBorder());
 		sequencedetailPanel.setMinimumSize(new Dimension(50, 40));
 		sequencedetailPanel.setPreferredSize(new Dimension(50, 40));
 		seqScrollPane.setBorder(BorderFactory.createEtchedBorder());
 		seqViewWPanel.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				jDisplayPanel_mouseClicked(e);
 			}
 		});
 		this.addInputMethodListener(new java.awt.event.InputMethodListener() {
 			public void inputMethodTextChanged(InputMethodEvent e) {
 			}
 
 			public void caretPositionChanged(InputMethodEvent e) {
 				this_caretPositionChanged(e);
 			}
 		});
 		this.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent e) {
 				this_propertyChange(e);
 			}
 		});
 		showAllBtn
 				.setToolTipText("Click to show sequences with selected patterns.");
 		showAllBtn.setText("All / Matching Pattern");
 		showAllBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				showAllBtn_actionPerformed(e);
 			}
 		});
 		jAllSequenceCheckBox.setToolTipText("Click to display all sequences.");
 		jAllSequenceCheckBox.setSelected(false);
 		jAllSequenceCheckBox.setText("All Sequences");
 		jAllSequenceCheckBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jAllSequenceCheckBox_actionPerformed(e);
 			}
 		});
 		jViewLabel.setText("View: ");
 		jSequenceSummaryTextField
 				.setText("Move the mouse over to see details.");
 
 		seqViewWPanel.addMouseMotionListener(new MouseMotionAdapter() {
 			public void mouseMoved(MouseEvent e) {
 				seqViewWPanel_mouseMoved(e);
 			}
 		});
 		jViewComboBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jViewComboBox_actionPerformed(e);
 			}
 		});
 		jViewComboBox.setToolTipText("Select a view to display results.");
 		bottomPanel = new JPanel();
 		leftShiftButton = new JButton();
 
 		ImageIcon leftButtonIcon = Util.createImageIcon("/images/back.gif");
 		leftShiftButton.setIcon(leftButtonIcon);
 		ImageIcon rightButtonIcon = Util.createImageIcon("/images/forward.gif");
 		rightShiftButton = new JButton();
 		rightShiftButton.setIcon(rightButtonIcon);
 		bottomPanel.setLayout(new BorderLayout());
 		bottomPanel.add(leftShiftButton, BorderLayout.WEST);
 		bottomPanel.add(sequencedetailPanel, BorderLayout.CENTER);
 		bottomPanel.add(rightShiftButton, BorderLayout.EAST);
 		leftShiftButton.setEnabled(false);
 		rightShiftButton.setEnabled(false);
 		leftShiftButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setMoveDirection(LEFT);
 				updateBottomPanel();
 			}
 		});
 		rightShiftButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setMoveDirection(RIGHT);
 				updateBottomPanel();
 			}
 		});
 
 		this.add(bottomPanel, BorderLayout.SOUTH);
 		this.add(seqScrollPane, BorderLayout.CENTER);
 		this.add(jToolBar1, BorderLayout.NORTH);
 		jToolBar1.add(jViewLabel);
 		jToolBar1.add(jViewComboBox);
 		jToolBar1.add(showAllBtn);
 		jToolBar1.add(jAllSequenceCheckBox);
 		jToolBar1.addSeparator();
 		// jToolBar1.add(jSequenceSummaryTextField);
 		jViewComboBox.addItem(LINEVIEW);
 		jViewComboBox.addItem(FULLVIEW);
		jViewComboBox.setSize(jViewComboBox.getPreferredSize());
 		if (sequenceDB != null) {
 			seqViewWPanel.setMaxSeqLen(sequenceDB.getMaxLength());
 		}
 
 		seqScrollPane.getViewport().add(seqViewWPanel, null);
 		seqViewWPanel.setShowAll(showAllBtn.isSelected());
 	}
 
 	/**
 	 * cleanButtons
 	 * 
 	 * @param aString
 	 *            String
 	 */
 	public void removeButtons(String aString) {
 		if (aString.equals(NONBASIC)) {
 			jToolBar1.remove(showAllBtn);
 			jToolBar1.remove(jAllSequenceCheckBox); // fix bug 924
 			jToolBar1.remove(jSequenceSummaryTextField);
 			repaint();
 		}
 	}
 
 	private void setMoveDirection(String directionStr) {
 		if (directionStr.equals(LEFT)) {
 			goLeft = true;
 		} else {
 			goLeft = false;
 		}
 	}
 
 	/**
 	 * receiveProjectSelection
 	 * 
 	 * @param e
 	 *            ProjectEvent
 	 */
 	@Subscribe
 	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
 		if (e.getMessage().equals(org.geworkbench.events.ProjectEvent.CLEARED)) {
 			// refMASet = null;
 			fireModelChangedEvent(null);
 		} else {
 			DSDataSet dataSet = e.getDataSet();
 			if (dataSet instanceof DSSequenceSet) {
 				if (orgSequenceDB != dataSet) {
 					this.orgSequenceDB = (DSSequenceSet) dataSet;
 					selectedPatterns = null;
 					// activatedMarkers = null;
 				}
 			}
 			// refreshMaSetView();
 		}
 		refreshMaSetView();
 	}
 
 	/**
 	 * geneSelectorAction
 	 * 
 	 * @param e
 	 *            GeneSelectorEvent
 	 */
 	public void sequenceDBUpdate(GeneSelectorEvent e) {
 		if (e.getPanel() == null) {
 			return;
 		}
 		if (e.getPanel() != null && e.getPanel().size() > 0) {
 			activatedMarkers = e.getPanel().activeSubset();
 		} else {
 			activatedMarkers = null;
 		}
 		refreshMaSetView();
 	}
 
 	protected void refreshMaSetView() {
 		getDataSetView();
 	}
 
 	protected void fireModelChangedEvent(MicroarraySetViewEvent event) {
 		this.repaint();
 	}
 
 	public void getDataSetView() {
 		subsetMarkerOn = !jAllSequenceCheckBox.isSelected();
 		if (subsetMarkerOn) {
 			if (activatedMarkers != null && activatedMarkers.size() > 0) {
 
 				if (subsetMarkerOn && (orgSequenceDB != null)) {
 					// createActivatedSequenceSet();
 					activeSequenceDB = (CSSequenceSet) ((CSSequenceSet) orgSequenceDB)
 							.getActiveSequenceSet(activatedMarkers);
 				}
 
 			} else if (orgSequenceDB != null) {
 				activeSequenceDB = (CSSequenceSet) orgSequenceDB;
 			}
 
 		} else if (orgSequenceDB != null) {
 			activeSequenceDB = (CSSequenceSet) orgSequenceDB;
 		}
 		if (activeSequenceDB != null) {
 			sequenceDB = activeSequenceDB;
 			initPanelView();
 		}
 	}
 
 	public void patternSelectionHasChanged(SequenceDiscoveryTableEvent e) {
 		setPatterns(e.getPatternMatchCollection());
 		refreshMaSetView();
 
 	}
 
 	public void updateBottomPanel() {
 
 		DSSequence selectedSequence = seqViewWPanel.getSelectedSequence();
 		if (selectedSequence == null) {
 			return;
 		}
 		if (goLeft) {
 			if (xStartPoint - GAP > 0) {
 				xStartPoint = xStartPoint >= GAP ? xStartPoint - GAP : 1;
 			} else {
 				xStartPoint = 1;
 				leftShiftButton.setEnabled(false);
 			}
 		} else {
 			if (xStartPoint < selectedSequence.length() - GAP) {
 				xStartPoint += GAP;
 			} else {
 				rightShiftButton.setEnabled(false);
 			}
 		}
 		sequencedetailPanel.repaint();
 
 		prevSeqDx = xStartPoint;
 		sequencedetailPanel.setOpaque(false);
 
 	}
 
 	/**
 	 * Update the detail of sequence.
 	 * 
 	 * @param e
 	 *            MouseEvent
 	 */
 	private void jDisplayPanel_mouseClicked(MouseEvent e) {
 		seqViewWPanel.this_mouseClicked(e);
 		xStartPoint = seqViewWPanel.getSeqXclickPoint();
 		sequencedetailPanel.repaint();
 	}
 
 	private void showPatterns() {
 		if (selectedPatterns.size() > 0) {
 			for (int i = 0; i < selectedPatterns.size(); i++) {
 				DSMatchedSeqPattern pattern = (DSMatchedSeqPattern) selectedPatterns
 						.get(i);
 				if (pattern instanceof CSMatchedSeqPattern) {
 					if (pattern.getASCII() == null) {
 						PatternOperations.fill((CSMatchedSeqPattern) pattern,
 								displaySequenceDB);
 					}
 					// ( (DefaultListModel)
 					// patternList.getModel()).addElement(pattern);
 					this.repaint();
 				}
 			}
 		}
 	}
 
 	private void this_caretPositionChanged(InputMethodEvent e) {
 		showPatterns();
 	}
 
 	private void this_propertyChange(PropertyChangeEvent e) {
 		showPatterns();
 	}
 
 	public void setSequenceDB(DSSequenceSet db) {
 		orgSequenceDB = db;
 		sequenceDB = db;
 		displaySequenceDB = db;
 		refreshMaSetView();
 		// selectedPatterns = new ArrayList();
 		if (sequenceDB != null) {
 			seqViewWPanel.setMaxSeqLen(sequenceDB.getMaxLength());
 			// seqViewWPanel.initialize(null, db);
 			selectedPatterns.clear();
 			repaint();
 		}
 	}
 
 	public void setDirection(boolean direction) {
 		this.goLeft = direction;
 	}
 
 	public DSSequenceSet getSequenceDB() {
 		return sequenceDB;
 	}
 
 	public SequenceViewWidgetPanel getSeqViewWPanel() {
 		return seqViewWPanel;
 	}
 
 	public boolean isDirection() {
 		return goLeft;
 	}
 
 	public void setPatterns(
 			DSCollection<DSMatchedPattern<DSSequence, CSSeqRegistration>> matches) {
 		selectedPatterns.clear();
 		for (int i = 0; i < matches.size(); i++) {
 			selectedPatterns.add(matches.get(i));
 		}
 	}
 
 	private void showAllBtn_actionPerformed(ActionEvent e) {
 		if (selectedPatterns == null && showAllBtn.isSelected()) {
 			if (sequenceDB == null) {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"No sequence is stored right now, please load sequences first.",
 								"No Pattern", JOptionPane.ERROR_MESSAGE);
 			} else {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"No pattern is stored right now, please generate patterns with Pattern Discory module first.",
 								"No Pattern", JOptionPane.ERROR_MESSAGE);
 				seqViewWPanel.setMaxSeqLen(sequenceDB.getMaxLength());
 				displaySequenceDB = sequenceDB;
 				seqViewWPanel.setShowAll(false);
 			}
 
 			showAllBtn.setSelected(false);
 
 		}
 		initPanelView();
 
 	}
 
 	public void jViewComboBox_actionPerformed(ActionEvent e) {
 		initPanelView();
 	}
 
 	public void seqViewWPanel_mouseMoved(MouseEvent e) {
 
 		seqViewWPanel.this_mouseMoved(e);
 		jSequenceSummaryTextField.setText(seqViewWPanel.getDisplayInfo());
 
 	}
 
 	/**
 	 * Transform the patterns to patternsUtil class. Child class should override
 	 * this method.
 	 */
 	public void updatePatternSeqMatches() {
 		patternLocationsMatches = PatternOperations.processPatterns(
 				selectedPatterns, sequenceDB);
 
 	}
 
 	/**
 	 * Initiate the Panel, which should be used as the entry point.
 	 * 
 	 * @return boolean
 	 */
 	public boolean initPanelView() {
 		updatePatternSeqMatches();
 		isLineView = jViewComboBox.getSelectedItem().equals(LINEVIEW);
 		onlyShowPattern = showAllBtn.isSelected();
 
 		if (onlyShowPattern) {
 			displaySequenceDB = new CSSequenceSet();
 		}
 
 		// if (patternLocationsMatches != null && sequenceDB != null) {
 		if (patternLocationsMatches != null && sequenceDB != null) {
 			seqViewWPanel.setMaxSeqLen(sequenceDB.getMaxLength());
 			for (int i = 0; i < sequenceDB.size(); i++) {
 				DSSequence sequence = sequenceDB.getSequence(i);
 				PatternSequenceDisplayUtil pdu = patternLocationsMatches
 						.get(sequence);
 				if (onlyShowPattern) {
 					if (pdu.hasPattern()) {
 						displaySequenceDB.addASequence(sequence);
 					}
 				}
 			}
 			if (onlyShowPattern) {
 				seqViewWPanel.initialize(patternLocationsMatches,
 						displaySequenceDB, isLineView);
 			} else {
 				seqViewWPanel.initialize(patternLocationsMatches, sequenceDB,
 						isLineView);
 			}
 
 		} else {
 			if (sequenceDB != null) {
 				seqViewWPanel.setMaxSeqLen(sequenceDB.getMaxLength());
 				displaySequenceDB = sequenceDB;
 				seqViewWPanel.setShowAll(false);
 				// seqViewWPanel.initialize(selectedPatterns, sequenceDB,
 				// isLineView);
 				showAllBtn.setSelected(false);
 				seqViewWPanel.initialize(patternLocationsMatches,
 						displaySequenceDB, isLineView);
 
 			} else {
 				seqViewWPanel.removeAll();
 			}
 		}
 
 		return true;
 	}
 
 	public void jAllSequenceCheckBox_actionPerformed(ActionEvent e) {
 		refreshMaSetView();
 	}
 
 	private class JDetailPanel extends JPanel {
 		private static final long serialVersionUID = -1720620118289765818L;
 
 		public void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			DSSequence selectedSequence = seqViewWPanel.getSelectedSequence();
 			if (selectedSequence == null) {
 				g.clearRect(0, 0, sequencedetailPanel.getWidth(),
 						sequencedetailPanel.getHeight());
 				rightShiftButton.setEnabled(false);
 				leftShiftButton.setEnabled(false);
 				return;
 
 			}
 			if (xStartPoint < 0 || xStartPoint >= selectedSequence.length()) {
 				rightShiftButton.setEnabled(false);
 				leftShiftButton.setEnabled(false);
 				return;
 			}
 			if (xStartPoint >= 0
 					&& xStartPoint < selectedSequence.length() - 2 * GAP) {
 				rightShiftButton.setEnabled(true);
 			} else {
 				rightShiftButton.setEnabled(false);
 			}
 			if (xStartPoint > GAP) {
 				leftShiftButton.setEnabled(true);
 			} else {
 				leftShiftButton.setEnabled(false);
 			}
 			final Font font = new Font("Courier", Font.BOLD, 10);
 			int seqId = -1;
 			int seqDx = -1;
 			if (sequenceDB != null) {
 				for (int i = 0; i < sequenceDB.size(); i++) {
 					DSSequence seq = sequenceDB.getSequence(i);
 					if (seq == selectedSequence) {
 						seqId = i;
 					}
 				}
 			}
 			seqDx = xStartPoint;
 
 			DSSequence sequence = selectedSequence;
 			// Check if we are clicking on a new sequence
 			if ((seqId >= 0) && (seqId != prevSeqId) || (seqDx != prevSeqDx)) {
 				g.clearRect(0, 0, sequencedetailPanel.getWidth(),
 						sequencedetailPanel.getHeight());
 				g.setFont(font);
 				if (sequence != null && (seqDx >= 0)
 						&& (seqDx < sequence.length())) {
 					// turn anti alising on
 					((Graphics2D) g).setRenderingHint(
 							RenderingHints.KEY_ANTIALIASING,
 							RenderingHints.VALUE_ANTIALIAS_ON);
 					// shift the selected pattern/sequence into middle of the
 					// panel.
 					int startPoint = 0;
 					if (seqDx > GAP) {
 						startPoint = seqDx / 10 * 10 - GAP;
 					}
 					FontMetrics fm = g.getFontMetrics(font);
 
 					String seqAscii = sequence.getSequence().substring(
 							startPoint);
 					Rectangle2D r2d = fm.getStringBounds(seqAscii, g);
 					int seqLength = seqAscii.length();
 					double xscale = (r2d.getWidth() + 3) / (double) (seqLength);
 					double yscale = 0.6 * r2d.getHeight();
 					g.drawString(seqAscii, 10, 20);
 					int paintPoint = 0;
 					while (paintPoint < seqLength) {
 						g.drawString("|", 10 + (int) (paintPoint * xscale),
 								(int) (GAP / 2 + yscale));
 						g.drawString(new Integer(paintPoint + 1 + startPoint)
 								.toString(), 10 + (int) (paintPoint * xscale),
 								(int) (GAP / 2 + 2 * yscale));
 						paintPoint += GAP;
 					}
 
 					if (patternLocationsMatches != null) {
 						PatternSequenceDisplayUtil psd = patternLocationsMatches
 								.get(sequence);
 						if (psd == null) {
 							return;
 						}
 						TreeSet<PatternLocations> patternsPerSequence = psd
 								.getTreeSet();
 						if (patternsPerSequence != null
 								&& patternsPerSequence.size() > 0) {
 							for (PatternLocations pl : patternsPerSequence) {
 								CSSeqRegistration registration = pl
 										.getRegistration();
 								if (registration != null) {
 									Rectangle2D r = fm.getStringBounds(
 											seqAscii, g);
 									double scale = (r.getWidth() + 3)
 											/ (double) (seqAscii.length());
 									CSSeqRegistration seqReg = (CSSeqRegistration) registration;
 									int patLength = pl.getAscii().length();
 									int dx = seqReg.x1;
 									double x1 = (dx - startPoint) * scale + 10;
 									double x2 = ((double) patLength) * scale;
 									if (pl.getPatternType().equals(
 											PatternLocations.TFTYPE)) {
 										x2 = Math.abs(registration.x2
 												- registration.x1)
 												* scale;
 									}
 									g.setColor(PatternOperations
 											.getPatternColor(new Integer(pl
 													.getIdForDisplay())));
 									g.drawRect((int) x1, 2, (int) x2, 23);
 									g.drawString("|", (int) x1,
 											(int) (GAP / 2 + yscale));
 									g.drawString("|", (int) (x1 + x2 - scale),
 											(int) (GAP / 2 + yscale));
 									g.drawString(
 											new Integer(dx + 1).toString(),
 											(int) x1,
 											(int) (GAP / 2 + 2 * yscale));
 									g.drawString(new Integer(dx
 											+ seqReg.length()).toString(),
 											(int) (x1 + x2 - scale),
 											(int) (GAP / 2 + 2 * yscale));
 									if (pl.getPatternType().equals(
 											PatternLocations.TFTYPE)) {
 
 										g
 												.setColor(SequenceViewWidgetPanel.DRECTIONCOLOR);
 
 										int shape = 3;
 										int[] xi = new int[shape];
 										int[] yi = new int[shape];
 										int triangleSize = 8;
 										if (registration.strand == 0) {
 											xi[0] = xi[1] = (int) x1;
 											yi[0] = 2;
 											yi[1] = 2 + triangleSize;
 											xi[2] = xi[0] + triangleSize / 2;
 											yi[2] = 2 + triangleSize / 2;
 											// g.drawPolyline(xi, yi,
 											// addtionalPoint);
 										} else {
 											xi[0] = xi[1] = (int) (x1 + x2);
 											yi[0] = 2;
 											yi[1] = 2 + triangleSize;
 											xi[2] = xi[0] - triangleSize / 2;
 											yi[2] = 2 + triangleSize / 2;
 
 										}
 
 										g.drawPolygon(xi, yi, shape);
 										g.fillPolygon(xi, yi, shape);
 
 									}
 
 								}
 
 							}
 						}
 					}
 
 				}
 
 			}
 
 		}
 	}
 
 }
