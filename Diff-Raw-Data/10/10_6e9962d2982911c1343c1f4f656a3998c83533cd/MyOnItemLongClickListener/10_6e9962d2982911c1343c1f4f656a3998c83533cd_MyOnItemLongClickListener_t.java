 package de.janssen.android.gsoplan;
 
 import de.janssen.android.gsoplan.runnables.Toaster;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Toast;
 
 public class MyOnItemLongClickListener implements OnItemLongClickListener
 {
 
     private MyContext ctxt;
     public MyOnItemLongClickListener(MyContext ctxt)
     {
 	this.ctxt=ctxt;
     }
     
     
     @Override
     public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
     {
	//TODO: ist noch nicht fertig!
	/*
 	LinearLayout row = (LinearLayout) arg1;
 	TextView tv = (TextView) row.getChildAt(1);
 	String myText = (String) tv.getText();
 	String newText = Tools.decodeContraction(ctxt,myText);
 	ctxt.handler.post(new Toaster(ctxt,newText,Toast.LENGTH_LONG));
	*/
 	return false;
     }
 
 }
