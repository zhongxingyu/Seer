 package com.barchart.web.util;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.egit.github.core.Issue;
 import org.eclipse.egit.github.core.service.IssueService;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.barchart.github.MilestoneServiceExtra;
 import com.barchart.github.RepositoryServiceExtra;
 import com.barchart.pivotal.model.Story;
 import com.barchart.pivotal.service.PivotalService;
 import com.typesafe.config.Config;
 
 /**
  * Github/Issue <-> Pivotal/Story synchronization utilities.
  */
 public class UtilSyncIsstory {
 
 	static class Context {
 
 		final Config reference = Util.reference();
 
 		final Config project;
 
 		final PivotalService pivotal;
 
 		final IssueService issueService;
 		final MilestoneServiceExtra milestoneService;
 		final RepositoryServiceExtra repositoryService;
 
 		Context(final Config project) throws IOException {
 
 			this.project = project;
 
 			this.pivotal = UtilPT.pivotalService();
 
 			this.issueService = UtilGH.issueService();
 			this.milestoneService = UtilGH.milestoneService();
 			this.repositoryService = UtilGH.repositoryService();
 
 		}
 
 	}
 
 	private static final Logger log = LoggerFactory
 			.getLogger(UtilSyncIsstory.class);
 
 	/**
 	 * Github issue relative URI.
 	 */
 	// barchart/barchart-http/issues/2
 	public static String externalId(final Context context, final Issue issue) {
 
 		final String githubUser = context.reference.getString("github.owner");
 		final String githubName = context.project.getString("github.name");
 
 		return githubUser + "/" + githubName + "/issues/" + issue.getNumber();
 	}
 
 	/**
 	 * 
 	 */
 	public static void linkIssueStory(final Context context) throws IOException {
 
 		final int githubId = context.project.getInt("github.id");
 		final String githubUser = context.reference.getString("github.owner");
 		final String githubName = context.project.getString("github.name");
 
 		final int pivotalId = context.project.getInt("pivotal.id");
 		final String pivotalName = context.project.getString("pivotal.name");
 
 		final Map<String, String> params = new HashMap<String, String>();
 		params.put("filter", "all");
 		params.put("state", "open");
 
 		final List<Issue> issueList = context.issueService.getIssues(
 				githubUser, githubName, params);
 
 		for (final Issue issue : issueList) {
			log.info("issue: {}", issue);
 
 			final List<Story> storyList = context.pivotal.storyList(pivotalId,
 					externalId(context, issue));
 
 			if (storyList.size() == 0) {
 				isstoryCreate(context, issue);
 			} else {
 				isstoryUpdate(context, issue, storyList.get(0));
 			}
 
 		}
 
 	}
 
 	public static void apply(final Issue issue, final Story story) {
 
 		story.name = issue.getTitle();
 		story.description = issue.getBody();
 
 	}
 
 	public static void apply(final Story story, final Issue issue) {
 
 		issue.setTitle(story.name);
 		issue.setBody(story.description);
 
 	}
 
 	/**
 	 * 
 	 */
 	public static void isstoryCreate(final Context context, final Issue issue)
 			throws IOException {
 
 		final int githubId = context.project.getInt("github.id");
 		final String githubUser = context.reference.getString("github.owner");
 		final String githubName = context.project.getString("github.name");
 
 		final int pivotalId = context.project.getInt("pivotal.id");
 		final String pivotalName = context.project.getString("pivotal.name");
 
 		final String integrationName = context.reference
 				.getString("pivotal.integration.name");
 
 		/** Story 1 */
 		final Story story1 = new Story();
 		apply(issue, story1);
 		story1.project_id = pivotalId;
 		story1.story_type = "feature";
 		story1.integration_id = UtilPT.integration(pivotalId, integrationName);
 		story1.external_id = externalId(context, issue);
 		log.info("story1: {}", story1);
 
 		/** Story 2 */
 		final Story story2 = context.pivotal.storyCreate(story1);
 		//
 		final SyncLink sync = new SyncLink();
 		sync.github = GooGl.shortURL(issueURL(context, issue));
 		sync.pivotal = GooGl.shortURL(story2.url);
 		//
 		story2.description = sync + story2.description;
 		log.info("story2: {}", story2);
 
 		/** Story 3 */
 		final Story story3 = context.pivotal.storyUpdate(story2);
 		log.info("story3: {}", story3);
 
 	}
 
 	/**
 	 * Generate HTML issue URL;
 	 */
 	// https://github.com/barchart/barchart-http/issues/2
 	public static String issueURL(final Context context, final Issue issue) {
 		return context.reference.getString("github.site") + "/"
 				+ externalId(context, issue);
 	}
 
 	/**
 	 * 
 	 */
 	public static void isstoryUpdate(final Context context, final Issue issue,
 			final Story story) throws IOException {
 
 		final int githubId = context.project.getInt("github.id");
 		final String githubUser = context.reference.getString("github.owner");
 		final String githubName = context.project.getString("github.name");
 
 		final int pivotalId = context.project.getInt("pivotal.id");
 		final String pivotalName = context.project.getString("pivotal.name");
 
 		final String integrationName = context.reference
 				.getString("pivotal.integration.name");
 
 		final long githubTime = issue.getUpdatedAt().getTime();
 		final long pivotalTime = story.updated_at.getMillis();
 
 		final String issueName = issue.getTitle();
 		final String storyName = story.name;
 
 		final String issueDesc = issue.getBody();
 		final String storyDesc = story.description;
 
 		final boolean isName = !issueName.equals(storyName);
 		final boolean isDesc = !issueDesc.equals(storyDesc);
 
 		final boolean isAny = isName || isDesc;
 
 		if (!isAny) {
 			log.info("no change");
 			return;
 		}
 
 		log.info("change: isName={}; isDesc={};", isName, isDesc);
 
 		log.info("githubTime={}; pivotalTime={}", new DateTime(githubTime),
 				new DateTime(pivotalTime));
 
 		if (githubTime > pivotalTime) {
 
 			log.info("apply: issue -> story");
 
 			apply(issue, story);
 
 			final Story result = context.pivotal.storyUpdate(story);
 
 			log.debug("result: {}", result);
 
 		} else {
 
 			log.info("apply: story -> issue");
 
 			apply(story, issue);
 
 			final Issue result = context.issueService.editIssue(githubUser,
 					githubName, issue);
 
 			log.debug("result: {}", result);
 
 		}
 
 	}
 
 	/**
 	 * 
 	 */
 	public static void linkIssueStoryAll() throws IOException {
 
 		final Config reference = Util.reference();
 
 		final List<? extends Config> projectList = reference
 				.getConfigList("project-list");
 
 		for (final Config project : projectList) {
 
 			final Context context = new Context(project);
 
 			linkIssueStory(context);
 
 		}
 
 	}
 
 	/**
 	 * Remove issue/story name pattern.
 	 */
 	public static String isstoryDrop(final String name) {
 		return name.replaceAll("P\\d+T", "").trim();
 	}
 
 	/**
 	 * Ensure mileston/epic name pattern.
 	 */
 	public static String isstoryName(final String name, final int id) {
 		return isstoryDrop(name) + " " + "P" + id + "T";
 	}
 
 }
