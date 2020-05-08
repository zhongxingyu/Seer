 package org.eclipse.swordfish.p2.internal.deploy.server;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.*;
 import static org.easymock.EasyMock.*;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.equinox.p2.core.ProvisionException;
 import org.eclipse.swordfish.p2.deploy.server.IDirector;
 import org.junit.Before;
 import org.junit.Test;
 import static org.eclipse.swordfish.p2.internal.deploy.server.ObjectFactory.*;
 
 public class ProvisioningServletTest {
 	private ProvisioningServlet servlet;
 	private final String iu = "CoqueApp";
 	private final String version = "1.0.0";
 	private final String message = "Le coque est mort.";
 
 
 	@Before
 	public void setUp() throws Exception {
 		servlet = new ProvisioningServlet();
 	}
 	
 	
 	/**
 	 * Test that setting the director works
 	 */
 	@Test
 	public void testSetDirector() {
 		IDirector director = createStrictMock(IDirector.class);
 		servlet.setDirector(director);
 		assertEquals(director, servlet.getDirector());
 	}
 
 	
 	/**
 	 * In case of an illegal operation verb
 	 */
 	@Test
 	public void testIllegalOperation() throws IOException, ServletException {
 		String illegalOperation = "KillHasi";
 		HttpServletRequest request = createRequest(illegalOperation, iu, version);
 		HttpServletResponse response = createResponse();
 		servlet.doPut(request, response);
 	}
 
 	
 	/**
 	 * Install operation failed!
 	 */
 	@Test
 	public void testInstallFailed() 
 		throws IOException, ServletException, ProvisionException, OperationCanceledException {
 		HttpServletRequest request = createRequest(ProvisioningServlet.OPERATION_INSTALL, iu, version);
 		HttpServletResponse response = createResponse();
 
 		IDirector director = createStrictMock(IDirector.class);
 		expect(director.install(iu, version, null,
 						ProvisioningServlet.DEFAULT_PROFILE)).andReturn(createStatus(IStatus.ERROR, message));
 		replay(director);
 
 		servlet.setDirector(director);
 		servlet.doPut(request, response);
 	}
 
 	
 	/**
 	 * Install operation succeeded!
 	 */
 	@Test
 	public void testInstallSucceeded() 
 		throws IOException, ServletException, ProvisionException, OperationCanceledException {
 		HttpServletRequest request = createRequest(ProvisioningServlet.OPERATION_INSTALL, iu, version);
 		HttpServletResponse response = createStrictMock(HttpServletResponse.class);
 
 		IDirector director = createStrictMock(IDirector.class);
 		expect(director.install(iu, version, null,
 						ProvisioningServlet.DEFAULT_PROFILE)).andReturn(createStatus(IStatus.OK, message));
 		replay(response, director);
 
 		servlet.setDirector(director);
 		servlet.doPut(request, response);
 	}
 
 	
 	/**
 	 * Update operation succeeded!
 	 */
 	@Test
 	public void testUpdateSucceeded() 
 		throws IOException, ServletException, ProvisionException, OperationCanceledException {
 		HttpServletRequest request = createRequest(ProvisioningServlet.OPERATION_UPDATE, iu, version);
 		HttpServletResponse response = createStrictMock(HttpServletResponse.class);
 
 		IDirector director = createStrictMock(IDirector.class);
 		expect(director.update(iu, version, null,
 						ProvisioningServlet.DEFAULT_PROFILE)).andReturn(createStatus(IStatus.OK, message));
 		replay(response, director);
 
 		servlet.setDirector(director);
 		servlet.doPut(request, response);
 	}
 
 	
 	/**
 	 * Remove operation succeeded!
 	 */
 	@Test
 	public void testRemoveSucceeded() 
 		throws IOException, ServletException, ProvisionException, OperationCanceledException {
 		HttpServletRequest request = createRequest(ProvisioningServlet.OPERATION_REMOVE, iu, version);
 		HttpServletResponse response = createStrictMock(HttpServletResponse.class);
 
 		IDirector director = createStrictMock(IDirector.class);
 		expect(director.remove(iu, version
 				, ProvisioningServlet.DEFAULT_PROFILE)).andReturn(createStatus(IStatus.OK, message));
 		replay(response, director);
 
 		servlet.setDirector(director);
 		servlet.doPut(request, response);
 	}
 
 	
 	/**
 	 * Exception during install!
 	 */
 	@Test
 	public void testExceptionInDoPut() 
 		throws IOException, ServletException, ProvisionException, OperationCanceledException {
 		HttpServletResponse response = createStrictMock(HttpServletResponse.class);
 		HttpServletRequest request = createMock(HttpServletRequest.class);
 		expect(request.getHeader(ProvisioningServlet.PARAM_IU_OPERATION)).andReturn(null);
		expect(request.getHeader(ProvisioningServlet.PARAM_IU_TARGET_PROFILE)).andReturn(null);
 		replay(request);
 		servlet.doPut(request, response);
 	}
 	
 	
 	/**
 	 * Check that a null parameter results in a servlet exception
 	 */
 	@Test(expected=ServletException.class)
 	public void testGetSaveParameterNull() throws ServletException {
 		HttpServletRequest request = createMock(HttpServletRequest.class);
 		expect(request.getHeader(iu)).andReturn(null);
 		replay(request);
 		servlet.getSaveParameter(request, iu);
 	}
 	
 	
 	/**
 	 * Check that a null parameter does not result in a servlet exception if a default value is specified
 	 */
 	@Test
 	public void testGetSaveParameterNullWithDefault() throws ServletException {
 		HttpServletRequest request = createMock(HttpServletRequest.class);
 		expect(request.getHeader(iu)).andReturn(null);
 		replay(request);
 		servlet.getSaveParameter(request, iu, "hasi");
 	}
 
 	
 	/**
 	 * Check that an empty string parameter results in a servlet exception
 	 */
 	@Test(expected=ServletException.class)
 	public void testGetSaveParameterEmtpyString() throws ServletException {
 		HttpServletRequest request = createMock(HttpServletRequest.class);
 		expect(request.getHeader(iu)).andReturn("");
 		replay(request);
 		servlet.getSaveParameter(request, iu);
 	}
 
 	
 	/**
 	 * Check that a non empty/ non null parameter returns the value without exception
 	 */
 	@Test
 	public void testGetSaveParameterOK() throws ServletException {
 		HttpServletRequest request = createMock(HttpServletRequest.class);
 		String value = "Schnuddel";
		expect(request.getHeader(iu)).andReturn(value).times(2);
 		replay(request);
 		assertThat(servlet.getSaveParameter(request, iu), is(value));
 	}
 
 	
 	/**
 	 * Test sending of errors on the response is called
 	 */
 	@Test
 	public void testSendError() throws IOException {
 		String message = "Le coque est mort.";
 		HttpServletResponse resp = createResponse();
 		servlet.sendError(resp, message);
 	}
 
 
 	/**
 	 * Test formatting of errors
 	 */
 	@Test
 	public void testFormatError() {
 		String op = "install";
 		String iu = "CoqueApp";
 		String version = "1.0.0";
 		String message = "Le coque est mort.";
 		IStatus status = new Status(IStatus.OK, "Coque", message);
 		String expected = op + "(\"" + iu + "\",\"" + version + "\")\n" + message;
 		assertEquals(expected, servlet.formatError(op, iu, version, status));
 	}
 	
 	
 	private HttpServletRequest createRequest(String operation, String iu, String version) throws IOException {
		HttpServletRequest request = createNiceMock(HttpServletRequest.class);
		expect(request.getHeader(ProvisioningServlet.PARAM_IU_OPERATION)).andReturn(operation).times(2);
		expect(request.getHeader(ProvisioningServlet.PARAM_IU_ID)).andReturn(iu).times(2);
		expect(request.getHeader(ProvisioningServlet.PARAM_IU_VERSION)).andReturn(version).times(2);
 		expect(request.getInputStream()).andReturn(null);
 		replay(request);
 		return request;
 	}
 	
 	
 	private HttpServletResponse createResponse() throws IOException {
 		HttpServletResponse response = createStrictMock(HttpServletResponse.class);
 		response.sendError(anyInt(), (String) anyObject());
 		replay(response);
 		return response;
 	}
 }
