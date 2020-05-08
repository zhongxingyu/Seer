 package entityFramework;
 
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.Map;
 
 public abstract class EntitySystem implements IEntitySystem{
 
 	private final IEntityWorld world;
 	private final Map<Integer, IEntity> entities;
 	private final BitSet componentBits;
 	private boolean enabled;
 	private int processingOrder;
 	
 	
 	protected final IEntityWorld getWorld() {
 		return this.world;
 	}
 	
 	protected final Map<Integer, IEntity> getEntities() {
 		return this.entities;
 	}
 	
 	@Override
 	public final boolean getEnabled() {
 		return this.enabled;
 	}
 	
 	@Override
 	public final void setEnabled(boolean value) {
 		this.enabled = value;
 	}
 	
 	@Override
 	public final int getProcessingOrder() {
 		return this.processingOrder;
 	}
 	
 	@Override
 	public final void setProcessingOrder(int order) {
 		this.processingOrder = order;
 	}
 	
 	@SafeVarargs
 	protected EntitySystem(IEntityWorld world, Class<? extends IComponent> ...componentsClasses) {
 		this.world = world;
 		this.entities = new HashMap<Integer, IEntity>();
 		this.componentBits = new BitSet();
 		this.composeBits(componentsClasses);
 		this.enabled = true;
 	}
 	
 	private void composeBits(Class<? extends IComponent>[] componentsClasses) {
 		IEntityDatabase database = this.world.getDatabase();
 		for (Class<? extends IComponent> componentClass : componentsClasses) {
 			BitSet componentBit = database.getComponentTypeBit(componentClass);
 			this.componentBits.or(componentBit);
 		}
 	}
 
 	@Override
 	public void entityChanged(IEntity entity) {
 		BitSet changedSet = (BitSet)this.componentBits.clone();
 		changedSet.and(entity.getComponentBits());

 		
 		if(changedSet.equals(this.componentBits)) {
 			if(!this.entities.containsKey(entity.getUniqueID())) {
 				this.entities.put(entity.getUniqueID(), entity);
 			}
 		} else {
 			this.entities.remove(entity.getUniqueID());
 		}
 		
 	}
 
 	@Override
 	public void entityDestroyed(IEntity entity) {
 		this.entities.remove(entity);
 	}
 	
 	@Override
 	public abstract void process();
 }
