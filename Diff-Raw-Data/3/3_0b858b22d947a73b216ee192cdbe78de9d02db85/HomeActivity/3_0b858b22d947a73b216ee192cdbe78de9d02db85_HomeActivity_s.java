 package com.intalker.borrow;
 
 import com.intalker.borrow.cloud.CloudAPI;
 import com.intalker.borrow.cloud.CloudAPIAsyncTask.ICloudAPITaskListener;
 import com.intalker.borrow.config.AppConfig;
 import com.intalker.borrow.config.ResultCode;
 import com.intalker.borrow.data.AppData;
 import com.intalker.borrow.data.UserInfo;
 import com.intalker.borrow.friends.FriendsNavigationVertical;
 import com.intalker.borrow.isbn.ISBNResolver;
 import com.intalker.borrow.ui.book.BookGallery;
 import com.intalker.borrow.ui.book.BookShelfItem;
 import com.intalker.borrow.ui.control.ControlFactory;
 import com.intalker.borrow.ui.control.sliding.SlidingMenu;
 import com.intalker.borrow.ui.login.LoginDialog;
 import com.intalker.borrow.ui.login.RegisterView;
 import com.intalker.borrow.ui.social.SocialPanel;
 import com.intalker.borrow.util.DBUtil;
 import com.intalker.borrow.util.DensityAdaptor;
 import com.intalker.borrow.util.LayoutUtil;
 import com.intalker.borrow.util.StorageUtil;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 public class HomeActivity extends Activity {
 	private static HomeActivity app = null;
 	private BookGallery mBookGallery = null;
 	private SocialPanel mSocialPanel = null;
 
 	private RegisterView mReg = null;
 	private FriendsNavigationVertical mFriendsNavigation = null; // it also
 																	// contains
 																	// self info
 																	// and
 																	// action
 																	// button!
 	private SlidingMenu mSlidingMenu = null;
 
 	public void toggleLeftPanel() {
 		mSlidingMenu.toggleLeftView();
 	}
 
 	public void toggleRightPanel() {
 		mSlidingMenu.toggleRightView();
 	}
 
 	public static HomeActivity getApp() {
 		return app;
 	}
 
 	public BookGallery getBookGallery() {
 		return mBookGallery;
 	}
 
 	public SocialPanel getSocialPanel() {
 		return mSocialPanel;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		app = this;
 		AppData.getInstance().initialize();
 		DensityAdaptor.init(this);
		StorageUtil.initialize();
 		StorageUtil.loadCachedBooks();
 		CloudAPI.CloudToken = "";
 
 		setContentView(initializeWithSlidingStyle());
 		
 		tryAutoLogin();
 	}
 	
 	private void tryAutoLogin() {
 		if (CloudAPI.setAccessToken(DBUtil.loadToken())) {
 			CloudAPI.getLoggedInUserInfo(this, new ICloudAPITaskListener() {
 
 				@Override
 				public void onFinish(int returnCode) {
 					doAfterGetUserInfoByToken(returnCode);
 				}
 
 			});
 		}
 	}
 
 	private View initializeWithSlidingStyle() {
 		mSlidingMenu = new SlidingMenu(this);
 
 		mSlidingMenu.setLeftView(createNavigationPanel());
 		mSocialPanel = new SocialPanel(this);
 		mSlidingMenu.setRightView(mSocialPanel);
 		mSlidingMenu.setCenterView(createHomeUI());
 		return mSlidingMenu;
 	}
 
 	// private View createLeftNavigationPanel()
 	// {
 	// RelativeLayout naviPanel = new RelativeLayout(this);
 	// Button btn = new Button(this);
 	// RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
 	// RelativeLayout.LayoutParams.WRAP_CONTENT,
 	// RelativeLayout.LayoutParams.WRAP_CONTENT);
 	// lp.width = LayoutUtil.getNavigationPanelWidth();
 	// naviPanel.addView(btn, lp);
 	// return naviPanel;
 	// }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// getMenuInflater().inflate(R.menu.activity_home, menu);
 		menu.addSubMenu("Settings");
 		menu.addSubMenu("Help");
 		menu.addSubMenu("About");
 		menu.addSubMenu("Exit");
 		return true;
 	}
 
 	private void doAfterSignUp(int returnCode) {
 		switch (returnCode) {
 		case CloudAPI.Return_OK:
 			Toast.makeText(this, UserInfo.getCurLoggedinUser().toString(),
 					Toast.LENGTH_SHORT).show();
 			break;
 		case CloudAPI.Return_UserNameOccupied:
 			Toast.makeText(this, "User name occupied.", Toast.LENGTH_SHORT)
 					.show();
 			break;
 		case CloudAPI.Return_NetworkError:
 			Toast.makeText(this, "Network error.", Toast.LENGTH_SHORT).show();
 			break;
 		default:
 			Toast.makeText(this, "Unknown error.", Toast.LENGTH_SHORT).show();
 			break;
 		}
 	}
 
 	private void doAfterUplaod(int returnCode) {
 		switch (returnCode) {
 		case CloudAPI.Return_OK:
 			Toast.makeText(this, "Upload done!", Toast.LENGTH_SHORT).show();
 			break;
 		case CloudAPI.Return_BadToken:
 			Toast.makeText(this, "Bad token.", Toast.LENGTH_SHORT).show();
 			break;
 		case CloudAPI.Return_NetworkError:
 			Toast.makeText(this, "Network error.", Toast.LENGTH_SHORT).show();
 			break;
 		default:
 			Toast.makeText(this, "Unknown error.", Toast.LENGTH_SHORT).show();
 			break;
 		}
 	}
 
 	private void doAfterGetOwnedBooks(int returnCode) {
 		switch (returnCode) {
 		case CloudAPI.Return_OK:
 			Toast.makeText(this, "Sync done!", Toast.LENGTH_SHORT).show();
 			break;
 		case CloudAPI.Return_BadToken:
 			Toast.makeText(this, "Bad token.", Toast.LENGTH_SHORT).show();
 			break;
 		case CloudAPI.Return_NetworkError:
 			Toast.makeText(this, "Network error.", Toast.LENGTH_SHORT).show();
 			break;
 		default:
 			Toast.makeText(this, "Unknown error.", Toast.LENGTH_SHORT).show();
 			break;
 		}
 	}
 
 	private void doAfterGetUserInfoByToken(int returnCode) {
 		switch (returnCode) {
 		case CloudAPI.Return_OK:
 			mBookGallery.updateTopPanel();
 			mSocialPanel.getFriendView().refreshList();
 			break;
 		case CloudAPI.Return_NoSuchUser:
 			Toast.makeText(this, "No such user.", Toast.LENGTH_SHORT).show();
 			break;
 		case CloudAPI.Return_BadToken:
 			Toast.makeText(this, "Bad token.", Toast.LENGTH_SHORT).show();
 			break;
 		case CloudAPI.Return_NetworkError:
 			Toast.makeText(this, "Network error.", Toast.LENGTH_SHORT).show();
 			break;
 		default:
 			Toast.makeText(this, "Unknown error.", Toast.LENGTH_SHORT).show();
 			break;
 		}
 	}
 
 	public View createHomeUI() {
 		BookShelfItem.lastBookForTest = null;
 
 		LinearLayout mainLayout = new LinearLayout(this);
 
 		// book gallery ui
 		mBookGallery = new BookGallery(this);
 		LinearLayout.LayoutParams bookGalleryLP = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.FILL_PARENT,
 				LinearLayout.LayoutParams.FILL_PARENT);
 		mainLayout.addView(mBookGallery, bookGalleryLP);
 
 		mBookGallery.initialWithCachedData();
 
 		mReg = new RegisterView(this);
 		mainLayout.addView(mReg);
 		mReg.setVisibility(View.GONE);
 		mReg.setRegisterListener(new RegisterView.OnRegisterListener() {
 
 			@Override
 			public void onSuccess() {
 				mReg.setVisibility(View.GONE);
 				mBookGallery.setVisibility(View.VISIBLE);
 			}
 
 			@Override
 			public void onBack() {
 				mReg.setVisibility(View.GONE);
 				mBookGallery.setVisibility(View.VISIBLE);
 			}
 		});
 
 		// Test settings
 		mainLayout.setBackgroundColor(Color.GRAY);
 		return mainLayout;
 	}
 
 	private View createNavigationPanel() {
 		LinearLayout navigationBar = new LinearLayout(this);
 		navigationBar.setBackgroundResource(R.drawable.stone_bg);
 		navigationBar.setOrientation(LinearLayout.VERTICAL);
 		RelativeLayout.LayoutParams navigationBarLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.FILL_PARENT);
 
 		navigationBarLP.width = LayoutUtil.getNavigationPanelWidth();
 
 		navigationBar.setLayoutParams(navigationBarLP);
 
 		View logoView = new View(this);
 		logoView.setBackgroundResource(R.drawable.logo);
 		LinearLayout.LayoutParams logoLP = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.WRAP_CONTENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 		logoLP.width = LayoutUtil.getNavigationPanelWidth();
 		logoLP.height = LayoutUtil.getNavigationPanelWidth();
 		navigationBar.addView(logoView, logoLP);
 
 		navigationBar.addView(ControlFactory
 				.createHoriSeparatorForLinearLayout(this));
 
 		Button b = new Button(this);
 		b.setText("<<");
 		b.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				mSlidingMenu.toggleLeftView();
 			}
 
 		});
 		navigationBar.addView(b);
 		// Sign up test
 		ImageButton btn0 = new ImageButton(this);
 		// btn0.setText("Reg");
 		btn0.setImageResource(R.drawable.register);
 		btn0.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				mSlidingMenu.toggleLeftView();
 				mBookGallery.setVisibility(View.GONE);
 				mReg.setVisibility(View.VISIBLE);
 				// Login API test
 				// CloudAPI.signUp(v.getContext(),
 				// "xiangyun.gaox@adsk.com",
 				// "gao",
 				// "Xiangyun",
 				// new ICloudAPITaskListener(){
 				//
 				// @Override
 				// public void onFinish(int returnCode) {
 				// doAfterSignUp(returnCode);
 				// }
 				//
 				// });
 			}
 		});
 
 		navigationBar.addView(btn0);
 
 		ImageButton btn = new ImageButton(this);
 		// btn.setText("Login");
 		btn.setImageResource(R.drawable.login);
 		btn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO: it is possible to add annimation to shown an dialog ?
 				LoginDialog loginDialog = new LoginDialog(v.getContext());
 				loginDialog.show();
 			}
 		});
 		navigationBar.addView(btn);
 
 		if (AppConfig.isDebugMode) {
 			Button btn_loginbysession = new Button(this);
 			btn_loginbysession.setText("Login by test token");
 			btn_loginbysession.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					// Login API test
 					String testToken = "7a3cf000-0662-715b-a6a8-89feb8466014";
 					CloudAPI.setAccessToken(testToken);
 					CloudAPI.getLoggedInUserInfo(v.getContext(),
 							new ICloudAPITaskListener() {
 
 								@Override
 								public void onFinish(int returnCode) {
 									doAfterGetUserInfoByToken(returnCode);
 								}
 
 							});
 				}
 			});
 
 			navigationBar.addView(btn_loginbysession);
 
 			Button btn_test = new Button(this);
 			btn_test.setText("Temp Test");
 			btn_test.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					// For any test code
 					// CloudAPI.uploadBooks(HomeActivity.getApp(),
 					// new ICloudAPITaskListener() {
 					//
 					// @Override
 					// public void onFinish(int returnCode) {
 					// doAfterUplaod(returnCode);
 					// }
 					//
 					// });
 
 					CloudAPI.getOwnedBooks(HomeActivity.getApp(),
 							new ICloudAPITaskListener() {
 
 								@Override
 								public void onFinish(int returnCode) {
 									doAfterGetOwnedBooks(returnCode);
 								}
 
 							});
 				}
 			});
 
 			navigationBar.addView(btn_test);
 		}
 
 		// ImageButton btn1 = new ImageButton(this);
 		// // btn1.setText("Scan");
 		// btn1.setImageResource(R.drawable.scan);
 		// btn1.setOnClickListener(new OnClickListener() {
 		//
 		// @Override
 		// public void onClick(View v) {
 		// ScanUtil.scanBarCode(HomeActivity.this);
 		// }
 		// });
 		//
 		// navigationBar.addView(btn1);
 
 		if (AppConfig.isDebugMode) {
 			Button btn2 = new Button(this);
 			btn2.setText("Clear");
 			btn2.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					mBookGallery.resetBookShelf();
 				}
 			});
 
 			navigationBar.addView(btn2);
 		}
 		return navigationBar;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (requestCode == ResultCode.SCAN_RESULT_CODE) {
 			switch (resultCode) {
 			case RESULT_OK:
 				String isbn = data.getStringExtra("SCAN_RESULT");
 				int length = isbn.length();
 				if (10 == length || 13 == length) {
 					ISBNResolver.getInstance().getBookInfoByISBN(this, isbn);
 				} else {
 					Toast.makeText(this, this.getString(R.string.invalid_isbn),
 							Toast.LENGTH_SHORT).show();
 				}
 				break;
 			case RESULT_CANCELED:
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		super.onStop();
 		StorageUtil.saveCachedBooks();
 	}
 }
