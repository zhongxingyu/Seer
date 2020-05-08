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
 
 
 public class Xmp {
 	
 	// limits
 	public static final int MAX_KEYS = 121;			/* Number of valid keys */
 	public static final int MAX_ENV_POINTS = 32;	/* Max number of envelope points */
 	public static final int MAX_MOD_LENGTH = 256;	/* Max number of patterns in module */
 	public static final int MAX_CHANNELS = 64;		/* Max number of channels in module */
 	public static final int MAX_SRATE = 49170;		/* max sampling rate (Hz) */
 	public static final int MIN_SRATE = 4000;		/* min sampling rate (Hz) */
 	public static final int MIN_BPM = 20;			/* min BPM */
 	/* frame rate = (50 * bpm / 125) Hz */
 	/* frame size = (sampling rate * channels * size) / frame rate */
 	public static final int MAX_FRAMESIZE = 5 * MAX_SRATE * 2 / MIN_BPM;
 
 	// error codes
 	static final int ERROR_INTERNAL = 2;
 	static final int ERROR_SYSTEM = 6;
 	static final int ERROR_INVALID = 7;
 	static final int ERROR_STATE = 8;
 
 	static final String[] errorString = {
 		"No error",
 		"End of module",
 		"Internal error",
 		"Unknown module format",
 		"Can't load module",
 		"Can't decompress module",
 		"System error",
 		"Invalid parameter"
 	};
 
 	// native API methods
 	native static long createContext();
 	native static void freeContext(long ctx);
 	native static int loadModule(long ctx, String path);
 	native static int testModule(String path, TestInfo info);
 	native static void releaseModule(long ctx);
 	native static int startPlayer(long ctx, int freq, int mode);
 	native static int playFrame(long ctx);
 	native static void getFrameInfo(long ctx, FrameInfo info);
 	native static void endPlayer(long ctx);
 	native static void getModuleInfo(long ctx, ModuleInfo info);
 	native static int setPlayer(long ctx, int param, int value);
 	native static int getPlayer(long ctx, int param);
 	native static int injectEvent(long ctx, int chn, Event event);
 	native static String[] getFormatList();
 	native static int nextPosition(long ctx);
 	native static int prevPosition(long ctx);
 	native static int setPosition(long ctx, int num);
 	native static void scanModule(long ctx);
 	native static void stopModule(long ctx);
 	native static void restartModule(long ctx);
 	native static int seekTime(long ctx, int time);
 	native static int channelMute(long ctx, int chn, int val);
 	
 	// native helpers
 	native static int getErrno();
 	native static String getStrError(int err);
 	native static void getModData(long ctx, Module mod);
 	native static void getInstrumentData(long ctx, Instrument instrument, int num);
 	native static void getPatternData(long ctx, Pattern pattern, int num);
 
 	static {
 		System.loadLibrary("xmp-jni");
 	}
 
 }
