 package com.liqing.kata.kata04;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class FootBall
 {
 
	public static final int ID_COLUMN = 1;
	public static final int FIRST_COLUMN = 6;
	public static final int SECOND_COLUMN = 8;
	public static final String DIVIDING_LINE = "-------------------------------------------------------";
 	private final ReadFile readFile;
 	private List<Record> footBallRecords;
 
 	public FootBall()
 	{
 		footBallRecords = new ArrayList<Record>();
 		readFile = new ReadFile();
 	}
 
 	public String getSmallestDifferenceFAndATeamName()
 	{
 		String[] weather;
 		try
 		{
 			weather = readFile.getRows("football.dat");
 
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 			return "";
 		}
 		return getSmallestDifference(weather);
 	}
 
 	private String getSmallestDifference(String[] rows)
 	{
 		int minIndex = 0;
 		for (int i = 1; i < rows.length; i++)
 		{
			if (rows[i].trim().contains(DIVIDING_LINE))
 			{
 				continue;
 			}
 			Record footBallRecord = setRecords(rows[i]);
 			footBallRecords.add(footBallRecord);
 			if (footBallRecords.get(minIndex).getDifference() > footBallRecord.getDifference())
 			{
 				minIndex = i - 2;
 			}
 		}
 		return footBallRecords.get(minIndex).getId();
 	}
 
 	private Record setRecords(String row)
 	{
 		String[] cells = readFile.splitRow(row);
		return new Record(cells[ID_COLUMN], cells[FIRST_COLUMN], cells[SECOND_COLUMN]);
 	}
 
 }
