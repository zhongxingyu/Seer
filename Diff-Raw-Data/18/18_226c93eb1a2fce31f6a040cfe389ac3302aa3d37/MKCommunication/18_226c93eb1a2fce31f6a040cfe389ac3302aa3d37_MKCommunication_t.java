 package mkpc.comm;
 
 import gnu.io.CommPortIdentifier;
 import gnu.io.PortInUseException;
 import gnu.io.SerialPort;
 import gnu.io.SerialPortEvent;
 import gnu.io.SerialPortEventListener;
 import gnu.io.UnsupportedCommOperationException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.TimerTask;
 import java.util.TooManyListenersException;
 import java.util.Timer;
 
 import mkpc.app.Application;
 import mkpc.log.LogSystem;
 
 /**
  * This class is used to communicated with the copter
  * with this class you can connect and disconnect.
  * 
  * The class manipulated the connection Button to give feedback to
  * the user. So it shows Search did start or shows is connected, is disconnected and so on.
  * 
  * All errors will send to the Log-System, so you can see this errors in the console and in the log files
  * @author bk
  */
 public class MKCommunication 
 {
 	// serial communication
 	boolean isPortOpen = false;
 	Enumeration<?> enumComm;
 	CommPortIdentifier serialPortId;
 	SerialPort serialPort;
 	OutputStream outputStream;
 	InputStream inputStream;
 	int baudrate = 57600;
 	int dataBits = SerialPort.DATABITS_8;
 	int stopBits = SerialPort.STOPBITS_1;
 	int parity = SerialPort.PARITY_NONE;
 	String portName = "COM1";
 
 	String buffer = "";
 	String packageInput = "";
 
 	boolean initalCopterRequestStarted = false;
 	boolean isCopterConnected = false;
 	int requestTimeOut = 12000;	// ms
 	Timer timer = null;
 	
 	/// new since 24.02.2011
 	MKCommunicationDelegate delegate = null;
 	ArrayList<String> outputQueue = null;
 	boolean isExecutingOutputQueue = false;
 	
 	public MKCommunication() 
 	{
 		outputQueue = new ArrayList<String>();
 		isExecutingOutputQueue = false;
 	}
 	
 	public boolean isPortOpen()
 	{
 		return this.isPortOpen;
 	}
 	
 	/**
 	 * Open COM-Port and manipulate the button text for feedback
 	 * @param portName	Portname example COM1
 	 * @param btn Button which will change the text
 	 */
 	public void openComPort(String portName, MKCommunicationDelegate delegate)
 	{
 		final String comPort = portName;
 		final MKCommunicationDelegate answerDelegate = delegate;
 		
 		this.delegate = delegate;
 		
 		new Thread(new Runnable() {
 			public void run() {
 				if(openComPort(comPort))
 				{
 					answerDelegate.communicationDidOpenCOMPort();
 					// request copter
 					initalCopterRequestStarted = true;
 					
 					if(timer == null)
 					{
 						timer = new Timer();
 					}
 					
 					timer.schedule(new InitalBreakTask(), requestTimeOut);
 					
 					int[] data = new int[2];
 					data[0] = 0;
 					data[1] = 0;
 
 					sendCommand('c', 'z', data);
 					//stopButtonAnimation = true;
 					//setButtonTextToFinish();
 				}
 			}
 		}).start();
 	}
 
 	public boolean openComPort(String portName)
 	{
 		Boolean foundPort = false;
 		
 		this.portName = portName;
 
 		if (isPortOpen != false) {
 			LogSystem.CLog(LogSystem.getMessage("serialCommError002"));
 			return false;
 		}
 		LogSystem.CLog("ffne Serialport");
 		enumComm = CommPortIdentifier.getPortIdentifiers();
 		while (enumComm.hasMoreElements()) {
 			serialPortId = (CommPortIdentifier) enumComm.nextElement();
 			LogSystem.CLog("serial Portname: " + serialPortId.getName());
 			if (portName.contentEquals(serialPortId.getName())) {
 				foundPort = true;
 				break;
 			}
 		}
 		if (foundPort != true) 
 		{
 			delegate.communicationDidFailOpenCOMPort(LogSystem.getMessage("serialCommError003")+ " " + portName);
 			//LogSystem.addLog(LogSystem.getMessage("serialCommError003")+ " " + portName);
 			return false;
 		}
 
 		try 
 		{
 			serialPort = (SerialPort) serialPortId.open("ffnen und Senden", 500);
 		} 
 		catch (PortInUseException e) 
 		{
 			delegate.communicationDidFailOpenCOMPort("Portbelegt");
 		}
 		
 		try 
 		{
 			outputStream = serialPort.getOutputStream();
 		} 
 		catch (IOException e) 
 		{
 			delegate.communicationDidFailOpenCOMPort("Keinen Zugriff auf OutputStream");
 		}
 
 		try 
 		{
 			inputStream = serialPort.getInputStream();
 		} 
 		catch (IOException e) 
 		{
 			delegate.communicationDidFailOpenCOMPort("Keinen Zugriff auf InputStream");
 		}
 
 		try 
 		{
 			serialPort.addEventListener(new serialPortEventListener());
 		} 
 		catch (TooManyListenersException e) 
 		{
 			delegate.communicationDidFailOpenCOMPort("TooManyListenersException fr Serialport");
 		}
 		serialPort.notifyOnDataAvailable(true);
 		
 		try 
 		{
 			serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
 		} 
 		catch (UnsupportedCommOperationException e) 
 		{
 			delegate.communicationDidFailOpenCOMPort("Konnte Schnittstellen-Paramter nicht setzen");
 		}
 		
 		isPortOpen = true;
 
 		return true;
 	}
 	
 	public void closeComPort()
 	{
 		if (isPortOpen == true) 
 		{
 			LogSystem.CLog("Schliee Serialport");
 			serialPort.close();
 			isPortOpen = false;
 		} 
 		else 
 		{
 			LogSystem.CLog("Serialport bereits geschlossen");
 		}
 	}
 	
 	public void dispatchDataInput() 
 	{
 		String stringBuffer = null;
 		
 		// get input data
 		try 
 		{
 			byte[] data = new byte[150];
 			int num;
 			while (inputStream.available() > 0) 
 			{
 				num = inputStream.read(data, 0, data.length);
 				stringBuffer = new String(data, 0, num);
 				
 				// check endbyte
 				LogSystem.CLog(stringBuffer);
 				int endBytePos = stringBuffer.indexOf('\r');
 				if(endBytePos > -1)
 				{
 					LogSystem.CLog("founded endbyte");
 					String subString = "";
 					// more input after endbyte?
 					if(endBytePos+1 < stringBuffer.length())
 					{
 						subString = stringBuffer.substring(endBytePos+1, stringBuffer.length()-1);
 					}
 					// some string already in the buffer
 					buffer = buffer + stringBuffer.substring(0, endBytePos);
 					
 					// check string is right (CRC1/ CRC2)
 					LogSystem.CLog("Buffer: " + buffer);
 					char[] dataToCheck = new char[buffer.length()-2];
 					buffer.getChars(0, buffer.length()-2, dataToCheck, 0);	// -1 start at index zero, -2 (CRC1/2)
 					char[] crc = this.createCheckSum(dataToCheck, dataToCheck.length);
 					LogSystem.CLog("CRC 1:" + crc[0] + " CRC 2:" + crc[1]);
 					if(crc[0] == buffer.charAt(buffer.length()-2) && crc[1] == buffer.charAt(buffer.length()-1))
 					{
 						// checkSum is correct
 						String command = buffer.substring(0, buffer.length()-2);
 						buffer = subString;
 						this.dispatchCommand(command);
 					}
 					else
 					{
 						buffer = subString;
 					}
 				}
 				else
 				{
 					buffer = buffer + stringBuffer;
 				}
 			}
 		} 
 		catch (IOException e) 
 		{
 			LogSystem.CLog(LogSystem.getMessage("serialCommError001"));
 		}
 	}
 	
 	/**
 	 * Dispatch input string and look which address will call
 	 * @param inputString Input command (without endByte '\r' and without decoding)
 	 */
 	public void dispatchCommand(String inputString)
 	{
 		char address = inputString.charAt(1);
 		
 		// testing ---
 		if(initalCopterRequestStarted)
 		{
 			initalCopterRequestStarted = false;
 			timer.cancel();
 			timer = null;
 			delegate.communicationDidOpenCopterConnection();
 		}
 		// testing ---
 		
 		switch (address)
 		{
 			case MKAddress.All:
 				this.dispatchCommandForAddressAll(inputString);
 				break;
 			case MKAddress.FC:
 				this.dispatchCommandForAddressFC(inputString);
 				break;
 			case MKAddress.NC:
 				this.dispatchCommandForAddressNC(inputString);
 				break;
 			case MKAddress.MK3MAG:
 				this.dispatchCommandForAddressMK3MAG(inputString);
 				break;
 			case MKAddress.MKGPS:
 				this.dispatchCommandForAddressMKGPS(inputString);
 				break;
 			default:
 				LogSystem.addLog("Incoming string has an unknown address.");
 				break;
 		}
 	}
 	
 	/**
 	 * Dispatch all command for address "ALL"
 	 * @param inputString Input command (without endByte '\r' and without decoding)
 	 */
 	public void dispatchCommandForAddressAll(String inputString)
 	{
 		char command = inputString.charAt(2);
 		switch (command)
 		{
 			case MKCommand.DebugLabelResponse:
 				break;
 			case MKCommand.ExternControlResponse:
 				break;
 			case MKCommand.DisplayResponse:
 				break;
 			case MKCommand.DisplayMenuResponse:
 				break;
 			case MKCommand.VersionResponse:
 				break;
 			case MKCommand.DebugValueResponse:		
 				break;
 			case MKCommand.GetExternControlResponse:		
 				break;
 			default:
 				LogSystem.addLog("Incoming string has an unknown command-ID for address '" + MKAddress.All + "'.");
 				break;
 		}
 	}
 	
 	/**
 	 * Dispatch all command for address "FC"
 	 * @param inputString Input command (without endByte '\r' and without decoding)
 	 */
 	public void dispatchCommandForAddressFC(String inputString)
 	{
 		char command = inputString.charAt(2);
 		switch (command)
 		{
 			case MKCommand.CompassHeadingResponse:
 				break;
 			case MKCommand.EnginTestResponse:
 				break;
 			case MKCommand.SettingsResponse:
 				break;
 			case MKCommand.WriteSettingsResponse:
 				break;
 			case MKCommand.ChannelsValueResponse:
 				break;
 			case MKCommand.Set3DDataIntervalResponse:
 				break;
 			case MKCommand.MixerReadResponse:		
 				break;
 			case MKCommand.MixerWriteResponse:		
 				break;
 			case MKCommand.ChangeSettingsResponse:		
 				break;
 			case MKCommand.SerialPotiResponse:		
 				break;
 			case MKCommand.BLParameterResponse:		
 				break;
 			case MKCommand.WriteBLParameterResponse:		
 				break;
 			default:
 				LogSystem.addLog("Incoming string has an unknown command-ID for address '" + MKAddress.All + "'.");
 				break;
 		}
 	}
 	
 	/**
 	 * Dispatch all command for address "NC"
 	 * @param inputString Input command (without endByte '\r' and without decoding)
 	 */
 	public void dispatchCommandForAddressNC(String inputString)
 	{
 		char command = inputString.charAt(2);
 		switch (command)
 		{
 			case MKCommand.SerialLinkTestResponse:
 				break;
 			case MKCommand.ErrorTextResponse:
 				break;
 			case MKCommand.SendWaypointResponse:
 				break;
 			case MKCommand.WaypointResponse:
 				break;
 			case MKCommand.OsdResponse:
 				break;
 			case MKCommand.Set3DDataIntervalResponse:
 				{
 					String dataStr = inputString.substring(3);
 					char[] data = decode64ToChar(dataStr.toCharArray());
 					Application.sharedApplication().getViewController3D().dispatchIncomingData(data);
 				}
 				break;
 			default:
 				LogSystem.addLog("Incoming string has an unknown command-ID for address '" + MKAddress.All + "'.");
 				break;
 		}
 	}
 	
 	/**
 	 * Dispatch all command for address "MK3MAG"
 	 * @param inputString Input command (without endByte '\r' and without decoding)
 	 */
 	public void dispatchCommandForAddressMK3MAG(String inputString)
 	{
 		char command = inputString.charAt(2);
 		switch (command)
 		{
 			case MKCommand.HeadingResponse:
 				break;
 			default:
 				LogSystem.addLog("Incoming string has an unknown command-ID for address '" + MKAddress.MK3MAG + "'.");
 				break;
 		}
 	}
 	
 	/**
 	 * Dispatch all command for address "MKGPS"
 	 * @param inputString Input command (without endByte '\r' and without decoding)
 	 */
 	public void dispatchCommandForAddressMKGPS(String inputString)
 	{
 		
 	}
 	
 	/**
 	 * pseudo Encode64
 	 */
 	public char[] encode64(int[] params)
 	{
 		int length = ( params.length/3 + (params.length%3==0?0:1) )*4;
 		char[] result = new char[length];
 		
 		for ( int i = 0; i < length/4; ++i)
 		{
 			int a = (i*3<params.length)?params[i*3]:0;
             int b = ((i*3+1)<params.length)?params[i*3+1]:0;
             int c = ((i*3+2)<params.length)?params[i*3+2]:0;
 
             result[i*4] =  (char)((a >> 2)+'=' );
             result[i*4+1] = (char)('=' + (((a & 0x03) << 4) | ((b & 0xf0) >> 4)));
             result[i*4+2] = (char)('=' + (((b & 0x0f) << 2) | ((c & 0xc0) >> 6)));
             result[i*4+3] = (char)('=' + ( c & 0x3f));
 		}
 		
 		return result;
 	}
 	
 	public char[] encode64(char[] params)
 	{
 		int length = ( params.length/3 + (params.length%3==0?0:1) )*4;
 		char[] result = new char[length];
 		
 		for ( int i = 0; i < length/4; ++i)
 		{
 			char a = (i*3<params.length)?params[i*3]:0;
             char b = ((i*3+1)<params.length)?params[i*3+1]:0;
             char c = ((i*3+2)<params.length)?params[i*3+2]:0;
 
             result[i*4] =  (char)((a >> 2)+'=' );
             result[i*4+1] = (char)('=' + (((a & 0x03) << 4) | ((b & 0xf0) >> 4)));
             result[i*4+2] = (char)('=' + (((b & 0x0f) << 2) | ((c & 0xc0) >> 6)));
             result[i*4+3] = (char)('=' + ( c & 0x3f));
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param in String input
 	 * @return char Array
 	 */
 	public static char[] decode64(String in) {
 		return decode64(in.toCharArray());
 	}
 
 	public static char[] decode64(char[] in_arr) {
 		// substract '='
 		for (int i = 0; i < in_arr.length; i++)
 			in_arr[i] -= '=';
 		// calculate length of out_arr, for every 4 bytes we get 3 bytes
 		int len = in_arr.length * 3 / 4;
 		char[] out_arr = new char[len];	// --- char change to int
 		// counting the blocks (4 bytes for in_arr, 3 bytes for out_arr)
 		for (int i = 0; (i + 1) * 4 < in_arr.length; i++) {
 			// placeholder for four bytes of in_arr
 			int fourbytes = 0;
 			for (int j = 0; j < 4; j++) {
 				/*
 				 * "or" 4 bytes togehter (i*4): 1. Byte of the block in in_arr
 				 * 3-j: position in a block from right to left <<(j*6):
 				 * bit-shift left 6 bits "j"-times it's then
 				 * 012345|012345|012345|012345
 				 */
 				fourbytes |= (in_arr[(i * 4) + 3 - j]) << (j * 6);
 			}
 			for (int j = 0; j < 3; j++) {
 				/*
 				 * seperated new to 01234501|23450123|45012345 (i*3): 1. Byte of
 				 * a block in out_arr 2-j: 3-j: position in a block from right
 				 * to left >>(j*8): bit-shift right 8 bits "j"-times &0x00ff:
 				 * use bit-mask, so that only rightest 8 bits are taken
 				 */
 				out_arr[(i * 3) + 2 - j] = (char) ((fourbytes >> (j * 8)) & 0x00ff); // --- char change to int
 			}
 		}
 		return out_arr;
 	}
 	
 	public static int[] decode64V2(char[] values)
 	{
 		int a, b, c, d;
 		int x, y, z;
 		int ptrIn = 0;
 		int ptrOut = 0;
 		int len = values.length; 
 		
 		int[] result = new int[(len + 2)*3/4];
 		LogSystem.CLog("Decoder64");
 		while (len-- > 0) 
 		{
 			a = (int) (values[ptrIn++] - '=');
 			b = (int) (values[ptrIn++] - '=');
 			c = (int) (values[ptrIn++] - '=');
 			d = (int) (values[ptrIn++] - '=');
 
 			x = (int) ((a << 2) | (b >> 4));
 			y = (int) (((b & 0x0f) << 4) | (c >> 2));
 			z = (int) (((c & 0x03) << 6) | d);
 
 			if (len-- > 0)
 				result[ptrOut++] = (int) x;
 			else
 				break;
 			
 			if (len-- > 0)
 				result[ptrOut++] = (int) y;
 			else
 				break;
 			
 			if (len-- > 0)
 				result[ptrOut++] = (int) z;
 			else
 				break;
 			
 			LogSystem.CLog("Length: "+len);
 		}
 		LogSystem.CLog("Result: "+result.toString());
 		return result;
 	}
 
 	public static char[] decode64ToChar(char[] values)
 	{
 		char a, b, c, d;
 		char x, y, z;
 		int ptrIn = 0;
 		int ptrOut = 0;
 		int len = values.length; 
 		
 		char[] result = new char[(len + 2)*3/4];
 		LogSystem.CLog("Decoder64");
 		while (len-- > 0) 
 		{
 			a = (char) (values[ptrIn++] - '=');
 			b = (char) (values[ptrIn++] - '=');
 			c = (char) (values[ptrIn++] - '=');
 			d = (char) (values[ptrIn++] - '=');
 
 			x = (char) ((a << 2) | (b >> 4));
 			y = (char) (((b & 0x0f) << 4) | (c >> 2));
 			z = (char) (((c & 0x03) << 6) | d);
 
 			if (len-- > 0)
 				result[ptrOut++] = (char) x;
 			else
 				break;
 			
 			if (len-- > 0)
 				result[ptrOut++] = (char) y;
 			else
 				break;
 			
 			if (len-- > 0)
 				result[ptrOut++] = (char) z;
 			else
 				break;
 			
 			LogSystem.CLog("Length: "+len);
 		}
 		LogSystem.CLog("Result: "+result.toString());
 		return result;
 	}
 	
 	/**
 	 * Create the check sum (CRC1 and CRC2)
 	 * @param data Array where the check sum is need
 	 * @param arrayLenght Length of the Array
 	 * @return char[2] 0 = CRC1; 1 = CRC2
 	 */
 	public char[] createCheckSum(char[] data, int arrayLenght) 
 	{
 		char[] result = new char[2];
 		
 		long tmpCRC = 0;
 		for (int i = 0; i < arrayLenght; i++) 
 		{
 			tmpCRC += (long) data[i];
 		}
 		tmpCRC %= 4096;
 		result[0] = (char) ((long) '=' + tmpCRC / 64);
 		result[1] = (char) ((long) '=' + tmpCRC % 64);
 		
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param modul
 	 * @param command
 	 * @param params
 	 */
 	public void sendCommand(char modul, char command, int[] params)
 	{
 		if (!this.isPortOpen)
 		{
 			LogSystem.addLog("Please start copter connect.");
 			return;
 		}
 		
 		char[] encode64Data = this.encode64(params);
 		char[] dataBuffer = new char[6+encode64Data.length];	// 6 = stateByte, addressByte, commandByte, check sum Bytes (CRC1/ CRC2), endByte
 		
 		int i = 0;
 		dataBuffer[i] = '#';
 		dataBuffer[++i] = modul;
 		dataBuffer[++i] = command;
 		
 		for(int j = 0; j < encode64Data.length; j++)
 		{
 			dataBuffer[++i] = encode64Data[j];
 		}
 		
 		char[] crc = this.createCheckSum(dataBuffer, dataBuffer.length - 3);
 		dataBuffer[++i] = crc[0];
 		dataBuffer[++i] = crc[1];
 		dataBuffer[++i] = '\r';
 
 		try 
 		{
             for(char value : dataBuffer)
             {
             	//System.out.print(value);
             	outputStream.write(value);
             }
             //outputStream.flush();
         } 
 		catch (IOException e) 
         {
         	LogSystem.CLog(e.getMessage());
         }
 	}
 	
 	public void sendCommand(final char modul, final char command, final char[] params)
 	{
 		if (!this.isPortOpen)
 		{
 			LogSystem.addLog("Please start copter connect.");
 			return;
 		}
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				char[] encode64Data = encode64(params);
 				char[] dataBuffer = new char[6+encode64Data.length];	// 6 = stateByte, addressByte, commandByte, check sum Bytes (CRC1/ CRC2), endByte
 				
 				int i = 0;
 				dataBuffer[i] = '#';
 				dataBuffer[++i] = modul;
 				dataBuffer[++i] = command;
 				
 				for(int j = 0; j < encode64Data.length; j++)
 				{
 					dataBuffer[++i] = encode64Data[j];
 				}
 				
 				char[] crc = createCheckSum(dataBuffer, dataBuffer.length - 3);
 				dataBuffer[++i] = crc[0];
 				dataBuffer[++i] = crc[1];
 				dataBuffer[++i] = '\r';
 
 				String output = "";
 	            for(char value : dataBuffer)
 	            {
 	            	System.out.print(value);
 	            	output = output + value;
 	            }
 	            
 	            addCommandToOutputQueue(output);
 			}
 		}).start();
 		
 //		char[] encode64Data = this.encode64(params);
 //		char[] dataBuffer = new char[6+encode64Data.length];	// 6 = stateByte, addressByte, commandByte, check sum Bytes (CRC1/ CRC2), endByte
 //		
 //		int i = 0;
 //		dataBuffer[i] = '#';
 //		dataBuffer[++i] = modul;
 //		dataBuffer[++i] = command;
 //		
 //		for(int j = 0; j < encode64Data.length; j++)
 //		{
 //			dataBuffer[++i] = encode64Data[j];
 //		}
 //		
 //		char[] crc = this.createCheckSum(dataBuffer, dataBuffer.length - 3);
 //		dataBuffer[++i] = crc[0];
 //		dataBuffer[++i] = crc[1];
 //		dataBuffer[++i] = '\r';
 //
 //		try 
 //		{
 //            for(char value : dataBuffer)
 //            {
 //            	System.out.print(value);
 //            	outputStream.write(value);
 //            }
 //            //outputStream.flush();
 //        } 
 //		catch (IOException e) 
 //        {
 //        	LogSystem.CLog(e.getMessage());
 //        }
 	}
 	
 	public void sendCommand(final String command)
 	{
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				try 
 				{
 					char[] dataBuffer = new char[command.length()];
 					command.getChars(0, command.length(), dataBuffer, 0);
 					char[] crc = createCheckSum(dataBuffer, dataBuffer.length);
 					String output = command + crc[0] + crc[1] + '\r';
 					LogSystem.addLog("Send: "+output);
 		            addCommandToOutputQueue(output);
 		            
 		        } 
 				catch (Exception e) 
 		        {
 		        	LogSystem.CLog(e.getMessage());
 		        }
 			}
 		}).start();
 	}
 	
 	/**
 	 * Add a command to the serial output queue, and start executing
 	 * @param command
 	 */
 	public void addCommandToOutputQueue(String command)
 	{
 		outputQueue.add(command);
 		this.executeOutputQueue();
 	}
 	
 	/**
 	 * start executing of the output queue and sends the commands
 	 * start only if no sending currently active
 	 */
 	public void executeOutputQueue()
 	{
 		LogSystem.CLog("executing ..............");
 		if(isExecutingOutputQueue == true)
 		{
 			return;
 		}
 		
 		isExecutingOutputQueue = true;
 		new Thread(new Runnable() 
 		{
 			@Override
 			public void run() 
 			{
 				while(outputQueue.size() > 0)
 				{
 					LogSystem.CLog("Working and sending.");
 					String command = outputQueue.get(0);
 					try 
 					{
 						outputStream.write(command.getBytes());
 					} 
 					catch (IOException e) 
 					{
 						LogSystem.addLog("Error: Can't send command: "+command);
 					}
 					outputQueue.remove(0);
 				}
 				isExecutingOutputQueue = false;
 			}
 		}).start();
 	}
 	
 	/**
 	 * @author bk
 	 * Internal Class to listen the serial port events
 	 */
 	class serialPortEventListener implements SerialPortEventListener {
 		public void serialEvent(SerialPortEvent event) {
 			// LogSystem.CLog("serialPortEventlistener");
 			switch (event.getEventType()) {
 			case SerialPortEvent.DATA_AVAILABLE:
 				dispatchDataInput();
 				break;
 			case SerialPortEvent.BI:
 			case SerialPortEvent.CD:
 			case SerialPortEvent.CTS:
 			case SerialPortEvent.DSR:
 			case SerialPortEvent.FE:
 			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
 			case SerialPortEvent.PE:
 			case SerialPortEvent.RI:
 			default:
 			}
 		}
 	}
 	
 	/**
 	 * Inital Request has an timer, if no answer incoming this timer will close the connection
 	 * and gives feedback
 	 * @author bk
 	 */
 	class InitalBreakTask extends TimerTask 
 	{
 		 public void run()  
 		 {
 			 initalCopterRequestStarted = false;
 			 isCopterConnected = false;
 			 delegate.communicationDidFailOpenCopterConnection(LogSystem.getMessage("serialCommError004"));
 			 //LogSystem.addLog(LogSystem.getMessage("serialCommError004"));
 		 }
 	}
 }
