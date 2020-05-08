 package com.devcamp.messagetimer.presenter;
 
 import android.view.View;
 import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
 
 import com.devcamp.messagetimer.MessageListActivity;
 
 public class MessageListPresenter {
 private MessageListActivity mActivity = null;
 	
 	public MessageListPresenter(MessageListActivity activity)
 	{
 		mActivity = activity;
 		bind();
 	}
 	
 	private void bind(){
		mActivity.getListView().setOnItemClickListener(new OnItemClickListener()
 		{

 			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
 			{
 				onMessageClick();
 			}
 		});
 	}
 	
 	private void onMessageClick() {
 		// TODO Auto-generated method stub
 	}
 }
