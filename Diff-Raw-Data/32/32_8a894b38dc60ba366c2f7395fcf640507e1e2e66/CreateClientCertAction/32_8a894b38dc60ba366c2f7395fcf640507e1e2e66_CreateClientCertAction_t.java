 
 				/*
  *  Adito
  *
  *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 			
 package com.adito.clientcerts.wizards.actions;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
 import com.adito.core.CoreUtil;
 import com.adito.security.LogonControllerFactory;
 import com.adito.security.SessionInfo;
 import com.adito.wizard.AbstractWizardSequence;
 import com.adito.wizard.DefaultWizardSequence;
 import com.adito.wizard.WizardStep;
 import com.adito.wizard.actions.AbstractWizardAction;
 
 /**
  */
 public class CreateClientCertAction extends AbstractWizardAction {
 
     public final static String ATTR_USER = "user";
    private static final Log LOG = LogFactory.getLog(CreateClientCertAction.class);
 
     /* (non-Javadoc)
      * @see com.adito.core.actions.CoreAction#getNavigationContext(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
     public int getNavigationContext(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
         return SessionInfo.USER_CONSOLE_CONTEXT | SessionInfo.MANAGEMENT_CONSOLE_CONTEXT;
     }
 
     /* (non-Javadoc)
      * @see com.adito.wizard.actions.AbstractWizardAction#unspecified(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
     public ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
         CoreUtil.addRequiredFieldMessage(this, request);
	LOG.info("unspecified called");
         return super.unspecified(mapping, form, request, response);
     }
     
     /* (non-Javadoc)
      * @see com.adito.wizard.actions.AbstractWizardAction#previous(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
     public ActionForward previous(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
                     throws Exception {
         throw new Exception("No previous steps.");
     }
 
     /* (non-Javadoc)
      * @see com.adito.wizard.actions.AbstractWizardAction#createWizardSequence(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
     protected AbstractWizardSequence createWizardSequence(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
	LOG.info("createWizardSequence called");
         ActionForward fwd = mapping.findForward("finish");
         SessionInfo session = LogonControllerFactory.getInstance().getSessionInfo(request);
         DefaultWizardSequence seq = new DefaultWizardSequence(fwd, "clientCerts", "clientCertWizard", CoreUtil.getReferer(request), "clientCertWizard", session);
         seq.putAttribute(ATTR_USER, LogonControllerFactory.getInstance().getUser(request));
         seq.addStep(new WizardStep("/createClientCert.do", true));
         /* seq.addStep(new WizardStep("/webForwardResourceDetails.do"));
         seq.addStep(new WizardStep("/webForwardSpecificDetails.do"));
         seq.addStep(new WizardStep("/webForwardAuthenticationDetails.do"));
         boolean isUserConsole = getSessionInfo(request).isUserConsoleContext();
         seq.addStep(new WizardStep( isUserConsole ? "/webForwardPersonalPolicy.do" : "/webForwardPolicySelection.do"));
         seq.addStep(new WizardStep("/webForwardSummary.do")); */
	LOG.info("createWizardSequence finished");
         return seq;
     }
 }
