 package com.trifork.vmap;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.activation.DataSource;
 
 import com.google.protobuf.AbstractMessage;
 import com.google.protobuf.ByteString;
 import com.google.protobuf.ByteString.Output;
 import com.trifork.activation.IO;
 import com.trifork.vmap.VectorMap.VEntry;
 import com.trifork.vmap.protobuf.PB;
 import com.trifork.vmap.protobuf.PB.PBClock;
 import com.trifork.vmap.protobuf.PB.PBValue;
 import com.trifork.vmap.protobuf.PB.PBVectorMap;
 
 
public class PBEncoder {
 
 	public byte[] encode(VectorMap map) throws IOException {
 		AbstractMessage msg = toMessage(map);
 		return msg.toByteArray();		
 	}
 
 	public void write(VectorMap map, OutputStream out) throws IOException {
 		AbstractMessage msg = toMessage(map);
 		msg.writeTo(out);
 	}
 
 	public static PBVectorMap toMessage(VectorMap vmap) throws IOException {
 	
 		Map<String,VectorMap.VEntry> content = vmap.content;
 		
 		PBVectorMap.Builder builder = PBVectorMap.newBuilder();
 	
 		//
 		// Pre-emit, we need to reconstruct the constant pools
 		//
 		
 		Map<String, Integer> string_pool = new HashMap<String, Integer>();
 		Map<VClock, Integer> clock_pool = new HashMap<VClock, Integer>();
 	
 		for (Map.Entry<String, VectorMap.VEntry> ent : content.entrySet()) {
 			collectConstants(string_pool, clock_pool, ent.getKey(), ent
 					.getValue());
 		}
 		
 		// Build the string_pool element of the VectorMap
 		
 		String[] strings = new String[string_pool.size()];
 		for (Map.Entry<String, Integer> ent : string_pool.entrySet()) {
 			strings[ent.getValue()] = ent.getKey();
 		}
 		for (int i = 0; i < strings.length; i++) {
 			builder.addStringPool(strings[i]);
 		}
 	
 		// Build the clock_pool element of VectorMap
 		
 		VClock[] clocks = new VClock[clock_pool.size()];
 		for (Map.Entry<VClock, Integer> ent : clock_pool.entrySet()) {
 			clocks[ent.getValue()] = ent.getKey();
 		}
 		for (int i = 0; i < clocks.length; i++) {
 			
 			PBClock.Builder cb = PBClock.newBuilder();
 			VClock c = clocks[i];
 	
 			for (int p = 0; p < c.peers.length; p++) {
 				cb.addNode(string_pool.get(c.peers[p]));
 				cb.addCounter(c.counters[p]);
 				cb.addUtcMillis(c.utc_millis[p]);				
 			}
 			
 			builder.addClockPool(cb);
 		}
 		
 		// Emit Entries
 		
 		for (Map.Entry<String, VectorMap.VEntry> ent : content.entrySet()) {
 			String key = ent.getKey();
 			VectorMap.VEntry ve = ent.getValue();
 			
 			// encode the key
 			builder.addKeys(key);
 			
 			// encode the value						
 			PB.PBEntry.Builder eb = encodeEntry(string_pool, clock_pool, ve);
 			builder.addEntries(eb);
 		}
 	
 		return builder.build();
 	}
 
 	public static PB.PBEntry.Builder encodeEntry(Map<String, Integer> string_pool,
 			Map<VClock, Integer> clock_pool, VectorMap.VEntry ve) throws IOException {
 		PB.PBEntry.Builder eb = PB.PBEntry.newBuilder();
 		eb.setClock( clock_pool.get( ve.vClock )  );
 		for (DataSource o : ve.values) {
 			if (o == null) {
 				eb.addValues(PBValue.newBuilder().setDeleted(true));
 			} else {
 				Output oo = ByteString.newOutput();
 				IO.copystream(o.getInputStream(), oo);
 				
 				eb.addValues(PBValue.newBuilder()
 						.setMimeType(string_pool.get(o.getContentType()))
 						.setContent(oo.toByteString()));
 			}
 		}
 		return eb;
 	}
 
 
 	static void collectConstants(Map<String, Integer> stringPool,
 			Map<VClock, Integer> clockPool, String key, VEntry ent) {
 
 		collectString(stringPool, key);
 		
 		for (DataSource o : ent.values) {
 			collectString(stringPool, o.getContentType());
 		}
 
 		for (String peer : ent.vClock.peers) {
 			collectString(stringPool, peer);
 		}
 		
 		if (!clockPool.containsKey(ent.vClock)) {
 			clockPool.put(ent.vClock, clockPool.size());
 		}
 	}
 
 	private static void collectString(Map<String, Integer> stringPool, String string) {
 		if (!stringPool.containsKey(string)) {
 			stringPool.put(string, stringPool.size());
 		}
 	}
 }
