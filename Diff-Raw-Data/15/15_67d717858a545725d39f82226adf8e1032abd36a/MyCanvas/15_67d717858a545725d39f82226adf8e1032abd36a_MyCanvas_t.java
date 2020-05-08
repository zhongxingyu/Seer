 package ntu.csie.wcm;
 
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.AvoidXfermode.Mode;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffColorFilter;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MyCanvas extends Activity{
 
 	private MyCanvas mSelf;
 	private MySurfaceView mView;
 	private MySocket mMySocket;
 	
 	boolean mIsServer;
 	private PorterDuffColorFilter mColorFilter;
 
 	private ImageButton CcBtn;
 	private ImageButton eraserBtn;
 	private ImageButton undoBtn;
 	private ImageButton redoBtn;
 	private ImageButton clearBtn;
 	
 	
 	private boolean iCanUndo;
 	private boolean iCanRedo;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.canvaslayout);
 		
 		mSelf = this;
 		mView = (MySurfaceView)findViewById(R.id.mySurfaceView1);
 		
 		mMySocket  =new MySocket(mView , 5050, (WifiManager) getSystemService(WIFI_SERVICE));
         
         mView.setSocket(mMySocket);
         
         Bundle bundle = this.getIntent().getExtras(); 
   	    
         // color filter
         mColorFilter = new PorterDuffColorFilter(Color.argb(180, 200, 200, 200), PorterDuff.Mode.SRC_ATOP);
         
         mIsServer = bundle.getBoolean("isServer");
         if(mIsServer)
         {
         	mMySocket.server();
         	checkIP();
         }
         else
         {
         	String remoteIP = bundle.getString("IP");
         	mMySocket.client(remoteIP, 5050);
         }
         
         
 		
 		//change color button
 		CcBtn = (ImageButton) findViewById(R.id.ChangeColorBt);
 		
 		CcBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on click
 				useColorPicker();
 							
 			}
 		});
 		CcBtn.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if(event.getAction() == MotionEvent.ACTION_DOWN){
 					CcBtn.setImageResource(R.drawable.bt_palette_down_128);
 				}else if(event.getAction() == MotionEvent.ACTION_UP){
 					CcBtn.setImageResource(R.drawable.bt_palette_128);
 				}
 				return false;
 			}
 		});
 		
 
 		
 		// eraser button
 		eraserBtn = (ImageButton) findViewById(R.id.EraserBt);
 		eraserBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on click
 				mView.getPaint().setColor(Color.WHITE);
 				
 				mMySocket.send(new Commands.ChangeColorCmd(mView.getPaint().getColor()));
 			}
 		});
 		
 		// tantofish: this will let the button change color when clicked
 		eraserBtn.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if(event.getAction() == MotionEvent.ACTION_DOWN){
 					eraserBtn.setImageResource(R.drawable.bt_eraser_down_128);
 				}else if(event.getAction() == MotionEvent.ACTION_UP){
 					eraserBtn.setImageResource(R.drawable.bt_eraser_128);
 				}
 				return false;
 			}
 		});
 		
 		
 		undoBtn = (ImageButton) findViewById(R.id.undoBt);
 		undoBtn.setColorFilter(mColorFilter);
 		undoBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				
 				// Perform action on click
 				mView.undo();
 	            // send command to remote
 				mMySocket.send(new Commands.UndoRedoCmd(true));
 				// tantofish: chenge undo redo arrow color (gray->color or color->gray)
 				checkUnReDoValid();
 			}
 		});
 		
 		// tantofish: this will let the button change color when clicked
 		undoBtn.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if(!iCanUndo)	return false;
 				if(event.getAction() == MotionEvent.ACTION_DOWN){
 					undoBtn.setImageResource(R.drawable.bt_undo_down_128);
 				}else if(event.getAction() == MotionEvent.ACTION_UP){
 					undoBtn.setImageResource(R.drawable.bt_undo_128);
 				}
 				return false;
 			}
 		});
 		
 		redoBtn = (ImageButton) findViewById(R.id.redoBt);
 		redoBtn.setColorFilter(mColorFilter);
 		redoBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				
 				// Perform action on click
 				mView.redo();
 				// Send command to remote
 				mMySocket.send(new Commands.UndoRedoCmd(false));
 				// tantofish: chenge undo redo arrow color (gray->color or color->gray)
 				checkUnReDoValid();
 			}
 		});
 		
 		// tantofish: this will let the button change color when clicked
 		redoBtn.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if(!iCanRedo)	return false;
 				if(event.getAction() == MotionEvent.ACTION_DOWN){
 					redoBtn.setImageResource(R.drawable.bt_redo_down_128);
 				}else if(event.getAction() == MotionEvent.ACTION_UP){
 					redoBtn.setImageResource(R.drawable.bt_redo_128);
 				}
 				return false;
 			}
 		});
 		
 		clearBtn = (ImageButton) findViewById(R.id.clearBt);
 		clearBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on click
 				mView.clearCanvas();
 				
 				
 			}
 		});
 		clearBtn.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if(event.getAction() == MotionEvent.ACTION_DOWN){
 					clearBtn.setImageResource(R.drawable.bt_clear_down_128);
 				}else if(event.getAction() == MotionEvent.ACTION_UP){
 					clearBtn.setImageResource(R.drawable.bt_clear_128);
 				}
 				return false;
 			}
 		});
 		
 		
 	}
 
 	
 	public MySocket getSocket()
 	{
 		return mMySocket;
 	}
 	
 	private void useColorPicker()
 	{
 		new ColorPickDialog(this , mView.getPaint() , mView.getPaint().getColor()).show();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Ѽ1:sid, Ѽ2:itemId, Ѽ3:item, Ѽ4:itemW
 		menu.add(0, 0, 0, "Next Page");
 		menu.add(0, 1, 1, "Frame Select");
 		menu.add(0, 2, 2, "Load Image");
 		menu.add(0, 3, 3, "Check IP");
 		menu.add(0, 4, 4, "Disconnect");
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// ̾itemIdӧP_ϥΪI@item
 		switch (item.getItemId()) {
 		case 0:
 			Toast.makeText(MyCanvas.this, "Iu...", Toast.LENGTH_SHORT).show();
 			break;
 		case 1:
 			Toast.makeText(MyCanvas.this, "Iu...", Toast.LENGTH_SHORT).show();
 			break;
 		case 2:
 			Intent intent = new Intent();
 			intent.setClass(MyCanvas.this, ImgLoaderActivity.class);
 			Bundle bundle = new Bundle();
 			intent.putExtras(bundle);
 			startActivityForResult(intent, 1);
 			break;
 		case 3:
 			checkIP();
 			break;
 		case 4:
 			mMySocket.disconnect();
 			this.finish();
 			break;
 		default:
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data){
 		Log.d("Debug","onActivityResult: q ImageLoader ^ӤF");
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		if (requestCode == 1 && resultCode == RESULT_OK){
 			Bundle b = data.getExtras();
 			String path = b.getString("imgPath");
 			
 			Log.d("Debug","Decode Returned Bitmap");
 
 			Bitmap img = BitmapFactory.decodeFile(path);
 			
 			
 			
 			float marginX = 0.9f;
 			float marginY = 0.8f;
 			
 			int width  = img.getWidth();
 			int height = img.getHeight();
 	        int bm_w   = mView.getWidth()  ;
 	        int bm_h   = mView.getHeight() ;
 	        
 	        float scaleX = (float) bm_w * marginX / width;
 	        float scaleY = (float) bm_h * marginY / height;
 	        
 	        float scale = java.lang.Math.min(scaleX, scaleY);
 	        Matrix matrix = new Matrix();
 	        matrix.postScale(scale, scale);
 	        Bitmap scaledImg = Bitmap.createBitmap(img, 0, 0, width, height, matrix, true);
 	        img.recycle();
 	        
 			
 			
 			
 			mView.drawImgOntoCanvas(scaledImg);
 
 		  
 
 			// Chengyan: transfer bitmap to byte stream then send 
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			scaledImg.compress(Bitmap.CompressFormat.PNG, 100, out);
 			mView.getSocket().send(
 				new Commands.SendBitmapCommit(out.toByteArray()));
 		   
 			scaledImg.recycle();
 			
 	
 		}
 	}
 	
 	public void checkIP(){
 
 		// tantofish : I use a layout "dialog.xml" because I want to set the text size,
 		//             and I have no idea how to achieve this without using an extra xml.
 		LayoutInflater inflater = LayoutInflater.from(MyCanvas.this);  
         final View textEntryView = inflater.inflate(R.layout.dialog, null);  
         final TextView ipTextView=(TextView)textEntryView.findViewById(R.id.ipTextView);
         String ip = mMySocket.getIP();
         ipTextView.setText(ip.subSequence(1, ip.length()));
         final ProgressDialog.Builder dialog = new ProgressDialog.Builder(MyCanvas.this); 
         dialog.setCancelable(false);  
         //dialog.setTitle("IP address");  
         dialog.setView(textEntryView);
         dialog.setNegativeButton("OK",  
                 new DialogInterface.OnClickListener() {  
                     public void onClick(DialogInterface dialog, int whichButton) {
                     }
                 });  
         dialog.show();
 	}
 
 	// tantofish : 
 	public void checkUnReDoValid(){
 
 		iCanRedo = mView.IcanRedo();
 		iCanUndo = mView.IcanUndo();
 
 		if(iCanRedo)
 			redoBtn.clearColorFilter();	// set button to color type 
 		else
 			redoBtn.setColorFilter(mColorFilter); // set button to gray type
 		
 		if(iCanUndo)
 			undoBtn.clearColorFilter();
 		else
 			undoBtn.setColorFilter(mColorFilter);
 		  
 	}
	// carefully use this
	public void enableUndoDisableRedo(){
		iCanUndo = true;
		undoBtn.clearColorFilter();
		iCanRedo = false;
		redoBtn.setColorFilter(mColorFilter);
 	}
     
     
 
 }
