 package yang.physics.massaggregation;
 
 import yang.graphics.skeletons.CartoonBone;
 import yang.graphics.skeletons.SkeletonCarrier;
 import yang.graphics.skeletons.defaults.NeutralSkeletonCarrier;
 import yang.graphics.skeletons.pose.Posture;
 import yang.math.objects.Point3f;
 import yang.math.objects.matrix.YangMatrix;
 import yang.model.Rect;
 import yang.physics.massaggregation.constraints.Constraint;
 import yang.physics.massaggregation.constraints.DistanceConstraint;
 import yang.physics.massaggregation.elements.Joint;
 import yang.physics.massaggregation.elements.JointConnection;
 import yang.util.YangList;
 
 public class MassAggregation {
 
 	public static int DEFAULT_ACCURACY = 16;
 	protected static SkeletonCarrier NEUTRAL_CARRIER = new NeutralSkeletonCarrier();
 
 	//Properties
 	public String mName = null;
 	public float mFloorFriction = 0.98f;
 	public float mConstantForceX;
 	public float mConstantForceY;
 	public float mConstantForceZ;
 	public float mLowerLimit;
 	public float mLimitForceInwards;
 	public float mLimitForceOutwards;
 	public float mSpeedFactor = 1;
 	public int mAccuracy;
 	public boolean m3D;
 	public float mDefaultJointRadius = 0.1f;
 	public float mDefaultBoneSpring = 10;
 
 	//Objects
 	public SkeletonCarrier mCarrier;
 	public ForceCallback mForceCallback = null;
 
 	//Data
 	public YangList<Joint> mJoints;
 	public YangList<JointConnection> mBones;
 	public YangList<Constraint> mConstraints;
 
 	//State
 //	public float mShiftX = 0;
 //	public float mShiftY = 0;
 //	public float mShiftZ = 0;
 	public float mScale = 1;
 	public YangMatrix mTransform = YangMatrix.IDENTITY.clone();
 	public YangMatrix mInvTransform = YangMatrix.IDENTITY.clone();
 	public YangMatrix mVectorTransform = YangMatrix.IDENTITY.clone();
 	public YangMatrix mInvVectorTransform = YangMatrix.IDENTITY.clone();
 	public boolean mConstraintsActivated;
 	public Posture mCurrentPose;
 	public Point3f mCurJointShift = new Point3f();
 	protected int mCurJointId = 0;
 	public float mCurJointScale = 1;
 
 
 	public MassAggregation() {
 		mJoints = new YangList<Joint>();
 		mBones = new YangList<JointConnection>();
 		mConstraints = new YangList<Constraint>();
 
 		mCarrier = NEUTRAL_CARRIER;
 		m3D = true;
 		mConstraintsActivated = true;
 		mAccuracy = DEFAULT_ACCURACY;
 		mConstantForceX = 0;
 		mConstantForceY = 0;
 		mConstantForceZ = 0;
 		mLimitForceInwards = 100f;
 		mLimitForceOutwards = 50f;
 		mLowerLimit = Float.MIN_VALUE;
 	}
 
 	protected void build() {
 
 	}
 
 	public void refreshTransform() {
 		mTransform.asInverted(mInvTransform.mValues);
 		mInvTransform.asTransposed(mVectorTransform.mValues);
 		mVectorTransform.asInverted(mInvVectorTransform.mValues);
 	}
 
 	public void recalculateConstraints() {
 		for(final Joint joint:mJoints) {
 			joint.recalculate();
 		}
 		for(final Constraint constraint:mConstraints)
 			constraint.recalculate();
 	}
 
 	public void get2DBoundaries(Rect target) {
 		target.set(100000,-100000,-100000,100000);
 		for(final Joint joint:mJoints) {
 			if(joint.mX<target.mLeft)
 				target.mLeft = joint.mX;
 			if(joint.mX>target.mRight)
 				target.mRight = joint.mX;
 			if(joint.mY>target.mTop)
 				target.mTop = joint.mY;
 			if(joint.mY<target.mBottom)
 				target.mBottom = joint.mY;
 		}
 	}
 
 	public int calcAnimatedJointCount() {
 		int count = 0;
 		for(final Joint joint:mJoints) {
 			if(joint.isAnimated()) {
 				count++;
 			}
 		}
 		return count;
 	}
 
 	public Joint addJoint(Joint joint) {
 		joint.setMassAggregation(this);
 		joint.mX = joint.mX*mCurJointScale + mCurJointShift.mX;
 		joint.mY = joint.mY*mCurJointScale + mCurJointShift.mY;
 		joint.mZ = joint.mZ*mCurJointScale + mCurJointShift.mZ;
 		joint.mRadius *= mCurJointScale;
 		mJoints.add(joint);
 		return joint;
 	}
 
 	public Joint addJoint(String name,Joint parent,float x,float y,float z) {
 		final Joint newJoint = new Joint(name, parent, x,y, mDefaultJointRadius);
 		newJoint.mZ = z;
 		return addJoint(newJoint);
 	}
 
 	public Joint addJoint(Joint parent,float x,float y,float z) {
 		return addJoint("JOINT",parent,x,y,z);
 	}
 
 	public void addConstraint(Constraint constraint) {
 		mConstraints.add(constraint);
 	}
 
 	public <ConnectionType extends JointConnection> ConnectionType addSpringBone(ConnectionType bone,float constraintDistanceStrength) {
 		mBones.add(bone);
 		if(constraintDistanceStrength>0)
 			addConstraint(new DistanceConstraint(bone,constraintDistanceStrength));
 		return bone;
 	}
 
 	public <ConnectionType extends JointConnection> ConnectionType addSpringBone(ConnectionType bone) {
 		return addSpringBone(bone,mDefaultBoneSpring);
 	}
 
 	public <ConnectionType extends JointConnection> ConnectionType addBone(ConnectionType bone) {
 		return addSpringBone(bone);
 	}
 
 	public Joint getJointByName(String name) {
 		//name = name.toUpperCase();
 		for(final Joint joint:mJoints) {
 			if(joint.mName.equals(name))
 				return joint;
 		}
 		return null;
 	}
 
 	public JointConnection getConnectionByName(String name) {
 		//name = name.toUpperCase();
 		for(final JointConnection connection:mBones) {
 			if(connection.mName.equals(name))
 				return connection;
 		}
 		return null;
 	}
 
 //	public DistanceConstraint getDistanceConstraint(Joint joint1,Joint joint2) {
 //		for(Constraint constraint:mConstraints) {
 //			if(constraint instanceof DistanceConstraint) {
 //				DistanceConstraint distConstr = (DistanceConstraint)constraint;
 //				if((distConstr.mBone.)
 //				return ()
 //			}
 //		}
 //		return null;
 //	}
 
 	public DistanceConstraint getDistanceConstraint(JointConnection connection) {
 		for(Constraint constraint:mConstraints) {
 			if(constraint instanceof DistanceConstraint) {
 				DistanceConstraint distConstr = (DistanceConstraint)constraint;
 				if(distConstr.mBone==connection)
 					return distConstr;
 			}
 		}
 		return null;
 	}
 
 	public DistanceConstraint getDistanceConstraintByName(String name) {
 		return getDistanceConstraint(getConnectionByName(name));
 	}
 
 	public DistanceConstraint getDistanceConstraint(Joint joint1,Joint joint2) {
 		return getDistanceConstraint(getJointConnection(joint1,joint2));
 	}
 
 	public JointConnection getJointConnection(Joint joint1,Joint joint2) {
 		for(JointConnection connection:mBones) {
 			if((connection.mJoint1==joint1 && connection.mJoint2==joint2) || (connection.mJoint2==joint1 && connection.mJoint1==joint2))
 				return connection;
 		}
 		return null;
 	}
 
 	public float getJointWorldX(Joint joint) {
 		//return mCarrier.getWorldX() + (mShiftX + joint.mPosX)*mCarrier.getScale()*mScale;
 		return mCarrier.getWorldX() + joint.mWorldPosition.mX*mCarrier.getScale();
 	}
 
 	public float getJointWorldY(Joint joint) {
 		return mCarrier.getWorldY() + joint.mWorldPosition.mY*mCarrier.getScale();
 	}
 
 	public float getJointWorldZ(Joint joint) {
 		return mCarrier.getWorldZ() + joint.mWorldPosition.mZ*mCarrier.getScale();
 	}
 
 	public float toJointX(float x) {
 		return (x-mCarrier.getWorldX())*mCarrier.getScale();
 	}
 
 	public float toJointY(float y) {
 		return (y-mCarrier.getWorldY())*mCarrier.getScale();
 	}
 
 	public void physicalStep(float deltaTime) {
 
 		final float uDeltaTime = deltaTime/mAccuracy;
 		final float worldY = mCarrier.getWorldY();
 		int stepCount = (int)(mAccuracy * mSpeedFactor);
 		for(int i=0;i<stepCount;i++) {
 
 			//Init force
 			for(final Joint joint:mJoints) {
 				if(joint.mFixed) {
 					joint.mForceX = 0;
 					joint.mForceY = 0;
 					joint.mForceZ = 0;
 				}else{
 					joint.mForceX = mConstantForceX*joint.mMass;
 					joint.mForceY = mConstantForceY*joint.mMass;
 					joint.mForceZ = mConstantForceZ*joint.mMass;
 				}
 
 				if(joint.mY+worldY<mLowerLimit) {
 					final float uForce = (joint.mVelY<0)?mLimitForceInwards:mLimitForceOutwards;
 					joint.mForceY += (mLowerLimit-joint.mY)*uForce;
 					joint.mVelX *= mFloorFriction;
 				}
 			}
 
 			if(mForceCallback!=null) {
 				mForceCallback.preApply(uDeltaTime);
 			}
 
 			//Apply constraints
 			if(mConstraintsActivated) {
 				for(final Constraint constraint:mConstraints) {
 					constraint.apply();
 				}
 
 				for(final Joint joint:mJoints) {
 					joint.applyConstraint();
 				}
 			}
 
 			for(final Joint joint:mJoints) {
 				joint.physicalStep(uDeltaTime);
 			}
 
 		}
 
 		refreshJointWorldPositions();
 	}
 
 	public void reApplyPose() {
 		if(mCurrentPose!=null)
 			mCurrentPose.applyPosture(this);
 	}
 
 	@SuppressWarnings("unchecked")
 	public <ConstraintType extends Constraint> ConstraintType getBoneConstraint(CartoonBone bone,Class<ConstraintType> type) {
 		for(final Constraint constraint:mConstraints) {
 			if((constraint.getClass()==type) && (constraint.containsBone(bone)))
 				return (ConstraintType)constraint;
 		}
 		return null;
 	}
 
 	public void reset() {
 		for(final Joint joint:mJoints) {
 			joint.reset();
 		}
 	}
 
 	public void setFriction(float friction) {
 		for(final Joint joint:mJoints) {
 			joint.mFriction = friction;
 		}
 	}
 
 	public int getNextJointId() {
 		return mCurJointId++;
 	}
 
 	public void setCarrier(SkeletonCarrier carrier) {
 		mCarrier = carrier;
 	}
 
 	public void refreshGeometry() {
 		for(final JointConnection connection:mBones) {
 			connection.refreshGeometry();
 		}
 	}
 
 	public void transformJointPositions(YangMatrix transform) {
 		for(Joint joint:mJoints) {
 			joint.applyTransform(transform);
 		}
 	}
 
 	public void refreshJointWorldPositions() {
 		if(mTransform!=null)
 			for(Joint joint:mJoints) {
 				joint.refreshWorldPosition();
 			}
 	}
 
 	public int getJointCount() {
 		return mJoints.size();
 	}
 
 	public void clearForces() {
 		for(Joint joint:mJoints) {
 			joint.mForceX = 0;
 			joint.mForceY = 0;
 			joint.mForceZ = 0;
 		}
 	}
 
 	public void clearVelocities() {
 		for(Joint joint:mJoints) {
 			joint.mVelX = 0;
 			joint.mVelY = 0;
 			joint.mVelZ = 0;
 		}
 	}
 
 	public float getScale() {
 		return mCarrier.getScale()*mScale;
 	}
 
 	public void clear() {
 		mJoints.clear();
 		mBones.clear();
 		mConstraints.clear();
 	}
 
 	public float getJointMinY() {
 		float minVal = Float.MAX_VALUE;
 		for(Joint joint:mJoints) {
 			if(joint.mY<minVal)
 				minVal = joint.mY;
 		}
 		return minVal;
 	}
 
 }
