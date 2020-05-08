 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class Database {
 	
 	private List<Record> records;
 	private String path;
 	
 	public Database(String path) {
 		this.path = path;
 		this.records = new ArrayList<Record>();
 		readRecords(path);
 	}
 	
 	private Record formRecord(String recordString){
 		String[] parts = recordString.split("\\s");
 		if (parts.length != 3){ //sanity check
 			System.out.println("Corrupted record");
 		}
 		return new Record(parts[0], parts[1], new Integer(parts[2]));
 	}
 	
 	private void readRecords(String path){
 		try {
 			FileInputStream iStream = new FileInputStream(path);
 			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(iStream)));
 			String line = null;
 			while ((line = reader.readLine()) != null){
 				records.add(formRecord(line));
 			}
 		} catch (Exception e) {
			System.out.println("Error reading Database file");
			e.printStackTrace();
 		}
 	}
 	
 	public List<Record> retrieve(String name, String ipAddress, Integer port){
 		List<Record> results = new ArrayList<Record>();
 		for (Record record : records){
 			if (record.matches(name, ipAddress, port)){
 				results.add(record);
 			}
 		}
 		return results;
 	}
 	
 	public String insert(String name, String ipAddress, Integer port){
 		if (retrieve(name, null, null).size() > 0)
 			return "ERROR: a record exists with the same name";
 		records.add(new Record(name, ipAddress, port));
 		return "Record Added successfully";
 	}
 	
 	public String delete(String name, String ipAddress, Integer port){
 		List<Integer> occurences = new ArrayList<Integer>();
 		for (int i=0; i< records.size(); i++){
 			if (records.get(i).matches(name, ipAddress, port)){
 				occurences.add(i);
 			}
 		}
 		for (int o : occurences){
 			records.remove(o);
 		}
 		if (occurences.size() == 0){
 			return "ERROR: record not found";
 		} else {
 			return "" + occurences.size() + " record(s) deleted successfully";
 		}
 	}
 	
 	public String save(){
 		try {
 			FileOutputStream oStream = new FileOutputStream(path);
 			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(oStream)));
 			for (Record record : records){
 				writer.write(record.toString() + "\n");
 			}
 		} catch (Exception e) {
 			System.out.println("Error writing Database file");
 			e.printStackTrace();
 			return "Error writing Database file";
 		}
 		return "Database saved successfully";
 	}
 }
