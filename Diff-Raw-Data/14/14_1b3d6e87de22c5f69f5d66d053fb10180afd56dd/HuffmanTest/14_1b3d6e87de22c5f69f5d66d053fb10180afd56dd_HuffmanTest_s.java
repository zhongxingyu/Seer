 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 /**
  * @author Jared Moore
  * @version Apr 11, 2013
  */
 public class HuffmanTest {
 
 	private final String EASY = "ab", ONE_LETTER = "a", DOUBLE = "aabb",
 			SENTENCE = "The quick brown fox trips over the lazy dog.",
 			CAPITALS = "aAaAaAaAaA",
 			SENTENCE2 = "This is a test sentence for Huffman's algorithm.";
 	
 	@Test
 	public void testEasy() {
 		Node tree = Huffman.buildHuffmanTree(Huffman.buildFrequencyMap(EASY));
 		assertEquals(EASY, Huffman.decode(tree, Huffman.encode(Huffman.buildEncodingMap(tree), EASY)));
 	}
 	
 	@Test
 	public void testOneLetter() {
 		Node tree = Huffman.buildHuffmanTree(Huffman.buildFrequencyMap(ONE_LETTER));
		assertEquals(EASY, Huffman.decode(tree, Huffman.encode(Huffman.buildEncodingMap(tree), ONE_LETTER)));
 	}
 
 	@Test
 	public void testDouble() {
 		Node tree = Huffman.buildHuffmanTree(Huffman.buildFrequencyMap(DOUBLE));
		assertEquals(EASY, Huffman.decode(tree, Huffman.encode(Huffman.buildEncodingMap(tree), DOUBLE)));
 	}
 	
 	@Test
 	public void testSentence() {
 		Node tree = Huffman.buildHuffmanTree(Huffman.buildFrequencyMap(SENTENCE));
		assertEquals(EASY, Huffman.decode(tree, Huffman.encode(Huffman.buildEncodingMap(tree), SENTENCE)));
 	}
 	
 	@Test
 	public void testCapitals() {
 		Node tree = Huffman.buildHuffmanTree(Huffman.buildFrequencyMap(CAPITALS));
		assertEquals(EASY, Huffman.decode(tree, Huffman.encode(Huffman.buildEncodingMap(tree), CAPITALS)));
 	}
 	
 	@Test
 	public void testSentence2() {
 		Node tree = Huffman.buildHuffmanTree(Huffman.buildFrequencyMap(SENTENCE2));
		assertEquals(EASY, Huffman.decode(tree, Huffman.encode(Huffman.buildEncodingMap(tree), SENTENCE2)));
 	}
 }
