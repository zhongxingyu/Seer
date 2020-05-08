 package grisu.frontend.tests;
 
 import static org.hamcrest.Matchers.isIn;
 import static org.junit.Assert.assertThat;
 import grisu.control.ServiceInterface;
 import grisu.frontend.tests.utils.TestConfig;
 import grisu.model.FileManager;
 import grisu.model.GrisuRegistryManager;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runners.Parameterized.Parameters;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 
 public class TestInfoSystem {
 
 	public static Logger myLogger = LoggerFactory
 			.getLogger(TestJobSubmission.class);
 
 	private static final TestConfig config = TestConfig.getTestConfig();
 
 	private static final Map<String, ServiceInterface> sis = config
 			.getServiceInterfaces();
 
 	@Parameters
 	public static Collection<Object[]> data() {
 		List<Object[]> result = Lists.newArrayList();
 
 		for (String backend : config.getServiceInterfaces().keySet()) {
 			result.add(new Object[] { backend,
 					config.getServiceInterfaces().get(backend) });
 		}
 
 		return result;
 	}
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	private final ServiceInterface si;
 	private final String backendname;
 	private final FileManager fm;
 
 	public TestInfoSystem(String backendname, ServiceInterface si) {
 		this.backendname = backendname;
 		this.si = si;
 		this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testAllVosExist() {
 
 		String[] fqans = si.getFqans().asArray();
 
 		assertThat("/none", isIn(fqans));
 		assertThat("/test/nesi", isIn(fqans));
 		assertThat("/test/demo", isIn(fqans));
 
 	}
 
 }
