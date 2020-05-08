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
 
 class NewLocalGameDialog extends Dialog implements OnClickListener
 {
 	public final static int MSG = 102;
 
 	private Handler handle;
 	private Spinner gametype_spin;
 	private Spinner opponent_spin;
 
 	public NewLocalGameDialog(Context context, Handler handler)
 	{
 		super(context);
 
 		handle = handler;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		setTitle("New Local Game");
 
 		setContentView(R.layout.newlocalgame);
 
 		Button button = (Button) findViewById(R.id.newgame_ok);
 		button.setOnClickListener(this);
 
 		button = (Button) findViewById(R.id.newgame_cancel);
 		button.setOnClickListener(this);
 
 		AdapterItem[] list = new AdapterItem[]
 			{new AdapterItem("Genesis", Enums.GENESIS_CHESS),
 			 new AdapterItem("Regular", Enums.REGULAR_CHESS) };
 
 		ArrayAdapter<AdapterItem> adapter = new ArrayAdapter<AdapterItem>(this.getContext(), android.R.layout.simple_spinner_item, list);
 
 		gametype_spin = (Spinner) findViewById(R.id.game_type);
 		gametype_spin.setAdapter(adapter);
 
 		list = new AdapterItem[] {new AdapterItem("Human", Enums.HUMAN_OPPONENT),
 			 new AdapterItem("Computer", Enums.COMPUTER_OPPONENT) };
 
 		adapter = new ArrayAdapter<AdapterItem>(this.getContext(), android.R.layout.simple_spinner_item, list);
 
 		opponent_spin = (Spinner) findViewById(R.id.opponent);
 		opponent_spin.setAdapter(adapter);
 	}
 
 	public void onClick(View v)
 	{
 		switch (v.getId()) {
 		case R.id.newgame_ok:
 			Bundle data = new Bundle();
 			EditText text = (EditText) findViewById(R.id.game_name);
 
 			data.putString("name", text.getText().toString());
 			data.putInt("gametype", ((AdapterItem) gametype_spin.getSelectedItem()).id);
 			data.putInt("opponent", ((AdapterItem) opponent_spin.getSelectedItem()).id);
 
 			handle.sendMessage(handle.obtainMessage(MSG, data));
 		}
 		dismiss();
 	}
 }
