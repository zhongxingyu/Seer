 package com.geeky.springmvc.repository;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.redis.core.RedisTemplate;
 import org.springframework.stereotype.Repository;
 
 import com.geeky.springmvc.domain.PostData;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 @Repository
 public class PostDataRepository {
 	
 	@Autowired
 	public RedisTemplate<String, String> redisTemplate;
 
 	public RedisTemplate<String, String> getRedisTemplate() {
 		return redisTemplate;
 	}
 
 	public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
 		this.redisTemplate = redisTemplate;
 	}
 	
 	public void add(PostData entry){
 		Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
 		String toJson = gson.toJson(entry);
 		redisTemplate.opsForList().leftPush(entry.getUserID(), toJson);
 	}
 	
 	public List<PostData> getAll(String userID) {
 		List<PostData> listData = new ArrayList<PostData>();
		for (Object postData: redisTemplate.opsForList().range(userID, 0, 20)) {
 			Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
 			PostData out = gson.fromJson((String)postData, PostData.class);
 			listData.add(out);
 		}
 		return listData;
 	}
 
 	public void delete(String userId, int index) {
 		redisTemplate.opsForList().remove(userId, index, 1);
 	}
 	
 }
