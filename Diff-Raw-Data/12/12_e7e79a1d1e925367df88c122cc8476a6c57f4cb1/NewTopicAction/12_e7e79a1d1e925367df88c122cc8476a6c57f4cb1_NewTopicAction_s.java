 package controller;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import model.CommunityDAO;
 import model.Model;
 import model.PostDAO;
 import model.TopicDAO;
 
 import org.mybeans.dao.DAOException;
 import org.mybeans.forms.FormBeanFactory;
 
 import databean.Community;
 import databean.User;
 import formbeans.NewTopicForm;
 
 public class NewTopicAction extends Action {
 
 	private FormBeanFactory<NewTopicForm> formBeanFactory = FormBeanFactory.getInstance(NewTopicForm.class, "<>\"");
 	
 	private CommunityDAO communityDAO;
 	private TopicDAO topicDAO;
 	private PostDAO postDAO;
 	
 	public NewTopicAction (Model model) {
 		communityDAO = model.getCommunityDAO();
 		topicDAO = model.getTopicDAO();
 		postDAO = model.getPostDAO();
 	}
 	@Override
 	public String getName() {
 		return "newTopic.do";
 	}
 
 	@Override
 	public String perform(HttpServletRequest request) {
 		List<String> errors = prepareErrors(request);
 		NewTopicForm form = formBeanFactory.create(request);
 		if (!form.isPresent()) return "browseCommunity.do";
 		User curUser = (User)request.getSession().getAttribute("user");
 		if (curUser == null) {
 			errors.add("You are not logged in");
 			return "browseCommunity.do";
 		}
 		if (form.getCommunityName() == null) {
 			errors.add("Community Name is required");
 			return "browseCommunity.do";
 		}
 		Community comm = null;
 		try {
 			comm = communityDAO.lookup(form.getCommunityName());
 		} catch(DAOException e) {
 			errors.add(e.getMessage());
 			return "browseCommunity.do";
 		}
 		if (comm == null) {
 			errors.add("Community named " + form.getCommunityName() + " does not exist");
 			return "browseCommunity.do";
 		}
 		request.setAttribute("form", form);
 		errors.addAll(form.getValidationErrors());
 		if (errors.size() != 0) return "viewCommunity.do?name=" + comm.getName();
		
 		
 	}
 
 
 }
