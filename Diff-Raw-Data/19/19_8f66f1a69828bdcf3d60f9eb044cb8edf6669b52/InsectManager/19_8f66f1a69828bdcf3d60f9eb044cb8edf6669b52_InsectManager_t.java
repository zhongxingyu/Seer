 package com.howfun.android.insectrush;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import android.graphics.Rect;
 import android.os.Handler;
 import android.os.Message;
 
 import com.howfun.android.insect.Insect;
 
 public class InsectManager {
 
    private static final long DELAY_MILLIS = 200L;
 
    private static final int MSG_UPDATE_SCREEN = 1;
    private static final int MSG_KILL_INSECT = 2;
 
    private static final int MAX_INSECT_NUM = 20;
 
    private ScreenView mScreenView;
 
    private Queue<Insect> mInsectQueue = null;
 
    private Handler mHandler = new Handler() {
       @Override
       public void handleMessage(Message msg) {
          switch (msg.what) {
          case MSG_UPDATE_SCREEN:
             update();
             break;
         case MSG_KILL_INSECT:
            Insect insect = (Insect) msg.obj;
            if (mInsectQueue.contains(insect)) {
               mInsectQueue.remove(insect);
            }
            break;
          default:
             break;
          }
       }
    };
 
    private Thread mThread = new Thread() {
 
       public void run() {
          while (true) {
             try {
                sleep(DELAY_MILLIS);
             } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
             }
             mHandler.sendEmptyMessage(MSG_UPDATE_SCREEN);
 
          }
       }
    };
 
    public InsectManager() {
       mInsectQueue = new LinkedList<Insect>();
       mThread.start();
    }
 
    public void addInsect(Insect insect) {
 
       if (insect == null) {
          return;
       }
 
       if (mInsectQueue.size() == MAX_INSECT_NUM) {
          mInsectQueue.remove();
       }
 
       mInsectQueue.add(insect);
       mScreenView.addView(insect);
       Rect rect = insect.getRect();
       insect.layout(rect.left, rect.top, rect.right, rect.bottom);
 
       Message msg = new Message();
       msg.what = MSG_KILL_INSECT;
       msg.obj = insect;
       mHandler.sendMessageDelayed(msg, insect.getLife());
    }
 
    private void setNextPosition() {
 
       if (mInsectQueue.size() == 0)
          return;
 
       Iterator<Insect> it = mInsectQueue.iterator();
       while (it.hasNext()) {
          Insect insect = it.next();
          int x = insect.getCenterX();
          int y = insect.getCenterY();
          int step = insect.getStep();
          int direction = insect.getDirection();
          switch (direction) {
          case Insect.NORTH:
             insect.setCenter(x, y - step);
             break;
          case Insect.SOUTH:
             insect.setCenter(x, y + step);
             break;
          case Insect.WEST:
             insect.setCenter(x - step, y);
             break;
          case Insect.EAST:
             insect.setCenter(x + step, y);
             break;
          default:
             break;
          }
       }
 
    }
 
    private void renderScreen() {
 
       if (mInsectQueue.size() == 0) {
          return;
       }
 
       mScreenView.removeAllViews();
       Iterator<Insect> it = mInsectQueue.iterator();
       while (it.hasNext()) {
          Insect insect = it.next();
          Rect rect = insect.getRect();
          mScreenView.addView(insect);
          insect.layout(rect.left, rect.top, rect.right, rect.bottom);
       }
 
    }
 
    private void update() {
       setNextPosition();
       renderScreen();
    }
 
    public void clear() {
       mInsectQueue.clear();
       mScreenView.removeAllViews();
    }
 
    public void setScreenView(ScreenView screenView) {
       this.mScreenView = screenView;
    }
 
 }
