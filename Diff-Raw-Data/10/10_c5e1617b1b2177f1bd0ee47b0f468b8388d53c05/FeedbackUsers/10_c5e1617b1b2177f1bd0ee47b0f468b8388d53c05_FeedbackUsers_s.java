 package mindmap;
 
 import mindmapGson.FeedbackJson;
 import mindmapGson.MindmapGson;
 import mindmapGson.MindmapJson;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 import java.util.List;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 
 public class FeedbackUsers extends HttpServlet {
 	private final int MAXNUM = 500;
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		return;
 	}
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		String id = req.getParameter("id");
 
 		resp.setCharacterEncoding("EUC-KR");
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Query query = new Query("feedback").addSort("date",
 				Query.SortDirection.DESCENDING);
 		List<Entity> entities = datastore.prepare(query).asList(
 				FetchOptions.Builder.withLimit(MAXNUM));
 
 		entities = datastore.prepare(query).asList(
 				FetchOptions.Builder.withLimit(MAXNUM));
 		
 		ArrayList<String> userList = new ArrayList<String>();
 		
 		for (Entity entity : entities) {
 			if (id.equals(entity.getProperty("id").toString()) &&
 					"n".equals(entity.getProperty("confirmChk").toString())) {
 				
 				if(userList.contains(entity.getProperty("user").toString()))
 					continue;
 				else
 					userList.add(entity.getProperty("user").toString());
 			}
 		}
 		
 		int userCnt = userList.size();
 		int[] userCount = new int[userList.size()];
 		
 		for (Entity entity : entities) {
 			for(int i = 0; i < userCnt; i++){
				if(userList.get(i).equals(entity.getProperty("user").toString()))
 					userCount[i]++;
 			}
 		}
 		
 		String jsonString;
 		MindmapGson myGson = new MindmapGson();
 		FeedbackJson feedback = new FeedbackJson();
 		ArrayList<String> user = feedback.getUser();
 		ArrayList<String> count = feedback.getCount();
 		int i;
 		for (i = 0; i < userCnt; i++) {
 			user.add(userList.get(i));
 			String tmpStr = userCount[i] + "";
 			count.add(tmpStr);
 		}
 		feedback.setCnt(i + "");
 		
 		jsonString = myGson.toJson(feedback);
 		resp.getWriter().print(jsonString);
 	}
 }
