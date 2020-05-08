 package uk.ac.ebi.esd.magetab;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.RandomAccessFile;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import uk.ac.ebi.age.util.StringUtil;
 
 
 public class CustomSDRF
 {
 
  /**
   * @param args
   */
  public static void main(String[] args)
  {
   // set our dummy validator on the parser
   // parser.setValidator(validator);
 
   // now, parse from a file
   // File idfFile = new
   // File("/home/mike/ESD/AE/AE-EXP/E-MEXP-242/E-MEXP-242.idf.txt");
   // File idfFile = new File("F:/BioSD/ae/E-ATMX-12/E-ATMX-12.idf.txt");
 
   Map<String, Set<String> > unitTypes = new HashMap<String, Set<String> >();
   Map<String, Set<String> > characTypes = new HashMap<String, Set<String> >();
   Map<String, Set<String> > otherHeaders = new HashMap<String, Set<String> >();
   Map<String, Set<String> > personHeaders = new HashMap<String, Set<String> >();
   Map<String, Set<String> > pubHeaders = new HashMap<String, Set<String> >();
 
   List<Map<String,String>> persons = new ArrayList<Map<String,String>>(5);
   List<Map<String,String>> terms = new ArrayList<Map<String,String>>(5);
   List<Map<String,String>> pubs = new ArrayList<Map<String,String>>(5);
 
   List<String> persKeys = new LinkedList<String>();
   List<String> termsKeys = new LinkedList<String>();
   List<String> pubsKeys = new LinkedList<String>();
   
   Map<String,String> comments = new LinkedHashMap<String, String>();
 
   Map<String,Map<String,String>> termMap = new HashMap<String, Map<String,String>>();
 
   
   File wDir = new File("/home/mike/BioSD/ae");
   File outDir = new File("/home/mike/BioSD/age-tab3");
 
   for(File expDir : wDir.listFiles())
   {
    try
    {
     if(!expDir.isDirectory())
      continue;
 
     String expName = expDir.getName();
 
 //    if( ! "E-AFMX-3".equals(expName) )
 //     continue;
     
     String expId="GAE-"+expName;
     
     
     String invTitle=null;
     String expDescr=null;
     String relsDate=null;
     String expDate=null;
 
     comments.clear();
     persons.clear();
     terms.clear();
     pubs.clear();
 
     persKeys.clear();
     termsKeys.clear();
     pubsKeys.clear();
     
     File idfFile = new File(expDir, expName + ".idf.txt");
     File sdrfFile = new File(expDir, expName + ".sdrf.txt");
 
     try
     {
 
      if(idfFile.canRead())
      {
       BufferedReader in = new BufferedReader(new FileReader(idfFile));
 
       String str = null;
 
       while((str = in.readLine()) != null)
       {
        String[] strArr = str.split("[\\t]");
 
        if("Investigation Title".equals(strArr[0]) && strArr.length > 1 && strArr[1].trim().length() > 0)
         invTitle = strArr[1];
        else if("Experiment Description".equals(strArr[0]) && strArr.length > 1 && strArr[1].trim().length() > 0)
         expDescr = strArr[1];
        else if("Public Release Date".equals(strArr[0]) && strArr.length > 1 && strArr[1].trim().length() > 0)
         relsDate = strArr[1];
        else if("Date of Experiment".equals(strArr[0]) && strArr.length > 1 && strArr[1].trim().length() > 0)
         expDate = strArr[1];
        else if(strArr[0].startsWith("Person "))
        {
         String key = strArr[0].substring(7);
         persKeys.add(key);
         processIDFObjLine(key, strArr, persons);
         
         registerTerm(key, expName, personHeaders);
        }
        else if(strArr[0].startsWith("Term Source "))
        {
         String key = strArr[0].substring(12);
         termsKeys.add(key);
         processIDFObjLine(key, strArr, terms);
        }
        else if(strArr[0].startsWith("Publication Status Term Source"))
        {
         String key = "Status[Term Source]";
         pubsKeys.add(key);
         processIDFObjLine(key, strArr, pubs);
         
         registerTerm(key, expName, pubHeaders);
        }
        else if(strArr[0].startsWith("Publication "))
        {
         String key = strArr[0].substring(12);
         pubsKeys.add(key);
         processIDFObjLine(key, strArr, pubs);
         
         registerTerm(key, expName, pubHeaders);
        }
        else if(strArr[0].startsWith("PubMed "))
        {
         String key = "PubMed ID";
         pubsKeys.add(key);
         processIDFObjLine(key, strArr, pubs);
         
         registerTerm(key, expName, pubHeaders);
        }
        else if(strArr[0].startsWith("Comment[") && strArr.length > 1 && strArr[1].trim().length() > 0)
        {
         comments.put(strArr[0].substring(8, strArr[0].length() - 1), strArr[1]);
        }
       }
 
       in.close();
      }
     }
     catch (Exception e) 
     {
      e.printStackTrace();
     }
     
     System.out.println("Parsing " + idfFile.getAbsolutePath() + "...");
 
 
     PrintStream out=null;
     
     
     File outF = new File(outDir,expName+".age.txt");
    
     SDRF sdrf = readSDRF(sdrfFile);
 
     if( sdrf.header == null )
      continue;
     
     out=null;
     try
     {
      out = new PrintStream(outF);
     }
     catch(FileNotFoundException e)
     {
      System.out.println("Can't open file for writing: "+outF.getAbsolutePath());
      continue;
     }
     
     out.print("Group\tDescription\tData Source\tLink");
     
     if(expDescr!=null)
      out.print("\tExperiment Description");
     
     if( relsDate!= null)
      out.print("\tPublic Release Date");
 
     if( expDate!= null)
      out.print("\tDate of Experiment");
     
     for(String comm : comments.keySet() )
      out.print("\tComment{"+comm+"}");
     
     out.print("\n");
     
     out.print(expId);
     out.print("\t");
     out.print(invTitle);
    out.print("\tArray Express\thttp://www.ebi.ac.uk/arrayexpress/experiments/"+expName);
 
     if(expDescr!=null)
      out.print("\t"+expDescr);
     
     if( relsDate!= null)
      out.print("\t"+relsDate);
 
     if( expDate!= null)
      out.print("\t"+expDate);
     
     for(String comm : comments.values() )
      out.print("\t"+comm);
     
     out.print("\n");
     
     persKeys.add("contactOf");
     for( Map<String,String> p : persons )
     {
      if( p!=null)
      {
       if( p.containsKey("Roles Term Source REF") )
       {
        p.put("Roles[Term Source]", p.get("Roles Term Source REF") );
        p.remove("Roles Term Source REF");
       }
       
       p.put("contactOf", expId);
      }
     }
     
     pubsKeys.add("publicationAbout");
     for( Map<String,String> p : pubs )
      if( p!=null)
       p.put("publicationAbout", expId);
     
     printBlock(persKeys,persons,"Person",out);
     printBlock(pubsKeys,pubs,"Publication",out);
    
     if( terms.size() > 0 )
     {
      termMap.clear();
     
      boolean hasFile=false;
      boolean hasVer=false;
 
      for( Map<String,String> t : terms)
      {
       if( t != null )
       {
        String nm = t.get("Name");
        
        if( nm == null || nm.length() == 0 )
         continue;
        
        Map<String,String> exMap = termMap.get(nm);
        
        if( exMap == null )
         termMap.put(t.get("Name"), exMap=t);
        else
         exMap.putAll(t);
        
        String val = exMap.get("File");
        if( val != null && val.length() > 0 )
         hasFile = true;
 
        val = exMap.get("Version");
        if( val != null && val.length() > 0 )
         hasVer = true;
       }
      }
      
      out.print("\nTerm Source");
      
      if( hasFile )
       out.print("\tFile");
 
      if( hasVer )
       out.print("\tVersion");
      
      
      for( Map.Entry<String, Map<String,String>> me : termMap.entrySet())
      {
       out.print("\n");
       out.print("$"+me.getKey());
      
       if( hasFile )
       {
        String val = me.getValue().get("File");
        
        if( val == null )
         val="";
        
        out.print("\t"+val);
       }
 
       if( hasVer )
       {
        String val = me.getValue().get("Version");
        
        if( val == null )
         val="";
        
        out.print("\t"+val);
       }
      }
     
      out.print("\n");
     }
     
     
     
     ArrayList<List<String>> sampls = new ArrayList<List<String>>(sdrf.samples.size());
     sampls.addAll(sdrf.samples.values());
     
     Collections.sort(sampls, new Comparator<List<String>>()
     {
      @Override
      public int compare(List<String> o1, List<String> o2)
      {
       return o1.get(0).compareTo(o2.get(0));
      }
     });
     
     int hdSize = sdrf.header.size();
     
     out.print("\nSample\tbelongsTo\n*\t"+expId);
     
     out.print("\n\nSample\tName");
     
     String mainAttr = null;
     for(int i = 1; i < hdSize; i++)
     {
      String name = sdrf.header.get(i);
 
      if(name.startsWith("Characteristics"))
      {
       Qualified qname = parseQualified(name);
 
       registerTerm(qname.qualifier, expName, characTypes);
 
       out.print("\tCharacteristics{"+qname.qualifier+"}");
       
       mainAttr = "Characteristics{"+qname.qualifier+"}";
      }
      else if(name.startsWith("Unit"))
      {
       Qualified qname = parseQualified(name);
 
       registerTerm(qname.qualifier, expName, unitTypes);
       
       out.print("\t"+mainAttr+"[Unit{"+qname.qualifier+"}]");
       
       mainAttr=mainAttr+"[Unit{"+qname.qualifier+"}]";
      }
      else if( name.equalsIgnoreCase("Term Source REF") )
      {
       out.print("\t"+mainAttr+"[Term Source]");
       registerTerm("[Term Source]", expName, otherHeaders);
      }
      else if( name.equalsIgnoreCase("Term Accession Number") )
      {
       out.print("\t"+mainAttr+"["+name+"]");
       registerTerm("[Term Accession Number]", expName, otherHeaders);
      }
      else if( name.startsWith("Comment") )
      {
       Qualified qname = parseQualified(name);
       out.print("\tComment{"+qname.qualifier+"}");
       mainAttr="Comment{"+qname.qualifier+"}";
      }
      else
      {
       mainAttr = name;
       registerTerm(name, expName, otherHeaders);
       out.print("\t"+name);
      }
     }
     
 //    out.print("\tbelongsTo");
 
     
     for( int i=0; i < sampls.size(); i++ )
     {
      out.print("\nSAE-"+expName+"-"+(i+1));
      
      List<String> vals = sampls.get(i);
      
      for( int j=0; j < hdSize; j++)
      {
       if( j < vals.size() )
        out.print("\t"+vals.get(j));
       else
        out.print("\t");
      }
      
 //     out.print("\t"+expId);
     }
     
     out.print("\n");
     
     if( out != null )
      out.close();
     // print out the parsed investigation
     // System.out.println(investigation);
 
    }
    catch(MalformedURLException e)
    {
     // This is if the url from the file is bad
     e.printStackTrace();
    }
    catch(IOException e)
    {
     // TODO Auto-generated catch block
     e.printStackTrace();
    }
 
   }
 
   try
   {
    RandomAccessFile log = new RandomAccessFile(new File(outDir,".log"), "rw");
 
    log.writeChars("Characteristics:\n");
    for(Map.Entry<String, Set<String>> me : characTypes.entrySet() )
    {
     log.writeChars("  "+me.getKey()+"\n");
     
     for( String exp : me.getValue() )
      log.writeChars("    "+exp+"\n");
    }
    
    log.writeChars("\nUnits:\n");
    for(Map.Entry<String, Set<String>> me : unitTypes.entrySet() )
    {
     log.writeChars("  "+me.getKey()+"\n");
     
     for( String exp : me.getValue() )
      log.writeChars("    "+exp+"\n");
    }
    
    
    log.writeChars("\nPerson props:\n");
    for(Map.Entry<String, Set<String>> me : personHeaders.entrySet() )
    {
     log.writeChars("  "+me.getKey()+"\n");
     
     for( String exp : me.getValue() )
      log.writeChars("    "+exp+"\n");
    }
    
    log.writeChars("\nPublication props:\n");
    for(Map.Entry<String, Set<String>> me : pubHeaders.entrySet() )
    {
     log.writeChars("  "+me.getKey()+"\n");
     
     for( String exp : me.getValue() )
      log.writeChars("    "+exp+"\n");
    }
   
    log.writeChars("\nOther props:\n");
    for(Map.Entry<String, Set<String>> me : otherHeaders.entrySet() )
    {
     log.writeChars("  "+me.getKey()+"\n");
     
     for( String exp : me.getValue() )
      log.writeChars("    "+exp+"\n");
    }
  
    
    log.close();
   }
   catch(FileNotFoundException e)
   {
    // TODO Auto-generated catch block
    e.printStackTrace();
   }
   catch(IOException e)
   {
    // TODO Auto-generated catch block
    e.printStackTrace();
   }
   
   
  }
 
  static class Qualified
  {
   String attr;
   String qualifier;
  }
 
  static Qualified parseQualified(String str)
  {
   Qualified q = new Qualified();
 
   int pos = str.indexOf('[');
 
   if(pos == -1)
   {
    q.attr = str;
    return q;
   }
 
   q.attr = str.substring(0, pos).trim();
 
   q.qualifier = str.substring(pos + 1).trim();
 
   if(q.qualifier.charAt(q.qualifier.length() - 1) != ']')
    System.err.println("Invalid qualified: " + str);
   else
    q.qualifier = q.qualifier.substring(0, q.qualifier.length() - 1).trim();
 
   return q;
  }
 
  private static void processIDFObjLine(String key, String[] strArr, List<Map<String, String>> persons)
  {
   int dif=strArr.length -1 - persons.size();
   if( dif > 0 )
   {
    for( int i=0; i < dif; i++ )
     persons.add(null);
   }
   
   
   for( int i=0; i < strArr.length -1; i++ )
   {
    String val = strArr[i+1].trim();
    
    if( val.length() == 0 )
     continue;
    
    Map<String, String> obj = persons.get(i);
    
    if( obj == null )
    {
     obj=new HashMap<String, String>();
     persons.set(i, obj);
    }
    
    obj.put(key, val);
   }
   
  }
  
  private static void printBlock( List<String> keys, List<Map<String,String>> data, String name, PrintStream out)
  {
   if( data.size() == 0 )
    return;
   
    Iterator<String> kIter = keys.iterator();
    while( kIter.hasNext() )
    {
     String key = kIter.next();
     boolean has=false;
     for( Map<String,String> obj : data )
     {
      if( obj != null && obj.containsKey(key) )
      {
       has=true;
       break;
      }
     }
     
     if( !has )
      kIter.remove();
    }
 
   out.print("\n");
   out.print(name);
   
   for( String k : keys )
    out.print("\t"+k);
   
   int ind=1;
   for( Map<String,String> obj : data )
   {
    if( obj == null )
     continue;
    
    out.print("\n?"); //+(ind++)
 
    for( String k : keys )
    {
     out.print("\t");
     
     String val = obj.get(k);
     
     if(val != null)
      out.print(val);
    }
   }
   out.print("\n");
 
  }
  
  static class SDRF
  {
   List<String> header;
   Map<String, List<String> > samples = new HashMap<String, List<String>>();
  }
  
  
  private static SDRF readSDRF( File sdrfFile ) throws IOException
  {
   RandomAccessFile file = new RandomAccessFile(sdrfFile, "r");
   
   String line=null;
   
   SDRF res = new SDRF();
   
   int hdSize = -1;
   
   List<String> spLine = new ArrayList<String>(100);
 
   while( (line = file.readLine()) != null )
   {
    spLine.clear();
    StringUtil.splitExcelString(line, "\t", spLine);
    
    
    if( hdSize == -1 )
    {
     if( spLine.size() == 0 || ! spLine.get(0).trim().matches("^Source\\s+Name$") )
      continue;
     
     int i=1;
     for( ; i<spLine.size(); i++ )
     {
      String hd = spLine.get(i).trim();
      
      if( hd.endsWith(" Name") ||  hd.startsWith("Protocol ") || hd.endsWith("Protocol REF") )
       break;
     }
     
     hdSize = i;
     
     List<String> hdrs = new ArrayList<String>(hdSize);
     hdrs.addAll(spLine.subList(0, hdSize));
     
     res.header=hdrs;
    }
    else
    {
     if( spLine.size() == 0 )
      continue;
     
     String nm = spLine.get(0).trim();
     
     if( nm.length() == 0 )
      continue;
     
     List<String> vals = new ArrayList<String>(hdSize);
 
     if( spLine.size() < hdSize )
      vals.addAll(spLine);
     else
      vals.addAll( spLine.subList(0, hdSize) );
 
     res.samples.put(nm,vals);
     
    }
   }
   
   file.close();
   
   return res;
  }
  
  static void registerTerm(String term, String exp, Map<String, Set<String>> map)
  {
   Set<String> set = map.get(term);
   
   if( set == null )
   {
    map.put(term, set = new HashSet<String>() );
    set.add(exp);
   }
   else if( set.size() < 15 )
    set.add(exp);
   
   
  }
 }
