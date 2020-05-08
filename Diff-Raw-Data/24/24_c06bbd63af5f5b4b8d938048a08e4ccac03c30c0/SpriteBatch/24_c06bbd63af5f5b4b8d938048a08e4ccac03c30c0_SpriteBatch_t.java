 package chum.gl;
 
 import chum.f.Vec3;
 import chum.gl.VertexAttributes.Usage;
 import chum.gl.render.Sprite;
 
 import javax.microedition.khronos.opengles.GL10;
 
 
 /**
  * Special type of mesh meant for displaying a bunch of static sprites.
  * 
  * The mesh is built as a set of quads, with each given an arbitrary location in
  * 2D space. {@link SpriteBatchBuilder} is provided for specifying the Sprites
  * and their locations.
  * 
  * All the sprites are drawn together as a single operation (single mesh). This
  * is much more efficient than drawing a bunch of individual {@link Sprite}s, in
  * which each one has to be placed + texture bound + verts and indices bound.
  * 
  * Alternatively, individual sprites can be rendered from this batch using
  * {@link Sprite}. Either way, it should be done as a child of
  * {@link SpriteModeNode} for optimal sprite rendering settings.
  * 
  * TODO: currently done w/ GL_TRIANGLES, and 4 verts/6 indices per sprite. That
  * is needed if multiple sprites are rendered from the batch in a single call.
  * Perhaps an optimization should be added that uses GL_TRIANGLE_STRIP, for the
  * (common) case when individual sprites are rendered (Sprite class with
  * count=1) -- that allows 4 verts/4 indices per sprite.
  */
 public class SpriteBatch extends Mesh {
 
     /** The maximum number of sprites */
     public int maxSprites;
 
     /** The sprites images being displayed */
     public SpriteSheet sheet;
 
 
     /**
      * Construct the mesh to hold the given number of sprites
      */
     public SpriteBatch(SpriteSheet sheet, int count) {
         super(true, true, false, // floating point
               4 * count, // 4 vertices per sprite (two triangles)
               6 * count, // 6 indices per sprite (two triangles)
 
               new VertexAttribute(Usage.Position), new VertexAttribute(Usage.Texture));
 
         this.type = GL10.GL_TRIANGLES;
 
         this.sheet = sheet;
         this.maxSprites = count;
     }
 
 
     @Override
     public Texture getTexture() {
         return sheet;
     }
 
 
     public void adjustOrigin(int offset, int count, Vec3 delta) {
         Mesh.Vertex vertex = new Mesh.Vertex(this.attributes);
 
        //chum.util.Log.d("adjustOrigin: offset=%d count=%d delta=%s", offset, count, delta);
         for (int i = offset, i1 = offset + count; i < i1; ++i) {
             int v0 = i * 4;
             for (int v = v0, v1 = v0 + 4; v < v1; ++v) {
                 getVertex(v, vertex);
                 vertex.position.add(delta, vertex.position);
                 putVertex(v, vertex);
             }
         }
     }
 
     public static final int ADJUST_CENTER = 1;
     public static final int ADJUST_WIDTH = 2;
     public static final int ADJUST_HEIGHT = 3;
 
 
     public void adjustOrigin(int offset, int count, int adjustment) {
         Mesh.Bounds bounds = Mesh.Bounds.obtain();
         Vec3 delta = new Vec3();
 
         for (int i = offset, i1 = offset + count; i < i1; ++i) {
             int v0 = i * 4;
             bounds.update(this, v0, 4);
             switch (adjustment) {
             case ADJUST_CENTER:
                 delta.set(bounds.minimum.x - bounds.center.x,
                           bounds.minimum.y - bounds.center.y,
                           0);
                 break;
             case ADJUST_WIDTH:
                 delta.set(bounds.minimum.x - bounds.maximum.x, 0, 0);
                 break;
             case ADJUST_HEIGHT:
                 delta.set(0, bounds.minimum.y - bounds.maximum.y, 0);
                 break;
             default:
                 return;
             }
 
             adjustOrigin(i, 1, delta);
         }
     }
 }
