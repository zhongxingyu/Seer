 /*
     openaltimeter -- an open-source altimeter for RC aircraft
     Copyright (C) 2010  Jony Hudson, Jan Steidl
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
 package org.openaltimeter.desktopapp;
 
 import java.awt.Dialog.ModalityType;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.JDialog;
 import javax.swing.SwingUtilities;
 
 import org.openaltimeter.Altimeter;
 import org.openaltimeter.Altimeter.DownloadTimeoutException;
 import org.openaltimeter.Altimeter.NotAnOpenaltimeterException;
 import org.openaltimeter.comms.SerialLink;
 import org.openaltimeter.data.FlightLog;
 import org.openaltimeter.desktopapp.MainWindow.DataState;
 import org.openaltimeter.settings.Settings;
 
 import flexjson.JSONDeserializer;
 import flexjson.JSONSerializer;
 
 
 public class Controller {
 	
 	public enum ConnectionState {CONNECTED, DISCONNECTED, BUSY}
 	private ConnectionState connectionState;
 	
 	public enum OS { WINDOWS, OTHER };
 	public OS os;
 	
 	private static final double LOG_INTERVAL = 0.5;
 	static Controller controller;
 	Altimeter altimeter;
 	MainWindow window;
 	FlightLog flightLog;
 	private boolean unitFeet;
 
 	public void setUnitFeet(boolean unitFeet) {
 		this.unitFeet = unitFeet;
 	}
 
 	public boolean isUnitFeet() {
 		return unitFeet;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			Controller c = new Controller();
 			controller = c;
 			c.run();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static Controller getController() {
 		return controller;
 	}
 
 	// this is the application's main method
 	private void run() {
 		// determine what os we're running, as some features are currently os specific
 		if (System.getProperty("os.name").startsWith("Windows")) os = OS.WINDOWS;
 		else os = OS.OTHER;
 		window = new MainWindow();
 		window.controller = this;
 		window.initialise();
 		altimeter = new Altimeter();
 		window.show();
 		buildSerialMenu();
 	}
 	
 	private void setConnectionState(ConnectionState state) {
 		connectionState = state;
 		window.setConnectedState(state);
 	}
 
 	// this is for initialisation code that runs before the GUI is built
 	private void buildSerialMenu() {
 		// build the serial port selection menu
 		Controller.log("Finding serial ports ...", "message");
 		List<String> serialPorts = SerialLink.getSerialPorts();
 		Iterator<String> it = serialPorts.iterator();
 		while (it.hasNext())
 			window.addCOMMenuItem(it.next());
 		window.selectFirstCOMItem();
 		Controller.log("Done.", "message");
 	}
 
 	// open the serial port and connect to the logger, print some summary information from the altimeter
 	public void connect() {
 		setConnectionState(ConnectionState.BUSY);
 		new Thread( new Runnable() {
 			public void run() {
 				try {
 					String comPort = window.getSelectedCOMPort();
 					Controller.log("Connecting to serial port " + comPort + " (please wait) ...", "message");
 					Controller.log(altimeter.connect(comPort), "altimeter");
 					Controller.log("Connected.", "message");
 					setConnectionState(ConnectionState.CONNECTED);
 				} catch (NotAnOpenaltimeterException e) {
 					Controller.log("Incorrect reply from device. Check that you've selected the correct serial port, and that " +
 							"the openaltimeter is connected and powered.", "error");
 					setConnectionState(ConnectionState.DISCONNECTED);
 				} catch (Exception e) {
 					Controller.log("Exception opening serial port. Check your serial port settings.", "error");
 					setConnectionState(ConnectionState.DISCONNECTED);
 				}
 				try {
 					Controller.log("Getting log information ...", "message");
 					Controller.log(altimeter.getFileInfo(), "altimeter");
 					Controller.log("Done.", "message");
 				} catch (IOException e) {
 					Controller.log("Unable to get file information from altimeter.", "error");
 				}
 			}
 		}).start();
 	}
 
 	// close the serial port
 	public void disconnect() {
 		if (connectionState != ConnectionState.DISCONNECTED) {
 			altimeter.disconnect();
 			Controller.log("Disconnected.", "message");
 			setConnectionState(ConnectionState.DISCONNECTED);
 		}
 	}
 
 	public void downloadData() {
 		setConnectionState(ConnectionState.BUSY);
 			new Thread( new Runnable() {
 			public void run() {
 				Controller.log("Downloading altimeter data (please wait) ...", "message");
 				try {
 					setFlightLog(altimeter.downloadData());
 				} catch (IOException e) {
 					Controller.log("Communications error while downloading data. Check the serial port.", "error");
 				} catch (DownloadTimeoutException e) {
 					Controller.log("Download started but did not complete in time. Check the serial port.", "error");
 				} finally {
 					window.setDataState(DataState.HAVE_DATA);
 					setConnectionState(ConnectionState.CONNECTED);
 					Controller.log("Done.", "message");
 				}
 			}
 		}).start();
 	}
 
 	public void erase() {
 		if (window.showConfirmDialog("Are you sure you want to erase the altimeter's memory?", "Erase ..."))
 		{
 			setConnectionState(ConnectionState.BUSY);
 			new Thread( new Runnable() {
 				public void run() {
 					Controller.log("Erasing altimeter (please wait) ...", "message");
 					try {
 						log(altimeter.erase(), "altimeter");
 					} catch (IOException e) {
 						Controller.log("Error sending erase command. Check the serial port.", "error");
 					} finally {
 						setConnectionState(ConnectionState.CONNECTED);
 						Controller.log("Done.", "message");
 					}
 				}
 			}).start();
 		}
 	}
 	
 	public void setFlightLog(FlightLog log)
 	{
 		flightLog = log;
 		
 		if (isUnitFeet())
 			window.setAltitudeData(log.getAltitudeFt(), LOG_INTERVAL);
 		else
 			window.setAltitudeData(log.getAltitudeM(), LOG_INTERVAL);
 		
 		window.setBatteryData(log.getBattery(), LOG_INTERVAL);
 		window.setTemperatureData(log.getTemperature(), LOG_INTERVAL);
 		window.setServoData(log.getServo(), LOG_INTERVAL);
 	}
 	
 	public void saveRaw() {
 		File f = window.showSaveDialog();
 		if (f == null) return;
 		try {
 			FileWriter fw = new FileWriter(f);
 			fw.write(flightLog.rawDataToString());
 			fw.close();
 		} catch (IOException e) {
 			window.log("Error writing file. Please check the filename and try again.", "error");
 		}
 	}
 	
 	public void saveRawSelection(double lower, double upper) {
 		System.out.println("Lower: " + lower + " Upper: " + upper);
 		File f = window.showSaveDialog();
 		if (f == null) return;
 		try {
 			FileWriter fw = new FileWriter(f);
 			fw.write(flightLog.rawDataToString((int) (lower / LOG_INTERVAL), (int) (upper / LOG_INTERVAL)));
 			fw.close();
 		} catch (IOException e) {
 			window.log("Error writing file. Please check the filename and try again.", "error");
 		}		
 	}
 	
 	public void saveProcessed() {
 		File f = window.showSaveDialog();
 		if (f == null) return;
 		try {
 			FileWriter fw = new FileWriter(f);
 			JSONSerializer js = new JSONSerializer();
 			String json = js
 							.include("logData", "annotations")
 							.prettyPrint(true)
 							.serialize(flightLog);
 			fw.write(json);
 			fw.close();
 		} catch (IOException e) {
 			window.log("Error writing file. Please check the filename and try again.", "error");
 		}
 	}
 	
 	public void loadRawData() {
 		File f = window.showOpenDialog();
 		if (f == null) return;
 		StringBuilder fileStringBuilder = new StringBuilder();
 		try {
 			BufferedReader fr = new BufferedReader(new FileReader(f));
 			String line;
 			while ((line = fr.readLine()) != null) {
 				fileStringBuilder.append(line);
 				fileStringBuilder.append("\n");
 			}
 		} catch (IOException e) {
 			log("Unable to open file. Please check file is not open elsewhere and try again.", "error");
 		}
 		FlightLog fl = new FlightLog();
 		try {
 			fl.fromRawData(fileStringBuilder.toString());
 		} catch (IOException e) {
 			log("Unable to parse file. Are you sure that this is a raw data file?", "error");
 		}
 		setFlightLog(fl);
 		window.setDataState(DataState.HAVE_DATA);
 	}
 
 	public void loadProcessedData() {
 		File f = window.showOpenDialog();
 		if (f == null) return;
 		StringBuilder fileStringBuilder = new StringBuilder();
 		try {
 			BufferedReader fr = new BufferedReader(new FileReader(f));
 			String line;
 			while ((line = fr.readLine()) != null) {
 				fileStringBuilder.append(line);
 				fileStringBuilder.append("\n");
 			}
 		} catch (IOException e) {
 			log("Unable to open file. Please check file is not open elsewhere and try again.", "error");
 		}
 		FlightLog fl = new JSONDeserializer<FlightLog>()
 							.use("logData", ArrayList.class)
 	//						.use("logData.values", LogEntry.class)
 							.use("annotations",  ArrayList.class)
 	//						.use("annotations.values", Annotation.class)
 							.deserialize(fileStringBuilder.toString());
 		setFlightLog(fl);
 		window.setDataState(DataState.HAVE_DATA);
 	}
 
 	public void exit() {
 		window.close();
 		System.exit(0);
 	}
 	
 	public static void log(String message, String style)
 	{
 		Controller.getController().window.log(message, style);
 	}
 	
 	public static void setProgress(int progress)
 	{
 		Controller.getController().window.updateProgress(progress);
 	}
 
 	public void altitudePlotSelectedChange(boolean selected) {
 		window.setAltitudePlotVisible(selected);
 	}
 
 	public void voltagePlotSelectedChange(boolean selected) {
 		window.setVoltagePlotVisible(selected);
 	}
 
 	public void temperaturePlotSelectedChange(boolean selected) {
 		window.setTemperaturePlotVisible(selected);		
 	}
 
 	public void servoPlotSelectedChange(boolean selected) {
 		window.setServoPlotVisible(selected);		
 	}
 
 	public void unitSelectedChange(boolean selected) {
 		this.setUnitFeet(selected);
 		window.setPlotUnit(selected);
 		
 		if (flightLog != null) {
 			if (isUnitFeet()) 
 				window.setAltitudeData(flightLog.getAltitudeFt(), LOG_INTERVAL);
 			else
 				window.setAltitudeData(flightLog.getAltitudeM(), LOG_INTERVAL);
 		}
 	}
 
 	// this is called by the settings menu event handler
 	SettingsDialog settingsDialog;
 	public void runSettingsInterface() {
 		settingsDialog = new SettingsDialog(this);
 		new Thread(new Runnable() {
 			public void run() {
 				loadSettingsFromAltimeter();
 				try {
 					SwingUtilities.invokeAndWait(new Runnable() {
 						public void run() {
 							settingsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 							settingsDialog.setModalityType(ModalityType.APPLICATION_MODAL);
 							settingsDialog.setVisible(true);
 						}});
 				} catch (Exception e) {
 					e.printStackTrace();
 			}
 		}}).start();
 	}
 	
 	// a helper method to load the settings from the OA and update the settings dialog
 	private void loadSettingsFromAltimeter()
 	{
 		try {
 			log("Loading settings from altimeter ...", "message");
 			altimeter.readSettings();
 			settingsDialog.setSettings(altimeter.settings);
 			log("Done.", "message");
 		} catch (IOException e) {
 			log("Unable to read settings from altimeter.", "error");
 		}
 	}
 	
 	//this is called by the settings dialog
 	void saveSettingsToAltimeter()
 	{
 		settingsDialog.enableButtons(false);
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					log("Writing settings to altimeter ...", "message");
 					Settings settingsToWrite = settingsDialog.getSettings();
 					altimeter.settings = settingsToWrite;
 					altimeter.writeSettings();
 					log("Done.", "message");
 					log("Rebooting altimeter with new settings ...", "message");
 					altimeter.disconnect();
 					String comPort = window.getSelectedCOMPort();
 					Controller.log("Connecting to serial port " + comPort + " (please wait) ...", "message");
 					Controller.log(altimeter.connect(comPort), "altimeter");
 					Controller.log("Reboot finished.", "message");
 					Controller.log("Verifying settings ...", "message");
 					loadSettingsFromAltimeter();
 					if (!settingsToWrite.equals(altimeter.settings)) {
 						log("Error verifying altimeter settings. Please try saving again.", "error");
 						settingsDialog.enableButtons(true);
 						return;
 					}
 				} catch (IOException e) {
 					log("Unable to write settings to altimeter.", "error");
 					e.printStackTrace();
 					disconnect();
 				} catch (NotAnOpenaltimeterException e) {
 					log("Incorrect reply from device. Check that you've selected the correct serial port, and that " +
 							"the openaltimeter is connected and powered.", "error");
 					e.printStackTrace();
 					disconnect();
 				} catch (Exception e) {
 					log("Exception opening serial port. Check your serial port settings.", "error");
 					e.printStackTrace();
 					disconnect();
 				} finally {
 					settingsDialog.enableButtons(true);
 					settingsDialog.dispose();
 				}
 			}}).start();
 	}
 
 	// flashes the firmware to a stable version, wipes the settings - putting them back
 	// to default - and erases the log memory. This is supposed to be a "sure-fire" way
 	// of getting the OA back into a workable state. It doesn't depend on there already
 	// being working OA software on the board, only the arduino compatible bootloader.
 	FirmwareDialog firmwareDialog;
 	public void flashFirmware() {
 		firmwareDialog = new FirmwareDialog(this);
 		firmwareDialog.setVisible(true);
 	}
 
 	public void doFirmwareFlash() {
 		firmwareDialog.enableButtons(false);
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					if (connectionState == ConnectionState.CONNECTED) {
 						altimeter.disconnect();
 						setConnectionState(ConnectionState.BUSY);
 					}
 					log("Flashing altimeter firmware ...", "message");
 					doFirmwareUpload();
 					log("Done.", "message");
 					log("Rebooting altimeter ...", "message");
 					String comPort = window.getSelectedCOMPort();
 					log("Connecting to serial port " + comPort + " (please wait) ...", "message");
 					log(altimeter.connect(comPort), "altimeter");
 					log("Reboot finished.", "message");
 					log("Erasing altimeter (please wait) ...", "message");
 					log(altimeter.erase(), "altimeter");
 					log("Wiping settings memory ...", "message");
 					altimeter.wipeSettings();
 					log("Firmware upgrade done - reconnecting ...", "message");
 					altimeter.disconnect();
 					log("Connecting to serial port " + comPort + " (please wait) ...", "message");
 					log(altimeter.connect(comPort), "altimeter");
 					log("Connected.", "message");
 					setConnectionState(ConnectionState.CONNECTED);
 				} catch (FirmwareFlashException e) {
 					e.printStackTrace();
 					log("Error flashing firmware.", "error");
 					setConnectionState(ConnectionState.DISCONNECTED);
 					return;
 				} catch (IOException e) {
 					e.printStackTrace();
 					log("Error communicating with altimeter.", "error");
 					altimeter.disconnect();
 					setConnectionState(ConnectionState.DISCONNECTED);
 					return;
 				} catch (NotAnOpenaltimeterException e) {
 					e.printStackTrace();
 					log("Incorrect reply from device. Check that you've selected the correct serial port, and that " +
 							"the openaltimeter is connected and powered.", "error");
 					altimeter.disconnect();
 					setConnectionState(ConnectionState.DISCONNECTED);
 					return;
 				} catch (Exception e) {
 					e.printStackTrace();
 					log("Exception communicating with altimeter. Check your serial port settings.", "error");
 					altimeter.disconnect();
 					setConnectionState(ConnectionState.DISCONNECTED);
 					return;
 				} finally {
 					firmwareDialog.enableButtons(true);
 					firmwareDialog.dispose();
 				}
 
 				runSettingsInterface();
 			}}).start();
 	}
 	
 	private void doFirmwareUpload() throws FirmwareFlashException {
 		int exitCode;
 		try {
 			ProcessBuilder pb = new ProcessBuilder();
 			// this would be where you'd switch based on OS to run the appropriate
 			// firmware upload command.
 			Vector<String> commandLine = new Vector<String>();
 			pb.directory(new File("windows_flash"));
 			commandLine.add("windows_flash/avrdude.exe");
 			commandLine.add("-Cavrdude.conf");
 			commandLine.add("-q");
 			commandLine.add("-patmega328p");
 			commandLine.add("-cstk500v1");
 			commandLine.add("-P" + window.getSelectedCOMPort());
 			commandLine.add("-b57600");
 			commandLine.add("-D");
 			commandLine.add("-Uflash:w:firmware_v2.hex:i");
 			pb.command(commandLine);
 
 			Process p = pb.start();
 
 			StreamLogPump errorPump = new StreamLogPump(p.getErrorStream(), "error");
 			StreamLogPump outputPump = new StreamLogPump(p.getInputStream(), "altimeter");
 			errorPump.start();
 			outputPump.start();
 	
 			exitCode = p.waitFor();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new FirmwareFlashException();
 		}
 		if (exitCode != 0) {
 			log("avrdude failed with exit code: " + exitCode, "error");
 			throw new FirmwareFlashException();
 		}
 	}
 	
 	class StreamLogPump extends Thread {
 		InputStream stream;
 		String type;
 
 		StreamLogPump(InputStream stream, String type) {
 			this.stream = stream;
 			this.type = type;
 		}
 
 		public void run() {
 			try {
 				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
 				String line;
 				while ((line = reader.readLine()) != null) Controller.log(line, type);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@SuppressWarnings("serial")
 	class FirmwareFlashException extends Exception {}
 }
 
