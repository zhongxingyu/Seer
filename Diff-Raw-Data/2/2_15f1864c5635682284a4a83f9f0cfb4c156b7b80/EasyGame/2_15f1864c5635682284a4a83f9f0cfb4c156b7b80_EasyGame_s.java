 package com.space.piano;
 
 import java.util.Vector;
 
 public class EasyGame extends Game {
 
     private static final int QUEUE_LEN_IN_MS = 200; // 200ms
 
     private static final int SCORE_FOR_BAD_KEY = 10;
 
     private int mTime;
     private int mRefTime;
     private long mLastPausedAt;
     private float mSpeed = 0.5f;
     private boolean mTimeRunning = false;
     private Vector<NoteEvent> mWaitingFor = new Vector<NoteEvent>();
     private Vector<NoteEvent> mQueue = new Vector<NoteEvent>();
 
     public EasyGame(App app) {
         super(app);
     }
 
     @Override
     public void reset() {
         super.reset();
         mRefTime = -5*1000;
         mLastPausedAt = System.currentTimeMillis();
         mTimeRunning = true;
     }
 
     private void resumeTime() {
         long now = System.currentTimeMillis();
         int delay = (int) (now - mLastPausedAt) / 100;
         addScore(delay);
         mTimeRunning = true;
         mLastPausedAt = now;
     }
 
     private void pauseTime() {
         mTimeRunning = false;
         mRefTime += (int) ((System.currentTimeMillis() - mLastPausedAt) * mSpeed);
         mLastPausedAt = System.currentTimeMillis();
     }
 
     @Override
     protected void tick() {
         super.tick();
         if (mTimeRunning) {
             mTime = (int) ((System.currentTimeMillis() - mLastPausedAt) * mSpeed) + mRefTime;
             SongView songView = getApp().getSongView();
             songView.setTime(mTime);
             cleanupQueue(mTime);
             synchronized (mWaitingFor) {
                 songView.getNewNoteEvents(mWaitingFor);
                 filterEvents(mWaitingFor);
                 if (mWaitingFor.size() > 0) {
                     pauseTime();
                 }
             }
         } else {
             synchronized (mWaitingFor) {
                 if (mWaitingFor.size() == 0) {
                     resumeTime();
                 }
             }
         }
     }
 
     private void filterEvents(Vector<NoteEvent> vec) {
         int cnt = vec.size();
         for (int i = cnt - 1; i >= 0; i--) {
             NoteEvent ev = vec.get(i);
             if (!getApp().isKeyVisible(ev.getMidiNote())) {
                 getApp().play(ev.getMidiNote(), ev.isOn());
                 vec.remove(i);
             } else if (ev.isOn() == false) {
                 vec.remove(i);
             } else if (removeFromQueue(ev)) {
                 vec.remove(i);
             }
         }
     }
 
     private boolean removeFromQueue(NoteEvent ev) {
         for (NoteEvent tmp : mQueue) {
             if (tmp.getMidiNote() == ev.getMidiNote() && tmp.isOn() == ev.isOn()) {
                 mQueue.remove(tmp);
                 return true;
             }
         }
         return false;
     }
 
     public void onKey(int note, boolean on) {
         synchronized (mWaitingFor) {
             int cnt = mWaitingFor.size();
             for (int i = cnt - 1; i >= 0; i--) {
                 NoteEvent ev = mWaitingFor.get(i);
                 if (ev.getMidiNote() == note && ev.isOn() == on) {
                     mWaitingFor.remove(i);
                     return;
                 }
             }
         }
 
         // If we got here, then the key was not needed yet
         // so add it to the waiting queue (but first remove some old events)
         int now = mRefTime;
         if (mTimeRunning) {
             now += (int) ((System.currentTimeMillis() - mLastPausedAt) * mSpeed);
         }
         cleanupQueue(now);
         mQueue.add(new NoteEvent(note, now, on));
     }
 
     private void cleanupQueue(int now) {
         while (mQueue.size() > 0) {
             NoteEvent ev = mQueue.get(0);
             if (now - ev.getTime() > QUEUE_LEN_IN_MS) {
                 mQueue.remove(0);
                 // Add score as well for wrong keys
                 if (ev.isOn()) {
                     addScore(SCORE_FOR_BAD_KEY);
                 }
             } else {
                 break;
             }
         }
     }
 
 }
