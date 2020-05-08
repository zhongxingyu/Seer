 package fi.haju.haju3d.client.bones;
 
 import com.jme3.font.BitmapFont;
 import com.jme3.font.BitmapText;
 import com.jme3.material.Material;
 import com.jme3.math.*;
 import com.jme3.renderer.queue.RenderQueue;
 import com.jme3.scene.*;
 import com.jme3.scene.debug.Arrow;
 import com.jme3.scene.shape.Line;
 import com.jme3.scene.shape.Quad;
 import com.jme3.util.BufferUtils;
 import fi.haju.haju3d.client.chunk.light.ChunkLightManager;
 import fi.haju.haju3d.client.ui.mesh.ChunkSpatialBuilder;
 import fi.haju.haju3d.client.ui.mesh.MyFace;
 import fi.haju.haju3d.client.ui.mesh.MyMesh;
 import fi.haju.haju3d.client.ui.mesh.MyVertex;
 import fi.haju.haju3d.protocol.coordinate.ChunkPosition;
 import fi.haju.haju3d.protocol.coordinate.Vector3i;
 import fi.haju.haju3d.protocol.world.*;
 import fi.haju.haju3d.util.noise.InterpolationUtil;
 
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.*;
 
 public final class BoneMeshUtils {
   private BoneMeshUtils() {
     assert false;
   }
 
   public static Vector3f[] AXES = new Vector3f[] {
       Vector3f.UNIT_X, Vector3f.UNIT_Y, Vector3f.UNIT_Z,
       Vector3f.UNIT_X.negate(), Vector3f.UNIT_Y.negate(), Vector3f.UNIT_Z.negate()};
 
   public static Spatial makeDragPlane(float sz, Mesh mesh, Material material, Vector3f direction, Vector3f left, Vector3f center) {
     final Geometry geom = new Geometry("DragPlane", mesh);
     geom.setMaterial(material);
     geom.setLocalTranslation(-sz / 2, -sz / 2, 0);
     Node node = new Node("DragPlane2");
     node.attachChild(geom);
     node.lookAt(direction.negate(), left);
     node.setLocalTranslation(center);
     return node;
   }
 
   public static BitmapText makeCircle(Vector3f screenPos, BitmapFont guiFont1) {
     BitmapText text = new BitmapText(guiFont1, false);
     text.setSize(guiFont1.getCharSet().getRenderedSize() * 2);
     text.setText("O");
     text.setLocalTranslation(screenPos.add(-text.getLineWidth() / 2, text.getLineHeight() / 2, 0));
     return text;
   }
 
   public static Geometry makeLine(Vector3f screenStart, Vector3f screenEnd, Material material) {
     Line lineMesh = new Line(screenStart, screenEnd);
     lineMesh.setLineWidth(2);
     Geometry line = new Geometry("line", lineMesh);
     line.setMaterial(material);
     return line;
   }
 
   public static Geometry makeArrow(Vector3f screenStart, Vector3f screenEnd, Material material) {
     Arrow lineMesh = new Arrow(screenEnd.subtract(screenStart));
     lineMesh.setLineWidth(2);
     Geometry line = new Geometry("line", lineMesh);
     line.setLocalTranslation(screenStart);
     line.setMaterial(material);
     return line;
   }
 
   public static Transform boneTransform(MyBone b) {
     return transformBetween(b.getStart(), b.getEnd(), Vector3f.UNIT_Z, b.getThickness());
   }
 
   public static Transform transformBetween(Vector3f start, Vector3f end, Vector3f front, float scale) {
     Vector3f dir = start.subtract(end);
     Vector3f left = dir.normalize().cross(front.normalize());
     Vector3f ahead = dir.normalize().cross(left.normalize());
 
     Quaternion q = new Quaternion();
     q.fromAxes(left.normalize(), ahead.normalize(), dir.normalize());
     return new Transform(
         start.add(end).multLocal(0.5f), q,
         new Vector3f(dir.length() * scale, dir.length() * scale, dir.length()));
   }
 
   public static Spatial makeFloor(Material material) {
     Mesh m = new Quad(100, 100);
     final Geometry geom = new Geometry("Floor", m);
     geom.setMaterial(material);
     geom.setShadowMode(RenderQueue.ShadowMode.Receive);
     Matrix3f rot = new Matrix3f();
     rot.fromAngleNormalAxis(3 * FastMath.PI / 2, Vector3f.UNIT_X);
     geom.setLocalRotation(rot);
     geom.setLocalTranslation(-50, -3, 50);
     return geom;
   }
 
   public static Mesh makeGridMesh(float width, float height, int xsteps, int ysteps) {
     Mesh mesh = new Mesh();
     mesh.setMode(Mesh.Mode.Lines);
     int lines = (xsteps + 1) + (ysteps + 1);
     IntBuffer indexes = BufferUtils.createIntBuffer(lines * 2);
     FloatBuffer vertexes = BufferUtils.createFloatBuffer(lines * 2 * 3);
 
     int i = 0;
     for (int x = 0; x <= xsteps; x++) {
       float xpos = x * width / xsteps;
       vertexes.put(xpos).put(0).put(0);
       vertexes.put(xpos).put(height).put(0);
       indexes.put(i).put(i + 1);
       i += 2;
     }
     for (int y = 0; y <= ysteps; y++) {
       float ypos = y * height / ysteps;
       vertexes.put(0).put(ypos).put(0);
       vertexes.put(width).put(ypos).put(0);
       indexes.put(i).put(i + 1);
       i += 2;
     }
 
     mesh.setBuffer(VertexBuffer.Type.Index, 2, indexes);
     mesh.setBuffer(VertexBuffer.Type.Position, 3, vertexes);
 
     mesh.updateBound();
     mesh.setStatic();
     return mesh;
   }
 
   public static Vector3f getAxisSnappedVector(Vector3f v) {
     double snapDist = 0.4;
     for (Vector3f axis : AXES) {
       if (v.distanceSquared(axis) < snapDist) {
         return axis;
       }
     }
     return v;
   }
 
 
   private static class BoneWorldGrid {
     private ByteArray3d grid;
     private Vector3i location;
   }
 
   private static final int MAX_BONES_PER_TILE = 4;
 
   private static class ResultGrid {
     private ByteArray3d dataGrid;
     private ByteArray3d[] boneIndexGrid = new ByteArray3d[MAX_BONES_PER_TILE];
     private ByteArray3d[] boneWeightGrid = new ByteArray3d[MAX_BONES_PER_TILE];
    public Vector3i minLocation;
   }
 
   public static Mesh buildMesh(List<MyBone> bones) {
     //-each bone has some 3d grid associated with it
     //-the grid should not be just binary, but values from 0 to 128 in order to do "meta"-shapes
     //-grid should have bounding box to represent valid x,y,z values where the shape is located..Or that's just grid size.
     //-grid should also indicate location of "start" and "end". So here it's ByteArray3d + startPos + endPos. During
     //  editing it could be represented with World, but when saving it's converted to ByteArray3d with "tight fit".
     //......
     //-based on desired output resolution, one can calculate how big grid is needed for each bone.
     //-each bone gridmesh should be transformed to world grid representation.
     //-each bone's world grid representation should be copied to single world grid that has simple binary value
     // for each cell
 
     ByteArray3d boneMeshGrid = makeBoneMeshGrid();
     float worldScale = 10;
     List<BoneWorldGrid> boneWorldGrids = new ArrayList<>();
     for (MyBone bone : bones) {
       boneWorldGrids.add(makeBoneWorldGrid(boneMeshGrid, worldScale, bone));
     }
     ResultGrid resultGrid = makeResultGrid(boneWorldGrids);
     return getMeshFromGrid(resultGrid, worldScale);
   }
 
   private static ResultGrid makeResultGrid(List<BoneWorldGrid> boneWorldGrids) {
     // find min and max location for all BoneWorldGrids
     Vector3i minLocation = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
     Vector3i maxLocation = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
     for (BoneWorldGrid bwg : boneWorldGrids) {
       minLocation.x = Math.min(minLocation.x, bwg.location.x);
       minLocation.y = Math.min(minLocation.y, bwg.location.y);
       minLocation.z = Math.min(minLocation.z, bwg.location.z);
 
       maxLocation.x = Math.max(maxLocation.x, bwg.location.x + bwg.grid.getWidth());
       maxLocation.y = Math.max(maxLocation.y, bwg.location.y + bwg.grid.getHeight());
       maxLocation.z = Math.max(maxLocation.z, bwg.location.z + bwg.grid.getDepth());
     }
 
     int requiredSize = Math.max(Math.max(maxLocation.x - minLocation.x, maxLocation.y - minLocation.y), maxLocation.z - minLocation.z);
     System.out.println("required grid size = " + requiredSize);
 
     ResultGrid resultGrid = new ResultGrid();
    resultGrid.minLocation = minLocation;
     resultGrid.dataGrid = new ByteArray3d(requiredSize, requiredSize, requiredSize);
     for (int i = 0; i < MAX_BONES_PER_TILE; i++) {
       resultGrid.boneIndexGrid[i] = new ByteArray3d(requiredSize, requiredSize, requiredSize);
       resultGrid.boneWeightGrid[i] = new ByteArray3d(requiredSize, requiredSize, requiredSize);
     }
 
     ByteArray3d dataGrid = resultGrid.dataGrid;
 
     int boneIndex = 0;
     for (BoneWorldGrid bwg : boneWorldGrids) {
       ByteArray3d grid = bwg.grid;
       int w = grid.getWidth();
       int h = grid.getHeight();
       int d = grid.getDepth();
       Vector3i offset = bwg.location.subtract(minLocation);
       Vector3i pos = new Vector3i();
       for (int x = 0; x < w; x++) {
         for (int y = 0; y < h; y++) {
           for (int z = 0; z < d; z++) {
             byte add = grid.get(x, y, z);
             if (add > 0) {
               pos.set(x + offset.x, y + offset.y, z + offset.z);
               byte old = dataGrid.get(pos);
               int newValue = (int) old + (int) add;
               if (newValue > 63) {
                 newValue = 63;
               }
               dataGrid.set(pos, (byte) newValue);
 
               // assign bone weight in next free slot in boneWeightGrid
               int bcount = 0;
               while (bcount < MAX_BONES_PER_TILE && resultGrid.boneWeightGrid[bcount].get(pos) != 0) {
                 bcount++;
               }
               if (bcount < MAX_BONES_PER_TILE) {
                 resultGrid.boneIndexGrid[bcount].set(pos, (byte) boneIndex);
                 resultGrid.boneWeightGrid[bcount].set(pos, add);
               }
             }
           }
         }
       }
       boneIndex++;
     }
     return resultGrid;
   }
 
   private static Mesh getMeshFromGrid(final ResultGrid resultGrid, float worldScale) {
     final int sz = resultGrid.dataGrid.getWidth();
     ChunkPosition cp = new ChunkPosition(0, 0, 0);
     Chunk chunk = new Chunk(sz, 0, cp);
     chunk.set(new Chunk.GetValue() {
       @Override
       public Tile getValue(int x, int y, int z) {
         return resultGrid.dataGrid.get(x, y, z) > 32 ? Tile.GROUND : Tile.AIR;
       }
     });
 
     ChunkCoordinateSystem chunkCoordinateSystem = new ChunkCoordinateSystem(sz);
     World world = new World(chunkCoordinateSystem);
     for (int x = 0; x < 3; x++) {
       for (int y = 0; y < 3; y++) {
         for (int z = 0; z < 3; z++) {
           ChunkPosition pos = new ChunkPosition(x - 1, y - 1, z - 1);
           world.setChunk(pos, new Chunk(sz, 0, pos, Tile.AIR));
         }
       }
     }
     world.setChunk(cp, chunk);
 
     ChunkLightManager light = new ChunkLightManager();
     MyMesh myMesh = ChunkSpatialBuilder.makeCubeMesh(world, cp, light);
 
     Map<MyVertex, Vector3f> vertexToColor = new HashMap<>();
     for (Map.Entry<MyVertex, List<MyMesh.MyFaceAndIndex>> v : myMesh.vertexFaces.entrySet()) {
       vertexToColor.put(v.getKey(), calcVertexColor(v.getValue(), resultGrid));
     }
 
     ChunkSpatialBuilder.smoothMesh(myMesh, 2);
     ChunkSpatialBuilder.prepareMesh(myMesh);
    Mesh mesh = new ChunkSpatialBuilder.SimpleMeshBuilder(myMesh, new Vector3f(resultGrid.minLocation.x, resultGrid.minLocation.y, resultGrid.minLocation.z), 1.0f / worldScale).build();
 
     List<MyFace> realFaces = myMesh.getRealFaces();
     FloatBuffer colors = BufferUtils.createFloatBuffer(realFaces.size() * 4 * 4);
     for (MyFace face : realFaces) {
       putColor(colors, vertexToColor.get(face.v1));
       putColor(colors, vertexToColor.get(face.v2));
       putColor(colors, vertexToColor.get(face.v3));
       putColor(colors, vertexToColor.get(face.v4));
     }
     mesh.setBuffer(VertexBuffer.Type.Color, 4, colors);
 
     // Setup bone weight buffer
     FloatBuffer weights = FloatBuffer.allocate(mesh.getVertexCount() * 4);
     VertexBuffer weightsBuf = new VertexBuffer(VertexBuffer.Type.BoneWeight);
     weightsBuf.setupData(VertexBuffer.Usage.CpuOnly, 4, VertexBuffer.Format.Float, weights);
     mesh.setBuffer(weightsBuf);
 
     // Setup bone index buffer
     ByteBuffer indices = ByteBuffer.allocate(mesh.getVertexCount() * 4);
     VertexBuffer indicesBuf = new VertexBuffer(VertexBuffer.Type.BoneIndex);
     indicesBuf.setupData(VertexBuffer.Usage.CpuOnly, 4, VertexBuffer.Format.UnsignedByte, indices);
     mesh.setBuffer(indicesBuf);
 
     mesh.generateBindPose(true);
 
     for (MyFace face : realFaces) {
       putBoneData(weights, indices, myMesh.vertexFaces.get(face.v1), resultGrid);
       putBoneData(weights, indices, myMesh.vertexFaces.get(face.v2), resultGrid);
       putBoneData(weights, indices, myMesh.vertexFaces.get(face.v3), resultGrid);
       putBoneData(weights, indices, myMesh.vertexFaces.get(face.v4), resultGrid);
     }
 
     mesh.setMaxNumWeights(MAX_BONES_PER_TILE);
 
     return mesh;
   }
 
   private static void putBoneData(FloatBuffer weights, ByteBuffer indices, List<MyMesh.MyFaceAndIndex> myFaceAndIndexes, ResultGrid resultGrid) {
     Vector3i wp = myFaceAndIndexes.get(0).face.worldPos;
     float sumWeights = 0;
     for (int i = 0; i < MAX_BONES_PER_TILE; i++) {
       sumWeights += resultGrid.boneWeightGrid[i].get(wp);
     }
 
     indices.put(resultGrid.boneIndexGrid[0].get(wp));
     indices.put(resultGrid.boneIndexGrid[1].get(wp));
     indices.put(resultGrid.boneIndexGrid[2].get(wp));
     indices.put(resultGrid.boneIndexGrid[3].get(wp));
 
     weights.put(resultGrid.boneWeightGrid[0].get(wp) / sumWeights);
     weights.put(resultGrid.boneWeightGrid[1].get(wp) / sumWeights);
     weights.put(resultGrid.boneWeightGrid[2].get(wp) / sumWeights);
     weights.put(resultGrid.boneWeightGrid[3].get(wp) / sumWeights);
   }
 
   private static Random r = new Random(0L);
   private static List<Vector3f> boneColors = new ArrayList<>();
 
   static {
     for (int i = 0; i < 100; i++) {
       boneColors.add(new Vector3f((float) r.nextDouble(), (float) r.nextDouble(), (float) r.nextDouble()));
     }
   }
 
   private static Vector3f calcVertexColor(List<MyMesh.MyFaceAndIndex> faceAndIndex, ResultGrid resultGrid) {
     for (MyMesh.MyFaceAndIndex fi : faceAndIndex) {
       Vector3i wp = fi.face.worldPos;
       byte bi = resultGrid.boneIndexGrid[0].get(wp);
       return boneColors.get(bi);
     }
     throw new IllegalStateException();
   }
 
   private static void putColor(FloatBuffer colors, Vector3f v) {
     colors.put(v.x).put(v.y).put(v.z).put(1.0f);
   }
 
   private static BoneWorldGrid makeBoneWorldGrid(ByteArray3d boneMeshGrid, float worldScale, MyBone bone) {
     Transform transform = boneTransform(bone);
     //bounding box needed for boneMeshGrid in world grid:
     float bs = 1.0f;
     Vector3f c1 = transform.transformVector(new Vector3f(-bs, -bs, -bs), null).multLocal(worldScale);
     Vector3f c2 = transform.transformVector(new Vector3f(+bs, -bs, -bs), null).multLocal(worldScale);
     Vector3f c3 = transform.transformVector(new Vector3f(-bs, +bs, -bs), null).multLocal(worldScale);
     Vector3f c4 = transform.transformVector(new Vector3f(-bs, -bs, +bs), null).multLocal(worldScale);
     Vector3f c5 = transform.transformVector(new Vector3f(+bs, +bs, -bs), null).multLocal(worldScale);
     Vector3f c6 = transform.transformVector(new Vector3f(-bs, +bs, +bs), null).multLocal(worldScale);
     Vector3f c7 = transform.transformVector(new Vector3f(+bs, -bs, +bs), null).multLocal(worldScale);
     Vector3f c8 = transform.transformVector(new Vector3f(+bs, +bs, +bs), null).multLocal(worldScale);
 
     Vector3f cmin = c1.clone();
     cmin.minLocal(c2);
     cmin.minLocal(c3);
     cmin.minLocal(c4);
     cmin.minLocal(c5);
     cmin.minLocal(c6);
     cmin.minLocal(c7);
     cmin.minLocal(c8);
     Vector3f cmax = c1.clone();
     cmax.maxLocal(c2);
     cmax.maxLocal(c3);
     cmax.maxLocal(c4);
     cmax.maxLocal(c5);
     cmax.maxLocal(c6);
     cmax.maxLocal(c7);
     cmax.maxLocal(c8);
 
     int xsize = (int) FastMath.ceil(cmax.x - cmin.x);
     int ysize = (int) FastMath.ceil(cmax.y - cmin.y);
     int zsize = (int) FastMath.ceil(cmax.z - cmin.z);
 
     BoneWorldGrid bwg2 = new BoneWorldGrid();
     bwg2.grid = new ByteArray3d(xsize, ysize, zsize);
     bwg2.location = new Vector3i(Math.round(cmin.x), Math.round(cmin.y), Math.round(cmin.z));
 
     ByteArray3d grid = bwg2.grid;
     int w = grid.getWidth();
     int h = grid.getHeight();
     int d = grid.getDepth();
     for (int x = 0; x < w; x++) {
       for (int y = 0; y < h; y++) {
         for (int z = 0; z < d; z++) {
           Vector3f v = new Vector3f(x, y, z).add(cmin).divide(worldScale);
           Vector3f inv = transform.transformInverseVector(v, null);
           float eps = 0.001f;
           if (inv.x > -bs + eps && inv.x < bs - eps &&
               inv.y > -bs + eps && inv.y < bs - eps &&
               inv.z > -bs + eps && inv.z < bs - eps) {
 
             int bx = (int) ((inv.x - (-bs)) / (bs - (-bs)) * boneMeshGrid.getWidth());
             int by = (int) ((inv.y - (-bs)) / (bs - (-bs)) * boneMeshGrid.getHeight());
             int bz = (int) ((inv.z - (-bs)) / (bs - (-bs)) * boneMeshGrid.getDepth());
 
             grid.set(x, y, z, boneMeshGrid.get(bx, by, bz));
           }
         }
       }
     }
     return bwg2;
   }
 
   private static ByteArray3d makeBoneMeshGrid() {
     int sz = 128;
     ByteArray3d grid = new ByteArray3d(sz, sz, sz);
     int w = grid.getWidth();
     int h = grid.getHeight();
     int d = grid.getDepth();
 
     FloatArray3d noise = make3dPerlinNoise(1, sz, sz, sz);
 
     for (int x = 0; x < w; x++) {
       for (int y = 0; y < h; y++) {
         for (int z = 0; z < d; z++) {
           int xd = x - sz / 2;
           int yd = y - sz / 2;
           int zd = z - sz / 2;
           int bsz = sz / 3;
           int value = bsz - (int) FastMath.sqrt(xd * xd + yd * yd + zd * zd);
 
           value = (value * 2) + 32;
 
           //x-symmetric noise
           value += noise.get(Math.abs(sz / 2 - x), y, z) * 3;
           value -= 32;
           if (value < 0) value = 0;
           if (value > 63) value = 63;
 
           grid.set(x, y, z, (byte) value);
         }
       }
     }
     return grid;
   }
 
 
   private static FloatArray3d make3dPerlinNoise(long seed, int w, int h, int d) {
     Random random = new Random(seed);
     FloatArray3d data = new FloatArray3d(w, h, d);
     for (int scale = 4; scale != 128; scale *= 2) {
       add3dNoise(random, data, scale, (float) Math.pow(0.5f * scale * 1.0f, 1.0f));
     }
     return data;
   }
 
   private static void add3dNoise(final Random random, FloatArray3d data, int scale, final float amp) {
     int w = data.getWidth();
     int h = data.getHeight();
     int d = data.getDepth();
 
     int nw = w / scale + 2;
     int nh = h / scale + 2;
     int nd = d / scale + 2;
 
     FloatArray3d noise = new FloatArray3d(nw, nh, nd, new FloatArray3d.Initializer() {
       @Override
       public float getValue(int x, int y, int z) {
         return (float) ((random.nextDouble() - 0.5) * amp);
       }
     });
 
     for (int z = 0; z < d; z++) {
       float zt = (float) (z % scale) / scale;
       int zs = z / scale;
       for (int y = 0; y < h; y++) {
         float yt = (float) (y % scale) / scale;
         int ys = y / scale;
         for (int x = 0; x < w; x++) {
           float xt = (float) (x % scale) / scale;
           int xs = x / scale;
 
           float n1 = noise.get(xs, ys, zs);
           float n2 = noise.get(xs + 1, ys, zs);
           float n3 = noise.get(xs, ys + 1, zs);
           float n4 = noise.get(xs + 1, ys + 1, zs);
 
           float n5 = noise.get(xs, ys, zs + 1);
           float n6 = noise.get(xs + 1, ys, zs + 1);
           float n7 = noise.get(xs, ys + 1, zs + 1);
           float n8 = noise.get(xs + 1, ys + 1, zs + 1);
 
           data.add(x, y, z, InterpolationUtil.interpolateLinear3d(xt, yt, zt, n1, n2, n3, n4, n5, n6, n7, n8));
         }
       }
     }
   }
 }
