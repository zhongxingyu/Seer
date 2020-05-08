 package cx.ath.jbzdak.jpaGui.db.dao;
 
 import cx.ath.jbzdak.jpaGui.Utils;
 import static cx.ath.jbzdak.jpaGui.Utils.isIdNull;
 import cx.ath.jbzdak.jpaGui.db.DBManager;
 import cx.ath.jbzdak.jpaGui.db.dao.annotations.LifecyclePhase;
 import javax.persistence.EntityManager;
 
 import java.text.Normalizer.Form;
 import java.util.NoSuchElementException;
 
 /**
  * Domyślne dao. Zarządza pojedyńczą encją i ma przypisanego
  * {@link #entityManager}  Tak na prawdę zarządza trazakcjami i tyle robi.
  * Powiązane z {@link Form}
  *
  * @author jb
  * @param <T>
  */
 //TODO Wysyłanie eventów po komicte tranzakcji
 public class AbstractDAO<T> implements DAO<T> {
 
    protected final DBManager manager;
 
    protected final Class<? extends T> clazz;
 
    private final CompositeEntityLifecycleListener listener;
 
    protected EntityManager entityManager;
 
    protected boolean transactionWideEntityManager;
 
    private boolean autoCreateEntity = false;
 
    private RefreshType refreshType = RefreshType.MERGE;
 
    private T entity;
 
    private int beginCount = 0;
 
 
    /**
     * true jeśli tranzakcja zarządza kto inny
     */
    private boolean transactionManaged;
 
    public AbstractDAO(DBManager manager, Class<? extends T> clazz) {
       super();
       this.manager = manager;
       this.clazz = clazz;
       listener = new CompositeEntityLifecycleListener(manager, clazz);
    }
 
    void firePersistenceEVT(LifecyclePhase phase){
       beginTransaction();
       try {
          manager.firePersEvent(entity,  phase, entityManager);
          commitTransaction();
       } catch (RuntimeException e) {
          rollback();
          throw e;
       }
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#getEntityManager()
    */
    EntityManager getEntityManager() {
       if (entityManager == null) {
          throw new IllegalStateException();
       }
       return entityManager;
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#beginTransaction()
    */
    @Override
    public void beginTransaction() {
       if (transactionManaged)
          return;
       if (entityManager == null || !entityManager.isOpen()) {
          entityManager = manager.createEntityManager();
          transactionWideEntityManager = true;
       } else {
          transactionWideEntityManager = false;
       }
       if (beginCount == 0) {
          entityManager.getTransaction().begin();
          if (entity != null && !isIdNull(getBean())) {
            entity = refreshType.perform(entityManager, entity, manager);
          }
       }
       beginCount++;
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#commitTransaction()
    */
    @Override
    public void commitTransaction() {
       if(beginCount<=0){
          throw new IllegalStateException("Cant commit unopened transaction");
       }
       if (transactionManaged)
          return;
       beginCount--;
       if (beginCount == 0) {
          entityManager.getTransaction().commit();
          if (transactionWideEntityManager) {
             entityManager.close();
             entityManager = null;
          }
       }
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#rollback()
    */
    @Override
    public void rollback() {
       if(entityManager.getTransaction().isActive()){
          entityManager.getTransaction().rollback();
       }
       beginCount = 0;
       if (transactionWideEntityManager) {
          entityManager.close();
          entityManager = null;
       }
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#rollbackIfActive()
    */
    @Override
    public void rollbackIfActive() {
       if (entityManager != null) {
          if (entityManager.getTransaction().isActive()) {
             rollback();
             entityManager = null;
             beginCount = 0;
          }
       }
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#persist()
    */
    @Override
    public void persist() {
       beginTransaction();
       try {
          if(!Utils.isIdNull(getBean())){
             throw new IllegalStateException("Trying to persist already persisted bean");
          }
          firePersistenceEVT(LifecyclePhase.PrePersist);
          entityManager.persist(getBean());
          commitTransaction();
       } catch (RuntimeException e) {
          rollback();
          throw e;
       }
       firePersistenceEVT(LifecyclePhase.PostPersist);
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#update()
    */
    @Override
    public void update() {
       beginTransaction();
        try {
          firePersistenceEVT(LifecyclePhase.PreUpdate);
          getEntityManager().flush();
          commitTransaction();
       } catch (RuntimeException e) {
          rollback();
          throw e;
       }
       firePersistenceEVT(LifecyclePhase.PostUpdate);
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#persistOrUpdate()
    */
    @Override
    public void persistOrUpdate() {
         if (Utils.willAutoSetId(getBean())) {
           persist();
         } else {
            if(entityManager.find(clazz, Utils.getId(getBean()))==null){
               persist();
            }else{
               update();
            }
         }
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#find(java.lang.Object)
    */
    @Override
    public void find(Object o) {
       beginTransaction();
       try {
          setBean(getEntityManager().find(clazz, o));
          if (getBean() == null) {
             throw new NoSuchElementException();
          }
          firePersistenceEVT(LifecyclePhase.PostLoad);
          commitTransaction();
       } catch (RuntimeException e) {
          rollback();
          throw e;
       }
 
    }
 
    @Override
    public void remove(){
       beginTransaction();
       try{
          firePersistenceEVT(LifecyclePhase.PreRemove);
          entityManager.remove(entity);
          commitTransaction();
       }catch (RuntimeException e){
          rollback();
          throw e;
       }
    }
 
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#clearEntity()
    */
    @Override
    public void clearEntity() {
       setBean(null);
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#createEntity()
    */
    @Override
    public void createEntity() {
       if (getBean() != null) {
          throw new IllegalStateException();
       }
       try {
          setBean(clazz.newInstance());
       } catch (InstantiationException e) {
          throw new RuntimeException(e);
       } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
       }
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#getBean()
    */
    @Override
    public T getBean() {
       if (autoCreateEntity && entity == null) {
          createEntity();
       }
       return entity;
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#setBean(T)
    */
    @Override
    public void setBean(T entity) {
       this.entity = entity;
       if(beginCount!=0){
          this.entity = refreshType.perform(entityManager, entity, manager);
       }
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#isAutoCreateEntity()
    */
    @Override
    public boolean isAutoCreateEntity() {
       return autoCreateEntity;
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#setAutoCreateEntity(boolean)
    */
    @Override
    public void setAutoCreateEntity(boolean autoCreateEntity) {
       this.autoCreateEntity = autoCreateEntity;
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#isTransactionManaged()
    */
    @Override
    public boolean isTransactionManaged() {
       return transactionManaged;
    }
 
    /* (non-Javadoc)
    * @see cx.ath.jbzdak.zarlok.db.dao.DAO#setTransactionManaged(boolean)
    */
    @Override
    public void setTransactionManaged(boolean transactionManaged) {
       this.transactionManaged = transactionManaged;
    }
 
    @Override
    public RefreshType getRefreshType() {
       return refreshType;
    }
 
    @Override
    public void setRefreshType(RefreshType refreshType) {
       this.refreshType = refreshType;
    }
 
    protected CompositeEntityLifecycleListener getListener() {
       return listener;
    }
 }
