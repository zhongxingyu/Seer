 package com.chap.memo.memoNodes;
 
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Map;
 import java.util.TreeSet;
 
 import com.eaio.uuid.UUID;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.QueryResultList;
 
 public class MemoReadBus {
 	//Shard caches:
 	static Map<Key, NodeValueShard> NodeValueShards = Collections
 			.synchronizedMap(new MyMap<Key, NodeValueShard>(5, new Float(
 					0.75), true));
 	static Map<Key, ArcOpShard> ArcOpShards = Collections
			.synchronizedMap(new MyMap<Key, ArcOpShard>(20, new Float(
 					0.75), true));
 	
 	TreeSet<NodeValueIndex> NodeValueIndexes = new TreeSet<NodeValueIndex>();
 	TreeSet<ArcOpIndex> ArcOpIndexes = new TreeSet<ArcOpIndex>();
 	DatastoreService datastore = null;
 	long lastValueChange= new Date().getTime();
 	long lastOpsChange= new Date().getTime();
 	long lastIndexesRun = 0;
 	
 	private final static MemoReadBus bus = new MemoReadBus();
 	
 	public void updateIndexes(){
 		loadIndexes(false,lastIndexesRun-10000);
 	}
 	public void loadIndexes(boolean clear, long sinceTimestamp){
 		if (datastore == null) datastore = DatastoreServiceFactory.getDatastoreService();
 		if (clear) {
 			NodeValueIndexes.clear();
 			ArcOpIndexes.clear();
 			lastValueChange= new Date().getTime();
 			lastOpsChange= new Date().getTime();
 		}
 		Query q =new Query("NodeValueIndex").addSort("timestamp");
 		if (sinceTimestamp > 0){
 			q.addFilter("timestamp", FilterOperator.GREATER_THAN_OR_EQUAL, sinceTimestamp);
 		}
 		PreparedQuery NodeValueIndexQuery = datastore.prepare(q);
 		QueryResultList<Entity> rl = NodeValueIndexQuery.asQueryResultList(withLimit(100));
 		if (rl.size() > 0) {
 			for (Entity ent : rl){
 				NodeValueIndex index = (NodeValueIndex) MemoStorable.load(ent);
 				NodeValueIndexes.add(index);
 			}
 			lastValueChange= new Date().getTime();
 		}
 		
 		q = new Query("ArcOpIndex").addSort("timestamp");
 		if (sinceTimestamp > 0){
 			q.addFilter("timestamp", FilterOperator.GREATER_THAN_OR_EQUAL, sinceTimestamp);
 		}
 		PreparedQuery ArcOpIndexQuery = datastore.prepare(q);
 		rl = ArcOpIndexQuery.asQueryResultList(withLimit(1000));
 		if (rl.size() > 0) {
 			for (Entity ent : rl){
 				ArcOpIndex index = (ArcOpIndex) MemoStorable.load(ent);
 				ArcOpIndexes.add(index);
 			}
 			lastOpsChange= new Date().getTime();
 		}
 	
 	}
 	
 	public void addValueIndex(NodeValueIndex index,NodeValueShard shard){
 		NodeValueShards.put(index.shardKey, shard);
 		NodeValueIndexes.add(index);
 		lastValueChange= new Date().getTime();
 	}
 	public void addOpsIndex(ArcOpIndex index,ArcOpShard shard){
 		ArcOpShards.put(index.shardKey, shard);
 		ArcOpIndexes.add(index);
 		lastOpsChange= new Date().getTime();
 	}
 	private MemoReadBus(){
 		loadIndexes(false,0);
 	};
 	
 	public static MemoReadBus getBus(){
 		return bus;
 	}
 	
 	public boolean valueChanged(long timestamp){
 		return timestamp<=lastValueChange;
 	}
 	public boolean opsChanged(long timestamp){
 		return timestamp<=lastOpsChange;
 	}
 	
 	public MemoNode find(UUID uuid){
 		return new MemoNode(getValue(uuid));
 	}
 	public MemoNode find(UUID uuid,long timestamp){
 		return new MemoNode(getValue(uuid,timestamp));
 	}
 	public ArrayList<MemoNode> findAll(UUID uuid){
 		ArrayList<MemoNode> result = new ArrayList<MemoNode>(100);
 		if (NodeValueIndexes.size() <=0) return result;
 		NodeValueIndex index = NodeValueIndexes.last();
 		while (index != null){
 			if (index.nodeIds.contains(uuid)){
 				NodeValueShard shard;
 				synchronized(NodeValueShards){
 					if (NodeValueShards.containsKey(index.shardKey)){
 						shard = NodeValueShards.get(index.shardKey);
 					} else {
 						shard = (NodeValueShard) MemoStorable.load(index.shardKey); 
 					}
 				}
 				NodeValueShards.put(shard.myKey,shard);
 				for (NodeValue nv : shard.findAll(uuid)){
 					result.add(new MemoNode(nv));
 				}
 			}
 			index = NodeValueIndexes.lower(index);	
 		}
 		Collections.sort(result);
 		return result;
 	}
 	public NodeValue getValue(UUID uuid){
 		return getValue(uuid,new Date().getTime());
 	}
 	public NodeValue getValue(UUID uuid,long timestamp){
 		NodeValue result = null;
 
 		MemoWriteBus writeBus = MemoWriteBus.getBus();
 		result = writeBus.values.findBefore(uuid, timestamp);
 		
 		if (NodeValueIndexes.size() == 0) return result;
 		NodeValueIndex index = NodeValueIndexes.last();
 		while (index != null && (result == null || index.newest > result.getTimestamp_long())){
 			if (index.oldest < timestamp && index.nodeIds.contains(uuid)){
 				NodeValueShard shard=null;
 				synchronized(NodeValueShards){
 					if (NodeValueShards.containsKey(index.shardKey)){
 						shard = NodeValueShards.get(index.shardKey);
 					}
 				}
 				if (shard ==null) {
 					shard = (NodeValueShard) MemoStorable.load(index.shardKey); 
 				}
 				NodeValueShards.put(shard.myKey,shard);
 				NodeValue res = shard.findBefore(uuid,timestamp); 
 				if (result == null || res.getTimestamp_long()>result.getTimestamp_long()){
 					result = res;
 				}
 			}
 			index = NodeValueIndexes.lower(index);
 		}		
 		return result;
 	}
 	public ArrayList<ArcOp> getOps(UUID uuid,int type){
 		return getOps(uuid,type,new Date().getTime());
 	}
 	public ArrayList<ArcOp> getOps(UUID uuid, int type, long timestamp){
 		ArrayList<ArcOp> result = new ArrayList<ArcOp>(100);	
 		
 		if (ArcOpIndexes.size() <=0) return result;
 		ArcOpIndex index = ArcOpIndexes.first();
 		while (index != null){
 			switch (type){
 			case 0: //parentList, UUID is child
 				if (index.children.contains(uuid)){
 					ArcOpShard shard=null;
 					synchronized(NodeValueShards){
 						if (ArcOpShards.containsKey(index.shardKey)){
 							shard = ArcOpShards.get(index.shardKey);
 						}
 					}
 					if (shard==null) {
 						shard = (ArcOpShard) MemoStorable.load(index.shardKey); 
 					}
 					ArcOpShards.put(shard.myKey,shard);
 					for (ArcOp op : shard.getChildOps(uuid)){
 						if (op.getTimestamp_long()<= timestamp){
 							result.add(op);
 						}
 					}	
 				}
 				break;
 			case 1: //parentList, UUID is child
 				if (index.parents.contains(uuid)){
 					ArcOpShard shard=null;
 					synchronized(NodeValueShards){
 						if (ArcOpShards.containsKey(index.shardKey)){
 							shard = ArcOpShards.get(index.shardKey);
 						}
 					}
 					if (shard==null) {
 						shard = (ArcOpShard) MemoStorable.load(index.shardKey); 
 					}
 					ArcOpShards.put(shard.myKey,shard);
 					for (ArcOp op : shard.getParentOps(uuid)){
 						if (op.getTimestamp_long()<= timestamp){
 							result.add(op);
 						}
 					}	
 				}
 				break;
 			}
 			index = ArcOpIndexes.higher(index);
 		}
 		switch (type){
 		case 0: //parentList, UUID is child
 			for (ArcOp op : MemoWriteBus.getBus().ops.getChildOps(uuid)){
 				if (op.getTimestamp_long()<= timestamp){
 					result.add(op);
 				}
 			}
 			break;
 		case 1:
 			for (ArcOp op : MemoWriteBus.getBus().ops.getParentOps(uuid)){
 				if (op.getTimestamp_long()<= timestamp){
 					result.add(op);
 				}
 			}
 			break;			
 		}
 		Collections.sort(result);
 		return result;
 	}
 	
 }
