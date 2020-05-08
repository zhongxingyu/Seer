 package net.frontlinesms.plugins.learn;
 
 import net.frontlinesms.events.EventBus;
 import net.frontlinesms.plugins.BasePluginControllerTests;
import net.frontlinesms.plugins.PluginInitialisationException;
import net.frontlinesms.plugins.learn.data.repository.AssessmentDao;
 import net.frontlinesms.plugins.learn.data.repository.TopicDao;
 import net.frontlinesms.test.spring.MockBean;
 import net.frontlinesms.ui.UiGeneratorController;
 
 import static org.mockito.Mockito.*;
 
 public class LearnPluginControllerTest extends BasePluginControllerTests<LearnPluginController> {
 	@SuppressWarnings("unused")
 	@MockBean private EventBus eventBus;
 	@SuppressWarnings("unused")
 	@MockBean private TopicDao topicDao;
	@SuppressWarnings("unused")
	@MockBean private AssessmentDao assessmentDao;
	@SuppressWarnings("unused")
	@MockBean private LearnIncomingMessageProcessor learnIncomingMessageProcessor;
 	
 	public Class<LearnPluginController> getControllerClass() {
 		return LearnPluginController.class;
 	}
 	
	public void testTabAvailable() throws PluginInitialisationException {
		controller.init(null, ctx);
 		UiGeneratorController ui = mock(UiGeneratorController.class);
 		inject(controller, "ctx", ctx);
 		assertNotNull(controller.getTab(ui));
 	}
 }
 
