 package tw.edu.ntu.csie.mhci.tapassist.utils;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.content.Context;
 import android.view.MotionEvent;
 
 public class LogHelper {
 
 	private final static String FILE_NAME = "log.txt";
 
 	public static void wirteLogTouchEvent(Context context, MotionEvent event) {
 		int count = event.getPointerCount();
 		for (int i = 0; i < count; i++) {
			String data = String.format("%ld %d %s %f %f", event.getEventTime(), i,
					getTouchAction(event), event.getX(i), event.getY(i));
 			wirte(context, data);
 		}
 
 	}
 
 	public static void wirteLogTask(Context context, MotionEvent event,
 			String action, int task) {
		String data = String.format("%ld %d %s %f %f", event.getEventTime(), -1,
 				action, event.getRawX(), event.getRawY());
 		wirte(context, data);
 	}
 
 	public static void wirte(Context context, String data) {
 		FileOutputStream fo;
 		try {
 			fo = context.openFileOutput(FILE_NAME, Context.MODE_APPEND);
 			data = data + "\n";
 			fo.write(data.getBytes());
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static String read(Context context) {
 		try {
 			FileInputStream fi = context.openFileInput(FILE_NAME);
 			byte[] buffer = new byte[1024];
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
 			while (fi.read(buffer) != -1) {
 				baos.write(buffer);
 			}
 			return new String(baos.toByteArray());
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static String getTouchAction(MotionEvent event) {
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_CANCEL:
 			return "ACTION_CANCEL";
 		case MotionEvent.ACTION_DOWN:
 			return "ACTION_DOWN";
 		case MotionEvent.ACTION_MASK:
 			return "ACTION_MASK";
 		case MotionEvent.ACTION_MOVE:
 			return "ACTION_MOVE";
 		case MotionEvent.ACTION_OUTSIDE:
 			return "ACTION_OUTSIDE";
 		case MotionEvent.ACTION_SCROLL:
 			return "ACTION_SCROLL";
 		case MotionEvent.ACTION_UP:
 			return "ACTION_UP";
 		}
		return null;
 	}
 }
