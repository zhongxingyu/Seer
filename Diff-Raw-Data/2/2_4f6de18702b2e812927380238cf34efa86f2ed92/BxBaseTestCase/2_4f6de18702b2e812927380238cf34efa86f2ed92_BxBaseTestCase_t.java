 package com.baixing.activity.test;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.athrun.android.framework.AthrunTestCase;
 import org.athrun.android.framework.utils.RClassUtils;
 import org.athrun.android.framework.utils.SleepUtils;
 import org.athrun.android.framework.utils.ViewFinder;
 import org.athrun.android.framework.viewelement.AbsListViewElement;
 import org.athrun.android.framework.viewelement.IViewElement;
 import org.athrun.android.framework.viewelement.ScrollViewElement;
 import org.athrun.android.framework.viewelement.TextViewElement;
 import org.athrun.android.framework.viewelement.ViewElement;
 import org.athrun.android.framework.viewelement.ViewGroupElement;
 import org.athrun.android.framework.viewelement.ViewUtils;
 
 import android.os.Build;
 import android.os.Environment;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.TextView;
 import de.mindpipe.android.logging.log4j.LogConfigurator;
 
 public class BxBaseTestCase extends AthrunTestCase {
 	public static final String LOG_TAG = "BaixingTest";
 	
 	//Common ID
 	public static final String CAMERA_LOCK_FILE = "baixing_waiting_click.lock";
 	public static final String GALLERY_LOCK_FILE = "baixing_waiting_sendkey.lock";
 	public static final String SCREEN_SAVE_LOCK_FILE = "bxtestcase_err.lock";
 	public static final String BACK_BUTTON_TEXT = "返回";
 	public static final String BACK_BUTTON_ID = "left_action";
 	public static final String VIEW_TITLE_ID = "tvTitle";
 	
 	public static final String CATEGORY_GRIDVIEW_ID = "gridcategory";
 	public static final String CATEGROY_GRIDVIEW_NAME_ID = "itemtext";
 	public static final String CATEGORY_SECOND_GRIDVIEW_ID = "gridSecCategory";
 	public static final String TAB_ID_HOME = "ivHomePage";
 	public static final String TAB_ID_HOME_V3 = "tab_button_1";
 	public static final String TAB_ID_HOME_TEXT = "分类查找";
 	public static final String TAB_ID_POST = "tab_button_2";
 	public static final String TAB_ID_POST_TEXT = "免费发布";
 	public static final String TAB_ID_MY = "ivMyCenter";
 	public static final String TAB_ID_MY_V3 = "tab_button_3";
 	public static final String TAB_ID_MY_TEXT = "个人中心";
 	public static final String TAB_TEXT_ID = "tab_text";
 	//Home ID
 	public static final String HOME_APP_NAME_ID = "title_label_app_name";
 	public static final String HOME_FIRST_RUN_ID = "topguide";
 	public static final String HOME_FIRST_RUN_ID_V3 = "etSearchCity";
 	public static final String HOME_CATEGORY_VIEWLIST_ID = "cateSelection";
 	public static final String HOME_CATEGORY_VIEWLIST_ITEM_NAME_ID = "tvName";
 	public static final String HOME_BACK_TITLE = "确认退出";
 	public static final String HOME_BACK_MSG = "创建快捷方式";
 	public static final String HOME_BACK_BUTTON_TEXT = "否";
 
 	public static final String HOME_MARK_TEXTS = TAB_ID_HOME_TEXT + ",物品交易,全职招聘,求职简历";
 	
 	public static final String SEARCH_MARK_BUTTON_ID = "globalSearch";
 	public static final String SEARCH_TEXTVIEW_ID = "etSearch";
 	public static final String SEARCH_BUTTON_ID = "btnCancel";
 	public static final String SEARCH_BUTTON_TEXT = "搜索";
 	public static final String SEARCH_DELETE_TEXT = "清除历史记录";
 	public static final String SEARCH_CATEGORY_RESULT_ID = "lvSearchResultList";
 	
 	public static final String CATEGORY_VIEWLIST_ID = "cateSelection";
 	
 	//AdList && AdView ID
 	public static final String AD_VIEWLIST_MARK_ID = "goods_item_view_root";
 	public static final String AD_VIEWLIST_ITEM_TITLE_ID = "tvDes";
 	public static final String AD_VIEWLIST_ITEM_DATE_ID = "tvUpdateDate";
 	public static final String AD_VIEWLIST_MORE_ID = "pulldown_to_getmore";
 	public static final String AD_VIEWLIST_ITEM_IMAGE_ID = "ivInfo";
 	public static final String AD_VIEWLIST_ITEM_IMAGE_IMG = "home_bg_thumb_2x";
 	
 	public static final String AD_VIEWLIST_ID = "lvGoodsList";
 	
 	public static final String AD_DETAILVIEW_ID = "svDetail";
 	public static final String AD_DETAILVIEW_DESC_ID = "llgl";
 	public static final String AD_DETAILVIEW_PREV_ID = "btn_prev";
 	public static final String AD_DETAILVIEW_NEXT_ID = "btn_next";
 	public static final String AD_DETAILVIEW_TITLE_ID = "goods_tittle";
 	public static final String AD_IMAGES_VIEWLIST_ID = "glDetail";
 	public static final String AD_DETAILVIEW_NO_IMAGE_ID = "vad_no_img_tip";
 	public static final String AD_DETAIL_META_LABEL_ID = "tvmetatxt";
 	public static final String AD_DETAIL_META_VALUE_ID = "tvmeta";
 	public static final String AD_DETAIL_META_AREA_TEXT = "地区";
 	
 	public static final String AD_BIG_IMAGE_VIEW_ID = "vfCoupon";
 	public static final String AD_BIG_IMAGE_SAVE_TEXT = "保存";
	public static final String AD_BIG_IMAGE_SAVED_TEXT = "图片已保存到相册";
 	//public static final String AD_FAVORITE_BUTTON_ID = "btn_fav_unfav";
 	//public static final String AD_FAVORITE_ADD_IMG = "icon_fav";
 	//public static final String AD_FAVORITE_REMOVE_IMG = "icon_unfav";
 	public static final String AD_FAVORITE_BUTTON_TEXT = "收藏";
 	public static final String AD_FAVORITE_CANCEL_TEXT = "取消收藏";
 	//POST ID
 	public static final String POST_FORM_MARK_ID = "layout_txt";
 	public static final String POST_SCROLLVIEW_ID = "scrollView1";
 	public static final String POST_SCROLLVIEW_PARENT_ID = "postgoodslayout";
 	public static final String POST_CATEGORY_GRIDVIEW_ID = "firstcategory";
 	public static final String POST_CATEGORY_LIST_ITEM_ID = "tvCategoryName";
 	public static final String POST_SECOND_CATEGORY_LISTVIEW_ID = "post_other_list";
 	public static final int POST_CATEGORY_SELEC_INDEX = 0;
 	public static final String POST_META_LISTVIEW_ID = "post_other_list";
 	public static final String POST_META_ITEM_DISPLAY_ID = "postshow";
 	public static final String POST_META_ITEM_ID = "post_select";
 	public static final String POST_META_EDITTEXT_ID = "postinput";
 	public static final String POST_META_EDIT_DISPLAY_ID = "postshow";
 	public static final String POST_META_EDIT_DISPLAY_DESC_ID = "postdescriptionshow";
 	public static final String POST_META_EDITTEXT_DESC_ID = "postdescriptioninput";
 	public static final String POST_META_EDIT_ITEM_ID = "postinputlayout";
 	public static final String POST_DONE = "完成";
 	public static final String POST_SEND = "立即免费发布";
 	public static final String POST_BACK_DIALOG_OK_BUTTON_ID = "是";
 	public static final String POST_META_IMAGEVIEW1_ID = "iv_1";
 	public static final String POST_META_IMAGEVIEW2_ID = "iv_2";
 	public static final String POST_META_IMAGEVIEW3_ID = "iv_3";
 	public static final String POST_CATEGORY_TEXT = "分类";
 	public static final String POST_CAMERA_PHOTO_TEXT = "拍照";
 	public static final String POST_GALLERY_PHOTO_TEXT = "相册";
 	
 	//My ID
 	public static final String MY_LISTITEM_MYAD_ID = "rl_wosent";
 	public static final String MY_LISTING_MYAD_COUNTER_ID = "tv_sentcount";
 	public static final String MY_LISTING_MYAD_TEXT = "已发布";
 	public static final String MY_LISTING_MYAD_APPROVE_TEXT = "审核未通过";
 	public static final String MY_LISTING_TITLE_TEXT = "已发布的信息";
 	public static final String MY_MYAD_APPROVE_BUTTON_ID = "ivMyfav";
 	public static final String MY_MYAD_DELETE_BUTTON_ID = "ivMyhistory";
 	public static final String MY_LISTING_HISTORY_ID = "rl_wohistory";
 	public static final String MY_LISTING_HISTORY_TEXT = "最近浏览";
 	public static final String MY_LISTING_HISTORY_COUNTER_ID = "tv_historycount";
 	public static final String MY_AD_FxH_VIEWLIST_ID = "plvlist";
 	public static final String MY_LISTING_FAVORITE_TEXT = "收藏";
 	
 	public static final String MY_SETTING_BUTTON_TEXT = "设置";
 	public static final String MY_LOGIN_BUTTON_TEXT = "登录百姓网";
 	public static final String MY_LOGOUT_BUTTON_TEXT = "退出登录";
 	public static final String MY_LOGOUT_OK_BUTTON_TEXT = "确认";
 	public static final String MY_LOGIN_BUTTON_ID = "btn_login";
 	public static final String MY_LOGIN_USER_TEXTVIEW_ID = "et_account";
 	public static final String MY_LOGIN_PASSWORD_TEXTVIEW_ID = "et_password";
 	public static final String MY_LOGON_SUCCESS_MESSAGE = "用户登录成功";
 	public static final String DIALOG_OK_BUTTON_TEXT = "确定";
 	public static final String MY_EDIT_BUTTON_ID = "编辑";
 	public static final String MY_DELETE_ALL_BUTTON_ID = "清空";
 	public static final String MY_BIND_DIALOG_OK_BUTTON_ID = "是";
 	public static final String MY_BIND_DIALOG_NO_BUTTON_ID = "否";
 	public static final String MY_PROFILE_USERNAME_ID = "userInfoNickname";
 	public static final String MY_PROFILE_EDIT_BUTTON_ID = "userInfo_editUsername_btn";
 	public static final String MY_PROFILE_EDIT_USERNAME_ID = "dialog_edit_username_et";
 	public static final String MY_PROFILE_EDIT_UPDATE_TEXT = "修改";
 	
 	public static final String MY_DETAILVIEW_MANAGE_BUTTON_ID = "managebtn";
 	public static final String MY_DETAILVIEW_DELETE_BUTTON_ID = "vad_btn_delete";
 	public static final String MY_VIEWLIST_DELXUPDATE_BUTTON_ID = "btnListOperate";
 	public static final String MY_VIEWLIST_DELETE_BUTTON_TEXT = "删除";
 	public static final String MSGBOX_CANCEL_TEXT = "取消";
 	public static final String MSGBOX_OPT_TITLE = "操作";
 	
 	public static final String MY_PROFILE_PHOTO_ID = "personalImage";
 	
 	public static final String MY_SETTING_VIETTYPE_TEXT = "流量优化设置";
 	public static final String MY_SETTING_VIETTYPE_PIC_TEXT = "图片模式";
 	public static final String MY_SETTING_VIETTYPE_NO_PIC_TEXT = "2G/3G网络下列表无图";
 	public static final String MSGBOX_SETTING_VIEWTYPE_NO_PIC_TEXT = "是否切换到无图模式";
 	public static final String MSGBOX_SETTING_VIEWTYPE_CANCEL_BUTTON_ID = "否";
 	
 	//Msgbox Texts
 	public static final String MSGBOX_TITLE_TEXT = "提示";
 	public static final String MSGBOX_WAITFOR_TEXT = "请稍候...";
 	public static final String MSGBOX_WAITFOR_PASSWORD_TEXT = "数据下载中，请稍后。。。";
 	public static final String MSGBOX_WAITFOR_PERSONAL_INFO_TEXT = "正在下载数据，请稍候...";
 	public static final String MSGBOX_WAITFOR_PROFILE_EDIT_TEXT = "更新中，请稍等...";
 	public static final String MSGBOX_WAITFOR_PROFILE_PHOTO_TEXT = "图片上传中，请稍等。。。";
 	
 	//POST META TYPE
 	public enum METATYPE {
 		CATEGORY("METATYPE_CATEGORY"),
 		SELECT("METATYPE_SELECT"),
 		MULTISELECT("METATYPE_MULTI_SELECT"),
 		TEXT("METATYPE_TEXT"),
 		TITLE("METATYPE_TITLE");
 		private String name;
 		METATYPE(String name) {
 			this.name = name;
 		}
 		@Override
 		public String toString() {
 			return this.name;
 		}
 	}
 	//Test DATA
 	public static final String TEST_DATA_MOBILE = "13917067724";
 	public static final String TEST_DATA_PASSWORD = "whonwyhw";
 	public static final String TEST_DATA_DEFAULT_CITYNAME = "上海";
 	public static final String TEST_DATA_CAT_WUPINJIAOYI = "物品交易";
 	
 	@SuppressWarnings("unchecked")
 	public BxBaseTestCase() throws Exception {
 		super("com.quanleimu.activity", "com.quanleimu.activity.QuanleimuMainActivity");
 		AthrunTestCase.setMaxTimeToFindView(3000);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		// log("This is a test for log() method");
 		startScreen();
 		startScreen_v3();
 	}
 	
 	public static void lockStatus(String statusFile, String params) {
 		String lockDirPath = "/mnt/sdcard/Athrun/";
 		File d = new File(lockDirPath);
 		if (!d.exists()) {
 			lockDirPath = Environment.getExternalStorageDirectory().getPath()  + "/Athrun/";
 			d = new File(lockDirPath);
 			if (!d.exists()) {
 				return;
 			}
 		}
 		
 		String lockFilePath = lockDirPath + statusFile;
 		LogConfigurator logConfigurator = new LogConfigurator();
 		logConfigurator.setFileName(lockFilePath);
 		logConfigurator.setRootLevel(Level.INFO);
 		logConfigurator.configure();
 		Logger logger = Logger.getLogger(BaixingTestCase.class);
 		logger.info("lock_params:" + params);
 
 		final Timer timer = new Timer();
 		class BxTimerTask extends TimerTask {
 			public boolean willStopped = false;
             @Override
             public void run () {
             	willStopped = true;
                 timer.cancel();
             }
         };
         BxTimerTask tt = new BxTimerTask();
         timer.schedule(tt, 10 * 1000); // N秒 * 1000
         while(!tt.willStopped) {
         	try {
         		Thread.sleep(1 * 100);
         		File file = new File(lockFilePath);
         		if (!file.exists()) {
         			timer.cancel();
         			break;
         		}
         	} catch (Exception ex) {}
         }
 	}
 	
 	public static void waitScreenSave() {
 		lockStatus(SCREEN_SAVE_LOCK_FILE, "");
 		/*try {
 			File lockFile = new File(lockFilePath);
     		if (!lockFile.exists()) {
     			lockFile.createNewFile();
     		}
     	} catch (Exception ex) {
     		AthrunTestCase.assertTrue("Cannot created err lock file.", true);
     	}*/
 	}
 	
 	public static void waitClickXY(int x, int y) {
 		lockStatus(CAMERA_LOCK_FILE, x + "," + y);
 	}
 	
 	public static void waitClickCamera() {
 		int x = 400, y = 710;
 		if (Build.VERSION.SDK_INT >= 11) { //Build.VERSION_CODES.HONEYCOMB
 			//Log.i(LOG_TAG, "waitClickCamera:device " + Build.MODEL);
 			x= 100; y = 710;
 			if (Build.MODEL.equals("Nexus 7")) {
 				x = 150; y = 1810;
 			}
 		}
 		waitClickXY(x, y);
 	}
 	
 	public static void waitSendKey(int keycode) {
 		lockStatus(GALLERY_LOCK_FILE, String.valueOf(keycode));
 	}
 	
 	public static void assertNotNull(Object object) {
 		assertNotNull(null, object);
 	}
 	
 	public static void assertTrue(boolean condition) {
 		assertTrue(null, condition);
 	}
 	
 	public static void assertEquals(String expected, String actual) {
 		assertEquals(null, expected, actual);
 	}
 
 	public static void assertEquals(int expected, int actual) {
 		assertEquals(null, String.valueOf(expected), String.valueOf(actual));
 	}
 	
 	public static void assertNotNull(String message, Object object) {
 		if (object == null) waitScreenSave();
 		AthrunTestCase.assertNotNull(message, object);
 	}
 	
 	public static void assertTrue(String message, boolean condition) {
 		if (condition == false) waitScreenSave();
 		AthrunTestCase.assertTrue(message, condition);
 	}
 	public static void assertEquals(String message, String expected, String actual) {
 		if (!expected.equals(actual)) waitScreenSave();
 		AthrunTestCase.assertEquals(message, expected, actual);
 	}
 	
 	private void startScreen() throws Exception {
 		assertEquals(true, getDevice().waitForActivity("QuanleimuMainActivity", 3000));
 		TextViewElement vm = findElementByText("以后再说");
 		if (vm != null) {
 			vm.doClick();
 			TimeUnit.SECONDS.sleep(1);
 		}
 		ViewElement v = findElementById(HOME_FIRST_RUN_ID);
 		if (v != null) {
 			v.doClick();
 			TimeUnit.SECONDS.sleep(1);
 		}
 	}
 	
 	private void startScreen_v3() throws Exception {
 		Log.i(LOG_TAG, "This is a test for startScreen_v3() method");
 		assertEquals(true, getDevice().waitForActivity("QuanleimuMainActivity", 3000));
 		TextViewElement v = findElementById(HOME_FIRST_RUN_ID_V3, TextViewElement.class);
 		if (v != null) {
 			v.setText(TEST_DATA_DEFAULT_CITYNAME);
 			SleepUtils.sleep(300);
 			ViewElement c = findElementByText(TEST_DATA_DEFAULT_CITYNAME, 1, true);
 			assertNotNull(c);
 			c.doClick();
 			TimeUnit.SECONDS.sleep(10);
 		}
 	}
 	
 	public <T> T findElementById(String literalId, int index, Class<T> returnType) throws Exception {
 		T v = null;
 		try {
 			 v = super.findElementById(literalId, index, returnType);
 		} catch (NoSuchFieldException ex) {}
 		return v;
 	}
 	
 	public <T> T findElementById(String literalId,
 			Class<T> returnType) throws Exception {
 		return this.findElementById(literalId, 0, returnType);
 	}
 	
 	public ViewElement findElementById(String literalId, int index) throws Exception {
 		return this.findElementById(literalId, index, ViewElement.class);
 	}
 	
 	public ViewElement findElementById(String literalId) throws Exception {
 		return this.findElementById(literalId, 0);
 	}
 	
 	public boolean findElementByTexts(String texts) throws Exception {
 		String[] lstText = texts.split(",");
 		for(int i = 0; i < lstText.length; i++) {
 			if (findElementByText(lstText[i], 0, true) == null) return false;
 		}
 		return true;
 	}
 	
 	public TextViewElement findElementByViewId(String listViewId) throws Exception {
 		return findElementByViewId(listViewId, null);
 	}
 	public TextViewElement findElementByViewId(String listViewId, String displayName) throws Exception {
 		if (displayName == null || displayName.length() == 0) return findElementById(listViewId, TextViewElement.class);
 		ScrollViewElement scrollView = null;
 		AbsListViewElement listView = null;
 		if (listViewId != null) {
 			try {
 
 				if (listViewId != null && listViewId.equals(POST_SCROLLVIEW_PARENT_ID)) {
 					scrollView = getPostScrollView();
 				} else {
 					if (listViewId != null) {
 						scrollView = findElementById(listViewId, ScrollViewElement.class);
 						assertNotNull(scrollView);
 					}
 				}
 			} catch (IllegalArgumentException ex) {
 				listView = findElementById(listViewId,
 						AbsListViewElement.class);
 				assertNotNull(listView);
 			}
 		} else {
 			for (int i = 0; i < 10; i++) {
 				scrollView = findScrollElementByIndex(i);
 				if (scrollView != null) break;
 			}
 			if (scrollView == null) {
 				for (int i = 0; i < 10; i++) {
 					listView = findListElementByIndex(i);
 					if (listView != null) break;
 				}
 			}
 		}
 		int loop = 0;
 		TextViewElement v3 = findElementByText(displayName, 0, true);
 		while(v3 == null) {
 			if (scrollView != null) scrollView.scrollToNextScreen();
 			else listView.scrollToNextScreen();
 			TimeUnit.SECONDS.sleep(1);
 			v3 = findElementByText(displayName, 0, true);
 			if (loop++ > 10) break;
 		}
 		return v3;
 	}
 	
 	public void clickById(String vId) throws Exception {
 		ViewElement v = findElementById(vId);
 		clickView(v);
 	}
 	
 	public <T extends ViewElement> T clickById(String vId, Class<T> returnType) throws Exception {
 		T v = findElementById(vId, returnType);
 		clickView(v);
 		return v;
 	}
 	
 	public TextViewElement clickByText(String text) throws Exception {
 		TextViewElement v = findElementByText(text);
 		clickView(v);
 		return v;
 	}
 	
 	public TextViewElement clickByText(String text, boolean match) throws Exception {
 		TextViewElement v = findElementByText(text, 0, match);
 		clickView(v);
 		return v;
 	}
 	
 	public void clickView(ViewElement v) throws Exception {
 		clickView(v, true);
 	}
 	
 	public void clickView(ViewElement v, boolean asserted) throws Exception {
 		if (asserted) {
 			assertNotNull(v);
 		}
 		if (v != null) v.doClick();
 		TimeUnit.SECONDS.sleep(1);
 	}
 	
 	public String getTextByElementId(String vId) throws Exception {
 		TextViewElement titleView = findElementById(vId, TextViewElement.class);
 		assertNotNull(titleView);
 		return titleView.getText();
 	}
 	
 	public ScrollViewElement getPostScrollView() throws Exception {
 		ViewGroupElement gridView = findElementById(POST_SCROLLVIEW_PARENT_ID,
 				ViewGroupElement.class);
 		assertNotNull(gridView);
 		ScrollViewElement scrollView = gridView.getChildByIndex(0, ScrollViewElement.class);
 		assertNotNull(scrollView);
 		return scrollView;
 	}
 	
 	public boolean checkHomeAlert() throws Exception {
 		if (findElementByText(HOME_BACK_TITLE, 0, true) == null) return false;
 		if (findElementByText(HOME_BACK_MSG, 0, true) == null) return false;
 		ViewElement v = findElementByText(HOME_BACK_BUTTON_TEXT, 0, true);
 		if (v == null) return false;
 		v.doClick();
 		TimeUnit.SECONDS.sleep(1);
 		return true;
 	}
 	
 	public void openTabbar(String vId) throws Exception {
 		ViewGroupElement v = findElementById(vId, ViewGroupElement.class);
 		int i = 0;
 		while(v == null) {
 			goBack();
 			if (checkHomeAlert()) break;
 			if (i++ > 10) break;
 			v = findElementById(vId, ViewGroupElement.class);
 		}
 		if (v != null) {
 			TextViewElement tv = v.findElementById(TAB_TEXT_ID, TextViewElement.class);
 			assertNotNull(tv);
 			if (tv == null) return;
 			if ((vId.equals(TAB_ID_HOME_V3) && tv.getText().equals(TAB_ID_HOME_TEXT))
 				|| (vId.equals(TAB_ID_MY_V3) && tv.getText().equals(TAB_ID_MY_TEXT))
 				|| (vId.equals(TAB_ID_POST) && tv.getText().equals(TAB_ID_POST_TEXT)))
 			{
 				v.doClick();
 				TimeUnit.SECONDS.sleep(1);
 			}
 		}
 	}
 	
 	public TextViewElement getGridItemByText(String text, String gridId) throws Exception {
 		ViewGroupElement gridView = null;
 		try {
 			gridView = findElementById(gridId, ViewGroupElement.class);
 			assertNotNull(gridView);
 			for(int i = 0; i < 50; i++) {
 				TextViewElement v = getGridItemByIndex(i, gridId);
 				if (v.getText().equals(text)) return v;
 			}
 		} catch (Exception ex) {
 			assertNotNull(gridView);
 		}
 		return null;
 	}
 	
 	public <T extends ViewElement> T getGridItemByIndex(int index, String gridId) throws Exception {
 		TextViewElement textView = null;
 		ViewGroupElement gridView = null;
 		try {
 			gridView = findElementById(gridId, ViewGroupElement.class);
 			assertNotNull(gridView);
 			textView = findElementById(CATEGROY_GRIDVIEW_NAME_ID, index,
 					TextViewElement.class);
 			if (textView != null) {
 				TimeUnit.MILLISECONDS.sleep(300);
 				assertNotNull(textView.getText());
 				assertTrue(textView.getText().length() > 0);
 			} else {
 				ViewGroupElement item = gridView.getChildByIndex(index, ViewGroupElement.class);
 				//assertNotNull(item);
 				if (item != null) return (T)item;
 			}
 		} catch (Exception ex) {
 			assertNotNull(gridView);
 		}
 		return (T)textView;
 	}
 	
 	public boolean showNextView(String viewId) throws Exception {
 		BXViewGroupElement bv = findElementById(viewId,
 				BXViewGroupElement.class);
 		if (bv != null) {
 			TimeUnit.SECONDS.sleep(1);
 			bv.doTouch(-bv.getWidth() + 20);
 			TimeUnit.SECONDS.sleep(3);
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean showPrevView(String viewId) throws Exception {
 		BXViewGroupElement bv = findElementById(viewId,
 				BXViewGroupElement.class);
 		if (bv != null) {
 			TimeUnit.SECONDS.sleep(1);
 			bv.doTouch(bv.getWidth() - 20);
 			TimeUnit.SECONDS.sleep(3);
 			return true;
 		}
 		return false;
 	}
 	
 	public void clickViewById(String itemId) throws Exception {
 		ViewElement el = findElementById(itemId);
 		assertNotNull(el);
 		el.doClick();
 		TimeUnit.SECONDS.sleep(1);
 	}
 	
 	public AbsListViewElement findListView() throws Exception {
 		ArrayList<View> views = ViewUtils.getAllViews(false);
 		if (views.size() == 0) return null;
 		for (View view : views) {
 			try {
 				AbsListViewElement lv = findElementById(view.getId(), AbsListViewElement.class);
 				if (lv != null && lv.getChildCount() > 0) {
 					return lv;
 				}
 			} catch (IllegalArgumentException e) {}
 		}
 		return null;
 	}
 
 	public void goBack() throws Exception {
 		goBack(true);
 	}
 	
 	public void goBack(boolean force) throws Exception {
 		ViewElement iv = null;
 		try {
 			iv = findElementById(BACK_BUTTON_ID);
 		} catch (Exception ex) {
 		}
 		if (!force) assertNotNull(iv);
 		if (iv != null) {
 			iv.doClick();
 		} else {
 			getDevice().pressBack();
 		}
 
 		TimeUnit.SECONDS.sleep(1);
 	}
 	
 	/*
 	 * 滚动页面，输出最后一个index号
 	 */
 	public int doScrollView(String viewId, int page) throws Exception {
 		AbsListViewElement lv = findElementById(viewId,
 				AbsListViewElement.class);
 		if (lv == null) return 0;
 		for (int i = 0; i < (page > 0 ? page: 10); i++) {
 			lv.scrollToNextScreen();
 		}
 		TimeUnit.SECONDS.sleep(3);
 		int lastIndex = lv.getLastVisiblePosition();
 		return lastIndex;
 	}
 	
 	public void scrollBottom(int pageSize, String viewId) throws Exception {
 		BXViewGroupElement bv = findElementById(viewId, BXViewGroupElement.class);
 		assertNotNull(bv);
 		scrollY(pageSize, viewId, bv.getHeight() / 2 - bv.getHeight() /3, bv.getHeight() / 2 + bv.getHeight() /3);
 	}
 	
 	public void scrollTop(int pageSize, String viewId) throws Exception {
 		BXViewGroupElement bv = findElementById(viewId, BXViewGroupElement.class);
 		assertNotNull(bv);
 		scrollY(pageSize, viewId, bv.getHeight() / 2 - bv.getHeight() /3, bv.getHeight() / 2 + bv.getHeight() /3);
 	}
 	
 	public void scrollY(int pageSize, String viewId, int fromY, int toY) throws Exception {
 		for (int i = 0; i < pageSize; i++) {
 			BXViewGroupElement bv = findElementById(viewId, BXViewGroupElement.class);
 			assertNotNull(bv);
 			bv.scrollByY(fromY, toY);
 			TimeUnit.SECONDS.sleep(2);
 		}
 	}
 	
 	public boolean waitForHideMsgbox(int timeout) throws Exception {
 		return waitForHideMsgbox("请稍候...", timeout);
 	}
 	
 	public boolean waitForHideMsgbox(String msg, int timeout) throws Exception {
 		SleepUtils.sleep(IViewElement.RETRY_TIME);
 		final long startTime = System.currentTimeMillis();
 		while (System.currentTimeMillis() < startTime + timeout) {
 			ViewElement v = findElementByText("提示", 0, true);
 			if (v == null) return true;
 			v = findElementByText(msg, 0, true);
 			if (v == null) return true;
 			SleepUtils.sleep(IViewElement.RETRY_TIME);
 		}
 
 		return false;
 	}
 	
 	boolean waitForSubText(String text, int timeout) {
 		ViewFinder viewFinder = new ViewFinder();
 		ArrayList<TextView> textViews = viewFinder.findViewsByText(text, false, timeout);
 		if (textViews == null) return false;
 		return textViews.isEmpty() ? false : true;
 	}
 	
 	boolean waitForSubTexts(String texts, int timeout) {
 		ViewFinder viewFinder = new ViewFinder();
 		String[] tt = texts.split("@");
 		int tmpTimeout = 10;
 		int loop = (int)(timeout / tmpTimeout);
 		for (int j = 0; j < loop; j++) {
 			for(int i = 0; i < tt.length; i++) {
 				ArrayList<TextView> textViews = viewFinder.findViewsByText(tt[i], false, tmpTimeout);
 				if (textViews != null && !textViews.isEmpty()) return true;
 			}
 		}
 		return false;
 	}
 	
 	boolean waitForMsgBox(String msg, String btnText, int timeout) throws Exception {
 		if (waitForSubText(msg, timeout)) {
 			ViewElement v = findElementByText(btnText, 0, true);
 			if (v != null) {
 				v.doClick();
 				SleepUtils.sleep(300);
 			}
 			return true;
 		}
 		return false;
 	}
 	
 }
