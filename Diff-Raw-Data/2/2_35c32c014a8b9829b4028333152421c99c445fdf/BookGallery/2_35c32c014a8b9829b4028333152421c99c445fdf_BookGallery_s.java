 package com.intalker.borrow.ui.book;
 
 import java.util.ArrayList;
 
 import com.intalker.borrow.HomeActivity;
 import com.intalker.borrow.R;
 import com.intalker.borrow.cloud.CloudAPI;
 import com.intalker.borrow.cloud.CloudAPIAsyncTask.ICloudAPITaskListener;
 import com.intalker.borrow.data.AppData;
 import com.intalker.borrow.data.BookInfo;
 import com.intalker.borrow.data.UserInfo;
 import com.intalker.borrow.isbn.ISBNResolver;
 import com.intalker.borrow.ui.control.HaloButton;
 import com.intalker.borrow.util.DensityAdaptor;
 import com.intalker.borrow.util.DeviceUtil;
 import com.intalker.borrow.util.LayoutUtil;
 import com.intalker.borrow.util.ScanUtil;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class BookGallery extends RelativeLayout {
 
 	private RelativeLayout mTopPanel = null;
 	private RelativeLayout mBottomPanel = null;
 	private BookShelfView mShelfView = null;
 	private TextView mShelfOwnerTextView = null;
 	private HaloButton mToggleLeftPanelBtn = null;
 	private HaloButton mToggleRightPanelBtn = null;
 	private HaloButton mRefreshBtn = null;
 	
 	public BookGallery(Context context) {
 		super(context);
 		
 		createUI();
 	}
 	
 	public void updateTopPanel()
 	{
 		UserInfo curLoginUser = UserInfo.getCurLoggedinUser();
 		if(null != curLoginUser)
 		{
 			mShelfOwnerTextView.setText(curLoginUser.getNickName()
 					+ this.getContext().getString(R.string.shelf_owner_suffix));
 		}
 	}
 
 	private void createUI()
 	{
 		createTopPanel();
 		createScrolledShelfView();
 		createBottomPanel();
 	}
 	
 	private void createTopPanel()
 	{
 		mTopPanel = new RelativeLayout(this.getContext());
 		mTopPanel.setBackgroundResource(R.drawable.hori_bar);
 		RelativeLayout.LayoutParams topPanelLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.FILL_PARENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		topPanelLP.height = LayoutUtil.getGalleryTopPanelHeight();
 		this.addView(mTopPanel, topPanelLP);
 		
 		mShelfOwnerTextView = new TextView(this.getContext());
 		mShelfOwnerTextView.setText(R.string.app_name);
 		mShelfOwnerTextView.setTextColor(Color.WHITE);
 		RelativeLayout.LayoutParams textLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		textLP.addRule(RelativeLayout.CENTER_IN_PARENT);
 		mTopPanel.addView(mShelfOwnerTextView, textLP);
 		
 		mRefreshBtn = new HaloButton(this.getContext(), R.drawable.refresh);
 		mRefreshBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				CloudAPI.sychronizeOwnedBooks(HomeActivity.getApp(),
 						new ICloudAPITaskListener() {
 
 							@Override
 							public void onFinish(int returnCode) {
 								Context context = HomeActivity.getApp();
 								switch (returnCode) {
 								case CloudAPI.Return_OK:
 									ISBNResolver.getInstance().batchGetBookInfo(context);
 									break;
 								case CloudAPI.Return_BadToken:
 									Toast.makeText(context, "Bad token.", Toast.LENGTH_SHORT)
 											.show();
 									break;
 								case CloudAPI.Return_NetworkError:
 									Toast.makeText(context, "Network error.", Toast.LENGTH_SHORT).show();
 									break;
 								default:
 									Toast.makeText(context, "Unknown error.", Toast.LENGTH_SHORT).show();
 									break;
 								}
 							}
 
 						});
 
 			}
 			
 		});
 		RelativeLayout.LayoutParams refreshBtnLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		refreshBtnLP.addRule(RelativeLayout.CENTER_VERTICAL);
 		refreshBtnLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 		refreshBtnLP.rightMargin = DensityAdaptor.getDensityIndependentValue(5);
 		mTopPanel.addView(mRefreshBtn, refreshBtnLP);
 	}
 	
 //	private void updateGalleryAfterSync() {
 //		AppData appData = AppData.getInstance();
 //		ArrayList<BookInfo> bookInfoList = appData.getBooks();
 //		int length = bookInfoList.size();
 //		for(int i = 0; i < length; ++i)
 //		{
 //			BookInfo bookInfo = bookInfoList.get(i);
 //			if(null != bookInfo && !bookInfo.getInitialized())
 //			{
 //				ISBNResolver.getInstance().getBookInfoByISBN(this.getContext(), bookInfo.getISBN());
 //			}
 //		}
 //	}
 	
 	private void createBottomPanel()
 	{
 		mBottomPanel = new RelativeLayout(this.getContext());
 		View imageBackground = new View(this.getContext());
 		imageBackground.setBackgroundResource(R.drawable.hori_bar);
 		imageBackground.setId(10000);
 		RelativeLayout.LayoutParams imageBGLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.FILL_PARENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		imageBGLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		imageBGLP.height = LayoutUtil.getGalleryBottomPanelHeight();
 		mBottomPanel.addView(imageBackground, imageBGLP);
 		
 		HaloButton scanBtn = new HaloButton(this.getContext(), R.drawable.scan_barcode);
 		scanBtn.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				ScanUtil.scanBarCode(HomeActivity.getApp());
 			}
 			
 		});
 		RelativeLayout.LayoutParams scanBtnLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		scanBtnLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
 		scanBtnLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		mBottomPanel.addView(scanBtn, scanBtnLP);
 		
 		//Toggle left-panel button
 		mToggleLeftPanelBtn = new HaloButton(this.getContext(), R.drawable.menu);
 		mToggleLeftPanelBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				HomeActivity.getApp().toggleLeftPanel();
 			}
 			
 		});
 		RelativeLayout.LayoutParams toggleLeftPanelBtnLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		toggleLeftPanelBtnLP.addRule(RelativeLayout.ALIGN_BOTTOM, imageBackground.getId());
 		toggleLeftPanelBtnLP.leftMargin = LayoutUtil.getMediumMargin();
 		toggleLeftPanelBtnLP.bottomMargin = LayoutUtil.getMediumMargin();
 		mBottomPanel.addView(mToggleLeftPanelBtn, toggleLeftPanelBtnLP);
 		
 		//Toggle right-panel button
 		mToggleRightPanelBtn = new HaloButton(this.getContext(), R.drawable.social);
 		mToggleRightPanelBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				HomeActivity.getApp().toggleRightPanel();
 			}
 			
 		});
 		RelativeLayout.LayoutParams toggleRightPanelBtnLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 
 		toggleRightPanelBtnLP.addRule(RelativeLayout.ALIGN_BOTTOM, imageBackground.getId());
 		toggleRightPanelBtnLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 		toggleRightPanelBtnLP.rightMargin = LayoutUtil.getMediumMargin();
 		toggleRightPanelBtnLP.bottomMargin = LayoutUtil.getMediumMargin();
 		mBottomPanel.addView(mToggleRightPanelBtn, toggleRightPanelBtnLP);
 		
 		RelativeLayout.LayoutParams bottomPanelLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.FILL_PARENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		bottomPanelLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		this.addView(mBottomPanel, bottomPanelLP);
 	}
 	
 	private void createScrolledShelfView()
 	{
 		mShelfView = new BookShelfView(this.getContext());
 		RelativeLayout.LayoutParams shelfViewLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.FILL_PARENT,
 				RelativeLayout.LayoutParams.FILL_PARENT);
 		shelfViewLP.topMargin = LayoutUtil.getGalleryTopPanelHeight();
 		shelfViewLP.bottomMargin = LayoutUtil.getGalleryBottomPanelHeight();
 		this.addView(mShelfView, shelfViewLP);
 		
 		if(DeviceUtil.isFroyo())
 		{
			mShelfView.setOnTouchListener(new OnTouchListener(){
 
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					return true;
 				}
 				
 			});
 		}
 	}
 	
 	public void clearBooks()
 	{
 		mShelfView.clearShelfRows();
 	}
 	
 	public void resetBookShelf()
 	{
 		mShelfView.initializeShelfRows();
 		AppData.getInstance().clearBooks();
 	}
 	
 	// for test
 	public void fillWithRandomBooks()
 	{
 		mShelfView.clearShelfRows();
 		mShelfView.addRandomBooks();
 	}
 	
 	public void addBook(BookShelfItem bookItem)
 	{
 	}
 	
 	public void initialWithCachedData()
 	{
 		//mShelfView.clearShelfRows();
 		ArrayList<BookInfo> books = AppData.getInstance().getBooks();
 		for(BookInfo bookInfo : books)
 		{
 			mShelfView.addBookByExistingInfo(bookInfo);
 		}
 	}
 }
