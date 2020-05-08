 package de.objectcode.time4u.client.store.impl.hibernate;
 
 import java.io.File;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.derby.jdbc.EmbeddedDriver;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cache.HashtableCacheProvider;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.hibernate.cfg.Environment;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.dialect.DerbyDialect;
 
 import de.objectcode.time4u.client.store.StorePlugin;
 import de.objectcode.time4u.client.store.api.IPersonRepository;
 import de.objectcode.time4u.client.store.api.IProjectRepository;
 import de.objectcode.time4u.client.store.api.IRepository;
 import de.objectcode.time4u.client.store.api.IServerConnectionRepository;
 import de.objectcode.time4u.client.store.api.IStatisticRepository;
 import de.objectcode.time4u.client.store.api.ITaskRepository;
 import de.objectcode.time4u.client.store.api.ITeamRepository;
 import de.objectcode.time4u.client.store.api.ITodoRepository;
 import de.objectcode.time4u.client.store.api.IWorkItemRepository;
 import de.objectcode.time4u.client.store.api.RepositoryException;
 import de.objectcode.time4u.client.store.api.event.IRepositoryListener;
 import de.objectcode.time4u.client.store.api.event.RepositoryEvent;
 import de.objectcode.time4u.client.store.api.event.RepositoryEventType;
 import de.objectcode.time4u.client.store.impl.hibernate.entities.ClientDataEntity;
 import de.objectcode.time4u.client.store.impl.util.MonitoringProxy;
 import de.objectcode.time4u.server.api.data.EntityType;
 import de.objectcode.time4u.server.api.data.Person;
 import de.objectcode.time4u.server.entities.ActiveWorkItemEntity;
 import de.objectcode.time4u.server.entities.DayInfoEntity;
 import de.objectcode.time4u.server.entities.DayInfoMetaPropertyEntity;
 import de.objectcode.time4u.server.entities.DayTagEntity;
 import de.objectcode.time4u.server.entities.PersonEntity;
 import de.objectcode.time4u.server.entities.PersonMetaPropertyEntity;
 import de.objectcode.time4u.server.entities.ProjectEntity;
 import de.objectcode.time4u.server.entities.ProjectMetaPropertyEntity;
 import de.objectcode.time4u.server.entities.TaskEntity;
 import de.objectcode.time4u.server.entities.TaskMetaPropertyEntity;
 import de.objectcode.time4u.server.entities.TeamEntity;
 import de.objectcode.time4u.server.entities.TeamMetaPropertyEntity;
 import de.objectcode.time4u.server.entities.TimePolicyEntity;
 import de.objectcode.time4u.server.entities.TodoAssignmentEntity;
 import de.objectcode.time4u.server.entities.TodoBaseEntity;
 import de.objectcode.time4u.server.entities.TodoEntity;
 import de.objectcode.time4u.server.entities.TodoGroupEntity;
 import de.objectcode.time4u.server.entities.TodoMetaPropertyEntity;
 import de.objectcode.time4u.server.entities.WeekTimePolicyEntity;
 import de.objectcode.time4u.server.entities.WorkItemEntity;
 import de.objectcode.time4u.server.entities.revision.ILocalIdGenerator;
 import de.objectcode.time4u.server.entities.revision.IRevisionGenerator;
 import de.objectcode.time4u.server.entities.revision.IRevisionLock;
 import de.objectcode.time4u.server.entities.revision.LocalIdEntity;
 import de.objectcode.time4u.server.entities.revision.RevisionEntity;
 import de.objectcode.time4u.server.entities.revision.SessionLocalIdGenerator;
 import de.objectcode.time4u.server.entities.revision.SessionRevisionGenerator;
 import de.objectcode.time4u.server.entities.sync.ServerConnectionEntity;
 import de.objectcode.time4u.server.entities.sync.SynchronizationStatusEntity;
 import de.objectcode.time4u.server.utils.Base64;
 import de.objectcode.time4u.server.utils.DefaultKeyChainEncoder;
 import de.objectcode.time4u.server.utils.IKeyChainEncoder;
 
 /**
  * Hibernate implementation of the repository interface.
  * 
  * For convenience we are using lots of server entities. There is no reason to redefine the whole data model here.
  * 
  * @author junglas
  */
 public class HibernateRepository implements IRepository
 {
   private final HibernateTemplate m_hibernateTemplate;
 
   private Person m_owner;
   private long m_clientId;
   private final IKeyChainEncoder m_keyChainEncoder;
 
   private final HibernatePersonRepository m_personRepository;
   private final HibernateTeamRepository m_teamRepository;
   private final HibernateProjectRepository m_projectRepository;
   private final HibernateTaskRepository m_taskRepository;
   private final HibernateTodoRepository m_todoRepository;
   private final HibernateWorkItemRepository m_workItemRepository;
   private final HibernateStatisticRepository m_statisticRepository;
   private final HibernateServerConnectionRepository m_serverConnectionRepository;
   private ILocalIdGenerator m_idGenerator;
 
   private final Map<RepositoryEventType, List<IRepositoryListener>> m_listeners = new HashMap<RepositoryEventType, List<IRepositoryListener>>();
 
   public HibernateRepository(final File directory) throws RepositoryException
   {
     m_hibernateTemplate = new HibernateTemplate(buildSessionFactory(directory));
     m_keyChainEncoder = new DefaultKeyChainEncoder();
 
     initialize();
 
     m_personRepository = new HibernatePersonRepository(this, m_hibernateTemplate);
     m_teamRepository = new HibernateTeamRepository(this, m_hibernateTemplate);
     m_projectRepository = new HibernateProjectRepository(this, m_hibernateTemplate);
     m_taskRepository = new HibernateTaskRepository(this, m_hibernateTemplate);
     m_todoRepository = new HibernateTodoRepository(this, m_hibernateTemplate);
     m_workItemRepository = new HibernateWorkItemRepository(this, m_hibernateTemplate);
     m_statisticRepository = new HibernateStatisticRepository(this, m_hibernateTemplate);
     m_serverConnectionRepository = new HibernateServerConnectionRepository(this, m_hibernateTemplate);
 
     addRepositoryListener(RepositoryEventType.PROJECT, m_statisticRepository);
     addRepositoryListener(RepositoryEventType.DAYINFO, m_statisticRepository);
   }
 
   /**
    * {@inheritDoc}
    */
   public IPersonRepository getPersonRepository()
   {
     return MonitoringProxy.getMonitoringProxy(IPersonRepository.class, m_personRepository);
   }
 
   /**
    * {@inheritDoc}
    */
   public ITeamRepository getTeamRepository()
   {
     return MonitoringProxy.getMonitoringProxy(ITeamRepository.class, m_teamRepository);
   }
 
   /**
    * {@inheritDoc}
    */
   public IProjectRepository getProjectRepository()
   {
     return MonitoringProxy.getMonitoringProxy(IProjectRepository.class, m_projectRepository);
   }
 
   /**
    * {@inheritDoc}
    */
   public ITaskRepository getTaskRepository()
   {
     return MonitoringProxy.getMonitoringProxy(ITaskRepository.class, m_taskRepository);
   }
 
   /**
    * {@inheritDoc}
    */
   public ITodoRepository getTodoRepository()
   {
     return MonitoringProxy.getMonitoringProxy(ITodoRepository.class, m_todoRepository);
   }
 
   /**
    * {@inheritDoc}
    */
   public IWorkItemRepository getWorkItemRepository()
   {
     return MonitoringProxy.getMonitoringProxy(IWorkItemRepository.class, m_workItemRepository);
   }
 
   /**
    * {@inheritDoc}
    */
   public IStatisticRepository getStatisticRepository()
   {
     return MonitoringProxy.getMonitoringProxy(IStatisticRepository.class, m_statisticRepository);
   }
 
   /**
    * {@inheritDoc}
    */
   public IServerConnectionRepository getServerConnectionRepository()
   {
     return m_serverConnectionRepository;
   }
 
   IKeyChainEncoder getKeyChainEncoder()
   {
     return m_keyChainEncoder;
   }
 
   String generateLocalId(final EntityType entityTpye)
   {
     return m_idGenerator.generateLocalId(entityTpye);
   }
 
   /**
    * {@inheritDoc}
    */
   public void addRepositoryListener(final RepositoryEventType eventType, final IRepositoryListener listener)
   {
     synchronized (m_listeners) {
       List<IRepositoryListener> listeners = m_listeners.get(eventType);
 
       if (listeners == null) {
         listeners = new ArrayList<IRepositoryListener>();
 
         m_listeners.put(eventType, listeners);
       }
       listeners.add(listener);
     }
   }
 
   /**
    * {@inheritDoc}
    */
   public void removeRepositoryListener(final RepositoryEventType eventType, final IRepositoryListener listener)
   {
     synchronized (m_listeners) {
       final List<IRepositoryListener> listeners = m_listeners.get(eventType);
 
       if (listeners != null) {
         listeners.remove(listener);
       }
     }
   }
 
   public Person getOwner()
   {
     return m_owner;
   }
 
   public long getClientId()
   {
     return m_clientId;
   }
 
   public void changeOwnerId(final String ownerId) throws RepositoryException
   {
     final String oldOwnerId = m_owner.getId();
 
     if (oldOwnerId.equals(ownerId)) {
       return;
     }
 
     m_owner = m_hibernateTemplate.executeInTransaction(new HibernateTemplate.OperationWithResult<Person>() {
       public Person perform(final Session session)
       {
         final IRevisionGenerator revisionGenerator = new SessionRevisionGenerator(session);
         final IRevisionLock revisionLock = revisionGenerator.getNextRevision(EntityType.PERSON, null);
 
         final PersonEntity personEntity = new PersonEntity(ownerId, revisionLock.getLatestRevision(), m_clientId);
 
         personEntity.fromDTO(m_owner);
         personEntity.setLastModifiedByClient(m_clientId);
 
         session.merge(personEntity);
         final ClientDataEntity clientData = (ClientDataEntity) session.get(ClientDataEntity.class, 1);
 
         clientData.setOwnerPerson(personEntity);
         session.flush();
 
         final Query updateDayInfos = session
             .createSQLQuery("update T4U_DAYINFOS set person_id=:newOwnerId where person_id=:oldOwnerId");
         updateDayInfos.setString("newOwnerId", ownerId);
         updateDayInfos.setString("oldOwnerId", oldOwnerId);
         updateDayInfos.executeUpdate();
 
         final Query updateTimePoliciesInfos = session
             .createSQLQuery("update T4U_TIMEPOLICIES set person_id=:newOwnerId where person_id=:oldOwnerId");
         updateTimePoliciesInfos.setString("newOwnerId", ownerId);
         updateTimePoliciesInfos.setString("oldOwnerId", oldOwnerId);
         updateTimePoliciesInfos.executeUpdate();
 
         final Query updateRevisions = session
             .createSQLQuery("update T4U_REVISIONS set part=:newOwnerId where part=:oldOwnerId");
         updateRevisions.setString("newOwnerId", ownerId);
         updateRevisions.setString("oldOwnerId", oldOwnerId);
         updateRevisions.executeUpdate();
 
         final Query deleteOld = session.createSQLQuery("delete from T4U_PERSONS where id=:oldOwnerId");
         deleteOld.setString("oldOwnerId", oldOwnerId);
         deleteOld.executeUpdate();
 
         final Person result = new Person();
         personEntity.toDTO(result);
 
         return result;
       }
     });
 
   }
 
   public Map<EntityType, Long> getRevisionStatus() throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.OperationWithResult<Map<EntityType, Long>>() {
       public Map<EntityType, Long> perform(final Session session)
       {
         final Criteria criteria = session.createCriteria(RevisionEntity.class);
         criteria.add(Restrictions.in("id.part", new Object[] { "<default>", m_owner.getId() }));
 
         final Map<EntityType, Long> result = new HashMap<EntityType, Long>();
 
         for (final Object row : criteria.list()) {
           final RevisionEntity revisionEntity = (RevisionEntity) row;
           result.put(revisionEntity.getId().getEntityType(), revisionEntity.getLatestRevision());
         }
         for (final EntityType type : EntityType.values()) {
           if (!result.containsKey(type)) {
             result.put(type, 0L);
           }
         }
 
         return result;
       }
     });
 
   }
 
   void fireRepositoryEvent(final RepositoryEvent event)
   {
     IRepositoryListener[] listenerArray;
 
     synchronized (m_listeners) {
       final List<IRepositoryListener> listeners = m_listeners.get(event.getEventType());
 
       if (listeners == null) {
         return;
       }
 
       listenerArray = listeners.toArray(new IRepositoryListener[listeners.size()]);
     }
 
     for (final IRepositoryListener listener : listenerArray) {
       listener.handleRepositoryEvent(event);
     }
   }
 
   private void initialize() throws RepositoryException
   {
     m_hibernateTemplate.executeInTransaction(new HibernateTemplate.OperationWithResult<Object>() {
       public Object perform(final Session session)
       {
         ClientDataEntity clientData = (ClientDataEntity) session.get(ClientDataEntity.class, 1);
 
         if (clientData == null) {
          // TODO: Reconsider this adhoc generation
          final long clientId = new SecureRandom().nextLong() & 0xffffffffffffffL;
 
           m_idGenerator = new SessionLocalIdGenerator(session.getSessionFactory(), clientId);
 
           final IRevisionGenerator revisionGenerator = new SessionRevisionGenerator(session);
           final IRevisionLock revisionLock = revisionGenerator.getNextRevision(EntityType.PERSON, null);
           final String personId = m_idGenerator.generateLocalId(EntityType.PERSON);
           final PersonEntity ownerPerson = new PersonEntity(personId, revisionLock.getLatestRevision(), clientId);
           ownerPerson.setSurname(System.getProperty("user.name"));
 
           session.persist(ownerPerson);
 
           clientData = new ClientDataEntity();
           clientData.setId(1);
           clientData.setOwnerPerson(ownerPerson);
           clientData.setClientId(clientId);
           clientData.setKeyChainKey(new String(Base64.encode(m_keyChainEncoder.generateKeyData())));
 
           session.persist(clientData);
 
           session.flush();
         } else {
           m_idGenerator = new SessionLocalIdGenerator(session.getSessionFactory(), clientData.getClientId());
         }
 
         m_owner = new Person();
         clientData.getOwnerPerson().toDTO(m_owner);
         m_clientId = clientData.getClientId();
         m_keyChainEncoder.init(Base64.decode(clientData.getKeyChainKey().toCharArray()));
 
         return null;
       }
     });
   }
 
   private SessionFactory buildSessionFactory(final File directory)
   {
     try {
       System.setProperty("derby.system.home", directory.getAbsolutePath());
 
       final AnnotationConfiguration cfg = new AnnotationConfiguration();
 
       cfg.setProperty(Environment.DRIVER, EmbeddedDriver.class.getName());
       cfg.setProperty(Environment.URL, "jdbc:derby:" + directory.getAbsolutePath() + "/time4u;create=true");
       cfg.setProperty(Environment.USER, "sa");
       cfg.setProperty(Environment.PASS, "");
       cfg.setProperty(Environment.POOL_SIZE, "5");
       cfg.setProperty(Environment.AUTOCOMMIT, "false");
       cfg.setProperty(Environment.ISOLATION, "2");
       cfg.setProperty(Environment.DIALECT, DerbyDialect.class.getName());
       cfg.setProperty(Environment.HBM2DDL_AUTO, "update");
       cfg.setProperty(Environment.SHOW_SQL, "false");
       cfg.setProperty(Environment.CACHE_PROVIDER, HashtableCacheProvider.class.getName());
       cfg.setProperty(Environment.USE_QUERY_CACHE, "true");
 
       cfg.addAnnotatedClass(RevisionEntity.class);
       cfg.addAnnotatedClass(LocalIdEntity.class);
       cfg.addAnnotatedClass(PersonEntity.class);
       cfg.addAnnotatedClass(PersonMetaPropertyEntity.class);
       cfg.addAnnotatedClass(TeamEntity.class);
       cfg.addAnnotatedClass(TeamMetaPropertyEntity.class);
       cfg.addAnnotatedClass(ProjectEntity.class);
       cfg.addAnnotatedClass(ProjectMetaPropertyEntity.class);
       cfg.addAnnotatedClass(TaskEntity.class);
       cfg.addAnnotatedClass(TaskMetaPropertyEntity.class);
       cfg.addAnnotatedClass(DayInfoEntity.class);
       cfg.addAnnotatedClass(DayTagEntity.class);
       cfg.addAnnotatedClass(DayInfoMetaPropertyEntity.class);
       cfg.addAnnotatedClass(WorkItemEntity.class);
       cfg.addAnnotatedClass(TodoBaseEntity.class);
       cfg.addAnnotatedClass(TodoEntity.class);
       cfg.addAnnotatedClass(TodoGroupEntity.class);
       cfg.addAnnotatedClass(TodoAssignmentEntity.class);
       cfg.addAnnotatedClass(TodoMetaPropertyEntity.class);
       cfg.addAnnotatedClass(ClientDataEntity.class);
       cfg.addAnnotatedClass(ActiveWorkItemEntity.class);
       cfg.addAnnotatedClass(ServerConnectionEntity.class);
       cfg.addAnnotatedClass(SynchronizationStatusEntity.class);
       cfg.addAnnotatedClass(TimePolicyEntity.class);
       cfg.addAnnotatedClass(WeekTimePolicyEntity.class);
 
       return cfg.buildSessionFactory();
     } catch (final Exception e) {
       e.printStackTrace();
       StorePlugin.getDefault().log(e);
     }
 
     return null;
   }
 }
