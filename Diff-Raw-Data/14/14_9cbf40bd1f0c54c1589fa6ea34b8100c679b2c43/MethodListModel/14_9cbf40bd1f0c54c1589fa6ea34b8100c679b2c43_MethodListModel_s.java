 package ogx.model;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Vector;
 
 import javax.swing.ListModel;
 import javax.swing.event.ListDataListener;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class MethodListModel implements ListModel<MethodModel> {
 	
 	Vector<MethodModel> list = new Vector<MethodModel>();
 	
 	public MethodListModel() {
 		// it will be created by controller for use by view
 		//
 		// temporary for testing
 //		try {
 //			addMethodModel(new MethodModel(new JSONObject("{\"author\":{\"email\":\"j.doe@example.com\",\"first_name\":\"John\",\"id\":0,\"last_name\":\"Doe\"},\"description\":\"Adds a new layer when provided with image, name and default value.\",\"id\":\"{521FEFE6-C9D3-485F-B411-83FCEB407158}\",\"name\":\"Add layer\",\"params\":{\"in\":[{\"name\":\"Image\",\"type\":\"image\",\"value\":\"\"},{\"name\":\"Name\",\"type\":\"string\",\"value\":\"New layer\"},{\"name\":\"Value\",\"type\":\"real\",\"value\":-1.0}],\"out\":[{\"name\":\"Name\",\"type\":\"layer\",\"value\":\"New layer\"},{\"name\":\"Value\",\"type\":\"real\",\"value\":-1.0}]}}")));
 //			addMethodModel(new MethodModel(new JSONObject("{\"author\":{\"email\":\"j.doe@example.com\",\"first_name\":\"John\",\"id\":0,\"last_name\":\"Doe\"},\"description\":\"Flips selected image horizontally and/or vertically to a new image based on ROI.\",\"id\":\"{BA728F54-6D6B-4478-AFAC-C4367BDB494F}\",\"name\":\"Flip Image (example)\",\"params\":{\"in\":[{\"name\":\"Image\",\"type\":\"image\",\"value\":\"\"},{\"name\":\"Horizontally?\",\"type\":\"bool\",\"value\":true},{\"name\":\"Vertically?\",\"type\":\"bool\",\"value\":true}],\"out\":[{\"name\":\"Flipped image\",\"type\":\"image\",\"value\":\"\"}]}}")));
 //		}
 //		catch (JSONException e) {
 //			System.out.println("error parsing json");
 //		}
 		
 	}
 	
 	public void addMethodModel(MethodModel methodmodel) {
 		list.add(methodmodel);
 		// it will be invoked by the controller after receiving answer for "getmethods" request
 	}
 
 	@Override
 	public void addListDataListener(ListDataListener arg0) {
 
 	}
 
 	@Override
 	public MethodModel getElementAt(int arg0) {
 		MethodModel result = null;
 		try {
 			result = list.get(arg0);
 		}
 		catch (IndexOutOfBoundsException e) {
 			
 		}
 		return result;
 	}
 
 	@Override
 	public int getSize() {
 		return list.size();
 	}
 	
 	public void removeElement(int index) {
 		list.remove(index);
 	}
 
 	@Override
 	public void removeListDataListener(ListDataListener arg0) {
 
 	}
 	
 	public boolean save(String path) {
 		JSONObject output = new JSONObject();
 		JSONArray method_list = new JSONArray();
 		for (int i = 0; i < list.size(); ++i) {
 			method_list.put(list.get(i).getSerialMethod());
 		}
 		try {
 			output.put("methods", method_list);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return false;
 		}
 		try {
 			FileWriter fw = new FileWriter(path);
 			fw.write(output.toString());
 			fw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean load(String path, MethodListModel available_methods) {
 		try {
 			FileReader fr = new FileReader(path);
 			char[] buffer = new char[1000];
 			int chars_to_read = 0;
 			String input_data = "";
 			do {
 				chars_to_read = fr.read(buffer);
 				input_data += new String(buffer);
 			}
 			while (chars_to_read != -1);
 			JSONObject input = new JSONObject(input_data);
 			JSONArray method_list = input.getJSONArray("methods");
 			for (int i = 0; i < method_list.length(); ++i) {
 				boolean match_found = false;
 				MethodModel method = new MethodModel((JSONObject) method_list.get(i));
 				for (int j = 0; j < available_methods.getSize(); ++j) {
 					if (method.getID().equals(available_methods.getElementAt(j).getID())) {
 						match_found = true;
 						break;
 					}
 				}
 				if (match_found) {
 					list.add(method);
 				}
 				else {
 					return false;
 				}
 			}
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 }
