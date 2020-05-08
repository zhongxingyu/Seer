 import java.util.*;
 import java.io.*;
 import java.lang.Runtime;
 
 public class Adversaria {
 
 	static void create(String fileName) {
 		advs_buffer buf = new advs_buffer(null, null);
 		write(fileName, buf);
 	}
 
 	static void write(String fileName, advs_buffer buf) {
 		try {
 			FileOutputStream fileOut = new FileOutputStream(fileName);
 			ObjectOutputStream out = new ObjectOutputStream(fileOut);
 			out.writeObject(buf);
 			out.close();
 			fileOut.close();
 		}
 
 		catch (IOException i) {
 			i.printStackTrace();
 		}
 	}
 
 	static advs_buffer open(String fileName) {
 		advs_buffer tmp = null;
 
 		try {
 			FileInputStream fileIn = new FileInputStream(fileName);
 			ObjectInputStream in = new ObjectInputStream(fileIn);
 			tmp = (advs_buffer)in.readObject();
 			in.close();
 			fileIn.close();
 
 			return tmp;
 		}
 
 		catch (Exception i) {
 			i.printStackTrace();
 		}
 
 		return null;
 	}
 
 	static void insert(String fileName, int timestamp, float[] vals) {
 		advs_buffer buf = open(fileName);
 		buf.put(timestamp, vals);
 		write(fileName, buf);
 	}
 
	static void delete(String fileName, int timestamp) {
		advs_buffer buf = open(fileName);
		buf.delete(timestamp);
		write(fileName, buf);
	}

 	static advs_buffer range(String fileName, int start, int end) {
 		advs_buffer buf = open(fileName);
 		return buf.getRange(start, end);
 	}
 
 	static float[] getFloats(String[] args) {
 		float[] val = new float[args.length];
 		for(int i = 0; i < args.length; i++)
 			val[i] = Float.parseFloat(args[i]);
 		return val;
 	}
 
 	static void printHelp() {
 		System.out.println("\nUsage: adversaria [function] [file name] [[arguments]]\n\n"
 			+ "Functions:\n"
 			+ "  create\tCreate a new storage file\n"
 			+ "     adversaria create data.db\n\n"
 			+ "  insert\tInsert a new data point\n"
 			+ "     adversaria insert data.db 503 0.0\n\n"
			+ "  delete\tDelete a data point\n"
			+ "     adversaria delete data.db 503\n\n"
 			+ "  export\tPrint as JSON\n"
 			+ "     adversaria export data.db 503\n\n"
 			+ "  dump\t\tPrint all values as JSON\n"
 			+ "     adversaria dump data.db\n\n"
 			+ "  range\t\tRead a range of values\n"
 			+ "     adversaria range data.db 0 503\n\n"
 			+ "  size\t\tPrint size\n"
 			+ "     adversaria size data.db\n\n"
 		);
 	}
 
 	public static void main(String[] args) {
 		if(args.length == 0) {
 			printHelp();
 			System.exit(0);
 		}
 
 		switch (args[0]) {
 			case "create": {
 				if(args.length > 1)
 					create(args[1]);
 				else
 					System.out.println("Need a file name.");
 				break;
 			}
 
 			case "insert": {
 				insert(args[1], Integer.parseInt(args[2]), getFloats(Arrays.copyOfRange(args, 3, args.length)));
 				break;
 			}
 
			case "delete": {
				delete(args[1], Integer.parseInt(args[2]));
				break;
			}

 			case "export": {
 				open(args[1]).print();
 				break;
 			}
 
 			case "dump": {
 				open(args[1]).printAll();
 				break;
 			}
 
 			case "range": {
 				range(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3])).print();
 				break;
 			}
 
 			case "exportTSV": {
 				open(args[1]).printTSV();
 				break;
 			}
 
 			case "dumpTSV": {
 				open(args[1]).printAllTSV();
 				break;
 			}
 
 			case "rangeTSV": {
 				range(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3])).printTSV();
 				break;
 			}
 
 			case "size": {
 				System.out.println(open(args[1]).getSize());
 				break;
 			}
 
 			default: {
 				printHelp();
 				break;
 			}
 		}
 	}
 }
