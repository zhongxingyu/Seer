 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2008 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * Created on Feb 28, 2005
  *
  */
 package com.sapienter.jbilling.client.order;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import org.apache.struts.config.ModuleConfig;
 import org.apache.struts.util.RequestUtils;
 import org.apache.struts.validator.DynaValidatorForm;
 import org.hibernate.StaleObjectStateException;
 
 import com.sapienter.jbilling.client.util.Constants;
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.interfaces.OrderSession;
 import com.sapienter.jbilling.interfaces.OrderSessionHome;
 import com.sapienter.jbilling.server.order.db.OrderPeriodDTO;
 
 /**
  * @author Emil
  * 
  */
 public class OrderPeriodAction extends Action {
 
 	private static final Logger LOG = Logger.getLogger(MaintainAction.class);
 
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws IOException, ServletException {
 
 		HttpSession session = request.getSession(false);
 		Integer languageId = (Integer) session
 				.getAttribute(Constants.SESSION_LANGUAGE);
 		Integer entityId = (Integer) session
 				.getAttribute(Constants.SESSION_ENTITY_ID_KEY);
 		DynaValidatorForm myForm;
 		try {
 			String ret = "view";
 			JNDILookup EJBFactory = JNDILookup.getFactory(false);
 			ActionMessages messages = new ActionMessages();
 			ActionErrors errors = new ActionErrors();
 
 			String action = request.getParameter("action");
 			OrderSessionHome orderHome = (OrderSessionHome) EJBFactory
 					.lookUpHome(OrderSessionHome.class,
 							OrderSessionHome.JNDI_NAME);
 			OrderSession orderSession = orderHome.create();
 
 			if (action.equals("setup")) {
 				OrderPeriodDTO[] periods = orderSession.getPeriods(entityId,
 						languageId);
 
 				Integer arr1[] = new Integer[periods.length];
 				Integer arr2[] = new Integer[periods.length];
 				String arr3[] = new String[periods.length];
 				String arr4[] = new String[periods.length];
 				for (int f = 0; f < periods.length; f++) {
 					arr1[f] = periods[f].getId();
 					arr2[f] = periods[f].getUnitId();
 					arr3[f] = periods[f].getValue().toString();
 					arr4[f] = periods[f].getDescription();
 				}
 
 				ModuleConfig moduleConfig = RequestUtils.getModuleConfig(
 						request, servlet.getServletContext());
 				myForm = (DynaValidatorForm) RequestUtils.createActionForm(
 						request, mapping, moduleConfig, servlet);
 				myForm.set("id", arr1);
 				myForm.set("unit", arr2);
 				myForm.set("value", arr3);
 				myForm.set("description", arr4);
 
 				session.setAttribute("orderPeriod", myForm);
 				session.setAttribute("orgOrderPeriods", periods); // needed for the 'update'
 			} else if (action.equals("edit")) {
 				myForm = (DynaValidatorForm) form;
 				Integer ids[] = (Integer[]) myForm.get("id");
 				OrderPeriodDTO[] orgPeriods = (OrderPeriodDTO[]) session.getAttribute(
 						"orgOrderPeriods");
 				for (int f = 0; f < ids.length; f++) {
 					// for this to work, the arrays have to be sorted the same way
 					if (orgPeriods[f].getId() != ids[f]) {
 						throw new Exception("periods not in sych");
 					}
 					orgPeriods[f].setUnitId(((Integer[]) myForm.get("unit"))[f]);
 					try {
 						orgPeriods[f].setValue(Integer.valueOf(((String[]) myForm
 								.get("value"))[f]));
 						if (orgPeriods[f].getValue().intValue() <= 0) {
 							throw new Exception();
 						}
 					} catch (Exception e) {
 						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 								"order.period.error.value"));
 						break;
 					}
 					orgPeriods[f].setDescription(((String[]) myForm
 							.get("description"))[f]);
 					if (orgPeriods[f].getDescription() == null
 							|| orgPeriods[f].getDescription().trim().length() == 0) {
 						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 								"order.period.error.description"));
 						break;
 					}
 				}
 
 				if (errors.isEmpty()) {
 					try {
 						orderSession.setPeriods(languageId, orgPeriods);
 						messages.add(ActionMessages.GLOBAL_MESSAGE,
 								new ActionMessage("order.period.updated"));
 						saveMessages(request, messages);
 			        } catch (Exception e) {
 			        	if (e.getCause().getClass().equals(StaleObjectStateException.class)) {
 				            errors.add(ActionErrors.GLOBAL_ERROR,
 				                   new ActionError("errors.modified"));
 			        	} else {
 			        		errors.add(ActionErrors.GLOBAL_ERROR,
 					                   new ActionError("all.internal"));
 			        	}
 						saveErrors(request, errors);
						ret = "refresh";
 					}
 				} else {
 					saveErrors(request, errors);
 				}
 			} else if (action.equals("add")) {
 				orderSession.addPeriod(entityId, languageId);
 				ret = "refresh";
 			} else if (action.equals("delete")) {
 				Integer id = Integer.valueOf(request.getParameter("id"));
 				Boolean result = orderSession.deletePeriod(id);
 				if (result.booleanValue()) {
 					messages.add(ActionMessages.GLOBAL_MESSAGE,
 							new ActionMessage("order.period.deleted", id));
 					saveMessages(request, messages);
 				} else {
 					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 							"order.period.error.delete"));
 					saveErrors(request, errors);
 				}
 				ret = "refresh";
 			}
 
 			return mapping.findForward(ret);
 		} catch (Exception e) {
 			LOG.error("Exception ", e);
 		}
 
 		return mapping.findForward("error");
 	}
 }
