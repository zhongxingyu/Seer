 package cl.own.usi.gateway.netty.controller;
 
 import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.handler.codec.http.HttpMethod;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.core.io.Resource;
 import org.springframework.stereotype.Component;
 
 import cl.own.usi.dao.GameDAO;
 import cl.own.usi.model.Game;
 import cl.own.usi.model.Question;
 
 /**
  * Controller that send asynchronously the {@link Question}
  * 
  * @author nicolas
  */
 @Component
 public class PlayController extends AbstractController {
 
 	public static final String URI_PLAY = "/play/";
 
 	@Value(value = "classpath:template/play.html")
 	private Resource playTemplate;
 
 	@Autowired
 	private GameDAO gameDAO;
 
 	final private static String NB_QUESTIONS = "%NB_QUESTIONS%";
 	final private static String QUESTION_TIMEOUT = "%QUESTION_TIMEOUT%";
	final private static String RANKING_WAITING_TIME = "%RANKING_WAITING_TIME%";
 
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
 			throws Exception {
 		HttpRequest request = (HttpRequest) e.getMessage();
 
 		if (request.getMethod() == HttpMethod.GET) {
 			String userId = getCookie(request, COOKIE_AUTH_NAME);
 
 			if (userId == null) {
 				writeResponse(e, UNAUTHORIZED);
 				getLogger().info("User not authorized");
 			} else {
 				Game game = gameDAO.getGame();
 
				if (game != null) {
					Map<String, String> mapping = new HashMap<String, String>();
					mapping.put(NB_QUESTIONS,
							String.valueOf(game.getNumberOfQuestion()));
					mapping.put(QUESTION_TIMEOUT,
							String.valueOf(game.getQuestionTimeLimit() * 1000));
					mapping.put(RANKING_WAITING_TIME, String.valueOf((game
							.getQuestionTimeLimit() + game
							.getSynchroTimeLimit()) * 1000));

					writeHtml(e, playTemplate, mapping);
				} else {
					writeResponse(e, BAD_REQUEST);
					getLogger().info("Wrong method");
				}
 
 			}
 		} else {
 			writeResponse(e, NOT_IMPLEMENTED);
 			getLogger().info("Wrong method");
 		}
 
 	}
 
 }
