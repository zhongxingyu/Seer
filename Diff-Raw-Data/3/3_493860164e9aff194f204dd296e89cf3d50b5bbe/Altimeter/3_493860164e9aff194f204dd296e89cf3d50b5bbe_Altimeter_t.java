 /*
     openaltimeter -- an open-source altimeter for RC aircraft
     Copyright (C) 2010  Jony Hudson
     http://openaltimeter.org
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.openaltimeter;
 
 import gnu.io.NoSuchPortException;
 import gnu.io.PortInUseException;
 import gnu.io.UnsupportedCommOperationException;
 
 import java.io.IOException;
 import java.util.TooManyListenersException;
 
 import org.openaltimeter.comms.SerialLink;
 import org.openaltimeter.data.FlightLog;
 import org.openaltimeter.data.LogEntry;
 import org.openaltimeter.data.LogEntry.DataFormat;
 import org.openaltimeter.desktopapp.Controller;
 import org.openaltimeter.settings.Settings;
 import org.openaltimeter.settings.Settings.SettingsFormat;
 
 public class Altimeter {
 
	// this timeout needs to be long enough for the longest download to finish.
	private static final int TIMEOUT_LOOP_LIMIT = 1200;
 	public static final int FLASH_MEMORY_SIZE = 512 * 1024;
 	// TODO: this is a fudge, 12 bytes for BETA, 5 bytes for V1
 	public static final int DATASTORE_LOG_ENTRY_SIZE = 5;
 	private static final int SETTINGS_MEMORY_SIZE = 512;	
 
 	private SerialLink serial;
 	public DataFormat dataFormat = DataFormat.V1_FORMAT;
 	public SettingsFormat settingsFormat = SettingsFormat.V2_FORMAT;
 	public Settings settings;
 	public String firmwareVersion;
 	
 	public Altimeter() {
 		serial = new SerialLink();
 	}
 
 	public String connect(String comPortName, int baudRate) throws NoSuchPortException,
 			PortInUseException, UnsupportedCommOperationException, IOException, NotAnOpenaltimeterException, TooManyListenersException {
 		// we reset the logger after opening the serial port, and then wait for
 		// the welcome message
 		serial.connect(comPortName, baudRate);
 		reset();
 		// wait for the logger to start up
 		try {Thread.sleep(16000);} catch (Exception e) {};
 		// read the welcome message, check that the altimeter has responded
 		// send a command to stop logging.
 		serial.write('c');
 		try {Thread.sleep(500);} catch (Exception e) {};
 		String welcomeString = serial.readString(2048);
 		if (!welcomeString.startsWith("openaltimeter")) {
 			serial.disconnect();
 			throw new NotAnOpenaltimeterException();
 		}
 		
 		// parse the welcome message to get the OA firmware version
 		String firstLine = welcomeString.split("\n")[0];
 		firmwareVersion = firstLine.split(": ")[1];
 		firmwareVersion = firmwareVersion.substring(0, firmwareVersion.length() - 1);
 		System.out.println("Logger version : " + firmwareVersion);
 		
 		// return the welcome message
 		return welcomeString;
 	}
 	
 	public String getFileInfo() throws IOException
 	{
 		serial.clearInput();
 		serial.write('i');
 		try {Thread.sleep(500);} catch (Exception e) {};
 		String fileInfoString = serial.readString(2048);	
 		
 		return  fileInfoString.trim();
 	}
 
 	public void disconnect() {
 		serial.disconnect();
 	}
 
 	public void reset() {
 		serial.sendReset();
 	}
 	
 	public FlightLog downloadData() throws IOException, DownloadTimeoutException {
 		// we first find out how many entries we're expecting
 		String fileInfo = getFileInfo();
 		String[] infoStrings = fileInfo.split("\n");
 		String numberEntriesString = (infoStrings[1].split(":"))[1].trim();
 		int numberOfEntries = Integer.parseInt(numberEntriesString);
 		// flush the input buffer and tell the logger to commence the upload
 		serial.clearInput();
 		// make sure the input stream is clear
 		while (serial.in.available() != 0) serial.clearInput();
 		// tell the logger to upload its data
 		serial.startBufferedRead(FLASH_MEMORY_SIZE * 2);
 		serial.write('d');
 		// loop, checking on the progress of the data upload, timeout if necessary
 		int timeoutCounter = 0;
 		while (serial.available() < (numberOfEntries + 2) * DATASTORE_LOG_ENTRY_SIZE ) {
 			int percentage = (int)((double)(100 * serial.available()) / (double)(numberOfEntries * DATASTORE_LOG_ENTRY_SIZE));
 			Controller.setProgress(percentage);
 			try { Thread.sleep(100); } catch (InterruptedException e) {}
 			if (timeoutCounter++ == TIMEOUT_LOOP_LIMIT) throw new DownloadTimeoutException();
 		}
 		serial.stopBufferedRead();
 		Controller.setProgress(100);
 		// there are numberOfEntries log entries, plus two file end markers.
 		FlightLog log = new FlightLog();
 		byte[] data = serial.getBuffer();
 		for (int i = 0; i < numberOfEntries + 2; i++)
 		{
 			int os = i *DATASTORE_LOG_ENTRY_SIZE;
 			LogEntry le = LogEntry.logEntryFromBytes(data, os, dataFormat);
 			log.add(le);
 		}
 		log.calculateAltitudes();
 		return log;
 	}
 	
 	public String erase() throws IOException {
 		serial.clearInput();
 		serial.write('e');
 		try {Thread.sleep(5000);} catch (Exception e) {};
 		String response = serial.readString(2048);	
 		
 		return  response.trim();
 	}
 	
 	public void readSettings() throws IOException {
 		serial.clearInput();
 		// make sure the input stream is clear
 		while (serial.in.available() != 0) serial.clearInput();
 		// tell the logger to upload its data
 		serial.startBufferedRead(SETTINGS_MEMORY_SIZE);
 		serial.write('r');
 		// cheesily just use a short delay
 		try {Thread.sleep(500);} catch (Exception e) {};
 		serial.stopBufferedRead();
 		settings = new Settings(serial.getBuffer(), settingsFormat);
 	}
 
 	public void writeSettings() throws IOException
 	{
 		serial.clearInput();
 		serial.write('s');
 		byte[] settingsBytes = settings.toByteArray();
 		for (int i = 0; i < settingsBytes.length; i++) {
 			serial.write((char)settingsBytes[i]);
 			try {Thread.sleep(10);} catch (InterruptedException e) {}			
 		}
 		try {Thread.sleep(4000);} catch (InterruptedException e) {}
 	}
 
 
 	public void wipeSettings() throws IOException {
 		serial.clearInput();
 		serial.write('w');
 		try {Thread.sleep(4000);} catch (Exception e) {};		
 	}
 	
 	@SuppressWarnings("serial")
 	public class DownloadTimeoutException extends Exception {
 	}
 	
 	@SuppressWarnings("serial")
 	public class NotAnOpenaltimeterException extends Exception {
 	}
 
 
 }
