 package net.budsbox.flatdb.main;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import org.yaml.snakeyaml.Yaml;
 
 import net.budsbox.flatdb.loader.FileLoader;
 import net.budsbox.flatdb.manager.PmManager;
 import net.budsbox.flatdb.tsp.DbnMo;
 import net.budsbox.flatdb.tsp.DbnRow;
 
 public abstract class FlatDbHelper {
 	
 	public static String getYaml(DbnMo mo) {
 		Yaml yaml = new Yaml();
 		return yaml.dump(mo);
 	}
 	public static void printAVPs(DbnMo mo) {
 		System.out.println("");
 		System.out.println("Tsp Pot");
 		System.out.println("----------------------------");
 		
 		for (String key : mo.getAttributeValuePair().keySet()) {
 			for (String values : mo.getAttributeValuePair().get(key)) {
 				System.out.println(key +": \t"+values);
 			}
 		}
 		System.out.println("");
 	}
	public static void printToConsole(String directory, String recordType, ArrayList<String> attributes, PmManager stats) {
 		
 		FileLoader fileLoader = new FileLoader(directory);
 		String oneFile=fileLoader.getOneFile();
 	
 		while (oneFile != null)
 		{
 			try
 			{
 				stats.statNumOfFiles();
 				String inRow;
 				FileInputStream fstream = new FileInputStream(oneFile);
 				DataInputStream in = new DataInputStream(fstream);
 				BufferedReader br = new BufferedReader(new InputStreamReader(in));
 				while ( (inRow = br.readLine()) != null ) 
 				{
 					stats.statNumOfRows();
 					if (inRow.contains(recordType))
 					{
 						DbnRow row = new DbnRow(inRow, attributes, recordType);
 						DbnMo mo = new DbnMo(row);
 						System.out.println(getYaml(mo));
 						stats.statNumOfObject();
 					} 
 					else 
 					{
 						stats.statNumOfLinesSkiped();
 					}
 				}
 				in.close();
 			} 
 			catch (Exception e) 
 			{
 					stats.statNumOfFailedFiles();
					System.err.println("=== Error: \n" + e.getMessage() + " Cant load file " + oneFile + "\n");
 			} 
 			oneFile=fileLoader.getOneFile();
 		}
 		
 	}
 	public static void printToFile(String inDir, String outDir, String recordType, ArrayList<String> attributes, PmManager stats)
 	{
 		
 		FileWriter fstream = null;
 		try 
 		{
 			fstream = new FileWriter(outDir);
 		} 
 		catch (IOException e1) 
 		{
			System.err.println("=== Error: \n" + e1.getMessage() + " Can't write to file " + outDir + "\n");
 		}
 		
 		BufferedWriter out = new BufferedWriter(fstream);
 		FileLoader fileLoader = new FileLoader(inDir);
 		String oneFile=fileLoader.getOneFile();
	
 		while (oneFile != null)
 		{
 			try
 			{
 				stats.statNumOfFiles();
 				String inRow;
 				FileInputStream finstream = new FileInputStream(oneFile);
 				DataInputStream in = new DataInputStream(finstream);
 				BufferedReader br = new BufferedReader(new InputStreamReader(in));
 				while ( (inRow = br.readLine()) != null ) 
 				{
 					stats.statNumOfRows();
 					if (inRow.contains(recordType))
 					{
 						DbnRow row = new DbnRow(inRow, attributes, recordType);
 						DbnMo mo = new DbnMo(row);
 						out.write(getYaml(mo));
 						out.write("---\n");
 						stats.statNumOfObject();
 					} 
 					else 
 					{
 						stats.statNumOfLinesSkiped();
 					}
 				}
 				in.close();
 			} 
 			catch (Exception e) 
 			{
 					stats.statNumOfFailedFiles();
					System.err.println("=== Error: \n" + e.getMessage()+ " Can't open file " + oneFile + "\n");
 			} 
 			oneFile=fileLoader.getOneFile();
 		}
 		try 
 		{
 			out.close();
 		} 
 		catch (IOException e) 
 		{
			System.err.println("=== Error: \n" + e.getMessage() + " Can't close file " + out.toString() + "\n");
 		}
 		
 	}
 	public static ArrayList<String> parseRow(DbnRow rp, String fieldName)
 	{
 		ArrayList<String> resault = new ArrayList<String>();
 		String fieldType = rp.getRow().split(fieldName)[1].split(":")[1].split("[0-9]")[0];
 		if (fieldType.equals("S")) 
 		{
 			resault = parseRowString(rp, fieldName);
 		} 
 		else if (fieldType.equals("A")) 
 		{
 			resault = parseRowArray(rp, fieldName);
 		}
 		return resault;
 	}
 	
 	private static ArrayList<String> parseRowString(DbnRow rp, String fieldName) 
 	{
 		
 		ArrayList<String> resault = new ArrayList<String>();
 		String strStringLength = new String();
 		String str1 = new String();
 		String cleanValue;
 		int index = rp.getAttributes().indexOf(fieldName);
 		int indexPlusOne = index + 1;
 		int fieldIndex1 = rp.getAttributes().indexOf(fieldName);
 		int fieldIndex2;
 		int fieldLength; 
 		int stringSizeLength;
 		int intStringLength;
 		
 		if (rp.getAttributes().size() < indexPlusOne) 
 		{
 			fieldIndex2 = rp.getAttributes().indexOf(rp.getAttributes().get(indexPlusOne));
 		} 
 		else 
 		{
 			fieldIndex2 = rp.getRow().indexOf("]]]");
 		}
 		
 		str1 = rp.getRow().substring(rp.getAttributeIndexArray().get(fieldIndex1), fieldIndex2);
 		strStringLength = str1.split("S")[1].split("\\(")[0];		
 		fieldLength = fieldName.length();
 		stringSizeLength = strStringLength.length() + 4;
 		intStringLength = Integer.parseInt(strStringLength);
 		
 		cleanValue = str1.substring(fieldLength + stringSizeLength, 
 				fieldLength + stringSizeLength + intStringLength).
 				replaceAll("\\\\n", "").replaceAll("\\\\\"", "\"");
 		
 		resault.add(cleanValue);
 		return resault;
 	}
 	private static ArrayList<String> parseRowArray(DbnRow rp, String fieldName)
 	{
 		int arraySize = Integer.parseInt(rp.getRow().split(fieldName)[1].toString().split("\\[")[0].split("A")[1]);
 		int fieldArrayindex = 0;
 		String cutRow = rp.getRow().split(fieldName)[1];
 		ArrayList<String> arrayValues= new ArrayList<String>();
 		
 		for (int i = 1 ; i <= arraySize ; i++)
 		{
 			if (i < arraySize)
 			{
 				String pattern1 = "ix" + fieldArrayindex;
 				String pattern2 = "ix" + ( fieldArrayindex + 1 );
 				arrayValues.add(cutRow.substring(cutRow.indexOf(pattern1), cutRow.indexOf(pattern2)).split("\"")[1]);
 			} 
 			else if (i==arraySize)
 			{
 				String pattern1 = "ix" + fieldArrayindex;
 				arrayValues.add(cutRow.substring(cutRow.indexOf(pattern1), cutRow.indexOf("]")).split("\"")[1]);
 			}
 			fieldArrayindex++;
 		}
 		return arrayValues;
 	}	
 }
