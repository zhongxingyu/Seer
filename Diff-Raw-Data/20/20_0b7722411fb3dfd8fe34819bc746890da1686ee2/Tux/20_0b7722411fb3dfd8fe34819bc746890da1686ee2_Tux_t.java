 package net.addictivesoftware.rpitux;
 
 import com.pi4j.io.gpio.*;
 import com.pi4j.io.i2c.I2CBus;
 import com.pi4j.io.i2c.I2CDevice;
 import com.pi4j.io.i2c.I2CFactory;
 import com.sun.servicetag.SystemEnvironment;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 public class Tux {
     public static boolean isArm = SystemEnvironment.getSystemEnvironment().getOsArchitecture().equals("arm");
 
    private int i2cBus = 1;
    private byte i2cAddress = 0x41;
 
     private static Object pinRed = null;
     private static Object pinGreen = null;
 
     static {
         if (isArm) {
             GpioController gpio = GpioFactory.getInstance();
             pinRed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "Red LED", PinState.LOW);
             pinGreen = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "Green LED", PinState.LOW);
         }
     }
 
     public static void main(String[] args) {
 
         try {
             Tux tux = new Tux();
 
             tux.setStatus("SUCCESS", "Very looooooooooooong text", "Raspberry Pi");
             Thread.sleep(2000);
             tux.setStatus("UNSTABLE", "Bla die bla", "Rocks");
             Thread.sleep(2000);
             tux.setStatus("FAILURE", "Thats too bad", "RPi forever and ever");
             Thread.sleep(2000);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
     }
 
    public Tux() {}
 
    public Tux(int bus, byte address) {
        this.i2cBus = bus;
        this.i2cAddress = address;
     }
 
     public void setStatus(String status, String build, String culprits) {
 
         if (isArm) {
             try {
                 setLcd(limitText(build), limitText(culprits));
                 setEyes(status);
             } catch (Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace(System.out);
             }
         } else {
             System.out.println(status);
            System.out.println("--------------");
             System.out.println(limitText(build));
             System.out.println(limitText(culprits));
            System.out.println("--------------");
         }
 
     }
 
     private void setEyes(String result) {
         if (null == result) {return;}
         if (result.equals("SUCCESS")) {
             ((GpioPinDigitalOutput)pinRed).low();
             ((GpioPinDigitalOutput)pinGreen).high();
         } else if (result.equals("FAILURE")) {
             ((GpioPinDigitalOutput)pinRed).high();
             ((GpioPinDigitalOutput)pinGreen).low();
         } else if (result.equals("UNSTABLE")){
             ((GpioPinDigitalOutput)pinRed).high();
             ((GpioPinDigitalOutput)pinGreen).high();
         } else if (result.equals("OFF")) {
             ((GpioPinDigitalOutput)pinRed).low();
             ((GpioPinDigitalOutput)pinGreen).low();
         }
     }
 
     private void setLcd(String build, String culprits) throws IOException {
         I2CBus i2CBus = I2CFactory.getInstance(i2cBus);
         I2CDevice device = i2CBus.getDevice(i2cAddress);
 
         clearScreen(device);
         writeText(device, build);
         setPosition(device, 0,1);
         writeText(device, culprits);
     }
 
     private synchronized static void clearScreen(I2CDevice device) throws IOException {
         byte[] bytes = {0x10, 0x00};
         device.write(bytes, 0, bytes.length);
     }
 
     private void setPosition(I2CDevice device, int x, int y) throws IOException {
         byte position = (byte)(y*32+x);
         byte[] bytes = {0x11, position};
         device.write(bytes, 0, bytes.length);
     }
 
     private void writeText(I2CDevice device, String text) throws IOException {
         byte[] bytes = ByteBuffer.allocate(text.length() + 1).array();
         bytes[0] = 0x00;
         System.arraycopy(text.getBytes(), 0, bytes, 1, text.length());
         device.write(bytes,0, bytes.length);
     }
 
     private String limitText(String text) {
         if (text.length() <=16) return text;
         return text.substring(0,7) + ".." + text.substring(text.length()-7);
     }
 
 
 }
