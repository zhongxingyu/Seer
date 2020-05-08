 /**
  * File BasicPlaybackAudioDevice.java
  * ---------------------------------------------------------
  *
  * Copyright (C) 2012 Martin Braun (martinbraun123@aol.com)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
  * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * - The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * - The origin of the software must not be misrepresented.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  * TL;DR: As long as you clearly give me credit for this Software, you are free to use as you like, even in commercial software, but don't blame me
  *   if it breaks something.
  */
 package de.hotware.hotsound.audio.data;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.Mixer;
 import javax.sound.sampled.SourceDataLine;
 
 /**
  * Default audio device for playing back audio
  */
 public class BasicPlaybackAudioDevice extends BaseAudioDevice implements IJavaSoundPlaybackAudioDevice {
 
 	protected Mixer mMixer;
 	protected SourceDataLine mSourceDataLine;
 	protected Class<? extends SourceDataLine> mSourceDataLineClass;
 	protected int mBufferSize;
 
 	public BasicPlaybackAudioDevice() {
 		this(null);
 	}
 
 	public BasicPlaybackAudioDevice(Mixer pMixer) {
 		this(pMixer, AudioSystem.NOT_SPECIFIED);
 	}
 
 	/**
 	 * @param pBufferSize
 	 *            the buffer size you want to use. this is just a hint. it may
 	 *            not be used
 	 */
 	public BasicPlaybackAudioDevice(Mixer pMixer, int pBufferSize) {
 		this(pMixer, pBufferSize, SourceDataLine.class);
 	}
 	
 	public BasicPlaybackAudioDevice(Mixer pMixer, int pBufferSize, Class<? extends SourceDataLine> pSourceDataLineClass) {
 		super();
 		this.mMixer = pMixer;
 		this.mBufferSize = pBufferSize;
 		this.mSourceDataLineClass = pSourceDataLineClass;
 	}
 
 	@Override
 	public void open(AudioFormat pAudioFormat) throws AudioDeviceException {
 		super.open(pAudioFormat);
 		DataLine.Info dataLineInfo = new DataLine.Info(this.mSourceDataLineClass,
 				pAudioFormat,
 				this.mBufferSize);
 		try {
 			if(this.mMixer == null) {
 				this.mSourceDataLine = (SourceDataLine) AudioSystem
 						.getLine(dataLineInfo);
 			} else {
 				if(!this.mMixer.isOpen()) {
 					this.mMixer.open();
 				}
 				this.mSourceDataLine = (SourceDataLine) this.mMixer
 						.getLine(dataLineInfo);
 			}
 			this.mSourceDataLine.open(pAudioFormat);
 		} catch(LineUnavailableException e) {
 			this.mClosed = true;
 			throw new AudioDeviceException("The AudioDevices' line is not available",
 					e);
 		}
 		this.mSourceDataLine.start();
 	}
 
 	@Override
 	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException {
 		super.write(pData, pStart, pLength);
 		return this.mSourceDataLine.write(pData, pStart, pLength);
 	}
 
 	@Override
 	public void pause(boolean pPause) {
 		super.pause(pPause);
		if(this.mSourceDataLine != null) {
			if(pPause) {
				this.mSourceDataLine.stop();
			} else {
				this.mSourceDataLine.start();
			}
 		}
 	}
 
 	@Override
 	public void close() throws AudioDeviceException {
 		super.close();
 		if(this.mSourceDataLine != null) {
 			this.mSourceDataLine.stop();
 			this.mSourceDataLine.flush();
 			this.mSourceDataLine = null;
 		}
 	}
 
 	@Override
 	public void setMixer(Mixer pMixer) {
 		if(!this.mClosed) {
 			throw new IllegalStateException("can't set the mixer if the AudioDevice is not stopped");
 		}
 		this.mMixer = pMixer;
 	}
 
 	@Override
 	public DataLine getDataLine() {
 		return this.mSourceDataLine;
 	}
 
 }
