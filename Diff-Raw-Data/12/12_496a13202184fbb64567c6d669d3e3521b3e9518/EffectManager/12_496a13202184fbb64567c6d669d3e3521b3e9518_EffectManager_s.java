/* $Id: EffectManager.java,v 1.2 2003/08/23 07:40:26 fredde Exp $
  * Copyright (C) 2003 Fredrik Ehnbom
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package org.gjt.fredde.silence.format.xm;
 
 class EffectManager {
 	public static final int VOL_SLIDE_UP = 1;
 	public static final int VOL_SLIDE_DOWN = 2;
 
 	Channel c			= null;
 
 	int	currentEffect		= -1;
 	int	currentEffectParam	= 0;
 
 	int	currentVolEffect	= -1;
 	int	currentVolEffectParam	= -1;
 
 	int	portaNote		= 0;
 	int	portaTarget		= 0;
 
 	public EffectManager(Channel c) {
 		this.c = c;
 	}
 
 	final void updateEffects() {
 		if (currentVolEffect != -1)
 			updateVolume();
 
 		if (currentEffect == -1)
 			return;
 
 		switch (currentEffect) {
 			case 0x00: // Arpeggio
 			{
 				int tmp = c.xm.tick % 3;
 				switch (tmp) {
 					case 0:
 						c.im.setNote(c.currentNote); break;
 					case 1:
 						c.im.setNote(c.currentNote + ((currentEffectParam >> 4)&0xf)); break;
 					case 2:
 						c.im.setNote(c.currentNote + ((currentEffectParam)&0xf)); break;
 				}
 			}
 				break;
 			case 0x01: // Porta up
 				c.im.porta -= currentEffectParam << 2;
 				c.im.setNote(c.currentNote);
 				break;
 			case 0x02: // Porta down
 				c.im.porta += currentEffectParam << 2;
 				c.im.setNote(c.currentNote);
 				break;
 
 			case 0x03: // Porta slide
 				if (c.im.porta < portaTarget) {
 					c.im.porta += currentEffectParam << 2;
 
 					if (c.im.porta >= portaTarget) {
 						currentEffect = -1;
 					}
 				} else {
 					c.im.porta -= currentEffectParam << 2;
 
 					if (c.im.porta <= portaTarget) {
 						currentEffect = -1;
 					}
 				}
 
 				if (currentEffect == -1) {
 					portaTarget = 0;
 					c.im.porta = 0;
 					c.currentNote = portaNote;
 				}
 				c.im.setNote(c.currentNote);
 				break;
 
 			case 0x09: // sample offset
 				currentEffect = -1;
 
 				if (currentEffectParam != 0) {
 					c.im.trigger();
 					c.im.setPosition(currentEffectParam << 8);
 				}
 				break;
 
 			case 0x0A: // Volume slide
 				c.im.currentVolume += (currentEffectParam & 0xF0) != 0 ?
 							 (currentEffectParam >> 4) & 0xF :
 							-(currentEffectParam & 0xF);
 
 				c.im.currentVolume = c.im.currentVolume < 0 ? 0 : c.im.currentVolume > 64 ? 64 : c.im.currentVolume;
 
 				if (c.im.currentVolume == 0 && (currentEffectParam & 0xF0) == 0) currentEffect = -1;
 				break;
 			case 0x0C: // set volume
 				c.im.currentVolume = currentEffectParam;
 				currentEffect = -1;
 
 				break;
 			case 0x0E: // extended MOD commands
 				int eff = (currentEffectParam >> 4) & 0xF;
 				if (eff == 0x0C) { // note cut
 					if ((currentEffectParam & 0xF) == 0) {
 						c.im.active = false;
 					} else {
 						currentEffectParam = (eff << 4) + (currentEffectParam & 0xF) - 1;
 					}
 				} else if (eff == 0x02) { // fine porta down
 					c.im.porta += (currentEffectParam & 0xF) << 2;
 					c.im.setNote(c.currentNote);
 					currentEffect = -1;
 				}
 				break;
 			case 0x0F:	// set tempo
 				if (currentEffectParam > 0x20) {
 					c.xm.defaultBpm = currentEffectParam;
 					c.xm.samplesPerTick = (5 * c.xm.deviceSampleRate) / (2 * c.xm.defaultBpm);
 				} else {
 					c.xm.defaultTempo = currentEffectParam;
 					c.xm.tick = 0;
 				}
 				currentEffect = -1;
 				break;
 			case 0x10: // set global volume (Gxx)
 				c.xm.globalVolume = currentEffectParam;
 				if (c.xm.globalVolume > 64) c.xm.globalVolume = 64;
 				currentEffect = -1;
 				break;
 			case 0x11: // global volume slide (Hxx)
 				c.xm.globalVolume += (currentEffectParam & 0xF0) != 0 ?
 						(currentEffectParam >> 4) & 0xF :
 						-(currentEffectParam & 0xF);
 				c.xm.globalVolume = c.xm.globalVolume > 64 ? 64 : c.xm.globalVolume < 0 ? 0 : c.xm.globalVolume;
 				break;
 
 			case 0x1B: // Multi retrig note (Rxx)
 				if (c.xm.tick == 0) return;
 				if (currentEffectParam == 0) {
 					currentEffect = -1;
 					return;
 				}
 				if (c.im.active && c.xm.tick % currentEffectParam == 0) {
 					c.im.trigger();
 				}
 				break;
 
 			default: // unknown effect
 				System.out.println("unknown effect: " + currentEffect);
 				currentEffect = -1;
 				break;
 		}
 	}
 
 	public int setEffect(int newEffect, int newEffectParam, int newNote) {
 		if (newEffectParam == -1) newEffectParam = 0;
 
 		if (newEffectParam == 0) {
 effectLoop:
 			switch (newEffect) {
 				case 0x00: // Arpeggio
 				case 0x08: // Set panning
 				case 0x09: // Sample offset
 				case 0x0B: // Position jump
 				case 0x0C: // Set volume
 				case 0x0D: // Pattern break
 					currentEffectParam = newEffectParam;
 					break;
 				case 0xE: // Extended
 					int eff = (currentEffectParam >> 4) & 0xF;
 					switch (eff) {
 						case 0x01: // Fine porta up
 						case 0x02: // Fine porta down
 						case 0x0A: // Fine volume slide up
 						case 0x0B: // Fine volume slide down
 							break effectLoop;
 						default:
 							break;
 					}
 				case 0xF: // Set tempo/BPM
 				case 0x10: // Set global volume (Gxx)
 				case 0x15: // Set envelope position (Lxx)
 				case 0x1D: // Tremor (Txx)
 					currentEffectParam = newEffectParam;
 					break;
 				default: break;
 			}
 		} else {
 			currentEffectParam = newEffectParam;
 		}
 
 		if (newEffect == 0x03) {
 			if (newNote != -1 && newNote != 97) {
 				portaNote = newNote;
 				portaTarget = c.im.getPeriod(portaNote) - c.im.getPeriod(c.currentNote);
 				if (c.im.release) {
 					c.im.setNote(c.currentNote);
 				}
				c.im.trigger();
 				newNote = -1;
 			}
 		}
 
 		currentEffect = newEffect;
 
 		return newNote;
 	}
 
 	public void updateVolume() {
 		switch (currentVolEffect) {
 			case VOL_SLIDE_DOWN: // Volume slide
 				c.im.currentVolume -= currentVolEffectParam;
 				if (c.im.currentVolume < 0) {
 					c.im.currentVolume = 0;
 					currentVolEffect = -1;
 				}
 
 				break;
 			case VOL_SLIDE_UP: // Volume slide
 				c.im.currentVolume += currentVolEffectParam;
 				if (c.im.currentVolume > 64) {
 					c.im.currentVolume = 64;
 					currentVolEffect = -1;
 				}
 
 				break;
 		}
 	}
 
 	public void setVolume(int newVolume) {
 		if (newVolume <= 0x50) { // volume
 			c.im.currentVolume = newVolume-10;
 		} else if (newVolume < 0x70) { // volume slide down
 			currentVolEffect = VOL_SLIDE_DOWN;
 			currentVolEffectParam = (newVolume - 0x60);
 		} else if (newVolume < 0x80) { // volume slide up
 			currentVolEffect = VOL_SLIDE_UP;
 			currentVolEffectParam = (newVolume - 0x70);
 		} else if (newVolume < 0x90) { // fine volume slide down
 			c.im.currentVolume -= (newVolume - 0x80);
 		} else if (newVolume < 0xa0) { // fine volume slide up
 			c.im.currentVolume += (newVolume - 0x90);
 //		} else if (newVolume < 0xb0) { // vibrato speed
 //		} else if (newVolume < 0xc0) { // vibrato
 //		} else if (newVolume < 0xd0) { // set panning
 //		} else if (newVolume < 0xe0) { // panning slide left
 //		} else if (newVolume < 0xf0) { // panning slide right
 //		} else if (newVolume >= 0xf0) { // Tone porta
 //		} else {
 //			System.out.println("unimplemented volume effect: " + newVolume);
 		}
 	}
 }
 
 /*
  * $Log: EffectManager.java,v $
  * Revision 1.2  2003/08/23 07:40:26  fredde
  * porta effects now working. 9xx implemented
  *
  * Revision 1.1  2003/08/22 12:35:22  fredde
  * moved effects from Channel to EffectManager
  *
  */
 
