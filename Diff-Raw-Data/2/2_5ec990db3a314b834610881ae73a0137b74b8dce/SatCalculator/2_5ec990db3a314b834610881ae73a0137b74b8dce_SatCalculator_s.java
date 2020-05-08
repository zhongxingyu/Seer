 package dk.dma.ais.analysis.coverage.calculator;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import dk.dma.ais.analysis.coverage.AisCoverage;
 import dk.dma.ais.analysis.coverage.calculator.geotools.Helper;
 import dk.dma.ais.analysis.coverage.data.Cell;
 import dk.dma.ais.analysis.coverage.data.QueryParams;
 import dk.dma.ais.analysis.coverage.data.Source;
 import dk.dma.ais.analysis.coverage.data.CustomMessage;
 import dk.dma.ais.analysis.coverage.data.Source.ReceiverType;
 import dk.dma.ais.analysis.coverage.data.TimeSpan;
 import dk.dma.ais.packet.AisPacketTags.SourceType;
 
 
 public class SatCalculator extends AbstractCalculator {
 
 	
 	private static final Logger LOG = LoggerFactory.getLogger(SatCalculator.class);
 	private int timeMargin = 600000; //in ms
 	private LinkedHashMap<String, Boolean> doubletBufferSat = new LinkedHashMap<String, Boolean>()
 	{
 	     @Override
 	     protected boolean removeEldestEntry(Map.Entry eldest)
 	     {
 	        return this.size() > 10000;   
 	     }
 	};	
 	private LinkedHashMap<String, Boolean> doubletBufferTerrestrial = new LinkedHashMap<String, Boolean>()
 			{
 		@Override
 		protected boolean removeEldestEntry(Map.Entry eldest)
 		{
 			return this.size() > 10000;   
 		}
 	};
 	//Cell id is mapped to a list of timespans
 //	private Map<String, FixedSpanCell> fixedSpans = new ConcurrentHashMap<String, FixedSpanCell>();
 	
 	//Ten minutes priority queue
 //	PriorityQueue<CustomMessage> tenMinQueue = new PriorityQueue<CustomMessage>(1000,new CustomMessageDateComparator()); 
 
 	//6 hours priority queue
 //	PriorityQueue<CustomMessage> sixHourQueue = new PriorityQueue<CustomMessage>(1000,new CustomMessageDateComparator()); 
 	
 //	public static void main(String[] args){
 //		SatCalculator s = new SatCalculator();
 //		s.getFixedTimeSpans(new Date(), new Date(new Date().getTime()+1000*60*60), 1, 1, 1, 1,1);
 //	}
 	
 	/**
 	 * Retrieves a list of time spans based on a rectangle defined by two lat-lon points. 
 	 * Cells within the rectangle each contain a number of time spans. Spans over the same time are merged.
 	 */
 	public List<TimeSpan> getFixedTimeSpans(Date startTime, Date endTime, double latMin, double latMax, double lonMin, double lonMax, int interval){
 		
 		
 		//Initialize timespans
 		Date floorDate = Helper.getFloorDate(startTime);
      	Date ceilDate = Helper.getFloorDate(endTime);
 		List<TimeSpan> result = new ArrayList<TimeSpan>();	
 		Map<Long, TimeSpan> timespanMap = new HashMap<Long, TimeSpan>();
 		int timeDifference = (int) Math.ceil((ceilDate.getTime() - floorDate.getTime())/1000/60/60); //in hours
 		for (int i = 0; i < timeDifference; i++) {
 			TimeSpan t = new TimeSpan(new Date(floorDate.getTime()+(1000*60*60*i)));
 			t.setLastMessage(new Date(floorDate.getTime()+(1000*60*60*(i+1))));
 			result.add(t);
 			timespanMap.put(t.getFirstMessage().getTime(),t );
 //			System.out.println("inserted" + t.getFirstMessage().getTime());
 		}
 		
 		Collection<Cell> cells = dataHandler.getSource("supersource").getGrid().values();
 		
 		for (Cell fixedSpanCell : cells) {
 //			System.out.println(latMax);
 //			System.out.println(latMin);
 //			System.out.println(lonMax);
 //			System.out.println(lonMin);
 //			System.out.println(fixedSpanCell.lat);
 //System.out.println(fixedSpanCell.lon);
 //System.out.println();
 			if(fixedSpanCell.getLatitude() <= latMax && fixedSpanCell.getLatitude() >= latMin &&
 					fixedSpanCell.getLongitude() >= lonMin && fixedSpanCell.getLongitude() <= lonMax	){
 				
 				Collection<TimeSpan> spans = fixedSpanCell.getFixedWidthSpans().values();
 				for (TimeSpan timeSpan : spans) {
 //					System.out.println("sdf");
 //					System.out.println(timeSpan.getFirstMessage().getTime());
 //					System.out.println(startTime);
 					if(timeSpan.getLastMessage().getTime() <= endTime.getTime() && timeSpan.getFirstMessage().getTime() >= startTime.getTime()){
 						TimeSpan resultSpan = timespanMap.get(timeSpan.getFirstMessage().getTime());
 						resultSpan.add(timeSpan);
 					}
 				}
 			}
 		}
 		System.out.println(timeDifference);
 		
 		
 		return result;
 	}
 	
 	/**
	 * Retrieves a list of time spans based on a rectangle defined p two lat-lon points. 
 	 * Cells within the rectangle each contain a number of time spans. Two time spans will be merged
 	 * if they are close to each other (closeness is defined by timeMargin). In that way the rectangle
 	 * given by the user will be seen as one big cell.
 	 * @param latStart
 	 * @param lonStart
 	 * @param latEnd
 	 * @param lonEnd
 	 * @return
 	 */
 	public List<TimeSpan> getDynamicTimeSpans(Date startTime, Date endTime, double latStart, double lonStart, double latEnd, double lonEnd){
 			
 		//Retrieve cells within the specified rectangle
     	Collection<Cell> cells = dataHandler.getCells(null);
 		List<Cell> areaFiltered = new ArrayList<Cell>();
 		for (Cell cell : cells) {
 			if(cell.getLatitude() <= latStart && cell.getLatitude() >= latEnd ){
 				if(cell.getLongitude() >= lonStart && cell.getLongitude() <= lonEnd ){
 					areaFiltered.add(cell);
 				}
 			}
 		}
 		
 		//Store every time span of the filtered cells
 		List<TimeSpan> spans = new ArrayList<TimeSpan>();
 		if(startTime != null && endTime != null){
 			for (Cell cell : areaFiltered) {
 				List<TimeSpan> individualSpan = cell.getTimeSpans();
 				for (TimeSpan timeSpan : individualSpan) {
 					if(timeSpan.getFirstMessage().getTime() > startTime.getTime() &&
 							timeSpan.getLastMessage().getTime() < endTime.getTime()){
 						spans.add(timeSpan);
 					}
 				}
 			}	
 		}else{
 	    	for (Cell cell : areaFiltered) {
 				List<TimeSpan> individualSpan = cell.getTimeSpans();
 				for (TimeSpan timeSpan : individualSpan) {
 					spans.add(timeSpan);
 				}
 			}	
 		}
     	
     	//sort time spans based on date
     	Collections.sort(spans, new SortByDate());
     	
     	//Merge time spans that are too close to each other (specified on timeMargin)
     	List<TimeSpan> merged = new ArrayList<TimeSpan>();
     	TimeSpan current = null;
     	for (int i = 0; i < spans.size(); i++) {
     		if(current == null){
     			current = spans.get(i).copy();
     			merged.add(current);
     		}else{
     			TimeSpan next = spans.get(i).copy();
     			if(	next.getFirstMessage().getTime() < current.getLastMessage().getTime() ||
     				Math.abs(next.getFirstMessage().getTime()-current.getLastMessage().getTime()) < timeMargin){
     				
     				//Merge current and next time span
     				TimeSpan m = mergeTimeSpans(current, next);
     				merged.remove(merged.size()-1);	
     				merged.add(m);
     				current = m;
     				
     			}else{
     				//Current and next don't need to be merged
     				current = next;
     				merged.add(current);
     			}
     		}
     		
     		LOG.debug(spans.get(i).getFirstMessage()+" "+spans.get(i).getLastMessage()+" "+spans.get(i).getMessageCounterSat()+ " "+spans.get(i).getDistinctShipsSat().size());
 		}
 
     	return merged;
 	}
 	public Collection<Cell> getCells(double latStart, double lonStart, double latEnd, double lonEnd){
 		Map<String, Boolean> sourcesMap = new HashMap<String, Boolean>();
 		sourcesMap.put("supersource", true);
 		QueryParams params = new QueryParams();
 		params.latStart=latStart;
 		params.latEnd=latEnd;
 		params.lonStart=lonStart;
 		params.lonEnd=lonEnd;
 		params.sources=sourcesMap;
 		params.multiplicationFactor=1;
 		
 		return dataHandler.getCells(params);
 	}
 	
 	private boolean matchTerrestrialMessage(CustomMessage m){
 		//Find cell
 		Cell c = dataHandler.getCell("supersource", m.getLatitude(), m.getLongitude());
 		if(c == null)return false;
 		
 		List<TimeSpan> timespans = c.getTimeSpans();
 		for (int i = timespans.size()-1; i >= 0; i--) {
 			TimeSpan t = c.getTimeSpans().get(i);
 			//If message is newer than t, then it will be newer than every other timespan
 			//So just return false
 			if(	t.getLastMessage().getTime() < m.getTimestamp().getTime()) return false;
 			
 			//If m is older than last message but newer than first messsage, then m belongs to the timespan
 			if(t.getFirstMessage().getTime() <= m.getTimestamp().getTime()){
 				t.getDistinctShipsTerrestrial().put(m.getShipMMSI()+"", true);
 				t.incrementMessageCounterTerrestrialUnfiltered();
 				
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	private void calcFixedTimeSpan(CustomMessage m){
 		double lonRound = Helper.roundLon(m.getLongitude(), 1);
 		double latRound = Helper.roundLat(m.getLatitude(), 1);
 		String cellId = Helper.getCellId(m.getLatitude(), m.getLongitude(), 1);
 		
 		//get the right cell, or create it if it doesn't exist.
 		Cell cell = dataHandler.getCell("supersource", m.getLatitude(), m.getLongitude());
 		if (cell == null) {
 			cell = dataHandler.createCell("supersource", m.getLatitude(), m.getLongitude());
 		}
 		
 		//Fetch specific time span
 		Date id = Helper.getFloorDate(m.getTimestamp());
 		TimeSpan fixedSpan = cell.getFixedWidthSpans().get(id.getTime());
 		if(fixedSpan==null){
 			fixedSpan=new TimeSpan(id);
 			fixedSpan.setLastMessage(Helper.getCeilDate(m.getTimestamp()));
 			cell.getFixedWidthSpans().put(id.getTime(), fixedSpan);
 		}
 
 		if(m.getSourceType() == SourceType.SATELLITE){	
 			fixedSpan.setMessageCounterSat(fixedSpan.getMessageCounterSat()+1);
 			fixedSpan.getDistinctShipsSat().put(""+m.getShipMMSI(), true);
 		}else{
 			fixedSpan.incrementMessageCounterTerrestrialUnfiltered();
 			fixedSpan.getDistinctShipsTerrestrial().put(""+m.getShipMMSI(), true);
 		}
 		
 	}
 	/**
 	 * The message belongs to a cell. In this cell a new time span will be created if
 	 * the time since the last message arrival is more than the timeMargin. Else, the 
 	 * current time span will be updated.
 	 * 
 	 * The order of messages is not guaranteed. Some times we can not just use the latest time span of the cell
 	 * because the message might need to go into an older time span. Or a new time span might need to be created
 	 * in between existing time spans. In this case two spans might need to be merged, if the time difference is smaller
 	 * than the timeMargin.
 	 */
 	@Override
 	public void calculate(CustomMessage m) {
 		
 		if(filterMessage(m))
 			return;
 		
 		calcFixedTimeSpan(m);
 		
 		if(m.getSourceType() != SourceType.SATELLITE){
 			return;
 		}
 //		if(m.getSourceType() != SourceType.SATELLITE){
 //			tenMinQueue.add(m);
 //			
 //			//Check ten minutes queue
 //			//Match each messages older than ten minutes with existing sat time spans.
 //			//If message matches no timespan, then put it in 6 hour queue for later check
 //			while(true){
 //				if(tenMinQueue.isEmpty())
 //					break;
 //				
 //				CustomMessage m2 = tenMinQueue.peek();
 //				//If message is older than ten minutes, try to put in sat timespan
 //				if(m.getTimestamp().getTime()-m2.getTimestamp().getTime() > 1000*60*10){
 //					tenMinQueue.poll();
 //					//if no timespan found, put in 6 hour queue
 //					if(!matchTerrestrialMessage(m2)){
 //						sixHourQueue.add(m2);
 //					}
 //					
 //				}else{
 //					break;
 //				}
 //			}
 //			//Check six hours queue
 //			//Match each messages older than six hours with existing sat time spans.
 //			//If message matches no sat-timespan, then we don't expect the timespan to ever be created
 //			//hence throw away message
 //			while(true){
 //				if(sixHourQueue.isEmpty())
 //					break;
 //				
 //				CustomMessage m2 = sixHourQueue.peek();
 //				//If message is older than six hours, try to put in sat timespan
 //				if(m.getTimestamp().getTime()-m2.getTimestamp().getTime() > 1000*60*60*6){
 //					sixHourQueue.poll();
 //					//if no timespan found, throw away message
 //					matchTerrestrialMessage(m2);
 //				}else{
 //					break;
 //				}
 //			}
 //			
 ////			System.out.println("oldest: "+tenMinQueue.peek().getTimestamp()+" newest: "+m.getTimestamp());
 //			return;
 //		}
 
 		
 		//get the right cell, or create it if it doesn't exist.
 //		Cell c = dataHandler.getCell("supersource", m.getLatitude(), m.getLongitude());
 //		if(c == null){
 //			c = dataHandler.createCell("supersource", m.getLatitude(), m.getLongitude());
 //			c.setTimeSpans(new ArrayList<TimeSpan>());
 //		}			
 //		
 //		//If no time spans exist for corresponding cell, create one
 //		if(c.getTimeSpans().isEmpty()){
 //			c.getTimeSpans().add(new TimeSpan(m.getTimestamp()));
 //		}
 //		
 //		//We can not be sure that the message belongs to the latest time span (because order is not guaranteed).
 //		//Search through list backwards, until a time span is found where first message is older than the new one.
 //		TimeSpan timeSpan = null;
 //		int timeSpanPos = 0;
 //		for (int i = c.getTimeSpans().size()-1; i >= 0; i--) {
 //			TimeSpan t = c.getTimeSpans().get(i);
 //			if(t.getFirstMessage().getTime() <= m.getTimestamp().getTime()){
 //				timeSpan = t;
 //				timeSpanPos = i;
 //			}
 //		}
 //
 //		//if no time span was found a new one has to be inserted at the beginning of the list
 //		if(timeSpan == null){
 //			timeSpan = new TimeSpan(m.getTimestamp());
 //			c.getTimeSpans().add(0,timeSpan);
 //			timeSpanPos = 0; //not necessary.. should be 0 at this point. Just to be sure.
 //		}
 //		
 //		
 //		//if time span is out dated, create new one and add it right after timeSpan.
 //		if(Math.abs(m.getTimestamp().getTime()-timeSpan.getLastMessage().getTime()) > timeMargin){
 //			timeSpan = new TimeSpan(m.getTimestamp());
 //			c.getTimeSpans().add(timeSpanPos+1,timeSpan);
 //			timeSpanPos = timeSpanPos+1;
 //			
 //		}
 //
 //		//Set the last message, if the new one is newer than the existing last message
 //		if(timeSpan.getLastMessage().getTime() < m.getTimestamp().getTime()){
 //			timeSpan.setLastMessage(m.getTimestamp());
 //			
 //			//Check if the time span needs to be merged with the next (if timeMargin is larger than time difference)
 //			if(c.getTimeSpans().size() > timeSpanPos+1){
 //				TimeSpan nextSpan = c.getTimeSpans().get(timeSpanPos+1);
 //				if(Math.abs(nextSpan.getFirstMessage().getTime() - timeSpan.getLastMessage().getTime()) <= timeMargin){
 //					//remove old timespans from list
 //					c.getTimeSpans().remove(timeSpanPos);
 //					c.getTimeSpans().remove(timeSpanPos);
 //					
 //					//add the merged time span to the list
 //					TimeSpan merged = mergeTimeSpans(timeSpan, nextSpan);
 //					c.getTimeSpans().add(timeSpanPos, merged);
 //					timeSpan = merged;
 //				}
 //			}
 //			
 //		
 //			
 //		}
 //		
 //		
 //		
 //		//Put ship mmsi in the map
 //		timeSpan.getDistinctShipsSat().put(""+m.getShipMMSI(), true);
 //		
 //		//Increment message counter
 //		timeSpan.setMessageCounterSat(timeSpan.getMessageCounterSat()+1);
 				
 	}
 	private TimeSpan mergeTimeSpans(TimeSpan span1, TimeSpan span2){
 		
 		TimeSpan merged = new TimeSpan(span1.getFirstMessage());
 		//merge two timespans
 		merged.setLastMessage(span2.getLastMessage());
 		merged.setMessageCounterSat(span1.getMessageCounterSat()+span2.getMessageCounterSat());
 		merged.addMessageCounterTerrestrialUnfiltered(span2.getMessageCounterTerrestrial());
 		Set<String> span1DistinctShips = span1.getDistinctShipsSat().keySet();
 		Set<String> span2DistinctShips = span2.getDistinctShipsSat().keySet();
 		Set<String> span1DistinctShipsTer = span2.getDistinctShipsTerrestrial().keySet();
 		Set<String> span2DistinctShipsTer = span2.getDistinctShipsTerrestrial().keySet();
 		for (String string : span1DistinctShips) {
 			merged.getDistinctShipsSat().put(string, true);
 		}
 		for (String string : span2DistinctShips) {
 			merged.getDistinctShipsSat().put(string, true);
 		}
 		for (String string : span1DistinctShipsTer) {
 			merged.getDistinctShipsTerrestrial().put(string, true);
 		}
 		for (String string : span2DistinctShipsTer) {
 			merged.getDistinctShipsTerrestrial().put(string, true);
 		}
 		
 		return merged;
 	}
 	
 	@Override
 	/**
 	 * Pretend that all messages are from same source
 	 */
 	protected Source extractBaseStation(String baseId, ReceiverType receiverType){
 		Source grid = dataHandler.getSource("supersource");
 		if (grid == null) {
 			grid = dataHandler.createSource("supersource");
 		}
 		return grid;
 	}
 	
 	/**
 	 * Rules for filtering
 	 */
 	@Override
 	public boolean filterMessage(CustomMessage customMessage){
 		
 		
 //		if(customMessage.getSog() < 3 || customMessage.getSog() > 50)
 //			return true;
 		if(customMessage.getCog() == 360){
 			return true;
 		}
 		if(isDoublet(customMessage))
 			return true;
 		
 		return false;
 	}
 
 	private boolean isDoublet(CustomMessage m){
 			String key = m.getKey();
 	
 			if(m.getSourceType() != SourceType.SATELLITE){			
 				//if message exist in sat map return true, otherwise false.
 				if(doubletBufferSat.containsKey(key)){
 					return true;
 				}
 				doubletBufferSat.put(key, true);
 				return false;
 			}else{
 				//if message exist in terrestrial map return true, otherwise false.
 				if(doubletBufferTerrestrial.containsKey(key)){
 					return true;
 				}
 				doubletBufferTerrestrial.put(key, true);		
 				return false;
 			}
 			
 		}
 	
 	 public class SortByDate implements Comparator<TimeSpan> {
 
 	        public int compare(TimeSpan a1, TimeSpan a2) {
 	            Date s1 = a1.getFirstMessage();
 	            Date s2 = a2.getFirstMessage();
 	            if(!s1.before(s2))
 	            	return 1;
 	            
 	            return -1;
 	        }
 	    }
 	 
 	 public class CustomMessageDateComparator implements Comparator<CustomMessage> {
 
 	        public int compare(CustomMessage c1, CustomMessage c2) {
 	        	
 	            Date d1 = c1.getTimestamp();
 	            Date d2 = c2.getTimestamp();
 	            if(!d1.before(d2))
 	            	return 1;
 	            
 	            return -1;
 	        }
 	    }
 
 	 public class FixedSpanCell{
 		 public double lat, lon;
 		 Map<Long, TimeSpan> timeSpans = new HashMap<Long, TimeSpan>();
 	 }
 }
