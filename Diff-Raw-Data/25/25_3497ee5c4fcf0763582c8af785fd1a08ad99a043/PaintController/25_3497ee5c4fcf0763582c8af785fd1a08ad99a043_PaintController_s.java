 import javax.media.j3d.Transform3D;
 import javax.vecmath.Vector3d;
 
 public class PaintController {
 	/**
 	 * Checks whether the pose of the pen marker is inside the editing volume.
 	 * @param penTranslation the translation of the pen marker
 	 * @param editingVolumeTranslation the translation of the editing volume
 	 * @param radius the radius of the editing volume
 	 * @return true if inside, false if not
 	 */
 	boolean shouldDraw(Vector3d penTranslation, Vector3d editingVolumeTranslation, double radius) {	
 		if (penTranslation == null || editingVolumeTranslation == null) {
 			return false;
 		}
 		
 		Vector3d distanceVec = new Vector3d();
 		distanceVec.sub(penTranslation, editingVolumeTranslation);
 		
 //		System.out.println("distance is " + distanceVec.length());
 		
 		if (distanceVec.length() <= radius) {
 			System.out.println("Inside the editing volume! distanceVec.length =" + distanceVec.length());
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Returns normalized draw coordinates inside the editing volume.
 	 * If shouldDraw() returns true, this is where the pixel should be drawn at.
 	 * @param penTranslation The position of the pen
 	 * @param editingVolumeTranslation position of the editing volume
 	 * @return Draw coordinates in the space of the editing volume, normalized to [0,1]^3
 	 */
 	Vector3d getDrawCoords(Vector3d penTranslation, Vector3d editingVolumeTranslation) {
 		// We probably already know that. But what the hell let's just compute it once more.
 		Vector3d distanceVec = new Vector3d();
 		distanceVec.sub(penTranslation, editingVolumeTranslation);
 		//distanceVec.normalize();
 		return distanceVec;
 	}
 	
 	Transform3D getDrawCoords(Transform3D penTransform, Transform3D editingVolumeTransform) {
		Transform3D ret = new Transform3D(penTransform);
 		ret.invert();
 		//editingVolumeTransform.invert();
 		//editingVolumeTransform.invert();
		ret.mul(ret, editingVolumeTransform);
 		//ret.add(editingVolumeTransform);
 		
 		return ret;
 	}
 }
