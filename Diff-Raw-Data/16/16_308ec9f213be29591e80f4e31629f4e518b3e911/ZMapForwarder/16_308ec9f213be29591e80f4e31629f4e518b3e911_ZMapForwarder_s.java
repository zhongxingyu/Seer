 package search.exp.searchV32cc;
 
 import search.search32k.Hash;
 import search.search32k.TTEntry;
 
 
 public final class ZMapForwarder implements Hash{
 
 	private final ZMap2 m;
 	private final TTEntry temp = new TTEntry();
 	
 	public ZMapForwarder(final int size){
 		m = new ZMap2(size);
 	}
 	
 	@Override
 	public void put(long zkey, TTEntry t) {
 		m.put2(zkey, t.move, t.score, t.depth, t.cutoffType, t.seq);
 	}
 
 	@Override
 	public TTEntry get(long zkey) {
 		ZMap2.Entry e = m.get(zkey);
 		if(e != null){
 			temp.zkey = zkey;
 			temp.score = e.score;
 			temp.depth = (int)e.depth;
 			temp.move = e.encoding;
 			temp.cutoffType = e.cutoffType;
 			temp.seq = e.sequence;
 			return temp;
 		}
 		return null;
 	}
 
 }
