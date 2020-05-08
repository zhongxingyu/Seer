 package com.steto.diaw.service;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpStatus;
 
 import roboguice.service.RoboIntentService;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.ResultReceiver;
 
 import com.google.inject.Inject;
 import com.steto.diaw.dao.DatabaseHelper;
 import com.steto.diaw.dao.ShowDao;
 import com.steto.diaw.model.Show;
 import com.steto.diaw.parser.AbstractParser;
 import com.steto.diaw.parser.SeriesParser;
 import com.steto.diaw.web.QueryString;
 import com.steto.diaw.web.SeriesIDConnector;
 import com.steto.diaw.web.SeriesNameConnector;
 import com.steto.diaw.web.WebConnector;
 
 /**
  * Created by Stephane on 02/06/13.
  */
 public class TVDBService extends RoboIntentService {
 
 	private static final String NAME = "TVDBService";
 	public static final String INPUT_SERIE = "INPUT_SERIE";
	public static final String INPUT_REAL_NAME = "INPUT_REAL_NAME";
 	private static final String SERIE_NAME_QUERY = "seriesname";
 	public static final String OUTPUT_DATA = "OUTPUT_DATA";
 	public static final String INPUT_RESULTRECEIVER = "INPUT_RESULTRECEIVER";
 	public static final int RESULT_CODE_OK = 0;
 	public static final int RESULT_CODE_AMBIGUITY = -20; // code d'erreur < a -20 sont des erreurs du service, < -10 des erreurs BDD, <0 des erreurs Parser, > 0
 															// les codes retours HTTP
 
 	@Inject
 	private DatabaseHelper mDatabaseHelper;
	private String mRealShowName;
 
 	public TVDBService() {
 		super(NAME);
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		Show input = (Show) intent.getExtras().get(INPUT_SERIE);
		mRealShowName = intent.getExtras().getString(INPUT_REAL_NAME, null);
 		ResultReceiver receiver = (ResultReceiver) intent.getExtras().get(INPUT_RESULTRECEIVER);
 		Integer id = input.getTVDBID();
 		int responseCode = RESULT_CODE_OK;
 		List<Show> ret = null;
 		if (id != 0) {
 			ret = getShowsFromId(input, id, ret);
 		} else {
 			ret = getShowsFromName(input, ret);
 			if (ret != null && !ret.isEmpty() && ret.size() == 1) { // si la "recherche" sur TVDB n'a donné qu'un seul resultat alors pas besoin de lancer une
 																	// recherche sur IMDB
 				ret = getShowsFromId(input, ret.get(0).getTVDBID(), ret);
 			} else {
 				responseCode = RESULT_CODE_AMBIGUITY;
 			}
 		}
 		sendBackResult(receiver, responseCode, (Serializable) ret);
 
 	}
 
 	private void sendBackResult(ResultReceiver receiver, int responseCode, Serializable ret) {
 		// retour à l'appelant
 		Bundle retBundle = new Bundle();
 		retBundle.putSerializable(OUTPUT_DATA, ret);
 		receiver.send(responseCode, retBundle);
 	}
 
 	private List<Show> getShowsFromName(Show input, List<Show> ret) {
 		SeriesNameConnector myWeb = new SeriesNameConnector();
 		QueryString myQuery = new QueryString();
 		myQuery.add(SERIE_NAME_QUERY, input.getShowName());
 		myWeb.requestFromNetwork(myQuery.getQuery(), WebConnector.HTTPMethod.GET, null);
 		if (myWeb.getStatusCode() == HttpStatus.SC_OK) {
 			SeriesParser myParser = new SeriesParser();
 			InputStream in = myWeb.getResponseBody();
 			InputStream inin = in;
 			try {
 				String tmp = IOUtils.toString(in);
 				inin = IOUtils.toInputStream(tmp);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			ret = myParser.parse(inin);
 
 			// if (myParser.getStatusCode() == AbstractParser.PARSER_OK) {
 			// //on va finalement renvoyer la serie avec toutes les infos à l'appelant
 			// //au lieu de recopier une a une les infos on récupère juste l'ID de la base dans l'ancien objet et on le copie dans le nouvel objet
 			// //on copie aussi le nom de la serie pour garder la correspondance avec la liste des episodes
 			// ret.get(0).setId(input.getId());
 			// ret.get(0).setTVDBConnected(false);
 			// ret.get(0).setShowName(input.getShowName());
 			// try {
 			// ShowDao myDAO = null;
 			// myDAO = mDatabaseHelper.getDao(Show.class);
 			// myDAO.createOrUpdate(ret != null ? ret.get(0) : null);
 			// } catch (SQLException e) {
 			// e.printStackTrace();
 			// }
 			// }
 		}
 		return ret;
 	}
 
 	private List<Show> getShowsFromId(Show input, Integer id, List<Show> ret) {
 		if (!input.isTVDBConnected()) {
 			SeriesIDConnector myWeb = new SeriesIDConnector();
 			myWeb.requestFromNetwork(id.toString() + "/" + Locale.getDefault().getLanguage() + ".xml", WebConnector.HTTPMethod.GET, null);
 
 			if (myWeb.getStatusCode() == HttpStatus.SC_OK) {
 				SeriesParser myParser = new SeriesParser();
 				ret = myParser.parse(myWeb.getResponseBody());
 				if (myParser.getStatusCode() == AbstractParser.PARSER_OK) {
 					ret.get(0).setId(input.getId());
 					ret.get(0).setTVDBConnected(true);
					ret.get(0).setShowName(mRealShowName != null ? mRealShowName : input.getShowName());
 					try {
 						ShowDao myDAO = null;
 						myDAO = mDatabaseHelper.getDao(Show.class);
 						myDAO.createOrUpdate(ret != null ? ret.get(0) : null);
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		} else {
 			ret = new ArrayList<Show>();
 			ret.add(input);// si l'episode est deja complet alors on le renvoie à l'apellant
 		}
 		return ret;
 	}
 }
