 package org.jcoderz.mp3.intern.util;
 
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.ReadableByteChannel;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
 import org.jaudiotagger.audio.mp3.MPEGFrameHeader;
 import org.jcoderz.commons.util.HexUtil;
 import org.jcoderz.commons.util.IoUtil;
 
 
 public class Mp3Util
 {
     private static final int BUFFER_SIZE = 10 * 1024 * 1024;
     private static final int CRC_SIZE_IN_BYTES = 2; // 16bit checksum
 
     /**
      * Calculate a sha1 of the raw audio part of the file.
      * @param mp3 the file to scan
      * @return a sha1 of the raw audio part of the file.
      */
     public static String calcAudioFramesSha1 (ReadableByteChannel fc)
     {
         boolean done = false;
         MessageDigest md = null;
         FileInputStream fis = null;
         final ByteBuffer bb = ByteBuffer.allocateDirect(BUFFER_SIZE);
         
         try
         {
             fc.read(bb);
             bb.flip();
             if (!searchHeaderOffset(bb))
             {
                 throw new RuntimeException("Failed, no reliable mpeg header found.");
             }            
             md = MessageDigest.getInstance("SHA-1");
 end: 
             while (!done)
             {
                 if (bb.remaining() < (MPEGFrameHeader.HEADER_SIZE + CRC_SIZE_IN_BYTES))
                 {
                     bb.compact();
                     while (bb.position() < (MPEGFrameHeader.HEADER_SIZE + CRC_SIZE_IN_BYTES))
                     {
                         if (fc.read(bb) < 0)
                         {
                             done = true;
                             break end;
                         }
                     }
                     bb.flip();
                 }
                 
                 if (MPEGFrameHeader.isMPEGFrame(bb))
                 {
                     try 
                     {
                         MPEGFrameHeader header = MPEGFrameHeader.parseMPEGHeader(bb);
                         int rem = header.getFrameLength();; 
                         if (header.isProtected()) // CRC should not be part of the sha1
                         {
                             bb.position(bb.position() + MPEGFrameHeader.HEADER_SIZE + CRC_SIZE_IN_BYTES);
                             rem -= MPEGFrameHeader.HEADER_SIZE + CRC_SIZE_IN_BYTES;
                         }
                         else
                         {
                            bb.position(bb.position() + MPEGFrameHeader.HEADER_SIZE);
                            rem -= MPEGFrameHeader.HEADER_SIZE;
                         }
                         while (bb.remaining() <= rem)
                         {
                             bb.compact();
                             if (fc.read(bb) < 0)
                             {   // Last frame not fully read, ignored! 
                                 done = true;
                                 break end;
                             }
                             bb.flip();
                         }
                         md.update((ByteBuffer) bb.slice().limit(rem));
                         bb.position(bb.position() + rem);
                     }
                     catch (InvalidAudioFrameException ex)
                     {   // skip
                         bb.get(); 
                     }
                 }
                 else
                 {
                     bb.get(); // Skip non MPEG Data
                 }
             }
         }
         catch (IOException ex)
         {
             throw new RuntimeException("Failed " + ex, ex);
         }
         catch (NoSuchAlgorithmException ex)
         {
             throw new RuntimeException("Failed " + ex, ex);
         }
         finally
         {
             IoUtil.close(fis);
         }
         return HexUtil.bytesToHex(md.digest());
     }
 
     
     private static boolean searchHeaderOffset(ByteBuffer in)
     {
         boolean matched = false;
         
         while (!matched)
         {
             if (in.remaining() > MPEGFrameHeader.HEADER_SIZE && MPEGFrameHeader.isMPEGFrame(in))
             {
                 in.mark();
                 try
                 {
                     int match = 0;
                     MPEGFrameHeader header = MPEGFrameHeader.parseMPEGHeader(in);
                     while (in.remaining() > header.getFrameLength() && !matched)
                     {
                         in.position(in.position() + header.getFrameLength());
                         if (MPEGFrameHeader.isMPEGFrame(in))
                         {
                             match++;
                             header = MPEGFrameHeader.parseMPEGHeader(in);
                         }
                         else
                         {
                             match = 0;
                             break;
                         }
                         if (match > 2)
                         {
                             break;
                         }
                     }
                     if (match > 0)
                     {
                         matched = true;
                     }
                 }
                 catch (InvalidAudioFrameException e)
                 {
                     // next try...
                 }
                 in.reset();
             }
             
             if (!matched && in.hasRemaining())
             {
                 in.get();
             }
             else
             {
                 break;
             }
         }
         return matched;
     }
     
     
     /**
      * Calculate a sha1 of the raw audio part of the file.
      * @param mp3 the file to scan
      * @return a sha1 of the raw audio part of the file.
      */
     public static String calcAudioFramesSha1 (File mp3)
     {
         final String result;
         FileInputStream in = null;
         try
         {
             in = new FileInputStream(mp3);
             result = calcAudioFramesSha1(in.getChannel());
         }
         catch (FileNotFoundException e)
         {
             throw new RuntimeException("Failed " + e, e);
         }
         finally
         {
             IoUtil.close(in);
         }
         return result;
     }
     
 }
