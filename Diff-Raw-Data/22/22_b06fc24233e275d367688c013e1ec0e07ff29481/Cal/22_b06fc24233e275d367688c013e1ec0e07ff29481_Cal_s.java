 /**
    I pledge my honor that I have abided by the Stevens Honor System.
    Jason Gardella - Jeremy Bassi
    Created: Sep 18, 2013
  */
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 
 public class Cal
 {
 	
 	public static int[][] days = new int[6][7];
 	public static int[] maxDays = { 31, 28, 31, 30, 31, 30, 31, 30, 31, 31, 30, 31 };
	public static int month, year;
 	public static GregorianCalendar cal;
 	
 	public static HashMap<Integer, String> monthMap = new HashMap<>();
 	
 	public static void packHash()
 	{
 		monthMap.put(0, "January");
 		monthMap.put(1, "February");
 		monthMap.put(2, "March");
 		monthMap.put(3, "April");
 		monthMap.put(4, "May");
 		monthMap.put(5, "June");
 		monthMap.put(6, "July");
 		monthMap.put(7, "August");
 		monthMap.put(8, "September");
 		monthMap.put(9, "Octobor");
 		monthMap.put(10, "November");
 		monthMap.put(11, "December");
 	}
 	
 	public static void fill()
 	{
 		int numDay = 1;
 		int startIndex = cal.get(Calendar.DAY_OF_WEEK) % 7;
 		for(int i = 0; i < days.length; i++)
 		{
 			for(int j = 0; j < days[i].length && numDay <= maxDays[month]; j++)
 			{
 				if(i == 0 && j < startIndex)
 					days[i][j] = 0;
 				else
 				{
 					days[i][j] = numDay;
 					numDay++;
 				}	
 			}
 		}
 	}
 	
 	
 	public static void printMonth()
 	{
 		System.out.printf("%15s", monthMap.get(month) + " " + year);
 		System.out.println();
 		System.out.println(" Su Mo Tu We Th Fr Sa");
 		for(int i = 0; i < days.length; i++)
 		{
 			for(int j = 0; j < days[i].length; j++)
 			{
 				if(days[i][j] != 0)
					System.out.printf("%3d", days[i][j]);
 				else
 					System.out.print("   ");
 			}
 			System.out.println();
 		}
 	}
 
 	
 	public static void main(String[] args)
 	{
 		if(args.length == 0)
 			cal = (GregorianCalendar) GregorianCalendar.getInstance();
 		else
 		{
 			year = Integer.parseInt(args[0]);
 			month = Integer.parseInt(args[1]);
 			cal = new GregorianCalendar(year, month, 0);
 		}
		packHash();
 		fill();
 		printMonth();
 	}
 	
 
 }
