 package org.frankversnel.nl.poortjes.component;
 
 import org.frankversnel.nl.poortjes.GameObject;
 
 public class Speed extends Component {
 	private final float distanceInMetersPerSecond;
 	private final float rotationSpeed;
 
 	private float currentDistance = 0f;
 	private float currentRotation = 0f;
 
 	public Speed(GameObject gameObject, float distanceInMetersPerSecond,
 			float rotationSpeed) {
 		super(gameObject);
 
 		this.distanceInMetersPerSecond = distanceInMetersPerSecond;
 		this.rotationSpeed = rotationSpeed;
 	}
 
 	public void rotate(float amount) {
 		assertSpeedModfier(amount);
 		this.currentRotation = rotationSpeed * amount;
 	}
 
 	public void move(float amount) {
 		assertSpeedModfier(amount);
 		this.currentDistance = distanceInMetersPerSecond * amount;
 	}
 
 	public float getDistance() {
 		return this.currentDistance;
 	}
 
 	public float getRotation() {
 		return this.currentRotation;
 	}
 
 	@Override
 	public void remove() {
 		// No need to remove anything.
 	}
 
 	private void assertSpeedModfier(float speedModifierAmount) {
 		assert(speedModifierAmount <= 1f && speedModifierAmount >= -1f)
			: "Speed modifier has to be a value between 1 and -1 but was " +
 			   distanceInMetersPerSecond;
 	}
 
 }
