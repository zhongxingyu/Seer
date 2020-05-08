 /**
 Copyright (C) 2012  Delcyon, Inc.
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.delcyon.capo.datastream.stream_attribute_filter;
 
 import java.io.FilterInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 
 /**
  * @author jeremiah
  *
  */
 public abstract class AbstractFilterInputStream extends FilterInputStream implements StreamAttributeFilter
 {
 
 	private final byte[] smallBuffer = new byte[1]; 
 	@Override
 	public String getName()
 	{
 		return getClass().getAnnotation(InputStreamAttributeFilterProvider.class).name();
 	}
 	
 	protected AbstractFilterInputStream(InputStream in)
 	{
 		super(in);		
 	}
 	
 	@Override
 	public int read(byte[] b) throws IOException
 	{
 		return read(b,0,b.length);
 	}
 	
 	@Override
 	public int read() throws IOException
 	{
 		 int bytesRead = read(smallBuffer, 0, 1);
 		 if (bytesRead > 0)  //because we are always passing a buffer of length 1, this should never be zero
 		 {
 		     //the bytes come in as signed, so we need to mask them to unsigned so that code using the int read() method always gets a postive result as expected
 			 return (int)(smallBuffer[0] & 0xff);
 		 }
 		 else //if we're closed, then return -1 to indicate as much
 		 {
 			 return -1;
 		 }
 	}
 }
