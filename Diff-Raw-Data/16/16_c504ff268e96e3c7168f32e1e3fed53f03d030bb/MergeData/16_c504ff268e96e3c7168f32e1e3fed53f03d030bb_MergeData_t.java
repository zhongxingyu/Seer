 package de.grundid.recycling.merge;
 
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.util.StringUtils;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
import de.gonam.openinghours.OpeningHoursJsonModule;
 import de.gonam.openinghours.WeeklyTimeInterval;
 import de.gonam.openinghours.WeeklyTimeIntervalViewModel;
 import de.grundid.recycling.RecyclingData;
 import de.grundid.recycling.openinghours.OpeningHoursParser;
 
 public class MergeData {
 
 	private ObjectMapper objectMapper = new ObjectMapper();
 
	public MergeData() {
		objectMapper.registerModule(new OpeningHoursJsonModule());
	}

 	public RecyclingContainer read(String filename) {
 		try {
 			InputStream inputStream = MergeData.class.getClassLoader().getResourceAsStream(filename);
 			return objectMapper.readValue(inputStream, RecyclingContainer.class);
 		}
 		catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private boolean isClose(RecyclingData data, RecyclingHn hn) {
 		if (hn.hasLocation()) {
 			double latDif = Math.abs(data.getLat() - hn.getLat().doubleValue());
 			double lonDif = Math.abs(data.getLon() - hn.getLon().doubleValue());
 			return latDif < 0.01 && lonDif < 0.01;
 		}
 		return false;
 	}
 
 	private void merge(RecyclingData data, RecyclingHn hn) {
 		data.setLat(hn.getLat().doubleValue());
 		data.setLon(hn.getLon().doubleValue());
 
 		Map<String, String> tags = data.getKeyValues();
 
 		tags.put("name", hn.getName());
 		tags.put("addr:zip", hn.getZip());
 		tags.put("addr:city", hn.getCity());
 		tags.put("addr:street", hn.getStreet());
 		tags.put("opening_hours", hn.getOpeningHours());
 	}
 
 	public void run() {
 		RecyclingContainer container1 = read("recycling.js");
 		RecyclingContainer containerHn = read("recycling-hn.js");
 
 		for (RecyclingData data : container1.getRecyclingData()) {
 			for (Iterator<RecyclingHn> it = containerHn.getRecyclingHn().iterator(); it.hasNext();) {
 				RecyclingHn hn = it.next();
 				if (isClose(data, hn)) {
 					System.out.println("Close: " + data.getLat() + "|" + data.getLon() + " => " + hn.getName() + " "
 							+ hn.getLat() + "|" + hn.getLon());
 					merge(data, hn);
 					it.remove();
 				}
 			}
 		}
 
 		for (Iterator<RecyclingHn> it = containerHn.getRecyclingHn().iterator(); it.hasNext();) {
 			RecyclingHn hn = it.next();
 			if (hn.hasLocation()) {
 				RecyclingData recyclingData = new RecyclingData();
 				merge(recyclingData, hn);
 
 				container1.getRecyclingData().add(recyclingData);
 			}
 		}
 
 		injectOpeningHours(container1.getRecyclingData());
 
 		try {
 			FileOutputStream out = new FileOutputStream("C:\\recycling.js");
 			objectMapper.writeValue(out, container1);
 			out.flush();
 			out.close();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private void injectOpeningHours(List<RecyclingData> recyclingData) {
 		OpeningHoursParser parser = new OpeningHoursParser();
 		for (RecyclingData data : recyclingData) {
 
 			String openingHours = data.getKeyValues().get("opening_hours");
 			if (StringUtils.hasText(openingHours)) {
 				try {
 					List<WeeklyTimeInterval> list = parser.parse(openingHours);
 					List<WeeklyTimeIntervalViewModel> models = new ArrayList<WeeklyTimeIntervalViewModel>();
 					for (WeeklyTimeInterval wti : list) {
 						models.add(new WeeklyTimeIntervalViewModel(wti));
 					}
 					data.setOpeningHours(models);
 				}
 				catch (Exception e) {
 					System.out.println("IAE: " + openingHours);
 				}
 			}
 		}
 
 	}
 
 	public static void main(String[] args) {
 		new MergeData().run();
 	}
 }
