 package chum.gl;
 
 import chum.f.Vec2;
 import chum.f.Vec3;
 import chum.gl.VertexAttributes.Usage;
 
 
 /**
  * A helper class for constructing a SpriteBatch
  */
 public class SpriteBatchBuilder {
 
     /** The SpriteBatch being constructed */
     protected SpriteBatch spriteBatch;
 
     /** The MeshBuilder instance used */
     protected MeshBuilder meshBuilder;
 
     protected Vec3 pos = new Vec3();
     protected Vec2 tex = new Vec2();
 
 
     public SpriteBatchBuilder() {
         this(new SpriteBatch(null, 1));
     }
 
 
     public SpriteBatchBuilder(SpriteBatch spriteBatch) {
         this.spriteBatch = spriteBatch;
         this.meshBuilder = new MeshBuilder(false, new VertexAttribute(Usage.Position),
                                            new VertexAttribute(Usage.Texture));
     }
 
 
     /**
      * Add a sprite to the SpriteBatch, at the specified location. This adds a
      * quad to the SpriteBatch mesh, using the width and height of the image,
      * and using the given x,y as the lower left corner.
      * 
      * @param image
      *            the location info for the sprint on the SpriteSheet
      * @param x
      *            the left location of the sprite
      * @param y
      *            the bottom location of the sprite
      */
    public void add(SpriteSheet.ImageData image, int x, int y) {
         if (this.spriteBatch.sheet == null)
             this.spriteBatch.sheet = image.sheet;
         else if (image.sheet != this.spriteBatch.sheet)
             throw new IllegalArgumentException(
                                                "Can't add Sprites from multiple SpriteSheets"
                                                        + " to the same SpriteBatch");
 
         int width = image.right - image.left;
         int height = image.bottom - image.top;
         short index = (short) meshBuilder.getNumVertices();
 
         // Lower left
         pos.x = x;
         pos.y = y;
         // pos.z = 0;
         tex.u = image.u1;
         tex.v = image.v1;
         short ll = index++;
         meshBuilder.addVertex(pos, tex);
 
         // Lower right
         pos.x = (x + width);
         tex.u = image.u2;
         short lr = index++;
         meshBuilder.addVertex(pos, tex);
 
         // Upper right
         pos.y = (y + height);
         tex.v = image.v2;
         short ur = index++;
         meshBuilder.addVertex(pos, tex);
 
         // Upper left
         pos.x = x;
         tex.u = image.u1;
         short ul = index++;
         meshBuilder.addVertex(pos, tex);
 
         meshBuilder.addIndex(ll, lr, ur,
                              ll, ur, ul);
     }
 
 
     /**
      * Finish building the SpriteBatch, after all the Sprites have been added
      */
     public SpriteBatch build() {
         meshBuilder.build(spriteBatch);
         return spriteBatch;
     }
 
 }
