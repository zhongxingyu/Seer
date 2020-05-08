 package com.fbudassi.neddy.benchmark.benchmarks;
 
 import com.fbudassi.neddy.benchmark.NeddyBenchmark;
 import com.fbudassi.neddy.benchmark.bean.SpeakerActionBean;
 import com.fbudassi.neddy.benchmark.config.Config;
 import com.fbudassi.neddy.benchmark.pipeline.DefaultHttpPipelineFactory;
 import com.google.gson.Gson;
 import de.svenjacobs.loremipsum.LoremIpsum;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
 import org.jboss.netty.handler.codec.http.HttpHeaders;
 import org.jboss.netty.handler.codec.http.HttpMethod;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpVersion;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Rest benchmark. Please go to the properties file for configurable parameters.
  *
  * @author fbudassi
  */
 public class RestBenchmark implements Benchmark {
 
     private static final Logger logger = LoggerFactory.getLogger(RestBenchmark.class);
     private List<String> categories;
     private Gson gson;
     private Random random;
     private LoremIpsum loremIpsum;
     // Configuration constants.
     private static final String USERAGENT = Config.getValue(Config.KEY_USERAGENT);
     private static final int NUMCATEGORIES = Config.getIntValue(Config.KEY_SPEAKER_NUMCATEGORIES);
     private static final int DELAYBETWEENMESSAGES = Config.getIntValue(Config.KEY_SPEAKER_DELAYBETWEENMESSAGES);
     private static final String RESOURCE_CATEGORY = Config.getValue(Config.KEY_RESOURCE_CATEGORY);
     // Server configuration constants.
     private static final int SERVER_PORT = Config.getIntValue(Config.KEY_REST_PORT);
     private static final String SERVER_ADDRESS = Config.getValue(Config.KEY_SERVER_ADDRESS);
 
     /**
      * Rest benchmark.
      */
     @Override
     public void execute() throws InterruptedException {
         logger.info("Generating {} categories. A message is going to be sent every {} ms.",
                 NUMCATEGORIES, DELAYBETWEENMESSAGES);
 
         // Instantiates some necessary classes.
         gson = new Gson();
         random = new Random();
         loremIpsum = new LoremIpsum();
 
         // First create the category list both locally and in Neddy.
         createCategories();
 
         // Then send a message every some milliseconds, and one message to all when the list fully looped.
         sendMessageLoop();
     }
 
     /**
      * Returns the REST benchmark pipeline.
      *
      * @return
      */
     @Override
     public ChannelPipelineFactory getPipeline() {
         return new DefaultHttpPipelineFactory();
     }
 
     /**
      * It creates the number of requested categories. A copy of every category
      * name is saved the "categories" list.
      */
     private void createCategories() {
         categories = new ArrayList<String>();
         for (int s = 0; s < NUMCATEGORIES; s++) {
             String categoryName = "Category " + s;
             categories.add(categoryName);
 
             // Category creation request payload.
             SpeakerActionBean requestBean = new SpeakerActionBean();
             requestBean.setCategory(categoryName);
             String jsonRequest = gson.toJson(requestBean);
 
             // Create the channel.
             ChannelFuture future = NeddyBenchmark.getBootstrap().connect(
                     new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT));
 
             // Wait until the connection attempt succeeds or fails.
             Channel channel = future.awaitUninterruptibly().getChannel();
             if (!future.isSuccess()) {
                 logger.error("Connection attempt not successful.", future.getCause());
                 return;
             }
 
             // Prepare the HTTP request.
             HttpRequest request = new DefaultHttpRequest(
                     HttpVersion.HTTP_1_1, HttpMethod.PUT, "/" + RESOURCE_CATEGORY);
             request.setHeader(HttpHeaders.Names.HOST, SERVER_ADDRESS);
             request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
             request.setHeader(HttpHeaders.Names.USER_AGENT, USERAGENT);
             request.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, jsonRequest.length());
            request.setContent(ChannelBuffers.copiedBuffer(jsonRequest, org.jboss.netty.util.CharsetUtil.UTF_8));
 
             // Send the HTTP request.
             channel.write(request);
         }
     }
 
     /**
      * Send a message to every category in the list every some milliseconds.
      */
     private void sendMessageLoop() throws InterruptedException {
         for (String category : categories) {
             // Message port request payload.
             SpeakerActionBean requestBean = new SpeakerActionBean();
             requestBean.setCategory(category);
             requestBean.setMessage(loremIpsum.getWords(random.nextInt(100) + 10));
             String jsonRequest = gson.toJson(requestBean);
 
             // Create the channel.
             ChannelFuture future = NeddyBenchmark.getBootstrap().connect(
                     new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT));
 
             // Wait until the connection attempt succeeds or fails.
             Channel channel = future.awaitUninterruptibly().getChannel();
             if (!future.isSuccess()) {
                 logger.error("Connection attempt not successful.", future.getCause());
                 return;
             }
 
             // Prepare the HTTP request.
             HttpRequest request = new DefaultHttpRequest(
                     HttpVersion.HTTP_1_1, HttpMethod.POST, "/" + RESOURCE_CATEGORY);
             request.setHeader(HttpHeaders.Names.HOST, SERVER_ADDRESS);
             request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
             request.setHeader(HttpHeaders.Names.USER_AGENT, USERAGENT);
             request.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, jsonRequest.length());
             request.setContent(ChannelBuffers.copiedBuffer(jsonRequest, org.jboss.netty.util.CharsetUtil.UTF_8));
 
             // Send the HTTP request.
             channel.write(request);
 
             // Introduce a delay between messages creation.
             Thread.sleep(DELAYBETWEENMESSAGES);
         }
     }
 }
