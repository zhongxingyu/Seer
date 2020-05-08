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
 package org.komusubi.feeder.bind;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.net.URL;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 /**
  * @author jun.ozeki
  */
 public class BitlyUrlShorteningTest {
    private static final Logger logger = LoggerFactory.getLogger(BitlyUrlShorteningTest.class);
      
     private BitlyUrlShortening target = new BitlyUrlShortening();
 
     @Before
     public void before() {
        logger.info("--------------------------------------------------------------");
        logger.info("このテストは bit.ly web api を呼び出すため、Ignore 設定しています");
        logger.info("--------------------------------------------------------------");
         target = new BitlyUrlShortening();
     }
 
     @Ignore
     @Test
     public void shorten() throws Exception {
         URL url = target.shorten(new URL("https://www.jal.co.jp/cms/other/ja/weather_info_dom.html"));
         assertThat(url.toExternalForm(), is("http://bit.ly/1dtKP91"));
     }
     
     @Ignore
     @Test
     public void shortenedTwice() {
         URL url = target.shorten("http://bit.ly/1dtKP91");
         // does not change shorten url if has already shortend it.
         assertThat(url.toExternalForm(), is("http://bit.ly/1dtKP91"));
     }
 
 }
