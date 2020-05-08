 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.portfolio;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
import java.util.Iterator;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.Toast;
 import eu.trentorise.smartcampus.ac.AACException;
 import eu.trentorise.smartcampus.ac.SCAccessProvider;
 import eu.trentorise.smartcampus.android.common.AppHelper;
 import eu.trentorise.smartcampus.android.common.Utils;
 import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
 import eu.trentorise.smartcampus.android.common.tagging.SuggestionHelper;
 import eu.trentorise.smartcampus.common.Preferences;
 import eu.trentorise.smartcampus.portfolio.conf.AppConfigurations;
 import eu.trentorise.smartcampus.portfolio.models.Concept;
 import eu.trentorise.smartcampus.portfolio.models.Portfolio;
 import eu.trentorise.smartcampus.portfolio.models.SharedPortfolioContainer;
 import eu.trentorise.smartcampus.portfolio.models.StudentExams;
 import eu.trentorise.smartcampus.portfolio.models.StudentInfo;
 import eu.trentorise.smartcampus.portfolio.models.UserData;
 import eu.trentorise.smartcampus.portfolio.models.UserProducedData;
 import eu.trentorise.smartcampus.portfolio.utils.PMUtils;
 import eu.trentorise.smartcampus.portfolio.utils.RawUtil;
 import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
 import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
 import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
 import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 import eu.trentorise.smartcampus.social.model.Entity;
 import eu.trentorise.smartcampus.storage.DataException;
 import eu.trentorise.smartcampus.storage.remote.RemoteStorage;
 
 public class PMHelper {
 
 	private static final String BROWSER_PREFS = "pm_browser_pref";
 	private static final String FIRST_LAUNCH_PREFS = "dt_firstLaunch";
 
 	private static AppConfigurations config = null;
 
 	private static Context mContext;
 	private static PMHelper instance = null;
 	private static RemoteStorage remoteStorage = null;
 	private static SCAccessProvider accessProvider = null;
 
 	private static String mToken;
 
 	// cached notes
 	private UserData userData = null;
 	// cached portfolios
 	private List<Portfolio> portfolioList = null;
 	// cached student info
 	private StudentInfo studentInfo = null;
 	private Boolean hasStudentInfo = null;
 	// cached student exams
 	private StudentExams studentExams = null;
 	private Boolean hasStudentExams = null;
 	// cached user produced data
 	private List<UserProducedData> userProducedData;
 
 	public static final String ENTITY_TYPE_PORTFOLIO = "portfolio";
 	private static boolean write_preferences = false;
 
 	protected PMHelper(Context mContext) {
 		super();
 		this.mContext = mContext;
 		this.accessProvider = SCAccessProvider.getInstance(mContext);
 	}
 
 	public static void init(Context ctx) {
 		config = new AppConfigurations(ctx);
 		mContext = ctx;
 		if (instance == null) {
 			instance = new PMHelper(mContext);
 		}
 	}
 
 	public static void start() throws NameNotFoundException, DataException, ConnectionException, ProtocolException,
 			SecurityException, AACException {
 		getPortfolioList();
 	}
 
 	public static PMHelper getInstance() throws DataException {
 		if (instance == null) {
 			throw new DataException("Helper is not initialized");
 		}
 		return instance;
 	}
 
 	public static RemoteStorage getRemoteStorage() throws NameNotFoundException, ProtocolException, AACException {
 		if (remoteStorage == null) {
 			remoteStorage = new RemoteStorage(mContext, Preferences.getAppToken());
 		}
 		remoteStorage.setConfig(PMHelper.getAuthToken(), Preferences.getHost(mContext), Preferences.getService());
 		return remoteStorage;
 	}
 
 	public static Context getContext() {
 		return mContext;
 	}
 
 	public static String getAuthToken() throws AACException {
 		mToken = PMHelper.getAccessProvider().readToken(mContext);
 		return mToken;
 	}
 
 	public static SCAccessProvider getAccessProvider() {
 		if (accessProvider == null) {
 			accessProvider = SCAccessProvider.getInstance(mContext);
 		}
 		return accessProvider;
 	}
 
 	public static void endAppFailure(Activity activity, int id) {
 		Toast.makeText(activity, activity.getResources().getString(id), Toast.LENGTH_LONG).show();
 		activity.finish();
 	}
 
 	public static void showFailure(Context ctx, int id) {
 		Toast.makeText(ctx, ctx.getResources().getString(id), Toast.LENGTH_LONG).show();
 	}
 
 	public static void setNotes(String string) throws DataException, NameNotFoundException, ConnectionException,
 			ProtocolException, SecurityException, AACException {
 		if (getInstance().userData == null) {
 			getNotes();
 		}
 		getInstance().userData.setNotes(string);
 		getRemoteStorage().update(getInstance().userData, false);
 
 	}
 
 	public static String getNotes() throws DataException, NameNotFoundException, ConnectionException,
 			ProtocolException, SecurityException, AACException {
 		// if (getInstance().userData == null) {
 		Collection<UserData> list = getRemoteStorage().getObjects(UserData.class);
 		if (list != null && !list.isEmpty()) {
 			getInstance().userData = list.iterator().next();
 		}
 		// }
 		return getInstance().userData.getNotes();
 	}
 
 	public static void createEmptyPortfolio(String name) throws NameNotFoundException, DataException,
 			ConnectionException, ProtocolException, SecurityException, AACException {
 		Portfolio p = new Portfolio();
 		p.timestamp = System.currentTimeMillis();
 		p.name = name;
 		p.showUserGeneratedData = new ArrayList<String>();
 		p.highlightUserGeneratedData = new ArrayList<String>();
 		p.showStudentInfo = new ArrayList<String>();
 		if (!config.isTestModeEnabled()) {
 			p = getRemoteStorage().create(p);
 		}
 
 		if (p == null) {
 			throw new DataException("Portfolio is null");
 		} else {
 			getPortfolioList().add(0, p);
 		}
 	}
 
 	public static List<UserProducedData> getUserProducedDataList() throws NameNotFoundException, DataException,
 			ConnectionException, ProtocolException, SecurityException, AACException {
 		if (getInstance().userProducedData == null) {
 			if (config.isTestModeEnabled()) {
 				String rawUPData = RawUtil.getRawAsString(mContext, R.raw.user_produced_data_test_list);
 				getInstance().userProducedData = Utils.convertJSONToObjects(rawUPData, UserProducedData.class);
 			} else {
 				Collection<UserProducedData> coll = getRemoteStorage().getObjects(UserProducedData.class);
 				if (coll == null)
 					getInstance().userProducedData = Collections.emptyList();
 				getInstance().userProducedData = new ArrayList<UserProducedData>(coll);
 			}
 		}
 		return getInstance().userProducedData;
 	}
 
 	public static void saveUserProducedData(Portfolio mPortfolio, HashSet<String> mUpdatedElements,
 			HashSet<String> mCherryElements) throws NameNotFoundException, DataException, ConnectionException,
 			ProtocolException, SecurityException, AACException {
 		mPortfolio.showUserGeneratedData = PMUtils.updateList(mPortfolio.showUserGeneratedData, mUpdatedElements);
		// CherryElements
		mPortfolio.highlightUserGeneratedData = PMUtils.updateList(mPortfolio.highlightUserGeneratedData, mCherryElements);
 		getRemoteStorage().update(mPortfolio, false);
 	}
 
 	public static void savePresentationData(Portfolio mPortfolio, HashSet<String> mUpdatedElements)
 			throws NameNotFoundException, DataException, ConnectionException, ProtocolException, SecurityException,
 			AACException {
 		mPortfolio.showUserGeneratedData = PMUtils.updateList(mPortfolio.showUserGeneratedData, mUpdatedElements);
 		getRemoteStorage().update(mPortfolio, false);
 	}
 
 	public static void savePortfolioData(Portfolio mPortfolio, HashSet<String> mUpdatedElements)
 			throws NameNotFoundException, DataException, ConnectionException, ProtocolException, SecurityException,
 			AACException {
 		// mUpdatedElements contains categories
 		HashSet<String> idsHashSet = new HashSet<String>();
 		for (String category : mUpdatedElements) {
 			for (UserProducedData upd : getInstance().userProducedData) {
 				if (upd.category.equalsIgnoreCase(category)) {
 					idsHashSet.add(upd.getId());
 				}
 			}
 		}
 		mPortfolio.showUserGeneratedData = PMUtils.updateList(mPortfolio.showUserGeneratedData, idsHashSet);
 		getRemoteStorage().update(mPortfolio, false);
 	}
 
 	public static void savePersonalInfo(Portfolio mPortfolio, HashSet<String> mUpdatedInfoElems,
 			HashSet<String> mUpdatedUPDataElements, HashSet<String> mCherryElements) throws NameNotFoundException,
 			DataException, ConnectionException, ProtocolException, SecurityException, AACException {
 		// InfoElems
 		mPortfolio.showStudentInfo = PMUtils.updateList(mPortfolio.showStudentInfo, mUpdatedInfoElems);
 		// UPDataElements
 		mPortfolio.showUserGeneratedData = PMUtils.updateList(mPortfolio.showUserGeneratedData, mUpdatedUPDataElements);
 		// CherryElements
 		mPortfolio.highlightUserGeneratedData = PMUtils.updateList(mPortfolio.highlightUserGeneratedData,
 				mCherryElements);
 
 		getRemoteStorage().update(mPortfolio, false);
 	}
 
 	public static void savePortfolioCherryData(Portfolio mPortfolio, HashSet<String> mCherryElements)
 			throws NameNotFoundException, DataException, ConnectionException, ProtocolException, SecurityException,
 			AACException {
 		mPortfolio.highlightUserGeneratedData = PMUtils.updateList(mPortfolio.highlightUserGeneratedData,
 				mCherryElements);
 		getRemoteStorage().update(mPortfolio, false);
 	}
 
 	public static void saveTags(Portfolio mPortfolio, List<Concept> list) throws NameNotFoundException, DataException,
 			ConnectionException, ProtocolException, SecurityException, AACException {
 		mPortfolio.tags = list;
 		getRemoteStorage().update(mPortfolio, false);
 	}
 
 	public static StudentExams getStudentExams(FragmentActivity activity) throws DataException, NameNotFoundException,
 			ConnectionException, ProtocolException, SecurityException, AACException {
 		if (getInstance().hasStudentExams == null) {
 			if (config.isTestModeEnabled()) {
 				String rawData = RawUtil.getRawAsString(mContext, R.raw.student_exams_test_list);
 				getInstance().studentExams = Utils.convertJSONToObject(rawData, StudentExams.class);
 			} else {
 				Collection<StudentExams> coll = getRemoteStorage().getObjects(StudentExams.class);
 				if (coll == null || coll.isEmpty()) {
 					getInstance().studentExams = null;
 				} else {
 					getInstance().studentExams = coll.iterator().next();
 				}
 			}
 			getInstance().hasStudentExams = getInstance().studentExams != null;
 		}
 		return getInstance().studentExams;
 	}
 
 	public static List<Portfolio> getPortfolioList() throws DataException, NameNotFoundException, ConnectionException,
 			ProtocolException, SecurityException, AACException {
 		if (getInstance().portfolioList == null) {
 			if (config.isTestModeEnabled()) {
 				String rawPortfolios = RawUtil.getRawAsString(mContext, R.raw.portfolios_test_list);
 				getInstance().portfolioList = Utils.convertJSONToObjects(rawPortfolios, Portfolio.class);
 			} else {
 				Collection<Portfolio> coll = getRemoteStorage().getObjects(Portfolio.class);
 				if (coll == null)
 					getInstance().portfolioList = Collections.emptyList();
 				getInstance().portfolioList = new ArrayList<Portfolio>(coll);
 			}
 		}
 		return getInstance().portfolioList;
 	}
 
 	public static void removePortfolioFromList(Portfolio p) throws NameNotFoundException, DataException,
 			ConnectionException, ProtocolException, SecurityException, AACException {
 		if (!config.isTestModeEnabled()) {
 			getRemoteStorage().delete(p.getId(), Portfolio.class);
 		}
 		List<Portfolio> list = getPortfolioList();
 		for (int i = 0; i < list.size(); i++) {
 			if (p.getId().equals(list.get(i).getId())) {
 				list.remove(i);
 				break;
 			}
 		}
 	}
 
 	public static boolean removePortfolio(Portfolio p) throws NameNotFoundException, DataException,
 			ConnectionException, ProtocolException, SecurityException, AACException {
 		if (!config.isTestModeEnabled()) {
 			getRemoteStorage().delete(p.getId(), Portfolio.class);
 			//delete from local
 			removeLocalPortfolio(p.getId());
 			return true;
 		}
 		return false;
 	}
 
 	private static void removeLocalPortfolio(String id) throws DataException {
		for (Iterator<Portfolio> iterator = getInstance().portfolioList.iterator(); iterator.hasNext();) {
			Portfolio p = iterator.next();
 			if (p.getId().equals(id)) {
				iterator.remove();
 			}
 		}
 	}
 
 	public static Portfolio getPortfolio(String portfolioEntityId) throws NameNotFoundException, DataException,
 			ConnectionException, ProtocolException, SecurityException, AACException {
 		if (portfolioEntityId == null)
 			return null;
 		List<Portfolio> list = getPortfolioList();
 		if (list != null)
 			for (Portfolio p : list)
 				if (portfolioEntityId.equals(p.entityId))
 					return p;
 		return null;
 	}
 
 	public static Portfolio findPortfolio(String portfolioEntityId) throws NameNotFoundException, DataException,
 			ConnectionException, ProtocolException, SecurityException, AACException {
 		Portfolio p = getPortfolio(portfolioEntityId);
 		if (p == null) {
 			MessageRequest request = new MessageRequest(Preferences.getHost(mContext), Preferences.getService()
 					+ "/eu.trentorise.smartcampus.portfolio.models.Portfolio/entity/" + portfolioEntityId);
 			request.setMethod(Method.GET);
 			MessageResponse response = new ProtocolCarrier(mContext, Preferences.getAppToken()).invokeSync(request,
 					Preferences.getAppToken(), getAuthToken());
 
 			p = Utils.convertJSONToObject(response.getBody(), Portfolio.class);
 
 		}
 		return p;
 	}
 
 	public static void resetData() throws DataException{
 		getInstance().portfolioList = null;
 		getInstance().hasStudentExams = null;
 		getInstance().hasStudentInfo = null;
 		getInstance().studentExams = null;
 		getInstance().studentInfo = null;
 		getInstance().userData = null;
 		getInstance().userProducedData = null;
 	}
 	public static Boolean isOwnPortfolio(String portfolioEntityId) throws NameNotFoundException, DataException,
 			ConnectionException, ProtocolException, SecurityException, AACException {
 		if (portfolioEntityId == null)
 			return false;
 		List<Portfolio> list = getPortfolioList();
 		if (list != null)
 			for (Portfolio p : list)
 				if (portfolioEntityId.equals(p.entityId))
 					return true;
 		return false;
 	}
 
 	public static StudentInfo getStudentInfo() throws DataException, NameNotFoundException, ConnectionException,
 			ProtocolException, SecurityException, AACException {
 		if (getInstance().hasStudentInfo == null) {
 			if (config.isTestModeEnabled()) {
 				String rawData = RawUtil.getRawAsString(mContext, R.raw.student_info_test);
 				getInstance().studentInfo = Utils.convertJSONToObject(rawData, StudentInfo.class);
 			} else {
 				Collection<StudentInfo> coll = getRemoteStorage().getObjects(StudentInfo.class);
 				if (coll == null || coll.isEmpty()) {
 					getInstance().studentInfo = null;
 				} else {
 					getInstance().studentInfo = coll.iterator().next();
 				}
 			}
 			getInstance().hasStudentInfo = getInstance().studentInfo != null;
 		}
 		return getInstance().studentInfo;
 	}
 
 	public static List<SemanticSuggestion> getTagSuggestions(CharSequence txt) {
 		try {
 			return SuggestionHelper.getSuggestions(txt, mContext, Preferences.getHost(mContext), getAuthToken(),
 					Preferences.getAppToken());
 		} catch (Exception e) {
 			return Collections.emptyList();
 		}
 	}
 
 	public static byte[] exportPortfolio(String portfolioId) throws ConnectionException, ProtocolException,
 			SecurityException, AACException {
 		MessageRequest request = new MessageRequest(Preferences.getHost(mContext), Preferences.getService()
 				+ "/generatecv/" + portfolioId + "/pdf/false");
 		request.setMethod(Method.GET);
 		request.setRequestFile(true);
 		MessageResponse response = new ProtocolCarrier(mContext, Preferences.getAppToken()).invokeSync(request,
 				Preferences.getAppToken(), getAuthToken());
 		return response.getFileContent();
 
 	}
 
 	public static StudentInfo getSharedStudentInfo(String id) throws ConnectionException, ProtocolException,
 			SecurityException, AACException {
 		MessageRequest request = new MessageRequest(Preferences.getHost(mContext), Preferences.getService()
 				+ "/eu.trentorise.smartcampus.portfolio.models.StudentInfo/portfolio/" + id);
 		request.setMethod(Method.GET);
 		MessageResponse response = new ProtocolCarrier(mContext, Preferences.getAppToken()).invokeSync(request,
 				Preferences.getAppToken(), getAuthToken());
 
 		return Utils.convertJSONToObject(response.getBody(), StudentInfo.class);
 	}
 
 	public static ArrayList<StudentExams> getSharedStudentExams(String id) throws ConnectionException,
 			ProtocolException, SecurityException, AACException {
 		MessageRequest request = new MessageRequest(Preferences.getHost(mContext), Preferences.getService()
 				+ "/eu.trentorise.smartcampus.portfolio.models.StudentExams/portfolio/" + id);
 		request.setMethod(Method.GET);
 		MessageResponse response = new ProtocolCarrier(mContext, Preferences.getAppToken()).invokeSync(request,
 				Preferences.getAppToken(), getAuthToken());
 
 		List<StudentExams> list = Utils.convertJSONToObjects(response.getBody(), StudentExams.class);
 		if (list != null)
 			return new ArrayList<StudentExams>(list);
 		return new ArrayList<StudentExams>();
 	}
 
 	public static ArrayList<UserProducedData> getSharedUserProducedDatas(String id) throws ConnectionException,
 			ProtocolException, SecurityException, AACException {
 		MessageRequest request = new MessageRequest(Preferences.getHost(mContext), Preferences.getService()
 				+ "/eu.trentorise.smartcampus.portfolio.models.UserProducedData/portfolio/" + id);
 		request.setMethod(Method.GET);
 		MessageResponse response = new ProtocolCarrier(mContext, Preferences.getAppToken()).invokeSync(request,
 				Preferences.getAppToken(), getAuthToken());
 
 		List<UserProducedData> list = Utils.convertJSONToObjects(response.getBody(), UserProducedData.class);
 		if (list != null)
 			return new ArrayList<UserProducedData>(list);
 		return new ArrayList<UserProducedData>();
 	}
 
 	public static SharedPortfolioContainer getSharedPortfolioContainer(String id) throws ConnectionException,
 			ProtocolException, SecurityException, AACException {
 		MessageRequest request = new MessageRequest(Preferences.getHost(mContext), Preferences.getService()
 				+ "/eu.trentorise.smartcampus.portfolio.models.SharedPortfolioContainer/" + id);
 		request.setMethod(Method.GET);
 		MessageResponse response = new ProtocolCarrier(mContext, Preferences.getAppToken()).invokeSync(request,
 				Preferences.getAppToken(), getAuthToken());
 
 		return Utils.convertJSONToObject(response.getBody(), SharedPortfolioContainer.class);
 	}
 
 	public static void share(Portfolio exp, Activity ctx) {
 		Entity obj = new Entity();
 		obj.setEntityId(exp.entityId);
 		obj.setTitle(exp.name);
 		obj.setEntityType(ENTITY_TYPE_PORTFOLIO);
 		Intent intent = new Intent();
 		intent.setAction(ctx.getString(eu.trentorise.smartcampus.android.common.R.string.share_intent_action));
 		intent.putExtra(ctx.getString(eu.trentorise.smartcampus.android.common.R.string.share_entity_arg_entity), obj);
 		AppHelper.startActivityForApp(intent, ctx);
 	}
 
 	private static SharedPreferences getPFBrowserPreferences(Context ctx) {
 		SharedPreferences out = ctx.getSharedPreferences(BROWSER_PREFS, Context.MODE_PRIVATE);
 		return out;
 	}
 
 	private static boolean isPFBrowserFirstLaunch(Context context) {
 		return getPFBrowserPreferences(context).getBoolean(FIRST_LAUNCH_PREFS, true);
 	}
 
 	private static void setFlagOpenPFinBrowser(Context context) {
 		Editor edit = getPFBrowserPreferences(context).edit();
 		edit.putBoolean(FIRST_LAUNCH_PREFS, false);
 		edit.commit();
 	}
 
 	private static void shoWarningDialog(final Context context) {
 		View checkBoxView = View.inflate(context, R.layout.open_pf_browser, null);
 		CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
 		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 
 				write_preferences = isChecked;
 			}
 		});
 		checkBox.setText(context.getString(R.string.open_browser_checkbox));
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setTitle(context.getString(R.string.open_browser_title));
 		builder.setMessage(context.getString(R.string.open_browser_message)).setView(checkBoxView).setCancelable(false)
 				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						if (write_preferences)
 							PMHelper.setFlagOpenPFinBrowser(context);
 						startPFBrowser(context);
 					}
 				}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						if (write_preferences)
 							PMHelper.setFlagOpenPFinBrowser(context);
 						dialog.cancel();
 					}
 				}).show();
 
 	}
 
 	public static void startPFBrowser(Context context) {
 		Intent browserIntent;
 		try {
 			browserIntent = new Intent(Intent.ACTION_VIEW,
 					Uri.parse(Preferences.getWebAddress(mContext)+"?token=" + getAuthToken()));
 			context.startActivity(browserIntent);
 
 		} catch (Exception e) {
 			showFailure(context, R.string.app_failure_operation);
 		}
 
 	}
 
 	public static void openPortfolioInBrowser(Context context) {
 		if (PMHelper.isPFBrowserFirstLaunch(context)) {
 			shoWarningDialog(context);
 		} else {
 			startPFBrowser(context);
 		}
 	}
 }
