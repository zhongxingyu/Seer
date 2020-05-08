 /**
  *
  */
 package ca.eandb.jmist.framework.measurement;
 
 import ca.eandb.jmist.math.MathUtil;
 import ca.eandb.jmist.math.SphericalCoordinates;
 import ca.eandb.jmist.math.Vector3;
 
 /**
  * A <code>CollectorSphere</code> where each stack of sensors spans the same
  * polar angle.
  * @author Brad Kimmel
  */
 public final class EqualPolarAnglesCollectorSphere implements CollectorSphere {
 
 	/**
 	 * Creates a new <code>EqualPolarAnglesCollectorSphere</code>.
 	 * @param stacks The number of stacks to divide each hemisphere into.
 	 * @param slices The number of slices to divide the sphere into (about the
 	 * 		azimuthal angle).
 	 * @param upper A value indicating whether to record hits for the upper
 	 * 		hemisphere.
 	 * @param lower A value indicating whether to record hits for the lower
 	 * 		hemisphere.
 	 * @throws IllegalArgumentException if both <code>upper</code> and
 	 * 		<code>lower</code> are <code>false</code>.
 	 */
 	public EqualPolarAnglesCollectorSphere(int stacks, int slices, boolean upper, boolean lower) {
 
 		if (!upper && !lower) {
 			throw new IllegalArgumentException("One of upper or lower must be true.");
 		}
 
 		int hemispheres = (upper ? 1 : 0) + (lower ? 1 : 0);
 
 		/* Each stack has "slices" sensors, except for the ones at the very top
 		 * of the upper hemisphere and the bottom of the lower hemisphere,
 		 * which have one (circular) patch.
 		 */
 		this.sensors = hemispheres * ((stacks - 1) * slices + 1);
 
 		this.stacks = stacks;
 		this.slices = slices;
 		this.upper = upper;
 		this.lower = lower;		
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#getSensorCenter(int)
 	 */
 	public SphericalCoordinates getSensorCenter(int sensor) {
 
 		int hemispheres = (upper ? 1 : 0) + (lower ? 1 : 0);
 		assert(hemispheres > 0);
 
 		int patchesPerHemisphere = this.sensors() / hemispheres;
 
 		/* The sensor is on the lower hemisphere if there is no upper
 		 * hemisphere or if the sensor ID is in the second half of the
 		 * sensors.
 		 */
 		boolean sensorOnLower = !upper || sensor >= patchesPerHemisphere;
 
 		/* If both hemispheres are present, and we are computing the center of
 		 * a patch on the lower hemisphere, then compute the center of the
 		 * corresponding patch on the upper hemisphere and then adjust it
 		 * after.
 		 */
 		if (upper && lower && sensorOnLower) {
 			sensor = this.sensors() - 1 - sensor;
 		}
 
 		int stack;
 		int slice;
 
 		if (sensor == 0) {
 
 			/* The first sensor is the one at the top (or bottom). */
 			stack = slice = 0;
 
 		} else { /* sensor > 0 */
 
 			stack = (sensor - 1) / slices + 1;
 			slice = (sensor - 1) % slices;
 
 			if (this.upper && this.lower && sensorOnLower) {
 				slice = slices - 1 - slice;
 			}
 
 		}
 
 		double phi = 2.0 * Math.PI * ((double) slice / (double) slices);
		double theta1 = 0.5 * Math.PI * ((double) stack / (double) stacks);
		double theta2 = 0.5 * Math.PI * ((double) (stack + 1) / (double) stacks);
		double z1 = Math.cos(theta1);
		double z2 = Math.cos(theta2);
		double z = 0.5 * (z1 + z2);
		double theta = Math.acos(z);
 		
 		if (sensorOnLower) {
 			theta = Math.PI - theta;
 		}
 
 		return SphericalCoordinates.canonical(theta, phi);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#getSensorProjectedSolidAngle(int)
 	 */
 	public double getSensorProjectedSolidAngle(int sensor) {
 
 		int hemispheres = (upper ? 1 : 0) + (lower ? 1 : 0);
 		int patchesPerHemisphere = this.sensors() / hemispheres;
 
 		/* The sensor is on the lower hemisphere if there is no upper
 		 * hemisphere or if the sensor ID is in the second half of the
 		 * sensors.
 		 */
 		boolean sensorOnLower = !upper || sensor >= patchesPerHemisphere;
 
 		/* If both hemispheres are present, and we are computing the projected
 		 * solid angle of a patch on the lower hemisphere, then compute the
  		 * projected solid angle of the corresponding patch on the upper
  		 * hemisphere (they will be equal).
 		 */
 		if (upper && lower && sensorOnLower) {
 			sensor = this.sensors() - 1 - sensor;
 		}
 
 		assert(0 <= sensor && sensor < patchesPerHemisphere);
 
 		int stack = (sensor > 0) ? (sensor - 1) / slices + 1 : 0;
 
 		double theta1 = 0.5 * Math.PI * ((double) stack / (double) stacks);
 		double theta2 = 0.5 * Math.PI * ((double) (stack + 1) / (double) stacks);
 
 		return sensor > 0 ?
 				0.5 * Math.PI * (Math.cos(2.0 * theta1) - Math.cos(2.0 * theta2)) / (double) slices :
 				0.5 * Math.PI * (Math.cos(2.0 * theta1) - Math.cos(2.0 * theta2));
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#getSensorSolidAngle(int)
 	 */
 	public double getSensorSolidAngle(int sensor) {
 
 		int hemispheres = (upper ? 1 : 0) + (lower ? 1 : 0);
 		int patchesPerHemisphere = this.sensors() / hemispheres;
 
 		/* The sensor is on the lower hemisphere if there is no upper
 		 * hemisphere or if the sensor ID is in the second half of the
 		 * sensors.
 		 */
 		boolean sensorOnLower = !upper || sensor >= patchesPerHemisphere;
 
 		/* If both hemispheres are present, and we are computing the projected
 		 * solid angle of a patch on the lower hemisphere, then compute the
  		 * projected solid angle of the corresponding patch on the upper
  		 * hemisphere (they will be equal).
 		 */
 		if (upper && lower && sensorOnLower) {
 			sensor = this.sensors() - 1 - sensor;
 		}
 
 		assert(0 <= sensor && sensor < patchesPerHemisphere);
 
 		int stack = (sensor > 0) ? (sensor - 1) / slices + 1 : 0;
 			
 		double theta1 = 0.5 * Math.PI * ((double) stack / (double) stacks);
 		double theta2 = 0.5 * Math.PI * ((double) (stack + 1) / (double) stacks);
 
 		return sensor > 0 ?
 				2.0 * Math.PI * (Math.cos(theta1) - Math.cos(theta2)) / (double) slices :
 				2.0 * Math.PI * (Math.cos(theta1) - Math.cos(theta2));
 
 	}
 
 	private int getSensor(SphericalCoordinates v) {
 		v = v.canonical();
 
 		double theta = v.polar();
 		double phi = v.azimuthal();
 		boolean hitUpper = theta < (0.5 * Math.PI);
 
 		if ((hitUpper && !upper) || (!hitUpper && !lower)) {
 			return -1;
 		}
 
 		theta = upper ? theta : Math.PI - theta;
 
 		int stack = MathUtil.clamp(
 				(int) Math.floor((double) stacks * theta / (0.5 * Math.PI)),
 				0, 2 * stacks - 1);
 		
 		if (stack == 0) {
 			return 0;
 		} else if (stack == 2 * stacks - 1) {
 			return sensors() - 1;
 		} else { /* 0 < stack < 2 * stacks - 1 */
 			phi += Math.PI / (double) slices;
 			
 			if (phi < 0.0) phi += 2.0 * Math.PI;
 			if (phi >= 2.0 * Math.PI) phi -= 2.0 * Math.PI;
 			
 			int slice = (int) ((double) slices * (phi / (2.0 * Math.PI)));
 			slice = MathUtil.clamp(slice, 0, slices - 1);
 			
 			return 1 + (stack - 1) * slices + slice;
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#record(ca.eandb.jmist.math.Vector3, ca.eandb.jmist.framework.measurement.CollectorSphere.Callback)
 	 */
 	public void record(Vector3 v, Callback f) {
 		record(SphericalCoordinates.fromCartesian(v), f);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#record(ca.eandb.jmist.math.SphericalCoordinates, ca.eandb.jmist.framework.measurement.CollectorSphere.Callback)
 	 */
 	public void record(SphericalCoordinates v, Callback f) {
 		int sensor = getSensor(v);
 		if (sensor >= 0) {
 			f.record(sensor);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#sensors()
 	 */
 	public int sensors() {
 		return sensors;
 	}
 
 	/** The total number of sensors. */
 	private final int sensors;
 
 	/** The number of stacks per hemisphere. */
 	private final int stacks;
 
 	/** The number of slices. */
 	private final int slices;
 
 	/** A value indicating whether the upper hemisphere is measured. */
 	private final boolean upper;
 
 	/** A value indicating whether the lower hemisphere is measured. */
 	private final boolean lower;
 
 	/**
 	 * Serialization version ID.
 	 */
 	private static final long serialVersionUID = 6947672588017728172L;
 
 }
