 package com.hotteststudio.epubreader;
 
 import group.pals.android.lib.ui.filechooser.FileChooserActivity;
 import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;
 import group.pals.android.lib.ui.filechooser.services.IFileProvider;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.google.ads.AdView;
 import com.hotteststudio.model.BookListAdapter;
 import com.hotteststudio.model.EpubInfo;
 import com.hotteststudio.model.UserSettings;
 import com.hotteststudio.model.XMLHandler;
 import com.hotteststudio.util.XAds;
 import com.hotteststudio.util.XCommon;
 
 public class MainActivity extends Activity {
 
 	public static final int FILE_SELECT_CODE = 0;
 	public static String xmlContent = "";
 	
 	public static XMLHandler xmlHandler;
 	public static UserSettings settings;
 	
 	public ListView listBook;
 	public BookListAdapter bookAdapater;
 	
 	public AdView adView;
 	
 	private static final int _ReqChooseFile = 0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		XCommon.strictmodePolicy();
 		
 		// xu li loading xml
 		xmlHandler = new XMLHandler();
 		xmlContent = getXMLSettings();
 		if (xmlContent.length()>1) {
 			Log.d("day ne",">>> " + xmlContent);
 			settings = loadXML(xmlContent);
 		} else {
 			settings = new UserSettings(); 
 		}
 		
 		initGui();
 		//loadRecentEpub(); 
 	}
 
 	/*
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}*/
 
 	public void test() {
 		try {
 			Intent test;
 			test = new Intent(this,Reader.class);
 			//test.putExtra("epub", bookid);
 
 			this.startActivity(test);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void initGui() {
 		listBook = (ListView)findViewById(R.id.bookList);
 		bookAdapater = new BookListAdapter(this,settings.arrRecentEpub);
 		listBook.setAdapter(bookAdapater);
 		listBook.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> av, View v, int position, long id) {
 				Object o = listBook.getItemAtPosition(position);
 				// continue here
 				EpubInfo e = (EpubInfo)o;
 				try {
 					File file = new File(e.path);
 	                if (file.exists()) {
 	        			Intent reader = new Intent(MainActivity.this,Reader.class);
 	        			reader.putExtra("epubPath", e.path);
 	        			startActivity(reader);
 	                }
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		});
 		
 		adView = (AdView) findViewById(R.id.adView);
     	XAds xads = new XAds(adView);
     	xads.loadAds();
 	}
 	
 	public void openFileChooser(View v) {
 		showFileChooser();
 	}
 	
 	private void showFileChooser() {
 		Intent intent = new Intent(getApplicationContext(), FileChooserActivity.class);
		intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile("/sdcard/"));
 		intent.putExtra(FileChooserActivity._MultiSelection, false);
 		intent.putExtra(FileChooserActivity._RegexFilenameFilter, "(?si).*\\.(epub)$");
 		startActivityForResult(intent, _ReqChooseFile);
 		
 //        Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
 //        intent.setType("file/*"); 
 //        intent.addCategory(Intent.CATEGORY_OPENABLE);
 //    
 //        try {
 //            startActivityForResult(
 //                    Intent.createChooser(intent, "Select an epub file"),
 //                    FILE_SELECT_CODE);
 //        } catch (android.content.ActivityNotFoundException ex) {
 //            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
 //        }
     }
 	
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             /*case FILE_SELECT_CODE:      
             	try {
 		            if (resultCode == RESULT_OK) {  
 		                // Get the Uri of the selected file 
 		                Uri uri = data.getData();
 		                // Get the path
 		                String path = getPath(this, uri);
 		                //Log.d("po tay", "File Path: " + path);
 		                // Get the file instance
 		                File file = new File(path);
 		                if (file.getName().endsWith(".epub")) {
 	            			Intent reader = new Intent(this,Reader.class);
 	            			reader.putExtra("epubPath", path);
 	            			this.startActivity(reader);
 		                } else {
 		                	Toast.makeText(this, "Error: Wrong format file.", Toast.LENGTH_LONG).show();
 		                }
 		                //Log.d("po tay", "File Name: " + file.getParent() + " > " + file.getAbsolutePath());
 		            }        
             	} catch (Exception e) {
             		e.printStackTrace();
             	}
             break;*/
             case _ReqChooseFile:
             	try {
 		            if (resultCode == RESULT_OK) {  
 		            	/*
 		                 * you can use two flags included in data
 		                 */
 		                IFileProvider.FilterMode filterMode = (IFileProvider.FilterMode)
 		                    data.getSerializableExtra(FileChooserActivity._FilterMode);
 		                boolean saveDialog = data.getBooleanExtra(FileChooserActivity._SaveDialog, false);
 
 		                /*
 		                 * a list of files will always return,
 		                 * if selection mode is single, the list contains one file
 		                 */
 		                List<LocalFile> files = (List<LocalFile>)data.getSerializableExtra(FileChooserActivity._Results);
 		                
 		                for (File file : files) {
 		                	//File file = new File(path);
 			                if (file.getName().endsWith(".epub")) {
 		            			Intent reader = new Intent(this,Reader.class);
 		            			reader.putExtra("epubPath", file.getPath());
 		            			this.startActivity(reader);
 			                } else {
 			                	Toast.makeText(this, "Error: Wrong format file.", Toast.LENGTH_LONG).show();
 			                }
 		                }
 		            }
             	} catch (Exception e) {
             		e.printStackTrace();
             	}
             break;
         }
         super.onActivityResult(requestCode, resultCode, data);
     }
 	
 	private String getPath(Context context, Uri uri) throws URISyntaxException {
         if ("content".equalsIgnoreCase(uri.getScheme())) {
             String[] projection = { "_data" };
             Cursor cursor = null;
 
             try {
                 cursor = context.getContentResolver().query(uri, projection, null, null, null);
                 int column_index = cursor
                 .getColumnIndexOrThrow("_data");
                 if (cursor.moveToFirst()) {
                     return cursor.getString(column_index);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
         else if ("file".equalsIgnoreCase(uri.getScheme())) {
             return uri.getPath();
         }
 
         return null;
     }
 	
 	public UserSettings loadXML(String xml) {
 		try {
 			return (UserSettings)xmlHandler.xstream.fromXML(xml);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public String getXMLSettings() {
 		try {
 			return XCommon.getFileContent(XCommon.XML_FILE);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		xmlContent = getXMLSettings();
 		Log.d("aaa","resume activity " + xmlContent);
 		if (xmlContent.length()>1) {
 			settings = loadXML(xmlContent);
 		} else {
 			//settings = new UserSettings(); 
 		}
 		
 		Log.d("aaa","resume activity " + settings.arrRecentEpub.size());
 		/*
 		runOnUiThread(new Runnable() {
 			
 			@Override
 			public void run() {
 				bookAdapater = new BookListAdapter(MainActivity.this,settings.arrRecentEpub);
 				bookAdapater.notifyDataSetChanged();
 				listBook.invalidateViews();
 			}
 		});
 		*/
 		
 		bookAdapater = new BookListAdapter(MainActivity.this,settings.arrRecentEpub);
 		bookAdapater.notifyDataSetChanged();
 		listBook.invalidateViews();
 	}
 	
 	
 	// xu li menu
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.layout.menubar, menu);
         return true;
     }
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_settings:
 				Intent setting = new Intent(MainActivity.this,Settings.class);
     			startActivity(setting);
 				return true;
 			default: 
 				return false;
 		}
 	}
 }
