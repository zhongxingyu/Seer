 /**
  * File BasicPlaybackAudio.java
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
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import de.hotware.hotsound.audio.util.AudioUtil;
 
 /**
  * converts to a playable format
  * 
  * @author Martin Braun
  */
 public class BasicPlaybackAudio extends BaseAudio implements ISeekableAudio {
 
 	protected InputStream mInputStream;
 	protected AudioInputStream mAudioInputStream;
 	protected long mFramePosition;
 	protected long mFrameSize;
 	protected long mFrameLength;
 
 	public BasicPlaybackAudio(InputStream pInputStream) {
 		this(pInputStream, AudioSystem.NOT_SPECIFIED);
 	}
 
 	public BasicPlaybackAudio(InputStream pInputStream, long pFrameLength) {
 		super();
 		this.mInputStream = pInputStream;
 		this.mFramePosition = 0;
 		this.mFrameSize = 0;
 		this.mFrameLength = pFrameLength;
 	}
 
 	/**
 	 * notice for overriding classes: mFrameSize is getting set in here.
 	 */
 	@Override
 	public void open() throws AudioException {
 		if(!this.mClosed) {
 			throw new IllegalStateException("The Audio is already opened");
 		}
 		try {
 			this.mAudioInputStream = AudioUtil
 					.getSupportedAudioInputStreamFromInputStream(this.mInputStream);
 			AudioFormat format = this.mAudioInputStream.getFormat();
 			this.mFrameSize = format.getFrameSize();
 		} catch(UnsupportedAudioFileException | IOException e) {
 			this.mClosed = true;
 			throw new AudioException("Error while opening the audiostream", e);
 		}
 		this.mClosed = false;
 	}
 
 	@Override
 	public int read(byte[] pData, int pStart, int pLength) throws AudioException {
 		if(this.mClosed) {
 			throw new IllegalStateException("The Audio is not opened");
 		}
 		try {
 			int read = this.mAudioInputStream.read(pData, pStart, pLength);
 			this.mFramePosition += read / this.mFrameSize;
 			return read;
 		} catch(IOException e) {
 			throw new AudioException("IOException while reading from the AudioInputStream",
 					e);
 		}
 	}
 
 	@Override
 	public void close() throws AudioException {
 		super.close();
 		try {
 			if(this.mAudioInputStream != null) {
 				this.mAudioInputStream.close();
 			}
 		} catch(IOException e) {
 			throw new AudioException("IOException while closing the AudioInputStream",
 					e);
 		}
 	}
 
 	@Override
 	public void seek(long pFrame) throws AudioException {
 		if(this.mClosed) {
 			throw new IllegalStateException("The Audio is not opened");
 		}
 		throw new UnsupportedOperationException("not implemented yet");
 	}
 
 	@Override
 	public void skip(long pFrames) throws AudioException {
 		if(this.mClosed) {
 			throw new IllegalStateException("The Audio is not opened");
 		}
 		double rate = ((double) pFrames) / this.mFrameLength;
 		long bytesToSkip = (long) Math.round(rate * this.mFrameLength *
 				this.mFrameSize);
 		long totalSkipped = 0;
 		try {
 			while(totalSkipped < bytesToSkip) {
 				long skipped = this.mAudioInputStream.skip(bytesToSkip -
 						totalSkipped);
 				totalSkipped += skipped;
 				if(skipped == 0) {
 					break;
 				}
 			}
 		} catch(IOException e) {
 			throw new AudioException("couldn't skip the audio");
 		}
 		this.mFramePosition += totalSkipped / this.mFrameSize;
 	}
 
 	@Override
 	public long getFramePosition() {
 		return this.mFramePosition;
 	}
 
 	@Override
 	public long getFrameLength() {
 		return this.mFrameLength;
 	}
 
 	@Override
 	public AudioFormat getAudioFormat() {
 		return this.mAudioInputStream.getFormat();
 	}
 
 }
