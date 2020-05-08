 /**  
  *  Betaville JSON Connection
  *  Copyright (C) 2011 Skye Book
  *  
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.skyebook.betaville.json;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonToken;
 
 import edu.poly.bxmc.betaville.jme.map.GPSCoordinate;
 import edu.poly.bxmc.betaville.jme.map.ILocation;
 import edu.poly.bxmc.betaville.model.AudibleDesign;
 import edu.poly.bxmc.betaville.model.Design;
 import edu.poly.bxmc.betaville.model.EmptyDesign;
 import edu.poly.bxmc.betaville.model.ModeledDesign;
 import edu.poly.bxmc.betaville.model.SketchedDesign;
 import edu.poly.bxmc.betaville.model.VideoDesign;
 
 /**
  * Methods for converting JSON objects into Betaville data structures
  * @author Skye Book
  *
  */
 public class JSONConverter{
 
 	private static HashMap<String, Class<? extends Design>> designTypeMap;
 
 	private static Design tempDesign;
 
 	static{
 		designTypeMap = new HashMap<String, Class<? extends Design>>();
 
 		designTypeMap.put("audio", AudibleDesign.class);
 		designTypeMap.put("video", VideoDesign.class);
 		designTypeMap.put("model", ModeledDesign.class);
 		designTypeMap.put("sketch", SketchedDesign.class);
 		designTypeMap.put("empty", EmptyDesign.class);
 
 		tempDesign = new Design();
 
 	}
 
 	public static List<Design> toDesignList(JsonParser json){
 		List<Design> designs = new ArrayList<Design>();
 
 		try {
 			json.nextToken();
			
			while(json.nextToken()!=JsonToken.END_OBJECT){
				designs.add(toDesign(json));
				json.nextToken();
 			}
			
 		} catch (JsonParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		
		System.out.println(designs.size() + " designs retrieved");
 
 		return designs;
 	}
 
 	public static Design toDesign(JsonParser json){
 
 		Design design = null;
 
 		try {
 
 			json.nextToken();
 
 			while(json.nextToken()!=JsonToken.END_OBJECT){
 
 				if(json.getCurrentName().equals("designID")){
 					json.nextToken();
 					tempDesign.setID(Integer.parseInt(json.getText()));
 				}
 				else if(json.getCurrentName().equals("name")){
 					json.nextToken();
 					tempDesign.setName(json.getText());
 				}
 				else if(json.getCurrentName().equals("filepath")){
 					json.nextToken();
 					tempDesign.setFilepath(json.getText());
 				}
 				else if(json.getCurrentName().equals("cityID")){
 					json.nextToken();
 					tempDesign.setCityID(Integer.parseInt(json.getText()));
 				}
 				else if(json.getCurrentName().equals("address")){
 					json.nextToken();
 					tempDesign.setAddress(json.getText());
 				}
 				else if(json.getCurrentName().equals("user")){
 					json.nextToken();
 					tempDesign.setUser(json.getText());
 				}
 				else if(json.getCurrentName().equals("coordinate")){
 					json.nextToken();
 					tempDesign.setCoordinate(toLocation(json).getUTM());
 				}
 				else if(json.getCurrentName().equals("date")){
 					json.nextToken();
 					tempDesign.setDateAdded(json.getText());
 				}
 				else if(json.getCurrentName().equals("description")){
 					json.nextToken();
 					tempDesign.setDescription(json.getText());
 				}
 				else if(json.getCurrentName().equals("url")){
 					json.nextToken();
 					tempDesign.setURL(json.getText());
 				}
 				else{
 					// skip this token
 					json.nextToken();
 				}
 			}
 
 			design = new Design();
 			design.load(tempDesign);
 
 		} catch (JsonParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return design;
 	}
 
 	public static ILocation toLocation(JsonParser json) throws JsonParseException, IOException{
 		GPSCoordinate gps = new GPSCoordinate(0, 0, 0);
 
 		while(json.nextToken() != JsonToken.END_OBJECT){
 
 			if(json.getCurrentName().equals("lat")){
 				json.nextToken();
 				gps.setLatitude(json.getDoubleValue());
 			}
 			else if(json.getCurrentName().equals("lon")){
 				json.nextToken();
 				gps.setLongitude(json.getDoubleValue());
 			}
 			else if(json.getCurrentName().equals("alt")){
 				json.nextToken();
 				gps.setLongitude(json.getDoubleValue());
 			}
 
 		}
 
 		return gps;
 	}
 }
