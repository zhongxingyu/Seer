 package dhbw.LWBS.CA5_KB1.controller;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
import java.util.PriorityQueue;
 import java.util.Queue;
import java.util.StringTokenizer;

import dhbw.LWBS.CA5_KB1.model.Person;
 
 public class CSVUtility
 {
	String fileName = "gruppe_ca5_kb1.csv";
 	String line = "";
	Queue<String[]> persons = new PriorityQueue<String[]>();
 
 	public CSVUtility()
 	{
 		try
 		{
 			BufferedReader readCSV = new BufferedReader(
 					new FileReader(fileName));
 
 			while ((line = readCSV.readLine()) != null)
 			{
 				persons.offer(line.split(";"));
 			}
 			readCSV.close();
 
		} catch (FileNotFoundException e)
 		{
 			e.printStackTrace();
		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 
 	public String[] getPerson()
 	{
 		return persons.poll();
 	}
 
 }
