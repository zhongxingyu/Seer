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
 package org.komusubi.feeder.aggregator.scraper;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 import org.htmlparser.tags.ParagraphTag;
 import org.htmlparser.util.NodeList;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
 import org.junit.runner.RunWith;
 import org.komusubi.feeder.aggregator.ExternalFileResource;
 import org.komusubi.feeder.aggregator.scraper.WeatherTitleScraper.WeatherTitleVisitor;
 
 /**
  * @author jun.ozeki
  */
 @RunWith(Enclosed.class)
 public class WeatherTitleScraperTest {
 
     /**
      * visit all node in weather_info_dom.html.
      * @author jun.ozeki
      */
     public static class WeatherInfoDomHtmlTest {
         @Rule public ExternalFileResource fileResource = new ExternalFileResource(WeatherTitleScraper.class,
                         "weather_info_dom.html", "Shift_JIS");
 
         private HtmlScraper scraper;
         
         @Before
         public void before() {
             scraper = new HtmlScraper();
         }
         
         @Test
         public void 運行概況タイトル取得() throws Exception {
             // setup
             String resources = fileResource.getResource();
             NodeList nodes = scraper.scrapeMatchNodes(resources, new WeatherTitleScraper().filter(), ParagraphTag.class); 
             NodeList actual = new NodeList();
             WeatherTitleVisitor target = new WeatherTitleVisitor(actual);
             String expected1 = "新年明けましておめでとうございます。";
             String expected2 = "2013年も、日本航空をご愛顧賜りますよう、どうぞ宜しくお願い申し上げます。";
             String expected3 = "≪北海道・東北地方　降雪による運航便情報について≫";
             String expected4 = "明日2日は、北海道・東北地方に降雪の予報がでており、各空港を発着する運航便への影響が懸念されております。";
             String expected5 = "なお、札幌千歳空港を夕刻以降に発着する運航便につきましては、降雪が強まる見込みのため影響が発生する可能性がございます。";
             String expected6 = "利用のお客さまは、お出かけ前に最新の運航状況を";
            String expected7 = "http://www.5971.jal.co.jp/rsv/ArrivalAndDepartureInput.do";
             String expected8 = "にてご確認ください。";
             String expected9 = "また、本日1日および明日2日の遅延、欠航、条件付運航（出発空港への引き返し、他空港への着陸）の可能性がある空港は、以下のとおりです。";
             // exercise
             nodes.visitAllNodesWith(target);
             // verify
             assertThat(actual, is(not(nullValue())));
             assertThat(actual.size(), is(9));
             assertThat(actual.elementAt(0).getText(), is(expected1));
             assertThat(actual.elementAt(1).getText(), is(expected2));
             assertThat(actual.elementAt(2).getText(), is(expected3));
             assertThat(actual.elementAt(3).getText(), is(expected4));
             assertThat(actual.elementAt(4).getText(), is(expected5));
             assertThat(actual.elementAt(5).getText(), is(expected6));
             assertThat(actual.elementAt(6).getText(), is(expected7));
             assertThat(actual.elementAt(7).getText(), is(expected8));
             assertThat(actual.elementAt(8).getText(), is(expected9));
         }
     }
     
     /**
      * visit in weather_info_dom_noon_normal.html.
      * @author jun.ozeki
      */
     public static class WeatherInfoDomNoonNormalHtmlTest {
         @Rule public ExternalFileResource fileResource = new ExternalFileResource(WeatherTitleScraper.class,
                         "weather_info_dom_noon_normal.html", "Shift_JIS");
 
         private HtmlScraper scraper;
         
         @Before
         public void before() {
             scraper = new HtmlScraper();
         }
         
         @Test
         public void 運行概況タイトル取得() throws Exception {
             // setup
             String resources = fileResource.getResource();
             NodeList nodes = scraper.scrapeMatchNodes(resources, new WeatherTitleScraper().filter(), ParagraphTag.class); 
             NodeList actual = new NodeList();
             WeatherTitleVisitor target = new WeatherTitleVisitor(actual);
             String expected1 = "【運航概況】";
             String expected2 = "本日5日の運航状況は、以下のとおりです。";
             // exercise
             nodes.visitAllNodesWith(target);
             // verify
             assertThat(actual, is(not(nullValue())));
             assertThat(actual.size(), is(2));
             assertThat(actual.elementAt(0).getText(), is(expected1));
             assertThat(actual.elementAt(1).getText(), is(expected2));
         }
     }
 }
