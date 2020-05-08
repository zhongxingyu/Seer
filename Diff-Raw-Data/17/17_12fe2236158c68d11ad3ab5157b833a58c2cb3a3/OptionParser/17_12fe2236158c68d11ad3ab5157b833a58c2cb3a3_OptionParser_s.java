 /**
  * 
  */
 package shtaag.network.curl.impl.parser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import shtaag.network.curl.impl.option.Option;
 import shtaag.network.curl.impl.option.OptionEntity;
 
 /**
  * @author takei_s
  * @date 2011/12/25
  */
 public class OptionParser {
 	
 	public static List<OptionEntity> parse(String[] str) {
 	
 		List<OptionEntity> resultList = new ArrayList<OptionEntity>();
 		for (int i = 0; i < str.length; i++) {
 			Option option = null;
 			if ((option = Option.check(str[i])) != null) {
 				if (Option.check(str[i+1]) != null) {
 					System.out.println("no Value for: " + str[i]);
 				}
 				OptionEntity entity = new OptionEntity(option, str[i + 1]);
 				resultList.add(entity);
 			}
 		}
 	
 		return resultList;
 		
 	}
 	
 }
