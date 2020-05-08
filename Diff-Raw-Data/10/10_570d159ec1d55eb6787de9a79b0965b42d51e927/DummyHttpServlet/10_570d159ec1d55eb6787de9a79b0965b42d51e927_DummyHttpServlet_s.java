 package com.worpdress.salaboy.smartprocessdiscovery.server;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.io.StringReader;
 import java.nio.CharBuffer;
 import java.util.Iterator;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
import com.plugtree.smartknowledgediscovery.util.DSRequest;
 import com.plugtree.smartknowledgediscovery.util.InterviewRequest;
 import com.plugtree.smartknowledgediscovery.util.InterviewResponse;
 import com.plugtree.smartknowledgediscovery.util.OperationType;
 import com.plugtree.smartknowledgediscovery.util.QuestionRequest;
 import com.plugtree.smartknowledgediscovery.util.QuestionResponse;
 import com.plugtree.smartknowledgediscovery.util.QuestionnaireRequest;
 import com.plugtree.smartknowledgediscovery.util.QuestionnaireResponse;
 import com.plugtree.smartprocessdiscovery.model.process.Interview;
 import com.plugtree.smartprocessdiscovery.model.questionaire.Question;
 import com.plugtree.smartprocessdiscovery.model.questionaire.Questionnaire;
