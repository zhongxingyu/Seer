 /*
  * Copyright (C) 2011 Nicolas Simonds
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package net.submedia.android.uqmwallpaper;
 
 import java.io.*;
 import java.util.*;
 import android.os.SystemClock;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.content.Context;
 import android.content.res.*;
 import android.util.Log;
 
 //------------------------------------------------------------------------
 // Animation - a port of the C structs used in the UQM code for
 // this stuff
 class Animation {
 
 	public static final String TAG = "UQMWallpaper.Animation";
 
 	// comm frame rate according to UQM sources
 	public static final int FRAME_RATE = (1000 / 40);
 	public int next_frame_delay;
 
 	public final byte RANDOM_ANIM = (1 << 0);
 	// The next index is randomly chosen.
 	public final byte CIRCULAR_ANIM = (1 << 1);
 	// After the last index has been reached, the animation starts over.
 	public final byte YOYO_ANIM = (1 << 2);
 	// After the last index has been reached, the order that the
 	// animation frames are used is reversed.
 	public final byte ANIM_MASK = (RANDOM_ANIM | CIRCULAR_ANIM | YOYO_ANIM);
 	// Mask of all animation types.
 
 	public final byte WAIT_TALKING = (1 << 3);
 	// This is set in AlienTalkDesc when the ambient animations should
 	// stop at the end of the current animation cycle.
 	// In AlienAmbientArray, this is set for those ambient animations
 	// which can not be active while the talking animation is active.
 	public final byte PAUSE_TALKING = (1 << 4);
 	public final byte TALK_INTRO = (1 << 5);
 	public final byte TALK_DONE = (1 << 6);
 
 	// Silly Java, this won't lose precision!  Stop complaining...
 	public final byte ANIM_DISABLED = (byte) (1 << 7);
 	public final byte COLORXFORM_ANIM = PAUSE_TALKING;
 
 	public enum Direction {
 		UP_DIR,		// Animation indices are increasing
 		DOWN_DIR,	// Animation indices are decreasing
 		NO_DIR
 	};
 
 	private List<Frame> frame;
 	private Content content;
 	private Canvas canvas;
 	private Bitmap result;
 	private Random rand = new Random();
 	private int ActiveMask = 0;
 
 	// TFB_Random() equivalent: rand.nextInt(0xFF), rand.nextInt(0xFFFF,), etc.
 	// GetTimeCounter() equivalent: SystemClock.uptimeMillis()
 	private long LastTime = SystemClock.uptimeMillis();
 
 	Animation(String content_pack, String alien_race, Context c)
 	throws Exception, IOException {
 
 		final String PACKAGE_NAME = c.getPackageName();
 		final Resources r = c.getResources();
 		final int resid = r.getIdentifier(alien_race, "array", PACKAGE_NAME);
 
 		if (resid == 0)
 			throw new Exception("Could not find resource id for " + alien_race);
 
 		this.frame = new ArrayList<Frame>();
 
 		boolean first = true;
 		for (String res : r.getStringArray(resid)) {
 			if (first) {
 				this.content = new Content(content_pack, r.getStringArray(r.getIdentifier(res, "array", PACKAGE_NAME)));
 				first = false;
 			}
 			else
 				this.frame.add(new Frame(r.getIntArray(r.getIdentifier(res, "array", PACKAGE_NAME))));
 		}
 		final Bitmap bg = this.content.frame.get(0).content;
 		this.result = bg.copy(bg.getConfig(), true);
 		this.canvas = new Canvas(this.result);
 
 		return;
 	}
 
 	private void DrawStamp(Content.Frame f) {
 		this.canvas.drawBitmap(f.content, f.hotspot.x, f.hotspot.y, null);
 	}
 
 	// a simplified implementation of ambient_anim_task from the UQM sources
 	//
 	public Bitmap getFrame() {
 		long CurTime = SystemClock.uptimeMillis();
 		long ElapsedTicks = CurTime - this.LastTime;
 		int i = 0;
 
 		this.next_frame_delay = 0x7FFFFFFF;
 		this.LastTime = CurTime;
 
 		// scribble all the updates onto the canvas
 		for (Frame f : this.frame) {
 			int ActiveBit = 1 << i++;
 
 			// but not yet...
 			if (f.Alarm > ElapsedTicks) {
 				f.Alarm -= ElapsedTicks;
 				if (f.Alarm < this.next_frame_delay)
 					this.next_frame_delay = f.Alarm;
 				continue;
 			}
 			if ((ActiveMask & f.BlockMask) != 0) {
 				f.Alarm = f.randomRestartRate();
 				continue;
 			} else {
 				ActiveMask |= ActiveBit;
 			}
 
			// Log.d(TAG, String.format("ActiveBit: %#x ActiveMask: %#x StartIndex: %d NumFrames: %d CurIndex: %d", ActiveBit, ActiveMask, f.StartIndex, f.NumFrames, f.CurIndex));
 
 			DrawStamp(this.content.frame.get(f.CurIndex));
 
 			// setup next iteration
 			f.Alarm = f.randomFrameRate();
 			if (f.Alarm < this.next_frame_delay)
 				this.next_frame_delay = f.Alarm;
 
 			final int num_frames = f.NumFrames - 1;
 
			if (COLORXFORM_ANIM == (f.AnimFlags & COLORXFORM_ANIM)) {
				ActiveMask &= ~ActiveBit;
				f.Alarm = 0;
			}
			else if (YOYO_ANIM == (f.AnimFlags & YOYO_ANIM)) {
 				if (f.Direction == Direction.UP_DIR) {
 					f.CurIndex++;
 					if (f.CurIndex > (f.StartIndex + num_frames)) {
 						f.Direction = Direction.DOWN_DIR;
 						f.CurIndex = (short) (f.StartIndex + num_frames);
 					}
 				}
 				else if (f.Direction == Direction.DOWN_DIR) {
 					f.CurIndex--;
 					if (f.CurIndex < f.StartIndex) {
 						f.Direction = Direction.UP_DIR;
 						f.CurIndex = f.StartIndex;
 						f.Alarm = f.randomRestartRate();
 						ActiveMask &= ~ActiveBit;
 					}
 				}
 			}
 			else if (CIRCULAR_ANIM == (f.AnimFlags & CIRCULAR_ANIM)) {
 				f.CurIndex++;
 				if (f.CurIndex > (f.StartIndex + num_frames)) {
 					f.CurIndex = f.StartIndex;
 					f.Alarm = f.randomRestartRate();
 					ActiveMask &= ~ActiveBit;
 				}
 			}
 			else if (RANDOM_ANIM == (f.AnimFlags & RANDOM_ANIM)) {
				f.CurIndex = (short) (f.StartIndex + rand.nextInt(f.NumFrames));
 				ActiveMask &= ~ActiveBit;
 			}
 
 			// Log.d(TAG, f.toString());
 		}
 
 		if (this.next_frame_delay < FRAME_RATE || this.next_frame_delay == 0x7FFFFFFF)
 			this.next_frame_delay = FRAME_RATE;
 
 		// return the resulting bitmap
 		return this.result;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder result = new StringBuilder();
 		String NEW_LINE = System.getProperty("line.separator");
 
 		for (Frame f : this.frame) {
 			result.append(f.toString()).append(NEW_LINE);
 		}
 		result.append(this.content.toString());
 
 		return result.toString();
 	}
 
 	//------------------------------------------------------------------------
 	// Animation.Frame - The animation data with associated content
 	class Frame {
 
 		public short StartIndex;
 		// Index of the first image
 		public byte NumFrames;
 		// Number of frames in the animation.
 		public byte AnimFlags;
 		// One of RANDOM_ANIM, CIRCULAR_ANIM, or YOYO_ANIM
 		public short BaseFrameRate;
 		public short RandomFrameRate;
 		public short BaseRestartRate;
 		public short RandomRestartRate;
 		public int BlockMask;
 		// Bit mask of the indices of all animations that can not
 		// be active at the same time as this animation.
 
 		public Direction Direction;
 		public byte CurFrame;
 		public short CurIndex;
 		public int Alarm;
 
 		Frame(int[] i) {
 			this.StartIndex        = (short) i[0];
 			this.NumFrames         =  (byte) i[1];
 			this.AnimFlags         =  (byte) i[2];
 			this.BaseFrameRate     = (short) i[3];
 			this.RandomFrameRate   = (short) i[4];
 			this.BaseRestartRate   = (short) i[5];
 			this.RandomRestartRate = (short) i[6];
 			this.BlockMask         = i[7];
 
 			this.Direction = Direction.UP_DIR;
 			this.CurFrame = 0;
 			this.CurIndex = this.StartIndex;
 			this.Alarm = this.randomRestartRate();
 		}
 
 		// stupid sign bits...
 		public int randomFrameRate () {
 			return 1 + this.BaseFrameRate +	rand.nextInt(0x7FFFFFFF) % (this.RandomFrameRate + 1);
 		}
 
 		public int randomRestartRate () {
 			return 1 + this.BaseRestartRate + rand.nextInt(0x7FFFFFFF) % (this.RandomRestartRate + 1);
 		}
 
 		@Override
 		public String toString() {
 			return String.format("Start[%05d] Frames[%02d] Flags[%02d] FrameRate[%05d] FrameRate2[%05d] Restart[%05d] Restart2[%05d] Block[%010d]",
 								 this.StartIndex,
 								 this.NumFrames,
 								 this.AnimFlags,
 								 this.BaseFrameRate,
 								 this.RandomFrameRate,
 								 this.BaseRestartRate,
 								 this.RandomRestartRate,
 								 this.BlockMask);
 		}
 	}
 }
 // END Animation
 //------------------------------------------------------------------------
