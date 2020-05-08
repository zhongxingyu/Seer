 package de.kile.zapfmaster2000.rest.impl.core.push;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonProcessingException;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.jboss.resteasy.spi.AsynchronousResponse;
 
 import com.google.gson.Gson;
 
 import de.kile.zapfmaster2000.rest.api.news.AbstractNewsResponse;
 import de.kile.zapfmaster2000.rest.api.push.DrawDraftKitResponse;
 import de.kile.zapfmaster2000.rest.api.push.LoginDraftKitResponse;
 import de.kile.zapfmaster2000.rest.api.push.LogoutDraftKitResponse;
 import de.kile.zapfmaster2000.rest.core.Zapfmaster2000Core;
 import de.kile.zapfmaster2000.rest.core.box.BoxService;
 import de.kile.zapfmaster2000.rest.core.box.BoxServiceListener;
 import de.kile.zapfmaster2000.rest.core.box.LoginFailureReason;
 import de.kile.zapfmaster2000.rest.core.challenge.ChallengeService;
 import de.kile.zapfmaster2000.rest.core.challenge.ChallengeServiceListener;
 import de.kile.zapfmaster2000.rest.core.news.NewsService;
 import de.kile.zapfmaster2000.rest.core.news.NewsServiceListener;
 import de.kile.zapfmaster2000.rest.core.push.PushService;
 import de.kile.zapfmaster2000.rest.core.util.NewsAdapter;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Box;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Challenge;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Challenge1v1;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Drawing;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.News;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.User;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Zapfmaster2000Package;
 
 public class PushServiceImpl implements PushService {
 
 	private static final Logger LOGGER = Logger
 			.getLogger(PushServiceImpl.class);
 
 	/** pending responses for news pushs: AccountId -> PushQueue */
 	private final Map<Long, PushQueue> pendingNewsResponses = new HashMap<>();
 
 	/** pending responses for draft kit pushs: BoxId -> PushQueue */
 	private final Map<Long, PushQueue> pendingDraftKitReponses = new HashMap<>();
 
 	/** pending reponses for challenge pushs: UserId -> PushQueue */
 	private final Map<Long, PushQueue> pendingChallengeResponses = new HashMap<>();
 
 	/** pending responses for unknown rfid tag pushes: BoxId -> PushQueue */
 	private final Map<Long, PushQueue> pendingUnkownRfidResponses = new HashMap<>();
 
 	public PushServiceImpl(NewsService pNewsService, BoxService pBoxService,
 			ChallengeService pChallengeService) {
 		pNewsService.addListener(createNewsListener());
 		pBoxService.addListener(createBoxServiceListener());
 		pChallengeService.addListener(createChallengeServiceListener());
 	}
 
 	@Override
 	public void addNewsRequest(AsynchronousResponse pResponse,
 			Account pAccount, String pToken) {
 		if (!pendingNewsResponses.containsKey(pAccount.getId())) {
 			PushQueue queue = new PushQueue();
 			pendingNewsResponses.put(pAccount.getId(), queue);
 		}
 		PushQueue queue = pendingNewsResponses.get(pAccount.getId());
 		assert (queue != null);
 		queue.addRequest(pResponse);
 	}
 
 	@Override
 	public void addDraftkitRequest(AsynchronousResponse pResponse, Box pBox,
 			String pToken) {
 		if (!pendingDraftKitReponses.containsKey(pBox.getId())) {
 			PushQueue queue = new PushQueue();
 			pendingDraftKitReponses.put(pBox.getId(), queue);
 		}
 		PushQueue queue = pendingDraftKitReponses.get(pBox.getId());
 		assert (queue != null);
 		queue.addRequest(pResponse);
 	}
 
 	@Override
 	public void addChallengeRequest(AsynchronousResponse pResponse, User pUser) {
 		if (!pendingChallengeResponses.containsKey(pUser.getId())) {
 			PushQueue queue = new PushQueue();
 			pendingChallengeResponses.put(pUser.getId(), queue);
 		}
 		PushQueue queue = pendingChallengeResponses.get(pUser.getId());
 		assert (queue != null);
 		queue.addRequest(pResponse);
 	}
 
 	@Override
 	public void addUnkownRfidRequest(AsynchronousResponse pResponse, Box pBox) {
 		if (!pendingUnkownRfidResponses.containsKey(pBox.getId())) {
 			PushQueue queue = new PushQueue();
 			pendingUnkownRfidResponses.put(pBox.getId(), queue);
 		}
 		PushQueue queue = pendingUnkownRfidResponses.get(pBox.getId());
 		assert (queue != null);
 		queue.addRequest(pResponse);
 	}
 
 	private NewsServiceListener createNewsListener() {
 		return new NewsServiceListener() {
 
 			@Override
 			public void onNewsPosted(News pNews) {
 				pushNews(pNews);
 			}
 		};
 	}
 
 	private BoxServiceListener createBoxServiceListener() {
 		return new BoxServiceListener() {
 
 			@Override
 			public void onLogout(Box pBox, User pUser) {
 				pushLogout(pBox);
 
 			}
 
 			@Override
 			public void onLoginsuccessful(Box pBox, User pUser) {
 				pushLogin(pBox, pUser);
 
 			}
 
 			@Override
 			public void onEndDrawing(Box pBox, Drawing pDrawing) {
 			}
 
 			@Override
 			public void onDrawing(Box pBox, User pUser, double pAmount) {
 				pushDrawing(pBox, pUser, pAmount);
 			}
 
 			@Override
 			public void onLoginFailed(Box pBox, LoginFailureReason pReason,
 					long pTag) {
 				if (pReason == LoginFailureReason.INVALID_RFID_TAG) {
 					pushInvalidRfidTag(pBox, pTag);
 				}
 			}
 
 		};
 	}
 
 	private ChallengeServiceListener createChallengeServiceListener() {
 		return new ChallengeServiceListener() {
 
 			@Override
 			public void onPendingChallengeCreated(Challenge pChallenge) {
 				pushPendingChallenge(pChallenge);
 			}
 
 			@Override
 			public void onChallengeStarted(Challenge pChallenge) {
 				pushChallengeStarted(pChallenge);
 			}
 
 			@Override
 			public void onChallengeFinished(Challenge pChallenge) {
 			}
 
 			@Override
 			public void onChallengeDeclined(Challenge pChallenge) {
 				pushChallengeDeclined(pChallenge);
 			}
 		};
 	}
 
 	private void pushNews(News pNews) {
 		Account account = pNews.getAccount();
 		if (pendingNewsResponses.containsKey(account.getId())) {
 			AbstractNewsResponse news = new NewsAdapter().adapt(pNews);
 			Response response = Response.ok(news)
 					.type(MediaType.APPLICATION_JSON).build();
 
 			PushQueue queue = pendingNewsResponses.get(account.getId());
 			queue.push(response, true);
 		}
 	}
 
 	private void pushLogin(Box pBox, User pUser) {
 		if (pendingDraftKitReponses.containsKey(pBox.getId())) {
 			LoginDraftKitResponse entity = new LoginDraftKitResponse();
 			entity.setBoxId(pBox.getId());
 			entity.setImagePath(pUser.getImagePath());
 			entity.setUserId(pUser.getId());
 			entity.setUserName(pUser.getName());
 
 			Response response = Response.ok(entity)
 					.type(MediaType.APPLICATION_JSON).build();
 
 			PushQueue queue = pendingDraftKitReponses.get(pBox.getId());
 			queue.push(response, true);
 		}
 	}
 
 	private void pushDrawing(Box pBox, User pUser, double pAmount) {
 		if (pendingDraftKitReponses.containsKey(pBox.getId())) {
 			DrawDraftKitResponse entity = new DrawDraftKitResponse();
 			entity.setBoxId(pBox.getId());
 			entity.setImagePath(pUser.getImagePath());
 			entity.setUserId(pUser.getId());
 			entity.setUserName(pUser.getName());
 			entity.setAmount(pAmount);
 
 			Response response = Response.ok(entity)
 					.type(MediaType.APPLICATION_JSON).build();
 
 			PushQueue queue = pendingDraftKitReponses.get(pBox.getId());
 			queue.push(response, false);
 		}
 	}
 
 	private void pushLogout(Box pBox) {
 		if (pendingDraftKitReponses.containsKey(pBox.getId())) {
 			LogoutDraftKitResponse entity = new LogoutDraftKitResponse();
 			entity.setBoxId(pBox.getId());
 			Response response = Response.ok(entity)
 					.type(MediaType.APPLICATION_JSON).build();
 
 			PushQueue queue = pendingDraftKitReponses.get(pBox.getId());
 			queue.push(response, true);
 		}
 	}
 
 	private void pushPendingChallenge(Challenge pChallenge) {
 		if (pChallenge instanceof Challenge1v1) {
 			Challenge1v1 challenge1v1 = (Challenge1v1) pChallenge;
 			User challengee = challenge1v1.getUser2();
 
 			ChallengeRequestResponse entity = new ChallengeRequestResponse();
 			User challenger = challenge1v1.getUser1();
 			entity.setChallengerUserName(challenger.getName());
 			entity.setChallengerImagePath(challenger.getImagePath());
 			entity.setChallengerUserId(challenger.getId());
 			entity.setChallengeId(challenge1v1.getId());
 
 			// send via reverse ajax
 			if (pendingChallengeResponses.containsKey(challengee.getId())) {
 				PushQueue queue = pendingChallengeResponses.get(challengee
 						.getId());
 				Response response = Response.ok(entity).build();
 				queue.push(response, true);
 			}
 
 			// send via gcm
 			sendViaGcm(entity, challengee.getId());
 		}
 	}
 
 	private void sendViaGcm(Object entity, Long... userIds) {
 
 		// collect the gcm tokens we want to send the entity to
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 
 		Set<String> gcmTokens = new HashSet<>();
 
 		for (Long userId : userIds) {
 			@SuppressWarnings("unchecked")
 			List<String> result = session
 					.createQuery(
 							"SELECT t.googleCloudMessagingToken From Token t "
 									+ "WHERE t.user IS NOT NULL AND t.user.id = :userId "
 									+ "AND t.googleCloudMessagingToken IS NOT NULL")
 					.setLong("userId", userId).list();
 			gcmTokens.addAll(result);
 		}
 
 		tx.commit();
 
 		if (!gcmTokens.isEmpty()) {
 
 			HttpClient httpclient = new HttpClient();
 			PostMethod post = new PostMethod(
 					"https://android.googleapis.com/gcm/send");
 
 			// convert entity to json
 			ByteArrayOutputStream os = new ByteArrayOutputStream();
 			JsonFactory factory = new JsonFactory();
 
 			try {
 				JsonGenerator jsonGenerator = factory.createJsonGenerator(os);
 
 				jsonGenerator.writeStartObject();
 
 				jsonGenerator.writeFieldName("registration_ids");
 				jsonGenerator.writeStartArray();
 				for (String gcmToken : gcmTokens) {
 					jsonGenerator.writeString(gcmToken);
 				}
 				jsonGenerator.writeEndArray();
 
 				jsonGenerator.writeFieldName("data");
				jsonGenerator.writeRaw(new Gson().toJson(entity));
 				jsonGenerator.writeEndObject();
                jsonGenerator.flush();
 
 				String json = os.toString();
				LOGGER.debug("Created json" + json);
 
 				post.setRequestHeader("Content-Length",
 						Integer.toString(json.length()));
 				post.setRequestHeader("Content-Type", "application/json");
 				post.setRequestHeader("Authorization",
 						"key=AIzaSyAzFGj3pOuMyhQq9udWWplVbWMQFKMuS1g");
 
 				post.setRequestEntity(new StringRequestEntity(json,
 						"application/json", null));
 
 				try {
 					httpclient.executeMethod(post);
					LOGGER.debug("gcm returned: " + post.getStatusLine());
 				} finally {
 					post.releaseConnection();
 				}
 
 			} catch (JsonGenerationException e) {
 				LOGGER.error("Could not generate json", e);
 			} catch (JsonProcessingException e) {
 				LOGGER.error("Could not generate json", e);
 			} catch (IOException e) {
 				LOGGER.error("Could not generate json", e);
 			}
 
 		}
 
 	}
 
 	void pushChallengeStarted(Challenge pChallenge) {
 		if (pChallenge instanceof Challenge1v1) {
 			Challenge1v1 challenge1v1 = (Challenge1v1) pChallenge;
 
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 			pChallenge = (Challenge) session.load(
 					Zapfmaster2000Package.eINSTANCE.getChallenge().getName(),
 					pChallenge.getId());
 
 			ChallengeAcceptedReponse entity = new ChallengeAcceptedReponse();
 			entity.setUser1Id(challenge1v1.getUser1().getId());
 			entity.setUser1Name(challenge1v1.getUser1().getName());
 			entity.setUser2Id(challenge1v1.getUser2().getId());
 			entity.setUser2Name(challenge1v1.getUser2().getName());
 
 			tx.commit();
 
 			Response response = Response.ok(entity).build();
 
 			// send via revese ajax
 			if (pendingChallengeResponses.containsKey(challenge1v1.getUser1()
 					.getId())) {
 				PushQueue queue = pendingChallengeResponses.get(challenge1v1
 						.getUser1().getId());
 				queue.push(response, true);
 			}
 			if (pendingChallengeResponses.containsKey(challenge1v1.getUser2()
 					.getId())) {
 				PushQueue queue = pendingChallengeResponses.get(challenge1v1
 						.getUser2().getId());
 				queue.push(response, true);
 			}
 
 			// send via gcm
 			sendViaGcm(entity, challenge1v1.getUser1().getId(), challenge1v1
 					.getUser2().getId());
 		}
 	}
 
 	private void pushChallengeDeclined(Challenge pChallenge) {
 		if (pChallenge instanceof Challenge1v1) {
 			Challenge1v1 challenge1v1 = (Challenge1v1) pChallenge;
 
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 			pChallenge = (Challenge) session.load(
 					Zapfmaster2000Package.eINSTANCE.getChallenge().getName(),
 					pChallenge.getId());
 
 			ChallengeDeclinedReponse entity = new ChallengeDeclinedReponse();
 			entity.setUser1Id(challenge1v1.getUser1().getId());
 			entity.setUser1Name(challenge1v1.getUser1().getName());
 			entity.setUser2Id(challenge1v1.getUser2().getId());
 			entity.setUser2Name(challenge1v1.getUser2().getName());
 
 			tx.commit();
 
 			Response response = Response.ok(entity).build();
 
 			// send via reverse ajax
 			if (pendingChallengeResponses.containsKey(challenge1v1.getUser1()
 					.getId())) {
 				PushQueue queue = pendingChallengeResponses.get(challenge1v1
 						.getUser1().getId());
 				queue.push(response, true);
 			}
 			if (pendingChallengeResponses.containsKey(challenge1v1.getUser2()
 					.getId())) {
 				PushQueue queue = pendingChallengeResponses.get(challenge1v1
 						.getUser2().getId());
 				queue.push(response, true);
 			}
 
 			// send via gcm
 			sendViaGcm(entity, challenge1v1.getUser1().getId(), challenge1v1
 					.getUser2().getId());
 		}
 	}
 
 	private void pushInvalidRfidTag(Box pBox, long pTag) {
 		if (pendingUnkownRfidResponses.containsKey(pBox.getId())) {
 			PushQueue queue = pendingUnkownRfidResponses.get(pBox.getId());
 
 			Response response = Response.ok(pTag).build();
 			queue.push(response, false);
 		} // else nobody cares
 	}
 
 }
