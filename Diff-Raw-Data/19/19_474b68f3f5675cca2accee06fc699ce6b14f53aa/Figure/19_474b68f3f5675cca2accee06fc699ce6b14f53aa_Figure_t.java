 package mocap.figure;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 import mocap.j3d.Util;
 import mocap.player.MocapPlayer;
 
 /**
  * Represents an autonomous graphical figure. Contains the skeleton and
  * its own player (= motor).
  * 
  * @author Michael Kipp
  */
 public class Figure
 {
     private String _name;
     private Bone _skeleton; // root bone of skeleton
     private MocapPlayer _player; // drives animation
     private Point3d _offset;
     private BranchGroup _branchGroup; // scenegraph root
     private TransformGroup _tg; // can be used to give a base orientation
 
     /**
      * Creates a new figure and initializes the skeleton.
      *
      * @param name
      *            ID of the figure
      * @param skeleton
      *            Root bone of the skeleton
      */
     public Figure(String name, Bone skeleton)
     {
         _name = name;
         _skeleton = skeleton;
         _branchGroup = new BranchGroup();
         _branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
         _tg = new TransformGroup();
         _tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 
         _tg.addChild(_skeleton.getBaseTG());
         _branchGroup.addChild(_tg);
         _branchGroup.compile();
     }
 
     public double getScale()
     {
         return _skeleton.getScale();
     }
 
     /**
      * Measures the length of the skeleton by computing the max. distance between any two leaves in
      * the skeleton tree. Figure must be LIVE to allow this operation.
      */
     public double measureLength()
     {
         List<Bone> leaves = new ArrayList<Bone>();
         List<Bone> all = new ArrayList<Bone>();
         _skeleton.collectBones(all);
         for (Bone b : all) {
             if (b.getChildren().length == 0) {
                 leaves.add(b);
             }
         }
         double dist = 0;
         Point3d p1 = new Point3d();
         Point3d p2 = new Point3d();
         for (Bone b1 : leaves) {
             for (Bone b2 : leaves) {
                 if (!b1.equals(b2)) {
                     b1.getWorldPosition(p1);
                     b2.getWorldPosition(p2);
                     dist = Math.max(p1.distance(p2), dist);
                 }
             }
         }
         System.out.println("### LENGTH IS " + dist);
         return dist;
     }
 
     /**
      * For motion data where the skeleton head point up the z-axis, use this transformation (reset
      * with resetRotation).
      */
     //    public void correctUpVector(Vector3d rotationAxis, double angle)
     //    {
     //        Transform3D t1 = new Transform3D();
     //        Transform3D t2 = new Transform3D();
     //        t1.rotX(-Math.PI / 2d);
     //        t2.rotY(-Math.PI / 2d);
     //        t2.mul(t1);
     //        _tg.setTransform(t2);
     //    }
     /**
      * For motion data where the skeleton head point up the z-axis, use this transformation (reset
      * with resetRotation).
      */
     public void setZUpRotation()
     {
         Transform3D t2 = new Transform3D();
         t2.rotX(-Math.PI / 2d);
         _tg.setTransform(t2);
     }
 
     /**
      * For motion data where the skeleton head point up the x-axis, use this transformation (reset
      * with resetRotation).
      */
     public void setXUpRotation()
     {
         Transform3D t2 = new Transform3D();
         t2.rotZ(Math.PI / 2d);
         _tg.setTransform(t2);
     }
 
     public void resetRotation()
     {
         _tg.setTransform(new Transform3D());
     }
 
     public boolean hasAnimation()
     {
         return _player != null;
     }
 
     public String getName()
     {
         return _name;
     }
 
     public MocapPlayer getPlayer()
     {
         return _player;
     }
 
     public Bone getSkeleton()
     {
         return _skeleton;
     }
 
     public void setBG(BranchGroup bg)
     {
         _branchGroup = bg;
     }
 
     /**
      * Point in the scene graph where this skeleton is attached.
      */
     public BranchGroup getBG()
     {
 
         return _branchGroup;
     }
 
     public void setAnimation(AnimData data)
     {
    	// based on the translational offset of the hips in the first frame of motion
    	// so set the offset to be minus that
    	float[] hipData = data.getBoneData(0);
    	Point3d negStartPos = new Point3d(hipData[3], hipData[4], hipData[5]);
    	this._offset.sub(negStartPos);
    	
         _player = new MocapPlayer(_skeleton, data, _offset);
         _player.setPlaybackFps(data.getFps());
     }
 
     public void setOffset(Point3d p)
     {
         _offset = p;
     }
 }
