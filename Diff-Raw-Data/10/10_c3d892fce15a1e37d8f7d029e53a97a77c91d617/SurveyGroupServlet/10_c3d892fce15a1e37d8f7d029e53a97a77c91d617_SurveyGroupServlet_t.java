 package com.gallatinsystems.survey.app.web;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.gallatinsystems.framework.dao.BaseDAO;
 import com.gallatinsystems.survey.domain.SurveyGroup;
 
 public class SurveyGroupServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -9158765660952256159L;
 
 	private static final Logger log = Logger.getLogger(SurveyGroupServlet.class
 			.getName());
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) {
 		String action = req.getParameter("action");
 		String outString = new String();
 		BaseDAO<SurveyGroup> surveyGroupDAO = new BaseDAO<SurveyGroup>(
 				SurveyGroup.class);
 		if (action != null && action.equals("addSurveyGroup")) {
 			String code = req.getParameter("code");
 			//String description = req.getParameter("description");
 			SurveyGroup surveyGroup = new SurveyGroup();
 			surveyGroup.setCode(code);						
 			surveyGroupDAO.save(surveyGroup);
 			outString = surveyGroup.toString();
 		} else if (action != null && action.equals("associateSurveyGroup")) {
			throw new RuntimeException("associateSurveyGroup not implemented");
 		} else if (action != null
 				&& action.equals("associateSurveyToSurveyGroup")) {
 
 			throw new RuntimeException("associateSurveyToSurveyGroup is Not Implemented");
 		}
 		resp.setContentType("text/html");
 		try {
 			resp.getWriter().print(outString);
 		} catch (IOException e) {
 			log
 					.log(Level.SEVERE,
 							"could not perform survey group operation", e);
 		}
 	}
 
 }
