 package com.android.ddmlib;
 
 import java.net.InetSocketAddress;
 
 public class CalabashAdbHelper {
    public static final int ADB_PORT;

    private static final int DEFAULT_ADB_PORT = 5037;
    private static final String ENV_VAR_ADB_PORT = "ANDROID_ADB_PORT";

    static {
        int port = DEFAULT_ADB_PORT;
        String envPort = System.getenv(ENV_VAR_ADB_PORT);

        if (envPort != null && !"".equals(envPort)) {
            port = Integer.parseInt(envPort);
        }

        ADB_PORT = port;
    }
 
     public static RawImage getFrameBuffer(Device device) throws Exception {
        return AdbHelper.getFrameBuffer(new InetSocketAddress("127.0.0.1", ADB_PORT), device);
     }
 }
