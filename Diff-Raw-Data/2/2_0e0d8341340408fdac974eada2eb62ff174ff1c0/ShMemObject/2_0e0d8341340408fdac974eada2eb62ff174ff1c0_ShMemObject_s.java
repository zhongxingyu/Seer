 package mp.java;
 
 
 import mp.java.ITimestamp.Comparison;
 
 import org.codehaus.jackson.*;
 import org.codehaus.jackson.node.*;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import java.math.BigDecimal;
 import java.util.*;
 
 import org.apache.commons.lang3.tuple.*;
 
 /*
  * Users should use ShMemObject where the intended to use JSONObjects. A user may still
  * user JSONObjects but they will not be interpreted in any way. If both the acquiring and
  * releasing processes make changes to unrelated parts of a JSONObject, the updates will be
  * marked conflicting. 
  */
 public class ShMemObject extends ObjectNode {
 	
 	
 	private static JsonNodeFactory s_factory;
 	
 	// Points to the parent of this object in the recursive ShMemObject hierarchy. 
 	private ShMemObject parent;
 	
 	// The key the parent uses to refer to this object. We need the key to update timestamp
 	// information in the parent recursively. 
 	private String parent_key;
 	
 	// The current value of "time". All puts to an ShMemObject will take this value. The value
 	// is changed whenever we fork. 
 	public static int[] s_now;
 	
 	// Index of the current node in the timestamps. 
 	public static int cur_node_;
 	
 	// Initialize an empty ShMemObject. 
 	public ShMemObject() {
 		super(JsonNodeFactory.instance);
 		this.parent = null;
 		this.parent_key = "";
 		m_key_map = new HashMap<String, ListNode>();
 		m_sorted_keys = new InternalLinkedList();
 	}
 	
 	public HashMap<String, ListNode> m_key_map;
 	public InternalLinkedList m_sorted_keys;
 	
 	public class ListNode {
 		
 		public final String m_key;
 		public final int[] m_timestamp;
 		
 		public ListNode m_prev;
 		public ListNode m_next;
 		
 		public ListNode(String key, int[] timestamp) {
 			m_key = key;
 			m_timestamp = timestamp;
 			m_prev = null;
 			m_next = null;
 		}
 	}
 	
 	protected class InternalLinkedList {
 		
 		public ListNode m_head;
 		public ListNode m_tail;
 		
 		public InternalLinkedList() {
 			m_head = null;
 			m_tail = null;
 		}
 		
 		public void InsertFront(ListNode cur) {
 			if (m_head == null) {
 				m_head = cur;
 				m_tail = cur;
 			}
 			else {
 				cur.m_prev = null;
 				cur.m_next = m_head;
 				m_head.m_prev = cur;
 				m_head = cur;
 			}
 		}
 		
 		public void InsertLast(ListNode cur) {
 			if (m_head == null) {
 				m_head = cur;
 				m_tail = cur;
 			}
 			else {
 				cur.m_prev = m_tail;
 				m_tail.m_next = cur;
 				cur.m_next = null;
 				m_tail = cur;
 			}
 		}
 		
 		public void MoveFront(ListNode cur) {
 			
 			// If prev is null, the node is already at the front of the list. 
 			if (cur.m_prev != null) {
 				cur.m_prev.m_next = cur.m_next;
 				if (cur.m_next != null) {
 					cur.m_next.m_prev = cur.m_prev;
 				}
 			}
 		}
 		
 		public void Remove(ListNode cur) {
 			ListNode prev = cur.m_prev;
 			ListNode next = cur.m_next;
 			
 			// We're trying to remove a head node. 
 			if (prev == null) {
 				m_head = next;
 			}
 			else {
 				prev.m_next = next;
 			}
 			
 			// We're trying to remove a tail node. 
 			if (next == null) {
 				m_tail = prev;
 			}
 			else {
 				next.m_prev = prev;
 			}
 			
 			cur.m_prev = null;
 			cur.m_next = null;
 		}
 	}
 	
 	private static void fixTime(ShMemObject cur, int[] time) {
 		while (cur.parent != null) {
 			String key = cur.parent_key;
 			int[] cur_timestamp = cur.parent.m_key_map.get(key).m_timestamp;
 			Comparison comp = VectorTimestamp.Compare(cur_timestamp,  time);
			if (comp == Comparison.LT) {
 				VectorTimestamp.Union(cur_timestamp,  time);
 				
 				ListNode cur_node = cur.parent.m_key_map.get(cur.parent_key);
 				cur.parent.m_sorted_keys.MoveFront(cur_node);
 				
 				cur = cur.parent;
 			}
 			else {
 				break;
 			}
 		}
 	}
 	
 	private ArrayNode getNodeTimestamp(String key) {
 		return (ArrayNode)super.get(key).get("shmem_timestamp");
 	}
 	
 	
 	
 	public static ObjectNode get_diff_tree(ShMemObject obj, int[] ts) {
 		ObjectNode ret = ShMem.mapper.createObjectNode();
 		
 		// The keys are sorted in timestamp order, so we can stop iterating
 		// so long as ts is less than the current node's timestamp. 
 		for (ListNode cur = obj.m_sorted_keys.m_head; 
 			 cur != null && VectorTimestamp.Compare(ts, cur.m_timestamp) != Comparison.GT;
 			 cur = cur.m_next) {
 			
 			// Create a wrapper which will contain the actual value and its
 			// corresponding timestamp. The value is contained in value_tree
 			// which is recursively derived if the value is an object node. 
 			ObjectNode wrapper = ShMem.mapper.createObjectNode();
 			JsonNode value = obj.get(cur.m_key);
 			ArrayNode serialized_time = VectorTimestamp.toArrayNode(cur.m_timestamp);
 			JsonNode value_tree;
 			
 			// If the value is an object node, recursively derive its
 			// serialization. 
 			if (value.isObject()) {
 				value_tree = get_diff_tree((ShMemObject)value, ts);
 			}
 			else {
 				value_tree = value;
 			}
 			
 			// Finally, put value_tree and serialized_time into a wrapper which
 			// gets put into ret.
 			wrapper.put("value",  value_tree);
 			wrapper.put("shmem_timestamp",  serialized_time);
 			ret.put(cur.m_key,  wrapper);
 		}
 		
 		return ret;
 	}
 	
 	
 	private ObjectNode getWrapper(String key) {
 		return (ObjectNode)super.get(key);
 	}
 	
 	private void put_common(String fieldname, int[] timestamp) {
 		ListNode cur_node;
 		try {
 			cur_node = m_key_map.get(fieldname);
 		}
 		catch (Exception e) {
 			cur_node = null;
 		}
 		if (cur_node == null) {
 			
 			// Allocate a list node for the new key. 
 			cur_node = new ListNode(fieldname, timestamp);
 			m_key_map.put(fieldname,  cur_node);
 			m_sorted_keys.InsertFront(cur_node);
 		}
 		else {	// There already exists a list node and timestamp. 
 			
 			VectorTimestamp.CopyFromTo(timestamp, cur_node.m_timestamp);
 			m_sorted_keys.MoveFront(cur_node);
 		}
 	}
 	
 	@Override
 	public void put(String fieldname, BigDecimal v) {
 		int[] new_timestamp = VectorTimestamp.Copy(s_now);
 		put_common(fieldname, new_timestamp);
 		super.put(fieldname,  v);
 		fixTime(this, s_now);
 	}
 	
 	@Override
 	public void put(String fieldname, boolean v) {
 		int[] new_timestamp = VectorTimestamp.Copy(s_now);
 		put_common(fieldname, new_timestamp);
 		super.put(fieldname,  v);
 		fixTime(this, s_now);
 	}
 	
 	@Override
 	public void put(String fieldname, byte[] v) {
 		int[] new_timestamp = VectorTimestamp.Copy(s_now);
 		put_common(fieldname, new_timestamp);
 		super.put(fieldname, v);
 		fixTime(this, s_now);
 	}
 	
 	@Override
 	public void put(String fieldname, double v) {
 		int[] new_timestamp = VectorTimestamp.Copy(s_now);
 		put_common(fieldname, new_timestamp);
 		super.put(fieldname,  v);
 		fixTime(this, s_now);
 	}
 	
 	@Override
 	public void put(String fieldname, float v) {
 		int[] new_timestamp = VectorTimestamp.Copy(s_now);
 		put_common(fieldname, new_timestamp);
 		super.put(fieldname,  v);
 		fixTime(this, s_now);
 	}
 	
 	@Override
 	public void put(String fieldname, int v) {
 		int[] new_timestamp = VectorTimestamp.Copy(s_now);
 		put_common(fieldname, new_timestamp);
 		super.put(fieldname,  v);
 		fixTime(this, s_now);
 	}
 	
 	@Override
 	public JsonNode put(String fieldname, JsonNode v) {
 		int[] new_timestamp = VectorTimestamp.Copy(s_now);
 		put_common(fieldname, new_timestamp);
 		super.put(fieldname,  v);
 		fixTime(this, s_now);
 		return null;
 	}
 	
 	@Override
 	public void put(String fieldname, long v) {
 		int[] new_timestamp = VectorTimestamp.Copy(s_now);
 		put_common(fieldname, new_timestamp);
 		super.put(fieldname,  v);
 		fixTime(this, s_now);
 	}
 	
 	@Override
 	public void put(String fieldname, String v) {
 		int[] new_timestamp = VectorTimestamp.Copy(s_now);
 		put_common(fieldname, new_timestamp);
 		super.put(fieldname,  v);
 		fixTime(this, s_now);
 	}
 	
 	public void put(String fieldname, ShMemObject v) {
 		v.parent = this;
 		v.parent_key = fieldname;
 		
 		int[] new_timestamp = VectorTimestamp.Copy(s_now);
 		put_common(fieldname, new_timestamp);
 		super.put(fieldname,  v);
 		fixTime(this, s_now);
 	}
 	
 	@Override
 	public JsonNode remove(String fieldname) {
 		int[] new_timestamp = VectorTimestamp.Copy(s_now);
 		put_common(fieldname, new_timestamp);
 		JsonNode ret = super.remove(fieldname);
 		fixTime(this, s_now);
 		return ret;
 	}
 	
 	public void InsertAt(String fieldname, JsonNode node, int[] time) {
 		VectorTimestamp.Union(s_now,  time);
 		put_common(fieldname, time);
 		super.put(fieldname,  node);
 		fixTime(this, time);
 	}
 	
 	private static ShMemObject DeserializeObjectNode(ObjectNode obj) {
 		ShMemObject ret = new ShMemObject();
 		Iterator<Map.Entry<String,JsonNode>> fields = obj.getFields();
 		
 		// Every field is "new" so we have to create new objects to keep
 		// track of timestamp information. 
 		while (fields.hasNext()) {
 			Map.Entry<String, JsonNode> cur = fields.next();
 			String cur_key = cur.getKey();
 			
 			// Serialized objects will always have a timestamp and value
 			// wrapped into a larger ObjectNode
 			JsonNode wrapped_value = cur.getValue();
 			JsonNode real_value = wrapped_value.get("value");
 			ArrayNode serialized_timestamp = 
 					(ArrayNode)wrapped_value.get("shmem_timestamp");
 			
 			// Create a timestamp object for the new node and a list node
 			// to keep track of it. 
 			// XXX: Does the serialization preserve the order in which
 			int[] new_timestamp = 
 					VectorTimestamp.CopySerialized(serialized_timestamp);
 			ListNode new_list_node = ret.new ListNode(cur_key, new_timestamp);
 			ret.m_key_map.put(cur_key,  new_list_node);
 			ret.m_sorted_keys.InsertLast(new_list_node);
 			
 			if (real_value.isObject()) {
 				ShMemObject to_insert = DeserializeObjectNode((ObjectNode)real_value);
 				ret.InsertAt(cur_key, 
 							 to_insert, 
 							 new_timestamp);
 				to_insert.parent = ret;
 				to_insert.parent_key = cur_key;
 				
 			}
 			else {
 				ret.InsertAt(cur_key, real_value, new_timestamp);
 			}
 		}
 		return ret;
 	}
 	
 	public class MergeException extends Exception {
 		
 		private String m_message;
 		
 		public MergeException(String value) {
 			m_message = value;
 		}
 		
 		@Override
 		public String toString() {
 			return m_message;
 		}
 	}
 	
 	public void merge(JsonNode release) throws MergeException {
 		Iterator<Map.Entry<String,JsonNode>> fields = release.getFields();
 		
 		while (fields.hasNext()) {
 			Map.Entry<String, JsonNode> cur = fields.next();
 			String key = cur.getKey();
 			JsonNode wrapped_value = cur.getValue();
 			ArrayNode other_timestamp = (ArrayNode)wrapped_value.get("shmem_timestamp");
 			JsonNode other_value = wrapped_value.get("value");
 			
 			ListNode my_list_node = m_key_map.get(key);
 			
 			if (my_list_node != null) {
 				
 				int[] my_timestamp = my_list_node.m_timestamp;
 				
 				// We need to compare timestamps, so use ObjectNode's get. 
 				JsonNode my_value = super.get(key);
 				
 				Comparison comp = 
 						VectorTimestamp.CompareWithSerializedTS(my_timestamp,  
 																other_timestamp);
 				
 				// If either one of these guys is a leaf, then this is the time that
 				// we have to check for conflicts. 
 				if (!(my_value instanceof ShMemObject) || (!other_value.isObject())) {
 					if (comp == Comparison.NONE) {
 						throw new MergeException("Merge exception!");
 					}
 					if (comp == Comparison.LT) {
 						int[] new_timestamp = VectorTimestamp.CopySerialized(other_timestamp);
 						if (other_value.isObject()) {
 							ShMemObject deserialized_value = DeserializeObjectNode((ObjectNode)other_value);
 							this.InsertAt(key, deserialized_value, new_timestamp);
 							deserialized_value.parent = this;
 							deserialized_value.parent_key = key;
 						}
 						else {
 							this.InsertAt(key, other_value, new_timestamp);
 						}
 					}
 				}
 				else { 	// Neither of them is a leaf node. 
 					if (comp != Comparison.GT) {
 						((ShMemObject)my_value).merge(other_value);
 					}
 				}
 			}
 			else {
 				// We need to create  new timestamp because it doesn't yet 
 				// exist. 
 				int[] new_timestamp = 
 						VectorTimestamp.CopySerialized(other_timestamp);
 				
 				if (other_value.isObject()) {
 					ShMemObject deserialized_value = DeserializeObjectNode((ObjectNode)other_value);
 					this.InsertAt(key, deserialized_value, new_timestamp);
 					deserialized_value.parent = this;
 					deserialized_value.parent_key = key;
 				}
 				else {
 					this.InsertAt(key, other_value, new_timestamp);
 				}
 			}
 		}
 	}
 }
