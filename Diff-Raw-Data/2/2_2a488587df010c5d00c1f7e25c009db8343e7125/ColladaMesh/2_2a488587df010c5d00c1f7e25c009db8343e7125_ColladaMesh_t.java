 package com.ntraft.modlur.collada;
 
 import com.ntraft.modlur.Geometry;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public final class ColladaMesh {
 
 	private static final int[] READ_BUF = new int[1024];
 
 	private String id;
 	private int[] upAxis;
 	private final Map<String, Source> sources = new HashMap<String, Source>();
 	private final Map<String, Vertices> vertices = new HashMap<String, Vertices>();
 	private final List<ColladaPrimitive> primitives = new ArrayList<ColladaPrimitive>();
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public int[] getUpAxis() {
 		return upAxis;
 	}
 
 	public void setUpAxis(int[] upAxis) {
 		this.upAxis = upAxis;
 	}
 
 	public void addSource(Source source) {
 		sources.put(source.getId(), source);
 	}
 
 	public void addVertices(Vertices vertexInput) {
 		vertices.put(vertexInput.getId(), vertexInput);
 	}
 
 	public void addPrimitive(ColladaPrimitive primitive) {
 		primitives.add(primitive);
 	}
 
 	public List<Geometry> build() {
 		List<Geometry> built = new ArrayList<Geometry>();
 		for (ColladaPrimitive primitive : primitives) {
 			Map<Semantic, DataSink> dataSinks = primitive.build(sources.keySet(), vertices);
 			FloatBuffer vertices = consume(dataSinks, Semantic.VERTEX);
 			FloatBuffer normals = consume(dataSinks, Semantic.NORMAL);
 			built.add(new Geometry(vertices, normals, primitive.getDrawMode(), upAxis, primitive.getNumVertices()));
 		}
 		return built;
 	}
 
 	private FloatBuffer consume(Map<Semantic, DataSink> dataSinks, Semantic semantic) {
 		DataSink sink = dataSinks.get(semantic);
 		if (sink == null) {
 			return null;
 		}
 
 		Source src = sources.get(sink.getSourceId());
 		if (src == null) {
 			return null;
 		}
 
 		IntBuffer indices = sink.getIndices();
 		int remaining = indices.remaining();
 
 		ByteBuffer bb = ByteBuffer.allocateDirect(remaining * src.getStride() * 4);
 		bb.order(ByteOrder.nativeOrder());
 		FloatBuffer dest = bb.asFloatBuffer();
 
 		int numRead;
 		while (remaining > 0) {
 			numRead = Math.min(remaining, READ_BUF.length);
 			indices.get(READ_BUF, 0, numRead);
 			for (int i = 0; i < numRead; i++) {
				src.get(dest, READ_BUF[i]);
 			}
 			remaining = indices.remaining();
 		}
 
 		dest.rewind();
 		return dest;
 	}
 }
