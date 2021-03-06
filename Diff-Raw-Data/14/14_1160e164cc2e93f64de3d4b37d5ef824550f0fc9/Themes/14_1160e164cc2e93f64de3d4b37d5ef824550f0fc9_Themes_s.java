 /***************************************************************************************
  * Copyright (c) 2011 Norbert Nagold <norbert.nagold@gmail.com>                         *
  *                                                                                      *
  * This program is free software; you can redistribute it and/or modify it under        *
  * the terms of the GNU General Public License as published by the Free Software        *
  * Foundation; either version 3 of the License, or (at your option) any later           *
  * version.                                                                             *
  *                                                                                      *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
  * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
  *                                                                                      *
  * You should have received a copy of the GNU General Public License along with         *
  * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
  ****************************************************************************************/
 
 package com.ichi2.themes;
 
 import com.ichi2.anki.R;
 import com.tomgibara.android.veecheck.util.PrefSettings;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Themes {
 
 	public final static int THEME_DEFAULT = 0;
 	public final static int THEME_ANDROID_LIGHT = 1;
 	public final static int THEME_BLUE= 2;
 
 	public final static int CALLER_STUDYOPTIONS = 1;
 	public final static int CALLER_DECKPICKER_DECK = 3;
 	public final static int CALLER_REVIEWER= 4;
 	public final static int CALLER_FEEDBACK= 5;
 	public final static int CALLER_DOWNLOAD_DECK= 6;
 
 	private static int mCurrentTheme = -1;
 	private static int mProgressbarsBackgroundColor;
 	private static int mProgressbarsFrameColor;
 	private static int mProgressbarsMatureColor;
 	private static int mProgressbarsYoungColor;
 	private static int mProgressbarsDeckpickerYoungColor;
 	private static int mReviewerBackground = 0;
 	private static int mReviewerProgressbar = 0;
 	private static int mFlashcardBorder = 0;
 	private static int mDeckpickerItemBorder = 0;
 	private static int mTitleStyle = 0;
 	private static int mTextViewStyle= 0;
 	private static int mWallpaper = 0;
 	private static int mBackgroundColor;
 	private static int mBackgroundDarkColor = 0;
 	private static int mDialogBackgroundColor = 0;
 	private static int mToastBackground = 0;
 	private static int[] mCardbrowserItemBorder;
 	private static int[] mChartColors;
 	private static int mPopupTopDark;
 	private static int mPopupCenterDark;
 	private static int mPopupCenterBright;
 	private static int mPopupCenterMedium;
 	private static int mPopupBottomDark;
 	private static int mPopupBottomBright;
 	private static int mPopupBottomMedium;
 	private static int mDividerHorizontalBright;
 	
 
 	public static void applyTheme(Context context) {
 		if (mCurrentTheme == -1) {
 			SharedPreferences preferences = PrefSettings.getSharedPrefs(context);
 			mCurrentTheme = Integer.parseInt(preferences.getString("theme", "2"));
 			switch (mCurrentTheme) {
 			case THEME_DEFAULT:
 				mDialogBackgroundColor = R.color.card_browser_background;
 				mCardbrowserItemBorder = new int[] {0, R.color.card_browser_marked, R.color.card_browser_suspended, R.color.card_browser_marked};
				mChartColors = new int[] {android.R.color.white, android.R.color.black};
 				mPopupTopDark = R.drawable.popup_top_dark;
 				mPopupCenterDark = R.drawable.popup_center_dark;
 				mPopupCenterBright = R.drawable.popup_center_bright;
 				mPopupCenterMedium = R.drawable.popup_center_medium;
 				mPopupBottomDark = R.drawable.popup_bottom_dark;
 				mPopupBottomBright = R.drawable.popup_bottom_bright;
 				mPopupBottomMedium = R.drawable.popup_bottom_medium;
 				mDividerHorizontalBright = R.drawable.blue_divider_horizontal_bright;
 				mBackgroundColor = R.color.white;
 				break;
 			case THEME_ANDROID_LIGHT:
 				mProgressbarsBackgroundColor = R.color.studyoptions_progressbar_background_light;
 				mProgressbarsFrameColor = R.color.studyoptions_progressbar_frame_light;
 				mProgressbarsMatureColor = R.color.studyoptions_progressbar_mature_light;
 				mProgressbarsYoungColor = R.color.studyoptions_progressbar_young_light;
 				mProgressbarsDeckpickerYoungColor = R.color.deckpicker_progressbar_young_light;
 				mDialogBackgroundColor = R.color.card_browser_background;
 				mCardbrowserItemBorder = new int[] {0, R.color.card_browser_marked, R.color.card_browser_suspended, R.color.card_browser_marked};
 				mReviewerProgressbar = mProgressbarsYoungColor;
				mChartColors = new int[] {Color.parseColor("#000000"), Color.parseColor("#ffffff")};
 				mPopupTopDark = R.drawable.popup_top_dark;
 				mPopupCenterDark = R.drawable.popup_center_dark;
 				mPopupCenterBright = R.drawable.popup_center_bright;
 				mPopupCenterMedium = R.drawable.popup_center_medium;
 				mPopupBottomDark = R.drawable.popup_bottom_dark;
 				mPopupBottomBright = R.drawable.popup_bottom_bright;
 				mPopupBottomMedium = R.drawable.popup_bottom_medium;
 				mDividerHorizontalBright = R.drawable.blue_divider_horizontal_bright;
 				mBackgroundColor = R.color.white;
 				break;				
 			case THEME_BLUE:
 				mProgressbarsBackgroundColor = R.color.studyoptions_progressbar_background_blue;
 				mProgressbarsFrameColor = R.color.studyoptions_progressbar_frame_light;
 				mProgressbarsMatureColor = R.color.studyoptions_progressbar_mature_light;
 				mProgressbarsYoungColor = R.color.studyoptions_progressbar_young_blue;
 				mProgressbarsDeckpickerYoungColor = R.color.deckpicker_progressbar_young_light;
 				mReviewerBackground = R.color.reviewer_background;
 				mFlashcardBorder = R.drawable.blue_bg_webview;
 				mDeckpickerItemBorder = R.drawable.blue_bg_deckpicker;
 				mTitleStyle = R.drawable.blue_title;
 				mTextViewStyle = R.drawable.blue_textview;
 				mWallpaper = R.drawable.blue_background;
 				mBackgroundColor = R.color.background_blue;
 				mToastBackground = R.drawable.blue_toast_frame;
 				mDialogBackgroundColor = R.color.background_dialog_blue;
 				mBackgroundDarkColor = R.color.background_dark_blue;
 				mReviewerProgressbar = R.color.reviewer_progressbar_session_blue;
 				mCardbrowserItemBorder = new int[] {R.drawable.blue_bg_cardbrowser, R.drawable.blue_bg_cardbrowser_marked, R.drawable.blue_bg_cardbrowser_suspended, R.drawable.blue_bg_cardbrowser_marked_suspended};
				mChartColors = new int[] {Color.parseColor("#000000"), Color.parseColor("#ffffff")};
 				mPopupTopDark = R.drawable.blue_popup_top_dark;
 				mPopupCenterDark = R.drawable.blue_popup_center_dark;
 				mPopupCenterBright = R.drawable.blue_popup_center_bright;
 				mPopupCenterMedium = R.drawable.blue_popup_center_medium;
 				mPopupBottomDark = R.drawable.blue_popup_bottom_dark;
 				mPopupBottomBright = R.drawable.blue_popup_bottom_bright;
 				mPopupBottomMedium = R.drawable.blue_popup_bottom_medium;
 				mDividerHorizontalBright = R.drawable.blue_divider_horizontal_bright;
 				break;
 			}
 		}
 		switch (mCurrentTheme) {
 		case THEME_DEFAULT:
 			context.setTheme(R.style.Theme_Black);
 			break;
 		case THEME_ANDROID_LIGHT:
 			context.setTheme(R.style.Theme_Light);
 			break;
 		case THEME_BLUE:
 			context.setTheme(R.style.Theme_Blue);
 			break;
 		}
 	}
 
 
 	public static void setContentStyle(View view, int caller) {
 		if (mCurrentTheme == THEME_DEFAULT) {
 			return;
 		}
 		switch (caller) {
 		case CALLER_STUDYOPTIONS:
 			((View) view.findViewById(R.id.studyoptions_progressbar1_border)).setBackgroundResource(mProgressbarsFrameColor);
 			((View) view.findViewById(R.id.studyoptions_progressbar2_border)).setBackgroundResource(mProgressbarsFrameColor);
 			((View) view.findViewById(R.id.studyoptions_global_limit_bars)).setBackgroundResource(mProgressbarsFrameColor);
 			((View) view.findViewById(R.id.studyoptions_progressbar4_border)).setBackgroundResource(mProgressbarsFrameColor);
 
 			((View) view.findViewById(R.id.studyoptions_bars_max)).setBackgroundResource(mProgressbarsBackgroundColor);
 			((View) view.findViewById(R.id.studyoptions_progressbar2_content)).setBackgroundResource(mProgressbarsBackgroundColor);
 			((View) view.findViewById(R.id.studyoptions_global_limit_bars_content)).setBackgroundResource(mProgressbarsBackgroundColor);
 			((View) view.findViewById(R.id.studyoptions_progressbar4_content)).setBackgroundResource(mProgressbarsBackgroundColor);
 
 			((View) view.findViewById(R.id.studyoptions_global_mat_limit_bar)).setBackgroundResource(mProgressbarsMatureColor);
 			((View) view.findViewById(R.id.studyoptions_global_mat_bar)).setBackgroundResource(mProgressbarsMatureColor);
 
 			((View) view.findViewById(R.id.studyoptions_global_limit_bar)).setBackgroundResource(mProgressbarsYoungColor);
 			((View) view.findViewById(R.id.studyoptions_global_bar)).setBackgroundResource(mProgressbarsYoungColor);
 			break;
 		case CALLER_DECKPICKER_DECK:
 			if (view.getId() == R.id.DeckPickerCompletionMat) {
 				view.setBackgroundResource(mProgressbarsFrameColor);
 			} else if (view.getId() == R.id.DeckPickerCompletionAll) {
 				view.setBackgroundResource(mProgressbarsDeckpickerYoungColor);
 			} else if (view.getId() == R.id.deckpicker_deck) {
 				view.setBackgroundResource(mDeckpickerItemBorder);
 			}
 			break;
 		case CALLER_REVIEWER:
 	        ((View)view.findViewById(R.id.main_layout)).setBackgroundResource(mReviewerBackground);
 	        ((View)view.findViewById(R.id.flashcard_border)).setBackgroundResource(mFlashcardBorder);
 	        ((View)view.findViewById(R.id.session_progress)).setBackgroundResource(mReviewerProgressbar);
 			break;
 		case CALLER_FEEDBACK:
 			((TextView)view).setTextColor(mProgressbarsFrameColor);
 			break;
 		case CALLER_DOWNLOAD_DECK:
 			view.setBackgroundResource(mDeckpickerItemBorder);
 			break;
 		}
 	}
 
 
 	public static void resetTheme(){
 		mCurrentTheme = -1;
 		mProgressbarsBackgroundColor = 0;
 		mProgressbarsFrameColor = 0;
 		mProgressbarsMatureColor = 0;
 		mProgressbarsYoungColor = 0;
 		mProgressbarsDeckpickerYoungColor = 0;
 		mReviewerBackground = 0;
 		mFlashcardBorder = 0;
 		mDeckpickerItemBorder = 0;
 		mTitleStyle = 0;
 		mTextViewStyle = 0;
 		mWallpaper = 0;
 		mBackgroundColor = 0;
 		mToastBackground = 0;
 	}
 
 
 	public static void setWallpaper(View view) {
 		setWallpaper(view, false);
 	}
 	public static void setWallpaper(View view, boolean solid) {
 		if (solid) {
 			view.setBackgroundResource(mBackgroundDarkColor);
 		} else {
 			view.setBackgroundResource(mWallpaper);
 		}
 	}
 
 
 	public static void setTextViewStyle(View view) {
 		view.setBackgroundResource(mTextViewStyle);
 	}
 
 
 	public static void setTitleStyle(View view) {
 		view.setBackgroundResource(mTitleStyle);
 	}
 
 
 	public static int getForegroundColor() {
 		return mProgressbarsFrameColor;
 	}
 
 
 	public static int getBackgroundColor() {
 		return mBackgroundColor;
 	}
 
 
 	public static int getDialogBackgroundColor() {
 		return mDialogBackgroundColor;
 	}
 
 
 	public static int getTheme() {
 		return mCurrentTheme;
 	}
 
 
 	public static int[] getCardBrowserBackground() {
 		return mCardbrowserItemBorder;
 	}
 
 
 	public static void showThemedToast(Context context, String text, boolean shortLength) {
 		Toast result = Toast.makeText(context, text, shortLength ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
 		if (mCurrentTheme >= THEME_BLUE) {
 			result.getView().setBackgroundResource(mToastBackground);
 		}
         result.show();
 	}
 
 
 	public static StyledDialog htmlOkDialog(Context context, String title, String text) {
 		StyledDialog.Builder builder = new StyledDialog.Builder(context);
         builder.setTitle(title);
         WebView view = new WebView(context);
         view.setBackgroundColor(context.getResources().getColor(mDialogBackgroundColor));
         view.loadDataWithBaseURL("", text, "text/html", "UTF-8", "");
         builder.setView(view);
         builder.setPositiveButton(context.getResources().getString(R.string.ok), null);
         builder.setCancelable(true);
         return builder.create();
 	}
 
 
 	public static void setStyledDialogBackgrounds(View main, int buttonNumbers) {
 		setStyledDialogBackgrounds(main, buttonNumbers, false);
 	}
 	public static void setStyledDialogBackgrounds(View main, int buttonNumbers, boolean brightCustomPanelBackground) {
 		((View) main.findViewById(R.id.topPanel)).setBackgroundResource(mPopupTopDark);
 		((View) main.findViewById(R.id.titleDivider)).setBackgroundResource(mDividerHorizontalBright);
 		((View) main.findViewById(R.id.contentPanel)).setBackgroundResource(mPopupCenterDark);
 		((View) main.findViewById(R.id.listViewPanel)).setBackgroundResource(mPopupCenterBright);
 		if (brightCustomPanelBackground) {
 			((View) main.findViewById(R.id.customPanel)).setBackgroundResource(mPopupCenterMedium);			
 		} else {
 			((View) main.findViewById(R.id.customPanel)).setBackgroundResource(mPopupCenterDark);
 		}
 		if (buttonNumbers == 0) {
 			((LinearLayout) main.findViewById(R.id.buttonPanel)).setVisibility(View.GONE);
 			if (((View) main.findViewById(R.id.customPanel)).getVisibility() != View.GONE) {
     		   ((View) main.findViewById(R.id.customPanel)).setBackgroundResource(mPopupBottomDark);
     	   } else if (((View) main.findViewById(R.id.listViewPanel)).getVisibility() != View.GONE) {
     		   ((View) main.findViewById(R.id.listViewPanel)).setBackgroundResource(mPopupBottomBright);
     	   } else if (((View) main.findViewById(R.id.contentPanel)).getVisibility() != View.GONE) {
     		   ((View) main.findViewById(R.id.contentPanel)).setBackgroundResource(mPopupBottomDark);
     	   }
         } else {
         	LinearLayout buttonPanel = (LinearLayout) main.findViewById(R.id.buttonPanel);
         	buttonPanel.setBackgroundResource(mPopupBottomMedium);
         }
 		if (buttonNumbers > 1) {
 			main.findViewById(R.id.rightSpacer).setVisibility(View.GONE);
 			main.findViewById(R.id.leftSpacer).setVisibility(View.GONE);
 		}
 	}
 
 	public static int[] getChartColors() {
 		return mChartColors;
 	}
 
 
 	public static int getNightModeCardBackground(Context context) {
 		switch (mCurrentTheme) {
 		case THEME_BLUE:
 			return context.getResources().getColor(R.color.reviewer_night_card_background);
 		default:
 			return Color.BLACK;
 		}
 	}
 }
