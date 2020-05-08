 /*
    Copyright 2011 kanata3249
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.github.kanata3249.ffxieq.android;
 
 import com.github.kanata3249.ffxi.FFXIDAO;
 import com.github.kanata3249.ffxieq.android.db.FFXIEQSettings;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.util.AttributeSet;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 
 public class CharacterSelectorView extends Spinner {
 
 	public CharacterSelectorView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 	}
 
 	FFXIDAO mDao;
 	FFXIEQSettings mSettings;
 
 	public boolean setParam(FFXIEQSettings settings, FFXIDAO dao, long current) {
 		CharacterSelectorAdapter adapter;
 		String [] columns = { FFXIEQSettings.C_Id, FFXIEQSettings.C_Name };
 		int []views = { 0, android.R.id.text1 };
 		Cursor cursor;
 
 		mDao = dao;
 		mSettings = settings;
 		
 		cursor = settings.getCharactersCursor(columns, FFXIEQSettings.C_Name + " ASC");
 		if (getAdapter() == null) {
 			adapter = new CharacterSelectorAdapter(getContext(), android.R.layout.simple_spinner_item, cursor, columns, views);
 			adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
 			setAdapter(adapter);
 		} else {
 			((CharacterSelectorAdapter)getAdapter()).changeCursor(cursor);
 		}
 		setSelectionById(current);
 		
 		return true;
 	}
 	
 	public void setSelectionById(long selectionid) {
 		if (getSelectedItemId() == selectionid) {
 			return;
 		}
 		for (int i = 0; i < getCount(); i++) {
 			long id = getItemIdAtPosition(i);
 			if (id == selectionid) {
 				setSelection(i);
 				break;
 			}
 		}
 	}
 	
 	private class CharacterSelectorAdapter extends SimpleCursorAdapter {
 
 		public CharacterSelectorAdapter(Context context,
 				int textViewResourceId, Cursor c, String[] from, int[] to) {
 			super(context, textViewResourceId, c, from, to);
 		}
 	}
 
 	public void notifyDatasetChanged(long id) {
 		String [] columns = { FFXIEQSettings.C_Id, FFXIEQSettings.C_Name };
 		Cursor cursor;
 
 		if (getAdapter() != null) {
 			cursor = mSettings.getCharactersCursor(columns, FFXIEQSettings.C_Name + " ASC");
 			((CharacterSelectorAdapter)getAdapter()).changeCursor(cursor);
 			setSelectionById(id);
 		}
 	}
 	
 	public String getName() {
 		long id;
 		
 		id = getSelectedItemId();
 		return mSettings.getCharacterName(id);
 	}

	@Override
	protected void onDetachedFromWindow() {
		CharacterSelectorAdapter adapter;

		adapter = ((CharacterSelectorAdapter)getAdapter());
		if (adapter != null) {
			adapter.changeCursor(null);
		}

		super.onDetachedFromWindow();
	}
 }
