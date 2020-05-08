 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui.jbands;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import de.cismet.tools.gui.jbands.interfaces.Band;
 import de.cismet.tools.gui.jbands.interfaces.BandAbsoluteHeightProvider;
 import de.cismet.tools.gui.jbands.interfaces.BandMember;
 import de.cismet.tools.gui.jbands.interfaces.BandMemberActionProvider;
 import de.cismet.tools.gui.jbands.interfaces.BandMemberMouseListeningComponent;
 import de.cismet.tools.gui.jbands.interfaces.BandMemberSelectable;
 import de.cismet.tools.gui.jbands.interfaces.BandModel;
 import de.cismet.tools.gui.jbands.interfaces.BandModelListener;
 import de.cismet.tools.gui.jbands.interfaces.BandModificationProvider;
 import de.cismet.tools.gui.jbands.interfaces.BandPrefixProvider;
 import de.cismet.tools.gui.jbands.interfaces.BandSnappingPointProvider;
 import de.cismet.tools.gui.jbands.interfaces.BandWeightProvider;
 import de.cismet.tools.gui.jbands.interfaces.Section;
 import de.cismet.tools.gui.jbands.interfaces.Spot;
 import de.cismet.tools.gui.jbands.interfaces.StationaryBandMemberMouseListeningComponent;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class JBand extends JPanel implements ActionListener, MouseListener, MouseMotionListener, BandModelListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final Dimension MINDIM = new Dimension(0, 0);
 
     //~ Instance fields --------------------------------------------------------
 
     HashMap<JComponent, BandMember> bandMembersViaComponents = new HashMap<JComponent, BandMember>();
     HashMap<BandMember, JComponent> componentsViaBandMembers = new HashMap<BandMember, JComponent>();
     JBandsPanel bandsPanel = new JBandsPanel();
     ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
     BandMember selectedBandMember = null;
     private int maxPreferredPrefixWidth = 0;
     private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private BandModel model;
     private double zoomFactor = 1.0;
     private JScrollPane scrollPane = new JScrollPane(bandsPanel);
     private float[] heightWeights;
     private double minValue = Double.MAX_VALUE;
     private double maxValue = Double.MIN_VALUE;
     private float heightsWeightSum = 0f;
     private double realWidth = 0;
     private int xoffset = 0;
     private List<JBandYDimension> bandPosY = new ArrayList<JBandYDimension>();
     private Map<Band, ArrayList<ArrayList<BandMember>>> subBandMap =
         new HashMap<Band, ArrayList<ArrayList<BandMember>>>();
     private List<SnappingPoint> snappingPoints = new ArrayList<SnappingPoint>();
     private boolean readOnly = false;
     private boolean refreshAvoided = false;
     private Component lastPressedComponent = null;
 
     private boolean dragged = false;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new JBand object.
      */
     public JBand() {
         this(false);
     }
 
     /**
      * Creates a new JBand object.
      *
      * @param  readOnly  DOCUMENT ME!
      */
     public JBand(final boolean readOnly) {
         this.readOnly = readOnly;
         setLayout(new BorderLayout());
         add(scrollPane, BorderLayout.CENTER);
         setOpaque(false);
         bandsPanel.setOpaque(false);
         bandsPanel.addMouseMotionListener(this);
         bandsPanel.addMouseListener(this);
         scrollPane.setOpaque(false);
         scrollPane.getViewport().setOpaque(false);
         scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
     }
 
     /**
      * Creates a new JBand object.
      *
      * @param  model  DOCUMENT ME!
      */
     public JBand(final BandModel model) {
         this();
         setModel(model);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public BandMember getSelectedBandMember() {
         return selectedBandMember;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  horizontalScrollBarPolicy  DOCUMENT ME!
      */
     public void setHorizontalScrollBarPolicy(final int horizontalScrollBarPolicy) {
         scrollPane.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  model  DOCUMENT ME!
      */
     public void setModel(final BandModel model) {
         if (this.model != null) {
             this.model.removeBandModelListener(this);
         }
         this.model = model;
         model.addBandModelListener(this);
         init();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void init() {
         minValue = Double.MAX_VALUE;
         maxValue = Double.MIN_VALUE;
         bandsPanel.removeAll();
         for (int row = 0; row < model.getNumberOfBands(); ++row) {
             final int cols = model.getBand(row).getNumberOfMembers();
             final Band rowBand = model.getBand(row);
             JComponent prefix = null;
             if (rowBand instanceof BandPrefixProvider) {
                 prefix = ((BandPrefixProvider)rowBand).getPrefixComponent();
                 bandsPanel.add(prefix);
                 maxPreferredPrefixWidth = (maxPreferredPrefixWidth < prefix.getPreferredSize().width)
                     ? prefix.getPreferredSize().width : maxPreferredPrefixWidth;
             }
 
             for (int col = 0; col < cols; ++col) {
                 final BandMember member = rowBand.getMember(col);
                 final JComponent comp = member.getBandMemberComponent();
                 if (member instanceof BandMemberActionProvider) {
                     ((BandMemberActionProvider)member).addActionListener(this);
                 }
                 bandsPanel.add(comp);
                 bandMembersViaComponents.put(comp, member);
                 componentsViaBandMembers.put(member, comp);
 
                 if (!Arrays.asList(comp.getMouseListeners()).contains(this)) {
                     comp.addMouseListener(this);
                 }
 
                 if (!Arrays.asList(comp.getMouseMotionListeners()).contains(this)) {
                     comp.addMouseMotionListener(this);
                 }
             }
         }
 
         heightWeights = new float[model.getNumberOfBands()];
 
         for (int zeile = 0; zeile < model.getNumberOfBands(); ++zeile) {
             final Band b = model.getBand(zeile);
             if (b instanceof BandWeightProvider) {
                 heightWeights[zeile] = ((BandWeightProvider)b).getBandWeight();
             } else if (b instanceof BandAbsoluteHeightProvider) {
                 heightWeights[zeile] = 0f;
             } else {
                 heightWeights[zeile] = 1f;
             }
             if (b.getMax() > getMaxValue()) {
                 setMaxValue(b.getMax());
             }
             if (b.getMin() < getMinValue()) {
                 setMinValue(b.getMin());
             }
         }
 
         realWidth = getMaxValue() - getMinValue();
 
         layoutBandMemberComponents();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void layoutBandMemberComponents() {
         heightsWeightSum = 0;
         bandPosY.clear();
         snappingPoints.clear();
 
         if ((model == null) || (model.getNumberOfBands() == 0) || (bandsPanel.getWidth() <= 0)) {
             return;
         }
 
         int posy = 0;
 
         int remainingBandsPanelHeight = bandsPanel.getHeight();
 
         for (int zeile = 0; zeile < model.getNumberOfBands(); ++zeile) {
             final Band rowBand = model.getBand(zeile);
             if (rowBand.isEnabled()) {
                 heightsWeightSum += heightWeights[zeile];
                 if (rowBand instanceof BandAbsoluteHeightProvider) {
                     remainingBandsPanelHeight -= ((BandAbsoluteHeightProvider)rowBand).getAbsoluteHeight();
                 }
 
                 // save snapping points
                 if (rowBand instanceof BandSnappingPointProvider) {
                     for (int i = 0; i < rowBand.getNumberOfMembers(); ++i) {
                         final BandMember m = rowBand.getMember(i);
                         snappingPoints.add(new SnappingPoint(m.getMin(), m.getBandMemberComponent()));
                         snappingPoints.add(new SnappingPoint(m.getMax(), m.getBandMemberComponent()));
                     }
                 }
 
                 if (remainingBandsPanelHeight < 0) {
                     // TODO
                 }
             }
         }
 
         // prefixes
         for (int zeile = 0; zeile < model.getNumberOfBands(); ++zeile) {
             final Band rowBand = model.getBand(zeile);
 
             int memberHeight;
             if (!rowBand.isEnabled()) {
                 memberHeight = 0;
             } else if ((heightWeights[zeile] == 0) && (rowBand instanceof BandAbsoluteHeightProvider)) {
                 memberHeight = ((BandAbsoluteHeightProvider)rowBand).getAbsoluteHeight();
             } else {
                 memberHeight = (int)(((double)remainingBandsPanelHeight) * heightWeights[zeile] / heightsWeightSum);
             }
             if (rowBand instanceof BandPrefixProvider) {
                 final JComponent prefix = ((BandPrefixProvider)rowBand).getPrefixComponent();
                 prefix.setBounds(0, posy, maxPreferredPrefixWidth, memberHeight);
 
                 xoffset = (xoffset > prefix.getPreferredSize().width) ? xoffset : prefix.getPreferredSize().width;
             }
             if (memberHeight > 0) {
                 bandPosY.add(new JBandYDimension(rowBand, posy, posy + memberHeight - 1));
             }
             posy += memberHeight;
         }
 
         // BandMember
         posy = 0;
         for (int zeile = 0; zeile < model.getNumberOfBands(); ++zeile) {
             final Band rowBand = model.getBand(zeile);
             int memberHeight;
             if (!rowBand.isEnabled()) {
                 memberHeight = 0;
                 for (int i = 0; i < rowBand.getNumberOfMembers(); ++i) {
                     rowBand.getMember(i).getBandMemberComponent().setBounds(new Rectangle(0, 0, 0, 0));
                 }
                 continue;
             } else if ((heightWeights[zeile] == 0) && (rowBand instanceof BandAbsoluteHeightProvider)) {
                 memberHeight = ((BandAbsoluteHeightProvider)rowBand).getAbsoluteHeight();
             } else {
                 memberHeight = (int)(((double)remainingBandsPanelHeight) * heightWeights[zeile] / heightsWeightSum);
             }
             final ArrayList<ArrayList<BandMember>> subBands = new ArrayList<ArrayList<BandMember>>();
             final ArrayList<BandMember> masterBand = new ArrayList<BandMember>();
             for (int i = 0; i < rowBand.getNumberOfMembers(); ++i) {
                 masterBand.add(rowBand.getMember(i));
             }
 
             for (int i = 0; i < rowBand.getNumberOfMembers(); ++i) {
                 for (int j = i; j < rowBand.getNumberOfMembers(); ++j) {
                     final BandMember bi = rowBand.getMember(i);
                     final BandMember bj = rowBand.getMember(j);
                     if ((i != j) && masterBand.contains(bi) && masterBand.contains(bj)) {
                         if (isColliding(bi, bj)) {
                             masterBand.remove(bj);
                             boolean added = false;
                             for (final ArrayList<BandMember> subBand : subBands) {
                                 if (!hasCollisions(subBand, bj)) {
                                     subBand.add(bj);
                                     added = true;
                                     break;
                                 }
                             }
                             if (!added) {
                                 final ArrayList<BandMember> newSubBand = new ArrayList<BandMember>();
                                 newSubBand.add(bj);
                                 subBands.add(newSubBand);
                             }
                         }
                     }
                 }
             }
             if (subBands.size() > 0) {
                 memberHeight = (int)(memberHeight / (1 + subBands.size()));
             }
             for (final BandMember bm : masterBand) {
                 bm.getBandMemberComponent().setBounds(getBoundsOfComponent(bm, memberHeight, posy));
             }
             posy += memberHeight;
             for (final ArrayList<BandMember> subBand : subBands) {
                 for (final BandMember bm : subBand) {
                     bm.getBandMemberComponent().setBounds(getBoundsOfComponent(bm, memberHeight, posy));
                 }
                 posy += memberHeight;
             }
             subBands.add(masterBand);
             subBandMap.put(rowBand, subBands);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   members    DOCUMENT ME!
      * @param   candidate  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean hasCollisions(final ArrayList<BandMember> members, final BandMember candidate) {
         for (final BandMember bm : members) {
             if (isColliding(bm, candidate)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   a  DOCUMENT ME!
      * @param   b  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IllegalArgumentException  DOCUMENT ME!
      */
     private boolean isColliding(final BandMember a, final BandMember b) {
         if ((a instanceof Section) && (b instanceof Section)) {
             return (!((a.getMax() <= b.getMin()) || (a.getMin() >= b.getMax())));
         } else if ((a instanceof Spot) || (b instanceof Spot)) {
             // dummy werte
             final int memberHeight = 10;
             final int posy = 10;
             final Rectangle ri = getBoundsOfComponent(a, memberHeight, posy);
             final Rectangle rj = getBoundsOfComponent(b, memberHeight, posy);
             final int rimax = ri.x + ri.width;
             final int rimin = ri.x;
             final int rjmax = rj.x + rj.width;
             final int rjmin = rj.x;
             return (!((rimax <= rjmin) || (rimin >= rjmax)));
         }
         throw new IllegalArgumentException("Possible Combinations are Section,Section and Spot,Spot");
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   member        DOCUMENT ME!
      * @param   memberHeight  DOCUMENT ME!
      * @param   posy          DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Rectangle getBoundsOfComponent(final BandMember member,
             final int memberHeight,
             final int posy) {
         final JComponent comp = member.getBandMemberComponent();
         final double widthFactor = ((double)bandsPanel.getWidth() - xoffset) / realWidth;
         if (member instanceof Section) {
             final int posx = (int)(((member.getMin() - minValue) * widthFactor) + 0.5d) + xoffset;
            final int lastPosX = (int)(((member.getMax() - minValue) * widthFactor) + 0.5d) + xoffset;
            final int memberWidth = Math.max(lastPosX - posx, 1);
             return new Rectangle(posx, posy, memberWidth, memberHeight);
         } else if (member instanceof Spot) {
             final int memberWidth = comp.getPreferredSize().width;
             final int posx = (int)(((member.getMin() - minValue) * widthFactor) + 0.5d - (memberWidth / 2d)) + xoffset;
             return new Rectangle(posx, posy, memberWidth, memberHeight);
         }
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public double getZoomFactor() {
         return zoomFactor;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  zoomFactor  DOCUMENT ME!
      */
     public void setZoomFactor(final double zoomFactor) {
         this.zoomFactor = zoomFactor;
         scrollPane.getViewport().revalidate();
     }
 
     /**
      * DOCUMENT ME!
      */
     public void fireActionPerformed() {
         final ActionEvent e = new ActionEvent(this, 1, "selectionChanged");
         log.info("BandMemberAction Performed");
         for (final ActionListener al : actionListeners) {
             al.actionPerformed(e);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  listener  DOCUMENT ME!
      */
     public void addActionListener(final ActionListener listener) {
         actionListeners.add(listener);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  listener  DOCUMENT ME!
      */
     public void removeActionListener(final ActionListener listener) {
         actionListeners.remove(listener);
     }
 
     @Override
     public void mouseClicked(final MouseEvent e) {
         if (!readOnly && (e.getClickCount() == 2)) {
             if (e.getComponent() == bandsPanel) {
                 if (e.getX() >= maxPreferredPrefixWidth) {
                     final int y = e.getY();
                     int startY = 0;
                     int bandHeight = 0;
                     Band targetBand = null;
 
                     for (final JBandYDimension tmp : bandPosY) {
                         if ((y >= tmp.getyMin()) && (y <= tmp.getyMax())) {
                             targetBand = tmp.getBand();
                             startY = tmp.getyMin();
                             bandHeight = tmp.getyMax() - tmp.getyMin();
                             break;
                         }
                     }
 
                     if (targetBand != null) {
                         if (targetBand instanceof BandModificationProvider) {
                             final double station = getSationForXValue(e.getX());
                             final ArrayList<ArrayList<BandMember>> subBands = subBandMap.get(targetBand);
                             int n = (y - startY) / (bandHeight / subBands.size());
 
                             --n;
                             // the first band is a the end of the list
                             if (n == -1) {
                                 n = subBands.size() - 1;
                             }
                             ((BandModificationProvider)targetBand).addMember(
                                 station,
                                 null,
                                 getMinValue(),
                                 getMaxValue(),
                                 subBands.get(n));
                         }
                     }
                 }
             }
         } else {
             if (e.getComponent() instanceof BandMemberSelectable) {
                 final BandMemberSelectable selecteable = (BandMemberSelectable)e.getComponent();
                 final BandMember oldBelectedBandMember = selectedBandMember;
                 JBandCursorManager.getInstance().setCursor(this);
 
                 if (e.getClickCount() == 1) {
                     if (!e.isPopupTrigger()) {
                         if (selecteable.isSelectable() && !selecteable.isSelected()
                                     && !(selecteable == selectedBandMember)) {
                             selecteable.setSelected(true);
                             if (selectedBandMember != null) {
                                 ((BandMemberSelectable)selectedBandMember).setSelected(false);
                             }
                             selectedBandMember = selecteable.getBandMember();
                         } else {
                             selecteable.setSelected(false);
                             selectedBandMember = null;
                         }
                         if (model instanceof SimpleBandModel) {
                             final SimpleBandModel sbm = ((SimpleBandModel)model);
                             sbm.fireBandModelSelectionChanged();
                         }
                     } else {
                         // Popup
                     }
                 }
             }
         }
        if ((e.getComponent() instanceof BandMemberMouseListeningComponent)) {
             ((BandMemberMouseListeningComponent)e.getComponent()).mouseClicked(e);
         }
     }
 
     @Override
     public void bandModelSelectionChanged(final BandModelEvent e) {
     }
 
     @Override
     public void mouseEntered(final MouseEvent e) {
         if (e.getComponent() instanceof BandMemberMouseListeningComponent) {
             ((BandMemberMouseListeningComponent)e.getComponent()).mouseEntered(e);
         }
     }
 
     @Override
     public void mouseExited(final MouseEvent e) {
         if (e.getComponent() instanceof BandMemberMouseListeningComponent) {
             ((BandMemberMouseListeningComponent)e.getComponent()).mouseExited(e);
         }
     }
 
     @Override
     public void mousePressed(final MouseEvent e) {
         if (!readOnly && (e.getComponent() instanceof BandMemberMouseListeningComponent)) {
             lastPressedComponent = e.getComponent();
             ((BandMemberMouseListeningComponent)e.getComponent()).mousePressed(e);
         }
     }
 
     @Override
     public void mouseReleased(final MouseEvent e) {
         dragged = false;
         if (!readOnly && (e.getComponent() instanceof BandMemberMouseListeningComponent)) {
             ((BandMemberMouseListeningComponent)e.getComponent()).mouseReleased(e);
         }
     }
 
     @Override
     public void mouseDragged(final MouseEvent e) {
         if (JBandCursorManager.getInstance().isLocked()) {
             JBandCursorManager.getInstance().setCursor(this);
         }
         dragged = true;
         if (!readOnly && (e.getComponent() instanceof BandMemberMouseListeningComponent)) {
             ((BandMemberMouseListeningComponent)e.getComponent()).mouseDragged(e);
         }
 
         if (!readOnly && (e.getComponent() instanceof StationaryBandMemberMouseListeningComponent)) {
             int x = 0;
             if (e.getSource() == bandsPanel) {
                 x = e.getX();
             } else {
                 x = (int)((Component)e.getSource()).getBounds().getX() + e.getX();
             }
             double station = getSationForXValue(x);
 
             if (station < getMinValue()) {
                 station = getMinValue();
             } else if (station > getMaxValue()) {
                 station = getMaxValue();
             }
 
             station = considerSnapping(station, e.getComponent());
 
             ((StationaryBandMemberMouseListeningComponent)e.getComponent()).mouseDragged(e, station);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   station  DOCUMENT ME!
      * @param   c        DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private double considerSnapping(final double station, final Component c) {
         int dist = (int)((maxValue - minValue) / (100 * ((zoomFactor == 0) ? 1 : zoomFactor)));
         if (dist < 1) {
             dist = 1;
         }
         for (final SnappingPoint tmp : snappingPoints) {
             if ((Math.abs(tmp.value - station) < (dist)) && (tmp.c != c)) {
                 return tmp.value;
             }
         }
 
         return station;
     }
 
     @Override
     public void mouseMoved(final MouseEvent e) {
         if (e.isControlDown()) {
             final Component source = e.getComponent();
             int x = 0;
             if (source == bandsPanel) {
                 x = e.getX();
             } else {
                 x = (int)source.getBounds().getX()
                             + e.getX();
             }
             if (!bandsPanel.isMeasurementEnabled()) {
                 bandsPanel.setMeasurementEnabled(true);
             }
             bandsPanel.setMeasurementx(x);
 
 //            System.out.println("x"+first.getBounds());
         } else {
             if (bandsPanel.isMeasurementEnabled()) {
                 bandsPanel.setMeasurementEnabled(false);
             }
         }
         if (JBandCursorManager.getInstance().isLocked()) {
             JBandCursorManager.getInstance().setCursor(this);
         } else {
             JBandCursorManager.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
             JBandCursorManager.getInstance().setCursor(this);
         }
         if ((e.getComponent() instanceof BandMemberMouseListeningComponent)) {
             ((BandMemberMouseListeningComponent)e.getComponent()).mouseMoved(e);
         }
     }
 
     @Override
     public void actionPerformed(final ActionEvent e) {
         // reagieren auf ActionEvents von BandMembern
     }
     @Override
     public void bandModelChanged(final BandModelEvent e) {
         if (!refreshAvoided) {
             init();
             if ((e != null) && e.isSelectionLost()) {
                 selectedBandMember = null;
             }
             EventQueue.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         bandsPanel.repaint();
                     }
                 });
         }
     }
 
     @Override
     public void bandModelValuesChanged(final BandModelEvent e) {
         if (!refreshAvoided) {
             if ((e != null) && e.isSelectionLost()) {
                 selectedBandMember = null;
             }
             layoutBandMemberComponents();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public BandModel getModel() {
         return model;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  args  DOCUMENT ME!
      */
     public static void main(final String[] args) {
         final JFrame jf = new JFrame("Test");
         jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         jf.getContentPane().setLayout(new BorderLayout());
 
         final JBand jbdTest = new JBand();
 
         final MinimumHeightBand sb0 = new MinimumHeightBand("Titel");
 
         sb0.addMember(new SimpleTextSection("OTOL-800", 0, 300, true, false));
         sb0.addMember(new SimpleTextSection("OTOL-8700", 300, 500, false, false));
         sb0.addMember(new SimpleTextSection("OTOL-900", 500, 1010, false, true));
 
         final DefaultBand sb1 = new DefaultBand("Das erste richtige");
         sb1.addMember(new SimpleSection(0, 10));
         int from = 10;
         int to = 20;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
         from += 10;
         to += 10;
         sb1.addMember(new SimpleSection(from, to));
 
         sb1.addMember(new SimpleSection(310, 1000));
         sb1.addMember(new SimpleSection(1000, 1010));
 
         final SimpleBand sb2 = new SimpleBand();
         sb2.addMember(new SimpleSection(0, 10));
         sb2.addMember(new SimpleSection(10, 20));
         sb2.addMember(new SimpleSection(20, 80));
         sb2.addMember(new SimpleSection(80, 100));
 
         final SimpleBand sb3 = new SimpleBand();
         sb3.addMember(new SimpleSection(0, 50));
         sb3.addMember(new SimpleSection(50, 300));
         sb3.addMember(new SimpleSection(300, 1010));
         final SimpleBand sb4 = new SimpleBand("Punkte .......");
 
         sb4.addMember(new SimpleSpot(100));
 
         sb4.addMember(new SimpleSpot(200));
 
         sb4.addMember(new SimpleSpot(300));
 
         sb4.addMember(new SimpleSpot(400));
 
         sb4.addMember(new SimpleSpot(500));
 
         final SimpleBand sb4a = new SimpleBand("auch");
 
         sb4a.addMember(new SimpleSection(100, 100));
 
         sb4a.addMember(new SimpleSection(110, 110));
 
         sb4a.addMember(new SimpleSection(300, 300));
 
         sb4a.addMember(new SimpleSection(400, 400));
 
         sb4a.addMember(new SimpleSection(500, 500));
 
         final SimpleBandModel sbm = new SimpleBandModel();
         sbm.addBand(sb0);
         sbm.addBand(sb1);
         // sbm.addBand(new EmptyWeightedBand(2f));
         sbm.addBand(new EmptyAbsoluteHeightedBand(1));
         sbm.addBand(sb2);
         sbm.addBand(new EmptyAbsoluteHeightedBand(1));
 
         sbm.addBand(sb3);
         sbm.addBand(sb4);
         sbm.addBand(sb4a);
 
         final SimpleBand sb5 = new SimpleBand();
         sb5.addMember(new SimpleSection(0, 1100));
         sb5.addMember(new SimpleSection(50, 500));
         sb5.addMember(new SimpleSection(400, 600));
         sbm.addBand(sb5);
 
         jbdTest.setModel(sbm);
         jbdTest.setZoomFactor(2);
 //        jf.getContentPane().setBackground(Color.red);
         jbdTest.setBorder(new EmptyBorder(10, 10, 10, 10));
         jf.getContentPane().add(jbdTest, BorderLayout.CENTER);
 
         final JSlider jsl = new JSlider(0, 100);
 
         jsl.addChangeListener(new ChangeListener() {
 
                 @Override
                 public void stateChanged(final ChangeEvent ce) {
                     final double zfAdd = jsl.getValue() / 10.0;
                     jbdTest.setZoomFactor(1 + zfAdd);
                 }
             });
 
         final JCheckBox checker = new JCheckBox("test");
         checker.setSelected(true);
         checker.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     sb1.setEnabled(!sb1.isEnabled());
                     ((SimpleBandModel)(jbdTest.getModel())).fireBandModelValuesChanged();
                 }
             });
         jf.getContentPane().add(checker, BorderLayout.NORTH);
 
         jsl.setValue(0);
         jf.getContentPane().add(jsl, BorderLayout.SOUTH);
         jf.setSize(300, 400);
         jf.setVisible(true);
         final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
         jf.setBounds((screenSize.width - 800) / 2, (screenSize.height - 222) / 2, 800, 222);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  the refreshAvoided
      */
     public boolean isRefreshAvoided() {
         return refreshAvoided;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  refreshAvoided  the refreshAvoided to set
      */
     public void setRefreshAvoided(final boolean refreshAvoided) {
         this.refreshAvoided = refreshAvoided;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  the minValue
      */
     public double getMinValue() {
         return minValue;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  minValue  the minValue to set
      */
     public void setMinValue(final double minValue) {
         this.minValue = minValue;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  the maxValue
      */
     public double getMaxValue() {
         return maxValue;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  maxValue  the maxValue to set
      */
     public void setMaxValue(final double maxValue) {
         this.maxValue = maxValue;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   x  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private double getSationForXValue(final int x) {
         return ((double)Math.round(((realWidth * (x - xoffset) / (bandsPanel.getWidth() - xoffset)) + minValue)
                             * 10.0)) / 10.0;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class SnappingPoint implements Comparable<SnappingPoint> {
 
         //~ Instance fields ----------------------------------------------------
 
         private Double value;
         private Component c;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new SnappingPoint object.
          *
          * @param  value  DOCUMENT ME!
          * @param  c      DOCUMENT ME!
          */
         public SnappingPoint(final double value, final Component c) {
             this.value = value;
             this.c = c;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public int compareTo(final SnappingPoint other) {
             final double o = other.value;
 
             if (Math.abs(value - o) < ((maxValue - minValue) / 100)) {
                 return 0;
             } else {
                 return value.compareTo(o);
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  the c
          */
         public Component getC() {
             return c;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  c  the c to set
          */
         public void setC(final Component c) {
             this.c = c;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class JBandYDimension {
 
         //~ Instance fields ----------------------------------------------------
 
         private Band band;
         private int yMin;
         private int yMax;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new JBandYDimension object.
          *
          * @param  band  DOCUMENT ME!
          * @param  yMin  DOCUMENT ME!
          * @param  yMax  DOCUMENT ME!
          */
         public JBandYDimension(final Band band, final int yMin, final int yMax) {
             this.band = band;
             this.yMin = yMin;
             this.yMax = yMax;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  the band
          */
         public Band getBand() {
             return band;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  band  the band to set
          */
         public void setBand(final Band band) {
             this.band = band;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  the yMin
          */
         public int getyMin() {
             return yMin;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  yMin  the yMin to set
          */
         public void setyMin(final int yMin) {
             this.yMin = yMin;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  the yMax
          */
         public int getyMax() {
             return yMax;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  yMax  the yMax to set
          */
         public void setyMax(final int yMax) {
             this.yMax = yMax;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class JBandsPanel extends JPanel {
 
         //~ Instance fields ----------------------------------------------------
 
         private boolean measurementEnabled = false;
         private int measurementx = 0;
         private Color MIDDLE = new Color(50, 50, 50, 150);
         private Color SIDE = new Color(250, 250, 250, 150);
         private Color SIDER = new Color(220, 220, 220, 50);
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new JBandsPanel object.
          */
         public JBandsPanel() {
             super(null);
             setForeground(new Color(50, 50, 50, 150));
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public boolean isMeasurementEnabled() {
             return measurementEnabled;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  measurementEnabled  DOCUMENT ME!
          */
         public void setMeasurementEnabled(final boolean measurementEnabled) {
             this.measurementEnabled = measurementEnabled;
             repaint();
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public int getMeasurementx() {
             return measurementx;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  measurementx  DOCUMENT ME!
          */
         public void setMeasurementx(final int measurementx) {
             if (measurementx < xoffset) {
                 this.measurementx = xoffset;
             } else {
                 this.measurementx = measurementx;
             }
             if (measurementEnabled) {
                 repaint();
             }
         }
 
         @Override
         public Dimension getPreferredSize() {
             final Dimension d = super.getPreferredSize();
             return new Dimension((int)(scrollPane.getWidth() * 0.9 * zoomFactor), d.height);
         }
 
         @Override
         public void setSize(final int width, final int height) {
             super.setSize(width, height);
             layoutBandMemberComponents();
         }
 
         @Override
         public void setSize(final Dimension d) {
             setSize(d.width, d.height);
         }
 
 //        @Override
 //        protected void paintComponent(Graphics g) {
 //
 //            super.paintComponent(g);
 //
 //
 //        }
         @Override
         protected void paintChildren(final Graphics g) {
             super.paintChildren(g);
             if (measurementEnabled) {
                 final Graphics2D g2d = (Graphics2D)g;
                 final double station = getSationForXValue(measurementx);
 
                 g.setColor(SIDER);
                 g.drawLine(measurementx - 2, 0, measurementx - 2, getHeight());
                 g.drawLine(measurementx + 2, 0, measurementx + 2, getHeight());
                 g.setColor(SIDE);
                 g.drawLine(measurementx - 1, 0, measurementx - 1, getHeight());
                 g.drawLine(measurementx + 1, 0, measurementx + 1, getHeight());
                 g.setColor(MIDDLE);
                 g.drawLine(measurementx, 0, measurementx, getHeight());
 
                 final String s = String.valueOf(station);
                 final int sWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), s);
                 final int sPos = measurementx
                             + 5;
                 if ((sPos + sWidth) <= (getWidth() - 10)) {
                     g.drawString(s, measurementx + 5, getHeight() - 3);
                 } else {
                     g.drawString(s, measurementx - sWidth - 5, getHeight() - 3);
                 }
             }
         }
     }
 }
