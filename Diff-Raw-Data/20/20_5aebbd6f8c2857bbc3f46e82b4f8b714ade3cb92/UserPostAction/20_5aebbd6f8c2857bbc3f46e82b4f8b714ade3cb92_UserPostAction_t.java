 package com.web.userPost;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionForward;
 import org.hibernate.Session;
 
 import util.HibernateUtil;
 
 public class UserPostAction extends org.apache.struts.action.Action {
 
     public ActionForward execute(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
         
     	UserPostForm upf = (UserPostForm) form;
         HttpSession httpSession = request.getSession(true);
        System.out.println(upf.getContent() + " " + upf.getContent() + " " + upf.getTitle());
        String key = HibernateUtil.createAndStorePost(upf.getTopic(), upf.getContent(), upf.getTitle(), upf.getEmail());
         
         httpSession.setAttribute("postkey", key);
 		 
 		return mapping.findForward("postsuccess");
     }
 }
