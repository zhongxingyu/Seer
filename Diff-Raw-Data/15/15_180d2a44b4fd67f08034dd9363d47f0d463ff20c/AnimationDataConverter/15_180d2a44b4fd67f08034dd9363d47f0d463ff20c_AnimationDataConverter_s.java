 package com.akjava.gwt.bvh.client.threejs;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.akjava.bvh.client.BVH;
 import com.akjava.bvh.client.BVHNode;
 import com.akjava.bvh.client.Channels;
 import com.akjava.bvh.client.NameAndChannel;
 import com.akjava.gwt.three.client.gwt.GWTThreeUtils;
 import com.akjava.gwt.three.client.gwt.animation.AnimationBone;
 import com.akjava.gwt.three.client.gwt.animation.AnimationData;
 import com.akjava.gwt.three.client.gwt.animation.AnimationHierarchyItem;
 import com.akjava.gwt.three.client.gwt.animation.AnimationKey;
 import com.akjava.gwt.three.client.gwt.animation.AnimationUtils;
 import com.akjava.gwt.three.client.js.THREE;
 import com.akjava.gwt.three.client.js.core.Object3D;
 import com.akjava.gwt.three.client.js.math.Matrix4;
 import com.akjava.gwt.three.client.js.math.Quaternion;
 import com.akjava.gwt.three.client.js.math.Vector3;
 import com.google.gwt.core.client.JsArray;
 
 public class AnimationDataConverter {
 	
 	//list of bone names
 	private List<String> nameOrderList;
 	
 	private boolean skipFirst=true;
 	public boolean isSkipFirst() {
 		return skipFirst;
 	}
 
 	public void setSkipFirst(boolean skipFirst) {
 		this.skipFirst = skipFirst;
 	}
 
 	
 	
 	
 	
 	public AnimationData convertJsonAnimation(BVH bvh){
 		return convertJsonAnimation(bvh.getHiearchy(),bvh);
 	}
 	
 	public AnimationData convertJsonAnimation(JsArray<AnimationBone> bones,BVH bvh){
 		BVHNode rootNode=new BVHConverter().convertBVHNode(bones);
 		
 		//maybe same as <AnimationBone>
 		nameOrderList=new ArrayList<String>();
 		String oldName=null;
 		for(int i=0;i<bvh.getNameAndChannels().size();i++){
 			String newName=bvh.getNameAndChannels().get(i).getName();
 
 			if(!newName.equals(oldName)){
 				nameOrderList.add(newName);
 				oldName=newName;
 			}
 		}
 	//	LogUtils.log("xx1");
 		/*
 		List<Quaternion> boneQ=new ArrayList<Quaternion>();
 		for(int i=0;i<bones.length();i++){
 			boneQ.add(GWTThreeUtils.jsArrayToQuaternion(bones.get(i).getRotq()));
 		}
 		*/
 		
 		//boneMap = new HashMap<String, Matrix4>();
 		
 		
 		AnimationData data=AnimationUtils.createAnimationData();
 		parentIdMaps=new HashMap<String,Integer>();
 		jointMap=new HashMap<String,Object3D>();
 		matrixMap=new HashMap<String,Matrix4>();
 		angleMap=new HashMap<String,Vector3>();
 		for(int i=0;i<nameOrderList.size();i++){
 			//parentIdMaps.put(nameOrderList.get(i), i);
 			jointMap.put(nameOrderList.get(i), THREE.Object3D());
 		}
 		for(int i=0;i<bones.length();i++){
 			parentIdMaps.put(bones.get(i).getName(), i);
 		}
 //		LogUtils.log("2");
 		//create hierarchy
 		Map<String,AnimationHierarchyItem> hmap=new HashMap<String,AnimationHierarchyItem>();
 		
 		AnimationHierarchyItem rootItem=AnimationUtils.createAnimationHierarchyItem();
 		rootItem.setParent(-1);
 		
 		hmap.put(rootNode.getName(), rootItem);
 		convert(hmap,rootNode);
 		
 		//List<AnimationHierarchyItem> aList=new ArrayList<AnimationHierarchyItem>();
 		
 		//IdNames=new HashMap<Integer,String>();
 		//LogUtils.log("nc:"+nameOrderList.size());
 		
 		/*
 		for(int i=0;i<nameOrderList.size();i++){
 			AnimationHierarchyItem abone=hmap.get(nameOrderList.get(i));
 			data.getHierarchy().push(abone);
 			//IdNames.put(i, bvh.getNameAndChannels().get(i).getName());
 		}*/
 		//create bones order by bones
 		for(int i=0;i<bones.length();i++){
 			AnimationHierarchyItem abone=hmap.get(bones.get(i).getName());
 			data.getHierarchy().push(abone);
 			
 		}
 	//	LogUtils.log("3");
 		
 		double ft=bvh.getFrameTime();
 		data.setName("BVHMotion");
 		data.setFps(30);//TODO change
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
 		
 	//	LogUtils.log("4");
 		for(int i=start;i<bvh.getFrames();i++){	
 			//get each joint rotation to object3
 			doPose(bvh,bvh.getFrameAt(i));
 			
 			//create matrix for key
 			matrixMap.clear();
 			angleMap.clear();
 			//BVHNode rootNode=bvh.getHiearchy();
 			Object3D o3d=jointMap.get(rootNode.getName());
 			if(o3d==null){
 				continue;//not found
 			}
 			Matrix4 mx=THREE.Matrix4();
 			
 			Vector3 bpos=THREE.Vector3();
 					bpos.add(o3d.getPosition(),BVHUtils.toVector3(rootNode.getOffset()));
 			//LogUtils.log(rootNode.getName()+","+bpos.getX()+","+bpos.getY()+","+bpos.getZ());
			mx.setPosition(bpos);
 			//mx.setRotationFromEuler(o3d.getRotation(), "XYZ");
 			mx.makeRotationFromEuler(o3d.getRotation());
 			//mx.multiply(nodeToMatrix(rootNode), mx);
 			matrixMap.put(rootNode.getName(), mx);
 			angleMap.put(rootNode.getName(), GWTThreeUtils.radiantToDegree(o3d.getRotation()));
 			doMatrix(rootNode);
 		//	LogUtils.log("5");
 			for(int j=0;j<nameOrderList.size();j++){
 				AnimationHierarchyItem item=hmap.get(nameOrderList.get(j));
 				if(item==null){
 					continue;
 				}
 				//AnimationHierarchyItem item=data.getHierarchy().get(j);
 				//create Key
 				Matrix4 matrix=matrixMap.get(nameOrderList.get(j));
 				Vector3 pos=THREE.Vector3();
 				pos.getPositionFromMatrix(matrix);
 				
 				Quaternion q=THREE.Quaternion();
 				q.setFromRotationMatrix(matrix);
 				
 				//q.multiplySelf(boneQ.get(j));
 				
 				
 				AnimationKey key=AnimationUtils.createAnimationKey();
 				key.setPos(pos);//key same as bone?
 				key.setRot(GWTThreeUtils.quaternionToJsArray(q));
 				key.setAngle(angleMap.get(nameOrderList.get(j)));
 				int frame=i;
 				if(skipFirst){
 					frame--;
 				}
 				key.setTime(ft*frame);
 				item.getKeys().push(key);
 			}
 			//LogUtils.log("6");
 			
 			
 		}
 		
 		
 			//check
 			Quaternion emptyQ=THREE.Quaternion();
 			Vector3 emptyAngle=THREE.Vector3();
 			for(int j=0;j<bones.length();j++){
 				
 				AnimationHierarchyItem item=hmap.get(bones.get(j).getName());
 				int totalFrames=bvh.getFrames();
 				if(skipFirst){
 					totalFrames--;
 				}
 				if(item.getKeys().length()==0){
 					for(int i=0;i<totalFrames;i++){
 					//add empty frame
 					AnimationKey key=AnimationUtils.createAnimationKey();
 					key.setPos(GWTThreeUtils.jsArrayToVector3(bones.get(j).getPos()));
 					key.setRot(GWTThreeUtils.quaternionToJsArray(emptyQ));
 					key.setAngle(emptyAngle);
 					
 					key.setTime(ft*i);
 					item.getKeys().push(key);
 					}
 				}
 			
 		}
 		
 		return data;
 	}
 	
 	
 	public AnimationData convertJsonAnimation(BVHNode rootNode,BVH bvh){
 		//maybe same as <AnimationBone>
 		nameOrderList=new ArrayList<String>();
 		String oldName=null;
 		for(int i=0;i<bvh.getNameAndChannels().size();i++){
 			String newName=bvh.getNameAndChannels().get(i).getName();
 
 			if(!newName.equals(oldName)){
 				nameOrderList.add(newName);
 				oldName=newName;
 			}
 		}
 		
 		/*
 		List<Quaternion> boneQ=new ArrayList<Quaternion>();
 		for(int i=0;i<bones.length();i++){
 			boneQ.add(GWTThreeUtils.jsArrayToQuaternion(bones.get(i).getRotq()));
 		}
 		*/
 		
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
 		
 		AnimationHierarchyItem rootItem=AnimationUtils.createAnimationHierarchyItem();
 		rootItem.setParent(-1);
 		
 		hmap.put(rootNode.getName(), rootItem);
 		convert(hmap,rootNode);
 		
 		//List<AnimationHierarchyItem> aList=new ArrayList<AnimationHierarchyItem>();
 		
 		//IdNames=new HashMap<Integer,String>();
 		//LogUtils.log("nc:"+nameOrderList.size());
 		for(int i=0;i<nameOrderList.size();i++){
 			AnimationHierarchyItem abone=hmap.get(nameOrderList.get(i));
 			data.getHierarchy().push(abone);
 			//IdNames.put(i, bvh.getNameAndChannels().get(i).getName());
 		}
 		
 		
 		double ft=bvh.getFrameTime();
 		data.setName("BVHMotion");
 		data.setFps(30);//TODO change
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
 			//BVHNode rootNode=bvh.getHiearchy();
 			Object3D o3d=jointMap.get(rootNode.getName());
 			if(o3d==null){
 				continue;//not found
 			}
 			Matrix4 mx=THREE.Matrix4();
 			
 			Vector3 bpos=THREE.Vector3();
 					bpos.add(o3d.getPosition(),BVHUtils.toVector3(rootNode.getOffset()));
 			//LogUtils.log(rootNode.getName()+","+bpos.getX()+","+bpos.getY()+","+bpos.getZ());
			mx.setPosition(bpos);
 			mx.makeRotationFromEuler(o3d.getRotation());
 			//mx.multiply(nodeToMatrix(rootNode), mx);
 			matrixMap.put(rootNode.getName(), mx);
 			angleMap.put(rootNode.getName(), GWTThreeUtils.radiantToDegree(o3d.getRotation()));
 			doMatrix(rootNode);
 			
 			for(int j=0;j<nameOrderList.size();j++){
 				AnimationHierarchyItem item=hmap.get(nameOrderList.get(j));
 				if(item==null){
 					continue;
 				}
 				//AnimationHierarchyItem item=data.getHierarchy().get(j);
 				//create Key
 				Matrix4 matrix=matrixMap.get(nameOrderList.get(j));
 				Vector3 pos=THREE.Vector3();
 				pos.getPositionFromMatrix(matrix);
 				
 				Quaternion q=THREE.Quaternion();
 				q.setFromRotationMatrix(matrix);
 				
 				//q.multiplySelf(boneQ.get(j));
 				
 				
 				AnimationKey key=AnimationUtils.createAnimationKey();
 				key.setPos(pos);//key same as bone?
 				key.setRot(GWTThreeUtils.quaternionToJsArray(q));
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
 			if(o3d==null){
 				continue;//not found
 			}
 			//GWT.log(message);
 			Matrix4 mx=THREE.Matrix4();
 			Vector3 mpos=THREE.Vector3();
 			mpos.add(o3d.getPosition(), BVHUtils.toVector3(children.getOffset()));
 			//LogUtils.log("doMatrix:"+children.getName()+",o3d="+ThreeLog.get(o3d.getPosition())+",childoffset="+children.getOffset());
			mx.setPosition(mpos);
 			mx.makeRotationFromEuler(o3d.getRotation());
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
 	/**
 	 * this is just set order
 	 * @param target
 	 * @param lastOrder
 	 */
 	private void setRotation(Object3D target,String lastOrder){
 		target.getRotation().setOrder(lastOrder);
 	}
 }
