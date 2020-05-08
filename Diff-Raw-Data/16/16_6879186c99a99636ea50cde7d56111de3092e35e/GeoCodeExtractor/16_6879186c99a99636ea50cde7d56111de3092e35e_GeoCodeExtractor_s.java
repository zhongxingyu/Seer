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
 import org.xeustechnologies.jtar.TarEntry;
 import org.xeustechnologies.jtar.TarInputStream;
 import org.xml.sax.SAXException;
 
 
 
 public  class GeoCodeExtractor
 
 {
 	
 	public static ArrayList countriesList=null;
 	public static BufferedWriter wrPlace=null;
 	private static final String ROOT_FOLDER = "/dev/shm/Trove/";
 	
 
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
 
 	
 	public static String afterline(BufferedReader sr, String curline) throws IOException
 	{
 	
 		String afterline = sr.readLine();
 		if (afterline != null)
 			return afterline;
 		else
 			return curline;
 	}
 	public static void linecheck() throws IOException
 	{
 		//read any file
 		BufferedReader sr = new BufferedReader(new FileReader("content.txt"));
 		String line = sr.readLine();
 		while (line != null)
 		{
 			//store current line
 			String curline = line;
 			//get aftline
 			String aftline = afterline(sr, curline);
 			//output current and after line
 			if (aftline != curline)
 			{
 				System.out.println("currentline: " + line + " aftline: " + aftline);
 				//continue loop
 				line = aftline;
 			}
 	   	   else
 			{
 				System.out.println("currentline: " + line + " aftline: " + "no more line");
 				//break loop
 				line = null;
 			}
 			
 		}
 	}
 	
 
 	public static BufferedWriter CreateFile(String filename) throws IOException
 	{
 		BufferedWriter wr = new BufferedWriter(new FileWriter(ROOT_FOLDER+"content/" + filename + ".txt"));
 		return wr;
 	}
 	
 
 	//This is to untar the documents collection and then apply senna.
 	//As it is expensive to untar all the files TarInputStream is used which could
 	//read tar files directly.
 	public static void batchNer(String tarFile,  String destFolder, int begin_idx, int end_idx) throws IOException, InterruptedException
 	//public static void batchNer(String tarFile,  String destFolder) throws IOException, InterruptedException
 	{
 		
 		 wrPlace = new BufferedWriter(new FileWriter("/home/kutty/Trove/data/GeoLoc/placeNames_colln_"+begin_idx+"_"+end_idx+".txt"));
          String str = null;
 
 		// Create a TarInputStream
 		for(int i=begin_idx;i<=end_idx;i++)
 		{
 		File TarName=new File(tarFile+i+".tar");
         //ile TarName=new File(tarFile+".tar");
 		if(TarName.exists())
 		{
 	    TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(tarFile+i+".tar")));
 		//TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(tarFile+".tar")));
 		TarEntry entry;
 		int num_articles=0;
 		String tempdestFolder=destFolder; 
 		int block_size=0;
 		ArrayList<String> fileList=new ArrayList<String>();
 		
 		
 		while((entry = tis.getNextEntry()) != null) 
 		{
 			
 			byte data[] = new byte[2048];
 			String filename="/dev/shm/Trove/"+entry.getName();
 			String filen=entry.getName();
 			if(!fileList.contains(filen))
 			{
 			  fileList.add(filen);
 			//System.out.println(filename);
 			
 			File file=new File(filename);
 			if(filename.contains(".txt"))
 			{
   		            int count;
 					String ParentFolder=file.getParent();
 					(new File(ParentFolder)).mkdirs();
 					FileOutputStream fos = new FileOutputStream(filename);
 					BufferedOutputStream dest = new BufferedOutputStream(fos);
 
 					while((count = tis.read(data)) != -1) {
 						dest.write(data, 0, count);
 					}
 
 					dest.flush();
 					dest.close();
 					fos.close();
 
 					
 					num_articles++;
 					if(num_articles%100==0)
 					  {
 					    System.out.println("Number of files processed by Senna:"+ num_articles);
 						wrPlace.flush();
 					  }
 						
 					 if(!(new File(destFolder)).exists())
 							new File(destFolder).mkdirs();
 											
 					ContextNer(filename);
 					
 				/*	if(file.exists())
 					  file.delete();*/
 					
 					
 		
 			}
 			}
 		}
 		tis.close();
 		}
 		else
 			System.out.println("Missing tar file : "+tarFile+i+".tar");
 		}
 		wrPlace.close();
 	}
 	public static void getGeoCodeForPublisher(String InputFile, String OutputFile) throws MalformedURLException, DOMException, IOException, ParserConfigurationException, SAXException
     {
         
         BufferedWriter wr1 = new BufferedWriter(new FileWriter(OutputFile));
         BufferedReader sr1 = new BufferedReader(new FileReader(InputFile));
         int num_articles=0;
         
             String line = "";
             String strAddress = "";
             String coordinates="";
            
             sr1.readLine();
             while ((line = sr1.readLine()) != null)
             {
             	//System.out.println(num_articles++);
             	num_articles++;
                 String[] strTemp = line.split("\\t");
                 if(strTemp.length>1){
                 	
                 coordinates=strTemp[0]+","+strTemp[1]+","+strTemp[2];
                
                 
                     String name = strTemp[3];
                     //String[] PlaceName=strTemp[2].split(" ");
                     //String cityname=PlaceName[PlaceName]
                     if(!name.trim().equals("N/A")){
                         strAddress = strTemp[3].trim()+",Australia" ;
 
                                             
                  coordinates=coordinates+","+getCoOrdinates(strTemp[0], strAddress,0);
                     
                 wr1.write(coordinates+"\n");
                 if(num_articles%10==0)
                 {
                 	System.out.println("Document count: "+num_articles);
                 	wr1.flush();
                 }
                 
                     }
             }
             }
             sr1.close();
             wr1.close();
       
         
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
 	
 	
 	 /* public static boolean batchNer() throws IOException
       {
           //directory same as batchsentence
         //  BufferedReader rd = new BufferedReader(new FileReader("/home/kutty/Trove/SennaOutput/list.txt"));
           wrPlace = new BufferedWriter(new FileWriter("/home/kutty/Trove/data/GeoLoc/PlaceNames.txt"));
           String str = null;
 
          
           String filename = null;
 
          
               if (!str.contains("list"))
               {
                   filename = str;
                   ContextNer(filename);
               }
 
           }
           rd.close();
           wrPlace.close();
           return true;
       }*/
 
 
 
 	public static void complete()
 	{
 
 	}
 
 	public static String CheckLoc(String word, String wordsense, String ner)
 	{
 		boolean addsign = (wordsense.contains("NN") || wordsense.contains("NNs") || wordsense.contains("NNP") || wordsense.contains("NNPS"));
 		if (ner.contains("O") && !ner.contains("-"))
 		{
 			if (addsign)
 			{
 				//System.out.println(word.Trim() + ", " + "LOC");
 				//wr.WriteLine(word.Trim() + ", " + "LOC");
 				return word;
 			}
 			return null;
 		}
 		return null;
 	}
 	public static void ContextNer(String filename)
     {
         try
         {
             //BufferedReader rd = new BufferedReader(new FileReader("/home/kutty/Trove/SennaOutput/" + filename));
 			BufferedReader rd = new BufferedReader(new FileReader(filename));
            // System.out.println(filename);
             filename = filename.substring(0, filename.length() - 4); //Remove .txt from the filename
 
             //
             wrPlace.write(filename + " ");
             String line = null;
             ArrayList ar = new ArrayList();
             String extraloc = null;
 
             while ((line = rd.readLine()) != null)
             {
             	if(!line.trim().equals(""))
                 ar.add(line);
             }
 
             String[] curtoken = null;
             String[] afttoken = null;
             String[] pretoken = null;
             String loc = null;
 
             for (int i = 1; i <= (ar.size() - 2); i++)
             {
                 //ar contains more than three records
                 if ((ar.size() - 2) >= 3)
                 {
                     //first line
 
                     if (i == 1)
                     {
                         String checkfirstline = ar.get(1).toString();
                         if (checkfirstline != "")
                         {
                             int j = i + 1;
 
                             String curline = ar.get(i).toString();
                             curtoken = curline.split("\t");
                             String curword = curtoken[0];
                             String curwordsense = curtoken[1];
                             String curner = curtoken[3];
 
                             String aftline = ar.get(j).toString();
                             afttoken = aftline.split("\t");
                             String aftword = afttoken[0];
                             String aftwordsense = afttoken[1];
                             String aftner = afttoken[3];
                             String addaftword = CheckLoc(aftword, aftwordsense, aftner);
 
                             if (curner.contains("S-LOC") )//|| curner.contains("S-ORG") )
                             {
                                 //wrPlacePlace.WriteLine(curword.Trim() + ", " + "LOC");
                                 String word = curword.trim();
                                 String extra = "#";
                                 if (addaftword != null)
                                 {
                                     extra = (word + " " + addaftword.trim());
                                 }
 
                                 //wrPlace.WriteLine(extra + "," + "LOC");
                                 wrPlace.write("#" + extra + "# ");
                                 //extra = null;
                             }
                         }
 
 
                     }
 
                     if (1 < i && i < (ar.size() - 2))
                     {
                         String checkpreline = ar.get(i - 1).toString();
                         String checkcurline = ar.get(i).toString();
                         String checkaftline = ar.get(i + 1).toString();
                         if (checkcurline != "" && checkaftline != "" && checkpreline != "")
                         {
                             int j = i + 1;
 
                             pretoken = ar.get(i - 1).toString().split("\t");
                             String preword = pretoken[0];
                             String prewordsense = pretoken[1];
                             String prener = pretoken[3];
                             String addpreword = CheckLoc(preword, prewordsense, prener);
 
                             String curline = ar.get(i).toString();
                             curtoken = curline.split("\t");
                             String curword = curtoken[0];
                             String curwordsense = curtoken[1];
                             String curner = curtoken[3];
 
                             String aftline = ar.get(j).toString();
                             afttoken = aftline.split("\t");
                             String aftword = afttoken[0];
                             //System.out.println(aftline);
                             //System.out.println("After token is "+aftword);
                             String aftwordsense = afttoken[1];
                             String aftner = afttoken[3];
                             String addaftword = CheckLoc(aftword, aftwordsense, aftner);
 
                             if (curner.contains("S-LOC"))//||curner.contains("S-ORG"))
                             {
                                 //wrPlace.WriteLine(curword.Trim() + ", " + "LOC");
                                 String word = curword.trim().toLowerCase();
                                 if (addpreword != null)
                                 {
                                     extraloc = "#" + (addpreword.trim() + " " + word);
                                     if (addaftword != null)
                                     {
                                         extraloc = (extraloc + " " + addaftword.trim().toLowerCase());
                                        
                                     }
                                 }
                                 else
                                 {
                                     extraloc = word;
                                     if (addaftword != null)
                                     {
                                         extraloc = (extraloc + " " + addaftword.trim().toLowerCase());
                                     }
                                 }
                                 //wrPlace.WriteLine(extraloc + "," + "LOC");
                                 wrPlace.write("#" + extraloc.toLowerCase() + "# ");
                                 extraloc = null;
                             }
                             if (curner.contains("B-LOC"))
                             {
                                 loc = curword.trim().toLowerCase();
                                 if (addpreword != null)
                                 {
                                     loc = (addpreword.trim().toLowerCase() + " " + loc);
                                 }
                             }
 
                             if (curner.contains("I-LOC"))
                                 loc += (" " + curword.trim().toLowerCase());
                             if (curner.contains("E-LOC"))
                             {
                                 loc += (" " + curword.trim());
                                 if (addaftword != null)
                                 {
                                     loc = (loc + " " + addaftword.trim());
                                 }
                                 wrPlace.write("#" + loc.toLowerCase() + "# ");
                                 loc = null;
                             }
                         }
 
                     }
                     if (i == (ar.size() - 2))
                     {
                         pretoken = ar.get(i - 1).toString().split("\t");
                         String preword = pretoken[0];
                         //  if (preword.Length > 1)
                         //{
                         String prewordsense = pretoken[1];
                         String prener = pretoken[3];
                         String addpreword = CheckLoc(preword, prewordsense, prener);
 
                         String curline = ar.get(i).toString();
                         curtoken = curline.split("\t");
                         String curword = curtoken[0];
                         String curwordsense = curtoken[1];
                         String curner = curtoken[3];
 
 
                         if (curner.contains("S-LOC"))// || curner.contains("S-ORG"))
                         {
                             //wrPlace.WriteLine(curword.Trim() + ", " + "LOC");
                             String word = curword.trim().toLowerCase();
                             String extra = null;
                             if (addpreword != null)
                             {
                                 extra = (addpreword.trim().toLowerCase() + " " + word);
                             }
 
                             //wrPlace.WriteLine(extra + "," + "LOC");
                             wrPlace.write("#" + extra + "# ");
                             //extra = null;
                         }
                         //}
                     }
                 }
             }
             wrPlace.write("\n");
             //
             //wrPlace.Close();
             rd.close();
             ar = null;
         }
         catch (IOException ex)
         {
 
         }
     }
 	public static void GeoCode() throws MalformedURLException, DOMException, IOException, ParserConfigurationException, SAXException
 	{
 		String strKey = "ABQIAAAAWxzgJsD_pv_jXT96S2RpuBS6dzvQgpOsen92WxVvS6POVuPc1BQns3ssVjJGerfTSdH9TyTGSXrdtw";
 		countriesList=getCountryList();
 		BufferedWriter wr1 = new BufferedWriter(new FileWriter("T:/Trove/data/GeoLoc/Co-ordinates_extra.txt"));
 		BufferedReader sr1 = new BufferedReader(new FileReader("T:/Trove/data/GeoLoc/placeNames_extra.txt"));
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
 							//System.out.println(name.ToLower() + " country");
 
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
 	
 	if(args.length==2)
 		{
 		  batchNer("/home/kutty/Trove/SennaOutput/tarFiles/Processed-Docs-part","/home/kutty/Trove/tempOutput",Integer.parseInt(args[0]),Integer.parseInt(args[1]));
 		  //batchNer("T:/Trove/SennaOutput/tarFiles/Processed-Docs-part-specific","/home/kutty/Trove/tempOutput");
 		  //GeoCode();
 		  }
 		  
 	//   GeoCode();
 	else
 		{
 		System.err.println("Usage:\n java GeoCodeExtractor <begin_idx> <end_idx>");
 		}
 
 	}
 }
