 /**
 * Copyright (C) 2010  David Monta�o
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package seeit3d.manager;
 
 import java.util.Enumeration;
 import java.util.Iterator;
 
 import javax.media.j3d.*;
 import javax.vecmath.*;
 
 import seeit3d.behavior.*;
 import seeit3d.colorscale.IColorScale;
 import seeit3d.error.exception.SeeIT3DException;
 import seeit3d.model.representation.Container;
 import seeit3d.preferences.IPreferencesListener;
 import seeit3d.utils.Utils;
 import seeit3d.utils.ViewConstants;
 import seeit3d.view.SeeIT3DCanvas;
 
 import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
 import com.sun.j3d.utils.picking.behaviors.PickMouseBehavior;
 import com.sun.j3d.utils.universe.SimpleUniverse;
 import com.sun.j3d.utils.universe.ViewingPlatform;
 
 /**
  * Class that handles all the interactions with the scene graph in Java 3D. All the changes in general structure of the scene graph should be handle by this class.
  * 
 * @author David Monta�o
  * 
  */
 public class SceneGraphHandler implements IPreferencesListener {
 
 	private final SeeIT3DManager manager;
 
 	private SeeIT3DCanvas canvas = null;
 
 	private BranchGroup rootObj = null;
 
 	private SimpleUniverse universe = null;
 
 	private Bounds bounds = null;
 
 	private BranchGroup containersGroup = null;
 
 	private OrbitBehavior orbit = null;
 
 	private Color3f backgroundColor;
 
 	public SceneGraphHandler(SeeIT3DManager manager) {
 		this.manager = manager;
 	}
 
 	public void addChildToGroup(BranchGroup branchGroup) {
 		containersGroup.addChild(branchGroup);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void clearScene() {
 		Enumeration<Node> children = containersGroup.getAllChildren();
 		while (children.hasMoreElements()) {
 			Node node = children.nextElement();
 			if (node instanceof BranchGroup) {
 				((BranchGroup) node).detach();
 				containersGroup.removeChild(node);
 			}
 		}
 	}
 
 	public synchronized void enableOrbiting() {
 		changeOrbitState(true);
 	}
 
 	public synchronized void disableOrbiting() {
 		changeOrbitState(false);
 	}
 
 	private synchronized void changeOrbitState(boolean enabled) {
 		if (orbit != null) {
 			orbit.setRotateEnable(enabled);
 			orbit.setTranslateEnable(enabled);
 		}
 	}
 
 	public synchronized void initializeVisualization(SeeIT3DCanvas canvasToSet) {
 		canvas = canvasToSet;
 		universe = new SimpleUniverse(canvas);
 
 		canvas.getView().setFrontClipPolicy(View.VIRTUAL_EYE);
 		canvas.getView().setBackClipPolicy(View.VIRTUAL_EYE);
 		canvas.getView().setFrontClipDistance(0.1f);
 		canvas.getView().setBackClipDistance(100.0f);
		canvas.getView().setTransparencySortingPolicy(View.TRANSPARENCY_SORT_GEOMETRY);
 
 		buildAllVisualization();
 
 	}
 
 	public synchronized void buildAllVisualization() {
 		if (rootObj != null) {
 			universe.getLocale().removeBranchGraph(rootObj);
 		}
 
 		rootObj = new BranchGroup();
 		rootObj.setCapability(BranchGroup.ALLOW_DETACH);
 		rootObj.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 		rootObj.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
 
 		buildWorld(canvas);
 		buildGraph();
 		setViewersPosition(20);
 		Utils.buildAxis(rootObj);
 
 		rootObj.compile();
 		universe.addBranchGraph(rootObj);
 	}
 
 	public synchronized void removeFromSceneGraph(Container container) {
 		BranchGroup containerBG = container.getContainerBG();
 		containerBG.detach();
 		containersGroup.removeChild(containerBG);
 	}
 
 	private synchronized void buildWorld(Canvas3D canvas) {
 
 		ViewingPlatform viewingPlatform = universe.getViewingPlatform();
 
 		bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 2000.0f);
 		rootObj.setBounds(bounds);
 
 		/************* LIGHT ****************/
 		AmbientLight ambientLightNode = new AmbientLight(ViewConstants.BLACK);
 		ambientLightNode.setInfluencingBounds(bounds);
 		rootObj.addChild(ambientLightNode);
 
 		DirectionalLight dLight = new DirectionalLight(true, ViewConstants.WHITE, new Vector3f(-1.0f, 1.0f, 1.0f));
 		dLight.setInfluencingBounds(bounds);
 		rootObj.addChild(dLight);
 
 		DirectionalLight dLight1 = new DirectionalLight(true, ViewConstants.WHITE, new Vector3f(1.0f, 1.0f, 1.0f));
 		dLight1.setInfluencingBounds(bounds);
 		rootObj.addChild(dLight1);
 
 		DirectionalLight dLight2 = new DirectionalLight(true, ViewConstants.WHITE, new Vector3f(-1.0f, 1.0f, -1.0f));
 		dLight2.setInfluencingBounds(bounds);
 		rootObj.addChild(dLight2);
 
 		DirectionalLight dLight3 = new DirectionalLight(true, ViewConstants.WHITE, new Vector3f(1.0f, 1.0f, -1.0f));
 		dLight3.setInfluencingBounds(bounds);
 		rootObj.addChild(dLight3);
 
 		DirectionalLight dLight4 = new DirectionalLight(true, ViewConstants.WHITE, new Vector3f(0.0f, -1.0f, 0.0f));
 		dLight4.setInfluencingBounds(bounds);
 		rootObj.addChild(dLight4);
 
 		/************* BACKGROUND ****************/
 		Background back = new Background(backgroundColor);
 		back.setApplicationBounds(bounds);
 		rootObj.addChild(back);
 
 		/******* BEHAVIORS ********/
 
 		PickRotate3DBehavior rotate = new PickRotate3DBehavior(canvas, rootObj, bounds, viewingPlatform);
 		rotate.setTolerance(ViewConstants.PICKING_TOLERANCE);
 		rootObj.addChild(rotate);
 
 		PickMouseBehavior selectionBehavior = new MouseClickedBehavior(canvas, rootObj, bounds);
 		selectionBehavior.setTolerance(ViewConstants.PICKING_TOLERANCE);
 		rootObj.addChild(selectionBehavior);
 
 		PickTranslate3DBehavior translation = new PickTranslate3DBehavior(canvas, rootObj, bounds, viewingPlatform);
 		translation.setTolerance(ViewConstants.PICKING_TOLERANCE);
 		rootObj.addChild(translation);
 
 		orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ROTATE | OrbitBehavior.REVERSE_TRANSLATE | OrbitBehavior.STOP_ZOOM);
 		orbit.setSchedulingBounds(bounds);
 		viewingPlatform.setViewPlatformBehavior(orbit);
 
 	}
 
 	private synchronized void buildGraph() throws SeeIT3DException {
 		Iterator<Container> iterator = manager.iteratorOnAllContainers();
 
 		containersGroup = new BranchGroup();
 		containersGroup.setCapability(BranchGroup.ALLOW_DETACH);
 		containersGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
 		containersGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 		containersGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
 
 		TransformGroup containersTG = new TransformGroup();
 		containersTG.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
 		containersTG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 		containersTG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
 		containersGroup.addChild(containersTG);
 
 		Container container = null;
 		while (iterator.hasNext()) {
 			container = iterator.next();
 			container.updateVisualRepresentation();
 			BranchGroup bgContainer = container.getContainerBG();
 			containersTG.addChild(bgContainer);
 		}
 		rootObj.addChild(containersGroup);
 
 	}
 
 	public synchronized void rebuildSceneGraph() {
 		if (containersGroup != null) {
 			containersGroup.detach();
 			rootObj.removeChild(containersGroup);
 			buildGraph();
 		}
 	}
 
 	public TransformGroup getViewPlatformTransform() {
 		return universe.getViewingPlatform().getViewPlatformTransform();
 	}
 
 	public void setViewersPosition(float max) {
 		float xMax = Math.max(max, 5);
 		ViewingPlatform vp = universe.getViewingPlatform();
 		TransformGroup tg = vp.getViewPlatformTransform();
 		Transform3D t3d = new Transform3D();
 		tg.getTransform(t3d);
 		t3d.lookAt(new Point3d(xMax * 2, xMax, xMax * 2), new Point3d(0, 0, 0), new Vector3d(0, 1, 0));
 		t3d.invert();
 		tg.setTransform(t3d);
 	}
 
 	/**
 	 * Listen for preferences
 	 */
 	@Override
 	public void backgroundColorChanged(Color3f newBackgroundColor) {
 		this.backgroundColor = newBackgroundColor;
 	}
 
 	@Override
 	public void scaleStepChanged(double newScale) {}
 
 	@Override
 	public void colorScaleChanged(IColorScale newColorScale) {}
 
 	@Override
 	public void containersPerRowChanged(int containersPerRow) {}
 
 	@Override
 	public void relationMarkColorChanged(Color3f newRelationMarkColor) {}
 
 	@Override
 	public void highlightColorChanged(Color3f newHighlightColor) {}
 
 	@Override
 	public void polycylindersPerRowChanged(int newPolycylinderPerRow) {}
 
 	@Override
 	public void transparencyStepChanged(float transparencyStepChanged) {}
 
 }
