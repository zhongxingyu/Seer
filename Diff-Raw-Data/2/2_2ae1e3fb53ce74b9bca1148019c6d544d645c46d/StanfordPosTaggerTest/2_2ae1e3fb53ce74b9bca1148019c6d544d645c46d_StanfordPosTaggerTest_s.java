 package ax.makila.comparableentititymining.test;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 import ax.makila.comparableentititymining.postagger.CompTaggedWord;
 import ax.makila.comparableentititymining.postagger.StanfordPosTagger;
 import edu.stanford.nlp.ling.TaggedWord;
 
 public class StanfordPosTaggerTest {
 
 	@Test
 	public void testTagString() {
 		String testString1 = "This is a test.";
 		TaggedWord t0 = new TaggedWord("This");
 		TaggedWord t1 = new TaggedWord("is");
 		TaggedWord t2 = new TaggedWord("a");
 		TaggedWord t3 = new TaggedWord("test");
 		TaggedWord t4 = new TaggedWord(".");
 		TaggedWord[] test1 = {t0, t1, t2, t3, t4};
 		
 		
 		String testString2 = "This is another test. Cool, huh?";
 		TaggedWord t5 = new TaggedWord("This");
 		TaggedWord t6 = new TaggedWord("is");
 		TaggedWord t7 = new TaggedWord("another");
 		TaggedWord t8 = new TaggedWord("test");
 		TaggedWord t9 = new TaggedWord(".");
 		TaggedWord t10 = new TaggedWord("Cool");
 		TaggedWord t11 = new TaggedWord(",");
 		TaggedWord t12 = new TaggedWord("huh");
 		TaggedWord t13 = new TaggedWord("?");
 		TaggedWord[] test2 = {t5, t6, t7, t8, t9};
 		TaggedWord[] test3 = {t10, t11, t12, t13};
 		
 		List<ArrayList<TaggedWord>> taggedString = StanfordPosTagger.tagString(testString1);
 		assertTrue(taggedString.size() == 1);
 		
 		List<ArrayList<TaggedWord>> anotherTagged = StanfordPosTagger.tagString(testString2);
 		assertTrue(anotherTagged.size() == 2);
 		
 		ArrayList<TaggedWord> tagged1 = taggedString.get(0);
 		for(int i = 0; i < tagged1.size(); i++) {
 			assertEquals(test1[i].value(), tagged1.get(i).value());
 		}
 		
 		ArrayList<TaggedWord> tagged2 = anotherTagged.get(0);
 		for(int i = 0; i < tagged2.size(); i++) {
 			assertEquals(test2[i].value(), tagged2.get(i).value());
 		}
 		
 		ArrayList<TaggedWord> tagged3 = anotherTagged.get(1);
 		for(int i = 0; i < tagged3.size(); i++) {
 			assertEquals(test3[i].value(), tagged3.get(i).value());
 		}
 		
 	}
 
 	@Test
 	public void testTagStringHandleIdentifier() {
 		String testString = "Hej $c what are you doing?";
 		String[] testArr = {"#start", "Hej", "$c", "what", "are", "you", "doing", "?", "#end"};
 		
 		List<List<CompTaggedWord>> test = StanfordPosTagger.tagStringHandleIdentifier(testString);
 		List<CompTaggedWord> innerList = new ArrayList<CompTaggedWord>(test.get(0));
 		for(int i = 1; i < test.size(); i++) {
 			innerList.addAll(test.get(i));
 		}
 		
 		assertTrue(innerList.size() == testArr.length);
 		
 		for(int i = 0; i < innerList.size(); i++) {
 			CompTaggedWord token = innerList.get(i);
 			assertEquals(token.value(), testArr[i]);
 			if(token.value().equals("#start") || token.value().equals("#end")) {
 				assertEquals(token.tag(), "#");
 			}
 			else if (token.value().equals("$c")) {
 				assertEquals(token.getCompTag(), "$c");
 			}
 			else {
 				assertEquals(token.value(), testArr[i]);
 			}
 		}
 		
 		String testString2 = "#start Hej $c what are you doing? #end";
 		String[] testArr2 = {"#start", "Hej", "$c", "what", "are", "you", "doing", "?", "#end"};
 		
 		List<List<CompTaggedWord>> test2 = StanfordPosTagger.tagStringHandleIdentifier(testString2);
 		List<CompTaggedWord> innerList2 = new ArrayList<CompTaggedWord>(test2.get(0));
 		for(int i = 1; i < test2.size(); i++) {
 			innerList.addAll(test2.get(i));
 		}
 		
 		assertTrue(innerList2.size() == testArr2.length);
 		
 		for(int i = 0; i < innerList2.size(); i++) {
 			CompTaggedWord token = innerList2.get(i);
 			assertEquals(token.value(), testArr2[i]);
 			if(token.value().equals("#start") || token.value().equals("#end")) {
 				assertEquals(token.tag(), "#");
 			}
 			else if (token.value().equals("$c")) {
 				assertEquals(token.getCompTag(), "$c");
 			}
 			else {
 				assertEquals(token.value(), testArr2[i]);
 			}
 		}
 		
 	}
 
 	@Test
 	public void testTokenizeString() {
 		String testString = "Hey $c what are you doing?";
 		String[] testArr = {"Hey", "$", "c", "what", "are", "you", "doing", "?"};
 		List<List<String>> list = StanfordPosTagger.tokenizeString(testString);
 		assertTrue(list.size() == 1);
 		
 		List<String> l = list.get(0);
 		assertEquals(l, Arrays.asList(testArr));
 		
 	}
 
 	@Test
 	public void testTokenizeStringMergeComp() {
 		String testString = "Hey $c what are you doing?";
		String[] testArr = {"Hey", "$c", "what", "are", "you", "doing", "?"};
 		
 		List<List<String>> list = StanfordPosTagger.tokenizeStringMergeComp(testString);
 		assertTrue(list.size() == 1);
 		
 		List<String> l = list.get(0);
 		assertEquals(l, Arrays.asList(testArr));
 	}
 
 }
