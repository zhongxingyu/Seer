 package com.example.bluetooth;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.util.UUID;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.os.AsyncTask;
 
 import com.example.solarpanelmanager.api.responses.BaseResponse;
 
 /**
  * @author seanhurley
  * 
  *         This is the base communication class which will manage the
  *         communication with the actual bluetooth device. This way, children
  *         only need to implement a little bit of the functionality to tell the
  *         bluetooth device which request it's sending in order to get all of
  *         the functionality.
  */
 public abstract class CommunicationHandler<T extends BaseResponse> {
 	private Callback<T> responseCallback;
 	private String address;
 	private ServiceASyncTask task;
 
 	/**
 	 * @return A JSON formatted string which will tell the bluetooth device
 	 *         which type of request this is.
 	 */
 	abstract protected String getRequest();
 
 	abstract protected T parseResponse(String data);
 
 	public CommunicationHandler(Callback<T> callback, String target) {
 		this.address = target;
 		this.responseCallback = callback;
 	}
 
 	/**
 	 * Will actually start the asynchronous communication and will call the
 	 * callback when it is finished.
 	 */
 	public void performAction() {
 		task = new ServiceASyncTask();
 		task.execute();
 	}
 
 	public void waitOnTask(long millis) {
 		try {
 			task.get(millis, TimeUnit.MILLISECONDS);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		} catch (TimeoutException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @author seanhurley This will actually serve as the asynchronous method by
 	 *         which we communicate with the device.
 	 */
 	private class ServiceASyncTask extends AsyncTask<Void, Void, T> {
 
 		private BluetoothAdapter mBluetoothAdapter;
 
 		public ServiceASyncTask() {
 			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		}
 
 		@Override
 		protected T doInBackground(Void... args) {
 
 			// TODO Fix the newline ending?
 			String request = getRequest() + "\n";
 			BluetoothSocket clientSocket = null;
 
 			if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
 				// Bluetooth is not enabled
 				return null;
 			}
 
 			// TODO Client knows the MAC address of server
 			// This should be passed along from the app to give them the which
 			// device address to talk to
 			System.out.println("checkpoint 1");
 			BluetoothDevice mmDevice = mBluetoothAdapter.getRemoteDevice(address);
 			System.out.println("checkpoint 2");
 			String data = "";
 			try {
 
 				// The app's UUID string, also used by the server code
 				clientSocket = mmDevice.createRfcommSocketToServiceRecord(UUID
 						.fromString("00001101-0000-1000-8000-00805F9B34FB"));
 
 				mBluetoothAdapter.cancelDiscovery(); // Cancel, discovery slows
 														// connection
 				System.out.println("but does get to here");
 				clientSocket.connect();
 				System.out.println("did get this far");
 				DataInputStream in = new DataInputStream(clientSocket.getInputStream());
 				DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
 
 				out.write(request.getBytes());
 				out.flush();
 
 				byte b;
 				while (((b = in.readByte()) > 0) && (b != 0x0a)) {
 					data += (char) b;
 				}
 
 				in.close();
 				out.close();
 				clientSocket.close();
 			} catch (Exception e) {
 			}
			
			return parseResponse(data);
 		}
 
 		@Override
 		protected void onPostExecute(T result) {
 			responseCallback.onComplete(result);
 		}
 
 	}
 
 }
