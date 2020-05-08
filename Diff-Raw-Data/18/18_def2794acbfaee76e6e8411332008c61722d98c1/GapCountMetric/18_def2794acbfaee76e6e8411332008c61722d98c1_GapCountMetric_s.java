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
 
 import java.nio.ByteBuffer;
 import java.util.logging.Logger;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.Calendar;
 
 import asl.metadata.*;
 import asl.metadata.meta_new.*;
 import asl.seedsplitter.*;
 
 import java.nio.ByteBuffer;
 import asl.util.Hex;
 
 public class GapCountMetric
 extends Metric
 {
     private static final Logger logger = Logger.getLogger("asl.seedscan.metrics.GapCountMetric");
 
     @Override public long getVersion()
     {
         return 1;
     }
 
     @Override public String getName()
     {
         return "GapCountMetric";
     }
 
     public void process()
     {
         System.out.format("\n              [ == Metric %s == ]\n", getName() ); 
    // Create a 3-channel array to use for loop
         ChannelArray channelArray = new ChannelArray("00","BHZ", "BH1", "BH2");
         ArrayList<Channel> channels = channelArray.getChannels();
 
    // Loop over channels, get metadata & data for channel and Calculate Metric
 
         for (Channel channel : channels){
 
          // Check to see that we have data + metadata & see if the digest has changed wrt the database:
             ByteBuffer digest = metricData.valueDigestChanged(channel, createIdentifier(channel));
            System.out.format("== %s: digest=%s\n", getName(), Hex.byteArrayToHexString(digest.array()) );
 
             if (digest == null) { 
                 System.out.format("%s INFO: Data and metadata have NOT changed for this channel:%s --> Skipping\n"
                                   ,getName(), channel);
                 continue;
             }
 
          // If we're here, it means we need to (re)compute the metric for this channel:
 
             ArrayList<DataSet>datasets = metricData.getChannelData(channel);
             int gapCount = datasets.size()-1;
             String dataHashString = null;
             for (DataSet dataset : datasets) {
                 dataHashString = dataset.getDigestString();
             } // end for each dataset
 
             metricResult.addResult(channel, (double)gapCount, digest);
 
 /**
             System.out.format("%s-%s [%s] %s %s-%s ", stationMeta.getStation(), stationMeta.getNetwork(),  
               EpochData.epochToDateString(stationMeta.getTimestamp()), getName(), chanMeta.getLocation(), chanMeta.getName() );
             System.out.format("Gap Count:%d %s %s\n", gapCount, chanMeta.getDigestString(), dataHashString );
 **/
         }// end foreach channel
     } // end process()
 }
 
