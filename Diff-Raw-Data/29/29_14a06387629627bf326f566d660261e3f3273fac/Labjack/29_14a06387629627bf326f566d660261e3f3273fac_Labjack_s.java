 /**
  * Rig Client Commons Ethernet Labjack Interface
  * 
  * Software to allow native interfacing to Ethernet-based Labjack hardware.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author David Knight (davknigh)
  * @date 5th July 2010
  *
  * Changelog:
  * - 05/07/2010 - davknigh - Initial file creation.
  * - 12/07/2010 - davknigh - Added USB functionality using javax.usb package. 
  */
 
 package au.edu.labshare.rigclient.internal.labjack;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.usb.UsbClaimException;
 import javax.usb.UsbConfiguration;
 import javax.usb.UsbDevice;
 import javax.usb.UsbDisconnectedException;
 import javax.usb.UsbEndpoint;
 import javax.usb.UsbException;
 import javax.usb.UsbHostManager;
 import javax.usb.UsbHub;
 import javax.usb.UsbInterface;
 import javax.usb.UsbNotActiveException;
 import javax.usb.UsbPipe;
 import javax.usb.UsbServices;
 
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * A Labjack utility for use with Rigs that have Ethernet Labjack.
  * 
  * This class holds the state of a connection as well as setting
  * and reading the value of ports.
  */
 public class Labjack
 {
     /** Polarity and Gain settings */
     public static final int UNIPOLAR_x1 = 0;
     public static final int UNIPOLAR_x2 = 1;
     public static final int UNIPOLAR_x4 = 2;
     public static final int UNIPOLAR_x8 = 3;
     public static final int BIPOLAR_x1 = 8;
     
     /** For Ethernet Connections */
         /** IP address. */
         private String ip;
         
         /** Socket Timeout */
         private int timeout;
         
         /** Socket. */
         private Socket socket;    
     
         /** Output data stream. */
         private OutputStream output;
         
         /** Input data stream. */
         private InputStream input;
         
     /** For USB Connections */
         /** Serial Number of device. */
         private int serialNumber;
         
         /** USB Interface */
         private UsbInterface interf;
         
         /** Pipes attached to the endpoints */
         private UsbPipe inputPipe;
         private UsbPipe outputPipe;
 
         /** USB Vendor Id */
         private static final short USB_VENDOR_ID = 0x0CD5;
         
         /** USB Product Id */
         private static final short USB_PRODUCT_ID = 0x0009;
         
         /** USB Interface Number to use. */
         private static final int USB_INTERFACE_NUMBER = 0;
         
         /** USB endpoint addresses. */
         private static final byte USB_ENDPOINT_IN_NUMBER = (byte) 0x81;
         private static final byte USB_ENDPOINT_OUT_NUMBER = (byte) 0x01;
         
         
         
         
     /** Connection type */
     private int connType;
     
     /** Logger. */
     private final ILogger logger;
 
     /** Calibration information. */
     private GeneralCalibrationInfo calInfo;
     
     /** LJTDAC calibration information. */
     private HashMap<Integer, LJTDACCalibrationInfo> lJTDACCalInfos;
 
     /** Bipolar/Unipolar and gain setting. */
     private int bipgain = Labjack.UNIPOLAR_x1;
     
     
     
     
     /** Default Port. */
     private static final int PORT = 52360;
     
     /** Ethernet Connection. */
     private static final short ETHERNET = 1;
     
     /** USB connection. */
     private static final short USB = 2;
     
     /** Power Level. High power level - low may not be supported by hardware */
     private static final int POWERLEVEL = 0;
     
     /** Resolution setting. Set to 14-bit resolution (medium) */
     private static final int RESOLUTION = 14;
     
     /** I2C address for LjTick DAC EEPROM */
     private static final int LJTDAC_EEPROM_ADDRESS = 0xA0;
     
     /** I2C address for LjTick DAC DAC */
     private static final int LJTDAC_DAC_ADDRESS = 0x24;
 
 
     /**
      * Constructor
      * 
      * @param _ip Ethernet IP address
      */
     public Labjack(String _ip)
     {
         this.logger = LoggerFactory.getLoggerInstance();
         this.ip = _ip;
         this.socket = null;
         
         this.logger.info("Labjack connection type is Ethernet with the IP address: " + this.ip + '.');
         this.connType = ETHERNET;
         
         this.calInfo = null;
     }
 
     /**
      * Constructor
      * 
      * @param _serialNo Serial number of device to connect to. 
      */
     public Labjack(int _serialNo) 
     {
         this.logger = LoggerFactory.getLoggerInstance();
         this.ip = null;
         this.serialNumber = _serialNo;
         this.socket = null;
         
         this.logger.info("Labjack connection type is USB with the Serial Number: " + this.serialNumber + '.');
         this.connType = USB;
         
         this.calInfo = null;
     }
     
     
     /**
      * Constructor
      * 
      * @param _serialNo Serial Number of device to connect to.
      * @param _bipgain Polarity and Gain setting for the device. Pass one of
      * Labjack.UNIPOLAR_x1 (0-5v) [default], Labjack.UNIPOLAR_x2 (0-2.5v), 
      * Labjack.UNIPOLAR_x4, (0-1.25v), Labjack.UNIPOLAR_x8 (0-0.625v)
      * or Labjack.BIPOLAR_x1 (-5-5v)
      */
     public Labjack(String _ip, int _bipgain)
     {
         this(_ip);
        
        if (_bipgain == Labjack.UNIPOLAR_x1 || _bipgain == Labjack.UNIPOLAR_x2 || _bipgain == Labjack.UNIPOLAR_x4 
                || _bipgain == Labjack.UNIPOLAR_x8 || _bipgain == Labjack.BIPOLAR_x1)
        {
            this.bipgain = _bipgain;
        }
     }
     
     
     /**
      * Constructor
      * 
      * @param _serialNo Serial Number of device to connect to.
      * @param _bipgain Polarity and Gain setting for the analogue read functions
      * of the device. Pass one of
      * Labjack.UNIPOLAR_x1 (0-5v) [default], Labjack.UNIPOLAR_x2 (0-2.5v), 
      * Labjack.UNIPOLAR_x4, (0-1.25v), Labjack.UNIPOLAR_x8 (0-0.625v)
      * or Labjack.BIPOLAR_x1 (-5-5v)
      */
     public Labjack(int _serialNo, int _bipgain)
     {
         this(_serialNo);
        
        if (_bipgain == Labjack.UNIPOLAR_x1 || _bipgain == Labjack.UNIPOLAR_x2 || _bipgain == Labjack.UNIPOLAR_x4 
                || _bipgain == Labjack.UNIPOLAR_x8 || _bipgain == Labjack.BIPOLAR_x1)
        {
            this.bipgain = _bipgain;
        }
     }
     
     
     /**
      * Setter for Polarity and Gain setting for the device.
      * 
      * @param _bipgain Polarity and Gain setting for the analogue read functions
      * of the device. Pass one of
      * Labjack.UNIPOLAR_x1 (0-5v) [default], Labjack.UNIPOLAR_x2 (0-2.5v), 
      * Labjack.UNIPOLAR_x4, (0-1.25v), Labjack.UNIPOLAR_x8 (0-0.625v)
      * or Labjack.BIPOLAR_x1 (-5-5v)
      */
     public void setBipgain (int _bipgain)
     {
         if (_bipgain == Labjack.UNIPOLAR_x1 || _bipgain == Labjack.UNIPOLAR_x2 || _bipgain == Labjack.UNIPOLAR_x4 
                 || _bipgain == Labjack.UNIPOLAR_x8 || _bipgain == Labjack.BIPOLAR_x1)
         {
             this.bipgain = _bipgain;
         }
     }
     
     
     
     /**
      * Connect to the Labjack
      * 
      * @throws Exception if error occurs
      */
     public void connect() throws Exception
     { 
         this.logger.info("Connecting to the Labjack.");
         if (this.connType == ETHERNET)
         {
             this.socket = new Socket(this.ip, PORT);
             
             if (timeout > 0)
             {
                 this.socket.setSoTimeout(timeout);
             }
             
             this.output = socket.getOutputStream();
             this.input = socket.getInputStream();  
         }
         else
         {
             // Access the system USB services, and access to the root 
             // hub. Then traverse through the root hub.
             boolean matchFound = false;
             List<UsbDevice> connectedLabjacks = new ArrayList<UsbDevice>();
             UsbServices services;
             services = UsbHostManager.getUsbServices();
             UsbHub rootHub = services.getRootUsbHub();
             traverse(rootHub, connectedLabjacks); 
             
             UsbDevice device = null;
                   
             if (connectedLabjacks.size() != 0)
             {
                 CommConfigInfo com = new CommConfigInfo();
                 
                 /** loops through discovered labjacks, looking for one which
                  * matches the provided serial number.
                  */
                 for (UsbDevice lj : connectedLabjacks)
                 {
                     device = lj;
                     
                     UsbConfiguration config = device.getActiveUsbConfiguration();
                     List usbInterfaces = config.getUsbInterfaces();
 
                     this.interf = (UsbInterface) usbInterfaces.get(USB_INTERFACE_NUMBER);
                     this.interf.claim();
                     
                     UsbEndpoint usbEndpointIn = this.interf.getUsbEndpoint(USB_ENDPOINT_IN_NUMBER);
                     UsbEndpoint usbEndpointOut = this.interf.getUsbEndpoint(USB_ENDPOINT_OUT_NUMBER);
 
                     this.inputPipe = usbEndpointIn.getUsbPipe();
                     this.inputPipe.open();
                     
                     this.outputPipe = usbEndpointOut.getUsbPipe();
                     this.outputPipe.open();
                     
                     com.getCommConfig();
                     
                     if (com.calculatedSerialNumber == this.serialNumber)
                     {
                         matchFound = true;
                         break;
                     }
                     else
                     {
                         disconnect();       // close pipes, release claim on interface
                     }
                 }
                 
                 if (!matchFound)
                 {
                     throw new Exception("No Labjacks matching that serial number found.");
                 }
             }
             else
             {
                 throw new Exception("No Labjacks found.");
             }
         }
         
         this.calInfo = new GeneralCalibrationInfo();
         try
         {
             this.calInfo.getCalibration();
         }
         catch (IOException ioex)
         {
             this.logger.warn("getCalibration method failed with message: " + ioex.getMessage() + " Reverting to " +
                     "uncalibrated conversions.");
             this.calInfo = null;
         }
     }
 
     
     /**
      * Recursively traverses the USB bus, adding any labjacks to a list.
      * @param device the device to begin traversing through.
      * @param list the list of UsbDevices to add matching devices to.
      */
     private void traverse(UsbDevice device, List<UsbDevice> list)
     {
         if (device.isUsbHub())
         {
             // This is a USB Hub, traverse through the hub.
             List attachedDevices = ((UsbHub) device).getAttachedUsbDevices();
             for (int i=0; i<attachedDevices.size(); i++)
             {
                 traverse((UsbDevice) attachedDevices.get(i), list);
             }
         }
         else
         {
             /** If the device is a Labjack, add it to the list */
             if (device.getUsbDeviceDescriptor().idVendor() == USB_VENDOR_ID 
                     && device.getUsbDeviceDescriptor().idProduct() == USB_PRODUCT_ID)
             {
                 list.add(device);
             }
         }
     }
 
 
     /**
      * Resets the LabJack.  This method sleeps for 20 seconds to
      * allow the LabJack to settle, so opening a connection
      * shouldn't fail because the LabJack isn't ready.
      * 
      * DODGY 20 seconds is an arbitrary sleep length that _seems_
      * to work.  The <em>LabJack UE9 Users Guide</em> isn't so
      * specific but claims a few seconds should do it.
      * 
      * @throws LabJackException if error occurs
      */
     public void reset() throws Exception
     {
         this.logger.info("Resetting LabJack. ");
 
         ResetPacket packet = new ResetPacket();
         
         packet.sendPacket(1);    //Hard Reset labjack
         
         try
         {
             Thread.sleep(20000);
         }
         catch (InterruptedException e)
         {
             this.logger.warn("Interrupted during LabJack reset sleep. Returning before the LabJack may be ready.");
             return;
         }
     }
 
     /**
      * Disconnect from the LabJack.
      */
     public void disconnect()
     {
         if (connType == ETHERNET)
         {
             try
             {
                 this.socket.close();
             }
             catch (IOException e)
             {
                 
             }
         }
         else
         {
             this.inputPipe.abortAllSubmissions();
             this.outputPipe.abortAllSubmissions();
             
             try
             {
                 this.inputPipe.close();
             }
             catch (UsbException e)
             {
                 /* Only catch USBException because all subclasses thrown
                  * by close() indicate the same thing - that the USB connection
                  * is already closed.
                  */
                 this.logger.warn("Error caught closing usb input pipe: " + e.getMessage() + " (Labjack->disconnect)");
             }
             
             try
             {
                 this.outputPipe.close();
             }
             catch (UsbException e)
             {
                 /* Only catch USBException because all subclasses thrown
                  * by close() indicate the same thing - that the USB connection
                  * is already closed.
                  */
                 this.logger.warn("Error caught closing usb output pipe: " + e.getMessage() + " (Labjack->disconnect)");
             }
             
             try
             {
                 this.interf.release();
             }
             catch (UsbClaimException e)
             {
                 this.logger.warn("USB Interface was not claimed: " + e.getMessage() + 
                         " (Labjack->disconnect)");
             }
             catch (UsbNotActiveException e)
             {
                 this.logger.warn("USB Interface released was not active: " + e.getMessage() + 
                         " (Labjack->disconnect)");
             }
             catch (UsbDisconnectedException e)
             {
                 this.logger.warn("USB Interface already disconnected: " + e.getMessage() + 
                 " (Labjack->disconnect)");
             }
             catch (UsbException e)
             {
                 this.logger.error("USB Interface could not be released: " + e.getMessage() + 
                 " (Labjack->disconnect)");
             }
         }
     }
 
     
     
     /**
      * Requests and stores the calibration information for an attached LJ Tick 
      * DAC module.
      * @param SCLPin the pin used for the SCL function. It is assumed that pin
      * SCLPin + 1 is used for SDA.
      * @throws Exception on error
      */
     public void getCalibrationLJTDAC(int SCLPin) throws Exception
     {
         LJTDACCalibrationInfo cal = new LJTDACCalibrationInfo();
         cal.getCalibration(SCLPin + 1, SCLPin);
         
         lJTDACCalInfos.put(SCLPin, cal);
     }
     
     
     
     /**
      * Writes an analogue value to a analogue (DAC) port. 
      * Valid value range is between 0 and 5.
      * 
      * @param port port number to write to (i.e. DAC\<n\>)
      * @param value value to write
      * @throws Exception if error occurs
      */
     public void writeAnalogue(int port, double value) throws Exception
     {
         int byteVoltage = convertAnToBinVoltage(this.calInfo, value, port);
         short[] returnData = new short[5];
         
         SingleIOPacket packet = new SingleIOPacket();
         
         this.logger.info("Writing voltage " + value + " to DAC" + port + ". (LabJack->writeAnalogue)");
         
         int ret = packet.sendPacket((short) 5, port, (short)(byteVoltage & 0x00FF), (short)((byteVoltage / 256) + 192),
                 (short) 0, returnData);
         if (ret != 0)
         {
             this.logger.warn("Failed writing value to DAC" + port + " with LJ_ERROR " + ret + ". (LabJack->writeAnalogue)");
             throw new Exception("Failed writing value to DAC" + port + " with LJ_ERROR " + ret);
         }
     }
     
     /**
      * Writes an analogue value to the specified channel of a LjTick-DAC. 
      * Valid values are ~-10v to ~+10v. Values outside of this range will
      * result in the maximum or minimum voltage being output. 
      * @param SCLPin the labjack pin used for SCL. It is assumed that the next
      * pin will be used for SDA
      * @param channel the LjTick-DAC channel to set. 0: DACA, otherwise DACB
      * @param value the analogue value to set
      * @throws Exception if error occurs
      */
     public void writeAnalogueLJTDAC(int SCLPin, int channel, double value) throws Exception
     {
         byte bytesSend[] = new byte[3];
         
         bytesSend[0] = (byte)(channel==0 ? 0x30 : 0x31);  //LJTDAC command byte : 0x30 (DACA) or 0x31 (DACB)
         
         int binaryVoltage = convertAntoBinVoltageLJTDAC(lJTDACCalInfos.isEmpty() ? null : lJTDACCalInfos.get(SCLPin), 
                 value, channel);
         
         bytesSend[1] = (byte)(binaryVoltage / 256);          //value (high)
         bytesSend[2] = (byte)(binaryVoltage & (0x00FF));   //value (low)
         
         I2cPacket packet = new I2cPacket();
         int ret = packet.sendPacket(0, 0, SCLPin + 1, SCLPin, LJTDAC_DAC_ADDRESS, bytesSend.length, 0, bytesSend, null);
         
         if (ret != 0)
         {
             this.logger.warn("Failed writing value to LJTDAC on pins " + SCLPin + " and " + SCLPin + 1 + 
                     " with LJ_ERROR " + ret + ". (LabJack->writeAnalogueLjTick)");
             throw new Exception("Failed writing value to LJTDAC on pins " + SCLPin + " and " + SCLPin + 1 + 
                     " with LJ_ERROR " + ret);
         }
     }
     
     
     /**
      * Writes a value to a digital port (FIO).  This function 
      * will make an the port an output, if it is not already 
      * an output.
      * 
      * @param port port to write to
      * @param state true for on, false off
      * @throws Exception
      */
     public void writeDigital(int port, boolean state) throws Exception
     {
         this.logger.info("Setting FIO" + port + " to " + (state ? "on" : " off") + ". (LabJack->writeDigital)");
         int st[] = {state ? 1 : 0};
         FeedbackPacket packet = new FeedbackPacket();
 
         int ret = packet.sendPacket(port, 1, st);
         if (ret != 0)
         {
             this.logger.warn("Failed writing value to FIO" + port + " with LJ_ERROR " + ret + ". (LabJack->writeDigital)");
             throw new Exception("Failed writing value to FIO" + port + " with LJ_ERROR " + ret);
         }
     }
     
     /**
      * Reads a value from the specified analogue (AIN) port.
      * 
      * @param port port number to read from
      * @return read in value
      * @throws Exception error reading value
      */
     public double readAnalogue(int port) throws Exception
     {
         this.logger.info("Reading analogue value from AIN " + port + ". (LabJack->readAnalogue)");
         short returnData[] = {0, 0, 0, 0, 0};
         double voltage;
         int rawVoltage;
         SingleIOPacket packet = new SingleIOPacket();
             
         int ret = packet.sendPacket(4, port, this.bipgain, RESOLUTION, 0, returnData);
         if (ret != 0)
         {
             this.logger.warn("Failed reading AIN port " + port + " with return value " + ret + ". (LabJack->readAnalogue)");
             throw new Exception("Failed reading AIN port " + port + " with return value " + ret);
         }
         
         rawVoltage = returnData[3] + returnData[4] * 256;
         
         if (port == 133 || port == 141)
         {
             voltage = convertBinToAnTemperature(this.calInfo, POWERLEVEL, rawVoltage);
         }
         else
         {
             voltage = convertBinToAnVoltage(this.calInfo, this.bipgain, RESOLUTION, rawVoltage);
         }
         
         this.logger.info("Value read from AIN" + port + " is " + voltage + ". (LabJack->readAnalogue)");
         return voltage;
     }
     
     /**
      * Reads a value from the specified digital (FIO) port.
      * This function will make the port an input if it is not
      * already an input.
      * 
      * @param port port to read from
      * @return true if on, false otherwise
      * @throws Exception error reading value
      */
     public boolean readDigital(int port) throws Exception
     {
         int state[] = {0};
         
         FeedbackPacket packet = new FeedbackPacket();
 
         int ret = packet.sendPacket(port, 0, state);
         if (ret != 0)
         {
             this.logger.warn("Failed reading FIO port " + port + " with return value " + ret + ". " +
                     "(LabJack->readDigital)");
             throw new Exception("Failed reading FIO port " + port + " with return value " + ret);
             
         }
 
         if (state[0] == 0)
         {
             return false;
         }
         else
         {
             return true;
         }
     }
     
     /**
      * Helper function to convert a voltage between 0 and 5 to a value between
      * 0 and 4095. If getCalibration has been called and valid data was returned,
      * this data will be used for the conversion. Otherwise, nominal calibration
      * factors will be used.
      * @param cal the calibration information returned by getCalibration()
      * @param voltage the input voltage, between 0 and 5
      * @param DACNumber the DAC channel to use calibration information from.
      * The default is 0, and this parameter is ignored for uncalibrated 
      * conversions
      * @return the value between 0 and 4095 corresponding to the input voltage
      */
     private int convertAnToBinVoltage(GeneralCalibrationInfo cal, double voltage, int DACNumber)
     {
         int tempBytesVoltage;
         double slope;
         double offset;
     
         if (cal == null || cal.checkCalibrationInfo() == false)
         {
             /* Use uncalibrated conversion factors */
             slope = 842.59;
             offset = 0;
         }
         else
         {
             /* Use calibrated conversion factors */
             switch(DACNumber) 
             {
                 case 1:
                     slope = cal.DACSlope[1];
                     offset = cal.DACOffset[1];
                     break;
                 default:
                     slope = cal.DACSlope[0];
                     offset = cal.DACOffset[0];
                     break;
             }
         }
         
         tempBytesVoltage = (int)((slope * voltage) + offset);
 
        /* Check to make sure the output will be a value between 0 and 4095,
         * or that a int overflow does not occur.
         */
         if (tempBytesVoltage < 0)
         {
             tempBytesVoltage = 0;
         }
 
        /* Restrict to values less than 4095, and remove any sign bits */
         return 0xFFF & tempBytesVoltage;
     }
     
     /**
      * Helper function to convert a voltage between ~-10v and ~10v to a value 
      * between 0 and 65535. If getCalibration has been called and valid data was
      * returned, this data will be used for the conversion. Otherwise, nominal 
      * calibration factors will be used.
      * @param cal the LJTDAC calibration info returned by getCalibration()
      * @param voltage the voltage to convert
      * @param DACNumber the DAC channel to use calibration information from.
      * The default is 0, and this parameter is ignored for uncalibrated 
      * conversions
      * @return a value between 0 and 65535 representing the analogue voltage
      */
     private int convertAntoBinVoltageLJTDAC(LJTDACCalibrationInfo cal, double voltage, int DACNumber)
     {
         int tempBytesVoltage;
         double slope;
         double offset;
     
         if (cal == null || cal.checkCalibrationInfo() == false)
         {
             /* Use uncalibrated conversion factors */
             slope = 3158.6;
             offset = 32624;
         }
         else
         {
             /* Use calibrated conversion factors */
             switch(DACNumber) 
             {
                 case 1:
                     slope = cal.DACSlopeB;
                     offset = cal.DACOffsetB;
                     break;
                 default:
                     slope = cal.DACSlopeA;
                     offset = cal.DACOffsetA;
                     break;
             }
         }
         
         tempBytesVoltage = (int)((slope * voltage) + offset);
 
         /* Check to make sure the output will be a value between 0 and 65535. */
         if (tempBytesVoltage < 0)
         {
             tempBytesVoltage = 0;
         }
         else if (tempBytesVoltage > 65535)
         {
             tempBytesVoltage = 65535;
         }
 
         return tempBytesVoltage;
     }
     
     /**
      * Helper function to convert a value between 0 and 4095 to the 
      * corresponding voltage (0-5v). If getCalibration has been called and 
      * valid data was returned, this data will be used for the conversion. 
      * Otherwise, nominal calibration factors will be used.
      * @param cal the calibration information returned by getCalibration()
      * @param gainBip the gain and polarity configuration for the value. 
      * The options are 0: 0-5v unipolar (default), 1: 0-2.5v unipolar,
      * 2: 0-1.25v unipolar, 3: 0-0.625v unipolar and 8: -5-5v bipolar.
      * @param resolution the number of bits of resolution for the conversion.
      * Valid values are 12-16, 17 (16 bits, low noise mode) or 18 (high-res
      * reading only available on UE9-Pro)
      * @param voltage the 12-bit value to convert to a voltage. 
      * @return the voltage corresponding to the voltage parameter.
      */
     private double convertBinToAnVoltage(GeneralCalibrationInfo cal, int gainBip, int resolution, int voltage)
     {
         double slope;
         double offset;
         
         if (cal == null || cal.checkCalibrationInfo() == false)
         {
             /* Use uncalibrated conversion factors */
             if (resolution < 18)
             {
                 switch (0xFF & gainBip)
                 {
                     case 1:
                         slope = 0.000038736;
                         offset = -0.012;
                         break;
                         
                     case 2:
                         slope = 0.000019353;
                         offset = -0.012;
                         break;
                         
                     case 3:
                         slope = 0.0000096764;
                         offset = -0.012;
                         break;
                         
                     case 8:
                         slope = 0.00015629;
                         offset = -5.1760;
                         break;
                     
                     default:
                         slope = 0.000077503;
                         offset = -0.012;
                         break; 
                 }
             }
             else  //UE9 Pro high res
             {
                 switch (0xFF & gainBip)
                 {
                     case 8:
                         slope = 0.00015629;
                         offset = -5.176;
                         break;
                         
                     default:
                         slope = 0.000077503;
                         offset = 0.012;
                         break;
                 }
             }
         }
         else
         {
             /* Use calibrated conversion factors */
             if (resolution < 18)
             {
                 switch (0xFF & gainBip)
                 {
                     case 1:
                         slope = cal.unipolarSlope[1];
                         offset = cal.unipolarOffset[1];
                         break;
                     
                     case 2:
                         slope = cal.unipolarSlope[2];
                         offset = cal.unipolarOffset[2];
                         break;
                     
                     case 3:
                         slope = cal.unipolarSlope[3];
                         offset = cal.unipolarOffset[3];
                         break;
                     
                     case 8:
                         slope = cal.bipolarSlope;
                         offset = cal.bipolarOffset;
                         break;
                     
                     default:
                         slope = cal.unipolarSlope[0];
                         offset = cal.unipolarOffset[0];
                         break;
                       
                 }
             }
             else  //UE9 Pro high res
             {
                 switch (0xFF & gainBip)
                 {
                       case 8:
                           slope = cal.hiResBipolarSlope;
                           offset = cal.hiResBipolarOffset;
                           break;
                           
                       default:
                         slope = cal.hiResUnipolarSlope;
                           offset = cal.hiResUnipolarOffset;
                           break;
                 }
             }    
         }
         
         return ((slope * voltage) + offset);
     }
     
     /**
      * Helper function to calculate the temperature corresponding to the value
      * provided by the LabJack. If getCalibration has been called and 
      * valid data was returned, this data will be used for the conversion. 
      * Otherwise, nominal calibration factors will be used.
      * @param cal the calibration information returned by getCalibration()
      * @param powerLevel the power level the Labjack is running at. A value of
      * 0 should be passed - low power mode (powerLevel = 1) not supported by 
      * hardware.
      * @param temperature the value to be converted to a temperature
      * @return the temperature corresponding to the temperature parameter, in 
      * degrees Kelvin.
      */
     private double convertBinToAnTemperature (GeneralCalibrationInfo cal, int powerLevel, int temperature)
     {
         double slope = 0;
        
         if (cal == null || cal.checkCalibrationInfo() == false)
         {
             /* Use uncalibrated conversion factors */
             slope = 0.012968;
         }
         else
         {
             /* Use calibrated conversion factors */
             switch (0xFF & powerLevel)
             {
                 case 1:
                     slope = cal.tempSlopeLow;
                     break;
                     
                 default:
                     slope = cal.tempSlope;
                     break;
             }
         }
         
         return temperature * slope;
     }
     
     /**
      * Converts byte array containing a signed 32.32, little endian, 2’s 
      * complement fixed point number to a double.
      * @param packet the byte array
      * @param startIndex the location of the first byte in the array
      * @return
      */
     private double convertByteArrayToDouble(byte[] packet, int startIndex) 
     { 
       long resultDec = 0;
       long resultWh = 0;
 
       for (int i = 0; i < 4; i++)
       {
         resultDec += (long)((0xFF & packet[startIndex + i]) * Math.pow(2, (i*8)));
         resultWh += (long)((0xFF & packet[startIndex + i + 4]) * Math.pow(2, (i*8)));
       }
 
       return ((double)((int)resultWh) + (double)(resultDec)/4294967296.0);
     }
 
     private void send(byte[] outPacket) throws IOException, UsbException
     {
         if (connType == ETHERNET)
         {
             output.write(outPacket);
         }
         else
         {
             this.outputPipe.syncSubmit(outPacket);
         }
     }
     
     private int receive(byte[] inPacket, int offset, int length) throws IOException, UsbException
     {
         if (connType == ETHERNET)
         {
             return (input.read(inPacket, offset, length));
         }
         else
         {
             return this.inputPipe.syncSubmit(inPacket);
         }
     }
     
     /**
      * Superclass of all packet classes
      */
     public abstract class Packet
     {
         /** Array for storing outgoing packet */
         protected byte[] outPacket;
         
         /** Array for storing incoming packet */
         protected byte[] inPacket;
         
         
         /**
          * Adds checksum to packet in normal command format
          * @param packet packet to modify
          * @param length number of bytes in packet
          */
         protected void calculateNormalChecksum(byte[] packet, int length)
         {
             packet[0] = (byte)calculateNormalChecksum8(packet, length);
         }
         
         /**
          * Adds checksum to packet in extended command format
          * @param packet packet to modify
          * @param length number of bytes in packet
          */
         protected void calculateExtendedChecksum(byte[] packet, int length)
         {
             int a;
     
             a = calculateExtendedChecksum16(packet, length);
             packet[4] = (byte)(a & 0xff);
             packet[5] = (byte)((a / 256) & 0xff);
             packet[0] = (byte)calculateExtendedChecksum8(packet);
         }
         
         /**
          * Returns checksum8 for normal command
          * @param packet packet to modify
          * @param length number of bytes in packet
          * @return 1-byte unsigned value in a short
          */
         protected short calculateNormalChecksum8(byte[] packet, int length)
         {
             int a = 0, bb;
             
             for (int i = 1; i < length; i++)
             {
                 /* Cast byte to int (automatically), remove sign bits, add to a */
                 a += (0xFF & packet[i]);
             }
     
             bb = a / 256;
             a = (a - 256 * bb) + bb;
             bb = a / 256;
     
             return (short)(0xFF & ((a - 256 * bb) + bb));
         }
         
         /**
          * Returns checksum8 for extended command
          * @param packet packet to modify
          * @return 1-byte unsigned value in a short
          */
         protected short calculateExtendedChecksum8(byte[] packet)
         {
             int a = 0, bb;
     
             /* Sums bytes 1 to 5.
              * Sums quotient and remainder of 256 division. 
              * Again, sums quotient and remainder of 256 division.
              */
             for (int i = 1; i < 6; i++)
             {
                 /* Cast byte to int (automatically), remove sign bits, add to a */
                 a += (0xFF & packet[i]);
             }
             
             bb = a / 256;
             a = (a - (256 * bb)) + bb;
             bb = a / 256;
             
             return (short)(0xFF & ((a - 256 * bb) + bb));
         }
         
         /**
          * Returns checksum16 for extended command
          * @param packet packet to modify
          * @param length number of bytes in packet 
          * @return 2-byte unsigned value in an int
          */
         protected int calculateExtendedChecksum16(byte[] packet, int length)
         {
             int a = 0;
     
             /* Sums bytes 6 to n-1 to a unsigned 2 byte value. */
             for(int i = 6; i < length; i++)
             {
                 /* Cast byte to int (automatically), remove sign bits, add to a */
                 a += (0xFF & packet[i]);
             }
             
             return (0xFFFF & a);
         }
     }
     
     /**
      * Class representing the SingleIO transaction packet.
      */
     public final class SingleIOPacket extends Packet
     {
         /**
          * Constructor.
          */
         public SingleIOPacket()
         {
             outPacket = new byte[8];
             inPacket = new byte[8];
         }
         
         /**
          * Sends 'Single IO' packet and makes response available.
          * @param inIOType the IO operation to perform. Valid values are 
          * 0: Digital Bit Read, 1: Digital Bit Write, 2: Digital Port Read, 
          * 3: Digital Port Write, 4: Analogue Read, 5: Analogue Write.
          * @param inChannel channel to read or write. For digital port writes,
          * use 0: FIO, 1: EIO, 2: CIO, 3:MIO. 
          * @param inDirBipGainDACL for digital bit write, the direction of the
          * bit. Use 0: input, 1: output. For digital port writes, each bit 
          * corresponds to a pin in the port. For analogue reads, this is the 
          * BipGain setting - see http://labjack.com/support/ue9/users-guide/5.3.3
          * For analogue writes, this is the low byte of the output value
          * @param inStateResDACH for digital bit write, the output state. Use 
          * 0: low, 1: high. For digital port writes, each bit corresponds to 
          * the state of a pin in the port. For analogue reads, this is the 
          * number of bits of resolution (12 - 16, or 17 (16 bits, low noise)).
          * For analogue writes, this is the high byte of the output value
          * @param inSettlingTime amount of additional settling time for analogue
          * reads. Adds approximately 5*inSettlingTime ms to the transaction.
          * @param returnData pass an array of 5 shorts. It will be filled with
          * IOType, Channel, Direction/AINL (For a digital bit read, this reads 
          * 0 for input, and 1 for output. For a digital port read, this is 
          * multiple bits returning input or output for each line. For digital 
          * writes this is just an echo. For an analogue input this is the lowest 
          * 8 bits of the 24-bit conversion value (generally ignored on the UE9)),
          * State/AINM (For a digital bit read, this is a read of the input 
          * state. For a digital port read, this is multiple bits returning a 
          * read of the input state for each line. For digital writes this is 
          * just an echo. For an analogue input this is the middle 8 bits of 
          * the 24-bit conversion value, or more typically considered the 
          * lowest 8 bits of the 16-bit conversion value) and AINH (For an 
          * analogue input this is the high 8 bits of the 24-bit conversion 
          * value, or more typically considered the high 8 bits of the 16-bit 
          * conversion value)
          * @return 0 on completion, -1 on error.
          * @throws IOException on IO error
          */
         private int sendPacket(int inIOType, int inChannel, int inDirBipGainDACL, int inStateResDACH, int inSettlingTime, short[] returnData) throws Exception
         {
             outPacket[1] = (byte)(0xA3);                //command byte
             outPacket[2] = (byte)inIOType;                 //IOType
             outPacket[3] = (byte)inChannel;                //Channel
             outPacket[4] = (byte)inDirBipGainDACL;         //Dir/BipGain/DACL
             outPacket[5] = (byte)inStateResDACH;           //State/Resolution/DACH
             outPacket[6] = (byte)inSettlingTime;          //Settling time
             outPacket[7] = (byte)0;                        //Reserved
             
             calculateNormalChecksum(outPacket, this.outPacket.length);
             
             send(outPacket);
             
             if (receive(inPacket, 0, inPacket.length) < inPacket.length)
             {
                 /* Error: read failed, or did not read all of the buffer. */
                 throw new Exception("Read failed, or did not read all of the buffer");
             }
             
             if (calculateNormalChecksum8(inPacket, 8) != (0xFF & inPacket[0]))
             {
                 /* Error: read buffer has bad checksum. */
                 throw new Exception("Read buffer has bad checksum8: " + inPacket[0] + " vs " + calculateNormalChecksum8(inPacket, 8));
             }
             
             if ((0xFF &inPacket[1]) != 0x00A3)
             {
                 /* Error: read buffer has wrong command byte. */
                 throw new Exception("Read buffer has wrong command byte: " + (0xFF & inPacket[1]) + " vs " + 0x00A3);
             }
             
             if(returnData != null)
             {
                 for (int i = 0; i < 5; i++)
                 {
                     returnData[i] = (short)(0xFF & inPacket[i + 2]);
                 }
             }
         
             return 0;
         }
     }
     
     /**
      * Class representing the Feedback transaction packet.
      */
     private final class FeedbackPacket extends Packet
     {
         /**
          * Constructor.
          */
         FeedbackPacket()
         {
             outPacket = new byte[34];
             inPacket = new byte[64];
         }
         /**
          * Reads from or writes to 1 pin using the Feedback packet and makes 
          * the response available.
          * @param channel the pin to read from or write to.
          * @param direction direction of pin and operation to perform - 
          * 0: input, otherwise output
          * @param state a one-element array containing the output state for 
          * writes. It is filled with the input state for reads. 
          * @return 0 on completion, or -1 for error.
          * @throws IOException or IO error
          */
         private int sendPacket(int channel, int direction, int[] state) throws Exception
         {
             short tempByte;
             boolean tempDir, tempState;
             
             outPacket[1] = (byte)(0xF8);            //Command Byte
             outPacket[2] = (byte)(0x0E);            //Number of words
             outPacket[3] = (byte)(0x00);            //extended command number
             
             tempDir = (direction != 0);
             tempState = (state[0] != 0);
                         
             if(channel <=  7)
             {
                 tempByte = (short) (0xFF & (int)Math.pow(2, channel));
                 outPacket[6] = (byte) tempByte;
                 if(tempDir)
                 {
                     outPacket[7] = (byte) tempByte;
                 }
                 if(tempState)
                 {
                     outPacket[8] = (byte) tempByte;
                 }
             }
             else if(channel <= 15)
             {
                 tempByte = (short) (0xFF & (int)Math.pow( 2, (channel - 8)));
                 outPacket[9] = (byte) tempByte;
                 if(tempDir)
                 {
                     outPacket[10] = (byte) tempByte;
                 }
                 if(tempState)
                 {
                     outPacket[11] = (byte) tempByte;
                 }
             }
             else if(channel <= 19)
             {
                 tempByte = (short) (0xFF & (int)Math.pow( 2, (channel - 16)));
                 outPacket[12] = (byte) tempByte;
                 if(tempDir)
                 {
                     outPacket[13] = (byte) ((byte)(tempByte) * 16);
                 }
                 if(tempState)
                 {
                     outPacket[13] += (byte) tempByte;
                 }
             }
             else if(channel <= 22)
             {
                 tempByte = (byte) (0xFF & (int)Math.pow( 2, (channel - 20)));
                 outPacket[14] = (byte) tempByte;
                 if(tempDir)
                 {
                     outPacket[15] = (byte) ((byte)(tempByte) * 16);
                 }
                 if(tempState)
                 {
                     outPacket[15] += (byte) tempByte;
                 }
             }
             else
             {
                 /* Error: Invalid Channel */
                 throw new Exception("Invalid Channel Number");
             }
 
             calculateExtendedChecksum(outPacket, this.outPacket.length);
             
             send(outPacket);
             
             if (receive(inPacket, 0, inPacket.length) < inPacket.length)
             {
                 /* Error: read failed, or did not read all of the buffer. */
                 throw new Exception("Read failed, or did not read all of the buffer");
             }
             
             int checksumTotal = calculateExtendedChecksum16(inPacket, inPacket.length);
             if (((checksumTotal/256) & 0xFF) != (0xFF & inPacket[5]))
             {
                 /* Error: read buffer has bad checksum16 - MSB. */
                 throw new Exception("Bad checksum - MSB: " + (0xFF & inPacket[5]) + " vs " + ((checksumTotal/256)));
             }
             if ((checksumTotal & 0xFF) != (0xFF &inPacket[4]))
             {
                 /* Error: read buffer has bad checksum16 - LSB. */
                 throw new Exception("Bad checksum - LSB: " + inPacket[4] + " vs " + (checksumTotal & 0xFF));
             }
             if (calculateExtendedChecksum8(inPacket) != (0xFF &inPacket[0]))
             {
                 /* Error: read buffer has bad checksum8. */
                 throw new Exception("Bad checksum: " + inPacket[0] + " vs " + calculateExtendedChecksum8(inPacket));
             }
             
             if (inPacket[1] != (byte)(0xF8) || inPacket[2] != (byte)(0x1D) || inPacket[3] != (byte)(0x00))
             {
                 /* Error: read buffer has wrong command bytes. */
                 throw new Exception("Wrong command bytes returned: " + inPacket[1] + ", " + inPacket[2] + ", " + inPacket[3]);
             }
             
             if(channel <=  7)
             {
                 state[0] = (short)((inPacket[7]&tempByte) == 0 ? 0 : 1);
             }
             else if(channel <= 15)
             {
                 state[0] = (short)((inPacket[9]&tempByte) == 0 ? 0 : 1);
             }
             else if(channel <= 19)
             {
                 state[0] = (short)((inPacket[10]&tempByte) == 0 ? 0 : 1);
             }
             else if(channel <= 22)
             {            
                 state[0] = (short)((inPacket[11]&tempByte) == 0 ? 0 : 1);
             }
             
             return 0;
         }
     }
     
     /**
      * Class representing the get calibration transaction packet.
      */
     private final class CalibrationPacket extends Packet
     {
         private CalibrationPacket()
         {
             outPacket = new byte[8];
             inPacket = new byte[136];
         }
         
         /**
          * Requests calibration information from the Labjack, then formats it
          * and places it in the object provided.
          * @param cal an object used to store the calibration constants
          * @return 0 on completion, or -1 on error
          * @throws IOException on IO error
          */
         private int sendPacket(GeneralCalibrationInfo cal) throws Exception
         {
         
             outPacket[1] = (byte)(0xF8);        //command byte
             outPacket[2] = (byte)(0x01);          //number of data words
             outPacket[3] = (byte)(0x2A);          //extended command number
             outPacket[6] = (byte)(0x00);
             outPacket[7] = (byte)(0x00);          //Block Number 0
             
             calculateExtendedChecksum(outPacket, this.outPacket.length);
             
             send(outPacket);
             
             if (receive(inPacket, 0, inPacket.length) < inPacket.length)
             {
                 /* Error: read failed, or did not read all of the buffer. */
                 throw new Exception("Read failed, or did not read all of the buffer");
             }
             
             if (inPacket[1] != (byte)(0xF8) || inPacket[2] != (byte)(0x41) || inPacket[3] != (byte)(0x2A))
             {
                 /* Error: read buffer has wrong command bytes. */
                 throw new Exception("Read buffer has wrong command bytes");
             }
             
             cal.unipolarSlope[0] = convertByteArrayToDouble(inPacket, 0 + 8);
             cal.unipolarOffset[0] = convertByteArrayToDouble(inPacket, 8 + 8);
             cal.unipolarSlope[1] = convertByteArrayToDouble(inPacket, 16 + 8);
             cal.unipolarOffset[1] = convertByteArrayToDouble(inPacket, 24 + 8);
             cal.unipolarSlope[2] = convertByteArrayToDouble(inPacket, 32 + 8);
             cal.unipolarOffset[2] = convertByteArrayToDouble(inPacket, 40 + 8);
             cal.unipolarSlope[3] = convertByteArrayToDouble(inPacket, 48 + 8);
             cal.unipolarOffset[3] = convertByteArrayToDouble(inPacket, 56 + 8);
 
             
             
             /* Block Number 1 */
             outPacket[7] = (byte)(0x01);
             
             calculateExtendedChecksum(outPacket, 8);
             
             send(outPacket);
             
             if (receive(inPacket, 0, inPacket.length) < inPacket.length)
             {
                 /* Error: read failed, or did not read all of the buffer. */
                 throw new Exception("Read failed, or did not read all of the buffer");
             }
             
             if (inPacket[1] != (byte)(0xF8) || inPacket[2] != (byte)(0x41) || inPacket[3] != (byte)(0x2A))
             {
                 /* Error: read buffer has wrong command bytes. */
                 throw new Exception("Read buffer has wrong command bytes");
             }
             
             cal.bipolarSlope = convertByteArrayToDouble(inPacket, 0 + 8);
             cal.bipolarOffset = convertByteArrayToDouble(inPacket, 8 + 8);
             
             
             
             /* Block Number 2 */
             outPacket[7] = (byte)(0x02);
             
             calculateExtendedChecksum(outPacket, 8);
             
             send(outPacket);
             
             if (receive(inPacket, 0, inPacket.length) < inPacket.length)
             {
                 /* Error: read failed, or did not read all of the buffer. */
                 throw new Exception("Read failed, or did not read all of the buffer");
             }
             
             if (inPacket[1] != (byte)(0xF8) || inPacket[2] != (byte)(0x41) || inPacket[3] != (byte)(0x2A))
             {
                 /* Error: read buffer has wrong command bytes. */
                 throw new Exception("Read buffer has wrong command bytes");
             }
             
             cal.DACSlope[0] = convertByteArrayToDouble(inPacket, 0 + 8);
             cal.DACOffset[0] = convertByteArrayToDouble(inPacket, 8 + 8);
             cal.DACSlope[1] = convertByteArrayToDouble(inPacket, 16 + 8);
             cal.DACOffset[1] = convertByteArrayToDouble(inPacket, 24 + 8);
             cal.tempSlope = convertByteArrayToDouble(inPacket, 32 + 8);
             cal.tempSlopeLow = convertByteArrayToDouble(inPacket, 48 + 8);
             cal.calTemp = convertByteArrayToDouble(inPacket, 64 + 8);
             cal.Vref = convertByteArrayToDouble(inPacket, 72 + 8);
             cal.VrefDiv2 = convertByteArrayToDouble(inPacket, 88 + 8);
             cal.VsSlope = convertByteArrayToDouble(inPacket, 96 + 8);
 
             
             
             /* Block Number 3 */
             outPacket[7] = (byte)(0x03);
             
             calculateExtendedChecksum(outPacket, 8);
             
             send(outPacket);
             
             if (receive(inPacket, 0, inPacket.length) < inPacket.length)
             {
                 /* Error: read failed, or did not read all of the buffer. */
                throw new Exception("Read failed, or did not read all of the buffer");
             }
             
             if (inPacket[1] != (byte)(0xF8) || inPacket[2] != (byte)(0x41) || inPacket[3] != (byte)(0x2A))
             {
                 /* Error: read buffer has wrong command bytes. */
                 throw new Exception("Read buffer has wrong command bytes");
             }
             
             cal.hiResUnipolarSlope = convertByteArrayToDouble(inPacket, 0 + 8);
             cal.hiResUnipolarOffset = convertByteArrayToDouble(inPacket, 8 + 8);
 
             
             
             /* Block Number 4 */
             outPacket[7] = (byte)(0x04);
             
             calculateExtendedChecksum(outPacket, 8);
             
             send(outPacket);
             
             if (receive(inPacket, 0, inPacket.length) < inPacket.length)
             {
                 /* Error: read failed, or did not read all of the buffer. */
                 throw new Exception("Read failed, or did not read all of the buffer");
             }
             
             if (inPacket[1] != (byte)(0xF8) || inPacket[2] != (byte)(0x41) || inPacket[3] != (byte)(0x2A))
             {
                 /* Error: read buffer has wrong command bytes. */
                 throw new Exception("Read buffer has wrong command bytes");
             }
             
             cal.hiResBipolarSlope = convertByteArrayToDouble(inPacket, 0 + 8);
             cal.hiResBipolarOffset = convertByteArrayToDouble(inPacket, 8 + 8);
             cal.prodID = 9;
             
             return 0;
         }
     }
     
     /**
      * Class representing the reset transaction
      */
     private final class ResetPacket extends Packet
     {
         private ResetPacket()
         {
             outPacket = new byte[4];
             inPacket = new byte[4];
         }
         
         /**
          * Resets labjack
          * @param resetType the type of reset to perform. 0 for soft reset, 
          * otherwise hard reset
          * @return 0 on completion, or -1 on error.
          * @throws IOException on IO error
          */
         private int sendPacket(int resetType) throws Exception
         {
         
             this.outPacket[1] = (byte)(0x99);                    //command byte
             this.outPacket[2] = (byte)(resetType == 0 ? 1 : 2);    //type of reset
             this.outPacket[3] = (byte)(0x00);
             
             calculateNormalChecksum(outPacket, this.outPacket.length);
             
             send(this.outPacket);
             
             if (receive(this.inPacket, 0, inPacket.length) < inPacket.length)
             {
                 /* Error: read failed, or did not read all of the buffer. */
                 throw new Exception("Read failed, or did not read all of the buffer");
             }
             
             if (this.inPacket[1] != (byte)(0x99) || this.inPacket[2] != (byte)(0x00))
             {
                 /* Error: read buffer has wrong command bytes. */
                 throw new Exception("Read buffer has wrong command bytes");
             }
           
             return 0;
         }
         
         
     }
     
     /**
      * Class providing the I2C read and write functionality
      */
     private final class I2cPacket extends Packet
     {
 
         /**
          * Requests the Labjack to send the specified I2C packet on the pins specified.
          * @param I2COptions Bits 7-5: Reserved, Bit 4: Enable clock stretching, 
          * Bit 2: No stop when restarting, Bit 1: ResetAtStart, Bit 0: Reserved
          * @param speedAdjust Allows the communication frequency to be reduced. 
          * 0: ~150KHz (max), 20: ~70KHz, 255: ~10KHz (min)  
          * @param SDAPin the pin to use for SDA
          * @param SCLPin the pin to use for SCL
          * @param address Bits 7-1: Address of slave device, Bit 0: 0
          * @param bytesToSend number of bytes to send (up to 240)
          * @param bytesToReceive number of bytes to read (up to 240)
          * @param bytesSend array containing bytes to be sent 
          * @param bytesReceive will be filled with bytes read
          * @return status returned by Labjack, or -1 on error. 
          * See http://labjack.com/support/ue9/users-guide/5.4
          * @throws IOException on IO error
          */
         private int sendPacket(int I2COptions, int speedAdjust, int SDAPin, int SCLPin, int address, int bytesToSend,
                 int bytesToReceive, byte[] bytesSend, byte[] bytesReceive) throws Exception
         {
             
             this.outPacket = new byte[6 + 8 + ((bytesToSend%2 != 0)?(bytesToSend + 1):(bytesToSend))];
             this.inPacket = new byte[6 + 6 + ((bytesToReceive%2 != 0)?(bytesToReceive + 1):(bytesToReceive))];
             
             this.outPacket[1] = (byte)(0xF8);                            //command byte
             this.outPacket[2] = (byte)((this.outPacket.length - 6)/2);   //number of data words = 4 + bytesToSend
             this.outPacket[3] = (byte)(0x3B);                            //extended command number
 
             this.outPacket[6] = (byte)I2COptions;                        //I2COptions
             this.outPacket[7] = (byte)speedAdjust;                       //SpeedAdjust
             this.outPacket[8] = (byte)SDAPin;                            //SDAPinNum
             this.outPacket[9] = (byte)SCLPin;                            //SCLPinNum
             this.outPacket[10] = (byte)address;                          //AddressByte: bit 0 needs to be zero, 
                                                                          //             bits 1-7 is the address
             this.outPacket[11] = 0;                                      //Reserved
             this.outPacket[12] = (byte)bytesToSend;                      //NumI2CByteToSend
             this.outPacket[13] = (byte)bytesToReceive;                   //NumI2CBytesToReceive
             
             for(int i = 0; i < bytesToSend; i++)
             {
                 this.outPacket[14 + i] = bytesSend[i];                   //I2CByte
             }
             
             calculateExtendedChecksum(this.outPacket, this.outPacket.length);
             
             send(this.outPacket);
             
             if (receive(inPacket, 0, this.inPacket.length) < this.inPacket.length)
             {
                 /* Error: read failed, or did not read all of the buffer. */
                 throw new Exception("Read failed, or did not read all of the buffer");
             }
             
             int checksumTotal = calculateExtendedChecksum16(this.inPacket, this.inPacket.length);
             if (((checksumTotal/256) & 0xFF) != (0xFF &this.inPacket[5]))
             {
                 /* Error: read buffer has bad checksum16 - MSB. */
                 throw new Exception("Read buffer has bad checksum16 - MSB");
                 //return -1;
             }
             if ((checksumTotal & 0xFF) != (0xFF & this.inPacket[4]))
             {
                 /* Error: read buffer has bad checksum16 - LSB. */
                 throw new Exception("read buffer has bad checksum16 - LSB");
               //  return -1;
             }
             if (calculateExtendedChecksum8(this.inPacket) != (0xFF & this.inPacket[0]))
             {
                 /* Error: read buffer has bad checksum8. */
                 throw new Exception("Read buffer has bad checksum8");
                // return -1;
             }
             
             if (this.inPacket[1] != this.outPacket[1] || this.inPacket[3] != this.outPacket[3])
             {
                 /* Error: read buffer has wrong command bytes. */
                 throw new Exception("Read buffer has wrong command bytes");
             }
             
             if ((0xFF & this.inPacket[2]) != (this.inPacket.length - 6)/2)
             {
                 /*  Error: read buffer has incorrect number of data words. */
                 throw new Exception();
               //  return -1;
             }
             
             for (int i = 0; i < bytesToReceive; i++)
             {
                 bytesReceive[i] = this.inPacket[12 + i];
             }
             
             return this.inPacket[6];        //TODO: untested - may not return 0 on success 
             
         }
         
         
     }
     
     
     /**
      * Class to read the communication config from the labjack
      */
     private final class CommConfigPacket extends Packet
     {
         private CommConfigPacket()
         {
             outPacket = new byte[38];
             inPacket = new byte[38];
         }
         
         private int sendPacket(CommConfigInfo con) throws Exception
         {
             outPacket[1] = (byte)(0x78);            //command byte
             outPacket[2] = (byte)(0x10);            //number of data words
             outPacket[3] = (byte)(0x01);            //extended command number
             outPacket[6] = (byte)(0x00);            //Read only
             outPacket[7] = (byte)(0x00);          
             
             calculateExtendedChecksum(outPacket, this.outPacket.length);
             
             send(outPacket);
             
             if (receive(inPacket, 0, inPacket.length) < inPacket.length)
             {
                 /* Error: read failed, or did not read all of the buffer. */
                 throw new Exception("Read failed, or did not read all of the buffer");
             }
             
             if (inPacket[1] != (byte)(0x78) || inPacket[2] != (byte)(0x10) || inPacket[3] != (byte)(0x01))
             {
                 /* Error: read buffer has wrong command bytes. */
                 throw new Exception("Read buffer has wrong command bytes");
             }
             
             con.localID = inPacket[8];
             con.powerLevel = inPacket[9];
             for (int i = 0; i < 4; i++)
             {
                 con.address[i] = inPacket[10 + i];          //address[0] is LSB
             }
             for (int i = 0; i < 4; i++)
             {            
                 con.gateway[i] = inPacket[14 + i];
             }
             for (int i = 0; i < 4; i++)
             {
                 con.subnet[i] = inPacket[18 + i];
             }
             con.portA = (0xff & inPacket[22]) + ((0xff & inPacket[23]) * 256);
             con.portB = (0xff & inPacket[24]) + ((0xff & inPacket[25]) * 256);
             con.dhcpEnabled = inPacket[26];
             con.prodID = inPacket[27];
             for (int i = 0; i < 6; i++)
             {
                 con.macAddress[i] = inPacket[28+i];
             }
             con.hwVersion = (double)(0xff & inPacket[35]) + ((double)(0xff & inPacket[34]) / 256.0);
             con.commFwVersion = (double)(0xff & inPacket[37]) + ((double)(0xff & inPacket[36]) / 256.0);
             
             return 0;
         }
     }
     
     
     
     
     
     /**
      * Superclass of all Calibration Info classes
      */
     private abstract class Info
     {
         public short prodID;
         
         /**
          * Checks validity of calibration info
          * @return true if calibration is valid, false otherwise
          */
         public boolean checkCalibrationInfo()
         {
             if (this.prodID != 9)
             {
                 return false;
             }
             return true;
         }
     }
     
     /**
      * Stores communication configuration info
      */
     private class CommConfigInfo extends Info
     {
         @SuppressWarnings("unused")
         public short localID;
         @SuppressWarnings("unused")
         public short powerLevel;
         public byte[] address;
         public byte[] gateway;
         public byte[] subnet;
         @SuppressWarnings("unused")
         public int portA;
         @SuppressWarnings("unused")
         public int portB;
         @SuppressWarnings("unused")
         public short dhcpEnabled;
         public byte[] macAddress;
         @SuppressWarnings("unused")
         public double hwVersion;
         @SuppressWarnings("unused")
         public double commFwVersion;
         
         public int calculatedSerialNumber;
         
         public CommConfigInfo()
         {
             address = new byte[4];
             gateway = new byte[4];
             subnet = new byte[4];
             macAddress = new byte[6];
         }
         
         public void getCommConfig() throws Exception
         {
             CommConfigPacket packet = new CommConfigPacket();
             packet.sendPacket(this);
                     
             this.calculatedSerialNumber = (0xFF & macAddress[0]) + ((0xFF & macAddress[1])*256) + 
                     ((0xFF & macAddress[2])*65536) + ((0xFF & 0x10)*16777216);
         }
     }
     
     /**
      * Stores calibration information for a LabJack Tick DAC module
      */
     public final class LJTDACCalibrationInfo extends Info
     {
         public double DACSlopeA;
         public double DACOffsetA;
         public double DACSlopeB;
         public double DACOffsetB;
         
         /**
          * Requests calibration information from LJTick module attached to 
          * Labjack
          * @param SDAPin pin used for SDA by LJTick
          * @param SCLPin pin used for SCL by LJTick
          * @throws IOException on IO error
          */
         public void getCalibration(int SDAPin, int SCLPin) throws Exception
         {
             I2cPacket packet = new I2cPacket();
             byte bytesSend[] = {64};
             byte bytesReceive[] = new byte[32];
             
             packet.sendPacket(0, 0, SDAPin, SCLPin, LJTDAC_EEPROM_ADDRESS, bytesSend.length, bytesReceive.length, bytesSend, 
                     bytesReceive);
             
             this.DACSlopeA = convertByteArrayToDouble(bytesReceive, 0);
             this.DACOffsetA = convertByteArrayToDouble(bytesReceive, 8);
             this.DACSlopeB = convertByteArrayToDouble(bytesReceive, 16);
             this.DACOffsetB = convertByteArrayToDouble(bytesReceive, 24);
         }
     }
     
     /**
      * Stores general calibration information for a LabJack
      */
     public final class GeneralCalibrationInfo extends Info
     {
         public double unipolarSlope[];
         public double unipolarOffset[];
         public double bipolarSlope;
         public double bipolarOffset;
         public double DACSlope[];
         public double DACOffset[];
         public double tempSlope;
         public double tempSlopeLow;
         public double calTemp;
         public double Vref;
         public double VrefDiv2;
         public double VsSlope;
         public double hiResUnipolarSlope;
         public double hiResUnipolarOffset;
         public double hiResBipolarSlope;
         public double hiResBipolarOffset;
         
         public GeneralCalibrationInfo()
         {
             unipolarSlope = new double[4];
             unipolarOffset = new double[4];
             DACSlope = new double[2];
             DACOffset = new double[2];
         }
         
         /**
          * Requests and stores calibration information from labjack
          * @throws IOException on IO error
          */
         public void getCalibration() throws Exception
         {
             CalibrationPacket packet = new CalibrationPacket();
             packet.sendPacket(this);
         }
     }
     
 }
 
