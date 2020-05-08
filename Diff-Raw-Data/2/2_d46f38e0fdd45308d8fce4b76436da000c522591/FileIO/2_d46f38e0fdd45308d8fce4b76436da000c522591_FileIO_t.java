/*  Copyright (C) 2013  Nicholas Wright
 
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
 package com.github.dozedoff.commonj.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileChannel.MapMode;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class FileIO {
 	private static final Logger logger = LoggerFactory.getLogger(FileIO.class);
 
 	public static FileInputStream openAsFileInputStream(File file) {
 		FileInputStream fis = null;
 
 		try {
 			fis = new FileInputStream(file);
 		} catch (IOException e) {
 			logger.warn("Failed to open file {}, {}", file, e.getMessage());
 		}
 
 		return fis;
 	}
 
 	public static MappedByteBuffer openReadOnlyBuffer(File file) {
 		FileInputStream fis = openAsFileInputStream(file);
 		MappedByteBuffer buffer = null;
 
 		if (fis == null) {
 			return null;
 		}
 
 		FileChannel channel = fis.getChannel();
 
 		try {
 			buffer = channel.map(MapMode.READ_ONLY, 0, file.length());
 		} catch (IOException e) {
 			logger.warn("Failed to map {} to buffer, {}", file, e.getMessage());
 		}
 
 		return buffer;
 	}
 
 	public static void closeFileInputStream(FileInputStream stream) {
 		if (stream == null) {
 			return;
 		}
 
 		try {
 			stream.close();
 		} catch (IOException e) {
 			logger.warn("Failed to close FileInputStream, {}", e.getMessage());
 		}
 	}
 }
