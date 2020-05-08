 /**
  * File SavingSong.java
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
 package de.hotware.hotsound.audio.player;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
import java.net.URLConnection;
 
 import javax.sound.sampled.AudioFileFormat;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import de.hotware.hotsound.audio.data.IAudio;
 import de.hotware.hotsound.audio.data.WaveAudio;
 
 
 public class SavingSong extends BasicPlaybackSong {
 	
 	public SavingSong(URL pURL) {
 		super(pURL);
 	}
 
 	public SavingSong(URI pURI) throws MalformedURLException {
 		super(pURI);
 	}
 
 	public SavingSong(File pFile) throws MalformedURLException {
 		super(pFile);
 	}
 	
 	@Override
 	public IAudio getAudio() throws MusicPlayerException {
 		try {
 			AudioFileFormat audioFileFormat;
 			if(this.mURL.getProtocol().toLowerCase().equals("file")) {
 				try {
 					audioFileFormat = AudioSystem.getAudioFileFormat(new File(this.mURL.toURI()));
 				} catch(URISyntaxException e) {
 					audioFileFormat = AudioSystem.getAudioFileFormat(new File(this.mURL.getPath()));
 				}
 			} else {
 				audioFileFormat = AudioSystem.getAudioFileFormat(this.mURL);
 			}
 			this.mAudioFormat = audioFileFormat.getFormat();
			URLConnection uc = this.mURL.openConnection();
			return new WaveAudio(uc.getInputStream(), audioFileFormat.getFrameLength());
 		} catch(IOException | UnsupportedAudioFileException e) {
 			throw new MusicPlayerException("Exception occured while getting the IAudio from this ISong", e);
 		}
 	}
 
 }
