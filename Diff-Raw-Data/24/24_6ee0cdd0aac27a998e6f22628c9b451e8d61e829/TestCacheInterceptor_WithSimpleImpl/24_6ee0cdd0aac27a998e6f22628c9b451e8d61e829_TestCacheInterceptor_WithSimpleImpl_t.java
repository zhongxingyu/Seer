 package cacher.aop.impl.simple;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import testframework.util.GuiceRunner;
 import testframework.util.GuiceRunner.UseModules;
 import cacher.aop.CacheInterceptor;
 import cacher.aop.FetcherMethod;
 import cacher.fetcher.FetchManager;
 import cacher.impl.InMemoryCache;
 
 import com.google.inject.Binder;
 import com.google.inject.Module;
 import com.google.inject.matcher.Matchers;
 
 @RunWith(GuiceRunner.class)
 @UseModules(TestCacheInterceptor_WithSimpleImpl.class)
 public class TestCacheInterceptor_WithSimpleImpl implements Module{
 
 	@Inject
 	private InMemoryCache cache;
 
 	@Inject
 	private TestHelper helper;
 
 	@Override
 	public void configure(Binder binder) {
 		InMemoryCache cnfConfig = new InMemoryCache();
 		binder.bind(InMemoryCache.class).toInstance(cnfConfig);
 
 		CacheInterceptor interceptor = new CacheInterceptor(new FetchManager(cnfConfig));
 		binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(FetcherMethod.class), interceptor);
 
 		binder.bind(TestHelper.class);
 	}
 
 	@Before
 	public void init(){
 		cache.clear();
 	}
 
 	@Test
 	public void testSingle(){
 		String key = "fakekey";
 
 		assertTrue(cache.isEmpty());
 
 		assertEquals(key, helper.fetchSingle(key));
 
 		assertTrue(cache.containsKey(key));
 	}
 
 	@Test
 	public void testMulti_SomeValuesAlreadyCached(){
 		String key1 = "key1";
 		String key2 = "key2";
 		String key3 = "key3";
 
 		assertTrue(cache.isEmpty());
 		cache.put(key2, "whatever");
 
 		Map<String, Object> map = helper.fetchMultiple(Arrays.asList(key2), Arrays.asList(key1, key2, key3), "someother value");
 
 		assertTrue(map.containsKey(key1));
 		assertTrue(map.containsKey(key2));
 		assertTrue(map.containsKey(key3));
 	}
 
 	public static class TestHelper{
 
 		@FetcherMethod
 		public String fetchSingle(@CacheKey String key){
 			return key;
 		}
 
 		@FetcherMethod(fetchBulk=true)
 		public Map<String, Object> fetchMultiple(List<String> shouldBeCachedKeys, @CacheKey List<String> keys, String anotherValue){
 			for(String cachedKey : shouldBeCachedKeys){
 				assertFalse(keys.contains(cachedKey));
 			}
 
 			Map<String, Object> map = new HashMap<String, Object>();
 			for(String key : keys){
 				map.put(key, new Object());
 			}
 			return map;
 		}
 
 	}
 
 }
