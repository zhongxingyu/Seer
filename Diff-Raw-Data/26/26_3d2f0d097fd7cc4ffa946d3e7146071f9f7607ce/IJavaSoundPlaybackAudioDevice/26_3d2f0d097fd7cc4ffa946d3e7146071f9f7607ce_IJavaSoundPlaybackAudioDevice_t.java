 /**
  * File IJavaSoundPlaybackAudioDevice.java
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
 
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.Mixer;
 
 /**
  * IPlaybackAudioDevice is used for actual playback devices. the normal
  * interface can play to whatever it likes (even to potatoes if they like the
  * sound of bytes)
  * 
  * @author Martin Braun
  */
 public interface IJavaSoundPlaybackAudioDevice extends IAudioDevice {
 
 	public void setMixer(Mixer pMixer);
 
 	/**
 	 * @return the DataLine to which is being written (with that you can control
 	 *         the volume, etc.)
	 *         may be null depending on the state of the audiodevice
 	 */
 	public DataLine getDataLine();
 
 }
