 package com.bizosys.hsearch.byteutils;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.List;
 
 import junit.framework.TestCase;
 import junit.framework.TestFerrari;
 
 import com.oneline.ferrari.TestAll;
 
 public class SortedBytesBitsetCompressedTest extends TestCase {
 
 	public static String[] modes = new String[] { "all", "random", "method"};
 		public static String mode = modes[1];  
 		
 		public static void main(String[] args) throws Exception {
 			SortedBytesBitsetCompressedTest t = new SortedBytesBitsetCompressedTest();
 			
 			if ( modes[0].equals(mode) ) {
 				TestAll.run(new TestCase[]{t});
 			} else if  ( modes[1].equals(mode) ) {
 		        TestFerrari.testRandom(t);
 		        
 			} else if  ( modes[2].equals(mode) ) {
 				t.setUp();
 				t.testSanity();
 				t.tearDown();
 			}
 		}
 
 		@Override
 		protected void setUp() throws Exception {
 		}
 		
 		@Override
 		protected void tearDown() throws Exception {
 		}
 		
 		public void testSanity() throws Exception {	
 			List<BitSet> sortedList = new ArrayList<BitSet>();
 			int MAX = 10;
 			for ( int i=0; i<MAX; i++) {
				BitSet b = new BitSet(i * 10000000); //10Million increment
 				b.set(i);
 				sortedList.add(b);
 			}
 
 			ISortedByte<BitSet> ser = SortedBytesBitsetCompressed.getInstance();
 			byte[] bytes = ser.toBytes(sortedList);
 
 			System.out.println( "Compressed Bits :" + bytes.length);
 			
 			ISortedByte<BitSet> deser = SortedBytesBitsetCompressed.getInstance();
 			deser.parse(bytes);
 			
 			for ( int i=0; i<MAX; i++) {
 				BitSet b = deser.getValueAt(i);
 				System.out.println( b.size() + "\t" + b.get(i) + "\t" + b.get(i*100) + "\t" + b.cardinality());
 			}
 		}
 }
