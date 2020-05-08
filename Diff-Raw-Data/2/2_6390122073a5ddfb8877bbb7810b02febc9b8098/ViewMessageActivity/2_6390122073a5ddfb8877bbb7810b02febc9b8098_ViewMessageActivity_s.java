 package com.phdroid.smsb.activity;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Gallery;
 import com.phdroid.smsb.R;
 import com.phdroid.smsb.SmsPojo;
 import com.phdroid.smsb.application.ApplicationController;
 import com.phdroid.smsb.application.NewSmsEvent;
 import com.phdroid.smsb.application.NewSmsEventListener;
 import com.phdroid.smsb.storage.IMessageProvider;
 import com.phdroid.smsb.storage.MessageProviderHelper;
 import com.phdroid.smsb.ui.EventInjectedActivity;
 
 import java.util.List;
 
 /**
  * Show detailed sms message with several control functions.
  */
 public class ViewMessageActivity extends EventInjectedActivity {
 	private Gallery mGallery;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.view_message);
 
 		dataBind();
 		mGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
 				SmsPojo sms = getMessageProvider().getMessageByOrdinal(i);
 				getMessageProvider().read(sms);
 				updateTitle();
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> adapterView) {
 			}
 		});
 
 		ApplicationController app = (ApplicationController)this.getApplicationContext();
 		app.attachNewSmsListener(new NewSmsEventListener() {
 			@Override
 			public void onNewSms(NewSmsEvent newSmsEvent) {
 				Log.v(this.getClass().getSimpleName(), "onNewSms");
 				MessageProviderHelper.invalidCache();
 				dataBind();
 			}
 		});
 
 		Bundle b = getIntent().getExtras();
 		long id = b.getLong("id", -1);
 		int position = b.getInt("position", -1);
 		if (id >= 0) {
			getMessageProvider().read(id);
 			mGallery.setSelection(position, false);
 
 			updateTitle();
 		}
 	}
 
 	@Override
 	public void dataBind() {
 		super.dataBind();
 		mGallery = (Gallery)findViewById(R.id.message_gallery);
 		int currentIndex = mGallery.getSelectedItemPosition();
 		mGallery.setAdapter(new SmsPojoSpinnerAdapter(this, getMessageProvider().getMessages()));
 		mGallery.setSelection(currentIndex);
 
 		Log.v(this.getClass().getSimpleName(), "DataBind");
 	}
 
 	private void updateTitle() {
 		setTitle(String.format(
 				"%s%s",
 				getString(R.string.app_name),
 				getMessageProvider().getUnreadCount() > 0 ?
 						String.format(" (%s)", Integer.toString(getMessageProvider().getUnreadCount())) : ""));
 	}
 
 	protected IMessageProvider getMessageProvider() {
 		return MessageProviderHelper.getMessageProvider(getContentResolver());
 	}
 
 
 	public void deleteClick(View view) {
 		long id = mGallery.getSelectedItemId();
 		getMessageProvider().delete(id);
 		finish();
 	}
 
 	public void notSpamClick(View view) {
 		long id = mGallery.getSelectedItemId();
 		getMessageProvider().notSpam(id);
 		//todo: move message to Android SMS storage
 		finish();
 	}
 
 	public void replyClick(View view) {
 		long id = mGallery.getSelectedItemId();
 		SmsPojo sms = getMessageProvider().getMessage(id);
 
 		getMessageProvider().notSpam(id);
 		//todo: move message to Android SMS storage
 		finish();
 
 		Intent intent = new Intent(Intent.ACTION_VIEW);
 		intent.putExtra("address", sms.getSender());
 		intent.setType("vnd.android-dir/mms-sms");
 		startActivity(intent);
 	}
 }
