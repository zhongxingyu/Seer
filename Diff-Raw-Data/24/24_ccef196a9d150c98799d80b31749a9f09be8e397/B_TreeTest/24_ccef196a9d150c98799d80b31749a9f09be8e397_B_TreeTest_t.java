 package suite.btree;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.io.IOException;
 import java.util.Comparator;
 import java.util.Random;
 
 import org.junit.Test;
 
 import suite.btree.Serializer.IntSerializer;
import suite.btree.Serializer.StringSerializer;
 import suite.util.FileUtil;
 import suite.util.Pair;
 
 public class B_TreeTest {
 
 	private static final int nKeys = 1024;
 	private Integer keys[] = new Integer[nKeys];
 
 	private B_Tree<Integer, String> b_tree;
 
 	private Random random = new Random();
 
 	private Comparator<Integer> comparator = new Comparator<Integer>() {
 		public int compare(Integer i0, Integer i1) {
 			return i0.compareTo(i1);
 		}
 	};
 
 	@Test
 	public void fileTest() throws IOException {
 		String pathName = FileUtil.tmp + "/test-btree";
 		boolean isNew = true;
 
 		try (B_TreeHolder<Integer, String> holder = new B_TreeHolder<>( //
 				pathName //
 				, isNew //
 				, comparator //
 				, new IntSerializer() //
 				, new StringSerializer(16))) {
 			b_tree = holder.get();
 			shuffleAndTest();
 		}
 	}
 
 	private void shuffleAndTest() {
 		for (int i = 0; i < nKeys; i++)
 			keys[i] = i;
 
 		addAndRemove();
 	}
 
 	private void shuffleNumbers() {
 		for (int i = 0; i < nKeys; i++) {
 			int j = random.nextInt(nKeys);
 			Integer temp = keys[i];
 			keys[i] = keys[j];
 			keys[j] = temp;
 		}
 	}
 
 	private void addAndRemove() {
 		shuffleNumbers();
 
 		for (int i = 0; i < nKeys; i++)
 			b_tree.put(keys[i], keys[i].toString());
 
 		for (int i = 0; i < nKeys; i++)
 			assertEquals(Integer.toString(i), b_tree.get(i));
 
 		int count = 0, half = nKeys / 2;
 
 		for (Pair<Integer, String> entry : b_tree.range(0, half)) {
 			Integer key = count++;
 			assertEquals(key, entry.t0);
 			assertEquals(Integer.toString(key), entry.t1);
 		}
 
 		for (Pair<Integer, String> entry : b_tree.range(half, nKeys)) {
 			Integer key = count++;
 			assertEquals(key, entry.t0);
 			assertEquals(Integer.toString(key), entry.t1);
 		}
 
 		shuffleNumbers();
 
 		for (int i = 0; i < nKeys; i += 2)
 			b_tree.remove(keys[i]);
 
 		b_tree.dump(System.out);
 
 		for (int i = 1; i < nKeys; i += 2)
 			b_tree.remove(keys[i]);
 
 		b_tree.dump(System.out);
 
 		for (Pair<Integer, String> entry : b_tree.range(0, nKeys))
 			assertNull(entry);
 	}
 
 }
