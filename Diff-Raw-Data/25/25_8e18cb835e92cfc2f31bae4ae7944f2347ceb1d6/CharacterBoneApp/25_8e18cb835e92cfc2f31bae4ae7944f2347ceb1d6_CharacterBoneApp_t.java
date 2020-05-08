 package fi.haju.haju3d.client.bones;
 
 import com.jme3.animation.Bone;
 import com.jme3.animation.Skeleton;
 import com.jme3.animation.SkeletonControl;
 import com.jme3.app.SimpleApplication;
 import com.jme3.collision.CollisionResult;
 import com.jme3.collision.CollisionResults;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.*;
 import com.jme3.material.Material;
 import com.jme3.math.*;
 import com.jme3.renderer.queue.RenderQueue;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Mesh;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import com.jme3.scene.shape.Quad;
 import com.jme3.scene.shape.Sphere;
 import fi.haju.haju3d.client.SimpleApplicationUtils;
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static fi.haju.haju3d.client.SimpleApplicationUtils.makeColorMaterial;
 import static fi.haju.haju3d.client.SimpleApplicationUtils.makeLineMaterial;
 
 /**
  * TODO:
 * - boneTransform is not very robust, the rotation is random
 * - somehow location of mesh affects transforms?
  * <p/>
  * Backlog:
  * - ability to select bone mesh
  * - ability to quickly edit bone mesh
  * - ability to set constraints on mesh joints
  * - IK animation on bones
  * - quick way to create a leg or an arm (both consist of 3 bones)
  * - texturing
  * <p/>
  * Bone snapping:
  * - create bone endpoint inside mesh, not on surface (try to snap to a "good" location..)
  * - the bigger bone is, the deeper inside its endpoint needs to be; how to make it so that endpoint location
  * is fixed regardless of bone size?
  * - or maybe endpoint should be on surface, but bone mesh extends beyond endpoint?
  * - maybe endpoint should always be forced on surface, no free movement allowed?
  * <p/>
  * Done
 * - create a voxel representation out of bones, make solid mesh
 * - apply perlin noise to voxel mesh
 * - skin/bones animatable mesh
  * - ability to delete bones
  * - save/load bones
  * - RMB to create, LMB to edit
  * - symmetrical editing (forced?)
  * - LMB on empty to rotate
  * - snap dragPlane to x/y/z axes
  */
 public class CharacterBoneApp extends SimpleApplication {
   private static final Logger LOGGER = LoggerFactory.getLogger(CharacterBoneApp.class);
 
   public static final float MINIMUM_BONE_THICKNESS = 0.05f;
  public static final File BONE_FILE = new File("bones3.json");
   public static final Charset BONE_FILE_ENCODING = Charset.forName("UTF-8");
 
   private MyBone camTarget;
   private boolean cameraDragging;
   private float camDistance = 15;
   private float camElevation = 0;
   private float camAzimuth = 0;
   private List<MyBone> bones;
   private Node boneSpatials = new Node();
 
   private DragTarget dragTarget;
   private Spatial dragPlane;
   private Spatial dragPlanePreview;
   private boolean showGuides = true;
   private boolean showMesh = false;
   private Spatial meshSpatial = null;
 
   private Spatial axisIndicators;
   private Skeleton meshSkeleton;
   private Quaternion meshRotation = new Quaternion();
   private List<Bone> meshBones;
 
   private static class Actions {
     public static final String ROTATE_LEFT_MOUSE = "RotateLeftMouse";
     public static final String ROTATE_RIGHT_MOUSE = "RotateRightMouse";
     public static final String ROTATE_UP_MOUSE = "RotateUpMouse";
     public static final String ROTATE_DOWN_MOUSE = "RotateDownMouse";
     public static final String CLICK = "Click";
     public static final String CLICK_RMB = "ClickRmb";
     public static final String RESIZE_DOWN = "ResizeDown";
     public static final String RESIZE_UP = "ResizeUp";
     public static final String SHOW_GUIDES = "ShowGuides";
     public static final String DELETE_BONE = "DeleteBone";
     public static final String SHOW_MESH = "ShowMesh";
   }
 
 
   private Map<String, Mesh> meshMap = new HashMap<>();
   private static final String SPHERE_MESH = "SPHERE";
   private static final String BOX_MESH = "BOX";
 
   {
     meshMap.put(SPHERE_MESH, new Sphere(20, 20, 0.7f));
     meshMap.put(BOX_MESH, new Box(0.2f, 0.2f, 0.5f));
   }
 
   private static class DragTarget {
     private MyBone bone;
     private boolean isStart;
 
     private DragTarget(MyBone bone, boolean isStart) {
       this.bone = bone;
       this.isStart = isStart;
     }
 
     public Vector3f getPosition() {
       return isStart ? bone.getStart() : bone.getEnd();
     }
 
     public void setPosition(Vector3f p) {
       bone.setPosition(p, isStart);
     }
   }
 
   public static Vector3f getMirroredVector(Vector3f p) {
     return new Vector3f(-p.x, p.y, p.z);
   }
 
   public static void main(String[] args) {
     CharacterBoneApp app = new CharacterBoneApp();
     SimpleApplicationUtils.configureSimpleApplication(app);
     app.start();
   }
 
   @Override
   public void simpleInitApp() {
     flyCam.setEnabled(false);
     SimpleApplicationUtils.addLights(this);
     SimpleApplicationUtils.addCartoonEdges(this);
     rootNode.attachChild(BoneMeshUtils.makeFloor(makeColorMaterial(assetManager, ColorRGBA.Blue)));
     axisIndicators = makeAxisIndicators();
     if (showGuides) {
       rootNode.attachChild(axisIndicators);
     }
     rootNode.attachChild(boneSpatials);
 
     try {
       bones = BoneSaveUtils.readBones(FileUtils.readFileToString(BONE_FILE, BONE_FILE_ENCODING));
     } catch (Exception e) {
       e.printStackTrace();
       bones = new ArrayList<>();
 
       LOGGER.warn("Could not read bone file. Create default bone.");
       MyBone bone = new MyBone(new Vector3f(0, 2, 0), new Vector3f(0, -1, 2), 0.2f, SPHERE_MESH);
       bones.add(bone);
     }
 
     camTarget = bones.get(0);
 
     for (MyBone bone : bones) {
       addBoneSpatial(bone);
     }
 
     inputManager.addMapping(Actions.ROTATE_LEFT_MOUSE, new MouseAxisTrigger(MouseInput.AXIS_X, true));
     inputManager.addMapping(Actions.ROTATE_RIGHT_MOUSE, new MouseAxisTrigger(MouseInput.AXIS_X, false));
     inputManager.addMapping(Actions.ROTATE_UP_MOUSE, new MouseAxisTrigger(MouseInput.AXIS_Y, false));
     inputManager.addMapping(Actions.ROTATE_DOWN_MOUSE, new MouseAxisTrigger(MouseInput.AXIS_Y, true));
     inputManager.addListener(new AnalogListener() {
       public static final float ROTATE_SPEED = 5;
 
       @Override
       public void onAnalog(String name, float value, float tpf) {
         if (!cameraDragging) {
           return;
         }
         float v = value * ROTATE_SPEED;
         switch (name) {
         case Actions.ROTATE_LEFT_MOUSE:
           camAzimuth -= v;
           break;
         case Actions.ROTATE_RIGHT_MOUSE:
           camAzimuth += v;
           break;
         case Actions.ROTATE_DOWN_MOUSE:
           camElevation -= v;
           if (camElevation < -FastMath.PI / 2) {
             camElevation = -FastMath.PI / 2;
           }
           break;
         case Actions.ROTATE_UP_MOUSE:
           camElevation += v;
           if (camElevation > FastMath.PI / 2) {
             camElevation = FastMath.PI / 2;
           }
           break;
         }
       }
     }, Actions.ROTATE_LEFT_MOUSE, Actions.ROTATE_RIGHT_MOUSE, Actions.ROTATE_UP_MOUSE, Actions.ROTATE_DOWN_MOUSE);
 
     inputManager.addMapping(Actions.RESIZE_DOWN, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
     inputManager.addMapping(Actions.RESIZE_UP, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
     inputManager.addListener(new AnalogListener() {
       @Override
       public void onAnalog(String name, float value, float tpf) {
         if (name.equals(Actions.RESIZE_DOWN)) {
           value = -value;
         }
         MyBone bone = findCurrentBone();
         if (bone != null) {
           bone.addThickness(value);
         }
       }
     }, Actions.RESIZE_DOWN, Actions.RESIZE_UP);
 
     inputManager.addMapping(Actions.CLICK, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
     inputManager.addMapping(Actions.CLICK_RMB, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
     inputManager.addListener(new ActionListener() {
       @Override
       public void onAction(String name, boolean isPressed, float tpf) {
         if (isPressed) {
           stopDragging();
           if (name.equals(Actions.CLICK)) {
             dragTarget = findDragTarget();
           } else {
             Vector3f attachPoint = findBoneCollisionPoint();
             if (attachPoint != null) {
               // create new bone
               MyBone bone = new MyBone(attachPoint.clone(), attachPoint.clone(), 0.2f, SPHERE_MESH);
               bones.add(bone);
               addBoneSpatial(bone);
               dragTarget = new DragTarget(bone, false);
 
               MyBone bone2 = new MyBone(getMirroredVector(attachPoint), getMirroredVector(attachPoint), 0.2f, SPHERE_MESH);
               bones.add(bone2);
               addBoneSpatial(bone2);
 
               bone.setMirrorBone(bone2);
               startDragging();
             }
           }
           if (dragTarget != null) {
             startDragging();
           } else {
             // start moving camera
             cameraDragging = true;
           }
         } else {
           stopDragging();
           cameraDragging = false;
         }
       }
     }, Actions.CLICK, Actions.CLICK_RMB);
 
     inputManager.addMapping(Actions.SHOW_GUIDES, new KeyTrigger(KeyInput.KEY_RETURN));
     inputManager.addListener(new ActionListener() {
       @Override
       public void onAction(String name, boolean isPressed, float tpf) {
         if (isPressed) {
           showGuides = !showGuides;
           if (showGuides) {
             rootNode.attachChild(axisIndicators);
           } else {
             rootNode.detachChild(axisIndicators);
           }
         }
       }
     }, Actions.SHOW_GUIDES);
 
     inputManager.addMapping(Actions.SHOW_MESH, new KeyTrigger(KeyInput.KEY_SPACE));
     inputManager.addListener(new ActionListener() {
       @Override
       public void onAction(String name, boolean isPressed, float tpf) {
         if (isPressed) {
           showMesh = !showMesh;
           if (showMesh) {
             LOGGER.info("Show mesh");
             Mesh mesh = BoneMeshUtils.buildMesh(bones);
 
             meshBones = new ArrayList<>();
             for (MyBone bone : bones) {
               Bone meshBone = new Bone("bone");
               Transform transform = BoneMeshUtils.boneTransform(bone);
               meshBone.setBindTransforms(transform.getTranslation(), transform.getRotation(), transform.getScale());
               meshBone.setUserControl(true);
               meshBones.add(meshBone);
             }
             meshSkeleton = new Skeleton(meshBones.toArray(new Bone[meshBones.size()]));
 
             // Create model
             Geometry geom = new Geometry("BoneMesh", mesh);
             Material col = SimpleApplicationUtils.makeColorMaterial(assetManager, ColorRGBA.White);
             col.setBoolean("UseVertexColor", true);
             geom.setMaterial(col);
             geom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
 
             Node geomNode = new Node("BoneMeshNode");
             geomNode.attachChild(geom);
 
             // Create skeleton control
             SkeletonControl skeletonControl = new SkeletonControl(meshSkeleton);
             geomNode.addControl(skeletonControl);
 
             meshSpatial = geomNode;
             rootNode.attachChild(meshSpatial);
             rootNode.detachChild(boneSpatials);
           } else {
             LOGGER.info("Hide mesh");
             if (meshSpatial != null) {
               rootNode.detachChild(meshSpatial);
               meshSpatial = null;
               meshSkeleton = null;
               meshBones = null;
             }
             rootNode.attachChild(boneSpatials);
           }
 
         }
       }
     }, Actions.SHOW_MESH);
 
     inputManager.addMapping(Actions.DELETE_BONE, new KeyTrigger(KeyInput.KEY_DELETE));
     inputManager.addListener(new ActionListener() {
       @Override
       public void onAction(String name, boolean isPressed, float tpf) {
         if (isPressed) {
           MyBone bone = findCurrentBone();
           if (bone != null && bones.indexOf(bone) != 0) {
             removeBoneAndMirror(bone);
           }
         }
       }
     }, Actions.DELETE_BONE);
   }
 
   private void addBoneSpatial(MyBone bone) {
     Mesh mesh = meshMap.get(bone.getMeshName());
     final Geometry geom = new Geometry("ColoredMesh", mesh);
     geom.setMaterial(makeColorMaterial(assetManager, ColorRGBA.White));
     geom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
     boneSpatials.attachChild(geom);
   }
 
 
   private void startDragging() {
     // start dragging
     dragPlane = makeDragPlane();
 //    dragPlanePreview = makeDragPlanePreview();
 //    rootNode.attachChild(dragPlanePreview);
   }
 
   private void stopDragging() {
     // stop dragging
     if (dragPlanePreview != null) {
       rootNode.detachChild(dragPlanePreview);
     }
     // possibly remove mirror buddy if bones lie along X plane
     if (dragTarget != null && dragTarget.bone.getMirrorBone() != null) {
       final float snapToXDistance = 0.3f;
       if (FastMath.abs(dragTarget.bone.getStart().x) < snapToXDistance && FastMath.abs(dragTarget.bone.getEnd().x) < snapToXDistance) {
         removeBone(dragTarget.bone.getMirrorBone());
         dragTarget.bone.getStart().x = 0;
         dragTarget.bone.getEnd().x = 0;
       }
     }
     dragPlane = null;
     dragPlanePreview = null;
     dragTarget = null;
   }
 
   public void removeBoneAndMirror(MyBone bone) {
     if (bone.getMirrorBone() != null) {
       removeBone(bone.getMirrorBone());
     }
     removeBone(bone);
   }
 
   public void removeBone(MyBone bone) {
     bone.setMirrorBone(null);
     boneSpatials.detachChildAt(bones.indexOf(bone));
     bones.remove(bone);
   }
 
   private Vector3f findBoneCollisionPoint() {
     CollisionResult collision = findBoneCollision();
     if (collision != null) {
       return collision.getContactPoint();
     }
     return null;
   }
 
   private MyBone findCurrentBone() {
     CollisionResult collision = findBoneCollision();
     if (collision != null) {
       Geometry geom = collision.getGeometry();
       for (Spatial s : boneSpatials.getChildren()) {
         if (s == geom) {
           return bones.get(boneSpatials.getChildIndex(s));
         }
       }
     }
     return null;
   }
 
   private CollisionResult findBoneCollision() {
     CollisionResults results = new CollisionResults();
     int i = boneSpatials.collideWith(getCursorRay(), results);
     if (i > 0) {
       return results.getClosestCollision();
     }
     return null;
   }
 
   private DragTarget findDragTarget() {
     DragTarget best = null;
     float bestDist = 20;
     for (MyBone bone : bones) {
       float distance = cursorDistanceTo(bone.getStart());
       if (distance < bestDist) {
         bestDist = distance;
         best = new DragTarget(bone, true);
       }
       distance = cursorDistanceTo(bone.getEnd());
       if (distance < bestDist) {
         bestDist = distance;
         best = new DragTarget(bone, false);
       }
     }
     return best;
   }
 
   private float cursorDistanceTo(Vector3f pos) {
     Vector3f sc = cam.getScreenCoordinates(pos);
     return new Vector2f(sc.x, sc.y).distance(inputManager.getCursorPosition());
   }
 
   private Spatial makeAxisIndicators() {
     Node n = new Node("AxisIndicators");
     float sz = 20;
     Mesh m = BoneMeshUtils.makeGridMesh(sz, sz, 20, 20);
     //n.attachChild(makeDragPlane(sz, m, makeLineMaterial(assetManager, ColorRGBA.Red), Vector3f.UNIT_Z, Vector3f.UNIT_X, Vector3f.ZERO));
     ColorRGBA color = new ColorRGBA(0.5f, 0.5f, 0, 0.2f);
     n.attachChild(BoneMeshUtils.makeDragPlane(sz, m, makeLineMaterial(assetManager, color), Vector3f.UNIT_X, Vector3f.UNIT_Z, Vector3f.ZERO));
     return n;
   }
 
   private Spatial makeDragPlanePreview() {
     float sz = 20;
     Mesh m = BoneMeshUtils.makeGridMesh(sz, sz, 50, 50);
     return makeDragPlane(sz, m, makeLineMaterial(assetManager, ColorRGBA.Red));
   }
 
   private Spatial makeDragPlane() {
     float sz = 100;
     Mesh m = new Quad(sz, sz);
     return makeDragPlane(sz, m, makeColorMaterial(assetManager, ColorRGBA.Red));
   }
 
   private Spatial makeDragPlane(float sz, Mesh mesh, Material material) {
     // mesh should be a plane [x=[0..sz],y=[0..sz],z=0]
     Vector3f direction = BoneMeshUtils.getAxisSnappedVector(cam.getDirection());
     Vector3f left = BoneMeshUtils.getAxisSnappedVector(cam.getLeft());
     Vector3f center = dragTarget.getPosition();
 
     // non-mirrored bones only dragged along X-plane
     if (dragTarget.bone.getMirrorBone() == null) {
       direction = Vector3f.UNIT_X;
       left = Vector3f.UNIT_Z;
     }
 
     return BoneMeshUtils.makeDragPlane(sz, mesh, material, direction, left, center);
   }
 
   @Override
   public void simpleUpdate(float tpf) {
     if (dragTarget != null) {
       Ray ray = getCursorRay();
       CollisionResults results = new CollisionResults();
       if (dragPlane.collideWith(ray, results) > 0) {
         dragTarget.setPosition(results.getCollision(0).getContactPoint());
       }
     }
 
     setCameraPosition();
     updateBoneSpatials();
 
     if (meshSkeleton != null) {
       // Rotate around X axis
       Quaternion rotate = new Quaternion();
       rotate.fromAngleAxis(tpf, Vector3f.UNIT_X);
 
       // Combine rotation with previous
       meshRotation.multLocal(rotate);
 
       // Set new rotation into bone
       int i = 0;
       for (MyBone bone : bones) {
         Transform transform = BoneMeshUtils.boneTransform(bone);
 
         Bone b = meshSkeleton.getBone(i);
         Vector3f scale = b.getWorldBindInverseScale().mult(transform.getScale());
         Quaternion rotation = b.getWorldBindInverseRotation().mult(transform.getRotation());
         Vector3f translation = b.getWorldBindInversePosition().add(transform.getTranslation());
         b.setUserTransforms(translation, rotation, scale);
         i++;
       }
 
       // After changing skeleton transforms, must update world data
       meshSkeleton.updateWorldVectors();
     }
   }
 
   /**
    * returns a ray pointing from camera to cursor
    */
   private Ray getCursorRay() {
     Vector3f origin = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.0f);
     Vector3f direction = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.3f);
     direction.subtractLocal(origin).normalizeLocal();
     return new Ray(origin, direction);
   }
 
   private void updateBoneSpatials() {
     guiNode.detachAllChildren();
 
     int i = 0;
     for (MyBone b : bones) {
       Transform t = BoneMeshUtils.boneTransform(b);
       if (Vector3f.isValidVector(t.getTranslation())) {
         boneSpatials.getChild(i).setLocalTransform(t);
       }
       i++;
     }
 
     MyBone b = findCurrentBone();
     if (b != null) {
       Node gui = new Node();
 
       Vector3f screenStart = cam.getScreenCoordinates(b.getStart());
       gui.attachChild(BoneMeshUtils.makeCircle(screenStart, guiFont));
 
       Vector3f screenEnd = cam.getScreenCoordinates(b.getEnd());
       gui.attachChild(BoneMeshUtils.makeCircle(screenEnd, guiFont));
       gui.attachChild(BoneMeshUtils.makeLine(screenStart, screenEnd, makeLineMaterial(assetManager, ColorRGBA.Green)));
       guiNode.attachChild(gui);
     }
 
   }
 
   private void setCameraPosition() {
     Quaternion quat = getCameraQuaternion();
     cam.setRotation(quat);
     Vector3f camPos = camTarget.getStart().add(camTarget.getEnd()).divideLocal(2);
     Vector3f lookDir = quat.mult(Vector3f.UNIT_Z);
     camPos.addLocal(lookDir.mult(-camDistance));
     cam.setLocation(camPos);
   }
 
   private Quaternion getCameraQuaternion() {
     Quaternion quat = new Quaternion();
     quat.fromAngles(camElevation, camAzimuth, 0.0f);
     return quat;
   }
 
   @Override
   public void destroy() {
     super.destroy();
     if (bones != null && false) {
       try {
         FileUtils.writeStringToFile(BONE_FILE, BoneSaveUtils.saveBones(bones), BONE_FILE_ENCODING);
       } catch (IOException e) {
         e.printStackTrace();
       }
     }
   }
 }
