 package com.android.ddmlib;
 
 import java.net.InetSocketAddress;
 
 public class CalabashAdbHelper {
 
     public static RawImage getFrameBuffer(Device device) throws Exception {
        return AdbHelper.getFrameBuffer(new InetSocketAddress("127.0.0.1", 5037), device);
     }
 }
