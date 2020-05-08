 package com.imdevice.pipe2wp.task;
 
 import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.TaskOptions.Method;
 import com.imdevice.WebSpider.Extractor;
 
 @SuppressWarnings("serial")
 public class PageWasher extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		doPost(req,resp);
 	}
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		req.setCharacterEncoding("UTF-8");
 		resp.setContentType("text/html;charset=UTF-8");
 		String dirtypage=req.getParameter("dirtypage");
 		Extractor extractor=new Extractor();
		extractor.setTitle(req.getParameter("title"));
 		String clearPage=extractor.getContent(dirtypage);
 		System.out.println("PageWash: "+req.getParameter("link"));
 		System.out.println("----------------------------------------------------------");
 		System.out.println( req.getParameter("title"));
 		System.out.println(clearPage);
 		try{
 			Queue queue=QueueFactory.getQueue("PostQueue");
 			queue.add(withUrl("/tasks/post2wp")
 					.param("link", req.getParameter("link"))
 					.param("title", req.getParameter("title"))
 					//.param("mt_excerpt", req.getParameter("mt_excerpt"))
 					.param("description", clearPage)
 					.method(Method.POST)//POST是默认值，传递长参数时最好不要用'GET'，因为'GET' url最大长度有限制，而且各浏览器和服务器软件支持不一致
 					);			
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 
 }
 
