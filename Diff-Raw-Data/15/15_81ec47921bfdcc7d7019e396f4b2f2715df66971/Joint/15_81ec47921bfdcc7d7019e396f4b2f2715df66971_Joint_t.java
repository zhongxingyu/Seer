 package yang.graphics.skeletons.elements;
 
 import yang.graphics.skeletons.Skeleton;
 import yang.math.Geometry;
 import yang.math.objects.Point3f;
 import yang.util.NonConcurrentList;
 
 public class Joint {
 
 	public static float DEFAULT_FRICTION = 0.9998f;
 	public static float TOWARDS_FACTOR = 50;
 	public static float AWAY_FACTOR = 160;
 	
 	//Properties
 	public int mId;
 	public String mName;
 	public float mRadius;
 	public boolean mFixed;
 	public float mMass;
 	public Skeleton mSkeleton;
 	public Joint mAngleParent;
 	public float mParentDistance;
 	public NonConcurrentList<Joint> mChildren;
 	public boolean mSavePose;
 	public float mFriction;
 	public float mPositionForceFactor = 1;
 	public float mForceFactor = 80;
 	public boolean mAnimate;
 	public boolean mEnabled;
 	
 	//State
 	public float mForceX,mForceY,mForceZ;
 	public float mVelX,mVelY,mVelZ;
 	public float mPosX, mPosY, mPosZ;
 	public Point3f mDragDelay = new Point3f();
 	public Point3f mDragTo = new Point3f();
 	public boolean mDragging;
 	public float mParentCurAngle;
 	
 	public Joint(String name,Joint parent,float posX,float posY,float radius,Skeleton skeleton) {
 		mName = name;
 		mFixed = false;
 		mPosX = posX;
 		mPosY = posY;
 		mPosZ = 0;
 		mRadius = radius;
 		mFixed = false;
 		mMass = 1;
 		mDragging = false;
 		mAnimate = true;
 		mSkeleton = skeleton;
 		mId = mSkeleton.getNextJointId();
 		mSavePose = true;
 		mChildren = new NonConcurrentList<Joint>();
 		mFriction = DEFAULT_FRICTION;
 		setParent(parent);
 		refreshParentAngle();
 		mEnabled = true;
 	}
 	
 	public void setParent(Joint parent) {
 		mAngleParent = parent;
 		mParentDistance = getDistance(parent);
 		if(parent!=null)
 			parent.mChildren.add(this);
 	}
 	
 	public void refreshParentAngle() {
 		if(mAngleParent==null)
 			mParentCurAngle = 0;
 		else
 			mParentCurAngle = getAngle(mAngleParent);
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
 			check = check.mAngleParent;
 			count++;
 		}
 		if(check==null)
 			return -1;
 		else
 			return count;
 	}
 	
 	public boolean isSubChildOf(Joint joint) {
 		return childDistance(joint)>=0;
 	}
 	
 	public float getDistance(Joint joint) {
 		if(joint==null)
 			return -1;
 		else
 			return Geometry.getDistance(mPosX,mPosY,joint.mPosX,joint.mPosY);
 	}
 	
 	public void setSpeed(Joint preface) {
 		mVelX = preface.mVelX;
 		mVelY = preface.mVelY;
 	}
 	
 	public void addPositionForce(float posX,float posY,float posZ,float factor) {
 		float dX = posX - mPosX;
 		float dY = posY - mPosY;
 		float dZ = posZ - mPosZ;
 		float dist = (float)Math.sqrt(dX*dX + dY*dY + dZ*dZ);
 		if(dist>0) {
 			float fac;
 			if(mSkeleton.m3D)
 				fac = TOWARDS_FACTOR;//TODO 3D
 			else
 				fac = (mVelX*dX+mVelY*dY<0)?AWAY_FACTOR:TOWARDS_FACTOR * mPositionForceFactor * factor;
 			mForceX += fac * dX;
 			mForceY += fac * dY;
 			mForceZ += fac * dZ;
 		}
 	}
 	
 	public void addPositionForce(float posX,float posY,float factor) {
 		addPositionForce(posX,posY,0,factor);
 	}
 	
 	/**
 	 * Zero degrees: downwards, CCW
 	 */
 	public float getAngle(Joint joint) {
 		return Geometry.getAngleDown(joint.mPosX,joint.mPosY,mPosX,mPosY);
 	}
 	
 	/**
 	 * Zero degrees: downwards, CCW
 	 */
 	public void setPosByAngle(Joint relativeJoint,float distance,float angle) {
 		mParentCurAngle = angle;
 		mPosX = relativeJoint.mPosX + (float)(distance*Math.sin(angle));
 		mPosY = relativeJoint.mPosY - (float)(distance*Math.cos(angle));//System.out.println(angle+" "+getAngle(relativeJoint));
 	}
 	
 	public void setPosByAngle(Joint relativeJoint,Bone connectingBone,float angle) {
 		setPosByAngle(relativeJoint,connectingBone.mDistance,angle);
 	}
 	
 	public void setPosByAngle(float angle) {
 		setPosByAngle(mAngleParent,mParentDistance,angle);
 	}
 	
 	public void rotate(float angle) {
 		setPosByAngle(mParentCurAngle+angle);
 	}
 	
 	public void physicalStep(float deltaTime) {
 		
 		if(mEnabled) {
 			if(mFixed) {
 				mForceX = 0;
 				mForceY = 0;
 				mForceZ = 0;
 			}
 			
 			if(mDragging) {
 				mDragDelay.lerp(mDragTo,0.02f);
 				addPositionForce(mDragDelay.mX,mDragDelay.mY,mDragDelay.mZ,1);
 			}
 			
 			mVelX += mForceX/mMass * mForceFactor * deltaTime;
 			mVelY += mForceY/mMass * mForceFactor * deltaTime;
 			mVelZ += mForceZ/mMass * mForceFactor * deltaTime;
 			
 			mVelX *= mFriction;
 			mVelY *= mFriction;
 			mVelZ *= mFriction;
 			
 			mPosX += mVelX * deltaTime;
 			mPosY += mVelY * deltaTime;
 			mPosZ += mVelZ * deltaTime;
 		}
 	}
 	
 	public void startDrag() {
 		mDragDelay.set(mPosX,mPosY,mPosZ);
 		mDragTo.set(mDragDelay);
 		mDragging = true;
 	}
 	
 	public void drag(float deltaX,float deltaY,float deltaZ) {
 		float fac = 1f/mSkeleton.mCarrier.getScale();
 		mDragTo.add(deltaX*fac,deltaY*fac,deltaZ*fac);
 	}
 	
 	public void drag(float deltaX,float deltaY) {
 		drag(deltaX,deltaY,0);
 	}
 	
 	public void endDrag() {
 		mDragging = false;
 	}
 	
 	public float getOutputRadius() {
		//return Math.max(0.01f,Math.min(0.1f,mRadius));
		return Math.max(0.01f,mRadius);
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
 	
 	public void applyConstraint() { }
 
 	public void setNormalDirection(float worldX, float worldY,Joint parent,float distance,boolean ortho,boolean invert,float xOffset,float yOffset) {
 		if(parent == null)
 			return;
 		float baseX = parent.mPosX;
 		float baseY = parent.mPosY;
 		float deltaX = mSkeleton.toJointX(worldX)-baseX;
 		float deltaY = mSkeleton.toJointY(worldY)-baseY;
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
 		mPosX = baseX + nX*distance;
 		mPosY = baseY + nY*distance;
 	}
 	
 	public void setNormalDirection(float worldX, float worldY,Joint parent,float distance,boolean ortho,float xOffset,float yOffset) {
 		setNormalDirection(worldX,worldY,parent,distance,ortho,false,xOffset,yOffset);
 	}
 	
 	public void setNormalDirection(float worldX, float worldY,boolean ortho,float xOffset,float yOffset) {
 		setNormalDirection(worldX,worldY,mAngleParent,mParentDistance,ortho,xOffset,yOffset);
 	}
 
 	public void setNormalDirection(float worldX, float worldY) {
 		setNormalDirection(worldX,worldY,mAngleParent,mParentDistance,false,0,0);
 	}
 
 	public void setPos(float x, float y) {
 		mPosX = x;
 		mPosY = y;
 	}
 	
 	public void setPos(Joint joint) {
 		mPosX = joint.mPosX;
 		mPosY = joint.mPosY;
 	}
 
 	public void setPosIK(float posX, float posY) {
 		mPosX = posX;
 		mPosY = posY;
 		
 		float baseX = mAngleParent.mAngleParent.mPosX;
 		float baseY = mAngleParent.mAngleParent.mPosY;
 
 		float deltaX = posX - baseX;
 		float deltaY = posY - baseY;
 		float dist = Geometry.getDistance(deltaX, deltaY);
 		float limDist = mParentDistance*2-0.001f;
 		if(dist>limDist) {
 			deltaX = deltaX/dist * limDist;
 			deltaY = deltaY/dist * limDist;
 			dist = limDist;
 			mPosX = baseX+deltaX;
 			mPosY = baseY+deltaY;
 		}
 		float shiftX;
 		float shiftY;
 		if(dist==0) {
 			shiftX = 0;
 			shiftY = this.mAngleParent.mParentDistance;
 		}else{
 			float shift = (float)Math.sqrt(mParentDistance*mParentDistance - dist*dist*0.25f);
 			float nX = deltaX/dist;
 			float nY = deltaY/dist;
 			shiftX = nY*shift;
 			shiftY = -nX*shift;
 		}
 		mAngleParent.setPos(
 				baseX + deltaX/2 + shiftX,
 				baseY + deltaY/2 + shiftY
 				);
 	}
 
 	public void setNormalDirectionIK(float relativeX, float relativeY, Skeleton skeleton, float straight, float orthoOffset) {
 		float shoulderX = skeleton.getJointWorldX(mAngleParent.mAngleParent);
 		float shoulderY = skeleton.getJointWorldY(mAngleParent.mAngleParent);
 		float dirX = relativeX - shoulderX;
 		float dirY = relativeY - shoulderY;
 		float dist = Geometry.getDistance(dirX, dirY);
 		
 		if(dist==0)
 			return;
 
 		if(orthoOffset!=0) {
 			float x = dirX;
 			dirX += dirY*orthoOffset;
 			dirY += -x*orthoOffset;
 		}
 		
 		float normX = dirX/dist;
 		float normY = dirY/dist;
 		float armLength = mParentDistance*2*straight;
 		
 		this.setPosIK(mAngleParent.mAngleParent.mPosX+normX*armLength, mAngleParent.mAngleParent.mPosY+normY*armLength);
 	}
 
 	public float getParentAngle() {
 		return getAngle(mAngleParent);
 	}
 
 	public void recalculate() {
 		mParentDistance = getDistance(mAngleParent);
 	}
 
 	public void setSpeed(float velX,float velY) {
 		mVelX = velX;
 		mVelY = velY;
 	}
	
	@Override
	public String toString() {
		return "JOINT:"+this.mName;
	}
 
 }
