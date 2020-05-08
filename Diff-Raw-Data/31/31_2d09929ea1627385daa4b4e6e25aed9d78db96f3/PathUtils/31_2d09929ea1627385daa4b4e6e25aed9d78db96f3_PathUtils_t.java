 package com.redhat.tools.nexus.request;
 
 import java.io.File;
 
 public final class PathUtils
 {
 
     // private static final Logger logger = LoggerFactory.getLogger( PathUtils.class );
 
     private static final char PATH_SLASH = File.separatorChar;
 
     private PathUtils()
     {
     }
 
     public static String joinPath( final char separator, final String basePath, final String... parts )
     {
         return basePath == null ? null : concat( separator, basePath, concat( separator, parts ) );
     }
 
     /**
      * NOTE: This doesn't normalize mixed-file-separator cases, where '/' is mixed with '\' in paths.
      */
     public static File joinFile( final File dir, final String... parts )
     {
         if ( dir == null )
         {
             return null;
         }
 
         final String path = concat( PATH_SLASH, parts );
 
         final File f = new File( dir, path );
 
         return f;
     }
 
    /**
     * NOTE: This doesn't normalize mixed-file-separator cases, where '/' is mixed with '\' in paths.
     */
    public static File joinFile( final String dir, final String... parts )
    {
        if ( dir == null )
        {
            return null;
        }

        final String path = concat( PATH_SLASH, parts );

        final File f = new File( dir, path );

        return f;
    }

     private static String concat( final char separator, final String... parts )
     {
         final StringBuilder builder = new StringBuilder();
         for ( final String part : parts )
         {
             if ( part == null )
             {
                 continue;
             }
 
             if ( builder.length() > 0 )
             {
                 if ( builder.charAt( builder.length() - 1 ) != separator && part.length() > 0
                     && part.charAt( 0 ) != separator )
                 {
                     builder.append( separator );
                 }
 
                 builder.append( part );
             }
             else if ( part.length() > 0 && part.charAt( 0 ) == separator )
             {
                 builder.append( part.substring( 1 ) );
             }
             else
             {
                 builder.append( part );
             }
         }
 
         return builder.toString();
     }
 
     public static String buildUri( final String applicationUrl, final String... parts )
     {
         return joinPath( '/', applicationUrl, parts );
     }
 
 }
