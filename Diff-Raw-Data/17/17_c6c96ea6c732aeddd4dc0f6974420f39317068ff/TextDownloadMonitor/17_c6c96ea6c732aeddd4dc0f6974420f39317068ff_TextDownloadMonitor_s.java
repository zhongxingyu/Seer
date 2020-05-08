 package cm.generic;
 
 import cm.media.text.NewPorterStemmer;
 
 import java.util.*;
 
 /**
  * A DownloadMonitor that processes text.
  * It makes one NewPorterStemmer's available to each thread.
  */
 public class TextDownloadMonitor
 extends
 DownloadMonitor
 {
    static HashMap	stemmersHash	= new HashMap();
    
    public TextDownloadMonitor(String name, int numDownloadThreads, 
 			  boolean getUrgent)
    {
       super(name, numDownloadThreads, getUrgent);
    }

    protected Thread makeDownloadThread(int i, String s)
    {
       Thread result = super.makeDownloadThread(i, s);
       debugA("creating stemmer for " + result);
       
       addStemmer(result);
 
       return result;
    }
    
    public static void addStemmer()
    {
       addStemmer(Thread.currentThread());
    }
    static void addStemmer(Thread thread)
    {
       NewPorterStemmer stemmer			= new NewPorterStemmer();
       stemmersHash.put(thread, stemmer);
    }
 
    public static NewPorterStemmer getStemmer()
    {
       Thread currentThread		= Thread.currentThread();
       NewPorterStemmer stemmer		= 
 	 (NewPorterStemmer) stemmersHash.get(currentThread);
       return stemmer;
    }
 }
 
