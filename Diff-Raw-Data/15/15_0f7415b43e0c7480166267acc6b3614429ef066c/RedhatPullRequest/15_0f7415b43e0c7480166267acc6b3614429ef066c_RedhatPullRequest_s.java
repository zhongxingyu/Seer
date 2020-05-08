 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2014, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.jboss.pull.shared.connectors;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.egit.github.core.Comment;
 import org.eclipse.egit.github.core.Issue;
 import org.eclipse.egit.github.core.Milestone;
 import org.eclipse.egit.github.core.PullRequest;
 import org.eclipse.egit.github.core.User;
 import org.jboss.pull.shared.BuildResult;
 import org.jboss.pull.shared.PullHelper;
 import org.jboss.pull.shared.connectors.bugzilla.BZHelper;
 import org.jboss.pull.shared.connectors.bugzilla.Bug;
 import org.jboss.pull.shared.connectors.github.GithubHelper;
 import org.jboss.pull.shared.connectors.jira.JiraIssue;
 import org.jboss.pull.shared.connectors.jira.JiraHelper;
 
 public class RedhatPullRequest {
     private static final Pattern UPSTREAM_NOT_REQUIRED = Pattern.compile(".*no.*upstream.*required.*",
             Pattern.CASE_INSENSITIVE);
 
     private static final Pattern BUGZILLA_ID_PATTERN = Pattern.compile("bugzilla\\.redhat\\.com/show_bug\\.cgi\\?id=(\\d+)",
             Pattern.CASE_INSENSITIVE);
 
     private static final Pattern RELATED_JIRA_PATTERN = Pattern.compile(".*issues\\.jboss\\.org/browse/([a-zA-Z_0-9-]*)",
             Pattern.CASE_INSENSITIVE);
 
     // This has to match two patterns
     // * https://github.com/uselessorg/jboss-eap/pull/4
     // * https://api.github.com/repos/uselessorg/jboss-eap/pulls/4
     private static final Pattern RELATED_PR_PATTERN = Pattern.compile(
             ".*github\\.com.*?/([a-zA-Z_0-9-]*)/([a-zA-Z_0-9-]*)/pull.?/(\\d+)", Pattern.CASE_INSENSITIVE);
 
     private PullRequest pullRequest;
     private List<Bug> bugs = new ArrayList<Bug>();
     private List<RedhatPullRequest> relatedPullRequests = null;
 
     // private PullHelper helper;
     private BZHelper bzHelper;
     private GithubHelper ghHelper;
     private JiraHelper jiraHelper;
 
     public RedhatPullRequest(PullRequest pullRequest, BZHelper bzHelper, GithubHelper ghHelper) {
         this.pullRequest = pullRequest;
         this.bzHelper = bzHelper;
         this.ghHelper = ghHelper;
 
         bugs = getBugsFromDescription(pullRequest);
         // Can't call getPRFromDescription here. If two PR's reference each other a loop occurs.
     }
 
     private List<JiraIssue> getJIRAFromDescription(PullRequest pull) {
         final List<String> ids = extractJiraIds(pull.getBody());
         final ArrayList<JiraIssue> issues = new ArrayList<JiraIssue>();
         for (String id : ids) {
             final JiraIssue bug = jiraHelper.getJIRA(id);
             if (bug != null) {
                 issues.add(bug);
             }
         }
         return issues;
     }
 
     private List<String> extractJiraIds(String body){
         final ArrayList<String> ids = new ArrayList<String>();
         final Matcher matcher = RELATED_JIRA_PATTERN.matcher(body);
         while (matcher.find()) {
             try {
                 ids.add(matcher.group(1));
             } catch (NumberFormatException ignore) {
                 System.err.printf("Invalid bug number: %s.\n", ignore);
             }
         }
         return ids;
     }
 
     private List<Bug> getBugsFromDescription(PullRequest pull) {
         final List<Integer> ids = extractBugzillaIds(pull.getBody());
         final ArrayList<Bug> bugs = new ArrayList<Bug>();
 
         for (Integer id : ids) {
             final Bug bug = bzHelper.getBug(id);
             if (bug != null) {
                 bugs.add(bug);
             }
         }
         return bugs;
     }
 
     private List<Integer> extractBugzillaIds(String body) {
         final ArrayList<Integer> ids = new ArrayList<Integer>();
         final Matcher matcher = BUGZILLA_ID_PATTERN.matcher(body);
         while (matcher.find()) {
             try {
                 ids.add(Integer.valueOf(matcher.group(1)));
             } catch (NumberFormatException ignore) {
                 System.err.printf("Invalid bug number: %s.\n", ignore);
             }
         }
         return ids;
     }
 
     public int getNumber() {
         return pullRequest.getNumber();
     }
 
     public void postGithubComment(String comment) {
         ghHelper.postGithubComment(pullRequest, comment);
     }
 
     public Milestone getMilestone() {
         return pullRequest.getMilestone();
     }
 
     public void setMilestone(Milestone milestone) {
         Issue issue = ghHelper.getIssue(pullRequest);
 
         issue.setMilestone(milestone);
         ghHelper.editIssue(issue);
     }
 
     public String getTargetBranchTitle() {
         return pullRequest.getBase().getRef();
     }
 
     public String getSourceBranchSha() {
         return pullRequest.getHead().getSha();
     }
 
     public User getGithubUser() {
         return pullRequest.getUser();
     }
 
     public List<Comment> getGithubComments() {
         return ghHelper.getComments(pullRequest);
     }
 
     public void postGithubStatus(String targetUrl, String status) {
         ghHelper.postGithubStatus(pullRequest, targetUrl, status);
     }
 
     public String getGithubDescription() {
         return pullRequest.getBody();
     }
 
     public Date getGithubUpdatedAt() {
         return pullRequest.getUpdatedAt();
     }
 
     /**
      * Searches for last github comment that contains the pattern.
      * @param pattern - REGEX pattern to match against comment body.
      * @return Last comment that matches the pattern or null if no comments match.
      */
     public Comment getLastMatchingGithubComment(Pattern pattern) {
         return ghHelper.getLastMatchingComment(pullRequest, pattern);
     }
 
     public List<RedhatPullRequest> getRelatedPullRequests() {
         if (relatedPullRequests != null) {
             return relatedPullRequests;
         } else {
             return relatedPullRequests = getPRFromDescription();
         }
     }
 
     private List<RedhatPullRequest> getPRFromDescription() {
         Matcher matcher = RELATED_PR_PATTERN.matcher(getGithubDescription());
 
         List<RedhatPullRequest> relatedPullRequests = new ArrayList<RedhatPullRequest>();
         while (matcher.find()) {
             PullRequest relatedPullRequest = ghHelper.getPullRequest(matcher.group(1), matcher.group(2),
                     Integer.valueOf(matcher.group(3)));
             if (relatedPullRequest != null) {
                 relatedPullRequests.add(new RedhatPullRequest(relatedPullRequest, bzHelper, ghHelper));
             }
         }
 
         return relatedPullRequests;
     }
 
     public String getState() {
         return pullRequest.getState();
     }
 
     public String getHtmlUrl() {
         return pullRequest.getHtmlUrl();
     }
 
     public boolean isMerged() {
         return ghHelper.isMerged(pullRequest);
     }
 
     public List<Bug> getBugs() {
         return bugs;
     }
 
     public boolean isJiraInDescription(){
        return extractJiraIds(pullRequest.getBody()).isEmpty();
     }
 
     public boolean isUpstreamRequired(){
         return !UPSTREAM_NOT_REQUIRED.matcher(pullRequest.getBody()).find();
     }
     public BuildResult getBuildResult() {
         BuildResult buildResult = BuildResult.UNKNOWN;
         Comment comment = ghHelper.getLastMatchingComment(pullRequest, PullHelper.BUILD_OUTCOME);
 
         if (comment != null) {
             Matcher matcher = PullHelper.BUILD_OUTCOME.matcher(comment.getBody());
             while (matcher.find()) {
                 buildResult = BuildResult.valueOf(matcher.group(2));
             }
         }
 
         return buildResult;
     }
 
     public String getOrganization() {
         Matcher matcher = RELATED_PR_PATTERN.matcher(pullRequest.getUrl());
         if (matcher.matches()) {
             return matcher.group(1);
         }
         return null;
     }
 
     public String getRepository() {
         Matcher matcher = RELATED_PR_PATTERN.matcher(pullRequest.getUrl());
         if (matcher.matches()) {
             return matcher.group(2);
         }
         return null;
     }
 
     public boolean updateBugzillaStatus(Bug bug, Bug.Status status) {
         return bzHelper.updateBugzillaStatus(bug.getId(), status);
     }
 
 }
