 package zw.co.mitech.mtutor.service;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import zw.co.mitech.mtutor.entities.Concept;
 import zw.co.mitech.mtutor.entities.Quiz;
 import zw.co.mitech.mtutor.entities.QuizQuestion;
 import zw.co.mitech.mtutor.entities.StudentConcept;
 import zw.co.mitech.mtutor.entities.StudentQuiz;
 import zw.co.mitech.mtutor.entities.StudentSubject;
 import zw.co.mitech.mtutor.entities.StudentTopic;
 import zw.co.mitech.mtutor.entities.Topic;
 import zw.co.mitech.mtutor.entities.TopicMarker;
 import zw.co.mitech.mtutor.session.ConceptFacade;
 import zw.co.mitech.mtutor.session.QuizFacade;
 import zw.co.mitech.mtutor.session.QuizQuestionFacade;
 import zw.co.mitech.mtutor.session.RankingsFacade;
 import zw.co.mitech.mtutor.session.StudentConceptFacade;
 import zw.co.mitech.mtutor.session.StudentQuizFacade;
 import zw.co.mitech.mtutor.session.StudentSubjectFacade;
 import zw.co.mitech.mtutor.session.StudentTopicFacade;
 import zw.co.mitech.mtutor.session.TopicFacade;
 import zw.co.mitech.mtutor.session.TopicMarkerFacade;
 import zw.co.mitech.mtutor.util.ApplicationConstants;
 import zw.co.mitech.mtutor.util.MenuLevel;
 import zw.co.mitech.mtutor.util.Messages;
 import zw.co.mitech.mtutor.util.OptionsUtil;
 import zw.co.mitech.mtutor.util.QuizType;
 import zw.co.mitech.mtutor.util.Rank;
 import zw.co.mitech.mtutor.util.StringUtil;
 import zw.co.mitech.mtutor.util.Txt;
 
 @Service
 public class QuizQuestionService {
 	
 	@Autowired
 	private StudentSubjectFacade studentSubjectFacade;
 	
 	@Autowired
 	private TopicFacade topicFacade;
 	@Autowired
 	private StudentTopicFacade studentTopicFacade;
 	@Autowired
 	private TopicMarkerFacade topicMarkerFacade;
 	@Autowired
 	private ConceptFacade conceptFacade;
 	@Autowired
 	private QuizFacade quizFacade;
 	@Autowired
 	private StudentQuizFacade studentQuizFacade;
 	@Autowired
 	private QuizQuestionFacade quizQuestionFacade;
 	@Autowired
 	private StudentConceptFacade studentConceptFacade;
 	@Autowired
 	private QuizService quizService;
 	@Autowired
 	private RankingsFacade rankingFacade;
 	@Autowired
 	private QuizQuestionFacade questionFacade;
 	
 	
 	
 	
 	
 	public Txt gotoSelectTopic(Txt txt){
 		long academicLevelId = txt.getAcademicLevelId();
 		String menu = getTopicsMenu(academicLevelId);
 		txt.setMessage(menu);
 		txt.putInSession(ApplicationConstants.MENU_LEVEL, MenuLevel.SELECT_TOPIC);
 		return txt;
 	}
 
 	public Txt processSelectTopic(Txt txt){
 		String response = txt.getToken(0);
 		long academicLevelId = txt.getAcademicLevelId();
 		Map<String, String> optionsMap = getTopicsMenuOptions(academicLevelId);
 		String topicId = optionsMap.get(response);
 		if(topicId == null){
 			String menu = getTopicsMenu(academicLevelId);
 			menu = "Invalid Response\n\n" + menu;
 			txt.setMessage(menu);
 			txt.putInSession(ApplicationConstants.MENU_LEVEL, MenuLevel.SELECT_TOPIC);
 			return txt;
 		}
 		txt.setTopicId( StringUtil.parseLong(topicId));
 		txt.putInSession(ApplicationConstants.MENU_LEVEL, MenuLevel.QUESTION);
 		txt = gotoNextQuestion(txt);
 		return txt;
 	}
 	
 	
 	private Txt gotoNextQuestion(Txt txt) {
 		TopicMarker topicMarker = getStudentTopicMarker(txt.getStudentId(),txt.getTopicId());
 		
 		Quiz quiz = quizFacade.find(topicMarker.getQuizId());
 		
 		StudentQuiz studentQuiz = getStudentQuiz(txt.getStudentId(),quiz.getId());
 		
 		String answer = txt.getToken(0);
 		QuizQuestion question = null;
 		StudentConcept studentConcept = null;
 		StudentTopic studentTopic = null;
 		StudentSubject studentSubject = null;
 		String expectedAnswer = txt.getFromSession(ApplicationConstants.EXPECTED_ANSWER);
 		//initialise startTime if this is new quiz
 		if(studentQuiz.getStartTime()== null){
 			studentQuiz.setStartTime(new Date());
 		}
 		
 		String msgHeader = "";
 		long nextQstnNumber = studentQuiz.getSequenceNumber() +1 ;
 		if(expectedAnswer != null){
 			
 			String questionId = txt.getFromSession(ApplicationConstants.QUESTION_ID);
 			question = quizQuestionFacade.find(Long.parseLong(questionId));
 			studentConcept = getStudentConcept(txt.getStudentId(),question.getConceptId());
 			studentTopic = getStudentTopic(txt.getStudentId(), question.getTopicId());
 			studentSubject = getStudentSubject(txt.getStudentId(),txt.getSubjectId(),txt.getAcademicLevelId());
 			
 			int points = txt.getIntFromSession(ApplicationConstants.QUESTION_POINTS);
 			
 			studentQuiz.incrementTotal(points);
 			studentConcept.incrementTotal(points);
 			studentTopic.incrementTotal(points);
 			studentSubject.incrementTotal(points);
 			if(expectedAnswer.equalsIgnoreCase(answer)){
 				//correct answer
 				studentQuiz.incrementCorrect();
 				studentQuiz.incrementPoints(points);
 				studentConcept.incrementCorrect();
 				studentConcept.incrementPoints(points);
 				studentTopic.incrementCorrect();
 				studentTopic.incrementPoints(points);
 				studentSubject.incrementCorrect();
 				studentSubject.incrementPoints(points);
 				
 				msgHeader = Messages.get(ApplicationConstants.CORRECT_RESPONSE_KEY);
 			}else{
 				//wrong answer
 				studentQuiz.incrementWrong();
 				studentConcept.incrementWrong();
 				studentTopic.incrementWrong();
 				studentSubject.incrementWrong();
 				msgHeader = Messages.get(ApplicationConstants.INCORRECT_RESPONSE_KEY);
 				msgHeader = msgHeader.replace("${answer}", expectedAnswer + question.getExplanation(answer));
 			}
 			
 		}
 		studentQuiz.setSequenceNumber(nextQstnNumber);
 		
 		Long nextQstnId = quiz.getQuestionId(nextQstnNumber);
 		
 		if(nextQstnId != null){
 			
 			question = quizQuestionFacade.find(nextQstnId);
 			String qstn = question.getSmsMessage(nextQstnNumber);
 			txt.setMessage(msgHeader + qstn);
 			txt.putInSession(ApplicationConstants.EXPECTED_ANSWER, question.getExpectedAnswer());
 			txt.putInSession(ApplicationConstants.QUESTION_ID, ""+question.getId());
 			txt.putInSession(ApplicationConstants.QUESTION_POINTS, ""+question.getPoints());
 			
 			txt.putInSession(ApplicationConstants.MENU_LEVEL, MenuLevel.QUESTION);
 			topicMarkerFacade.edit(topicMarker);
 			studentQuizFacade.edit(studentQuiz);
 			studentConceptFacade.edit(studentConcept);
 			studentTopicFacade.edit(studentTopic);
 			studentSubjectFacade.edit(studentSubject);
 			return txt;
 		}else{
 			
 			
 			if ( studentConcept != null && studentConcept.isDueForAnAssignment()) {
 				quizService.createAssignment(studentConcept, txt);
 			}
 			//increment to the next quiz if available
 				//else increment to the next concept
 					//else tell student that they have done all questions on this topic & clear topic sequence
 			
 			Quiz nextQuiz = quizFacade.findQuiz(quiz.getConceptId(), quiz.getQuizType(), quiz.getQuizNumber() + 1);
 			String msg = Messages.get(ApplicationConstants.END_CONCEPT_QUIZ_MSG);
 			if(nextQuiz != null){
 				topicMarker.setQuizId(nextQuiz.getId());
 				topicMarker.setQuizNumber(nextQuiz.getQuizNumber());
 			}else{
 				Concept nextConcept = conceptFacade.findConceptByTopicAndSequence(topicMarker.getTopicId(), topicMarker.getConceptNumber() + 1);
 				if(nextConcept != null){
 					topicMarker.setConceptId(nextConcept.getId());
 					topicMarker.setConceptNumber(nextConcept.getTopicSequence());
 				}else{
 					//clear topicMarker
 					
 					nextConcept = conceptFacade.findConceptByTopicAndSequence(topicMarker.getTopicId(),1);
					if(nextConcept!=null){
 					topicMarker.setConceptId(nextConcept.getId());
 					topicMarker.setConceptNumber(nextConcept.getTopicSequence());
 					nextQuiz = quizFacade.findQuiz(nextConcept.getId(),QuizType.CONCEPT,1);
 					topicMarker.setQuizId(nextQuiz.getId());
 					topicMarker.setQuizNumber(nextQuiz.getQuizNumber());
					}
 					//notify student that they have attempted all quizzes on this topic
 					msg = Messages.get(ApplicationConstants.END_TOPIC_QUIZ_MSG);
 				}
 			}
 			
 			topicMarkerFacade.edit(topicMarker);
 			studentQuiz.setEndTime(new Date());
 			studentQuizFacade.edit(studentQuiz);
 			studentTopicFacade.edit(studentTopic);
 			studentConceptFacade.edit(studentConcept);
 			studentSubjectFacade.edit(studentSubject);
 			Rank rank = rankingFacade.getStudentRankingByQuiz(txt.getStudentId(),quiz.getId());
 			int numOfStudents = rankingFacade.getNumberOfStudentsThatTookQuiz(quiz.getId());
 			
 			msg = msg.replace("${score}", studentQuiz.getPoints() + "/" + studentQuiz.getTotal());
			long position = rank.getRank();
			if(position == 0){
				position = 1;
			}
			//msg = msg.replace("${rank}", ""+rank.getRank());
			msg = msg.replace("${rank}", ""+position);
 			msg = msg.replace("${numberOfStudents}", ""+numOfStudents);
 			msg = msg.replace("${percentage}", StringUtil.getPercentage( studentQuiz.getPoints(), studentQuiz.getTotal()));
 			
 			txt.setMessage(msg);
 			txt.putInSession(ApplicationConstants.MENU_LEVEL, MenuLevel.END_CONCEPT_QUIZ);
 			//txt.clearSessionState();
 			
 			return txt;
 		}
 	}
 	
 	
 	
 	public StudentSubject getStudentSubject(long studentId, long subjectId, long academicLevelId) {
 		StudentSubject studentSubject = studentSubjectFacade.findStudentSubject(studentId, subjectId,academicLevelId);
 		if (studentSubject == null) {
 			studentSubject = new StudentSubject();
 			studentSubject.setSubjectId(subjectId);
 			studentSubject.setAcademicLevelId(academicLevelId);
 			studentSubject.setStudentId(studentId);
 			studentSubjectFacade.create(studentSubject);
 		}
 		return studentSubject;
 	}
 
 	public StudentConcept getStudentConcept(long studentId, long conceptId) {
 		StudentConcept studentConcept = studentConceptFacade.findStudentConcept(studentId,conceptId);
 		if(studentConcept == null){
 			studentConcept = new StudentConcept();
 			studentConcept.setConceptId(conceptId);
 			studentConcept.setStudentId(studentId);
 			studentConceptFacade.create(studentConcept);
 		}
 		return studentConcept;
 	}
 
 	
 	
 	public StudentQuiz getStudentQuiz(long studentId, Long quizId) {
 		StudentQuiz studentQuiz = studentQuizFacade.findStudentQuizBy(quizId,studentId);
 		if(studentQuiz == null){
 			studentQuiz = new StudentQuiz();
 			studentQuiz.setStartTime(new Date());
 			studentQuiz.setStudentId(studentId);
 			studentQuiz.setSequenceNumber(0);
 			studentQuiz.setQuizId(quizId);
 			studentQuizFacade.create(studentQuiz);
 		}
 		return studentQuiz;
 	}
 
 	private TopicMarker getStudentTopicMarker(long studentId,long topicId){
 		TopicMarker topicMarker = topicMarkerFacade.findStudentTopicMarker(studentId,topicId);
 		if(topicMarker == null){
 			topicMarker = new TopicMarker();
 			topicMarker.setStudentId(studentId);
 			topicMarker.setTopicId(topicId);
 			Concept concept = conceptFacade.findConceptByTopicAndSequence(topicId,ApplicationConstants.CONCEPT_START_INDEX);
 			topicMarker.setConceptId(concept.getId());
 			topicMarker.setConceptNumber(concept.getTopicSequence());
 			Quiz quiz = quizFacade.findQuiz(concept.getId(),QuizType.CONCEPT,ApplicationConstants.QUIZ_START_INDEX);
 			topicMarker.setQuizId(quiz.getId());
 			topicMarker.setQuizNumber(quiz.getQuizNumber());
 			topicMarkerFacade.create(topicMarker);
 		}
 		return topicMarker;
 	}
 
 	public Map<String, String> getTopicsMenuOptions(long academicLevelId) {
 		
 		List<Topic> topics = topicFacade.findTopicsByAcademicLevel(academicLevelId);
 		StringBuilder menuOptionBuffer = new StringBuilder();
 		int count = 1;
 			
 		for (Topic topic : topics) {
 			menuOptionBuffer.append(count +":" + topic.getId() +",");
 			count++;
 		}
 		menuOptionBuffer.append(count +":" + ApplicationConstants.ALL_TOPICS_ID );
 		return OptionsUtil.getOptionsMap(menuOptionBuffer.toString());
 	}
 
 	public Txt processAnswer(Txt txt) {
 		
 		return gotoNextQuestion(txt);
 	}
 
 	
 	
 	
 	public String getTopicsMenu(long academicLevelId) {
 		String menu = "";
 		
 		List<Topic> topics = topicFacade.findTopicsByAcademicLevel(academicLevelId);
 		String qtsnTopicsMenu = Messages.get(ApplicationConstants.TOPICS_MESSAGE_KEY);
 			
 		StringBuilder menuBuffer = new StringBuilder();
 		int count = 1;
 			
 		for (Topic topic : topics) {
 			menuBuffer.append(count +"." + topic.getTopicName() +"\n");
 			count++;
 		}
 			
 		menuBuffer.append(count +"." + ApplicationConstants.ALL_TOPICS +"\n");	
 		menu =qtsnTopicsMenu.replace("${topics}", menuBuffer.toString());
 			
 		return menu;
 	}
 	
 	public StudentTopic getStudentTopic(long studentId, long topicId) {
 		StudentTopic studentTopic = studentTopicFacade.findStudentTopic(studentId, topicId);
 		if (studentTopic == null) {
 			studentTopic = new StudentTopic();
 			studentTopic.setTopicId(topicId);
 			Topic topic  = topicFacade.find(topicId);
 			studentTopic.setSubjectId(topic.getSubjectId());
 			studentTopic.setTopic(topic.getTopicName());
 			studentTopic.setStudentId(studentId);
 			studentTopicFacade.create(studentTopic);
 		}
 		return studentTopic;
 	}
 
 	public Txt gotoNextQuiz(Txt txt) {
 		txt.putInSession(ApplicationConstants.EXPECTED_ANSWER, null);
 		txt.putInSession(ApplicationConstants.MENU_LEVEL, MenuLevel.QUESTION);
 		txt = gotoNextQuestion(txt);
 		return txt;
 	}
 
 	public void addQuestion(QuizQuestion question) {
 		questionFacade.create(question);
 	}
 
 }
