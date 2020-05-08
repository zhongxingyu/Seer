 package kr.swmaestro.hsb.data;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import redis.clients.jedis.Jedis;
 
 /**
  * key-value 혹은 key-list 캐시
  * 여기서는 Redis를 사용 (Jedis)
  * 
  * 데이터 형태는 json을 사용합니다. (Gson)
  * 
  * @author 심영재
  */
 @Component
 public class KeyValueListCache {
 	
 	@Autowired
 	private Jedis jedis;
 	
 	private final static int COMMON_EXPIRE_SECOND = 60; // 테스트용 1분
 	//private final static int COMMON_EXPIRE_SECOND = 7 * 24 * 60 * 60; // 1주일 정도 캐시에 저장해둔다.
 	
 	private final static long MAX_LIST_SIZE = 100; // 테스트용 100개
 	//private final static long MAX_LIST_SIZE = 300; // 한 목록에 최대로 저장할 수 있는 갯수
 	
 	public void set(String key, Object object) {
 		
 		ObjectMapper om = new ObjectMapper();
 		try {
 			// 필요없는 property 제외
 			om.getSerializationConfig().addMixInAnnotations(object.getClass(), JsonIgnoreResultModelPropertyesMixIn.class);
 			
 			// 깔끔하게 캐시에 저장!!
 			//System.out.println(om.writeValueAsString(object));
 			// 저장하는 순간 expire 시간 재생성
 			jedis.setex(key, COMMON_EXPIRE_SECOND, om.writeValueAsString(object));
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	public Set<String> getSetByKey(String key){
 		return jedis.smembers(key);
 	}
 	
 	public void addSetElement(String key,String targetKey){
 		System.out.println("sadd:"+key+","+targetKey);
 		jedis.sadd(key, targetKey);
 		jedis.expire(key, COMMON_EXPIRE_SECOND);
 	}
 	
 	public void removeSetElement(String key,String targetKey){
 		System.out.println("srem:"+key+","+targetKey);
 		jedis.srem(key, targetKey);
 		jedis.expire(key,COMMON_EXPIRE_SECOND);
 	}
 	
 	public void addIndex(String key, Long score, String targetKey) {
 		// 읽어오는 순간 expire 시간 재생성
 		jedis.expire(key, COMMON_EXPIRE_SECOND);
 		jedis.zadd(key, score, targetKey);
 		long count = jedis.zcard(key);
 		if (count > MAX_LIST_SIZE) { // 갯수가 최대값보다 크면
 			// 마지막 값을 삭제
 			jedis.zremrangeByRank(targetKey, 0, 0);
 		}
 	}
 	
 	public Set<String> getIndexes(String key) {
 		
		if (!jedis.exists(key)) {
			return null;
		}
		
 		// 읽어오는 순간 expire 시간 재생성
 		jedis.expire(key, COMMON_EXPIRE_SECOND);
 		
 		//class java.util.LinkedHashSet 이기 때문에 순서대로 가져온다.
 		return jedis.smembers(key); 
 	}
 
 	public <T> List<T> list(String key, Long beforeScore, int count, Class<T> classOfT, Map<String, Integer> emptyValueIndexMap) {
 		
 		// 읽어오는 순간 expire 시간 재생성
 		jedis.expire(key, COMMON_EXPIRE_SECOND);
 		
 		//class java.util.LinkedHashSet 이기 때문에 순서대로 가져온다.
 		Set<String> keySet = jedis.zrangeByScore(key, "(" + Long.toString(beforeScore), "+inf", 0, count);
 		
 		return getCachedList(keySet,classOfT, emptyValueIndexMap);
 	}
 	
 	public <T> List<T> getCachedList(Set<String> keySet,Class<T> classOfT, Map<String, Integer> emptyValueIndexMap){
 		List<T> l = new ArrayList<>();
 		if (keySet.size() > 0) {
 			String[] keySets = keySet.toArray(new String[]{});
 			List<String> jsonList = jedis.mget(keySets);
 			
 			// 순서 반대로.
 			Collections.reverse(jsonList);
 			
 			ObjectMapper om = new ObjectMapper();
 			
 			int size = jsonList.size();
 			for (int i = 0 ; i < size ; i++) {
 				String json = jsonList.get(i);
 				
 				if (json != null) {
 					try {
 						l.add(om.readValue(json, classOfT));
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				} else {
 					// 비어있는 값인 경우 key와 index를 저장
 					emptyValueIndexMap.put(keySets[size - i - 1], i);
 					
 					l.add(null);
 				}
 				
 			}
 			
 		}
 		
 		return l;
 	}
 	
 	public <T> Object get(String key, Class<T> classOfT) {
 		if (key == null) {
 			return null;
 		}
 		ObjectMapper om = new ObjectMapper();
 		try {
 			// 읽어오는 순간 expire 시간 재생성
 			jedis.expire(key, COMMON_EXPIRE_SECOND);
 			return om.readValue(jedis.get(key), classOfT);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public void delete(String key) {
 		jedis.del(key);
 	}
 
 }
