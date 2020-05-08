 package net.oemig.scta.model.impl.jaxb;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 import net.oemig.scta.model.ITraceModel;
 import net.oemig.scta.model.data.ExperiementId;
 import net.oemig.scta.model.data.Millisecond;
 import net.oemig.scta.model.data.QuestionType;
 import net.oemig.scta.model.data.UserName;
 import net.oemig.scta.model.exception.NoCurrentRunSelectedException;
 import net.oemig.scta.model.exception.NoCurrentSessionSelectedException;
 import net.oemig.scta.model.exception.SessionAlreadyExistsException;
 import net.oemig.scta.model.exception.TraceFileNotFoundExeption;
 import net.oemig.scta.model.test.SctaModelTestConfig;
 
 public class JAXBTraceModelTest extends TestCase{
 	
 	private ITraceModel m;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		m=JAXBTraceModelImpl.builder().
 			traceName(SctaModelTestConfig.TRACE_NAME).
 			sessionName(SctaModelTestConfig.SESSION_NAME).
 			build();
 
 	}
 	
 	public void test()throws Exception{
 		
 		Assert.assertEquals("Unexpected trace name", SctaModelTestConfig.TRACE_NAME,m.getCurrentTrace().getName());
 		Assert.assertEquals("Unexpected session name", SctaModelTestConfig.SESSION_NAME,m.getCurrentSession().getName());
 		Assert.assertEquals("Unexpected number of sessions", 1,m.getCurrentTrace().getSessions().size());
 		Assert.assertEquals("Unexpected number of runs", 1,m.getCurrentSession().getRuns().size());
 	}
 	
 	public void testAddResponseData()throws Exception{
 		
 		m.addResponseData(UserName.JEFF, true, Millisecond.of(100), QuestionType.GroupHow);
 		
 		Assert.assertEquals("Unexpected number of response data", 1, m.getCurrentRun().getResponseData().size());
 		Assert.assertEquals("Unexpected participant", UserName.JEFF,m.getCurrentRun().getResponseData().get(0).getParticipantName());
 		Assert.assertEquals("Unexpected response time", Millisecond.of(100),m.getCurrentRun().getResponseData().get(0).getResponseTime());
 		Assert.assertEquals("Unexpected question type", QuestionType.GroupHow,m.getCurrentRun().getResponseData().get(0).getQuestionType());
 		Assert.assertEquals("Unexpected correctness", true,m.getCurrentRun().getResponseData().get(0).isCorrect());
 	}
 
 	public void testAddCountData()throws Exception{
 		
 		m.addCountData(UserName.JEFF, "s", 11);
 		
 		Assert.assertEquals("Unexpected number of count data", 1, m.getCurrentRun().getCountData().size());
 		Assert.assertEquals("Unexpected participant", UserName.JEFF, m.getCurrentRun().getCountData().get(0).getParticipant());
 		Assert.assertEquals("Unexpected letter","s", m.getCurrentRun().getCountData().get(0).getLetter());
 		Assert.assertEquals("Unexpected quantity", 11, m.getCurrentRun().getCountData().get(0).getQuantity());
 	}
 	
 	public void testAddParticipant()throws Exception{
 		m.addParticipant(UserName.JEFF, ExperiementId.TEST);
 		
 		Assert.assertEquals("Unexpected number of participants", 1,m.getCurrentRun().getParticipants().size());
 		Assert.assertEquals("Unexpected name", UserName.JEFF,m.getCurrentRun().getParticipants().get(0).getName());
 		Assert.assertEquals("Unexpected experiment id", ExperiementId.TEST,m.getCurrentRun().getParticipants().get(0).getExperimentId());
 		
 	}
 	
 
 	
 	public void testSave()throws Exception{
 		m.addParticipant(UserName.JEFF, ExperiementId.TEST);
 		m.addParticipant(UserName.TINA, ExperiementId.TEST);
 		m.addResponseData(UserName.JEFF, true, Millisecond.of(100), QuestionType.GroupHow);
 		m.addResponseData(UserName.TINA, false, Millisecond.of(300), QuestionType.GroupWho);
 		m.addCountData(UserName.JEFF, "s", 11);
 		m.addCountData(UserName.TINA, "z", 44);
 		
		m.save("c:\\Users\\christoph.oemig\\testrace.xml");
 	}
 	
 	public void testLoad()throws Exception{
 		ITraceModel lm=JAXBTraceModelImpl.builder().load(getClass().getClassLoader().getResourceAsStream("simpletrace.xml")).build();
 		try{
 			lm.getCurrentSession();
 			Assert.fail("Expected exception not thrown");
 		}catch(NoCurrentSessionSelectedException e){
 			
 			try{
 				lm.getCurrentRun();
 				Assert.fail("Expected exception not thrown");
 			}catch(NoCurrentRunSelectedException ee){
 				Assert.assertNotNull("Trace must not be null",lm.getCurrentTrace());
 				Assert.assertEquals("Unexpected number of sessions", 1,lm.getCurrentTrace().getSessions().size());
 				Assert.assertEquals("Unexpected number of runs",1, lm.getCurrentTrace().getSessions().get(0).getRuns().size());
 			}
 			
 		}
 		
 	}
 	
 	public void testLoadFails()throws Exception{
 		try{
 			JAXBTraceModelImpl.builder().load(getClass().getClassLoader().getResourceAsStream("fail")).build();
 			Assert.fail("Expected exception not thrown");
 		}catch(TraceFileNotFoundExeption e){
 			//ok
 		}
 	}
 	
 	public void testAddSession()throws Exception{
 		String secondSessionName="second";
 		m.newSession(secondSessionName);
 		
 		Assert.assertEquals("Unexpected number of sessions", 2,m.getCurrentTrace().getSessions().size());
 		Assert.assertEquals("Unexpected current session",secondSessionName, m.getCurrentSession().getName());
 	}
 	
 	public void testAddRun()throws Exception{
 		m.addCountData(UserName.JEFF, "d", 13);
 		
 		m.newRun(SctaModelTestConfig.SESSION_NAME);
 		
 		Assert.assertEquals("Unexpected number of runs", 2,m.getCurrentSession().getRuns().size());
 		Assert.assertEquals("Unexpected current run", 0,m.getCurrentRun().getCountData().size());
 	}
 	
 	public void testLoadAndAddDuplicateSession()throws Exception{
 		try{
 			JAXBTraceModelImpl.builder().
 			load(getClass().getClassLoader().getResourceAsStream("simpletrace.xml")).
 			newSession(SctaModelTestConfig.SESSION_NAME).
 			build();
 		
 			Assert.fail("Expected exception not thrown");
 		}catch(SessionAlreadyExistsException e){
 			//ok
 		}
 	}
 }
