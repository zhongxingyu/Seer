 /**************************************************************************
  * alpha-Portal: A web portal, for managing knowledge-driven 
  * ad-hoc processes, in form of case files.
  * ==============================================
  * Copyright (C) 2011-2012 by 
  *   - Christoph P. Neumann (http://www.chr15t0ph.de)
  *   - and the SWAT 2011 team
  **************************************************************************
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *     http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  **************************************************************************
  * $Id$
  *************************************************************************/
 package alpha.portal.webapp.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.appfuse.model.User;
 import org.appfuse.service.UserManager;
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.DataBinder;
 import org.springframework.web.servlet.ModelAndView;
 
 import alpha.portal.model.AlphaCase;
 import alpha.portal.service.CaseManager;
 import alpha.portal.webapp.util.CardFilterHolder;
 
 /**
  * The Class CaseFormControllerTest.
  */
 public class CaseFormControllerTest extends BaseControllerTestCase {
 
 	/** The Constant caseId. */
 	private static final String caseId = "550e4713-e22b-11d4-a716-446655440000";
 
 	/** The form. */
 	@Autowired
 	private CaseFormController form;
 
 	/** The user manager. */
 	@Autowired
 	private UserManager userManager;
 
 	/** The case manager. */
 	@Autowired
 	private CaseManager caseManager;
 
 	/** The filters. */
 	private final CardFilterHolder filters = new CardFilterHolder();
 
 	/**
 	 * This test is run here since we don't have a userManager available in the
 	 * core module and participants are connected with a case.
 	 */
 	@Test
 	public void testCaseCRA() {
 		final User u = this.userManager.get(-2L);
 		AlphaCase aCase = this.caseManager.get(CaseFormControllerTest.caseId);
 		Assert.assertNotNull(aCase);
 		Assert.assertNotNull(aCase.getListOfParticipants());
 		aCase.addParticipant(u);
 		aCase = this.caseManager.save(aCase);
 
 		final Set<User> participants = aCase.getListOfParticipants();
 		// this must be true from sample data
 		aCase = this.caseManager.get(CaseFormControllerTest.caseId);
 		Assert.assertTrue(participants.contains(u));
 	}
 
 	/**
 	 * Test new.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@Test
 	public void testNew() throws Exception {
 		final MockHttpServletRequest request = this.newGet("/caseform");
 		request.setRemoteUser("admin");
 
 		final HttpServletResponse response = new MockHttpServletResponse();
 		final ModelAndView mv = this.form.showForm(this.filters, request,
 				response);
 		Assert.assertNotNull(mv);
 		Assert.assertEquals("caseform", mv.getViewName());
 		Assert.assertEquals(new AlphaCase(), mv.getModel().get("case"));
 	}
 
 	/**
 	 * Test last.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@Test
 	public void testLast() throws Exception {
 		MockHttpServletRequest request = this.newGet("/caseform");
 		request.setParameter("caseId", "550e4713-e22b-11d4-a716-446655440002");
 		request.setRemoteUser("admin");
 		ModelAndView mv = this.form.showForm(this.filters, request,
 				new MockHttpServletResponse());
 
 		request = this.newGet("/caseform");
 		request.setParameter("caseId", "last");
 		request.setRemoteUser("admin");
 
 		final AlphaCase aCase = this.caseManager
 				.get("550e4713-e22b-11d4-a716-446655440002");
 		mv = this.form.showForm(this.filters, request,
 				new MockHttpServletResponse());
 		Assert.assertEquals("caseform", mv.getViewName());
 		Assert.assertEquals(aCase, mv.getModel().get("case"));
 	}
 
 	/**
 	 * Test add.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@Test
 	public void testAdd() throws Exception {
 		MockHttpServletRequest request = this.newGet("/caseform");
 		request.setRemoteUser("admin");
 		final ModelAndView mv = this.form.showForm(this.filters, request,
 				new MockHttpServletResponse());
 
 		request = this.newPost("/caseform");
 		request.setRemoteUser("admin");
 		final AlphaCase aCase = (AlphaCase) mv.getModel().get("case");
 		aCase.setName("test case which does not exist yet");
 		final BindingResult errors = new DataBinder(aCase).getBindingResult();
 		final String view = this.form.addCase(aCase, errors, request,
 				new MockHttpServletResponse());
 
 		final List<AlphaCase> dbCases = this.caseManager.findByName(aCase
 				.getName());
 		Assert.assertNotNull(dbCases);
 		Assert.assertTrue(dbCases.size() >= 1);
 		final AlphaCase dbCase = dbCases.get(0);
 		Assert.assertNotNull(dbCase);
 		Assert.assertEquals("redirect:/caseform?caseId=" + dbCase.getCaseId(),
 				view);
 		Assert.assertFalse(errors.hasErrors());
 		Assert.assertNotNull(request.getSession().getAttribute(
 				"successMessages"));
 
 		final Locale locale = request.getLocale();
 		final ArrayList<Object> msgs = (ArrayList<Object>) request.getSession()
 				.getAttribute("successMessages");
 		Assert.assertTrue(msgs.contains(this.form.getText("case.added", locale)));
 	}
 
 	/**
 	 * Test edit.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@Test
 	public void testEdit() throws Exception {
 		MockHttpServletRequest request = this.newGet("/caseform");
 		request.setParameter("caseId", CaseFormControllerTest.caseId);
 		request.setRemoteUser("admin");
 		final ModelAndView mv = this.form.showForm(this.filters, request,
 				new MockHttpServletResponse());
 		Assert.assertEquals("caseform", mv.getViewName());
 		final AlphaCase aCase = (AlphaCase) mv.getModel().get("case");
 		AlphaCase dbCase = this.caseManager.get(CaseFormControllerTest.caseId);
 		Assert.assertEquals(dbCase, aCase);
 		Assert.assertEquals(dbCase.getAlphaCards(), mv.getModel().get("cards"));
 		Assert.assertEquals(dbCase.getListOfParticipants(),
 				mv.getModel().get("participants"));
 
 		request = this.newPost("/caseform");
 		request.setRemoteUser("admin");
 		aCase.setName("test case with a new name");
 		final BindingResult errors = new DataBinder(aCase).getBindingResult();
 		final String view = this.form.saveCase(aCase, errors, request,
 				new MockHttpServletResponse());
 		Assert.assertEquals("redirect:/caseform?caseId=" + aCase.getCaseId(),
 				view);
 		Assert.assertFalse(errors.hasErrors());
 		Assert.assertNotNull(request.getSession().getAttribute(
 				"successMessages"));
 
 		final Locale locale = request.getLocale();
 		final ArrayList<Object> msgs = (ArrayList<Object>) request.getSession()
 				.getAttribute("successMessages");
 		Assert.assertTrue(msgs.contains(this.form.getText("case.updated",
 				locale)));
 
 		dbCase = this.caseManager.get(CaseFormControllerTest.caseId);
		Assert.assertEquals(dbCase, aCase);
 	}
 
 	/**
 	 * Test delete.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@Test
 	public void testDelete() throws Exception {
 		MockHttpServletRequest request = this.newGet("/caseform");
 		request.setParameter("caseId", CaseFormControllerTest.caseId);
 		request.setRemoteUser("admin");
 		final ModelAndView mv = this.form.showForm(this.filters, request,
 				new MockHttpServletResponse());
 		final AlphaCase myCase = (AlphaCase) mv.getModel().get("case");
 
 		request = this.newPost("/caseform");
 		request.setRemoteUser("admin");
 		request.addParameter("delete", "");
 
 		final BindingResult errors = new DataBinder(myCase).getBindingResult();
 		final String view = this.form.deleteCase(myCase, errors, request);
 		Assert.assertEquals(this.form.getCancelView(), view);
 		Assert.assertNotNull(request.getSession().getAttribute(
 				"successMessages"));
 
 		final Locale locale = request.getLocale();
 		final ArrayList<Object> msgs = (ArrayList<Object>) request.getSession()
 				.getAttribute("successMessages");
 		Assert.assertTrue(msgs.contains(this.form.getText("case.deleted",
 				locale)));
 
 		Assert.assertFalse(this.caseManager
 				.exists(CaseFormControllerTest.caseId));
 	}
 }
