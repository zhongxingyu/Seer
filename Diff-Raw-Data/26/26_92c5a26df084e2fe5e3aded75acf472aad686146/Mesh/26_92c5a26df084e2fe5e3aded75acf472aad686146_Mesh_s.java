 package net.untoldwind.moredread.model.mesh;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.untoldwind.moredread.model.enums.GeometryType;
 import net.untoldwind.moredread.model.enums.MeshType;
 import net.untoldwind.moredread.model.state.IStateHolder;
 import net.untoldwind.moredread.model.state.IStateReader;
 import net.untoldwind.moredread.model.state.IStateWriter;
 
 import com.jme.math.Vector3f;
 
 public abstract class Mesh<FaceK extends FaceId, FaceT extends Face<?, ?>>
 		implements IMesh, IStateHolder {
 	protected int vertexCount = 0;
 	protected final List<Vertex> vertices;
 	protected final Map<EdgeId, Edge> edges;
 	protected final Map<FaceK, FaceT> faces;
 	protected boolean dirty = false;
 
 	protected Mesh() {
 		vertices = new ArrayList<Vertex>();
 		edges = new HashMap<EdgeId, Edge>();
 		faces = new LinkedHashMap<FaceK, FaceT>();
 	}
 
 	@Override
 	public GeometryType getGeometryType() {
 		return GeometryType.MESH;
 	}
 
 	public Vertex addVertex(final Vector3f point) {
 		return addVertex(point, false);
 	}
 
 	public Vertex addVertex(final Vector3f point, final boolean smooth) {
 		final Vertex vertex = new Vertex(this, vertices.size(), point);
 
 		vertices.add(vertex);
 
 		return vertex;
 	}
 
 	public List<Vertex> getVertices() {
 		return vertices;
 	}
 
 	public Vertex getVertex(final int vertexIndes) {
 		return vertices.get(vertexIndes);
 	}
 
 	public Edge getEdge(final EdgeId edgeIndex) {
 		return edges.get(edgeIndex);
 	}
 
 	protected Edge addEdge(final Vertex vertex1, final Vertex vertex2) {
 		Edge edge = edges
 				.get(new EdgeId(vertex1.getIndex(), vertex2.getIndex()));
 
 		if (edge == null) {
 			edge = new Edge(this, vertex1, vertex2);
 
 			edges.put(edge.getIndex(), edge);
 		}
 
 		return edge;
 	}
 
 	public Collection<Edge> getEdges() {
 		return edges.values();
 	}
 
 	@Override
 	public Collection<FaceT> getFaces() {
 		return faces.values();
 	}
 
 	public FaceT getFace(final FaceId faceIndex) {
 		return faces.get(faceIndex);
 	}
 
 	void markDirty() {
 		dirty = true;
 	}
 
 	public boolean clearDirty() {
 		if (dirty) {
 			dirty = false;
 			return true;
 		}
 		return false;
 	}
 
 	public abstract MeshType getMeshType();
 
 	public abstract TriangleMesh toTriangleMesh();
 
 	@Override
 	public void writeState(final IStateWriter writer) throws IOException {
 		writer.writeInt("type", getMeshType().getCode());
 		writer.writeCollection("vertices", vertices);
 		writer.writeCollection("faces", faces.values());
 	}
 
 	public class VertexInstanceCreator implements
 			IStateReader.InstanceCreator<Vertex> {
 
 		@Override
 		public Vertex createInstance(final IStateReader reader)
 				throws IOException {
 			final boolean smooth = reader.readBoolean();
 			final Vector3f point = reader.readVector3f();
 
 			return addVertex(point, smooth);
 		}
 	}
 
 	public static class MeshInstanceCreator implements
 			IStateReader.InstanceCreator<Mesh<?, ?>> {
 
 		@Override
 		public Mesh<?, ?> createInstance(final IStateReader reader)
 				throws IOException {
 			final MeshType meshType = MeshType.forCode(reader.readInt());
 
 			switch (meshType) {
 			case TRIANGLE:
 				final TriangleMesh triangleMesh = new TriangleMesh();
 				triangleMesh.vertices
 						.addAll(reader
 								.readCollection(triangleMesh.new VertexInstanceCreator()));
 				reader.readCollection(triangleMesh.new FaceInstanceCreator());
 				return triangleMesh;
 			case QUAD:
 				final QuadMesh quadMesh = new QuadMesh();
 				quadMesh.vertices.addAll(reader
 						.readCollection(quadMesh.new VertexInstanceCreator()));
 				reader.readCollection(quadMesh.new FaceInstanceCreator());
 				return quadMesh;
 			case POLY:
 				final PolyMesh polyMesh = new PolyMesh();
 				polyMesh.vertices.addAll(reader
 						.readCollection(polyMesh.new VertexInstanceCreator()));
 				reader.readCollection(polyMesh.new FaceInstanceCreator());
 				return polyMesh;
 			default:
 				throw new RuntimeException("Invalid mesh type: " + meshType);
 			}
 		}
 
 	}
 }
