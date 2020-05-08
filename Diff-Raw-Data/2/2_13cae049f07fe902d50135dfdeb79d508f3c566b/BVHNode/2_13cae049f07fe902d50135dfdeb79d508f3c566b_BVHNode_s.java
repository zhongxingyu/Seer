 package org.tavatar.tavimator;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeMap;
 
 import android.util.Log;
 
 /**
  * adapted from bvhnode.cpp and bvhnode.h in QAvimator
  * @author tapple
  *
  */
 public class BVHNode {
 	private static final String TAG = "FrameData";
 
 	private String m_name;
 
 	// this node's mirror, if applicable
 	private BVHNode mirrorPart;
 	private int mirrorIndex;
 
 	private List<BVHNode> children = new ArrayList<BVHNode>();
 	// I'd use the interface NavigableMap, but it wasn't added until Gingerbread
 	private TreeMap<Integer,FrameData> keyframes = new TreeMap<Integer, FrameData>();
 
 	// rotation/position cache on load, will be cleared once the animation is loaded
 	private List<Rotation> rotations = new ArrayList<Rotation>();
 	private List<Position> positions = new ArrayList<Position>();
 
 	public BVHNodeType type;
 	public float[] offset = new float[3];
 	public int numChannels;
 	public BVHChannelType[] channelType = new BVHChannelType[6];
 	public BVHOrderType channelOrder;
 	public float[] channelMin = new float[6];
 	public float[] channelMax = new float[6];
 
 	public boolean ikOn;
 	public Rotation ikRot = new Rotation();
 	public float[] ikGoalPos = new float[3];
 	public float[] ikGoalDir = new float[3];
 	public float ikWeight;
 
 
 	
 	
 	public BVHNode(String name)	{
 	//  qDebug(QString("BVHNode::BVHNode(%1)").arg(name));
 	  setName(name);
 
 	  // clean out lists
 	  keyframes.clear();
 	  children.clear();
 
 	  // have clean one-time cache
 	  flushFrameCache();
 
 	  setMirror(null, 0);
 
 	  numChannels=0;
 
 	  ikRot.x=0;
 	  ikRot.y=0;
 	  ikRot.z=0;
 
 	  ikGoalPos[0]=0;
 	  ikGoalPos[1]=0;
 	  ikGoalPos[2]=0;
 
 	  ikGoalDir[0]=0;
 	  ikGoalDir[1]=1;
 	  ikGoalDir[2]=0;
 	}
 
 	public String name() {
 		return m_name;
 	}
 
 	
 	public int numChildren() {
 	  return children.size();
 	}
 
 	public BVHNode child(int num) {
 		return children.get(num);
 	}
 
 	public void addChild(BVHNode newChild) {
 	// qDebug(QString("BVHNode(%1): addChild(%2)").arg(name()).arg(newChild->name()));
 	  children.add(newChild);
 	}
 
 	public void insertChild(BVHNode newChild,int index) {
 	// qDebug(QString("BVHNode(%1): insertChild(%2,%3)").arg(name()).arg(newChild->name()).arg(index));
 	  children.add(index,newChild);
 	}
 
 	public void removeChild(BVHNode child) {
 	// qDebug(QString("BVHNode(%1): removeChild(%2)").arg(name()).arg(child->name()));
 	  while(children.remove(child)); // this was QList::removeAll in qavimator
 	}
 
 
 	public FrameData frameData(int frame) {
 	  // return empty frame data on end site nodes
 	  if(type==BVHNodeType.BVH_END) return new FrameData();
 	  // if the keyframe exists, return the data
 	  if(isKeyframe(frame)) return keyframes.get(frame);
 
 	  // get keyframes before and after desired frame
 	  FrameData before=getKeyframeBefore(frame);
 	  FrameData after=getNextKeyframe(before.frameNumber());
 
 	  int frameBefore=before.frameNumber();
 	  int frameAfter=after.frameNumber();
 
 	  // if before and after frames are the same, there are no more keyframes left, so
 	  // we return the last keyframe data
 	  if(frameBefore==frameAfter) return before;
 
 	  Rotation rotBefore=before.rotation();
 	  Rotation rotAfter=after.rotation();
 	  Position posBefore=before.position();
 	  Position posAfter=after.position();
 
 	  Rotation iRot = new Rotation();
 	  Position iPos = new Position();
 
 	  iRot.x=interpolate(rotBefore.x,rotAfter.x,frameAfter-frameBefore,frame-frameBefore,before.easeOut(),after.easeIn());
 	  iRot.y=interpolate(rotBefore.y,rotAfter.y,frameAfter-frameBefore,frame-frameBefore,before.easeOut(),after.easeIn());
 	  iRot.z=interpolate(rotBefore.z,rotAfter.z,frameAfter-frameBefore,frame-frameBefore,before.easeOut(),after.easeIn());
 
 	  iPos.x=interpolate(posBefore.x,posAfter.x,frameAfter-frameBefore,frame-frameBefore,before.easeOut(),after.easeIn());
 	  iPos.y=interpolate(posBefore.y,posAfter.y,frameAfter-frameBefore,frame-frameBefore,before.easeOut(),after.easeIn());
 	  iPos.z=interpolate(posBefore.z,posAfter.z,frameAfter-frameBefore,frame-frameBefore,before.easeOut(),after.easeIn());
 
 	// qDebug(QString("iRot.x %1 frame %2: %3").arg(rotBefore.bodyPart).arg(before.frameNumber()).arg(iRot.x));
 
 	  // return interpolated frame data here
 	  return new FrameData(frame,iPos,iRot);
 	}
 
 	public FrameData keyframeDataByIndex(int index) {
 	  // get a list of all keyframe numbers
 	  Integer[] keys=keyframeList();
 	  // get frame number of keyframe at given index
 	  int number=keys[index];
 	  // return keyframe data
 	  return keyframes.get(number);
 	}
 
 	public Integer[] keyframeList() {
		return (Integer[])keyframes.keySet().toArray();
 	}
 
 
 	public void addKeyframe(int frame,Position pos,Rotation rot) {
 	//  qDebug(QString("addKeyframe(%1)").arg(frame));
 	  keyframes.put(frame, new FrameData(frame,pos,rot));
 	//  if(frame==0 && name().equals("hip")) qDebug(QString("BVHNode::addKeyframe(%1,<%2,%3,%4>,<%5,%6,%7>) %8").arg(frame).arg(pos.x).arg(pos.y).arg(pos.z).arg(rot.x).arg(rot.y).arg(rot.z).arg(pos.bodyPart));
 	}
 
 	public void deleteKeyframe(int frame) {
 	  keyframes.remove(frame);
 	}
 
 	public void setKeyframePosition(int frame, Position pos) {
 	//  qDebug(QString("setKeyframePosition(%1)").arg(frame));
 	  if(!isKeyframe(frame)) Log.d(TAG, "setKeyframePosition(" + frame + "): not a keyframe!");
 	  else
 	  {
 	    FrameData key=keyframes.get(frame);
 	    key.setPosition(pos);
 	  }
 	}
 
 	public void setKeyframeRotation(int frame, Rotation rot) {
 	//  qDebug(QString("setKeyframeRotation(%1)").arg(frame));
 	  if(!isKeyframe(frame)) Log.d(TAG, "setKeyframeRotation(" + frame + "): not a keyframe!");
 	  else
 	  {
 	    FrameData key=keyframes.get(frame);
 	    key.setRotation(rot);
 	  }
 	}
 
 	// moves all key frames starting at "frame" one frame further
 	public void insertFrame(int frame) {
 		Integer[] keyframeList = keyframeList();
 		int i = keyframeList.length-1;
 		int keyframe = keyframeList[i];
 		while (keyframe >= frame) {
 			FrameData data = keyframes.remove(keyframe);
 			data.setFrameNumber(keyframe+1);
 			keyframes.put(keyframe+1, data);
 		}
 	}
 
 	// removes frame at position and moves all further frames one down
 	// delete a frame and move all keys back one frame
 	public void deleteFrame(int frame) {
 		//  qDebug("BVHNode::deleteFrame(%d)",frame);
 		// if this is a keyframe, remove it
 		if(isKeyframe(frame)) deleteKeyframe(frame);
 		for (int frameToMove:(Integer[])keyframes.tailMap(frame).keySet().toArray()) {
 			FrameData data = keyframes.remove(frameToMove);
 			data.setFrameNumber(frameToMove-1);
 			keyframes.put(frameToMove-1, data);
 		}
 	}
 
 	public boolean isKeyframe(int frame) {
 	  return keyframes.containsKey(frame);
 	}
 
 	public int numKeyframes() {
 	  return keyframes.size();
 	}
 
 
 	public FrameData getKeyframeBefore(int frame) {
 		if(frame==0) {
 			// should never happen
 			Log.d(TAG, "BVHNode::getKeyframeBefore(int frame): frame==0!");
 			return keyframes.get(0);
 		}
 		return frameData(getKeyframeNumberBefore(frame));
 	}
 
 	public FrameData getNextKeyframe(int frame) {
 		int keyframe = getKeyframeNumberAfter(frame);
 		// if we are asked for a keyframe past the last one, return the last one
 		if (keyframe < 0) keyframe = keyframes.lastKey();
 		return frameData(keyframe);
 	}
 
 
 	public void setEaseIn(int frame,boolean state) {
 		// ####### What if frame is not a keyframe? ########
 		keyframes.get(frame).setEaseIn(state);
 	}
 
 	public void setEaseOut(int frame,boolean state) {
 		// ####### What if frame is not a keyframe? ########
 		keyframes.get(frame).setEaseOut(state);
 	}
 
 	public boolean easeIn(int frame) {
 	  if(keyframes.containsKey(frame))
 	    return keyframes.get(frame).easeIn();
 
 	  Log.d(TAG, "BVHNode::easeIn(): asked on non-keyframe!");
 	  return false;
 	}
 
 	public boolean easeOut(int frame) {
 	  if(keyframes.containsKey(frame))
 	    return keyframes.get(frame).easeOut();
 
 	  Log.d(TAG, "BVHNode::easeOut(): asked on non-keyframe!");
 	  return false;
 	}
 
 
 	public Rotation getCachedRotation(int frame) {
 	  return rotations.get(frame);
 	}
 
 	public Position getCachedPosition(int frame) {
 	  return positions.get(frame);
 	}
 
 	public void cacheRotation(Rotation rot) {
 	  rotations.add(rot);
 	}
 
 	public void cachePosition(Position pos) {
 	  positions.add(pos);
 	}
 
 	public void flushFrameCache() {
 	  rotations.clear();
 	  positions.clear();
 	}
 
 
 	public boolean compareFrames(int key1,int key2) {
 	  if(type==BVHNodeType.BVH_POS) {
 	    final Position pos1=frameData(key1).position();
 	    final Position pos2=frameData(key2).position();
 
 	    if(pos1.x!=pos2.x) return false;
 	    if(pos1.y!=pos2.y) return false;
 	    if(pos1.z!=pos2.z) return false;
 	  } else {
 	    final Rotation rot1=frameData(key1).rotation();
 	    final Rotation rot2=frameData(key2).rotation();
 
 	    if(rot1.x!=rot2.x) return false;
 	    if(rot1.y!=rot2.y) return false;
 	    if(rot1.z!=rot2.z) return false;
 	  }
 
 	  return true;
 	}
 
 	public void optimize() {
 		// PASS 1 - remove identical keyframes
 
 		// get a list of all keyframe numbers
 		Integer[] keys=keyframeList();
 		List<Integer> keysToDelete = new ArrayList<Integer>();
 
 		// build a list of all identical keyframes to delete
 		for(int i=1;i< keys.length;i++) {
 			// if we're comparing the last keyframe, it only makes sense to check for the one before
 			if(i==keys.length-1) {
 				if(compareFrames(keys[i],keys[i-1])) {
 					keysToDelete.add(keys[i]);
 				}
 			// otherwise check for the one before and the one after
 			} else if(compareFrames(keys[i],keys[i-1]) && compareFrames(keys[i],keys[i+1])) {
 				keysToDelete.add(keys[i]);
 			}
 		}
 
 		// delete keyframes on the delete list
 		for(int keyToDelete:keysToDelete) {
 			deleteKeyframe(keyToDelete);
 		}
 
 		// 	PASS 2 - remove keyframes that are superfluous due to linear interpolation
 
 		keys = keyframeList();
 		Rotation oldRDifference = new Rotation();
 		Position oldPDifference = new Position();
 
 		// get first frame to compare - we even compare frame 1 here because we need
 		// the initial "distance" and "difference" values. The first keyframe will
 		// never be deleted, though
 		int itBefore = 0;
 
 		if(itBefore==keys.length) return;
 
 		// make "current" frame one frame after "before" frame
 		int itCurrent=itBefore;
 		itCurrent++;
 
 		if(itCurrent==keys.length) return;
 
 		// defines how much difference from anticipated change is acceptable for optimizing
 		float tolerance=0.01f;
 
 		// loop as long as there are keyframes left
 		while(itCurrent < keys.length) {
 			int frameCurrent = keys[itCurrent];
 			int frameBefore  = keys[itBefore ];
 			FrameData dataCurrent = keyframes.get(frameCurrent);
 			FrameData dataBefore  = keyframes.get(frameBefore );
 			
 			int distance=frameCurrent-frameBefore;
 
 			// optimize positions if this is the position node
 			if(type==BVHNodeType.BVH_POS) {
 				Position pDifference=Position.difference(dataBefore.position(),dataCurrent.position());
 
 				pDifference.x/=distance;
 				pDifference.y/=distance;
 				pDifference.z/=distance;
 
 				if(Math.abs(pDifference.x-oldPDifference.x)<tolerance &&
 						Math.abs(pDifference.y-oldPDifference.y)<tolerance &&
 						Math.abs(pDifference.z-oldPDifference.z)<tolerance)
 				{
 					// never delete the key in the first frame
 					if(frameBefore!=0) keyframes.remove(frameBefore);
 				}
 
 				oldPDifference=pDifference;
 			// otherwise optimize rotations
 			} else {
 				Rotation rDifference=Rotation.difference(dataBefore.rotation(),dataCurrent.rotation());
 
 				rDifference.x/=distance;
 				rDifference.y/=distance;
 				rDifference.z/=distance;
 
 				if(Math.abs(rDifference.x-oldRDifference.x)<tolerance &&
 						Math.abs(rDifference.y-oldRDifference.y)<tolerance &&
 						Math.abs(rDifference.z-oldRDifference.z)<tolerance)
 				{
 					// never delete the key in the first frame
 					if(frameBefore!=0) keyframes.remove(frameBefore);
 				}
 
 				oldRDifference=rDifference;
 			}
 
 			itBefore=itCurrent;
 			itCurrent++;
 		} // while
 	}
 
 
 	public void dumpKeyframes() {
 		Integer[] keys=keyframeList();
 		for(int index=0;index< keyframes.size();index++) {
 			Rotation rot=frameData(keys[index]).rotation();
 			Position pos=frameData(keys[index]).position();
 
 			Log.d(TAG, "" + name() + ": " + keys[index] + 
 					" - Pos <" + pos.x + "," + pos.y + "," + pos.z + 
 					"> Rot: <" + rot.x + "," + rot.y + "," + rot.z + ">");
 		}
 	}
 
 
 	// set and get mirror nodes for this node
 	public void setMirror(BVHNode mirror, int index) {
 		mirrorPart=mirror;
 		mirrorIndex=index;
 	}
 
 	public BVHNode getMirror() {
 		return mirrorPart;
 	}
 
 	public int getMirrorIndex() {
 		return mirrorIndex;
 	}
 
 
 	// mirrors the rotations in a node and swaps the tracks' keyframes if needed
 	public void mirror() {
 	  mirrorKeys();
 
 	  BVHNode node2=getMirror();
 
 	  // if a mirror node is given, swap the keyframes, too
 	  if(node2 != null) {
 	    node2.mirrorKeys();
 	    TreeMap<Integer,FrameData> temp=keyframes;
 	    keyframes=node2.keyframes;
 	    node2.keyframes=temp;
 	  }
 	}
 
 
 	private void setName(String newName) {
 	  m_name=newName;
 	}
 
 	private float interpolate(float from,float to,int steps,int pos,boolean easeOut,boolean easeIn) {
 	  boolean ease=false;
 
 	  // do not start any calculation if there's nothing to do
 	  if(from==to) return from;
 
 	  if(pos<=(steps/2) && easeOut) ease=true;
 	  if(pos>(steps/2) && easeIn) ease=true;
 
 	  // sine interpolation for ease in / out
 	  if(ease)
 	  {
 	    float distance=to-from;
 	    float step=3.1415f/(steps);
 
 	    return from+(0.5f-(float)Math.cos(step*(float) pos)/2)*distance;
 	  }
 	  // classic linear interpolation
 	  else
 	  {
 	    float distance=to-from;
 	    float increment=distance/(float) steps;
 	    return from+increment*(float) pos;
 	  }
 	}
 
 
 	private int getKeyframeNumberBefore(int frame) {
 	  if(frame==0)
 	  {
 	    // should never happen
 	    Log.d(TAG, "BVHNode::getKeyframeNumberBefore(int frame): frame==0!");
 	    return 0;
 	  }
 
 	  // find previous key
 	  while(--frame > 0 && !isKeyframe(frame)) {};
 
 	  return frame;
 	}
 
 	private int getKeyframeNumberAfter(int frame) {
 	  // get a list of all keyframe numbers
 	  Integer[] keys=keyframeList();
 
 	  // past the end? return -1
 	  if(frame>(int) keys[keyframes.size()-1])
 	    return -1;
 
 	  // find next key
 	  while(++frame != 0 && !isKeyframe(frame)) {};
 
 	  return frame;
 	}
 
 
 	// mirrors the keyframes inside of this node
 	private void mirrorKeys() {
 	  for(int frame:keyframeList()) {
 	    if(type==BVHNodeType.BVH_POS) {
 	      Position pos=frameData(frame).position();
 	      pos.x=-pos.x;
 	      setKeyframePosition(frame,pos);
 	    } else {
 	      Rotation rot=frameData(frame).rotation();
 	      rot.y=-rot.y;
 	      rot.z=-rot.z;
 	      setKeyframeRotation(frame,rot);
 	    }
 	  }
 	}
 }
