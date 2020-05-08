 /*
  * Created on Dec 3, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package gov.nih.nci.security.upt.actions;
 
 /**
  *
  *<!-- LICENSE_TEXT_START -->
  *
  *The NCICB Common Security Module's User Provisioning Tool (UPT) Software License,
  *Version 3.0 Copyright 2004-2005 Ekagra Software Technologies Limited ('Ekagra')
  *
  *Copyright Notice.  The software subject to this notice and license includes both
  *human readable source code form and machine readable, binary, object code form
  *(the 'UPT Software').  The UPT Software was developed in conjunction with the
  *National Cancer Institute ('NCI') by NCI employees and employees of Ekagra.  To
  *the extent government employees are authors, any rights in such works shall be
  *subject to Title 17 of the United States Code, section 105.    
  *
  *This UPT Software License (the 'License') is between NCI and You.  'You (or
  *'Your') shall mean a person or an entity, and all other entities that control,
  *are controlled by, or are under common control with the entity.  'Control' for
  *purposes of this definition means (i) the direct or indirect power to cause the
  *direction or management of such entity, whether by contract or otherwise, or
  *(ii) ownership of fifty percent (50%) or more of the outstanding shares, or
  *(iii) beneficial ownership of such entity.  
  *
  *This License is granted provided that You agree to the conditions described
  *below.  NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up,
  *no-charge, irrevocable, transferable and royalty-free right and license in its
  *rights in the UPT Software to (i) use, install, access, operate, execute, copy,
  *modify, translate, market, publicly display, publicly perform, and prepare
  *derivative works of the UPT Software; (ii) distribute and have distributed to
  *and by third parties the UPT Software and any modifications and derivative works
  *thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to
  *third parties, including the right to license such rights to further third
  *parties.  For sake of clarity, and not by way of limitation, NCI shall have no
  *right of accounting or right of payment from You or Your sublicensees for the
  *rights granted under this License.  This License is granted at no charge to You.
  *
  *1.	Your redistributions of the source code for the Software must retain the
  *above copyright notice, this list of conditions and the disclaimer and
  *limitation of liability of Article 6 below.  Your redistributions in object code
  *form must reproduce the above copyright notice, this list of conditions and the
  *disclaimer of Article 6 in the documentation and/or other materials provided
  *with the distribution, if any.
  *2.	Your end-user documentation included with the redistribution, if any, must
  *include the following acknowledgment: 'This product includes software developed
  *by Ekagra and the National Cancer Institute.'  If You do not include such
  *end-user documentation, You shall include this acknowledgment in the Software
  *itself, wherever such third-party acknowledgments normally appear.
  *
  *3.	You may not use the names 'The National Cancer Institute', 'NCI' 'Ekagra
  *Software Technologies Limited' and 'Ekagra' to endorse or promote products
  *derived from this Software.  This License does not authorize You to use any
  *trademarks, service marks, trade names, logos or product names of either NCI or
  *Ekagra, except as required to comply with the terms of this License.
  *
  *4.	For sake of clarity, and not by way of limitation, You may incorporate this
  *Software into Your proprietary programs and into any third party proprietary
  *programs.  However, if You incorporate the Software into third party proprietary
  *programs, You agree that You are solely responsible for obtaining any permission
  *from such third parties required to incorporate the Software into such third
  *party proprietary programs and for informing Your sublicensees, including
  *without limitation Your end-users, of their obligation to secure any required
  *permissions from such third parties before incorporating the Software into such
  *third party proprietary software programs.  In the event that You fail to obtain
  *such permissions, You agree to indemnify NCI for any claims against NCI by such
  *third parties, except to the extent prohibited by law, resulting from Your
  *failure to obtain such permissions.
  *
  *5.	For sake of clarity, and not by way of limitation, You may add Your own
  *copyright statement to Your modifications and to the derivative works, and You
  *may provide additional or different license terms and conditions in Your
  *sublicenses of modifications of the Software, or any derivative works of the
  *Software as a whole, provided Your use, reproduction, and distribution of the
  *Work otherwise complies with the conditions stated in this License.
  *
  *6.	THIS SOFTWARE IS PROVIDED 'AS IS,' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
  *(INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  *NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  IN NO
  *EVENT SHALL THE NATIONAL CANCER INSTITUTE, EKAGRA, OR THEIR AFFILIATES BE LIABLE
  *FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  *DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  *SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  *TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  *THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *<!-- LICENSE_TEXT_END -->
  *
  */
 
 
 
 import java.util.ArrayList;
 import org.apache.log4j.Logger;
 
 import gov.nih.nci.logging.api.user.UserInfoHelper;
 import gov.nih.nci.security.exceptions.CSException;
 import gov.nih.nci.security.upt.constants.DisplayConstants;
 import gov.nih.nci.security.upt.constants.ForwardConstants;
 import gov.nih.nci.security.upt.forms.BaseDBForm;
 import gov.nih.nci.security.upt.forms.LoginForm;
 import gov.nih.nci.security.upt.viewobjects.SearchResult;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import org.apache.struts.actions.DispatchAction;
 
 /**
  * @author Kunal Modi (Ekagra Software Technologies Ltd.)
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class CommonDBAction extends DispatchAction
 {
 	private static final Logger logDB = Logger.getLogger(CommonDBAction.class);
 	
 	public ActionForward loadHome(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDBForm baseDBForm = (BaseDBForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDB.isDebugEnabled())
 				logDB.debug("||"+baseDBForm.getFormName()+"|loadHome|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		
 		session.removeAttribute(DisplayConstants.CURRENT_ACTION);
 		session.removeAttribute(DisplayConstants.CURRENT_FORM);
 		session.removeAttribute(DisplayConstants.SEARCH_RESULT);
 
 		if (logDB.isDebugEnabled())
 			logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDBForm.getFormName()+"|loadHome|Success|Load the Home Page||");
 		
 		return (mapping.findForward(ForwardConstants.LOAD_HOME_SUCCESS));	
 	}
 	
 	public ActionForward loadAdd(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDBForm baseDBForm = (BaseDBForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDB.isDebugEnabled())
 				logDB.debug("||"+baseDBForm.getFormName()+"|loadAdd|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		
 		baseDBForm.resetForm();
 
 		session.setAttribute(DisplayConstants.CURRENT_ACTION, DisplayConstants.ADD);
 		session.setAttribute(DisplayConstants.CURRENT_FORM, baseDBForm);
 
 		if (logDB.isDebugEnabled())
 			logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDBForm.getFormName()+"|loadAdd|Success|Loading the Add Page||");
 		
 		return (mapping.findForward(ForwardConstants.LOAD_ADD_SUCCESS));
 	}
 	
 	public ActionForward loadSearch(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDBForm baseDBForm = (BaseDBForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDB.isDebugEnabled())
 				logDB.debug("||"+baseDBForm.getFormName()+"|loadSearch|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		
 		baseDBForm.resetForm();
 
 		session.setAttribute(DisplayConstants.CURRENT_ACTION, DisplayConstants.SEARCH);
 		session.setAttribute(DisplayConstants.CURRENT_FORM, baseDBForm);
 
 		if (logDB.isDebugEnabled())
 			logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDBForm.getFormName()+"|loadSearch|Success|Loading the Search Page||");
 		
 		return (mapping.findForward(ForwardConstants.LOAD_SEARCH_SUCCESS));
 	}
 	
 	public ActionForward loadSearchResult(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDBForm baseDBForm = (BaseDBForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDB.isDebugEnabled())
 				logDB.debug("||"+baseDBForm.getFormName()+"|loadSearchResult|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		
 		// VNP: Added this to handle Original search results for popup searches.
 		if(session.getAttribute(DisplayConstants.ORIGINAL_SEARCH_RESULT) != null){
 			session.removeAttribute(DisplayConstants.ORIGINAL_SEARCH_RESULT);
 		}
 
 		if (logDB.isDebugEnabled())
 			logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDBForm.getFormName()+"|loadSearchResult|Success|Loading the Search Result Page||");		
 		
 		return (mapping.findForward(ForwardConstants.LOAD_SEARCH_RESULT_SUCCESS));
 	}
 	
 	/**
 	* Added this method to handle pre-popup search results.
 	*/
 
 	public ActionForward loadOriginalSearchResult(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDBForm baseDBForm = (BaseDBForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDB.isDebugEnabled())
 				logDB.debug("||"+baseDBForm.getFormName()+"|loadSearchResult|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		
 		
 		if(session.getAttribute(DisplayConstants.ORIGINAL_SEARCH_RESULT) != null){
 			session.setAttribute(DisplayConstants.SEARCH_RESULT,session.getAttribute(DisplayConstants.ORIGINAL_SEARCH_RESULT));
 			session.removeAttribute(DisplayConstants.ORIGINAL_SEARCH_RESULT);
 		}
 
 		if (logDB.isDebugEnabled())
 			logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDBForm.getFormName()+"|loadSearchResult|Success|Loading the Search Result Page||");		
 		
 		return (mapping.findForward(ForwardConstants.LOAD_SEARCH_RESULT_SUCCESS));
 	}
 
 
 	
 	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDBForm baseDBForm = (BaseDBForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDB.isDebugEnabled())
 				logDB.debug("||"+baseDBForm.getFormName()+"|create|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		UserInfoHelper.setUserInfo(((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId(), session.getId());		
 		try
 		{
 			errors = form.validate(mapping, request);
 			if(!errors.isEmpty()) 
 			{
 				saveErrors(request,errors);
 				session.setAttribute(DisplayConstants.CURRENT_FORM, baseDBForm);
 				if (logDB.isDebugEnabled())
 					logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 						"|"+baseDBForm.getFormName()+"|create|Failure|Error validating the "+baseDBForm.getFormName()+" object|"
 						+form.toString()+"|");
 				return mapping.getInputForward();
 			}
 			baseDBForm.buildDBObject(request);
 			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(DisplayConstants.MESSAGE_ID, "Add Successful"));
 			saveMessages( request, messages );
 		}
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));
 			saveErrors( request,errors );
 			if (logDB.isDebugEnabled())
 				logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDBForm.getFormName()+"|create|Failure|Error Adding the "+baseDBForm.getFormName()+" object|"
 					+form.toString()+"|"+ cse.getMessage());
 		}
 		session.setAttribute(DisplayConstants.CURRENT_FORM, baseDBForm);
 		if (logDB.isDebugEnabled())
 			logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 				"|"+baseDBForm.getFormName()+"|create|Success|Adding a  new "+baseDBForm.getFormName()+" object|"
 				+form.toString()+"|");		
 		return (mapping.findForward(ForwardConstants.CREATE_SUCCESS));
 	}
 
 	
 	public ActionForward read(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDBForm baseDBForm = (BaseDBForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDB.isDebugEnabled())
 				logDB.debug("||"+baseDBForm.getFormName()+"|read|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		if (baseDBForm.getPrimaryId() == null || baseDBForm.getPrimaryId().equalsIgnoreCase(""))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, "A record needs to be selected first to view details" ));
 			saveErrors( request,errors );
 			if (logDB.isDebugEnabled())
 				logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDBForm.getFormName()+"|read|Failure|No Primary Id for "+baseDBForm.getFormName()+" object||");
 			return (mapping.findForward(ForwardConstants.READ_FAILURE));
 		}
 		try
 		{
 			baseDBForm.buildDisplayForm(request);
 		}
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));			
 			saveErrors( request,errors );
 			if (logDB.isDebugEnabled())
 				logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDBForm.getFormName()+"|read|Failure|Error Reading the "+baseDBForm.getFormName()+" object|"
 					+form.toString()+"|"+ cse.getMessage());
 		}
 		
 		session.setAttribute(DisplayConstants.CURRENT_FORM, baseDBForm);
 		if (logDB.isDebugEnabled())
 			logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 				"|"+baseDBForm.getFormName()+"|read|Success|Success reading "+baseDBForm.getFormName()+" object|"
 				+form.toString()+"|");
 		return (mapping.findForward(ForwardConstants.READ_SUCCESS));
 
 	}
 	
 	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDBForm baseDBForm = (BaseDBForm)form;
 	
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDB.isDebugEnabled())
 				logDB.debug("||"+baseDBForm.getFormName()+"|update|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		UserInfoHelper.setUserInfo(((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId(), session.getId());
 		try
 		{
 			errors = form.validate(mapping, request);
 			if(!errors.isEmpty()) 
 			{
 				saveErrors(request,errors);
 				session.setAttribute(DisplayConstants.CURRENT_FORM, baseDBForm);
 				if (logDB.isDebugEnabled())
 					logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 						"|"+baseDBForm.getFormName()+"|update|Failure|Error validating the "+baseDBForm.getFormName()+" object|"
 						+form.toString()+"|");
 				return mapping.getInputForward();
 			}			
 			baseDBForm.buildDBObject(request);
 			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(DisplayConstants.MESSAGE_ID, "Update Successful"));
 			saveMessages( request, messages );
 		}
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));			
 			saveErrors( request,errors );
 			if (logDB.isDebugEnabled())
 				logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDBForm.getFormName()+"|update|Failure|Error Updating the "+baseDBForm.getFormName()+" object|"
 					+form.toString()+"|"+ cse.getMessage());
 		}
 		session.setAttribute(DisplayConstants.CURRENT_FORM, baseDBForm);
 
 		if (logDB.isDebugEnabled())
 			logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 				"|"+baseDBForm.getFormName()+"|update|Success|Updating existing "+baseDBForm.getFormName()+" object|"
 				+form.toString()+"|");
 		return (mapping.findForward(ForwardConstants.UPDATE_SUCCESS));
 	}
 	
 	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDBForm baseDBForm = (BaseDBForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDB.isDebugEnabled())
 				logDB.debug("||"+baseDBForm.getFormName()+"|delete|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		UserInfoHelper.setUserInfo(((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId(), session.getId());
 		try
 		{
 			baseDBForm.removeDBObject(request);
 			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(DisplayConstants.MESSAGE_ID, "Delete Successful"));
 			saveMessages( request, messages );
 		}
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));			
 			saveErrors( request,errors );
 			if (logDB.isDebugEnabled())
 				logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDBForm.getFormName()+"|delete|Failure|Error Deleting the "+baseDBForm.getFormName()+" object|"
 					+form.toString()+"|"+ cse.getMessage());
 		}
 		session.setAttribute(DisplayConstants.CURRENT_FORM, baseDBForm);
 		if (logDB.isDebugEnabled())
 			logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 				"|"+baseDBForm.getFormName()+"|delete|Success|Success Deleting "+baseDBForm.getFormName()+" object|"
 				+form.toString()+"|");
 		return (mapping.findForward(ForwardConstants.DELETE_SUCCESS));
 	}
 	
 	public ActionForward search(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDBForm baseDBForm = (BaseDBForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDB.isDebugEnabled())
 				logDB.debug("||"+baseDBForm.getFormName()+"|search|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		UserInfoHelper.setUserInfo(((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId(), session.getId());
 		try
 		{
 			SearchResult searchResult = baseDBForm.searchObjects(request,errors,messages);
 
 			if ( searchResult.getSearchResultObjects() == null || searchResult.getSearchResultObjects().isEmpty())
 			{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, "The search criteria returned zero results"));
 				saveErrors( request,errors );
 				if (logDB.isDebugEnabled())
 					logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 						"|"+baseDBForm.getFormName()+"|search|Failure|No Records found for "+baseDBForm.getFormName()+" object|"
 						+form.toString()+"|");
 				baseDBForm.resetForm();
 				return (mapping.findForward(ForwardConstants.SEARCH_FAILURE));					
 			}
 			if (searchResult.getSearchResultMessage() != null && !(searchResult.getSearchResultMessage().trim().equalsIgnoreCase("")))
 			{
 				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(DisplayConstants.MESSAGE_ID, searchResult.getSearchResultMessage()));
 				saveMessages( request, messages );
 			}
 			
 			if(session.getAttribute(DisplayConstants.SEARCH_RESULT)!=null){
				session.setAttribute(DisplayConstants.ORIGINAL_SEARCH_RESULT, session.getAttribute(DisplayConstants.SEARCH_RESULT) );
 			}
 			session.setAttribute(DisplayConstants.SEARCH_RESULT, searchResult);
 		}
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));			
 			saveErrors( request,errors );
 			if (logDB.isDebugEnabled())
 				logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDBForm.getFormName()+"|search|Failure|Error Searching the "+baseDBForm.getFormName()+" object|"
 					+form.toString()+"|"+ cse.getMessage());
 		}
 		session.setAttribute(DisplayConstants.CURRENT_FORM, baseDBForm);
 		if (logDB.isDebugEnabled())
 			logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 				"|"+baseDBForm.getFormName()+"|search|Success|Success in searching "+baseDBForm.getFormName()+" object|"
 				+form.toString()+"|");
 		return (mapping.findForward(ForwardConstants.SEARCH_SUCCESS));	
 	}
 
 	
 }
