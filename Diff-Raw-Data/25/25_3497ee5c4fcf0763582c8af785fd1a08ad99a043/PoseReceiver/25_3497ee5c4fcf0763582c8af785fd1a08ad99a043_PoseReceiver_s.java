 import javax.vecmath.Quat4d;
 import javax.vecmath.Vector3d;
 
 import ubitrack.SimplePose;
 import ubitrack.SimplePoseReceiver;
 
 /**
  * Register with ubitrack to receive pose updates. The received positional
  * and rotational information is then saved for later access. Every time an
  * update occurs a notification will be sent via callback to the creating class
  * (using the PoseUpdatedNotification interface).
  * @author bader
  */
 public class PoseReceiver extends SimplePoseReceiver {
 	/** The latest known ubitrack pose. */
 	private SimplePose pose = null;
 	
 	/** The latest known translation of the pose. */
 	private Vector3d translationVector = null;
 	
 	/** The latest known rotation of the pose. */
 	private Quat4d rotationQuaternion = null;
 	
 	/** The object that receives update notifications. */
 	private PoseUpdatedNotification updateReceiver = null;
 	
 	/** Just a number you can set when creating the object.
 	 * Use this for convenience in the update notification callback
 	 * to find out which tracked oject updated its position. */
 	private int tag = -1;
 	
 	public PoseReceiver(PoseUpdatedNotification updateReceiver, int tag) {
 		this.updateReceiver = updateReceiver;
 		this.tag = tag;
 		
 		System.out.println("Registered PoseReceiver with tag " + tag);
 	}
 	
 	public PoseReceiver() {
 		
 	}
 	
 	public SimplePose getPose() {
 		return pose;
 	}
 
 	public Vector3d getTranslationVector() {
 		return translationVector;
 	}
 	
 	public Quat4d getRotationQuaternion() {
 		return rotationQuaternion;
 	}
 	
 	public int getTag() {
 		return tag;
 	}
 	
 	public void receivePose(SimplePose pose) {
 		this.pose = pose;
 		translationVector = Utils.poseTranslationToVector3d(pose);
 		rotationQuaternion = Utils.poseRotationToQuat4d(pose);
 		
 		if (updateReceiver != null) {
 			updateReceiver.handlePoseUpdatedNotification(this);
 		}
 	}
 }
