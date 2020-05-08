 package com.example.android.BluetoothChat;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.IOException;
 
 import android.os.Environment;
 
 
 public class InputParser// extends Observer
 {
 	
 	private String _buffer;
 	private String[] _labelHeaders;
 	private float[] _tempFloatData;
     private int _messageLength;
     private int _dataLength;
     private String endLineString = "\r";
 	private BluetoothChat _BluetoothChat;
 	private FileWriter _FileWriter;
 	private boolean errorLog = false;
 	private String _errorLogFileName;
 	private FileWriter errorLogWriter;
 	
 	public InputParser(BluetoothChat bluetoothChat, FileWriter fileWriter, String errorLogName)
 	{
 		_BluetoothChat = bluetoothChat;
 		_FileWriter = fileWriter;
 		_errorLogFileName = errorLogName;
 
 		_labelHeaders = null;
 		_buffer = "";
 	}
 	private void sendToView(String message)
 	{
 		_BluetoothChat.displayLine(message);
 		_FileWriter.writeLine(message);
 	}
 	private void sendDataToModel(String message, float[] tempArray)
 	{
 		float[] newLabelArray = new float[tempArray.length+1];
 		System.arraycopy(tempArray, 0, newLabelArray, 0, tempArray.length);
 		newLabelArray[tempArray.length] = _BluetoothChat.getCurrentSpeed();
		tempArray = newLabelArray;
		
		message += "," + _BluetoothChat.getCurrentSpeed();
 		
 		sendToView(message);
		_BluetoothChat.receiveData(tempArray);
 	}
 	private void sendDataToModel(String message, String[] labelsArray)
 	{
 		String[] newLabelArray = new String[labelsArray.length+1];
 		System.arraycopy(labelsArray, 0, newLabelArray, 0, labelsArray.length);
		newLabelArray[labelsArray.length] = "BRPM";
		labelsArray = newLabelArray;
		
		message += ","+"BRPM";
 		
 		sendToView(message);
		_BluetoothChat.receiveHeaders(labelsArray);
 	}
 	
 	public void parseMessage(String message) //throws IOException
 	{
             _buffer = _buffer+message;
             int messageSize = _buffer.length();
 
             if(messageSize >= 6)
             {
                 String label = _buffer.substring(0, 5);
                 //_buffer = _buffer.substring(5);
 
                 if(label.equals("DATA_"))
                 {
                     // if takes into account an unfilled data segment
                     if(messageSize >= _messageLength+5)
                     {
                         _buffer = DATA_(_buffer);
                     }
                 }
                 else if(label.equals("COMP_"))
                 {
                     if(_buffer.contains(endLineString))
                     {
                         _buffer = COMP_(_buffer);
                     }
                 }
                 else if(label.equals("TEST_"))
                 {
                     if(_buffer.contains(endLineString))
                     {
                         _buffer = TEST_(_buffer);
                     }
                 }
                 
                 /*if(_buffer.charAt(0) == '\n')
                 {
                 	_buffer = _buffer.substring(1);
                 }*/
 
                 // repeat function if more data
                 if(_buffer.length() >= 6 && _buffer.length() < messageSize)
                 {
                     parseMessage("");
                 }
             }
 	}
         
         private String TEST_(String message)
         {
             int lineEndIndex = message.indexOf(endLineString)-5;
             message = message.substring(5);
             sendToView("TEST,"+message.substring(0, lineEndIndex));
             _BluetoothChat.displayPopUp("TEST,"+message.substring(0, lineEndIndex));
             return message.substring(lineEndIndex+endLineString.length());
         }
 
         private String DATA_(String message)
         {
             message = message.substring(5); //strip off DATA_            
             // grab all but last chunk of data
             int floatArrayCounter = 0;
             for(int i = 0; i < _messageLength - (4+endLineString.length()); i = i + 5)
             {
                 _tempFloatData[floatArrayCounter] = convertByteArrayToFloat(
                         (message.substring(i, i+4)).getBytes());
                  floatArrayCounter++;
 
                  if(message.charAt(i+4)!=',')
                  {
                      errorLog("inputParser: DATA_: comma not found!");
                  }
             }
             // get last chunk of data
             _tempFloatData[floatArrayCounter] = convertByteArrayToFloat(
                     (message.substring(_dataLength-4, _dataLength)).getBytes());
 
             if(!(message.substring(_messageLength-endLineString.length(), _messageLength).equals(endLineString)))
             {
                 errorLog("inputParser: DATA_: "+endLineString+" not found!");
             }
 
             String dataLine = "DATA,";
             for(float item : _tempFloatData)
             {
                 dataLine+=item+",";
             }
             dataLine = dataLine.substring(0,dataLine.length()-1);
             sendDataToModel(dataLine, _tempFloatData);
             
             return message.substring(_messageLength);
         }
 
         private String COMP_(String message)
         {
             String line;
             message = message.substring(5); //strip off COMP_
             int index = message.indexOf(endLineString);
             line = message.substring(0, index);
             message = message.substring(index+endLineString.length());
 
             //_OutputStream.write(line.getBytes());
             //displayLine(line);
             _labelHeaders = line.split(",");
             _tempFloatData = new float[_labelHeaders.length];
             _messageLength = index+endLineString.length();
             _dataLength = _messageLength - endLineString.length();
             sendDataToModel("COMP,"+line, _labelHeaders);            
             
             return message;
         }
         
         private void errorLog(String message)
         {
         	if(errorLog==false)
         	{
         		errorLog = true;
         		errorLogWriter = new FileWriter(new File(Environment.getExternalStorageDirectory()+"/ssdata",_errorLogFileName));
         	}
         	errorLogWriter.writeLine(message);
         }
 	
 	private static float convertByteArrayToFloat(byte[] b)
 	{
 		if (b.length != 4) return 0.0F;
 		
 		DataInputStream dis = null; float f = 0.0F;
 		
 		try
 		{
 		dis = new DataInputStream(new ByteArrayInputStream(b));
 		
 		// Effectively performs:
 		// * readInt call
 		// * Float.intBitsToFloat call
 		f = dis.readFloat();
 		}
 		
 		catch (EOFException e)
 		{
 		// Handle ...
 		}
 		
 		catch (IOException e)
 		{
 		// Handle ...
 		}
 		
 		finally
 		{
 		if (dis != null) { try { dis.close(); } catch (IOException e) {} dis =
 		null; }
 		}
 		
 		return f;
 	}
 	
 }
