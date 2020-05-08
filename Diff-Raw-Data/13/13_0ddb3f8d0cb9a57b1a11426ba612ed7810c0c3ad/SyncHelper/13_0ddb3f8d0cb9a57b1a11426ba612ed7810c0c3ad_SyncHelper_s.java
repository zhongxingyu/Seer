 package com.mili.xiaominglui.app.vello.syncadapter;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.TimeZone;
 
 import com.foxykeep.datadroid.exception.ConnectionException;
 import com.foxykeep.datadroid.exception.DataException;
 import com.foxykeep.datadroid.network.NetworkConnection;
 import com.foxykeep.datadroid.network.NetworkConnection.ConnectionResult;
 import com.foxykeep.datadroid.network.NetworkConnection.Method;
 import com.google.gson.Gson;
 import com.mili.xiaominglui.app.vello.R;
 import com.mili.xiaominglui.app.vello.config.VelloConfig;
 import com.mili.xiaominglui.app.vello.config.WSConfig;
 import com.mili.xiaominglui.app.vello.data.factory.WordCardListJsonFactory;
 import com.mili.xiaominglui.app.vello.data.model.WordCard;
 import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
 import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
 import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
 import com.mili.xiaominglui.app.vello.ui.MainActivity;
 import com.mili.xiaominglui.app.vello.util.AccountUtils;
 
 import android.accounts.Account;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ContentProviderOperation;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.OperationApplicationException;
 import android.content.SyncResult;
 import android.database.Cursor;
 import android.net.ConnectivityManager;
 import android.os.Bundle;
 import android.os.RemoteException;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 
 public class SyncHelper {
 	private static final String TAG = SyncHelper.class.getSimpleName();
 	
 	public static final int FLAG_SYNC_LOCAL = 0x1;
     public static final int FLAG_SYNC_REMOTE = 0x2;
 	
 	private Context mContext;
     private String mAuthToken;
 	
 	public SyncHelper(Context context) {
         mContext = context;
         mAuthToken = AccountUtils.getAuthToken(mContext);
     }
 	
 	public static void requestManualSync(Account mChosenAccount) {
         Bundle b = new Bundle();
         b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
         ContentResolver.requestSync(
                 mChosenAccount,
                 VelloProvider.AUTHORITY, b);
     }
 	
 	public void performSync(SyncResult syncResult, int flags) throws IOException {
 		if (!isOnline()) {
 			return;
 		}
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "onPerformSync...");
 		}
 
 		// query and backup all local items that syncInNext=true or merge
 		// locally later
 		HashMap<String, WordCard> localDirtyWords = new HashMap<String, WordCard>();
 		final ContentResolver resolver = mContext.getContentResolver();
 		ProviderCriteria criteria = new ProviderCriteria();
 		criteria.addEq(DbWordCard.Columns.SYNCINNEXT, "true");
 		Cursor c = resolver.query(DbWordCard.CONTENT_URI,
 				DbWordCard.PROJECTION, criteria.getWhereClause(),
 				criteria.getWhereParams(), criteria.getOrderClause());
 		if (c != null) {
 			while (c.moveToNext()) {
 				WordCard wc = new WordCard(c);
 				localDirtyWords.put(wc.id, wc);
 			}
 		}
 
 		try {
 			ArrayList<WordCard> preSyncRemoteWordCardList = getOpenWordCards();
 			if (preSyncRemoteWordCardList.size() > 0) {
 				SimpleDateFormat format = new SimpleDateFormat(
 						"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 				for (WordCard wordCard : preSyncRemoteWordCardList) {
 					String idCard = wordCard.id;
 					if (localDirtyWords.containsKey(idCard)) {
 						// need merging
 						WordCard dirtyWordCard = localDirtyWords.get(idCard);
 						String stringLocalDateLastActivity = dirtyWordCard.dateLastActivity;
 						String stringRemoteDateLastActivity = wordCard.dateLastActivity;
 						if (stringLocalDateLastActivity
 								.equals(stringRemoteDateLastActivity)) {
 							// remote has no commit
 							// commit local due, closed, listId to remote
 							updateRemoteWordCard(dirtyWordCard);
 						} else {
 							// remote has commit
 							// update remote data based on local data
 							WordCard newWordCard = upgradeWordCard(wordCard,
 									dirtyWordCard);
 							updateRemoteWordCard(newWordCard);
 						}
 					}
 				}
 			}
 
 			// Clear the table
//			mContext.getContentResolver().delete(DbWordCard.CONTENT_URI, null, null);
 
 			ArrayList<WordCard> postSyncRemoteWordCardList = getOpenWordCards();
 			if (postSyncRemoteWordCardList.size() > 0) {
 				ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
 				for (WordCard wordCard : postSyncRemoteWordCardList) {
 					operationList.add(ContentProviderOperation
 							.newInsert(DbWordCard.CONTENT_URI)
 							.withValues(wordCard.toContentVaalues()).build());
 				}
 
 				mContext.getContentResolver().applyBatch(
 						VelloProvider.AUTHORITY, operationList);
 
 				// Build notification
 				// TODO need rework
 				Intent intent = new Intent(mContext, MainActivity.class);
 				PendingIntent pIntent = PendingIntent.getActivity(mContext, 0,
 						intent, 0);
 
 				ProviderCriteria cri = new ProviderCriteria();
 				cri.addSortOrder(DbWordCard.Columns.DUE, true);
 
 				Calendar rightNow = Calendar.getInstance();
 				long rightNowUnixTime = rightNow.getTimeInMillis();
 				long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
 				SimpleDateFormat fo = new SimpleDateFormat(
 						"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 				String now = fo.format(new Date(rightNowUnixTimeGMT));
 				cri.addLt(DbWordCard.Columns.DUE, now, true);
 				cri.addNe(DbWordCard.Columns.CLOSED, "true");
 				Cursor cur = mContext.getContentResolver().query(
 						DbWordCard.CONTENT_URI, DbWordCard.PROJECTION,
 						cri.getWhereClause(), cri.getWhereParams(),
 						cri.getOrderClause());
 				if (cur != null) {
 					int num = cur.getCount();
 					if (num > 0) {
 						NotificationManager notificationManager = (NotificationManager) mContext
 								.getSystemService(Context.NOTIFICATION_SERVICE);
 
 						NotificationCompat.Builder builder = new NotificationCompat.Builder(
 								mContext)
 								.setWhen(rightNow.getTimeInMillis())
 								.setSmallIcon(R.drawable.ic_stat_vaa)
 								.setContentTitle(mContext.getText(R.string.notif_content_title))
 								.setContentText(mContext.getText(R.string.notif_content_text))
 								.setContentInfo(String.valueOf(num))
 								.setContentIntent(pIntent)
 								.setAutoCancel(true);
 
 						notificationManager.notify(0, builder.build());
 					}
 				}
 			}
 
 		} catch (ConnectionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (DataException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (OperationApplicationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * retrieve all open card from trello
 	 * 
 	 * @throws ConnectionException
 	 * @throws DataException
 	 */
 	private ArrayList<WordCard> getOpenWordCards() throws ConnectionException,
 			DataException {
 		String vocabularyBoardId = AccountUtils.getVocabularyBoardId(mContext);
 		String urlString = WSConfig.TRELLO_API_URL
 				+ WSConfig.WS_TRELLO_TARGET_BOARD + "/" + vocabularyBoardId
 				+ WSConfig.WS_TRELLO_FIELD_CARDS;
 
 		HashMap<String, String> parameterMap = new HashMap<String, String>();
 		parameterMap.put(WSConfig.WS_TRELLO_PARAM_FILTER, "open");
 		parameterMap.put(WSConfig.WS_TRELLO_PARAM_FIELDS,
 				"name,desc,due,closed,idList,dateLastActivity");
 		parameterMap.put(WSConfig.WS_TRELLO_PARAM_APP_KEY,
 				WSConfig.VELLO_APP_KEY);
 		parameterMap.put(WSConfig.WS_TRELLO_PARAM_ACCESS_TOKEN, mAuthToken);
 
 		NetworkConnection networkConnection = new NetworkConnection(mContext,
 				urlString);
 		networkConnection.setMethod(Method.GET);
 		networkConnection.setParameters(parameterMap);
 		ConnectionResult result = networkConnection.execute();
 		return WordCardListJsonFactory.parseResult(result.body);
 	}
 	
 	/**
 	 * update trello card due, idList & closed field
 	 * 
 	 * @param wordCard
 	 * @throws ConnectionException
 	 */
 	private void updateRemoteWordCard(WordCard wordCard)
 			throws ConnectionException {
 		String urlString = WSConfig.TRELLO_API_URL
 				+ WSConfig.WS_TRELLO_TARGET_CARD + "/" + wordCard.id;
 
 		HashMap<String, String> parameterMap = new HashMap<String, String>();
 		parameterMap.put(WSConfig.WS_TRELLO_PARAM_CLOSED, wordCard.closed);
 		parameterMap.put(WSConfig.WS_TRELLO_PARAM_DUE, wordCard.due);
 		parameterMap.put(WSConfig.WS_TRELLO_PARAM_IDLIST, wordCard.idList);
 
 		parameterMap.put(WSConfig.WS_TRELLO_PARAM_APP_KEY,
 				WSConfig.VELLO_APP_KEY);
 		parameterMap.put(WSConfig.WS_TRELLO_PARAM_ACCESS_TOKEN, mAuthToken);
 
 		NetworkConnection networkConnection = new NetworkConnection(mContext,
 				urlString);
 		networkConnection.setMethod(Method.PUT);
 		networkConnection.setParameters(parameterMap);
 		ConnectionResult result = networkConnection.execute();
 
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "result.body = " + result.body);
 		}
 
 		Gson gson = new Gson();
 		WordCard updatedWordCard = gson.fromJson(result.body, WordCard.class);
 		if (updatedWordCard != null) {
 			// update success
 		} else {
 			// update failed
 		}
 	}
 	
 	/**
 	 * merge wordcard when remote wordcard is NOT the same state
 	 * @param wordCard remote wordcard
 	 * @param dirtyWordCard local changed wordcard
 	 * @return new wordcard merged
 	 */
 	private WordCard upgradeWordCard(WordCard wordCard, WordCard dirtyWordCard) {
 		int localPositionInLists = AccountUtils.getVocabularyListPosition(
 				mContext, dirtyWordCard.idList);
 		int remotePostionInLists = AccountUtils.getVocabularyListPosition(
 				mContext, wordCard.idList);
 		int delta = remotePostionInLists - localPositionInLists;
 		if (delta > 0) {
 			int compensation = delta + 1;
 			if (remotePostionInLists + compensation > VelloConfig.VOCABULARY_LIST_POSITION_8TH) {
 				wordCard.closed = "true";
 			} else {
 				int newPostionInLists = remotePostionInLists + compensation;
 				wordCard.idList = AccountUtils.getVocabularyListId(mContext,
 						newPostionInLists);
 				Calendar rightNow = Calendar.getInstance();
 				long rightNowUnixTime = rightNow.getTimeInMillis();
 				long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
 				long deltaTime = VelloConfig.VOCABULARY_LIST_DUE_DELTA[newPostionInLists];
 				long newDueUnixTime = rightNowUnixTimeGMT + deltaTime;
 				SimpleDateFormat format = new SimpleDateFormat(
 						"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 				Date newDueDate = new Date(newDueUnixTime);
 				String stringNewDueDate = format.format(newDueDate);
 				wordCard.due = stringNewDueDate;
 			}
 		}
 		return wordCard;
 	}
 
 	private boolean isOnline() {
 		ConnectivityManager cm = (ConnectivityManager) mContext
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		return cm.getActiveNetworkInfo() != null
 				&& cm.getActiveNetworkInfo().isConnectedOrConnecting();
 	}
 }
