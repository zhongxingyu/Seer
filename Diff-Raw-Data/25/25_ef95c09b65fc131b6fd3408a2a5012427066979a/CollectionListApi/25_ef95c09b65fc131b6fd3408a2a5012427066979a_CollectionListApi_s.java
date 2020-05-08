 package com.youxiachai.demo.model.api;
 
 import java.util.List;
 
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
 import com.youxiachai.demo.model.bean.Collection;
 
 /**
  * @author youxiachai
  * @date 2013-7-16
  */
 public class CollectionListApi implements IApiMethod<CollectionListApi> ,Transformer{
 	public int count;
 	public int start;
 	public int total;
 	public List<Collection> collections;
 	
 	public interface Path {
 		String START = "start";
 		String collection = ApiCommon.getApiHost() + "/v2/book/user/";
 	}
 	
 	public String getCollectionByUser(Bundle query){
 		
		return  Path.collection + query.getString("id") + "/collections";
 	}
 	
 	public String getCollectionStart(Bundle query){
		return  Path.collection + query.getString("id") + "/collections?start=" + query.getInt("start");
 	}
 
 	@Override
 	public void get(Bundle queryMap, AQuery req,
 			ICallback<CollectionListApi> callback) {
 		
 		int start = queryMap.getInt(Path.START, -1);
 		NetOption no;
 		if(start != -1){
 			no = new NetOption(getCollectionStart(queryMap), null);
 		}else {
 			no = new NetOption(getCollectionByUser(queryMap), null);
 		}
 		
 		
 		// 缓存一个小时
 		no.expire = 60 * 60 * 1000;
 		NetCallback<CollectionListApi> book = new NetCallback<CollectionListApi>(CollectionListApi.class, no, callback);
 		
 		req.transformer(this).ajax(book);
 	}
 
 	@Override
 	public void post(Bundle queryMap, AQuery req,
 			ICallback<CollectionListApi> callback) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public <T> T transform(String url, Class<T> type, String encoding,
 			byte[] data, AjaxStatus status) {
 		// TODO Auto-generated method stub
 		return JSON.parseObject(new String(data), type);
 	}
 
 }
