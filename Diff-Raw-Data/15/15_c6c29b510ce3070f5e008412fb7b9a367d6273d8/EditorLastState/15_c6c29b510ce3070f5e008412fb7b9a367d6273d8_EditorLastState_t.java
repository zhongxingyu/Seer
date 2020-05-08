 /* ========================================================================= *
  * Boarder                                                                   *
  * http://boarder.mikuz.org/                                                 *
  * ========================================================================= *
  * Copyright (C) 2013 Boarder                                                *
  *                                                                           *
  * Licensed under the Apache License, Version 2.0 (the "License");           *
  * you may not use this file except in compliance with the License.          *
  * You may obtain a copy of the License at                                   *
  *                                                                           *
  *     http://www.apache.org/licenses/LICENSE-2.0                            *
  *                                                                           *
  * Unless required by applicable law or agreed to in writing, software       *
  * distributed under the License is distributed on an "AS IS" BASIS,         *
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
  * See the License for the specific language governing permissions and       *
  * limitations under the License.                                            *
  * ========================================================================= */
 
 package fi.mikuz.boarder.util.editor;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import fi.mikuz.boarder.component.soundboard.GraphicalSound;
 import fi.mikuz.boarder.component.soundboard.GraphicalSoundboard;
 import fi.mikuz.boarder.gui.BoardEditor;
 
 /**
  * Remembers last state of the <code>BoardEditor</code>.
  * <p>
  * When leaving the {@link BoardEditor} activity in order to modify a page or sound 
  * the editor itself may have changed or no longer live when returning to it.
  * Possible causes are orientation change on hybrid boards or Android's 
  * background activity killing.
  * <p>
  * In order to always resolve the context of data returned by activity in 
  * <code>onActivityResult</code> method the editor must save its state when 
  * leaving the activity.
  * Additionally the state must honor the Android activity life cycle.
  * <p>
  * Life cycle of this object:
  * <p><ol>
  * <li><code>BoardEditor</code> creates this when calling {@link Activity#startActivityForResult(Intent,int)}
  * <li><code>BoardEditor</code> calls {@link #saveEditorState(Bundle)} if necessary
  * <li>this is restored on <code>BoardEditor</code> constructor if necessary
  * <li><code>BoardEditor</code> uses and discards this on {@link Activity#onActivityResult(int,int,Intent)}
  * </ol><p>
  */
 public class EditorLastState {
 	private static final String TAG = EditorLastState.class.getSimpleName();
 	
 	private static final String EDITOR_PAGE_ID = "editorPageId";
 	private static final String EDITOR_PRESSED_SOUND_ID = "editorPressedSoundId";
 	
 	private GraphicalSoundboard lastPage;
 	private GraphicalSound lastPressedSound;
 	
 	public EditorLastState(GraphicalSoundboard page, GraphicalSound pressedSound) {
 		this.lastPage = page;
 		this.lastPressedSound = pressedSound;
 	}
 	
 	public EditorLastState(GraphicalSoundboardProvider gsbp, Bundle extras) {
 		
         Integer lastPageId = null;
         Long lastPressedSoundId = null;
         try {
         	lastPageId = extras.getInt(EDITOR_PAGE_ID);
         	lastPressedSoundId = extras.getLong(EDITOR_PRESSED_SOUND_ID);
         } catch (NullPointerException e) {}
         
         
         
         if (lastPageId != null) {
         	Log.v(TAG, "Recovering last page.");
         	
         	for (GraphicalSoundboard page : gsbp.getBoardList()) {
         		if (page.getId() == lastPageId) {
         			this.lastPage = page;
         			
         			if (lastPressedSoundId != null) {
         				Log.v(TAG, "Recovering last pressed sound.");
         				
         				for (GraphicalSound sound : page.getSoundList()) {
             				if (sound.getId() == lastPressedSoundId) {
             					this.lastPressedSound = sound;
             					break;
             				}
             			}
         			}
         			
         			break;
         		}
         	}
         	
         	if (this.lastPage != null) {
         		Log.v(TAG, "Last page was recovered");
         	}
         	
         	if (this.lastPressedSound != null) {
         		Log.v(TAG, "Last pressed sound was not recovered");
         	}
         }
 	}
 	
 	public GraphicalSoundboard getLastPage() {
 		return lastPage;
 	}
 	
 	public GraphicalSound getLastPressedSound() {
 		return lastPressedSound;
 	}
 	
 	public void saveEditorState(Bundle outState) {
 		if (this.lastPage != null) {
 			outState.putInt(EDITOR_PAGE_ID, this.lastPage.getId());
 		}
 		if (this.lastPressedSound != null) {
 			outState.putLong(EDITOR_PRESSED_SOUND_ID, this.lastPressedSound.getId());
 		}
 	}
 }
