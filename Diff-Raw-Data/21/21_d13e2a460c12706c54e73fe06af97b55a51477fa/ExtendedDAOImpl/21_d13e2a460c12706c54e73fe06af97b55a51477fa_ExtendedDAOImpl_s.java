 package org.jooq.impl;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.jooq.ConfigurationExtended;
 import org.jooq.DAOExtended;
 import org.jooq.Field;
 import org.jooq.RecordMapper;
 import org.jooq.Table;
 import org.jooq.UpdatableRecord;
 
 public abstract class ExtendedDAOImpl<R extends UpdatableRecord<R>, P, T> implements DAOExtended<R, P, T> {
 	
 	private final DAOImpl<R, P, T> delegate;
 	private ConfigurationExtended configurationExtended;
 
 	public ExtendedDAOImpl(final Table<R> table, final Class<P> type) {
 		this(table, type, null);
 	}
 
 	protected ExtendedDAOImpl(final Table<R> table, final Class<P> type, final ConfigurationExtended configurationExtended) {
 		this.delegate = new DAOImpl<R, P, T>(table, type, configurationExtended) {
 
 			@Override
 			protected T getId(P object) {
 				return ExtendedDAOImpl.this.getId(object);
 			}
 			
 		};
 		this.configurationExtended = configurationExtended;
 	}
 	
 	@Override
 	public ConfigurationExtended configuration() {
 		return configurationExtended;
 	}
 	
 	public final void setConfiguration(ConfigurationExtended configuration) {
 		this.configurationExtended = configuration;
 		delegate.setConfiguration(configuration);
 	}
 	
     // ------------------------------------------------------------------------
     // XXX: DAOImpl override
     // ------------------------------------------------------------------------
 
 	public final void insert(Collection<P> objects) {
 		if(configuration().settingsExtended().isIdGenerated()) {
 			for(P object : objects) {
 				setId(object, configurationExtended.idGenerator().generate(Long.class));
 			}
 		}
 		delegate.insert(objects);
 	}
 	
     // ------------------------------------------------------------------------
     // XXX: Delegate calls
     // ------------------------------------------------------------------------
 	
 	public RecordMapper<R, P> mapper() {
 		return delegate.mapper();
 	}
 
	public final void insert(P object) {
		delegate.insert(object);
	}

	public final void insert(P... objects) {
		delegate.insert(objects);
	}

 	public final void update(P object) {
 		delegate.update(object);
 	}
 
 	public final void update(P... objects) {
 		delegate.update(objects);
 	}
 
 	public final void update(Collection<P> objects) {
 		delegate.update(objects);
 	}
 
 	public final void delete(P... objects) {
 		delegate.delete(objects);
 	}
 
 	public final void delete(Collection<P> objects) {
 		delegate.delete(objects);
 	}
 
 	public final void deleteById(T... ids) {
 		delegate.deleteById(ids);
 	}
 
 	public final void deleteById(Collection<T> ids) {
 		delegate.deleteById(ids);
 	}
 
 	public final boolean exists(P object) {
 		return delegate.exists(object);
 	}
 
 	public final boolean existsById(T id) {
 		return delegate.existsById(id);
 	}
 
 	public final long count() {
 		return delegate.count();
 	}
 
 	public final List<P> findAll() {
 		return delegate.findAll();
 	}
 
 	public final P findById(T id) {
 		return delegate.findById(id);
 	}
 
 	public final <Z> List<P> fetch(Field<Z> field, Z... values) {
 		return delegate.fetch(field, values);
 	}
 
 	public final <Z> P fetchOne(Field<Z> field, Z value) {
 		return delegate.fetchOne(field, value);
 	}
 
 	public final Table<R> getTable() {
 		return delegate.getTable();
 	}
 
 	public final Class<P> getType() {
 		return delegate.getType();
 	}
 	
     // ------------------------------------------------------------------------
     // XXX: Template methods for generated subclasses
     // ------------------------------------------------------------------------
 	
 	protected abstract T getId(P object);
 	
 	/**
 	 * Set a primary key value to the POJO
 	 * @param object
 	 * @param id
 	 */
 	protected abstract void setId(P object, Long id);
 	
 }
