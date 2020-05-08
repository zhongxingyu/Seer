 package sachjo.develop.tool.android_layout_xml_viewer;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.content.res.XmlResourceParser;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.view.View.OnClickListener;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.util.Xml;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 
 public class MainActivity extends Activity implements OnClickListener,
 		OnItemClickListener {
 	static final int MODE_EDITOR = 0;
 	static final int MODE_VIEWER = 1;
 	static final int MODE_FILER = 2;
 	static final String ANDROID_XML_VIEWER = "AndroidXMLViewer";
 
 	private SharedPreferences mPrefs;
 	private int mMode;
 	private String mXmlText;
 	private String mCurrentPath;
 	
 	private String[] layoutNameArray;
 	private Integer[] layoutIdArray;
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		boolean ret = true;
 		switch (item.getItemId()) {
 		default:
 			ret = super.onOptionsItemSelected(item);
 			break;
 		case R.id.menu_initialize:
 			mMode = MODE_EDITOR;
 			mXmlText = "";
 			setView();
 			ret = true;
 			break;
 		case R.id.menu_editor:
 			mMode = MODE_EDITOR;
 			setView();
 			ret = true;
 			break;
 		case R.id.menu_viewer:
 			if(mMode == MODE_EDITOR){
 				EditText editText = (EditText) findViewById(R.id.editText1);
 				mXmlText = editText.getText().toString();
 			}
 			mMode = MODE_VIEWER;
 			setView();
 			break;
 		case R.id.menu_filer:
 			mMode = MODE_FILER;
 			setView();
 			break;
 		}
 		return ret;
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if(mMode == MODE_EDITOR) {
 			EditText editText = (EditText) findViewById(R.id.editText1);
 			mXmlText = editText.getText().toString();
 			outState.putString("XMLText", mXmlText);
 		}
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		mXmlText = savedInstanceState.getString("XMLText");
 		if(mMode == MODE_EDITOR) {
 			EditText editText = (EditText) findViewById(R.id.editText1);
 			editText.setText(mXmlText);
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		SharedPreferences.Editor ed = mPrefs.edit();
 		ed.putString("XMLText", mXmlText);
 		ed.putInt("AppMode", mMode);
 		ed.putString("CurrentPath", mCurrentPath);
 		ed.commit();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 //		case R.id.button1:
 //			EditText editText = (EditText) findViewById(R.id.editText1);
 //			mXmlText = editText.getText().toString();
 //			mMode = MODE_VIEWER;
 //			setView(null);
 //			break;
 //		case R.id.button2:
 //			Resources res = getResources();
 //			XmlResourceParser resParser = res.getLayout(layoutIdArray[0]);
 //			setContentView(this.getLayoutInflater().inflate(resParser, null));
 //			Button mGoBtn = (Button) findViewById(R.id.button1);
 //			mGoBtn.setOnClickListener(this);
 //			Button mGoBtn2 = (Button) findViewById(R.id.button2);
 //			mGoBtn2.setOnClickListener(this);
 //			Button mGoBtn3 = (Button) findViewById(R.id.button3);
 //			mGoBtn3.setOnClickListener(this);
 //			break;
 //		case R.id.button3:
 //			mMode = MODE_EDITOR;
 //			setView(null);
 //			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mPrefs = getSharedPreferences(ANDROID_XML_VIEWER, MODE_PRIVATE);
 		mMode = mPrefs.getInt("AppMode", MODE_EDITOR);
 		mXmlText = mPrefs.getString("XMLText", "");
 		mCurrentPath = mPrefs.getString("CurrentPath","/");
 		
 		if (mXmlText.isEmpty() && mMode == MODE_VIEWER) {
 			mMode = MODE_EDITOR;
 		}
 		if(savedInstanceState != null) {
 			mXmlText = savedInstanceState.getString("XMLText");
 		}
 		setView();
 	}
 	
 	private void setView() {
 		switch(mMode) {
 		case MODE_EDITOR:
 			setContentView(R.layout.activity_list);
 			EditText editText = (EditText) findViewById(R.id.editText1);
 	
 //			Button mGoBtn = (Button) findViewById(R.id.button1);
 //			mGoBtn.setOnClickListener(this);
 //			Button mGoBtn2 = (Button) findViewById(R.id.button2);
 //			mGoBtn2.setOnClickListener(this);
 //			Button mGoBtn3 = (Button) findViewById(R.id.button3);
 //			mGoBtn3.setOnClickListener(this);
 			java.lang.reflect.Field[] fields = R.layout.class.getFields();
 			List<String> layoutNameList = new ArrayList<String>();
 			List<Integer> layoutIdList = new ArrayList<Integer>();
 			R.layout layoutInstance = new R.layout();
 			for (Field field : fields) {
 				layoutNameList.add(field.getName());
 				try {
 					layoutIdList.add(field.getInt(layoutInstance));
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				}
 			}
 			layoutNameArray = layoutNameList.toArray(new String[0]);
 			layoutIdArray = layoutIdList.toArray(new Integer[0]);
 			ListView listView = (ListView) findViewById(R.id.listView1);
 			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 					R.layout.list, layoutNameArray);
 			listView.setAdapter(adapter);
 			// リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
 			listView.setOnItemClickListener(this);
 			editText.setText(mXmlText);
 			break;
 		case MODE_FILER:
 			File currentPath = new File(mCurrentPath);
 			//System.out.println("selected is " + currentPath.getPath() + "," + currentPath.getName());
 			if(currentPath.isDirectory()){
 				File[] ffTemp = currentPath.listFiles();
 				File[] ff;
 				if(currentPath.getPath().equals("/")){
 					ff = ffTemp;
 				}
 				else {
 					if(ffTemp != null){
 						ff = new File[ffTemp.length+1];
 						System.arraycopy(ffTemp, 0, ff, 1, ffTemp.length);
 					}
 					else {
 						ff = new File[1];
 					}
 					ff[0] = currentPath.getParentFile();
 				}
 				if(ff != null) {
					Arrays.sort(ff);
 					System.out.println("list is null");
 					ArrayAdapter<File> adapter_filelist = new ArrayAdapter<File>(this,
 							R.layout.list, ff);
 					setContentView(R.layout.activity_filer);
 					ListView listView_file = (ListView) findViewById(R.id.listView_file);
 					listView_file.setAdapter(adapter_filelist);
 
 					// リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
 					listView_file.setOnItemClickListener(this);
 				}
 			}
 			else {
 				System.out.println("not directory");
 				mCurrentPath = currentPath.getParent();
 				if(!currentPath.getParentFile().getName().equals("layout")) {
 					// ダイアログの表示
 					AlertDialog.Builder dlg;
 					dlg = new AlertDialog.Builder(this);
 					dlg.setTitle("Not opened.");
 					dlg.setMessage("Select XML file in layout folder.");
 					dlg.show();
 				}
 				else if(!currentPath.getName().endsWith("xml")) {
 					AlertDialog.Builder dlg;
 					dlg = new AlertDialog.Builder(this);
 					dlg.setTitle("Can'T opened.");
 					dlg.setMessage("Select XML file.");
 					dlg.show();
 				}
 				else {
 					FileInputStream fileInputStream;
 	                try {
 						fileInputStream = new FileInputStream(currentPath.getPath());
 		                byte[] readBytes = new byte[fileInputStream.available()];
 		                fileInputStream.read(readBytes);
 		                mXmlText = new String(readBytes);
 		                mMode = MODE_EDITOR;
 		                setView();
 					} catch (FileNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			break;
 		case MODE_VIEWER:
 			if (mXmlText.isEmpty()) {
 				// ダイアログの表示
 				AlertDialog.Builder dlg;
 				dlg = new AlertDialog.Builder(this);
 				dlg.setTitle("String Empty");
 				dlg.setMessage("Select from list or input text.");
 				dlg.show();
 				mMode = MODE_EDITOR;
 			} else {
 				InputStream bais = null;
 				try {
 					bais = new ByteArrayInputStream(mXmlText.getBytes("utf-8"));
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 				XmlPullParserFactory factory;
 				try {
 					factory = XmlPullParserFactory.newInstance();
 					factory.setNamespaceAware(true);
 
 					XmlPullParser parser = Xml.newPullParser();
 					try {
 						parser.setInput(bais, "utf-8");
 					} catch (XmlPullParserException e) {
 						e.printStackTrace();
 					}
 					try {
 						DisplayMetrics metrics = new DisplayMetrics();
 						getWindowManager().getDefaultDisplay().getMetrics(
 								metrics);
 						LayoutInflater orgLayInf = this.getLayoutInflater();
 						LayoutInflater2 layInf = new LayoutInflater2(orgLayInf,
 								this, metrics);
 						View view = layInf.inflate(parser, null);
 						setContentView(view);
 					} catch (Exception e) {
 						e.printStackTrace();
 						// ダイアログの表示
 						AlertDialog.Builder dlg;
 						dlg = new AlertDialog.Builder(this);
 						dlg.setTitle("inflate error");
 						dlg.setMessage(e.getMessage());
 						dlg.show();
 						mMode = MODE_EDITOR;
 						setView();
 					}
 				} catch (XmlPullParserException e1) {
 					e1.printStackTrace();
 				}
 			}
 			break;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		Log.d("test", "clicked at " + position);
 		switch(parent.getId()) {
 		case R.id.listView1:
 			Resources res = getResources();
 			XmlResourceParser resParser = res.getLayout(layoutIdArray[position]);
 			String parsedStr = parseResXmlToStr(resParser);
 			EditText editText = (EditText) findViewById(R.id.editText1);
 			if(parsedStr != null) {
 				mXmlText = parsedStr;
 				editText.setText(parsedStr);
 			}
 			break;
 		case R.id.listView_file:
 			File currentPath =(File)parent.getItemAtPosition(position);
 			mCurrentPath = currentPath.getPath();
 			setView();
 			break;
 		}
 	}
 	
 	private String parseResXmlToStr(XmlResourceParser resParser) {
 		int eventType;
 		String TAG = "resParseTest";
 		String outStr = "";
 		int nestNum = -1;
 		boolean isInTag = false;
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("http://schemas.android.com/apk/res/android", "android");
 		map.put("http://schemas.android.com/tools", "tools");
 		try {
 			eventType = resParser.getEventType();
 			while (eventType != XmlPullParser.END_DOCUMENT) {
 				if (eventType == XmlPullParser.START_DOCUMENT) {
 					Log.e(TAG, "Start document");
 				} else if (eventType == XmlPullParser.END_DOCUMENT) {
 					Log.e(TAG, "End document");
 				} else if (eventType == XmlPullParser.START_TAG) {
 					Log.e(TAG, "Start tag " + resParser.getName());
 					if (isInTag) {
 						for (int i = 0; i < nestNum; i++) {
 							outStr += "    ";
 						}
 						outStr += " >\n";
 						nestNum++;
 						for (int i = 0; i < nestNum; i++) {
 							outStr += "    ";
 						}
 					} else {
 						nestNum++;
 						for (int i = 0; i < nestNum; i++) {
 							outStr += "    ";
 						}
 						isInTag = true;
 					}
 					String name = resParser.getName();
 					if (name.equals("RelativeLayout") || name.equals("Button")
 							|| name.equals("ListView")
 							|| name.equals("EditText")) {
 						name = "android.widget." + name;
 					}
 					outStr += "<" + name;
 					if (nestNum == 0) {
 						for (String s : map.keySet()) {
 							outStr += " xmlns:" + map.get(s) + "=\"" + s
 									+ "\"\n";
 						}
 					}
 					int num = resParser.getAttributeCount();
 					String msg = "";
 					for (int j = 0; j < num; j++) {
 						msg += "" + resParser.getAttributeName(j) + ","
 								+ resParser.getAttributeType(j) + ","
 								+ resParser.getAttributeValue(j) + "\n";
 						Log.e(TAG, "Tag Attribute " + msg);
 						if (map.containsKey(resParser.getAttributeNamespace(j))) {
 							outStr += " "
 									+ map.get(resParser
 											.getAttributeNamespace(j));
 						} else {
 							outStr += " " + resParser.getAttributeNamespace(j);
 						}
 						outStr += ":" + resParser.getAttributeName(j) + "=\""
 								+ resParser.getAttributeValue(j) + "\"\n";
 						for (int i = 0; i < nestNum; i++) {
 							outStr += "    ";
 						}
 					}
 				} else if (eventType == XmlPullParser.END_TAG) {
 					Log.e(TAG, "End tag " + resParser.getName());
 					for (int i = 0; i < nestNum; i++) {
 						outStr += "    ";
 					}
 					nestNum--;
 					if (isInTag) {
 						outStr += "/>\n";
 						isInTag = false;
 					} else {
 						String name = resParser.getName();
 						if (name.equals("RelativeLayout")
 								|| name.equals("Button")
 								|| name.equals("ListView")
 								|| name.equals("EditText")) {
 							name = "android.widget." + name;
 						}
 						outStr += "</" + name + ">\n";
 					}
 				} else if (eventType == XmlPullParser.TEXT) {
 					Log.e(TAG, "Text " + resParser.getText());
 				}
 				eventType = resParser.next();
 			}
 		} catch (XmlPullParserException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return outStr;
 	}
 }
