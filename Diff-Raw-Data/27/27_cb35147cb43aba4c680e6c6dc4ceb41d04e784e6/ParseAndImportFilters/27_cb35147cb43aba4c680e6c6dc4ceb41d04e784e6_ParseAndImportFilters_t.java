 package fi.aalto.lounaspaikka.filters;
 
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 import fi.aalto.lounaspaikka.objectfiles.ObjectsContainer;
 
 public class ParseAndImportFilters {
 
 	public void parseandimport(String json) {
 	try {
 		JsonParser jsonParser = new JsonParser();
 		int arraysize =  jsonParser.parse(json).getAsJsonArray().size();
 		int counter=0;
		ObjectsContainer.filter.clear();
 		while (arraysize>counter) {
 			JsonObject filter=jsonParser.parse(json).getAsJsonArray().get(counter).getAsJsonObject();
 		filterArrayObject filterO = new filterArrayObject();
 		String filterstring=filter.get("filter").getAsString();
 		filterO.filter=filterstring;
 		filterO.filterweight=filter.get("filterweight").getAsInt();
 		counter++;
 		ObjectsContainer.filter.add(filterO);
 		}
 	} catch (Throwable t)  {
 		
 	}
 	
 	
 	}
 	
 	
 	
 	
 	
 	
 	
 }
