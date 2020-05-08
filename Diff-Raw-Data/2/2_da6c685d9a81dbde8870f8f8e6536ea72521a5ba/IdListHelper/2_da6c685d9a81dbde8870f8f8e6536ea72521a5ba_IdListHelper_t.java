 package de.hska.shareyourspot.android.helper;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class IdListHelper {
 	
 	public static String addIdtoIdString(String idList, Long id)
 	{
		return idList += ";"+ id.toString();
 	}
 	
 	public static List<Long> StringToIdList(String list)
 	{
 		String[] stringList = list.split(";");
 		List<Long> returnlist = new ArrayList<Long>();
 		
 		for(String s : stringList)
 		{
 			Long l = Long.parseLong(s);
 			if(l != null)
 			{
 			returnlist.add(l);
 			}
 		}
 		
 		return returnlist;
 	}
 
 	public static String ListToString(List<Long> usersInParty) {
 		String returnString = "";
 		
 		for(Long l : usersInParty)
 		{
 			returnString += l.toString() + ";";
 		}
 		
 		return returnString;
 	}
 
 	public static String AddSingleIdToString(String list, Long id) {
 		if(list == null || list == "")
 			return id.toString() + ";";
 		
 		return list += id.toString() + ";";
 	}
 }
