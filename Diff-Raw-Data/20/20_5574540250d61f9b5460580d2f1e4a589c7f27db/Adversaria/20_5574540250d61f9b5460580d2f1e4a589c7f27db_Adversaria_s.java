 import java.util.*;
 import java.io.*;
 import java.lang.Runtime;
 
 public class Adversaria {
 
 	static void create(String fileName) {
 		advs_buffer buf = new advs_buffer(null, null);
 		float[] empty = {0f, 0f};
 		int i = 0;
 		for(i = 0; i < 50000; i++) {
 //			buf.put(i, empty);
 		}
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
		System.out.println("\nUsage: java -jar Adversaria.jar {function} {file} {args}\n\n"
 			+ "Functions:\n"
 			+ "  create\tCreate a new storage file\n"
			+ "     java -jar Adversaria.jar create data.db\n\n"
 			+ "  insert\tInsert a new data point\n"
			+ "     java -jar Adversaria.jar insert data.db 503 0.0\n\n"
 			+ "  export\tPrint as JSON\n"
			+ "     java -jar Adversaria.jar export data.db 503\n\n"
 			+ "  dump\t\tPrint all values as JSON\n"
			+ "     java -jar Adversaria.jar dump data.db\n\n"
 			+ "  range\t\tRead a range of values\n"
			+ "     java -jar Adversaria.jar range data.db 0 503\n\n"
 			+ "  size\t\tPrint size\n"
			+ "     java -jar Adversaria.jar size data.db\n\n"
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
 
 			case "export": {
 				open(args[1]).printAll();
 				break;
 			}
 
 			case "dump": {
 				open(args[1]).printAll();
 				break;
 			}
 
 			case "range": {
 				range(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3])).printAll();
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
 
 //		Runtime runtime = Runtime.getRuntime();
 //		System.out.println("Currently using " + runtime.totalMemory() / 1000000 + " MBytes of RAM.");
 	}
 }
