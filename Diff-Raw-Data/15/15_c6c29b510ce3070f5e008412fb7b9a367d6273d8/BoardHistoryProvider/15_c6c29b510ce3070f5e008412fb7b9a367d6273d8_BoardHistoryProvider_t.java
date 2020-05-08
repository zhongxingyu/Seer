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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.util.Log;
 import fi.mikuz.boarder.component.soundboard.BoardHistory;
 import fi.mikuz.boarder.component.soundboard.GraphicalSoundboard;
 
 public class BoardHistoryProvider {
 	private static final String TAG = BoardHistoryProvider.class.getSimpleName();
 	
 	private List<BoardHistory> historyList;
 	private BoardHistory lastHistory;
 	
 	public BoardHistoryProvider() {
 		this.historyList = new ArrayList<BoardHistory>();
 	}
 	
 	private BoardHistory createBoardHistory(Context context, GraphicalSoundboard initialHistoryCheckpoint) {
 		int pageId = initialHistoryCheckpoint.getId();
 		Log.v(TAG, "Creating a new history for page id " + pageId);
 		
 		BoardHistory history = new BoardHistory(pageId);
 		history.createHistoryCheckpoint(context, initialHistoryCheckpoint);
 		
 		historyList.add(history);
 		
 		return history;
 	}
 	
 	private BoardHistory getBoardHistory(Context context, GraphicalSoundboard page) {
 		int pageId = page.getId();
		if (lastHistory != null && lastHistory.getBoardId() == pageId) {
 			return lastHistory;
 		}
 		for (BoardHistory history : historyList) {
 			if (history.getBoardId() == pageId) {
 				return history;
 			}
 		}
 		Log.v(TAG, "No history for board id " + page.getId());
 		return null;
 	}
 	
 	/**
 	 * Create checkpoint only if it's an initial checkpoint.
 	 * 
 	 * @param context
 	 * @param board
 	 */
 	public void createInitialHistoryCheckpoint(Context context, GraphicalSoundboard board) {
 		BoardHistory history = getBoardHistory(context, board);
 		if (history == null) {
 			history = createBoardHistory(context, board);
 			history.createHistoryCheckpoint(context, board);
 		}
 	}
 	
 	public void createHistoryCheckpoint(Context context, GraphicalSoundboard board) {
 		BoardHistory history = getBoardHistory(context, board);
 		if (history == null) history = createBoardHistory(context, board);
 		history.createHistoryCheckpoint(context, board);
 	}
 	
 	public void setHistoryCheckpoint(Context context, GraphicalSoundboard board) {
 		BoardHistory history = getBoardHistory(context, board);
 		if (history == null) history = createBoardHistory(context, board);
 		history.setHistoryCheckpoint(context, board);
 	}
 	
 	public GraphicalSoundboard undo(Context context, GraphicalSoundboard board) {
 		BoardHistory history = getBoardHistory(context, board);
 		if (history == null) history = createBoardHistory(context, board);
 		return history.undo(context);
 	}
 	
 	public GraphicalSoundboard redo(Context context, GraphicalSoundboard board) {
 		BoardHistory history = getBoardHistory(context, board);
 		if (history == null) history = createBoardHistory(context, board);
 		return history.redo(context);
 	}
 }
