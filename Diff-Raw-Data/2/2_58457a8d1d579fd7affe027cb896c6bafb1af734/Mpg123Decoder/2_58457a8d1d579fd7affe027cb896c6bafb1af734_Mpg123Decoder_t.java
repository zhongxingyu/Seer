 /*******************************************************************************
  * Copyright 2012 See AUTHORS file.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.marekstoj.droidaudio;
 
 import java.io.File;
 
 /**
  * @author badlogicgames@gmail.com
  * @author marek.stoj@gmail.com
  */
 public class Mpg123Decoder extends Decoder {
   
 	static {
    System.loadLibrary("droidaudio");
 	}
 	
 	private final long _handle;
 
 	/**
    * Opens the given file for mp3 decoding.
    * Only files stored on external storage (ie. SD card are supported).
 	 */
 	public Mpg123Decoder(File file) {
     _handle = openFile(file.getAbsolutePath());
 	}
 
 	@Override
 	public int readSamples (short[] samples, int offset, int numSamples) {
 		int readSamplesCount = readSamples(_handle, samples, offset, numSamples);
     
 		return readSamplesCount;
 	}
 
 	@Override
 	public int skipSamples (int numSamples) {
 		return skipSamples(_handle, numSamples);
 	}
 
   @Override
 	public int getChannels () {
 		return getNumChannels(_handle);
 	}
 
   @Override
 	public int getRate () {
 		return getRate(_handle);
 	}
 
   @Override
 	public float getLength () {
 		return getLength(_handle);
 	}
 	
   @Override
   public long getSamplesCount() {
     return length(_handle);
   }
   
   @Override
   public long getCurrentSample() {
     return tell(_handle);
   }
   
   @Override
   public long seekToSample(long offset) {
     return seek(_handle, offset);
   }
   
 	@Override
 	public void dispose () {
 		closeFile(_handle);
   }
 
 	private native long openFile(String filename);
 
 	private native int readSamples(long handle, short[] buffer, int offset, int numSamples);
 
 	private native int skipSamples(long handle, int numSamples);
   
 	private native int getNumChannels(long handle);
 
 	private native int getRate(long handle);
 
 	private native float getLength(long handle);
 
 	private native void closeFile(long handle);
 
   private native long length(long handle);
   
   private native long tell(long handle);
   
 	private native long seek(long handle, long offset);
   
 }
