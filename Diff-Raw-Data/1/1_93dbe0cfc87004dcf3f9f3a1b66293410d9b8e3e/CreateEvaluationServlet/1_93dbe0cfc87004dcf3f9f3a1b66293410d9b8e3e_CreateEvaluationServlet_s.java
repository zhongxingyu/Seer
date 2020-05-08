 package fr.adrienbrault.notetonsta.servlet;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import fr.adrienbrault.notetonsta.dao.EvaluationDao;
 import fr.adrienbrault.notetonsta.dao.InterventionDao;
 import fr.adrienbrault.notetonsta.entity.Evaluation;
 import fr.adrienbrault.notetonsta.entity.Intervention;
 
 @WebServlet("/evaluate/intervention")
 @SuppressWarnings("serial")
 public class CreateEvaluationServlet extends HibernateServlet {
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		InterventionDao interventionDao = new InterventionDao(createEm());
 		
 		Integer id = Integer.parseInt(request.getParameter("id"));
 		Intervention intervention = interventionDao.findById(id);
 		
 		if (intervention == null) {
 			return; // 404
 		}
 		
 		request.setAttribute("intervention", intervention);
 		
 		RequestDispatcher rd = request.getRequestDispatcher("/createEvaluation.jsp");
         rd.forward(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		EntityManager entityManager = createEm();
 		
 		// Fetch Intervention
 		
 		InterventionDao interventionDao = new InterventionDao(entityManager);
 		
 		Integer id = Integer.parseInt(request.getParameter("id"));
 		Intervention intervention = interventionDao.findById(id);
 		
 		if (intervention == null) {
 			return; // 404
 		}
 		
 		request.setAttribute("intervention", intervention);
 		
 		// Handle form data
 		
 		Integer idBooster = getIntegerParameter(request, "id_booster");
 		
 		Float speakerKnowledgeMark = getFloatParameter(request, "speaker_knowledge_mark");
 		Float speakerTeachingMark = getFloatParameter(request, "speaker_teaching_mark");
 		Float speakerAnswersMark = getFloatParameter(request, "speaker_answers_mark");
 		
 		Float slidesContentMark = getFloatParameter(request, "slides_content_mark");
 		Float slidesFormatMark = getFloatParameter(request, "slides_format_mark");
 		Float slidesExamplesMark = getFloatParameter(request, "slides_examples_mark");
 		
 		String comment = request.getParameter("comments");
 		
 		EvaluationDao evaluationDao = new EvaluationDao(entityManager);
 		
 		Map<String, String> errors = new HashMap<String, String>();
 		request.setAttribute("errors", errors);
 		
 		if (idBooster == null || idBooster < 1) {
 			errors.put("id_booster", "This field is required.");
 		} else if (evaluationDao.countByIdBooster(idBooster) > 0) {
 			errors.put("id_booster", "This id has already evaluated this intervention.");
 		}
 		
 		if (speakerKnowledgeMark == null || speakerTeachingMark == null || speakerAnswersMark == null
 			|| speakerKnowledgeMark < 1 || speakerTeachingMark < 1 || speakerAnswersMark < 1
 			|| speakerKnowledgeMark > 5 || speakerTeachingMark > 5 || speakerAnswersMark > 5) {
 			errors.put("speaker_mark", "This field is required.");
 		}
 		
 		if (slidesContentMark == null || slidesFormatMark == null || slidesExamplesMark == null
 			|| slidesContentMark < 1 || slidesFormatMark < 1 || slidesExamplesMark < 1
 			|| slidesContentMark > 5 || slidesFormatMark > 5 || slidesExamplesMark > 5) {
 			errors.put("slides_mark", "This field is required.");
 		}
 		
 		if (errors.size() == 0) {
 			// Create new evaluation
 			
 			Evaluation evaluation = new Evaluation(intervention);
 			
 			evaluation.setComment(comment);
 			evaluation.setIdBooster(idBooster);
 			
 			evaluation.setSpeakerKnowledgeMark(speakerKnowledgeMark);
 			evaluation.setSpeakerTeachingMark(speakerTeachingMark);
 			evaluation.setSpeakerAnswersMark(speakerAnswersMark);
 			
 			evaluation.setSlidesContentMark(slidesContentMark);
 			evaluation.setSlidesFormatMark(slidesFormatMark);
 			evaluation.setSlidesExamplesMark(slidesExamplesMark);
 			
 			evaluationDao.beginTransaction();
 			evaluationDao.persist(evaluation);
 			evaluationDao.commitTransaction();
 			
 			response.getWriter().print("OK");
 			return;
 		}
 		
 		RequestDispatcher rd = request.getRequestDispatcher("/createEvaluation.jsp");
         rd.forward(request, response);
 	}
 	
 	private Float getFloatParameter(HttpServletRequest request, String name) {
 		String param = request.getParameter(name);
 		Float value = null;
 		
 		if (param != null) {
 			try {
 				value = Float.parseFloat(param);
 			} catch (NumberFormatException e) {
 				value = null;
 			}
 		}
 		
 		return value;
 	}
 	
 	private Integer getIntegerParameter(HttpServletRequest request, String name) {
 		String param = request.getParameter(name);
 		Integer value = null;
 		
 		if (param != null) {
 			try {
 				value = Integer.parseInt(param);
 			} catch (NumberFormatException e) {
 				value = null;
 			}
 		}
 		
 		return value;
 	}
 
 }
