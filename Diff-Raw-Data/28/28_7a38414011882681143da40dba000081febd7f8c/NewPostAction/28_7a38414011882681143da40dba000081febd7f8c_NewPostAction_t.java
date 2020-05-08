 package controller;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import model.Model;
 import model.PostDAO;
 import model.TopicDAO;
 import model.MessageDAO;
 import model.UserDAO;
 
 import org.mybeans.dao.DAOException;
 import org.mybeans.factory.RollbackException;
 import org.mybeans.factory.Transaction;
 import org.mybeans.forms.FormBeanFactory;
 
 import databean.Post;
 import databean.Topic;
 import databean.User;
 import formbeans.NewPostForm;
 
 public class NewPostAction extends Action{
 
 	private FormBeanFactory<NewPostForm> formBeanFactory = FormBeanFactory.getInstance(NewPostForm.class, "<>\"");
 
 	private PostDAO postDAO;
 	private TopicDAO topicDAO;
 	private UserDAO userDAO;
     private MessageDAO messageDAO;
 	
 	public NewPostAction(Model model){
 		postDAO = model.getPostDAO();
 		topicDAO = model.getTopicDAO();
 		messageDAO = model.getMessageDAO();
 		userDAO = model.getUserDAO();
 	}
 	
 	@Override
 	public String getName() {
 		// TODO Auto-generated method stub
 		return "newPost.do";
 	}
 
 	@Override
 	public String perform(HttpServletRequest request) {
 		// TODO Auto-generated method stub
 		List<String> errors = prepareErrors(request);
 		NewPostForm form = formBeanFactory.create(request);
 		if (!form.isPresent()) return "browseCommunity.do";
 		
 		
 		request.setAttribute("form", form);
 		
 		errors.addAll(form.getIntegerValidationErrors());
 		if(errors.size() != 0)
 			return "browseCommunity.do";
 		
 	
 		
 		try {
 			Topic topic = topicDAO.lookup(form.getTopicIdAsInt());
 			if(topic == null){
 				errors.add("No such topic!");
 				return "browseCommunity.do";
 			}
 			User curUser = (User)request.getSession().getAttribute("user");
 			if (curUser == null) {
 				errors.add("You are not logged in");
 				return "viewTopic.do?topicId=" + topic.getId();
 			}
 			errors.addAll(form.getValidationErrors());
 			
 			if(errors.size() != 0)
 				return "viewTopic.do?topicId=" + topic.getId();
 			
 			request.setAttribute("topic", topic);
 			Post newPost = new Post();
 			newPost.setContent(form.getPostContent());
 			newPost.setPostDate(new Date());
 			newPost.setPoster(curUser);
 			newPost.setTopic(topic);
 			postDAO.create(newPost);
 			topicDAO.addReplyCount(topic.getId());
 			Post[] list = postDAO.getPostsByTopic(topic);
 			request.setAttribute("posts", list);
 			String success = "Your post was created successfully";
 			request.setAttribute("success", success);
 			
 			if(!topic.getPoster().getUserName().equals(curUser.getUserName())){
 				System.out.println("get here!");
 				User sender = userDAO.lookup("Admin");
 				String title = "NOTICE: Topic Responded";
 				String content = curUser.getUserName() + " responded your topic: " + topic.getTitle() + " , click the "
 						+ "<a href=viewTopic.do?topicId=" + topic.getId() + ">link</a> to view it.";
 				
 				messageDAO.sendDirectly(sender, topic.getPoster(), title, content);
				
 				try {
 					Transaction.begin();
 					userDAO.updateNewMsgCount( topic.getPoster().getUserName(), 1);
 					Transaction.commit();
 				} catch(RollbackException e) {
 					throw new DAOException(e);
 				} finally {
 					if (Transaction.isActive()) Transaction.rollback();
 				}
 				
 			}
 		} catch (DAOException e) {
 			// TODO Auto-generated catch block
 			errors.add(e.getMessage());
 			return "browseCommunity.do";
 		}
 		
 		User curUser = (User)request.getSession().getAttribute("user");
 		
 		try {
 			curUser = userDAO.lookup(curUser.getUserName());
 			request.getSession().setAttribute("user", curUser);
 		} catch (DAOException e) {
 		}
 		return "topic.jsp";
 	}
 
 }
