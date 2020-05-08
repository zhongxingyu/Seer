 package fhdw.ipscrum.shared.model.metamodel.ticketsAndTypes;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 import fhdw.ipscrum.shared.exceptions.IPScrumGeneralException;
 import fhdw.ipscrum.shared.model.Model;
 import fhdw.ipscrum.shared.model.metamodel.fields.DateFieldType;
 import fhdw.ipscrum.shared.model.metamodel.fields.EffortFieldType;
 import fhdw.ipscrum.shared.model.metamodel.fields.FieldType;
 import fhdw.ipscrum.shared.model.metamodel.fields.Many;
 import fhdw.ipscrum.shared.model.metamodel.fields.One;
 import fhdw.ipscrum.shared.model.metamodel.fields.PBIFieldType;
 import fhdw.ipscrum.shared.model.metamodel.fields.PersonFieldType;
 import fhdw.ipscrum.shared.model.metamodel.fields.ReleaseFieldType;
 import fhdw.ipscrum.shared.model.metamodel.fields.SprintFieldType;
 import fhdw.ipscrum.shared.model.metamodel.fields.SystemFieldType;
 import fhdw.ipscrum.shared.model.metamodel.fields.TextFieldType;
 import fhdw.ipscrum.shared.model.metamodel.states.StateType;
 
 /**
  * The type manager is the central manager instance for standard-types and user-types. Types stands for ticket types,
  * field types and state types.
  */
 public class TypeManager implements IsSerializable, Serializable {
 
 	/**
 	 * represents the serialVersionUID.
 	 */
 	private static final long serialVersionUID = 437359099602102358L;
 
 	/**
 	 * represents the model related to the manager.
 	 */
 	private Model model;
 
 	/**
 	 * represents the copy of the managed ticketType.
 	 */
 	private TicketType copy;
 
 	/**
 	 * represents the exception generally thrown.
 	 */
 	private IPScrumGeneralException exception;
 
 	/**
 	 * Multiplicities: Only 1 instance per multiplicity needed.
 	 * 
 	 * represents the multiplicity many.
 	 */
 	private Many many;
 
 	/**
 	 * represents the multiplicity one.
 	 */
 	private One one;
 
 	/**
 	 * represents the standard FeatureTicketType.
 	 */
 	private FeatureTicketType standardFeatureType;
 
 	/**
 	 * represents the standard BugType.
 	 */
 	private BugTicketType standardBugType;
 
 	/**
 	 * represents the standard TaskType.
 	 */
 	private TaskTicketType standardTaskType;
 	/*
 	 * Field types for TicketType
 	 */
 	/**
 	 * represents the field for the name.
 	 */
 	private TextFieldType nameType;
 
 	/**
 	 * represents the field for the description.
 	 */
 	private TextFieldType descriptionType;
 	/*
 	 * Field types for PBITicketType (= FeatureTicketType)
 	 */
 	/**
 	 * represents the field for the last editor.
 	 */
 	private PersonFieldType lastEditorType;
 
 	/**
 	 * represents the field for the man day costs.
 	 */
 	private EffortFieldType manDayCostsType;
 
 	/**
 	 * represents the field for the hints.
 	 */
 	private TextFieldType hintsType;
 
 	/**
 	 * represents the field for the acceptance criteria.
 	 */
 	private TextFieldType acceptanceCriteriaType;
 
 	/**
 	 * represents the field for the sprint.
 	 */
 	private SprintFieldType sprintType;
 	/*
 	 * Field types for BugTicketType
 	 */
 	/**
 	 * represents the field for the release.
 	 */
 	private ReleaseFieldType versionType;
 
 	/**
 	 * represents the field for the system.
 	 */
 	private SystemFieldType systemsType;
 	/*
 	 * Field types for TaskTicketType
 	 */
 	/**
 	 * represents the field for the plan effort.
 	 */
 	private EffortFieldType planEffortType;
 
 	/**
 	 * represents the responsible person.
 	 */
 	private PersonFieldType responsiblePersonType;
 
 	/**
 	 * represents the assigned PBIs.
 	 */
 	private PBIFieldType assignedPBIsType;
 
 	/**
 	 * represents the field for the finished date.
 	 */
 	private DateFieldType finishDateType;
 	/*
 	 * State Types
 	 */
 	/**
 	 * represents state open.
 	 */
 	private StateType open;
 
 	/**
 	 * represents the sate closed.
 	 */
 	private StateType closed;
 
 	/**
 	 * represents the state in process.
 	 */
 	private StateType inProcess;
 
 	/**
 	 * constructor of the type manager.
 	 * 
 	 * @param model
 	 *            : it is inserted into the model
 	 */
 	public TypeManager(final Model model) {
 		this();
 		this.model = model;
 	}
 
 	/**
 	 * constructor without parameters. Needed for serialization.
 	 */
 	private TypeManager() {
 
 	}
 
 	/**
 	 * Returns all standard field types.
 	 * 
 	 * @return list of {@link FieldType}
 	 */
 	public List<FieldType> fetchStandardFieldTypes() {
 		final List<FieldType> result = new ArrayList<FieldType>();
 		result.add(this.getAcceptanceCriteriaType());
 		result.add(this.getDescriptionType());
 		result.add(this.getHintsType());
 		result.add(this.getLastEditorType());
 		result.add(this.getManDayCostsType());
 		result.add(this.getNameType());
 		result.add(this.getPlanEffortType());
 		result.add(this.getResponsiblePersonType());
 		result.add(this.getSprintType());
 		result.add(this.getSystemsType());
 		result.add(this.getVersionType());
 		result.add(this.getFinishDateType());
 		result.add(this.getAssignedPBIsType());
 		return result;
 	}
 
 	/**
 	 * Returns all standard state types.
 	 * 
 	 * @return list of {@link StateType}
 	 */
 	public List<StateType> fetchStandardStateTypes() {
 		final List<StateType> result = new ArrayList<StateType>();
 		result.add(this.getOpen());
 		result.add(this.getClosed());
 		result.add(this.getInProcess());
 		return result;
 	}
 
 	/**
 	 * Returns all standard ticket types.
 	 * 
 	 * @return list of {@link TicketType}
 	 */
 	public List<TicketType> fetchStandardTicketTypes() {
 		final List<TicketType> result = new ArrayList<TicketType>();
 		result.add(this.getStandardBugType());
 		result.add(this.getStandardFeatureType());
 		result.add(this.getStandardTaskType());
 		return result;
 	}
 
 	/**
 	 * Returns all user defined field types.
 	 * 
 	 * @return list of {@link FieldType}
 	 */
 	public List<FieldType> fetchUserFieldTypes() {
 		final List<FieldType> result = new ArrayList<FieldType>();
 		final Iterator<FieldType> i = this.getFieldTypes().iterator();
 		final List<FieldType> standardFieldTypes = this.fetchStandardFieldTypes();
 		while (i.hasNext()) {
 			final FieldType current = i.next();
 			if (!standardFieldTypes.contains(current)) {
 				result.add(current);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Returns all user defined state types.
 	 * 
 	 * @return list of {@link StateType}
 	 */
 	public List<StateType> fetchUserStateTypes() {
 		final List<StateType> result = new ArrayList<StateType>();
 		final List<StateType> standardStateTypes = this.fetchStandardStateTypes();
 		final Iterator<StateType> i = this.getStateTypes().iterator();
 		while (i.hasNext()) {
 			final StateType current = i.next();
 			if (!standardStateTypes.contains(current)) {
 				result.add(current);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Returns all user defined ticket types.
 	 * 
 	 * @return list of {@link TicketType}
 	 */
 	public List<TicketType> fetchUserTicketTypes() {
 		final List<TicketType> result = new ArrayList<TicketType>();
 		final List<TicketType> standardTicketTypes = this.fetchStandardTicketTypes();
 		final Iterator<TicketType> i = this.getActiveTicketTypes().iterator();
 		while (i.hasNext()) {
 			final TicketType current = i.next();
 			if (!standardTicketTypes.contains(current)) {
 				result.add(current);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * getter of the field of the acceptance criteria.
 	 * 
 	 * @return the acceptance criteria field
 	 */
 	public TextFieldType getAcceptanceCriteriaType() {
 		return this.acceptanceCriteriaType;
 	}
 
 	/**
 	 * returns the newest version of the ticket type with the unique name.
 	 * 
 	 * @param name
 	 *            of the searched ticket type
 	 * @return the active ticket type with the given name
 	 */
 	public TicketType getActiveTicketTypeByUniqueName(final String name) {
 		TicketType result = null;
 		final Iterator<TicketType> activeTypes = this.getActiveTicketTypes().iterator();
 		while (activeTypes.hasNext()) {
 			final TicketType current = activeTypes.next();
 			if (current.getTypeName().equals(name)) {
 				result = current;
 				break;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Returns all active ticket types.
 	 * 
 	 * @return list of {@link TicketType}
 	 */
 	public List<TicketType> getActiveTicketTypes() {
 		final List<TicketType> result = new ArrayList<TicketType>();
 		// 1. get all ticket types
 		final List<TicketType> allTicketTypes = this.getModel().getAllTicketTypes();
 		// 2. separate superseded types from active types
 		final Set<TicketType> supersededTypes = new HashSet<TicketType>();
 		Iterator<TicketType> allTicketTypesIterator = allTicketTypes.iterator();
 		while (allTicketTypesIterator.hasNext()) {
 			final TicketType current = allTicketTypesIterator.next();
 			if (current.getSupersededTypes().size() > 0) {
 				final Iterator<TicketType> currentSupersededTypesIterator = current.getSupersededTypes().iterator();
 				while (currentSupersededTypesIterator.hasNext()) {
 					supersededTypes.add(currentSupersededTypesIterator.next());
 				}
 			}
 		}
 		// 3. collect all ticket types which are not in <supersededTypes>
 		allTicketTypesIterator = allTicketTypes.iterator();
 		while (allTicketTypesIterator.hasNext()) {
 			final TicketType current = allTicketTypesIterator.next();
 			if (!supersededTypes.contains(current)) {
 				result.add(current);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * getter of the field of the assigned PBIs.
 	 * 
 	 * @return the assigned PBIs field
 	 */
 	public PBIFieldType getAssignedPBIsType() {
 		return this.assignedPBIsType;
 	}
 
 	/**
 	 * getter of the state closed.
 	 * 
 	 * @return the closed state type
 	 */
 	public StateType getClosed() {
 		return this.closed;
 	}
 
 	/**
 	 * getter of the field of the description.
 	 * 
 	 * @return the description field type
 	 */
 	public TextFieldType getDescriptionType() {
 		return this.descriptionType;
 	}
 
 	/**
 	 * Returns all existing field types.
 	 * 
 	 * @return list of {@link FieldType}
 	 */
 	public List<FieldType> getFieldTypes() {
 		return this.getModel().getAllFieldTypes();
 	}
 
 	/**
 	 * getter of the field of the finished date.
 	 * 
 	 * @return the finished date field
 	 */
 	public DateFieldType getFinishDateType() {
 		return this.finishDateType;
 	}
 
 	/**
 	 * getter of the field of the hints.
 	 * 
 	 * @return the hints field
 	 */
 	public TextFieldType getHintsType() {
 		return this.hintsType;
 	}
 
 	/**
 	 * getter of the state in progress.
 	 * 
 	 * @return the in progress state type
 	 */
 	public StateType getInProcess() {
 		return this.inProcess;
 	}
 
 	/**
 	 * getter of the field of the last editor.
 	 * 
 	 * @return the last editor field type
 	 */
 	public PersonFieldType getLastEditorType() {
 		return this.lastEditorType;
 	}
 
 	/**
 	 * getter of the man day costs field.
 	 * 
 	 * @return the man day costs field
 	 */
 	public EffortFieldType getManDayCostsType() {
 		return this.manDayCostsType;
 	}
 
 	/**
 	 * get the multiplicity many.
 	 * 
 	 * @return the multiplicity many
 	 */
 	public Many getMany() {
 		return this.many;
 	}
 
 	/**
 	 * getter of the field of the name.
 	 * 
 	 * @return the name field
 	 */
 	public TextFieldType getNameType() {
 		return this.nameType;
 	}
 
 	/**
 	 * getter of the multiplicity one.
 	 * 
 	 * @return the multiplicity one
 	 */
 	public One getOne() {
 		return this.one;
 	}
 
 	/**
 	 * getter of the state open.
 	 * 
 	 * @return the open state
 	 */
 	public StateType getOpen() {
 		return this.open;
 	}
 
 	/**
 	 * getter of the field for the plan effort.
 	 * 
 	 * @return the plan effort field
 	 */
 	public EffortFieldType getPlanEffortType() {
 		return this.planEffortType;
 	}
 
 	/**
 	 * getter of the field for the responsible person.
 	 * 
 	 * @return the responsible person field
 	 */
 	public PersonFieldType getResponsiblePersonType() {
 		return this.responsiblePersonType;
 	}
 
 	/**
 	 * getter of the field of the sprint.
 	 * 
 	 * @return the sprint field type
 	 */
 	public SprintFieldType getSprintType() {
 		return this.sprintType;
 	}
 
 	/**
 	 * getter of the standard bug.
 	 * 
 	 * @return the standard bug type
 	 */
 	public BugTicketType getStandardBugType() {
 		return this.standardBugType;
 	}
 
 	/**
 	 * getter of the standard feature.
 	 * 
 	 * @return the standard feature type
 	 */
 	public FeatureTicketType getStandardFeatureType() {
 		return this.standardFeatureType;
 	}
 
 	/**
 	 * getter of the standard task.
 	 * 
 	 * @return the standard task tyspe
 	 */
 	public TaskTicketType getStandardTaskType() {
 		return this.standardTaskType;
 	}
 
 	/**
 	 * Returns all state types.
 	 * 
 	 * @return list of {@link StateType}
 	 */
 	public List<StateType> getStateTypes() {
 		return this.getModel().getAllStateTypes();
 	}
 
 	/**
 	 * getter of the field of the system.
 	 * 
 	 * @return of the system field type
 	 */
 	public SystemFieldType getSystemsType() {
 		return this.systemsType;
 	}
 
 	/**
 	 * getter of the field of the release.
 	 * 
 	 * @return the release field
 	 */
 	public ReleaseFieldType getVersionType() {
 		return this.versionType;
 	}
 
 	/**
 	 * sets the field of the acceptance criteria.
 	 * 
 	 * @param acceptanceCriteriaType
 	 *            the field to set
 	 */
 	public void setAcceptanceCriteriaType(final TextFieldType acceptanceCriteriaType) {
 		if (this.acceptanceCriteriaType == null) {
 			this.acceptanceCriteriaType = acceptanceCriteriaType;
 		}
 	}
 
 	/**
 	 * sets the field of the assigned PBIs.
 	 * 
 	 * @param assignedPBIsType
 	 *            is the field to set
 	 */
 	public void setAssignedPBIsType(final PBIFieldType assignedPBIsType) {
 		if (this.assignedPBIsType == null) {
 			this.assignedPBIsType = assignedPBIsType;
 		}
 	}
 
 	/**
 	 * sets the state type closed.
 	 * 
 	 * @param closedState
 	 *            is the state type to set
 	 */
 	public void setClosedType(final StateType closedState) {
 		if (this.closed == null) {
 			this.closed = closedState;
 		}
 	}
 
 	/**
 	 * sets the field of the description.
 	 * 
 	 * @param descriptionType
 	 *            the field to set
 	 */
 	public void setDescriptionType(final TextFieldType descriptionType) {
 		if (this.descriptionType == null) {
 			this.descriptionType = descriptionType;
 		}
 	}
 
 	/**
 	 * sets the field of the finished date.
 	 * 
 	 * @param finishDateFieldType
 	 *            is the field to set
 	 */
 	public void setFinishDateType(final DateFieldType finishDateFieldType) {
 		if (this.finishDateType == null) {
 			this.finishDateType = finishDateFieldType;
 		}
 	}
 
 	/**
 	 * sets the field of the hint.
 	 * 
 	 * @param hintsType
 	 *            is the field to set
 	 */
 	public void setHintsType(final TextFieldType hintsType) {
 		if (this.hintsType == null) {
 			this.hintsType = hintsType;
 		}
 	}
 
 	/**
 	 * sets the state in process.
 	 * 
 	 * @param inProcessState
 	 *            is the state to set
 	 */
 	public void setInProcessType(final StateType inProcessState) {
 		if (this.inProcess == null) {
 			this.inProcess = inProcessState;
 		}
 	}
 
 	/**
 	 * sets the field of the last editor.
 	 * 
 	 * @param lastEditorType
 	 *            is the field to set
 	 */
 	public void setLastEditorType(final PersonFieldType lastEditorType) {
 		if (this.lastEditorType == null) {
 			this.lastEditorType = lastEditorType;
 		}
 	}
 
 	/**
 	 * sets the field for the man day costs.
 	 * 
 	 * @param manDayCostsType
 	 *            is the field to set
 	 */
 	public void setManDayCostsType(final EffortFieldType manDayCostsType) {
 		if (this.manDayCostsType == null) {
 			this.manDayCostsType = manDayCostsType;
 		}
 	}
 
 	/**
 	 * sets the multiplicity many.
 	 * 
 	 * @param many
 	 *            is the multiplicity to set
 	 */
 	public void setMany(final Many many) {
 		if (this.many == null) {
 			this.many = many;
 		}
 	}
 
 	/**
 	 * sets the field for the name of the type.
 	 * 
 	 * @param nameType
 	 *            is the field to set
 	 */
 	public void setNameType(final TextFieldType nameType) {
 		if (this.nameType == null) {
 			this.nameType = nameType;
 		}
 	}
 
 	/**
 	 * sets the multiplicity one.
 	 * 
 	 * @param one
 	 *            the multiplicity to set
 	 */
 	public void setOne(final One one) {
 		if (this.one == null) {
 			this.one = one;
 		}
 	}
 
 	/**
 	 * sets the state open.
 	 * 
 	 * @param openState
 	 *            is the state to set
 	 */
 	public void setOpenType(final StateType openState) {
 		if (this.open == null) {
 			this.open = openState;
 		}
 	}
 
 	/**
 	 * sets the field for the plan effort.
 	 * 
 	 * @param planEffortType
 	 *            is the field to set
 	 */
 	public void setPlanEffortType(final EffortFieldType planEffortType) {
 		if (this.planEffortType == null) {
 			this.planEffortType = planEffortType;
 		}
 	}
 
 	/**
 	 * sets the field for the responsible person.
 	 * 
 	 * @param responsiblePersonType
 	 *            : initializes the new field
 	 */
 	public void setResponsiblePersonType(final PersonFieldType responsiblePersonType) {
 		if (this.responsiblePersonType == null) {
 			this.responsiblePersonType = responsiblePersonType;
 		}
 	}
 
 	/**
 	 * sets the field for the sprint.
 	 * 
 	 * @param sprintType
 	 *            : new field
 	 */
 	public void setSprintType(final SprintFieldType sprintType) {
 		if (this.sprintType == null) {
 			this.sprintType = sprintType;
 		}
 	}
 
 	/**
 	 * sets a bugTicketType as standard.
 	 * 
 	 * @param bugType
 	 *            : new standard bugTicketType
 	 */
 	public void setStandardBugType(final BugTicketType bugType) {
 		if (this.standardBugType == null) {
 			this.standardBugType = bugType;
 		}
 	}
 
 	/**
 	 * sets a featureTicketType as standard.
 	 * 
 	 * @param featureType
 	 *            : new standard featureTicketType
 	 */
 	public void setStandardFeatureType(final FeatureTicketType featureType) {
 		if (this.standardFeatureType == null) {
 			this.standardFeatureType = featureType;
 		}
 	}
 
 	/**
 	 * sets a taskTicketTypa as the standard.
 	 * 
 	 * @param taskType
 	 *            : new standard taskTicketType
 	 */
 	public void setStandardTaskType(final TaskTicketType taskType) {
 		if (this.standardTaskType == null) {
 			this.standardTaskType = taskType;
 		}
 	}
 
 	/**
 	 * sets the field for the system.
 	 * 
 	 * @param systemsType
 	 *            : new field
 	 */
 	public void setSystemsType(final SystemFieldType systemsType) {
 		if (this.systemsType == null) {
 			this.systemsType = systemsType;
 		}
 	}
 
 	/**
 	 * sets the field for the version.
 	 * 
 	 * @param versionType
 	 *            : new field
 	 */
 	public void setVersionType(final ReleaseFieldType versionType) {
 		if (this.versionType == null) {
 			this.versionType = versionType;
 		}
 	}
 
 	/**
 	 * this Method returns a copy of a ticket type. the copy will contain all attributes of the original, but has a
 	 * different ID! this method is called when a ticket-type is changed and the change has influence on the ticket
 	 * instances.
 	 * 
 	 * @param original
 	 *            the ticket type to copy
 	 * @return the copy of the attached ticket type
 	 * @throws IPScrumGeneralException
 	 *             if something fails
 	 */
 	protected TicketType provideTicketTypeCopy(final TicketType original) throws IPScrumGeneralException {
 		this.setException(null);
 		this.setCopy(null);
 		original.accept(new TicketTypeVisitor() {
 			@Override
 			public void handleBugTicketType(final BugTicketType bugTicketType) {
 				TypeManager.this.createBugCopy(bugTicketType);
 			}
 
 			@Override
 			public void handleFeatureTicketType(final FeatureTicketType featureTicketType) {
 				TypeManager.this.createFeatureCopy(featureTicketType);
 			}
 
 			@Override
 			public void handleTaskTicketType(final TaskTicketType taskTicketType) {
 				TypeManager.this.createTaskCopy(taskTicketType);
 			}
 		});
 		this.checkException();
 		final List<FieldType> standardFieldTypes = this.fetchStandardFieldTypes();
 		final Iterator<FieldType> fieldTypesIterator = original.getAllFieldTypes().iterator();
 		while (fieldTypesIterator.hasNext()) {
 			final FieldType current = fieldTypesIterator.next();
 			if (!standardFieldTypes.contains(current)) {
 				this.copy.addFieldType(current);
 			}
 		}
 		this.copy.setVersionNumber(original.getVersionNumber());
 		this.copy.incrementVersionNumber();
 		return this.copy;
 	}
 
 	/**
 	 * checks if there is an exception.
 	 * 
 	 * @throws IPScrumGeneralException
 	 *             if an exception is found
 	 */
 	private void checkException() throws IPScrumGeneralException {
 		if (this.exception != null) {
 			throw this.exception;
 		}
 	}
 
 	/**
 	 * Getter of the model.
 	 * 
 	 * @return the current model
 	 */
 	private Model getModel() {
 		return this.model;
 	}
 
 	/**
 	 * checks if a ticket type has a doublet.
 	 * 
 	 * @param possibleDoublet
 	 *            the attached ticket type to check
 	 * @return true if the ticket type has a doublet
 	 */
 	@SuppressWarnings("unused")
 	private boolean hasTicketTypeDoublet(final TicketType possibleDoublet) {
 		boolean result = false;
 		final Iterator<TicketType> ticketTypes = this.getActiveTicketTypes().iterator();
 		while (ticketTypes.hasNext()) {
 			final TicketType current = ticketTypes.next();
 			if (possibleDoublet.getTypeName().equals(current.getTypeName())) {
 				result = true;
 				break;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * sets a copy of a ticketType.
 	 * 
 	 * @param ticketType
 	 *            the attached type to copy
 	 */
 	private void setCopy(final TicketType ticketType) {
 		this.copy = ticketType;
 	}
 
 	/**
 	 * sets an exception.
 	 * 
 	 * @param exception
 	 *            to set
 	 */
 	private void setException(final IPScrumGeneralException exception) {
 		this.exception = exception;
 	}
 
 	/**
 	 * Creates a copy of the bug type.
 	 * 
 	 * @param original
 	 *            the original bug type
 	 */
 	private void createBugCopy(final BugTicketType original) {
 		try {
 			final BugTicketType concreteCopy =
 					new BugTicketType(TypeManager.this.model, original.getTypeName(), original.getTypeDescription(),
 							TypeManager.this);
 			TypeManager.this.setCopy(concreteCopy);
 		} catch (final IPScrumGeneralException e) {
 			TypeManager.this.setException(e);
 		}
 	}
 
 	/**
 	 * Creates a copy of the feature type.
 	 * 
 	 * @param original
 	 *            the original feature type
 	 */
 	private void createFeatureCopy(final FeatureTicketType original) {
 		try {
 			final FeatureTicketType concreteCopy =
 					new FeatureTicketType(TypeManager.this.model, original.getTypeName(),
 							original.getTypeDescription(), TypeManager.this);
 			TypeManager.this.setCopy(concreteCopy);
 		} catch (final IPScrumGeneralException e) {
 			TypeManager.this.setException(e);
 		}
 	}
 
 	/**
 	 * Creates a copy of the task type.
 	 * 
 	 * @param original
 	 *            the original task type
 	 */
 	private void createTaskCopy(final TaskTicketType original) {
 		try {
 			final TaskTicketType concreteCopy =
 					new TaskTicketType(TypeManager.this.model, original.getTypeName(), original.getTypeDescription(),
 							TypeManager.this);
 			TypeManager.this.setCopy(concreteCopy);
 		} catch (final IPScrumGeneralException e) {
 			TypeManager.this.setException(e);
 		}
 	}
 
 }
