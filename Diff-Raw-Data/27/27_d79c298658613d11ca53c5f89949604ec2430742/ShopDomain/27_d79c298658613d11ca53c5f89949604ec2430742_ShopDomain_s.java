 package org.helper.domain;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
 import java.util.ArrayList;
import java.util.Date;
 import java.util.List;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 public class ShopDomain implements Serializable {
 	private static final long serialVersionUID = -4482579886995409763L;
 	private static List<CropDomain> cropList = new ArrayList<CropDomain>();
 
 	static {
 		File shopFile = new File("resource/shop.json");
 		InputStreamReader inputStreamReader = null;
 		try {
 			inputStreamReader = new InputStreamReader(new FileInputStream(
 					shopFile));
 			JSONObject shopJson = (JSONObject) new JSONParser()
 					.parse(inputStreamReader);
 			inputStreamReader.close();
 
 			JSONArray cropsArray = (JSONArray) shopJson.get("1");
 			cropList.clear();
 			for (Object jsonUnit : cropsArray) {
 				CropDomain crop = new CropDomain();
 				crop.setcId(String.valueOf(((JSONObject) jsonUnit).get("cId")));
 				crop.setcName(String.valueOf(((JSONObject) jsonUnit)
 						.get("cName")));
 				crop.setcType(String.valueOf(((JSONObject) jsonUnit)
 						.get("cType")));
 				crop.setGrowthCycle(String.valueOf(((JSONObject) jsonUnit)
 						.get("growthCycle")));
 				crop.setMaturingTime(String.valueOf(((JSONObject) jsonUnit)
 						.get("maturingTime")));
 				crop.setExpect(String.valueOf(((JSONObject) jsonUnit)
 						.get("expect")));
 				crop.setOutput(String.valueOf(((JSONObject) jsonUnit)
 						.get("output")));
 				crop.setSale(String.valueOf(((JSONObject) jsonUnit).get("sale")));
 				crop.setPrice(String.valueOf(((JSONObject) jsonUnit)
 						.get("price")));
 				crop.setFBPrice(String.valueOf(((JSONObject) jsonUnit)
 						.get("FBPrice")));
 				crop.setcLevel(String.valueOf(((JSONObject) jsonUnit)
 						.get("cLevel")));
 				crop.setCropExp(String.valueOf(((JSONObject) jsonUnit)
 						.get("cropExp")));
 				crop.setcCharm(String.valueOf(((JSONObject) jsonUnit)
 						.get("cCharm")));
 				crop.setCropChr(String.valueOf(((JSONObject) jsonUnit)
 						.get("cropChr")));
 				cropList.add(crop);
 			}
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static String getGrowthCycle(String cId) {
 		int cropId = Integer.parseInt(cId);
 		for (CropDomain cd : cropList) {
 			if (Integer.parseInt(cd.getcId()) == cropId) {
 				return cd.getGrowthCycle();
 			}
 		}
 		return "";
 	}
 
 	public static String getCropName(String cId) {
 		int cropId = Integer.parseInt(cId);
 		for (CropDomain cd : cropList) {
 			if (Integer.parseInt(cd.getcId()) == cropId) {
 				return cd.getcName();
 			}
 		}
 		return "";
 	}
 }
