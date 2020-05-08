package au.org.paperminer.util;

 /**
  * @author kutty
  * Purpose: To extract the geocodes from the output of Senna with the entities.
  * Filter the entities to identify only the locations in the given newspaper article.
  */
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.DOMException;
 import org.xml.sax.SAXException;
 
 
 
 public  class LatLongExtractor
 
 {
 	
 	public static ArrayList countriesList=null;
 	public static BufferedWriter wrPlace=null;
 	private static final String ROOT_FOLDER = "/dev/shm/Trove/";
 	
 	public static BufferedWriter wrFileList=null;
 	
 
 	public static ArrayList getCountryList() throws IOException
 	{
 		BufferedReader rd = new BufferedReader(new FileReader(ROOT_FOLDER+"country_list.txt"));
 		String str=null;
 		ArrayList countries=new ArrayList();
 		while ((str = rd.readLine()) != null)
 		{
 			countries.add(str.trim());
 		}
 		return countries;
 
 	}
 
 	
 	
 	
 
 	
 	public static String getCoOrdinates(String article_Id, String strAddress) throws IOException
     {
     	String coordinates="";
     	String strKey = "ABQIAAAAWxzgJsD_pv_jXT96S2RpuBS6dzvQgpOsen92WxVvS6POVuPc1BQns3ssVjJGerfTSdH9TyTGSXrdtw";
     	String sPath = "http://maps.google.com/maps/geo?q=" + strAddress + "&output=csv&key=" + strKey;
         // System.out.println(strAddress);
          //System.out.println(sPath);
          URL client = new URL(sPath);
          String thisLine;
 
 
          BufferedReader theHTML = new BufferedReader(new InputStreamReader(client.openStream()));
 		 
          if((thisLine = theHTML.readLine()) != null)
          {
          	// System.out.println(thisLine);
                String[] split=thisLine.split(",");
                if(split[0].equals("200"))
              	  coordinates=coordinates+split[2]+","+split[3]+" ";
                else
                {
             	  
              	  System.err.println("Error for "+ article_Id+" "+ strAddress);
              	  //Need to call this recursively unless the co-ordinates are fetched
              	  coordinates=getCoOrdinates(article_Id,strAddress);				  
              	  System.err.println(sPath);
              	 
                }
       
                theHTML.close();
                
          }
          return coordinates;
 
     }
 
 	
 	
 	public static String getCoOrdinates(String article_Id, String strAddress, int num_loops) throws IOException
     {
     	String coordinates="";
     	String strKey = "ABQIAAAAWxzgJsD_pv_jXT96S2RpuBS6dzvQgpOsen92WxVvS6POVuPc1BQns3ssVjJGerfTSdH9TyTGSXrdtw";
     	String sPath = "http://maps.google.com/maps/geo?q=" + strAddress + "&output=csv&key=" + strKey;
         // System.out.println(strAddress);
          //System.out.println(sPath);
          URL client = new URL(sPath);
          String thisLine;
 
 
          BufferedReader theHTML = new BufferedReader(new InputStreamReader(client.openStream()));
 		 
          if((thisLine = theHTML.readLine()) != null)
          {
          	// System.out.println(thisLine);
                String[] split=thisLine.split(",");
                if(split[0].equals("200"))
              	  coordinates=coordinates+split[2]+","+split[3]+" ";
                else 
                {
 			    if(num_loops<3)
 				{
             	  
              	 
              	  //Need to call this recursively unless the co-ordinates are fetched
              	  coordinates=getCoOrdinates(article_Id,strAddress,num_loops++);				  
              	  
              	 }
 				 else
 				 {
 				  System.err.println("Error for "+ article_Id+" "+ strAddress);
 				  System.err.println(sPath);
 				  }
                }
       
                theHTML.close();
                
          }
          return coordinates;
 
     }
 	
 	
 	
 	public static void GeoCode(String Placenames, String Coordinates) throws MalformedURLException, DOMException, IOException, ParserConfigurationException, SAXException
 	{
 		String strKey = "ABQIAAAAWxzgJsD_pv_jXT96S2RpuBS6dzvQgpOsen92WxVvS6POVuPc1BQns3ssVjJGerfTSdH9TyTGSXrdtw";
 		countriesList=getCountryList();
 		BufferedWriter wr1 = new BufferedWriter(new FileWriter(Coordinates));
 		BufferedReader sr1 = new BufferedReader(new FileReader(Placenames));
 		//BufferedWriter wr1 = new BufferedWriter(new FileWriter("T:/Trove/data/GeoLoc/Co-ordinates_extra.txt"));
 		//BufferedReader sr1 = new BufferedReader(new FileReader("T:/Trove/data/GeoLoc/placeNames_extra.txt"));
 		int num_articles=0;
 
 		String line = "";
 		String strAddress = "";
 		String coordinates="";
 
 
 		while ((line = sr1.readLine()) != null)
 		{
 			num_articles++;
 			boolean flag = false;
 			String[] strTemp = line.split("#");
 			coordinates=strTemp[0]+" ";
 
 			for (int i = 1; i < strTemp.length; i++)
 			{
 				String name = strTemp[i];
 				if(!name.trim().equals("")){
 					String country=null;
 					//check if it is a country
 					for(int j=0;j<countriesList.size();j++)
 					{
 						if (name.toLowerCase() == countriesList.get(j))
 						{
 							//no add australia
 							flag = true;
 						}
 
 					}
 
 
 					if (flag)
 						strAddress = strTemp[i];
 					else
 						strAddress = strTemp[i] + ", Australia";
 
 					String sPath = "http://maps.google.com/maps/geo?q=" + strAddress + "&output=csv&key=" + strKey;
 					System.out.println(sPath);
 					URL client = new URL(sPath);
 					String thisLine;
 
 
 					BufferedReader theHTML = new BufferedReader(new InputStreamReader(client.openStream()));
 					if((thisLine = theHTML.readLine()) != null)
 					{
 						System.out.println(thisLine);
 						String[] split=thisLine.split(",");
 						if(split[0].equals("200"))
 							coordinates=coordinates+split[2]+","+split[3]+" ";
 						else
 						{
 							theHTML.close();
 							sPath = "http://maps.google.com/maps/geo?q=" + strTemp[i] + "&output=csv&key=" + strKey;
 							client = new URL(sPath);
 							theHTML = new BufferedReader(new InputStreamReader(client.openStream()));
 							if((thisLine = theHTML.readLine()) != null){
 								System.out.println(thisLine);
 								split=thisLine.split(",");
 								if(split[0].equals("200"))
 									coordinates=coordinates+split[2]+","+split[3]+":";
 							}
 
 						}
 						theHTML.close();
 
 					}
 
 				}
 
 			}
 			//     System.out.println(strAddress + ": " +coordinates);
 			wr1.write(coordinates+"\n");
 			if(num_articles%10==0)
 			{
 				System.out.println("Document count: "+num_articles);
 				wr1.flush();
 			}
 
 
 		}
 		sr1.close();
 		wr1.close();
 	
 
 	}
 	//This method enables to count the frequency of the places in a given document
 	public static void FreqCounter(String s[]) throws IOException{
 		BufferedReader inreader=new BufferedReader(new FileReader(s[0]));
 		BufferedWriter outwriter=new BufferedWriter(new FileWriter(s[1]));
 		String line="";
 		Hashtable<String, Integer> hash=new Hashtable<String, Integer>();
 		int count=0;
 			while((line=inreader.readLine())!=null)
 			{
 
 				String[] split=line.split("#");
 				
 				for(int i=1;i<split.length;i++)
 				{
 					if(!split[i].trim().equals(""))
 					{
 					if(!hash.containsKey(split[i]))
 					{
 						hash.put(split[i], 1);
 						//System.out.println("Value added "+ split[i]);
 					}
 					else
 						hash.put(split[i], hash.get(split[i])+1);
 						count++;
 					}
 				
 				}
 				
 				Set freqcount=hash.keySet();
 				Iterator it=freqcount.iterator();
 				String output="";
 				while(it.hasNext())
 				{
 					String freq=(String)it.next();
 					output=output+freq+":"+hash.get(freq)+" ";
 				}
 				
 				outwriter.write(split[0]+" "+output.trim()+"\n");
 				
 			}
 			outwriter.close();
 			inreader.close();
 		
 		}
 
 
 	public static void main(String[] args) throws MalformedURLException, DOMException, IOException, ParserConfigurationException, SAXException, InterruptedException
 	{
 		 GeoCode(args[0],args[1]);
 	
 	}
 }
