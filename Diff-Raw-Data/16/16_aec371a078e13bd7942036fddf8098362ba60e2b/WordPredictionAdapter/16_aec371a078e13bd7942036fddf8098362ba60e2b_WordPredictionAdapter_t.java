 package com.android.tecla.keyboard;
 
 import android.graphics.Color;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class WordPredictionAdapter {
 
 	private static final int[] SUGGESTIONSVIEWINDICES = {0,2,4};
 	private static final int BACKGROUND_HIGHLIGHT_COLOR = Color.BLUE;
	private static final int BACKGROUND_NORMAL_COLOR = Color.DKGRAY;
 	
 	private static ViewGroup sSuggestionsViewGroup = null;
 	
 	public static void setSuggestionsViewGroup(ViewGroup vg) {
 		sSuggestionsViewGroup = vg;
 	}
 	
 	public static void selectHighlighted() {
 		if(sSuggestionsViewGroup == null) return;
 		if(sSuggestionsViewGroup.getVisibility() != ViewGroup.VISIBLE) return;
 		WordPredictionStates.click();
 	}
 
 	public static void highlightNext() {
 		if(sSuggestionsViewGroup == null) return;
 		if(sSuggestionsViewGroup.getVisibility() != ViewGroup.VISIBLE) return;
 		WordPredictionStates.scanNext();
 	}
 
 	public static void highlightPrevious() {
 		if(sSuggestionsViewGroup == null) return;
 		if(sSuggestionsViewGroup.getVisibility() != ViewGroup.VISIBLE) return;
 		WordPredictionStates.scanPrevious();
 	}
 	
 	private static void highlightSuggestion(int index, boolean highlight) {
 		if(index < 0 || index >= SUGGESTIONSVIEWINDICES.length) return;
 		View view = sSuggestionsViewGroup.getChildAt(SUGGESTIONSVIEWINDICES[index]);
 		if(highlight) {
 			view.setBackgroundColor(BACKGROUND_HIGHLIGHT_COLOR);
 		} else {
 			view.setBackgroundColor(BACKGROUND_NORMAL_COLOR);
 		}
 	}
 	
 	private static class WordPredictionStates {
 		private static final int WPSCAN_NONE = 0x6666;
 		private static final int WPSCAN_HIGHLIGHTED = 0x7777;
 		private static final int WPSCAN_SUGGESTIONS = 0x8888;
 		private static final int WPSCAN_MORESUGGESTIONS = 0x9999;
 		private static final int WPSCAN_CLICK = 0xaaaa;
 		private static int sState = WPSCAN_NONE;
 		
 		private static int sCurrentIndex = -1;
 		
 		private static void reset() {
 			sState = WPSCAN_HIGHLIGHTED;
 			sCurrentIndex = -1;
 		}
 		
 		private static void scanNext() {
 			switch(sState) {
 			case(WPSCAN_NONE):		highlightSuggestion(0, true);
 									highlightSuggestion(1, true);
 									highlightSuggestion(2, true);
 									sState = WPSCAN_HIGHLIGHTED;
 									break;
 			case(WPSCAN_HIGHLIGHTED):		highlightSuggestion(0, false);
 											highlightSuggestion(1, false);
 											highlightSuggestion(2, false);
 											sState = WPSCAN_NONE;
 											break;
 			case(WPSCAN_SUGGESTIONS):		highlightSuggestion(sCurrentIndex++, false);
 											sCurrentIndex %= SUGGESTIONSVIEWINDICES.length + 1;
 											highlightSuggestion(sCurrentIndex, true);
 											break;
 			default:						break;
 			}
 		}
 		
 		private static void scanPrevious() {
 			switch(sState) {
 			case(WPSCAN_NONE):		highlightSuggestion(0, true);
 									highlightSuggestion(1, true);
 									highlightSuggestion(2, true);
 									sState = WPSCAN_HIGHLIGHTED;
 									break;
 			case(WPSCAN_HIGHLIGHTED):		highlightSuggestion(0, false);
 											highlightSuggestion(1, false);
 											highlightSuggestion(2, false);
 											sState = WPSCAN_NONE;
 			case(WPSCAN_SUGGESTIONS):		highlightSuggestion(sCurrentIndex--, false);
 											if (sCurrentIndex < 0)
 												sCurrentIndex = SUGGESTIONSVIEWINDICES.length;
 											highlightSuggestion(sCurrentIndex, true);
 			default:						break;
 			}
 		}
 		
 		private static void click() {
 			switch(sState) {
 			case(WPSCAN_NONE):				break;
 			case(WPSCAN_HIGHLIGHTED):		highlightSuggestion(0, false);
 											highlightSuggestion(1, false);
 											highlightSuggestion(2, false);
 											sState = WPSCAN_SUGGESTIONS;
 											sCurrentIndex = 0;
 											highlightSuggestion(sCurrentIndex, true);
 											break;
 			case(WPSCAN_SUGGESTIONS):		highlightSuggestion(sCurrentIndex, false);
 											sState = WPSCAN_NONE;
 											// TODO: choose word
 											break;
 			default:						break;
 			}
 		}
 		
 		
 	}
 }
