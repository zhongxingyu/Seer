 package uk.ac.ed.inf.proj.xmlnormaliser.validator;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.apache.log4j.BasicConfigurator;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import uk.ac.ed.inf.proj.xmlnormaliser.Utils;
 import uk.ac.ed.inf.proj.xmlnormaliser.parser.dtd.DTD;
 import uk.ac.ed.inf.proj.xmlnormaliser.parser.dtd.DTDParser;
 import uk.ac.ed.inf.proj.xmlnormaliser.parser.fd.FDParser;
 import uk.ac.ed.inf.proj.xmlnormaliser.parser.fd.FDPath;
 
 /**
  * Unit tests of creating new element types
  * 
  * @author Tomas Tauber
  * 
  */
 public class XNFTransformationTestCourses {
 
 	/* the parsed objects */
 	private static Map<FDPath, FDPath> originalFds;
 	private static DTD parsedDTD;
 
 	/* the test files */
 	private static final File TEST_FILE_FD = new File("tests-resources",
 			"courses.txt");
 	private static final File TEST_FILE_DTD = new File("tests-resources",
 			"courses.dtd");
 
 	/**
 	 * Reads the file and loads it into the parser
 	 * 
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		BasicConfigurator.configure();
 		originalFds = FDParser.parse(Utils.readFile(TEST_FILE_FD));
 		parsedDTD = DTDParser.parse(Utils.readFile(TEST_FILE_DTD));
 	}	
 	
 	@Test
 	public void testCreateNewET() {
 		List<TransformAction> actions = XNFTransformation.createNewET(0, "newET", new FDPath("courses", "courses.course.taken_by.student.@sno"), "courses.course.taken_by.student.name.@#PCDATA", originalFds, parsedDTD);
 		Assert.assertEquals(8, actions.size());
 		Assert.assertEquals(TransformAction.ActionType.ADD_NODE, actions.get(0).getType());
 		Assert.assertEquals("courses", (String) actions.get(0).getParameters()[0]);
 		Assert.assertEquals("newET0", (String) actions.get(0).getParameters()[1]);
 
 		Assert.assertEquals(TransformAction.ActionType.ADD_NODE, actions.get(1).getType());
 		Assert.assertEquals("newET0", (String) actions.get(1).getParameters()[0]);
 		Assert.assertEquals("newET00", (String) actions.get(1).getParameters()[1]);		
 
 		Assert.assertEquals(TransformAction.ActionType.DELETE_NODE, actions.get(2).getType());
 		Assert.assertEquals("student", (String) actions.get(2).getParameters()[0]);
 		Assert.assertEquals("name", (String) actions.get(2).getParameters()[1]);		
 
 		Assert.assertEquals(TransformAction.ActionType.ADD_NODE, actions.get(3).getType());
 		Assert.assertEquals("newET0", (String) actions.get(3).getParameters()[0]);
 		Assert.assertEquals("name", (String) actions.get(3).getParameters()[1]);		
 		
 		Assert.assertEquals(TransformAction.ActionType.ADD_ATTRIBUTE, actions.get(4).getType());
 		Assert.assertEquals("newET00", (String) actions.get(4).getParameters()[0]);
		Assert.assertEquals("sno", (String) actions.get(4).getParameters()[1]);
 		
 		Assert.assertEquals(TransformAction.ActionType.DELETE_XFD, actions.get(5).getType());
 		Assert.assertEquals(new FDPath("courses", "courses.course.taken_by.student.@sno"), (FDPath) actions.get(5).getParameters()[0]);
 		Assert.assertEquals(new FDPath("courses.course.taken_by.student.name.@#PCDATA"), (FDPath) actions.get(5).getParameters()[1]);
 		
 		Assert.assertEquals(TransformAction.ActionType.ADD_XFD, actions.get(6).getType());
 		Assert.assertEquals(new FDPath("courses.newET0", "courses.newET0.newET00.@sno"), (FDPath) actions.get(6).getParameters()[0]);
 		Assert.assertEquals(new FDPath("courses.newET0.newET00"), (FDPath) actions.get(6).getParameters()[1]);
 		
 		Assert.assertEquals(TransformAction.ActionType.ADD_XFD, actions.get(7).getType());
 		Assert.assertEquals(new FDPath("courses", "courses.newET0.newET00.@sno"), (FDPath) actions.get(7).getParameters()[0]);
 		Assert.assertEquals(new FDPath("courses.newET0"), (FDPath) actions.get(7).getParameters()[1]);		
 		
 	}
 	
 }
