 package com.karhatsu.fingerpori;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class FingerporiActivity extends Activity {
 	private static final String FINGERPORI_URL = "http://www.hs.fi/fingerpori";
 	private static final String IMAGE_URL_REGEX = "(http://www.hs.fi/kuvat/iso_webkuva/[0-9]*.gif)";
	private static final String PREV_URL_REGEX = "((http://www.hs.fi/fingerpori/[0-9]+)\".*>\\s*Edellinen)";
 
 	private String imageUrl;
 	private String prevUrl;
 
 	public FingerporiActivity() {
 		readImageAndPrevUrls(FINGERPORI_URL, false);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		loadImage();
 		// showToast("prev: " + prevUrl);
 
 		Button button = (Button) findViewById(R.id.prevButton);
 		button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (prevUrl != null) {
 					readImageAndPrevUrls(prevUrl, false);
 					loadImage();
 				} else {
 					showToast("Edellistä Fingerporia ei ole saatavilla");
 				}
 			}
 
 		});
 	}
 
 	private void showToast(String text) {
 		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
 	}
 
 	private void loadImage() {
 		WebView webView = (WebView) findViewById(R.id.webView);
 		webView.loadUrl(imageUrl);
 	}
 
 	private void readImageAndPrevUrls(String fullHtmlUrl, boolean showToast) {
 		try {
 			String fullHtml = loadFullHtml(fullHtmlUrl);
 			imageUrl = parseImageUrl(fullHtml);
 			prevUrl = parsePrevUrl(fullHtml);
 			if (showToast) {
 				// showToast("img: " + imageUrl);
 				showToast("prev: " + prevUrl);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			imageUrl = fullHtmlUrl; // fallback to full html page
 			prevUrl = null;
 		}
 	}
 
 	private String loadFullHtml(String fullHtmlUrl) throws IOException,
 			ClientProtocolException {
 		HttpClient client = new DefaultHttpClient();
 		HttpGet request = new HttpGet(fullHtmlUrl);
 		HttpResponse response = client.execute(request);
 		BufferedReader rd = new BufferedReader(new InputStreamReader(response
 				.getEntity().getContent()));
 		String line = "";
 		StringBuilder sb = new StringBuilder();
 		while ((line = rd.readLine()) != null) {
 			sb.append(line);
 		}
 		return sb.toString();
 	}
 
 	private String parseImageUrl(String html) {
		return parseUrl(html, IMAGE_URL_REGEX, 0);
 	}
 
 	private String parsePrevUrl(String html) {
		return parseUrl(html, PREV_URL_REGEX, 2);
 	}
 
	private String parseUrl(String html, String regex, int group) {
 		Pattern pattern = Pattern.compile(regex);
 		Matcher matcher = pattern.matcher(html);
 		matcher.find();
		return matcher.group(group);
 	}
 }
