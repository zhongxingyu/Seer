 package com.example.modernhome;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.Window;
 import android.view.WindowManager;
 import android.content.pm.ActivityInfo;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.view.View;
 import android.os.CountDownTimer;
 
 public class MainActivity extends Activity {
 
 	private Controller _controller;
     private TextView anweisungText;
     public TextView okText;
 
     public TextView countdownLabel;
     public ImageButton microBtn;
     public Button cancelBtn;
     public Button bestaetigenBtn;
     public TextView executeText;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
         // Set window fullscreen and remove title bar, and force landscape orientation
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         setContentView(R.layout.activity_main);
 
         anweisungText = (TextView)findViewById(R.id.textView);
         okText = (TextView)findViewById(R.id.textView2);
         executeText = (TextView)findViewById(R.id.textView3);
 
         cancelBtn = (Button) findViewById(R.id.button);
         bestaetigenBtn = (Button) findViewById(R.id.button2);
         countdownLabel = (TextView) findViewById(R.id.textView4);
 		_controller = new Controller(this);
 
         addListenerOnOKButton();
         addListenerOnbestaetigenButton();
         addListenerOncancelButton();
 
 	}
 
     public void addListenerOnOKButton() {
 
         microBtn = (ImageButton) findViewById(R.id.imageButton);
 
         microBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 buzzWordRecognized();
             }
         });
     }
 
     public void addListenerOnbestaetigenButton() {
 
 
 
         bestaetigenBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 sendCommand();
             }
         });
     }
 
     public void addListenerOncancelButton() {
 
 
 
         cancelBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 cancelCommand();
             }
         });
     }
 	@Override
 	protected void onPause() {
 		if (_controller != null)
 			_controller.onPause();
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		if (_controller != null)
 			_controller.onResume();
 		super.onResume();
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) 
 	{
 		if(resultCode == 1234)
 		{
 			_controller.readyForListning = true;
 			_controller.notify();
 		}
 	}
 
 	public void buzzWordRecognized()
 	{
         if(_controller._buzzWordRecognized==false)
         {
 
 
         anweisungText.setVisibility(View.INVISIBLE);
         okText.setVisibility(View.VISIBLE);
         _controller._buzzWordRecognized = true;
         //_audioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP, 100);
         _controller._soundPool.play(_controller._sound, 1, 1, 1, 0, 1);
         _controller._sound = _controller._soundPool.load(_controller._mainView, R.raw.ding, 1);
         }
 
 	}
 
     public void commandRecognized()
     {
 
         okText.setVisibility(View.INVISIBLE);
         executeText.setVisibility(View.VISIBLE);
         _controller._soundPool.play(_controller._sound, 1, 1, 1, 0, 1);
         _controller._sound = _controller._soundPool.load(_controller._mainView, R.raw.ding, 1);
 
 
         cancelBtn.setVisibility(View.VISIBLE);
         bestaetigenBtn.setVisibility(View.VISIBLE);
         countdownLabel.setVisibility(View.VISIBLE);
 
         // counter bis neuer command verstanden werden kann
 
 
         new CountDownTimer(5000, 1000) {
 
             public void onTick(long millisUntilFinished) {
                 final int j = (int) millisUntilFinished;
                 runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
 
                         countdownLabel.setText(String.valueOf(Math.round(j/1000d)-1));
                     }
                 });
             }
 
             public void onFinish() {
                 sendCommand();
             }
         }.start();
     }
 
     public void sendCommand()
     {
 
         cancelBtn.setVisibility(View.INVISIBLE);
         bestaetigenBtn.setVisibility(View.INVISIBLE);
         countdownLabel.setVisibility(View.INVISIBLE);
 
         anweisungText.setVisibility(View.VISIBLE);
         okText.setVisibility(View.INVISIBLE);
         executeText.setVisibility(View.INVISIBLE);
         _controller._buzzWordRecognized=false;
 
         _controller.sendHttpRequest();
 
     }
 
 
     public void cancelCommand()
     {
 
         cancelBtn.setVisibility(View.INVISIBLE);
         bestaetigenBtn.setVisibility(View.INVISIBLE);
         countdownLabel.setVisibility(View.INVISIBLE);
 
         anweisungText.setVisibility(View.VISIBLE);
         okText.setVisibility(View.INVISIBLE);
         executeText.setVisibility(View.INVISIBLE);
         _controller._buzzWordRecognized=false;
 
     }
 
 
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
