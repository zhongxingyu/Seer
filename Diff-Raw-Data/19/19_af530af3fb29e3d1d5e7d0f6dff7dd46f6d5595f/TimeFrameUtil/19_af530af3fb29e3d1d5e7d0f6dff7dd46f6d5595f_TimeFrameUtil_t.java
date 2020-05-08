 /*
     TrayRSS - simply notification of feed information
     (c) 2009-2011 TrayRSS Developement Team
     visit the project at http://trayrss.nullpointer.at/
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program. If not, see <http://www.gnu.org/licenses/>.
 
  */
 package at.nullpointer.trayrss.configuration.timeframes;
 
import java.util.HashSet;
 import java.util.Set;
 
 import at.nullpointer.trayrss.configuration.model.SingleTimeFrame;
 
 /**
  * This class is an utility class to konvert different represenations of time frames
  * 
  * @author Thomas Pummer
  */
 public class TimeFrameUtil {
 
 	/**
 	 * This method is used to extract SingleTimeFrames from a String by splitting them
 	 * up at the seperator expression " " or ";"
 	 * 
 	 * @param frameString
 	 * @return
 	 */
 	public static Set<SingleTimeFrame> stringToSingleTimeFrame(String frameString) {
 		
		Set<SingleTimeFrame> result = new HashSet<SingleTimeFrame>();
 		
 		String[] splitted = frameString.trim().split(" |;");
 		
 		for(String timeFrame : splitted){
 			result.add(new SingleTimeFrame(timeFrame.split("-")));
 		}
 		
 		return result;
 	}
 
 	/**
 	 * This method is used to transform a set of {@link}SingleTimeFrame to a String
 	 * representation that can be stored in a property file
 	 * 
 	 * @param timeFrames
 	 * @return
 	 */
 	public static String singleTimeFrameToString(Set<SingleTimeFrame> timeFrames) {
 		
 		StringBuilder result = new StringBuilder();
 		
 		for(SingleTimeFrame singleFrame : timeFrames){
 			if(result.length() > 0){
 				result.append(";");
 			}
 			result.append(singleFrame.toString());
 		}
 		
 		return result.toString();
 	}
 
 }
