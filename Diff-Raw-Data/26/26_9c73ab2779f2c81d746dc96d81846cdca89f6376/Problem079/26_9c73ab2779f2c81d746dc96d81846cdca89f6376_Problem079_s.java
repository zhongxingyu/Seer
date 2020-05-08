 package euler.level2;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import euler.Pair;
 import euler.Problem;
 
 public class Problem079 extends Problem<Number> {
 	@Override
 	public Number solve() {
 		Byte[] bytes = new Byte[256];
 		for (char c = '0'; c <= '9'; c++) {
 			bytes[c] = Byte.valueOf((byte) (c - '0'));
 		}
 		
 		Set<Pair<Byte, Byte>> pairs = new HashSet<Pair<Byte, Byte>>();
 
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader("Problem79.txt"));
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				final int max = line.length();
 				for (int fIx = 0; fIx < max; fIx++) {
 					Byte first = bytes[line.charAt(fIx)];
 					if(first != null) {
 						for(int sIx = fIx + 1; sIx < max; sIx++) {
 							Byte second = bytes[line.charAt(sIx)];
 							if(second != null) {
 								pairs.add(Pair.from(first, second));
 							}
 						}
 					}
 				}
 			}
 		} catch (IOException ex) {
 			ex.printStackTrace();
 			return null;
 		}
 		
 		// Now we have all the pairs loaded
		System.out.println(pairs);
 		
 		Map<Byte, Pair<Set<Byte>, Set<Byte>>> splits = new HashMap<Byte, Pair<Set<Byte>,Set<Byte>>>();
 		for(byte center = 0; center <= 9; center++) {
 			Pair<Set<Byte>, Set<Byte>> split = new Pair<Set<Byte>, Set<Byte>>(new HashSet<Byte>(), new HashSet<Byte>());
 			for(Pair<Byte,Byte> pair: pairs) {
 				if(center == pair.getFirst()) {
 					split.getSecond().add(pair.getSecond());
 				}
 				if(center == pair.getSecond()) {
 					split.getFirst().add(pair.getFirst());
 				}
 			}
 			if(!split.getFirst().isEmpty() || !split.getSecond().isEmpty()) {
 				splits.put(center, split);
 			}
 		}
 		
 		System.out.println(splits);
 
 		return null;
 	}
 }
