 package net.untoldwind.moredread.ui.canvas;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import net.untoldwind.moredread.model.renderer.SolidNodeRenderer;
 import net.untoldwind.moredread.model.renderer.SolidNodeRendererParam;
 import net.untoldwind.moredread.model.scene.INode;
 import net.untoldwind.moredread.model.scene.ISceneHolder;
 import net.untoldwind.moredread.model.scene.SpatialNodeReference;
 import net.untoldwind.moredread.ui.MoreDreadUI;
 import net.untoldwind.moredread.ui.controls.IControlHandle;
 import net.untoldwind.moredread.ui.controls.IModelControl;
 import net.untoldwind.moredread.ui.controls.Modifier;
 import net.untoldwind.moredread.ui.preferences.IPreferencesConstants;
 import net.untoldwind.moredread.ui.tools.IDisplaySystem;
 import net.untoldwind.moredread.ui.tools.IToolDescriptor;
 
 import com.jme.input.FirstPersonHandler;
 import com.jme.input.InputHandler;
 import com.jme.intersection.PickData;
 import com.jme.intersection.PickResults;
 import com.jme.intersection.TrianglePickResults;
 import com.jme.light.DirectionalLight;
 import com.jme.math.FastMath;
 import com.jme.math.Quaternion;
 import com.jme.math.Ray;
 import com.jme.math.Vector2f;
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.scene.Node;
 import com.jme.scene.state.BlendState;
 import com.jme.scene.state.LightState;
 import com.jme.scene.state.MaterialState;
 import com.jme.scene.state.ZBufferState;
 import com.jme.scene.state.BlendState.BlendEquation;
 import com.jme.scene.state.MaterialState.ColorMaterial;
 import com.jme.scene.state.MaterialState.MaterialFace;
 import com.jme.system.canvas.SimpleCanvasImpl;
 import com.jme.util.GameTaskQueue;
 import com.jme.util.GameTaskQueueManager;
 
 public class MDCanvasImplementor extends SimpleCanvasImpl implements
 		IDisplaySystem {
 
 	private Quaternion rotQuat;
 	private Vector3f axis;
 	long startTime = 0;
 	long fps = 0;
 	private InputHandler input;
 
 	Node controlsNode;
 	Node sceneNode;
 	Node backdropNode;
 
 	GridBackdrop gridBackdrop;
 
 	private List<IControlHandle> controlHandles;
 	private final List<IModelControl> modelControls = new ArrayList<IModelControl>();
 	private IControlHandle activeControlHandle;
 
 	private final ISceneHolder sceneHolder;
 
 	public MDCanvasImplementor(final int width, final int height,
 			final ISceneHolder sceneHolder) {
 		super(width, height);
 
 		this.sceneHolder = sceneHolder;
 	}
 
 	@Override
 	public void simpleSetup() {
 		controlsNode = new Node("controlsNode");
 		sceneNode = new Node("sceneNode");
 		backdropNode = new Node("backdropNode");
 
 		rootNode.attachChild(sceneNode);
 		rootNode.attachChild(backdropNode);
 
 		final DirectionalLight light = new DirectionalLight();
 		light.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
 		light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
 		light.setDirection(new Vector3f(-1, -1, -1));
 		light.setEnabled(true);
 
 		/** Attach the light to a lightState and the lightState to rootNode. */
 		final LightState lightState = renderer.createLightState();
 		lightState.setEnabled(true);
 		lightState.attach(light);
 		rootNode.setRenderState(lightState);
 
 		final MaterialState materialState = renderer.createMaterialState();
 		materialState.setMaterialFace(MaterialFace.FrontAndBack);
 		materialState.setColorMaterial(ColorMaterial.AmbientAndDiffuse);
 		materialState.setEnabled(true);
 		final BlendState sceneBlendState = renderer.createBlendState();
 		sceneBlendState.setEnabled(true);
 		sceneBlendState.setBlendEnabled(true);
 		sceneBlendState
 				.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
 		sceneBlendState
 				.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
 
 		rootNode.setRenderState(materialState);
 		rootNode.setRenderState(sceneBlendState);
 
 		final ZBufferState zBufferState = renderer.createZBufferState();
 		zBufferState.setEnabled(false);
 		final BlendState blendState = renderer.createBlendState();
 		blendState.setEnabled(true);
 		blendState.setBlendEnabled(true);
 		blendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
 		blendState
 				.setDestinationFunction(BlendState.DestinationFunction.DestinationAlpha);
 		blendState.setBlendEquation(BlendEquation.Subtract);
 
 		controlsNode.setRenderState(blendState);
 		controlsNode.setRenderState(zBufferState);
 
 		// Normal Scene setup stuff...
 		rotQuat = new Quaternion();
 		axis = new Vector3f(1, 1, 0.5f);
 		axis.normalizeLocal();
 
 		final SolidNodeRendererParam rendererParam = new SolidNodeRendererParam(
 				MoreDreadUI
 						.getDefault()
 						.getPreferenceStore()
 						.getBoolean(
 								IPreferencesConstants.MODEL3DVIEW_SELECTED_SHOW_NORMALS));
 		sceneHolder.getScene().updateDisplayNode(
 				new SolidNodeRenderer(renderer, sceneHolder.getSelectionMode(),
 						rendererParam), sceneNode);
 
 		startTime = System.currentTimeMillis() + 5000;
 
 		input = new FirstPersonHandler(cam, 50, 1);
 
 		controlsNode.updateGeometricState(0, true);
 		controlsNode.updateRenderState();
 
 		updateToolControls();
 
 		gridBackdrop = new GridBackdrop(sceneHolder.getScene()
 				.getWorldBoundingBox(), renderer.getCamera());
 		backdropNode.attachChild(gridBackdrop);
 
 		backdropNode.updateGeometricState(0, true);
 		backdropNode.updateRenderState();
 	}
 
 	public void updateDisplayNodes() {
 
 		final SolidNodeRendererParam rendererParam = new SolidNodeRendererParam(
 				MoreDreadUI
 						.getDefault()
 						.getPreferenceStore()
 						.getBoolean(
 								IPreferencesConstants.MODEL3DVIEW_SELECTED_SHOW_NORMALS));
 		sceneHolder.getScene().updateDisplayNode(
 				new SolidNodeRenderer(renderer, sceneHolder.getSelectionMode(),
 						rendererParam), sceneNode);
 		rootNode.updateGeometricState(0.0f, true);
 		rootNode.updateRenderState();
 
 		if (controlsNode.getChildren() != null) {
 			for (final IModelControl modelControl : modelControls) {
 				modelControl.updatePositions();
 			}
 			controlsNode.updateGeometricState(tpf, true);
 			controlsNode.updateWorldBound();
 		}
 
 	}
 
 	@Override
 	public void simpleUpdate() {
 		input.update(0.05f);
 
 		for (final IModelControl modelControl : modelControls) {
 			modelControl.cameraUpdated(renderer.getCamera());
 		}
 
 		controlsNode.updateGeometricState(tpf, true);
 		controlsNode.updateWorldBound();
 
 		rotQuat.fromAngleNormalAxis(1 * FastMath.DEG_TO_RAD, axis);
 
 		gridBackdrop.updateGrid(sceneHolder.getScene().getWorldBoundingBox(),
 				renderer.getCamera());
 		backdropNode.updateGeometricState(0, true);
 		backdropNode.updateRenderState();
 	}
 
 	@Override
 	public void simpleRender() {
 		renderer.renderQueue();
 		renderer.clearZBuffer();
 		renderer.draw(controlsNode);
 	}
 
 	@Override
 	public void resizeCanvas(final int newWidth, final int newHeight) {
 		final int fWidth = newWidth <= 0 ? 1 : newWidth;
 		final int fHeight = newHeight <= 0 ? 1 : newHeight;
 		final Callable<?> exe = new Callable<Object>() {
 			public Object call() {
 				if (renderer != null) {
 					height = fHeight;
 					width = fWidth;
 
 					renderer.getCamera().setFrustumPerspective(45.0f,
 							(float) fWidth / (float) fHeight, 1, 1000);
 					renderer.getCamera().apply();
 					renderer.reinit(fWidth, fHeight);
 				}
 				return null;
 			}
 		};
 		GameTaskQueueManager.getManager().getQueue(GameTaskQueue.UPDATE)
 				.enqueue(exe);
 	}
 
 	@Override
 	public INode pickNode(final Vector2f screenCoord) {
 		final PickResults results = new TrianglePickResults();
		final Ray ray = new Ray();
 
		renderer.getCamera().getWorldCoordinates(screenCoord, 0, ray.origin);
		renderer.getCamera().getWorldCoordinates(screenCoord, 0.3f,
				ray.direction).subtractLocal(ray.origin).normalizeLocal();

		results.setCheckDistance(true);
 		sceneNode.findPick(ray, results);
 
 		for (int i = 0; i < results.getNumber(); i++) {
 			final PickData pick = results.getPickData(i);
 			final SpatialNodeReference userData = (SpatialNodeReference) pick
 					.getTargetMesh()
 					.getUserData(ISceneHolder.NODE_USERDATA_KEY);
 
 			if (userData != null) {
 				return userData.getNode();
 			}
 		}
 		return null;
 	}
 
 	public void updateToolControls() {
 		controlHandles = null;
 		controlsNode.detachAllChildren();
 		modelControls.clear();
 
 		for (final IToolDescriptor tool : MoreDreadUI.getDefault()
 				.getActiveTools()) {
 			final List<? extends IModelControl> toolControls = tool
 					.getModelControls(sceneHolder.getScene(), this);
 
 			if (toolControls != null) {
 				modelControls.addAll(toolControls);
 			}
 		}
 		for (final IModelControl modelControl : modelControls) {
 			if (modelControl.getSpatial() != null) {
 				controlsNode.attachChild(modelControl.getSpatial());
 			}
 		}
 
 		controlsNode.updateGeometricState(0, true);
 		controlsNode.updateRenderState();
 	}
 
 	public void handleClick(final int x, final int y,
 			final EnumSet<Modifier> modifiers) {
 		if (activeControlHandle != null) {
 			activeControlHandle.handleClick(new Vector2f(x, height - y),
 					modifiers);
 		}
 
 	}
 
 	public void handleDrag(final int startX, final int startY, final int endX,
 			final int endY, final EnumSet<Modifier> modifiers,
 			final boolean finnished) {
 		if (activeControlHandle != null) {
 			activeControlHandle.handleDrag(
 					new Vector2f(startX, height - startY), new Vector2f(endX,
 							height - endY), modifiers, finnished);
 		}
 	}
 
 	public boolean findControl(final int x, final int y) {
 		IControlHandle foundHandle = null;
 		float minSalience = Float.MAX_VALUE;
 		final Vector2f screenCoord = new Vector2f(x, height - y);
 		for (final IControlHandle controlHandle : getControlHandles()) {
 			final float salience = controlHandle.matches(screenCoord);
 			if (salience >= 0 && salience < minSalience) {
 				minSalience = salience;
 				foundHandle = controlHandle;
 			}
 		}
 
 		if (foundHandle != activeControlHandle) {
 			if (activeControlHandle != null) {
 				activeControlHandle.setActive(false);
 			}
 			activeControlHandle = foundHandle;
 			if (activeControlHandle != null) {
 				activeControlHandle.setActive(true);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	private List<IControlHandle> getControlHandles() {
 		if (controlHandles == null) {
 			final List<IControlHandle> newControlHandles = new ArrayList<IControlHandle>();
 			for (final IModelControl modelControl : modelControls) {
 				modelControl.collectControlHandles(newControlHandles, renderer
 						.getCamera());
 			}
 
 			controlHandles = newControlHandles;
 		}
 		return controlHandles;
 	}
 }
