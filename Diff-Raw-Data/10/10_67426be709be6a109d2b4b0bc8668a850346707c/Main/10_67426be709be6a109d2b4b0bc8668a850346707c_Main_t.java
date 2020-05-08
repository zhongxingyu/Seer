 /*
  * Copyright 2011 Yusuke Yamamoto
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.todoke.countstream;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import twitter4j.FilterQuery;
 import twitter4j.TwitterStream;
 import twitter4j.TwitterStreamFactory;
 
 import java.io.File;
 
 /**
  * @author Yusuke Yamamoto - yusuke at mac.com
  */
 public class Main {
     static Logger logger = LoggerFactory.getLogger(Main.class);
 
     public static void main(String[] args) {
        if (args.length != 1) {
             System.out.println("usage: java org.todoke.hashcount.Main [comma separated terms to track]");
             System.exit(-1);
         }
         logger.info("terms to track: " + args[0]);
         String[] terms = args[0].split(",");
         FilterQuery query = new FilterQuery().track(terms);
         TwitterStream stream = new TwitterStreamFactory().getInstance();
         StringBuffer path = new StringBuffer(args[0].length());
        for (String term : terms) {
            if (0 != path.length()) {
                 path.append("-");
             }
            path.append(term.replaceAll("#", ""));
         }
         Callback callback = new Callback(new File(path.toString()));
         Counter counter = new Counter(callback);
 
         stream.addListener(counter);
         stream.filter(query);
     }
 }
