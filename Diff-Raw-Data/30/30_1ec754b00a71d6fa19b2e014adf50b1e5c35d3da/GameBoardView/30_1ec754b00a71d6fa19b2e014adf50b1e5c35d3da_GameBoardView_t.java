 package com.zhideel.tapathon.ui;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import com.squareup.otto.Bus;
import com.zhideel.tapathon.Config;
 import com.zhideel.tapathon.R;
 import com.zhideel.tapathon.logic.CommunicationBus;
 
 import java.util.ArrayList;
 
 /**
  * Created by Adeel on 14/11/13.
  */
 public class GameBoardView implements CommunicationBus.BusManager {
     private Activity mContext;
     private final Bus mBus;
     private GridView gridview;
 
     public GameBoardView(Context context, MultiTouchView.GameLevel level, ViewGroup viewGroup) {
         MultiTouchView.setLevel(level);
         mContext = (Activity) context;
         this.mBus = CommunicationBus.getInstance();
 
         View.inflate(mContext, R.layout.view_game_pad, viewGroup);
         gridview = (GridView) viewGroup.findViewById(R.id.gridview);
         gridview.setAdapter(new PadAdapter(mContext));
 
     }
 
     public void resetBoard() {
         AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
         anim.setDuration(500);
         anim.setRepeatMode(Animation.REVERSE);
         gridview.startAnimation(anim);
         gridview.setAdapter(new PadAdapter(mContext));
     }
 
     public void pauseBoard(boolean paused) {
         ListAdapter adapter = gridview.getAdapter();
         for (int i = 0; i < adapter.getCount(); i++) {
             MultiTouchView view = (MultiTouchView) adapter.getItem(i);
             view.setPaused(paused);
         }
     }
 
     @Override
     public void startBus() {
         mBus.register(this);
     }
 
     @Override
     public void stopBus() {
         mBus.unregister(this);
     }
 
 }
 
 
 class PadAdapter extends BaseAdapter {
     private Context mContext;
     private ArrayList<MultiTouchView> pads;
 
     public PadAdapter(Context c) {
         mContext = c;
         pads = new ArrayList<MultiTouchView>();
         for (int i = 0; i < getCount(); i++) {
             MultiTouchView pad;
             pad = new MultiTouchView(mContext, null);
             pad.setAlpha(0.8f);
             pads.add(pad);
         }
     }
 
     public int getCount() {
         return 9;
     }
 
     public Object getItem(int position) {
         return pads.get(position);
     }
 
     public long getItemId(int position) {
         return 0;
     }
 
     public View getView(int position, View convertView, ViewGroup parent) {
 
         MultiTouchView pad = (MultiTouchView) getItem(position);
         GridView.LayoutParams params = new GridView.LayoutParams(parent.getWidth() / 3 - 10,
                parent.getHeight() / 3 - Config.getDipfromPixels(3));
         pad.setLayoutParams(params);
         return pad;
     }
 }
