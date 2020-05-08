 package com.formicite;
 
 import android.util.Log;
 import java.lang.Runtime;
 import java.lang.Process;
 import java.lang.ProcessBuilder;
 import com.ftdi.D2xx;
 import com.ftdi.D2xx.D2xxException;
 import java.lang.Thread;
 import java.io.OutputStreamWriter;
 import java.io.IOException;
 
 public class SerialInterface extends Thread {
 	
 	private D2xx ftD2xx;
 	private boolean alive;
 	
 	private boolean power;
 	private int motor;
 	private int steering;
 	private int controllerSteering;
 	private int controllerThrottle;
 	private int[] sonar = new int[5];
 	
 	public void run() {
 		alive = true;
 		String packets = "";
 		while (alive) {
 			fixUEventD();
			while (!startSerial()) {
 				Log.i("SerialInterface", "failed to find serial port");
 				try {
 					Thread.sleep(1000L);
 				} catch (Exception ex) {
 					alive = false;
 					break;
 				}
 			}
 			while (alive) {
 				int rxq = 0;
 				int[] devStatus = null;
 				try {
 					devStatus = ftD2xx.getStatus();
 				} catch (Exception ex) {
 					ex.printStackTrace();
 					break;
 				}
 				// Rx Queue status is in first element of the array
 				rxq = devStatus[0];
 				if (rxq > 0) {
 					// read the data back!
 					byte[] InData = new byte[rxq];
 					try {
 						ftD2xx.read(InData,rxq);
 						packets = packets + new String(InData);
 					} catch (Exception ex) {
 						ex.printStackTrace();
 						break;
 					}
 					packets = parsePackets(packets);
 					Log.i("SerialInterface", "Got: \"" + new String(InData) + "\"");
 				} else {
 					writeData("T");
 				}
 				try {
 					Thread.sleep(1L);
 				} catch (Exception ex) {
 					alive = false;
 					break;
 				}
 			}
 			try {
 				ftD2xx.close();
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 			ftD2xx = null;
 		}
 	}
 	
 	private String parsePackets(String packets) {
 		while (packets.indexOf("\n") != -1) {
 			String packet = packets.substring(0, packets.indexOf("\n")).trim();
 			packets = packets.substring(packets.indexOf("\n") + 1);
 			String[] data = packet.split(" ");
 			if (data.length == 0) { //Skip nulls
 				continue;
 			}
 			if (data[0] == "Settings" && data.length > 3) {
 				power = (data[1] == "1");
 				steering = new Integer(data[2]).intValue();
 				motor = new Integer(data[3]).intValue();
 			}
 			if (data[0] == "PWM" && data.length > 3) {
 				int[] newSonar = new int[data.length - 3];
 				//1 and 2 are controller
 				controllerSteering = new Integer(data[1]).intValue();
 				controllerThrottle = new Integer(data[2]).intValue();
 				for (int i = 0; i < newSonar.length; i++) {
 					newSonar[i] = new Integer(data[i + 3]).intValue();
 				}
 				sonar = newSonar;
 			}
 		}
 		return packets;
 	}
 	
 	public int getControllerSteering() {
 		return controllerSteering;
 	}
 	public int getControllerThrottle() {
 		return controllerThrottle;
 	}
 	public int[] getSonar() {
 		return sonar;
 	}
 	public boolean getPower() {
 		return power;
 	}
 	public int getSteering() {
 		return steering;
 	}
 	public int getMotor() {
 		return motor;
 	}
 	
 	public void halt() {
 		alive = false;
 	}
 	
 	public void setMotor(int value) {
 		setServo(value, "M");
 	}
 	
 	public void setSteering(int value) {
 		setServo(value, "S");
 	}
 	
 	public void setPowerDraw(boolean v) {
 		if (v) {
 			writeData("P");
 		} else {
 			writeData("O");
 		}
 	}
 	
 	private void setServo(int value, String name) {
 		if (value < 0) { value = 0; }
 		if (value >= 1000000) { value = 1000000 - 1; }
 		String wr = new Integer(value).toString();
 		while (wr.length() < "100000".length()) {
 			wr = "0" + wr;
 		}
 		writeData(name + wr);
 	}
 	
 	private void writeData(String writeData) {
 		try {
 			byte[] OutData = writeData.getBytes();
 	        ftD2xx.write(OutData, writeData.length());
 		} catch(Exception ex) {}
 	}
 	
 	
 	private boolean startSerial() {
     	// Specify a non-default VID and PID combination to match if required
     	try {
     		D2xx.setVIDPID(0x0403, 0xada1);
 			if (D2xx.createDeviceInfoList() != 1) {
 				return false;
 			}
     	}
     	catch (D2xxException e)
     	{
 			e.printStackTrace();
 			return false;
     	}
 		
        	// create a D2xx object
        	ftD2xx = new D2xx();
             	
          try {
             		
          	// open our first device
 			ftD2xx.openByIndex(0);
 			// configure our port
 			// reset to UART mode for 232 devices
 			ftD2xx.setBitMode((byte)0, D2xx.FT_BITMODE_RESET);
 			// set 115200 baud
 			ftD2xx.setBaudRate(115200);
 			// set 8 data bits, 1 stop bit, no parity
 			ftD2xx.setDataCharacteristics(D2xx.FT_DATA_BITS_8, D2xx.FT_STOP_BITS_1, D2xx.FT_PARITY_NONE);
 			// set no flow control
 			ftD2xx.setFlowControl(D2xx.FT_FLOW_NONE, (byte)0x11, (byte)0x13);
 			// set latency timer to 16ms					
 	        ftD2xx.setLatencyTimer((byte)16);
 			// set a read timeout of 30ms
 	        ftD2xx.setTimeouts(30, 0);
   		    // purge buffers
 	        ftD2xx.purge((byte) (D2xx.FT_PURGE_TX | D2xx.FT_PURGE_RX));
 		} catch(Exception ex) {
 			ftD2xx = null;
 			ex.printStackTrace();
 			return false;
 		}
 		
 		
 		return true;
 	}
 	
 	
 	/**
 	 * The problem here is that editing the ramdisk can brick the phone.
 	 * I don't want to take that risk, so I have code here to reset the
 	 * ramdisk to enable permissions. If that one darn number was 666 instead
 	 * of 660, then it would work. Maybe this is not needed if we can find a 
 	 * way to get the app added to the USB group. This might be possible with
 	 * permission stuff, but for now. Note we only do this if we haven't already.
 	 * This is because excessive killing of ueventd can freak out the phone.
 	 */
 	public void fixUEventD() {
 		//execCommandLine("mount -o rw,remount rootfs /");
 		//execCommandLine("sed -i \"s/*            0660   root       usb/*            0666   root       usb/g\" /ueventd.rc");
 		//execCommandLine("kill `pgrep ueventd`");
 	}
 	
 	
 	/**
 	 * Used for execing command lines (as root!) to fix the serial port
 	 */
 	private void execCommandLine(String command) {
 		Runtime runtime = Runtime.getRuntime();
 		Process proc = null;
 		OutputStreamWriter osw = null;
 		try {
 			proc = new ProcessBuilder()
 				.command("su", "root")
 				.redirectErrorStream(true)
 				.start();
 			osw = new OutputStreamWriter(proc.getOutputStream());
 			osw.write(command);
 			osw.flush();
 			osw.close();
 		} catch (IOException ex) {
 			Log.e("execCommandLine()", "Command resulted in an IO Exception: " + command);
 			ex.printStackTrace();
 			return;
 		} finally {
 			if (osw != null) {
 				try {
 					osw.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		try {
 			int by = 0, n = 0;
 			byte[] buffer = null;
 			while (by != -1) {
 				try {
 					by = proc.getInputStream().read();
 				} catch (IOException ex) {
 					break;
 				}
 				if (by != -1) {
 					n++;
 					byte[] nbuffer = new byte[n];
 					for (int i = 0; i < n - 1; i++) {
 						nbuffer[i] = buffer[i];
 					}
 					nbuffer[n - 1] = (byte)by;
 					buffer = nbuffer;
 				}
 			}
 			Log.i("execCommandLine()", "Output: \"" + new String(buffer) + "\"");
 			proc.waitFor();
 		} catch (InterruptedException e){}
 		if (proc.exitValue() != 0) {
 			Log.e("execCommandLine()", "Command returned error: " + command + "\n  Exit code: " + proc.exitValue());
 		}
 	}
 
 	
 
 }