import com.smartgwt.client.types.DSOperationType;
import com.worpdress.salaboy.smartprocessdiscovery.client.questionnaire.AnsweredQuestionnaireRestDS;
 import com.worpdress.salaboy.smartprocessdiscovery.client.questionnaire.QuestionnaireRestDS;
 
 public class DummyHttpServlet extends HttpServlet {
 
 
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 		resp.setContentType("text/xml");
 		PrintWriter out = resp.getWriter();
 		out.println("<response>"+
 				" <status>0</status>"+
 				"<startRow>0</startRow>"+
 				"<endRow>0</endRow>"+
 				"<totalRows>1</totalRows>"+
 				"<data>"+
 				"<record>"+
 				"<id>1</id>"+
 				"<question>"+ "What's your favorite Horror movie?" +"</question>"+
 				"<description>"+ "Horror Movie Question" +"</description>"+
 				"<category>"+ "Movie" +"</category>"+
 				"</record>"+
 				"</data>"+
 		"</response>");
 
 	}
 
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 		try {
 			createResponse(req,resp);
 		} catch (JAXBException e) {
 			e.printStackTrace();
 		}
 
 
 	}
 
 	public void createResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException, JAXBException{
 
 
 		//Shows the content type, in this case text/xml
 		System.out.println(req.getContentType());		
 		//Shows the URL of the service Required
 		System.out.println(req.getPathInfo());
 	
 		if(req.getPathInfo().contains("question"))createQuestionResponse(req,resp);
 		if(req.getPathInfo().contains("interview"))createInterviewResponse(req,resp);
 		if(req.getPathInfo().contains("questionnaire"))createQuestionnaireResponse(req,resp);
 	}
 	
 	private void createQuestionResponse(HttpServletRequest req,HttpServletResponse resp) throws IOException, JAXBException{
 		//The xml received in the request.
 		BufferedReader in = req.getReader();
 
 		JAXBContext jaxbReqContext = JAXBContext.newInstance(QuestionRequest.class);
 		Unmarshaller reqUnmarshaller = jaxbReqContext.createUnmarshaller();
 
 		QuestionRequest questionRequest =(QuestionRequest)reqUnmarshaller.unmarshal(in);
 
 		if(questionRequest.getOperationType()== OperationType.FETCH){
 			QuestionResponse questionResponse = new QuestionResponse();
 
 			Question question = new Question();
 			question.setId((long)1);
 			question.setNotes("Horror Movie Question.");
 			question.setText("What's your favorite Horror movie?");
 
 			Question question2 = new Question();
 			question2.setId((long)2);
 			question2.setNotes("History Question.");
 			question2.setText("What happend on November the 5th?");
 			questionResponse.addQuestion(question2);
 			questionResponse.addQuestion(question);
 
 			try {
 				JAXBContext jaxbContext = JAXBContext.newInstance(QuestionResponse.class);
 				Marshaller marshaller = jaxbContext.createMarshaller();
 
 				resp.setContentType("text/xml");
 				PrintWriter out = resp.getWriter();
 				marshaller.marshal(questionResponse, out);
 
 			} catch (JAXBException e) {
 				e.printStackTrace();
 			}	 
 		}
 
 		if(questionRequest.getOperationType()== OperationType.ADD){
 			Iterator<Question> it = questionRequest.getQuestions().iterator();
 			while(it.hasNext()){
 				//TODO: the question will be persisted using JPA.
 				/* <!-- XML response to an add request. Should contain details of the newly added  
 				      record as stored on the server.  
 				      Normally this response would be generated dynamically on the server. --> */ 
 				Question question = it.next();
 				QuestionResponse questionRespRemove = new QuestionResponse();
 				Question questionUpdated = question;				
 				questionRespRemove.addQuestion(questionUpdated);				
 				try {
 					JAXBContext jaxbContext = JAXBContext.newInstance(QuestionResponse.class);
 					Marshaller marshaller = jaxbContext.createMarshaller();
 
 					resp.setContentType("text/xml");
 					PrintWriter out = resp.getWriter();
 					marshaller.marshal(questionRespRemove, out);
 
 				} catch (JAXBException e) {
 					e.printStackTrace();
 				}	 
 
 			}
 		}
 		if(questionRequest.getOperationType()== OperationType.REMOVE){
 			/*	# <!-- XML response to an remove request. Should contain the primary key field of  
 			#      the record that was removed from the server-side data set.  
 			#      Normally this response would be generated dynamically on the server. -->  
 			 */
 			Iterator<Question> it = questionRequest.getQuestions().iterator();
 			while(it.hasNext()){
 
 				Question question = it.next();
 				QuestionResponse questionRespRemove = new QuestionResponse();
 				Question questionRemoved = new Question();
 				questionRemoved.setId(question.getId());
 				questionRespRemove.addQuestion(questionRemoved);
 
 				try {
 					JAXBContext jaxbContext = JAXBContext.newInstance(QuestionResponse.class);
 					Marshaller marshaller = jaxbContext.createMarshaller();
 
 					resp.setContentType("text/xml");
 					PrintWriter out = resp.getWriter();
 					marshaller.marshal(questionRespRemove, out);
 
 				} catch (JAXBException e) {
 					e.printStackTrace();
 				}	 
 
 
 			}
 
 		}
 		if(questionRequest.getOperationType()== OperationType.UPDATE){
 			/*# <!-- XML response to an update request. Should contain details of the updated record  
 			#      which will be integrated into the client data cache.  
 			#      Normally this response would be generated dynamically on the server. -->  
 			 */
 			Iterator<Question> it = questionRequest.getQuestions().iterator();
 			while(it.hasNext()){
 
 				Question question = it.next();
 				QuestionResponse questionRespRemove = new QuestionResponse();
 				Question questionUpdated = question;				
 				questionRespRemove.addQuestion(questionUpdated);				
 				try {
 					JAXBContext jaxbContext = JAXBContext.newInstance(QuestionResponse.class);
 					Marshaller marshaller = jaxbContext.createMarshaller();
 
 					resp.setContentType("text/xml");
 					PrintWriter out = resp.getWriter();
 					marshaller.marshal(questionRespRemove, out);
 
 				} catch (JAXBException e) {
 					e.printStackTrace();
 				}	 
 
 
 			}
 		}
 	}
 	private void createInterviewResponse(HttpServletRequest req,HttpServletResponse resp) throws IOException, JAXBException{
 		//The xml received in the request.
 		BufferedReader in = req.getReader();
 
 		JAXBContext jaxbReqContext = JAXBContext.newInstance(InterviewRequest.class);
 		Unmarshaller reqUnmarshaller = jaxbReqContext.createUnmarshaller();
 
 		InterviewRequest interviewRequest =(InterviewRequest)reqUnmarshaller.unmarshal(in);
 
 		if(interviewRequest.getOperationType()== OperationType.FETCH){
 			InterviewResponse interviewResponse = new InterviewResponse();
			
			QuestionnaireRestDS questionnaire = QuestionnaireRestDS.getInstance();
		
			
 			Interview interview1 = new Interview();
 			interview1.setId((long)1);
 			interview1.setDescription("Job Interview");
 			
 			
 			Interview interview2 = new Interview();
 			interview2.setId((long)2);
 			interview2.setDescription("Passport Interview");		
 			
 			interviewResponse.addInterview(interview1);		
 			interviewResponse.addInterview(interview2);
 			
 			try {
 				JAXBContext jaxbContext = JAXBContext.newInstance(InterviewResponse.class);
 				Marshaller marshaller = jaxbContext.createMarshaller();
 
 				resp.setContentType("text/xml");
 				PrintWriter out = resp.getWriter();
 				marshaller.marshal(interviewResponse, out);
 
 			} catch (JAXBException e) {
 				e.printStackTrace();
 			}	 
 		}
 
 		if(interviewRequest.getOperationType()== OperationType.ADD){
 			
 		}
 		if(interviewRequest.getOperationType()== OperationType.REMOVE){
 			/*	# <!-- XML response to an remove request. Should contain the primary key field of  
 			#      the record that was removed from the server-side data set.  
 			#      Normally this response would be generated dynamically on the server. -->  
 			 */
 		}
 		if(interviewRequest.getOperationType()== OperationType.UPDATE){
 			/*# <!-- XML response to an update request. Should contain details of the updated record  
 			#      which will be integrated into the client data cache.  
 			#      Normally this response would be generated dynamically on the server. -->  
 			 */
 		}
 	}
 	private void createQuestionnaireResponse(HttpServletRequest req,HttpServletResponse resp) throws IOException, JAXBException{
 		//The xml received in the request.
 		BufferedReader in = req.getReader();
 
 		JAXBContext jaxbReqContext = JAXBContext.newInstance(QuestionnaireRequest.class);
 		Unmarshaller reqUnmarshaller = jaxbReqContext.createUnmarshaller();
 
 		QuestionnaireRequest questionnaireRequest =(QuestionnaireRequest)reqUnmarshaller.unmarshal(in);
 
 		if(questionnaireRequest.getOperationType()== OperationType.FETCH){
 			QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
 
 			Questionnaire questionnaire = new Questionnaire();
 			questionnaire.setId((long)1);
 			
 			Question question = new Question();
 			question.setId((long)1);
 			question.setNotes("Horror Movie Question.");
 			question.setText("What's your favorite Horror movie?");
 			
 			Question question2 = new Question();
 			question2.setId((long)2);
 			question2.setNotes("History Question.");
 			question2.setText("What happend on November the 5th?");
 			
 			questionnaire.addQuestion(question);
 			questionnaire.addQuestion(question2);
 			questionnaireResponse.addQuestionnaire(questionnaire);
 			
 
 			try {
 				JAXBContext jaxbContext = JAXBContext.newInstance(QuestionResponse.class);
 				Marshaller marshaller = jaxbContext.createMarshaller();
 
 				resp.setContentType("text/xml");
 				PrintWriter out = resp.getWriter();
 				marshaller.marshal(questionnaireResponse, out);
 
 			} catch (JAXBException e) {
 				e.printStackTrace();
 			}	 
 		}
 
 		if(questionnaireRequest.getOperationType()== OperationType.ADD){
 				//TODO: the question will be persisted using JPA.
 				/* <!-- XML response to an add request. Should contain details of the newly added  
 				      record as stored on the server.  
 				      Normally this response would be generated dynamically on the server. --> */ 
 		}
 		if(questionnaireRequest.getOperationType()== OperationType.REMOVE){
 			/*	# <!-- XML response to an remove request. Should contain the primary key field of  
 			#      the record that was removed from the server-side data set.  
 			#      Normally this response would be generated dynamically on the server. -->  
 			 */
 					}
 		if(questionnaireRequest.getOperationType()== OperationType.UPDATE){
 			/*# <!-- XML response to an update request. Should contain details of the updated record  
 			#      which will be integrated into the client data cache.  
 			#      Normally this response would be generated dynamically on the server. -->  
 			 */
 		}
 	}
 	
 }
