 package com.diandianwan.app.learning.async;
 
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class TimerTaskTest extends Activity{
     /**执行Timer进度**/
     public final static int LOAD_PROGRESS =0;
 
     /**关闭TImer进度**/
     public final static int CLOSE_PROGRESS =1;
 
     /**开始TIMERTASK按钮**/
     Button mButton0 = null;
 
     /**关闭TIMERTASK按钮**/
     Button mButton1 =null;
 
     /**显示内容**/
     TextView mTextView = null;
 
     Context mContext = null;
 
     /**timer对象**/
     Timer mTimer = null;
 
     /**TimerTask对象**/
     TimerTask mTimerTask = null;
 
     /**记录TimerID**/
     int mTimerID =0;
 
     Handler handler = new Handler(){
         public void handleMessage(Message msg){
             switch(msg.what){
                 case LOAD_PROGRESS:
                     mTextView.setText("当前得TimerID为："+msg.arg1);
                     break;
                 case CLOSE_PROGRESS:
                     mTextView.setText("当前Timer已经关闭请重新启动");
                     break;
             }
             super.handleMessage(msg);
         }
     };
     protected void onCreate(Bundle s){
         setContentView(R.layout.timer);
         mContext  = this;
         mButton0 = (Button) this.findViewById(R.id.button1);
         mButton1 = (Button) this.findViewById(R.id.button2);
         mTextView = (TextView) this.findViewById(R.id.textView1);
         mTextView.setText("点击按钮开始更新时间");
         mButton0.setOnClickListener(new OnClickListener(){
             public void onClick(View v){
                 StartTimer();
             }
         });
         mButton1.setOnClickListener(new OnClickListener(){
             public void onClick(View v){
                 CloseTimer();
             }
         });
         super.onCreate(s);
     }
     public void StartTimer(){
         if(mTimer == null){
             mTimerTask = new TimerTask(){
 
                 @Override
                 public void run() {
                     mTimerID ++;
                     Message msg = new Message();
                     msg.what = LOAD_PROGRESS;
                     msg.arg1 =(int) (mTimerID);
                     handler.sendMessage(msg);
 
                 }
 
             };
             mTimer = new Timer();
             //第一个参数为执行的mTimerTask
             //第二个参数为延迟得事件，这里写1000得意思是 mTimerTask将延迟1秒执行
             //第三个参数为多久执行一次，这里写1000 表示没1秒执行一次mTimerTask的Run方法
             mTimer.schedule(mTimerTask, 1000,1000);
         }
     }
     public void CloseTimer(){
         if(mTimer !=null){
             mTimer.cancel();
             mTimer = null;
         }
         if(mTimerTask!= null){
             mTimerTask = null;
         }
         mTimerID =0;
         handler.sendEmptyMessage(CLOSE_PROGRESS);
     }
 }
