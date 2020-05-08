 package com.hyperactivity.android_app.activities;
 
 import java.util.List;
 
import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.hyperactivity.android_app.R;
 import com.hyperactivity.android_app.core.Engine;
 import com.hyperactivity.android_app.forum.models.Reply;
 import com.hyperactivity.android_app.forum.models.Thread;
 
 public class ViewThreadFragment extends Fragment {
 	
 	private int SINGLE_LINE_TEXT_HEIGHT;
 	private int MULTIPLE_LINE_TEXT_HEIGHT;
 	
 	private ReplyListFragment replyList;
 	private Thread currentThread;
 	private TextView headlineField, textField;
 	private EditText writeReplyField;
 	private LinearLayout writeReplyContainer;
 	private Button writeReplyButton;
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.view_thread_fragment, null);
 		
 		headlineField = (TextView)view.findViewById(R.id.thread_headline_field);
 		textField = (TextView)view.findViewById(R.id.thread_text_field);
 		writeReplyField = (EditText)view.findViewById(R.id.write_reply_field);
 		writeReplyContainer = (LinearLayout)view.findViewById(R.id.write_reply_container);
 		
 		// Needed to take focus from writeReplyField
 		headlineField.setFocusable(true);
 		headlineField.setFocusableInTouchMode(true);
 		
 		// TODO ugly
		SINGLE_LINE_TEXT_HEIGHT = 70;
 		MULTIPLE_LINE_TEXT_HEIGHT = SINGLE_LINE_TEXT_HEIGHT * 3;		
 		
 		writeReplyField.setOnFocusChangeListener(new OnFocusChangeListener() {
 			
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (hasFocus) {
 					expandWriteReply();
 				} else {
 					shrinkWriteReply();
 				}
 			}
 		});
 		
 		replyList = new ReplyListFragment();
 		getFragmentManager().beginTransaction().replace(R.id.reply_list_container, replyList).commit();
 		
 		OnClickListener removeFocusListener = new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				headlineField.requestFocus();
 			}
 		};
 		
 		view.setOnClickListener(removeFocusListener);
 		
 		return view;
 	}
 	
 	private void expandWriteReply() {
 		writeReplyField.setSingleLine(false);
 		writeReplyField.getLayoutParams().height = MULTIPLE_LINE_TEXT_HEIGHT;
 		writeReplyButton = new Button(getActivity());
 		writeReplyButton.setText(getActivity().getText(R.string.create_reply_button_text));
 		writeReplyButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if (writeReplyField.getText().toString().trim().length() > 0) {
 					((Engine)getActivity().getApplication()).getPublicForum().createReply(getActivity(), currentThread.getId(), writeReplyField.getText().toString().trim(), true);
 				}
 			}
 		});
 		writeReplyContainer.addView(writeReplyButton);
		
		// Show keyboard
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(writeReplyField, InputMethodManager.SHOW_IMPLICIT);
 	}
 	
 	private void shrinkWriteReply() {
 		writeReplyField.getLayoutParams().height = SINGLE_LINE_TEXT_HEIGHT;
 		writeReplyField.setSingleLine(true);
 		writeReplyContainer.removeView(writeReplyButton);
		
		// Hide keyboard
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(writeReplyField.getWindowToken(), 0);
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
 			replyList.updateReplyList(replies);
 		}
 	}
 	
 	private void updateCurrentThread() {
 		headlineField.setText(currentThread.getHeadLine());
 		textField.setText(currentThread.getText());
 		writeReplyField.setText("");
 	}
 }
