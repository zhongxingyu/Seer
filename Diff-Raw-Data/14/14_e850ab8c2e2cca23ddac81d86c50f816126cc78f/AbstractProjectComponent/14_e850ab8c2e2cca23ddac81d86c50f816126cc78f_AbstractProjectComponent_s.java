 package fiuba.mda.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.google.common.base.Optional;
 
 import fiuba.mda.utilities.SimpleEvent;
 import fiuba.mda.utilities.SimpleEvent.Observer;
 
 /**
  * Base abstract class useful for implementing {@link ProjectComponent}
  * containing default behavior for all methods
  */
 public abstract class AbstractProjectComponent implements ProjectComponent {
 	private final SimpleEvent<ProjectComponent> hierarchyChangedEvent = new SimpleEvent<ProjectComponent>(
 			this);
 
 	private String name;
 
 	private ProjectComponent parent;
 
 	private final List<ProjectComponent> children = new ArrayList<>();
 
 	private Observer<ProjectComponent> onChildrenHierarchyChanged = new Observer<ProjectComponent>() {
 		@Override
 		public void notify(ProjectComponent observable) {
 			hierarchyChangedEvent.raise();
 		}
 	};
 
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
 			return getParent().locateOwningPackage().qualifiedNameFor(this);
 		}
 
 	}
 
 	@Override
 	public List<ProjectComponent> getChildren() {
 		return Collections.unmodifiableList(children);
 	}
 	
 	@Override
 	public boolean hasChildren() {
 		return !children.isEmpty();
 	}
 	
 	@Override
 	public void addChildren(final ProjectComponent component) {
 		if (isLeaf()) {
 			throw new RuntimeException("Unable to add children to leaf component");		
 		}
 		component.setParent(this);
 		component.hierarchyChangedEvent().observe(onChildrenHierarchyChanged);
 		this.children.add(component);
 		this.hierarchyChangedEvent.raise();
 	}
 
     @Override
     public void deleteChildrenFromList(ProjectComponent o){
         children.remove(o);
     }
 
     @Override
     public void removeChildrens() {
 
         List<ProjectComponent> componentsToDelete = new ArrayList<>();
 
         for (ProjectComponent component : this.getChildren()){
             component.removeChildrens();
             componentsToDelete.add(component);
         }
 
         for (ProjectComponent component : componentsToDelete){
             this.deleteChildrenFromList(component);
             component.setParent(null);
             component.hierarchyChangedEvent().unobserve(onChildrenHierarchyChanged);
         }
 
 
         this.hierarchyChangedEvent().raise();
 
 
     }
 
     @Override
     public void removeChildren(final ProjectComponent component) {
 
         if (!this.children.contains(component)) return;
 
         component.removeChildrens();
         this.deleteChildrenFromList(component);
         component.setParent(null);
         component.hierarchyChangedEvent().unobserve(onChildrenHierarchyChanged);
         this.hierarchyChangedEvent().raise();
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
 }
