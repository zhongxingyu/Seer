 package com.zerodes.exchangesync.tasksource.rtm;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.apache.commons.codec.binary.Hex;
 import org.apache.commons.lang.StringUtils;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Node;
 import org.dom4j.io.SAXReader;
 import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.zerodes.exchangesync.dto.NoteDto;
 import com.zerodes.exchangesync.dto.TaskDto;
 import com.zerodes.exchangesync.settings.Settings;
 import com.zerodes.exchangesync.tasksource.TaskSource;
 
 public class RtmTaskSourceImpl implements TaskSource {
 	private static final Logger LOG = LoggerFactory.getLogger(RtmTaskSourceImpl.class);
 	
 	private static final String RTM_API_KEY = "0bcf4c7e3182ec34f45321512e576300";
 	private static final String RTM_SHARED_SECRET = "fbf7a0bdb0011532";
 
 	private static final String REST_HOST = "api.rememberthemilk.com";
 	private static final String REST_AUTH_PATH = "/services/auth/";
 	private static final String REST_METHOD_PATH = "/services/rest/";
 
 	private static final String EXCHANGE_ID_NOTE_TITLE = "ExchangeID";
 	private static final String ORIGINAL_SUBJECT_NOTE_TITLE = "Original Email Subject";
 	
 	private static final String USER_SETTING_RTM_LIST_NAME = "rtmListName";
 	private static final String INTERNAL_SETTING_FROB = "frob";
 	private static final String INTERNAL_SETTING_AUTH_TOKEN = "authToken";
 
 	private Settings settings;
 	private String defaultRtmListId;
 
 	private enum RtmAuthStatus {
 		NEEDS_USER_APPROVAL,
 		NEEDS_AUTH_TOKEN,
 		AUTHORIZED
 	}
 
 	public RtmTaskSourceImpl(final Settings settings) throws RtmServerException {
 		this.settings = settings;
 		LOG.info("Connecting to Remember The Milk...");
 		switch (getAuthStatus()) {
 		case NEEDS_USER_APPROVAL:
 			throw new RuntimeException("Please go to the following URL to authorize application to sync with Remember The Milk: "
 					+ getAuthenticationUrl("write"));
 		case NEEDS_AUTH_TOKEN:
 			completeAuthentication();
 		}
 		this.defaultRtmListId = getIdForListName(settings.getUserSetting(USER_SETTING_RTM_LIST_NAME));
 	}
 	
 	@Override
 	public Collection<TaskDto> getAllTasks() throws Exception {
 		Collection<TaskDto> results = new ArrayList<TaskDto>();
 		results = getAllTasks(defaultRtmListId);
 		return results;
 	}
 
 	@Override
 	public void addTask(TaskDto task) throws Exception {
 		// Add email tag
 		task.addTag("email");
 
 		// Add ExchangeID note
 		NoteDto exchangeIdNote = new NoteDto();
 		exchangeIdNote.setTitle(EXCHANGE_ID_NOTE_TITLE);
 		exchangeIdNote.setBody(task.getExchangeId());
 		task.addNote(exchangeIdNote);
 
 		// Add Original Subject note
 		NoteDto originalSubjectNote = new NoteDto();
 		originalSubjectNote.setTitle(ORIGINAL_SUBJECT_NOTE_TITLE);
 		originalSubjectNote.setBody(task.getName());
 		task.addNote(originalSubjectNote);
 
		String timelineId = createTimeline();
 		addTask(timelineId, defaultRtmListId, task);
 		LOG.debug("Added RTM task " + task.getName());
 	}
 
 	@Override
 	public void updateDueDate(TaskDto task) throws Exception {
 		String timelineId = createTimeline();
 		updateDueDate(timelineId, defaultRtmListId, (RtmTaskDto) task);
 		LOG.debug("Updated RTM task due date for " + task.getName());
 	}
 
 	@Override
 	public void updateCompletedFlag(TaskDto task) throws Exception {
 		String timelineId = createTimeline();
 		updateCompleteFlag(timelineId, defaultRtmListId, (RtmTaskDto) task);
 		if (task.isCompleted()) {
 			LOG.debug("Marked RTM task as completed for " + task.getName());
 		} else {
 			LOG.debug("Marked RTM task as incomplete for " + task.getName());
 		}
 	}
 
 	private RtmAuthStatus getAuthStatus() throws RtmServerException {
 		if (StringUtils.isNotEmpty(settings.getInternalSetting(INTERNAL_SETTING_FROB))) {
 			return RtmAuthStatus.NEEDS_AUTH_TOKEN;
 		}
 		if (StringUtils.isEmpty(settings.getInternalSetting(INTERNAL_SETTING_AUTH_TOKEN)) || !checkToken()) {
 			return RtmAuthStatus.NEEDS_USER_APPROVAL;
 		}
 		return RtmAuthStatus.AUTHORIZED;
 	}
 
 	private URL getAuthenticationUrl(final String perms) throws RtmServerException {
 		try {
 			// Call getFrob
 			TreeMap<String, String> getFrobParams = new TreeMap<String, String>();
 			getFrobParams.put("method", "rtm.auth.getFrob");
 			Document response = parseXML(getRtmUri(REST_METHOD_PATH, getFrobParams));
 			Node node = response.selectSingleNode("/rsp/frob");
 			settings.setInternalSetting(INTERNAL_SETTING_FROB, node.getText());
 
 			// Generate url
 			TreeMap<String, String> params = new TreeMap<String, String>();
 			params.put("perms", perms);
 			params.put("frob", settings.getInternalSetting(INTERNAL_SETTING_FROB));
 			return getRtmUri(REST_AUTH_PATH, params).toURL();
 		} catch (MalformedURLException e) {
 			throw new RuntimeException("Unable to get authentication url", e);
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException("Unable to get authentication url", e);
 		}
 	}
 
 	public void completeAuthentication() throws RtmServerException {
 		if (settings.getInternalSetting(INTERNAL_SETTING_FROB) == null) {
 			throw new RuntimeException("Unable to complete authentication unless in NEEDS_AUTH_TOKEN status.");
 		}
 		try {
 			TreeMap<String, String> params = new TreeMap<String, String>();
 			params.put("method", "rtm.auth.getToken");
 			params.put("frob", settings.getInternalSetting(INTERNAL_SETTING_FROB));
 			Document response = parseXML(getRtmUri(REST_METHOD_PATH, params));
 			Node node = response.selectSingleNode("/rsp/auth/token");
 			settings.setInternalSetting(INTERNAL_SETTING_AUTH_TOKEN, node.getText());
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException("Unable to complete authentication", e);
 		} finally {
 			settings.setInternalSetting(INTERNAL_SETTING_FROB, null);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private String getIdForListName(final String listName) throws RtmServerException {
 		try {
 			Document response = parseXML(getRtmMethodUri("rtm.lists.getList"));
 			List<Node> listNodesList = response.selectNodes("/rsp/lists/list");
 			for (Node listNode : listNodesList) {
 				Node nameNode = listNode.selectSingleNode("@name");
 				Node idNode = listNode.selectSingleNode("@id");
 				if (nameNode.getText().equals(listName)) {
 					return idNode.getText();
 				}
 			}
 			throw new RuntimeException("Unable to find list named " + listName);
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException("Unable to retrieve list of lists", e);
 		}
 	}
 
 	/**
 	 * Create a new timeline.
 	 *
 	 * @return timeline id
 	 * @throws RtmServerException
 	 */
 	private String createTimeline() throws RtmServerException {
 		try {
 			Document response = parseXML(getRtmMethodUri("rtm.timelines.create"));
 			Node node = response.selectSingleNode("/rsp/timeline");
 			return node.getText();
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException("Unable to create timeline", e);
 		}
 	}
 
 	/**
 	 * Add a new task.
 	 * 
 	 * @param timelineId
 	 * @param listId
 	 * @param task
 	 * @throws RtmServerException
 	 */
 	private void addTask(final String timelineId, final String listId, final TaskDto task) throws RtmServerException {
 		try {
 			TreeMap<String, String> addTaskParams = new TreeMap<String, String>();
 			addTaskParams.put("timeline", timelineId);
 			addTaskParams.put("list_id", listId);
 			addTaskParams.put("name", task.getName());
 			Document response = parseXML(getRtmMethodUri("rtm.tasks.add", addTaskParams));
 			
 			RtmTaskDto rtmTask = new RtmTaskDto();
 			task.copyTo(rtmTask);
 			Node idNode = response.selectSingleNode("/rsp/list/taskseries/task/@id");
 			rtmTask.setRtmTaskId(idNode.getText());
 			Node taskSeriesIdNode = response.selectSingleNode("/rsp/list/taskseries/@id");
 			rtmTask.setRtmTimeSeriesId(taskSeriesIdNode.getText());
 			
 			// Set due date (if required)
 			if (rtmTask.getDueDate() != null) {
 				updateDueDate(timelineId, listId, rtmTask);
 			}
 			
 			// Set completed (if required)
 			if (rtmTask.isCompleted()) {
 				updateCompleteFlag(timelineId, listId, rtmTask);
 			}
 			
 			// Add tags (if required)
 			if (!rtmTask.getTags().isEmpty()) {
 				addTags(timelineId, listId, rtmTask, rtmTask.getTags());
 			}
 			
 			// Add notes (if required)
 			if (!rtmTask.getNotes().isEmpty()) {
 				for (final NoteDto note : rtmTask.getNotes()) {
 					addNote(timelineId, listId, rtmTask, note);
 				}
 			}
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException("Unable to add task", e);
 		}
 	}
 
 	/**
 	 * Update a task's due date.
 	 * 
 	 * @param timelineId
 	 * @param listId
 	 * @param task
 	 * @throws RtmServerException
 	 * @throws UnsupportedEncodingException
 	 */
 	private void updateDueDate(final String timelineId, final String listId, final RtmTaskDto task)
 			throws RtmServerException, UnsupportedEncodingException {
 		TreeMap<String, String> setDueDateParams = new TreeMap<String, String>();
 		setDueDateParams.put("task_id", task.getRtmTaskId());
 		setDueDateParams.put("taskseries_id", task.getRtmTimeSeriesId());
 		setDueDateParams.put("timeline", timelineId);
 		setDueDateParams.put("list_id", listId);
 		setDueDateParams.put("due", convertJodaDateTimeToString(task.getDueDate()));
 		parseXML(getRtmMethodUri("rtm.tasks.setDueDate", setDueDateParams));
 	}
 
 	/**
 	 * Update a task's url.
 	 * 
 	 * @param timelineId
 	 * @param listId
 	 * @param task
 	 * @throws RtmServerException
 	 * @throws UnsupportedEncodingException
 	 */
 	private void updateUrl(final String timelineId, final String listId, final RtmTaskDto task)
 			throws RtmServerException, UnsupportedEncodingException {
 		TreeMap<String, String> setUrlParams = new TreeMap<String, String>();
 		setUrlParams.put("task_id", task.getRtmTaskId());
 		setUrlParams.put("taskseries_id", task.getRtmTimeSeriesId());
 		setUrlParams.put("timeline", timelineId);
 		setUrlParams.put("list_id", listId);
 		setUrlParams.put("url", task.getUrl());
 		parseXML(getRtmMethodUri("rtm.tasks.setURL", setUrlParams));
 	}
 
 	/**
 	 * Add tags to a task.
 	 * 
 	 * @param timelineId
 	 * @param listId
 	 * @param task
 	 * @throws RtmServerException
 	 * @throws UnsupportedEncodingException
 	 */
 	private void addTags(final String timelineId, final String listId,
 			final RtmTaskDto task, final Set<String> tags) throws RtmServerException,
 			UnsupportedEncodingException {
 		TreeMap<String, String> addTagsParams = new TreeMap<String, String>();
 		addTagsParams.put("task_id", task.getRtmTaskId());
 		addTagsParams.put("taskseries_id", task.getRtmTimeSeriesId());
 		addTagsParams.put("timeline", timelineId);
 		addTagsParams.put("list_id", listId);
 		addTagsParams.put("tags", StringUtils.join(tags, ","));
 		parseXML(getRtmMethodUri("rtm.tasks.addTags", addTagsParams));
 	}
 
 	/**
 	 * Add a note to a task.
 	 * 
 	 * @param timelineId
 	 * @param listId
 	 * @param task
 	 * @throws RtmServerException
 	 * @throws UnsupportedEncodingException
 	 */
 	private void addNote(final String timelineId, final String listId,
 			final RtmTaskDto task, final NoteDto note) throws RtmServerException,
 			UnsupportedEncodingException {
 		TreeMap<String, String> addNoteParams = new TreeMap<String, String>();
 		addNoteParams.put("task_id", task.getRtmTaskId());
 		addNoteParams.put("taskseries_id", task.getRtmTimeSeriesId());
 		addNoteParams.put("timeline", timelineId);
 		addNoteParams.put("list_id", listId);
 		addNoteParams.put("note_title", note.getTitle());
 		addNoteParams.put("note_text", note.getBody());
 		parseXML(getRtmMethodUri("rtm.tasks.notes.add", addNoteParams));
 	}
 
 	/**
 	 * Mark a task as completed or incomplete.
 	 * 
 	 * @param timelineId
 	 * @param listId
 	 * @param task
 	 * @throws RtmServerException
 	 */
 	private void updateCompleteFlag(final String timelineId, final String listId, final RtmTaskDto task) throws RtmServerException {
 		try {
 			TreeMap<String, String> setCompletedParams = new TreeMap<String, String>();
 			setCompletedParams.put("task_id", task.getRtmTaskId());
 			setCompletedParams.put("taskseries_id", task.getRtmTimeSeriesId());
 			setCompletedParams.put("timeline", timelineId);
 			setCompletedParams.put("list_id", listId);
 			if (task.isCompleted()) {
 				parseXML(getRtmMethodUri("rtm.tasks.complete", setCompletedParams));
 			} else {
 				parseXML(getRtmMethodUri("rtm.tasks.uncomplete", setCompletedParams));
 			}
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException("Unable to add task", e);
 		}
 	}
 
 	/**
 	 * Retrieve a list of tasks in a specific list.
 	 *
 	 * @param listId the list from which to retrieve tasks
 	 * @return a list of tasks
 	 * @throws RtmServerException
 	 */
 	@SuppressWarnings("unchecked")
 	private List<TaskDto> getAllTasks(final String listId) throws RtmServerException {
 		try {
 			List<TaskDto> results = new ArrayList<TaskDto>();
 			TreeMap<String, String> params = new TreeMap<String, String>();
 			params.put("list_id", listId);
 			Document response = parseXML(getRtmMethodUri("rtm.tasks.getList", params));
 			List<Node> taskSeriesNodesList = response.selectNodes("/rsp/tasks/list/taskseries");
 			for (Node taskSeriesNode : taskSeriesNodesList) {
 				Node timeSeriesIdNode = taskSeriesNode.selectSingleNode("@id");
 				Node lastModifiedNode = taskSeriesNode.selectSingleNode("@modified");
 				Node nameNode = taskSeriesNode.selectSingleNode("@name");
 				Node idNode = taskSeriesNode.selectSingleNode("task/@id");
 				Node dueNode = taskSeriesNode.selectSingleNode("task/@due");
 				Node completedNode = taskSeriesNode.selectSingleNode("task/@completed");
 				RtmTaskDto rtmTask = new RtmTaskDto();
 				rtmTask.setRtmTaskId(idNode.getText());
 				rtmTask.setRtmTimeSeriesId(timeSeriesIdNode.getText());
 				rtmTask.setLastModified(convertStringToJodaDateTime(lastModifiedNode.getText()));
 				rtmTask.setName(nameNode.getText());
 				rtmTask.setDueDate(convertStringToJodaDateTime(dueNode.getText()));
 				rtmTask.setCompleted(StringUtils.isNotEmpty(completedNode.getText()));
 				List<Node> tagNodes = taskSeriesNode.selectNodes("tags/tag");
 				for (Node tagNode : tagNodes) {
 					rtmTask.addTag(tagNode.getText());
 				}
 				List<Node> noteNodes = taskSeriesNode.selectNodes("notes/note");
 				for (Node noteNode : noteNodes) {
 					NoteDto note = new NoteDto();
 					note.setTitle(noteNode.selectSingleNode("@title").getText());
 					note.setBody(noteNode.getText());
 					rtmTask.addNote(note);
 					if (note.getTitle().equals(EXCHANGE_ID_NOTE_TITLE)) {
 						rtmTask.setExchangeId(note.getBody());
 					}
 				}
 				results.add(rtmTask);
 			}
 			return results;
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException("Unable to add task", e);
 		}
 	}
 
 	private boolean checkToken() throws RtmServerException {
 		try {
 			Document response = parseXML(getRtmMethodUri("rtm.auth.checkToken"));
 			Node tokenNode = response.selectSingleNode("/rsp/auth/token");
 			Node usernameNode = response.selectSingleNode("/rsp/auth/user/@username");
 			LOG.info("Connected to Remember The Milk as " + usernameNode.getText());
 			if (tokenNode.getText().equals(settings.getInternalSetting(INTERNAL_SETTING_AUTH_TOKEN))) {
 				return true;
 			}
 			return false;
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException("Unable to check token", e);
 		}
 	}
 
 	private URI getRtmMethodUri(final String methodName) throws UnsupportedEncodingException {
 		return getRtmMethodUri(methodName, new TreeMap<String, String>());
 	}
 
 	private URI getRtmMethodUri(final String methodName, final TreeMap<String, String> params) throws UnsupportedEncodingException {
 		params.put("method", methodName);
 		params.put("auth_token", settings.getInternalSetting(INTERNAL_SETTING_AUTH_TOKEN));
 		return getRtmUri(REST_METHOD_PATH, params);
 	}
 
 	private URI getRtmUri(final String uriPath, final TreeMap<String, String> params) throws UnsupportedEncodingException {
 		params.put("api_key", RTM_API_KEY);
 		StringBuffer uriString = new StringBuffer("http://" + REST_HOST + uriPath + "?");
 		for (String key : params.keySet()) {
 			uriString.append(key).append("=").append(URLEncoder.encode(params.get(key), "UTF-8")).append("&");
 		}
 		uriString.append("api_sig").append("=").append(getApiSig(params));
 		return URI.create(uriString.toString());
 	}
 
 	private Document parseXML(final URI uri) throws RtmServerException {
 		SAXReader reader = new SAXReader();
 		Document response;
 		try {
 			response = reader.read(uri.toURL());
 			Node status = response.selectSingleNode("/rsp/@stat");
 			if (status != null) {
 				if (status.getText().equals("fail")) {
 					Node errCode = response.selectSingleNode("/rsp/err/@code");
 					Node errMessage = response.selectSingleNode("/rsp/err/@msg");
 					throw new RtmServerException(Integer.valueOf(errCode.getText()), errMessage.getText());
 				}
 			}
 			return response;
 		} catch (MalformedURLException e) {
 			throw new RuntimeException("A malformed URL was specified", e);
 		} catch (DocumentException e) {
 			throw new RuntimeException("There was a problem parsing the response from RTM", e);
 		}
 	}
 
 	private String getApiSig(final TreeMap<String, String> params) {
 		StringBuffer rawString = new StringBuffer(RTM_SHARED_SECRET);
 		for (String key : params.keySet()) {
 			rawString.append(key);
 			rawString.append(params.get(key));
 		}
 		try {
 			byte[] bytesOfMessage = rawString.toString().getBytes("UTF-8");
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			byte[] thedigest = md.digest(bytesOfMessage);
 			return new String(Hex.encodeHex(thedigest));
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException("Unable to create API signature", e);
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException("Unable to create API signature", e);
 		}
 	}
 	
 	private String convertJodaDateTimeToString(DateTime theDate) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		return fmt.print(theDate) + "T23:59:59Z";
 	}
 
 	private DateTime convertStringToJodaDateTime(String theDate) {
 		if (StringUtils.isEmpty(theDate)) {
 			return null;
 		}
 		DateTimeFormatter dateFormat = ISODateTimeFormat.dateTimeNoMillis();
 		return new DateTime(dateFormat.parseDateTime(theDate).toDate());
 	}
 }
