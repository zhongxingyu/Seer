 package fiuba.mda.model;
 
 import com.google.common.base.Optional;
 
 import fiuba.mda.utilities.SimpleEvent;
 
 /**
  * Base abstract class useful for implementing {@link ProjectComponent}
  * containing default behavior for all methods
  */
 public abstract class AbstractProjectComponent implements ProjectComponent {
 	protected final SimpleEvent<ProjectComponent> hierarchyChangedEvent = new SimpleEvent<ProjectComponent>(
 			this);
 
 	private final SimpleEvent<ProjectComponent> removedEvent = new SimpleEvent<ProjectComponent>(
 			this);
 
 	private String name;
 
 	private ProjectComponent parent;
 
 	/**
 	 * Creates a new {@link AbstractProjectComponent} instance
 	 * 
 	 * @param name
 	 *            the name which represents the project component
 	 */
 	public AbstractProjectComponent(final String name) {
 		this.name = name;
 	}
 
 	@Override
 	public boolean isNewNameInConflict(ProjectComponent parent, String newName) {
 		// If this is a root component, it can have any name as it will never
 		// conflict with any siblings
 		if (isRoot()) {
 			return false;
 		}
 
 		Optional<ProjectComponent> conflict = parent.findChildrenNamed(newName);
 
 		// There is a conflict if there is a component with the same name, but
 		// only if the conflicting component isn't this one
 		return conflict.isPresent() && conflict.get() != this;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void setName(final String name) {
 		this.name = name;
 		this.hierarchyChangedEvent.raise();
 	}
 
 	@Override
 	public String getQualifiedName() {
 		if (isRoot()) {
 			return getName();
 		} else {
            String s = this.getName();
            ProjectComponent parent = getParent();
            while (parent != null){
                s = parent.getName() + "." + s;
                if (parent.isRoot()) parent = null;
                else parent = parent.getParent();
            }
            s = s.replace(" ","_");
            return s;
 		}
 
 	}
 
 	@Override
 	public ProjectComponent getParent() {
 		if (isRoot()) {
 			throw new RuntimeException("This is a root component");
 		}
 		return parent;
 	}
 
 	@Override
 	public void setParent(ProjectComponent parent) {
 		this.parent = parent;
 	}
 
 	@Override
 	public void removeFromHierarchy() {
 		parent = null;
 		removedEvent.raise();
 	}
 
 	@Override
 	public boolean isRoot() {
 		return parent == null;
 	}
 
 	@Override
 	public ModelPackage locateOwningPackage() {
 		return getParent().locateOwningPackage();
 	}
 
 	@Override
 	public Optional<ModelAspect> locateAspect(final String name) {
 		return Optional.absent();
 	}
 
 	@Override
 	public SimpleEvent<ProjectComponent> hierarchyChangedEvent() {
 		return hierarchyChangedEvent;
 	}
 
 	@Override
 	public SimpleEvent<ProjectComponent> removedEvent() {
 		return removedEvent;
 	}
 }
