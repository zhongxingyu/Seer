 package de.objectcode.time4u.migrator.server05;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
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
 import de.objectcode.time4u.server.entities.TimePolicyEntity;
 import de.objectcode.time4u.server.entities.TodoAssignmentEntity;
 import de.objectcode.time4u.server.entities.TodoBaseEntity;
 import de.objectcode.time4u.server.entities.TodoEntity;
 import de.objectcode.time4u.server.entities.TodoGroupEntity;
 import de.objectcode.time4u.server.entities.TodoMetaPropertyEntity;
 import de.objectcode.time4u.server.entities.WeekTimePolicyEntity;
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
 
   Migrator(final String[] args) throws Exception
   {
     List<String> userIds = null;
 
    if (args.length > 0) {
       if (args.length == 2 || "-f".equals(args[0])) {
         userIds = readFromFile(args[1]);
       } else {
         userIds = new ArrayList<String>();
         for (final String userId : args) {
           userIds.add(userId);
         }
       }
 
       System.out.println("Restrict to users: " + userIds);
     }
 
     m_oldSessionFactory = buildOldSessionFactory();
     m_newSessionFactory = buildNewSessionFactory();
 
     m_migratorParts = new ArrayList<IMigratorPart>();
     m_migratorParts.add(new ProjectMigratorPart());
     m_migratorParts.add(new TaskMigratorPart());
     if (args.length > 0) {
       m_migratorParts.add(new PersonMigratorPart(userIds));
     } else {
       m_migratorParts.add(new PersonMigratorPart());
     }
     m_migratorParts.add(new TeamMigratorPart());
     if (args.length > 0) {
       m_migratorParts.add(new WorkItemMigrator(userIds));
     } else {
       m_migratorParts.add(new WorkItemMigrator());
     }
   }
 
   private List<String> readFromFile(final String fileName)
   {
     try {
       final List<String> result = new ArrayList<String>();
       final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
       String line;
 
       while ((line = reader.readLine()) != null) {
         if (line.trim().length() > 0) {
           result.add(line.trim());
         }
       }
 
       return result;
     } catch (final Exception e) {
       e.printStackTrace();
       throw new RuntimeException("IOException");
     }
   }
 
   private SessionFactory buildNewSessionFactory() throws Exception
   {
     final AnnotationConfiguration cfg = new AnnotationConfiguration();
 
     cfg.configure("new-version.cfg.xml");
 
     final Properties properties = new Properties();
 
     properties.load(getClass().getClassLoader().getResourceAsStream("new-version.properties"));
     for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
       cfg.setProperty(entry.getKey().toString(), entry.getValue().toString());
     }
 
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
     cfg.addAnnotatedClass(ActiveWorkItemEntity.class);
     cfg.addAnnotatedClass(ServerConnectionEntity.class);
     cfg.addAnnotatedClass(SynchronizationStatusEntity.class);
     cfg.addAnnotatedClass(ClientEntity.class);
     cfg.addAnnotatedClass(UserAccountEntity.class);
     cfg.addAnnotatedClass(UserRoleEntity.class);
     cfg.addAnnotatedClass(TimePolicyEntity.class);
     cfg.addAnnotatedClass(WeekTimePolicyEntity.class);
 
     return cfg.buildSessionFactory();
   }
 
   private SessionFactory buildOldSessionFactory() throws Exception
   {
     final AnnotationConfiguration cfg = new AnnotationConfiguration();
 
     cfg.configure("old-version.cfg.xml");
 
     final Properties properties = new Properties();
 
     properties.load(getClass().getClassLoader().getResourceAsStream("old-version.properties"));
     for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
       cfg.setProperty(entry.getKey().toString(), entry.getValue().toString());
     }
 
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
     try {
       final Migrator migrator = new Migrator(args);
 
       migrator.run();
     } catch (final Exception e) {
       e.printStackTrace();
     }
   }
 }
