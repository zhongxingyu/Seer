 package chum.gl;
 
 import chum.f.Vec2;
 import chum.f.Vec3;
 import chum.fp.FP;
 import chum.gl.VertexAttributes.Usage;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.nio.ShortBuffer;
 
 import javax.microedition.khronos.opengles.GL10;
 
 
 /**
    Class to help construct meshes by adding vertices one at a time.
    This is intended for simple meshes built up in code, such as a
    cube, rather than more complex mesh models.
 */
 public class MeshBuilder {
 
     /** fixed point or floating */
     public boolean useFixedPoint;
 
     /** the vertex attributes */
     public VertexAttributes attributes;
 
     /** the floating point vertex array */
     public FloatBuffer floatVerts;
 
     /** the fixed-point vertex array */
     public IntBuffer fixedVerts;
 
     /** the number of vertices added so far */
     protected int count;
 
     /** the max number of vertices */
     protected int capacity;
 
     /** the indices */
     public ShortBuffer indices;
 
     /** the max number of indices */
     protected int ind_count;
 
     /** the max number of indices */
     protected int ind_capacity;
 
     /** The mesh to build */
     protected Mesh meshToBuild;
 
 
     public MeshBuilder( boolean useFixedPoint,
                         VertexAttribute ... attributes ) {
         this.useFixedPoint = useFixedPoint;
         this.attributes = new VertexAttributes( attributes );
     }
     
 
     public MeshBuilder(Mesh mesh) {
         this.meshToBuild = mesh;
         this.useFixedPoint = mesh.useFixedPoint;
         this.attributes = mesh.attributes;
     }
     
 
     public void ensureCapacity(int numVerts, int numIndices) {
         int deltaVerts = numVerts - count;
         if ( deltaVerts > 0 ) extendVerts(deltaVerts);
 
         int deltaInd = numIndices - ind_count;
         if ( deltaInd > 0 ) extendIndices(deltaInd);
     }
 
 
     public void addVertex(Vec3 pos) {
         if ( !checkAttributes(Usage.Position) )
             throw new IllegalArgumentException("Vertex data doesn't match attributes");
 
         extendVerts(1);
         put(pos);
         count++;
     }
 
     public void addVertex(Vec3 pos, Vec3 norm, Color col) {
         if ( !checkAttributes(Usage.Position,
                               Usage.Normal,
                               Usage.Color) )
             throw new IllegalArgumentException("Vertex data doesn't match attributes");
 
         extendVerts(1);
         put(pos);
         put(norm);
         put(col);
         count++;
     }
 
     public void addVertex(Vec3 pos, Vec3 norm) {
         if ( !checkAttributes(Usage.Position,
                               Usage.Normal) )
             throw new IllegalArgumentException("Vertex data doesn't match attributes");
 
         extendVerts(1);
         put(pos);
         put(norm);
         count++;
     }
 
     public void addVertex(Vec3 pos, Color col) {
         if ( !checkAttributes(Usage.Position,
                               Usage.Color) )
             throw new IllegalArgumentException("Vertex data doesn't match attributes");
 
         extendVerts(1);
         put(pos);
         put(col);
         count++;
     }
 
     public void addVertex(Vec3 pos, Vec3 norm, Vec2 tex) {
         if ( !checkAttributes(Usage.Position,
                               Usage.Normal,
                               Usage.Texture) )
             throw new IllegalArgumentException("Vertex data doesn't match attributes");
 
         extendVerts(1);
         put(pos);
         put(norm);
         put(tex);
         count++;
     }
 
     public void addVertex(Vec3 pos, Vec2 tex) {
         if ( !checkAttributes(Usage.Position,
                               Usage.Texture) )
             throw new IllegalArgumentException("Vertex data doesn't match attributes");
 
         extendVerts(1);
         put(pos);
         put(tex);
         count++;
     }
 
 
     /** Get the number of vertices added so far */
     public int getNumVertices() {
         return count;
     }
 
 
     public void addIndex( int ... index ) {
         extendIndices(index.length);
         for( int i=0; i<index.length; ++i )
             indices.put((short)index[i]);
         ind_count += index.length;
     }
 
 
     /** Get the number of indices added so far */
     public int getNumIndices() {
         return ind_count;
     }
 
 
     /** Build a new mesh */
     public Mesh build(boolean managed, boolean isStatic) {
         return build(managed,isStatic,GL10.GL_TRIANGLES);
     }
 
 
     /** Build a new mesh */
     public Mesh build(boolean managed, boolean isStatic,int type) {
         Mesh newMesh = new Mesh(managed, isStatic, useFixedPoint,
                                 count, indices.position(), attributes);
         return build(newMesh,type);
     }
 
 
     /** Build (populate) an existing mesh instance.
         This can only be done when the MeshBuilder was created with a Mesh instance */
     public Mesh build() {
         if ( meshToBuild == null )
             throw new IllegalArgumentException("Can't use this build() without a mesh");
         Mesh mesh = meshToBuild;
         meshToBuild = null;
         return build(mesh);
     }
 
 
     
     public Mesh build(Mesh mesh) {
         return build(mesh,mesh.type);
     }
 
     
     public Mesh build(Mesh mesh, int type) {
         if ( useFixedPoint ) {
             int[] verts = new int[count * (attributes.vertexSize / 4)]; // 4=sizeof(int)
             fixedVerts.clear();
            fixedVerts.get(verts);
             mesh.setVertices(verts);
         } else {
             float[] verts = new float[count * (attributes.vertexSize / 4)]; // 4=sizeof(int)
             floatVerts.clear();
            floatVerts.get(verts);
             mesh.setVertices(verts);
         }
 
         short[] i_array = new short[indices.position()];
         indices.clear();
        indices.get(i_array);
         mesh.setIndices(i_array);
 
         mesh.type = type;
         return mesh;
     }
 
 
     protected boolean checkAttributes(int ... expectedUsage) {
         if ( expectedUsage.length != attributes.size() )
             return false;
 
         for (int i=0; i < expectedUsage.length; ++i) {
             if ( expectedUsage[i] != attributes.get(i).usage )
                 return false;
         }
 
         return true;
     }
 
 
     /**
        Extend the vertices array to hold at least 'additional' vertices
        @param additional number of additional vertices to allocate
     */
     protected void extendVerts(int additional) {
         // Check if there is room for another vert
         if ( useFixedPoint ) {
             if ( fixedVerts != null &&
                  (fixedVerts.position() + additional) < fixedVerts.capacity() )
                 return;
         } else {
             if ( floatVerts != null &&
                  (floatVerts.position() + additional) < floatVerts.capacity() )
                 return;
         }
 
         // Increase capacity
         int vertexInts = (attributes.vertexSize/4); // sizeof(int) or sizeof(float)
         int minCapacity = (count + additional) * vertexInts;
         while ( capacity < minCapacity ) {
             if ( capacity == 0 )
                 capacity = 10 * vertexInts;
             else if ( additional > 1 )
                 capacity += additional * vertexInts;
             else
                 capacity = capacity * 2;
         }
        //Log.d("increased vert capacity to %d", capacity);
 
         // Allocate / Re-allocate the buffers for new capacity
         if ( useFixedPoint ) {
             if ( fixedVerts == null )
                 fixedVerts = IntBuffer.allocate(capacity * attributes.vertexSize);
             else {
                 IntBuffer moreVerts = IntBuffer.allocate(capacity * attributes.vertexSize);
                 fixedVerts.clear();
                for( int c = count; c > 0; -- c )
                    moreVerts.put(fixedVerts.get());
                 fixedVerts = moreVerts;
             }
             //Log.d("extendVerts: vert capacity=%d, buf position=%d, capacity=%d",
             //      capacity, fixedVerts.position(), fixedVerts.capacity());
         } else {
             if ( floatVerts == null )
                 floatVerts = FloatBuffer.allocate(capacity);
             else {
                FloatBuffer moreVerts = FloatBuffer.allocate(capacity);
                 floatVerts.clear();
                for( int c = count; c > 0; -- c )
                    moreVerts.put(floatVerts.get());
                 floatVerts = moreVerts;
             }
         }
     }
 
 
     protected void extendIndices(int additional) {
         // Check if there is room for another vert
         if ( indices != null &&
              (indices.position() + additional) < indices.capacity() )
             return;
 
         // Increase capacity
         if ( ind_capacity == 0 ) ind_capacity = (additional < 10) ? 10 : additional;
         while ( ind_capacity < ind_count + additional ) {
             if ( additional > 1 )
                 ind_capacity += additional;
             else
                 ind_capacity = ind_capacity * 2;
         }
         //Log.d("increased index capacity to %d", ind_capacity);
 
         // Allocate / re-allocate for increased capacity
         if ( indices == null )
             indices = ShortBuffer.allocate(ind_capacity);
         else {
             ShortBuffer moreIndices = ShortBuffer.allocate(ind_capacity);
             indices.clear();
             for( int c = ind_count; c > 0; -- c )
                 moreIndices.put(indices.get());
             indices = moreIndices;
         }
         //Log.d("extendIndices: index count=%d, capacity=%d, buf position=%d, capacity=%d",
         //      ind_count, ind_capacity, indices.position(), indices.capacity());
     }
 
 
     protected void put(Vec3 v) {
         if ( useFixedPoint ) {
             fixedVerts.put(FP.floatToFP(v.x));
             fixedVerts.put(FP.floatToFP(v.y));
             fixedVerts.put(FP.floatToFP(v.z));
         } else {
             floatVerts.put(v.x);
             floatVerts.put(v.y);
             floatVerts.put(v.z);
         }
     }
 
 
     protected void put(Vec2 v) {
         if ( useFixedPoint ) {
             fixedVerts.put(FP.floatToFP(v.u));
             fixedVerts.put(FP.floatToFP(v.v));
         } else {
             floatVerts.put(v.u);
             floatVerts.put(v.v);
         }
     }
 
     protected void put(Color c) {
         if ( useFixedPoint ) {
             fixedVerts.put(FP.floatToFP(c.red));
             fixedVerts.put(FP.floatToFP(c.green));
             fixedVerts.put(FP.floatToFP(c.blue));
             fixedVerts.put(FP.floatToFP(c.alpha));
         } else {
             floatVerts.put(c.red);
             floatVerts.put(c.green);
             floatVerts.put(c.blue);
             floatVerts.put(c.alpha);
         }
     }
 
 }
