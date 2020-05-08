 package com.chap.memo.memoNodes.model;
 
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.HashSet;
 
 import com.chap.memo.memoNodes.MemoNode;
 import com.chap.memo.memoNodes.bus.MemoReadBus;
 import com.chap.memo.memoNodes.bus.MemoWriteBus;
 import com.eaio.uuid.UUID;
 
 public class ArcList {
 	MemoReadBus readBus = MemoReadBus.getBus();
 	MemoWriteBus writeBus = MemoWriteBus.getBus();
 	long lastUpdate = 0;
 
 	// UUID[] nodes = new UUID[0];
 	ArrayList<ArcOp> arcops = null;
 
 	int type; // 0: parent list, 1:child list
 	UUID nodeId;
 
 	private long timestamp;
 
 	public ArcList(UUID nodeId, int type) {
 		this.type = type;
 		this.nodeId = nodeId;
 	}
 
 	public long getTimestamp_long() {
 		return this.timestamp;
 	}
 
 	public UUID[] update() {
 		if (this.arcops == null || readBus.opsChanged(lastUpdate)) {
 			if (this.arcops == null) {
 				this.arcops = readBus.getOps(nodeId, type, 0);
 			} else {
 				this.arcops.addAll(readBus.getOps(nodeId, type, lastUpdate));
 			}
			Collections.sort(this.arcops);
 			lastUpdate = System.currentTimeMillis();
 		}
 		return ops2nodes();
 	}
 
 	public ArrayList<MemoNode> getNodes(long timestamp) {
 		if (this.arcops == null) {
 			this.arcops = readBus.getOps(nodeId, type, timestamp, 0);
 		} else {
 			this.arcops.addAll(readBus.getOps(nodeId, type, timestamp,
 					lastUpdate));
 		}
		Collections.sort(this.arcops);
		
 		UUID[] nodes = ops2nodes();
 		ArrayList<MemoNode> result = new ArrayList<MemoNode>(nodes.length);
 		for (UUID id : nodes) {
 			result.add(readBus.find(id, timestamp));
 		}
 		return result;
 	}
 
 	public UUID[] getNodesIds() {
 		return update();
 	}
 
 	public ArrayList<MemoNode> getNodes() {
 		UUID[] nodes = update();
 		ArrayList<MemoNode> result = new ArrayList<MemoNode>(nodes.length);
 		for (UUID id : nodes) {
 			result.add(new MemoNode(id));
 		}
 		return result;
 	}
 
 	public int getLength() {
 		return update().length;
 	}
 
 	public void addNode(UUID other) {
 		if (this.arcops == null) { // small performance gain
 			update();
 		}
 		UUID[] arc = new UUID[2];
 		arc[this.type] = other;
 		arc[Math.abs(this.type - 1)] = this.nodeId;
 
 		ArcOp op = new ArcOp(OpsType.ADD, arc, System.currentTimeMillis());
 		writeBus.store(op);
 		arcops.add(op);
 	}
 
 	public void delNode(UUID other) {
 		if (this.arcops == null) { // small performance gain
 			update();
 		}
 		UUID[] arc = new UUID[2];
 		arc[this.type] = other;
 		arc[Math.abs(this.type - 1)] = this.nodeId;
 
 		ArcOp op = new ArcOp(OpsType.DELETE, arc, System.currentTimeMillis());
 		writeBus.store(op);
 		arcops.add(op);
 	}
 
 	public void clear() {
 		UUID[] nodes = update();
 		arcops.ensureCapacity(arcops.size() + nodes.length);
 		for (UUID other : nodes) {
 			this.delNode(other);
 		}
 	}
 
 	private UUID[] ops2nodes() {
 		if (arcops == null)
 			arcops = new ArrayList<ArcOp>(10);
 		HashSet<UUID> nodeList = new HashSet<UUID>(arcops.size() / 2);
 		for (ArcOp op : arcops) {
 			switch (op.getType()) {
 			case ADD:
 				nodeList.add(op.get(this.type));
 				break;
 			case DELETE:
 				nodeList.remove(op.get(this.type));
 				break;
 			}
 		}
 		if (arcops.size() > 0) {
 			this.timestamp = arcops.get(arcops.size() - 1).getTimestamp_long();
 		}
 		return nodeList.toArray(new UUID[0]);
 	}
 }
