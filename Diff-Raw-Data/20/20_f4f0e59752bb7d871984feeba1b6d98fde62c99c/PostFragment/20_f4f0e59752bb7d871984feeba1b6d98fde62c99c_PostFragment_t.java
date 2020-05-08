 /**
  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  This code is licensed
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  */
 package edu.rit.csh.androidwebnews;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import java.util.ArrayList;
 
 public class PostFragment extends SherlockFragment {
     private ArrayList<String> contents;
     private String body;
     private String otherBody;
     final PostThread myThread;
     private HttpsConnector hc;
     private PostFragmentAdapter<String> listAdapter;
     private int me = 0;
     private int total = 0;
 
     public PostFragment(PostThread thread, int me, int total) {
         myThread = thread;
         this.me = me;
         this.total = total;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceBundle) {
         super.onCreate(savedInstanceBundle);
         hc = new HttpsConnector(getActivity());
         hc.getPostBody(myThread.getNewsgroup(), myThread.getNumber());
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         ListView mainListView = new ListView(getActivity());
         mainListView.setId(android.R.id.list);
         contents = new ArrayList<String>();
         body = "";
 
         contents.add(myThread.getNumber() + "");
         contents.add("Newsgroup: " + myThread.getNewsgroup());
         contents.add("Author Name: " + myThread.getAuthorName());
         contents.add("Subject: " + myThread.getSubject());
         contents.add("Post Date: " + myThread.getDate());
         swapBodies();
         contents.add("[Post loading...]");
         contents.add(myThread.getStarred() + "");
 
         listAdapter = new PostFragmentAdapter<String>(getActivity(), R.layout.rowlayout, contents);
 
         mainListView.setAdapter(listAdapter);
 
         mainListView.setOnItemClickListener(new OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> adapter, View arg1, int position,
                                     long id) {
                 Log.d("MyDebugging", "Clicky!");
                 if (position == 5) {
                     swapBodies();
                     contents.remove(5);
                     contents.add(5, body);
                     //listAdapter.clear();
                     //listAdapter.addAll(contents);
                     listAdapter.notifyDataSetChanged();
                 }
             }
         });
 
         return mainListView;
 
     }
 
     void swapBodies() {
         String temp = body;
         body = otherBody;
         otherBody = temp;
 
     }
 
     private void processBody() {
         String[] lines = otherBody.split("\n");
         boolean messageSet = false;
         body = "";
 
         for (String line : lines) {
             if (line.length() > 0) {
                 if (line.charAt(0) != '>') {
                     body += line + "\n";
                 } else {
 
                     if (!messageSet) {
                         body += "[Tap here to show hidden text]\n";
                         messageSet = true;
                     }
                 }
             } else
                 body += "\n";
         }
     }
 
     @Override
     public void setUserVisibleHint(boolean isVisibleToUser) {
         super.setUserVisibleHint(isVisibleToUser);
         if (isVisibleToUser && !myThread.getUnread().equals("null")) {
 
             myThread.setUnread("null");
             hc.markRead(myThread.getNewsgroup(), myThread.getNumber());
         }
         if (getActivity() != null)
             getActivity().setTitle("Post " + me + " of " + total);
     }
 
     public void update(String jsonstuff) {
         String otherBody = hc.getPostBodyFromString(jsonstuff);
         Log.d("jddebug", "body set");
         this.otherBody = otherBody;
         processBody();
         //swapBodies();
         contents.remove(5);
         contents.add(5, body);
         listAdapter.notifyDataSetChanged();
     }
 
 }
