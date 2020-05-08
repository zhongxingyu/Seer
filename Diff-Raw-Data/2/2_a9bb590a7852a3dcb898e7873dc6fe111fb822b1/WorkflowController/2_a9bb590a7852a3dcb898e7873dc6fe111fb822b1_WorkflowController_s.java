 /* ===============================================================================
 /* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
 
 package org.infoglue.cms.controllers.kernel.impl.simple;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.entities.kernel.BaseEntityVO;
 import org.infoglue.cms.entities.mydesktop.WorkflowActionVO;
 import org.infoglue.cms.entities.mydesktop.WorkflowVO;
 import org.infoglue.cms.exception.Bug;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.workflow.WorkflowFacade;
 
 import com.opensymphony.module.propertyset.PropertySet;
 import com.opensymphony.workflow.WorkflowException;
 
 /**
  * This controller acts as the api towards the OSWorkflow Workflow-engine.
  * @author Mattias Bogeblad
  * @author <a href="mailto:jedprentice@gmail.com">Jed Prentice</a>
  */
 public class WorkflowController extends BaseController
 {
 	private static final WorkflowController controller = new WorkflowController();
 
 	/**
 	 * Returns the WorkflowController singleton
 	 * @return a reference to a WorkflowController
 	 */
 	public static WorkflowController getController()
 	{
 		return controller;
 	}
 
 	private WorkflowController() {}
 
 	/**
 	 * TODO: move; used by tests + CreateWorkflowInstanceAction 
 	 */
 	public static Map createWorkflowParameters(final HttpServletRequest request)
 	{
 		final Map parameters = new HashMap();
 		parameters.putAll(request.getParameterMap());
 		parameters.put("request", request);
 		return parameters;
 	}
 
 	/**
 	 * @param principal the user principal representing the desired user
 	 * @param name the name of the workflow to create.
 	 * @param actionId the ID of the initial action
 	 * @param inputs the inputs to the workflow
 	 * @return a WorkflowVO representing the newly created workflow instance
 	 * @throws SystemException if an error occurs while initiaizing the workflow
 	 */
 	public WorkflowVO initializeWorkflow(InfoGluePrincipal principal, String name, int actionId, Map inputs) throws SystemException
 	{
 		try
 		{
 			if(getIsAccessApproved(name, principal))
 			{
 				return new WorkflowFacade(principal, name, actionId, inputs).createWorkflowVO();
 			}
 			else
 			{
 				throw new Bug("You are not allowed to create " + name + " workflows.");
 			}
 		}
 		catch (Exception e)
 		{
 			throw new SystemException(e);
 		}
 	}
 
 	/**
 	 * Returns a list of all available workflows, i.e., workflows defined in workflows.xml
 	 * @param userPrincipal a user principal
 	 * @return a list WorkflowVOs representing available workflows
 	 */
 	public List getAvailableWorkflowVOList(InfoGluePrincipal userPrincipal) throws SystemException
 	{
 		final List allWorkflows = new WorkflowFacade(userPrincipal).getDeclaredWorkflows();
 		final List accessibleWorkflows = new ArrayList();
 		for(final Iterator i = allWorkflows.iterator(); i.hasNext(); )
 		{
 			final WorkflowVO workflowVO = (WorkflowVO) i.next();
 			if(getIsAccessApproved(workflowVO.getName(), userPrincipal))
 			{
 				accessibleWorkflows.add(workflowVO);
 			}
 		}
 		
 		return accessibleWorkflows;
 	}
 
 	/**
 	 * This method returns true if the user should have access to the contentTypeDefinition sent in.
 	 */
     
 	public boolean getIsAccessApproved(String workflowName, InfoGluePrincipal infoGluePrincipal) throws SystemException
 	{
 	    final String protectWorkflows = CmsPropertyHandler.getProperty("protectWorkflows");
	    if(protectWorkflows == null && !protectWorkflows.equalsIgnoreCase("true"))
 	    {
 	    	return true;
 	    }
 	    	
 		getLogger().info("getIsAccessApproved for " + workflowName + " AND " + infoGluePrincipal);
 		boolean hasAccess = false;
     	
 		Database db = CastorDatabaseService.getDatabase();
 		beginTransaction(db);
 
 		try
 		{ 
 			hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Workflow.Create", workflowName);
 			commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			getLogger().error("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
     
 		return hasAccess;
 	}
 
 	/**
 	 * Returns current workflows, i.e., workflows that are active.
 	 * @param userPrincipal a user principal
 	 * @return a list of WorkflowVOs representing all active workflows
 	 * @throws SystemException if an error occurs while finding the current workflows
 	 */
 	public List getCurrentWorkflowVOList(InfoGluePrincipal userPrincipal) throws SystemException
 	{
 		return new WorkflowFacade(userPrincipal).getActiveWorkflows();
 	}
 	
 	/**
 	 * Returns the workflows owned by the specified principal.
 	 * 
 	 * @param userPrincipal a user principal.
 	 * @return a list of WorkflowVOs owned by the principal.
 	 * @throws SystemException if an error occurs while finding the workflows
 	 */
 	public List getMyCurrentWorkflowVOList(InfoGluePrincipal userPrincipal) throws SystemException
 	{
 		return new WorkflowFacade(userPrincipal).getMyActiveWorkflows(userPrincipal);
 	}
 	
 
 	/**
 	 * Invokes an action on a workflow for a given user and request
 	 * @param principal the user principal
 	 * @param workflowId the ID of the desired workflow
 	 * @param actionId the ID of the desired action
 	 * @param inputs the inputs to the workflow 
 	 * @return a WorkflowVO representing the current state of the workflow identified by workflowId
 	 * @throws WorkflowException if a workflow error occurs
 	 */
 	public WorkflowVO invokeAction(InfoGluePrincipal principal, long workflowId, int actionId, Map inputs) throws WorkflowException
 	{
 		WorkflowFacade workflow = new WorkflowFacade(principal, workflowId);
 		workflow.doAction(actionId, inputs);
 		return workflow.createWorkflowVO();
 	}
 
 	/**
 	 * Returns the workflow property set for a particular user and workflow
 	 * @return the workflow property set for the workflow with workflowId and the user represented by userPrincipal
 	 */
 	public PropertySet getPropertySet(InfoGluePrincipal userPrincipal, long workflowId)
 	{
 		return new WorkflowFacade(userPrincipal, workflowId).getPropertySet();
 	}
 
 	/**
 	 * Returns the contents of the PropertySet for a particular workflow
 	 * @param userPrincipal a user principal
 	 * @param workflowId the ID of the desired workflow
 	 * @return a map containing the contents of the workflow property set
 	 */
 	public Map getProperties(InfoGluePrincipal userPrincipal, long workflowId)
 	{
 		getLogger().info("userPrincipal:" + userPrincipal);
 		getLogger().info("workflowId:" + workflowId);
 
 		PropertySet propertySet = getPropertySet(userPrincipal, workflowId);
 		Map parameters = new HashMap();
 		for (Iterator keys = getPropertySet(userPrincipal, workflowId).getKeys().iterator(); keys.hasNext();)
 		{
 			String key = (String)keys.next();
 			parameters.put(key, propertySet.getString(key));
 		}
 
 		return parameters;
 	}
 
 	/**
 	 * Returns all history steps for a workflow, i.e., all the steps that have already been performed.
 	 * @param userPrincipal a user principal
 	 * @param workflowId the ID of the desired workflow
 	 * @return a list of WorkflowStepVOs representing all history steps for the workflow with workflowId
 	 */
 	public List getHistorySteps(InfoGluePrincipal userPrincipal, long workflowId)
 	{
 		return new WorkflowFacade(userPrincipal, workflowId).getHistorySteps();
 	}
 
 	/**
 	 * Returns all current steps for a workflow, i.e., steps that could be performed in the workflow's current state
 	 * @param userPrincipal a user principal
 	 * @param workflowId the Id of the desired workflow
 	 * @return a list of WorkflowStepVOs representing the current steps of the workflow with workflowId
 	 */
 	public List getCurrentSteps(InfoGluePrincipal userPrincipal, long workflowId)
 	{
 		return new WorkflowFacade(userPrincipal, workflowId).getCurrentSteps();
 	}
 
 	/**
 	 * Returns all steps for a workflow definition.  These are the steps declared in the workfow descriptor; there is
 	 * no knowledge of current or history steps at this point.
 	 * @param userPrincipal an InfoGluePrincipal representing a system user
 	 * @param workflowId a workflowId
 	 * @return a list of WorkflowStepVOs representing all steps in the workflow.
 	 */
 	public List getAllSteps(InfoGluePrincipal userPrincipal, long workflowId)
 	{
 		return new WorkflowFacade(userPrincipal, workflowId).getDeclaredSteps();
 	}
 
 	/**
 	 * Returns a new WorkflowActionVO.  This method is apparently unused, but is required by BaseController.  We don't
 	 * use it internally because it requires a cast; it is simpler to just use <code>new</code> to create an instance.
 	 * @return a new WorkflowActionVO.
 	 */
 	public BaseEntityVO getNewVO()
 	{
 		return new WorkflowActionVO();
 	}
 }
