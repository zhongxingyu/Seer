 
 package ngrams;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.TreeMap;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import frequency.HibernateUtil;
 
 
 
 
 
 
 /**
  *  This program will read words from an input file, and count the
  *  number of occurrences of each word.  The word data is written to
  *  an output file twice, once with the words in alphabetical order
  *  and once with the words ordered by number of occurrences.  The
  *  user specifies the input file and the output file.
  *
  *  The program demonstrates several parts of Java's framework for
  *  generic programming:  TreeMap, List sorting, Comparators, etc.
  */
 public class ConditionalLikelyHood {
 	
 	Session session = HibernateUtil.getSessionFactory().openSession();
    /**
     * Represents the data we need about a word:  the word and
     * the number of times it has been encountered.
     */
 	private static class WordData { 
 	      String bigram;
 	      int count;
 	      float probability;
 	      
 	      WordData(String w) {
 	         bigram = w;
 	         count = 1;  // The initial value of count is 1.
 	      }	
 	   } // end class WordData
 
 
    /**
     * A comparator for comparing objects of type WordData according to
     * their counts.  This is used for sorting the list of words by frequency.
     */
    private static class CountCompare implements Comparator<WordData> {
       public int compare(WordData data1, WordData data2) {
          return data2.count - data1.count;
              // The return value is positive if data2.count > data1.count.
              // I.E., data1 comes after data2 in the ordering if there
              // were more occurrences of data2.word than of data1.word.
              // The words are sorted according to decreasing counts.
       }
    } // end class CountCompare
 
 
    public static void main(String[] args) {
       System.out.println("\n\nThis program will ask you to select an input file");
       System.out.println("It will calculate Conditional Likely Hood for the document");
       System.out.print("Press return to begin.");
       TextIO.getln();  // Wait for user to press return.
 
       try {
            
 	         if (CDLHTextIO.readUserSelectedFilesonebyone()==false) {
 	            System.out.println("No input file selected.  Exiting.");
 	            System.exit(1);
 	         }
 	         //TextIO.readUserSelectedFilesonebyone();  
       	}      
                 catch (Exception e) {
                 System.out.println("Sorry, an error has occurred.");
                 System.out.println("Error Message:  " + e.getMessage());
                 e.printStackTrace();
                 }
              System.exit(0);  // Might be necessary, because of use of file dialogs.
    		}
      
       public  void bigramCondLH(String inputFileName,String inputFilePath) throws IOException
       {
     	  
     	  System.out.println(" i am here for "+inputFileName);
     	  HashMap <String,Integer> h=new HashMap<String,Integer>();//outer is the hashmap on first word
     	  
     	  String word = readNextWord();
           String previousWord=null;
           String bigram=null;
           
           Float CDL=0.0f;
           int K=0;//no of N Grams
         
           
           while(word!=null)
           {
          	 word = word.toLowerCase();
          	 if(word.endsWith("."))
                	word=word.substring(0, word.length()-1);
          	 
          	 if(word.endsWith(":"))
                 	word=word.substring(0, word.length()-1);
                
          	 if(previousWord!=null)
          	 {
          		 bigram=previousWord+" "+word;
          		 //System.out.println("bigram being considered is "+bigram);
          		 
          		 if(!h.containsKey(bigram))
          		 {
          			 h.put(bigram,1);//first time inserting into outer hashmap
          			 
          			 Transaction tx= session.beginTransaction();
          			 //String SQL_QUERY="select bigram,count,probability from ngrams.BiGram_POJO where bigram=:bigram";
          			 String SQL_QUERY="select bigram,counter,probability from BiGram_POJO where bigram=:wordinQuery";
          			 Query query=session.createQuery(SQL_QUERY);
          			 query.setParameter("wordinQuery",bigram);
          			
          			 for(Iterator it=query.iterate();it.hasNext();){
                    	 Object[] row = (Object[]) it.next();
      	 
                    	 Double add= Math.log((Float)row[2]);
                    	 //System.out.println("The probability for the bigram "+bigram+" is "+row[2]+" whose ln is "+add.floatValue());
                    	 //System.out.println("CDL before was"+CDL);
                    	 CDL=CDL+add.floatValue();
                    	 //System.out.println("CDL after is"+CDL);
                    	 K++;
                     }
          			 
          		 }
          		 else 
         		 {
         			Integer count=h.get(bigram);
         			count++;
          			h.put(bigram,count);
          			//System.out.println("This bigram has already been considered");
         		 }
          		 
          	}
          	 previousWord=word;
          	 word=readNextWord();   	 
          	 
           }
           System.out.println("The CDL is"+CDL);
           System.out.println("The no of unique bigrams is "+K);
           CDL=(Float)CDL/K*-1;
           
     	  
     	  System.out.println("CDL for document"+inputFileName+"is"+CDL);
     	  
     	  Transaction tx1= session.beginTransaction();
 		  CDLScore2_POJO db=new CDLScore2_POJO(inputFileName,CDL);
 		  session.save(db);
 		  tx1.commit();
 
        }
        
       public  void trigramCondLH(String inputFileName,String inputFilePath) throws IOException
       {
     	  
     	  System.out.println(" i am here for "+inputFileName);
     	  HashMap <String,Integer> h=new HashMap<String,Integer>();//outer is the hashmap on first word
     	  
     	  
           String firstword=readNextWord();
           String secondword=readNextWord();
           
           String word = readNextWord();
           String trigram=null;
     
           Float CDL=0.0f;
           int K=0;//no of N Grams
         
           
           while(word!=null)
           {
         	  String previous2Words=firstword+" "+secondword;
         	  word = word.toLowerCase();
          	 if(word.endsWith("."))
                	word=word.substring(0, word.length()-1);
          	 
          	 if(word.endsWith(":"))
                 	word=word.substring(0, word.length()-1);
                
          	 if(previous2Words!=null)
          	 {
          		 trigram=previous2Words+" "+word;
          		 System.out.println("trigram being considered is "+trigram);
          		 
          		 if(!h.containsKey(trigram))
          		 {
          			 h.put(trigram,1);//first time inserting into outer hashmap
          			 
          			 Transaction tx= session.beginTransaction();
          			 //String SQL_QUERY="select bigram,count,probability from ngrams.BiGram_POJO where bigram=:bigram";
          			 String SQL_QUERY="select trigram,counter,probability from TriGram_POJO where trigram=:wordinQuery";
          			 Query query=session.createQuery(SQL_QUERY);
          			 query.setParameter("wordinQuery",trigram);
          			
          			 for(Iterator it=query.iterate();it.hasNext();){
                    	 Object[] row = (Object[]) it.next();
      	 
                    	 Double add= Math.log((Float)row[2]);
                    	 System.out.println("The probability for the trigram "+trigram+" is "+row[2]+" whose ln is "+add.floatValue());
                    	 System.out.println("CDL before was"+CDL);
                    	 CDL=CDL+add.floatValue();
                    	 System.out.println("CDL after is"+CDL);
                    	 K++;
                     }
          			 
          		 }
          		 else 
         		 {
         			Integer count=h.get(trigram);
         			count++;
          			h.put(trigram,count);
          			//System.out.println("This bigram has already been considered");
         		 }
          		 
          	}
          	 firstword=secondword;
          	 secondword=word;
          	 word=readNextWord();   	 
          	 
           }
           System.out.println("The CDL is"+CDL);
           System.out.println("The no of unique trigrams is "+K);
           CDL=(Float)CDL/K*-1;
           
     	  
     	  System.out.println("CDL for document"+inputFileName+"is"+CDL);
     	  
     	  Transaction tx1= session.beginTransaction();
 		  //CDLScore3_POJO db=new CDLScore3_POJO(inputFileName,CDL);
 		  //session.save(db);
     	  Query query = session.createQuery("update ngrams.CDLScore3_POJO set CDLScore_Spam_Data_Set='"+ CDL+"' where inputFileName= '"+inputFileName+"'");
     	  int update = query.executeUpdate();
     	  System.out.println("Updated "+update);
 		  tx1.commit();
 
        }
        
       
       
       public  void trigramCondLHLapalceSmoothing(String inputFileName,String inputFilePath) throws IOException
       {
     	  
     	  System.out.println(" i am here for "+inputFileName);
     	  HashMap <String,Integer> h=new HashMap<String,Integer>();//outer is the hashmap on first word
     	  
           String firstword=readNextWord();
           String secondword=readNextWord();
           
           String word = readNextWord();
           String trigram=null;
     
           Float CDL=0.0f;
           int K=0;//no of N Grams
         
           
           while(word!=null)
           {
         	  String previous2Words=firstword+" "+secondword;
         	  word = word.toLowerCase();
          	 if(word.endsWith("."))
                	word=word.substring(0, word.length()-1);
          	 
          	 if(word.endsWith(":"))
                 	word=word.substring(0, word.length()-1);
                
          	 if(previous2Words!=null)
          	 {
          		 trigram=previous2Words+" "+word;
          		 System.out.println("trigram being considered is "+trigram);
          		 
          		 if(!h.containsKey(trigram))
          		 {
          			 h.put(trigram,1);//first time inserting into outer hashmap
          			 
          			 Transaction tx= session.beginTransaction();
          			 //String SQL_QUERY="select bigram,count,probability from ngrams.BiGram_POJO where bigram=:bigram";
          			 String SQL_QUERY="select trigram,counter,probability from TriGram_POJO_LaplaceSmoothing_NonSpam where trigram=:wordinQuery";
          			 Query query=session.createQuery(SQL_QUERY);
          			 query.setParameter("wordinQuery",trigram);
          			
          			 
          			 	if(query.list().size()==0) //trigram does not exist in DB
          			 	{
          			 		 System.out.println("trigram does not exist :"+trigram);
          			 		 
          			 		 String temp[]=null;
          			 		 temp=trigram.split(" ");
          			 		 trigram="";
          			 		 
          			 		 for(int i=0;i<2;i++)
          			 		 {
          			 			 trigram=trigram+temp[i]+" ";
          			 		 }
          			 		 
          			 		 trigram=trigram+"<unknown>";
          			 		 System.out.println("Unknown trigram formed is :"+trigram);
          			 			 
          			 		 SQL_QUERY="select trigram,counter,probability from ngrams.TriGram_POJO_LaplaceSmoothing_NonSpam where trigram=:wordinQuery";
                 			 query=session.createQuery(SQL_QUERY);
                 			 query.setParameter("wordinQuery",trigram);
                 			 
                 			 for(Iterator it=query.iterate();it.hasNext();){
               			 		Object[] row = (Object[]) it.next();
               			 		System.out.println("------------"+row[2]);
               			 		Double add= Math.log((Float)row[2]);
               			 		System.out.println("The probability for the trigram "+trigram+" is "+row[2]+" whose ln is "+add.floatValue());
               			 		System.out.println("CDL before was"+CDL);
               			 		CDL=CDL+add.floatValue();
               			 		System.out.println("CDL after is"+CDL);
               			 		K++;
               			 	}
          			 	}
          			 	
          			 	else // trigram exists in DB
          			 	{
          			 		for(Iterator it=query.iterate();it.hasNext();){
              			 		Object[] row = (Object[]) it.next();
              			 		Double add= Math.log((Float)row[2]);
              			 		System.out.println("The probability for the trigram "+trigram+" is "+row[2]+" whose ln is "+add.floatValue());
              			 		System.out.println("CDL before was"+CDL);
              			 		CDL=CDL+add.floatValue();
              			 		System.out.println("CDL after is"+CDL);
              			 		K++;
              			 	}
          			 	}
          			 
          		 }
          		 
          		 else 
         		 {
         			Integer count=h.get(trigram);
         			count++;
          			h.put(trigram,count);
          			//System.out.println("This bigram has already been considered");
         		 }
          		 
          	}
          	 firstword=secondword;
          	 secondword=word;
          	 word=readNextWord();   	 
          	 
           }
           System.out.println("The CDL is"+CDL);
           System.out.println("The no of unique trigrams is "+K);
           CDL=(Float)CDL/K*-1;
           
     	  
     	  System.out.println("CDL for document "+inputFileName+" is "+CDL);
     	  
     	  //Transaction tx1= session.beginTransaction();
     	  //Query query = session.createQuery("update ngrams.CDLScore3_POJO set CDLScore_Laplace_Smoothing_NonSpam_Data_Set='"+ CDL+"' where inputFileName= '"+inputFileName+"'");
     	  //int update = query.executeUpdate();
     	  //System.out.println("Updated "+update);
 		  //tx1.commit();
 
        }
       
      
      //####################################################
       
       public  void trigramCondLHLapalceSmoothing_Spam(String inputFileName,String inputFilePath) throws IOException
       {
     	  
     	  System.out.println(" i am here for "+inputFileName);
     	  HashMap <String,Integer> h=new HashMap<String,Integer>();//outer is the hashmap on first word
     	  
           String firstword=readNextWord();
           String secondword=readNextWord();
           
           String word = readNextWord();
           String trigram=null;
     
           Float CDL=0.0f;
           int K=0;//no of N Grams
         
           
           while(word!=null)
           {
         	  String previous2Words=firstword+" "+secondword;
         	  word = word.toLowerCase();
          	 if(word.endsWith("."))
                	word=word.substring(0, word.length()-1);
          	 
          	 if(word.endsWith(":"))
                 	word=word.substring(0, word.length()-1);
                
          	 if(previous2Words!=null)
          	 {
          		 trigram=previous2Words+" "+word;
          		 System.out.println("trigram being considered is "+trigram);
          		 
          		 if(!h.containsKey(trigram))
          		 {
          			 h.put(trigram,1);//first time inserting into outer hashmap
          			 
          			 Transaction tx= session.beginTransaction();
          			 //String SQL_QUERY="select bigram,count,probability from ngrams.BiGram_POJO where bigram=:bigram";
          			 String SQL_QUERY="select trigram,counter,probability from TriGram_POJO_LaplaceSmoothing_Spam where trigram=:wordinQuery";
          			 Query query=session.createQuery(SQL_QUERY);
          			 query.setParameter("wordinQuery",trigram);
          			
          			 
          			 	if(query.list().size()==0) //trigram does not exist in DB
          			 	{
          			 		 System.out.println("trigram does not exist :"+trigram);
          			 		 
          			 		 String temp[]=null;
          			 		 temp=trigram.split(" ");
          			 		 trigram="";
          			 		 
          			 		 for(int i=0;i<2;i++)
          			 		 {
          			 			 trigram=trigram+temp[i]+" ";
          			 		 }
          			 		 
          			 		 trigram=trigram+"<unknown>";
          			 		 System.out.println("Unknown trigram formed is :"+trigram);
          			 			 
          			 		 SQL_QUERY="select trigram,counter,probability from ngrams.TriGram_POJO_LaplaceSmoothing_Spam where trigram=:wordinQuery";
                 			 query=session.createQuery(SQL_QUERY);
                 			 query.setParameter("wordinQuery",trigram);
                 			 
                 			 for(Iterator it=query.iterate();it.hasNext();){
               			 		Object[] row = (Object[]) it.next();
               			 		System.out.println("------------"+row[2]);
               			 		Double add= Math.log((Float)row[2]);
               			 		System.out.println("The probability for the trigram "+trigram+" is "+row[2]+" whose ln is "+add.floatValue());
               			 		System.out.println("CDL before was"+CDL);
               			 		CDL=CDL+add.floatValue();
               			 		System.out.println("CDL after is"+CDL);
               			 		K++;
               			 	}
          			 	}
          			 	
          			 	else // trigram exists in DB
          			 	{
          			 		for(Iterator it=query.iterate();it.hasNext();){
              			 		Object[] row = (Object[]) it.next();
              			 		Double add= Math.log((Float)row[2]);
              			 		System.out.println("The probability for the trigram "+trigram+" is "+row[2]+" whose ln is "+add.floatValue());
              			 		System.out.println("CDL before was"+CDL);
              			 		CDL=CDL+add.floatValue();
              			 		System.out.println("CDL after is"+CDL);
              			 		K++;
              			 	}
          			 	}
          			 
          		 }
          		 
          		 else 
         		 {
         			Integer count=h.get(trigram);
         			count++;
          			h.put(trigram,count);
          			//System.out.println("This bigram has already been considered");
         		 }
          		 
          	}
          	 firstword=secondword;
          	 secondword=word;
          	 word=readNextWord();   	 
          	 
           }
           System.out.println("The CDL is"+CDL);
           System.out.println("The no of unique trigrams is "+K);
           CDL=(Float)CDL/K*-1;
           
     	  
     	  System.out.println("CDL for document "+inputFileName+" is "+CDL);
     	  
     	  Transaction tx1= session.beginTransaction();
     	  Query query = session.createQuery("update ngrams.CDLScore3_POJO set CDLScore_Laplace_Smoothing_Spam_Data_Set='"+ CDL+"' where inputFileName= '"+inputFileName+"'");
     	  int update = query.executeUpdate();
     	  System.out.println("Updated "+update);
 		  tx1.commit();
 
        }
       
       public  void trigramCondLHGoodturing(String inputFileName,String inputFilePath) throws IOException
       {
     	  
     	  System.out.println(" i am here for "+inputFileName);
     	  HashMap <String,Integer> h=new HashMap<String,Integer>();//outer is the hashmap on first word
     	  
           String firstword=readNextWord();
           String secondword=readNextWord();
           
           String word = readNextWord();
           String trigram=null;
     
           Float CDL=0.0f;
           int K=0;//no of N Grams
         
           
           while(word!=null)
           {
         	  String previous2Words=firstword+" "+secondword;
         	  word = word.toLowerCase();
          	 if(word.endsWith("."))
                	word=word.substring(0, word.length()-1);
          	 
          	 if(word.endsWith(":"))
                 	word=word.substring(0, word.length()-1);
                
          	 if(previous2Words!=null)
          	 {
          		 trigram=previous2Words+" "+word;
          		 System.out.println("trigram being considered is "+trigram);
          		 
          		 if(!h.containsKey(trigram))
          		 {
          			 h.put(trigram,1);//first time inserting into outer hashmap
          			 
          			 Transaction tx= session.beginTransaction();
          			 //String SQL_QUERY="select bigram,count,probability from ngrams.BiGram_POJO where bigram=:bigram";
          			 String SQL_QUERY="select trigram,counter,probability from TriGram_POJO_GoodTuring_NonSpam where trigram=:wordinQuery";
          			 Query query=session.createQuery(SQL_QUERY);
          			 query.setParameter("wordinQuery",trigram);
          			 
          			 	if(query.list().size()==0) //trigram does not exist in DB
          			 	{
          			 		 System.out.println("trigram does not exist :"+trigram);
          			 		 
          			 		 String temp[]=null;
          			 		 temp=trigram.split(" ");
          			 		 trigram="";
          			 		 
          			 		 for(int i=0;i<2;i++)
          			 		 {
          			 			 trigram=trigram+temp[i]+" ";
          			 		 }
          			 		 
          			 		 trigram=trigram+"<unknown>";
          			 		 System.out.println("Unknown trigram formed is :"+trigram);
          			 			 
          			 		 SQL_QUERY="select trigram,counter,probability from TriGram_POJO_GoodTuring_NonSpam where trigram=:wordinQuery";
                 			 query=session.createQuery(SQL_QUERY);
                 			 query.setParameter("wordinQuery",trigram);
                 			 
                 			 for(Iterator it=query.iterate();it.hasNext();){
               			 		Object[] row = (Object[]) it.next();
               			 		System.out.println("------------"+row[2]);
               			 		Double add= Math.log((Float)row[2]);
               			 		System.out.println("The probability for the trigram "+trigram+" is "+row[2]+" whose ln is "+add.floatValue());
               			 		System.out.println("CDL before was"+CDL);
               			 		CDL=CDL+add.floatValue();
               			 		System.out.println("CDL after is"+CDL);
               			 		K++;
               			 	}
          			 	}
          			 	
          			 	else // trigram exists in DB
          			 	{
          			 		for(Iterator it=query.iterate();it.hasNext();){
              			 		Object[] row = (Object[]) it.next();
              			 		Double add= Math.log((Float)row[2]);
              			 		System.out.println("The probability for the trigram "+trigram+" is "+row[2]+" whose ln is "+add.floatValue());
              			 		System.out.println("CDL before was"+CDL);
              			 		CDL=CDL+add.floatValue();
              			 		System.out.println("CDL after is"+CDL);
              			 		K++;
              			 	}
          			 	}
          			 
          		 }
          		 
          		 else 
         		 {
         			Integer count=h.get(trigram);
         			count++;
          			h.put(trigram,count);
          			//System.out.println("This bigram has already been considered");
         		 }
          		 
          	}
          	 firstword=secondword;
          	 secondword=word;
          	 word=readNextWord();   	 
          	 
           }
           System.out.println("The CDL is"+CDL);
           System.out.println("The no of unique trigrams is "+K);
           CDL=(Float)CDL/K*-1;
           
     	  
     	  System.out.println("CDL for document "+inputFileName+" is "+CDL);
     	  
     	  //Transaction tx1= session.beginTransaction();
     	  //Query query = session.createQuery("update ngrams.CDLScore3_POJO set CDLScore_Good_turing_NonSpam_Data_Set='"+ CDL+"' where inputFileName= '"+inputFileName+"'");
     	  //int update = query.executeUpdate();
     	  //System.out.println("Updated "+update);
 		  //tx1.commit();
 
        }
       
      
      //####################################################
       
       public  void trigramCondLHgoodturning_Spam(String inputFileName,String inputFilePath) throws IOException
       {
     	  
     	  System.out.println(" i am here for "+inputFileName);
     	  HashMap <String,Integer> h=new HashMap<String,Integer>();//outer is the hashmap on first word
     	  
           String firstword=readNextWord();
           String secondword=readNextWord();
           
           String word = readNextWord();
           String trigram=null;
     
           Float CDL=0.0f;
           int K=0;//no of N Grams
         
           
           while(word!=null)
           {
         	  String previous2Words=firstword+" "+secondword;
         	  word = word.toLowerCase();
          	 if(word.endsWith("."))
                	word=word.substring(0, word.length()-1);
          	 
          	 if(word.endsWith(":"))
                 	word=word.substring(0, word.length()-1);
                
          	 if(previous2Words!=null)
          	 {
          		 trigram=previous2Words+" "+word;
          		 System.out.println("trigram being considered is "+trigram);
          		 
          		 if(!h.containsKey(trigram))
          		 {
          			 h.put(trigram,1);//first time inserting into outer hashmap
          			 
          			 Transaction tx= session.beginTransaction();
          			 //String SQL_QUERY="select bigram,count,probability from ngrams.BiGram_POJO where bigram=:bigram";
         			 String SQL_QUERY="select trigram,counter,probability from TriGram_POJO_GoodTuring_Spam where trigram=:wordinQuery";
          			 Query query=session.createQuery(SQL_QUERY);
          			 query.setParameter("wordinQuery",trigram);
          			 
         			 if(query.list().size()==0) //trigram does not exist in DB
          			 	{
          			 		 System.out.println("trigram does not exist :"+trigram);
          			 		 
          			 		 String temp[]=null;
          			 		 temp=trigram.split(" ");
          			 		 trigram="";
          			 		 
          			 		 for(int i=0;i<2;i++)
          			 		 {
          			 			 trigram=trigram+temp[i]+" ";
          			 		 }
          			 		 
          			 		 trigram=trigram+"<unknown>";
          			 		 System.out.println("Unknown trigram formed is :"+trigram);
          			 			 
         			 		 SQL_QUERY="select trigram,counter,probability from TriGram_POJO_GoodTuring_Spam where trigram=:wordinQuery";
                 			 query=session.createQuery(SQL_QUERY);
                 			 query.setParameter("wordinQuery",trigram);
                 			 
                 			 for(Iterator it=query.iterate();it.hasNext();){
               			 		Object[] row = (Object[]) it.next();
               			 		System.out.println("------------"+row[2]);
               			 		Double add= Math.log((Float)row[2]);
               			 		System.out.println("The probability for the trigram "+trigram+" is "+row[2]+" whose ln is "+add.floatValue());
               			 		System.out.println("CDL before was"+CDL);
               			 		CDL=CDL+add.floatValue();
               			 		System.out.println("CDL after is"+CDL);
               			 		K++;
               			 	}
          			 	}
          			 	
          			 	else // trigram exists in DB
          			 	{
          			 		for(Iterator it=query.iterate();it.hasNext();){
              			 		Object[] row = (Object[]) it.next();
              			 		Double add= Math.log((Float)row[2]);
              			 		System.out.println("The probability for the trigram "+trigram+" is "+row[2]+" whose ln is "+add.floatValue());
              			 		System.out.println("CDL before was"+CDL);
              			 		CDL=CDL+add.floatValue();
              			 		System.out.println("CDL after is"+CDL);
              			 		K++;
              			 	}
          			 	}
          			 
          		 }
          		 
          		 else 
         		 {
         			Integer count=h.get(trigram);
         			count++;
          			h.put(trigram,count);
         		 }
          		 
          	}
          	 firstword=secondword;
          	 secondword=word;
          	 word=readNextWord();   	 
          	 
           }
           System.out.println("The CDL is"+CDL);
           System.out.println("The no of unique trigrams is "+K);
           CDL=(Float)CDL/K*-1;
           
     	  
     	  System.out.println("CDL for document "+inputFileName+" is "+CDL);
     	  
    	 /* Transaction tx1= session.beginTransaction();
     	  Query query = session.createQuery("update ngrams.CDLScore3_POJO set CDLScore_Good_turing_Spam_Data_Set='"+ CDL+"' where inputFileName= '"+inputFileName+"'");
     	  int update = query.executeUpdate();
     	  System.out.println("Updated "+update);
 		  tx1.commit();
		 */
        }
 
      
 
    /**
     * Read the next word from TextIO, if there is one.  First, skip past
     * any non-letters in the input.  If an end-of-file is encountered before
     * a word is found, return null.  Otherwise, read and return the word.
     * A word is defined as a sequence of letters.  Also, a word can include
     * an apostrophe if the apostrophe is surrounded by letters on each side.
     * @return the next word from TextIO, or null if an end-of-file is encountered
     */
    private static String readNextWord() {
       char ch = CDLHTextIO.peek(); // Look at next character in input.
       while (ch != CDLHTextIO.EOF && ! Character.isLetterOrDigit(ch)) {
     	  CDLHTextIO.getAnyChar();  // Read the character.
          ch = CDLHTextIO.peek();   // Look at the next character.
       }
       if (ch == CDLHTextIO.EOF) // Encountered end-of-file
          return null;
       // At this point, we know that the next character, so read a word.
       String word = "";  // This will be the word that is read.
       while (true) {
          word += CDLHTextIO.getAnyChar();  // Append the letter onto word.
          ch = CDLHTextIO.peek();  // Look at next character.
          if ( ch == '\'' ) {
                 // The next character is an apostrophe.  Read it, and
                 // if the following character is a letter, add both the
                 // apostrophe and the letter onto the word and continue
                 // reading the word.  If the character after the apostrophe
                 // is not a letter, the word is done, so break out of the loop.
         	 CDLHTextIO.getAnyChar();   // Read the apostrophe.
             ch = CDLHTextIO.peek();    // Look at char that follows apostrophe.
             if (Character.isLetter(ch)) {
                word += "\'" + CDLHTextIO.getAnyChar();
                ch = CDLHTextIO.peek();  // Look at next char.
             }
             else
                break;
          }
          if ( ! Character.isLetterOrDigit(ch)&&ch!='@'&&ch!='.'&&ch!='_'&&ch!='/'&&ch!=':') {
                 // If the next character is not a letter, the word is
                 // finished, so bread out of the loop.
             break;
          }
          // If we haven't broken out of the loop, next char is a letter.
       }
       return word;  // Return the word that has been read.
    }
 
 
 } // end class WordCount
