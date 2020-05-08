 /*
  * FlaechenUebersichtsTabellenPanel.java
  *
  * Created on 5. Januar 2005, 14:02
  */
 package de.cismet.verdis.gui;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.Polygon;
 import de.cismet.cismap.commons.ServiceLayer;
 import de.cismet.cismap.commons.features.DefaultFeatureCollection;
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.features.FeatureCollectionEvent;
 import de.cismet.cismap.commons.features.FeatureCollectionListener;
 import de.cismet.cismap.commons.features.PureNewFeature;
 import de.cismet.cismap.commons.featureservice.DefaultFeatureServiceFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.AttachFeatureListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.DeleteFeatureListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.FeatureMoveListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.JoinPolygonsListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.SplitPolygonListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.CustomAction;
 import de.cismet.cismap.commons.interaction.memento.MementoInterface;
 import de.cismet.cismap.commons.preferences.CismapPreferences;
 import de.cismet.cismap.commons.retrieval.RetrievalEvent;
 import de.cismet.cismap.commons.retrieval.RetrievalListener;
 import de.cismet.cismap.commons.retrieval.RetrievalService;
 import de.cismet.gui.tools.TableSorter;
 import de.cismet.tools.StaticDecimalTools;
 import de.cismet.tools.gui.StaticSwingTools;
 
 import de.cismet.tools.CurrentStackTrace;
 import de.cismet.tools.NumberStringComparator;
 import de.cismet.tools.gui.historybutton.JHistoryButton;
 import de.cismet.verdis.data.Flaeche;
 import de.cismet.verdis.interfaces.FlaechenAuswahlChangedListener;
 import de.cismet.verdis.interfaces.KassenzeichenChangedListener;
 import edu.umd.cs.piccolo.PNode;
 import edu.umd.cs.piccolox.event.PNotification;
 import edu.umd.cs.piccolox.event.PNotificationCenter;
 import edu.umd.cs.piccolox.event.PSelectionEventHandler;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.util.Observable;
 import java.util.Vector;
 import java.beans.PropertyChangeListener;
 import java.sql.Connection;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Observer;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import org.jdesktop.swingx.JXTable;
 import org.jdesktop.swingx.decorator.ColorHighlighter;
 import org.jdesktop.swingx.decorator.ComponentAdapter;
 import org.jdesktop.swingx.decorator.HighlightPredicate;
 import org.jdesktop.swingx.decorator.Highlighter;
 import org.jdesktop.swingx.decorator.SortOrder;
 
 /**
  * @author  hell
  */
 public class FlaechenUebersichtsTabellenPanel extends javax.swing.JPanel implements KassenzeichenChangedListener, ListSelectionListener, PropertyChangeListener, FeatureCollectionListener, RetrievalListener, Observer {
 
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private Connection connection;
     private FlaechenUebersichtsTableModel tableModel;
     private Vector auswahlChangedListeners = new Vector();
     private CismapPreferences cismapPrefs;
 
     /** Creates new form FlaechenUebersichtsTabellenPanel */
     public FlaechenUebersichtsTabellenPanel() {
         initComponents();
         //featureCollectionAndListModel=new FeatureCollectionAndListModel();
 //        mappingComp.setFeatureCollection(featureCollectionAndListModel);
 //        tblOverview.setSelectionModel(featureCollectionAndListModel);
 
         mappingComp.addPropertyChangeListener(this);
         mappingComp.getFeatureCollection().addFeatureCollectionListener(this);
         //mappingComp.putInputListener(MappingComponent.ATTACH_POLYGON_TO_ALPHADATA, new AttachFeatureListener());
 
         mappingComp.setBackgroundEnabled(true);
 
         cmdSelect.setSelected(true);
 
 
 //        CreateGeometryListener g=new CreateGeometryListener(mappingComp,JLabel.class) {
 //           
 //            
 //        };
         //mappingComp.addInputListener("TIM_EASY_CREATOR",)
 
 
 
         //TIM Easy
         cmdNewPoint.setVisible(true);
 
 
 
         ((JHistoryButton) cmdForward).setDirection(JHistoryButton.DIRECTION_FORWARD);
         ((JHistoryButton) cmdBack).setDirection(JHistoryButton.DIRECTION_BACKWARD);
         ((JHistoryButton) cmdForward).setHistoryModel(mappingComp);
         ((JHistoryButton) cmdBack).setHistoryModel(mappingComp);
 
         cmdWmsBackground.setSelected(mappingComp.isBackgroundEnabled());
 
         PNotificationCenter.defaultCenter().addListener(this,
                 "coordinatesChanged",
                 SimpleMoveListener.COORDINATES_CHANGED,
                 mappingComp.getInputListener(MappingComponent.MOTION));
 
         PNotificationCenter.defaultCenter().addListener(this,
                 "selectionChanged",
                 PSelectionEventHandler.SELECTION_CHANGED_NOTIFICATION,
                 mappingComp.getInputListener(MappingComponent.SELECT));
         PNotificationCenter.defaultCenter().addListener(this,
                 "selectionChanged",
                 FeatureMoveListener.SELECTION_CHANGED_NOTIFICATION,
                 mappingComp.getInputListener(MappingComponent.MOVE_POLYGON));
         PNotificationCenter.defaultCenter().addListener(this,
                 "selectionChanged",
                 SplitPolygonListener.SELECTION_CHANGED,
                 mappingComp.getInputListener(MappingComponent.SPLIT_POLYGON));
         PNotificationCenter.defaultCenter().addListener(this,
                 "featureDeleteRequested",
                 DeleteFeatureListener.FEATURE_DELETE_REQUEST_NOTIFICATION,
                 mappingComp.getInputListener(MappingComponent.REMOVE_POLYGON));
         PNotificationCenter.defaultCenter().addListener(this,
                 "attachFeatureRequested",
                 AttachFeatureListener.ATTACH_FEATURE_NOTIFICATION,
                 mappingComp.getInputListener(MappingComponent.ATTACH_POLYGON_TO_ALPHADATA));
         PNotificationCenter.defaultCenter().addListener(this,
                 "splitPolygon",
                 SplitPolygonListener.SPLIT_FINISHED,
                 mappingComp.getInputListener(MappingComponent.SPLIT_POLYGON));
         PNotificationCenter.defaultCenter().addListener(this,
                 "joinPolygons",
                 JoinPolygonsListener.FEATURE_JOIN_REQUEST_NOTIFICATION,
                 mappingComp.getInputListener(MappingComponent.JOIN_POLYGONS));
 
         tableModel = new FlaechenUebersichtsTableModel(connection, jxtOverview, mappingComp);
 
         jxtOverview.setModel(tableModel);
 //        jxtOverview.setDefaultRenderer(Object.class,tableModel);
 
         HighlightPredicate errorPredicate = new HighlightPredicate() {
 
             public boolean isHighlighted(Component renderer, ComponentAdapter componentAdapter) {
                 int displayedIndex = componentAdapter.row;
                 int modelIndex = jxtOverview.getFilters().convertRowIndexToModel(displayedIndex);
                 Flaeche f = getTableModel().getFlaechebyIndex(modelIndex);
                 return f != null && !f.isValid();
             }
         };
         Highlighter errorHighlighter = new ColorHighlighter(errorPredicate, Color.RED, Color.WHITE);
 
 
 //        Highlighter errorHighlighter = new ConditionalHighlighter(Color.RED, Color.WHITE, 0, -1) {
 ////            public java.awt.Component highlight(java.awt.Component renderer, ComponentAdapter adapter) {
 ////                Font f=new Font(renderer.getFont().getFontName(),Font.BOLD,renderer.getFont().getSize());
 ////                renderer.setFont(f);
 ////                return super.highlight(renderer,adapter);
 ////            }
 //            protected boolean test(ComponentAdapter componentAdapter) {
 //                int displayedIndex = componentAdapter.row;
 //                int modelIndex = jxtOverview.getFilters().convertRowIndexToModel(displayedIndex);
 //                Flaeche f = getTableModel().getFlaechebyIndex(modelIndex);
 //                return f != null && !f.isValid();
 //            }
 //        };
 
 
         HighlightPredicate changedPredicate = new HighlightPredicate() {
 
             public boolean isHighlighted(Component renderer, ComponentAdapter componentAdapter) {
                 int displayedIndex = componentAdapter.row;
                 int modelIndex = jxtOverview.getFilters().convertRowIndexToModel(displayedIndex);
                 Flaeche f = getTableModel().getFlaechebyIndex(modelIndex);
                 return f != null && !f.equals(f.getBackup());
             }
         };
 
         Highlighter changedHighlighter = new ColorHighlighter(errorPredicate, null, Color.RED);
 
 //        Highlighter changedHighlighter = new ConditionalHighlighter(null, Color.RED, 0, -1) {
 //
 //            protected boolean test(ComponentAdapter componentAdapter) {
 //                int displayedIndex = componentAdapter.row;
 //                int modelIndex = jxtOverview.getFilters().convertRowIndexToModel(displayedIndex);
 //                Flaeche f = getTableModel().getFlaechebyIndex(modelIndex);
 //                return f != null && !f.equals(f.getBackup());
 //            }
 //        };
 
 
         HighlightPredicate noGeometryPredicate = new HighlightPredicate() {
 
             public boolean isHighlighted(Component renderer, ComponentAdapter componentAdapter) {
                 int displayedIndex = componentAdapter.row;
                 int modelIndex = jxtOverview.getFilters().convertRowIndexToModel(displayedIndex);
                 Flaeche f = getTableModel().getFlaechebyIndex(modelIndex);
                 return f != null && f.getGeometry() == null;
             }
         };
 
         Highlighter noGeometryHighlighter = new ColorHighlighter(noGeometryPredicate, Color.lightGray, null);
 
 //        Highlighter noGeometryHighlighter = new ConditionalHighlighter(Color.lightGray, null, 0, -1) {
 //
 //            protected boolean test(ComponentAdapter componentAdapter) {
 //                int displayedIndex = componentAdapter.row;
 //                int modelIndex = jxtOverview.getFilters().convertRowIndexToModel(displayedIndex);
 //                Flaeche f = getTableModel().getFlaechebyIndex(modelIndex);
 //                return f != null && f.getGeometry() == null;
 //            }
 //        };
         jxtOverview.setHighlighters(changedHighlighter, noGeometryHighlighter, errorHighlighter);
 
         jxtOverview.getColumnModel().getColumn(0).setCellRenderer(jxtOverview.getDefaultRenderer(Icon.class));
         jxtOverview.getColumnModel().getColumn(2).setCellRenderer(jxtOverview.getDefaultRenderer(Icon.class));
         jxtOverview.getColumnModel().getColumn(3).setCellRenderer(jxtOverview.getDefaultRenderer(Number.class));
         jxtOverview.getSelectionModel().addListSelectionListener(this);
 
         jxtOverview.getColumnExt(1).setComparator(new NumberStringComparator());
 
         jxtOverview.getColumnModel().getColumn(0).setPreferredWidth(24);
         jxtOverview.getColumnModel().getColumn(1).setPreferredWidth(80);
         jxtOverview.getColumnModel().getColumn(2).setPreferredWidth(24);
         jxtOverview.getColumnModel().getColumn(3).setPreferredWidth(70);
         jxtOverview.getColumnModel().getColumn(4).setPreferredWidth(70);
         jxtOverview.getColumnModel().getColumn(5).setPreferredWidth(80);
 
         jxtOverview.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         jxtOverview.setDragEnabled(false);
 
         jxtOverview.getTableHeader().setResizingAllowed(false);
         jxtOverview.getTableHeader().setReorderingAllowed(false);
         jxtOverview.setSortOrder(1, SortOrder.ASCENDING);
 
         mappingComp.setBackground(getBackground());
 
         mappingComp.setBackgroundEnabled(true);
         initWaitingIcon();
         setWaiting(false);
 
         mappingComp.getCamera().addPropertyChangeListener(mappingComp.getCamera().PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener() {
 
             public void propertyChange(PropertyChangeEvent evt) {
                 int sd = (int) (mappingComp.getScaleDenominator() + 0.5);
                 lblScale.setText("1:" + sd);
             }
         });
 
         addScalePopupMenu("1:500", 500);
         addScalePopupMenu("1:7500", 750);
         addScalePopupMenu("1:1000", 1000);
         addScalePopupMenu("1:1500", 1500);
         addScalePopupMenu("1:2000", 2000);
         addScalePopupMenu("1:2500", 2500);
         addScalePopupMenu("1:5000", 5000);
         addScalePopupMenu("1:7500", 7500);
         addScalePopupMenu("1:10000", 10000);
 
         log.debug("Fl\u00E4chen\u00DCbersichtsTabellenPanel als Observer anmelden");
         ((Observable) mappingComp.getMemUndo()).addObserver(FlaechenUebersichtsTabellenPanel.this);
         ((Observable) mappingComp.getMemRedo()).addObserver(FlaechenUebersichtsTabellenPanel.this);
 
 
         if (mappingComp.getFeatureCollection() instanceof DefaultFeatureCollection) {
             ((DefaultFeatureCollection) mappingComp.getFeatureCollection()).setSingleSelection(true);
         }
     //tbpMain.remove(0); //PPT ;-)
     }
 
     public MappingComponent getMappingComponent() {
         return mappingComp;
     }
 
     public void coordinatesChanged(PNotification notification) {
         Object o = notification.getObject();
         if (o instanceof SimpleMoveListener) {
             double x = ((SimpleMoveListener) o).getXCoord();
             double y = ((SimpleMoveListener) o).getYCoord();
             double scale = ((SimpleMoveListener) o).getCurrentOGCScale();
 
             //double test= mappingComp.getWtst().getSourceX(36)-this.mappingComp.getWtst().getSourceX(0))/mappingComp.getCamera().getViewScale();
             //scale +" ... "+
             lblCoord.setText(MappingComponent.getCoordinateString(x, y));//+ "... " +test);
             PFeature pf = ((SimpleMoveListener) o).getUnderlyingPFeature();
 
             if (pf != null && pf.getFeature() instanceof DefaultFeatureServiceFeature && pf.getVisible() == true && pf.getParent() != null && pf.getParent().getVisible() == true) {
                 lblInfo.setText(((DefaultFeatureServiceFeature) pf.getFeature()).getObjectName());
             } else if (pf != null && pf.getFeature() instanceof Flaeche) {
                 String name = "Kassenzeichen: " + ((Flaeche) pf.getFeature()).getKassenzeichen() + "::" + ((Flaeche) pf.getFeature()).getBezeichnung();
                 lblInfo.setText(name);
             } else {
                 lblInfo.setText("");
             }
         }
     }
 
     //Karte
     //Wird vom Notifier per Reflection aufgerufen
     //abgelost von featurecollectionlistenermethoiden ganz unten in dieser klasse
     public void selectionChanged(PNotification notfication) {
         Object o = notfication.getObject();
         if (o instanceof SelectionListener || o instanceof FeatureMoveListener || o instanceof SplitPolygonListener) {
             PNode p = null;
             PFeature pf = null;
             if (o instanceof SelectionListener) {
                 pf = ((SelectionListener) o).getSelectedPFeature();
 //
 //                if (pf!=null && pf.getFeature() instanceof Flaeche|| pf.getFeature() instanceof PureNewFeature) {
 //                    if (((DefaultFeatureCollection)mappingComp.getFeatureCollection()).isSelected(pf.getFeature())) {
 //                        if (((DefaultFeatureCollection)mappingComp.getFeatureCollection()).getSelectedFeatures().size()>1) {
 //                            int index=sorter.getSortedPosition(getTableModel().getIndexOfFlaeche((Flaeche)pf.getFeature()));
 //                            tblOverview.getSelectionModel().addSelectionInterval(index,index);
 //                        } else {
 //                            int index=sorter.getSortedPosition(getTableModel().getIndexOfFlaeche((Flaeche)pf.getFeature()));
 //                            tblOverview.getSelectionModel().setSelectionInterval(index,index);
 //                        }
 //                    }
 //                    else {
 //                        int index=sorter.getSortedPosition(getTableModel().getIndexOfFlaeche((Flaeche)pf.getFeature()));
 //                        tblOverview.getSelectionModel().removeSelectionInterval(index,index);
 //                    }
 //                } else
                 if (((SelectionListener) o).getClickCount() > 1 && pf.getFeature() instanceof DefaultFeatureServiceFeature) {
                     log.debug("SelectionchangedListener: clickCOunt:" + ((SelectionListener) o).getClickCount());
                     DefaultFeatureServiceFeature dfsf = ((DefaultFeatureServiceFeature) pf.getFeature());
                     try {
                         if (pf.getVisible() == true && pf.getParent().getVisible() == true && dfsf.getFeatureType().equalsIgnoreCase("KASSENZEICHEN")) {
                             Main.THIS.getKzPanel().gotoKassenzeichen(dfsf.getGroupingKey());//TODO
                         }
                     } catch (Exception e) {
                         log.info("Fehler beim gotoKassenzeichen", e);
                     }
                 }
 //            } else if (o instanceof SplitPolygonListener) {
 //                //Muss nix besonderes gemacht werden, weil schon selectPFeatureManually im Listener aufgerufen wird
             }
         }
     }
 
     public void featureDeleteRequested(PNotification notfication) {
         Object o = notfication.getObject();
         if (o instanceof DeleteFeatureListener) {
             DeleteFeatureListener dfl = (DeleteFeatureListener) o;
             PFeature pf = dfl.getFeatureRequestedForDeletion();
             if (pf.getFeature() instanceof Flaeche) {
                 Flaeche f = (Flaeche) (pf.getFeature());
                 f.setRemovedGeometryId(f.getGeom_id());
                 f.setGeometryRemoved(true);
                 f.setGeom_id(-1);
                 f.setGeometry(null);
             //mappingComp.getFeatureLayer().removeChild(pf);
             //    mappingComp.getFeatureCollection().removeFeature(f); // wurde schon im Listener erledigt
             //log.debug("Bezeichnung der zu l\u00F6schenden Fl\u00E4che"+f.getBezeichnung());
             }
         }
     }
 
     public void attachFeatureRequested(PNotification notification) {
         Object o = notification.getObject();
         if (o instanceof AttachFeatureListener) {
             AttachFeatureListener afl = (AttachFeatureListener) o;
             PFeature pf = afl.getFeatureToAttach();
             if (pf.getFeature() instanceof PureNewFeature) {
                 if (tableModel.getSelectedFlaeche().getGeom_id() < 0 && tableModel.getSelectedFlaeche().getGeometry() == null && tableModel.getSelectedFlaeche().isMarkedForDeletion()) {
                     JOptionPane.showMessageDialog(mappingComp, "Dieser Fl\u00E4che kann im Moment keine Geometrie zugewiesen werden. Bitte zuerst speichern.");
                 } else if (tableModel.getSelectedFlaeche().getGeom_id() < 0 && tableModel.getSelectedFlaeche().getGeometry() == null && !tableModel.getSelectedFlaeche().isMarkedForDeletion()) {
                     Geometry g = pf.getFeature().getGeometry();
                     mappingComp.getFeatureCollection().removeFeature(pf.getFeature());
                     tableModel.getSelectedFlaeche().setGeometry(g);
                     tableModel.getSelectedFlaeche().setGeometryRemoved(false);
                     mappingComp.getFeatureCollection().addFeature(tableModel.getSelectedFlaeche());
                     tableModel.getSelectedFlaeche().setGr_grafik(new Integer((int) (tableModel.getSelectedFlaeche().getGeometry().getArea())));
                     tableModel.getSelectedFlaeche().setGr_korrektur(tableModel.getSelectedFlaeche().getGr_grafik());
                     tableModel.getSelectedFlaeche().sync();
                     //Details synchronisieren
                     fireAuswahlChanged(tableModel.getSelectedFlaeche());
 
 
                 } else {
                     //Vorhandene Fl\u00E4che wird ausgetauscht
                     //Hier kommt man niemals rein
                 }
             } else if (pf.getFeature() instanceof Flaeche) {
                 JOptionPane.showMessageDialog(mappingComp, "Es k\u00F6nnen nur nicht bereits zugeordnete Fl\u00E4chen zugeordnet werden.");
             }
         }
     }
 
     public void splitPolygon(PNotification notification) {
         Object o = notification.getObject();
         if (o instanceof SplitPolygonListener) {
             SplitPolygonListener l = (SplitPolygonListener) o;
             PFeature pf = l.getFeatureClickedOn();
             if (pf.isSplittable()) {
                 log.debug("Split");
                 if (pf.getFeature() instanceof Flaeche) {
                     ((Flaeche) pf.getFeature()).setGeometryRemoved(true);
                     ((Flaeche) pf.getFeature()).setGeometry(null);
                     ((Flaeche) pf.getFeature()).setGeom_id(-1);
                 }
                 Feature[] f_arr = pf.split();
 
                 mappingComp.getFeatureCollection().removeFeature(pf.getFeature());
                 f_arr[0].setEditable(true);
                 f_arr[1].setEditable(true);
                 mappingComp.getFeatureCollection().addFeature(f_arr[0]);
                 mappingComp.getFeatureCollection().addFeature(f_arr[1]);
                 cmdAttachPolyToAlphadataActionPerformed(null);
             }
         }
     }
 
     public void joinPolygons(PNotification notification) {
         PFeature one, two;
         one = mappingComp.getSelectedNode();
         two = null;
         log.debug("");
         Object o = notification.getObject();
 
         if (o instanceof JoinPolygonsListener) {
             JoinPolygonsListener listener = ((JoinPolygonsListener) o);
             PFeature joinCandidate = listener.getFeatureRequestedForJoin();
             if (joinCandidate.getFeature() instanceof Flaeche || joinCandidate.getFeature() instanceof PureNewFeature) {
                 int CTRL_MASK = 2; //TODO: HIer noch eine korrekte Konstante verwenden
                 if ((listener.getModifier() & CTRL_MASK) != 0) {
 
                     if (one != null && joinCandidate != one) {
                         if (one.getFeature() instanceof PureNewFeature && joinCandidate.getFeature() instanceof Flaeche) {
                             two = one;
 
                             one = joinCandidate;
                             one.setSelected(true);
                             two.setSelected(false);
                             mappingComp.getFeatureCollection().select(one.getFeature());
                             //tableModel.setSelectedFlaeche((Flaeche)one.getFeature());
                             fireAuswahlChanged((Flaeche) one.getFeature());
                         } else {
                             two = joinCandidate;
                         }
                         try {
 
                             Geometry backup = one.getFeature().getGeometry();
                             Geometry newGeom = one.getFeature().getGeometry().union(two.getFeature().getGeometry());
                             if (newGeom.getGeometryType().equalsIgnoreCase("Multipolygon")) {
                                 JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this), "Es k\u00F6nnen nur Polygone zusammengefasst werden, die aneinander angrenzen oder sich \u00FCberlappen.", "Zusammenfassung nicht m\u00F6glich", JOptionPane.WARNING_MESSAGE, null);
                                 return;
                             }
                             if (newGeom.getGeometryType().equalsIgnoreCase("Polygon") && ((Polygon) newGeom).getNumInteriorRing() > 0) {
                                 JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this), "Polygone k\u00F6nnen nur dann zusammengefasst werden, wenn dadurch kein Loch entsteht.", "Zusammenfassung nicht m\u00F6glich", JOptionPane.WARNING_MESSAGE, null);
                                 return;
                             }
                             if (one != null && two != null && one.getFeature() instanceof Flaeche && two.getFeature() instanceof Flaeche) {
                                 Flaeche fOne = (Flaeche) one.getFeature();
                                 Flaeche fTwo = (Flaeche) two.getFeature();
                                 if (fOne.getArt() != fTwo.getArt() || fOne.getGrad() != fTwo.getGrad()) {
                                     JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this), "Fl\u00E4chen k\u00F6nnen nur zusammengefasst werden, wenn Fl\u00E4chenart und Anschlussgrad gleich sind.", "Zusammenfassung nicht m\u00F6glich", JOptionPane.WARNING_MESSAGE, null);
                                     return;
                                 }
                                 //Check machen ob eine Fl\u00E4che eine Teilfl\u00E4che ist
                                 if (fOne.getAnteil() != null || fTwo.getAnteil() != null) {
                                     JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this), "Fl\u00E4chen die von Teileigentum betroffen sind k\u00F6nnen nicht zusammengefasst werden.", "Zusammenfassung nicht m\u00F6glich", JOptionPane.WARNING_MESSAGE, null);
                                     return;
                                 }
                                 tableModel.removeFlaeche(fTwo);
                                 fOne.setGr_grafik(new Integer((int) (newGeom.getArea())));
                                 if (fOne.getBemerkung() != null && fOne.getBemerkung().trim().length() > 0) {
                                     fOne.setBemerkung(fOne.getBemerkung() + "\n");
                                 }
                                 fOne.setBemerkung(fTwo.getJoinBackupString());
                                 if (!fOne.isSperre() && fTwo.isSperre()) {
                                     fOne.setSperre(true);
                                     fOne.setBem_sperre("JOIN::" + fTwo.getBem_sperre());
                                 }
                                 fOne.sync();
                                 //tableModel.fireSelectionChanged(); TODO
                                 fireAuswahlChanged(fOne);
                             }
                             if (one.getFeature() instanceof Flaeche) {
                                 //Eine vorhandene Fl\u00E4che und eine neuangelegt wurden gejoint
                                 ((Flaeche) (one.getFeature())).setGr_grafik(new Integer((int) (newGeom.getArea())));
                                 ((Flaeche) (one.getFeature())).sync();
                                 //tableModel.fireSelectionChanged(); TODO
                                 fireAuswahlChanged((Flaeche) (one.getFeature()));
                             }
 
                             log.debug("newGeom ist vom Typ:" + newGeom.getGeometryType());
                             one.getFeature().setGeometry(newGeom);
                             if (!(one.getFeature().getGeometry().equals(backup))) {
                                 two.removeFromParent();
                                 two = null;
                             }
                             one.visualize();
 
                         } catch (Exception e) {
                             log.error("one: " + one + "\n two: " + two, e);
                         }
                         return;
                     }
                 } else {
                     PFeature pf = joinCandidate;
                     if (one != null) {
                         one.setSelected(false);
                     }
                     one = pf;
                     mappingComp.selectPFeatureManually(one);
                     if (one.getFeature() instanceof Flaeche) {
                         Flaeche f = (Flaeche) one.getFeature();
                         mappingComp.getFeatureCollection().select(f);
                         //tableModel.setSelectedFlaeche(f);
                         fireAuswahlChanged(f);
                         try {
                             makeRowVisible(this.jxtOverview, jxtOverview.getFilters().convertRowIndexToView(tableModel.getIndexOfFlaeche((Flaeche) f)));
                         } catch (Exception e) {
                             log.debug("Fehler beim Scrollen der Tabelle", e);
                         }
                     } else {
                         //tableModel.setSelectedFlaeche(null);
                         mappingComp.getFeatureCollection().unselectAll();
                         fireAuswahlChanged(null);
                     }
                 }
             }
         }
     }
 
     private void makeRowVisible(JTable table, int row) {
         Rectangle cellRect = table.getCellRect(row, 0, true);
         if (cellRect != null) {
             table.scrollRectToVisible(cellRect);
         }
     }
 
     public FlaechenUebersichtsTableModel getModel() {
         return this.tableModel;
     }
 
     public void undoSelectedFlaeche() {
         de.cismet.verdis.data.Flaeche f = tableModel.getSelectedFlaeche();
         if (f != null) {
             f.setToBackupFlaeche();
             f.sync();
             fireAuswahlChanged(f);
             tableModel.fireTableDataChanged();
             mappingComp.reconsiderFeature(f);
         }
     }
 
     public void setConnection(Connection c) {
         connection = c;
         tableModel.setConnection(connection);
     }
 
     public void kassenzeichenChanged(String kz) {
         kassenzeichenChanged(kz, null);
     }
 
     public void kassenzeichenChanged(String kz, javax.swing.Timer t) {
         //this.sfViewer=new SimpleFeatureViewer();
         tableModel.kassenzeichenChanged(kz, t);
     }
 
     //vom ListSelectionModel
     public void valueChanged(final ListSelectionEvent ev) {
         //das war drin: (ev==null||ev.getValueIsAdjusting())&& ::warum ?
         if (jxtOverview.getSelectedRow() != -1 && jxtOverview.getSelectedRowCount() == 1) {
 //            EventQueue.invokeLater(new Runnable() {
 //                public void run() {
             try {
                 int displayedIndex = jxtOverview.getSelectedRow();
                 int modelIndex = jxtOverview.getFilters().convertRowIndexToModel(displayedIndex);
                 Flaeche f = getTableModel().getFlaechebyIndex(modelIndex);
 
                 fireAuswahlChanged(f);
                 if (!mappingComp.getFeatureCollection().isSelected(f)) {
                     mappingComp.getFeatureCollection().select(f);
                 }
             } catch (Throwable ee) {
                 log.error("Error in valueChangedListener ", ee);
             }
 //                }
 //            });
         } else if (jxtOverview.getSelectedRowCount() > 1) {
 //            EventQueue.invokeLater(new Runnable() {
 //                public void run() {
             //l\u00F6sche den kram in details
             fireAuswahlChanged(null);
             //aktualisiere die Karte
             Vector v = new Vector();
             int[] rows = jxtOverview.getSelectedRows();
             for (int i = 0; i < rows.length; ++i) {
                 int displayedIndex = rows[i];
                 int modelIndex = jxtOverview.getFilters().convertRowIndexToModel(displayedIndex);
                 Flaeche f = getTableModel().getFlaechebyIndex(modelIndex);
                 if (!mappingComp.getFeatureCollection().isSelected(f)) {
                     v.add(f);
                 }
             }
             if (v.size() > 0) {
                 mappingComp.getFeatureCollection().addToSelection(v);
             }
 //                }
 //            });
 
         }
 
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         jLabel1 = new javax.swing.JLabel();
         jButton1 = new javax.swing.JButton();
         pomScale = new javax.swing.JPopupMenu();
         tbpMain = new javax.swing.JTabbedPane();
         panMap = new javax.swing.JPanel();
         jPanel2 = new javax.swing.JPanel();
         mappingComp = new de.cismet.cismap.commons.gui.MappingComponent();
         panStatus = new javax.swing.JPanel();
         cmdAdd = new javax.swing.JButton();
         lblInfo = new javax.swing.JLabel();
         lblCoord = new javax.swing.JLabel();
         jSeparator1 = new javax.swing.JSeparator();
         lblWaiting = new javax.swing.JLabel();
         lblMeasurement = new javax.swing.JLabel();
         lblScale = new javax.swing.JLabel();
         jSeparator2 = new javax.swing.JSeparator();
         tobVerdis = new javax.swing.JToolBar();
         cmdFullPoly = new javax.swing.JButton();
         cmdFullPoly1 = new javax.swing.JButton();
         cmdBack = new JHistoryButton();
         cmdForward = new JHistoryButton();
         jSeparator5 = new javax.swing.JSeparator();
         cmdWmsBackground = new javax.swing.JButton();
         cmdSnap = new javax.swing.JButton();
         sep2 = new javax.swing.JSeparator();
         cmdZoom = new javax.swing.JButton();
         cmdPan = new javax.swing.JButton();
         cmdSelect = new javax.swing.JButton();
         cmdALB = new javax.swing.JButton();
         cmdMovePolygon = new javax.swing.JButton();
         cmdNewPolygon = new javax.swing.JButton();
         cmdNewPoint = new javax.swing.JButton();
         cmdRaisePolygon = new javax.swing.JButton();
         cmdRemovePolygon = new javax.swing.JButton();
         cmdAttachPolyToAlphadata = new javax.swing.JButton();
         cmdJoinPoly = new javax.swing.JButton();
         cmdSplitPoly = new javax.swing.JButton();
         sep3 = new javax.swing.JSeparator();
         cmdMoveHandle = new javax.swing.JButton();
         cmdAddHandle = new javax.swing.JButton();
         cmdRemoveHandle = new javax.swing.JButton();
         cmdRotatePolygon = new javax.swing.JButton();
         sep4 = new javax.swing.JSeparator();
         cmdUndo = new javax.swing.JButton();
         cmdRedo = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         jxtOverview = new org.jdesktop.swingx.JXTable();
 
         jLabel1.setText("jLabel1");
 
         jButton1.setText("Refresh");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
         setLayout(new java.awt.BorderLayout());
 
         panMap.setLayout(new java.awt.BorderLayout());
 
         jPanel2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2), javax.swing.BorderFactory.createEtchedBorder()), javax.swing.BorderFactory.createEmptyBorder(4, 4, 1, 4)));
         jPanel2.setLayout(new java.awt.BorderLayout());
 
         mappingComp.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         mappingComp.setInternalLayerWidgetAvailable(true);
         jPanel2.add(mappingComp, java.awt.BorderLayout.CENTER);
 
         panStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 1, 1, 1));
         panStatus.setLayout(new java.awt.GridBagLayout());
 
         cmdAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/statusbar/layersman.png"))); // NOI18N
         cmdAdd.setBorderPainted(false);
         cmdAdd.setFocusPainted(false);
         cmdAdd.setMinimumSize(new java.awt.Dimension(25, 25));
        cmdAdd.setOpaque(false);
         cmdAdd.setPreferredSize(new java.awt.Dimension(25, 25));
         cmdAdd.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/statusbar/layersman.png"))); // NOI18N
         cmdAdd.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdAddActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 7;
         gridBagConstraints.gridy = 0;
         panStatus.add(cmdAdd, gridBagConstraints);
 
         lblInfo.setText(" ");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         panStatus.add(lblInfo, gridBagConstraints);
 
         lblCoord.setText(" ");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         panStatus.add(lblCoord, gridBagConstraints);
 
         jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
         panStatus.add(jSeparator1, gridBagConstraints);
 
         lblWaiting.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/exec.png"))); // NOI18N
         lblWaiting.setText(" ");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 6;
         gridBagConstraints.gridy = 0;
         panStatus.add(lblWaiting, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 5;
         gridBagConstraints.gridy = 0;
         panStatus.add(lblMeasurement, gridBagConstraints);
 
         lblScale.setComponentPopupMenu(pomScale);
         lblScale.setText("1:???");
         lblScale.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 lblScaleMousePressed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         panStatus.add(lblScale, gridBagConstraints);
 
         jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
         panStatus.add(jSeparator2, gridBagConstraints);
 
         jPanel2.add(panStatus, java.awt.BorderLayout.SOUTH);
 
         panMap.add(jPanel2, java.awt.BorderLayout.CENTER);
 
         tobVerdis.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         tobVerdis.setFloatable(false);
         tobVerdis.setRollover(true);
 
         cmdFullPoly.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/fullPoly.png"))); // NOI18N
         cmdFullPoly.setToolTipText("Zeige alle Flächen");
         cmdFullPoly.setFocusPainted(false);
         cmdFullPoly.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdFullPolyActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdFullPoly);
 
         cmdFullPoly1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/fullSelPoly.png"))); // NOI18N
         cmdFullPoly1.setToolTipText("Zoom zur ausgewählten Fläche");
         cmdFullPoly1.setFocusPainted(false);
         cmdFullPoly1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdFullPoly1ActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdFullPoly1);
 
         cmdBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/back.png"))); // NOI18N
         cmdBack.setToolTipText("Zurück");
         tobVerdis.add(cmdBack);
 
         cmdForward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/fwd.png"))); // NOI18N
         cmdForward.setToolTipText("Vor");
         tobVerdis.add(cmdForward);
 
         jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator5.setMaximumSize(new java.awt.Dimension(2, 32767));
         tobVerdis.add(jSeparator5);
 
         cmdWmsBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/map.png"))); // NOI18N
         cmdWmsBackground.setToolTipText("Hintergrund an/aus");
         cmdWmsBackground.setFocusPainted(false);
         cmdWmsBackground.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/map_on.png"))); // NOI18N
         cmdWmsBackground.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/map_on.png"))); // NOI18N
         cmdWmsBackground.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdWmsBackgroundActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdWmsBackground);
 
         cmdSnap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/snap.png"))); // NOI18N
         cmdSnap.setToolTipText("Snapping an/aus");
         cmdSnap.setFocusPainted(false);
         cmdSnap.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/snap_selected.png"))); // NOI18N
         cmdSnap.setSelected(true);
         cmdSnap.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/snap_selected.png"))); // NOI18N
         cmdSnap.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdSnapActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdSnap);
 
         sep2.setOrientation(javax.swing.SwingConstants.VERTICAL);
         sep2.setMaximumSize(new java.awt.Dimension(2, 32767));
         tobVerdis.add(sep2);
 
         cmdZoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/zoom.png"))); // NOI18N
         cmdZoom.setToolTipText("Zoomen");
         cmdZoom.setFocusPainted(false);
         cmdZoom.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/zoom_selected.png"))); // NOI18N
         cmdZoom.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/zoom_selected.png"))); // NOI18N
         cmdZoom.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdZoomActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdZoom);
 
         cmdPan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/move2.png"))); // NOI18N
         cmdPan.setToolTipText("Verschieben");
         cmdPan.setFocusPainted(false);
         cmdPan.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/move2_selected.png"))); // NOI18N
         cmdPan.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/move2_selected.png"))); // NOI18N
         cmdPan.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdPanActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdPan);
 
         cmdSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/select.png"))); // NOI18N
         cmdSelect.setToolTipText("Auswählen");
         cmdSelect.setFocusPainted(false);
         cmdSelect.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/select_selected.png"))); // NOI18N
         cmdSelect.setSelected(true);
         cmdSelect.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/select_selected.png"))); // NOI18N
         cmdSelect.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdSelectActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdSelect);
 
         cmdALB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/alb.png"))); // NOI18N
         cmdALB.setToolTipText("ALB");
         cmdALB.setFocusPainted(false);
         cmdALB.setFocusable(false);
         cmdALB.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         cmdALB.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/alb_selected.png"))); // NOI18N
         cmdALB.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/alb_selected.png"))); // NOI18N
         cmdALB.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         cmdALB.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdALBActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdALB);
 
         cmdMovePolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/movePoly.png"))); // NOI18N
         cmdMovePolygon.setToolTipText("Polygon verschieben");
         cmdMovePolygon.setFocusPainted(false);
         cmdMovePolygon.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/movePoly_selected.png"))); // NOI18N
         cmdMovePolygon.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/movePoly_selected.png"))); // NOI18N
         cmdMovePolygon.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdMovePolygonActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdMovePolygon);
 
         cmdNewPolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/newPoly.png"))); // NOI18N
         cmdNewPolygon.setToolTipText("neues Polygon");
         cmdNewPolygon.setFocusPainted(false);
         cmdNewPolygon.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/newPoly_selected.png"))); // NOI18N
         cmdNewPolygon.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/newPoly_selected.png"))); // NOI18N
         cmdNewPolygon.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdNewPolygonActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdNewPolygon);
 
         cmdNewPoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/newPoint.png"))); // NOI18N
         cmdNewPoint.setToolTipText("neuer Punkt");
         cmdNewPoint.setFocusPainted(false);
         cmdNewPoint.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/newPoint_selected.png"))); // NOI18N
         cmdNewPoint.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/newPoint_selected.png"))); // NOI18N
         cmdNewPoint.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdNewPointActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdNewPoint);
 
         cmdRaisePolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/raisePoly.png"))); // NOI18N
         cmdRaisePolygon.setToolTipText("Polygon hochholen");
         cmdRaisePolygon.setFocusPainted(false);
         cmdRaisePolygon.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/raisePoly_selected.png"))); // NOI18N
         cmdRaisePolygon.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/raisePoly_selected.png"))); // NOI18N
         cmdRaisePolygon.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdRaisePolygonActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdRaisePolygon);
 
         cmdRemovePolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/removePoly.png"))); // NOI18N
         cmdRemovePolygon.setToolTipText("Polygon entfernen");
         cmdRemovePolygon.setFocusPainted(false);
         cmdRemovePolygon.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/removePoly_selected.png"))); // NOI18N
         cmdRemovePolygon.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/removePoly_selected.png"))); // NOI18N
         cmdRemovePolygon.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdRemovePolygonActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdRemovePolygon);
 
         cmdAttachPolyToAlphadata.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/polygonAttachment.png"))); // NOI18N
         cmdAttachPolyToAlphadata.setToolTipText("Polygon zuordnen");
         cmdAttachPolyToAlphadata.setFocusPainted(false);
         cmdAttachPolyToAlphadata.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/polygonAttachment_selected.png"))); // NOI18N
         cmdAttachPolyToAlphadata.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/polygonAttachment_selected.png"))); // NOI18N
         cmdAttachPolyToAlphadata.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdAttachPolyToAlphadataActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdAttachPolyToAlphadata);
 
         cmdJoinPoly.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/joinPoly.png"))); // NOI18N
         cmdJoinPoly.setToolTipText("Polygone zusammenfassen");
         cmdJoinPoly.setFocusPainted(false);
         cmdJoinPoly.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/joinPoly_selected.png"))); // NOI18N
         cmdJoinPoly.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/joinPoly_selected.png"))); // NOI18N
         cmdJoinPoly.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdJoinPolyActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdJoinPoly);
 
         cmdSplitPoly.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/splitPoly.png"))); // NOI18N
         cmdSplitPoly.setToolTipText("Polygon splitten");
         cmdSplitPoly.setFocusPainted(false);
         cmdSplitPoly.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/splitPoly_selected.png"))); // NOI18N
         cmdSplitPoly.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/splitPoly_selected.png"))); // NOI18N
         cmdSplitPoly.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdSplitPolyActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdSplitPoly);
 
         sep3.setOrientation(javax.swing.SwingConstants.VERTICAL);
         sep3.setMaximumSize(new java.awt.Dimension(2, 32767));
         tobVerdis.add(sep3);
 
         cmdMoveHandle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/moveHandle.png"))); // NOI18N
         cmdMoveHandle.setToolTipText("Handle verschieben");
         cmdMoveHandle.setFocusPainted(false);
         cmdMoveHandle.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/moveHandle_selected.png"))); // NOI18N
         cmdMoveHandle.setSelected(true);
         cmdMoveHandle.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/moveHandle_selected.png"))); // NOI18N
         cmdMoveHandle.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdMoveHandleActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdMoveHandle);
 
         cmdAddHandle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/addHandle.png"))); // NOI18N
         cmdAddHandle.setToolTipText("Handle hinzufügen");
         cmdAddHandle.setFocusPainted(false);
         cmdAddHandle.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/addHandle_selected.png"))); // NOI18N
         cmdAddHandle.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/addHandle_selected.png"))); // NOI18N
         cmdAddHandle.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdAddHandleActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdAddHandle);
 
         cmdRemoveHandle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/removeHandle.png"))); // NOI18N
         cmdRemoveHandle.setToolTipText("Handle entfernen");
         cmdRemoveHandle.setFocusPainted(false);
         cmdRemoveHandle.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/removeHandle_selected.png"))); // NOI18N
         cmdRemoveHandle.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/removeHandle_selected.png"))); // NOI18N
         cmdRemoveHandle.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdRemoveHandleActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdRemoveHandle);
 
         cmdRotatePolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/rotate16.png"))); // NOI18N
         cmdRotatePolygon.setToolTipText("Rotiere Polygon");
         cmdRotatePolygon.setFocusPainted(false);
         cmdRotatePolygon.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/rotate16.png"))); // NOI18N
         cmdRotatePolygon.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/rotate16_selected.png"))); // NOI18N
         cmdRotatePolygon.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/rotate16_selected.png"))); // NOI18N
         cmdRotatePolygon.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdRotatePolygonActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdRotatePolygon);
 
         sep4.setOrientation(javax.swing.SwingConstants.VERTICAL);
         sep4.setMaximumSize(new java.awt.Dimension(2, 32767));
         tobVerdis.add(sep4);
 
         cmdUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/undo.png"))); // NOI18N
         cmdUndo.setToolTipText("Undo");
         cmdUndo.setEnabled(false);
         cmdUndo.setFocusPainted(false);
         cmdUndo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         cmdUndo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdUndoActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdUndo);
 
         cmdRedo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/redo.png"))); // NOI18N
         cmdRedo.setToolTipText("Redo");
         cmdRedo.setEnabled(false);
         cmdRedo.setFocusable(false);
         cmdRedo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         cmdRedo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         cmdRedo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdRedoActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdRedo);
 
         panMap.add(tobVerdis, java.awt.BorderLayout.NORTH);
 
         tbpMain.addTab("Karte", panMap);
 
         jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
 
         jxtOverview.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         jScrollPane1.setViewportView(jxtOverview);
 
         tbpMain.addTab("Tabelle", jScrollPane1);
 
         add(tbpMain, java.awt.BorderLayout.CENTER);
     }// </editor-fold>//GEN-END:initComponents
     private void cmdNewPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdNewPointActionPerformed
         removeMainGroupSelection();
 //        boolean snapEnab = cismapPrefs.getGlobalPrefs().isSnappingEnabled();
 //        boolean snapVizEnab = cismapPrefs.getGlobalPrefs().isSnappingPreviewEnabled();
 //        mappingComp.setSnappingEnabled(snapEnab);
 //        mappingComp.setVisualizeSnappingEnabled(snapVizEnab);
         cmdNewPoint.setSelected(true);
         mappingComp.setInteractionMode(MappingComponent.NEW_POLYGON);
         ((CreateGeometryListener) mappingComp.getInputListener(MappingComponent.NEW_POLYGON)).setMode(CreateGeometryListener.POINT);
     }//GEN-LAST:event_cmdNewPointActionPerformed
 
     private void lblScaleMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblScaleMousePressed
         if (evt.isPopupTrigger()) {
             pomScale.setVisible(true);
         }
     }//GEN-LAST:event_lblScaleMousePressed
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         mappingComp.refresh();
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void cmdRaisePolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRaisePolygonActionPerformed
         removeMainGroupSelection();
         cmdRaisePolygon.setSelected(true);
         mappingComp.setInteractionMode(MappingComponent.RAISE_POLYGON);
 
     }//GEN-LAST:event_cmdRaisePolygonActionPerformed
 
     private void cmdSnapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSnapActionPerformed
         cmdSnap.setSelected(!cmdSnap.isSelected());
 //        cismapPrefs.getGlobalPrefs().setSnappingEnabled(cmdSnap.isSelected());
 //        cismapPrefs.getGlobalPrefs().setSnappingPreviewEnabled(cmdSnap.isSelected());
         mappingComp.setSnappingEnabled(cmdSnap.isSelected());
         mappingComp.setVisualizeSnappingEnabled(cmdSnap.isSelected());
         mappingComp.setInGlueIdenticalPointsMode(cmdSnap.isSelected());
 
     }//GEN-LAST:event_cmdSnapActionPerformed
 
     private void cmdJoinPolyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdJoinPolyActionPerformed
         removeMainGroupSelection();
         cmdJoinPoly.setSelected(true);
         mappingComp.setInteractionMode(MappingComponent.JOIN_POLYGONS);
     }//GEN-LAST:event_cmdJoinPolyActionPerformed
 
     private void cmdAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAddActionPerformed
         try {
             mappingComp.showInternalLayerWidget(!mappingComp.isInternalLayerWidgetVisible(), 500);
         } catch (Throwable t) {
             log.error("Fehler beim Anzeigen des Layersteuerelements", t);
         }
     }//GEN-LAST:event_cmdAddActionPerformed
 
     private void cmdSplitPolyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSplitPolyActionPerformed
         removeMainGroupSelection();
         cmdSplitPoly.setSelected(true);
         mappingComp.setInteractionMode(MappingComponent.SPLIT_POLYGON);
     }//GEN-LAST:event_cmdSplitPolyActionPerformed
 
     private void cmdAttachPolyToAlphadataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAttachPolyToAlphadataActionPerformed
         removeMainGroupSelection();
         cmdAttachPolyToAlphadata.setSelected(true);
         mappingComp.setInteractionMode(MappingComponent.ATTACH_POLYGON_TO_ALPHADATA);
     }//GEN-LAST:event_cmdAttachPolyToAlphadataActionPerformed
 
     private void cmdRemovePolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemovePolygonActionPerformed
         removeMainGroupSelection();
         cmdRemovePolygon.setSelected(true);
         mappingComp.setInteractionMode(MappingComponent.REMOVE_POLYGON);
 
     }//GEN-LAST:event_cmdRemovePolygonActionPerformed
 
     private void cmdNewPolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdNewPolygonActionPerformed
         removeMainGroupSelection();
 //        boolean snapEnab = cismapPrefs.getGlobalPrefs().isSnappingEnabled();
 //        boolean snapVizEnab = cismapPrefs.getGlobalPrefs().isSnappingPreviewEnabled();
 //        mappingComp.setSnappingEnabled(snapEnab);
 //        mappingComp.setVisualizeSnappingEnabled(snapVizEnab);
         cmdNewPolygon.setSelected(true);
         mappingComp.setInteractionMode(MappingComponent.NEW_POLYGON);
         ((CreateGeometryListener) mappingComp.getInputListener(MappingComponent.NEW_POLYGON)).setMode(CreateGeometryListener.POLYGON);
     }//GEN-LAST:event_cmdNewPolygonActionPerformed
 
     private void cmdMovePolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMovePolygonActionPerformed
         removeMainGroupSelection();
         cmdMovePolygon.setSelected(true);
         mappingComp.setInteractionMode(MappingComponent.MOVE_POLYGON);
     }//GEN-LAST:event_cmdMovePolygonActionPerformed
 
     private void cmdRemoveHandleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemoveHandleActionPerformed
         removeHandleGroupSelection();
         cmdRemoveHandle.setSelected(true);
         mappingComp.setHandleInteractionMode(MappingComponent.REMOVE_HANDLE);
     }//GEN-LAST:event_cmdRemoveHandleActionPerformed
 
     private void cmdAddHandleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAddHandleActionPerformed
         removeHandleGroupSelection();
         cmdAddHandle.setSelected(true);
         mappingComp.setHandleInteractionMode(MappingComponent.ADD_HANDLE);
     }//GEN-LAST:event_cmdAddHandleActionPerformed
 
     private void cmdMoveHandleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMoveHandleActionPerformed
         removeHandleGroupSelection();
         cmdMoveHandle.setSelected(true);
         mappingComp.setHandleInteractionMode(MappingComponent.MOVE_HANDLE);
     }//GEN-LAST:event_cmdMoveHandleActionPerformed
 
     private void cmdFullPoly1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdFullPoly1ActionPerformed
         mappingComp.zoomToSelectedNode();
     }//GEN-LAST:event_cmdFullPoly1ActionPerformed
 
     private void removeMainGroupSelection() {
         cmdSelect.setSelected(false);
         cmdPan.setSelected(false);
         cmdZoom.setSelected(false);
         cmdMovePolygon.setSelected(false);
         cmdNewPolygon.setSelected(false);
         cmdRemovePolygon.setSelected(false);
         cmdAttachPolyToAlphadata.setSelected(false);
         cmdSplitPoly.setSelected(false);
         cmdJoinPoly.setSelected(false);
         cmdRaisePolygon.setSelected(false);
         cmdALB.setSelected(false);
 //        if (mappingComp.isReadOnly()) {
 //            mappingComp.setSnappingEnabled(false);
 //            mappingComp.setVisualizeSnappingEnabled(false);
 //        }
     }
 
     private void removeHandleGroupSelection() {
         cmdRemoveHandle.setSelected(false);
         cmdAddHandle.setSelected(false);
         cmdMoveHandle.setSelected(false);
         cmdRotatePolygon.setSelected(false);
     }
 
     private void cmdRotatePolygonActionPerformed(java.awt.event.ActionEvent evt) {
         removeHandleGroupSelection();
         cmdRotatePolygon.setSelected(true);
         mappingComp.setHandleInteractionMode(MappingComponent.ROTATE_POLYGON);
     }
 
     private void cmdWmsBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdWmsBackgroundActionPerformed
         if (mappingComp.isBackgroundEnabled()) {
             mappingComp.setBackgroundEnabled(false);
             cmdWmsBackground.setSelected(false);
         } else {
             mappingComp.setBackgroundEnabled(true);
             cmdWmsBackground.setSelected(true);
             mappingComp.queryServices();
         }
 
     }//GEN-LAST:event_cmdWmsBackgroundActionPerformed
 
     private void cmdSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSelectActionPerformed
         removeMainGroupSelection();
         cmdSelect.setSelected(true);
         mappingComp.setInteractionMode(MappingComponent.SELECT);
         cmdMoveHandleActionPerformed(null);
     }//GEN-LAST:event_cmdSelectActionPerformed
 
     private void cmdPanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdPanActionPerformed
         removeMainGroupSelection();
         cmdPan.setSelected(true);
         mappingComp.setInteractionMode(MappingComponent.PAN);
 
 
     }//GEN-LAST:event_cmdPanActionPerformed
 
     private void cmdZoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdZoomActionPerformed
         removeMainGroupSelection();
         cmdZoom.setSelected(true);
         mappingComp.setInteractionMode(MappingComponent.ZOOM);
     }//GEN-LAST:event_cmdZoomActionPerformed
 
     private void cmdFullPolyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdFullPolyActionPerformed
         mappingComp.zoomToFullFeatureCollectionBounds();
     }//GEN-LAST:event_cmdFullPolyActionPerformed
 
     private void cmdRedoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRedoActionPerformed
         log.info("REDO");
         CustomAction a = mappingComp.getMemRedo().getLastAction();
         log.debug("... Aktion ausf\u00FChren: " + a.info());
         try {
             a.doAction();
         } catch (Exception e) {
             log.error("Error beim Ausf\u00FChren der Aktion", e);
         }
         CustomAction inverse = a.getInverse();
         mappingComp.getMemUndo().addAction(inverse);
         log.debug("... neue Aktion auf UNDO-Stack: " + inverse);
         log.debug("... fertig");
     }//GEN-LAST:event_cmdRedoActionPerformed
 
     private void cmdUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdUndoActionPerformed
         log.info("UNDO");
         CustomAction a = mappingComp.getMemUndo().getLastAction();
         log.debug("... Aktion ausf\u00FChren: " + a.info());
         try {
             a.doAction();
         } catch (Exception e) {
             log.error("Error beim Ausf\u00FChren der Aktion", e);
         }
         CustomAction inverse = a.getInverse();
         mappingComp.getMemRedo().addAction(inverse);
         log.debug("... neue Aktion auf REDO-Stack: " + inverse);
         log.debug("... fertig");
     }//GEN-LAST:event_cmdUndoActionPerformed
 
     private void cmdALBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdALBActionPerformed
         log.info("ALB");
         removeMainGroupSelection();
         cmdALB.setSelected(true);
         mappingComp.setInteractionMode(MappingComponent.CUSTOM_FEATUREINFO);
 }//GEN-LAST:event_cmdALBActionPerformed
 
     public void removeSelectedFlaeche() {
         Feature f = tableModel.getSelectedFlaeche();
         tableModel.removeSelectedFlaeche();
         mappingComp.getFeatureCollection().removeFeature(f);
     //mappingComp.selectPFeatureManually(null);
     }
 
     public void removeFlaeche(Flaeche f) {
         tableModel.removeFlaeche(f);
         mappingComp.getFeatureCollection().removeFeature(f);
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton cmdALB;
     private javax.swing.JButton cmdAdd;
     private javax.swing.JButton cmdAddHandle;
     private javax.swing.JButton cmdAttachPolyToAlphadata;
     private javax.swing.JButton cmdBack;
     private javax.swing.JButton cmdForward;
     private javax.swing.JButton cmdFullPoly;
     private javax.swing.JButton cmdFullPoly1;
     private javax.swing.JButton cmdJoinPoly;
     private javax.swing.JButton cmdMoveHandle;
     private javax.swing.JButton cmdMovePolygon;
     private javax.swing.JButton cmdNewPoint;
     private javax.swing.JButton cmdNewPolygon;
     private javax.swing.JButton cmdPan;
     private javax.swing.JButton cmdRaisePolygon;
     private javax.swing.JButton cmdRedo;
     private javax.swing.JButton cmdRemoveHandle;
     private javax.swing.JButton cmdRemovePolygon;
     private javax.swing.JButton cmdRotatePolygon;
     private javax.swing.JButton cmdSelect;
     private javax.swing.JButton cmdSnap;
     private javax.swing.JButton cmdSplitPoly;
     private javax.swing.JButton cmdUndo;
     private javax.swing.JButton cmdWmsBackground;
     private javax.swing.JButton cmdZoom;
     private javax.swing.JButton jButton1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JSeparator jSeparator5;
     private org.jdesktop.swingx.JXTable jxtOverview;
     private javax.swing.JLabel lblCoord;
     private javax.swing.JLabel lblInfo;
     private javax.swing.JLabel lblMeasurement;
     private javax.swing.JLabel lblScale;
     private javax.swing.JLabel lblWaiting;
     private de.cismet.cismap.commons.gui.MappingComponent mappingComp;
     private javax.swing.JPanel panMap;
     private javax.swing.JPanel panStatus;
     private javax.swing.JPopupMenu pomScale;
     private javax.swing.JSeparator sep2;
     private javax.swing.JSeparator sep3;
     private javax.swing.JSeparator sep4;
     private javax.swing.JTabbedPane tbpMain;
     private javax.swing.JToolBar tobVerdis;
     // End of variables declaration//GEN-END:variables
 
     public void changeSelectedButtonAccordingToInteractionMode() {
         removeMainGroupSelection();
         String im = mappingComp.getInteractionMode();
         log.debug("changeSelectedButtonAccordingToInteractionMode: " + mappingComp.getInteractionMode(), new CurrentStackTrace());
         if (im.equals(MappingComponent.ZOOM)) {
             cmdZoom.setSelected(true);
         }
         if (im.equals(MappingComponent.CUSTOM_FEATUREINFO)) {
             cmdALB.setSelected(true);
         } else if (im.equals(MappingComponent.PAN)) {
             cmdPan.setSelected(true);
         } else if (im.equals(MappingComponent.SELECT)) {
             cmdSelect.setSelected(true);
         } else if (im.equals(MappingComponent.NEW_POLYGON)) {
             if (((CreateGeometryListener) mappingComp.getInputListener(MappingComponent.NEW_POLYGON)).getMode().equals(CreateGeometryListener.POINT)) {
                 cmdNewPoint.setSelected(true);
             } else if (((CreateGeometryListener) mappingComp.getInputListener(MappingComponent.NEW_POLYGON)).getMode().equals(CreateGeometryListener.POLYGON)) {
                 cmdNewPolygon.setSelected(true);
             } else {
                 cmdSelect.setSelected(true);
             }
         } else {
             cmdSelect.setSelected(true);
         }
 
 
         if (mappingComp.isSnappingEnabled()) {
             cmdSnap.setSelected(true);
         } else {
             cmdSnap.setSelected(false);
         }
     }
 
     public void addAuswahlChangedListener(FlaechenAuswahlChangedListener l) {
         auswahlChangedListeners.add(l);
 
     }
 
     public void removeAuswahlChangedListener(FlaechenAuswahlChangedListener l) {
         auswahlChangedListeners.remove(l);
     }
 
     protected void fireAuswahlChanged(de.cismet.verdis.data.Flaeche f) {
         java.util.Iterator it = auswahlChangedListeners.iterator();
         while (it.hasNext()) {
             try {
                 FlaechenAuswahlChangedListener fac = (FlaechenAuswahlChangedListener) it.next();
                 fac.flaechenAuswahlChanged(f);
             } catch (java.lang.ClassCastException cce) {
                 log.error("FlaechenAuswahlChangedListener nicht vom richtigen Typ.", cce);
             }
         }
     }
 //    public void addWmsBackground(String url) {
 //        if (url.trim().length()>0) {
 //            SimpleWmsGetMapUrl wms=new SimpleWmsGetMapUrl(url,"<cids:width>","<cids:height>","<cids:boundingBox>");
 //            mappingComp.getMappingModel().addRasterService(new SimpleWMS(wms));
 //            mappingComp.setBackgroundEnabled(true);
 //            cmdWmsBackground.setSelected(true);
 //            mappingComp.setMappingModel(mappingComp.getMappingModel());//TODO: Sauber machen: MappingComponent muss \u00C4nderungen implements MappingModel selbst merken oder Methode
 //        }
 //    }
 
     public void setEnabled(boolean b) {
         //In dieser Methode wird eigentlich vom ReadOnly Mode in den RW Mode geschaltet und umgekehrt
         this.mappingComp.setReadOnly(!b);
 //        boolean snapEnab = b & cismapPrefs.getGlobalPrefs().isSnappingEnabled();
 //        boolean snapVizEnab = b & cismapPrefs.getGlobalPrefs().isSnappingPreviewEnabled();
 //        mappingComp.setSnappingEnabled(snapEnab);
 //        mappingComp.setVisualizeSnappingEnabled(snapVizEnab);
 
         this.cmdMovePolygon.setVisible(b);
         //this.cmdNewPolygon.setVisible(b);
         this.cmdRemovePolygon.setVisible(b);
         this.cmdAttachPolyToAlphadata.setVisible(b);
         this.cmdJoinPoly.setVisible(b);
         this.sep3.setVisible(b);
         this.cmdMoveHandle.setVisible(b);
         this.cmdAddHandle.setVisible(b);
         this.cmdRemoveHandle.setVisible(b);
         this.cmdSplitPoly.setVisible(b);
         this.cmdRaisePolygon.setVisible(b);
         this.sep4.setVisible(b);
         repaint();
 
         mappingComp.selectPFeatureManually(mappingComp.getSelectedNode());
         if (!b && !this.cmdZoom.isSelected() && !this.cmdSelect.isSelected() && !cmdPan.isSelected()) {
             cmdSelectActionPerformed(null);
         }
 
 
     }
 
     public void addNewFlaeche(int art, JComponent parent) {
         PFeature sole = mappingComp.getSolePureNewFeature();
         Flaeche f = null;
         if (sole == null) {
             f = getModel().addNewFlaeche(art);
         } else {
             int answer = JOptionPane.showConfirmDialog(parent, "Soll die vorhandene, noch nicht zugeordnete Geometrie der neuen Fl\u00E4che zugeordnet werden?", "Geometrie verwenden?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
             f = getModel().addNewFlaeche(art);
             if (answer == JOptionPane.YES_OPTION) {
 //                tableModel.getSelectedFlaeche().setGeometry(sole.getFeature().getGeometry());
 //                sole.setFeature(tableModel.getSelectedFlaeche());
 //                sole.syncGeometry();
 
                 Geometry g = sole.getFeature().getGeometry();
                 mappingComp.getFeatureCollection().removeFeature(sole.getFeature());
                 f.setGeometry(g);
                 f.setEditable(true);
                 f.setGeometryRemoved(false);
                 mappingComp.getFeatureCollection().addFeature(f);
                 f.setGr_grafik(new Integer((int) (f.getGeometry().getArea())));
                 f.setGr_korrektur(f.getGr_grafik());
                 f.sync();
 
                 //Details synchronisieren
                 mappingComp.getFeatureCollection().select(f);
                 fireAuswahlChanged(f);
 
 
 
 //
 //                mappingComp.refreshHM(sole);
 //                tableModel.getSelectedFlaeche().setGr_grafik(new Integer((int)(tableModel.getSelectedFlaeche().getGeometry().getArea())));
 //                tableModel.getSelectedFlaeche().setGr_korrektur(tableModel.getSelectedFlaeche().getGr_grafik());
 //                tableModel.getSelectedFlaeche().sync();
 //                mappingComp.reconsiderFeature(tableModel.getSelectedFlaeche());
             }
         }
     }
 
     public void setCismapPreferences(CismapPreferences p) {
         cismapPrefs = p;
 //        new Thread() {
 //            public void run() {
         mappingComp.setPreferences(cismapPrefs);
         //mappingComp.setFeatureCollection(tableModel); DOUBLECHECK
 //        String sMode = cismapPrefs.getGlobalPrefs().getStartMode();
 //        if (sMode.equals(mappingComp.ZOOM)) {
 //            cmdZoomActionPerformed(null);
 //        } else if (sMode.equals(mappingComp.PAN)) {
 //            cmdPanActionPerformed(null);
 //        } else if (sMode.equals(mappingComp.SELECT)) {
 //            cmdSelectActionPerformed(null);
 //        }
 //        mappingComp.gotoInitialBoundingBox();
         Collection c = mappingComp.getMappingModel().getFeatureServices().values();
         for (Object featureService : c) {
             ((RetrievalService) featureService).addRetrievalListener(FlaechenUebersichtsTabellenPanel.this);
         }
         c = mappingComp.getMappingModel().getRasterServices().values();
         for (Object rasterService : c) {
             ((RetrievalService) rasterService).addRetrievalListener(FlaechenUebersichtsTabellenPanel.this);
         }
 //            }
 //        }.start();
 
 
     }
     ImageIcon[] waiting = new ImageIcon[32];
     int waitingCounter = 0;
 
     private void initWaitingIcon() {
         for (int i = 0; i < 32; ++i) {
             waiting[i] = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/statusbar/wait/wait" + i + ".png"));
         }
     }
     javax.swing.Timer animationTimer = null;
     //former synchronized
 
     private void setWaiting(boolean wait) {
         lblWaiting.setVisible(wait);
 //        if (wait&&animationTimer==null) {
 //            java.awt.event.ActionListener iconAnimator = new java.awt.event.ActionListener() {
 //                int counter=0;
 //                public void actionPerformed( java.awt.event.ActionEvent event ) {
 //                    javax.swing.Timer tt=(javax.swing.Timer)event.getSource();
 //                    if (tt.isRepeats()==false) {
 //                        lblWaiting.setIcon(null);
 //                    } else {
 //                        lblWaiting.setIcon(waiting[counter%32]);
 //                        counter++;
 //                    }
 //                }
 //            };
 //            animationTimer = new javax.swing.Timer(200, iconAnimator );
 //            animationTimer.setRepeats(true);
 //            animationTimer.start();
 //        } else if (!wait&&animationTimer!=null) {
 //            animationTimer.setRepeats(false);
 //            animationTimer.stop();
 //            animationTimer=null;
 //        }
     }
 
     /**
      * This method gets called when a bound property is changed.
      * @param evt A PropertyChangeEvent object describing the event source
      *   	and the property that has changed.
      */
     public void propertyChange(java.beans.PropertyChangeEvent evt) {
 //        if (evt.getPropertyName().equalsIgnoreCase("activityChanged")) {
 //            if (mappingComp.isRunning()) {
 //                setWaiting(true);
 //            } else {
 //                setWaiting(false);
 //            }
 //        }
     }
 
     public void reEnumerateFlaechen() {
         TableSorter sort = new TableSorter(tableModel);
         sort.setSortingStatus(3, sort.DESCENDING);
         int counterInt = 0;
         String counterString = null;
         for (int i = 0; i < tableModel.getRowCount(); ++i) {
             Flaeche f = tableModel.getFlaechebyIndex(sort.getSortedIndex(i));
             //String newBemerkung=f.getBemerkung()+"\n<HistBez>"+f.getBezeichnung()+"</HistBez>";
             //f.setBemerkung(newBemerkung.trim());
             int art = f.getArt();
 
             switch (art) {
                 case 1:
                 case 2:
                     counterInt++;
                     f.setBezeichnung(new Integer(counterInt).toString());
                     break;
                 case 3:
                 case 4:
                 case 5:
                 case 6:
                 default:
                     counterString = tableModel.nextFlBez(counterString);
                     f.setBezeichnung(counterString);
             }
             f.sync();
         }
         tableModel.fireTableDataChanged();
         fireAuswahlChanged(tableModel.getSelectedFlaeche());
         repaint();
 
     }
 
     public JXTable getJxtOverview() {
         return jxtOverview;
     }
 
     public FlaechenUebersichtsTableModel getTableModel() {
         return tableModel;
     }
 
     public void featuresRemoved(FeatureCollectionEvent fce) {
     }
 
     public void allFeaturesRemoved(FeatureCollectionEvent fce) {
     }
 
     public void featuresChanged(FeatureCollectionEvent fce) {
         log.debug("FeatureChanged");
         if (mappingComp.getInteractionMode() == MappingComponent.NEW_POLYGON) {
             refreshMeasurementsInStatus(fce.getEventFeatures());
         } else {
             refreshMeasurementsInStatus();
         }
     }
 
     public void featuresAdded(FeatureCollectionEvent fce) {
     }
 
     public void featureSelectionChanged(FeatureCollectionEvent fce) {
         Collection<Feature> cf = fce.getEventFeatures();
         jxtOverview.getSelectionModel().removeListSelectionListener(this);
 
         if (cf != null) {
             for (Feature f : cf) {
                 if (f instanceof Flaeche) {
                     int modelIndex = getTableModel().getIndexOfFlaeche((Flaeche) f);
                     int displayedIndex = jxtOverview.getFilters().convertRowIndexToView(modelIndex);
                     if (fce.getFeatureCollection().isSelected(f)) {
                         jxtOverview.getSelectionModel().addSelectionInterval(displayedIndex, displayedIndex);
                     } else {
                         jxtOverview.getSelectionModel().removeSelectionInterval(displayedIndex, displayedIndex);
                     }
                 }
             }
             jxtOverview.getSelectionModel().addListSelectionListener(this);
             if (cf.size() > 0) {
                 valueChanged(null);
             }
             refreshMeasurementsInStatus();
         }
     }
 
     public void featureReconsiderationRequested(FeatureCollectionEvent fce) {
     }
 
     public void refreshMeasurementsInStatus() {
         Collection<Feature> cf = mappingComp.getFeatureCollection().getSelectedFeatures();
         refreshMeasurementsInStatus(cf);
     }
 
     public void refreshMeasurementsInStatus(Collection<Feature> cf) {
         double umfang = 0.0;
         double area = 0.0;
         for (Feature f : cf) {
             if (f != null && f.getGeometry() != null) {
                 area += f.getGeometry().getArea();
                 umfang += f.getGeometry().getLength();
             }
         }
         if ((area == 0.0 && umfang == 0.0) || cf.size() == 0) {
             lblMeasurement.setText("");
         } else {
             lblMeasurement.setText("Fl\u00E4che: " + StaticDecimalTools.round(area) + "  Umfang: " + StaticDecimalTools.round(umfang));
         }
 
     }
     //RetrievalListener nur zum Setzten der ProgressB\u00E4llchen
     HashSet activeRetrievalServices = new HashSet();
 
     public void retrievalStarted(RetrievalEvent e) {
         activeRetrievalServices.add(((ServiceLayer) e.getRetrievalService()).getName());
         checkProgress();
     }
 
     public void retrievalProgress(RetrievalEvent e) {
     }
 
     public void retrievalError(RetrievalEvent e) {
         activeRetrievalServices.remove(((ServiceLayer) e.getRetrievalService()).getName());
         checkProgress();
     }
 
     public void retrievalComplete(RetrievalEvent e) {
         activeRetrievalServices.remove(((ServiceLayer) e.getRetrievalService()).getName());
         checkProgress();
     }
 
     public void retrievalAborted(RetrievalEvent e) {
         activeRetrievalServices.remove(((ServiceLayer) e.getRetrievalService()).getName());
         checkProgress();
     }
 
     private void checkProgress() {
         // log.debug(activeRetrievalServices);
         if (activeRetrievalServices.size() > 0) {
             setWaiting(true);
         } else {
             setWaiting(false);
         }
     }
 
     private void addScalePopupMenu(String text, final double scaleDenominator) {
         JMenuItem jmi = new JMenuItem(text);
         jmi.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 mappingComp.gotoBoundingBoxWithHistory(mappingComp.getBoundingBoxFromScale(scaleDenominator));
             }
         });
         pomScale.add(jmi);
     }
 
     public void featureCollectionChanged() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void update(Observable o, Object arg) {
         if (o.equals(mappingComp.getMemUndo())) {
             if (arg.equals(MementoInterface.ACTIVATE) && !cmdUndo.isEnabled()) {
                 log.debug("UNDO-Button aktivieren");
                 EventQueue.invokeLater(new Runnable() {
 
                     public void run() {
                         cmdUndo.setEnabled(true);
                     }
                 });
             } else if (arg.equals(MementoInterface.DEACTIVATE) && cmdUndo.isEnabled()) {
                 log.debug("UNDO-Button deaktivieren");
                 EventQueue.invokeLater(new Runnable() {
 
                     public void run() {
                         cmdUndo.setEnabled(false);
                     }
                 });
             }
         } else if (o.equals(mappingComp.getMemRedo())) {
             if (arg.equals(MementoInterface.ACTIVATE) && !cmdRedo.isEnabled()) {
                 log.debug("REDO-Button aktivieren");
                 EventQueue.invokeLater(new Runnable() {
 
                     public void run() {
                         cmdRedo.setEnabled(true);
                     }
                 });
             } else if (arg.equals(MementoInterface.DEACTIVATE) && cmdRedo.isEnabled()) {
                 log.debug("REDO-Button deaktivieren");
                 EventQueue.invokeLater(new Runnable() {
 
                     public void run() {
                         cmdRedo.setEnabled(false);
                     }
                 });
             }
         }
     }
 }
