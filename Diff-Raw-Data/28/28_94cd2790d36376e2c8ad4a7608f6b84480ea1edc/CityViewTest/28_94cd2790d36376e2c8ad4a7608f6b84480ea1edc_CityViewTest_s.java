 package com.quanleimu.activity.test;
 
 import java.util.concurrent.TimeUnit;
 
 import org.athrun.android.framework.AthrunTestCase;
 import org.athrun.android.framework.Test;
 import org.athrun.android.framework.viewelement.ViewElement;
 
 
 public class CityViewTest extends BaixingTestCase {
 	public static final String TEST_DATA_APP_NAME_TEXT = "百姓网";
 	public static final String TEST_DATA_CITY_SHENZHEN = "深圳";
 	public static final String CITY_SELECT_TEXT = "选择城市";
 	public static final String CITY_SELECT_TABLE_ID = "llParentView";
 	public static final String CITY_OTHER_CITY_TEXT = "选择其他城市";
 	public static final String CITY_SELECT_PROVICE_TEXT = "选择省份";
 	public static final String TEST_DATA_CITY_ZHEJIANG = "浙江";
 	public static final String TEST_DATA_CITY_HANGZHOU = "杭州";
 	
 	public CityViewTest() throws Exception {
		super();
		AthrunTestCase.setMaxTimeToFindView(10000);
 	}
 	
 	@Test
 	public void testCityView() throws Exception {
 		
 		//android3.0
 		//home页点击左上方按钮“**百姓网”
 		clickById(HOME_APP_NAME_ID);
 		//检查title文字为“选择城市”
 		ViewElement v = findElementByText(CITY_SELECT_TEXT);
 		assertNotNull(v);
 		//点击深圳
 		clickByText(TEST_DATA_CITY_SHENZHEN);
 		//检查home title为“深圳百姓网”
 		v = findElementByText(TEST_DATA_CITY_SHENZHEN);
 		assertNotNull(v);
 		v = findElementByText(TEST_DATA_APP_NAME_TEXT);
 		assertNotNull(v);
 		//再次在home页点击左上方按钮“深圳百姓网”
 		clickById(HOME_APP_NAME_ID);
 		//滑动当前页至下方，点击“选择其他城市”
 		scrollBottom(3, CITY_SELECT_TABLE_ID);
 		clickByText(CITY_OTHER_CITY_TEXT);
 		//检查title为“选择省份”
 		v = findElementByText(CITY_SELECT_PROVICE_TEXT);
 		assertNotNull(v);
 		//点击返回
 		goBack();
 		//检查title为“选择城市”
 		v = findElementByText(CITY_SELECT_TEXT);
 		assertNotNull(v);
 		//再次滑动当前页至下方，点击“选择其他城市”
 		scrollBottom(3, CITY_SELECT_TABLE_ID);
 		clickByText(CITY_OTHER_CITY_TEXT);
 		//点击“浙江”
 		clickByText(TEST_DATA_CITY_ZHEJIANG);
 		//检查title为“选择城市”
 		v = findElementByText(CITY_SELECT_TEXT);
 		assertNotNull(v);
 		//点击“杭州”
 		clickByText(TEST_DATA_CITY_HANGZHOU);
 		//检查home title为“杭州百姓网”
 		v = findElementByText(TEST_DATA_CITY_HANGZHOU);
 		assertNotNull(v);
 		v = findElementByText(TEST_DATA_APP_NAME_TEXT);
 		assertNotNull(v);
 		//重置
 		clickById(HOME_APP_NAME_ID);
 		clickByText(TEST_DATA_DEFAULT_CITYNAME);
 		
 	}
 }
