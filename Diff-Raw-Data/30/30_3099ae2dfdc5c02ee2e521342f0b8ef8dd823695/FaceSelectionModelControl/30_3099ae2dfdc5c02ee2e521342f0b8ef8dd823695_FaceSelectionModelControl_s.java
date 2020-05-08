 package net.untoldwind.moredread.ui.controls.impl;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.untoldwind.moredread.model.mesh.IMesh;
 import net.untoldwind.moredread.model.mesh.IPoint;
 import net.untoldwind.moredread.model.mesh.IPolygon;
 import net.untoldwind.moredread.model.op.ITriangulator;
 import net.untoldwind.moredread.model.op.TriangulatorFactory;
 import net.untoldwind.moredread.model.scene.IMeshNode;
 import net.untoldwind.moredread.ui.controls.IControlHandle;
 import net.untoldwind.moredread.ui.controls.IModelControl;
 import net.untoldwind.moredread.ui.tools.spi.IToolAdapter;
 
 import com.jme.math.Vector3f;
 import com.jme.renderer.Camera;
 import com.jme.renderer.ColorRGBA;
 import com.jme.scene.Spatial;
 import com.jme.scene.TriMesh;
 import com.jme.scene.state.BlendState;
 import com.jme.system.DisplaySystem;
 import com.jme.util.geom.BufferUtils;
 
 public class FaceSelectionModelControl extends TriMesh implements IModelControl {
 
 	private static final long serialVersionUID = 8784417429205599768L;
 
 	private final IToolAdapter toolAdapter;
 
 	private final IMeshNode node;
 	private final int faceIndex;
 
 	private transient PolygonControlHandle polygonControlHandle;
 
 	public FaceSelectionModelControl(final IMeshNode node, final int faceIndex,
 			final IToolAdapter toolAdapter) {
 		this.toolAdapter = toolAdapter;
 		this.node = node;
 		this.faceIndex = faceIndex;
 
 		updateGeometry();
 
 		final BlendState blendState = DisplaySystem.getDisplaySystem()
 				.getRenderer().createBlendState();
 		blendState.setEnabled(true);
 		blendState.setBlendEnabled(true);
 		blendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
 		blendState
 				.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
 
 		setDefaultColor(new ColorRGBA(1, 1, 1, 0.0f));
 		setRenderState(blendState);
 	}
 
 	@Override
 	public void cameraUpdated(final Camera camera) {
 		updateHandle(camera);
 	}
 
 	@Override
 	public void collectControlHandles(final List<IControlHandle> handles,
 			final Camera camera) {
 		updateHandle(camera);
 
 		handles.add(polygonControlHandle);
 	}
 
 	@Override
 	public Spatial getSpatial() {
 		return this;
 	}
 
 	@Override
 	public void setActive(final boolean active) {
 		if (active) {
 			setDefaultColor(new ColorRGBA(1, 1, 1, 0.5f));
 		} else {
 			setDefaultColor(new ColorRGBA(1, 1, 1, 0.0f));
 		}
 	}
 
 	@Override
 	public void updatePositions() {
 		updateGeometry();
 	}
 
 	void updateGeometry() {
 		final IMesh mesh = node.getGeometry();
 		final ITriangulator triangulator = TriangulatorFactory.createDefault();
 		final IPolygon face = node.localToWorld(mesh.getFace(faceIndex));
 		final Vector3f normal = face.getMeanNormal();
 		final List<? extends IPoint> vertices = face.getVertices();
 		final FloatBuffer vertexBuffer = BufferUtils
 				.createVector3Buffer(vertices.size());
 		final FloatBuffer normalBuffer = BufferUtils
 				.createVector3Buffer(vertices.size());
 
 		for (final IPoint vertex : vertices) {
 			final Vector3f point = vertex.getPoint();
 
 			vertexBuffer.put(point.x);
 			vertexBuffer.put(point.y);
 			vertexBuffer.put(point.z);
 			normalBuffer.put(normal.x);
 			normalBuffer.put(normal.y);
 			normalBuffer.put(normal.z);
 		}
 
 		final int[] indices = triangulator.triangulate(face);
 
 		final IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices);
 
 		setVertexBuffer(vertexBuffer);
 		setNormalBuffer(normalBuffer);
 		setIndexBuffer(indexBuffer);
 	}
 
 	void updateHandle(final Camera camera) {
 		final IMesh mesh = node.getGeometry();
 		final IPolygon face = mesh.getFace(faceIndex);
 		final List<Vector3f> points = new ArrayList<Vector3f>();
 
 		for (final IPoint vertex : face.getVertices()) {
 			points.add(node.localToWorld(vertex.getPoint(), new Vector3f()));
 		}
 
 		if (polygonControlHandle == null) {
 			polygonControlHandle = new PolygonControlHandle(this, toolAdapter,
 					camera, points);
 		} else {
 			polygonControlHandle.update(camera, points);
 		}
 
 	}
 }
