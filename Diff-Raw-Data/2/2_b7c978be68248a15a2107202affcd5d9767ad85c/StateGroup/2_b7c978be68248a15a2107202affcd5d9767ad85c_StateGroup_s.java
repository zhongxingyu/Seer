 package com.luzi82.game;
 
 import java.util.TreeMap;
 
 import android.graphics.Canvas;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 
public abstract class StateGroup<M extends StateGroup<M, P>, P extends AbstractState<?>>
 		extends AbstractState<P> {
 
 	private TreeMap<String, AbstractState<M>> mStateMap = new TreeMap<String, AbstractState<M>>();
 
 	private AbstractState<M> mCurrentState;
 
 	private boolean mStateStarted = false;
 
 	protected StateGroup(P parent) {
 		super(parent);
 	}
 
 	public void addState(String key, AbstractState<M> state) {
 		mStateMap.put(key, state);
 	}
 
 	public void removeState(String key) {
 		mStateMap.remove(key);
 	}
 
 	public void setCurrentState(String key) {
 		if (mStateStarted && (mCurrentState != null)) {
 			mCurrentState.onStateEnd();
 		}
 		mCurrentState = mStateMap.get(key);
 		if (mStateStarted && (mCurrentState != null)) {
 			mCurrentState.onStateStart();
 		}
 	}
 
 	@Override
 	public void draw(Canvas c) {
 		if (mCurrentState != null) {
 			mCurrentState.draw(c);
 		}
 	}
 
 	@Override
 	public void onKeyDown(int keyCode, KeyEvent msg) {
 		if (mCurrentState != null) {
 			mCurrentState.onKeyDown(keyCode, msg);
 		}
 	}
 
 	@Override
 	public void onKeyUp(int keyCode, KeyEvent msg) {
 		if (mCurrentState != null) {
 			mCurrentState.onKeyUp(keyCode, msg);
 		}
 	}
 
 	@Override
 	public void onTouchEvent(MotionEvent event) {
 		if (mCurrentState != null) {
 			mCurrentState.onTouchEvent(event);
 		}
 	}
 
 	@Override
 	public void onGamePause() {
 		if (mCurrentState != null) {
 			mCurrentState.onGamePause();
 		}
 	}
 
 	@Override
 	public void onGameResume() {
 		if (mCurrentState != null) {
 			mCurrentState.onGameResume();
 		}
 	}
 
 	@Override
 	public void surfaceChanged(int format, int width, int height) {
 		if (mCurrentState != null) {
 			mCurrentState.surfaceChanged(format, width, height);
 		}
 	}
 
 	@Override
 	public void tick() {
 		if (mCurrentState != null) {
 			mCurrentState.tick();
 		}
 	}
 
 	@Override
 	public void onStateStart() {
 		mStateStarted = true;
 		if (mCurrentState != null) {
 			mCurrentState.onStateStart();
 		}
 	}
 
 	@Override
 	public void onStateEnd() {
 		mStateStarted = false;
 		if (mCurrentState != null) {
 			mCurrentState.onStateEnd();
 		}
 	}
 }
