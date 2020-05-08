 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.komusubi.feeder.aggregator.scraper;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertThat;
 
 import org.htmlparser.Node;
 import org.htmlparser.Tag;
 import org.htmlparser.Text;
 import org.htmlparser.filters.AndFilter;
 import org.htmlparser.filters.HasAttributeFilter;
 import org.htmlparser.filters.NodeClassFilter;
 import org.htmlparser.nodes.TextNode;
 import org.htmlparser.tags.Div;
 import org.htmlparser.tags.ParagraphTag;
 import org.htmlparser.tags.TableColumn;
 import org.htmlparser.tags.TableHeader;
 import org.htmlparser.tags.TableTag;
 import org.htmlparser.util.NodeIterator;
 import org.htmlparser.util.NodeList;
 import org.htmlparser.visitors.NodeVisitor;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
 import org.junit.runner.RunWith;
 import org.komusubi.feeder.aggregator.ExternalFileResource;
 
 /**
  * @author jun.ozeki
  */
 @RunWith(Enclosed.class)
 public class HtmlScraperTest {
 
     /**
      * scrape HtmlScraperTestスクレイプ処理呼び出し.html.
      * @author jun.ozeki
      */
     public static class スクレイプ処理呼び出し {
         @Rule public ExternalFileResource fileResource = new ExternalFileResource(HtmlScraperTest.class,
                         "HtmlScraperTestスクレイプ処理呼び出し.html");
 
         private HtmlScraper target;
 
         @Before
         public void before() {
             target = new HtmlScraper();
         }
 
         @Test
         public void スクレイプ処理実施() throws Exception {
             // setup
             String resources = fileResource.getResource();
             String expected = "in html string value.";
             // exercise
             NodeList nodes = target.scrape(resources, null);
             // verify
             assertThat(nodes.asString().trim(), is(expected));
         }
 
     }
 
     /**
      * scrape weather_info_dom.html.
      * @author jun.ozeki
      */
     public static class 天気状況取得 {
         @Rule public ExternalFileResource fileResource = new ExternalFileResource(HtmlScraperTest.class,
                         "weather_info_dom.html", "Shift_JIS");
 
         private HtmlScraper target;
 
         @Before
         public void before() {
             target = new HtmlScraper();
         }
 
         @Test
         public void スクレイプ結果がNULLではない事() throws Exception {
             // setup
             String resources = fileResource.getResource();
             AndFilter filter = new AndFilter(
                             new NodeClassFilter(Div.class),
                             new HasAttributeFilter("class", "weather_info_txtBox"));
             // exercise
             NodeList nodes = target.scrape(resources, filter);
             // verify
             assertThat(nodes, is(not(nullValue())));
         }
 
         @Test
         public void 天気状況ヘッダー取得処理() {
             // setup
             String resources = fileResource.getResource();
             AndFilter filter = new AndFilter(
                             new NodeClassFilter(Div.class),
                             new HasAttributeFilter("class", "weather_info_txtBox"));
             // exercise
             NodeList nodes = target.scrapeMatchNodes(resources, filter, TableHeader.class);
             // verify
             assertThat(nodes, is(not(nullValue())));
             assertThat(nodes.size(), is(5));
             assertThat(nodes.elementAt(0).toPlainTextString(), is("北海道"));
             assertThat(nodes.elementAt(1).toPlainTextString(), is("東北・北陸"));
             assertThat(nodes.elementAt(2).toPlainTextString(), is("関東・東海・近畿"));
             assertThat(nodes.elementAt(3).toPlainTextString(), is("中国・四国"));
             assertThat(nodes.elementAt(4).toPlainTextString(), is("九州・沖縄"));
         }
 
         @Test
         public void 天気状況ステータス取得処理() {
             // setup
             String resources = fileResource.getResource();
             AndFilter filter = new AndFilter(
                             new NodeClassFilter(Div.class),
                             new HasAttributeFilter("class", "weather_info_txtBox"));
             String expected1 = "【本日1日】平常どおりの運航を予定しています。\n【明日2日】函館（降雪）、札幌千歳・とかち帯広・釧路（夕刻からの降雪）";
             String expected2 = "【本日1日】青森・秋田・新潟（降雪）\n【明日2日】青森・三沢・秋田・新潟（降雪）、いわて花巻・小松（強風）";
             String expected3 = "平常どおりの運航を予定しています。";
             String expected4 = "平常どおりの運航を予定しています。";
             String expected5 = "平常どおりの運航を予定しています。";
             // exercise
             NodeList nodes = target.scrapeMatchNodes(resources, filter, TableColumn.class);
             // verify
             assertThat(nodes, is(not(nullValue())));
             assertThat(nodes.size(), is(5));
             assertThat(nodes.elementAt(0).toPlainTextString(), is(expected1));
             assertThat(nodes.elementAt(1).toPlainTextString(), is(expected2));
             assertThat(nodes.elementAt(2).toPlainTextString(), is(expected3));
             assertThat(nodes.elementAt(3).toPlainTextString(), is(expected4));
             assertThat(nodes.elementAt(4).toPlainTextString(), is(expected5));
         }
         
         @Test
         public void アナウンス日時取得() {
             // setup
             String resources = fileResource.getResource();
             AndFilter filter = new AndFilter(
                             new NodeClassFilter(ParagraphTag.class), 
                             new HasAttributeFilter("class", "mgt10"));
             String expected = "この情報は、日本時間2013年1月1日（火）の日本出発便・到着便に関する20時20分現在のものです。";
             // exercise
             NodeList nodes = target.scrape(resources, filter);
             // verify
             assertThat(nodes, is(not(nullValue())));
             assertThat(nodes.size(), is(1));
             assertThat(nodes.asString(), is(expected));
         }
 
         @Ignore
         @Test
         public void 概況タイトル取得() {
              // setup
             String resources = fileResource.getResource();
             AndFilter filter = new AndFilter(
                             new NodeClassFilter(Div.class), 
                             new HasAttributeFilter("class", "weather_info_txtBox"));
             String expected = "";
             // exercise
             NodeList nodes = target.scrapeMatchNodes(resources, filter, ParagraphTag.class);
             // verify
             assertThat(nodes, is(not(nullValue())));
             assertThat(nodes.size(), is(1));
             assertThat(nodes.asString(), is(expected));
         }
     }
 
     public static class ScrapeNoonNormal {
         @Rule public ExternalFileResource fileResource = new ExternalFileResource(HtmlScraperTest.class,
                         "weather_info_dom_noon_normal.html", "Shift_JIS");
 
         private HtmlScraper target;
 
         @Before
         public void before() {
             target = new HtmlScraper();
         }
 
         @Test
         public void スクレイプ処理結果がNULLではない事() {
             // setup
             String resources = fileResource.getResource();
             AndFilter filter = new AndFilter(
                             new NodeClassFilter(Div.class),
                            new HasAttributeFilter(AbstractWeatherScraper.ATTR_NAME_CLASS, AbstractWeatherScraper.ATTR_VALUE_WEATHER_BOX));
             // exercise
             NodeList nodes = target.scrape(resources, filter);
             // verify
             assertThat(nodes, is(not(nullValue())));
         }
 
         @Test
         public void 天気状況ヘッダー取得処理() {
             // setup
             String resources = fileResource.getResource();
             AndFilter filter = new AndFilter(
                             new NodeClassFilter(Div.class),
                            new HasAttributeFilter(AbstractWeatherScraper.ATTR_NAME_CLASS, AbstractWeatherScraper.ATTR_VALUE_WEATHER_BOX));
             // exercise
             NodeList nodes = target.scrapeMatchNodes(resources, filter, TableHeader.class);
             // verify
             assertThat(nodes, is(not(nullValue())));
             assertThat(nodes.size(), is(5));
             assertThat(nodes.elementAt(0).toPlainTextString(), is("北海道"));
             assertThat(nodes.elementAt(1).toPlainTextString(), is("東北・北陸"));
             assertThat(nodes.elementAt(2).toPlainTextString(), is("関東・東海・近畿"));
             assertThat(nodes.elementAt(3).toPlainTextString(), is("中国・四国"));
             assertThat(nodes.elementAt(4).toPlainTextString(), is("九州・沖縄"));
         }
 
         @Test
         public void 天気状況ステータス取得処理() {
             // setup
             String resources = fileResource.getResource();
             AndFilter filter = new AndFilter(
                             new NodeClassFilter(Div.class),
                            new HasAttributeFilter(AbstractWeatherScraper.ATTR_NAME_CLASS, AbstractWeatherScraper.ATTR_VALUE_WEATHER_BOX));
             String expected1 = "平常どおりの運航を予定しています。";
             String expected2 = "平常どおりの運航を予定しています。";
             String expected3 = "平常どおりの運航を予定しています。";
             String expected4 = "平常どおりの運航を予定しています。";
             String expected5 = "平常どおりの運航を予定しています。";
             // exercise
             NodeList nodes = target.scrapeMatchNodes(resources, filter, TableColumn.class);
             // verify
             assertThat(nodes, is(not(nullValue())));
             assertThat(nodes.size(), is(5));
             assertThat(nodes.elementAt(0).toPlainTextString(), is(expected1));
             assertThat(nodes.elementAt(1).toPlainTextString(), is(expected2));
             assertThat(nodes.elementAt(2).toPlainTextString(), is(expected3));
             assertThat(nodes.elementAt(3).toPlainTextString(), is(expected4));
             assertThat(nodes.elementAt(4).toPlainTextString(), is(expected5));
         }
 
         @Test
         public void アナウンス日時取得() {
             // setup
             String resources = fileResource.getResource();
             AndFilter filter = new AndFilter(
                             new NodeClassFilter(ParagraphTag.class), 
                             new HasAttributeFilter("class", "mgt10"));
             String expected = "この情報は、日本時間2013年1月5日（土）の日本出発便・到着便に関する10時00分現在のものです。";
             // exercise
             NodeList nodes = target.scrape(resources, filter);
             // verify
             assertThat(nodes, is(not(nullValue())));
             assertThat(nodes.size(), is(1));
             assertThat(nodes.asString(), is(expected));
         }
         
         @Ignore
         @Test
         public void 概況タイトル取得() throws Exception {
              // setup
             String resources = fileResource.getResource();
             AndFilter filter = new AndFilter(
                             new NodeClassFilter(Div.class), 
                            new HasAttributeFilter("class", "weather_info_txtBox mgt20"));
             String expected = "";
             // exercise
             NodeList nodes = target.scrape(resources, filter);
 //            NodeList matchNode = nodes.extractAllNodesThatMatch(new NodeClassFilter(ParagraphTag.class), true);
             NodeList matchNode = nodes.extractAllNodesThatMatch(new NodeClassFilter(ParagraphTag.class), true);
             final NodeList nodeList = new NodeList();
             NodeVisitor visitor = new NodeVisitor() {
                 private boolean inTable;
                 
                 @Override
                 public void visitTag(Tag tag) {
                     if (tag instanceof TableTag) {
                         inTable = true;
                     } else if ("BR".equalsIgnoreCase(tag.getTagName()) && !inTable) {
 //                        nodeList.add(tag);
 //                        tag
                     }
                 }
                 
                 @Override
                 public void visitEndTag(Tag tag) {
                     if (tag instanceof TableTag) {
                         inTable = false;
 //                        System.out.println("tag: " + tag.getTagName());
                     } else {
 //                        System.out.println("end tag: " + tag);
                     }
                 }
                 
                 @Override
                 public void visitStringNode(Text text) {
                     if (!inTable && !"\n".equals(text.getText())) {
                         Text textNode;
                         if (text.getText().startsWith("\n")) {
                             String value = text.getText().substring("\n".length());
                             textNode = new TextNode(value);
                         } else {
                             textNode = text;
                         }
                         nodeList.add(textNode);
                     }
                 }
             };
             matchNode.visitAllNodesWith(visitor);
             for (NodeIterator it = nodeList.elements(); it.hasMoreNodes(); ) {
                 Node node = it.nextNode();
                 System.out.printf("node: %s\n", node);
             }
 //            System.out.printf("before node: %s\n", nodes.asString());
 //            nodes.keepAllNodesThatMatch(new OrFilter(new NodeClassFilter(Div.class), 
 //                                            new NodeClassFilter(TextNode.class)), true);
 //            System.out.printf("node: %s\n", matchNode.asString());
             System.out.flush();
 //            System.out.printf("match node: %s\nsize: %d\n", matchNode, matchNode.size());
 //            for (int i = 0; i < matchNode.size(); i++) {
 //                System.out.printf("node: %s\n", nodes.elementAt(i).toPlainTextString());
 //            }
             // verify
             assertThat(nodes, is(not(nullValue())));
             assertThat(nodes.size(), is(1));
             assertThat(nodes.asString(), is(expected));
         }
     }
 }
