 package cl.own.usi.gateway.netty.controller;
 
 import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
 import static cl.own.usi.gateway.netty.ResponseHelper.writeStringToReponse;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CREATED;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
 
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.util.CharsetUtil;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import cl.own.usi.gateway.client.WorkerClient;
 import cl.own.usi.gateway.client.WorkerClient.UserAndScoreAndAnswer;
 import cl.own.usi.model.Question;
 import cl.own.usi.service.GameService;
 
 /**
  * Controller that validate and call for storing the answer.
  * 
  * @author bperroud
  * @author nicolas
  * 
  */
 @Component
 public class AnswerController extends AbstractController {
 
 	public static final String URI_ANSWER = "/answer/";
 	protected static final int URI_ANSWER_LENGTH = URI_ANSWER.length();
 	
 	@Autowired
 	private GameService gameService;
 
 	@Autowired
 	private WorkerClient workerClient;
 		
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
 			throws Exception {
 		HttpRequest request = (HttpRequest) e.getMessage();
 
 		String uri = request.getUri();
 		uri = uri.substring(URI_API_LENGTH);
 		
 		String userId = getCookie(request, COOKIE_AUTH_NAME);
 
 		if (userId == null) {
 			writeResponse(e, UNAUTHORIZED);
 			getLogger().info("User not authorized");
 		} else {
 			try {
 				int questionNumber = Integer.parseInt(uri
 						.substring(URI_ANSWER_LENGTH));
 
 				if (!gameService
 						.validateQuestionToAnswer(questionNumber)) {
 					writeResponse(e, BAD_REQUEST);
 					getLogger().info("Invalid question number"
 							+ questionNumber);
 				} else {
 
 					getLogger().debug("Answer Question " + questionNumber
 							+ " for user " + userId);
 
 					gameService.userAnswer(questionNumber);
 
 					JSONObject object = (JSONObject) JSONValue
 							.parse(request.getContent().toString(
 									CharsetUtil.UTF_8));
 
 					Question question = gameService
 							.getQuestion(questionNumber);
 
 					Long answerLong = ((Long) object.get("answer"));
 					Integer answer = null;
 					if (answerLong != null) {
 						answer = answerLong.intValue();
 					}
 
 					// answer =
 					// gameService.validateAnswer(questionNumber,
 					// answer);
 					// boolean answerCorrect =
 					// gameService.isAnswerCorrect(questionNumber,
 					// answer);
 
 					UserAndScoreAndAnswer userAndScoreAndAnswer = workerClient
 							.validateUserAndInsertQuestionResponseAndUpdateScore(
 									userId, questionNumber, answer);
 
 					if (userAndScoreAndAnswer.userId == null) {
 						writeResponse(e, BAD_REQUEST);
 						getLogger().info("Invalid userId " + userId);
 					} else {
 						StringBuilder sb = new StringBuilder(
 								"{ \"are_u_ok\" : ");
 						if (userAndScoreAndAnswer.answer) {
 							sb.append("true");
 						} else {
 							sb.append("false");
 						}
 						sb.append(", \"good_answer\" : \""
 								+ question.getChoices().get(
										question.getCorrectChoice())
 								+ "\", \"score\" : "
 								+ userAndScoreAndAnswer.score + "}");
 
 						writeStringToReponse(sb.toString(), e, CREATED);
 					}
 				}
 			} catch (NumberFormatException exception) {
 				writeResponse(e, BAD_REQUEST);
 				getLogger().warn("NumberFormatException", exception);
 			}
 		}
 		
 	}
 
 }
