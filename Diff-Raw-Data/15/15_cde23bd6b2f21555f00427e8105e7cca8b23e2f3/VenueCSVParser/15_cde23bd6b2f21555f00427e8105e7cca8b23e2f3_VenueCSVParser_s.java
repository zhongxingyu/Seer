 package com.amazinglystupidsoftware.arewegonnaburn.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.util.Log;
 
 import com.amazinglystupidsoftware.arewegonnaburn.model.service.VenueServiceModel;
 
 public class VenueCSVParser {
 	
 	private static VenueCSVParser _INSTANCE;
 
 	public static VenueCSVParser getInstance() {
 		if (_INSTANCE == null)
 			_INSTANCE = new VenueCSVParser();
 
 		return _INSTANCE;
 	}
 	
 	public static class Safe<T>
 	{
 		public T get(T[] array, int index)
 		{
 			if (index < array.length)
 				return array[index];
 			
 			Log.w(VenueCSVParser.class.getSimpleName(), "Parse error: missing "+index+" field");
 			return null;
 		}
 		
 	}
 
 	protected List<VenueServiceModel> getVenueServiceModels() {
 		List<VenueServiceModel> list = new ArrayList<VenueServiceModel>();
 		for (String line : VenueCSVStub.getInstance().getLines())
 			list.add(new VenueServiceModel(line.split(";")));
 		return list;
 	}
 	
 
 	protected int parseLeft (String str)
 	{
 		int i;
 		for (i=0;(i<str.length() && Character.isDigit(str.charAt(i)));i++);
 		try {
 			return Integer.parseInt(str.substring(0, i));
 		}
 		catch (NumberFormatException e)
 		{
 			Log.e(VenueCSVParser.class.getSimpleName(),"Invalid number: "+str+".");
 			return -1;
 		}
 	}
 
 
 	public String humanizeString(String string) {
		return string.replace("\"", "").trim();
 	}
 
 }
