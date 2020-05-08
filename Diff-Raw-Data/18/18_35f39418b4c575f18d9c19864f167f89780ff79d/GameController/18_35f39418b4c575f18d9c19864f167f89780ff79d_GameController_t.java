 package cl.own.usi.gateway.netty.controller;
 
 import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CREATED;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;
 
 import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.handler.codec.http.HttpMethod;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.util.CharsetUtil;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.Namespace;
 import org.jdom.input.JDOMParseException;
 import org.jdom.input.SAXBuilder;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import cl.own.usi.gateway.client.WorkerClient;
 import cl.own.usi.model.Game;
 import cl.own.usi.service.GameService;
 
 /**
  * Controller that create the {@link Game}
 *
  * @author bperroud
  * @author nicolas
 *
  */
 @Component
 public class GameController extends AbstractAuthenticateController {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(GameController.class);
 
 	@Autowired
 	private GameService gameService;
 
 	@Autowired
 	private WorkerClient workerClient;
 
 	private String validationFile;
 
 	@Value(value = "${frontend.validationFile:src/main/resources/gamesession.xsd}")
 	public void setXMLValidationFile(String validationFile) {
 		this.validationFile = validationFile;
 	}
 
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
 			throws Exception {
 
 		HttpRequest request = (HttpRequest) e.getMessage();
 
 		if (request.getMethod() == HttpMethod.POST) {
 
 			JSONObject object = (JSONObject) JSONValue.parse(request
 					.getContent().toString(CharsetUtil.UTF_8));
 
 			if (object == null) {
 				getLogger().error("No content or bad content");
 				writeResponse(e, BAD_REQUEST);
 				return;
 			}
 
 			String authenticationKey = (String) object
 					.get(AUTHENTIFICATION_KEY_NAME);
 
 			if (!isAuthenticationKeyValid(authenticationKey)) {
 				getLogger().error("Bad authentification key received: '{}'",
 						authenticationKey);
 				writeResponse(e, FORBIDDEN);
 				return;
 			}
 
 			String xmlParametersObject = (String) object.get("parameters");
 
 			Namespace namespace = Namespace.getNamespace("usi",
 					"http://www.usi.com");
 
 			SAXBuilder builder = new SAXBuilder();
 			builder.setValidation(true);
 			builder.setFeature(
 					"http://apache.org/xml/features/validation/schema", true);
 			builder.setProperty(
 					"http://apache.org/xml/properties/schema/external-schemaLocation",
 					"http://www.usi.com " + validationFile);
 
 			ByteArrayInputStream in = new ByteArrayInputStream(
 					StringEscapeUtils.unescapeHtml(xmlParametersObject)
 							.getBytes("UTF-8"));
 
 			List<Map<String, Map<String, Boolean>>> questions = new ArrayList<Map<String, Map<String, Boolean>>>();
 
 			try {
 
 				Document document = (Document) builder.build(in);
 
 				Element rootNode = document.getRootElement();
 				Element xmlQuestions = rootNode
 						.getChild("questions", namespace);
 
 				if (xmlQuestions == null) {
 					writeResponse(e, BAD_REQUEST);
 					return;
 				}
 
 				for (Object child : xmlQuestions.getChildren()) {
 
 					Map<String, Map<String, Boolean>> question1 = new HashMap<String, Map<String, Boolean>>();
 					Map<String, Boolean> answer1 = new LinkedHashMap<String, Boolean>();
 
 					Element question = (Element) child;
 					String goodChoiceStr = question
 							.getAttributeValue("goodchoice");
 					if (goodChoiceStr == null) {
 						writeResponse(e, BAD_REQUEST);
 						return;
 					}
 					int goodChoice = Integer.parseInt(goodChoiceStr);
 
 					Element label = question.getChild("label", namespace);
 					if (label == null) {
 						writeResponse(e, BAD_REQUEST);
 						return;
 					}
 
 					List<Element> choices = question.getChildren("choice",
 							namespace);
 
 					int i = 1;
 					boolean hasGoodchoice = false;
 					for (Element xmlChoice : choices) {
 						String choice = (String) xmlChoice.getValue();
 						boolean isGoodchoice = i == goodChoice;
 						answer1.put(choice, isGoodchoice ? Boolean.TRUE
 								: Boolean.FALSE);
 						if (isGoodchoice) {
 							hasGoodchoice = true;
 						}
 						i++;
 					}
 					if (!hasGoodchoice) {
 						writeResponse(e, BAD_REQUEST);
 						return;
 					}
 
 					question1.put(label.getValue(), answer1);
 					questions.add(question1);
 				}
 
 				Element parameters = rootNode.getChild("parameters", namespace);
 				if (parameters == null) {
 					writeResponse(e, BAD_REQUEST);
 					return;
 				}
 
 				String logintimeoutStr = parameters.getChildText(
 						"logintimeout", namespace);
 				Integer logintimeout = parseIntSafe(logintimeoutStr);
 				if (logintimeout == null) {
 					writeResponse(e, BAD_REQUEST);
 					return;
 				}
 
 				String synchrotimeStr = parameters.getChildText("synchrotime",
 						namespace);
 				Integer synchrotime = parseIntSafe(synchrotimeStr);
 				if (synchrotime == null) {
 					writeResponse(e, BAD_REQUEST);
 					return;
 				}
 
 				String nbusersthresholdStr = parameters.getChildText(
 						"nbusersthreshold", namespace);
 				Integer nbusersthreshold = parseIntSafe(nbusersthresholdStr);
 				if (nbusersthreshold == null) {
 					writeResponse(e, BAD_REQUEST);
 					return;
 				}
 
 				String questiontimeframeStr = parameters.getChildText(
 						"questiontimeframe", namespace);
 				Integer questiontimeframe = parseIntSafe(questiontimeframeStr);
 				if (questiontimeframe == null) {
 					writeResponse(e, BAD_REQUEST);
 					return;
 				}
 
 				String flushusertableStr = parameters.getChildText(
 						"flushusertable", namespace);
 				boolean flushusertable = Boolean
 						.parseBoolean(flushusertableStr);
 
 				if (questions.isEmpty() || questions.size() > 20) {
 					getLogger()
 							.error("No content or bad content for attribute numberOfQuestion");
 					writeResponse(e, BAD_REQUEST);
 					return;
 				}
 
				final boolean gameInserted = gameService.insertGame(
						nbusersthreshold, questiontimeframe, logintimeout,
						synchrotime, questions);

				if (!gameInserted) {
					getLogger()
							.error("Cannot re-create a game as previous one is not ended");
					writeResponse(e, BAD_REQUEST);
					return;
				}
 
 				if (flushusertable) {
 					long starttime = System.currentTimeMillis();
 					workerClient.flushUsers();
 					getCacheManager().flush();
 					getLogger().info("Flushed in {} ms",
 							(System.currentTimeMillis() - starttime));
 				}
 
 			} catch (JDOMParseException ex) {
 				LOGGER.warn("JDOMexception", ex);
 				writeResponse(e, BAD_REQUEST);
 				return;
 			} finally {
 				in.close();
 			}
 
 			writeResponse(e, CREATED);
 
 		} else {
 			writeResponse(e, NOT_IMPLEMENTED);
 		}
 
 	}
 
 	private static Integer parseIntSafe(String value) {
 		if (value == null) {
 			return null;
 		} else {
 			try {
 				return Integer.parseInt(value);
 			} catch (NumberFormatException e) {
 				return null;
 			}
 		}
 	}
 }
