 package de.objectcode.time4u.client.store.impl.hibernate;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import de.objectcode.time4u.client.store.api.ITaskRepository;
 import de.objectcode.time4u.client.store.api.RepositoryException;
 import de.objectcode.time4u.client.store.api.event.TaskRepositoryEvent;
 import de.objectcode.time4u.server.api.data.SynchronizableType;
 import de.objectcode.time4u.server.api.data.Task;
 import de.objectcode.time4u.server.api.data.TaskSummary;
 import de.objectcode.time4u.server.api.filter.TaskFilter;
 import de.objectcode.time4u.server.entities.ProjectEntity;
 import de.objectcode.time4u.server.entities.TaskEntity;
 import de.objectcode.time4u.server.entities.context.SessionPersistenceContext;
 import de.objectcode.time4u.server.entities.revision.IRevisionGenerator;
 import de.objectcode.time4u.server.entities.revision.IRevisionLock;
 import de.objectcode.time4u.server.entities.revision.SessionRevisionGenerator;
 
 /**
  * Hibernate implementation of the task repository.
  * 
  * @author junglas
  */
 public class HibernateTaskRepository implements ITaskRepository
 {
   private final HibernateRepository m_repository;
   private final HibernateTemplate m_hibernateTemplate;
 
   HibernateTaskRepository(final HibernateRepository repository, final HibernateTemplate hibernateTemplate)
   {
     m_repository = repository;
     m_hibernateTemplate = hibernateTemplate;
   }
 
   /**
    * {@inheritDoc}
    */
   public Task getTask(final String taskId) throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation<Task>() {
       public Task perform(final Session session)
       {
         final TaskEntity taskEntity = (TaskEntity) session.get(TaskEntity.class, taskId);
 
         if (taskEntity != null) {
           final Task task = new Task();
           taskEntity.toDTO(task);
 
           return task;
         }
         return null;
       }
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public TaskSummary getTaskSummary(final String taskId) throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation<TaskSummary>() {
       public TaskSummary perform(final Session session)
       {
         final TaskEntity taskEntity = (TaskEntity) session.get(TaskEntity.class, taskId);
 
         if (taskEntity != null) {
           final TaskSummary task = new TaskSummary();
           taskEntity.toSummaryDTO(task);
 
           return task;
         }
         return null;
       }
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public List<Task> getTasks(final TaskFilter filter) throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation<List<Task>>() {
       public List<Task> perform(final Session session)
       {
         final Criteria criteria = session.createCriteria(TaskEntity.class);
 
         if (filter.getActive() != null) {
           criteria.add(Restrictions.eq("active", filter.getActive()));
         }
         if (filter.getDeleted() != null) {
           criteria.add(Restrictions.eq("deleted", filter.getDeleted()));
         }
         if (filter.getProject() != null) {
           criteria.add(Restrictions.eq("project.id", filter.getProject()));
         }
         if (filter.getMinRevision() != null) {
           criteria.add(Restrictions.ge("revision", filter.getMinRevision()));
         }
         if (filter.getMaxRevision() != null) {
           criteria.add(Restrictions.le("revision", filter.getMaxRevision()));
         }
         if (filter.getLastModifiedByClient() != null) {
           criteria.add(Restrictions.eq("lastModifiedByClient", filter.getLastModifiedByClient()));
         }
         switch (filter.getOrder()) {
           case ID:
             criteria.addOrder(Order.asc("id"));
             break;
           case NAME:
             criteria.addOrder(Order.asc("name"));
             criteria.addOrder(Order.asc("id"));
             break;
         }
 
         final List<Task> result = new ArrayList<Task>();
 
         for (final Object row : criteria.list()) {
           final Task task = new Task();
 
           ((TaskEntity) row).toDTO(task);
 
           result.add(task);
         }
 
         return result;
       }
 
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public List<TaskSummary> getTaskSummaries(final TaskFilter filter) throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation<List<TaskSummary>>() {
       public List<TaskSummary> perform(final Session session)
       {
         final Criteria criteria = session.createCriteria(TaskEntity.class);
 
         if (filter.getActive() != null) {
           criteria.add(Restrictions.eq("active", filter.getActive()));
         }
         if (filter.getDeleted() != null) {
           criteria.add(Restrictions.eq("deleted", filter.getDeleted()));
         }
         if (filter.getProject() != null) {
           criteria.add(Restrictions.eq("project.id", filter.getProject()));
         }
         if (filter.getMinRevision() != null) {
           criteria.add(Restrictions.ge("revision", filter.getMinRevision()));
         }
         if (filter.getMaxRevision() != null) {
           criteria.add(Restrictions.lt("revision", filter.getMaxRevision()));
         }
         if (filter.getLastModifiedByClient() != null) {
           criteria.add(Restrictions.eq("lastModifiedByClient", filter.getLastModifiedByClient()));
         }
         switch (filter.getOrder()) {
           case ID:
             criteria.addOrder(Order.asc("id"));
             break;
           case NAME:
             criteria.addOrder(Order.asc("name"));
             criteria.addOrder(Order.asc("id"));
             break;
         }
 
         final List<TaskSummary> result = new ArrayList<TaskSummary>();
 
         for (final Object row : criteria.list()) {
           final TaskSummary task = new TaskSummary();
 
           ((TaskEntity) row).toSummaryDTO(task);
 
           result.add(task);
         }
 
         return result;
       }
 
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public Task storeTask(final Task task, final boolean modifiedByOwner) throws RepositoryException
   {
     final Task result = m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation<Task>() {
       public Task perform(final Session session)
       {
         final IRevisionGenerator revisionGenerator = new SessionRevisionGenerator(session);
 
         final IRevisionLock revisionLock = revisionGenerator.getNextRevision(SynchronizableType.TASK, null);
 
         if (task.getId() == null) {
           task.setId(revisionLock.generateId(m_repository.getClientId()));
         }
 
        final ProjectEntity projectEntity = task.getProjectId() != null ? (ProjectEntity) session.get(
            ProjectEntity.class, task.getProjectId()) : null;
         final TaskEntity taskEntity = new TaskEntity(task.getId(), revisionLock.getLatestRevision(), m_repository
             .getClientId(), projectEntity, task.getName());
 
         taskEntity.fromDTO(new SessionPersistenceContext(session), task);
         if (modifiedByOwner) {
           taskEntity.setLastModifiedByClient(m_repository.getClientId());
         }
 
         session.merge(taskEntity);
         session.flush();
 
         final Task result = new Task();
 
         taskEntity.toDTO(result);
 
         return result;
       }
     });
 
     m_repository.fireRepositoryEvent(new TaskRepositoryEvent(task));
 
     return result;
   }
 
   /**
    * {@inheritDoc}
    */
   public List<Task> storeTasks(final List<Task> tasks, final boolean modifiedByOwner) throws RepositoryException
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   /**
    * {@inheritDoc}
    */
   public void deleteTask(final Task task) throws RepositoryException
   {
     // TODO Auto-generated method stub
 
   }
 
 }
