 package yang.physics.massaggregation.elements;
 
 import yang.graphics.skeletons.CartoonSkeleton2D;
 import yang.math.Geometry;
 import yang.math.objects.Point3f;
 import yang.math.objects.Quaternion;
 import yang.math.objects.Vector3f;
 import yang.math.objects.YangMatrix;
 import yang.physics.massaggregation.MassAggregation;
 import yang.util.YangList;
 
 public class Joint extends Point3f {
 
 	public static float DEFAULT_FRICTION = 0.9997f;
 	public static float DEFAULT_RADIUS = 0.1f;
 	public static float TOWARDS_FACTOR = 50;
 	public static float AWAY_FACTOR = 500;
 
 	//Properties
 	public int mId;
 	public String mName;
 	public float mRadius;
 	public boolean mFixed;
 	public float mMass;
 	public float mInitialMass;
 	public MassAggregation mMassAggregation;
 	public Joint mParent;
 	public float mParentDistance;
 	public YangList<Joint> mChildren;
 	public boolean mSavePose;
 	public float mFriction;
 	public float mPositionForceFactor = 1;
 	public float mForceFactor = 80;
 	public boolean mAnimate;
 	public boolean mEnabled;
 	public float mInitialX,mInitialY,mInitialZ;
 	public Quaternion mOrientation = null;
 	public float mDragDelay = 0.01f;
 	public boolean mDragKeepDistance = false;
 	//public boolean mDisableAnimation = false;
 
 	//State
 	public float mForceX,mForceY,mForceZ;
 	public float mVelX,mVelY,mVelZ;
 	public Point3f mWorldPosition = new Point3f();
 	public Point3f mDragDelayed = new Point3f();
 	public Point3f mDragTo = new Point3f();
 	public Point3f mPrevDrag = new Point3f();
 	public Point3f mCurResDrag = new Point3f();
 	public Vector3f mDragVec = new Vector3f();
 	public Vector3f mParentSpatial = new Vector3f();
 	public boolean mDragging;
 	public float mParentCurAngle;
 	public boolean mAnimDisabled = false;
 
 	private Vector3f tempVec = new Vector3f(), tempVec2 = new Vector3f();
 	public Joint mUpJoint = null;
 	public Joint mRightJoint = null;
 
 	public Joint(String name,Joint parent,float posX,float posY,float posZ,float radius) {
 		super(posX,posY,posZ);
 		mName = name;
 		mFixed = false;
 
 		mRadius = radius;
 		mFixed = false;
 		mMass = 1;
 		setInitialValues();
 		mDragging = false;
 		mAnimate = true;
 		mSavePose = true;
 		mChildren = new YangList<Joint>();
 		mFriction = DEFAULT_FRICTION;
 		mEnabled = true;
 
 		setParent(parent);
 		refreshParentAngle();
 	}
 
 	public Joint(String name) {
 		this(name,null, 0,0,0, DEFAULT_RADIUS);
 	}
 
 	public Joint(String name,Joint parent,float posX,float posY,float radius) {
 		this(name,parent,posX,posY,0,radius);
 	}
 
 	public Joint(String name,Joint parent,Point3f position,float radius) {
 		this(name,parent,position.mX,position.mY,position.mZ, radius);
 	}
 
 	public void applyConstraint() { }
 
 	public void setMassAggregation(MassAggregation massAggregation) {
 		mMassAggregation = massAggregation;
 		mId = mMassAggregation.getNextJointId();
 	}
 
 	public Joint setName(String name) {
 		mName = name;
 		return this;
 	}
 
 	public Joint setRadius(float radius) {
 		mRadius = radius * mMassAggregation.mCurJointScale;
 		return this;
 	}
 
 	public void setInitialValues() {
 		mInitialX = mX;
 		mInitialY = mY;
 		mInitialZ = mZ;
 		mInitialMass = mMass;
 	}
 
 	public void reset() {
 		mX = mInitialX;
 		mY = mInitialY;
 		mZ = mInitialZ;
 		mMass = mInitialMass;
 		mVelX = 0;
 		mVelY = 0;
 		mVelZ = 0;
 	}
 
 	public void setParent(Joint parent) {
 		mParent = parent;
 		mParentDistance = getDistance(parent);
 		if(parent!=null)
 			parent.mChildren.add(this);
 	}
 
 	public void refreshParentAngle() {
 		if(mParent==null)
 			mParentCurAngle = 0;
 		else
 			mParentCurAngle = getAngle2D(mParent);
 	}
 
 	/**
 	 * -1 no child
 	 * 0 self
 	 * 1+ distance
 	 */
 	public int childDistance(Joint joint) {
 		int count = 0;
 		Joint check = this;
 		while(check!=null && check!=joint) {
 			check = check.mParent;
 			count++;
 		}
 		if(check==null)
 			return -1;
 		else
 			return count;
 	}
 
 	public float getWorldX() {
 		return mMassAggregation.getJointWorldX(this);
 	}
 
 	public float getWorldY() {
 		return mMassAggregation.getJointWorldY(this);
 	}
 
 	public float getWorldZ() {
 		return mMassAggregation.getJointWorldZ(this);
 	}
 
 	public float getWorldRadius() {
 		return mRadius*mMassAggregation.mScale*mMassAggregation.mCarrier.getScale();
 	}
 
 	public boolean isSubChildOf(Joint joint) {
 		return childDistance(joint)>=0;
 	}
 
 	public float getDistance(Joint joint) {
 		if(joint==null)
 			return -1;
 		else
 			return Geometry.getDistance(mX-joint.mX,mY-joint.mY,mZ-joint.mZ);
 	}
 
 	public void setSpeed(Joint preface) {
 		mVelX = preface.mVelX;
 		mVelY = preface.mVelY;
 	}
 
 	public void addPositionForce(float posX,float posY,float posZ,float factor) {
 		final float dX = posX - mX;
 		final float dY = posY - mY;
 		final float dZ = posZ - mZ;
 		final float dist = (float)Math.sqrt(dX*dX + dY*dY + dZ*dZ);
 		if(dist>0) {
 			float fac;
 			if(mMassAggregation.m3D)
 				fac = TOWARDS_FACTOR * factor;//TODO 3D
 			else
 				fac = ((mVelX*dX+mVelY*dY<0)?AWAY_FACTOR:TOWARDS_FACTOR) * mPositionForceFactor * factor;
 			mForceX += fac * dX;
 			mForceY += fac * dY;
 			mForceZ += fac * dZ;
 		}
 	}
 
 	public void addPositionForce(float posX,float posY,float factor) {
 		addPositionForce(posX,posY,0,factor);
 	}
 
 	public void addWorldPositionForce(float worldX,float worldY,float worldZ, float factor) {
 		float[] matrix = mMassAggregation.mInvTransform.mValues;
 		float x = matrix[0] * worldX + matrix[4] * worldY + matrix[8] * worldZ + matrix[12];
 		float y = matrix[1] * worldX + matrix[5] * worldY + matrix[9] * worldZ + matrix[13];
 		float z = matrix[2] * worldX + matrix[6] * worldY + matrix[10] * worldZ + matrix[14];
 		addPositionForce(x,y,z,factor);
 	}
 
 	public void addWorldPositionForce(Point3f worldPosition, float factor) {
 		addWorldPositionForce(worldPosition.mX,worldPosition.mY,worldPosition.mZ, factor);
 	}
 
 	public void setWorldPosition(float worldX,float worldY,float worldZ) {
 		float[] matrix = mMassAggregation.mInvTransform.mValues;
 		mX = matrix[0] * worldX + matrix[4] * worldY + matrix[8] * worldZ + matrix[12];
 		mY = matrix[1] * worldX + matrix[5] * worldY + matrix[9] * worldZ + matrix[13];
 		mZ = matrix[2] * worldX + matrix[6] * worldY + matrix[10] * worldZ + matrix[14];
 	}
 
 	public void setWorldPosition(Point3f worldPosition) {
 		setWorldPosition(worldPosition.mX,worldPosition.mY,worldPosition.mZ);
 	}
 
 	/**
 	 * Zero degrees: downwards, CCW
 	 */
 	public float getAngle2D(Joint joint) {
 		return Geometry.getAngleDown(joint.mX,joint.mY,mX,mY);
 	}
 
 	/**
 	 * Zero degrees: downwards, CCW
 	 */
 	public void setPosByAngle2D(Joint relativeJoint,float distance,float angle) {
 		mParentCurAngle = angle;
 		mX = relativeJoint.mX + (float)(distance*Math.sin(angle));
 		mY = relativeJoint.mY - (float)(distance*Math.cos(angle));
 	}
 
 	public void setPosByAngle2D(Joint relativeJoint,JointConnection connectingBone,float angle) {
 		setPosByAngle2D(relativeJoint,connectingBone.mDistance,angle);
 	}
 
 	public void setPosByAngle2D(float angle) {
 		setPosByAngle2D(mParent,mParentDistance,angle);
 	}
 
 	public void scaleDistance(Joint pivotJoint,float factor) {
 		mX = pivotJoint.mX + (mX-pivotJoint.mX)*factor;
 		mY = pivotJoint.mY + (mY-pivotJoint.mY)*factor;
 		mZ = pivotJoint.mZ + (mZ-pivotJoint.mZ)*factor;
 	}
 
 	public void rotate(float angle) {
 		setPosByAngle2D(mParentCurAngle+angle);
 	}
 
 	private void refreshResDrag() {
 		if(mDragKeepDistance && mParent!=null) {
 			float dist = mParent.getDistance(mDragTo.mX,mDragTo.mY,mDragTo.mZ);
 			if(dist!=0) {
 				dist = 1/dist*mParentDistance;
 				float dx = (mDragTo.mX-mParent.mX);
 				float dy = (mDragTo.mY-mParent.mY);
 				float dz = (mDragTo.mZ-mParent.mZ);
 				mCurResDrag.set(mParent.mX+dx*dist,mParent.mY+dy*dist,mParent.mZ+dz*dist);
 			}else{
 				mCurResDrag.set(mDragTo);
 			}
 		}else{
 			mCurResDrag.set(mDragTo);
 		}
 	}
 
 	public void physicalStep(float deltaTime) {
 
 		if(mEnabled) {
 
 			if(mDragging) {
 				//refreshResDrag();
 				mDragDelayed.lerp(mCurResDrag,mDragDelay);
 
 				addPositionForce(mDragDelayed.mX,mDragDelayed.mY,mDragDelayed.mZ,6f);
 			}
 
 			mVelX += mForceX/mMass * mForceFactor * deltaTime;
 			mVelY += mForceY/mMass * mForceFactor * deltaTime;
 			mVelZ += mForceZ/mMass * mForceFactor * deltaTime;
 
 			mVelX *= mFriction;
 			mVelY *= mFriction;
 			mVelZ *= mFriction;
 
 			mX += mVelX * deltaTime;
 			mY += mVelY * deltaTime;
 			mZ += mVelZ * deltaTime;
 		}
 	}
 
 	public void refreshWorldPosition() {
 		mMassAggregation.mTransform.apply3D(mX*mMassAggregation.mScale,mY*mMassAggregation.mScale,mZ*mMassAggregation.mScale, mWorldPosition);
 	}
 
 	public void startDrag() {
 		mDragDelayed.set(mX,mY,mZ);
 		mDragTo.set(mDragDelayed);
 		mDragging = true;
 		mPrevDrag.set(mDragDelayed);
 		refreshResDrag();
 	}
 
 	public void dragLocal(float deltaX,float deltaY,float deltaZ) {
 		final float fac = 1f/mMassAggregation.mCarrier.getScale()/mMassAggregation.mScale;
 
 		//mDragVec.set(deltaX*fac,deltaY*fac,deltaZ*fac);
 		mDragTo.add(deltaX*fac,deltaY*fac,deltaZ*fac);
 
 		refreshResDrag();
 		if(mCurResDrag.mY<mMassAggregation.mLowerLimit)
 			mCurResDrag.mY = mMassAggregation.mLowerLimit;
 		mDragVec.setFromTo(mPrevDrag,mCurResDrag);
 		mPrevDrag.set(mCurResDrag);
 	}
 
 	public void dragWorld(float deltaX,float deltaY,float deltaZ) {
 //		final float fac = 1f/mSkeleton.mCarrier.getScale()/mSkeleton.mScale;
 		//mSkeleton.mInvVectorTransform.apply3D(deltaX*fac,deltaY*fac,deltaZ*fac, mDragVec);
 
 		mMassAggregation.mInvVectorTransform.apply3D(deltaX,deltaY,deltaZ, tempVec);
 		dragLocal(tempVec.mX,tempVec.mY,tempVec.mZ);
 //		mDragTo.add(mDragVec);
 //		if(mDragTo.mY<mSkeleton.mLowerLimit)
 //			mDragTo.mY = mSkeleton.mLowerLimit;
 
 	}
 
 	public void dragWorld(float deltaX,float deltaY) {
 		dragWorld(deltaX,deltaY,0);
 	}
 
 	public void dragWorld(Vector3f vector) {
 		dragWorld(vector.mX,vector.mY,vector.mZ);
 	}
 
 	public void endDrag() {
 		mDragging = false;
 	}
 
 	public float getOutputRadius() {
 		//return Math.max(0.01f,Math.min(0.1f,mRadius));
 		return Math.max(0.01f,mRadius)*mMassAggregation.mJointRadiusOutputFactor;
 	}
 
 	public void addForce(float fX, float fY) {
 		this.mForceX += fX;
 		this.mForceY += fY;
 	}
 
 	public void addForce(float fX,float fY,float fZ) {
 		this.mForceX += fX;
 		this.mForceY += fY;
 		this.mForceZ += fZ;
 	}
 
 	public void addForce(Vector3f force) {
 		this.mForceX += force.mX;
 		this.mForceY += force.mY;
 		this.mForceZ += force.mZ;
 	}
 
 	public void setNormalDirection(float worldX, float worldY,Joint parent,float distance,boolean ortho,boolean invert,float xOffset,float yOffset) {
 		if(parent == null)
 			return;
 		final float baseX = parent.mX*mMassAggregation.mScale;
 		final float baseY = parent.mY*mMassAggregation.mScale;
 		float deltaX = mMassAggregation.toJointX(worldX)-baseX;
 		float deltaY = mMassAggregation.toJointY(worldY)-baseY;
 		if(invert) {
 			deltaX = -deltaX;
 			deltaY = -deltaY;
 		}
 		float dist = Geometry.getDistance(deltaX, deltaY);
 		if(dist==0)
 			return;
 		float nX,nY;
 		if(ortho) {
 			nX = -deltaY/dist;
 			nY = deltaX/dist;
 		}else{
 			nX = deltaX/dist;
 			nY = deltaY/dist;
 		}
 		if(xOffset!=0 || yOffset!=0) {
 			nX += xOffset*Math.signum(deltaX);
 			nY += yOffset;
 			dist = Geometry.getDistance(nX, nY);
 			nX /= dist;
 			nY /= dist;
 		}
 		mX = baseX + nX*distance;
 		mY = baseY + nY*distance;
 	}
 
 	public void setNormalDirection(float worldX, float worldY,Joint parent,float distance,boolean ortho,float xOffset,float yOffset) {
 		setNormalDirection(worldX,worldY,parent,distance,ortho,false,xOffset,yOffset);
 	}
 
 	public void setNormalDirection(float worldX, float worldY,boolean ortho,float xOffset,float yOffset) {
 		setNormalDirection(worldX,worldY,mParent,mParentDistance,ortho,xOffset,yOffset);
 	}
 
 	public void setNormalDirection(float worldX, float worldY) {
 		setNormalDirection(worldX,worldY,mParent,mParentDistance,false,0,0);
 	}
 
 	public void setPos(float x, float y) {
 		mX = x;
 		mY = y;
 	}
 
 	public void setPos(float x,float y,float z) {
 		mX = x;
 		mY = y;
 		mZ = z;
 	}
 
 	public void setPos(Joint joint) {
 		mX = joint.mX;
 		mY = joint.mY;
 		mZ = joint.mZ;
 	}
 
 	public void setPosIK(float posX, float posY) {
 		mX = posX;
 		mY = posY;
 
 		final float baseX = mParent.mParent.mX;
 		final float baseY = mParent.mParent.mY;
 
 		float deltaX = posX - baseX;
 		float deltaY = posY - baseY;
 		float dist = Geometry.getDistance(deltaX, deltaY);
 		final float limDist = mParentDistance*2-0.001f;
 		if(dist>limDist) {
 			deltaX = deltaX/dist * limDist;
 			deltaY = deltaY/dist * limDist;
 			dist = limDist;
 			mX = baseX+deltaX;
 			mY = baseY+deltaY;
 		}
 		float shiftX;
 		float shiftY;
 		if(dist==0) {
 			shiftX = 0;
 			shiftY = this.mParent.mParentDistance;
 		}else{
 			final float shift = (float)Math.sqrt(mParentDistance*mParentDistance - dist*dist*0.25f);
 			final float nX = deltaX/dist;
 			final float nY = deltaY/dist;
 			shiftX = nY*shift;
 			shiftY = -nX*shift;
 		}
 		mParent.setPos(
 				baseX + deltaX/2 + shiftX,
 				baseY + deltaY/2 + shiftY
 				);
 	}
 
 	public void setNormalDirectionIK(float relativeX, float relativeY, CartoonSkeleton2D skeleton, float straight, float orthoOffset) {
 		final float shoulderX = skeleton.getJointWorldX(mParent.mParent);
 		final float shoulderY = skeleton.getJointWorldY(mParent.mParent);
 		float dirX = relativeX - shoulderX;
 		float dirY = relativeY - shoulderY;
 		final float dist = Geometry.getDistance(dirX, dirY);
 
 		if(dist==0)
 			return;
 
 		if(orthoOffset!=0) {
 			final float x = dirX;
 			dirX += dirY*orthoOffset;
 			dirY += -x*orthoOffset;
 		}
 
 		final float normX = dirX/dist;
 		final float normY = dirY/dist;
 		final float armLength = mParentDistance*2*straight;
 
 		this.setPosIK(mParent.mParent.mX+normX*armLength, mParent.mParent.mY+normY*armLength);
 	}
 
 	public float getParentAngle() {
 		return getAngle2D(mParent);
 	}
 
 	public void recalculate() {
 		mParentDistance = getDistance(mParent);
 	}
 
 	public void setVelocity(float velX,float velY) {
 		mVelX = velX;
 		mVelY = velY;
 	}
 
 	public void setVelocity(float velX,float velY,float velZ) {
 		mVelX = velX;
 		mVelY = velY;
 		mVelZ = velZ;
 	}
 
 	public void addVelocity(float velX, float velY) {
 		mVelX += velX;
 		mVelY += velY;
 	}
 
 	public void addVelocity(float velX, float velY,float velZ) {
 		mVelX += velX;
 		mVelY += velY;
 		mVelZ += velZ;
 	}
 
 	public float getWorldDistance(float worldX, float worldY) {
 		final float dx = worldX-mMassAggregation.getJointWorldX(this);
 		final float dy = worldY-mMassAggregation.getJointWorldY(this);
 		return (float)Math.sqrt(dx*dx + dy*dy);
 	}
 
 	public void initOrientation() {
 		mOrientation = new Quaternion();
 	}
 
 	public boolean hasOrientation() {
 		return mOrientation!=null;
 	}
 
 	public boolean isAnimated() {
 		return mAnimate && !mAnimDisabled;
 	}
 
 	public void applyTransform(YangMatrix transform) {
 		float[] matrix = transform.mValues;
 		float x = mX;
 		float y = mY;
 		float z = mZ;
 		mX = matrix[0]*x+matrix[4]*y+matrix[8]*z+matrix[12];
 		mY = matrix[1]*x+matrix[5]*y+matrix[9]*z+matrix[13];
 		mZ = matrix[2]*x+matrix[6]*y+matrix[10]*z+matrix[14];
 	}
 
 //	protected void setDragRec(Vector3f dragVector) {
 //		this.drag(dragVector);
 //		for(Joint child:mChildren) {
 //			if(!child.mDragging)
 //				child.startDrag();
 //			child.setDragRec(dragVector);
 //		}
 //	}
 //
 //	public void setChildrenDrag() {
 //		for(Joint child:mChildren) {
 //			if(!child.mDragging)
 //				child.startDrag();
 //			child.setDragRec(mDragVec);
 //		}
 //	}
 
 	public void copyFrom(Joint joint) {
 		super.set(joint);
 		mMassAggregation = joint.mMassAggregation;
 		mFixed = joint.mFixed;
 		mAnimate = joint.mAnimate;
 		mDragDelay = joint.mDragDelay;
 		mEnabled = joint.mEnabled;
 		mRadius = joint.mRadius;
 		mDragKeepDistance = joint.mDragKeepDistance;
 	}
 
 	@Override
 	public Joint clone() {
 		Joint joint = new Joint(this.mName);
 		joint.copyFrom(this);
 		return joint;
 	}
 
 	@Override
 	public String toString() {
 		return this.mName+": "+super.toString();
 	}
 
 	public void clearForce() {
 		mForceX = 0;
 		mForceY = 0;
 		mForceZ = 0;
 	}
 
 	public void clearVelocity() {
 		mVelX = 0;
 		mVelY = 0;
 		mVelZ = 0;
 	}
 
 	public boolean intersects2D(Joint joint) {
 		float dx = mX-joint.mX;
 		float dy = mY-joint.mY;
 		return dx*dx + dy*dy < mRadius+joint.mRadius;
 	}
 
 	public boolean intersects(Joint joint) {
 		float dx = mX-joint.mX;
 		float dy = mY-joint.mY;
 		float dz = mZ-joint.mZ;
 		return dx*dx + dy*dy + dz*dz < mRadius+joint.mRadius;
 	}
 
 	public boolean worldIntersects2D(Joint joint) {
 		float dx = joint.getWorldX()-getWorldX();
 		float dy = joint.getWorldY()-getWorldY();
 		float r = getWorldRadius()+joint.getWorldRadius();
 		return dx*dx + dy*dy <= r*r;
 	}
 
 	public boolean worldIntersects(Joint joint) {
 		float dx = joint.getWorldX()-getWorldX();
 		float dy = joint.getWorldY()-getWorldY();
 		float dz = joint.getWorldZ()-getWorldZ();
 		float r = getWorldRadius()+joint.getWorldRadius();
 		return dx*dx + dy*dy + dz*dz < r*r;
 	}
 
 	public void dragTo(float x,float y,float z) {
 		this.dragLocal(x-mX, y-mY, z-mZ);
 	}
 
 	public float calcVelocity() {
 		return (float)Math.sqrt(mVelX*mVelX+mVelY*mVelY+mVelZ*mVelZ);
 	}
 
 	public float calcSqrVelocity() {
 		return mVelX*mVelX+mVelY*mVelY+mVelZ*mVelZ;
 	}
 
 	public void setVelocity(Joint template) {
 		mVelX = template.mVelX;
 		mVelY = template.mVelY;
 		mVelZ = template.mVelZ;
 	}
	
	public void shiftChildren(float shiftX, float shiftY) {
		for(Joint child:mChildren) {
			child.mX += shiftX;
			child.mY += shiftY;
			child.shiftChildren(shiftX,shiftY);
		}
	}
 
 
 
 }
