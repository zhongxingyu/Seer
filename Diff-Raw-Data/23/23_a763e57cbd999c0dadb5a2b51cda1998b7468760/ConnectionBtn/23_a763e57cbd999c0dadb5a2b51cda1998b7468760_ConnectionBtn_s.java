 package com.tassadar.lorrismobile.connections;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.PopupWindow;
 import android.widget.PopupWindow.OnDismissListener;
 import android.widget.TextView;
 
 import com.tassadar.lorrismobile.R;
 import com.tassadar.lorrismobile.WorkspaceActivity;
 
 public class ConnectionBtn implements ConnectionInterface, OnClickListener {
     public ConnectionBtn(ImageButton btn) {
         m_btn = btn;
         m_btn.setOnClickListener(this);
     }
 
     public void setIconByState(int state) {
         int icon;
         switch(state) {
             case Connection.ST_CONNECTED:
                 icon = R.drawable.conn_green;
                 break;
             case Connection.ST_CONNECTING:
                 icon = R.drawable.conn_orange;
                 break;
             case Connection.ST_DISCONNECTED:
             default:
                 icon = R.drawable.conn_red;
                 break;
         }
         m_btn.setImageResource(icon);
     }
 
     public void setConnection(Connection c) {
         if(m_conn != null) {
             m_conn.removeInterface(this);
         }
 
         m_conn = c;
 
         if(m_conn != null) {
             m_conn.addInterface(this);
             setIconByState(m_conn.getState());
         } else {
             stateChanged(Connection.ST_DISCONNECTED);
         }
     }
 
     @Override
     public void stateChanged(int state) {
         setIconByState(state);
         setPopupTextByState(state);
     }
 
     @Override
     public void onClick(View v) {
         switch(v.getId()) {
             case R.id.conn_btn:
                 on_connBtn_clicked(v);
                 break;
             case R.id.set_conn_btn:
             {
                 Activity act = (Activity)m_btn.getContext();
                 Intent i = new Intent(act, ConnectionsActivity.class);
                 act.startActivityForResult(i, WorkspaceActivity.REQ_SET_CONN);
                 closePopup();
                 break;
             }
             case R.id.connect_btn:
                 if(m_conn == null)
                     break;
 
                 if(m_conn.isOpen())
                     m_conn.close();
                 else
                     m_conn.open();
                 break;
         }
     }
 
     private void on_connBtn_clicked(View v) {
         v.setSelected(true);
 
         LayoutInflater inflater = (LayoutInflater)m_btn.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View layout = inflater.inflate(R.layout.conn_popup, null);
 
         Resources r = m_btn.getContext().getResources();
         // FIXME: should use real size
         int w = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, r.getDisplayMetrics());
         int h = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 130, r.getDisplayMetrics());
 
         m_connPopup = new PopupWindow(layout, w, h);
         m_connPopup.setOutsideTouchable(true);
 
         Button b = (Button)layout.findViewById(R.id.connect_btn);
         if(m_conn == null)
             b.setEnabled(false);
         else
             setPopupTextByState(m_conn.getState());
         b.setOnClickListener(this);
 
         b = (Button)layout.findViewById(R.id.set_conn_btn);
         b.setOnClickListener(this);
 
         TextView t = (TextView)layout.findViewById(R.id.conn_name);
         if(m_conn == null)
             t.setVisibility(View.GONE);
         else
             t.setText(m_conn.getName());
 
         // This forces it to accept outside touch events. Probably hack.
        m_connPopup.setBackgroundDrawable(new BitmapDrawable());
         m_connPopup.setTouchInterceptor(new OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                     closePopup();
                     return true;
                 }
                 return false;
             }
         });
         m_connPopup.setOnDismissListener(new OnDismissListener() {
             @Override
             public void onDismiss() {
                 m_btn.setSelected(false);
             }
         });
         m_connPopup.showAsDropDown(v);
     }
 
     public void setVisible(boolean visible) {
         m_btn.setVisibility(visible ? View.VISIBLE : View.GONE);
         View s = m_btn.getRootView().findViewById(R.id.separator);
         s.setVisibility(visible ? View.VISIBLE : View.GONE);
     }
 
     public void hide() {
         setVisible(false);
     }
 
     public void show() {
         setVisible(true);
     }
 
     public void closePopup() {
         if(m_connPopup != null) {
             m_connPopup.dismiss();
             m_connPopup = null;
         }
     }
 
     private void setPopupTextByState(int state) {
         if(m_connPopup == null)
             return;
 
         Button b = (Button)m_connPopup.getContentView().findViewById(R.id.connect_btn);
         switch(state) {
             case Connection.ST_CONNECTED:
                 b.setText(R.string.disconnect);
                 break;
             case Connection.ST_CONNECTING:
                 b.setText(R.string.connecting);
                 break;
             case Connection.ST_DISCONNECTED:
                 b.setText(R.string.connect);
                 break;
         }
         b.invalidate();
         b.setEnabled(state != Connection.ST_CONNECTING);
     }
 
     @Override
     public void connected(boolean connected) { }
     @Override
     public void disconnecting() { }
     @Override
     public void dataRead(byte[] data) { }
 
     private Connection m_conn;
     private ImageButton m_btn;
     private PopupWindow m_connPopup;
 }
