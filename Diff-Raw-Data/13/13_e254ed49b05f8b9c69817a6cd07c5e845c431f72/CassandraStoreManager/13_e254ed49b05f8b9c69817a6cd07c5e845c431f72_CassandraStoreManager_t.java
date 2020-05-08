 package com.datastax.hectorjpa.store;
 
import java.lang.reflect.Modifier;
 import java.util.BitSet;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import me.prettyprint.hector.api.mutation.Mutator;
 
 import org.apache.openjpa.abstractstore.AbstractStoreManager;
 import org.apache.openjpa.conf.OpenJPAConfiguration;
 import org.apache.openjpa.kernel.FetchConfiguration;
 import org.apache.openjpa.kernel.OpenJPAStateManager;
 import org.apache.openjpa.kernel.PCState;
 import org.apache.openjpa.kernel.QueryLanguages;
 import org.apache.openjpa.kernel.StoreQuery;
 import org.apache.openjpa.kernel.exps.ExpressionParser;
 import org.apache.openjpa.lib.rop.ResultObjectProvider;
 import org.apache.openjpa.meta.ClassMetaData;
 import org.apache.openjpa.util.UnsupportedException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.datastax.hectorjpa.query.CassandraStoreQuery;
 
 public class CassandraStoreManager extends AbstractStoreManager {
 
 	private static Logger log = LoggerFactory
 			.getLogger(CassandraStoreManager.class);
 
 	private CassandraStore cassandraStore;
 
 	@Override
 	public ResultObjectProvider executeExtent(ClassMetaData cMetaData,
 			boolean useSubClasses, FetchConfiguration fetchConfiguration) {
 		// cMetaData is essentially the query construct
 		// the ResultObjectProvider runs the query
 		if (log.isDebugEnabled()) {
 			log.debug(
 					"in executeExtent with ClassMetaData {}: useSubClasses: {} and fetchConfiguration: {}",
 					new Object[] { cMetaData, useSubClasses, fetchConfiguration });
 		}
 
 		// ask the store for all ObjectDatas for the given type; this
 		// actually gives us all instances of the base class of the type
 		// CFMetaData
 		// ObjectData[] datas = _store.getData(meta);
 		// get the Class
 		// Class candidate = meta.getDescribedType();
 
 		// create a list of the corresponding persistent objects that
 		// match the type and subclasses criteria
 		/*
 		 * List pcs = new ArrayList(datas.length); for (int i = 0; i <
 		 * datas.length; i++) { // does this instance belong in the extent?
 		 * Class c = datas[i].getMetaData().getDescribedType(); if (c !=
 		 * candidate && (!subclasses || !candidate.isAssignableFrom(c)))
 		 * continue;
 		 * 
 		 * // look up the pc instance for the data, passing in the data // as
 		 * well so that we can take advantage of the fact that we've // already
 		 * looked it up. note that in the store manager's // initialize(),
 		 * load(), etc methods we check for this data // being passed through
 		 * and save ourselves a trip to the store // if it is present; this is
 		 * particularly important in systems // where a trip to the store can be
 		 * expensive. pcs.add(ctx.find(datas[i].getId(), fetch, null, datas[i],
 		 * 0)); } return new ListResultObjectProvider(pcs);
 		 */
 		return null;
 	}
 
 	@Override
   public StoreQuery newQuery(String language) {
 	  ExpressionParser ep = QueryLanguages.parserForLanguage(language);
 	  
 	  if(ep == null){
 	    throw new  UnsupportedException(language);
 	  }
 
 	  CassandraStoreConfiguration conf = ((CassandraStoreConfiguration)getContext().getConfiguration());
 
 	  
     return new CassandraStoreQuery(ep,  conf.getMetaCache(), cassandraStore);
   }
 
   @Override
 	public Class<?> getManagedType(Object oid) {
 		return cassandraStore.getDataStoreId(oid, this.getContext() );
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected Collection flush(Collection pNew, Collection pNewUpdated,
 			Collection pNewFlushedDeleted, Collection pDirty,
 			Collection pDeleted) {
 		/*
 		 * defn. of above arguments --------------------------- pNew - Objects
 		 * that should be added to the store, and that have not previously been
 		 * flushed. pNewUpdated - New objects that have been modified since they
 		 * were initially flushed. These were in persistentNew in an earlier
 		 * flush invocation. pNewFlushedDeleted - New objects that have been
 		 * deleted since they were initially flushed. These were in
 		 * persistentNew in an earlier flush invocation. pDirty - Objects that
 		 * were loaded from the data store and have since been modified.
 		 * pDeleted - Objects that were loaded from the data store and have
 		 * since been deleted. These may have been in a previous flush
 		 * invocation's persistentDirty list.
 		 */
 		// _updates = new ArrayList(pNew.size() + pDirty.size());
 		// _deletes = new ArrayList(pDeleted.size());
 		long clock = cassandraStore.getClock();
 
 		Mutator<?> mutator = cassandraStore.createMutator();
 
 		writeEntities(pNew, mutator, clock);
 		writeEntities(pNewUpdated, mutator, clock);
 		writeEntities(pDirty, mutator, clock);
 
 		deleteEntities(pNewFlushedDeleted, mutator, clock);
 		deleteEntities(pDeleted, mutator, clock);
 
 		mutator.execute();
 
 		return null;
 	}
 
 	/**
 	 * Write all entities in the collection
 	 * 
 	 * @param writes
 	 * @param mutator
 	 * @param clock
 	 */
 	private void writeEntities(Collection writes, Mutator mutator, long clock) {
 
 		OpenJPAStateManager sm = null;
 
 		for (Iterator itr = writes.iterator(); itr.hasNext();) {
 			sm = (OpenJPAStateManager) itr.next();
 			cassandraStore.storeObject(mutator, sm, sm.getDirty(), clock);
 		}
 
 	}
 
 	/**
 	 * Delete all entities in the collection
 	 * 
 	 * @param deletes
 	 * @param mutator
 	 * @param clock
 	 */
 	private void deleteEntities(Collection deletes, Mutator mutator, long clock) {
 		for (Iterator itr = deletes.iterator(); itr.hasNext();) {
 			// create new object data for instance
 			OpenJPAStateManager sm = (OpenJPAStateManager) itr.next();
 			cassandraStore.removeObject(mutator, sm, clock);
 
 		}
 	}
 
 	@Override
 	public boolean initialize(OpenJPAStateManager stateManager,
 			PCState pcState, FetchConfiguration fetchConfiguration, Object obj) {
 
	  //if it's an abstract type, we couldn't find it in the datastore, just return false
	  Class<?> type = stateManager.getMetaData().getDescribedType();
	  
	  if(Modifier.isAbstract(type.getModifiers())){
	    return false;
	  }
	  
	  
 		log.debug("In initialize operation...");
		stateManager.initialize(type,
 				pcState);
 		return cassandraStore.getObject(stateManager,
 				stateManager.getUnloaded(fetchConfiguration));
 
 	}
 
 	@Override
 	public boolean load(OpenJPAStateManager stateManager, BitSet fields,
 			FetchConfiguration fetch, int lockLevel, Object edata) {
 		log.debug("In load operation...");
 		// return cassandraStore.getObject(stateManager, fields);
 
 		// load is called to fill in additional information not retrieved from
 		// initialize call above
 		return cassandraStore.getObject(stateManager, fields);
 
 	}
 
 	@Override
 	public boolean exists(OpenJPAStateManager stateManager, Object edata) {
 		log.debug("In CSM.exists()");
 		return cassandraStore.exists(stateManager);
 	}
 
 	@Override
 	public boolean isCached(List<Object> oids, BitSet edata) {
 		log.debug("In CSM.isCached()");
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public int compareVersion(OpenJPAStateManager state, Object v1, Object v2) {
 		log.debug("in CSM.compareVersion");
 		return super.compareVersion(state, v1, v2);
 	}
 
 	@Override
 	public boolean syncVersion(OpenJPAStateManager sm, Object edata) {
 		// TODO Auto-generated method stub
 		log.debug("in CSM.syncVersion");
 		return super.syncVersion(sm, edata);
 	}
 
 	@Override
 	protected void open() {
 		OpenJPAConfiguration conf = ctx.getConfiguration();
 		// TODO
 		// encapsulate into CassandraStore or similar (should take
 		// CassandraStoreConfig as an argg
 		// cluster =
 		// HFactory.getCluster(conf.getValue("me.prettyprint.hom.clusterName").getOriginalValue());
 		// keyspace =
 		// HFactory.createKeyspace(conf.getValue("me.prettyprint.hom.keyspace").getOriginalValue(),
 		// cluster);
 
 		cassandraStore = new CassandraStore((CassandraStoreConfiguration) conf);
 		cassandraStore.open();
 		log.debug("in CSM.open()");
 	}
 
 	protected Collection getUnsupportedOptions() {
 		Collection c = super.getUnsupportedOptions();
 
 		// remove options we do support but the abstract store doesn't
 		// c.remove(OpenJPAConfiguration.OPTION_ID_DATASTORE);
 		// c.remove(OpenJPAConfiguration.OPTION_OPTIMISTIC);
 
 		// and add some that we don't support but the abstract store does
 		// TODO take these out one by one
 //		c.add(OpenJPAConfiguration.OPTION_EMBEDDED_RELATION);
 //		c.add(OpenJPAConfiguration.OPTION_EMBEDDED_COLLECTION_RELATION);
 //		c.add(OpenJPAConfiguration.OPTION_EMBEDDED_MAP_RELATION);
 		return c;
 	}
 
 	@Override
 	protected OpenJPAConfiguration newConfiguration() {
 
 		CassandraStoreConfiguration conf = new CassandraStoreConfiguration();
 
 		if (log.isDebugEnabled()) {
 			log.debug("In newConfiguration with conf: {}",
 					conf.toProperties(true));
 		}
 
 		return conf;
 	}
 
 }
 
 /*
  * NOTES - OpenJPAStateManager holds the 'state' of an Entity instance -
  * ClassMetaData holds the details of the persistable Class
  * 
  * Consider using ClassCacheMgr as a MetaDataFactory
  * http://openjpa.apache.org/builds
  * /2.0.1/apache-openjpa-2.0.1/docs/manual/ref_guide_meta.html
  * 
  * Consider rolling a custom FetchPlan (or FetchConfiguration?) for CL
  * 
  * Returns the OpenJPAId.getType() returns Entity class:
  * log.debug("OID class name: {}",((OpenJPAId)idObj).getType().getName());
  */
