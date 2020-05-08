 package com.adtworker.mail;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 import com.adtworker.mail.service.CallbackHandler;
 import com.adtworker.mail.service.Service;
 import com.adtworker.mail.service.entity.ImgInfo;
 import com.adtworker.mail.service.entity.ImgLoadSupporter;
 import com.adtworker.mail.view.ScrollViewListener;
 import com.adtworker.mail.view.SuperScrollView;
 
 public class WallPhotoActivity extends Activity {
 	private LinearLayout photoLayout1;
 	private LinearLayout photoLayout2;
 	private LinearLayout photoLayout3;
 	private CallbackHandler wallImageGetter;
 	public Service service;
 	protected long oldTimeInMillis = 0;
 	private SuperScrollView superScrollView;
 	private final ArrayList<ImageView> imageViewList = new ArrayList<ImageView>();
 	public static int index = 0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		service = Service.getInstance(this);
 		setContentView(R.layout.wall_photo);
 		addGroupProducts();
 		setScrollView();
 		initGetter();
 	}
 	private void initGetter() {
 		wallImageGetter = new CallbackHandler(
 				CallbackHandler.CallbackType.wallProduct) {
 
 			@Override
 			public void dispatchMessage(Message msg) {
 				super.dispatchMessage(msg);
 				addGroupProducts();
 				WallPhotoActivity.index = 0;
 			}
 		};
 	}
 
 	private void initPhotoLayouts() {
 		if (photoLayout1 == null) {
 			photoLayout1 = (LinearLayout) findViewById(R.id.photo_layout1);
 			photoLayout2 = (LinearLayout) findViewById(R.id.photo_layout2);
 			photoLayout3 = (LinearLayout) findViewById(R.id.photo_layout3);
 			photoLayout1.setTag(0);
 			photoLayout2.setTag(0);
 			photoLayout3.setTag(0);
 		}
 	}
 	private int photoWallWidth = 0;
 	private int wallPage = 0;
 	private final int offset = 3000;
 
 	private void addImageViewToList(ImageView imageView) {
 		if (!imageViewList.contains(imageView)) {
 			imageViewList.add(imageView);
 		}
 	}
 	/**
 	 * 图片墙初始化
 	 */
 	private void addGroupProducts() {
 		wallPage++;
 		initPhotoLayouts();
 		try {
 			if (photoWallWidth == 0) {
 				photoWallWidth = getResources().getDisplayMetrics().widthPixels / 3;
 			}
 			List<ImgInfo> imgInfoUrl = BaiduImage.getImgInfoList(
 					"android MM bizhi", 2, 20, 480, 800);
 			for (int i = 0; i < imgInfoUrl.size(); i++) {
 
 				ImageView imageView = generateWallImage(imgInfoUrl.get(i));
 				addImageViewToList(imageView);
 				updateImageOfProduct(imageView, false);
 				addImageViewToLayout(imageView, photoWallWidth);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	/**
 	 * 将图片画到屏幕上
 	 * 
 	 * @param imageView
 	 * @param photoWallWidth2
 	 */
 	private void addImageViewToLayout(ImageView imageView, int photoWallWidth) {
 		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 				photoWallWidth, (photoWallWidth * 320) / 240);
 		params.setMargins(0, 0, 0, (int) (4 * getResources()
 				.getDisplayMetrics().density));
 		LinearLayout tempLayout = getLayout();
 		tempLayout.addView(imageView, params);
 
 		int length = (Integer) tempLayout.getTag();
 		tempLayout.setTag(length + ((photoWallWidth * 320) / 240));
 	}
 	private LinearLayout getLayout() {
 		int length1 = (Integer) photoLayout1.getTag();
 		int length2 = (Integer) photoLayout2.getTag();
 		int length3 = (Integer) photoLayout3.getTag();
 		if ((length1 <= length2) && (length1 <= length3)) {
 			return photoLayout1;
 		} else {
 			if (length2 <= length3) {
 				return photoLayout2;
 			} else {
 				return photoLayout3;
 			}
 		}
 	}
 	private boolean isChecking = false;
 	private int checkCount = 0;
 
 	private void setScrollView() {
 		if (superScrollView == null) {
 			superScrollView = (SuperScrollView) findViewById(R.id.photo_wall_scrollview);
 		}
 		superScrollView.setVisibility(View.VISIBLE);
 		superScrollView.setScrollViewListener(new ScrollViewListener() {
 
 			public void onScrollChanged(SuperScrollView scrollView, int x,
 					int y, int oldx, int oldy) {
 				if (!isChecking) {
 					if ((checkCount++) > 10) {
 						if (oldy >= y) {
 							checkImageViewList(y, y + scrollView.getHeight(),
 									offset, false);
 						} else {
 							checkImageViewList(y, y + scrollView.getHeight(),
 									offset, true);
 						}
 						checkScrollStoped(scrollView);
 					}
 				}
 				superScrollView.getBottom();
 				View view = scrollView.getChildAt(0);
 				if (view.getMeasuredHeight() <= (scrollView.getScrollY()
 						+ scrollView.getHeight() + 0)) {
 					if (0 == WallPhotoActivity.index) {
 						WallPhotoActivity.index++;
 						loadNewPhotosHandler.sendEmptyMessage(0);
 					}
 				}
 			}
 
 			private void checkScrollStoped(SuperScrollView scrollView) {
 				long currentTimeInMillis = Calendar.getInstance()
 						.getTimeInMillis();
 				if ((currentTimeInMillis - oldTimeInMillis) > 100) {
 					int top = scrollView.computeVerticalScrollOffset();
 					sendScrollStopedImageViewList(top,
 							top + scrollView.getHeight(), 0);
 				}
 				oldTimeInMillis = currentTimeInMillis;
 			}
 		});
 
 	}
 	private String sendScrollStopedImageViewList(int top, int bottom, int offset) {
 		String item_ids = "";
 		ImageView tempImageView = null;
 		for (ImageView imageView : imageViewList) {
 			if ((imageView.getTop() > (bottom + offset))
 					|| (imageView.getBottom() < (top - offset))) {
 
 			} else {
 				if (item_ids.equals("")) {
 					item_ids = item_ids
 							+ String.valueOf(imageView.getTag(R.id.tag_id));
 				} else {
 					item_ids = item_ids + "-"
 							+ String.valueOf(imageView.getTag(R.id.tag_id));
 				}
 				tempImageView = imageView;
 			}
 		}
 		// if (tempImageView != null) {
 		// DataHandler
 		// .getInstance(this)
 		// .handleCustomData(
 		// DataAcquisitionParamsFactory.generateParams("page",
 		// String.valueOf(currentPage), "item_ids",
 		// item_ids, "index", String
 		// .valueOf(tempImageView
 		// .getTag(R.id.tag_page))),
 		// DataAcquisitionParamsFactory.EVENT_WALL_SCROLL_STOP);
 		// }
 
 		return item_ids;
 	}
 
 	public Handler loadNewPhotosHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			wallImageGetter.dispatchMessage(msg);
 		};
 	};
 	private void checkImageViewList(int top, int bottom, int offset,
 			boolean isDownFling) {
 		isChecking = true;
 		synchronized (imageViewList) {
 			ArrayList<ImgLoadSupporter> imgLoadSupporterList = new ArrayList<ImgLoadSupporter>();
 			for (ImageView imageView : imageViewList) {
 				if ((imageView.getTop() > (bottom + offset))
 						|| (imageView.getBottom() < (top - offset))) {
 					imageView.setImageBitmap(null);
 					imageView.setContentDescription(TAG_IMAGE_STATE_UNLOAD);
 				} else {
 					String contentString = imageView.getContentDescription()
 							.toString();
 					if (!contentString.equals(TAG_IMAGE_STATE_OK)
 							&& !contentString.equals(TAG_IMAGE_STATE_LOADING)) {
 						if (isDownFling) {
 							imgLoadSupporterList
 									.add(getImgLoadSupporter(imageView));
 						} else {
 							imgLoadSupporterList.add(0,
 									getImgLoadSupporter(imageView));
 						}
 						// updateImageOfProduct(imageView, true);
 					}
 				}
 			}
 			int size = imgLoadSupporterList.size();
 			if (size > 0) {
 				// Log.e("new local imgTask", "" + size);
 				service.loadImgFromLocal(imgLoadSupporterList);
 			}
 		}
 		checkCount = 0;
 		isChecking = false;
 	}
 
 	/**
	 * 创建单个图片对象
 	 * 
 	 * @return
 	 */
 	private ImageView generateWallImage(ImgInfo imgInfo) {
 		ImageView imageView = new ImageView(this);
 		imageView.setImageDrawable(null);
 		imageView.setBackgroundColor(Color.WHITE);
 		imageView.setTag(imgInfo.getUrl());
 		imageView.setTag(R.id.tag_page, wallPage);
 		imageView.setTag(R.id.tag_id, imgInfo.getTagId());
 		imageView.setClickable(true);
 		imageView.setContentDescription(TAG_IMAGE_STATE_UNLOAD);
 		return imageView;
 	}
 
 	private void updateImageOfProduct(ImageView imageView, boolean isLocal) {
 
 		service.loadImg(getImgLoadSupporter(imageView));
 	}
 
 	private final String TAG_IMAGE_STATE_OK = "true";
 	private final String TAG_IMAGE_STATE_LOADING = "isLoading";
 	private final String TAG_IMAGE_STATE_UNLOAD = "unload";
 	/**
 	 * 定义图片异步处理加载
 	 * 
 	 * @param imageView
 	 * @return
 	 */
 	private ImgLoadSupporter getImgLoadSupporter(final ImageView imageView) {
 		CallbackHandler callbackHandler = new CallbackHandler(
 				CallbackHandler.CallbackType.img) {
 			@Override
 			public void dispatchMessage(Message msg) {
 				super.dispatchMessage(msg);
 				Bitmap tempBitmap = (Bitmap) msg.obj;
 				if (tempBitmap != null) {
 					imageView.setImageBitmap(tempBitmap);
 					imageView.setContentDescription(TAG_IMAGE_STATE_OK);
 					tempBitmap = null;
 				}
 				msg.obj = null;
 				// tempBitmap.recycle();
 			}
 		};
 		ImgLoadSupporter imgLoadSupporter = new ImgLoadSupporter();
 		imgLoadSupporter.callbackHandler = callbackHandler;
 		imgLoadSupporter.url = (String) imageView.getTag();
 		imageView.setContentDescription(TAG_IMAGE_STATE_LOADING);
 		return imgLoadSupporter;
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (service != null) {
 			service.destroy();
 		}
 		super.onDestroy();
 	}
 
 }
