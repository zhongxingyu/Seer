 package fi.haju.haju3d.client.ui.mesh;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.jme3.asset.AssetManager;
 import com.jme3.asset.TextureKey;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Mesh;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.VertexBuffer.Type;
 import com.jme3.texture.Image;
 import com.jme3.texture.Texture.MinFilter;
 import com.jme3.texture.Texture.WrapMode;
 import com.jme3.texture.TextureArray;
 import com.jme3.util.BufferUtils;
 import fi.haju.haju3d.client.ui.ChunkRenderer;
 import fi.haju.haju3d.client.ui.ChunkSpatial;
 import fi.haju.haju3d.client.ui.mesh.MyMesh.MyFaceAndIndex;
 import fi.haju.haju3d.client.ui.mesh.TileRenderPropertyProvider.TileProperties;
 import fi.haju.haju3d.protocol.Vector3i;
 import fi.haju.haju3d.protocol.world.Tile;
 import fi.haju.haju3d.protocol.world.World;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.*;
 
 @Singleton
 public class ChunkSpatialBuilder {
   public static final int SMOOTH_BUFFER = 2;
   private static final Logger LOGGER = LoggerFactory.getLogger(ChunkSpatialBuilder.class);
   private Material lowMaterial;
   private Material highMaterial;
 
   @Inject
   private ChunkRenderer chunkRenderer;
 
   public void init() {
     init(chunkRenderer.getAssetManager());
   }
 
   public void init(AssetManager assetManager) {
     List<Image> images = new ArrayList<Image>();
     List<Image> normalImages = new ArrayList<Image>();
     for (MyTexture tex : MyTexture.values()) {
       images.add(loadImage(assetManager, tex.getTexturefileName()));
       normalImages.add(loadImage(assetManager, "brick_normal.png"));
     }
     TextureArray textures = makeTextures(images);
     TextureArray normals = makeTextures(normalImages);
     this.lowMaterial = makeMaterial(assetManager, textures, normals, "fi/haju/haju3d/client/shaders/Lighting.j3md");
     this.highMaterial = makeMaterial(assetManager, textures, normals, "fi/haju/haju3d/client/shaders/Terrain.j3md");
   }
 
   private TextureArray makeTextures(List<Image> images) {
     TextureArray textures = new TextureArray(images);
     textures.setWrap(WrapMode.Clamp);
     //TODO setting MinFilter to use MipMap causes GLError GL_INVALID_ENUM! But not idea where exactly..
     textures.setMinFilter(MinFilter.BilinearNearestMipMap);
     textures.setAnisotropicFilter(4);
     return textures;
   }
 
   private Image loadImage(AssetManager assetManager, String tex) {
     String textureResource = "fi/haju/haju3d/client/textures/" + tex;
     TextureKey key = new TextureKey(textureResource);
     key.setGenerateMips(true);
     Image image = assetManager.loadTexture(key).getImage();
     return image;
   }
 
   private Material makeMaterial(AssetManager assetManager, TextureArray textures, TextureArray normals, String materialFile) {
     Material mat = new Material(assetManager, materialFile);
     mat.setBoolean("UseMaterialColors", true);
     mat.setTexture("DiffuseMap", textures);
    //mat.setTexture("NormalMap", normals);
     mat.setColor("Ambient", ColorRGBA.White);
     mat.setColor("Diffuse", ColorRGBA.White);
     return mat;
   }
 
   public void rebuildChunkSpatial(World world, ChunkSpatial chunkSpatial) {
     LOGGER.info("Updating chunk spatial at " + chunkSpatial.chunk.getPosition());
     MyMesh myMesh = makeCubeMesh(world, chunkSpatial.chunk.getPosition());
     chunkSpatial.cubes = makeCubeSpatial(myMesh);
 
     // common processing for non-cube meshes
     smoothMesh(myMesh);
     prepareMesh(myMesh);
 
     chunkSpatial.lowDetail = makeSpatial(true, myMesh);
     chunkSpatial.highDetail = makeSpatial(false, myMesh);
     LOGGER.info("Done");
   }
 
   public static void prepareMesh(MyMesh myMesh) {
     for (MyFace face : myMesh.faces) {
       face.normal = face.v2.v.subtract(face.v1.v).cross(face.v4.v.subtract(face.v1.v)).normalize();
     }
     for (Map.Entry<MyVertex, List<MyFaceAndIndex>> e : myMesh.vertexFaces.entrySet()) {
       Collections.sort(e.getValue(), new Comparator<MyFaceAndIndex>() {
         @Override
         public int compare(MyFaceAndIndex o1, MyFaceAndIndex o2) {
           return Integer.compare(o1.face.zIndex, o2.face.zIndex);
         }
       });
     }
     myMesh.calcVertexNormals();
   }
 
   private Spatial makeCubeSpatial(MyMesh myMesh) {
     // normals are not meaningful for cube spatial, just assign something
     for (MyFace face : myMesh.faces) {
       face.normal = Vector3f.UNIT_Z;
     }
     for (MyVertex v : myMesh.vertexFaces.keySet()) {
       myMesh.vertexToNormal.put(v, Vector3f.UNIT_Z);
     }
     Geometry geom = new Geometry("ColoredMesh", new SimpleMeshBuilder(myMesh).build());
     myMesh.vertexToNormal.clear();
     return geom;
   }
 
   public ChunkSpatial makeChunkSpatial(World world, Vector3i chunkIndex) {
     LOGGER.info("Making chunk spatial at " + chunkIndex);
     ChunkSpatial chunkSpatial = new ChunkSpatial();
     chunkSpatial.chunk = world.getChunk(chunkIndex);
     rebuildChunkSpatial(world, chunkSpatial);
     return chunkSpatial;
   }
 
   public Spatial makeSpatial(boolean useSimpleMesh, MyMesh myMesh) {
     Mesh m = useSimpleMesh ? new SimpleMeshBuilder(myMesh).build() : new NewMeshBuilder2(myMesh).build();
     final Geometry groundObject = new Geometry("ColoredMesh", m);
     groundObject.setMaterial(useSimpleMesh ? lowMaterial : highMaterial);
     groundObject.setShadowMode(ShadowMode.CastAndReceive);
     return groundObject;
   }
 
 
   public static class NewMeshBuilder2 {
     private List<MyFace> realFaces;
     private FloatBuffer vertexes;
     private FloatBuffer vertexNormals;
     private FloatBuffer textureUvs;
     private FloatBuffer textureUvs2;
     private FloatBuffer textureUvs3;
     private FloatBuffer textureUvs4;
     private FloatBuffer textureUvs5;
     private IntBuffer indexes;
     private FloatBuffer[] allUvs;
 
     private MyMesh mesh;
 
     public NewMeshBuilder2(MyMesh mesh) {
       List<MyFace> realFaces = mesh.getRealFaces();
       this.mesh = mesh;
       this.realFaces = realFaces;
 
       final int quadsPerFace = 1;
       this.vertexes = BufferUtils.createFloatBuffer(realFaces.size() * 4 * 3 * quadsPerFace);
       this.vertexNormals = BufferUtils.createFloatBuffer(realFaces.size() * 4 * 3 * quadsPerFace);
       this.textureUvs = BufferUtils.createFloatBuffer(realFaces.size() * 4 * 3 * quadsPerFace);
       this.textureUvs2 = BufferUtils.createFloatBuffer(realFaces.size() * 4 * 3 * quadsPerFace);
       this.textureUvs3 = BufferUtils.createFloatBuffer(realFaces.size() * 4 * 3 * quadsPerFace);
       this.textureUvs4 = BufferUtils.createFloatBuffer(realFaces.size() * 4 * 3 * quadsPerFace);
       this.textureUvs5 = BufferUtils.createFloatBuffer(realFaces.size() * 4 * 3 * quadsPerFace);
       this.indexes = BufferUtils.createIntBuffer(realFaces.size() * 6 * quadsPerFace);
       this.allUvs = new FloatBuffer[] {textureUvs, textureUvs2, textureUvs3, textureUvs4, textureUvs5};
     }
 
     private static final Map<String, List<Vector2f>> UV_MAPPING = new HashMap<>();
     static {
 
       //quad vertex order:
       //32
       //41
 
       // UV_MAPPING is a map that tells you u,v coordinates for vertices 1,2,3,4 of the quad for each
       // neighboring quad.
       {
         List<Vector2f> quad = makeQuad(0, 0);
         String key = uvKey(quad, quad);
         List<Vector2f> uvs = makeUvs(quad);
         UV_MAPPING.put(key, uvs);
       }
       addUvMappings(makeQuad(1, 0));
       addUvMappings(makeQuad(0, 1));
       addUvMappings(makeQuad(0, -1));
       addUvMappings(makeQuad(-1, 0));
     }
 
     private static void addUvMappings(List<Vector2f> quad) {
       for (int e = 0; e < 2; e++) {
         for (int i = 0; i < 4; i++) {
           rotateQuad(quad);
           String key = uvKey(makeQuad(0, 0), quad);
           List<Vector2f> uvs = makeUvs(quad);
           if (UV_MAPPING.containsKey(key)) {
             throw new IllegalStateException("key exists: " + key);
           }
           UV_MAPPING.put(key, uvs);
         }
         flipQuad(quad);
       }
     }
 
     private static String uvKey(List<Vector2f> centerQuad, List<Vector2f> quad) {
       String uvKey = "";
       for (int ci = 0; ci < centerQuad.size(); ci++) {
         boolean match = false;
         int qi = 0;
         for (; qi < quad.size(); qi++) {
           if (quad.get(qi).equals(centerQuad.get(ci))) {
             match = true;
             break;
           }
         }
         if (match) {
           uvKey += (ci + 1) + "=" + (qi + 1) + ",";
         }
       }
       return uvKey;
     }
 
     private static List<Vector2f> makeUvs(List<Vector2f> quad) {
       List<Vector2f> result = new ArrayList<>();
       for (Vector2f q : makeQuad(0, 0)) {
         result.add(makeUv(q, quad));
       }
       return result;
     }
 
     private static Vector2f makeUv(Vector2f q, List<Vector2f> quad) {
       //quad[0] == 0.75 , 0.25
       //quad[2] == 0.25 , 0.75
       //quad[3] == 0.25 , 0.25
 
       float a = q.x - quad.get(3).x;
       float e = q.y - quad.get(3).y;
       float c = quad.get(0).x - quad.get(3).x;
       float g = quad.get(0).y - quad.get(3).y;
       float d = quad.get(2).x - quad.get(3).x;
       float h = quad.get(2).y - quad.get(3).y;
 
       //solve a=x c + y d, e=x g + y h for x and y
       float x, y;
       x = (d * e - a * h) / (d * g - c * h);
       y = (c * e - a * g) / (c * h - d * g);
       return new Vector2f(
           0.25f + 0.5f * x,
           0.25f + 0.5f * y);
     }
 
     private static void flipQuad(List<Vector2f> quad) {
       Collections.reverse(quad);
     }
 
     private static void rotateQuad(List<Vector2f> quad) {
       quad.add(quad.get(0));
       quad.remove(0);
     }
 
     private static List<Vector2f> makeQuad(int x, int y) {
       List<Vector2f> result = new ArrayList<>();
       result.add(new Vector2f(x + 1, y));
       result.add(new Vector2f(x + 1, y + 1));
       result.add(new Vector2f(x, y + 1));
       result.add(new Vector2f(x, y));
       return result;
     }
 
 
     private static class TexZUvs {
       MyTexture texture;
       List<Vector2f> uvs;
       int zIndex;
 
       private TexZUvs(MyTexture texture, List<Vector2f> uvs, int zIndex) {
         this.texture = texture;
         this.uvs = uvs;
         this.zIndex = zIndex;
       }
     }
 
     public Mesh build() {
       int i = 0;
       for (MyFace face : realFaces) {
         face.calcCenter();
 
         Vector3f v1 = face.v1.v;
         Vector3f v2 = face.v2.v;
         Vector3f v3 = face.v3.v;
         Vector3f v4 = face.v4.v;
 
         Vector3f n1 = mesh.vertexToNormal.get(face.v1);
         Vector3f n2 = mesh.vertexToNormal.get(face.v2);
         Vector3f n3 = mesh.vertexToNormal.get(face.v3);
         Vector3f n4 = mesh.vertexToNormal.get(face.v4);
 
         putVector(vertexes, v1);
         putVector(vertexes, v2);
         putVector(vertexes, v3);
         putVector(vertexes, v4);
 
         putVector(vertexNormals, n1);
         putVector(vertexNormals, n2);
         putVector(vertexNormals, n3);
         putVector(vertexNormals, n4);
 
         List<TexZUvs> texZUvses = new ArrayList<>();
         texZUvses.add(new TexZUvs(face.texture, UV_MAPPING.get("1=1,2=2,3=3,4=4,"), face.zIndex));
 
         addBuddyUvs(face, texZUvses, face.v1, face.v2, 1, 2);
         addBuddyUvs(face, texZUvses, face.v2, face.v3, 2, 3);
         addBuddyUvs(face, texZUvses, face.v3, face.v4, 3, 4);
         addBuddyUvs(face, texZUvses, face.v1, face.v4, 1, 4);
 
         Collections.sort(texZUvses, new Comparator<TexZUvs>() {
           @Override
           public int compare(TexZUvs o1, TexZUvs o2) {
             return Integer.compare(o1.zIndex, o2.zIndex);
           }
         });
         // remove top textures if there's too many
         while (texZUvses.size() > 5) {
           texZUvses.remove(1);
         }
 
         for (int ii = 0; ii < texZUvses.size(); ii++) {
           FloatBuffer textures = allUvs[ii];
           TexZUvs texZUvs = texZUvses.get(ii);
           int ti = texZUvs.texture.ordinal();
           for (Vector2f uv : texZUvs.uvs) {
             textures.put(uv.x).put(uv.y).put(ti);
           }
         }
 
         for (int fi = texZUvses.size(); fi < 5; fi++) {
           FloatBuffer uvs = allUvs[fi];
           for (int e = 0; e < 4; e++) {
             uvs.put(0.0f).put(0.0f).put(0);
           }
         }
 
         indexes.put(i + 0).put(i + 1).put(i + 3);
         indexes.put(i + 1).put(i + 2).put(i + 3);
         i += 4;
 
         //vertex/quadrant order:
         //32
         //41
       }
 
       Mesh m = new Mesh();
       m.setBuffer(Type.Position, 3, vertexes);
       m.setBuffer(Type.Normal, 3, vertexNormals);
       m.setBuffer(Type.TexCoord, 3, textureUvs);
       m.setBuffer(Type.TexCoord2, 3, textureUvs2);
       m.setBuffer(Type.TexCoord3, 3, textureUvs3);
       m.setBuffer(Type.TexCoord4, 3, textureUvs4);
       m.setBuffer(Type.TexCoord5, 3, textureUvs5);
       m.setBuffer(Type.Index, 1, indexes);
       m.updateBound();
       return m;
     }
 
     private void addBuddyUvs(MyFace face, List<TexZUvs> texZUvses, MyVertex a, MyVertex b, int ai, int bi) {
       MyFace bf = findBuddyFace(face, a, b);
       if (bf != null && bf.zIndex > face.zIndex) {
         String key = ai + "=" + findFaceVertexIndex(bf, a) + "," + bi + "=" + findFaceVertexIndex(bf, b) + ",";
         texZUvses.add(new TexZUvs(bf.texture, UV_MAPPING.get(key), bf.zIndex));
       }
     }
 
     private MyFace findBuddyFace(MyFace face, MyVertex v1, MyVertex v2) {
       Set<MyFace> potentialFaces = new HashSet<>();
       for (MyFaceAndIndex fi : mesh.vertexFaces.get(v1)) {
         if (fi.face != face) {
           potentialFaces.add(fi.face);
         }
       }
       for (MyFaceAndIndex fi : mesh.vertexFaces.get(v2)) {
         if (potentialFaces.contains(fi.face)) {
           return fi.face;
         }
       }
       return null;
     }
 
     private int findFaceVertexIndex(MyFace face, MyVertex v) {
       for (MyFaceAndIndex fi : mesh.vertexFaces.get(v)) {
         if (fi.face == face) {
           return fi.index;
         }
       }
       throw new IllegalStateException();
     }
   }
 
   public static class SimpleMeshBuilder {
     private MyMesh myMesh;
 
     public SimpleMeshBuilder(MyMesh myMesh) {
       this.myMesh = myMesh;
     }
 
     public Mesh build() {
       List<MyFace> realFaces = myMesh.getRealFaces();
 
       FloatBuffer vertexes = BufferUtils.createFloatBuffer(realFaces.size() * 4 * 3);
       FloatBuffer vertexNormals = BufferUtils.createFloatBuffer(realFaces.size() * 4 * 3);
       FloatBuffer textures = BufferUtils.createFloatBuffer(realFaces.size() * 4 * 3);
       IntBuffer indexes = BufferUtils.createIntBuffer(realFaces.size() * 6);
 
       int i = 0;
       for (MyFace face : realFaces) {
         putVector(vertexes, face.v1.v);
         putVector(vertexes, face.v2.v);
         putVector(vertexes, face.v3.v);
         putVector(vertexes, face.v4.v);
 
         if (face.tile == Tile.BRICK) {
           putVector(vertexNormals, face.normal);
           putVector(vertexNormals, face.normal);
           putVector(vertexNormals, face.normal);
           putVector(vertexNormals, face.normal);
         } else {
           putVector(vertexNormals, myMesh.vertexToNormal.get(face.v1));
           putVector(vertexNormals, myMesh.vertexToNormal.get(face.v2));
           putVector(vertexNormals, myMesh.vertexToNormal.get(face.v3));
           putVector(vertexNormals, myMesh.vertexToNormal.get(face.v4));
         }
 
         int ti = face.texture.ordinal();
         textures.put(0.25f).put(0.25f).put(ti);
         textures.put(0.25f).put(0.75f).put(ti);
         textures.put(0.75f).put(0.75f).put(ti);
         textures.put(0.75f).put(0.25f).put(ti);
 
         indexes.put(i + 0).put(i + 1).put(i + 3);
         indexes.put(i + 1).put(i + 2).put(i + 3);
 
         i += 4;
       }
 
       Mesh m = new Mesh();
       m.setBuffer(Type.Position, 3, vertexes);
       m.setBuffer(Type.Normal, 3, vertexNormals);
       m.setBuffer(Type.TexCoord, 3, textures);
       m.setBuffer(Type.Index, 1, indexes);
       m.updateBound();
       return m;
     }
   }
 
   private static void putVector(FloatBuffer vertexes, Vector3f v) {
     vertexes.put(v.x).put(v.y).put(v.z);
   }
 
   private static int getZIndex(int x, int y, int z, int edge) {
     return new Random(x + y * 133 + z * 23525 + edge * 1248234).nextInt();
   }
 
   public static MyMesh makeCubeMesh(World world, Vector3i chunkIndex) {
     synchronized (world) {
       MyMesh myMesh = new MyMesh();
 
       Vector3i w1o = world.getWorldPosition(chunkIndex);
       Vector3i w2o = world.getWorldPosition(chunkIndex.add(1, 1, 1));
 
       Vector3i w1 = w1o.add(-SMOOTH_BUFFER, -SMOOTH_BUFFER, -SMOOTH_BUFFER);
       Vector3i w2 = w2o.add(SMOOTH_BUFFER, SMOOTH_BUFFER, SMOOTH_BUFFER);
 
       for (int z = w1.z; z < w2.z; z++) {
         for (int y = w1.y; y < w2.y; y++) {
           for (int x = w1.x; x < w2.x; x++) {
             Tile tile = world.get(x, y, z);
             if (tile != Tile.AIR) {
               TileProperties properties = TileRenderPropertyProvider.getProperties(tile);
               boolean realTile =
                   x >= w1o.x && x < w2o.x &&
                       y >= w1o.y && y < w2o.y &&
                       z >= w1o.z && z < w2o.z;
               float color = world.getColor(x, y, z);
               if (world.get(x, y - 1, z) == Tile.AIR) {
                 int seed = getZIndex(x, y, z, 0);
                 myMesh.addFace(
                     new Vector3f(x, y, z),
                     new Vector3f(x + 1, y, z),
                     new Vector3f(x + 1, y, z + 1),
                     new Vector3f(x, y, z + 1),
                     properties.getSideTexture(seed), color,
                     realTile,
                     seed, tile);
               }
               if (world.get(x, y + 1, z) == Tile.AIR) {
                 int seed = getZIndex(x, y, z, 1);
                 myMesh.addFace(
                     new Vector3f(x, y + 1, z + 1),
                     new Vector3f(x + 1, y + 1, z + 1),
                     new Vector3f(x + 1, y + 1, z),
                     new Vector3f(x, y + 1, z),
                     properties.getTopTexture(seed), color,
                     realTile,
                     seed, tile);
               }
               if (world.get(x - 1, y, z) == Tile.AIR) {
                 int seed = getZIndex(x, y, z, 2);
                 myMesh.addFace(
                     new Vector3f(x, y, z + 1),
                     new Vector3f(x, y + 1, z + 1),
                     new Vector3f(x, y + 1, z),
                     new Vector3f(x, y, z),
                     properties.getSideTexture(seed), color,
                     realTile,
                     seed, tile);
               }
               if (world.get(x + 1, y, z) == Tile.AIR) {
                 int seed = getZIndex(x, y, z, 3);
                 myMesh.addFace(
                     new Vector3f(x + 1, y, z),
                     new Vector3f(x + 1, y + 1, z),
                     new Vector3f(x + 1, y + 1, z + 1),
                     new Vector3f(x + 1, y, z + 1),
                     properties.getSideTexture(seed), color,
                     realTile,
                     seed, tile);
               }
               if (world.get(x, y, z - 1) == Tile.AIR) {
                 int seed = getZIndex(x, y, z, 4);
                 myMesh.addFace(
                     new Vector3f(x, y, z),
                     new Vector3f(x, y + 1, z),
                     new Vector3f(x + 1, y + 1, z),
                     new Vector3f(x + 1, y, z),
                     properties.getSideTexture(seed), color,
                     realTile,
                     seed, tile);
               }
               if (world.get(x, y, z + 1) == Tile.AIR) {
                 int seed = getZIndex(x, y, z, 5);
                 myMesh.addFace(
                     new Vector3f(x + 1, y, z + 1),
                     new Vector3f(x + 1, y + 1, z + 1),
                     new Vector3f(x, y + 1, z + 1),
                     new Vector3f(x, y, z + 1),
                     properties.getSideTexture(seed), color,
                     realTile,
                     seed, tile);
               }
             }
           }
         }
       }
       return myMesh;
     }
   }
 
   private static class PositionChange {
     Vector3f oldPos;
     Vector3f newPos;
 
     private PositionChange(Vector3f oldPos, Vector3f newPos) {
       this.oldPos = oldPos;
       this.newPos = newPos;
     }
   }
 
   public static void smoothMesh(MyMesh myMesh) {
     for (int i = 0; i < SMOOTH_BUFFER; i++) {
       List<PositionChange> newPos = new ArrayList<>(myMesh.vertexFaces.size());
       for (MyFace f : myMesh.faces) {
         f.calcCenter();
       }
       for (Map.Entry<MyVertex, List<MyFaceAndIndex>> e : myMesh.vertexFaces.entrySet()) {
         MyVertex vertex = e.getKey();
         Vector3f sum = Vector3f.ZERO.clone();
         List<MyFaceAndIndex> faces = e.getValue();
         int maxSmooths = SMOOTH_BUFFER;
         for (MyFaceAndIndex f : faces) {
           sum.addLocal(f.face.center);
           maxSmooths = Math.min(TileRenderPropertyProvider.getProperties(f.face.tile).getMaxSmooths(), maxSmooths);
         }
         sum.divideLocal(faces.size());
         if (vertex.smooths < maxSmooths) {
           vertex.smooths++;
           newPos.add(new PositionChange(vertex.v, sum));
         }
       }
       for (PositionChange positionChange : newPos) {
         positionChange.oldPos.set(positionChange.newPos);
       }
     }
   }
 }
