 /*
  * Copyright (C) 2013 Jan Renz
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.janrenz.app.mediathek;
 
 import android.annotation.TargetApi;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.nostra13.universalimageloader.core.DisplayImageOptions;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.turbomanage.httpclient.HttpResponse;
 import com.turbomanage.httpclient.android.AndroidHttpClient;
 
 import org.holoeverywhere.app.AlertDialog;
 import org.holoeverywhere.widget.Button;
 import org.holoeverywhere.widget.Spinner;
 import org.holoeverywhere.widget.TextView;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 /**
  * Fragment that displays a news article.
  */
 public class LiveFragment extends ArticleFragment implements LoaderManager.LoaderCallbacks<Cursor> {
 
 
 	// Parameterless constructor is needed by framework
 	public LiveFragment() {
 		super();
 	}
 
 
 	/**
 	 * Sets up the UI.
 	 */
 	@Override
 	public View onCreateView(org.holoeverywhere.LayoutInflater  inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		mView = inflater.inflate(R.layout.detaillive, container, false);
 		setRetainInstance(false);
 		try {
 			displayArticle();
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		return mView;
 	}
 	
 	
 	@Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 		
         triggerLoad(true);
 	}
 	
 	private void triggerLoad(Boolean forceReload ){
 		    
 	    int loaderId = 200;
 		getActivity().getSupportLoaderManager().restartLoader(loaderId, null, this);
 	 
 	}
 	
 
 	/**
 	 * Displays a particular article.
 	 *
 	 *            the article to display
 	 */
 	public void displayArticle() {
 		//display the information we already got, then fetch the detail information 
 		TextView text = (TextView) mView.findViewById(R.id.headline1);
 		TextView text2 = (TextView) mView.findViewById(R.id.headline2);
 		TextView text3 = (TextView) mView.findViewById(R.id.senderinfo);
 		text2.setText(getArguments().getString("title"));
 		text.setText(getArguments().getString("subtitle"));
 		text3.setText(getArguments().getString("senderinfo"));
 		this.title = getArguments().getString("title");
 		new AccessWebServiceTask()
 				.execute("http://m.daserste.de/resources/data/livestream_de.xml");
 		triggerLoad(true);
 				
 	}
 	public static boolean canDisplayM3u8(Context context) {
 		if (context != null ){
 			 return false;
 		}
         try {
             PackageManager packageManager = context.getPackageManager();
             Intent testIntent = new Intent(Intent.ACTION_VIEW);
             testIntent.setType("application/x-mpegURL");
             if (packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
                 return true;
             }
         }catch (Exception e){
 
         }
         return false;
 	}
 
 	private class AccessWebServiceTask extends AsyncTask<String, Void, String> {
 		protected String doInBackground(String... urls) {
 			return loadXML(urls[0]);
 		}
 
 		protected void onPostExecute(String result) {
 
 			// get duration
 			InputSource inputSrc = new InputSource(new StringReader(result));
 			inputSrc.setEncoding("UTF-8");
 			XPath xpath = XPathFactory.newInstance().newXPath();
 			String expression = "//playlist/video/streamingRTSP/stream";
 			// list of nodes queried
 			
 			videoSources = new ArrayList<String[]>();
 			Boolean canHandleRtmp = canDisplayRtsp(getActivity());
 	
 			// list of nodes queried
 			try {
 				String tempUrl = "";
 				NodeList nodes = (NodeList) xpath.evaluate(expression,
 						inputSrc, XPathConstants.NODESET);
 				for (int i = 0; i < nodes.getLength(); i++) {
 					Node node = nodes.item(i);
 					String bandwidth = null;
 					String serverPrefix = "";
 					NodeList nodeChilds = node.getChildNodes();
 					for (int j = 0; j < nodeChilds.getLength(); j++) {
 						Node childNode = nodeChilds.item(j);
 						String nodeName = childNode.getNodeName();
 						String nodeValue = childNode.getTextContent();
 						if (nodeName.equals("recommendedBandwidth")) {
 							bandwidth = nodeValue;
 							break;
 
 						} else if (nodeName.equals("url")) {
 							tempUrl = nodeValue;
 						} else if (nodeName.equals("serverPrefix")) {
 							serverPrefix = nodeValue;
 						} else {
 						}
 					}
 					videoPath = tempUrl;
 					String videoUrl = serverPrefix + videoPath;
 					if (bandwidth.equals("")) {
 						bandwidth = "HbbTV";
 					}
 					if (videoUrl.startsWith("rtsp")) {
 						bandwidth = bandwidth + " (RTSP)";
 						if (!videoUrl.equals("")) {
 							videoSources
 									.add(new String[] { bandwidth, videoUrl });
 						}
 					} else if (canHandleRtmp){
 						bandwidth = bandwidth + " ";
 					}
 
 				
 					
 				}
 				// }
 				if (canDisplayM3u8(getActivity()) == true){
 					InputSource inputSrc2 = new InputSource(new StringReader(result));
 					inputSrc2.setEncoding("UTF-8");
 					XPath xpath2 = XPathFactory.newInstance().newXPath();
 					String expression2 = "//playlist/video/streamingUrlIPad";
 					NodeList nodes2 = (NodeList) xpath2.evaluate(expression2,
 							inputSrc2, XPathConstants.NODESET);	
 					videoSources
 					.add(new String[] { "Dynamisch (.m3u8)",  nodes2.item(0).getTextContent() });
 
 				}
 				
 				// Spinner population
 				// default quality
 
 				ArrayList<String> qualities = new ArrayList<String>();
 				for (String[] obj : videoSources) {
 					qualities.add(obj[0]);
 				}
 				final Spinner s = (Spinner) mView
 						.findViewById(R.id.qualitySpinner);
 				if (s == null || getActivity() == null) {
 					//this might be a background taskfinsihed while the ui is long gone
 					return;
 				}
 				ArrayAdapter<String> mspinnerAdapter = new ArrayAdapter<String>(
 						getActivity(),
 						android.R.layout.simple_spinner_dropdown_item,
 						qualities);
 				s.setAdapter(mspinnerAdapter);
 				SharedPreferences appSettings = getActivity()
 						.getSharedPreferences("AppPreferences",
 								getActivity().MODE_PRIVATE);
 				String defaultQuality = appSettings.getString("QualityLive", "DSL768");
 
 				videoPath = videoSources
 						.get(getQualityPositionForString(defaultQuality))[1];
 				s.setSelection(getQualityPositionForString(defaultQuality));
 
 				s.setOnItemSelectedListener(new org.holoeverywhere.widget.AdapterView.OnItemSelectedListener() {
 
 					@Override
 					public void onItemSelected(org.holoeverywhere.widget.AdapterView<?> arg0, View arg1,
 							int arg2, long arg3) {
 						// TODO Auto-generated method stub
 						// Integer item = s.getSelectedItemPosition();
 
 						videoPath = videoSources.get(arg2)[1];
 						SharedPreferences appSettings = getActivity()
 								.getSharedPreferences("AppPreferences",
 										getActivity().MODE_PRIVATE);
 						SharedPreferences.Editor prefEditor = appSettings
 								.edit();
 						prefEditor.putString("QualityLive",
 								videoSources.get(arg2)[0]);
 						prefEditor.commit();
 					}
 
 					
 			
 
 					@Override
 					public void onNothingSelected(org.holoeverywhere.widget.AdapterView<?> arg0) {
 						// TODO Auto-generated method stub
 						
 					}
 
 					
 					
 				});
 				Button button = (Button) mView.findViewById(R.id.buttonWatch);
 
 				button.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						// if (mOnMovieClickedListener != null) {
 						if (videoPath != null) {
 							Intent intent = new Intent(Intent.ACTION_VIEW);
 							String mime = "application/x-mpegURL";
 							if (videoPath.startsWith("rtsp")) {
 								mime = "video/rtsp";
 
 							} else {
 							}
 							intent.setDataAndType(Uri.parse(videoPath), mime);
 							//intent.putExtra(Intent.EXTRA_SUBJECT, title);
 							try {
 								startActivity(intent);								
 							} catch (Exception e) {
 								// Kein passender IntentHandler gefunden
 								 AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 							        
 							        builder.setTitle("Fehler")         
 							                .setMessage("Auf diesem Smartphone kann die URL "+ videoPath +" mit dem Dateityp "+ mime + " nicht geöffnet werden. Bitte lade Dir ein passenden Player herunter.")
 							                .setInverseBackgroundForced(true)//needded for old android version
 							                .setCancelable(true)
 							                // OK button
 							                .setPositiveButton(
 							                       getActivity().getResources().getString(R.string.changelog_ok_button),
 							                        new DialogInterface.OnClickListener() {
 							                            @Override
 							                            public void onClick(DialogInterface dialog, int which) {
 							                               dialog.dismiss();
 							                            }
 							                        });
 						            // Show "More…" button if we're only displaying a partial change log.
 
 							       AlertDialog dialog = builder.create();
 							       dialog.show();
 							}
 							
 						}
 
 					}
 
 				});
 				Button buttonCopy = (Button) mView
 						.findViewById(R.id.buttonCopy);
 				
 				if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB){
 					
 					buttonCopy.setOnClickListener(new View.OnClickListener() {
 						
 						@TargetApi(Build.VERSION_CODES.HONEYCOMB) public void onClick(View v) {
 							ClipboardManager clipboard = (ClipboardManager) getActivity()
 									.getSystemService(
 											getActivity().CLIPBOARD_SERVICE);
 							ClipData clip = ClipData.newPlainText("Mediathek",
 									videoPath);
 							clipboard.setPrimaryClip(clip);
 							Toast.makeText(getActivity(),
 									"Url wurde in Zwischenablage kopiert",
 									Toast.LENGTH_LONG).show();
 						}
 						
 					});
 				    
 				}else{
 					buttonCopy.setVisibility(View.GONE);
 				} 
 				//set invisble on app settings
 				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
 				Boolean hideCopyButtonSetting = sharedPref.getBoolean(SettingsActivity.HIDE_COPYBUTTON, false);
 				if (hideCopyButtonSetting == true){
 					
 					buttonCopy.setVisibility(View.GONE);
 				}
 
 				mView.findViewById(R.id.showAfterLoadItems).setVisibility(
 						View.VISIBLE);
 				mView.findViewById(R.id.hideAfterLoadItems).setVisibility(
 						View.GONE);
 
 			} catch (XPathExpressionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 		// query codea
 				Uri queryUri = Uri.parse("content://de.janrenz.app.mediathek.cursorloader.data");
 				queryUri = queryUri.buildUpon().appendQueryParameter("method", "broadcast").build();
 
 				return new CursorLoader(
 						getActivity(),
 						queryUri,
 						new String[] { "title", "image" , "extId", "startTime", "startTimeAsTimestamp", "isLive", "description", "timeinfo"}, 
 						null, 
 						null, 
 						null);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		// TODO Auto-generated method stub
 		if (cursor != null && cursor.getCount()>0){
 		
 			Cursor myCursor = cursor;
 			myCursor.moveToFirst();
 			TextView tView = (TextView) mView
 					.findViewById(R.id.description);
 			tView.setText(myCursor.getString(myCursor.getColumnIndexOrThrow("description")));
 			Boolean showLongDesc = false;
 			try {
 				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
				 showLongDesc = sharedPref.getBoolean(SettingsActivity.SHOW_LONG_DESC, false);
 				
 			} catch (Exception e) {
 				// use the default if we cant fetch it
 			}
 			if (showLongDesc == false){
 				tView.setMaxLines(5);
 				
 		        tView.setHorizontalFadingEdgeEnabled(true);
 		        
 				tView.setOnClickListener(  new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						TextView tv = (TextView) v;
 						tv.setMaxLines(200);
 						tv.setHorizontalFadingEdgeEnabled(false);
 					}
 				} );
 				
 			}else{
 				
 			}
 			TextView dView = (TextView) mView
 					.findViewById(R.id.durationText);
 			dView.setText(myCursor.getString(myCursor.getColumnIndexOrThrow("timeinfo")));
 			/**
 			 * Set the image
 			 */
 			DisplayImageOptions loadingOptions = new DisplayImageOptions.Builder()
 					.showStubImage(R.drawable.ic_stub)
 					// .showImageForEmptyUri(R.drawable.ic_empty)
 					.showImageOnFail(R.drawable.ic_error)
 					.cacheInMemory()
 					// .cacheOnDisc()
 					.build();
 			ImageView image_view = (ImageView) mView
 					.findViewById(R.id.thumbnail);
 
 			if (image_view != null) {
 				ImageLoader.getInstance().displayImage(myCursor.getString(myCursor.getColumnIndexOrThrow("image"))+"/"+image_view.getWidth(), image_view,
 						loadingOptions);
 			}
 			TextView h1 = (TextView) mView.findViewById(R.id.headline1);
 			h1.setText(myCursor.getString(myCursor.getColumnIndexOrThrow("title")));
 			
 				// The Cursor is now set to the right position
 				Movie mMovie = new Movie();
 				mMovie.setTitle(myCursor.getString(myCursor.getColumnIndexOrThrow("title")));
 				mMovie.setSubtitle(myCursor.getString(myCursor.getColumnIndexOrThrow("subtitle")));
 				mMovie.setExtId(myCursor.getString(myCursor.getColumnIndexOrThrow("extId")));
 				mMovie.setStarttime(myCursor.getString(myCursor.getColumnIndexOrThrow("startTime")));
 				mMovie.setStarttimestamp(myCursor.getInt(myCursor.getColumnIndexOrThrow("startTimeAsTimestamp")));
 				mMovie.setIsLive(myCursor.getString(myCursor.getColumnIndexOrThrow("isLive")));
 				mView.findViewById(R.id.showAfterLoadItems).setVisibility(
 						View.VISIBLE);
 				mView.findViewById(R.id.hideAfterLoadItems).setVisibility(
 						View.GONE);
 		}	
 				
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 }
