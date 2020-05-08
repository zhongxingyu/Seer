 package com.blarg.gdx.entities;
 
 import com.badlogic.gdx.utils.*;
 import com.blarg.gdx.ReflectionUtils;
 import com.blarg.gdx.entities.systemcomponents.EntityPresetComponent;
 import com.blarg.gdx.entities.systemcomponents.InactiveComponent;
 import com.blarg.gdx.events.EventManager;
 
 public class EntityManager implements Disposable {
 	public final EventManager eventManager;
 
 	ObjectSet<Entity> entities;
 	ObjectMap<Class<? extends Component>, ObjectMap<Entity, Component>> componentStore;
 	ObjectMap<Class<? extends Component>, Component> globalComponents;
 	Array<ComponentSystem> componentSystems;
 	ObjectMap<Class<? extends EntityPreset>, EntityPreset> presets;
 
 	Pool<Entity> entityPool = new Pool<Entity>() {
 		@Override
 		protected Entity newObject() {
 			return new Entity(EntityManager.this);
 		}
 	};
 
 	ObjectMap<Entity, Component> empty;
 
 	public EntityManager(EventManager eventManager) {
 		if (eventManager == null)
 			throw new IllegalArgumentException("eventManager can not be null.");
 
 		this.eventManager = eventManager;
 		entities = new ObjectSet<Entity>();
 		componentStore = new ObjectMap<Class<? extends Component>, ObjectMap<Entity, Component>>();
 		globalComponents = new ObjectMap<Class<? extends Component>, Component>();
 		componentSystems = new Array<ComponentSystem>();
 		presets = new ObjectMap<Class<? extends EntityPreset>, EntityPreset>();
 
 		// possibly ugliness, but this allows us to return empty.keys() in getAllWith()
 		// when there are no entities with a given component, preventing the calling code
 		// from needing to check for null before a for-loop
 		empty = new ObjectMap<Entity, Component>();
 	}
 
 	/*** public ComponentSystem management */
 
 	public <T extends ComponentSystem> T addSubsystem(Class<T> componentSystemType) {
 		if (getSubsystem(componentSystemType) != null)
 			throw new UnsupportedOperationException("ComponentSystem of that type is already registered.");
 
 		T subsystem;
 		try {
 			subsystem = ReflectionUtils.instantiateObject(componentSystemType,
 			                                              new Class<?>[] { EntityManager.class, EventManager.class },
 			                                              new Object[] { this, eventManager });
 		} catch (Exception e) {
 			throw new IllegalArgumentException("Could not instantiate this type of ComponentSystem.", e);
 		}
 
 		componentSystems.add(subsystem);
 		return subsystem;
 	}
 
 	public <T extends ComponentSystem> T getSubsystem(Class<T> componentSystemType) {
 		int i = getSubsystemIndex(componentSystemType);
 		if (i == -1)
 			return null;
 		else
 			return componentSystemType.cast(componentSystems.get(i));
 	}
 
 	public <T extends ComponentSystem> void removeSubsystem(Class<T> componentSystemType) {
 		int i = getSubsystemIndex(componentSystemType);
 		if (i == -1)
 			return;
 
 		componentSystems.removeIndex(i);
 	}
 
 	public void removeAllSubsystems() {
 		for (int i = 0; i < componentSystems.size; ++i)
 			componentSystems.get(i).dispose();
 		componentSystems.clear();
 	}
 
 	/*** public EntityPreset management */
 
 	public <T extends EntityPreset> void addPreset(Class<T> presetType) {
 		if (presets.containsKey(presetType))
 			throw new UnsupportedOperationException("EntityPreset of that type is already registered.");
 
 		T preset;
 		try {
 			preset = ReflectionUtils.instantiateObject(presetType,
 			                                           new Class<?>[] { EntityManager.class },
 			                                           new Object[] { this });
 		} catch (Exception e) {
 			throw new IllegalArgumentException("Could not instantiate this type of EntityPreset.", e);
 		}
 
 		presets.put(presetType, preset);
 	}
 
 	public <T extends EntityPreset> void removePreset(Class<T> presetType) {
 		EntityPreset preset = presets.get(presetType);
 		presets.remove(presetType);
 		if (Disposable.class.isInstance(preset))
 			((Disposable)preset).dispose();
 	}
 
 	public void removeAllPresets() {
 		for (ObjectMap.Entry<Class<? extends EntityPreset>, EntityPreset> i : presets.entries()) {
 			if (Disposable.class.isInstance(i.value))
 				((Disposable)i.value).dispose();
 		}
 		presets.clear();
 	}
 
 	/*** public Entity management ***/
 
 	public Entity add() {
 		Entity entity = entityPool.obtain();
 		entities.add(entity);
 		return entity;
 	}
 
 	public <T extends EntityPreset> Entity addUsingPreset(Class<T> presetType) {
 		return addUsingPreset(presetType, null);
 	}
 
 	public <T extends EntityPreset> Entity addUsingPreset(Class<T> presetType, EntityPreset.CreationArgs args) {
 		EntityPreset preset = presets.get(presetType);
 		if (preset == null)
 			throw new IllegalArgumentException("Cannot add entity using an unregistered EntityPreset.");
 
 		Entity entity = preset.create(args);
 		entity.add(EntityPresetComponent.class).presetType = presetType;
 
 		return entity;
 	}
 
 	public <T extends Component> Entity getFirstWith(Class<T> componentType) {
 		ObjectMap<Entity, Component> componentEntities = componentStore.get(componentType);
 		if (componentEntities == null)
 			return null;
 
 		if (componentEntities.size > 0)
 			return componentEntities.keys().next();
 		else
 			return null;
 	}
 
 	public <T extends Component> ObjectMap.Keys<Entity> getAllWith(Class<T> componentType) {
 		ObjectMap<Entity, Component> componentEntities = componentStore.get(componentType);
 		if (componentEntities == null)
 			return empty.keys();   // calling code won't need to check for null
 		else
 			return componentEntities.keys();
 	}
 
 	public <T extends EntityPreset> void getAllCreatedWithPreset(Class<T> presetType, Array<Entity> outMatchingEntities) {
 		if (outMatchingEntities == null)
 			throw new IllegalArgumentException("Must supply an Array object to store the matching entities in.");
 
 		for (Entity i : getAllWith(EntityPresetComponent.class)) {
 			EntityPresetComponent entityPreset = i.get(EntityPresetComponent.class);
 			if (presetType.isAssignableFrom(entityPreset.presetType))
 				outMatchingEntities.add(i);
 		}
 	}
 
 	public void remove(Entity entity) {
 		if (entity == null)
 			throw new IllegalArgumentException("entity can not be null.");
 
 		removeAllComponentsFrom(entity);
 		entities.remove(entity);
 
 		entityPool.free(entity);
 	}
 
 	public void removeAll() {
 		for (Entity i : entities) {
 			removeAllComponentsFrom(i);
 			entityPool.free(i);
 		}
 
 		entities.clear();
 	}
 
 	public boolean isValid(Entity entity) {
 		if (entity == null)
 			return false;
 		else
 			return entities.contains(entity);
 	}
 
 	/*** public Entity Component management ***/
 
 	public <T extends Component> T addComponent(Class<T> componentType, Entity entity) {
 		if (getComponent(componentType, entity) != null)
 			throw new UnsupportedOperationException("Component of that type has been added to this entity already.");
 
 		// find the component-to-entity list for this component type, or create it if it doesn't exist yet
 		ObjectMap<Entity, Component> componentEntities = componentStore.get(componentType);
 		if (componentEntities == null) {
 			componentEntities = new ObjectMap<Entity, Component>();
 			componentStore.put(componentType, componentEntities);
 		}
 
 		T component = Pools.obtain(componentType);
 
 		componentEntities.put(entity, component);
 		return componentType.cast(component);
 	}
 
 	public <T extends Component> T getComponent(Class<T> componentType, Entity entity) {
 		ObjectMap<Entity, Component> componentEntities = componentStore.get(componentType);
 		if (componentEntities == null)
 			return null;
 
 		Component existing = componentEntities.get(entity);
 		if (existing == null)
 			return null;
 		else
 			return componentType.cast(existing);
 	}
 
 	public <T extends Component> void removeComponent(Class<T> componentType, Entity entity) {
 		ObjectMap<Entity, Component> componentEntities = componentStore.get(componentType);
 		if (componentEntities == null)
 			return;
 
 		Component component = componentEntities.remove(entity);
		Pools.free(component);
 	}
 
 	public <T extends Component> boolean hasComponent(Class<T> componentType, Entity entity) {
 		ObjectMap<Entity, Component> componentEntities = componentStore.get(componentType);
 		if (componentEntities == null)
 			return false;
 
 		return componentEntities.containsKey(entity);
 	}
 
 	public void getAllComponentsFor(Entity entity, Array<Component> list) {
 		if (list == null)
 			throw new IllegalArgumentException("list can not be null.");
 
 		for (ObjectMap.Entry<Class<? extends Component>, ObjectMap<Entity, Component>> i : componentStore.entries()) {
 			ObjectMap<Entity, Component> entitiesWithComponent = i.value;
 			Component component = entitiesWithComponent.get(entity);
 			if (component != null)
 				list.add(component);
 		}
 	}
 
 	/*** global component management ***/
 
 	public <T extends Component> T addGlobal(Class<T> componentType) {
 		if (getGlobal(componentType) != null)
 			throw new UnsupportedOperationException("Global component of that type has been added already.");
 
 		T component = Pools.obtain(componentType);
 
 		globalComponents.put(componentType, component);
 		return componentType.cast(component);
 	}
 
 	public <T extends Component> T getGlobal(Class<T> componentType) {
 		Component existing = globalComponents.get(componentType);
 		if (existing == null)
 			return null;
 		else
 			return componentType.cast(existing);
 	}
 
 	public <T extends Component> void removeGlobal(Class<T> componentType) {
 		Component component = globalComponents.remove(componentType);
 		Pools.free(component);
 	}
 
 	public <T extends Component> boolean hasGlobal(Class<T> componentType) {
 		return globalComponents.containsKey(componentType);
 	}
 
 	public void removeAllGlobals() {
 		for (ObjectMap.Entry<Class<? extends Component>, Component> i : globalComponents.entries())
 			Pools.free(i.value);
 		globalComponents.clear();
 	}
 
 	/*** events ***/
 
 	public void onAppResume() {
 		for (int i = 0; i < componentSystems.size; ++i)
 			componentSystems.get(i).onAppResume();
 	}
 
 	public void onAppPause() {
 		for (int i = 0; i < componentSystems.size; ++i)
 			componentSystems.get(i).onAppPause();
 	}
 
 	public void onResize() {
 		for (int i = 0; i < componentSystems.size; ++i)
 			componentSystems.get(i).onResize();
 	}
 
 	public void onRender(float delta) {
 		for (int i = 0; i < componentSystems.size; ++i)
 			componentSystems.get(i).onRender(delta);
 	}
 
 	public void onUpdate(float delta) {
 		for (Entity i : getAllWith(InactiveComponent.class))
 			remove(i);
 
 		for (int i = 0; i < componentSystems.size; ++i)
 			componentSystems.get(i).onUpdate(delta);
 	}
 
 	/*** private Entity/Component management ***/
 
 	private void removeAllComponentsFrom(Entity entity) {
 		if (entity == null)
 			throw new IllegalArgumentException("entity can not be null.");
 
 		for (ObjectMap.Entry<Class<? extends Component>, ObjectMap<Entity, Component>> i : componentStore.entries()) {
 			ObjectMap<Entity, Component> entitiesWithComponent = i.value;
 			Component component = entitiesWithComponent.get(entity);
 			if (component != null)
 				Pools.free(component);
 			entitiesWithComponent.remove(entity);
 		}
 	}
 
 	private <T extends ComponentSystem> int getSubsystemIndex(Class<T> componentSystemType) {
 		for (int i = 0; i < componentSystems.size; ++i) {
 			if (componentSystems.get(i).getClass() == componentSystemType)
 				return i;
 		}
 		return -1;
 	}
 
 	@Override
 	public void dispose() {
 		removeAll();
 		removeAllGlobals();
 		removeAllPresets();
 		removeAllSubsystems();
 	}
 }
