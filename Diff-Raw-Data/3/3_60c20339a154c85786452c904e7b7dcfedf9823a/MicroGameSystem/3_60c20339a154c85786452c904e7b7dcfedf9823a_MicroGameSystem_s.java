 package net.intensicode.me;
 
 import net.intensicode.core.*;
 import net.intensicode.util.Log;
 
 public final class MicroGameSystem extends GameSystem
     {
     public MicroGameSystem( final SystemContext aSystemContext )
         {
         super( aSystemContext );
         }
 
     // From GameSystem
 
     protected final void throwWrappedExceptionToTellCallingSystemAboutBrokenGameSystem( final Exception aException )
         {
         throw new ChainedException( "failed showing error screen", aException );
         }
 
     protected final void fillInformationStrings()
         {
         for ( int idx = 0; idx < PROPERTIES.length; idx++ )
             {
             addProperty( PROPERTIES[ idx ] );
             }
         }
 
     // Implementation
 
     private void addProperty( final String aPropertyName )
         {
         try
             {
             final String value = System.getProperty( aPropertyName );
             if ( value != null && value.length() > 0 ) myInformationStrings.add( value );
             }
         catch ( final Throwable t )
             {
             Log.error( "Failed querying system property {}", aPropertyName, t );
             }
         }
 
     private static final String[] PROPERTIES =
             { "microedition.platform", "microedition.encoding", "microedition.configuration", "microedition.profiles",
             "microedition.locale", "microedition.m3g.version"};
     }
