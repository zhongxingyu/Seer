 package com.paripper.paripper.Adapter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import twitter4j.HashtagEntity;
 import twitter4j.Status;
 import twitter4j.URLEntity;
 import twitter4j.UserMentionEntity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.text.Html;
 import android.text.format.DateFormat;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.paripper.paripper.R;
 import com.paripper.paripper.util.AvatarDownloader;
 import com.paripper.paripper.util.Constants;
 
 public class TweetAdapter extends ArrayAdapter<Status> {
 	private final Context context;
 	private final List<Status> values;
 	
 	private ImageView tweetAvatar = null;
 	
 	public TweetAdapter(Context context, List<Status> objects) {
 		super(context, R.layout.tweet, objects);
 		// TODO Auto-generated constructor stub
 		this.context = context;
 		this.values = objects;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		// TODO Auto-generated method stub
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	 	View rowView = inflater.inflate(R.layout.tweet, parent, false);
 	 	
 	 	String scheme = Constants.SCHEME;
 	 	Status tweet = values.get(position); 
 	 	
 	 	TextView tweetID = (TextView) rowView.findViewById(R.id.tweetID);
 	 	TextView tweetUser = (TextView) rowView.findViewById(R.id.tweetUser);
 	 	TextView tweetUserId = (TextView) rowView.findViewById(R.id.tweetUserId);
 	 	tweetUserId.setMovementMethod(LinkMovementMethod.getInstance());
 	 	TextView tweetText = (TextView) rowView.findViewById(R.id.tweetText);
 	 	tweetText.setMovementMethod(LinkMovementMethod.getInstance());
 	 	TextView tweetDate = (TextView) rowView.findViewById(R.id.tweetDate);
 	 	TextView tweetVia = (TextView) rowView.findViewById(R.id.tweetVia);
 	 	tweetVia.setMovementMethod(LinkMovementMethod.getInstance());
 	 	tweetAvatar = (ImageView) rowView.findViewById(R.id.tweetAvatar);
 	 	
 	 	tweetID.setText(tweet.getId()+"");
	 	tweetUser.setText(tweet.getUser().getName());
 	 	String twUser = "<a href=\"" + scheme + "://user?" + 
 	 					tweet.getUser().getScreenName() + "\">@" + tweet.getUser().getScreenName()
 	 					+ "</a>";
 		tweetUserId.setText(Html.fromHtml(twUser));
 
  		String txt = tweet.getText();
 
  		URLEntity[] urls = tweet.getURLEntities();
  		for (int i=0; i < urls.length; i++) {
  			URLEntity url = urls[i];
  			txt = txt.replace(url.getURL().toString(), "<a href=\"" + url.getURL().toString()
  							+ "\">" + url.getDisplayURL() + "</a>");
  		}
  		
  		HashtagEntity[] htags = tweet.getHashtagEntities();
  		for (int i=0; i < htags.length; i++) {
  			HashtagEntity ht = htags[i];
  			txt = txt.replace("#" + ht.getText(), "<a href=\"" + scheme + "://htag?" + 
  						ht.getText() + "\">#" + ht.getText() + "</a>");
  		}
 
  		UserMentionEntity[] users = tweet.getUserMentionEntities();
  		for (int i=0; i < users.length; i++) {
  			UserMentionEntity user = users[i];
  			txt = txt.replace("@" + user.getScreenName(), "<a href=\"" + scheme + "://htag?" + 
  					user.getScreenName() + "\">@" + user.getScreenName() + "</a>");
  		}
 	 	tweetText.setText(Html.fromHtml(txt));
 
 	 	tweetDate.setText(DateFormat.format("dd/MM/yyy hh:mm ", tweet.getCreatedAt()));
	 	
	 	String viaTxt = tweet.getSource();
	 	tweetVia.setText(" " + Html.fromHtml(viaTxt));
	 	
 	 	try {
 			tweetAvatar.setImageDrawable(new getAvatarTask().execute(tweet.getUser().getProfileImageURL()).get());
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 	 	//new getAvatarTask().execute(tweet.getUser().getProfileImageURL());
 //	 	try {
 //			FileInputStream input = getContext().openFileInput(
 //								getContext().getExternalCacheDir().getAbsolutePath() +
 //								File.separator +
 //								tweet.getUser().getScreenName());
 //			Bitmap bp = BitmapFactory.decodeStream(input);
 //			tweetAvatar.setImageBitmap(bp);			
 //		} catch (FileNotFoundException e) {
 //			//e.printStackTrace();
 //			Log.w("TimeLine", tweet.getUser().getName() + " avatar not found. Launching new task");
 //			new AvatarDownloader(getContext()).execute(tweet.getUser().getScreenName());
 //		}
 		return rowView;
 	}
 	
 	public void setAvatar(Drawable d) {
 		tweetAvatar.setDrawingCacheEnabled(true);
 		tweetAvatar.setImageDrawable(d);
 	}
 	
 	private class getAvatarTask extends AsyncTask<URL, Void, Drawable> {
 		@Override
 		protected Drawable doInBackground(URL... params) {
 			try {
 				URL url = params[0];
 				InputStream is = (InputStream) url.getContent();
 				Drawable d = Drawable.createFromStream(is, "src name");
 				return d;
 			}catch (Exception e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Drawable result) {
 			super.onPostExecute(result);
 			//setAvatar(result);
 		}
 	}
 	
 	private class doGet extends AsyncTask<String, Void, Header[]> {
 		@Override
 		protected Header[] doInBackground(String... params) {
 			try {
 				String url = params[0].toString();
 	 			HttpClient hc = new DefaultHttpClient();
 	 			HttpGet get = new HttpGet(url);
 
 	 			HttpResponse rp = hc.execute(get);
 
 	 			if(rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
 	 			{
 	 				return rp.getHeaders("Content-type");
 	 			}
 	 			return null;
  			} catch (Exception e){
  				e.printStackTrace();
  				return new Header[0];
  			}
 		}
 	}
 	
 	private List<String> regexExtract(String input, String regex) {
         List<String> result = new ArrayList<String>();
 
         Pattern pattern = Pattern.compile(regex);
 
         Matcher matcher = pattern.matcher(input);
         while (matcher.find()) {
             result.add(matcher.group());
         }
 
         return result;
     }
 
 }
