 package com.aavu.server.web.controllers;
 
 import java.util.Arrays;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.aavu.client.domain.Topic;
 import com.aavu.client.domain.User;
 import com.aavu.client.exception.HippoBusinessException;
 import com.aavu.server.service.TopicService;
 
 public class MVCBrowseController extends BasicController {
 	private static final Logger log = Logger.getLogger(MVCBrowseController.class);
 
 	private String notFoundView;
 
 	private TopicService topicService;
 
 
 
 	public String getNotFoundView() {
 		return notFoundView;
 	}
 
 
 	@Override
 	protected ModelAndView handleRequestInternal(HttpServletRequest req, HttpServletResponse arg1)
 			throws Exception {
 
 		String path = req.getPathInfo();
 
 		String[] pathParts = path.split("/");
 		log.debug("!path parts " + Arrays.toString(pathParts));
 
 
 
 		Map<String, Object> model = getDefaultModel(req);
 
 
 		// "/user/topic" splits to [,user,topic]
 		if (pathParts.length < 3) {
 			return new ModelAndView(getNotFoundView());
 		}
 
 		if (3 == pathParts.length) {
 			String userString = pathParts[2];
 
 			return userOnly(model, userString);
 		} else {
 
 			String userString = pathParts[2];
 			String topicString = pathParts[3];
 			return userAndTopic(model, userString, topicString);
 		}
 
 
 	}
 
 
 
 	@Required
 	public void setNotFoundView(String notFoundView) {
 		this.notFoundView = notFoundView;
 	}
 
 	@Required
 	public void setTopicService(TopicService topicService) {
 		this.topicService = topicService;
 	}
 
 
 	private ModelAndView userAndTopic(Map<String, Object> model, String userString,
 			String topicString) {
 		try {

			User u = userService.getUserWithNormalization(userString);

			if (u == null) {
				return new ModelAndView(getNotFoundView(), "message", "User " + userString
						+ " not found.");
			}

			Topic result = topicService.getPublicTopic(u.getUsername(), topicString);
 
 			model.put("topic", result);
 
 			model.put("foruser", result.getUser());
 
 			log.info("User: " + result.getUser() + " Topic " + result);
 
 			return new ModelAndView(getView(), model);
 		} catch (HippoBusinessException e) {
 			return new ModelAndView(getNotFoundView(), "message", topicString + " for "
 					+ userString + " not found.");
 		}
 	}
 
 
 
 	private ModelAndView userOnly(Map<String, Object> model, String userString) {
 		try {
 			User u = userService.getUserWithNormalization(userString);
 
 			Topic root = topicService.getRootTopic(u);
 
 			model.put("topic", root);
 			model.put("foruser", u);
 
 			log.info("User: " + u + " Root " + root);
 
 			return new ModelAndView(getView(), model);
 
 		} catch (Exception e) {
 			log.debug(e.getMessage());
 			return new ModelAndView(getNotFoundView(), "message", "Browse User " + userString
 					+ " not found.");
 		}
 	}
 
 }
