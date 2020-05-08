 package vara.app.startupargs;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
import vara.app.startupargs.exceptions.UnexpectedNumberOfArguments;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * User: Grzegorz (vara) Warywoda
  * Date: 2010-05-12
  * Time: 15:28:20
  */
 //@Ignore
 public class CmdLineArgumentsTest extends FixtureUtil{
 	private static final Logger log = Logger.getLogger(CmdLineArgumentsTest.class);
 
 	@BeforeClass
 	public static void beforeClass() throws Exception {
 		FixtureUtil.beforeClass();
 		Logger.getRootLogger().setLevel(Level.TRACE);
 
 		GStringValueParameter.define("gstring","gs");
 	}
 	
 	@Test
 	public void cmdlWithEqualsOrWithout(){
 
 		log.info("cmdLine With Equals Or Without");
 
 		List<String> args = Arrays.asList("-f=1002.2 -boolean=true -gs=test".split(" "));
 
 		ArgsParser.parseParameters(args);
 
 		GlobalParameter gp  = GlobalParameters.getGlobalParameter("gs");
 		System.out.println("gp class: "+gp.getClass());
 		Assert.assertNotNull(gp);
 		Assert.assertTrue(gp.isSet());
 
 		args = Arrays.asList("-f 10.2 --boolean false -gs test".split(" "));
 
 		ArgsParser.parseParameters(args);
 	}
 
 	//@Test
 	public void cmdlWrongNumOfInputParams(){
 		log.info("cmdlWrongNumOfInputParams");
 		List<String> args = Arrays.asList("-f 1002.2 --boolean -h".split(" "));
 
 		ArgsParser.parseParameters(args);
 	}

	@Test(expected = UnexpectedNumberOfArguments.class)
	public void cmdlEmptyCombinedArg(){
		List<String> args = Arrays.asList("-f=".split(" "));

		ArgsParser.parseParameters(args);

	}
 }
