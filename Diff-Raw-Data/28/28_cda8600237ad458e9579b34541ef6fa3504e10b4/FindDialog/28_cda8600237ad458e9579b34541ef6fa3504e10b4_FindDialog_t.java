 /*
  * Copyright (C) 2007 The Android Open Source Project
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
 
 package com.android.browser;
 
 import android.content.Context;
 import android.text.Editable;
 import android.text.Selection;
 import android.text.Spannable;
 import android.text.TextWatcher;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.AnimationUtils;
 import android.view.inputmethod.InputMethodManager;
 import android.webkit.WebView;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /* package */ class FindDialog extends LinearLayout implements TextWatcher {
     private WebView         mWebView;
     private TextView        mMatches;
     private BrowserActivity mBrowserActivity;
     
     // Views with which the user can interact.
     private EditText        mEditText;
     private View            mNextButton;
     private View            mPrevButton;
     private View            mMatchesView;
 
     // When the dialog is opened up with old text, enter needs to be pressed
     // (or the text needs to be changed) before WebView.findAll can be called.
     // Once it has been called, enter should move to the next match.
     private boolean         mMatchesFound;
     private View.OnClickListener mFindListener = new View.OnClickListener() {
         public void onClick(View v) {
             findNext();
         }
     };
 
     private View.OnClickListener mFindCancelListener  = 
             new View.OnClickListener() {
         public void onClick(View v) {
             mBrowserActivity.closeFind();
         }
     };
     
     private View.OnClickListener mFindPreviousListener  = 
             new View.OnClickListener() {
         public void onClick(View v) {
             if (mWebView == null) {
                 throw new AssertionError("No WebView for FindDialog::onClick");
             }
             mWebView.findNext(false);
             hideSoftInput();
         }
     };
 
     /*
      * Remove the soft keyboard from the screen.
      */
     private void hideSoftInput() {
         InputMethodManager imm = (InputMethodManager)
                 mBrowserActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
     }
 
     private void disableButtons() {
         mPrevButton.setEnabled(false);
         mNextButton.setEnabled(false);
         mPrevButton.setFocusable(false);
         mNextButton.setFocusable(false);
     }
 
     /* package */ void setWebView(WebView webview) {
         mWebView = webview;
     }
 
     /* package */ FindDialog(BrowserActivity context) {
         super(context);
         mBrowserActivity = context;
 
         LayoutInflater factory = LayoutInflater.from(context);
         factory.inflate(R.layout.browser_find, this);
 
         mEditText = (EditText) findViewById(R.id.edit);
         
         View button = findViewById(R.id.next);
         button.setOnClickListener(mFindListener);
         mNextButton = button;
         
         button = findViewById(R.id.previous);
         button.setOnClickListener(mFindPreviousListener);
         mPrevButton = button;
         
         button = findViewById(R.id.done);
         button.setOnClickListener(mFindCancelListener);
         
         mMatches = (TextView) findViewById(R.id.matches);
         mMatchesView = findViewById(R.id.matches_view);
         disableButtons();
 
     }
 
     /**
      * Called by BrowserActivity.closeFind.  Start the animation to hide
      * the dialog, inform the WebView that the dialog is being dismissed,
      * and hide the soft keyboard.
      */
     public void dismiss() {
         mWebView.notifyFindDialogDismissed();
         startAnimation(AnimationUtils.loadAnimation(mBrowserActivity,
                 R.anim.find_dialog_exit));
         hideSoftInput();
     }
 
     @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            KeyEvent.DispatcherState state = getKeyDispatcherState();
            if (state != null) {
                int action = event.getAction();
                if (KeyEvent.ACTION_DOWN == action
                        && event.getRepeatCount() == 0) {
                    state.startTracking(event, this);
                    return true;
                } else if (KeyEvent.ACTION_UP == action
                        && !event.isCanceled() && state.isTracking(event)) {
                    mBrowserActivity.closeFind();
                    return true;
                }
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }

    @Override
     public boolean dispatchKeyEvent(KeyEvent event) {
         int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_UP) {
             if (keyCode == KeyEvent.KEYCODE_ENTER
                     && mEditText.hasFocus()) {
                 if (mMatchesFound) {
                     findNext();
                 } else {
                     findAll();
                     // Set the selection to the end.
                     Spannable span = (Spannable) mEditText.getText();
                     Selection.setSelection(span, span.length());
                 }
                 return true;
             }
         }
         return super.dispatchKeyEvent(event);
     }
 
     private void findNext() {
         if (mWebView == null) {
             throw new AssertionError("No WebView for FindDialog::findNext");
         }
         mWebView.findNext(true);
         hideSoftInput();
     }
 
     public void show() {
         // In case the matches view is showing from a previous search
         mMatchesView.setVisibility(View.INVISIBLE);
         mMatchesFound = false;
         mEditText.requestFocus();
         Spannable span = (Spannable) mEditText.getText();
         int length = span.length();
         Selection.setSelection(span, 0, length);
         span.setSpan(this, 0, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
         setMatchesFound(0);
         disableButtons();
         startAnimation(AnimationUtils.loadAnimation(mBrowserActivity,
                 R.anim.find_dialog_enter));
         InputMethodManager imm = (InputMethodManager)
                 mBrowserActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.showSoftInput(mEditText, 0);
     }
     
     // TextWatcher methods
     public void beforeTextChanged(CharSequence s, 
                                   int start, 
                                   int count, 
                                   int after) {
     }
     
     public void onTextChanged(CharSequence s,  
                               int start, 
                               int before, 
                               int count) {
         if (mWebView == null) {
             throw new AssertionError(
                     "No WebView for FindDialog::onTextChanged");
         }
         findAll();
     }
 
     private void findAll() {
         CharSequence find = mEditText.getText();
         if (0 == find.length()) {
             disableButtons();
             mWebView.clearMatches();
             mMatchesView.setVisibility(View.INVISIBLE);
         } else {
             mMatchesView.setVisibility(View.VISIBLE);
             int found = mWebView.findAll(find.toString());
             mMatchesFound = true;
             setMatchesFound(found);
             if (found < 2) {
                 disableButtons();
                 if (found == 0) {
                     setMatchesFound(0);
                 }
             } else {
                 mPrevButton.setFocusable(true);
                 mNextButton.setFocusable(true);
                 mPrevButton.setEnabled(true);
                 mNextButton.setEnabled(true);
             }
         }
     }
 
     private void setMatchesFound(int found) {
         String template = mBrowserActivity.getResources().
                 getQuantityString(R.plurals.matches_found, found, found);
 
         mMatches.setText(template);
     }
 
     public void afterTextChanged(Editable s) {
     }
 }
