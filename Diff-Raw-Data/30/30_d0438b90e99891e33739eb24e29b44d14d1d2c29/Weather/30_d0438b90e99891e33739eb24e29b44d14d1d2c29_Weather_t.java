 package com.liqing.kata.kata04;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Weather
 {
	public static final int ID_COLUMN = 0;
	public static final int FIRST_COLUMN = 1;
	public static final int SECOND_COLUMN = 2;
    public static final CharSequence DIVIDING_LINE = " ";
    private final ReadFile readFile;
 	private List<Record> records;
 
 	public Weather()
 	{
 		records = new ArrayList<Record>();
 		readFile = new ReadFile();
 	}
 
 	public String getSmallestTemperatureSpreadDate()
 	{
 		String[] weather;
 		try
 		{
 			weather = readFile.getRows("weather.dat");
 
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 			return "";
 		}
 		return getSmallestSpread(weather);
 	}
 
 	private String getSmallestSpread(String[] rows)
 	{
 		int minIndex = 0;
 		for (int i = 2; i < rows.length; i++)
 		{
            if (rows[i].trim().equals(DIVIDING_LINE))
            {
                continue;
            }
 			Record record = setRecords(rows[i]);
 			records.add(record);
 			if (records.get(minIndex).getDifference() > record.getDifference())
 			{
 				minIndex = i - 2;
 			}
 		}
 		return records.get(minIndex).getId();
 	}
 
 	private Record setRecords(String row)
 	{
 		String[] cells = readFile.splitRow(row);
		return new Record(cells[ID_COLUMN], cells[FIRST_COLUMN], cells[SECOND_COLUMN]);
 	}
 }
