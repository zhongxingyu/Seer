 /*
  Dubsar Dictionary Project
  Copyright (C) 2010-11 Jimmy Dee
  
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package com.dubsar_dictionary.Dubsar;
 
 import java.lang.ref.WeakReference;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.BaseColumns;
 import android.view.View;
 import android.widget.ExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 
 public class SynsetActivity extends DubsarActivity {
 	public static final String SYNSET_SYNONYM_IDS = "synset_synonym_ids";
 	public static final String SYNSET_SYNONYM_NAMES = "synset_synonym_names";
 	public static final String SYNSET_SYNONYM_MARKERS = "synset_synonym_markers";
 	public static final String SYNSET_SYNONYM_FREQ_CNTS = "synset_synonym_freq_cnts";
 	public static final String SYNSET_SAMPLE_IDS = "synset_sample_ids";
 	public static final String SYNSET_SAMPLES = "synset_samples";
 	
 	private TextView mBanner=null;
 	private TextView mGlossView=null;
 	private ExpandableListView mLists=null;
 	
 	private String mSubtitle=null;
 	private String mGloss=null;
 	
 	private Cursor mResult=null;
 	private SynsetExpandableListAdapter mAdapter=null;
 	
 	private int mSynonymCount=0;
 	private int mSampleCount=0;
 	private int mPointerCount=0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.synset);
 		
 		mBanner = (TextView)findViewById(R.id.synset_banner);
 		mGlossView = (TextView)findViewById(R.id.synset_gloss);
 		mLists = (ExpandableListView)findViewById(R.id.synset_lists);
 		
 		setupFonts();
 		
 		Intent intent = getIntent();
 		Uri uri = intent.getData();
 		
 		if (savedInstanceState != null) {
 			retrieveInstanceState(savedInstanceState);
 		}
 
 		if (mResult != null) {
 			populateData();
 		}
 		else {
 			new SynsetQuery(mBanner, mGlossView, mLists).execute(uri);
 		}
 	}
 	
 	protected void setupFonts() {
 		setBoldItalicTypeface(mBanner);
 	}
 	
 	public Activity getActivity() {
 		return this;
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		saveState(outState);
 	}
 	
 	protected void retrieveInstanceState(Bundle inState) {
 
 		/* Seems to do this without me. Plus, this crashes with a NPE inside
 		 * expandGroup().ENSE
 		boolean[] expanded = inState.getBooleanArray(EXPANDED);
 		for (int j=0; j<expanded.length; ++j) {
 			if (expanded[j]) {
 				mLists.expandGroup(j);
 			}
 		}
 		 */
 			
 		mSubtitle = inState.getString(DubsarContentProvider.SYNSET_SUBTITLE);
 		mGloss = inState.getString(DubsarContentProvider.SYNSET_GLOSS);
 		
 		setupResultCursor(inState);
 		unbundleSamples(inState);
 		unbundleSynonyms(inState);
 		unbundlePointers(inState);
 	}
 	
 	protected void saveState(Bundle outState) {
 		if (mResult == null) return;
 		
 		if (mAdapter != null) {
 			outState.putBooleanArray(EXPANDED, mAdapter.getExpanded());
 		}
 		outState.putString(DubsarContentProvider.SYNSET_SUBTITLE, mSubtitle);
 		outState.putString(DubsarContentProvider.SYNSET_GLOSS, mGloss);
 		
 		bundleSynonyms(outState);
 		bundleSamples(outState);
 		bundlePointers(outState);
 	}
 	
 	protected void saveResults(Cursor result) {
 		int subtitleColumn = result.getColumnIndex(DubsarContentProvider.SYNSET_SUBTITLE);
 		int glossColumn = result.getColumnIndex(DubsarContentProvider.SYNSET_GLOSS);
 		
 		int synonymCountColumn = result.getColumnIndex(DubsarContentProvider.SYNSET_SENSE_COUNT);
 		int sampleCountColumn = result.getColumnIndex(DubsarContentProvider.SYNSET_SAMPLE_COUNT);
 		int pointerCountColumn = result.getColumnIndex(DubsarContentProvider.POINTER_COUNT);
 
 		result.moveToFirst();
 		mSubtitle = result.getString(subtitleColumn);
 		mGloss = result.getString(glossColumn);
 		
 		mSynonymCount = result.getInt(synonymCountColumn);
 		mSampleCount = result.getInt(sampleCountColumn);
 		mPointerCount = result.getInt(pointerCountColumn);
 
 		mResult = result;
 	}
 	
 	protected void populateData() {
 		mBanner.setText(mSubtitle);
 		mGlossView.setText(mGloss);
 		
 		// set up the expandable list view
 		mAdapter = new SynsetExpandableListAdapter(getActivity(), mResult);
 		mLists.setAdapter(mAdapter);
 	}
 	
 	protected void bundleSynonyms(Bundle outState) {
 		int synonymCountColumn = 
 				mResult.getColumnIndex(DubsarContentProvider.SYNSET_SENSE_COUNT);
 		int idColumn = mResult.getColumnIndex(BaseColumns._ID);
 		int nameColumn = mResult.getColumnIndex(DubsarContentProvider.SENSE_NAME);
 		int markerColumn = mResult.getColumnIndex(DubsarContentProvider.SENSE_MARKER);
 		int freqCntColumn = mResult.getColumnIndex(DubsarContentProvider.SENSE_FREQ_CNT);
 		
 		mResult.moveToFirst();
 		mSynonymCount = mResult.getInt(synonymCountColumn);
 		
 		int[] ids = new int[mSynonymCount];
 		String[] names = new String[mSynonymCount];
 		String[] markers = new String[mSynonymCount];
 		int[] freqCnts = new int[mSynonymCount];
 		
 		outState.putInt(DubsarContentProvider.SYNSET_SENSE_COUNT, mSynonymCount);
 		for (int j=0; j<mSynonymCount; ++j) {
 			mResult.moveToPosition(mSampleCount+j);
 			
 			ids[j] = mResult.getInt(idColumn);
 			names[j] = mResult.getString(nameColumn);
 			markers[j] = mResult.getString(markerColumn);
 			freqCnts[j] = mResult.getInt(freqCntColumn);
 		}
 		
 		outState.putIntArray(SYNSET_SYNONYM_IDS, ids);
 		outState.putStringArray(SYNSET_SYNONYM_NAMES, names);
 		outState.putStringArray(SYNSET_SYNONYM_MARKERS, markers);
 		outState.putIntArray(SYNSET_SYNONYM_FREQ_CNTS, freqCnts);
 	}
 	
 	protected void bundleSamples(Bundle outState) {
 		int sampleCountColumn = mResult.getColumnIndex(DubsarContentProvider.SYNSET_SAMPLE_COUNT);
 		int idColumn = mResult.getColumnIndex(BaseColumns._ID);
 		int sampleColumn = mResult.getColumnIndex(DubsarContentProvider.SYNSET_SAMPLE);
 		
 		mResult.moveToFirst();
 		mSampleCount = mResult.getInt(sampleCountColumn);
 		
 		int[] ids = new int[mSampleCount];
 		String[] samples = new String[mSampleCount];
 		
 		outState.putInt(DubsarContentProvider.SYNSET_SAMPLE_COUNT, mSampleCount);
 		for (int j=0; j<mSampleCount; ++j) {
 			mResult.moveToPosition(j);
 			ids[j] = mResult.getInt(idColumn);
 			samples[j] = mResult.getString(sampleColumn);
 		}
 		
 		outState.putIntArray(SYNSET_SAMPLE_IDS, ids);
 		outState.putStringArray(SYNSET_SAMPLES, samples);	
 	}
 	
 	protected void bundlePointers(Bundle outState) {
 		int pointerCountColumn = mResult.getColumnIndex(DubsarContentProvider.POINTER_COUNT);
 		int idColumn = mResult.getColumnIndex(BaseColumns._ID);
 		int ptypeColumn = mResult.getColumnIndex(DubsarContentProvider.POINTER_TYPE);
 		int targetTypeColumn = mResult.getColumnIndex(DubsarContentProvider.POINTER_TARGET_TYPE);
 		int targetIdColumn = mResult.getColumnIndex(DubsarContentProvider.POINTER_TARGET_ID);
 		int targetTextColumn = mResult.getColumnIndex(DubsarContentProvider.POINTER_TARGET_TEXT);
 		int targetGlossColumn = mResult.getColumnIndex(DubsarContentProvider.POINTER_TARGET_GLOSS);
 
 		mResult.moveToFirst();
 		mPointerCount = mResult.getInt(pointerCountColumn);
 		
 		int ids[] = new int[mPointerCount];
 		String ptypes[] = new String[mPointerCount];
 		String targetTypes[] = new String[mPointerCount];
 		int targetIds[] = new int[mPointerCount];
 		String targetTexts[] = new String[mPointerCount];
 		String targetGlosses[] = new String[mPointerCount];
 		
 		outState.putInt(DubsarContentProvider.POINTER_COUNT, mPointerCount);
 		for (int j=0; j<mPointerCount; ++j) {
 			mResult.moveToPosition(mSynonymCount+mSampleCount+j);
 			
 			ids[j] = mResult.getInt(idColumn);
 			ptypes[j] = mResult.getString(ptypeColumn);
 			targetTypes[j] = mResult.getString(targetTypeColumn);
 			targetIds[j] = mResult.getInt(targetIdColumn);
 			targetTexts[j] = mResult.getString(targetTextColumn);
 			targetGlosses[j] = mResult.getString(targetGlossColumn);
 		}
 		
 		outState.putIntArray(POINTER_IDS, ids);
 		outState.putStringArray(POINTER_TYPES, ptypes);
 		outState.putStringArray(POINTER_TARGET_TYPES, targetTypes);
 		outState.putIntArray(POINTER_TARGET_IDS, targetIds);
 		outState.putStringArray(POINTER_TARGET_TEXTS, targetTexts);
 		outState.putStringArray(POINTER_TARGET_GLOSSES, targetGlosses);
 	}
 	
 	protected void unbundlePointers(Bundle inState) {
 		if (mPointerCount <= 0) return;
 		
 		int[] ids = inState.getIntArray(POINTER_IDS);
 		String[] ptypes = inState.getStringArray(POINTER_TYPES);
 		String[] targetTypes = inState.getStringArray(POINTER_TARGET_TYPES);
 		int[] targetIds = inState.getIntArray(POINTER_TARGET_IDS);
 		String[] targetTexts = inState.getStringArray(POINTER_TARGET_TEXTS);
 		String[] targetGlosses = inState.getStringArray(POINTER_TARGET_GLOSSES);
 		
 		MatrixCursor.RowBuilder builder;
 		MatrixCursor cursor = (MatrixCursor)mResult;
 		for (int j=0; j<mPointerCount; ++j) {
 			builder = cursor.newRow();
 			buildRowBase(ids[j], builder);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(ptypes[j]);
 			builder.add(targetTypes[j]);
 			builder.add(new Integer(targetIds[j]));
 			builder.add(targetTexts[j]);
 			builder.add(targetGlosses[j]);
 		}
 	}
 
 	protected void unbundleSynonyms(Bundle inState) {
 		if (mSynonymCount <= 0) return;
 		
 		int[] ids = inState.getIntArray(SYNSET_SYNONYM_IDS);
 		String[] names = inState.getStringArray(SYNSET_SYNONYM_NAMES);
 		String[] markers = inState.getStringArray(SYNSET_SYNONYM_MARKERS);
 		int[] freqCnts = inState.getIntArray(SYNSET_SYNONYM_FREQ_CNTS);
 		
 		MatrixCursor.RowBuilder builder;
 		MatrixCursor cursor = (MatrixCursor)mResult;
 		for (int j=0; j<mSynonymCount; ++j) {
 			builder = cursor.newRow();
 			buildRowBase(ids[j], builder);
 			builder.add(names[j]);
 			builder.add(markers[j]);
 			builder.add(freqCnts[j]);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 		}
 	}	
 	
 	protected void unbundleSamples(Bundle inState) {
 		if (mSampleCount <= 0) return;
 		
 		int[] ids = inState.getIntArray(SYNSET_SAMPLE_IDS);
 		String[] samples = inState.getStringArray(SYNSET_SAMPLES);
 		
 		MatrixCursor.RowBuilder builder;
 		MatrixCursor cursor = (MatrixCursor)mResult;
 		for (int j=0; j<mSampleCount; ++j) {
 			builder = cursor.newRow();
 			buildRowBase(ids[j], builder);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(samples[j]);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 		}
 	}
 	
 	protected MatrixCursor setupResultCursor(Bundle inState) {
		mSynonymCount = inState.getInt(DubsarContentProvider.SENSE_SYNONYM_COUNT);
		mSampleCount = inState.getInt(DubsarContentProvider.SENSE_SAMPLE_COUNT);
 		mPointerCount = inState.getInt(DubsarContentProvider.POINTER_COUNT);
 		
 		int totalCount = mSynonymCount + mSampleCount + mPointerCount;
 		
 		String[] columns = new String[] { 
 			BaseColumns._ID,
 			DubsarContentProvider.SYNSET_GLOSS,
 			DubsarContentProvider.SYNSET_SUBTITLE,
 			DubsarContentProvider.SYNSET_SENSE_COUNT,
 			DubsarContentProvider.SYNSET_SAMPLE_COUNT,
 			DubsarContentProvider.POINTER_COUNT,
 			DubsarContentProvider.SENSE_NAME,
 			DubsarContentProvider.SENSE_MARKER,
 			DubsarContentProvider.SENSE_FREQ_CNT,
			DubsarContentProvider.SENSE_SAMPLE,
 			DubsarContentProvider.POINTER_TYPE,
 			DubsarContentProvider.POINTER_TARGET_TYPE,
 			DubsarContentProvider.POINTER_TARGET_ID,
 			DubsarContentProvider.POINTER_TARGET_TEXT,
 			DubsarContentProvider.POINTER_TARGET_GLOSS
 		};
 
 		MatrixCursor cursor = new MatrixCursor(columns, totalCount > 0 ? totalCount : 1);
 		mResult = cursor;
 		return cursor;
 	}
 	
 	protected void buildRowBase(int id, MatrixCursor.RowBuilder builder) {
 		builder.add(new Integer(id));
 		builder.add(mGloss);
 		builder.add(mSubtitle);
 		builder.add(new Integer(mSynonymCount));
 		builder.add(new Integer(mSampleCount));
 		builder.add(new Integer(mPointerCount));
 	}
 	
 
 	class SynsetQuery extends AsyncTask<Uri, Void, Cursor> {
 		private final WeakReference<TextView> mBannerReference;
 		private final WeakReference<TextView> mGlossReference;
 		private final WeakReference<ExpandableListView> mListsReference;
 		
 		public SynsetQuery(TextView banner, TextView gloss, ExpandableListView lists) {
 			mBannerReference = new WeakReference<TextView>(banner);
 			mGlossReference = new WeakReference<TextView>(gloss);
 			mListsReference = new WeakReference<ExpandableListView>(lists);
 		}
 		
 		@Override
 		protected Cursor doInBackground(Uri... params) {
 			return managedQuery(params[0], null, null, null, null);
 		}
 
 		@Override
 		protected void onPostExecute(Cursor result) {
 			super.onPostExecute(result);
 			
 			if (isCancelled()) {
 				return;
 			}
 			
 			TextView banner = mBannerReference != null ? mBannerReference.get() : null;
 			TextView gloss = mGlossReference != null ? mGlossReference.get() : null;
 			ExpandableListView lists = mListsReference != null ? mListsReference.get() : null;
 			
 			if (banner == null ||
 				gloss == null ||
 				lists == null) return;
 			
 			if (result == null) {
 				// DEBT: Externalize
 				gloss.setText("ERROR!");
 				gloss.setBackgroundResource(R.drawable.rounded_orange_rectangle);
 				banner.setVisibility(View.GONE);
 				lists.setVisibility(View.GONE);
 			}
 			else {
 				saveResults(result);
 				
 				result.moveToFirst();
 				
 				// first set the scalar field values
 				int glossColumn = result.getColumnIndex(DubsarContentProvider.SYNSET_GLOSS);
 				int subtitleColumn = result.getColumnIndex(DubsarContentProvider.SYNSET_SUBTITLE);
 				
 				gloss.setText(result.getString(glossColumn));
 				banner.setText(result.getString(subtitleColumn));
 				
 				// set up the expandable list view
 				ExpandableListAdapter adapter = 
 						new SynsetExpandableListAdapter(getActivity(), result);
 				lists.setAdapter(adapter);
 			}
 		}
 		
 	}
 }
