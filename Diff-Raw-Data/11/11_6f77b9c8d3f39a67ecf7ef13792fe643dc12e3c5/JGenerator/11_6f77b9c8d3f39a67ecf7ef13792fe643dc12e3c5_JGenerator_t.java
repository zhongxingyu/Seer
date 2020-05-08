 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.DecimalFormat;
 
 import javax.swing.JFrame;
 
 public class JGenerator 
 {
 	private static String IP				= "http://192.168.1.100";
 	private static String PORT				= "8080";
	private static String URL				= IP + ":" + PORT;
 
 	private static int DURATION				= 30;
 
 	private static final int NPS			= 0;
 	private static final int NPP			= 1;
 	private static final int PWP			= 2;
 	private static final int PWOP			= 3;
 
 	private static int rate;
 	private static int connType;
 	
 	private static boolean GUI = false;
 	
 	private static int t1Request = 0;
 	private static int t2Request = 0;
 	
 	private static double t1TotalTime = 0;
 	private static double t2TotalTime = 0;
 	
 	public static void main(String[] args) throws IOException 
 	{
 		if(args.length < 2 || args.length > 4)
 			generateGUI();
 		else
 		{
 			boolean validInput = determineConnectionType(args);
 			if(!validInput)
 				printUsage();
 			else
 				generateTraffic();
 		}
 	}
 	
 	public static String generateTraffic() throws IOException
 	{
		URL = IP + ":" + PORT;
 		int requestCount = 0;
 		double start = 0, finish = 0;
 		double totalTime = 0;
 		BufferedReader in = null;
 		BufferedReader other = null;
 		HttpURLConnection conn = null;
 		HttpURLConnection pic = null;
 		switch(connType)
 		{
 			case NPS:
 				while(totalTime/1000 < DURATION)
 				{
 					requestCount++;
 					start = System.currentTimeMillis();
 					String urlString = URL;
 					URL url = new URL(urlString);
 					conn = (HttpURLConnection) url.openConnection();
 					conn.setRequestProperty("Connection", "close");
 					in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 					while (in.read() != -1);
 					in.close();
 					requestCount++;
 					while((1000/rate) > System.currentTimeMillis()-start);
 					totalTime += System.currentTimeMillis() - start;
 					start = System.currentTimeMillis();
 					conn = (HttpURLConnection) new URL(urlString + "/0.jpg").openConnection();
 					conn.setRequestProperty("Connection", "close");
 					other = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 					while (other.read() != -1);
 					other.close();
 					while((1000/rate) > System.currentTimeMillis()-start);
 					finish = System.currentTimeMillis();
 					totalTime += finish - start;
 				}
 				conn.disconnect();
 				in.close();
 				other.close();
 				break;
 			case NPP:
 				boolean t1Done = false;
 				boolean t2Done = false;
 
 				Thread t1 = (new Thread()
 				{
 					@Override
 					public void run()
 					{
 						try
 						{
 							int requestCount = 0;
 							double start = 0, finish = 0;
 							double totalTime = 0;
 							BufferedReader in = null;
 							BufferedReader other = null;
 							HttpURLConnection conn = null;
 							HttpURLConnection pic = null;
 							
 							while(totalTime/1000 < DURATION)
 							{
 								requestCount += 2;
 								start = System.currentTimeMillis();
 								URL url = new URL(URL);
 								conn = (HttpURLConnection) url.openConnection();
 								conn.setRequestProperty("Connection", "close");
 								pic = (HttpURLConnection) new URL(URL + "/0.jpg").openConnection();
 								pic.setRequestProperty("Connection", "close");
 								in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 								other = new BufferedReader(new InputStreamReader(pic.getInputStream()));
 								
 								while(in.read() != -1 && other.read() != -1)
 									;
 								
 								while((2000/(rate/2)) > System.currentTimeMillis()-start)
 									;
 								
 								totalTime += System.currentTimeMillis() - start;
 								in.close();
 								other.close();
 							}
 							
 							conn.disconnect();
 							pic.disconnect();
 							in.close();
 							other.close();
 							setThreadValue(1, requestCount, totalTime);
 						}
 						catch(IOException ioe) { ioe.printStackTrace(); }
 					}
 				});
 				
 				Thread t2 = (new Thread()
 				{
 					@Override
 					public void run()
 					{
 						try
 						{
 							int requestCount = 0;
 							double start = 0, finish = 0;
 							double totalTime = 0;
 							BufferedReader in = null;
 							BufferedReader other = null;
 							HttpURLConnection conn = null;
 							HttpURLConnection pic = null;
 							
 							while(totalTime/1000 < DURATION)
 							{
 								requestCount += 2;
 								start = System.currentTimeMillis();
 								URL url = new URL(URL);
 								conn = (HttpURLConnection) url.openConnection();
 								conn.setRequestProperty("Connection", "close");
 								pic = (HttpURLConnection) new URL(URL + "/0.jpg").openConnection();
 								pic.setRequestProperty("Connection", "close");
 								in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 								other = new BufferedReader(new InputStreamReader(pic.getInputStream()));
 								
 								while(in.read() != -1 && other.read() != -1)
 									;
 								
 								while((2000/(rate/2)) > System.currentTimeMillis()-start)
 									;
 								
 								totalTime += System.currentTimeMillis() - start;
 								in.close();
 								other.close();
 							}
 							
 							conn.disconnect();
 							pic.disconnect();
 							in.close();
 							other.close();
 							setThreadValue(2, requestCount, totalTime);
 						}
 						catch(IOException ioe) { ioe.printStackTrace(); }
 					}
 				});
 
 				start = System.currentTimeMillis();
 				t1.start();
 				t2.start();
 				while(t1.isAlive() || t2.isAlive())
 					;
 				totalTime = (t1TotalTime > t2TotalTime)?t1TotalTime:t2TotalTime;
 				requestCount = t1Request + t2Request;
 				break;
 			default:
 				while(totalTime/1000 < DURATION)
 				{
 					requestCount++;
 					start = System.currentTimeMillis();
 					String urlString = URL;
 					URL url = new URL(urlString);
 					conn = (HttpURLConnection) url.openConnection();
 					conn.setRequestProperty("Connection", "Keep-Alive");
 					in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 					while (in.read() != -1);
 					in.close();
 					requestCount++;
 					while((1000/rate) > System.currentTimeMillis()-start);
 					totalTime += System.currentTimeMillis() - start;
 					start = System.currentTimeMillis();
 					conn = (HttpURLConnection) new URL(urlString + "/0.jpg").openConnection();
 					conn.setRequestProperty("Connection", "Keep-Alive");
 					other = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 					while (other.read() != -1);
 					other.close();
 					while((1000/rate) > System.currentTimeMillis()-start);
 					finish = System.currentTimeMillis();
 					totalTime += finish - start;
 				}
 				conn.disconnect();
 				in.close();
 				other.close();
 				break;
 		}
 
 		double value = round(requestCount/(totalTime/1000));
 		if(!GUI)
 			System.out.println("Average Requests/Second: " + value);
 		else
 			return "Average Requests/Second: " + value;
 		
 		return null;
 	}
 	
 	public static void printUsage()
 	{
 		System.out.println("Usage: java JGenerator <mode> <rate> -d <duration>");
 		System.out.println("          Modes:");
 		System.out.println("             -nps (Non-Persistent Serial)");
 		System.out.println("             -npp (Non-Persistent Parallel)");
 		System.out.println("             -pwp (Persistent with Pipe)");
 		System.out.println("             -pwop (Persistent without Pipe)");
 		System.out.println("");
 		System.out.println("          Rate: (Requests/Second)");
 		System.out.println("             E.g. 1");
 		System.out.println("");
 		System.out.println("          Duration: (Optional - Default: 30)");
 		System.out.println("             E.g. 10");
 	}
 	
 	public static boolean determineConnectionType(String[] v)
 	{
 		if(v[0].equals("nps"))
 			connType = NPS;
 		else if(v[0].equals("npp"))
 			connType = NPP;
 		else if(v[0].equals("pwp"))
 			connType = PWP;
 		else if(v[0].equals("pwop"))
 			connType = PWOP;
 		else
 			return false;
 		
 		try					{ rate = Integer.parseInt(v[1]);	}
 		catch (Exception e)	{ return false;						}
 		
 		if(v.length > 2)
 		{
 			if(v[2].equals("-d"))
 			{
 				try { DURATION = Integer.parseInt(v[3]); }
 				catch (Exception e)
 				{
 					DURATION = 30; // Reset
 					return false;
 				}
 			}
 			else
 				return false;
 		}
 		
 		return true;
 	}
 	
 	public static void generateGUI()
 	{
 		GUI go = new GUI();
 
 		go.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		go.setSize(440, 195);
 		go.setResizable(false);
 		go.setVisible(true);
 	}
 	
 	public static String GUIExecute(Object[] parameters) throws IOException
 	{
 		GUI			= true;
 		if(((String)parameters[0]).contains(":"))
 		{
 			IP		= "http://" + ((String)parameters[0]).split(":")[0];
 			PORT	= ((String)parameters[0]).split(":")[1];
 		}
 		else
 			IP = "http://" + (String)parameters[0];
 		
 		connType	= (int)parameters[1];
 		rate		= (int)parameters[2];
 		DURATION	= (int)parameters[3];
 		
 		return generateTraffic();
 	}
 	
 	public static double round(Double value)
 	{
 		return Double.valueOf(new DecimalFormat("#.###").format(value));
 	}
 	
 	public static void setThreadValue(int thread, int requestTime, double totalTime)
 	{
 		switch(thread)
 		{
 			case 1:
 				t1Request = requestTime;
 				t1TotalTime = totalTime;
 				break;
 			case 2:
 				t2Request = requestTime;
 				t2TotalTime = totalTime;
 				break;
 		}
 	}
 }
