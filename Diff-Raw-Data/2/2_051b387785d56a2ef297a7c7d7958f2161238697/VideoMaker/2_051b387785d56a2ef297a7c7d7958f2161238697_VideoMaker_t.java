 package com.example;
 
 import com.xuggle.mediatool.*;
 import java.util.*;
 import java.io.*;
 import java.awt.image.*;
 import java.util.concurrent.*;
 import static java.util.concurrent.TimeUnit.SECONDS;
 import static java.util.concurrent.TimeUnit.MILLISECONDS;
 import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
 public class VideoMaker
 {
 
     final int videoStreamIndex = 0;
     final int videoStreamId = 0;
     final int width = 800;
     final int height = 600;
     final long frameRate = 66;
     private int nextFrame = 0;
     private final IMediaWriter writer;
     public VideoMaker(String fileName)
     {
        writer = ToolFactory.makeWriter(fileName);
         //add a video stream
         writer.addVideoStream(videoStreamIndex, videoStreamId, width, height);
     }
     public void addFrame(BufferedImage frame)
     {
 
         writer.encodeVideo(videoStreamIndex, frame, nextFrame, MILLISECONDS);
         //increment the next frame
         nextFrame += frameRate;
 
     }
 
     public void finish()
     {
         //close the writer
         writer.close();
     }
 
 }
