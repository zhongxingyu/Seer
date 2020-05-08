 package org.ggj2013;
 
 import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
 import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
 
 import android.util.Log;
 
 public class Player extends Entity {
 
 	public Player(String name) {
 		super(name);
 	}
 
	float HEARING_MINIMUM = 1f;
 	int heartbeatLevel = 1;
 
 	public float[] getBalanceForSoundFrom(Entity e) {
 		Vector3D normalized = relativeOrientationFor(e);
 
 		float[] balance = new float[2];
 
 		balance[0] = 0.5f - (float) normalized.getX() * 0.5f;
 		balance[1] = 0.5f + (float) normalized.getX() * 0.5f;
 
 		float distance = distanceTo(e);
 		float distanceVolume = 1;
 
 		if (distance > HEARING_MINIMUM) {
 			distanceVolume = 1f - ((distance - HEARING_MINIMUM) / (e.volume() - HEARING_MINIMUM));
 		}
 
 		distanceVolume = (float) Math.pow(
 				MathUtils.clamp(distanceVolume, 0, 1), 2);
 
 		if (normalized.getY() < 0) {
			/*
			 * if (balance[0] <= 0.5f) { balance[0] = 0; } else if (balance[1]
			 * <= 0.5f) { balance[1] = 0; }
			 */
 
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
 
 		if (toOther.getNormSq() == 0) {
 			return Entity.FORWARD;
 		} else {
 			return lookToOrigin.applyTo(toOther.normalize());
 		}
 	}
 
 	public boolean doCollision(float minX, float minY, float maxX, float maxY) {
 		if (MathUtils.ComputeOutCode(position.getX(), position.getY(), minX,
 				minY, maxX, maxY) == MathUtils.INSIDE) {
 			return false;
 		}
 
 		float centerX = minX + (maxX - minX) * 0.5f;
 		float centerY = minY + (maxY - minY) * 0.5f;
 
 		double[] collision = MathUtils.CohenSutherlandLineClipAndDraw(centerX,
 				centerY, position.getX(), position.getY(), minX, minY, maxX,
 				maxY);
 
 		if (collision == null) {
 			throw new RuntimeException(
 					"Line from center of room to player was outside of screen!?");
 		}
 
 		position = new Vector3D(collision[2], collision[3], 0);
 
 		Log.i("Player", "Collsion moved player to " + position.toString());
 
 		return true;
 	}
 
 	public void playHeartBeatSound(SoundManager soundManager) {
 		if (heartbeatLevel == 5) {
 			soundManager.play("player_heartbeat",
 					SoundPackStandard.HEARTBEAT_05,
 					SoundManager.BALANCE_CENTER, 1f,
 					SoundManager.LOOPS_INFINITE);
 
 		} else if (heartbeatLevel == 4) {
 			soundManager.play("player_heartbeat",
 					SoundPackStandard.HEARTBEAT_04,
 					SoundManager.BALANCE_CENTER, 0.7f,
 					SoundManager.LOOPS_INFINITE);
 		} else if (heartbeatLevel == 3) {
 			soundManager.play("player_heartbeat",
 					SoundPackStandard.HEARTBEAT_03,
 					SoundManager.BALANCE_CENTER, 0.5f,
 					SoundManager.LOOPS_INFINITE);
 		} else if (heartbeatLevel == 2) {
 			soundManager.play("player_heartbeat",
 					SoundPackStandard.HEARTBEAT_02,
 					SoundManager.BALANCE_CENTER, 0.3f,
 					SoundManager.LOOPS_INFINITE);
 		} else {
 			soundManager.play("player_heartbeat",
 					SoundPackStandard.HEARTBEAT_01,
 					SoundManager.BALANCE_CENTER, 0.2f,
 					SoundManager.LOOPS_INFINITE);
 		}
 	}
 }
