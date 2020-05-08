 package br.materdei.bdd.codegen;
 
 import static java.util.Arrays.asList;
 
 import java.util.List;
 
 import org.jbehave.core.configuration.Configuration;
 import org.jbehave.core.embedder.Embedder;
 import org.jbehave.core.junit.JUnitStory;
 import org.jbehave.core.steps.CandidateSteps;
 import org.jbehave.core.steps.InstanceStepsFactory;
 import org.junit.After;
 import org.junit.Before;
 
 import br.materdei.bdd.jbehave.JBehaveConfigurationUtil;
 import br.materdei.bdd.jbehave.SeleniumServerControllerSingleton;
 import br.materdei.bdd.jbehave.config.BddConfigPropertiesEnum;
 import br.materdei.bdd.steps.WebSteps;
 
 public class StoryBase extends JUnitStory {
 
 	public StoryBase() {
 		super();
 	}
 	
 	public void run(String storyName) throws Throwable {
 		Embedder embedder = configuredEmbedder();
         try {
             embedder.runStoriesAsPaths(asList(storyName));
         } finally {
             embedder.generateCrossReference();
         }
 	}
 	
 	@Override
 	public List<CandidateSteps> candidateSteps() {
 		return new InstanceStepsFactory(configuration(), new WebSteps()).createCandidateSteps();
 	}
 	
 	@Override
 	public Configuration configuration() {
 		return JBehaveConfigurationUtil.createSeleniumConfiguration(this.getClass());
 	}
 	
 	@Before
 	public void beforeTest() {
 		String ignore = BddConfigPropertiesEnum.IGNORE_SELENIUM_START_UP.getValue();
		if ((ignore == null) && ("false".equalsIgnoreCase(ignore))) {
 			SeleniumServerControllerSingleton controlador = SeleniumServerControllerSingleton.getInstancia();
 			controlador.iniciaServidorSelenium();
 			controlador.iniciaSelenium();
 		}
 	}
 
 	@After
 	public void afterTest() throws Exception { }
 }
