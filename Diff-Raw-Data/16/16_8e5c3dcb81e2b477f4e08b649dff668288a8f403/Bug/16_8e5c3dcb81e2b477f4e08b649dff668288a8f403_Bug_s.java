 /**
  * 
  */
 package fhdw.ipscrum.shared.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 import fhdw.ipscrum.shared.constants.ExceptionConstants;
 import fhdw.ipscrum.shared.exceptions.UserException;
 import fhdw.ipscrum.shared.exceptions.WrongReleaseException;
 import fhdw.ipscrum.shared.exceptions.WrongSystemException;
 import fhdw.ipscrum.shared.model.interfaces.IBugState;
 import fhdw.ipscrum.shared.model.interfaces.IHasSystems;
 import fhdw.ipscrum.shared.model.interfaces.IRelease;
 import fhdw.ipscrum.shared.model.visitor.IProductBacklogItemVisitor;
 
 /**
  * Represents a Bug as special type of a {@link ProductBacklogItem}. A feature
  * may contain relationships to other features. Furthermore, acceptance criteria
  * and hints can be associated. A feature can be editable in the state "open"
  * and is read-only in the state "closed".
  */
 public class Bug extends ProductBacklogItem implements IHasSystems {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3237023915419793623L;
 
 	private IRelease version;
 
 	private IBugState state;
 
 	private Collection<System> systems;
 
 	@SuppressWarnings("unused")
 	private Bug() {
 	}
 
 	/**
 	 * Constructor for a new Bug
 	 * 
 	 * @param name
 	 *            String
 	 * @param description
 	 *            String
 	 * @param version
 	 *            {@link IRelease}
 	 * @param backlog
 	 *            {@link ProductBacklog}
 	 * @throws UserException
 	 */
 	public Bug(final String name, final String description,
 			final IRelease version, final ProductBacklog backlog)
 			throws UserException {
 		super(name, description, backlog);
 		this.setVersion(version);
 		this.systems = new ArrayList<System>();
 	}
 
 	@Override
 	public void accept(final IProductBacklogItemVisitor visitor) {
 		visitor.handleBug(this);
 	}
 
 	/**
 	 * action depends on current state, if the bug is open, the System will be
 	 * added
 	 * 
 	 * @param system
 	 * @throws UserException
 	 */
 	@Override
 	public void addSystem(final System system) throws UserException {
 		this.getState().addSystem(system);
 	}
 
 	/**
 	 * action depends on current state, if the bug is open, the System will be
 	 * removed
 	 * 
 	 * @param system
 	 * @throws UserException
 	 */
 	@Override
 	public void removeSystem(final System system) throws UserException {
 		this.getState().removeSystem(system);
 	}
 
 	/**
 	 * Adds a system to the local list to restrict where the bug happened <br>
 	 * <b> ATTENTION </b><br>
 	 * Not for direct usage, use instead the Method: addSystem(System system)
 	 * 
 	 * @param system
 	 * @throws WrongSystemException
 	 */
	public void doAddSystem(final System system) throws WrongSystemException {
 		if (this.getBacklog().getProject().isPossibleSystem(system)) {
 			this.systems.add(system);
 		} else {
 			throw new WrongSystemException(
 					ExceptionConstants.SYSTEM_IS_NOT_POSSIBLE);
 		}
 		this.notifyObservers();
 	}
 
 	@Override
 	protected void doClose() {
 		this.state = new BugClosedState();
 		this.notifyObservers();
 	}
 
 	/**
 	 * action depends on current state, if the bug is open, the action will
 	 * executed
 	 * 
 	 * @param version
 	 * @throws WrongReleaseException
 	 *             , if the release is not referenced in project
 	 */
	public void doSetVersion(final IRelease version)
 			throws WrongReleaseException {
 		if (this.getBacklog().getProject().getReleasePlan().contains(version)) {
 			this.version = version;
 		} else {
 			throw new WrongReleaseException(
 					ExceptionConstants.RELEASE_NOT_IN_PROJECT);
 		}
 		this.notifyObservers();
 	}
 
 	/**
 	 * Method getVersion.
 	 * 
 	 * @return release
 	 */
 	public IRelease getVersion() {
 		return this.version;
 	}
 
 	@Override
 	public IBugState getState() {
 		return this.state;
 	}
 
 	/**
 	 * Method getSystems.
 	 * 
 	 * @return unmodifiable List
 	 */
 	@Override
 	public Collection<System> getSystems() {
 		return Collections.unmodifiableCollection(this.systems);
 	}
 
 	@Override
 	protected void initializeState() {
 		this.state = new BugOpenState(this);
 	}
 
 	public void setVersion(final IRelease version) throws UserException {
 		this.state.setVersion(version);
 	}
 
 	/**
 	 * Remove a system of the local list<br>
 	 * <b> ATTENTION </b><br>
 	 * Not for direct usage, use instead the Method: removeSystem(System system)
 	 * 
 	 * @param system
 	 */
	public void doRemoveSystem(final System system) {
 		this.systems.remove(system);
 		this.notifyObservers();
 	}
 }
