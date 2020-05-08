 package uk.frequency.glance.test;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import uk.frequency.glance.server.model.component.Position;
 import uk.frequency.glance.server.model.user.UserProfile;
 import uk.frequency.glance.server.transfer.event.EventDTO;
 import uk.frequency.glance.server.transfer.trace.PositionTraceDTO;
 import uk.frequency.glance.server.transfer.trace.TraceDTO;
 import uk.frequency.glance.server.transfer.user.UserDTO;
 
 import com.google.gson.reflect.TypeToken;
 
 public class TestCaseLauncher {
 
 	static final String DIR = "src/test/java/test_cases";
 	static final String DATE_FORMAT = "HH:mm:ss:S";
 //	static final String DATE_FORMAT = "HH:mm:ss";
 	static final int TIME_BETWEEN_REQUESTS = 3000;
 	
 	static String ROOT_URL = "http://localhost:8080/services/";
 //	static String ROOT_URL = "http://glance-server.herokuapp.com/services/";
 	static final TestClient client = new TestClient(ROOT_URL);
 
 	public static void main(String[] args) {
 		try {
//			runTestCase("fionn_03-22");
			runTestCase("victor_03-25_move");
 //			runTestCase("test");
 //			runTestCase("case_1");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	static void runTestCase(String fileName) throws IOException, ParseException, InterruptedException{
 		UserDTO user = createTestUser();
 		user = (UserDTO) client.postAndPrint(user, "user");
 		
 		List<PositionTraceDTO> traces = loadTraces(fileName, user.getId());
 		Collections.sort(traces, new TraceTimeComp());
 		for(PositionTraceDTO trace : traces){
 			Date requestTime = new Date();
 			client.postAndPrint(trace, "trace");
 			Thread.sleep(TIME_BETWEEN_REQUESTS);
 			
 			String path = String.format("event/user-%d/created_after-%d", user.getId(), requestTime.getTime());
 			List<EventDTO> events = (List<EventDTO>) client.getListAndPrint(path, new TypeToken<List<EventDTO>>(){});
 			verifyEvents(events);
 		}
 	}
 	
 	static boolean verifyEvents(List<EventDTO> events){
 		//TODO
 		return true;
 	}
 	
 	static UserDTO createTestUser(){
 		UserProfile profile = new UserProfile();
 		profile.setFullName("Test");
 		UserDTO user = new UserDTO();
 		user.setUsername("testuser");
 		user.setProfile(profile);
 		return user;
 	}
 	
 	static List<PositionTraceDTO> loadTraces(String fileName, long user) throws IOException, ParseException{
 		File file = new File(DIR, fileName);
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 
 		List<PositionTraceDTO> traces = new ArrayList<PositionTraceDTO>();
 		String line = reader.readLine();
 		while(line != null){
 			PositionTraceDTO trace = parseTrace(line, user);
 			traces.add(trace);
 			line = reader.readLine();
 		}
 
 		reader.close();
 		return traces;
 	}
 	
 	static PositionTraceDTO parseTrace(String line, long user) throws ParseException{
 		String[] parts = line.split("\t");
 		long time = new SimpleDateFormat(DATE_FORMAT).parse(parts[0]).getTime();
 		String[] latlngParts = parts[1].split(",");
 		double lat = Double.valueOf(latlngParts[0]);
 		double lng = Double.valueOf(latlngParts[1]);
 		Position pos = new Position();
 		pos.setLat(lat);
 		pos.setLng(lng);
 		PositionTraceDTO trace = new PositionTraceDTO();
 		trace.setPosition(pos);
 		trace.setTime(time);
 		trace.setUserId(user);
 		return trace;
 	}
 	
 	static class TraceTimeComp implements Comparator<TraceDTO>{
 		@Override
 		public int compare(TraceDTO o1, TraceDTO o2) {
 			if(o1.getTime() < o2.getTime())
 				return -1;
 			else if(o1.getTime() > o2.getTime())
 				return 1;
 			else
 				return 0;
 		}
 	}
 	
 }
