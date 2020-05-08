package im.akash.arduino;
 
 import gnu.io.CommPortIdentifier;
 import gnu.io.SerialPort;
 import java.io.InputStream;
 import java.io.OutputStream;
 import processing.app.Preferences;
 //import java.lang.reflect.*;
 
 public class Arduino
 {
 
     static InputStream input;
     static OutputStream output;
     SerialPort port;
     CommPortIdentifier portId;
     
     public Arduino() throws Exception
     {
     
         Preferences.init();
 		//System.out.println("Using port: " + Preferences.get("serial.port"));
 
 		portId = CommPortIdentifier.getPortIdentifier(Preferences.get("serial.port"));
 
 		port = (SerialPort)portId.open("ArduinoSerialComm", 4000);
         input = port.getInputStream();
         output = port.getOutputStream();
         port.setSerialPortParams(115200,
                                     SerialPort.DATABITS_8,
                                     SerialPort.STOPBITS_1,
                                     SerialPort.PARITY_NONE
                                 );
    
     }
 
     public void getsay() throws Exception
     {
         while(true){
 			while (input.available()>0)
 				System.out.print((char)(input.read()));
         }
     }
     
     public boolean output(int[] pinArray)
     {
         sendPin(pinArray.length);
         for(int i=0; i<pinArray.length; i++)
         {
             sendPin(pinArray[i]);
         }
         return true;
     }
     
     public boolean output(int onePin)
     {
         //output pin setting
         sendPin(1);
         sendPin(onePin);
         return true;
     }
     
     public boolean setHigh(int pin)
     {
         //digital high
         sendData('1');
         sendPin(pin);
         return true;
     }
     
     public boolean setLow(int pin)
     {
         //digital low
         sendData('0');
         sendPin(pin);
         return true;
     }
     
     public boolean getState(int pin)
     {
         //get digital state
         byte io_buffer[] = new byte[10];
         String tempStr;
         
         sendData('2');
         sendPin(pin);
 
         try{
             input.read(io_buffer);
             tempStr = new String(io_buffer);
             if(tempStr.indexOf("1")==0)
             {
                 return true;
             }
             
             if(tempStr.indexOf("0")==0)
             {
                 return false;
             }
         }
         catch(Exception e){}
         return false;
     }
     
     public boolean analogWrite(int pin, int value)
     {
         //analog write
         return true;
     }
     
     public int analogRead(int pin)
     {
         
         byte io_buffer[] = new byte[4];
         String tempStr;
         try{
             input.read(io_buffer);
             tempStr = new String(io_buffer);
             return(Integer.parseInt(tempStr));
         }
         catch(Exception e){
             return(-1);
         }
     }
     
     public boolean turnOff()
     {
         //turn
         return true;
     }
     
     public boolean close()
     {
         //close serial connection   
         return true;
     }
     
     private int getData()
     {
         byte io_buffer[] = new byte[10];
         String tempStr;
         try{
             input.read(io_buffer);
             tempStr = new String(io_buffer);
             if(tempStr.indexOf("1")==0)
             {
                 return(-1);
             }
             
             if(tempStr.indexOf("0")==0)
             {
                 return(-1);
             }
         }
         catch(Exception e){
             
         }
         return(-1);
     }
     
     private boolean sendPin(int pin)
     {
         char pin_in_char = (char) (pin+48);
         sendData(pin_in_char);
         return true;
     }
     
     private static void sendData(char commData){
         byte io_buffer[] = new byte[10];
         String tempStr;
         try{
             while(true)
             {
                 if(input.available()>0)
                 {
                     input.read(io_buffer);
                     tempStr = new String(io_buffer);
                     if(tempStr.indexOf("what")!=-1)
                         break;
                 }    
             }
             output.write(commData);
         }
         catch(Exception e){
             System.out.println("serial write problem");
         }
     }
     
     public static int byteArrayToInt(byte[] b, int offset)
     {
         int value = 0;
         for (int i = 0; i < 4; i++) {
             int shift = (4 - 1 - i) * 8;
             value += (b[i + offset] & 0x000000FF) << shift;
         }
         return value;
     }
 }
