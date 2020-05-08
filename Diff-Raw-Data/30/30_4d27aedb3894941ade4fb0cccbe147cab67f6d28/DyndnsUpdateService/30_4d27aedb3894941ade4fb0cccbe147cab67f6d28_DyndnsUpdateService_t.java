 package idv.kaomk.eicrhios.dyndns;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sun.misc.BASE64Encoder;
 
 @SuppressWarnings("restriction")
 public class DyndnsUpdateService implements Callable<String>, Runnable {
 	private static final URL QUERY_IP_URL;
 
 	private static enum STATUS {
		good, nochg, badauth, abuse
 	}
 
 	private Logger logger = LoggerFactory.getLogger(DyndnsUpdateService.class);
 
 	private long mPeriod;
 
 	private TimeUnit mTimeUnit;
 
 	private URL mDnsUpdateUrl;
 
 	private String mUsername;
 
 	private String mPassword;
 
 	private String mHostname;
 
 	private STATUS mStatus;
 
 	private ScheduledFuture<?> mUpdateDnsFuture;
 
 	static {
 		try {
 			QUERY_IP_URL = new URL("http://queryip.net/ip");
 		} catch (MalformedURLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void run() {
 		try {
 			call();
 		} catch (IOException e) {
 			logger.info("update dns failed:", e);
 		} catch (Exception e) {
 			logger.error("unexpectd exception:", e);
 			mUpdateDnsFuture.cancel(true);
 			mUpdateDnsFuture = null;
 		}
 	}
 
 	@Override
 	public String call() throws IOException {
 		boolean shouldUpdate = false;
 		String nowIp = null;
		String dnsIp = null;
 		try {
 			nowIp = requestWebSite(QUERY_IP_URL, false);
			dnsIp = InetAddress.getByName(mHostname).getHostAddress();
			if (!dnsIp.equals(nowIp)) {
 				logger.info(String.format(
						"dnsIp(%s) != nowIp(%s), need update...",
						dnsIp, nowIp));
 				shouldUpdate = true;
 			} else {
				logger.debug("IP %s is not changed.", nowIp);
 			}
 		} catch (IOException e) {
 			logger.warn("query current ip failed: ", e);
 			shouldUpdate = true;
 		}
 
 		if (shouldUpdate) {
 			String response = requestWebSite(mDnsUpdateUrl, true);
			logger.info(String.format("dns update result: %s ", response));
 			mStatus = STATUS.valueOf(response.split(
 					" ")[0]);
 			logger.debug(String.format("mStatus: %s", mStatus));
 			if (mStatus == STATUS.good || mStatus == STATUS.nochg) {
				dnsIp = nowIp;
 			}
 			return response;
 		} else
			return String.format("%s %s", mStatus, dnsIp);
 	}
 
 	private ScheduledExecutorService mExecutor = Executors
 			.newSingleThreadScheduledExecutor();
 
 	public void start() throws MalformedURLException {
 		if (logger.isDebugEnabled()) {
 			logger.info("DyndnsUpdateService.start");
 			logger.info(String.format(
 					"scheduleAtFixedRate param: %s", this));
 
 		}
 
 		mDnsUpdateUrl = new URL(
 				String.format(
 						"http://members.dyndns.org/nic/update?hostname=%1$s",
 						mHostname));
 
 		mUpdateDnsFuture = mExecutor.scheduleAtFixedRate(this, 0, mPeriod,
 				mTimeUnit);
 
 	}
 
 	public void stop() {
 		logger.debug("DyndnsUpdateService.stop");
 		mExecutor.shutdownNow();
 	}
 
 	public String refresh() throws InterruptedException, ExecutionException {
 		ScheduledFuture<String> future = mExecutor.schedule(
 				(Callable<String>) this, 0, TimeUnit.SECONDS);
 		return future.get();
 	}
 
 	public long getPeriod() {
 		return mPeriod;
 	}
 
 	public void setPeriod(long period) {
 		mPeriod = period;
 	}
 
 	public String getTimeUnit() {
 		return mTimeUnit.toString();
 	}
 
 	public void setTimeUnit(String timeUnit) {
 		mTimeUnit = TimeUnit.valueOf(TimeUnit.class, timeUnit);
 	}
 
 	public String getUsername() {
 		return mUsername;
 	}
 
 	public void setUsername(String username) {
 		mUsername = username;
 	}
 
 	public String getPassword() {
 		return mPassword;
 	}
 
 	public void setPassword(String password) {
 		mPassword = password;
 	}
 
 	public String getHostname() {
 		return mHostname;
 	}
 
 	public void setHostname(String hostname) {
 		mHostname = hostname;
 	}
 
 	public URL getDnsUpdateUrl() {
 		return mDnsUpdateUrl;
 	}
 
 	public STATUS getStatus() {
 		return mStatus;
 	}
 
 	private String requestWebSite(URL url, boolean needAuth) throws IOException {
 		BufferedReader reader = null;
 		try {
 			logger.debug(String.format("try to connect to Url: %s",
 					url.toString()));
 
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 
 			conn.setDoOutput(true);
 			conn.setDoInput(true);
 			conn.setUseCaches(false);
 			conn.setRequestMethod("GET");
 			conn.setRequestProperty("Content-Type", "text/html;charset=UTF-8");
 			if (needAuth) {
 				BASE64Encoder enc = new sun.misc.BASE64Encoder();
 				String userpassword = mUsername + ":" + mPassword;
 				conn.setRequestProperty("Authorization",
 						"Basic " + enc.encode(userpassword.getBytes()));
 
 			}
 			conn.connect();
 
 			int responseCode = conn.getResponseCode();
 			logger.debug(String.format("response code: %d", responseCode));
 			reader = new BufferedReader(new InputStreamReader(
 					responseCode == HttpURLConnection.HTTP_OK ? conn
 							.getInputStream() : conn.getErrorStream()));
 
 			if (responseCode == HttpURLConnection.HTTP_OK) {
 				return reader.readLine();
 			} else {
 				String line;
 				StringBuilder sb = new StringBuilder();
 				while ((line = reader.readLine()) != null) {
 					sb.append(line);
 				}
 				throw new IOException(sb.toString());
 			}
 		} finally {
 			if (reader != null)
 				try {
 					reader.close();
 				} catch (IOException e) {
 				}
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "DyndnsUpdateService [mPeriod=" + mPeriod + ", mTimeUnit="
 				+ mTimeUnit + ", mUsername=" + mUsername + ", mPassword="
 				+ mPassword + ", mHostname=" + mHostname + "]";
 	}
 	
 	
 }
