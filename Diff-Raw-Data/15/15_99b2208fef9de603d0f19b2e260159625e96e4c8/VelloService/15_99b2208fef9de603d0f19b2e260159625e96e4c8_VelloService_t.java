 package com.mili.xiaominglui.app.vello.service;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ContentProviderOperation;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.OperationApplicationException;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.foxykeep.datadroid.requestmanager.Request;
 import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
 import com.mili.xiaominglui.app.vello.R;
 import com.mili.xiaominglui.app.vello.config.VelloConfig;
 import com.mili.xiaominglui.app.vello.data.factory.IcibaWordXmlParser;
 import com.mili.xiaominglui.app.vello.data.model.Board;
 import com.mili.xiaominglui.app.vello.data.model.IcibaWord;
 import com.mili.xiaominglui.app.vello.data.model.List;
 import com.mili.xiaominglui.app.vello.data.model.WordCard;
 import com.mili.xiaominglui.app.vello.data.model.WordList;
 import com.mili.xiaominglui.app.vello.data.operation.GetDueWordCardListOperation;
 import com.mili.xiaominglui.app.vello.data.operation.LookUpInDictionaryOperation;
 import com.mili.xiaominglui.app.vello.data.provider.VelloContent;
 import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
 import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
 import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
 import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
 import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestManager;
 import com.mili.xiaominglui.app.vello.dialogs.ConnectionErrorDialogFragment.ConnectionErrorDialogListener;
 import com.mili.xiaominglui.app.vello.ui.MainActivity;
 import com.mili.xiaominglui.app.vello.util.AccountUtils;
 
 import java.lang.ref.WeakReference;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
