 package controller;
 
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 
 import model.Model;
 import model.ProblemDAO;
 
 import org.genericdao.RollbackException;
 import org.mybeans.form.FormBeanException;
 import org.mybeans.form.FormBeanFactory;
 
 import databeans.Problem;
 import databeans.User;
 import formbeans.PostProblemForm;
 
 public class PostProblemAction extends Action {
 	private FormBeanFactory<PostProblemForm> formBeanFactory = FormBeanFactory.getInstance(PostProblemForm.class);
 	
 	private ProblemDAO	problemDAO;
 
 	public PostProblemAction(Model model) {
 		problemDAO = model.getProblemDAO();
 	}
 
 	public String getName() { return "postProblem.do"; }
     
     public String perform(HttpServletRequest request) {
     	// Set up the errors list
         List<String> errors = new ArrayList<String>();
         request.setAttribute("errors",errors);
         
 		try {
 
 			User user = (User) request.getSession(false).getAttribute("user");
 	        if(!user.getUserGroup().equals("admin")) {
 	        	errors.add("Permission denied");
 	            request.setAttribute("errors",errors);
 	            return "error.jsp";
 	        }
 	        
 	        PostProblemForm form = formBeanFactory.create(request);
 			
 	        request.setAttribute("postForm",form);
 	        
 	        if (!form.isPresent()) {
 	            return "postProblem.jsp";
 	        }
 	       
 	        errors.addAll(form.getValidationErrors());
 	        if (errors.size() > 0) return "postProblem.jsp";
 
 			Problem problem = new Problem();  // id & position will be set when created
 			
 			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");      
 			Date curDate = new Date(System.currentTimeMillis()); 
 			
 			problem.setDate(formatter.format(curDate));
 			problem.setContent((fixBadChars(form.getContent())).getBytes("Unicode"));
 			problem.setTitle(fixBadChars(form.getTitle()));
 			problem.setCommentNum(0);
 			problem.setStartCode(form.getStartCode().getBytes("Unicode"));
 			problem.setTestCode(form.getTestCode().getBytes("Unicode"));
 			problem.setReferRes(form.getReferRes().getBytes("Unicode"));
 			problemDAO.create(problem);
			
	        Problem[] hotProblem = problemDAO.match();
			Problem pt = new Problem();
			Arrays.sort(hotProblem, pt.hp);
			if (hotProblem.length > 10)
				hotProblem = Arrays.copyOf(hotProblem, 10);
			
			//System.out.println(hotProblem.length);
			request.setAttribute("hotproblem", hotProblem);
 
 			request.setAttribute("errors", errors);
 			Problem[] problems = problemDAO.match();
 			Arrays.sort(problems);
 			request.setAttribute("problemlist",problems);
 			request.setAttribute("begin",1);
 			
 
 	        return "problemList.jsp";
 	 	} catch (RollbackException e) {
 			errors.add(e.getMessage());
 			return "error.jsp";
 	 	} catch (FormBeanException e) {
 			errors.add(e.getMessage());
 			return "error.jsp";
 		} catch (UnsupportedEncodingException e) {
 			errors.add(e.getMessage());
 			return "postProblem.jsp";
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
