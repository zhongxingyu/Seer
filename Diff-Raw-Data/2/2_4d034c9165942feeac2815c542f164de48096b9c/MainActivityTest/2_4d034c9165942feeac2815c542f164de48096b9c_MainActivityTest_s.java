 package com.youxiachai.demo.test;
 
 import java.net.HttpURLConnection;
 
 import android.os.Bundle;
 import android.test.ActivityInstrumentationTestCase2;
 
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxCallback;
 import com.androidquery.callback.AjaxStatus;
 import com.androidquery.util.AQUtility;
 import com.youxiachai.ajax.ICallback;
 import com.youxiachai.demo.model.api.BookApi;
 import com.youxiachai.demo.model.api.CollectionListApi;
 import com.youxiachai.demo.model.bean.BookInfo;
 import com.youxiachai.demo.view.act.MainActivity;
 
 /**
  * @author youxiachai
  * @date 2013-7-16
  */
 public class MainActivityTest extends
 		ActivityInstrumentationTestCase2<MainActivity> {
 
 	public MainActivityTest() {
 		super(MainActivity.class);
 	}
 
 	AQuery request;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		request = new AQuery(getActivity());
 		AQUtility.setDebug(true);
 		AQUtility.debug("new act", getActivity() + ":"
 				+ getActivity().isFinishing());
 	}
 	
 	/**
 	 * 异步请求的结束信号
 	 */
 	public void done(){
 		AQUtility.debugNotify();
 	}
 
 	/**
 	 *测试网络是否通畅 
 	 */
 	public void testRequest() {
 		String url = "http://www.baidu.com";
 		request.ajax(url, String.class,  new AjaxCallback<String>(){
 			@Override
 			public void callback(String url, String object, AjaxStatus status) {
 				super.callback(url, object, status);
 				AQUtility.debug(object);
 				assertEquals(HttpURLConnection.HTTP_OK, status.getCode());
 				
 				done();
 			}
 		});
 		AQUtility.debugWait(10000);
 	}
 	
 	/**
 	 * 测试api url 是否正确
 	 */
 	public void testApiUrl () {
 		String book = "http://api.douban.com/v2/book/1220562";
 		String collections = "http://api.douban.com/v2/book/user/youxiachai/collections";
 		Bundle b = new Bundle();
 		b.putString("id", "1220562");
 		
 		assertEquals(book, new BookApi().getBookById(b));
 		
 		b.putString("id", "youxiachai");
 		assertEquals(collections, new CollectionListApi().getCollectionByUser(b));
 	}
 	
 	/**
 	 * 测试获取书籍
 	 */
 	public void testBookModel(){
 		Bundle b = new Bundle();
		b.putInt("id", 1220562);
 		new BookApi().get(b, request, new ICallback<BookInfo>() {
 			@Override
 			public void onSuccess(BookInfo result, Enum<?> type, AjaxStatus status) {
 				// TODO Auto-generated method stub
 				assertNotNull(result);
 				assertEquals("180", result.pages);
 				assertEquals("青岛出版社", result.publisher);
 				assertEquals("9787543632608", result.isbn13);
 				done();
 			}
 			
 			@Override
 			public void onError(int code, String message) {
 				// TODO Auto-generated method stub
 			}
 		});
 		
 		AQUtility.debugWait(10000);
 	}
 	
 	/**
 	 * 测试获取收藏api
 	 */
 	public void testCollections() {
 		Bundle query = new Bundle();
 //		query.("id", 66863378);
 		query.putString("id", "youxiachai");
 		
 		new CollectionListApi().get(query, request, new ICallback<CollectionListApi>() {
 			
 			@Override
 			public void onSuccess(CollectionListApi result, Enum<?> type,
 					AjaxStatus status) {
 				assertNotNull(result);
 				assertNotNull(result.collections.get(0).book);
 				BookInfo book = result.collections.get(0).book;
 				
 				assertEquals(222, result.total);
 				done();
 			}
 			
 			@Override
 			public void onError(int code, String message) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		AQUtility.debugWait(10000);
 	}
 
 	
 }
