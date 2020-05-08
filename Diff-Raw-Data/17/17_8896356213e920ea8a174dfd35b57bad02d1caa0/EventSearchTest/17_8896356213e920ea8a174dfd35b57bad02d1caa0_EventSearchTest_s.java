 package com.celements.calendar.search;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 import com.celements.search.lucene.LuceneSearchResult;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.web.Utils;
 
 public class EventSearchTest extends AbstractBridgedComponentTestCase {
   
   private EventSearch eventSearch;
   private XWiki xwiki;
 
   @Before
   public void setUp_EventSearchTest() throws Exception {
     xwiki = getWikiMock();
     eventSearch = (EventSearch) Utils.getComponent(IEventSearch.class);
   }
 
   @Test
   public void testGetSearchResult() throws Exception {
     List<String> sortFields = Arrays.asList("field1", "field2");
     IEventSearchQuery query = new DefaultEventSearchQuery("theDB", sortFields);
    expect(xwiki.getXWikiPreference(eq("search_skipChecks"), eq("search.skipChecks"), 
        eq("0"), same(getContext()))).andReturn("1").once();
 
     replayDefault();
     EventSearchResult searchResult = eventSearch.getSearchResult(query);
     LuceneSearchResult lSearchResult = searchResult.getSearchResult();
     verifyDefault();
 
     assertNotNull(searchResult);
     String expQueryString = "(wiki:(+\"theDB\") "
         + "AND object:(+\"Classes.CalendarEventClass\"))";
     assertEquals(expQueryString, lSearchResult.getQueryString());
     assertEquals(sortFields, lSearchResult.getSortFields());
   }
 
   @Test
   public void testGetSearchResult_null() throws Exception {
    expect(xwiki.getXWikiPreference(eq("search_skipChecks"), eq("search.skipChecks"), 
        eq("0"), same(getContext()))).andReturn("0").once();
     
     replayDefault();
     EventSearchResult searchResult = eventSearch.getSearchResult(null);
     LuceneSearchResult lSearchResult = searchResult.getSearchResult();
     verifyDefault();
 
     assertNotNull(searchResult);
     String expQueryString = "(wiki:(+\"xwikidb\") "
         + "AND object:(+\"Classes.CalendarEventClass\"))";
     assertEquals(expQueryString, lSearchResult.getQueryString());
     assertEquals(0, lSearchResult.getSortFields().size());
   }
 
 }
