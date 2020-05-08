 package org.isoblue.isoblue;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.UUID;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.isoblue.isobus.Bus;
 import org.isoblue.isobus.ISOBUSNetwork;
 
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.util.Log;
 
 public class ISOBlueDevice extends ISOBUSNetwork {
 
 	private static final UUID MY_UUID = UUID
 			.fromString("00000000-0000-0000-0000-00000000abcd");
 	private static final byte[] MY_PIN = { '0', '0', '0', '0' };
 
 	private BluetoothDevice mDevice;
 	private volatile BluetoothSocket mSocket;
 	private ISOBlueBus mEngineBus, mImplementBus;
 	private Thread mReadThread, mWriteThread;
 	private BlockingQueue<ISOBlueCommand> mOutCommands;
 
 	public ISOBlueDevice(BluetoothDevice device) throws IOException {
 		mDevice = device;
 
 		mEngineBus = new ISOBlueBus(this, ISOBlueBus.BusType.ENGINE);
 		mImplementBus = new ISOBlueBus(this, ISOBlueBus.BusType.IMPLEMENT);
 
 		mOutCommands = new LinkedBlockingQueue<ISOBlueCommand>();
 
 		try {
 			device.getClass().getMethod("setPin", byte[].class)
 					.invoke(device, MY_PIN);
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
 		mSocket.connect();
 
 		mReadThread = new ReadThread();
 		mWriteThread = new WriteThread();
 
 		mReadThread.start();
 		mWriteThread.start();
 	}
 
 	private synchronized BluetoothSocket reconnectSocket() {
 		try {
 			mSocket.close();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		mSocket = null;
 		while (mSocket == null) {
 			try {
 				mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
 				mSocket.connect();
 			} catch (IOException e) {
 				mSocket = null;
 
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				android.os.SystemClock.sleep(100);
 			}
 		}
 
 		return mSocket;
 	}
 
 	protected void sendCommand(ISOBlueCommand cmd) throws InterruptedException {
 		mOutCommands.put(cmd);
 
 		Log.d("CMD", cmd.toString());
 	}
 
 	public Bus getEngineBus() {
 		return mEngineBus;
 	}
 
 	public Bus getImplementBus() {
 		return mImplementBus;
 	}
 
 	private class ReadThread extends Thread {
 
 		private BufferedReader mReader;
 
 		private ReadThread() throws IOException {
 			mReader = new BufferedReader(new InputStreamReader(
 					mSocket.getInputStream()));
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Thread#run()
 		 */
 		@Override
 		public void run() {
 
 			while (true) {
 				while (true) {
 					String line;
 					ISOBlueCommand cmd;
 
 					// Receive the command
 					try {
 						line = mReader.readLine();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						break;
 					}
 					Log.d("CMD", line);
 
 					// Parse the command
 					try {
 						cmd = ISOBlueCommand.receiveCommand(line);
 
 						switch (cmd.getBus()) {
 						case 0:
 							mEngineBus.handleCommand(cmd);
 							break;
 
 						case 1:
 							mImplementBus.handleCommand(cmd);
 							break;
 						}
					} catch (IllegalArgumentException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						continue;
 					}
 				}
 
 				synchronized (mSocket) {
 					try {
 						reconnectSocket();
 						mReader = new BufferedReader(new InputStreamReader(
 								mSocket.getInputStream()));
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 
 	private class WriteThread extends Thread {
 
 		private OutputStream mOut;
 
 		private WriteThread() throws IOException {
 			mOut = mSocket.getOutputStream();
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Thread#run()
 		 */
 		@Override
 		public void run() {
 			ISOBlueCommand cmd;
 
 			while (true) {
 				while (true) {
 					try {
 						cmd = mOutCommands.take();
 
 						cmd.sendCommand(mOut);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						break;
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						break;
 					} catch (NullPointerException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						break;
 					}
 				}
 
 				synchronized (mSocket) {
 					try {
 						mOut = mSocket.getOutputStream();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @return the mDevice
 	 */
 	public BluetoothDevice getDevice() {
 		return mDevice;
 	}
 }
