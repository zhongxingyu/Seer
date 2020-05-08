 package com.hyperactivity.android_app.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.hyperactivity.android_app.R;
 import com.hyperactivity.android_app.forum.models.Reply;
 import com.hyperactivity.android_app.forum.models.Thread;
 
 public class ViewThreadFragment extends Fragment {
 	
 	private ReplyListFragment replyList;
 	private Thread currentThread;
 	private TextView headlineField, textField;
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.view_thread_fragment, null);
 		
 		headlineField = (TextView)view.findViewById(R.id.thread_headline_field);
 		textField = (TextView)view.findViewById(R.id.thread_text_field);
 		
 		replyList = new ReplyListFragment();
		getFragmentManager().beginTransaction().replace(R.id.reply_list_container, replyList);
 		
 		return view;
 	}
 	
 	public void onResume() {
 		super.onResume();
 		if (currentThread != null) {
 			updateCurrentThread();
 		}
 	}
 	
 	public void setCurrentThread(Thread thread) {
 		currentThread = thread;
 		if (getActivity() != null) {
 			updateCurrentThread();
 		}
 	}
 	
 	public void updateReplies() {
 		List<Reply> replies = currentThread.getReplies();
 		if (replies != null) {
 			System.out.println(replies.size() + " replies found!");
 			replyList.updateReplyList(replies);
 		}
 	}
 	
 	private void updateCurrentThread() {
 		headlineField.setText(currentThread.getHeadLine());
 		textField.setText(currentThread.getText());
 	}
 }