import org.apache.http.HttpStatus;

 public class VelloService extends Service implements RequestListener,
 		ConnectionErrorDialogListener {
 	private static final String TAG = VelloService.class.getSimpleName();
 	private NotificationManager mNM;
 
 	private static boolean isRunning = false;
 	ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track
 																// of all
 																// current
 																// registered
 																// clients.
 
 	int mValue = 0; // Holds last value set by a client.
 	public static final int MSG_UNREGISTER_CLIENT = -1;
 	public static final int MSG_REGISTER_CLIENT = 0;
 
 	public static final int MSG_SPINNER_ON = 1;
 	public static final int MSG_SPINNER_OFF = 2;
 	public static final int MSG_DIALOG_BAD_DATA_ERROR_SHOW = 3;
 	public static final int MSG_DIALOG_CONNECTION_ERROR_SHOW = 4;
 	public static final int MSG_TOAST_INIT_VOCABULARY_START = 5;
 	public static final int MSG_TOAST_INIT_VOCABULARY_END = 6;
 	public static final int MSG_TOAST_GET_DUE_WORD = 7;
 	public static final int MSG_TOAST_NO_WORD_NOW = 8;
 	public static final int MSG_TOAST_NOT_AVAILABLE_WORD = 9;
 	public static final int MSG_TOAST_WORD_REVIEWED_COUNT_PLUS = 10;
 	public static final int MSG_SHOW_RESULT_WORDCARD = 11;
 
 	public static final int MSG_CHECK_VOCABULARY_BOARD = 100;
 	public static final int MSG_GET_DUE_WORDCARD_LIST = 101;
 	public static final int MSG_REVIEWED_WORDCARD = 102;
 	public static final int MSG_REVIEWED_PLUS_WORDCARD = 103;
 	public static final int MSG_CLOSE_WORDCARD = 104;
 	public static final int MSG_SYNC_LOCAL_CACHE = 106;
 	public static final int MSG_TRIGGER_QUERY_WORD = 107;
 
 	// Unique Identification Number for the Notification.
 	// We use it on Notification start, and to cancel it.
 	private int NOTIFICATION = R.string.local_service_started;
 
 	final Messenger mMessenger = new Messenger(new IncomingHandler(this));
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mMessenger.getBinder();
 	}
 
 	static class IncomingHandler extends Handler { // Handler of incoming
 													// messages from clients.
 		private final WeakReference<VelloService> mService;
 
 		IncomingHandler(VelloService service) {
 			mService = new WeakReference<VelloService>(service);
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			VelloService service = mService.get();
 			if (service != null) {
 				switch (msg.what) {
 				case MSG_REGISTER_CLIENT:
 					service.mClients.add(msg.replyTo);
 					break;
 				case MSG_UNREGISTER_CLIENT:
 					service.mClients.remove(msg.replyTo);
 					break;
 				case MSG_CHECK_VOCABULARY_BOARD:
 					service.checkVocabularyBoard();
 					break;
 				case MSG_GET_DUE_WORDCARD_LIST:
 					service.getDueWordCardList();
 					break;
 				case MSG_REVIEWED_WORDCARD:
 					String cardID = (String) msg.obj;
 					int listPosition = msg.arg1;
 					service.reviewedWordCard(cardID, listPosition);
 					break;
 				case MSG_REVIEWED_PLUS_WORDCARD:
 					String plusCardID = (String) msg.obj;
 					service.reviewedPlusWordCard(plusCardID);
 					break;
 				case MSG_CLOSE_WORDCARD:
 					String cardId = (String) msg.obj;
 					service.archiveWordCard(cardId);
 					break;
 				case MSG_TRIGGER_QUERY_WORD:
 				    String query = (String) msg.obj;
 				    service.queryInRemoteStorage(query);
 				    break;
 				default:
 					super.handleMessage(msg);
 				}
 			}
 
 		}
 	}
 
 	private void sendMessageToUI(int type, Object obj) {
 		for (int i = mClients.size() - 1; i >= 0; i--) {
 			try {
 			    Message msg = Message.obtain(null, type);
 			    if (obj != null) {
 			        msg.obj = obj;
 			    }
 				mClients.get(i).send(msg);
 			} catch (RemoteException e) {
 				// The client is dead. Remove it from the list; we are going
 				// through the list from back to front so this is safe to do
 				// inside the loop.
 				mClients.remove(i);
 			}
 		}
 	}
 
     @Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.i("LocalService", "Received start id " + startId + ": " + intent);
 		// We want this service to continue running until it is explicitly
 		// stopped, so return sticky.
 		return START_STICKY;
 	}
 
 	public static boolean isRunning() {
 		return isRunning;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Log.i(TAG, "VelloService Started.");
 		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		mRequestManager = VelloRequestManager.from(this);
 		mRequestList = new ArrayList<Request>();
 
 		// Display a notification about us starting. We put an icon in the
 		// status bar.
 		// showNotification();
 		isRunning = true;
 	}
 
 	@Override
 	public void onDestroy() {
 		// Cancel the persistent notification.
 		Log.i(TAG, "VelloService Stopped.");
 		isRunning = false;
 		mNM.cancel(NOTIFICATION);
 
 		// Tell the user we stopped.
 		Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_LONG)
 				.show();
 	}
 
 	protected VelloRequestManager mRequestManager;
 	protected ArrayList<Request> mRequestList;
 	
 	private void archiveWordCard(String idCard) {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "archiveWordCard start...");
 		}
 		Request archiveWordCard = VelloRequestFactory
 				.archiveWordCardRequest(idCard);
 		mRequestManager.execute(archiveWordCard, this);
 		mRequestList.add(archiveWordCard);
 	}
 
 	private void reviewedWordCard(String idCard, int position) {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "reviewedWordCard start...");
 		}
 		Request reviewedWordCard = VelloRequestFactory.reviewedWordCardRequest(
 				idCard, position);
 		mRequestManager.execute(reviewedWordCard, this);
 		mRequestList.add(reviewedWordCard);
 	}
 
 	private void reviewedPlusWordCard(String idCard) {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "reviewedPlusWordCard start...");
 		}
 		Request reviewedPlusWordCard = VelloRequestFactory
 				.reviewedPlusWordCardRequest(idCard);
 		mRequestManager.execute(reviewedPlusWordCard, this);
 		mRequestList.add(reviewedPlusWordCard);
 	}
 
 	private void checkVocabularyBoard() {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "checkVocabularyBoard start...");
 		}
 
 		Request checkVocabularyBoardRequest = VelloRequestFactory
 				.checkVocabularyBoardRequest();
 		mRequestManager.execute(checkVocabularyBoardRequest, this);
 		mRequestList.add(checkVocabularyBoardRequest);
 	}
 
 	private void createVocabularyBoard() {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "createVocabularyBoard start...");
 		}
 		Request createVocabularyBoardRequest = VelloRequestFactory
 				.createVocabularyBoardRequest();
 		mRequestManager.execute(createVocabularyBoardRequest, this);
 		mRequestList.add(createVocabularyBoardRequest);
 	}
 
 	private void createVocabularyList(int position) {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "createVocabularyList start...");
 		}
 		Request createVocabularyListRequest = VelloRequestFactory
 				.createVocabularyListRequest(position);
 		mRequestManager.execute(createVocabularyListRequest, this);
 		mRequestList.add(createVocabularyListRequest);
 	}
 
 	private void reOpenVocabulayList(int position, String id) {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "reOpenVocabulayList start...");
 		}
 		Request reOpenVocabularyListRequest = VelloRequestFactory
 				.reOpenVocabularyListRequest(position, id);
 		mRequestManager.execute(reOpenVocabularyListRequest, this);
 		mRequestList.add(reOpenVocabularyListRequest);
 	}
 
 	private void checkVocabularyLists() {
 		for (int i = 0; i < AccountUtils.VOCABULARY_LISTS_TITLE_ID.length; i++) {
 			if (VelloConfig.DEBUG_SWITCH) {
 				Log.d(TAG, "checkVocabularyLists start..." + i);
 			}
 			Request checkVocabularyListReqest = VelloRequestFactory
 					.checkVocabularyListRequest(i);
 
 			mRequestManager.execute(checkVocabularyListReqest, this);
 			mRequestList.add(checkVocabularyListReqest);
 		}
 
 	}
 
 	private void configureVocabularyBoard(String id) {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "configureVocabularyBoard start...");
 		}
 		Request configureVocabularyBoardRequest = VelloRequestFactory
 				.configureVocabularyBoardRequest(id);
 		mRequestManager.execute(configureVocabularyBoardRequest, this);
 		mRequestList.add(configureVocabularyBoardRequest);
 	}
 
 	private void getDueWordCardList() {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "getAllWordCardList start ...");
 		}
 
 		Request getAllWordCardListRequest = VelloRequestFactory
 				.getDueWordCardListRequest();
 		mRequestManager.execute(getAllWordCardListRequest, this);
 		mRequestList.add(getAllWordCardListRequest);
 
 	}
 	
 	private void queryInLocalCache(String query) {
 	    if (VelloConfig.DEBUG_SWITCH) {
 	        Log.d(TAG, "query in local cache...");
 	    }
 	    Request queryInLocalCache = VelloRequestFactory.queryInLocalCacheRequest(query);
 	    mRequestManager.execute(queryInLocalCache, this);
 	    mRequestList.add(queryInLocalCache);
         
     }
 
 	private void queryInRemoteStorage(String query) {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "checkWordCardStatusRequest start...");
 		}
 		Request queryInRemoteStorage = VelloRequestFactory
 				.queryInRemoteStorageRequest(query);
 		mRequestManager.execute(queryInRemoteStorage, this);
 		mRequestList.add(queryInRemoteStorage);
 	}
 	
 	private void lookUpInDictionary(String keyword) {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "look up in dictionary...");
 		}
 		Request lookUpInDictionary = VelloRequestFactory.lookUpInDictionaryRequest(keyword);
 		mRequestManager.execute(lookUpInDictionary, this);
 		mRequestList.add(lookUpInDictionary);
 	}
 
 	private void addWordCard(String keyword, String data) {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "addWordCard start...");
 		}
 		Request addWordCard = VelloRequestFactory.addWordCardRequest(keyword,
 				data);
 		mRequestManager.execute(addWordCard, this);
 		mRequestList.add(addWordCard);
 	}
 
 	private void initializeWordCard(String idCard) {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "initializeWordCard start...");
 		}
 		Request initializeWordCard = VelloRequestFactory
 				.initializeWordCardRequest(idCard);
 		mRequestManager.execute(initializeWordCard, this);
 		mRequestList.add(initializeWordCard);
 	}
 
 	private void reStartWordCard(String idCard) {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "reStartWordCard start...");
 		}
 		Request reStartWordCard = VelloRequestFactory
 				.reStartWordCardRequest(idCard);
 		mRequestManager.execute(reStartWordCard, this);
 		mRequestList.add(reStartWordCard);
 	}
 	
 	@Override
 	public void onRequestFinished(Request request, Bundle resultData) {
 		if (mRequestList.contains(request)) {
 			mRequestList.remove(request);
 
 			switch (request.getRequestType()) {
 			case VelloRequestFactory.REQUEST_TYPE_REVIEWED_WORDCARD:
 				WordCard reviewedWordCard = resultData
 						.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
 				if (reviewedWordCard != null) {
 					// reviewed
 
 				} else {
 					// reviewed failed
 					// do nothing at present
 				}
 				if (VelloConfig.DEBUG_SWITCH) {
 					Log.d(TAG, "reviewedWordCard end.");
 				}
 				return;
 
 			case VelloRequestFactory.REQUEST_TYPE_REVIEWED_PLUS_WORDCARD:
 				WordList wordList = resultData
 						.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDLIST);
 				String idReviewedPlusCard = request
 						.getString(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_CARD_ID);
 				if (wordList != null) {
 					String idList = wordList.id;
 					int position = AccountUtils.getVocabularyListPosition(
 							getApplicationContext(), idList);
 					reviewedWordCard(idReviewedPlusCard, position);
 				}
 				return;
 
 			case VelloRequestFactory.REQUEST_TYPE_CHECK_VOCABULARY_BOARD:
 				ArrayList<Board> boardList = resultData
 						.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_BOARD_LIST);
 				for (Board board : boardList) {
 					if (board.name.equals(AccountUtils
 							.getVocabularyBoardName(getApplicationContext()))
 							&& board.desc.equals(AccountUtils
 									.getVocabularyBoardVerification())) {
 						// s - 0 find out vocabulary board, check the closed
 						// flag.
 
 						if (!board.closed.equals("true")) {
 							// s - 0.0 vocabulary board is NOT well
 							// configured.
 							configureVocabularyBoard(board.id);
 							return;
 						} else {
 							// s - 0.1 well configured vocabulary board,
 							// save id
 							AccountUtils.setVocabularyBoardId(
 									getApplicationContext(), board.id);
 							checkVocabularyLists();
 							return;
 						}
 					}
 				}
 
 				// s - 1 NO vocabulary board found, need create one
 				createVocabularyBoard();
 				if (VelloConfig.DEBUG_SWITCH) {
 					Log.d(TAG, "check vocabulary board end.");
 				}
 				return;
 
 			case VelloRequestFactory.REQUEST_TYPE_CONFIGURE_VOCABULARY_BOARD:
 				String boardId = request
 						.getString(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_BOARD_ID);
 				if (resultData != null) {
 					// configure board successfully & save it
 					String id = resultData
 							.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_BOARD_ID);
 					AccountUtils.setVocabularyBoardId(getApplicationContext(),
 							id);
 					Log.d(TAG,
 							"configure board successfully! vocabulary board id = "
 									+ id);
 
 					// continue to check vocabulary list if not well formed
 					checkVocabularyLists();
 				} else {
 					// configure board failed, try again
 					configureVocabularyBoard(boardId);
 				}
 				return;
 
 			case VelloRequestFactory.REQUEST_TYPE_CHECK_VOCABULARY_LIST:
 				ArrayList<List> listList = resultData
 						.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_LIST);
 				int position = request
 						.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
 				for (List list : listList) {
 					if (list.name
 							.equals(AccountUtils.VOCABULARY_LISTS_TITLE_ID[position])) {
 						// get the vocabulary list[position]
 						if (!list.closed.equals("false")) {
 							// list[position] be closed unexpectedly
 							reOpenVocabulayList(position, list.id);
 							return;
 						} else {
 							// list[position]'s status ok, save listId
 							AccountUtils.setVocabularyListId(
 									getApplicationContext(), list.id, position);
 							if (AccountUtils
 									.isVocabularyBoardWellFormed(getApplicationContext())) {
 								// getDueWordCardList();
 							}
 							return;
 						}
 
 					}
 
 				}
 				// no vocabulary list found
 				createVocabularyList(position);
 				if (VelloConfig.DEBUG_SWITCH) {
 					Log.d(TAG, "check vocabulary list end.");
 				}
 				return;
 
 			case VelloRequestFactory.REQUEST_TYPE_REOPEN_VOCABULARY_LIST:
 				int positonList = request
 						.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
 				String idList = request
 						.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID);
 				if (resultData != null) {
 					// reopen list successfully
 					String id = resultData
 							.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID);
 					int pos = resultData
 							.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
 					AccountUtils.setVocabularyListId(getApplicationContext(),
 							id, pos);
 					if (AccountUtils
 							.isVocabularyBoardWellFormed(getApplicationContext())) {
 						// getDueWordCardList();
 					}
 				} else {
 					// reopen failed, try again
 					reOpenVocabulayList(positonList, idList);
 				}
 				return;
 
 			case VelloRequestFactory.REQUEST_TYPE_CREATE_VOCABULARY_LIST:
 				int positionList = request
 						.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
 				if (resultData != null) {
 					// create list successfully
 					String id = resultData
 							.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID);
 					int pos = resultData
 							.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
 					AccountUtils.setVocabularyListId(getApplicationContext(),
 							id, pos);
 					if (AccountUtils
 							.isVocabularyBoardWellFormed(getApplicationContext())) {
 						// getDueWordCardList();
 					}
 				} else {
 					// create list failed, try again
 					createVocabularyList(positionList);
 				}
 				if (VelloConfig.DEBUG_SWITCH) {
 					Log.d(TAG, "create vocabulary list end.");
 				}
 				return;
 
 			case VelloRequestFactory.REQUEST_TYPE_CREATE_VOCABULARY_BOARD:
 				if (resultData != null) {
 					// create vocabulary board successfully
 					String id = resultData
 							.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_BOARD_ID);
 
 					configureVocabularyBoard(id);
 				} else {
 					// create vocabulary board failed, try again
 					createVocabularyBoard();
 				}
 				return;
 			case VelloRequestFactory.REQUEST_TYPE_LOOK_UP_IN_DICTIONARY:
 			    String keywordInQuery = request.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);
 				String wsResponse = resultData.getString(VelloRequestFactory.BUNDLE_EXTRA_DICTIONARY_WS_RESPONSE);
 				if (!wsResponse.equals("null\n")) {
 				    addWordCard(keywordInQuery, wsResponse);
 				} else {
 				    // no result in dictionary server. TODO
 				    Toast.makeText(getApplicationContext(), "no result", Toast.LENGTH_SHORT).show();
 				}
 				
 				return;
 
 			case VelloRequestFactory.REQUEST_TYPE_QUERY_IN_REMOTE_STORAGE:
 				ArrayList<WordCard> existedWordCardList = resultData
 						.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD_LIST);
 				String keyword = request
 						.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);
 				if (existedWordCardList.isEmpty()) {
 					// not exist in remote storage
 					if (VelloConfig.DEBUG_SWITCH) {
 						Log.d(TAG, "not exist in remote storage");
 					}
 					// query in dictionary service
 					lookUpInDictionary(keyword);
 				} else {
 					// check more to decide
 					// filter to find the right one
 					for (WordCard w : existedWordCardList) {
 						if (w.name.equals(keyword)) {
 							// got the right word card
 							// show with user first
 							sendMessageToUI(MSG_SHOW_RESULT_WORDCARD, w);
 
 							if (w.closed.equals("true")) {
 								// the existed word card has be closed,
 								// re-open it.
 								if (VelloConfig.DEBUG_SWITCH) {
 									Log.d(TAG, "re-start existed word card.");
 								}
 								reStartWordCard(w.id);
 							} else {
 								if (w.due.equals("null")) {
 									// the existed word card has not be
 									// initialized, initialize it. this is the
 									// double check
 									if (VelloConfig.DEBUG_SWITCH) {
 										Log.d(TAG,
 												"initialize existed word card.");
 									}
 									initializeWordCard(w.id);
 								} else {
 									// the existed word is in review
 									// process, do
 									// nothing at present.
 									if (VelloConfig.DEBUG_SWITCH) {
 										Log.d(TAG,
 												"the existed word is in review.");
 									}
 								}
 							}
 							return;
 						}
 					}
 				}
 				return;
 
 			case VelloRequestFactory.REQUEST_TYPE_ADD_WORDCARD:
 			    String keywordInAddWordCard = request.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);
 			    String wsResult = request.getString(VelloRequestFactory.PARAM_EXTRA_DICTIONARY_WS_RESULT);
 				WordCard addedWordCard = resultData
 						.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
 				if (addedWordCard != null) {
 					// show added wordcard to user
 					// TODO
 					// new WordCardToWordTask().execute(addedWordCard);
 					// at the same time initialize it.
 					initializeWordCard(addedWordCard.id);
 				} else {
 					// add failed, add again
 				    addWordCard(keywordInAddWordCard, wsResult);
 				}
 
 				if (VelloConfig.DEBUG_SWITCH) {
 					Log.d(TAG, "addWordCard end.");
 				}
 
 				return;
 
 			case VelloRequestFactory.REQUEST_TYPE_RESTART_WORDCARD:
 				WordCard restartedWordCard = resultData
 						.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
 				if (restartedWordCard != null) {
 					// restarted
 					// do nothing at present.
 				} else {
 					// restart failed
 					// do nothing at present.
 				}
 				if (VelloConfig.DEBUG_SWITCH) {
 					Log.d(TAG, "reStartzeWordCard end.");
 				}
 				return;
 
 			case VelloRequestFactory.REQUEST_TYPE_INITIALIZE_WORDCARD:
 				WordCard initializedWordCard = resultData
 						.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
 				if (initializedWordCard != null) {
 					// initialized
 					// should show to user
 					// TODO
 				} else {
 					// initialized failed
 					// do nothing at present
 				}
 				if (VelloConfig.DEBUG_SWITCH) {
 					Log.d(TAG, "initializeWordCard end.");
 				}
 				return;
 
 			case VelloRequestFactory.REQUEST_TYPE_GET_DUE_WORDCARD_LIST:
 				boolean finished = resultData
 						.getBoolean(VelloRequestFactory.BUNDLE_EXTRA_RESULT_STATUS);
 				if (finished) {
 					// TODO finished expectly
 					// Build notification
 		            // TODO need rework
 		        	Intent intent = new Intent(getApplicationContext(), MainActivity.class);
 		            PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
 
 		            
 		            ProviderCriteria cri = new ProviderCriteria();
 		            cri.addSortOrder(DbWordCard.Columns.DUE, true);
 
 		            Calendar rightNow = Calendar.getInstance();
 		            SimpleDateFormat fo = new SimpleDateFormat(
 		                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 		            String now = fo.format(rightNow.getTime());
 		            cri.addLt(DbWordCard.Columns.DUE, now, true);
 		            cri.addNe(DbWordCard.Columns.CLOSED, "true");
 		            Cursor cur = getContentResolver().query(DbWordCard.CONTENT_URI, DbWordCard.PROJECTION,
 		                    cri.getWhereClause(), cri.getWhereParams(), cri.getOrderClause());
 		            if (cur != null) {
 		                int num = cur.getCount();
 		                if (num > 0) {
 		                    Notification noti = new Notification.Builder(getApplicationContext())
 		                            .setContentTitle(
 		                                    "You have " + num + " words need reviewing!")
 		                            .setContentText("Click me to begin reviewing!")
 		                            .setSmallIcon(R.drawable.ic_launcher)
 		                            .setContentIntent(pIntent).build();
 
 		                    NotificationManager notificationManager = (NotificationManager) getApplicationContext()
 		                            .getSystemService(Context.NOTIFICATION_SERVICE);
 
 		                    // Hide the notification after its selected
 		                    noti.flags |= Notification.FLAG_AUTO_CANCEL;
 
 		                    notificationManager.notify(0, noti);
 		                }
 		            }
 				} else {
 					// TODO something error
 				}
 				return;
 
 			case VelloRequestFactory.REQUEST_TYPE_ARCHIVE_WORDCARD:
 				WordCard closedWordCard = resultData
 						.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
 				String idCard = request
 						.getString(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_CARD_ID);
 				if (closedWordCard != null
 						&& closedWordCard.closed.equals("true")) {
 					// success, do nothing
 				} else {
 					// retry
 					archiveWordCard(idCard);
 				}
 				return;
 				
 			case VelloRequestFactory.REQUEST_TYPE_QUERY_IN_LOCAL_CACHE:
 			    WordCard localWordCard = resultData.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
 			    if (localWordCard != null) {
 			        // yes, show in UI
 			    	sendMessageToUI(MSG_SHOW_RESULT_WORDCARD, localWordCard);
 			    } else {
 			        // no, go on query in remote storage
 			        String query = request.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);
 			        queryInRemoteStorage(query);
 			    }
 			    return;
 
 			default:
 				return;
 			}
 		}
 	}
 
 	@Override
 	public void onRequestConnectionError(Request request, int statusCode) {
 		if (mRequestList.contains(request)) {
			if (statusCode == HttpStatus.SC_UNAUTHORIZED && request.getRequestType() == VelloRequestFactory.REQUEST_TYPE_CONFIGURE_VOCABULARY_BOARD) {
				String boardId = request.getString(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_BOARD_ID);
				AccountUtils.setVocabularyBoardId(getApplicationContext(), boardId);
				Log.d(TAG, "get a board without ownership, skip configuration! vocabulary board id = " + boardId);
 
				// continue to check vocabulary list if not well formed
				checkVocabularyLists();
			}
			mRequestList.remove(request);
 		}
 	}
 
 	@Override
 	public void onRequestDataError(Request request) {
 		// TODO
 		if (mRequestList.contains(request)) {
 			mRequestList.remove(request);
 
 		}
 
 	}
 
 	@Override
 	public void onRequestCustomError(Request request, Bundle resultData) {
 		// Never called.
 
 	}
 
 	@Override
 	public void connectionErrorDialogCancel(Request request) {
 	}
 
 	@Override
 	public void connectionErrorDialogRetry(Request request) {
 		mRequestManager.execute(request, this);
 		mRequestList.add(request);
 	}
 }
