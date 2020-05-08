 package ca.idrc.tecla.framework;
 
 import java.util.Iterator;
 import java.util.List;
 
 import android.inputmethodservice.Keyboard;
 import android.inputmethodservice.Keyboard.Key;
 import android.inputmethodservice.KeyboardView;
 import android.os.Handler;
 import android.os.Message;
 
 public class IMEAdapter {
 
 	private static final String tag = "IMEAdapter";
 	
 	private static Keyboard sKeyboard = null;
 	private static KeyboardView sKeyboardView = null;
 	private static List<Key> sKeys = null;
 		
 	private static final int REDRAW_KEYBOARD = 0x22;
 	
 	private static Handler sHandler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 			switch(msg.what) {
 			case(REDRAW_KEYBOARD):	sKeyboardView.invalidateAllKeys();
 									break;
 			default:				break;
 			}	
 			super.handleMessage(msg);
 		}
 		
 	};
 	
 	public static void setKeyboardView(KeyboardView kbv) {
 		sKeyboardView = kbv;
 		if(kbv == null) {
 			sKeyboard = null;
 			sKeys = null;	
 			return;
 		}
 		sKeyboard = kbv.getKeyboard();
 		sKeys = sKeyboard.getKeys();
 		IMEStates.reset();
 	}
 	
 	public static void selectHighlighted() {
 		int index = IMEStates.getCurrentKeyIndex();
 		if(index < 0 || index >= sKeys.size()) return;
 		Key key = sKeys.get(index);
		TeclaIME.getInstance().getCurrentInputConnection()
			.commitText(String.valueOf((char)key.codes[0]), 1);		
 	}
 
 	public static void selectScanHighlighted() {
 		IMEStates.click();
 	}
 
 	public static void scanNext() {
 		switch(IMEStates.sState) {
 		case(IMEStates.SCAN_STOPPED):	break;
 		case(IMEStates.SCAN_ROW):		IMEAdapter.highlightNextRow();
 										break;
 		case(IMEStates.SCAN_COLUMN):	IMEAdapter.highlightNextKey();
 										break;
 		case(IMEStates.SCAN_CLICK):		IMEAdapter.highlightNextKey();		
 										break;
 		default:						break;
 		}		
 	}
 	
 	public static void scanPrevious() {
 		switch(IMEStates.sState) {
 		case(IMEStates.SCAN_STOPPED):	break;
 		case(IMEStates.SCAN_ROW):		IMEAdapter.highlightPreviousRow();
 										break;
 		case(IMEStates.SCAN_COLUMN):	IMEAdapter.highlightPreviousKey();
 		case(IMEStates.SCAN_CLICK):		IMEAdapter.highlightPreviousKey();	
 										break;
 		default:						break;
 		}		
 		
 	}
 
 	public static void scanUp() {
 		int index = IMEStates.getCurrentKeyIndex();
 		Key key = sKeys.get(index);
 		Key test_key;
 		int closest_key_index = -1;
 		float distance_sq, shortest_distance_sq;
 		shortest_distance_sq = Float.MAX_VALUE;
 		for(int i=0; i<sKeys.size(); ++i) {
 			if(i == index) continue;
 			test_key = sKeys.get(i);
 			if(test_key.y < key.y) {
 				distance_sq = (key.y - test_key.y)*(key.y - test_key.y) + 
 						(key.x - test_key.x)*(key.x - test_key.x); 
 				if(distance_sq < shortest_distance_sq) {
 					shortest_distance_sq = distance_sq;
 					closest_key_index = i;
 				}
 			}
 		}
 		if(closest_key_index != -1) {
 			IMEAdapter.highlightKey(index, false);
 			IMEStates.setKey(closest_key_index);
 			IMEAdapter.highlightKey(closest_key_index, true);
 		}
 		IMEAdapter.invalidateKeys();
 	}
 
 	public static void scanDown() {
 		int index = IMEStates.getCurrentKeyIndex();
 		Key key = sKeys.get(index);
 		Key test_key;
 		int closest_key_index = -1;
 		float distance_sq, shortest_distance_sq;
 		shortest_distance_sq = Float.MAX_VALUE;
 		for(int i=0; i<sKeys.size(); ++i) {
 			if(i == index) continue;
 			test_key = sKeys.get(i);
 			if(test_key.y > key.y) {
 				distance_sq = (key.y - test_key.y)*(key.y - test_key.y) + 
 						(key.x - test_key.x)*(key.x - test_key.x); 
 				if(distance_sq < shortest_distance_sq) {
 					shortest_distance_sq = distance_sq;
 					closest_key_index = i;
 				}
 			}
 		}
 		if(closest_key_index != -1) {
 			IMEAdapter.highlightKey(index, false);
 			IMEStates.setKey(closest_key_index);
 			IMEAdapter.highlightKey(closest_key_index, true);
 		}
 		IMEAdapter.invalidateKeys();
 	}
 
 	public static void scanLeft() {
 		int index = IMEStates.getCurrentKeyIndex();
 		Key key = sKeys.get(index);
 		Key test_key;
 		int closest_key_index = -1;
 		int distance;
 		int shortest_distance = Integer.MAX_VALUE;
 		for(int i=0; i<sKeys.size(); ++i) {
 			if(i == index) continue;
 			test_key = sKeys.get(i);
 			if(test_key.y == key.y && test_key.x < key.x) {
 				distance = key.x - test_key.x;
 				if(distance < shortest_distance) {
 					shortest_distance = distance;
 					closest_key_index = i;
 				}
 			}
 		}
 		if(closest_key_index != -1) {
 			IMEAdapter.highlightKey(index, false);
 			IMEStates.setKey(closest_key_index);
 			IMEAdapter.highlightKey(closest_key_index, true);
 		}
 		IMEAdapter.invalidateKeys();
 	}
 
 	public static void scanRight() {
 		int index = IMEStates.getCurrentKeyIndex();
 		Key key = sKeys.get(index);
 		Key test_key;
 		int closest_key_index = -1;
 		int distance;
 		int shortest_distance = Integer.MAX_VALUE;
 		for(int i=0; i<sKeys.size(); ++i) {
 			if(i == index) continue;
 			test_key = sKeys.get(i);
 			if(test_key.y == key.y && test_key.x > key.x) {
 				distance = test_key.x - key.x;
 				if(distance < shortest_distance) {
 					shortest_distance = distance;
 					closest_key_index = i;
 				}
 			}
 		}
 		if(closest_key_index != -1) {
 			IMEAdapter.highlightKey(index, false);
 			IMEStates.setKey(closest_key_index);
 			IMEAdapter.highlightKey(closest_key_index, true);
 		}
 		IMEAdapter.invalidateKeys();
 	}
 
 	public static void stepOut() {
 		if(IMEStates.sState != IMEStates.SCAN_ROW) {
 			int index = IMEStates.getCurrentKeyIndex();
 			IMEAdapter.highlightKey(index, false);
 			IMEStates.sState = IMEStates.SCAN_ROW;
 			IMEStates.reset();
 			IMEAdapter.highlightKeys(IMEStates.getRowStart(0), 
 					IMEStates.getRowEnd(0), true);
 		}
 	}
 	
 	
 	private static void highlightKey(int key_index, boolean highlighted) {
 		if(sKeys == null || key_index < 0 || key_index >= sKeys.size()) return; 
         Key key = sKeys.get(key_index);
 		key.pressed = highlighted;
 	}
 	
 	
 	private static void highlightKeys(int start_index, int end_index, boolean highlighted) {
 		for(int i=start_index; i<=end_index; ++i) {
 			highlightKey(i, highlighted);
 		}			
 	}
 
 	private static void invalidateKeys() {
 		Message msg = new Message();
 		msg.what = IMEAdapter.REDRAW_KEYBOARD;
 		sHandler.sendMessageDelayed(msg, 0);		
 	}
 	
 	private static void highlightNextKey() {
 		if(sKeyboard ==null) return;
 		highlightKey(IMEStates.getCurrentKeyIndex(), false);
 		highlightKey(IMEStates.scanNextKey(), true);
 		invalidateKeys();	
 	}
 	
 	private static void highlightPreviousKey() {
 		if(sKeyboard ==null) return;
 		highlightKey(IMEStates.getCurrentKeyIndex(), false);
 		highlightKey(IMEStates.scanPreviousKey(), true);
 		invalidateKeys();		
 	}
 	
 	private static void highlightNextRow() {
 		if(sKeyboard ==null) return;
 		highlightKeys(IMEStates.sKeyStartIndex, IMEStates.sKeyEndIndex, false);
 		IMEStates.scanNextRow();
 		highlightKeys(IMEStates.sKeyStartIndex, IMEStates.sKeyEndIndex, true);
 		invalidateKeys();		
 	}
 	
 	private static void highlightPreviousRow() {
 		if(sKeyboard ==null) return;
 		highlightKeys(IMEStates.sKeyStartIndex, IMEStates.sKeyEndIndex, false);
 		IMEStates.scanPreviousRow();
 		highlightKeys(IMEStates.sKeyStartIndex, IMEStates.sKeyEndIndex, true);
 		invalidateKeys();		
 	}
 	
 	private static class IMEStates {
 
 		private static final int SCAN_STOPPED = 0xa0;
 		private static final int SCAN_ROW = 0xa1;
 		private static final int SCAN_COLUMN = 0xa2;
 		private static final int SCAN_CLICK = 0xa3;
 		private static int sState = SCAN_STOPPED;
 		
 		private static int sRowCount = 0;
 		private static int sCurrentRow = -1;
 		private static int sCurrentColumn = -1; 
 		private static int sKeyStartIndex = -1;
 		private static int sKeyEndIndex = -1;
 		
 		private static void reset() {
 			if(sKeyboard == null) return;
 			sRowCount = getRowCount();
 			sCurrentRow = 0;
 			sCurrentColumn = -1;
 			sKeyStartIndex = getRowStart(0);
 			sKeyEndIndex = getRowEnd(0);
 		}
 
 		private static void click() {
 			switch(sState) {
 			case(SCAN_STOPPED):	sState = SCAN_ROW;
 								AutomaticScan.startAutoScan();
 								break;
 			case(SCAN_ROW):		sState = SCAN_COLUMN;
 								highlightKeys(sKeyStartIndex, sKeyEndIndex, false);
 								AutomaticScan.resetTimer();
 								break;
 			case(SCAN_COLUMN):	sState = SCAN_CLICK;
 								IMEAdapter.selectHighlighted();
 								AutomaticScan.setExtendedTimer();
 								break;
 			case(SCAN_CLICK):	IMEAdapter.selectHighlighted();
 								AutomaticScan.setExtendedTimer();
 								break;
 			default:			break;
 			}
 		}
 		
 		private static int getCurrentKeyIndex() {
 			return sCurrentColumn;
 		}
 
 		private static int getCurrentRowIndex() {
 			return sCurrentRow;
 		}
 		
 		private static boolean setKey(int index) {
 			if(index < 0 || index >= sKeys.size()) return false;
 			sCurrentColumn = index;
 			return true;
 		}
 
 		private static boolean setKeyRow(int index) {
 			if(index < 0 || index >= IMEStates.sRowCount) return false;
 			sCurrentRow = index;
 			return true;
 		}
 		
 		private static int scanNextKey() {
 			if(sCurrentColumn == -1) sCurrentColumn = sKeyStartIndex;
 			else {
 				++sCurrentColumn;
 				if(sCurrentColumn > sKeyEndIndex) sCurrentColumn = -1;
 			}
 			return sCurrentColumn;
 		}
 		
 		private static int scanPreviousKey() {
 			if(sCurrentColumn == -1) sCurrentColumn = sKeyEndIndex;
 			else {
 				--sCurrentColumn;
 				if(sCurrentColumn < sKeyStartIndex) sCurrentColumn = -1;
 			}
 			return sCurrentColumn;
 		}
 		
 		private static void scanNextRow() {
 			++sCurrentRow;
 			sCurrentRow %= sRowCount;
 			updateRowKeyIndices();
 		}
 		
 		private static void scanPreviousRow() {
 			if(sCurrentRow == 0) sCurrentRow = sRowCount - 1;
 			else --sCurrentRow;
 			updateRowKeyIndices();
 		}
 		
 		private static void updateRowKeyIndices() {
 			sKeyStartIndex = getRowStart(sCurrentRow);
 			sKeyEndIndex = getRowEnd(sCurrentRow);			
 		}
 		
 		private static int getRowStart(int rowNumber) {
 			if(sKeyboard == null || rowNumber == -1) return -1;
 			int keyCounter = 0;
 			if (rowNumber != 0) {
 				List<Key> keyList = sKeyboard.getKeys();
 				Key key;
 				int rowCounter = 0;
 				int prevCoord = keyList.get(0).y;
 				int thisCoord;
 				while (rowCounter != rowNumber) {
 					keyCounter++;
 					key = keyList.get(keyCounter);
 					thisCoord = key.y;
 					if (thisCoord != prevCoord) {
 						// Changed rows
 						rowCounter++;
 						prevCoord = thisCoord;
 					}
 				}
 			}
 			return keyCounter;
 		}
 
 		private static int getRowEnd(int rowNumber) {
 			if(sKeyboard == null || rowNumber == -1) return -1;
 			List<Key> keyList = sKeyboard.getKeys();
 			int totalKeys = keyList.size();
 			int keyCounter = 0;
 			if (rowNumber == (getRowCount() - 1)) {
 				keyCounter = totalKeys - 1;
 			} else {
 				Key key;
 				int rowCounter = 0;
 				int prevCoord = keyList.get(0).y;
 				int thisCoord;
 				while (rowCounter <= rowNumber) {
 					keyCounter++;
 					key = keyList.get(keyCounter);
 					thisCoord = key.y;
 					if (thisCoord != prevCoord) {
 						// Changed rows
 						rowCounter++;
 						prevCoord = thisCoord;
 					}
 				}
 				keyCounter--;
 			}
 			return keyCounter;
 		}
 
 		private static int getRowCount() {
 			if(sKeyboard == null) return 0;
 			List<Key> keyList = sKeyboard.getKeys();
 			Key key;
 			int rowCounter = 0;
 			int coord = 0;
 			for (Iterator<Key> i = keyList.iterator(); i.hasNext();) {
 				key = i.next();
 				if (rowCounter == 0) {
 					rowCounter++;
 					coord = key.y;
 				}
 				if (coord != key.y) {
 					rowCounter++;
 					coord = key.y;
 				}
 			}
 			return rowCounter;
 		}
 			
 	}
 	
 }
