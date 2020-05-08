 package com.chess.genesis;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.Spinner;
 import android.widget.TextView.BufferType;
 
 class NewOnlineGameDialog extends Dialog implements OnClickListener, OnCheckedChangeListener
 {
 	public final static int MSG = 100;
 
 	private Handler handle;
 	private Spinner spinner;
 
 	public NewOnlineGameDialog(Context context, Handler handler)
 	{
 		super(context);
 
 		handle = handler;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		setTitle("New Online Game");
 
 		setContentView(R.layout.newnetworkgame);
 
 		Button button = (Button) findViewById(R.id.newgame_ok);
 		button.setOnClickListener(this);
 
 		button = (Button) findViewById(R.id.newgame_cancel);
 		button.setOnClickListener(this);
 
 		RadioGroup group = (RadioGroup) findViewById(R.id.radio_group);
 		group.setOnCheckedChangeListener(this);
 
 		AdapterItem[] list = new AdapterItem[]
 			{new AdapterItem("Genesis", Enums.GENESIS_CHESS),
 			 new AdapterItem("Regular", Enums.REGULAR_CHESS) };
 
 		ArrayAdapter<AdapterItem> adapter = new ArrayAdapter<AdapterItem>(this.getContext(), android.R.layout.simple_spinner_item, list);
 
 		spinner = (Spinner) findViewById(R.id.game_type);
 		spinner.setAdapter(adapter);
 	}
 
 	public void onClick(View v)
 	{
 		switch (v.getId()) {
 		case R.id.newgame_ok:
 			Bundle data = new Bundle();
 
 			data.putInt("gametype", ((AdapterItem) spinner.getSelectedItem()).id);
 			data.putInt("opponent", Enums.RANDOM); // Enums.INVITE
 			// data.putString("opp_name", name);
 			handle.sendMessage(handle.obtainMessage(MSG, data));
 		}
 		dismiss();
 	}
 
 	public void onCheckedChanged(RadioGroup group, int checkedId)
 	{
 		EditText text = (EditText) findViewById(R.id.opp_name);
 		boolean state = false;
 
 		switch (checkedId) {
 		case R.id.random_opp:
			state = false;
 			text.setText("");
 			break;
 		case R.id.invite_opp:
 			state = true;
 			break;
 		}
 		text.setEnabled(state);
 	}
 }
