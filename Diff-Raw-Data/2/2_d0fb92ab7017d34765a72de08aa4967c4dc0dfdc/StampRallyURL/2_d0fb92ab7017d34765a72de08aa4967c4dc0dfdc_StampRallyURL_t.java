 package jag.kumamoto.apps.StampRally.Data;
 
 import java.net.URLEncoder;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * 
  * サーバへの問い合わせクエリを生成するクラス
  * 
  * @author aharisu
  *
  */
 public final class StampRallyURL {
 	private static final String StampRallyHostURL = "http://kumamotogotochi.appspot.com/client";
 
 	private static final String AppTokenParamName = "appKey";
 	private static final String AppTokenKey = "ca3244d3-ef19-4bb1-8fd1-120ff4cabb3f";
 	
 	
 	/**
 	 * ピンの取得
 	 * @return 
 	 */
 	public static final String getGetAllPinQuery() {
 		return new StringBuilder(StampRallyHostURL).append("/pins?")
 			.append(AppTokenParamName).append("=").append(AppTokenKey)
 			.toString();
 	}
 	
 	/**
 	 * クイズの取得
 	 * @param pin
 	 * @return
 	 */
 	public static String getQuizesQuery(StampPin pin) {
 		return new StringBuilder(StampRallyHostURL).append("/quizes?")
 			.append(AppTokenParamName).append("=").append(AppTokenKey)
 			.append("&pinId=").append(pin)
 			.toString();
 	}
 	
 	/**
 	 * ユーザ登録
 	 * @param user
 	 * @return
 	 */
 	public static String getRegistrationQuery(User user) {
 		return new StringBuilder(StampRallyHostURL).append("/registration?")
 			.append(AppTokenParamName).append("=").append(AppTokenKey)
 			.append("&token=").append(user.token)
 			.append("&gender=").append(user.gender)
 			.append("&nickname=").append(URLEncoder.encode(user.nickname))
 			.toString();
 	}
 	
 	/***
 	 * ユーザ情報取得
 	 * @param token
 	 * @return
 	 */
 	public static String getUserInfoQuery(String token) {
 		return new StringBuilder(StampRallyHostURL).append("/user?")
 			.append(AppTokenParamName).append("=").append(AppTokenKey)
 			.append("&token=").append(token)
 			.toString();
 	}
 	
 	/**
 	 * ユーザの履歴情報取得
 	 * @param user
 	 * @param pinsOnly
 	 * @return
 	 */
 	public static String getUserHistoryQuery(User user, boolean pinsOnly) {
 		return new StringBuilder(StampRallyHostURL).append("/user?")
 			.append(AppTokenParamName).append("=").append(AppTokenKey)
 			.append("&token=").append(user.token)
 			.append("&pinsOnly=").append(pinsOnly ? "true" : "false")
 			.toString();
 	}
 	
 	/**
 	 * 受賞情報の取得
 	 * @param user
 	 * @return
 	 */
 	public static String getPrizesQuery(User user) {
 		return new StringBuilder(StampRallyHostURL).append("/prizes?")
 			.append(AppTokenParamName).append("=").append(AppTokenKey)
 			.append("&token=").append(user.token)
 			.toString();
 	}
 	
 	/**
 	 * 解答送信
 	 * @param user
 	 * @param quiz
 	 * @param correctness
 	 * @param answeringTime
 	 * @param isCheckedAry
 	 * @return
 	 */
 	public static String getLoggingQuery(User user, QuizData quiz,
 			boolean correctness, long answeringTime, boolean[] isCheckedAry) {
 		StringBuilder builder = new StringBuilder(StampRallyHostURL).append("/answer?")
 			.append(AppTokenParamName).append("=").append(AppTokenKey)
 			.append("&token=").append(user.token)
 			.append("&pinId=").append(quiz.pinId)
 			.append("&quizId=").append(quiz.id)
 			.append("&correctness=").append(correctness ? 1 : 0)
 			.append("&answeringTime=").append(answeringTime);
 		
 		for(int i = 0;i < isCheckedAry.length;++i) {
 			if(isCheckedAry[i]) {
 				builder.append("&optionIdArray=").append(quiz.choices.getChoice(i).id);
 			}
 		}
 		
 		return builder.toString();
 	}
 	
 	/**
 	 * 到着送信
 	 * @param user
 	 * @param pin
 	 * @return
 	 */
 	public static String getArriveQuery(User user, StampPin pin) {
 		return new StringBuilder(StampRallyHostURL).append("/arrive?")
 			.append(AppTokenParamName).append("=").append(AppTokenKey)
			.append("&token=").append(user.token)
 			.append("&pinId=").append(pin.id)
 			.toString();
 	}
 	
 	/**
 	 * 通信が正常に成功したかを確認する
 	 * @param obj
 	 * @return
 	 * @throws JSONException
 	 */
 	public static boolean isSuccess(JSONObject obj) throws JSONException {
 		return obj.getString("status").equals("OK") && obj.getString("success").equals("true");
 	}
 		
 }
