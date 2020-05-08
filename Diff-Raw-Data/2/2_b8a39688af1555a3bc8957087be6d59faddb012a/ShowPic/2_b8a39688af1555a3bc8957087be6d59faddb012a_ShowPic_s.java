 /**
  * 
  */
 package com.k99k.keel.wallpaper;
 
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.youmi.android.AdListener;
 
 import com.admob.android.ads.AdView;
 import com.k99k.keel.wallpaper.R;
 import com.wooboo.adlib_android.WoobooAdView;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 import android.widget.RelativeLayout.LayoutParams;
 
 /**
  * 显示大图片
  * @author keel
  *
  */
 public class ShowPic extends Activity implements AdListener {
 	
 	private static final int DIALOG_ERR_WEBPIC_404 = 2;
 	private static final int DIALOG_ERR_NOSDCARD = 3;
 	private static final int DIALOG_ERR_SAVE = 4;
 	private static final int DIALOG_OK_SAVE = 5;
 	private static final int DIALOG_SET_WALLPAPER = 6;
 	private static final int DIALOG_ERR_SET_WALLPAPER = 7;
 	
 	private static final int MENU_HELP = 1;
 	private static final int MENU_SETWALL = 6;
 	private static final int MENU_ABOUT = 8;
 	private static final int MENU_SAVE = 7;
 	private static final int MENU_EXIT = 5;
 	
 	private static final int SCROLL_INIT = 1;
 	private static final int SCROLL_RESIZE = 2;
 	
 	private static final int ADD_STAR_OK = 100;
 	private static final int ADD_STAR_FAIL = 101;
 	private static final int DEL_STAR_OK = 102;
 	private static final int DEL_STAR_FAIL = 103;
 	private static final int SET_WALL_OK = 104;
 	
 	private static final int LOAD_AD = 200;
 	
 	public static final int RESULT_OK = 10;
 	private static ProgressDialog dialog ;
 	//private static ImageView bigImg;
 	private Button b_back;
 	private Button b_save;
 	private Button b_setWall;
 	private Button b_star;
 	//private int picId;
 	private static final String TAG = "ShowPic";
 //	private String remoteDomain;// = this.getString(R.string.remoteDomain);
 	private String picUrl;
 	private Handler picHandler;
 	//private ProgressBar bar;
 	private ImageView bigImgView;
 	private View progress;
 	private HorizontalScrollView centerPic;
 	private Bitmap picToSave;
 	private String saveSdPath;// = this.getString(R.string.saveSdPath);
 	private String picFileName;
 	
 //	/**
 //	 * 用于将加星后的cate#picId直接存到K99KWall的 myStarList中
 //	 */
 //	private String starCatePicId = "";
 	
 	private String starUrl = "addstar.htm";
 	private String downUrl = "adddown.htm";
 //	/**
 //	 * 大图的objectId
 //	 */
 	private static String pic_oid = "";
 	
 	/**
 	 * 消息处理Handler,用于更新界面
 	 */
 	private Handler mHandler = new Handler(){
 		@Override
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 			switch (msg.what) {
 			case SCROLL_INIT:
 				centerPic.smoothScrollTo(400, 0);
 				break;
 			case SCROLL_RESIZE:
 				centerPic.smoothScrollTo(120, 0);
 				break;
 			case ADD_STAR_OK:
 				Toast.makeText(ShowPic.this, getString(R.string.star_add_ok), Toast.LENGTH_SHORT).show();
 				break;
 			case ADD_STAR_FAIL:
 				Toast.makeText(ShowPic.this, getString(R.string.star_add_err), Toast.LENGTH_SHORT).show();
 				break;
 			case DEL_STAR_OK:
 				Toast.makeText(ShowPic.this, getString(R.string.star_del_ok), Toast.LENGTH_SHORT).show();
 				break;
 			case DEL_STAR_FAIL:
 				Toast.makeText(ShowPic.this, getString(R.string.star_del_err), Toast.LENGTH_SHORT).show();
 				break;
 			case SET_WALL_OK:
 				Toast.makeText(ShowPic.this,getString(R.string.msg_setOK),Toast.LENGTH_SHORT).show();
 				break;
 			case LOAD_AD:
 				loadAdMob();
 				break;
 			default:
 				break;
 			}
 		}
 	};
 	
 	net.youmi.android.AdView youmiAdView;
 	 
     private void loadAdMob(){
         if (ID.getLANG().equals("CN")) {
         	// 先获取远程指令
 			String adType = NetWork
 					.postUrl(ID.remoteAdOrder+"?acti=s", ID.getSmallJsonEnc());
 			if (adType.equals("youmi")) {
 	      	 	//======================= youmi ====================
 	        	youmiAdView = new net.youmi.android.AdView(this,Color.argb(255, 61, 31, 51),Color.argb(255, 204, 204, 204),160);
 	        	LayoutParams lparams = new LayoutParams(LayoutParams.FILL_PARENT,
 	    				LayoutParams.WRAP_CONTENT);
 	        	((LinearLayout)this.findViewById(R.id.SetWall)).addView(youmiAdView,lparams);
 	        	youmiAdView.setAdListener(this);
 	        					
 			}else{
 	    		WoobooAdView ad = new WoobooAdView(this,"5a198962dbd644ddb60062b143270482",Color.argb(255, 61, 31, 51),
 	    				Color.argb(255, 204, 204, 204), false, 28);
 	    		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
 	    				LayoutParams.WRAP_CONTENT);
 	    		ad.setLayoutParams(params);
 	            ((LinearLayout)this.findViewById(R.id.SetWall)).addView(ad);				
 			}
 
 
 		}else{
 			//=======================ADMOB====================
 			com.admob.android.ads.AdView admob2 = new AdView(ShowPic.this);
 	        admob2.setBackgroundColor(Color.argb(255, 61, 31, 51));
 	        admob2.setPrimaryTextColor(Color.argb(255, 204, 204, 204));
 	        admob2.setRequestInterval(30);
 	        admob2.setKeywords(ID.adkey);
 	        admob2.setLayoutParams(LP_FW);
 	        ((LinearLayout)this.findViewById(R.id.SetWall)).addView(admob2);
 		}
     }
     private static final RelativeLayout.LayoutParams LP_FW = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);  
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.showpic);
 		 b_star = (Button)this.findViewById(R.id.b_star1);
 		//bar = (ProgressBar) this.findViewById(R.id.progressbar);
 		progress = this.findViewById(R.id.progress);
 		b_back = (Button) this.findViewById(R.id.b_back);
 		b_save = (Button) this.findViewById(R.id.b_save);
 		b_setWall = (Button) this.findViewById(R.id.b_setwall);
 		bigImgView = (ImageView) this.findViewById(R.id.bigImg);
 		centerPic = (HorizontalScrollView)this.findViewById(R.id.CenterPic);
 		b_back.setText(getString(R.string.bt_back));
 		b_save.setText(getString(R.string.bt_save));
 		b_setWall.setText(getString(R.string.bt_set));
 		
 		picFileName = this.getIntent().getStringExtra("bigImg");
 		//remoteDomain = this.getIntent().getStringExtra("server");
 		saveSdPath = this.getIntent().getStringExtra("saveSdPath");
 		
 		//去掉状态栏
 		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         
 /*		//本地图片
 		if (!isRemote) {
 			//picFileName += ".jpg";
 			Log.d(TAG, "Local picFileName:"+picFileName);
 //			int id = this.getResources().getIdentifier(picFileName, "drawable", this.getPackageName());
 //			bigImgView.setImageResource(id);
 			bigImgView.setImageBitmap(K99KWall.getImageFromAssetFile(this,picFileName));
 			bigImgView.setScaleType(ImageView.ScaleType.CENTER);
 			progress.setVisibility(View.GONE);
 			centerPic.setVisibility(View.VISIBLE);
 		}else{*/
 			picUrl = picFileName;
 			Log.d(TAG, "Remote picFileName:"+picFileName);
 			new LoadThread().start();
 		//}
 		
 		picHandler = new Handler();
 
 		b_back.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				ShowPic.this.finish();
 			}
 		});
 		b_setWall.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showDialog(DIALOG_SET_WALLPAPER);
 			}
 		});
 
 		b_save.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				new SaveThread(true).start();
 			}
 		});
 	    b_star.setOnClickListener(new OnClickListener(){
 			public void onClick(View arg0) {
 				Intent intent = new Intent();
 				//1为显示类别的状态，2为显示四图的状态,3为显示大图的状态,4为加上按星排序的类别状态
 				int s = 3;
 				intent.putExtra("star", s);
 				//不显示排序
 				intent.putExtra("isShowOrder", false);
 				if (K99KWall.myStarList.contains(pic_oid)) {
 					intent.putExtra("isAdd", "n");
 				}
 				intent.setClass(ShowPic.this, StarActivity.class);
 				//startActivity(intent);
 				startActivityForResult(intent,RESULT_OK);
 			}
         });
 	    
 	    mHandler.sendEmptyMessage(LOAD_AD);
 	}
 	
 	private final String getCateAndPicId(){
 		String s = picFileName.substring(2,picFileName.indexOf("."));
 		Pattern pattern = Pattern.compile("([a-z]{2,})(\\d+)");
 		Matcher matcher = pattern.matcher(s);
 		String cate = "abs";
 		String picId = "100";
 		if (matcher.find()) {
 			cate = matcher.group(1);
 			picId = matcher.group(2);
 		}
 		return cate+"#"+picId;
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		//处理加星减星
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (resultCode) {
 		case RESULT_OK:
 			String type = data.getStringExtra("type");
 			//由picFileName得到cate和picId
 			String[] sarr = this.getCateAndPicId().split("#");
 			String cate = sarr[0];
 			String picId = sarr[1];
 			Log.d(TAG, "-----cate:"+cate+" picId:"+picId+" type:"+type);
 			String u = starUrl;
 			dialog = ProgressDialog.show(this, getString(R.string.proc_diag_title), getString(R.string.proc_diag_text));
 			new StarOpt(u, type).start();
 			
 			
 			break;
 
 		default:
 			break;
 		}
 		
 		
 	}
 
 	private class StarOpt extends Thread {
 		private String type;
 		private String url;
 		
 		public StarOpt(String url,String type){
 			this.type = type;
 			this.url = url;
 		}
 		
 		@Override
 		public void run() {
 			String re = ShowPic.this.getRemoteTxt(this.url, this.type);
 			if (dialog != null && dialog.isShowing()) {
 				dialog.dismiss();
 			}
 			if (!re.equals("fail")) {
 				if (this.type.equals("add")) {
 					mHandler.sendEmptyMessage(ADD_STAR_OK);
 					Log.e(TAG, "add star:"+re);
 					K99KWall.myStarList.add(re);
 				}else{
 					mHandler.sendEmptyMessage(DEL_STAR_OK);
 					K99KWall.myStarList.remove(re);
 				}
 			}else{
 				//显示错误
 				if (this.type.equals("add")) {
 					mHandler.sendEmptyMessage(ADD_STAR_FAIL);
 				}else{
 					mHandler.sendEmptyMessage(DEL_STAR_FAIL);
 				}
 			}
 		}
 		
 	}
 	
 
     private final String getRemoteTxt(String url,String type){
     	String str = "";
     	String[] keys = new String[]{"wall","type","pic_oid"};
     	String[] values = new String[]{ID.getSmallJsonEnc(),type,pic_oid};
 //    	HashMap<String,String> paras = new HashMap<String,String>();
 //    	paras.put("wall", ID.getSmallJsonEnc());
 //    	paras.put("type", type);
 //    	paras.put("pic_oid", pic_oid);
     	str = NetWork.postUrl(url, keys,values, 3000, false);
     	
     	/*
     	
 		try {
 			URL aURL = new URL(url);
 			URLConnection conn = aURL.openConnection();
 			conn.setRequestProperty("appVersion", K99KWall.appver+"");
 			conn.setRequestProperty("imei", K99KWall.imei);
 			conn.setRequestProperty("imsi", K99KWall.imsi);
 			conn.setRequestProperty("lang", K99KWall.lang);
 			conn.setRequestProperty("type", type);
 			conn.setRequestProperty("pic_oid", pic_oid);
 			conn.setConnectTimeout(5000);
 			conn.connect();
 			StringBuilder b = new StringBuilder();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
 		
 			String line;
 			while ((line = reader.readLine()) != null) {
 				b.append(line);
 			}
 			reader.close();
 			str = b.toString();
 			Log.d(TAG, "getRemoteTxt OK:" + url+" re:"+str);
 		} catch (IOException e) {
 			Log.e(TAG, "getRemoteTxt Error!"+url, e);
 			return "";
 		} catch (Exception e) {
 			Log.e(TAG, "getRemoteTxt unknown Error!"+url, e);
 			return "";
 		}*/
 		return str;
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         //menu.add(0,MENU_INIT,0,"刷新索引"); 
 		menu.add(0,MENU_SETWALL,0,getString(R.string.menu_setwall));
 		menu.add(0,MENU_SAVE,1,getString(R.string.menu_save));
         menu.add(0,MENU_ABOUT,2,getString(R.string.menu_about));  
         menu.add(0,MENU_HELP,3,getString(R.string.menu_help));  
         menu.add(0,MENU_EXIT,4,getString(R.string.menu_back));  
         return super.onCreateOptionsMenu(menu);
         
     }
     @Override  
     public boolean onOptionsItemSelected(MenuItem item) {  
     	switch (item.getItemId()) {
 		case MENU_ABOUT:
 			showDialog(MENU_ABOUT);
 			break;
 		case MENU_HELP:
 			showDialog(MENU_HELP);
 			break;
 		case MENU_SETWALL:
 			setWall();
 			break;
 		case MENU_SAVE:
 			new SaveThread(true).start();
 			break;
 		case MENU_EXIT:
 			finish();
 			break;
 
 		default:
 			break;
 		}
     	return super.onOptionsItemSelected(item); 
     }
 	
 	@Override
 	public void setWallpaper(Bitmap bitmap) throws IOException {
 		super.setWallpaper(bitmap);
 	}
 	
 	private void setWall(){
 		if (picToSave == null) {
 			showDialog(DIALOG_ERR_SAVE);
 			return;
 		}else{
 			dialog = ProgressDialog.show(this, getString(R.string.proc_diag_title), getString(R.string.proc_diag_text));
 			new Thread(new Runnable(){
 				public void run() {
 					 try {
 							setWallpaper(picToSave);
 						} catch (IOException e) {
 							Log.e(TAG, "set wallpaper failed!",e);
 							showDialog(DIALOG_ERR_SAVE);
 							return;
 						}
 						dialog.dismiss();
 						mHandler.sendEmptyMessage(SET_WALL_OK);
 				}}).start();
 			new SaveThread(false).start();
 		}
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case MENU_ABOUT:
 			 return new AlertDialog.Builder(ShowPic.this)
 	         .setMessage(R.string.about)
 	         .setTitle(getString(R.string.menu_about))
 	         .setPositiveButton("OK", new DialogInterface.OnClickListener() {
 	             public void onClick(DialogInterface dialog, int whichButton) {
 	            	 
 	             }
 	         })
 	         .create();
 		case MENU_HELP:
 			return new AlertDialog.Builder(ShowPic.this)
 	         .setMessage(R.string.help)
 	         .setTitle(getString(R.string.menu_help))
 	         .setPositiveButton("OK", new DialogInterface.OnClickListener() {
 	             public void onClick(DialogInterface dialog, int whichButton) {
 	            	 
 	             }
 	         })
 	         .create();
 
 		case DIALOG_SET_WALLPAPER:
 			return new AlertDialog.Builder(ShowPic.this)
 			.setMessage(getString(R.string.msg_save_confirm))
 			.setNegativeButton(getString(R.string.msg_cancel), new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 							
 						}
 					})
 			.setPositiveButton("OK",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 							// 设置壁纸
 							setWall();
 						}
 					}).create();
 		case DIALOG_ERR_SET_WALLPAPER:
 			return new AlertDialog.Builder(ShowPic.this).setMessage(getString(R.string.msg_wait))
 			.setTitle(getString(R.string.msg_setErr))
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									// 无操作
 								}
 							}).create();			
 		case DIALOG_ERR_WEBPIC_404:
 			return new AlertDialog.Builder(ShowPic.this).setMessage(
 					getString(R.string.msg_loadPicErr))
 			// .setTitle("网络连接错误")
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									// 返回上级
 									ShowPic.this.finish();
 								}
 							}).create();
 		case DIALOG_ERR_NOSDCARD:
 			return new AlertDialog.Builder(ShowPic.this).setMessage(getString(R.string.msg_nocard))
 			// .setTitle("网络连接错误")
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									// 无操作
 								}
 							}).create();
 		case DIALOG_ERR_SAVE:
 			return new AlertDialog.Builder(ShowPic.this).setMessage(getString(R.string.msg_saveErrMsg))
 			// .setTitle("网络连接错误")
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									// 无操作
 								}
 							}).create();
 		case DIALOG_OK_SAVE:
 			return new AlertDialog.Builder(ShowPic.this)
 			.setMessage(getString(R.string.msg_saveOKMsg))
 			.setTitle(getString(R.string.msg_saveOK))
 			.setPositiveButton("OK",
 			new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									// 无操作
 								}
 							}).create();
 		default:
 			break;
 		}
 		return null;
 	}
 	
 	
 	
 	/**
 	 * 保存图片到SD卡
 	 * @param filePath
 	 * @param showResult 是否显示成功的提示
 	 */
 	private final void savePic(String fileName,String path,boolean showResult){
 		
 //			try {
 				
 				//如果SD卡未挂载
 //				if (android.os.Environment.getExternalStorageState() != android.os.Environment.MEDIA_MOUNTED) {
 //					showDialog(DIALOG_ERR_NOSDCARD);
 //					return;
 //				}
 				//FileOutputStream out = this.openFileOutput(fileName, MODE_WORLD_READABLE);//这是保存到/data/package下面，不能定义位置
 		boolean re = IO.savePic(this,fileName, path, this.picToSave);
 		if (showResult) {
 			if (re) {
 				showDialog(DIALOG_OK_SAVE);
 			} else {
 				showDialog(DIALOG_ERR_SAVE);
 			}
 		}
 					
 //				}else{
 					/*//本地图片保存到sd卡
 					FileOutputStream out = new FileOutputStream(filePath);
 					K99KWall.getImageFromAssetFile(this, picFileName).compress(CompressFormat.JPEG, 100, out);
 					
 					out.flush();
 					out.close();*/
 //					if (showResult) {
 //						showDialog(DIALOG_ERR_SAVE);
 //					}
 //					
 //				}
 //			} catch (IOException e) {
 //				Log.e(TAG, getString(R.string.msg_saveErr),e);
 //				showDialog(DIALOG_ERR_SAVE);
 //			}
 		
 	}
 
 	private class SaveThread extends Thread {
 		
 		public SaveThread(boolean showResult){
 			this.showResult = showResult;
 		}
 		private final boolean showResult;
 		
 		@Override
 		public void run() {
 			try {
 				//String[] sarr = ShowPic.this.getCateAndPicId().split("#");
 				//final String cate = sarr[0];
 				//final String picId = sarr[1];
 				picHandler.post(new Runnable() {
 					public void run() {
 						Log.d(TAG, "showResult:"+showResult);
 //						CharSequence timeStr = DateFormat.format("hh-mm-ssaa_MM-dd-yyyy", new Date());
 //						savePic(cate+timeStr+".jpg",saveSdPath,showResult);
 						
 						savePic(pic_oid+".jpg",saveSdPath,showResult);
 					}
 				});
 				
 				String url = downUrl;
 				String re = ShowPic.this.getRemoteTxt(url, "");
 				Log.d(TAG, "DOWN:"+re);
 			} catch (Exception e) {
 				
 				Log.e(TAG, "SaveThread error",e);
 			}
 			
 
 			
 		}
 	}
     @Override
     public boolean dispatchKeyEvent(KeyEvent e) {
 		if (e.getKeyCode() == KeyEvent.KEYCODE_BACK
 				&& e.getAction() == KeyEvent.ACTION_UP) {
 			
 			finish();
 			return true;
 		}
 		return false;
     	//return super.dispatchKeyEvent(event);
     }
     
 	private class LoadThread extends Thread {
 		@Override
 		public void run() {
 			try {
 				String[] pic_oid_arr = new String[1];
 				final Bitmap b = NetWork.getRemotePicWithWallProp(picUrl,pic_oid_arr);
 				pic_oid = pic_oid_arr[0];
 				//Log.e(TAG, "PIC_OID:"+pic_oid);
 				picHandler.post(new Runnable() {
 					public void run() {
 						if (b == null) {
							showDialog(DIALOG_ERR_WEBPIC_404);
 						}else{
 							picToSave = b;;
 							//bigImgView.setImageBitmap(resizePic(b));
 							bigImgView.setAdjustViewBounds(true);
 							bigImgView.setMaxWidth(800);
 							bigImgView.setImageBitmap(b);
 							bigImgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
 							
 							
 						}
 						progress.setVisibility(View.GONE);
 						
 						centerPic.setVisibility(View.VISIBLE);
 						mHandler.sendEmptyMessageDelayed(SCROLL_INIT,500);
 						mHandler.sendEmptyMessageDelayed(SCROLL_RESIZE,1000);
 						
 						//centerPic.smoothScrollTo(0, 0);
 					}
 				});
 			} catch (Exception e) {
 				Log.e(TAG, e.toString());
 			}
 		}
 	}
 	/*
 	 private static final int IO_BUFFER_SIZE = 1024*4;
 	    private static final void copy(InputStream in, OutputStream out) throws IOException {
 	        byte[] b = new byte[IO_BUFFER_SIZE];
 	        int read;
 	        while ((read = in.read(b)) != -1) {
 	            out.write(b, 0, read);
 	        }
 	    }
 		private final Bitmap getRemotePic(String url) {
 			DisplayMetrics dm = new DisplayMetrics();
 			getWindowManager().getDefaultDisplay().getMetrics(dm);
 			int screenH = dm.heightPixels;
 			if (screenH<500) {
 				url = url.replace(".jpg", "_l.jpg");
 			}
 			Log.d(TAG, "screenH:"+screenH+" remote picUrl:" + url);
 			Bitmap bm = null;
 			try {
 				URL aURL = new URL(url);
 				URLConnection conn = aURL.openConnection();
 				conn.setRequestProperty("appVersion", ID.getAppVer()+"");
 				conn.setRequestProperty("imei", K99KWall.imei);
 				conn.setRequestProperty("lang", K99KWall.lang);
 				String sortby = (K99KWall.orderby.equals("random") || K99KWall.orderby.equals("shuffle"))?"time":K99KWall.orderby;
 				conn.setRequestProperty("sortBy", sortby);
 				conn.setRequestProperty("sortType", K99KWall.orderAsc+"");
 				conn.connect();
 				//获取图片id,判断是否已加星(根据同步后的本地加星列表)
 				//处理objid方式加星
 				String oid = conn.getHeaderField("pic_oid");
 				if (oid!=null && oid.length()>0) {
 					pic_oid = oid;
 					Log.d(TAG, "========pic_oid:"+oid);
 				}
 				final BufferedInputStream in = new BufferedInputStream(conn.getInputStream(),
 						IO_BUFFER_SIZE);
 				
 				final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
 				final BufferedOutputStream  out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
 				copy(in, out);
 				out.flush();
 				
 				final byte[] data = dataStream.toByteArray();
 				bm = BitmapFactory.decodeByteArray(data, 0, data.length);
 			} catch (MalformedURLException e) {
 				Log.e(TAG, "getRemotePic Error!"+url,e);
 			} catch (IOException e) {
 				Log.e(TAG, "getRemotePic Error!"+url,e);
 			} catch (Exception e) {
 				Log.e(TAG, "getRemotePic Error!"+url,e);
 			}
 			if (bm ==null ) {
 				Log.e(TAG, "getRemotePic Error!"+url);
 			}else{
 				Log.d(TAG, "getRemotePic OK:" + url);
 			}
 			return bm;
 			
 			
 		}
 		*/
 		
 //	private Bitmap resizePic(Bitmap b){
 //		int orgWidth = b.getWidth();
 //		int orgHeight = b.getHeight();
 //		DisplayMetrics dm = new DisplayMetrics();
 //		getWindowManager().getDefaultDisplay().getMetrics(dm);
 //		int maxHeight = dm.heightPixels-100;
 //		Log.d(TAG, "maxHeight:"+maxHeight+" orgHeight:"+orgHeight);
 //		//double ds = (float)maxHeight/(float)orgHeight;
 //		float scale = ((float)maxHeight)/((float)orgHeight);
 //		float toWidth = (orgWidth*scale);
 //		float toHeight = (orgHeight*scale);
 //		Log.d(TAG, "scale:"+scale);
 //		Log.d(TAG, "toWidth:"+toWidth);
 //		Log.d(TAG, "toHeight:"+toHeight);
 //		
 //		Matrix matrix = new Matrix();
 //		matrix.postScale(scale, scale);
 //		Bitmap newBmp = Bitmap.createBitmap(b,0,0,orgWidth,orgHeight,matrix,true);
 //		Log.d(TAG, "newHeight:"+newBmp.getHeight());
 //		Log.d(TAG, "newWidth:"+newBmp.getWidth());
 //		return newBmp;
 //	}
 	
 	
     @Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		// 解决横竖屏切换导致重载的问题
 		super.onConfigurationChanged(newConfig);
 
 		if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){  
 		    //横向  
 			//bigImgView.setImageBitmap(resizePic(picToSave));
 		}else{  
 		    //竖向  
 			//bigImgView.setImageBitmap(resizePic(picToSave));
 		}  
 	}
 
 	public void onConnectFailed() {
 		
 	}
 
 	public void onReceiveAd() {
 		
 	}
 	
 }
