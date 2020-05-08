 package com.vaadin.training.bugrap.domain.repository;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.vaadin.training.bugrap.domain.entity.Comment;
 import com.vaadin.training.bugrap.domain.entity.Report;
 
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
 
         String queryString = generateQueryString(reportQuery, reportQuery.getSearchTerm());
 
         TypedQuery<Report> query = em.createQuery(queryString, Report.class);
 
         assignParameters(reportQuery, query);
 
         return query.getResultList();
     }
 
     private String generateQueryString(ReportQuery reportQuery, String searchTerm) {
         StringBuilder stringBuilder = new StringBuilder();
         stringBuilder.append("select r from Report r ");
 
         boolean criteriaAdded = addBaseCriteria(reportQuery, stringBuilder);
 
         addSearchCriteria(searchTerm, stringBuilder, criteriaAdded);
 
         return stringBuilder.toString();
     }
 
     private boolean addBaseCriteria(ReportQuery reportQuery, StringBuilder stringBuilder) {
         List<String> criteria = Lists.newLinkedList();
 
         if (reportQuery.getAssignee() != null) {
             criteria.add("r.assigned.id = :assigneeId");
         }
 
         if (reportQuery.getVersion() != null) {
             criteria.add("r.projectVersion.id = :versionId");
         }
 
         if (reportQuery.getStatus() != null) {
             criteria.add("r.status = :status");
         }
 
         if (!reportQuery.getResolutions().isEmpty()) {
             criteria.add("r.resolution in :resolutions");
         }
 
         boolean criteriaAdded = false;
 
         if (!criteria.isEmpty()) {
             stringBuilder.append("where ");
             criteriaAdded = true;
             stringBuilder.append(Joiner.on(" and ").join(criteria));
         }
         return criteriaAdded;
     }
 
     private void addSearchCriteria(String searchTerm, StringBuilder stringBuilder, boolean criteriaAdded) {
         List<String> criteria = Lists.newLinkedList();
 
         if (searchTerm != null && !searchTerm.isEmpty()) {
             criteria.add("upper(r.summary) like :summary");
             criteria.add("upper(r.description) like :description");
         }
 
         if (!criteria.isEmpty()) {
             if (criteriaAdded) {
                 stringBuilder.append("and (");
             } else {
                 stringBuilder.append("where (");
             }
 
             stringBuilder.append(Joiner.on(" or ").join(criteria));
             stringBuilder.append(")");
         }
     }
 
     private void assignParameters(ReportQuery reportQuery, TypedQuery<Report> query) {
         if (reportQuery.getAssignee() != null) {
             query.setParameter("assigneeId", reportQuery.getAssignee().getId());
         }
         if (reportQuery.getVersion() != null) {
             query.setParameter("versionId", reportQuery.getVersion().getId());
         }
         if (reportQuery.getStatus() != null) {
             query.setParameter("status", reportQuery.getStatus());
         }
         if (!reportQuery.getResolutions().isEmpty()) {
             query.setParameter("resolutions", reportQuery.getResolutions());
         }
 
         String searchTerm = reportQuery.getSearchTerm();
         if (searchTerm != null && !searchTerm.isEmpty()) {
             String searchWildcard = "%" + searchTerm.toUpperCase() + "%";
             query.setParameter("summary", searchWildcard);
             query.setParameter("description", searchWildcard);
         }
     }
 
     public List<Comment> findComments(Report report) {
        TypedQuery<Comment> query = em.createNamedQuery(Comment.FIND_BY_REPORT, Comment.class);
 
         query.setParameter("reportId", report.getId());
 
         return query.getResultList();
     }
 }
