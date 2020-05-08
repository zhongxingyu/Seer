 /*******************************************************************************
  * Copyright (c) 2010 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.bug.module.camera.pub;
 
 public interface ICamera2ModuleControl {
 	/**
 	 * @return test pattern setting or negative value if error
 	 */
 	public int getTestPattern();
 	/**
 	 * Set camera's test pattern:
 	 *
	 * @param testPattern 0=Disabled, 1=Walking 1's, 2=Solid White, 3=Grey Ramp, 4=Color Bars, 5=Black/White Bars, 6=PseudoRandom
 	 * @return 0 if successful, negative value otherwise.
 	 */
 	public int setTestPattern(int testPattern);
 
 	/**
 	 * 
 	 * @return color effects setting or negative value if error
 	 */
 	public int getColorEffects();
 	/**
 	 * Set camera's color effects
 	 * 
 	 * @param colorEffects 0=Disabled, 1=Black & White, 2=Sepia, 3=Negative, 4=Solarize
 	 * @return 0 if successful, negative value otherwise.
 	 */
 	public int setColorEffects(int colorEffects);
 	
 	/**
 	 * 
 	 * @return vertical flip setting or negative value if error
 	 */
 	public int getVerticalFlip();
 	/**
 	 * Set vertical flip.
 	 * 
 	 * @param verticalFlip 0=Disabled, 1=Flipped
 	 * @return 0 if successful, negative value otherwise.
 	 */
 	public int setVerticalFlip(int verticalFlip);
 
 	/**
 	 * 
 	 * @return horizontal mirror setting or negative value if error
 	 */
 	public int getHorizontalMirror();
 	/**
 	 * Set horizontal mirror.
 	 * 
 	 * @param horizontalMirror 0=Disabled, 1=Mirrored
 	 * @return 0 if successful, negative value otherwise.
 	 */
 	public int setHorizontalMirror(int horizontalMirror);
 	
 	/**
 	 * 
 	 * @return exposure level or negative value if error
 	 */
 	public int getExposureLevel();
 	/**
 	 * Set exposure level.
 	 * @param exposureLevel in the range 0-255 (default is 55)
 	 * @return
 	 */
 	public int setExposureLevel(int exposureLevel);
 }
