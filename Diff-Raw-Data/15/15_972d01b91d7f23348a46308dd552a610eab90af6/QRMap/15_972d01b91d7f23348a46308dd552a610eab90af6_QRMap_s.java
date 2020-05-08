 package il.ac.shenkar.qrmap;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.LogInCallback;
 import com.parse.Parse;
 import com.parse.ParseAnalytics;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.SignUpCallback;
 
 public class QRMap extends Activity implements OnTouchListener {
 	// properties
 	private float XposiTion, YposiTion, XinDp, YinDp, XinPixel, YinPixel,
 			deltaX, deltaY, deltaX1, deltaY1;
 	private float radius = 1500;
 	private int position;
 	private Integer index;
 	public static MyView myView;
 	private EditText editText, username, password, email;
 	private ImageButton signupImage;
 	private Button button, delbtn, loginbtn;
 	private Pinpoint pinpoint, newpoint;
 	private RelativeLayout myLayout;
 	private ArrayList<Pinpoint> arrPoints;
 	private List<ParseObject> parseObjects;
 	private boolean exist, changed;
 	private Boolean notfirstTime;
 	private SharedPreferences sharedPref;
 	private java.util.Date date;
 	private final Handler _handler = new Handler();
 	private static int DATA_INTERVAL = 10*1000;
 
 	private IntentIntegrator integrator;
 	private String	result;
 	// private InputMethodManager imm;
 	private ParseObject parseObject;
 	private SingelDB dB;
 	private boolean showFlag = true;
 	private InputMethodManager imm;
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Parse.initialize(this,"QyolXfZSgjWUkNupySyOSIfXsKKCeeFIgtGh2t1y",
 				"bdCoouwCcrTscggcrj3KrcgkYsPGy9wLxib9VXLx");
 		
 		ParseAnalytics.trackAppOpened(getIntent());
 		sharedPref = getSharedPreferences("hasRunBefore", MODE_PRIVATE);
 		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		notfirstTime = sharedPref.getBoolean("notfirstTime", false);
 		date = new Date(sharedPref.getLong("time", 0));	
 		dB = SingelDB.getInstance(this);
 		integrator = new IntentIntegrator(this);		
 		//myView = new MyView(this);	
 		
 		
 		if(Constants.isOnline()){	
 			
 			if (Constants.signInPage) {
 				Constants.signInPage =false;
 				signin();
 				
 			} else {
 				if (Constants.isForceStopped) {
 					login();
 					
 				} else {
 					// application is running
 					if(showFlag)
 					    showMap();
 				//	dB.test();
 					paintOnCanvas();
 				}
 			}
 		}else{
 			//no network
 			Constants.signInPage =false;
 			ParseUser user = new ParseUser();
 			if(showFlag)
 				showMap();
 			
 		}
 	}
 
 	
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		//sensorManager.unregisterListener(listener);
 	}
 
 
 
 	private void signin() {
 		// TODO Auto-generated method stub
 		setContentView(R.layout.signin);
 		// user name field configuration
 		username = (EditText) findViewById(R.id.username);
 		// password field configuration
 		password = (EditText) findViewById(R.id.password);
 		// email field configuration
 		email = (EditText) findViewById(R.id.email);
 		signupImage = (ImageButton) findViewById(R.id.signup);
 		signupImage.setOnClickListener(new OnClickListener() {
 
 			@SuppressLint("NewApi")
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Constants.username = username.getText().toString();
 				if(username.getText().toString().isEmpty()){
 					Toast.makeText(QRMap.this,"username is empty", Toast.LENGTH_SHORT).show();
 					
 				}else{	
 				//System.out.println("user"+username.getText().toString());
 				ParseUser user = new ParseUser();
 				user.setUsername(username.getText().toString());
 			//	System.out.println(username.getText().toString());
 				user.setPassword(password.getText().toString());
 				user.setEmail(email.getText().toString());
 				user.signUpInBackground(new SignUpCallback() {
 
 					@Override
 					public void done(com.parse.ParseException e) {
 						// TODO Auto-generated method stub
 						if (e == null) {
 							// Hooray! Let them use the app now.
 						//	System.out.println("Hooray!");
 							Constants.isForceStopped = false;
 							imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
 							getDataFrame();	
 							finish();
         					 Intent intent = new Intent(QRMap.this, QRMap.class);
        						 startActivity(intent);
 						} else {
 							Toast.makeText(QRMap.this, e.getMessage(), Toast.LENGTH_SHORT).show();
 						}
 					}
 				});
 				}
 			}
 
 		});
 
 	}
 
 	private void login() {
 		// TODO Auto-generated method stub
 		setContentView(R.layout.login);
 		loginbtn = (Button) findViewById(R.id.loginbtn);
 		loginbtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				// user name field configuration
 				username = (EditText) findViewById(R.id.username);
 				// password field configuration
 				password = (EditText) findViewById(R.id.password);
 				
 				Constants.username = username.getText().toString();
 				ParseUser.logInInBackground(username.getText().toString(),
 						password.getText().toString(), new LogInCallback() {
 							@Override
 							public void done(ParseUser user,
 									com.parse.ParseException e) {
 								// TODO Auto-generated method stub
 								if (user != null) {
 								//	System.out.println("hello");
 									//showFlag = false;
 									Constants.isForceStopped = false;
 								//	showMap();
 																		
 								imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
 								getDataFrame();
 								finish();
         						Intent intent = new Intent(QRMap.this, QRMap.class);
        							 startActivity(intent);
 								} else {
 									// Sign up failed. Look at the
 									// ParseException to see what happened.
 									Toast.makeText(QRMap.this, e.getMessage(), Toast.LENGTH_SHORT).show();
 								
 								}
 							}
 						});
 			}
 			
 
 		});
 		signupImage = (ImageButton) findViewById(R.id.signup);
 		signupImage.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				sharedPref.edit().putBoolean("notfirstTime", false).commit();
 				Constants.signInPage =true;
 				finish();
 				Intent intent = new Intent(QRMap.this, QRMap.class);
 					 startActivity(intent);
 
 			}
 
 		});
 
 	}
 	
 	private void showMap() {
 		myView = new MyView(this);	
 		arrPoints = (ArrayList<Pinpoint>) dB.getList();
 		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		params.addRule(RelativeLayout.BELOW, myView.getId());
 		LayoutInflater inflater = LayoutInflater.from(this);
 		myLayout = (RelativeLayout) inflater.inflate(R.layout.activity_main,
 				null);
 		myLayout.addView(myView);
 		setContentView(myLayout);
 		//show map
 
 		// buttons configuration
 		editText = (EditText) findViewById(R.id.editText);
 		button = (Button) findViewById(R.id.btn);
 		delbtn = (Button) findViewById(R.id.delbtn);
 		// Delete button
 		delbtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(Constants.isOnline()){	
 				if(Constants.inOperation){
 				Toast.makeText(getApplicationContext(), "wait i am working",Toast.LENGTH_SHORT).show();
 				myView.inValid();
 					return;
 					}
 				if (delbtn.getText().equals("Delete")) {
 					dB.upDateAndDeleteParse(newpoint,"delete",position,2);
 					Constants.inOperation = true;
 					Constants.inDelOperation = true;
 					myView.inValid();
 				} else {
 					if(!exist)
 					 myView.delLastPinpoint();
 					changed = false;
 					Constants.pointFlag=false;
 					imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
 					myView.inValid();
 				}
 				}else
 					Toast.makeText(QRMap.this, "Missing Network Connection", Toast.LENGTH_SHORT).show();
 				delbtn.setText("Delete");
 				button.setText("save");
 				myView.bringToFront();
 				editText.setText("");
 				editText.setEnabled(true);
 				myView.inValid();
 			}
 
 		});
 		 
 		// button configuration
 		button.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 			if(Constants.inOperation){
 				myView.inValid();	
 				return;
 			}
 				delbtn.setText("Delete");
 				myView.inValid();
 				doneSave();
 				
 			}
 		});
 		editText.setOnEditorActionListener(new OnEditorActionListener() {
 
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				// TODO Auto-generated method stub
 					if(Constants.inOperation){
						myView.inValid();
 						return false;
 					}
 				if (actionId == EditorInfo.IME_ACTION_DONE) {
 					doneSave();
 					myView.inValid();
 					return true;
 				} else {
 					myView.inValid();
 					return false;
 				}
 			}
 		});
 		// On click view text of point/delete exist point
 		myView.setOnClickListener(new MyView.OnClickListener() {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(Constants.inOperation||Constants.inDelOperation){
					myView.inValid();
 					return ;
 				}
 				Constants.inOperation = true;	
 				newpoint = getPinpoint();
 				
 			//	System.out.println(newpoint);
 				if ((newpoint != null) && (changed == false)) {
 					//Toast.makeText(getApplicationContext(), newpoint.getText(),Toast.LENGTH_SHORT).show();
 					System.out.println(button.getText());
 					button.setText("cancel");
 					editText.bringToFront();
 					editText.setText(newpoint.getText());
 					editText.setEnabled(false);
 					button.bringToFront();
 					delbtn.bringToFront();
 					
 				}
 				myView.inValid();
 			}
 
 			
 		});
 		// On long click create new point/edit exist point
 		myView.setOnLongClickListener(new MyView.OnLongClickListener() {
 
 			@Override
 			public boolean onLongClick(View v) {
 				// TODO Auto-generated method stub
 				if(Constants.inOperation||Constants.inDelOperation)
 					return false;
 				
 				
 			//	integrator.initiateScan();
 				if (!editText.isEnabled() || (changed == true))
 					return true;
 				
 				newpoint = getPinpoint();
 			//	System.out.println(newpoint);
 				if(newpoint == null) {
 					exist = false;
 					if(Constants.pointFlag){
 				//	Constants.QRid=1;
 						
 					pinpoint = new Pinpoint(XposiTion, YposiTion, "stamText",
 							myView.maxW, myView.maxH, myView.orientation,
 							Constants.QRid, Constants.parseNumber);
 					myView.addPinpoint(pinpoint);
 					
 					Constants.pointFlag=false;
 					}else{
 						myView.inValid();
 						return true;						
 					}
 				} else {
 					exist = true;
 					delbtn.setText("cancel");
 					editText.setText(newpoint.getText());
 				}
 				
 				editText.bringToFront();
 				button.bringToFront();
 				delbtn.bringToFront();
 				changed = true;
 				myView.inValid();
 				return true;
 				
 			}
 
 		});
 		myView.setOnTouchListener(this);
 		if(!Constants.isOnline())
 		  paintOnCanvas();
 		myView.inValid();
 	}
 	public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.items, menu);
         return super.onCreateOptionsMenu(menu);
     }
  
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
  
         super.onOptionsItemSelected(item);
  
         switch(item.getItemId()){
             case R.id.qrcode:
               		integrator.initiateScan();
             	break;
             case R.id.cancel:
                // Toast.makeText(getBaseContext(), "You selected Cancel button", Toast.LENGTH_SHORT).show();
                 android.os.Process.killProcess(android.os.Process.myPid()) ;
                 break;
             case R.id.like:
                 Toast.makeText(getBaseContext(), "You selected Like button", Toast.LENGTH_SHORT).show();
                 break;    
  
         }
         return true;
  
     }
 
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 	//	stopDataFrame();
 	}
 	// handle result
 		public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 			IntentResult scanResult = IntentIntegrator.parseActivityResult(
 					requestCode, resultCode, intent);
 
 			if (scanResult.getContents() != null) {
 				result = scanResult.getContents();
 				String[] splitResult = result.split("\\."); 
 				//"shenkar_qr_code_01."
 				if(splitResult.length >= 2){
 				if(SingelDB.isInteger(splitResult[1])){
 				
 					if(!Constants.isOnline()){
 						Constants.inOperation = false;
 						//check if exist in local database
 						// Pinpoint p = dB.getPinpointByQRid(object.getInt("QRid"));
 						Constants.QRid = Integer.parseInt(splitResult[1]);
 						 int i = dB.getQRPosition(Constants.QRid);
 						 Toast.makeText(this, "Missing Network Connection", Toast.LENGTH_SHORT).show();
 						 if(i != -1){
 							myView.changeColor(i);
 							myView.inValid();
 						 }
 						 return;					
 						
 					}
 					ParseQuery query = new ParseQuery("PinPointObject");
 					
 					Constants.QRid = Integer.parseInt(splitResult[1]);
 					query.whereEqualTo("QRid", Constants.QRid);
 					query.getFirstInBackground(new GetCallback() {
 					  
 					@Override
 					public void done(ParseObject object, com.parse.ParseException e) {
 						// TODO Auto-generated method stub
 						 if (e == null) {
 						      //  found QRid exist
 							 
 							 if(object.getBoolean("Deleted")){
 									parseObject = object;
 									//Constants.instedOfdeleted = true;
 									Constants.pointFlag =true;
 								 Toast.makeText(QRMap.this, "point your position", Toast.LENGTH_LONG).show();
 							 }else{
 								 Pinpoint p = dB.getPinpointByQRid(object.getInt("QRid"));
 								 int i = dB.getQRPosition(object.getInt("QRid"));
 								 if(i != -1){
 									 myView.changeColor(i);
 									 myView.inValid();
 								 	}
 							 	   }						
 							 	
 						    } else {
 
 						    	Constants.pointFlag =true;
 						    	Toast.makeText(QRMap.this, "point your position", Toast.LENGTH_LONG).show();
 						    }  
 						 
 						 }
 					
 					});					
 					
 				}
 				}else
 					Toast.makeText(this, "you are in the wrong floor!!", Toast.LENGTH_SHORT).show();
 					
 			}
 			
 		}
 
 		
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		
 		sharedPref.edit().putBoolean("notfirstTime", true).commit();
 		
 	}
 
 	// get position of touch
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		// TODO Auto-generated method stub
 
 		XposiTion = event.getX();
 		YposiTion = event.getY();
 		return false;
 	}
 
 	// find flag by it position
 	private Pinpoint getPinpoint() {
 		Iterator<Pinpoint> it = arrPoints.iterator();
 		newpoint = null;
 		position = 0;
 		int i = 0;
 		while (it.hasNext()) {
 
 			pinpoint = it.next();
 
 			// change back to pixels
 			XinPixel = MyView.convertDpToPixel(pinpoint.getxPos(), QRMap.this);
 			YinPixel = MyView.convertDpToPixel(pinpoint.getyPos(), QRMap.this);
 
 			Pinpoint tempPoint = new Pinpoint(XinPixel, YinPixel,
 					pinpoint.getText(), pinpoint.getMaxW(), pinpoint.getMaxH(),
 					pinpoint.getOrientation(), pinpoint.getIdQR(),
 					pinpoint.getParseId());
 			// chack orientation
 			tempPoint = myView.viewTrans(tempPoint);
 			// (x-center_x)^2 + (y - center_y)^2 < radius^2.
 			float f = (XposiTion - tempPoint.getxPos())
 					* (XposiTion - tempPoint.getxPos())
 					+ (YposiTion - tempPoint.getyPos())
 					* (YposiTion - tempPoint.getyPos());
 			// find flag that near to center into specific radius
 			if (f < radius) {
 				if (newpoint != null) {
 					deltaX = (XposiTion - tempPoint.getxPos())
 							* (XposiTion - tempPoint.getxPos());
 					deltaY = (YposiTion - tempPoint.getyPos())
 							* (YposiTion - tempPoint.getyPos());
 					deltaX1 = (newpoint.getxPos() - tempPoint.getxPos())
 							* (newpoint.getxPos() - tempPoint.getxPos());
 					;
 					deltaY1 = (newpoint.getyPos() - tempPoint.getyPos())
 							* (newpoint.getyPos() - tempPoint.getyPos());
 					if ((deltaX1 + deltaY1) < (deltaX + deltaY)) {
 						newpoint = tempPoint;
 						position = i;
 					}
 				} else {
 					newpoint = tempPoint;
 					position = i;
 				}
 			}
 			++i;
 		}
 		Constants.inOperation = false;
 		return newpoint;
 	}
 
 	// method that disappear the keyboard and save message in landscape view
 	private void doneSave() {
 		if (!editText.isEnabled()) {
 			editText.setEnabled(true);
 			button.setText("save");
 			myView.bringToFront();
 			editText.setText("");
 		}
 		String text = editText.getText().toString();
 		if (text != null && text.trim().length() > 0) {
 
 			if (exist) {
 
 				pinpoint = arrPoints.get(position);
 				editText.setText("");
 				//update here
 				dB.upDateAndDeleteParse(pinpoint,text,0,1);
 				
 				
 			} else {
 
 				pinpoint.setText(text);
 				// change pixel to DP
 				XinDp = MyView
 						.convertPixelsToDp(pinpoint.getxPos(), QRMap.this);
 				YinDp = MyView
 						.convertPixelsToDp(pinpoint.getyPos(), QRMap.this);
 				pinpoint.setxPos(XinDp);
 				pinpoint.setyPos(YinDp);
 				pinpoint.setOrientation(0);
 				pinpoint.setIdQR(Constants.QRid);
 				
 
 				// if(!dB.isExist(Constants.QRid))
 				Pinpoint p = new Pinpoint(XinDp, YinDp, pinpoint.getText(),
 						pinpoint.getMaxW(), pinpoint.getMaxH(),
 						pinpoint.getOrientation(), pinpoint.getIdQR());
 						
 				dB.addIfExist(p);		
 				editText.setText("");
 
 			}
 			myView.bringToFront();
 		}
 		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
 		changed = false;
 	}
 	
 
 	private void paintOnCanvas() {
 		Pinpoint pin;
 		Constants.inOperation = true;
 		arrPoints = (ArrayList<Pinpoint>) dB.getList();
 		Iterator<Pinpoint> it = arrPoints.iterator();
 		while (it.hasNext()) {
 			pinpoint = it.next();
 			int orientation = getResources().getConfiguration().orientation;
 
 			// change back to pixels
 			XinPixel = MyView.convertDpToPixel(pinpoint.getxPos(), QRMap.this);
 			YinPixel = MyView.convertDpToPixel(pinpoint.getyPos(), QRMap.this);
 
 			pin = new Pinpoint(XinPixel, YinPixel, pinpoint.getText(),
 					pinpoint.getMaxW(), pinpoint.getMaxH(),
 					pinpoint.getOrientation(), pinpoint.getIdQR(),
 					pinpoint.getParseId());
 
 			myView.addPinpoint(pin);
 			myView.inValid();
 		}
 		Constants.inOperation = false;
 	}
 	
 	public void getParse(){
 		//dB.print();
 		ParseQuery query = new ParseQuery("PinPointObject");
 		//query.whereEqualTo("Deleted", false);
 		query.whereGreaterThan("updatedAt", date);
 		query.findInBackground(new FindCallback() {
 		 
 			@Override
 			public void done(List<ParseObject> objects,
 					com.parse.ParseException e) {
 				// TODO Auto-generated method stub
 				 if (e == null) {
 					 parseObjects=objects;
 					 for(int i=0;i<objects.size();++i){
 						 ParseObject ob = objects.get(i);
 					//	 System.out.println("ob.getBoolean(Deleted):"+ ob.getBoolean("Deleted"));
 						 if(ob.getBoolean("Deleted")==true){
 							///delete ob from DB 
 							 index=ob.getInt("QRid");
 						Integer	index1 = dB.getQRPosition(index);
 						
 						if(index1!=-1){
 							dB.delList(index1);
 							myView.delPinpoint(index1);	
 							myView.inValid();	
 							 }
 						 }else{
 							 //if exist update ob in database
 							 index=ob.getInt("QRid");
 							 index = dB.getQRPosition(index);
 							  if(index!=-1){
 								Pinpoint p = dB.getItemDetails(index);
 								 		p.setText(ob.getString("Text"));	
 								 dB.upDatePinpoint(p);
 								 
 							 }							 
 							 else{
 								 //add new ob to database
 								 
 								 XinDp = ob.getLong("xPos");
 								 YinDp = ob.getLong("yPos");
 								String t =ob.getString("Text");
 								float maxW = ob.getLong("maxW");
 								float maxH = ob.getLong("maxH");		
 								Integer orien =0;
 								Integer idQR = ob.getInt("QRid");	
 								String idParse = ob.getObjectId(); 
 								Pinpoint pin = dB.addListFromParse(XinDp,YinDp,t,maxW, maxH,orien,idQR,idParse);
 								// change back to pixels
 								
 								XinPixel = MyView.convertDpToPixel(XinDp, QRMap.this);
 								YinPixel = MyView.convertDpToPixel(YinDp, QRMap.this);
 								Pinpoint pinP =new Pinpoint(pin);
 								pinP.setxPos(XinPixel);
 								pinP.setyPos(YinPixel);
 								myView.addPinpoint(pinP);
 								myView.inValid();
 							 }							 
 						 	}
 						 if(date != null){
 							if(date.before(ob.getUpdatedAt()))
 								date=ob.getUpdatedAt();
 						 }else
 						  date=ob.getUpdatedAt();
 					  						 
 					 }					
 					 sharedPref.edit().putLong("time", date.getTime()).commit();
 			        } else {
 			           System.out.println(e.getMessage());
 			        }
 			}
 		});
 		
 	}
 
 	private final Runnable getData = new Runnable()
 	{
 	    @Override
 	    public void run()
 	    {
 	        getDataFrame();
 	    }
 	};
 	private void getDataFrame() 
 	{
 	    _handler.postDelayed(getData, DATA_INTERVAL);
 	    getParse();
 	}
 	private void stopDataFrame() 
 	{
 	  _handler.removeCallbacks(getData);
 	}
 	
 		
 
 }
