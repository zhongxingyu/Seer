 package com.crowdgame.model;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.crowdgame.model.TaskInput;
 
 
 
 public class TaskInputTest {
 
 	private TaskInput task = new TaskInput();
 	
 	private static final String problemType = "insertion1";
 	private static final String problemWord = "trust";
 	private static final String problemAnswer1 = "t";
 	private static final int problemNumAnswers = 4;
 	private static final int problemId = 7;
 	private static final int problemLevel = 1;
 	private static final String problemLanguage = "EN";
 	private static final String problemDisplay = "trus_";
 	private static final String taskContents = "{\"answers\":[\"t\",\"e\",\"y\",\"h\"],\"id\":7,\"word\":\"trust\",\"level\":1,\"type\":\"insertion1\",\"language\":\"EN\",\"display\":\"trus_\"}";
 	
 	@Before
 	public void setUp() {
 		task.setContents(taskContents);
 	}
 	
 	@Test
 	public void testDecodeType() {
 		String result = task.getType();
 		assertEquals(problemType, result);
 	}
 	
 	@Test
 	public void testDecodeWord() {
 		String result = task.getWord();
 		assertEquals(problemWord, result);
 	}
 	
 	@Test
 	public void testDecodeAnswers() {
 		List<String> result = task.getAnswers();
 		assertEquals(problemNumAnswers, result.size());
 		assert(result.contains(problemAnswer1));
 	}
 	
 	@Test
 	public void testDecodeId() {
		int result = task.getId();
 		assertEquals(problemId, result);
 	}
 	
 	@Test
 	public void testDecodeLevel() {
 		int result = task.getLevel();
 		assertEquals(problemLevel, result);
 	}
 	
 	@Test
 	public void testDecodeLanguage() {
 		String result = task.getLanguage();
 		assertEquals(problemLanguage, result);
 	}
 	
 	@Test
 	public void testDecodeDisplay() {
 		String result = task.getDisplayText();
 		assertEquals(problemDisplay, result);
 	}
 }
