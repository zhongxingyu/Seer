 package jp.hidetobara.pasteltouch;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.RectF;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 
 public class PastelTouchActivity extends Activity
 	implements View.OnClickListener, DialogInterface.OnClickListener
 {
     public static final String APPLICATION_NAME = "PastelTouch";
 	private static final String TAG = "My";
 
 	final static int MENU_FILE = 100;
 	final static int MENU_FILE_NEW = 101;
 	final static int MENU_FILE_OPEN = 102;
 	final static int MENU_FILE_SAVE = 103;
 	
 	final static int MENU_ZOOM = 400;
 	final static int MENU_PEN_ACTIVITY = 500;
 	
 	final static int MENU_DETAIL = 900;
 	
 	final static int MENU_YESNO_NEW = 1001;
 	final static int MENU_YESNO_LOAD = 1002;
 	final static int MENU_YESNO_OVERWRITE = 1003;
 	
 	Bitmap _Bitmap;
 	CanvasView _Canvas;
 	Uri _Uri = null;
 	boolean _IsCreating = false;
 	String _Filehead;
 	
 	int _MenuMode = 0;
 	ImageView _IconPen, _IconFile, _IconSize, _IconSurface;
 	
 	TextView _Debug;
 	public void dp(String s){ if(s==null)return; Log.d(TAG,s); if(_Debug!=null)_Debug.setText(s); }
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
     	Utility.context = this;
     	
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
 		_Canvas = (CanvasView)findViewById(R.id.canvasMain);
 
         Intent i = (Intent)getIntent();
         String action = i.getAction();
         if(Intent.ACTION_GET_CONTENT.equals(action)) _Uri = i.getData();
         if(Intent.ACTION_SEND.equals(action)) _Uri = i.getParcelableExtra(Intent.EXTRA_STREAM);
     	_IsCreating = true;
 
     	createButtons();
     	
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	//省電力オフ
     }
     
     private void createButtons()
     {
     	//ペン変更
     	_IconPen = (ImageView)findViewById(R.id.pen_change);
     	_IconPen.setOnClickListener(this);
     	//サイズ
     	_IconSize = (ImageView)findViewById(R.id.size_change);
     	_IconSize.setOnClickListener(this);    	
     	//ファイル変更
     	_IconFile = (ImageView)findViewById(R.id.file_change);
     	_IconFile.setOnClickListener(this);
     	//表面
     	_IconSurface = (ImageView)findViewById(R.id.canvas_surface);
     	_IconSurface.setOnClickListener(this);
     }
     
 	@Override
 	public void onClick(View v)
 	{
 		if(v == _IconPen || v == _IconSize || v == _IconSurface)
 		{
 			invokePenActivity(v);
 		}
 		if(v == _IconFile)
 		{
 			invokeFileMenu();
 		}
 	}
     
 	// ダイアログの処理
 	@Override
 	public void onClick(DialogInterface dialog, int which)
 	{
 		if(_MenuMode == MENU_FILE)
 		{
 			if(which == 0){	//new
 				invokeDialogYesNo(MENU_YESNO_NEW, "現在のキャンバスを破棄しますか？");
 			}else if(which == 1){	//load
 	        	Intent i = new Intent(Intent.ACTION_PICK);
 	        	i.setType("image/*");
 	        	startActivityForResult(i, MENU_FILE_OPEN);
 			}else if(which == 2 /*save*/|| which == 3 /*overwrite*/){
 				if(which == 2 || _Filehead == null)
 				{
 					_Filehead = getCurrentName();
 		        	File file = Utility.saveBitmapPng(_Filehead, _Canvas.getBitmap());
 		        	registerFileToGallery(file);
 				}else{
 					invokeDialogYesNo(MENU_YESNO_OVERWRITE, "キャンバスを上書きしますか？");
 				}
 			}
 			return;
 		}
 		if(_MenuMode == MENU_ZOOM)
 		{
 			if(which == 0) _Canvas.setBitmapFit();
 			if(which == 1) _Canvas.setBitmapZoom(1.0f);
 			if(which == 2) _Canvas.setBitmapZoom(2.0f);
 			if(which == 3) _Canvas.setBitmapZoom(4.0f);
 			return;
 		}
 		if(_MenuMode == MENU_YESNO_NEW)
 		{
 			if(which == -1) handleNewFile();
 			return;
 		}
 		if(_MenuMode == MENU_YESNO_OVERWRITE)
 		{
 			if(which == -1)
 			{
 	        	unregisterFileToGallery(_Filehead);
 	        	File file = Utility.saveBitmapPng(_Filehead, _Canvas.getBitmap());
 	        	registerFileToGallery(file);
 			}
 			return;
 		}
 	}
     
     @Override
     public boolean dispatchKeyEvent(KeyEvent event)
     {
     	if(event.getAction() == KeyEvent.ACTION_DOWN)
     	{
     		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
     			_Canvas.popHistoryBitmap();
     			return true;
     		}
     		if(event.getKeyCode() == KeyEvent.KEYCODE_SEARCH){
     			invokeZoomMenu();
     			return true;
     		}
     	}
     	return super.dispatchKeyEvent(event);
     }
 	
     @Override
     public void onWindowFocusChanged(boolean hasFocus)
     {
     	super.onWindowFocusChanged(hasFocus);
     	if( _IsCreating )
     	{
     		_IsCreating = false;
         	if(_Uri != null){
         		handleUriFile(_Uri);
         	}else{
         		handleNewFile();
         	}
         	
         	setDefaultColor(0, 0, 0);
         	_Canvas.setRadius(4);
     	}
     	if(_Canvas != null) _Canvas.doDraw();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
 		menu.addSubMenu(0, MENU_FILE, Menu.NONE, "ファイル");
 		menu.addSubMenu(0, MENU_PEN_ACTIVITY, Menu.NONE, "ペン");
 		menu.addSubMenu(0, MENU_ZOOM, Menu.NONE, "倍率");
 		
         return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
 		switch (item.getItemId())
 		{
 		case MENU_FILE:
 			invokeFileMenu();
 			break;
 		case MENU_ZOOM:
 			invokeZoomMenu();
 			break;
 		case MENU_PEN_ACTIVITY:
 			invokePenActivity(_IconPen);
 			break;
 		}
 		return true;
     }
     
     private void setDefaultColor(int red, int green, int blue)
     {
     	_Canvas.setColor(red, green, blue, 224);
     	_Canvas.setBlot(32, 32, 16);		_Canvas.setRemain(0, 128, 8);		_Canvas.setBase(24);
     }
     
     private void setDefaultFinger(int alpha, int remain)
     {
     	_Canvas.setColor(192, 192, 192, alpha);
     	_Canvas.setBlot(32, 128, 4);		_Canvas.setRemain(2, remain, 4);		_Canvas.setBase(0);	
     }
     
     private Display getDisplay()
     {
     	WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
     	return wm.getDefaultDisplay();
     }
     
     private void invokeFileMenu()
     {
 		String[] items = new String[]{"新規", "読み込み", "新規保存", "上書き保存"};
 		_MenuMode = MENU_FILE;
 		new AlertDialog.Builder(this).setTitle("ファイル").setItems(items, this).show();    	
     }
     private void invokeZoomMenu()
     {
 		String[] items = new String[]{"FIT", "X1", "X2", "X4"};
 		_MenuMode = MENU_ZOOM;
 		new AlertDialog.Builder(this).setTitle("倍率").setItems(items, this).show();	    	
     }
     private void invokeDialogYesNo(int mode, String m)
     {
     	_MenuMode = mode;
     	new AlertDialog.Builder(this).setTitle("確認").setMessage(m)
     		.setPositiveButton("はい", this)
     		.setNegativeButton("いいえ", this).show();
     }
     
     private void invokePenActivity(View v)
     {
     	Intent i = new Intent(getApplicationContext(), PenChangeActivity.class);
     	if(v == _IconPen) i.putExtra(PenChangeActivity.NAME_MODE, PenChangeActivity.MODE_PEN);
     	else if(v == _IconSize) i.putExtra(PenChangeActivity.NAME_MODE, PenChangeActivity.MODE_SIZE);
     	else if(v == _IconSurface) i.putExtra(PenChangeActivity.NAME_MODE, PenChangeActivity.MODE_SURFACE);
     	startActivityForResult(i, MENU_PEN_ACTIVITY);
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent i)
     {
     	if(requestCode == MENU_FILE_OPEN && resultCode == RESULT_OK)
     	{
     		Uri uri = i.getData();
     		handleUriFile(uri);
     		return;
     	}
     	if(requestCode == MENU_PEN_ACTIVITY && resultCode == RESULT_OK)
     	{
     		int mode = i.getIntExtra(PenChangeActivity.NAME_MODE, 0);
     		if(mode == PenChangeActivity.MODE_PEN) handlePenActivityResult(i);
     		if(mode == PenChangeActivity.MODE_SIZE) handleSizeActivityResult(i);
     		if(mode == PenChangeActivity.MODE_SURFACE) handleSurfaceActivityResult(i);
     		return;
     	}
     	super.onActivityResult(requestCode, resultCode, i);
     }
     
     private void handleUriFile(Uri uri)
     {
     	if(uri == null) return;
     	
     	_Uri = uri;
 		String path = getUriToPath(uri);
		_Filehead = checkPastelTouchName(path);
		
 		_Bitmap = Utility.loadBitmap(uri);
		convertFitBitmap();
 		assignBitmap();
     }
     private void handleNewFile()
     {
     	_Uri = null;
     	_Filehead = null;
     	
     	_Canvas.refreshWorking();	//描画のタスクをすぐに止める
     	newBitmap();
 		assignBitmap();
     }
     private void handlePenActivityResult(Intent i)
     {
 		int type = i.getIntExtra(PenChangeActivity.NAME_TYPE, 0);
 		if(type == PenChangeActivity.PEN_PASTEL){
 			int c = i.getIntExtra(PenChangeActivity.NAME_COLOR, 0);
 			if(c == 0) return;
 			
 			setDefaultColor(Color.red(c), Color.green(c), Color.blue(c));
 		}
 		else if(type == PenChangeActivity.PEN_FINGER) {			
 			setDefaultFinger(224, 128);
 		}
 		else if(type == PenChangeActivity.PEN_BRUSH){			
 			setDefaultFinger(64, 224);
 		}
 		
 		int r = i.getIntExtra(PenChangeActivity.NAME_R, 0);
 		if(r > 0) _IconPen.setImageResource(r);
     }
     private void handleSizeActivityResult(Intent i)
     {
 		int s = i.getIntExtra(PenChangeActivity.NAME_SIZE, 0);
 		if(s == 0) return;
 		
 		_Canvas.setRadius(s);
 		
 		int r = i.getIntExtra(PenChangeActivity.NAME_R, 0);
 		if(r > 0) _IconSize.setImageResource(r);
     }
     private void handleSurfaceActivityResult(Intent i)
     {
     	String s = i.getStringExtra(PenChangeActivity.NAME_SURFACE);
     	if(s.equals("dither")) _Canvas.setSurface(CanvasView.SURFACE_DITHER);
     	else if(s.equals("random")) _Canvas.setSurface(CanvasView.SURFACE_RANDOM);
     	else _Canvas.setSurface(CanvasView.SURFACE_FLAT);
     	
 		int r = i.getIntExtra(PenChangeActivity.NAME_R, 0);
 		if(r > 0) _IconSurface.setImageResource(r);    	
     }
     
     private void newBitmap()
     {
     	Display disp = getDisplay();
     	_Bitmap = Bitmap.createBitmap(disp.getWidth(), disp.getHeight(), Config.ARGB_8888);
     	_Bitmap.eraseColor(Color.WHITE);
     }
     
     private void convertFitBitmap()
     {
     	if(_Bitmap == null) return;
     	Bitmap bitmap = _Bitmap;
     	try
     	{
 			Display display = getDisplay();
 			Matrix m = makeFitMatrix(bitmap, display);
 			RectF r = new RectF(0.0f, 0.0f, bitmap.getWidth(), bitmap.getHeight());
 			m.mapRect(r);
 			_Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true)
 					.copy(Config.ARGB_8888, true);			
     	}
     	catch(Exception ex)
     	{
 			dp(ex.getMessage());
 			_Bitmap = null;
     	}
     }
 
     private Matrix makeFitMatrix(Bitmap b, Display d)
     {
     	Matrix m = new Matrix();
     	int bh = b.getHeight();
     	int bw = b.getWidth();
 
     	boolean isRotated = false;
     	if(d.getWidth() < d.getHeight() && b.getWidth() > b.getHeight()) isRotated = true;
     	if(d.getWidth() > d.getHeight() && b.getWidth() < b.getHeight()) isRotated = true;
     	if(isRotated)
     	{
     		m.postRotate(90.0f);
     		bh = b.getWidth();
     		bw = b.getHeight();
     	}
 		float sw = (float)d.getWidth() / (float)bw;
 		float sh = (float)d.getHeight() / (float)bh;
 		float scale = sw > sh ? sh : sw;
 		if( scale > 1.0f ) scale = 1.0f;
 		m.postScale(scale, scale);
     	return m;
     }
     
     private void assignBitmap()
     {
     	if(_Bitmap == null || _Canvas == null) return;
 
     	_Canvas.setBitmap(_Bitmap);	//Bitmapの適応
 		_Canvas.setActivity(this);	//activityの登録
 		_Canvas.reinitialize();		//初期化
 		_Canvas.setBitmapFit(); 	//表示をFitさせる
     }
     
     public void registerFileToGallery(File file)
     {
     	if( file == null ) return;
     	
         String filepath = file.getPath();
         String filename = file.getName();
         String suffix   = Utility.getSuffix(file);
         String mimetype = "";
         
         if ((suffix.equals("jpg")) || (suffix.equals("jpeg"))) {
             mimetype = "image/jpeg";
         }else if (suffix.equals("gif")) {
             mimetype = "image/gif";
         }else if (suffix.equals("png")) {
             mimetype = "image/png";
         }
         
         if (mimetype != "") {
             ContentResolver contentresolver = this.getContentResolver();
             ContentValues contentvalues = new ContentValues();
             contentvalues.put(MediaStore.Images.Media.MIME_TYPE, mimetype);
             contentvalues.put(MediaStore.Images.Media.DATA, filepath);
             contentvalues.put(MediaStore.Images.Media.TITLE, filename);
             contentvalues.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
             Uri uri = contentresolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentvalues);
             dp(uri.getPath());
         }else{
         	dp("Fail: ギャラリーへの登録");
         }
     }
 
     public void unregisterFileToGallery(String name)
     {
     	if( name.length()==0 ) return;
     	
     	String path = Utility.getRootPath() + "/" + name + ".png";
 		ContentResolver resolver = this.getContentResolver();
 		int affect = resolver.delete(
 				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
 				MediaStore.Images.Media.DATA + " = '" + path + "'", null);
 		if(affect == 0) dp("Fail: ギャラリーへの抹消");
     }
     
     /*
      * URI から path へ変換する
      */
     private String getUriToPath(Uri uri)
     {
 		Cursor c = getContentResolver().query(uri, null, null, null, null);
 		c.moveToFirst();
 		return c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
     }
     
     /*
      * Pastel Touch 用の名前かどうか調べる
      */
     private String checkPastelTouchName(String path)
     {
     	String[] cells = path.split("/");
     	if(cells.length < 2) return null;
     	
     	String appname = cells[cells.length-2];
     	String name = cells[cells.length-1];
     	if(!appname.equals(APPLICATION_NAME)) return null;
     	
     	int last = name.lastIndexOf(".");
     	String head = name.substring(0, last);
     	
     	return head;
     }
         
     private String getCurrentName()
     {
     	Date date = new Date(System.currentTimeMillis());
     	SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
     	return format.format(date);
     }
     
 ///// ゴミ置き場
 /*
     @Override
     protected void onSaveInstanceState(Bundle outState)
     {
     	super.onSaveInstanceState(outState);
     	if(_Uri != null)
     	{
     		try{ outState.putString("URI", _Uri.toString()); }
     		catch(Exception ex){ dp(ex.getMessage()); }
     	}
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle inState)
     {
     	super.onRestoreInstanceState(inState);
     	String data = inState.getString("URI");
     	if(data.length() > 0)
     	{
     		_Uri = Uri.parse(data);
     		loadBitmap(_Uri);
         	displayBitmap();
     	}
     }
 */
 }
