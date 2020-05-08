 package org.fuzzydb.spring.repository;
 
import org.fuzzydb.client.DataOperations;
 import org.fuzzydb.client.Ref;
 
 public class RawIdPersistenceHelper<I> implements IdPersistenceHelper<org.fuzzydb.client.Ref<I>, I> {
 
	private DataOperations persister;

	public RawIdPersistenceHelper(DataOperations persister) {
		this.persister = persister;
	}

 	@Override
 	public boolean exists(Ref<I> id) {
 		return findEntityById(id) != null;
 	}
 
 	@Override
 	public I findEntityById(Ref<I> id) {
 		return persister.retrieve(id);
 	}
 
 	@Override
 	public Ref<I> toInternalId(Ref<I> id) {
 		return id;
 	}
 
 	@Override
 	public Ref<I> toExternalId(Ref<I> id) {
 		return id;
 	}
 
 }
