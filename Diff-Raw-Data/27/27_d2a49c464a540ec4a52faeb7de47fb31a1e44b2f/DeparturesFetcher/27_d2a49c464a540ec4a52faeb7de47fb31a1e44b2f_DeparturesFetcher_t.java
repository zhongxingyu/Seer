 package com.papagiannis.tuberun.fetchers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import com.papagiannis.tuberun.LinePresentation;
 import com.papagiannis.tuberun.LineType;
 
 public class DeparturesFetcher extends Fetcher {
 
 	private static final long serialVersionUID = 1L;
 	private AtomicBoolean isFirst = new AtomicBoolean(true);
 	private transient RequestTask task=null;
 	@Override
 	public void update() {
 		boolean first = isFirst.compareAndSet(true, false);
 		if (!first)
 			return; // only one at a time
 		String request_query = "http://www.tfl.gov.uk/tfl/livetravelnews/departureboards/tube/default.asp?LineCode=" + line +
                 "&StationCode=" + station_code + "&switch=off";
 		task=new RequestTask(new HttpCallback() {
 			public void onReturn(String s) {
 				getDeparturesCallBack(s);
 			}
 		});
 		task.execute(request_query);
 	}
 
 	private Date last_update=new Date();
 	@Override
 	public Date getUpdateTime() {
 		return last_update;
 	}
 	
 	
 	protected int update_counter=0;
     protected String line, station_code,station_nice;
     
     protected ArrayList<HashMap<String, String>> departures = new ArrayList<HashMap<String, String>>();
 
     public DeparturesFetcher(LineType line, String stationcode, String stationnice)
     {
         this.line = LinePresentation.getDeparturesRespresentation(line);
         station_code = stationcode;
         station_nice = stationnice;
     }
     private void getDeparturesCallBack(String reply)
     {
         try
         {
             departures.clear();
 
             while (true)
             {
                 int i = reply.indexOf("Departure times for");
                 if (i != -1)
                 {
                     reply = reply.substring(i);
                     i = reply.indexOf("Departure times for");
                 }
                 int j = reply.indexOf("<td class=\"destination\">");
                 if (i == -1 || j == -1 || j < i) break;
                 String dirpart = reply.substring(i, j);
 
                 String start = "<caption xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">";
                 String end = "</caption>";
                 int si = dirpart.indexOf(start) + start.length();
                 int ei = dirpart.indexOf(end);
                 String plat = dirpart.substring(si, ei);
 
                 HashMap<String, String> train1 = getInfo(reply);
                 j = reply.indexOf("<td class=\"destination\">") + 24;
                 reply = reply.substring(j);
                 if (train1 != null) train1.put("platform", plat);
                 HashMap<String, String> train2 = getInfo(reply);
                 j = reply.indexOf("<td class=\"destination\">") + 24;
                 if (train2 != null) train2.put("platform", plat);
                 reply = reply.substring(j);
                 HashMap<String, String> train3 = getInfo(reply);
                 if (train3 != null) train3.put("platform", plat);
                 //The direction exists, go on
                 if (train1 != null) departures.add(train1);
                 if (train2 != null) departures.add(train2);
                 if (train3 != null) departures.add(train3);
             }
             notifyClients();
             isFirst.set(true);
         }
         catch (Exception e)
         {
             notifyClients();
             isFirst.set(true);
         }
     }
     private HashMap<String, String> getInfo(String reply)
     {
         HashMap<String, String> train1 = new HashMap<String, String>();
         int i, j;
 
         String q1 = "<td class=\"destination\">";
         i = reply.indexOf(q1) + q1.length();
         if (i == q1.length() - 1) return null;
 
         reply = reply.substring(i);
         j = reply.indexOf("<");
         String r = reply.substring(0, j);
         r = r.replaceAll( "\n", "");
         r = r.replaceAll( "\t", "");
         r = r.replaceAll("\r", "");
         r = r.replaceAll("&amp;", "and");
         r = r.substring(3);
         r = r.trim();
         train1.put("destination", r);
         reply = reply.substring(j);
 
         String q2 = "<td class=\"message\">";
         i = reply.indexOf(q2) + q2.length();
         reply = reply.substring(i);
         j = reply.indexOf("<");
         String s = reply.substring(0, j);
         s = s.replaceAll("&amp;", "and");
         train1.put("position", s);
         reply = reply.substring(j);
 
         String q3 = "<td class=\"time\">";
         i = reply.indexOf(q3) + q3.length();
         reply = reply.substring(i);
         j = reply.indexOf("<");
         r = reply.substring(0, j);
         r = r.replaceAll("\n", "");
         r = r.replaceAll("\t", "");
         r = r.replaceAll("\r", "");
         r = r.trim();
         train1.put("time", r);
         reply = reply.substring(j);
 
         return train1;
     }
 
     public HashMap<String, ArrayList<HashMap<String, String>>> getDepartures()
     {
         HashMap<String, ArrayList<HashMap<String, String>>> categorised = new HashMap<String, ArrayList<HashMap<String, String>>>();
         for (HashMap<String, String> train : departures)
         {
             if (!categorised.containsKey(train.get("platform")))
             {
                 categorised.put(train.get("platform") , new ArrayList<HashMap<String, String>>());
             }
             categorised.get(train.get("platform")).add(train);
         }
         return categorised;
 
     }
     
     public ArrayList<HashMap<String, String>> getDepartures(String platform)
     {
         ArrayList<HashMap<String, String>> result=new ArrayList<HashMap<String,String>>();
         for (HashMap<String, String> d : departures)
         {
            if (d.get("platform").equalsIgnoreCase(platform))
             {
                 result.add(d);
             }
         }
         return result;
     }
     
     @Override
     public void abort() {
     	isFirst.set(true);
     	if (task!=null) task.cancel(true);
     }
 
 }
