 /*
  * Copyright 2012, United States Geological Survey or
  * third-party contributors as indicated by the @author tags.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/  >.
  *
  */
 package asl.seedscan.metrics;
 
 import asl.metadata.Channel;
 import asl.metadata.Station;
 import asl.metadata.meta_new.StationMeta;
 
 import java.nio.ByteBuffer;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Set;
 import java.util.logging.Logger;
 
 public class MetricResult
 {
     private static final Logger logger = Logger.getLogger("asl.seedscan.metrics.MetricResult");
 
     private String metricName;
     private Calendar date;
     private Station station;
     private Hashtable<String, Double> valueMap;
     private Hashtable<String, ByteBuffer> digestMap;
 
     public MetricResult(StationMeta stationInfo, String metricName)
     {
     	this.metricName = metricName;
    	this.date = (Calendar)stationInfo.getTimestamp().clone();
     	this.station = new Station(stationInfo.getNetwork(), stationInfo.getStation());
         this.valueMap = new Hashtable<String, Double>();
         this.digestMap = new Hashtable<String, ByteBuffer>();
     }
     
     public String getMetricName()
     {
     	return metricName;
     }
     
     public Calendar getDate()
     {
     	return date;
     }
     
     public Station getStation()
     {
     	return station;
     }
     
     public void addResult(Channel channel, Double value, ByteBuffer digest)
     {
         addResult(createResultId(channel), value, digest);
     }
     
     public void addResult(Channel channelA, Channel channelB, Double value, ByteBuffer digest)
     {
         addResult(createResultId(channelA, channelB), value, digest);
     }
     
     public void addResult(String id, Double value, ByteBuffer digest)
     {
         valueMap.put(id, value);
         digestMap.put(id, digest);
     }
 
     public Double getResult(String id)
     {
         return valueMap.get(id);
     }
     
     public ByteBuffer getDigest(String id)
     {
     	return digestMap.get(id);
     }
 
     public Enumeration<String> getIds()
     {
         return valueMap.keys();
     }
 
     public Set<String> getIdSet()
     {
         return valueMap.keySet();
     }
 
  // Static methods
     public static String createResultId(Channel channel)
     {
     	return String.format("%s,%s", channel.getLocation(), channel.getChannel());
     }
     
     public static String createResultId(Channel channelA, Channel channelB)
     {
     	return String.format("%s-%s,%s-%s", channelA.getLocation(), channelB.getLocation(),
     										channelA.getChannel(),  channelB.getChannel());
     }
     
     public static Channel createChannel(String id)
     {
     	Channel channel = null;
     	String[] parts = id.split(",");
     	if (parts.length == 2) {
     		channel = new Channel(parts[0], parts[1]);
     	}
     	return channel;
     }
 }
 
