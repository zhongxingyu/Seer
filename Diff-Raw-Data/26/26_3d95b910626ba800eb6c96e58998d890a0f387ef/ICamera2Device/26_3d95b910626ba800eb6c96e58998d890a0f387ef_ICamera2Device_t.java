 /*******************************************************************************
  * Copyright (c) 2008, 2009, 2010 Bug Labs, Inc.
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
 
 /**
  * An interface for a device that can return images.
  * 
  * @author dfindlay
  * 
  */
 public interface ICamera2Device {
 	/**
 	 * The default media node to specify when calling bug_camera_open().
 	 */
 	public final static String DEFAULT_MEDIA_NODE = "/dev/media0";
 	
	public boolean isCameraOpen();
	public boolean isCameraStarted();
 	
 	/**
 	 * Opens a connection with a camera module and configure it.
 	 * 
 	 * @param media_node
 	 * @param slot_num the slot number of the camera module to use, or -1 to use the default (if there's only 1)
 	 * @param full_height height in pixels to capture full images at
 	 * @param full_width width in pixels to capture full images at
 	 * @param preview_height height in pixels to capture preview images at
 	 * @param preview_width width in pixels to capture preview images at
 	 * @return 0 if open was successful
 	 */
 	public int cameraOpen(
 			final String media_node,
 			int slot_num,
 			int full_height,
 			int full_width,
 			int preview_height,
 			int preview_width);
 	
 	/**
 	 * Calls bug_camera_open with reasonable default values.
 	 * (DEFAULT_MEDIA_NODE, -1, 2048, 1536, 320, 240)
 	 * @return 0 if open was successful
 	 */
 	public int cameraOpenDefault();
 	
 	/**
	 * Closes the camera - allowing other processes to abug_camera_openccess it.
 	 * @return 0 if open was successful
 	 */
 	public int cameraClose();
 
 	/**
 	 *  Start acquiring frames.
 	 *  Must be called before the grab methods may be called. 
 	 *  @return 0 if start was successful
 	 */
 	public int cameraStart();
 	
 	/**
 	 * Stop acquiring frames.
 	 * @return 0 if stop was successful.
 	 */
 	public int cameraStop();
 
 	/**
 	 * Grabs a raw RGB-encoded preview image. 
 	 * bug_camera_open() and bug_camera_start() must already have been called.
 	 * @param pixelBuffer array of size preview width * preview height from
 	 *        the call to open(). You can create such a buffer with:
 	 *        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
 	 *        int [] buf = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
 	 * @return true if grab succeeded
 	 */
 	public boolean grabPreview(int [] pixelBuffer);
 	
 
 	/**
 	 * Grabs a JPEG-encoded full image. 
 	 * bug_camera_open() and bug_camera_start() must already have been called.
 	 * @return
 	 */
 	public byte[] grabFull();
 }
