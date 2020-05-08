 package com.diandianwan.app.learning.async;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;

 
 public class AsyncTaskDemo extends Activity{
   
 	/**开始StartAsync按钮**/
 	Button mButton = null;
 	
 	Context mContext = null;
 	
 	//内容显示出来
 	TextView mTextView = null;
 	
 	//Timer 对象
 	Timer mTimer = null;
 	
 	/** timerTask 对象**/
 	TimerTask mTimerTask = null;
 	
 	/**记录TimerId**/
 	int mTimerId =0;
 	/**图片列表**/
 	private List<Bitmap> list;
 	/**图片容器**/
 	private ImageView mImageView;
 	public void onCreate(Bundle s){
 		super.onCreate(s);
 		setContentView(R.layout.main);
 		mContext = this;
 		mButton =(Button) this.findViewById(R.id.button1);
 		mImageView =(ImageView)this.findViewById(R.id.imageview);
 		mTextView =(TextView) this.findViewById(R.id.textView1);
 		mButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View v){
 				StartAsync();
 			}
 		});
 		
 		
 	}
 	public void StartAsync(){
 		new AsyncTask<Object,Object,Object>(){
             protected void onPreExecute(){
             	//首先执行这个方法，它在UI线程中，可以执行一些异步操作
             	mTextView.setText("开始加载进度");
             	super.onPreExecute();
             }
 			@Override
 			protected Object doInBackground(Object... params) {
 				// TODO Auto-generated method stub
 				//异步后台执行，执行完毕可以返回出去一个结果 Object 对象
 				//得到开始加载得时间
 				list = new ArrayList<Bitmap>();
 				for(int i =0;i<100;i++){
 					Bitmap bitmap =ReadBitmap(mContext,R.drawable.icon);					
 					final ByteArrayOutputStream os = new ByteArrayOutputStream();
 					bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
 					byte[] image = os.toByteArray();
 					Bundle bundle = new Bundle();
 					bundle.putInt("index", i);
 					bundle.putByteArray("image", image);
 					publishProgress(bundle);
 					list.add(bitmap);
 					
 				}
 				
 				return list;
 			}
 			protected void onPostExecute(Object result){
 				//doInBackground 执行之后在这里可以接受到返回结果的对象
 				List<Bitmap> list = (List<Bitmap>) result;
 				mTextView.setText("一共加载了"+list.size()+"张图片");
 			   super.onPostExecute(result);
 			}
 			protected void onProgressUpdate(Object... values){
 				//时时拿到当前的进度更新UI
 				Bundle bundle = (Bundle)values[0];
 				byte[] image = bundle.getByteArray("image");
 				Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
 				int index = bundle.getInt("index");
 				mTextView.setText("当前加载进度"+index);
 				mImageView.setImageBitmap(bitmap);
 				super.onProgressUpdate(values);
 			}
 			
 		}.execute();
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
