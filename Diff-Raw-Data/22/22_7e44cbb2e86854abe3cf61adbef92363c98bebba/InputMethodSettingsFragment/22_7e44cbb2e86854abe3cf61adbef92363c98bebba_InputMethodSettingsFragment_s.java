 /*
  * Copyright (C) 2011 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.android.inputmethodcommon;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceFragment;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.text.TextUtils;
 import android.view.inputmethod.InputMethodInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.view.ViewGroup;
 
 import java.util.List;
 
 /**
  * This is a helper class for an IME's settings preference fragment. It's recommended for every
  * IME to have its own settings preference fragment which inherits this class.
  */
 public abstract class InputMethodSettingsFragment extends PreferenceFragment {
     private PreferenceCategory mInputMethodSettingsCategory;
     private Preference mSubtypeEnablerPreference;
     private int mInputMethodSettingsCategoryTitleRes;
     private CharSequence mInputMethodSettingsCategoryTitle;
     private int mSubtypeEnablerTitleRes;
     private CharSequence mSubtypeEnablerTitle;
     private int mSubtypeEnablerSummaryRes;
     private CharSequence mSubtypeEnablerSummary;
     private int mSubtypeEnablerIconRes;
     private Drawable mSubtypeEnablerIcon;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         final Context context = getActivity();
         final InputMethodManager imm =
                 (InputMethodManager) context.getSystemService(
                         Context.INPUT_METHOD_SERVICE);

         final InputMethodInfo imi = getMyImi(imm);
         if (imi != null && imi.getSubtypeCount() > 1) {
             mInputMethodSettingsCategory = new PreferenceCategory(context);
            mSubtypeEnablerPreference =
                    getPreferenceManager().createPreferenceScreen(context);
             mSubtypeEnablerPreference
                     .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                         @Override
                         public boolean onPreferenceClick(Preference preference) {
                            final Bundle bundle = new Bundle();
                            bundle.putString(Settings.EXTRA_INPUT_METHOD_ID, imi.getId());
                             return true;
                         }
                     });
            mInputMethodSettingsCategory.addPreference(mSubtypeEnablerPreference);
             getPreferenceScreen().addPreference(mInputMethodSettingsCategory);
         }
         updateSubtypeEnabler();
     }
 
     private InputMethodInfo getMyImi(InputMethodManager imm) {
         final List<InputMethodInfo> imis = imm.getInputMethodList();
         for (int i = 0; i < imis.size(); ++i) {
             final InputMethodInfo imi = imis.get(i);
             if (imis.get(i).getPackageName().equals(getActivity().getPackageName())) {
                 return imi;
             }
         }
         return null;
     }
 
     /**
      * Sets the title for the input method settings category with a resource ID.
      * @param resId The resource ID of the title.
      */
     public void setInputMethodSettingsCategoryTitle(int resId) {
         mInputMethodSettingsCategoryTitleRes = resId;
         updateSubtypeEnabler();
     }
 
     /**
      * Sets the title for the input method settings category with a CharSequence.
      * @param title The title for this preference.
      */
     public void setInputMethodSettingsCategoryTitle(CharSequence title) {
         mInputMethodSettingsCategoryTitleRes = 0;
         mInputMethodSettingsCategoryTitle = title;
         updateSubtypeEnabler();
     }
 
     /**
      * Sets the title for the input method enabler preference for launching subtype enabler with a
      * resource ID.
      * @param resId The resource ID of the title.
      */
     public void setSubtypeEnablerTitle(int resId) {
         mSubtypeEnablerTitleRes = resId;
         updateSubtypeEnabler();
     }
 
     /**
      * Sets the title for the input method enabler preference for launching subtype enabler with a
      * CharSequence.
      * @param title The title for this preference.
      */
     public void setSubtypeEnablerTitle(CharSequence title) {
         mSubtypeEnablerTitleRes = 0;
         mSubtypeEnablerTitle = title;
         updateSubtypeEnabler();
     }
 
     /**
      * Sets the summary for the inputmethod enabler preference for launching subtype enabler with a
      * resource ID.
      * @param resId The resource id of the summary for the preference.
      */
     public void setSubtypeEnablerSummary(int resId) {
         mSubtypeEnablerSummaryRes = resId;
         updateSubtypeEnabler();
     }
 
     /**
      * Sets the summary for the inputmethod enabler preference for launching subtype enabler with a
      * CharSequence.
      * @param summary The summary of the preference.
      */
     public void setSubtypeEnablerSummary(CharSequence summary) {
         mSubtypeEnablerSummaryRes = 0;
         mSubtypeEnablerSummary = summary;
         updateSubtypeEnabler();
     }
 
     /**
      * Sets the icon for the preference for launching subtype enabler with a resource ID.
      * @param resId The resource id of an optional icon for the preference.
      */
     public void setSubtypeEnablerIcon(int resId) {
         mSubtypeEnablerIconRes = resId;
         updateSubtypeEnabler();
     }
 
     /**
      * Sets the icon for the Preference for launching subtype enabler with a Drawable.
      * @param drawable The drawable of an optional icon for the preference.
      */
     public void setSubtypeEnablerIcon(Drawable drawable) {
         mSubtypeEnablerIconRes = 0;
         mSubtypeEnablerIcon = drawable;
         updateSubtypeEnabler();
     }
 
     private void updateSubtypeEnabler() {
         if (mSubtypeEnablerPreference != null) {
             if (mSubtypeEnablerTitleRes != 0) {
                 mSubtypeEnablerPreference.setTitle(mSubtypeEnablerTitleRes);
             } else if (!TextUtils.isEmpty(mSubtypeEnablerTitle)) {
                 mSubtypeEnablerPreference.setTitle(mSubtypeEnablerTitle);
             }
             if (mSubtypeEnablerSummaryRes != 0) {
                 mSubtypeEnablerPreference.setSummary(mSubtypeEnablerSummaryRes);
             } else if (!TextUtils.isEmpty(mSubtypeEnablerSummary)) {
                 mSubtypeEnablerPreference.setSummary(mSubtypeEnablerSummary);
             }
             if (mSubtypeEnablerIconRes != 0) {
                 mSubtypeEnablerPreference.setIcon(mSubtypeEnablerIconRes);
             } else if (mSubtypeEnablerIcon != null) {
                 mSubtypeEnablerPreference.setIcon(mSubtypeEnablerIcon);
             }
         }
         if (mInputMethodSettingsCategory != null) {
             if (mInputMethodSettingsCategoryTitleRes != 0) {
                 mInputMethodSettingsCategory.setTitle(mInputMethodSettingsCategoryTitleRes);
             } else if (!TextUtils.isEmpty(mInputMethodSettingsCategoryTitle)) {
                 mInputMethodSettingsCategory.setTitle(mInputMethodSettingsCategoryTitle);
             }
         }
     }
 }
