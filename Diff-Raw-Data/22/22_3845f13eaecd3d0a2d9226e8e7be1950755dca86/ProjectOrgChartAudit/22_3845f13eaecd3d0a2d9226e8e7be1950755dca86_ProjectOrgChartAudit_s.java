 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  *
  * @author mcculley
  */
 public class ProjectOrgChartAudit implements Audit {
 
     private final int project;
     private final ConnectionFactory connectionFactory;
     private final OrganizationHierarchy organizationHierarchy;
     private final Directory directory;
 
     public ProjectOrgChartAudit(int project, ConnectionFactory connectionFactory, OrganizationHierarchy organizationHierarchy, Directory directory) {
         this.project = project;
         this.connectionFactory = connectionFactory;
         this.organizationHierarchy = organizationHierarchy;
         this.directory = directory;
     }
 
     public String getDisplayName() {
         return "Project Organization Hierarchy";
     }
 
     public Collection<AuditResult> getResults() {
         try {
             Connection connection = connectionFactory.openConnection();
             try {
                PreparedStatement p = connection.prepareStatement("SELECT project_managers.employee AS manager, hours.employee, hours.date FROM hours JOIN tasks ON hours.task = tasks.id JOIN project_managers ON project_managers.project = tasks.project WHERE hours.date >= DATE_SUB(NOW(), INTERVAL 7 DAY) AND tasks.project = ?;");
                 try {
                     p.setInt(1, project);
                     ResultSet rs = p.executeQuery();
                     try {
                         Set<Integer> missing = new HashSet<Integer>();
                         while (rs.next()) {
                             int manager = rs.getInt("manager");
                             int employee = rs.getInt("employee");
                             Date date = rs.getDate("date");
                            if (employee != manager) {
                                Collection<Integer> orgChartManagers = organizationHierarchy.getManagers(employee, date);
                                if (!orgChartManagers.contains(manager)) {
                                    missing.add(employee);
                                }
                             }
                         }
 
                         Collection<AuditResult> results = new ArrayList<AuditResult>();
                         for (Integer employee : missing) {
                             String name = directory.getByNumber().get(employee).getDisplayName();
                             AuditResult result = new AuditResult(AuditResultType.error, String.format("%s is not in org chart under a manager of this project", name));
                             results.add(result);
                         }
 
                         return results;
                     } finally {
                         rs.close();
                     }
                 } finally {
                     p.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
 }
