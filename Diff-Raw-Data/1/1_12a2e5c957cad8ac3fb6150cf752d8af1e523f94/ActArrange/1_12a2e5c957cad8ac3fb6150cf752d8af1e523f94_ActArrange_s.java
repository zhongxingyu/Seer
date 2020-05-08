 package jp.juggler.ImgurMush;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import jp.juggler.ImgurMush.data.ResizePreset;
 import jp.juggler.ImgurMush.helper.ImageTempDir;
 import jp.juggler.ImgurMush.helper.PreviewLoader;
 import jp.juggler.util.LogCategory;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.SeekBar;
import android.widget.Toast;
 
 public class ActArrange extends BaseActivity{
 	static final LogCategory log = new LogCategory("ActArrange");
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		initUI();
 		init_page();
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		init_page();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		avPreview.setShowing(true);
 
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		avPreview.setShowing(false);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		SharedPreferences.Editor e = pref().edit();
 		if( resize_preset == null ){
 			e.putInt(PrefKey.KEY_LAST_RESIZE_MODE,-1);
 		}else{
 			e.putInt(PrefKey.KEY_LAST_RESIZE_MODE,resize_preset.mode);
 			e.putInt(PrefKey.KEY_LAST_RESIZE_VALUE,resize_preset.value);
 		}
 		e.commit();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if( requestCode == REQUEST_RESIZE_PRESET && resultCode == RESULT_OK && data != null){
 			int mode = data.getIntExtra(PrefKey.EXTRA_RESIZE_PRESET_MODE,-1);
 			int value = data.getIntExtra(PrefKey.EXTRA_RESIZE_PRESET_VALUE,-1);
 			set_resize(mode,value);
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	ActArrange act = this;
 	Button btnSave;
 	jp.juggler.ImgurMush.ArrangeView avPreview; 
 	View btnRotateLeft;
 	View btnRotateRight;
 	SeekBar sbCropLeft;
 	SeekBar sbCropRight;
 	SeekBar sbCropTop;
 	SeekBar sbCropBottom;
 	Button btnResize;
 	static final int REQUEST_RESIZE_PRESET = 1;
 	void initUI(){
 		setContentView(R.layout.act_arrange);
 		
 		avPreview =(jp.juggler.ImgurMush.ArrangeView)findViewById(R.id.preview); // preview
 		btnRotateLeft =findViewById(R.id.btnRotateLeft); 
 		btnRotateRight =findViewById(R.id.btnRotateRight); 
 		sbCropLeft =(SeekBar)findViewById(R.id.sbCropLeft); 
 		sbCropRight =(SeekBar)findViewById(R.id.sbCropRight); 
 		sbCropTop =(SeekBar)findViewById(R.id.sbCropTop); 
 		sbCropBottom =(SeekBar)findViewById(R.id.sbCropBottom); 
 		btnResize =(Button)findViewById(R.id.btnResize); 
 		btnSave =(Button)findViewById(R.id.btnSave); 
 		
 		
 		findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				finish();
 			}
 		});
 		
 		btnRotateLeft.setOnClickListener(new View.OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				avPreview.setRotate(avPreview.getRotate()-1);
 				seekbar_busy = true;
 				int l = sbCropLeft.getProgress();
 				int r = sbCropRight.getProgress();
 				int t = sbCropTop.getProgress();
 				int b = sbCropBottom.getProgress();
 				sbCropTop.setProgress(r);
 				sbCropRight.setProgress(b);
 				sbCropBottom.setProgress(l);
 				sbCropLeft.setProgress(t);
 				seekbar_busy = false;
 				update_crop_preview();
 			}
 		});
 		btnRotateRight.setOnClickListener(new View.OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				avPreview.setRotate(avPreview.getRotate()+1);
 				seekbar_busy = true;
 				int l = sbCropLeft.getProgress();
 				int r = sbCropRight.getProgress();
 				int t = sbCropTop.getProgress();
 				int b = sbCropBottom.getProgress();
 				sbCropTop.setProgress(l);
 				sbCropRight.setProgress(t);
 				sbCropBottom.setProgress(r);
 				sbCropLeft.setProgress(b);
 				seekbar_busy = false;
 				update_crop_preview();
 			}
 		});
 		
 		sbCropLeft.setOnSeekBarChangeListener(seekbar_listener);
 		sbCropRight.setOnSeekBarChangeListener(seekbar_listener);
 		sbCropTop.setOnSeekBarChangeListener(seekbar_listener);
 		sbCropBottom.setOnSeekBarChangeListener(seekbar_listener);
 		
 
 		btnResize.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				startActivityForResult(new Intent(act,ActResizePreset.class),REQUEST_RESIZE_PRESET);
 				
 			}
 		});
 		
 		btnSave.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				save();
 			}
 		});
 	}
 	
 	
 	void init_page(){
 		setResult(RESULT_CANCELED);
 		//
 		SharedPreferences pref = pref();
 		int mode = pref.getInt(PrefKey.KEY_LAST_RESIZE_MODE,-1);
 		int value = pref.getInt(PrefKey.KEY_LAST_RESIZE_VALUE,-1);
 		set_resize(mode,value);
 		//
 		delay_init.run();
 	}
 	
 	int image_orig_w;
 	int image_orig_h;
 	String src_path;
 	
 	Runnable delay_init = new Runnable() {
 		@Override
 		public void run() {
 			act.ui_handler.removeCallbacks(delay_init);
 			
 			avPreview.setImageBitmap(null);
 			seekbar_busy = true;
 			sbCropLeft.setMax(65535);
 			sbCropRight.setMax(65535);
 			sbCropTop.setMax(65535);
 			sbCropBottom.setMax(65535);
 			sbCropLeft.setProgress(0);
 			sbCropRight.setProgress(0);
 			sbCropTop.setProgress(0);
 			sbCropBottom.setProgress(0);
 			seekbar_busy = false;
 			
 			btnSave.setEnabled(false);
 			btnRotateLeft.setEnabled(false);
 			btnRotateRight.setEnabled(false);
 			sbCropLeft.setEnabled(false);
 			sbCropRight.setEnabled(false);
 			sbCropTop.setEnabled(false);
 			sbCropBottom.setEnabled(false);
 			btnResize.setEnabled(false);
 			
 			int w = avPreview.getWidth();
 			int h = avPreview.getHeight();
 			if( w <1 || h<1 ){
 				act.ui_handler.postDelayed(delay_init,66);
 				return;
 			}
 			int preview_image_max_wh =(int)(0.5+ (w>h?w:h)*1.5f);
 			
 			Intent intent = getIntent();
 			src_path = intent.getStringExtra(PrefKey.EXTRA_SRC_PATH);
 			
 			PreviewLoader.load(act,src_path,false,preview_image_max_wh,preview_image_max_wh,new PreviewLoader.Callback() {
 				
 				@Override
 				public void onMeasure(int w, int h) {
 					image_orig_w = w;
 					image_orig_h = h;
 				}
 				
 				@Override
 				public void onLoad(Bitmap bitmap) {
 					btnSave.setEnabled(true);
 					btnRotateLeft.setEnabled(true);
 					btnRotateRight.setEnabled(true);
 					sbCropLeft.setEnabled(true);
 					sbCropRight.setEnabled(true);
 					sbCropTop.setEnabled(true);
 					sbCropBottom.setEnabled(true);
 					btnResize.setEnabled(true);
 					
 					//
 					avPreview.setRotate(0);
 					avPreview.setImageBitmap(bitmap);
 					update_crop_preview();
 				}
 			});
 		}
 	};
 	
 	static final float getCropFromSeekBar(SeekBar sb){
 		return sb.getProgress() /(float)sb.getMax();
 	}
 	
 	boolean seekbar_busy = false;
 	void update_crop_preview(){
 		if(!seekbar_busy){
 			float l = getCropFromSeekBar(sbCropLeft);
 			float r = getCropFromSeekBar(sbCropRight);
 			float t = getCropFromSeekBar(sbCropTop);
 			float b = getCropFromSeekBar(sbCropBottom);
 			avPreview.setCrop(l,r,t,b);
 			if( l+r >= 1.0f || t+b >= 1.0f ){
 				btnSave.setEnabled(false);
 			}else{
 				btnSave.setEnabled(true);
 			}
 		}
 	}
 	
 	SeekBar.OnSeekBarChangeListener seekbar_listener = new SeekBar.OnSeekBarChangeListener() {
 		
 		@Override
 		public void onStopTrackingTouch(SeekBar seekBar) {
 			update_crop_preview();
 			
 		}
 		@Override
 		public void onStartTrackingTouch(SeekBar seekBar) {
 			update_crop_preview();
 		}
 		
 		@Override
 		public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
 			update_crop_preview();
 		}
 	};
 	ResizePreset resize_preset;
 	
 	void set_resize(int mode,int value){
 		if( mode < 0 ){
 			resize_preset = null;
 			btnResize.setText(getString(R.string.resize_disabled));
 		}else{
 			resize_preset = new ResizePreset();
 			resize_preset.mode = mode;
 			resize_preset.value = value;
 			btnResize.setText(resize_preset.makeTitle(act));
 		}
 	}
 	
 	////////////////////////////////////////////////////////////////////
 	
 	int getJPEGQuality(){
 		int v = 85;
 		try{
 			v = Integer.parseInt( act.pref().getString(PrefKey.KEY_JPEG_QUALITY,null) ,10 );
 		}catch(Throwable ex){
 		}
 		return v<10?10:v>100?100:v;
 	}
 	
 	void save(){
 		final int rot_mode = avPreview.getRotate();
 		final float crop_l = getCropFromSeekBar(sbCropLeft);
 		final float crop_r = getCropFromSeekBar(sbCropRight);
 		final float crop_t = getCropFromSeekBar(sbCropTop);
 		final float crop_b = getCropFromSeekBar(sbCropBottom); 
 
 		final int quality = getJPEGQuality();
 
 		
 		final ProgressDialog progress_dialog = new ProgressDialog(act);
 		progress_dialog.setIndeterminate(true);
 		progress_dialog.setTitle(R.string.edit_progress_title);
 		progress_dialog.setCancelable(true);
 		act.dialog_manager.show_dialog(progress_dialog);
 
 		new Thread(){
 			
 			boolean isCancelled(){
 				if(! progress_dialog.isShowing() ) return true;
 				return false;
 			}
 			void abort(){
 				act.ui_handler.post(new Runnable() {
 					public void run() {
 						if(act.isFinishing()) return;
 						progress_dialog.dismiss();
 					}
 				});
 			}
 			
 			@Override
 			public void run() {
 				BitmapFactory.Options options = new BitmapFactory.Options();
 				options.inJustDecodeBounds = false;
 				
 				options.inPurgeable = true;
 				options.inTargetDensity = 0;
 				options.inDensity = 0;
 				options.inDither = true;
 				options.inScaled = false;
 				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
 
 				// 画像を読み込む
 	    		Bitmap bitmap_src = null;
 	    		Bitmap bitmap_dst = null;
 	    		for(int shift=1;;++shift){
 	    			try{
 			    		if( isCancelled() ) return;
 	    				// 入力ビットマップをロードする 
 		    			options.inSampleSize = shift;
 		    			bitmap_src = BitmapFactory.decodeFile(src_path, options);
 	    				if(bitmap_src == null ) throw new RuntimeException("bitmap decode failed.");
 	    				// ロードできたならサイズが分かるはず
 	    				int source_w = bitmap_src.getWidth();
 	    				int source_h = bitmap_src.getHeight();
 	    				if(source_w <1 || source_h<1) throw new RuntimeException("bitmap decode failed.");
 	    				if( isCancelled() ) return;
 	    				// 切り抜き範囲の計算
 			    		int source_crop_l;
 			    		int source_crop_r;
 			    		int source_crop_t;
 			    		int source_crop_b;
 			    		switch(rot_mode){
 			    		case 0: default:
 			    			source_crop_t = (int)(0.5f + source_h * crop_t);
 			    			source_crop_r = (int)(0.5f + source_w * crop_r);
 			    			source_crop_b = (int)(0.5f + source_h * crop_b);
 			    			source_crop_l = (int)(0.5f + source_w * crop_l);
 			    			break;
 			    		case 1:
 			    			source_crop_t = (int)(0.5f + source_h * crop_r);
 			    			source_crop_r = (int)(0.5f + source_w * crop_b);
 			    			source_crop_b = (int)(0.5f + source_h * crop_l);
 			    			source_crop_l = (int)(0.5f + source_w * crop_t);
 			    			break;
 			    		case 2:
 			    			source_crop_t = (int)(0.5f + source_h * crop_b);
 			    			source_crop_r = (int)(0.5f + source_w * crop_l);
 			    			source_crop_b = (int)(0.5f + source_h * crop_t);
 			    			source_crop_l = (int)(0.5f + source_w * crop_r);
 			    			break;
 			    		case 3:
 			    			source_crop_t = (int)(0.5f + source_h * crop_l);
 			    			source_crop_r = (int)(0.5f + source_w * crop_t);
 			    			source_crop_b = (int)(0.5f + source_h * crop_r);
 			    			source_crop_l = (int)(0.5f + source_w * crop_b);
 			    			break;
 			    		}
 			    		// 切り抜き量を正規化
 			    		if( source_crop_l >= source_w ) source_crop_l = source_w -1;
 			    		if( source_crop_l < 0 ) source_crop_l = 0;
 			    		if( source_crop_r >= source_w - source_crop_l) source_crop_r = source_w - source_crop_l - 1;
 			    		//
 			    		if( source_crop_t >= source_h ) source_crop_t = source_h -1;
 			    		if( source_crop_t < 0 ) source_crop_t = 0;
 			    		if( source_crop_b >= source_h - source_crop_t) source_crop_b = source_h - source_crop_t - 1;
 			    		// 
 			    		int cropped_w = source_w - source_crop_l - source_crop_r;
 			    		int cropped_h = source_h - source_crop_t - source_crop_b;
 			    		if(cropped_w < 1) cropped_w =1;
 			    		if(cropped_h < 1) cropped_h =1;
 			    		
 			    		log.d("crop left=%s,right=%s,width=%s,src=%s",source_crop_l,source_crop_r,cropped_w,source_w);
 			    		log.d("crop top=%s,bottom=%s,height=%s,src=%s",source_crop_t,source_crop_b,cropped_h,source_h);
 			    		
 			    		// リサイズを計算する
 			    		int resized_w = cropped_w;
 			    		int resized_h = cropped_h;
 			    		float scale = 1.0f;
 			    		if(resize_preset != null ){
 			    			switch(resize_preset.mode){
 			    			case 0: default:
 			    				{
 			    					scale = resize_preset.value/(float)100;
 			    					resized_w = (int)(0.5d + resized_w * scale);
 			    					resized_h = (int)(0.5d + resized_h * scale);
 			    				}
 			    				break;
 			    			case 1:
 			    				{
 			    					if( cropped_w >= cropped_h ){
 			    						if( cropped_w > resize_preset.value ){
 			    							scale = resize_preset.value / (float)cropped_w;
 			    							resized_w = resize_preset.value;
 			    							resized_h = (int)(0.5d + resize_preset.value * (cropped_h / (float)cropped_w) );
 			    						}
 			    					}else{
 			    						if( cropped_h > resize_preset.value ){
 			    							scale = resize_preset.value / (float)cropped_h;
 			    							resized_w = (int)(0.5d + resize_preset.value * (cropped_w / (float)cropped_h) );
 			    							resized_h = resize_preset.value;
 			    						}
 			    					}
 			    				}
 			    				break;
 			    			case 2:
 			    				{
 			    					if( cropped_w <= cropped_h ){
 			    						if( cropped_w > resize_preset.value ){
 			    							scale = resize_preset.value / (float)cropped_w;
 			    							resized_w = resize_preset.value;
 			    							resized_h = (int)(0.5d + resize_preset.value * (cropped_h / (float)cropped_w) );
 			    						}
 			    					}else{
 			    						if( cropped_h > resize_preset.value ){
 			    							scale = resize_preset.value / (float)cropped_h;
 			    							resized_w = (int)(0.5d + resize_preset.value * (cropped_w / (float)cropped_h) );
 			    							resized_h = resize_preset.value;
 			    						}
 			    					}
 			    				}
 			    				break;
 			    			}
 			    		}
 			    		// 回転後の出力サイズ
 			    		if( (rot_mode &1) != 0 ){
 			    			int tmp = resized_w; resized_w = resized_h;resized_h = tmp; 
 			    		}
 			    		// 出力ビットマップを生成する
 			    		bitmap_dst = Bitmap.createBitmap(resized_w,resized_h,Bitmap.Config.ARGB_8888);
 			    		if( bitmap_dst == null ) throw new RuntimeException("bitmap generate failed.");
 			    		int dst_w = bitmap_dst.getWidth();
 			    		int dst_h = bitmap_dst.getHeight();
 			    		if( dst_w < 1 || dst_h < 1) throw new RuntimeException("bitmap generate failed.");
 			    		// 生成できたので画像を転送する
 			    		Canvas canvas = new Canvas(bitmap_dst);
 			    		Paint paint = new Paint();
 			    		paint.setFilterBitmap(true);
 			    		Matrix m = new Matrix();
 			        			
 			        	switch(rot_mode){
 			        	case 0:
 			        		m.postTranslate( -source_crop_l,-source_crop_t);
 			        		break;
 			        	case 1:
 			        		m.postRotate(rot_mode * 90);
 			        		m.postTranslate( source_h,0);
 			        		m.postTranslate( -source_crop_b,-source_crop_l);
 			        		break;
 			        	case 2:
 			        		m.postRotate(rot_mode * 90);
 			        		m.postTranslate( source_w,source_h);
 			        		m.postTranslate( -source_crop_r,-source_crop_b);
 			        		break;
 			        	case 3:
 			        		m.postRotate(rot_mode * 90);
 			        		m.postTranslate( 0,source_w);
 			        		m.postTranslate( -source_crop_t,-source_crop_r);
 			        		break;
 			        	}
 			    		m.postScale(scale,scale);
 			    		
 			    		float[] values = new float[9];
 			    		m.getValues(values);
 			    		log.d("matrix %s,%s,%s",values[0],values[1],values[2]);
 			    		log.d("matrix %s,%s,%s",values[3+0],values[3+1],values[3+2]);
 			    		log.d("matrix %s,%s,%s",values[6+0],values[6+1],values[6+2]);
 			    		
 			    		canvas.drawBitmap(bitmap_src,m,paint);
 			    		try{
 			    			final File dst_path = ImageTempDir.makeTempFile(act);
 			    			if(dst_path==null){
 			    				abort();
 			    				return;
 			    			}
 			    			FileOutputStream fos = new FileOutputStream(dst_path);
 			    			try{
 			    				bitmap_dst.compress(Bitmap.CompressFormat.JPEG,quality,fos);
 			    			}finally{
 			    				fos.close();
 			    			}
 			    			act.ui_handler.post(new Runnable() {
 								@Override
 								public void run() {
 									if(isFinishing()) return;
 									progress_dialog.dismiss();
 									Intent intent = new Intent();
 									intent.putExtra(PrefKey.EXTRA_DST_PATH,dst_path.getAbsolutePath());
 									setResult(RESULT_OK,intent);
 									finish();
 								}
 							});
 			    			return;
 			    		}catch(IOException ex){
 			    			act.report_ex(ex);
 			    			abort();
 			    			return;
 			    		}
 			    		
 	    			}catch(Throwable ex){
 	    				ex.printStackTrace();
 	    				continue;
 	    			}finally{
 	    				if( bitmap_src != null ){
 	    					bitmap_src.recycle();
 	    					bitmap_src = null;
 	    				}
 	    				if( bitmap_dst != null ){
 	    					bitmap_dst.recycle();
 	    					bitmap_dst = null;
 	    				}
 	    			}
 	    		}
 			}
 		}.start();
 	}
 	
 }
