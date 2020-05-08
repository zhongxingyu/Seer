 package misc;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 import script.LonelehMining;
 import script.mining.MiningVars;
 import script.mining.Ore;
 
 public class SubmitServer
 {
 	private final String submitFile = "/submit.php";
 	private final String signatureFile = "/signature.php";
 	private final String logFile = "/gtfo/log.php";
 	
 	private String submitUrl = "";
 	@SuppressWarnings("unused")
 	private String signatureUrl = "";
 	private String logUrl = "";
 	
 	private long lastTime = 0; //in millis
 	private long lastOres = 0;
 	private long lastGems = 0;
 	private long lastProfit = 0;
 	private double lastExp = 0;
 	
 	public SubmitServer(String url)
 	{
 		this.setUrl(url, submitFile, signatureFile, logFile);
 	}
 	
 	public String submit()
 	{
 		String[] submitParameters = new String[] {
 				"script", "mining",
 				"name", LonelehMining.getPbName(),
 				"time", String.format("%d", Variables.miningTimer.getElapsedTime()),
 				"ores", String.format("%d", MiningVars.oresMined),
 				"gems", String.format("%d", MiningVars.gemsMined),
 				"profit", String.format("%d", Ore.getTotalProfit()),
 				"exp", String.format("%.0f", Ore.getTotalExp())};
		for (int i = 0 ; i < submitParameters.length-1 ; i++)
			System.out.println(submitParameters[i] + ": " + submitParameters[++i]);
 		
		return submit(submitUrl, true, submitParameters) + "\n" + submit(logUrl, false, submitParameters);
 	}
 	
 	/**
 	 * uses http get to submit data to the specified server url
 	 * @param endpoint your url
 	 * @param requestParameters field1, val1, field2, val2, etc
 	 * @return the response from server
 	 */
 	private String submit(String endpoint, boolean subtract, final String ...requestParameters)
 	{
 		String result = null;
 		
 		// Send a GET request to the servlet
 		try
 		{
 			String urlStr = endpoint + "?";
 			if (requestParameters != null && requestParameters.length-1 > 0)
 			{
 				for (int i = 0 ; i < requestParameters.length ; i++)
 				{
 					if (requestParameters[i].equalsIgnoreCase("time"))
 					{
 						lastTime = subtract == true ? lastTime : 0;
 						urlStr += requestParameters[i] + "=" + (Long.parseLong(requestParameters[i+1])-lastTime) + "&";
 						lastTime = Long.parseLong(requestParameters[++i]);
 					}
 					else if (requestParameters[i].equalsIgnoreCase("ores"))
 					{
 						lastOres = subtract == true ? lastOres : 0;
 						urlStr += requestParameters[i] + "=" + (Long.parseLong(requestParameters[i+1])-lastOres) + "&";
 						lastOres = Long.parseLong(requestParameters[++i]);
 					}
 					else if (requestParameters[i].equalsIgnoreCase("gems"))
 					{
 						lastGems = subtract == true ? lastGems : 0;
 						urlStr += requestParameters[i] + "=" + (Long.parseLong(requestParameters[i+1])-lastGems) + "&";
 						lastGems = Long.parseLong(requestParameters[++i]);
 					}
 					else if (requestParameters[i].equalsIgnoreCase("profit"))
 					{
 						lastProfit = subtract == true ? lastProfit : 0;
 						urlStr += requestParameters[i] + "=" + (Long.parseLong(requestParameters[i+1])-lastProfit) + "&";
 						lastProfit = Long.parseLong(requestParameters[++i]);
 					}
 					else if (requestParameters[i].equalsIgnoreCase("exp"))
 					{
 						lastExp = subtract == true ? lastExp : 0;
 						urlStr += requestParameters[i] + "=" + (Double.parseDouble(requestParameters[i+1])-lastExp) + "&";
 						lastExp = Double.parseDouble(requestParameters[++i]);
 					}
 					else
 					{
 						urlStr += requestParameters[i] + "=" + requestParameters[++i] + "&";
 					}
 				}
 			}
			System.out.println("urlStr = " + urlStr);
 			
 			URL url = new URL(urlStr);
 			URLConnection submitConn = url.openConnection();
 			
 			// Get the response
 			BufferedReader rd = new BufferedReader(new InputStreamReader(submitConn.getInputStream()));
 			StringBuffer sb = new StringBuffer();
 			String line;
 			
 			while ((line = rd.readLine()) != null)
 				sb.append(line);
 			
 			rd.close();
 			
 			result = sb.toString().replaceAll("\\<.*?\\>", "");
 			
 			if (result.contains("*") || result.contains("error") || result.contains("Error") || result.contains("ERROR"))
 			{
 				LonelehMining.logger.severe(result);
 			}
 		}
 		catch (Exception e)
 		{
 			LonelehMining.logger.severe("An error has occured while pushing data to server");
 		}
 		return result;
 	}
 	
 	public long getLastTime()
 	{
 		return lastTime;
 	}
 	
 	public void setUrl(String newUrl, String submit, String signature, String log)
 	{
 		if (!newUrl.startsWith("http://"))
 			newUrl = "http://" + newUrl;
 		
 		this.submitUrl = newUrl + submit;
 		this.signatureUrl = newUrl + signature;
 		this.logUrl = newUrl + log;
 	}
 }
