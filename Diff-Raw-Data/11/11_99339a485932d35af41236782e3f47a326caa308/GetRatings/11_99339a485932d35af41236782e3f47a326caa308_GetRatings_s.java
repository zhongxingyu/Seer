 package com.where.atlas.feed.yelp;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class GetRatings {
 	static FileReader ids_fileReader = null;
 	static BufferedReader ids_buff = null;
 	static String ids_input_dir = "/home/qimeng/yelp.output/";
 
 	static FileReader json_fileReader = null;
 	static BufferedReader json_buff = null;
 	static String json_input_dir = "/home/qimeng/GetRating/inputjson/";
 	static HashMap<Long, Long> hashMap = new HashMap<Long, Long>();
 
 	static FileWriter result_fileWriter = null;
 	static String result_dir = "/home/qimeng/GetRating/outPutForTest";
 	static BufferedWriter result_buff = null;
 
 	static AtomicLong atomicN = new AtomicLong();
 	static HashMap<String, Long> id_map = new HashMap<String, Long>();
 	static String id_hash_dir = "/home/qimeng/GetRating/id_hash";
 	static FileWriter id_hash_file = null;
 	static BufferedWriter id_hash_buff = null;
 	public static final void main(String[] args){
 		try {
 			File ids_input_directory = new File(ids_input_dir);
 			File[] ids_input_files = ids_input_directory.listFiles();
 			for(int x=0; x<ids_input_files.length; x++){
 				ids_fileReader = new FileReader(ids_input_files[x]);
 				ids_buff = new BufferedReader(ids_fileReader);
 				String input_line = null;
 
 				//			test version
 				//			while((input_line = ids_buff.readLine()) != null){
 				//
 				//				String[] inputs = input_line.split(",");
 				//
 				//				Long yelp_id = Long.parseLong(inputs[0]);
 				//				Long where_id = Long.parseLong(inputs[1]);
 				//				hashMap.put(yelp_id, where_id);
 				//			}
 
 				//			generate id HashMap from id pairs, 
 				//			samples:
 				//			y:10100 1389	4 1 0 0 (0.9) <y:10100 Bank Of America> <1389 Bank Of America>
 				//			y:10100 198	4 1 0 0 (0.9) <y:10100 Bank Of America> <198 Bank Of America>
 				//			y:10100 199	4 1 0 0 (0.9) <y:10100 Bank Of America> <199 Bank Of America>
 				//			10100 y:2830	4 1 0 0 (0.9) <10100 Bank Of America> <y:2830 Bank Of America>
 				//			10100 y:3102	4 1 0 0 (0.9) <10100 Bank Of America> <y:3102 Bank Of America>
 				//			10100 y:5297	4 1 0 0 (0.9) <10100 Bank Of America> <y:5297 Bank Of America>
 				//			y:10100 y:5500	4 1 0 0 (0.9) <y:10100 Bank Of America> <y:5500 Bank Of America>
 				//			y:10125 y:10203	4 1 0 0 (0.9) <y:10125 Public Storage> <y:10203 Public Storage>
 				//			y:10125 y:1374	4 1 0 0 (0.9) <y:10125 Public Storage> <y:1374 Public Storage>
 
 				while((input_line = ids_buff.readLine()) != null){
 					String[] inputs = input_line.split("\\s".intern());
 
 					if(inputs[0].startsWith("y".intern())){
 						if(!inputs[1].startsWith("y".intern())){
 							long yelp_id = Long.parseLong(inputs[0].substring(2));
 							long where_id = Long.parseLong(inputs[1]); 
 							hashMap.put(yelp_id, where_id);
 						}
 						//else, both are yelp id, ignore wrong input
 					}
 
 					else {
 						if (inputs[1].startsWith("y".intern())){
							long yelp_id = Long.parseLong(inputs[0]);
							long where_id = Long.parseLong(inputs[1].substring(2)); 
 							hashMap.put(yelp_id, where_id);
 						}
 						//else, both are where id, ignore
 					}
 				}
 				System.out.println((x+1)+" id file processed");
 			}
 			System.out.println("HashMap done!!!!");
 
 			result_fileWriter = new FileWriter(result_dir);
 			result_buff = new BufferedWriter(result_fileWriter);
 
 			id_hash_file = new FileWriter(id_hash_dir);
 			id_hash_buff = new BufferedWriter(id_hash_file);
 			System.out.println("initialized buffers");
 
 			File json_input_directory = new File(json_input_dir);
 			final File[] json_input_files = json_input_directory.listFiles();
 
 			int counter = 0;
 			for(int i=0; i<json_input_files.length;  i++){
 				json_fileReader = new FileReader(json_input_files[i]);
 				json_buff = new BufferedReader(json_fileReader);
 
 				String json_line = null;
 				while((json_line = json_buff.readLine()) != null){
 					try {
 						JSONObject json = new JSONObject(json_line);
 						Long place_id = null;
 						if((place_id = hashMap.get(json.optLong("_id"))) != null){
 							JSONArray jsar = json.optJSONArray("reviews");
 							if(jsar != null){
 								int jsar_size = jsar.length();
 								for(int j=0; j<jsar_size; j++){
 									JSONObject user = (JSONObject)jsar.get(j);
 									String userHash = user.optString("user_id");
 									Long userId;
 									if(id_map.get(userHash) == null){
 										userId = atomicN.getAndIncrement();
 										id_map.put(userHash, userId);
 										id_hash_buff.write(userHash +","+userId);
 										id_hash_buff.newLine();
 									}
 									else {
 										userId = id_map.get(userHash);
 									}
 									result_buff.write(userId +","+place_id+","+user.optString("rating"));
 									result_buff.newLine();
 									result_buff.flush();
 								}
 							} 
 						}
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}		
 				}	
 
 				counter++;
 				if(counter % 30 == 0){
 					System.out.println(counter + " files processed");
 				}
 			}
 			System.out.println("All set!!!!!!!!");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try{
 				id_hash_buff.flush();
 				id_hash_buff.close();
 				id_hash_file.close();
 				ids_buff.close();
 				ids_fileReader.close();
 				json_buff.close();
 				json_fileReader.close();
 				result_buff.close();
 				result_fileWriter.close();
 			}
 			catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 	} 
 
 }
