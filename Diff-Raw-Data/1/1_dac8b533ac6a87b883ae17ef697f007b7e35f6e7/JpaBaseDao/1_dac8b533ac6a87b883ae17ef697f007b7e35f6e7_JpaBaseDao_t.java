 package cz.cvut.fel.jee.labEshop.dao.jpa;
 
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityNotFoundException;
 import javax.persistence.criteria.CriteriaQuery;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cz.cvut.fel.jee.labEshop.dao.IBaseDao;
 import cz.cvut.fel.jee.labEshop.model.BaseEntity;
 
 /**
  * Fulfills base contract of {@linkplain IBaseDao}. Should be used by all JPA
  * Dao implementations which operates on subclasses of {@linkplain BaseEntity}
  * which defines primary key as java.lang.Long. Dao classes are not responsible
  * for transaction demarcation, that's the resposnsibility of business tier. So
  * they are POJO.
  * 
  * <p>
  * JpaBaseDao uses <code>@Inject EntityManager</code> to retrieve default entity
  * manager. Clients should set EntityManager instance after DI or manually after
  * instantiation.
  * 
  * @author Kamil Prochazka (<a href="mailto:prochka6@fel.cvut.cz">prochka6</a>)
  * 
  * @param <T>
  *            Dao entity class which should extends from {@linkplain BaseEntity}
  */
 public abstract class JpaBaseDao<T extends BaseEntity> implements IBaseDao<T, Long> {
 
 	// used programatic instantiating of logger because clients can call new
 	// without CDI
 	protected final Logger log = LoggerFactory.getLogger(getClass());
 
 	protected EntityManager em;
 	protected Class<T> entityClass;
 
 	@SuppressWarnings("unchecked")
 	protected JpaBaseDao() {
 		entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
 	}
 
 	protected JpaBaseDao(EntityManager entityManager) {
 		this();
 		this.em = entityManager;
 	}
 
 	@Override
 	public T getReference(Long id) {
 		return em.getReference(entityClass, id);
 	}
 
 	@Override
 	public T get(Long id) {
 		return em.find(entityClass, id);
 	}
 
 	@Override
 	public List<T> getAll() {
 		CriteriaQuery<T> query = em.getCriteriaBuilder().createQuery(entityClass);
 		query.from(entityClass);
 
 		return em.createQuery(query).getResultList();
 	}
 
 	@Override
 	public T saveOrUpdate(T entity) {
 		cz.cvut.fel.jee.labEshop.util.Assert.notNull(entity, "Entity may not be null.");
 
 		if (entity.getId() == null) {
 			em.persist(entity);
 		} else {
 			entity = em.merge(entity);
 		}
 
 		return entity;
 	}
 
 	@Override
 	public void delete(T entity) {
		em.merge(entity);
 		em.remove(entity);
 	}
 
 	@Override
 	public void deleteById(Long id) throws EntityNotFoundException {
 		T reference = getReference(id);
 		delete(reference);
 	}
 
 	@Override
 	public T refresh(T entity) {
 		em.refresh(entity);
 		return entity;
 	}
 
 	@Override
 	public void flush() {
 		em.flush();
 	}
 
 	@Override
 	public void flushAndClear() {
 		em.flush();
 		em.clear();
 	}
 
 	@Override
 	public EntityManager getEntityManager() {
 		return em;
 	}
 
 	@Inject
 	@Override
 	public void setEntityManager(EntityManager entityManager) {
 		this.em = entityManager;
 	}
 
 }
