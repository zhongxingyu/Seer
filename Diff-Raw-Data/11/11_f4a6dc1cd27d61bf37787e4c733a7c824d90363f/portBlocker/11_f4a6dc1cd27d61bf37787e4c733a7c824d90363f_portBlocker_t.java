 package com.example.firerwar;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.DhcpInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView.*;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class portBlocker extends Fragment {
 	ServerSocket sock;
 	Socket temp;
 	public EditText portText;
 	public ArrayList<String> tcpViewText;
 	public ArrayList<String> udpViewText;
 	public ArrayList<String> tcpFilterList;
 	public ArrayList<String> udpFilterList;
 	public ArrayAdapter<String> adapter;
 	public ArrayAdapter<String> adapter2;
 	
 	protected TextView tcpDisplay;
 	protected TextView udpDisplay;
 	protected LinearLayout tempView;
 	protected ListView tcpPortsList;
 	protected ListView udpPortsList;
 	
 	protected Button closeTCPButton;
 	protected Button openTCPButton;
 	protected Button closeUDPButton;
 	protected Button openUDPButton;
 	
 	protected View rootView;
 
 	Context mContext;
 
 	protected int greenText;
 	protected int redText;
 	
 	protected static final int FILTER_SHOW_ALL = 0;
 	protected static final int FILTER_OPEN = 1;
 	protected static final int FILTER_CLOSE = 2;
 	protected static final int TCP = 1;
 	protected static final int UDP = 0;
 	protected static final int OPEN = 1;
 	protected static final int CLOSED = 0;
 	
 	public static final String ARG_SECTION_NUMBER = "BOOP";
 	
 	DatabaseManager db;
 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		db = new DatabaseManager(mContext);
 
 	}
 	
 	public void setContext(Context mContext) {
 		this.mContext = mContext;
 		
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		rootView = inflater.inflate(R.layout.port_enter, container, false);
 		greenText = R.color.openGreen;
 		redText = R.color.blockRed;
 
 		try {
 			System.out.println("derp");
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		initLayout();
 		return printNetworkSettings(mContext);
 	}
 	
 	protected void initLayout() {
 		tcpDisplay = (TextView) rootView.findViewById(R.id.TCPports);
 		udpDisplay = (TextView) rootView.findViewById(R.id.UDPports);
 		tempView = (LinearLayout) rootView.findViewById(R.id.LinearPortHolder);
 		tcpPortsList = (ListView) rootView.findViewById(R.id.PortItems);
 		udpPortsList = (ListView) rootView.findViewById(R.id.UDPItems);
 		
 		closeTCPButton = (Button) rootView.findViewById(R.id.portClosedButton);
 		openTCPButton = (Button) rootView.findViewById(R.id.portOpenButton);
 		closeUDPButton = (Button)rootView.findViewById(R.id.UDPcloseButton);
 		openUDPButton = (Button)rootView.findViewById(R.id.UDPopenButton);
 		
 		portText = (EditText) rootView.findViewById(R.id.portText);
 		portText.setRawInputType(Configuration.KEYBOARD_12KEY);
 
 		tcpViewText = new ArrayList<String>();
 		udpViewText = new ArrayList<String>();
 		tcpFilterList = new ArrayList<String>();
 		udpFilterList = new ArrayList<String>();
 		
 		tcpDisplay.setText("TCP Ports");
 		udpDisplay.setText("UDP Ports");
 		
 		adapter = new ArrayAdapter<String>(mContext,
 				android.R.layout.simple_list_item_1, tcpFilterList);
 		adapter2 = new ArrayAdapter<String>(mContext,
 				android.R.layout.simple_list_item_1, udpFilterList);
 		
 		int i = 0;
 		ArrayList<String> temp = db.getAllPorts(TCP);
 		while(temp.size() != i){
 			tcpViewText.add(temp.get(i));
 			tcpFilterList.add(temp.get(i));
 			try {
 				//TODO have it either open or close the port and parse the string that comes out.
 				blockporttcp(Integer.parseInt(temp.get(i)));
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			i++;
 		}
 		
		i = 0;
 		temp = db.getAllPorts(UDP);
 		while(temp.size() != i){
 			udpViewText.add(temp.get(i));
 			udpFilterList.add(temp.get(i));
 			try {
 				//TODO have it either open or close the port 
 				blockportudp(Integer.parseInt(temp.get(i)));
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			i++;
 		}
 
 		tcpPortsList.setAdapter(adapter);
 		udpPortsList.setAdapter(adapter2);
 
 	}
 
 	public void blockporttcp(final int port) throws IOException {
 		temp = new Socket();
 		db.addPort(port,CLOSED,TCP);
 
 		try {
 			new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 
 						if (!sock.isClosed()) {
 							if (!sock.isBound()) {
 								sock = new ServerSocket(port);
 								sock.close();
 							} else
 								sock.close();
 
 						}
 
 					} catch (Exception e) {
 						System.out.println("thread blockport failed" + e);
 					}
 					// }
 
 				}
 
 			}).start();
 		} catch (Exception e) {
 			Log.d("blockport Exception", "" + e);
 		}
 
 	}
 	
 	public void blockportudp(final int port) throws IOException {
 		temp = new Socket();
 		db.addPort(port,CLOSED,UDP);
 
 		try {
 			new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 
 						if (!sock.isClosed()) {
 							if (!sock.isBound()) {
 								sock = new ServerSocket(port);
 								sock.close();
 							} else
 								sock.close();
 
 						}
 
 					} catch (Exception e) {
 						System.out.println("thread blockport failed" + e);
 					}
 					// }
 
 				}
 
 			}).start();
 		} catch (Exception e) {
 			Log.d("blockport Exception", "" + e);
 		}
 
 	}
 
 	public void openporttcp(final int port) throws IOException {
 		sock = new ServerSocket();
 		db.addPort(port,OPEN,TCP);
 
 		try {
 			new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 
 						if (!sock.isBound()) {
 							sock = new ServerSocket(port);
 
 							sock.accept();
 
 						}
 
 					} catch (Exception e) {
 						System.out.println("thread openport failed: " + e);
 					}
 
 				}
 
 			}).start();
 		} catch (Exception e) {
 			Log.d("openport Exception", "" + e);
 		}
 
 	}
 	
 	public void openportudp(final int port) throws IOException {
 		sock = new ServerSocket();
 		db.addPort(port,OPEN,UDP);
 
 		try {
 			new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 
 						if (!sock.isBound()) {
 							sock = new ServerSocket(port);
 
 							sock.accept();
 
 						}
 
 					} catch (Exception e) {
 						System.out.println("thread openport failed: " + e);
 					}
 
 				}
 
 			}).start();
 		} catch (Exception e) {
 			Log.d("openport Exception", "" + e);
 		}
 
 	}
 	
 	@Override
 	public void onStart() {
 		setHasOptionsMenu(true);
 		super.onStart();
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		if (inflater != null) {
 			inflater.inflate(R.menu.ports_menu, menu);
 		}
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_open:
 			filterList(FILTER_OPEN);
 			return true;
 		case R.id.menu_close:
 			filterList(FILTER_CLOSE);
 			return true;
 		case R.id.menu_all:
 			filterList(FILTER_SHOW_ALL);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	private void filterList(int filter) {
 		int i;
 		tcpFilterList.clear();
 		udpFilterList.clear();
 
 		switch (filter) {
 		case FILTER_OPEN:
 			for (i = 0; i < tcpViewText.size(); i++) {
 				if (tcpViewText.get(i).contains("opened")) {
 					tcpFilterList.add(tcpViewText.get(i));
 				}
 			}
 			for (i = 0; i < udpViewText.size(); i++) {
 				if (udpViewText.get(i).contains("opened")) {
 					udpFilterList.add(udpViewText.get(i));
 				}
 			}
 			break;
 		case FILTER_CLOSE:
 			for (i = 0; i < tcpViewText.size(); i++) {
 				if (tcpViewText.get(i).contains("blocked")) {
 					tcpFilterList.add(tcpViewText.get(i));
 				}
 			}
 			for (i = 0; i < udpViewText.size(); i++) {
 				if (udpViewText.get(i).contains("blocked")) {
 					udpFilterList.add(udpViewText.get(i));
 				}
 			}
 			break;
 		case FILTER_SHOW_ALL:
 			for (i = 0; i < tcpViewText.size(); i++) {
 				tcpFilterList.add(tcpViewText.get(i));
 			}
 			for (i = 0; i < udpViewText.size(); i++) {
 				udpFilterList.add(udpViewText.get(i));
 			}
 			break;
 		}
 		
 		adapter.notifyDataSetChanged();
 		adapter2.notifyDataSetChanged();
 	}
 
 	public View printNetworkSettings(Context mContext) {
 
 		closeTCPButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String portHold = portText.getText().toString();
 				int i;
 				
 				try {
 					int port = Integer.parseInt(portHold);
 					
 					for (i = 0; i < tcpViewText.size(); i++) {
 						if (tcpViewText.get(i).contains(portHold)) {
 							tcpViewText.remove(i);
 							break;
 						}
 					}
 					for (i = 0; i < tcpFilterList.size(); i++) {
 						if (tcpFilterList.get(i).contains(portHold)) {
 							tcpFilterList.remove(i);
 							break;
 						}
 					}
 
 					
 					tcpViewText.add(portHold + " closed");
 					tcpFilterList.add(portHold + " closed");
 
 
 					adapter.notifyDataSetChanged();
 
 					// TODO add error checking for the above here and below here
 					//to notify the user that something went wrong with blocking the port. 
 					//there should probably be error checking else where as well. 
 
 					blockporttcp(port);
 				} catch (IOException e) {
 					System.out.println(e + " failed in block port");
 				} catch (Exception e) {
 					System.out.println("failed in int conversion: " + e);
 				}
 
 			}
 		});
 
 		openTCPButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				String portHold = portText.getText().toString();
 				int i;
 				
 				try {
 					int port = Integer.parseInt(portHold);
 					
 					for (i = 0; i < tcpViewText.size(); i++) {
 						if (tcpViewText.get(i).contains(portHold)) {
 							tcpViewText.remove(i);
 							break;
 						}
 					}
 					for (i = 0; i < tcpFilterList.size(); i++) {
 						if (tcpFilterList.get(i).contains(portHold)) {
 							tcpFilterList.remove(i);
 							break;
 						}
 					}
 					tcpViewText.add(portHold + " opened");
 					tcpFilterList.add(portHold + " opened");
 					adapter.notifyDataSetChanged();
 
 					// TODO add error checking for the above here
 
 					openporttcp(port);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					System.out.println(e + " failed in open port");
 				} catch (Exception e) {
 					System.out.println("failed in open int conversion: " + e);
 				}
 
 			}
 		});
 		
 		closeUDPButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				String portHold = portText.getText().toString();
 				int i;
 				
 				try {
 					int port = Integer.parseInt(portHold);
 					
 					for (i = 0; i < udpViewText.size(); i++) {
 						if (udpViewText.get(i).contains(portHold)) {
 							udpViewText.remove(i);
 							break;
 						}
 					}
 					for (i = 0; i < udpFilterList.size(); i++) {
 						if (udpFilterList.get(i).contains(portHold)) {
 							udpFilterList.remove(i);
 							break;
 						}
 					}
 					udpViewText.add(portHold + " blocked");
 					udpFilterList.add(portHold + " blocked");
 					adapter2.notifyDataSetChanged();
 
 					// TODO add error checking for the above here
 
 					blockportudp(port);
 				} catch (IOException e) {
 					System.out.println(e + " failed in block port");
 				} catch (Exception e) {
 					System.out.println("failed in int conversion: " + e);
 				}
 			}
 			
 		});
 
 		openUDPButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				String portHold = portText.getText().toString();
 				int i;
 				
 				try {
 					int port = Integer.parseInt(portHold);
 					
 					for (i = 0; i < udpViewText.size(); i++) {
 						if (udpViewText.get(i).contains(portHold)) {
 							udpViewText.remove(i);
 							break;
 						}
 					}
 					for (i = 0; i < udpFilterList.size(); i++) {
 						if (udpFilterList.get(i).contains(portHold)) {
 							udpFilterList.remove(i);
 							break;
 						}
 					}
 					
 					udpViewText.add(portHold + " opened");
 					udpFilterList.add(portHold + " opened");
 					adapter2.notifyDataSetChanged();
 
 					// TODO add error checking for the above here
 
 					openportudp(port);
 				} catch (IOException e) {
 					System.out.println(e + " failed in open port");
 				} catch (Exception e) {
 					System.out.println("failed in open int conversion: " + e);
 				}
 			}
 			
 		});
 		
 		adapter.notifyDataSetChanged();
 		adapter2.notifyDataSetChanged();
 
 		return tempView;
 
 	}
 
 }
