 package fr.nantes1900.control.display3d;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.media.j3d.BranchGroup;
 import javax.swing.event.EventListenerList;
 
 import com.sun.j3d.utils.picking.PickCanvas;
 import com.sun.j3d.utils.picking.PickIntersection;
import com.sun.j3d.utils.picking.PickResult;
 import com.sun.j3d.utils.picking.PickTool;
 
 import fr.nantes1900.listener.ElementsSelectedListener;
 import fr.nantes1900.models.basis.Mesh;
 import fr.nantes1900.models.basis.Point;
 import fr.nantes1900.models.basis.Triangle;
 import fr.nantes1900.models.extended.Surface;
 import fr.nantes1900.view.display3d.MeshView;
 import fr.nantes1900.view.display3d.SurfaceView;
 import fr.nantes1900.view.display3d.Universe3DView;
 
 /**
  * The Universe3DController generates the Universe3DView and contains the
  * pickCanvas. It implements MouseListener, MouseMotionListener to handle users
  * clicks on the 3D view.
  * @author Siju Wu, Nicolas Bouillon, Daniel Lefevre
  */
 public class Universe3DController implements MouseListener, MouseMotionListener {
 
     /**
      * The Universe3DView linked to this controller.
      */
     private Universe3DView u3DView;
     /**
      * The canvas used to pick.
      */
     private PickCanvas pickCanvas;
     /**
      * The class overwriting MouseRotate to change the roation center.
      */
     private NewMouseRotate mouseRotate;
 
     /**
      * The list of ElementsSelected listeners.
      */
     private final EventListenerList listeners = new EventListenerList();
 
     /**
      * The attribute to know if the U3DView has to display the mesh or the
      * polygon (a surface is compounded of a mesh and a polygon).
      */
     private int displayMode;
     /**
      * The attribute to know that the surfaces should be displayed by meshes of
      * triangles.
      */
     public static final int DISPLAY_MESH_MODE = 3;
     /**
      * The attribute to know that the surfaces should be displayed by polygons.
      */
     public static final int DISPLAY_POLYGON_MODE = 4;
     /**
      * The attribute to know about the selection mode: triangles or surfaces.
      */
     private int selectionMode;
     /**
      * The attribute to know about the lock mode: lock or unlock.
      */
     private int lockMode;
     /**
      * The attribute to know that mouse left click will pick triangles.
      */
     public static final int SELECTION_TRIANGLE_MODE = 1;
     /**
      * The attribute to know that mouse left click will pick surfaces.
      */
     public static final int SELECTION_SURFACE_MODE = 2;
     /**
      * A constant defining the orientation tolerance (in degrees) when getting
      * all the triangles oriented as a triangle input.
      */
     public static final int ORIENTATION_TOLERANCE = 20;
     /**
      * Constant defining the unlocked mode.
      */
     public static final int UNLOCK_MODE = 5;
     /**
      * Constant defining the locked mode.
      */
     public static final int LOCK_MODE = 6;
     /**
      * The list of the triangles currently selected.
      */
     private List<Triangle> trianglesSelected = new ArrayList<>();
     /**
      * The list of the meshes selected. This list is used when deselecting a set
      * of triangles.
      */
     private List<Mesh> meshesSelected = new ArrayList<>();
     /**
      * The list of the surfaces currently selected.
      */
     private List<Surface> surfacesSelected = new ArrayList<>();
     /**
      * The surface locked.
      */
     private Surface surfaceLocked;
 
     /**
      * Generates the U3DView and set the display mode to mesh.
      */
     public Universe3DController() {
         this.u3DView = new Universe3DView(this);
         this.displayMode = DISPLAY_MESH_MODE;
         this.selectionMode = SELECTION_TRIANGLE_MODE;
         this.lockMode = UNLOCK_MODE;
     }
 
     /**
      * Adds a new ElementsSelectedListener.
      * @param listener
      *            the listener
      */
     public final void addElementsSelectedListener(
             final ElementsSelectedListener listener) {
         this.listeners.add(ElementsSelectedListener.class, listener);
     }
 
     /**
      * Changes the rotation center.
      */
     public final void changeRotationCenter() {
         Point center = null;
 
         if (this.selectionMode == SELECTION_SURFACE_MODE) {
             if (!this.surfacesSelected.isEmpty()) {
                 SurfaceView surfaceViewSeleted = this
                         .getSurfaceViewFromSurface(this.surfacesSelected.get(0));
 
                 center = surfaceViewSeleted.getMeshView().getCentroid();
             }
         } else {
             if (!this.trianglesSelected.isEmpty()) {
                 center = this.trianglesSelected.get(0).getP1();
             }
         }
 
         if (center != null) {
             this.mouseRotate.setCenter(center);
         }
 
     }
 
     /**
      * Changes the rotation center when clicking on a surface.
      * @param surfaceView
      *            The surfaceView becomming the rotation center
      */
     public final void changeRotationCenter(final SurfaceView surfaceView) {
 
         if (surfaceView.getMeshView() != null) {
             Point center = new Point(surfaceView.getMeshView().getCentroid()
                     .getX(), surfaceView.getMeshView().getCentroid().getY(),
                     surfaceView.getMeshView().getCentroid().getZ());
             this.mouseRotate.setCenter(center);
         }
 
     }
 
     /**
      * Change the selection mode.
      * @param selectionModeIn
      *            the new selection mode
      */
     public final void changeSelectionMode(final int selectionModeIn) {
         if (this.surfacesSelected != null) {
             this.deselectEverySurfaces();
         }
         if (this.meshesSelected != null) {
             List<Mesh> meshesToRemove = new ArrayList<>(this.meshesSelected);
             this.unSelectTriangles(meshesToRemove);
         }
         this.selectionMode = selectionModeIn;
     }
 
     /**
      * Clears everything in the Universe 3D View.
      */
     public final void clearAll() {
         this.surfacesSelected.clear();
         this.u3DView.clearAll();
     }
 
     /**
      * Deselects every surfaces.
      */
     public final void deselectEverySurfaces() {
         // Copies the list to avoid ConcurrentModificationException of the list
         // surfacesSelected.
         List<Surface> surfaces = new ArrayList<>(this.surfacesSelected);
 
         for (Surface surface : surfaces) {
             this.selectOrUnselectSurface(this
                     .getSurfaceViewFromSurface(surface));
         }
     }
 
     /**
      * Notifies all the ElementsSelectedListeners that triangles have been
      * selected.
      */
     private void fireNewTrianglesSelection() {
         ElementsSelectedListener[] elementsSelectedListeners = this.listeners
                 .getListeners(ElementsSelectedListener.class);
 
         for (ElementsSelectedListener listener : elementsSelectedListeners) {
             listener.newTrianglesSelection(this.trianglesSelected);
         }
     }
 
     /**
      * Notifies all the ElementsSelectedListeners that a surface has been
      * deselected.
      * @param surfaceDeselected
      *            the surface deselected
      */
     private void fireSurfaceDeselected(final Surface surfaceDeselected) {
         ElementsSelectedListener[] elementsSelectedListeners = this.listeners
                 .getListeners(ElementsSelectedListener.class);
 
         for (ElementsSelectedListener listener : elementsSelectedListeners) {
             listener.surfaceDeselected(surfaceDeselected);
         }
     }
 
     /**
      * Notifies all the ElementsSelectedListeners that a surface has been
      * selected.
      * @param surfaceSelected
      *            the surface selected
      */
     private void fireSurfaceSelected(final Surface surfaceSelected) {
         ElementsSelectedListener[] elementsSelectedListeners = this.listeners
                 .getListeners(ElementsSelectedListener.class);
 
         for (ElementsSelectedListener listener : elementsSelectedListeners) {
             listener.surfaceSelected(surfaceSelected);
         }
     }
 
     /**
      * Get the meshes from meshesSelected list not containing the input
      * triangle.
      * @param triangle
      *            The triangle to check.
      * @return the meshes not containing the triangle.
      */
     public final List<Mesh> getComplementaryMeshesSelected(
             final Triangle triangle) {
         List<Mesh> meshes = new ArrayList<>();
         for (Mesh m : this.meshesSelected) {
             if (!m.contains(triangle)) {
                 meshes.add(m);
             }
         }
         return meshes;
     }
 
     /**
      * Getter.
      * @return the display mode
      */
     public final int getDisplayMode() {
         return this.displayMode;
     }
 
     /**
      * Returns the lock mode.
      * @return LOCK_MODE if the mode is locked, UNLOCK_MODE if the mode is
      *         unlocked, or everything else if there was an error in the
      *         initialisation.
      */
     public final int getLockOrUnlockMode() {
         return this.lockMode;
     }
 
     /**
      * Get the list of meshes selected.
      * @return meshesSelected the list of the meshes selected.
      */
     public final List<Mesh> getMeshesSelected() {
         return this.meshesSelected;
     }
 
     /**
      * Gets the mesh from meshesSelected list containing the input triangle.
      * @param triangle
      *            The triangle to check.
      * @return the mesh containing the triangle.
      */
     public final Mesh getMeshSelected(final Triangle triangle) {
         Mesh mesh = null;
         for (Mesh m : this.meshesSelected) {
             if (m.contains(triangle)) {
                 mesh = m;
             }
         }
         return mesh;
     }
 
     /**
      * Gets the MeshView containing the input Triangle.
      * @param triangle
      *            the input triangle to search the meshView from.
      * @return the meshView result.
      */
     public final MeshView getMeshViewFromTriangle(final Triangle triangle) {
         for (SurfaceView sV : this.u3DView.getSurfaceViewList()) {
             if (sV.getMeshView().getMesh().contains(triangle)) {
                 return sV.getMeshView();
             }
         }
         return null;
     }
 
     /**
      * Getter.
      * @return the mouse rotate class
      */
     public final NewMouseRotate getMouseRotate() {
         return this.mouseRotate;
     }
 
     /**
      * Getter.
      * @return the selection mode
      */
     public final int getSelectionMode() {
         return this.selectionMode;
     }
 
     /**
      * Getter.
      * @return the surface locked
      */
     public final Surface getSurfaceLocked() {
         return this.surfaceLocked;
     }
 
     /**
      * Setter.
      * @return the surfaces selected
      */
     public final List<Surface> getSurfacesSelected() {
         return this.surfacesSelected;
     }
 
     /**
      * Returns the SurfaceView associated to the surface.
      * @param surface
      *            the surface to search
      * @return the surfaceView associated
      */
     public final SurfaceView getSurfaceViewFromSurface(final Surface surface) {
         for (SurfaceView sView : this.u3DView.getSurfaceViewList()) {
             if (sView.getSurface() == surface) {
                 return sView;
             }
         }
         return null;
     }
 
     /**
      * Returns the list of Triangle associated with the trianglesView contained
      * in trianglesViewSelected.
      * @return the list of selected triangles
      */
     public final List<Triangle> getTrianglesSelected() {
         return this.trianglesSelected;
     }
 
     /**
      * Getter.
      * @return the universe 3D view
      */
     public final Universe3DView getUniverse3DView() {
         return this.u3DView;
     }
 
     /**
      * Hide the surface selected.
      * @param surfacehide
      *            the surface selected to hide.
      */
     public final void hideSurface(final Surface surfacehide) {
         SurfaceView surfaceViewHide = this
                 .getSurfaceViewFromSurface(surfacehide);
         surfaceViewHide.removeAllGeometries();
     }
 
     /**
      * Treats the different clicking actions (left or right).
      * @param e
      *            The MouseEvent caught by the mouseListener when clicking
      */
     @Override
     public final void mouseClicked(final MouseEvent e) {
         int buttonDown = e.getButton();
         if (this.pickCanvas != null) {
             this.pickCanvas.setShapeLocation(e);
             PickResult result = this.pickCanvas.pickClosest();
 
             if (buttonDown == MouseEvent.BUTTON1 && result != null) {
                 if (this.selectionMode == SELECTION_TRIANGLE_MODE) {
                     this.treatTriangleSelection(e, result);
                 } else if (this.selectionMode == SELECTION_SURFACE_MODE) {
                     this.treatSurfaceSelection(e, result);
                 }
             }
         }
 
     }
 
     @Override
     public void mouseDragged(final MouseEvent arg0) {
         // Not used.
     }
 
     @Override
     public void mouseEntered(final MouseEvent e) {
         // Not used.
     }
 
     @Override
     public void mouseExited(final MouseEvent e) {
         // Not used.
     }
 
     @Override
     public void mouseMoved(final MouseEvent e) {
         // Not used.
     }
 
     @Override
     public void mousePressed(final MouseEvent e) {
         // Not used.
     }
 
     @Override
     public void mouseReleased(final MouseEvent e) {
         // Not used.
     }
 
     /**
      * Removes one of the ElementsSelectedListener.
      * @param listener
      *            the listener
      */
     public final void removeElementsSelectedListener(
             final ElementsSelectedListener listener) {
         this.listeners.remove(ElementsSelectedListener.class, listener);
     }
 
     /**
      * Change the appearance of the surfaceView parameter : select or unselect
      * the corresponding surface.
      * @param surfaceView
      *            The surfaceView containing the surface to select
      */
     public final void selectOrUnselectSurface(final SurfaceView surfaceView) {
         Surface surface = surfaceView.getSurface();
 
         // Surface not selected when clicked
         if (!this.surfacesSelected.contains(surface)) {
             this.surfacesSelected.add(surface);
             surfaceView.setMaterial(SurfaceView.MATERIAL_SELECTED);
             fireSurfaceSelected(surface);
             showNeighbours(surface);
 
         } else {
 
             // Surface already selected when clicked
             this.surfacesSelected.remove(surface);
             if (surfaceView.getPolygonView() != null) {
                 surfaceView.setMaterial(SurfaceView.MATERIAL_POLYGON);
             } else {
                 surfaceView.setMaterial(SurfaceView.MATERIAL_NON_POLYGON);
             }
             unshowNeighbours(surface);
 
             fireSurfaceDeselected(surface);
         }
     }
 
     /**
      * When a surface is clicked in the JTree, selects it if it was not
      * selected, deselects it if it was selected.
      * @param surface
      *            the surface clicked
      */
     public final void selectOrUnselectSurfaceFromTree(final Surface surface) {
         SurfaceView surfaceView = this.getSurfaceViewFromSurface(surface);
 
         this.selectOrUnselectSurface(surfaceView);
     }
 
     /**
      * Selects a set of triangles.
      * @param meshViewPicked
      *            The meshiew giving the possibility to modify the triangles
      *            appearance (change the texture).
      * @param meshToSelect
      *            The mesh containing the list of triangle to picked.
      */
     public final void selectTriangles(final MeshView meshViewPicked,
             final Mesh meshToSelect) {
         for (Triangle t : meshToSelect) {
             meshViewPicked.select(t);
         }
         this.trianglesSelected.addAll(meshToSelect);
         this.meshesSelected.add(meshToSelect);
     }
 
     /**
      * Setter.
      * @param newDisplayMode
      *            the new display mode
      */
     public final void setDisplayMode(final int newDisplayMode) {
         this.displayMode = newDisplayMode;
     }
 
     /**
      * Sets up the locked mode.
      */
     public final void setLockMode() {
         this.lockMode = LOCK_MODE;
     }
 
     /**
      * Setter.
      * @param mouseRotateIn
      *            the mouse rotate object
      */
     public final void setMouseRotate(final NewMouseRotate mouseRotateIn) {
         this.mouseRotate = mouseRotateIn;
     }
 
     /**
      * Generates a pickCanves linked to the branchGroup in the U3DView.
      * @param branchGroup
      *            the branch group containing all the datas
      */
     public final void setPickCanvas(final BranchGroup branchGroup) {
         this.pickCanvas = new PickCanvas(this.u3DView.getSimpleUniverse()
                 .getCanvas(), branchGroup);
         this.pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
         this.pickCanvas.setTolerance(0.0f);
     }
 
     /**
      * Setter.
      * @param surface
      *            the surface to lock
      */
     public final void setSurfaceLocked(final Surface surface) {
         this.surfaceLocked = surface;
     }
 
     /**
      * Sets up the unlocked mode.
      */
     public final void setUnLockMode() {
         this.lockMode = UNLOCK_MODE;
     }
 
     /**
      * Displays the meshs or the polygons, depending on the display mode.
      */
     public final void showMeshOrPolygon() {
         if (this.displayMode == DISPLAY_MESH_MODE) {
             for (SurfaceView surfaceView : this.u3DView.getSurfaceViewList()) {
                 surfaceView.removeAllGeometries();
                 surfaceView.addGeometry(surfaceView.getMeshView());
             }
         } else {
             for (SurfaceView surfaceView : this.u3DView.getSurfaceViewList()) {
                 surfaceView.removeAllGeometries();
                 surfaceView.addGeometry(surfaceView.getPolygonView());
             }
         }
     }
 
     /**
      * Displays the neighbours.
      * @param surface
      *            the surface which neighbours will be displayed
      */
     public final void showNeighbours(final Surface surface) {
 
         // FIXME by Daniel : look at this.
 
         List<SurfaceView> surfaceViewNeighbours = new ArrayList<>();
         for (Surface surfaceNeighbours : surface.getNeighbours()) {
             for (SurfaceView surfaceViewsDisplayed : this.u3DView
                     .getSurfaceViewList()) {
                 if (this.displayMode == DISPLAY_MESH_MODE) {
                     if (surfaceViewsDisplayed.getSurface().getMesh() == surfaceNeighbours
                             .getMesh()) {
                         surfaceViewNeighbours.add(surfaceViewsDisplayed);
                         break;
                     }
                 } else {
                     if (surfaceViewsDisplayed.getSurface().getPolygon() == surfaceNeighbours
                             .getPolygon()) {
                         surfaceViewNeighbours.add(surfaceViewsDisplayed);
                         break;
                     }
                 }
 
             }
         }
         for (SurfaceView surfaceViewNeighbour : surfaceViewNeighbours) {
             if (!this.surfacesSelected.contains(surfaceViewNeighbour
                     .getSurface())) {
                 surfaceViewNeighbour
                         .setMaterial(SurfaceView.MATERIAL_NEIGHBOUR);
             }
         }
     }
 
     /**
      * Cancel the hide mode of the surface.
      * @param surfacehide
      *            the surface selected to cancel the hide mode.
      */
     public final void showSurface(final Surface surfacehide) {
         SurfaceView surfaceViewHide = this
                 .getSurfaceViewFromSurface(surfacehide);
         if (this.displayMode == DISPLAY_MESH_MODE) {
             surfaceViewHide.addGeometry(surfaceViewHide.getMeshView());
         }
         if (this.displayMode == DISPLAY_POLYGON_MODE) {
             surfaceViewHide.addGeometry(surfaceViewHide.getPolygonView());
         }
     }
 
     /**
      * Treats the surface selection.
      * @param e
      *            The mouse event get from the MouseListener.
      * @param result
      *            the PickResult get from the MouseListener.
      */
     private void treatSurfaceSelection(final MouseEvent e,
             final PickResult result) {
 
         // FIXME : look at this...
 
         SurfaceView surfaceViewPicked = (SurfaceView) result
                 .getNode(PickResult.SHAPE3D);
         Surface surfacePicked = surfaceViewPicked.getSurface();
 
         if (e.isControlDown()) {
             // Control down
             // -> Add or remove the surface to the surfacesSelected list.
             this.selectOrUnselectSurface(surfaceViewPicked);
         } else {
             // If a surface is locked.
             if (this.lockMode == LOCK_MODE) {
                 // If the surfacePicked is not the surface locked.
                 if (this.surfaceLocked != surfacePicked) {
                     // If the surfacePicked is not in the list of the neighbours
                     // of the surface locked, add it to the list.
                     if (!this.surfaceLocked.getNeighbours().contains(
                             surfacePicked)) {
                         this.surfaceLocked.getNeighbours().add(surfacePicked);
                     } else {
                         // If the surfacePicked is in the list of the neighbours
                         // of the surface locked, delete it from the list.
                         this.surfaceLocked.getNeighbours()
                                 .remove(surfacePicked);
                     }
                 }
             } else {
                 // Control up
                 // -> Unselect all the selected surfaces except the picked one.
                 if (this.surfacesSelected.contains(surfacePicked)
                         && this.surfacesSelected.size() == 1) {
                     this.selectOrUnselectSurface(surfaceViewPicked);
                 } else {
                     this.deselectEverySurfaces();
                     this.selectOrUnselectSurface(surfaceViewPicked);
                 }
             }
 
         }
     }
 
     /**
      * Treats the triangle selection when the user clicks on the mesh and if the
      * selection mode is set to triangle.
      * @param e
      *            the mouse event get from the MouseListener.
      * @param result
      *            the PickResult get from the MouseListener.
      */
     private void treatTriangleSelection(final MouseEvent e,
             final PickResult result) {
 
         PickIntersection pickIntersection = result.getIntersection(0);
 
         // Gets the meshView picked.
         MeshView meshView = (MeshView) pickIntersection.getGeometryArray();
         // Gets the the triangle picked.
         int[] pointIndex = pickIntersection.getPrimitiveVertexIndices();
 
         Triangle trianglePicked = meshView
                 .getTriangleFromArrayPosition(pointIndex[0]
                         / MeshView.TRIANGLE_POINTS_COUNT);
 
         // Computes the neighbours of the triangle picked.
         Mesh oriented = meshView.getMesh().orientedAs(
                 trianglePicked.getNormal(), ORIENTATION_TOLERANCE);
         Mesh neighbours = new Mesh();
         trianglePicked.returnNeighbours(neighbours, oriented);
 
         if (e.isControlDown()) {
             if (this.trianglesSelected.contains(trianglePicked)) {
                 // Control down and triangle picked selected before click
                 // -> Unselect the area picked
 
                 Mesh meshToRemove = getMeshSelected(trianglePicked);
                 this.unSelectTriangles(meshToRemove);
             } else {
                 // Control down and triangle picked not selected before click
                 // -> select the area picked
 
                 this.selectTriangles(meshView, neighbours);
             }
         } else if (!e.isControlDown()) {
             if (this.trianglesSelected.contains(trianglePicked)) {
                 // Control up and triangle picked selected before click
                 // -> unselect the other areas
 
                 if (this.meshesSelected.size() == 1) {
                     this.unSelectTriangles(this.meshesSelected.get(0));
                 } else {
                     this.unSelectTriangles(this
                             .getComplementaryMeshesSelected(trianglePicked));
                 }
             } else {
                 // Control up and triangle picked not selected before click
                 // -> unselect the other areas and select the area picked
                 this.unSelectTriangles(this
                         .getComplementaryMeshesSelected(trianglePicked));
                 this.selectTriangles(meshView, neighbours);
             }
 
         }
         fireNewTrianglesSelection();
     }
 
     /**
      * Unselects a list of meshes containing triangles.
      * @param meshesToUnSelect
      *            A list of meshes containing triangles to unselect.
      */
     public final void unSelectTriangles(final List<Mesh> meshesToUnSelect) {
         for (Mesh m : meshesToUnSelect) {
             this.unSelectTriangles(m);
         }
         fireNewTrianglesSelection();
     }
 
     /**
      * Unselects a set of triangles.
      * @param meshToUnselect
      *            The mesh containing the triangles to unselect.
      */
     public final void unSelectTriangles(final Mesh meshToUnselect) {
 
         // get the meshView containing the triangles to unselect to be able to
         // modify their appearance.
         MeshView mV = this.getMeshViewFromTriangle(meshToUnselect.getOne());
 
         if (mV != null) {
             for (Triangle t : meshToUnselect) {
                 mV.unSelect(t);
             }
         }
         this.trianglesSelected.removeAll(meshToUnselect);
         this.meshesSelected.remove(meshToUnselect);
     }
 
     /**
      * TODO .
      * @param surface
      *            TODO .
      */
     public final void unshowNeighbours(final Surface surface) {
         List<SurfaceView> surfaceViewNeighbours = new ArrayList<>();
         for (Surface surfaceNeighbours : surface.getNeighbours()) {
             for (SurfaceView surfaceViewsDisplayed : this.u3DView
                     .getSurfaceViewList()) {
                 if (this.displayMode == DISPLAY_MESH_MODE) {
                     if (surfaceViewsDisplayed.getSurface().getMesh() == surfaceNeighbours
                             .getMesh()) {
                         surfaceViewNeighbours.add(surfaceViewsDisplayed);
                         break;
                     }
                 } else {
                     if (surfaceViewsDisplayed.getSurface().getPolygon() == surfaceNeighbours
                             .getPolygon()) {
                         surfaceViewNeighbours.add(surfaceViewsDisplayed);
                         break;
                     }
                 }
 
             }
         }
         for (SurfaceView surfaceViewNeighbour : surfaceViewNeighbours) {
             if (surfaceViewNeighbour.getPolygonView() != null) {
                 if (!this.surfacesSelected.contains(surfaceViewNeighbour
                         .getSurface())) {
                     surfaceViewNeighbour
                             .setMaterial(SurfaceView.MATERIAL_POLYGON);
                 }
 
             } else {
                 if (!this.surfacesSelected.contains(surfaceViewNeighbour
                         .getSurface())) {
                     surfaceViewNeighbour
                             .setMaterial(SurfaceView.MATERIAL_NON_POLYGON);
                 }
 
             }
         }
     }
 }
