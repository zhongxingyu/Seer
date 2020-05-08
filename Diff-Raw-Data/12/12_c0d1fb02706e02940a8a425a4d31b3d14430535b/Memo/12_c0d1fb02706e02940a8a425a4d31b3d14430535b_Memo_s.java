 package amstin.models.grammar.parsing;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Memo implements IParser {
 
 	private IParser parser;
 
 	public Memo(IParser parser) {
 //		System.out.println("Memoizing: " + parser.getClass());
 		this.parser = parser;
 	}
 	
 
 	@Override
 	public void parse(Map<IParser, Map<Integer, Entry>> table, Cnt cnt, final String src, int pos) {
 		if (!table.containsKey(parser)) {
 			table.put(parser, new HashMap<Integer, Entry>());
 		}
 		if (!table.get(parser).containsKey(pos)) {
 			table.get(parser).put(pos, new Entry());
 		}
 		
 		final Entry entry = table.get(parser).get(pos);
 		
 		if (entry.cnts.isEmpty()) {
 			entry.cnts.add(cnt);
 			parser.parse(table, new MemoCnt(entry), src, pos);
 		}
 		else {
 			entry.cnts.add(cnt);
			int i = 0;
			for (int r: entry.results) {
 				cnt.apply(r, entry.objects.get(i));
				i++;
 			}
 		}
 	}
 
 
 
 	
 	private static class MemoCnt implements Cnt {
 
 		private final Entry entry;
 
 		public MemoCnt(Entry entry) {
 			this.entry = entry;
 		}
 		
 		@Override
 		public void apply(int result, Object tree) {
 			if (!entry.isSubsumed(result, tree)) {
 				entry.results.add(result);
 				entry.objects.add(tree);
 				for (Cnt c: entry.cnts) {
 					c.apply(result, tree);
 				}
 			}
 		}
 		
 	}
 	
 
 
 	
 }
