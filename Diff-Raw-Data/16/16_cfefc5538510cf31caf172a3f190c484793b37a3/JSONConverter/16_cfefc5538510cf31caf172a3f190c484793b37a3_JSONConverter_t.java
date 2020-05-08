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
 import edu.poly.bxmc.betaville.model.City;
 import edu.poly.bxmc.betaville.model.Comment;
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
 
 	static{
 		designTypeMap = new HashMap<String, Class<? extends Design>>();
 
 		designTypeMap.put("audio", AudibleDesign.class);
 		designTypeMap.put("video", VideoDesign.class);
 		designTypeMap.put("model", ModeledDesign.class);
 		designTypeMap.put("sketch", SketchedDesign.class);
 		designTypeMap.put("empty", EmptyDesign.class);
 
 	}
 	
 	public static List<City> cityList(JsonParser json) {
 		List<City> cities = new ArrayList<City>();
 		try {
 			json.nextToken();
 			JsonToken token;
 			
 			boolean arrayEntered = false;
 			while((token = json.nextToken())!=JsonToken.END_OBJECT) {
 				if(token==null) {
 					System.out.println("Json returned an empty array");
 					cities = null;
 					return cities;
 				}
 				if(token == JsonToken.START_ARRAY) {
 					System.out.println("Entered Array");
 					arrayEntered = true;
 				}
 				if(arrayEntered){
 					cities.add(toCityList(json));
 					json.nextToken();
 				}
 			}
 
 		} catch (JsonParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return cities;
 	}	
 		public static City toCityList(JsonParser json) {
 			City newcity = new City("0","0","0",0);
 			
 			try {
 				json.nextToken();
 				
 				while(json.nextToken()!=JsonToken.END_OBJECT) {
 					if(json.getCurrentName()=="cityID") {
 						json.nextToken();
 						newcity.setCityID(Integer.parseInt(json.getText()));
 					}
 					else if(json.getCurrentName()=="cityName") {
 						json.nextToken();
 						newcity.setCityName(json.getText());
 					}
 					else if(json.getCurrentName()=="state") {
 						json.nextToken();
 						newcity.setCityState(json.getText());
 					}
 					else if(json.getCurrentName()=="country") {
 						json.nextToken();
 						newcity.setCityCountry(json.getText());
 					}
 					else { 
 						json.nextToken();
 					}
 				}
 			} catch (JsonParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			
 			}
 			return newcity;
 		}
 	public static List<Comment> commentList(JsonParser json) {
 		List<Comment> comments = new ArrayList<Comment>();
 		try {
 
 			json.nextToken();
 			JsonToken token;
 
 			boolean arrayEntered = false;
 			while((token = json.nextToken())!=JsonToken.END_OBJECT){
 				if(token==null) {
 					System.out.println("Json returned an empty array");
 					comments = null;
 					return comments;
 				}
 				if(token == JsonToken.START_ARRAY){
 					System.out.println("Start Array");
 					arrayEntered=true;
 				}
 
 				if(arrayEntered){
 					comments.add(toComments(json));
 					json.nextToken();
 				}
 			}
 
 		} catch (JsonParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return comments;
 	}
 
 	public static Comment toComments(JsonParser json){
 		Comment comment = new Comment(0, 0, "", "");
 
 		try {
 
 			json.nextToken();
 
 			while(json.nextToken()!=JsonToken.END_OBJECT){
 
 				if(json.getCurrentName().equals("commentid")){
 					json.nextToken();
 					comment.setID(Integer.parseInt(json.getText()));
 				}		
 				else if(json.getCurrentName().equals("designid")){
 					json.nextToken();
 					comment.setDesignID(json.getText());
 				}		
 				else if(json.getCurrentName().equals("user")) {
 					json.nextToken();
 					comment.setUser(json.getText());
 				}
 				else if(json.getCurrentName().equals("comment")){
 					json.nextToken();
 					comment.setComment(json.getText());
 				}		
 				else if(json.getCurrentName().equals("date")){
 					json.nextToken();
 					comment.setDate(json.getText());
 				}		
 				else if(json.getCurrentName().equals("repliesTo")){
 					json.nextToken();
 					comment.setRepliesTo(Integer.parseInt(json.getText()));
 				}		
 				
 				else{
 					// skip this token
 					json.nextToken();
 				}
 			}
 
 			
 		
 		} catch (JsonParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		
 		}
 
 		return comment;
 	}
 
 
 	public static List<Design> toDesignList(JsonParser json){
 		List<Design> designs = new ArrayList<Design>();
 
 		try {
 
 			json.nextToken();
 			JsonToken token;
 
 			boolean arrayEntered = false;
 			while((token = json.nextToken())!=JsonToken.END_OBJECT){
 				if(token==null) {
 					System.out.println("Json returned an empty array");
					//designs = null;
 					return designs;
 				}
 				if(token == JsonToken.START_ARRAY){
 					System.out.println("Start Array");
 					arrayEntered=true;
 				}
 
 				if(arrayEntered){
					Design design = toDesign(json);
					if(design.getName()!=null)designs.add(design);
 					json.nextToken();
 				}
 			}
 
 		} catch (JsonParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return designs;
 	}
 
 	public static Design toDesign(JsonParser json){
 
 		Design tempDesign = new Design();
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
 				
 				// See if this is a specified type of design
 				else if(json.getCurrentName().equals("designtype")){
 					json.nextToken();
 					
 					// what type of design is this?
 					Class<? extends Design> clazz = designTypeMap.get(json.getText());
 					
 					// create a new instance
 					Design newInstance = clazz.newInstance();
 					
 					// load existing information
 					newInstance.load(tempDesign);
 					
 					// assign new new instance to tempDesign
 					tempDesign = newInstance;
 				}
 				
 				/*
 				 * There is also the possibility that proposal information
 				 * has been received with the designs so we need to check
 				 * for it
 				 */
 				
 				
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
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
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
 
