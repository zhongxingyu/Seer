 import java.util.*;
 import java.io.*;
 import java.lang.Runtime;
 
 public class Adversaria {
 
 	static void create(String fileName) {
 		advs_buffer buf = new advs_buffer(null, null);
 		float[] empty = {0f, 0f};
 		int i = 0;
		for(i = 0; i < 40000; i++)
 			buf.put(i, empty);
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
 
 	static void insert(String fileName, int timestamp, float inBytes, float outBytes) {
 		advs_buffer buf = open(fileName);
 		float[] val = {inBytes, outBytes};
 		buf.put(timestamp, val);
 		write(fileName, buf);
 	}
 
 	static advs_buffer range(String fileName, int start, int end) {
 		advs_buffer buf = open(fileName);
 		return buf.getRange(start, end);
 	}
 
 	static void printHelp() {
 		System.out.println("\nusage: java -jar adversaria.jar {function} {file} {data}");
 		System.out.println("\nFunctions:\n");
 		System.out.println("  create\tCreate a new storage file");
 		System.out.println("  insert\tInsert a new data point");
 		System.out.println("  export\tPrint as JSON");
 		System.out.println("  range\t\tRead a range of values");
 		System.out.println("");
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
 				insert(args[1], Integer.parseInt(args[2]), Float.parseFloat(args[3]), Float.parseFloat(args[4]));
 				break;
 			}
 
 			case "export": {
 				open(args[1]).print();
 				break;
 			}
 
 			case "range": {
 				range(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3])).print();
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
