 package de.objectcode.time4u.server.ejb.seam.impl;
 
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ejb.Local;
 import javax.ejb.Stateless;
 import javax.el.ELContext;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.AutoCreate;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.security.Identity;
 
 import de.objectcode.time4u.server.ejb.seam.api.IReportServiceLocal;
 import de.objectcode.time4u.server.ejb.seam.api.report.BaseReportDefinition;
 import de.objectcode.time4u.server.ejb.seam.api.report.CrossTableResult;
 import de.objectcode.time4u.server.ejb.seam.api.report.IReportDataCollector;
 import de.objectcode.time4u.server.ejb.seam.api.report.ReportResult;
 import de.objectcode.time4u.server.ejb.seam.api.report.parameter.BaseParameterValue;
 import de.objectcode.time4u.server.ejb.util.ReportEL;
 import de.objectcode.time4u.server.entities.DayInfoEntity;
 import de.objectcode.time4u.server.entities.PersonEntity;
 import de.objectcode.time4u.server.entities.ProjectEntity;
 import de.objectcode.time4u.server.entities.TaskEntity;
 import de.objectcode.time4u.server.entities.TeamEntity;
 import de.objectcode.time4u.server.entities.TodoEntity;
 import de.objectcode.time4u.server.entities.WorkItemEntity;
 import de.objectcode.time4u.server.entities.account.UserAccountEntity;
 
 @Stateless
 @Local(IReportServiceLocal.class)
 @org.jboss.annotation.ejb.LocalBinding(jndiBinding = "time4u-server/seam/ReportServiceSeam/local")
 @org.jboss.ejb3.annotation.LocalBinding(jndiBinding = "time4u-server/seam/ReportServiceSeam/local")
 @Name("ReportService")
 @AutoCreate
 @Scope(ScopeType.CONVERSATION)
 public class ReportServiceSeam implements IReportServiceLocal
 {
   @PersistenceContext(unitName = "time4u")
   private EntityManager m_manager;
 
   @In("org.jboss.seam.security.identity")
   Identity m_identity;
 
   @SuppressWarnings("unchecked")
   public ReportResult generateReport(final BaseReportDefinition reportDefinition,
       final Map<String, BaseParameterValue> parameters)
   {
     final UserAccountEntity userAccount = m_manager.find(UserAccountEntity.class, m_identity.getPrincipal().getName());
     final Set<String> allowedPersonIds = new HashSet<String>();
 
     allowedPersonIds.add(userAccount.getPerson().getId());
     for (final TeamEntity team : userAccount.getPerson().getResponsibleFor()) {
       for (final PersonEntity member : team.getMembers()) {
         allowedPersonIds.add(member.getId());
       }
     }
 
     final StringBuffer queryStr = new StringBuffer();
     String orderStr = null;
     IRowDataIterator<?> rowDataIterator = null;
     switch (reportDefinition.getEntityType()) {
       case WORKITEM:
         queryStr.append("from ");
         queryStr.append(WorkItemEntity.class.getName());
         queryStr.append(" w join fetch w.dayInfo where w.dayInfo.person.id in (:allowedPersons)");
         orderStr = " order by w.dayInfo.date asc, w.begin asc";
         rowDataIterator = new WorkItemRowDataIterator();
         break;
       case DAYINFO:
         queryStr.append("select distinct d from ");
         queryStr.append(DayInfoEntity.class.getName());
        queryStr.append(" d left outer join fetch d.tags left outer join fetch d.workItems join fetch d.person where d.person.id in (:allowedPersons)");
         orderStr = " order by d.date asc";
         if (reportDefinition.isFill()) {
           rowDataIterator = new DayInfoRowDataIteratorWithFill();
         } else {
           rowDataIterator = new DayInfoRowDataIterator();
         }
         break;
       case TODO:
         queryStr.append("from ");
         queryStr.append(TodoEntity.class.getName());
         queryStr.append(" t where t.reporter.id in (:allowedPersons)");
         orderStr = " order by t.header asc";
         rowDataIterator = new TodoRowDataIterator();
         break;
     }
 
     final ELContext context = ReportEL.createELContext();
 
     for (final Map.Entry<String, BaseParameterValue> parameter : parameters.entrySet()) {
       context.getVariableMapper().setVariable(parameter.getKey(),
           ReportEL.getExpressionFactory().createValueExpression(parameter.getValue(), Object.class));
     }
 
     if (reportDefinition.getFilter() != null) {
       queryStr.append(" and ");
       queryStr.append(reportDefinition.getFilter()
           .getWhereClause(reportDefinition.getEntityType(), parameters, context));
     }
 
     queryStr.append(orderStr);
 
     final Query query = m_manager.createQuery(queryStr.toString());
 
     query.setParameter("allowedPersons", allowedPersonIds);
     if (reportDefinition.getFilter() != null) {
       reportDefinition.getFilter().setQueryParameters(reportDefinition.getEntityType(), query, parameters, context);
     }
 
     final IReportDataCollector dataCollector = reportDefinition.createDataCollector();
 
     rowDataIterator.iterate(query.getResultList(), dataCollector);
 
     m_manager.clear();
 
     return dataCollector.getReportResult();
   }
 
   @SuppressWarnings("unchecked")
   public CrossTableResult generateProjectPersonCrossTableOrig(final String mainProjectId, final Date from,
       final Date until)
   {
     final UserAccountEntity userAccount = m_manager.find(UserAccountEntity.class, m_identity.getPrincipal().getName());
     final Set<String> allowedPersonIds = new HashSet<String>();
 
     allowedPersonIds.add(userAccount.getPerson().getId());
     for (final TeamEntity team : userAccount.getPerson().getResponsibleFor()) {
       for (final PersonEntity member : team.getMembers()) {
         allowedPersonIds.add(member.getId());
       }
     }
 
     ProjectEntity mainProject = null;
 
     if (mainProjectId != null) {
       mainProject = m_manager.find(ProjectEntity.class, mainProjectId);
     }
 
     final StringBuffer queryStr = new StringBuffer("from ");
     queryStr.append(WorkItemEntity.class.getName());
     queryStr.append(" w join fetch w.dayInfo where w.dayInfo.person.id in (:allowedPersons)");
     queryStr.append(" and (w.dayInfo.date >= :from and w.dayInfo.date < :until)");
     if (mainProject != null) {
       queryStr.append(" and w.project.parentKey like :parentKey");
     }
     queryStr.append(" order by w.dayInfo.date asc, w.begin asc");
 
     final Query query = m_manager.createQuery(queryStr.toString());
 
     query.setParameter("allowedPersons", allowedPersonIds);
     query.setParameter("from", from);
     query.setParameter("until", until);
     if (mainProject != null) {
       query.setParameter("parentKey", mainProject.getParentKey() + "%");
     }
 
     final ProjectPersonCrosstableDataCollector dataCollector = new ProjectPersonCrosstableDataCollector(mainProject);
     final IRowDataIterator<?> rowDataIterator = new WorkItemRowDataIterator();
 
     rowDataIterator.iterate(query.getResultList(), dataCollector);
 
     m_manager.clear();
 
     return dataCollector.getCrossTable();
   }
 
   public CrossTableResult generateProjectPersonCrossTable(final String mainProjectId, final Date from, final Date until)
   {
     final UserAccountEntity userAccount = m_manager.find(UserAccountEntity.class, m_identity.getPrincipal().getName());
 
     final StringBuffer newQueryString = new StringBuffer();
     // The inner joins are redundant due to the WHERE conditions.
     // We use them to force Hibernate to use inner joins instead of
     // the from a,b-notation because of a bug in hibernate 3.2.4. hibernate
     // does not calculate the right join order when mixing "," and "join"
     newQueryString.append("SELECT ");
     newQueryString.append("     wi.dayInfo.person, ");
     newQueryString.append("     wi.project, ");
     newQueryString.append("     SUM(wi.end-wi.begin) ");
     newQueryString.append("FROM ");
     newQueryString.append(WorkItemEntity.class.getName()).append(" wi ");
     newQueryString.append(" INNER JOIN wi.dayInfo AS di");
     newQueryString.append(" INNER JOIN di.person ");
     newQueryString.append(" INNER JOIN wi.project ");
     newQueryString.append(" INNER JOIN wi.dayInfo.person.memberOf team ");
     newQueryString.append(" INNER JOIN team.owners owner ");
     newQueryString.append("WHERE ");
     newQueryString.append("     wi.dayInfo.date >= :from AND ");
    newQueryString.append("     wi.dayInfo.date <= :until AND ");
     newQueryString.append(" (owner.id = :userId OR wi.dayInfo.person.id = :userId) ");
     newQueryString.append("GROUP BY ");
     newQueryString.append("     wi.dayInfo.person, ");
     newQueryString.append("     wi.project");
 
     final Query query = m_manager.createQuery(newQueryString.toString());
     query.setParameter("userId", userAccount.getPerson().getId());
     query.setParameter("from", from);
     query.setParameter("until", until);
 
     ProjectEntity mainProject = null;
     if (mainProjectId != null) {
       mainProject = m_manager.find(ProjectEntity.class, mainProjectId);
     }
 
     final GenericProjectCrosstableDataCollector<PersonEntity> dataCollector = new GenericProjectCrosstableDataCollector<PersonEntity>(
         new PersonComparator(), mainProject);
 
     for (final Object row : query.getResultList()) {
       final PersonEntity person = (PersonEntity) ((Object[]) row)[0];
       final ProjectEntity project = (ProjectEntity) ((Object[]) row)[1];
       final int aggregate = ((Long) ((Object[]) row)[2]).intValue();
 
       final GenericCrosstableDataCollector.RowDataAdaptor<ProjectEntity, PersonEntity> rowDataAdapter = new GenericCrosstableDataCollector.RowDataAdaptor<ProjectEntity, PersonEntity>(
           project, person, aggregate);
 
       dataCollector.collect(rowDataAdapter);
     }
     dataCollector.finish();
     m_manager.clear();
 
     return dataCollector.getCrossTable();
   }
 
   public CrossTableResult generateProjectTeamCrossTable(final String mainProjectId, final Date from, final Date until)
   {
     final UserAccountEntity userAccount = m_manager.find(UserAccountEntity.class, m_identity.getPrincipal().getName());
 
     final StringBuffer newQueryString = new StringBuffer();
     newQueryString.append("SELECT ");
     newQueryString.append("     team, ");
     newQueryString.append("     wi.project, ");
     newQueryString.append("     SUM(wi.end-wi.begin) ");
     newQueryString.append("FROM ");
     newQueryString.append(WorkItemEntity.class.getName()).append(" wi ");
     newQueryString.append(" INNER JOIN wi.dayInfo.person.memberOf team ");
     newQueryString.append(" INNER JOIN team.owners owner ");
     newQueryString.append("WHERE ");
     newQueryString.append(" wi.dayInfo.date >= :from AND ");
    newQueryString.append(" wi.dayInfo.date <= :until AND ");
     newQueryString.append(" (owner.id = :userId OR wi.dayInfo.person.id = :userId) ");
     newQueryString.append("GROUP BY ");
     newQueryString.append("     team, ");
     newQueryString.append("     wi.project");
 
     final Query query = m_manager.createQuery(newQueryString.toString());
     query.setParameter("from", from);
     query.setParameter("until", until);
     query.setParameter("userId", userAccount.getPerson().getId());
 
     ProjectEntity mainProject = null;
     if (mainProjectId != null) {
       mainProject = m_manager.find(ProjectEntity.class, mainProjectId);
     }
 
     final GenericProjectCrosstableDataCollector<TeamEntity> dataCollector = new GenericProjectCrosstableDataCollector<TeamEntity>(
         new TeamComparator(), mainProject);
 
     for (final Object row : query.getResultList()) {
       final TeamEntity team = (TeamEntity) ((Object[]) row)[0];
       final ProjectEntity project = (ProjectEntity) ((Object[]) row)[1];
       final int aggregate = ((Long) ((Object[]) row)[2]).intValue();
 
       final GenericCrosstableDataCollector.RowDataAdaptor<ProjectEntity, TeamEntity> rowDataAdapter = new GenericCrosstableDataCollector.RowDataAdaptor<ProjectEntity, TeamEntity>(
           project, team, aggregate);
 
       dataCollector.collect(rowDataAdapter);
     }
     dataCollector.finish();
     m_manager.clear();
 
     return dataCollector.getCrossTable();
   }
 
   public CrossTableResult generateTaskTeamCrossTable(final String mainProjectId, final Date from, final Date until)
   {
     final UserAccountEntity userAccount = m_manager.find(UserAccountEntity.class, m_identity.getPrincipal().getName());
 
     final StringBuffer newQueryString = new StringBuffer();
     newQueryString.append("SELECT ");
     newQueryString.append("     team, ");
     newQueryString.append("     wi.task, ");
     newQueryString.append("     SUM(wi.end-wi.begin) ");
     newQueryString.append("FROM ");
     newQueryString.append(WorkItemEntity.class.getName()).append(" wi ");
     newQueryString.append(" INNER JOIN wi.dayInfo.person.memberOf team ");
     newQueryString.append(" INNER JOIN team.owners owner ");
     newQueryString.append("WHERE ");
     newQueryString.append(" wi.task.project.id = :projectId AND ");
     newQueryString.append(" wi.dayInfo.date >= :from AND ");
    newQueryString.append(" wi.dayInfo.date <= :until AND ");
     newQueryString.append(" (owner.id = :userId OR wi.dayInfo.person.id = :userId) ");
     newQueryString.append("GROUP BY ");
     newQueryString.append("     team, ");
     newQueryString.append("     wi.task");
 
     final Query query = m_manager.createQuery(newQueryString.toString());
     query.setParameter("projectId", mainProjectId);
     query.setParameter("from", from);
     query.setParameter("until", until);
     query.setParameter("userId", userAccount.getPerson().getId());
 
     final GenericTaskCrosstableDataCollector<TeamEntity> dataCollector = new GenericTaskCrosstableDataCollector<TeamEntity>(
         new TeamComparator());
 
     for (final Object row : query.getResultList()) {
       final TeamEntity team = (TeamEntity) ((Object[]) row)[0];
       final TaskEntity task = (TaskEntity) ((Object[]) row)[1];
       final int aggregate = ((Long) ((Object[]) row)[2]).intValue();
 
       final GenericCrosstableDataCollector.RowDataAdaptor<TaskEntity, TeamEntity> rowDataAdapter = new GenericCrosstableDataCollector.RowDataAdaptor<TaskEntity, TeamEntity>(
           task, team, aggregate);
 
       dataCollector.collect(rowDataAdapter);
     }
     dataCollector.finish();
     m_manager.clear();
 
     return dataCollector.getCrossTable();
   }
 
   public CrossTableResult generateTaskPersonCrossTable(final String mainProjectId, final Date from, final Date until)
   {
     final UserAccountEntity userAccount = m_manager.find(UserAccountEntity.class, m_identity.getPrincipal().getName());
 
     final StringBuffer newQueryString = new StringBuffer();
     newQueryString.append("SELECT ");
     newQueryString.append("     wi.dayInfo.person, ");
     newQueryString.append("     wi.task, ");
     newQueryString.append("     SUM(wi.end-wi.begin) ");
     newQueryString.append("FROM ");
     newQueryString.append(WorkItemEntity.class.getName()).append(" wi ");
     newQueryString.append(" INNER JOIN wi.dayInfo.person.memberOf team ");
     newQueryString.append(" INNER JOIN team.owners owner ");
     newQueryString.append("WHERE ");
     newQueryString.append(" wi.task.project.id = :projectId AND ");
     newQueryString.append(" wi.dayInfo.date >= :from AND ");
    newQueryString.append(" wi.dayInfo.date <= :until AND ");
     newQueryString.append(" (owner.id = :userId OR wi.dayInfo.person.id = :userId) ");
     newQueryString.append("GROUP BY ");
     newQueryString.append("     wi.dayInfo.person, ");
     newQueryString.append("     wi.task");
 
     final Query query = m_manager.createQuery(newQueryString.toString());
     query.setParameter("projectId", mainProjectId);
     query.setParameter("from", from);
     query.setParameter("until", until);
     query.setParameter("userId", userAccount.getPerson().getId());
 
     final GenericTaskCrosstableDataCollector<PersonEntity> dataCollector = new GenericTaskCrosstableDataCollector<PersonEntity>(
         new PersonComparator());
 
     for (final Object row : query.getResultList()) {
       final PersonEntity person = (PersonEntity) ((Object[]) row)[0];
       final TaskEntity task = (TaskEntity) ((Object[]) row)[1];
       final int aggregate = ((Long) ((Object[]) row)[2]).intValue();
 
       final GenericCrosstableDataCollector.RowDataAdaptor<TaskEntity, PersonEntity> rowDataAdapter = new GenericCrosstableDataCollector.RowDataAdaptor<TaskEntity, PersonEntity>(
           task, person, aggregate);
 
       dataCollector.collect(rowDataAdapter);
     }
     dataCollector.finish();
     m_manager.clear();
 
     return dataCollector.getCrossTable();
   }
 
   private static class PersonComparator implements Comparator<PersonEntity>
   {
     public int compare(final PersonEntity o1, final PersonEntity o2)
     {
       final String name1 = o1.getSurname() + " " + o1.getGivenName();
       final String name2 = o2.getSurname() + " " + o2.getGivenName();
       final int result = name1.compareTo(name2);
 
       if (result != 0) {
         return result;
       }
 
       return o1.getId().compareTo(o2.getId());
     }
   }
 
   private class TeamComparator implements Comparator<TeamEntity>
   {
 
     public int compare(final TeamEntity arg0, final TeamEntity arg1)
     {
       return arg0.getName().compareTo(arg1.getName());
     }
 
   }
 }
