 package com.akjava.bvh.client.threejs;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.akjava.bvh.client.BVH;
 import com.akjava.bvh.client.BVHNode;
 import com.akjava.bvh.client.Channels;
 import com.akjava.bvh.client.NameAndChannel;
 import com.akjava.gwt.lib.client.LogUtils;
 import com.akjava.gwt.three.client.THREE;
 import com.akjava.gwt.three.client.core.Matrix4;
 import com.akjava.gwt.three.client.core.Object3D;
 import com.akjava.gwt.three.client.core.Quaternion;
 import com.akjava.gwt.three.client.core.Vector3;
 import com.akjava.gwt.three.client.gwt.GWTThreeUtils;
 import com.akjava.gwt.three.client.gwt.animation.AnimationBone;
 import com.akjava.gwt.three.client.gwt.animation.AnimationData;
 import com.akjava.gwt.three.client.gwt.animation.AnimationHierarchyItem;
 import com.akjava.gwt.three.client.gwt.animation.AnimationKey;
 import com.akjava.gwt.three.client.gwt.animation.AnimationUtils;
 import com.google.gwt.core.client.JsArray;
 
 public class AnimationDataConverter {
 	
 	
 	private List<String> nameOrderList;
 	
 	private boolean skipFirst=true;
 	public boolean isSkipFirst() {
 		return skipFirst;
 	}
 
 	public void setSkipFirst(boolean skipFirst) {
 		this.skipFirst = skipFirst;
 	}
 
 	public AnimationData convertJsonAnimation(JsArray<AnimationBone> bones,BVH bvh){
 		
 		nameOrderList=new ArrayList<String>();
 		String oldName=null;
 		for(int i=0;i<bvh.getNameAndChannels().size();i++){
 			String newName=bvh.getNameAndChannels().get(i).getName();
 
 			if(!newName.equals(oldName)){
 				nameOrderList.add(newName);
 				oldName=newName;
 			}
 		}
 		//maybe same as bone
 		List<Quaternion> boneQ=new ArrayList<Quaternion>();
 		for(int i=0;i<bones.length();i++){
 			boneQ.add(GWTThreeUtils.jsArrayToQuaternion(bones.get(i).getRotq()));
 		}
 		
 		//boneMap = new HashMap<String, Matrix4>();
 		
 		
 		AnimationData data=AnimationUtils.createAnimationData();
 		parentIdMaps=new HashMap<String,Integer>();
 		jointMap=new HashMap<String,Object3D>();
 		matrixMap=new HashMap<String,Matrix4>();
 		angleMap=new HashMap<String,Vector3>();
 		for(int i=0;i<nameOrderList.size();i++){
 			parentIdMaps.put(nameOrderList.get(i), i);
 			jointMap.put(nameOrderList.get(i), THREE.Object3D());
 		}
 		
 		//create hierarchy
 		Map<String,AnimationHierarchyItem> hmap=new HashMap<String,AnimationHierarchyItem>();
 		BVHNode root=bvh.getHiearchy();
 		AnimationHierarchyItem rootItem=AnimationUtils.createAnimationHierarchyItem();
 		rootItem.setParent(-1);
 		
 		hmap.put(root.getName(), rootItem);
 		convert(hmap,root);
 		//List<AnimationHierarchyItem> aList=new ArrayList<AnimationHierarchyItem>();
 		
 		//IdNames=new HashMap<Integer,String>();
 		LogUtils.log("nc:"+nameOrderList.size());
 		for(int i=0;i<nameOrderList.size();i++){
 			AnimationHierarchyItem abone=hmap.get(nameOrderList.get(i));
 			data.getHierarchy().push(abone);
 			//IdNames.put(i, bvh.getNameAndChannels().get(i).getName());
 		}
 		
 		
 		double ft=bvh.getFrameTime();
 		data.setName("BVHMotion");
 		data.setFps(30);//noway
 		int minus=1;
 		if(skipFirst){
 			minus++;
 		}
 		data.setLength(ft*(bvh.getFrames()-minus));
 		//convert each frame
 		int start=0;
 		if(skipFirst){
 			start=1;
 		}
 		for(int i=start;i<bvh.getFrames();i++){	
 			//get each joint rotation to object3
 			doPose(bvh,bvh.getFrameAt(i));
 			
 			//create matrix for key
 			matrixMap.clear();
 			angleMap.clear();
 			BVHNode rootNode=bvh.getHiearchy();
 			Object3D o3d=jointMap.get(rootNode.getName());
 			Matrix4 mx=THREE.Matrix4();
 			
 			Vector3 bpos=THREE.Vector3();
 					bpos.add(o3d.getPosition(),BVHUtils.toVector3(rootNode.getOffset()));
 			//LogUtils.log(rootNode.getName()+","+bpos.getX()+","+bpos.getY()+","+bpos.getZ());
 			mx.setPosition(bpos);
 			mx.setRotationFromEuler(o3d.getRotation(), "XYZ");
 			//mx.multiply(nodeToMatrix(rootNode), mx);
 			matrixMap.put(rootNode.getName(), mx);
 			angleMap.put(rootNode.getName(), GWTThreeUtils.radiantToDegree(o3d.getRotation()));
 			doMatrix(rootNode);
 			
 			for(int j=0;j<nameOrderList.size();j++){
 				AnimationHierarchyItem item=data.getHierarchy().get(j);
 				//create Key
 				Matrix4 matrix=matrixMap.get(nameOrderList.get(j));
 				Vector3 pos=THREE.Vector3();
 				pos.setPositionFromMatrix(matrix);
 				
 				Quaternion q=THREE.Quaternion();
 				q.setFromRotationMatrix(matrix);
 				
 				//q.multiplySelf(boneQ.get(j));
 				
 				
 				AnimationKey key=AnimationUtils.createAnimationKey();
 				key.setPos(pos);//key same as bone?
 				key.setRot(q);
 				key.setAngle(angleMap.get(nameOrderList.get(j)));
 				int frame=i;
 				if(skipFirst){
 					frame--;
 				}
 				key.setTime(ft*frame);
 				item.getKeys().push(key);
 			}
 		}
 		
 		return data;
 	}
 	//private void BoneTo
 	
 	private Matrix4 nodeToMatrix(BVHNode node){
 		Matrix4 mx=THREE.Matrix4();
 		mx.setPosition(BVHUtils.toVector3(node.getOffset()));
 		return mx;
 	}
 	
 	private void doMatrix(BVHNode parent) {
 		for(BVHNode children:parent.getJoints()){
 			Object3D o3d=jointMap.get(children.getName());
 			//GWT.log(message);
 			Matrix4 mx=THREE.Matrix4();
 			Vector3 mpos=THREE.Vector3();
 			mpos.add(o3d.getPosition(), BVHUtils.toVector3(children.getOffset()));
 			mx.setPosition(mpos);
 			mx.setRotationFromEuler(o3d.getRotation(), "XYZ");
 			//mx=mx.multiply(nodeToMatrix(children), mx);
 			
 			Matrix4 parentM=matrixMap.get(parent.getName());
 			
 			//TODO If you wish absolutepath use parent matrix,but this version format dont need it.
 			//mx=mx.multiply(parentM, mx);
 			matrixMap.put(children.getName(), mx);
			angleMap.put(children.getName(), GWTThreeUtils.radiantToDegree(o3d.getRotation()));
 			doMatrix(children);
 		}
 	}
 	private Map<String,Vector3> angleMap;
 	private Map<String,Matrix4> matrixMap;
 	private Map<String,Object3D> jointMap;
 	private Map<String,Integer> parentIdMaps;
 	//private Map<String,Matrix4> boneMap;
 	//private Map<Integer,String> IdNames;
 	
 	private  void convert(Map<String,AnimationHierarchyItem> map,BVHNode parent){
 		for(BVHNode children:parent.getJoints()){
 			AnimationHierarchyItem item=AnimationUtils.createAnimationHierarchyItem();
 			item.setParent(parentIdMaps.get(parent.getName()));
 			
 			map.put(children.getName(), item);
 			convert(map,children);
 		}
 	}
 	
 	private void doPose(BVH bvh,double[] vs){
 		Object3D oldTarget=null;
 		String lastOrder=null;
 		for(int i=0;i<vs.length;i++){
 			NameAndChannel nchannel=bvh.getNameAndChannels().get(i);
 			lastOrder=nchannel.getOrder();
 			Object3D target=jointMap.get(nchannel.getName());
 			switch(nchannel.getChannel()){
 			case Channels.XROTATION:
 				target.getRotation().setX(Math.toRadians(vs[i]));
 				
 			break;
 			case Channels.YROTATION:
 				target.getRotation().setY(Math.toRadians(vs[i]));
 			break;
 			case Channels.ZROTATION:
 				target.getRotation().setZ(Math.toRadians(vs[i]));
 			break;
 			case Channels.XPOSITION:
 				target.getPosition().setX(vs[i]);
 				
 				break;
 			case Channels.YPOSITION:
 				target.getPosition().setY(vs[i]);
 				
 				break;
 			case Channels.ZPOSITION:
 				target.getPosition().setZ(vs[i]);
 				
 				break;	
 			}
 			
 			if(oldTarget!=null && oldTarget!=target){
 				setRotation(oldTarget,lastOrder);
 			}
 			oldTarget=target;
 		}
 		setRotation(oldTarget,lastOrder);//do last one
 	}
 	
 	private void setRotation(Object3D target,String lastOrder){
 		Vector3 vec=target.getRotation();
 		Matrix4 mx=THREE.Matrix4();
 		mx.setRotationFromEuler(vec, lastOrder);
 		
 		vec.setRotationFromMatrix(mx);
 	}
 }
