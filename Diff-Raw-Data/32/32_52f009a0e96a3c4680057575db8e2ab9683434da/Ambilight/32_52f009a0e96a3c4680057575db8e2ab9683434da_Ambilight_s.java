 package com.banasiak.ambilight;
 
 import gnu.io.CommPortIdentifier;
 import gnu.io.SerialPort;
 
 import java.awt.AWTException;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.awt.image.Raster;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Enumeration;
 
 import com.banasiak.ambilight.Config.ScreenRegion;
 
 public class Ambilight
 {
     private static final int SERIAL_PORT_TIMEOUT = 2000;
     private static final int SERIAL_PORT_DATA_RATE = 9600;
 
     private static SerialPort serialPort = null;
     private static BufferedReader input = null;
     private static OutputStream output = null;
 
     static Config config = new Config();
 
 
 
     /**
      * @param args
      */
     public static void main(String[] args)
     {
 
         if (!initializeSerialPort())
         {
             return;
         }
 
         try
         {
             final Robot robot = new Robot();
             final Dimension screenDimension = Toolkit.getDefaultToolkit()
                 .getScreenSize();
 
             final Rectangle regionLeftRectangle = createRectangle(
                 screenDimension, ScreenRegion.LEFT);
             final Rectangle regionRightRectangle = createRectangle(
                 screenDimension, ScreenRegion.RIGHT);
 
             final int sampleResolution = config.getSampleResolution();
 
             Color regionLeftColor;
             Color regionRightColor;
 
             while (true)
             {
                 regionLeftColor = sampleRectangle(robot, regionLeftRectangle,
                     sampleResolution);
                 regionRightColor = sampleRectangle(robot, regionRightRectangle,
                     sampleResolution);
 
                 System.out.println("Region: LEFT | Color: "
                     + regionLeftColor.toString());
                 System.out.println("Region: RIGHT | Color: "
                     + regionRightColor.toString());
                 System.out.println("");
 
                 // write marker for synchronization
                 output.write(0xff);
 
                 // RGB colors for left region
                 output.write(regionLeftColor.getRed());
                 output.write(regionLeftColor.getGreen());
                 output.write(regionLeftColor.getBlue());
 
                 // RGB colors for right region
                 output.write(regionRightColor.getRed());
                 output.write(regionRightColor.getGreen());
                 output.write(regionRightColor.getBlue());
 
                 Thread.sleep(10);
             }
 
         }
         catch (final AWTException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (final InterruptedException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (final IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
     }
 
     private static boolean initializeSerialPort()
     {
         final String serialPortName = config.getSerialPort();
         CommPortIdentifier portId = null;
         final Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier
             .getPortIdentifiers();
 
         while (portEnum.hasMoreElements())
         {
             final CommPortIdentifier currentPortId = portEnum.nextElement();
             if (currentPortId.getName().equals(serialPortName))
             {
                 portId = currentPortId;
                 break;
             }
         }
 
         if (portId == null)
         {
             System.out.println("Could not find COM port: " + serialPortName);
             return false;
         }
 
         try
         {
             serialPort = (SerialPort) portId.open("Ambilight",
                 SERIAL_PORT_TIMEOUT);
             serialPort.setSerialPortParams(SERIAL_PORT_DATA_RATE,
                 SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                 SerialPort.PARITY_NONE);
 
             output = serialPort.getOutputStream();
 
         }
         catch (final Exception e)
         {
             e.printStackTrace();
         }
 
         return true;
     }
 
     private static Rectangle createRectangle(Dimension screenDimension,
         ScreenRegion region)
     {
 
         final int width = (int) screenDimension.getWidth();
         final int height = (int) screenDimension.getHeight();
         final int halfWidth = width / 2;
         final int halfHeight = height / 2;
 
         Rectangle rectangle = null;
 
         switch (region)
         {
             case LEFT:
                 rectangle = new Rectangle(0, 0, halfWidth, height);
             break;
             case RIGHT:
                 rectangle = new Rectangle(halfWidth + 1, 0, halfWidth, height);
             break;
             case TOP:
                 rectangle = new Rectangle(0, 0, width, halfHeight);
             break;
             case BOTTOM:
                 rectangle = new Rectangle(0, halfHeight + 1, width, halfHeight);
             break;
 
         }
 
         return rectangle;
     }
 
     private static Color sampleRectangle(final Robot robot, Rectangle region,
         int sampleResolution)
     {
         int red = 0;
         int green = 0;
         int blue = 0;
 
         final BufferedImage grid = robot.createScreenCapture(region);
         final Raster raster = grid.getData();
         int rgbArray[] = null;
 
         for (int x = 0; x < raster.getWidth(); x = x + sampleResolution)
         {
             for (int y = 0; y < raster.getHeight(); y = y + sampleResolution)
             {
                 rgbArray = raster.getPixel(x, y, rgbArray);
                 red = red + rgbArray[0];
                 green = green + rgbArray[1];
                 blue = blue + rgbArray[2];
             }
         }
 
        // average color
         final int sampleArea = raster.getWidth() * raster.getHeight();
        final int resolutionArea = sampleArea / sampleResolution;
 
         red = red / resolutionArea;
         green = green / resolutionArea;
         blue = blue / resolutionArea;
 
         return new Color(red, green, blue);
     }
 
     public synchronized void closeSerialPort()
     {
         if (serialPort != null)
         {
             serialPort.removeEventListener();
             serialPort.close();
         }
     }
 
 }
