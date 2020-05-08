 /*
 * Copyright 1999-2011 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
  */
 package com.yangqi.test;
 
 import com.meetup.memcached.MemcachedClient;
 import com.meetup.memcached.SockIOPool;
 
 /**
  * this code is written by yangqi
 * 
  * @author yangqi 2012-8-2 下午01:41:56
  */
 public class TestMemache {
 
     public static void main(String[] args) {
         /* 初始化SockIOPool，管理memcached的连接池 */
         String[] servers = { "10.16.90.4:11211" };
         SockIOPool pool = SockIOPool.getInstance();
         pool.setServers(servers);
         pool.setFailover(true);
         pool.setInitConn(10);
         pool.setMinConn(5);
         pool.setMaxConn(250);
         pool.setMaintSleep(30);
         pool.setNagle(false);
         pool.setSocketTO(3000);
         pool.setAliveCheck(true);
         pool.initialize();
         /* 建立MemcachedClient实例 */
         MemcachedClient memCachedClient = new MemcachedClient();
        long start = System.currentTimeMillis();
         for (int i = 0; i < 100; i++) {
             /* 将对象加入到memcached缓存 */

             boolean success = memCachedClient.set("" + i, "Hello!Hello!Hello!Hello!");
            long setEnd = System.currentTimeMillis();
             /* 从memcached缓存中按key值取对象 */
             String result = (String) memCachedClient.get("" + i);
            long getEnd = System.currentTimeMillis();

            System.out.println("SET USE " + (setEnd - start) + " ,GET USE " + (getEnd - setEnd));

         }
     }
 
 }
