 package ntu.csie.wcmlab.canvasnetcore;
 
 import java.io.ByteArrayOutputStream;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.util.Collections;
 import java.util.List;
 
 import ntu.csie.wcmlab.canvasnetcore.mycanvas.NoWifyAndThetheringView;
 import ntu.csie.wcmlab.canvasnetcore.utility.NetworkStatusChecker;
 
 import org.apache.http.conn.util.InetAddressUtils;
 
 import wcm.ytwhyc.ratiofixer.RatioActivity;
 import wcm.ytwhyc.ratiofixer.RatioFixer;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffColorFilter;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.text.format.Formatter;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MyCanvas extends RatioActivity {
 	  private static final int SELECT_PICTURE = 1;
 	private MyCanvas mSelf;
 	public MySurfaceView mView;
 	private MySocket mMySocket;
 	private MyImgEditView mImageEditingView; // Image Editting vuew
 	private RelativeLayout mRL;
 	boolean mIsServer;
 	private PorterDuffColorFilter mColorFilter;
 
 	private ImageButton CcBtn;
 	private ImageButton eraserBtn;
 	private ImageButton undoBtn;
 	private ImageButton redoBtn;
 	private ImageButton clearBtn;
 	private Button imgEdtOKBtn;
 	private Button imgEdtCancelBtn;
 
 	public ImageView loadedImage;
 	// just for now, remember to correct it to private when release
 
 	private boolean iCanUndo;
 	private boolean iCanRedo;
 
 	public static final int VIEWMODE_CANVAS = 2735;
 	public static final int VIEWMODE_IMAGE_EDITING = 5512;
 
 	private void myFindViewByID() {
 		getMainLayout().setBackgroundColor(Color.WHITE);
 
 		mView = new MySurfaceView(this);
 		mImageEditingView = new MyImgEditView(this);
 		loadedImage = new ImageView(this);
 
 		mRL = new RelativeLayout(this);
 		imgEdtOKBtn = new Button(this);
 		imgEdtOKBtn.setText(R.string.global_ok);
 		imgEdtCancelBtn = new Button(this);
 		imgEdtCancelBtn.setText(R.string.global_cancel);
 
 		CcBtn = new ImageButton(this);
 		initImageButton(CcBtn, R.drawable.bt_palette_128);
 
 		eraserBtn = new ImageButton(this);
 		initImageButton(eraserBtn, R.drawable.bt_eraser_128);
 		undoBtn = new ImageButton(this);
 		initImageButton(undoBtn, R.drawable.bt_undo_128);
 		redoBtn = new ImageButton(this);
 		initImageButton(redoBtn, R.drawable.bt_redo_128);
 		clearBtn = new ImageButton(this);
 		initImageButton(clearBtn, R.drawable.bt_clear_128);
 
 		loadedImage.setAlpha(225);
 	}
 
 	private void initImageButton(ImageButton pIb, int imageResource) {
 		pIb.setScaleType(ScaleType.FIT_XY);
 		pIb.setImageResource(imageResource);
 		pIb.setBackgroundColor(Color.TRANSPARENT);
 	}
 
 	private void setContentView() {
 		getMainLayout().addView(mView,
 				RatioFixer.getLayoutParam(768, 1230, 0, 0));
 		getMainLayout().addView(mImageEditingView,
 				RatioFixer.getLayoutParam(768, 1230, 0, 0));
 		// getMainLayout().addView(mView,RatioFixer.getLayoutParam(153, 130, 0,
 		// 0));
 
 		getMainLayout().addView(CcBtn,
 				RatioFixer.getLayoutParam(153, 130, 0, 0));
 		getMainLayout().addView(eraserBtn,
 				RatioFixer.getLayoutParam(153, 130, 153, 0));
 		getMainLayout().addView(undoBtn,
 				RatioFixer.getLayoutParam(153, 130, 306, 0));
 		getMainLayout().addView(redoBtn,
 				RatioFixer.getLayoutParam(153, 130, 459, 0));
 		getMainLayout().addView(clearBtn,
 				RatioFixer.getLayoutParam(153, 130, 612, 0));
 
 		getMainLayout().addView(imgEdtOKBtn,
 				RatioFixer.getLayoutParam(384, 130,0, 1100));
 		getMainLayout().addView(imgEdtCancelBtn,
 				RatioFixer.getLayoutParam(384, 130, 384, 1100));
 
 		getMainLayout().addView(loadedImage,
 				RatioFixer.getLayoutParam(300, 300, 200, 500));
 	}
 
 	/*
 	 * private void myFindViewByID(){ mView = new MySurfaceView(this);
 	 * mImageEditingView = new MyImgEditView(this); loadedImage = new
 	 * ImageView(this);
 	 * 
 	 * mRL = (RelativeLayout) findViewById(R.id.relativeLayout1); imgEdtOKBtn =
 	 * (Button) findViewById(R.id.bt_imgEdit_OK); imgEdtCancelBtn = (Button)
 	 * findViewById(R.id.bt_imgEdit_Cancel);
 	 * 
 	 * CcBtn = (ImageButton) findViewById(R.id.ChangeColorBt); eraserBtn =
 	 * (ImageButton) findViewById(R.id.EraserBt); undoBtn = (ImageButton)
 	 * findViewById(R.id.undoBt); redoBtn = (ImageButton)
 	 * findViewById(R.id.redoBt); clearBtn = (ImageButton)
 	 * findViewById(R.id.clearBt);
 	 * 
 	 * loadedImage.setAlpha(225); }
 	 */
 
 	private String remoteIP;
 	private ProgressDialog connectingDialog;
 
 	@Override
 	public void onLayoutCreated() {
 		// TODO Auto-generated method stub
 		super.onLayoutCreated();
 		myFindViewByID();
 		setContentView();
 		onCreateProcess();
 
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 	}
 
 	protected void onCreateProcess() {
 		// TODO Auto-generated method stub
 		// setContentView(R.layout.canvaslayout);
 		setRequestedOrientation(1); // lock rotate
 
 		setCanvasViewMode(VIEWMODE_CANVAS);
 
 		mSelf = this;
 
 		mMySocket = new MySocket(mView, 5050,
 				(WifiManager) getSystemService(WIFI_SERVICE));
 
 		mView.setSocket(mMySocket);
 
 		Bundle bundle = this.getIntent().getExtras();
 
 		// tantofish : this is a color filter used for setting the undo/redo
 		// buttons to gray
 		mColorFilter = new PorterDuffColorFilter(
 				Color.argb(180, 200, 200, 200), PorterDuff.Mode.SRC_ATOP);
 
 		mIsServer = bundle.getBoolean("isServer");
 		if (mIsServer) {
 			mMySocket.server();
 
 			if (NetworkStatusChecker.checkIfThethering(this)) {
 				
 				checkIP();
 			} else {
 
 				if (NetworkStatusChecker.checkIfWifyConnected(this)) {
 					checkIP();
 				} else {
 					NoWifyAndThetheringView.show(this);
 				}
 				
 			}
 
 		} else {
 
 			// ChengYan: show progressDialog when client connecting to host
 			connectingDialog = ProgressDialog.show(MyCanvas.this, "",
 					"Connecting. Please wait...", true);
 
 			// ChengYan: parse back the six-number to IP
 			remoteIP = bundle.getString("IP");
 			String part1 = remoteIP.substring(0, 3);
 			part1 = Integer.toString(Integer.parseInt(part1));
 			String part2 = remoteIP.substring(3);
 			part2 = Integer.toString(Integer.parseInt(part2));
 			
 			String[] tt = mMySocket.getIP().split("\\.");
 			if (tt[0].equals("0"))
 				tt[0] = "192";
 			if (tt[1].equals("0"))
 				tt[1] = "168";
 	
 			remoteIP = tt[0] + "." + tt[1] + "." + part1 + "." + part2;
 
 			// ChengYan: open a thread for Client connecting to avoid stall.
 			Thread tmp = new Thread() {
 				public void run() {
 
 					if (mMySocket.client(remoteIP, 5050) == -1) {
 						MyCanvas.this.clientTimeOut();
 					} else
 						connectingDialog.dismiss();
 
 				}
 			};
 			tmp.start();
 
 		}
 
 		// change color button
 		CcBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on click
 				mView.drawStateMap.get(mMySocket.idFromIP).stopUseEraser();
 				mMySocket.send(new Commands.UseEraserCmd(false));
 				useColorPicker();
 
 			}
 		});
 		CcBtn.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					CcBtn.setImageResource(R.drawable.bt_palette_down_128);
 				} else if (event.getAction() == MotionEvent.ACTION_UP) {
 					CcBtn.setImageResource(R.drawable.bt_palette_128);
 				}
 				return false;
 			}
 		});
 
 		// eraser button
 		eraserBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on click
 				// mView.getPaint().setAlpha(0);
 				// mView.getPaint().setXfermode(new
 				// PorterDuffXfermode(PorterDuff.Mode.DST_IN));
 				mView.drawStateMap.get(mMySocket.idFromIP).useEraser();
 				// mView.getPaint().setColor(Color.WHITE);
 				mMySocket.send(new Commands.UseEraserCmd(true));
 			}
 		});
 		// tantofish: this will let the button change color when clicked
 		eraserBtn.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					eraserBtn.setImageResource(R.drawable.bt_eraser_down_128);
 				} else if (event.getAction() == MotionEvent.ACTION_UP) {
 					eraserBtn.setImageResource(R.drawable.bt_eraser_128);
 				}
 				return false;
 			}
 		});
 
 		// undo button
 		undoBtn.setColorFilter(mColorFilter);
 		undoBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on click
 				mView.undo();
 				// send command to remote
 				mMySocket.send(new Commands.UndoRedoCmd(true));
 				// tantofish: chenge undo redo arrow color (gray->color or
 				// color->gray)
 				checkUnReDoValid();
 			}
 		});
 		// tantofish: this will let the button change color when clicked
 		undoBtn.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if (!iCanUndo)
 					return false;
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					undoBtn.setImageResource(R.drawable.bt_undo_down_128);
 				} else if (event.getAction() == MotionEvent.ACTION_UP) {
 					undoBtn.setImageResource(R.drawable.bt_undo_128);
 				}
 				return false;
 			}
 		});
 
 		// redo button
 		redoBtn.setColorFilter(mColorFilter);
 
 		redoBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 
 				// Perform action on click
 				mView.redo();
 				// Send command to remote
 				mMySocket.send(new Commands.UndoRedoCmd(false));
 				// tantofish: chenge undo redo arrow color (gray->color or
 				// color->gray)
 				checkUnReDoValid();
 			}
 		});
 
 		// tantofish: this will let the button change color when clicked
 		redoBtn.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if (!iCanRedo)
 					return false;
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					redoBtn.setImageResource(R.drawable.bt_redo_down_128);
 				} else if (event.getAction() == MotionEvent.ACTION_UP) {
 					redoBtn.setImageResource(R.drawable.bt_redo_128);
 				}
 				return false;
 			}
 		});
 
 		// clear button
 		clearBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on click
 				mView.clearCanvas();
 
 			}
 		});
 		clearBtn.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					clearBtn.setImageResource(R.drawable.bt_clear_down_128);
 				} else if (event.getAction() == MotionEvent.ACTION_UP) {
 					clearBtn.setImageResource(R.drawable.bt_clear_128);
 				}
 				return false;
 			}
 		});
 
 		// cancel button should appear when doing image editing
 		imgEdtCancelBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				mImageEditingView.cancel();
 			}
 		});
 		// cancel button should appear when doing image editing
 		imgEdtOKBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Bitmap bm = mImageEditingView.ok(mView.getBitmap());
 				mView.setBitmap(bm);
 				mView.pushBuffer(bm);
 
 				/*
 				 * tantofish: compress takes a lot of time use a thread to
 				 * "start" this action as background execution.
 				 */
 				new Thread() {
 					public void run() {
 						Bitmap bm = mImageEditingView.ok(mView.getBitmap());
 
 						Bitmap bg = Bitmap.createBitmap(bm.getWidth(),
 								bm.getHeight(), Bitmap.Config.ARGB_8888);
 						Canvas tmpCan = new Canvas(bg);
 
 						// tmpCan.drawColor(Color.TRANSPARENT);
 
 						// Resources res = getResources();
 
 						Rect rect = new Rect();
 						rect.left = 0;
 						rect.right = mView.getWidth();
 						rect.top = 0;
 						rect.bottom = mView.getHeight();
 
 						/*
 						 * /chengyan: temporary comment out Bitmap bgpaper =
 						 * BitmapFactory.decodeResource(res,
 						 * R.drawable.background_paper);
 						 * 
 						 * tmpCan.drawBitmap(bgpaper, null, rect,
 						 * mView.mBitmapPaint);
 						 */
 						tmpCan.drawColor(Color.WHITE);
 
 						tmpCan.drawBitmap(bm, 0, 0, mView.mBitmapPaint);
 
 						// ChengYan: send bitmap to remote
 						ByteArrayOutputStream out = new ByteArrayOutputStream();
 						bg.compress(Bitmap.CompressFormat.JPEG, 100, out);
 						mView.getSocket().send(
 								new Commands.SendBitmapCmd(out.toByteArray()));
 
 						bg.recycle();
 						System.gc();
 
 					}
 				}.start();
 			}
 		});
 	}
 
 	// ChengYan: called when client connect fail
 	private void clientTimeOut() {
 		// Toast.makeText(getApplicationContext(), "Can not connect to : " +
 		// remoteIP, Toast.LENGTH_SHORT).show();
 
 		connectingDialog.dismiss();
 		this.finish();
 
 	}
 
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		super.onBackPressed();
 
 		mView.mBufferDealer.clear();
 
 		mMySocket.disconnect();
 		this.finish();
 
 	}
 
 	// @Override
 	// protected void onPause() {
 	// // TODO Auto-generated method stub
 	// super.onPause();
 	// mView.mBufferDealer.clear();
 	//
 	// mMySocket.disconnect();
 	// this.finish();
 	//
 	// }
 
 
 	public MySocket getSocket() {
 		return mMySocket;
 	}
 
 	private void useColorPicker() {
 		// new ColorPickDialog(this , mView.getPaint() ,
 		// mView.getPaint().getColor()).show();
 		new ColorPickDialog(this, mView.getPaint(), mView.getPaint().getColor())
 				.show();
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Ѽ1:sid, Ѽ2:itemId, Ѽ3:item, Ѽ4:itemW
 		// menu.add(0, 0, 0, "Next Page");
 		// menu.add(0, 1, 1, "Frame Select");
 		menu.add(0, 2, 2, R.string.mycanvas_menu_loadimage);
 		menu.add(0, 3, 3, R.string.mycanvas_menu_checkip);
 		menu.add(0, 4, 4, R.string.mycanvas_menu_save);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// ̾itemIdӧP_ϥΪI@item
 		switch (item.getItemId()) {
 		case 0:
 			Toast.makeText(getApplicationContext(), "Iu...",
 					Toast.LENGTH_SHORT).show();
 			break;
 		case 1:
 			Toast.makeText(getApplicationContext(), "Iu...",
 					Toast.LENGTH_SHORT).show();
 			break;
 		case 2:
 			  Intent intent = new Intent();
               intent.setType("image/*");
               intent.setAction(Intent.ACTION_GET_CONTENT);
               startActivityForResult(Intent.createChooser(intent,
                       "Select Picture"), SELECT_PICTURE);
 			break;
 		case 3:

 				checkIP();
 			break;
 		case 4:
 //			String fileName = mView.mBufferDealer.saveBitmapToMemory(mView
 //					.getBitmap());
 //
 //			mView.errorToast("Save picture to " + fileName + ".jpg");
 
 			 AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 			 dialog.setTitle("Note:");
 			 dialog.setMessage("Comming SoOn!!");
 			 dialog.setIcon(android.R.drawable.ic_dialog_alert);
 			 dialog.setCancelable(false);
 			 dialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {  
 			     public void onClick(DialogInterface dialog, int which) {  
 			     
 			     }  
 			 }); 
 			
 			 dialog.show();
 			
 			break;
 		default:
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/*
 	 * Tantofish: load image activity has bean dead and thus return to there
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d("tantofish", "onActivityResult: q ImageLoader ^ӤF");
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (requestCode == 1 && resultCode == RESULT_OK) {
             if (requestCode == SELECT_PICTURE) {
                 Uri selectedImageUri = data.getData();
                 String selectedImagePath = getPath(selectedImageUri);
                 mImageEditingView.startEditing(selectedImagePath);
             }
 
 		}
 	}
 
     private String getPath(Uri uri) {
         String[] projection = { MediaStore.Images.Media.DATA };
         Cursor cursor = managedQuery(uri, projection, null, null, null);
         int column_index = cursor
                 .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         cursor.moveToFirst();
         return cursor.getString(column_index);
     }
 	
 	public void checkIP(String ip) {
 		LayoutInflater inflater = LayoutInflater.from(MyCanvas.this);
 		final View textEntryView = inflater.inflate(R.layout.dialog, null);
 		final TextView ipTextView = (TextView) textEntryView
 				.findViewById(R.id.ipTextView);
 		ipTextView.setText(ip);
 		final ProgressDialog.Builder dialog = new ProgressDialog.Builder(
 				MyCanvas.this);
 		// dialog.setCancelable(false);
 		dialog.setTitle(R.string.mycanvas_magicnumber_title);
 
 		dialog.setView(textEntryView);
 		dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 			}
 		});
 		dialog.show();
 
 	}
 
 
 	// IP alert dialog
 	public void checkIP() {
 
 		// tantofish : I use a layout "dialog.xml" because I want to set the
 		// text size,
 		// and I have no idea how to achieve this without using an extra xml.
 		LayoutInflater inflater = LayoutInflater.from(MyCanvas.this);
 		final View textEntryView = inflater.inflate(R.layout.dialog, null);
 		final TextView ipTextView = (TextView) textEntryView
 				.findViewById(R.id.ipTextView);
 
 		// ChengYan: cheng ip to Six number
 
 		//String ip = mMySocket.getIP();
 	    String ip = MySocket.getIPAddress(true);
 		
 		
 		String[] tempp = ip.split("\\.");
 
 		tempp[2] = ("00" + tempp[2]);
 		tempp[2] = tempp[2].substring(tempp[2].length() - 3);
 		tempp[3] = "00" + tempp[3];
 		tempp[3] = tempp[3].substring(tempp[3].length() - 3);
 		ip = tempp[2] + tempp[3];
 
 		ipTextView.setText(ip);
 		final ProgressDialog.Builder dialog = new ProgressDialog.Builder(
 				MyCanvas.this);
 		// dialog.setCancelable(false);
 		dialog.setTitle(R.string.mycanvas_magicnumber_title);
 
 		dialog.setView(textEntryView);
 		dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 			}
 		});
 		dialog.show();
 	}
 
 	// tantofish :
 	public void checkUnReDoValid() {
 
 		iCanRedo = mView.IcanRedo();
 		iCanUndo = mView.IcanUndo();
 
 		if (iCanRedo)
 			redoBtn.clearColorFilter(); // set button to color type
 		else
 			redoBtn.setColorFilter(mColorFilter); // set button to gray type
 
 		if (iCanUndo)
 			undoBtn.clearColorFilter();
 		else
 			undoBtn.setColorFilter(mColorFilter);
 
 	}
 
 	// carefully use this
 	public void enableUndoDisableRedo() {
 		iCanUndo = true;
 		undoBtn.clearColorFilter();
 		iCanRedo = false;
 		redoBtn.setColorFilter(mColorFilter);
 	}
 
 	// tantofish: for image view rotate and translate
 	public void transformIV(float angle, RelativeLayout.LayoutParams params,
 			Bitmap img) {
 		loadedImage.setLayoutParams(params);
 		Matrix matrix = new Matrix();
 		matrix.postRotate(angle);
 		Bitmap finalBM = MyImgEditView.createBitmapCarefully(img, matrix);
 		loadedImage.setImageBitmap(finalBM);
 	}
 
 	/*
 	 * tantofish: The main canvas (Surface)view and the image loading(editing)
 	 * view share the same activity->MyCanvas, thus i use this function to
 	 * switch between canvas mode and image editing mode. This function simply
 	 * set the visibility of each unit(eg. button, view...)
 	 */
 	public void setCanvasViewMode(int mode) {
 		switch (mode) {
 		case VIEWMODE_IMAGE_EDITING:
 			eraserBtn.setVisibility(ImageButton.INVISIBLE);
 			CcBtn.setVisibility(ImageButton.INVISIBLE);
 			undoBtn.setVisibility(ImageButton.INVISIBLE);
 			redoBtn.setVisibility(ImageButton.INVISIBLE);
 			clearBtn.setVisibility(ImageButton.INVISIBLE);
 			// mView.setVisibility(View.INVISIBLE);
 			mView.setEnabled(false);
 
 			mImageEditingView.setVisibility(View.VISIBLE);
 			imgEdtCancelBtn.setVisibility(Button.VISIBLE);
 
 			imgEdtOKBtn.setVisibility(Button.VISIBLE);
 			imgEdtOKBtn.setVisibility(Button.VISIBLE);
 
 			loadedImage.setVisibility(ImageView.VISIBLE);
 			break;
 		case VIEWMODE_CANVAS:
 			eraserBtn.setVisibility(ImageButton.VISIBLE);
 			CcBtn.setVisibility(ImageButton.VISIBLE);
 			undoBtn.setVisibility(ImageButton.VISIBLE);
 			redoBtn.setVisibility(ImageButton.VISIBLE);
 			clearBtn.setVisibility(ImageButton.VISIBLE);
 			// mView.setVisibility(View.VISIBLE);
 			mView.setEnabled(true);
 
 			mImageEditingView.setVisibility(View.INVISIBLE);
 			imgEdtCancelBtn.setVisibility(Button.INVISIBLE);
 			imgEdtOKBtn.setVisibility(Button.INVISIBLE);
 			loadedImage.setVisibility(ImageView.INVISIBLE);
 			break;
 		}
 	}
 }
