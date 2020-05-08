 package test;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import restdisp.urltree.LookupTree;
 import restdisp.urltree.Node;
 import restdisp.urltree.UrlDescriptor;
 import restdisp.urltree.UrlTreeBuilder;
 import restdisp.validation.HandlerException;
 import restdisp.validation.RoutingException;
 import restdisp.validation.ConfigurationException;
 import restdisp.worker.TreeExecutor;
 import static org.junit.Assert.*; 
 
 import org.junit.Test;
 
 public class ExceptionTest {
 	private InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test/conf/router.conf");
 	private Node root = UrlTreeBuilder.buildUrlTree(is);
 	public ExceptionTest() throws Exception {}
 	
 	@Test
 	public void testHandlerException() throws ConfigurationException, IOException, RoutingException, restdisp.validation.HandlerException {
 		try {
 			UrlDescriptor res = LookupTree.getPath(root, "get", "/svc/exc");
 			TreeExecutor.exec(res, null, null);
 			assertTrue(false);
 		} catch (HandlerException e) {
 			assertTrue(e.getMessage().contains("Handler invocation exception [test.actors.Action:getException]. Variables count [0]."));
 		}
 	}
 	
 	@Test
 	@SuppressWarnings("unused")
 	public void testRoutingException() throws ConfigurationException, IOException, RoutingException, restdisp.validation.HandlerException {
 		try {
 			UrlDescriptor res = LookupTree.getPath(root, "get", "/svc/exc/tst");
 			assertTrue(false);
 		} catch (RoutingException e) {
 			assertTrue(e.getMessage().contains("Path not defined [/get/svc/exc/tst]"));
 		}
 	}
 	
 	@Test
 	@SuppressWarnings("unused")
 	public void testConfigurationExceptionBranch() throws ConfigurationException, IOException {
 		try {
 			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test/conf/router.exception.branch.conf");
 			Node root = UrlTreeBuilder.buildUrlTree(is);
 			assertTrue(false);
 		} catch (ConfigurationException e) {
 			Throwable inner = e.getCause();
 			assertTrue(e.getMessage().contains("Failed to add branch"));
 			assertTrue(inner.getMessage().contains("Method not found [class test.actors.Action:getExceptionCase]. Variables count [0]."));
 		}
 	}
 	
 	@Test
 	@SuppressWarnings("unused")
 	public void testConfigurationExceptionMethod() throws ConfigurationException, IOException {
 		try {
 			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test/conf/router.exception.method.conf");
 			Node root = UrlTreeBuilder.buildUrlTree(is);
 			assertTrue(false);
 		} catch (ConfigurationException e) {
 			Throwable inner = e.getCause();
 			assertTrue(e.getMessage().contains("Failed to add branch"));
 			assertTrue(inner.getMessage().contains("Method not found [class test.actors.Action:getException]. Variables count [1]."));
 		}
 	}
 	
 	@Test
 	@SuppressWarnings("unused")
 	public void testConfigurationExceptionGenMethod() throws ConfigurationException, IOException {
 		try {
 			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test/conf/router.exception.genericmethod.conf");
 			Node root = UrlTreeBuilder.buildUrlTree(is);
 			assertTrue(false);
 		} catch (ConfigurationException e) {
 			Throwable inner = e.getCause();
 			assertTrue(e.getMessage().contains("Failed to add branch"));
 			assertTrue(inner.getMessage().contains("Failed to build leaf. Default constructor not found [test.actors.ActionMethodErr]."));
 		}
 	}
 	
 	@Test
 	@SuppressWarnings("unused")
 	public void testConfigurationGenClassException() throws ConfigurationException, IOException {
 		try {
 			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test/conf/router.exception.genericclass.conf");
 			Node root = UrlTreeBuilder.buildUrlTree(is);
 			assertTrue(false);
 		} catch (ConfigurationException e) {
 			Throwable inner = e.getCause();
 			assertTrue(e.getMessage().contains("Failed to add branch"));
 			assertTrue(inner.getMessage().contains("Failed to build leaf. Class not found [test.actors.ActionErr]."));
 		}
 	}
 	
 	@Test
 	@SuppressWarnings("unused")
 	public void testMethodValiationException() throws ConfigurationException, IOException {
 		try {
 			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test/conf/router.exception.methodvalidation.conf");
 			Node root = UrlTreeBuilder.buildUrlTree(is);
 			assertTrue(false);
 		} catch (ConfigurationException e) {
 			Throwable inner = e.getCause();
 			assertTrue(e.getMessage().contains("Failed to add branch"));
 			assertTrue(inner.getMessage().contains("Wrong method [gets]"));
 		}
 	}
 	
 	@Test
 	@SuppressWarnings("unused")
 	public void testUrlValValiationException() throws ConfigurationException, IOException {
 		try {
 			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test/conf/router.exception.urlvalvalidation.conf");
 			Node root = UrlTreeBuilder.buildUrlTree(is);
 			assertTrue(false);
 		} catch (ConfigurationException e) {
 			Throwable inner = e.getCause();
 			Throwable inner2 = inner.getCause();
 			assertTrue(e.getMessage().contains("Failed to add branch"));
 			assertTrue(inner.getMessage().contains("Wrong configuration entry [/svc/exc/{id]"));
 			assertTrue(inner2.getMessage().contains("Wrong value [{id]"));
 		}
 	}
 	
 	@Test
 	public void testAbstractWorkerException() throws ConfigurationException, IOException, RoutingException, HandlerException {
 		try {
 			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test/conf/router.exception.abstractworkerexc.conf");
 			Node root = UrlTreeBuilder.buildUrlTree(is);
 			UrlDescriptor res = LookupTree.getPath(root, "get", "/svc/exc/1");
 			TreeExecutor.exec(res, null, null);
 			assertTrue(false);
 		} catch (RoutingException e) {
 			assertTrue(e.getMessage().contains("Failed to instantiate worker [test.actors.UsrAbstractWorker]"));
 		}
 	}
 	
 	@Test
 	public void testAbstractConstructorException() throws ConfigurationException, IOException, RoutingException, HandlerException {
 		try {
 			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test/conf/router.exception.consworkerexc.conf");
 			Node root = UrlTreeBuilder.buildUrlTree(is);
 			UrlDescriptor res = LookupTree.getPath(root, "get", "/svc/exc/1");
 			TreeExecutor.exec(res, null, null);
 			assertTrue(false);
 		} catch (HandlerException e) {
 			assertTrue(e.getMessage().contains("Constructor invocation exception [test.actors.ConstructorException]"));
 		}
 	}
 	
 	@Test
 	public void testWrongArgsException() throws ConfigurationException, IOException, RoutingException, HandlerException {
 		try {
 			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test/conf/router.exception.wrongargs.conf");
 			@SuppressWarnings("unused")
 			Node root = UrlTreeBuilder.buildUrlTree(is);
 			assertTrue(false);
 		} catch (ConfigurationException e) {
 			Throwable inner = e.getCause();
 			Throwable inner2 = inner.getCause();
 			assertTrue(e.getMessage().contains("Failed to add branch [get /svc/exc/{id} test.actors.WrongArgs:test]"));
 			assertTrue(inner.getMessage().contains("Class method has unsupported argument [test.actors.WrongArgs.test]"));
 			assertTrue(inner2.getMessage().contains("Unsupported argument type [java.util.Arrays]"));
 		}
 	}
 	
 	@Test
 	public void testCastException() throws ConfigurationException, IOException, RoutingException, restdisp.validation.HandlerException {
 		try {
 			UrlDescriptor res = LookupTree.getPath(root, "get", "/svc/act/1a");
 			TreeExecutor.exec(res, null, null);
 			assertTrue(false);
 		} catch (Exception e) {
 			Throwable inner = e.getCause();
 			Throwable inner2 = inner.getCause();
 			assertTrue(e.getMessage().contains("Failed to call method: [test.actors.Action:getUser()]"));
 			assertTrue(inner.getMessage().contains("Failed to cast variable for method call: ['1a' => int]"));
 			assertTrue(inner2.getMessage().contains("For input string: \"1a\""));
 		}
 	}
 	
 	@Test
 	public void testCastCharacterException() throws ConfigurationException, IOException, RoutingException, restdisp.validation.HandlerException {
 		try {
 			UrlDescriptor res = LookupTree.getPath(root, "get", "/svc/act/true/true/1/1/256/256/c/ss");
 			TreeExecutor.exec(res, null, null);
 			assertTrue(false);
 		} catch (Exception e) {
 			Throwable inner = e.getCause();
 			Throwable inner2 = inner.getCause();
 			assertTrue(e.getMessage().contains("Failed to call method: [test.actors.Action:getShortTypes()]"));
 			assertTrue(inner.getMessage().contains("Failed to cast variable for method call: ['ss' => class java.lang.Character]"));
 			assertTrue(inner2.getMessage().contains("Failed to cast String to Character [ss]"));
 		}
 	}
 	
 	@Test
 	public void testWrongWorkerMethodException() throws ConfigurationException, IOException, RoutingException, restdisp.validation.HandlerException {
 		try {
 			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test/conf/router.exception.wrongworkermethod.conf");
 			@SuppressWarnings("unused")
 			Node root = UrlTreeBuilder.buildUrlTree(is);
 			assertTrue(false);
 		} catch (ConfigurationException e) {
 			ConfigurationException inner = (ConfigurationException) e.getCause();
 			assertTrue(e.getMessage().contains("Failed to add branch [get /svc/act test.actors.Action]"));
 			assertTrue(inner.getMessage().contains("Wrong class method entry [get /svc/act test.actors.Action]"));
 		}
 	}
 }
