 import java.util.ArrayList;
 
 public class DM {
 	ArrayList<String> memory = new ArrayList<String>();
 	ArrayList<String> index = new ArrayList<String>(); 
 	public String readData(String address){
 		if (index.contains(address)) {
 			int i = index.indexOf(address);
 			return memory.get(i);
 		} else {
 			return "null";
 		}
 	}
 	
 	public void writeData(String address, String data){
 		long temp = 0;
 		String binaryValue = "";
 	    index.add(address);
		memory.add(data.substring(0, 7));
 		temp = Long.parseLong(address,2)+1;
 		binaryValue = Long.toBinaryString(temp);
 		index.add(binaryValue);
		memory.add(data.substring(8, 15));
 		temp = Long.parseLong(address,2)+2;
 		binaryValue = Long.toBinaryString(temp);
 		index.add(binaryValue);
		memory.add(data.substring(16, 24));
 		temp = Long.parseLong(address,2)+3;
 		binaryValue = Long.toBinaryString(temp);
 		index.add(binaryValue);
		memory.add(data.substring(25, 31));
 	}
 }
