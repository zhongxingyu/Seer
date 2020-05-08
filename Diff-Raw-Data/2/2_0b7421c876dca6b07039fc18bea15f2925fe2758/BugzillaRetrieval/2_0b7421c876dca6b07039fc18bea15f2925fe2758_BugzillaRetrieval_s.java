 /*
  * Copyright (C) 2012-2013 GSyC/LibreSoft, Universidad Rey Juan Carlos
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  *
  * Authors: Santiago Dueñas <sduenas@libresoft.es>
  *
  */
 
 package eu.alertproject.kesi.database;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import eu.alertproject.kesi.model.Activity;
 import eu.alertproject.kesi.model.ComputerSystem;
 import eu.alertproject.kesi.model.Issue;
 import eu.alertproject.kesi.model.IssueTracker;
 import eu.alertproject.kesi.model.Milestone;
 import eu.alertproject.kesi.model.Person;
 import eu.alertproject.kesi.model.Product;
 
 public class BugzillaRetrieval extends ITSRetrieval {
     /* Bugzilla fields */
     private static final String ITS_BUGZILLA_PRODUCT = "product";
     private static final String ITS_BUGZILLA_COMPONENT = "component";
     private static final String ITS_BUGZILLA_VERSION = "version";
     private static final String ITS_BUGZILLA_PLATFORM = "rep_platform";
     private static final String ITS_BUGZILLA_OS = "op_sys";
     private static final String ITS_BUGZILLA_TARGET_MILESTONE = "target_milestone";
 
     /* Bugzilla values for changes */
     private static final String BUGZILLA_CHANGE_ASSIGNEE = "Assignee";
     private static final String BUGZILLA_CHANGE_CC = "CC";
     private static final String BUGZILLA_CHANGE_COMPONENT = "Component";
     private static final String BUGZILLA_CHANGE_PRIORITY = "Priority";
     private static final String BUGZILLA_CHANGE_PRODUCT = "Product";
     private static final String BUGZILLA_CHANGE_SEVERITY = "Severity";
     private static final String BUGZILLA_CHANGE_STATUS = "status";
     private static final String BUGZILLA_CHANGE_SUMMARY = "Summary";
     private static final String BUGZILLA_CHANGE_RESOLUTION = "resolution";
 
     /* Bugzilla values for status field */
     private static final String BUGZILLA_STATUS_ASSIGNED = "ASSIGNED";
     private static final String BUGZILLA_STATUS_CLOSED = "CLOSED";
     private static final String BUGZILLA_STATUS_NEEDS_INFO = "NEEDSINFO";
     private static final String BUGZILLA_STATUS_NEW = "NEW";
     private static final String BUGZILLA_STATUS_REOPENED = "REOPENED";
     private static final String BUGZILLA_STATUS_RESOLVED = "RESOLVED";
     private static final String BUGZILLA_STATUS_UNCONFIRMED = "UNCONFIRMED";
     private static final String BUGZILLA_STATUS_VERIEFED = "VERIEFED";
     private static final String BUGZILLA_STATUS_CONFIRMED = "CONFIRMED";
     private static final String BUGZILLA_STATUS_IN_PROGRESS = "IN_PROGRESS";
 
     /* Bugzilla values for resolution field */
     private static final String BUGZILLA_RESOLUTION_FIXED = "FIXED";
     private static final String BUGZILLA_RESOLUTION_INVALID = "INVALID";
     private static final String BUGZILLA_RESOLUTION_WORKS_FOR_ME = "WORKSFORME";
     private static final String BUGZILLA_RESOLUTION_DUPLICATE = "DUPLICATE";
     private static final String BUGZILLA_RESOLUTION_WONT_FIX = "WONTFIX";
     private static final String BUGZILLA_RESOLUTION_RESOLVED = "RESOLVED";
     private static final String BUGZILLA_RESOLUTION_REMIND = "REMIND";
     private static final String BUGZILLA_RESOLUTION_NONE = "---";
 
     /* Bugzilla values for severity field */
     private static final String BUGZILLA_SEVERITY_ENHANCEMENT = "enhancement";
     private static final String BUGZILLA_SEVERITY_TRIVIAL = "trivial";
     private static final String BUGZILLA_SEVERITY_MINOR = "minor";
     private static final String BUGZILLA_SEVERITY_NORMAL = "normal";
     private static final String BUGZILLA_SEVERITY_MAJOR = "major";
     private static final String BUGZILLA_SEVERITY_CRITICAL = "critical";
     private static final String BUGZILLA_SEVERITY_BLOCKER = "blocker";
 
     /* Bugzilla values for priority field */
     private static final String BUGZILLA_PRIORITY_LOWEST = "lowest";
     private static final String BUGZILLA_PRIORITY_LOW = "low";
     private static final String BUGZILLA_PRIORITY_MEDIUM = "medium";
     private static final String BUGZILLA_PRIORITY_HIGH = "high";
     private static final String BUGZILLA_PRIORITY_HIGHEST = "highest";
 
     /* Specific queries for Bugzilla */
     private static final String ITS_QUERY_ISSUE_BUGZILLA = "SELECT product, component, "
             + "version, rep_platform, op_sys, delta_ts, target_milestone "
             + "FROM issues_log_bugzilla "
             + "WHERE issue_id = ? ORDER BY id LIMIT 1 ";
 
     public BugzillaRetrieval(String driver, String username, String password,
             String host, String port, String database)
             throws DriverNotSupportedError, DatabaseConnectionError {
         super(driver, username, password, host, port, database);
     }
 
     @Override
     public Issue getEventIssueNew(int issueID) throws DatabaseExtractionError {
         try {
             Issue issue = getBasicIssueNew(issueID);
             Product product = getBugzillaProduct(issueID);
             ComputerSystem computerSystem = getBugzillaCP(issueID);
             Milestone milestone = getBugzillaMilestone(issueID);
 
             issue.setProduct(product);
             issue.setComputerSystem(computerSystem);
             issue.setMilestone(milestone);
 
             return issue;
         } catch (SQLException e) {
             String msg = "Error getting issue from event " + issueID + "."
                     + e.getMessage();
             logger.error(msg, e);
             throw new DatabaseExtractionError(msg);
         }
     }
 
     @Override
     public Issue getEventIssueUpdateComment(int commentID, int issueID)
             throws DatabaseExtractionError {
         try {
             return getBasicIssueUpdateComment(commentID, issueID);
         } catch (SQLException e) {
             String msg = "Error getting comment " + commentID + "."
                     + e.getMessage();
             logger.error(msg, e);
             throw new DatabaseExtractionError(msg);
         }
     }
 
     @Override
     public Issue getEventIssueUpdateChange(int changeID, int issueID)
             throws DatabaseExtractionError {
         try {
             Issue issue = getBasicIssueUpdateChange(changeID, issueID);
             Activity change = issue.getActivity().get(0);
             String what = change.getWhat();
             String newValue = change.getNewValue();
 
             if (what.equals(BUGZILLA_CHANGE_ASSIGNEE)) {
                 Person assignedTo = new Person("", newValue, "");
                 issue.setAssignedTo(assignedTo);
             } else if (what.equals(BUGZILLA_CHANGE_CC)) {
                 Person CCPerson = new Person("", newValue, "");
                 issue.addCCPerson(CCPerson);
             } else if (what.equals(BUGZILLA_CHANGE_COMPONENT)) {
                 Product product = getBugzillaProduct(issueID);
                 product.setComponentId(newValue);
                 issue.setProduct(product);
             } else if (what.equals(BUGZILLA_CHANGE_PRIORITY)) {
                 issue.setPriority(toPriority(newValue));
             } else if (what.equals(BUGZILLA_CHANGE_PRODUCT)) {
                 Product product = getBugzillaProduct(issueID);
                 product.setId(newValue);
                 issue.setProduct(product);
             } else if (what.equals(BUGZILLA_CHANGE_RESOLUTION)) {
                 issue.setResolution(toResolution(newValue));
             } else if (what.equals(BUGZILLA_CHANGE_SEVERITY)) {
                 issue.setSeverity(toSeverity(newValue));
             } else if (what.equals(BUGZILLA_CHANGE_STATUS)) {
                 issue.setState(toStatus(newValue));
             } else if (what.equals(BUGZILLA_CHANGE_SUMMARY)) {
                 issue.setDescription(newValue);
             }
 
             return issue;
         } catch (SQLException e) {
             String msg = "Error getting change " + changeID + "."
                     + e.getMessage();
             logger.error(msg, e);
             throw new DatabaseExtractionError(msg);
         }
     }
 
     @Override
     protected URI getIssueURL(int issueID, String publicID) throws SQLException {
         IssueTracker tracker = getTrackerFromIssue(issueID);
 
         try {
             String baseURL = tracker.getURI().toASCIIString()
                     .replaceFirst("buglist\\.cgi.*$", "");
             URI issueURL = new URI(baseURL + "show_bug.cgi?id=" + publicID);
             return issueURL;
         } catch (URISyntaxException e) {
             logger.error("Error converting URI for issue " + issueID
                     + ". Set to null.", e);
             return null;
         }
     }
 
     @Override
     protected String toStatus(String value) {
         if (value.equals(BUGZILLA_STATUS_ASSIGNED)) {
             return Issue.ASSIGNED;
         } else if (value.equals(BUGZILLA_STATUS_CLOSED)) {
             return Issue.CLOSED;
         } else if (value.equals(BUGZILLA_STATUS_NEEDS_INFO)) {
             return Issue.OPEN;
         } else if (value.equals(BUGZILLA_STATUS_NEW)) {
             return Issue.OPEN;
         } else if (value.equals(BUGZILLA_STATUS_REOPENED)) {
             return Issue.OPEN;
         } else if (value.equals(BUGZILLA_STATUS_RESOLVED)) {
             return Issue.RESOLVED;
         } else if (value.equals(BUGZILLA_STATUS_UNCONFIRMED)) {
             return Issue.OPEN;
         } else if (value.equals(BUGZILLA_STATUS_VERIEFED)) {
             return Issue.VERIFIED;
         } else if (value.equals(BUGZILLA_STATUS_CONFIRMED)) {
             return Issue.VERIFIED;
         } else if (value.equals(BUGZILLA_STATUS_IN_PROGRESS)) {
             return Issue.ASSIGNED;
         } else {
             return Issue.UNKNOWN;
         }
     }
 
     @Override
     protected String toResolution(String value) {
         if (value.equals(BUGZILLA_RESOLUTION_FIXED)) {
             return Issue.FIXED;
         } else if (value.equals(BUGZILLA_RESOLUTION_INVALID)) {
             return Issue.INVALID;
         } else if (value.equals(BUGZILLA_RESOLUTION_WORKS_FOR_ME)) {
             return Issue.WORKS_FOR_ME;
         } else if (value.equals(BUGZILLA_RESOLUTION_DUPLICATE)) {
             return Issue.DUPLICATED;
         } else if (value.equals(BUGZILLA_RESOLUTION_WONT_FIX)) {
             return Issue.WONT_FIX;
         } else if (value.equals(BUGZILLA_RESOLUTION_RESOLVED)) {
             return Issue.RESOLVED;
         } else if (value.equals(BUGZILLA_RESOLUTION_REMIND)) {
             return Issue.REMIND;
         } else if (value.equals("") || value.equals(BUGZILLA_RESOLUTION_NONE)) {
             return Issue.NONE;
         } else {
             return Issue.UNKNOWN;
         }
     }
 
     @Override
     protected String toSeverity(String value) {
         if (value.equals(BUGZILLA_SEVERITY_ENHANCEMENT)) {
             return Issue.FEATURE;
         } else if (value.equals(BUGZILLA_SEVERITY_TRIVIAL)) {
             return Issue.TRIVIAL;
         } else if (value.equals(BUGZILLA_SEVERITY_MINOR)) {
             return Issue.MINOR;
         } else if (value.equals(BUGZILLA_SEVERITY_NORMAL)) {
             return Issue.NORMAL;
         } else if (value.equals(BUGZILLA_SEVERITY_MAJOR)) {
             return Issue.MAJOR;
         } else if (value.equals(BUGZILLA_SEVERITY_CRITICAL)) {
             return Issue.CRITICAL;
         } else if (value.equals(BUGZILLA_SEVERITY_BLOCKER)) {
             return Issue.BLOCKER;
         } else {
             return Issue.UNKNOWN;
         }
     }
 
     @Override
     protected String toPriority(String value) {
         if (value.equals(BUGZILLA_PRIORITY_LOWEST)) {
             return Issue.LOWEST;
         } else if (value.equals(BUGZILLA_PRIORITY_LOW)) {
             return Issue.LOW;
         } else if (value.equals(BUGZILLA_PRIORITY_MEDIUM)) {
             return Issue.MEDIUM;
         } else if (value.equals(BUGZILLA_PRIORITY_HIGH)) {
             return Issue.HIGH;
         } else if (value.equals(BUGZILLA_PRIORITY_HIGHEST)) {
             return Issue.HIGHEST;
         } else {
            return Issue.UNKNOWN;
         }
     }
 
     private Product getBugzillaProduct(int issueID) throws SQLException {
         String productName;
         String componentName;
         String version;
         Product product;
         PreparedStatement stmt;
         ResultSet rs;
 
         stmt = prepareStatement(ITS_QUERY_ISSUE_BUGZILLA);
         stmt.setInt(1, issueID);
         rs = executeQuery(stmt);
         rs.first();
 
         productName = rs.getString(ITS_BUGZILLA_PRODUCT);
         componentName = rs.getString(ITS_BUGZILLA_COMPONENT);
         version = rs.getString(ITS_BUGZILLA_VERSION);
 
         stmt.close();
 
         product = new Product(productName, componentName, version);
 
         return product;
     }
 
     private ComputerSystem getBugzillaCP(int issueID) throws SQLException {
         String os;
         String platform;
         ComputerSystem cp;
         PreparedStatement stmt;
         ResultSet rs;
 
         stmt = prepareStatement(ITS_QUERY_ISSUE_BUGZILLA);
         stmt.setInt(1, issueID);
         rs = executeQuery(stmt);
         rs.first();
 
         platform = rs.getString(ITS_BUGZILLA_PLATFORM);
         os = rs.getString(ITS_BUGZILLA_OS);
 
         stmt.close();
 
         cp = new ComputerSystem(platform, os);
 
         return cp;
     }
 
     private Milestone getBugzillaMilestone(int issueID) throws SQLException {
         String release;
         Milestone milestone;
         PreparedStatement stmt;
         ResultSet rs;
 
         stmt = prepareStatement(ITS_QUERY_ISSUE_BUGZILLA);
         stmt.setInt(1, issueID);
         rs = executeQuery(stmt);
         rs.first();
 
         release = rs.getString(ITS_BUGZILLA_TARGET_MILESTONE);
 
         stmt.close();
 
         milestone = new Milestone(release);
 
         return milestone;
     }
 
 }
