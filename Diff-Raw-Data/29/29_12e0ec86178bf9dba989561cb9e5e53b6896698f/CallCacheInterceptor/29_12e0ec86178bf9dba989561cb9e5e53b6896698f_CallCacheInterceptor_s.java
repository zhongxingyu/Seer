 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.caching.interceptor;
 
 import java.io.Serializable;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheException;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 
 import org.aopalliance.intercept.MethodInvocation;
 import org.apache.commons.lang.SerializationUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.seasar.framework.aop.interceptors.AbstractInterceptor;
 
 /**
  * メソッドに対する呼び出しをキャッシュするInterceptor.
  * 
  * Daoに対して適用する場合、以下のように用いる.
  * <ul>
  *  <li>getやfindなどの取得系メソッドに CallCacheInterceptorをセットする
  *  <li>update,insert,delete,setなどの更新系メソッドに CallCachePurgeInterceptorをセットする
  *  <li>両Interceptorが同じキャッシュ領域を見に行くように コンストラクタの第二引数を等しいキー文字列にする
  *  <li>おなじく同一のCacheManagerを参照にいくように第一引数を等しいものにする
  * </ul>
  *
  * 特に以下の点に注意する必要がある
  * <ul>
  *  <li><b>インターセプト先のインスタンスはキャッシュ領域に対してsingletonでなくてはならない.</b>
  *  キャッシュに格納されているキーに、対象インスタンスを識別する機能がなく、メソッドのシグネチャと引数
  *  のセットのみをキーとしているため.
  *   <li>メソッドの引数は全てSerializableでない場合はキャッシュしない.
  *   <li>メソッドの戻り値型がSerializableでない場合はキャッシュしない.
  *   <li>例外がスローされた場合はキャッシュしない.
  * </ul> 
  *  
  * @author TANIGUCHI Hikaru
  */
 public class CallCacheInterceptor extends AbstractInterceptor {
     private static final Log logger = LogFactory.getLog(CallCacheInterceptor.class);
     
     private final Cache cache;
     private final CacheManager cacheManager;
     private final String cacheName;
     
     /**
      * constructor
      * 
      * @param cacheManager ehCacheのキャッシュマネージャ
      * @param cacheName キャッシュ名称(キャッシュマネージャに対するキー)
      * @throws CacheException
      */
     public CallCacheInterceptor(CacheManager cacheManager, String cacheName) throws CacheException {
         this.cacheManager = cacheManager;
         this.cacheName = cacheName;
         if (!cacheManager.cacheExists(cacheName)) {
             cacheManager.addCache(cacheName);
         }
         this.cache = cacheManager.getCache(cacheName);
     }
     
     public Object invoke(MethodInvocation invocation) throws Throwable {
         // 全ての引数がSerializableでないとキャッシュの問い合わせ・追加に意味はない
         if (!isAllArgumentsSerializable(invocation) || !isReturnTypeSerializable(invocation)) {
             return invocation.proceed();
         }
         
         // 引数は全てSerializableなので CallDescriptionが生成可能。キャッシュ問い合わせ
         CallDescription description = new CallDescription(invocation);
         Element element = cache.get(description);
         if (element != null) {
             Serializable originalResult = element.getValue();
             
             return SerializationUtils.clone(originalResult);
         } else {
             Object result = invocation.proceed(); // 例外発生時は上位へそのままスロー、キャッシュされない
             
             Element insertElement = new Element(description, (Serializable) result);
             cache.put(insertElement);
             
             return SerializationUtils.clone((Serializable)result);
         }
     }
 
     protected boolean isReturnTypeSerializable(MethodInvocation invocation) {
        return ((invocation.getMethod().getReturnType()) instanceof Serializable);
     }
 
     protected boolean isAllArgumentsSerializable(MethodInvocation invocation) {
         Object[] arguments = invocation.getArguments();
         for (int i=0; i<arguments.length; i++) {
             if (!(arguments[i] instanceof Serializable)) {
                 return false;
             }
         }
         return true;
     }
 }
