 package mindmap;
 
 import mindmapGson.FeedbackJson;
 import mindmapGson.FeedbackVersionJson;
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
 
 public class FeedbackVersion extends HttpServlet {
 	private final int MAXNUM = 500;
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		String id = req.getParameter("id");
 		String strCnt = req.getParameter("cnt");
 		
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		Query query = new Query("mindmap").addSort("date",
 				Query.SortDirection.DESCENDING);
 		List<Entity> entities = datastore.prepare(query).asList(
 				FetchOptions.Builder.withLimit(MAXNUM));
 		
 		// accept map
 		if (entities.isEmpty()) {
 			return;
 		} else {
 			for (Entity entity : entities) {
 				if (id.equals(entity.getProperty("id").toString())) {
 
 					Entity tmp = entity.clone();
 					tmp.setProperty("cnt", strCnt);
 					int cnt = Integer.parseInt(strCnt);
 
 					for(int i = 0; i < cnt; i++){
 						String para = req.getParameter("mindmap" + i);
						tmp.setProperty("mindmap" + i, para);
 					}
 					datastore.put(tmp);
 					break;
 				}
 			}
 		}
 		// feedback n -> y
 		query = new Query("feedback").addSort("date",
 				Query.SortDirection.DESCENDING);
 		entities = datastore.prepare(query).asList(
 				FetchOptions.Builder.withLimit(MAXNUM));
 		if (entities.isEmpty()) {
 			return;
 		} else {
 			for (Entity entity : entities) {
 				if (id.equals(entity.getProperty("id").toString())) {
 
 					Entity tmp = entity.clone();
 					tmp.setProperty("confirmChk", "y");
 
 					datastore.put(tmp);
 				}
 			}
 		}
 	}
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		resp.setCharacterEncoding("EUC-KR");
 
 		String id = req.getParameter("id");
 		String user = req.getParameter("user");
 
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Query query = new Query("feedback").addSort("date",
 				Query.SortDirection.DESCENDING);
 		
 		List<Entity> entities = datastore.prepare(query).asList(
 				FetchOptions.Builder.withLimit(MAXNUM));
 
 		entities = datastore.prepare(query).asList(
 				FetchOptions.Builder.withLimit(MAXNUM));
 		
 		String jsonString;
 		MindmapGson myGson = new MindmapGson();
 		FeedbackVersionJson feedbackVersion = new FeedbackVersionJson();
 		ArrayList<String> versionList = feedbackVersion.getVersion();
 		
 		for (Entity entity : entities) {
 			if (id.equals(entity.getProperty("id").toString()) &&
 					user.equals(entity.getProperty("user").toString()) &&
 					"n".equals(entity.getProperty("confirmChk").toString())) {
 				versionList.add(entity.getProperty("version").toString());
 			}
 		}
 		String cnt = versionList.size() + "";
 		
 
 		feedbackVersion.setCnt(cnt + "");
 		jsonString = myGson.toJson(feedbackVersion);
 		resp.getWriter().print(jsonString);
 		
 		
 		
 		
 	}
 }
