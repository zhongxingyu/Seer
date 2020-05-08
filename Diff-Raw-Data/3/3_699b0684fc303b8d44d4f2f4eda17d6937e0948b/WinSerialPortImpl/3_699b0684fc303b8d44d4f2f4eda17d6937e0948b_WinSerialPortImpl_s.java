 package waba.io.impl;
 import javax.comm.*;
 
 public class WinSerialPortImpl implements ISerialPort
 {
   javax.comm.SerialPort sPort;
   java.io.InputStream is;
   java.io.OutputStream os;
   boolean isOpen=false;
 
 	public WinSerialPortImpl(int number, int baudRate){
 		this(number, baudRate, 8, false, 1);
 	}
 	public WinSerialPortImpl(int number, int baudRate, int bits, boolean parity, int stopBits){
 		initPort(number,baudRate,8,false,1);
 	}
 	public void initPort(int number, int baudRate, int bits, boolean parity, int stopBits){
 	
 		String portName = null;
 		SerialPortDesc sPortDesc = SerialManager.getAssignedPort();
 		if(sPortDesc == null){
 		 	waba.io.impl.SerialManager.checkAvailableSerialPorts();
 			java.awt.Frame f = SerialManager.getMainFrame();
 			if(f != null){
 				SerialChoiceDialog dialog = new SerialChoiceDialog(f);
 				sPortDesc = SerialManager.getAssignedPort();
 			}
 		}
 		if(sPortDesc == null){
 			portName = "COM"+(number+1);
 		}else{
 			portName = sPortDesc.name;
 		}
 	    try
 	    {
 	      CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);
 	      sPort = (javax.comm.SerialPort)portId.open("Waba", 1000);
 	      sPort.setSerialPortParams(baudRate,bits,stopBits,(!parity)?0:1);
 	      is=sPort.getInputStream();
 	      os=sPort.getOutputStream();
 	      isOpen=true;
 	    }
 	    catch(Exception e)
 	    {
 	      System.out.println("Open Error: "+e);
 	    }
 	
 	}
 	public void clearBuffer(int i){}
 
   public boolean setFlowControl(boolean on)
   {
     return true;
   }
 
   public boolean setReadTimeout(int millis)
   {
     try
     {
 	sPort.enableReceiveTimeout(millis);
 	if(millis == 0){
		sPort.enableReceiveThreshold(0);
 	}
       return true;
     }
     catch(UnsupportedCommOperationException e)
     {
       return false;
     }
   }
 
   public boolean close()
   {
     if (!isOpen)
       return false;
     sPort.close();
     return true;
   }
 
   public boolean isOpen()
   {
     return isOpen;
   }
 
   public int readCheck()
   {
     return -1;
   }
 
   public int readBytes(byte buf[], int start, int count)
   {
     if (!isOpen)
       return -1;
     try
     {
       return is.read(buf,start,count);
     }
     catch(Exception e)
     {
       System.out.println("Read error! "+e);
       return -1;
     }
   }
 
   public int writeBytes(byte buf[], int start, int count)
   {
     if (!isOpen)
       return -1;
     try
     {
       os.write(buf,start,count);
       return count;
     }
     catch(Exception e)
     {
       System.out.println("Write error! "+e);
       return -1;
     }
   }
 }
