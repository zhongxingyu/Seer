 package com.trio.triotictactoe.model;
 
 import android.graphics.drawable.Drawable;
 import android.util.SparseArray;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.trio.triotictactoe.R;
 import com.trio.triotictactoe.activity.GameActivity;
 import com.trio.triotictactoe.preferences.UserPreferences;
 import com.trio.triotictactoe.utils.CellState;
 import com.trio.triotictactoe.utils.TTTConstants;
 
 /**
  * Static methods to save & restore game data
  * @author ajith
  *
  */
 public class GameDataManager {
 	private GameActivity activity;
 	private OnClickListener linearLayoutListener;
 	private UserPreferences prefs;
 
 	public GameDataManager(GameActivity gameActivity, OnClickListener listener) {
 		this.activity = gameActivity;
 		this.linearLayoutListener = listener;
 		prefs = new UserPreferences(activity);
 	}
 
 	public boolean isPreviousGameDataFound() {
 		return prefs.getPreference(TTTConstants.SAVED_GAME_DATA_KEY, null) != null;
 	}
 
 	/**
 	 * Remove stored game data
 	 */
 	public void clearSavedGame() {
 		prefs.removePreference(TTTConstants.SAVED_GAME_TIME_KEY);
 		prefs.removePreference(TTTConstants.SAVED_GAME_DATA_KEY);
 	}
 
 	/**
 	 * Serialize the last game data
 	 */
 	public void storeThisGame() {
 		prefs.setPreference(TTTConstants.SAVED_GAME_TIME_KEY, Long.toString(System.currentTimeMillis() - activity.getStartTime()));
		prefs.setPreference(TTTConstants.SAVED_GAME_DATA_KEY, getSerializeGameData());
 	}
 
 	/**
 	 * Populate data from a previous game
 	 */
 	public void restorePreviousGame() {
 		SparseArray<MiniTTTData> newMiniTTTDataMap = getDeserializedGameData();
 
 		for (int i = 0; i <= 2; i++) {
 			for (int j = 0; j <= 2; j++) {
 				String viewIdStr = "ttt_" + i + "_" + j;
 				int viewId = activity.getResources().getIdentifier(viewIdStr, "id", activity.getPackageName());
 
 				View view = activity.findViewById(R.id.main_game_layout).findViewById(viewId);
 				view.setTag(R.dimen.row_id, i);
 				view.setTag(R.dimen.col_id, j);
 				view.setOnClickListener(linearLayoutListener);
 
 				MiniTTTData oneTTTData = newMiniTTTDataMap.get(viewId);
 				System.out.println("viewIdStr: " + viewIdStr);
 
 				for (int x = 0; x <= 2; x++) {
 					for (int y = 0; y <= 2; y++) {
 						String cellIdStr = "b_" + x + "_" + y;
 						int cellId = activity.getResources().getIdentifier(cellIdStr, "id", activity.getPackageName());
 						Button cell = (Button) view.findViewById(cellId);
 
 						CellState userState = oneTTTData.getCell(x, y);
 
 						switch (userState) {
 							case EMPTY :
 								break;
 
 							case CROSS :
 								Drawable xMove = activity.getResources().getDrawable(R.drawable.x_move);
 								cell.setBackgroundDrawable(xMove);
 								cell.setTag(R.dimen.cell_state, CellState.CROSS);
 								break;
 
 							case NOUGHTS :
 								Drawable oMove = activity.getResources().getDrawable(R.drawable.o_move);
 								cell.setBackgroundDrawable(oMove);
 								cell.setTag(R.dimen.cell_state, CellState.NOUGHTS);
 								break;
 						}
 					}
 				}
 			}
 		}
 
 		activity.setMiniTTTDataMap(newMiniTTTDataMap);
 		activity.adjustStartTime(Long.valueOf(prefs.getPreference(TTTConstants.SAVED_GAME_TIME_KEY, "0")));
 	}
 
 	/**
 	 * Create a brand new game
 	 */
 	public void createEmptyGameCells() {
 		View gameLayout = activity.findViewById(R.id.main_game_layout);
 		SparseArray<MiniTTTData> newMiniTTTDataMap = new SparseArray<MiniTTTData>();
 
 		for (int i = 0; i <= 2; i++) {
 			for (int j = 0; j <= 2; j++) {
 				String viewIdStr = "ttt_" + i + "_" + j;
 				int viewId = activity.getResources().getIdentifier(viewIdStr, "id", activity.getPackageName());
 				View view = gameLayout.findViewById(viewId);
 
 				view.setTag(R.dimen.row_id, i);
 				view.setTag(R.dimen.col_id, j);
 				view.setOnClickListener(linearLayoutListener);
 
 				newMiniTTTDataMap.put(viewId, new MiniTTTData());
 			}
 		}
 
 		activity.setMiniTTTDataMap(newMiniTTTDataMap);
 	}
 
 	/**
 	 * Before calling zoom view, fill the required data in MiniTTTData for that layout.
 	 * This is also called after the system updates with its reply to the user.
 	 * 
 	 * @param clickedLinearLayout
 	 */
 	public void fillClickedLinearLayoutToOneTTTData(final View clickedLinearLayout) {
 		MiniTTTData oneTTTdata = activity.getMiniTTTDataMap().get(clickedLinearLayout.getId());
 		for (int i = 0; i <= 2; i++) {
 			for (int j = 0; j <= 2; j++) {
 				String viewIdStr = "b_" + i + "_" + j;
 				int viewId = activity.getResources().getIdentifier(viewIdStr, "id", activity.getPackageName());
 				Button cell = (Button) clickedLinearLayout.findViewById(viewId);
 
 				if (cell != null) {
 					CellState state = (CellState) cell.getTag(R.dimen.cell_state);
 					if (state != null)
 						switch (state) {
 							case EMPTY :
 								oneTTTdata.populateData(i, j, CellState.EMPTY);
 								break;
 
 							case CROSS :
 								oneTTTdata.populateData(i, j, CellState.CROSS);
 								break;
 
 							case NOUGHTS :
 								oneTTTdata.populateData(i, j, CellState.NOUGHTS);
 								break;
 						}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Return serialized data
 	 * @return
 	 */
	private String getSerializeGameData() {
 		View gameLayout = activity.findViewById(R.id.main_game_layout);
 		StringBuffer serializedData = new StringBuffer();
 
 		for (int i = 0; i <= 2; i++) {
 			for (int j = 0; j <= 2; j++) {
 				String viewIdStr = "ttt_" + i + "_" + j;
 				int viewId = activity.getResources().getIdentifier(viewIdStr, "id", activity.getPackageName());
 				View view = gameLayout.findViewById(viewId);
 
 				if (view != null) {
 					serializedData.append(getOneTTTDataAsString(view));
 
 					// Do not append only for the last TTT
 					if (!(i == 2 && j == 2)) {
 						serializedData.append(TTTConstants.SERIALIZATION_TTT_DATA_SEPARATOR);
 					}
 				}
 			}
 		}
 
 		return serializedData.toString();
 	}
 
 	/**
 	 * Return one TTT data as string of @CellState enum values separated by comma.
 	 * Used during serialization
 	 * @param view
 	 * @return
 	 */
 	private StringBuffer getOneTTTDataAsString(View view) {
 		StringBuffer oneTTTData = new StringBuffer();
 		MiniTTTData data = activity.getMiniTTTDataMap().get(view.getId());
 		for (int i = 0; i <= 2; i++) {
 			for (int j = 0; j <= 2; j++) {
 				oneTTTData.append(data.getCell(i, j));
 
 				// Do not append only for the last cell
 				if (!(i == 2 && j == 2)) {
 					oneTTTData.append(TTTConstants.SERIALIZATION_CELL_DATA_SEPARATOR);
 				}
 			}
 		}
 		return oneTTTData;
 	}
 
 	/**
 	 * De-serialize the last game data
 	 */
 	private SparseArray<MiniTTTData> getDeserializedGameData() {
 		String oldData = prefs.getPreference(TTTConstants.SAVED_GAME_DATA_KEY, null);
 		SparseArray<MiniTTTData> miniTTTDataMap = new SparseArray<MiniTTTData>();
 
 		if (oldData != null) {
 			String[] individualTTTData = oldData.split(TTTConstants.SERIALIZATION_TTT_DATA_SEPARATOR);
 
 			for (int i = 0; i <= 2; i++) {
 				for (int j = 0; j <= 2; j++) {
 					String viewIdStr = "ttt_" + i + "_" + j;
 					int viewId = activity.getResources().getIdentifier(viewIdStr, "id", activity.getPackageName());
 
 					int index = i * 3 + j;
 					miniTTTDataMap.put(viewId, getStringAsOneTTTData(individualTTTData[index]));
 				}
 			}
 
 		}
 
 		return miniTTTDataMap;
 	}
 
 	/**
 	 * Return @MiniTTTData corresponding to one mini TTT game.
 	 * Used during de-serialization
 	 * @param tttData
 	 * @return
 	 */
 	private MiniTTTData getStringAsOneTTTData(String tttData) {
 		CellState[][] stateData = new CellState[3][3];
 		String[] individualCellData = tttData.split(TTTConstants.SERIALIZATION_CELL_DATA_SEPARATOR);
 
 		for (int i = 0; i <= 2; i++) {
 			for (int j = 0; j <= 2; j++) {
 				int index = i * 3 + j;
 				stateData[i][j] = CellState.valueOf(individualCellData[index]);
 			}
 		}
 		MiniTTTData miniTTTData = new MiniTTTData();
 		miniTTTData.setData(stateData);
 		return miniTTTData;
 	}
 
 }
