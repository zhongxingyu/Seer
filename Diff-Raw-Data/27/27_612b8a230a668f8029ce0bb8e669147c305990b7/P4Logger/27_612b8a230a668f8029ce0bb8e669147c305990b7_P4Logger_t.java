 package p4idea;
 
 import com.intellij.ide.plugins.PluginManager;
 import com.intellij.openapi.diagnostic.Logger;
 
 public class P4Logger
 {
   private static final P4Logger INSTANCE = new P4Logger();
 
   private P4Logger()
   {
   }
 
   public static P4Logger getInstance()
   {
     return INSTANCE;
   }
 
   public void log( String message )
   {
     Logger.getInstance( "P4" ).info( message );
   }
 
   public void error( String message, Throwable t )
   {
    Logger.getInstance( "P4" ).error( message, t );
   }
 }
