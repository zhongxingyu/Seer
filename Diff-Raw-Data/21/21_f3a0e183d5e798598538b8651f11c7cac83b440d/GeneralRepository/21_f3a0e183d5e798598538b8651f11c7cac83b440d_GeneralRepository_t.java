 package ${package}.persistence.provider;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Set;
 
 import org.domdrides.entity.Entity;
 import org.hibernate.type.EntityType;
 
 public interface GeneralRepository {
 
 	public abstract <T> List<T> getAllAsList(
 			Class<? extends Entity<String>> entityClass);
 
 	public abstract Entity<? extends Serializable> add(Entity<? extends Serializable> entity);
 
 	public abstract boolean contains(
 			Entity entity);
 
 	public abstract Set<EntityType> getAll(
 			Class<? extends Entity<String>> entityClass);
 
 	public abstract <T> Entity<? extends Serializable> getById(Object id, Class<? extends Entity<? extends Serializable>> clazz);
 
 	public abstract void remove(EntityType entity);
 
	public abstract <T> Entity<? extends Serializable> update(
			Entity<? extends Serializable> entityClass);
	public abstract <T> Entity<? extends Serializable> getByName(String name, Class<? extends Entity<? extends Serializable>> clazz);
 
 	public abstract int size(Class<? extends Entity<String>> entityClass);
 
 }
