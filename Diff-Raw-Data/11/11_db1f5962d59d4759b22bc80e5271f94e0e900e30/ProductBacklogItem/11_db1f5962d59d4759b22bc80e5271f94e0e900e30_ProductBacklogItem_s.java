 package fhdw.ipscrum.shared.model;
 
 import fhdw.ipscrum.shared.bdas.BDACompare;
 import fhdw.ipscrum.shared.bdas.BDAManyToMany;
 import fhdw.ipscrum.shared.constants.TextConstants;
 import fhdw.ipscrum.shared.exceptions.ConsistencyException;
 import fhdw.ipscrum.shared.exceptions.DoubleDefinitionException;
 import fhdw.ipscrum.shared.exceptions.NoSprintDefinedException;
 import fhdw.ipscrum.shared.exceptions.NoValidValueException;
 import fhdw.ipscrum.shared.exceptions.UserException;
 import fhdw.ipscrum.shared.model.interfaces.IPerson;
 import fhdw.ipscrum.shared.model.interfaces.ISprint;
 import fhdw.ipscrum.shared.model.visitor.IProductBacklogItemVisitor;
 import fhdw.ipscrum.shared.observer.Observable;
 
 /**
  * Represents the abstract Root Class for a ProductBacklogItem.
  */
 public abstract class ProductBacklogItem extends Observable implements
 		BDACompare {
 
 	class ToBacklogAssoc extends
 			BDAManyToMany<ProductBacklog.ToPBIAssoc, ProductBacklogItem> {
 		public ToBacklogAssoc(final ProductBacklogItem element) {
 			super(element);
 		}
 	}
 
 	public class ToSprintAssoc extends
 			BDAManyToMany<ISprint.ToPBIAssoc, ProductBacklogItem> {
 		public ToSprintAssoc(final ProductBacklogItem element) {
 			super(element);
 		}
 	}
 
 	private String name;
 
 	private Integer manDayCosts;
 	private IPerson lastEditor;
 
 	private final ToBacklogAssoc backlogAssoc;
 
 	private final ToSprintAssoc sprintAssoc;
 
 	/**
 	 * @param name
 	 *            Name of the PBI.
 	 * @param backlog
 	 *            Backlog of the PBI.
 	 * @throws NoValidValueException
 	 *             If the name for the PBI is not valid. Valid names are not
 	 *             null and have not only whitespace characters.
 	 */
 	public ProductBacklogItem(final String name, final ProductBacklog backlog)
 			throws UserException {
 		super();
 		this.backlogAssoc = new ToBacklogAssoc(this);
 		this.sprintAssoc = new ToSprintAssoc(this);
 		this.checkName(backlog, name); // Initiale Prüfung
 		this.setManDayCosts(0);
 		this.getBacklogAssoc().finalSet(backlog.getAssoc());
 	}
 
 	public abstract void accept(IProductBacklogItemVisitor visitor);
 
 	private void checkName(final ProductBacklog backlog, final String name)
 			throws NoValidValueException, DoubleDefinitionException {
 		if (name != null && name.trim().length() > 0) {
 			if (backlog!=null) {
 				for (final ProductBacklogItem item : backlog.getItems()) {
 					if (!item.equals(this) && item.getName().equals(name)) {
 						throw new DoubleDefinitionException(
 								TextConstants.DOUBLE_DEFINITION_PBI);
 					}
 				}
 			}
 			this.name = name;
 		} else {
 			throw new NoValidValueException(
 					TextConstants.MISSING_TEXT_ERROR);
 		}
 	}
 
 	// public void connectPBIToBacklog(ProductBacklog backlog){
 	// if(this.getBacklog()==null){
 	// this.getBacklogAssoc().finalSet(backlog.getAssoc());
 	// }
 	// }
 
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (!super.equals(obj)) {
 			return false;
 		}
 		if (this.getClass() != obj.getClass()) {
 			return false;
 		}
 		final ProductBacklogItem other = (ProductBacklogItem) obj;
 		if (this.backlogAssoc == null) {
 			if (other.backlogAssoc != null) {
 				return false;
 			}
 		} else if (!this.backlogAssoc.equals(other.backlogAssoc)) {
 			return false;
 		}
 		if (this.lastEditor == null) {
 			if (other.lastEditor != null) {
 				return false;
 			}
 		} else if (!this.lastEditor.equals(other.lastEditor)) {
 			return false;
 		}
 		if (this.manDayCosts == null) {
 			if (other.manDayCosts != null) {
 				return false;
 			}
 		} else if (!this.manDayCosts.equals(other.manDayCosts)) {
 			return false;
 		}
 		if (this.name == null) {
 			if (other.name != null) {
 				return false;
 			}
 		} else if (!this.name.equals(other.name)) {
 			return false;
 		}
 		if (this.sprintAssoc == null) {
 			if (other.sprintAssoc != null) {
 				return false;
 			}
 		} else if (!this.sprintAssoc.equals(other.sprintAssoc)) {
 			return false;
 		}
 		return true;
 	}
 
 	public ProductBacklog getBacklog() {
 		if (this.getBacklogAssoc().get() != null) {
 			return this.getBacklogAssoc().get().getElement();
 		}
 		return null;
 	}
 
 	protected ToBacklogAssoc getBacklogAssoc() {
 		return this.backlogAssoc;
 	}
 
 	public IPerson getLastEditor() {
 		return this.lastEditor;
 	}
 
	public final Integer getManDayCosts() {
 		return this.manDayCosts;
 	}
 
	public final String getName() {
 		return this.name;
 	}
 
 	public ISprint getSprint() {
 		if (this.getSprintAssoc().get() != null) {
 			return this.getSprintAssoc().get().getElement();
 		} else {
 			return null;
 		}
 	}
 
 	protected ToSprintAssoc getSprintAssoc() {
 		return this.sprintAssoc;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = this.indirectHashCode();
 		result = prime
 				* result
 				+ ((this.backlogAssoc == null) ? 0 : this.backlogAssoc
 						.hashCode());
 		result = prime * result
 				+ ((this.lastEditor == null) ? 0 : this.lastEditor.hashCode());
 		result = prime
 				* result
 				+ ((this.sprintAssoc == null) ? 0 : this.sprintAssoc.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean indirectEquals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (!super.equals(obj)) {
 			return false;
 		}
 		if (this.getClass() != obj.getClass()) {
 			return false;
 		}
 		final ProductBacklogItem other = (ProductBacklogItem) obj;
 		if (this.lastEditor == null) {
 			if (other.lastEditor != null) {
 				return false;
 			}
 		} else if (!this.lastEditor.equals(other.lastEditor)) {
 			return false;
 		}
 		if (this.manDayCosts == null) {
 			if (other.manDayCosts != null) {
 				return false;
 			}
 		} else if (!this.manDayCosts.equals(other.manDayCosts)) {
 			return false;
 		}
 		if (this.name == null) {
 			if (other.name != null) {
 				return false;
 			}
 		} else if (!this.name.equals(other.name)) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public int indirectHashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime
 				* result
 				+ ((this.manDayCosts == null) ? 0 : this.manDayCosts.hashCode());
 		result = prime * result
 				+ ((this.name == null) ? 0 : this.name.hashCode());
 		return result;
 	}
 
 	public void setLastEditor(final IPerson lastEditor) {
 		this.lastEditor = lastEditor;
 		this.notifyObservers();
 	}
 
 	/**
 	 * 
 	 * @param manDayCosts
 	 *            Values smaller 0 are not allow. 0 means not defined.
 	 * @throws NoValidValueException
 	 *             If the value is smaller 0!
 	 */
	public final void setManDayCosts(final Integer manDayCosts)
 			throws NoValidValueException {
 		if (manDayCosts != null && manDayCosts >= 0) {
 			this.manDayCosts = manDayCosts;
 			this.notifyObservers();
 		} else {
 			// TODO Textkonstante bauen
 			throw new NoValidValueException(
 					"Es muss eine gï¿½ltige Aufwandsschï¿½tzung in Manntagen (>=0) angegeben werden!");
 		}
 	}
 
 	/**
 	 * Changes the Name of the PBI.
 	 * 
 	 * @param name
 	 *            New Name of the PBI.
 	 * @throws NoValidValueException
 	 *             If the name for the PBI is not valid. Valid names are not
 	 *             null and have not only whitespace characters.
 	 */
	public final void setName(final String name) throws NoValidValueException,
 			DoubleDefinitionException, ConsistencyException {
 		if (this.getBacklog() != null) {
 			this.checkName(this.getBacklog(), name);
 		} else {
 			// TODO Textkonstante bauen!
 			throw new ConsistencyException(
 					"Das PBI muss zunächst mit dem Backlog verbunden werden");
 		}
 	}
 
 	/**
 	 * TODO Kommentar schreiben
 	 * 
 	 * @param sprint
 	 *            Null Value Means, that the PBI will be removed from the
 	 *            Sprint!
 	 * @throws NoSprintDefinedException
 	 *             , ConsistencyException
 	 */
 	public void setSprint(final ISprint sprint)
 			throws NoSprintDefinedException, ConsistencyException {
 		if (sprint != null) {
 			this.getBacklog().getProject().isSprintDefined(sprint);
 			this.getSprintAssoc().set(sprint.getToPBIAssoc());
 		} else {
 			this.getSprintAssoc().set(null);
 		}
 		this.notifyObservers();
 	}
 
 	@Override
 	public String toString() {
 		return "ProductBacklogItem [aufwand=" + this.manDayCosts + ", name="
 				+ this.name + "]";
 	}
 }
