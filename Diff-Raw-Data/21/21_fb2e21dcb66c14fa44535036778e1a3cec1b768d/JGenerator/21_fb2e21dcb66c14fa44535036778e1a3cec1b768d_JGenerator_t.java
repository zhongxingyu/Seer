 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 public class JGenerator 
 {
 	private static final String IP			= "http://192.168.1.100";
 	private static final String PORT		= "8080";
 	private static final String URL			= IP + ":" + PORT;
 
 	private static int DURATION				= 30;
 
 	private static final int NPS			= 0;
 	private static final int NPP			= 1;
 	private static final int PWP			= 2;
 	private static final int PWOP			= 3;
 
 	private static int rate;
 	private static int connType;
 	
 	public static void main(String[] args) throws IOException 
 	{
 		if(args.length < 2 || args.length > 4)
 			printUsage();
 		else
 		{
 			boolean validInput = determineConnectionType(args);
 			if(!validInput)
 				printUsage();
 			else
 				generateTraffic();
 		}
 	}
 	
 	public static void generateTraffic() throws IOException
 	{
 		int requestCount = 0;
 		double start = 0, finish = 0, totalTime = 0;
 		BufferedReader in = null;
 		BufferedReader other = null;
 		HttpURLConnection conn = null;
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
					
					//omg wtf am i doing?
					conn.disconnect();
					
 					requestCount++;
 					while((1000/rate) > System.currentTimeMillis()-start);
 					totalTime += System.currentTimeMillis() - start;
 					start = System.currentTimeMillis();
					//HttpURLConnection pic = (HttpURLConnection) new URL(urlString + "/0.jpg").openConnection();
					//pic.setRequestProperty("Connection", "close");
					
					//augh!
					conn = (HttpURLConnection) new URL(urlString + "/0.jpg").openConnection();
					conn.setRequestProperty("Connection", "close");
					
 					other = new BufferedReader(new InputStreamReader(pic.getInputStream()));
 					while (other.read() != -1);
 					other.close();
					
					//again!?
					
					
 					while((1000/rate) > System.currentTimeMillis()-start);
 					finish = System.currentTimeMillis();
 					totalTime += finish - start;
 				}
 
				//conn.disconnect();
 				break;
 			case NPP:
 				while(totalTime/1000 < DURATION)
 				{
 					requestCount+=2;
 					start = System.currentTimeMillis();
 					String urlString = URL;
 					URL url = new URL(urlString);
 					conn = (HttpURLConnection) url.openConnection();
 					conn.setRequestProperty("Connection", "close");
 					HttpURLConnection pic = (HttpURLConnection) new URL(urlString + "/0.jpg").openConnection();
 					pic.setRequestProperty("Connection", "close");
 					in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 					other = new BufferedReader(new InputStreamReader(pic.getInputStream()));
 					while(in.read() != -1 && other.read() != -1);
 					while((2000/rate) > System.currentTimeMillis()-start);
 					totalTime += System.currentTimeMillis() - start;
 					in.close();
 					other.close();
 				}
 
 				break;
 			case PWP:
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
 					HttpURLConnection pic = (HttpURLConnection) new URL(urlString + "/0.jpg").openConnection();
 					pic.setRequestProperty("Connection", "Keep-Alive");
 					other = new BufferedReader(new InputStreamReader(pic.getInputStream()));
 					while (other.read() != -1);
 					other.close();
 					while((1000/rate) > System.currentTimeMillis()-start);
 					finish = System.currentTimeMillis();
 					totalTime += finish - start;
 				}
 
 				conn.disconnect();
 				break;
 			case PWOP:
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
 					HttpURLConnection pic = (HttpURLConnection) new URL(urlString + "/0.jpg").openConnection();
 					pic.setRequestProperty("Connection", "Keep-Alive");
 					other = new BufferedReader(new InputStreamReader(pic.getInputStream()));
 					while (other.read() != -1);
 					other.close();
 					while((1000/rate) > System.currentTimeMillis()-start);
 					finish = System.currentTimeMillis();
 					totalTime += finish - start;
 				}
 				conn.disconnect();
 				break;
 		}
 		
 		in.close();
 		other.close();
 		System.out.println("Average Request/Second: " + requestCount/(totalTime/1000));
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
 }
