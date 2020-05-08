 package com.luzi82.libmbgwalpurgis;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Executor;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.ProtocolException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultRedirectStrategy;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class MwClient implements IMwClient {
 
 	public static final String ATTACK_CONF_URL = "http://sp.pf.mbga.jp/12012090/?url=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fraid_boss_attack_conf.php";
 
 	final Executor mExecutor;
 
 	public MwClient(Executor aExecutor) {
 		mExecutor = aExecutor;
 	}
 
 	@Override
 	public void connect(String aLoginId, String aPassword, ICallback<Void> aCallback, ICallback<Exception> aException) {
 		mStateOp.connect(aLoginId, aPassword, aCallback, aException);
 	}
 
 	@Override
 	public void disconnect(ICallback<Void> aCallback) {
 		mStateOp.disconnect(aCallback);
 	}
 
 	@Override
 	public String getLoginId() {
 		return mStateOp.getLoginId();
 	}
 
 	@Override
 	public State getState() {
 		return mStateOp.getState();
 	}
 
 	@Override
 	public void getFeed(ICallback<RaidBossMatchingFeed> aCallback, ICallback<Exception> aExceptionCallback) {
 		mStateOp.getFeed(aCallback, aExceptionCallback);
 	}
 
 	@Override
 	public void getStatus(ICallback<PlayerStatus> aCallback, ICallback<Exception> aExceptionCallback) {
 		mStateOp.getStatus(aCallback, aExceptionCallback);
 	}
 
 	public ICallback<State> mStateChangeCallback;
 
 	@Override
 	public void setStateChangeCallback(ICallback<State> aCallback) {
 		mStateOp.setStateChangeCallback(aCallback);
 	}
 
 	@Override
 	public void burnBronze(ICallback<Void> aCallback, ICallback<Exception> aExceptionCallback) {
 		mStateOp.burnBronze(aCallback, aExceptionCallback);
 	}
 
 	@Override
 	public void getAttackConf(ICallback<RaidBossAttackConf> aCallback, ICallback<Exception> aExceptionCallback) {
 		mStateOp.getAttackConf(aCallback, aExceptionCallback);
 	}
 
 	@Override
 	public void attack(RaidBossAttackConf aAttackConf, AttackType aAttackType, ICallback<Void> aCallback, ICallback<Exception> aExceptionCallback) {
 		mStateOp.attack(aAttackConf, aAttackType, aCallback, aExceptionCallback);
 	}
 
 	public void notifyChangeCallback() {
 		Utils.startCallback(mStateChangeCallback, mStateOp.getState(), mExecutor);
 	}
 
 	public abstract class MwClientState implements IMwClient {
 
 		public final State mState;
 		public final String mLoginId;
 
 		public MwClientState(State aState, String aLoginId) {
 			mState = aState;
 			mLoginId = aLoginId;
 		}
 
 		@Override
 		public void connect(String aLoginId, String aPassword, ICallback<Void> aCallback, ICallback<Exception> aException) {
 			Utils.startCallback(aException, new IllegalStateException(), mExecutor);
 		}
 
 		@Override
 		public String getLoginId() {
 			return null;
 		}
 
 		@Override
 		public final State getState() {
 			return mState;
 		}
 
 		@Override
 		public final void setStateChangeCallback(ICallback<State> aCallback) {
 			mStateChangeCallback = aCallback;
 		}
 
 		@Override
 		public void getFeed(ICallback<RaidBossMatchingFeed> aCallback, ICallback<Exception> aExceptionCallback) {
 			Utils.startCallback(aExceptionCallback, new IllegalStateException(), mExecutor);
 		}
 
 		@Override
 		public void getStatus(ICallback<PlayerStatus> aCallback, ICallback<Exception> aExceptionCallback) {
 			Utils.startCallback(aExceptionCallback, new IllegalStateException(), mExecutor);
 		}
 
 		@Override
 		public void burnBronze(ICallback<Void> aCallback, ICallback<Exception> aExceptionCallback) {
 			Utils.startCallback(aExceptionCallback, new IllegalStateException(), mExecutor);
 		}
 
 		@Override
 		public void getAttackConf(ICallback<RaidBossAttackConf> aCallback, ICallback<Exception> aExceptionCallback) {
 			Utils.startCallback(aExceptionCallback, new IllegalStateException(), mExecutor);
 		}
 
 		@Override
 		public void attack(RaidBossAttackConf aAttackConf, AttackType aAttackType, ICallback<Void> aCallback, ICallback<Exception> aExceptionCallback) {
 			Utils.startCallback(aExceptionCallback, new IllegalStateException(), mExecutor);
 		}
 	}
 
 	MwClientState mStateOp = new MwClientStateOffline();
 
 	public class MwClientStateOffline extends MwClientState {
 
 		public MwClientStateOffline() {
 			super(State.OFFLINE, null);
 		}
 
 		@Override
 		public void connect(String aLoginId, String aPassword, ICallback<Void> aCallback, ICallback<Exception> aException) {
 			MwClientStateConnecting state = new MwClientStateConnecting(aLoginId, aPassword, aCallback, aException);
 			mStateOp = state;
 			notifyChangeCallback();
 			state.startProcess();
 		}
 
 		@Override
 		public void disconnect(final ICallback<Void> aCallback) {
 			Utils.startCallback(aCallback, null, mExecutor);
 		}
 
 	}
 
 	public class MwClientStateOnline extends MwClientState {
 
 		final HttpClient mHttpClient;
 
 		public MwClientStateOnline(HttpClient aHttpClient, String aLoginId) {
 			super(State.ONLINE, aLoginId);
 			mHttpClient = aHttpClient;
 		}
 
 		@Override
 		public void disconnect(final ICallback<Void> aCallback) {
 			MwClientStateDisconnecting state = new MwClientStateDisconnecting(aCallback);
 			mStateOp = state;
 			notifyChangeCallback();
 			state.startProcess();
 		}
 
 		@Override
 		public void getFeed(ICallback<RaidBossMatchingFeed> aCallback, final ICallback<Exception> aExceptionCallback) {
 			final AsynList asynList = new AsynList();
 			asynList.addCallback(new ICallback<Void>() {
 				@Override
 				public void callback(Void aV) {
 					HttpGet httpGet = new HttpGet("http://sp.pf.mbga.jp/12012090/?url=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fraid_boss_matching_feed.php");
 					httpDoc(mHttpClient, httpGet, asynList.createStartNextCallback(new Document[0]), aExceptionCallback);
 				}
 			});
 			asynList.addCallback(RaidBossMatchingFeed.toFeed(asynList.createStartNextCallback(new RaidBossMatchingFeed[0]), aExceptionCallback, mExecutor));
 			asynList.addCallback(aCallback);
 			asynList.start(mExecutor);
 		}
 
 		@Override
 		public void getStatus(ICallback<PlayerStatus> aCallback, final ICallback<Exception> aExceptionCallback) {
 			final AsynList asynList = new AsynList();
 			asynList.addCallback(new ICallback<Void>() {
 				@Override
 				public void callback(Void aV) {
 					HttpGet httpGet = new HttpGet("http://sp.pf.mbga.jp/12012090/?url=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fmypage.php");
 					httpDoc(mHttpClient, httpGet, asynList.createStartNextCallback(new Document[0]), aExceptionCallback);
 				}
 			});
 			asynList.addCallback(PlayerStatus.toStatus(asynList.createStartNextCallback(new PlayerStatus[0]), aExceptionCallback, mExecutor));
 			asynList.addCallback(aCallback);
 			asynList.start(mExecutor);
 		}
 
 		@Override
 		public void burnBronze(ICallback<Void> aCallback, ICallback<Exception> aExceptionCallback) {
 			BurnBronzeOp bbo = new BurnBronzeOp(MwClient.this, mHttpClient);
 			bbo.start(aCallback, aExceptionCallback);
 		}
 
 		@Override
 		public void getAttackConf(ICallback<RaidBossAttackConf> aCallback, final ICallback<Exception> aExceptionCallback) {
 			final AsynList asynList = new AsynList();
 			asynList.addCallback(new ICallback<Void>() {
 				@Override
 				public void callback(Void aV) {
 					HttpGet httpGet = new HttpGet(ATTACK_CONF_URL);
 					httpDoc(mHttpClient, httpGet, asynList.createStartNextCallback(new Document[0]), aExceptionCallback);
 				}
 			});
 			asynList.addCallback(RaidBossAttackConf.toRaidBossAttackConf(asynList.createStartNextCallback(new RaidBossAttackConf[0]), aExceptionCallback, mExecutor));
 			asynList.addCallback(aCallback);
 			asynList.start(mExecutor);
 		}
 
 		@Override
 		public void attack(RaidBossAttackConf aAttackConf, AttackType aAttackType, ICallback<Void> aCallback, final ICallback<Exception> aExceptionCallback) {
			String url = (aAttackType == AttackType.BP1) ? aAttackConf.mBp1Url : (aAttackType == AttackType.BP3) ? aAttackConf.mBp3Url : null;
 			if (url == null)
 				aExceptionCallback.callback(new IllegalArgumentException());
 			final AsynList asynList = new AsynList();
 			asynList.addCallback(new ICallback<Void>() {
 				@Override
 				public void callback(Void aV) {
					HttpGet httpGet = new HttpGet(ATTACK_CONF_URL);
 					httpDoc(mHttpClient, httpGet, asynList.createStartNextCallback(new Document[0]), aExceptionCallback);
 				}
 			});
 			asynList.addCallback(aCallback, null);
 			asynList.start(mExecutor);
 		}
 	}
 
 	public class MwClientStateConnecting extends MwClientState {
 		final public String mPassword;
 		final public ICallback<Void> mCallback;
 		final public ICallback<Exception> mExceptionCallback;
 		public HttpClient mHttpClient;
 
 		public MwClientStateConnecting(String aLoginId, String aPassword, ICallback<Void> aCallback, ICallback<Exception> aException) {
 			super(State.CONNECTING, aLoginId);
 			mPassword = aPassword;
 			mCallback = aCallback;
 			mExceptionCallback = aException;
 		}
 
 		public void startProcess() {
 			mExecutor.execute(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						mHttpClient = createHttpClient();
 						if (login(mHttpClient, mLoginId, mPassword)) {
 							mStateOp = new MwClientStateOnline(mHttpClient, mLoginId);
 							notifyChangeCallback();
 							Utils.startCallback(mCallback, null, mExecutor);
 						} else {
 							mStateOp = new MwClientStateOffline();
 							notifyChangeCallback();
 							Utils.startCallback(mExceptionCallback, new Exception("login fail"), mExecutor);
 						}
 					} catch (Exception e) {
 						mStateOp = new MwClientStateOffline();
 						notifyChangeCallback();
 						Utils.startCallback(mExceptionCallback, e, mExecutor);
 					}
 				}
 			});
 		}
 
 		@Override
 		public void disconnect(final ICallback<Void> aCallback) {
 			throw new IllegalStateException();
 		}
 
 	}
 
 	public class MwClientStateDisconnecting extends MwClientState {
 
 		final ICallback<Void> mCallback;
 
 		public MwClientStateDisconnecting(ICallback<Void> aCallback) {
 			super(State.DISCONNECING, null);
 			mCallback = aCallback;
 		}
 
 		public void startProcess() {
 			mExecutor.execute(new Runnable() {
 				@Override
 				public void run() {
 					mStateOp = new MwClientStateOffline();
 					notifyChangeCallback();
 					Utils.startCallback(mCallback, null, mExecutor);
 				}
 			});
 		}
 
 		@Override
 		public void disconnect(final ICallback<Void> aCallback) {
 			// TODO
 			throw new IllegalStateException();
 		}
 
 	}
 
 	protected static HttpClient createHttpClient() {
 		HttpParams httpParams = new BasicHttpParams();
 		httpParams.setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
 		HttpClientParams.setRedirecting(httpParams, true);
 		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
 		httpClient.setRedirectStrategy(new DefaultRedirectStrategy() {
 			@Override
 			public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
 				if (super.isRedirected(request, response, context))
 					return true;
 				int code = response.getStatusLine().getStatusCode();
 				if (code == 302)
 					return true;
 				return false;
 			}
 		});
 		return httpClient;
 	}
 
 	protected boolean login(HttpClient aHttpClient, String aLoginId, String aPassword) throws ClientProtocolException, IOException {
 		synchronized (aHttpClient) {
 			HttpGet httpGet = new HttpGet("https://ssl.sp.mbga.jp/_lg");
 			HttpResponse response = aHttpClient.execute(httpGet);
 			if (response.getStatusLine().getStatusCode() != 200)
 				return false;
 
 			HttpEntity httpEntity = response.getEntity();
 			String contentType = httpEntity.getContentType().getValue();
 			if (!contentType.equals("text/html; charset=UTF-8")) {
 				return false;
 			}
 
 			InputStream is = httpEntity.getContent();
 			Document doc = Jsoup.parse(is, "UTF-8", "https://ssl.sp.mbga.jp/_lg");
 			EntityUtils.consume(httpEntity);
 
 			Elements formElements = doc.select("form[action*=https://ssl.sp.mbga.jp/_lg]");
 			if (formElements.size() != 1)
 				return false;
 			Element formElement = formElements.get(0);
 
 			Elements inputElements = formElement.select("input");
 			if (inputElements.select("[name*=login_id]").size() != 1)
 				return false;
 			if (inputElements.select("[name*=login_pw]").size() != 1)
 				return false;
 			if (inputElements.select("[type*=submit]").size() != 1)
 				return false;
 			for (Element inputElement : inputElements) {
 				String type = inputElement.attr("type");
 				if (type.equals("hidden"))
 					continue;
 				if ((type.equals("email")) && (inputElement.id().equals("login_id")))
 					continue;
 				if ((type.equals("password")) && (inputElement.id().equals("login_pw")))
 					continue;
 				if (type.equals("submit"))
 					continue;
 				return false;
 			}
 
 			HttpPost httpPost = new HttpPost("https://ssl.sp.mbga.jp/_lg");
 			List<NameValuePair> nvpList = new LinkedList<NameValuePair>();
 			nvpList.add(new BasicNameValuePair("login_id", aLoginId));
 			nvpList.add(new BasicNameValuePair("login_pw", aPassword));
 			Elements hiddenInputElements = formElement.select("[type*=hidden]");
 			for (Element hiddenInputElement : hiddenInputElements) {
 				nvpList.add(new BasicNameValuePair(hiddenInputElement.attr("name"), hiddenInputElement.attr("value")));
 			}
 			Element submitElement = inputElements.select("[type*=submit]").get(0);
 			nvpList.add(new BasicNameValuePair(submitElement.attr("name"), submitElement.attr("value")));
 			StringEntity entity = new StringEntity(URLEncodedUtils.format(nvpList, "UTF-8"));
 			httpPost.setEntity(entity);
 			response = aHttpClient.execute(httpPost);
 			EntityUtils.consume(response.getEntity());
 			if (response.getStatusLine().getStatusCode() != 200)
 				return false;
 
 			httpGet = new HttpGet("http://sp.pf.mbga.jp/12012090");
 			response = aHttpClient.execute(httpGet);
 			EntityUtils.consume(response.getEntity());
 			if (response.getStatusLine().getStatusCode() != 200)
 				return false;
 
 			return true;
 		}
 	}
 
 	protected void httpDoc(final HttpClient aHttpClient, final HttpUriRequest aHttpUriRequest, final ICallback<Document> aCallback, final ICallback<Exception> aExceptionCallback) {
 		mExecutor.execute(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					synchronized (aHttpClient) {
 						HttpResponse response = aHttpClient.execute(aHttpUriRequest);
 						Utils.parseCheck(200, response.getStatusLine().getStatusCode());
 
 						HttpEntity httpEntity = response.getEntity();
 						String contentType = httpEntity.getContentType().getValue();
 						Utils.parseCheck("text/html; charset=UTF-8", contentType);
 						InputStream is = httpEntity.getContent();
 						Document doc = Jsoup.parse(is, "UTF-8", aHttpUriRequest.getURI().toString());
 						EntityUtils.consume(httpEntity);
 						Utils.startCallback(aCallback, doc, mExecutor);
 					}
 				} catch (Exception e) {
 					Utils.startCallback(aExceptionCallback, e, mExecutor);
 				}
 			}
 		});
 	}
 
 	public ICallback<HttpUriRequest> httpDocCallback(final HttpClient aHttpClient, final ICallback<Document> aCallback, final ICallback<Exception> aExceptionCallback) {
 		return new ICallback<HttpUriRequest>() {
 			@Override
 			public void callback(HttpUriRequest aResult) {
 				httpDoc(aHttpClient, aResult, aCallback, aExceptionCallback);
 			}
 		};
 	}
 
 }
