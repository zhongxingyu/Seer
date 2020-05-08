 package com.drexelexp.professor;
 
 import java.sql.Timestamp;
 import java.util.List;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.drexelexp.Query;
 import com.drexelexp.review.JdbcReviewDAO;
 import com.drexelexp.review.Review;
 import com.drexelexp.user.JdbcUserDAO;
 
 /**
  * Controller for the Professor object
  * @author
  *
  */
 @Controller
 public class ProfessorController {
 	private void addUsername(Model model){
 		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 		if (authentication.getName().equals("anonymousUser")) {
 			model.addAttribute("username","");
 		} else {
 			model.addAttribute("username",authentication.getName());
 		}
 	}
 
 	private JdbcProfessorDAO _professorDAO;
 	private JdbcProfessorDAO getProfessorDAO(){
 		if(_professorDAO!=null)
 			return _professorDAO;
 		
 		ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Module.xml");
 		_professorDAO = (JdbcProfessorDAO) context.getBean("professorDAO");
 		
 		return _professorDAO;
 	}
 	
 	
 	@RequestMapping(value="/professor", method = RequestMethod.GET)
 	public ModelAndView list(Model model) {		
 		return new ModelAndView("redirect:/professor/1");
 	}
 
 	@RequestMapping(value="/professor/{pageNum}", method = RequestMethod.GET)
 	public ModelAndView listPage(@PathVariable String pageNum, Model model) {		
 		addUsername(model);
 				
 		List<Professor> professors = getProfessorDAO().getPage(Integer.parseInt(pageNum), 20);
 		
 		model.addAttribute("professors",professors);
 		model.addAttribute("pageNum",Integer.parseInt(pageNum));
 		return new ModelAndView("professor/list", "command", new Professor());
 	}
 	
 	@RequestMapping(value="/professor/show/{profID}", method = RequestMethod.GET)
 	public ModelAndView show(@PathVariable String profID, Model model) {
 		addUsername(model);
 			
 		Professor professor = getProfessorDAO().getById(Integer.parseInt(profID));
 		
 		model.addAttribute("professor",professor);
 		
 		if(professor.getReviews().size()==0){
 			Timestamp t = new Timestamp(0);
 		
			Review review =  new Review(1, "Okay so I really hated this prof!!!", 1, t,1, 1, 1);
 			professor.getReviews().add(review);
 		}
 		
 		Review newReview = new Review();
 		ModelAndView mav = new ModelAndView("professor/show");
 		mav.addObject("newReview", newReview);
 		return mav;
 	}	
 	
 	@RequestMapping(value="professor/show/{profID}", method = RequestMethod.POST)
 	public ModelAndView review(@PathVariable String profID,@ModelAttribute Professor professor, @ModelAttribute Review review, Model model) {
 		ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Module.xml");
 		JdbcReviewDAO reviewDAO =  (JdbcReviewDAO) context.getBean("reviewDAO");
 		JdbcUserDAO userDAO =  (JdbcUserDAO) context.getBean("userDAO");
 		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 		
 		review.setProfessorId(Integer.parseInt(profID));
 		review.setUser(userDAO.findByEmail(authentication.getName()));
 		
 		reviewDAO.insert(review);
 		
 		String redirectTo  ="redirect:../show/" + profID;
 		return new ModelAndView(redirectTo);
 	}
 	
 	@RequestMapping(value="/professor/search", method = RequestMethod.GET)
 	public ModelAndView search(Model model) {
 		addUsername(model);
 		
 		ModelAndView mav = new ModelAndView("professor/search");
 		mav.addObject("profQuery", new Query());
 		
 		return mav;
 	}
 
 	@RequestMapping(value="/professor/search", method = RequestMethod.POST)
 	public String search(@ModelAttribute("profQuery") Query q, Model model) {
 		String query = q.getQuery();
 		
 		List<Professor> professors = getProfessorDAO().search(query);
 				
 		model.addAttribute("professors",professors);
 
 		return "/professor/list";
 	}
 }
