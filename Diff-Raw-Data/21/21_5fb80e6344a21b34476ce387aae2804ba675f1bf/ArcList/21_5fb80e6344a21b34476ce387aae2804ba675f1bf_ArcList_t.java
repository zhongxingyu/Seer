 package com.chap.memo.memoNodes;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 
 import com.eaio.uuid.UUID;
 
 public class ArcList {
 	MemoReadBus readBus = MemoReadBus.getBus();
 	MemoWriteBus writeBus = MemoWriteBus.getBus();
 	long lastUpdate = 0;
 	
 	UUID[] nodes = new UUID[0];
 	ArrayList<ArcOp> arcops = null;
 	int type; //0: parent list, 1:child list
 	UUID nodeId;
 	
 	private long timestamp;
 	
 	public ArcList(UUID nodeId,int type){
 		this.type=type;
 		this.nodeId=nodeId;
 	}
 	//TODO: get ArcList @timestamp, don't update!
 	
 	public long getTimestamp_long(){
 		return this.timestamp;
 	}
 	public ArrayList<MemoNode> getNodes(long timestamp){
 		this.arcops=readBus.getOps(nodeId,type,timestamp);
 		ops2nodes();
 		ArrayList<MemoNode> result = new ArrayList<MemoNode>(
 				this.nodes.length);
 		for (UUID id : this.nodes) {
 			result.add(readBus.find(id));
 		}
 		return result;
 	}
 	public ArrayList<MemoNode> getNodes(){
 		if (this.arcops == null || readBus.opsChanged(lastUpdate)){
 			this.arcops=readBus.getOps(nodeId,type);
 			ops2nodes();
 			lastUpdate=new Date().getTime();
 		}
 		ArrayList<MemoNode> result = new ArrayList<MemoNode>(
 				this.nodes.length);
 		for (UUID id : this.nodes) {
 			result.add(readBus.find(id));
 		}
 		return result;
 	}
 	public int getLength(){
		if (this.arcops == null || readBus.opsChanged(lastUpdate)){
			this.arcops=readBus.getOps(nodeId,type);
			ops2nodes();
			lastUpdate=new Date().getTime();
		}
 		return this.nodes.length;
 	}
 	public void addNode(UUID other){
		if (this.arcops == null){
			this.arcops=readBus.getOps(nodeId,type);
			lastUpdate=new Date().getTime();
		}
 		UUID[] arc = new UUID[2];
 		arc[this.type]=other;
 		arc[Math.abs(this.type-1)]=this.nodeId;
 		
 		ArcOp op=new ArcOp(Ops.ADD, arc, new Date());
 		writeBus.store(op);
 		arcops.add(op);
 	}
 	public void delNode(UUID other){
		if (this.arcops == null){
			this.arcops=readBus.getOps(nodeId,type);
			lastUpdate=new Date().getTime();
		}
 		UUID[] arc = new UUID[2];
 		arc[this.type]=other;
 		arc[Math.abs(this.type-1)]=this.nodeId;
 
 		ArcOp op=new ArcOp(Ops.DELETE,arc, new Date());
 		writeBus.store(op);
 		arcops.add(op);
 	}
 	private void ops2nodes(){
 		HashSet<UUID> nodeList = new HashSet<UUID>(arcops.size());
 		for (ArcOp op : arcops){
 			switch(op.getType()){
 			case ADD:
 				nodeList.add(op.get(this.type));
 				break;
 			case DELETE:
 				nodeList.remove(op.get(this.type));
 				break;
 			}
 		}
 		if (arcops.size()>0){
 			this.timestamp = arcops.get(arcops.size()-1).getTimestamp_long();
 		}
 		this.nodes=nodeList.toArray(new UUID[0]);
 	}
 }
