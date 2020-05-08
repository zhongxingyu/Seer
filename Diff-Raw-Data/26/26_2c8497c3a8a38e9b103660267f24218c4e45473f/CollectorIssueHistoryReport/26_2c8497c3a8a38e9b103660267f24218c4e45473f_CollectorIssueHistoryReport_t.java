 package ru.andreymarkelov.atlas.plugins.datacollector;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import ru.andreymarkelov.atlas.plugins.datacollector.struct.IssueDataKeeper;
 import ru.andreymarkelov.atlas.plugins.datacollector.struct.StatusUsers;
 import ru.andreymarkelov.atlas.plugins.datacollector.struct.Statuses;
 import ru.andreymarkelov.atlas.plugins.datacollector.struct.UserStatuses;
 import ru.andreymarkelov.atlas.plugins.datacollector.struct.Users;
 
 import com.atlassian.crowd.embedded.api.User;
 import com.atlassian.jira.ComponentManager;
 import com.atlassian.jira.component.ComponentAccessor;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
 import com.atlassian.jira.issue.fields.CustomField;
 import com.atlassian.jira.issue.search.SearchException;
 import com.atlassian.jira.issue.search.SearchResults;
 import com.atlassian.jira.jql.builder.JqlQueryBuilder;
 import com.atlassian.jira.plugin.report.impl.AbstractReport;
 import com.atlassian.jira.util.I18nHelper;
 import com.atlassian.jira.util.ParameterUtils;
 import com.atlassian.jira.web.action.ProjectActionSupport;
 import com.atlassian.jira.web.bean.I18nBean;
 import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
 
 public class CollectorIssueHistoryReport extends AbstractReport {
     @Override
     public String generateReportExcel(ProjectActionSupport action, Map reqParams) throws Exception {
         return descriptor.getHtml("excel", getVelocityParams(action, reqParams));
     }
 
     @Override
     public String generateReportHtml(ProjectActionSupport action, Map reqParams) throws Exception {
         return descriptor.getHtml("view", getVelocityParams(action, reqParams));
     }
 
     private List<Issue> getIssuesFromProject(Long pid, Date startDate) throws SearchException {
        Query q;
        if (startDate != null) {
            q = JqlQueryBuilder.newBuilder().where().project(pid).and().createdAfter(startDate).buildQuery();
        } else {
            q = JqlQueryBuilder.newBuilder().where().project(pid).buildQuery();
        }

         SearchResults results = ComponentManager.getInstance().getSearchService().search(
             ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(),
            q,
             PagerFilter.getUnlimitedFilter());
         return results.getIssues();
     }
 
     private Map<String, Object> getVelocityParams(ProjectActionSupport action, Map reqParams) throws SearchException {
         User remoteUser = action.getRemoteUser();
         I18nHelper i18nBean = new I18nBean(remoteUser);
         Date startDate = ParameterUtils.getDateParam(reqParams, "startDate", i18nBean.getLocale());
 
         Long projectId = ParameterUtils.getLongParam(reqParams, "projectId");
         String grouperId = ParameterUtils.getStringParam(reqParams, "groupField");
         String grouperValue = ParameterUtils.getStringParam(reqParams, "groupFieldValue");
         List<String> statusIds = ParameterUtils.getListParam(reqParams, "statusIds");
         if (statusIds == null || statusIds.isEmpty()) {
             statusIds = new ArrayList<String>();
             statusIds.add(ParameterUtils.getStringParam(reqParams, "statusIds"));
         }
         boolean isUserStatus = ParameterUtils.getBooleanParam(reqParams, "userstatus");
         boolean dataranges = ParameterUtils.getBooleanParam(reqParams, "dataranges");
 
         final Map<String, Object> velocityParams = new HashMap<String, Object>();
         velocityParams.put("grouperId", grouperId);
         velocityParams.put("isUserStatus", isUserStatus);
         velocityParams.put("dataranges", dataranges);
         velocityParams.put("report", this);
         velocityParams.put("action", action);
         velocityParams.put("helper", new RendererHelper());
 
         Map<String, List<String>> issueGroups = new HashMap<String, List<String>>();
         Map<String, IssueDataKeeper> data = new HashMap<String, IssueDataKeeper>();
 
         List<Issue> issues = getIssuesFromProject(projectId, startDate);
         if (!grouperId.equals("null")) {
             CustomField cf = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(grouperId);
             if (cf != null) {
                 velocityParams.put("cfName", cf.getName());
                 for (Issue issue : issues) {
                     Object cfVal = cf.getValue(issue);
                     if (cfVal != null) {
                         if (!StringUtils.isEmpty(grouperValue) && !grouperValue.trim().equals(cfVal.toString())) {
                             continue;
                         }
 
                         if (issueGroups.containsKey(cfVal.toString())) {
                             issueGroups.get(cfVal.toString()).add(issue.getKey());
                         } else {
                             List<String> keys = new ArrayList<String>();
                             keys.add(issue.getKey());
                             issueGroups.put(cfVal.toString(), keys);
                         }
                         issueGroups.get(cfVal.toString());
                     } else if (StringUtils.isEmpty(grouperValue)) {
                         if (issueGroups.containsKey("noGroupValue")) {
                             issueGroups.get("noGroupValue").add(issue.getKey());
                         } else {
                             List<String> keys = new ArrayList<String>();
                             keys.add(issue.getKey());
                             issueGroups.put("noGroupValue", keys);
                         }
                     }
                 }
             }
         }
 
         for (Issue issue : issues) {
             List<ChangeHistoryItem> items = ComponentAccessor.getChangeHistoryManager().getAllChangeItems(issue);
             if (isUserStatus) {
                 List<UserStatuses> userStatuses = CollectorUtils.reduceUserStatuses(
                     CollectorUtils.getUserStatuses(
                         new Users(CollectorUtils.getUserRanges(items, issue)),
                         new Statuses(CollectorUtils.getStatusRanges(items, issue))),
                     statusIds);
                 if (!userStatuses.isEmpty()) {
                     data.put(issue.getKey(), new IssueDataKeeper(issue.getKey(), issue.getSummary(), userStatuses));
                 }
             } else {
                 List<StatusUsers> statusUsers = CollectorUtils.reduceStatusUsers(
                     CollectorUtils.getStatusUsers(
                         new Users(CollectorUtils.getUserRanges(items, issue)),
                         new Statuses(CollectorUtils.getStatusRanges(items, issue))),
                     statusIds);
                 if (!statusUsers.isEmpty()) {
                     data.put(issue.getKey(), new IssueDataKeeper(issue.getKey(), issue.getSummary(), statusUsers));
                 }
             }
         }
 
        Map<String, Long> issueGroupSum = new HashMap<String, Long>();
        for (Map.Entry<String, List<String>> entry : issueGroups.entrySet()) {
            String key = entry.getKey();
            List<String> iKeys = entry.getValue();
            long total = 0;
            for (String iKey : iKeys) {
                IssueDataKeeper idk = data.get(iKey);
                if (idk != null) total += idk.getTotalTime();
            }
            issueGroupSum.put(key, total);
        }

        velocityParams.put("issueGroupSum", issueGroupSum);
         velocityParams.put("totalData", getTotalTime(data.values()));
         velocityParams.put("issueGroups", issueGroups);
         velocityParams.put("data", data);
         return velocityParams;
     }
 
     public long getTotalTime(Collection<IssueDataKeeper> items) {
         long totalTime = 0;
         for (IssueDataKeeper idk : items) {
             totalTime += idk.getTotalTime();
         }
         return totalTime;
     }
 
     @Override
     public boolean isExcelViewSupported() {
         return true;
     }
 
     @Override
     public boolean showReport() {
         return true;
     }
 
     @Override
     public void validate(ProjectActionSupport action, Map params) {
         List<String> statusIds = ParameterUtils.getListParam(params, "statusIds");
         if (statusIds == null || statusIds.isEmpty()) {
             String statusId = ParameterUtils.getStringParam(params, "statusIds");
             if (statusId == null) {
                 action.addError("statusIds", ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("datacollector.report.statuses.error"));
             }
         }
         super.validate(action, params);
     }
 }
