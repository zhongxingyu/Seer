package au.org.paperminer.util;

 import java.io.*;
 import java.util.*;
 
 
 public class PlaceOfEventExtractor {
 	
       public static BufferedWriter wrPlace=null;
       private static final String ROOT_FOLDER = "/dev/shm/Trove/SennaOutput/";
     
 	 public static void ContextNer(String filename)
      {
          try
          {
              BufferedReader rd = new BufferedReader(new FileReader(ROOT_FOLDER + filename));
 //             System.out.println(ROOT_FOLDER + filename);
              filename = filename.substring(0, filename.length() - 4); //Remove .txt from the filename
             
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
 
                              if (curner.contains("S-LOC"))
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
 
                              if (curner.contains("S-LOC"))
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
 
 
                          if (curner.contains("S-LOC"))
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
 	public static boolean batchNer(String FileListName, String OutputFileName) throws IOException
     {
         //directory same as batchsentence
         BufferedReader rd = new BufferedReader(new FileReader(FileListName));
         wrPlace = new BufferedWriter(new FileWriter(OutputFileName));
         
         String filename = null;
         int num_articles=0;
 
         while ((filename = rd.readLine()) != null)
         {
             if (filename.contains(".txt"))
             {
                 ContextNer(filename);
                 num_articles++;
                 if(num_articles%1000==0)
                 	System.out.println(num_articles);
 
             }
             
         }
         rd.close();
         wrPlace.close();
         return true;
     }
 	
 	public static String CheckLoc(String word, String wordsense, String ner)
     {
         boolean addsign = (wordsense.contains("NN") || wordsense.contains("NNs") || wordsense.contains("NNP") || wordsense.contains("NNPS"));
         if (ner.contains("O") && !ner.contains("-"))
         {
             if (addsign)
             {
                 return word;
             }
             return null;
         }
         return null;
     }
 	public static void main(String s[]) throws IOException
 	{
 		if(s.length==2)
 		{
 			batchNer(s[0],s[1]);
 			wrPlace.close();
 		}
 		else
 		{
 			System.err.println("Usage: \n java PlaceOfEventExtractor ListOfFileNames PlaceOfEventOutputFile");
 		}
 	}
 
 
 }
