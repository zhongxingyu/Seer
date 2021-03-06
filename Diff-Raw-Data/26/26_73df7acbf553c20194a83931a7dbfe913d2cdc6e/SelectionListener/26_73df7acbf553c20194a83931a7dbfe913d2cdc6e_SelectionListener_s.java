 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cismap.commons.gui.piccolo.eventlistener;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.PrecisionModel;
 
 import edu.umd.cs.piccolo.event.PInputEvent;
 import edu.umd.cs.piccolox.event.PNotificationCenter;
 
 import org.openide.util.Lookup;
 
 import java.awt.geom.Point2D;
 
 import java.util.*;
 
 import javax.swing.Action;
 import javax.swing.JMenu;
 import javax.swing.JPopupMenu;
 import javax.swing.JSeparator;
 
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.WorldToScreenTransform;
 import de.cismet.cismap.commons.features.CommonFeatureAction;
 import de.cismet.cismap.commons.features.DefaultFeatureCollection;
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.features.SearchFeature;
 import de.cismet.cismap.commons.gui.MapPopupAction;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.tools.PFeatureTools;
 
 import de.cismet.tools.gui.ActionsProvider;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten.hell@cismet.de
  * @version  $Revision$, $Date$
  */
 public class SelectionListener extends RectangleRubberBandListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final String SELECTION_CHANGED_NOTIFICATION = "SELECTION_CHANGED_NOTIFICATION"; // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     PFeature sel = null;
     Vector<PFeature> pfVector = new Vector<PFeature>();
     MappingComponent mappingComponent = null;
     ArrayList<? extends CommonFeatureAction> commonFeatureActions = null;
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private int clickCount = 0;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new SelectionListener object.
      */
     public SelectionListener() {
         final Lookup.Result<CommonFeatureAction> result = Lookup.getDefault().lookupResult(CommonFeatureAction.class);
         commonFeatureActions = new ArrayList<CommonFeatureAction>(result.allInstances());
         Collections.sort(commonFeatureActions, new Comparator<CommonFeatureAction>() {
 
                 @Override
                 public int compare(final CommonFeatureAction o1, final CommonFeatureAction o2) {
                     return Integer.valueOf(o1.getSorter()).compareTo(Integer.valueOf(o2.getSorter()));
                 }
             });
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Selektiere einen PNode.
      *
      * @param  pInputEvent  DOCUMENT ME!
      */
     @Override
     public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
         if (log.isDebugEnabled()) {
             log.debug("mouseClicked():" + pInputEvent.getPickedNode()); // NOI18N
         }
        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[] { PFeature.class });
         clickCount = pInputEvent.getClickCount();
         if (pInputEvent.getComponent() instanceof MappingComponent) {
             mappingComponent = (MappingComponent)pInputEvent.getComponent();
         }
 
         if (pInputEvent.isRightMouseButton()) {
             if (log.isDebugEnabled()) {
                 log.debug("right mouseclick"); // NOI18N
             }
             final JPopupMenu popup = new JPopupMenu("MapPopup");
 
             if (o instanceof PFeature) {
                 final PFeature pf = (PFeature)o;
                 if (pf.getFeature() instanceof ActionsProvider) {
                     final ActionsProvider ap = (ActionsProvider)((PFeature)o).getFeature();
                     final Collection<? extends Action> ac = ap.getActions();
                     for (final Action a : ac) {
                         popup.add(a);
                     }
                 }
 
                 final JSeparator sep = new JSeparator();
 
                 if (popup.getComponentCount() > 0) {
                     popup.add(sep);
                 }
 
                 int commonActionCounter = 0;
                 if (commonFeatureActions != null) {
                     for (final CommonFeatureAction cfa : commonFeatureActions) {
                         cfa.setSourceFeature(pf.getFeature());
                         if (cfa.isActive()) {
                             popup.add(cfa);
                             commonActionCounter++;
                         }
                     }
                 }
                 if (commonActionCounter == 0) {
                     popup.remove(sep);
                 }
             }
 
             // we build a popup menu from all the registered generic point actions
             final Point point = createPointFromInput(pInputEvent);
 
             final Collection<? extends MapPopupAction> lookupResult = Lookup.getDefault()
                         .lookupAll(MapPopupAction.class);
             final ArrayList<MapPopupAction> popupActions = new ArrayList<MapPopupAction>(lookupResult);
             Collections.sort(popupActions);
 
             boolean first = true;
             for (final MapPopupAction action : popupActions) {
                 action.setPoint(point);
 
                 if (action.isActive(o instanceof PFeature)) {
                     if (first && (popup.getComponentCount() > 0)) {
                         popup.add(new JSeparator());
                         first = false;
                     }
 
                     final JMenu submenu = action.getSubmenu();
 
                     if (submenu != null) {
                         popup.add(submenu);
                     } else {
                         popup.add(action);
                     }
                 }
             }
 
             if (popup.getComponentCount() > 0) {
                 popup.show(
                     mappingComponent,
                     (int)pInputEvent.getCanvasPosition().getX(),
                     (int)pInputEvent.getCanvasPosition().getY());
             }
         } else {
             if (o instanceof PFeature) {
                 super.mouseClicked(pInputEvent);
                 sel = (PFeature)o;
                 if (sel.getFeature().canBeSelected()) {
                     if ((mappingComponent != null) && pInputEvent.isLeftMouseButton() && pInputEvent.isControlDown()) {
                         if (mappingComponent.getFeatureCollection() instanceof DefaultFeatureCollection) {
                             if (
                                 !((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).isSelected(
                                             sel.getFeature())) {
                                 ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).addToSelection(
                                     sel.getFeature());
                             } else {
                                 ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselect(
                                     sel.getFeature());
                             }
                         } else {
                             log.warn("mc.getFeatureCollection() instanceof DefaultFeatureCollection == false !!!!!!!"); // NOI18N
                         }
                     } else {
                         mappingComponent.getFeatureCollection().select(sel.getFeature());
                         if (pInputEvent.isAltDown()) {
                             final Coordinate mouseCoord = new Coordinate(
                                     mappingComponent.getWtst().getSourceX(
                                         pInputEvent.getPosition().getX()
                                                 - mappingComponent.getClip_offset_x()),
                                     mappingComponent.getWtst().getSourceY(
                                         pInputEvent.getPosition().getY()
                                                 - mappingComponent.getClip_offset_y()));
 
                             final GeometryFactory geometryFactory = new GeometryFactory(
                                     new PrecisionModel(PrecisionModel.FLOATING),
                                     CrsTransformer.extractSridFromCrs(
                                         mappingComponent.getMappingModel().getSrs().getCode()));
                             final Point mousePoint = CrsTransformer.transformToGivenCrs(geometryFactory.createPoint(
                                         mouseCoord),
                                     CismapBroker.getInstance().getDefaultCrs());
 
                             sel.setSelectedEntity(sel.getEntityPositionUnderPoint(mousePoint));
                         }
                     }
                 } else {
                     if (log.isDebugEnabled()) {
                         log.debug("Feature cannot be selected"); // NOI18N
                     }
                     if (mappingComponent.getFeatureCollection() instanceof DefaultFeatureCollection) {
                         ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselectAll();
                     }
                 }
                 postSelectionChanged();
                 if (pInputEvent.getClickCount() == 2) {
                     if (sel.getFeature() instanceof SearchFeature) {
                         final SearchFeature searchFeature = (SearchFeature)sel.getFeature();
                         if (pInputEvent.isLeftMouseButton()) {
                             ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselectAll();
                             mappingComponent.getHandleLayer().removeAllChildren();
                             // neue Suche mit Geometry auslösen
                             ((AbstractCreateSearchGeometryListener)mappingComponent.getInputListener(
                                     searchFeature.getInteractionMode())).search(searchFeature);
                         }
                     }
                 }
             } else {
                 if (mappingComponent.getFeatureCollection() instanceof DefaultFeatureCollection) {
                     ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselectAll();
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   event  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Point createPointFromInput(final PInputEvent event) {
         final Point2D pos = event.getPosition();
         final WorldToScreenTransform wtst = mappingComponent.getWtst();
         final Coordinate coord = new Coordinate(wtst.getSourceX(pos.getX()), wtst.getSourceY(pos.getY()));
         final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                 CrsTransformer.getCurrentSrid());
 
         return gf.createPoint(coord);
     }
 
     @Override
     public void mouseDragged(final PInputEvent e) {
         super.mouseDragged(e); // To change body of generated methods, choose Tools | Templates.
 
         clickCount = e.getClickCount();
     }
 
     /**
      * Wird gefeuert, wenn die Maustaste nach dem Ziehen des Markiervierecks losgelassen wird.
      *
      * @param  event  das Mouseevent (als PInputEvent)
      */
     @Override
     public void mouseReleased(final PInputEvent event) {
         super.mouseReleased(event);
         if (event.getButton() == 1) { // linke Maustaste
             // Mouseevent muss von einer MappingComponent gefeuert werden
             if (event.getComponent() instanceof MappingComponent) {
                 mappingComponent = (MappingComponent)event.getComponent();
                 mappingComponent.getHandleLayer().removeAllChildren();
                 // einfacher Klick ohne ziehen des Markiervierecks
                 // rectangle ist bei einem Linksklick nie null. Siehe mousePressed in RectangleRubberBandListener
                 if ((rectangle != null) && !rectangleEmpty) {
                     if (log.isDebugEnabled()) {
                         // Hole alle PFeatures die das Markierviereck schneiden
                         // und Hinzuf\u00FCgen dieser PFeatures zur Selektion
                         log.debug("Markierviereck = (X=" + rectangle.getBounds().getX() + ",Y="
                                     + rectangle.getBounds().getY() // NOI18N
                                     + ",W=" + rectangle.getBounds().getWidth() + ",H="
                                     + rectangle.getBounds().getHeight() + ")"); // NOI18N
                     }
 
                     if (!event.isControlDown()) {
                         ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselectAll();
                     }
                     final PFeature[] pfArr = PFeatureTools.getPFeaturesInArea(mappingComponent, rectangle.getBounds());
                     final Vector<Feature> toBeSelected = new Vector<Feature>();
                     final Vector<Feature> toBeUnselected = new Vector<Feature>();
 
                     for (final PFeature pf : pfArr) {
                         if (pf.getFeature().canBeSelected()) {
                             if (mappingComponent.getFeatureCollection() instanceof DefaultFeatureCollection) {
                                 if (
                                     !((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).isSelected(
                                                 pf.getFeature())) {
                                     if (log.isDebugEnabled()) {
                                         log.debug("Feature markiert: " + pf); // NOI18N
                                     }
                                     toBeSelected.add(pf.getFeature());
                                 } else {
                                     toBeUnselected.add(pf.getFeature());
                                     // mappingComponent.getFeatureCollection().unselect(pf.getFeature()); //war vorher
                                     // unselectAll()
                                 }
                             }
                         } else {
                             if (log.isDebugEnabled()) {
                                 log.debug("Feature cannot be selected");      // NOI18N
                             }
                             if (mappingComponent.getFeatureCollection() instanceof DefaultFeatureCollection) {
                                 toBeUnselected.add(pf.getFeature());
                                 // ((DefaultFeatureCollection)
                                 // mappingComponent.getFeatureCollection()).unselect(pf.getFeature());//war vorher
                                 // unselectAll()
                             }
                         }
                     }
 
                     // Hier passierts
                     ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).addToSelection(toBeSelected);
                     ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselect(toBeUnselected);
 
                     pfVector = new Vector(((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
                                     .getSelectedFeatures());
                     postSelectionChanged();
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void postSelectionChanged() {
         if (log.isDebugEnabled()) {
             log.debug("postSelectionChanged"); // NOI18N
         }
         final PNotificationCenter pn = PNotificationCenter.defaultCenter();
         pn.postNotification(SelectionListener.SELECTION_CHANGED_NOTIFICATION, this);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Vector<PFeature> getSelectedPFeatures() {
         return pfVector;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Deprecated
     public PFeature getSelectedPFeature() {
         return sel;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public PFeature getAffectedPFeature() {
         return sel;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int getClickCount() {
         return clickCount;
     }
 }
