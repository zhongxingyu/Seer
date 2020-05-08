 package com.qut.spc.feedInTariff;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.qut.spc.postcode.PostcodeUtil;
 
 public class FeedInTariffProvider {
 
 	private static Map<String,Double> locationToFeedInTariffIntensity=new HashMap<String, Double>(){
 		{
 			put("4000",0.44); //QLD
 			put("5000",0.44); //SA
 			put("2000",0.103); //NSW
 			//put("3000",new double[]{5.58,1000}); //VIC
 			//put("2900",new double[]{7.33,1000}); //ACT
 			put("6000",0.44); //WA
 			put("0800",0.2177); //NT
 			
 		}};
 		
 		public static double getFeedInTariffByPostcode(String location) throws IllegalArgumentException {
 			if(PostcodeUtil.validatePostcode(location)){
 				String postcode =PostcodeUtil.transformPostcode(location);
 				if(locationToFeedInTariffIntensity.containsKey(postcode))
					return locationToFeedInTariffIntensity.get(postcode);			
 			}
 			
 			return average(locationToFeedInTariffIntensity.values());
 		}
 	
 		private static double average(Collection<Double> values) {
 			double sum=0;
 			for(double d:values){
 				sum+=d;
 			}
 			return sum/values.size();
 		}
 
 }
