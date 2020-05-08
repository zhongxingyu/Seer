 package de.hsrm.mi.mobcomp.y2k11grp04.service;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import android.app.Service;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.Answer;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.AnswerAverage;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.BaseModel;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.Choice;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.Comment;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.QuestionOption;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;
 
 public class MoodServerService extends Service {
 
 	public static final int MSG_PAUSE = 1;
 	public static final int MSG_PAUSE_RESULT = 2;
 	public static final int MSG_RESUME = 3;
 	public static final int MSG_RESUME_RESULT = 4;
 	public static final int MSG_MEETING = 5;
 	public static final int MSG_MEETING_RESULT = 6;
 	public static final int MSG_MEETING_SUBSCRIBE = 7;
 	public static final int MSG_MEETING_UNSUBSCRIBE = 8;
 	public static final int MSG_MEETING_COMPLETE = 9;
 	public static final int MSG_MEETING_COMPLETE_RESULT = 10;
 	public static final int MSG_MEETING_COMPLETE_PROGRESS = 11;
 	public static final int MSG_ANSWER = 12;
 	public static final int MSG_ANSWER_RESULT = 13;
 	public static final int MSG_TOPIC_IMAGE_RESULT = 14;
 	public static final int MSG_TOPIC_COMMENTS = 15;
 	public static final int MSG_TOPIC_COMMENTS_RESULT = 16;
 	public static final int MSG_TOPIC_COMMENT = 17;
 	public static final int MSG_MEETING_UPDATE = 18;
 	public static final int MSG_MEETING_UPDATE_RESULT = 19;
 	public static final int MSG_FOTOVOTE_CREATE = 20;
 	public static final int MSG_FOTOVOTE_CREATE_TOPIC = 21;
 	public static final int MSG_FOTOVOTE_CREATE_TOPIC_RESULT = 22;
 	public static final int MSG_ERROR = 99;
 
 	public static final String KEY_API_URI = "api.uri";
 	public static final String KEY_MEETING_MODEL = "model.Meeting";
 	public static final String KEY_MEETING_ID = "meeting.id";
 	public static final String KEY_MEETING_URI = "meeting.uri";
 	public static final String KEY_MEETING_NAME = "meeting.name";
 	public static final String KEY_ERROR_MESSAGE = "error.message";
 	public static final String KEY_ANSWER = "answer.answer";
 	public static final String KEY_TOPIC_ID = "topic.id";
 	public static final String KEY_TOPIC_IMAGE = "topic.image";
 	public static final String KEY_TOPIC_URI = "topic.uri";
 	public static final String KEY_QUESTION_URI = "question.uri";
 	public static final String KEY_COMMENT_COMMENT = "comment.comment";
 
 	public static final String KEY_TOPIC_MODEL = "model.Topic";
 	public static final String KEY_QUESTION_MODEL = "model.Question";
 	public static final String KEY_COMMENT_MODEL = "model.Comment";
 	public static final String KEY_ANSWER_MODEL = "model.Answer";
 
 	private final Messenger messenger = new Messenger(new IncomingHandler());
 	private Timer timer;
 	private Map<Messenger, Uri> meetingSubscription = new HashMap<Messenger, Uri>();
 	// Merkt sich immer den letzten Request eines Typs eines Messengers um bei
 	// langsamer Verbindung keine unnötigen Requests zu erzeugen
 	private ConcurrentLinkedQueue<LatestTask> serviceRequest = new ConcurrentLinkedQueue<LatestTask>();
 	private Object serviceRequestLock = new Object();
 
 	private ExecutorService pool = Executors.newFixedThreadPool(1,
 			Executors.defaultThreadFactory());
 
 	private Queue<Topic> missingImages = new LinkedList<Topic>();
 
 	/**
 	 * Wie oft das Meeting aktualisiert wird (ms)
 	 */
 	private int updateRate = 10000;
 	private MoodServerApi api;
 	private ResponseRunner runner;
 
 	private class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message request) {
 
 			// Aktionen ohne Antwort
 			switch (request.what) {
 			case MSG_MEETING_SUBSCRIBE:
 				meetingSubscription
 						.put(request.replyTo,
 								Uri.parse(request.getData().getString(
 										KEY_MEETING_URI)));
 				break;
 			case MSG_MEETING_UNSUBSCRIBE:
 				meetingSubscription.remove(request.replyTo);
 				break;
 			case MSG_PAUSE:
 				stopTimer();
 				break;
 			case MSG_RESUME:
 				startTimer();
 				break;
 			default:
 				super.handleMessage(request);
 			}
 
 			// Aktionen die eine Antwort erzeugen
 			// Nur letzten Request eines Messengers verwenden um bei langsamer
 			// Verbindung keine unnötigen Requests zu erzeugen
 			LatestTask t = new LatestTask(request);
 			if (serviceRequest.remove(t)) {
 				Log.w(getClass().getCanonicalName(),
 						"Request for " + t.getWhat()
 								+ " already queued. Replaced.");
 			}
 			serviceRequest.offer(t);
 
 			synchronized (serviceRequestLock) {
 				serviceRequestLock.notifyAll();
 			}
 		}
 	}
 
 	/**
 	 * Lädt ein Meeting
 	 * 
 	 * @param request
 	 * @throws ApiException
 	 */
 	public void fetchMeeting(Message request) throws ApiException {
 		Meeting meeting = api.getMeeting(Uri.parse(request.getData().getString(
 				KEY_MEETING_URI)));
 		sendMeetingTo(meeting, request.replyTo, MSG_MEETING_RESULT);
 	}
 
 	/**
 	 * Legt ein Foto-Topic zu einem Meeting an
 	 * 
 	 * @param request
 	 * @throws ApiException
 	 */
 	public void createFotoVoteTopic(Message request) throws ApiException {
 		Meeting meeting = api.getMeeting(Uri.parse(request.getData().getString(
 				KEY_MEETING_URI)));
 		File image = new File(request.getData().getString(KEY_TOPIC_IMAGE));
 		api.createTopicFotoVote(meeting, image);
 		sendMsg(request.replyTo,
 				Message.obtain(null, MSG_FOTOVOTE_CREATE_TOPIC_RESULT));
 		sendMeetingTo(getUpdateMeeting(meeting.getUri()), request.replyTo,
 				MSG_MEETING_UPDATE_RESULT);
 		fetchMissingImages(request.replyTo);
 	}
 
 	/**
 	 * Legt ein Meeting vom Typ FotoVote an
 	 * 
 	 * @param request
 	 * @throws ApiException
 	 */
 	public void createFotoVoteMeeting(Message request) throws ApiException {
 
 		String meetingName = request.getData().getString(KEY_MEETING_NAME);
 		Uri apiUri = Uri.parse(request.getData().getString(KEY_API_URI));
 		File image = new File(request.getData().getString(KEY_TOPIC_IMAGE));
 
 		Meeting meeting = api.createMeetingFotoVote(apiUri, meetingName, image);
 		sendMeetingTo(meeting, request.replyTo, MSG_MEETING_RESULT);
 	}
 
 	/**
 	 * Lädt die Commentare eines Topics
 	 * 
 	 * @param request
 	 * @throws ApiException
 	 */
 	public void fetchTopicComments(Message request) throws ApiException {
 		Topic topic = api.getTopic(Uri.parse(request.getData().getString(
 				KEY_TOPIC_URI)));
 		fetchTopicComments(request, topic);
 	}
 
 	private void fetchTopicComments(Message request, Topic topic)
 			throws ApiException {
 		ArrayList<Comment> comments = api.getComments(topic);
 		Bundle b = new Bundle();
 		b.putString(MoodServerService.KEY_TOPIC_URI, topic.getUri().toString());
 		b.putParcelableArrayList(MoodServerService.KEY_COMMENT_MODEL, comments);
 		Message info = Message.obtain(null, MSG_TOPIC_COMMENTS_RESULT);
 		info.setData(b);
 		sendMsg(request.replyTo, info);
 	}
 
 	/**
 	 * Erzeugt ein neues Kommentar zu einem Topic
 	 * 
 	 * @param request
 	 * @throws ApiException
 	 */
 	public void createComment(Message request) throws ApiException {
 		Topic topic = api.getTopic(Uri.parse(request.getData().getString(
 				KEY_TOPIC_URI)));
 		api.addComment(topic, request.getData().getString(KEY_COMMENT_COMMENT));
 		// Also Antwort Liste der Kommentare schicken
 		fetchTopicComments(request, topic);
 	}
 
 	/**
 	 * Erzeugt eine neue Antwort zu einer Frage
 	 * 
 	 * @param request
 	 * @throws ApiException
 	 */
 	private void createAnswer(Message request) throws ApiException {
 		Question question = api.getQuestion(Uri.parse(request.getData()
 				.getString(KEY_QUESTION_URI)));
 
 		Bundle b = new Bundle();
 		if (question.isChoiceType() && question.getMaxChoices() > 1) {
 			ArrayList<Answer> answers = api.addAnswers(question, request
 					.getData().getStringArray(KEY_ANSWER));
 			b.putParcelableArrayList(MoodServerService.KEY_ANSWER_MODEL,
 					answers);
 		} else {
 			Answer answer = api.addAnswer(question, request.getData()
 					.getString(KEY_ANSWER));
 			b.putParcelable(MoodServerService.KEY_ANSWER_MODEL, answer);
 		}
 		Message info = Message.obtain(null, MSG_ANSWER_RESULT);
 		info.setData(b);
 		sendMsg(request.replyTo, info);
 
 	}
 
 	/**
 	 * @param request
 	 * @throws ApiException
 	 */
 	public void fetchMeetingComplete(Message request) throws ApiException {
 		Meeting meeting = api.getMeetingRecursive(Uri.parse(request.getData()
 				.getString(KEY_MEETING_URI)));
 
 		for (Topic topic : meeting.getTopics()) {
 			if (topic.getImage() == null)
 				continue;
 			File imageFile = getTopicImageFile(topic);
 			if (!imageFile.exists()) {
 				missingImages.add(topic);
 			} else {
 				topic.setImageFile(imageFile);
 			}
 		}
 		
 		sendMeetingTo(meeting, request.replyTo, MSG_MEETING_COMPLETE_RESULT);
 		
 		fetchMissingImages(request.replyTo);
 	}
 
 	private void fetchMissingImages(Messenger rcpt, int startProgress,
 			int maxProgress) throws ApiException {
 		int p = 0;
 		while (!missingImages.isEmpty()) {
 			sendMeetingProgress(rcpt, startProgress + p, maxProgress);
 			Topic topic = missingImages.poll();
 			fetchTopicImage(rcpt, topic);
 			p++;
 		}
 		sendMeetingProgress(rcpt, startProgress + p, maxProgress);
 	}
 
 	private void fetchMissingImages(Messenger rcpt) throws ApiException {
 		fetchMissingImages(rcpt, 0, missingImages.size());
 	}
 
 	private File getTopicImageFile(Topic topic) {
 		return new File(Environment.getExternalStorageDirectory()
 				.getAbsolutePath()
 				+ "/Android/data/de.hsrm.mi.mobcomp.y2k11grp04/cache/topic-"
				+ topic.getId() + ".png");
 	}
 
 	/**
 	 * Lädt das Bild eines Meetings
 	 * 
 	 * @throws ApiException
 	 */
 	private void fetchTopicImage(Messenger rcpt, Topic topic)
 			throws ApiException {
 
 		File imageFile = getTopicImageFile(topic);
 
 		// Bilder werden letzendlich blockierend geladen,
 		// um die Verbindung zu schonen
 		// Könnte man auch ohne Future lösen ...
 		Future<File> imageLoader = pool.submit(new HttpToFileLoader(topic
 				.getImage(), imageFile));
 		try {
 			imageLoader.get();
 		} catch (Exception e) {
 			throw new ApiException(e.getMessage());
 		}
 
 		Message info = Message.obtain(null, MSG_TOPIC_IMAGE_RESULT);
 		Bundle data = new Bundle();
 		data.putInt(KEY_TOPIC_ID, topic.getId());
 		data.putString(KEY_TOPIC_IMAGE, imageFile.toString());
 		info.setData(data);
 		sendMsg(rcpt, info);
 	}
 
 	/**
 	 * Sendet Infos mit dem Fortschritt des Ladens eines Meetings an die
 	 * Activity, die es angefordert hat
 	 * 
 	 * @param replyTo
 	 * @param progress
 	 * @param max
 	 */
 	private void sendMeetingProgress(Messenger replyTo, int progress, int max) {
 		Message info = Message.obtain(null, MSG_MEETING_COMPLETE_PROGRESS);
 		info.arg1 = progress;
 		info.arg2 = max;
 		sendMsg(replyTo, info);
 	}
 
 	/**
 	 * Sendet das meeting and den Empfänger
 	 * 
 	 * @param meeting
 	 * @param rcpt
 	 */
 	private void sendMeetingTo(BaseModel meeting, Messenger rcpt, int type) {
 		Message info = Message.obtain(null, type);
 		Bundle data = new Bundle();
 		data.putParcelable(KEY_MEETING_MODEL, meeting);
 		info.setData(data);
 		sendMsg(rcpt, info);
 	}
 
 	@Override
 	public void onCreate() {
 		api = new MoodServerApi();
 		api.registerModel(Meeting.class,
 				Uri.parse("http://groupmood.net/jsonld/meeting"));
 		api.registerModel(Topic.class,
 				Uri.parse("http://groupmood.net/jsonld/topic"));
 		api.registerModel(Question.class,
 				Uri.parse("http://groupmood.net/jsonld/question"));
 		api.registerModel(QuestionOption.class,
 				Uri.parse("http://groupmood.net/jsonld/questionoption"));
 		api.registerModel(Choice.class,
 				Uri.parse("http://groupmood.net/jsonld/choice"));
 		api.registerModel(Comment.class,
 				Uri.parse("http://groupmood.net/jsonld/comment"));
 		api.registerModel(Answer.class,
 				Uri.parse("http://groupmood.net/jsonld/answer"));
 		api.registerModel(AnswerAverage.class,
 				Uri.parse("http://groupmood.net/jsonld/answeraverage"));
 		startTimer();
 
 		runner = new ResponseRunner();
 		new Thread(runner).start();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return messenger.getBinder();
 	}
 
 	public void onDestroy() {
 		stopTimer();
 		runner.running = false;
 		synchronized (serviceRequest) {
 			serviceRequest.clear();
 		}
 		synchronized (serviceRequestLock) {
 			serviceRequestLock.notifyAll();
 		}
 	}
 
 	/**
 	 * Startet den Timer
 	 */
 	private boolean startTimer() {
 		if (timer != null)
 			return false;
 		timer = new Timer(true);
 		timer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				// Alle registrierten Meeting-Watcher benachrichtigen
 				for (Messenger messenger : meetingSubscription.keySet()) {
 					Meeting updatedMeeting;
 					try {
 						updatedMeeting = getUpdateMeeting(meetingSubscription
 								.get(messenger));
 
 						if (updatedMeeting != null) {
 							sendMeetingTo(updatedMeeting, messenger,
 									MSG_MEETING_UPDATE_RESULT);
 							fetchMissingImages(messenger);
 						}
 					} catch (ApiException e) {
 						sendError(messenger, e.getMessage());
 					}
 				}
 
 			}
 		}, updateRate, updateRate);
 		return true;
 	}
 
 	private Meeting getUpdateMeeting(Uri meetingUri) throws ApiException {
 		Meeting meeting = api.getMeetingRecursive(meetingUri);
 
 		// Bilder der Topics prüfen
 		for (Topic topic : meeting.getTopics()) {
 			if (topic.getImage() != null) {
 				File imageFile = getTopicImageFile(topic);
 				if (imageFile.exists()) {
 					topic.setImageFile(imageFile);
 				} else {
 					missingImages.add(topic);
 				}
 			}
 		}
 
 		return meeting;
 	}
 
 	/**
 	 * Hält den Timer an
 	 */
 	private boolean stopTimer() {
 		if (timer == null)
 			return false;
 		timer.cancel();
 		timer = null;
 		return true;
 	}
 
 	protected void sendMsg(Messenger rcpt, Message response) {
 		if (rcpt == null)
 			return;
 		try {
 			rcpt.send(response);
 		} catch (RemoteException e) {
 			Log.e(getClass().getCanonicalName(), "Failed to send message.");
 		}
 	}
 
 	private void sendError(Messenger rcpt, String message) {
 		Log.e(getClass().getCanonicalName(), message);
 		Message errorMsg = Message.obtain(null, MSG_ERROR);
 		Bundle data = new Bundle();
 		data.putString(KEY_ERROR_MESSAGE, message);
 		errorMsg.setData(data);
 		sendMsg(rcpt, errorMsg);
 	}
 
 	private class ResponseRunner implements Runnable {
 		public boolean running = true;
 
 		@Override
 		public void run() {
 			while (running) {
 				if (!serviceRequest.isEmpty()) {
 					// Task nur holen, wird erst entfernt, wenn wir wirklich
 					// fertig sind.
 					LatestTask t = serviceRequest.peek();
 					if (t != null) {
 						Message request = new Message();
 						request.what = t.getWhat();
 						request.setData(t.getData());
 						request.replyTo = t.getReplyTo();
 						try {
 							switch (request.what) {
 							case MSG_MEETING:
 								fetchMeeting(request);
 								break;
 							case MSG_TOPIC_COMMENTS:
 								fetchTopicComments(request);
 								break;
 							case MSG_TOPIC_COMMENT:
 								createComment(request);
 								break;
 							case MSG_MEETING_COMPLETE:
 							case MSG_MEETING_UPDATE:
 								fetchMeetingComplete(request);
 								break;
 							case MSG_FOTOVOTE_CREATE:
 								createFotoVoteMeeting(request);
 								break;
 							case MSG_FOTOVOTE_CREATE_TOPIC:
 								createFotoVoteTopic(request);
 								break;
 							case MSG_ANSWER:
 								createAnswer(request);
 								break;
 							}
 						} catch (ApiException e) {
 							sendError(request.replyTo, e.getMessage());
 						}
 						// Task aus Queue entfernen
 						serviceRequest.remove(t);
 					}
 				} else {
 					try {
 						synchronized (serviceRequestLock) {
 							serviceRequestLock.wait();
 						}
 					} catch (InterruptedException e) {
 						Log.e(getClass().getCanonicalName(), e.getMessage());
 					}
 				}
 			}
 		}
 	}
 }
