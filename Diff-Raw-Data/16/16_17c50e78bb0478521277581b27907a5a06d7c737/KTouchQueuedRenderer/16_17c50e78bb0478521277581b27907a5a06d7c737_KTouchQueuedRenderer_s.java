 package com.kapouta.katools;
 
 import java.util.Vector;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.opengl.GLSurfaceView.Renderer;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 public abstract class KTouchQueuedRenderer implements Renderer, OnTouchListener {
 	Vector<MotionEventData> events = null;
 
 	public KTouchQueuedRenderer() {
 		events = new Vector<MotionEventData>();
 	}
 
 	@Override
 	public synchronized void onDrawFrame(GL10 gl) {
 		if (events.size() > 0) {
 			MotionEventData curr_event = events.remove(0);
 
 			// System.out.println(curr_event);
 
 			switch (curr_event.action) {
 			case MotionEvent.ACTION_DOWN:
 				this.onTouchDown(gl, curr_event.x1, curr_event.y1);
 				break;
 
 			case MotionEvent.ACTION_MOVE:
 				// only one finger
 				if (curr_event.count == 1)
 					this.onTouchMove(gl, curr_event.x1, curr_event.y1);
 				else
 					// two or more (only ready for two)
 					this.onDualTouchMove(gl, curr_event.x1, curr_event.y1, curr_event.x2, curr_event.y2);
 				break;
 
 			case MotionEvent.ACTION_UP:
 				this.onTouchUp(gl, curr_event.x1, curr_event.y1);
 				break;
 
 			case MotionEvent.ACTION_POINTER_1_DOWN:
 				this.onTouchUp(gl, curr_event.x2, curr_event.y2);
 				this.onDualTouchDown(gl, curr_event.x1, curr_event.y1, curr_event.x2, curr_event.y2);
 				break;
 
 			case MotionEvent.ACTION_POINTER_2_DOWN:
 				this.onTouchUp(gl, curr_event.x1, curr_event.y1);
 				this.onDualTouchDown(gl, curr_event.x1, curr_event.y1, curr_event.x2, curr_event.y2);
 				break;
 
 			case MotionEvent.ACTION_POINTER_1_UP:
 				this.onDualTouchUp(gl, curr_event.x1, curr_event.y1, curr_event.x2, curr_event.y2);
 				this.onTouchDown(gl, curr_event.x2, curr_event.y2);
 				break;
 
 			case MotionEvent.ACTION_POINTER_2_UP:
 				this.onDualTouchUp(gl, curr_event.x1, curr_event.y1, curr_event.x2, curr_event.y2);
 				this.onTouchDown(gl, curr_event.x1, curr_event.y1);
 				break;
 
 			default:
 				System.out.println("Unknown: " + curr_event);
 			}
 		}
 	}
 
 	@Override
 	public void onSurfaceChanged(GL10 arg0, int arg1, int arg2) {
 
 	}
 
 	@Override
 	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
 
 	}
 
 	public void onTouchDown(GL10 gl, float x, float y) {
 		// System.out.println("One DOWN ( " + x + " , " + y + " )");
 	}
 
 	public void onTouchMove(GL10 gl, float x, float y) {
 		// System.out.println("One MOVE ( " + x + " , " + y + " )");
 	}
 
 	public void onTouchUp(GL10 gl, float x, float y) {
 		// System.out.println("One UP ( " + x + " , " + y + " )");
 	}
 
 	public void onDualTouchDown(GL10 gl, float x1, float y1, float x2, float y2) {
 		// System.out.println("Two DOWN ( " + x1 + " , " + y1 + " ; " + x2 + " , " + y2 + " ) ");
 	}
 
 	public void onDualTouchMove(GL10 gl, float x1, float y1, float x2, float y2) {
 		// System.out.println("Two MOVE ( " + x1 + " , " + y1 + " ; " + x2 + " , " + y2 + " ) ");
 	}
 
 	public void onDualTouchUp(GL10 gl, float x1, float y1, float x2, float y2) {
 		// System.out.println("Two UP ( " + x1 + " , " + y1 + " ; " + x2 + " , " + y2 + " ) ");
 	}
 
 	@Override
 	public boolean onTouch(View view, MotionEvent event) {
 		// System.out.println(event);
 
 		// by discarding MVOE events, this condition ensures that the event
 		// queue does not grow crazy when the user is moving around a lot.
 		if (MotionEvent.ACTION_MOVE != event.getAction() || events.size() == 0)
 			events.add(new MotionEventData(event));
 		return true;
 	}
 
 	protected class MotionEventData {
 		float x1, y1, x2, y2;
 		int action;
 		int count;
 
 		public MotionEventData(MotionEvent event) {
			int pid0 = event.getPointerId(0);
			int pid1;
			x1 = event.getX(pid0);
			y1 = event.getY(pid0);
 			if (event.getPointerCount() > 1) {
				pid1  = event.getPointerId(1);
				x2 = event.getX(pid1);
				y2 = event.getY(pid1);
 			}
 			count = event.getPointerCount();
 			action = event.getAction();
 		}
 
 		public String toString() {
 			return "A[" + action + "]  P1( " + x1 + " , " + y1 + " )  P2 " + x2 + " , " + y2 + " )";
 		}
 
 	}
 
 }
