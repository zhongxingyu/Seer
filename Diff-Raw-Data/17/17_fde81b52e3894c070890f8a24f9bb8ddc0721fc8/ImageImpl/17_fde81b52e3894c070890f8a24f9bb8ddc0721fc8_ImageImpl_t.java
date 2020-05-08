 /*
  * This is a utility project for wide range of applications
  *
  * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  10-1  USA
  */
 package com.smartitengineering.util.opensearch.impl;
 
 import com.smartitengineering.util.opensearch.api.Image;
 import java.net.URI;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * An implementation of {@link Image}
  * @author imyousuf
  */
 class ImageImpl implements Image {
 
   private int height = -1, width = -1;
   private String mimeType = null;
   private URI imageUri;
 
   public ImageImpl(URI imageUri) {
     setImageUri(imageUri);
   }
 
   public void setHeight(int height) {
     if(height <= 0) {
       return;
     }
     this.height = height;
   }
 
  public final void setImageUri(URI imageUri) {
     if(imageUri == null) {
       throw new IllegalArgumentException("Image URI can not be set to Null!");
     }
     this.imageUri = imageUri;
   }
 
   public void setMimeType(String mimeType) {
     if(StringUtils.isBlank(mimeType)) {
       return;
     }
     this.mimeType = mimeType;
   }
 
   public void setWidth(int width) {
     if(width <= 0) {
       return;
     }
     this.width = width;
   }
 
  @Override
   public int getHeight() {
     return height;
   }
 
  @Override
   public int getWidth() {
     return width;
   }
 
  @Override
   public String getMimeType() {
     return mimeType;
   }
 
  @Override
   public URI getImageUri() {
     return imageUri;
   }
 
 }
