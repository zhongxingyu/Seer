 package org.ggj2013;
 
 import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
 import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
 
 public class Player extends Entity {
 
 	float HEARING_MINIMUM = 3f;
 	float HEARING_MAXIMUM = 12f;
 
 	public float[] getBalanceForSoundFrom(Entity e) {
 		Vector3D normalized = relativeOrientationFor(e);
 
 		float[] balance = new float[2];
 
		balance[0] = 0.5f - (float) normalized.getX() * 0.5f;
		balance[1] = 0.5f + (float) normalized.getX() * 0.5f;
 
 		float distance = (float) distanceTo(e);
 		float distanceVolume = 1;
 
 		if (distance < HEARING_MINIMUM) {
 			distanceVolume = 1f;
 		} else {
 			distanceVolume = 1f - ((distance - HEARING_MINIMUM) / (HEARING_MAXIMUM - HEARING_MINIMUM));
 		}
 
 		distanceVolume = (float) Math.pow(
 				Math.max(0, Math.min(1, distanceVolume)), 2);
 
 		if (normalized.getY() < 0) {
 			if (balance[0] <= 0.5f) {
 				balance[0] = 0;
 			} else if (balance[1] <= 0.5f) {
 				balance[1] = 0;
 			}
 
 			distanceVolume *= (float) Math
 					.max(0, 1f + normalized.getY() * 1.3f);
 		}
 
 		balance[0] *= distanceVolume;
 		balance[1] *= distanceVolume;
 
 		return balance;
 	}
 
 	public Vector3D relativeOrientationFor(Entity e) {
 		Vector3D look = getLookDirection();
 		Rotation lookToOrigin = new Rotation(look, Entity.FORWARD);
 		Vector3D toOther = vectorTo(e);
 		return lookToOrigin.applyTo(toOther.normalize());
 	}
 }
