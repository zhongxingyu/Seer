 package edu.cmu.mse.aes.project1.bussiness;
 
 /*
  * author: Rui Li
  * 
  */
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import edu.cmu.mse.aes.project1.data.Bike;
 import edu.cmu.mse.aes.project1.data.Componentinfo;
 import edu.cmu.mse.aes.project1.dataaccess.XMLIntegrator;
 import edu.cmu.mse.aes.project1.dataaccess.XMLProcessor;
 
 public class ACMEBicyle {
 
 	private final String url = "http://bikereviews.com/road-bikes/";
 	private final String configFileName = "data/config.txt";
 	private static HashMap<String, String> brandToURLMap = new HashMap<String, String>();
 	private static HashMap<String, ArrayList<String>> brandModelMap = new HashMap<String, ArrayList<String>>();
 
 	public static HashMap<String, ArrayList<String>> getBrandModelMap() {
 		return brandModelMap;
 	}
 
 	public static void setBrandToURLMap(HashMap<String, String> brandToURLMap) {
 		ACMEBicyle.brandToURLMap = brandToURLMap;
 	}
 
 	public static void setBrandModelMap(
 			HashMap<String, ArrayList<String>> brandModelMap) {
 		ACMEBicyle.brandModelMap = brandModelMap;
 	}
 
 	public static void main(String[] args) {
 	
 		ACMEBicyle acmeBicyle = new ACMEBicyle();
 		
 		//get system init
 		acmeBicyle.init();
 		//if choose do not init, need to fix the config for persistence all the supported brands and models 
 		//not 
 		
 		
 		ACMETransform transForm = new ACMETransform(
 				acmeBicyle.getBrandModelMap());
 		
 		while (true) {
 			System.out.println("here is our supported brands:");
 			transForm.supportBrands();
 			System.out.println("please select the brand:");
 			BufferedReader buf = new BufferedReader(new InputStreamReader(
 					System.in));
 			String selectedbrand = "";
 			try {
 				selectedbrand = buf.readLine();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			System.out.println("here is the the models with the brand:"
 					+ selectedbrand);
 			transForm.supportedModels(selectedbrand);
 			BufferedReader buf2 = new BufferedReader(new InputStreamReader(
 					System.in));
 			int selectedModelNumber = 0;
 			String selectedModel = "";
 			try {
 				selectedModelNumber = Integer.parseInt(buf2.readLine());
 				selectedModel = brandModelMap.get(selectedbrand).get(
						selectedModelNumber - 1);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			System.out.println("your selected models"+selectedModel);
 			transForm.viewCertainModel(selectedModel);
 		}
 	}
 
 	// System init, get all the data ready.
 
 	void init() {
 		DataFetcher dataFetcher = new DataFetcher();
 		DataFilter dataFilter = new DataFilter();
 		String rawData = dataFetcher.doPost(url);
 		brandToURLMap = dataFilter.filterDataForInternalUse(rawData,
 				RegualExpression.RegxForFilterLinks);
 		int count = 0;
 		for (String eachBrand : brandToURLMap.keySet()) {
 			++count;
 			HashMap<String, String> currentBrand2010linkMap = new HashMap<String, String>();
 			if (eachBrand.contains("raleigh") || eachBrand.contains("giant")
 					|| eachBrand.contains("felt")
 					|| eachBrand.contains("specialized")
 					|| eachBrand.contains("cannondale")
 					|| eachBrand.contains("gary-fisher")) {
 				currentBrand2010linkMap = dataFilter.filterDataForInternalUse(
 						dataFetcher.doPost(url + eachBrand),
 						RegualExpression.regx2ForDumppages);
 
 			} else
 				currentBrand2010linkMap = dataFilter.filterDataForInternalUse(
 						dataFetcher.doPost(url + eachBrand),
 						RegualExpression.regx2);
 
 			ArrayList<Bike> bikesforoneBrand = new ArrayList<Bike>();
 
 			for (String currentBrand2010link : currentBrand2010linkMap.keySet()) {
 
 				HashMap<String, String> currentBrand2010withModellinkMap = new HashMap<String, String>();
 				currentBrand2010withModellinkMap = dataFilter
 						.filterDataForInternalUse(
 								dataFetcher.doPost(url + currentBrand2010link),
 								RegualExpression.regx3);
 				// /System.out.println("currentBrandin2010" +
 				// currentBrand2010link);
 				ArrayList<String> bikeModelsForCertainBrand = new ArrayList<String>();
 				for (String currentBrand2012withSpecificModel : currentBrand2010withModellinkMap
 						.keySet()) {
 					// System.out.println("currentBrand 2011 with SpecificModel:"
 					// + currentBrand2012withSpecificModel);
 
 					String certainModelPageSource = dataFetcher.doPost(url
 							+ currentBrand2012withSpecificModel);
 					Bike b = new Bike();
 					b.setBrand(eachBrand);
 
 					String currentModel = dataFilter.extract(dataFilter
 							.filterData(certainModelPageSource,
 									RegualExpression.regxForModel),
 							RegualExpression.regxCleanModel);
 					bikeModelsForCertainBrand.add(currentModel);
 
 					Componentinfo c = new Componentinfo();
 					c.setBrakeset(dataFilter.extract(certainModelPageSource,
 							RegualExpression.regxForBrakes));
 					c.setHandlebars(dataFilter.extract(certainModelPageSource,
 							RegualExpression.regxForHanlebar));
 					c.setHeadset(dataFilter.extract(certainModelPageSource,
 							RegualExpression.regxForHeadSet));
 					c.setSaddle(dataFilter.extract(certainModelPageSource,
 							RegualExpression.regxForSaddle));
 					c.setSeatpost(dataFilter.extract(certainModelPageSource,
 							RegualExpression.regxForSeatPost));
 					c.setStem(dataFilter.extract(certainModelPageSource,
 							RegualExpression.regxForStem));
 
 					b.setComponentinfo(c);
 					b.setRating(dataFilter.extract(dataFilter.filterData(
 							certainModelPageSource,
 							RegualExpression.regxForrating),
 							RegualExpression.regxCleanRating)
 							+ " out of 5");
 					b.setForkmaterial(dataFilter.filterData(
 							certainModelPageSource,
 							RegualExpression.regxForFrameMaterial));
 					b.setFramesize(dataFilter.extract(certainModelPageSource,
 							RegualExpression.regxForFrameSize));
 					b.setModel(currentModel);
 					b.setPrice(dataFilter.filterData(certainModelPageSource,
 							RegualExpression.regxForPrice));
 					b.setForkmaterial(dataFilter.extract(
 							certainModelPageSource,
 							RegualExpression.regxForFork));
 					// b.printinfo();
 					bikesforoneBrand.add(b);
 				}
 				System.out.println("#"+eachBrand+","+bikeModelsForCertainBrand);
 				brandModelMap.put(eachBrand, bikeModelsForCertainBrand);
 
 			}
 			
 			//writeMapToFile(brandModelMap);
 			
 			
 			// when all the models of such brand been retrieved, call xml
 			// processor to save one xml file
 
 			System.out.println("for this brand, we have "
 					+ bikesforoneBrand.size() + " bikes:" + eachBrand + "no "
 					+ count);
 			XMLProcessor xmlprocessor = new XMLProcessor();
 			xmlprocessor.saveIntoXML(bikesforoneBrand);
 
 			// when all the small xml created for all the brands, integrate the
 			// xml into a big one.
 			XMLIntegrator xmlIntegrator = new XMLIntegrator();
 			xmlIntegrator.integrateXMLs("hello");
 
 		}
 
 	}
 
 	private void writeMapToFile(HashMap<String, ArrayList<String>> configMap) {
 		// the format like this: #brand1:model1,model2...model3#brand2:model1...
 		String s = new String();
 		for (String brand : configMap.keySet()) {
 			s = "#" + brand + ",";
 			for (String model : configMap.get(brand)) {
 				s = s + model + ",";
 			}
 
 			try {
 				File f = new File(configFileName);
 				if (f.exists()) {
 					f.delete();
 				} else {
 
 					f.createNewFile();// create the file if it do not exists.
 				}
 
 				BufferedWriter output = new BufferedWriter(new FileWriter(f));
 				output.write(s);
 				output.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	private void bulidMap() {
 		File f = new File(configFileName);
 		String context;
 		if (f.exists()) {
 			try {
 				BufferedReader input = new BufferedReader(new FileReader(f));
 				context = input.readLine();
 				setBrandModelMap( buildMapFromString(context));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		} else {
 			System.out.println("what the hell");
 		}
 
 	}
 
 	private HashMap<String, ArrayList<String>> buildMapFromString(String str) {
 
 		HashMap<String, ArrayList<String>> tmpHashMap = new HashMap<String, ArrayList<String>>();
 		//each element in this array, it's brand and models.
 		String[] infos = str.split("#");
 		for (int i = 0; i < infos.length; i++) {
 			ArrayList<String> modelsArr = new ArrayList<String>();
 			//each element in the arr, it's either brand or model
 			String[] pieces=infos[i].split(",");
 			for (int j = 1; j < infos.length; j++) {
 				modelsArr.add(pieces[j]);
 			}
 			tmpHashMap.put(pieces[0], modelsArr);
 
 		}
 
 		return tmpHashMap;
 
 	}
 
 }
