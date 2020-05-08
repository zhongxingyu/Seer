 import java.io.*;
 import java.util.*;
 
 
 public class myjarvis{
 	public static void main(String[] args)
 	{
 		String myinput = "";
 		System.out.println("Good day sir, how are you?");
 
 		BufferedReader myread = new BufferedReader (new InputStreamReader(System.in));
 
 
 		
 		
 
 		int x = 0;
 
 		while (x != 99)
 		{
 			// take input
 			try
 			{
 				myinput = myread.readLine();
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 
 
 			// greetings
 			if(myinput.equals("fine"))
 			{
 				System.out.println("I am glad to here that sir!!!");
 			}
 			else
 			{
 				System.out.println("ok, sir!");
 			}
 
 			// appointment for today
 			// get sysdate
 			Date d = new Date();
 			String sysdate = d.toString();
 			System.out.println(sysdate);
 
 			String currTime24 = sysdate.substring(11,16);
 
 			
 			// convert to standard day time
 			String currTimeSTD = sysdate.substring(11,13);
 			int foo = Integer.parseInt(currTimeSTD);
 			if(foo > 12)
 			{
 				foo = foo - 12;
 			};
 			currTimeSTD = foo + ":" + sysdate.substring(14,16);
 			// pad 0's
 			if(foo < 10)
 			{
 				currTimeSTD = "0"+currTimeSTD;
 			};
 
 			System.out.println("24 clock time is :"+currTime24);
 			System.out.println("Standard clock time is : "+currTimeSTD);
 
 
 			// split REMEMBER DESC TIME
 			File myfile = new File("jarvismem.txt");
			if(myinput.equals("REMEMBER"))
 			{
 			// store a reminder
 
 			try
 			{
 				System.out.println("create a new file");
 				//File myfile = new File("jarvismem.txt");
 				myfile.createNewFile();
 				PrintWriter pw = new PrintWriter(myfile);
 				pw.println("testing testing");
 				pw.close();
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 			}
 				
 
 			// check if a reminder is required at this time
 			try
 			{
 				FileReader fr = new FileReader(myfile);
 				BufferedReader br = new BufferedReader(fr);
 
 				// check reminders in file
 				String aline = br.readLine();
 				System.out.println("LINE: "+aline);
 			}
 			catch(Exception f)
 			{
 				f.printStackTrace();
 			}
 
 
 			// bye
 			if(myinput.equals("bye"))
 			{
 				x = 99;
 			}
 
 		}
 
 	}
 
 }
