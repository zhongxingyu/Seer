 package org.gsoft.openserv.rules;
 
 import static org.junit.Assert.assertFalse;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.doAnswer;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.drools.KnowledgeBase;
 import org.drools.KnowledgeBaseFactory;
 import org.drools.builder.KnowledgeBuilder;
 import org.drools.builder.KnowledgeBuilderFactory;
 import org.drools.builder.ResourceType;
 import org.drools.io.ResourceFactory;
 import org.drools.logger.KnowledgeRuntimeLogger;
 import org.drools.logger.KnowledgeRuntimeLoggerFactory;
 import org.drools.runtime.StatelessKnowledgeSession;
 import org.gsoft.openserv.buslogic.amortization.AmortizationLogic;
 import org.gsoft.openserv.buslogic.system.SystemSettingsLogic;
 import org.gsoft.openserv.domain.loan.Loan;
 import org.gsoft.openserv.util.ApplicationContextLocator;
 import org.junit.Test;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.springframework.context.ApplicationContext;
 import org.springframework.util.ResourceUtils;
 
 public class GenerateAmortizationScheduleTest {
 	
 	@Test
 	public void test() throws IOException {
 		final Loan loan = new Loan();
 		loan.setLoanID(1L);
 		loan.setInitialUsedLoanTerm(180);
 
 		final AmortizationLogic amortizor = mock(AmortizationLogic.class);
 		doAnswer(new Answer<Object>(){
 			public Object answer(InvocationOnMock invocation){
 				System.out.println("AmortizationLogic.createAmortizationSchedule called.");
 				//just need to set something to stop the rule from firing
 				return null;
 			}
 		}).when(amortizor).createAmortizationSchedule(any(List.class), any(Date.class));
 		
 		SystemSettingsLogic sysSettingsLogic = mock(SystemSettingsLogic.class);
 		when(sysSettingsLogic.getCurrentSystemDate()).thenReturn(new Date());
 		
 		ApplicationContext springContext = mock(ApplicationContext.class);
 		when(springContext.getBean(AmortizationLogic.class)).thenAnswer(new Answer<AmortizationLogic>(){
 			public AmortizationLogic answer(InvocationOnMock invocation){
 				System.out.println("returning the Mocked AmortizationLogic class: " + amortizor);
 				return amortizor;
 			}
 		});
 		when(springContext.getBean(SystemSettingsLogic.class)).thenReturn(sysSettingsLogic);
 		
 		new ApplicationContextLocator().setApplicationContext(springContext);
 		
 		KnowledgeBuilder builder= KnowledgeBuilderFactory.newKnowledgeBuilder();
		File ruleFile = ResourceUtils.getFile("classpath:rules/GenerateAmortizationSchedule.drl");
 		builder.add(ResourceFactory.newFileResource(ruleFile), ResourceType.DRL);
 		if(builder.hasErrors())
 			System.out.println(builder.getErrors());
 		assertFalse("Errors loading DRL.", builder.hasErrors());
 		KnowledgeBase base = KnowledgeBaseFactory.newKnowledgeBase();
 		base.addKnowledgePackages(builder.getKnowledgePackages());
 		
 		StatelessKnowledgeSession session = base.newStatelessKnowledgeSession();
 		session.setGlobal("springContext", springContext);
 		KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newConsoleLogger(session);
 		session.execute(Arrays.asList(new Object[]{loan}));
 		logger.close();
 	}
 
 }
