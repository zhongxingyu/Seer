 package fi.haju.haju3d.client;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.light.DirectionalLight;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Spatial;
 import fi.haju.haju3d.client.ui.mesh.ChunkSpatialBuilder;
 import fi.haju.haju3d.client.ui.mesh.MyMesh;
 import fi.haju.haju3d.client.ui.mesh.MyTexture;
 import fi.haju.haju3d.protocol.world.Tile;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 public class ShaderTestingApp extends SimpleApplication {
   public static void main(String[] args) {
     ShaderTestingApp app = new ShaderTestingApp();
     SimpleApplicationUtils.configureSimpleApplication(app);
     app.start();
   }
 
   private ChunkSpatialBuilder builder = new ChunkSpatialBuilder();
 
   private void loadTextures() {
     builder.init(assetManager);
   }
 
   @Override
   public void simpleInitApp() {
     loadTextures();
 
     int height = 10;
     int width = 10;
 
     MyMesh myMesh = new MyMesh();
     Random r = new Random(0L);
 
     List<MyTexture> texs = Arrays.asList(MyTexture.values());
 
     int z = 0;
     for (int x = 0; x < width; x++) {
       for (int y = 0; y < height; y++) {
         int ti = r.nextInt(texs.size());
         MyTexture texture = texs.get(ti);
 
         int zindex = r.nextInt(10000);
         // grass always on top
         if (texture == MyTexture.GRASS) {
           zindex += 10000;
         }
 
         List<Vector3f> verts = new ArrayList<>();
         verts.add(new Vector3f(x + 1, y, z));
         verts.add(new Vector3f(x + 1, y + 1, z));
         verts.add(new Vector3f(x, y + 1, z));
         verts.add(new Vector3f(x, y, z));
 
         int si = r.nextInt(4);
         List<Vector3f> verts2 = new ArrayList<>();
         verts2.add(verts.get(si++ % 4));
         verts2.add(verts.get(si++ % 4));
         verts2.add(verts.get(si++ % 4));
         verts2.add(verts.get(si++ % 4));
 
         myMesh.addFace(
             verts2.get(0),
             verts2.get(1),
             verts2.get(2),
             verts2.get(3),
             texture,
             1.0f, true,
             zindex,
             Tile.ROCK,
            100);
       }
     }
 
     ChunkSpatialBuilder.prepareMesh(myMesh);
 
     /*
     // simple faces
     int i = 0;
     for (MyFace face : realFaces) {
       face.normal = new Vector3f(0, 0, -1);
       face.calcCenter();
       
       Vector3f v1 = face.v1.v;
       Vector3f v2 = face.v2.v;
       Vector3f v3 = face.v3.v;
       Vector3f v4 = face.v4.v;
       
       // full face quadrant
       putVector(vertexes, v1);
       putVector(vertexes, v2);
       putVector(vertexes, v3);
       putVector(vertexes, v4);
 
       putVector(vertexNormals, face.normal);
       putVector(vertexNormals, face.normal);
       putVector(vertexNormals, face.normal);
       putVector(vertexNormals, face.normal);
       
       FloatBuffer uvs = allUvs[0];
       int ti = face.texture.ordinal();
       uvs.put(0.75f).put(0.75f).put(ti);
       uvs.put(0.75f).put(0.25f).put(ti);
       uvs.put(0.25f).put(0.25f).put(ti);
       uvs.put(0.25f).put(0.75f).put(ti);
       
       indexes.put(i + 0).put(i + 1).put(i + 3);
       indexes.put(i + 1).put(i + 2).put(i + 3);
 
       i += 4;
     }
     */
 
 
     //unshaded: 5500  FPS (limited?)
     //lightning, simple texture: 3900 FPS
     //lightning, texture array: 3700 FPS
     //lightning, texture array with 2d UVs and modf: 3700 FPS
     //lightning, texture array with 2d UVs and modf, vertex lightning: 5100 FPS
     //lightning, texture array with 3d UVs, 4-way blending: 2900 FPS
     //lightning, texture array with 3d UVs, 4-way blending, doubled polys: 2800 FPS
     //final: 2600 FPS
 
     Spatial obj = builder.makeSpatial(false, myMesh);
     float scale = 1.0f;
     obj.setLocalScale(scale);
     obj.setLocalTranslation(-width * 0.5f * scale, -height * 0.5f * scale, 0);
     rootNode.attachChild(obj);
 
     DirectionalLight light = new DirectionalLight();
     Vector3f lightDir = new Vector3f(-1, -2, -3);
     light.setDirection(lightDir.normalizeLocal());
     light.setColor(new ColorRGBA(1f, 1f, 1f, 1f).mult(1.0f));
     rootNode.addLight(light);
 
   }
 
 }
