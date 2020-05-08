 package com.mofirouz.prank;
 
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
 import com.google.common.io.Files;
 
 import java.awt.*;
 import java.io.File;
 import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
 import java.util.Properties;
 
 import static java.nio.charset.Charset.defaultCharset;
 
 public class Main {
     public static String QUIT = "QUIT";
     public static String PAUSE = "pause";
     public static String SPEED = "speed";
     public static String DISTANCE = "distance";
     public static String JIGGLE = "jiggle";
 
     public static void main(String[] args) throws AWTException, InterruptedException, IOException {
        new Main();
    }

    private Main() throws AWTException, InterruptedException, IOException {
        InputStream configFile = getClass().getClassLoader().getResourceAsStream("prank.config");
        String[] configs = CharStreams.toString(new InputStreamReader(configFile, Charsets.UTF_8)).split(System.getProperty("line.separator"));
         SambaFileMonitor sambaFileMonitor = new SambaFileMonitor(configs[0], configs[1]);
         Properties prop = new Properties();
         MouseMover mouseMover = new MouseMover();
 
         while (true) {
             if (sambaFileMonitor.exists()) {
                 prop.clear();
                 prop.load(sambaFileMonitor.getFile().getInputStream());
 
                 if (prop.get(QUIT) != null)
                     break;
 
                 int distance = (prop.get(DISTANCE)) != null ? Integer.parseInt(prop.get(DISTANCE).toString()) : 100;
                 int speed = (prop.get(SPEED)) != null ? Integer.parseInt(prop.get(SPEED).toString()) : 10;
                 boolean jiggle = (prop.get(JIGGLE)) != null ? Boolean.parseBoolean(prop.get(JIGGLE).toString()) : false;
 
                 boolean paused = (prop.get(PAUSE)) != null ? Boolean.parseBoolean(prop.get(PAUSE).toString()) : false;
                 if (!paused)
                     mouseMover.move(distance, speed, jiggle);
             }
             Thread.sleep(5000);
         }
     }
 }
