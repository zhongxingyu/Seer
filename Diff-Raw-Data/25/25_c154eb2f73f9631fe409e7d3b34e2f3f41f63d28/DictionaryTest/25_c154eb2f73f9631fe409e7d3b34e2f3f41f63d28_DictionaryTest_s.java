 package tof;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.junit.Test;
 
 public class DictionaryTest {
 
 	@Test
 	public void ShouldYieldEmptyListWhenNoAnagramFound() {
		Dictionary dictionary = new Dictionary();
 		assertEquals(new ArrayList<String>(), dictionary.anagrams("notaword"));
 	}
 	@Test
 	public void ShouldReturnAKnownAnagram() {
		Dictionary dictionary = new Dictionary();
 		assertEquals(Arrays.asList("cinema"), dictionary.anagrams("iceman"));
 	}
 }
