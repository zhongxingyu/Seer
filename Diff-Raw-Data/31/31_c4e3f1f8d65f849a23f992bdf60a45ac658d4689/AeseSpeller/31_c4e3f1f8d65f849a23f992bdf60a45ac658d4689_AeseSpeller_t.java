 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package calliope;
 
 /**
  * Look words up in the aspell dictionary
  * @author desmond
  */
 public class AeseSpeller 
 {
     String lang;
     static 
     {  
         System.loadLibrary("AeseSpeller");
         System.loadLibrary("aspell");
     }
     public AeseSpeller( String lang ) throws Exception
     {
         this.lang = lang;
         if ( !initialise(lang) )
             throw new Exception("failed to initialise "+lang );
     }
     native boolean initialise( String lang );
     public native void cleanup();
     public native boolean hasWord( String word, String lang );
     public native String[] listDicts();
     /**
      * For testing
      * @param args 
      */
     public static void main( String[] args )
     {
         try
         {
             AeseSpeller as = new AeseSpeller("en_GB");
             if ( as.hasWord( "housing", "en_GB") )
                 System.out.println("Dictionary (en_GB) has housing");
             if ( as.hasWord( "pratiche", "it") )
                 System.out.println("Dictionary (it) has practiche");
             if ( as.hasWord( "progetto", "it") )
                 System.out.println("Dictionary (it) has progetto");
             if ( as.hasWord( "automobile", "en_GB") )
                 System.out.println("Dictionary (en_GB) has automobile");
             String[] dicts = as.listDicts();
             for ( int i=0;i<dicts.length;i++ )
             {
                 System.out.println(dicts[i]);
             }
             if ( dicts.length==0 )
                 System.out.println("no dicts!");
             as.cleanup();
         }
         catch ( Exception e )
         {
             System.out.println(e.getMessage());
         }
     }
 }
