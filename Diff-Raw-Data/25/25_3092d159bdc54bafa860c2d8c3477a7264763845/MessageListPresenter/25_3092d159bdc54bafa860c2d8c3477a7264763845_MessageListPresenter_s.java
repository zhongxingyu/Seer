 package com.devcamp.messagetimer.presenter;
 
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import com.devcamp.messagetimer.MessageListActivity;
 
 public class MessageListPresenter {
 private MessageListActivity mActivity = null;
 	
 	public MessageListPresenter(MessageListActivity activity)
 	{
 		mActivity = activity;
 		bind();
 	}
 	
 	private void bind(){
		mActivity.getListView().setOnClickListener(new OnClickListener()
 		{
 			@Override
			public void onClick(View v)
 			{
 				onMessageClick();
 			}
 		});
 	}
 	
 	private void onMessageClick() {
 		// TODO Auto-generated method stub
 	}
 }
