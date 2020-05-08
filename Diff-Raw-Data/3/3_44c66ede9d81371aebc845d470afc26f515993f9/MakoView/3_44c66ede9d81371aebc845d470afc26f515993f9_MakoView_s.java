 package com.rtg.makovm;
 
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 
 import android.view.KeyEvent;
 import android.view.View;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.AttributeSet;
 import android.util.Log;
 
 import android.graphics.*;
 
 public class MakoView extends View {
 
 	private static final String TAG = "MakoView";
 	private static final int FRAME_RATE = 1000 / 60;
 	private MakoVM mVm;
 
 	public interface MakoViewListener {
 		public void makoViewStartLoading();
 
 		public void makoViewFinishLoading();
 
 		public void makoViewLoadError();
 	}
 
 	private MakoViewListener mListener = null;
 
 	public void setListener(MakoViewListener listener) {
 		mListener = listener;
 	}
 
 	private class RomLoadTask extends AsyncTask<String, Void, Boolean> {
 		private int[] mLoadedRom = null;
 
 		@Override
 		protected void onPreExecute() {
 			if(mListener!=null)
 			{
 				mListener.makoViewStartLoading();
 			}
 		}
 		
 		@Override
 		protected Boolean doInBackground(String... params) {
 			// Only can load one rom at a time... which makes sense
 			try {
 				mLoadedRom = loadRom(new FileInputStream(params[0]), null);
 				return true;
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 			return false;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			if (mListener != null) {
 				if (!result) {
 					mListener.makoViewLoadError();
 				}
 				else
 				{
 					mVm = new MakoVM(mLoadedRom);
 					postInvalidate(); // Start rendering
 				}
 				mListener.makoViewFinishLoading();
 				
 			}
 		}
 	}
 
 	public MakoView(Context c, AttributeSet a) {
 		super(c, a);
 	}
 
 	public void setRom(String filename) {
 		mVm = null;
 		new RomLoadTask().execute(filename);
 	}
 
 	private static int[] loadRom(InputStream i, int[] prev) {
 		try {
 			int romSize = i.available();
 
 			// Allocate a byteBuffer, this will be used later to convert the
 			// bytes
 			// into an integer array
 			ByteBuffer buffer = ByteBuffer.allocate(romSize);
 			buffer.clear();
 			byte[] page = new byte[4096];
 			int read = 0;
 			int totalRead = 0;
 			while (i.available() > 0) {
 				read = i.read(page, 0, 4096);
 				totalRead += read;
 				buffer.put(page, 0, read);
 			}
 			int[] rom = new int[totalRead / 4];
 			buffer.rewind();
 			IntBuffer intBuf = buffer.asIntBuffer();
 			intBuf.get(rom);
 			buffer.clear();
 			i.close();
 			Log.i(TAG, "Restored from save file!");
 			return rom;
 		} catch (IOException ioe) {
 			Log.e(TAG, "Unable to load rom!");
 			return prev;
 		}
 	}
 
 	public void setKeys(int mask) {
 		mVm.keys |= mask;
 	}
 
 	public void unsetKeys(int mask) {
 		mVm.keys -= (mVm.keys & mask);
 	}
 
 	@Override
 	public boolean dispatchKeyEvent(KeyEvent event) {
 		if (event.getAction() == KeyEvent.ACTION_DOWN) {
 			if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
 				keyPressed(8);
			} else {
 				keyPressed(event.getUnicodeChar());
 			}
 		}
 		return super.dispatchKeyEvent(event);
 	}
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		heightMeasureSpec = MeasureSpec.makeMeasureSpec(
 				(MeasureSpec.getSize(widthMeasureSpec) / 4) * 3,
 				MeasureSpec.EXACTLY);
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 	}
 
 	@Override
 	public void onDraw(Canvas c) {
 		super.onDraw(c);
 		if (isInEditMode()) {
 			c.drawColor(0xffff8800);
 			return;
 		}
 		
 		if(mVm == null)
 		{
 			return;
 		}
 		
 		c.save();
 		c.scale(2.5f, 2.5f);
 		c.drawBitmap(mVm.p, 0, 320, 0, 0, 320, 240, false, null);
 		c.restore();
 
 		mVm.run();
 
 		postInvalidateDelayed(FRAME_RATE);
 	}
 
 	public void keyPressed(int charAt) {
 		mVm.keyQueue.add(charAt);
 	}
 
 	public void keyReleased(int charAt) {
 		// Nothing?
 	}
 }
