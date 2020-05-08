 package vara.app.startupargs;
 
 import org.apache.log4j.Logger;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import vara.app.startupargs.exceptions.OptionNotFoundException;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertNotNull;
 
 /**
  * ArgsParser Tester.
  *
  * @author <Authors name>
  * @since <pre>08/26/2010</pre>
  * @version 1.0
  */
 public class ArgsParserTest extends FixtureUtil {
 
 	private static final Logger log = Logger.getLogger(ArgsParserTest.class);
 
 	@BeforeClass
 	public static void beforeClass() throws Exception {
 		FixtureUtil.beforeClass();
 	}
 
 //	public static void main(String[] args) throws Exception {
 //		beforeClass();
 //	}
 	/**
 	 *
 	 * Method: setCatchOnException(CatchOnException catcher)
 	 *
 	 */
 	public void testSetCatchOnException() throws Exception {}
 
 	/**
 	 *
 	 * Method: removeCatchOnException(CatchOnException catcher)
 	 *
 	 */
 	public void testRemoveCatchOnException() throws Exception {	}
 
 	/**
 	 *
 	 * Method: parseParameters(List<String> args)
 	 *
 	 */
 	public void testParseParametersArgs() throws Exception {}
 
 	/**
 	 *
 	 * Method: deliverCaughtException(final Exception exc)
 	 *
 	 */
 	public void testDeliverCaughtException() throws Exception {	}
 
 	/**
 	 *
 	 * Method: getSymbolIndexes(List<String> args)
 	 *
 	 */
 	@Test
 	public void testGetSymbolIndexes() throws Exception {
 
 		log.info("* testGetSymbolIndexes");
 
 		int [] vals = (int[])callPrivateMethod(ArgsParser.class,"getSymbolIndexes",
 							new Class[]{java.util.List.class},
 							new Object[]{Arrays.asList("--boolean","true","-h")});
 
 		assertNotNull(vals);
 		assertArrayEquals(new int []{0,2},vals);
 	}
 
 	/**
 	 * Combined argument should be created like one parameter in command line.
 	 * Combined argument don't have any arguments from the point of view parser.
 	 *
 	 * valid   : -symbol=param1,param2...
 	 * valid   : -symbol1 -symbol2=param1,param2... -symbol3
 	 *
 	 * invalid : -symbol= param1                             //param1 will be passed as argument
 	 * invalid : -symbol1 -symbol2=param1,param2... symbol3  //symbol3 will be passed as argument
 	 */
 	@Test(expected = OptionNotFoundException.class)
 	public void testWrongCombinedArgs(){
 
 		log.info("* testWrongCombinedArgs");
 		List<String> args = Arrays.asList("--boolean=", "param");
 
 		try{
 			//ArgsParser.setExceptionBehaviour(ArgsParser.ExceptionBehaviour.THROW);
 			ArgsParser.parseParameters(args);
 
 		}catch (OptionNotFoundException e){
 			log.info("Exception Thrown : "+e);
			boolean isExpectedException = e.getLocalizedMessage().contains("missing prefix");
 			log.info("isExpectedException : "+isExpectedException);
 			Assert.assertTrue(isExpectedException);
 
 			throw e;
 		}
 
 		Assert.fail("Fail !!! Expected exception !!!");
 	}
 
 	
 
 	/**
 	 *
 	 * Method: createParameterEntryHelpers(List<String> args)
 	 *
 	 */
 	public void testCreateParameterEntryHelpers() throws Exception {}
 
 }
