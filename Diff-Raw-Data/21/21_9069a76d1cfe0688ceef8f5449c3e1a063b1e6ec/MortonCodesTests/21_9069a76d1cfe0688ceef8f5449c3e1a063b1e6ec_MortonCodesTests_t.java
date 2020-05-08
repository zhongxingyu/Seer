 package assignment2.tests;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import assignment2.MortonCodes;
 
 public class MortonCodesTests {
 
 	long hash = 		0b1000101000100;
 	
 	//the hashes of its parent and neighbors
 	long parent = 		0b1000101000;
 	long nbr_plus_x = 	0b1000101100000;
 	long nbr_plus_y =   0b1000101000110;
 	long nbr_plus_z =   0b1000101000101;
 	
 	long nbr_minus_x = 	0b1000101000000;
 	long nbr_minus_y =  -1; //invalid: the vertex lies on the boundary and an underflow should occur
 	long nbr_minus_z =  0b1000100001101;
 	
 	long vertexHash = 0b1000110100000;
 	@Test
 	public void testParent() {
 		assertEquals(parent, MortonCodes.parentCode(hash));
 		assertEquals(0b1000101100, MortonCodes.parentCode(nbr_plus_x));
 		assertEquals(parent, MortonCodes.parentCode(nbr_plus_y));
 		assertEquals(parent, MortonCodes.parentCode(nbr_plus_z));
 		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 0));
 		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 1));
 		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 2));
 		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 3));
 		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 4));
 		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 5));
 		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 6));
 		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 7));
 		assertFalse(parent == MortonCodes.parentCode((parent << 3) + 8)); 
 	}
 	
 	@Test
 	public void testCellonLevelXgrid() {
 		assertTrue(MortonCodes.isCellOnLevelXGrid(0b1000, 1));
 		assertTrue(MortonCodes.isCellOnLevelXGrid(0b1000000, 2));
 		assertTrue(MortonCodes.isCellOnLevelXGrid(0b1010, 1));
 		assertTrue(MortonCodes.isCellOnLevelXGrid(0b1100, 1));
 		
 		assertFalse(MortonCodes.isCellOnLevelXGrid(0b1000, 2));
 		assertFalse(MortonCodes.isCellOnLevelXGrid(0b1000000, 1));
 		assertFalse(MortonCodes.isCellOnLevelXGrid(0b1000000001, 1));
 		assertFalse(MortonCodes.isCellOnLevelXGrid(0b1000111, 1));
 	}
 	
 	@Test
 	public void testOverflow() {
 		assertTrue(MortonCodes.isOverflown(hash, 5));
 		assertFalse(MortonCodes.isOverflown(hash, 4));
 		assertTrue(MortonCodes.isOverflown(hash, 3));
 	
 		assertTrue(MortonCodes.isOverflown(parent, 4));
 		assertFalse(MortonCodes.isOverflown(parent, 3));
 		assertTrue(MortonCodes.isOverflown(parent, 2));
 
 	}
 	
 	@Test
 	public void testNeighborAddition() {
 		assertEquals(nbr_plus_x, MortonCodes.nbrCode(hash, 4, 0b100));
 		assertEquals(nbr_plus_y, MortonCodes.nbrCode(hash, 4, 0b010));
 		assertEquals(nbr_plus_z, MortonCodes.nbrCode(hash, 4, 0b001));
 	}
 	
 	@Test
 	public void testNeighborAdditionOverflow() {
 		assertEquals(-1L, MortonCodes.nbrCode(0b1100, 1, 0b100));
 		assertEquals(-1L, MortonCodes.nbrCode(0b1111, 1, 0b100));
 		assertEquals(-1L, MortonCodes.nbrCode(0b1001, 1, 0b001));
 	}
 	
 	@Test
 	public void testNeighborSubtraction() {
 		assertEquals(nbr_minus_x, MortonCodes.nbrCodeMinus(hash, 4, 0b100));
 		assertEquals(nbr_minus_y, MortonCodes.nbrCodeMinus(hash, 4, 0b010));
 		assertEquals(nbr_minus_z, MortonCodes.nbrCodeMinus(hash, 4, 0b001));
 		
 		assertEquals(hash, MortonCodes.nbrCodeMinus(nbr_plus_x, 4, 0b100));
 		assertEquals(hash, MortonCodes.nbrCodeMinus(nbr_plus_y, 4, 0b010));
 		assertEquals(hash, MortonCodes.nbrCodeMinus(nbr_plus_z, 4, 0b001));
 	}
 	
 	@Test
 	public void testNeighborSubtractionOverflow() {
 		assertEquals(-1L, MortonCodes.nbrCodeMinus(0b000000, 2, 0b100));
 		assertEquals(-1L, MortonCodes.nbrCodeMinus(0b000000, 2, 0b010));
 		assertEquals(-1L, MortonCodes.nbrCodeMinus(0b000000, 2, 0b001));
 	}
 	
 	@Test
	public void testVertexOnLevelXGrid() {
		assertTrue(MortonCodes.isVertexOnLevelXGrid(vertexHash, 4, 4));
		assertTrue(MortonCodes.isVertexOnLevelXGrid(vertexHash, 3, 4));
 		assertFalse(MortonCodes.isVertexOnLevelXGrid(vertexHash, 2, 4));
 		assertFalse(MortonCodes.isVertexOnLevelXGrid(vertexHash, 1, 4));
		
		//invalid input, but important according ms
		assertTrue(MortonCodes.isVertexOnLevelXGrid(vertexHash, 5, 4));

 	}
 	
 	private void assertEquals(long exp, long got) {
 		if (exp != got)
 			fail("expected: " + Long.toBinaryString(exp) + " but got: " + Long.toBinaryString(got));
 		else org.junit.Assert.assertEquals(exp, got); //fall back to default assertion for green tests!
 	}
 
 }
