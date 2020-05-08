 package com.baixing.activity.test;
 
 import org.athrun.android.framework.Test;
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
 
 public class DailyTestCase extends BaixingTestCase {
 	public static final String LOGO_TEXT = "百姓网";
 	public static final String CATEGORY_WUPINJIAOYI_TEXT = "物品交易";
 	public static final String CATEGORY_WUPINJIAOYI_SHOUJI_TEXT = "二手手机";
 	public static final String CATEGORY_WUPINJIAOYI_JIADIAN_TEXT = "家电";
 	public static final String CATEGORY_WUPINJIAOYI_YUEQIWENJU_TEXT = "乐器/文具";
 	public static final String CATEGORY_WUPINJIAOYI_GONGYESHEBEI_TEXT = "工业设备";
 	public static final String CATEGORY_WUPINJIAOYI_MIANFEIZENGSONG_TEXT = "免费赠送";
 	public static final String CATEGORY_CHELIANG_TEXT = "车辆买卖";
 	public static final String CATEGORY_CHELIANG_JIAOCHE_TEXT = "二手轿车";
 	public static final String CATEGORY_FANGWU_TEXT = "房屋租售";
 	public static final String CATEGORY_QUANZHIZHAOPIN_TEXT = "全职招聘";
 	public static final String CATEGORY_JIANZHIZHAOPIN_TEXT = "兼职招聘";
 	public static final String CATEGORY_QIUZHI_TEXT = "求职简历";
 	public static final String CATEGORY_JIAOYOU_TEXT = "交友活动";
 	public static final String CATEGORY_CHONGWU_TEXT = "宠物";
 	public static final String CATEGORY_SHENGHUOFUWU_TEXT = "生活服务";
 	public static final String CATEGORY_JIAOYUPEIXUN_TEXT = "教育培训";
 
 	public static final String ADLISTING_POST_TEXT = "发布";
 	public static final String ADLISTING_FILTER_MORE_TEXT = "更多";
 	public static final String ADVIEW_PRICE_TEXT = "价格";
 	public static final String ADVIEW_NO_PIC_TEXT = "暂无图片";
 	
 	public static final String ADLISTING_FILTER_BOX_CANCEL_TEXT = "取消";
 	public static final String ADLISTING_FILTER_BOX_SELECT_TEXT = "选择";
 	public static final String ADLISTING_FILTER_MORE_OK_TEXT = "确定";
 	public static final String ADLISTING_FILTER_MORE_CLEAR_TEXT = "清空";
 	
 	public DailyTestCase() throws Exception {
 	}
 	
 	@Test
 	public void testHome() throws Exception {
 		//Check some tab && logo
 		assertElementByText("Home Category/Search title not found", TAB_ID_HOME_TEXT);
 		assertElementByText("Home Post title not found", TAB_ID_POST_TEXT);
 		assertElementByText("Home MyCenter title not found", TAB_ID_MY_TEXT);
 		assertElementByText("Home BaixingWang title not found", LOGO_TEXT);
 		
 		//Check first some category
 		assertElementByText("Home First Category wupinjiaoyi not found", CATEGORY_WUPINJIAOYI_TEXT);
 		assertElementByText("Home First Category jianzhizhaopin not found", CATEGORY_JIANZHIZHAOPIN_TEXT);
 		assertElementByText("Home First Category jiaoyupeixun not found", CATEGORY_JIAOYUPEIXUN_TEXT);
 		
 		//Check second some category
 		findElementByText(CATEGORY_WUPINJIAOYI_TEXT).doClick();
 		sleep(1);
 		assertElementByText("Home Category wupinjiaoyi Second category shouji not found", CATEGORY_WUPINJIAOYI_SHOUJI_TEXT);
 		assertElementByText("Home Category wupinjiaoyi Second category jiadian not found", CATEGORY_WUPINJIAOYI_JIADIAN_TEXT);
 
 		assertElementById("Second Category list id:CATEGORY_SECOND_GRIDVIEW_ID:gridSecCategory not found", CATEGORY_SECOND_GRIDVIEW_ID);
 		doScrollView(CATEGORY_SECOND_GRIDVIEW_ID, 2);
 
 		assertElementByText("Home Category wupinjiaoyi Second category yueqi/wenju not found", CATEGORY_WUPINJIAOYI_YUEQIWENJU_TEXT);
 		assertElementByText("Home Category wupinjiaoyi Second category gongyeshebei not found", CATEGORY_WUPINJIAOYI_GONGYESHEBEI_TEXT);
 		assertElementByText("Home Category wupinjiaoyi Second category mianfeizengsong not found", CATEGORY_WUPINJIAOYI_MIANFEIZENGSONG_TEXT);
 		
 		goBack(true);
 		//Check some adListing
 		//Check and click ershouche
 		assertElementByText("Home First Category cheliangmaimai not found", CATEGORY_CHELIANG_TEXT);
 		findElementByText(CATEGORY_CHELIANG_TEXT).doClick();
 		assertElementByText("Home Category cheliangmaimai Second category ershoujiaoche not found", CATEGORY_CHELIANG_JIAOCHE_TEXT);
 		//Click ershouche
 		findElementByText(CATEGORY_CHELIANG_JIAOCHE_TEXT).doClick();
 		sleep(1);
 		waitForMsgBox(MSGBOX_SETTING_VIEWTYPE_NO_PIC_TEXT, MSGBOX_SETTING_VIEWTYPE_CANCEL_BUTTON_ID, 3000);
 		waitForHideMsgbox(20000);
 		sleep(5);
 		//Check ershouche adlisting
 		assertElementByText("adlisting ershoujiaoche title not found",  CATEGORY_CHELIANG_JIAOCHE_TEXT);
 		assertElementByText("adlisting ershoujiaoche fabu title not found",  ADLISTING_POST_TEXT);
 		assertElementByText("adlisting ershoujiaoche filter more title not found",  ADLISTING_FILTER_MORE_TEXT);
 
 		assertElementById("adlisting ershoujiaoche listView not found", AD_VIEWLIST_ID);
 		
 		//Open a Ad with pics
 		openAdWithPic(true);
 		//Check AdView
 		assertElementByText("adlisting ershoujiaoche adview price title not found", ADVIEW_PRICE_TEXT);
		assertElementByText("adlisting ershoujiaoche adview favorite title not found", AD_FAVORITE_BUTTON_TEXT);
 		
 		goBack();
 		
 		//Open a Ad without pics
 		openAdWithPic(false);
 		//Check AdView
 		if (findElementById(AD_VIEWLIST_ID) == null) {
 			assertElementByText("adlisting ershoujiaoche adview price title not found", ADVIEW_PRICE_TEXT);
 			assertElementByText("adlisting ershoujiaoche adview favorite title not found", AD_FAVORITE_BUTTON_TEXT);
 			assertElementByText("adlisting ershoujiaoche adview no pic title not found", ADVIEW_NO_PIC_TEXT);
 			goBack();
 		}
 		//Check more button
 		ViewElement footer = scrollAdListViewToFooter();
 		assertNotNull("adlisting ershoujiaoche more button not found", footer);
 		
 		//Return
 		assertElementById("adlisting id:AD_VIEWLIST_ID:lvGoodsList not found", AD_VIEWLIST_ID);
 		
 		goBack();
 		assertElementById("Second Category list id:CATEGORY_SECOND_GRIDVIEW_ID:gridSecCategory not found", CATEGORY_SECOND_GRIDVIEW_ID);
 		
 		goBack();
 		assertElementById("Home Category list id:CATEGORY_GRIDVIEW_ID:gridcategory not found", CATEGORY_GRIDVIEW_ID);
 		
 		sleep(1);
 		
 	}
 	
 	@Test
 	public void testListingPic() throws Exception {
 		
 		//android3.0
 		//点击我的百姓网>设置>流量优化设置
 		setAdListingViewType(MY_SETTING_VIETTYPE_PIC_TEXT);
 		//点击返回
 		goBack();
 		//点击浏览信息
 		openTabbar(TAB_ID_HOME_TEXT);
 		//点击物品交易>台式电脑
 		openCategoryByIndex(0, 4);
 		//检查listing信息为带图片展示
 		assertElementById(AD_VIEWLIST_ITEM_IMAGE_ID);
 		//点击返回
 		goBack();
 		//点击返回
 		goBack();
 		//点击我的百姓网>设置>流量优化设置
 		setAdListingViewType(MY_SETTING_VIETTYPE_NO_PIC_TEXT);
 		//点击返回
 		goBack();
 		//点击浏览信息
 		openTabbar(TAB_ID_HOME_TEXT);
 		//点击物品交易>台式电脑
 		openCategoryByIndex(0, 4);
 		//检查listing信息为不带图片展示
 		assertNoElementById(AD_VIEWLIST_ITEM_IMAGE_ID);
 		goBack();
 		goBack();
 		setAdListingViewType(MY_SETTING_VIETTYPE_PIC_TEXT);
 	}
 	
 	@Test
 	public void testListing1() throws Exception {
 		int first = random(3);
 		switch (first) {
 			case 1:
 				openListingByFirstCategory(CATEGORY_CHELIANG_TEXT);
 				break;
 			case 2:
 				openListingByFirstCategory(CATEGORY_FANGWU_TEXT);
 				break;
 			default:
 				openListingByFirstCategory(CATEGORY_WUPINJIAOYI_TEXT);
 				break;
 		}
 	}
 
 	@Test
 	public void testListing2() throws Exception {
 		int first = random(3);
 		switch (first) {
 			case 1:
 				openListingByFirstCategory(CATEGORY_QIUZHI_TEXT);
 				break;
 			case 2:
 				openListingByFirstCategory(CATEGORY_JIANZHIZHAOPIN_TEXT);
 				break;
 			default:
 				openListingByFirstCategory(CATEGORY_QUANZHIZHAOPIN_TEXT);
 				break;
 		}
 	}
 	
 	@Test
 	public void testListing3() throws Exception {
 		int first = random(4);
 		switch (first) {
 			case 1:
 				openListingByFirstCategory(CATEGORY_JIAOYOU_TEXT);
 				break;
 			case 2:
 				openListingByFirstCategory(CATEGORY_CHONGWU_TEXT);
 				break;
 			case 3:
 				openListingByFirstCategory(CATEGORY_JIAOYUPEIXUN_TEXT);
 				break;
 			default:
 				openListingByFirstCategory(CATEGORY_SHENGHUOFUWU_TEXT);
 				break;
 		}
 	}
 	
 	@Test
 	public void testPost1() throws Exception {
 		logon();
 		openTabbar(TAB_ID_POST_TEXT);
 		postByIndex(random(3), random(10));
 	}
 	
 	@Test
 	public void testPost2() throws Exception {
 		openTabbar(TAB_ID_POST_TEXT);
 		postByIndex(3 + random(3), random(10));
 	}
 	
 	@Test
 	public void testPost3() throws Exception {
 		openTabbar(TAB_ID_POST_TEXT);
 		postByIndex(6 + random(4), random(10));
 	}
 	
 	private void openListingByFirstCategory(String categoryName) throws Exception {
 		//openHomeCategoryByIndex(i);
 		findElementByText(categoryName).doClick();
 		AbsListViewElement subCatListView = findElementById(CATEGORY_SECOND_GRIDVIEW_ID,
 				AbsListViewElement.class);
 		if (subCatListView == null) {
 			goBack();
 			//openHomeCategoryByIndex(i);
 			findElementByText(categoryName).doClick();
 			subCatListView = findElementById(CATEGORY_SECOND_GRIDVIEW_ID,
 					AbsListViewElement.class);
 		}
 		int count = (subCatListView != null) ? subCatListView.getChildCount() : 50;
 		String oldCateName = "";
 		int j = random(20);
 		openSecondCategoryByIndex(j);
 		TextViewElement v = findElementById(VIEW_TITLE_ID, TextViewElement.class);
 		assertNotNull("checkListing " + categoryName + ":" + j + " id:VIEW_TITLE_ID:tvTitle not found", v);
 		if(!oldCateName.equals(v.getText())) {
 			oldCateName = v.getText();
 			try {
 				checkListing(categoryName, oldCateName);
 			} catch (NullPointerException ex) {
 				return;
 			}
 		}
 		goBack();
 		
 	}
 	
 	private void checkListing(String firstCategory, String categoryName) throws Exception {
 		boolean adNoPic = false;
 		BXViewGroupElement lv = findElementById(AD_VIEWLIST_ID, BXViewGroupElement.class);
 		assertNotNull("checkListing " + firstCategory + ":" + categoryName + " id:AD_VIEWLIST_ID:lvGoodsList not found", lv);
 		openAdWithPic(true);
 		if (findElementById(AD_VIEWLIST_ID) == null) {
 			if (findElementByText(ADLISTING_FILTER_BOX_CANCEL_TEXT) != null &&
 					findElementByText(ADLISTING_FILTER_BOX_SELECT_TEXT) != null) {
 				findElementByText(ADLISTING_FILTER_BOX_CANCEL_TEXT).doClick();
 			} else if(findElementByText(ADLISTING_FILTER_MORE_CLEAR_TEXT) != null &&
 					findElementByText(ADLISTING_FILTER_MORE_OK_TEXT) != null) {
 				findElementByText(ADLISTING_FILTER_MORE_OK_TEXT).doClick();
 			} else {
 				assertElementByText("checkListing " + firstCategory + ":" + categoryName + " adview favorite title not found", 
 						AD_FAVORITE_BUTTON_TEXT);
 
 				TextViewElement v = savePhoto();
 				assertNotNull(v); // clickByText(AD_BIG_IMAGE_SAVE_TEXT);
 				//检查弹出式提示信息，包含“成功”
 				assertEquals(true, waitForSubText(AD_BIG_IMAGE_SAVED_TEXT, 10000));
 				goBack();
 				TextViewElement tv = findDetailViewMetaByName(AD_DETAIL_META_AREA_TEXT);
 				assertNotNull(tv);
 				checkMap(firstCategory, categoryName);
 				goBack();
 			}
 		} else {
 			adNoPic = true;
 		}
 		int from = lv.getHeight() / 2 + lv.getHeight() /3;
 		int max = random(3);
 		for(int i = 0; i < max; i++)
 			lv.scrollByY(from, from - 200);
 		
 		openAdWithPic(false);
 		if (findElementById(AD_VIEWLIST_ID) == null) {
 			if (findElementByText(ADLISTING_FILTER_BOX_CANCEL_TEXT) != null &&
 					findElementByText(ADLISTING_FILTER_BOX_SELECT_TEXT) != null) {
 				findElementByText(ADLISTING_FILTER_BOX_CANCEL_TEXT).doClick();
 			} else if(findElementByText(ADLISTING_FILTER_MORE_CLEAR_TEXT) != null &&
 					findElementByText(ADLISTING_FILTER_MORE_OK_TEXT) != null) {
 				findElementByText(ADLISTING_FILTER_MORE_OK_TEXT).doClick();
 			} else {
 				assertElementByText("checkListing " + firstCategory + ":" + categoryName + " adview no pic title not found",
 						ADVIEW_NO_PIC_TEXT);
 
 				TextViewElement tv = findDetailViewMetaByName(AD_DETAIL_META_AREA_TEXT);
 				assertNotNull(tv);
 				checkMap(firstCategory, categoryName);
 				goBack();
 			}
 		} else {
 			assertTrue("checkListing " + firstCategory + ":" + categoryName + " maybe no ads?", !adNoPic);
 		}
 	}
 	
 	private void checkMap(String firstCategory, String categoryName) throws Exception {
 		//提取当前信息的地区地点信息，如“浦东金桥”
 		TextViewElement tv = findDetailViewMetaByName(AD_DETAIL_META_AREA_TEXT);
 		assertNotNull(tv);
 		TextViewElement tv2 = findElementById(VIEW_TITLE_ID, TextViewElement.class);
 		String adtitle = tv2.getText();
 		//点击地图查看
 		tv.doClick();
 		sleep(3);
 		assertNoElementByText("checkListing " + firstCategory + ":" + categoryName + " checkMap error", 
 				categoryName);
 		assertNoElementByText(AD_DETAIL_META_AREA_TEXT);
 		//检查页面title包含当前地区地点文字“金桥”
 		String area = tv.getText();
 		tv = findElementById(VIEW_TITLE_ID, TextViewElement.class);
 		assertNotNull(tv);
 		assertTrue(!adtitle.equals(area));
 		//boolean found = false;
 		//assertTrue("checkListing " + firstCategory + ":" + categoryName + " checkMap not found area:" + area, area.indexOf(tv.getText()) != -1);
 		goBack();
 	}
 	
 	private TextViewElement savePhoto() throws Exception {
 		//点击图片
 		showAdPic(0);
 		//检查title右侧包含button“保存”
 		TextViewElement v = findElementByText(AD_BIG_IMAGE_SAVE_TEXT);
 		//点击左上方button返回
 		goBack(true);
 		
 		//点击图片再次进入
 		showAdPic(0);
 		//点击右上方按钮保存
 		if (v != null) v.doClick();
 		return v;
 	}
 	
 	private void postByIndex(int firstIndex, int secondIndex) throws Exception {
 		String oldCateName = "";
 		openPostFirstCategory(firstIndex);
 		//openSecondCategoryByIndex(secondIndex);
 		TextViewElement v = openPostSecondCategory(secondIndex);
 		//TextViewElement v = findElementById(VIEW_TITLE_ID, TextViewElement.class);
 		assertNotNull("ERRORRETRY,checkPost Category,Post," + firstIndex + "," + secondIndex + ",", v);
 		if(!oldCateName.equals(v.getText())) {
 			oldCateName = v.getText();
 			postAutoEnterData(oldCateName.equals("女找男"));
 			sleep(1);
 			boolean success = postSend(false);
 			if (!success) {
 				//assertTrue("POST Category1:" + oldCateName + " ERROR", false); //TODO...
 			}
 			afterPostSend();
 			if (!checkPostSuccess(true)) {
 				if (!success) 
 					assertTrue("ERROR,Category,Post," + firstIndex + "," + secondIndex + "," + oldCateName, false);
 			}
 		}
 	}
 }
