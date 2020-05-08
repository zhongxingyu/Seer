 package th.ac.kbu.cs.ExamProject.Exception;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.dao.DataAccessException;
 import org.springframework.orm.hibernate3.HibernateSystemException;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
 import org.springframework.web.servlet.HandlerExceptionResolver;
 import org.springframework.web.servlet.ModelAndView;
 
 import th.ac.kbu.cs.ExamProject.Util.BeanUtils;
 
 @Component
 public class CoreExceptionResolver implements HandlerExceptionResolver{
 
 	@Override
 	public ModelAndView resolveException(HttpServletRequest request,
 			HttpServletResponse response, Object handler, Exception ex) {
 		ModelAndView mv = null;
 		if(ex instanceof CoreRedirectException){
 			CoreRedirectException cre = (CoreRedirectException) ex;
 			StringBuilder modelStr = new StringBuilder();
 			modelStr.append("redirect:")
 						.append(cre.getPath())
 						.append("?error=")
 						.append(cre.getMessage());
 			if(BeanUtils.isNotEmpty(cre.getParamStr())){
 				modelStr.append("&").append(cre.getParamStr());
 			}
 			mv = new ModelAndView(modelStr.toString());
 		}else if (ex instanceof CoreException){
 			mv = new ModelAndView("errorJson");
 			mv.addObject("message", ex.getMessage());
 		}else if (ex instanceof HibernateSystemException){
 			System.out.println("--------------------------------------------------");
 			ex.printStackTrace();
 			System.out.println("--------------------------------------------------");
 			mv = new ModelAndView("errorJson");
 			mv.addObject("message", ex.getMessage());
 		}else if (ex instanceof DataAccessException){
 			System.out.println("--------------------------------------------------");
 			ex.printStackTrace();
 			System.out.println("--------------------------------------------------");
 			mv = new ModelAndView("errorJson");
 			mv.addObject("message", ex.getMessage());
 		}else if(ex instanceof AccessDeniedException){
 			mv = new ModelAndView("redirect:/errors/access-denied.html");
		}else if(ex instanceof BindException){
			mv = new ModelAndView("errorJson");
			mv.addObject("message", "ไม่พบข้อมูล");
 		}
 		
 		
 //		if(ex instanceof AccessDeniedException){
 //			mv = new ModelAndView("redirect:/errors/access-denied.html");
 //		}else if (ex instanceof ContentFileException){
 //			ContentFileException cfe = (ContentFileException) ex;
 //			mv = new ModelAndView("redirect:/main/content.html?path="+cfe.getFolderId()+"&error="+cfe.getMessage());
 //		}else if (ex instanceof AssignmentException){
 //			AssignmentException ae = (AssignmentException) ex;
 //			mv = new ModelAndView("redirect:/assignment/select.html?error="+ae.getMessage());
 //		}else if (ex instanceof TaskManagementException){
 //			TaskManagementException tme = (TaskManagementException) ex;
 //			mv = new ModelAndView("redirect:/management/task/taskList.html?error="+tme.getMessage());
 //		}else if (ex instanceof HibernateException){
 //			mv = new ModelAndView("errorJson");
 //			mv.addObject("message", ex.getMessage());
 //		}else if (ex instanceof MainException){
 //			mv = new ModelAndView("errorJson");
 //			mv.addObject("message", ex.getMessage());
 //		}else if (ex instanceof NullPointerException){
 //			mv = new ModelAndView("errorJson");
 //			mv.addObject("message", ex.getMessage());
 //		}
 		response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
 		return mv;
 	}
 
 }
