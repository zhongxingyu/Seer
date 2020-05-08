 /**
  * File ListStreamMusicPlayer.java
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
 package de.hotware.puremp3.console;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.locks.ReentrantLock;
 
 import de.hotware.hotsound.audio.player.IMusicListener;
 import de.hotware.hotsound.audio.player.ISong;
 import de.hotware.hotsound.audio.player.MusicPlayerException;
 import de.hotware.hotsound.audio.player.StreamMusicPlayer;
 
 public class ListStreamMusicPlayer extends StreamMusicPlayer implements
 		IListMusicPlayer {
 
 	/**
 	 * may never be null
 	 */
 	protected List<ISong> mSongs;
 	protected int mCurrent;
 	private ReentrantLock mLock;
 
 	public ListStreamMusicPlayer(IMusicListener pPlaybackListener) {
 		this(pPlaybackListener, null);
 	}
 
 	public ListStreamMusicPlayer(IMusicListener pPlaybackListener,
 			ExecutorService pExecutorService) {
 		super(pPlaybackListener, pExecutorService);
 		this.mLock = new ReentrantLock();
 		this.mMusicListener = pPlaybackListener;
 		this.mSongs = new ArrayList<ISong>();
 		this.mCurrent = 0;
 	}
 
 	@Override
 	public void setPlaylist(List<ISong> pPlaylist) throws MusicPlayerException {
 		this.mLock.lock();
 		try {
 			this.mSongs = pPlaylist;
 			this.mCurrent = 0;
 			if(this.mSongs.size() != 0) {
 				super.insert(this.mSongs.get(this.mCurrent));
 			}
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	public List<ISong> getPlayList(List<ISong> pPlaylist) {
 		this.mLock.lock();
 		try {
 			return this.mSongs;
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public void insert(ISong pSong) throws MusicPlayerException {
 		this.mLock.lock();
 		try {
			if(this.mPlayerRunnable == null || this.mPlayerRunnable.isStopped()) {
 				super.insert(pSong);
 			}
 			this.mSongs.add(pSong);
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public void stop() throws MusicPlayerException {
 		this.mLock.lock();
 		try {
 			super.stop();
 			this.mSongs = new ArrayList<ISong>();
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public void next() throws MusicPlayerException {
 		this.mLock.lock();
 		try {
 			if(this.mCurrent == this.mSongs.size() - 1) {
 				this.mCurrent = 0;
 			} else {
 				this.mCurrent++;
 			}
 			super.insert(this.mSongs.get(this.mCurrent));
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public void previous() throws MusicPlayerException {
 		this.mLock.lock();
 		try {
 			if(this.mCurrent == 0) {
 				this.mCurrent = this.mSongs.size() - 1;
 			} else {
 				this.mCurrent--;
 			}
 			super.insert(this.mSongs.get(this.mCurrent));
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public void play(int pX) throws MusicPlayerException {
 		this.mLock.lock();
 		try {
 			int size = this.mSongs.size();
 			if(pX < 0 || pX >= size || size == 0) {
 				throw new IllegalArgumentException("Song-Index not available");
 			}
 			this.mCurrent = pX;
 			super.insert(this.mSongs.get(pX));
 			super.start();
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public void insertAt(int pX) throws SongInsertionException {
 		throw new UnsupportedOperationException("not implemented, yet");
 	}
 
 	@Override
 	public void removeAt(int pX) {
 		this.mLock.lock();
 		try {
 			if(pX < 0 || pX >= this.mSongs.size()) {
 				throw new IllegalArgumentException("Song-Index not available");
 			}
 			this.mSongs.remove(pX);
 			if(pX <= this.mCurrent && this.mCurrent != 0) {
 				this.mCurrent--;
 			}
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public int getCurrent() {
 		this.mLock.lock();
 		try {
 			return this.mCurrent;
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public int size() {
 		this.mLock.lock();
 		try {
 			return this.mSongs.size();
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 }
