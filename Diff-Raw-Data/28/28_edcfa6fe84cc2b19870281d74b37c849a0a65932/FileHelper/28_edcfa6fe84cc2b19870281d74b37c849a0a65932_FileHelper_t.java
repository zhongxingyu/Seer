/*
* Copyright (C) 2011 <galaran.h@gmail.com>
*
* This file is part of the Bukkit plugin McTranslit.
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston,
* MA 02111-1307, USA.
*/

 package me.galaran.mctranslit;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 public class FileHelper {
 
     public static void extractFileFromJarRoot(String fileName, File targetDir) throws IOException {
 
         InputStream is = FileHelper.class.getResourceAsStream('/' + fileName);
         if (!targetDir.exists())
             targetDir.mkdir();
         OutputStream os = new FileOutputStream(targetDir + File.separator + fileName);
         byte[] buffer = new byte[512];
         int length;
         while ((length = is.read(buffer)) > 0) {
             os.write(buffer, 0, length);
         }
         os.close();
         is.close();
     }
 }
