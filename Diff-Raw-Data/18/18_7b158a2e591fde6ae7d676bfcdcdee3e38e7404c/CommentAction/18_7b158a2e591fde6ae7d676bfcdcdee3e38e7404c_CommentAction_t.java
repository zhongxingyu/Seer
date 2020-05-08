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
 import model.UserDAO;
 
 import org.genericdao.RollbackException;
 import org.mybeans.form.FormBeanException;
 import org.mybeans.form.FormBeanFactory;
 
 import databeans.Blog;
 import databeans.Comment;
 import databeans.User;
 import formbeans.CommentForm;
 
 public class CommentAction extends Action {
 	private FormBeanFactory<CommentForm> formBeanFactory = FormBeanFactory.getInstance(CommentForm.class);
 	
 	private CommentDAO commentDAO;
 	private BlogDAO blogDAO;
 	private UserDAO userDAO;
 
 	public CommentAction(Model model) {
 		commentDAO = model.getCommentDAO();
 		blogDAO = model.getBlogDAO();
 		userDAO = model.getUserDAO();
 	}
 
 	public String getName() { return "comment.do"; }
     
     public String perform(HttpServletRequest request) {
     	// Set up the errors list
         List<String> errors = new ArrayList<String>();
         request.setAttribute("errors",errors);
         
 		try {
 			int blogid = Integer.parseInt(request.getParameter("blogid"));
 			Blog blog = blogDAO.read(blogid);
 			User user = (User) request.getSession(false).getAttribute("user");
 	                
 	        CommentForm form = formBeanFactory.create(request);
 	        request.setAttribute("form",form);
 	       
 	        
 	        errors.addAll(form.getValidationErrors());
	        if (errors.size() > 0) return "error.jsp";
 
 			Comment comment = new Comment();  // id & position will be set when created
 			
 			SimpleDateFormat formatter =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");      
 			Date curDate = new Date(System.currentTimeMillis()); 
 			comment.setDate(formatter.format(curDate));
 			comment.setContent((fixBadChars(form.getContent())).getBytes("Unicode"));
 			comment.setBlogId(blog.getId());
 			comment.setUser(user.getUserName());
 			commentDAO.create(comment);
 			
 			blog.setCommentNum(blog.getCommentNum()+1); 
 			
 			blogDAO.update(blog);
 			request.setAttribute("errors",errors);
 			
 			//request.setAttribute("comment", comment);
 			Comment[] comments = commentDAO.getComments(blog.getId());
 			
 			
 			User u = userDAO.read(blog.getEmail());
 			Blog[] archives = blogDAO.getBlogs(u.getEmail());
 
 			request.setAttribute("commentlist",comments);
 			request.setAttribute("blog", blog);
 			request.setAttribute("blogOwner",u); 
     		request.setAttribute("archives",archives); 
 			request.setAttribute("begin",1);
 			
 	        return "viewblog.jsp";
 	 	} catch (RollbackException e) {
 			errors.add(e.getMessage());
 			return "error.jsp";
 	 	} catch (FormBeanException e) {
 			errors.add(e.getMessage());
 			return "error.jsp";
 		} catch (UnsupportedEncodingException e) {
 			errors.add(e.getMessage());
 			return "error.jsp";
 		} catch (NumberFormatException e) {
 			errors.add(e.getMessage());
 			return "error.jsp";
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
