 package com.atlassian.refapp.ctk.sal;
 
 import com.atlassian.functest.junit.SpringAwareTestCase;
 
 import com.atlassian.refapp.ctk.AppSpecificInfoProvider;
 import com.atlassian.sal.api.search.SearchMatch;
 import com.atlassian.sal.api.search.SearchProvider;
 import com.atlassian.sal.api.search.SearchResults;
 
 import org.apache.commons.lang.StringUtils;
 import org.junit.Test;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertNotNull;
 
 public class SearchProviderTest extends SpringAwareTestCase
 {
     private SearchProvider searchProvider;
     private AppSpecificInfoProvider infoProvider;
 
     public void setSearchProvider(SearchProvider searchProvider)
     {
         this.searchProvider = searchProvider;
     }
 
     public void setInfoProvider(AppSpecificInfoProvider infoProvider)
     {
         this.infoProvider = infoProvider;
     }
 
     @Test
     public void testSearchProviderAvailable()
     {
         assertNotNull("SearchProvider must be available to plugins", searchProvider);
     }
 
     @Test
     public void testSearchButNoMatch()
     {
         final SearchResults sresults = searchProvider.search(infoProvider.getAdminUsername(),
                                                              "superweirdsearchtermwahaha");
 
         assertNotNull("Should never return null", sresults);
     }
 
     @Test
     public void testSearchWithMatch()
     {
         final String matchingSearchTerm = infoProvider.getMatchingSearchTerm();
         final boolean searchSupported = StringUtils.isNotBlank(matchingSearchTerm);
         final SearchResults sresults = searchProvider.search(infoProvider.getAdminUsername(),
                                        searchSupported ? matchingSearchTerm : "dummySearchTerm");
 
         assertNotNull("Should never return null", sresults);
 
         if (searchSupported)
         {
             assertTrue("We expect matches in search", sresults.getMatches().size() > 0);
 
             for (String content : infoProvider.getExpectedMatchingContents())
             {
                 boolean found = false;
                 for (SearchMatch match : sresults.getMatches())
                 {
                     // if any of these match, then we're good.
                    if (contains(match.getTitle(), content) ||
                        contains(match.getUrl(), content) ||
                        contains(match.getExcerpt(), content))
                     {
                         found = true;
                         break;
                     }
                 }
                 assertTrue("the expected content should exist in the search results:" + content, found);
             }
         }
     }

    private boolean contains(String result, String expectedContent)
    {
        if (result != null)
        {
            return result.contains(expectedContent);
        }

        return false;
    }
 }
