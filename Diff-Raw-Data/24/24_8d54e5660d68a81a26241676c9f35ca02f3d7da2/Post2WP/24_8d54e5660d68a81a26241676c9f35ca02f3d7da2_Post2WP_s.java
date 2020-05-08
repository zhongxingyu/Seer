 package com.imdevice.pipe2wp.task;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.imdevice.pipe2wp.Post;
 import com.imdevice.pipe2wp.XmlRPCHandler;
 
 @SuppressWarnings("serial")
 public class Post2WP extends HttpServlet {
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		doGet(req,resp);
 	}
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		req.setCharacterEncoding("UTF-8");
 		resp.setContentType("text/html;charset=UTF-8");
 
 		Post post=new Post();
 		post.setPost_status("pending");
 		post.setTitle(req.getParameter("title"));
 		post.setMt_excerpt(req.getParameter("mt_excerpt"));
		post.setDescription(req.getParameter("content"));
 		try {
 			new XmlRPCHandler().callRpc1(post);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}finally{
 			post=null;//也许能帮助加快内存回收
 		}
 		/* information for debug
 		PrintWriter o=resp.getWriter();
 		o.println(req.getParameter("title"));
 		o.println("<hr>");
 		o.println(req.getParameter("content"));
 		o.flush();
         o.close();
 		*/
 		//Post post=new Post();
 		//post.setPost_status("pending");//comment out for 远端blog自主判断状态
 		//post.setTitle((String)ses.getAttribute("title"));
 		//post.setMt_excerpt(req.getParameter("mt_excerpt"));
 		//post.setDate_created_gmt(req.getParameter("date_created_gmt"));
 		//post.setDescription((String)ses.getAttribute("description"));
 
 		
 	}
 	
 
 }
 
