 package com.vaadin.training.bugrap.domain.repository;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.vaadin.training.bugrap.domain.entity.*;
 
 import javax.persistence.TypedQuery;
 import java.util.List;
 
 /**
  * @author Marcus Hellberg (marcus@vaadin.com)
  */
 public class ReportRepository extends AbstractRepository<Report> {
     @Override
     protected Class<Report> getEntityClass() {
         return Report.class;
     }
 
     public List<Report> findReports(ReportQuery reportQuery) {
         User assignee = reportQuery.getAssignee();
         ProjectVersion version = reportQuery.getVersion();
         ReportStatus status = reportQuery.getStatus();
         List<ReportResolution> resolutions = reportQuery.getResolutions();
 
         StringBuilder stringBuilder = new StringBuilder();
         stringBuilder.append("select r from Report r ");
 
         List<String> criteria = Lists.newLinkedList();
 
         if (assignee != null) {
             criteria.add("r.assigned.id = :assigneeId");
         }
 
         if (version != null) {
             criteria.add("r.projectVersion.id = :versionId");
         }
 
         if (status != null) {
             criteria.add("r.status = :status");
         }
 
         if (!resolutions.isEmpty()) {
            criteria.add("r.resolution in :resolutions");
         }
 
         if (!criteria.isEmpty()) {
             stringBuilder.append("where ");
             stringBuilder.append(Joiner.on(" and ").join(criteria));
         }
 
         String queryString = stringBuilder.toString();
 
         TypedQuery<Report> query = em.createQuery(queryString, Report.class);
 
 
         if (assignee != null) {
             query.setParameter("assigneeId", assignee.getId());
         }
         if (version != null) {
             query.setParameter("versionId", version.getId());
         }
         if (status != null) {
             query.setParameter("status", status);
         }
         if (!resolutions.isEmpty()) {
            query.setParameter("resolutions", resolutions);
         }
 
         List<Report> resultList = query.getResultList();
         return resultList;
     }
 }
