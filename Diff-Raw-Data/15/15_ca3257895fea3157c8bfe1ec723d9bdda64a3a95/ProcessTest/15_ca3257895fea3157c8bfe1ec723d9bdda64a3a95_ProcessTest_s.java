 package cz.cvut.fit.mi_mpr_dip.admission.jbpm;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.drools.logger.KnowledgeRuntimeLogger;
 import org.drools.logger.KnowledgeRuntimeLoggerFactory;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import cz.cvut.fit.mi_mpr_dip.admission.BaseSpringJbpmTest;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.Admission;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.AdmissionState;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.Appeal;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.Evaluation;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.EvaluationType;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.address.Address;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.address.AddressType;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.address.City;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.address.Country;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.address.PrintLine;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.address.PrintLineType;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.personal.Document;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.personal.DocumentType;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.personal.Person;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.study.Degree;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.study.Language;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.study.Programme;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.study.StudyMode;
 import cz.cvut.fit.mi_mpr_dip.admission.jbpm.eval.MSPProcessEvaluator;
 import cz.cvut.fit.mi_mpr_dip.admission.service.AdminService;
 import cz.cvut.fit.mi_mpr_dip.admission.util.StringPool;
 
 /**
  * This is a sample file to launch a process.
  */
 public class ProcessTest extends BaseSpringJbpmTest {
 
 //	@Autowired
 //	private JbpmTaskService jbpmTaskService;
 
 //	private StatefulKnowledgeSession ksession;
 	
 	@Autowired
 	private ProcessService processService;
 	
 	@Autowired
 	private AdminService adminService;
 
 	private Admission admission;
 	private Map<String, String> applicationProperties;
 	private Map<String, Object> processParameters;
 	private final String processName = "2012_MSP_main";
 
 	@Override
 	@Before
 	public void setUp() {
 		admission = setTestAdmission();
 		//ksession = jbpmTaskService.getKnowledgeSession();
 		applicationProperties = adminService.getApplicationProperties();
 		
 		processParameters = new HashMap<String, Object>();
 		processParameters.put("admission", admission);
 		processParameters.put("evaluator", new MSPProcessEvaluator());
 		processParameters.put("prop", applicationProperties);
 	}
 
 	@Test
 	public void testValidAdmissionData() {
 		isAdmissionValid(admission);
 	}
 
 	@Test
 	public void testRunBlankProcess() {
		processService.runBlankProcess();
 	}
 	
 //	@Test
 //	public void testEmailProcess() {
 //		processService.runEmailProcess();
 //	}
 	
	@Test
	public void testMainProcess() {
		String admissionCode = "123-456-02";
		
		processService.runProcess(admissionCode);
	}
 
 //	@Test
 //	public void testHumanTaskProcess() {
 //		try {
 //			UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
 //			ut.begin();
 //
 //			/*
 //			 * Get the local task service
 //			 */
 //			TaskService taskService = jbpmTaskService.getTaskService();
 //
 //			
 //			
 //			ProcessInstance processInstance = ksession.startProcess("cz.cvut.fit.mi_mpr_dip.admission.test_user_task");
 //
 //			/*
 //			 * Retrive the tasks owned by a user
 //			 */
 //			List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("krisv", "en-UK");
 //			TaskSummary task = list.get(0);
 //
 //			System.out.println("krisv is executing task " + task.getName());
 //			taskService.start(task.getId(), "krisv");
 //			taskService.complete(task.getId(), "krisv", null);
 //
 //			ut.commit();
 //		} catch (Throwable t) {
 //			// TODO Auto-generated catch block
 //			t.printStackTrace();
 //		}
 //	}
 
 //	@Test
 //	public void testTestSubprocess() {
 //		Map<String, Object> parameters = new HashMap<String, Object>();
 //		parameters.put("admission", admission);
 //
 //		TestWorkItemHandler testHandler = new TestWorkItemHandler();
 //
 //		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", testHandler);
 //		ksession.startProcess("cz.cvut.fit.mi_mpr_dip.admission.2012_test", parameters);
 //
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //	}
 
 //	@Test
 //	public void testApologyApprovalSubprocess() {
 //		Map<String, Object> parameters = new HashMap<String, Object>();
 //		parameters.put("admission", admission);
 //
 //		TestWorkItemHandler testHandler = new TestWorkItemHandler();
 //
 //		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", testHandler);
 //		ksession.startProcess("cz.cvut.fit.mi_mpr_dip.admission.apology_approval", parameters);
 //
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //	}
 
 //	@Test
 //	public void testAdmissionTestSubprocess() {
 //		Map<String, Object> parameters = new HashMap<String, Object>();
 //		parameters.put("admission", admission);
 //		parameters.put("evaluator", new MSPProcessEvaluator());
 //
 //		TestWorkItemHandler testHandler = new TestWorkItemHandler();
 //
 //		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", testHandler);
 //		ksession.getWorkItemManager().registerWorkItemHandler("Email", testHandler);
 //		ksession.startProcess("cz.cvut.fit.mi_mpr_dip.admission.2012_admission_test", parameters);
 //
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //	}
 
 //	@Test
 //	public void testDecisionSubprocess() {
 //		Map<String, Object> parameters = new HashMap<String, Object>();
 //		parameters.put("admission", admission);
 //		parameters.put("evaluator", new MSPProcessEvaluator());
 //
 //		TestWorkItemHandler testHandler = new TestWorkItemHandler();
 //
 //		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", testHandler);
 //		ksession.getWorkItemManager().registerWorkItemHandler("Email", testHandler);
 //		ksession.startProcess("cz.cvut.fit.mi_mpr_dip.admission.2012_decision", parameters);
 //
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //	}
 
 //	@Test
 //	public void testDocumentRequestSubprocess() {
 //		Map<String, Object> parameters = new HashMap<String, Object>();
 //		parameters.put("admission", admission);
 //
 //		TestWorkItemHandler testHandler = new TestWorkItemHandler();
 //
 //		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", testHandler);
 //		ksession.getWorkItemManager().registerWorkItemHandler("Email", testHandler);
 //		ksession.startProcess("cz.cvut.fit.mi_mpr_dip.admission.document_request", parameters);
 //
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //	}
 
 //	@Test
 //	public void testRegisterToStudySubprocess() {
 //		Map<String, Object> parameters = new HashMap<String, Object>();
 //		parameters.put("admission", admission);
 //
 //		TestWorkItemHandler testHandler = new TestWorkItemHandler();
 //
 //		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", testHandler);
 //		ksession.startProcess("cz.cvut.fit.mi_mpr_dip.admission.register_to_study", parameters);
 //
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //	}
 	
 //	@Test
 //	public void testRegistrationSubprocess() {
 //		Map<String, Object> parameters = new HashMap<String, Object>();
 //		parameters.put("admission", admission);
 //		parameters.put("evaluator", new MSPProcessEvaluator());
 //
 //		TestWorkItemHandler testHandler = new TestWorkItemHandler();
 //
 //		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", testHandler);
 //		ksession.getWorkItemManager().registerWorkItemHandler("Email", testHandler);
 //		ksession.startProcess("cz.cvut.fit.mi_mpr_dip.admission.2012_registration", parameters);
 //
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //	}	
 	
 //	@Test
 //	public void testProcess() {
 //		try {
 //			KnowledgeRuntimeLogger logger = createLogger(ksession);
 //
 //			Map<String, Object> parameters = new HashMap<String, Object>();
 //			parameters.put("admission", admission);
 //			parameters.put("evaluator", new MSPProcessEvaluator());
 //
 //			TestWorkItemHandler testHandler = new TestWorkItemHandler();
 //
 //			ksession.getWorkItemManager().registerWorkItemHandler("Human Task", testHandler);
 //			ksession.getWorkItemManager().registerWorkItemHandler("Email", testHandler);
 ////			ProcessInstance processInstance = 
 //			ksession.startProcess("cz.cvut.fit.mi_mpr_dip.admission.2012_msp_main", parameters);
 //
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //			logger.close();
 //		} catch (Throwable t) {
 //			t.printStackTrace();
 //		}
 //	} 
 
 //	@Test
 //	public void testFireSignalEvent() {
 //		TestWorkItemHandler testHandler = new TestWorkItemHandler();
 //
 //		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", testHandler);
 //		ProcessInstance processInstance = ksession.startProcess("cz.cvut.fit.mi_mpr_dip.admission.test.signal_event");
 //
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //		processInstance.signalEvent("backToUserAction", null);
 //		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
 //	}
 
 //	@Test
 //	public void testGatewayMethod() {
 //		Map<String, Object> parameters = new HashMap<String, Object>();
 //		parameters.put("evaluator", new TestGatewayEvaluator());
 //
 //		ksession.startProcess("cz.cvut.fit.mi_mpr_dip.admission.test.gateway_method", parameters);
 //	}
 
 //	@Test
 //	public void testProcessWithDataFromDB() {
 //		// TODO
 //	}
 
 	private Admission setTestAdmission() {
 		Admission a = new Admission();
 		a.setCode("73935282");
 		a.setType("P");
 		a.setAccepted(false);
 
 		// Appeal ap = new Appeal();
 		// AppealType apt = new AppealType();
 		// apt.setName("Odvolání k děkanovi");
 		//
 		// ap.setAppealType(apt);
 		// ap.setAccepted(false);
 		//
 		Set<Appeal> appeals = new HashSet<Appeal>();
 		// appeals.add(ap);
 		a.setAppeals(appeals);
 
 		AdmissionState state = new AdmissionState();
 		state.setCode("S05");
 		state.setName("TO_AT");
 
 		a.setAdmissionState(state);
 
 		Person p = new Person();
 		p.setFirstname("Frantisek");
 		p.setLastname("Vomáčka");
 		p.setBirthIdentificationNumber("8203151111");
 		p.setPhone("737999999");
 		p.setEmail("f.vomacka123@mail.cz");
 
 		City c = new City();
 		c.setName("Město v ČR");
 
 		Country co = new Country();
 		co.setName("Česká republika");
 
 		AddressType adt1 = new AddressType();
 		adt1.setName("main");
 
 		AddressType adt2 = new AddressType();
 		adt2.setName("contact");
 
 		Set<PrintLine> printLines = createPrintLines();
 
 		Address ad = new Address();
 		ad.setStreet("Ulice");
 		ad.setHouseNumber("37");
 		ad.setCity(c);
 		ad.setPostalCode("99988");
 		ad.setCountry(co);
 		ad.setAddressType(adt1);
 		ad.setPrintLines(printLines);
 
 		Address adc = new Address();
 		adc.setStreet("Ulice");
 		adc.setHouseNumber("37");
 		adc.setCity(c);
 		adc.setPostalCode("99988");
 		adc.setCountry(co);
 		adc.setAddressType(adt2);
 		adc.setPrintLines(printLines);
 
 		Set<Address> addresses = new HashSet<Address>();
 		addresses.add(ad);
 		addresses.add(adc);
 
 		p.setAddresses(addresses);
 		p.setCitizenship(co);
 		p.setCityOfBirth(c);
 		p.setCountryOfBirth(co);
 
 		Document d = new Document();
 		DocumentType dt = new DocumentType();
 		dt.setName("OP");
 		d.setNumber("111222333");
 		d.setDocumentType(dt);
 
 		Set<Document> documents = new HashSet<Document>();
 		documents.add(d);
 
 		p.setDocuments(documents);
 
 		a.setPerson(p);
 
 		Degree degree = new Degree();
 		degree.setName("B");
 
 		StudyMode studyMode = new StudyMode();
 		studyMode.setName("P");
 
 		Language language = new Language();
 		language.setName("cz");
 
 		Programme programme = new Programme();
 		programme.setDegree(degree);
 		programme.setStudyMode(studyMode);
 		programme.setLanguage(language);
 		programme.setName("BI");
 
 		a.setProgramme(programme);
 
 		EvaluationType h1 = new EvaluationType();
 		h1.setName("H1");
 		EvaluationType h3 = new EvaluationType();
 		h3.setName("H3");
 		EvaluationType h5 = new EvaluationType();
 		h5.setName("H5");
 		EvaluationType h7 = new EvaluationType();
 		h7.setName("H7");
 		EvaluationType h8 = new EvaluationType();
 		h8.setName("H8");
 
 		Evaluation e1 = new Evaluation();
 		e1.setEvaluationType(h1);
 		e1.setValue("Varianta A");
 
 		Evaluation e3 = new Evaluation();
 		e3.setEvaluationType(h3);
 		e3.setValue("1");
 
 		Evaluation e5 = new Evaluation();
 		e5.setEvaluationType(h5);
 		e5.setValue("Olympiáda z Matematiky 2011!");
 
 		Evaluation e7 = new Evaluation();
 		e7.setEvaluationType(h7);
 		e7.setValue("90");
 
 		Evaluation e8 = new Evaluation();
 		e8.setEvaluationType(h8);
 		e8.setValue("1.9");
 
 		Set<Evaluation> evaluations = new HashSet<Evaluation>();
 		evaluations.add(e1);
 		evaluations.add(e3);
 		// evaluations.add(e5);
 		// evaluations.add(e7);
 		// evaluations.add(e8);
 
 		a.setEvaluations(evaluations);
 
 		return a;
 	}
 
 	private Set<PrintLine> createPrintLines() {
 		String[][] printLines = { { "1. řádek", "František Vomáčka" }, { "2. řádek", "Ulice 37" },
 				{ "3. řádek", "99988 Město v ČR" }, { "4. řádek", StringPool.BLANK }, { "5. řádek", StringPool.BLANK } };
 
 		return createPrintLines(printLines);
 	}
 
 	private Set<PrintLine> createPrintLines(String[][] lines) {
 		Set<PrintLine> printLines = new HashSet<PrintLine>();
 		for (String[] line : lines) {
 			printLines.add(createPrintLine(line));
 		}
 		return printLines;
 	}
 
 	private PrintLine createPrintLine(String[] line) {
 		PrintLine printLine = new PrintLine();
 		printLine.setPrintLineType(new PrintLineType());
 		printLine.getPrintLineType().setName(line[0]);
 		printLine.setValue(line[1]);
 		return printLine;
 	}
 
 	private void isAdmissionValid(Admission admission) {
 		assertNotNull(admission);
 		assertNotNull(admission.getCode());
 		assertNotNull(admission.getAccepted());
 		isAdmissionStateValid(admission.getAdmissionState());
 
 		if (!admission.getEvaluations().isEmpty()) {
 			for (Evaluation evaluation : admission.getEvaluations()) {
 				isEvaluationValid(evaluation);
 			}
 		}
 		isPersonValid(admission.getPerson());
 		isProgrammeValid(admission.getProgramme());
 	}
 
 	private void isAdmissionStateValid(AdmissionState admissionState) {
 		assertNotNull(admissionState);
 		assertNotNull(admissionState.getCode());
 		assertNotNull(admissionState.getName());
 		// assertNotNull(admissionState.getDesciption());
 	}
 
 	private void isProgrammeValid(Programme programme) {
 		assertNotNull(programme);
 		assertNotNull(programme.getDegree());
 		assertNotNull(programme.getStudyMode());
 		assertNotNull(programme.getLanguage());
 	}
 
 	private void isPersonValid(Person person) {
 		assertNotNull(person);
 		assertNotNull(person.getFirstname());
 		assertNotNull(person.getLastname());
 		assertNotNull(person.getBirthIdentificationNumber());
 		assertNotNull(person.getEmail());
 		assertNotNull(person.getPhone());
 		assertNotNull(person.getCitizenship());
 		assertNotNull(person.getCountryOfBirth());
 		assertNotNull(person.getCityOfBirth());
 		assertNotNull(person.getAddresses());
 		assertTrue(!person.getAddresses().isEmpty());
 		if (!person.getAddresses().isEmpty()) {
 			for (Address address : person.getAddresses()) {
 				isAddressValid(address);
 			}
 		}
 		assertNotNull(person.getDocuments());
 		assertTrue(!person.getDocuments().isEmpty());
 		if (!person.getDocuments().isEmpty()) {
 			for (Document document : person.getDocuments()) {
 				isDocumentValid(document);
 			}
 		}
 	}
 
 	private void isDocumentValid(Document document) {
 		assertNotNull(document);
 		assertNotNull(document.getDocumentType());
 	}
 
 	private void isAddressValid(Address address) {
 		assertNotNull(address);
 		assertNotNull(address.getCity());
 		assertNotNull(address.getPostalCode());
 		assertNotNull(address.getCountry());
 		assertNotNull(address.getAddressType());
 		assertNotNull(address.getPrintLines());
 	}
 
 	private void isEvaluationValid(Evaluation evaluation) {
 		assertNotNull(evaluation.getValue());
 		assertNotNull(evaluation.getEvaluationType());
 		assertNotNull(evaluation.getEvaluationType().getName());
 		assertEquals(2, evaluation.getEvaluationType().getName().length());
 		assertEquals('H', evaluation.getEvaluationType().getName().charAt(0));
 	}
 
 	private KnowledgeRuntimeLogger createLogger(StatefulKnowledgeSession ksession) {
 		int logIntervalInMilliseconds = 500;
 		String logName = processName;
 		KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newThreadedFileLogger(ksession, logName,
 				logIntervalInMilliseconds);
 		return logger;
 	}
 }
