 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package org.komusubi.feeder.sns.twitter;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.komusubi.feeder.model.Message;
 import org.komusubi.feeder.model.Message.Script;
 import org.komusubi.feeder.model.Page;
 import org.komusubi.feeder.model.ScriptLine;
 import org.komusubi.feeder.model.Topic;
 import org.komusubi.feeder.model.Topics;
 import org.komusubi.feeder.sns.History;
 import org.komusubi.feeder.sns.SocialNetwork;
 
 import twitter4j.Paging;
 import twitter4j.ResponseList;
 import twitter4j.Status;
 import twitter4j.StatusUpdate;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 
 /**
  * @author jun.ozeki
  */
 public class Twitter4j implements SocialNetwork {
     
     private Twitter twitter;
     private static final int MESSAGE_MAX_LENGTH = 140;
     
     /**
      * create new instance. 
      */
     public Twitter4j() {
         this(TwitterFactory.getSingleton());
     }
 
     /**
      * create new instance for unit test
      * @param twitter
      */
     Twitter4j(Twitter twitter) {
         this.twitter = twitter;
     }
     
     /**
      * create new message instance.
      * @return
      */
     @Override
     public Message newMessage() {
         return new TweetMessage();
     }
     
     /**
      * chunk message.
      * @param message
      * @return
      */
     List<StatusUpdate> chunkStatus(Message message) {
         StringBuilder builder = new StringBuilder();
         List<StatusUpdate> chunks = new ArrayList<StatusUpdate>();
         for (Script script: message) {
             // a script over tweet message max length.
             // TODO a script is over MESSAGE_MAX_LENGTH that must edit message.
             if (script.codePointCount() >= MESSAGE_MAX_LENGTH) {
 //                chunks.add(new StatusUpdate(script.codePointSubstring(0, MESSAGE_MAX_LENGTH)));
 //            } else {
                 
             }
             if (builder.codePointCount(0, builder.length()) + script.codePointCount() >= MESSAGE_MAX_LENGTH) {
                 chunks.add(new StatusUpdate(builder.toString()));
                 builder.delete(0, builder.length());
             } 
             builder.append(script.line());
         }
         if (builder.length() > 0)
             chunks.add(new StatusUpdate(builder.toString()));
         return chunks;
     }
 
     /**
      * @see org.komusubi.feeder.sns.SocialNetwork#post(org.komusubi.feeder.model.Message)
      */
     @Override
     public void post(Message message) {
         tweet(message);
     }
     
     /**
      * @see org.komusubi.feeder.sns.SocialNetwork#post(Topic topic)
      */
     @Override
     public void post(Topic topic) {
         tweet(topic.message());
     }
 
     @Override
    public void post(Topics topics) {
         tweet(topics.message());
     }
 
     /**
      * tweet.
      * @param message
      */
     public void tweet(Message message) {
        try {
            for (StatusUpdate status: chunkStatus(message)) {
                Status result = twitter.updateStatus(status);
                Thread.sleep(1000); // wait 1 second.
            }
        } catch (TwitterException e) {
            throw new Twitter4jException(e);
        } catch (InterruptedException e) {
            throw new Twitter4jException(e);
        }
     }
 
     /**
      * @see org.komusubi.feeder.sns.SocialNetwork#history()
      */
     @Override
     public History<Message> history() {
         return new History<Message>() {
 
             @Override
             public boolean exists(Script script) {
                 throw new UnsupportedOperationException("not implemented.");
 //                return false;
             }
 
             @Override
             public Page<Message> page(int number) {
                 HistoryPage page = new HistoryPage();
                 try {
 //                    ResponseList<Status> list = twitter.getHomeTimeline(new Paging(number));
                     ResponseHistory<Status> list = (ResponseHistory<Status>) twitter.getHomeTimeline(new Paging(number));
                     for (Status status: list) {
                         Message message = new TweetMessage();
                         message.append(status.getText());
                         page.add(message);
                     }
                 } catch (TwitterException e) {
                     throw new Twitter4jException(e);
                 }
                 return null;
             }
             
         };
     }
     
     /**
      * 
      * @author jun.ozeki
      * @param <T>
      */
     public interface ResponseHistory<T> extends ResponseList<T> {
         
     }
     
     
     /**
      * 
      * @author jun.ozeki
      */
     public static class HistoryPage extends Page<Message> {
 
         private ResponseList<Status> response;
         
         public HistoryPage() {
             super();
         }
 
         public HistoryPage(Collection<? extends Message> c) {
             super(c);
         }
 
         public HistoryPage(int initialCapacity) {
             super(initialCapacity);
         }
        
         
         
     }
     
     public static class ScriptStatus implements Script {
         
         private Status status;
 
         /**
          * create new instance.
          * @param line
          */
         public ScriptStatus(Status status) {
             this.status = status;
         }
         
         /**
          * @see org.komusubi.feeder.model.Message.Script#line()
          */
         @Override
         public String line() {
             return status.getText();
         }
 
         /**
          * @see org.komusubi.feeder.model.Message.Script#codePointCount()
          */
         @Override
         public int codePointCount() {
             if (line() == null)
                 return 0;
             return line().codePointCount(0, line().length());
         }
 
         /**
          * @see org.komusubi.feeder.model.Message.Script#codePointSubstring(int, int)
          */
         @Override
         public String codePointSubstring(int begin, int end) {
             // FIXME code point substring
             if (line() == null)
                 return "";
             return line().substring(begin, end);
         }
 
         /**
          * @see org.komusubi.feeder.model.Message.Script#codePointSubstring(int)
          */
         @Override
         public String codePointSubstring(int begin) {
             return codePointSubstring(begin, line().length());
         }
         
     }
     /**
      * 
      * @author jun.ozeki
      */
     public static class ResponseListMessage extends ArrayList<Script> implements Message {
 
         private static final long serialVersionUID = 1L;
         private ResponseList<Status> responses;
 
         /**
          * create new instance.
          * @param resopnses
          */
         public ResponseListMessage(ResponseList<Status> resopnses) {
             this.responses = resopnses;
         }
 
         /**
          * @see org.komusubi.feeder.model.Message#text()
          */
         @Override
         public String text() {
 //            StringBuilder builder = new StringBuilder();
 //            builder.append(responses.)
             throw new UnsupportedOperationException("not implemented.");
         }
 
         /**
          * @see org.komusubi.feeder.model.Message#append(org.komusubi.feeder.model.Message.Script)
          */
         @Override
         public Message append(Script script) {
             add(script);
             return this;
         }
 
         /**
          * @see org.komusubi.feeder.model.Message#append(java.lang.String)
          */
         @Override
         public Message append(String line) {
             return append(new ScriptLine(line));
         }
         
     }
 }
