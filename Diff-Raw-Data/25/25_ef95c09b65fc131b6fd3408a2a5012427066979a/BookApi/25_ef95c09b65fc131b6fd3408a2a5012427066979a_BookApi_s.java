 package com.youxiachai.demo.model.api;
 
 import android.os.Bundle;
 
 import com.alibaba.fastjson.JSON;
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxStatus;
 import com.androidquery.callback.Transformer;
 import com.youxiachai.ajax.ICallback;
 import com.youxiachai.ajax.NetCallback;
 import com.youxiachai.ajax.NetOption;
 import com.youxiachai.api.ApiCommon;
 import com.youxiachai.api.IApiMethod;
 import com.youxiachai.demo.model.bean.BookInfo;
 
 /**
  * @author youxiachai
  * @date   2013-7-17
  */
 public class BookApi implements IApiMethod<BookInfo>,Transformer{
 	
	interface Path {
 		String book = "/v2/book/";
 	}
 	
 	public String getBookById(Bundle query){
		int id = query.getInt("id");
 		return ApiCommon.getApiHost() + Path.book + id; 
 	}
 	
 	@Override
 	public <T> T transform(String url, Class<T> type, String encoding,
 			byte[] data, AjaxStatus status) {
 		// TODO Auto-generated method stub
 		return JSON.parseObject(new String(data), type);
 	}
 
 
 	
 	/*
 	 * 获取书籍详细信息
 	 */
 	@Override
 	public void get(Bundle queryMap, AQuery req, ICallback<BookInfo> callback) {
 		// TODO Auto-generated method stub
 		// 设置类
 		NetOption no = new NetOption(getBookById(queryMap), null);
 		// 缓存一个小时
 		no.expire = 60 * 60 * 1000;
 		NetCallback<BookInfo> book = new NetCallback<BookInfo>(BookInfo.class, no, callback);
 		
 		req.transformer(this).ajax(book);
 	}
 
 	@Override
 	public void post(Bundle queryMap, AQuery req, ICallback<BookInfo> callback) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
