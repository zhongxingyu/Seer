 /*
  * Java API for libxmp
  * Copyright (C) 2013 Claudio Matsuoka
  *
  * Permission is hereby granted, free of charge, to any person obtaining a
  * copy of this software and associated documentation files (the "Software"),
  * to deal in the Software without restriction, including without limitation
  * the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package org.helllabs.java.libxmp;
 
 import java.nio.ByteBuffer;
 
 
 public class Player {
 	private final int samplingRate;
 	private final int mode;
 	private final long ctx;
 	
 	public static class Callback {
 		public boolean callback(FrameInfo fi, Object args) {
 			return true;
 		}
 	}
 	
 	public Player() {
 		this(44100);
 	}
 	
 	public Player(int samplingRate) {
 		this(samplingRate, 0);
 	}	
 
 	public Player(int samplingRate, int mode) {
 		this.samplingRate = samplingRate;
 		this.mode = mode;
 		ctx = Xmp.createContext();
 	}		
 
 	@Override
 	protected void finalize() {
 		Xmp.freeContext(ctx);
 	}
 	
 	long getContext() {
 		return ctx;
 	}
 	
 	public Player start() {
 		final int code = Xmp.startPlayer(ctx, samplingRate, mode);
 		switch (code) {
 		case -Xmp.ERROR_INTERNAL:
 			throw new RuntimeException(Xmp.errorString[-code]);
 		case -Xmp.ERROR_INVALID:
 			throw new IllegalArgumentException("Invalid sampling rate " + samplingRate + "Hz");
 		case -Xmp.ERROR_SYSTEM:
 			break;
 			
 		// What do do if no module loaded?
 			
 			
 			
 		}
 		
 		return this;
 	}
 	
     public Player end() {
         Xmp.endPlayer(ctx);
         return this;
     }
 
     public void set(int param, int value) {
         final int code = Xmp.setPlayer(ctx, param, value);
         if (code == Xmp.ERROR_INVALID) {
         	throw new IllegalArgumentException("Invalid value " + value);
         }
     }
 
     public int get(int param) {
         return Xmp.getPlayer(ctx, param);
     }
 
     public void inject_event(int chn, Event event) {
         Xmp.injectEvent(ctx, chn, event);
     }
 
     public static String[] getFormatList() {
         return Xmp.getFormatList();
     }
 
     public int nextPosition() {
         return Xmp.nextPosition(ctx);
     }
 
     public int prevPosition() {
     	return Xmp.prevPosition(ctx);
     }
 
     public int setPosition(int num) {
     	return Xmp.setPosition(ctx, num);
     }
 
     public Player scan() {
     	Xmp.scanModule(ctx);
     	return this;
     }
 
 	public Player play() {
 		return play(null, false, null);
 	}
 	
 	public Player play(Callback callback) {
 		return play(callback, false, null);
 	}
 	
 	public Player play(Callback callback, boolean loop) {
 		return play(callback, loop, null);
 	}
 	
 	public Player play(Callback callback, boolean loop, Object args) {
 		FrameInfo fi = new FrameInfo();
 		start();
 		while (true) {
 			playFrame(fi);
 			
 			if (!loop && fi.loopCount > 0)
 				break;
 			
 			if (callback != null) {
 				if (!callback.callback(fi, args))
 					break;
 			}	
 		}
 		end();
 		
 		return this;
 	}
 
 	public Player playFrame() {
 		Xmp.playFrame(ctx);
 		return this;
 	}
 	
 	public Player playFrame(FrameInfo fi) {		
 		Xmp.playFrame(ctx);
 		getFrameInfo(fi);
 		return this;
 	}
 	
 	public Player resetBuffer() {
 		return this;
 	}
 	
 	public Player playBuffer(ByteBuffer buffer, int bufferSize, boolean loop) {
 
 		return this;
 	}
 	
 	public Player stop() {
 		Xmp.stopModule(ctx);
 		return this;
 	}
 	
 	public FrameInfo getFrameInfo(FrameInfo info) {
 		if (info == null)
 			info = new FrameInfo();
 		Xmp.getFrameInfo(ctx, info);
 		return info;
 	}
 	
 	public Player restart() {
 		Xmp.restartModule(ctx);
 		return this;
 	}
 	
     public int seekTime(int time) {
         return Xmp.seekTime(ctx, time);
     }
 
     public int channelMute(int chn, int val) {
         return Xmp.channelMute(ctx, chn, val);
     }
 
    public int channelVol(int chn, int val) {
        return Xmp.channelVol(ctx, chn, val);
    }
 
    public Player setInstrumentPath(String path) {
        Xmp.setInstrumentPath(ctx, path);
        return this;
    }
 
 }
