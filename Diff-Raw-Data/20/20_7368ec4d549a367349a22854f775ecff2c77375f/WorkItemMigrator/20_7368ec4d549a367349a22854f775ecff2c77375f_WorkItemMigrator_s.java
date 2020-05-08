 package de.objectcode.time4u.migrator.server05.parts;
 
 import java.util.Calendar;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 
 import de.objectcode.time4u.migrator.server05.old.entities.OldWorkitems;
 import de.objectcode.time4u.server.api.data.CalendarDay;
 import de.objectcode.time4u.server.api.data.EntityType;
 import de.objectcode.time4u.server.entities.DayInfoEntity;
 import de.objectcode.time4u.server.entities.ProjectEntity;
 import de.objectcode.time4u.server.entities.TaskEntity;
 import de.objectcode.time4u.server.entities.WorkItemEntity;
 import de.objectcode.time4u.server.entities.revision.IRevisionLock;
 
 public class WorkItemMigrator extends BasePersonalizedMigratorPart<OldWorkitems>
 {
   public WorkItemMigrator()
   {
     super(EntityType.DAYINFO, OldWorkitems.class, "personId");
   }
 
   @Override
   protected void migrateEntity(final Session oldSession, final Session newSession, final OldWorkitems oldEntity,
       final IRevisionLock revisionLock)
   {
     final Calendar begin = Calendar.getInstance();
     begin.setTime(oldEntity.getWbegin());
 
     final CalendarDay day = new CalendarDay(begin);
 
     final Criteria criteria = newSession.createCriteria(DayInfoEntity.class);
     criteria.add(Restrictions.eq("person", m_newPerson));
     criteria.add(Restrictions.eq("date", day.getDate()));
 
     DayInfoEntity dayInfoEntity = (DayInfoEntity) criteria.uniqueResult();
 
     if (dayInfoEntity == null) {
       dayInfoEntity = new DayInfoEntity(m_idGenerator.generateLocalId(EntityType.DAYINFO), revisionLock
           .getLatestRevision(), m_idGenerator.getClientId(), m_newPerson, day.getDate());
 
       newSession.persist(dayInfoEntity);
     } else {
       dayInfoEntity.setRevision(revisionLock.getLatestRevision());
     }
 
    final WorkItemEntity workItemEntity = new WorkItemEntity(migrateId(EntityType.DAYINFO, oldEntity.getId()),
         dayInfoEntity);
 
     workItemEntity.setBegin(begin.get(Calendar.HOUR_OF_DAY) * 3600 + begin.get(Calendar.MINUTE) * 60
         + begin.get(Calendar.SECOND));
     workItemEntity.setEnd(workItemEntity.getBegin()
         + (int) ((oldEntity.getWend().getTime() - oldEntity.getWbegin().getTime()) / 1000));
     workItemEntity.setComment(oldEntity.getWcomment());
     workItemEntity.setProject((ProjectEntity) newSession.get(ProjectEntity.class, migrateId(EntityType.PROJECT,
         oldEntity.getProjectId())));
     workItemEntity.setTask((TaskEntity) newSession.get(TaskEntity.class, migrateId(EntityType.TASK, oldEntity
         .getTaskId())));
 
     dayInfoEntity.getWorkItems().put(workItemEntity.getId(), workItemEntity);
     newSession.merge(workItemEntity);
     dayInfoEntity.validate();
 
     newSession.merge(dayInfoEntity);
   }
 }
