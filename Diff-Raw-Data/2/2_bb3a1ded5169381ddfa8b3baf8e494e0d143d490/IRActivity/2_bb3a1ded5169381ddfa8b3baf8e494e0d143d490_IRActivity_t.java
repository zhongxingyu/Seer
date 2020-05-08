 package com.drew.myirremote;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.SparseArray;
 import android.view.KeyEvent;
 import android.view.View;
 
 public class IRActivity extends Activity {
 	private SparseArray<String> codes;
 	private Object irService;
 	private Method irWrite;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_ir);
 
 		// Set up the code array
 		codes = new SparseArray<String>();
 
 		// Store each code in the array with the corresponding button's id as
 		// the key
 		codes.put(
 				R.id.irRemoteTvPower,
 				"0000 006d 0022 0003 00a9 00a8 0015 003f 0015 003f 0015 003f "
 						+ "0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 003f "
 						+ "0015 003f 0015 003f 0015 0015 0015 0015 0015 0015 0015 0015 "
 						+ "0015 0015 0015 0015 0015 003f 0015 0015 0015 0015 0015 0015 "
 						+ "0015 0015 0015 0015 0015 0015 0015 0040 0015 0015 0015 003f "
 						+ "0015 003f 0015 003f 0015 003f 0015 003f 0015 003f 0015 0702 "
 						+ "00a9 00a8 0015 0015 0015 0e6e");
 		codes.put(
 				R.id.irRemoteXboxPower,
 				"0000 0073 0000 0022 0060 0020 0010 0010 0010 0010 0010 0020 "
 						+ "0010 0020 0030 0020 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0020 0020 0020 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0010 0020 0010 0010 0020 0010 0010 0010 09AC");
 
 		codes.put(R.id.irRemoteTvSource,
 				"0000 006c 0022 0003 00ab 00aa 0015 003f 0015 003f "
 						+ "0015 003f 0015 0015 0015 0015 0015 0015 0015 0015 "
 						+ "0015 0015 0015 003f 0015 003f 0015 003f 0015 0015 "
 						+ "0015 0015 0015 0015 0015 0015 0015 0015 0015 0040 "
 						+ "0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 "
 						+ "0015 0015 0015 0015 0015 0015 0015 003f 0015 003f "
 						+ "0015 003f 0015 003f 0015 0040 0015 003f 0015 003f "
 						+ "0015 0713 00ab 00aa 0015 0015 0015 0e91");
 		codes.put(R.id.irRemoteTvEnter,
 				"0000 006c 0022 0003 00ab 00aa 0015 003f 0015 003f "
 						+ "0015 003f 0015 0015 0015 0015 0015 0015 0015 0015 "
 						+ "0015 0015 0015 003f 0015 0040 0015 003f 0015 0015 "
 						+ "0015 0015 0015 0015 0015 0015 0015 0015 0015 0040 "
 						+ "0015 003f 0015 0015 0015 0015 0015 0015 0015 003f "
 						+ "0015 0015 0015 0015 0015 0015 0015 0015 0015 003f "
 						+ "0015 003f 0015 003f 0015 0015 0015 003f 0015 003f "
 						+ "0015 0712 00ab 00aa 0015 0015 0015 0e91");
 
 		codes.put(
 				R.id.irRemoteXboxPlay,
 				"0000 0073 0000 0020 0060 0020 0010 0010 0010 0010 0010 0020 "
 						+ "0010 0020 0030 0020 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 "
 						+ "0010 0020 0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0020 0020 0010 0010 0020 0010 09AC");
 		codes.put(
 				R.id.irRemoteXboxPause,
 				"0000 0073 0000 0021 0060 0020 0010 0010 0010 0010 0010 0020 "
 						+ "0010 0020 0030 0020 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 "
 						+ "0010 0020 0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0010 0010 0020 0010 0010 0010 0010 0010 09AC");
 
 		codes.put(
 				R.id.irRemoteXboxA,
 				"0000 0073 0000 0020 0060 0020 0010 0010 0010 0010 0010 0020 "
 						+ "0010 0020 0030 0020 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 "
 						+ "0010 0020 0020 0020 0010 0010 0010 0010 0020 0010 0010 0020 "
 						+ "0010 0010 0020 0010 0010 0020 0010 09AC");
 		codes.put(
 				R.id.irRemoteXboxX,
 				"0000 0073 0000 0020 0060 0020 0010 0010 0010 0010 0010 0020 "
 						+ "0010 0020 0030 0020 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 "
 						+ "0010 0020 0020 0020 0010 0010 0010 0010 0020 0010 0010 0020 "
 						+ "0020 0020 0010 0010 0010 0010 0010 09AC");
 		codes.put(
 				R.id.irRemoteXboxB,
 				"0000 0073 0000 001F 0060 0020 0010 0010 0010 0010 0010 0020 "
 						+ "0010 0020 0030 0020 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 "
 						+ "0010 0020 0020 0020 0010 0010 0010 0010 0010 0010 0020 0020 "
 						+ "0010 0010 0020 0020 0020 09BC");
 		codes.put(
 				R.id.irRemoteXboxY,
 				"0000 0073 0000 0020 0060 0020 0010 0010 0010 0010 0010 0020 "
 						+ "0010 0020 0030 0020 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 "
 						+ "0010 0020 0020 0020 0010 0010 0010 0010 0010 0010 0020 0020 "
 						+ "0010 0010 0020 0010 0010 0020 0010 09AC");
 
 		codes.put(
 				R.id.irRemoteXboxUp,
 				"0000 0073 0000 0022 0060 0020 0010 0010 0010 0010 0010 0020 "
 						+ "0010 0020 0030 0020 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0020 0020 0020 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0020 0010 0010 0010 0010 0010 0010 0020 0010 09AC");
 
 		codes.put(
 				R.id.irRemoteXboxLeft,
 				"0000 0073 0000 0022 0060 0020 0010 0010 0010 0010 0010 0020 "
 						+ "0010 0020 0030 0020 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0020 0020 0020 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 09AC");
 		codes.put(
 				R.id.irRemoteXboxXbox,
 				"0000 0073 0000 0020 0060 0020 0010 0010 0010 0010 0010 0020 "
 						+ "0010 0020 0030 0020 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 "
 						+ "0010 0020 0020 0020 0010 0010 0010 0010 0020 0010 0010 0020 "
 						+ "0010 0010 0020 0020 0010 0010 0010 09AC");
 		codes.put(
 				R.id.irRemoteXboxRight,
 				"0000 0073 0000 0021 0060 0020 0010 0010 0010 0010 0010 0020 "
 						+ "0010 0020 0030 0020 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0020 0020 0020 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0020 0010 0010 0010 0010 0010 0010 0020 09BC");
 
 		codes.put(
 				R.id.irRemoteXboxDown,
 				"0000 0073 0000 0022 0060 0020 0010 0010 0010 0010 0010 0020 "
 						+ "0010 0020 0030 0020 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0010 0020 0020 0020 0010 0010 0010 0010 0010 0010 "
 						+ "0010 0010 0020 0010 0010 0010 0010 0010 0010 0010 0010 09BC");
 
 		// Get the irda system service
 		irService = this.getSystemService("irda");
 		// Get the class of the server
 		Class c = irService.getClass();
 		// Get the string class
 		Class p[] = { String.class };
 
 		// Attempt to get the IR write function
 		try {
 			// Get the method from the service
 			irWrite = c.getMethod("write_irsend", p);
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public boolean dispatchKeyEvent(KeyEvent event) {
 		if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
 				&& event.getAction() == KeyEvent.ACTION_UP) {
 			// Get the code for volume up
 			String data = "0000 006d 0022 0003 00a9 00a8 0015 003f 0015 003f 0015 003f "
 					+ "0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 003f 0015 "
 					+ "003f 0015 003f 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 "
 					+ "0015 003f 0015 003f 0015 003f 0015 0015 0015 0015 0015 0015 0015 "
 					+ "0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 003f 0015 003f "
 					+ "0015 003f 0015 003f 0015 003f 0015 0702 00a9 00a8 0015 0015 0015 0e6e";
 
 			// Send the command
 			irSend(data);
 
 			// Consumed the event
 			return true;
 		} else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
 				&& event.getAction() == KeyEvent.ACTION_UP) {
 			// Get the code for volume down
 			String data = "0000 006d 0022 0003 00a9 00a8 0015 003f 0015 003f 0015 003f "
 					+ "0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 003f 0015 "
 					+ "003f 0015 003f 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 "
 					+ "0015 003f 0015 003f 0015 0015 0015 003f 0015 0015 0015 0015 0015 "
 					+ "0015 0015 0015 0015 0015 0015 0015 0015 003f 0015 0015 0015 003f "
 					+ "0015 003f 0015 003f 0015 003f 0015 0702 00a9 00a8 0015 0015 0015 0e6e";
 
 			// Send the data
 			irSend(data);
 
 			// Consumed the event
 			return true;
 		} else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
 				&& event.getAction() == KeyEvent.ACTION_DOWN) {
 			// Consume the down event
 			return true;
 		} else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
 				&& event.getAction() == KeyEvent.ACTION_DOWN) {
 			// Consume the up event
 			return true;
 		}
 
 		// Did not consume the event
		return super.dispatchKeyEvent(event);
 	}
 
 	/**
 	 * Send the IR command using the port
 	 * 
 	 * @param view
 	 *            The view calling the function (button)
 	 */
 	public void irSend(View view) {
 		// Get the code from the array
 		String data = codes.get(view.getId());
 
 		irSend(data);
 	}
 
 	private void irSend(String data) {
 		// Make sure one got called
 		if (data != null) {
 			try {
 				// Convert the code to hex
 				data = hex2dec(data);
 
 				// Activity the IR port
 				irWrite.invoke(irService, data);
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Convert the code to hex
 	 * 
 	 * @param irData
 	 *            The String representation of the code
 	 * @return The hex code for the IR command
 	 */
 	private String hex2dec(String irData) {
 		// Split the command on ' '
 		List<String> list = new ArrayList<String>(Arrays.asList(irData
 				.split(" ")));
 
 		// Remove the dummy data
 		list.remove(0);
 
 		// Get the frequency from the list and parse it
 		int frequency = Integer.parseInt(list.remove(0), 16); // frequency
 
 		// Remove the next two parts
 		list.remove(0); // seq1
 		list.remove(0); // seq2
 
 		// Go through each section and parse it to as a base 16 int
 		for (int i = 0; i < list.size(); i++) {
 			// Set the new section
 			list.set(i, Integer.toString(Integer.parseInt(list.get(i), 16)));
 		}
 
 		// Calculate the frequency
 		frequency = (int) (1000000 / (frequency * 0.241246));
 
 		// Insert the frequency at the start
 		list.add(0, Integer.toString(frequency));
 
 		// Reset the ir data string
 		irData = "";
 
 		// Add each converted section to return
 		for (String s : list) {
 			// Add it and separate by comma
 			irData += s + ",";
 		}
 
 		// Return the hex representation
 		return irData;
 	}
 }
