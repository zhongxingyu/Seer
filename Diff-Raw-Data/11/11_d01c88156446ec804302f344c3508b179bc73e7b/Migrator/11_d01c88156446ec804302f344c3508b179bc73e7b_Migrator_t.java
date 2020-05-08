 package de.objectcode.time4u.migrator.server05;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.hibernate.criterion.Restrictions;
 
 import de.objectcode.time4u.migrator.server05.old.entities.OldPersons;
 import de.objectcode.time4u.migrator.server05.old.entities.OldPersonsToTeams;
 import de.objectcode.time4u.migrator.server05.old.entities.OldProjects;
 import de.objectcode.time4u.migrator.server05.old.entities.OldTasks;
 import de.objectcode.time4u.migrator.server05.old.entities.OldTeams;
 import de.objectcode.time4u.migrator.server05.old.entities.OldWorkitems;
 import de.objectcode.time4u.migrator.server05.parts.IMigratorPart;
 import de.objectcode.time4u.migrator.server05.parts.PersonMigratorPart;
 import de.objectcode.time4u.migrator.server05.parts.ProjectMigratorPart;
 import de.objectcode.time4u.migrator.server05.parts.TaskMigratorPart;
 import de.objectcode.time4u.migrator.server05.parts.TeamMigratorPart;
 import de.objectcode.time4u.migrator.server05.parts.WorkItemMigrator;
 import de.objectcode.time4u.server.entities.ActiveWorkItemEntity;
 import de.objectcode.time4u.server.entities.ClientEntity;
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
 import de.objectcode.time4u.server.entities.TodoEntity;
 import de.objectcode.time4u.server.entities.TodoMetaPropertyEntity;
 import de.objectcode.time4u.server.entities.WorkItemEntity;
 import de.objectcode.time4u.server.entities.account.UserAccountEntity;
 import de.objectcode.time4u.server.entities.account.UserRoleEntity;
 import de.objectcode.time4u.server.entities.revision.ILocalIdGenerator;
 import de.objectcode.time4u.server.entities.revision.LocalIdEntity;
 import de.objectcode.time4u.server.entities.revision.RevisionEntity;
 import de.objectcode.time4u.server.entities.revision.SessionLocalIdGenerator;
 import de.objectcode.time4u.server.entities.sync.ServerConnectionEntity;
 import de.objectcode.time4u.server.entities.sync.SynchronizationStatusEntity;
 
 public class Migrator
 {
   SessionFactory m_oldSessionFactory;
   SessionFactory m_newSessionFactory;
   List<IMigratorPart> m_migratorParts;
 
   Migrator()
   {
     m_oldSessionFactory = buildOldSessionFactory();
     m_newSessionFactory = buildNewSessionFactory();
 
     m_migratorParts = new ArrayList<IMigratorPart>();
     m_migratorParts.add(new ProjectMigratorPart());
     m_migratorParts.add(new TaskMigratorPart());
     m_migratorParts.add(new PersonMigratorPart());
     m_migratorParts.add(new TeamMigratorPart());
     m_migratorParts.add(new WorkItemMigrator());
   }
 
   private SessionFactory buildNewSessionFactory()
   {
     final AnnotationConfiguration cfg = new AnnotationConfiguration();
 
     cfg.configure("new-version.cfg.xml");
 
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
     cfg.addAnnotatedClass(TodoEntity.class);
     cfg.addAnnotatedClass(TodoMetaPropertyEntity.class);
     cfg.addAnnotatedClass(ActiveWorkItemEntity.class);
     cfg.addAnnotatedClass(ServerConnectionEntity.class);
     cfg.addAnnotatedClass(SynchronizationStatusEntity.class);
     cfg.addAnnotatedClass(ClientEntity.class);
     cfg.addAnnotatedClass(UserAccountEntity.class);
     cfg.addAnnotatedClass(UserRoleEntity.class);
 
     return cfg.buildSessionFactory();
   }
 
   private SessionFactory buildOldSessionFactory()
   {
     final AnnotationConfiguration cfg = new AnnotationConfiguration();
 
     cfg.configure("old-version.cfg.xml");
 
     cfg.addAnnotatedClass(OldProjects.class);
     cfg.addAnnotatedClass(OldTasks.class);
     cfg.addAnnotatedClass(OldPersons.class);
     cfg.addAnnotatedClass(OldTeams.class);
     cfg.addAnnotatedClass(OldPersonsToTeams.class);
     cfg.addAnnotatedClass(OldWorkitems.class);
 
     return cfg.buildSessionFactory();
   }
 
   public void run()
   {
     final Session newSession = m_newSessionFactory.openSession();
     final Transaction trx = newSession.beginTransaction();
     final Criteria clientCriteria = newSession.createCriteria(ClientEntity.class);
     clientCriteria.add(Restrictions.eq("myself", true));
 
     final ClientEntity clientEntity = (ClientEntity) clientCriteria.uniqueResult();
 
     trx.rollback();
     newSession.close();
 
     final ILocalIdGenerator idGenerator = new SessionLocalIdGenerator(m_newSessionFactory, clientEntity.getClientId());
 
     if (clientEntity == null) {
       throw new RuntimeException("ClientEntity not found");
     }
 
     for (final IMigratorPart part : m_migratorParts) {
       part.migrate(idGenerator, m_oldSessionFactory, m_newSessionFactory);
     }
   }
 
   public static void main(final String args[])
   {
     final Migrator migrator = new Migrator();
 
     migrator.run();
   }
 }
