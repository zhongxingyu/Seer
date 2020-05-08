 package controller;
 
 import static br.com.caelum.vraptor.view.Results.http;
 import infra.Email;
 import infra.EmailSender;
 import infra.EmailSenderRunnable;
 import infra.UserSession;
 
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServletResponse;
 
 import model.Answer;
 import model.Question;
 import model.QuestionStatus;
 import model.Specialty;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import dao.AnswerDao;
 
 @Resource
 public class AnswerController {
 	
 	private final Result result;
 	private final AnswerDao dao;
 	private final UserSession userSession;
 	private final EmailSender emailSender;
 	
 	public AnswerController(Result result, AnswerDao dao, UserSession userSession, EmailSender emailSender) {
 		this.result = result;
 		this.dao = dao;
 		this.userSession = userSession;
 		this.emailSender = emailSender;
 	}
 	
 	@Path("/perguntas/{questionId}/responder/")
 	public void answer(Answer answer, Long questionId) {
 		Question question = dao.getQuestion(questionId);
 		Specialty specialty = question.getSpecialty();
 		if (!userSession.isAuthenticated()) {
 			result.use(http()).sendError(HttpServletResponse.SC_UNAUTHORIZED);
 			return;
 		}
 		answer.setAuthor(userSession.getLoggedUser());
 		answer.setQuestion(question);
		if (userSession.getLoggedUser().equals(question.getAuthor()))
 			question.setStatus(QuestionStatus.OPEN);
		else if (userSession.getLoggedUser().isSpecialistIn(specialty))
 			question.setStatus(QuestionStatus.ANSWERED);
 		dao.save(answer);
 		sendEmailToAuthor(answer);
 		result.redirectTo(QuestionController.class).detail(questionId);
 	}
 	
 	private void sendEmailToAuthor(Answer answer) {
 		ArrayList<String> receivers = new ArrayList<String>();
 		Question question = answer.getQuestion();
 		String subject = "Uma resposta para sua pergunta - " + question.getTitle();
 		
 		String message = Email.templateForMessage(answer.getDescription(), answer.getQuestion().getId());
 		
 		receivers.add(question.getEmail());
 		emailSender.sendEmail(receivers, message, subject);
 	}
 }
