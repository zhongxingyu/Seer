 /**
  * File IAudio.java
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
 
 import de.hotware.hotsound.audio.player.MusicPlayerException;
 
 /**
 * All Audio data is being read from subclasses of this interface. Has to be reopenable.
  *
  * @author Martin Braun
  */
 public interface Audio extends AutoCloseable {
 
 	/**
 	 * @return the AudioFormat of the IAudio.
 	 * doesn't throw exceptions!
 	 */
 	public AudioFormat getAudioFormat();
 
 	/**
 	 * Reads up to a specified maximum number of bytes of data from the audio
 	 * stream, putting them into the given byte array. This method will always
 	 * read an integral number of frames. If pLength does not specify an
 	 * integral number of frames, a maximum of len - (pLength % frameSize) bytes
 	 * will be read.
 	 *
 	 * @throws IllegalStateException
 	 *             if not opened yet
 	 */
 	public int read(byte[] pData, int pStart, int pLength) throws AudioException;
 
 	/**
 	 * @throws IllegalStateException
 	 *             if opened while not being closed
 	 */
 	public void open() throws AudioException;
 	
 	public boolean isClosed();
 
 	/**
 	 * closes the IAudios resources
 	 */
 	@Override
 	public void close() throws AudioException;
 
 	public static class AudioException extends MusicPlayerException {
 
 		private static final long serialVersionUID = 2153542499704614401L;
 
 		public AudioException() {
 			super();
 		}
 
 		public AudioException(String pMessage) {
 			super(pMessage);
 		}
 
 		public AudioException(String pMessage, Throwable pCause) {
 			super(pMessage, pCause);
 		}
 
 	}
 
 }
