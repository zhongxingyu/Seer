 package com.forum.client;
 
 import com.forum.client.data.CategoryData;
 import com.forum.client.data.PostData;
 import com.forum.client.data.TopicData;
 import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
 
@RemoteServiceRelativePath("forumTasks")
public interface ForumService extends RemoteService {
 
	// //gets posts of a thread
 	PostData[] getPosts(int threadID);

	// //gets Threads in a category
 	TopicData[] getThreads(int categoryID);

	// //gets Categories
 	CategoryData[] getCategories();

	// Adds a new Post to a thread returns ID
 	int addPost(String text, int thrID, int authID);

	// Adds a Thread to a category returns ID
 	int addThread(String name, int catID, int authID);

 }
