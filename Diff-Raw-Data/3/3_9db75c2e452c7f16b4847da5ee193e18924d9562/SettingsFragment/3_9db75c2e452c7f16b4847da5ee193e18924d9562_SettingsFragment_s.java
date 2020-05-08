 package com.tassadar.lorrismobile.yunicontrol;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 
 import com.tassadar.lorrismobile.BlobInputStream;
 import com.tassadar.lorrismobile.BlobOutputStream;
 import com.tassadar.lorrismobile.R;
 
 
 public class SettingsFragment extends YuniControlFragment implements OnCheckedChangeListener, OnClickListener {
 
     @Override
     public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.yunicontrol_settings, container, false);
 
         EditText t = (EditText)v.findViewById(R.id.get_data_period);
         t.addTextChangedListener(m_delayWatcher);
         t = (EditText)v.findViewById(R.id.get_data_mask);
         t.addTextChangedListener(m_maskWatcher);
 
         CompoundButton c = (CompoundButton)v.findViewById(R.id.send_req_toggle);
         c.setOnCheckedChangeListener(this);
 
         Button b = (Button)v.findViewById(R.id.start_cal_btn);
         b.setOnClickListener(this);
         b.setEnabled(m_protocol.getCurBoard() != null);
         return v;
     }
 
     @Override
     public void onStart() {
         super.onStart();
         retrieveValues();
     }
 
     private void retrieveValues() {
         View v = getView();
         if(v == null)
             return;
 
         EditText t = (EditText)v.findViewById(R.id.get_data_period);
         t.setText(String.valueOf((m_protocol.getDataDelay())));
         t = (EditText)v.findViewById(R.id.get_data_mask);
         t.setText("0x" + Integer.toHexString((m_protocol.getDataMask())));
 
         CompoundButton c = (CompoundButton)v.findViewById(R.id.send_req_toggle);
         c.setChecked(m_protocol.getDataEnabled());
     }
 
     @Override
     public void onPacketReceived(Packet pkt) { }
 
     @Override
     public void onInfoRequested() { }
 
     @Override
     public void onInfoReceived(GlobalInfo i) {
         View v = getView();
         if(v == null)
             return;
 
         Button b = (Button)v.findViewById(R.id.start_cal_btn);
         b.setEnabled(i != null);
     }
 
     @Override
     public void onBoardChange(BoardInfo b) { }
 
     @Override
     public void onClick(View v) {
         FragmentActivity act = (FragmentActivity)getActivity();
         if(act == null)
             return;
 
         FragmentManager m = act.getSupportFragmentManager();
         CalibrationFragment f= new CalibrationFragment();
         f.setProtocol(m_protocol);
         f.show(m, "cal_dialog");
     }
 
     @Override
     public void onCheckedChanged(CompoundButton v, boolean checked) {
         m_protocol.setGetDataEnabled(checked);
 
         Activity act = getActivity();
         if(act != null) {
             SharedPreferences.Editor e = act.getPreferences(0).edit();
             e.putBoolean("yc_getDataEnabled", checked);
             e.commit();
         }
     }
 
     private final TextWatcher m_delayWatcher = new TextWatcher() {
         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) {
             try {
                 int i = Integer.decode(s.toString());
                 if(i <= 0)
                     return;
 
                 m_protocol.setGetDataDelay(i);
 
                 Activity act = getActivity();
                 if(act != null) {
                     SharedPreferences.Editor e = act.getPreferences(0).edit();
                     e.putInt("yc_getDataDelay", i);
                     e.commit();
                 }
             } catch(NumberFormatException e) {
                 e.printStackTrace();
             }
         }
         @Override
         public void afterTextChanged(Editable s) { }
         @Override
         public void beforeTextChanged(CharSequence arg0, int start, int count, int after) { }
     };
 
     private final TextWatcher m_maskWatcher = new TextWatcher() {
         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) {
             try {
                 m_protocol.setGetDataMask(Integer.decode(s.toString()));
             } catch(NumberFormatException e) {
                 e.printStackTrace();
             }
         }
         @Override
         public void afterTextChanged(Editable s) { }
         @Override
         public void beforeTextChanged(CharSequence arg0, int start, int count, int after) { }
     };
 
     public void saveDataStream(BlobOutputStream str) {
         str.writeBool("enableGetData", m_protocol.getDataEnabled());
         str.writeInt("getDataDelay", m_protocol.getDataDelay());
         str.writeInt("getDataMask", m_protocol.getDataMask());
     }
 
     public void loadDataStream(BlobInputStream str) {
         m_protocol.setGetDataEnabled(str.readBool("enableGetData"));
         m_protocol.setGetDataDelay(str.readInt("getDataDelay", 100));
         m_protocol.setGetDataMask(str.readInt("getDataMask", 0xF));
         retrieveValues();
     }
 }
