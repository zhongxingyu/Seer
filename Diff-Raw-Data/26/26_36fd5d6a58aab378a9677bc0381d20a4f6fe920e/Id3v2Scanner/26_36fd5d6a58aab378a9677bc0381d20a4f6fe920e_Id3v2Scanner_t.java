 /**
  * This file is part of mp3d.
  * Copyright (C) 2009 Elliott Hughes.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.jessies.mp3d;
 
 import e.util.*;
 import java.io.*;
 import java.nio.charset.*;
 
 /**
  * Decodes an mp3 file's ID3v2 tag.
  * 
  * See: http://www.id3.org/Developer_Information
  */
 public class Id3v2Scanner {
     private static final Charset US_ASCII = Charset.forName("US-ASCII");
     
     private static final Charset[] ID3_STRING_ENCODINGS = {
         Charset.forName("ISO-8859-1"),
         Charset.forName("UTF-16"),
         Charset.forName("UTF-16BE"),
         Charset.forName("UTF-8")
     };
     
     private final File file;
     private InputStream in;
     private int bytesRead;
     
     public Id3v2Scanner(File file) {
         this.file = file;
     }
     
     public Mp3Info scanId3v2Tags() {
         try {
             in = new BufferedInputStream(new FileInputStream(file));
             
             if (readByte() != 'I' || readByte() != 'D' || readByte() != '3') {
                System.err.println(file + ": warning: doesn't start with ID3 tag");
                 return null;
             }
             
             final int majorVersion = readByte();
             final int revision = readByte();
             if (majorVersion < 2 || majorVersion > 4) {
                System.err.println(file + ": warning: has ID3v2." + majorVersion + "." + revision + " tags");
                 return null;
             }
             final boolean isNewFormat = (majorVersion == 3 || majorVersion == 4);
             
             final int flags = readByte();
             if (flags != 0) {
                System.err.println(file + ": warning: has non-zero flags " + Integer.toBinaryString(flags));
                 return null;
             }
             
             final int tagSize = readSynchSafeInteger(4);
             
            // FIXME: there might be an extended header to skip before we get to the frames (but not while we have the non-zero flags test above).
             
             String title = null;
             String artist = null;
             String album = null;
             String disc = null;
             String track = null;
             
             bytesRead = 0;
             while (bytesRead < tagSize) {
                 if (isNewFormat) {
                     // ID3v2.3 or ID3v2.4, which are the same for our purposes.
                     final String code = readAsciiString(4);
                     final int size = readSize(majorVersion);
                     if (size == 0) {
                         break;
                     }
                     // Skip flag bytes.
                    final int frameFlags = (readByte() << 8) | readByte();
                     if (code.equals("TALB")) {
                         album = readEncodedString(size);
                     } else if (code.equals("TIT2")) {
                         title = readEncodedString(size);
                     } else if (code.equals("TPE1")) {
                         artist = readEncodedString(size);
                     } else if (code.equals("TRCK")) {
                         track = readEncodedString(size);
                     } else if (code.equals("TPOS")) {
                         disc = readEncodedString(size);
                     } else {
                         skipBytes(size);
                     }
                 } else {
                     // ID3v2.2 (we already rejected anything older).
                     final String code = readAsciiString(3);
                     final int size = readSize(majorVersion);
                     if (size == 0) {
                         break;
                     }
                     if (code.equals("TAL")) {
                         album = readEncodedString(size);
                     } else if (code.equals("TT2")) {
                         title = readEncodedString(size);
                     } else if (code.equals("TP1")) {
                         artist = readEncodedString(size);
                     } else if (code.equals("TRK")) {
                         track = readEncodedString(size);
                     } else if (code.equals("TPA")) {
                         disc = readEncodedString(size);
                     } else {
                         skipBytes(size);
                     }
                 }
             }
             
             // We can guess a usable title...
             if (title == null) {
                 title = file.getName();
                System.err.println(file + ": warning: no title found in tags");
             }
             // ...but we're a bit hard-pressed to guess album and artist.
             if (album == null) {
                 album = "Unknown Album";
             }
             if (artist == null) {
                 artist = "Unknown Artist";
             }
             
             final Mp3Info result = new Mp3Info(title, artist, album, disc, track, file.toString());
             return result;
         } catch (Exception ex) {
             // FIXME: anything cleverer?
            System.err.print(file + ": warning: caught exception: ");
             ex.printStackTrace();
             return null;
         } finally {
             FileUtilities.close(in);
         }
     }
     
     // Every "major" version appears to have used a different encoding for the frame size!
     // Where "major" is the x in ID3v2.x!
     private int readSize(int majorVersion) throws IOException {
         if (majorVersion == 4) {
             return readSynchSafeInteger(4);
         } else if (majorVersion == 3) {
             return (readByte() << 24) | (readByte() << 16) | (readByte() << 8) | readByte();
         } else if (majorVersion == 2) {
             return (readByte() << 16) | (readByte() << 8) | readByte();
         }
         throw new RuntimeException("Don't know how to readSize for ID3v2." + majorVersion + " tags");
     }
     
     private void skipBytes(int byteCount) throws IOException {
        int stillToSkip = byteCount;
        while (stillToSkip > 0) {
            long skipped = in.skip(stillToSkip);
            stillToSkip -= skipped;
        }
         bytesRead += byteCount;
     }
     
     private int readByte() throws IOException {
         final int b = in.read();
         if (b == -1) {
             throw new EOFException();
         }
         ++bytesRead;
         return b;
     }
     
     private int readSynchSafeInteger(int byteCount) throws IOException {
         int result = 0;
         for (int i = 0; i < byteCount; ++i) {
             final int nextSevenBits = readByte();
             result = (result << 7) | nextSevenBits;
         }
         return result;
     }
     
     private String readAsciiString(int byteCount) throws IOException {
         final byte[] bytes = new byte[byteCount];
         for (int i = 0; i < byteCount; ++i) {
             bytes[i] = (byte) readByte();
         }
         return new String(bytes, US_ASCII);
     }
     
     private String readEncodedString(int byteCount) throws IOException {
         // The first byte is the encoding, and 'byteCount' includes that byte.
         final Charset charset = ID3_STRING_ENCODINGS[readByte()];
         final byte[] bytes = new byte[byteCount - 1];
         for (int i = 0; i < bytes.length; ++i) {
             bytes[i] = (byte) readByte();
         }
         final String value = new String(bytes, charset);
         // There should always be a trailing NUL.
         // That doesn't appear to be the case in reality, especially for empty strings.
         // I've also seen ID3 tags with the NUL at the beginning of the string.
         // So let's just strip them where we find them.
         return value.replace("\u0000", "");
     }
 }
