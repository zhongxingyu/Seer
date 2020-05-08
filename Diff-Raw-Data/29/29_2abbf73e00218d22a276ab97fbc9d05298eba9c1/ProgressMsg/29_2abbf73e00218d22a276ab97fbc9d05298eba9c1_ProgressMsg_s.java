 package com.chess.genesis;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 
 class ProgressMsg extends ProgressDialog implements Runnable
 {
	public ProgressMsg(Context context)
 	{
 		super(context);
 	}
 
 	public void onBackPressed()
 	{
 	}
 
 	public void remove()
 	{
 		(new Thread(this)).start();
 	}
 
	public void setText(String msg)
 	{
 		setMessage(msg);
 		if (!isShowing())
 			show();
 	}
 
 	public void run()
 	{
 	try {
 		Thread.sleep(256);
 		dismiss();
 	} catch (InterruptedException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 }
