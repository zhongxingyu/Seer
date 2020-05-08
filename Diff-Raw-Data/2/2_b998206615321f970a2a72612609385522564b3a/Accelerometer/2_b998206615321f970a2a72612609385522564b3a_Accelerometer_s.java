 package com.yuniclient;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.yuni.client.R;
 
 public class Accelerometer extends Activity
 {
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);  
         
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
         view = new AccelerometerView(this);
         setContentView(view);
         
         SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
         accelerometerListener = new AccelerometerListener();
         mSensorManager.registerListener(accelerometerListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
         
         lock = ((PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE))
             .newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK), "YuniClient accelerometer lock");
         lock.acquire();
         context = this;
     }
     
     @Override
     protected void onDestroy()
     {
         ((SensorManager) getSystemService(SENSOR_SERVICE)).unregisterListener(accelerometerListener);
         lock.release();
         lock = null;
         accelerometerListener = null;
        if(controlAPI.HasSeparatedSpeed(controlAPI.GetInst().GetAPIType()))
             mMovementFlags = controlAPI.MOVE_NONE;
         byte[] data = controlAPI.GetInst().BuildMovementPacket(mMovementFlags, false, (byte)0);
         
         if(data != null)
             Connection.GetInst().write(data);
         controlAPI.GetInst().SetDefXY(0, 0);
         super.onDestroy();
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
         if(keyCode == KeyEvent.KEYCODE_MENU)
         {
             ShowAPIDialog();
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
     
     private void ShowAPIDialog()
     {
         final CharSequence[] items = {"Keyboard", "YuniRC", "Packets", "Quorra", "Quorra final"};
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Choose control API");
         builder.setSingleChoiceItems(items, controlAPI.GetInst().GetAPIType(), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 controlAPI.GetInst().SetAPIType((byte) item);
                 Toast.makeText(context, items[item] + " has been chosen as control API.", Toast.LENGTH_SHORT).show();
                 dialog.dismiss();
                 
                 if(!controlAPI.IsTargetSpeedDefined((byte) item))
                 {
                     AlertDialog.Builder builder = new AlertDialog.Builder(context);
                     builder.setTitle("Set speed");
                     LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                     View layout = inflater.inflate(R.layout.save_data,
                                                    (ViewGroup) findViewById(R.id.layout_root));
                     ((TextView)layout.findViewById(R.id.data_file_save)).setText("300");
                     builder.setView(layout);
                     builder.setNeutralButton("Set", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface arg0, int arg1) {
                            EditText text = (EditText)alertDialog.findViewById(R.id.data_file_save);
                            int speed = 300;
                            try
                            {
                                speed = Integer.valueOf(text.getText().toString());
                            }
                            catch(NumberFormatException e)
                            {
                                Toast.makeText(context, "Wrong format!", Toast.LENGTH_SHORT).show();
                            }
                            controlAPI.GetInst().SetQuarraSpeed(speed);
                        }
                     });
                     alertDialog = builder.create();
                     alertDialog.show();
                 }
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
     
     private class AccelerometerListener implements SensorEventListener
     {    
         public AccelerometerListener() { }
 
         public void onAccuracyChanged(Sensor arg0, int arg1) {}
 
         public void onSensorChanged(SensorEvent event)
         {
             if (event.sensor.getType() != Sensor.TYPE_ORIENTATION)
                 return;
             
             if(controlAPI.GetInst().GetDefX() == 0 && controlAPI.GetInst().GetDefY() == 0)
             {
                 controlAPI.GetInst().SetDefXY(event.values[1], event.values[2]);
                 return;
             }
             
             byte[] moveFlags = controlAPI.GetInst().XYToMoveFlags(event.values[1], event.values[2]);
 
             if(moveFlags == null || (mMovementFlags == moveFlags[0] && mSpeed == moveFlags[1]))
                 return;
             
             mSpeed = moveFlags[1];
             if(controlAPI.HasSeparatedSpeed(controlAPI.GetInst().GetAPIType()))
             {
                 byte[] speedData = null;
                 if(controlAPI.GetInst().GetAPIType() == controlAPI.API_KEYBOARD)
                     speedData = new byte[1];
                 else
                 {
                     speedData = new byte[2];
                     speedData[1] = (byte)'d';
                 }
                 if(mSpeed == 50)
                     speedData[0] = (byte)'a';
                 else if(mSpeed == 100)
                     speedData[0] = (byte)'b';
                 else 
                     speedData[0] = (byte)'c';
                 Connection.GetInst().write(speedData.clone());
                 if(controlAPI.GetInst().GetAPIType() == controlAPI.API_YUNIRC && mMovementFlags != 0)
                 {
                     speedData = controlAPI.GetInst().BuildMovementPacket(mMovementFlags, false, mSpeed);
                     if(speedData != null)
                         Connection.GetInst().write(speedData.clone());
                 }
             }
             
             mMovementFlags = moveFlags[0];
             if(moveFlags[0] != controlAPI.MOVE_NONE || controlAPI.HasPacketStructure(controlAPI.GetInst().GetAPIType()))
             {
                 byte[] data = controlAPI.GetInst().BuildMovementPacket(mMovementFlags, moveFlags[0] != 0, mSpeed);
                 if(data != null)
                 {
                     Connection.GetInst().write(data.clone());
                     view.drawArrows();
                 }
             }
             else if(moveFlags[0] == controlAPI.MOVE_NONE)
                 view.drawArrows();
         }
     }
     
     public class AccelerometerView extends SurfaceView implements SurfaceHolder.Callback
     {
         private Paint mLine = new Paint(Paint.ANTI_ALIAS_FLAG);
         private Paint mSpeedFrame = new Paint(Paint.ANTI_ALIAS_FLAG);
         private Paint mSpeedPnt = new Paint(Paint.ANTI_ALIAS_FLAG);
         private SurfaceHolder holder = null;
         private Bitmap arrow_up = null;
         private Bitmap arrow_down = null;
         private Bitmap arrow_left = null;
         private Bitmap arrow_right = null;
         
         private float height;
         private float width;
 
         public AccelerometerView(Context context)
         {
             super(context);
             SurfaceHolder holder = getHolder();
             holder.addCallback(this);
             setFocusable(true); // make sure we get key events
             setFocusableInTouchMode(true); // make sure we get touch events
             mLine.setColor(0xFFFF0000);
             mLine.setTextSize(17);
             mLine.setTextAlign(Paint.Align.CENTER);   
             
             mSpeedFrame.setStyle(Style.STROKE);
             mSpeedFrame.setStrokeWidth(2);
             mSpeedFrame.setColor(0xFFFF0000);
             
             mSpeedPnt.setStyle(Style.STROKE);
             mSpeedPnt.setStrokeWidth(35);
             mSpeedPnt.setColor(0xFFFFFF00);
             
             arrow_up = BitmapFactory.decodeResource(getResources(), R.drawable.up);
             arrow_down = BitmapFactory.decodeResource(getResources(), R.drawable.down);
             arrow_left = BitmapFactory.decodeResource(getResources(), R.drawable.left);
             arrow_right = BitmapFactory.decodeResource(getResources(), R.drawable.right);
         }
 
         public void surfaceCreated(SurfaceHolder holder) {
         }         
         
         public void surfaceDestroyed(SurfaceHolder holder) {
         }
 
         public void surfaceChanged(SurfaceHolder holder, int arg1, int width, int height)
         {
             this.width = width/2;
             this.height = height/2;            
             this.holder = holder;
 
             drawArrows();
         }
         
         private void drawBase(Canvas c)
         {
             c.drawColor(Color.BLACK);
             c.drawText("Hold your phone in landscape mode.", width, height-12, mLine);
             c.drawText("Press back to exit.", width, height+12, mLine);
             c.drawText("Speed", width, height+50, mLine);
             
             c.drawLine(width-130, height+25, width+130, height+25, mSpeedFrame);
             c.drawLine(width-130, height+60, width+130, height+60, mSpeedFrame);
             
             c.drawLine(width-130, height+25, width-130, height+60, mSpeedFrame);
             c.drawLine(width+130, height+25, width+130, height+60, mSpeedFrame);
         }
         
         public void drawArrows()
         {
             Canvas c = holder.lockCanvas();
             if(c == null)
                 return;
             
             drawBase(c);
             
             switch(mMovementFlags)
             {
                 case controlAPI.MOVE_NONE:
                     holder.unlockCanvasAndPost(c);
                     return;
                 case controlAPI.MOVE_FORWARD:
                     c.drawBitmap(arrow_up, ((width*2)-arrow_up.getWidth())/2, 0, null);
                     break;
                 case controlAPI.MOVE_BACKWARD:
                     c.drawBitmap(arrow_down, ((width*2)-arrow_down.getWidth())/2, (height*2)-arrow_down.getHeight(), null);
                     break;
                 case controlAPI.MOVE_LEFT:
                     c.drawBitmap(arrow_left, 0, ((height*2)-arrow_left.getHeight())/2, null);
                     break;
                 case controlAPI.MOVE_RIGHT:
                     c.drawBitmap(arrow_right, (width*2)-arrow_right.getWidth(), ((height*2)-arrow_right.getHeight())/2, null);
                     break;
                 default:
                 {
                     if((mMovementFlags & controlAPI.MOVE_FORWARD) != 0)
                         c.drawBitmap(arrow_up, ((width*2)-arrow_up.getWidth())/2, 0, null);
                     else
                         c.drawBitmap(arrow_down, ((width*2)-arrow_down.getWidth())/2, (height*2)-arrow_down.getHeight(), null);
                     
                     if((mMovementFlags & controlAPI.MOVE_LEFT) != 0)
                         c.drawBitmap(arrow_left, 0, ((height*2)-arrow_left.getHeight())/2, null);
                     else
                         c.drawBitmap(arrow_right, (width*2)-arrow_right.getWidth(), ((height*2)-arrow_right.getHeight())/2, null);
                     break;
                 }
             }
             switch(mSpeed)
             {
                 case 0:
                 default:
                     break;
                 case 50:
                     c.drawLine(width-130, height+43, width-34, height+43, mSpeedPnt);
                     break;
                 case 100:
                     c.drawLine(width-130, height+43, width+52, height+43, mSpeedPnt);
                     break;
                 case 127:
                     c.drawLine(width-130, height+43, width+130, height+43, mSpeedPnt);
                     break;
             }
             holder.unlockCanvasAndPost(c);
         }
     }
     
     private AccelerometerView view;
     private AccelerometerListener accelerometerListener;
     private WakeLock lock;
     private Context context;
     private AlertDialog alertDialog;
     
     private byte mMovementFlags;
     private byte mSpeed;
 }
