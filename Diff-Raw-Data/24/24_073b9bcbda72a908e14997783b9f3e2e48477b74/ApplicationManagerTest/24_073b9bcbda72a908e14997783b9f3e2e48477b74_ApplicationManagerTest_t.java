 package org.unbiquitous.uos.core.applicationManager;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 
 import java.io.File;
 import java.util.ListResourceBundle;
 import java.util.ResourceBundle;
 import java.util.TreeMap;
 
 import org.fest.assertions.data.MapEntry;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
 import org.unbiquitous.uos.core.applicationManager.ApplicationManager;
 import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
 import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;
 import org.unbiquitous.uos.core.ontology.OntologyReasonerTest;
 import org.unbiquitous.uos.core.ontologyEngine.api.OntologyStart;
 
 
 public class ApplicationManagerTest {
 
 	private ApplicationManager manager;
 	private Gateway gateway;
 
 	@Before public void setUp() throws Exception{
 		gateway = mock(Gateway.class);
 		new File("resources/owl/uoscontext.owl").createNewFile();
 		ResourceBundle bundle = new ListResourceBundle() {
 			protected Object[][] getContents() {
 				return new Object[][] {
 						{"ubiquitos.ontology.path","resources/owl/uoscontext.owl"},
 						{"ubiquitos.ontology.reasonerFactory",OntologyReasonerTest.class.getName()},
 				};
 			}
 		};
 		manager = new ApplicationManager(bundle,gateway);
 	}
 	
 	private void createOntologyDisabledManager() {
 		ResourceBundle bundle = new ListResourceBundle() {
 			protected Object[][] getContents() {
 				return new Object[][] {
 						{"ubiquitos.ontology.path","resources/owl/uoscontext.owl"},
 				};
 			}
 		};
 		manager = new ApplicationManager(bundle,gateway);
 	}
 	
 	@After public void tearDown(){
 		new File("resources/owl/uoscontext.owl").delete();
 	}
 	
 	@Test public void addingAnApplicationDoesNothingToIt(){
 		DummyApp app = new DummyApp();
 		manager.add(app);
 		assertThat(app.inited).isFalse();
 		assertThat(app.started).isFalse();
 		assertThat(app.stoped).isFalse();
 		assertThat(app.finished).isFalse();
 	}
 	
 	@Test public void startInitsAndStartsAllApplications() throws InterruptedException{
 		final DummyApp app1 = new DummyApp();
 		final DummyApp app2 = new DummyApp();
 		manager.add(app1);
 		manager.add(app2);
 		manager.startApplications();
 		waitToStart(app1);
 		waitToStart(app2);
 		assertThat(app1.inited).isTrue();
 		assertThat(app1.started).isTrue();
 		assertThat(app1.stoped).isFalse();
 		assertThat(app1.finished).isFalse();
 		assertThat(app2.inited).isTrue();
 		assertThat(app2.started).isTrue();
 		assertThat(app2.stoped).isFalse();
 		assertThat(app2.finished).isFalse();
 	}
 	
 	@Test public void startInitsAndStartsAllApplicationsOnlyOnce() throws InterruptedException{
 		final DummyApp app = new DummyApp();
 		manager.add(app);
 		manager.startApplications();
 		manager.startApplications();
 		waitToStart(app);
 		assertThat(app.initedCount).isEqualTo(1);
 		assertThat(app.startedCount).isEqualTo(1);
 	}
 	
 	@Test public void initsTheAppWithTheProperOntologyAndId(){
 		DummyApp app = new DummyApp();
 		manager.add(app);
 		manager.startApplications();
 		assertThat(app.initOntology).isNotNull();
 		assertThat(app.appId).isNotNull();
 	}
 	
 	@Test public void initsTheAppWithNoOntologyWhenDisabled(){
 		createOntologyDisabledManager();
 		DummyApp app = new DummyApp();
 		manager.add(app);
 		manager.startApplications();
 		assertThat(app.initOntology).isNull();
 	}
 
 	@Test public void startsTheAppWithTheProperOntologyAndGateway() throws InterruptedException{
 		final DummyApp app = new DummyApp();
 		manager.add(app);
 		manager.startApplications();
 		waitToStart(app);
 		assertThat(app.startOntology).isNotNull();
 		assertThat(app.gateway).isSameAs(gateway);
 	}
 	
 	@Test public void startsTheAppWithNoOntologyWhenDisableButStillWithGateway() throws InterruptedException{
 		createOntologyDisabledManager();
 		final DummyApp app = new DummyApp();
 		manager.add(app);
 		manager.startApplications();
 		waitToStart(app);
 		assertThat(app.startOntology).isNull();
 		assertThat(app.gateway).isSameAs(gateway);
 	}
 	
 	@Test public void startsTheAppInADifferentThread() throws InterruptedException{
 		final DummyApp app = new DummyApp(){
 			@Override
 			public void start(Gateway gateway, OntologyStart ontology) {
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {}
 				super.start(gateway, ontology);
 			}
 		};
 		manager.add(app);
 		manager.startApplications();
 		Thread.sleep(50);
 		assertThat(app.started).isFalse();
 		waitToStart(app);
 	}
 	
 	@Test public void deployDoesInitsAndStarts() throws InterruptedException{
 		final DummyApp app = new DummyApp();
 		manager.deploy(app);
 		waitToStart(app);
 		assertThat(app.inited).isTrue();
 		assertThat(app.started).isTrue();
 		assertThat(app.stoped).isFalse();
 		assertThat(app.finished).isFalse();
 	}
 	
 	@Test public void tearDownStopsAndTearDownAllApplications() throws Exception{
 		final DummyApp app1 = new DummyApp();
 		final DummyApp app2 = new DummyApp();
 		manager.add(app1);
 		manager.add(app2);
 		manager.startApplications();
 		manager.tearDown();
 		assertThat(app1.stoped).isTrue();
 		assertThat(app1.finished).isTrue();
 		assertThat(app2.stoped).isTrue();
 		assertThat(app2.finished).isTrue();
 	}
 	
 	@Test public void tearDownStopsAndTearDownApplicationstwice() throws Exception{
 		final DummyApp app = new DummyApp();
 		manager.add(app);
 		manager.startApplications();
 		manager.tearDown();
 		manager.tearDown();
 		assertThat(app.stopedCount).isEqualTo(1);
 		assertThat(app.finishedCount).isEqualTo(1);
 	}
 	
 	@Ignore
 	//TODO: Disabled as explained in ApplicationManager.teardown()
 	@Test public void tearsDownTheAppWithTheProperOntology() throws Exception{
 		final DummyApp app = new DummyApp();
 		manager.add(app);
 		manager.startApplications();
 		manager.tearDown();
 		assertThat(app.teardownOntology).isNotNull();
 	}
 	
 	@Test public void tearsDownTheAppWithNoOntologyWhenDisable() throws Exception{
 		createOntologyDisabledManager();
 		final DummyApp app = new DummyApp();
 		manager.add(app);
 		manager.tearDown();
 		assertThat(app.teardownOntology).isNull();
 	}
 	
 	@Test public void findDeployedApps() throws Exception{
 		DummyApp app1 = new DummyApp();
 		manager.deploy(app1,"app1");
 		DummyApp app2 = new DummyApp();
 		manager.deploy(app2,"app2");
 		assertThat(manager.findApplication("app1")).isSameAs(app1);
 		assertThat(manager.findApplication("app2")).isSameAs(app2);
 	}
 	
 	@Test public void findAddedApps() throws Exception{
 		DummyApp app1 = new DummyApp();
 		manager.add(app1,"app1");
 		DummyApp app2 = new DummyApp();
 		manager.add(app2,"app2");
 		manager.startApplications();
 		assertThat(manager.findApplication("app1")).isSameAs(app1);
 		assertThat(manager.findApplication("app2")).isSameAs(app2);
 	}
 	
 	@Test public void deployingAppsAutoAsingIds() throws Exception{
 		DummyApp app = new DummyApp();
 		manager.deploy(app);
 		assertThat(manager.findApplication(DummyApp.class.getName()+"0"))
 															.isSameAs(app);
 	}
 	
 	@Test public void addingAppsAutoAsingIds() throws Exception{
 		DummyApp app = new DummyApp();
 		manager.add(app);
 		manager.startApplications();
 		assertThat(manager.findApplication(DummyApp.class.getName()+"0"))
 															.isSameAs(app);
 	}
 	
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Test public void handleServiceConvertsToAMethodCall() throws Exception{
 		DummyApp app = new DummyApp();
 		manager.deploy(app, "myId");
 		
 		ServiceCall serviceCall = new ServiceCall("app","callback","myId");
 		TreeMap parameters = new TreeMap();
 		serviceCall.setParameters(parameters);
 		manager.handleServiceCall(serviceCall,new UOSMessageContext());
 		
 		assertThat(app.callbackMap).isSameAs(parameters);
 	}
 	
 	@Test public void handleServiceReturnsMapAsResponse() throws Exception{
 		DummyApp app = new DummyApp();
 		manager.deploy(app, "myId");
 		
 		ServiceCall call = new ServiceCall("app","callback","myId")
		.addParameter("echo", "ping");
		ServiceResponse r = manager.handleServiceCall(call,new UOSMessageContext());
		
		assertThat(r.getResponseData())
		.contains(MapEntry.entry("echo", "ping"));
	}
	
	@Ignore
	@Test public void handleServiceReturnsMapAsResponseWithourId() throws Exception{
		DummyApp app = new DummyApp();
		manager.deploy(app, "myId");
		
		ServiceCall call = new ServiceCall("app","callback")
 									.addParameter("echo", "ping");
 		ServiceResponse r = manager.handleServiceCall(call,new UOSMessageContext());
 		
 		assertThat(r.getResponseData())
 									.contains(MapEntry.entry("echo", "ping"));
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Test public void handleServiceFailsOnUnexistendId() throws Exception{
 		DummyApp app = new DummyApp();
 		manager.deploy(app, "myId");
 		
 		ServiceCall call = new ServiceCall("app","callback","NotMyId");
 		call.setParameters(new TreeMap());
 		ServiceResponse r =manager.handleServiceCall(call,new UOSMessageContext());
 		
 		assertThat(app.callbackMap).isNull();
 		assertThat(r.getError()).isNotNull();
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Test public void handleServiceFailsOnUnexistendMethod() throws Exception{
 		DummyApp app = new DummyApp();
 		manager.deploy(app, "myId");
 		
 		ServiceCall call = new ServiceCall("app","notCallback","myId");
 		call.setParameters(new TreeMap());
 		ServiceResponse r =manager.handleServiceCall(call,new UOSMessageContext());
 		
 		assertThat(app.callbackMap).isNull();
 		assertThat(r.getError()).isNotNull();
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Test public void handleServiceFailsOnIncorrectMethodInterface() throws Exception{
 		DummyApp app = new DummyApp();
 		manager.deploy(app, "myId");
 		
 		ServiceCall call = new ServiceCall("app","notCallback","myId");
 		call.setParameters(new TreeMap());
 		ServiceResponse r =manager.handleServiceCall(call,new UOSMessageContext());
 		
 		assertThat(app.callbackMap).isNull();
 		assertThat(r.getError()).isNotNull();
 	}
 	
 	private void waitToStart(final DummyApp app) throws InterruptedException {
 		assertEventuallyTrue("Must start a Thread with the app", 1000, 
 				new EventuallyAssert() {
 					public boolean assertion() {
 						return app.started;
 					}
 				});
 	}
 	
 	static interface EventuallyAssert{
 		boolean assertion();
 	}
 	
 	private void assertEventuallyTrue(String msg, long wait, EventuallyAssert assertion) throws InterruptedException{
 		long time = 0;
 		while (time <= wait && !assertion.assertion()){
 			Thread.sleep(10);
 			time += 10;
 		}
 		assertTrue(msg,assertion.assertion());
 	}
 	
 }
