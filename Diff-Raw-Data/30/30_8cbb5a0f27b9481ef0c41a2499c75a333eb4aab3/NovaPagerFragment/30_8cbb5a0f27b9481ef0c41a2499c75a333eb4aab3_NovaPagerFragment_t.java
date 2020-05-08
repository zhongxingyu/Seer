 package com.schema.bro.nova;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Calendar;
 
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.ViewPager;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.schema.bro.R;
 
 public class NovaPagerFragment extends Fragment {
 
 	private static final String URL_FIRST = "http://www.novasoftware.se/ImgGen/schedulegenerator.aspx?format=png&schoolid=80740/sv-se&type=1&id={";
 	private static final String URL_SECOND = "}&period=&week=&mode=0&printer=0&colors=32&head=0&clock=0&foot=0&";
 	private static final String URL_THIRD = "&maxwidth=2000&maxheight=2000.png";
 
 	private NovaPagerAdapter adapter;
 	private static final int[] days = {1, 2, 4, 8, 16};
 	
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		final View rootView = inflater.inflate(R.layout.pager_layout,
 				container, false);
 
 		adapter = new NovaPagerAdapter(getFragmentManager());
 
 		final ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);
 		pager.setAdapter(adapter);
 		pager.setOffscreenPageLimit(4);
 
 		int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
 		if (day == Calendar.TUESDAY)
 			pager.setCurrentItem(1);
 		else if (day == Calendar.WEDNESDAY)
 			pager.setCurrentItem(2);
 		else if (day == Calendar.THURSDAY)
 			pager.setCurrentItem(3);
 		else if (day == Calendar.FRIDAY)
 			pager.setCurrentItem(4);
 
 		return rootView;
 	}
 
 	private void loadImages(){
		for (int i = 0; i < 5; i++) {
			NovaFragment novaFrag = adapter.getFragment(i);
			novaFrag.notifyDownload();
		}
		
 		DisplayMetrics metrics = new DisplayMetrics();
 		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		int screenWidth = metrics.widthPixels; 
 		int screenHeight = metrics.heightPixels;
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
 		String classURL = prefs.getString("class_url", NovaOnItemSelectedListener.BF1);
 		
 		String url1 = assembleURL(classURL, 0, screenWidth, screenHeight);
 		String url2 = assembleURL(classURL, 1, screenWidth, screenHeight);
 		String url3 = assembleURL(classURL, 2, screenWidth, screenHeight);
 		String url4 = assembleURL(classURL, 3, screenWidth, screenHeight);
 		String url5 = assembleURL(classURL, 4, screenWidth, screenHeight);
 		new DownloadImageTask().execute(url1, url2, url3, url4, url5);
 	}
 	
 	private String assembleURL(String classURL, int day, int screenWidth, int screenHeight){
 		return URL_FIRST + classURL + URL_SECOND
 				+ "day=" + days[day] + "&width=" + screenWidth/2 + "&height=" + screenHeight/2 + URL_THIRD;
 	}
 	
 	public void changeClass(String classURL, int pos) {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
 		prefs.edit().putString("class_url", classURL).commit();
 		prefs.edit().putInt("class_spinner_pos", pos).commit();
 		
 		loadImages();
 	}
 
 	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap[]> {
 
 		@Override
 		protected Bitmap[] doInBackground(String... urls) {
 			int count = urls.length;
 			Bitmap[] bitmap = new Bitmap[count];
 			for (int i = 0; i < count; i++) {
 				if (isCancelled())
 					break;
 				
 				try {
 					bitmap[i] = BitmapFactory.decodeStream((InputStream) new URL(urls[i]).getContent());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			return bitmap;
 		}
 
 		@Override
 		protected void onPostExecute(Bitmap[] result) {
 			if (isCancelled())
 				return;
 			for (int i = 0; i < result.length; i++) {
 				NovaFragment novaFrag = adapter.getFragment(i);
 				if(novaFrag != null)
 					novaFrag.setImageView(result[i]);
 				else
 					Log.e("NovaPagerFragment", "fragment is null");
 			}
 		}
 	}
 
 }
