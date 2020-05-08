 package com.diandianwan.app.learning.async;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.diandianwan.app.R;
 
 /**
  * 
  * 1.Thread与Handler组合，比较常见
 
 * Handler主要是帮助我们来时时更新UI线程
 
 * 这里在后天加载100张图片，然后没加载完成一个用handler 返回给UI线程一张图片并显示
 
 * 最后加载完成返回一个List给UI线程 ，Handler就是一个后台线程与UI线程中间的桥梁
 */
 
 public class ThreadHandler extends Activity {
     /** Called when the activity is first created. */
   
     /**读取进度**/
     public final static int LOAD_PROGRESS =0;
     
     /**标志读取进度结束**/
     public final static int LOAD_COMPLETE = 1;
     /**开始加载100张图片按钮**/
     Button mButton = null;
     
     /**显示内容**/
     TextView mTextView = null;
     
     /**加载图片前的时间**/
     Long mLoadStart = 0L;
     /**加载图片完成的时间**/
     Long mLoadEndt = 0L;
     
     Context mContext = null;
     /**图片列表**/
     private List<Bitmap> list;
     /**图片容器**/
     private ImageView mImageView;
     //接受传过来得消息
     Handler handler = new Handler(){
       public void handleMessage(Message msg){
     		switch(msg.what){
     		case LOAD_PROGRESS:
     			Bitmap bitmap = (Bitmap)msg.obj;
     			mTextView.setText("当前读取到第"+msg.arg1+"张图片");
     			mImageView.setImageBitmap(bitmap);
     			break;
     		case LOAD_COMPLETE:	
     			list = (List<Bitmap>) msg.obj;
     			mTextView.setText("读取结束一共加载"+list.size()+"图片");
     			break;
     		}
     		super.handleMessage(msg);
     	}
     };
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mContext = this;
         setContentView(R.layout.main);
         mButton =(Button) findViewById(R.id.button1);
         mTextView=(TextView) findViewById(R.id.textView1);
         mImageView =(ImageView) this.findViewById(R.id.imageview);
         mTextView.setText("点击按钮加载图片");
         mButton.setOnClickListener(new OnClickListener(){
         	public void onClick(View v){
         		//调用方法
         		LoadImage();
         	}
         });
         	
         
     }
     public void LoadImage(){
     	new Thread(){
     		public void run(){
     			mLoadStart = System.currentTimeMillis();
     			List<Bitmap> list = new ArrayList<Bitmap>();
     			for(int i =0;i<100;i++){
     				Bitmap bitmap=ReadBitmap(mContext,R.drawable.icon);
     				Message msg = new Message();
     				msg.what = LOAD_PROGRESS;
     				msg.arg1 = i+1;
     				list.add(bitmap);
     				msg.obj = bitmap;
     				handler.sendMessage(msg);
     			}
     			mLoadEndt = System.currentTimeMillis();
     			Message msg = new Message();
     			msg.what = LOAD_COMPLETE;
     			msg.obj=list;
     			msg.arg1 = (int) (mLoadEndt - mLoadStart);
     			handler.sendMessage(msg);
     			
     		}
     	}.start();
     }
     public Bitmap ReadBitmap(Context context,int resId){
     	BitmapFactory.Options opt = new BitmapFactory.Options();
     	opt.inPreferredConfig = Bitmap.Config.RGB_565;
     	opt.inPurgeable = true;
     	opt.inInputShareable = true;
     	InputStream is = context.getResources().openRawResource(resId);
     	return BitmapFactory.decodeStream(is, null, opt);
     }
 }
