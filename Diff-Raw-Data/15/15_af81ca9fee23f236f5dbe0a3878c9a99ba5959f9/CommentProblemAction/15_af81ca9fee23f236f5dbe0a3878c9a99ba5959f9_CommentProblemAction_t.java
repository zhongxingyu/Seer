 package controller;
 
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 
 import model.Model;
 import model.PCommentDAO;
 import model.ProblemDAO;
import model.StatisticDAO;
 
import org.genericdao.MatchArg;
 import org.genericdao.RollbackException;
 import org.mybeans.form.FormBeanException;
 import org.mybeans.form.FormBeanFactory;
 
 import databeans.PComment;
 import databeans.Problem;
import databeans.Statistic;
 import databeans.User;
 import formbeans.CommentForm;
 
 public class CommentProblemAction extends Action {
 	private FormBeanFactory<CommentForm> formBeanFactory = FormBeanFactory.getInstance(CommentForm.class);
 	
 	private PCommentDAO pcommentDAO;
 	private ProblemDAO problemDAO;
	private StatisticDAO statisticDAO;
 
 	public CommentProblemAction(Model model) {
 		pcommentDAO = model.getPCommentDAO();
 		problemDAO = model.getProblemDAO();
		statisticDAO = model.getStatisticDAO();
 	}
 
 	public String getName() { return "commentproblem.do"; }
     
     public String perform(HttpServletRequest request) {
     	// Set up the errors list
         List<String> errors = new ArrayList<String>();
         request.setAttribute("errors",errors);
         
 		try {
 			int problemid = Integer.parseInt(request.getParameter("problemid"));
 			Problem problem = problemDAO.read(problemid);
 			User user = (User) request.getSession(false).getAttribute("user");
 	                
 	        CommentForm form = formBeanFactory.create(request);
 	        request.setAttribute("form",form);
 	        
 	        if (!form.isPresent()) {
 	            return "viewproblem.jsp";
 	        }
 	       
 	        
 	        errors.addAll(form.getValidationErrors());
 	        if (errors.size() > 0) return "viewproblem.jsp";
 
 			PComment comment = new PComment();  // id & position will be set when created
 			
 			SimpleDateFormat formatter =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");      
 			Date curDate = new Date(System.currentTimeMillis()); 
 			comment.setDate(formatter.format(curDate));
 			comment.setContent((fixBadChars(form.getContent())).getBytes("Unicode"));
 			comment.setProblemId(problem.getId());
 			comment.setUser(user.getUserName());
 			pcommentDAO.create(comment);
 			
 			problem.setCommentNum(problem.getCommentNum()+1);
 			
 			problemDAO.update(problem);
 			request.setAttribute("errors",errors);
 			
 	        request.setAttribute("problem",problem);
 			
 			PComment[] comments = pcommentDAO.getComments(problem.getId());
			Statistic[] stat = statisticDAO.match(MatchArg.equals("problemId", problem.getId()));
 			request.setAttribute("commentlist",comments);
 			request.setAttribute("problem", problem);
 			request.setAttribute("begin",1);
			request.setAttribute("stat", stat);
 			
 	        return "viewproblem.jsp";
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
