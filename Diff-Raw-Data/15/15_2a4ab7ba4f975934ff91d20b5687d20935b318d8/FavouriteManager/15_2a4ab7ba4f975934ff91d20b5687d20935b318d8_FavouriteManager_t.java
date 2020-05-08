 package com.engine9;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Vector;
 
 import android.content.Context;
 import android.util.Log;
 
 /*Control the file input output for the favourite*/
 
 public class FavouriteManager {
 
 	/**
 	 * Add the favourite service in to output file
 	 * 
 	 * @param fav
 	 * 		favourite service code
 	 * @param context
 	 * 		the 
 	 * 
 	 * @throws IOException
 	 * 		No output
 	 * */
 	public static void AddFavourite(String fav, String route, Context context){
 		FileOutputStream outputStream;
 		try {
 			outputStream =context.openFileOutput("favourites.txt", Context.MODE_APPEND);
 			outputStream.write(("Name:" + fav + "|" + "Route:" + route + '\n').getBytes());
 			outputStream.close();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 	
 	/**
 	 * Delete the favourite service from the group
 	 * 
 	 * @param fav
 	 * 		favourite service code in exist file
 	 * @param context
 	 * 		the 
 	 * 
 	 * @throws
 	 * 
 	 * */
 	public static void deleteFavourite(String fav, Context context){
 		new File(context.getFilesDir(), "favourites.txt");
 		try {
 			BufferedReader input = new BufferedReader(new InputStreamReader(
 					context.openFileInput("favourites.txt")));
 			String inputStr;
 			String stringBuffer = "";
 			while((inputStr = input.readLine()) !=  null){
 				String[] parts = inputStr.split("\\|");
 				Log.d("DEBUG", parts[0]);
 				if(!parts[0].split(":")[1].equals(fav)){
 					stringBuffer += (inputStr + '\n');
 				}
 			}
 			input.close();
 			
 			FileOutputStream outputStream;
 			outputStream =context.openFileOutput("favourites.txt", Context.MODE_PRIVATE);
 			outputStream.write(stringBuffer.getBytes());
 			outputStream.close();
 			
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public static void deleteAllFavourites(Context context){
 		
 		try {
 			FileOutputStream outputStream;
 			outputStream =context.openFileOutput("favourites.txt", Context.MODE_PRIVATE);
 			outputStream.write("".getBytes());
 			outputStream.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Get the favourite service from the file and construct them into list 
 	 * */
 	public static Vector<FavouriteInfo> getFavourites(Context context){
 		try{
 			Vector<FavouriteInfo> retVector = new Vector<FavouriteInfo>();
 			BufferedReader input = new BufferedReader(new InputStreamReader(
 					context.openFileInput("favourites.txt")));
 			String inputStr;
 			while((inputStr = input.readLine()) !=  null){
 				//retVector.add(inputStr);
				Log.d("DEBUG", inputStr);
 				String[] parts = inputStr.split("\\|");
 				FavouriteInfo fi = new FavouriteInfo();
 				for(String s : parts){
 					String[] vals = s.split(":");
 					if(vals[0].equals("Name")){
 						fi.name = vals[1];
 					}
 					if(vals[0].equals("Route")){
 						fi.route = vals[1];
 					}
 					if(vals[0].equals("Colour")){
 						fi.colour = vals[1];
 					}
 					if(vals[0].equals("Description")){
 						fi.description = vals[1];
 					}
 				}
 				retVector.add(fi);
 			}
 			
 			return retVector;
 		} catch(Exception e){
 			e.printStackTrace();
 			return new Vector<FavouriteInfo>();
 		}
 	}
 	
 	public static Boolean inFavourites(Context context, String fav){
 		try{
 			BufferedReader input = new BufferedReader(new InputStreamReader(
 					context.openFileInput("favourites.txt")));
 			String inputStr;
 			while((inputStr = input.readLine()) !=  null){
 				String[] parts = inputStr.split("\\|");
 				parts = parts[0].split(":");
 				if(parts[1].equals(fav)){
 					return true;
 				}
 			}
 			
 			return false;
 		} catch(Exception e){
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public static void setColour(Context context, String fav, String colour){
 		new File(context.getFilesDir(), "favourites.txt");
 		try {
 			BufferedReader input = new BufferedReader(new InputStreamReader(
 					context.openFileInput("favourites.txt")));
 			String inputStr;
 			String stringBuffer = "";
 			while((inputStr = input.readLine()) !=  null){
 				String[] parts = inputStr.split("\\|");
 				if(!parts[0].split(":")[1].equals(fav)){
 					stringBuffer += (inputStr + '\n');
 				}
 				else{
 					Boolean colourSet = false;
 					int count = 0;
 					for(String s : parts){
 						if(s.split(":")[0] == "Colour"){
 							colourSet = true;
 							stringBuffer += "Colour:" + colour;
 							if(count < parts.length - 1){
 								stringBuffer += "|";
 							}
 						}
 						else{
 							stringBuffer += s;
 							if(count < parts.length - 1){
 								stringBuffer += "|";
 							}
 						}
 					}
 					if(!colourSet){
 						stringBuffer += "Colour:" + colour;
 					}
 					stringBuffer += '\n';
 				}
 			}
 			input.close();
 			
 			FileOutputStream outputStream;
 			outputStream =context.openFileOutput("favourites.txt", Context.MODE_PRIVATE);
 			outputStream.write(stringBuffer.getBytes());
 			outputStream.close();
 			
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public static void setDescription(Context context, String fav, String description){
 		new File(context.getFilesDir(), "favourites.txt");
 		try {
 			BufferedReader input = new BufferedReader(new InputStreamReader(
 					context.openFileInput("favourites.txt")));
 			String inputStr;
 			String stringBuffer = "";
 			while((inputStr = input.readLine()) !=  null){
 				String[] parts = inputStr.split("\\|");
 				if(!parts[0].split(":")[1].equals(fav)){
 					stringBuffer += (inputStr + '\n');
 				}
 				else{
 					Boolean descriptionSet = false;
 					int count = 0;
 					for(String s : parts){
 						if(s.split(":")[0] == "Description"){
 							descriptionSet = true;
 							stringBuffer += "Description:" + description;
 							if(count < parts.length - 1){
 								stringBuffer += "|";
 							}
 							
 						}
 						else{
 							stringBuffer += s;
 							if(count < parts.length - 1){
 								stringBuffer += "|";
 							}
 						}
 						count ++;
 					}
 					if(!descriptionSet){
 						stringBuffer += "Description:" + description;
 					}
 					stringBuffer += '\n';
 				}
 			}
 			input.close();
 			
 			FileOutputStream outputStream;
 			outputStream =context.openFileOutput("favourites.txt", Context.MODE_PRIVATE);
 			outputStream.write(stringBuffer.getBytes());
 			outputStream.close();
 			
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	
 }
