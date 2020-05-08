 /*
  * Copyright 2010 Lars Heuer (heuer[at]semagia.com). All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.isotopicmaps.sdsharetests.server;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import nu.xom.Attribute;
 import nu.xom.Document;
 import nu.xom.Element;
 import nu.xom.Nodes;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 import static org.junit.Assert.*;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Tests against the 
  * <a href="http://www.egovpt.org/fg/CWA_Part_1b#head-c41ad0664f1b6fb60343d7369e78ad90ea9b1bc3">fragments feed</a>.
  * 
  * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
  * @version $Rev:$ - $Date:$
  */
 @RunWith(Parameterized.class)
 public class TestFragmentsFeed extends AbstractServerTestCase {
 
     private static final Logger LOG = LoggerFactory.getLogger(TestFragmentsFeed.class);
 
     private final URI _uri;
 
     public TestFragmentsFeed(final URI uri) {
         _uri = uri;
     }
 
     @Parameters
     public static Collection<Object> makeTestCases() {
         final Collection<Object> result = new ArrayList<Object>();
         try {
             for (URI uri: Utils.fetchFragmentsFeedURIs()) {
                 result.add(new Object[] { uri });
             }
         }
         catch (Exception ex) {
             throw new RuntimeException(ex);
         }
         return result;
     }
 
     @Test
     public void testFragmentFeed() throws Exception {
         final Document feed = super.fetchAtomFeedAsDOM(_uri);
         final Nodes srcLocPrefixNodes = query(feed, "atom:feed/sd:ServerSrcLocatorPrefix");
         assertEquals(1, srcLocPrefixNodes.size());
         final Element srcLocPrefix = (Element) srcLocPrefixNodes.get(0);
         assertFalse("The ServerSrcLocatorPrefix must not be empty", srcLocPrefix.getValue().isEmpty());
        final Nodes entries = query(feed, "atom:feed/atom:entry[sd:TopicSI]");
         if (entries.size() == 0) {
             LOG.info("No fragment entries found in " + feed.getBaseURI());
             return;
         }
        final Nodes links = query(feed, "atom:feed/atom:entry[sd:TopicSI]/atom:link[@rel='" + REL_ALTERNATE + "']");
         if (links.size() == 0) {
             fail(feed.getBaseURI() + " provides no links to fragments");
         }
         for (int i=0; i<links.size(); i++) {
             Element link = (Element) links.get(i);
             Attribute attr = link.getAttribute("href");
             assertNotNull("No href attribute available", attr);
             final URI href = URI.create(feed.getBaseURI()).resolve(attr.getValue());
             attr = link.getAttribute("type");
             // TODO: Assume that the media type is required,
             // see <http://projects.topicmapslab.de/issues/3691>
             assertNotNull(link);
             super.testURIRetrieval(href, attr.getValue());
             super.testWithUnknownMediaType(href);
         }
     }
 }
