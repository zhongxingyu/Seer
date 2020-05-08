 package org.hbird.parameterarchive.mongodb;
 
 import org.hbird.core.commons.tmtc.Parameter;
 import org.hbird.core.spacesystemmodel.parameters.HummingbirdParameter;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration
 public class SimpleMongoParameterRetrieverTest {
 
 	@Autowired
	private SimpleMongoParameterRetriever parameterRetriever;
 
 	private Parameter<Integer> testParameter;
 
 	@Before
 	public final void beforeAllTestsSetup() {
 		testParameter = new HummingbirdParameter<Integer>("Test.Parameter",
				 										  "testparam",
				 										  "Quick test int parameter",
				 										  "Integer parameter for mongo db testing");
 	}
 
 	@Test
 	public void test() {
 		parameterRetriever.findParamerers(testParameter.getQualifiedName());
 	}
 }
