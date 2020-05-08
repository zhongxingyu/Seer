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
 
 package org.infoglue.cms.util.workflow;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.sf.hibernate.HibernateException;
 import net.sf.hibernate.SessionFactory;
 import net.sf.hibernate.cfg.Configuration;
 
 import org.apache.log4j.Logger;
 import org.infoglue.cms.entities.mydesktop.WorkflowActionVO;
 import org.infoglue.cms.entities.mydesktop.WorkflowStepVO;
 import org.infoglue.cms.entities.mydesktop.WorkflowVO;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGluePrincipal;
 
 import com.opensymphony.module.propertyset.PropertySet;
 import com.opensymphony.workflow.AbstractWorkflow;
 import com.opensymphony.workflow.InvalidActionException;
 import com.opensymphony.workflow.WorkflowException;
 import com.opensymphony.workflow.basic.BasicWorkflow;
 import com.opensymphony.workflow.loader.ActionDescriptor;
 import com.opensymphony.workflow.loader.StepDescriptor;
 import com.opensymphony.workflow.loader.WorkflowDescriptor;
 import com.opensymphony.workflow.query.FieldExpression;
 import com.opensymphony.workflow.query.WorkflowExpressionQuery;
 import com.opensymphony.workflow.spi.Step;
 import com.opensymphony.workflow.spi.WorkflowEntry;
 
 /**
  * A facade to OSWorkflow that gives us a place to cache workflow data as we need it while interacting with it.
  * This class has kind of a strange interface due to the idiosyncracies of the OSWorkflow, particularly
  * the Workflow interface.  The idea is to encapsulate the interactions with OSWorkflow and eliminate the
  * need to pass a Workflow reference and the workflow ID all over the place when extracting data from OSWorkflow
  * @author <a href="mailto:jedprentice@gmail.com">Jed Prentice</a>
 * @version $Revision: 1.22 $ $Date: 2005/09/21 10:10:46 $
  */
 public class WorkflowFacade
 {
 	/**
 	 * If the following attribute is specified in the workflow meta attributes, 
 	 * The title will be fetch from the propertyset associated with the workflow, using the meta value as a key.
 	 */
 	private static final String WORKFLOW_TITLE_EXTENSION_META_ATTRIBUTE = "org.infoglue.title";
 	
 	/**
 	 * If the following attribute is specified in the workflow meta attributes,
 	 * then all actions will have access to a DatabaseSession instance controlled by this class. 
 	 */
 	private static final String WORKFLOW_DATABASE_EXTENSION_META_ATTRIBUTE = "org.infoglue.database";
 	
 	
 	private final static Logger logger = Logger.getLogger(WorkflowFacade.class.getName());
 
 	private static SessionFactory hibernateSessionFactory;
 
 	static
 	{
 		try
 		{
 			hibernateSessionFactory = new Configuration().configure().buildSessionFactory();
 		}
 		catch (HibernateException e)
 		{
 			e.printStackTrace();
 			throw new ExceptionInInitializerError(e);
 		}
 	}
 
 	private final AbstractWorkflow workflow;
 	private long workflowId;
 
 	private WorkflowDescriptor workflowDescriptor;
 
 	/**
 	 * Constructs a WorkflowFacade with the given owner.
 	 * 
 	 * @param owner the owner of the workflow.
 	 */
 	public WorkflowFacade(final Owner owner)
 	{
 		workflow = new BasicWorkflow(owner.getIdentifier());
 		workflow.getConfiguration().getPersistenceArgs().put("sessionFactory", hibernateSessionFactory);
 	}
 	
 	/**
 	 * Constructs a WorkflowFacade with the given user principal
 	 * @param userPrincipal an InfoGluePrincipal representing a system user
 	 */
 	public WorkflowFacade(InfoGluePrincipal userPrincipal)
 	{
 		this(OwnerFactory.create(userPrincipal));
 	}
 
 	/**
 	 * Constructs a WorkflowFacade with the given user principal representing an initialized instance of the workflow
 	 * with the given name.  "Initialized" in this context means that the initial action has been executed and we have
 	 * the workflow ID.
 	 * @param userPrincipal an InfoGluePrincipal representing a system user
 	 * @param name the name of the workflow to create
 	 * @param initialAction the ID of the initial action to perform to get the workflow started.
 	 */
 	public WorkflowFacade(InfoGluePrincipal userPrincipal, String name, int initialAction) throws SystemException
 	{
 		this(userPrincipal, name, initialAction, new HashMap());
 	}
 
 	/**
 	 * Constructs a WorkflowFacade with the given user principal representing an initialized instance of the workflow
 	 * with the given name.  "Initialized" in this context means that the initial action has been executed and we have
 	 * the workflow ID.
 	 * @param userPrincipal an InfoGluePrincipal representing a system user
 	 * @param name the name of the workflow to create
 	 * @param initialAction the ID of the initial action to perform to get the workflow started.
 	 * @param inputs a map of inputs to use to initialize the workflow.
 	 */
 	public WorkflowFacade(InfoGluePrincipal userPrincipal, String name, int initialAction, Map inputs) throws SystemException
 	{
 		this(userPrincipal);
 		initialize(name, initialAction, inputs);
 	}
 
 	/**
 	 * Constructs a WorkflowFacade for a user with the given workflow ID.
 	 * @param userPrincipal an InfoGluePrincipal representing a system user
 	 * @param workflowId the ID representing an instance of the desired workflow
 	 */
 	public WorkflowFacade(InfoGluePrincipal userPrincipal, long workflowId)
 	{
 		this(userPrincipal);
 		setWorkflowIdAndDescriptor(workflowId);
 	}
 
 	/**
 	 * Sets the workflow ID to the given value, and caches the associated workflow descriptor
 	 * @param workflowId the desired workflow ID
 	 */
 	private void setWorkflowIdAndDescriptor(long workflowId)
 	{
 		this.workflowId = workflowId;
 		workflowDescriptor = workflow.getWorkflowDescriptor(workflow.getWorkflowName(workflowId));
 	}
 
 	/**
 	 * Returns the workflow ID
 	 * @return the workflow ID
 	 */
 	public long getWorkflowId()
 	{
 		return workflowId;
 	}
 
 	/**
 	 * Initializes the workflow, setting workflowId as a side-effect.
 	 * @param name the name of the workflow to initialize
 	 * @param initialAction the ID of the initial action to perform to get the workflow started.
 	 * @param inputs a map of inputs to use to initialize the workflow.
 	 * @throws SystemException if a workflow error occurs.
 	 */
 	private void initialize(String name, int initialAction, Map inputs) throws SystemException
 	{
 		try
 		{
 			if(useDatabaseExtension(workflow.getWorkflowDescriptor(name)))
 				setWorkflowIdAndDescriptor(doExtendedInitialize(name, initialAction, inputs));
 			else
 				setWorkflowIdAndDescriptor(workflow.initialize(name, initialAction, inputs));
 		}
 		catch (Exception e)
 		{
 			throw new SystemException(e);
 		}
 	}
 
 	/**
 	 * Initializes the workflow.  
 	 * A <code>DatabaseSession</code> object whose lifecycle is handled by this method is inserted into the <code>inputs</code>.
 	 * 
 	 * @param name the name of the workflow to initialize
 	 * @param initialAction the ID of the initial action to perform to get the workflow started.
 	 * @param inputs a map of inputs to use to initialize the workflow.
 	 * @throws SystemException if a workflow error occurs.
 	 */
 	private long doExtendedInitialize(final String name, final int initialAction, final Map inputs) throws WorkflowException
 	{
 		long result = 0;
 		final DatabaseSession db = new DatabaseSession();
 		try
 		{
 			final Map copy = new HashMap();
 			copy.putAll(inputs);
 			copy.put(workflow.getWorkflowDescriptor(name).getMetaAttributes().get(WORKFLOW_DATABASE_EXTENSION_META_ATTRIBUTE), db);
 			result = workflow.initialize(name, initialAction, copy);
 		} 
 		catch(Exception e) 
 		{
 			e.printStackTrace();
 			if(db != null)
 			{
 				db.setRollbackOnly();
 			}
 		} 
 		finally 
 		{
 			db.releaseDB();
 		}
 		return result;
 	}
 	
 	/**
 	 * Performs an action using the given inputs
 	 * @param actionId the ID of the action to perform
 	 * @param inputs a map of inputs to the action
 	 * @throws WorkflowException if a workflow error occurs, or if the underlying workflow is not active
 	 */
 	public void doAction(int actionId, Map inputs) throws WorkflowException
 	{
 		if (!isActive())
 		{
 			throw new InvalidActionException("Workflow " + workflowId + " is no longer active");
 		}
 
 		if(useDatabaseExtension(workflowDescriptor))
 		{
 			doExtendedAction(actionId, inputs);
 		}
 		else
 		{
 			workflow.doAction(workflowId, actionId, inputs);
 		}
 	}
 
 	/**
 	 * Performs an action using the given inputs.
 	 * A <code>DatabaseSession</code> object whose lifecycle is handled by this method is inserted into the <code>inputs</code>.
 	 * 
 	 * @param actionId the ID of the action to perform
 	 * @param inputs a map of inputs to the action
 	 * @throws WorkflowException if a workflow error occurs, or if the underlying workflow is not active
 	 */
 	private void doExtendedAction(final int actionId, final Map inputs) throws WorkflowException
 	{
 		final DatabaseSession db = new DatabaseSession();
 		try 
 		{
 			final Map copy = new HashMap();
 			copy.putAll(inputs);
 			copy.put(workflowDescriptor.getMetaAttributes().get(WORKFLOW_DATABASE_EXTENSION_META_ATTRIBUTE), db);
 			workflow.doAction(workflowId, actionId, copy);
 		} 
 		catch(Exception e) 
 		{
 			e.printStackTrace();
 			if(db != null)
 				db.setRollbackOnly();
 		} 
 		finally 
 		{
 			db.releaseDB();
 		}
 	}
 
 	/**
 	 * Returns the property set associated with the underlying workflow
 	 * @return the property set associated with the underlying workflow
 	 */
 	public PropertySet getPropertySet()
 	{
 		return workflow.getPropertySet(workflowId);
 	}
 
 	/**
 	 * Returns the state of the underlying workflow entry
 	 * @return the state of the underlying workflow entry
 	 */
 	private int getEntryState()
 	{
 		return workflow.getEntryState(workflowId);
 	}
 
 	/**
 	 * Indicates whether the underlying workflow is active.
 	 * 
 	 * @return true if the underlying workflow's state is WorkflowEntry.ACTIVATED, otherwise returns false.
 	 */
 	public boolean isActive()
 	{
 		return getEntryState() == WorkflowEntry.ACTIVATED;
 	}
 
 	/**
 	 * Indicates whether the underlying workflow is finished.
 	 * 
 	 * @return true if the underlying workflow's state is WorkflowEntry.KILLED or WorkflowEntry.COMPLETED, otherwise returns false.
 	 */
 	public boolean isFinished()
 	{
 		int state = getEntryState();
 		return state == WorkflowEntry.KILLED || state == WorkflowEntry.COMPLETED;
 	}
 	
 	/**
 	 * Returns a list of all declared workflows, i.e., workflows defined in workflows.xml
 	 * @return a list WorkflowVOs representing all declared workflows
 	 */
 	public List getDeclaredWorkflows()
 	{
 		String[] workflowNames = workflow.getWorkflowNames();
 		List availableWorkflows = new ArrayList();
 
 		for (int i = 0; i < workflowNames.length; i++)
 			availableWorkflows.add(createWorkflowVO(workflowNames[i]));
 
 		return availableWorkflows;
 	}
 
 	/**
 	 * Returns a list of all active workflows.
 	 * 
 	 * @return a list of WorkflowVOs representing all active workflows
 	 * @throws SystemException if an error occurs finding the active workflows
 	 */
 	public List getActiveWorkflows() throws SystemException
 	{
 		List workflowVOs = new ArrayList();
 
 		for (Iterator workflows = findActiveWorkflows().iterator(); workflows.hasNext();)
 		{
 			setWorkflowIdAndDescriptor(((Long)workflows.next()).longValue());
 			logger.info("workflowId:" + workflowId);
 			workflowVOs.add(createWorkflowVO());
 		}
 
 		return workflowVOs;
 	}
 	
 	/**
 	 * Returns a list of workflows owned by the specified principal. If the principal is
 	 * an administrator, all active workflows are returned.
 	 * 
 	 * @param principal the principal.
 	 * @return the workflows owned by the specified principal.
 	 */
 	public List getMyActiveWorkflows(final InfoGluePrincipal principal) throws SystemException
 	{
 		if(principal.getIsAdministrator())
 		{
 			return getActiveWorkflows();
 		}
 		final Set workflowVOs = new HashSet();
 		for(final Iterator owners = OwnerFactory.createAll(principal).iterator(); owners.hasNext(); )
 		{
 			final Owner owner = (Owner) owners.next();
 			workflowVOs.addAll(createWorkflowsForOwner(owner));
 		}
 		return new ArrayList(workflowVOs);
 	}
 	
 	/**
 	 * Creates value object for all workflows having the specified owner.
 	 * 
 	 * @param owner the owner.
 	 * @return the value objects.
 	 * @throws SystemException if an error occurs when creating the value objects.
 	 */
 	private final Set createWorkflowsForOwner(final Owner owner) throws SystemException
 	{
 		final Set workflowVOs = new HashSet(); 
 		for (Iterator workflows = findWorkflows(owner).iterator(); workflows.hasNext();)
 		{
 			setWorkflowIdAndDescriptor(((Long)workflows.next()).longValue());
 			workflowVOs.add(createWorkflowVO());
 		}
 		return workflowVOs;
 	}
 	
 	/**
 	 * Finds all active workflows
 	 * @return A list of workflowIds representing workflows that match the hard-wored query expression.
 	 * @throws SystemException if a workflow error occurs during the search
 	 */
 	private List findActiveWorkflows() throws SystemException
 	{
 		try
 		{
 			return workflow.query(new WorkflowExpressionQuery(new FieldExpression(FieldExpression.STATE,
 						FieldExpression.ENTRY, FieldExpression.EQUALS, new Integer(WorkflowEntry.ACTIVATED))));
 		}
 		catch (WorkflowException e)
 		{
 			throw new SystemException(e);
 		}
 	}
 
 	/**
 	 * Finds all workflows for the specified owner.
 	 * 
 	 * @param owner the owner.
 	 * @return The active workflows owned by the specified owner. 
 	 * @throws SystemException
 	 */
 	private List findWorkflows(final Owner owner) throws SystemException
 	{
 		try
 		{
 			return workflow.query(new WorkflowExpressionQuery(new FieldExpression(FieldExpression.OWNER,
 						FieldExpression.CURRENT_STEPS, FieldExpression.EQUALS, owner.getIdentifier())));
 		}
 		catch (WorkflowException e)
 		{
 			throw new SystemException(e);
 		}
 	}
 	
 	/**
 	 * Returns all current steps for the workflow, i.e., steps that could be performed in the workflow's current state
 	 * Steps are filtered according to ownership; if a step has an owner, it is only included if the ownser matches
 	 * the caller or if the current user is an administrator.
 	 * @return a list of WorkflowStepVOs representing the current steps of the workflow with workflowId
 	 */
 	public List getCurrentSteps()
 	{
 		return getCurrentSteps(null);
 	}
 	
 	public List getCurrentSteps(final WorkflowVO workflowVO)
 	{
 		return createStepVOs(workflowVO, workflow.getCurrentSteps(workflowId));
 	}
 
 	/**
 	 * Returns all history steps for the workflow, i.e., all the steps that have already been performed.
 	 * @return a list of WorkflowStepVOs representing all history steps for the workflow with workflowId
 	 */
 	public List getHistorySteps()
 	{
 		return getHistorySteps(null);
 	}
 	
 	public List getHistorySteps(final WorkflowVO workflowVO)
 	{
 		return createStepVOs(workflowVO, workflow.getHistorySteps(workflowId));
 	}
 
 	/**
 	 * Returns all steps for a workflow definition.  These are the steps declared in the workfow descriptor; there is
 	 * no knowledge of current or history steps at this point.
 	 * @return a list of WorkflowStepVOs representing all steps in the workflow.
 	 */
 	public List getDeclaredSteps()
 	{
 		return getDeclaredSteps(workflowDescriptor);
 	}
 
 	/**
 	 * Creates a list of WorkflowStepVOs from the given list of steps
 	 * @param steps a list of Steps
 	 * @return a list of WorkflowStepVOs corresponding to all steps that pass the filter
 	 */
 	private List createStepVOs(final WorkflowVO workflowVO, final List steps)
 	{
 		List stepVOs = new ArrayList();
 		for (Iterator i = steps.iterator(); i.hasNext();)
 			stepVOs.add(createStepVO(workflowVO, (Step)i.next()));
 
 		return stepVOs;
 	}
 
 	/**
 	 * Returns all steps for a workflow definition.  These are the steps declared in the workfow descriptor; there is
 	 * no knowledge of current or history steps at this point.
 	 * @param descriptor a workflow descriptor from which to get current steps
 	 * @return a list of WorkflowStepVOs representing all steps in the workflow.
 	 */
 	private List getDeclaredSteps(WorkflowDescriptor descriptor)
 	{
 		List steps = new ArrayList();
 		for (Iterator i = descriptor.getSteps().iterator(); i.hasNext();)
 			steps.add(createStepVO((StepDescriptor)i.next()));
 
 		return steps;
 	}
 
 	/**
 	 * Returns a list of initial actions for the workflow
 	 * @return a list of WorkflowActionVOs representing the global actions for the workflow with workflowId
 	 */
 	private List getInitialActions()
 	{
 		return createActionVOs(workflowDescriptor.getInitialActions());
 	}
 
 	/**
 	 * Returns a list of global actions for the workflow
 	 * @return a list of WorkflowActionVOs representing the global actions for the workflow with workflowId
 	 */
 	private List getGlobalActions()
 	{
 		return createActionVOs(workflowDescriptor.getGlobalActions());
 	}
 
 	/**
 	 * Creates a list of WorkflowActionVOs from a list of action descriptors
 	 * @param actionDescriptors a list of ActionDescriptors
 	 * @return a list of WorkflowActionVOs representing actionDescriptors
 	 */
 	private List createActionVOs(List actionDescriptors)
 	{
 		List actions = new ArrayList();
 		for (Iterator i = actionDescriptors.iterator(); i.hasNext();)
 			actions.add(createActionVO((ActionDescriptor)i.next()));
 
 		return actions;
 
 	}
 
 	/**
 	 * Creates a new WorkflowVO.  This represents a pretty complete workflow; you get all the current steps, history
 	 * steps, available actions, and global actions.
 	 * @return a WorkflowVO representing workflow, with workflowId
 	 */
 	public WorkflowVO createWorkflowVO()
 	{
 		WorkflowVO workflowVO = new WorkflowVO(new Long(workflowId), workflow.getWorkflowName(workflowId));
 		if(useTitleExtension(workflowDescriptor))
 			workflowVO.setTitle(getWorkflowTitle());
 		workflowVO.setCurrentSteps(getCurrentSteps(workflowVO));
 		workflowVO.setHistorySteps(getHistorySteps(workflowVO));
 		workflowVO.setInitialActions(getInitialActions());
 		workflowVO.setGlobalActions(getGlobalActions());
 
 		return workflowVO;
 	}
 
 	/**
 	 * Returns the title of the workflow instance.
 	 * 
 	 * @return the title of the workflow instance.
 	 */
	private String getWorkflowTitle() {
 		if(!workflowDescriptor.getMetaAttributes().containsKey(WORKFLOW_TITLE_EXTENSION_META_ATTRIBUTE))
 		{
 			return null;
 		}
 		
 		final String key = (String) workflowDescriptor.getMetaAttributes().get(WORKFLOW_TITLE_EXTENSION_META_ATTRIBUTE);
 		final PropertySet ps = getPropertySet();
 		return ps.exists(key) ? ps.getString(key) : null;
 	}
 	
 	/**
 	 * Creates a new WorkflowVO from workflow with the given name.  The resulting workflow VO contains only a
 	 * minimal amount of data because we don't have the workflow ID.  Basically all you get is all the steps.
 	 * @param name the name of the desired workflow
 	 * @return a new WorkflowVO representing workflow
 	 */
 	private WorkflowVO createWorkflowVO(String name)
 	{
 		WorkflowVO workflowVO = new WorkflowVO(null, name);
 		workflowVO.setDeclaredSteps(getDeclaredSteps(workflow.getWorkflowDescriptor(name)));
 		return workflowVO;
 	}
 
 	/**
 	 * Creates a WorkflowStepVO from the given step
 	 * @param step the desired step
 	 * @return a new WorkflowStepVO representing step.
 	 */
 	private WorkflowStepVO createStepVO(final WorkflowVO workflowVO, final Step step)
 	{
 		logger.info("step:" + step + ':' + step.getId());
 		logger.info("Owner:" + step.getOwner());
 
 		WorkflowStepVO stepVO = new WorkflowStepVO(workflowVO);
 		stepVO.setId(new Integer((int)step.getId()));// Hope it doesn't get too big; we are stuck with int thanks to BaseEntityVO
 		stepVO.setStepId(new Integer(step.getStepId()));
 		stepVO.setWorkflowId(new Long(workflowId));
 		stepVO.setStatus(step.getStatus());
 		stepVO.setStartDate(step.getStartDate());
 		stepVO.setFinishDate(step.getFinishDate());
 		stepVO.setOwner(step.getOwner());
 		stepVO.setCaller(step.getCaller());
 
 		StepDescriptor stepDescriptor = workflowDescriptor.getStep(step.getStepId());
 		stepVO.setName(stepDescriptor.getName());
 		for (Iterator i = stepDescriptor.getActions().iterator(); i.hasNext();)
 			stepVO.addAction(createActionVO((ActionDescriptor)i.next()));
 
 		return stepVO;
 	}
 
 	/**
 	 * Creates a WorkflowStepVO from a step descriptor.  Some of the step data, e.g., status, startDate,
 	 * finishDate, etc. cannot be populated here because the step descriptor does not know about these things.
 	 * @param stepDescriptor a step descriptor
 	 * @return a WorkflowStepVO representing stepDescriptor
 	 */
 	private WorkflowStepVO createStepVO(StepDescriptor stepDescriptor)
 	{
 		WorkflowStepVO step = new WorkflowStepVO();
 		step.setStepId(new Integer(stepDescriptor.getId()));
 		step.setName(stepDescriptor.getName());
 		step.setStatus("Not started");
 
 		for (Iterator i = stepDescriptor.getActions().iterator(); i.hasNext();)
 			step.addAction(createActionVO((ActionDescriptor)i.next()));
 
 		return step;
 	}
 
 	/**
 	 * Creates a WorkflowActionVO for the given action descriptor
 	 * @param actionDescriptor an action descriptor
 	 * @return a WorkflowActionVO representing actionDescriptor
 	 */
 	private WorkflowActionVO createActionVO(ActionDescriptor actionDescriptor)
 	{
 		logger.info("Action:" + actionDescriptor.getId() + ':' + actionDescriptor.getName()
 					+ ':' + actionDescriptor.getParent().getClass());
 
 		WorkflowActionVO actionVO = new WorkflowActionVO(new Integer(actionDescriptor.getId()));
 		actionVO.setWorkflowId(new Long(workflowId));
 		actionVO.setName(actionDescriptor.getName());
 		actionVO.setView(actionDescriptor.getView());
 		actionVO.setAutoExecute(actionDescriptor.getAutoExecute());
 		actionVO.setMetaAttributes(actionDescriptor.getMetaAttributes());
 		return actionVO;
 	}
 	
 	/**
 	 * Checks if the title extension should be used.
 	 * 
 	 * @return true if the extension should be used, false otherwise.
 	 */
	private boolean useTitleExtension(final WorkflowDescriptor descriptor) {
 		return descriptor.getMetaAttributes().containsKey(WORKFLOW_TITLE_EXTENSION_META_ATTRIBUTE);
 	}
 
 	/**
 	 * Checks if the database extension should be used.
 	 * 
 	 * @return true if the extension should be used, false otherwise.
 	 */
	private boolean useDatabaseExtension(final WorkflowDescriptor descriptor) {
 		return descriptor.getMetaAttributes().containsKey(WORKFLOW_DATABASE_EXTENSION_META_ATTRIBUTE);
 	}	
 }
