 package controller;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import model.Model;
 import model.PCommentDAO;
 import model.ProblemDAO;
 import model.StatisticDAO;
import model.UserDAO;
 
 import org.genericdao.MatchArg;
 import org.genericdao.RollbackException;
 import org.mybeans.form.FormBeanException;
 import org.mybeans.form.FormBeanFactory;
 
 import databeans.PComment;
 import databeans.Problem;
 import databeans.Statistic;
 
 import formbeans.IdForm;
 
 public class ViewProblemAction extends Action {
 	private FormBeanFactory<IdForm> formBeanFactory = FormBeanFactory.getInstance(IdForm.class);
 
 	private ProblemDAO problemDAO;
 	private PCommentDAO pcommentDAO;
 	private StatisticDAO statisticDAO;
 	
     public ViewProblemAction(Model model) {
     	problemDAO  = model.getProblemDAO();
     	pcommentDAO = model.getPCommentDAO();
     	statisticDAO = model.getStatisticDAO();
 	}
 
 	public String getName() { return "problem.do"; }
 
     public String perform(HttpServletRequest request) {
         List<String> errors = new ArrayList<String>();
         request.setAttribute("errors",errors);
         
 		try {
 			IdForm form = formBeanFactory.create(request);
 
 			
 	        // Any validation errors?
 	        errors.addAll(form.getValidationErrors());
 	        if (errors.size() != 0) {
 	            return "error.jsp";
 	        }
         
     		int id = form.getIdAsInt();
     		Problem p = problemDAO.read(id);
     		if (p == null) {
     			errors.add("No problem with id="+id);
     			return "error.jsp";
     		}
     		
     		Statistic[] stat = statisticDAO.match(MatchArg.equals("problemId", p.getId()));
    		Arrays.sort(stat);
     		
     		request.setAttribute("problem",p); 
    		request.setAttribute("stat",stat);
 
 
 			PComment[] pcomments = pcommentDAO.getComments(p.getId());
 			request.setAttribute("commentlist",pcomments);
 			String begin;
 			if((begin = request.getParameter("begin")) == null) {
 				request.setAttribute("begin",1);
 			}else {
 				request.setAttribute("begin", Integer.parseInt(begin));
 			}
     		
             return "viewproblem.jsp";
     	} catch (RollbackException e) {
     		errors.add(e.getMessage());
     		return "error.jsp";
     	} catch (FormBeanException e) {
     		errors.add(e.getMessage());
     		return "error.jsp";
     	}
     }
 }
