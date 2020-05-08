 /*
  * ihome inc.
  * sox
  */
 package com.ihome.sox.session;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.ihome.sox.store.SessionStore;
 import com.ihome.sox.store.StoreType;
 import com.ihome.sox.util.SessionAttributeConfigParser;
 import com.ihome.sox.util.SoxConstants;
 
 /**
  * 默认的SocSessionManager的实现
  * @author sihai
  *
  */
 public class DefaultSessionManager implements SessionManager {
 
 	private final Log logger  = LogFactory.getLog(getClass());
 	
 	// session存储的设置
     private Map<StoreType, SessionStore> storeMap = new HashMap<StoreType, SessionStore>();
     
     // 属性值的配置
     private Map<String, SessionAttributeConfig> sessionAttributeConfigMap; //解析成对象的配置文件
     
 	// 当前管理的session
 	ThreadLocal<SoxSession> threadLocal = new ThreadLocal<SoxSession>();
 	
 	/**
 	 * 使用默认的<code>CONFIG_FILE_NAME</code>初始化
 	 */
 	public void init() {
 		init(SoxConstants.DEFAULT_CONFIG_FILE_NAME);
 	}
 	
 	/**
 	 * 使用指定的配置文件初始化, 配置文件位于classpath下
 	 * @param configFileName
 	 */
 	public void init(String configFileName) {
 		try {
 			Properties properties = new Properties();
 			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(configFileName));
 			init(properties);
 		} catch (IOException e) {
 			throw new IllegalArgumentException("SOC init failed", e);
 		}
 	}
 	
 	/**
 	 * 使用制定的Properties初始化
 	 * @param properties
 	 */
 	public void init(Properties properties) {
 		sessionAttributeConfigMap = SessionAttributeConfigParser.parse(properties);
 	}
 	
 	@Override
 	public void setSession(SoxSession session) {
 		threadLocal.set(session);
 	}
 
 	@Override
 	public SoxSession getSession() {
 		return threadLocal.get();
 	}
 
 	@Override
 	public void save() {
 		
 		logger.debug("start save session attribute!");
 		SoxSession session = getSession();
 		Map<String, Boolean> change = session.getChangedMarkMap();		
 		
 		for(Iterator<Entry<String, Boolean>> iterator = change.entrySet().iterator(); iterator.hasNext();) {
 			Entry<String, Boolean> e = iterator.next();
 			String key = e.getKey();
 			if(e.getValue().booleanValue()) {
 				//取得它的STORE, 保存它
 				SessionAttributeConfig config = sessionAttributeConfigMap.get(key);
 				
 				if (null == config) {
 					continue;
 				}
 				
 				//取得该KEY配置的STORE        此处需防止没有取到STORE
 		        StoreType type = config.getStoreType();
 		        SessionStore store = getStore(type);
 		        store.save(this.getSession().getHttpContext(), key);
 			}			
 		}		
 	}
 
 	@Override
 	public void invalidate() {
 		// 遍历配置，再转给保存的STORE处理
 		for (Iterator<Entry<String, SessionAttributeConfig>> iterator = sessionAttributeConfigMap.entrySet().iterator(); iterator.hasNext();) {
 			Entry<String, SessionAttributeConfig> e = iterator.next();
 			SessionAttributeConfig config = e.getValue();
			if (config.getLifeTime() >= 0 && null != config.getStoreType()) { // 说明需要处理
 				getStore(config.getStoreType()).invalidate(e.getKey());
 			}
 		}	
 	}
 
 	@Override
 	public SessionStore getSessionStore(StoreType type) {
 		return storeMap.get(type);
 	}
 	
 	@Override
 	public Object getAttribute(String key) {
 		//取得该key配置的store
         SessionAttributeConfig config = (SessionAttributeConfig)sessionAttributeConfigMap.get(key);
         StoreType storeType = config.getStoreType();
         SessionStore store = getStore(storeType);
 
         return store.getAttribute(key);
 	}
 
 	@Override
 	public boolean isExistKey(String key) {
 		if(sessionAttributeConfigMap.containsKey(key)) {
             return true;
         }
 
         return false;
 	}
 	
 	/**
 	 * 
 	 * @param storeKey
 	 * @return
 	 */
 	private SessionStore getStore(StoreType storeType) {
 		SessionStore store = this.getSession().getSessionStore(storeType);
 
         //如果当前环境上下文中没有STORE，则新建一个。
         if(null == store) {
             store = (SessionStore)SessionStoreFactory.newInstance(storeType);
             Map<String, Object> context = new HashMap<String, Object>();
             context.put(SessionStore.SESSION, this.getSession());
             context.put(SessionStore.CONFIG, sessionAttributeConfigMap);
             store.init(context);
             getSession().setSessionStore(storeType, store);
         }
 		return store;
 	}
 	
 	/**
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		DefaultSessionManager sm = new DefaultSessionManager();
 		sm.init();
 		System.out.println(sm);
 	}
 }
