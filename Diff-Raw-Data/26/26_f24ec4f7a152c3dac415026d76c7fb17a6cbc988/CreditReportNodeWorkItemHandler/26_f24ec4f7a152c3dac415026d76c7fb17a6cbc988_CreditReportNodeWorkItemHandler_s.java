 package org.jbpm.homeloan;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.drools.runtime.process.WorkItem;
 import org.drools.runtime.process.WorkItemHandler;
 import org.drools.runtime.process.WorkItemManager;
 import org.jbpm.homeloan.creditreport.CreditQuery;
 import org.jbpm.homeloan.creditreport.CreditReport;
 
 /**
  * WorkItem handler for retrieval of a credit report.
  */
 public class CreditReportNodeWorkItemHandler implements WorkItemHandler {
     /** {@inheritDoc} */
     @Override
     public void executeWorkItem(final WorkItem item, final WorkItemManager itemMgr) {
         // Get input parameters.
         final String firstName = (String) item.getParameter("firstName");
         final String lastName = (String) item.getParameter("lastName");
         final String dob = (String) item.getParameter("dob");
         final String ssn = (String) item.getParameter("ssn");
 
         // Map to service input.
         final CreditQuery creditQuery = new CreditQuery();
         creditQuery.setFirstName(firstName);
         creditQuery.setLastName(lastName);
         creditQuery.setDob(dob);
         creditQuery.setSsn(ssn);
 
         // Call Web Service.
        // TODO: for now, just fill in the result.
        final CreditReport creditReport = new CreditReport();
        creditReport.setScore(ssn.substring(0, 3));
 
        // Signal that the work item is completed.
         final Map<String, Object> output = new HashMap<String, Object>();
         output.put("creditScore", creditReport.getScore());
         itemMgr.completeWorkItem(item.getId(), output);
     }
 
     /** {@inheritDoc} */
     @Override
     public void abortWorkItem(final WorkItem item, final WorkItemManager itemMgr) {
         System.err.println("Abort called on Credit Report Node");
     }
 }
