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
 
 import org.barbon.mangaget.data.DB;
 
 import org.barbon.mangaget.tests.UiUtils;
 import org.barbon.mangaget.tests.Utils;
 
 // TODO test paging
 
 public class SearchTest extends ActivityInstrumentationTestCase2<MangaSearch> {
     private MangaSearch activity;
 
     public SearchTest() {
         super("org.barbon.mangaget", MangaSearch.class);
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
 
         setActivityInitialTouchMode(false);
     }
 
     public void testSimpleSearch() {
         setActivityIntent(searchIntent(""));
        activity = (MangaSearch) getActivity();
        ListView resultList = activity.getListView();
 
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
        activity = (MangaSearch) getActivity();
        ListView resultList = activity.getListView();
 
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
