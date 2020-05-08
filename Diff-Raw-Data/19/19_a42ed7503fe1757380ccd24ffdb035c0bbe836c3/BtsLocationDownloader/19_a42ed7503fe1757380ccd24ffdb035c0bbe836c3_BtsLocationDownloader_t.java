 package carnero.netmap.common;
 
 import android.os.AsyncTask;
 import android.text.TextUtils;
 import android.util.Log;
 import carnero.netmap.listener.OnLocationObtainedListener;
 import carnero.netmap.model.Bts;
 import com.google.android.gms.maps.model.LatLng;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.Inflater;
 import java.util.zip.InflaterInputStream;
 
 public class BtsLocationDownloader extends AsyncTask<Void, Void, LatLng> {
 
 	private Bts mBts;
 	private OnLocationObtainedListener mListener;
 	// consts
	private final Pattern locationPattern = Pattern.compile("<tr><td>([0-9]+)</td><td>[^<]*</td><td>([0-9]+)</td>(<td[^>]*>.*</td>){5}<td><A HREF=\"([^\"]+)\"[^>]*>[^<]+</A>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
 	private final Pattern linkPattern = Pattern.compile("d=coor_([0-9\\.]+),([0-9\\.]+)_[0-9]+", Pattern.CASE_INSENSITIVE);
 
 	public BtsLocationDownloader(Bts bts, OnLocationObtainedListener listener) {
 		mBts = bts;
 		mListener = listener;
 	}
 
 	public void onPreExecute() {
 		mBts.setLoading();
 	}
 
 	public LatLng doInBackground(Void... params) {
 		final String url = Constants.URL_BASE_GSMWEB + Integer.toHexString(mBts.cid).toUpperCase();
 		String data = null;
 
 		try {
 			Log.d(Constants.TAG, "Downloading location for " + mBts.lac + ":" + mBts.cid);
 
 			URL u = new URL(url);
 			URLConnection uc = u.openConnection();
 
 			uc.setRequestProperty("Host", u.getHost());
 			uc.setRequestProperty("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
 			uc.setRequestProperty("Accept-Charset", "utf-8, iso-8859-1, utf-16, *;q=0.7");
 			uc.setRequestProperty("Accept-Language", "en-US");
 			uc.setRequestProperty("User-Agent", Util.pickUserAgent());
 			uc.setRequestProperty("Connection", "keep-alive");
 			uc.setRequestProperty("Keep-Alive", "300");
 
 			HttpURLConnection connection = (HttpURLConnection) uc;
 			connection.setReadTimeout(30000); // 30 sec
 			connection.setRequestMethod("GET");
 			HttpURLConnection.setFollowRedirects(false);
 			connection.setDoInput(true);
 			connection.setDoOutput(false);
 
 			final String encoding = connection.getContentEncoding();
 
 			InputStream ins;
 			if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
 				ins = new GZIPInputStream(connection.getInputStream());
 			} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
 				ins = new InflaterInputStream(connection.getInputStream(), new Inflater(true));
 			} else {
 				ins = connection.getInputStream();
 			}
 			final InputStreamReader inr = new InputStreamReader(ins);
 			final BufferedReader br = new BufferedReader(inr);
 			final StringBuffer buffer = new StringBuffer();
 
 			Util.readIntoBuffer(br, buffer);
 			data = buffer.toString();
 
 			connection.disconnect();
 			br.close();
 			ins.close();
 			inr.close();
 		} catch (Exception e) {
 			Log.e(Constants.TAG, "Failed to download BTS' location");
 		}
 
 		if (!TextUtils.isEmpty(data)) {
 			int lac;
 			int cid;
 			String link = null;
 			double latitude = Double.NaN;
 			double longitude = Double.NaN;
 
 			final Matcher locationMatcher = locationPattern.matcher(data);
 			while (locationMatcher.find()) {
 				if (locationMatcher.groupCount() > 0) {
 					lac = Integer.parseInt(locationMatcher.group(2));
 					cid = Integer.parseInt(locationMatcher.group(1));
 					link = locationMatcher.group(4);
 
 					if (lac == mBts.lac && cid == mBts.cid) {
 						break;
 					}
 				}
 			}
 
 			if (TextUtils.isEmpty(link)) {
 				return null;
 			}
 
 			final Matcher linkMatcher = linkPattern.matcher(link);
 			if (linkMatcher.find() && linkMatcher.groupCount() > 0) {
 				latitude = Double.parseDouble(linkMatcher.group(2));
 				longitude = Double.parseDouble(linkMatcher.group(1));
 			}
 
 			if (latitude != Double.NaN && longitude != Double.NaN) {
 				return new LatLng(latitude, longitude);
 			}
 		}
 
 		return null;
 	}
 
 	public void onPostExecute(LatLng result) {
 		mBts.location = result;
 
 		if (mListener != null) {
 			mListener.onLocationObtained(mBts);
 		}
 
 		mBts.clearLoading();
 	}
 }
