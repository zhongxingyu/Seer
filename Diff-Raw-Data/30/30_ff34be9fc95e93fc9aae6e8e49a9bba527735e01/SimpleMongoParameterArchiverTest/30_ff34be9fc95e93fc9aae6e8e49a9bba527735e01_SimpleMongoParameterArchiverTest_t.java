 package org.hbird.parameterarchive.mongodb;
 
 import org.hbird.core.commons.tmtc.Parameter;
 import org.hbird.core.spacesystemmodel.parameters.HummingbirdParameter;
 import org.hbird.parameterarchive.interfaces.ParameterArchiver;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration
 public class SimpleMongoParameterArchiverTest {
 
 	@Autowired
 	private ParameterArchiver parameterArchiver;
 
 	private Parameter<Integer> testParameter;
 
 	@Before
 	public final void beforeAllTestsSetup() {
 		testParameter = new HummingbirdParameter<Integer>("Test.Parameter",
				"testparam",
				"Quick test int parameter",
				"Integer parameter for mongo db testing");
 	}
 
 	@Test
	@Ignore // no current embedded mode for mongo db means it has to be running. Solutions are appearing...
 	public void test() {
 		parameterArchiver.archiveParameter(testParameter);
 	}
 }
