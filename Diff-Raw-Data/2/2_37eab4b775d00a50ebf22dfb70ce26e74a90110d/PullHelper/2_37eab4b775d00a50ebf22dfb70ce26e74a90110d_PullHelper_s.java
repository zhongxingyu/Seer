 /*
  * JBoss, Home of Professional Open Source.
  * Copyright (c) 2013, Red Hat, Inc., and individual contributors
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
 package org.jboss.pull.shared;
 
 import org.eclipse.egit.github.core.Comment;
 import org.eclipse.egit.github.core.CommitStatus;
 import org.eclipse.egit.github.core.IRepositoryIdProvider;
 import org.eclipse.egit.github.core.Milestone;
 import org.eclipse.egit.github.core.PullRequest;
 import org.eclipse.egit.github.core.RepositoryId;
 import org.eclipse.egit.github.core.client.GitHubClient;
 import org.eclipse.egit.github.core.service.CommitService;
 import org.eclipse.egit.github.core.service.IssueService;
 import org.eclipse.egit.github.core.service.MilestoneService;
 import org.eclipse.egit.github.core.service.PullRequestService;
 import org.jboss.pull.shared.evaluators.PullEvaluatorFacade;
 import org.jboss.pull.shared.spi.PullEvaluator;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * A shared functionality regarding mergeable PRs, Github and Bugzilla.
  *
  * @author <a href="mailto:istudens@redhat.com">Ivo Studensky</a>
  * @author wangchao
  */
 public class PullHelper {
     private static final Pattern BUILD_OUTCOME = Pattern.compile(
             "outcome was (\\*\\*)?+(SUCCESS|FAILURE|ABORTED)(\\*\\*)?+ using a merge of ([a-z0-9]+)", Pattern.CASE_INSENSITIVE);
 
     public static final Pattern PENDING = Pattern.compile(".*Build.*merging.*has\\W+been\\W+triggered.*",
             Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
     public static final Pattern RUNNING = Pattern.compile(".*Build.*merging.*has\\W+been\\W+started.*",
             Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
     public static final Pattern FINISHED = Pattern.compile(".*Build.*merging.*has\\W+been\\W+finished.*",
             Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
     public static final Pattern MERGE = Pattern.compile(".*(re)?merge\\W+this\\W+please.*", Pattern.CASE_INSENSITIVE
             | Pattern.DOTALL);
     public static final Pattern FORCE_MERGE = Pattern.compile(".*force\\W+merge\\W+this.*", Pattern.CASE_INSENSITIVE
             | Pattern.DOTALL);
 
     private static final String BUGZILLA_BASE = "https://bugzilla.redhat.com/";
 
     private final String GITHUB_ORGANIZATION;
     private final String GITHUB_REPO;
     private final String GITHUB_LOGIN;
     private final String GITHUB_TOKEN;
 
     private final String BUGZILLA_LOGIN;
     private final String BUGZILLA_PASSWORD;
 
     private final IRepositoryIdProvider repository;
     private final CommitService commitService;
     private final IssueService issueService;
     private final PullRequestService pullRequestService;
     private final MilestoneService milestoneService;
 
     private final Bugzilla bugzillaClient;
 
     private final Properties props;
 
     private final PullEvaluatorFacade evaluatorFacade;
 
     private final UserList adminList;
 
     public PullHelper(final String configurationFileProperty, final String configurationFileDefault) throws Exception {
         try {
             props = Util.loadProperties(configurationFileProperty, configurationFileDefault);
 
             GITHUB_ORGANIZATION = Util.require(props, "github.organization");
             GITHUB_REPO = Util.require(props, "github.repo");
 
             GITHUB_LOGIN = Util.require(props, "github.login");
             GITHUB_TOKEN = Util.get(props, "github.token");
 
             // initialize client and services
             GitHubClient client = new GitHubClient();
             if (GITHUB_TOKEN != null && GITHUB_TOKEN.length() > 0)
                 client.setOAuth2Token(GITHUB_TOKEN);
             repository = RepositoryId.create(GITHUB_ORGANIZATION, GITHUB_REPO);
             commitService = new CommitService(client);
             issueService = new IssueService(client);
             pullRequestService = new PullRequestService(client);
             milestoneService = new MilestoneService(client);
 
             BUGZILLA_LOGIN = Util.require(props, "bugzilla.login");
             BUGZILLA_PASSWORD = Util.require(props, "bugzilla.password");
 
             // initialize bugzilla client
             bugzillaClient = new Bugzilla(BUGZILLA_BASE, BUGZILLA_LOGIN, BUGZILLA_PASSWORD);
 
             // initialize evaluators
             evaluatorFacade = new PullEvaluatorFacade(this, props);
 
             adminList = UserList.loadUserList(Util.require(props, "admin.list.file"));
 
         } catch (Exception e) {
             System.err.printf("Cannot initialize: %s\n", e);
             e.printStackTrace(System.err);
             throw e;
         }
     }
 
     /**
      * Checks the state of the given pull request from the pull-processor perspective.
     * 
      * @param pull the pull request
      * @return relevant state
      */
     public ProcessorPullState checkPullRequestState(final PullRequest pull) {
         ProcessorPullState result = ProcessorPullState.NEW;
 
         try {
             final List<Comment> comments = issueService.getComments(repository, pull.getNumber());
             for (Comment comment : comments) {
                 if (GITHUB_LOGIN.equals(comment.getUser().getLogin())) {
                     if (PENDING.matcher(comment.getBody()).matches()) {
                         result = ProcessorPullState.PENDING;
                         continue;
                     }
 
                     if (RUNNING.matcher(comment.getBody()).matches()) {
                         result = ProcessorPullState.RUNNING;
                         continue;
                     }
 
                     if (FINISHED.matcher(comment.getBody()).matches()) {
                         result = ProcessorPullState.FINISHED;
                         continue;
                     }
                 }
 
                 if (MERGE.matcher(comment.getBody()).matches()) {
                     result = ProcessorPullState.MERGEABLE;
                     continue;
                 }
             }
 
             if (result == ProcessorPullState.MERGEABLE || result == ProcessorPullState.NEW) {
                 // check other conditions, i.e. upstream pull request and bugzilla and jira...
                 final PullEvaluator.Result mergeable = evaluatorFacade.isMergeable(pull);
                 if (!mergeable.isMergeable()) {
                     result = ProcessorPullState.INCOMPLETE;
                 }
 
                 if (result == ProcessorPullState.INCOMPLETE && !comments.isEmpty()) {
                     Comment lastComment = comments.get(comments.size() - 1);
                     if (FORCE_MERGE.matcher(lastComment.getBody()).matches() && isAdminUser(lastComment.getUser().getLogin()))
                         result = ProcessorPullState.MERGEABLE;
                 }
             }
 
         } catch (IOException e) {
             System.err.printf("Cannot read comments of PR#%d due to %s\n", pull.getNumber(), e);
             result = ProcessorPullState.ERROR;
         }
 
         return result;
     }
 
     public boolean isAdminUser(final String username) {
         return adminList.has(username);
     }
 
     public boolean isMerged(final PullRequest pull) {
         if (pull == null) {
             return false;
         }
 
         if (!pull.getState().equals("closed")) {
             return false;
         }
 
         try {
             if (pullRequestService.isMerged(pull.getBase().getRepo(), pull.getNumber())) {
                 return true;
             }
         } catch (IOException ignore) {
             System.err.printf("Cannot get Merged information of the pull request %d: %s.\n", pull.getNumber(), ignore);
             ignore.printStackTrace(System.err);
         }
 
         try {
             final List<Comment> comments = issueService.getComments(pull.getBase().getRepo(), pull.getNumber());
             for (Comment comment : comments) {
                 if (comment.getBody().toLowerCase().indexOf("merged") != -1) {
                     return true;
                 }
             }
         } catch (IOException ignore) {
             System.err.printf("Cannot get comments of the pull request %d: %s.\n", pull.getNumber(), ignore);
             ignore.printStackTrace(System.err);
         }
 
         return false;
     }
 
     public BuildResult checkBuildResult(PullRequest pullRequest) {
         BuildResult buildResult = BuildResult.UNKNOWN;
         List<Comment> comments;
         try {
             comments = issueService.getComments(repository, pullRequest.getNumber());
         } catch (IOException e) {
             System.err.println("Error to get comments for pull request : " + pullRequest.getNumber());
             e.printStackTrace(System.err);
             return buildResult;
         }
         for (Comment comment : comments) {
             Matcher matcher = BUILD_OUTCOME.matcher(comment.getBody());
             while (matcher.find()) {
                 buildResult = BuildResult.valueOf(matcher.group(2));
             }
         }
         return buildResult;
     }
 
     // -------- Bugzilla related methods
     public Bug getBug(Integer bugzillaId) {
         return bugzillaClient.getBug(bugzillaId);
     }
 
     public boolean updateBugzillaStatus(Integer bugzillaId, Bug.Status status) {
         return bugzillaClient.updateBugzillaStatus(bugzillaId, status);
     }
 
     // -------- Github related methods
     public PullRequest getPullRequest(int id) throws IOException {
         return getPullRequest(repository, id);
     }
 
     public PullRequest getPullRequest(String upstreamOrganization, String upstreamRepository, int id) throws IOException {
         return getPullRequest(RepositoryId.create(upstreamOrganization, upstreamRepository), id);
     }
 
     public PullRequest getPullRequest(IRepositoryIdProvider repository, int id) throws IOException {
         return pullRequestService.getPullRequest(repository, id);
     }
 
     public List<PullRequest> getPullRequests(String state) {
         List<PullRequest> result;
         try {
             result = pullRequestService.getPullRequests(repository, state);
         } catch (IOException e) {
             System.err.printf("Couldn't get pull requests in state %s of repository %s due to %s.\n", state, repository, e);
             result = new ArrayList<PullRequest>();
         }
         return result;
     }
 
     public List<Comment> getPullRequestComments(int pullNumber) {
         List<Comment> result;
         try {
             result = issueService.getComments(repository, pullNumber);
         } catch (IOException e) {
             System.err.printf("Couldn't get comments of pull request #%d due to %s.\n", pullNumber, e);
             result = new ArrayList<Comment>();
         }
         return result;
     }
 
     public void postGithubStatus(PullRequest pull, String targetUrl, String status) {
         try {
             CommitStatus commitStatus = new CommitStatus();
             commitStatus.setTargetUrl(targetUrl);
             commitStatus.setState(status);
             commitService.createStatus(repository, pull.getHead().getSha(), commitStatus);
         } catch (Exception e) {
             System.err.printf("Problem posting a status build for sha: %s\n", pull.getHead().getSha());
             e.printStackTrace(System.err);
         }
     }
 
     public void postGithubComment(PullRequest pull, String comment) {
         try {
             issueService.createComment(repository, pull.getNumber(), comment);
         } catch (IOException e) {
             System.err.printf("Problem posting a comment build for pull: %d\n", pull.getNumber());
             e.printStackTrace(System.err);
         }
     }
 
     public List<Milestone> getMilestones() {
         List<Milestone> milestones;
         try {
             milestones = milestoneService.getMilestones(repository, "open");
         } catch (IOException e) {
             System.err.printf("Problem getting milestones");
             e.printStackTrace(System.err);
             milestones = new ArrayList<Milestone>();
         }
         return milestones;
     }
 
     public Milestone createMilestone(String title) {
         Milestone newMilestone = new Milestone();
         newMilestone.setTitle(title);
         Milestone returnMilestone = null;
         try {
             returnMilestone = milestoneService.createMilestone(repository, newMilestone);
         } catch (IOException e) {
             System.err.printf("Problem creating new milestone. title: " + title);
             e.printStackTrace(System.err);
         }
         return returnMilestone;
     }
 
     public org.eclipse.egit.github.core.Issue getIssue(int id) {
         org.eclipse.egit.github.core.Issue issue = null;
         try {
             issue = issueService.getIssue(repository, id);
         } catch (IOException e) {
             System.err.printf("Problem getting issue. id: " + id);
             e.printStackTrace(System.err);
         }
         return issue;
     }
 
     public org.eclipse.egit.github.core.Issue editIssue(org.eclipse.egit.github.core.Issue issue) {
         org.eclipse.egit.github.core.Issue returnIssue = null;
         try {
             returnIssue = issueService.editIssue(repository, issue);
         } catch (IOException e) {
             System.err.printf("Problem editing issue. id: " + issue.getId());
             e.printStackTrace(System.err);
         }
         return returnIssue;
     }
 
     public PullEvaluatorFacade getEvaluatorFacade() {
         return evaluatorFacade;
     }
 
     public Properties getProps() {
         return props;
     }
 
 }
