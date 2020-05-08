 package fhdw.ipscrum.shared.model.metamodel.ticketsAndTypes;
 
 import java.io.File;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import fhdw.ipscrum.client.IDGenerator;
 import fhdw.ipscrum.server.ServerContext;
 import fhdw.ipscrum.server.TestUtils;
 import fhdw.ipscrum.shared.exceptions.IPScrumGeneralException;
 import fhdw.ipscrum.shared.exceptions.model.ConsistencyException;
 import fhdw.ipscrum.shared.exceptions.model.DoubleDefinitionException;
 import fhdw.ipscrum.shared.model.Model;
 import fhdw.ipscrum.shared.model.metamodel.fields.Field;
 import fhdw.ipscrum.shared.model.metamodel.fields.FieldType;
 import fhdw.ipscrum.shared.model.metamodel.fields.PersonFieldType;
 import fhdw.ipscrum.shared.model.metamodel.fields.SingleField;
 import fhdw.ipscrum.shared.model.metamodel.fields.TextFieldType;
 import fhdw.ipscrum.shared.model.metamodel.states.StateType;
 import fhdw.ipscrum.shared.model.metamodel.states.TransitionRule;
 import fhdw.ipscrum.shared.model.nonMeta.Bug;
 import fhdw.ipscrum.shared.model.nonMeta.Feature;
 import fhdw.ipscrum.shared.model.nonMeta.Project;
 import fhdw.ipscrum.shared.model.nonMeta.Release;
 import fhdw.ipscrum.shared.model.nonMeta.Sprint;
 import fhdw.ipscrum.shared.model.nonMeta.SprintBacklog;
 import fhdw.ipscrum.shared.model.nonMeta.Task;
 import fhdw.ipscrum.shared.model.nonMeta.Team;
 
 /**
  * Also covers methods of state profile!
  * 
  */
 public class TicketTypeTest {
 
 	/**
 	 * represents the model needed to use the IPScrum.
 	 */
 	private Model modelClient;
 	/**
 	 * represents the server context needed to use the IPScrum.
 	 */
 	private ServerContext serverContext;
 	/**
 	 * represents the type manager needed to use the IPScrum.
 	 */
 	private TypeManager typeManager;
 	/**
 	 * represents the standard feature ticketType needed to use the IPScrum.
 	 */
 	private FeatureTicketType theStandardFeature;
 	/**
 	 * represents the standard bug ticketType needed to use the IPScrum.
 	 */
 	private BugTicketType theStandardBug;
 	/**
 	 * represents the standard task ticketType needed to use the IPScrum.
 	 */
 	private TaskTicketType theStandardTask;
 	/**
 	 * represents a state type to test.
 	 */
 	private StateType anyUserDefinedStateType;
 	/**
 	 * represents a field type to test.
 	 */
 	private FieldType anyUserDefinedFieldType;
 	/**
 	 * represents a project needed to test the ticketTypes.
 	 */
 	private Project testProject;
 	/**
 	 * represents a sprint needed to test the ticketType.
 	 */
 	private Sprint testSprint;
 	/**
 	 * represents the sprintBacklog needed to test the taskTickettype.
 	 */
 	private SprintBacklog testSprintBacklog;
 	/**
 	 * represents the release needed to test the bugTicketType.
 	 */
 	private Release r;
 	/**
 	 * represents a team needed to test the ticketTypes.
 	 */
 	private Team team;
 
 	/**
 	 * sets up the data before the whole class.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 
 	}
 
 	/**
 	 * sets up the data before every test.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Before
 	public void setUp() throws Exception {
 		TestUtils.deleteFolderContent(new File("output"));
 
 		this.serverContext = ServerContext.getInstance();
 
 		this.modelClient = this.serverContext.getPersistenceManager().getModelForTesting();
 		this.modelClient.setUuidManager(new IDGenerator());
 		this.modelClient = this.serverContext.getPersistenceManager().getModelForTesting();
 		this.modelClient.setUuidManager(new IDGenerator());
 		this.typeManager = this.modelClient.getTypeManager();
 		this.theStandardFeature = this.typeManager.getStandardFeatureType();
 		this.theStandardBug = this.typeManager.getStandardBugType();
 		this.theStandardTask = this.typeManager.getStandardTaskType();
 		this.anyUserDefinedFieldType = new PersonFieldType(this.modelClient, "Der BOSS!", this.typeManager.getOne());
 		this.anyUserDefinedStateType =
 				new StateType(this.modelClient, "Am Rande der Galaxie", "Am Rande der Galaxie [...]");
 		this.testProject = new Project(this.modelClient, "Das tolle Projekt");
 		this.team = new Team(this.modelClient, "Testteam");
 		this.team.addProject(this.testProject);
 		this.testSprint =
 				new Sprint(this.modelClient, "Sprint1", "Der tolle Sprint", new Date(), new Date(), this.team,
 						this.testProject);
 		this.r = new Release(this.modelClient, "2.0", new Date(), this.testProject);
 		this.testSprintBacklog = this.testSprint.getSprintBacklog();
 	}
 
 	/**
 	 * test if all the needed managers are ready.
 	 */
 	@Test
 	public void test01() {
 		Assert.assertTrue(this.typeManager != null);
 		Assert.assertTrue(this.theStandardBug != null);
 		Assert.assertTrue(this.theStandardFeature != null);
 		Assert.assertTrue(this.theStandardTask != null);
 	}
 
 	/**
 	 * test create a feature ticket type.
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test
 	public void testcreateFeatureTicketType() throws IPScrumGeneralException {
 		final String nameOfType = "ExtendedTicketType";
 		final String descriptionOfType = "Description of extended Ticket type";
 
 		final FeatureTicketType newTicketType = new FeatureTicketType(this.modelClient, nameOfType, descriptionOfType);
 		Assert.assertTrue(newTicketType != null);
 
 	}
 
 	/**
 	 * A user defined ticket type with the name "Feature" must not be possible!
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = DoubleDefinitionException.class)
 	public void testfeatureTypeDoubleDefinition() throws IPScrumGeneralException {
 		final String name = "Feature";
 		new FeatureTicketType(this.modelClient, name, "");
 	}
 
 	/**
 	 * A user defined ticket type with the name "Bug" must not be possible!
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = DoubleDefinitionException.class)
 	public void testbugDoubleDefinition() throws IPScrumGeneralException {
 		final String name = "Bug";
 		new BugTicketType(this.modelClient, name, "");
 	}
 
 	/**
 	 * A user defined ticket type with the name "Task" must not be possible!
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = DoubleDefinitionException.class)
 	public void testtaskDoubleDefinition() throws IPScrumGeneralException {
 		final String name = "Task";
 		new TaskTicketType(this.modelClient, name, "");
 	}
 
 	/**
 	 * creating a user defined ticket type with an unique name must be possible! using this name twice must not be
 	 * possible!
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = DoubleDefinitionException.class)
 	public void testsomeOtherDoubleDefinition() throws IPScrumGeneralException {
 		final String name = "Extended Task";
 		new TaskTicketType(this.modelClient, name, "");
 		new TaskTicketType(this.modelClient, name, "");
 	}
 
 	/**
 	 * try to add a state to the standard feature.
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = ConsistencyException.class)
 	public void testaddStateStandardFeature() throws IPScrumGeneralException {
 		this.theStandardFeature.addPossibleState(this.anyUserDefinedStateType);
 	}
 
 	/**
 	 * try to remove a state from the standard feature.
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = ConsistencyException.class)
 	public void testremoveStateStandardFeature() throws IPScrumGeneralException {
 		this.theStandardFeature.removePossibleState(this.typeManager.getClosed());
 	}
 
 	/**
 	 * try to add a transition rule to the standard feature.
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = ConsistencyException.class)
 	public void testaddTransitionRuleStandardFeature() throws IPScrumGeneralException {
 		this.theStandardFeature.addTransitionRule(new TransitionRule(this.modelClient, this.typeManager.getClosed(),
 				this.typeManager.getOpen()));
 	}
 
 	/**
 	 * try to remove a transitionRule from standard feature.
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = ConsistencyException.class)
 	public void testremoveTransitionRuleStandardFeature() throws IPScrumGeneralException {
 		this.theStandardFeature.removeTransitionRule(this.theStandardFeature.getStateProfile().getTransitionRules()
 				.iterator().next());
 	}
 
 	/**
 	 * try to change a field accessibility.
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = ConsistencyException.class)
 	public void testchangeAccessibilityStandardFeature1() throws IPScrumGeneralException {
 		this.theStandardFeature.setActive(this.typeManager.getOpen(), this.typeManager.getManDayCostsType());
 	}
 
 	/**
 	 * try to change a field accessibility.
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = ConsistencyException.class)
 	public void testchangeAccessibilityStandardFeature2() throws IPScrumGeneralException {
 		this.theStandardFeature.setNonActive(this.typeManager.getOpen(), this.typeManager.getManDayCostsType());
 	}
 
 	/**
 	 * try to add a field type to the standard feature.
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = ConsistencyException.class)
 	public void testaddFieldTypeStandardFeature() throws IPScrumGeneralException {
 		this.theStandardFeature.addFieldType(this.anyUserDefinedFieldType);
 	}
 
 	/**
 	 * try to remove a field type from the standard feature.
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = ConsistencyException.class)
 	public void testremoveFieldTypeStandardFeature() throws IPScrumGeneralException {
 		this.theStandardFeature.removeFieldType(this.typeManager.getSprintType());
 	}
 
 	/**
 	 * test modify a task ticket type without having instances.
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test
 	public void testmodifyTaskTypeWithoutInstances() throws IPScrumGeneralException {
 		// create user task type
 		final String uniqueName = "Aktivität";
 		new TaskTicketType(this.modelClient, uniqueName, "Task auf Deutsch;-)");
 		// make some changes
 		this.typeManager.getActiveTicketTypeByUniqueName(uniqueName).addFieldType(this.anyUserDefinedFieldType);
 		// test
 		Assert.assertTrue(this.typeManager.getActiveTicketTypeByUniqueName(uniqueName).getAllFieldTypes()
 				.contains(this.anyUserDefinedFieldType));
 		Assert.assertTrue(this.typeManager.getActiveTicketTypeByUniqueName(uniqueName).getSupersededTypes().isEmpty());
 	}
 
 	/**
 	 * test modify a task ticket type with having instances.
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if the use of one of the methods fails
 	 */
 	@Test
 	public void testmodifyTaskTypeWithInstances() throws IPScrumGeneralException {
 		// create user task type
 		final String uniqueName = "Aktivität";
 		new TaskTicketType(this.modelClient, uniqueName, "Task auf Deutsch;-)");
 		// create 1st instance
 		final Task task1 =
 				new Task(this.modelClient,
 						(TaskTicketType) this.typeManager.getActiveTicketTypeByUniqueName(uniqueName), "Aktivität 1",
 						" Hahaha", this.testSprintBacklog);
 		// make some changes -> new version of task type
 		this.typeManager.getActiveTicketTypeByUniqueName(uniqueName).addFieldType(this.anyUserDefinedFieldType);
 		// create 2nd instance
 		final Task task2 =
 				new Task(this.modelClient,
 						(TaskTicketType) this.typeManager.getActiveTicketTypeByUniqueName(uniqueName), "Aktivität 2",
 						" Hahaha", this.testSprintBacklog);
 		// test
 		Assert.assertFalse(task1.getTicketType().equals(task2.getTicketType()));
 		Assert.assertTrue(task2.getTicketType().getSupersededTypes().contains(task1.getTicketType()));
 		Assert.assertTrue(this.typeManager.getActiveTicketTypeByUniqueName(uniqueName).getAllFieldTypes()
 				.contains(this.anyUserDefinedFieldType));
 		Assert.assertFalse(this.typeManager.getActiveTicketTypeByUniqueName(uniqueName).getSupersededTypes().isEmpty());
 	}
 
 	/**
 	 * addEndState. with date so that the consistency is hurt to check if the exception is thrown.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = ConsistencyException.class)
 	public void testaddEndState1() throws Exception {
 		this.theStandardFeature.addEndState(this.modelClient.getTypeManager().getClosed());
 	}
 
 	/**
 	 * addEndState.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Test
 	public void testaddEndState2() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "");
 		final StateType state = new StateType(this.modelClient, "neuer Endstatus", "blubb");
 		anyTicketType.addPossibleState(state);
 		anyTicketType.addEndState(state);
 	}
 
 	/**
	 * addEndState. With data so that the consistency is hurt to check if the exception is thrown.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
	@Test(expected = ConsistencyException.class)
 	public void testaddEndState3() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "");
 		final StateType state = new StateType(this.modelClient, "neuer Endstatus", "blubb");
 		new Bug(this.modelClient, anyTicketType, "neuer Bug", "", this.testProject.getBacklog(), this.r);
 		anyTicketType.addPossibleState(state);
 		anyTicketType.addEndState(state);
 	}
 
 	/**
 	 * addFieldType. With a field already added to check if the exception is thrown.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = DoubleDefinitionException.class)
 	public void testaddFieldType() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "");
 		final FieldType ft = new TextFieldType(this.modelClient, "name", this.typeManager.getMany());
 		anyTicketType.addFieldType(ft);
 		anyTicketType.addFieldType(ft);
 	}
 
 	/**
 	 * tests to add a transitionRule to a ticketType.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Test
 	public void testaddTransitionRule1() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "");
 		final TransitionRule tr =
 				new TransitionRule(this.modelClient, this.typeManager.getClosed(), this.typeManager.getOpen());
 		anyTicketType.addTransitionRule(tr);
 	}
 
 	/**
 	 * tests to add a transitionRule to a ticketType.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Test
 	public void testaddTransitionRule2() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "");
 		final TransitionRule tr =
 				new TransitionRule(this.modelClient, this.typeManager.getClosed(), this.typeManager.getOpen());
 		new Bug(this.modelClient, anyTicketType, "neuer Bug", "", this.testProject.getBacklog(), this.r);
 		anyTicketType.addTransitionRule(tr);
 	}
 
 	/**
 	 * addTransitionRule.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@SuppressWarnings("rawtypes")
 	@Test(expected = ConsistencyException.class)
 	public void testcheckFieldChange() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "");
 		final BugTicketType otherTicketType = new BugTicketType(this.modelClient, "ExtendedBug2", "");
 		final FieldType ft = new TextFieldType(this.modelClient, "name", this.typeManager.getMany());
 		otherTicketType.addFieldType(ft);
 		final Bug anyBug =
 				new Bug(this.modelClient, anyTicketType, "blubbeldi", "", this.testProject.getBacklog(), this.r);
 		final Bug otherBug =
 				new Bug(this.modelClient, otherTicketType, "bla", "bla", this.testProject.getBacklog(), this.r);
 		final Iterator<Field> fIt = otherBug.getAllFields().iterator();
 		while (fIt.hasNext()) {
 			final Field current = fIt.next();
 			if (current.getType().equals(ft)) {
 				anyTicketType.checkFieldChange(current, anyBug);
 			}
 		}
 	}
 
 	/**
 	 * tests to get the description of a ticket type.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testGetDescription() throws Exception {
 		final Feature f =
 				new Feature(this.modelClient, this.theStandardFeature, "Featurename", "Beschreibung",
 						this.testProject.getBacklog());
 		Assert.assertEquals(((SingleField<String>) this.theStandardFeature.getField(
 				this.theStandardFeature.getDescriptionType(), f)).getValue(), this.theStandardFeature.getDescription(f));
 	}
 
 	/**
 	 * tests to remove a field type from a ticket type.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Test
 	public void testRemoveFieldType1() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "");
 		final FieldType ft = new TextFieldType(this.modelClient, "name", this.typeManager.getMany());
 		anyTicketType.addFieldType(ft);
 		anyTicketType.removeFieldType(ft);
 	}
 
 	/**
 	 * test to remove a field type from a ticket type with data which hurts the consistency to check if the exception is
 	 * thrown.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Test(expected = ConsistencyException.class)
 	public void testRemoveFieldType2() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "");
 		anyTicketType.removeFieldType(this.typeManager.getFieldTypes().get(0));
 	}
 
 	/**
 	 * tests to remove a field type from a ticket type.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Test
 	public void testRemoveFieldType3() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "");
 		final FieldType ft = new TextFieldType(this.modelClient, "name", this.typeManager.getMany());
 		anyTicketType.addFieldType(ft);
 		new Bug(this.modelClient, anyTicketType, "neuer Bug", "", this.testProject.getBacklog(), this.r);
 		anyTicketType.removeFieldType(ft);
 	}
 
 	/**
 	 * tests to remove a possible state from a ticket type.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Test
 	public void testRemovePossibleState1() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "");
 		final StateType state = new StateType(this.modelClient, "neuer Endstatus", "blubb");
 		anyTicketType.addPossibleState(state);
 		anyTicketType.removePossibleState(state);
 	}
 
 	/**
 	 * tests to remove a possible state form a ticketType.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Test
 	public void testRemovePossibleState2() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "blaa");
 		final StateType state = new StateType(this.modelClient, "neuer Endstatus", "blubb");
 		anyTicketType.addPossibleState(state);
 		new Bug(this.modelClient, anyTicketType, "neuer Bug", "", this.testProject.getBacklog(), this.r);
 		anyTicketType.removePossibleState(state);
 	}
 
 	/**
 	 * tests to remove a transitionRule from the ticketType.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Test
 	public void testRemoveTransitionRule1() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "");
 		final TransitionRule tr =
 				new TransitionRule(this.modelClient, this.typeManager.getClosed(), this.typeManager.getOpen());
 		anyTicketType.addTransitionRule(tr);
 		anyTicketType.removeTransitionRule(tr);
 	}
 
 	/**
 	 * tests to remove a transition rule form a ticketType.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Test
 	public void testRemoveTransitionRule2() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "");
 		final TransitionRule tr =
 				new TransitionRule(this.modelClient, this.typeManager.getClosed(), this.typeManager.getOpen());
 		new Bug(this.modelClient, anyTicketType, "neuer Bug", "", this.testProject.getBacklog(), this.r);
 		anyTicketType.addTransitionRule(tr);
 		anyTicketType.removeTransitionRule(tr);
 	}
 
 	/**
 	 * tests to set a ticketType active.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Test
 	public void testSetActive1() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "blaa");
 		final StateType state = new StateType(this.modelClient, "neuer Endstatus", "blubb");
 		final FieldType ft = new TextFieldType(this.modelClient, "name", this.typeManager.getMany());
 		anyTicketType.addPossibleState(state);
 		anyTicketType.addEndState(state);
 		anyTicketType.setActive(state, ft);
 	}
 
 	/**
 	 * test to set a ticketType active.
 	 * 
 	 * @throws Exception
 	 *             if the use of one of the methods fails
 	 */
 	@Test
 	public void testSetActive2() throws Exception {
 		final BugTicketType anyTicketType = new BugTicketType(this.modelClient, "ExtendedBug", "blaa");
 		new Bug(this.modelClient, anyTicketType, "neuer Bug", "", this.testProject.getBacklog(), this.r);
 		final StateType state = new StateType(this.modelClient, "neuer Endstatus", "blubb");
 		final FieldType ft = new TextFieldType(this.modelClient, "name", this.typeManager.getMany());
 		anyTicketType.addPossibleState(state);
 		anyTicketType.addEndState(state);
 		anyTicketType.setActive(state, ft);
 	}
 }
