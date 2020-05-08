 package walsh_hadamard;
 
 import static org.junit.Assert.*;
 
 import java.util.BitSet;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class WalshHadamardTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@Test
 	public void testEncode5() {
 		BitSet input = BitSet.valueOf(new byte[] {5});
 		BitSet codeword = WalshHadamard.encode(input, 3);
 		BitSet expected = BitSet.valueOf(new byte[] {90});
 		assertEquals(expected, codeword);
 	}
 	
 	@Test
 	public void testEncode2() {
 		BitSet input = BitSet.valueOf(new byte[] {2});
 		BitSet codeword = WalshHadamard.encode(input, 2);
 		BitSet expected = BitSet.valueOf(new byte[] {3});
 		assertEquals(expected, codeword);
 	}
 	
 	
 	@Test
 	public void testEncode100() {
 		BitSet input = BitSet.valueOf(new byte[] {100});
 		BitSet codeword = WalshHadamard.encode(input, 7);
 		BitSet expected = BitSet.valueOf(new byte[] {15,15,15,15,
 													-16,-16,-16,-16,
 													-16,-16,-16,-16,
 													 15,15,15,15});
 		assertEquals(expected, codeword);
 	}
 
 	
 
 	@Test
 	public void testDecodeInverseEncode2() {
 		BitSet input = BitSet.valueOf(new byte[] {2});
 		BitSet actual = WalshHadamard.decode(WalshHadamard.encode(input, 2), 4);
 		assertEquals(input, actual);
 	}
 	
 	@Test
 	public void testDecodeInverseEncode100() {
 		BitSet input = BitSet.valueOf(new byte[] {100});
 		BitSet actual = WalshHadamard.decode(WalshHadamard.encode(input, 7), (int)Math.pow(2, 7));
 		assertEquals(input, actual);
 	}
 	
	/*@Test
	public void testRecoverBitAtPosition6() {	// to test on corrupted input...
		BitSet encoding = WalshHadamard.encode(BitSet.valueOf(new byte[] {6}),3);
 		BitSet results = new BitSet(8);
 		for (int i = 0; i<8; i++) {
 			if (WalshHadamard.recoverBitAtPosition(encoding, 3, i)) {
 				results.set(i);
 			}
 		}
 		assertEquals(encoding, results);
 		
	}*/
 	
 	@Test
 	public void testRecoverBitAtPosition2() {
 		BitSet encoding = WalshHadamard.encode(BitSet.valueOf(new byte[] {2}),2);
 		BitSet results = new BitSet(4);
 		for (int i = 0; i<4; i++) {
			System.out.println("i: "+i);
 			if (WalshHadamard.recoverBitAtPosition(encoding, 2, i)) {
 				results.set(i);
				System.out.println("yes");
 			}
 		}
 		assertEquals(encoding, results);
 		
 	}
 	
 	@Test
 	public void testBinlog() {
 		int actual = WalshHadamard.binlog(8);
 		int expected = 3;
 		assertEquals(expected, actual);
 		
 		actual = WalshHadamard.binlog(1024);
 		expected = 10;
 		assertEquals(expected, actual);
 		
 		actual = WalshHadamard.binlog(1);
 		expected = 0;
 		assertEquals(expected, actual);
 	}
 
 }
