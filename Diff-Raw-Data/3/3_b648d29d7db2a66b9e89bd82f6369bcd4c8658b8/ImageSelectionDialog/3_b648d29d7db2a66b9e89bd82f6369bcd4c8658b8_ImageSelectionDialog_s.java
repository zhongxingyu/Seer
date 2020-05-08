 package com.baixing.widget;
 
 import java.io.File;
 import java.io.Serializable;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnKeyListener;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.MediaStore;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.baixing.broadcast.CommonIntentAction;
 import com.baixing.imageCache.SimpleImageLoader;
 import com.baixing.util.Communication;
 import com.baixing.util.Util;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 
 public class ImageSelectionDialog extends DialogFragment implements OnClickListener{
 //	public static final String KEY_BITMAP_URL = "bitmapurl";
 //	public static final String KEY_CACHED_BPS = "cachedbps";
 	private static final String KEY_CURRENT_IMGVIEW = "currentImageView";
 //	private static final String KEY_OUT_HANDLER = "outHandler";
 	public static final String KEY_THUMBNAIL_URL = "thumbnailurl";
 	private static final String KEY_BUNDLE = "key_bundle";
 	public static final String KEY_IMG_CONTAINER = "image_container";
 	public static final String KEY_HANDLER = "handler";
 	private static final int NONE = 0;
 	private static final int MSG_START_UPLOAD = 5;
 	private static final int MSG_FAIL_UPLOAD = 6;
 	private static final int MSG_SUCCEED_UPLOAD = 7;
 	public static final int MSG_IMG_SEL_DISMISSED = 0x11110001;
 	private static final int PHOTORESOULT = 3;
 	
 	private int uploadCount = 0;
     private int currentImgView = -1;
 //    private ArrayList<String> bitmap_url;
 //    private ArrayList<String> thumbnail_url;
     private int imgHeight = 0;
     private List<ImageView> imgs;
 //    private ArrayList<WeakReference<Bitmap> > cachedBps;
 //    private ArrayList<Bitmap> cachedBps;
     private Bundle bundle;
     private Handler outHandler;
     private boolean pickDlgShown = false;
 
 	public static enum ImageStatus{
 		ImageStatus_Normal,
 		ImageStatus_Unset,
 		ImageStatus_Failed,
 		ImageStatus_Uploading
 	}
 	
 	static final private int[] imgIds = {R.id.iv_1, R.id.iv_2, R.id.iv_3, R.id.iv_4, R.id.iv_5, R.id.iv_6};
 	
 	public static class ImageContainer implements Serializable{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 8249910731524630367L;
 		public ImageStatus status = ImageStatus.ImageStatus_Unset;
 		public String bitmapUrl = "";
 		public String thumbnailPath = "";
 		public String bitmapPath = "";
 		public void reset(){
 			status = ImageStatus.ImageStatus_Unset;
 			bitmapUrl = "";
 			thumbnailPath = "";
 			bitmapPath = "";			
 		}
 		public void set(ImageContainer rhs){
 			status = rhs.status;
 			bitmapUrl = rhs.bitmapUrl;
 			bitmapPath = rhs.bitmapPath;
 			thumbnailPath = rhs.thumbnailPath;
 		}
 	};
 	    
 	private ImageContainer[] imgContainer = 
 			new ImageContainer[]{new ImageContainer(),new ImageContainer(),new ImageContainer(),new ImageContainer(),new ImageContainer(),new ImageContainer()}; 
 	
 	
     @SuppressWarnings("unchecked")
 	public ImageSelectionDialog(Bundle bundle){
     	this.setMsgOutBundle(bundle); 	
     }
     
     public ImageSelectionDialog(){
     	
     }
     
     public void clearResource(){
     	for(int i = 0; i < imgContainer.length; ++ i){
     		imgContainer[i].reset();
     	}
     }
     
     public void setMsgOutHandler(Handler handler){
     	outHandler = handler;
     }
     
     public void setMsgOutBundle(Bundle bundle){
     	this.bundle = bundle;
     	if(bundle != null){
     		Object[] container = (Object[])bundle.getSerializable(KEY_IMG_CONTAINER);
     		if(container != null){
     			for(int i = 0; i < imgContainer.length && i < container.length; ++ i){
     				imgContainer[i].set((ImageContainer)container[i]);
     			}
     		}
     	}       	
     }
     
     private void adjustImageCountAfterNew(View v){
     	if(this.currentImgView >= imgIds.length - 1) return;
     	if(currentImgView < 0) return;
     	if(imgs.size() <= currentImgView + 1){
     		imgs.add((ImageView)v.findViewById(imgIds[currentImgView + 1]));
     		v.findViewById(imgIds[currentImgView + 1]).setVisibility(View.VISIBLE);
     	}
 		imgs.get(0).setVisibility(View.VISIBLE);
 		imgs.get(0).getRootView().findViewById(R.id.btn_finish_sel).setVisibility(View.VISIBLE);
     	adjustImageLines();
     }
     
     private void removeImageContainer(int index){
     	if(index < 0 || index >= imgContainer.length) return;
     	for(int i = index; i < imgContainer.length - 1; ++ i){
     		imgContainer[i].set(imgContainer[i + 1]);
     	}
     	imgContainer[imgContainer.length - 1].reset();
     }
     
     static public Bitmap getThumbnailWithPath(String path){
     	if(path == null || path.length() <= 0) return null;
     	WeakReference<Bitmap> thumbnail = QuanleimuApplication.getImageLoader().getWithImmediateIO(path);
     	if(thumbnail == null) return null;
     	return thumbnail.get();
     }
     
     private void adjustImageCountAfterDel(View v){
     	removeImageContainer(currentImgView);
 //		bitmap_url.remove(currentImgView);
 //		cachedBps.remove(currentImgView);
     	int i = currentImgView;
     	for(; i < imgs.size(); ++ i){
     		if(imgContainer[i].status == ImageStatus.ImageStatus_Unset){
     			break;
     		}
     		if(imgContainer[i].thumbnailPath != null && imgContainer[i].thumbnailPath.length() > 0){
     			imgs.get(i).setImageBitmap(getThumbnailWithPath(imgContainer[i].thumbnailPath));
     		}
     	}
     	imgs.get(i).setImageResource(R.drawable.btn_add_picture);
     	imgs.get(i).setClickable(true);
     	for(int j = i + 1; j < imgs.size(); ++ j){
     		imgs.get(j).setVisibility(View.INVISIBLE);
     	}
     	if(i + 1 < imgs.size()){
     		imgs = imgs.subList(0, i + 1);
     	}
 		imgs.get(0).setVisibility(View.VISIBLE);
 		imgs.get(0).getRootView().findViewById(R.id.btn_finish_sel).setVisibility(View.VISIBLE);
     	adjustImageLines();
     }    
     
     private Uri uri = null;
     
 	private void getBitmap(Uri uri, int id) {
 		String path = uri == null ? "" : uri.toString();
 		if (uri != null) {
 			if (imgs != null){
 				imgs.get(currentImgView).setFocusable(true);
 			}
 //			imgContainer[currentImgView].status = ImageStatus.ImageStatus_Uploading;
 //			imgContainer[currentImgView].bitmapPath = path;
 			new Thread(new UpLoadThread(path, currentImgView)).start();
 		}
 	}
     
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d("index", "bitmap, onActivityResult");
 		pickDlgShown = false;
 		
 		if (resultCode == NONE) {
 			return;
 		}
 		imgs.get(0).setVisibility(View.VISIBLE);
 		imgs.get(0).getRootView().findViewById(R.id.btn_finish_sel).setVisibility(View.VISIBLE);
 //		this.showPost();
 		
 		// 拍照 
 		if (requestCode == CommonIntentAction.PhotoReqCode.PHOTOHRAPH) {
 			// 设置文件保存路径这里放在跟目录下
 			File picture = new File(Environment.getExternalStorageDirectory(), "temp" + this.currentImgView + ".jpg");
 			uri = Uri.fromFile(picture);
 			getBitmap(uri, requestCode); // 直接返回图片
 			//startPhotoZoom(uri); //截取图片尺寸
 		}
 
 		if (data == null) {
 			return;
 		}
 
 		// 读取相册缩放图片
 		if (requestCode == CommonIntentAction.PhotoReqCode.PHOTOZOOM) {
 			uri = data.getData();
 			//startPhotoZoom(uri);
 			getBitmap(uri, requestCode);
 		}
 		// 处理结果
 		if (requestCode == PHOTORESOULT) {
 			File picture = new File("/sdcard/cropped.jpg");
 			
 			uri = Uri.fromFile(picture);
 			getBitmap(uri, CommonIntentAction.PhotoReqCode.PHOTOHRAPH);
 			File file = new File(Environment.getExternalStorageDirectory(), "temp" + this.currentImgView + "jpg");
 			try {
 				if(file.isFile() && file.exists()){
 					file.delete();
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@Override
 	public void onResume(){
 		if(pickDlgShown){
 			if(!imgs.get(0).isShown()){
 				this.dismiss();
 			}			
 		}
 		pickDlgShown = false;
 		super.onResume();
 	}
 	
 	@Override
 	public void onDismiss(DialogInterface dialog){
 		if(outHandler != null){
 			if(this.bundle != null){
 //				bundle.putSerializable(KEY_BITMAP_URL, bitmap_url);
 //				bundle.putSerializable(KEY_CACHED_BPS, (cachedBps));
 				bundle.putSerializable(KEY_IMG_CONTAINER, imgContainer);
 			}
 			outHandler.sendEmptyMessage(MSG_IMG_SEL_DISMISSED);
 		}
 //		popUpDlgFragment();
 		super.onDismiss(dialog);
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		
 		if(savedInstanceState != null){
 //    		bitmap_url = (ArrayList<String>)savedInstanceState.getSerializable(KEY_BITMAP_URL);
 //    		cachedBps = (ArrayList<Bitmap>)savedInstanceState.getSerializable(KEY_CACHED_BPS);
 			currentImgView = savedInstanceState.getInt(KEY_CURRENT_IMGVIEW);
     		Log.d("index", "bitmap:   onCreate: " + String.valueOf(currentImgView));
     		bundle = savedInstanceState.getBundle(KEY_BUNDLE);
     		Object[] container = (Object[])savedInstanceState.getSerializable(KEY_IMG_CONTAINER);
     		if(container != null){
     			for(int i = 0; i < imgContainer.length && i < container.length; ++ i){
     				imgContainer[i].set((ImageContainer)container[i]);
     			}
     		}
     		 
 		}
 		
 	}
 	
 	@Override
 	public void onStart(){
 		super.onStart();
 		FragmentActivity activity = getActivity();
 		if(activity != null){
 			FragmentManager fm = activity.getSupportFragmentManager();
 			if(fm != null){
 				fm.putFragment(this.bundle, "imageFragment", this);
 			}
 		}
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState){
 		super.onSaveInstanceState(outState);
 		if(outState != null){
 //			outState.putSerializable(KEY_BITMAP_URL, bitmap_url);
 //			outState.putSerializable(KEY_CACHED_BPS, cachedBps);
 			outState.putInt(KEY_CURRENT_IMGVIEW, currentImgView);
 			Log.d("index", "bitmap:   onsave: " + String.valueOf(currentImgView));
 			outState.putBundle(KEY_BUNDLE, bundle);
 			outState.putSerializable(KEY_IMG_CONTAINER, imgContainer);
 		}
 	}
 
 	public boolean handleBack(){
 		if(this.getDialog() != null && this.getDialog().isShowing()){
 			if(this.getView() != null){
 				View v = getView().findViewById(R.id.post_big);
 				if(v != null && v.isShown()){
 					v.setVisibility(View.INVISIBLE);
 					imgs.get(0).getRootView().findViewById(R.id.btn_finish_sel).setVisibility(View.VISIBLE);
 					return true;
 				}
 			}
 			getDialog().dismiss();
 			return true;
 		}
 		return false;
 	}
 	
 	private void adjustImageLines(){
 		if(imgs == null) return;
 		if(imgs.size() <= imgIds.length / 2){
 			for(int i = imgIds.length / 2; i < imgIds.length; ++ i){
 				imgs.get(0).getRootView().findViewById(imgIds[i]).setVisibility(View.GONE);
 			}
 			LinearLayout ll = (LinearLayout)imgs.get(0).getRootView().findViewById(imgIds[imgIds.length / 2]).getParent();
 			ll.setVisibility(View.GONE);
 		}else{
 			LinearLayout ll = (LinearLayout)imgs.get(0).getRootView().findViewById(imgIds[imgIds.length / 2]).getParent();
 			ll.setVisibility(View.VISIBLE);
 
 			for(int i = imgs.size(); i < imgIds.length; ++ i){
 				imgs.get(0).getRootView().findViewById(imgIds[i]).setVisibility(View.INVISIBLE);
 			}			
 		}
 	}
 	
 	private void setImageViews(View parent, boolean reUploadBitmap){
 		Log.d("index", "bitmap, setImageViews");
 		imgs = new ArrayList<ImageView>();
 		if(imgContainer.length == 0){
 	        ImageView iv = (ImageView)parent.findViewById(imgIds[0]);
 	    	iv.setVisibility(View.VISIBLE);
 	    	imgs.add(iv);
 		}else{
 			for(int i = 0; i < imgContainer.length; ++ i){
 				if(imgContainer[i].status == ImageStatus.ImageStatus_Unset){
 					ImageView iv = (ImageView)parent.findViewById(imgIds[i]);
 					iv.setVisibility(View.VISIBLE);
 					imgs.add(iv);
 					break;
 				}else if(imgContainer[i].status == ImageStatus.ImageStatus_Normal){
 					ImageView iv = (ImageView)parent.findViewById(imgIds[i]);
 					iv.setVisibility(View.VISIBLE);
 					if(imgContainer[i].thumbnailPath != null){
 						if(imgContainer[i].thumbnailPath.contains("http://")){
 							SimpleImageLoader.showImg(iv, imgContainer[i].thumbnailPath, null, getActivity());
 						}else{
 							Bitmap bmp = getThumbnailWithPath(imgContainer[i].thumbnailPath);
 							iv.setImageBitmap(bmp);
 						}
 					}
 					imgs.add(iv);
 				}else if(imgContainer[i].status == ImageStatus.ImageStatus_Uploading){
 					ImageView iv = (ImageView)parent.findViewById(imgIds[i]);
 					iv.setImageResource(R.drawable.icon_post_loading);
 					iv.setVisibility(View.VISIBLE);
 					if(reUploadBitmap && imgContainer[i].bitmapPath != null && imgContainer[i].bitmapPath.length() > 0){
 						new Thread(new UpLoadThread(imgContainer[i].bitmapPath, i)).start();
 					}
 					imgs.add(iv);
 				}else if(imgContainer[i].status == ImageStatus.ImageStatus_Failed){
 					ImageView iv = (ImageView)parent.findViewById(imgIds[i]);
 					iv.setImageResource(R.drawable.f);
 					iv.setVisibility(View.VISIBLE);
 					imgs.add(iv);
 				}				
 			}
 		}
 	}
 	
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
 //   		imgs = new ArrayList<ImageView>(imgIds.length);
 		Log.d("index", "bitmap, onCreateDialog");
 		uploadCount = 0;
         
         Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
         LayoutInflater inflater = getActivity().getLayoutInflater();
         View v = inflater.inflate(R.layout.dialog_image_selection, null);
         dialog.setContentView(v);
         for(int i = 0; i < imgIds.length; ++ i){
         	v.findViewById(imgIds[i]).setOnClickListener(this);
         }
         v.findViewById(R.id.btn_finish_sel).setOnClickListener(this);
         Log.d("index", "index, before setimageview, container count is:   " + imgContainer.length);
         
         for(int i = 0; i < imgContainer.length; ++ i){
         	Log.d("bitmap", "bitmap container content:  " + imgContainer[i].status + "index:  " + i);
         }
         
         setImageViews(v, true);
         Log.d("index", "index, after setimageview, count is:   " + imgs.size());
         
         adjustImageLines();
         
         v.setOnClickListener(this);
         dialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
 
 			@Override
 			public boolean onKey(DialogInterface dialog, int keyCode,
 					KeyEvent event) {
 				// TODO Auto-generated method stub
 				if(imgs.get(0).getRootView() != null){
 					View vv = imgs.get(0).getRootView().findViewById(R.id.post_big);
 					if(vv != null && vv.isShown()){
 						if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
 							vv.setVisibility(View.GONE);
 							imgs.get(0).getRootView().findViewById(R.id.img_sel_content).setVisibility(View.VISIBLE);								
 							imgs.get(0).getRootView().findViewById(R.id.btn_finish_sel).setVisibility(View.VISIBLE);
 							return true;
 						}
 					}
 				}
 				return false;
 			}
 			
 		});
         v.findViewById(R.id.post_big).setVisibility(View.GONE);
         
         if(imgContainer[0].status == ImageStatus.ImageStatus_Unset && savedInstanceState == null){
         	imgs.get(0).post(new Runnable(){
         		@Override
         		public void run(){
         			imgs.get(0).performClick();
         		}
         	});
 
         	imgs.get(0).getRootView().findViewById(R.id.btn_finish_sel).setVisibility(View.INVISIBLE);
         	imgs.get(0).setVisibility(View.INVISIBLE);
         }
 
         return dialog;
     }
         
 	private ImageStatus getCurrentImageStatus(int index){
 		if(index < 0 || index >= imgContainer.length) return ImageStatus.ImageStatus_Unset;
 //		if(imgContainer.length <= index || imgContainer.get(index) == null)return ImageStatus.ImageStatus_Unset;
 		return imgContainer[index].status;
 //		if(bitmap_url.get(index).contains("http:")) return ImageStatus.ImageStatus_Normal; 
 //		return ImageStatus.ImageStatus_Failed;
 	}
 	
 	private void pickupPhoto(final int tmpFileIndex){
 		final String[] names = {"拍照","相册"};
 		Activity act = getActivity();
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("请选择")//.setMessage("无法确定当前位置")
 		.setItems(names, new DialogInterface.OnClickListener(){
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which){
 				Intent backIntent = new Intent();
 				backIntent.setClass(getActivity(), getActivity().getClass());
 				
 				Intent goIntent = new Intent();
 				goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_INTENT, backIntent);
 				switch(which){
 					case 0:
 						goIntent.setAction(CommonIntentAction.ACTION_IMAGE_CAPTURE);
 						goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_REQUST_CODE, CommonIntentAction.PhotoReqCode.PHOTOHRAPH);
 						goIntent.putExtra(CommonIntentAction.EXTRA_IMAGE_SAEV_PATH, "temp" + tmpFileIndex + ".jpg");
 						getActivity().startActivity(goIntent);
 //						Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 //						intent2.putExtra(MediaStore.EXTRA_OUTPUT,
 //								Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp" + tmpFileIndex + ".jpg")));
 //						context.startActivityForResult(intent2, CommonIntentAction.PhotoReqCode.PHOTOHRAPH);
 						break;						
 					case 1:
 						goIntent.setAction(CommonIntentAction.ACTION_IMAGE_SELECT);
 						goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_REQUST_CODE, CommonIntentAction.PhotoReqCode.PHOTOZOOM);
 						getActivity().startActivity(goIntent);
 //						Intent intent3 = new Intent(Intent.ACTION_GET_CONTENT);
 //						intent3.addCategory(Intent.CATEGORY_OPENABLE);
 //						intent3.setType("image/*");
 //						context.startActivityForResult(Intent.createChooser(intent3, "选择图片"), CommonIntentAction.PhotoReqCode.PHOTOZOOM);
 						break;
 
 				}				
 			}
 		})
 		.setNegativeButton("取消", new DialogInterface.OnClickListener(){
 			@Override
 			public void onClick(DialogInterface dialog, int which){
 				dialog.dismiss();
 				if(!imgs.get(0).isShown()){
 					ImageSelectionDialog.this.dismiss();
 				}		
 				pickDlgShown = false;
 			}
 		});//.show();
 		AlertDialog dlg = builder.create();
 		dlg.setOnKeyListener(new OnKeyListener(){
 
 			@Override
 			public boolean onKey(DialogInterface dialog, int keyCode,
 					KeyEvent event) {
 				// TODO Auto-generated method stub
 				if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
 					dialog.dismiss();
 					pickDlgShown = false;
 					if(imgs.size() <= 1){
 						ImageSelectionDialog.this.dismiss();						
 					}
 					return true;
 				}
 				return false;
 			}
 			
 		});
 		dlg.show();
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		for (int i = 0; i < imgIds.length; i++) {
 			if (imgIds[i] == v.getId()) {
 				currentImgView = i;
 				ImageStatus status = getCurrentImageStatus(i);
 				if(ImageStatus.ImageStatus_Unset == status){
 //					showDialog();
 					pickupPhoto(this.currentImgView);
 					pickDlgShown = true;
 				}
 				else if(ImageStatus.ImageStatus_Failed == status){
 					String[] items = {"重试", "换一张"};
 					new AlertDialog.Builder(getActivity())
 					.setTitle("选择操作")
 					.setItems(items, new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							if(0 == which){
 								new Thread(new UpLoadThread(imgContainer[currentImgView].bitmapPath, currentImgView)).start();
 							}
 							else{
 //								if (cachedBps.size() > currentImgView && cachedBps.get(currentImgView) != null){
 //									if(cachedBps.get(currentImgView) != null && cachedBps.get(currentImgView) != null){
 //										cachedBps.get(currentImgView).recycle();
 //									}
 //									cachedBps.set(currentImgView, null);
 //								}
 //								setListContent(bitmap_url, null, currentImgView);
 //								bitmap_url.set(currentImgView, null);
 								imgs.get(currentImgView).setImageResource(R.drawable.btn_add_picture);
 //								showDialog();
 								pickupPhoto(currentImgView);
 								//((BXDecorateImageView)imgs[currentImgView]).setDecorateResource(-1, BXDecorateImageView.ImagePos.ImagePos_LeftTop);
 							}
 							
 						}
 					})
 					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 					}).show();
 				}
 				else{
 					imgs.get(0).getRootView().findViewById(R.id.img_sel_content).setVisibility(View.GONE);
 					imgs.get(0).getRootView().findViewById(R.id.btn_finish_sel).setVisibility(View.GONE);
 					imgs.get(0).getRootView().findViewById(R.id.post_big).setVisibility(View.VISIBLE);
 					((ImageView)imgs.get(0).getRootView().findViewById(R.id.iv_post_big_img)).setImageResource(R.drawable.loading_210_black);
 					SimpleImageLoader.showImg(imgs.get(0).getRootView().findViewById(R.id.iv_post_big_img), 
 //							bitmap_url.get(currentImgView),
 							imgContainer[currentImgView].bitmapUrl,
 							"",
 							getActivity());
 					imgs.get(0).getRootView().findViewById(R.id.post_big).setOnClickListener(new OnClickListener(){
 						@Override
 						public void onClick(View v){
 							imgs.get(0).getRootView().findViewById(R.id.post_big).setVisibility(View.GONE);
 							imgs.get(0).getRootView().findViewById(R.id.img_sel_content).setVisibility(View.VISIBLE);
 							imgs.get(0).getRootView().findViewById(R.id.btn_finish_sel).setVisibility(View.VISIBLE);
 						}
 					});
 					imgs.get(0).getRootView().findViewById(R.id.btn_post_del_img).setOnClickListener(new OnClickListener(){
 						@Override
 						public void onClick(View v){
 							imgs.get(0).getRootView().findViewById(R.id.post_big).setVisibility(View.GONE);
 							imgs.get(0).getRootView().findViewById(R.id.img_sel_content).setVisibility(View.VISIBLE);
 							adjustImageCountAfterDel(imgs.get(0).getRootView());
 						}
 					});
 //					new AlertDialog.Builder(this.getActivity())
 //					.setMessage("删除当前图片?")
 //					.setPositiveButton("删除", new DialogInterface.OnClickListener(){
 //						
 //						@Override
 //						public void onClick(DialogInterface dialog, int which) {
 //							bitmap_url.set(currentImgView, null);
 //							imgs.get(currentImgView).setImageResource(R.drawable.btn_add_picture);
 //							cachedBps.set(currentImgView, null);
 //						}
 //					})
 //					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 //						
 //						@Override
 //						public void onClick(DialogInterface dialog, int which) {
 //							dialog.dismiss();
 //						}
 //					}).show();
 				}
 				return;
 			}
 		}
 		if(v.getId() == R.id.btn_finish_sel){
 			this.dismiss();	
 		}		
 	}
 	
 	
 	private String getRealPathFromURI(Uri contentUri) {
 		if(getActivity() == null) return null;
 		String[] proj = { MediaStore.Images.Media.DATA };
 		Cursor cursor = getActivity().managedQuery(contentUri, proj, null, null, null);
 
 		if (cursor == null)
 			return null;
 
 		int column_index = cursor
 				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 
 		cursor.moveToFirst();
 
 		String ret = cursor.getString(column_index);
 //		cursor.close();
 		return ret;
 	}
 	
 	Handler handler = new Handler(){
 		@Override
 		public void handleMessage(Message msg) {
 			Activity activity = getActivity();
 //			View rootView = getView();
 			if (activity == null) {
 				return;
 			}
 			switch(msg.what){
 			case MSG_START_UPLOAD:{
 				Log.d("bitmap", "bitmap MSG_START_UPLOAD got");
 				Integer index = (Integer) msg.obj;
 				if (imgs != null){
 					imgs.get(index.intValue()).setImageResource(R.drawable.icon_post_loading);
 					imgs.get(index).setClickable(false);
 					imgs.get(index.intValue()).invalidate();
 					
 					adjustImageCountAfterNew(imgs.get(0).getRootView());
 				}
 				break;		
 			}
 			case MSG_FAIL_UPLOAD:{
 				Log.d("bitmap", "bitmap MSG_FAIL_UPLOAD got");
 				if (imgs != null){			
 					Integer index = (Integer) msg.obj;
 					BitmapDrawable bd = (BitmapDrawable)getResources().getDrawable(R.drawable.f);
 					imgs.get(index.intValue()).setImageBitmap(bd.getBitmap());
 //					imgs.get(index.intValue()).setImageResource(R.drawable.f);
 					imgs.get(index).setClickable(true);
 					imgs.get(index.intValue()).invalidate();	
 					Log.d("bitmap", "bitmap MSG_FAIL_UPLOAD got, and invalidate");
 				}
 				break;			
 			}
 			case MSG_SUCCEED_UPLOAD:{
 				
 				Integer index = (Integer)msg.obj;
 				
 				if (imgs != null){
 //					imgs.get(index).setImageBitmap(cachedBps.get(index));
 					Bitmap bmp = getThumbnailWithPath(imgContainer[index].thumbnailPath);
 					Log.d("bitmap", "bitmap MSG_SUCCEED_UPLOAD got, thumbnailpath:  " + imgContainer[index].thumbnailPath
 							+ "bitmap:   " + (bmp == null ? "" : bmp.toString()));
 					imgs.get(index).setImageBitmap(bmp);
 					imgs.get(index).setClickable(true);
 					imgs.get(index).invalidate();
 				}
 				break;
 			}
 			}
 		}
 		
 	};
 	
 	
 	private static int getClosestResampleSize(int cx, int cy, int maxDim){
         int max = Math.max(cx, cy);
         
         int resample = 1;
         for (resample = 1; resample < Integer.MAX_VALUE; resample++)
         {
             if (resample * maxDim > max)
             {
                 resample--;
                 break;
             }
         }
         
         if (resample > 0)
         {
             return resample;
         }
         return 1;
     }
 	
 	static private Bitmap createThumbnail(Bitmap srcBmp, int thumbHeight){
 		Float width  = new Float(srcBmp.getWidth());
 		Float height = new Float(srcBmp.getHeight());
 		Float ratio = width/height;
 //		Bitmap thumbnail = Bitmap.createScaledBitmap(srcBmp, (int)(thumbHeight*ratio), thumbHeight, true);
 		Bitmap thumbnail = Bitmap.createScaledBitmap(srcBmp, thumbHeight, thumbHeight, true);
 		return thumbnail;
 	}
 	
 	static <T> void setListContent(List<T> container, T obj, int index){
 		if(container == null) return;
 		if(container.size() > index){
 			container.set(index, obj);
 		}else{
 			container.add(obj);
 		}	
 	}
 	
 	class UploadMsg extends Object{
 		int index;
 		String bitmapUrl;
 		UploadMsg(int index, String url){
 			this.index = index;
 			bitmapUrl = url;
 		}
 	}
 	
 	private int adjustImgContainerAfterUpload(String bitmapPath, int index, String bitmapUrl, String thumbnailPath){
 		String url = imgContainer[index].bitmapPath;
 		int ret = -1;
 		if(url != null && url.length() > 0 && url.equals(bitmapPath) && imgContainer[index].status == ImageStatus.ImageStatus_Uploading){
 			ret = index;
 		}else{
 			for(int i = index; i >= 0; -- i){
 				url = imgContainer[i].bitmapPath;
 				if(url != null && url.equals(bitmapPath) && imgContainer[i].status == ImageStatus.ImageStatus_Uploading){
 					ret = i;
 					break;
 				}
 			}
 		}
 		if(ret >= 0){
 			imgContainer[ret].status = ImageStatus.ImageStatus_Normal;
 			imgContainer[ret].bitmapUrl = bitmapUrl;
 			imgContainer[ret].thumbnailPath = thumbnailPath;
 		}
 		return ret;
 	}
 	
 	class UpLoadThread implements Runnable {
 		private String bmpPath;
 		private int currentIndex = -1;
 
 		public UpLoadThread(String path, int index) {
 			super();
 			this.bmpPath = path;
 			currentIndex = index;
 		}
 
 		public void run() {
 
 			final Activity activity = getActivity();
 			if (activity == null)
 			{
 				return;
 			}
 
 			if(handler != null){
 				Message msg = Message.obtain();
 				msg.what = MSG_START_UPLOAD;
 				msg.obj = currentIndex;
 				handler.sendMessage(msg);
 			}
 			
 //			synchronized(ImageSelectionDialog.this){
 			++ uploadCount;
 			if(bmpPath == null || bmpPath.equals("")) return;
 
 			Uri uri = Uri.parse(bmpPath);
 			String path = getRealPathFromURI(uri); // from Gallery
 			if (path == null) {
 				path = uri.getPath(); // from File Manager
 			}
 			Bitmap currentBmp = null;
 			if (path != null) {
 				try {
 				       
 				    BitmapFactory.Options bfo = new BitmapFactory.Options();
 			        bfo.inJustDecodeBounds = true;
 			        BitmapFactory.decodeFile(path, bfo);
 			        
 				    BitmapFactory.Options o =  new BitmapFactory.Options();
 	                o.inPurgeable = true;
 	                
 	                int maxDim = 600;
 	                
 	                o.inSampleSize = getClosestResampleSize(bfo.outWidth, bfo.outHeight, maxDim);
 	                
 	                
 	                currentBmp = BitmapFactory.decodeFile(path, o);
 					//photo = Util.newBitmap(tphoto, 480, 480);
 					//tphoto.recycle();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}			
 									
 			if(currentBmp != null){
 				imgContainer[currentIndex].bitmapPath = path;
 				imgContainer[currentIndex].status = ImageStatus.ImageStatus_Uploading;
 				String result = Communication.uploadPicture(currentBmp);
 //				imgContainer[currentIndex].bitmapUrl = result;
 //				imgContainer[currentIndex].status = ImageStatus.ImageStatus_Normal;
 				-- uploadCount;
 				if(imgs != null && imgs.size() > 0 && imgs.get(0) != null && imgs.get(0).getHeight() != 0){
 					imgHeight = imgs.get(0).getHeight();
 				}
 				Bitmap thumbnailBmp = createThumbnail(currentBmp, imgHeight == 0 ? 90 : imgHeight);//imgs[currentIndex].getHeight());
 				String thumbnailPath = "";
 				if(thumbnailBmp != null){
 					QuanleimuApplication.getImageLoader().putImageToDisk("thumbnail_" + path, thumbnailBmp);
 					QuanleimuApplication.getImageLoader().putImageToCache("thumbnail_" + path, thumbnailBmp);
 //					imgContainer[currentIndex].thumbnailPath = "thumbnail_" + path;
 					thumbnailPath = "thumbnail_" + path;
 					Log.d("bitmpa", "bitmap thumbnail is NOT NULL:  " + thumbnailPath);
 				}else{
 					Log.d("bitmap", "bitmap  thumbnail is nullllllll");
 				}
 				currentIndex = adjustImgContainerAfterUpload(path, currentIndex, result, thumbnailPath);
 				if(currentIndex < 0) return;
 
 				currentBmp.recycle();
 				currentBmp = null;
 	
 				if (result != null) {
 //					setListContent(bitmap_url, result, currentIndex);						
 	
 					if(handler == null) {
 						Log.d("bitmap", "bitmap, handler is nullllll");
 						return;
 					}
 					Log.d("index", "bitmap, handler is NOTTTT nullllll  " + handler.toString());
 					Message msg = Message.obtain();
 					msg.what = MSG_SUCCEED_UPLOAD;
 					msg.obj = currentIndex;
 					handler.sendMessage(msg);
 	
 					activity.runOnUiThread(new Runnable(){
 						public void run(){
 							if(getDialog() != null && getDialog().isShowing()){
 								Toast.makeText(activity, "上传图片成功", 0).show();
 							}
 						}
 					});	     
 					return;
 				} else{
 					imgContainer[currentIndex].status = ImageStatus.ImageStatus_Failed;
 				}
 			}else{
 				Log.d("bitmap", "bitmap is nullllllll");
 				-- uploadCount;
 				imgContainer[currentIndex].status = ImageStatus.ImageStatus_Failed;
 //				setListContent(bitmap_url, bmpPath, currentIndex);
 			}
 			
 			if(handler == null) return;
 			Message msg = Message.obtain();
 			msg.what = MSG_FAIL_UPLOAD;
 			msg.obj = currentIndex;
 			handler.sendMessage(msg);
 			Log.d("bitmap", "bitmap send Mesage");
 
 //			activity.runOnUiThread(new Runnable(){
 //				public void run(){
 //					Toast.makeText(activity, "上传图片失败", 0).show();
 //				}
 //			});
 //			}
 		}
 	}
 	
 	class DownloadThumbnailsThread extends Thread{
 		private List<String> urls = new ArrayList<String>();
 		public DownloadThumbnailsThread(List<String> urls){
 			this.urls.addAll(urls);
 		}
 		@Override
 		public void run(){
 			if(urls.size() == 0) return;
 			synchronized(ImageSelectionDialog.this){
 //				int size = cachedBps.size() < urls.size() ? cachedBps.size() : urls.size();
 //				for(int i = 0; i < size; ++ i){
 //					if(cachedBps.get(i) == null){
 //						Bitmap bmp = Util.getImage(urls.get(i));
 //						setListContent(cachedBps, bmp, i);
 //					}
 //				}
 //				getActivity().runOnUiThread(new Runnable(){
 //					@Override
 //					public void run(){
 //						for(int i = 0; i < cachedBps.size(); ++ i){
 //							((ImageView)imgs.get(i)).setImageBitmap(cachedBps.get(i));
 //						}
 //					}
 //				});
 			}
 		}
 	}
 }
