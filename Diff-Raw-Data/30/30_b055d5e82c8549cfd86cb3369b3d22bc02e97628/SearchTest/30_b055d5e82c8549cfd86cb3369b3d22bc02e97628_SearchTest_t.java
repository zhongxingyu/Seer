 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.mangaget;
 
 import android.app.Activity;
 import android.app.Instrumentation;
 import android.app.SearchManager;
 
 import android.content.Context;
 import android.content.Intent;
 
 import android.database.Cursor;
 
 import android.test.ActivityInstrumentationTestCase2;
 
 import android.widget.ListView;
 
import android.support.v4.app.FragmentManager;

 import org.barbon.mangaget.data.DB;
 
import org.barbon.mangaget.fragments.MangaSearchResults;

 import org.barbon.mangaget.tests.UiUtils;
 import org.barbon.mangaget.tests.Utils;
 
 // TODO test paging
 
 public class SearchTest extends ActivityInstrumentationTestCase2<MangaSearch> {
     private MangaSearch activity;
    private MangaSearchResults results;
    private FragmentManager manager;
 
     public SearchTest() {
         super("org.barbon.mangaget", MangaSearch.class);
     }
 
    private void refreshMembers() {
        activity = (MangaSearch) getActivity();
        manager = activity.getSupportFragmentManager();

        results = (MangaSearchResults)
            manager.findFragmentById(R.id.search_results);
    }

     public Intent searchIntent(String query) {
         Context targetContext = getInstrumentation().getTargetContext();
         Intent intent = new Intent(targetContext, MangaSearch.class);
 
         intent.setAction(Intent.ACTION_SEARCH);
         intent.putExtra(SearchManager.QUERY, query);
 
         return intent;
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
         UiUtils.setInstrumentation(getInstrumentation());
         Utils.setupTestAnimeaEnvironment(this);
        Utils.setupTestAnimeaDatabase(this);
 
         setActivityInitialTouchMode(false);
     }
 
     public void testSimpleSearch() {
         setActivityIntent(searchIntent(""));
        refreshMembers();
        ListView resultList = results.getListView();
 
         while (resultList.getCount() == 0)
             UiUtils.sleep(500);
 
         assertEquals(49, resultList.getCount());
     }
 
     private int mangaCount() {
         DB db = DB.getInstance(null);
         Cursor manga = db.getMangaList();
         int count = manga.getCount();
 
         manga.close();
 
         return count;
     }
 
     public void testMangaAdd() {
         Instrumentation instr = getInstrumentation();
 
         setActivityIntent(searchIntent(""));
        setActivity(UiUtils.forceHorizontal(getActivity()));
        refreshMembers();
        ListView resultList = results.getListView();
 
         while (resultList.getCount() == 0)
             UiUtils.sleep(500);
 
         assertEquals(49, resultList.getCount());
 
         UiUtils.selectListAndMoveToTop(resultList);
         UiUtils.moveDown();
         UiUtils.selectCurrent();
 
         int prev_count = mangaCount();
 
         instr.invokeContextMenuAction(activity, R.id.add_manga, 0);
 
         while (mangaCount() == prev_count)
             UiUtils.sleep(500);
 
         assertEquals(prev_count + 1, mangaCount());
     }
 }
