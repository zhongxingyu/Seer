 package net.hermeto.android.main;
 
 import net.hermeto.android.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 public class Main extends Activity {
 
 	ConnectionController mConnectionController;
 	Toast ConnToast;
 
 	private ImageButton btnUp;
 	private ImageButton btnLeft;
 	private ImageButton btnDown;
 	private ImageButton btnRight;
 	private ImageButton btnConnect;
 	private ImageButton btnCenter;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 
 		// Setup Controller
 		mConnectionController = new ConnectionController(Main.this);
 
 		Window janela = getWindow();
 		janela.requestFeature(Window.FEATURE_LEFT_ICON);
 		setContentView(R.layout.main);
 		janela.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
 				R.drawable.icon);
 
 		bindViews();
 		bindEvents();
 
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 	
 	@Override
 	public void onStop() {
 		super.onStop();
 		mConnectionController.disconnect();
 	}
 	
 	@Override
 	public void onRestart() {
 		super.onRestart();
 	}
 
 	/********
 	 * Binders
 	 **********/
 	public void bindViews() {
 
 		// Controls
 		btnUp = (ImageButton) findViewById(R.id.b_up);
 		btnLeft = (ImageButton) findViewById(R.id.b_left);
 		btnDown = (ImageButton) findViewById(R.id.b_down);
 		btnRight = (ImageButton) findViewById(R.id.b_right);
 		btnCenter = (ImageButton) findViewById(R.id.b_center);
 
 		// Connection
 		btnConnect = (ImageButton) findViewById(R.id.b_connect);
 	}
 
 	public void bindEvents() {
 
 		btnUp.setOnClickListener(mBtnUpClick);
 		btnLeft.setOnClickListener(mBtnLeftClick);
 		btnDown.setOnClickListener(mBtnDownClick);
 		btnRight.setOnClickListener(mBtnRightClick);
 		btnCenter.setOnClickListener(mBtnCenterClick);
 		btnConnect.setOnClickListener(mBtnConnectClick);
 
 	}
 
 	/***********
 	 * OnClickListeners
 	 *********/
 	OnClickListener mBtnUpClick = new OnClickListener() {
 
 		public void onClick(View v) {
 
 			mConnectionController.upClick();
 
 		}
 	};
 
 	OnClickListener mBtnLeftClick = new OnClickListener() {
 
 		public void onClick(View v) {
 
 			mConnectionController.leftClick();
 
 		}
 	};
 
 	OnClickListener mBtnDownClick = new OnClickListener() {
 
 		public void onClick(View v) {
 
 			mConnectionController.downClick();
 
 		}
 	};
 
 	OnClickListener mBtnRightClick = new OnClickListener() {
 
 		public void onClick(View v) {
 
 			mConnectionController.rightClick();
 
 		}
 	};
 
 	OnClickListener mBtnCenterClick = new OnClickListener() {
 
 		public void onClick(View v) {
 			mConnectionController.buttonClick();
 		}
 	};
 
 	OnClickListener mBtnConnectClick = new OnClickListener() {
 
 		public void onClick(View v) {
 
 			AlertDialog.Builder dialogo = new AlertDialog.Builder(Main.this);
 			dialogo.setTitle(R.string.d_title);
 			dialogo.setIcon(R.drawable.net_fail);
 			dialogo.setMessage(R.string.d_text);
 
 			final EditText input = new EditText(Main.this);
 			input.setSingleLine();
 
 			dialogo.setView(input);
 
 			dialogo.setPositiveButton(R.string.d_ok,
 					new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 
 					String nickname = input.getText().toString();
					Log.e("XMPP", "Button pressed - "+nickname);
 					mConnectionController.connect(nickname);
 
 				}
 			});
 
 			dialogo.setNegativeButton(R.string.d_cancel,
 					new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 
 				}
 			});
 
 			dialogo.show();
 
 		}
 	};
 }
