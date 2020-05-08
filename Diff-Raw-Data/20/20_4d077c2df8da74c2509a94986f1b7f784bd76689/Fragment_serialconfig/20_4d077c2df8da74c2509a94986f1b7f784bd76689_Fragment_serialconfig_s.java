 package com.example.testgateway;
 
 
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.Thread.State;
 
 import com.example.testgateway.MainActivity.HaveData;
 
 
 import senseHuge.model.Serial;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android_serialport_api.SerialPort;
 /**
  * serial configure
  * @author jiangnanEdu
  *
  */
 public class Fragment_serialconfig extends Fragment {
 	protected static final String tag = "Fragment_serialconfig";
 	Serial serial = new Serial();
 	Spinner spinner =null;
 	Spinner devSpinner = null;
 	InputStream mInputStream;
 	OutputStream mOutputStream;
 	SerialPort serialPort = null;
 	byte[] mBuffer;
 	SerialPort mSerialPort = null;
 	Button connectButton = null;
 	Button closeButton = null;
 	ReadThread readThread = null;
 	MainActivity ma;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 	
 		 ma = (MainActivity) getActivity();
 			
 		View view1=LayoutInflater.from(getActivity()).inflate(R.layout.serialconfig,null);
 		
 		connectButton = (Button) view1.findViewById(R.id.connect);
 		closeButton = (Button) view1.findViewById(R.id.close);
 		connectButton.setOnClickListener(new ButtonClickListener());
 		closeButton.setOnClickListener(new ButtonClickListener());
 		
 		devSpinner =  (Spinner) view1.findViewById(R.id.spinner_device);
 		spinner = (Spinner) view1.findViewById(R.id.spinner_baud);
 		
 		ArrayAdapter<CharSequence> bauldadapter = ArrayAdapter.createFromResource(getActivity(), R.array.baudrates_name, android.R.layout.simple_dropdown_item_1line);
 		ArrayAdapter<CharSequence> devadapter =   ArrayAdapter.createFromResource(getActivity(), R.array.dev_path, android.R.layout.simple_dropdown_item_1line);
 		
 		spinner.setAdapter(bauldadapter);
 		devSpinner.setAdapter(devadapter);
 		//USB0
 		devSpinner.setSelection(4);
 		spinner.setSelection(16);
 	
 		serial.setFilePath( getActivity().getResources().getStringArray(R.array.dev_path)[3]);
 		serial.setBuandrate(Integer.parseInt(( getActivity().getResources().getStringArray(R.array.baudrates_name)[12])));
 		serial.setState(false);
 
 		devSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int position, long id) {
 				Log.i(tag, ""+position);
 			
 				serial.setFilePath( getActivity().getResources().getStringArray(R.array.dev_path)[position]);
 	            Log.i(tag, serial.getFilePath());
 	            
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 		
 				
 			}
 		});
 		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int position, long id) {
 				Log.i(tag, ""+position);
 				serial.setBuandrate(Integer.parseInt(( getActivity().getResources().getStringArray(R.array.baudrates_name)[position])));
 			
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		return view1;
 	}
 	
 	class ButtonClickListener implements OnClickListener{
 		FragmentTransaction transaction ;
 			@Override
 			public void onClick(View v) {
 				switch(v.getId()){
 				case R.id.connect:
 				{
 					if(mSerialPort == null || !serial.getState()){
 						
 						try {
 							Log.i(tag, serial.getFilePath()+serial.getBuandrate());
 							
 							ma.mSerialPort = mSerialPort = new SerialPort(new File(serial.getFilePath()), serial.getBuandrate(), 0);
 							mOutputStream = mSerialPort.getOutputStream();
 							mInputStream = mSerialPort.getInputStream();
 							
 							/*byte [] buffer = {'A','B','C','e'};
 							mOutputStream.write(buffer);*/
 						} catch (SecurityException e1) {
 							// TODO Auto-generated catch block
 							Log.i(tag, "򿪴ʧܣ");
 							e1.printStackTrace();
 							break;
 						} catch (IOException e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 							Log.i(tag, "򿪴ʧܣ");
 							break;
 						}
 						Log.i(tag, "򿪴sucess");
 						
 						serial.setState(true);
 						readThread=new ReadThread();
 						readThread.start();
 			
 						ma.isWork = true;
 						ma.havadata = ma.getHaveData(); 
 					
 							ma.havadata.start();
 				        ma.serialState.setValue(true);
 					}
 				
 					break;
 				}
 				case R.id.close:{
 					if(mSerialPort != null && serial.getState()){
 						
 						try {
 							mSerialPort.close();
 							mOutputStream .close();
 							mInputStream .close();
 							
 							readThread.interrupt();
 							
 							serial.setState(false);
 							Log.i(tag, "رhaveData sucess");
 							
 							Log.i(tag,ma.havadata.getState().toString() );
 							ma.isWork = false;
 							ma.havadata.interrupt();
 							ma.serialState.setValue(false);
 							
 						if (ma.havadata.isInterrupted()) {
 							Log.i(tag,"жϣ");
 						}
 							
 							Log.i(tag,ma.havadata.getState().toString() );
 							
 //							readThread.stop();
 							Log.i(tag, "رմsucess");
 						    serial.setState(false);
 						} catch (SecurityException e1) {
 							// TODO Auto-generated catch block
 							Log.i(tag, "رմʧܣ");
 							e1.printStackTrace();
 						} catch (IOException e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 							Log.i(tag, "رմʧܣ");
 						}
 					}
 					
 					break;
 				}
 				
 				default:{
 				
 					break;
 				}
 				}
 			}
 		}
 	/**
 	 * ڶ߳
 	 * @author jiangnanEdu
 	 *
 	 */
 	private class ReadThread extends Thread {

 		@Override
 		public void run() {
 			super.run();
 			System.out.println("^^^^^^^^^"+ma.serialUtil.stringBuffer.toString());
			while(!isInterrupted()) {
 				int size;
 				try {
					byte[] buffer = new byte[64];
 					if (mInputStream == null) return;
 					size = mInputStream.read(buffer);
 					/*if (size > 0) {
 						//onDataReceived(buffer, size);
 						Log.i(tag, "start+"+size);
 					}*/
 					for (int j = 0; j < size; j++) {
 						
 						ma.serialUtil.stringBuffer.append(byteToHex(buffer[j]));
 					}
 	
 					
 				} catch (IOException e) {
 					e.printStackTrace();
 					return;
 				}
 			}
 		}
 	}
 	public String byteToHex(byte byteData) {
 		// TODO Auto-generated method stub
 		String hex = "";
 		hex = Integer.toHexString(byteData & 0xFF); 
 		if(hex.length() == 1) hex = "0"+hex;
 		return hex.toUpperCase();
 	}
 }
