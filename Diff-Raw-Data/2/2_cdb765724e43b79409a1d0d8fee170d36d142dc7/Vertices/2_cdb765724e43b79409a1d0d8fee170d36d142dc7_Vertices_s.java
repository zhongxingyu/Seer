 package com.blarg.gdx.graphics;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.VertexAttribute;
 import com.badlogic.gdx.graphics.VertexAttributes;
 import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.FloatArray;
 
 /**
  * Wrapper around a {@link FloatArray} that to provide an easy to use non-GPU bound store for vertices. This is
  * intended to provide a convenient API for building up a list of vertices at run-time (where the final size
  * may not be known in advance) in a type-safe manner free from manually calculating attribute offsets, etc.
  *
  * Support for rendering the vertices stored in this object directly is not provided. A VertexArray, VertexBufferObject,
  * Mesh, etc. must be created from these first before it can be rendered.
  */
 public class Vertices {
 	public final VertexAttributes attributes;
 
 	FloatArray array;
 	int numVertices;
 	int strideInFloats;
 
 	int positionOffset;
 	int texCoordOffset;
 	int normalOffset;
 	int colorOffset;
 
 	boolean hasPosition;
 	boolean hasTexCoord;
 	boolean hasNormal;
 	boolean hasColor;
 
 	int currentPosition;
 
 	public Vertices(int numVertices, VertexAttribute... attributes) {
 		this.attributes = new VertexAttributes(attributes);
 		this.numVertices = numVertices;
 		calculateOffsetsAndSizes();
 		array = new FloatArray(numVertices * strideInFloats);
 		currentPosition = 0;
 	}
 
 	public Vertices(int numVertices, VertexAttributes attributes) {
 		this.attributes = attributes;
 		this.numVertices = numVertices;
 		calculateOffsetsAndSizes();
 		array = new FloatArray(numVertices * strideInFloats);
 		currentPosition = 0;
 	}
 
 	private void calculateOffsetsAndSizes() {
 		strideInFloats = attributes.vertexSize / (Float.SIZE / 8);
 
 		positionOffset = attributes.getOffset(VertexAttributes.Usage.Position);
 		texCoordOffset = attributes.getOffset(VertexAttributes.Usage.TextureCoordinates);
 		normalOffset = attributes.getOffset(VertexAttributes.Usage.Normal);
 		colorOffset = attributes.getOffset(VertexAttributes.Usage.Color);
 
 		hasPosition = attributes.findByUsage(VertexAttributes.Usage.Position) != null;
 		hasTexCoord = attributes.findByUsage(VertexAttributes.Usage.TextureCoordinates) != null;
 		hasNormal = attributes.findByUsage(VertexAttributes.Usage.Normal) != null;
 		hasColor = attributes.findByUsage(VertexAttributes.Usage.Color) != null;
 	}
 
 	public float[] buffer() {
 		return array.items;
 	}
 
 	public int count() {
 		return numVertices;
 	}
 
 	public int stride() {
 		return strideInFloats;
 	}
 
 	public boolean moveNext() {
 		++currentPosition;
 		if (currentPosition >= numVertices) {
 			currentPosition = numVertices - 1;
 			return false;
 		} else
 			return true;
 	}
 
 	public boolean movePrev() {
 		--currentPosition;
 		if (currentPosition < 0) {
 			currentPosition = 0;
 			return false;
 		} else
 			return true;
 	}
 
 	public boolean move(int numVertices) {
 		currentPosition += numVertices;
 		if (currentPosition < 0) {
 			currentPosition = 0;
 			return false;
 		} else if (currentPosition >= numVertices) {
 			currentPosition = numVertices - 1;
 			return false;
 		} else
 			return true;
 	}
 
 	public void moveToStart() {
 		currentPosition = 0;
 	}
 
 	public void moveToEnd() {
 		currentPosition = numVertices - 1;
 	}
 
 	public void moveTo(int index) {
 		currentPosition = index;
 	}
 
 	public void resize(int numVertices) {
 		if (numVertices <= 0)
 			throw new IllegalArgumentException();
 
 		int newCapacity = strideInFloats * numVertices;
 
 		if (numVertices < this.numVertices) {
 			array.truncate(newCapacity);
 		} else if (numVertices > this.numVertices) {
 			int additionalCapacity = newCapacity - array.size;
 			array.ensureCapacity(additionalCapacity);
 		}
 
 		this.numVertices = numVertices;
 		if (currentPosition >= numVertices)
 			currentPosition = numVertices - 1;
 	}
 
 	public void extend(int amount) {
 		int newSize = numVertices + amount;
 		resize(newSize);
 	}
 
 	public int position() {
 		return currentPosition;
 	}
 
 	public int remainingSpace() {
		return (numVertices - 1) - currentPosition;
 	}
 
 	public void getPos(int index, Vector3 out) {
 		int offset = (index * strideInFloats) + positionOffset;
 		out.x = array.items[offset + 0];
 		out.y = array.items[offset + 1];
 		out.z = array.items[offset + 2];
 	}
 
 	public void getPos(Vector3 out) {
 		getPos(currentPosition, out);
 	}
 
 	public void setPos(int index, Vector3 position) {
 		int offset = (index * strideInFloats) + positionOffset;
 		array.items[offset + 0] = position.x;
 		array.items[offset + 1] = position.y;
 		array.items[offset + 2] = position.z;
 	}
 
 	public void setPos(int index, float x, float y, float z) {
 		int offset = (index * strideInFloats) + positionOffset;
 		array.items[offset + 0] = x;
 		array.items[offset + 1] = y;
 		array.items[offset + 2] = z;
 	}
 
 	public void setPos(Vector3 position) {
 		setPos(currentPosition, position);
 	}
 
 	public void setPos(float x, float y, float z) {
 		setPos(currentPosition, x, y, z);
 	}
 
 	public void getUV(int index, Vector2 out) {
 		int offset = (index * strideInFloats) + texCoordOffset;
 		out.x = array.items[offset + 0];
 		out.y = array.items[offset + 1];
 	}
 
 	public void getUV(Vector2 out) {
 		getUV(currentPosition, out);
 	}
 
 	public void setUV(int index, Vector2 texCoord) {
 		int offset = (index * strideInFloats) + texCoordOffset;
 		array.items[offset + 0] = texCoord.x;
 		array.items[offset + 1] = texCoord.y;
 	}
 
 	public void setUV(int index, float u, float v) {
 		int offset = (index * strideInFloats) + texCoordOffset;
 		array.items[offset + 0] = u;
 		array.items[offset + 1] = v;
 	}
 
 	public void setUV(Vector2 texCoord) {
 		setUV(currentPosition, texCoord);
 	}
 
 	public void setUV(float u, float v) {
 		setUV(currentPosition, u, v);
 	}
 
 	public void getNor(int index, Vector3 out) {
 		int offset = (index * strideInFloats) + normalOffset;
 		out.x = array.items[offset + 0];
 		out.y = array.items[offset + 1];
 		out.z = array.items[offset + 2];
 	}
 
 	public void getNor(Vector3 out) {
 		getNor(currentPosition, out);
 	}
 
 	public void setNor(int index, Vector3 normal) {
 		int offset = (index * strideInFloats) + normalOffset;
 		array.items[offset + 0] = normal.x;
 		array.items[offset + 1] = normal.y;
 		array.items[offset + 2] = normal.z;
 	}
 
 	public void setNor(int index, float x, float y, float z) {
 		int offset = (index * strideInFloats) + normalOffset;
 		array.items[offset + 0] = x;
 		array.items[offset + 1] = y;
 		array.items[offset + 2] = z;
 	}
 
 	public void setNor(Vector3 normal) {
 		setNor(currentPosition, normal);
 	}
 
 	public void setNor(float x, float y, float z) {
 		setNor(currentPosition, x, y, z);
 	}
 
 	public void getCol(int index, Color out) {
 		int offset = (index * strideInFloats) + colorOffset;
 		out.r = array.items[offset + 0];
 		out.g = array.items[offset + 1];
 		out.b = array.items[offset + 2];
 		out.a = array.items[offset + 3];
 	}
 
 	public void getCol(Color out) {
 		getCol(currentPosition, out);
 	}
 
 	public void setCol(int index, Color color) {
 		int offset = (index * strideInFloats) + colorOffset;
 		array.items[offset + 0] = color.r;
 		array.items[offset + 1] = color.g;
 		array.items[offset + 2] = color.b;
 		array.items[offset + 3] = color.a;
 	}
 
 	public void setCol(int index, float r, float g, float b, float a) {
 		int offset = (index * strideInFloats) + colorOffset;
 		array.items[offset + 0] = r;
 		array.items[offset + 1] = g;
 		array.items[offset + 2] = b;
 		array.items[offset + 3] = a;
 	}
 
 	public void setCol(Color color) {
 		setCol(currentPosition, color);
 	}
 
 	public void setCol(float r, float g, float b, float a) {
 		setCol(currentPosition, r, g, b, a);
 	}
 
 	public void getVertex(int index, MeshPartBuilder.VertexInfo out) {
 		if (hasPosition)
 			getPos(index, out.position);
 		if (hasTexCoord)
 			getUV(index, out.uv);
 		if (hasNormal)
 			getNor(index, out.normal);
 		if (hasColor)
 			getCol(index, out.color);
 
 		out.hasPosition = hasPosition;
 		out.hasUV = hasTexCoord;
 		out.hasNormal = hasNormal;
 		out.hasColor = hasColor;
 	}
 
 	public void getVertex(MeshPartBuilder.VertexInfo out) {
 		getVertex(currentPosition, out);
 	}
 }
