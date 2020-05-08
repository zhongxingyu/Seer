 package org.araqne.bloomfilter;
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class BloomFilterTest {
 	private BloomFilter<String> filter = null;
 	private Set<String> map = null;
 
 	// failed performance : 0 - 4, 5, 6, 7, 8, 2-3
 	// success : 0-1, 2, 3, 1-2, 3, 2-1, 3-1
 	@SuppressWarnings("unchecked")
 	public void init(int num, boolean doMap) {
		filter = new BloomFilter<String>(0.001, 10000, GeneralHashFunction.stringHashFunctions[2],
				GeneralHashFunction.stringHashFunctions[1]);
 		map = new HashSet<String>(num);
 
 		String in;
 		for (int i = 0; i < num / 2; i++) {
 			in = VMIDGen.next();
 			filter.add(in);
 			if (doMap)
 				map.add(in);
 		}
 		for (int i = 0; i < num / 2; i++) {
 			in = UIDGen.next();
 			filter.add(in);
 			if (doMap)
 				map.add(in);
 		}
 	}
 
 	@Test
 	public void contains() {
 		System.out.println(new Date());
 		init(50000, true);
 		for (String key : map) {
 			if (map.contains(key) == false)
 				fail();
 		}
 		System.out.println(new Date());
 	}
 
 	@Test
 	public void doesNotContain() {
 		System.out.println(new Date());
 		init(10000, false);
 		int count = 0;
 		for (int i = 0; i < 10000; i++) {
 			if (filter.contains(VMIDGen.next()))
 				count++;
 		}
 		for (int i = 0; i < 10000; i++) {
 			if (filter.contains(UIDGen.next()))
 				count++;
 		}
 
 		System.out.printf("false positive count: %d, rate : %f\n", count, count / 20000D);
 		assertTrue(count < 20);
 		System.out.println(new Date());
 	}
 
 	@Test
 	public void test6and7Encoding() throws IOException {
 		BloomFilter<String> filter = new BloomFilter<String>(100);
 		filter.add("test");
 
 		// jdk 7
 		ByteArrayOutputStream os1 = new ByteArrayOutputStream();
 		filter.save(os1);
 		byte[] b1 = os1.toByteArray();
 
 		// under jdk 6
 		ByteArrayOutputStream os2 = new ByteArrayOutputStream();
 		filter.save(os2, true);
 		byte[] b2 = os2.toByteArray();
 
 		assertTrue(Arrays.equals(b1, b2));
 
 		BloomFilter<String> nbf1 = new BloomFilter<String>(100);
 		nbf1.load(new ByteArrayInputStream(b1));
 
 		BloomFilter<String> nbf2 = new BloomFilter<String>(100);
 		nbf2.load(new ByteArrayInputStream(b1), true);

 		BloomFilter<String> nbf3 = new BloomFilter<String>(); // test config loading
 		nbf3.load(new ByteArrayInputStream(b1));
 
 		assertTrue(filter.contains("test"));
 		assertTrue(nbf1.contains("test"));
 		assertTrue(nbf2.contains("test"));
 		assertTrue(nbf3.contains("test"));
 		assertTrue(filter.getBitmap().equals(nbf1.getBitmap()));
 		assertTrue(filter.getBitmap().equals(nbf2.getBitmap()));
 		assertTrue(filter.getBitmap().equals(nbf3.getBitmap()));
 		assertTrue(filter.getHashFuncCount() == nbf3.getHashFuncCount());
 		assertTrue(nbf1.getBitmap().equals(nbf2.getBitmap()));
 	}
 
 	public abstract static class VMIDGen {
 		static String next() {
 			return new java.rmi.dgc.VMID().toString();
 		}
 	}
 
 	public abstract static class UIDGen {
 		static String next() {
 			return new java.rmi.server.UID().toString();
 		}
 	}
 
 	@Test
 	public void saveAndLoadBackwardCompatTest() throws IOException {
 		BloomFilter<String> f = new BloomFilter<String>(0.1, 30000);
 		for (int i = 0; i < 30000; ++i) {
 			f.add("token" + i);
 		}
 		// old ver save
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		DataOutputStream dos = new DataOutputStream(os);
 		dos.writeInt(f.getBitmap().length());
 
 		// try jdk 7 accelration first
 		try {
 			long[] words = f.getBitmap().toLongArray();
 			for (long word : words) {
 				dos.writeLong(word);
 			}
 
 			dos.close();
 			os.close();

 			byte[] oldVerOutput = os.toByteArray();
 
 			BloomFilter<String> nf = new BloomFilter<String>(0.1, 30000);
 			nf.load(new ByteArrayInputStream(oldVerOutput));
 			BloomFilter<String> nf2 = new BloomFilter<String>(0.1, 30000);
 			nf2.load(new ByteArrayInputStream(oldVerOutput), true);
 			assertTrue(f.getHashFuncCount() == nf.getHashFuncCount());
 			assertTrue(f.getBitmap().size() == nf.getBitmap().size());
 			assertTrue(f.getHashFuncCount() == nf2.getHashFuncCount());
 			assertTrue(f.getBitmap().size() == nf2.getBitmap().size());
 			assertTrue(nf.contains("token1423"));
 			assertTrue(nf.contains("token14230"));
 			assertTrue(nf2.contains("token1423"));
 			assertTrue(nf2.contains("token14230"));
		} catch (NoSuchMethodError e) {
 		}
 	}
 }
