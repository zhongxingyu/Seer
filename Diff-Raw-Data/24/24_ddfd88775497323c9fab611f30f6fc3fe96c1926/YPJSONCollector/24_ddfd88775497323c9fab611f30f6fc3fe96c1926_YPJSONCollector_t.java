 package com.where.atlas.feed.yellowpages;
 
 import java.io.IOException;
 
 import com.where.atlas.feed.PlaceCollector;
 import com.where.place.Place;
 import com.where.place.YPPlace;
 
 public class YPJSONCollector implements PlaceCollector{
 	@Override
 	public void collect(Place place) {
 		YPPlace ypplace = (YPPlace)place;
 		try {
 			
			YPRawDataParser.bufferedWriter().write(ypplace.toJSON().toString()+"\n");
 			
 		} catch (IOException e) {
 			System.err.println("error writing:"+ypplace+"\t"+e.getMessage());
 		}
 	}
 
 	@Override
 	public void collectBadInput(Object input, Exception reason) {
 				//if all else fails do nothing :(  --The data's bad enough!
 	}
 
 }
 
