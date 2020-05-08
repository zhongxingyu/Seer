 package controller;
 
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 
 import model.BlogDAO;
 import model.CommentDAO;
 import model.Model;
 
 import org.genericdao.RollbackException;
 import org.genericdao.Transaction;
 import org.mybeans.form.FormBeanException;
 import org.mybeans.form.FormBeanFactory;
 
 import databeans.Blog;
 import databeans.Comment;
 
 import formbeans.PostBlogForm;
 
 public class EditBlogAction extends Action {
 	private FormBeanFactory<PostBlogForm> formBeanFactory = FormBeanFactory.getInstance(PostBlogForm.class);
 	
 
 	private BlogDAO  blogDAO;
 	private CommentDAO commentDAO;
 	
     public EditBlogAction(Model model) {
     	blogDAO  = model.getBlogDAO();
     	commentDAO = model.getCommentDAO();
 	}
 
     public String getName() { return "editblog.do"; }
 
     public String perform(HttpServletRequest request) {
         List<String> errors = new ArrayList<String>();
         request.setAttribute("errors",errors);
         
 		try {
 			PostBlogForm form = formBeanFactory.create(request);
 		    
     		int id = Integer.parseInt((String)request.getParameter("id"));
     		Blog p = blogDAO.read(id);
     		if (p == null) {
     			errors.add("No blog with id="+id);
     			return "blog.jsp";
     		}
     		
     		PostBlogForm form2 = new PostBlogForm();
 
     		form2.setContent(p.getReadableCon().replaceAll("<br>", "\n"));
     		form2.setTitle(p.getTitle());
 	        // Any validation errors?
 	        if (!form.isPresent()) {	        	
 	    		request.setAttribute("form",form2);  
 	    		request.setAttribute("edit", 1);
 	    		request.setAttribute("id", id);
 	            return "blog.jsp";
 	        }
 	         
 	       
 	        errors.addAll(form.getValidationErrors());
 	        if (errors.size() > 0) {
 	        	request.setAttribute("form",form2); 
 	        	request.setAttribute("edit", 1);
 	        	request.setAttribute("id", id);
 	        	return "blog.jsp";
 	        }
     		
     		SimpleDateFormat formatter =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");      
 			Date curDate = new Date(System.currentTimeMillis()); 
 			Transaction.begin();
 			p.setDate(formatter.format(curDate));
 			p.setContent((fixBadChars(form.getContent())).getBytes("Unicode"));
 			
 			p.setTitle(fixBadChars(form.getTitle()));
 			blogDAO.update(p);
 			Transaction.commit();
     		request.setAttribute("blog",p);  
 			Comment[] comments = commentDAO.getComments(p.getId());
 			request.setAttribute("commentlist",comments);
     		
            return "viewblog.jsp";
     	} catch (RollbackException e) {
     		errors.add(e.getMessage());
     		return "error.jsp";
     	} catch (FormBeanException e) {
     		errors.add(e.getMessage());
     		return "error.jsp";
     	}catch (NumberFormatException e) {
     		errors.add(e.getMessage());
     		return "error.jsp";
     	}catch (UnsupportedEncodingException e) {
     		errors.add(e.getMessage());
 			return "error.jsp";
 		}finally {
 			if (Transaction.isActive()) Transaction.rollback();
 		}
     }
 		
 		private String fixBadChars(String s) {
 			if (s == null || s.length() == 0) return s;
 			
 			Pattern p = Pattern.compile("[<>\"&]");
 	        Matcher m = p.matcher(s);
 	        StringBuffer b = null;
 	        while (m.find()) {
 	            if (b == null) b = new StringBuffer();
 	            switch (s.charAt(m.start())) {
 	                case '<':  m.appendReplacement(b,"&lt;");
 	                           break;
 	                case '>':  m.appendReplacement(b,"&gt;");
 	                           break;
 	                case '&':  m.appendReplacement(b,"&amp;");
 	                		   break;
 	                case '"':  m.appendReplacement(b,"&quot;");
 	                           break;
 	                default:   m.appendReplacement(b,"&#"+((int)s.charAt(m.start()))+';');
 	            }
 	        }
 	        
 	        if (b == null) return s;
 	        m.appendTail(b);
 	        return b.toString();
 	    }
 }
