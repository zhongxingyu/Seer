 /*
  * Copyright (C) 2013 The MoKee OpenSource Project
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
 
 package com.mokee.setupwizard.setup;
 
 import com.mokee.setupwizard.R;
 import com.mokee.setupwizard.widget.InputMethodItem;
 
 import android.app.Fragment;
 import android.content.Context;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.inputmethod.InputMethodInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class InputMethodPage extends Fragment {
 
     private Context mContext;
 
     private List<InputMethodItem> mImList;
 
     private ListView mListView;
     private RadioGroup mImGroup;
 
     private String mEnabledIM;
     private String mDefaultIM;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         // TODO Should be ignored if only one input method exists.
         View view = inflater.inflate(R.layout.page_input, null);
 
         mContext = getActivity();
 
         mImList = new ArrayList<InputMethodItem>();
         mImGroup = (RadioGroup) view.findViewById(R.id.input_method_group);
 
         // get IM
         InputMethodManager manager = (InputMethodManager) mContext
                 .getSystemService(Context.INPUT_METHOD_SERVICE);
         List<InputMethodInfo> infoList = manager.getInputMethodList();
 
         mEnabledIM = Settings.Secure.getString(mContext.getContentResolver(),
                 Settings.Secure.ENABLED_INPUT_METHODS);
         mDefaultIM = Settings.Secure.getString(mContext.getContentResolver(),
                 Settings.Secure.DEFAULT_INPUT_METHOD);
 
         int count = (infoList == null ? 0 : infoList.size());
 
         if (count == 0) {
             mListView.setVisibility(View.GONE);
             TextView tvInfo = new TextView(mContext);
             LayoutParams params = new
                     LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
             tvInfo.setText(getResources().getString(R.string.page_im_none));
             tvInfo.setLayoutParams(params);
             tvInfo.setTextAppearance(mContext,
                     R.style.Content);
         } else {
             for (int i = 0; i < count; ++i) {
                 mImList.add(new InputMethodItem(mContext, infoList.get(i)));
             }
         }
         MkOnCheckedListener checkedListener = new MkOnCheckedListener();
         for (int i = 0; i < count; i++) {
             RadioButton button = new RadioButton(mContext);
             button.setText(mImList.get(i).getImLabel());
             button.setTag(mImList.get(i).getImPackage());
             button.setOnCheckedChangeListener(checkedListener);
             mImGroup.addView(button);
            if (mImList.get(i).getImPackage().equals(mDefaultIM)) {
                mImGroup.check(mImGroup.getChildAt(i).getId());
            }
         }
 
         return view;
     }
 
     private class MkOnCheckedListener implements OnCheckedChangeListener {
 
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
             String defaultTag = buttonView.getTag().toString();
             Settings.Secure.putString(mContext.getContentResolver(),
                     Settings.Secure.DEFAULT_INPUT_METHOD, defaultTag);
             if (!mEnabledIM.contains(defaultTag)) {
                 Settings.Secure.putString(mContext.getContentResolver(),
                         Settings.Secure.ENABLED_INPUT_METHODS, mEnabledIM
                                 + ":" + defaultTag);
             }
         }
     }
 }
