 package com.ubermind.internal.jenkinsnotifier.controller;
 
 import java.io.IOException;
 import java.util.List;
 
 import com.google.api.client.auth.oauth2.Credential;
 import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
 import com.google.api.client.json.jackson2.JacksonFactory;
 import com.google.api.services.tasks.Tasks;
 import com.google.api.services.tasks.model.Task;
 import com.google.api.services.tasks.model.TaskList;
 import com.google.api.services.tasks.model.TaskLists;
 import com.ubermind.internal.jenkinsnotifier.jenkins.JenkinsNotification;
 import com.ubermind.internal.jenkinsnotifier.jenkins.JenkinsNotification.BuildStatus;
 import com.ubermind.internal.jenkinsnotifier.util.AuthUtil;
 
 public class UserNotificationController {
 
 	private static final UserBuildNotifier notifier = new TasksNotifier();
 
 	public static void notifyUserOfBuild(String userId, JenkinsNotification buildInfo) {
 		boolean success = false;
 		try {
 			success = notifier.notifyUser(userId, buildInfo);
 		}
 		catch (Exception e) {
			e.printStackTrace(System.err);
 		}
 
 		if (!success) {
 			System.err.println("error notifying user");
 		}
 	}
 
 	private static class TasksNotifier implements UserBuildNotifier {
 
 		@Override
 		public boolean notifyUser(String userId, JenkinsNotification buildInfo) throws IOException {
			System.err.println("notifying user " + userId + " of " + buildInfo.getJobName());

 			// get the OAuth token
 			String oauthToken = getOauthToken(userId);
 
 			// get the Tasks Service
 			Tasks tasksService = new Tasks.Builder(new UrlFetchTransport(), new JacksonFactory(), null)
 				.setApplicationName("Jenkins Notifier")
 				.build();
 
 			// get the user's task lists
 			TaskLists listsResponse = tasksService.tasklists().list()
 					.setOauthToken(oauthToken)
 					.setFields("items(id,title)")
 					.execute();
 
 			// find the task that matches this build job
 			List<TaskList> lists = listsResponse.getItems();
 			String listId = null;
 			for (TaskList taskList : lists) {
 				if (taskList.getTitle().equals(buildInfo.getJobName())) {
 					listId = taskList.getId();
 					break;
 				}
 			}
 			if (listId == null) {
 				// insert a new list if it wasn't found
 				TaskList newList = new TaskList();
 				newList.setTitle(buildInfo.getJobName());
 
 				TaskList insertedList = tasksService.tasklists().insert(newList).setOauthToken(oauthToken).execute();
 				listId = insertedList.getId();
 			}
 
 			// create a new Task for this build
 			Task buildTask = new Task();
 			String title = String.format("%d - %s at %s", Integer.valueOf(buildInfo.getNumber()),
 					buildInfo.getStatus(), buildInfo.getTimestamp().toStringRfc3339());
 			buildTask.setTitle(title);
 			buildTask.setStatus(buildInfo.getStatus() == BuildStatus.SUCCESS ? "completed" : "needsAction");
 			buildTask.setPosition(String.valueOf(buildInfo.getNumber()));
 
 			// insert the task into the list
 			Task insertResponse = tasksService.tasks().insert(listId, buildTask)
 					.setOauthToken(oauthToken)
 					.execute();
 
 			return insertResponse != null;
 		}
 
 		private static String getOauthToken(String userId) throws IOException {
 			Credential credential = AuthUtil.getCredential(userId);
 			if (credential.getExpiresInSeconds().longValue() < 60) {
 				credential.refreshToken();
 			}
 
 			return credential.getAccessToken();
 		}
 
 	}
 
 	@SuppressWarnings("unused")
 	private static class GlassNotifier implements UserBuildNotifier {
 		@Override
 		public boolean notifyUser(String userId, JenkinsNotification buildInfo) throws IOException {
 			// TODO Auto-generated method stub
 			return false;
 		}
 	}
 
 	private static interface UserBuildNotifier {
 		public boolean notifyUser(String userId, JenkinsNotification buildInfo) throws IOException;
 	}
 
 }
