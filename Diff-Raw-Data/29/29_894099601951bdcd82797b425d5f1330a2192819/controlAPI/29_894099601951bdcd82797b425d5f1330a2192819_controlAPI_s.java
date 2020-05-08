 package com.yuniclient;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.os.Handler;
 import android.os.Message;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 class controlAPI
 {
     public static final byte API_KEYBOARD = 0;       // just keyboard characters
     public static final byte API_YUNIRC   = 1;       // keyboard with d or u for press and release
     public static final byte API_PACKETS  = 2;       // YuniControl packets1
     
     
     // For API_PACKETS
     public static final byte MOVE_NONE     = 0x00;
     public static final byte MOVE_FORWARD  = 0x01; 
     public static final byte MOVE_BACKWARD = 0x02; 
     public static final byte MOVE_LEFT     = 0x04; 
     public static final byte MOVE_RIGHT    = 0x08; 
 
 
 
     public controlAPI(Handler handler)
     {
         apiType = API_YUNIRC;
         playThread = null;
         mHandler = handler;
     }
     
     public void SetAPIType(byte type) { apiType = type; }
     public byte GetAPIType() { return apiType; }
     public byte[] BuildMovementPacket(byte flags, boolean down, byte speed)
     {
         if(apiType == API_KEYBOARD && !down)
             return null;
         byte[] packet = null;
         
         switch(apiType)
         {
             case API_KEYBOARD:
             case API_YUNIRC:
             {
                 packet = new byte[apiType == API_YUNIRC ? 2 : 1];
                 switch(flags)
                 {
                     default:
                     case MOVE_FORWARD:
                        packet[0] = (byte)'W';
                        break;
                     case MOVE_BACKWARD:
                        packet[0] = (byte)'S';
                        break;
                     case MOVE_LEFT:
                        packet[0] = (byte)'A';
                        break;
                     case MOVE_RIGHT:
                        packet[0] = (byte)'D';
                        break;
                 }
                 if(apiType == API_YUNIRC)
                 {
                     if(down)
                         packet[1] = (byte)'d';
                     else
                         packet[1] = (byte)'u';
                 }
                 break;
             }
             case API_PACKETS:
             {
                 packet = new byte[5];
                 packet[0] = 1;
                 packet[1] = 0x03; // SMSG_SET_MOVEMENT
                 packet[2] = 2;    // lenght
                 packet[3] = speed == 0 ? 127 : speed;  // speed
                 packet[4] = down ? flags : 0;// moveflags, 0 to stop on release
                 break;
             }
         }
         return packet;
     }
     
     public byte[] XYToMoveFlags(float x, float y)
     {
         byte[] flags = {0, 0};
         if(fabs(y - mDefY) >= 20)
         {
             if(mDefY - y < -20)
                 flags[0] |= MOVE_BACKWARD;
             else if(mDefY - y > 20)
                 flags[0] |= MOVE_FORWARD;
             
             //speed
             if(fabs(y - mDefY) < 30)
                 flags[1] = 50;
            if(fabs(y - mDefY) < 35)
                 flags[1] = 100;
            if(fabs(y - mDefY) >= 35)
                 flags[1] = 127;
         }
         if(fabs(x - mDefX) >= 20)
         {
             if(mDefX - x < -20)
                 flags[0] |= MOVE_LEFT;
             else if(mDefX - x > 20)
                 flags[0] |= MOVE_RIGHT;
             if(flags[1] == 0)
             {
                 if(fabs(x - mDefX) < 30)
                     flags[1] = 50;
                if(fabs(x - mDefX) < 35)
                     flags[1] = 100;
                if(fabs(x - mDefX) >= 35)
                     flags[1] = 127;
             }
         }
         return flags;
     }
     
     public void Play(eeprom mem)
     {
         playThread = new PlayThread(mem, YuniClient.eeprom_part);
         playThread.start();
     }
     
     public void received(Packet pkt)
     {
         playThread.PacketReceived(pkt);
     }
     public void StopPlay()
     {
         if(playThread != null)
         {
             playThread.cancel();
             playThread = null;
         }
     }
     
     private class PlayThread extends Thread {
         eeprom EEPROM;
         byte eepromPart;
         boolean stop;
         boolean canContinue;
         byte speed;
         byte moveFlags;
         byte endEvent;
         int endData;
 
         public PlayThread(eeprom mem, byte part) {
             EEPROM = mem;
             eepromPart = part;
             speed = 127;
             canContinue = false;
             stop = false;
             endEvent = 0;
             endData = 0;
         }
 
         public void run() {
             byte[] rec = null;
             boolean down;
             Message msg = null;
             byte[] packet = null;
             int y = EEPROM.getPartRecCount(eepromPart == 1)*5;
             for(int i = 0; i < y && !stop; i +=5)
             {
                 rec = EEPROM.getRec(i);
                 down = rec[1] == (byte)'d';
                 packet = null;
                 // keys
                 switch(rec[0])
                 {
                     case (byte)'W':
                     case (byte)'S':
                     case (byte)'A':
                     case (byte)'D':
                         moveFlags = LetterToFlags(rec[0]);
                         packet = Movement(down);
                         break;
                     case (byte)'a':
                         speed = 50;
                         packet = Movement(down);
                         break;
                     case (byte)'b':
                         speed = 100;
                         packet = Movement(down);
                         break;
                     case (byte)'c':
                         speed = 127;
                         packet = Movement(down);
                         break;
                      
                 }
                 msg = new Message();
                 msg.what = 1;
                 msg.obj = packet;
                 msg.getData().putString("log", "Key " + (char)rec[0] + (char)rec[1] + " action sent\r\n"+
                        "Waiting for event " + rec[2] + " ...");
                 mHandler.sendMessage(msg);
                 
                 // wait events
                 switch(rec[2])
                 {
                     default:
                     case 0:   // EVENT_NONE
                         continue;
                     case 1:   // EVENT_TIME
                         int time = ((rec[3] << 8) | (rec[4] & 0xFF))*10;
                         try {
                             Thread.sleep(time);
                         } catch (InterruptedException e) {
                             // TODO Auto-generated catch block
                             e.printStackTrace();
                         }
                         break;
                     case 2:  // EVENT_SENSOR_LEVEL_HIGHER
                         if(rec[3] == 8)
                         {
                             byte[] emergency = new byte[4];
                             emergency [0] = 1;
                             emergency [1] = Protocol.SMSG_SET_EMERGENCY_INFO;
                             emergency [2] = 1;    // lenght
                             emergency [3] = 1;
                             msg = new Message();
                             msg.what = 1;
                             msg.obj = emergency.clone();
                             msg.getData().putString("log", "Enable emergency");
                             canContinue = false;
                             endEvent = rec[2];
                             endData = rec[3];
                             while(!canContinue)
                             {
                                 try {
                                     Thread.sleep(300);
                                 } catch (InterruptedException e) {
                                     // TODO Auto-generated catch block
                                     e.printStackTrace();
                                 }
                             }
                             emergency [3] = 0;
                             msg = new Message();
                             msg.what = 1;
                             msg.obj = emergency.clone();
                             msg.getData().putString("log", "Disable emergency");
                             endEvent = 0;
                         }
                         break;
                     case 3:  // EVENT_SENSOR_LEVEL_LOWER
                     //TODO NYI
                     break;
                     case 4:  // EVENT_RANGE_HIGHER
                     case 5:  // EVENT_RANGE_LOWER
                         canContinue = false;
                         endEvent = rec[2];
                         endData = (0xFF & rec[4]);
                         byte[] range = new byte[4];
                         range [0] = 1;
                         range [1] = Protocol.SMSG_GET_RANGE_VAL;
                         range [2] = 1;
                         range [3] = rec[3];
                         
                         while(!canContinue)
                         {
                             msg = new Message();
                             msg.what = 1;
                             msg.obj = range.clone();
                             msg.getData().putString("log", "Get range adr " + (0xFF & rec[3]) + ", target " + endData);
                             mHandler.sendMessage(msg);
                             try {
                                     Thread.sleep(300);
                                 } catch (InterruptedException e) {
                                     // TODO Auto-generated catch block
                                     e.printStackTrace();
                                 }
                         } 
                         endEvent = 0;
                         break;
                     case 6:  // EVENT_DISTANCE
                     case 7:  // EVENT_DISTANCE_LEFT
                     case 8:  // EVENT_DISTANCE_RIGHT
                         canContinue = false;
                         endEvent = rec[2];
                         endData = ((rec[3] << 8) | (rec[4] & 0xFF))*5;
                         byte[] encoders = new byte[3];
                         encoders [0] = 1;
                         encoders [1] = Protocol.SMSG_ENCODER_START;
                         encoders [2] = 0;    // lenght
                         msg = new Message();
                         msg.what = 1;
                         msg.obj = encoders.clone();
                         msg.getData().putString("log", "Starting encoders");
                         mHandler.sendMessage(msg);
                         
                         encoders [1] = Protocol.SMSG_ENCODER_GET;
                         
                         while(!canContinue)
                         {
                             msg = new Message();
                             msg.what = 1;
                             msg.obj = encoders.clone();
                             msg.getData().putString("log", "Get encoders val");
                             mHandler.sendMessage(msg);
                             
                             try {
                                 Thread.sleep(300);
                             } catch (InterruptedException e) {
                                 // TODO Auto-generated catch block
                                 e.printStackTrace();
                             }
                         }
                         endEvent = 0;
                         
                         encoders = new byte[4];
                         encoders [0] = 1;
                         encoders [1] = Protocol.SMSG_ENCODER_STOP;
                         encoders [2] = 1;
                         encoders [3] = 1; // also clear encoders
                         msg = new Message();
                         msg.what = 1;
                         msg.obj = encoders.clone();
                         msg.getData().putString("log", "Stopping encoders");
                         mHandler.sendMessage(msg);
                         break;
                 }
                 
             }
             msg = new Message();
             msg.what = 3;
             msg.getData().putString("log", "Playback finished.");
             mHandler.sendMessage(msg);
         }
         public byte[] Movement(boolean down)
         {
             byte[] packet = new byte[5];
             packet[0] = 1;
             packet[1] = Protocol.SMSG_SET_MOVEMENT; // SMSG_SET_MOVEMENT
             packet[2] = 2;    // lenght
             packet[3] = speed;  // speed
             packet[4] = down ? moveFlags : 0;// moveflags, 0 to stop on release
             return packet;
         }
         public byte LetterToFlags(byte character)
         {
             switch(character)
             {
                 case (byte)'W':
                     return controlAPI.MOVE_FORWARD;
                 case (byte)'S':
                     return controlAPI.MOVE_BACKWARD;
                 case (byte)'A':
                     return controlAPI.MOVE_LEFT;
                 case (byte)'D':
                     return controlAPI.MOVE_RIGHT;
             }
             return 0;
         }
         
         public void cancel()
         {
             stop = true;
             canContinue = true;
         }
         
         public void PacketReceived(Packet pkt)
         {
             switch(endEvent)
             {
                 case 0:
                     return;
                 case 2:
                     if(endData != 8 || pkt.getOpcode() != Protocol.CMSG_EMERGENCY_START) // TODO NYI
                         return;
                     canContinue = true;
                     break;
                 case 4:
                 case 5:
                     if(pkt.getOpcode() != Protocol.CMSG_GET_RANGE_VAL)
                         return;
                     pkt.setPos((byte)1);
                     if((endEvent == 4 && pkt.readUInt16() >= endData) ||
                         (endEvent == 5 && pkt.readUInt16() <= endData))
                         canContinue = true;
                     break;
                 case 6:
                 case 7:
                 case 8:
                     if(pkt.getOpcode() != Protocol.CMSG_ENCODER_SEND)
                         break;
                     pkt.setPos((byte)0);
                     int left = pkt.readUInt16();
                     int right = pkt.readUInt16();
                     if((endEvent == 6 && (left + right)/2 > endData) ||
                         (endEvent == 7 && left > endData) ||
                         (endEvent == 8 && right > endData))
                         canContinue = true;
                     break;
             }
         }        
     }
     
     private float fabs(float val)
     {
         if(val < 0)
             return -val;
         return val;
     }
     public void SetDefXY(float x, float y) { mDefX = x; mDefY = y; }
     public float GetDefX() { return mDefX; }
     public float GetDefY() { return mDefY; }
 
     public class MTView extends SurfaceView implements SurfaceHolder.Callback
     {
         private int width, height;
         Paint mLine = new Paint(Paint.ANTI_ALIAS_FLAG);
         Paint mCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
         Context context = null;
 
         public MTView(Context context) {
             super(context);
             this.context = context;
             SurfaceHolder holder = getHolder();
             holder.addCallback(this);
             setFocusable(true); // make sure we get key events
             setFocusableInTouchMode(true); // make sure we get touch events
             mLine.setColor(0xFFFF0000);
             mLine.setStyle(Style.STROKE);
             mLine.setStrokeWidth(3);
             mCircle.setColor(0xFFFFFF00);
             mCircle.setStyle(Style.FILL);
         }
 
         @Override
         public boolean onTouchEvent(MotionEvent event)
         {
             Canvas c = getHolder().lockCanvas();
             if (c == null)
                 return true;
             
             c.drawColor(Color.BLACK);
             c.drawCircle(width/2, height/2, width/2, mLine);
             c.drawCircle(width/2, height/2, (float) ((width/2)*0.66), mLine);
             c.drawCircle(width/2, height/2, (float) ((width/2)*0.33), mLine);
             c.drawLine((width/2)-20, height/2, (width/2)+20, height/2, mLine);
             c.drawLine(width/2, (height/2)-20, width/2, (height/2)+20, mLine);
             
             float dx = event.getX() - width/2;
             float dy = event.getY() - height/2;
             float dist = (float) Math.sqrt((dx*dx) + (dy*dy));
             
             if (event.getAction() != MotionEvent.ACTION_UP && dist <= width/2)
             {
                 c.drawCircle(event.getX(), event.getY(), 40, mCircle);
                 c.drawLine(0, event.getY(), width, event.getY(), mLine);
                 c.drawLine(event.getX(), 0, event.getX(), height, mLine);    
             }
             Message msg = new Message();
             msg.obj = event;
             msg.arg1 = width;
             msg.arg2 = height;
             ((YuniClient)context).ballHandler.sendMessage(msg);
             
             getHolder().unlockCanvasAndPost(c);
             return true;
         }
 
 
         public void surfaceCreated(SurfaceHolder holder) {
         }
 
         public void surfaceDestroyed(SurfaceHolder holder) {
         }
 
         public void surfaceChanged(SurfaceHolder holder, int arg1, int width,
                 int height) {
             this.width = width;
             this.height = height;
             
             Canvas c = holder.lockCanvas();
             
             c.drawColor(Color.BLACK);
             c.drawCircle(width/2, height/2, width/2, mLine);
             c.drawCircle(width/2, height/2, (float) ((width/2)*0.66), mLine);
             c.drawCircle(width/2, height/2, (float) ((width/2)*0.33), mLine);
             c.drawLine((width/2)-20, height/2, (width/2)+20, height/2, mLine);
             c.drawLine(width/2, (height/2)-20, width/2, (height/2)+20, mLine);
             holder.unlockCanvasAndPost(c);
         }
     }
     
     public byte[] BallXYToFlags(float x, float y, int width, int height)
     {
         float dx = x - width/2;
         float dy = y - height/2;
         float dist = (float) Math.sqrt((dx*dx) + (dy*dy));
         float distPct = (float) (dist/(((float)(width/2))/100.0));
         
         byte speed = 127;
         if(distPct < 33)
             speed = 50;
         else if(distPct < 66)
             speed = 100;
         
         byte[] flags = {0, speed };
         
                     
         float ang = (float) Math.atan2(dy, dx);                                                                                                 
         ang = (float) ((ang >= 0) ? ang : 2 * 3.141592 + ang);  
         
         if(ang > 5.18362 || ang <  1.09956) // 0.7 PI range
             flags[0] |= MOVE_FORWARD;
         else if(ang > 2.04203 && ang < 4.24115) 
             flags[0] |= MOVE_BACKWARD;
         
         if(ang > 0.471236 && ang < 2.67036)
             flags[0] |= MOVE_RIGHT;
         else if(ang > 3.61283 && ang <  5.81195)
             flags[0] |= MOVE_LEFT;
         
         return flags;
         
     }
     
     private float mDefX = 0;
     private float mDefY = 0;
 
     private PlayThread playThread;
     private byte apiType;
     private Handler mHandler;
         
 };
