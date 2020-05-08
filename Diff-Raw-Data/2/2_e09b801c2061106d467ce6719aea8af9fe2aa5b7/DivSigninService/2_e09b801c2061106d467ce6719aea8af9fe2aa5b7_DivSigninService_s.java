 package com.njnu.kai.divsignin;
 
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.protocol.HTTP;
 
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.util.Log;
 
 import com.njnu.kai.divsignin.common.DivConst;
 import com.njnu.kai.divsignin.common.HttpUtility;
 
 public class DivSigninService extends Service {
 
 	private static String PREFIX = "DivSigninService";
 	private boolean mThreadContinue = true;
 	private String mDevAccountUid;
 	private String mDevAccountName;
 	private String mDevAccountPw;
 	private String mQiangLouTitle;
 	private String mQiangLouContent;
 	private int mOpenUrlFailedTimes = 0;
 
 	private Thread mThreadQiangLou = new Thread(new Runnable() {
 
 		@Override
 		public void run() {
 			Log.i(PREFIX, "run");
 			try {
 				doThreadQiangLou();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	});
 
 	private void doThreadQiangLou() throws InterruptedException {
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 		mDevAccountUid = settings.getString("div_user_uid", "");
 		mDevAccountName = settings.getString("div_user_name", "");
 		mDevAccountPw = settings.getString("div_user_pw", "");
 		mQiangLouTitle = settings.getString("divsignin_qianglou_title", "");
 		mQiangLouContent = settings.getString("divsignin_qianglou_content", "");
 		if (TextUtils.isEmpty(mDevAccountUid) || TextUtils.isEmpty(mDevAccountName) || TextUtils.isEmpty(mDevAccountPw)) {
 			sendNotifyBroadcast(1, "Error: Check your account.");
 			stopSelf();
 			return;
 		}
 		if (TextUtils.isEmpty(mQiangLouTitle) || TextUtils.isEmpty(mQiangLouContent)) {
 			sendNotifyBroadcast(1, "Error: Need Qianglou title and content.");
 			stopSelf();
 			return;
 		}
 
 		sendNotifyBroadcast("title=" + mQiangLouTitle + " content=" + mQiangLouContent);
 		sendNotifyBroadcast("Start，Check Cookie ? [" + mDevAccountName + "]");
 		if (IsValidDevdCookie()) {
 			sendNotifyBroadcast("Success, Cookie Valid.");
 		} else {
 			sendNotifyBroadcast("need login, login...");
 			boolean loginSuccess = loginDevdBBs();
 			if (loginSuccess) {
 				sendNotifyBroadcast("login Success, will QiangLou...");
 			} else {
 				sendNotifyBroadcast(1, "Error: Login Failed.");
 				stopSelf();
 				return;
 			}
 		}
 
 		int qlResult = doTaskQiangLou(); // 0:Success, 1:need reply others subject, else Error need stop.
 		if (!mThreadContinue) return;
 		if (qlResult == 0) {
 			sendNotifyBroadcast(2, "Congratulations, Qianglou success.");
 			stopSelf();
 			return;
 		} else if (qlResult == 1) {
 			sendNotifyBroadcast("Qianglou failed, will reply others subject.");
 		} else {
 			sendNotifyBroadcast(3, "Qianglou Error, Terminate task.");
 			stopSelf();
 			return;
 		}
 
 		int replyResult = doTaskReplyOthersSubject(); // 0:Success 1:Error
 		if (!mThreadContinue) return;
 		if (replyResult == 0) {
 			sendNotifyBroadcast(4, "Congratulations, Reply others subject and get score success.");
 		} else {
 			sendNotifyBroadcast(4, "Reply others subject or get score Error, Terminate task.");
 		}
 		stopSelf();
 
 		// int i = 0;
 		// while (mThreadContinue) {
 		// ++i;
 		// // Log.i(PREFIX, "doThreadQiangLou " + (++i));
 		// try {
 		// Thread.sleep(1000);
 		// } catch (InterruptedException e) {
 		// e.printStackTrace();
 		// }
 		// }
 	}
 
 	private int doTaskReplyOthersSubject() throws InterruptedException {
 		mOpenUrlFailedTimes = 0;
 		int breakReason = 0;
 		while (true) {
 			String _lastContent = HttpUtility.GetUseAutoEncoding("http://www.devdiv.com/forum-151-1.html");
 			if (_lastContent.indexOf("\u62a2\u697c\u7b7e\u5230\u002d\u0044\u0045\u0056\u0044\u0049\u0056\u002e\u0043\u004f\u004d") < 0) {
 				breakReason = dealHttpFailed(-100, "open board qianglou failed, will retry...", "Error: Open board qianglou failed");
 				if (breakReason == 0)
 					continue;
 				else
 					break;
 			} else {
 				sendNotifyBroadcast("open board qianglou sucess.");
 				mOpenUrlFailedTimes = 0;
 			}
 
 			if (!mThreadContinue) break;
 
 			int topicIndex = _lastContent.indexOf("版块主题");
 			_lastContent = _lastContent.substring(topicIndex, topicIndex + 10000);
 			Pattern pattern = Pattern.compile("版块主题<.+?<a href=\"(.+?)\" title=\"(有新回复 - ){0,1}新窗口打开\" target=\"_blank\">", Pattern.DOTALL);
 			Matcher matcher = pattern.matcher(_lastContent);
 			matcher.find();
 			String firstFloorUrl = matcher.group(1);
 			// Log.i(PREFIX, firstFloorUrl);
 			sendNotifyBroadcast("get qianglou 首帖 Sucess.");
 
 			while (true) {
 				if (!mThreadContinue) break;
 				
 				_lastContent = HttpUtility.GetUseAutoEncoding(firstFloorUrl);
 				if (_lastContent.indexOf("\u6240\u6709\u4f1a\u5458\u5747\u53ef\u62a2\u697c\u548c\u7b7e\u5230") < 0) {
 					breakReason = dealHttpFailed(-101, "Open 跟帖 url Failed, will retry...", "Error: Open 跟帖 url Failed.");
 					if (breakReason == 0)
 						continue;
 					else
 						break;
 				} else {
 					mOpenUrlFailedTimes = 0;
 					sendNotifyBroadcast("Open 跟帖 url Sucess, reply...");
 					if (!mThreadContinue) break;
 					
 					pattern = Pattern.compile("<form method=\"post\" autocomplete=\"off\" id=\"fastpostform\" action=\"(.+?)\" onSubmit",
 							Pattern.DOTALL);
 					_lastContent = _lastContent.substring(_lastContent.indexOf("<form method=\"post\""));
 					matcher = pattern.matcher(_lastContent);
 					matcher.find();
 					String postUrl = matcher.group(1);
 					postUrl = "http://www.devdiv.com/" + postUrl.replace("&amp;", "&") + "&inajax=1";
 //					Log.i(PREFIX, postUrl);
 					pattern = Pattern.compile("<input type=\"hidden\" name=\"formhash\".*? value=\"(\\w+?)\" />");
 					matcher = pattern.matcher(_lastContent);
 					matcher.find();
 					String formhash = matcher.group(1);
 					
 					pattern = Pattern.compile("<input type=\"hidden\" name=\"posttime\".*? value=\"(\\w+?)\" />");
 					matcher = pattern.matcher(_lastContent);
 					matcher.find();
 					String posttime = matcher.group(1);
 					
 					String postData = "formhash=" + formhash + "&subject=&message=" + mQiangLouContent + "&posttime=" + posttime;
 					_lastContent = HttpUtility.PostUseAutoEncoding(postUrl, postData, "GBK");
 					if (_lastContent.indexOf("succeedhandle_fastpost") < 0) {
 						breakReason = dealHttpFailed(-102, "Reply 跟帖 Failed, will retry...", "Error: Reply 跟帖 Failed.");
 						if (breakReason == 0)
 							continue;
 						else
 							break;
 					} else {
 						mOpenUrlFailedTimes = 0;
 						sendNotifyBroadcast("reply 跟帖 sucess, will get score.");
 						
 						pattern = Pattern.compile("&tid=(\\d+?)&");
 						matcher = pattern.matcher(_lastContent);
 						matcher.find();
 						String tidStr = matcher.group(1);
 						postUrl = "http://www.devdiv.com/plugin.php?id=donglin8_signin:submit&tid=" + tidStr;
 						
 						while (true) {
 							if (!mThreadContinue) break;
 							
 							_lastContent = HttpUtility.GetUseAutoEncoding(postUrl);
 							if (_lastContent.indexOf(mDevAccountName) < 0) {
 								breakReason = dealHttpFailed(-103, "Get score failed, will retry...", "Error: Get score Failed.");
 								if (breakReason == 0)
 									continue;
 								else
 									break;
 							} else {
 								mOpenUrlFailedTimes = 0;
 								
 								sendNotifyBroadcast("Get score sucess.");
 							}
 							break;
 						}
 					}
 				}
 				break;
 			}
 
 			break;
 		}
 		return breakReason;
 	}
 
 	private int dealHttpFailed(int breakNo, String warningText, String errorText) throws InterruptedException {
 		int dealResult = 0;
 		++mOpenUrlFailedTimes;
 		if (mOpenUrlFailedTimes > 5) {
 			sendNotifyBroadcast(breakNo, errorText);
 			dealResult = breakNo;
 		} else {
 			sendNotifyBroadcast(warningText);
 			Thread.sleep(2000);
 		}
 		return dealResult;
 	}
 
 	private int doTaskQiangLou() throws InterruptedException {
 		int breakReason = 0;
 		mOpenUrlFailedTimes = 0;
 		int retryQiangInterval = 0;
 		while (true) {
 
 			if (!mThreadContinue) break;
 
 			String _lastContent = HttpUtility
 					.GetUseAutoEncoding("http://www.devdiv.com/forum.php?mod=post&action=newthread&fid=151&specialextra=donglin8_signin");
 			if (_lastContent.indexOf(mDevAccountName) < 0) {
 				breakReason = dealHttpFailed(-1, "Open qianglou url failed, will retry...", "Error: Open qianglou url failed.");
 				if (breakReason == 0)
 					continue;
 				else
 					break;
 			} else {
 				sendNotifyBroadcast("Open qianglou url Success.");
 				mOpenUrlFailedTimes = 0;
 			}
 
 			if (!mThreadContinue) break;
 
 			Pattern pattern = Pattern.compile("<input type=\"hidden\" name=\"formhash\".+? value=\"(\\w+?)\" />");
 			Matcher match = pattern.matcher(_lastContent);
 			match.find();
 			String formhash = match.group(1);
 
 			pattern = Pattern.compile("<input type=\"hidden\" name=\"posttime\".+? value=\"(\\w+?)\" />");
 			match = pattern.matcher(_lastContent);
 			match.find();
 			String posttime = match.group(1);
 
 			String postData = String
 					.format("formhash=%s&posttime=%s&wysiwyg=0&special=127&specialextra=donglin8_signin&subject=%s&checkbox=0&message=%s&replycredit_extcredits=0&replycredit_times=1&replycredit_membertimes=1&replycredit_random=100&readperm=&tags=&rushreplyfrom=&rushreplyto=&rewardfloor=&stopfloor=&save=&uploadalbum=307&newalbum=&usesig=1&allownoticeauthor=1",
 							formhash, posttime, mQiangLouTitle, mQiangLouContent);
 			String postUrl = "http://www.devdiv.com/forum.php?mod=post&action=newthread&fid=151&extra=&topicsubmit=yes";
 			_lastContent = HttpUtility.PostUseAutoEncoding(postUrl, postData, "GBK");
 			if (!mThreadContinue) break;
 			if (_lastContent.indexOf("对不起，已有人先你") > 0) {
 				sendNotifyBroadcast("Sorry, Too Late, will reply.");
 				breakReason = 1;
 				break;
 			} else if (_lastContent.indexOf("未到\u62a2\u697c时间") > 0) {
 				if (!mThreadContinue) break;
 				pattern = Pattern.compile("GMT\\+8, \\d+?(-\\d+?){2} (\\d+?):(\\d+?)<");
 				match = pattern.matcher(_lastContent);
 				match.find();// GMT+8, 2012-7-9 00:33<span id="debuginfo">
 				int curTimeH = Integer.parseInt(match.group(2));
 				int curTimeM = Integer.parseInt(match.group(3));
 				if (curTimeH < 7 || curTimeM < 58) {
 					retryQiangInterval = 10000;
 				} else {
 					retryQiangInterval = (retryQiangInterval > 1000) ? 1000 : retryQiangInterval;
 					retryQiangInterval -= 40;
 					retryQiangInterval = (retryQiangInterval < 30) ? 30 : retryQiangInterval;
 				}
 //				Log.i(PREFIX, "server curTime=" + curTimeH + ":" + curTimeM + " retryQiangInterval=" + retryQiangInterval);
 				sendNotifyBroadcast("QiangLou not begin(" + curTimeM + "), will try again after " + retryQiangInterval + "ms.");
 				Thread.sleep(retryQiangInterval);
 				continue;
 			} else if (_lastContent.indexOf("无法提交") > 0) {
 				sendNotifyBroadcast("无法提交，可能验证码问题.");
 				breakReason = -2;
 				break;
 			} else if (_lastContent.indexOf(mQiangLouContent) > 0) {
 				breakReason = 0;
 				break;
 			} else {
 				breakReason = dealHttpFailed(-3, "Open qianglou url failed(unknow error), will retry...", "Unknow Error");
 				if (breakReason == 0)
 					continue;
 				else
 					break;
 			}
 		}
 		return breakReason;
 	}
 
 	private boolean loginDevdBBs() {
 		String _lastContent = HttpUtility.GetUseAutoEncoding("http://www.devdiv.com/member.php?mod=logging&action=login");
 		if (_lastContent == null || _lastContent.indexOf("登录 -  DEVDIV.COM") < 0) {
 			return false;
 		}
 		// AddLineToResult(_lastContent);
 		Pattern pattern = Pattern.compile("<input type=\"hidden\" name=\"formhash\" value=\"(\\w+?)\" />");
 		Matcher match = pattern.matcher(_lastContent);
 		match.find();
 		String formhash = match.group(1);
 
 		pattern = Pattern.compile("action=\"(member.php\\?mod=logging.+?)\">");
 		match = pattern.matcher(_lastContent);
 		match.find();
 		String posturl = match.group(1).replace("&amp;", "&");
 
 		pattern = Pattern.compile("value=\"(\\d+?)\"  />自动登录</label>");
 		match = pattern.matcher(_lastContent);
 		match.find();
 		String cookietime = match.group(1);
 
 		String md5pw = HttpUtility.getMD5(mDevAccountPw.getBytes());
 		// List<NameValuePair> formparams = new ArrayList<NameValuePair>();
 		// formparams.add(new BasicNameValuePair("formhash", formhash));
 		// formparams.add(new BasicNameValuePair("referer", ""));
 		// formparams.add(new BasicNameValuePair("username", devUserAccount));
 		// formparams.add(new BasicNameValuePair("password", md5pw));
 		// formparams.add(new BasicNameValuePair("questionid", "0"));
 		// formparams.add(new BasicNameValuePair("answer", ""));
 		// formparams.add(new BasicNameValuePair("cookietime", cookietime));
 		String postdata = String.format("formhash=%s&referer=&username=%s&password=%s&questionid=0&answer=&cookietime=%s", formhash, mDevAccountName,
 				md5pw, cookietime);
 		// AddLineToResult(posturl + " " + postdata);
 		Log.i(PREFIX, "postData=" + postdata);
 		_lastContent = HttpUtility.PostUseAutoEncoding("http://www.devdiv.com/" + posturl, postdata, HTTP.UTF_8);
 		// AddLineToResult(_lastContent);
 		return _lastContent != null && _lastContent.indexOf("欢迎您回来") >= 0;
 	}
 
 	private boolean IsValidDevdCookie() {
		String needStr = String.format("<a href=\"http://www.devdiv.com/space-uid-%s.html\" target=\"_blank\" title=\"访问我的空间\">%s</a>",
 				mDevAccountUid, mDevAccountName);
 		String _lastContent = HttpUtility.GetUseAutoEncoding("http://www.devdiv.com/forum-154-1.html");
 		// Log.i(PREFIX, _lastContent);
 		// sendNotifyBroadcast(_lastContent);
 		return _lastContent != null && _lastContent.indexOf(needStr) >= 0;
 	}
 
 	private void sendNotifyBroadcast(String message) {
 		sendNotifyBroadcast(0, message);
 	}
 
 	private void sendNotifyBroadcast(int type, String message) {
 		Intent intent = new Intent(DivConst.ACTION_QIANGLOU_NOTIFY);
 		intent.putExtra("type", type);
 		intent.putExtra("message", message);
 		sendBroadcast(intent);
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		Log.i(PREFIX, "onCreate");
 		super.onCreate();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Thread.State state = mThreadQiangLou.getState();
 		Log.i(PREFIX, "thread state=" + state);
 		if (mThreadQiangLou.getState() == Thread.State.NEW) {
 			mThreadQiangLou.start();
 		} else {
 			// mThreadContinue = false;
 		}
 		return START_NOT_STICKY;
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.i(PREFIX, "onDestroy");
 		mThreadContinue = false;
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onUnbind(Intent intent) {
 		Log.i(PREFIX, "onUnbind");
 		return super.onUnbind(intent);
 	}
 
 }
