 package entityFramework;
 
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import utils.ResizableGrid;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.inject.*;
 
 public class EntityDatabase implements IEntityDatabase{
 
 	private final ComponentTypeManager typeManager;
 	private final Map<Integer, IEntity> entityMap;
 	private final ResizableGrid<IComponent> componentGrid;	
 	
 	
 	@Inject
 	public EntityDatabase(ComponentTypeManager manager) {
 		this.typeManager = manager;
		this.componentGrid = new ResizableGrid<IComponent>();
 		this.entityMap = new HashMap<Integer, IEntity>();
 	}
 	
 	@Override
 	public <T extends IComponent> T getComponent(int entityID,
 			Class<T> componentType) {
 		int componentID = typeManager.getTypeID(componentType);
 		return this.getComponent(entityID, componentID);
 	}
 
 	@SuppressWarnings("unchecked") //This Feature is just SOOO anoying
 	@Override
 	public <T extends IComponent> T getComponent(int entityID, int componentID) {
 		return (T)this.componentGrid.get(entityID, componentID);
 	}
 
 	@Override
 	public void setComponent(int entityID, IComponent component) {
 		int componentID = typeManager.getTypeID(component.getClass());	
 		this.setComponent(entityID, componentID, component);
 	}
 
 	@Override
 	public void setComponent(int entityID, int componentID, IComponent component) {	
 		this.componentGrid.set(entityID, componentID, component);
 		
 		BitSet bit = typeManager.getTypeBit(component.getClass());
 		IEntity entity = this.entityMap.get(entityID);
 		entity.addComponentBit(bit);
 	}
 
 	@Override
 	public void deleteComponent(int entityID,
 			Class<? extends IComponent> componentType) {
 		int componentID = typeManager.getTypeID(componentType);
 		this.deleteComponent(entityID, componentID);
 	}
 
 	@Override
 	public void deleteComponent(int entityID, int componentTypeID) {
 		this.componentGrid.set(entityID, componentTypeID, null);	
 		
 		BitSet bit = typeManager.getTypeBit(componentTypeID);
 		IEntity entity = this.entityMap.get(entityID);
 		entity.removeComponentBit(bit);
 	}
 
 	@Override
 	public int getComponentTypeID(Class<? extends IComponent> componentType) {
 		return this.typeManager.getTypeID(componentType);
 	}
 
 	@Override
 	public ImmutableSet<IComponent> getComponents(IEntity entity) {
 		return this.getComponents(entity.getUniqueID());
 	}
 
 	@Override
 	public ImmutableSet<IComponent> getComponents(int entityID) {
 		List<IComponent> components = this.componentGrid.getRow(entityID);
 		for (int i = components.size() - 1; i >= 0; i--) {
 			if(components.get(i) == null)
 				components.remove(i);
 		}
 		return ImmutableSet.copyOf(components);
 	}
 
 	@Override
 	public ImmutableSet<IEntity> getEntitysContainingComponent(
 			Class<? extends IComponent> componentType) {
 		int componentID = this.typeManager.getTypeID(componentType);
 		return this.getEntitysContainingComponent(componentID);
 	}
 
 	@Override
 	public ImmutableSet<IEntity> getEntitysContainingComponent(int componentID) {
 		List<IComponent> componentColumn = this.componentGrid.getCollumn(componentID); 
 		List<IEntity> entities = new ArrayList<IEntity>(componentColumn.size());
 		for (int i = 0; i < componentColumn.size(); i++) {
 			if(componentColumn.get(i) != null) {
 				entities.add(this.entityMap.get(i));
 			}
 		}
 		
 		return ImmutableSet.copyOf(entities);
 	}
 	
 
 	@SuppressWarnings("unchecked")
 	public ImmutableSet<IEntity> getEntitysContainingComponents(Class<? extends IComponent>... components) {
 		List<List<IComponent>> componentColumns = new ArrayList<>();
 		for (int i = 0; i < components.length; i++) {
 			int componentID = this.typeManager.getTypeID(components[i]);
 			componentColumns.add(this.componentGrid.getCollumn(componentID));
 		}
 		
 		List<IEntity> entities = new ArrayList<IEntity>();
 		for (int i = 0; i < componentColumns.get(0).size(); i++) {
 			boolean containsAll = true;
 			for (int j = 0; j < components.length; j++) {
 				if(componentColumns.get(j).get(i) == null) {
 					containsAll = false;
 					break;
 				}
 			}
 			if(containsAll) {
 				entities.add(this.entityMap.get(i));
 			}
 		}
 		
 		return ImmutableSet.copyOf(entities);
 	}
 
 	@Override
 	public void addEntity(IEntity entity) {
 		this.entityMap.put(entity.getUniqueID(), entity);	
 		this.componentGrid.ensureLargeEnough(entity.getUniqueID(), typeManager.getTypeCount());
 	}
 
 	@Override
 	public void removeEntity(IEntity entity) {
 		this.removeEntity(entity.getUniqueID());
 	}
 
 	@Override
 	public void removeEntity(int entityID) {
 
 		this.entityMap.get(entityID).clearBits();
 		this.entityMap.remove(entityID);
 		this.componentGrid.clearRow(entityID);
 	}
 
 	@Override
 	public IEntity getEntity(int entityID) {
 		return this.entityMap.get(entityID);
 	}
 
 	@Override
 	public int getEntityCount() {
 		return this.entityMap.size();
 	}
 
 	@Override
 	public void clear() {
 		for (IEntity entity : this.entityMap.values()) {
 			entity.clearBits();
 		}
 		this.entityMap.clear();
 		this.componentGrid.clear();
 	}
 	
 	@Override
 	public BitSet getComponentTypeBit(Class<? extends IComponent> componentType) {
 		return this.typeManager.getTypeBit(componentType);
 	}
 
 	@Override
 	public ImmutableSet<IEntity> getAllEntities() {
 		return ImmutableSet.copyOf(this.entityMap.values());
 	}
 
 	@Override
 	public void clearEntity(int entityID) {
 		this.entityMap.get(entityID).clearBits();
 		this.componentGrid.clearRow(entityID);
 	}
 }
