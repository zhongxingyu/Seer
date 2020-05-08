 /**
  * Copyright (C) 2012 uphy.jp
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package jp.uphy.dsptn.singleton;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 
 /**
  * @author Yuhi Ishikura
  */
 class Util {
 
   /**
    * 与えられたクラスのstaticメソッド"getInstance()"を取得し並列に複数回実行します。
    * 
    * @param singletonClass シングルトンクラス
    * @param times 回数
    */
   static void getInstanceParallelly(final Class<?> singletonClass, final int times) {
     final ExecutorService executor = Executors.newFixedThreadPool(times);
     final Runnable getInstanceTask = new Runnable() {
 
       @Override
       public void run() {
         try {
           final Method getInstanceMethod = singletonClass.getMethod("getInstance");
           getInstanceMethod.invoke(null);
         } catch (NoSuchMethodException e) {
           throw new RuntimeException(e);
         } catch (SecurityException e) {
           throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
           throw new RuntimeException(e);
         } catch (IllegalArgumentException e) {
           throw new RuntimeException(e);
         } catch (InvocationTargetException e) {
           throw new RuntimeException(e);
         }
       }
     };
 
     for (int i = 0; i < times; i++) {
       executor.execute(getInstanceTask);
     }
     executor.shutdown();
   }
 
 }
