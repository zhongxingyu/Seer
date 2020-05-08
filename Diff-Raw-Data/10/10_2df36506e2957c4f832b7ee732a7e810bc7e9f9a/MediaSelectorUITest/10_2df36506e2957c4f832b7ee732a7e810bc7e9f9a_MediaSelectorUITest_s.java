 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
 
     (C) Copyright 2012: Daniel Kvist, Henrik Hugo, Gustaf Werlinder, Patrik Thitusson, Markus Schutzer
 */
 
 package se.team05.test.ui;
 
 import java.util.ArrayList;
 
 import se.team05.R;
 import se.team05.activity.MediaSelectorActivity;
 import se.team05.adapter.MediaSelectorAdapter;
 import se.team05.content.Track;
 import android.content.Intent;
 import android.database.Cursor;
 import android.provider.MediaStore;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.ListView;
 
 import com.jayway.android.robotium.solo.Solo;
 
 public class MediaSelectorUITest extends ActivityInstrumentationTestCase2<MediaSelectorActivity>
 {
 	private Solo solo;
 	private MediaSelectorActivity activity;
 	private ListView listView;
 	private MediaSelectorAdapter adapter;
 	private String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
 	private String[] projection = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST,
 			MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
 			MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION };
 
 	public MediaSelectorUITest()
 	{
 		super(MediaSelectorActivity.class);
 	}
 
 	/**
 	 * Runs automatically by JUnit before each test case. Sets up the testing
 	 * environment before each individual test by getting a Solo instance for
 	 * robotium and setting variables for use in test-methods.
 	 */
 	@Override
 	protected void setUp() throws Exception
 	{
 		super.setUp();
 		solo = new Solo(this.getInstrumentation(), getActivity());
 		this.setActivityInitialTouchMode(false);
 		Intent intent = new Intent();
 		intent.setClassName("se.team05", "MediaSelectorActivity");
 		intent.putExtra(MediaSelectorActivity.EXTRA_SELECTED_ITEMS, new ArrayList<Track>());
 		setActivityIntent(intent);
 		activity = getActivity();
 		listView = (ListView) activity.findViewById(R.id.list);
 		solo.waitForView(listView);
 		adapter = (MediaSelectorAdapter) listView.getAdapter();
 	}
 
 	/**
 	 * This method tests so that the selection and deselection of a checkbox
 	 * item is working and that a track is being saved and removed in the
 	 * selected items list.
 	 */
 	public void testMediaSelectorList()
 	{
 		solo.clickOnCheckBox(0);
 		int i = adapter.getSelectedItems().size();
 		assertEquals(1, i);
 		solo.clickOnCheckBox(0);
 		i = adapter.getSelectedItems().size();
 		assertEquals(0, i);
 	}
 
 	/**
 	 * This method tests the sorting of the track list view. It firsts makes a
 	 * default sort on "artist" and asserts that it is equal to what the media
 	 * store returns. It the queries and sorts for album and title in the same
 	 * way, comparing the strings of the "data" field to make sure the rows are
 	 * pointing at the same file.
 	 * 
 	 * @throws InterruptedException
 	 */
 	public void testMediaSelectorListSort() throws InterruptedException
 	{
 		Cursor testCursor = activity.managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,
 				null, MediaStore.Audio.Media.ARTIST);
 		testCursor.moveToFirst();
 		solo.clickOnMenuItem("Sort by artist");
		Thread.sleep(1000);
 		Cursor cursor = adapter.getCursor();
 		cursor.moveToFirst();
 		String testData = testCursor.getString(testCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
 		String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
 		assertEquals(testData, data);
 
 		testCursor = activity.managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
 				MediaStore.Audio.Media.ALBUM);
 		testCursor.moveToFirst();
 		solo.clickOnMenuItem("Sort by album");
		Thread.sleep(1000);
 		cursor = adapter.getCursor();
 		cursor.moveToFirst();
 		testData = testCursor.getString(testCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
 		data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
 		assertEquals(testData, data);
 
 		testCursor = activity.managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
 				MediaStore.Audio.Media.TITLE);
 		testCursor.moveToFirst();
 		solo.clickOnMenuItem("Sort by track");
		Thread.sleep(1000);
 		cursor = adapter.getCursor();
 		cursor.moveToFirst();
 		testData = testCursor.getString(testCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
 		data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
 		assertEquals(testData, data);
 		
 		solo.clickOnActionBarItem(R.id.done);
 	}
 
 	/**
 	 * Runs automatically by JUnit after each test case. Finalizes the activity,
 	 * releasing the object, and then calls parent tear down method to do its
 	 * thing.
 	 */
 	@Override
 	protected void tearDown() throws Exception
 	{
 		activity.finish();
 	}
 }
