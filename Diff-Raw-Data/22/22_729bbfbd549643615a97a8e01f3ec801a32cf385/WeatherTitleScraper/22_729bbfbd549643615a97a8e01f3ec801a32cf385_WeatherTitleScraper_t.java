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
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.htmlparser.Node;
 import org.htmlparser.NodeFilter;
 import org.htmlparser.Tag;
 import org.htmlparser.Text;
 import org.htmlparser.filters.AndFilter;
 import org.htmlparser.filters.HasAttributeFilter;
 import org.htmlparser.filters.NodeClassFilter;
 import org.htmlparser.nodes.TextNode;
 import org.htmlparser.tags.Div;
import org.htmlparser.tags.LinkTag;
 import org.htmlparser.tags.ParagraphTag;
 import org.htmlparser.tags.TableTag;
 import org.htmlparser.util.NodeIterator;
 import org.htmlparser.util.NodeList;
 import org.htmlparser.util.ParserException;
 import org.htmlparser.visitors.NodeVisitor;
 import org.komusubi.feeder.aggregator.AggregatorException;
 import org.komusubi.feeder.aggregator.scraper.WeatherTitleScraper.Title;
 import org.komusubi.feeder.aggregator.site.WeatherTopicSite;
 import org.komusubi.feeder.model.Message.Script;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author jun.ozeki
  */
 public class WeatherTitleScraper extends AbstractWeatherScraper implements Iterable<Title> {
     /**
      * weather title.
      * @author jun.ozeki
      */
     public static class Title implements Script, Serializable {
 
         private static final long serialVersionUID = 1L;
         private String title;
 
         /**
          * create new instance.
          */
         public Title(String title) {
             this.title = title;
         }
 
         /**
          * @see org.komusubi.feeder.model.Message.Script#codePointLength()
          */
         @Override
         public int codePointCount() {
             if (title == null)
                 return 0;
             return title.codePointCount(0, title.length());
         }
 
         @Override
         public String codePointSubstring(int begin) {
             if (title == null)
                 return null;
             return codePointSubstring(begin, title.length());
         }
 
         @Override
         public String codePointSubstring(int begin, int end) {
             if (title == null)
                 return null;
             return title.substring(begin, end);
         }
 
         /**
          * @see org.komusubi.feeder.model.Message.Script#line()
          */
         @Override
         public String line() {
             return title;
         }
 
         @Override
         public String toString() {
             StringBuilder builder = new StringBuilder();
             builder.append("Title [title=").append(title).append("]");
             return builder.toString();
         }
 
     }
 
     /**
      * 
      * @author jun.ozeki
      */
     public static class WeatherTitleVisitor extends NodeVisitor {
         private NodeList visited;
         private boolean startTable;
        private boolean linkParseToggle = false;
 
         /**
          * create new instance.
          * default constructor.
          */
         public WeatherTitleVisitor(NodeList visited) {
             this.visited = visited;
         }
 
         @Override
         public void visitStringNode(Text text) {
            if (linkParseToggle) {
                linkParseToggle = !linkParseToggle;
                return;
            }
             if (!startTable && !"\n".equals(text.toPlainTextString())) {
                 Text textNode;
                 if (text.getText().startsWith("\n")) {
                     String value = text.getText().substring("\n".length());
                     textNode = new TextNode(value);
                 } else {
                     textNode = text;
                 }
                 visited.add(textNode);
             }
         }
 
         @Override
         public void visitTag(Tag tag) {
             if (tag instanceof TableTag)
                 startTable = true;
            if (!startTable && (tag instanceof LinkTag)) {
               LinkTag link = (LinkTag) tag;
               visited.add(new TextNode(link.getLink()));
               linkParseToggle = true;
            }
         }
     }
 
     private static final Logger logger = LoggerFactory.getLogger(WeatherTitleScraper.class);
 
     /**
      * create new instance.
      */
     public WeatherTitleScraper() {
 
     }
 
     /**
      * create new instance.
      * @param scraper
      */
     public WeatherTitleScraper(HtmlScraper scraper) {
         this(new WeatherTopicSite(), scraper);
     }
 
     /**
      * create new instance.
      * @param site
      */
     public WeatherTitleScraper(WeatherTopicSite site) {
         this(site, new HtmlScraper());
     }
 
     /**
      * create new instance.
      * @param site
      * @param scraper
      */
     @Inject
     public WeatherTitleScraper(WeatherTopicSite site, HtmlScraper scraper) {
         super(site, scraper);
     }
 
     /**
      * 
      * @return
      */
     protected NodeFilter filter() {
         return new AndFilter(
                         new NodeClassFilter(Div.class),
                         new HasAttributeFilter(ATTR_NAME_CLASS, ATTR_VALUE_WEATHER_BOX));
     }
 
     /**
      * @see java.lang.Iterable#iterator()
      */
     @Override
     public Iterator<Title> iterator() {
         return scrape(filter()).iterator();
     }
 
     /**
      * scrape title.
      * @return
      */
     public List<Title> scrape() {
         return scrape(filter());
     }
 
     /**
      * scrape title.
      * @param filter
      * @return
      */
     public List<Title> scrape(NodeFilter filter) {
         NodeList nodes = scraper().scrapeMatchNodes(site().url(), filter, ParagraphTag.class);
         NodeList visited = new NodeList();
         List<Title> titles = new ArrayList<Title>();
         try {
             nodes.visitAllNodesWith(new WeatherTitleVisitor(visited));
             for (NodeIterator it = visited.elements(); it.hasMoreNodes(); ) {
                 Node n = it.nextNode();
                if (n instanceof LinkTag) {
                   logger.debug("bingo!! : {}", n);
                   logger.debug("bingo!! : {}", n.getText());
                   logger.debug("bingo!! : {}", n.toHtml(true));
                   logger.debug("bingo!! : {}", n.toHtml(false));
                   logger.debug("bingo!! : {}", n.toPlainTextString());
                }
                 titles.add(new Title(n.getText()));
                 logger.debug("title: {}", n.getText());
             }
         } catch (ParserException e) {
             throw new AggregatorException(e);
         }
         return titles;
     }
 }
