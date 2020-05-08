 package de.objectcode.time4u.client.store.impl.hibernate;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import de.objectcode.time4u.client.store.api.IWorkItemRepository;
 import de.objectcode.time4u.client.store.api.RepositoryException;
 import de.objectcode.time4u.client.store.api.event.ActiveWorkItemRepositoryEvent;
 import de.objectcode.time4u.client.store.api.event.DayInfoRepositoryEvent;
 import de.objectcode.time4u.client.store.api.event.WorkItemRepositoryEvent;
 import de.objectcode.time4u.server.api.data.CalendarDay;
 import de.objectcode.time4u.server.api.data.DayInfo;
 import de.objectcode.time4u.server.api.data.DayInfoSummary;
 import de.objectcode.time4u.server.api.data.SynchronizableType;
 import de.objectcode.time4u.server.api.data.TimePolicy;
 import de.objectcode.time4u.server.api.data.WorkItem;
 import de.objectcode.time4u.server.api.filter.DayInfoFilter;
 import de.objectcode.time4u.server.entities.ActiveWorkItemEntity;
 import de.objectcode.time4u.server.entities.DayInfoEntity;
 import de.objectcode.time4u.server.entities.PersonEntity;
 import de.objectcode.time4u.server.entities.WorkItemEntity;
 import de.objectcode.time4u.server.entities.context.SessionPersistenceContext;
 import de.objectcode.time4u.server.entities.revision.IRevisionGenerator;
 import de.objectcode.time4u.server.entities.revision.IRevisionLock;
 import de.objectcode.time4u.server.entities.revision.SessionRevisionGenerator;
 
 public class HibernateWorkItemRepository implements IWorkItemRepository
 {
   private final HibernateRepository m_repository;
   private final HibernateTemplate m_hibernateTemplate;
 
   HibernateWorkItemRepository(final HibernateRepository repository, final HibernateTemplate hibernateTemplate)
   {
     m_repository = repository;
     m_hibernateTemplate = hibernateTemplate;
   }
 
   /**
    * {@inheritDoc}
    */
   public DayInfo getDayInfo(final CalendarDay day) throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation<DayInfo>() {
       public DayInfo perform(final Session session)
       {
         final Criteria criteria = session.createCriteria(DayInfoEntity.class);
         criteria.add(Restrictions.eq("person.id", m_repository.getOwner().getId()));
         criteria.add(Restrictions.eq("date", day.getDate()));
 
         final DayInfoEntity entity = (DayInfoEntity) criteria.uniqueResult();
 
         if (entity == null) {
           return null;
         }
 
         final DayInfo dayInfo = new DayInfo();
 
         entity.toDTO(dayInfo);
 
         return dayInfo;
       }
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public List<DayInfoSummary> getDayInfoSummaries(final DayInfoFilter filter) throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation<List<DayInfoSummary>>() {
       public List<DayInfoSummary> perform(final Session session)
       {
         final Criteria criteria = session.createCriteria(DayInfoEntity.class);
         criteria.add(Restrictions.eq("person.id", m_repository.getOwner().getId()));
         if (filter.getFrom() != null) {
           criteria.add(Restrictions.ge("date", filter.getFrom().getDate()));
         }
         if (filter.getTo() != null) {
           criteria.add(Restrictions.lt("date", filter.getTo().getDate()));
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
         criteria.addOrder(Order.asc("date"));
 
         final List<?> result = criteria.list();
         final List<DayInfoSummary> dayInfos = new ArrayList<DayInfoSummary>();
 
         for (final Object row : result) {
           final DayInfoEntity entity = (DayInfoEntity) row;
           final DayInfoSummary dayInfo = new DayInfoSummary();
 
           entity.toSummaryDTO(dayInfo);
           dayInfos.add(dayInfo);
         }
 
         return dayInfos;
       }
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public List<DayInfo> getDayInfos(final DayInfoFilter filter) throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation<List<DayInfo>>() {
       public List<DayInfo> perform(final Session session)
       {
         final Criteria criteria = session.createCriteria(DayInfoEntity.class);
         criteria.add(Restrictions.eq("person.id", m_repository.getOwner().getId()));
         if (filter.getFrom() != null) {
           criteria.add(Restrictions.ge("date", filter.getFrom().getDate()));
         }
         if (filter.getTo() != null) {
           criteria.add(Restrictions.lt("date", filter.getTo().getDate()));
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
         criteria.addOrder(Order.asc("date"));
 
         final List<?> result = criteria.list();
         final List<DayInfo> dayInfos = new ArrayList<DayInfo>();
 
         for (final Object row : result) {
           final DayInfoEntity entity = (DayInfoEntity) row;
           final DayInfo dayInfo = new DayInfo();
 
           entity.toDTO(dayInfo);
           dayInfos.add(dayInfo);
         }
 
         return dayInfos;
       }
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public DayInfo storeDayInfo(final DayInfo dayInfo, final boolean modifiedByOwner) throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation<DayInfo>() {
       public DayInfo perform(final Session session)
       {
         final IRevisionGenerator revisionGenerator = new SessionRevisionGenerator(session);
         final IRevisionLock revisionLock = revisionGenerator.getNextRevision(SynchronizableType.DAYINFO, m_repository
             .getOwner().getId());
 
         DayInfoEntity dayInfoEntity = null;
 
         if (dayInfo.getId() == null) {
           dayInfo.setId(m_repository.generateLocalId(SynchronizableType.DAYINFO));
         } else {
           dayInfoEntity = (DayInfoEntity) session.get(DayInfoEntity.class, dayInfo.getId());
         }
 
         if (dayInfoEntity == null) {
           dayInfoEntity = new DayInfoEntity(dayInfo.getId(), revisionLock.getLatestRevision(), dayInfo
               .getLastModifiedByClient(), (PersonEntity) session.get(PersonEntity.class, m_repository.getOwner()
               .getId()), dayInfo.getDay().getDate());
 
           session.persist(dayInfoEntity);
         }
         dayInfoEntity.fromDTO(new SessionPersistenceContext(session), dayInfo);
         dayInfoEntity.setRevision(revisionLock.getLatestRevision());
         if (modifiedByOwner) {
           dayInfoEntity.setLastModifiedByClient(m_repository.getClientId());
         }
         dayInfoEntity.validate();
 
         session.flush();
 
         final DayInfo result = new DayInfo();
 
         dayInfoEntity.toDTO(result);
 
         return result;
       }
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public WorkItem storeWorkItem(final WorkItem workItem) throws RepositoryException
   {
     final DayInfoWorkItemHolder dayInfoWorkItemHolder = m_hibernateTemplate
         .executeInTransaction(new HibernateTemplate.Operation<DayInfoWorkItemHolder>() {
           public DayInfoWorkItemHolder perform(final Session session)
           {
             final IRevisionGenerator revisionGenerator = new SessionRevisionGenerator(session);
             final IRevisionLock revisionLock = revisionGenerator.getNextRevision(SynchronizableType.DAYINFO,
                 m_repository.getOwner().getId());
 
             WorkItemEntity workItemEntity;
 
             if (workItem.getId() != null) {
               workItemEntity = (WorkItemEntity) session.get(WorkItemEntity.class, workItem.getId());
 
               workItemEntity.getDayInfo().setRevision(revisionLock.getLatestRevision());
 
               workItemEntity.fromDTO(new SessionPersistenceContext(session), workItem, m_repository.getClientId());
               workItemEntity.getDayInfo().validate();
               session.flush();
             } else {
               final Criteria criteria = session.createCriteria(DayInfoEntity.class);
               criteria.add(Restrictions.eq("person.id", m_repository.getOwner().getId()));
               criteria.add(Restrictions.eq("date", workItem.getDay().getDate()));
 
               DayInfoEntity dayInfoEntity = (DayInfoEntity) criteria.uniqueResult();
 
               if (dayInfoEntity == null) {
                 // This is save because the revision counter locks this section
                 final String dayInfoId = m_repository.generateLocalId(SynchronizableType.DAYINFO);
 
                 dayInfoEntity = new DayInfoEntity(dayInfoId, revisionLock.getLatestRevision(), m_repository
                     .getClientId(), (PersonEntity) session.get(PersonEntity.class, m_repository.getOwner().getId()),
                     workItem.getDay().getDate());
 
                 session.persist(dayInfoEntity);
               }
               final String workItemId = m_repository.generateLocalId(SynchronizableType.DAYINFO);
               workItemEntity = new WorkItemEntity(workItemId, dayInfoEntity);
 
               workItemEntity.fromDTO(new SessionPersistenceContext(session), workItem, m_repository.getClientId());
 
               dayInfoEntity.getWorkItems().put(workItemEntity.getId(), workItemEntity);
               dayInfoEntity.validate();
               session.flush();
             }
 
             final DayInfo dayInfo = new DayInfo();
 
             workItemEntity.getDayInfo().toDTO(dayInfo);
 
             final WorkItem workItem = new WorkItem();
 
             workItemEntity.toDTO(workItem);
 
             return new DayInfoWorkItemHolder(dayInfo, workItem);
           }
         });
 
     m_repository.fireRepositoryEvent(new DayInfoRepositoryEvent(dayInfoWorkItemHolder.getDayInfo()));
     m_repository.fireRepositoryEvent(new WorkItemRepositoryEvent(dayInfoWorkItemHolder.getDayInfo().getWorkItems()));
 
     return dayInfoWorkItemHolder.getWorkItem();
   }
 
   /**
    * {@inheritDoc}
    */
   public void deleteWorkItem(final WorkItem workItem) throws RepositoryException
   {
     if (workItem.getId() == null) {
       return;
     }
     final DayInfo dayInfo = m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation<DayInfo>() {
       public DayInfo perform(final Session session)
       {
         final IRevisionGenerator revisionGenerator = new SessionRevisionGenerator(session);
         final IRevisionLock revisionLock = revisionGenerator.getNextRevision(SynchronizableType.DAYINFO, m_repository
             .getOwner().getId());
 
         if (workItem.getId() != null) {
           final WorkItemEntity workItemEntity = (WorkItemEntity) session.get(WorkItemEntity.class, workItem.getId());
           final DayInfoEntity dayInfoEntity = workItemEntity.getDayInfo();
 
           dayInfoEntity.setRevision(revisionLock.getLatestRevision());
 
          // Workaround to enforce lazy initialization at this point
          dayInfoEntity.getWorkItems().size();
          dayInfoEntity.getWorkItems().remove(workItemEntity.getId());
           session.delete(workItemEntity);
 
           workItemEntity.getDayInfo().validate();
           session.flush();
 
           final DayInfo dayInfo = new DayInfo();
 
           workItemEntity.getDayInfo().toDTO(dayInfo);
 
           return dayInfo;
         }
         return null;
       }
     });
 
     if (dayInfo != null) {
       m_repository.fireRepositoryEvent(new DayInfoRepositoryEvent(dayInfo));
       m_repository.fireRepositoryEvent(new WorkItemRepositoryEvent(dayInfo.getWorkItems()));
     }
   }
 
   /**
    * {@inheritDoc}
    */
   public WorkItem getActiveWorkItem() throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation<WorkItem>() {
       public WorkItem perform(final Session session)
       {
         final ActiveWorkItemEntity activeWorkItemEntity = (ActiveWorkItemEntity) session.get(
             ActiveWorkItemEntity.class, m_repository.getOwner().getId());
 
         if (activeWorkItemEntity != null && activeWorkItemEntity.getWorkItem() != null) {
           final WorkItem workItem = new WorkItem();
 
           activeWorkItemEntity.getWorkItem().toDTO(workItem);
 
           return workItem;
         }
         return null;
       }
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public void setActiveWorkItem(final WorkItem workItem) throws RepositoryException
   {
     m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation<Object>() {
       public Object perform(final Session session)
       {
         WorkItemEntity workItemEntity = null;
 
         if (workItem != null) {
           workItemEntity = (WorkItemEntity) session.get(WorkItemEntity.class, workItem.getId());
 
           if (workItemEntity == null) {
             throw new RuntimeException("WorkItem " + workItem.getId() + " not found");
           }
         }
 
         final IRevisionGenerator revisionGenerator = new SessionRevisionGenerator(session);
 
         final IRevisionLock revisionLock = revisionGenerator.getNextRevision(SynchronizableType.ACTIVE_WORKITEM,
             m_repository.getOwner().getId());
 
         ActiveWorkItemEntity activeWorkItemEntity = (ActiveWorkItemEntity) session.get(ActiveWorkItemEntity.class,
             m_repository.getOwner().getId());
 
         if (activeWorkItemEntity == null) {
           // This is save because the revision counter locks this section
           activeWorkItemEntity = new ActiveWorkItemEntity(revisionLock.getLatestRevision(), (PersonEntity) session.get(
               PersonEntity.class, m_repository.getOwner().getId()), workItemEntity);
 
           session.persist(activeWorkItemEntity);
         } else {
           activeWorkItemEntity.setRevision(revisionLock.getLatestRevision());
           activeWorkItemEntity.setLastModifiedByClient(m_repository.getClientId());
           activeWorkItemEntity.setWorkItem(workItemEntity);
         }
         session.flush();
 
         return null;
       }
     });
 
     m_repository.fireRepositoryEvent(new ActiveWorkItemRepositoryEvent(workItem));
   }
 
   public List<TimePolicy> getTimePolicies() throws RepositoryException
   {
     // TODO: Store this
     return Collections.emptyList();
   }
 
   private static class DayInfoWorkItemHolder
   {
     private final DayInfo m_dayInfo;
     private final WorkItem m_workItem;
 
     private DayInfoWorkItemHolder(final DayInfo dayInfo, final WorkItem workItem)
     {
       m_dayInfo = dayInfo;
       m_workItem = workItem;
     }
 
     public DayInfo getDayInfo()
     {
       return m_dayInfo;
     }
 
     public WorkItem getWorkItem()
     {
       return m_workItem;
     }
   }
 }
