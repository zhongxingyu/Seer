 package com.station.taxi.sockets;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import android.content.Context;
 import android.support.v4.content.AsyncTaskLoader;
 
 import com.station.taxi.sockets.message.AbstractResponse;
 import com.station.taxi.sockets.message.ListDrivingCabsResponse;
 import com.station.taxi.sockets.message.ListPassengersResponse;
 import com.station.taxi.sockets.message.ListWaitingCabsResponse;
 
 public class SocketClientLoader extends AsyncTaskLoader<List<String>> {
 	
 	
 	private static final int TYPE_WAITING_CABS = 0;
 	private static final int TYPE_WAITING_PASSENGERS = 1;
 	private static final int TYPE_DRIVING_CABS = 2;
 
 	private String mServerIp;
 	private int mType;
 
 	public SocketClientLoader(String serverIp, int type, Context context) {
 		super(context);
 		mServerIp = serverIp;
 		mType = type;
 	}
 
 	@Override
 	public List<String> loadInBackground() {
 		
 		StationClient client = new StationClient(mServerIp);
 
 		List<String> list = new ArrayList<String>();
 		AbstractResponse absResponse = client.request(typeToRequest(mType));
 
 		if (absResponse== null || !absResponse.isStatusOk()) {
 			return list;
 		}
 		switch(mType) {
 		case TYPE_WAITING_CABS:
 			return readWaitingCabsList((ListWaitingCabsResponse)absResponse,list);
 		case TYPE_WAITING_PASSENGERS:
 			return readWaitingPassengers((ListPassengersResponse)absResponse,list);
 		case TYPE_DRIVING_CABS:
 			return readDrivingCabs((ListDrivingCabsResponse)absResponse,list);
 		}
 		return list;
 	}
 
 	private String typeToRequest(int type) {
 		switch(mType) {
 		case TYPE_WAITING_CABS:
 			return StationClient.REQUEST_LIST_WAITING_CABS;
 		case TYPE_WAITING_PASSENGERS:
 			return StationClient.REQUEST_LIST_WAITING_PASSENGER;
 		case TYPE_DRIVING_CABS:
 			return StationClient.REQUEST_LIST_DRIVING;
 		}
 		return null;
 	}
 	
 	private List<String> readDrivingCabs(ListDrivingCabsResponse response, List<String> list) {
 		Set<Integer> nums = response.getCabNumbers();
 		for(Integer num: nums) {
 			StringBuilder sb = new StringBuilder();
 			sb.append(num);
 			String dest = response.getDestination(num);
 			sb.append(" --> ").append(dest).append(" [");
 			List<String> names = response.getPassengers(num);
 			sb.append(names.get(0));
 			for (int i=1; i<names.size(); i++) {
 				sb.append(", ").append(names.get(i));
 			}
 			sb.append("]");
 			list.add(sb.toString());
 		}
 		
 		return list;
 	}
 
 	private List<String> readWaitingPassengers(ListPassengersResponse response, List<String> list) {
 		Map<String, String> passengers = response.getPassengers();
 		for(String name: passengers.keySet()) {
 			StringBuilder sb = new StringBuilder();
 			sb.append(name);
 			String dest = passengers.get(name);
 			sb.append(" --> ").append(dest);
 			list.add(sb.toString());
 		}
 		
 		return list;
 	}
 
 	private List<String> readWaitingCabsList(ListWaitingCabsResponse response, List<String> list) {
 		Map<Integer,String> nums = response.getCabsStatus();
 		Map<Integer, String> acts = response.getCabsWaitActions();
 		for(Integer num: nums.keySet()) {
 			StringBuilder sb = new StringBuilder();
 			sb.append(num);
 			String status = nums.get(num);
 			sb.append(" [").append(status);
 			if (status.equals("waiting")) {
 				String act = acts.get(num);
 				sb.append(", ").append(act);
 			}
 			sb.append("]");
 			list.add(sb.toString());
 		}
 		
 		return list;
 	}
 	
 }
 
