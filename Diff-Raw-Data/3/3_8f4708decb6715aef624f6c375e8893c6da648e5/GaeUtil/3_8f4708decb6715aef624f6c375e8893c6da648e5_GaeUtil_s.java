 package ydeb_a10.android.Audiotest;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.HttpRequestExecutor;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONObject;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.accounts.AuthenticatorException;
 import android.accounts.OperationCanceledException;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.Toast;
 
 
 public class GaeUtil {
 	static final String GAE_APP_URI = "https://ydeb-a10.appspot.com";
 	static String acsid = null;
 
 	public static final String GAE_SERVER = "ydeb-a10.appspot.com";
 	public static final String BASE_PATH = "/api/posts/";
 	public static final String BASE_URL = "https://" + GAE_SERVER + BASE_PATH;
 	public static final int BASE_LENGTH = BASE_URL.length();
 
 	/**
 	 * 認証。アプリの起動直後に呼ばれる必要アリ。
 	 * 
 	 * @param context
 	 */
 	public static void authorization(Context context) throws Exception {
 		AccountManager accountManager = AccountManager.get(context);
 		Account[] accounts = accountManager.getAccountsByType("com.google");
 
		if (accounts.length >= 0) {
 			Account account = accounts[0];
 			accountManager.getAuthToken(account, "ah", false,
 					new LoginCallback(context), null);
 		}
 	}
 	
 	/**
 	 * 指定されたIDを持つ投稿のデータをJSONで取得する
 	 * 
 	 * @param aid
 	 *            ありがとうID
 	 * @return JSONデータ
 	 * @throws Exception
 	 */
 	public static JSONObject getPostInfo(String aid) throws Exception {
 		// TODO 投稿情報取得するぞ
 		HttpRequestExecutor executor = new HttpRequestExecutor();
 		HttpResponse execute = executor.execute(new HttpGet(BASE_URL + aid
 				+ "/json"), null, null);
 		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
 		execute.getEntity().writeTo(outstream);
 		String string = outstream.toString(execute.getEntity()
 				.getContentEncoding().getValue());
 		JSONObject json = new JSONObject(string);
 
 		return json;
 	}
 
 	/**
 	 * 指定されたIDを持つ投稿の音声データをバイナリで取得する
 	 * 
 	 * @param aid
 	 *            ありがとうID
 	 * @return 音声データ
 	 * @throws Exception
 	 */
 	public static byte[] getPostMessage(String aid) throws Exception {
 		// TODO 音声取得するぞ
 		return new byte[0];
 	}
 
 	/**
 	 * 自身の投稿一覧をJSONで取得する
 	 * 
 	 * @return JSONデータ
 	 * @throws Exception
 	 */
 	public static JSONObject getMyPosts() throws Exception {
 		// TODO 一覧取得するぞ
 		JSONObject json = new JSONObject("");
 		return json;
 	}
 
 	/**
 	 * 新しい投稿を行い、割り振られたIDをJSONデータとして取得する。
 	 * 
 	 * @param message
 	 *            音声データ
 	 * @param memo
 	 *            メモ
 	 * @param parentAid
 	 *            親ID（ない場合はnull）
 	 * @return JSONデータ
 	 * @throws Exception
 	 */
 	public static JSONObject post(String filepath, String memo, String parentAid)
 			throws Exception {
 		// TODO データ送信するぞ
 		DefaultHttpClient client = new DefaultHttpClient();
 		client.getParams().setBooleanParameter(
 				ClientPNames.HANDLE_REDIRECTS, false);
 
 		String uri = GAE_APP_URI + "/api/post";
 
 		HttpPost httpPost = new HttpPost(uri);
 		httpPost.setHeader("Cookie", acsid);
 		
 /*		File file = new File(filepath);
 
 	    List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
    	    nameValuePair.add(new BasicNameValuePair("memo", memo));
    	    nameValuePair.add(new BasicNameValuePair("raid", parentAid));
 		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
 
 		FileEntity entity;
 		entity = new FileEntity(file,"binary/octet-stream");
 		entity.setChunked(true);
 		httpPost.setEntity(entity);
 		httpPost.addHeader("data", "upload.3gp");*/
 		
 		// マルチパートフォーム
         final MultipartEntity reqEntity =
              new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
         // memo項目
         reqEntity.addPart("memo",
                 new StringBody(memo, Charset.forName("UTF-8")));
         // parentAid項目
         reqEntity.addPart("raid",
                 new StringBody(parentAid, Charset.forName("UTF-8")));
         // ファイル項目
         final File upfile = new File(filepath);
         reqEntity.addPart("data",
                 new FileBody(upfile, "application/octet-stream"));
 
         httpPost.setEntity(reqEntity);
 
 		JSONObject json = null;
 		HttpResponse response = client.execute(httpPost);
 	    int statusCode = response.getStatusLine().getStatusCode();
 	    if (statusCode == HttpStatus.SC_OK) {
 	        String data = EntityUtils.toString(response.getEntity());
 			json = new JSONObject(data);
 	    }
 		return json;
 	}
 
 	/**
 	 * 
 	 * @deprecated 外からこのクラスを使用する必要はありません
 	 */
 	@Deprecated
 	public static class LoginCallback implements
 			AccountManagerCallback<Bundle> {
 
 		private Context context;
 
 		public LoginCallback(Context context) {
 			this.context = context;
 		}
 
         public void removeAuthTokenCache(Bundle bundle, String authToken) {
             String accountType = bundle.getString(AccountManager.KEY_ACCOUNT_TYPE);
             AccountManager manager = AccountManager.get(context);
             manager.invalidateAuthToken(accountType, authToken);
         }
 
         public void run(AccountManagerFuture<Bundle> result) {
 			Bundle bundle;
 			try {
 				bundle = result.getResult();
 				Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
 				if (intent != null) {
 					this.context.startActivity(intent);
 				} else {
 		            final int RETRY_MAX = 3;
 		            boolean isValidToken = false;
 
 		            int    retry = 0;
 		            
 		            String authToken = bundle
 							.getString(AccountManager.KEY_AUTHTOKEN);
 
 					DefaultHttpClient client = null;
 					while (!isValidToken) {
 
 					    client = new DefaultHttpClient();
 					    client.getParams().setBooleanParameter(
 							ClientPNames.HANDLE_REDIRECTS, false);
 
     					String uri = GAE_APP_URI
     							+ "/_ah/login?continue=/api/&auth=" + authToken;
     
     			        HttpGet httpGet = new HttpGet(uri);
     					HttpResponse response = client.execute(httpGet);
     				    int statusCode = response.getStatusLine().getStatusCode();
                         if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                             // 認証トークンキャッシュの削除
                             //   期限切れ、もしくは、認証リクエストが無効になるような、認証トークンが見つかった場合、
                             //   キャッシュのクリアを行う
                             removeAuthTokenCache(bundle, authToken);                            
                         } else {
                             isValidToken = true;
                         }
                         retry++;
                         if (retry > RETRY_MAX) {
                             break;
                         }
 				    }
                     if (isValidToken) {
                         for (Cookie cookie : client.getCookieStore().getCookies()) {
                             if ("SACSID".equals(cookie.getName())
                                     || "ACSID".equals(cookie.getName())) {
                                 acsid = cookie.getName() + "=" + cookie.getValue();
                             }
                         }
                     }
 				}
 			} catch (OperationCanceledException e) {
 				Toast.makeText(this.context, "lbl_operation_canceled",
 						Toast.LENGTH_LONG).show();
 			} catch (AuthenticatorException e) {
 				Toast.makeText(this.context, "lbl_authenticator_failed",
 						Toast.LENGTH_LONG).show();
 			} catch (IOException e) {
 				Toast.makeText(this.context, "lbl_error", Toast.LENGTH_LONG)
 						.show();
 			}
 		}
 	}
 }
