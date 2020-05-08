 package yang.physics.massaggregation;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
import java.util.Iterator;
 
 import yang.graphics.defaults.DefaultGraphics;
 import yang.graphics.skeletons.SkeletonCarrier;
 import yang.graphics.skeletons.defaults.NeutralSkeletonCarrier;
 import yang.graphics.skeletons.pose.Posture;
 import yang.graphics.translator.GraphicsTranslator;
 import yang.graphics.translator.Texture;
 import yang.math.objects.Point3f;
 import yang.math.objects.YangMatrix;
 import yang.model.Rect;
 import yang.physics.massaggregation.constraints.Constraint;
 import yang.physics.massaggregation.constraints.DistanceConstraint;
 import yang.physics.massaggregation.elements.Joint;
 import yang.physics.massaggregation.elements.JointConnection;
 import yang.util.YangList;
 
 public class MassAggregation {
 
 	public static Texture JOINT_DEBUG_TEXTURE;
 
 	public static int DEFAULT_ACCURACY = 16;
 	protected static SkeletonCarrier NEUTRAL_CARRIER = new NeutralSkeletonCarrier();
 
 	//Properties
 	public String mName = null;
 	public float mFloorFriction = 0.98f;
 	public float mConstantForceX;
 	public float mConstantForceY;
 	public float mConstantForceZ;
 	public float mLowerLimit;
 	public float mFarLimit = -128;
 	public float mLimitForceInwards;
 	public float mLimitForceOutwards;
 	public float mSpeedFactor = 1;
 	public int mAccuracy;
 	public boolean m3D;
 	public float mDefaultJointRadius = 0.1f;
 	public float mDefaultBoneSpring = 10;
 	public float mJointRadiusOutputFactor = 1;
 
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
 
 	public static void getRigidTransform(YangMatrix target,Joint baseJoint, Joint rightJoint, Joint topJoint, Joint frontJoint) {
 		target.setFromAxis(baseJoint, rightJoint, topJoint, frontJoint, true);
 	}
 
 	public static void getAverageWorldPosition(Point3f targetPoint, Joint[] joints) {
 		if(joints.length<1)
 			throw new RuntimeException("Empty joint array");
 		targetPoint.setZero();
 		for(Joint joint:joints) {
 			targetPoint.mX += joint.getWorldX();
 			targetPoint.mY += joint.getWorldY();
 			targetPoint.mZ += joint.getWorldZ();
 		}
 		targetPoint.scale(1f/joints.length);
 	}
 
 	public MassAggregation() {
		mJoints = new YangList<Joint>(16);
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
 		mVectorTransform.mValues[3] = 0;
 		mVectorTransform.mValues[7] = 0;
 		mVectorTransform.mValues[11] = 0;
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
 		target.set(100000,100000,-100000,-100000);
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
 			if(joint.mAnimate) {
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
 
 	public void addJoints(YangList<Joint> joints) {
 		for(Joint joint:joints)
 			addJoint(joint);
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
 
 	public DistanceConstraint addSpringBoneGetSpring(JointConnection bone,float constraintDistanceStrength) {
 		mBones.add(bone);
 		DistanceConstraint constraint = new DistanceConstraint(bone,constraintDistanceStrength);
 		if(constraintDistanceStrength>0) {
 			addConstraint(constraint);
 		}else
 			constraint = null;
 		return constraint;
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
 		return (x-mCarrier.getWorldX())*mCarrier.getScale()*mScale;
 	}
 
 	public float toJointY(float y) {
 		return (y-mCarrier.getWorldY())*mCarrier.getScale()*mScale;
 	}
 
 	public void calcForces() {
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
 
 			if(joint.mY+mCarrier.getWorldY()<mLowerLimit) {
 				final float uForce = (joint.mVelY<0)?mLimitForceInwards:mLimitForceOutwards;
 				joint.mForceY += (mLowerLimit-joint.mY)*uForce;
 				joint.mVelX *= mFloorFriction;
 				joint.mVelZ *= mFloorFriction;
 			}
 			if(joint.mZ+mCarrier.getWorldZ()<mFarLimit) {
 				final float uForce = (joint.mVelZ<0)?mLimitForceInwards:mLimitForceOutwards;
 				joint.mForceZ += (mFarLimit-joint.mZ)*uForce;
 				joint.mVelX *= mFloorFriction;
 				joint.mVelY *= mFloorFriction;
 			}
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
 	}
 
 	public void jointsStep(float deltaTime) {
 		for(final Joint joint:mJoints) {
 //			if(joint.mForceZ!=0)
 //				System.out.println(joint);
 			joint.physicalStep(deltaTime);
 
 		}
 	}
 
 	public void physicalStep(float deltaTime) {
 
 		final float uDeltaTime = deltaTime/mAccuracy;
 		int stepCount = (int)(mAccuracy * mSpeedFactor);
 		for(int i=0;i<stepCount;i++) {
 
 			calcForces();
 
 			if(mForceCallback!=null) {
 				refreshJointWorldPositions();
 				mForceCallback.preApply(uDeltaTime);
 			}
 
 			jointsStep(uDeltaTime);
 
 		}
 
 		refreshJointWorldPositions();
 	}
 
 	public void reApplyPose() {
 		if(mCurrentPose!=null)
 			mCurrentPose.applyPosture(this);
 	}
 
 	@SuppressWarnings("unchecked")
 	public <ConstraintType extends Constraint> ConstraintType getBoneConstraint(JointConnection bone,Class<ConstraintType> type) {
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
 		mCurJointId = 0;
 	}
 
 	public float getJointMinY() {
 		float minVal = Float.MAX_VALUE;
 		for(Joint joint:mJoints) {
 			if(joint.mY<minVal)
 				minVal = joint.mY;
 		}
 		return minVal;
 	}
 
 	public void setDragDelay(float delay) {
 		for(Joint joint:mJoints) {
 			joint.mDragDelay = delay;
 		}
 	}
 
 	public void connectAll() {
 		int i = 0;
 		for(Joint joint1:mJoints) {
 			int j = 0;
 			mJoints.incIteratorIndex();
 			for(Joint joint2:mJoints) {
 				if(j>i) {
 					if(getDistanceConstraint(joint1,joint2)==null) {
 						addSpringBone(new JointConnection("ALL_"+joint1.mName+"-"+joint2.mName,joint1,joint2));
 					}
 				}
 				j++;
 			}
 			mJoints.decIteratorIndex();
 			i++;
 		}
 	}
 
 	public void appendMassAggregation(MassAggregation massAggregation) {
 		int idOffset = mJoints.size();
 		for(Joint joint:massAggregation.mJoints) {
 			addJoint(joint.clone());
 		}
 
 		for(Joint joint:massAggregation.mJoints) {
 			if(joint.mParent!=null) {
 				Joint joint1 = getJointByName(joint.mName);
 				Joint parent = getJointByName(joint.mParent.mName);
 				joint1.setParent(parent);
 			}
 		}
 
 		for(JointConnection bone:massAggregation.mBones) {
 			bone.cloneInto(this);
 		}
 		for(Constraint constraint:massAggregation.mConstraints) {
 			constraint.cloneInto(this);
 		}
 	}
 
 	public void copyFrom(MassAggregation massAggregation) {
 		clear();
 		appendMassAggregation(massAggregation);
 		mTransform.set(massAggregation.mTransform);
 		mLowerLimit = massAggregation.mLowerLimit;
 		mConstantForceX = massAggregation.mConstantForceX;
 		mConstantForceY = massAggregation.mConstantForceY;
 		mConstantForceZ = massAggregation.mConstantForceZ;
 		mAccuracy = massAggregation.mAccuracy;
 		mCarrier = massAggregation.mCarrier;
 		mScale = massAggregation.mScale;
 		m3D = massAggregation.m3D;
 	}
 
 	@Override
 	public MassAggregation clone() {
 		MassAggregation result = new MassAggregation();
 		result.copyFrom(this);
 		return result;
 	}
 
 	@Override
 	public String toString() {
 		return "----"+mName+"----\n"+"JOINTS: "+mJoints+"\nCONNECTIONS: "+mBones;
 	}
 
 	public void setFixed(boolean b) {
 		for(Joint joint:mJoints) {
 			joint.mFixed = true;
 		}
 	}
 
 	public static void drawDebug2D(MassAggregation massAggregation,DefaultGraphics<?> graphics,SkeletonEditing skeletonEditing,float offsetX,float offsetY,int lookDirection) {
 		Joint markedJoint;
 		if(skeletonEditing==null)
 			markedJoint = null;
 		else
 			markedJoint = skeletonEditing.mMainMarkedJoint;
 		final float worldPosX = massAggregation.mCarrier.getWorldX() + offsetX;
 		final float worldPosY = massAggregation.mCarrier.getWorldY() + offsetY;
 		final float scale = massAggregation.mCarrier.getScale()*massAggregation.mScale;
 		final int mirrorFac = lookDirection;
 
 		final GraphicsTranslator translator = graphics.mTranslator;
 		graphics.setDefaultProgram();
 		graphics.setColorFactor(1);
 
 		massAggregation.refreshJointWorldPositions();
 
 		translator.bindTexture(null);
 		graphics.setColor(0.8f,0.1f,0, 0.8f);
 		for(final Joint joint:massAggregation.mJoints) {
 			if(joint.mEnabled && joint.mParent!=null) {
 				graphics.drawLine(
 						worldPosX + joint.mWorldPosition.mX * mirrorFac, worldPosY + joint.mWorldPosition.mY,
 						worldPosX + joint.mParent.mWorldPosition.mX * mirrorFac, worldPosY + joint.mParent.mWorldPosition.mY,
 						0.015f
 						);
 			}
 		}
 
 		graphics.setColor(0.8f,0.8f,0.8f,0.6f);
 		for(JointConnection bone:massAggregation.mBones) {
 			if(!bone.connectsChildParent()) {
 				Joint joint1 = bone.mJoint1;
 				Joint joint2 = bone.mJoint2;
 				if(joint1.mEnabled && joint2.mEnabled) {
 					graphics.drawLine(
 							worldPosX + joint1.mWorldPosition.mX * mirrorFac, worldPosY + joint1.mWorldPosition.mY,
 							worldPosX + joint2.mWorldPosition.mX * mirrorFac, worldPosY + joint2.mWorldPosition.mY,
 							0.015f
 							);
 				}
 			}
 		}
 
 		translator.bindTexture(JOINT_DEBUG_TEXTURE);
 		for(final Joint joint:massAggregation.mJoints)
 			if(joint.mEnabled){
 				final float alpha = (markedJoint==joint)?1:0.6f;
 				if(joint.mFixed)
 					graphics.setColor(1, 0, 0, alpha);
 				else
 					graphics.setColor(0.8f,0.8f,0.8f,alpha);
 				if(!joint.isAnimated()) {
 					graphics.multColor(0.55f);
 				}
 				graphics.drawRectCentered(worldPosX + joint.mWorldPosition.mX * mirrorFac, worldPosY + joint.mWorldPosition.mY, joint.getOutputRadius()*2*scale);
 			}
 
 	}
 
 	public float getMaxVel() {
 		float maxVel = 0;
 		for(Joint joint:mJoints) {
 			float v = joint.calcSqrVelocity();
 			if(v>maxVel)
 				maxVel = v;
 		}
 		return (float)Math.sqrt(maxVel);
 	}
 
 	public void drawDebug2D(DefaultGraphics<?> graphics,SkeletonEditing skeletonEditing) {
 		drawDebug2D(this,graphics,skeletonEditing,0,0,1);
 	}
 
 	public void createRigidBody(Joint[] joints,String namePrefix,float strength) {
 		for(Joint joint1:joints) {
 			for(Joint joint2:joints) {
 				if(joint1==joint2)
 					continue;
 				addSpringBone(new JointConnection(namePrefix+joint1.mName+"-"+joint2.mName,joint1,joint2),strength);
 			}
 		}
 	}
 
 	public void linkTransform(MassAggregation skeleton) {
 		mTransform = skeleton.mTransform;
 		mInvTransform = skeleton.mInvTransform;
 		mVectorTransform = skeleton.mVectorTransform;
 		mInvVectorTransform = skeleton.mInvVectorTransform;
 	}
 
 	public void writePosture(DataOutputStream stream) throws IOException {
		Iterator<Joint> iter = mJoints.iterator(12);
		while(iter.hasNext()) {
			Joint joint = iter.next();
 			stream.writeFloat(joint.mX);
 			stream.writeFloat(joint.mY);
 			stream.writeFloat(joint.mZ);
 		}
 	}
 
 	public void readPosture(DataInputStream stream) throws IOException {
		Iterator<Joint> iter = mJoints.iterator(12);
		while(iter.hasNext()) {
			Joint joint = iter.next();
 			joint.mX = stream.readFloat();
 			joint.mY = stream.readFloat();
 			joint.mZ = stream.readFloat();
 		}
 	}
 
 }
