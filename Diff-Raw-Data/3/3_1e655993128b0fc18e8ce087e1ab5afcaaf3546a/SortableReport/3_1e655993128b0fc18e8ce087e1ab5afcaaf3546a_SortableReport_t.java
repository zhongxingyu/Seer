 package gov.nih.nci.eagle.web.reports;
 
 import gov.nih.nci.caintegrator.service.task.Task;
 import gov.nih.nci.caintegrator.service.task.TaskResult;
 import gov.nih.nci.caintegrator.studyQueryService.FindingsManager;
 import gov.nih.nci.eagle.util.FieldBasedComparator;
 
 import java.util.Collections;
 import java.util.List;
 
 import javax.faces.event.ActionEvent;
 
 public abstract class SortableReport {
     
     protected FindingsManager findingsManager;
     protected Task task;
     protected TaskResult taskResult;
     protected String sortedBy;
     protected Boolean sortAscending;
     protected FieldBasedComparator sortComparator;
     protected List reportBeans;
     
     protected abstract void init();
     
     public void sortDataList(ActionEvent event) {
         String sortFieldAttribute = getAttribute(event, "sortField");
 
         sortedBy = sortFieldAttribute;
         // Get and set sort field and sort order.
         if (sortFieldAttribute != null
                 && sortFieldAttribute.equals(sortComparator.getField())) {
             sortAscending = !sortAscending;
         } else {
             sortAscending = true;
         }
         if (sortFieldAttribute != null) {
             sortComparator = new FieldBasedComparator(sortFieldAttribute,
                     sortAscending);
         }
     }    
     
     protected static String getAttribute(ActionEvent event, String name) {
         return (String) event.getComponent().getAttributes().get(name);
     }
     
     public FindingsManager getFindingsManager() {
         return findingsManager;
     }
     public void setFindingsManager(FindingsManager findingsManager) {
         this.findingsManager = findingsManager;
     }
     public List getReportBeans() {
        if(reportBeans != null)
            Collections.sort(reportBeans, sortComparator);
         return reportBeans;
     }
     public void setReportBeans(List reportBeans) {
         this.reportBeans = reportBeans;
     }
     public Boolean getSortAscending() {
         return sortAscending;
     }
     public void setSortAscending(Boolean sortAscending) {
         this.sortAscending = sortAscending;
     }
     public FieldBasedComparator getSortComparator() {
         return sortComparator;
     }
     public void setSortComparator(FieldBasedComparator sortComparator) {
         this.sortComparator = sortComparator;
     }
     public String getSortedBy() {
         return sortedBy;
     }
     public void setSortedBy(String sortedBy) {
         this.sortedBy = sortedBy;
     }
     public Task getTask() {
         return task;
     }
     public void setTask(Task task) {
         this.task = task;
     }
     public TaskResult getTaskResult() {
         return taskResult;
     }
     public void setTaskResult(TaskResult taskResult) {
         this.taskResult = taskResult;
     }
 
 }
