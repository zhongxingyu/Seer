 package com.thoughtworks.thoughtferret.test.unit;
 
 import java.util.Arrays;
 
 import junit.framework.TestCase;
 
import com.thoughtworks.thoughtferret.model.tags.MoodTagsBuilder;
 
 public class MoodTagsBuilderTests extends TestCase {
 	
 	public void testCanMergeForwardFromTheStart() {
 		MoodTagsBuilder presenter = new MoodTagsBuilder();
 		presenter.addKeywords(Arrays.asList("one", "two", "three", "four"));
 		presenter.merge("one", "two");
 		presenter.getKeywords().equals(Arrays.asList("one two", "three", "four"));
 	}
 
 	public void testCanMergeForwardInTheMiddle() {
 		MoodTagsBuilder presenter = new MoodTagsBuilder();
 		presenter.addKeywords(Arrays.asList("one", "two", "three", "four"));
 		presenter.merge("two", "three");
 		presenter.getKeywords().equals(Arrays.asList("one", "two three", "four"));
 	}
 
 	public void testCanMergeForwardAtTheEnd() {
 		MoodTagsBuilder presenter = new MoodTagsBuilder();
 		presenter.addKeywords(Arrays.asList("one", "two", "three", "four"));
 		presenter.merge("three", "four");
 		presenter.getKeywords().equals(Arrays.asList("one", "two", "three four"));
 	}
 	
 	public void testCanMergeBackwardsToTheStart() {
 		MoodTagsBuilder presenter = new MoodTagsBuilder();
 		presenter.addKeywords(Arrays.asList("one", "two", "three", "four"));
 		presenter.merge("two", "one");
 		presenter.getKeywords().equals(Arrays.asList("two one", "three", "four"));
 	}
 
 	public void testCanMergeBackwardsInTheMiddle() {
 		MoodTagsBuilder presenter = new MoodTagsBuilder();
 		presenter.addKeywords(Arrays.asList("one", "two", "three", "four"));
 		presenter.merge("three", "two");
 		presenter.getKeywords().equals(Arrays.asList("one", "three two", "four"));
 	}
 
 	public void testCanMergeBackwardsFromTheEnd() {
 		MoodTagsBuilder presenter = new MoodTagsBuilder();
 		presenter.addKeywords(Arrays.asList("one", "two", "three", "four"));
 		presenter.merge("four", "three");
 		presenter.getKeywords().equals(Arrays.asList("one", "two", "four three"));
 	}
 	
 }
