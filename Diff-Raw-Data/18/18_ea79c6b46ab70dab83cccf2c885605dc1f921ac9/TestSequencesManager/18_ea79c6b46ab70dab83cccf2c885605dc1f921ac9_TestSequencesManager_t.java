 package yucatan.sequence;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import yucatan.sequence.generated.XmlTypeCommand;
 
 /**
  * Test SequencesManager
  * 
  */
 public class TestSequencesManager {
 
 	/**
 	 * Test method for {@link yucatan.sequence.SequencesManager#getSequence(java.lang.String)}.
 	 */
 	@Test
 	public void testGetSequence() {
 		// null, null parameter check
 		Assert.assertEquals(SequencesManager.SEQUENCE_FILE_NOT_FOUND, SequencesManager.registerSequenceCollection(null, null));
 
 		// file not found
 		Assert.assertEquals(SequencesManager.SEQUENCE_FILE_NOT_FOUND, SequencesManager.registerSequenceCollection("notfound", "yucatan.sequences"));
 
 		// invalid file format
 		Assert.assertEquals(SequencesManager.SEQUENCE_FORMAT_ERROR, SequencesManager.registerSequenceCollection("invalidtag", "yucatan.sequences"));
 
 		// correct format
 		Assert.assertEquals(SequencesManager.SEQUENCE_FOUND, SequencesManager.registerSequenceCollection("category", "yucatan.sequences"));
 
 		// non declared attributes will be ignored
 		Assert.assertEquals(SequencesManager.SEQUENCE_FOUND, SequencesManager.registerSequenceCollection("ignoredinvalidattribute", "yucatan.sequences"));
 
 		// test number of registered commands
 		List<XmlTypeCommand> commandsDeclaration = SequencesManager.getSequence("category-show");
 		assertEquals(2, commandsDeclaration.size());
 	}
 
 	/**
 	 * Test method for {@link yucatan.sequence.SequencesManager#inspectSequence(java.lang.String)}.
 	 */
 	@Test
 	public void testInspectSequence() {
 
 		// prepare register test sequence collection
 		SequencesManager.registerSequenceCollection("category", "yucatan.sequences");
		List<String> sequenceItemNames = SequencesManager.inspectSequence("category-show");
 		String resultString = "";
 		for (String sequenceItemName : sequenceItemNames) {
 			resultString += sequenceItemName;
 			resultString += "+";
 		}
 		// check position of commands
 		Assert.assertEquals("yucatan.category.GetCategoryCommand+yucatan.category.ModifyCategoryCommand+", resultString);
 		Assert.assertEquals( null, SequencesManager.inspectSequence("non-existent") );
 	}
 
 }
