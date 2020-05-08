 /* Copyright (c) 2007, 2008 Bug Labs, Inc.
  * All rights reserved.
  *   
  * This program is free software; you can redistribute it and/or  
  * modify it under the terms of the GNU General Public License version  
  * 2 only, as published by the Free Software Foundation.   
  *   
  * This program is distributed in the hope that it will be useful, but  
  * WITHOUT ANY WARRANTY; without even the implied warranty of  
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU  
  * General Public License version 2 for more details (a copy is  
  * included at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html).   
  *   
  * You should have received a copy of the GNU General Public License  
  * version 2 along with this work; if not, write to the Free Software  
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  
  * 02110-1301 USA   
  *
  */
 package com.buglabs.bug.module.camera.pub;
 
import java.io.InputStream;
 import java.awt.Rectangle;
 
 /**
  * An interface for a device that can return images.
  * @author kgilmer
  *
  */
 public interface ICameraDevice {
 	public byte[] getImage();
 	public boolean initOverlay(Rectangle pbounds);
 	public boolean startOverlay();
 	public boolean stopOverlay();
 	public InputStream getImageInputStream();
 	public String getFormat();
 }
