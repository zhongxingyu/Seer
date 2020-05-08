 /*
  * Copyright 2012 Jeanfrancois Arcand
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.atmosphere.wasync.loader;
 
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;
 import org.atmosphere.wasync.ClientFactory;
 import org.atmosphere.wasync.Event;
 import org.atmosphere.wasync.Function;
 import org.atmosphere.wasync.Request;
 import org.atmosphere.wasync.Socket;
 import org.atmosphere.wasync.impl.AtmosphereClient;
 import org.atmosphere.wasync.impl.AtmosphereRequest;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * A utility class that can be used to load a WebSocket enabled Server
  *
  * @author jeanfrancois Arcand
  */
 public class Loader {
 
     private final static Logger logger = LoggerFactory.getLogger(Loader.class);
 
     public static void main(String[] s) throws InterruptedException, IOException {
 
         if (s.length == 0) {
            s = new String[]{"1", "1", "1", "http://127.0.0.1:8080/default/test"};
         }
 
         int run = Integer.valueOf(s[0]);
         final int clientNum = Integer.valueOf(s[1]);
         final int messageNum = Integer.valueOf(s[2]);
         String url = s[3];
 
         System.out.println("Stressing: " + url);
         System.out.println("Number of Client: " + clientNum);
         System.out.println("Number of Message: " + messageNum);
         System.out.println("Number of run: " + run);
         long count = 0;
 

        AsyncHttpClientConfig.Builder b = new AsyncHttpClientConfig.Builder();
        b.setFollowRedirects(true).setIdleConnectionTimeoutInMs(-1).setRequestTimeoutInMs(-1);

        NettyAsyncHttpProviderConfig nettyConfig = new NettyAsyncHttpProviderConfig();

        nettyConfig.addProperty("child.tcpNoDelay", "true");
        nettyConfig.addProperty("child.keepAlive", "true");
        final AsyncHttpClient c = new AsyncHttpClient(b.setAsyncHttpClientProviderConfig(nettyConfig).build());

         for (int r = 0; r < run; r++) {
 
             final CountDownLatch l = new CountDownLatch(clientNum);
             final CountDownLatch messages = new CountDownLatch(messageNum * clientNum);
             long clientCount = l.getCount();
             final AtomicLong total = new AtomicLong(0);
 
             Socket[] sockets = new Socket[clientNum];
             for (int i = 0; i < clientCount; i++) {
                 final AtomicLong start = new AtomicLong(0);
                 AtmosphereClient client = ClientFactory.getDefault().newClient(AtmosphereClient.class);
                 AtmosphereRequest.AtmosphereRequestBuilder request = client.newRequestBuilder();
                 request.method(Request.METHOD.GET).uri(url);
                request.transport(Request.TRANSPORT.WEBSOCKET);
                request.header("X-wakeUpNIO", "true");
                sockets[i] = client.create(client.newOptionsBuilder().runtimeShared(true).runtime(c).reconnect(true).build())
                         .on(Event.OPEN, new Function<String>() {
                             @Override
                             public void on(String statusCode) {
                                 start.set(System.currentTimeMillis());
                                 l.countDown();
                             }
                         }).on(new Function<String>() {
 
                             int mCount = 0;
 
                             @Override
                             public void on(String s) {
                                 logger.info(++mCount + "=>" + s);
                                 String[] m = s.split("-");
                                 for (String i : m) {
                                     messages.countDown();
                                 }
                             }
                         }).on(new Function<Throwable>() {
                             @Override
                             public void on(Throwable t) {
                                 t.printStackTrace();
                             }
                         });
 
                 sockets[i].open(request.build());
 
             }
 
             l.await(5, TimeUnit.SECONDS);
 
             // System.out.println("OK, all Connected: " + clientNum);
 
             Socket socket = sockets[0];
             for (int i = 0; i < messageNum; i++) {
                 socket.fire("message" + i);
             }
             messages.await(1, TimeUnit.HOURS);
             for (int i = 0; i < clientCount; i++) {
                 sockets[i].close();
             }
             count += (total.get() / clientCount);
             System.out.println("Run " + r + " => Total run : " + (total.get() / clientCount));
             System.gc();
         }
         System.out.println("=== Means " + (count/run) + "=====");
     }
 
 }
