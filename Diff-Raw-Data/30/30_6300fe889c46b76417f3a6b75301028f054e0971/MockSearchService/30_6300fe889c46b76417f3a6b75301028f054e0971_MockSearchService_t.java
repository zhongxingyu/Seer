 package com.bics.jira.mail.mock;
 
 import com.atlassian.crowd.embedded.api.User;
 import com.atlassian.jira.bc.issue.search.SearchService;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.search.SearchContext;
 import com.atlassian.jira.issue.search.SearchException;
 import com.atlassian.jira.issue.search.SearchResults;
 import com.atlassian.jira.issue.search.searchers.util.RecursiveClauseVisitor;
 import com.atlassian.jira.jql.context.QueryContext;
 import com.atlassian.jira.util.MessageSet;
 import com.atlassian.jira.web.bean.PagerFilter;
 import com.atlassian.query.Query;
 import com.atlassian.query.clause.TerminalClause;
 import com.atlassian.query.operand.EmptyOperand;
 import com.atlassian.query.operand.FunctionOperand;
 import com.atlassian.query.operand.MultiValueOperand;
 import com.atlassian.query.operand.OperandVisitor;
 import com.atlassian.query.operand.SingleValueOperand;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Java Doc here
  *
  * @author Victor Polischuk
  * @since 21/07/13 13:36
  */
 public class MockSearchService implements SearchService {
     private final Set<Issue> issues = new HashSet<Issue>();
 
     public void set(Issue issue) {
         issues.add(issue);
     }
 
     @Override
     public SearchResults search(User user, Query query, PagerFilter pagerFilter) throws SearchException {
         SummaryClauseVisitor summaryClauseVisitor = new SummaryClauseVisitor();
 
         for (Issue issue : issues) {
             query.getWhereClause().accept(summaryClauseVisitor);
 
             if (issue.getSummary().equals(summaryClauseVisitor.summary)) {
                 return new SearchResults(Collections.singletonList(issue), pagerFilter);
             }
         }
 
         return new SearchResults(Collections.<Issue>emptyList(), pagerFilter);
     }
 
     @Override
     public long searchCount(User user, Query query) throws SearchException {
         return 0;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getQueryString(User user, Query query) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ParseResult parseQuery(User user, String s) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public MessageSet validateQuery(User user, Query query) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean doesQueryFitFilterForm(User user, Query query) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public QueryContext getQueryContext(User user, Query query) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public QueryContext getSimpleQueryContext(User user, Query query) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public SearchContext getSearchContext(User user, Query query) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getJqlString(Query query) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getGeneratedJqlString(Query query) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Query sanitiseSearchQuery(User user, Query query) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     private static class SummaryClauseVisitor extends RecursiveClauseVisitor {
         private String summary;
 
         @Override
         public Void visit(TerminalClause clause) {
             if ("summary".equals(clause.getName())) {
                 summary = clause.getOperand().accept(new SummaryOperandVisitor());
             }
 
             return super.visit(clause);
         }
     }
 
     private static class SummaryOperandVisitor implements OperandVisitor<String> {
         @Override
         public String visit(EmptyOperand emptyOperand) {
             return null;
         }
 
         @Override
         public String visit(FunctionOperand functionOperand) {
             return null;
         }
 
         @Override
         public String visit(MultiValueOperand multiValueOperand) {
             return null;
         }
 
         @Override
         public String visit(SingleValueOperand singleValueOperand) {
            String value = singleValueOperand.getStringValue();

            return value == null || value.length() < 2 ? "" : value.substring(1, value.length() - 1);
         }
     }
 }
