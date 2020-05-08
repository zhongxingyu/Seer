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
 
 import cl.own.usi.cache.CachedUser;
 import cl.own.usi.gateway.client.UserAndScoreAndAnswer;
 import cl.own.usi.gateway.client.WorkerClient;
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
 
 		final String userId = getCookie(request, COOKIE_AUTH_NAME);
 
 		if (userId == null) {
 			writeResponse(e, UNAUTHORIZED);
 			getLogger().info("User not authorized");
 		} else {
 			try {
 				final int questionNumber = Integer.parseInt(uri
 						.substring(URI_ANSWER_LENGTH));
 
 				if (!gameService.validateQuestionToAnswer(questionNumber)) {
 					writeResponse(e, BAD_REQUEST);
 					getLogger()
 							.info("Invalid question number {}", questionNumber);
 				} else {
 
 					final CachedUser cachedUser = getCacheManager().loadUser(userId);
 					
 					if (cachedUser == null) {
 						writeResponse(e, UNAUTHORIZED);
 						getLogger().info("Invalid userId {}", userId);
 						return;
 					}
 					getLogger().debug(
 							"Answer Question {} for user {} ", questionNumber, userId);
 
 //					gameService.userAnswer(questionNumber);
 
 					final JSONObject object = (JSONObject) JSONValue.parse(request
 							.getContent().toString(CharsetUtil.UTF_8));
 
 					final Question question = gameService.getQuestion(questionNumber);
 
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
 
 					final UserAndScoreAndAnswer userAndScoreAndAnswer = workerClient
 							.validateUserAndInsertQuestionResponseAndUpdateScore(
 									userId, questionNumber, answer);
 
 					if (userAndScoreAndAnswer == null
 							|| userAndScoreAndAnswer.getUserId() == null) {
 						writeResponse(e, UNAUTHORIZED);
 						getLogger().info("Invalid userId {}", userId);
 					} else {
 						
 						if (!cachedUser.setLastAnswerdQuestion(questionNumber)) {
 							writeResponse(e, BAD_REQUEST);
 							getLogger().info("User {} has already answered the question {} ", userId, questionNumber);
 							return;
 						}
 						
 						cachedUser.setScore(userAndScoreAndAnswer.getScore());
 						
 						final StringBuilder sb = new StringBuilder(
								"{\"are_u_right\":\"");
 						if (userAndScoreAndAnswer.isAnswer()) {
 							sb.append("true");
 						} else {
 							sb.append("false");
 						}
 
 						sb.append("\",\"good_answer\":\"");
 						sb.append(question.getChoices().get(
 								question.getCorrectChoice() - 1));
 						sb.append("\",\"score\":\"");
 						sb.append(userAndScoreAndAnswer.getScore());
 						sb.append("\"}");
 
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
