 package net.nan21.dnet.core.presenter.service;
 
 import java.io.File;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.eclipse.persistence.config.HintValues;
 import org.eclipse.persistence.config.QueryHints;
 import org.eclipse.persistence.jpa.JpaQuery;
 import org.eclipse.persistence.queries.Cursor;
 import org.springframework.util.Assert;
 import org.springframework.util.StringUtils;
 
 import net.nan21.dnet.core.api.action.IDsExport;
 import net.nan21.dnet.core.api.action.IQueryBuilder;
 import net.nan21.dnet.core.api.action.SortToken;
 import net.nan21.dnet.core.api.converter.IDsConverter;
 import net.nan21.dnet.core.api.marshall.IDsMarshaller;
 import net.nan21.dnet.core.api.model.IModelWithClientId;
 import net.nan21.dnet.core.api.model.IModelWithId;
 import net.nan21.dnet.core.api.service.IEntityService;
 import net.nan21.dnet.core.api.session.Session;
 import net.nan21.dnet.core.presenter.action.DsCsvLoader;
 import net.nan21.dnet.core.presenter.action.DsCsvLoaderResult;
 import net.nan21.dnet.core.presenter.action.QueryBuilderWithJpql;
 import net.nan21.dnet.core.presenter.converter.AbstractDsConverter;
 import net.nan21.dnet.core.presenter.converter.BaseDsConverter;
 import net.nan21.dnet.core.presenter.exception.ActionNotSupportedException;
 import net.nan21.dnet.core.presenter.marshaller.JsonMarshaller;
 import net.nan21.dnet.core.presenter.model.AbstractDsModel;
 import net.nan21.dnet.core.presenter.model.DsDescriptor;
 import net.nan21.dnet.core.presenter.model.EmptyParam;
 import net.nan21.dnet.core.presenter.model.RpcDefinition;
 import net.nan21.dnet.core.presenter.model.ViewModelDescriptorManager;
 
 public abstract class AbstractDsService<M, F, P, E> extends AbstractDsProcessor {
 
 	protected boolean noInsert = false;
 	protected boolean noUpdate = false;
 	protected boolean noDelete = false;
 
 	protected Class<M> modelClass;
 	protected Class<F> filterClass;
 	protected Class<P> paramClass;
 	protected Class<E> entityClass;
 	protected Class<?> converterClass; // ? extends AbstractDsConverter<M, E>
 	protected Class<?> queryBuilderClass;
 	protected IDsConverter<M, E> converter;
 
 	private DsDescriptor<M> descriptor;
 
 	private IEntityService<E> entityService;
 	//
 	// private List<IEntityServiceFactory> entityServiceFactories;
 
 	private Map<String, RpcDefinition> rpcData = new HashMap<String, RpcDefinition>();
 	private Map<String, RpcDefinition> rpcFilter = new HashMap<String, RpcDefinition>();
 
 	// ======================== Find ===========================
 
 	public Long count(F filter, P params, IQueryBuilder<M, F, P> builder)
 			throws Exception {
 		QueryBuilderWithJpql<M, F, P> bld = (QueryBuilderWithJpql<M, F, P>) builder;
 		Object count = bld.createQueryCount().getSingleResult();
 		if (count instanceof Integer) {
 			return ((Integer) count).longValue();
 		} else {
 			return (Long) count;
 		}
 	}
 
 	protected void preFind(F filter, P params, IQueryBuilder<M, F, P> builder)
 			throws Exception {
 	}
 
 	public List<M> find(F filter, P params, List<SortToken> sortTokens,
 			int resultStart, int resultSize) throws Exception {
 		QueryBuilderWithJpql<M, F, P> builder = (QueryBuilderWithJpql<M, F, P>) this
 				.createQueryBuilder();
 		builder.addFetchLimit(resultStart, resultSize);
 		builder.addSortInfo(sortTokens);
 		return this.find(filter, params, builder);
 	}
 
 	public List<M> find(F filter, P params, int resultStart, int resultSize)
 			throws Exception {
 		QueryBuilderWithJpql<M, F, P> builder = (QueryBuilderWithJpql<M, F, P>) this
 				.createQueryBuilder();
 		builder.addFetchLimit(resultStart, resultSize);
 		return this.find(filter, params, builder);
 	}
 
 	public List<M> find(F filter, P params) throws Exception {
 		QueryBuilderWithJpql<M, F, P> builder = (QueryBuilderWithJpql<M, F, P>) this
 				.createQueryBuilder();
 		return this.find(filter, params, builder);
 	}
 
 	public List<M> find(F filter) throws Exception {
 		QueryBuilderWithJpql<M, F, P> builder = (QueryBuilderWithJpql<M, F, P>) this
 				.createQueryBuilder();
 		return this.find(filter, this.paramClass.newInstance(), builder);
 	}
 
 	public List<M> find(F filter, P params, IQueryBuilder<M, F, P> builder)
 			throws Exception {
 		QueryBuilderWithJpql<M, F, P> bld = null;
 		if (builder != null) {
 			bld = (QueryBuilderWithJpql<M, F, P>) builder;
 		} else {
 			bld = (QueryBuilderWithJpql<M, F, P>) this.createQueryBuilder();
 		}
 
 		if(filter!=null) {
 			bld.setFilter(filter);
 		} else {
 			bld.setFilter(this.filterClass.newInstance());
 		}
 		if (params != null) {
 			bld.setParams(params);
 		} else {
 			bld.setParams(this.paramClass.newInstance());
 		}
 		
 
 		List<M> result = new ArrayList<M>();
 
 		List<E> list = bld.createQuery().setFirstResult(bld.getResultStart())
 				.setMaxResults(bld.getResultSize()).getResultList();
 		for (E e : list) {
 			M m = this.getModelClass().newInstance();
 			this.getConverter().entityToModel(e, m);
 			result.add(m);
 		}
 		return result;
 	}
 
 	protected void postFind(F filter, P params, IQueryBuilder<M, F, P> builder)
 			throws Exception {
 	}
 
 	public M findById(Object id) throws Exception {
 		return this.findById(id, this.getParamClass().newInstance());
 	}
 
 	public M findById(Object id, P params) throws Exception {
		Method setter = this.getFilterClass().getMethod("setId",
 				Object.class);
 		F filter = this.getFilterClass().newInstance();
 		setter.invoke(filter, id);
 		List<M> result = this.find(filter, params, null);
 		if (result.size() == 0) {
 			return null;
 		} else {
 			return result.get(0);
 		}
 	}
 
 	public List<M> findByIds(List<Object> ids) throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	// ======================== Insert ===========================
 
 	/**
 	 * Provide custom logic to decide if the action can be executed.
 	 * 
 	 * @param list
 	 * @param params
 	 * @return
 	 */
 	protected boolean canInsert(M ds, P params) {
 		return true;
 	}
 
 	/**
 	 * Provide custom logic to decide if the action can be executed.
 	 * 
 	 * @param list
 	 * @param params
 	 * @return
 	 */
 	protected boolean canInsert(List<M> list, P params) {
 		return true;
 	}
 
 	/**
 	 * Pre-insert event with the data-source values as received from client.
 	 * 
 	 * @param ds
 	 * @throws Exception
 	 */
 	protected void preInsert(M ds, P params) throws Exception {
 	}
 
 	/**
 	 * Pre-insert event with data-source and a new entity instance populated
 	 * from the data-source.
 	 * 
 	 * @param ds
 	 * @param e
 	 * @throws Exception
 	 */
 	protected void preInsert(M ds, E e, P params) throws Exception {
 	}
 
 	/**
 	 * Post-insert event. The entity has been persisted by the
 	 * <code>entityManager</code>, but the possible changes which might alter
 	 * the entity during its persist phase are not applied yet to the
 	 * data-source.
 	 * 
 	 * @param ds
 	 * @param e
 	 * @throws Exception
 	 */
 	protected void postInsertBeforeModel(M ds, E e, P params) throws Exception {
 	}
 
 	/**
 	 * Post-insert event. The entity has been persisted by the
 	 * <code>entityManager</code>, and the possible changes which might alter
 	 * the entity during its persist phase are applied to the data-source.
 	 * 
 	 * @param ds
 	 * @param e
 	 * @throws Exception
 	 */
 	protected void postInsertAfterModel(M ds, E e, P params) throws Exception {
 	}
 
 	/**
 	 * Insert event. The steps performed during this phase are: <li>Check if
 	 * action is allowed. <li>Create a new backing entity for the data-source.
 	 * <li>Apply the new values from the data-source and call the
 	 * <code>pre-insert</code> events <li>Delegate execution of insert to the
 	 * business-service. <li>Call the <code>post-insert</code> event.
 	 * 
 	 * @param ds
 	 * @throws Exception
 	 */
 	public void insert(M ds, P params) throws Exception {
 		if (this.noInsert || !this.canInsert(ds, params)) {
 			throw new ActionNotSupportedException("Insert not allowed.");
 		}
 		// add the client
 		if (ds instanceof IModelWithClientId) {
 			((IModelWithClientId) ds).setClientId(Session.user.get()
 					.getClientId());
 		}
 		this.preInsert(ds, params);
 		E e = (E) this.getEntityService().create();
 		this.getConverter().modelToEntity(ds, e, true);
 		this.preInsert(ds, e, params);
 		this.onInsert(ds, e, params);
 		postInsertBeforeModel(ds, e, params);
 		this.getConverter().entityToModel(e, ds);
 		postInsertAfterModel(ds, e, params);
 	}
 
 	protected void onInsert(M ds, E e, P params) throws Exception {
 		this.getEntityService().insert(e);
 	}
 
 	/**
 	 * Template method for <code>pre-insert</code>.
 	 * 
 	 * @param list
 	 * @throws Exception
 	 */
 	protected void preInsert(List<M> list, P params) throws Exception {
 
 	}
 
 	public void insert(List<M> list, P params) throws Exception {
 		if (this.noInsert) {
 			throw new ActionNotSupportedException("Insert not allowed.");
 		}
 		// add the client
 		for (M ds : list) {
 			if (ds instanceof IModelWithClientId) {
 				((IModelWithClientId) ds).setClientId(Session.user.get()
 						.getClientId());
 			}
 		}
 		this.preInsert(list, params);
 		// add entities in a queue and then try to insert them all in one
 		// transaction
 		List<E> entities = new ArrayList<E>();
 		for (M ds : list) {
 			this.preInsert(ds, params);
 			E e = (E) this.getEntityService().create();
 			entities.add(e);
 			((AbstractDsModel<E>) ds)._setEntity_(e);
 			this.getConverter().modelToEntity(ds, e, true);
 			this.preInsert(ds, e, params);
 		}
 		// this.getEntityService().insert(entities);
 		this.onInsert(list, entities, params);
 		for (M ds : list) {
 			E e = ((AbstractDsModel<E>) ds)._getEntity_();
 			postInsertBeforeModel(ds, e, params);
 			this.getConverter().entityToModel(e, ds);
 			postInsertAfterModel(ds, e, params);
 		}
 		this.postInsert(list, params);
 	}
 
 	protected void onInsert(List<M> list, List<E> entities, P params)
 			throws Exception {
 		this.getEntityService().insert(entities);
 	}
 
 	/**
 	 * Template method for <code>post-insert</code>.
 	 * 
 	 * @param list
 	 * @throws Exception
 	 */
 	public void postInsert(List<M> list, P params) throws Exception {
 
 	}
 
 	// ======================== Update ===========================
 
 	/**
 	 * Provide custom logic to decide if the action can be executed.
 	 * 
 	 * @param list
 	 * @param params
 	 * @return
 	 */
 	protected boolean canUpdate(M ds, P params) {
 		return true;
 	}
 
 	/**
 	 * Provide custom logic to decide if the action can be executed.
 	 * 
 	 * @param list
 	 * @param params
 	 * @return
 	 */
 	protected boolean canUpdate(List<M> list, P params) {
 		return true;
 	}
 
 	/**
 	 * Pre-insert event with the data-source values.
 	 * 
 	 * @param ds
 	 * @throws Exception
 	 */
 	protected void preUpdate(M ds, P params) throws Exception {
 	}
 
 	/**
 	 * Pre-insert event with data-source and the corresponding source entity has
 	 * been found. The entity has not been yet populated with the new values
 	 * from the data-source.
 	 * 
 	 * @param ds
 	 * @param e
 	 * @throws Exception
 	 */
 	protected void preUpdateBeforeEntity(M ds, E e, P params) throws Exception {
 	}
 
 	/**
 	 * Pre-insert event with data-source and the corresponding source entity has
 	 * been found. The new values from the data-source are already applied to
 	 * the entity.
 	 * 
 	 * @param ds
 	 * @param e
 	 * @throws Exception
 	 */
 	protected void preUpdateAfterEntity(M ds, E e, P params) throws Exception {
 	}
 
 	/**
 	 * Post-update event. The entity has been merged by the
 	 * <code>entityManager</code>, but the possible changes which might alter
 	 * the entity during its merging phase are not applied yet to the
 	 * data-source.
 	 * 
 	 * @param ds
 	 * @param e
 	 * @throws Exception
 	 */
 	protected void postUpdateBeforeModel(M ds, E e, P params) throws Exception {
 	}
 
 	/**
 	 * Post-update event. The entity has been merged by the
 	 * <code>entityManager</code>, and the possible changes which might alter
 	 * the entity during its merging phase are applied to the data-source.
 	 * 
 	 * @param ds
 	 * @param e
 	 * @throws Exception
 	 */
 	protected void postUpdateAfterModel(M ds, E e, P params) throws Exception {
 	}
 
 	/**
 	 * Update event. The steps performed during this phase are: <li>Check if
 	 * action is allowed. <li>Find the backing entity of the data-source. <li>
 	 * Apply the new values from the data-source and call the
 	 * <code>pre-update</code> events <li>Delegate execution of update to the
 	 * business-service. <li>Call the <code>post-update</code> event.
 	 * 
 	 * @param ds
 	 * @throws Exception
 	 */
 	public void update(M ds, P params) throws Exception {
 		if (this.noUpdate || !this.canUpdate(ds, params)) {
 			throw new ActionNotSupportedException("Update not allowed.");
 		}
 
 		this.preUpdate(ds, params);
 		E e = (E) this.getEntityService().findById(((IModelWithId) ds).getId());
 		this.preUpdateBeforeEntity(ds, e, params);
 		this.getConverter().modelToEntity(ds, e, false);
 		this.preUpdateAfterEntity(ds, e, params);
 		this.getEntityService().update(e);
 		postUpdateBeforeModel(ds, e, params);
 		this.getConverter().entityToModel(e, ds);
 		postUpdateAfterModel(ds, e, params);
 	}
 
 	/**
 	 * Template method for <code>pre-update</code>.
 	 * 
 	 * @param list
 	 * @throws Exception
 	 */
 	public void preUpdate(List<M> list, P params) throws Exception {
 	}
 
 	public void update(List<M> list, P params) throws Exception {
 		if (this.noUpdate || !this.canUpdate(list, params)) {
 			throw new ActionNotSupportedException("Update not allowed.");
 		}
 		this.preUpdate(list, params);
 		List<E> entities = this.getEntityService().findByIds(
 				this.collectIds(list));
 
 		for (M ds : list) {
 			this.preUpdate(ds, params);
 			// TODO: optimize me
 			E e = lookupEntityById(entities, ((IModelWithId) ds).getId());
 			this.preUpdateBeforeEntity(ds, e, params);
 			this.getConverter().modelToEntity(ds, e, false);
 			this.preUpdateAfterEntity(ds, e, params);
 		}
 		this.getEntityService().update(entities);
 		for (M ds : list) {
 			E e = this.getEntityService().getEntityManager().find(
 					this.getEntityClass(), ((IModelWithId) ds).getId());
 			postUpdateBeforeModel(ds, e, params);
 			this.getConverter().entityToModel(e, ds);
 			postUpdateAfterModel(ds, e, params);
 		}
 		this.postUpdate(list, params);
 	}
 
 	private E lookupEntityById(List<E> list, Object id) {
 		for (E e : list) {
 			if (((IModelWithId) e).getId().equals(id)) {
 				return e;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Template method for <code>post-update</code>.
 	 * 
 	 * @param list
 	 * @throws Exception
 	 */
 	public void postUpdate(List<M> list, P params) throws Exception {
 
 	}
 
 	// ======================== Delete ===========================
 
 	/**
 	 * Provide custom logic to decide if the action can be executed.
 	 */
 	protected boolean canDelete(Object id) {
 		return true;
 	}
 
 	/**
 	 * Provide custom logic to decide if the action can be executed.
 	 */
 	protected boolean canDelete(List<Object> ids) {
 		return true;
 	}
 
 	/**
 	 * Template method for <code>pre-delete</code>.
 	 */
 	protected void preDelete(Object id) {
 	}
 
 	/**
 	 * Template method for <code>post-delete</code>.
 	 */
 	protected void postDelete(Object id) {
 	}
 
 	/**
 	 * Delete by id.
 	 * 
 	 * @param id
 	 * @throws Exception
 	 */
 	public void deleteById(Object id) throws Exception {
 		if (this.noDelete || !this.canDelete(id)) {
 			throw new ActionNotSupportedException("Delete not allowed.");
 		}
 		preDelete(id);
 		this.getEntityService().deleteById(id);
 		postDelete(id);
 	}
 
 	protected void preDelete(List<Object> ids) throws Exception {
 	}
 
 	protected void postDelete(List<Object> ids) throws Exception {
 	}
 
 	/**
 	 * Delete by list of ids
 	 * 
 	 * @param ids
 	 * @throws Exception
 	 */
 	public void deleteByIds(List<Object> ids) throws Exception {
 		if (this.noDelete || !this.canDelete(ids)) {
 			throw new ActionNotSupportedException("Delete not allowed.");
 		}
 		preDelete(ids);
 		this.getEntityService().deleteByIds(ids);
 		postDelete(ids);
 	}
 
 	// ======================== Import/Export ===========================
 
 	public void doImport(String absoluteFileName) throws Exception {
 		this.doImportAsInsert_(new File(absoluteFileName));
 	}
 
 	public void doImport(String relativeFileName, String path) throws Exception {
 		this.doImportAsInsert_(new File(path + "/" + relativeFileName));
 	}
 
 	public void doImport(String absoluteFileName, String ukFieldName,
 			int batchSize) throws Exception {
 		this.doImportAsUpdate_(new File(absoluteFileName), ukFieldName,
 				batchSize);
 	}
 
 	public void doImport(String relativeFileName, String path,
 			String ukFieldName, int batchSize) throws Exception {
 		this.doImportAsUpdate_(new File(path + "/" + relativeFileName),
 				ukFieldName, batchSize);
 	}
 
 	protected void doImportAsInsert_(File file) throws Exception {
 		DsCsvLoader l = new DsCsvLoader();
 		List<M> list = l.run(file, this.modelClass, null);
 		this.insert(list, null);
 	}
 
 	protected void doImportAsUpdate_(File file, String ukFieldName,
 			int batchSize) throws Exception {
 
 		Assert.notNull(ukFieldName,
 				"For import as update you must specify the unique-key "
 						+ "field which is used to lookup the existing record.");
 
 		// TODO: check type-> csv, json, etc
 		DsCsvLoader l = new DsCsvLoader();
 		DsCsvLoaderResult<M> result = l.run2(file, this.modelClass, null);
 		List<M> list = result.getResult();
 		String[] columns = result.getHeader();
 		
 		F filter = this.filterClass.newInstance();
 		 
 		//TODO: ******************   optimize me to do the work in batches !! ***************
 		
 		Method filterUkFieldSetter = this.filterClass.getMethod("set"+ StringUtils.capitalize(ukFieldName), String.class);
 		Method modelUkFieldGetter = this.modelClass.getMethod("get"+ StringUtils.capitalize(ukFieldName));
 		
 		Map<String, Method> modelSetters = new HashMap<String, Method>();
 		Map<String, Method> modelGetters = new HashMap<String, Method>();
 		
 		int len = columns.length;
 		
 		for(int i=0;i<len;i++) {
 			String fieldName = columns[i];
 			Class<?> clz = this.modelClass;
 			Field f = null;
 			while (f==null && clz != null) {				
 				try {
 					f = clz.getDeclaredField(fieldName);
 				} catch(Exception e) {
 					
 				}
 				clz=clz.getSuperclass();
 			}
 			 		
 			if (f!=null) {
 				Method modelSetter = this.modelClass.getMethod("set"+ StringUtils.capitalize(fieldName), f.getType());
 				modelSetters.put(fieldName, modelSetter);
 				
 				Method modelGetter = this.modelClass.getMethod("get"+ StringUtils.capitalize(fieldName));
 				modelGetters.put(fieldName, modelGetter);	
 			} else {
 				
 			}
 		}
 		
 		for (M newDs : list) {
 			filterUkFieldSetter.invoke(filter, modelUkFieldGetter.invoke(newDs));
			List<M> res =  this.find(filter);
			// TODO: add an extra flag for what to do if the target is not found:
			// ignore or raise an error
			if (res.size() > 0) {
				M oldDs = this.find(filter).get(0);
				for(Map.Entry<String, Method> entry : modelSetters.entrySet()) {
					entry.getValue().invoke(oldDs, modelGetters.get(entry.getKey()).invoke(newDs) );
				}
				this.update(oldDs, null);
 			}
 		}
 	}
 
 	public void doExport(F filter, P params, IQueryBuilder<M, F, P> builder,
 			IDsExport<M> writer) throws Exception {
 
 		QueryBuilderWithJpql<M, F, P> bld = (QueryBuilderWithJpql<M, F, P>) builder;
 		bld.setForExport(true);
 
 		EntityManager lem = bld.getEntityManager().getEntityManagerFactory()
 				.createEntityManager();
 		bld.setEntityManager(lem);
 
 		try {
 
 			if (filter != null) {
 				bld.setFilter(filter);
 			} else {
 				bld.setFilter(getFilterClass().newInstance());
 			}
 			if (params != null) {
 				bld.setParams(params);
 			} else {
 				bld.setParams(getParamClass().newInstance());
 			}
 
 			List<M> result = new ArrayList<M>();
 
 			Query q = bld.createQuery().setHint(QueryHints.CURSOR, true)
 					.setHint(QueryHints.CURSOR_INITIAL_SIZE, 30).setHint(
 							QueryHints.CURSOR_PAGE_SIZE, 30).setHint(
 							QueryHints.READ_ONLY, HintValues.TRUE)
 					.setFirstResult(bld.getResultStart()).setMaxResults(
 							bld.getResultSize());
 
 			Cursor c = q.unwrap(JpaQuery.class).getResultCursor();
 
 			M ds;
 			writer.begin();
 			boolean isFirst = true;
 			while (c.hasMoreElements()) {
 				ds = getModelClass().newInstance();
 				this.getConverter().entityToModel((E) c.nextElement(), ds);
 				writer.write(ds, isFirst);
 				isFirst = false;
 			}
 			writer.end();
 			c.close();
 		} finally {
 			lem.close();
 		}
 
 	}
 
 	// ======================== RPC ===========================
 
 	public void rpcData(String procedureName, M ds, P params) throws Exception {
 		if (!rpcData.containsKey(procedureName)) {
 			throw new Exception("No such procedure defined: " + procedureName);
 		}
 		RpcDefinition def = rpcData.get(procedureName);
 		AbstractDsDelegate delegate = def.getDelegateClass().newInstance();
 		this.prepareDelegate(delegate);
 		Method m = null;
 		boolean withParams = false;
 		try {
 			m = def.getDelegateClass().getMethod(def.getMethodName(),
 					getModelClass(), getParamClass());
 			withParams = true;
 		} catch (NoSuchMethodException e) {
 			m = def.getDelegateClass().getMethod(def.getMethodName(),
 					getModelClass());
 		}
 		if (withParams) {
 			m.invoke(delegate, ds, params);
 		} else {
 			m.invoke(delegate, ds);
 		}
 		// delegate.execute(ds);
 	}
 
 	public InputStream rpcDataStream(String procedureName, M ds, P params)
 			throws Exception {
 		if (!rpcData.containsKey(procedureName)) {
 			throw new Exception("No such procedure defined: " + procedureName);
 		}
 		RpcDefinition def = rpcData.get(procedureName);
 		AbstractDsDelegate delegate = def.getDelegateClass().newInstance();
 		this.prepareDelegate(delegate);
 
 		Method m = null;
 		boolean withParams = false;
 		try {
 			m = def.getDelegateClass().getMethod(def.getMethodName(),
 					getModelClass(), getParamClass());
 			withParams = true;
 		} catch (NoSuchMethodException e) {
 			m = def.getDelegateClass().getMethod(def.getMethodName(),
 					getModelClass());
 		}
 		InputStream result = null;
 		if (withParams) {
 			result = (InputStream) m.invoke(delegate, ds, params);
 		} else {
 			result = (InputStream) m.invoke(delegate, ds);
 		}
 
 		// delegate.execute(ds);
 		return result;
 	}
 
 	public void rpcFilter(String procedureName, F filter, P params)
 			throws Exception {
 		if (!rpcFilter.containsKey(procedureName)) {
 			throw new Exception("No such procedure defined: " + procedureName);
 		}
 		RpcDefinition def = rpcFilter.get(procedureName);
 		AbstractDsDelegate delegate = def.getDelegateClass().newInstance();
 		this.prepareDelegate(delegate);
 		Method m = null;
 		boolean withParams = false;
 		try {
 			m = def.getDelegateClass().getMethod(def.getMethodName(),
 					getFilterClass(), getParamClass());
 			withParams = true;
 		} catch (NoSuchMethodException e) {
 			m = def.getDelegateClass().getMethod(def.getMethodName(),
 					getFilterClass());
 		}
 		if (withParams) {
 			m.invoke(delegate, filter, params);
 		} else {
 			m.invoke(delegate, filter);
 		}
 
 		// delegate.execute(filter);
 	}
 
 	public InputStream rpcFilterStream(String procedureName, F filter, P params)
 			throws Exception {
 		if (!rpcFilter.containsKey(procedureName)) {
 			throw new Exception("No such procedure defined: " + procedureName);
 		}
 		RpcDefinition def = rpcFilter.get(procedureName);
 		AbstractDsDelegate delegate = def.getDelegateClass().newInstance();
 		this.prepareDelegate(delegate);
 		Method m = null;
 		boolean withParams = false;
 		try {
 			m = def.getDelegateClass().getMethod(def.getMethodName(),
 					getFilterClass(), getParamClass());
 			withParams = true;
 		} catch (NoSuchMethodException e) {
 			m = def.getDelegateClass().getMethod(def.getMethodName(),
 					getFilterClass());
 		}
 		InputStream result = null;
 		if (withParams) {
 			result = (InputStream) m.invoke(delegate, filter, params);
 		} else {
 			result = (InputStream) m.invoke(delegate, filter);
 		}
 
 		return result;
 	}
 
 	public void rpcData(String procedureName, List<M> list, P params)
 			throws Exception {
 		throw new Exception("Not implemented yet");
 	}
 
 	public InputStream rpcDataStream(String procedureName, List<M> list,
 			P params) throws Exception {
 		throw new Exception("Not implemented yet");
 	}
 
 	// ======================== Getters-setters ===========================
 
 	public Class<E> getEntityClass() {
 		return entityClass;
 	}
 
 	public void setEntityClass(Class<E> entityClass) {
 		this.entityClass = entityClass;
 	}
 
 	public Class<M> getModelClass() {
 		return modelClass;
 	}
 
 	public void setModelClass(Class<M> modelClass) throws Exception {
 		this.modelClass = modelClass;
 	}
 
 	public Class<F> getFilterClass() {
 		return filterClass;
 	}
 
 	public void setFilterClass(Class<F> filterClass) {
 		this.filterClass = filterClass;
 	}
 
 	public Class<P> getParamClass() {
 		if (this.paramClass == null) {
 			this.paramClass = (Class<P>) EmptyParam.class;
 		}
 		return paramClass;
 	}
 
 	public void setParamClass(Class<P> paramClass) {
 		this.paramClass = paramClass;
 	}
 
 	public Class<?> getQueryBuilderClass() {
 		return queryBuilderClass;
 	}
 
 	public void setQueryBuilderClass(Class<?> queryBuilderClass) {
 		this.queryBuilderClass = queryBuilderClass;
 	}
 
 	public Class<?> getConverterClass() {
 		return converterClass;
 	}
 
 	public void setConverterClass(Class<?> converterClass) {
 		this.converterClass = converterClass;
 	}
 
 	protected IDsConverter<M, E> getConverter() throws Exception {
 		if (this.converter == null) {
 			if (this.converterClass == null) {
 				this.converterClass = BaseDsConverter.class;
 			}
 			this.converter = (IDsConverter<M, E>) this.converterClass
 					.newInstance();
 			this.converter.setEntityManager(this.getEntityService()
 					.getEntityManager());
 			// this.converter
 			// .setEntityServiceFactories(this.getEntityServiceFactories());
 			AbstractDsConverter<M, E> cnv = (AbstractDsConverter<M, E>) this.converter;
 			cnv.setAppContext(this.getAppContext());
 			cnv.setDescriptor(this.getDescriptor());
 			cnv.setEntityClass(this.getEntityClass());
 			cnv.setModelClass(this.getModelClass());
 			cnv.setServiceLocator(this.getServiceLocator());
 		}
 		return this.converter;
 	}
 
 	public void setConverter(IDsConverter<M, E> converter) {
 		this.converter = converter;
 	}
 
 	public void setDescriptor(DsDescriptor<M> descriptor) {
 		this.descriptor = descriptor;
 	}
 
 	public DsDescriptor<M> getDescriptor() throws Exception {
 		if (this.descriptor == null) {
 			this.descriptor = ViewModelDescriptorManager.getDsDescriptor(
 					this.modelClass, this.getSystemConfig()
 							.shouldCacheDescriptor());
 		}
 		return descriptor;
 	}
 
 	public IEntityService<E> getEntityService() throws Exception {
 		if (this.entityService == null) {
 			this.entityService = this.findEntityService(this.getEntityClass());
 		}
 		return this.entityService;
 	}
 
 	// ======================== Helpers ===========================
 
 	protected List<Object> collectIds(List<M> list) {
 		List<Object> ids = new ArrayList<Object>();
 		for (M ds : list) {
 			ids.add(((IModelWithId) ds).getId());
 		}
 		return ids;
 	}
 
 	public Map<String, RpcDefinition> getRpcData() {
 		return rpcData;
 	}
 
 	public void setRpcData(Map<String, RpcDefinition> rpcData) {
 		this.rpcData = rpcData;
 	}
 
 	public Map<String, RpcDefinition> getRpcFilter() {
 		return rpcFilter;
 	}
 
 	public void setRpcFilter(Map<String, RpcDefinition> rpcFilter) {
 		this.rpcFilter = rpcFilter;
 	}
 
 	public IQueryBuilder<M, F, P> createQueryBuilder() throws Exception {
 		IQueryBuilder<M, F, P> qb = null;
 		if (this.queryBuilderClass == null) {
 			qb = new QueryBuilderWithJpql<M, F, P>();
 		} else {
 			qb = (IQueryBuilder<M, F, P>) this.queryBuilderClass.newInstance();
 		}
 		this._prepareQueryBuilder(qb);
 		return qb;
 	}
 
 	private void _prepareQueryBuilder(IQueryBuilder<M, F, P> qb)
 			throws Exception {
 		qb.setModelClass(this.getModelClass());
 		qb.setFilterClass(this.getFilterClass());
 		qb.setParamClass(this.getParamClass());
 		qb.setDescriptor(this.getDescriptor());
 		qb.setSystemConfig(this.getSystemConfig());
 		if (qb instanceof QueryBuilderWithJpql) {
 			QueryBuilderWithJpql jqb = (QueryBuilderWithJpql) qb;
 			jqb.setEntityManager(this.getEntityService().getEntityManager());
 			jqb.setBaseEql("select e from "
 					+ this.getEntityClass().getSimpleName() + " e");
 			jqb.setBaseEqlCount("select count(1) from "
 					+ this.getEntityClass().getSimpleName() + " e");
 			if (this.getDescriptor().isWorksWithJpql()) {
 				jqb.setDefaultWhere(this.getDescriptor().getJpqlDefaultWhere());
 				jqb.setDefaultSort(this.getDescriptor().getJpqlDefaultSort());
 			}
 		}
 	}
 
 	public IDsMarshaller<M, F, P> createMarshaller(String dataFormat)
 			throws Exception {
 		IDsMarshaller<M, F, P> marshaller = null;
 		if (dataFormat.equals(IDsMarshaller.JSON)) {
 			marshaller = new JsonMarshaller<M, F, P>(this.getModelClass(), this
 					.getFilterClass(), this.getParamClass());
 		}
 		return marshaller;
 	}
 
 }
