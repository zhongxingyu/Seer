 /*
  * Copyright (C) 2013 The ChameleonOS Project
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
 
 package com.android.mms.ui;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.support.v4.widget.SlidingPaneLayout;
 
 import android.view.View;
 import com.android.mms.R;
 import com.android.mms.data.Conversation;
 
 /**
  * @author Clark Scheff
  */
 public class MmsActivity extends Activity {
     SlidingPaneLayout mSlidingPane;
     ConversationListFragment mConversationListFragment;
     ComposeMessageFragment mComposeMessageFragment;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.mms_activity);
 
         mSlidingPane = (SlidingPaneLayout)findViewById(R.id.slidingpanelayout);
         Resources res = getResources();
         int parallax = res.getDimensionPixelSize(R.dimen.pane_parallax);
         mSlidingPane.setParallaxDistance(parallax);
         mSlidingPane.setSliderFadeColor(res.getColor(R.color.slider_fade_color));
         mSlidingPane.setCoveredFadeColor(res.getColor(R.color.covered_fade_color));
         mSlidingPane.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
             @Override
             public void onPanelSlide(View view, float v) {
             }
 
             @Override
             public void onPanelOpened(View view) {
 
                 switch (view.getId()) {
                     case R.id.fragment_secondpane:
                         //mConversationListFragment.setHasOptionsMenu(true);
                         //mComposeMessageFragment.setHasOptionsMenu(false);
                         mConversationListFragment.setActive(true);
                         mComposeMessageFragment.setActive(false);
                         break;
                     default:
                         break;
                 }
             }
 
             @Override
             public void onPanelClosed(View view) {
 
                 switch (view.getId()) {
                     case R.id.fragment_secondpane:
                         //mConversationListFragment.setHasOptionsMenu(false);
                         //mComposeMessageFragment.setHasOptionsMenu(true);
                         mConversationListFragment.setActive(false);
                         mComposeMessageFragment.setActive(true);
                         break;
                     default:
                         break;
                 }
             }
         });
     }
 
     @Override
     public void onBackPressed() {
         if (mSlidingPane.isOpen())
             super.onBackPressed();
         else
             mSlidingPane.openPane();
     }
 
     public void openConversation(long threadId) {
         if (threadId == 0)
             mComposeMessageFragment.newConversation();
         else
             mComposeMessageFragment.openConversation(threadId);
         mSlidingPane.closePane();
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         mConversationListFragment = (ConversationListFragment)getFragmentManager()
                 .findFragmentById(R.id.fragment_firstpane);
         mComposeMessageFragment = (ComposeMessageFragment)getFragmentManager()
                 .findFragmentById(R.id.fragment_secondpane);
 
         Intent intent = getIntent();
         onNewIntent(intent);
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         setIntent(intent);
         String action = intent.getAction();
         if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
             mSlidingPane.closePane();
             mComposeMessageFragment.setActive(true);
             mComposeMessageFragment.newConversation();
             mComposeMessageFragment.handleSendIntent();
         } else if (intent.getBooleanExtra("forwarded_message", false)) {
             mSlidingPane.closePane();
             mComposeMessageFragment.setActive(true);
             mComposeMessageFragment.newConversation();
             mComposeMessageFragment.handleForwardedMessage();
         } else if (intent.getData() != null || intent.getLongExtra(ComposeMessageFragment.THREAD_ID, 0) > 0) {
             mSlidingPane.closePane();
             mComposeMessageFragment.setActive(true);
             mComposeMessageFragment.initFragmentState(intent);
         } else {
             mSlidingPane.openPane();
             mConversationListFragment.setActive(true);
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         mComposeMessageFragment.onActivityResult(requestCode, resultCode, data);
     }
 
     /**
      * If emojis are enabled we will show the emoji dialog, otherwise show the smiley dialog
      * @param v
      */
     public void insertEmoji(View v) {
         mComposeMessageFragment.insertEmoji(v);
     }
 
     public static Intent createIntent(Context context, long threadId) {
         Intent intent = new Intent(context, MmsActivity.class);
 
         if (threadId > 0) {
             intent.setData(Conversation.getUri(threadId));
         }
 
         return intent;
     }
 
     public void openPane() {
         mSlidingPane.openPane();
     }
 
     public void closePane() {
         mSlidingPane.closePane();
     }
 }
