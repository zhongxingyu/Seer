 package yang.physics.massaggregation.constraints;
 
 import yang.math.objects.Point3f;
 import yang.math.objects.Vector3f;
 import yang.physics.massaggregation.MassAggregation;
 import yang.physics.massaggregation.elements.Joint;
 import yang.util.YangList;
 
 public class PlaneConstraint extends Constraint{
 
 	public static float FIXED_FACTOR = 2;
 
 	public YangList<Joint> mJoints = new YangList<Joint>();
 	public Point3f mBasePosition = new Point3f();
 	public Vector3f mPlaneVector = new Vector3f(0,1,0);
 	private Vector3f mTempVec = new Vector3f();
	public float mStrength = 100;
 
 	public PlaneConstraint(Point3f basePosition,Vector3f planeVector) {
 		mBasePosition.set(basePosition);
 		mPlaneVector.set(planeVector);
 	}
 
 	public PlaneConstraint() {
 
 	}
 
 	public void addJoint(Joint joint) {
 		mJoints.add(joint);
 	}
 
 	@Override
 	public void apply() {
 		for(Joint joint:mJoints) {
			mTempVec.setFromTo(mBasePosition,joint);
 			float dot = mTempVec.dot(mPlaneVector);
			float f = -dot*mStrength;
 			joint.mForceX += mPlaneVector.mX*f;
 			joint.mForceY += mPlaneVector.mY*f;
 			joint.mForceZ += mPlaneVector.mZ*f;
 		}
 	}
 
 	@Override
 	public Constraint cloneInto(MassAggregation target) {
		PlaneConstraint newConstraint = new PlaneConstraint();
		for(Joint joint:mJoints) {
			newConstraint.addJoint(target.getJointByName(joint.mName));
		}
		newConstraint.mBasePosition.set(mBasePosition);
		newConstraint.mPlaneVector.set(mPlaneVector);
		newConstraint.mStrength = mStrength;
		return newConstraint;
 	}
 
 }
