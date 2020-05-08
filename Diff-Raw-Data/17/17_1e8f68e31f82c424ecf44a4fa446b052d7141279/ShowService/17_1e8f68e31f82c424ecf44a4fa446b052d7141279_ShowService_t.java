 package com.steto.diaw.service;
 
 import android.app.Activity;
 import android.app.IntentService;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.ResultReceiver;
 import com.steto.diaw.dao.EpisodeDao;
 import com.steto.diaw.model.Episode;
 import com.steto.diaw.parser.ShowParser;
 import com.steto.diaw.tools.DatabaseHelper;
 import com.steto.diaw.tools.QueryString;
 import com.steto.diaw.tools.Tools;
 import com.steto.diaw.web.ParseConnector;
 import com.steto.diaw.web.ShowConnector;
 import org.apache.http.HttpStatus;
 
 import java.io.InputStream;
 import java.io.Serializable;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 
 public class ShowService extends IntentService {
 
 	public static final String INTENT_RESULT_RECEIVER = "INTENT_RESULT_RECEIVER";
 	public static final String INTENT_LOGIN = "INTENT_LOGIN";
 	public static final String URL_SHOW = "/1/classes/Show";
 	public static final int RESULT_CODE_OK = 0;
 	public static final String RESULT_DATA = "RESULT_DATA";
 	private static String WS_QUERY_WHERE = "where";
 
 	public ShowService() {
 		super("ShowService");
 	}
 
 	public ShowService(String name) {
 		super(name);
 	}
 
 	public String createQueryString(String mail) {
 
 		QueryString myQuery = new QueryString();
 		myQuery.add("limit", "500");
 		myQuery.add(WS_QUERY_WHERE, createWhereClause(mail));
 		return myQuery.toString();
 	}
 
 	public String createWhereClause(String mail) {
 		return "{\"login\": \"" + mail + "\" }";
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		ResultReceiver sender = (ResultReceiver) intent.getExtras().get(INTENT_RESULT_RECEIVER);
 		String login = intent.getExtras().getString(INTENT_LOGIN);
 		int responseCode = RESULT_CODE_OK;
 		String query = createQueryString(login);
 		ShowConnector myWeb = new ShowConnector();
 		myWeb.requestFromNetwork(query, ParseConnector.HTTPMethod.GET, null);
 		List<Episode> allEp = null;
 		if (myWeb.getStatusCode() == HttpStatus.SC_OK) {
 			InputStream response = myWeb.getResponseBody();
 			ShowParser myParser = new ShowParser();
 			allEp = myParser.parse(response);
 			responseCode = myParser.getStatusCode();
 
 			addInDatabase(allEp);
 		} else {
 			responseCode = myWeb.getStatusCode();
 
 		}
 		Bundle ret = new Bundle();
 		ret.putSerializable(RESULT_DATA, (Serializable) allEp);
 		sender.send(responseCode, ret);
 	}
 
 	private void addInDatabase(List<Episode> allEp) {
 		DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
 
 		try {
 			EpisodeDao epDao = databaseHelper.getEpisodeDao();
 			for (Episode episode : allEp) {
 				epDao.createOrUpdate(episode);
 			}
 			SharedPreferences settings = getSharedPreferences(Tools.SHARED_PREF_FILE, Activity.MODE_PRIVATE);
 			SharedPreferences.Editor editor = settings.edit();
 			editor.putLong(Tools.SHARED_PREF_LAST_UPDATE, (new Date()).getTime());
 			editor.commit();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
